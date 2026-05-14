package net.luck.narutoaddon.OtherCode.stat.core;

public final class StatConstants {
   public static final int SP_CAP = 120;
   public static final int NATURE_MASTERY_MAX_LEVEL = 10;
   public static final int[] NATURE_MASTERY_COSTS = new int[]{1, 2, 2, 3, 3, 4, 4, 5, 5, 6};
   public static final double NATURE_MASTERY_DAMAGE_PER_LEVEL = 0.025;
   public static final double NATURE_MASTERY_CHARGE_PER_LEVEL = 0.014;
   public static final int KG_MASTERY_MAX_LEVEL = 7;
   public static final int[] KG_MASTERY_COSTS = new int[]{2, 3, 4, 5, 6, 8, 14};
   public static final double KG_MASTERY_DAMAGE_PER_LEVEL = 0.04;
   public static final double KG_MASTERY_CHARGE_PER_LEVEL = 0.021;
   public static final int NATURE_DEFENSE_MAX_LEVEL = 8;
   public static final int[] NATURE_DEFENSE_COSTS = new int[]{1, 1, 2, 2, 3, 3, 5, 8};
   public static final double NATURE_DEFENSE_REDUCTION_PER_LEVEL = 0.0175;
   public static final int KG_DEFENSE_MAX_LEVEL = 5;
   public static final int[] KG_DEFENSE_COSTS = new int[]{2, 3, 4, 6, 8};
   public static final double KG_DEFENSE_REDUCTION_PER_LEVEL = 0.02;
   public static final double ELEMENTAL_ADVANTAGE_BONUS = 0.08;
   public static final double ELEMENTAL_DISADVANTAGE_PENALTY = 0.05;
   public static final double RAID_BOSS_DEFENSE_BYPASS = (double)0.5F;
   public static final int RESPEC_RYO_COST = 5000;
   public static final long RESPEC_COOLDOWN_MS = 604800000L;
   public static final int SP_FROM_B_RANK = 2;
   public static final int SP_FROM_A_RANK = 4;
   public static final int SP_FROM_S_RANK = 7;
   public static final int SP_FROM_S_PLUS_RANK = 10;
   public static final float DAMAGE_FLOOR = 1.0F;

   private StatConstants() {
   }

   public static int getMaxLevel(StatCategory category) {
      switch (category) {
         case NATURE_OFFENSE:
            return 10;
         case KG_OFFENSE:
            return 7;
         case NATURE_DEFENSE:
            return 8;
         case KG_DEFENSE:
            return 5;
         default:
            return 0;
      }
   }

   public static int[] getCosts(StatCategory category) {
      switch (category) {
         case NATURE_OFFENSE:
            return NATURE_MASTERY_COSTS;
         case KG_OFFENSE:
            return KG_MASTERY_COSTS;
         case NATURE_DEFENSE:
            return NATURE_DEFENSE_COSTS;
         case KG_DEFENSE:
            return KG_DEFENSE_COSTS;
         default:
            return new int[0];
      }
   }

   public static int getCostForNextLevel(StatCategory category, int currentLevel) {
      int[] costs = getCosts(category);
      return currentLevel >= 0 && currentLevel < costs.length ? costs[currentLevel] : -1;
   }

   public static int getTotalCostForLevel(StatCategory category, int level) {
      int[] costs = getCosts(category);
      int total = 0;

      for(int i = 0; i < level && i < costs.length; ++i) {
         total += costs[i];
      }

      return total;
   }
}
