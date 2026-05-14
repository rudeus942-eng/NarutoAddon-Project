package net.luck.narutoaddon.OtherCode.endgame.outpost;

import java.util.Random;

public enum OutpostDifficultyTier {
   CHUNIN("Chunin", (double)1.0F, (double)1.0F, 1200, 1800, "chunin"),
   JONIN("Jonin", (double)1.75F, (double)1.5F, 2250, 3300, "jonin"),
   ANBU("ANBU", (double)3.0F, 2.2, 3750, 5250, "anbu");

   private final String displayName;
   private final double hpMultiplier;
   private final double dmgMultiplier;
   private final int ryoMin;
   private final int ryoMax;
   private final String configSuffix;

   private OutpostDifficultyTier(String displayName, double hpMultiplier, double dmgMultiplier, int ryoMin, int ryoMax, String configSuffix) {
      this.displayName = displayName;
      this.hpMultiplier = hpMultiplier;
      this.dmgMultiplier = dmgMultiplier;
      this.ryoMin = ryoMin;
      this.ryoMax = ryoMax;
      this.configSuffix = configSuffix;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public double getHpMultiplier() {
      return this.hpMultiplier;
   }

   public double getDmgMultiplier() {
      return this.dmgMultiplier;
   }

   public int getRyoMin() {
      return this.ryoMin;
   }

   public int getRyoMax() {
      return this.ryoMax;
   }

   public String getConfigSuffix() {
      return this.configSuffix;
   }

   public int getRandomRyo(Random rand) {
      return this.ryoMin + rand.nextInt(this.ryoMax - this.ryoMin + 1);
   }
}
