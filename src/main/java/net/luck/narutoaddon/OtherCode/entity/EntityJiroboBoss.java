
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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityJiroboBoss extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 240;

   public EntityJiroboBoss(ElementsInfTsukAddon instance) {
      super(instance, 240);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "jiroboboss"), 240).name("jiroboboss").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, JiroboBossRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class JiroboBossRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public JiroboBossRenderer(RenderManager renderManager) {
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
      private static final float JIROBO_PHASE_2 = 0.65F;
      private static final float JIROBO_PHASE_3 = 0.3F;
      private static final double JIROBO_BASE_HP = (double)12000.0F;
      private int jiroboPhase = 1;
      private boolean jiroboIntroPlayed = false;
      private int jiroboEarthSlideCD = 0;
      private int jiroboShoulderChargeCD = 0;
      private int jiroboEarthDomeCD = 0;
      private int jiroboGroundPoundCD = 0;
      private int jiroboEarthBarrierCD = 0;
      private int jiroboComboCD = 0;
      private boolean jiroboBarrierActive = false;
      private int jiroboBarrierTicks = 0;
      private int jiroboMeleeCD = 0;

      public EntityCustom(World world) {
         super(world);
      }

      protected boolean usesVanillaMeleeAI() {
         return false;
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            double yDiff = target.posY - this.posY;
            if (yDiff > (double)5.0F && !this.isDashing) {
               if (yDiff > (double)30.0F) {
                  double safeY = this.findSafeY(target.posX, target.posY, target.posZ);
                  this.setPositionAndUpdate(target.posX, safeY, target.posZ);
                  return;
               }

               double adx = target.posX - this.posX;
               double adz = target.posZ - this.posZ;
               double ahd = Math.sqrt(adx * adx + adz * adz);
               if (ahd > (double)0.5F) {
                  double anx = adx / ahd;
                  double anz = adz / ahd;
                  this.isDashing = true;
                  this.isLeaping = true;
                  this.dashType = 4;
                  this.dashDuration = 14;
                  this.dashTicksRemaining = 14;
                  this.dashVelX = anx * 0.6;
                  this.dashVelZ = anz * 0.6;
                  this.dashArcSustain = 0.0F;
                  this.dashFallAccel = 0.06F;
                  this.dashMaxFall = -1.0F;
                  this.dashHasMidairGuidance = true;
                  this.motionY = Math.min((double)2.5F, 0.7 + yDiff * 0.12);
                  this.velocityChanged = true;
                  return;
               }
            }

            float hpPercent = this.getHealth() / this.getMaxHealth();
            this.jiroboCheckPhaseTransition(hpPercent);
            if (!this.jiroboIntroPlayed) {
               this.jiroboIntroPlayed = true;
               this.jiroboBroadcast("§6Jirobo: §eHeh... you look weak. This won't take long.");
            }

            int earthSlideCDBase = this.jiroboPhase >= 3 ? 40 : (this.jiroboPhase >= 2 ? 50 : 65);
            if (this.jiroboEarthSlideCD <= 0 && !this.isDashing && dist >= (double)6.0F && dist <= (double)20.0F) {
               this.jiroboEarthSlide(target);
               this.jiroboEarthSlideCD = this.cdMul(earthSlideCDBase);
            } else {
               int chargeBase = this.jiroboPhase >= 3 ? 80 : (this.jiroboPhase >= 2 ? 100 : 140);
               if (this.jiroboShoulderChargeCD <= 0 && !this.isDashing && dist >= (double)8.0F && dist <= (double)30.0F) {
                  this.jiroboShoulderCharge(target);
                  this.jiroboShoulderChargeCD = this.cdMul(chargeBase);
               } else if (this.jiroboPhase >= 2 && this.jiroboGroundPoundCD <= 0 && !this.isDashing && dist >= (double)4.0F && dist <= (double)18.0F) {
                  this.jiroboLeapingGroundPound(target);
                  this.jiroboGroundPoundCD = this.cdMul(this.jiroboPhase >= 3 ? 100 : 160);
               } else {
                  int domeBase = this.jiroboPhase >= 3 ? 120 : (this.jiroboPhase >= 2 ? 160 : 200);
                  if (this.jiroboEarthDomeCD <= 0 && dist <= (double)15.0F && dist >= (double)2.0F) {
                     this.jiroboEarthDomePrison(target);
                     this.jiroboEarthDomeCD = this.cdMul(domeBase);
                  } else if (this.jiroboPhase >= 3 && this.jiroboEarthBarrierCD <= 0 && !this.jiroboBarrierActive && hpPercent < 0.25F) {
                     this.jiroboActivateEarthBarrier();
                     this.jiroboEarthBarrierCD = this.cdMul(120);
                  } else if (dist <= (double)3.5F && this.jiroboMeleeCD <= 0) {
                     if (this.jiroboPhase >= 2 && this.jiroboComboCD <= 0 && this.rand.nextFloat() < 0.35F) {
                        this.jiroboMeleeCombo(target);
                        this.jiroboComboCD = this.cdMul(this.jiroboPhase >= 3 ? 40 : 60);
                        this.jiroboMeleeCD = 15;
                     } else {
                        this.jiroboHeavyPunch(target);
                        this.jiroboMeleeCD = this.cdMul(this.jiroboPhase >= 3 ? 15 : (this.jiroboPhase >= 2 ? 20 : 25));
                     }

                  } else {
                     if (dist > (double)3.5F) {
                        this.getNavigator().tryMoveToEntityLiving(target, this.jiroboPhase >= 3 ? 1.4 : (this.jiroboPhase >= 2 ? (double)1.25F : 1.1));
                        this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
                     }

                  }
               }
            }
         }
      }

      protected void tickStyleCooldowns() {
         if (this.jiroboEarthSlideCD > 0) {
            --this.jiroboEarthSlideCD;
         }

         if (this.jiroboShoulderChargeCD > 0) {
            --this.jiroboShoulderChargeCD;
         }

         if (this.jiroboEarthDomeCD > 0) {
            --this.jiroboEarthDomeCD;
         }

         if (this.jiroboGroundPoundCD > 0) {
            --this.jiroboGroundPoundCD;
         }

         if (this.jiroboEarthBarrierCD > 0) {
            --this.jiroboEarthBarrierCD;
         }

         if (this.jiroboComboCD > 0) {
            --this.jiroboComboCD;
         }

         if (this.jiroboMeleeCD > 0) {
            --this.jiroboMeleeCD;
         }

         if (this.jiroboBarrierActive) {
            --this.jiroboBarrierTicks;
            if (this.jiroboBarrierTicks <= 0) {
               this.jiroboBarrierActive = false;
            }
         }

      }

      protected void resetCombatState() {
         this.jiroboPhase = 1;
         this.jiroboIntroPlayed = false;
         this.jiroboBarrierActive = false;
         this.jiroboBarrierTicks = 0;
         this.jiroboEarthSlideCD = 0;
         this.jiroboShoulderChargeCD = 0;
         this.jiroboEarthDomeCD = 0;
         this.jiroboGroundPoundCD = 0;
         this.jiroboEarthBarrierCD = 0;
         this.jiroboComboCD = 0;
         this.jiroboMeleeCD = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("jiroboPhase", this.jiroboPhase);
         compound.setBoolean("jiroboIntroPlayed", this.jiroboIntroPlayed);
         compound.setInteger("jiroboEarthSlideCD", this.jiroboEarthSlideCD);
         compound.setInteger("jiroboShoulderChargeCD", this.jiroboShoulderChargeCD);
         compound.setInteger("jiroboEarthDomeCD", this.jiroboEarthDomeCD);
         compound.setInteger("jiroboGroundPoundCD", this.jiroboGroundPoundCD);
         compound.setInteger("jiroboEarthBarrierCD", this.jiroboEarthBarrierCD);
         compound.setInteger("jiroboComboCD", this.jiroboComboCD);
         compound.setBoolean("jiroboBarrierActive", this.jiroboBarrierActive);
         compound.setInteger("jiroboBarrierTicks", this.jiroboBarrierTicks);
         compound.setInteger("jiroboMeleeCD", this.jiroboMeleeCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.jiroboPhase = compound.hasKey("jiroboPhase") ? compound.getInteger("jiroboPhase") : 1;
         this.jiroboIntroPlayed = compound.getBoolean("jiroboIntroPlayed");
         this.jiroboEarthSlideCD = compound.hasKey("jiroboEarthSlideCD") ? compound.getInteger("jiroboEarthSlideCD") : 0;
         this.jiroboShoulderChargeCD = compound.hasKey("jiroboShoulderChargeCD") ? compound.getInteger("jiroboShoulderChargeCD") : 0;
         this.jiroboEarthDomeCD = compound.hasKey("jiroboEarthDomeCD") ? compound.getInteger("jiroboEarthDomeCD") : 0;
         this.jiroboGroundPoundCD = compound.hasKey("jiroboGroundPoundCD") ? compound.getInteger("jiroboGroundPoundCD") : 0;
         this.jiroboEarthBarrierCD = compound.hasKey("jiroboEarthBarrierCD") ? compound.getInteger("jiroboEarthBarrierCD") : 0;
         this.jiroboComboCD = compound.hasKey("jiroboComboCD") ? compound.getInteger("jiroboComboCD") : 0;
         this.jiroboBarrierActive = compound.getBoolean("jiroboBarrierActive");
         this.jiroboBarrierTicks = compound.hasKey("jiroboBarrierTicks") ? compound.getInteger("jiroboBarrierTicks") : 0;
         this.jiroboMeleeCD = compound.hasKey("jiroboMeleeCD") ? compound.getInteger("jiroboMeleeCD") : 0;
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         Entity trueSource = source.getTrueSource();
         if (this.jiroboBarrierActive) {
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + (double)1.0F, this.posZ, 10, (double)0.5F, 0.8, (double)0.5F, 0.03, new int[]{Block.getStateId(Blocks.HARDENED_CLAY.getDefaultState())});
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_STONE_HIT, SoundCategory.HOSTILE, 1.0F, 0.6F);
            return -1.0F;
         } else {
            if (this.jiroboPhase >= 3) {
               amount *= 0.85F;
            }

            if (!this.world.isRemote && trueSource instanceof EntityLivingBase && (double)this.getDistance(trueSource) <= (double)3.5F) {
               float absorbChance = this.jiroboPhase >= 3 ? 0.3F : (this.jiroboPhase >= 2 ? 0.2F : 0.12F);
               if (this.rand.nextFloat() < absorbChance) {
                  EntityLivingBase attacker = (EntityLivingBase)trueSource;
                  int weakDuration = this.jiroboPhase >= 3 ? 80 : (this.jiroboPhase >= 2 ? 60 : 40);
                  attacker.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, weakDuration, 0));
                  if (attacker instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)attacker).sendMessage(new TextComponentString("§6§o* Jirobo absorbs your chakra... *"));
                  }

                  float healAmount = amount * 0.15F;
                  this.setHealth(Math.min(this.getHealth() + healAmount, this.getMaxHealth()));
               }
            }

            return amount;
         }
      }

      protected void endDash() {
         int endedType = this.dashType;
         super.endDash();
         if (endedType == 4) {
            this.jiroboGroundPoundLanding();
         }

      }

      private void jiroboCheckPhaseTransition(float hpPercent) {
         if (this.jiroboPhase == 1 && hpPercent <= 0.65F) {
            this.jiroboPhase = 2;
            this.jiroboBroadcast("§6Jirobo: §eYou think you can beat me? I'll show you real power!");
            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               double baseSpeed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(baseSpeed * 1.15);
            }

            if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
               double baseDmg = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(baseDmg * 1.2);
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)0.5F, 0.8, (double)0.5F, 0.1, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + (double)0.5F, this.posZ, 20, 0.6, 0.3, 0.6, 0.05, new int[]{Block.getStateId(Blocks.HARDENED_CLAY.getDefaultState())});
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.5F, 0.5F);
         }

         if (this.jiroboPhase == 2 && hpPercent <= 0.3F) {
            this.jiroboPhase = 3;
            this.jiroboBroadcast("§4§lJirobo: THIS IS THE POWER LORD OROCHIMARU GAVE ME!");
            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               double baseSpeed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(baseSpeed * 1.087);
            }

            if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
               double baseDmg = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(baseDmg * 1.167);
            }

            if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
               double currentArmor = this.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(currentArmor + (double)10.0F);
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 50, 0.6, (double)1.0F, 0.6, 0.15, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 20, 0.4, 0.6, 0.4, 0.05, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + (double)0.5F, this.posZ, 30, 0.8, 0.4, 0.8, 0.08, new int[]{Block.getStateId(Blocks.HARDENED_CLAY.getDefaultState())});
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.5F, 0.7F);
         }

      }

      private void jiroboEarthSlide(EntityLivingBase target) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (!(horizDist < (double)0.5F)) {
            double nx = dx / horizDist;
            double nz = dz / horizDist;
            double dashDist = Math.max(horizDist - (double)1.5F, (double)1.0F);
            double velocityPerTick = dashDist / (double)8.0F;
            this.isDashing = true;
            this.dashType = 1;
            this.dashDuration = 8;
            this.dashTicksRemaining = 8;
            this.dashVelX = nx * velocityPerTick * 1.2;
            this.dashVelZ = nz * velocityPerTick * 1.2;
            this.dashArcSustain = 0.04F;
            this.dashFallAccel = 0.06F;
            this.dashMaxFall = -0.4F;
            this.dashHasMidairGuidance = false;
            this.motionY = 0.15;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.2, this.posZ, 15, 0.4, 0.15, 0.4, 0.05, new int[]{Block.getStateId(Blocks.DIRT.getDefaultState())});
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 0.7F);
         }
      }

      private void jiroboShoulderCharge(EntityLivingBase target) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (!(horizDist < (double)0.5F)) {
            double nx = dx / horizDist;
            double nz = dz / horizDist;
            int duration = this.jiroboPhase >= 3 ? 10 : 12;
            double dashDist = Math.max(horizDist - (double)1.0F, (double)2.0F);
            double velocityPerTick = dashDist / (double)duration;
            this.isDashing = true;
            this.dashType = 2;
            this.dashDuration = duration;
            this.dashTicksRemaining = duration;
            this.dashVelX = nx * velocityPerTick * 1.4;
            this.dashVelZ = nz * velocityPerTick * 1.4;
            this.dashArcSustain = 0.06F;
            this.dashFallAccel = 0.07F;
            this.dashMaxFall = -0.5F;
            this.dashHasMidairGuidance = false;
            this.motionY = (double)0.25F;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.3, this.posZ, 25, 0.6, 0.2, 0.6, 0.08, new int[]{Block.getStateId(Blocks.HARDENED_CLAY.getDefaultState())});
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 10, 0.4, 0.3, 0.4, 0.03, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_IRONGOLEM_ATTACK, SoundCategory.HOSTILE, 1.3F, 0.6F);
            this.jiroboBroadcast("§6Jirobo: §eOut of my way!");
         }
      }

      private void jiroboLeapingGroundPound(EntityLivingBase target) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (!(horizDist < (double)0.5F)) {
            double nx = dx / horizDist;
            double nz = dz / horizDist;
            double dashDist = Math.max(horizDist - (double)0.5F, (double)1.0F);
            double velocityPerTick = dashDist / (double)14.0F;
            this.isDashing = true;
            this.dashType = 4;
            this.dashDuration = 14;
            this.dashTicksRemaining = 14;
            this.dashVelX = nx * velocityPerTick * 0.9;
            this.dashVelZ = nz * velocityPerTick * 0.9;
            this.dashArcSustain = 0.0F;
            this.dashFallAccel = 0.12F;
            this.dashMaxFall = -1.2F;
            this.dashHasMidairGuidance = true;
            this.motionY = 0.85;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.1, this.posZ, 30, 0.8, 0.1, 0.8, 0.1, new int[]{Block.getStateId(Blocks.DIRT.getDefaultState())});
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_IRONGOLEM_ATTACK, SoundCategory.HOSTILE, 1.5F, 0.5F);
         }
      }

      private void jiroboEarthDomePrison(EntityLivingBase target) {
         float radius = this.jiroboPhase >= 3 ? 5.0F : (this.jiroboPhase >= 2 ? 4.0F : 3.0F);
         float baseDamage = this.jiroboPhase >= 3 ? 12.0F : (this.jiroboPhase >= 2 ? 8.0F : 5.0F);
         float dmgMul = (float)this.getDamageMultiplier();
         float normalDmg;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            normalDmg = baseDamage * (1.0F - this.trueDamageSplit) * dmgMul;
            trueDmg = baseDamage * this.trueDamageSplit * dmgMul * this.trueDamageMultiplier;
         } else {
            normalDmg = baseDamage * dmgMul;
            trueDmg = 0.0F;
         }

         int slowDuration = this.jiroboPhase >= 3 ? 80 : (this.jiroboPhase >= 2 ? 60 : 40);
         int slowLevel = this.jiroboPhase >= 3 ? 2 : (this.jiroboPhase >= 2 ? 1 : 0);

         for(EntityPlayer p : this.world.playerEntities) {
            if (p.getDistance(target.posX, target.posY, target.posZ) <= (double)radius) {
               p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
               if (trueDmg > 0.0F) {
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
               }

               p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, slowDuration, slowLevel));
               p.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, slowDuration, 1));
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int angle = 0; angle < 360; angle += 15) {
               double rad = Math.toRadians((double)angle);
               double px = target.posX + Math.cos(rad) * (double)radius;
               double pz = target.posZ + Math.sin(rad) * (double)radius;
               ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, px, target.posY + 0.3, pz, 5, 0.2, 0.3, 0.2, 0.02, new int[]{Block.getStateId(Blocks.HARDENED_CLAY.getDefaultState())});
            }

            ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, target.posX, target.posY + (double)0.5F, target.posZ, 40, (double)1.0F, (double)0.5F, (double)1.0F, 0.1, new int[]{Block.getStateId(Blocks.DIRT.getDefaultState())});
            ws.spawnParticle(EnumParticleTypes.CLOUD, target.posX, target.posY + (double)1.0F, target.posZ, 10, (double)0.5F, (double)0.5F, (double)0.5F, 0.03, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.2F, 0.6F);
         this.jiroboBroadcast("§6Jirobo: §eEarth Style: Dome Prison!");
      }

      private void jiroboActivateEarthBarrier() {
         this.jiroboBarrierActive = true;
         this.jiroboBarrierTicks = 40;
         if (this.world instanceof WorldServer) {
            for(int angle = 0; angle < 360; angle += 20) {
               double rad = Math.toRadians((double)angle);
               double px = this.posX + Math.cos(rad) * 1.2;
               double pz = this.posZ + Math.sin(rad) * 1.2;
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, px, this.posY + (double)1.0F, pz, 8, 0.15, (double)0.5F, 0.15, 0.02, new int[]{Block.getStateId(Blocks.HARDENED_CLAY.getDefaultState())});
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.HOSTILE, 1.5F, 0.5F);
         this.jiroboBroadcast("§4Jirobo: §eEarth Style: Earth Barrier!");
      }

      private void jiroboHeavyPunch(EntityLivingBase target) {
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float dmgMul = (float)this.getDamageMultiplier();
         float normalDmg;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            normalDmg = baseDmg * 1.3F * (1.0F - this.trueDamageSplit) * dmgMul;
            trueDmg = baseDmg * 1.3F * this.trueDamageSplit * dmgMul * this.trueDamageMultiplier;
         } else {
            normalDmg = baseDmg * 1.3F * dmgMul;
            trueDmg = this.jiroboPhase >= 3 ? 6.0F : (this.jiroboPhase >= 2 ? 4.0F : 3.0F);
         }

         target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         double kx = target.posX - this.posX;
         double kz = target.posZ - this.posZ;
         double kDist = Math.sqrt(kx * kx + kz * kz);
         if (kDist > (double)0.0F) {
            float knockStr = this.jiroboPhase >= 3 ? 1.2F : (this.jiroboPhase >= 2 ? 0.9F : 0.6F);
            target.motionX += kx / kDist * (double)knockStr;
            target.motionY += (double)0.25F;
            target.motionZ += kz / kDist * (double)knockStr;
            target.velocityChanged = true;
         }

         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 10, 0.3, 0.3, 0.3, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_IRONGOLEM_ATTACK, SoundCategory.HOSTILE, 1.0F, 0.8F);
      }

      private void jiroboMeleeCombo(EntityLivingBase target) {
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float dmgMul = (float)this.getDamageMultiplier();
         int hits = this.jiroboPhase >= 3 ? 3 : 2;

         for(int i = 0; i < hits; ++i) {
            float hitNormal;
            float hitTrue;
            if (this.trueDamageSplit > 0.0F) {
               hitNormal = baseDmg * (0.7F + (float)i * 0.15F) * (1.0F - this.trueDamageSplit) * dmgMul;
               hitTrue = baseDmg * (0.7F + (float)i * 0.15F) * this.trueDamageSplit * dmgMul * this.trueDamageMultiplier;
            } else {
               hitNormal = baseDmg * (0.7F + (float)i * 0.15F) * dmgMul;
               hitTrue = 0.0F;
            }

            target.attackEntityFrom(DamageSource.causeMobDamage(this), hitNormal);
            target.hurtResistantTime = 0;
            if (hitTrue > 0.0F) {
               target.attackEntityFrom(DamageSource.MAGIC, hitTrue);
            }
         }

         float finishTrue;
         if (this.trueDamageSplit > 0.0F) {
            finishTrue = baseDmg * this.trueDamageSplit * dmgMul * this.trueDamageMultiplier;
         } else {
            finishTrue = this.jiroboPhase >= 3 ? 5.0F : 3.0F;
         }

         target.attackEntityFrom(DamageSource.MAGIC, finishTrue);
         double kx = target.posX - this.posX;
         double kz = target.posZ - this.posZ;
         double kDist = Math.sqrt(kx * kx + kz * kz);
         if (kDist > (double)0.0F) {
            target.motionX += kx / kDist * 0.7;
            target.motionY += 0.2;
            target.motionZ += kz / kDist * 0.7;
            target.velocityChanged = true;
         }

         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 15, 0.3, 0.3, 0.3, 0.15, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 1.2F, 0.7F);
      }

      private void jiroboGroundPoundLanding() {
         float radius = this.jiroboPhase >= 3 ? 5.0F : 4.0F;
         float baseDamage = this.jiroboPhase >= 3 ? 18.0F : 12.0F;
         float damage = (float)((double)baseDamage * this.getDamageMultiplier());

         for(EntityPlayer p : this.world.playerEntities) {
            if (p.getDistance(this) <= radius) {
               p.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
               p.hurtResistantTime = 0;
               p.attackEntityFrom(DamageSource.MAGIC, this.jiroboPhase >= 3 ? 6.0F : 4.0F);
               double kx = p.posX - this.posX;
               double kz = p.posZ - this.posZ;
               double kDist = Math.sqrt(kx * kx + kz * kz);
               if (kDist > (double)0.0F) {
                  p.motionX += kx / kDist * (double)1.0F;
                  p.motionY += 0.4;
                  p.motionZ += kz / kDist * (double)1.0F;
                  p.velocityChanged = true;
               }
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.2, this.posZ, 50, (double)radius * 0.7, 0.3, (double)radius * 0.7, 0.15, new int[]{Block.getStateId(Blocks.DIRT.getDefaultState())});
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 25, (double)radius * (double)0.5F, 0.4, (double)radius * (double)0.5F, 0.05, new int[0]);
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 3, (double)0.5F, 0.2, (double)0.5F, (double)0.0F, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 0.5F);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.HOSTILE, 1.2F, 0.4F);
      }

      private void jiroboBroadcast(String msg) {
         for(EntityPlayer p : this.world.playerEntities) {
            if ((double)p.getDistance(this) < (double)60.0F) {
               ((EntityPlayerMP)p).sendMessage(new TextComponentString(msg));
            }
         }

      }
   }
}
