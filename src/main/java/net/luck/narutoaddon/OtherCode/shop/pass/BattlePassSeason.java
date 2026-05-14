package net.luck.narutoaddon.OtherCode.shop.pass;

import java.util.Calendar;
import java.util.TimeZone;

public class BattlePassSeason {
   public static final long DURATION_MS = 1209600000L;
   public static final long GRACE_PERIOD_MS = 172800000L;
   public static final String RESET_TIMEZONE = "America/New_York";
   public static final int RESET_HOUR = 16;
   private static final long ANCHOR_MS;
   private final int seasonNumber;
   private final String seasonName;
   private final long startTime;
   private final long endTime;
   private final String exclusiveJutsuItemId;
   private final String killEffectId;

   public BattlePassSeason(int seasonNumber, String seasonName, long startTime) {
      this.seasonNumber = seasonNumber;
      this.seasonName = seasonName;
      this.startTime = startTime;
      this.endTime = startTime + 1209600000L;
      this.exclusiveJutsuItemId = "inftsukaddon:seasonal_release";
      this.killEffectId = getDefaultKillEffectId(seasonNumber);
   }

   private static String getDefaultKillEffectId(int seasonNum) {
      switch (seasonNum) {
         case 1:
            return "s1_fire_burst";
         case 2:
            return "s2_lightning_strike";
         case 3:
            return "s3_shadow_dissolve";
         default:
            return "s" + seasonNum + "_fire_burst";
      }
   }

   public BattlePassSeason(int seasonNumber, String seasonName, long startTime, String exclusiveJutsuItemId, String killEffectId) {
      this.seasonNumber = seasonNumber;
      this.seasonName = seasonName;
      this.startTime = startTime;
      this.endTime = startTime + 1209600000L;
      this.exclusiveJutsuItemId = exclusiveJutsuItemId;
      this.killEffectId = killEffectId;
   }

   public int getSeasonNumber() {
      return this.seasonNumber;
   }

   public String getSeasonName() {
      return this.seasonName;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public long getEndTime() {
      return this.endTime;
   }

   public String getExclusiveJutsuItemId() {
      return this.exclusiveJutsuItemId;
   }

   public String getKillEffectId() {
      return this.killEffectId;
   }

   public boolean isActive(long now) {
      return now >= this.startTime && now < this.endTime;
   }

   public boolean isInGracePeriod(long now) {
      return now >= this.endTime && now < this.endTime + 172800000L;
   }

   public boolean canEarnXP(long now) {
      return now >= this.startTime && now < this.endTime + 172800000L;
   }

   public long getTimeRemainingMs(long now) {
      long remaining = this.endTime - now;
      return remaining > 0L ? remaining : 0L;
   }

   public static long getAlignedStartTime(long now) {
      long elapsed = now - ANCHOR_MS;
      if (elapsed < 0L) {
         return ANCHOR_MS;
      } else {
         long cycleCount = elapsed / 1209600000L;
         return ANCHOR_MS + cycleCount * 1209600000L;
      }
   }

   public static long getNextResetTime(long now) {
      return getAlignedStartTime(now) + 1209600000L;
   }

   public static BattlePassSeason createSeason1(long startTime) {
      return new BattlePassSeason(1, "Season 1: Dawn of the Shinobi", startTime);
   }

   public static String getSeasonDisplayName(int seasonNum) {
      switch (seasonNum) {
         case 1:
            return "Season 1: Dawn of the Shinobi";
         case 2:
            return "Season 2: Wrath of the Storm";
         case 3:
            return "Season 3: Veil of Shadow";
         default:
            return "Season " + seasonNum;
      }
   }

   static {
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
      cal.set(2026, 2, 27, 0, 0, 0);
      cal.set(14, 0);
      ANCHOR_MS = cal.getTimeInMillis();
   }
}
