
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
public class EntityPainPretaNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 323;

   public EntityPainPretaNPC(ElementsInfTsukAddon instance) {
      super(instance, 323);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "painpretanpc"), 323).name("painpretanpc").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, PretaPathNpcRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class PretaPathNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public PretaPathNpcRenderer(RenderManager renderManager) {
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
      private boolean absorptionShieldActive = false;
      private int absorptionShieldTimer = 0;
      private int absorptionShieldCooldown = 0;
      private int grabCooldown = 0;
      private float damageAbsorbedDuringShield = 0.0F;
      private boolean introPlayed = false;
      private static final int SHIELD_BASE_CD = 400;
      private static final int SHIELD_DURATION = 100;
      private static final float COUNTER_ABSORB_HEAL_PERCENT = 0.08F;
      private static final int GRAB_BASE_CD = 360;
      private static final double GRAB_RANGE = (double)3.0F;
      private static final int GRAB_SLOWNESS_DURATION = 60;
      private static final float GRAB_HEAL_AMOUNT = 4.0F;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processPretaPathCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.absorptionShieldCooldown > 0) {
            --this.absorptionShieldCooldown;
         }

         if (this.grabCooldown > 0) {
            --this.grabCooldown;
         }

         if (this.absorptionShieldActive) {
            --this.absorptionShieldTimer;
            if (this.absorptionShieldTimer <= 0) {
               this.absorptionShieldActive = false;
               this.damageAbsorbedDuringShield = 0.0F;
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.4, 0.6, 0.4, 0.05, new int[0]);
               }
            } else if (this.ticksExisted % 3 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.0F, this.posZ, 6, (double)0.5F, 0.8, (double)0.5F, 0.3, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.5F, this.posZ, 3, 0.3, 0.4, 0.3, 0.02, new int[0]);
            }
         }

      }

      protected void resetCombatState() {
         this.introPlayed = false;
         this.absorptionShieldActive = false;
         this.absorptionShieldTimer = 0;
         this.absorptionShieldCooldown = 0;
         this.grabCooldown = 0;
         this.damageAbsorbedDuringShield = 0.0F;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("pretaIntroPlayed", this.introPlayed);
         compound.setBoolean("absorptionShieldActive", this.absorptionShieldActive);
         compound.setInteger("absorptionShieldTimer", this.absorptionShieldTimer);
         compound.setInteger("absorptionShieldCooldown", this.absorptionShieldCooldown);
         compound.setInteger("grabCooldown", this.grabCooldown);
         compound.setFloat("damageAbsorbedDuringShield", this.damageAbsorbedDuringShield);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.introPlayed = compound.getBoolean("pretaIntroPlayed");
         this.absorptionShieldActive = compound.getBoolean("absorptionShieldActive");
         this.absorptionShieldTimer = compound.hasKey("absorptionShieldTimer") ? compound.getInteger("absorptionShieldTimer") : 0;
         this.absorptionShieldCooldown = compound.hasKey("absorptionShieldCooldown") ? compound.getInteger("absorptionShieldCooldown") : 0;
         this.grabCooldown = compound.hasKey("grabCooldown") ? compound.getInteger("grabCooldown") : 0;
         this.damageAbsorbedDuringShield = compound.hasKey("damageAbsorbedDuringShield") ? compound.getFloat("damageAbsorbedDuringShield") : 0.0F;
      }

      protected void onCombatDeath() {
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.0F, this.posZ, 50, 0.8, 1.2, 0.8, (double)0.5F, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)0.5F, this.posZ, 30, 0.6, 0.8, 0.6, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_HURT, SoundCategory.HOSTILE, 1.2F, 0.5F);
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         if (this.absorptionShieldActive) {
            this.damageAbsorbedDuringShield += amount;
            float healAmount = amount * 0.08F;
            this.heal(healAmount);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + (double)1.0F, this.posZ, 8, 0.3, (double)0.5F, 0.3, 0.05, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL, this.posX, this.posY + (double)1.5F, this.posZ, 5, 0.2, 0.3, 0.2, 0.02, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.HOSTILE, 0.6F, 1.5F);
            return 0.0F;
         } else {
            return amount;
         }
      }

      private void processPretaPathCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            if (!this.introPlayed) {
               this.introPlayed = true;
               this.pretaPathBroadcast("§5Pain: §dYour jutsu are useless against the Preta Path.");
            }

            if (!this.absorptionShieldActive && this.absorptionShieldCooldown <= 0 && dist <= (double)15.0F && this.rand.nextFloat() < 0.25F) {
               this.activateAbsorptionShield();
               this.absorptionShieldCooldown = this.cdMul(400);
            } else if (this.grabCooldown <= 0 && dist <= (double)3.0F && this.rand.nextFloat() < 0.3F) {
               this.performGrab(target, dist);
               this.grabCooldown = this.cdMul(360);
            } else {
               double moveSpeed = (double)1.0F;
               if (dist > (double)8.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, moveSpeed);
               } else if (dist > (double)3.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, moveSpeed * 0.85);
               }

               if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= 2.8) {
                  this.performMeleeSwing(target);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + (double)1.0F, target.posZ, 5, 0.2, 0.3, 0.2, 0.03, new int[0]);
                  }
               }

            }
         }
      }

      private void activateAbsorptionShield() {
         this.absorptionShieldActive = true;
         this.absorptionShieldTimer = 100;
         this.damageAbsorbedDuringShield = 0.0F;
         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 24; ++i) {
               double angle = (double)i / (double)24.0F * Math.PI * (double)2.0F;
               double px = this.posX + Math.cos(angle) * (double)2.0F;
               double pz = this.posZ + Math.sin(angle) * (double)2.0F;
               ws.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, px, this.posY + (double)1.0F, pz, 4, 0.1, (double)0.5F, 0.1, 0.3, new int[0]);
            }

            for(int j = 0; j < 6; ++j) {
               ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)j * 0.4, this.posZ, 8, 0.3, 0.1, 0.3, 0.05, new int[0]);
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.HOSTILE, 1.5F, 0.6F);
         this.pretaPathBroadcast("§5Pain: §dPreta Path... Absorption!");
      }

      private void performGrab(EntityLivingBase target, double dist) {
         this.swingArm(EnumHand.MAIN_HAND);
         if (target instanceof EntityLivingBase) {
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
         }

         this.heal(4.0F);
         float drainDamage = 4.0F;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), drainDamage);
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            double dx = this.posX - target.posX;
            double dy = this.posY + (double)1.0F - (target.posY + (double)1.0F);
            double dz = this.posZ - target.posZ;

            for(int i = 0; i < 8; ++i) {
               double t = (double)i / (double)8.0F;
               double px = target.posX + dx * t;
               double py = target.posY + (double)1.0F + dy * t;
               double pz = target.posZ + dz * t;
               ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, px, py, pz, 2, 0.05, 0.05, 0.05, 0.02, new int[0]);
               ws.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, px, py, pz, 1, 0.05, 0.05, 0.05, 0.1, new int[0]);
            }

            ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + (double)1.0F, this.posZ, 10, 0.3, (double)0.5F, 0.3, 0.05, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 0.8F, 1.4F);
         this.pretaPathBroadcast("§5Pain: §dYour chakra is mine.");
         if (target instanceof EntityPlayerMP) {
            ((EntityPlayerMP)target).sendMessage(new TextComponentString("§5§o* The Preta Path grabs you, draining your chakra! *"));
         }

      }

      private void pretaPathBroadcast(String msg) {
         for(EntityPlayer p : this.world.playerEntities) {
            if ((double)p.getDistance(this) < (double)48.0F && p instanceof EntityPlayerMP) {
               ((EntityPlayerMP)p).sendMessage(new TextComponentString(msg));
            }
         }

      }
   }
}
