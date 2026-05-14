package net.luck.narutoaddon.OtherCode.shop.network;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class BattlePassClientData {
   public static int seasonNumber = 0;
   public static String seasonName = "";
   public static long seasonEndTime = 0L;
   public static int playerTier = 0;
   public static int playerXP = 0;
   public static int purchasedTier = 0;
   public static Set<Integer> claimedTiers = new HashSet();
   public static String activeKillEffect = "";
   public static Set<String> ownedKillEffects = new HashSet();
   public static int otsutsukiSkipsRemaining = 0;

   public static boolean hasPurchasedPass() {
      return purchasedTier > 0;
   }

   public static boolean isKagePass() {
      return purchasedTier >= 1;
   }

   public static boolean isOtsutsukiPass() {
      return purchasedTier >= 2;
   }

   public static boolean hasClaimed(int tier) {
      return claimedTiers.contains(tier);
   }

   public static boolean canClaim(int tier) {
      return tier <= playerTier && !claimedTiers.contains(tier);
   }

   public static long getTimeRemainingMs() {
      long remaining = seasonEndTime - System.currentTimeMillis();
      return remaining > 0L ? remaining : 0L;
   }

   public static void clear() {
      seasonNumber = 0;
      seasonName = "";
      seasonEndTime = 0L;
      playerTier = 0;
      playerXP = 0;
      purchasedTier = 0;
      claimedTiers = new HashSet();
      activeKillEffect = "";
      ownedKillEffects = new HashSet();
      otsutsukiSkipsRemaining = 0;
   }
}
