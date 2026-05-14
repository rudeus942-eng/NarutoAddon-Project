
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.ModelPlayerPoseable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcPose;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityDeidaraBoss extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 257;

   public EntityDeidaraBoss(ElementsInfTsukAddon instance) {
      super(instance, 257);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "deidaraboss"), 257).name("deidaraboss").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, DeidaraBossRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class DeidaraBossRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public DeidaraBossRenderer(RenderManager renderManager) {
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
      private int deidaraPhase = 1;
      private boolean deidara_isAirborne = false;
      private int deidara_airborneTimer = 0;
      private double deidara_hoverTargetY = (double)0.0F;
      private int deidara_strafeTimer = 0;
      private boolean deidara_strafingRight = false;
      private boolean deidara_c3Charging = false;
      private int deidara_c3ChargeTimer = 0;
      private double deidara_c3TargetX;
      private double deidara_c3TargetY;
      private double deidara_c3TargetZ;
      private boolean deidara_phase2Announced = false;
      private boolean deidara_phase3Announced = false;
      private int deidara_attackAnimTimer = 0;
      private int deidara_invulnTicks = 0;
      private int deidara_attackTick = 0;
      private int deidara_clayCloneCooldown = 0;
      private int deidara_c1CD = 0;
      private int deidara_c2CD = 0;
      private int deidara_c3CD = 0;
      private int deidara_mineCD = 0;
      private int deidara_evasiveCD = 0;
      private int deidara_meleeCD = 0;
      private final List<DeidaraClayMine> deidara_activeMines = new ArrayList();
      private static final float DEIDARA_MELEE_DMG = 8.0F;
      private static final float DEIDARA_MELEE_TRUE = 2.0F;
      private static final float DEIDARA_C1_DMG = 15.0F;
      private static final float DEIDARA_C1_TRUE = 4.0F;
      private static final float DEIDARA_MINE_DMG = 12.0F;
      private static final float DEIDARA_MINE_TRUE = 3.0F;
      private static final float DEIDARA_C2_DMG = 10.0F;
      private static final float DEIDARA_C2_TRUE = 3.0F;
      private static final float DEIDARA_C3_DMG = 30.0F;
      private static final float DEIDARA_C3_TRUE = 10.0F;

      public EntityCustom(World world) {
         super(world);
      }

      protected boolean usesVanillaMeleeAI() {
         return false;
      }

      public void fall(float distance, float multiplier) {
         if (!this.deidara_isAirborne && this.deidaraPhase < 2) {
            super.fall(distance, multiplier);
         }
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         if (this.deidara_invulnTicks > 0) {
            return -1.0F;
         } else if (this.deidara_clayCloneCooldown <= 0 && this.getRNG().nextFloat() < 0.15F && amount >= 5.0F) {
            this.deidara_clayCloneCooldown = 200;
            this.deidara_invulnTicks = 5;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int i = 0; i < 20; ++i) {
                  ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.getRNG().nextDouble() - (double)0.5F) * 0.8, this.posY + this.getRNG().nextDouble() * 1.8, this.posZ + (this.getRNG().nextDouble() - (double)0.5F) * 0.8, 1, (double)0.0F, 0.03, (double)0.0F, 0.02, new int[0]);
               }

               ws.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX, this.posY + (double)1.0F, this.posZ, 5, 0.3, 0.3, 0.3, 0.02, new int[0]);
            }

            Entity attacker = source.getTrueSource();
            if (attacker != null) {
               double dx = this.posX - attacker.posX;
               double dz = this.posZ - attacker.posZ;
               double d = Math.sqrt(dx * dx + dz * dz);
               if (d > (double)0.0F) {
                  double sideAngle = this.getRNG().nextBoolean() ? (Math.PI / 2D) : (-Math.PI / 2D);
                  double dashX = dx / d * Math.cos(sideAngle) - dz / d * Math.sin(sideAngle);
                  double dashZ = dx / d * Math.sin(sideAngle) + dz / d * Math.cos(sideAngle);
                  this.motionX = dashX * 1.8;
                  this.motionY = 0.3;
                  this.motionZ = dashZ * 1.8;
                  this.velocityChanged = true;
               }
            }

            return -1.0F;
         } else if (this.deidara_isAirborne && this.getRNG().nextFloat() < 0.15F) {
            this.motionX += (this.getRNG().nextDouble() - (double)0.5F) * (double)1.0F;
            this.motionZ += (this.getRNG().nextDouble() - (double)0.5F) * (double)1.0F;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 8, 0.3, 0.3, 0.3, 0.05, new int[0]);
            }

            return -1.0F;
         } else {
            return amount;
         }
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            float hpPercent = this.getHealth() / this.getMaxHealth();
            this.deidaraCheckPhaseTransition(hpPercent);
            if (!this.deidara_c3Charging) {
               this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
               this.deidaraHandleMovement(target, dist);
               switch (this.deidaraPhase) {
                  case 1:
                     this.deidaraPhase1(target, dist);
                     break;
                  case 2:
                     this.deidaraPhase2(target, dist);
                     break;
                  case 3:
                     this.deidaraPhase3(target, dist);
               }

            }
         }
      }

      protected void tickStyleCooldowns() {
         if (this.deidara_c1CD > 0) {
            --this.deidara_c1CD;
         }

         if (this.deidara_c2CD > 0) {
            --this.deidara_c2CD;
         }

         if (this.deidara_c3CD > 0) {
            --this.deidara_c3CD;
         }

         if (this.deidara_mineCD > 0) {
            --this.deidara_mineCD;
         }

         if (this.deidara_evasiveCD > 0) {
            --this.deidara_evasiveCD;
         }

         if (this.deidara_meleeCD > 0) {
            --this.deidara_meleeCD;
         }

         if (this.deidara_attackAnimTimer > 0) {
            --this.deidara_attackAnimTimer;
         }

         if (this.deidara_invulnTicks > 0) {
            --this.deidara_invulnTicks;
         }

         if (this.deidara_clayCloneCooldown > 0) {
            --this.deidara_clayCloneCooldown;
         }

         ++this.deidara_attackTick;
         if (this.deidara_isAirborne) {
            this.deidaraProcessAerialMovement();
         }

         if (this.deidara_c3Charging) {
            this.deidaraProcessC3Charge();
         }

         this.deidaraProcessClayMines();
      }

      protected void resetCombatState() {
         this.deidaraPhase = 1;
         this.deidara_isAirborne = false;
         this.deidara_airborneTimer = 0;
         this.deidara_strafeTimer = 0;
         this.deidara_c3Charging = false;
         this.deidara_c3ChargeTimer = 0;
         this.deidara_phase2Announced = false;
         this.deidara_phase3Announced = false;
         this.deidara_attackAnimTimer = 0;
         this.deidara_invulnTicks = 0;
         this.deidara_attackTick = 0;
         this.deidara_clayCloneCooldown = 0;
         this.deidara_c1CD = 0;
         this.deidara_c2CD = 0;
         this.deidara_c3CD = 0;
         this.deidara_mineCD = 0;
         this.deidara_evasiveCD = 0;
         this.deidara_meleeCD = 0;
         this.deidara_activeMines.clear();
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("deidaraPhase", this.deidaraPhase);
         compound.setBoolean("deidara_isAirborne", this.deidara_isAirborne);
         compound.setBoolean("deidara_phase2Announced", this.deidara_phase2Announced);
         compound.setBoolean("deidara_phase3Announced", this.deidara_phase3Announced);
         compound.setInteger("deidara_c1CD", this.deidara_c1CD);
         compound.setInteger("deidara_c2CD", this.deidara_c2CD);
         compound.setInteger("deidara_c3CD", this.deidara_c3CD);
         compound.setInteger("deidara_mineCD", this.deidara_mineCD);
         compound.setInteger("deidara_evasiveCD", this.deidara_evasiveCD);
         compound.setInteger("deidara_meleeCD", this.deidara_meleeCD);
         compound.setInteger("deidara_clayCloneCooldown", this.deidara_clayCloneCooldown);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.deidaraPhase = compound.hasKey("deidaraPhase") ? compound.getInteger("deidaraPhase") : 1;
         this.deidara_isAirborne = compound.getBoolean("deidara_isAirborne");
         this.deidara_phase2Announced = compound.getBoolean("deidara_phase2Announced");
         this.deidara_phase3Announced = compound.getBoolean("deidara_phase3Announced");
         this.deidara_c1CD = compound.getInteger("deidara_c1CD");
         this.deidara_c2CD = compound.getInteger("deidara_c2CD");
         this.deidara_c3CD = compound.getInteger("deidara_c3CD");
         this.deidara_mineCD = compound.getInteger("deidara_mineCD");
         this.deidara_evasiveCD = compound.getInteger("deidara_evasiveCD");
         this.deidara_meleeCD = compound.getInteger("deidara_meleeCD");
         this.deidara_clayCloneCooldown = compound.hasKey("deidara_clayCloneCooldown") ? compound.getInteger("deidara_clayCloneCooldown") : 0;
      }

      public void onCombatDeath() {
         this.deidara_activeMines.clear();
      }

      private void deidaraCheckPhaseTransition(float hpPercent) {
         if (this.deidaraPhase == 1 && hpPercent <= 0.65F) {
            this.deidaraPhase = 2;
            this.deidaraOnPhaseTransition(2);
         }

         if (this.deidaraPhase == 2 && hpPercent <= 0.3F) {
            this.deidaraPhase = 3;
            this.deidaraOnPhaseTransition(3);
         }

      }

      private void deidaraOnPhaseTransition(int newPhase) {
         if (newPhase == 2 && !this.deidara_phase2Announced) {
            this.deidara_phase2Announced = true;
            this.deidaraBroadcast("§e§lDeidara: §7\"Now I'll show you art from above, hm!\"");
            this.deidara_isAirborne = true;
            this.deidara_airborneTimer = 0;
            this.motionY = 0.8;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int i = 0; i < 30; ++i) {
                  ws.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (this.getRNG().nextDouble() - (double)0.5F) * (double)3.0F, this.posY + this.getRNG().nextDouble(), this.posZ + (this.getRNG().nextDouble() - (double)0.5F) * (double)3.0F, 1, 0.1, 0.2, 0.1, 0.01, new int[0]);
               }
            }
         }

         if (newPhase == 3 && !this.deidara_phase3Announced) {
            this.deidara_phase3Announced = true;
            this.deidaraBroadcast("§4§lDeidara: §7\"Art is an EXPLOSION! This is my ultimate masterpiece!\"");
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int i = 0; i < 50; ++i) {
                  ws.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (this.getRNG().nextDouble() - (double)0.5F) * (double)5.0F, this.posY + this.getRNG().nextDouble() * (double)3.0F, this.posZ + (this.getRNG().nextDouble() - (double)0.5F) * (double)5.0F, 1, 0.2, 0.3, 0.2, 0.02, new int[0]);
               }

               for(int i = 0; i < 30; ++i) {
                  ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.getRNG().nextDouble() - (double)0.5F) * (double)4.0F, this.posY + this.getRNG().nextDouble() * (double)2.0F, this.posZ + (this.getRNG().nextDouble() - (double)0.5F) * (double)4.0F, 1, (double)0.0F, 0.1, (double)0.0F, 0.02, new int[0]);
               }

               for(int i = 0; i < 15; ++i) {
                  ws.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX + (this.getRNG().nextDouble() - (double)0.5F) * (double)3.0F, this.posY + this.getRNG().nextDouble() * (double)2.5F, this.posZ + (this.getRNG().nextDouble() - (double)0.5F) * (double)3.0F, 1, (double)0.0F, 0.05, (double)0.0F, 0.01, new int[0]);
               }
            }
         }

      }

      private void deidaraHandleMovement(EntityLivingBase target, double dist) {
         ++this.deidara_strafeTimer;
         if (this.deidara_isAirborne) {
            if (this.deidara_strafeTimer > 30) {
               this.deidara_strafingRight = !this.deidara_strafingRight;
               this.deidara_strafeTimer = 0;
            }

            double angle = Math.atan2(target.posZ - this.posZ, target.posX - this.posX);
            double strafeSpeed = 0.3;
            double perpX = (double)(this.deidara_strafingRight ? -1 : 1) * Math.sin(angle) * strafeSpeed;
            double perpZ = (double)(this.deidara_strafingRight ? 1 : -1) * Math.cos(angle) * strafeSpeed;
            this.motionX = perpX;
            this.motionZ = perpZ;
            double horizDist = Math.sqrt((this.posX - target.posX) * (this.posX - target.posX) + (this.posZ - target.posZ) * (this.posZ - target.posZ));
            if (horizDist < (double)10.0F) {
               this.motionX += (this.posX - target.posX) / horizDist * 0.15;
               this.motionZ += (this.posZ - target.posZ) / horizDist * 0.15;
            } else if (horizDist > (double)25.0F) {
               this.motionX += (target.posX - this.posX) / horizDist * 0.15;
               this.motionZ += (target.posZ - this.posZ) / horizDist * 0.15;
            }

            this.velocityChanged = true;
         } else {
            switch (this.deidaraPhase) {
               case 1:
                  if (dist > (double)25.0F) {
                     this.getNavigator().tryMoveToEntityLiving(target, 1.1);
                  } else if (dist < (double)8.0F) {
                     this.deidaraRetreatFrom(target, (double)8.0F, 1.2);
                  } else {
                     if (this.deidara_strafeTimer > 35) {
                        this.deidara_strafingRight = !this.deidara_strafingRight;
                        this.deidara_strafeTimer = 0;
                     }

                     double angle = Math.atan2(target.posZ - this.posZ, target.posX - this.posX);
                     double perpX = (double)(this.deidara_strafingRight ? -1 : 1) * Math.sin(angle) * (double)5.0F;
                     double perpZ = (double)(this.deidara_strafingRight ? 1 : -1) * Math.cos(angle) * (double)5.0F;
                     this.getNavigator().tryMoveToXYZ(this.posX + perpX, this.posY, this.posZ + perpZ, (double)1.0F);
                  }
                  break;
               case 3:
                  if (dist > (double)15.0F) {
                     this.getNavigator().tryMoveToEntityLiving(target, 1.3);
                  } else if (dist < (double)6.0F) {
                     this.deidaraRetreatFrom(target, (double)5.0F, 1.1);
                  } else {
                     if (this.deidara_strafeTimer > 25) {
                        this.deidara_strafingRight = !this.deidara_strafingRight;
                        this.deidara_strafeTimer = 0;
                     }

                     double angle = Math.atan2(target.posZ - this.posZ, target.posX - this.posX);
                     double perpX = (double)(this.deidara_strafingRight ? -1 : 1) * Math.sin(angle) * (double)4.0F;
                     double perpZ = (double)(this.deidara_strafingRight ? 1 : -1) * Math.cos(angle) * (double)4.0F;
                     this.getNavigator().tryMoveToXYZ(this.posX + perpX, this.posY, this.posZ + perpZ, 1.1);
                  }
                  break;
               default:
                  if (dist > (double)20.0F) {
                     this.getNavigator().tryMoveToEntityLiving(target, 1.2);
                  } else if (dist < (double)8.0F) {
                     this.deidaraRetreatFrom(target, (double)6.0F, 1.1);
                  }
            }

         }
      }

      private void deidaraRetreatFrom(EntityLivingBase target, double distance, double speed) {
         double dx = this.posX - target.posX;
         double dz = this.posZ - target.posZ;
         double d = Math.sqrt(dx * dx + dz * dz);
         if (d > (double)0.0F) {
            this.getNavigator().tryMoveToXYZ(this.posX + dx / d * distance, this.posY, this.posZ + dz / d * distance, speed);
         }

      }

      private void deidaraPhase1(EntityLivingBase target, double dist) {
         if (this.deidara_evasiveCD <= 0 && dist < (double)5.0F) {
            this.deidaraEvasiveJump();
         } else if (this.deidara_mineCD <= 0 && dist <= (double)5.0F) {
            this.deidaraPlaceClayMine(target);
         } else if (this.deidara_c1CD <= 0 && dist >= (double)6.0F && dist <= (double)25.0F) {
            this.deidaraFireC1Bomb(target);
         } else {
            if (dist <= (double)3.0F && this.deidara_attackTick >= 15) {
               this.deidaraMeleeKick(target);
            }

         }
      }

      private void deidaraPhase2(EntityLivingBase target, double dist) {
         if (this.deidara_c2CD <= 0 && dist >= (double)8.0F && dist <= (double)30.0F) {
            this.deidaraFireC2DragonSpread(target);
         } else if (this.deidara_c1CD <= 0 && dist >= (double)5.0F) {
            this.deidaraFireC1Bomb(target);
         } else if (this.deidara_mineCD <= 0 && dist < (double)15.0F) {
            this.deidaraPlaceClayMine(target);
         } else {
            if (dist <= (double)3.0F && this.deidara_attackTick >= 12) {
               this.deidaraMeleeKick(target);
            }

         }
      }

      private void deidaraPhase3(EntityLivingBase target, double dist) {
         if (this.deidara_c3CD <= 0 && !this.deidara_c3Charging && dist <= (double)30.0F) {
            this.deidaraStartC3MegaBomb(target);
         } else if (this.deidara_c2CD <= 0 && dist >= (double)8.0F && dist <= (double)30.0F) {
            this.deidaraFireC2DragonSpread(target);
         } else if (this.deidara_c1CD <= 0 && dist >= (double)4.0F) {
            this.deidaraFireC1Bomb(target);
         } else if (this.deidara_mineCD <= 0 && dist < (double)12.0F) {
            this.deidaraPlaceClayMine(target);
         } else if (!this.deidara_isAirborne && this.deidara_evasiveCD <= 0 && dist < (double)5.0F) {
            this.deidaraEvasiveJump();
         } else {
            if (dist <= (double)3.0F && this.deidara_attackTick >= 10) {
               this.deidaraMeleeKick(target);
            }

         }
      }

      private void deidaraFireC1Bomb(EntityLivingBase target) {
         int[] cd = this.deidaraGetCooldownRange(40, 60, 30, 45, 15, 30);
         this.deidara_c1CD = this.cdMul(cd[0] + this.getRNG().nextInt(cd[1] - cd[0] + 1));
         this.deidara_attackAnimTimer = 20;
         double dmgMult = this.getDamageMultiplier();
         float normDmg = (float)((double)15.0F * dmgMult);
         float trueDmg = (float)((double)4.0F * dmgMult);
         EntityClayBomb.EntityCustom bomb = new EntityClayBomb.EntityCustom(this.world, this, normDmg, trueDmg, 3.0F);
         double dx = target.posX - this.posX;
         double dy = target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
         double dz = target.posZ - this.posZ;
         double hdist = Math.sqrt(dx * dx + dz * dz);
         if (this.deidara_isAirborne) {
            bomb.shoot(dx, dy - hdist * 0.01, dz, 1.4F, 1.5F);
         } else {
            bomb.shoot(dx, dy + hdist * 0.04, dz, 1.6F, 0.8F);
         }

         this.world.spawnEntity(bomb);
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 8; ++i) {
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.getRNG().nextDouble() - (double)0.5F), this.posY + 1.2 + this.getRNG().nextDouble() * 0.3, this.posZ + (this.getRNG().nextDouble() - (double)0.5F), 1, (double)0.0F, 0.03, (double)0.0F, (double)0.0F, new int[0]);
            }
         }

      }

      private void deidaraFireC2DragonSpread(EntityLivingBase target) {
         int[] cd = this.deidaraPhase == 3 ? this.getCooldownRange(40, 60) : this.getCooldownRange(60, 90);
         this.deidara_c2CD = cd[0] + this.getRNG().nextInt(cd[1] - cd[0] + 1);
         this.deidara_attackAnimTimer = 25;
         double dmgMult2 = this.getDamageMultiplier();
         float normDmg2 = (float)((double)10.0F * dmgMult2);
         float trueDmg2 = (float)((double)3.0F * dmgMult2);
         double bx = target.posX - this.posX;
         double by = target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
         double bz = target.posZ - this.posZ;
         double bd = Math.sqrt(bx * bx + bz * bz);

         for(int i = 0; i < 3; ++i) {
            EntityClayBomb.EntityCustom bomb2 = new EntityClayBomb.EntityCustom(this.world, this, normDmg2, trueDmg2, 4.5F);
            double spreadAngle = (double)(i - 1) * 0.18;
            double sx = bx * Math.cos(spreadAngle) - bz * Math.sin(spreadAngle);
            double sz = bx * Math.sin(spreadAngle) + bz * Math.cos(spreadAngle);
            if (this.deidara_isAirborne) {
               bomb2.shoot(sx, by - bd * 0.01, sz, 1.3F, 1.5F);
            } else {
               bomb2.shoot(sx, by + bd * 0.04, sz, 1.5F, 1.0F);
            }

            this.world.spawnEntity(bomb2);
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 15; ++i) {
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.getRNG().nextDouble() - (double)0.5F) * (double)2.0F, this.posY + (double)1.0F + this.getRNG().nextDouble(), this.posZ + (this.getRNG().nextDouble() - (double)0.5F) * (double)2.0F, 1, (double)0.0F, 0.05, (double)0.0F, (double)0.0F, new int[0]);
            }
         }

      }

      private void deidaraStartC3MegaBomb(EntityLivingBase target) {
         int[] cd = this.getCooldownRange(200, 300);
         this.deidara_c3CD = cd[0] + this.getRNG().nextInt(cd[1] - cd[0] + 1);
         this.deidara_attackAnimTimer = 70;
         this.deidaraBroadcast("§6§lDeidara: §7\"Katsu! This is my ultimate art!\"");
         this.deidara_c3Charging = true;
         this.deidara_c3ChargeTimer = 60;
         this.deidara_c3TargetX = target.posX;
         this.deidara_c3TargetY = target.posY;
         this.deidara_c3TargetZ = target.posZ;
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 25; ++i) {
               ws.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX + (this.getRNG().nextDouble() - (double)0.5F) * (double)2.0F, this.posY + (double)1.0F + this.getRNG().nextDouble() * (double)2.0F, this.posZ + (this.getRNG().nextDouble() - (double)0.5F) * (double)2.0F, 1, (double)0.0F, 0.1, (double)0.0F, (double)0.0F, new int[0]);
            }
         }

      }

      private void deidaraPlaceClayMine(EntityLivingBase target) {
         int[] cd = this.deidaraGetCooldownRange(80, 120, 60, 100, 40, 70);
         this.deidara_mineCD = this.cdMul(cd[0] + this.getRNG().nextInt(cd[1] - cd[0] + 1));
         this.deidara_attackAnimTimer = 10;
         DeidaraClayMine mine = new DeidaraClayMine(target.posX, target.posY, target.posZ);
         this.deidara_activeMines.add(mine);

         while(this.deidara_activeMines.size() > 5) {
            this.deidara_activeMines.remove(0);
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, target.posX, target.posY + 0.2, target.posZ, 2, 0.1, 0.02, 0.1, (double)0.0F, new int[0]);
         }

      }

      private void deidaraEvasiveJump() {
         int[] cd = this.deidaraGetCooldownRange(100, 140, 60, 100, 40, 70);
         this.deidara_evasiveCD = this.cdMul(cd[0] + this.getRNG().nextInt(cd[1] - cd[0] + 1));
         this.deidara_invulnTicks = 8;
         EntityLivingBase target = this.getAttackTarget();
         double jumpX;
         double jumpZ;
         if (target != null) {
            double dx = this.posX - target.posX;
            double dz = this.posZ - target.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > (double)0.0F) {
               double sideAngle = this.getRNG().nextBoolean() ? (Math.PI / 4D) : (-Math.PI / 4D);
               jumpX = dx / d * Math.cos(sideAngle) - dz / d * Math.sin(sideAngle);
               jumpZ = dx / d * Math.sin(sideAngle) + dz / d * Math.cos(sideAngle);
            } else {
               jumpX = this.getRNG().nextDouble() - (double)0.5F;
               jumpZ = this.getRNG().nextDouble() - (double)0.5F;
            }
         } else {
            jumpX = this.getRNG().nextDouble() - (double)0.5F;
            jumpZ = this.getRNG().nextDouble() - (double)0.5F;
         }

         this.motionX = jumpX * (double)1.5F;
         this.motionY = 0.6;
         this.motionZ = jumpZ * (double)1.5F;
         this.velocityChanged = true;
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 15; ++i) {
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.getRNG().nextDouble() - (double)0.5F) * (double)1.5F, this.posY + this.getRNG().nextDouble() * (double)1.5F, this.posZ + (this.getRNG().nextDouble() - (double)0.5F) * (double)1.5F, 1, (double)0.0F, 0.05, (double)0.0F, (double)0.0F, new int[0]);
            }
         }

      }

      private void deidaraMeleeKick(EntityLivingBase target) {
         this.deidara_attackTick = 0;
         this.swingArm(EnumHand.MAIN_HAND);
         this.deidara_attackAnimTimer = 10;
         double dmgMult = this.getDamageMultiplier();
         float meleeDmg = (float)((double)8.0F * dmgMult);
         float meleeTrueDmg = (float)((double)2.0F * dmgMult);
         target.attackEntityFrom(DamageSource.causeMobDamage(this), meleeDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, meleeTrueDmg);
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double d = Math.sqrt(dx * dx + dz * dz);
         if (d > (double)0.0F) {
            target.motionX += dx / d * 0.8;
            target.motionY += 0.3;
            target.motionZ += dz / d * 0.8;
            if (target instanceof EntityPlayerMP) {
               ((EntityPlayerMP)target).velocityChanged = true;
            }
         }

      }

      private void deidaraProcessAerialMovement() {
         EntityLivingBase target = this.getAttackTarget();
         if (target != null) {
            this.deidara_hoverTargetY = target.posY + (double)8.0F + this.getRNG().nextDouble() * (double)4.0F;
            double dy = this.deidara_hoverTargetY - this.posY;
            if (Math.abs(dy) > (double)1.0F) {
               this.motionY = MathHelper.clamp(dy * 0.1, -0.3, (double)0.5F);
            } else {
               this.motionY = (double)0.0F;
            }

            if (this.motionY < -0.1 && this.posY < target.posY + (double)6.0F) {
               this.motionY = 0.1;
            }

            this.fallDistance = 0.0F;
            this.onGround = false;
            this.velocityChanged = true;
         }
      }

      private void deidaraProcessC3Charge() {
         --this.deidara_c3ChargeTimer;
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            double radius = (double)8.0F;

            for(int i = 0; i < 16; ++i) {
               double angle = (Math.PI / 8D) * (double)i;
               double px = this.deidara_c3TargetX + Math.cos(angle) * radius;
               double pz = this.deidara_c3TargetZ + Math.sin(angle) * radius;
               ws.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, px, this.deidara_c3TargetY + (double)0.5F, pz, 2, 0.1, 0.1, 0.1, 0.01, new int[0]);
            }

            ws.spawnParticle(EnumParticleTypes.CLOUD, this.deidara_c3TargetX, this.deidara_c3TargetY + (double)1.0F, this.deidara_c3TargetZ, 5, (double)0.5F, (double)1.0F, (double)0.5F, 0.02, new int[0]);
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.deidara_c3TargetX, this.deidara_c3TargetY + (double)0.5F, this.deidara_c3TargetZ, 3, 0.3, 0.3, 0.3, 0.01, new int[0]);
         }

         if (this.deidara_c3ChargeTimer <= 0) {
            this.deidara_c3Charging = false;
            this.deidaraDetonateC3();
         }

      }

      private void deidaraDetonateC3() {
         double dmgMult = this.getDamageMultiplier();
         float normDmg = (float)((double)30.0F * dmgMult);
         float trueDmg = (float)((double)10.0F * dmgMult);

         for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(this.deidara_c3TargetX - (double)8.0F, this.deidara_c3TargetY - (double)3.0F, this.deidara_c3TargetZ - (double)8.0F, this.deidara_c3TargetX + (double)8.0F, this.deidara_c3TargetY + (double)8.0F, this.deidara_c3TargetZ + (double)8.0F))) {
            if (e instanceof EntityLivingBase && e != this) {
               EntityLivingBase living = (EntityLivingBase)e;
               double eDist = Math.sqrt((living.posX - this.deidara_c3TargetX) * (living.posX - this.deidara_c3TargetX) + (living.posZ - this.deidara_c3TargetZ) * (living.posZ - this.deidara_c3TargetZ));
               if (eDist <= (double)8.0F) {
                  float falloff = (float)((double)1.0F - eDist / (double)16.0F);
                  living.hurtResistantTime = 0;
                  living.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg * falloff);
                  living.hurtResistantTime = 0;
                  living.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
                  double kbX = living.posX - this.deidara_c3TargetX;
                  double kbZ = living.posZ - this.deidara_c3TargetZ;
                  double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
                  if (kbDist > (double)0.0F) {
                     living.motionX += kbX / kbDist * (double)1.5F;
                     living.motionY += 0.6;
                     living.motionZ += kbZ / kbDist * (double)1.5F;
                     if (living instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)living).velocityChanged = true;
                     }
                  }
               }
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 40; ++i) {
               double angle = this.getRNG().nextDouble() * Math.PI * (double)2.0F;
               double r = this.getRNG().nextDouble() * (double)8.0F;
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.deidara_c3TargetX + Math.cos(angle) * r, this.deidara_c3TargetY + this.getRNG().nextDouble() * (double)5.0F, this.deidara_c3TargetZ + Math.sin(angle) * r, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

            for(int i = 0; i < 80; ++i) {
               double angle = this.getRNG().nextDouble() * Math.PI * (double)2.0F;
               double r = this.getRNG().nextDouble() * (double)7.0F;
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.deidara_c3TargetX + Math.cos(angle) * r, this.deidara_c3TargetY + this.getRNG().nextDouble() * (double)6.0F, this.deidara_c3TargetZ + Math.sin(angle) * r, 1, (double)0.0F, 0.1, (double)0.0F, 0.08, new int[0]);
            }

            for(int i = 0; i < 30; ++i) {
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.deidara_c3TargetX + (this.getRNG().nextDouble() - (double)0.5F) * (double)10.0F, this.deidara_c3TargetY + this.getRNG().nextDouble() * (double)8.0F, this.deidara_c3TargetZ + (this.getRNG().nextDouble() - (double)0.5F) * (double)10.0F, 1, (double)0.0F, 0.15, (double)0.0F, 0.03, new int[0]);
            }

            ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.deidara_c3TargetX, this.deidara_c3TargetY + (double)2.0F, this.deidara_c3TargetZ, 5, (double)2.0F, (double)2.0F, (double)2.0F, (double)0.0F, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.deidara_c3TargetX, this.deidara_c3TargetY, this.deidara_c3TargetZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 3.0F, 0.6F);
         this.deidaraBroadcast("§c§l[C3] §7The ultimate art detonates!");
      }

      private void deidaraProcessClayMines() {
         Iterator<DeidaraClayMine> it = this.deidara_activeMines.iterator();

         while(it.hasNext()) {
            DeidaraClayMine mine = (DeidaraClayMine)it.next();
            if (mine.exploded) {
               it.remove();
            } else {
               --mine.ticksLeft;
               List<EntityPlayer> nearPlayers = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(mine.x - (double)2.0F, mine.y - (double)1.0F, mine.z - (double)2.0F, mine.x + (double)2.0F, mine.y + (double)2.0F, mine.z + (double)2.0F));
               boolean playerNear = false;

               for(EntityPlayer p : nearPlayers) {
                  if (p.isEntityAlive() && !p.isSpectator()) {
                     playerNear = true;
                     break;
                  }
               }

               if (playerNear || mine.ticksLeft <= 0) {
                  this.deidaraExplodeMine(mine);
                  mine.exploded = true;
               }

               if (!mine.exploded && this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, mine.x, mine.y + 0.2, mine.z, 1, 0.1, 0.05, 0.1, (double)0.0F, new int[0]);
               }
            }
         }

         this.deidara_activeMines.removeIf((m) -> m.exploded);
      }

      private void deidaraExplodeMine(DeidaraClayMine mine) {
         double dmgMult = this.getDamageMultiplier();
         float normDmg = (float)((double)12.0F * dmgMult);
         float trueDmg = (float)((double)3.0F * dmgMult);

         for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(mine.x - (double)3.0F, mine.y - (double)1.0F, mine.z - (double)3.0F, mine.x + (double)3.0F, mine.y + (double)3.0F, mine.z + (double)3.0F))) {
            if (e instanceof EntityLivingBase && e != this) {
               EntityLivingBase living = (EntityLivingBase)e;
               living.hurtResistantTime = 0;
               living.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
               living.hurtResistantTime = 0;
               living.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, mine.x, mine.y + (double)0.5F, mine.z, 3, (double)0.5F, (double)0.5F, (double)0.5F, (double)0.0F, new int[0]);

            for(int i = 0; i < 12; ++i) {
               ws.spawnParticle(EnumParticleTypes.CLOUD, mine.x + (this.getRNG().nextDouble() - (double)0.5F) * (double)2.5F, mine.y + this.getRNG().nextDouble() * (double)1.5F, mine.z + (this.getRNG().nextDouble() - (double)0.5F) * (double)2.5F, 1, (double)0.0F, 0.05, (double)0.0F, 0.03, new int[0]);
            }
         }

         this.world.playSound((EntityPlayer)null, mine.x, mine.y, mine.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 1.2F);
      }

      private void deidaraBroadcast(String msg) {
         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F))) {
            p.sendMessage(new TextComponentString(msg));
         }

      }

      private int[] deidaraGetCooldownRange(int p1Min, int p1Max, int p2Min, int p2Max, int p3Min, int p3Max) {
         switch (this.deidaraPhase) {
            case 2:
               return this.getCooldownRange(p2Min, p2Max);
            case 3:
               return this.getCooldownRange(p3Min, p3Max);
            default:
               return this.getCooldownRange(p1Min, p1Max);
         }
      }

      private static class DeidaraClayMine {
         double x;
         double y;
         double z;
         int ticksLeft;
         boolean exploded;

         DeidaraClayMine(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.ticksLeft = 60;
            this.exploded = false;
         }
      }
   }
}
