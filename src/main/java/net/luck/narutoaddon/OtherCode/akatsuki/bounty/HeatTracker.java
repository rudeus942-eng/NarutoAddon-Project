package net.luck.narutoaddon.OtherCode.akatsuki.bounty;

import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiMember;

public class HeatTracker {
   private HeatTracker() {
   }

   public static void addHeat(AkatsukiMember member, String village, int amount) {
      member.addHeat(village, amount);
   }

   public static void decayAllHeat(AkatsukiMember member, int amount) {
      member.decayHeat(amount);
   }

   public static int getHeat(AkatsukiMember member, String village) {
      return member.getHeat(village);
   }

   public static String getHeatLevel(AkatsukiMember member, String village) {
      int heat = member.getHeat(village);
      if (heat < 25) {
         return "LOW";
      } else if (heat < 50) {
         return "MEDIUM";
      } else {
         return heat < 80 ? "HIGH" : "CRITICAL";
      }
   }

   public static boolean shouldSpawnHunters(AkatsukiMember member, String village) {
      return member.getHeat(village) >= 80;
   }
}
