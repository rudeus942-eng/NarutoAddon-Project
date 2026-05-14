package net.luck.narutoaddon.OtherCode.territory.core;

import java.util.Calendar;
import java.util.TimeZone;

public class TerritoryConstants {
   public static boolean ENABLED = false;
   public static final int CAPTURE_POINTS_NEEDED = 75;
   public static final int PVP_KILL_POINTS = 15;
   public static final int PVE_NPC_KILL_POINTS = 10;
   public static final int STANDING_POINTS_PER_TICK = 1;
   public static final int STANDING_TICK_INTERVAL = 200;
   public static final int CAPTURE_DECAY_RATE = 1;
   public static final int CAPTURE_DECAY_INTERVAL = 300;
   public static final int TOGGLE_COOLDOWN_TICKS = 2400;
   public static final double RYO_MULTIPLIER_1ST = (double)0.25F;
   public static final double RYO_MULTIPLIER_2ND = 0.12;
   public static final double RYO_MULTIPLIER_3RD = 0.05;
   public static final int LEADERBOARD_BONUS_1ST = 200000;
   public static final int LEADERBOARD_BONUS_2ND = 100000;
   public static final int LEADERBOARD_BONUS_3RD = 50000;
   public static final int LEADERBOARD_BONUS_4TH = 25000;
   public static final int LEADERBOARD_BONUS_5TH = 10000;
   public static final int LEADERBOARD_BONUS_6TH = 5000;
   public static final int CONTRIBUTION_DIAMOND_THRESHOLD = 40000;
   public static final int CONTRIBUTION_GOLD_THRESHOLD = 28000;
   public static final int CONTRIBUTION_SILVER_THRESHOLD = 16000;
   public static final int CONTRIBUTION_BRONZE_THRESHOLD = 6000;
   public static final int CONTRIBUTION_IRON_THRESHOLD = 1500;
   public static final int CONTRIBUTION_DIAMOND_PERCENT = 150;
   public static final int CONTRIBUTION_GOLD_PERCENT = 120;
   public static final int CONTRIBUTION_SILVER_PERCENT = 90;
   public static final int CONTRIBUTION_BRONZE_PERCENT = 60;
   public static final int CONTRIBUTION_IRON_PERCENT = 25;
   public static final double UNDERDOG_FLOOR = 0.15;
   public static final int WEEKLY_RESET_DAY = 7;
   public static final int WEEKLY_RESET_LOCAL_HOUR = 16;
   public static final String RESET_TIMEZONE = "America/New_York";
   public static final int WEEKLY_RESET_HOUR = 20;
   public static final long FIRST_SEASON_END_MS;
   public static final long PATROL_DEFENDER_COOLDOWN_TICKS = 18000L;
   public static final int HOLDING_SCORE_INTERVAL = 6000;
   public static final int HOLDING_SCORE_NORMAL = 5;
   public static final int HOLDING_SCORE_PLAYER = 1;
   public static final int[] WALL_COSTS;
   public static final int[] GARRISON_COSTS;
   public static final int[] TRAINING_COSTS;
   public static final int[] WATCHTOWER_COSTS;
   public static final int[] SPECIALIST_COSTS;
   public static final int[] AK_WALL_COSTS;
   public static final int[] AK_GARRISON_COSTS;
   public static final int[] AK_TRAINING_COSTS;
   public static final int[] AK_WATCHTOWER_COSTS;
   public static final int[] AK_SPECIALIST_COSTS;
   public static final int[] DEFENDER_COOLDOWNS;
   public static final double AKATSUKI_CAP_MULTIPLIER = (double)2.5F;
   public static final double AFK_MOVEMENT_THRESHOLD = (double)3.0F;
   public static final int ZETSU_MAX_TARGETS = 3;
   public static final long ZETSU_TARGET_TIMEOUT_MS = 900000L;
   public static final long ZETSU_RESPAWN_COOLDOWN_MS = 300000L;

   private TerritoryConstants() {
   }

   static {
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
      cal.set(2026, 2, 21, 16, 0, 0);
      cal.set(14, 0);
      FIRST_SEASON_END_MS = cal.getTimeInMillis();
      WALL_COSTS = new int[]{500, 1500, 3000};
      GARRISON_COSTS = new int[]{750, 2000, 4000};
      TRAINING_COSTS = new int[]{1000, 2500, 5000};
      WATCHTOWER_COSTS = new int[]{800, 2000};
      SPECIALIST_COSTS = new int[]{600, 1500};
      AK_WALL_COSTS = new int[]{10, 25, 50};
      AK_GARRISON_COSTS = new int[]{15, 30, 60};
      AK_TRAINING_COSTS = new int[]{20, 40, 75};
      AK_WATCHTOWER_COSTS = new int[]{10, 25};
      AK_SPECIALIST_COSTS = new int[]{10, 20};
      DEFENDER_COOLDOWNS = new int[]{18000, 12000, 7200};
   }
}
