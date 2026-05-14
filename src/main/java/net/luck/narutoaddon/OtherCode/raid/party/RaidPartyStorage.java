
package net.luck.narutoaddon.OtherCode.raid.party;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class RaidPartyStorage extends WorldSavedData {
   private static final String DATA_NAME = "RaidPartyStorage";
   private Map<UUID, RaidParty> parties = new HashMap();
   private Map<UUID, UUID> playerToParty = new HashMap();

   public RaidPartyStorage() {
      super("RaidPartyStorage");
   }

   public RaidPartyStorage(String name) {
      super(name);
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.parties.clear();
      this.playerToParty.clear();
      NBTTagList partyList = nbt.getTagList("parties", 10);

      for(int i = 0; i < partyList.tagCount(); ++i) {
         NBTTagCompound partyTag = partyList.getCompoundTagAt(i);
         RaidParty party = RaidParty.readFromNBT(partyTag);
         this.parties.put(party.getPartyId(), party);

         for(UUID memberUUID : party.getMemberUUIDs()) {
            this.playerToParty.put(memberUUID, party.getPartyId());
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagList partyList = new NBTTagList();

      for(RaidParty party : this.parties.values()) {
         if (party.getState() != RaidParty.PartyState.COMPLETED) {
            partyList.appendTag(party.writeToNBT());
         }
      }

      nbt.setTag("parties", partyList);
      return nbt;
   }

   public void registerParty(RaidParty party) {
      this.parties.put(party.getPartyId(), party);

      for(UUID memberUUID : party.getMemberUUIDs()) {
         this.playerToParty.put(memberUUID, party.getPartyId());
      }

      this.markDirty();
   }

   public void removeParty(UUID partyId) {
      RaidParty party = (RaidParty)this.parties.remove(partyId);
      if (party != null) {
         for(UUID memberUUID : party.getMemberUUIDs()) {
            this.playerToParty.remove(memberUUID);
         }

         this.markDirty();
      }

   }

   public RaidParty getParty(UUID partyId) {
      return (RaidParty)this.parties.get(partyId);
   }

   public RaidParty getPlayerParty(UUID playerUUID) {
      UUID partyId = (UUID)this.playerToParty.get(playerUUID);
      return partyId != null ? (RaidParty)this.parties.get(partyId) : null;
   }

   public boolean isInParty(UUID playerUUID) {
      return this.playerToParty.containsKey(playerUUID);
   }

   public void updatePlayerMapping(UUID playerUUID, UUID partyId) {
      if (partyId == null) {
         this.playerToParty.remove(playerUUID);
      } else {
         this.playerToParty.put(playerUUID, partyId);
      }

      this.markDirty();
   }

   public void cleanupCompletedParties() {
      List<UUID> toRemove = new ArrayList();

      for(Map.Entry<UUID, RaidParty> entry : this.parties.entrySet()) {
         if (((RaidParty)entry.getValue()).getState() == RaidParty.PartyState.COMPLETED) {
            toRemove.add(entry.getKey());
         }
      }

      for(UUID partyId : toRemove) {
         this.removeParty(partyId);
      }

   }

   public Collection<RaidParty> getAllParties() {
      return Collections.unmodifiableCollection(this.parties.values());
   }

   public int getPartyCount() {
      return this.parties.size();
   }

   public static RaidPartyStorage get(World world) {
      if (world != null && world.getMapStorage() != null) {
         MapStorage storage = world.getMapStorage();
         RaidPartyStorage instance = (RaidPartyStorage)storage.getOrLoadData(RaidPartyStorage.class, "RaidPartyStorage");
         if (instance == null) {
            instance = new RaidPartyStorage();
            storage.setData("RaidPartyStorage", instance);
         }

         return instance;
      } else {
         return null;
      }
   }
}
