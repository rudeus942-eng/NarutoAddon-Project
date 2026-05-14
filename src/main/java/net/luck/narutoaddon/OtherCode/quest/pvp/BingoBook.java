
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class BingoBook {
   private static BingoBook INSTANCE;
   private final List<BingoEntry> activeEntries = new ArrayList();
   private static final int MAX_ENTRIES_PER_REFRESH = 1;
   private static final int BASE_NINJA_XP_PER_1000 = 18;
   private static final int BASE_PVP_XP_PER_1000 = 28;
   private static final int MIN_NINJA_XP = 70;
   private static final int MIN_PVP_XP = 22;
   private static final int BASE_RYO_PER_1000 = 20;
   private static final int MIN_RYO = 66;
   public static final int ACCEPT_OK = 0;
   public static final int ACCEPT_INVALID = 1;
   public static final int ACCEPT_SELF = 2;
   public static final int ACCEPT_ALREADY_CLAIMED = 3;
   public static final int ACCEPT_DAILY_LIMIT = 4;

   private BingoBook() {
   }

   public static BingoBook getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new BingoBook();
      }

      return INSTANCE;
   }

   public void refresh(World world) {
      MinecraftServer server = world.getMinecraftServer();
      if (server != null) {
         long now = System.currentTimeMillis();
         this.activeEntries.removeIf((e) -> now - e.postedAtMs > 172800000L);
         List<PlayerXpEntry> allCandidates = new ArrayList();
         Set<UUID> alreadyListed = new HashSet();

         for(BingoEntry existing : this.activeEntries) {
            alreadyListed.add(existing.targetUUID);
         }

         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            VillageHelper.Village village = VillageHelper.getVillage(player);
            if (village != VillageHelper.Village.UNKNOWN && !alreadyListed.contains(player.getUniqueID())) {
               int battleXp = getBattleXp(player);
               if (battleXp >= 100) {
                  allCandidates.add(new PlayerXpEntry(player.getUniqueID(), player.getName(), village, battleXp));
               }
            }
         }

         allCandidates.sort((a, b) -> Integer.compare(b.battleXp, a.battleXp));
         int toAdd = Math.min(1, allCandidates.size());

         for(int i = 0; i < toAdd; ++i) {
            PlayerXpEntry pxe = (PlayerXpEntry)allCandidates.get(i);
            int ninjaXp = Math.max(70, pxe.battleXp / 1000 * 18);
            int pvpXp = Math.max(22, pxe.battleXp / 1000 * 28);
            int ryoAmount = Math.max(66, pxe.battleXp / 1000 * 20);
            this.activeEntries.add(new BingoEntry(pxe.uuid, pxe.name, pxe.village.teamName, ninjaXp, pvpXp, ryoAmount, now));
         }

         PvpSavedData data = PvpSavedData.get(world);
         data.setLastBingoRotation(now);
         data.markDirty();
      }
   }

   public int acceptEntry(UUID hunterUUID, int entryIndex, World world) {
      if (entryIndex >= 0 && entryIndex < this.activeEntries.size()) {
         BingoEntry entry = (BingoEntry)this.activeEntries.get(entryIndex);
         if (entry.targetUUID.equals(hunterUUID)) {
            return 2;
         } else if (!entry.claimedBy.isEmpty()) {
            return 3;
         } else {
            PvpSavedData data = PvpSavedData.get(world);
            if (data.hasBingoClaimed(hunterUUID)) {
               return 4;
            } else {
               entry.claimedBy.add(hunterUUID);
               data.recordBingoClaimed(hunterUUID);
               return 0;
            }
         }
      } else {
         return 1;
      }
   }

   @Nullable
   public BingoEntry onTargetKilled(UUID killerUUID, UUID victimUUID) {
      Iterator<BingoEntry> it = this.activeEntries.iterator();

      while(it.hasNext()) {
         BingoEntry entry = (BingoEntry)it.next();
         if (entry.targetUUID.equals(victimUUID) && entry.claimedBy.contains(killerUUID)) {
            it.remove();
            return entry;
         }
      }

      return null;
   }

   public void abandonEntry(UUID hunterUUID, int entryIndex) {
      if (entryIndex >= 0 && entryIndex < this.activeEntries.size()) {
         ((BingoEntry)this.activeEntries.get(entryIndex)).claimedBy.remove(hunterUUID);
      }
   }

   public List<BingoEntry> getActiveEntries() {
      return new ArrayList(this.activeEntries);
   }

   public List<BingoEntry> getEntriesForPlayer(UUID hunterUUID) {
      List<BingoEntry> result = new ArrayList();

      for(BingoEntry entry : this.activeEntries) {
         if (entry.claimedBy.contains(hunterUUID)) {
            result.add(entry);
         }
      }

      return result;
   }

   @Nullable
   public BingoEntry getEntry(int index) {
      return index >= 0 && index < this.activeEntries.size() ? (BingoEntry)this.activeEntries.get(index) : null;
   }

   public int getEntryCount() {
      return this.activeEntries.size();
   }

   public void setEntry(UUID targetUUID, String targetName, String targetVillage, int ninjaXpReward, int pvpXpReward) {
      this.activeEntries.add(new BingoEntry(targetUUID, targetName, targetVillage, ninjaXpReward, pvpXpReward, 0, System.currentTimeMillis()));
   }

   public void removeEntry(UUID targetUUID) {
      this.activeEntries.removeIf((b) -> b.targetUUID.equals(targetUUID));
   }

   public void clearAll() {
      this.activeEntries.clear();
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      NBTTagList list = new NBTTagList();

      for(BingoEntry entry : this.activeEntries) {
         list.appendTag(entry.writeToNBT());
      }

      nbt.setTag("entries", list);
      return nbt;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.activeEntries.clear();
      String key = nbt.hasKey("entries") ? "entries" : "bounties";
      if (nbt.hasKey(key)) {
         NBTTagList list = nbt.getTagList(key, 10);

         for(int i = 0; i < list.tagCount(); ++i) {
            BingoEntry entry = BingoEntry.readFromNBT(list.getCompoundTagAt(i));
            if (entry != null) {
               this.activeEntries.add(entry);
            }
         }
      }

   }

   private static int getBattleXp(EntityPlayerMP player) {
      try {
         return (int)player.getEntityData().getDouble("battle_experience");
      } catch (Exception var2) {
         return 0;
      }
   }

   public static class BingoEntry {
      public final UUID targetUUID;
      public final String targetName;
      public final String targetVillage;
      public final int ninjaXpReward;
      public final int pvpXpReward;
      public final int ryoReward;
      public final long postedAtMs;
      public final Set<UUID> claimedBy;

      public BingoEntry(UUID targetUUID, String targetName, String targetVillage, int ninjaXpReward, int pvpXpReward, int ryoReward, long postedAtMs) {
         this.targetUUID = targetUUID;
         this.targetName = targetName;
         this.targetVillage = targetVillage;
         this.ninjaXpReward = ninjaXpReward;
         this.pvpXpReward = pvpXpReward;
         this.ryoReward = ryoReward;
         this.postedAtMs = postedAtMs;
         this.claimedBy = new HashSet();
      }

      public NBTTagCompound writeToNBT() {
         NBTTagCompound nbt = new NBTTagCompound();
         nbt.setString("targetUUID", this.targetUUID.toString());
         nbt.setString("targetName", this.targetName);
         nbt.setString("targetVillage", this.targetVillage);
         nbt.setInteger("ninjaXpReward", this.ninjaXpReward);
         nbt.setInteger("pvpXpReward", this.pvpXpReward);
         nbt.setInteger("ryoReward", this.ryoReward);
         nbt.setLong("postedAt", this.postedAtMs);
         NBTTagList claimedList = new NBTTagList();

         for(UUID uuid : this.claimedBy) {
            claimedList.appendTag(new NBTTagString(uuid.toString()));
         }

         nbt.setTag("claimedBy", claimedList);
         return nbt;
      }

      public static BingoEntry readFromNBT(NBTTagCompound nbt) {
         UUID targetUUID;
         try {
            targetUUID = UUID.fromString(nbt.getString("targetUUID"));
         } catch (IllegalArgumentException var14) {
            return null;
         }

         String targetName = nbt.getString("targetName");
         String targetVillage = nbt.getString("targetVillage");
         int ninjaXpReward = nbt.getInteger("ninjaXpReward");
         int pvpXpReward = nbt.getInteger("pvpXpReward");
         int ryoReward = nbt.getInteger("ryoReward");
         long postedAt = nbt.getLong("postedAt");
         BingoEntry entry = new BingoEntry(targetUUID, targetName, targetVillage, ninjaXpReward, pvpXpReward, ryoReward, postedAt);
         NBTTagList claimedList = nbt.getTagList("claimedBy", 8);

         for(int i = 0; i < claimedList.tagCount(); ++i) {
            try {
               entry.claimedBy.add(UUID.fromString(claimedList.getStringTagAt(i)));
            } catch (IllegalArgumentException var13) {
            }
         }

         return entry;
      }
   }

   private static class PlayerXpEntry {
      final UUID uuid;
      final String name;
      final VillageHelper.Village village;
      final int battleXp;

      PlayerXpEntry(UUID uuid, String name, VillageHelper.Village village, int battleXp) {
         this.uuid = uuid;
         this.name = name;
         this.village = village;
         this.battleXp = battleXp;
      }
   }
}
