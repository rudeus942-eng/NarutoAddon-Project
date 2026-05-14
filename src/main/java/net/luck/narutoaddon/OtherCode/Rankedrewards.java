
package net.luck.narutoaddon.OtherCode;

import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Rankedrewards {
   public static final int BASE_WIN_RYO = 1000;
   public static final int LOSER_RYO = 250;
   public static final int DAILY_WINS_REQUIRED = 3;
   public static final int DAILY_TASK_RYO = 3000;
   private static final Map<Integer, StreakBonus> STREAK_BONUSES = new HashMap();
   private static final Map<String, Integer> RANK_UP_RYO;
   private static final Map<String, String> RANK_UP_MESSAGES;

   public static void giveRyo(EntityPlayerMP player, int amount) {
      if (amount > 0) {
         RyoRewardHelper.grantRyoSilent(player, amount);
      }
   }

   public static long getCurrentDay() {
      return System.currentTimeMillis() / 86400000L;
   }

   public static void checkDailyReset(Rankeddatastorage.PlayerRankedData playerData) {
      long currentDay = getCurrentDay();
      if (playerData.dailyTaskLastReset < currentDay) {
         playerData.dailyWinsToday = 0;
         playerData.dailyTaskCompleted = false;
         playerData.dailyTaskClaimed = false;
         playerData.dailyTaskLastReset = currentDay;
      }

   }

   public static DailyTaskProgress getDailyTaskProgress(World world, String uuid) {
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      if (storage == null) {
         return new DailyTaskProgress();
      } else {
         Rankeddatastorage.PlayerRankedData playerData = storage.getPlayerData(uuid);
         checkDailyReset(playerData);
         DailyTaskProgress progress = new DailyTaskProgress();
         progress.winsToday = playerData.dailyWinsToday;
         progress.winsRequired = 3;
         progress.completed = playerData.dailyTaskCompleted;
         progress.claimed = playerData.dailyTaskClaimed;
         progress.lastResetDay = playerData.dailyTaskLastReset;
         return progress;
      }
   }

   private static boolean hasClaimedRankRewardThisSeason(Rankeddatastorage.PlayerRankedData playerData, String tierName) {
      if (playerData.claimedRankRewards != null && !playerData.claimedRankRewards.isEmpty()) {
         String key = playerData.season + ":" + tierName;
         return playerData.claimedRankRewards.contains(key);
      } else {
         return false;
      }
   }

   private static void markRankRewardClaimed(Rankeddatastorage.PlayerRankedData playerData, String tierName) {
      if (playerData.claimedRankRewards == null) {
         playerData.claimedRankRewards = new HashSet();
      }

      String key = playerData.season + ":" + tierName;
      playerData.claimedRankRewards.add(key);
   }

   public static RewardResult processWinReward(EntityPlayerMP player, int newWinStreak, int gamesPlayed, String oldTier, String newTier) {
      RewardResult result = new RewardResult();
      World world = player.world;
      String uuid = player.getUniqueID().toString();
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      if (storage == null) {
         return result;
      } else {
         Rankeddatastorage.PlayerRankedData playerData = storage.getPlayerData(uuid);
         result.ryoEarned = 1000;
         StreakBonus streakBonus = getStreakBonus(newWinStreak);
         if (streakBonus != null) {
            result.ryoEarned += streakBonus.ryoAmount;
            result.streakAnnouncement = streakBonus.announcement;
            result.hadStreak = true;
         }

         if (oldTier != null && newTier != null && !oldTier.equals(newTier) && !hasClaimedRankRewardThisSeason(playerData, newTier)) {
            Integer rankRyo = (Integer)RANK_UP_RYO.get(newTier);
            if (rankRyo != null) {
               result.ryoEarned += rankRyo;
               result.rankUpMessage = (String)RANK_UP_MESSAGES.get(newTier);
               result.hadRankUp = true;
               markRankRewardClaimed(playerData, newTier);
            }
         }

         checkDailyReset(playerData);
         ++playerData.dailyWinsToday;
         if (playerData.dailyWinsToday >= 3 && !playerData.dailyTaskClaimed) {
            playerData.dailyTaskCompleted = true;
            result.ryoEarned += 3000;
            result.completedDailyTask = true;
            result.dailyTaskMessage = "§a§lDaily Task Complete! +3000 Ryo";
            playerData.dailyTaskClaimed = true;
         }

         storage.savePlayerData(uuid, playerData);
         giveRyo(player, result.ryoEarned);
         player.sendMessage(new TextComponentString("§a§l+" + result.ryoEarned + " Ryo"));
         if (result.hadStreak && result.streakAnnouncement != null) {
            player.sendMessage(new TextComponentString(result.streakAnnouncement));
         }

         if (result.hadRankUp && result.rankUpMessage != null) {
            player.sendMessage(new TextComponentString(result.rankUpMessage));
         }

         if (result.completedDailyTask && result.dailyTaskMessage != null) {
            player.sendMessage(new TextComponentString(result.dailyTaskMessage));
         }

         return result;
      }
   }

   public static void processLossReward(EntityPlayerMP player) {
      if (player != null) {
         giveRyo(player, 250);
         player.sendMessage(new TextComponentString("§7Ranked match: you lost. §a+250 Ryo §7(consolation)"));
      }
   }

   private static StreakBonus getStreakBonus(int streak) {
      StreakBonus best = null;

      for(Map.Entry<Integer, StreakBonus> entry : STREAK_BONUSES.entrySet()) {
         if (streak >= (Integer)entry.getKey() && (best == null || (Integer)entry.getKey() > best.streak)) {
            best = (StreakBonus)entry.getValue();
         }
      }

      return best;
   }

   private static boolean isExactStreakMilestone(int streak) {
      return streak == 3 || streak == 5 || streak == 7 || streak == 10 || streak == 15;
   }

   public static int getRankUpRyo(String tierName) {
      return (Integer)RANK_UP_RYO.getOrDefault(tierName, 0);
   }

   public static int[] getStreakThresholds() {
      return new int[]{3, 5, 7, 10, 15};
   }

   public static int getStreakRyo(int streak) {
      StreakBonus bonus = (StreakBonus)STREAK_BONUSES.get(streak);
      return bonus != null ? bonus.ryoAmount : 0;
   }

   static {
      STREAK_BONUSES.put(3, new StreakBonus(3, 1000, "§e3 Win Streak!"));
      STREAK_BONUSES.put(5, new StreakBonus(5, 1600, "§6§l5 Win Streak!"));
      STREAK_BONUSES.put(7, new StreakBonus(7, 2400, "§c§l7 Win Streak!!"));
      STREAK_BONUSES.put(10, new StreakBonus(10, 3200, "§d§l10 WIN STREAK!"));
      STREAK_BONUSES.put(15, new StreakBonus(15, 4000, "§4§lLEGENDARY 15 STREAK!"));
      RANK_UP_RYO = new HashMap();
      RANK_UP_RYO.put("Chunin", 3000);
      RANK_UP_RYO.put("Jonin", 6000);
      RANK_UP_RYO.put("S. Jonin", 10000);
      RANK_UP_RYO.put("Elite Jonin", 16000);
      RANK_UP_RYO.put("ANBU", 24000);
      RANK_UP_RYO.put("Kage", 36000);
      RANK_UP_RYO.put("Otsutsuki", 50000);
      RANK_UP_MESSAGES = new HashMap();
      RANK_UP_MESSAGES.put("Chunin", "§aPromoted to Chunin!");
      RANK_UP_MESSAGES.put("Jonin", "§6Promoted to Jonin!");
      RANK_UP_MESSAGES.put("S. Jonin", "§ePromoted to S. Jonin!");
      RANK_UP_MESSAGES.put("Elite Jonin", "§bPromoted to Elite Jonin!");
      RANK_UP_MESSAGES.put("ANBU", "§5Promoted to ANBU!");
      RANK_UP_MESSAGES.put("Kage", "§cPromoted to Kage!");
      RANK_UP_MESSAGES.put("Otsutsuki", "§4§lACHIEVED OTSUTSUKI RANK!");
   }

   public static class StreakBonus {
      public int streak;
      public int ryoAmount;
      public String announcement;

      public StreakBonus(int streak, int ryoAmount, String announcement) {
         this.streak = streak;
         this.ryoAmount = ryoAmount;
         this.announcement = announcement;
      }
   }

   public static class RewardResult {
      public int ryoEarned = 0;
      public String streakAnnouncement;
      public String rankUpMessage;
      public String dailyTaskMessage;
      public boolean hadStreak;
      public boolean hadRankUp;
      public boolean completedDailyTask;
   }

   public static class DailyTaskProgress {
      public int winsToday = 0;
      public int winsRequired = 3;
      public boolean completed = false;
      public boolean claimed = false;
      public long lastResetDay = Rankedrewards.getCurrentDay();

      public int getProgressPercent() {
         return Math.min(100, this.winsToday * 100 / this.winsRequired);
      }

      public String getProgressString() {
         return this.winsToday + "/" + this.winsRequired;
      }
   }
}
