
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
import net.minecraft.entity.player.EntityPlayerMP;
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
public class EntityPainHumanNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 322;

   public EntityPainHumanNPC(ElementsInfTsukAddon instance) {
      super(instance, 322);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "painhumannpc"), 322).name("painhumannpc").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, HumanPathNpcRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class HumanPathNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public HumanPathNpcRenderer(RenderManager renderManager) {
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
            GlStateManager.color(0.4F, 0.6F, 1.0F, alpha);
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
      private int soulGrabCooldown = 0;
      private int lungeCooldown = 0;
      private boolean introPlayed = false;
      private static final int SOUL_GRAB_BASE_CD = 240;
      private static final double SOUL_GRAB_RANGE = (double)4.0F;
      private static final float SOUL_GRAB_DAMAGE_MULT = 3.0F;
      private static final int SOUL_GRAB_DEBUFF_DURATION = 60;
      private static final int LUNGE_BASE_CD = 160;
      private static final double LUNGE_MIN_RANGE = (double)8.0F;
      private static final double LUNGE_MAX_RANGE = (double)12.0F;
      private static final float LUNGE_BONUS_DAMAGE = 1.5F;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processHumanPathCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.soulGrabCooldown > 0) {
            --this.soulGrabCooldown;
         }

         if (this.lungeCooldown > 0) {
            --this.lungeCooldown;
         }

      }

      protected void resetCombatState() {
         this.introPlayed = false;
         this.soulGrabCooldown = 0;
         this.lungeCooldown = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("humanIntroPlayed", this.introPlayed);
         compound.setInteger("soulGrabCooldown", this.soulGrabCooldown);
         compound.setInteger("lungeCooldown", this.lungeCooldown);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.introPlayed = compound.getBoolean("humanIntroPlayed");
         this.soulGrabCooldown = compound.hasKey("soulGrabCooldown") ? compound.getInteger("soulGrabCooldown") : 0;
         this.lungeCooldown = compound.hasKey("lungeCooldown") ? compound.getInteger("lungeCooldown") : 0;
      }

      protected void onCombatDeath() {
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 40, 0.6, (double)1.0F, 0.6, 0.15, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 20, (double)0.5F, 0.8, (double)0.5F, 0.05, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_HURT, SoundCategory.HOSTILE, 1.0F, 0.6F);
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         return amount;
      }

      private void processHumanPathCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            if (!this.introPlayed) {
               this.introPlayed = true;
               this.humanPathBroadcast("§5Pain: §dYour soul reveals all your secrets...");
            }

            if (this.soulGrabCooldown <= 0 && dist <= (double)4.0F && this.rand.nextFloat() < 0.35F) {
               this.performSoulGrab(target, dist);
               this.soulGrabCooldown = this.cdMul(240);
            } else if (!this.isDashing && this.lungeCooldown <= 0 && dist >= (double)8.0F && dist <= (double)12.0F && this.rand.nextFloat() < 0.35F) {
               this.performLunge(target, dist);
               this.lungeCooldown = this.cdMul(160);
            } else if (!this.isDashing && this.dashCooldown <= 0 && dist >= (double)12.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.25F) {
               this.performLunge(target, dist);
               this.dashCooldown = this.cdMul(200);
            } else {
               double moveSpeed = 1.3;
               if (dist > (double)6.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, moveSpeed);
               } else if (dist > (double)2.5F) {
                  this.getNavigator().tryMoveToEntityLiving(target, moveSpeed * 0.9);
               }

               if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= 2.8) {
                  this.performMeleeSwing(target);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, target.posX, target.posY + (double)1.0F, target.posZ, 5, 0.2, 0.3, 0.2, 0.05, new int[0]);
                  }
               }

            }
         }
      }

      private void performSoulGrab(EntityLivingBase target, double dist) {
         if (dist > (double)2.0F) {
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double horizDist = Math.sqrt(dx * dx + dz * dz);
            if (horizDist > (double)0.5F) {
               double nx = dx / horizDist;
               double nz = dz / horizDist;
               this.setPositionAndUpdate(target.posX - nx * (double)1.5F, target.posY, target.posZ - nz * (double)1.5F);
            }
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.3, (double)0.5F, 0.3, 0.1, new int[0]);
         }

         double dmgMult = this.getDamageMultiplier();
         float burstDamage = (float)(this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() * (double)3.0F * dmgMult);
         target.attackEntityFrom(DamageSource.causeMobDamage(this), burstDamage);
         if (target instanceof EntityLivingBase) {
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 2));
            target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 1));
         }

         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.PORTAL, target.posX, target.posY + (double)1.0F, target.posZ, 30, 0.3, 0.8, 0.3, 0.15, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + (double)1.5F, target.posZ, 15, 0.2, (double)0.5F, 0.2, 0.05, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, target.posX, target.posY + (double)0.5F, target.posZ, 10, 0.3, 0.3, 0.3, 0.02, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.0F, 1.2F);
         this.humanPathBroadcast("§5Pain: §dHuman Path... Soul Extraction!");
         if (target instanceof EntityPlayerMP) {
            ((EntityPlayerMP)target).sendMessage(new TextComponentString("§5§o* The Human Path grabs you, tearing at your very soul! *"));
         }

      }

      private void performLunge(EntityLivingBase target, double dist) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (!(horizDist < (double)0.5F)) {
            double nx = dx / horizDist;
            double nz = dz / horizDist;
            int duration = 6;
            double dashDist = Math.max(horizDist - (double)1.5F, (double)1.0F);
            double velocityPerTick = dashDist / (double)duration;
            this.isDashing = true;
            this.dashType = 2;
            this.dashDuration = duration;
            this.dashTicksRemaining = duration;
            this.dashVelX = nx * velocityPerTick * 1.6;
            this.dashVelZ = nz * velocityPerTick * 1.6;
            this.dashArcSustain = 0.04F;
            this.dashFallAccel = 0.07F;
            this.dashMaxFall = -0.4F;
            this.dashHasMidairGuidance = false;
            this.motionY = (double)0.25F;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 12, 0.2, 0.3, 0.2, 0.1, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)0.5F, this.posZ, 8, 0.3, 0.2, 0.3, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.3F);
         }
      }

      private void humanPathBroadcast(String msg) {
         for(EntityPlayer p : this.world.playerEntities) {
            if ((double)p.getDistance(this) < (double)48.0F && p instanceof EntityPlayerMP) {
               ((EntityPlayerMP)p).sendMessage(new TextComponentString(msg));
            }
         }

      }
   }
}
