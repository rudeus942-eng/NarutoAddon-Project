package net.luck.narutoaddon.OtherCode.raid.core;

import net.minecraft.util.text.TextFormatting;

public enum RaidDifficulty {
   GENIN("Genin", 1.0F, 1.0F, 3, 5000, 0, 6, TextFormatting.GRAY),
   CHUNIN("Chunin", 1.5F, 1.2F, 4, 15000, 0, 6, TextFormatting.GREEN),
   JONIN("Jonin", 2.5F, 1.5F, 5, 30000, 0, 6, TextFormatting.GOLD),
   ANBU("Anbu", 1.5F, 1.2F, 4, 50000, 70000, 1, TextFormatting.RED);

   private final String displayName;
   private final float healthMultiplier;
   private final float damageMultiplier;
   private final int maxPhases;
   private final int baseRyoReward;
   private final int entryCost;
   private final int defaultPartySize;
   private final TextFormatting color;
   public static final long ENRAGE_1_TIME = 600000L;
   public static final long ENRAGE_2_TIME = 900000L;
   public static final long HARD_WIPE_TIME = 1050000L;
   public static final long ANBU_WIPE_TIME = 1200000L;
   public static final int DEFAULT_REQUIRED_PARTY_SIZE = 6;
   private static int testingMinPartySize = -1;
   public static final float ENRAGE_1_DAMAGE_MULT = 1.15F;
   public static final float ENRAGE_1_SPEED_MULT = 1.1F;
   public static final float ENRAGE_2_DAMAGE_MULT = 1.3F;
   public static final float ENRAGE_2_SPEED_MULT = 1.2F;

   private RaidDifficulty(String displayName, float healthMultiplier, float damageMultiplier, int maxPhases, int baseRyoReward, int entryCost, int defaultPartySize, TextFormatting color) {
      this.displayName = displayName;
      this.healthMultiplier = healthMultiplier;
      this.damageMultiplier = damageMultiplier;
      this.maxPhases = maxPhases;
      this.baseRyoReward = baseRyoReward;
      this.entryCost = entryCost;
      this.defaultPartySize = defaultPartySize;
      this.color = color;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public float getHealthMultiplier() {
      return this.healthMultiplier;
   }

   public float getDamageMultiplier() {
      return this.damageMultiplier;
   }

   public int getMaxPhases() {
      return this.maxPhases;
   }

   public int getBaseRyoReward() {
      return this.baseRyoReward;
   }

   public TextFormatting getColor() {
      return this.color;
   }

   public String getColoredName() {
      return this.color + this.displayName;
   }

   public int getEntryCost() {
      return this.entryCost;
   }

   public int getCloneCount() {
      switch (this) {
         case GENIN:
            return 3;
         case CHUNIN:
            return 4;
         case JONIN:
            return 6;
         case ANBU:
            return 4;
         default:
            return 3;
      }
   }

   public int getWarningTimeReduction() {
      switch (this) {
         case GENIN:
            return 0;
         case CHUNIN:
            return 20;
         case JONIN:
            return 40;
         case ANBU:
            return 20;
         default:
            return 0;
      }
   }

   public int getMinPartySize() {
      return testingMinPartySize > 0 ? testingMinPartySize : this.defaultPartySize;
   }

   public static void setTestingMinPartySize(int size) {
      if (size >= 1 && size <= 6) {
         testingMinPartySize = size;
      } else {
         testingMinPartySize = -1;
      }

   }

   public static int getTestingMinPartySize() {
      return testingMinPartySize;
   }

   public static boolean isTestingModeActive() {
      return testingMinPartySize > 0;
   }

   public static RaidDifficulty fromString(String name) {
      for(RaidDifficulty diff : values()) {
         if (diff.name().equalsIgnoreCase(name) || diff.displayName.equalsIgnoreCase(name)) {
            return diff;
         }
      }

      return null;
   }

   public static RaidDifficulty fromOrdinal(int ordinal) {
      return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : GENIN;
   }
}
