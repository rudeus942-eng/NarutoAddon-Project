package net.luck.narutoaddon.OtherCode.akatsuki.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AkatsukiMember {
   private UUID playerId;
   private AkatsukiRing ring;
   private int reputation;
   private UUID partnerId;
   private long joinTimestamp;
   private final Map<String, Integer> heatPerVillage;
   private int bountyTokens;
   private final int[] ringUpgradeLevels;
   private boolean isLeader;

   public AkatsukiMember(UUID playerId, AkatsukiRing ring) {
      this.heatPerVillage = new HashMap();
      this.ringUpgradeLevels = new int[5];
      this.playerId = playerId;
      this.ring = ring;
      this.reputation = 0;
      this.partnerId = null;
      this.joinTimestamp = System.currentTimeMillis();
      this.bountyTokens = 0;
      this.isLeader = false;
   }

   public AkatsukiMember() {
      this(UUID.randomUUID(), AkatsukiRing.REI);
   }

   public UUID getPlayerId() {
      return this.playerId;
   }

   public AkatsukiRing getRing() {
      return this.ring;
   }

   public void setRing(AkatsukiRing ring) {
      this.ring = ring;
   }

   public int getReputation() {
      return this.reputation;
   }

   public void setReputation(int reputation) {
      this.reputation = Math.max(0, reputation);
   }

   public UUID getPartnerId() {
      return this.partnerId;
   }

   public void setPartnerId(UUID partnerId) {
      this.partnerId = partnerId;
   }

   public long getJoinTimestamp() {
      return this.joinTimestamp;
   }

   public boolean isLeader() {
      return this.isLeader;
   }

   public void setLeader(boolean leader) {
      this.isLeader = leader;
   }

   public int getBountyTokens() {
      return this.bountyTokens;
   }

   public AkatsukiRank getRank() {
      return AkatsukiRank.fromReputation(this.reputation);
   }

   public void addReputation(int amount) {
      this.reputation = Math.max(0, this.reputation + amount);
   }

   public void addHeat(String village, int amount) {
      int current = (Integer)this.heatPerVillage.getOrDefault(village, 0);
      this.heatPerVillage.put(village, Math.min(100, Math.max(0, current + amount)));
   }

   public int getHeat(String village) {
      return (Integer)this.heatPerVillage.getOrDefault(village, 0);
   }

   public void decayHeat(int amount) {
      this.heatPerVillage.entrySet().removeIf((entry) -> {
         entry.setValue((Integer)entry.getValue() - amount);
         return (Integer)entry.getValue() <= 0;
      });
   }

   public Map<String, Integer> getHeatMap() {
      return this.heatPerVillage;
   }

   public void addBountyTokens(int amount) {
      this.bountyTokens = Math.max(0, this.bountyTokens + amount);
   }

   public int getRingUpgradeLevel(int slot) {
      return slot >= 0 && slot < this.ringUpgradeLevels.length ? this.ringUpgradeLevels[slot] : 0;
   }

   public void upgradeRing(int slot) {
      if (slot >= 0 && slot < this.ringUpgradeLevels.length && this.ringUpgradeLevels[slot] < 5) {
         int var10002 = this.ringUpgradeLevels[slot]++;
      }

   }

   public int[] getRingUpgradeLevels() {
      return this.ringUpgradeLevels;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("playerId", this.playerId.toString());
      nbt.setInteger("ring", this.ring.ordinal());
      nbt.setInteger("reputation", this.reputation);
      if (this.partnerId != null) {
         nbt.setString("partnerId", this.partnerId.toString());
      }

      nbt.setLong("joinTimestamp", this.joinTimestamp);
      nbt.setInteger("bountyTokens", this.bountyTokens);
      NBTTagList heatList = new NBTTagList();

      for(Map.Entry<String, Integer> entry : this.heatPerVillage.entrySet()) {
         NBTTagCompound heatTag = new NBTTagCompound();
         heatTag.setString("village", (String)entry.getKey());
         heatTag.setInteger("heat", (Integer)entry.getValue());
         heatList.appendTag(heatTag);
      }

      nbt.setTag("heatPerVillage", heatList);
      nbt.setIntArray("ringUpgradeLevels", this.ringUpgradeLevels);
      nbt.setBoolean("isLeader", this.isLeader);
      return nbt;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      try {
         this.playerId = UUID.fromString(nbt.getString("playerId"));
      } catch (IllegalArgumentException var6) {
         this.playerId = UUID.randomUUID();
      }

      this.ring = AkatsukiRing.getByIndex(nbt.getInteger("ring"));
      this.reputation = nbt.getInteger("reputation");
      if (nbt.hasKey("partnerId")) {
         try {
            this.partnerId = UUID.fromString(nbt.getString("partnerId"));
         } catch (IllegalArgumentException var5) {
            this.partnerId = null;
         }
      } else {
         this.partnerId = null;
      }

      this.joinTimestamp = nbt.getLong("joinTimestamp");
      this.bountyTokens = nbt.getInteger("bountyTokens");
      this.heatPerVillage.clear();
      NBTTagList heatList = nbt.getTagList("heatPerVillage", 10);

      for(int i = 0; i < heatList.tagCount(); ++i) {
         NBTTagCompound heatTag = heatList.getCompoundTagAt(i);
         this.heatPerVillage.put(heatTag.getString("village"), heatTag.getInteger("heat"));
      }

      int[] saved = nbt.getIntArray("ringUpgradeLevels");

      for(int i = 0; i < this.ringUpgradeLevels.length; ++i) {
         this.ringUpgradeLevels[i] = i < saved.length ? saved[i] : 0;
      }

      this.isLeader = nbt.getBoolean("isLeader");
   }
}
