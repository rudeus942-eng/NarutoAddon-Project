
package net.luck.narutoaddon.OtherCode.shop.pass;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KillEffectRegistry {
   private static final Map<String, KillEffect> EFFECTS = new HashMap();

   private static void register(KillEffect effect) {
      EFFECTS.put(effect.getEffectId(), effect);
   }

   public static KillEffect getEffect(String id) {
      return id != null ? (KillEffect)EFFECTS.get(id) : null;
   }

   public static List<KillEffect> getAllEffects() {
      return new ArrayList(EFFECTS.values());
   }

   static {
      register(new KillEffect("s1_fire_burst", EnumParticleTypes.FLAME, 30, (double)1.0F, SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 0.8F, 1.0F, 0.5F, 0.0F));
      register(new KillEffect("s2_lightning_strike", EnumParticleTypes.CRIT_MAGIC, 40, 1.2, SoundEvents.ENTITY_LIGHTNING_THUNDER, 0.8F, 1.5F, 0.3F, 0.6F, 1.0F));
      register(new KillEffect("s3_shadow_dissolve", EnumParticleTypes.SMOKE_LARGE, 50, 1.2, SoundEvents.ENTITY_WITHER_SPAWN, 0.9F, 0.7F, 0.15F, 0.12F, 0.18F));
   }

   public static class KillEffect {
      private final String effectId;
      private final EnumParticleTypes particleType;
      private final int particleCount;
      private final double particleSpread;
      private final SoundEvent soundEvent;
      private final float soundVolume;
      private final float soundPitch;
      private final float colorR;
      private final float colorG;
      private final float colorB;

      public KillEffect(String effectId, EnumParticleTypes particleType, int particleCount, double particleSpread, SoundEvent soundEvent, float soundVolume, float soundPitch, float colorR, float colorG, float colorB) {
         this.effectId = effectId;
         this.particleType = particleType;
         this.particleCount = particleCount;
         this.particleSpread = particleSpread;
         this.soundEvent = soundEvent;
         this.soundVolume = soundVolume;
         this.soundPitch = soundPitch;
         this.colorR = colorR;
         this.colorG = colorG;
         this.colorB = colorB;
      }

      public String getEffectId() {
         return this.effectId;
      }

      public EnumParticleTypes getParticleType() {
         return this.particleType;
      }

      public int getParticleCount() {
         return this.particleCount;
      }

      public double getParticleSpread() {
         return this.particleSpread;
      }

      public SoundEvent getSoundEvent() {
         return this.soundEvent;
      }

      public float getSoundVolume() {
         return this.soundVolume;
      }

      public float getSoundPitch() {
         return this.soundPitch;
      }

      public float getColorR() {
         return this.colorR;
      }

      public float getColorG() {
         return this.colorG;
      }

      public float getColorB() {
         return this.colorB;
      }
   }
}
