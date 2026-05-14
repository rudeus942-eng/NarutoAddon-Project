
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.jutsu.EntityKatonFireDragon;
import net.luck.narutoaddon.OtherCode.jutsu.EntityPhoenixSageFire;
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
public class EntityItachiStyleNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 245;

   public EntityItachiStyleNPC(ElementsInfTsukAddon instance) {
      super(instance, 245);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "itachistyle"), 245).name("itachistyle").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, ItachiStyleRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class ItachiStyleRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public ItachiStyleRenderer(RenderManager renderManager) {
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
      private static final float ITACHI_PHASE_2 = 0.65F;
      private static final float ITACHI_PHASE_3 = 0.35F;
      private static final double ITACHI_BASE_HP = (double)12000.0F;
      private int itachiPhase = 1;
      private boolean itachiIntroPlayed = false;
      private int itachiMode = 0;
      private int itachiModeSwitchTimer = 0;
      private int itachiMeleeCD = 0;
      private int itachiComboStep = 0;
      private int itachiComboDelay = 0;
      private EntityLivingBase itachiComboTarget = null;
      private int itachiBodyFlickerCD = 0;
      private int itachiKickCD = 0;
      private int itachiFireballCD = 0;
      private int itachiPhoenixCD = 0;
      private int itachiShurikenCD = 0;
      private int itachiSideDashCD = 0;
      private int itachiTsukuyomiCD = 0;
      private int itachiDemonicIllusionCD = 0;
      private int itachiCrowCloneCD = 0;
      private int itachiAmaterasuCD = 0;
      private int itachiPhoenixSageCD = 0;
      private int itachiFireDragonCD = 0;
      private int itachiYataMirrorCD = 0;
      private boolean itachiYataMirrorActive = false;
      private int itachiYataMirrorTicks = 0;
      private boolean itachiSusanooActive = false;
      private int itachiTotsukaCD = 0;
      private Entity itachiSusanooEntity = null;
      private boolean itachiSusanooDestroyed = false;
      private int itachiCounterCD = 0;
      private int itachiComboHitCount = 0;
      private int itachiChatCD = 0;

      public EntityCustom(World world) {
         super(world);
      }

      protected boolean usesVanillaMeleeAI() {
         return false;
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processItachiCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.itachiMeleeCD > 0) {
            --this.itachiMeleeCD;
         }

         if (this.itachiBodyFlickerCD > 0) {
            --this.itachiBodyFlickerCD;
         }

         if (this.itachiKickCD > 0) {
            --this.itachiKickCD;
         }

         if (this.itachiFireballCD > 0) {
            --this.itachiFireballCD;
         }

         if (this.itachiPhoenixCD > 0) {
            --this.itachiPhoenixCD;
         }

         if (this.itachiShurikenCD > 0) {
            --this.itachiShurikenCD;
         }

         if (this.itachiSideDashCD > 0) {
            --this.itachiSideDashCD;
         }

         if (this.itachiTsukuyomiCD > 0) {
            --this.itachiTsukuyomiCD;
         }

         if (this.itachiDemonicIllusionCD > 0) {
            --this.itachiDemonicIllusionCD;
         }

         if (this.itachiCrowCloneCD > 0) {
            --this.itachiCrowCloneCD;
         }

         if (this.itachiAmaterasuCD > 0) {
            --this.itachiAmaterasuCD;
         }

         if (this.itachiCounterCD > 0) {
            --this.itachiCounterCD;
         }

         if (this.itachiModeSwitchTimer > 0) {
            --this.itachiModeSwitchTimer;
         }

         if (this.itachiYataMirrorCD > 0) {
            --this.itachiYataMirrorCD;
         }

         if (this.itachiTotsukaCD > 0) {
            --this.itachiTotsukaCD;
         }

         if (this.itachiPhoenixSageCD > 0) {
            --this.itachiPhoenixSageCD;
         }

         if (this.itachiFireDragonCD > 0) {
            --this.itachiFireDragonCD;
         }

         if (this.itachiChatCD > 0) {
            --this.itachiChatCD;
         }

         if (this.itachiYataMirrorActive) {
            --this.itachiYataMirrorTicks;
            if (this.itachiYataMirrorTicks <= 0) {
               this.itachiYataMirrorActive = false;
            }

            if (this.world instanceof WorldServer && this.ticksExisted % 3 == 0) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + 1.2, this.posZ, 8, 0.6, 0.8, 0.6, (double)0.5F, new int[0]);
            }
         }

         if (this.itachiSusanooActive && this.world instanceof WorldServer && this.ticksExisted % 4 == 0) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, this.posY + (double)0.5F + this.rand.nextDouble() * (double)1.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, 2, 0.3, 0.4, 0.3, 0.01, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.REDSTONE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posY + this.rand.nextDouble() * (double)2.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, 3, (double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F, new int[0]);
         }

         if (this.itachiComboStep > 0 && this.itachiComboTarget != null) {
            if (this.itachiComboDelay > 0) {
               --this.itachiComboDelay;
            } else {
               this.processItachiComboHit();
            }
         }

         if (this.itachiSusanooEntity != null && this.itachiSusanooEntity.isEntityAlive() && !this.isRiding() && this.ticksExisted % 10 == 0) {
            this.startRiding(this.itachiSusanooEntity, true);
         }

         if (this.itachiSusanooActive && (this.itachiSusanooEntity == null || !this.itachiSusanooEntity.isEntityAlive())) {
            this.itachiSusanooActive = false;
            this.itachiSusanooDestroyed = true;
            this.itachiSusanooEntity = null;
         }

      }

      protected void resetCombatState() {
         this.itachiPhase = 1;
         this.itachiIntroPlayed = false;
         this.itachiMode = 0;
         this.itachiModeSwitchTimer = 300 + this.rand.nextInt(100);
         this.itachiSusanooActive = false;
         this.itachiYataMirrorActive = false;
         this.itachiComboStep = 0;
         this.itachiComboHitCount = 0;
         this.itachiPhoenixSageCD = 0;
         this.itachiFireDragonCD = 0;
      }

      protected void onCombatDeath() {
         this.itachiCleanupSusanoo();
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         Entity trueSource = source.getTrueSource();
         if (!this.world.isRemote && this.itachiPhase >= 3 && !this.itachiYataMirrorActive && this.itachiYataMirrorCD <= 0 && amount > this.getMaxHealth() * 0.05F) {
            EntityLivingBase attacker = trueSource instanceof EntityLivingBase ? (EntityLivingBase)trueSource : null;
            this.itachiActivateYataMirror(attacker);
         }

         if (this.itachiYataMirrorActive) {
            amount *= 0.1F;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.0F, this.posZ, 10, (double)0.5F, 0.8, (double)0.5F, 0.3, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.HOSTILE, 1.0F, 1.2F);
         }

         if (this.itachiSusanooActive) {
            amount *= 0.7F;
         }

         if (!this.world.isRemote && this.itachiCounterCD <= 0 && trueSource instanceof EntityLivingBase && (double)this.getDistance(trueSource) <= (double)3.5F && this.itachiMode == 0) {
            float counterChance = this.itachiPhase >= 2 ? 0.25F : 0.2F;
            if (this.rand.nextFloat() < counterChance) {
               EntityLivingBase attacker = (EntityLivingBase)trueSource;
               double aDx = attacker.posX - this.posX;
               double aDz = attacker.posZ - this.posZ;
               double aDist = Math.sqrt(aDx * aDx + aDz * aDz);
               if (aDist > 0.1) {
                  double behindX = attacker.posX + aDx / aDist * (double)1.5F;
                  double behindZ = attacker.posZ + aDz / aDist * (double)1.5F;
                  double behindY = this.findSafeY(behindX, this.posY, behindZ);
                  this.internalReposition = true;
                  this.setPositionAndUpdate(behindX, behindY, behindZ);
                  this.internalReposition = false;
               }

               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               float counterNormal = baseDmg * 0.7F * (float)this.getDamageMultiplier();
               attacker.attackEntityFrom(DamageSource.causeMobDamage(this), counterNormal);
               attacker.hurtResistantTime = 0;
               attacker.attackEntityFrom(DamageSource.MAGIC, 3.0F);
               this.swingArm(EnumHand.MAIN_HAND);
               this.itachiCounterCD = this.itachiPhase >= 3 ? 60 : 80;
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, attacker.posX, attacker.posY + (double)1.0F, attacker.posZ, 15, 0.3, (double)0.5F, 0.3, 0.02, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.8F, 1.5F);
               if (attacker instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)attacker).sendMessage(new TextComponentString("§c§o* Itachi reads your movements with Sharingan... *"));
               }

               return -1.0F;
            }
         }

         return amount;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("itachiPhase", this.itachiPhase);
         compound.setBoolean("itachiIntroPlayed", this.itachiIntroPlayed);
         compound.setInteger("itachiMode", this.itachiMode);
         compound.setInteger("itachiModeSwitchTimer", this.itachiModeSwitchTimer);
         compound.setInteger("itachiMeleeCD", this.itachiMeleeCD);
         compound.setInteger("itachiBodyFlickerCD", this.itachiBodyFlickerCD);
         compound.setInteger("itachiKickCD", this.itachiKickCD);
         compound.setInteger("itachiFireballCD", this.itachiFireballCD);
         compound.setInteger("itachiPhoenixCD", this.itachiPhoenixCD);
         compound.setInteger("itachiShurikenCD", this.itachiShurikenCD);
         compound.setInteger("itachiSideDashCD", this.itachiSideDashCD);
         compound.setInteger("itachiTsukuyomiCD", this.itachiTsukuyomiCD);
         compound.setInteger("itachiDemonicIllusionCD", this.itachiDemonicIllusionCD);
         compound.setInteger("itachiCrowCloneCD", this.itachiCrowCloneCD);
         compound.setInteger("itachiAmaterasuCD", this.itachiAmaterasuCD);
         compound.setInteger("itachiCounterCD", this.itachiCounterCD);
         compound.setInteger("itachiYataMirrorCD", this.itachiYataMirrorCD);
         compound.setBoolean("itachiYataMirrorActive", this.itachiYataMirrorActive);
         compound.setInteger("itachiYataMirrorTicks", this.itachiYataMirrorTicks);
         compound.setBoolean("itachiSusanooActive", this.itachiSusanooActive);
         compound.setBoolean("itachiSusanooDestroyed", this.itachiSusanooDestroyed);
         compound.setInteger("itachiTotsukaCD", this.itachiTotsukaCD);
         compound.setInteger("itachiPhoenixSageCD", this.itachiPhoenixSageCD);
         compound.setInteger("itachiFireDragonCD", this.itachiFireDragonCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.itachiPhase = compound.hasKey("itachiPhase") ? compound.getInteger("itachiPhase") : 1;
         this.itachiIntroPlayed = compound.getBoolean("itachiIntroPlayed");
         this.itachiMode = compound.hasKey("itachiMode") ? compound.getInteger("itachiMode") : 0;
         this.itachiModeSwitchTimer = compound.hasKey("itachiModeSwitchTimer") ? compound.getInteger("itachiModeSwitchTimer") : 300;
         this.itachiMeleeCD = compound.hasKey("itachiMeleeCD") ? compound.getInteger("itachiMeleeCD") : 0;
         this.itachiBodyFlickerCD = compound.hasKey("itachiBodyFlickerCD") ? compound.getInteger("itachiBodyFlickerCD") : 0;
         this.itachiKickCD = compound.hasKey("itachiKickCD") ? compound.getInteger("itachiKickCD") : 0;
         this.itachiFireballCD = compound.hasKey("itachiFireballCD") ? compound.getInteger("itachiFireballCD") : 0;
         this.itachiPhoenixCD = compound.hasKey("itachiPhoenixCD") ? compound.getInteger("itachiPhoenixCD") : 0;
         this.itachiShurikenCD = compound.hasKey("itachiShurikenCD") ? compound.getInteger("itachiShurikenCD") : 0;
         this.itachiSideDashCD = compound.hasKey("itachiSideDashCD") ? compound.getInteger("itachiSideDashCD") : 0;
         this.itachiTsukuyomiCD = compound.hasKey("itachiTsukuyomiCD") ? compound.getInteger("itachiTsukuyomiCD") : 0;
         this.itachiDemonicIllusionCD = compound.hasKey("itachiDemonicIllusionCD") ? compound.getInteger("itachiDemonicIllusionCD") : 0;
         this.itachiCrowCloneCD = compound.hasKey("itachiCrowCloneCD") ? compound.getInteger("itachiCrowCloneCD") : 0;
         this.itachiAmaterasuCD = compound.hasKey("itachiAmaterasuCD") ? compound.getInteger("itachiAmaterasuCD") : 0;
         this.itachiCounterCD = compound.hasKey("itachiCounterCD") ? compound.getInteger("itachiCounterCD") : 0;
         this.itachiYataMirrorCD = compound.hasKey("itachiYataMirrorCD") ? compound.getInteger("itachiYataMirrorCD") : 0;
         this.itachiYataMirrorActive = compound.getBoolean("itachiYataMirrorActive");
         this.itachiYataMirrorTicks = compound.hasKey("itachiYataMirrorTicks") ? compound.getInteger("itachiYataMirrorTicks") : 0;
         this.itachiSusanooActive = compound.getBoolean("itachiSusanooActive");
         this.itachiSusanooDestroyed = compound.getBoolean("itachiSusanooDestroyed");
         this.itachiTotsukaCD = compound.hasKey("itachiTotsukaCD") ? compound.getInteger("itachiTotsukaCD") : 0;
         this.itachiPhoenixSageCD = compound.hasKey("itachiPhoenixSageCD") ? compound.getInteger("itachiPhoenixSageCD") : 0;
         this.itachiFireDragonCD = compound.hasKey("itachiFireDragonCD") ? compound.getInteger("itachiFireDragonCD") : 0;
      }

      private void processItachiCombat(EntityLivingBase target, double dist) {
         if (!this.itachiIntroPlayed) {
            this.itachiIntroPlayed = true;
            this.itachiBroadcast(target, "§c§lItachi Uchiha");
         } else {
            double yDiff = target.posY - this.posY;
            if (yDiff > (double)5.0F && !this.isDashing) {
               if (yDiff > (double)30.0F) {
                  this.itachiTeleport(target.posX, target.posY, target.posZ);
               } else {
                  double ldx = target.posX - this.posX;
                  double ldz = target.posZ - this.posZ;
                  double hd = Math.sqrt(ldx * ldx + ldz * ldz);
                  if (hd > (double)0.5F) {
                     double nx = ldx / hd;
                     double nz = ldz / hd;
                     this.isDashing = true;
                     this.isLeaping = true;
                     this.dashType = 4;
                     this.dashDuration = 14;
                     this.dashTicksRemaining = 14;
                     this.dashVelX = nx * 0.6;
                     this.dashVelZ = nz * 0.6;
                     this.dashArcSustain = 0.0F;
                     this.dashFallAccel = 0.06F;
                     this.dashMaxFall = -1.0F;
                     this.dashHasMidairGuidance = true;
                     this.motionY = Math.min((double)2.5F, 0.7 + yDiff * 0.12);
                     this.velocityChanged = true;
                  }
               }

            } else {
               this.itachiCheckPhaseTransition(target);
               if (this.itachiModeSwitchTimer <= 0) {
                  this.itachiSwitchMode(target, dist);
               }

               if (this.itachiPhase >= 3 && !this.itachiSusanooActive && !this.itachiSusanooDestroyed && this.getHealth() < this.getMaxHealth() * 0.25F) {
                  this.itachiSusanooActive = true;
                  this.itachiBroadcast(target, "§4Susanoo...");
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 60, (double)2.0F, (double)2.0F, (double)2.0F, 0.05, new int[0]);
                  }
               }

               switch (this.itachiMode) {
                  case 0:
                     this.processItachiTaijutsu(target, dist);
                     break;
                  case 1:
                     this.processItachiNinjutsu(target, dist);
                     break;
                  case 2:
                     this.processItachiGenjutsu(target, dist);
               }

               if (this.itachiPhase >= 3 && this.itachiSusanooActive && this.itachiTotsukaCD <= 0 && dist < (double)5.0F) {
                  this.itachiTotsukaCD = (int)(300.0F * this.configCooldownMultiplier);
                  float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                  float dmg = baseDmg * 1.5F * (float)this.getDamageMultiplier();
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.causeMobDamage(this), dmg);
                  target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 2));
                  target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 1));
                  this.itachiBroadcast(target, "§4Totsuka Blade!");
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 30, (double)1.0F, (double)1.0F, (double)1.0F, 0.1, new int[0]);
                  }
               }

            }
         }
      }

      private void itachiSwitchMode(EntityLivingBase target, double dist) {
         int oldMode = this.itachiMode;
         if (this.itachiPhase >= 3) {
            int roll = this.rand.nextInt(10);
            if (roll < 2) {
               this.itachiMode = 0;
            } else if (roll < 7) {
               this.itachiMode = 1;
            } else {
               this.itachiMode = 2;
            }
         } else if (this.itachiPhase == 2) {
            int roll = this.rand.nextInt(10);
            if (roll < 3) {
               this.itachiMode = 0;
            } else if (roll < 6) {
               this.itachiMode = 1;
            } else {
               this.itachiMode = 2;
            }
         } else {
            int roll = this.rand.nextInt(10);
            if (roll < 5) {
               this.itachiMode = 0;
            } else if (roll < 8) {
               this.itachiMode = 1;
            } else {
               this.itachiMode = 2;
            }
         }

         if (this.itachiMode == oldMode && this.rand.nextInt(3) == 0) {
            this.itachiMode = (this.itachiMode + 1) % 3;
         }

         this.itachiModeSwitchTimer = 200 + this.rand.nextInt(200);
      }

      private void processItachiTaijutsu(EntityLivingBase target, double dist) {
         if (dist > (double)8.0F && this.itachiBodyFlickerCD <= 0) {
            this.itachiBodyFlicker(target);
         } else {
            if (dist < (double)4.0F) {
               if (this.itachiComboStep == 0 && this.itachiKickCD <= 0) {
                  this.itachiStartCombo(target);
                  return;
               }

               if (this.itachiSideDashCD <= 0 && this.rand.nextInt(5) == 0) {
                  this.itachiSideDash(target);
                  return;
               }

               if (this.itachiMeleeCD <= 0) {
                  this.performMeleeSwing(target);
                  this.itachiMeleeCD = (int)(15.0F * this.configCooldownMultiplier);
               }
            } else {
               this.itachiMoveToward(target, 1.4);
            }

         }
      }

      private void processItachiNinjutsu(EntityLivingBase target, double dist) {
         if (dist < (double)3.0F && this.itachiBodyFlickerCD <= 0 && this.rand.nextInt(3) == 0) {
            double angle = Math.atan2(this.posZ - target.posZ, this.posX - target.posX);
            double bx = this.posX + Math.cos(angle) * (double)8.0F;
            double bz = this.posZ + Math.sin(angle) * (double)8.0F;
            double by = this.findSafeY(bx, this.posY, bz);
            if (by >= (double)0.0F) {
               this.itachiTeleport(bx, by, bz);
               this.itachiBodyFlickerCD = (int)(60.0F * this.configCooldownMultiplier);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
               }
            }

         } else if (this.itachiPhoenixSageCD <= 0 && dist > (double)10.0F && dist < (double)18.0F) {
            this.itachiCastPhoenixSageFire(target);
         } else if (this.itachiPhase >= 2 && this.itachiFireDragonCD <= 0 && dist > (double)8.0F && dist < (double)30.0F) {
            this.itachiCastFireDragon(target);
         } else if (this.itachiPhoenixCD <= 0 && dist > (double)4.0F && dist < (double)30.0F) {
            this.itachiCastPhoenixFlower(target);
         } else if (this.itachiFireballCD <= 0 && dist > (double)5.0F && dist < (double)35.0F) {
            this.itachiCastFireball(target);
         } else if (this.itachiPhase >= 2 && this.itachiAmaterasuCD <= 0 && dist < (double)25.0F) {
            this.itachiCastAmaterasu(target);
         } else if (this.itachiShurikenCD <= 0 && dist > (double)6.0F && dist < (double)25.0F) {
            this.itachiShurikenBarrage(target);
         } else {
            if (dist < (double)6.0F) {
               this.itachiMoveToward(target, 1.2);
            } else if (dist > (double)20.0F) {
               this.itachiMoveToward(target, (double)1.0F);
            }

         }
      }

      private void processItachiGenjutsu(EntityLivingBase target, double dist) {
         if (this.itachiPhase >= 2 && this.itachiTsukuyomiCD <= 0 && dist < (double)15.0F) {
            this.itachiCastTsukuyomi(target);
         } else if (this.itachiDemonicIllusionCD <= 0 && dist < (double)20.0F) {
            this.itachiCastDemonicIllusion(target);
         } else if (this.itachiCrowCloneCD <= 0 && dist < (double)8.0F) {
            this.itachiCrowClone(target);
         } else if (this.itachiTsukuyomiCD > 0 && this.itachiDemonicIllusionCD > 0 && this.itachiCrowCloneCD > 0) {
            this.processItachiTaijutsu(target, dist);
         } else {
            if (dist > (double)12.0F) {
               this.itachiMoveToward(target, 1.2);
            }

         }
      }

      private void itachiCheckPhaseTransition(EntityLivingBase target) {
         float hpPct = this.getHealth() / this.getMaxHealth();
         if (this.itachiPhase == 1 && hpPct < 0.6F) {
            this.itachiPhase = 2;
            this.itachiBroadcast(target, "§4Mangekyo Sharingan!");
            this.itachiTsukuyomiCD = 0;
            this.itachiAmaterasuCD = 0;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, this.posX, this.posY + (double)2.0F, this.posZ, 20, (double)1.0F, (double)1.0F, (double)1.0F, 0.05, new int[0]);
            }
         } else if (this.itachiPhase == 2 && hpPct < 0.3F) {
            this.itachiPhase = 3;
            this.itachiBroadcast(target, "§4§lSusanoo!");
            this.itachiSusanooActive = true;
            this.itachiSpawnSusanoo();
            if (this.itachiSusanooEntity == null || !this.itachiSusanooEntity.isEntityAlive()) {
               this.itachiSusanooActive = false;
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 80, (double)3.0F, (double)3.0F, (double)3.0F, 0.08, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.LAVA, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)2.0F, (double)2.0F, (double)2.0F, 0.05, new int[0]);
            }
         }

      }

      private void itachiBodyFlicker(EntityLivingBase target) {
         double angle = Math.atan2(target.posZ - this.posZ, target.posX - this.posX);
         double tx = target.posX - Math.cos(angle) * (double)2.0F;
         double tz = target.posZ - Math.sin(angle) * (double)2.0F;
         double ty = this.findSafeY(tx, target.posY, tz);
         if (ty < (double)0.0F) {
            ty = target.posY;
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
         }

         this.itachiTeleport(tx, ty, tz);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
         }

         this.itachiBodyFlickerCD = (int)((float)(this.itachiPhase >= 2 ? 40 : 60) * this.configCooldownMultiplier);
         this.faceEntity(target, 360.0F, 360.0F);
      }

      private void itachiStartCombo(EntityLivingBase target) {
         this.itachiComboStep = 1;
         this.itachiComboTarget = target;
         this.itachiComboDelay = 8;
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float dmg = baseDmg * 0.6F * (float)this.getDamageMultiplier();
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), dmg);
         this.swingArm(EnumHand.MAIN_HAND);
         this.itachiKickCD = (int)(40.0F * this.configCooldownMultiplier);
      }

      private void processItachiComboHit() {
         EntityLivingBase target = this.itachiComboTarget;
         if (target != null && target.isEntityAlive() && !((double)target.getDistance(this) > (double)5.0F)) {
            if (this.itachiComboStep <= 3) {
               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               float dmg = baseDmg * 0.5F * (float)this.getDamageMultiplier();
               this.swingArm(this.itachiComboStep % 2 == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.causeMobDamage(this), dmg);
               ++this.itachiComboStep;
               this.itachiComboDelay = 6 + this.rand.nextInt(4);
            } else {
               this.itachiFinisherKick(target);
               this.itachiComboStep = 0;
            }
         } else {
            this.itachiComboStep = 0;
         }
      }

      private void itachiFinisherKick(EntityLivingBase target) {
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float dmg = baseDmg * 0.8F * (float)this.getDamageMultiplier();
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), dmg);
         double kbAngle = Math.atan2(target.posZ - this.posZ, target.posX - this.posX);
         target.motionX += Math.cos(kbAngle) * 0.8;
         target.motionY += 0.4;
         target.motionZ += Math.sin(kbAngle) * 0.8;
         target.velocityChanged = true;
         this.swingArm(EnumHand.MAIN_HAND);
      }

      private void itachiSideDash(EntityLivingBase target) {
         double angle = Math.atan2(target.posZ - this.posZ, target.posX - this.posX);
         double perpAngle = angle + (this.rand.nextBoolean() ? (Math.PI / 2D) : (-Math.PI / 2D));
         double dashDist2 = (double)3.0F + this.rand.nextDouble() * (double)2.0F;
         double dx = this.posX + Math.cos(perpAngle) * dashDist2;
         double dz = this.posZ + Math.sin(perpAngle) * dashDist2;
         double dy = this.findSafeY(dx, this.posY, dz);
         if (dy >= (double)0.0F) {
            this.itachiTeleport(dx, dy, dz);
         }

         this.itachiSideDashCD = (int)(30.0F * this.configCooldownMultiplier);
      }

      private void itachiMoveToward(EntityLivingBase target, double speed) {
         if (this.isRiding() && this.itachiSusanooEntity != null) {
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > (double)1.5F) {
               double step = Math.min(0.15 * speed, dist - (double)1.0F);
               double nx = this.itachiSusanooEntity.posX + dx / dist * step;
               double nz = this.itachiSusanooEntity.posZ + dz / dist * step;
               double ny = this.itachiSusanooEntity.posY;
               double dy = target.posY - this.posY;
               if (dy > (double)0.5F) {
                  ny += Math.min(0.15, dy * 0.15);
               } else if (dy < (double)-0.5F) {
                  ny += Math.max(-0.15, dy * 0.15);
               }

               this.itachiSusanooEntity.setPosition(nx, ny, nz);
               this.itachiSusanooEntity.motionX = (double)0.0F;
               this.itachiSusanooEntity.motionY = (double)0.0F;
               this.itachiSusanooEntity.motionZ = (double)0.0F;
               this.faceEntity(target, 360.0F, 360.0F);
            }
         } else {
            this.getNavigator().tryMoveToEntityLiving(target, speed);
         }

      }

      private void itachiCastFireball(EntityLivingBase target) {
         this.itachiBroadcastRare(target, "§6Katon: Gokakyu no Jutsu!", 0.3);
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float normalDmg = baseDmg * 1.2F * (float)this.getDamageMultiplier();
         float trueDmg = 4.0F;
         EntityKatonFireball.EntityCustom fireball = new EntityKatonFireball.EntityCustom(this.world, this, normalDmg, trueDmg);
         fireball.setPosition(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
         fireball.shoot(target.posX - this.posX, target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight()), target.posZ - this.posZ, 1.2F, 2.0F);
         this.world.spawnEntity(fireball);
         this.itachiFireballCD = (int)((float)(this.itachiPhase >= 2 ? 80 : 120) * this.configCooldownMultiplier);
      }

      private void itachiCastPhoenixFlower(EntityLivingBase target) {
         this.itachiBroadcastRare(target, "§6Katon: Hosenka no Jutsu!", 0.4);
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float normalDmg = baseDmg * 0.6F * (float)this.getDamageMultiplier();
         float trueDmg = 2.0F;
         int count = this.itachiPhase >= 3 ? 5 : (this.itachiPhase >= 2 ? 4 : 3);

         for(int i = 0; i < count; ++i) {
            EntityKatonFireball.EntityCustom fb = new EntityKatonFireball.EntityCustom(this.world, this, normalDmg, trueDmg);
            fb.setPosition(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
            fb.shoot(target.posX - this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight()), target.posZ - this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, 1.0F, 4.0F);
            this.world.spawnEntity(fb);
         }

         this.itachiPhoenixCD = (int)((float)(this.itachiPhase >= 2 ? 100 : 140) * this.configCooldownMultiplier);
      }

      private void itachiShurikenBarrage(EntityLivingBase target) {
         int count = this.itachiPhase >= 2 ? 4 : 3;

         for(int i = 0; i < count; ++i) {
            this.throwKunai(target);
         }

         this.itachiShurikenCD = (int)(60.0F * this.configCooldownMultiplier);
      }

      private void itachiCastTsukuyomi(EntityLivingBase target) {
         this.itachiBroadcast(target, "§4Tsukuyomi!");
         target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 80, 0));
         target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 2));
         target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 80, 1));
         target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 100, 0));
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float dmg = baseDmg * (1.0F + (float)(this.itachiPhase - 1) * 0.2F) * (float)this.getDamageMultiplier();
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), dmg);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, target.posX, target.posY + (double)1.0F, target.posZ, 50, (double)1.0F, (double)1.0F, (double)1.0F, (double)0.5F, new int[0]);
         }

         this.itachiTsukuyomiCD = (int)(400.0F * this.configCooldownMultiplier);
      }

      private void itachiCastDemonicIllusion(EntityLivingBase target) {
         this.itachiBroadcastRare(target, "§5Demonic Illusion: Shackling Stakes!", (double)0.5F);
         target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
         target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0));
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float dmg = baseDmg * 0.7F * (float)this.getDamageMultiplier();
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), dmg);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + (double)1.0F, target.posZ, 30, (double)1.0F, (double)1.5F, (double)1.0F, 0.02, new int[0]);
         }

         this.itachiDemonicIllusionCD = (int)(120.0F * this.configCooldownMultiplier);
      }

      private void itachiCrowClone(EntityLivingBase target) {
         this.itachiBroadcast(target, "§8Crow Clone...");
         double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
         double cDist = (double)8.0F + this.rand.nextDouble() * (double)5.0F;
         double cx = this.posX + Math.cos(angle) * cDist;
         double cz = this.posZ + Math.sin(angle) * cDist;
         double cy = this.findSafeY(cx, this.posY, cz);
         if (cy < (double)0.0F) {
            cy = this.posY;
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX, this.posY + (double)1.0F, this.posZ, 40, (double)1.0F, (double)1.5F, (double)1.0F, (double)0.0F, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)1.0F, this.posZ, 25, 0.8, (double)1.0F, 0.8, 0.05, new int[0]);
         }

         this.itachiTeleport(cx, cy, cz);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
         }

         this.itachiCrowCloneCD = (int)(100.0F * this.configCooldownMultiplier);
      }

      private void itachiCastAmaterasu(EntityLivingBase target) {
         this.itachiBroadcast(target, "§4Amaterasu!");
         target.setFire(8);
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float dmg = baseDmg * (this.itachiPhase >= 3 ? 1.4F : 1.1F) * (float)this.getDamageMultiplier();
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), dmg);
         target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, target.posX, target.posY + (double)0.5F, target.posZ, 30, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.LAVA, target.posX, target.posY + (double)0.5F, target.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
         }

         this.itachiAmaterasuCD = (int)(300.0F * this.configCooldownMultiplier);
      }

      private void itachiCastPhoenixSageFire(EntityLivingBase target) {
         this.itachiBroadcast(target, "§6Itachi: Fire Style: Phoenix Sage Fire Technique!");

         for(int i = 0; i < 5; ++i) {
            EntityPhoenixSageFire.EntityCustom fireball = new EntityPhoenixSageFire.EntityCustom(this.world, this);
            double dx = target.posX - this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F;
            double dy = target.posY + (double)(target.height / 2.0F) - (this.posY + (double)this.getEyeHeight());
            double dz = target.posZ - this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F;
            fireball.shoot(dx, dy, dz, 1.2F, 3.0F);
            fireball.setPosition(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
            this.world.spawnEntity(fireball);
         }

         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), baseDmg * 0.3F);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)this.getEyeHeight(), this.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.05, new int[0]);
         }

         this.itachiPhoenixSageCD = (int)(240.0F * this.configCooldownMultiplier);
      }

      private void itachiCastFireDragon(EntityLivingBase target) {
         this.itachiBroadcast(target, "§4Itachi: Fire Style: Great Fire Dragon!");
         EntityKatonFireDragon.EntityCustom dragon = new EntityKatonFireDragon.EntityCustom(this.world, this);
         double dx = target.posX - this.posX;
         double dy = target.posY + (double)(target.height / 2.0F) - (this.posY + (double)this.getEyeHeight());
         double dz = target.posZ - this.posZ;
         dragon.shoot(dx, dy, dz, 0.8F, 1.0F);
         dragon.setPosition(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
         this.world.spawnEntity(dragon);
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), baseDmg * 0.5F);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.LAVA, this.posX, this.posY + (double)this.getEyeHeight(), this.posZ, 30, (double)1.0F, (double)1.0F, (double)1.0F, 0.08, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)this.getEyeHeight(), this.posZ, 40, (double)1.5F, (double)1.5F, (double)1.5F, 0.1, new int[0]);
         }

         this.itachiFireDragonCD = (int)(500.0F * this.configCooldownMultiplier);
      }

      private void itachiActivateYataMirror(EntityLivingBase target) {
         this.itachiYataMirrorActive = true;
         this.itachiYataMirrorTicks = 60;
         this.itachiYataMirrorCD = (int)(200.0F * this.configCooldownMultiplier);
         this.itachiBroadcast(target, "§6Yata Mirror!");
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.5F, this.posZ, 30, (double)1.0F, (double)1.0F, (double)1.0F, (double)0.5F, new int[0]);
         }

      }

      private void itachiTeleport(double x, double y, double z) {
         Entity mount = this.getRidingEntity();
         if (mount != null && mount == this.itachiSusanooEntity) {
            this.dismountRidingEntity();
            this.internalReposition = true;
            mount.setPositionAndUpdate(x, y, z);
            this.setPositionAndUpdate(x, y, z);
            this.internalReposition = false;
            this.startRiding(mount, true);
         } else {
            this.internalReposition = true;
            this.setPositionAndUpdate(x, y, z);
            this.internalReposition = false;
         }

      }

      private void itachiSpawnSusanoo() {
         if (!this.world.isRemote) {
            if (this.itachiSusanooEntity == null || !this.itachiSusanooEntity.isEntityAlive()) {
               try {
                  EntityItachiSusanoo.EntityCustom susanoo = new EntityItachiSusanoo.EntityCustom(this.world);
                  susanoo.setPosition(this.posX, this.posY, this.posZ);
                  susanoo.setOwnerEntity(this);
                  susanoo.setFlameColor(13369344);
                  susanoo.setFullBody(true);
                  float myMaxHp = this.getMaxHealth();
                  if (myMaxHp <= 5000.0F) {
                     susanoo.setSusanooHealth(myMaxHp * 0.4F);
                     susanoo.setSusanooDamage(15.0F);
                  } else if (myMaxHp <= 10000.0F) {
                     susanoo.setSusanooHealth(myMaxHp * 0.5F);
                     susanoo.setSusanooDamage(25.0F);
                  } else {
                     susanoo.setSusanooHealth(myMaxHp * 0.6F);
                     susanoo.setSusanooDamage(35.0F);
                  }

                  this.world.spawnEntity(susanoo);
                  this.startRiding(susanoo, true);
                  this.itachiSusanooEntity = susanoo;
               } catch (Exception e) {
                  System.err.println("[ItachiStyle] FAILED to spawn Susanoo: " + e.getMessage());
                  this.itachiSusanooActive = false;
                  this.itachiSusanooEntity = null;
               }

            }
         }
      }

      private void itachiCleanupSusanoo() {
         if (this.itachiSusanooEntity != null) {
            this.itachiSusanooEntity.setDead();
            this.itachiSusanooEntity = null;
         }

         if (this.isRiding()) {
            this.dismountRidingEntity();
         }

      }

      private void itachiBroadcast(EntityLivingBase target, String msg) {
         if (this.itachiChatCD <= 0) {
            this.itachiChatCD = 40;

            for(EntityPlayer p : this.world.playerEntities) {
               if ((double)p.getDistance(this) < (double)48.0F) {
                  ((EntityPlayerMP)p).sendMessage(new TextComponentString(msg));
               }
            }

         }
      }

      private void itachiBroadcastRare(EntityLivingBase target, String msg, double chance) {
         if (this.rand.nextDouble() < chance) {
            this.itachiBroadcast(target, msg);
         }

      }
   }
}
