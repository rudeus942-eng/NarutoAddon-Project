package net.luck.narutoaddon.OtherCode.akatsuki.mission;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AkatsukiMission {
   private String templateId;
   private String templateName;
   private String description;
   private UUID ownerId;
   private long startTime;
   private boolean completed;
   private BlockPos targetPos;
   private AkatsukiMissionTemplate.Category category;
   private List<StepInstance> steps;
   private int currentStepIndex;
   private int scoutProgress;
   private MissionState missionState;
   private final List<UUID> spawnedEntityUUIDs;
   private int killsAchieved;
   private int killsRequired;
   private long stateChangeTime;
   private int countdownTicks;
   private int ryoReward;
   private int pveXpReward;

   public AkatsukiMission(String templateId, String templateName, String description, UUID ownerId, BlockPos targetPos, AkatsukiMissionTemplate.Category category) {
      this.description = "";
      this.steps = new ArrayList();
      this.currentStepIndex = 0;
      this.scoutProgress = 0;
      this.missionState = MissionState.TRAVELING;
      this.spawnedEntityUUIDs = new ArrayList();
      this.killsAchieved = 0;
      this.killsRequired = 0;
      this.stateChangeTime = 0L;
      this.countdownTicks = 0;
      this.ryoReward = 0;
      this.pveXpReward = 0;
      this.templateId = templateId;
      this.templateName = templateName;
      this.description = description;
      this.ownerId = ownerId;
      this.startTime = System.currentTimeMillis();
      this.completed = false;
      this.targetPos = targetPos;
      this.category = category;
      this.missionState = MissionState.TRAVELING;
      this.stateChangeTime = System.currentTimeMillis();
   }

   public AkatsukiMission(String templateId, String templateName, UUID ownerId, BlockPos targetPos, AkatsukiMissionTemplate.Category category) {
      this(templateId, templateName, "", ownerId, targetPos, category);
   }

   public AkatsukiMission() {
      this.description = "";
      this.steps = new ArrayList();
      this.currentStepIndex = 0;
      this.scoutProgress = 0;
      this.missionState = MissionState.TRAVELING;
      this.spawnedEntityUUIDs = new ArrayList();
      this.killsAchieved = 0;
      this.killsRequired = 0;
      this.stateChangeTime = 0L;
      this.countdownTicks = 0;
      this.ryoReward = 0;
      this.pveXpReward = 0;
      this.templateId = "";
      this.templateName = "";
      this.description = "";
      this.ownerId = UUID.randomUUID();
      this.startTime = 0L;
      this.completed = false;
      this.targetPos = BlockPos.ORIGIN;
      this.category = AkatsukiMissionTemplate.Category.DAILY;
      this.missionState = MissionState.TRAVELING;
   }

   public String getTemplateId() {
      return this.templateId;
   }

   public String getTemplateName() {
      return this.templateName;
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public UUID getOwnerId() {
      return this.ownerId;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public boolean isCompleted() {
      return this.completed;
   }

   public void setCompleted(boolean completed) {
      this.completed = completed;
   }

   public AkatsukiMissionTemplate.Category getCategory() {
      return this.category;
   }

   public BlockPos getTargetPos() {
      StepInstance step = this.getCurrentStep();
      return step != null ? step.getPosition() : this.targetPos;
   }

   public List<StepInstance> getSteps() {
      return this.steps;
   }

   public int getCurrentStepIndex() {
      return this.currentStepIndex;
   }

   public int getTotalSteps() {
      return this.steps.size();
   }

   public void addStep(StepInstance step) {
      this.steps.add(step);
   }

   public StepInstance getCurrentStep() {
      return !this.steps.isEmpty() && this.currentStepIndex >= 0 && this.currentStepIndex < this.steps.size() ? (StepInstance)this.steps.get(this.currentStepIndex) : null;
   }

   public boolean isOnFinalStep() {
      return this.currentStepIndex >= this.steps.size() - 1;
   }

   public boolean advanceStep() {
      StepInstance current = this.getCurrentStep();
      if (current != null) {
         current.setCompleted(true);
      }

      ++this.currentStepIndex;
      if (this.currentStepIndex >= this.steps.size()) {
         return false;
      } else {
         this.killsAchieved = 0;
         this.killsRequired = 0;
         this.spawnedEntityUUIDs.clear();
         this.countdownTicks = 0;
         this.scoutProgress = 0;
         this.missionState = MissionState.TRAVELING;
         this.stateChangeTime = System.currentTimeMillis();
         return true;
      }
   }

   public int getScoutProgress() {
      return this.scoutProgress;
   }

   public void setScoutProgress(int progress) {
      this.scoutProgress = progress;
   }

   public void incrementScoutProgress() {
      ++this.scoutProgress;
   }

   public MissionState getMissionState() {
      return this.missionState;
   }

   public void setMissionState(MissionState state) {
      this.missionState = state;
      this.stateChangeTime = System.currentTimeMillis();
      if (state == MissionState.COUNTDOWN) {
         this.countdownTicks = 0;
      }

   }

   public long getStateChangeTime() {
      return this.stateChangeTime;
   }

   public int getCountdownTicks() {
      return this.countdownTicks;
   }

   public void incrementCountdownTicks() {
      ++this.countdownTicks;
   }

   public String[] getNpcConfigIds() {
      StepInstance step = this.getCurrentStep();
      return step != null && step.getNpcConfigIds() != null ? step.getNpcConfigIds() : new String[0];
   }

   public void setNpcConfigIds(String[] ids) {
      if (this.steps.isEmpty()) {
         StepInstance step = new StepInstance(this.targetPos, AkatsukiMissionTemplate.MissionStep.StepType.COMBAT, "", ids, 0);
         this.steps.add(step);
      } else {
         StepInstance step = this.getCurrentStep();
         if (step != null) {
         }
      }

      this.killsRequired = ids != null ? ids.length : 0;
   }

   public List<UUID> getSpawnedEntityUUIDs() {
      return this.spawnedEntityUUIDs;
   }

   public void addSpawnedEntity(UUID entityUUID) {
      this.spawnedEntityUUIDs.add(entityUUID);
   }

   public int getKillsAchieved() {
      return this.killsAchieved;
   }

   public int getKillsRequired() {
      return this.killsRequired;
   }

   public void setKillsRequired(int killsRequired) {
      this.killsRequired = killsRequired;
   }

   public void incrementKills() {
      ++this.killsAchieved;
   }

   public boolean allEnemiesDefeated() {
      return this.killsRequired > 0 && this.killsAchieved >= this.killsRequired;
   }

   public int getRyoReward() {
      return this.ryoReward;
   }

   public void setRyoReward(int ryo) {
      this.ryoReward = ryo;
   }

   public int getPveXpReward() {
      return this.pveXpReward;
   }

   public void setPveXpReward(int xp) {
      this.pveXpReward = xp;
   }

   public boolean isCombatActive() {
      return this.missionState == MissionState.COUNTDOWN || this.missionState == MissionState.ACTIVE;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("templateId", this.templateId);
      nbt.setString("templateName", this.templateName);
      nbt.setString("description", this.description);
      nbt.setString("ownerId", this.ownerId.toString());
      nbt.setLong("startTime", this.startTime);
      nbt.setBoolean("completed", this.completed);
      nbt.setInteger("targetX", this.targetPos.getX());
      nbt.setInteger("targetY", this.targetPos.getY());
      nbt.setInteger("targetZ", this.targetPos.getZ());
      nbt.setInteger("category", this.category.ordinal());
      nbt.setInteger("missionState", this.missionState.ordinal());
      nbt.setInteger("killsAchieved", this.killsAchieved);
      nbt.setInteger("killsRequired", this.killsRequired);
      nbt.setInteger("ryoReward", this.ryoReward);
      nbt.setInteger("pveXpReward", this.pveXpReward);
      nbt.setInteger("countdownTicks", this.countdownTicks);
      nbt.setInteger("currentStepIndex", this.currentStepIndex);
      nbt.setInteger("scoutProgress", this.scoutProgress);
      NBTTagList stepList = new NBTTagList();

      for(StepInstance step : this.steps) {
         stepList.appendTag(step.writeToNBT());
      }

      nbt.setTag("steps", stepList);
      return nbt;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.templateId = nbt.getString("templateId");
      this.templateName = nbt.getString("templateName");
      this.description = nbt.hasKey("description") ? nbt.getString("description") : "";

      try {
         this.ownerId = UUID.fromString(nbt.getString("ownerId"));
      } catch (IllegalArgumentException var9) {
         this.ownerId = UUID.randomUUID();
      }

      this.startTime = nbt.getLong("startTime");
      this.completed = nbt.getBoolean("completed");
      this.targetPos = new BlockPos(nbt.getInteger("targetX"), nbt.getInteger("targetY"), nbt.getInteger("targetZ"));
      int catOrd = nbt.getInteger("category");
      AkatsukiMissionTemplate.Category[] cats = AkatsukiMissionTemplate.Category.values();
      this.category = catOrd >= 0 && catOrd < cats.length ? cats[catOrd] : AkatsukiMissionTemplate.Category.DAILY;
      int stateOrd = nbt.getInteger("missionState");
      MissionState[] states = MissionState.values();
      this.missionState = stateOrd >= 0 && stateOrd < states.length ? states[stateOrd] : MissionState.TRAVELING;
      this.killsAchieved = nbt.getInteger("killsAchieved");
      this.killsRequired = nbt.getInteger("killsRequired");
      this.ryoReward = nbt.getInteger("ryoReward");
      this.pveXpReward = nbt.getInteger("pveXpReward");
      this.countdownTicks = nbt.getInteger("countdownTicks");
      this.currentStepIndex = nbt.getInteger("currentStepIndex");
      this.scoutProgress = nbt.getInteger("scoutProgress");
      this.steps.clear();
      if (nbt.hasKey("steps")) {
         NBTTagList stepList = nbt.getTagList("steps", 10);

         for(int i = 0; i < stepList.tagCount(); ++i) {
            StepInstance step = new StepInstance();
            step.readFromNBT(stepList.getCompoundTagAt(i));
            this.steps.add(step);
         }
      }

      if (this.steps.isEmpty() && nbt.hasKey("npcCount")) {
         int npcCount = nbt.getInteger("npcCount");
         String[] npcIds = new String[npcCount];

         for(int i = 0; i < npcCount; ++i) {
            npcIds[i] = nbt.getString("npc_" + i);
         }

         if (npcCount > 0) {
            StepInstance legacyStep = new StepInstance(this.targetPos, AkatsukiMissionTemplate.MissionStep.StepType.COMBAT, "", npcIds, 0);
            this.steps.add(legacyStep);
         }
      }

      if ((this.missionState == MissionState.ACTIVE || this.missionState == MissionState.COUNTDOWN) && this.spawnedEntityUUIDs.isEmpty() && this.killsAchieved < this.killsRequired) {
         this.missionState = MissionState.TRAVELING;
         this.killsAchieved = 0;
         this.scoutProgress = 0;
      }

   }

   public static enum MissionState {
      TRAVELING,
      COUNTDOWN,
      ACTIVE,
      VICTORY,
      FAILED;
   }

   public static class StepInstance {
      private BlockPos position;
      private AkatsukiMissionTemplate.MissionStep.StepType type;
      private String objective;
      private String[] npcConfigIds;
      private int scoutDurationTicks;
      private boolean completed;

      public StepInstance(BlockPos position, AkatsukiMissionTemplate.MissionStep.StepType type, String objective, String[] npcConfigIds, int scoutDurationTicks) {
         this.position = position;
         this.type = type;
         this.objective = objective != null ? objective : "";
         this.npcConfigIds = npcConfigIds;
         this.scoutDurationTicks = scoutDurationTicks;
         this.completed = false;
      }

      public StepInstance() {
         this.position = BlockPos.ORIGIN;
         this.type = AkatsukiMissionTemplate.MissionStep.StepType.TRAVEL;
         this.objective = "";
         this.npcConfigIds = null;
         this.scoutDurationTicks = 0;
         this.completed = false;
      }

      public BlockPos getPosition() {
         return this.position;
      }

      public AkatsukiMissionTemplate.MissionStep.StepType getType() {
         return this.type;
      }

      public String getObjective() {
         return this.objective;
      }

      public String[] getNpcConfigIds() {
         return this.npcConfigIds;
      }

      public int getScoutDurationTicks() {
         return this.scoutDurationTicks;
      }

      public boolean isCompleted() {
         return this.completed;
      }

      public void setCompleted(boolean completed) {
         this.completed = completed;
      }

      public NBTTagCompound writeToNBT() {
         NBTTagCompound nbt = new NBTTagCompound();
         nbt.setInteger("posX", this.position.getX());
         nbt.setInteger("posY", this.position.getY());
         nbt.setInteger("posZ", this.position.getZ());
         nbt.setInteger("stepType", this.type.ordinal());
         nbt.setString("objective", this.objective);
         nbt.setInteger("scoutDurationTicks", this.scoutDurationTicks);
         nbt.setBoolean("completed", this.completed);
         if (this.npcConfigIds != null) {
            nbt.setInteger("npcCount", this.npcConfigIds.length);

            for(int i = 0; i < this.npcConfigIds.length; ++i) {
               nbt.setString("npc_" + i, this.npcConfigIds[i]);
            }
         } else {
            nbt.setInteger("npcCount", -1);
         }

         return nbt;
      }

      public void readFromNBT(NBTTagCompound nbt) {
         this.position = new BlockPos(nbt.getInteger("posX"), nbt.getInteger("posY"), nbt.getInteger("posZ"));
         int typeOrd = nbt.getInteger("stepType");
         AkatsukiMissionTemplate.MissionStep.StepType[] types = AkatsukiMissionTemplate.MissionStep.StepType.values();
         this.type = typeOrd >= 0 && typeOrd < types.length ? types[typeOrd] : AkatsukiMissionTemplate.MissionStep.StepType.TRAVEL;
         this.objective = nbt.getString("objective");
         this.scoutDurationTicks = nbt.getInteger("scoutDurationTicks");
         this.completed = nbt.getBoolean("completed");
         int npcCount = nbt.getInteger("npcCount");
         if (npcCount >= 0) {
            this.npcConfigIds = new String[npcCount];

            for(int i = 0; i < npcCount; ++i) {
               this.npcConfigIds[i] = nbt.getString("npc_" + i);
            }
         } else {
            this.npcConfigIds = null;
         }

      }
   }
}
