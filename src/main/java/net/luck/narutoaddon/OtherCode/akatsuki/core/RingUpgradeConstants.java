package net.luck.narutoaddon.OtherCode.akatsuki.core;

public class RingUpgradeConstants {
   public static final String[] UPGRADE_NAMES = new String[]{"Offense", "Defense", "Speed", "Chakra", "Versatility"};
   public static final String[] UPGRADE_DESCRIPTIONS = new String[]{"+3% damage per level", "-2% damage taken per level", "+2% movement speed per level", "+3% chakra regen per level", "+2% all stats per level"};
   public static final int[] BASE_TOKEN_COSTS = new int[]{50, 50, 75, 75, 100};
   public static final int MAX_LEVEL = 5;

   public static int getUpgradeCost(int slot, int currentLevel) {
      return slot >= 0 && slot < 5 && currentLevel < 5 ? BASE_TOKEN_COSTS[slot] * (currentLevel + 1) : Integer.MAX_VALUE;
   }
}
