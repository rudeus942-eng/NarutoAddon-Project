
package net.luck.narutoaddon.OtherCode.quest.core;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RepeatableQuestGenerator {
   private static final int DAILY_XP_MIN = 50;
   private static final int DAILY_XP_MAX = 100;
   private static final int WEEKLY_XP_MIN = 200;
   private static final int WEEKLY_XP_MAX = 300;
   private static final int RANDOM_XP_MIN = 15;
   private static final int RANDOM_XP_MAX = 30;
   private static final int C_DAILY_XP_MIN = 100;
   private static final int C_DAILY_XP_MAX = 200;
   private static final int C_WEEKLY_XP_MIN = 400;
   private static final int C_WEEKLY_XP_MAX = 600;
   private static final int C_RANDOM_XP_MIN = 30;
   private static final int C_RANDOM_XP_MAX = 60;
   private static final int B_DAILY_XP_MIN = 200;
   private static final int B_DAILY_XP_MAX = 400;
   private static final int B_WEEKLY_XP_MIN = 800;
   private static final int B_WEEKLY_XP_MAX = 1200;
   private static final int B_RANDOM_XP_MIN = 60;
   private static final int B_RANDOM_XP_MAX = 120;
   private static final int D_RANK_DIST_MIN = 500;
   private static final int D_RANK_DIST_MAX = 1000;
   private static final int C_RANK_DIST_MIN = 700;
   private static final int C_RANK_DIST_MAX = 1200;
   private static final int B_RANK_DIST_MIN = 1000;
   private static final int B_RANK_DIST_MAX = 1500;
   private static final int MAP_BOUND = 6000;
   private static final int MAX_LAND_RETRIES = 10;

   public static QuestOffer generateOffer(String subSlot, BlockPos playerPos, Random random, EntityPlayerMP player, QuestDefinition.QuestRank rank) {
      String baseType;
      if (subSlot.startsWith("daily")) {
         baseType = "daily";
      } else if (subSlot.startsWith("weekly")) {
         baseType = "weekly";
      } else {
         baseType = "random";
      }

      return generateTemplateOffer(subSlot, baseType, playerPos, random, player, rank);
   }

   public static QuestOffer generateOffer(String subSlot, BlockPos playerPos, Random random, EntityPlayerMP player) {
      return generateOffer(subSlot, playerPos, random, player, QuestDefinition.QuestRank.D);
   }

   public static QuestOffer generateOffer(String subSlot, BlockPos playerPos, Random random) {
      return generateOffer(subSlot, playerPos, random, (EntityPlayerMP)null, QuestDefinition.QuestRank.D);
   }

   private static QuestOffer generateTemplateOffer(String subSlot, String baseType, BlockPos playerPos, Random random, EntityPlayerMP player, QuestDefinition.QuestRank rank) {
      VillageHelper.Village village = VillageHelper.Village.UNKNOWN;
      if (player != null) {
         village = VillageHelper.getVillage(player);
      }

      MissionTemplate template;
      BlockPos origin;
      VillageHelper.Village descVillage;
      if ("daily".equals(baseType)) {
         template = MissionTemplate.randomDaily(rank, random);
         origin = getVillageOrigin(village, playerPos);
         descVillage = village;
      } else if ("weekly".equals(baseType)) {
         template = MissionTemplate.randomWeekly(rank, random);
         origin = getVillageOrigin(village, playerPos);
         descVillage = village;
      } else {
         template = MissionTemplate.randomRandom(rank, random);
         origin = playerPos;
         descVillage = VillageHelper.Village.UNKNOWN;
      }

      QuestDefinition.QuestRank templateRank;
      if (template.id.name().contains("_B_")) {
         templateRank = QuestDefinition.QuestRank.B;
      } else if (template.id.name().contains("_C_")) {
         templateRank = QuestDefinition.QuestRank.C;
      } else {
         templateRank = QuestDefinition.QuestRank.D;
      }

      World world = player != null ? player.world : null;
      BlockPos mainPos = generatePosition(origin, templateRank, random, world);
      List<GeneratedStepData> steps = template.generateSteps(mainPos, descVillage, random, world);
      String description = template.getDescription(descVillage);
      int totalEnemies = 0;
      int difficultyScore = 0;

      for(GeneratedStepData step : steps) {
         if (step.npcConfigIds != null) {
            totalEnemies += step.npcConfigIds.length;

            for(String configId : step.npcConfigIds) {
               difficultyScore += getEnemyThreat(configId);
            }
         }
      }

      int xpReward = calculateXpReward(baseType, templateRank, difficultyScore);
      int ryoReward = RyoRewardHelper.calculateGeneratedQuestRyo(templateRank, subSlot);
      String questId = "gen_" + subSlot + "_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
      return new QuestOffer(questId, template.baseName, description, templateRank, (String)null, totalEnemies, mainPos, xpReward, ryoReward, steps);
   }

   private static BlockPos getVillageOrigin(VillageHelper.Village village, BlockPos playerPos) {
      return village != null && village != VillageHelper.Village.UNKNOWN ? new BlockPos(village.centerX, 64, village.centerZ) : playerPos;
   }

   private static BlockPos generatePosition(BlockPos origin, QuestDefinition.QuestRank rank, Random random, World world) {
      int distMin;
      int distMax;
      if (rank != QuestDefinition.QuestRank.B && rank.ordinal() <= QuestDefinition.QuestRank.B.ordinal()) {
         if (rank == QuestDefinition.QuestRank.C) {
            distMin = 700;
            distMax = 1200;
         } else {
            distMin = 500;
            distMax = 1000;
         }
      } else {
         distMin = 1000;
         distMax = 1500;
      }

      TerrainCache terrain = world != null ? TerrainCache.get(world) : null;
      BlockPos result = null;

      for(int attempt = 0; attempt < 10; ++attempt) {
         double angle = random.nextDouble() * (double)2.0F * Math.PI;
         int distance = distMin + random.nextInt(distMax - distMin + 1);
         int targetX = origin.getX() + (int)(Math.cos(angle) * (double)distance);
         int targetZ = origin.getZ() + (int)(Math.sin(angle) * (double)distance);
         targetX = Math.max(-6000, Math.min(6000, targetX));
         targetZ = Math.max(-6000, Math.min(6000, targetZ));
         result = new BlockPos(targetX, 64, targetZ);
         if (terrain == null || !terrain.isWater(world, targetX, targetZ)) {
            return result;
         }
      }

      return result;
   }

   private static int getEnemyThreat(String configId) {
      if (configId == null) {
         return 1;
      } else if (configId.startsWith("b_jonin_captain")) {
         return 15;
      } else if (configId.startsWith("b_specialist")) {
         return 10;
      } else if (configId.startsWith("b_rogue_chunin")) {
         return 7;
      } else if (configId.startsWith("b_merc")) {
         return 5;
      } else if (configId.startsWith("c_chunin_captain")) {
         return 8;
      } else if (configId.startsWith("c_missing_nin")) {
         return 6;
      } else if (configId.startsWith("c_tracker_nin")) {
         return 5;
      } else if (configId.startsWith("c_assassin")) {
         return 4;
      } else if (configId.startsWith("c_rogue_genin")) {
         return 4;
      } else if (configId.startsWith("c_thug_heavy")) {
         return 3;
      } else if (configId.startsWith("c_bandit_archer")) {
         return 2;
      } else if (configId.startsWith("c_enforcer")) {
         return 2;
      } else if (configId.startsWith("d_mercenary")) {
         return 3;
      } else {
         return !configId.startsWith("d_rogue_genin") && !configId.startsWith("d_assassin") ? 1 : 2;
      }
   }

   private static int calculateXpReward(String baseType, QuestDefinition.QuestRank rank, int difficultyScore) {
      int minXp;
      int maxXp;
      int minDiff;
      int maxDiff;
      if (rank != QuestDefinition.QuestRank.B && rank.ordinal() <= QuestDefinition.QuestRank.B.ordinal()) {
         if (rank == QuestDefinition.QuestRank.C) {
            if ("daily".equals(baseType)) {
               minXp = 100;
               maxXp = 200;
               minDiff = 4;
               maxDiff = 16;
            } else if ("weekly".equals(baseType)) {
               minXp = 400;
               maxXp = 600;
               minDiff = 24;
               maxDiff = 60;
            } else {
               minXp = 30;
               maxXp = 60;
               minDiff = 4;
               maxDiff = 24;
            }
         } else if ("daily".equals(baseType)) {
            minXp = 50;
            maxXp = 100;
            minDiff = 2;
            maxDiff = 8;
         } else if ("weekly".equals(baseType)) {
            minXp = 200;
            maxXp = 300;
            minDiff = 12;
            maxDiff = 30;
         } else {
            minXp = 15;
            maxXp = 30;
            minDiff = 2;
            maxDiff = 12;
         }
      } else if ("daily".equals(baseType)) {
         minXp = 200;
         maxXp = 400;
         minDiff = 10;
         maxDiff = 40;
      } else if ("weekly".equals(baseType)) {
         minXp = 800;
         maxXp = 1200;
         minDiff = 40;
         maxDiff = 120;
      } else {
         minXp = 60;
         maxXp = 120;
         minDiff = 10;
         maxDiff = 40;
      }

      float t = (float)(difficultyScore - minDiff) / (float)(maxDiff - minDiff);
      t = Math.max(0.0F, Math.min(1.0F, t));
      return minXp + Math.round(t * (float)(maxXp - minXp));
   }

   public static class QuestOffer {
      public final String questId;
      public final String name;
      public final String description;
      public final QuestDefinition.QuestRank rank;
      public final String targetEntityId;
      public final int killCount;
      public final BlockPos position;
      public final int xpReward;
      public final int ryoReward;
      public final List<GeneratedStepData> steps;
      public final long createdAt;

      public QuestOffer(String questId, String name, String description, QuestDefinition.QuestRank rank, String targetEntityId, int killCount, BlockPos position, int xpReward, List<GeneratedStepData> steps) {
         this(questId, name, description, rank, targetEntityId, killCount, position, xpReward, 0, steps, System.currentTimeMillis());
      }

      public QuestOffer(String questId, String name, String description, QuestDefinition.QuestRank rank, String targetEntityId, int killCount, BlockPos position, int xpReward, int ryoReward, List<GeneratedStepData> steps) {
         this(questId, name, description, rank, targetEntityId, killCount, position, xpReward, ryoReward, steps, System.currentTimeMillis());
      }

      public QuestOffer(String questId, String name, String description, QuestDefinition.QuestRank rank, String targetEntityId, int killCount, BlockPos position, int xpReward, int ryoReward, List<GeneratedStepData> steps, long createdAt) {
         this.questId = questId;
         this.name = name;
         this.description = description;
         this.rank = rank;
         this.targetEntityId = targetEntityId;
         this.killCount = killCount;
         this.position = position;
         this.xpReward = xpReward;
         this.ryoReward = ryoReward;
         this.steps = steps;
         this.createdAt = createdAt;
      }

      public NBTTagCompound toNBT() {
         NBTTagCompound nbt = new NBTTagCompound();
         nbt.setString("questId", this.questId);
         nbt.setString("name", this.name != null ? this.name : "");
         nbt.setString("description", this.description != null ? this.description : "");
         nbt.setInteger("rank", this.rank != null ? this.rank.ordinal() : 0);
         if (this.targetEntityId != null) {
            nbt.setString("targetEntityId", this.targetEntityId);
         }

         nbt.setInteger("killCount", this.killCount);
         if (this.position != null) {
            nbt.setInteger("posX", this.position.getX());
            nbt.setInteger("posY", this.position.getY());
            nbt.setInteger("posZ", this.position.getZ());
         }

         nbt.setInteger("xpReward", this.xpReward);
         nbt.setInteger("ryoReward", this.ryoReward);
         nbt.setLong("createdAt", this.createdAt);
         if (this.steps != null) {
            NBTTagList stepList = new NBTTagList();

            for(GeneratedStepData step : this.steps) {
               stepList.appendTag(step.writeToNBT());
            }

            nbt.setTag("steps", stepList);
         }

         return nbt;
      }

      public static QuestOffer fromNBT(NBTTagCompound nbt) {
         String questId = nbt.getString("questId");
         String name = nbt.getString("name");
         String description = nbt.getString("description");
         int rankOrd = nbt.getInteger("rank");
         QuestDefinition.QuestRank[] ranks = QuestDefinition.QuestRank.values();
         QuestDefinition.QuestRank rank = rankOrd < ranks.length ? ranks[rankOrd] : QuestDefinition.QuestRank.D;
         String targetEntityId = nbt.hasKey("targetEntityId") ? nbt.getString("targetEntityId") : null;
         int killCount = nbt.getInteger("killCount");
         BlockPos position = null;
         if (nbt.hasKey("posX")) {
            position = new BlockPos(nbt.getInteger("posX"), nbt.getInteger("posY"), nbt.getInteger("posZ"));
         }

         int xpReward = nbt.getInteger("xpReward");
         int ryoReward = nbt.getInteger("ryoReward");
         long createdAt = nbt.hasKey("createdAt") ? nbt.getLong("createdAt") : System.currentTimeMillis();
         List<GeneratedStepData> steps = new ArrayList();
         if (nbt.hasKey("steps")) {
            NBTTagList stepList = nbt.getTagList("steps", 10);

            for(int i = 0; i < stepList.tagCount(); ++i) {
               steps.add(GeneratedStepData.readFromNBT(stepList.getCompoundTagAt(i)));
            }
         }

         return new QuestOffer(questId, name, description, rank, targetEntityId, killCount, position, xpReward, ryoReward, steps, createdAt);
      }
   }
}
