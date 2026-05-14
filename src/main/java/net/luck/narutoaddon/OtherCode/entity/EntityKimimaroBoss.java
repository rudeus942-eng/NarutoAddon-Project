
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.ModelPlayerPoseable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcPose;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKimimaroBoss extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 260;

   public EntityKimimaroBoss(ElementsInfTsukAddon instance) {
      super(instance, 260);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kimimaroboss"), 260).name("kimimaroboss").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KimimaroBossRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class KimimaroBossRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public KimimaroBossRenderer(RenderManager renderManager) {
         super(renderManager, new ModelPlayerPoseable(0.0F, false), 0.5F);
         this.addLayer(new LayerHeldItem(this));
         this.addLayer(new LayerBipedArmor(this));
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         String texOverride = entity.getTextureOverride();
         if (texOverride != null && !texOverride.isEmpty()) {
            return new ResourceLocation(texOverride);
         } else {
            String configId = entity.getNpcConfigId();
            if (configId != null && !configId.isEmpty()) {
               NpcConfig config = NpcConfigRegistry.get(configId);
               if (config != null && config.getTexture() != null) {
                  return config.getTexture();
               }
            }

            return FALLBACK_TEXTURE;
         }
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         String configId = entity.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null) {
               float scale = config.getRenderScale();
               GlStateManager.scale(scale, scale, scale);
            }
         }

         String poseName = entity.getNpcPoseName();
         NpcPose pose = NpcPose.fromName(poseName);
         if (pose.yOffset != 0.0F) {
            GlStateManager.translate(0.0F, pose.yOffset, 0.0F);
         }

      }

      protected void applyRotations(EntityCustom entity, float ageInTicks, float rotationYaw, float partialTicks) {
         super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
      }
   }

   public static class EntityCustom extends QuestNpcBase {
      private static final float KIM_COMBO_LIGHT_DMG = 8.0F;
      private static final float KIM_COMBO_LIGHT_TRUE = 2.5F;
      private static final float KIM_COMBO_HEAVY_DMG = 15.0F;
      private static final float KIM_COMBO_HEAVY_TRUE = 5.0F;
      private static final float KIM_MELEE_DMG = 11.0F;
      private static final float KIM_MELEE_TRUE = 3.0F;
      private static final float KIM_FINGER_BULLET_DMG = 5.0F;
      private static final float KIM_CLEMATIS_VINE_DMG = 12.0F;
      private static final float KIM_CLEMATIS_VINE_TRUE = 4.0F;
      private static final float KIM_BONE_DRILL_DMG = 20.0F;
      private static final float KIM_BONE_DRILL_TRUE = 6.0F;
      private static final float KIM_CLEMATIS_FLOWER_DMG = 30.0F;
      private static final float KIM_CLEMATIS_FLOWER_TRUE = 10.0F;
      private static final float KIM_SEEDLING_FERN_DMG = 25.0F;
      private static final float KIM_SEEDLING_FERN_TRUE = 8.0F;
      private static final float KIM_TAIL_SWEEP_DMG = 10.0F;
      private static final float KIM_TAIL_SWEEP_TRUE = 3.0F;
      private static final float KIM_THORNS_DMG = 3.0F;
      private static final int KIM_COMBO_MAX_HITS = 4;
      private static final int[] KIM_COMBO_DELAYS = new int[]{0, 7, 8, 12};
      private static final float KIM_PHASE_2 = 0.65F;
      private static final float KIM_PHASE_3 = 0.3F;
      private static final int KIM_RUSH_TICKS = 8;
      private static final int KIM_DRILL_TICKS = 10;
      private static final int KIM_LUNGE_TICKS = 10;
      private static final int KIM_SEEDLING_TELEGRAPH = 60;
      private static final float KIM_SEEDLING_RADIUS = 8.0F;
      private int kimimaroPhase = 1;
      private boolean phase2Announced = false;
      private boolean phase3Announced = false;
      private int invulnTimer = 0;
      private int comboCD = 0;
      private int fingerBulletCD = 0;
      private int rushCD = 0;
      private int clematisVineCD = 0;
      private int boneDrillCD = 0;
      private int clematisFlowerCD = 0;
      private int kimMeleeCD = 0;
      private int seedlingFernCD = 0;
      private boolean kim_isRushing = false;
      private int kim_rushTicksRemaining = 0;
      private double kim_rushVelX = (double)0.0F;
      private double kim_rushVelZ = (double)0.0F;
      private boolean kim_isDrillCharging = false;
      private int kim_drillTicksRemaining = 0;
      private double kim_drillVelX = (double)0.0F;
      private double kim_drillVelZ = (double)0.0F;
      private boolean kim_isLunging = false;
      private int kim_lungeTicksRemaining = 0;
      private double kim_lungeVelX = (double)0.0F;
      private double kim_lungeVelZ = (double)0.0F;
      private boolean seedlingFernActive = false;
      private int seedlingFernTimer = 0;
      private int comboHitCount = 0;
      private int comboTimer = 0;
      private boolean inCombo = false;
      private EntityLivingBase comboTarget = null;
      private int attackTick = 0;
      private int stuckSafetyTimer = 0;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
         if (!this.inCombo && !this.kim_isRushing && !this.kim_isDrillCharging && !this.kim_isLunging && !this.seedlingFernActive) {
            ++this.attackTick;
            this.handleMovement(target, dist);
            switch (this.kimimaroPhase) {
               case 1:
                  this.phase1(target, dist);
                  break;
               case 2:
                  this.phase2(target, dist);
                  break;
               case 3:
                  this.phase3(target, dist);
            }

         }
      }

      protected void tickStyleCooldowns() {
         if (this.comboCD > 0) {
            --this.comboCD;
         }

         if (this.fingerBulletCD > 0) {
            --this.fingerBulletCD;
         }

         if (this.rushCD > 0) {
            --this.rushCD;
         }

         if (this.clematisVineCD > 0) {
            --this.clematisVineCD;
         }

         if (this.boneDrillCD > 0) {
            --this.boneDrillCD;
         }

         if (this.clematisFlowerCD > 0) {
            --this.clematisFlowerCD;
         }

         if (this.kimMeleeCD > 0) {
            --this.kimMeleeCD;
         }

         if (this.seedlingFernCD > 0) {
            --this.seedlingFernCD;
         }

         if (this.invulnTimer > 0) {
            --this.invulnTimer;
         }

         this.processRushTick();
         this.processDrillTick();
         this.processLungeTick();
         this.processSeedlingFern();
         this.processComboTick();
         this.checkPhaseTransition();
         this.processStuckSafety();
      }

      protected void resetCombatState() {
         this.kimimaroPhase = 1;
         this.phase2Announced = false;
         this.phase3Announced = false;
         this.invulnTimer = 0;
         this.comboCD = 0;
         this.fingerBulletCD = 0;
         this.rushCD = 0;
         this.clematisVineCD = 0;
         this.boneDrillCD = 0;
         this.clematisFlowerCD = 0;
         this.kimMeleeCD = 0;
         this.seedlingFernCD = 0;
         this.kim_isRushing = false;
         this.kim_isDrillCharging = false;
         this.kim_isLunging = false;
         this.seedlingFernActive = false;
         this.inCombo = false;
         this.comboTarget = null;
         this.comboHitCount = 0;
         this.comboTimer = 0;
         this.attackTick = 0;
         this.stuckSafetyTimer = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("kimimaroPhase", this.kimimaroPhase);
         compound.setBoolean("phase2Announced", this.phase2Announced);
         compound.setBoolean("phase3Announced", this.phase3Announced);
         compound.setInteger("invulnTimer", this.invulnTimer);
         compound.setInteger("comboCD", this.comboCD);
         compound.setInteger("fingerBulletCD", this.fingerBulletCD);
         compound.setInteger("rushCD", this.rushCD);
         compound.setInteger("clematisVineCD", this.clematisVineCD);
         compound.setInteger("boneDrillCD", this.boneDrillCD);
         compound.setInteger("clematisFlowerCD", this.clematisFlowerCD);
         compound.setInteger("kimMeleeCD", this.kimMeleeCD);
         compound.setInteger("seedlingFernCD", this.seedlingFernCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.kimimaroPhase = compound.hasKey("kimimaroPhase") ? compound.getInteger("kimimaroPhase") : 1;
         this.phase2Announced = compound.getBoolean("phase2Announced");
         this.phase3Announced = compound.getBoolean("phase3Announced");
         this.invulnTimer = compound.getInteger("invulnTimer");
         this.comboCD = compound.getInteger("comboCD");
         this.fingerBulletCD = compound.getInteger("fingerBulletCD");
         this.rushCD = compound.getInteger("rushCD");
         this.clematisVineCD = compound.getInteger("clematisVineCD");
         this.boneDrillCD = compound.getInteger("boneDrillCD");
         this.clematisFlowerCD = compound.getInteger("clematisFlowerCD");
         this.kimMeleeCD = compound.getInteger("kimMeleeCD");
         this.seedlingFernCD = compound.getInteger("seedlingFernCD");
         if (this.kimimaroPhase == 2) {
            this.setTextureOverride("inftsukaddon:textures/kimimaro_stage1.png");
         } else if (this.kimimaroPhase == 3) {
            this.setTextureOverride("inftsukaddon:textures/kimimaro_stage2.png");
         }

      }

      protected void onCombatDeath() {
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         if (this.invulnTimer > 0) {
            return -1.0F;
         } else {
            if (this.kimimaroPhase >= 2) {
               float dr = this.kimimaroPhase == 3 ? 0.35F : 0.25F;
               amount *= 1.0F - dr;
            }

            return amount;
         }
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         boolean result = super.attackEntityFrom(source, amount);
         if (result && !this.world.isRemote && this.kimimaroPhase >= 2) {
            Entity attacker = source.getTrueSource();
            if (attacker instanceof EntityLivingBase && !source.isProjectile() && !source.isMagicDamage() && !source.isExplosion() && (double)this.getDistance(attacker) <= (double)4.0F) {
               attacker.attackEntityFrom(DamageSource.causeThornsDamage(this), 3.0F);
            }
         }

         return result;
      }

      protected boolean usesVanillaMeleeAI() {
         return true;
      }

      public void onLivingUpdate() {
         super.onLivingUpdate();
      }

      private void checkPhaseTransition() {
         float hpPct = this.getHealth() / this.getMaxHealth();
         if (this.kimimaroPhase == 1 && hpPct <= 0.65F) {
            this.kimimaroPhase = 2;
            this.onKimimaroPhaseTransition(2);
         } else if (this.kimimaroPhase == 2 && hpPct <= 0.3F) {
            this.kimimaroPhase = 3;
            this.onKimimaroPhaseTransition(3);
         }

      }

      private void onKimimaroPhaseTransition(int newPhase) {
         this.inCombo = false;
         this.comboTarget = null;
         this.comboHitCount = 0;
         this.invulnTimer = 20;
         if (newPhase == 2 && !this.phase2Announced) {
            this.phase2Announced = true;
            this.broadcastNearby((double)50.0F, "§5§lKimimaro: §7\"The curse mark... I will use everything for Lord Orochimaru.\"");
            this.setTextureOverride("inftsukaddon:textures/kimimaro_stage1.png");
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.48);
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int i = 0; i < 50; ++i) {
                  ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, this.posY + this.rand.nextDouble() * (double)3.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{0});
               }

               for(int i = 0; i < 30; ++i) {
                  ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, this.posY + this.rand.nextDouble() * (double)2.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{0});
               }
            }

            this.pushPlayersAway((double)5.0F, 0.7, (double)0.25F);
         }

         if (newPhase == 3 && !this.phase3Announced) {
            this.phase3Announced = true;
            this.broadcastNearby((double)50.0F, "§4§lKimimaro: §7\"This body... is my weapon. I will not fall!\"");
            this.setTextureOverride("inftsukaddon:textures/kimimaro_stage2.png");
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.45);
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)14.0F);
            this.comboCD = 0;
            this.fingerBulletCD = 0;
            this.rushCD = 0;
            this.clematisVineCD = 0;
            this.boneDrillCD = 0;
            this.clematisFlowerCD = 0;
            this.seedlingFernCD = 0;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int i = 0; i < 70; ++i) {
                  ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)7.0F, this.posY + this.rand.nextDouble() * (double)4.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)7.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{0});
               }

               for(int i = 0; i < 40; ++i) {
                  ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F, this.posY + this.rand.nextDouble() * (double)3.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{0});
               }

               for(int i = 0; i < 25; ++i) {
                  ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, this.posY + this.rand.nextDouble() * (double)3.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{0});
               }
            }

            this.pushPlayersAway((double)7.0F, (double)1.0F, 0.4);
         }

      }

      private void pushPlayersAway(double radius, double strength, double upY) {
         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(radius))) {
            if (p.isEntityAlive() && !p.isSpectator()) {
               double dx = p.posX - this.posX;
               double dz = p.posZ - this.posZ;
               double d = Math.sqrt(dx * dx + dz * dz);
               if (d > (double)0.0F) {
                  p.motionX += dx / d * strength;
                  p.motionY += upY;
                  p.motionZ += dz / d * strength;
                  if (p instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)p).velocityChanged = true;
                  }
               }
            }
         }

      }

      private void handleMovement(EntityLivingBase target, double dist) {
         if (!this.kim_isRushing && !this.kim_isDrillCharging && !this.kim_isLunging) {
            switch (this.kimimaroPhase) {
               case 1:
                  if (dist > (double)5.0F) {
                     this.getNavigator().tryMoveToEntityLiving(target, 1.3);
                  } else if (dist < (double)2.0F) {
                     this.getNavigator().clearPath();
                  } else {
                     this.getNavigator().tryMoveToEntityLiving(target, (double)1.0F);
                  }
                  break;
               case 2:
                  if (dist > (double)7.0F) {
                     this.getNavigator().tryMoveToEntityLiving(target, 1.4);
                  } else if (dist < (double)2.0F) {
                     this.getNavigator().clearPath();
                  } else {
                     this.getNavigator().tryMoveToEntityLiving(target, 1.1);
                  }
                  break;
               case 3:
                  if (dist > (double)4.0F) {
                     this.getNavigator().tryMoveToEntityLiving(target, (double)1.5F);
                  } else {
                     this.getNavigator().tryMoveToEntityLiving(target, 1.1);
                  }
            }

         }
      }

      private void phase1(EntityLivingBase target, double dist) {
         if (this.rushCD <= 0 && dist >= (double)5.0F && dist <= (double)18.0F) {
            this.doRush(target);
         } else if (this.comboCD <= 0 && dist <= (double)3.5F) {
            this.startCombo(target);
         } else if (this.fingerBulletCD <= 0 && dist >= (double)4.0F && dist <= (double)25.0F) {
            this.doFingerBullets(target);
         } else {
            if (dist <= (double)3.0F && this.attackTick >= 18) {
               this.doMelee(target);
               this.attackTick = 0;
            }

         }
      }

      private void phase2(EntityLivingBase target, double dist) {
         if (this.boneDrillCD <= 0 && dist >= (double)4.0F && dist <= (double)20.0F) {
            this.doDrillCharge(target);
         } else if (this.clematisVineCD <= 0 && dist <= (double)6.0F) {
            this.doClematisPull(target);
         } else {
            this.phase1(target, dist);
         }
      }

      private void phase3(EntityLivingBase target, double dist) {
         if (this.seedlingFernCD <= 0 && !this.seedlingFernActive && dist <= (double)12.0F) {
            this.doSeedlingFern();
         } else if (this.clematisFlowerCD <= 0 && dist >= (double)4.0F && dist <= (double)15.0F) {
            this.doClematisflowerLunge(target);
         } else if (this.boneDrillCD <= 0 && dist >= (double)4.0F && dist <= (double)20.0F) {
            this.doDrillCharge(target);
         } else if (this.kimMeleeCD <= 0 && dist <= (double)4.0F) {
            this.doTailSweep();
         } else if (this.clematisVineCD <= 0 && dist <= (double)6.0F) {
            this.doClematisPull(target);
         } else if (this.rushCD <= 0 && dist >= (double)5.0F && dist <= (double)18.0F) {
            this.doRush(target);
         } else if (this.comboCD <= 0 && dist <= (double)3.5F) {
            this.startCombo(target);
         } else if (this.fingerBulletCD <= 0 && dist >= (double)4.0F && dist <= (double)25.0F) {
            this.doFingerBullets(target);
         } else {
            if (dist <= (double)3.0F && this.attackTick >= 12) {
               this.doMelee(target);
               this.attackTick = 0;
            }

         }
      }

      private void doMelee(EntityLivingBase target) {
         float dm = this.dmgMul();
         float normDmg = 11.0F * dm;
         float trueDmg = 3.0F * dm;
         if (target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg)) {
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         }

         this.swingArm(EnumHand.MAIN_HAND);
         this.kimMeleeCD = this.cdMul(20);
      }

      private void startCombo(EntityLivingBase target) {
         if (this.comboCD <= 0) {
            int[] c = this.getCooldownRange(this.kimimaroPhase == 3 ? 25 : (this.kimimaroPhase == 2 ? 35 : 45), this.kimimaroPhase == 3 ? 40 : (this.kimimaroPhase == 2 ? 50 : 65));
            this.comboCD = c[0] + this.getRNG().nextInt(Math.max(1, c[1] - c[0] + 1));
            this.comboHitCount = 0;
            this.inCombo = true;
            this.comboTarget = target;
            this.comboTimer = 0;
         }
      }

      private void performComboHit(EntityLivingBase target) {
         if ((double)this.getDistance(target) > (double)4.5F) {
            this.inCombo = false;
            this.comboTarget = null;
            this.comboHitCount = 0;
         } else {
            float dm = this.dmgMul();
            boolean isHeavy = this.comboHitCount == 3;
            float nd = (isHeavy ? 15.0F : 8.0F) * dm;
            float td = (isHeavy ? 5.0F : 2.5F) * dm;
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), nd);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, td);
            this.swingArm(EnumHand.MAIN_HAND);
            if (isHeavy) {
               float kb = 1.2F;
               target.motionX += (target.posX - this.posX) * 0.14 * (double)kb;
               target.motionY += 0.35;
               target.motionZ += (target.posZ - this.posZ) * 0.14 * (double)kb;
               if (target instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)target).velocityChanged = true;
               }

               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.8F);
               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.3F, 1.6F);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 25, 0.6, 0.6, 0.6, (double)0.25F, new int[0]);
               }
            } else {
               float pitch = 0.9F + (float)this.comboHitCount * 0.1F;
               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.9F, pitch);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 5 + this.comboHitCount * 3, 0.3, 0.3, 0.3, 0.05, new int[0]);
               }
            }

         }
      }

      private void doFingerBullets(EntityLivingBase target) {
         if (this.fingerBulletCD <= 0) {
            int[] c = this.getCooldownRange(this.kimimaroPhase == 3 ? 15 : (this.kimimaroPhase == 2 ? 20 : 25), this.kimimaroPhase == 3 ? 25 : (this.kimimaroPhase == 2 ? 35 : 40));
            this.fingerBulletCD = c[0] + this.getRNG().nextInt(Math.max(1, c[1] - c[0] + 1));
            float dm = this.dmgMul();
            float dmgPerBullet = 5.0F * dm;
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
            double dz = target.posZ - this.posZ;

            for(int i = 0; i < 5; ++i) {
               EntityFingerBullet.EntityCustom bullet = new EntityFingerBullet.EntityCustom(this.world, this, dmgPerBullet, dmgPerBullet * 0.3F);
               bullet.shoot(dx + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, dy, dz + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, 1.8F, 2.0F);
               this.world.spawnEntity(bullet);
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + 1.2, this.posZ, 10, 0.3, 0.3, 0.3, 0.1, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SKELETON_SHOOT, SoundCategory.HOSTILE, 1.2F, 1.5F);
         }
      }

      private void doRush(EntityLivingBase target) {
         if (this.rushCD <= 0 && !this.kim_isRushing && !this.kim_isDrillCharging && !this.kim_isLunging) {
            int[] c = this.getCooldownRange(this.kimimaroPhase == 3 ? 30 : (this.kimimaroPhase == 2 ? 45 : 60), this.kimimaroPhase == 3 ? 50 : (this.kimimaroPhase == 2 ? 70 : 90));
            this.rushCD = c[0] + this.getRNG().nextInt(Math.max(1, c[1] - c[0] + 1));
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (!(d <= (double)0.0F)) {
               double rushDist = Math.min(d - (double)1.5F, (double)16.0F);
               if (!(rushDist <= (double)0.0F)) {
                  double nx = dx / d;
                  double nz = dz / d;
                  double velocityPerTick = rushDist / (double)8.0F;
                  this.kim_isRushing = true;
                  this.kim_rushTicksRemaining = 8;
                  this.kim_rushVelX = nx * velocityPerTick * 1.3;
                  this.kim_rushVelZ = nz * velocityPerTick * 1.3;
                  this.motionY = 0.2;
                  this.velocityChanged = true;
                  if (this.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)this.world;
                     ws.spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)0.5F, this.posZ, 20, 0.8, (double)0.5F, 0.8, 0.15, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 8, 0.4, 0.3, 0.4, 0.1, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.5F, 0.8F);
               }
            }
         }
      }

      private void doClematisPull(EntityLivingBase target) {
         if (this.clematisVineCD <= 0) {
            int[] c = this.getCooldownRange(this.kimimaroPhase == 3 ? 40 : 50, this.kimimaroPhase == 3 ? 60 : 70);
            this.clematisVineCD = c[0] + this.getRNG().nextInt(Math.max(1, c[1] - c[0] + 1));
            float dm = this.dmgMul();
            float normDmg = 12.0F * dm;
            float trueDmg = 4.0F * dm;

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)6.0F))) {
               if (e instanceof EntityLivingBase && !(e instanceof QuestNpcBase)) {
                  double dx = e.posX - this.posX;
                  double dz = e.posZ - this.posZ;
                  double eDist = Math.sqrt(dx * dx + dz * dz);
                  if (!(eDist > (double)6.0F)) {
                     double lookX = (double)(-MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F)));
                     double lookZ = (double)MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F));
                     double dot = (dx * lookX + dz * lookZ) / eDist;
                     if (dot > (double)-0.5F) {
                        EntityLivingBase living = (EntityLivingBase)e;
                        living.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                        living.hurtResistantTime = 0;
                        living.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                        living.motionX += dx / eDist * (double)0.5F;
                        living.motionY += 0.2;
                        living.motionZ += dz / eDist * (double)0.5F;
                        if (living instanceof EntityPlayerMP) {
                           ((EntityPlayerMP)living).velocityChanged = true;
                        }
                     }
                  }
               }
            }

            if (this.world instanceof WorldServer) {
               double lookX = (double)(-MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F)));
               double lookZ = (double)MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F));

               for(int i = 1; i <= 12; ++i) {
                  double px = this.posX + lookX * (double)i * (double)0.5F;
                  double pz = this.posZ + lookZ * (double)i * (double)0.5F;
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, px, this.posY + (double)1.0F, pz, 3, 0.1, 0.2, 0.1, 0.02, new int[0]);
               }
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.5F, 0.6F);
         }
      }

      private void doDrillCharge(EntityLivingBase target) {
         if (this.boneDrillCD <= 0 && !this.kim_isDrillCharging && !this.kim_isRushing && !this.kim_isLunging) {
            int[] c = this.getCooldownRange(this.kimimaroPhase == 3 ? 60 : 80, this.kimimaroPhase == 3 ? 100 : 120);
            this.boneDrillCD = c[0] + this.getRNG().nextInt(Math.max(1, c[1] - c[0] + 1));
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (!(d <= (double)0.0F)) {
               double chargeDist = Math.min(d - (double)1.0F, (double)18.0F);
               if (!(chargeDist <= (double)0.0F)) {
                  double nx = dx / d;
                  double nz = dz / d;
                  double velocityPerTick = chargeDist / (double)10.0F * 1.4;
                  this.kim_isDrillCharging = true;
                  this.kim_drillTicksRemaining = 10;
                  this.kim_drillVelX = nx * velocityPerTick;
                  this.kim_drillVelZ = nz * velocityPerTick;
                  this.motionY = 0.15;
                  this.velocityChanged = true;
                  Item drill = (Item)Item.REGISTRY.getObject(new ResourceLocation("narutomod", "bone_drill"));
                  if (drill != null) {
                     this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(drill));
                  }

                  if (this.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)this.world;
                     ws.spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)0.5F, this.posZ, 20, 0.8, (double)0.5F, 0.8, 0.15, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 10, 0.4, 0.3, 0.4, 0.1, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.8F, 0.5F);
               }
            }
         }
      }

      private void doClematisflowerLunge(EntityLivingBase target) {
         if (this.clematisFlowerCD <= 0 && !this.kim_isLunging && !this.kim_isDrillCharging && !this.kim_isRushing) {
            int[] c = this.getCooldownRange(100, 140);
            this.clematisFlowerCD = c[0] + this.getRNG().nextInt(Math.max(1, c[1] - c[0] + 1));
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (!(d <= (double)0.0F)) {
               double lungeDist = Math.min(d - (double)0.5F, (double)12.0F);
               if (!(lungeDist <= (double)0.0F)) {
                  double nx = dx / d;
                  double nz = dz / d;
                  double velocityPerTick = lungeDist / (double)10.0F * 1.6;
                  this.kim_isLunging = true;
                  this.kim_lungeTicksRemaining = 10;
                  this.kim_lungeVelX = nx * velocityPerTick;
                  this.kim_lungeVelZ = nz * velocityPerTick;
                  this.motionY = (double)0.25F;
                  this.velocityChanged = true;
                  Item drill = (Item)Item.REGISTRY.getObject(new ResourceLocation("narutomod", "bone_drill"));
                  if (drill != null) {
                     this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(drill));
                  }

                  this.broadcastNearby((double)50.0F, "§4§lKimimaro: §7\"Clematis Flower!\"");
                  if (this.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)this.world;
                     ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)0.5F, this.posZ, 30, (double)1.0F, (double)0.5F, (double)1.0F, 0.2, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)0.5F, this.posZ, 15, (double)0.5F, 0.3, (double)0.5F, 0.1, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 2.0F, 0.4F);
               }
            }
         }
      }

      private void doSeedlingFern() {
         if (this.seedlingFernCD <= 0 && !this.seedlingFernActive) {
            int[] c = this.getCooldownRange(160, 220);
            this.seedlingFernCD = c[0] + this.getRNG().nextInt(Math.max(1, c[1] - c[0] + 1));
            this.seedlingFernActive = true;
            this.seedlingFernTimer = 60;
            this.broadcastNearby((double)50.0F, "§4§lKimimaro: §7\"Dance of the Seedling Fern!\"");
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 1.5F, 0.6F);
         }
      }

      private void doTailSweep() {
         if (this.kimMeleeCD <= 0) {
            int[] c = this.getCooldownRange(60, 100);
            this.kimMeleeCD = c[0] + this.getRNG().nextInt(Math.max(1, c[1] - c[0] + 1));
            float dm = this.dmgMul();
            float normDmg = 10.0F * dm;
            float trueDmg = 3.0F * dm;

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)4.0F))) {
               if (e instanceof EntityLivingBase && !(e instanceof QuestNpcBase)) {
                  double dx = e.posX - this.posX;
                  double dz = e.posZ - this.posZ;
                  double eDist = Math.sqrt(dx * dx + dz * dz);
                  if (!(eDist > (double)4.0F) && !(eDist < 0.1)) {
                     EntityLivingBase living = (EntityLivingBase)e;
                     living.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                     living.hurtResistantTime = 0;
                     living.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                     living.motionX += dx / eDist * 0.45;
                     living.motionY += 0.35;
                     living.motionZ += dz / eDist * 0.45;
                     if (living instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)living).velocityChanged = true;
                     }
                  }
               }
            }

            if (this.world instanceof WorldServer) {
               for(int i = 0; i < 16; ++i) {
                  double angle = 0.19634954084936207 * (double)i + (double)(this.rotationYaw * ((float)Math.PI / 180F)) + (Math.PI / 2D);
                  double px = this.posX + Math.cos(angle) * (double)3.0F;
                  double pz = this.posZ + Math.sin(angle) * (double)3.0F;
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, px, this.posY + (double)0.5F, pz, 1, 0.1, 0.1, 0.1, 0.01, new int[0]);
               }
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.5F, 0.7F);
         }
      }

      private void processRushTick() {
         if (this.kim_isRushing) {
            --this.kim_rushTicksRemaining;
            this.motionX = this.kim_rushVelX;
            this.motionZ = this.kim_rushVelZ;
            if (this.kim_rushTicksRemaining > 4) {
               this.motionY = 0.05;
            } else {
               this.motionY = Math.max(-0.3, this.motionY - 0.06);
            }

            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)0.5F, this.posZ, 2, 0.2, 0.2, 0.2, 0.02, new int[0]);
            }

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)1.0F))) {
               if (e instanceof EntityLivingBase && !(e instanceof QuestNpcBase)) {
                  float rushDmg = 5.5F * this.dmgMul();
                  e.attackEntityFrom(DamageSource.causeMobDamage(this), rushDmg);
               }
            }

            if (this.kim_rushTicksRemaining <= 0) {
               this.kim_isRushing = false;
               this.kim_rushVelX = (double)0.0F;
               this.kim_rushVelZ = (double)0.0F;
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + 0.1, this.posZ, 12, (double)0.5F, 0.1, (double)0.5F, 0.1, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_SMALL_FALL, SoundCategory.HOSTILE, 1.0F, 1.0F);
            }

         }
      }

      private void processDrillTick() {
         if (this.kim_isDrillCharging) {
            --this.kim_drillTicksRemaining;
            this.motionX = this.kim_drillVelX;
            this.motionZ = this.kim_drillVelZ;
            if (this.kim_drillTicksRemaining > 5) {
               this.motionY = 0.03;
            } else {
               this.motionY = Math.max(-0.3, this.motionY - 0.06);
            }

            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)0.5F, this.posZ, 3, 0.2, 0.2, 0.2, 0.02, new int[0]);
            }

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)1.0F))) {
               if (e instanceof EntityLivingBase && !(e instanceof QuestNpcBase)) {
                  float dm = this.dmgMul();
                  float normDmg = 20.0F * dm;
                  float trueDmg = 6.0F * dm;
                  e.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                  ((EntityLivingBase)e).hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  double kbX = e.posX - this.posX;
                  double kbZ = e.posZ - this.posZ;
                  double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
                  if (kbDist > (double)0.0F) {
                     e.motionX += kbX / kbDist * 0.7;
                     e.motionY += 0.3;
                     e.motionZ += kbZ / kbDist * 0.7;
                     if (e instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)e).velocityChanged = true;
                     }
                  }

                  this.kim_drillTicksRemaining = 0;
                  break;
               }
            }

            if (this.kim_drillTicksRemaining <= 0) {
               this.kim_isDrillCharging = false;
               this.kim_drillVelX = (double)0.0F;
               this.kim_drillVelZ = (double)0.0F;
               this.equipWeapon();
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + 0.1, this.posZ, 15, (double)0.5F, 0.1, (double)0.5F, 0.1, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_SMALL_FALL, SoundCategory.HOSTILE, 1.0F, 1.0F);
            }

         }
      }

      private void processLungeTick() {
         if (this.kim_isLunging) {
            --this.kim_lungeTicksRemaining;
            this.motionX = this.kim_lungeVelX;
            this.motionZ = this.kim_lungeVelZ;
            if (this.kim_lungeTicksRemaining > 5) {
               this.motionY = 0.05;
            } else {
               this.motionY = Math.max((double)-0.5F, this.motionY - 0.08);
            }

            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + 0.8, this.posZ, 5, 0.3, 0.4, 0.3, 0.05, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)0.5F, this.posZ, 3, 0.2, 0.2, 0.2, 0.02, new int[0]);
            }

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(1.2))) {
               if (e instanceof EntityLivingBase && !(e instanceof QuestNpcBase)) {
                  float dm = this.dmgMul();
                  float normDmg = 30.0F * dm;
                  float trueDmg = 10.0F * dm;
                  e.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                  ((EntityLivingBase)e).hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  double kbX = e.posX - this.posX;
                  double kbZ = e.posZ - this.posZ;
                  double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
                  if (kbDist > (double)0.0F) {
                     e.motionX += kbX / kbDist * 1.2;
                     e.motionY += (double)0.5F;
                     e.motionZ += kbZ / kbDist * 1.2;
                     if (e instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)e).velocityChanged = true;
                     }
                  }

                  this.kim_lungeTicksRemaining = 0;
                  break;
               }
            }

            if (this.kim_lungeTicksRemaining <= 0) {
               this.kim_isLunging = false;
               this.kim_lungeVelX = (double)0.0F;
               this.kim_lungeVelZ = (double)0.0F;
               this.equipWeapon();
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + 0.1, this.posZ, 30, (double)1.5F, 0.2, (double)1.5F, 0.15, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + 0.2, this.posZ, 15, (double)1.0F, 0.3, (double)1.0F, 0.1, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.2F, 0.9F);
            }

         }
      }

      private void processSeedlingFern() {
         if (this.seedlingFernActive) {
            --this.seedlingFernTimer;
            float progress = 1.0F - (float)this.seedlingFernTimer / 60.0F;
            float currentRadius = 8.0F * progress;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               int particleCount = (int)(currentRadius * 4.0F);

               for(int i = 0; i < particleCount; ++i) {
                  double angle = (Math.PI * 2D) / (double)Math.max(1, particleCount) * (double)i;
                  double px = this.posX + Math.cos(angle) * (double)currentRadius;
                  double pz = this.posZ + Math.sin(angle) * (double)currentRadius;
                  ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, px, this.posY + 0.3, pz, 1, 0.1, 0.1, 0.1, 0.01, new int[0]);
                  if (i % 3 == 0) {
                     ws.spawnParticle(EnumParticleTypes.CRIT, px, this.posY + (double)0.5F, pz, 1, 0.05, 0.3, 0.05, 0.01, new int[0]);
                  }
               }
            }

            if (this.seedlingFernTimer <= 0) {
               this.seedlingFernActive = false;
               float dm = this.dmgMul();
               float normDmg = 25.0F * dm;
               float trueDmg = 8.0F * dm;

               for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)8.0F))) {
                  if (e instanceof EntityLivingBase && !(e instanceof QuestNpcBase)) {
                     double eDist = (double)this.getDistance(e);
                     if (eDist <= (double)8.0F) {
                        EntityLivingBase living = (EntityLivingBase)e;
                        living.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                        living.hurtResistantTime = 0;
                        living.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                        living.motionY += 0.6;
                        if (living instanceof EntityPlayerMP) {
                           ((EntityPlayerMP)living).velocityChanged = true;
                        }
                     }
                  }
               }

               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;

                  for(int i = 0; i < 60; ++i) {
                     double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
                     double r = this.rand.nextDouble() * (double)8.0F;
                     double px = this.posX + Math.cos(angle) * r;
                     double pz = this.posZ + Math.sin(angle) * r;
                     ws.spawnParticle(EnumParticleTypes.CRIT, px, this.posY + (double)0.5F, pz, 3, 0.1, 0.8, 0.1, 0.1, new int[0]);
                  }

                  ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 5, (double)3.0F, (double)0.5F, (double)3.0F, (double)0.0F, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0F, 0.5F);
            }

         }
      }

      private void processComboTick() {
         if (this.inCombo && this.comboTarget != null && this.comboTarget.isEntityAlive()) {
            ++this.comboTimer;
            if (this.comboHitCount < 4 && this.comboTimer >= KIM_COMBO_DELAYS[Math.min(this.comboHitCount, KIM_COMBO_DELAYS.length - 1)]) {
               this.performComboHit(this.comboTarget);
               ++this.comboHitCount;
               this.comboTimer = 0;
               if (this.comboHitCount >= 4) {
                  this.inCombo = false;
                  this.comboTarget = null;
                  this.comboHitCount = 0;
               }
            }
         } else if (this.inCombo) {
            this.inCombo = false;
            this.comboTarget = null;
            this.comboHitCount = 0;
         }

      }

      private void processStuckSafety() {
         if (!this.inCombo && !this.kim_isRushing && !this.kim_isDrillCharging && !this.kim_isLunging && !this.seedlingFernActive) {
            this.stuckSafetyTimer = 0;
         } else {
            ++this.stuckSafetyTimer;
            if (this.stuckSafetyTimer > 100) {
               if (this.inCombo) {
                  this.inCombo = false;
                  this.comboTarget = null;
                  this.comboHitCount = 0;
               }

               if (this.kim_isRushing) {
                  this.kim_isRushing = false;
                  this.kim_rushVelX = (double)0.0F;
                  this.kim_rushVelZ = (double)0.0F;
               }

               if (this.kim_isDrillCharging) {
                  this.kim_isDrillCharging = false;
                  this.kim_drillVelX = (double)0.0F;
                  this.kim_drillVelZ = (double)0.0F;
                  this.equipWeapon();
               }

               if (this.kim_isLunging) {
                  this.kim_isLunging = false;
                  this.kim_lungeVelX = (double)0.0F;
                  this.kim_lungeVelZ = (double)0.0F;
                  this.equipWeapon();
               }

               if (this.seedlingFernActive) {
                  this.seedlingFernActive = false;
               }

               this.stuckSafetyTimer = 0;
            }
         }

      }

      private float dmgMul() {
         double base = this.getDamageMultiplier();
         if (this.kimimaroPhase == 3) {
            float hpPct = this.getHealth() / this.getMaxHealth();
            return (float)(base * ((double)1.0F + ((double)1.0F - (double)hpPct) * 0.8));
         } else {
            return this.kimimaroPhase == 2 ? (float)(base * (double)1.25F) : (float)base;
         }
      }
   }
}
