package net.luck.narutoaddon.OtherCode.shop.pass;

public class BattlePassDefinition {
   public static final int MAX_TIER = 20;
   public static final int TOTAL_XP = 25000;
   public static final int KAGE_PASS_COST = 300;
   public static final int OTSUTSUKI_PASS_COST = 500;
   public static final int OTSUTSUKI_FREE_SKIPS = 5;
   public static final int SKIP_COST_LOW = 10;
   public static final int SKIP_COST_MID = 15;
   public static final int SKIP_COST_HIGH = 25;
   public static final int CAP_LOGIN = 100;
   public static final int CAP_DAILY_PVE = 375;
   public static final int CAP_RANKED_WIN = 225;
   public static final int CAP_RANKED_LOSS = 60;
   public static final int CAP_WAR_PARTICIPATE = 150;
   public static final int CAP_WAR_WIN = 100;
   public static final int CAP_CRATE_ROLL = 120;
   public static final int CAP_TERRITORY = 150;
   public static final int XP_LOGIN = 100;
   public static final int XP_DAILY_PVE = 125;
   public static final int XP_WEEKLY_PVE = 350;
   public static final int XP_RANKED_WIN = 75;
   public static final int XP_RANKED_LOSS = 20;
   public static final int XP_WAR_PARTICIPATE = 150;
   public static final int XP_WAR_WIN = 100;
   public static final int XP_CRATE_ROLL = 40;
   public static final int XP_TERRITORY = 75;
   public static final int XP_STORY_QUEST = 125;
   public static final int XP_LOGIN_STREAK_7 = 250;

   public static int getXPForLevel(int level) {
      if (level > 0 && level <= 20) {
         if (level <= 5) {
            return 500;
         } else if (level <= 10) {
            return 800;
         } else {
            return level <= 15 ? 1200 : 2500;
         }
      } else {
         return 0;
      }
   }

   public static int getCumulativeXP(int level) {
      if (level <= 0) {
         return 0;
      } else {
         if (level > 20) {
            level = 20;
         }

         int total = 0;

         for(int i = 1; i <= level; ++i) {
            total += getXPForLevel(i);
         }

         return total;
      }
   }

   public static int getSkipCost(int currentLevel) {
      if (currentLevel >= 0 && currentLevel < 20) {
         int nextLevel = currentLevel + 1;
         if (nextLevel <= 7) {
            return 10;
         } else {
            return nextLevel <= 14 ? 15 : 25;
         }
      } else {
         return 0;
      }
   }

   public static int getDailyCap(String source) {
      switch (source) {
         case "login":
            return 100;
         case "daily_pve":
            return 375;
         case "ranked_win":
            return 225;
         case "ranked_loss":
            return 60;
         case "war_participate":
            return 150;
         case "war_win":
            return 100;
         case "crate_roll":
            return 120;
         case "territory":
            return 150;
         default:
            return -1;
      }
   }

   public static TierReward getFreeTierReward(int tier) {
      switch (tier) {
         case 1:
            return TierReward.ryo(500);
         case 2:
            return TierReward.itemReward("narutomod:military_rations_pill", 0, 3, "Military Rations Pill x3");
         case 3:
            return TierReward.ryo(1000);
         case 4:
            return TierReward.crate("armor_crate_1", 1, 0);
         case 5:
            return TierReward.crate("jutsu_xp_crate", 1, 0);
         case 6:
            return TierReward.ryo(1500);
         case 7:
            return TierReward.itemReward("narutomod:military_rations_pill_gold", 0, 3, "Military Rations Pill (Gold) x3");
         case 8:
            return TierReward.crate("jutsu_xp_crate", 1, 0);
         case 9:
            return TierReward.ryo(2000);
         case 10:
            return TierReward.crate("armor_crate_2", 1, 0);
         case 11:
            return TierReward.ryo(2500);
         case 12:
            return TierReward.crate("jutsu_xp_crate", 2, 0);
         case 13:
            return TierReward.ryo(3000);
         case 14:
            return TierReward.crate("jutsu_crate_1", 1, 0);
         case 15:
            return TierReward.crate("armor_crate_3", 1, 0);
         case 16:
            return TierReward.ryo(3500);
         case 17:
            return TierReward.itemReward("narutomod:military_rations_pill_gold", 0, 3, "Military Rations Pill (Gold) x3");
         case 18:
            return TierReward.crate("jutsu_xp_crate", 2, 0);
         case 19:
            return TierReward.ryo(4000);
         case 20:
            return TierReward.ryoAndCrate(5000, "armor_crate_3", 1);
         default:
            return TierReward.EMPTY;
      }
   }

   /** @deprecated */
   public static TierReward getPremiumTierReward(int tier) {
      return getPremiumTierReward(tier, 1);
   }

   public static TierReward getPremiumTierReward(int tier, int seasonNumber) {
      switch (seasonNumber) {
         case 2:
            return getSeason2PremiumReward(tier);
         case 3:
            return getSeason3PremiumReward(tier);
         default:
            return getSeason1PremiumReward(tier);
      }
   }

   private static TierReward getSeason1PremiumReward(int tier) {
      switch (tier) {
         case 1:
            return TierReward.crystals(15);
         case 2:
            return TierReward.ryoAndCrate(5000, "armor_crate_3", 1);
         case 3:
            return TierReward.crate("jutsu_xp_crate", 2, 0);
         case 4:
            return TierReward.ryoAndCrate(10000, "jutsu_crate_2", 1);
         case 5:
            return TierReward.crystalsAndCrate(10, "armor_crate_3", 1);
         case 6:
            return TierReward.ryoAndJutsuXP(5000, 1000);
         case 7:
            return TierReward.doubleCrate("jutsu_crate_2", 1, "armor_crate_3", 1);
         case 8:
            return TierReward.ryo(15000);
         case 9:
            return TierReward.jutsuXPAndCrate(1000, "jutsu_crate_3", 1);
         case 10:
            return TierReward.crystalsAndCrate(15, "armor_crate_4", 1);
         case 11:
            return TierReward.ryoAndCrate(20000, "jutsu_xp_crate", 2);
         case 12:
            return TierReward.doubleCrate("jutsu_crate_3", 1, "armor_crate_4", 1);
         case 13:
            return TierReward.jutsuXPAndRyo(1000, 15000);
         case 14:
            return TierReward.doubleCrate("cash_weapon", 1, "jutsu_crate_3", 1);
         case 15:
            return TierReward.crystalsAndExclusive(10, false, "kill_effect");
         case 16:
            return TierReward.ryoAndCrate(25000, "armor_crate_5", 1);
         case 17:
            return TierReward.doubleCrate("jutsu_xp_crate", 2, "jutsu_crate_4", 1);
         case 18:
            return TierReward.ryoAndCrate(30000, "cash_weapon", 1);
         case 19:
            return TierReward.crystalsAndRyo(30, 20000);
         case 20:
            return TierReward.exclusive("season_jutsu");
         default:
            return TierReward.EMPTY;
      }
   }

   private static TierReward getSeason2PremiumReward(int tier) {
      switch (tier) {
         case 1:
            return TierReward.crystals(15);
         case 2:
            return TierReward.ryoAndCrate(5000, "armor_crate_3", 1);
         case 3:
            return TierReward.crate("jutsu_xp_crate", 2, 0);
         case 4:
            return TierReward.ryoAndCrate(10000, "jutsu_crate_2", 1);
         case 5:
            return TierReward.crystalsAndCrate(10, "armor_crate_3", 1);
         case 6:
            return TierReward.ryoAndJutsuXP(5000, 1000);
         case 7:
            return TierReward.doubleCrate("jutsu_crate_2", 1, "armor_crate_3", 1);
         case 8:
            return TierReward.ryo(15000);
         case 9:
            return TierReward.jutsuXPAndCrate(1000, "jutsu_crate_3", 1);
         case 10:
            return TierReward.crystalsAndCrate(15, "armor_crate_4", 1);
         case 11:
            return TierReward.ryoAndCrate(20000, "jutsu_xp_crate", 2);
         case 12:
            return TierReward.doubleCrate("jutsu_crate_3", 1, "armor_crate_4", 1);
         case 13:
            return TierReward.jutsuXPAndRyo(1000, 15000);
         case 14:
            return TierReward.doubleCrate("cash_weapon", 1, "jutsu_crate_3", 1);
         case 15:
            return TierReward.crystalsAndExclusive(10, false, "kill_effect");
         case 16:
            return TierReward.ryoAndCrate(25000, "armor_crate_5", 1);
         case 17:
            return TierReward.doubleCrate("jutsu_xp_crate", 2, "jutsu_crate_4", 1);
         case 18:
            return TierReward.ryoAndCrate(30000, "cash_weapon", 1);
         case 19:
            return TierReward.crystalsAndRyo(30, 20000);
         case 20:
            return TierReward.exclusive("season_jutsu");
         default:
            return TierReward.EMPTY;
      }
   }

   private static TierReward getSeason3PremiumReward(int tier) {
      switch (tier) {
         case 1:
            return TierReward.crystals(15);
         case 2:
            return TierReward.ryoAndCrate(5000, "armor_crate_3", 1);
         case 3:
            return TierReward.crate("jutsu_xp_crate", 2, 0);
         case 4:
            return TierReward.ryoAndCrate(10000, "jutsu_crate_2", 1);
         case 5:
            return TierReward.crystalsAndCrate(10, "armor_crate_3", 1);
         case 6:
            return TierReward.ryoAndJutsuXP(5000, 1000);
         case 7:
            return TierReward.doubleCrate("jutsu_crate_2", 1, "armor_crate_3", 1);
         case 8:
            return TierReward.ryo(15000);
         case 9:
            return TierReward.jutsuXPAndCrate(1000, "jutsu_crate_3", 1);
         case 10:
            return TierReward.crystalsAndCrate(15, "armor_crate_4", 1);
         case 11:
            return TierReward.ryoAndCrate(20000, "jutsu_xp_crate", 2);
         case 12:
            return TierReward.doubleCrate("jutsu_crate_3", 1, "armor_crate_4", 1);
         case 13:
            return TierReward.jutsuXPAndRyo(1000, 15000);
         case 14:
            return TierReward.doubleCrate("cash_weapon", 1, "jutsu_crate_3", 1);
         case 15:
            return TierReward.crystalsAndExclusive(10, false, "kill_effect");
         case 16:
            return TierReward.ryoAndCrate(25000, "armor_crate_5", 1);
         case 17:
            return TierReward.doubleCrate("jutsu_xp_crate", 2, "jutsu_crate_4", 1);
         case 18:
            return TierReward.ryoAndCrate(30000, "cash_weapon", 1);
         case 19:
            return TierReward.crystalsAndRyo(30, 20000);
         case 20:
            return TierReward.exclusive("season_jutsu");
         default:
            return TierReward.EMPTY;
      }
   }

   public static class TierReward {
      public static final TierReward EMPTY = new TierReward(0, 0, 0, (String)null, 0, (String)null, 0, false, (String)null, (String)null, 0, 0);
      public final int ryo;
      public final int crystals;
      public final int jutsuXP;
      public final String crateId;
      public final int crateCount;
      public final String crateId2;
      public final int crateCount2;
      public final boolean isExclusive;
      public final String exclusiveType;
      public final String itemId;
      public final int itemMeta;
      public final int itemCount;

      public TierReward(int ryo, int crystals, int jutsuXP, String crateId, int crateCount, String crateId2, int crateCount2, boolean isExclusive, String exclusiveType) {
         this(ryo, crystals, jutsuXP, crateId, crateCount, crateId2, crateCount2, isExclusive, exclusiveType, (String)null, 0, 0);
      }

      public TierReward(int ryo, int crystals, int jutsuXP, String crateId, int crateCount, String crateId2, int crateCount2, boolean isExclusive, String exclusiveType, String itemId, int itemMeta, int itemCount) {
         this.ryo = ryo;
         this.crystals = crystals;
         this.jutsuXP = jutsuXP;
         this.crateId = crateId;
         this.crateCount = crateCount;
         this.crateId2 = crateId2;
         this.crateCount2 = crateCount2;
         this.isExclusive = isExclusive;
         this.exclusiveType = exclusiveType;
         this.itemId = itemId;
         this.itemMeta = itemMeta;
         this.itemCount = itemCount;
      }

      public boolean isEmpty() {
         return this == EMPTY;
      }

      public boolean hasCrate() {
         return this.crateId != null && this.crateCount > 0;
      }

      public boolean hasCrate2() {
         return this.crateId2 != null && this.crateCount2 > 0;
      }

      public boolean hasItem() {
         return this.itemId != null && !this.itemId.isEmpty() && this.itemCount > 0;
      }

      static TierReward ryo(int ryo) {
         return new TierReward(ryo, 0, 0, (String)null, 0, (String)null, 0, false, (String)null);
      }

      static TierReward crystals(int crystals) {
         return new TierReward(0, crystals, 0, (String)null, 0, (String)null, 0, false, (String)null);
      }

      public static TierReward jutsuXP(int xp) {
         return new TierReward(0, 0, xp, (String)null, 0, (String)null, 0, false, (String)null);
      }

      static TierReward item(String desc) {
         return new TierReward(0, 0, 0, (String)null, 0, (String)null, 0, false, desc);
      }

      static TierReward itemReward(String itemId, int meta, int count, String displayDesc) {
         return new TierReward(0, 0, 0, (String)null, 0, (String)null, 0, false, displayDesc, itemId, meta, count);
      }

      static TierReward crate(String crateId, int count, int ryo) {
         return new TierReward(ryo, 0, 0, crateId, count, (String)null, 0, false, (String)null);
      }

      static TierReward ryoAndCrate(int ryo, String crateId, int count) {
         return new TierReward(ryo, 0, 0, crateId, count, (String)null, 0, false, (String)null);
      }

      static TierReward crystalsAndCrate(int crystals, String crateId, int count) {
         return new TierReward(0, crystals, 0, crateId, count, (String)null, 0, false, (String)null);
      }

      static TierReward crystalsAndRyo(int crystals, int ryo) {
         return new TierReward(ryo, crystals, 0, (String)null, 0, (String)null, 0, false, (String)null);
      }

      static TierReward crystalsAndExclusive(int crystals, boolean isExcl, String exclType) {
         return new TierReward(0, crystals, 0, (String)null, 0, (String)null, 0, true, exclType);
      }

      static TierReward jutsuXPAndRyo(int xp, int ryo) {
         return new TierReward(ryo, 0, xp, (String)null, 0, (String)null, 0, false, (String)null);
      }

      static TierReward ryoAndJutsuXP(int ryo, int xp) {
         return new TierReward(ryo, 0, xp, (String)null, 0, (String)null, 0, false, (String)null);
      }

      static TierReward jutsuXPAndCrate(int xp, String crateId, int count) {
         return new TierReward(0, 0, xp, crateId, count, (String)null, 0, false, (String)null);
      }

      static TierReward doubleCrate(String crate1, int count1, String crate2, int count2) {
         return new TierReward(0, 0, 0, crate1, count1, crate2, count2, false, (String)null);
      }

      static TierReward exclusive(String type) {
         return new TierReward(0, 0, 0, (String)null, 0, (String)null, 0, true, type);
      }

      public String getDisplayString() {
         StringBuilder sb = new StringBuilder();
         if (this.ryo > 0) {
            sb.append(String.format("%,d Ryo", this.ryo));
         }

         if (this.crystals > 0) {
            if (sb.length() > 0) {
               sb.append(" + ");
            }

            sb.append(this.crystals).append(" Crystals");
         }

         if (this.jutsuXP > 0) {
            if (sb.length() > 0) {
               sb.append(" + ");
            }

            sb.append(String.format("%,d Jutsu XP", this.jutsuXP));
         }

         if (this.hasCrate()) {
            if (sb.length() > 0) {
               sb.append(" + ");
            }

            sb.append(this.crateCount).append("x ").append(this.crateId);
         }

         if (this.hasCrate2()) {
            if (sb.length() > 0) {
               sb.append(" + ");
            }

            sb.append(this.crateCount2).append("x ").append(this.crateId2);
         }

         if (this.isExclusive) {
            if (sb.length() > 0) {
               sb.append(" + ");
            }

            if ("kill_effect".equals(this.exclusiveType)) {
               sb.append("EXCLUSIVE Kill Effect");
            } else if ("season_jutsu".equals(this.exclusiveType)) {
               sb.append("EXCLUSIVE Season Jutsu");
            } else {
               sb.append("EXCLUSIVE");
            }
         }

         if (this.exclusiveType != null && !this.isExclusive) {
            if (sb.length() > 0) {
               sb.append(" + ");
            }

            sb.append(this.exclusiveType);
         }

         return sb.length() > 0 ? sb.toString() : "---";
      }
   }
}
