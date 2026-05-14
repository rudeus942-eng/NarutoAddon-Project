
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
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
public class EntityKabutoShippuden extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 267;

   public EntityKabutoShippuden(ElementsInfTsukAddon instance) {
      super(instance, 267);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kabutoshippuden"), 267).name("kabutoshippuden").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KabutoShippudenRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class KabutoShippudenRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/kabuto1.png");

      public KabutoShippudenRenderer(RenderManager renderManager) {
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

         if (entity.getPhase() == 3) {
            float pulse = 0.85F + 0.15F * MathHelper.sin((float)entity.ticksExisted * 0.1F);
            GlStateManager.color(0.6F * pulse, 0.8F * pulse, 1.0F * pulse, 1.0F);
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
      private static final float PHASE_2_THRESHOLD = 0.7F;
      private static final float PHASE_3_THRESHOLD = 0.4F;
      private static final UUID PHASE3_SPEED_UUID = UUID.fromString("d4a5e3c1-7b2f-4c8e-9a1d-3f6e8b0c2d7a");
      private static final AttributeModifier PHASE3_SPEED_MODIFIER;
      private int phase = 1;
      private int attackTick = 0;
      private int meleeCD = 0;
      private int scalpelCD = 0;
      private int healCD = 0;
      private int poisonCD = 0;
      private int flickerCD = 0;
      private int comboStep = 0;
      private int comboDelay = 0;
      private EntityLivingBase comboTarget = null;
      private boolean isFlickering = false;
      private int flickerTicks = 0;
      private int healPulseTimer = 0;
      private boolean phase2Announced = false;
      private boolean phase3Announced = false;
      private boolean phase3SpeedApplied = false;

      public EntityCustom(World world) {
         super(world);
      }

      public int getPhase() {
         return this.phase;
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.checkPhaseTransition();
         ++this.attackTick;
         if (this.comboStep > 0) {
            this.processCombo(target, dist);
         } else {
            if (this.phase == 3 && this.attackTick % 10 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + 1.2, this.posZ, 6, 0.4, 0.6, 0.4, 0.05, new int[0]);
            }

            if (this.phase >= 2 && this.healCD <= 0) {
               this.performSelfHeal();
            } else {
               if (this.phase >= 2 && this.poisonCD <= 0 && dist <= (double)6.0F) {
                  int poisonInterval = this.phase == 3 ? 180 : 300;
                  if (this.attackTick % poisonInterval == 0) {
                     this.performPoisonMist();
                     return;
                  }
               }

               int comboInterval = this.phase == 3 ? 48 : 80;
               if (this.scalpelCD <= 0 && dist <= (double)3.0F && this.attackTick % comboInterval == 0) {
                  this.startScalpelCombo(target);
               } else if (dist > (double)8.0F && dist <= (double)25.0F && this.kunaiCooldown <= 0) {
                  this.performKunaiThrow(target);
               } else {
                  int meleeInterval = this.phase == 3 ? 18 : 30;
                  if (this.meleeCD <= 0 && dist <= (double)3.0F) {
                     this.performChakraScalpel(target);
                  } else {
                     if (dist > (double)3.0F) {
                        this.getNavigator().tryMoveToEntityLiving(target, this.phase == 3 ? 1.3 : (double)1.0F);
                     }

                     this.faceEntity(target, 30.0F, 30.0F);
                  }
               }
            }
         }
      }

      protected void tickStyleCooldowns() {
         if (this.meleeCD > 0) {
            --this.meleeCD;
         }

         if (this.scalpelCD > 0) {
            --this.scalpelCD;
         }

         if (this.healCD > 0) {
            --this.healCD;
         }

         if (this.poisonCD > 0) {
            --this.poisonCD;
         }

         if (this.flickerCD > 0) {
            --this.flickerCD;
         }

         if (this.comboDelay > 0) {
            --this.comboDelay;
         }

         if (this.healPulseTimer > 0) {
            --this.healPulseTimer;
         }

         if (this.flickerTicks > 0) {
            --this.flickerTicks;
            if (this.flickerTicks <= 0) {
               this.isFlickering = false;
            }
         }

      }

      protected void resetCombatState() {
         this.phase = 1;
         this.attackTick = 0;
         this.meleeCD = 0;
         this.scalpelCD = 0;
         this.healCD = 0;
         this.poisonCD = 0;
         this.flickerCD = 0;
         this.comboStep = 0;
         this.comboDelay = 0;
         this.comboTarget = null;
         this.isFlickering = false;
         this.flickerTicks = 0;
         this.healPulseTimer = 0;
         this.phase2Announced = false;
         this.phase3Announced = false;
         this.phase3SpeedApplied = false;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("kabPhase", this.phase);
         compound.setInteger("kabAttackTick", this.attackTick);
         compound.setInteger("kabMeleeCD", this.meleeCD);
         compound.setInteger("kabScalpelCD", this.scalpelCD);
         compound.setInteger("kabHealCD", this.healCD);
         compound.setInteger("kabPoisonCD", this.poisonCD);
         compound.setInteger("kabFlickerCD", this.flickerCD);
         compound.setInteger("kabHealPulseTimer", this.healPulseTimer);
         compound.setBoolean("kabPhase2Announced", this.phase2Announced);
         compound.setBoolean("kabPhase3Announced", this.phase3Announced);
         compound.setBoolean("kabPhase3SpeedApplied", this.phase3SpeedApplied);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.phase = compound.hasKey("kabPhase") ? compound.getInteger("kabPhase") : 1;
         this.attackTick = compound.hasKey("kabAttackTick") ? compound.getInteger("kabAttackTick") : 0;
         this.meleeCD = compound.hasKey("kabMeleeCD") ? compound.getInteger("kabMeleeCD") : 0;
         this.scalpelCD = compound.hasKey("kabScalpelCD") ? compound.getInteger("kabScalpelCD") : 0;
         this.healCD = compound.hasKey("kabHealCD") ? compound.getInteger("kabHealCD") : 0;
         this.poisonCD = compound.hasKey("kabPoisonCD") ? compound.getInteger("kabPoisonCD") : 0;
         this.flickerCD = compound.hasKey("kabFlickerCD") ? compound.getInteger("kabFlickerCD") : 0;
         this.healPulseTimer = compound.hasKey("kabHealPulseTimer") ? compound.getInteger("kabHealPulseTimer") : 0;
         this.phase2Announced = compound.getBoolean("kabPhase2Announced");
         this.phase3Announced = compound.getBoolean("kabPhase3Announced");
         this.phase3SpeedApplied = compound.getBoolean("kabPhase3SpeedApplied");
         if (this.phase3SpeedApplied && this.phase == 3) {
            this.applyPhase3SpeedBoost();
         }

      }

      protected float onStyleDamage(DamageSource source, float amount) {
         if (!this.world.isRemote && this.flickerCD <= 0 && !this.isFlickering) {
            Entity trueSource = source.getTrueSource();
            if (trueSource instanceof EntityLivingBase) {
               float dodgeChance = this.phase == 3 ? 0.35F : (this.phase == 2 ? 0.2F : 0.0F);
               if (dodgeChance > 0.0F && this.rand.nextFloat() < dodgeChance) {
                  this.performBodyFlicker((EntityLivingBase)trueSource);
                  return -1.0F;
               }
            }
         }

         return this.isFlickering ? -1.0F : amount;
      }

      protected void onCombatDeath() {
         this.removePhase3SpeedBoost();
         String name = this.hasCustomName() ? this.getCustomNameTag() : "Kabuto";
         this.broadcastChat((double)50.0F, "§a" + name + ": §7Impressive... You've exceeded my analysis.");
      }

      protected boolean usesVanillaMeleeAI() {
         return true;
      }

      private void checkPhaseTransition() {
         float healthPct = this.getHealth() / this.getMaxHealth();
         if (this.phase == 1 && healthPct <= 0.7F) {
            this.phase = 2;
            if (!this.phase2Announced) {
               this.phase2Announced = true;
               String name = this.hasCustomName() ? this.getCustomNameTag() : "Kabuto";
               this.broadcastChat((double)50.0F, "§a" + name + ": §2You're not bad... Time to get serious.");
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + (double)1.0F, this.posZ, 40, (double)1.5F, (double)1.0F, (double)1.5F, 0.1, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.HOSTILE, 0.8F, 1.2F);
            }
         } else if (this.phase == 2 && healthPct <= 0.4F) {
            this.phase = 3;
            if (!this.phase3Announced) {
               this.phase3Announced = true;
               String name = this.hasCustomName() ? this.getCustomNameTag() : "Kabuto";
               this.broadcastChat((double)50.0F, "§a" + name + ": §bChakra Enhanced Scalpel... I'll end this quickly!");
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.0F, this.posZ, 60, (double)2.0F, (double)1.5F, (double)2.0F, 0.15, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 0.6F, 1.5F);
               this.applyPhase3SpeedBoost();
            }
         }

      }

      private void applyPhase3SpeedBoost() {
         IAttributeInstance speedAttr = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
         if (speedAttr.getModifier(PHASE3_SPEED_UUID) == null) {
            speedAttr.applyModifier(PHASE3_SPEED_MODIFIER);
            this.phase3SpeedApplied = true;
         }

      }

      private void removePhase3SpeedBoost() {
         IAttributeInstance speedAttr = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
         speedAttr.removeModifier(PHASE3_SPEED_UUID);
         this.phase3SpeedApplied = false;
      }

      private void performChakraScalpel(EntityLivingBase target) {
         if (this.isIceDome(target)) {
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), 200.0F);
            this.swingArm(EnumHand.MAIN_HAND);
            this.meleeCD = 20;
         } else {
            float baseDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            float normalDmg = baseDamage * 0.7F * (float)dmgMul;
            float trueDmg = baseDamage * 0.3F * (float)dmgMul * this.trueDamageMultiplier;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            this.swingArm(EnumHand.MAIN_HAND);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, target.posX, target.posY + (double)target.height * (double)0.5F, target.posZ, 8, 0.3, 0.4, 0.3, 0.02, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 0.9F, 1.3F);
            int[] cdRange = this.getCooldownRange(this.phase == 3 ? 12 : (this.phase == 2 ? 18 : 25), this.phase == 3 ? 20 : (this.phase == 2 ? 28 : 35));
            this.meleeCD = cdRange[0] + this.rand.nextInt(cdRange[1] - cdRange[0] + 1);
         }
      }

      private void startScalpelCombo(EntityLivingBase target) {
         this.comboStep = 1;
         this.comboDelay = 0;
         this.comboTarget = target;
         this.performComboHit(target, 1);
         this.scalpelCD = this.phase == 3 ? 48 : 80;
      }

      private void processCombo(EntityLivingBase target, double dist) {
         if (this.comboTarget != null && this.comboTarget.isEntityAlive() && !((double)this.getDistance(this.comboTarget) > (double)5.0F)) {
            this.faceEntity(this.comboTarget, 30.0F, 30.0F);
            if ((double)this.getDistance(this.comboTarget) > (double)2.5F) {
               this.getNavigator().tryMoveToEntityLiving(this.comboTarget, 1.2);
            }

            if (this.comboDelay <= 0) {
               ++this.comboStep;
               if (this.comboStep <= 3) {
                  this.performComboHit(this.comboTarget, this.comboStep);
               } else {
                  this.comboStep = 0;
                  this.comboTarget = null;
               }
            }

         } else {
            this.comboStep = 0;
            this.comboTarget = null;
         }
      }

      private void performComboHit(EntityLivingBase target, int hitNum) {
         if ((double)this.getDistance(target) > (double)3.5F) {
            this.comboDelay = 4;
         } else {
            float baseDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            float normalDmg;
            float trueDmg;
            switch (hitNum) {
               case 1:
                  normalDmg = baseDamage * 0.8F * (float)dmgMul;
                  trueDmg = baseDamage * 0.2F * (float)dmgMul * this.trueDamageMultiplier;
                  this.comboDelay = 6;
                  break;
               case 2:
                  normalDmg = baseDamage * 0.5F * (float)dmgMul;
                  trueDmg = baseDamage * 0.5F * (float)dmgMul * this.trueDamageMultiplier;
                  this.comboDelay = 6;
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, target.posX, target.posY + (double)target.height * (double)0.5F, target.posZ, 12, 0.3, (double)0.5F, 0.3, 0.03, new int[0]);
                  }
                  break;
               case 3:
                  normalDmg = baseDamage * 0.6F * (float)dmgMul;
                  trueDmg = baseDamage * 0.4F * (float)dmgMul * this.trueDamageMultiplier;
                  this.comboDelay = 0;
                  double dx = target.posX - this.posX;
                  double dz = target.posZ - this.posZ;
                  double len = Math.sqrt(dx * dx + dz * dz);
                  if (len > 0.1) {
                     target.motionX += dx / len * 0.6;
                     target.motionY += (double)0.25F;
                     target.motionZ += dz / len * 0.6;
                     if (target instanceof EntityPlayerMP) {
                        target.velocityChanged = true;
                     }
                  }

                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, target.posX, target.posY + (double)1.0F, target.posZ, 3, 0.3, 0.2, 0.3, 0.05, new int[0]);
                  }
                  break;
               default:
                  return;
            }

            target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            this.swingArm(EnumHand.MAIN_HAND);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 0.8F, 1.1F + (float)hitNum * 0.1F);
         }
      }

      private void performKunaiThrow(EntityLivingBase target) {
         double dmgMult = this.getDamageMultiplier();
         float normalDmg = 5.0F * (float)dmgMult;
         float trueDmg = 2.0F * this.trueDamageMultiplier;
         EntityKunaiProjectile.EntityCustom kunai = new EntityKunaiProjectile.EntityCustom(this.world, this, normalDmg, trueDmg);
         double dx = target.posX - this.posX;
         double dy = target.posY + (double)target.getEyeHeight() - 0.1 - kunai.posY;
         double dz = target.posZ - this.posZ;
         double dist = Math.sqrt(dx * dx + dz * dz);
         kunai.shoot(dx, dy + dist * 0.15, dz, 1.6F, 2.0F);
         this.world.spawnEntity(kunai);
         this.swingArm(EnumHand.MAIN_HAND);
         int[] cdRange = this.getCooldownRange(50, 80);
         this.kunaiCooldown = cdRange[0] + this.rand.nextInt(cdRange[1] - cdRange[0] + 1);
      }

      private void performSelfHeal() {
         float healAmount = this.getMaxHealth() * 0.03F;
         this.heal(healAmount);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)1.0F, 0.8, (double)1.0F, 0.08, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.HOSTILE, 0.7F, 1.4F);
         String name = this.hasCustomName() ? this.getCustomNameTag() : "Kabuto";
         this.broadcastNearby((double)50.0F, "§a" + name + ": §2Healing Palm Technique!");
         int[] cdRange = this.getCooldownRange(1100, 1300);
         this.healCD = cdRange[0] + this.rand.nextInt(cdRange[1] - cdRange[0] + 1);
      }

      private void performPoisonMist() {
         for(EntityPlayer player : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)5.0F))) {
            if (player.isEntityAlive() && !player.isSpectator()) {
               player.addPotionEffect(new PotionEffect(MobEffects.POISON, 60, 0));
            }
         }

         if (this.world instanceof WorldServer) {
            for(int i = 0; i < 40; ++i) {
               double px = this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)10.0F;
               double py = this.posY + this.rand.nextDouble() * (double)2.0F;
               double pz = this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)10.0F;
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_MOB, px, py, pz, 0, 0.4, 0.8, 0.2, (double)1.0F, new int[0]);
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITCH_AMBIENT, SoundCategory.HOSTILE, 1.0F, 0.8F);
         String name = this.hasCustomName() ? this.getCustomNameTag() : "Kabuto";
         this.broadcastNearby((double)50.0F, "§a" + name + ": §5Poison Mist!");
         int[] cdRange = this.getCooldownRange(this.phase == 3 ? 180 : 260, this.phase == 3 ? 220 : 300);
         this.poisonCD = cdRange[0] + this.rand.nextInt(cdRange[1] - cdRange[0] + 1);
      }

      private void performBodyFlicker(EntityLivingBase attacker) {
         float aYaw = attacker.rotationYaw * ((float)Math.PI / 180F);
         double behindX = attacker.posX + Math.sin((double)aYaw) * ((double)4.0F + this.rand.nextDouble() * (double)2.0F);
         double behindZ = attacker.posZ - Math.cos((double)aYaw) * ((double)4.0F + this.rand.nextDouble() * (double)2.0F);
         double behindY = this.findSafeY(behindX, attacker.posY, behindZ);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, 0.6, (double)0.5F, 0.08, new int[0]);
         }

         if (this.world.isBlockLoaded(new BlockPos(behindX, behindY, behindZ)) && this.isPositionSafe(behindX, behindY, behindZ)) {
            this.internalReposition = true;
            this.setPositionAndUpdate(behindX, behindY, behindZ);
            this.internalReposition = false;
         } else {
            double dx = this.posX - attacker.posX;
            double dz = this.posZ - attacker.posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 0.1) {
               len = (double)1.0F;
            }

            double perpX;
            double perpZ;
            if (this.rand.nextBoolean()) {
               perpX = -dz / len;
               perpZ = dx / len;
            } else {
               perpX = dz / len;
               perpZ = -dx / len;
            }

            double sideX = this.posX + perpX * (double)5.0F;
            double sideZ = this.posZ + perpZ * (double)5.0F;
            double sideY = this.findSafeY(sideX, this.posY, sideZ);
            if (this.world.isBlockLoaded(new BlockPos(sideX, sideY, sideZ)) && this.isPositionSafe(sideX, sideY, sideZ)) {
               this.internalReposition = true;
               this.setPositionAndUpdate(sideX, sideY, sideZ);
               this.internalReposition = false;
            }
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.4, (double)0.5F, 0.4, 0.06, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.7F, 1.3F);
         this.isFlickering = true;
         this.flickerTicks = 5;
         this.flickerCD = this.phase == 3 ? 40 : 60;
      }

      private int getAdjustedCooldown(int baseCooldown) {
         return this.phase == 3 ? (int)((float)baseCooldown * 0.6F) : baseCooldown;
      }

      static {
         PHASE3_SPEED_MODIFIER = new AttributeModifier(PHASE3_SPEED_UUID, "kabutoShippudenPhase3Speed", 0.3, 2);
      }
   }
}
