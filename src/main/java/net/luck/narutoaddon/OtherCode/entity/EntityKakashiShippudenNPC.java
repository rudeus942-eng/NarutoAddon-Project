
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.ModelPlayerPoseable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcPose;
import net.minecraft.client.renderer.GlStateManager;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKakashiShippudenNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 261;

   public EntityKakashiShippudenNPC(ElementsInfTsukAddon instance) {
      super(instance, 261);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kakashishippuden"), 261).name("kakashishippuden").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, CustomRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class CustomRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/anko.png");

      public CustomRenderer(RenderManager renderManager) {
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
   }

   public static class EntityCustom extends QuestNpcBase {
      private int kakashiLightningBladeCD = 0;
      private int kakashiSharinganCounterCD = 0;
      private int kakashiKamuiCD = 0;
      private int kakashiRetreatCD = 0;
      private int kakashiMeleeCD = 0;
      private int kakashiComboStep = 0;
      private int kakashiComboDelay = 0;
      private EntityLivingBase kakashiComboTarget = null;
      private boolean kakashiRetreating = false;
      private int kakashiRetreatTicks = 0;
      private boolean kakashiIntroPlayed = false;
      private int kakashiPhase = 1;
      private int kakashiFireballCD = 0;
      private int kakashiFireballBurstRemaining = 0;
      private int kakashiFireballBurstDelay = 0;
      private EntityLivingBase kakashiFireballBurstTarget = null;
      private int kakashiWaterDragonCD = 0;
      private int kakashiLightningCloneCD = 0;
      private static final int KAKASHI_LIGHTNING_BLADE_CD_BASE = 200;
      private static final int KAKASHI_SHARINGAN_COUNTER_CD_BASE = 160;
      private static final int KAKASHI_KAMUI_CD_BASE = 500;
      private static final int KAKASHI_RETREAT_CD_BASE = 160;
      private static final int KAKASHI_FIREBALL_CD_BASE = 120;
      private static final int KAKASHI_WATER_DRAGON_CD_BASE = 200;
      private static final int KAKASHI_LIGHTNING_CLONE_CD_BASE = 300;
      private static final float KAKASHI_PHASE_2 = 0.5F;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         float hpFrac = this.getHealth() / this.getMaxHealth();
         if (this.kakashiPhase == 1 && hpFrac <= 0.5F) {
            this.kakashiPhase = 2;
            this.broadcastChat((double)30.0F, "§7[§cKakashi§7] §fI suppose I should take this seriously...");
            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               double curSpeed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(curSpeed * 1.15);
            }
         }

         if (!this.kakashiIntroPlayed && target instanceof EntityPlayerMP) {
            this.kakashiIntroPlayed = true;
            ((EntityPlayerMP)target).sendMessage(new TextComponentString("§7[§cKakashi§7] §fA thousand jutsu... let me show you a few."));
         }

         if (this.kakashiComboStep > 0 && this.kakashiComboDelay <= 0 && this.kakashiComboTarget != null) {
            this.processKakashiCombo();
         } else {
            if (this.kakashiRetreating) {
               if (this.kakashiRetreatTicks > 0) {
                  double dx = this.posX - target.posX;
                  double dz = this.posZ - target.posZ;
                  double retreatDist = Math.sqrt(dx * dx + dz * dz);
                  if (retreatDist > 0.1) {
                     this.motionX = dx / retreatDist * 0.4;
                     this.motionZ = dz / retreatDist * 0.4;
                     this.velocityChanged = true;
                  }

                  this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
                  if (this.kakashiPhase >= 2 && this.kakashiFireballCD <= 0 && dist >= (double)5.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.2F) {
                     this.performKakashiFireball(target);
                  }

                  return;
               }

               this.kakashiRetreating = false;
            }

            if (this.kakashiPhase >= 2 && this.kakashiKamuiCD <= 0 && dist >= (double)5.0F && dist <= (double)15.0F && this.rand.nextFloat() < 0.03F) {
               this.performKamui(target);
            } else if (this.kakashiWaterDragonCD <= 0 && dist >= (double)8.0F && dist <= (double)25.0F && this.rand.nextFloat() < (this.kakashiPhase >= 2 ? 0.12F : 0.06F)) {
               this.performKakashiWaterDragon(target);
            } else if (this.kakashiFireballCD <= 0 && dist >= (double)5.0F && dist <= (double)20.0F && this.rand.nextFloat() < (this.kakashiPhase >= 2 ? 0.15F : 0.08F)) {
               this.performKakashiFireball(target);
            } else if (this.kakashiLightningBladeCD <= 0 && dist >= (double)4.0F && dist <= (double)18.0F && this.rand.nextFloat() < (this.kakashiPhase >= 2 ? 0.15F : 0.1F)) {
               this.performLightningBlade(target);
            } else if (this.kakashiRetreatCD <= 0 && dist <= (double)4.0F && hpFrac < 0.7F && this.rand.nextFloat() < 0.08F) {
               this.kakashiRetreating = true;
               this.kakashiRetreatTicks = 40;
               this.kakashiRetreatCD = this.cdMul(160);
            } else {
               if (this.kakashiMeleeCD <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)3.0F) {
                  this.performKakashiMelee(target);
               }

            }
         }
      }

      protected void tickStyleCooldowns() {
         if (this.kakashiLightningBladeCD > 0) {
            --this.kakashiLightningBladeCD;
         }

         if (this.kakashiSharinganCounterCD > 0) {
            --this.kakashiSharinganCounterCD;
         }

         if (this.kakashiKamuiCD > 0) {
            --this.kakashiKamuiCD;
         }

         if (this.kakashiRetreatCD > 0) {
            --this.kakashiRetreatCD;
         }

         if (this.kakashiMeleeCD > 0) {
            --this.kakashiMeleeCD;
         }

         if (this.kakashiComboDelay > 0) {
            --this.kakashiComboDelay;
         }

         if (this.kakashiRetreatTicks > 0) {
            --this.kakashiRetreatTicks;
         }

         if (this.kakashiFireballCD > 0) {
            --this.kakashiFireballCD;
         }

         if (this.kakashiWaterDragonCD > 0) {
            --this.kakashiWaterDragonCD;
         }

         if (this.kakashiLightningCloneCD > 0) {
            --this.kakashiLightningCloneCD;
         }

         if (this.kakashiFireballBurstRemaining > 0) {
            if (this.kakashiFireballBurstDelay > 0) {
               --this.kakashiFireballBurstDelay;
            } else if (this.kakashiFireballBurstTarget != null && this.kakashiFireballBurstTarget.isEntityAlive()) {
               this.fireKakashiSingleFireball(this.kakashiFireballBurstTarget);
               --this.kakashiFireballBurstRemaining;
               this.kakashiFireballBurstDelay = 5;
               if (this.kakashiFireballBurstRemaining <= 0) {
                  this.kakashiFireballBurstTarget = null;
               }
            } else {
               this.kakashiFireballBurstRemaining = 0;
               this.kakashiFireballBurstTarget = null;
            }
         }

      }

      protected void resetCombatState() {
         this.kakashiPhase = 1;
         this.kakashiIntroPlayed = false;
         this.kakashiComboStep = 0;
         this.kakashiRetreating = false;
         this.kakashiFireballCD = 0;
         this.kakashiWaterDragonCD = 0;
         this.kakashiLightningCloneCD = 0;
         this.kakashiLightningBladeCD = 0;
         this.kakashiSharinganCounterCD = 0;
         this.kakashiKamuiCD = 0;
         this.kakashiRetreatCD = 0;
         this.kakashiMeleeCD = 0;
         this.kakashiComboDelay = 0;
         this.kakashiComboTarget = null;
         this.kakashiRetreatTicks = 0;
         this.kakashiFireballBurstRemaining = 0;
         this.kakashiFireballBurstDelay = 0;
         this.kakashiFireballBurstTarget = null;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("kakashiPhase", this.kakashiPhase);
         compound.setBoolean("kakashiIntroPlayed", this.kakashiIntroPlayed);
         compound.setInteger("kakashiLightningBladeCD", this.kakashiLightningBladeCD);
         compound.setInteger("kakashiSharinganCounterCD", this.kakashiSharinganCounterCD);
         compound.setInteger("kakashiKamuiCD", this.kakashiKamuiCD);
         compound.setInteger("kakashiRetreatCD", this.kakashiRetreatCD);
         compound.setInteger("kakashiMeleeCD", this.kakashiMeleeCD);
         compound.setInteger("kakashiFireballCD", this.kakashiFireballCD);
         compound.setInteger("kakashiWaterDragonCD", this.kakashiWaterDragonCD);
         compound.setInteger("kakashiLightningCloneCD", this.kakashiLightningCloneCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.kakashiPhase = compound.hasKey("kakashiPhase") ? compound.getInteger("kakashiPhase") : 1;
         this.kakashiIntroPlayed = compound.getBoolean("kakashiIntroPlayed");
         this.kakashiLightningBladeCD = compound.hasKey("kakashiLightningBladeCD") ? compound.getInteger("kakashiLightningBladeCD") : 0;
         this.kakashiSharinganCounterCD = compound.hasKey("kakashiSharinganCounterCD") ? compound.getInteger("kakashiSharinganCounterCD") : 0;
         this.kakashiKamuiCD = compound.hasKey("kakashiKamuiCD") ? compound.getInteger("kakashiKamuiCD") : 0;
         this.kakashiRetreatCD = compound.hasKey("kakashiRetreatCD") ? compound.getInteger("kakashiRetreatCD") : 0;
         this.kakashiMeleeCD = compound.hasKey("kakashiMeleeCD") ? compound.getInteger("kakashiMeleeCD") : 0;
         this.kakashiFireballCD = compound.hasKey("kakashiFireballCD") ? compound.getInteger("kakashiFireballCD") : 0;
         this.kakashiWaterDragonCD = compound.hasKey("kakashiWaterDragonCD") ? compound.getInteger("kakashiWaterDragonCD") : 0;
         this.kakashiLightningCloneCD = compound.hasKey("kakashiLightningCloneCD") ? compound.getInteger("kakashiLightningCloneCD") : 0;
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         Entity trueSource = source.getTrueSource();
         if (!this.world.isRemote && trueSource instanceof EntityLivingBase && this.kakashiLightningCloneCD <= 0 && this.rand.nextFloat() < 0.1F) {
            this.kakashiLightningCloneCD = this.cdMul(300);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, 0.8, (double)0.5F, 0.03, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY + (double)1.0F, this.posZ, 10, 0.3, (double)0.5F, 0.3, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 0.3F, 2.0F);
            double behindDist = (double)2.5F;
            double attackerYaw = Math.toRadians((double)trueSource.rotationYaw);
            double newX = trueSource.posX + Math.sin(attackerYaw) * behindDist;
            double newZ = trueSource.posZ - Math.cos(attackerYaw) * behindDist;
            this.internalReposition = true;
            this.setPositionAndUpdate(newX, trueSource.posY, newZ);
            this.internalReposition = false;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, newX, trueSource.posY + (double)1.0F, newZ, 8, 0.2, 0.3, 0.2, 0.03, new int[0]);
            }

            this.broadcastChat((double)30.0F, "§7[§cKakashi§7] §fA lightning clone...");
            return -1.0F;
         } else {
            if (!this.world.isRemote && trueSource instanceof EntityPlayer && this.kakashiSharinganCounterCD <= 0) {
               float dodgeChance = this.kakashiPhase >= 2 ? 0.2F : 0.15F;
               if (this.rand.nextFloat() < dodgeChance) {
                  this.kakashiSharinganCounterCD = this.cdMul(160);
                  EntityPlayer attacker = (EntityPlayer)trueSource;
                  float counterTotal = amount * 0.6F;
                  if (this.trueDamageSplit > 0.0F) {
                     float counterNorm = counterTotal * (1.0F - this.trueDamageSplit);
                     float counterTrue = counterTotal * this.trueDamageSplit * this.trueDamageMultiplier;
                     attacker.attackEntityFrom(DamageSource.causeMobDamage(this), counterNorm);
                     attacker.hurtResistantTime = 0;
                     attacker.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), counterTrue);
                  } else {
                     attacker.attackEntityFrom(DamageSource.causeMobDamage(this), counterTotal);
                  }

                  double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
                  double newX = this.posX + Math.cos(angle) * (double)2.5F;
                  double newZ = this.posZ + Math.sin(angle) * (double)2.5F;
                  this.internalReposition = true;
                  this.setPositionAndUpdate(newX, this.posY, newZ);
                  this.internalReposition = false;
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 12, 0.4, 0.6, 0.4, 0.02, new int[0]);
                  }

                  if (trueSource instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)trueSource).sendMessage(new TextComponentString("§7[§cKakashi§7] §fI've already seen through that move."));
                  }

                  return -1.0F;
               }
            }

            return amount;
         }
      }

      private void performLightningBlade(EntityLivingBase target) {
         this.kakashiLightningBladeCD = this.cdMul(200);
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double dDist = Math.sqrt(dx * dx + dz * dz);
         if (!(dDist < 0.1)) {
            double speed = 1.8;
            this.isDashing = true;
            this.dashVelX = dx / dDist * speed;
            this.dashVelZ = dz / dDist * speed;
            this.dashTicksRemaining = Math.min(8, (int)(dDist / speed));
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int i = 0; i < 20; ++i) {
                  double px = this.posX + dx / dDist * (double)i * (double)0.5F;
                  double pz = this.posZ + dz / dDist * (double)i * (double)0.5F;
                  ws.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, px, this.posY + 0.8, pz, 2, 0.1, 0.2, 0.1, 0.02, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, px, this.posY + (double)1.0F, pz, 1, 0.1, 0.1, 0.1, 0.05, new int[0]);
               }
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 0.5F, 1.8F);
            float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            float lightningDmg;
            float lightningTrue;
            if (this.trueDamageSplit > 0.0F) {
               float lightSplit = Math.min(1.0F, this.trueDamageSplit + 0.25F);
               lightningDmg = baseDmg * 1.8F * (1.0F - lightSplit) * (float)dmgMul;
               lightningTrue = baseDmg * 1.8F * lightSplit * (float)dmgMul * this.trueDamageMultiplier;
            } else {
               lightningDmg = baseDmg * 1.8F * (float)dmgMul;
               lightningTrue = baseDmg * 0.6F * this.trueDamageMultiplier;
            }

            if ((double)this.getDistance(target) <= (double)4.5F) {
               target.attackEntityFrom(DamageSource.causeMobDamage(this), lightningDmg);
               target.hurtResistantTime = 0;
               target.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), lightningTrue);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, target.posX, target.posY + (double)1.0F, target.posZ, 15, 0.3, (double)0.5F, 0.3, 0.1, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 10, 0.4, 0.4, 0.4, 0.1, new int[0]);
               }
            }

            this.swingArm(EnumHand.MAIN_HAND);
         }
      }

      private void performKamui(EntityLivingBase target) {
         this.kakashiKamuiCD = this.cdMul(500);
         this.broadcastChat((double)40.0F, "§7[§cKakashi§7] §5§lKamui!");
         List<EntityPlayer> nearbyPlayers = this.world.getEntitiesWithinAABB(EntityPlayer.class, target.getEntityBoundingBox().grow((double)8.0F), (pxx) -> pxx != null && pxx.isEntityAlive() && !pxx.isSpectator() && !pxx.isCreative());
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         double dmgMul = this.getDamageMultiplier();
         float kamuiTotal = baseDmg * 2.5F * (float)dmgMul;
         float kamuiNormal;
         float kamuiTrue;
         if (this.trueDamageSplit > 0.0F) {
            float kamuiSplit = Math.min(1.0F, this.trueDamageSplit + 0.2F);
            kamuiNormal = kamuiTotal * (1.0F - kamuiSplit);
            kamuiTrue = kamuiTotal * kamuiSplit * this.trueDamageMultiplier;
         } else {
            kamuiNormal = kamuiTotal;
            kamuiTrue = baseDmg * 1.0F * this.trueDamageMultiplier;
         }

         for(EntityPlayer p : nearbyPlayers) {
            p.attackEntityFrom(DamageSource.causeMobDamage(this), kamuiNormal);
            p.hurtResistantTime = 0;
            p.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), kamuiTrue);
            p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 40; ++i) {
               double angle = (double)i / (double)40.0F * Math.PI * (double)4.0F;
               double radius = (double)2.0F + (double)i / (double)40.0F * (double)6.0F;
               double px = target.posX + Math.cos(angle) * radius;
               double pz = target.posZ + Math.sin(angle) * radius;
               ws.spawnParticle(EnumParticleTypes.PORTAL, px, target.posY + (double)1.0F, pz, 3, 0.1, 0.3, 0.1, (double)0.5F, new int[0]);
            }

            ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, target.posX, target.posY + (double)1.0F, target.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.5F, 0.5F);
      }

      private void performKakashiMelee(EntityLivingBase target) {
         this.performMeleeSwing(target);
         this.kakashiMeleeCD = this.kakashiPhase >= 2 ? 8 + this.rand.nextInt(6) : 12 + this.rand.nextInt(8);
         this.meleeCooldown = this.kakashiMeleeCD;
         if (this.kakashiComboStep == 0 && this.rand.nextFloat() < (this.kakashiPhase >= 2 ? 0.55F : 0.45F)) {
            this.kakashiComboStep = 1;
            this.kakashiComboDelay = this.kakashiPhase >= 2 ? 5 : 7;
            this.kakashiComboTarget = target;
         }

      }

      private void performKakashiFireball(EntityLivingBase target) {
         this.kakashiFireballCD = this.cdMul(120);
         this.fireKakashiSingleFireball(target);
         this.kakashiFireballBurstRemaining = 2;
         this.kakashiFireballBurstDelay = 5;
         this.kakashiFireballBurstTarget = target;
         if (this.rand.nextFloat() < 0.4F) {
            this.broadcastChat((double)30.0F, "§7[§cKakashi§7] §fFire Release: Phoenix Flower!");
         }

      }

      private void fireKakashiSingleFireball(EntityLivingBase target) {
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         double dmgMul = this.getDamageMultiplier();
         float normalDmg;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            normalDmg = baseDmg * 1.5F * (1.0F - this.trueDamageSplit) * (float)dmgMul;
            trueDmg = baseDmg * 1.5F * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
         } else {
            normalDmg = baseDmg * 1.5F * (float)dmgMul;
            trueDmg = 6.0F * this.trueDamageMultiplier;
         }

         EntityKatonFireball.EntityCustom fireball = new EntityKatonFireball.EntityCustom(this.world, this, normalDmg, trueDmg);
         double dx = target.posX - this.posX;
         double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
         double dz = target.posZ - this.posZ;
         fireball.shoot(dx, dy, dz, 1.5F, 4.0F);
         this.world.spawnEntity(fireball);
         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + 1.2, this.posZ, 5, 0.2, 0.2, 0.2, 0.05, new int[0]);
         }

      }

      private void performKakashiWaterDragon(EntityLivingBase target) {
         this.kakashiWaterDragonCD = this.cdMul(200);
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         double dmgMul = this.getDamageMultiplier();
         float normalDmg;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            normalDmg = baseDmg * 3.0F * (1.0F - this.trueDamageSplit) * (float)dmgMul;
            trueDmg = baseDmg * 3.0F * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
         } else {
            normalDmg = baseDmg * 3.0F * (float)dmgMul;
            trueDmg = 16.0F * this.trueDamageMultiplier;
         }

         EntityWaterDragonJutsu.EntityCustom dragon = new EntityWaterDragonJutsu.EntityCustom(this.world, this, normalDmg, trueDmg);
         double dx = target.posX - this.posX;
         double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         dragon.shoot(dx, dy + horizDist * 0.01, dz, 1.2F, 1.5F);
         this.world.spawnEntity(dragon);
         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + 1.2, this.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
         }

         this.broadcastChat((double)30.0F, "§7[§cKakashi§7] §9Water Release: Water Dragon!");
      }

      private void processKakashiCombo() {
         if (this.kakashiComboTarget != null && this.kakashiComboTarget.isEntityAlive() && !(this.getDistanceSq(this.kakashiComboTarget) > (double)16.0F)) {
            float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            if (this.kakashiComboStep == 1) {
               float comboNormal;
               float comboTrue;
               if (this.trueDamageSplit > 0.0F) {
                  comboNormal = baseDmg * 0.8F * (1.0F - this.trueDamageSplit) * (float)dmgMul;
                  comboTrue = baseDmg * 0.8F * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
               } else {
                  comboNormal = baseDmg * 0.8F * (float)dmgMul;
                  comboTrue = 3.0F * this.trueDamageMultiplier;
               }

               this.kakashiComboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), comboNormal);
               this.kakashiComboTarget.hurtResistantTime = 0;
               this.kakashiComboTarget.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), comboTrue);
               this.swingArm(EnumHand.MAIN_HAND);
               this.kakashiComboStep = 2;
               this.kakashiComboDelay = 10;
            } else if (this.kakashiComboStep == 2) {
               float heavyNormal;
               float heavyTrue;
               if (this.trueDamageSplit > 0.0F) {
                  float heavySplit = Math.min(1.0F, this.trueDamageSplit + 0.15F);
                  heavyNormal = baseDmg * 1.4F * (1.0F - heavySplit) * (float)dmgMul;
                  heavyTrue = baseDmg * 1.4F * heavySplit * (float)dmgMul * this.trueDamageMultiplier;
               } else {
                  heavyNormal = baseDmg * 1.4F * (float)dmgMul;
                  heavyTrue = 6.0F * this.trueDamageMultiplier;
               }

               this.kakashiComboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), heavyNormal);
               this.kakashiComboTarget.hurtResistantTime = 0;
               this.kakashiComboTarget.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), heavyTrue);
               this.swingArm(EnumHand.MAIN_HAND);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.kakashiComboTarget.posX, this.kakashiComboTarget.posY + (double)1.0F, this.kakashiComboTarget.posZ, 8, 0.3, 0.4, 0.3, 0.08, new int[0]);
               }

               this.kakashiComboStep = 0;
               this.kakashiComboTarget = null;
               this.meleeCooldown = 25;
            }

         } else {
            this.kakashiComboStep = 0;
            this.kakashiComboTarget = null;
         }
      }
   }
}
