
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
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
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
public class EntityFangNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 236;

   public EntityFangNPC(ElementsInfTsukAddon instance) {
      super(instance, 236);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "fangnpc"), 236).name("fangnpc").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, FangNpcRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class FangNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public FangNpcRenderer(RenderManager renderManager) {
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
         float alpha = 1.0F;
         String configId = entity.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null) {
               alpha = config.getGhostAlpha();
            }
         }

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
   }

   public static class EntityCustom extends QuestNpcBase {
      private int fangOverFangCD = 0;
      private int fangTunnelingCD = 0;
      private int fangMarkingCD = 0;
      private int fangBeastCloneCD = 0;
      private boolean fangOverFangActive = false;
      private int fangOverFangTicks = 0;
      private Vec3d fangDashTarget = null;
      private boolean fangPhase2 = false;
      private boolean fangIntroPlayed = false;
      private boolean fangMarkedTarget = false;
      private int fangMarkTicks = 0;
      private boolean fangBeastCloneActive = false;
      private int fangBeastCloneTicks = 0;
      private double fangPreCloneSpeed = (double)-1.0F;
      private int fangDashPass = 0;
      private Vec3d fangDashOrigin = null;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processFangCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.fangOverFangCD > 0) {
            --this.fangOverFangCD;
         }

         if (this.fangTunnelingCD > 0) {
            --this.fangTunnelingCD;
         }

         if (this.fangMarkingCD > 0) {
            --this.fangMarkingCD;
         }

         if (this.fangBeastCloneCD > 0) {
            --this.fangBeastCloneCD;
         }

         if (this.fangMarkedTarget && this.fangMarkTicks > 0) {
            --this.fangMarkTicks;
            if (this.fangMarkTicks <= 0) {
               this.fangMarkedTarget = false;
            }
         }

         if (this.fangBeastCloneActive && this.fangBeastCloneTicks > 0) {
            --this.fangBeastCloneTicks;
            if (this.fangBeastCloneTicks <= 0) {
               this.fangBeastCloneActive = false;
               if (this.fangPreCloneSpeed > (double)0.0F) {
                  IAttributeInstance speedAttr = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                  if (speedAttr != null) {
                     speedAttr.setBaseValue(this.fangPreCloneSpeed);
                  }

                  this.fangPreCloneSpeed = (double)-1.0F;
               }
            }
         }

      }

      protected void resetCombatState() {
         this.fangOverFangActive = false;
         this.fangOverFangTicks = 0;
         this.fangDashTarget = null;
         this.fangPhase2 = false;
         this.fangIntroPlayed = false;
         this.fangMarkedTarget = false;
         this.fangBeastCloneActive = false;
         this.fangBeastCloneTicks = 0;
         this.fangPreCloneSpeed = (double)-1.0F;
         this.fangDashPass = 0;
         this.fangDashOrigin = null;
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         if (!this.world.isRemote && this.fangOverFangActive) {
            amount *= 0.5F;
         }

         if (!this.world.isRemote && this.fangBeastCloneActive && this.rand.nextFloat() < 0.2F) {
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 10, 0.3, 0.4, 0.3, 0.05, new int[0]);
            }

            return -1.0F;
         } else {
            if (!this.world.isRemote && !this.fangPhase2 && this.getHealth() / this.getMaxHealth() < 0.4F) {
               this.fangPhase2 = true;
               this.fangSay("§6Akamaru and I won't lose! Let's go all out!");
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, this.posX, this.posY + (double)1.5F, this.posZ, 25, (double)1.0F, (double)1.0F, (double)1.0F, 0.1, new int[0]);
               }

               IAttributeInstance speedAttr = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
               if (speedAttr != null) {
                  double baseSpeed = speedAttr.getBaseValue();
                  speedAttr.setBaseValue(baseSpeed * 1.2);
               }
            }

            return amount;
         }
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("fangPhase2", this.fangPhase2);
         compound.setInteger("fangOverFangCD", this.fangOverFangCD);
         compound.setInteger("fangTunnelingCD", this.fangTunnelingCD);
         compound.setInteger("fangMarkingCD", this.fangMarkingCD);
         compound.setInteger("fangBeastCloneCD", this.fangBeastCloneCD);
         compound.setBoolean("fangIntroPlayed", this.fangIntroPlayed);
         compound.setBoolean("fangMarkedTarget", this.fangMarkedTarget);
         compound.setInteger("fangMarkTicks", this.fangMarkTicks);
         compound.setBoolean("fangBeastCloneActive", this.fangBeastCloneActive);
         compound.setInteger("fangBeastCloneTicks", this.fangBeastCloneTicks);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.fangPhase2 = compound.getBoolean("fangPhase2");
         this.fangOverFangCD = compound.hasKey("fangOverFangCD") ? compound.getInteger("fangOverFangCD") : 0;
         this.fangTunnelingCD = compound.hasKey("fangTunnelingCD") ? compound.getInteger("fangTunnelingCD") : 0;
         this.fangMarkingCD = compound.hasKey("fangMarkingCD") ? compound.getInteger("fangMarkingCD") : 0;
         this.fangBeastCloneCD = compound.hasKey("fangBeastCloneCD") ? compound.getInteger("fangBeastCloneCD") : 0;
         this.fangIntroPlayed = compound.getBoolean("fangIntroPlayed");
         this.fangMarkedTarget = compound.getBoolean("fangMarkedTarget");
         this.fangMarkTicks = compound.hasKey("fangMarkTicks") ? compound.getInteger("fangMarkTicks") : 0;
         this.fangBeastCloneActive = compound.getBoolean("fangBeastCloneActive");
         this.fangBeastCloneTicks = compound.hasKey("fangBeastCloneTicks") ? compound.getInteger("fangBeastCloneTicks") : 0;
      }

      private void processFangCombat(EntityLivingBase target, double dist) {
         if (target != null && target.isEntityAlive()) {
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            if (!this.fangIntroPlayed) {
               this.fangIntroPlayed = true;
               this.fangSay("§6Heh, you don't look so tough! Right, Akamaru? Let's get 'em!");
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WOLF_GROWL, SoundCategory.HOSTILE, 1.0F, 1.0F);
            }

            if (this.fangBeastCloneActive && this.ticksExisted % 3 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)1.0F, this.posZ, 3, 0.3, (double)0.5F, 0.3, 0.05, new int[0]);
            }

            if (this.fangOverFangActive) {
               this.processFangOverFangTick(target);
            } else {
               if (dist > (double)6.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, (double)1.5F);
               }

               double dmgMul = this.getDamageMultiplier();
               float cdMul = this.fangPhase2 ? 0.7F : 1.0F;
               float markBonus = this.fangMarkedTarget ? 1.3F : 1.0F;
               float markTrueExtra = this.fangMarkedTarget ? 1.0F : 0.0F;
               if (this.fangBeastCloneCD <= 0 && !this.fangBeastCloneActive && this.rand.nextFloat() < 0.1F) {
                  int[] cd = this.getCooldownRange((int)(240.0F * cdMul), (int)(320.0F * cdMul));
                  this.fangBeastCloneCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.fangBeastCloneActive = true;
                  this.fangBeastCloneTicks = 80;
                  this.fangSay("§6Man-Beast Clone!");
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
                  }

                  IAttributeInstance speedAttr = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                  if (speedAttr != null && this.fangPreCloneSpeed < (double)0.0F) {
                     this.fangPreCloneSpeed = speedAttr.getBaseValue();
                     speedAttr.setBaseValue(this.fangPreCloneSpeed * 1.4);
                  }

               } else if (this.fangMarkingCD <= 0 && dist <= (double)10.0F && !this.fangMarkedTarget && this.rand.nextFloat() < 0.12F) {
                  int[] cd = this.getCooldownRange((int)(200.0F * cdMul), (int)(280.0F * cdMul));
                  this.fangMarkingCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.fangMarkedTarget = true;
                  this.fangMarkTicks = 100;
                  target.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 100, 0));
                  this.fangSay("§6Akamaru, mark 'em!");
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, target.posX, target.posY + (double)1.0F, target.posZ, 10, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
                  }

               } else if (this.fangOverFangCD <= 0 && dist >= (double)6.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.18F) {
                  int[] cd = this.getCooldownRange((int)(100.0F * cdMul), (int)(160.0F * cdMul));
                  this.fangOverFangCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.fangSay("§6Fang Over Fang!");
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WOLF_GROWL, SoundCategory.HOSTILE, 1.2F, 0.8F);
                  this.fangOverFangActive = true;
                  this.fangOverFangTicks = 15;
                  this.fangDashPass = 1;
                  this.fangDashTarget = new Vec3d(target.posX, target.posY, target.posZ);
                  this.fangDashOrigin = new Vec3d(this.posX, this.posY, this.posZ);
               } else if (this.fangTunnelingCD <= 0 && dist >= (double)4.0F && dist <= (double)12.0F && this.rand.nextFloat() < 0.22F) {
                  int[] cd = this.getCooldownRange((int)(60.0F * cdMul), (int)(100.0F * cdMul));
                  this.fangTunnelingCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  double dx = target.posX - this.posX;
                  double dz = target.posZ - this.posZ;
                  double horizDist = Math.sqrt(dx * dx + dz * dz);
                  if (horizDist > (double)0.5F) {
                     double nx = dx / horizDist;
                     double nz = dz / horizDist;
                     double dashDist2 = Math.max(horizDist - (double)1.5F, (double)1.0F);
                     double vel = dashDist2 / (double)8.0F;
                     this.isDashing = true;
                     this.dashType = 2;
                     this.dashDuration = 8;
                     this.dashTicksRemaining = 8;
                     this.dashVelX = nx * vel * (double)1.5F;
                     this.dashVelZ = nz * vel * (double)1.5F;
                     this.dashArcSustain = 0.05F;
                     this.dashFallAccel = 0.06F;
                     this.dashMaxFall = -0.4F;
                     this.dashHasMidairGuidance = false;
                     this.motionY = (double)0.25F;
                     this.velocityChanged = true;
                  }

                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 10, 0.4, 0.3, 0.4, 0.05, new int[0]);
                  }

                  if (dist <= (double)5.0F) {
                     target.hurtResistantTime = 0;
                     target.attackEntityFrom(DamageSource.causeMobDamage(this), 6.0F * (float)dmgMul * markBonus);
                     target.hurtResistantTime = 0;
                     target.attackEntityFrom(DamageSource.MAGIC, 2.0F + markTrueExtra);
                     double kbx = target.posX - this.posX;
                     double kbz = target.posZ - this.posZ;
                     double kblen = Math.sqrt(kbx * kbx + kbz * kbz);
                     if (kblen > 0.1) {
                        target.motionX += kbx / kblen * (double)1.0F;
                        target.motionY += 0.2;
                        target.motionZ += kbz / kblen * (double)1.0F;
                        target.velocityChanged = true;
                     }
                  }

               } else {
                  if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
                     float baseDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                     float normalDmg = baseDamage * (float)dmgMul * markBonus;
                     float trueDmg = 2.0F + markTrueExtra;
                     if (this.fangBeastCloneActive) {
                        normalDmg *= 1.25F;
                     }

                     target.hurtResistantTime = 0;
                     target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                     target.hurtResistantTime = 0;
                     target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                     this.swingArm(EnumHand.MAIN_HAND);
                     if (this.fangBeastCloneActive) {
                        target.hurtResistantTime = 0;
                        target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * 0.7F);
                        target.hurtResistantTime = 0;
                        target.attackEntityFrom(DamageSource.MAGIC, 1.0F);
                        this.swingArm(EnumHand.OFF_HAND);
                     }

                     int[] cdRange = this.getCooldownRange(15, 25);
                     this.meleeCooldown = cdRange[0] + this.rand.nextInt(cdRange[1] - cdRange[0] + 1);
                  }

               }
            }
         }
      }

      private void processFangOverFangTick(EntityLivingBase target) {
         --this.fangOverFangTicks;
         double dmgMul = this.getDamageMultiplier();
         float markBonus = this.fangMarkedTarget ? 1.3F : 1.0F;
         float markTrueExtra = this.fangMarkedTarget ? 1.0F : 0.0F;
         if (this.fangDashPass == 1) {
            if (this.fangDashTarget != null) {
               double dx = this.fangDashTarget.x - this.posX;
               double dz = this.fangDashTarget.z - this.posZ;
               double horizDist = Math.sqrt(dx * dx + dz * dz);
               if (horizDist > (double)0.5F) {
                  double speed = horizDist / (double)Math.max(this.fangOverFangTicks, 1);
                  this.motionX = dx / horizDist * Math.min(speed, (double)2.0F);
                  this.motionZ = dz / horizDist * Math.min(speed, (double)2.0F);
                  this.motionY = 0.05;
                  this.velocityChanged = true;
               }
            }

            if (this.world instanceof WorldServer && this.ticksExisted % 2 == 0) {
               double angle = (double)this.ticksExisted * 0.8;
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX + Math.cos(angle) * 0.8, this.posY + (double)1.0F, this.posZ + Math.sin(angle) * 0.8, 3, 0.2, 0.3, 0.2, 0.05, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 2, 0.3, 0.2, 0.3, 0.02, new int[0]);
            }

            for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)1.5F))) {
               if (e != this && !(e instanceof EntityCustom)) {
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.causeMobDamage(this), 8.0F * (float)dmgMul * markBonus);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.MAGIC, 3.0F + markTrueExtra);
                  double kbx = e.posX - this.posX;
                  double kbz = e.posZ - this.posZ;
                  double kblen = Math.sqrt(kbx * kbx + kbz * kbz);
                  if (kblen > 0.1) {
                     e.motionX += kbx / kblen * (double)1.5F;
                     e.motionY += 0.3;
                     e.motionZ += kbz / kblen * (double)1.5F;
                     e.velocityChanged = true;
                  }
               }
            }

            if (this.fangOverFangTicks <= 0) {
               this.fangDashPass = 2;
               this.fangOverFangTicks = 10;
               if (target != null && target.isEntityAlive()) {
                  this.fangDashTarget = new Vec3d(target.posX, target.posY, target.posZ);
               } else {
                  this.fangDashTarget = this.fangDashOrigin;
               }
            }
         } else if (this.fangDashPass == 2) {
            if (this.fangDashTarget != null) {
               double dx = this.fangDashTarget.x - this.posX;
               double dz = this.fangDashTarget.z - this.posZ;
               double horizDist = Math.sqrt(dx * dx + dz * dz);
               if (horizDist > (double)0.5F) {
                  double speed = horizDist / (double)Math.max(this.fangOverFangTicks, 1);
                  this.motionX = dx / horizDist * Math.min(speed, (double)2.0F);
                  this.motionZ = dz / horizDist * Math.min(speed, (double)2.0F);
                  this.motionY = 0.05;
                  this.velocityChanged = true;
               }
            }

            if (this.world instanceof WorldServer) {
               double angle = (double)this.ticksExisted * 0.8;
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX + Math.cos(angle) * 0.8, this.posY + (double)1.0F, this.posZ + Math.sin(angle) * 0.8, 2, 0.2, 0.3, 0.2, 0.05, new int[0]);
            }

            for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)1.5F))) {
               if (e != this && !(e instanceof EntityCustom)) {
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.causeMobDamage(this), 6.0F * (float)dmgMul * markBonus);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.MAGIC, 2.0F + markTrueExtra);
               }
            }

            if (this.fangOverFangTicks <= 0) {
               this.fangOverFangActive = false;
               this.fangDashPass = 0;
               this.fangDashTarget = null;
               this.fangDashOrigin = null;
            }
         } else {
            this.fangOverFangActive = false;
            this.fangDashPass = 0;
         }

      }

      private void fangSay(String msg) {
         if (this.chatCooldown <= 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
               p.sendMessage(new TextComponentString("§6" + this.getCustomNameTag() + ": §e" + msg));
            }

            this.chatCooldown = 80;
         }
      }
   }
}
