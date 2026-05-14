
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
public class EntityHyugaNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 235;

   public EntityHyugaNPC(ElementsInfTsukAddon instance) {
      super(instance, 235);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "hyuganpc"), 235).name("hyuganpc").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, HyugaNpcRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class HyugaNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public HyugaNpcRenderer(RenderManager renderManager) {
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
      private boolean hyugaRotationActive = false;
      private int hyugaRotationTicks = 0;
      private int hyugaRotationCD = 0;
      private int hyuga64PalmsCD = 0;
      private int hyugaAirPalmCD = 0;
      private int hyugaGentleFistCD = 0;
      private int hyugaDashCD = 0;
      private boolean hyugaDashActive = false;
      private int hyugaDashTicks = 0;
      private double hyugaDashVelX = (double)0.0F;
      private double hyugaDashVelZ = (double)0.0F;
      private EntityLivingBase hyugaDashTarget = null;
      private boolean hyugaPhase2 = false;
      private boolean hyugaIntroPlayed = false;
      private int hyugaComboStep = 0;
      private int hyugaComboDelay = 0;
      private EntityLivingBase hyugaComboTarget = null;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processHyugaCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.hyugaRotationCD > 0) {
            --this.hyugaRotationCD;
         }

         if (this.hyuga64PalmsCD > 0) {
            --this.hyuga64PalmsCD;
         }

         if (this.hyugaAirPalmCD > 0) {
            --this.hyugaAirPalmCD;
         }

         if (this.hyugaGentleFistCD > 0) {
            --this.hyugaGentleFistCD;
         }

         if (this.hyugaDashCD > 0) {
            --this.hyugaDashCD;
         }

      }

      protected void resetCombatState() {
         this.hyugaRotationActive = false;
         this.hyugaRotationTicks = 0;
         this.hyugaDashActive = false;
         this.hyugaDashTarget = null;
         this.hyugaPhase2 = false;
         this.hyugaIntroPlayed = false;
         this.hyugaComboStep = 0;
         this.hyugaComboTarget = null;
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         Entity trueSource = source.getTrueSource();
         if (!this.world.isRemote && this.hyugaRotationCD <= 0 && !this.hyugaRotationActive && trueSource instanceof EntityLivingBase && (double)this.getDistance(trueSource) < (double)4.0F) {
            float rotChance = this.hyugaPhase2 ? 0.5F : 0.4F;
            if (this.rand.nextFloat() < rotChance) {
               this.hyugaRotationActive = true;
               this.hyugaRotationTicks = 30;
               float cdMul = this.hyugaPhase2 ? 0.7F : 1.0F;
               int[] cd = this.getCooldownRange((int)(140.0F * cdMul), (int)(200.0F * cdMul));
               this.hyugaRotationCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
               this.hyugaSay("§bEight Trigrams: Rotation!");
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)1.5F, (double)0.5F, (double)1.5F, 0.1, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.5F, 0.7F);
            }
         }

         if (!this.world.isRemote && this.hyugaRotationActive && trueSource instanceof EntityLivingBase) {
            float reflectPct = this.hyugaPhase2 ? 0.3F : 0.2F;
            amount *= 0.4F;
            EntityLivingBase attacker = (EntityLivingBase)trueSource;
            float reflectDmg = Math.min(amount * reflectPct, 8.0F);
            if (attacker.getHealth() > reflectDmg + 2.0F) {
               attacker.hurtResistantTime = 0;
               attacker.attackEntityFrom(DamageSource.MAGIC, reflectDmg);
            }
         }

         if (!this.world.isRemote && !this.hyugaPhase2 && this.getHealth() / this.getMaxHealth() < 0.3F) {
            this.hyugaPhase2 = true;
            this.hyugaSay("§bI will prove that fate cannot be changed...");
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.5F, this.posZ, 40, (double)1.5F, (double)1.5F, (double)1.5F, 0.3, new int[0]);
            }
         }

         return amount;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("hyugaPhase2", this.hyugaPhase2);
         compound.setInteger("hyugaRotationCD", this.hyugaRotationCD);
         compound.setInteger("hyuga64PalmsCD", this.hyuga64PalmsCD);
         compound.setInteger("hyugaAirPalmCD", this.hyugaAirPalmCD);
         compound.setInteger("hyugaGentleFistCD", this.hyugaGentleFistCD);
         compound.setBoolean("hyugaIntroPlayed", this.hyugaIntroPlayed);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.hyugaPhase2 = compound.getBoolean("hyugaPhase2");
         this.hyugaRotationCD = compound.hasKey("hyugaRotationCD") ? compound.getInteger("hyugaRotationCD") : 0;
         this.hyuga64PalmsCD = compound.hasKey("hyuga64PalmsCD") ? compound.getInteger("hyuga64PalmsCD") : 0;
         this.hyugaAirPalmCD = compound.hasKey("hyugaAirPalmCD") ? compound.getInteger("hyugaAirPalmCD") : 0;
         this.hyugaGentleFistCD = compound.hasKey("hyugaGentleFistCD") ? compound.getInteger("hyugaGentleFistCD") : 0;
         this.hyugaIntroPlayed = compound.getBoolean("hyugaIntroPlayed");
      }

      private void processHyugaCombat(EntityLivingBase target, double dist) {
         if (target != null && target.isEntityAlive()) {
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            if (!this.hyugaIntroPlayed) {
               this.hyugaIntroPlayed = true;
               this.hyugaSay("§bYou cannot escape your fate. The Byakugan sees all.");
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;

                  for(int i = 0; i < 20; ++i) {
                     double angle = (double)i / (double)20.0F * Math.PI * (double)4.0F;
                     double r = 0.3 + (double)i / (double)20.0F * (double)1.5F;
                     double px = this.posX + Math.cos(angle) * r;
                     double pz = this.posZ + Math.sin(angle) * r;
                     ws.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, px, this.posY + (double)0.5F + (double)i / (double)20.0F * (double)1.5F, pz, 1, 0.05, 0.05, 0.05, 0.01, new int[0]);
                  }
               }
            }

            if (dist > (double)4.0F) {
               double moveSpeed = this.hyugaPhase2 ? 1.2 : (double)1.0F;
               this.getNavigator().tryMoveToEntityLiving(target, moveSpeed);
            }

            if (this.hyugaDashActive) {
               --this.hyugaDashTicks;
               this.motionX = this.hyugaDashVelX;
               this.motionZ = this.hyugaDashVelZ;
               this.motionY = (double)0.0F;
               this.velocityChanged = true;
               if (this.world instanceof WorldServer && this.ticksExisted % 2 == 0) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.posX, this.posY + (double)1.0F, this.posZ, 1, 0.2, 0.1, 0.2, (double)0.0F, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)0.5F, this.posZ, 3, 0.3, 0.3, 0.3, 0.01, new int[0]);
               }

               if (this.hyugaDashTicks <= 0 || this.hyugaDashTarget != null && (double)this.getDistance(this.hyugaDashTarget) < (double)2.0F) {
                  this.hyugaDashActive = false;
                  if (this.hyugaDashTarget != null && (double)this.getDistance(this.hyugaDashTarget) < (double)3.5F) {
                     double dmgMulDash = this.getDamageMultiplier();
                     this.hyugaDashTarget.hurtResistantTime = 0;
                     this.hyugaDashTarget.attackEntityFrom(DamageSource.causeMobDamage(this), 4.0F * (float)dmgMulDash);
                     this.hyugaDashTarget.hurtResistantTime = 0;
                     this.hyugaDashTarget.attackEntityFrom(DamageSource.MAGIC, 3.0F);
                     this.swingArm(EnumHand.MAIN_HAND);
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 1.0F, 1.2F);
                  }

                  this.hyugaDashTarget = null;
               }

            } else if (this.hyugaRotationActive) {
               --this.hyugaRotationTicks;
               if (this.hyugaRotationTicks <= 0) {
                  this.hyugaRotationActive = false;
               } else {
                  float pushRadius = this.hyugaPhase2 ? 4.0F : 3.0F;
                  if (this.ticksExisted % 5 == 0) {
                     for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)pushRadius))) {
                        if (e != this && !(e instanceof EntityCustom)) {
                           e.hurtResistantTime = 0;
                           e.attackEntityFrom(DamageSource.MAGIC, 2.0F);
                           double kbx = e.posX - this.posX;
                           double kbz = e.posZ - this.posZ;
                           double kblen = Math.sqrt(kbx * kbx + kbz * kbz);
                           if (kblen > 0.1) {
                              e.motionX += kbx / kblen * 0.8;
                              e.motionY += 0.2;
                              e.motionZ += kbz / kblen * 0.8;
                              e.velocityChanged = true;
                           }
                        }
                     }
                  }

                  if (this.world instanceof WorldServer && this.ticksExisted % 2 == 0) {
                     double angle = (double)this.ticksExisted * (double)0.5F % (Math.PI * 2D);
                     double px = this.posX + Math.cos(angle) * (double)2.0F;
                     double pz = this.posZ + Math.sin(angle) * (double)2.0F;
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, px, this.posY + (double)1.0F, pz, 2, 0.2, 0.2, 0.2, 0.01, new int[0]);
                  }
               }

            } else if (this.hyugaComboStep > 0) {
               --this.hyugaComboDelay;
               if (this.hyugaComboDelay <= 0) {
                  this.processHyuga64PalmsHit();
               }

            } else {
               double dmgMul = this.getDamageMultiplier();
               float cdMul = this.hyugaPhase2 ? 0.7F : 1.0F;
               if (this.hyugaDashCD <= 0 && dist >= (double)4.0F && dist <= (double)15.0F && this.rand.nextFloat() < 0.25F) {
                  float dashCdMul = this.hyugaPhase2 ? 0.65F : 1.0F;
                  int[] cd = this.getCooldownRange((int)(60.0F * dashCdMul), (int)(100.0F * dashCdMul));
                  this.hyugaDashCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.hyugaDashActive = true;
                  this.hyugaDashTicks = (int)Math.min((double)12.0F, dist / 1.2);
                  this.hyugaDashTarget = target;
                  double dx = target.posX - this.posX;
                  double dz = target.posZ - this.posZ;
                  double horizDist = Math.sqrt(dx * dx + dz * dz);
                  if (horizDist > (double)0.5F) {
                     double speed = horizDist / (double)Math.max(this.hyugaDashTicks, 1);
                     this.hyugaDashVelX = dx / horizDist * speed;
                     this.hyugaDashVelZ = dz / horizDist * speed;
                  }

                  this.getNavigator().clearPath();
                  this.hyugaSay("§bYou're within range!");
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.3F);
               } else if (this.hyuga64PalmsCD <= 0 && dist <= (double)2.5F && this.rand.nextFloat() < 0.15F) {
                  int[] cd = this.getCooldownRange((int)(180.0F * cdMul), (int)(260.0F * cdMul));
                  this.hyuga64PalmsCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  this.hyugaComboStep = 1;
                  this.hyugaComboDelay = 5;
                  this.hyugaComboTarget = target;
                  this.hyugaSay("§bYou are within my field of divination... Eight Trigrams: 64 Palms!");
               } else if (this.hyugaAirPalmCD <= 0 && dist >= (double)8.0F && dist <= (double)15.0F && this.rand.nextFloat() < 0.2F) {
                  int[] cd = this.getCooldownRange((int)(80.0F * cdMul), (int)(130.0F * cdMul));
                  this.hyugaAirPalmCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F * (float)dmgMul);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.MAGIC, 2.0F);
                  double kbx = target.posX - this.posX;
                  double kbz = target.posZ - this.posZ;
                  double kblen = Math.sqrt(kbx * kbx + kbz * kbz);
                  if (kblen > 0.1) {
                     target.motionX += kbx / kblen * (double)1.5F;
                     target.motionY += (double)0.25F;
                     target.motionZ += kbz / kblen * (double)1.5F;
                     target.velocityChanged = true;
                  }

                  if (this.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)this.world;

                     for(int i = 0; i < 10; ++i) {
                        double t = (double)i / (double)10.0F;
                        double px = this.posX + (target.posX - this.posX) * t;
                        double py = this.posY + (double)1.0F + (target.posY + (double)target.getEyeHeight() - this.posY - (double)1.0F) * t;
                        double pz = this.posZ + (target.posZ - this.posZ) * t;
                        ws.spawnParticle(EnumParticleTypes.CLOUD, px, py, pz, 2, 0.1, 0.1, 0.1, 0.01, new int[0]);
                        ws.spawnParticle(EnumParticleTypes.CRIT, px, py, pz, 1, 0.05, 0.05, 0.05, 0.01, new int[0]);
                     }
                  }

                  this.hyugaSay("§bAir Palm!");
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.0F, 0.8F);
                  this.swingArm(EnumHand.MAIN_HAND);
               } else if (this.hyugaGentleFistCD <= 0 && dist <= (double)2.5F && this.meleeCooldown <= 0) {
                  int[] cd = this.getCooldownRange((int)(30.0F * cdMul), (int)(50.0F * cdMul));
                  this.hyugaGentleFistCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.causeMobDamage(this), 2.0F * (float)dmgMul);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.MAGIC, 4.0F);
                  target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 40, 0));
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, target.posX, target.posY + (double)target.height * (double)0.5F, target.posZ, 5, 0.2, 0.3, 0.2, 0.05, new int[0]);
                  }

                  this.swingArm(EnumHand.MAIN_HAND);
                  this.meleeCooldown = 15;
               } else {
                  if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
                     this.performMeleeSwing(target);
                  }

               }
            }
         }
      }

      private void processHyuga64PalmsHit() {
         if (this.hyugaComboTarget != null && this.hyugaComboTarget.isEntityAlive() && !(this.getDistanceSq(this.hyugaComboTarget) > (double)16.0F)) {
            double dmgMul = this.getDamageMultiplier();
            boolean isFinalHit = this.hyugaComboStep >= 8;
            if (isFinalHit) {
               this.hyugaComboTarget.hurtResistantTime = 0;
               this.hyugaComboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), 6.0F * (float)dmgMul);
               this.hyugaComboTarget.hurtResistantTime = 0;
               this.hyugaComboTarget.attackEntityFrom(DamageSource.MAGIC, 4.0F);
               this.hyugaComboTarget.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 2));
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.hyugaComboTarget.posX, this.hyugaComboTarget.posY + (double)1.0F, this.hyugaComboTarget.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.2, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.HOSTILE, 1.2F, 0.6F);
               this.hyugaComboStep = 0;
               this.hyugaComboTarget = null;
            } else {
               this.hyugaComboTarget.hurtResistantTime = 0;
               this.hyugaComboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F * (float)dmgMul);
               this.hyugaComboTarget.hurtResistantTime = 0;
               this.hyugaComboTarget.attackEntityFrom(DamageSource.MAGIC, 2.0F);
               int weakLevel = Math.min(this.hyugaComboStep - 1, 3);
               this.hyugaComboTarget.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, weakLevel));
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.hyugaComboTarget.posX, this.hyugaComboTarget.posY + (double)1.0F, this.hyugaComboTarget.posZ, 5, 0.2, 0.3, 0.2, 0.05, new int[0]);
               }

               ++this.hyugaComboStep;
               this.hyugaComboDelay = 5;
            }

            this.swingArm(EnumHand.MAIN_HAND);
         } else {
            this.hyugaComboStep = 0;
            this.hyugaComboTarget = null;
         }
      }

      private void hyugaSay(String msg) {
         if (this.chatCooldown <= 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
               p.sendMessage(new TextComponentString("§3" + this.getCustomNameTag() + ": §b" + msg));
            }

            this.chatCooldown = 80;
         }
      }
   }
}
