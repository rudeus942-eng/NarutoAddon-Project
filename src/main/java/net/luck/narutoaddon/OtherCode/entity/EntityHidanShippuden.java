
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Particles;
import net.narutomod.Particles.Types;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityHidanShippuden extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 275;

   public EntityHidanShippuden(ElementsInfTsukAddon instance) {
      super(instance, 950);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "hidanshippuden"), 275).name("hidanshippuden").tracker(64, 1, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, HidanShippudenRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class HidanShippudenRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/hidan.png");

      public HidanShippudenRenderer(RenderManager renderManager) {
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
         int phase = entity.getPhaseForRender();
         if (phase >= 2) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            float redIntensity = phase >= 3 ? 0.4F : 0.2F;
            float r = 1.0F;
            float g = 1.0F - redIntensity * 0.7F;
            float b = 1.0F - redIntensity * 0.8F;
            float a = alpha < 1.0F ? alpha : 1.0F;
            GlStateManager.color(r, g, b, a);
            if (alpha < 1.0F) {
               GlStateManager.depthMask(false);
            }

            if (this.bindEntityTexture(entity)) {
               this.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            }

            if (alpha < 1.0F) {
               GlStateManager.depthMask(true);
            }

            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         } else if (alpha < 1.0F) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
            GlStateManager.color(1.0F, 0.85F, 0.85F, alpha);
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
      private int phase = 1;
      private int attackTick = 0;
      private boolean introPlayed = false;
      private int meleeCD = 0;
      private int scytheThrowCD = 0;
      private int ritualCircleCD = 0;
      private int aoeSlashCD = 0;
      private int antiYCheeseCD = 0;
      private int jashinCurseCD = 0;
      private int selfHarmAoeCD = 0;
      private boolean hasResurrected = false;
      private int resurrectionInvulnTicks = 0;
      private boolean phase2Announced = false;
      private boolean phase3Announced = false;
      private int comboStep = 0;
      private int comboDelay = 0;
      private EntityLivingBase comboTarget = null;
      private static final int[] COMBO_DELAYS = new int[]{0, 5, 5, 8};

      public EntityCustom(World world) {
         super(world);
      }

      public int getPhaseForRender() {
         return this.phase;
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (target != null && target.isEntityAlive()) {
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            if (!this.introPlayed) {
               this.introPlayed = true;
               this.hidanSay("§dHeh... Another sacrifice for Lord Jashin!");
            }

            double yDiff = target.posY - this.posY;
            if (yDiff > (double)3.0F && !this.isDashing && this.antiYCheeseCD <= 0) {
               if (yDiff > (double)30.0F) {
                  double safeY = this.findSafeY(target.posX, target.posY, target.posZ);
                  this.setPositionAndUpdate(target.posX, safeY, target.posZ);
                  this.motionX = (double)0.0F;
                  this.motionY = (double)0.0F;
                  this.motionZ = (double)0.0F;
                  this.velocityChanged = true;
                  this.antiYCheeseCD = 40;
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
                  this.antiYCheeseCD = 50;
                  return;
               }
            }

            ++this.attackTick;
            this.updatePhase();
            if (this.comboStep > 0 && this.comboTarget != null) {
               this.processCombo();
            } else {
               double dmgMul = this.getDamageMultiplier();
               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               float cdMul = this.phaseCdMul();
               if (this.phase >= 3 && !this.world.isRemote && this.ticksExisted % 5 == 0) {
                  this.spawnRitualGroundParticles();
               }

               int aoeInterval = this.phase >= 3 ? 25 : 40;
               if (this.phase >= 2 && this.aoeSlashCD <= 0 && !this.world.isRemote) {
                  int[] cd = this.getCooldownRange((int)((float)aoeInterval * cdMul), (int)((float)(aoeInterval + 15) * cdMul));
                  this.aoeSlashCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.performAoeSlash(baseDmg, dmgMul);
               }

               int ritualInterval = this.phase >= 2 ? 40 : 80;
               if (this.ritualCircleCD <= 0 && !this.world.isRemote) {
                  int[] cd = this.getCooldownRange((int)((float)ritualInterval * cdMul), (int)((float)(ritualInterval + 20) * cdMul));
                  this.ritualCircleCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.spawnRitualCircle();
               }

               if (this.hasResurrected && this.jashinCurseCD <= 0 && dist <= (double)6.0F && !this.world.isRemote) {
                  this.jashinCurseCD = 120;
                  this.performJashinCurse(target, baseDmg, dmgMul);
               }

               if (this.hasResurrected && this.selfHarmAoeCD <= 0 && dist <= (double)10.0F && !this.world.isRemote && this.rand.nextFloat() < 0.35F) {
                  this.selfHarmAoeCD = 100;
                  this.performSelfHarmAoe(target, baseDmg, dmgMul);
               } else {
                  double throwRange = this.phase >= 2 ? (double)8.0F : (double)10.0F;
                  int throwInterval = this.phase >= 2 ? 40 : 60;
                  if (this.scytheThrowCD <= 0 && dist >= throwRange && !this.world.isRemote) {
                     int[] cd = this.getCooldownRange((int)((float)throwInterval * cdMul), (int)((float)(throwInterval + 20) * cdMul));
                     this.scytheThrowCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                     this.performScytheThrow(target, baseDmg, dmgMul);
                  } else if (this.meleeCD <= 0 && dist <= (double)4.0F && this.rand.nextFloat() < 0.28F) {
                     int[] cd = this.getCooldownRange((int)(35.0F * cdMul), (int)(50.0F * cdMul));
                     this.meleeCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                     this.startCombo(target);
                  } else {
                     if (this.meleeCD <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)4.0F) {
                        this.performScytheMelee(target, baseDmg, dmgMul);
                     }

                  }
               }
            }
         }
      }

      private void updatePhase() {
         if (!this.world.isRemote) {
            float healthPercent = this.getHealth() / this.getMaxHealth();
            if (this.phase == 1 && healthPercent < 0.6F) {
               this.phase = 2;
               if (!this.phase2Announced) {
                  this.phase2Announced = true;
                  this.hidanSay("§dYes... YES! The pain is exquisite! Lord Jashin demands MORE blood!");
                  Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.5F, this.posZ, 40, (double)2.0F, (double)2.0F, (double)2.0F, (double)0.0F, 0.1, (double)0.0F, new int[]{-3407668, 35});
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 25, (double)1.5F, (double)1.5F, (double)1.5F, (double)0.0F, 0.05, (double)0.0F, new int[]{-869072896, 25});
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.5F, 0.6F);
               }
            } else if (this.phase == 2 && healthPercent < 0.3F) {
               this.phase = 3;
               if (!this.phase3Announced) {
                  this.phase3Announced = true;
                  this.hidanSay("§4§lLord Jashin... grant me your unholy power!");
                  Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.5F, this.posZ, 80, (double)3.0F, (double)3.0F, (double)3.0F, (double)0.0F, 0.15, (double)0.0F, new int[]{-5636096, 40});
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)0.5F, this.posZ, 40, (double)2.0F, (double)2.0F, (double)2.0F, (double)0.0F, 0.08, (double)0.0F, new int[]{-870186991, 30});
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 2.0F, 0.5F);
               }
            }

         }
      }

      private float phaseCdMul() {
         if (this.phase >= 3) {
            return 0.5F;
         } else {
            return this.phase >= 2 ? 0.7F : 1.0F;
         }
      }

      private void performScytheMelee(EntityLivingBase target, float baseDmg, double dmgMul) {
         float normalDmg;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            normalDmg = baseDmg * (1.0F - this.trueDamageSplit) * (float)dmgMul;
            trueDmg = baseDmg * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
         } else {
            normalDmg = baseDmg * (float)dmgMul;
            trueDmg = 8.0F * this.trueDamageMultiplier;
         }

         target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         target.addPotionEffect(new PotionEffect(MobEffects.WITHER, 40, 0));
         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.2F, 0.8F);
         int[] cd = this.getCooldownRange((int)(20.0F * this.phaseCdMul()), (int)(30.0F * this.phaseCdMul()));
         this.meleeCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
      }

      private void performScytheThrow(EntityLivingBase target, float baseDmg, double dmgMul) {
         float normalDmg = baseDmg * 0.6F * (float)dmgMul;
         float trueDmg = baseDmg * 0.2F;
         EntityHidanScytheProjectile.EntityCustom scythe = new EntityHidanScytheProjectile.EntityCustom(this.world, this, normalDmg, trueDmg);
         double dx = target.posX - this.posX;
         double dy = target.posY + (double)target.getEyeHeight() - 0.1 - (this.posY + (double)this.getEyeHeight() - 0.1);
         double dz = target.posZ - this.posZ;
         double d = Math.sqrt(dx * dx + dz * dz);
         scythe.shoot(dx, dy + d * 0.1, dz, this.kunaiSpeed, this.kunaiInaccuracy);
         this.world.spawnEntity(scythe);
         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1.0F, 0.8F);
         this.hidanSay("§dYou can't run from Jashin!");
      }

      private void performAoeSlash(float baseDmg, double dmgMul) {
         float normalDmg = baseDmg * 0.5F * (float)dmgMul;
         float trueDmg = baseDmg * 0.15F;

         for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)5.0F))) {
            if (e != this && !(e instanceof QuestNpcBase)) {
               double eDist = (double)this.getDistance(e);
               float falloff = (float)Math.max(0.3, (double)1.0F - eDist / (double)6.0F);
               e.hurtResistantTime = 0;
               e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
               e.hurtResistantTime = 0;
               e.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
               if (e instanceof EntityLivingBase) {
                  e.addPotionEffect(new PotionEffect(MobEffects.WITHER, 40, 0));
               }
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.7F);
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)3.0F, 0.3, (double)3.0F, 0.1, 0.05, 0.1, new int[]{-5636096, 20});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)0.5F, this.posZ, 10, (double)2.5F, 0.2, (double)2.5F, 0.05, 0.02, 0.05, new int[]{-869072896, 15});
         this.swingArm(EnumHand.MAIN_HAND);
      }

      private void spawnRitualCircle() {
         int particleCount = this.phase >= 2 ? 20 : 12;
         double radius = this.phase >= 2 ? (double)3.0F : (double)2.0F;

         for(int i = 0; i < particleCount; ++i) {
            double angle = (Math.PI * 2D) / (double)particleCount * (double)i;
            double px = this.posX + Math.cos(angle) * radius;
            double pz = this.posZ + Math.sin(angle) * radius;
            Particles.spawnParticle(this.world, Types.SMOKE, px, this.posY + 0.1, pz, 2, 0.1, 0.05, 0.1, (double)0.0F, 0.02, (double)0.0F, new int[]{-869072896, 18});
         }

         if (this.phase >= 2) {
            for(int i = 0; i < 10; ++i) {
               double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double r = this.rand.nextDouble() * radius;
               double px = this.posX + Math.cos(angle) * r;
               double pz = this.posZ + Math.sin(angle) * r;
               Particles.spawnParticle(this.world, Types.FLAME, px, this.posY + 0.1, pz, 1, 0.05, 0.02, 0.05, (double)0.0F, 0.08, (double)0.0F, new int[]{-5636096, 15});
            }
         }

         SoundEvent elecSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
         if (elecSound != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, elecSound, SoundCategory.HOSTILE, 0.3F, 0.4F);
         }

      }

      private void spawnRitualGroundParticles() {
         for(int i = 0; i < 6; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double r = this.rand.nextDouble() * (double)2.5F;
            double px = this.posX + Math.cos(angle) * r;
            double pz = this.posZ + Math.sin(angle) * r;
            Particles.spawnParticle(this.world, Types.FLAME, px, this.posY + 0.05, pz, 1, 0.05, 0.01, 0.05, (double)0.0F, 0.04, (double)0.0F, new int[]{-5636096, 12});
         }

      }

      private void performJashinCurse(EntityLivingBase target, float baseDmg, double dmgMul) {
         this.hidanSay("§4§lJashin Curse... §rYour blood is MINE!");
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double len = Math.sqrt(dx * dx + dz * dz);
         if (len > (double)1.0F) {
            this.motionX = dx / len * 0.8;
            this.motionY = 0.2;
            this.motionZ = dz / len * 0.8;
            this.velocityChanged = true;
         }

         float normalDmg = baseDmg * 0.8F * (float)dmgMul;
         float trueDmg = baseDmg * this.trueDamageSplit * 1.5F * (float)dmgMul * this.trueDamageMultiplier;
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         target.addPotionEffect(new PotionEffect(MobEffects.WITHER, 80, 1));
         this.swingArm(EnumHand.MAIN_HAND);
         Particles.spawnParticle(this.world, Types.FLAME, target.posX, target.posY + (double)1.0F, target.posZ, 30, (double)1.0F, (double)1.5F, (double)1.0F, (double)0.0F, 0.1, (double)0.0F, new int[]{-10092442, 30});
         Particles.spawnParticle(this.world, Types.SMOKE, target.posX, target.posY + (double)0.5F, target.posZ, 15, 0.8, 0.8, 0.8, 0.02, 0.05, 0.02, new int[]{-870186974, 25});

         for(int i = 0; i < 12; ++i) {
            double angle = (Math.PI / 6D) * (double)i;
            double px = target.posX + Math.cos(angle) * (double)2.0F;
            double pz = target.posZ + Math.sin(angle) * (double)2.0F;
            Particles.spawnParticle(this.world, Types.SMOKE, px, target.posY + 0.1, pz, 2, 0.1, 0.05, 0.1, (double)0.0F, 0.02, (double)0.0F, new int[]{-867958716, 20});
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.5F, 0.5F);
      }

      private void performSelfHarmAoe(EntityLivingBase target, float baseDmg, double dmgMul) {
         this.hidanSay("§4§lLord Jashin... accept this offering!");
         float selfDmg = this.getMaxHealth() * 0.05F;
         this.setHealth(Math.max(1.0F, this.getHealth() - selfDmg));
         float normalDmg = baseDmg * 1.3F * (float)dmgMul;
         float trueDmg = baseDmg * this.trueDamageSplit * 1.2F * (float)dmgMul * this.trueDamageMultiplier;

         for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)8.0F))) {
            if (e != this && !(e instanceof QuestNpcBase)) {
               double eDist = (double)this.getDistance(e);
               float falloff = (float)Math.max(0.4, (double)1.0F - eDist / (double)9.0F);
               e.hurtResistantTime = 0;
               e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
               e.hurtResistantTime = 0;
               e.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
               e.addPotionEffect(new PotionEffect(MobEffects.WITHER, 60, 1));
            }
         }

         this.swingArm(EnumHand.MAIN_HAND);
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 60, (double)4.0F, (double)2.0F, (double)4.0F, 0.1, 0.2, 0.1, new int[]{-3407872, 35});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)0.5F, this.posZ, 30, (double)3.0F, (double)1.5F, (double)3.0F, 0.05, 0.1, 0.05, new int[]{-867958784, 30});

         for(int i = 0; i < 20; ++i) {
            double angle = (Math.PI / 10D) * (double)i;
            double px = this.posX + Math.cos(angle) * (double)5.0F;
            double pz = this.posZ + Math.sin(angle) * (double)5.0F;
            Particles.spawnParticle(this.world, Types.FLAME, px, this.posY + 0.2, pz, 3, 0.2, 0.1, 0.2, Math.cos(angle) * 0.05, 0.03, Math.sin(angle) * 0.05, new int[]{-5636096, 25});
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_HURT, SoundCategory.HOSTILE, 2.0F, 0.4F);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0F, 0.6F);
      }

      private void startCombo(EntityLivingBase target) {
         this.comboStep = 1;
         this.comboDelay = COMBO_DELAYS[0];
         this.comboTarget = target;
         this.executeComboHit(target, 1);
      }

      private void processCombo() {
         if (this.comboTarget != null && this.comboTarget.isEntityAlive() && !((double)this.getDistance(this.comboTarget) > (double)6.0F)) {
            this.getLookHelper().setLookPositionWithEntity(this.comboTarget, 30.0F, 30.0F);
            --this.comboDelay;
            if (this.comboDelay <= 0) {
               ++this.comboStep;
               if (this.comboStep > 4) {
                  this.comboStep = 0;
                  this.comboDelay = 0;
                  this.comboTarget = null;
                  return;
               }

               int delay = COMBO_DELAYS[this.comboStep - 1];
               if (this.phase >= 3) {
                  delay = Math.max(2, delay / 2);
               }

               this.comboDelay = delay;
               this.executeComboHit(this.comboTarget, this.comboStep);
            }

         } else {
            this.comboStep = 0;
            this.comboDelay = 0;
            this.comboTarget = null;
         }
      }

      private void executeComboHit(EntityLivingBase target, int step) {
         if (!this.world.isRemote) {
            float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            float stepMul = 0.7F + (float)step * 0.1F + (step == 4 ? 0.1F : 0.0F);
            float normalDmg = baseDmg * stepMul * (float)dmgMul;
            float trueDmg = 4.0F + (float)step * 1.5F;
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            target.addPotionEffect(new PotionEffect(MobEffects.WITHER, 40, 0));
            if (step == 4) {
               double dx = target.posX - this.posX;
               double dz = target.posZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > 0.1) {
                  target.motionX += dx / len * 1.2;
                  target.motionY += 0.3;
                  target.motionZ += dz / len * 1.2;
                  target.velocityChanged = true;
               }

               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.7F);
            } else {
               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 0.9F + (float)step * 0.05F);
            }

            this.swingArm(step % 2 == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
            Particles.spawnParticle(this.world, Types.FLAME, target.posX, target.posY + (double)1.0F, target.posZ, 5, 0.3, 0.3, 0.3, (double)0.0F, 0.02, (double)0.0F, new int[]{-5636096, 15});
         }
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         if (this.world.isRemote) {
            return amount;
         } else if (this.resurrectionInvulnTicks > 0) {
            return -1.0F;
         } else if (!this.hasResurrected && this.getHealth() - amount <= 0.0F) {
            this.performResurrection();
            return -1.0F;
         } else {
            this.checkPhaseTransition();
            return amount;
         }
      }

      private void performResurrection() {
         this.hasResurrected = true;
         this.resurrectionInvulnTicks = 10;
         this.setHealth(this.getMaxHealth() * 0.5F);
         this.setTextureOverride("inftsukaddon:textures/hidan_ritual.png");
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 2.0F, 0.3F);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.HOSTILE, 1.5F, 0.5F);
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 80, (double)3.0F, (double)3.0F, (double)3.0F, 0.05, 0.2, 0.05, new int[]{-5636045, 45});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)0.5F, this.posZ, 50, (double)2.5F, (double)2.5F, (double)2.5F, 0.03, 0.1, 0.03, new int[]{-870186991, 35});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)2.0F, this.posZ, 30, (double)1.5F, (double)2.0F, (double)1.5F, (double)0.0F, 0.15, (double)0.0F, new int[]{-3407668, 30});

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)40.0F))) {
            p.sendMessage(new TextComponentString("§5§lHidan: §r§dYou think you can kill me?! Lord Jashin grants me eternal life!"));
         }

         this.phase = 2;
         this.phase2Announced = true;
         this.phase3Announced = false;
      }

      private void checkPhaseTransition() {
      }

      protected void tickStyleCooldowns() {
         if (this.meleeCD > 0) {
            --this.meleeCD;
         }

         if (this.scytheThrowCD > 0) {
            --this.scytheThrowCD;
         }

         if (this.ritualCircleCD > 0) {
            --this.ritualCircleCD;
         }

         if (this.aoeSlashCD > 0) {
            --this.aoeSlashCD;
         }

         if (this.antiYCheeseCD > 0) {
            --this.antiYCheeseCD;
         }

         if (this.jashinCurseCD > 0) {
            --this.jashinCurseCD;
         }

         if (this.selfHarmAoeCD > 0) {
            --this.selfHarmAoeCD;
         }

         if (this.resurrectionInvulnTicks > 0) {
            --this.resurrectionInvulnTicks;
         }

         ++this.attackTick;
         if (this.comboStep > 0 && this.comboTarget != null && this.comboDelay > 0) {
            --this.comboDelay;
         }

      }

      protected void resetCombatState() {
         this.phase = 1;
         this.attackTick = 0;
         this.introPlayed = false;
         this.setTextureOverride("");
         this.meleeCD = 0;
         this.scytheThrowCD = 0;
         this.ritualCircleCD = 0;
         this.aoeSlashCD = 0;
         this.antiYCheeseCD = 0;
         this.jashinCurseCD = 0;
         this.selfHarmAoeCD = 0;
         this.hasResurrected = false;
         this.resurrectionInvulnTicks = 0;
         this.phase2Announced = false;
         this.phase3Announced = false;
         this.comboStep = 0;
         this.comboDelay = 0;
         this.comboTarget = null;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("hidanPhase", this.phase);
         compound.setInteger("hidanAttackTick", this.attackTick);
         compound.setBoolean("hidanIntroPlayed", this.introPlayed);
         compound.setInteger("hidanMeleeCD", this.meleeCD);
         compound.setInteger("hidanScytheThrowCD", this.scytheThrowCD);
         compound.setInteger("hidanRitualCircleCD", this.ritualCircleCD);
         compound.setInteger("hidanAoeSlashCD", this.aoeSlashCD);
         compound.setInteger("hidanAntiYCheeseCD", this.antiYCheeseCD);
         compound.setInteger("hidanJashinCurseCD", this.jashinCurseCD);
         compound.setInteger("hidanSelfHarmAoeCD", this.selfHarmAoeCD);
         compound.setBoolean("hidanHasResurrected", this.hasResurrected);
         compound.setInteger("hidanResInvuln", this.resurrectionInvulnTicks);
         compound.setBoolean("hidanPhase2Announced", this.phase2Announced);
         compound.setBoolean("hidanPhase3Announced", this.phase3Announced);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.phase = compound.hasKey("hidanPhase") ? compound.getInteger("hidanPhase") : 1;
         this.attackTick = compound.hasKey("hidanAttackTick") ? compound.getInteger("hidanAttackTick") : 0;
         this.introPlayed = compound.getBoolean("hidanIntroPlayed");
         this.meleeCD = compound.hasKey("hidanMeleeCD") ? compound.getInteger("hidanMeleeCD") : 0;
         this.scytheThrowCD = compound.hasKey("hidanScytheThrowCD") ? compound.getInteger("hidanScytheThrowCD") : 0;
         this.ritualCircleCD = compound.hasKey("hidanRitualCircleCD") ? compound.getInteger("hidanRitualCircleCD") : 0;
         this.aoeSlashCD = compound.hasKey("hidanAoeSlashCD") ? compound.getInteger("hidanAoeSlashCD") : 0;
         this.antiYCheeseCD = compound.hasKey("hidanAntiYCheeseCD") ? compound.getInteger("hidanAntiYCheeseCD") : 0;
         this.jashinCurseCD = compound.hasKey("hidanJashinCurseCD") ? compound.getInteger("hidanJashinCurseCD") : 0;
         this.selfHarmAoeCD = compound.hasKey("hidanSelfHarmAoeCD") ? compound.getInteger("hidanSelfHarmAoeCD") : 0;
         this.hasResurrected = compound.getBoolean("hidanHasResurrected");
         this.resurrectionInvulnTicks = compound.hasKey("hidanResInvuln") ? compound.getInteger("hidanResInvuln") : 0;
         this.phase2Announced = compound.getBoolean("hidanPhase2Announced");
         this.phase3Announced = compound.getBoolean("hidanPhase3Announced");
      }

      protected void onCombatDeath() {
         this.hidanSay("§7No... Lord Jashin... forgive me...");
      }

      protected boolean usesVanillaMeleeAI() {
         return true;
      }

      private void hidanSay(String msg) {
         if (this.chatCooldown <= 0) {
            String name = this.getCustomNameTag();
            if (name == null || name.isEmpty()) {
               name = "Hidan";
            }

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)40.0F))) {
               p.sendMessage(new TextComponentString("§5" + name + ": " + msg));
            }

            this.chatCooldown = 80;
         }
      }
   }
}
