package net.luck.narutoaddon.OtherCode.akatsuki.core;

import net.luck.narutoaddon.OtherCode.akatsuki.bounty.BountyEntry;
import net.luck.narutoaddon.OtherCode.akatsuki.mission.LeaderMission;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AkatsukiSavedData extends WorldSavedData {
   // Rimosso "InfTsuk" dal nome del salvataggio
   public static final String DATA_NAME = "LuckAkatsukiData";
   private final Map<UUID, AkatsukiMember> members = new ConcurrentHashMap<>();
   private final Set<UUID> pendingInvites = ConcurrentHashMap.newKeySet();
   private final List<BountyEntry> activeBounties = new CopyOnWriteArrayList<>();
   private final Map<UUID, Map<String, Integer>> allianceContributions = new ConcurrentHashMap<>();
   private final Map<UUID, LeaderMission> leaderMissions = new ConcurrentHashMap<>();
   private final Map<UUID, Long> zetsuCooldowns = new ConcurrentHashMap<>();
   private NBTTagCompound missionsNBT = new NBTTagCompound();
   private NBTTagCompound contractsNBT = new NBTTagCompound();
   private NBTTagCompound raidNBT = new NBTTagCompound();

   public AkatsukiSavedData() {
      super(DATA_NAME);
   }

   public AkatsukiSavedData(String name) {
      super(name);
   }

   public AkatsukiMember getMember(UUID playerId) {
      return this.members.get(playerId);
   }

   public void addMember(UUID playerId, AkatsukiMember member) {
      this.members.put(playerId, member);
      this.markDirty();
   }

   public void removeMember(UUID playerId) {
      this.members.remove(playerId);
      this.markDirty();
   }

   public boolean isMember(UUID playerId) {
      return this.members.containsKey(playerId);
   }

   public Map<UUID, AkatsukiMember> getAllMembers() {
      return this.members;
   }

   public void resetMemberProgression(UUID playerId) {
      AkatsukiMember member = this.members.get(playerId);
      if (member != null) {
         int[] levels = member.getRingUpgradeLevels();
         if (levels != null) {
            Arrays.fill(levels, 0);
         }

         member.setReputation(0);
         int tokens = member.getBountyTokens();
         if (tokens != 0) {
            member.addBountyTokens(-tokens);
         }

         this.markDirty();
      }
   }

   // --- Logica Bounty e Missioni (Pulita) ---

   public void addAllianceContribution(UUID playerId, String type, int amount) {
      Map<String, Integer> contributions = this.allianceContributions.computeIfAbsent(playerId, k -> new HashMap<>());
      contributions.put(type, contributions.getOrDefault(type, 0) + amount);
      this.markDirty();
   }

   // --- Lettura NBT (Deoffuscata) ---
   @Override
   public void readFromNBT(NBTTagCompound nbt) {
      this.members.clear();
      // getTagList(String, int) - 10 è il tipo per NBTTagCompound
      NBTTagList memberList = nbt.getTagList("members", 10);

      for(int i = 0; i < memberList.tagCount(); ++i) {
         NBTTagCompound tag = memberList.getCompoundTagAt(i);
         AkatsukiMember member = new AkatsukiMember();
         member.readFromNBT(tag);
         this.members.put(member.getPlayerId(), member);
      }

      this.pendingInvites.clear();
      NBTTagList inviteList = nbt.getTagList("pendingInvites", 10);
      for(int i = 0; i < inviteList.tagCount(); ++i) {
         NBTTagCompound tag = inviteList.getCompoundTagAt(i);
         try {
            this.pendingInvites.add(UUID.fromString(tag.getString("uuid")));
         } catch (IllegalArgumentException ignored) {}
      }

      // Correzione lettura contributi alleanza per evitare errori di tipo
      this.allianceContributions.clear();
      NBTTagList allianceList = nbt.getTagList("allianceContributions", 10);
      for(int i = 0; i < allianceList.tagCount(); ++i) {
         NBTTagCompound playerTag = allianceList.getCompoundTagAt(i);
         try {
            UUID playerId = UUID.fromString(playerTag.getString("uuid"));
            Map<String, Integer> contributions = new HashMap<>();
            NBTTagList contribList = playerTag.getTagList("contributions", 10);
            for(int j = 0; j < contribList.tagCount(); ++j) {
               NBTTagCompound contribTag = contribList.getCompoundTagAt(j);
               contributions.put(contribTag.getString("type"), contribTag.getInteger("amount"));
            }
            this.allianceContributions.put(playerId, contributions);
         } catch (Exception ignored) {}
      }

      // Caricamento NBT Missioni, Contratti e Raid
      this.missionsNBT = nbt.hasKey("activeMissions", 10) ? nbt.getCompoundTag("activeMissions") : new NBTTagCompound();
      this.contractsNBT = nbt.hasKey("activeContracts", 10) ? nbt.getCompoundTag("activeContracts") : new NBTTagCompound();
      this.raidNBT = nbt.hasKey("activeRaid", 10) ? nbt.getCompoundTag("activeRaid") : new NBTTagCompound();
   }

   // --- Scrittura NBT (Risolto errore Entry Object) ---
   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagList memberList = new NBTTagList();
      for(AkatsukiMember member : this.members.values()) {
         memberList.appendTag(member.writeToNBT());
      }
      nbt.setTag("members", memberList);

      // Correzione Errore Immagine: Specificati tipi corretti nel ciclo per evitare "Incompatible Types"
      NBTTagList allianceList = new NBTTagList();
      for(Map.Entry<UUID, Map<String, Integer>> entry : this.allianceContributions.entrySet()) {
         NBTTagCompound playerTag = new NBTTagCompound();
         playerTag.setString("uuid", entry.getKey().toString());

         NBTTagList contribList = new NBTTagList();
         // Qui forziamo il tipo Map.Entry<String, Integer> per risolvere il bug della foto
         for(Map.Entry<String, Integer> contrib : entry.getValue().entrySet()) {
            NBTTagCompound contribTag = new NBTTagCompound();
            contribTag.setString("type", contrib.getKey());
            contribTag.setInteger("amount", contrib.getValue());
            contribList.appendTag(contribTag);
         }
         playerTag.setTag("contributions", contribList);
         allianceList.appendTag(playerTag);
      }
      nbt.setTag("allianceContributions", allianceList);

      nbt.setTag("activeMissions", this.missionsNBT);
      nbt.setTag("activeContracts", this.contractsNBT);
      nbt.setTag("activeRaid", this.raidNBT);
      return nbt;
   }

   public static AkatsukiSavedData get(World world) {
      MapStorage storage = world.getMapStorage();
      AkatsukiSavedData data = (AkatsukiSavedData)storage.getOrLoadData(AkatsukiSavedData.class, DATA_NAME);
      if (data == null) {
         data = new AkatsukiSavedData();
         storage.setData(DATA_NAME, data);
      }
      return data;
   }
}