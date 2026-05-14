package net.luck.narutoaddon.OtherCode;

public class Rankedelocore {
   public static final int DEFAULT_ELO = 1200;
   public static final int MIN_ELO = 100;
   public static final int ELO_SCALE_FACTOR = 400;
   public static final int K_PLACEMENT = 60;
   public static final int K_LEARNING = 50;
   public static final int K_ESTABLISHED = 42;
   public static final int K_TOP_PLAYER = 34;
   public static final int DEFAULT_K = 42;
   public static final int PLACEMENT_GAMES = 10;
   public static final int LEARNING_GAMES = 30;
   public static final int TOP_PLAYER_THRESHOLD = 1800;

   public static double calculateExpectedScore(int playerRating, int opponentRating) {
      return (double)1.0F / ((double)1.0F + Math.pow((double)10.0F, (double)(opponentRating - playerRating) / (double)400.0F));
   }

   public static double calculateNewRating(int currentRating, int kFactor, int actualScore, double expectedScore) {
      return (double)currentRating + (double)kFactor * ((double)actualScore - expectedScore);
   }

   public static int getKFactor(int gamesPlayed, int currentRating) {
      if (currentRating >= 1800) {
         return 34;
      } else if (gamesPlayed <= 10) {
         return 60;
      } else {
         return gamesPlayed <= 30 ? 50 : 42;
      }
   }

   public static double getStreakMultiplier(int currentWinStreak) {
      if (currentWinStreak >= 7) {
         return (double)1.5F;
      } else if (currentWinStreak >= 5) {
         return 1.35;
      } else {
         return currentWinStreak >= 3 ? 1.2 : (double)1.0F;
      }
   }

   public static int clampElo(double rating) {
      return rating < (double)100.0F ? 100 : (int)Math.round(rating);
   }

   public static PotentialEloChange calculatePotentialEloChange(int playerElo, int opponentElo, int playerGamesPlayed) {
      double expected = calculateExpectedScore(playerElo, opponentElo);
      int kFactor = getKFactor(playerGamesPlayed, playerElo);
      int eloIfWin = clampElo(calculateNewRating(playerElo, kFactor, 1, expected));
      int eloIfLoss = clampElo(calculateNewRating(playerElo, kFactor, 0, expected));
      return new PotentialEloChange(playerElo, eloIfWin - playerElo, playerElo - eloIfLoss, (int)Math.round(expected * (double)100.0F), kFactor);
   }

   public static RankTier getRankTier(int elo) {
      if (elo >= 2400) {
         return new RankTier("Otsutsuki", 10, "§4");
      } else if (elo >= 2100) {
         return new RankTier("Kage", 9, "§c");
      } else if (elo >= 1800) {
         return new RankTier("ANBU", 8, "§5");
      } else if (elo >= 1600) {
         return new RankTier("Elite Jonin", 7, "§b");
      } else if (elo >= 1400) {
         return new RankTier("S. Jonin", 6, "§e");
      } else if (elo >= 1200) {
         return new RankTier("Jonin", 5, "§6");
      } else if (elo >= 1000) {
         return new RankTier("Chunin", 4, "§a");
      } else {
         return elo >= 800 ? new RankTier("Genin", 2, "§7") : new RankTier("Academy Student", 1, "§f");
      }
   }

   public static boolean isInPlacement(int gamesPlayed) {
      return gamesPlayed < 10;
   }

   public static String getPlacementProgress(int gamesPlayed) {
      return gamesPlayed >= 10 ? null : gamesPlayed + "/" + 10 + " Placements";
   }

   public static String formatEloChange(int change) {
      return change >= 0 ? "+" + change : String.valueOf(change);
   }

   public static String getEloChangeColor(int change) {
      if (change > 0) {
         return "§a";
      } else {
         return change < 0 ? "§c" : "§7";
      }
   }

   public static class MatchResult {
      public String winnerUUID;
      public String loserUUID;
      public int winnerOldElo;
      public int winnerNewElo;
      public int winnerEloChange;
      public int winnerKFactor;
      public double winnerExpectedScore;
      public int winnerGamesPlayed;
      public boolean winnerIsPlacement;
      public int loserOldElo;
      public int loserNewElo;
      public int loserEloChange;
      public int loserKFactor;
      public double loserExpectedScore;
      public int loserGamesPlayed;
      public boolean loserIsPlacement;
      public long timestamp;
      public int eloSwing;
   }

   public static class PotentialEloChange {
      public int currentElo;
      public int potentialGain;
      public int potentialLoss;
      public int expectedWinChance;
      public int kFactor;

      public PotentialEloChange(int currentElo, int potentialGain, int potentialLoss, int expectedWinChance, int kFactor) {
         this.currentElo = currentElo;
         this.potentialGain = potentialGain;
         this.potentialLoss = potentialLoss;
         this.expectedWinChance = expectedWinChance;
         this.kFactor = kFactor;
      }
   }

   public static class RankTier {
      public String name;
      public int tier;
      public String color;

      public RankTier(String name, int tier, String color) {
         this.name = name;
         this.tier = tier;
         this.color = color;
      }
   }
}
