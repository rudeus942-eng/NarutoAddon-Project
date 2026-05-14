
package net.luck.narutoaddon.OtherCode.raid.boss.bosses.itachi;

import net.luck.narutoaddon.OtherCode.entity.EntityItachiStyleNPC;
import net.luck.narutoaddon.OtherCode.entity.EntityKatonFireball;
import net.luck.narutoaddon.OtherCode.entity.EntityRaidBossItachi;
import net.luck.narutoaddon.OtherCode.jutsu.EntityPhoenixSageFire;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.raid.boss.BossMechanic;
import net.luck.narutoaddon.OtherCode.raid.boss.mechanics.SummonMechanic;
import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.luck.narutoaddon.OtherCode.raid.util.KnockbackHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.*;

public class ItachiMechanics {
   private static final float TRUE_DAMAGE_PERCENT = 0.3F;
   private static final DamageSource TRUE_DAMAGE_SOURCE = (new DamageSource("itachi_true")).setDamageBypassesArmor().setMagicDamage();
   private static Map<Integer, Entity> activeSusanooByRaid = new HashMap();

   public static void applyDamageWithTrueComponent(EntityLivingBase target, float totalDamage, DamageSource normalSource) {
      float trueDamage = totalDamage * 0.3F;
      float normalDamage = totalDamage * 0.7F;
      target.hurtResistantTime = 0;
      target.attackEntityFrom(normalSource, normalDamage);
      target.hurtResistantTime = 0;
      target.attackEntityFrom(TRUE_DAMAGE_SOURCE, trueDamage);
   }

   public static void setActiveSusanoo(Entity susanoo, int raidId) {
      activeSusanooByRaid.put(raidId, susanoo);
   }

   public static void clearActiveSusanoo(int raidId) {
      Entity susanoo = (Entity)activeSusanooByRaid.remove(raidId);
      if (susanoo != null && !susanoo.isDead) {
         susanoo.setDead();
      }

   }

   public static void resetMechanicState(int raidId) {
      clearActiveSusanoo(raidId);
      System.out.println("[ItachiMechanics] Reset mechanic state for raid #" + raidId);
   }

   public static void resetMechanicState() {
      for(Map.Entry<Integer, Entity> entry : activeSusanooByRaid.entrySet()) {
         if (entry.getValue() != null && !((Entity)entry.getValue()).isDead) {
            ((Entity)entry.getValue()).setDead();
         }
      }

      activeSusanooByRaid.clear();
   }

   public static BossMechanic createKatonFireball(RaidDifficulty difficulty) {
      float damage = 12.0F * difficulty.getDamageMultiplier();
      int radius = 8 + difficulty.ordinal() * 2;
      return new KatonFireballMechanic(damage, radius, difficulty);
   }

   public static BossMechanic createShurikenBarrage(RaidDifficulty difficulty) {
      float damage = 8.0F * difficulty.getDamageMultiplier();
      int shurikenCount = 5 + difficulty.ordinal() * 2;
      return new ShurikenBarrageMechanic(damage, shurikenCount, difficulty);
   }

   public static BossMechanic createCrowSubstitution(RaidDifficulty difficulty) {
      return new CrowSubstitutionMechanic(difficulty);
   }

   public static BossMechanic createAmaterasu(RaidDifficulty difficulty) {
      float damage = 15.0F * difficulty.getDamageMultiplier();
      return new AmaterasuMechanic(damage, difficulty, false);
   }

   public static BossMechanic createEnhancedAmaterasu(RaidDifficulty difficulty) {
      float damage = 22.0F * difficulty.getDamageMultiplier();
      return new AmaterasuMechanic(damage, difficulty, true);
   }

   public static BossMechanic createTsukuyomi(RaidDifficulty difficulty) {
      float failDamage = 40.0F * difficulty.getDamageMultiplier();
      return new TsukuyomiMechanic(failDamage, difficulty);
   }

   public static BossMechanic createCrowCloneSwarm(RaidDifficulty difficulty) {
      int cloneCount = difficulty.getCloneCount();
      float cloneHealth = 250.0F * difficulty.getHealthMultiplier();
      float cloneDamage = 6.0F * difficulty.getDamageMultiplier();
      return new SummonMechanic("crow_clone_swarm", "Crow Clones", 50 - difficulty.getWarningTimeReduction(), 600, cloneCount, true, 200, (world, boss) -> {
         EntityItachiStyleNPC.EntityCustom clone = new EntityItachiStyleNPC.EntityCustom(world);
         NpcConfig itachiConfig = NpcConfigRegistry.get("arc11_itachi_phantom");
         if (itachiConfig != null) {
            clone.applyNpcConfig(itachiConfig);
         }

         clone.setNoAI(false);
         clone.setLeashRange((double)99999.0F);
         clone.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)cloneHealth);
         clone.setHealth(cloneHealth);
         clone.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)cloneDamage);
         clone.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35);
         clone.setCustomNameTag("§4Itachi's Crow Clone");
         clone.setAlwaysRenderNameTag(true);
         clone.setTextureOverride("inftsukaddon:textures/itachi_raid_phase2.png");
         clone.enablePersistence();
         if (world instanceof WorldServer) {
            ((WorldServer)world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, clone.posX, clone.posY + (double)1.0F, clone.posZ, 30, 0.8, (double)1.0F, 0.8, 0.05, new int[0]);
            ((WorldServer)world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, clone.posX, clone.posY + (double)1.0F, clone.posZ, 15, (double)0.5F, 0.8, (double)0.5F, 0.02, new int[0]);
         }

         return clone;
      }) {
         protected void spawnSummonParticles(EntityLivingBase boss) {
            if (boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;

               for(Entity summon : this.getSummonedEntities()) {
                  if (!summon.isDead) {
                     world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, summon.posX, summon.posY + (double)2.0F, summon.posZ, 3, 0.3, 0.3, 0.3, 0.02, new int[0]);
                  }
               }
            }

         }
      };
   }

   public static BossMechanic createYasakaMagatama(RaidDifficulty difficulty) {
      float damage = 35.0F * difficulty.getDamageMultiplier();
      return new YasakaMagatamaMechanic(damage, difficulty);
   }

   public static BossMechanic createGenjutsuRealm(RaidDifficulty difficulty) {
      float damage = 20.0F * difficulty.getDamageMultiplier();
      int safeZones = 4 - difficulty.ordinal();
      if (safeZones < 2) {
         safeZones = 2;
      }

      return new GenjutsuRealmMechanic(damage, difficulty, safeZones);
   }

   public static BossMechanic createTotsukaBlade(RaidDifficulty difficulty) {
      float damage = 45.0F * difficulty.getDamageMultiplier();
      return new TotsukaBladeAnMechanic(damage, difficulty);
   }

   public static BossMechanic createYataMirror(RaidDifficulty difficulty) {
      return new YataMirrorMechanic(difficulty);
   }

   public static BossMechanic createYasakaMagatamaBarrage(RaidDifficulty difficulty) {
      float damage = 30.0F * difficulty.getDamageMultiplier();
      int safeZones = 3 - Math.min(difficulty.ordinal(), 2);
      if (safeZones < 1) {
         safeZones = 1;
      }

      return new YasakaBarrageMechanic(damage, difficulty, safeZones);
   }

   private static class KatonFireballMechanic implements BossMechanic {
      private final float damage;
      private final int radius;
      private final RaidDifficulty difficulty;
      private EntityPlayer targetPlayer;
      private boolean firedMain = false;
      private boolean firedPhoenix = false;

      public KatonFireballMechanic(float damage, int radius, RaidDifficulty difficulty) {
         this.damage = damage;
         this.radius = radius;
         this.difficulty = difficulty;
      }

      public String getName() {
         return "katon_fireball";
      }

      public String getDisplayName() {
         return "Katon: Great Fireball";
      }

      public String getWarningMessage() {
         return "Itachi prepares Katon: Gokakyu no Jutsu!";
      }

      public int getWarningTicks() {
         return 50 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 60;
      }

      public int getSelectionWeight() {
         return 12;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         if (raid != null) {
            this.firedMain = false;
            this.firedPhoenix = false;
            List<EntityPlayerMP> players = raid.getParticipants();
            if (!players.isEmpty()) {
               this.targetPlayer = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
               if (raid != null) {
                  raid.broadcastMessage("§c§lKaton: Great Fireball targets " + this.targetPlayer.getName() + "!");
               }
            }

            SoundEvent flamethrow = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "flamethrow"));
            if (flamethrow != null) {
               boss.world.playSound((EntityPlayer)null, boss.posX, boss.posY, boss.posZ, flamethrow, SoundCategory.HOSTILE, 2.5F, 0.8F);
            } else {
               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 2.5F, 0.6F);
            }

         }
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null && this.targetPlayer != null && !this.targetPlayer.isDead) {
            if (ticksElapsed == 5 && !this.firedMain) {
               this.firedMain = true;
               float normalDmg = this.damage;
               float trueDmg = this.damage * 0.3F;
               EntityKatonFireball.EntityCustom fireball = new EntityKatonFireball.EntityCustom(boss.world, boss, normalDmg, trueDmg);
               fireball.setPosition(boss.posX, boss.posY + (double)boss.getEyeHeight(), boss.posZ);
               double dx = this.targetPlayer.posX - boss.posX;
               double dy = this.targetPlayer.posY + (double)this.targetPlayer.getEyeHeight() - (boss.posY + (double)boss.getEyeHeight());
               double dz = this.targetPlayer.posZ - boss.posZ;
               fireball.shoot(dx, dy, dz, 1.4F, 0.5F);
               boss.world.spawnEntity(fireball);
               if (raid != null) {
                  raid.trackSpawnedEntity(fireball);
               }

               if (boss.world instanceof WorldServer) {
                  ((WorldServer)boss.world).spawnParticle(EnumParticleTypes.FLAME, boss.posX, boss.posY + (double)boss.getEyeHeight(), boss.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.05, new int[0]);
               }
            }

            if (ticksElapsed == 25 && !this.firedPhoenix) {
               this.firedPhoenix = true;
               int count = 3 + this.difficulty.ordinal();
               float normalDmg = this.damage * 0.4F;
               float trueDmg = this.damage * 0.12F;

               for(int i = 0; i < count; ++i) {
                  EntityKatonFireball.EntityCustom fb = new EntityKatonFireball.EntityCustom(boss.world, boss, normalDmg, trueDmg);
                  fb.setPosition(boss.posX, boss.posY + (double)boss.getEyeHeight(), boss.posZ);
                  double dx = this.targetPlayer.posX - boss.posX + (boss.world.rand.nextDouble() - (double)0.5F) * (double)1.5F;
                  double dy = this.targetPlayer.posY + (double)this.targetPlayer.getEyeHeight() - (boss.posY + (double)boss.getEyeHeight());
                  double dz = this.targetPlayer.posZ - boss.posZ + (boss.world.rand.nextDouble() - (double)0.5F) * (double)1.5F;
                  fb.shoot(dx, dy, dz, 1.2F, 1.0F);
                  boss.world.spawnEntity(fb);
                  if (raid != null) {
                     raid.trackSpawnedEntity(fb);
                  }
               }

               if (boss.world instanceof WorldServer) {
                  ((WorldServer)boss.world).spawnParticle(EnumParticleTypes.FLAME, boss.posX, boss.posY + (double)boss.getEyeHeight(), boss.posZ, 30, 0.8, (double)0.5F, 0.8, 0.08, new int[0]);
               }
            }

            if (ticksElapsed % 15 == 0) {
               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.HOSTILE, 1.5F, 0.8F + boss.world.rand.nextFloat() * 0.4F);
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         this.targetPlayer = null;
         this.firedMain = false;
         this.firedPhoenix = false;
      }
   }

   private static class ShurikenBarrageMechanic implements BossMechanic {
      private final float damage;
      private final int shurikenCount;
      private final RaidDifficulty difficulty;
      private EntityPlayer targetPlayer;

      public ShurikenBarrageMechanic(float damage, int shurikenCount, RaidDifficulty difficulty) {
         this.damage = damage;
         this.shurikenCount = shurikenCount;
         this.difficulty = difficulty;
      }

      public String getName() {
         return "shuriken_barrage";
      }

      public String getDisplayName() {
         return "Phoenix Sage Fire Barrage";
      }

      public String getWarningMessage() {
         return "Itachi readies Phoenix Sage Fire Technique!";
      }

      public int getWarningTicks() {
         return 30 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 30;
      }

      public int getSelectionWeight() {
         return 14;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         if (raid != null) {
            List<EntityPlayerMP> players = raid.getParticipants();
            if (!players.isEmpty()) {
               this.targetPlayer = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
            }

            SoundEvent flamethrow = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "flamethrow"));
            if (flamethrow != null) {
               boss.world.playSound((EntityPlayer)null, boss.posX, boss.posY, boss.posZ, flamethrow, SoundCategory.HOSTILE, 1.5F, 1.2F);
            } else {
               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 2.0F, 1.2F);
            }

         }
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            if (ticksElapsed < 10 && ticksElapsed % 2 == 0) {
               int batch = this.shurikenCount / 5;
               if (batch < 1) {
                  batch = 1;
               }

               for(int i = 0; i < batch; ++i) {
                  List<EntityPlayerMP> players = raid.getParticipants();
                  if (players.isEmpty()) {
                     return;
                  }

                  EntityPlayer target = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
                  EntityPhoenixSageFire.EntityCustom fireball = new EntityPhoenixSageFire.EntityCustom(boss.world, boss);
                  fireball.setDamage(this.damage);
                  double dx = target.posX - boss.posX + (boss.world.rand.nextDouble() - (double)0.5F) * (double)2.0F;
                  double dy = target.posY + (double)(target.height / 2.0F) - (boss.posY + (double)boss.getEyeHeight());
                  double dz = target.posZ - boss.posZ + (boss.world.rand.nextDouble() - (double)0.5F) * (double)2.0F;
                  fireball.shoot(dx, dy, dz, 1.3F, 1.0F);
                  fireball.setPosition(boss.posX, boss.posY + (double)boss.getEyeHeight(), boss.posZ);
                  boss.world.spawnEntity(fireball);
                  if (raid != null) {
                     raid.trackSpawnedEntity(fireball);
                  }

                  if (boss.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)boss.world;
                     ws.spawnParticle(EnumParticleTypes.FLAME, boss.posX, boss.posY + (double)boss.getEyeHeight(), boss.posZ, 5, 0.3, 0.3, 0.3, 0.05, new int[0]);
                  }
               }

               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 0.8F, 1.2F + boss.world.rand.nextFloat() * 0.3F);
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         this.targetPlayer = null;
      }
   }

   private static class CrowSubstitutionMechanic implements BossMechanic {
      private final RaidDifficulty difficulty;
      private BlockPos teleportDest;
      private boolean hasTeleported = false;
      private static final int IMMUNITY_TICKS = 20;

      public CrowSubstitutionMechanic(RaidDifficulty difficulty) {
         this.difficulty = difficulty;
      }

      public String getName() {
         return "crow_substitution";
      }

      public String getDisplayName() {
         return "Crow Substitution";
      }

      public String getWarningMessage() {
         return "Itachi forms a hand seal...";
      }

      public int getWarningTicks() {
         return 20 - Math.min(this.difficulty.getWarningTimeReduction(), 10);
      }

      public int getDurationTicks() {
         return 30;
      }

      public int getSelectionWeight() {
         return 8;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public boolean allowsDamage() {
         return this.hasTeleported;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.hasTeleported = false;
         if (raid != null) {
            double oldX = boss.posX;
            double oldY = boss.posY;
            double oldZ = boss.posZ;
            List<EntityPlayerMP> players = raid.getParticipants();
            double destX;
            double destY;
            double destZ;
            if (!players.isEmpty()) {
               EntityPlayer target = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
               double angle = Math.toRadians((double)(target.rotationYaw + 180.0F));
               double dist = (double)(3 + boss.world.rand.nextInt(4));
               destX = target.posX + dist * Math.sin(angle);
               destZ = target.posZ - dist * Math.cos(angle);
               destY = target.posY;
            } else {
               double angle = boss.world.rand.nextDouble() * Math.PI * (double)2.0F;
               double dist = (double)8.0F + boss.world.rand.nextDouble() * (double)5.0F;
               destX = boss.posX + Math.cos(angle) * dist;
               destZ = boss.posZ + Math.sin(angle) * dist;
               destY = boss.posY;
            }

            if (boss instanceof EntityRaidBossItachi.EntityCustom) {
               EntityRaidBossItachi.EntityCustom itachi = (EntityRaidBossItachi.EntityCustom)boss;
               if (itachi.getRaidInstance() != null && itachi.getRaidInstance().getArena() != null) {
                  AxisAlignedBB bounds = itachi.getRaidInstance().getArena().getBounds();
                  double margin = (double)1.0F;
                  destX = Math.max(bounds.minX + margin, Math.min(bounds.maxX - margin, destX));
                  destZ = Math.max(bounds.minZ + margin, Math.min(bounds.maxZ - margin, destZ));
               }
            }

            BlockPos destBlock = new BlockPos(destX, destY, destZ);
            boolean foundSafe = false;

            for(int dy = 0; dy <= 5; ++dy) {
               for(int sign : new int[]{0, -1, 1}) {
                  int checkY = destBlock.getY() + dy * (sign == 0 ? 0 : sign);
                  if (checkY >= 1) {
                     BlockPos floor = new BlockPos(destX, (double)(checkY - 1), destZ);
                     BlockPos foot = new BlockPos(destX, (double)checkY, destZ);
                     BlockPos head = new BlockPos(destX, (double)(checkY + 1), destZ);
                     if (boss.world.getBlockState(floor).getMaterial().isSolid() && !boss.world.getBlockState(foot).getMaterial().isSolid() && !boss.world.getBlockState(head).getMaterial().isSolid()) {
                        destY = (double)checkY;
                        foundSafe = true;
                        break;
                     }

                     if (sign == 0) {
                        break;
                     }
                  }
               }

               if (foundSafe) {
                  break;
               }
            }

            this.teleportDest = new BlockPos(destX, destY, destZ);
            if (boss.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)boss.world;
               ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, oldX, oldY + (double)1.0F, oldZ, 40, (double)1.0F, (double)1.5F, (double)1.0F, 0.05, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, oldX, oldY + (double)1.0F, oldZ, 25, 0.8, (double)1.0F, 0.8, 0.1, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SPELL_MOB, oldX, oldY + (double)1.0F, oldZ, 40, (double)1.0F, (double)1.5F, (double)1.0F, (double)0.0F, new int[0]);
            }

            boss.world.playSound((EntityPlayer)null, new BlockPos(oldX, oldY, oldZ), SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.HOSTILE, 2.0F, 0.5F);
            boss.world.playSound((EntityPlayer)null, new BlockPos(oldX, oldY, oldZ), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.2F, 1.5F);
            Entity mount = boss.getRidingEntity();
            if (mount != null) {
               boss.dismountRidingEntity();
               mount.setPositionAndUpdate(destX, destY, destZ);
               boss.setPositionAndUpdate(destX, destY, destZ);
               boss.startRiding(mount, true);
            } else {
               boss.setPositionAndUpdate(destX, destY, destZ);
            }

            boss.motionX = (double)0.0F;
            boss.motionY = (double)0.0F;
            boss.motionZ = (double)0.0F;
            this.hasTeleported = true;
            if (boss.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)boss.world;
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, boss.posX, boss.posY + (double)1.0F, boss.posZ, 25, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, boss.posX, boss.posY + (double)1.0F, boss.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
            }

            if (raid != null) {
               raid.broadcastMessage("§7Itachi vanishes in a flock of crows!");
            }

         }
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (ticksElapsed < 20 && boss.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)boss.world;
            ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, boss.posX, boss.posY + (double)1.0F, boss.posZ, 5, (double)0.5F, 0.8, (double)0.5F, 0.02, new int[0]);
         }

      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         this.teleportDest = null;
         this.hasTeleported = true;
      }
   }

   private static class AmaterasuMechanic implements BossMechanic {
      private final float damage;
      private final RaidDifficulty difficulty;
      private final boolean enhanced;
      private EntityPlayer targetPlayer;
      private BlockPos flameCenter;
      private int trackingTicks;
      private int radius;

      public AmaterasuMechanic(float damage, RaidDifficulty difficulty, boolean enhanced) {
         this.damage = damage;
         this.difficulty = difficulty;
         this.enhanced = enhanced;
         this.trackingTicks = enhanced ? 40 : 20;
         this.radius = enhanced ? 10 + difficulty.ordinal() * 2 : 6 + difficulty.ordinal() * 2;
      }

      public String getName() {
         return this.enhanced ? "enhanced_amaterasu" : "amaterasu";
      }

      public String getDisplayName() {
         return this.enhanced ? "Amaterasu: Inferno" : "Amaterasu";
      }

      public String getWarningMessage() {
         return this.enhanced ? "Itachi's eyes bleed... Amaterasu INFERNO!" : "Itachi's left eye bleeds... Amaterasu!";
      }

      public int getWarningTicks() {
         return 40 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return this.enhanced ? 120 : 80;
      }

      public int getSelectionWeight() {
         return this.enhanced ? 5 : 5;
      }

      public boolean isHeavyMechanic() {
         return true;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         if (raid != null) {
            List<EntityPlayerMP> players = raid.getParticipants();
            if (!players.isEmpty()) {
               this.targetPlayer = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
               this.flameCenter = this.targetPlayer.getPosition();
            } else {
               this.flameCenter = boss.getPosition();
            }

            if (raid != null) {
               String msg = this.enhanced ? "§4§l[AMATERASU INFERNO] Black flames engulf " + (this.targetPlayer != null ? this.targetPlayer.getName() : "the area") + "!" : "§4[AMATERASU] Black flames ignite on " + (this.targetPlayer != null ? this.targetPlayer.getName() : "the ground") + "!";
               raid.broadcastMessage(msg);
            }

            boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_BLAZE_AMBIENT, SoundCategory.HOSTILE, 2.5F, 0.3F);
            boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, 2.0F, 0.5F);
         }
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null && this.flameCenter != null) {
            if (ticksElapsed < this.trackingTicks && this.targetPlayer != null && !this.targetPlayer.isDead) {
               this.flameCenter = this.targetPlayer.getPosition();
            }

            if (ticksElapsed % 2 == 0 && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;

               for(int i = 0; i < (this.enhanced ? 60 : 35); ++i) {
                  double x = (double)this.flameCenter.getX() + (double)0.5F + (world.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)this.radius;
                  double z = (double)this.flameCenter.getZ() + (double)0.5F + (world.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)this.radius;
                  double distSq = (x - (double)this.flameCenter.getX() - (double)0.5F) * (x - (double)this.flameCenter.getX() - (double)0.5F) + (z - (double)this.flameCenter.getZ() - (double)0.5F) * (z - (double)this.flameCenter.getZ() - (double)0.5F);
                  if (distSq <= (double)(this.radius * this.radius)) {
                     double y = (double)this.flameCenter.getY() + world.rand.nextDouble() * (double)2.5F;
                     world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 1, 0.1, 0.2, 0.1, 0.01, new int[0]);
                     if (world.rand.nextFloat() < 0.3F) {
                        world.spawnParticle(EnumParticleTypes.LAVA, x, y, z, 1, 0.1, 0.1, 0.1, (double)0.0F, new int[0]);
                     }
                  }
               }

               for(int angle = 0; angle < 360; angle += 12) {
                  double rad = Math.toRadians((double)angle);
                  double px = (double)this.flameCenter.getX() + (double)0.5F + (double)this.radius * Math.cos(rad);
                  double pz = (double)this.flameCenter.getZ() + (double)0.5F + (double)this.radius * Math.sin(rad);
                  world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, px, (double)this.flameCenter.getY() + 0.3, pz, 1, (double)0.0F, 0.05, (double)0.0F, (double)0.0F, new int[0]);
               }
            }

            if (ticksElapsed % 15 == 0) {
               Potion amaterasuPotion = (Potion)Potion.REGISTRY.getObject(new ResourceLocation("narutomod", "amaterasuflame"));

               for(EntityPlayer player : raid.getParticipants()) {
                  double distSq = player.getPosition().distanceSq(this.flameCenter);
                  if (distSq <= (double)(this.radius * this.radius)) {
                     ItachiMechanics.applyDamageWithTrueComponent(player, this.damage, DamageSource.IN_FIRE);
                     if (amaterasuPotion != null) {
                        int ampli = this.enhanced ? 3 : 2;
                        player.addPotionEffect(new PotionEffect(amaterasuPotion, 100, ampli));
                     } else {
                        player.setFire(this.enhanced ? 6 : 4);
                     }

                     player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
                  }
               }
            }

            if (ticksElapsed % 15 == 0) {
               SoundEvent amaterasuSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "amaterasu2"));
               if (amaterasuSound != null) {
                  boss.world.playSound((EntityPlayer)null, (double)this.flameCenter.getX(), (double)this.flameCenter.getY(), (double)this.flameCenter.getZ(), amaterasuSound, SoundCategory.HOSTILE, 1.5F, 1.0F);
               } else {
                  boss.world.playSound((EntityPlayer)null, this.flameCenter, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.HOSTILE, 2.0F, 0.4F);
               }
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 2.0F, 0.8F);
         if (boss.world instanceof WorldServer && this.flameCenter != null) {
            WorldServer ws = (WorldServer)boss.world;
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)this.flameCenter.getX() + (double)0.5F, (double)(this.flameCenter.getY() + 1), (double)this.flameCenter.getZ() + (double)0.5F, 30, (double)this.radius / (double)2.0F, (double)1.0F, (double)this.radius / (double)2.0F, 0.05, new int[0]);
         }

         this.targetPlayer = null;
         this.flameCenter = null;
      }
   }

   private static class TsukuyomiMechanic implements BossMechanic {
      private final float failDamage;
      private final RaidDifficulty difficulty;
      private int escapeTimeTicks;
      private List<UUID> trappedPlayers = new ArrayList();

      public TsukuyomiMechanic(float failDamage, RaidDifficulty difficulty) {
         this.failDamage = failDamage;
         this.difficulty = difficulty;
         this.escapeTimeTicks = 80 - difficulty.ordinal() * 15;
      }

      public String getName() {
         return "tsukuyomi";
      }

      public String getDisplayName() {
         return "Tsukuyomi";
      }

      public String getWarningMessage() {
         return "§4Itachi's right eye glows crimson... TSUKUYOMI!";
      }

      public int getWarningTicks() {
         return 60 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return this.escapeTimeTicks + 20;
      }

      public int getSelectionWeight() {
         return 5;
      }

      public boolean isHeavyMechanic() {
         return true;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.trappedPlayers.clear();
         if (raid != null) {
            for(EntityPlayerMP player : raid.getParticipants()) {
               if (!player.isDead) {
                  this.trappedPlayers.add(player.getUniqueID());
                  player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, this.escapeTimeTicks + 40, 0));
                  player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, this.escapeTimeTicks + 40, 2));
                  player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, this.escapeTimeTicks + 40, 1));
                  player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, this.escapeTimeTicks + 40, 0));
               }
            }

            if (raid != null) {
               raid.broadcastMessage("§4§l[TSUKUYOMI] You are trapped in Itachi's genjutsu!");
               raid.broadcastMessage("§e§lBreak line of sight with Itachi to escape! (" + this.escapeTimeTicks / 20 + " seconds)");
            }

            boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 3.0F, 0.5F);
         }
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            if (ticksElapsed % 5 == 0 && ticksElapsed < this.escapeTimeTicks) {
               List<UUID> escaped = new ArrayList();

               for(UUID playerId : this.trappedPlayers) {
                  EntityPlayerMP player = null;

                  for(EntityPlayerMP p : raid.getParticipants()) {
                     if (p.getUniqueID().equals(playerId)) {
                        player = p;
                        break;
                     }
                  }

                  if (player != null && !player.isDead) {
                     boolean hasLineOfSight = boss.canEntityBeSeen(player);
                     boolean farEnough = boss.getDistanceSq(player) > (double)400.0F;
                     if (!hasLineOfSight || farEnough) {
                        escaped.add(playerId);
                        player.removePotionEffect(MobEffects.BLINDNESS);
                        player.removePotionEffect(MobEffects.SLOWNESS);
                        player.removePotionEffect(MobEffects.WEAKNESS);
                        player.removePotionEffect(MobEffects.NAUSEA);
                        raid.broadcastMessage("§a" + player.getName() + " broke free from Tsukuyomi!");
                        if (boss.world instanceof WorldServer) {
                           ((WorldServer)boss.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, player.posX, player.posY + (double)1.0F, player.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
                        }
                     }
                  } else {
                     escaped.add(playerId);
                  }
               }

               this.trappedPlayers.removeAll(escaped);
            }

            if (ticksElapsed % 3 == 0 && boss.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)boss.world;
               ws.spawnParticle(EnumParticleTypes.REDSTONE, boss.posX, boss.posY + (double)1.5F, boss.posZ, 10, (double)1.0F, (double)1.0F, (double)1.0F, (double)0.0F, new int[0]);
               double spiralAngle = Math.toRadians((double)(ticksElapsed * 15 % 360));
               double sx = boss.posX + (double)2.0F * Math.cos(spiralAngle);
               double sz = boss.posZ + (double)2.0F * Math.sin(spiralAngle);
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, sx, boss.posY + (double)1.0F + (double)(ticksElapsed % 20) / (double)10.0F, sz, 3, 0.1, 0.1, 0.1, 0.01, new int[0]);
            }

            if (ticksElapsed == this.escapeTimeTicks) {
               for(UUID playerId : this.trappedPlayers) {
                  EntityPlayerMP player = null;

                  for(EntityPlayerMP p : raid.getParticipants()) {
                     if (p.getUniqueID().equals(playerId)) {
                        player = p;
                        break;
                     }
                  }

                  if (player != null && !player.isDead) {
                     ItachiMechanics.applyDamageWithTrueComponent(player, this.failDamage, DamageSource.MAGIC);
                     if (raid != null) {
                        raid.broadcastMessage("§c" + player.getName() + " was consumed by Tsukuyomi!");
                     }

                     player.removePotionEffect(MobEffects.BLINDNESS);
                     player.removePotionEffect(MobEffects.SLOWNESS);
                     player.removePotionEffect(MobEffects.WEAKNESS);
                     player.removePotionEffect(MobEffects.NAUSEA);
                     if (boss.world instanceof WorldServer) {
                        ((WorldServer)boss.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, player.posX, player.posY + (double)1.0F, player.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
                     }
                  }
               }

               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.HOSTILE, 2.0F, 0.6F);
            }

            if (ticksElapsed == this.escapeTimeTicks - 40 && raid != null) {
               raid.broadcastMessage("§c§l[TSUKUYOMI] 2 seconds remaining!");
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         if (raid != null) {
            for(EntityPlayerMP player : raid.getParticipants()) {
               player.removePotionEffect(MobEffects.BLINDNESS);
               player.removePotionEffect(MobEffects.SLOWNESS);
               player.removePotionEffect(MobEffects.WEAKNESS);
               player.removePotionEffect(MobEffects.NAUSEA);
            }
         }

         this.trappedPlayers.clear();
      }

      public void onInterrupt(EntityLivingBase boss, RaidInstance raid) {
         this.onEnd(boss, raid);
      }
   }

   private static class YasakaMagatamaMechanic implements BossMechanic {
      private final float damage;
      private final RaidDifficulty difficulty;
      private List<BlockPos> safeZones = new ArrayList();
      private BlockPos bossStartPos;
      private float lockedYaw;

      public YasakaMagatamaMechanic(float damage, RaidDifficulty difficulty) {
         this.damage = damage;
         this.difficulty = difficulty;
      }

      public String getName() {
         return "yasaka_magatama";
      }

      public String getDisplayName() {
         return "Yasaka Magatama";
      }

      public String getWarningMessage() {
         return "§4Itachi's Susanoo charges Yasaka Magatama! Get BEHIND him!";
      }

      public int getWarningTicks() {
         return 80 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 100;
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
         return 10;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.safeZones.clear();
         this.bossStartPos = boss.getPosition();
         this.lockedYaw = boss.rotationYaw;
         if (boss instanceof EntityRaidBossItachi.EntityCustom) {
            ((EntityRaidBossItachi.EntityCustom)boss).setFacingLocked(true, this.lockedYaw);
         }

         double behindAngle = Math.toRadians((double)(this.lockedYaw + 180.0F));
         int safeX = (int)(boss.posX + (double)15.0F * Math.sin(behindAngle));
         int safeZ = (int)(boss.posZ - (double)15.0F * Math.cos(behindAngle));
         this.safeZones.add(new BlockPos(safeX, (int)boss.posY, safeZ));
         if (raid != null) {
            raid.broadcastMessage("§c§lYasaka Magatama incoming! Only 1 safe zone - BEHIND Itachi!");
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 2.5F, 0.7F);
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            if (boss instanceof EntityRaidBossItachi.EntityCustom) {
               EntityRaidBossItachi.EntityCustom itachi = (EntityRaidBossItachi.EntityCustom)boss;
               if (!itachi.isFacingLocked()) {
                  itachi.setFacingLocked(true, this.lockedYaw);
               }
            }

            if (ticksElapsed % 3 == 0 && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;

               for(int i = 0; i < 25; ++i) {
                  double angle = Math.toRadians((double)this.lockedYaw + (world.rand.nextDouble() - (double)0.5F) * (double)160.0F);
                  double distance = (double)5.0F + world.rand.nextDouble() * (double)35.0F;
                  double x = boss.posX + distance * Math.sin(angle);
                  double z = boss.posZ - distance * Math.cos(angle);
                  double y = boss.posY + (double)1.0F + world.rand.nextDouble() * (double)3.0F;
                  world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 1, 0.3, 0.3, 0.3, 0.02, new int[0]);
                  if (world.rand.nextFloat() < 0.3F) {
                     world.spawnParticle(EnumParticleTypes.LAVA, x, y, z, 1, 0.1, 0.1, 0.1, (double)0.0F, new int[0]);
                  }
               }

               for(BlockPos safeZone : this.safeZones) {
                  int safeRadius = this.getSafeZoneRadius();

                  for(int angle = 0; angle < 360; angle += 12) {
                     double rad = Math.toRadians((double)angle);
                     double px = (double)safeZone.getX() + (double)0.5F + (double)safeRadius * Math.cos(rad);
                     double pz = (double)safeZone.getZ() + (double)0.5F + (double)safeRadius * Math.sin(rad);
                     world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, px, (double)safeZone.getY() + (double)0.5F, pz, 2, 0.1, 0.2, 0.1, (double)0.0F, new int[0]);
                  }

                  for(int y = 0; y < 10; ++y) {
                     world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)safeZone.getX() + (double)0.5F, (double)(safeZone.getY() + y), (double)safeZone.getZ() + (double)0.5F, 1, 0.2, 0.1, 0.2, (double)0.0F, new int[0]);
                  }
               }
            }

            if (ticksElapsed % 20 == 0) {
               for(EntityPlayer player : raid.getParticipants()) {
                  if (!this.isInSafeZone(player.getPosition())) {
                     ItachiMechanics.applyDamageWithTrueComponent(player, this.damage, DamageSource.MAGIC);
                     double dx = player.posX - boss.posX;
                     double dz = player.posZ - boss.posZ;
                     double dist = Math.sqrt(dx * dx + dz * dz);
                     if (dist > (double)0.0F) {
                        KnockbackHelper.applyWallSafeKnockback(player, dx / dist * 0.8, 0.3, dz / dist * 0.8);
                     }

                     if (boss.world instanceof WorldServer) {
                        ((WorldServer)boss.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, player.posX, player.posY + (double)1.0F, player.posZ, 5, 0.3, 0.3, 0.3, (double)0.0F, new int[0]);
                     }
                  }
               }
            }

            if (ticksElapsed % 30 == 0) {
               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 0.9F);
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

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         this.safeZones.clear();
         if (boss instanceof EntityRaidBossItachi.EntityCustom) {
            ((EntityRaidBossItachi.EntityCustom)boss).setFacingLocked(false, 0.0F);
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0F, 1.2F);
         if (raid != null) {
            raid.broadcastMessage("§aYasaka Magatama has ended!");
         }

      }

      public void onInterrupt(EntityLivingBase boss, RaidInstance raid) {
         if (boss instanceof EntityRaidBossItachi.EntityCustom) {
            ((EntityRaidBossItachi.EntityCustom)boss).setFacingLocked(false, 0.0F);
         }

         this.safeZones.clear();
      }
   }

   private static class GenjutsuRealmMechanic implements BossMechanic {
      private final float damage;
      private final RaidDifficulty difficulty;
      private final int safeZoneCount;
      private List<BlockPos> safeZones = new ArrayList();
      private Map<BlockPos, IBlockState> originalBlocks = new HashMap();
      private boolean blocksPlaced = false;

      public GenjutsuRealmMechanic(float damage, RaidDifficulty difficulty, int safeZoneCount) {
         this.damage = damage;
         this.difficulty = difficulty;
         this.safeZoneCount = safeZoneCount;
      }

      public String getName() {
         return "genjutsu_realm";
      }

      public String getDisplayName() {
         return "Genjutsu Realm";
      }

      public String getWarningMessage() {
         return "The world around you shifts... Itachi's Genjutsu Realm!";
      }

      public int getWarningTicks() {
         return 80 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 160;
      }

      public int getSelectionWeight() {
         return 7;
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
         this.originalBlocks.clear();
         this.blocksPlaced = false;
         if (raid != null) {
            Random rand = boss.world.rand;

            for(int i = 0; i < this.safeZoneCount; ++i) {
               double angle = Math.toRadians((double)360.0F / (double)this.safeZoneCount * (double)i + (double)rand.nextInt(40));
               int distance = 12 + rand.nextInt(15);
               int x = (int)(boss.posX + (double)distance * Math.cos(angle));
               int z = (int)(boss.posZ + (double)distance * Math.sin(angle));
               this.safeZones.add(new BlockPos(x, (int)boss.posY, z));
            }

            int realmRadius = 25;
            int bossX = (int)boss.posX;
            int bossY = (int)boss.posY;
            int bossZ = (int)boss.posZ;

            for(int dx = -realmRadius; dx <= realmRadius; dx += 2) {
               for(int dz = -realmRadius; dz <= realmRadius; dz += 2) {
                  if (dx * dx + dz * dz <= realmRadius * realmRadius) {
                     BlockPos checkPos = new BlockPos(bossX + dx, bossY - 1, bossZ + dz);
                     if (!this.isInSafeZone(new BlockPos(bossX + dx, bossY, bossZ + dz)) && boss.world.getBlockState(checkPos).getMaterial().isSolid() && boss.world.isAirBlock(checkPos.up())) {
                        this.originalBlocks.put(checkPos.toImmutable(), boss.world.getBlockState(checkPos));
                        if (rand.nextFloat() < 0.3F) {
                           boss.world.setBlockState(checkPos, Blocks.SOUL_SAND.getDefaultState(), 2);
                        } else {
                           boss.world.setBlockState(checkPos, Blocks.NETHERRACK.getDefaultState(), 2);
                        }
                     }
                  }
               }
            }

            this.blocksPlaced = true;

            for(BlockPos safeZone : this.safeZones) {
               BlockPos markerPos = new BlockPos(safeZone.getX(), bossY - 1, safeZone.getZ());
               if (boss.world.getBlockState(markerPos).getMaterial().isSolid()) {
                  this.originalBlocks.put(markerPos.toImmutable(), boss.world.getBlockState(markerPos));
                  boss.world.setBlockState(markerPos, Blocks.GLOWSTONE.getDefaultState(), 2);
               }
            }

            raid.broadcastMessage("§5§l[GENJUTSU REALM] The world shifts! Find the safe zones! " + this.safeZoneCount + " exist!");
            boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 2.0F, 0.7F);
            boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.AMBIENT_CAVE, SoundCategory.HOSTILE, 3.0F, 0.5F);

            for(EntityPlayerMP player : raid.getParticipants()) {
               if (!player.isDead) {
                  player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 30, 0));
               }
            }

         }
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            if (ticksElapsed % 3 == 0 && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;

               for(int i = 0; i < 40; ++i) {
                  double x = boss.posX + (world.rand.nextDouble() - (double)0.5F) * (double)50.0F;
                  double z = boss.posZ + (world.rand.nextDouble() - (double)0.5F) * (double)50.0F;
                  BlockPos pos = new BlockPos(x, boss.posY, z);
                  if (!this.isInSafeZone(pos)) {
                     double y = boss.posY + (double)0.5F + world.rand.nextDouble() * (double)5.0F;
                     world.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                     if (world.rand.nextFloat() < 0.2F) {
                        world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, 1, 0.3, 0.3, 0.3, (double)0.5F, new int[0]);
                     }
                  }
               }

               for(BlockPos safeZone : this.safeZones) {
                  int safeRadius = this.getSafeZoneRadius();

                  for(int angle = 0; angle < 360; angle += 10) {
                     double rad = Math.toRadians((double)angle);
                     double px = (double)safeZone.getX() + (double)0.5F + (double)safeRadius * Math.cos(rad);
                     double pz = (double)safeZone.getZ() + (double)0.5F + (double)safeRadius * Math.sin(rad);
                     world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, px, (double)safeZone.getY() + (double)0.5F, pz, 2, 0.1, 0.2, 0.1, (double)0.0F, new int[0]);
                  }

                  for(int y = 0; y < 12; ++y) {
                     world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)safeZone.getX() + (double)0.5F, (double)(safeZone.getY() + y), (double)safeZone.getZ() + (double)0.5F, 1, 0.2, 0.1, 0.2, (double)0.0F, new int[0]);
                  }

                  for(int i = 0; i < 5; ++i) {
                     double ox = (world.rand.nextDouble() - (double)0.5F) * (double)safeRadius * (double)1.5F;
                     double oz = (world.rand.nextDouble() - (double)0.5F) * (double)safeRadius * (double)1.5F;
                     world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)safeZone.getX() + (double)0.5F + ox, (double)safeZone.getY() + (double)0.5F + world.rand.nextDouble() * (double)2.0F, (double)safeZone.getZ() + (double)0.5F + oz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  }
               }
            }

            if (ticksElapsed % 20 == 0) {
               for(EntityPlayer player : raid.getParticipants()) {
                  if (!this.isInSafeZone(player.getPosition())) {
                     ItachiMechanics.applyDamageWithTrueComponent(player, this.damage, DamageSource.MAGIC);
                     player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 30, 1));
                     player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 30, 0));
                  }
               }
            }

            if (ticksElapsed % 40 == 0) {
               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.AMBIENT_CAVE, SoundCategory.HOSTILE, 1.5F, 0.6F + boss.world.rand.nextFloat() * 0.3F);
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

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         this.restoreBlocks(boss.world);
         this.safeZones.clear();
         if (raid != null) {
            raid.broadcastMessage("§aThe Genjutsu Realm fades away!");
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 1.2F);
      }

      public void onInterrupt(EntityLivingBase boss, RaidInstance raid) {
         this.restoreBlocks(boss.world);
         this.safeZones.clear();
      }

      private void restoreBlocks(World world) {
         if (this.blocksPlaced) {
            for(Map.Entry<BlockPos, IBlockState> entry : this.originalBlocks.entrySet()) {
               world.setBlockState((BlockPos)entry.getKey(), (IBlockState)entry.getValue(), 2);
            }

            this.originalBlocks.clear();
            this.blocksPlaced = false;
         }
      }
   }

   private static class TotsukaBladeAnMechanic implements BossMechanic {
      private final float damage;
      private final RaidDifficulty difficulty;
      private float sweepYaw;
      private int sweepRadius;

      public TotsukaBladeAnMechanic(float damage, RaidDifficulty difficulty) {
         this.damage = damage;
         this.difficulty = difficulty;
         this.sweepRadius = 12 + difficulty.ordinal() * 3;
      }

      public String getName() {
         return "totsuka_blade";
      }

      public String getDisplayName() {
         return "Totsuka Blade";
      }

      public String getWarningMessage() {
         return "§4Susanoo raises the Totsuka Blade! GET AWAY!";
      }

      public int getWarningTicks() {
         return 60 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 40;
      }

      public int getSelectionWeight() {
         return 10;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.sweepYaw = boss.rotationYaw;
         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 3.0F, 0.5F);
         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 2.0F, 1.0F);
         if (raid != null) {
            raid.broadcastMessage("§4§l[TOTSUKA BLADE] Massive sweep incoming!");
         }

      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            float sweepProgress = (float)ticksElapsed / 30.0F;
            float currentSweepAngle = -90.0F + 180.0F * Math.min(1.0F, sweepProgress);
            if (boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;

               for(int r = 3; r <= this.sweepRadius; r += 2) {
                  double angle = Math.toRadians((double)(this.sweepYaw + currentSweepAngle));
                  double x = boss.posX + (double)r * Math.sin(angle);
                  double z = boss.posZ - (double)r * Math.cos(angle);
                  world.spawnParticle(EnumParticleTypes.FLAME, x, boss.posY + (double)1.0F + world.rand.nextDouble() * (double)3.0F, z, 5, 0.3, (double)0.5F, 0.3, 0.05, new int[0]);
                  world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, boss.posY + (double)2.0F, z, 2, 0.2, 0.3, 0.2, 0.02, new int[0]);
               }

               if (ticksElapsed % 3 == 0) {
                  for(float a = -90.0F; a <= currentSweepAngle; a += 15.0F) {
                     double angle = Math.toRadians((double)(this.sweepYaw + a));
                     double dist = (double)this.sweepRadius * 0.7 + world.rand.nextDouble() * (double)this.sweepRadius * 0.3;
                     double x = boss.posX + dist * Math.sin(angle);
                     double z = boss.posZ - dist * Math.cos(angle);
                     world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, boss.posY + (double)0.5F, z, 1, 0.2, 0.1, 0.2, 0.01, new int[0]);
                  }
               }
            }

            if (ticksElapsed <= 30 && ticksElapsed % 5 == 0) {
               for(EntityPlayer player : raid.getParticipants()) {
                  double dx = player.posX - boss.posX;
                  double dz = player.posZ - boss.posZ;
                  double distSq = dx * dx + dz * dz;
                  if (!(distSq > (double)(this.sweepRadius * this.sweepRadius)) && !(distSq < (double)4.0F)) {
                     double playerAngle = Math.toDegrees(Math.atan2(dx, -dz));
                     double relAngle = this.normalizeAngle(playerAngle - (double)this.sweepYaw);
                     if (relAngle >= (double)-95.0F && relAngle <= (double)(currentSweepAngle + 5.0F)) {
                        player.hurtResistantTime = 0;
                        ItachiMechanics.applyDamageWithTrueComponent(player, this.damage, DamageSource.MAGIC);
                        double dist = Math.sqrt(distSq);
                        if (dist > (double)0.0F) {
                           KnockbackHelper.applyWallSafeKnockback(player, dx / dist * (double)1.5F, 0.6, dz / dist * (double)1.5F);
                        }
                     }
                  }
               }
            }

            if (ticksElapsed == 15 || ticksElapsed == 25) {
               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0F, 0.7F);
            }

         }
      }

      private double normalizeAngle(double angle) {
         while(angle > (double)180.0F) {
            angle -= (double)360.0F;
         }

         while(angle < (double)-180.0F) {
            angle += (double)360.0F;
         }

         return angle;
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.5F, 0.8F);
      }
   }

   private static class YataMirrorMechanic implements BossMechanic {
      private final RaidDifficulty difficulty;
      private int immuneWindowLength;
      private int vulnerableWindowLength;
      private boolean currentlyImmune = false;
      private int cycleTimer = 0;

      public YataMirrorMechanic(RaidDifficulty difficulty) {
         this.difficulty = difficulty;
         this.immuneWindowLength = 30 + difficulty.ordinal() * 10;
         this.vulnerableWindowLength = 60 - difficulty.ordinal() * 10;
      }

      public String getName() {
         return "yata_mirror";
      }

      public String getDisplayName() {
         return "Yata Mirror";
      }

      public String getWarningMessage() {
         return "Susanoo raises the Yata Mirror! Attack during openings!";
      }

      public int getWarningTicks() {
         return 40 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 200;
      }

      public int getSelectionWeight() {
         return 6;
      }

      public boolean isHeavyMechanic() {
         return true;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.currentlyImmune = true;
         this.cycleTimer = 0;
         if (boss instanceof EntityRaidBossItachi.EntityCustom) {
            ((EntityRaidBossItachi.EntityCustom)boss).setDamageImmune(true, "Yata Mirror active");
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.HOSTILE, 2.5F, 0.5F);
         if (raid != null) {
            raid.broadcastMessage("§6§l[YATA MIRROR] Itachi is immune! Wait for the opening!");
         }

      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            ++this.cycleTimer;
            int windowLength = this.currentlyImmune ? this.immuneWindowLength : this.vulnerableWindowLength;
            if (this.cycleTimer >= windowLength) {
               this.cycleTimer = 0;
               this.currentlyImmune = !this.currentlyImmune;
               if (boss instanceof EntityRaidBossItachi.EntityCustom) {
                  ((EntityRaidBossItachi.EntityCustom)boss).setDamageImmune(this.currentlyImmune, this.currentlyImmune ? "Yata Mirror active" : "");
               }

               if (this.currentlyImmune) {
                  boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.HOSTILE, 2.0F, 0.6F);
                  if (raid != null) {
                     raid.broadcastMessage("§6[YATA MIRROR] Shield is UP! Itachi is immune!");
                  }
               } else {
                  boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.HOSTILE, 2.0F, 1.2F);
                  if (raid != null) {
                     raid.broadcastMessage("§a§l[YATA MIRROR] Shield DOWN! ATTACK NOW!");
                  }
               }
            }

            if (boss.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)boss.world;
               if (this.currentlyImmune) {
                  double shieldAngle = Math.toRadians((double)(ticksElapsed * 12 % 360));

                  for(int i = 0; i < 3; ++i) {
                     double a = shieldAngle + Math.toRadians((double)(120 * i));
                     double x = boss.posX + (double)2.5F * Math.cos(a);
                     double z = boss.posZ + (double)2.5F * Math.sin(a);
                     ws.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, x, boss.posY + (double)1.0F, z, 5, 0.1, (double)0.5F, 0.1, (double)0.0F, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.END_ROD, x, boss.posY + (double)1.5F, z, 2, 0.05, 0.05, 0.05, (double)0.0F, new int[0]);
                  }

                  if (this.cycleTimer > this.immuneWindowLength - 20 && this.cycleTimer % 4 == 0) {
                     ws.spawnParticle(EnumParticleTypes.CRIT, boss.posX, boss.posY + (double)1.0F, boss.posZ, 15, (double)2.0F, (double)1.0F, (double)2.0F, 0.1, new int[0]);
                  }
               } else {
                  if (ticksElapsed % 5 == 0) {
                     ws.spawnParticle(EnumParticleTypes.REDSTONE, boss.posX, boss.posY + (double)2.0F, boss.posZ, 8, (double)1.0F, (double)0.5F, (double)1.0F, (double)0.0F, new int[0]);
                  }

                  if (this.cycleTimer > this.vulnerableWindowLength - 30 && this.cycleTimer % 5 == 0) {
                     ws.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, boss.posX, boss.posY + (double)1.0F, boss.posZ, 10, (double)1.5F, (double)1.0F, (double)1.5F, (double)0.0F, new int[0]);
                  }
               }
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         if (boss instanceof EntityRaidBossItachi.EntityCustom) {
            ((EntityRaidBossItachi.EntityCustom)boss).setDamageImmune(false, (String)null);
         }

         this.currentlyImmune = false;
         if (raid != null) {
            raid.broadcastMessage("§aYata Mirror effect has ended!");
         }

      }

      public void onInterrupt(EntityLivingBase boss, RaidInstance raid) {
         if (boss instanceof EntityRaidBossItachi.EntityCustom) {
            ((EntityRaidBossItachi.EntityCustom)boss).setDamageImmune(false, (String)null);
         }

         this.currentlyImmune = false;
      }
   }

   private static class YasakaBarrageMechanic implements BossMechanic {
      private final float damage;
      private final RaidDifficulty difficulty;
      private final int safeZoneCount;
      private List<BlockPos> safeZones = new ArrayList();

      public YasakaBarrageMechanic(float damage, RaidDifficulty difficulty, int safeZoneCount) {
         this.damage = damage;
         this.difficulty = difficulty;
         this.safeZoneCount = safeZoneCount;
      }

      public String getName() {
         return "yasaka_barrage";
      }

      public String getDisplayName() {
         return "Yasaka Magatama Barrage";
      }

      public String getWarningMessage() {
         return "§4Susanoo charges a devastating Yasaka Magatama Barrage!";
      }

      public int getWarningTicks() {
         return 100 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 200;
      }

      public int getSelectionWeight() {
         return 6;
      }

      public boolean isHeavyMechanic() {
         return true;
      }

      public List<BlockPos> getSafeZones() {
         return this.safeZones;
      }

      public int getSafeZoneRadius() {
         return 6;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.safeZones.clear();
         if (raid != null) {
            Random rand = boss.world.rand;

            for(int i = 0; i < this.safeZoneCount; ++i) {
               double angle = Math.toRadians((double)360.0F / (double)this.safeZoneCount * (double)i + (double)rand.nextInt(30));
               int distance = 18 + rand.nextInt(10);
               int x = (int)(boss.posX + (double)distance * Math.cos(angle));
               int z = (int)(boss.posZ + (double)distance * Math.sin(angle));
               this.safeZones.add(new BlockPos(x, (int)boss.posY, z));
            }

            raid.broadcastMessage("§c§l[YASAKA BARRAGE] Get to a safe zone! Only " + this.safeZoneCount + " safe spots!");
            boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 3.0F, 0.4F);
         }
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            if (ticksElapsed % 2 == 0 && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;

               for(int i = 0; i < 30; ++i) {
                  double angle = world.rand.nextDouble() * (double)360.0F;
                  double rad = Math.toRadians(angle);
                  double distance = (double)5.0F + world.rand.nextDouble() * (double)35.0F;
                  double x = boss.posX + distance * Math.cos(rad);
                  double z = boss.posZ + distance * Math.sin(rad);
                  double y = boss.posY + (double)1.0F + world.rand.nextDouble() * (double)4.0F;
                  BlockPos pos = new BlockPos(x, boss.posY, z);
                  if (!this.isInSafeZone(pos)) {
                     world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 2, 0.3, 0.3, 0.3, 0.03, new int[0]);
                     if (world.rand.nextFloat() < 0.2F) {
                        world.spawnParticle(EnumParticleTypes.LAVA, x, y, z, 1, 0.1, 0.1, 0.1, (double)0.0F, new int[0]);
                     }

                     if (world.rand.nextFloat() < 0.05F) {
                        world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, x, y, z, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                     }
                  }
               }

               for(BlockPos safeZone : this.safeZones) {
                  int safeRadius = this.getSafeZoneRadius();

                  for(int angle = 0; angle < 360; angle += 10) {
                     double rad = Math.toRadians((double)angle);
                     double px = (double)safeZone.getX() + (double)0.5F + (double)safeRadius * Math.cos(rad);
                     double pz = (double)safeZone.getZ() + (double)0.5F + (double)safeRadius * Math.sin(rad);
                     world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, px, (double)safeZone.getY() + (double)0.5F, pz, 3, 0.1, 0.3, 0.1, (double)0.0F, new int[0]);
                  }

                  for(int y = 0; y < 15; ++y) {
                     world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)safeZone.getX() + (double)0.5F, (double)(safeZone.getY() + y), (double)safeZone.getZ() + (double)0.5F, 2, 0.3, 0.1, 0.3, (double)0.0F, new int[0]);
                  }
               }
            }

            if (ticksElapsed % 15 == 0) {
               for(EntityPlayer player : raid.getParticipants()) {
                  if (!this.isInSafeZone(player.getPosition())) {
                     ItachiMechanics.applyDamageWithTrueComponent(player, this.damage, DamageSource.MAGIC);
                     if (boss.world instanceof WorldServer) {
                        ((WorldServer)boss.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, player.posX, player.posY + (double)1.0F, player.posZ, 5, 0.3, 0.3, 0.3, (double)0.0F, new int[0]);
                     }
                  }
               }
            }

            if (ticksElapsed % 25 == 0) {
               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 0.8F + boss.world.rand.nextFloat() * 0.3F);
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

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         this.safeZones.clear();
         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0F, 1.0F);
         if (raid != null) {
            raid.broadcastMessage("§aThe Yasaka Magatama Barrage has ended!");
         }

      }
   }
}
