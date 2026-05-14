
package net.luck.narutoaddon.OtherCode.raid.boss.bosses.kimimaro;

import net.luck.narutoaddon.OtherCode.entity.EntityFingerBullet;
import net.luck.narutoaddon.OtherCode.raid.boss.BossMechanic;
import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.luck.narutoaddon.OtherCode.raid.util.KnockbackHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.*;

public class KimimaroMechanics {
   private static final float TRUE_DAMAGE_PERCENT = 0.3F;
   private static final DamageSource TRUE_DAMAGE_SOURCE = (new DamageSource("kimimaro_true")).setDamageBypassesArmor().setMagicDamage();
   private static Map<Integer, BoneCageMechanic> activeBoneCageByRaid = new HashMap();
   private static Map<Integer, World> activeWorldByRaid = new HashMap();
   private static BoneCageMechanic activeBoneCage = null;
   private static World activeWorld = null;

   public static void applyDamageWithTrueComponent(EntityLivingBase target, float totalDamage, DamageSource normalSource) {
      float trueDamage = totalDamage * 0.3F;
      float normalDamage = totalDamage * 0.7F;
      target.hurtResistantTime = 0;
      target.attackEntityFrom(normalSource, normalDamage);
      target.hurtResistantTime = 0;
      target.attackEntityFrom(TRUE_DAMAGE_SOURCE, trueDamage);
   }

   public static void setActiveBoneCage(BoneCageMechanic mechanic, World world, int raidId) {
      activeBoneCage = mechanic;
      activeWorld = world;
      activeBoneCageByRaid.put(raidId, mechanic);
      activeWorldByRaid.put(raidId, world);
   }

   public static void clearActiveBoneCage(int raidId) {
      activeBoneCageByRaid.remove(raidId);
      activeBoneCage = null;
   }

   public static void clearActiveBoneCage() {
      activeBoneCage = null;
   }

   public static void resetMechanicState(int raidId) {
      BoneCageMechanic boneCage = (BoneCageMechanic)activeBoneCageByRaid.remove(raidId);
      World raidWorld = (World)activeWorldByRaid.remove(raidId);
      if (boneCage != null) {
         System.out.println("[KimimaroMechanics] Cleaning up BoneCage blocks for raid #" + raidId);
         boneCage.forceCleanup(raidWorld);
      }

      if (activeBoneCage == boneCage) {
         activeBoneCage = null;
      }

      if (activeBoneCageByRaid.isEmpty()) {
         activeWorld = null;
      }

   }

   public static void resetMechanicState() {
      for(Map.Entry<Integer, BoneCageMechanic> entry : activeBoneCageByRaid.entrySet()) {
         World world = (World)activeWorldByRaid.get(entry.getKey());
         if (entry.getValue() != null) {
            ((BoneCageMechanic)entry.getValue()).forceCleanup(world);
         }
      }

      activeBoneCageByRaid.clear();
      activeWorldByRaid.clear();
      if (activeBoneCage != null) {
         activeBoneCage.forceCleanup(activeWorld);
         activeBoneCage = null;
      }

      activeWorld = null;
   }

   public static BossMechanic createFingerBulletBarrage(RaidDifficulty difficulty) {
      float damage = 6.0F * difficulty.getDamageMultiplier();
      int bulletCount = 5 + difficulty.ordinal() * 2;
      return new FingerBulletBarrageMechanic(damage, bulletCount, difficulty);
   }

   public static BossMechanic createBoneSpikeField(RaidDifficulty difficulty) {
      float damage = 12.0F * difficulty.getDamageMultiplier();
      int radius = 8 + difficulty.ordinal() * 2;
      return new BoneSpikeFieldMechanic(damage, radius, difficulty);
   }

   public static BossMechanic createClematisfVineSweep(RaidDifficulty difficulty) {
      float damage = 15.0F * difficulty.getDamageMultiplier();
      return new ClematisfVineSweepMechanic(damage, difficulty);
   }

   public static BossMechanic createBoneDrillCharge(RaidDifficulty difficulty) {
      float damage = 22.0F * difficulty.getDamageMultiplier();
      return new BoneDrillChargeMechanic(damage, difficulty);
   }

   public static BossMechanic createClematisflowerLunge(RaidDifficulty difficulty) {
      float damage = 35.0F * difficulty.getDamageMultiplier();
      return new ClematisflowerLungeMechanic(damage, difficulty);
   }

   public static BossMechanic createBoneForest(RaidDifficulty difficulty) {
      float damage = 10.0F * difficulty.getDamageMultiplier();
      int safeZones = 3 - difficulty.ordinal();
      if (safeZones < 1) {
         safeZones = 1;
      }

      return new BoneForestMechanic(damage, difficulty, Math.max(1, safeZones));
   }

   public static BossMechanic createEnhancedFingerBullets(RaidDifficulty difficulty) {
      float damage = 8.0F * difficulty.getDamageMultiplier();
      int bulletCount = 8 + difficulty.ordinal() * 3;
      return new EnhancedFingerBulletsMechanic(damage, bulletCount, difficulty);
   }

   public static BossMechanic createSeedlingFern(RaidDifficulty difficulty) {
      float damage = 30.0F * difficulty.getDamageMultiplier();
      return new SeedlingFernMechanic(damage, difficulty);
   }

   public static BossMechanic createBoneCage(RaidDifficulty difficulty) {
      return new BoneCageMechanic(difficulty);
   }

   public static BossMechanic createDigitalShrapnel(RaidDifficulty difficulty) {
      float damage = 7.0F * difficulty.getDamageMultiplier();
      return new DigitalShrapnelMechanic(damage, difficulty);
   }

   private static class FingerBulletBarrageMechanic implements BossMechanic {
      private final float damage;
      private final int bulletCount;
      private final RaidDifficulty difficulty;
      private int ticksSinceLastBarrage = 0;
      private int chargeUpTicks = 0;
      private static final int CHARGE_UP_TIME = 15;
      private boolean isCharging = false;
      private EntityPlayer targetPlayer = null;

      public FingerBulletBarrageMechanic(float damage, int bulletCount, RaidDifficulty difficulty) {
         this.damage = damage;
         this.bulletCount = bulletCount;
         this.difficulty = difficulty;
      }

      public String getName() {
         return "finger_bullet_barrage";
      }

      public String getDisplayName() {
         return "Teshi Sendan";
      }

      public String getWarningMessage() {
         return "Kimimaro aims his fingers!";
      }

      public int getWarningTicks() {
         return 30 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 200;
      }

      public int getSelectionWeight() {
         return 10;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.ticksSinceLastBarrage = 0;
         this.chargeUpTicks = 0;
         this.isCharging = false;
         this.targetPlayer = null;
         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_SKELETON_SHOOT, SoundCategory.HOSTILE, 1.5F, 1.2F);
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            int barrageCooldown = 60 - this.difficulty.ordinal() * 12;
            ++this.ticksSinceLastBarrage;
            if (!this.isCharging && this.ticksSinceLastBarrage >= barrageCooldown) {
               this.isCharging = true;
               this.chargeUpTicks = 0;
               List<EntityPlayerMP> players = raid.getParticipants();
               if (!players.isEmpty()) {
                  this.targetPlayer = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
                  raid.broadcastMessage("§cKimimaro targets " + this.targetPlayer.getName() + " with Teshi Sendan!");
               }
            }

            if (this.isCharging) {
               ++this.chargeUpTicks;
               if (this.chargeUpTicks % 3 == 0 && boss.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)boss.world;
                  double lookX = (double)(-MathHelper.sin(boss.rotationYaw * ((float)Math.PI / 180F)));
                  double lookZ = (double)MathHelper.cos(boss.rotationYaw * ((float)Math.PI / 180F));
                  ws.spawnParticle(EnumParticleTypes.CRIT, boss.posX + lookX * (double)0.5F, boss.posY + 1.2, boss.posZ + lookZ * (double)0.5F, 5, 0.2, 0.2, 0.2, 0.05, new int[0]);
               }

               if (this.chargeUpTicks >= 15) {
                  this.fireBarrage(boss, raid);
                  this.isCharging = false;
                  this.ticksSinceLastBarrage = 0;
               }
            }

         }
      }

      private void fireBarrage(EntityLivingBase boss, RaidInstance raid) {
         if (this.targetPlayer == null || this.targetPlayer.isDead) {
            List<EntityPlayerMP> players = raid.getParticipants();
            if (players.isEmpty()) {
               return;
            }

            this.targetPlayer = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
         }

         double dx = this.targetPlayer.posX - boss.posX;
         double dy = this.targetPlayer.posY + (double)this.targetPlayer.getEyeHeight() - (boss.posY + (double)boss.getEyeHeight());
         double dz = this.targetPlayer.posZ - boss.posZ;
         float trueDmg = this.damage * 0.3F;

         for(int i = 0; i < this.bulletCount; ++i) {
            EntityFingerBullet.EntityCustom bullet = new EntityFingerBullet.EntityCustom(boss.world, boss, this.damage, trueDmg);
            bullet.shoot(dx + (boss.world.rand.nextDouble() - (double)0.5F) * (double)2.0F, dy + (boss.world.rand.nextDouble() - (double)0.5F) * (double)0.5F, dz + (boss.world.rand.nextDouble() - (double)0.5F) * (double)2.0F, 2.0F, 2.5F);
            boss.world.spawnEntity(bullet);
            if (raid != null) {
               raid.trackSpawnedEntity(bullet);
            }
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_SKELETON_SHOOT, SoundCategory.HOSTILE, 1.5F, 1.5F);
         if (boss.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)boss.world;
            ws.spawnParticle(EnumParticleTypes.CRIT, boss.posX, boss.posY + 1.2, boss.posZ, 15, (double)0.5F, 0.3, (double)0.5F, 0.15, new int[0]);
         }

      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         this.isCharging = false;
         this.targetPlayer = null;
      }
   }

   private static class BoneSpikeFieldMechanic implements BossMechanic {
      private final float damage;
      private final int radius;
      private final RaidDifficulty difficulty;
      private BlockPos center;
      private boolean spikeErupted = false;
      private final int eruptionTick;

      public BoneSpikeFieldMechanic(float damage, int radius, RaidDifficulty difficulty) {
         this.damage = damage;
         this.radius = radius;
         this.difficulty = difficulty;
         this.eruptionTick = 40 - difficulty.ordinal() * 10;
      }

      public String getName() {
         return "bone_spike_field";
      }

      public String getDisplayName() {
         return "Bone Spike Field";
      }

      public String getWarningMessage() {
         return "Kimimaro drives bone spikes into the ground!";
      }

      public int getWarningTicks() {
         return 60 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return this.eruptionTick + 30;
      }

      public int getSelectionWeight() {
         return 10;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.center = boss.getPosition();
         this.spikeErupted = false;
         boss.world.playSound((EntityPlayer)null, this.center, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 2.0F, 1.2F);
         if (raid != null) {
            raid.broadcastMessage("§c§lBone spikes are erupting from the ground!");
            raid.broadcastMessage("§eMove away from Kimimaro!");
         }

      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            if (!this.spikeErupted && ticksElapsed % 3 == 0 && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;
               float progress = (float)ticksElapsed / (float)this.eruptionTick;
               int currentRadius = Math.max(1, (int)((float)this.radius * progress));

               for(int angle = 0; angle < 360; angle += 10) {
                  double rad = Math.toRadians((double)angle);
                  double x = (double)this.center.getX() + (double)0.5F + (double)currentRadius * Math.cos(rad);
                  double z = (double)this.center.getZ() + (double)0.5F + (double)currentRadius * Math.sin(rad);
                  world.spawnParticle(EnumParticleTypes.CRIT, x, (double)this.center.getY() + 0.3, z, 2, (double)0.0F, 0.1, (double)0.0F, (double)0.0F, new int[0]);
               }

               for(int i = 0; i < 10; ++i) {
                  double x = (double)this.center.getX() + (double)0.5F + (boss.world.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)currentRadius;
                  double z = (double)this.center.getZ() + (double)0.5F + (boss.world.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)currentRadius;
                  EnumParticleTypes var10001 = EnumParticleTypes.BLOCK_DUST;
                  double var10003 = (double)this.center.getY() + 0.1;
                  int[] var10010 = new int[1];
                  Block var10013 = Blocks.BONE_BLOCK;
                  var10010[0] = Block.getStateId(Blocks.BONE_BLOCK.getDefaultState());
                  world.spawnParticle(var10001, x, var10003, z, 3, 0.2, 0.1, 0.2, 0.02, var10010);
               }
            }

            if (ticksElapsed == this.eruptionTick && !this.spikeErupted) {
               this.spikeErupted = true;
               AxisAlignedBB hitbox = new AxisAlignedBB((double)(this.center.getX() - this.radius), (double)(this.center.getY() - 2), (double)(this.center.getZ() - this.radius), (double)(this.center.getX() + this.radius), (double)(this.center.getY() + 5), (double)(this.center.getZ() + this.radius));

               for(EntityPlayer player : boss.world.getEntitiesWithinAABB(EntityPlayer.class, hitbox)) {
                  double distSq = this.center.distanceSq(player.posX, player.posY, player.posZ);
                  if (distSq <= (double)(this.radius * this.radius)) {
                     KimimaroMechanics.applyDamageWithTrueComponent(player, this.damage, DamageSource.MAGIC);
                     double dx = player.posX - (double)this.center.getX();
                     double dz = player.posZ - (double)this.center.getZ();
                     double dist = Math.sqrt(dx * dx + dz * dz);
                     if (dist > (double)0.0F) {
                        KnockbackHelper.applyWallSafeKnockback(player, dx / dist * 0.8, 0.6, dz / dist * 0.8);
                     }
                  }
               }

               if (boss.world instanceof WorldServer) {
                  WorldServer world = (WorldServer)boss.world;

                  for(int angle = 0; angle < 360; angle += 20) {
                     double rad = Math.toRadians((double)angle);

                     for(int r = 2; r <= this.radius; r += 2) {
                        double x = (double)this.center.getX() + (double)0.5F + (double)r * Math.cos(rad);
                        double z = (double)this.center.getZ() + (double)0.5F + (double)r * Math.sin(rad);

                        for(int h = 0; h < 4; ++h) {
                           world.spawnParticle(EnumParticleTypes.CRIT, x, (double)(this.center.getY() + h), z, 3, 0.1, 0.2, 0.1, 0.02, new int[0]);
                        }
                     }
                  }

                  world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, (double)this.center.getX() + (double)0.5F, (double)(this.center.getY() + 1), (double)this.center.getZ() + (double)0.5F, 5, (double)this.radius / (double)2.0F, (double)1.0F, (double)this.radius / (double)2.0F, (double)0.0F, new int[0]);
                  EnumParticleTypes var34 = EnumParticleTypes.BLOCK_DUST;
                  double var10002 = (double)this.center.getX() + (double)0.5F;
                  double var35 = (double)this.center.getY() + (double)0.5F;
                  double var10004 = (double)this.center.getZ() + (double)0.5F;
                  double var10006 = (double)this.radius * (double)0.5F;
                  double var10008 = (double)this.radius * (double)0.5F;
                  int[] var36 = new int[1];
                  Block var37 = Blocks.BONE_BLOCK;
                  var36[0] = Block.getStateId(Blocks.BONE_BLOCK.getDefaultState());
                  world.spawnParticle(var34, var10002, var35, var10004, 100, var10006, (double)0.5F, var10008, 0.1, var36);
               }

               boss.world.playSound((EntityPlayer)null, this.center, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.5F, 0.6F);
               boss.world.playSound((EntityPlayer)null, this.center, SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.HOSTILE, 2.0F, 0.4F);
            }

            if (this.spikeErupted && ticksElapsed % 5 == 0 && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;

               for(int i = 0; i < 15; ++i) {
                  double x = (double)this.center.getX() + (double)0.5F + (boss.world.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)this.radius;
                  double z = (double)this.center.getZ() + (double)0.5F + (boss.world.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)this.radius;
                  world.spawnParticle(EnumParticleTypes.CRIT, x, (double)this.center.getY() + boss.world.rand.nextDouble() * (double)3.0F, z, 1, 0.1, 0.2, 0.1, 0.01, new int[0]);
               }
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.HOSTILE, 1.0F, 1.2F);
      }
   }

   private static class ClematisfVineSweepMechanic implements BossMechanic {
      private final float damage;
      private final RaidDifficulty difficulty;
      private boolean swept = false;
      private final int sweepTick;
      private static final float SWEEP_RANGE = 7.0F;

      public ClematisfVineSweepMechanic(float damage, RaidDifficulty difficulty) {
         this.damage = damage;
         this.difficulty = difficulty;
         this.sweepTick = 35 - difficulty.ordinal() * 8;
      }

      public String getName() {
         return "clematis_vine_sweep";
      }

      public String getDisplayName() {
         return "Clematis Vine";
      }

      public String getWarningMessage() {
         return "Kimimaro extends a bone whip!";
      }

      public int getWarningTicks() {
         return 50 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return this.sweepTick + 20;
      }

      public int getSelectionWeight() {
         return 10;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.swept = false;
         if (raid != null) {
            raid.broadcastMessage("§c§lKimimaro prepares Clematis Vine!");
            raid.broadcastMessage("§eDodge the frontal sweep!");
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 2.0F, 0.5F);
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            if (!this.swept && ticksElapsed % 3 == 0 && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;
               double lookX = (double)(-MathHelper.sin(boss.rotationYaw * ((float)Math.PI / 180F)));
               double lookZ = (double)MathHelper.cos(boss.rotationYaw * ((float)Math.PI / 180F));

               for(int i = 1; i <= 14; ++i) {
                  double dist = (double)i * (double)0.5F;

                  for(int a = -75; a <= 75; a += 15) {
                     double angle = Math.toRadians((double)a);
                     double rotatedX = lookX * Math.cos(angle) - lookZ * Math.sin(angle);
                     double rotatedZ = lookX * Math.sin(angle) + lookZ * Math.cos(angle);
                     double px = boss.posX + rotatedX * dist;
                     double pz = boss.posZ + rotatedZ * dist;
                     world.spawnParticle(EnumParticleTypes.CRIT, px, boss.posY + (double)0.5F, pz, 1, 0.05, 0.1, 0.05, (double)0.0F, new int[0]);
                  }
               }
            }

            if (ticksElapsed == this.sweepTick && !this.swept) {
               this.swept = true;
               double lookX = (double)(-MathHelper.sin(boss.rotationYaw * ((float)Math.PI / 180F)));
               double lookZ = (double)MathHelper.cos(boss.rotationYaw * ((float)Math.PI / 180F));

               for(Entity e : boss.world.getEntitiesWithinAABBExcludingEntity(boss, boss.getEntityBoundingBox().grow((double)7.0F))) {
                  if (e instanceof EntityPlayer) {
                     double dx = e.posX - boss.posX;
                     double dz = e.posZ - boss.posZ;
                     double eDist = Math.sqrt(dx * dx + dz * dz);
                     if (!(eDist > (double)7.0F)) {
                        double dot = (dx * lookX + dz * lookZ) / eDist;
                        if (dot > -0.26) {
                           EntityPlayer player = (EntityPlayer)e;
                           KimimaroMechanics.applyDamageWithTrueComponent(player, this.damage, DamageSource.MAGIC);
                           KnockbackHelper.applyWallSafeKnockback(player, dx / eDist * 0.7, 0.3, dz / eDist * 0.7);
                        }
                     }
                  }
               }

               if (boss.world instanceof WorldServer) {
                  WorldServer world = (WorldServer)boss.world;

                  for(int i = 1; i <= 14; ++i) {
                     for(int a = -75; a <= 75; a += 10) {
                        double angle = Math.toRadians((double)a);
                        double rotatedX = lookX * Math.cos(angle) - lookZ * Math.sin(angle);
                        double rotatedZ = lookX * Math.sin(angle) + lookZ * Math.cos(angle);
                        double px = boss.posX + rotatedX * (double)i * (double)0.5F;
                        double pz = boss.posZ + rotatedZ * (double)i * (double)0.5F;
                        world.spawnParticle(EnumParticleTypes.SWEEP_ATTACK, px, boss.posY + (double)1.0F, pz, 1, 0.1, 0.1, 0.1, (double)0.0F, new int[0]);
                     }
                  }
               }

               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 2.0F, 0.6F);
               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.HOSTILE, 1.5F, 0.7F);
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
      }
   }

   private static class BoneDrillChargeMechanic implements BossMechanic {
      private final float damage;
      private final RaidDifficulty difficulty;
      private EntityPlayer target;
      private boolean charged = false;
      private BlockPos startPos;
      private BlockPos endPos;
      private final int chargeTick;

      public BoneDrillChargeMechanic(float damage, RaidDifficulty difficulty) {
         this.damage = damage;
         this.difficulty = difficulty;
         this.chargeTick = 30 - difficulty.ordinal() * 6;
      }

      public String getName() {
         return "bone_drill_charge";
      }

      public String getDisplayName() {
         return "Bone Drill Charge";
      }

      public String getWarningMessage() {
         return "Kimimaro forms a bone drill on his arm!";
      }

      public int getWarningTicks() {
         return 60 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return this.chargeTick + 25;
      }

      public int getSelectionWeight() {
         return 10;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.charged = false;
         this.startPos = boss.getPosition();
         List<EntityPlayerMP> players = (List<EntityPlayerMP>)(raid != null ? raid.getParticipants() : new ArrayList());
         if (!players.isEmpty()) {
            this.target = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
            this.endPos = this.target.getPosition();
            if (raid != null) {
               raid.broadcastMessage("§c§lKimimaro targets " + this.target.getName() + " with Bone Drill!");
               raid.broadcastMessage("§eDodge to the side!");
            }
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 2.0F, 0.5F);
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null && this.target != null) {
            if (!this.charged && ticksElapsed % 3 == 0 && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;
               double dx = this.target.posX - boss.posX;
               double dz = this.target.posZ - boss.posZ;
               double dist = Math.sqrt(dx * dx + dz * dz);
               if (dist > (double)0.0F) {
                  for(int i = 0; i < (int)(dist * (double)2.0F); ++i) {
                     double t = (double)i / (dist * (double)2.0F);
                     double px = boss.posX + dx * t;
                     double pz = boss.posZ + dz * t;
                     world.spawnParticle(EnumParticleTypes.CRIT, px, boss.posY + 0.3, pz, 1, 0.1, (double)0.0F, 0.1, (double)0.0F, new int[0]);
                  }
               }

               world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, boss.posX, boss.posY + 1.2, boss.posZ, 8, 0.3, 0.3, 0.3, 0.1, new int[0]);
            }

            if (ticksElapsed == this.chargeTick && !this.charged) {
               this.charged = true;
               this.endPos = this.target.getPosition();
               double dx = (double)this.endPos.getX() + (double)0.5F - boss.posX;
               double dz = (double)this.endPos.getZ() + (double)0.5F - boss.posZ;
               double dist = Math.sqrt(dx * dx + dz * dz);
               if (dist > (double)0.0F) {
                  double nx = dx / dist;
                  double nz = dz / dist;
                  double chargeDist = Math.min(dist + (double)3.0F, (double)22.0F);
                  AxisAlignedBB chargePath = new AxisAlignedBB(Math.min(boss.posX, boss.posX + nx * chargeDist) - (double)2.0F, boss.posY - (double)1.0F, Math.min(boss.posZ, boss.posZ + nz * chargeDist) - (double)2.0F, Math.max(boss.posX, boss.posX + nx * chargeDist) + (double)2.0F, boss.posY + (double)3.0F, Math.max(boss.posZ, boss.posZ + nz * chargeDist) + (double)2.0F);

                  for(EntityPlayer player : boss.world.getEntitiesWithinAABB(EntityPlayer.class, chargePath)) {
                     double playerDx = player.posX - boss.posX;
                     double playerDz = player.posZ - boss.posZ;
                     double projLen = playerDx * nx + playerDz * nz;
                     if (projLen > (double)0.0F && projLen <= chargeDist) {
                        double perpDist = Math.abs(playerDx * -nz + playerDz * nx);
                        if (perpDist <= (double)2.5F) {
                           KimimaroMechanics.applyDamageWithTrueComponent(player, this.damage, DamageSource.MAGIC);
                           KnockbackHelper.applyWallSafeKnockback(player, nx * 1.2, (double)0.5F, nz * 1.2);
                        }
                     }
                  }

                  double finalX = boss.posX + nx * Math.min(dist, (double)20.0F);
                  double finalZ = boss.posZ + nz * Math.min(dist, (double)20.0F);
                  boss.setPositionAndUpdate(finalX, boss.posY, finalZ);
                  if (boss.world instanceof WorldServer) {
                     WorldServer world = (WorldServer)boss.world;

                     for(int i = 0; i < (int)(dist * (double)2.0F); ++i) {
                        double t = (double)i / (dist * (double)2.0F);
                        double px = (double)this.startPos.getX() + dx * t;
                        double pz = (double)this.startPos.getZ() + dz * t;
                        world.spawnParticle(EnumParticleTypes.CRIT, px, boss.posY + (double)1.0F, pz, 5, 0.3, 0.3, 0.3, 0.1, new int[0]);
                        world.spawnParticle(EnumParticleTypes.CLOUD, px, boss.posY + (double)0.5F, pz, 2, 0.1, 0.1, 0.1, 0.02, new int[0]);
                     }

                     world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, finalX, boss.posY + (double)1.0F, finalZ, 3, (double)0.5F, (double)0.5F, (double)0.5F, (double)0.0F, new int[0]);
                  }
               }

               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 2.5F, 0.4F);
               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 1.2F);
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         this.target = null;
      }
   }

   private static class ClematisflowerLungeMechanic implements BossMechanic {
      private final float damage;
      private final RaidDifficulty difficulty;
      private EntityPlayer target;
      private boolean lunged = false;
      private final int lungeTick;

      public ClematisflowerLungeMechanic(float damage, RaidDifficulty difficulty) {
         this.damage = damage;
         this.difficulty = difficulty;
         this.lungeTick = 45 - difficulty.ordinal() * 8;
      }

      public String getName() {
         return "clematis_flower";
      }

      public String getDisplayName() {
         return "Dance of the Clematis: Flower";
      }

      public String getWarningMessage() {
         return "Kimimaro forms a massive bone spear!";
      }

      public int getWarningTicks() {
         return 80 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return this.lungeTick + 30;
      }

      public int getSelectionWeight() {
         return 8;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.lunged = false;
         List<EntityPlayerMP> players = (List<EntityPlayerMP>)(raid != null ? raid.getParticipants() : new ArrayList());
         if (!players.isEmpty()) {
            this.target = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
            if (raid != null) {
               raid.broadcastMessage("§4§lKimimaro: §7\"Clematis Flower!\"");
               raid.broadcastMessage("§c§l" + this.target.getName() + " is the target! Move NOW!");
            }
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 2.0F, 0.6F);
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null && this.target != null) {
            if (!this.lunged && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;
               if (ticksElapsed % 2 == 0) {
                  double angle = Math.toRadians((double)(ticksElapsed * 20 % 360));
                  double spiralRadius = (double)1.5F * ((double)1.0F - (double)ticksElapsed / (double)this.lungeTick);
                  double px = boss.posX + spiralRadius * Math.cos(angle);
                  double pz = boss.posZ + spiralRadius * Math.sin(angle);
                  world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, px, boss.posY + (double)1.0F + (double)ticksElapsed * 0.02, pz, 3, 0.1, 0.1, 0.1, 0.05, new int[0]);
               }

               if (ticksElapsed % 5 == 0) {
                  double dx = this.target.posX - boss.posX;
                  double dz = this.target.posZ - boss.posZ;
                  double dist = Math.sqrt(dx * dx + dz * dz);

                  for(int i = 0; i < (int)dist; ++i) {
                     double t = (double)i / dist;
                     world.spawnParticle(EnumParticleTypes.SPELL_WITCH, boss.posX + dx * t, boss.posY + (double)0.5F, boss.posZ + dz * t, 1, 0.1, (double)0.0F, 0.1, (double)0.0F, new int[0]);
                  }
               }
            }

            if (ticksElapsed == this.lungeTick && !this.lunged) {
               this.lunged = true;
               double dx = this.target.posX - boss.posX;
               double dz = this.target.posZ - boss.posZ;
               double dist = Math.sqrt(dx * dx + dz * dz);
               if (dist > (double)0.0F) {
                  double nx = dx / dist;
                  double nz = dz / dist;
                  double lungeDist = Math.min(dist + (double)2.0F, (double)15.0F);
                  AxisAlignedBB impactZone = new AxisAlignedBB(this.target.posX - (double)3.0F, this.target.posY - (double)1.0F, this.target.posZ - (double)3.0F, this.target.posX + (double)3.0F, this.target.posY + (double)3.0F, this.target.posZ + (double)3.0F);

                  for(EntityPlayer player : boss.world.getEntitiesWithinAABB(EntityPlayer.class, impactZone)) {
                     KimimaroMechanics.applyDamageWithTrueComponent(player, this.damage, DamageSource.MAGIC);
                     double kbX = player.posX - boss.posX;
                     double kbZ = player.posZ - boss.posZ;
                     double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
                     if (kbDist > (double)0.0F) {
                        KnockbackHelper.applyWallSafeKnockback(player, kbX / kbDist * (double)1.5F, 0.7, kbZ / kbDist * (double)1.5F);
                     }
                  }

                  double finalX = boss.posX + nx * Math.min(dist, (double)12.0F);
                  double finalZ = boss.posZ + nz * Math.min(dist, (double)12.0F);
                  boss.setPositionAndUpdate(finalX, boss.posY, finalZ);
                  if (boss.world instanceof WorldServer) {
                     WorldServer world = (WorldServer)boss.world;
                     world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.target.posX, this.target.posY + (double)1.0F, this.target.posZ, 3, (double)1.0F, (double)1.0F, (double)1.0F, (double)0.0F, new int[0]);
                     world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.target.posX, this.target.posY + (double)1.0F, this.target.posZ, 50, (double)2.0F, (double)2.0F, (double)2.0F, 0.3, new int[0]);

                     for(int i = 0; i < (int)(dist * (double)2.0F); ++i) {
                        double t = (double)i / (dist * (double)2.0F);
                        world.spawnParticle(EnumParticleTypes.SPELL_WITCH, boss.posX + dx * t * 0.8, boss.posY + (double)1.0F, boss.posZ + dz * t * 0.8, 3, 0.2, 0.2, 0.2, 0.05, new int[0]);
                     }
                  }
               }

               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.5F, 0.5F);
               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 2.0F, 0.6F);
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         this.target = null;
      }
   }

   private static class BoneForestMechanic implements BossMechanic {
      private final float damage;
      private final RaidDifficulty difficulty;
      private final int safeZoneCount;
      private List<BlockPos> safeZones = new ArrayList();
      private List<BlockPos> placedBlocks = new ArrayList();

      public BoneForestMechanic(float damage, RaidDifficulty difficulty, int safeZoneCount) {
         this.damage = damage;
         this.difficulty = difficulty;
         this.safeZoneCount = safeZoneCount;
      }

      public String getName() {
         return "bone_forest";
      }

      public String getDisplayName() {
         return "Dance of the Seedling Fern: Forest";
      }

      public String getWarningMessage() {
         return "Kimimaro slams the ground — bones erupt!";
      }

      public int getWarningTicks() {
         return 80 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 200;
      }

      public int getSelectionWeight() {
         return 8;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public List<BlockPos> getSafeZones() {
         return this.safeZones;
      }

      public int getSafeZoneRadius() {
         return 5;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.safeZones.clear();
         this.placedBlocks.clear();
         Random rand = boss.world.rand;

         for(int i = 0; i < this.safeZoneCount; ++i) {
            double angle = Math.toRadians((double)360.0F / (double)this.safeZoneCount * (double)i + (double)rand.nextInt(30));
            int distance = 12 + rand.nextInt(10);
            int x = (int)(boss.posX + (double)distance * Math.cos(angle));
            int z = (int)(boss.posZ + (double)distance * Math.sin(angle));
            this.safeZones.add(new BlockPos(x, (int)boss.posY, z));
         }

         int spikeCount = 20 + this.difficulty.ordinal() * 8;
         int bossY = (int)boss.posY;

         for(int i = 0; i < spikeCount; ++i) {
            double angle = rand.nextDouble() * Math.PI * (double)2.0F;
            int dist = 5 + rand.nextInt(25);
            int sx = (int)(boss.posX + (double)dist * Math.cos(angle));
            int sz = (int)(boss.posZ + (double)dist * Math.sin(angle));
            BlockPos spikeBase = new BlockPos(sx, bossY, sz);
            if (!this.isInSafeZone(spikeBase)) {
               int height = 2 + rand.nextInt(4);

               for(int y = 0; y < height; ++y) {
                  BlockPos pos = spikeBase.up(y);
                  if (boss.world.isAirBlock(pos)) {
                     boss.world.setBlockState(pos, Blocks.BONE_BLOCK.getDefaultState());
                     this.placedBlocks.add(pos);
                  }
               }

               BlockPos tipPos = spikeBase.up(height);
               if (boss.world.isAirBlock(tipPos)) {
                  boss.world.setBlockState(tipPos, Blocks.END_ROD.getDefaultState());
                  this.placedBlocks.add(tipPos);
               }
            }
         }

         if (raid != null) {
            raid.trackBlocks(this.placedBlocks);
         }

         if (raid != null) {
            raid.broadcastMessage("§c§lBone spikes erupt from the ground!");
            raid.broadcastMessage("§e§lFind the " + this.safeZoneCount + " clear gaps!");
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 3.0F, 0.5F);
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            if (ticksElapsed % 3 == 0 && boss.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)boss.world;

               for(BlockPos safeZone : this.safeZones) {
                  int safeRadius = this.getSafeZoneRadius();

                  for(int angle = 0; angle < 360; angle += 10) {
                     double rad = Math.toRadians((double)angle);
                     double px = (double)safeZone.getX() + (double)0.5F + (double)safeRadius * Math.cos(rad);
                     double pz = (double)safeZone.getZ() + (double)0.5F + (double)safeRadius * Math.sin(rad);
                     ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, px, (double)safeZone.getY() + (double)0.5F, pz, 3, 0.1, 0.3, 0.1, (double)0.0F, new int[0]);
                  }

                  for(int y = 0; y < 12; ++y) {
                     ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)safeZone.getX() + (double)0.5F, (double)(safeZone.getY() + y), (double)safeZone.getZ() + (double)0.5F, 2, 0.3, 0.1, 0.3, (double)0.0F, new int[0]);
                  }
               }

               for(int i = 0; i < 15; ++i) {
                  if (i < this.placedBlocks.size()) {
                     BlockPos bp = (BlockPos)this.placedBlocks.get(boss.world.rand.nextInt(this.placedBlocks.size()));
                     EnumParticleTypes var10001 = EnumParticleTypes.BLOCK_DUST;
                     double var10002 = (double)bp.getX() + (double)0.5F;
                     double var10003 = (double)bp.getY() + (double)0.5F;
                     double var10004 = (double)bp.getZ() + (double)0.5F;
                     int[] var10010 = new int[1];
                     Block var10013 = Blocks.BONE_BLOCK;
                     var10010[0] = Block.getStateId(Blocks.BONE_BLOCK.getDefaultState());
                     ws.spawnParticle(var10001, var10002, var10003, var10004, 2, 0.3, 0.3, 0.3, 0.01, var10010);
                  }
               }
            }

            if (ticksElapsed % 20 == 0) {
               for(EntityPlayer player : raid.getParticipants()) {
                  if (!this.isInSafeZone(player.getPosition())) {
                     KimimaroMechanics.applyDamageWithTrueComponent(player, this.damage, DamageSource.MAGIC);
                     if (boss.world instanceof WorldServer) {
                        ((WorldServer)boss.world).spawnParticle(EnumParticleTypes.CRIT, player.posX, player.posY + (double)0.5F, player.posZ, 10, 0.2, (double)1.0F, 0.2, 0.05, new int[0]);
                     }
                  }
               }
            }

         }
      }

      private boolean isInSafeZone(BlockPos pos) {
         for(BlockPos safeZone : this.safeZones) {
            double distSq = safeZone.distanceSq((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            if (distSq <= (double)(this.getSafeZoneRadius() * this.getSafeZoneRadius())) {
               return true;
            }
         }

         return false;
      }

      private void removeSpikes(World world) {
         for(BlockPos pos : this.placedBlocks) {
            if (world.getBlockState(pos).getBlock() == Blocks.BONE_BLOCK || world.getBlockState(pos).getBlock() == Blocks.END_ROD) {
               world.setBlockToAir(pos);
            }
         }

         this.placedBlocks.clear();
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         this.removeSpikes(boss.world);
         this.safeZones.clear();
         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0F, 0.8F);
         if (raid != null) {
            raid.broadcastMessage("§aThe bone spikes crumble away!");
         }

      }

      public void onInterrupt(EntityLivingBase boss, RaidInstance raid) {
         this.removeSpikes(boss.world);
         this.safeZones.clear();
      }
   }

   private static class EnhancedFingerBulletsMechanic implements BossMechanic {
      private final float damage;
      private final int bulletCount;
      private final RaidDifficulty difficulty;
      private int ticksSinceLastVolley = 0;

      public EnhancedFingerBulletsMechanic(float damage, int bulletCount, RaidDifficulty difficulty) {
         this.damage = damage;
         this.bulletCount = bulletCount;
         this.difficulty = difficulty;
      }

      public String getName() {
         return "enhanced_finger_bullets";
      }

      public String getDisplayName() {
         return "Enhanced Teshi Sendan";
      }

      public String getWarningMessage() {
         return "Kimimaro's fingers glow with intense chakra!";
      }

      public int getWarningTicks() {
         return 40 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 160;
      }

      public int getSelectionWeight() {
         return 10;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.ticksSinceLastVolley = 0;
         if (raid != null) {
            raid.broadcastMessage("§c§lKimimaro unleashes a storm of bone bullets!");
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 2.0F, 1.5F);
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            int volleyInterval = 30 - this.difficulty.ordinal() * 5;
            ++this.ticksSinceLastVolley;
            if (this.ticksSinceLastVolley >= volleyInterval) {
               this.ticksSinceLastVolley = 0;
               List<EntityPlayerMP> players = raid.getParticipants();
               if (players.isEmpty()) {
                  return;
               }

               float trueDmg = this.damage * 0.3F;

               for(EntityPlayerMP target : players) {
                  if (!target.isDead) {
                     int bulletsPerPlayer = Math.max(2, this.bulletCount / players.size());
                     double dx = target.posX - boss.posX;
                     double dy = target.posY + (double)target.getEyeHeight() - (boss.posY + (double)boss.getEyeHeight());
                     double dz = target.posZ - boss.posZ;

                     for(int i = 0; i < bulletsPerPlayer; ++i) {
                        EntityFingerBullet.EntityCustom bullet = new EntityFingerBullet.EntityCustom(boss.world, boss, this.damage, trueDmg);
                        bullet.shoot(dx + (boss.world.rand.nextDouble() - (double)0.5F) * (double)2.5F, dy + (boss.world.rand.nextDouble() - (double)0.5F) * 0.8, dz + (boss.world.rand.nextDouble() - (double)0.5F) * (double)2.5F, 2.2F, 3.0F);
                        boss.world.spawnEntity(bullet);
                        if (raid != null) {
                           raid.trackSpawnedEntity(bullet);
                        }
                     }
                  }
               }

               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_SKELETON_SHOOT, SoundCategory.HOSTILE, 1.5F, 1.8F);
               if (boss.world instanceof WorldServer) {
                  ((WorldServer)boss.world).spawnParticle(EnumParticleTypes.CRIT, boss.posX, boss.posY + 1.2, boss.posZ, 20, 0.8, 0.3, 0.8, 0.2, new int[0]);
               }
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
      }
   }

   private static class SeedlingFernMechanic implements BossMechanic {
      private final float damage;
      private final RaidDifficulty difficulty;
      private BlockPos center;
      private boolean erupted = false;
      private static final float FERN_RADIUS = 15.0F;
      private static final int TELEGRAPH_TICKS = 60;

      public SeedlingFernMechanic(float damage, RaidDifficulty difficulty) {
         this.damage = damage;
         this.difficulty = difficulty;
      }

      public String getName() {
         return "seedling_fern";
      }

      public String getDisplayName() {
         return "Dance of the Seedling Fern";
      }

      public String getWarningMessage() {
         return "Kimimaro places his palm on the ground...";
      }

      public int getWarningTicks() {
         return 100 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 100;
      }

      public int getSelectionWeight() {
         return 5;
      }

      public boolean isHeavyMechanic() {
         return true;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.center = boss.getPosition();
         this.erupted = false;
         if (raid != null) {
            raid.broadcastMessage("§4§lKimimaro: §7\"Dance of the Seedling Fern!\"");
            raid.broadcastMessage("§c§lMassive bone eruption incoming! GET AWAY!");
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 3.0F, 0.4F);
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            World world = boss.world;
            if (!this.erupted && ticksElapsed < 60 && world instanceof WorldServer) {
               WorldServer ws = (WorldServer)world;
               float progress = (float)ticksElapsed / 60.0F;
               float currentRadius = 15.0F * progress;

               for(int angle = 0; angle < 360; angle += 5) {
                  double rad = Math.toRadians((double)angle);
                  double x = (double)this.center.getX() + (double)0.5F + (double)currentRadius * Math.cos(rad);
                  double z = (double)this.center.getZ() + (double)0.5F + (double)currentRadius * Math.sin(rad);
                  ws.spawnParticle(EnumParticleTypes.CRIT, x, (double)this.center.getY() + 0.3, z, 2, (double)0.0F, 0.3, (double)0.0F, (double)0.0F, new int[0]);
               }

               if (ticksElapsed % 4 == 0) {
                  for(int i = 0; i < 8; ++i) {
                     double angle = world.rand.nextDouble() * Math.PI * (double)2.0F;
                     double r = world.rand.nextDouble() * (double)currentRadius;
                     double x = (double)this.center.getX() + (double)0.5F + r * Math.cos(angle);
                     double z = (double)this.center.getZ() + (double)0.5F + r * Math.sin(angle);
                     ws.spawnParticle(EnumParticleTypes.CRIT, x, (double)this.center.getY() + 0.1, z, 5, 0.1, (double)2.0F, 0.1, 0.01, new int[0]);
                     EnumParticleTypes var10001 = EnumParticleTypes.BLOCK_DUST;
                     double var10003 = (double)this.center.getY() + 0.1;
                     int[] var10010 = new int[1];
                     Block var10013 = Blocks.BONE_BLOCK;
                     var10010[0] = Block.getStateId(Blocks.BONE_BLOCK.getDefaultState());
                     ws.spawnParticle(var10001, x, var10003, z, 3, 0.2, 0.1, 0.2, 0.02, var10010);
                  }
               }

               if (ticksElapsed % 20 == 0) {
                  float volume = 0.5F + progress * 2.0F;
                  world.playSound((EntityPlayer)null, this.center, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, volume, 0.3F + progress * 0.4F);
               }
            }

            if (ticksElapsed == 60 && !this.erupted) {
               this.erupted = true;
               AxisAlignedBB hitbox = new AxisAlignedBB((double)((float)this.center.getX() - 15.0F), (double)(this.center.getY() - 2), (double)((float)this.center.getZ() - 15.0F), (double)((float)this.center.getX() + 15.0F), (double)(this.center.getY() + 8), (double)((float)this.center.getZ() + 15.0F));

               for(EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, hitbox)) {
                  double distSq = this.center.distanceSq(player.posX, player.posY, player.posZ);
                  if (distSq <= (double)225.0F) {
                     double dist = Math.sqrt(distSq);
                     float damageMult = 1.0F - (float)(dist / (double)15.0F) * 0.5F;
                     float finalDmg = this.damage * damageMult;
                     KimimaroMechanics.applyDamageWithTrueComponent(player, finalDmg, DamageSource.MAGIC);
                     double dx = player.posX - (double)this.center.getX();
                     double dz = player.posZ - (double)this.center.getZ();
                     double d = Math.sqrt(dx * dx + dz * dz);
                     if (d > (double)0.0F) {
                        KnockbackHelper.applyWallSafeKnockback(player, dx / d * (double)1.5F, (double)1.0F, dz / d * (double)1.5F);
                     }
                  }
               }

               if (world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)world;

                  for(int angle = 0; angle < 360; angle += 10) {
                     double rad = Math.toRadians((double)angle);

                     for(int r = 1; r <= 15; r += 2) {
                        double x = (double)this.center.getX() + (double)0.5F + (double)r * Math.cos(rad);
                        double z = (double)this.center.getZ() + (double)0.5F + (double)r * Math.sin(rad);

                        for(int h = 0; h < 6; ++h) {
                           ws.spawnParticle(EnumParticleTypes.CRIT, x, (double)this.center.getY() + (double)h * (double)0.5F, z, 3, 0.1, 0.1, 0.1, 0.02, new int[0]);
                        }
                     }
                  }

                  ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, (double)this.center.getX() + (double)0.5F, (double)(this.center.getY() + 2), (double)this.center.getZ() + (double)0.5F, 10, (double)4.5F, (double)2.0F, (double)4.5F, (double)0.0F, new int[0]);
                  EnumParticleTypes var45 = EnumParticleTypes.BLOCK_DUST;
                  double var10002 = (double)this.center.getX() + (double)0.5F;
                  double var46 = (double)this.center.getY() + (double)0.5F;
                  double var10004 = (double)this.center.getZ() + (double)0.5F;
                  int[] var47 = new int[1];
                  Block var48 = Blocks.BONE_BLOCK;
                  var47[0] = Block.getStateId(Blocks.BONE_BLOCK.getDefaultState());
                  ws.spawnParticle(var45, var10002, var46, var10004, 200, (double)7.5F, (double)0.5F, (double)7.5F, 0.1, var47);
               }

               world.playSound((EntityPlayer)null, this.center, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 4.0F, 0.4F);
               world.playSound((EntityPlayer)null, this.center, SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.HOSTILE, 3.0F, 0.6F);
            }

            if (this.erupted && ticksElapsed % 5 == 0 && world instanceof WorldServer) {
               WorldServer ws = (WorldServer)world;

               for(int i = 0; i < 20; ++i) {
                  double angle = world.rand.nextDouble() * Math.PI * (double)2.0F;
                  double r = world.rand.nextDouble() * (double)15.0F;
                  double x = (double)this.center.getX() + (double)0.5F + r * Math.cos(angle);
                  double z = (double)this.center.getZ() + (double)0.5F + r * Math.sin(angle);
                  ws.spawnParticle(EnumParticleTypes.CRIT, x, (double)this.center.getY() + world.rand.nextDouble() * (double)4.0F, z, 1, 0.1, 0.2, 0.1, 0.01, new int[0]);
               }
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.HOSTILE, 1.5F, 0.8F);
      }
   }

   static class BoneCageMechanic implements BossMechanic {
      private final RaidDifficulty difficulty;
      private EntityPlayer trappedPlayer;
      private BlockPos prisonLocation;
      private List<BlockPos> placedBlocks = new ArrayList();
      private int rescueProgress = 0;
      private static final int RESCUE_THRESHOLD = 100;
      private static final int PRISON_RADIUS = 2;
      private static final int PRISON_HEIGHT = 4;
      private static final int RESCUE_RADIUS = 3;
      private boolean playerFreed = false;

      public BoneCageMechanic(RaidDifficulty difficulty) {
         this.difficulty = difficulty;
      }

      public String getName() {
         return "bone_cage";
      }

      public String getDisplayName() {
         return "Bone Cage";
      }

      public String getWarningMessage() {
         return "Kimimaro prepares a bone prison!";
      }

      public int getWarningTicks() {
         return 60 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 200;
      }

      public int getSelectionWeight() {
         return 8;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         if (raid != null) {
            this.placedBlocks.clear();
            this.rescueProgress = 0;
            this.playerFreed = false;
            KimimaroMechanics.setActiveBoneCage(this, boss.world, raid.getRaidId());
            List<EntityPlayerMP> players = raid.getParticipants();
            if (!players.isEmpty()) {
               this.trappedPlayer = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
               this.prisonLocation = this.findSafePrisonLocation(boss.world, this.trappedPlayer.getPosition());
               this.buildPrison(boss.world);
               raid.trackBlocks(this.placedBlocks);
               raid.broadcastMessage("§c" + this.trappedPlayer.getName() + " has been trapped in a bone cage!");
               raid.broadcastMessage("§e§lTeammates: Attack the bone cage to free them!");
            }

            boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.HOSTILE, 2.0F, 0.5F);
         }
      }

      private void buildPrison(World world) {
         if (this.prisonLocation != null) {
            int[][] corners = new int[][]{{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};

            for(int[] corner : corners) {
               int groundY = this.findGroundY(world, this.prisonLocation.add(corner[0], 0, corner[1]));

               for(int y = groundY; y < this.prisonLocation.getY() + 4; ++y) {
                  BlockPos pos = new BlockPos(this.prisonLocation.getX() + corner[0], y, this.prisonLocation.getZ() + corner[1]);
                  if (world.isAirBlock(pos)) {
                     world.setBlockState(pos, Blocks.BONE_BLOCK.getDefaultState());
                     this.placedBlocks.add(pos);
                  }
               }
            }

            for(int x = -1; x < 2; ++x) {
               int groundY = this.findGroundY(world, this.prisonLocation.add(x, 0, -2));

               for(int y = groundY; y < this.prisonLocation.getY() + 4; ++y) {
                  BlockPos pos = new BlockPos(this.prisonLocation.getX() + x, y, this.prisonLocation.getZ() - 2);
                  if (world.isAirBlock(pos)) {
                     world.setBlockState(pos, Blocks.IRON_BARS.getDefaultState());
                     this.placedBlocks.add(pos);
                  }
               }
            }

            for(int x = -1; x < 2; ++x) {
               int groundY = this.findGroundY(world, this.prisonLocation.add(x, 0, 2));

               for(int y = groundY; y < this.prisonLocation.getY() + 4; ++y) {
                  BlockPos pos = new BlockPos(this.prisonLocation.getX() + x, y, this.prisonLocation.getZ() + 2);
                  if (world.isAirBlock(pos)) {
                     world.setBlockState(pos, Blocks.IRON_BARS.getDefaultState());
                     this.placedBlocks.add(pos);
                  }
               }
            }

            for(int z = -1; z < 2; ++z) {
               int groundY = this.findGroundY(world, this.prisonLocation.add(-2, 0, z));

               for(int y = groundY; y < this.prisonLocation.getY() + 4; ++y) {
                  BlockPos pos = new BlockPos(this.prisonLocation.getX() - 2, y, this.prisonLocation.getZ() + z);
                  if (world.isAirBlock(pos)) {
                     world.setBlockState(pos, Blocks.IRON_BARS.getDefaultState());
                     this.placedBlocks.add(pos);
                  }
               }
            }

            for(int z = -1; z < 2; ++z) {
               int groundY = this.findGroundY(world, this.prisonLocation.add(2, 0, z));

               for(int y = groundY; y < this.prisonLocation.getY() + 4; ++y) {
                  BlockPos pos = new BlockPos(this.prisonLocation.getX() + 2, y, this.prisonLocation.getZ() + z);
                  if (world.isAirBlock(pos)) {
                     world.setBlockState(pos, Blocks.IRON_BARS.getDefaultState());
                     this.placedBlocks.add(pos);
                  }
               }
            }

            for(int x = -2; x <= 2; ++x) {
               for(int z = -2; z <= 2; ++z) {
                  BlockPos pos = this.prisonLocation.add(x, 3, z);
                  if (world.isAirBlock(pos)) {
                     world.setBlockState(pos, Blocks.IRON_BARS.getDefaultState());
                     this.placedBlocks.add(pos);
                  }

                  BlockPos pos2 = this.prisonLocation.add(x, 4, z);
                  if (world.isAirBlock(pos2)) {
                     world.setBlockState(pos2, Blocks.IRON_BARS.getDefaultState());
                     this.placedBlocks.add(pos2);
                  }
               }
            }

         }
      }

      private int findGroundY(World world, BlockPos pos) {
         for(int y = pos.getY(); y > pos.getY() - 10; --y) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            BlockPos belowPos = checkPos.down();
            if (world.isAirBlock(checkPos) && !world.isAirBlock(belowPos)) {
               return y;
            }

            if (!world.isAirBlock(checkPos)) {
               return y + 1;
            }
         }

         return pos.getY();
      }

      private BlockPos findSafePrisonLocation(World world, BlockPos pos) {
         int x = pos.getX();
         int z = pos.getZ();

         for(int y = pos.getY(); y < pos.getY() + 10 && y < 255; ++y) {
            boolean feetClear = !world.getBlockState(new BlockPos(x, y, z)).getMaterial().isSolid();
            boolean headClear = !world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial().isSolid();
            boolean hasFloor = world.getBlockState(new BlockPos(x, y - 1, z)).getMaterial().isSolid();
            if (feetClear && headClear && hasFloor) {
               return new BlockPos(x, y, z);
            }
         }

         return pos;
      }

      private void removePrison(World world) {
         for(BlockPos pos : this.placedBlocks) {
            if (world.getBlockState(pos).getBlock() == Blocks.BONE_BLOCK || world.getBlockState(pos).getBlock() == Blocks.IRON_BARS) {
               world.setBlockToAir(pos);
               if (world instanceof WorldServer) {
                  ((WorldServer)world).spawnParticle(EnumParticleTypes.CLOUD, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, 3, 0.2, 0.2, 0.2, 0.02, new int[0]);
               }
            }
         }

         this.placedBlocks.clear();
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (this.trappedPlayer != null && !this.trappedPlayer.isDead && !this.playerFreed) {
            if (this.trappedPlayer instanceof EntityPlayerMP) {
               ((EntityPlayerMP)this.trappedPlayer).connection.setPlayerLocation((double)this.prisonLocation.getX() + (double)0.5F, (double)this.prisonLocation.getY(), (double)this.prisonLocation.getZ() + (double)0.5F, this.trappedPlayer.rotationYaw, this.trappedPlayer.rotationPitch);
            } else {
               this.trappedPlayer.setPositionAndUpdate((double)this.prisonLocation.getX() + (double)0.5F, (double)this.prisonLocation.getY(), (double)this.prisonLocation.getZ() + (double)0.5F);
            }

            this.trappedPlayer.motionX = (double)0.0F;
            this.trappedPlayer.motionY = (double)0.0F;
            this.trappedPlayer.motionZ = (double)0.0F;
            this.trappedPlayer.velocityChanged = true;
            if (raid != null) {
               for(EntityPlayerMP player : raid.getParticipants()) {
                  if (player != this.trappedPlayer && !player.isDead) {
                     double dist = player.getDistance((double)this.prisonLocation.getX(), (double)this.prisonLocation.getY(), (double)this.prisonLocation.getZ());
                     if (dist <= (double)5.0F && player.isSwingInProgress) {
                        this.rescueProgress += 5;
                        if (boss.world instanceof WorldServer) {
                           WorldServer ws = (WorldServer)boss.world;
                           ws.spawnParticle(EnumParticleTypes.CRIT, (double)this.prisonLocation.getX() + (double)0.5F, (double)this.prisonLocation.getY() + (double)1.5F, (double)this.prisonLocation.getZ() + (double)0.5F, 5, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
                        }

                        boss.world.playSound((EntityPlayer)null, this.prisonLocation, SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.BLOCKS, 0.8F, 1.0F + boss.world.rand.nextFloat() * 0.2F);
                        int progressPercent = this.rescueProgress * 100 / 100;
                        if (this.rescueProgress == 25 || this.rescueProgress == 50 || this.rescueProgress == 75) {
                           raid.broadcastMessage("§e[RESCUE] Bone cage integrity: " + (100 - progressPercent) + "%");
                        }
                     }
                  }
               }

               if (this.rescueProgress >= 100) {
                  this.playerFreed = true;
                  raid.broadcastMessage("§a§l" + this.trappedPlayer.getName() + " has been rescued by teammates!");
                  boss.world.playSound((EntityPlayer)null, this.prisonLocation, SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.BLOCKS, 2.0F, 0.8F);
                  this.removePrison(boss.world);
                  if (boss.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)boss.world;
                     ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)this.prisonLocation.getX() + (double)0.5F, (double)(this.prisonLocation.getY() + 1), (double)this.prisonLocation.getZ() + (double)0.5F, 30, (double)1.0F, (double)1.0F, (double)1.0F, 0.1, new int[0]);
                  }

                  return;
               }
            }

            if (ticksElapsed % 5 == 0 && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;
               EnumParticleTypes particleType = EnumParticleTypes.CRIT;
               if (this.rescueProgress > 75) {
                  particleType = EnumParticleTypes.FLAME;
               } else if (this.rescueProgress > 50) {
                  particleType = EnumParticleTypes.CRIT_MAGIC;
               }

               for(int angle = 0; angle < 360; angle += 90) {
                  double rad = Math.toRadians((double)angle);
                  double x = (double)this.prisonLocation.getX() + (double)0.5F + (double)2.0F * Math.cos(rad);
                  double z = (double)this.prisonLocation.getZ() + (double)0.5F + (double)2.0F * Math.sin(rad);

                  for(int yOffset = 0; yOffset < 4; ++yOffset) {
                     world.spawnParticle(particleType, x, (double)(this.prisonLocation.getY() + yOffset), z, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  }
               }
            }

            if (ticksElapsed % 40 == 0) {
               float dmg = 10.0F * this.difficulty.getDamageMultiplier();
               KimimaroMechanics.applyDamageWithTrueComponent(this.trappedPlayer, dmg, DamageSource.MAGIC);
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         if (raid != null) {
            KimimaroMechanics.clearActiveBoneCage(raid.getRaidId());
         } else {
            KimimaroMechanics.clearActiveBoneCage();
         }

         if (this.trappedPlayer != null && raid != null && !this.playerFreed) {
            raid.broadcastMessage("§a" + this.trappedPlayer.getName() + " has been freed from the bone cage!");
         }

         if (!this.playerFreed) {
            this.removePrison(boss.world);
            if (this.prisonLocation != null) {
               boss.world.playSound((EntityPlayer)null, this.prisonLocation, SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.BLOCKS, 1.5F, 0.8F);
            }
         }

         this.trappedPlayer = null;
         this.prisonLocation = null;
         this.playerFreed = false;
         this.rescueProgress = 0;
      }

      public void onInterrupt(EntityLivingBase boss, RaidInstance raid) {
         if (raid != null) {
            KimimaroMechanics.clearActiveBoneCage(raid.getRaidId());
         } else {
            KimimaroMechanics.clearActiveBoneCage();
         }

         if (boss.world != null) {
            this.removePrison(boss.world);
         }

         this.trappedPlayer = null;
         this.prisonLocation = null;
         this.playerFreed = false;
         this.rescueProgress = 0;
      }

      public void forceCleanup(World world) {
         if (world != null) {
            this.removePrison(world);
         }

         this.trappedPlayer = null;
         this.prisonLocation = null;
      }
   }

   private static class DigitalShrapnelMechanic implements BossMechanic {
      private final float damage;
      private final RaidDifficulty difficulty;
      private int ticksSinceLastWave = 0;

      public DigitalShrapnelMechanic(float damage, RaidDifficulty difficulty) {
         this.damage = damage;
         this.difficulty = difficulty;
      }

      public String getName() {
         return "digital_shrapnel";
      }

      public String getDisplayName() {
         return "Digital Shrapnel";
      }

      public String getWarningMessage() {
         return "Kimimaro's body bristles with bone shards!";
      }

      public int getWarningTicks() {
         return 40 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 120;
      }

      public int getSelectionWeight() {
         return 10;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.ticksSinceLastWave = 0;
         if (raid != null) {
            raid.broadcastMessage("§c§lKimimaro launches a storm of bone shrapnel!");
            raid.broadcastMessage("§eKeep moving! Dodge the projectiles!");
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 2.0F, 1.8F);
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            int waveInterval = 15 - this.difficulty.ordinal() * 2;
            ++this.ticksSinceLastWave;
            if (this.ticksSinceLastWave >= waveInterval) {
               this.ticksSinceLastWave = 0;
               List<EntityPlayerMP> players = raid.getParticipants();
               if (players.isEmpty()) {
                  return;
               }

               float trueDmg = this.damage * 0.3F;
               int bulletsPerWave = 3 + this.difficulty.ordinal();

               for(EntityPlayerMP target : players) {
                  if (!target.isDead) {
                     double dx = target.posX - boss.posX;
                     double dy = target.posY + (double)target.getEyeHeight() - (boss.posY + (double)boss.getEyeHeight());
                     double dz = target.posZ - boss.posZ;

                     for(int i = 0; i < bulletsPerWave; ++i) {
                        EntityFingerBullet.EntityCustom bullet = new EntityFingerBullet.EntityCustom(boss.world, boss, this.damage, trueDmg);
                        bullet.shoot(dx + (boss.world.rand.nextDouble() - (double)0.5F) * (double)4.0F, dy + (boss.world.rand.nextDouble() - (double)0.5F) * (double)1.5F, dz + (boss.world.rand.nextDouble() - (double)0.5F) * (double)4.0F, 1.8F, 4.0F);
                        boss.world.spawnEntity(bullet);
                        raid.trackSpawnedEntity(bullet);
                     }
                  }
               }

               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_SKELETON_SHOOT, SoundCategory.HOSTILE, 1.2F, 1.5F + boss.world.rand.nextFloat() * 0.5F);
               if (boss.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)boss.world;
                  ws.spawnParticle(EnumParticleTypes.CRIT, boss.posX, boss.posY + (double)1.0F, boss.posZ, 15, (double)1.0F, (double)0.5F, (double)1.0F, 0.2, new int[0]);
               }
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.HOSTILE, 1.0F, 1.2F);
      }
   }
}
