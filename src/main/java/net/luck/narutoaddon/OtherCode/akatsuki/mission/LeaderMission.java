package net.luck.narutoaddon.OtherCode.akatsuki.mission;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class LeaderMission {
   private String type;
   private String description;
   private String objectiveText;
   private int tokenReward;
   private int repReward;
   private UUID assignedTo;
   private long createdTime;
   private boolean completed;
   private BlockPos targetPos;
   private String[] npcConfigIds;
   private int missionState;
   private int countdownTicks;
   private int killsAchieved;
   private int killsRequired;
   private List<UUID> spawnedEntityUUIDs;
   private static final Map<String, String> TYPE_OBJECTIVES = new HashMap();
   private static final Map<String, String> TYPE_DESCRIPTIONS = new HashMap();
   private static final Map<String, String[]> TYPE_NPC_CONFIGS = new HashMap();

   public LeaderMission() {
      this.missionState = 0;
      this.countdownTicks = 0;
      this.killsAchieved = 0;
      this.killsRequired = 0;
      this.spawnedEntityUUIDs = new ArrayList();
      this.targetPos = BlockPos.ORIGIN;
      this.objectiveText = "";
   }

   public LeaderMission(String type, String description, int tokenReward, int repReward, UUID assignedTo, BlockPos targetPos) {
      this.missionState = 0;
      this.countdownTicks = 0;
      this.killsAchieved = 0;
      this.killsRequired = 0;
      this.spawnedEntityUUIDs = new ArrayList();
      this.type = type;
      this.objectiveText = (String)TYPE_OBJECTIVES.getOrDefault(type, "Carry out Pain's orders.");
      if (description != null && description.contains("assigned by")) {
         this.description = (String)TYPE_DESCRIPTIONS.getOrDefault(type, description);
      } else {
         this.description = description;
      }

      this.tokenReward = tokenReward;
      this.repReward = repReward;
      this.assignedTo = assignedTo;
      this.createdTime = System.currentTimeMillis();
      this.completed = false;
      this.targetPos = targetPos != null ? targetPos : BlockPos.ORIGIN;
      this.npcConfigIds = (String[])TYPE_NPC_CONFIGS.get(type);
      if (this.npcConfigIds != null) {
         this.killsRequired = this.npcConfigIds.length;
      }

   }

   public LeaderMission(String type, String description, int tokenReward, int repReward, UUID assignedTo) {
      this(type, description, tokenReward, repReward, assignedTo, BlockPos.ORIGIN);
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getObjectiveText() {
      return this.objectiveText != null ? this.objectiveText : "";
   }

   public void setObjectiveText(String objectiveText) {
      this.objectiveText = objectiveText;
   }

   public int getTokenReward() {
      return this.tokenReward;
   }

   public void setTokenReward(int tokenReward) {
      this.tokenReward = tokenReward;
   }

   public int getRepReward() {
      return this.repReward;
   }

   public void setRepReward(int repReward) {
      this.repReward = repReward;
   }

   public UUID getAssignedTo() {
      return this.assignedTo;
   }

   public void setAssignedTo(UUID assignedTo) {
      this.assignedTo = assignedTo;
   }

   public long getCreatedTime() {
      return this.createdTime;
   }

   public void setCreatedTime(long createdTime) {
      this.createdTime = createdTime;
   }

   public boolean isCompleted() {
      return this.completed;
   }

   public void setCompleted(boolean completed) {
      this.completed = completed;
   }

   public BlockPos getTargetPos() {
      return this.targetPos;
   }

   public void setTargetPos(BlockPos targetPos) {
      this.targetPos = targetPos;
   }

   public String[] getNpcConfigIds() {
      return this.npcConfigIds;
   }

   public void setNpcConfigIds(String[] npcConfigIds) {
      this.npcConfigIds = npcConfigIds;
   }

   public int getMissionState() {
      return this.missionState;
   }

   public void setMissionState(int missionState) {
      this.missionState = missionState;
   }

   public int getCountdownTicks() {
      return this.countdownTicks;
   }

   public void setCountdownTicks(int countdownTicks) {
      this.countdownTicks = countdownTicks;
   }

   public void incrementCountdownTicks() {
      ++this.countdownTicks;
   }

   public int getKillsAchieved() {
      return this.killsAchieved;
   }

   public void setKillsAchieved(int killsAchieved) {
      this.killsAchieved = killsAchieved;
   }

   public void incrementKills() {
      ++this.killsAchieved;
   }

   public int getKillsRequired() {
      return this.killsRequired;
   }

   public void setKillsRequired(int killsRequired) {
      this.killsRequired = killsRequired;
   }

   public boolean allEnemiesDefeated() {
      return this.killsRequired > 0 && this.killsAchieved >= this.killsRequired;
   }

   public List<UUID> getSpawnedEntityUUIDs() {
      return this.spawnedEntityUUIDs;
   }

   public void addSpawnedEntity(UUID entityUUID) {
      this.spawnedEntityUUIDs.add(entityUUID);
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.type = nbt.getString("type");
      this.description = nbt.getString("description");
      this.objectiveText = nbt.getString("objectiveText");
      if (this.objectiveText.isEmpty()) {
         this.objectiveText = (String)TYPE_OBJECTIVES.getOrDefault(this.type, "");
      }

      this.tokenReward = nbt.getInteger("tokenReward");
      this.repReward = nbt.getInteger("repReward");

      try {
         this.assignedTo = UUID.fromString(nbt.getString("assignedTo"));
      } catch (IllegalArgumentException var3) {
         this.assignedTo = null;
      }

      this.createdTime = nbt.getLong("createdTime");
      this.completed = nbt.getBoolean("completed");
      this.targetPos = new BlockPos(nbt.getInteger("targetX"), nbt.getInteger("targetY"), nbt.getInteger("targetZ"));
      this.missionState = nbt.getInteger("missionState");
      this.countdownTicks = nbt.getInteger("countdownTicks");
      this.killsAchieved = nbt.getInteger("killsAchieved");
      this.killsRequired = nbt.getInteger("killsRequired");
      if (nbt.hasKey("npcConfigIds")) {
         String joined = nbt.getString("npcConfigIds");
         if (!joined.isEmpty()) {
            this.npcConfigIds = joined.split(",");
         }
      }

      if (this.npcConfigIds == null) {
         this.npcConfigIds = (String[])TYPE_NPC_CONFIGS.get(this.type);
         if (this.npcConfigIds != null && this.killsRequired == 0) {
            this.killsRequired = this.npcConfigIds.length;
         }
      }

      if ((this.missionState == 1 || this.missionState == 2) && this.spawnedEntityUUIDs.isEmpty() && this.killsAchieved < this.killsRequired) {
         this.missionState = 0;
         this.killsAchieved = 0;
         this.countdownTicks = 0;
      }

   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("type", this.type != null ? this.type : "");
      nbt.setString("description", this.description != null ? this.description : "");
      nbt.setString("objectiveText", this.objectiveText != null ? this.objectiveText : "");
      nbt.setInteger("tokenReward", this.tokenReward);
      nbt.setInteger("repReward", this.repReward);
      nbt.setString("assignedTo", this.assignedTo != null ? this.assignedTo.toString() : "");
      nbt.setLong("createdTime", this.createdTime);
      nbt.setBoolean("completed", this.completed);
      nbt.setInteger("targetX", this.targetPos != null ? this.targetPos.getX() : 0);
      nbt.setInteger("targetY", this.targetPos != null ? this.targetPos.getY() : 0);
      nbt.setInteger("targetZ", this.targetPos != null ? this.targetPos.getZ() : 0);
      nbt.setInteger("missionState", this.missionState);
      nbt.setInteger("countdownTicks", this.countdownTicks);
      nbt.setInteger("killsAchieved", this.killsAchieved);
      nbt.setInteger("killsRequired", this.killsRequired);
      if (this.npcConfigIds != null && this.npcConfigIds.length > 0) {
         nbt.setString("npcConfigIds", String.join(",", this.npcConfigIds));
      }

      return nbt;
   }

   static {
      TYPE_OBJECTIVES.put("assassination", "Travel to the designated area and eliminate the target.");
      TYPE_OBJECTIVES.put("infiltration", "Reach the target location and hold position undetected.");
      TYPE_OBJECTIVES.put("extraction", "Travel to the extraction point and secure the objective.");
      TYPE_OBJECTIVES.put("sabotage", "Reach the target site and carry out the sabotage operation.");
      TYPE_OBJECTIVES.put("escort", "Travel to the rendezvous point and ensure safe passage.");
      TYPE_DESCRIPTIONS.put("assassination", "A high-priority target has been identified. Eliminate them without hesitation.");
      TYPE_DESCRIPTIONS.put("infiltration", "Intel suggests activity at this location. Investigate and remain undetected.");
      TYPE_DESCRIPTIONS.put("extraction", "A valuable asset must be retrieved from hostile territory. Move quickly.");
      TYPE_DESCRIPTIONS.put("sabotage", "Pain has ordered the destruction of enemy infrastructure at this location.");
      TYPE_DESCRIPTIONS.put("escort", "An Akatsuki contact requires safe passage through dangerous territory.");
      TYPE_NPC_CONFIGS.put("assassination", new String[]{"ak_bounty_elite", "ak_leaf_anbu"});
      TYPE_NPC_CONFIGS.put("infiltration", new String[]{"ak_patrol_captain", "ak_leaf_anbu", "ak_cloud_sentinel"});
      TYPE_NPC_CONFIGS.put("extraction", new String[]{"ak_mist_hunter", "ak_bounty_hunter"});
      TYPE_NPC_CONFIGS.put("sabotage", new String[]{"ak_stone_guard", "ak_patrol_captain", "ak_bounty_elite"});
      TYPE_NPC_CONFIGS.put("escort", new String[]{"ak_sand_guard", "ak_rain_agent", "ak_patrol_captain"});
   }
}
