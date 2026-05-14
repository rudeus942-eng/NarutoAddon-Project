package net.luck.narutoaddon.OtherCode.endgame;

public enum PveRank {
   GENIN("Genin", 0, "D"),
   CHUNIN("Chunin", 500, "C"),
   JONIN("Jonin", 2000, "B"),
   ANBU("ANBU", 5000, "A"),
   KAGE("Kage", 12000, "S");

   private final String displayName;
   private final int xpThreshold;
   private final String maxMissionRank;
   public static final int XP_OUTPOST_CHUNIN = 50;
   public static final int XP_OUTPOST_JONIN = 100;
   public static final int XP_OUTPOST_ANBU = 200;
   public static final int XP_BINGO_CHUNIN = 30;
   public static final int XP_BINGO_JONIN = 60;
   public static final int XP_BINGO_ANBU = 100;
   public static final int XP_BINGO_JACKPOT = 150;
   public static final int XP_INCURSION_WAVE = 15;
   public static final int XP_INCURSION_COMPLETE = 50;
   public static final int XP_DEFENSE_WAVE = 15;
   public static final int XP_DEFENSE_VICTORY_NORMAL = 50;
   public static final int XP_DEFENSE_VICTORY_HARD = 75;
   public static final int XP_DEFENSE_VICTORY_NIGHTMARE = 100;
   public static final int XP_MISSION_D = 10;
   public static final int XP_MISSION_C = 20;
   public static final int XP_MISSION_B = 35;
   public static final int XP_MISSION_A = 50;
   public static final int XP_MISSION_S = 75;

   private PveRank(String displayName, int xpThreshold, String maxMissionRank) {
      this.displayName = displayName;
      this.xpThreshold = xpThreshold;
      this.maxMissionRank = maxMissionRank;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public int getXpThreshold() {
      return this.xpThreshold;
   }

   public String getMaxMissionRank() {
      return this.maxMissionRank;
   }

   public PveRank getNextRank() {
      PveRank[] ranks = values();
      int next = this.ordinal() + 1;
      return next < ranks.length ? ranks[next] : null;
   }

   public int xpToNextRank(int currentXp) {
      PveRank next = this.getNextRank();
      return next == null ? -1 : Math.max(0, next.xpThreshold - currentXp);
   }

   public static PveRank fromXp(int xp) {
      PveRank[] ranks = values();
      PveRank result = GENIN;

      for(PveRank rank : ranks) {
         if (xp < rank.xpThreshold) {
            break;
         }

         result = rank;
      }

      return result;
   }

   public static PveRank byName(String name) {
      for(PveRank rank : values()) {
         if (rank.name().equalsIgnoreCase(name) || rank.displayName.equalsIgnoreCase(name)) {
            return rank;
         }
      }

      return null;
   }

   public boolean canAccessMissionRank(String rankLetter) {
      if (rankLetter == null) {
         return true;
      } else {
         int myLevel = missionRankLevel(this.maxMissionRank);
         int targetLevel = missionRankLevel(rankLetter);
         return myLevel >= targetLevel;
      }
   }

   private static int missionRankLevel(String letter) {
      if (letter == null) {
         return 0;
      } else {
         switch (letter.toUpperCase()) {
            case "D":
               return 1;
            case "C":
               return 2;
            case "B":
               return 3;
            case "A":
               return 4;
            case "S":
               return 5;
            case "S+":
               return 6;
            default:
               return 0;
         }
      }
   }

   public static int getOutpostXp(int tierOrdinal) {
      switch (tierOrdinal) {
         case 0:
            return 50;
         case 1:
            return 100;
         case 2:
            return 200;
         default:
            return 50;
      }
   }

   public static int getBingoXp(int tierOrdinal) {
      switch (tierOrdinal) {
         case 0:
            return 30;
         case 1:
            return 60;
         case 2:
            return 100;
         default:
            return 30;
      }
   }

   public static int getDefenseVictoryXp(int difficultyOrdinal) {
      switch (difficultyOrdinal) {
         case 0:
            return 50;
         case 1:
            return 75;
         case 2:
            return 100;
         default:
            return 50;
      }
   }

   public static int getMissionXp(String rankLetter) {
      if (rankLetter == null) {
         return 10;
      } else {
         switch (rankLetter.toUpperCase()) {
            case "D":
               return 10;
            case "C":
               return 20;
            case "B":
               return 35;
            case "A":
               return 50;
            case "S":
            case "S+":
               return 75;
            default:
               return 10;
         }
      }
   }
}
