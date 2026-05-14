package net.luck.narutoaddon.OtherCode.raid.boss;

import java.util.ArrayList;
import java.util.List;

public class BossPhase {
   private final String name;
   private final PhaseType type;
   private final float healthThresholdPercent;
   private final List<BossMechanic> mechanics;
   private final int duration;
   private Runnable onStartCallback;
   private Runnable onEndCallback;
   private boolean bossImmune;
   private float damageMultiplier;
   private float speedMultiplier;

   private BossPhase(Builder builder) {
      this.bossImmune = false;
      this.damageMultiplier = 1.0F;
      this.speedMultiplier = 1.0F;
      this.name = builder.name;
      this.type = builder.type;
      this.healthThresholdPercent = builder.healthThresholdPercent;
      this.mechanics = new ArrayList(builder.mechanics);
      this.duration = builder.duration;
      this.onStartCallback = builder.onStartCallback;
      this.onEndCallback = builder.onEndCallback;
      this.bossImmune = builder.bossImmune;
      this.damageMultiplier = builder.damageMultiplier;
      this.speedMultiplier = builder.speedMultiplier;
   }

   public void onPhaseStart() {
      if (this.onStartCallback != null) {
         this.onStartCallback.run();
      }

   }

   public void onPhaseEnd() {
      if (this.onEndCallback != null) {
         this.onEndCallback.run();
      }

   }

   public String getName() {
      return this.name;
   }

   public PhaseType getType() {
      return this.type;
   }

   public float getHealthThresholdPercent() {
      return this.healthThresholdPercent;
   }

   public List<BossMechanic> getMechanics() {
      return this.mechanics;
   }

   public int getDuration() {
      return this.duration;
   }

   public boolean isBossImmune() {
      return this.bossImmune || this.type == PhaseType.IMMUNITY;
   }

   public float getDamageMultiplier() {
      return this.damageMultiplier;
   }

   public float getSpeedMultiplier() {
      return this.speedMultiplier;
   }

   public boolean hasDuration() {
      return this.duration > 0;
   }

   public static enum PhaseType {
      DAMAGE,
      IMMUNITY,
      PUZZLE,
      INTERMISSION;
   }

   public static class Builder {
      private String name = "Unnamed Phase";
      private PhaseType type;
      private float healthThresholdPercent;
      private List<BossMechanic> mechanics;
      private int duration;
      private Runnable onStartCallback;
      private Runnable onEndCallback;
      private boolean bossImmune;
      private float damageMultiplier;
      private float speedMultiplier;

      public Builder() {
         this.type = PhaseType.DAMAGE;
         this.healthThresholdPercent = 100.0F;
         this.mechanics = new ArrayList();
         this.duration = 0;
         this.bossImmune = false;
         this.damageMultiplier = 1.0F;
         this.speedMultiplier = 1.0F;
      }

      public Builder name(String name) {
         this.name = name;
         return this;
      }

      public Builder type(PhaseType type) {
         this.type = type;
         if (type == PhaseType.IMMUNITY) {
            this.bossImmune = true;
         }

         return this;
      }

      public Builder healthThreshold(float percent) {
         this.healthThresholdPercent = percent;
         return this;
      }

      public Builder addMechanic(BossMechanic mechanic) {
         this.mechanics.add(mechanic);
         return this;
      }

      public Builder mechanics(List<BossMechanic> mechanics) {
         this.mechanics.addAll(mechanics);
         return this;
      }

      public Builder duration(int ticks) {
         this.duration = ticks;
         return this;
      }

      public Builder onStart(Runnable callback) {
         this.onStartCallback = callback;
         return this;
      }

      public Builder onEnd(Runnable callback) {
         this.onEndCallback = callback;
         return this;
      }

      public Builder immune(boolean immune) {
         this.bossImmune = immune;
         return this;
      }

      public Builder damageMultiplier(float multiplier) {
         this.damageMultiplier = multiplier;
         return this;
      }

      public Builder speedMultiplier(float multiplier) {
         this.speedMultiplier = multiplier;
         return this;
      }

      public BossPhase build() {
         return new BossPhase(this);
      }
   }
}
