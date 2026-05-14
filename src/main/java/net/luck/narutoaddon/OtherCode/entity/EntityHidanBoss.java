
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
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityHidanBoss extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 258;

   public EntityHidanBoss(ElementsInfTsukAddon instance) {
      super(instance, 258);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "hidanboss"), 258).name("hidanboss").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, HidanBossRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class HidanBossRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public HidanBossRenderer(RenderManager renderManager) {
         super(renderManager, new ModelPlayerPoseable(0.0F, false), 0.5F);
         this.addLayer(new LayerHeldItem(this));
         this.addLayer(new LayerBipedArmor(this));
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         String variant = entity.getTextureVariant();
         if (variant != null && !variant.isEmpty()) {
            return new ResourceLocation(variant);
         } else {
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
      private static final DataParameter<String> TEXTURE_VARIANT;
      private static final float HIDAN_MELEE_DMG = 9.0F;
      private static final float HIDAN_MELEE_TRUE = 3.0F;
      private static final float HIDAN_COMBO_LIGHT_DMG = 7.5F;
      private static final float HIDAN_COMBO_LIGHT_TRUE = 2.5F;
      private static final float HIDAN_COMBO_HEAVY_DMG = 15.0F;
      private static final float HIDAN_COMBO_HEAVY_TRUE = 5.0F;
      private static final float HIDAN_SCYTHE_THROW_DMG = 12.0F;
      private static final float HIDAN_SCYTHE_THROW_TRUE = 4.0F;
      private static final float HIDAN_RITUAL_SELF_DMG = 4.0F;
      private static final float HIDAN_AOE_SWING_DMG = 11.0F;
      private static final float HIDAN_AOE_SWING_TRUE = 3.5F;
      private static final float HIDAN_AOE_SWING_RADIUS = 4.0F;
      private static final int HIDAN_COMBO_MAX_HITS = 4;
      private static final int[] HIDAN_COMBO_DELAYS;
      private static final float HIDAN_RUSH_MIN_RANGE = 6.0F;
      private static final float HIDAN_RUSH_MAX_RANGE = 20.0F;
      private static final int HIDAN_RUSH_DURATION_TICKS = 8;
      private static final double HIDAN_SCYTHE_THROW_MIN = (double)6.0F;
      private static final double HIDAN_SCYTHE_THROW_MAX = (double)25.0F;
      private static final double HIDAN_MELEE_RANGE = (double)3.0F;
      private static final double HIDAN_COMBO_RANGE = (double)3.5F;
      private static final int HIDAN_P3_MELEE_DURATION = 100;
      private static final int HIDAN_P3_RITUAL_DURATION = 60;
      private static final int HIDAN_MAX_RITUAL_DURATION = 200;
      private int hidanPhase = 1;
      private UUID hidan_linkedPlayerUUID = null;
      private boolean hidan_inRitual = false;
      private int hidan_ritualTimer = 0;
      private int hidan_ritualDamageTick = 0;
      private boolean hidan_phase2Announced = false;
      private boolean hidan_phase3Announced = false;
      private int hidan_regenTimer = 0;
      private boolean hidan_p3MeleePhase = true;
      private int hidan_p3AlternateTimer = 0;
      private boolean hidan_isRushing = false;
      private int hidan_rushTicksRemaining = 0;
      private double hidan_rushVelocityX = (double)0.0F;
      private double hidan_rushVelocityZ = (double)0.0F;
      private boolean hidan_inActiveCombo = false;
      private Entity hidan_activeComboTarget = null;
      private int hidan_comboHitCount = 0;
      private int hidan_comboTimer = 0;
      private int hidan_attackTick = 0;
      private int hidan_stuckSafetyTimer = 0;
      private int hidan_scytheCD = 0;
      private int hidan_rushCD = 0;
      private int hidan_comboCD = 0;
      private int hidan_ritualCD = 0;
      private int hidan_aoeCD = 0;
      private int hidan_meleeCD = 0;

      public EntityCustom(World world) {
         super(world);
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(TEXTURE_VARIANT, "");
      }

      protected boolean usesVanillaMeleeAI() {
         return false;
      }

      public String getTextureVariant() {
         return (String)this.dataManager.get(TEXTURE_VARIANT);
      }

      public void setTextureVariant(String variant) {
         this.dataManager.set(TEXTURE_VARIANT, variant != null ? variant : "");
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            float hpPercent = this.getHealth() / this.getMaxHealth();
            this.hidanCheckPhaseTransition(hpPercent);
            if (!this.hidan_inActiveCombo && !this.hidan_isRushing && !this.hidan_inRitual) {
               ++this.hidan_attackTick;
               this.hidanHandleMovement(target, dist);
               switch (this.hidanPhase) {
                  case 1:
                     this.hidanPhase1(target, dist);
                     break;
                  case 2:
                     this.hidanPhase2(target, dist);
                     break;
                  case 3:
                     this.hidanPhase3(target, dist);
               }

            }
         }
      }

      protected void tickStyleCooldowns() {
         if (this.hidan_scytheCD > 0) {
            --this.hidan_scytheCD;
         }

         if (this.hidan_rushCD > 0) {
            --this.hidan_rushCD;
         }

         if (this.hidan_comboCD > 0) {
            --this.hidan_comboCD;
         }

         if (this.hidan_ritualCD > 0) {
            --this.hidan_ritualCD;
         }

         if (this.hidan_aoeCD > 0) {
            --this.hidan_aoeCD;
         }

         if (this.hidan_meleeCD > 0) {
            --this.hidan_meleeCD;
         }

         if (this.hidan_comboTimer > 0) {
            --this.hidan_comboTimer;
         }

         this.hidanTickProcessing();
      }

      protected void resetCombatState() {
         this.hidanPhase = 1;
         this.hidan_linkedPlayerUUID = null;
         this.hidan_inRitual = false;
         this.hidan_ritualTimer = 0;
         this.hidan_ritualDamageTick = 0;
         this.hidan_phase2Announced = false;
         this.hidan_phase3Announced = false;
         this.hidan_regenTimer = 0;
         this.hidan_p3MeleePhase = true;
         this.hidan_p3AlternateTimer = 0;
         this.hidan_isRushing = false;
         this.hidan_rushTicksRemaining = 0;
         this.hidan_inActiveCombo = false;
         this.hidan_activeComboTarget = null;
         this.hidan_comboHitCount = 0;
         this.hidan_comboTimer = 0;
         this.hidan_attackTick = 0;
         this.hidan_stuckSafetyTimer = 0;
         this.hidan_scytheCD = 0;
         this.hidan_rushCD = 0;
         this.hidan_comboCD = 0;
         this.hidan_ritualCD = 0;
         this.hidan_aoeCD = 0;
         this.hidan_meleeCD = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("hidanPhase", this.hidanPhase);
         compound.setBoolean("hidan_phase2Announced", this.hidan_phase2Announced);
         compound.setBoolean("hidan_phase3Announced", this.hidan_phase3Announced);
         compound.setBoolean("hidan_inRitual", this.hidan_inRitual);
         compound.setInteger("hidan_ritualTimer", this.hidan_ritualTimer);
         compound.setBoolean("hidan_p3MeleePhase", this.hidan_p3MeleePhase);
         compound.setInteger("hidan_p3AlternateTimer", this.hidan_p3AlternateTimer);
         if (this.hidan_linkedPlayerUUID != null) {
            compound.setString("hidan_linkedPlayerUUID", this.hidan_linkedPlayerUUID.toString());
         }

         String variant = this.getTextureVariant();
         if (variant != null && !variant.isEmpty()) {
            compound.setString("textureVariant", variant);
         }

      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.hidanPhase = compound.hasKey("hidanPhase") ? compound.getInteger("hidanPhase") : 1;
         this.hidan_phase2Announced = compound.getBoolean("hidan_phase2Announced");
         this.hidan_phase3Announced = compound.getBoolean("hidan_phase3Announced");
         this.hidan_inRitual = compound.getBoolean("hidan_inRitual");
         this.hidan_ritualTimer = compound.getInteger("hidan_ritualTimer");
         this.hidan_p3MeleePhase = compound.getBoolean("hidan_p3MeleePhase");
         this.hidan_p3AlternateTimer = compound.getInteger("hidan_p3AlternateTimer");
         if (compound.hasKey("hidan_linkedPlayerUUID")) {
            try {
               this.hidan_linkedPlayerUUID = UUID.fromString(compound.getString("hidan_linkedPlayerUUID"));
            } catch (IllegalArgumentException var3) {
            }
         }

         if (compound.hasKey("textureVariant")) {
            this.dataManager.set(TEXTURE_VARIANT, compound.getString("textureVariant"));
         }

         if (this.hidan_inRitual) {
            this.setTextureVariant("inftsukaddon:textures/hidan_ritual.png");
            this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)0.0F);
         }

      }

      private void hidanTickProcessing() {
         if (this.hidan_inRitual) {
            this.hidanTickRitual();
            if (Math.abs(this.motionX) > 0.3 || Math.abs(this.motionZ) > 0.3) {
               this.hidanBroadcast("§6§lHidan: §7\"Tch! You broke my ritual!\"");
               this.hidanExitRitual();
            }
         }

         if (this.hidan_isRushing) {
            this.hidanProcessRushTick();
         }

         if (this.hidan_inActiveCombo && this.hidan_comboTimer <= 0 && this.hidan_activeComboTarget != null) {
            if (!this.hidan_activeComboTarget.isDead && this.getDistanceSq(this.hidan_activeComboTarget) <= (double)16.0F) {
               this.hidanPerformComboHit((EntityLivingBase)this.hidan_activeComboTarget);
            } else {
               this.hidan_inActiveCombo = false;
               this.hidan_activeComboTarget = null;
               this.hidan_comboHitCount = 0;
            }
         }

         if (!this.hidan_inActiveCombo && !this.hidan_isRushing && !this.hidan_inRitual) {
            this.hidan_stuckSafetyTimer = 0;
         } else {
            ++this.hidan_stuckSafetyTimer;
            if (this.hidan_stuckSafetyTimer > 100) {
               if (this.hidan_inActiveCombo) {
                  this.hidan_inActiveCombo = false;
                  this.hidan_activeComboTarget = null;
                  this.hidan_comboHitCount = 0;
               }

               if (this.hidan_isRushing) {
                  this.hidan_isRushing = false;
               }

               if (this.hidan_inRitual) {
                  this.hidanExitRitual();
               }

               this.hidan_stuckSafetyTimer = 0;
            }
         }

         if (this.hidanPhase >= 3) {
            ++this.hidan_regenTimer;
            if (this.hidan_regenTimer >= 40) {
               this.hidan_regenTimer = 0;
               this.heal(this.getMaxHealth() * 0.0015F);
            }
         }

         if (this.hidanPhase == 3) {
            --this.hidan_p3AlternateTimer;
            if (this.hidan_p3AlternateTimer <= 0) {
               if (this.hidan_p3MeleePhase) {
                  this.hidan_p3MeleePhase = false;
                  this.hidan_p3AlternateTimer = 60;
                  if (this.hidan_linkedPlayerUUID != null && !this.hidan_inRitual && this.hidan_ritualCD <= 0) {
                     this.hidanEnterRitual();
                  }
               } else {
                  this.hidan_p3MeleePhase = true;
                  this.hidan_p3AlternateTimer = 100;
                  if (this.hidan_inRitual) {
                     this.hidanExitRitual();
                  }
               }
            }
         }

      }

      private void hidanCheckPhaseTransition(float hpPercent) {
         if (this.hidanPhase == 1 && hpPercent <= 0.65F) {
            this.hidanPhase = 2;
            this.hidanOnPhaseTransition(2);
         }

         if (this.hidanPhase == 2 && hpPercent <= 0.3F) {
            this.hidanPhase = 3;
            this.hidanOnPhaseTransition(3);
         }

      }

      private void hidanOnPhaseTransition(int newPhase) {
         this.hidan_inActiveCombo = false;
         this.hidan_activeComboTarget = null;
         this.hidan_comboHitCount = 0;
         if (this.hidan_inRitual) {
            this.hidanExitRitual();
         }

         if (newPhase == 2 && !this.hidan_phase2Announced) {
            this.hidan_phase2Announced = true;
            this.hidanBroadcast("§4§lHidan: §7\"Jashin-sama... let me show you true pain!\"");

            for(int i = 0; i < 40; ++i) {
               this.world.spawnParticle(EnumParticleTypes.REDSTONE, this.posX + (this.getRNG().nextDouble() - (double)0.5F) * (double)6.0F, this.posY + this.getRNG().nextDouble() * (double)3.0F, this.posZ + (this.getRNG().nextDouble() - (double)0.5F) * (double)6.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

            for(int i = 0; i < 20; ++i) {
               this.world.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + (this.getRNG().nextDouble() - (double)0.5F) * (double)4.0F, this.posY + this.getRNG().nextDouble() * (double)2.0F, this.posZ + (this.getRNG().nextDouble() - (double)0.5F) * (double)4.0F, (double)0.0F, 0.1, (double)0.0F, new int[0]);
            }
         }

         if (newPhase == 3 && !this.hidan_phase3Announced) {
            this.hidan_phase3Announced = true;
            this.hidanBroadcast("§4§lHidan: §7\"I'M IMMORTAL! JASHIN-SAMA PROTECTS ME!\"");
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue() * 1.15);

            for(int i = 0; i < 60; ++i) {
               this.world.spawnParticle(EnumParticleTypes.REDSTONE, this.posX + (this.getRNG().nextDouble() - (double)0.5F) * (double)8.0F, this.posY + this.getRNG().nextDouble() * (double)4.0F, this.posZ + (this.getRNG().nextDouble() - (double)0.5F) * (double)8.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

            for(int i = 0; i < 30; ++i) {
               this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX + (this.getRNG().nextDouble() - (double)0.5F) * (double)5.0F, this.posY + this.getRNG().nextDouble() * (double)3.0F, this.posZ + (this.getRNG().nextDouble() - (double)0.5F) * (double)5.0F, (double)0.0F, 0.05, (double)0.0F, new int[0]);
            }

            this.hidan_p3MeleePhase = true;
            this.hidan_p3AlternateTimer = 100;
         }

      }

      private void hidanHandleMovement(EntityLivingBase t, double d) {
         switch (this.hidanPhase) {
            case 1:
               if (d > (double)5.0F) {
                  this.getNavigator().tryMoveToEntityLiving(t, 1.4);
               } else if (d < (double)2.0F) {
                  double jx = (this.getRNG().nextDouble() - (double)0.5F) * (double)3.0F;
                  double jz = (this.getRNG().nextDouble() - (double)0.5F) * (double)3.0F;
                  this.getNavigator().tryMoveToXYZ(t.posX + jx, t.posY, t.posZ + jz, 1.2);
               } else {
                  this.getNavigator().tryMoveToEntityLiving(t, 1.1);
               }
               break;
            case 2:
               if (d > (double)8.0F) {
                  this.getNavigator().tryMoveToEntityLiving(t, 1.3);
               } else if (d < (double)2.5F) {
                  this.getNavigator().tryMoveToEntityLiving(t, 0.9);
               } else {
                  this.getNavigator().tryMoveToEntityLiving(t, (double)1.0F);
               }
               break;
            case 3:
               if (this.hidan_p3MeleePhase) {
                  this.getNavigator().tryMoveToEntityLiving(t, d > (double)4.0F ? (double)1.5F : 1.2);
               }
         }

      }

      private void hidanPhase1(EntityLivingBase t, double d) {
         if (this.hidan_rushCD <= 0 && d >= (double)6.0F && d <= (double)20.0F) {
            this.hidanRush(t);
         } else if (this.hidan_scytheCD <= 0 && d >= (double)6.0F && d <= (double)25.0F) {
            this.hidanScythe(t);
         } else if (this.hidan_comboCD <= 0 && d <= (double)3.5F) {
            this.hidanStartCombo(t);
         } else {
            if (d <= (double)3.0F && this.hidan_attackTick >= 16) {
               this.hidanMelee(t);
               this.hidan_attackTick = 0;
            }

         }
      }

      private void hidanPhase2(EntityLivingBase t, double d) {
         if (this.hidan_linkedPlayerUUID != null && this.hidan_ritualCD <= 0 && !this.hidan_inRitual) {
            this.hidanEnterRitual();
         } else if (this.hidan_rushCD <= 0 && d >= (double)6.0F && d <= (double)20.0F) {
            this.hidanRush(t);
         } else if (this.hidan_scytheCD <= 0 && d >= (double)6.0F && d <= (double)25.0F) {
            this.hidanScythe(t);
         } else if (this.hidan_comboCD <= 0 && d <= (double)3.5F) {
            this.hidanStartCombo(t);
         } else {
            if (d <= (double)3.0F && this.hidan_attackTick >= 16) {
               this.hidanMelee(t);
               this.hidan_attackTick = 0;
            }

         }
      }

      private void hidanPhase3(EntityLivingBase t, double d) {
         if (this.hidan_p3MeleePhase) {
            if (this.hidan_aoeCD <= 0 && d <= (double)4.0F) {
               this.hidanAoeSwing();
            } else if (this.hidan_rushCD <= 0 && d >= (double)6.0F && d <= (double)20.0F) {
               this.hidanRush(t);
            } else if (this.hidan_scytheCD <= 0 && d >= (double)6.0F && d <= (double)25.0F) {
               this.hidanScythe(t);
            } else if (this.hidan_comboCD <= 0 && d <= (double)3.5F) {
               this.hidanStartCombo(t);
            } else {
               if (d <= (double)3.0F && this.hidan_attackTick >= 14) {
                  this.hidanMelee(t);
                  this.hidan_attackTick = 0;
               }

            }
         }
      }

      private float hidanPhaseDmgMul() {
         switch (this.hidanPhase) {
            case 2:
               return 1.15F;
            case 3:
               return 1.4F;
            default:
               return 1.0F;
         }
      }

      private double hidanDmgMul() {
         return this.getDamageMultiplier() * (double)this.hidanPhaseDmgMul();
      }

      private void hidanMelee(EntityLivingBase t) {
         double m = this.hidanDmgMul();
         float n = (float)((double)9.0F * m);
         float tr = (float)((double)3.0F * m);
         if (t.attackEntityFrom(DamageSource.causeMobDamage(this), n)) {
            t.hurtResistantTime = 0;
            t.attackEntityFrom(DamageSource.MAGIC, tr);
            this.hidanBloodLink(t);
            this.swingArm(EnumHand.MAIN_HAND);
         }

         this.hidan_meleeCD = this.cdMul(20);
      }

      private void hidanStartCombo(EntityLivingBase t) {
         if (this.hidan_comboCD <= 0) {
            int[] c = this.getCooldownRange(this.hidanPhase == 3 ? 20 : (this.hidanPhase == 2 ? 30 : 35), this.hidanPhase == 3 ? 35 : (this.hidanPhase == 2 ? 45 : 55));
            this.hidan_comboCD = c[0] + this.getRNG().nextInt(Math.max(1, c[1] - c[0] + 1));
            this.hidan_comboHitCount = 0;
            this.hidan_inActiveCombo = true;
            this.hidan_activeComboTarget = t;
            this.hidan_comboTimer = 0;
         }
      }

      private void hidanPerformComboHit(EntityLivingBase t) {
         double m = this.hidanDmgMul();
         boolean heavy = this.hidan_comboHitCount >= 3;
         float n = heavy ? (float)((double)15.0F * m) : (float)((double)7.5F * m);
         float tr = heavy ? (float)((double)5.0F * m) : (float)((double)2.5F * m);
         t.hurtResistantTime = 0;
         t.attackEntityFrom(DamageSource.causeMobDamage(this), n);
         t.hurtResistantTime = 0;
         t.attackEntityFrom(DamageSource.MAGIC, tr);
         this.hidanBloodLink(t);
         this.swingArm(EnumHand.MAIN_HAND);
         if (heavy) {
            t.motionX += (t.posX - this.posX) * 0.12;
            t.motionY += 0.4;
            t.motionZ += (t.posZ - this.posZ) * 0.12;
            if (t instanceof EntityPlayerMP) {
               ((EntityPlayerMP)t).velocityChanged = true;
            }

            this.world.playSound((EntityPlayer)null, t.posX, t.posY, t.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.7F);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, t.posX, t.posY + (double)1.0F, t.posZ, 20, 0.6, 0.6, 0.6, (double)0.25F, new int[0]);
            }

            this.hidan_comboHitCount = 0;
            this.hidan_inActiveCombo = false;
            this.hidan_activeComboTarget = null;
         } else {
            this.world.playSound((EntityPlayer)null, t.posX, t.posY, t.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.8F, 0.8F + (float)this.hidan_comboHitCount * 0.12F);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, t.posX, t.posY + (double)1.0F, t.posZ, 5 + this.hidan_comboHitCount * 3, 0.3, 0.3, 0.3, 0.05, new int[0]);
            }

            ++this.hidan_comboHitCount;
            this.hidan_comboTimer = HIDAN_COMBO_DELAYS[Math.min(this.hidan_comboHitCount, HIDAN_COMBO_DELAYS.length - 1)];
         }

      }

      private void hidanScythe(EntityLivingBase t) {
         if (this.hidan_scytheCD <= 0) {
            int[] c = this.getCooldownRange(this.hidanPhase == 3 ? 35 : (this.hidanPhase == 2 ? 50 : 60), this.hidanPhase == 3 ? 55 : (this.hidanPhase == 2 ? 75 : 90));
            this.hidan_scytheCD = c[0] + this.getRNG().nextInt(Math.max(1, c[1] - c[0] + 1));
            double m = this.hidanDmgMul();
            float n = (float)((double)12.0F * m);
            float tr = (float)((double)4.0F * m);
            double dx = t.posX - this.posX;
            double dz = t.posZ - this.posZ;
            double dH = Math.sqrt(dx * dx + dz * dz);
            if (dH <= (double)25.0F) {
               t.hurtResistantTime = 0;
               t.attackEntityFrom(DamageSource.causeMobDamage(this), n);
               t.hurtResistantTime = 0;
               t.attackEntityFrom(DamageSource.MAGIC, tr);
               this.hidanBloodLink(t);
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.REDSTONE, this.posX, this.posY + (double)1.5F, this.posZ, 10, 0.3, 0.3, 0.3, 0.01, new int[0]);
               if (dH > (double)0.0F) {
                  int pts = (int)Math.min(dH, (double)10.0F);

                  for(int i = 1; i <= pts; ++i) {
                     double f = (double)i / (double)pts;
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.REDSTONE, this.posX + dx * f, this.posY + (double)1.5F + (t.posY + (double)1.0F - this.posY - (double)1.5F) * f, this.posZ + dz * f, 2, 0.1, 0.1, 0.1, 0.01, new int[0]);
                  }
               }
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.5F, 0.8F);
            this.swingArm(EnumHand.MAIN_HAND);
         }
      }

      private void hidanAoeSwing() {
         if (this.hidan_aoeCD <= 0) {
            int[] c = this.getCooldownRange(50, 80);
            this.hidan_aoeCD = c[0] + this.getRNG().nextInt(Math.max(1, c[1] - c[0] + 1));
            double m = this.hidanDmgMul();
            float n = (float)((double)11.0F * m);
            float tr = (float)((double)3.5F * m);

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)4.0F))) {
               if (e instanceof EntityPlayer) {
                  EntityLivingBase l = (EntityLivingBase)e;
                  l.hurtResistantTime = 0;
                  l.attackEntityFrom(DamageSource.causeMobDamage(this), n);
                  l.hurtResistantTime = 0;
                  l.attackEntityFrom(DamageSource.MAGIC, tr);
                  if (this.hidan_linkedPlayerUUID == null) {
                     this.hidan_linkedPlayerUUID = e.getUniqueID();
                  }
               }
            }

            this.swingArm(EnumHand.MAIN_HAND);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.posX, this.posY + (double)1.0F, this.posZ, 8, (double)2.0F, (double)0.5F, (double)2.0F, 0.1, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.REDSTONE, this.posX, this.posY + (double)0.5F, this.posZ, 15, 3.2, 0.3, 3.2, 0.01, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.8F, 0.6F);
         }
      }

      private void hidanRush(EntityLivingBase t) {
         if (this.hidan_rushCD <= 0) {
            double rd = (double)this.getDistance(t);
            if (!(rd < (double)6.0F) && !(rd > (double)20.0F)) {
               int[] c = this.getCooldownRange(this.hidanPhase == 3 ? 40 : (this.hidanPhase == 2 ? 55 : 70), this.hidanPhase == 3 ? 60 : (this.hidanPhase == 2 ? 80 : 100));
               this.hidan_rushCD = c[0] + this.getRNG().nextInt(Math.max(1, c[1] - c[0] + 1));
               double dx = t.posX - this.posX;
               double dz = t.posZ - this.posZ;
               double d = Math.sqrt(dx * dx + dz * dz);
               if (!(d <= (double)0.0F)) {
                  double dc = Math.min(d - (double)1.5F, (double)18.0F);
                  if (!(dc <= (double)0.0F)) {
                     double nx = dx / d;
                     double nz = dz / d;
                     double vpt = dc / (double)8.0F;
                     this.hidan_isRushing = true;
                     this.hidan_rushTicksRemaining = 8;
                     this.hidan_rushVelocityX = nx * vpt * 1.3;
                     this.hidan_rushVelocityZ = nz * vpt * 1.3;
                     this.motionY = 0.2;
                     this.velocityChanged = true;
                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 15, 0.6, 0.3, 0.6, 0.1, new int[0]);
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.REDSTONE, this.posX, this.posY + (double)0.5F, this.posZ, 10, 0.4, 0.3, 0.4, 0.01, new int[0]);
                     }

                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.5F, 0.7F);
                  }
               }
            }
         }
      }

      private void hidanProcessRushTick() {
         --this.hidan_rushTicksRemaining;
         this.motionX = this.hidan_rushVelocityX;
         this.motionZ = this.hidan_rushVelocityZ;
         this.motionY = this.hidan_rushTicksRemaining > 4 ? 0.05 : Math.max(-0.3, this.motionY - 0.06);
         this.velocityChanged = true;
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)0.5F, this.posZ, 3, 0.2, 0.2, 0.2, 0.02, new int[0]);
         }

         for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)1.0F))) {
            if (e instanceof EntityPlayer) {
               e.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((double)4.5F * this.hidanDmgMul()));
               if (this.hidan_linkedPlayerUUID == null) {
                  this.hidan_linkedPlayerUUID = e.getUniqueID();
               }
            }
         }

         if (this.hidan_rushTicksRemaining <= 0) {
            this.hidan_isRushing = false;
            this.hidan_rushVelocityX = (double)0.0F;
            this.hidan_rushVelocityZ = (double)0.0F;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + 0.1, this.posZ, 10, (double)0.5F, 0.1, (double)0.5F, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_SMALL_FALL, SoundCategory.HOSTILE, 1.0F, 1.0F);
         }

      }

      private void hidanEnterRitual() {
         if (!this.hidan_inRitual && this.hidan_linkedPlayerUUID != null) {
            EntityPlayer lp = this.hidanGetLinkedPlayer();
            if (lp != null && lp.isEntityAlive()) {
               this.hidan_inRitual = true;
               this.hidan_ritualTimer = 0;
               this.hidan_ritualDamageTick = 0;
               this.setTextureVariant("inftsukaddon:textures/hidan_ritual.png");
               this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)0.0F);
               this.getNavigator().clearPath();
               this.motionX = (double)0.0F;
               this.motionZ = (double)0.0F;
               this.hidanBroadcast("§4§lHidan: §7\"Jashin-sama... accept this offering!\"");
               this.hidanSpawnRitualCircle();
            } else {
               this.hidan_linkedPlayerUUID = null;
            }
         }
      }

      private void hidanExitRitual() {
         if (this.hidan_inRitual) {
            this.hidan_inRitual = false;
            this.hidan_ritualTimer = 0;
            this.setTextureVariant("");
            this.hidan_linkedPlayerUUID = null;
            this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.9);
            int[] c = this.hidanPhase == 3 ? this.getCooldownRange(100, 160) : this.getCooldownRange(200, 300);
            this.hidan_ritualCD = c[0] + this.getRNG().nextInt(Math.max(1, c[1] - c[0] + 1));
         }
      }

      private void hidanTickRitual() {
         if (this.hidan_inRitual) {
            ++this.hidan_ritualTimer;
            ++this.hidan_ritualDamageTick;
            if (this.hidan_ritualTimer >= 200) {
               this.hidanExitRitual();
            } else {
               EntityPlayer lp = this.hidanGetLinkedPlayer();
               if (lp != null && lp.isEntityAlive()) {
                  if (this.hidan_ritualDamageTick >= 20) {
                     this.hidan_ritualDamageTick = 0;
                     this.attackEntityFrom(DamageSource.GENERIC, 4.0F);
                     lp.hurtResistantTime = 0;
                     lp.attackEntityFrom(DamageSource.MAGIC, 4.0F);
                     this.world.playSound((EntityPlayer)null, lp.posX, lp.posY, lp.posZ, SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.HOSTILE, 1.0F, 0.5F);
                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.REDSTONE, lp.posX, lp.posY + (double)1.0F, lp.posZ, 15, 0.4, 0.8, 0.4, 0.01, new int[0]);
                     }
                  }

                  this.motionX = (double)0.0F;
                  this.motionZ = (double)0.0F;
                  this.getNavigator().clearPath();
                  if (this.hidan_ritualTimer % 5 == 0) {
                     this.hidanSpawnRitualCircle();
                  }

               } else {
                  this.hidan_linkedPlayerUUID = null;
                  this.hidanExitRitual();
               }
            }
         }
      }

      private void hidanSpawnRitualCircle() {
         double r = (double)5.0F;

         for(int i = 0; i < 24; ++i) {
            double a = 0.2617993877991494 * (double)i;
            this.world.spawnParticle(EnumParticleTypes.REDSTONE, this.posX + Math.cos(a) * r, this.posY + 0.1, this.posZ + Math.sin(a) * r, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
         }

         for(int i = 0; i < 3; ++i) {
            double a1 = 2.0943951023931953 * (double)i - (Math.PI / 2D);
            double a2 = 2.0943951023931953 * (double)(i + 1) - (Math.PI / 2D);
            double x1 = this.posX + Math.cos(a1) * (r - (double)1.0F);
            double z1 = this.posZ + Math.sin(a1) * (r - (double)1.0F);
            double x2 = this.posX + Math.cos(a2) * (r - (double)1.0F);
            double z2 = this.posZ + Math.sin(a2) * (r - (double)1.0F);

            for(int j = 0; j <= 8; ++j) {
               double f = (double)j / (double)8.0F;
               this.world.spawnParticle(EnumParticleTypes.REDSTONE, x1 + (x2 - x1) * f, this.posY + 0.1, z1 + (z2 - z1) * f, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }
         }

         for(int i = 0; i < 5; ++i) {
            this.world.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + (this.getRNG().nextDouble() - (double)0.5F) * (double)1.5F, this.posY + (double)0.5F + this.getRNG().nextDouble(), this.posZ + (this.getRNG().nextDouble() - (double)0.5F) * (double)1.5F, (double)0.0F, 0.05, (double)0.0F, new int[0]);
         }

      }

      private void hidanBloodLink(EntityLivingBase t) {
         if (t instanceof EntityPlayer && this.hidan_linkedPlayerUUID == null) {
            this.hidan_linkedPlayerUUID = t.getUniqueID();
            this.hidanBroadcast("§4§lHidan: §7\"I've tasted your blood! You're mine now!\"");
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.REDSTONE, t.posX, t.posY + (double)1.0F, t.posZ, 25, (double)0.5F, 0.8, (double)0.5F, 0.01, new int[0]);
            }
         }

      }

      private EntityPlayer hidanGetLinkedPlayer() {
         if (this.hidan_linkedPlayerUUID == null) {
            return null;
         } else {
            for(Entity e : this.world.loadedEntityList) {
               if (e instanceof EntityPlayer && e.getUniqueID().equals(this.hidan_linkedPlayerUUID)) {
                  return (EntityPlayer)e;
               }
            }

            return null;
         }
      }

      private void hidanBroadcast(String msg) {
         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F))) {
            p.sendMessage(new TextComponentString(msg));
         }

      }

      static {
         TEXTURE_VARIANT = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
         HIDAN_COMBO_DELAYS = new int[]{0, 7, 9, 12};
      }
   }
}
