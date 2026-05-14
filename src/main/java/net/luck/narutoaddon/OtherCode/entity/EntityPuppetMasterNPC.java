
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
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityPuppetMasterNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 237;

   public EntityPuppetMasterNPC(ElementsInfTsukAddon instance) {
      super(instance, 237);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "puppetmasternpc"), 237).name("puppetmasternpc").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, PuppetNpcRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class PuppetNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public PuppetNpcRenderer(RenderManager renderManager) {
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
      private int puppetPoisonCD = 0;
      private int puppetStringsCD = 0;
      private int puppetDashCD = 0;
      private int puppetBombCD = 0;
      private boolean puppetPhase2 = false;
      private boolean puppetIntroPlayed = false;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (!this.puppetIntroPlayed && this.chatCooldown <= 0) {
            this.puppetIntroPlayed = true;
            this.chatCooldown = 200;
         }

         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         double dmgMul = this.getDamageMultiplier();
         if (!this.puppetPhase2 && this.getHealth() / this.getMaxHealth() <= 0.5F) {
            this.puppetPhase2 = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
            }
         }

         float cdMul = this.puppetPhase2 ? 0.7F : 1.0F;
         if (this.puppetDashCD <= 0 && dist < (double)4.0F) {
            int[] cd = this.getCooldownRange((int)(50.0F * cdMul), (int)(70.0F * cdMul));
            this.puppetDashCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            double dx = this.posX - target.posX;
            double dz = this.posZ - target.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > (double)0.0F) {
               this.motionX = dx / d * 1.8;
               this.motionZ = dz / d * 1.8;
               this.motionY = 0.3;
            }

            float senbonDmg = baseDmg * 0.4F * (float)dmgMul;
            float senbonTrue = baseDmg * 0.15F;

            for(int i = 0; i < 3; ++i) {
               EntityKunaiProjectile.EntityCustom kunai = new EntityKunaiProjectile.EntityCustom(this.world, this, senbonDmg, senbonTrue);
               double kdx = target.posX - this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F;
               double kdy = target.posY + (double)target.getEyeHeight() - 0.1 - kunai.posY;
               double kdz = target.posZ - this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F;
               double kd = Math.sqrt(kdx * kdx + kdz * kdz);
               kunai.shoot(kdx, kdy + kd * 0.1, kdz, 1.2F, 3.0F);
               this.world.spawnEntity(kunai);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.0F, 1.2F);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, (double)0.5F, new int[0]);
            }

         } else if (this.puppetPoisonCD <= 0 && dist >= (double)6.0F && dist <= (double)30.0F && this.rand.nextFloat() < 0.3F) {
            int[] cd = this.getCooldownRange((int)(70.0F * cdMul), (int)(110.0F * cdMul));
            this.puppetPoisonCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            float aoeRange = this.puppetPhase2 ? 7.0F : 5.0F;
            float bombNormal = baseDmg * 0.6F * (float)dmgMul;
            float bombTrue = baseDmg * 0.3F;

            for(Entity e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(target.posX - (double)aoeRange, target.posY - (double)2.0F, target.posZ - (double)aoeRange, target.posX + (double)aoeRange, target.posY + (double)3.0F, target.posZ + (double)aoeRange))) {
               if (e instanceof EntityPlayer) {
                  EntityLivingBase elb = (EntityLivingBase)e;
                  elb.attackEntityFrom(DamageSource.causeMobDamage(this), bombNormal);
                  elb.hurtResistantTime = 0;
                  elb.attackEntityFrom(DamageSource.MAGIC, bombTrue);
                  elb.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 1));
                  if (this.puppetPhase2) {
                     elb.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 0));
                  }
               }
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_MOB, target.posX, target.posY + (double)1.0F, target.posZ, 30, (double)aoeRange * (double)0.5F, (double)1.0F, (double)aoeRange * (double)0.5F, (double)0.0F, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, target.posX, target.posY + (double)0.5F, target.posZ, 15, (double)aoeRange * 0.3, (double)0.5F, (double)aoeRange * 0.3, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 1.0F, 0.8F);
            this.swingArm(EnumHand.MAIN_HAND);
         } else if (this.puppetStringsCD <= 0 && dist >= (double)5.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.28F) {
            int[] cd = this.getCooldownRange((int)(55.0F * cdMul), (int)(90.0F * cdMul));
            this.puppetStringsCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            float stringNormal = baseDmg * 0.5F * (float)dmgMul;
            float stringTrue = baseDmg * 0.2F;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), stringNormal);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, stringTrue);
            double dx = this.posX - target.posX;
            double dz = this.posZ - target.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > (double)0.0F) {
               target.motionX += dx / d * 0.9;
               target.motionZ += dz / d * 0.9;
               target.motionY += 0.15;
               if (target instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)target).velocityChanged = true;
               }
            }

            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 30, 1));
            if (this.world instanceof WorldServer) {
               for(int i = 0; i < 10; ++i) {
                  double frac = (double)i / (double)10.0F;
                  double px = this.posX + (target.posX - this.posX) * frac;
                  double py = this.posY + (double)1.0F + (target.posY + (double)1.0F - this.posY - (double)1.0F) * frac;
                  double pz = this.posZ + (target.posZ - this.posZ) * frac;
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, px, py, pz, 2, 0.05, 0.05, 0.05, (double)0.0F, new int[0]);
               }
            }

            this.swingArm(EnumHand.MAIN_HAND);
         } else if (this.puppetPhase2 && this.puppetDashCD <= 0 && dist >= (double)8.0F && dist <= (double)18.0F && this.rand.nextFloat() < 0.2F) {
            int[] cd = this.getCooldownRange((int)(80.0F * cdMul), (int)(120.0F * cdMul));
            this.puppetDashCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            float bladeDmg = baseDmg * 0.7F * (float)dmgMul;
            float bladeTrue = baseDmg * 0.35F;
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);

            for(int i = 3; i <= (int)Math.min(dist + (double)1.0F, (double)16.0F); i += 2) {
               double px = this.posX + dx / d * (double)i;
               double pz = this.posZ + dz / d * (double)i;

               for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(px - (double)1.5F, this.posY - (double)1.0F, pz - (double)1.5F, px + (double)1.5F, this.posY + (double)3.0F, pz + (double)1.5F))) {
                  if (e != this && !(e instanceof EntityCustom)) {
                     e.attackEntityFrom(DamageSource.causeMobDamage(this), bladeDmg);
                     e.hurtResistantTime = 0;
                     e.attackEntityFrom(DamageSource.MAGIC, bladeTrue);
                  }
               }

               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, px, this.posY + (double)1.0F, pz, 3, 0.3, 0.3, 0.3, (double)0.0F, new int[0]);
               }
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.5F, 0.7F);
            this.swingArm(EnumHand.MAIN_HAND);
         } else if (this.hasRangedAttack && this.kunaiCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F && this.rand.nextFloat() < 0.3F) {
            float senbonDmg = baseDmg * 0.35F * (float)dmgMul;
            float senbonTrue = baseDmg * 0.15F;
            int count = this.puppetPhase2 ? 5 : 3;

            for(int i = 0; i < count; ++i) {
               EntityKunaiProjectile.EntityCustom kunai = new EntityKunaiProjectile.EntityCustom(this.world, this, senbonDmg, senbonTrue);
               double kdx = target.posX - this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.5F;
               double kdy = target.posY + (double)target.getEyeHeight() - 0.1 - kunai.posY + (this.rand.nextDouble() - (double)0.5F);
               double kdz = target.posZ - this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.5F;
               double kd = Math.sqrt(kdx * kdx + kdz * kdz);
               kunai.shoot(kdx, kdy + kd * 0.12, kdz, 1.1F, 3.5F);
               this.world.spawnEntity(kunai);
            }

            int[] cd = this.getCooldownRange((int)(50.0F * cdMul), (int)(80.0F * cdMul));
            this.kunaiCooldown = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
            this.swingArm(EnumHand.MAIN_HAND);
         } else {
            if (dist < (double)6.0F && this.puppetDashCD > 0) {
               double dx = this.posX - target.posX;
               double dz = this.posZ - target.posZ;
               double d = Math.sqrt(dx * dx + dz * dz);
               if (d > (double)0.0F) {
                  this.getNavigator().tryMoveToXYZ(this.posX + dx / d * (double)5.0F, this.posY, this.posZ + dz / d * (double)5.0F, (double)1.0F);
               }
            } else if (dist > (double)20.0F) {
               this.getNavigator().tryMoveToEntityLiving(target, (double)1.0F);
            } else {
               double angle = Math.atan2(this.posZ - target.posZ, this.posX - target.posX);
               angle += this.rand.nextBoolean() ? (double)0.5F : (double)-0.5F;
               double strafeX = target.posX + Math.cos(angle) * (double)12.0F;
               double strafeZ = target.posZ + Math.sin(angle) * (double)12.0F;
               this.getNavigator().tryMoveToXYZ(strafeX, this.posY, strafeZ, 0.8);
            }

            if (this.puppetPhase2 && this.meleeCooldown <= 0 && dist <= (double)3.0F) {
               this.performMeleeSwing(target);
            }

         }
      }

      protected void tickStyleCooldowns() {
         if (this.puppetPoisonCD > 0) {
            --this.puppetPoisonCD;
         }

         if (this.puppetStringsCD > 0) {
            --this.puppetStringsCD;
         }

         if (this.puppetDashCD > 0) {
            --this.puppetDashCD;
         }

         if (this.puppetBombCD > 0) {
            --this.puppetBombCD;
         }

      }

      protected void resetCombatState() {
         this.puppetPhase2 = false;
         this.puppetIntroPlayed = false;
         this.puppetPoisonCD = 0;
         this.puppetStringsCD = 0;
         this.puppetDashCD = 0;
         this.puppetBombCD = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("puppetPhase2", this.puppetPhase2);
         compound.setBoolean("puppetIntroPlayed", this.puppetIntroPlayed);
         compound.setInteger("puppetPoisonCD", this.puppetPoisonCD);
         compound.setInteger("puppetStringsCD", this.puppetStringsCD);
         compound.setInteger("puppetDashCD", this.puppetDashCD);
         compound.setInteger("puppetBombCD", this.puppetBombCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.puppetPhase2 = compound.getBoolean("puppetPhase2");
         this.puppetIntroPlayed = compound.getBoolean("puppetIntroPlayed");
         this.puppetPoisonCD = compound.hasKey("puppetPoisonCD") ? compound.getInteger("puppetPoisonCD") : 0;
         this.puppetStringsCD = compound.hasKey("puppetStringsCD") ? compound.getInteger("puppetStringsCD") : 0;
         this.puppetDashCD = compound.hasKey("puppetDashCD") ? compound.getInteger("puppetDashCD") : 0;
         this.puppetBombCD = compound.hasKey("puppetBombCD") ? compound.getInteger("puppetBombCD") : 0;
      }
   }
}
