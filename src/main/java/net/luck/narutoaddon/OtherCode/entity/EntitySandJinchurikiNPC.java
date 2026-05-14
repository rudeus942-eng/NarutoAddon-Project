
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
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
public class EntitySandJinchurikiNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 238;

   public EntitySandJinchurikiNPC(ElementsInfTsukAddon instance) {
      super(instance, 238);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "sandjinchuriki"), 238).name("sandjinchuriki").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SandJinchNpcRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class SandJinchNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public SandJinchNpcRenderer(RenderManager renderManager) {
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
      private int jinchPhase = 1;
      private boolean jinchShieldActive = true;
      private int jinchSandBulletCD = 0;
      private int jinchSandBindCD = 0;
      private int jinchSandCoffinCD = 0;
      private int jinchSandWaveCD = 0;
      private int jinchArmCD = 0;
      private int jinchRoarCD = 0;
      private int jinchClawCD = 0;
      private int jinchDashCD = 0;
      private boolean jinchIntroPlayed = false;
      private boolean jinchCoffinGrabbing = false;
      private int jinchCoffinTicks = 0;
      private EntityLivingBase jinchCoffinTarget = null;
      private int jinchClawComboStep = 0;
      private int jinchClawComboDelay = 0;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (target != null && target.isEntityAlive()) {
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            if (this.jinchClawComboStep > 0) {
               this.processSandClawCombo(target);
            } else if (this.meleeCooldown <= 0) {
               if (!this.jinchIntroPlayed && this.chatCooldown <= 0) {
                  this.jinchIntroPlayed = true;
                  this.chatCooldown = 200;
               }

               float hpPct = this.getHealth() / this.getMaxHealth();
               if (this.jinchPhase == 1 && hpPct <= 0.4F) {
                  this.jinchPhase = 2;
                  this.jinchShieldActive = false;
                  if (target instanceof EntityPlayerMP) {
                     SPacketTitle titlePkt = new SPacketTitle(Type.TITLE, new TextComponentString("§6Gaara: Partial Transformation"));
                     SPacketTitle subPkt = new SPacketTitle(Type.SUBTITLE, new TextComponentString("§eThe sand is going wild!"));
                     ((EntityPlayerMP)target).connection.sendPacket(titlePkt);
                     ((EntityPlayerMP)target).connection.sendPacket(subPkt);
                  }

                  IAttributeInstance speedAttr = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                  if (speedAttr != null) {
                     speedAttr.applyModifier(new AttributeModifier(UUID.fromString("a1234567-b890-c123-d456-e78901234567"), "jinch_phase2_speed", 0.3, 2));
                  }

                  IAttributeInstance dmgAttr = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
                  if (dmgAttr != null) {
                     dmgAttr.applyModifier(new AttributeModifier(UUID.fromString("b1234567-c890-d123-e456-f78901234567"), "jinch_phase2_damage", (double)0.5F, 2));
                  }

                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, this.posX, this.posY + (double)1.5F, this.posZ, 25, (double)1.0F, (double)1.0F, (double)1.0F, 0.1, new int[0]);
                     WorldServer var10000 = (WorldServer)this.world;
                     EnumParticleTypes var10001 = EnumParticleTypes.BLOCK_CRACK;
                     double var10002 = this.posX;
                     double var10003 = this.posY + (double)1.0F;
                     double var10004 = this.posZ;
                     int[] var10010 = new int[1];
                     BlockSand var10013 = Blocks.SAND;
                     var10010[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                     var10000.spawnParticle(var10001, var10002, var10003, var10004, 40, (double)1.5F, (double)1.0F, (double)1.5F, 0.2, var10010);
                  }
               }

               if (this.jinchPhase == 2 && hpPct <= 0.15F) {
                  this.jinchPhase = 3;
                  if (target instanceof EntityPlayerMP) {
                     SPacketTitle titlePkt = new SPacketTitle(Type.TITLE, new TextComponentString("§4§lShukaku Frenzy!"));
                     SPacketTitle subPkt = new SPacketTitle(Type.SUBTITLE, new TextComponentString("§cGaara has lost control!"));
                     ((EntityPlayerMP)target).connection.sendPacket(titlePkt);
                     ((EntityPlayerMP)target).connection.sendPacket(subPkt);
                  }

                  IAttributeInstance speedAttr3 = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
                  if (speedAttr3 != null) {
                     speedAttr3.applyModifier(new AttributeModifier(UUID.fromString("c1234567-d890-e123-f456-a78901234568"), "jinch_phase3_speed", 0.2, 2));
                  }

                  IAttributeInstance dmgAttr3 = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
                  if (dmgAttr3 != null) {
                     dmgAttr3.applyModifier(new AttributeModifier(UUID.fromString("d1234567-e890-f123-a456-b78901234568"), "jinch_phase3_damage", 0.3, 2));
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 2.5F, 0.4F);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 5, (double)2.0F, (double)1.0F, (double)2.0F, (double)0.0F, new int[0]);
                     WorldServer var56 = (WorldServer)this.world;
                     EnumParticleTypes var65 = EnumParticleTypes.BLOCK_CRACK;
                     double var74 = this.posX;
                     double var82 = this.posY + (double)1.0F;
                     double var91 = this.posZ;
                     int[] var99 = new int[1];
                     BlockSand var108 = Blocks.SAND;
                     var99[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                     var56.spawnParticle(var65, var74, var82, var91, 60, (double)3.0F, (double)2.0F, (double)3.0F, 0.3, var99);
                  }
               }

               if (this.world instanceof WorldServer && this.ticksExisted % 3 == 0) {
                  int particleCount = this.jinchPhase >= 3 ? 8 : (this.jinchPhase == 2 ? 5 : 2);
                  double radius = this.jinchPhase >= 3 ? (double)2.0F : (this.jinchPhase == 2 ? (double)1.5F : 0.8);
                  WorldServer var57 = (WorldServer)this.world;
                  EnumParticleTypes var66 = EnumParticleTypes.BLOCK_CRACK;
                  double var75 = this.posX + (this.rand.nextDouble() - (double)0.5F) * radius * (double)2.0F;
                  double var83 = this.posY + this.rand.nextDouble() * (double)2.0F;
                  double var92 = this.posZ + (this.rand.nextDouble() - (double)0.5F) * radius * (double)2.0F;
                  double var10006 = radius * 0.3;
                  double var10008 = radius * 0.3;
                  int[] var100 = new int[1];
                  BlockSand var109 = Blocks.SAND;
                  var100[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                  var57.spawnParticle(var66, var75, var83, var92, particleCount, var10006, (double)0.5F, var10008, (double)0.0F, var100);
               }

               if (this.jinchCoffinGrabbing) {
                  this.processSandCoffin(target);
               } else {
                  if (this.jinchPhase >= 3) {
                     float baseDmg3 = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                     if (this.jinchDashCD <= 0 && dist >= (double)4.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.3F) {
                        this.jinchDashCD = 80 + this.rand.nextInt(41);
                        double dx = target.posX - this.posX;
                        double dz = target.posZ - this.posZ;
                        double d = Math.sqrt(dx * dx + dz * dz);
                        if (d > (double)0.0F) {
                           this.motionX = dx / d * 2.2;
                           this.motionZ = dz / d * 2.2;
                           this.motionY = (double)0.25F;
                        }

                        for(Entity e : this.world.getEntitiesWithinAABB(EntityPlayer.class, target.getEntityBoundingBox().grow((double)5.0F))) {
                           e.attackEntityFrom(DamageSource.causeMobDamage(this), baseDmg3 * 0.8F);
                           ((EntityLivingBase)e).hurtResistantTime = 0;
                           e.attackEntityFrom(DamageSource.MAGIC, 5.0F);
                        }

                        if (this.world instanceof WorldServer) {
                           WorldServer var64 = (WorldServer)this.world;
                           EnumParticleTypes var73 = EnumParticleTypes.BLOCK_CRACK;
                           double var81 = target.posX;
                           double var90 = target.posY + (double)0.5F;
                           double var98 = target.posZ;
                           int[] var107 = new int[1];
                           BlockSand var116 = Blocks.SAND;
                           var107[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                           var64.spawnParticle(var73, var81, var90, var98, 30, (double)2.5F, (double)0.5F, (double)2.5F, 0.2, var107);
                        }

                        if (this.rand.nextFloat() < 0.6F) {
                           this.jinchClawCD = 30;
                           this.jinchClawComboStep = 1;
                           this.jinchClawComboDelay = 5;
                        }

                        return;
                     }

                     if (this.jinchRoarCD <= 0 && dist <= (double)15.0F && this.rand.nextFloat() < 0.3F) {
                        this.jinchRoarCD = 60 + this.rand.nextInt(41);

                        for(Entity e : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)15.0F))) {
                           e.attackEntityFrom(DamageSource.causeMobDamage(this), baseDmg3 * 1.2F);
                           ((EntityLivingBase)e).hurtResistantTime = 0;
                           e.attackEntityFrom(DamageSource.MAGIC, 7.0F);
                           ((EntityLivingBase)e).addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 80, 0));
                           ((EntityLivingBase)e).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 2));
                           double kx = e.posX - this.posX;
                           double kz = e.posZ - this.posZ;
                           double kd = Math.sqrt(kx * kx + kz * kz);
                           if (kd > (double)0.0F) {
                              e.motionX += kx / kd * (double)2.0F;
                              e.motionY += 0.6;
                              e.motionZ += kz / kd * (double)2.0F;
                              e.velocityChanged = true;
                           }
                        }

                        this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 3.0F, 0.4F);
                        if (this.world instanceof WorldServer) {
                           ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 12, (double)5.0F, (double)1.5F, (double)5.0F, (double)0.0F, new int[0]);
                        }

                        return;
                     }

                     if (this.jinchArmCD <= 0 && this.rand.nextFloat() < 0.25F) {
                        this.jinchArmCD = 100 + this.rand.nextInt(41);

                        for(Entity e : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)12.0F))) {
                           e.attackEntityFrom(DamageSource.causeMobDamage(this), baseDmg3 * 0.6F);
                           ((EntityLivingBase)e).hurtResistantTime = 0;
                           e.attackEntityFrom(DamageSource.MAGIC, 4.0F);
                           ((EntityLivingBase)e).addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 40, 0));
                        }

                        if (this.world instanceof WorldServer) {
                           WorldServer var63 = (WorldServer)this.world;
                           EnumParticleTypes var72 = EnumParticleTypes.BLOCK_CRACK;
                           double var80 = this.posX;
                           double var89 = this.posY + (double)1.0F;
                           double var97 = this.posZ;
                           int[] var106 = new int[1];
                           BlockSand var115 = Blocks.SAND;
                           var106[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                           var63.spawnParticle(var72, var80, var89, var97, 80, (double)6.0F, (double)2.0F, (double)6.0F, 0.3, var106);
                        }

                        return;
                     }
                  }

                  if (this.jinchPhase >= 2) {
                     if (this.jinchRoarCD <= 0 && dist <= (double)15.0F && this.rand.nextFloat() < 0.25F) {
                        this.jinchRoarCD = 100 + this.rand.nextInt(51);
                        float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();

                        for(Entity e : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)15.0F))) {
                           if (e instanceof EntityPlayer) {
                              e.attackEntityFrom(DamageSource.causeMobDamage(this), baseDmg * 1.0F);
                              ((EntityLivingBase)e).hurtResistantTime = 0;
                              e.attackEntityFrom(DamageSource.MAGIC, 5.0F);
                              ((EntityLivingBase)e).addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 60, 0));
                              double kx = e.posX - this.posX;
                              double kz = e.posZ - this.posZ;
                              double kd = Math.sqrt(kx * kx + kz * kz);
                              if (kd > (double)0.0F) {
                                 e.motionX += kx / kd * (double)1.5F;
                                 e.motionY += (double)0.5F;
                                 e.motionZ += kz / kd * (double)1.5F;
                                 if (e instanceof EntityPlayerMP) {
                                    ((EntityPlayerMP)e).velocityChanged = true;
                                 }
                              }
                           }
                        }

                        this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 2.0F, 0.6F);
                        if (this.world instanceof WorldServer) {
                           ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 8, (double)5.0F, (double)1.0F, (double)5.0F, (double)0.0F, new int[0]);
                        }

                        return;
                     }

                     if (this.jinchArmCD <= 0 && dist >= (double)4.0F && dist <= (double)10.0F && this.rand.nextFloat() < 0.35F) {
                        this.jinchArmCD = 50 + this.rand.nextInt(31);
                        float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                        target.attackEntityFrom(DamageSource.causeMobDamage(this), baseDmg * 1.2F);
                        target.hurtResistantTime = 0;
                        target.attackEntityFrom(DamageSource.MAGIC, 5.0F);
                        double kx = target.posX - this.posX;
                        double kz = target.posZ - this.posZ;
                        double kd = Math.sqrt(kx * kx + kz * kz);
                        if (kd > (double)0.0F) {
                           target.motionX += kx / kd * (double)1.0F;
                           target.motionY += 0.4;
                           target.motionZ += kz / kd * (double)1.0F;
                           if (target instanceof EntityPlayerMP) {
                              ((EntityPlayerMP)target).velocityChanged = true;
                           }
                        }

                        this.swingArm(EnumHand.MAIN_HAND);
                        return;
                     }

                     if (this.jinchClawCD <= 0 && dist <= (double)3.0F && this.rand.nextFloat() < 0.3F) {
                        this.jinchClawCD = 60 + this.rand.nextInt(31);
                        this.jinchClawComboStep = 1;
                        this.jinchClawComboDelay = 0;
                        this.performJinchClawHit(target, 1);
                        return;
                     }

                     if (this.jinchDashCD <= 0 && dist >= (double)5.0F && dist <= (double)15.0F && this.rand.nextFloat() < 0.3F) {
                        this.jinchDashCD = 40 + this.rand.nextInt(21);
                        double dx = target.posX - this.posX;
                        double dz = target.posZ - this.posZ;
                        double d = Math.sqrt(dx * dx + dz * dz);
                        if (d > (double)0.0F) {
                           this.motionX = dx / d * 1.8;
                           this.motionZ = dz / d * 1.8;
                           this.motionY = 0.2;
                        }

                        if (this.world instanceof WorldServer) {
                           WorldServer var62 = (WorldServer)this.world;
                           EnumParticleTypes var71 = EnumParticleTypes.BLOCK_CRACK;
                           double var79 = this.posX;
                           double var88 = this.posY + (double)0.5F;
                           double var96 = this.posZ;
                           int[] var105 = new int[1];
                           BlockSand var114 = Blocks.SAND;
                           var105[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                           var62.spawnParticle(var71, var79, var88, var96, 20, (double)1.5F, (double)0.5F, (double)1.5F, 0.2, var105);
                        }

                        if (this.rand.nextFloat() < 0.5F) {
                           this.jinchClawCD = 20;
                           this.jinchClawComboStep = 1;
                           this.jinchClawComboDelay = 8;
                        }

                        return;
                     }
                  }

                  if (this.jinchSandCoffinCD <= 0 && dist >= (double)4.0F && dist <= (double)8.0F && this.rand.nextFloat() < 0.2F) {
                     this.jinchSandCoffinCD = 120 + this.rand.nextInt(61);
                     this.jinchCoffinGrabbing = true;
                     this.jinchCoffinTicks = 40;
                     this.jinchCoffinTarget = target;
                     target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 50, 4));
                     target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 50, 2));
                     if (this.world instanceof WorldServer) {
                        WorldServer var61 = (WorldServer)this.world;
                        EnumParticleTypes var70 = EnumParticleTypes.BLOCK_CRACK;
                        double var78 = target.posX;
                        double var87 = target.posY + (double)0.5F;
                        double var95 = target.posZ;
                        int[] var104 = new int[1];
                        BlockSand var113 = Blocks.SAND;
                        var104[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                        var61.spawnParticle(var70, var78, var87, var95, 25, (double)0.5F, (double)1.0F, (double)0.5F, 0.1, var104);
                     }

                  } else if (this.jinchSandBulletCD <= 0 && dist >= (double)10.0F && dist <= (double)25.0F && this.rand.nextFloat() < 0.35F) {
                     this.jinchSandBulletCD = 40 + this.rand.nextInt(21);
                     target.attackEntityFrom(DamageSource.causeMobDamage(this), 10.0F);
                     target.hurtResistantTime = 0;
                     target.attackEntityFrom(DamageSource.MAGIC, 2.0F);
                     this.swingArm(EnumHand.MAIN_HAND);
                     this.meleeCooldown = 15;
                     if (this.world instanceof WorldServer) {
                        WorldServer var60 = (WorldServer)this.world;
                        EnumParticleTypes var69 = EnumParticleTypes.BLOCK_CRACK;
                        double var77 = target.posX;
                        double var86 = target.posY + (double)0.5F;
                        double var94 = target.posZ;
                        int[] var103 = new int[1];
                        BlockSand var112 = Blocks.SAND;
                        var103[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                        var60.spawnParticle(var69, var77, var86, var94, 20, 0.8, (double)0.5F, 0.8, 0.15, var103);
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 8, 0.3, 0.3, 0.3, 0.2, new int[0]);
                     }

                  } else if (this.jinchSandBindCD <= 0 && dist >= (double)6.0F && dist <= (double)15.0F && this.rand.nextFloat() < 0.25F) {
                     this.jinchSandBindCD = 80 + this.rand.nextInt(41);
                     target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 2));
                     target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0));
                     if (this.world instanceof WorldServer) {
                        WorldServer var59 = (WorldServer)this.world;
                        EnumParticleTypes var68 = EnumParticleTypes.BLOCK_CRACK;
                        double var76 = target.posX;
                        double var85 = target.posY;
                        double var93 = target.posZ;
                        int[] var102 = new int[1];
                        BlockSand var111 = Blocks.SAND;
                        var102[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                        var59.spawnParticle(var68, var76, var85, var93, 20, 0.3, (double)0.5F, 0.3, 0.1, var102);
                     }

                  } else if (this.jinchSandWaveCD <= 0 && dist >= (double)8.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.25F) {
                     this.jinchSandWaveCD = 70 + this.rand.nextInt(31);
                     double dx = target.posX - this.posX;
                     double dz = target.posZ - this.posZ;
                     double d = Math.sqrt(dx * dx + dz * dz);

                     for(int i = 2; i <= (int)Math.min(dist, (double)15.0F); ++i) {
                        double px = this.posX + dx / d * (double)i;
                        double pz = this.posZ + dz / d * (double)i;

                        for(Entity e : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(px - (double)1.5F, this.posY - (double)1.0F, pz - (double)1.5F, px + (double)1.5F, this.posY + (double)3.0F, pz + (double)1.5F))) {
                           e.attackEntityFrom(DamageSource.causeMobDamage(this), 12.0F);
                           ((EntityLivingBase)e).hurtResistantTime = 0;
                           e.attackEntityFrom(DamageSource.MAGIC, 3.0F);
                        }

                        if (this.world instanceof WorldServer) {
                           WorldServer var58 = (WorldServer)this.world;
                           EnumParticleTypes var67 = EnumParticleTypes.BLOCK_CRACK;
                           double var84 = this.posY + (double)0.5F;
                           int[] var101 = new int[1];
                           BlockSand var110 = Blocks.SAND;
                           var101[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                           var58.spawnParticle(var67, px, var84, pz, 5, (double)0.5F, 0.3, (double)0.5F, 0.1, var101);
                        }
                     }

                  } else {
                     float pursuitSpeed = this.jinchPhase >= 3 ? 1.5F : (this.jinchPhase == 2 ? 1.3F : 1.0F);
                     this.getNavigator().tryMoveToEntityLiving(target, (double)pursuitSpeed);
                     if (dist <= (double)2.5F && this.meleeCooldown <= 0) {
                        this.performMeleeSwing(target);
                        if (this.jinchPhase >= 3 && this.rand.nextFloat() < 0.3F && this.jinchClawComboStep == 0) {
                           this.jinchClawComboStep = 1;
                           this.jinchClawComboDelay = 4;
                        }
                     }

                  }
               }
            }
         }
      }

      protected void tickStyleCooldowns() {
         if (this.jinchSandBulletCD > 0) {
            --this.jinchSandBulletCD;
         }

         if (this.jinchSandBindCD > 0) {
            --this.jinchSandBindCD;
         }

         if (this.jinchSandCoffinCD > 0) {
            --this.jinchSandCoffinCD;
         }

         if (this.jinchSandWaveCD > 0) {
            --this.jinchSandWaveCD;
         }

         if (this.jinchArmCD > 0) {
            --this.jinchArmCD;
         }

         if (this.jinchRoarCD > 0) {
            --this.jinchRoarCD;
         }

         if (this.jinchClawCD > 0) {
            --this.jinchClawCD;
         }

         if (this.jinchDashCD > 0) {
            --this.jinchDashCD;
         }

         if (this.jinchClawComboDelay > 0) {
            --this.jinchClawComboDelay;
         }

         if (this.jinchCoffinGrabbing && this.jinchCoffinTicks > 0) {
            --this.jinchCoffinTicks;
         }

      }

      protected void resetCombatState() {
         this.jinchPhase = 1;
         this.jinchShieldActive = true;
         this.jinchIntroPlayed = false;
         this.jinchCoffinGrabbing = false;
         this.jinchCoffinTarget = null;
         this.jinchClawComboStep = 0;
         this.jinchClawComboDelay = 0;
         this.jinchSandBulletCD = 0;
         this.jinchSandBindCD = 0;
         this.jinchSandCoffinCD = 0;
         this.jinchSandWaveCD = 0;
         this.jinchArmCD = 0;
         this.jinchRoarCD = 0;
         this.jinchClawCD = 0;
         this.jinchDashCD = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("jinchPhase", this.jinchPhase);
         compound.setBoolean("jinchShieldActive", this.jinchShieldActive);
         compound.setBoolean("jinchIntroPlayed", this.jinchIntroPlayed);
         compound.setInteger("jinchSandBulletCD", this.jinchSandBulletCD);
         compound.setInteger("jinchSandBindCD", this.jinchSandBindCD);
         compound.setInteger("jinchSandCoffinCD", this.jinchSandCoffinCD);
         compound.setInteger("jinchSandWaveCD", this.jinchSandWaveCD);
         compound.setInteger("jinchArmCD", this.jinchArmCD);
         compound.setInteger("jinchRoarCD", this.jinchRoarCD);
         compound.setInteger("jinchClawCD", this.jinchClawCD);
         compound.setInteger("jinchDashCD", this.jinchDashCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.jinchPhase = compound.hasKey("jinchPhase") ? compound.getInteger("jinchPhase") : 1;
         this.jinchShieldActive = compound.hasKey("jinchShieldActive") ? compound.getBoolean("jinchShieldActive") : true;
         this.jinchIntroPlayed = compound.getBoolean("jinchIntroPlayed");
         this.jinchSandBulletCD = compound.hasKey("jinchSandBulletCD") ? compound.getInteger("jinchSandBulletCD") : 0;
         this.jinchSandBindCD = compound.hasKey("jinchSandBindCD") ? compound.getInteger("jinchSandBindCD") : 0;
         this.jinchSandCoffinCD = compound.hasKey("jinchSandCoffinCD") ? compound.getInteger("jinchSandCoffinCD") : 0;
         this.jinchSandWaveCD = compound.hasKey("jinchSandWaveCD") ? compound.getInteger("jinchSandWaveCD") : 0;
         this.jinchArmCD = compound.hasKey("jinchArmCD") ? compound.getInteger("jinchArmCD") : 0;
         this.jinchRoarCD = compound.hasKey("jinchRoarCD") ? compound.getInteger("jinchRoarCD") : 0;
         this.jinchClawCD = compound.hasKey("jinchClawCD") ? compound.getInteger("jinchClawCD") : 0;
         this.jinchDashCD = compound.hasKey("jinchDashCD") ? compound.getInteger("jinchDashCD") : 0;
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         float drMultiplier = this.jinchPhase == 1 ? 0.75F : (this.jinchPhase == 2 ? 0.85F : 0.9F);
         amount *= drMultiplier;
         if (this.world instanceof WorldServer) {
            WorldServer var10000 = (WorldServer)this.world;
            EnumParticleTypes var10001 = EnumParticleTypes.BLOCK_CRACK;
            double var10002 = this.posX;
            double var10003 = this.posY + (double)1.0F;
            double var10004 = this.posZ;
            int[] var10010 = new int[1];
            BlockSand var10013 = Blocks.SAND;
            var10010[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
            var10000.spawnParticle(var10001, var10002, var10003, var10004, 8, 0.3, 0.3, 0.3, (double)0.0F, var10010);
         }

         if (source.getTrueSource() instanceof EntityPlayer && !source.isMagicDamage() && !source.isProjectile() && this.rand.nextFloat() < 0.3F) {
            EntityLivingBase attacker = (EntityLivingBase)source.getTrueSource();
            double cDist = (double)this.getDistance(attacker);
            if (cDist <= (double)5.0F) {
               float counterDmg = this.jinchPhase >= 2 ? 4.0F : 2.5F;
               attacker.attackEntityFrom(DamageSource.MAGIC, counterDmg);
               double kx = attacker.posX - this.posX;
               double kz = attacker.posZ - this.posZ;
               double kd = Math.sqrt(kx * kx + kz * kz);
               if (kd > (double)0.0F) {
                  attacker.motionX += kx / kd * 0.6;
                  attacker.motionY += 0.2;
                  attacker.motionZ += kz / kd * 0.6;
                  attacker.velocityChanged = true;
               }

               if (this.world instanceof WorldServer) {
                  WorldServer var15 = (WorldServer)this.world;
                  EnumParticleTypes var16 = EnumParticleTypes.BLOCK_CRACK;
                  double var17 = attacker.posX;
                  double var18 = attacker.posY + (double)1.0F;
                  double var19 = attacker.posZ;
                  int[] var20 = new int[1];
                  BlockSand var21 = Blocks.SAND;
                  var20[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                  var15.spawnParticle(var16, var17, var18, var19, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.15, var20);
               }
            }
         }

         return amount;
      }

      private void processSandCoffin(EntityLivingBase target) {
         if (this.jinchCoffinTicks > 0 && this.jinchCoffinTarget != null && this.jinchCoffinTarget.isEntityAlive()) {
            if (this.world instanceof WorldServer && this.ticksExisted % 2 == 0) {
               WorldServer var3 = (WorldServer)this.world;
               EnumParticleTypes var4 = EnumParticleTypes.BLOCK_CRACK;
               double var5 = this.jinchCoffinTarget.posX;
               double var6 = this.jinchCoffinTarget.posY + (double)0.5F;
               double var7 = this.jinchCoffinTarget.posZ;
               int[] var8 = new int[1];
               BlockSand var9 = Blocks.SAND;
               var8[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
               var3.spawnParticle(var4, var5, var6, var7, 5, 0.4, 0.8, 0.4, (double)0.0F, var8);
            }

            this.getNavigator().clearPath();
            this.getLookHelper().setLookPositionWithEntity(this.jinchCoffinTarget, 30.0F, 30.0F);
         } else {
            if (this.jinchCoffinTarget != null && this.jinchCoffinTarget.isEntityAlive()) {
               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               this.jinchCoffinTarget.attackEntityFrom(DamageSource.causeMobDamage(this), baseDmg * 2.0F);
               this.jinchCoffinTarget.hurtResistantTime = 0;
               this.jinchCoffinTarget.attackEntityFrom(DamageSource.MAGIC, 5.0F);
               if (this.world instanceof WorldServer) {
                  WorldServer var10000 = (WorldServer)this.world;
                  EnumParticleTypes var10001 = EnumParticleTypes.BLOCK_CRACK;
                  double var10002 = this.jinchCoffinTarget.posX;
                  double var10003 = this.jinchCoffinTarget.posY + (double)1.0F;
                  double var10004 = this.jinchCoffinTarget.posZ;
                  int[] var10010 = new int[1];
                  BlockSand var10013 = Blocks.SAND;
                  var10010[0] = BlockSand.getStateId(Blocks.SAND.getDefaultState());
                  var10000.spawnParticle(var10001, var10002, var10003, var10004, 30, (double)0.5F, (double)0.5F, (double)0.5F, 0.2, var10010);
               }

               this.world.playSound((EntityPlayer)null, this.jinchCoffinTarget.posX, this.jinchCoffinTarget.posY, this.jinchCoffinTarget.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.8F, 0.7F);
            }

            this.jinchCoffinGrabbing = false;
            this.jinchCoffinTarget = null;
         }
      }

      private void processSandClawCombo(EntityLivingBase target) {
         if (this.jinchClawComboDelay <= 0) {
            if (target != null && target.isEntityAlive() && !((double)this.getDistance(target) > (double)4.0F)) {
               ++this.jinchClawComboStep;
               int maxHits = this.jinchPhase >= 2 ? 5 : 3;
               if (this.jinchClawComboStep <= maxHits) {
                  this.performJinchClawHit(target, this.jinchClawComboStep);
                  this.jinchClawComboDelay = this.jinchPhase >= 3 ? 4 + this.rand.nextInt(2) : 6 + this.rand.nextInt(3);
               } else {
                  this.jinchClawComboStep = 0;
               }

            } else {
               this.jinchClawComboStep = 0;
            }
         }
      }

      private void performJinchClawHit(EntityLivingBase target, int step) {
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float hitDmg = baseDmg * (0.8F + (float)step * 0.3F);
         float trueDmg = 2.0F + (float)step;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), hitDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         this.swingArm(step % 2 == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
         this.meleeCooldown = 8;
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 8, 0.4, 0.4, 0.4, 0.15, new int[0]);
         }

      }
   }
}
