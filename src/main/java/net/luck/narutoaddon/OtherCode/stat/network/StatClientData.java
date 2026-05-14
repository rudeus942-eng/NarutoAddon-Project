package net.luck.narutoaddon.OtherCode.stat.network;

import net.luck.narutoaddon.OtherCode.quest.gui.QuestLogGui;
import net.luck.narutoaddon.OtherCode.stat.core.PlayerStatData;
import net.luck.narutoaddon.OtherCode.stat.core.StatCategory;
import net.luck.narutoaddon.OtherCode.stat.core.StatConstants;
import net.luck.narutoaddon.OtherCode.stat.core.StatElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumMap;

@SideOnly(Side.CLIENT)
public final class StatClientData {
   private static boolean statsUnlocked = false;
   private static int spEarned = 0;
   private static int spSpent = 0;
   private static int respecTokens = 0;
   private static long lastRespecTime = 0L;
   private static int ceCharges = 0;
   private static int ceCap = 0;
   private static PlayerStatData localData = new PlayerStatData();

   private StatClientData() {
   }

   public static void receive(StatSyncMessage msg) {
      statsUnlocked = msg.isStatsUnlocked();
      spEarned = msg.getSpEarned();
      spSpent = msg.getSpSpent();
      respecTokens = msg.getRespecTokens();
      lastRespecTime = msg.getLastRespecTime();
      ceCharges = msg.getCeCharges();
      ceCap = msg.getCeCap();
      localData = msg.toPlayerData();
      QuestLogGui.chakraEnhancementCharges = ceCharges;
      QuestLogGui.chakraEnhancementCap = ceCap;
   }

   public static boolean isStatsUnlocked() {
      return statsUnlocked;
   }

   public static int getSpEarned() {
      return spEarned;
   }

   public static int getSpSpent() {
      return spSpent;
   }

   public static int getAvailableSP() {
      return spEarned - spSpent;
   }

   public static int getRespecTokens() {
      return respecTokens;
   }

   public static long getLastRespecTime() {
      return lastRespecTime;
   }

   public static int getCeCharges() {
      return ceCharges;
   }

   public static int getCeCap() {
      return ceCap;
   }

   public static int getLevel(StatCategory category, StatElement element) {
      return localData.getLevel(category, element);
   }

   public static double getOffenseBonus(StatElement element) {
      return localData.getOffenseBonus(element);
   }

   public static double getDefenseReduction(StatElement element) {
      return localData.getDefenseReduction(element);
   }

   public static int getNextLevelCost(StatCategory category, StatElement element) {
      int currentLevel = getLevel(category, element);
      return StatConstants.getCostForNextLevel(category, currentLevel);
   }

   public static boolean canAllocate(StatCategory category, StatElement element) {
      int cost = getNextLevelCost(category, element);
      return cost > 0 && getAvailableSP() >= cost;
   }

   public static boolean isRespecAvailable() {
      if (respecTokens <= 0) {
         return false;
      } else {
         long elapsed = System.currentTimeMillis() - lastRespecTime;
         return elapsed >= 604800000L;
      }
   }

   public static long getRespecCooldownRemaining() {
      long elapsed = System.currentTimeMillis() - lastRespecTime;
      long remaining = 604800000L - elapsed;
      return Math.max(0L, remaining);
   }

   public static EnumMap<StatElement, Integer> getAllocations(StatCategory category) {
      return localData.getAllocationsForCategory(category);
   }

   public static void reset() {
      statsUnlocked = false;
      spEarned = 0;
      spSpent = 0;
      respecTokens = 0;
      lastRespecTime = 0L;
      ceCharges = 0;
      ceCap = 0;
      localData = new PlayerStatData();
   }
}
