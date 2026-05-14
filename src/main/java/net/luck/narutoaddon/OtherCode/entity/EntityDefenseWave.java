
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.ModelPlayerPoseable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcPose;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityDefenseWave extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 253;

   public EntityDefenseWave(ElementsInfTsukAddon instance) {
      super(instance, 253);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "defensewave"), 253).name("defensewave").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, CustomRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class CustomRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public CustomRenderer(RenderManager renderManager) {
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

      protected void renderModel(EntityCustom entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
         float alpha = this.getGhostAlpha(entity);
         if (alpha < 1.0F) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
            GlStateManager.color(0.7F, 0.85F, 1.0F, alpha);
            if (this.bindEntityTexture(entity)) {
               this.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            }

            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         } else {
            super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
         }

      }

      private float getGhostAlpha(EntityCustom entity) {
         String configId = entity.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null) {
               return config.getGhostAlpha();
            }
         }

         return 1.0F;
      }
   }

   public static class EntityCustom extends QuestNpcBase {
      private boolean headhunterActive = false;
      private int headhunterTick = 0;
      private EntityLivingBase headhunterTarget = null;
      private double headhunterStartX;
      private double headhunterStartY;
      private double headhunterStartZ;
      private static final int HEADHUNTER_UNDERGROUND_TICKS = 15;
      private static final int HEADHUNTER_STUN_TICKS = 40;
      private int comboStep = 0;
      private int comboTickDelay = 0;
      private EntityLivingBase comboTarget = null;
      private boolean hiddenMistActive = false;
      private int hiddenMistTick = 0;
      private EntityLivingBase hiddenMistTarget = null;
      private static final int HIDDEN_MIST_INVIS_TICKS = 60;
      private static final int HIDDEN_MIST_TOTAL_TICKS = 70;
      private static final int WATER_PRISON_COOLDOWN_BASE = 600;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (target != null && target.isEntityAlive()) {
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            if (this.headhunterActive) {
               this.processHeadhunter();
            } else {
               if ("RANGED".equals(this.teamRole) && target != null && dist < (double)6.0F) {
                  double distFromSpawn = this.getDistance(this.spawnOriginX, this.spawnOriginY, this.spawnOriginZ);
                  if (Double.isNaN(this.spawnOriginX) || !(distFromSpawn > (double)40.0F)) {
                     double fleeX = this.posX + (this.posX - target.posX) * (double)0.5F;
                     double fleeZ = this.posZ + (this.posZ - target.posZ) * (double)0.5F;
                     this.getNavigator().tryMoveToXYZ(fleeX, this.posY, fleeZ, (double)1.5F);
                     return;
                  }

                  this.getNavigator().clearPath();
                  this.setAttackTarget(target);
                  this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
               }

               if (this.teamRole != null && this.combatTier >= 3 && this.switchRetreatTicks <= 0) {
                  float retreatThreshold = this.combatTier >= 4 ? 0.2F : 0.15F;
                  if (this.getHealth() / this.getMaxHealth() < retreatThreshold && this.ticksExisted % 10 == 0) {
                     boolean allyAlive = false;

                     for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)30.0F))) {
                        if (e instanceof EntityCustom && !e.isDead) {
                           allyAlive = true;
                           break;
                        }
                     }

                     if (allyAlive) {
                        this.switchRetreatTicks = 60;
                        return;
                     }
                  }
               }

               if (this.hasHiddenMist && !this.hiddenMistTriggered && !this.hiddenMistActive && target.isEntityAlive()) {
                  float hpPercent = this.getHealth() / this.getMaxHealth();
                  if (hpPercent <= this.hiddenMistThreshold) {
                     this.triggerHiddenMist(target);
                     return;
                  }
               }

               if (!this.hiddenMistActive) {
                  if (this.hasWaterPrison && this.waterPrisonCooldown <= 0 && !this.isDashing && dist <= (double)3.0F && dist >= (double)1.0F && this.rand.nextFloat() < 0.15F) {
                     this.castWaterPrison(target);
                  }

                  if (this.hasIceDomePhase && !this.iceDomeTriggered && target.isEntityAlive()) {
                     float hpPercent = this.getHealth() / this.getMaxHealth();
                     if (hpPercent <= this.iceDomeHealthThreshold) {
                        this.triggerIceDome(target);
                     }
                  }

                  if (this.hasWaterDragon && this.waterDragonCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F && this.rand.nextFloat() < 0.15F) {
                     this.spawnWaterDragon(target);
                  }

                  if (this.hasHeadhunter && this.headhunterCooldown <= 0 && !this.isDashing && dist <= (double)5.0F && dist >= (double)1.5F && this.rand.nextFloat() < 0.2F) {
                     this.startHeadhunter(target);
                  } else if (this.natureType != 0 && this.combatTier >= 2 && this.jutsuCooldown <= 0 && this.jutsuWindup <= 0 && dist >= (double)6.0F && dist <= (double)18.0F && this.rand.nextFloat() < 0.3F) {
                     this.startNatureJutsu(target);
                  } else if (this.hasRangedAttack && this.combatTier >= 2 && this.kunaiCooldown <= 0 && dist >= (double)8.0F && dist <= (double)20.0F) {
                     this.throwKunai(target);
                  } else if (this.combatTier >= 2 && this.dashCooldown <= 0 && !this.isDashing && dist >= (double)4.0F && dist <= (double)15.0F) {
                     this.startDashStrike(target);
                  } else {
                     if (this.combatTier == 1 && this.dashCooldown <= 0 && !this.isDashing && dist >= (double)4.0F && dist <= (double)10.0F && this.rand.nextFloat() < 0.4F) {
                        this.startLungeDash(target);
                     }

                     if (this.combatTier >= 2 && this.repositionCooldown <= 0 && dist < (double)8.0F && dist > (double)2.5F && !"RANGED".equals(this.teamRole) && this.strafeTicksRemaining <= 0) {
                        Vec3d offset = this.getSurroundOffset(target);
                        if (offset != null) {
                           double goalX = target.posX + offset.x;
                           double goalZ = target.posZ + offset.z;
                           this.getNavigator().tryMoveToXYZ(goalX, target.posY, goalZ, 1.2);
                           this.repositionCooldown = 40 + this.rand.nextInt(30);
                        }
                     }

                     if (this.combatTier >= 2 && this.strafeCooldown <= 0 && this.strafeTicksRemaining <= 0 && dist >= (double)3.0F && dist <= (double)7.0F && !"RANGED".equals(this.teamRole)) {
                        float strafeChance = this.combatTier >= 4 ? 0.35F : (this.combatTier >= 3 ? 0.25F : 0.15F);
                        if (this.rand.nextFloat() < strafeChance) {
                           this.strafeDirection = this.rand.nextBoolean() ? 1 : -1;
                           this.strafeTicksRemaining = 10 + this.rand.nextInt(15);
                           this.strafeCooldown = 40 + this.rand.nextInt(40);
                        }
                     }

                     if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
                        if (this.hasSwordCombo && this.comboStep == 0 && this.rand.nextFloat() < 0.3F) {
                           this.startSwordCombo(target);
                        } else {
                           this.performMeleeSwing(target);
                        }
                     }

                  }
               }
            }
         }
      }

      protected void tickStyleCooldowns() {
         if (this.comboStep > 0 && this.comboTarget != null) {
            if (this.comboTickDelay > 0) {
               --this.comboTickDelay;
            } else {
               this.processComboHit();
            }
         }

         if (this.hiddenMistActive) {
            this.processHiddenMist();
         }

      }

      protected void resetCombatState() {
         this.headhunterActive = false;
         this.headhunterTarget = null;
         this.comboStep = 0;
         this.comboTarget = null;
         this.hiddenMistActive = false;
         this.hiddenMistTarget = null;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("headhunterActive", this.headhunterActive);
         compound.setBoolean("hiddenMistActive", this.hiddenMistActive);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.headhunterActive = compound.getBoolean("headhunterActive");
         this.hiddenMistActive = compound.getBoolean("hiddenMistActive");
      }

      private void startDashStrike(EntityLivingBase target) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (!(horizDist < (double)0.5F)) {
            double nx = dx / horizDist;
            double nz = dz / horizDist;
            int duration = 10;
            double dashDist = Math.max(horizDist - (double)2.0F, (double)1.0F);
            double velocityPerTick = dashDist / (double)duration;
            this.isDashing = true;
            this.dashType = 2;
            this.dashDuration = duration;
            this.dashTicksRemaining = duration;
            this.dashVelX = nx * velocityPerTick * 1.3;
            this.dashVelZ = nz * velocityPerTick * 1.3;
            this.dashArcSustain = 0.08F;
            this.dashFallAccel = 0.08F;
            this.dashMaxFall = -0.5F;
            this.dashHasMidairGuidance = false;
            this.motionY = 0.35;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.15, new int[0]);
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 10, 0.3, 0.3, 0.3, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.2F, 0.9F);
            int maxCd;
            int baseCd;
            if (this.combatTier <= 2) {
               baseCd = 60;
               maxCd = 100;
            } else if (this.combatTier == 3) {
               baseCd = 60;
               maxCd = 120;
            } else {
               baseCd = 80;
               maxCd = 140;
            }

            int[] cdRange = this.getCooldownRange(baseCd, maxCd);
            this.dashCooldown = cdRange[0] + this.rand.nextInt(cdRange[1] - cdRange[0] + 1);
         }
      }

      private void startHeadhunter(EntityLivingBase target) {
         this.headhunterActive = true;
         this.headhunterTick = 0;
         this.headhunterTarget = target;
         this.headhunterStartX = this.posX;
         this.headhunterStartY = this.posY;
         this.headhunterStartZ = this.posZ;
         if (this.chatCooldown <= 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
               p.sendMessage(new TextComponentString("§7" + this.getCustomNameTag() + ": §eEarth Style: Headhunter Jutsu!"));
            }

            this.chatCooldown = 100;
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, 0.8, (double)0.5F, 0.02, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 10, 0.4, 0.4, 0.4, 0.01, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.HOSTILE, 1.5F, 0.7F);
         this.internalReposition = true;
         this.setPositionAndUpdate(this.posX, this.posY - (double)3.0F, this.posZ);
         this.internalReposition = false;
         int[] cdRange = this.getCooldownRange(300, 400);
         this.headhunterCooldown = cdRange[0] + this.rand.nextInt(cdRange[1] - cdRange[0] + 1);
      }

      private void processHeadhunter() {
         ++this.headhunterTick;
         if (this.headhunterTarget != null && this.headhunterTarget.isEntityAlive()) {
            if (this.headhunterTick <= 15) {
               if (this.headhunterTick % 3 == 0 && this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.headhunterTarget.posX, this.headhunterTarget.posY, this.headhunterTarget.posZ, 5, 0.3, 0.1, 0.3, 0.01, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.headhunterTarget.posX, this.headhunterTarget.posY, this.headhunterTarget.posZ, 8, 0.4, 0.05, 0.4, 0.02, new int[]{Block.getStateId(Blocks.DIRT.getDefaultState())});
               }

            } else if (this.headhunterTick != 16) {
               int stunEnd = 56;
               if (this.headhunterTick <= stunEnd) {
                  if (this.headhunterTarget.isEntityAlive()) {
                     this.headhunterTarget.motionX = (double)0.0F;
                     this.headhunterTarget.motionZ = (double)0.0F;
                     this.headhunterTarget.velocityChanged = true;
                  }

                  if (this.headhunterTick % 10 == 0 && this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.headhunterTarget.posX, this.headhunterTarget.posY + 0.3, this.headhunterTarget.posZ, 5, 0.2, 0.1, 0.2, 0.01, new int[0]);
                  }

               } else {
                  if (this.headhunterTick >= stunEnd + 10) {
                     this.headhunterActive = false;
                     this.headhunterTarget = null;
                  }

               }
            } else {
               double dx = this.headhunterTarget.posX - this.headhunterStartX;
               double dz = this.headhunterTarget.posZ - this.headhunterStartZ;
               double angle = Math.atan2(dz, dx);
               double behindX = this.headhunterTarget.posX - Math.cos(angle) * (double)1.5F;
               double behindZ = this.headhunterTarget.posZ - Math.sin(angle) * (double)1.5F;
               double behindY = this.headhunterTarget.posY;
               BlockPos emergePos = new BlockPos(behindX, behindY, behindZ);
               if (this.world.isBlockLoaded(emergePos) && this.isPositionSafe(behindX, behindY, behindZ)) {
                  this.internalReposition = true;
                  this.setPositionAndUpdate(behindX, behindY, behindZ);
                  this.internalReposition = false;
               } else {
                  this.internalReposition = true;
                  this.setPositionAndUpdate(this.headhunterTarget.posX, this.headhunterTarget.posY, this.headhunterTarget.posZ);
                  this.internalReposition = false;
               }

               this.getLookHelper().setLookPositionWithEntity(this.headhunterTarget, 360.0F, 360.0F);
               double fDx = this.headhunterTarget.posX - this.posX;
               double fDz = this.headhunterTarget.posZ - this.posZ;
               this.rotationYaw = (float)(Math.atan2(-fDx, fDz) * (180D / Math.PI));
               this.rotationYawHead = this.rotationYaw;
               double dmgMul = this.getDamageMultiplier();
               float normalDmg = 4.0F * (float)dmgMul;
               this.headhunterTarget.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
               this.headhunterTarget.hurtResistantTime = 0;
               this.headhunterTarget.attackEntityFrom(DamageSource.MAGIC, 2.0F);
               this.headhunterTarget.motionX = (double)0.0F;
               this.headhunterTarget.motionY = (double)-0.5F;
               this.headhunterTarget.motionZ = (double)0.0F;
               this.headhunterTarget.velocityChanged = true;
               this.headhunterTarget.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 45, 10, false, false));
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.headhunterTarget.posX, this.headhunterTarget.posY + (double)0.5F, this.headhunterTarget.posZ, 25, (double)0.5F, (double)0.5F, (double)0.5F, 0.03, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.headhunterTarget.posX, this.headhunterTarget.posY, this.headhunterTarget.posZ, 15, 0.4, 0.1, 0.4, 0.05, new int[]{Block.getStateId(Blocks.DIRT.getDefaultState())});
               }

               this.world.playSound((EntityPlayer)null, this.headhunterTarget.posX, this.headhunterTarget.posY, this.headhunterTarget.posZ, SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.HOSTILE, 2.0F, 0.6F);
            }
         } else {
            this.internalReposition = true;
            this.setPositionAndUpdate(this.headhunterStartX, this.headhunterStartY, this.headhunterStartZ);
            this.internalReposition = false;
            this.headhunterActive = false;
            this.headhunterTarget = null;
         }
      }

      private void startSwordCombo(EntityLivingBase target) {
         float baseDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         double dmgMul = this.getDamageMultiplier();
         float slashNormal;
         float slashTrue;
         if (this.trueDamageSplit > 0.0F) {
            slashNormal = baseDamage * 0.8F * (1.0F - this.trueDamageSplit) * (float)dmgMul;
            slashTrue = baseDamage * 0.8F * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
         } else {
            slashNormal = baseDamage * 0.8F * (float)dmgMul;
            slashTrue = (this.combatTier >= 4 ? 6.0F : (this.combatTier >= 3 ? 4.0F : (this.combatTier >= 2 ? 3.0F : 2.0F))) * this.trueDamageMultiplier;
         }

         target.attackEntityFrom(DamageSource.causeMobDamage(this), slashNormal);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, slashTrue);
         this.swingArm(EnumHand.MAIN_HAND);
         this.comboStep = 1;
         this.comboTickDelay = 8;
         this.comboTarget = target;
      }

      private void processComboHit() {
         if (this.comboTarget != null && this.comboTarget.isEntityAlive() && !(this.getDistanceSq(this.comboTarget) > (double)16.0F)) {
            float baseDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            if (this.comboStep == 1) {
               float comboNormal;
               float comboTrue;
               if (this.trueDamageSplit > 0.0F) {
                  comboNormal = baseDamage * 0.7F * (1.0F - this.trueDamageSplit) * (float)dmgMul;
                  comboTrue = baseDamage * 0.7F * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
               } else {
                  comboNormal = baseDamage * 0.7F * (float)dmgMul;
                  comboTrue = (this.combatTier >= 4 ? 10.0F : (this.combatTier >= 3 ? 6.0F : (this.combatTier >= 2 ? 4.0F : 2.5F))) * this.trueDamageMultiplier;
               }

               this.comboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), comboNormal);
               this.comboTarget.hurtResistantTime = 0;
               this.comboTarget.attackEntityFrom(DamageSource.MAGIC, comboTrue);
               this.swingArm(EnumHand.MAIN_HAND);
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.1F);
               this.comboStep = 2;
               this.comboTickDelay = 12;
            } else if (this.comboStep == 2) {
               float heavyNormal;
               float heavyTrue;
               if (this.trueDamageSplit > 0.0F) {
                  float heavySplit = Math.min(1.0F, this.trueDamageSplit + 0.1F);
                  heavyNormal = baseDamage * 1.3F * (1.0F - heavySplit) * (float)dmgMul;
                  heavyTrue = baseDamage * 1.3F * heavySplit * (float)dmgMul * this.trueDamageMultiplier;
               } else {
                  heavyNormal = baseDamage * 1.3F * (float)dmgMul;
                  heavyTrue = (this.combatTier >= 4 ? 16.0F : (this.combatTier >= 3 ? 10.0F : (this.combatTier >= 2 ? 6.0F : 4.0F))) * this.trueDamageMultiplier;
               }

               this.comboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), heavyNormal);
               this.comboTarget.hurtResistantTime = 0;
               this.comboTarget.attackEntityFrom(DamageSource.MAGIC, heavyTrue);
               this.swingArm(EnumHand.MAIN_HAND);
               double kbX = this.comboTarget.posX - this.posX;
               double kbZ = this.comboTarget.posZ - this.posZ;
               double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
               if (kbDist > 0.01) {
                  EntityLivingBase var10000 = this.comboTarget;
                  var10000.motionX += kbX / kbDist * 0.6;
                  var10000 = this.comboTarget;
                  var10000.motionY += 0.2;
                  var10000 = this.comboTarget;
                  var10000.motionZ += kbZ / kbDist * 0.6;
                  this.comboTarget.velocityChanged = true;
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.2F, 0.8F);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.comboTarget.posX, this.comboTarget.posY + (double)1.0F, this.comboTarget.posZ, 15, 0.4, (double)0.5F, 0.4, 0.2, new int[0]);
               }

               this.comboStep = 0;
               this.comboTarget = null;
               int[] cdRange = this.getCooldownRange(30, 50);
               this.meleeCooldown = cdRange[0] + this.rand.nextInt(cdRange[1] - cdRange[0] + 1);
            }

         } else {
            this.comboStep = 0;
            this.comboTarget = null;
         }
      }

      private void triggerHiddenMist(EntityLivingBase target) {
         this.hiddenMistTriggered = true;
         this.hiddenMistActive = true;
         this.hiddenMistTick = 0;
         this.hiddenMistTarget = target;
         if (this.chatCooldown <= 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
               p.sendMessage(new TextComponentString("§7" + this.getCustomNameTag() + ": §8Ninja Art: Hidden Mist Jutsu!"));
            }

            this.chatCooldown = 100;
         }

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)20.0F))) {
            p.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 160, 0));
            p.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 100, 0));
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 100; ++i) {
               double px = this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)30.0F;
               double py = this.posY + this.rand.nextDouble() * (double)4.0F;
               double pz = this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)30.0F;
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 40, (double)8.0F, (double)2.0F, (double)8.0F, 0.02, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 2.0F, 0.5F);
         this.setInvisible(true);
      }

      private void processHiddenMist() {
         ++this.hiddenMistTick;
         if (this.hiddenMistTarget != null && this.hiddenMistTarget.isEntityAlive()) {
            if (this.hiddenMistTick <= 60) {
               if (this.hiddenMistTick % 10 == 0 && this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.hiddenMistTarget.posX, this.hiddenMistTarget.posY + (double)1.0F, this.hiddenMistTarget.posZ, 15, (double)5.0F, (double)2.0F, (double)5.0F, 0.01, new int[0]);
               }

               if (this.hiddenMistTick == 50) {
                  double behindX = this.hiddenMistTarget.posX - this.hiddenMistTarget.getLookVec().x * (double)3.0F;
                  double behindZ = this.hiddenMistTarget.posZ - this.hiddenMistTarget.getLookVec().z * (double)3.0F;
                  this.internalReposition = true;
                  this.setPositionAndUpdate(behindX, this.hiddenMistTarget.posY, behindZ);
                  this.internalReposition = false;
               }

            } else {
               if (this.hiddenMistTick == 61) {
                  this.setInvisible(false);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 25, 0.6, 0.8, 0.6, 0.05, new int[0]);
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.4, (double)0.5F, 0.4, 0.15, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.5F, 0.7F);

                  for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
                     p.sendMessage(new TextComponentString("§7" + this.getCustomNameTag() + ": §cSilent Killing!"));
                  }

                  float baseDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                  this.hiddenMistTarget.attackEntityFrom(DamageSource.causeMobDamage(this), baseDamage * 1.5F);
                  this.hiddenMistTarget.hurtResistantTime = 0;
                  this.hiddenMistTarget.attackEntityFrom(DamageSource.MAGIC, 8.0F);
                  this.swingArm(EnumHand.MAIN_HAND);
               }

               if (this.hiddenMistTick >= 70) {
                  this.endHiddenMist();
               }

            }
         } else {
            this.endHiddenMist();
         }
      }

      private void endHiddenMist() {
         this.hiddenMistActive = false;
         this.hiddenMistTarget = null;
         this.setInvisible(false);
      }

      private void castWaterPrison(EntityLivingBase target) {
         if (this.chatCooldown <= 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
               p.sendMessage(new TextComponentString("§7" + this.getCustomNameTag() + ": §9Water Prison Jutsu!"));
            }

            this.chatCooldown = 100;
         }

         if (target instanceof EntityPlayer) {
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 9));
            target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 80, 9));
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.WATER_SPLASH, target.posX, target.posY + (double)1.0F, target.posZ, 60, 0.8, (double)1.0F, 0.8, 0.3, new int[0]);
            ws.spawnParticle(EnumParticleTypes.DRIP_WATER, target.posX, target.posY + (double)2.0F, target.posZ, 30, 0.6, (double)0.5F, 0.6, (double)0.0F, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 1.5F, 0.8F);
         int[] cdRange = this.getCooldownRange(600, 800);
         this.waterPrisonCooldown = cdRange[0] + this.rand.nextInt(cdRange[1] - cdRange[0] + 1);
      }

      private void triggerIceDome(EntityLivingBase target) {
         this.iceDomeTriggered = true;
         if (this.chatCooldown <= 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
               p.sendMessage(new TextComponentString("§b" + this.getCustomNameTag() + ": §fSecret Jutsu... Demonic Mirroring Ice Crystals!"));
            }

            this.chatCooldown = 200;
         }

         EntityIceMirrors.EntityCustom dome = new EntityIceMirrors.EntityCustom(this.world, this, target.posX, target.posY, target.posZ);
         this.world.spawnEntity(dome);
         this.activeDome = dome;
         SoundEvent makyoSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "makyohyosho"));
         if (makyoSound != null) {
            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, makyoSound, SoundCategory.HOSTILE, 3.0F, 1.0F);
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SNOWBALL, target.posX, target.posY + (double)1.0F, target.posZ, 50, (double)3.0F, (double)2.0F, (double)3.0F, 0.1, new int[0]);
         }

      }

      private void spawnWaterDragon(EntityLivingBase target) {
         if (this.chatCooldown <= 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
               p.sendMessage(new TextComponentString("§9" + this.getCustomNameTag() + ": §fWater Style: Water Dragon Jutsu!"));
            }

            this.chatCooldown = 200;
         }

         double dx = target.posX - this.posX;
         double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
         double dz = target.posZ - this.posZ;
         double dist = Math.sqrt(dx * dx + dz * dz);
         float dragonDmg = 20.0F * this.waterDragonPower;
         float dragonTrueDmg = 10.0F * this.waterDragonPower;
         EntityWaterDragonJutsu.EntityCustom dragon = new EntityWaterDragonJutsu.EntityCustom(this.world, this, dragonDmg, dragonTrueDmg);
         dragon.shoot(dx, dy + dist * 0.01, dz, 1.2F, 2.0F);
         this.world.spawnEntity(dragon);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)1.0F, (double)0.5F, (double)1.0F, 0.1, new int[0]);
         }

         this.waterDragonCooldown = 200 + this.rand.nextInt(100);
      }

      private Vec3d getSurroundOffset(EntityLivingBase target) {
         List<EntityCustom> allies = new ArrayList();

         for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)15.0F))) {
            if (e instanceof EntityCustom && !e.isDead) {
               EntityCustom ally = (EntityCustom)e;
               if (ally.getAttackTarget() == target) {
                  allies.add(ally);
               }
            }
         }

         if (allies.isEmpty()) {
            return null;
         } else {
            int totalAttackers = allies.size() + 1;
            if (totalAttackers < 2) {
               return null;
            } else {
               double[] allyAngles = new double[allies.size()];

               for(int i = 0; i < allies.size(); ++i) {
                  allyAngles[i] = Math.atan2(((EntityCustom)allies.get(i)).posZ - target.posZ, ((EntityCustom)allies.get(i)).posX - target.posX);
               }

               double myAngle = Math.atan2(this.posZ - target.posZ, this.posX - target.posX);
               double bestAngle = myAngle;
               double bestMinDist = (double)-1.0F;
               int slots = Math.min(totalAttackers, 4);

               for(int s = 0; s < slots; ++s) {
                  double candidateAngle = myAngle + (Math.PI * 2D) * (double)s / (double)slots;
                  double minDistToAlly = Double.MAX_VALUE;

                  for(double a : allyAngles) {
                     double diff = Math.abs(this.angleDiff(candidateAngle, a));
                     if (diff < minDistToAlly) {
                        minDistToAlly = diff;
                     }
                  }

                  if (minDistToAlly > bestMinDist) {
                     bestMinDist = minDistToAlly;
                     bestAngle = candidateAngle;
                  }
               }

               double currentMinDist = Double.MAX_VALUE;

               for(double a : allyAngles) {
                  double diff = Math.abs(this.angleDiff(myAngle, a));
                  if (diff < currentMinDist) {
                     currentMinDist = diff;
                  }
               }

               if (bestMinDist <= currentMinDist + 0.3) {
                  return null;
               } else {
                  double approachDist = (double)3.0F + this.rand.nextDouble() * (double)2.0F;
                  return new Vec3d(Math.cos(bestAngle) * approachDist, (double)0.0F, Math.sin(bestAngle) * approachDist);
               }
            }
         }
      }

      private double angleDiff(double a, double b) {
         double d;
         for(d = a - b; d > Math.PI; d -= (Math.PI * 2D)) {
         }

         while(d < -Math.PI) {
            d += (Math.PI * 2D);
         }

         return d;
      }

      protected void onCombatDeath() {
         if (this.activeDome != null && !this.activeDome.isDead) {
            this.activeDome.setDead();
         }

      }
   }
}
