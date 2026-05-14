package net.luck.narutoaddon.OtherCode.akatsuki.raid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillageRaid {
   private UUID raidId;
   private String targetVillage;
   private int rallyX;
   private int rallyZ;
   private long startTime;
   private int durationMinutes;
   private RaidStatus status;
   private final Map<UUID, Boolean> participants;
   private int objectivesCompleted;
   private int objectiveCount;
   private int killsRequired;
   private int killsAchieved;
   private int tokenReward;
   private int repReward;
   private long preparingEndTime;

   public VillageRaid() {
      this.participants = new HashMap();
      this.raidId = UUID.randomUUID();
      this.durationMinutes = 30;
      this.status = RaidStatus.PREPARING;
      this.objectiveCount = 3;
      this.killsRequired = 5;
      this.tokenReward = 150;
      this.repReward = 200;
   }

   public VillageRaid(String targetVillage, int rallyX, int rallyZ, int durationMinutes) {
      this();
      this.targetVillage = targetVillage;
      this.rallyX = rallyX;
      this.rallyZ = rallyZ;
      this.durationMinutes = durationMinutes;
      this.startTime = System.currentTimeMillis();
      this.preparingEndTime = this.startTime + 180000L;
   }

   public UUID getRaidId() {
      return this.raidId;
   }

   public String getTargetVillage() {
      return this.targetVillage;
   }

   public int getRallyX() {
      return this.rallyX;
   }

   public int getRallyZ() {
      return this.rallyZ;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public int getDurationMinutes() {
      return this.durationMinutes;
   }

   public RaidStatus getStatus() {
      return this.status;
   }

   public Map<UUID, Boolean> getParticipants() {
      return this.participants;
   }

   public int getObjectivesCompleted() {
      return this.objectivesCompleted;
   }

   public int getObjectiveCount() {
      return this.objectiveCount;
   }

   public int getKillsRequired() {
      return this.killsRequired;
   }

   public int getKillsAchieved() {
      return this.killsAchieved;
   }

   public int getTokenReward() {
      return this.tokenReward;
   }

   public int getRepReward() {
      return this.repReward;
   }

   public long getPreparingEndTime() {
      return this.preparingEndTime;
   }

   public void setStatus(RaidStatus status) {
      this.status = status;
   }

   public void setKillsAchieved(int killsAchieved) {
      this.killsAchieved = killsAchieved;
   }

   public void setObjectivesCompleted(int objectivesCompleted) {
      this.objectivesCompleted = objectivesCompleted;
   }

   public void addParticipant(UUID playerId) {
      this.participants.putIfAbsent(playerId, false);
   }

   public void markArrived(UUID playerId) {
      this.participants.put(playerId, true);
   }

   public boolean isParticipant(UUID playerId) {
      return this.participants.containsKey(playerId);
   }

   public boolean hasArrived(UUID playerId) {
      return Boolean.TRUE.equals(this.participants.get(playerId));
   }

   public void incrementKills() {
      ++this.killsAchieved;
   }

   public long getDeadlineTime() {
      return this.preparingEndTime + (long)this.durationMinutes * 60L * 1000L;
   }

   public long getTimeRemainingMs() {
      return this.status == RaidStatus.PREPARING ? Math.max(0L, this.preparingEndTime - System.currentTimeMillis()) : Math.max(0L, this.getDeadlineTime() - System.currentTimeMillis());
   }

   public boolean isTimedOut() {
      return System.currentTimeMillis() > this.getDeadlineTime();
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("raidId", this.raidId.toString());
      nbt.setString("targetVillage", this.targetVillage != null ? this.targetVillage : "");
      nbt.setInteger("rallyX", this.rallyX);
      nbt.setInteger("rallyZ", this.rallyZ);
      nbt.setLong("startTime", this.startTime);
      nbt.setInteger("durationMinutes", this.durationMinutes);
      nbt.setInteger("status", this.status.ordinal());
      nbt.setInteger("objectivesCompleted", this.objectivesCompleted);
      nbt.setInteger("objectiveCount", this.objectiveCount);
      nbt.setInteger("killsRequired", this.killsRequired);
      nbt.setInteger("killsAchieved", this.killsAchieved);
      nbt.setInteger("tokenReward", this.tokenReward);
      nbt.setInteger("repReward", this.repReward);
      nbt.setLong("preparingEndTime", this.preparingEndTime);
      NBTTagList partList = new NBTTagList();

      for(Map.Entry<UUID, Boolean> entry : this.participants.entrySet()) {
         NBTTagCompound tag = new NBTTagCompound();
         tag.setString("uuid", ((UUID)entry.getKey()).toString());
         tag.setBoolean("arrived", (Boolean)entry.getValue());
         partList.appendTag(tag);
      }

      nbt.setTag("participants", partList);
      return nbt;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      try {
         this.raidId = UUID.fromString(nbt.getString("raidId"));
      } catch (IllegalArgumentException var9) {
         this.raidId = UUID.randomUUID();
      }

      this.targetVillage = nbt.getString("targetVillage");
      this.rallyX = nbt.getInteger("rallyX");
      this.rallyZ = nbt.getInteger("rallyZ");
      this.startTime = nbt.getLong("startTime");
      this.durationMinutes = nbt.getInteger("durationMinutes");
      int statusOrd = nbt.getInteger("status");
      this.status = statusOrd >= 0 && statusOrd < RaidStatus.values().length ? RaidStatus.values()[statusOrd] : RaidStatus.CANCELLED;
      this.objectivesCompleted = nbt.getInteger("objectivesCompleted");
      this.objectiveCount = nbt.getInteger("objectiveCount");
      this.killsRequired = nbt.getInteger("killsRequired");
      this.killsAchieved = nbt.getInteger("killsAchieved");
      this.tokenReward = nbt.getInteger("tokenReward");
      this.repReward = nbt.getInteger("repReward");
      this.preparingEndTime = nbt.getLong("preparingEndTime");
      this.participants.clear();
      NBTTagList partList = nbt.getTagList("participants", 10);

      for(int i = 0; i < partList.tagCount(); ++i) {
         NBTTagCompound tag = partList.getCompoundTagAt(i);

         try {
            UUID uuid = UUID.fromString(tag.getString("uuid"));
            boolean arrived = tag.getBoolean("arrived");
            this.participants.put(uuid, arrived);
         } catch (IllegalArgumentException var8) {
         }
      }

   }

   public static enum RaidStatus {
      PREPARING,
      ACTIVE,
      COMPLETED,
      FAILED,
      CANCELLED;
   }
}
