
package net.luck.narutoaddon.OtherCode.raid.boss.mechanics;

import net.luck.narutoaddon.OtherCode.raid.boss.BossMechanic;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

public class AOEMechanic implements BossMechanic {
   protected final String name;
   protected final String displayName;
   protected final float baseDamage;
   protected final int radius;
   protected final int warningTicks;
   protected final int durationTicks;
   protected final int damageInterval;
   protected BlockPos center;

   public AOEMechanic(String name, String displayName, float baseDamage, int radius, int warningTicks, int durationTicks, int damageInterval) {
      this.name = name;
      this.displayName = displayName;
      this.baseDamage = baseDamage;
      this.radius = radius;
      this.warningTicks = warningTicks;
      this.durationTicks = durationTicks;
      this.damageInterval = damageInterval;
   }

   public String getName() {
      return this.name;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getWarningMessage() {
      return "Preparing " + this.displayName + "!";
   }

   public int getWarningTicks() {
      return this.warningTicks;
   }

   public int getDurationTicks() {
      return this.durationTicks;
   }

   public void onStart(EntityLivingBase boss, RaidInstance raid) {
      this.center = boss.getPosition();
      boss.world.playSound((EntityPlayer)null, this.center, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 2.0F, 1.2F);
      this.spawnWarningParticles(boss);
   }

   public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
      if (raid != null) {
         if (ticksElapsed % 5 == 0) {
            this.spawnDamageParticles(boss);
         }

         boolean shouldDamage = this.damageInterval == 0 && ticksElapsed == 0 || this.damageInterval > 0 && ticksElapsed % this.damageInterval == 0;
         if (shouldDamage) {
            int enrageLevel = raid.getEnrageLevel();
            float damage = this.baseDamage * this.getEnrageDamageMultiplier(enrageLevel);

            for(EntityPlayer player : raid.getParticipants()) {
               if (this.isInRange(player.getPosition())) {
                  player.attackEntityFrom(DamageSource.MAGIC, damage);
               }
            }
         }

      }
   }

   public void onEnd(EntityLivingBase boss, RaidInstance raid) {
      boss.world.playSound((EntityPlayer)null, this.center, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0F, 1.0F);
   }

   protected boolean isInRange(BlockPos pos) {
      if (this.center == null) {
         return false;
      } else {
         double distSq = this.center.distanceSq(pos);
         return distSq <= (double)(this.radius * this.radius);
      }
   }

   protected void spawnWarningParticles(EntityLivingBase boss) {
      if (boss.world instanceof WorldServer) {
         WorldServer world = (WorldServer)boss.world;

         for(int angle = 0; angle < 360; angle += 10) {
            double rad = Math.toRadians((double)angle);
            double x = (double)this.center.getX() + (double)0.5F + (double)this.radius * Math.cos(rad);
            double z = (double)this.center.getZ() + (double)0.5F + (double)this.radius * Math.sin(rad);
            world.spawnParticle(EnumParticleTypes.FLAME, x, (double)this.center.getY() + (double)0.5F, z, 1, (double)0.0F, 0.1, (double)0.0F, (double)0.0F, new int[0]);
         }
      }

   }

   protected void spawnDamageParticles(EntityLivingBase boss) {
      if (boss.world instanceof WorldServer) {
         WorldServer world = (WorldServer)boss.world;

         for(int i = 0; i < 20; ++i) {
            double x = (double)this.center.getX() + (double)0.5F + (boss.world.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)this.radius;
            double z = (double)this.center.getZ() + (double)0.5F + (boss.world.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)this.radius;
            world.spawnParticle(EnumParticleTypes.FLAME, x, (double)(this.center.getY() + 1), z, 1, 0.1, 0.2, 0.1, 0.02, new int[0]);
         }
      }

   }

   public static class Builder {
      private String name = "aoe_attack";
      private String displayName = "AOE Attack";
      private float baseDamage = 8.0F;
      private int radius = 10;
      private int warningTicks = 60;
      private int durationTicks = 40;
      private int damageInterval = 0;

      public Builder name(String name) {
         this.name = name;
         return this;
      }

      public Builder displayName(String displayName) {
         this.displayName = displayName;
         return this;
      }

      public Builder damage(float damage) {
         this.baseDamage = damage;
         return this;
      }

      public Builder radius(int radius) {
         this.radius = radius;
         return this;
      }

      public Builder warningTicks(int ticks) {
         this.warningTicks = ticks;
         return this;
      }

      public Builder durationTicks(int ticks) {
         this.durationTicks = ticks;
         return this;
      }

      public Builder damageInterval(int ticks) {
         this.damageInterval = ticks;
         return this;
      }

      public AOEMechanic build() {
         return new AOEMechanic(this.name, this.displayName, this.baseDamage, this.radius, this.warningTicks, this.durationTicks, this.damageInterval);
      }
   }
}
