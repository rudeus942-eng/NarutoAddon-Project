
package net.luck.narutoaddon.OtherCode.raid.boss.mechanics;

import net.luck.narutoaddon.OtherCode.raid.boss.BossMechanic;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class SummonMechanic implements BossMechanic {
   protected final String name;
   protected final String displayName;
   protected final int warningTicks;
   protected final int durationTicks;
   protected final int summonCount;
   protected final boolean isPuzzle;
   protected final int damageWindowTicks;
   protected final BiFunction<World, EntityLivingBase, EntityLiving> entityFactory;
   protected List<Entity> summonedEntities = new ArrayList();
   protected boolean puzzleComplete = false;
   protected int damageWindowTicksRemaining = 0;

   public SummonMechanic(String name, String displayName, int warningTicks, int durationTicks, int summonCount, boolean isPuzzle, int damageWindowTicks, BiFunction<World, EntityLivingBase, EntityLiving> entityFactory) {
      this.name = name;
      this.displayName = displayName;
      this.warningTicks = warningTicks;
      this.durationTicks = durationTicks;
      this.summonCount = summonCount;
      this.isPuzzle = isPuzzle;
      this.damageWindowTicks = damageWindowTicks;
      this.entityFactory = entityFactory;
   }

   public String getName() {
      return this.name;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getWarningMessage() {
      return "Summoning " + this.displayName + "!";
   }

   public int getWarningTicks() {
      return this.warningTicks;
   }

   public int getDurationTicks() {
      return this.durationTicks;
   }

   public boolean isPuzzleMechanic() {
      return this.isPuzzle;
   }

   public boolean allowsDamage() {
      return !this.isPuzzle || this.puzzleComplete;
   }

   public boolean isPuzzleComplete(EntityLivingBase boss, RaidInstance raid) {
      return this.puzzleComplete;
   }

   public int getSelectionWeight() {
      return 3;
   }

   public boolean isHeavyMechanic() {
      return true;
   }

   public void onStart(EntityLivingBase boss, RaidInstance raid) {
      this.summonedEntities.clear();
      this.puzzleComplete = false;
      this.damageWindowTicksRemaining = 0;
      boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 2.0F, 0.8F);
      this.spawnSummons(boss, raid);
      if (raid != null && this.isPuzzle) {
         raid.broadcastMessage("§c§lDestroy the " + this.displayName + " to damage the boss!");
      }

   }

   protected void spawnSummons(EntityLivingBase boss, RaidInstance raid) {
      for(int i = 0; i < this.summonCount; ++i) {
         double angle = Math.toRadians((double)360.0F / (double)this.summonCount * (double)i);
         int spawnRadius = 8 + boss.world.rand.nextInt(5);
         double x = boss.posX + (double)spawnRadius * Math.cos(angle);
         double z = boss.posZ + (double)spawnRadius * Math.sin(angle);
         EntityLiving summon = (EntityLiving)this.entityFactory.apply(boss.world, boss);
         if (summon != null) {
            summon.setPosition(x, boss.posY, z);
            boss.world.spawnEntity(summon);
            this.summonedEntities.add(summon);
            if (raid != null) {
               raid.trackSpawnedEntity(summon);
            }

            if (boss.world instanceof WorldServer) {
               ((WorldServer)boss.world).spawnParticle(EnumParticleTypes.PORTAL, x, boss.posY + (double)1.0F, z, 30, (double)0.5F, (double)1.0F, (double)0.5F, 0.1, new int[0]);
            }
         }
      }

   }

   public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
      int beforeCount = this.summonedEntities.size();
      this.summonedEntities.removeIf((e) -> e.isDead);
      int afterCount = this.summonedEntities.size();
      if (beforeCount != afterCount) {
         System.out.println("[SummonMechanic] " + (beforeCount - afterCount) + " summon(s) died. Remaining: " + afterCount);
      }

      if (this.isPuzzle && !this.puzzleComplete && this.summonedEntities.isEmpty()) {
         this.puzzleComplete = true;
         this.damageWindowTicksRemaining = this.damageWindowTicks;
         System.out.println("[SummonMechanic] All summons dead! Setting puzzleComplete = true");
         if (raid != null) {
            raid.broadcastMessage("§a§l" + this.displayName + " destroyed! Damage window open!");
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.HOSTILE, 2.0F, 1.2F);
      }

      if (this.puzzleComplete && this.damageWindowTicksRemaining > 0) {
         --this.damageWindowTicksRemaining;
         if (this.damageWindowTicksRemaining == 60 && raid != null) {
            raid.broadcastMessage("§e§lDamage window closing in 3 seconds!");
         }
      }

      if (ticksElapsed % 10 == 0) {
         this.spawnSummonParticles(boss);
      }

   }

   protected void spawnSummonParticles(EntityLivingBase boss) {
      if (boss.world instanceof WorldServer) {
         WorldServer world = (WorldServer)boss.world;

         for(Entity summon : this.summonedEntities) {
            if (!summon.isDead) {
               world.spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, summon.posX, summon.posY + (double)2.0F, summon.posZ, 3, 0.3, 0.3, 0.3, (double)0.0F, new int[0]);
            }
         }
      }

   }

   public void onEnd(EntityLivingBase boss, RaidInstance raid) {
      for(Entity summon : this.summonedEntities) {
         if (!summon.isDead) {
            summon.setDead();
         }

         if (raid != null) {
            raid.untrackSpawnedEntity(summon);
         }
      }

      this.summonedEntities.clear();
   }

   public List<Entity> getSummonedEntities() {
      return Collections.unmodifiableList(this.summonedEntities);
   }

   public static class Builder {
      private String name = "summon_adds";
      private String displayName = "Minions";
      private int warningTicks = 40;
      private int durationTicks = 600;
      private int summonCount = 3;
      private boolean isPuzzle = false;
      private int damageWindowTicks = 200;
      private BiFunction<World, EntityLivingBase, EntityLiving> entityFactory;

      public Builder name(String name) {
         this.name = name;
         return this;
      }

      public Builder displayName(String displayName) {
         this.displayName = displayName;
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

      public Builder summonCount(int count) {
         this.summonCount = count;
         return this;
      }

      public Builder puzzle(boolean isPuzzle) {
         this.isPuzzle = isPuzzle;
         return this;
      }

      public Builder damageWindowTicks(int ticks) {
         this.damageWindowTicks = ticks;
         return this;
      }

      public Builder entityFactory(BiFunction<World, EntityLivingBase, EntityLiving> factory) {
         this.entityFactory = factory;
         return this;
      }

      public SummonMechanic build() {
         if (this.entityFactory == null) {
            throw new IllegalStateException("Entity factory must be set");
         } else {
            return new SummonMechanic(this.name, this.displayName, this.warningTicks, this.durationTicks, this.summonCount, this.isPuzzle, this.damageWindowTicks, this.entityFactory);
         }
      }
   }
}
