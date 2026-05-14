
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.ModelPlayerPoseable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcPose;
import net.minecraft.block.BlockSand;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntitySandNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 239;

   public EntitySandNPC(ElementsInfTsukAddon instance) {
      super(instance, 239);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "sandnpc"), 239).name("sandnpc").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SandNpcRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class SandNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public SandNpcRenderer(RenderManager renderManager) {
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
      private int sandSlideCD = 0;
      private int sandSurgeCD = 0;
      private int sandBuryCD = 0;
      private int sandGrindCD = 0;
      private int sandFistCD = 0;
      private int sandWaveCD = 0;
      private boolean sandSlideActive = false;
      private int sandSlideTicks = 0;
      private Vec3d sandSlideTarget = null;
      private boolean sandSurgeActive = false;
      private boolean sandPhase2 = false;
      private boolean sandIntroPlayed = false;
      private int sandComboStep = 0;
      private int sandComboDelay = 0;
      private EntityLivingBase sandComboTarget = null;

      public EntityCustom(World world) {
         super(world);
      }

      protected boolean isKnockbackImmune() {
         return !this.sandSurgeActive;
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (target != null && target.isEntityAlive()) {
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            this.sandSurgeActive = false;
            if (!this.sandIntroPlayed && this.chatCooldown <= 0) {
               this.sandIntroPlayed = true;
               this.chatCooldown = 200;
            }

            if (!this.sandPhase2 && this.getHealth() / this.getMaxHealth() <= 0.4F) {
               this.sandPhase2 = true;
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)1.0F, this.posZ, 20, 0.8, 0.8, 0.8, 0.1, new int[0]);
               }
            }

            float cdMul = this.sandPhase2 ? 0.75F : 1.0F;
            double dmgMul = this.sandPhase2 ? 1.2 : (double)1.0F;
            if (this.sandSlideActive) {
               this.processSandSlide(target);
            } else if (this.sandComboTarget != null && this.sandComboStep > 0) {
               this.processSandCombo(target);
            } else {
               if (this.world instanceof WorldServer && this.ticksExisted % 3 == 0) {
                  WorldServer var10000 = (WorldServer)this.world;
                  EnumParticleTypes var10001 = EnumParticleTypes.BLOCK_CRACK;
                  double var10002 = this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.8;
                  double var10003 = this.posY + 0.3;
                  double var10004 = this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.8;
                  int[] var10010 = new int[1];
                  BlockSand var10013 = Blocks.SAND;
                  var10010[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                  var10000.spawnParticle(var10001, var10002, var10003, var10004, 2, 0.3, 0.2, 0.3, (double)0.0F, var10010);
               }

               if (this.sandSlideCD <= 0 && dist >= (double)6.0F && dist <= (double)22.0F && this.rand.nextFloat() < 0.35F) {
                  int[] cd = this.getCooldownRange((int)(60.0F * cdMul), (int)(100.0F * cdMul));
                  this.sandSlideCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.sandSlideActive = true;
                  this.sandSlideTicks = 0;
                  this.sandSlideTarget = new Vec3d(target.posX, target.posY, target.posZ);
               } else if (this.sandWaveCD <= 0 && dist >= (double)8.0F && dist <= (double)25.0F && this.rand.nextFloat() < 0.3F) {
                  int[] cd = this.getCooldownRange((int)(70.0F * cdMul), (int)(110.0F * cdMul));
                  this.sandWaveCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  float normalDmg = (float)((double)6.0F * dmgMul);
                  float trueDmg = 1.5F;
                  target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                  if (trueDmg > 0.0F) {
                     target.hurtResistantTime = 0;
                     target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  }

                  this.swingArm(EnumHand.MAIN_HAND);
                  this.meleeCooldown = 15;
                  if (this.world instanceof WorldServer) {
                     WorldServer var27 = (WorldServer)this.world;
                     EnumParticleTypes var30 = EnumParticleTypes.BLOCK_CRACK;
                     double var33 = target.posX;
                     double var36 = target.posY + (double)0.5F;
                     double var39 = target.posZ;
                     int[] var42 = new int[1];
                     BlockSand var45 = Blocks.SAND;
                     var42[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                     var27.spawnParticle(var30, var33, var36, var39, 25, (double)1.0F, (double)0.5F, (double)1.0F, 0.15, var42);
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 10, (double)0.5F, (double)0.5F, (double)0.5F, 0.2, new int[0]);
                     var27 = (WorldServer)this.world;
                     var30 = EnumParticleTypes.BLOCK_CRACK;
                     var33 = (this.posX + target.posX) / (double)2.0F;
                     var36 = this.posY + 0.3;
                     var39 = (this.posZ + target.posZ) / (double)2.0F;
                     double var10006 = dist * (double)0.25F;
                     double var10008 = dist * (double)0.25F;
                     var42 = new int[1];
                     var45 = Blocks.SAND;
                     var42[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                     var27.spawnParticle(var30, var33, var36, var39, 15, var10006, 0.2, var10008, 0.05, var42);
                  }

               } else if (this.sandSurgeCD <= 0 && dist >= (double)3.0F && dist <= (double)8.0F && this.rand.nextFloat() < 0.3F) {
                  int[] cd = this.getCooldownRange((int)(50.0F * cdMul), (int)(80.0F * cdMul));
                  this.sandSurgeCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  double dx = target.posX - this.posX;
                  double dz = target.posZ - this.posZ;
                  double d = Math.sqrt(dx * dx + dz * dz);
                  if (d > (double)0.0F) {
                     this.sandSurgeActive = true;
                     this.motionX = dx / d * 1.2;
                     this.motionZ = dz / d * 1.2;
                     this.motionY = 0.15;
                  }

                  this.meleeCooldown = 10;
                  if (this.world instanceof WorldServer) {
                     WorldServer var26 = (WorldServer)this.world;
                     EnumParticleTypes var29 = EnumParticleTypes.BLOCK_CRACK;
                     double var32 = this.posX;
                     double var35 = this.posY + (double)0.5F;
                     double var38 = this.posZ;
                     int[] var41 = new int[1];
                     BlockSand var44 = Blocks.SAND;
                     var41[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                     var26.spawnParticle(var29, var32, var35, var38, 15, (double)0.5F, 0.3, (double)0.5F, 0.1, var41);
                  }

               } else if (this.sandFistCD <= 0 && this.meleeCooldown <= 0 && dist <= (double)3.0F && this.rand.nextFloat() < 0.25F) {
                  int[] cd = this.getCooldownRange((int)(80.0F * cdMul), (int)(120.0F * cdMul));
                  this.sandFistCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                  float heavyDmg = baseDmg * 1.5F * (float)dmgMul;
                  float trueDmg = this.combatTier >= 4 ? 3.0F : 2.0F;
                  target.attackEntityFrom(DamageSource.causeMobDamage(this), heavyDmg);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  double kbX = target.posX - this.posX;
                  double kbZ = target.posZ - this.posZ;
                  double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
                  if (kbDist > (double)0.0F) {
                     target.motionX += kbX / kbDist * 0.6;
                     target.motionY += (double)0.25F;
                     target.motionZ += kbZ / kbDist * 0.6;
                     if (target instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)target).velocityChanged = true;
                     }
                  }

                  this.swingArm(EnumHand.MAIN_HAND);
                  this.meleeCooldown = 25;
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.2, new int[0]);
                  }

               } else if (this.sandBuryCD <= 0 && this.meleeCooldown <= 0 && dist <= (double)2.5F && this.rand.nextFloat() < 0.3F) {
                  int[] cd = this.getCooldownRange((int)(40.0F * cdMul), (int)(60.0F * cdMul));
                  this.sandBuryCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.sandComboStep = 1;
                  this.sandComboDelay = 0;
                  this.sandComboTarget = target;
                  this.performSandComboHit(target, dmgMul, 1, 3);
               } else if (this.sandGrindCD <= 0 && this.meleeCooldown <= 0 && dist <= (double)2.5F && this.rand.nextFloat() < 0.25F) {
                  int[] cd = this.getCooldownRange((int)(50.0F * cdMul), (int)(70.0F * cdMul));
                  this.sandGrindCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.sandComboStep = 1;
                  this.sandComboDelay = 0;
                  this.sandComboTarget = target;
                  this.performSandComboHit(target, dmgMul, 1, 4);
               } else {
                  this.getNavigator().tryMoveToEntityLiving(target, 1.2);
                  if (dist <= (double)2.5F && this.meleeCooldown <= 0) {
                     this.performMeleeSwing(target);
                  }

               }
            }
         }
      }

      protected void tickStyleCooldowns() {
         if (this.sandSlideCD > 0) {
            --this.sandSlideCD;
         }

         if (this.sandSurgeCD > 0) {
            --this.sandSurgeCD;
         }

         if (this.sandBuryCD > 0) {
            --this.sandBuryCD;
         }

         if (this.sandGrindCD > 0) {
            --this.sandGrindCD;
         }

         if (this.sandFistCD > 0) {
            --this.sandFistCD;
         }

         if (this.sandWaveCD > 0) {
            --this.sandWaveCD;
         }

         if (this.sandComboDelay > 0) {
            --this.sandComboDelay;
         }

      }

      protected void resetCombatState() {
         this.sandPhase2 = false;
         this.sandIntroPlayed = false;
         this.sandSlideCD = 0;
         this.sandSurgeCD = 0;
         this.sandBuryCD = 0;
         this.sandGrindCD = 0;
         this.sandFistCD = 0;
         this.sandWaveCD = 0;
         this.sandSlideActive = false;
         this.sandComboStep = 0;
         this.sandComboTarget = null;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("sandPhase2", this.sandPhase2);
         compound.setBoolean("sandIntroPlayed", this.sandIntroPlayed);
         compound.setInteger("sandSlideCD", this.sandSlideCD);
         compound.setInteger("sandSurgeCD", this.sandSurgeCD);
         compound.setInteger("sandBuryCD", this.sandBuryCD);
         compound.setInteger("sandGrindCD", this.sandGrindCD);
         compound.setInteger("sandFistCD", this.sandFistCD);
         compound.setInteger("sandWaveCD", this.sandWaveCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.sandPhase2 = compound.getBoolean("sandPhase2");
         this.sandIntroPlayed = compound.getBoolean("sandIntroPlayed");
         this.sandSlideCD = compound.hasKey("sandSlideCD") ? compound.getInteger("sandSlideCD") : 0;
         this.sandSurgeCD = compound.hasKey("sandSurgeCD") ? compound.getInteger("sandSurgeCD") : 0;
         this.sandBuryCD = compound.hasKey("sandBuryCD") ? compound.getInteger("sandBuryCD") : 0;
         this.sandGrindCD = compound.hasKey("sandGrindCD") ? compound.getInteger("sandGrindCD") : 0;
         this.sandFistCD = compound.hasKey("sandFistCD") ? compound.getInteger("sandFistCD") : 0;
         this.sandWaveCD = compound.hasKey("sandWaveCD") ? compound.getInteger("sandWaveCD") : 0;
      }

      private void processSandSlide(EntityLivingBase target) {
         ++this.sandSlideTicks;
         if (this.sandSlideTicks <= 20 && this.sandSlideTarget != null) {
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > (double)0.0F) {
               double speed = (double)1.5F;
               this.motionX = dx / d * speed;
               this.motionZ = dz / d * speed;
               this.motionY = 0.05;
            }

            if (this.world instanceof WorldServer) {
               WorldServer var10000 = (WorldServer)this.world;
               EnumParticleTypes var10001 = EnumParticleTypes.BLOCK_CRACK;
               double var10002 = this.posX;
               double var10003 = this.posY + 0.2;
               double var10004 = this.posZ;
               int[] var10010 = new int[1];
               BlockSand var10013 = Blocks.SAND;
               var10010[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
               var10000.spawnParticle(var10001, var10002, var10003, var10004, 5, 0.3, 0.1, 0.3, (double)0.0F, var10010);
            }

            if (d <= (double)2.5F) {
               this.sandSlideActive = false;
               if (this.meleeCooldown <= 0) {
                  this.performMeleeSwing(target);
               }
            }

         } else {
            this.sandSlideActive = false;
         }
      }

      private void processSandCombo(EntityLivingBase target) {
         if (this.sandComboDelay <= 0) {
            if (this.sandComboTarget != null && this.sandComboTarget.isEntityAlive()) {
               double dist = (double)this.getDistance(this.sandComboTarget);
               if (dist > (double)4.0F) {
                  this.sandComboStep = 0;
                  this.sandComboTarget = null;
               } else {
                  ++this.sandComboStep;
                  int maxHits = this.sandBuryCD > this.sandGrindCD ? 3 : 4;
                  if (this.sandComboStep <= maxHits) {
                     double dmgMul = this.sandPhase2 ? 1.2 : (double)1.0F;
                     this.performSandComboHit(this.sandComboTarget, dmgMul, this.sandComboStep, maxHits);
                     this.sandComboDelay = 6 + this.rand.nextInt(4);
                  } else {
                     this.sandComboStep = 0;
                     this.sandComboTarget = null;
                  }

               }
            } else {
               this.sandComboStep = 0;
               this.sandComboTarget = null;
            }
         }
      }

      private void performSandComboHit(EntityLivingBase target, double dmgMul, int step, int maxHits) {
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float hitDmg = baseDmg * (float)dmgMul * (step == maxHits ? 1.3F : 0.8F);
         float trueDmg = step == maxHits ? 2.5F : 1.5F;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), hitDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         this.swingArm(step % 2 == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
         this.meleeCooldown = 8;
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 5, 0.3, 0.3, 0.3, 0.1, new int[0]);
         }

      }
   }
}
