
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Particles;
import net.narutomod.Particles.Types;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityWorldBossZabuza extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 306;

   public EntityWorldBossZabuza(ElementsInfTsukAddon instance) {
      super(instance, 962);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "worldbosszabuza"), 306).name("worldbosszabuza").tracker(64, 1, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, WorldBossZabuzaRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class WorldBossZabuzaRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/zabuza.png");

      public WorldBossZabuzaRenderer(RenderManager renderManager) {
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
            float mistIntensity = phase >= 3 ? 0.3F : 0.15F;
            float r = 1.0F - mistIntensity * 0.5F;
            float g = 1.0F - mistIntensity * 0.3F;
            float b = 1.0F;
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
            GlStateManager.color(0.85F, 0.9F, 1.0F, alpha);
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
      private int cleaveCD = 0;
      private int waterDragonCD = 0;
      private int silentKillingCD = 0;
      private int waterPrisonCD = 0;
      private int antiYCheeseCD = 0;
      private boolean phase2Announced = false;
      private boolean phase3Announced = false;
      private int comboStep = 0;
      private int comboDelay = 0;
      private EntityLivingBase comboTarget = null;
      private static final int[] COMBO_DELAYS = new int[]{0, 6, 10};
      private int mistTickCounter = 0;

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
               this.zabuzaSay("§7§lYou're in my domain now. None of you will leave alive.");
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
               ++this.mistTickCounter;
               int mistInterval = this.getMistInterval();
               if (this.mistTickCounter >= mistInterval && !this.world.isRemote) {
                  this.mistTickCounter = 0;
                  this.performHiddenMist();
               }

               if (this.silentKillingCD <= 0 && target.isPotionActive(MobEffects.BLINDNESS) && dist >= (double)3.0F && dist <= (double)20.0F && !this.world.isRemote) {
                  float silentChance = this.phase >= 3 ? 0.6F : (this.phase >= 2 ? 0.4F : 0.2F);
                  if (this.rand.nextFloat() < silentChance) {
                     int[] cd = this.getCooldownRange((int)(40.0F * cdMul), (int)(60.0F * cdMul));
                     this.silentKillingCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                     this.performSilentKilling(target, baseDmg, dmgMul);
                     return;
                  }
               }

               if (this.phase >= 2 && this.waterPrisonCD <= 0 && dist >= (double)6.0F && dist <= (double)12.0F && !this.world.isRemote && this.rand.nextFloat() < 0.3F) {
                  this.waterPrisonCD = 150;
                  this.performWaterPrison(target, baseDmg, dmgMul);
               } else if (this.waterDragonCD <= 0 && dist >= (double)15.0F && dist <= (double)30.0F && !this.world.isRemote) {
                  this.waterDragonCD = 200;
                  this.performWaterDragon(target, baseDmg, dmgMul);
               } else if (this.meleeCD <= 0 && dist <= (double)4.0F && this.rand.nextFloat() < 0.25F && !this.world.isRemote) {
                  int[] cd = this.getCooldownRange((int)(30.0F * cdMul), (int)(45.0F * cdMul));
                  this.meleeCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.startCombo(target);
               } else if (this.cleaveCD <= 0 && dist <= (double)5.0F && !this.world.isRemote) {
                  int[] cd = this.getCooldownRange((int)(15.0F * cdMul), (int)(25.0F * cdMul));
                  this.cleaveCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.performCleave(target, baseDmg, dmgMul);
               } else {
                  if (dist > (double)5.0F && dist <= (double)20.0F && !this.isDashing && this.meleeCD <= 0 && !this.world.isRemote) {
                     double gdx = target.posX - this.posX;
                     double gdz = target.posZ - this.posZ;
                     double glen = Math.sqrt(gdx * gdx + gdz * gdz);
                     if (glen > (double)0.5F) {
                        this.isDashing = true;
                        this.dashType = 1;
                        this.dashDuration = 8;
                        this.dashTicksRemaining = 8;
                        this.dashVelX = gdx / glen * 1.2;
                        this.dashVelZ = gdz / glen * 1.2;
                        this.motionY = 0.15;
                        this.velocityChanged = true;
                        this.meleeCD = 15;
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
                  this.zabuzaSay("§7§lThe mist thickens... You should have run while you had the chance.");
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 50, (double)4.0F, (double)2.0F, (double)4.0F, 0.05, 0.1, 0.05, new int[]{-863461960, 40});
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.5F, 0.7F);
               }
            } else if (this.phase == 2 && healthPercent < 0.3F) {
               this.phase = 3;
               if (!this.phase3Announced) {
                  this.phase3Announced = true;
                  this.zabuzaSay("§8§lNow you die. Silent Killing... the art of the Demon.");
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 80, (double)6.0F, (double)3.0F, (double)6.0F, 0.08, 0.15, 0.08, new int[]{-865699960, 50});
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

      private int getMistInterval() {
         if (this.phase >= 3) {
            return 30;
         } else {
            return this.phase >= 2 ? 50 : 80;
         }
      }

      private void performCleave(EntityLivingBase target, float baseDmg, double dmgMul) {
         float normalDmg;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            normalDmg = baseDmg * 1.2F * (1.0F - this.trueDamageSplit) * (float)dmgMul;
            trueDmg = baseDmg * 1.2F * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
         } else {
            normalDmg = baseDmg * 1.2F * (float)dmgMul;
            trueDmg = 10.0F * this.trueDamageMultiplier;
         }

         double lookX = -Math.sin(Math.toRadians((double)this.rotationYaw));
         double lookZ = Math.cos(Math.toRadians((double)this.rotationYaw));

         for(EntityLivingBase ent : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)4.0F), (e) -> e != this && e.isEntityAlive())) {
            double ex = ent.posX - this.posX;
            double ez = ent.posZ - this.posZ;
            double eDist = Math.sqrt(ex * ex + ez * ez);
            if (!(eDist > (double)4.0F) && !(eDist < 0.1)) {
               double dot = (ex * lookX + ez * lookZ) / eDist;
               if (!(dot < (double)-0.25F)) {
                  ent.hurtResistantTime = 0;
                  ent.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                  ent.hurtResistantTime = 0;
                  ent.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  ent.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 20, 0));
               }
            }
         }

         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.6F);
         Particles.spawnParticle(this.world, Types.SMOKE, target.posX, target.posY + (double)1.0F, target.posZ, 10, (double)1.0F, (double)0.5F, (double)1.0F, 0.05, 0.02, 0.05, new int[]{-864581547, 15});
      }

      private void performWaterDragon(EntityLivingBase target, float baseDmg, double dmgMul) {
         float normalDmg = baseDmg * 1.5F * (float)dmgMul;
         float trueDmg = baseDmg * 0.5F * (float)dmgMul * this.trueDamageMultiplier;
         EntityWaterDragonJutsu.EntityCustom dragon = new EntityWaterDragonJutsu.EntityCustom(this.world, this, normalDmg, trueDmg);
         double dx = target.posX - this.posX;
         double dy = target.posY + (double)target.height * 0.3 - (this.posY + (double)this.getEyeHeight());
         double dz = target.posZ - this.posZ;
         dragon.shoot(dx, dy, dz, 1.0F, 1.5F);
         this.world.spawnEntity(dragon);
         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 2.0F, 0.6F);
         this.zabuzaSay("§9Water Style: Water Dragon Jutsu!");
      }

      private void performHiddenMist() {
         for(int i = 0; i < 40; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double r = this.rand.nextDouble() * (double)10.0F;
            double px = this.posX + Math.cos(angle) * r;
            double py = this.posY + this.rand.nextDouble() * (double)3.0F;
            double pz = this.posZ + Math.sin(angle) * r;
            Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 3, 0.8, (double)0.5F, 0.8, 0.01, 0.02, 0.01, new int[]{-863461960, 30});
         }

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)10.0F))) {
            p.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 60, 0));
         }

         if (this.rand.nextFloat() < 0.15F) {
            this.zabuzaSay("§8Hidden Mist Technique...");
         }

      }

      private void performSilentKilling(EntityLivingBase target, float baseDmg, double dmgMul) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double len = Math.sqrt(dx * dx + dz * dz);
         if (len > (double)1.0F) {
            double behindX = target.posX - dx / len * (double)2.0F;
            double behindZ = target.posZ - dz / len * (double)2.0F;
            double safeY = this.findSafeY(behindX, target.posY, behindZ);
            this.setPositionAndUpdate(behindX, safeY, behindZ);
            this.motionX = (double)0.0F;
            this.motionY = (double)0.0F;
            this.motionZ = (double)0.0F;
            this.velocityChanged = true;
         }

         float trueDmg;
         float normalDmg;
         if (this.trueDamageSplit > 0.0F) {
            normalDmg = baseDmg * 1.5F * (1.0F - this.trueDamageSplit) * (float)dmgMul;
            trueDmg = baseDmg * 1.5F * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
         } else {
            normalDmg = baseDmg * 1.5F * (float)dmgMul;
            trueDmg = 12.0F * this.trueDamageMultiplier;
         }

         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)target.height * (double)0.5F, target.posZ, 15, 0.4, 0.4, 0.4, 0.1, new int[0]);
         }

         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)1.0F, (double)1.0F, (double)1.0F, 0.05, 0.05, 0.05, new int[]{-865699960, 15});
         double dx2 = target.posX - this.posX;
         double dz2 = target.posZ - this.posZ;
         double len2 = Math.sqrt(dx2 * dx2 + dz2 * dz2);
         if (len2 > (double)0.5F) {
            this.motionX = dx2 / len2 * 0.6;
            this.motionZ = dz2 / len2 * 0.6;
            this.velocityChanged = true;
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.5F);
         this.zabuzaSay("§8Silent Killing...");
      }

      private void performWaterPrison(EntityLivingBase target, float baseDmg, double dmgMul) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double len = Math.sqrt(dx * dx + dz * dz);
         if (len > (double)1.0F) {
            double lungeX = target.posX - dx / len * (double)1.5F;
            double lungeZ = target.posZ - dz / len * (double)1.5F;
            double safeY = this.findSafeY(lungeX, target.posY, lungeZ);
            this.setPositionAndUpdate(lungeX, safeY, lungeZ);
            this.motionX = (double)0.0F;
            this.motionY = (double)0.0F;
            this.motionZ = (double)0.0F;
            this.velocityChanged = true;
         }

         float trueDmg;
         float normalDmg;
         if (this.trueDamageSplit > 0.0F) {
            normalDmg = baseDmg * 0.7F * (1.0F - this.trueDamageSplit) * (float)dmgMul;
            trueDmg = baseDmg * 0.7F * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
         } else {
            normalDmg = baseDmg * 0.7F * (float)dmgMul;
            trueDmg = 6.0F * this.trueDamageMultiplier;
         }

         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 2));
         target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 60, 1));
         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, target.posX, target.posY + (double)0.5F, target.posZ, 30, (double)1.0F, (double)0.5F, (double)1.0F, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 1.5F, 0.8F);
         this.zabuzaSay("§9Water Prison Technique!");
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
               if (this.comboStep > 3) {
                  this.comboStep = 0;
                  this.comboDelay = 0;
                  this.comboTarget = null;
                  return;
               }

               int delay = COMBO_DELAYS[this.comboStep - 1];
               if (this.phase >= 3) {
                  delay = Math.max(1, delay / 2);
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
            float stepMul = step == 1 ? 0.8F : (step == 2 ? 1.0F : 1.3F);
            float normalDmg = baseDmg * stepMul * (float)dmgMul;
            float trueDmg = 4.0F + (float)step * 2.0F;
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            if (step == 3) {
               double dx = target.posX - this.posX;
               double dz = target.posZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > 0.1) {
                  target.motionX += dx / len * (double)1.5F;
                  target.motionY += 0.3;
                  target.motionZ += dz / len * (double)1.5F;
                  target.velocityChanged = true;
               }

               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.MAGIC, 4.8F * this.trueDamageMultiplier);
               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.6F);
            } else {
               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 0.8F + (float)step * 0.05F);
            }

            this.swingArm(step % 2 == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
            Particles.spawnParticle(this.world, Types.SMOKE, target.posX, target.posY + (double)1.0F, target.posZ, 5, 0.3, 0.3, 0.3, (double)0.0F, 0.02, (double)0.0F, new int[]{-864577161, 12});
         }
      }

      protected void tickStyleCooldowns() {
         if (this.meleeCD > 0) {
            --this.meleeCD;
         }

         if (this.cleaveCD > 0) {
            --this.cleaveCD;
         }

         if (this.waterDragonCD > 0) {
            --this.waterDragonCD;
         }

         if (this.silentKillingCD > 0) {
            --this.silentKillingCD;
         }

         if (this.waterPrisonCD > 0) {
            --this.waterPrisonCD;
         }

         if (this.antiYCheeseCD > 0) {
            --this.antiYCheeseCD;
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
         this.meleeCD = 0;
         this.cleaveCD = 0;
         this.waterDragonCD = 0;
         this.silentKillingCD = 0;
         this.waterPrisonCD = 0;
         this.antiYCheeseCD = 0;
         this.phase2Announced = false;
         this.phase3Announced = false;
         this.comboStep = 0;
         this.comboDelay = 0;
         this.comboTarget = null;
         this.mistTickCounter = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("wbZabuzaPhase", this.phase);
         compound.setInteger("wbZabuzaAttackTick", this.attackTick);
         compound.setBoolean("wbZabuzaIntroPlayed", this.introPlayed);
         compound.setInteger("wbZabuzaMeleeCD", this.meleeCD);
         compound.setInteger("wbZabuzaCleaveCD", this.cleaveCD);
         compound.setInteger("wbZabuzaWaterDragonCD", this.waterDragonCD);
         compound.setInteger("wbZabuzaSilentKillingCD", this.silentKillingCD);
         compound.setInteger("wbZabuzaWaterPrisonCD", this.waterPrisonCD);
         compound.setInteger("wbZabuzaAntiYCheeseCD", this.antiYCheeseCD);
         compound.setBoolean("wbZabuzaPhase2Announced", this.phase2Announced);
         compound.setBoolean("wbZabuzaPhase3Announced", this.phase3Announced);
         compound.setInteger("wbZabuzaMistTickCounter", this.mistTickCounter);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.phase = compound.hasKey("wbZabuzaPhase") ? compound.getInteger("wbZabuzaPhase") : 1;
         this.attackTick = compound.hasKey("wbZabuzaAttackTick") ? compound.getInteger("wbZabuzaAttackTick") : 0;
         this.introPlayed = compound.getBoolean("wbZabuzaIntroPlayed");
         this.meleeCD = compound.hasKey("wbZabuzaMeleeCD") ? compound.getInteger("wbZabuzaMeleeCD") : 0;
         this.cleaveCD = compound.hasKey("wbZabuzaCleaveCD") ? compound.getInteger("wbZabuzaCleaveCD") : 0;
         this.waterDragonCD = compound.hasKey("wbZabuzaWaterDragonCD") ? compound.getInteger("wbZabuzaWaterDragonCD") : 0;
         this.silentKillingCD = compound.hasKey("wbZabuzaSilentKillingCD") ? compound.getInteger("wbZabuzaSilentKillingCD") : 0;
         this.waterPrisonCD = compound.hasKey("wbZabuzaWaterPrisonCD") ? compound.getInteger("wbZabuzaWaterPrisonCD") : 0;
         this.antiYCheeseCD = compound.hasKey("wbZabuzaAntiYCheeseCD") ? compound.getInteger("wbZabuzaAntiYCheeseCD") : 0;
         this.phase2Announced = compound.getBoolean("wbZabuzaPhase2Announced");
         this.phase3Announced = compound.getBoolean("wbZabuzaPhase3Announced");
         this.mistTickCounter = compound.hasKey("wbZabuzaMistTickCounter") ? compound.getInteger("wbZabuzaMistTickCounter") : 0;
      }

      protected void onCombatDeath() {
         this.zabuzaSay("§7So... this is how it ends... in the mist...");
      }

      protected boolean usesVanillaMeleeAI() {
         return true;
      }

      private void zabuzaSay(String msg) {
         if (this.chatCooldown <= 0) {
            String name = this.getCustomNameTag();
            if (name == null || name.isEmpty()) {
               name = "Zabuza";
            }

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)40.0F))) {
               p.sendMessage(new TextComponentString("§8" + name + ": " + msg));
            }

            this.chatCooldown = 80;
         }
      }
   }
}
