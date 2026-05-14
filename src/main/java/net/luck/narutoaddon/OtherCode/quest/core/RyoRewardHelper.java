
package net.luck.narutoaddon.OtherCode.quest.core;

import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

import java.util.Random;

public class RyoRewardHelper {
   private static final Random RANDOM = new Random();
   private static final int D_DAILY_RYO_MIN = 80;
   private static final int D_DAILY_RYO_MAX = 180;
   private static final int D_WEEKLY_RYO_MIN = 350;
   private static final int D_WEEKLY_RYO_MAX = 600;
   private static final int D_RANDOM_RYO_MIN = 30;
   private static final int D_RANDOM_RYO_MAX = 60;
   private static final int C_DAILY_RYO_MIN = 150;
   private static final int C_DAILY_RYO_MAX = 350;
   private static final int C_WEEKLY_RYO_MIN = 700;
   private static final int C_WEEKLY_RYO_MAX = 1200;
   private static final int C_RANDOM_RYO_MIN = 60;
   private static final int C_RANDOM_RYO_MAX = 120;
   private static final int B_DAILY_RYO_MIN = 300;
   private static final int B_DAILY_RYO_MAX = 600;
   private static final int B_WEEKLY_RYO_MIN = 1200;
   private static final int B_WEEKLY_RYO_MAX = 2000;
   private static final int B_RANDOM_RYO_MIN = 120;
   private static final int B_RANDOM_RYO_MAX = 250;
   private static final int A_DAILY_RYO_MIN = 500;
   private static final int A_DAILY_RYO_MAX = 900;
   private static final int A_WEEKLY_RYO_MIN = 2000;
   private static final int A_WEEKLY_RYO_MAX = 3500;
   private static final int A_RANDOM_RYO_MIN = 200;
   private static final int A_RANDOM_RYO_MAX = 400;
   private static final int S_DAILY_RYO_MIN = 700;
   private static final int S_DAILY_RYO_MAX = 1500;
   private static final int S_WEEKLY_RYO_MIN = 3500;
   private static final int S_WEEKLY_RYO_MAX = 6000;
   private static final int S_RANDOM_RYO_MIN = 300;
   private static final int S_RANDOM_RYO_MAX = 600;
   private static final int STORY_QUEST_RYO_FALLBACK = 1500;
   private static final int STORY_QUEST_RYO_MILESTONE_BONUS = 5000;
   private static final int FIRST_PVE_WIN_RYO = 100;

   public static void grantRyo(EntityPlayerMP player, int amount) {
      if (amount > 0) {
         ShopSavedData shopData = ShopSavedData.get(player.world);
         if (shopData != null) {
            shopData.addBalance(player.getUniqueID(), (long)amount);
         }

         String msg = "§aReceived: §e" + amount + " Ryo";
         player.sendMessage(new TextComponentString(msg));
      }
   }

   public static int grantRyoSilent(EntityPlayerMP player, int amount) {
      if (amount <= 0) {
         return 0;
      } else {
         ShopSavedData shopData = ShopSavedData.get(player.world);
         if (shopData != null) {
            shopData.addBalance(player.getUniqueID(), (long)amount);
         }

         return amount;
      }
   }

   public static int calculateGeneratedQuestRyo(QuestDefinition.QuestRank rank, String slot) {
      String baseType;
      if (slot.startsWith("daily")) {
         baseType = "daily";
      } else if (slot.startsWith("weekly")) {
         baseType = "weekly";
      } else {
         baseType = "random";
      }

      int min;
      int max;
      switch (rank) {
         case C:
            min = "daily".equals(baseType) ? 150 : ("weekly".equals(baseType) ? 700 : 60);
            max = "daily".equals(baseType) ? 350 : ("weekly".equals(baseType) ? 1200 : 120);
            break;
         case B:
            min = "daily".equals(baseType) ? 300 : ("weekly".equals(baseType) ? 1200 : 120);
            max = "daily".equals(baseType) ? 600 : ("weekly".equals(baseType) ? 2000 : 250);
            break;
         case A:
            min = "daily".equals(baseType) ? 500 : ("weekly".equals(baseType) ? 2000 : 200);
            max = "daily".equals(baseType) ? 900 : ("weekly".equals(baseType) ? 3500 : 400);
            break;
         case S:
         case S_PLUS:
            min = "daily".equals(baseType) ? 700 : ("weekly".equals(baseType) ? 3500 : 300);
            max = "daily".equals(baseType) ? 1500 : ("weekly".equals(baseType) ? 6000 : 600);
            break;
         default:
            min = "daily".equals(baseType) ? 80 : ("weekly".equals(baseType) ? 350 : 30);
            max = "daily".equals(baseType) ? 180 : ("weekly".equals(baseType) ? 600 : 60);
      }

      return min + RANDOM.nextInt(max - min + 1);
   }

   public static int calculateStoryQuestRyo(QuestDefinition def) {
      if (def == null) {
         return 1500;
      } else {
         int baseRyo = def.hasRyoOverride() ? def.getRyoReward() : 1500;
         if (baseRyo <= 0) {
            return 0;
         } else {
            int multiplier = getArcMultiplier(def.getArcNumber());
            int scaled = baseRyo * multiplier;
            boolean isMilestone = false;

            for(QuestReward reward : def.getRewards()) {
               if (reward.type == QuestReward.RewardType.COMMAND && reward.command != null && reward.command.contains("rank add")) {
                  isMilestone = true;
                  break;
               }
            }

            if (isMilestone) {
               scaled += 5000;
            }

            return scaled;
         }
      }
   }

   private static int getArcMultiplier(int arc) {
      if (arc >= 99) {
         return 8;
      } else if (arc >= 10) {
         return 20;
      } else if (arc >= 9) {
         return 18;
      } else if (arc >= 8) {
         return 15;
      } else if (arc >= 7) {
         return 13;
      } else if (arc >= 6) {
         return 11;
      } else if (arc >= 5) {
         return 9;
      } else if (arc >= 4) {
         return 7;
      } else if (arc >= 3) {
         return 5;
      } else {
         return arc >= 2 ? 4 : 3;
      }
   }

   public static void grantGeneratedQuestReward(EntityPlayerMP player, QuestInstance quest, String slot) {
      QuestDefinition.QuestRank rank = QuestDefinition.QuestRank.values()[quest.getGeneratedRankOrdinal()];
      int ryoAmount = calculateGeneratedQuestRyo(rank, slot);
      grantRyo(player, ryoAmount);
      checkFirstPveWinBonus(player);
   }

   public static void grantStoryQuestReward(EntityPlayerMP player, QuestDefinition def) {
      int ryoAmount = calculateStoryQuestRyo(def);
      grantRyo(player, ryoAmount);
   }

   private static void checkFirstPveWinBonus(EntityPlayerMP player) {
      try {
         ShopSavedData shopData = ShopSavedData.get(player.world);
         if (shopData.canClaimFirstPveWin(player.getUniqueID())) {
            shopData.claimFirstPveWin(player.getUniqueID());
            grantRyo(player, 100);
            player.sendMessage(new TextComponentString("§6§l☆ First PvE Win Bonus! §r§a+100 Ryo"));
         }
      } catch (Exception var2) {
      }

   }
}
