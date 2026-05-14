package net.luck.narutoaddon.OtherCode;

import java.util.ArrayList;
import java.util.List;

public class Rankedtiers {
   public static final int DIVISION_SIZE = 25;
   public static final int DIVISIONS_PER_TIER = 4;
   public static final int TIER_SIZE = 100;
   public static final int LP_PER_DIVISION = 100;
   public static final String[] DIVISION_NAMES = new String[]{"IV", "III", "II", "I"};
   private static final List<Tier> TIERS = new ArrayList();

   public static RankInfo eloToRank(int elo) {
      RankInfo info = new RankInfo();
      if (elo < 800) {
         Tier genin = (Tier)TIERS.get(0);
         info.tier = genin;
         info.division = "IV";
         info.divisionIndex = 0;
         info.lp = 0;
         info.fullName = "Genin IV";
         info.color = genin.color;
         info.colorCode = genin.colorCode;
         info.eloInTier = 0;
         info.eloInDivision = 0;
         info.promotionThreshold = 825;
         info.demotionThreshold = 800;
         info.atPromotion = false;
         info.atDemotion = true;
         return info;
      } else {
         Tier matchedTier = null;

         for(Tier tier : TIERS) {
            if (elo >= tier.minElo && elo <= tier.maxElo) {
               matchedTier = tier;
               break;
            }
         }

         if (matchedTier == null) {
            matchedTier = (Tier)TIERS.get(TIERS.size() - 1);
         }

         info.tier = matchedTier;
         info.color = matchedTier.color;
         info.colorCode = matchedTier.colorCode;
         if (matchedTier.hasDivisions) {
            int eloInTier = elo - matchedTier.minElo;
            int tierRange = matchedTier.maxElo - matchedTier.minElo + 1;
            int divisionSize = tierRange / 4;
            int divisionIndex = Math.min(eloInTier / divisionSize, 3);
            int eloInDivision = eloInTier % divisionSize;
            int lp = eloInDivision * 100 / divisionSize;
            info.divisionIndex = divisionIndex;
            info.division = DIVISION_NAMES[divisionIndex];
            info.lp = lp;
            info.fullName = matchedTier.name + " " + info.division;
            info.eloInTier = eloInTier;
            info.eloInDivision = eloInDivision;
            int divisionMinElo = matchedTier.minElo + divisionIndex * divisionSize;
            int divisionMaxElo = divisionMinElo + divisionSize - 1;
            info.promotionThreshold = divisionMaxElo + 1;
            info.demotionThreshold = divisionMinElo;
            info.atPromotion = lp >= 100;
            info.atDemotion = lp <= 0 && divisionIndex > 0;
         } else {
            info.division = null;
            info.divisionIndex = 0;
            info.lp = 0;
            info.fullName = matchedTier.name;
            info.eloInTier = elo - matchedTier.minElo;
            info.eloInDivision = 0;
            info.promotionThreshold = Integer.MAX_VALUE;
            info.demotionThreshold = matchedTier.minElo;
            info.atPromotion = false;
            info.atDemotion = false;
         }

         return info;
      }
   }

   public static Tier getTierByName(String name) {
      for(Tier tier : TIERS) {
         if (tier.name.equalsIgnoreCase(name)) {
            return tier;
         }
      }

      return null;
   }

   public static List<Tier> getAllTiers() {
      return new ArrayList(TIERS);
   }

   public static boolean isOtsutsukiEligible(int elo) {
      return elo >= 2400;
   }

   public static String getEloColor(int elo) {
      RankInfo info = eloToRank(elo);
      return info.color;
   }

   public static String formatRankDisplay(int elo) {
      RankInfo info = eloToRank(elo);
      String display = info.color + info.fullName;
      if (info.tier.hasDivisions) {
         display = display + " §7(" + info.lp + " LP)";
      }

      return display;
   }

   public static String getShortRankName(int elo) {
      RankInfo info = eloToRank(elo);
      return info.tier.name;
   }

   public static int calculateLP(int elo) {
      RankInfo info = eloToRank(elo);
      return info.lp;
   }

   public static boolean wouldPromote(int currentElo, int newElo) {
      RankInfo current = eloToRank(currentElo);
      RankInfo newRank = eloToRank(newElo);
      if (!current.tier.name.equals(newRank.tier.name)) {
         return newRank.tier.index > current.tier.index;
      } else if (current.tier.hasDivisions) {
         return newRank.divisionIndex > current.divisionIndex;
      } else {
         return false;
      }
   }

   public static boolean wouldDemote(int currentElo, int newElo) {
      RankInfo current = eloToRank(currentElo);
      RankInfo newRank = eloToRank(newElo);
      if (!current.tier.name.equals(newRank.tier.name)) {
         return newRank.tier.index < current.tier.index;
      } else if (current.tier.hasDivisions) {
         return newRank.divisionIndex < current.divisionIndex;
      } else {
         return false;
      }
   }

   static {
      TIERS.add(new Tier("Genin", 800, 999, true, "§7", "&7", "Beginner ninja rank", 0));
      TIERS.add(new Tier("Chunin", 1000, 1199, true, "§a", "&a", "Intermediate ninja rank", 1));
      TIERS.add(new Tier("Jonin", 1200, 1399, true, "§6", "&6", "Elite ninja commander", 2));
      TIERS.add(new Tier("S. Jonin", 1400, 1599, true, "§e", "&e", "Specialized elite ninja", 3));
      TIERS.add(new Tier("Elite Jonin", 1600, 1799, true, "§b", "&b", "Elite among the elite", 4));
      TIERS.add(new Tier("ANBU", 1800, 2099, true, "§5", "&5", "Special ops ninja", 5));
      TIERS.add(new Tier("Kage", 2100, 2399, true, "§c", "&c", "Village leader level", 6));
      TIERS.add(new Tier("Otsutsuki", 2400, 9999, false, "§4", "&4", "Top 10 - Godlike power", 7));
   }

   public static class Tier {
      public String name;
      public int minElo;
      public int maxElo;
      public boolean hasDivisions;
      public String color;
      public String colorCode;
      public String description;
      public int index;

      public Tier(String name, int minElo, int maxElo, boolean hasDivisions, String color, String colorCode, String description, int index) {
         this.name = name;
         this.minElo = minElo;
         this.maxElo = maxElo;
         this.hasDivisions = hasDivisions;
         this.color = color;
         this.colorCode = colorCode;
         this.description = description;
         this.index = index;
      }
   }

   public static class RankInfo {
      public Tier tier;
      public String division;
      public int divisionIndex;
      public int lp;
      public String fullName;
      public String color;
      public String colorCode;
      public int eloInTier;
      public int eloInDivision;
      public int promotionThreshold;
      public int demotionThreshold;
      public boolean atPromotion;
      public boolean atDemotion;

      public String getDisplayName() {
         return this.color + this.fullName;
      }
   }
}
