package net.luck.narutoaddon.OtherCode.quest.core;

import net.minecraft.util.math.BlockPos;

public class QuestStep {
   public final StepType type;
   public final String description;
   public final BlockPos defaultPosition;
   public final boolean spawnEnemies;
   public final String[] spawnEntityIds;
   public final int[] spawnCounts;
   public final String targetEntityId;
   public final int killsRequired;
   public final double dropChance;
   public final String dropItemName;
   public final boolean isScriptedRetreat;
   public final double retreatHealthPercent;
   public final String[] npcConfigIds;
   public final String npcName;
   public final String[] dialogLines;
   public final String[] dialogResponses;
   public final String[] moreInfoLines;
   public final String npcConfigId;
   public final int correctResponseIndex;
   public final int stayDurationTicks;
   public final double stayRadius;
   public String sceneGroup;
   public String onCompleteCommand;
   public final DialogPhase[] dialogPhases;

   public QuestStep withScene(String group) {
      this.sceneGroup = group;
      return this;
   }

   public QuestStep withOnComplete(String command) {
      this.onCompleteCommand = command;
      return this;
   }

   private QuestStep(StepType type, String description, BlockPos defaultPosition, boolean spawnEnemies, String[] spawnEntityIds, int[] spawnCounts, String targetEntityId, int killsRequired, double dropChance, String dropItemName, boolean isScriptedRetreat, String npcName, String[] dialogLines, String[] dialogResponses, String[] moreInfoLines, String npcConfigId, double retreatHealthPercent, String[] npcConfigIds, int correctResponseIndex, int stayDurationTicks, double stayRadius, DialogPhase[] dialogPhases) {
      this.type = type;
      this.description = description;
      this.defaultPosition = defaultPosition;
      this.spawnEnemies = spawnEnemies;
      this.spawnEntityIds = spawnEntityIds;
      this.spawnCounts = spawnCounts;
      this.targetEntityId = targetEntityId;
      this.killsRequired = killsRequired;
      this.dropChance = dropChance;
      this.dropItemName = dropItemName;
      this.isScriptedRetreat = isScriptedRetreat;
      this.npcName = npcName;
      this.dialogLines = dialogLines;
      this.dialogResponses = dialogResponses;
      this.moreInfoLines = moreInfoLines;
      this.npcConfigId = npcConfigId;
      this.retreatHealthPercent = retreatHealthPercent;
      this.npcConfigIds = npcConfigIds;
      this.correctResponseIndex = correctResponseIndex;
      this.stayDurationTicks = stayDurationTicks;
      this.stayRadius = stayRadius;
      this.dialogPhases = dialogPhases;
   }

   public static QuestStep travel(String description, BlockPos pos) {
      return new QuestStep(StepType.TRAVEL, description, pos, false, (String[])null, (int[])null, (String)null, 0, (double)0.0F, (String)null, false, (String)null, (String[])null, (String[])null, (String[])null, (String)null, (double)0.0F, (String[])null, 0, 0, (double)0.0F, (DialogPhase[])null);
   }

   public static QuestStep combat(String description, BlockPos pos, String[] entityIds, int[] counts) {
      return new QuestStep(StepType.COMBAT, description, pos, true, entityIds, counts, (String)null, 0, (double)0.0F, (String)null, false, (String)null, (String[])null, (String[])null, (String[])null, (String)null, (double)0.0F, (String[])null, 0, 0, (double)0.0F, (DialogPhase[])null);
   }

   public static QuestStep combatKillCount(String description, BlockPos pos, String targetEntityId, int count) {
      return new QuestStep(StepType.COMBAT, description, pos, false, (String[])null, (int[])null, targetEntityId, count, (double)0.0F, (String)null, false, (String)null, (String[])null, (String[])null, (String[])null, (String)null, (double)0.0F, (String[])null, 0, 0, (double)0.0F, (DialogPhase[])null);
   }

   public static QuestStep combatDropChance(String description, BlockPos pos, String targetEntityId, double chance, String dropName) {
      return new QuestStep(StepType.COMBAT, description, pos, false, (String[])null, (int[])null, targetEntityId, 1, chance, dropName, false, (String)null, (String[])null, (String[])null, (String[])null, (String)null, (double)0.0F, (String[])null, 0, 0, (double)0.0F, (DialogPhase[])null);
   }

   public static QuestStep combatScriptedRetreat(String description, BlockPos pos, String[] entityIds, int[] counts) {
      return new QuestStep(StepType.COMBAT, description, pos, true, entityIds, counts, (String)null, 0, (double)0.0F, (String)null, true, (String)null, (String[])null, (String[])null, (String[])null, (String)null, (double)0.0F, (String[])null, 0, 0, (double)0.0F, (DialogPhase[])null);
   }

   public static QuestStep combatWithNpcConfig(String description, BlockPos pos, String[] entityIds, int[] counts, String[] npcConfigIds) {
      return new QuestStep(StepType.COMBAT, description, pos, true, entityIds, counts, (String)null, 0, (double)0.0F, (String)null, false, (String)null, (String[])null, (String[])null, (String[])null, (String)null, (double)0.0F, npcConfigIds, 0, 0, (double)0.0F, (DialogPhase[])null);
   }

   public static QuestStep dialog(String description, BlockPos pos, String npcName, String[] lines, String[] responses) {
      return new QuestStep(StepType.DIALOG, description, pos, false, (String[])null, (int[])null, (String)null, 0, (double)0.0F, (String)null, false, npcName, lines, responses, (String[])null, (String)null, (double)0.0F, (String[])null, 0, 0, (double)0.0F, (DialogPhase[])null);
   }

   public static QuestStep dialog(String description, BlockPos pos, String npcName, String[] lines, String[] responses, String[] moreInfoLines) {
      return new QuestStep(StepType.DIALOG, description, pos, false, (String[])null, (int[])null, (String)null, 0, (double)0.0F, (String)null, false, npcName, lines, responses, moreInfoLines, (String)null, (double)0.0F, (String[])null, 0, 0, (double)0.0F, (DialogPhase[])null);
   }

   public static QuestStep dialogWithNpc(String description, BlockPos pos, String npcName, String[] lines, String[] responses, String npcConfigId) {
      return new QuestStep(StepType.DIALOG, description, pos, false, (String[])null, (int[])null, (String)null, 0, (double)0.0F, (String)null, false, npcName, lines, responses, (String[])null, npcConfigId, (double)0.0F, (String[])null, 0, 0, (double)0.0F, (DialogPhase[])null);
   }

   public static QuestStep dialogWithNpc(String description, BlockPos pos, String npcName, String[] lines, String[] responses, String[] moreInfoLines, String npcConfigId) {
      return new QuestStep(StepType.DIALOG, description, pos, false, (String[])null, (int[])null, (String)null, 0, (double)0.0F, (String)null, false, npcName, lines, responses, moreInfoLines, npcConfigId, (double)0.0F, (String[])null, 0, 0, (double)0.0F, (DialogPhase[])null);
   }

   public static QuestStep dialogQuiz(String description, BlockPos pos, String npcName, String[] lines, String[] responses, int correctResponseIndex) {
      return new QuestStep(StepType.DIALOG, description, pos, false, (String[])null, (int[])null, (String)null, 0, (double)0.0F, (String)null, false, npcName, lines, responses, (String[])null, (String)null, (double)0.0F, (String[])null, correctResponseIndex, 0, (double)0.0F, (DialogPhase[])null);
   }

   public static QuestStep interact(String description, BlockPos pos) {
      return new QuestStep(StepType.INTERACT, description, pos, false, (String[])null, (int[])null, (String)null, 0, (double)0.0F, (String)null, false, (String)null, (String[])null, (String[])null, (String[])null, (String)null, (double)0.0F, (String[])null, 0, 0, (double)0.0F, (DialogPhase[])null);
   }

   public static QuestStep areaStay(String description, BlockPos pos, int durationTicks, double radius) {
      return new QuestStep(StepType.AREA_STAY, description, pos, false, (String[])null, (int[])null, (String)null, 0, (double)0.0F, (String)null, false, (String)null, (String[])null, (String[])null, (String[])null, (String)null, (double)0.0F, (String[])null, 0, durationTicks, radius, (DialogPhase[])null);
   }

   public static QuestStep dialogMultiPhase(String description, BlockPos pos, String npcName, String npcConfigId, DialogPhase[] phases) {
      return new QuestStep(StepType.DIALOG, description, pos, false, (String[])null, (int[])null, (String)null, 0, (double)0.0F, (String)null, false, npcName, (String[])null, (String[])null, (String[])null, npcConfigId, (double)0.0F, (String[])null, 0, 0, (double)0.0F, phases);
   }

   public boolean hasMultiPhaseDialog() {
      return this.dialogPhases != null && this.dialogPhases.length > 0;
   }

   public boolean hasNpcConfig() {
      return this.npcConfigId != null && !this.npcConfigId.isEmpty();
   }

   public boolean isKillAllSpawned() {
      return this.type == StepType.COMBAT && this.spawnEnemies && this.killsRequired == 0 && !this.isScriptedRetreat && this.retreatHealthPercent <= (double)0.0F;
   }

   public boolean isKillCount() {
      return this.type == StepType.COMBAT && this.killsRequired > 0 && this.dropChance <= (double)0.0F;
   }

   public boolean isDropChance() {
      return this.type == StepType.COMBAT && this.dropChance > (double)0.0F;
   }

   public boolean isHealthRetreat() {
      return this.type == StepType.COMBAT && this.retreatHealthPercent > (double)0.0F;
   }

   public boolean isAreaStay() {
      return this.type == StepType.AREA_STAY;
   }

   public static enum StepType {
      TRAVEL,
      COMBAT,
      DIALOG,
      INTERACT,
      AREA_STAY;
   }

   public static class DialogPhase {
      public final String[] lines;
      public final String[] responses;
      public final String[] moreInfoLines;
      public final int correctResponseIndex;
      public final boolean isQuiz;
      public final String[][] resultVariants;
      public final int quizQuestionCount;

      private DialogPhase(String[] lines, String[] responses, String[] moreInfoLines, int correctResponseIndex, boolean isQuiz, String[][] resultVariants, int quizQuestionCount) {
         this.lines = lines;
         this.responses = responses;
         this.moreInfoLines = moreInfoLines;
         this.correctResponseIndex = correctResponseIndex;
         this.isQuiz = isQuiz;
         this.resultVariants = resultVariants;
         this.quizQuestionCount = quizQuestionCount;
      }

      public static DialogPhase dialog(String[] lines, String[] responses, int correctResponseIndex, String[] moreInfoLines) {
         return new DialogPhase(lines, responses, moreInfoLines, correctResponseIndex, false, (String[][])null, 0);
      }

      public static DialogPhase quiz(String[] lines, String[] responses, int correctResponseIndex) {
         return new DialogPhase(lines, responses, (String[])null, correctResponseIndex, true, (String[][])null, 0);
      }

      public static DialogPhase results(String[][] resultVariants, String[] responses, int quizQuestionCount) {
         return new DialogPhase((String[])null, responses, (String[])null, 0, false, resultVariants, quizQuestionCount);
      }
   }
}
