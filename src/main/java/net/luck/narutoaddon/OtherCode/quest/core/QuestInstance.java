
package net.luck.narutoaddon.OtherCode.quest.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class QuestInstance {
   private final String questId;
   private int currentStepIndex;
   private QuestState state;
   private long startTime;
   private final Set<UUID> spawnedEntityUUIDs;
   private int combatTimer;
   private int killProgress;
   private boolean dropObtained;
   private int retreatPhase;
   private UUID jiraiyaRescueUUID;
   private int retreatTimer;
   private int dialogPhaseIndex = 0;
   private int quizCorrectCount = 0;
   private boolean isGenerated;
   private BlockPos generatedPosition;
   private String generatedTargetEntityId;
   private int generatedKillCount;
   private String generatedName;
   private String generatedDescription;
   private byte generatedRankOrdinal;
   private int generatedXpReward;
   private List<GeneratedStepData> generatedSteps;
   private final Map<String, UUID> sceneEntityUUIDs = new HashMap();

   public QuestInstance(String questId) {
      this.questId = questId;
      this.currentStepIndex = 0;
      this.state = QuestState.ACTIVE;
      this.startTime = System.currentTimeMillis();
      this.spawnedEntityUUIDs = new HashSet();
      this.combatTimer = 0;
      this.killProgress = 0;
      this.dropObtained = false;
      this.retreatPhase = 0;
      this.jiraiyaRescueUUID = null;
      this.retreatTimer = 0;
      this.isGenerated = false;
   }

   private QuestInstance(String questId, int stepIndex, QuestState state, long startTime, Set<UUID> entities, int combatTimer, int killProgress, boolean dropObtained, int retreatPhase, UUID jiraiyaRescueUUID, int retreatTimer, boolean isGenerated, BlockPos generatedPosition, String generatedTargetEntityId, int generatedKillCount, String generatedName, String generatedDescription, byte generatedRankOrdinal, int generatedXpReward) {
      this.questId = questId;
      this.currentStepIndex = stepIndex;
      this.state = state;
      this.startTime = startTime;
      this.spawnedEntityUUIDs = entities;
      this.combatTimer = combatTimer;
      this.killProgress = killProgress;
      this.dropObtained = dropObtained;
      this.retreatPhase = retreatPhase;
      this.jiraiyaRescueUUID = jiraiyaRescueUUID;
      this.retreatTimer = retreatTimer;
      this.isGenerated = isGenerated;
      this.generatedPosition = generatedPosition;
      this.generatedTargetEntityId = generatedTargetEntityId;
      this.generatedKillCount = generatedKillCount;
      this.generatedName = generatedName;
      this.generatedDescription = generatedDescription;
      this.generatedRankOrdinal = generatedRankOrdinal;
      this.generatedXpReward = generatedXpReward;
   }

   public static QuestInstance fromOffer(RepeatableQuestGenerator.QuestOffer offer) {
      QuestInstance inst = new QuestInstance(offer.questId);
      inst.isGenerated = true;
      inst.generatedPosition = offer.position;
      inst.generatedTargetEntityId = offer.targetEntityId;
      inst.generatedKillCount = offer.killCount;
      inst.generatedName = offer.name;
      inst.generatedDescription = offer.description;
      inst.generatedRankOrdinal = (byte)offer.rank.ordinal();
      inst.generatedXpReward = offer.xpReward;
      if (offer.steps != null && !offer.steps.isEmpty()) {
         inst.generatedSteps = new ArrayList(offer.steps);
      }

      return inst;
   }

   public String getQuestId() {
      return this.questId;
   }

   public int getCurrentStepIndex() {
      return this.currentStepIndex;
   }

   public QuestState getState() {
      return this.state;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public int getCombatTimer() {
      return this.combatTimer;
   }

   public void setCombatTimer(int ticks) {
      this.combatTimer = ticks;
   }

   public void decrementCombatTimer() {
      if (this.combatTimer > 0) {
         --this.combatTimer;
      }

   }

   public int getKillProgress() {
      return this.killProgress;
   }

   public int incrementKills() {
      return ++this.killProgress;
   }

   public void resetKillProgress() {
      this.killProgress = 0;
   }

   public boolean isDropObtained() {
      return this.dropObtained;
   }

   public void setDropObtained() {
      this.dropObtained = true;
   }

   public int getRetreatPhase() {
      return this.retreatPhase;
   }

   public void setRetreatPhase(int phase) {
      this.retreatPhase = phase;
   }

   public UUID getJiraiyaRescueUUID() {
      return this.jiraiyaRescueUUID;
   }

   public void setJiraiyaRescueUUID(UUID uuid) {
      this.jiraiyaRescueUUID = uuid;
   }

   public int getRetreatTimer() {
      return this.retreatTimer;
   }

   public void setRetreatTimer(int ticks) {
      this.retreatTimer = ticks;
   }

   public void incrementRetreatTimer() {
      ++this.retreatTimer;
   }

   public int getDialogPhaseIndex() {
      return this.dialogPhaseIndex;
   }

   public void setDialogPhaseIndex(int idx) {
      this.dialogPhaseIndex = idx;
   }

   public int getQuizCorrectCount() {
      return this.quizCorrectCount;
   }

   public void incrementQuizCorrectCount() {
      ++this.quizCorrectCount;
   }

   public void resetQuizState() {
      this.dialogPhaseIndex = 0;
      this.quizCorrectCount = 0;
   }

   public boolean isGenerated() {
      return this.isGenerated;
   }

   public BlockPos getGeneratedPosition() {
      return this.generatedPosition;
   }

   public String getGeneratedTargetEntityId() {
      return this.generatedTargetEntityId;
   }

   public int getGeneratedKillCount() {
      return this.generatedKillCount;
   }

   public String getGeneratedName() {
      return this.generatedName;
   }

   public String getGeneratedDescription() {
      return this.generatedDescription;
   }

   public byte getGeneratedRankOrdinal() {
      return this.generatedRankOrdinal;
   }

   public int getGeneratedXpReward() {
      return this.generatedXpReward;
   }

   public QuestStep getCurrentStep() {
      if (this.isGenerated) {
         return this.generatedSteps != null && this.currentStepIndex < this.generatedSteps.size() ? ((GeneratedStepData)this.generatedSteps.get(this.currentStepIndex)).toQuestStep() : QuestStep.combatKillCount(this.generatedDescription, this.generatedPosition != null ? this.generatedPosition : BlockPos.ORIGIN, this.generatedTargetEntityId, this.generatedKillCount);
      } else {
         QuestDefinition def = QuestRegistry.getById(this.questId);
         return def == null ? null : def.getStep(this.currentStepIndex);
      }
   }

   public BlockPos getCurrentStepPosition() {
      if (this.isGenerated && this.generatedSteps != null && this.currentStepIndex < this.generatedSteps.size()) {
         BlockPos stepPos = ((GeneratedStepData)this.generatedSteps.get(this.currentStepIndex)).position;
         return stepPos != null ? stepPos : (this.generatedPosition != null ? this.generatedPosition : BlockPos.ORIGIN);
      } else {
         return this.generatedPosition != null ? this.generatedPosition : BlockPos.ORIGIN;
      }
   }

   public boolean isOnFinalStep() {
      if (this.isGenerated) {
         if (this.generatedSteps != null) {
            return this.currentStepIndex >= this.generatedSteps.size() - 1;
         } else {
            return true;
         }
      } else {
         QuestDefinition def = QuestRegistry.getById(this.questId);
         if (def == null) {
            return true;
         } else {
            return this.currentStepIndex >= def.getStepCount() - 1;
         }
      }
   }

   public int getGeneratedStepCount() {
      return this.generatedSteps != null ? this.generatedSteps.size() : 1;
   }

   public List<GeneratedStepData> getGeneratedSteps() {
      return this.generatedSteps;
   }

   public void advanceStep() {
      ++this.currentStepIndex;
      this.spawnedEntityUUIDs.clear();
      this.combatTimer = 0;
      this.killProgress = 0;
      this.dropObtained = false;
      this.retreatPhase = 0;
      this.jiraiyaRescueUUID = null;
      this.retreatTimer = 0;
   }

   public void revertToStep(int stepIndex) {
      if (stepIndex < 0) {
         stepIndex = 0;
      }

      this.currentStepIndex = stepIndex;
      this.spawnedEntityUUIDs.clear();
      this.combatTimer = 0;
      this.killProgress = 0;
      this.dropObtained = false;
      this.retreatPhase = 0;
      this.jiraiyaRescueUUID = null;
      this.retreatTimer = 0;
      this.dialogPhaseIndex = 0;
      this.quizCorrectCount = 0;
   }

   public void complete() {
      this.state = QuestState.COMPLETED;
   }

   public void fail() {
      this.state = QuestState.FAILED;
   }

   public void trackEntity(UUID entityUUID) {
      this.spawnedEntityUUIDs.add(entityUUID);
   }

   public Set<UUID> getSpawnedEntities() {
      return this.spawnedEntityUUIDs;
   }

   public void removeSpawnedEntity(UUID entityUUID) {
      this.spawnedEntityUUIDs.remove(entityUUID);
   }

   public void trackSceneEntity(String npcConfigId, UUID entityUUID) {
      this.sceneEntityUUIDs.put(npcConfigId, entityUUID);
   }

   public UUID getSceneEntity(String npcConfigId) {
      return (UUID)this.sceneEntityUUIDs.get(npcConfigId);
   }

   public Map<String, UUID> getSceneEntities() {
      return this.sceneEntityUUIDs;
   }

   public void clearSceneEntities() {
      this.sceneEntityUUIDs.clear();
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      nbt.setString("questId", this.questId);
      nbt.setInteger("stepIndex", this.currentStepIndex);
      nbt.setInteger("state", this.state.ordinal());
      nbt.setLong("startTime", this.startTime);
      nbt.setInteger("combatTimer", this.combatTimer);
      nbt.setInteger("killProgress", this.killProgress);
      nbt.setBoolean("dropObtained", this.dropObtained);
      nbt.setInteger("retreatPhase", this.retreatPhase);
      nbt.setInteger("retreatTimer", this.retreatTimer);
      if (this.jiraiyaRescueUUID != null) {
         nbt.setString("jiraiyaRescueUUID", this.jiraiyaRescueUUID.toString());
      }

      nbt.setInteger("dialogPhaseIndex", this.dialogPhaseIndex);
      nbt.setInteger("quizCorrectCount", this.quizCorrectCount);
      NBTTagList entityList = new NBTTagList();

      for(UUID uuid : this.spawnedEntityUUIDs) {
         entityList.appendTag(new NBTTagString(uuid.toString()));
      }

      nbt.setTag("spawnedEntities", entityList);
      if (!this.sceneEntityUUIDs.isEmpty()) {
         NBTTagCompound sceneNbt = new NBTTagCompound();

         for(Map.Entry<String, UUID> entry : this.sceneEntityUUIDs.entrySet()) {
            sceneNbt.setString((String)entry.getKey(), ((UUID)entry.getValue()).toString());
         }

         nbt.setTag("sceneEntities", sceneNbt);
      }

      if (this.isGenerated) {
         nbt.setBoolean("isGenerated", true);
         if (this.generatedPosition != null) {
            nbt.setInteger("genPosX", this.generatedPosition.getX());
            nbt.setInteger("genPosY", this.generatedPosition.getY());
            nbt.setInteger("genPosZ", this.generatedPosition.getZ());
         }

         if (this.generatedTargetEntityId != null) {
            nbt.setString("genTargetEntity", this.generatedTargetEntityId);
         }

         nbt.setInteger("genKillCount", this.generatedKillCount);
         if (this.generatedName != null) {
            nbt.setString("genName", this.generatedName);
         }

         if (this.generatedDescription != null) {
            nbt.setString("genDesc", this.generatedDescription);
         }

         nbt.setByte("genRank", this.generatedRankOrdinal);
         nbt.setInteger("genXpReward", this.generatedXpReward);
         if (this.generatedSteps != null && !this.generatedSteps.isEmpty()) {
            NBTTagList stepsList = new NBTTagList();

            for(GeneratedStepData stepData : this.generatedSteps) {
               stepsList.appendTag(stepData.writeToNBT());
            }

            nbt.setTag("generatedSteps", stepsList);
         }
      }

      return nbt;
   }

   public static QuestInstance readFromNBT(NBTTagCompound nbt) {
      String questId = nbt.getString("questId");
      int stepIndex = nbt.getInteger("stepIndex");
      int stateOrd = nbt.getInteger("state");
      QuestState state = stateOrd < QuestState.values().length ? QuestState.values()[stateOrd] : QuestState.ACTIVE;
      long startTime = nbt.getLong("startTime");
      int combatTimer = nbt.getInteger("combatTimer");
      int killProgress = nbt.getInteger("killProgress");
      boolean dropObtained = nbt.getBoolean("dropObtained");
      int retreatPhase = nbt.getInteger("retreatPhase");
      int retreatTimer = nbt.getInteger("retreatTimer");
      UUID jiraiyaRescueUUID = null;
      if (nbt.hasKey("jiraiyaRescueUUID")) {
         try {
            jiraiyaRescueUUID = UUID.fromString(nbt.getString("jiraiyaRescueUUID"));
         } catch (IllegalArgumentException var31) {
         }
      }

      int dialogPhaseIndex = nbt.getInteger("dialogPhaseIndex");
      int quizCorrectCount = nbt.getInteger("quizCorrectCount");
      Set<UUID> entities = new HashSet();
      if (nbt.hasKey("spawnedEntities", 9)) {
         NBTTagList entityList = nbt.getTagList("spawnedEntities", 8);

         for(int i = 0; i < entityList.tagCount(); ++i) {
            try {
               entities.add(UUID.fromString(entityList.getStringTagAt(i)));
            } catch (IllegalArgumentException var30) {
            }
         }
      }

      boolean isGenerated = nbt.getBoolean("isGenerated");
      BlockPos genPos = null;
      String genTargetEntity = null;
      int genKillCount = 0;
      String genName = null;
      String genDesc = null;
      byte genRank = 0;
      int genXpReward = 0;
      if (isGenerated) {
         if (nbt.hasKey("genPosX")) {
            genPos = new BlockPos(nbt.getInteger("genPosX"), nbt.getInteger("genPosY"), nbt.getInteger("genPosZ"));
         }

         genTargetEntity = nbt.hasKey("genTargetEntity") ? nbt.getString("genTargetEntity") : null;
         genKillCount = nbt.getInteger("genKillCount");
         genName = nbt.hasKey("genName") ? nbt.getString("genName") : null;
         genDesc = nbt.hasKey("genDesc") ? nbt.getString("genDesc") : null;
         genRank = nbt.getByte("genRank");
         genXpReward = nbt.getInteger("genXpReward");
      }

      QuestInstance inst = new QuestInstance(questId, stepIndex, state, startTime, entities, combatTimer, killProgress, dropObtained, retreatPhase, jiraiyaRescueUUID, retreatTimer, isGenerated, genPos, genTargetEntity, genKillCount, genName, genDesc, genRank, genXpReward);
      inst.dialogPhaseIndex = dialogPhaseIndex;
      inst.quizCorrectCount = quizCorrectCount;
      if (nbt.hasKey("sceneEntities", 10)) {
         NBTTagCompound sceneNbt = nbt.getCompoundTag("sceneEntities");

         for(String key : sceneNbt.getKeySet()) {
            try {
               inst.sceneEntityUUIDs.put(key, UUID.fromString(sceneNbt.getString(key)));
            } catch (IllegalArgumentException var29) {
            }
         }
      }

      if (isGenerated && nbt.hasKey("generatedSteps")) {
         NBTTagList stepsList = nbt.getTagList("generatedSteps", 10);
         List<GeneratedStepData> steps = new ArrayList();

         for(int i = 0; i < stepsList.tagCount(); ++i) {
            steps.add(GeneratedStepData.readFromNBT(stepsList.getCompoundTagAt(i)));
         }

         inst.generatedSteps = steps;
      }

      return inst;
   }

   public static enum QuestState {
      ACTIVE,
      COMPLETED,
      FAILED;
   }
}
