
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SafeZoneMechanic implements BossMechanic {
   protected final String name;
   protected final String displayName;
   protected final float baseDamage;
   protected final int warningTicks;
   protected final int durationTicks;
   protected final int damageInterval;
   protected final int safeZoneRadius;
   protected List<BlockPos> safeZones = new ArrayList();
   protected final SafeZonePattern pattern;
   protected final int safeZoneCount;

   public SafeZoneMechanic(String name, String displayName, float baseDamage, int warningTicks, int durationTicks, int damageInterval, int safeZoneRadius, SafeZonePattern pattern, int safeZoneCount) {
      this.name = name;
      this.displayName = displayName;
      this.baseDamage = baseDamage;
      this.warningTicks = warningTicks;
      this.durationTicks = durationTicks;
      this.damageInterval = damageInterval;
      this.safeZoneRadius = safeZoneRadius;
      this.pattern = pattern;
      this.safeZoneCount = safeZoneCount;
   }

   public String getName() {
      return this.name;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getWarningMessage() {
      return "Preparing " + this.displayName + "! Get to safety!";
   }

   public int getWarningTicks() {
      return this.warningTicks;
   }

   public int getDurationTicks() {
      return this.durationTicks;
   }

   public List<BlockPos> getSafeZones() {
      return Collections.unmodifiableList(this.safeZones);
   }

   public int getSafeZoneRadius() {
      return this.safeZoneRadius;
   }

   public void onStart(EntityLivingBase boss, RaidInstance raid) {
      this.safeZones.clear();
      this.calculateSafeZones(boss, raid);
      boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 3.0F, 0.5F);
      if (raid != null) {
         raid.broadcastMessage("§b§lSafe zones have appeared! Get inside!");
      }

   }

   protected void calculateSafeZones(EntityLivingBase boss, RaidInstance raid) {
      BlockPos bossPos = boss.getPosition();
      switch (this.pattern) {
         case BEHIND_BOSS:
            int behindDistance = 15;
            double angle = Math.toRadians((double)(boss.rotationYaw + 180.0F));
            int x = (int)((double)bossPos.getX() + (double)behindDistance * Math.sin(angle));
            int z = (int)((double)bossPos.getZ() + (double)behindDistance * Math.cos(angle));
            this.safeZones.add(new BlockPos(x, bossPos.getY(), z));
            break;
         case AROUND_BOSS:
            int aroundDistance = 20;

            for(int i = 0; i < this.safeZoneCount; ++i) {
               double a = Math.toRadians((double)360.0F / (double)this.safeZoneCount * (double)i);
               int ax = (int)((double)bossPos.getX() + (double)aroundDistance * Math.cos(a));
               int az = (int)((double)bossPos.getZ() + (double)aroundDistance * Math.sin(a));
               this.safeZones.add(new BlockPos(ax, bossPos.getY(), az));
            }
            break;
         case RANDOM:
            for(int i = 0; i < this.safeZoneCount; ++i) {
               int rx = bossPos.getX() + boss.world.rand.nextInt(40) - 20;
               int rz = bossPos.getZ() + boss.world.rand.nextInt(40) - 20;
               this.safeZones.add(new BlockPos(rx, bossPos.getY(), rz));
            }
            break;
         case FIXED_POSITIONS:
         default:
            this.safeZones.add(bossPos.add(0, 0, 15));
      }

   }

   public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
      if (raid != null) {
         if (ticksElapsed % 5 == 0) {
            this.spawnSafeZoneParticles(boss);
            this.spawnDangerParticles(boss);
         }

         boolean shouldDamage = this.damageInterval == 0 && ticksElapsed == 0 || this.damageInterval > 0 && ticksElapsed % this.damageInterval == 0;
         if (shouldDamage) {
            int enrageLevel = raid.getEnrageLevel();
            float damage = this.baseDamage * this.getEnrageDamageMultiplier(enrageLevel);

            for(EntityPlayer player : raid.getParticipants()) {
               if (!this.isInSafeZone(player.getPosition())) {
                  player.attackEntityFrom(DamageSource.MAGIC, damage);
                  if (boss.world instanceof WorldServer) {
                     ((WorldServer)boss.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, player.posX, player.posY + (double)1.0F, player.posZ, 5, 0.3, 0.3, 0.3, (double)0.0F, new int[0]);
                  }
               }
            }
         }

      }
   }

   protected boolean isInSafeZone(BlockPos pos) {
      for(BlockPos safeZone : this.safeZones) {
         double distSq = safeZone.distanceSq((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
         if (distSq <= (double)(this.safeZoneRadius * this.safeZoneRadius)) {
            return true;
         }
      }

      return false;
   }

   protected void spawnSafeZoneParticles(EntityLivingBase boss) {
      if (boss.world instanceof WorldServer) {
         WorldServer world = (WorldServer)boss.world;

         for(BlockPos safeZone : this.safeZones) {
            for(int angle = 0; angle < 360; angle += 15) {
               double rad = Math.toRadians((double)angle);
               double x = (double)safeZone.getX() + (double)0.5F + (double)this.safeZoneRadius * Math.cos(rad);
               double z = (double)safeZone.getZ() + (double)0.5F + (double)this.safeZoneRadius * Math.sin(rad);
               world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, x, (double)safeZone.getY() + (double)0.5F, z, 1, (double)0.0F, 0.2, (double)0.0F, (double)0.0F, new int[0]);
            }
         }
      }

   }

   protected void spawnDangerParticles(EntityLivingBase boss) {
      if (boss.world instanceof WorldServer) {
         WorldServer world = (WorldServer)boss.world;

         for(int i = 0; i < 30; ++i) {
            double x = boss.posX + (boss.world.rand.nextDouble() - (double)0.5F) * (double)60.0F;
            double z = boss.posZ + (boss.world.rand.nextDouble() - (double)0.5F) * (double)60.0F;
            BlockPos pos = new BlockPos(x, boss.posY, z);
            if (!this.isInSafeZone(pos)) {
               world.spawnParticle(EnumParticleTypes.FLAME, x, boss.posY + (double)1.0F, z, 1, 0.1, 0.3, 0.1, 0.01, new int[0]);
            }
         }
      }

   }

   public void onEnd(EntityLivingBase boss, RaidInstance raid) {
      boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0F, 0.8F);
      this.safeZones.clear();
   }

   public static enum SafeZonePattern {
      BEHIND_BOSS,
      AROUND_BOSS,
      FIXED_POSITIONS,
      RANDOM;
   }

   public static class Builder {
      private String name = "safe_zone_attack";
      private String displayName = "Safe Zone Attack";
      private float baseDamage = 30.0F;
      private int warningTicks = 100;
      private int durationTicks = 200;
      private int damageInterval = 20;
      private int safeZoneRadius = 5;
      private SafeZonePattern pattern;
      private int safeZoneCount;

      public Builder() {
         this.pattern = SafeZonePattern.BEHIND_BOSS;
         this.safeZoneCount = 1;
      }

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

      public Builder safeZoneRadius(int radius) {
         this.safeZoneRadius = radius;
         return this;
      }

      public Builder pattern(SafeZonePattern pattern) {
         this.pattern = pattern;
         return this;
      }

      public Builder safeZoneCount(int count) {
         this.safeZoneCount = count;
         return this;
      }

      public SafeZoneMechanic build() {
         return new SafeZoneMechanic(this.name, this.displayName, this.baseDamage, this.warningTicks, this.durationTicks, this.damageInterval, this.safeZoneRadius, this.pattern, this.safeZoneCount);
      }
   }
}
