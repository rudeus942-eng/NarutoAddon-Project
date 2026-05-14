package net.luck.narutoaddon.OtherCode.quest.core;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestDefinition {
   private final String id;
   private final String name;
   private final String description;
   private final int arcNumber;
   private final RepeatType repeatType;
   private final QuestRank rank;
   private final List<QuestStep> steps;
   private final List<QuestReward> rewards;
   private final List<String> prerequisiteQuestIds;
   private final String requiredAdvancement;
   private final int ryoReward;
   private final List<SceneNpc> sceneNpcs;
   private final String storyline;

   private QuestDefinition(Builder builder) {
      this.id = builder.id;
      this.name = builder.name;
      this.description = builder.description;
      this.arcNumber = builder.arcNumber;
      this.repeatType = builder.repeatType;
      this.rank = builder.rank;
      this.steps = Collections.unmodifiableList(new ArrayList(builder.steps));
      this.rewards = Collections.unmodifiableList(new ArrayList(builder.rewards));
      this.prerequisiteQuestIds = Collections.unmodifiableList(new ArrayList(builder.prerequisiteQuestIds));
      this.requiredAdvancement = builder.requiredAdvancement;
      this.ryoReward = builder.ryoReward;
      this.sceneNpcs = Collections.unmodifiableList(new ArrayList(builder.sceneNpcs));
      this.storyline = builder.storyline;
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public int getArcNumber() {
      return this.arcNumber;
   }

   public RepeatType getRepeatType() {
      return this.repeatType;
   }

   public boolean isRepeatable() {
      return this.repeatType != RepeatType.NONE;
   }

   public QuestRank getRank() {
      return this.rank;
   }

   public QuestStep getStep(int index) {
      return index >= 0 && index < this.steps.size() ? (QuestStep)this.steps.get(index) : null;
   }

   public int getStepCount() {
      return this.steps.size();
   }

   public List<QuestStep> getSteps() {
      return this.steps;
   }

   public List<QuestReward> getRewards() {
      return this.rewards;
   }

   public List<String> getPrerequisites() {
      return this.prerequisiteQuestIds;
   }

   public String getRequiredAdvancement() {
      return this.requiredAdvancement;
   }

   public boolean hasRequiredAdvancement() {
      return this.requiredAdvancement != null && !this.requiredAdvancement.isEmpty();
   }

   public int getRyoReward() {
      return this.ryoReward;
   }

   public boolean hasRyoOverride() {
      return this.ryoReward >= 0;
   }

   public List<SceneNpc> getSceneNpcs() {
      return this.sceneNpcs;
   }

   public List<SceneNpc> getSceneNpcsForGroup(String group) {
      List<SceneNpc> result = new ArrayList();

      for(SceneNpc npc : this.sceneNpcs) {
         if (npc.sceneGroup.equals(group)) {
            result.add(npc);
         }
      }

      return result;
   }

   public String getStoryline() {
      return this.storyline;
   }

   public static Builder builder(String id, String name) {
      return new Builder(id, name);
   }

   public static class SceneNpc {
      public final String sceneGroup;
      public final String npcConfigId;
      public final BlockPos position;

      public SceneNpc(String sceneGroup, String npcConfigId, BlockPos position) {
         this.sceneGroup = sceneGroup;
         this.npcConfigId = npcConfigId;
         this.position = position;
      }
   }

   public static enum RepeatType {
      NONE,
      DAILY,
      WEEKLY,
      RANDOM;
   }

   public static enum QuestRank {
      D("D", -11141291, 1.2),
      C("C", -11184641, (double)1.5F),
      B("B", -5635926, (double)2.0F),
      A("A", -22016, (double)2.5F),
      S("S", -43691, (double)3.0F),
      S_PLUS("S+", -43521, (double)4.0F);

      public final String displayName;
      public final int color;
      public final double xpMultiplier;

      private QuestRank(String displayName, int color, double xpMultiplier) {
         this.displayName = displayName;
         this.color = color;
         this.xpMultiplier = xpMultiplier;
      }
   }

   public static class Builder {
      private final String id;
      private final String name;
      private String description;
      private int arcNumber;
      private RepeatType repeatType;
      private QuestRank rank;
      private final List<QuestStep> steps;
      private final List<QuestReward> rewards;
      private final List<String> prerequisiteQuestIds;
      private String requiredAdvancement;
      private int ryoReward;
      private final List<SceneNpc> sceneNpcs;
      private String storyline;

      private Builder(String id, String name) {
         this.description = "";
         this.arcNumber = 1;
         this.repeatType = RepeatType.NONE;
         this.rank = QuestRank.D;
         this.steps = new ArrayList();
         this.rewards = new ArrayList();
         this.prerequisiteQuestIds = new ArrayList();
         this.ryoReward = -1;
         this.sceneNpcs = new ArrayList();
         this.storyline = "leaf_story";
         this.id = id;
         this.name = name;
      }

      public Builder arc(int arc) {
         this.arcNumber = arc;
         return this;
      }

      public Builder desc(String description) {
         this.description = description;
         return this;
      }

      public Builder repeat(RepeatType type) {
         this.repeatType = type;
         return this;
      }

      public Builder rank(QuestRank rank) {
         this.rank = rank;
         return this;
      }

      public Builder addStep(QuestStep step) {
         this.steps.add(step);
         return this;
      }

      public Builder addReward(QuestReward reward) {
         this.rewards.add(reward);
         return this;
      }

      public Builder prereq(String questId) {
         this.prerequisiteQuestIds.add(questId);
         return this;
      }

      public Builder requireAdvancement(String advancementId) {
         this.requiredAdvancement = advancementId;
         return this;
      }

      public Builder ryoReward(int amount) {
         this.ryoReward = amount;
         return this;
      }

      public Builder addSceneNpc(String sceneGroup, String npcConfigId, BlockPos pos) {
         this.sceneNpcs.add(new SceneNpc(sceneGroup, npcConfigId, pos));
         return this;
      }

      public Builder storyline(String storyline) {
         this.storyline = storyline;
         return this;
      }

      public QuestDefinition build() {
         return new QuestDefinition(this);
      }
   }
}
