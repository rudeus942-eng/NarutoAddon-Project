
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
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
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
public class EntitySasukeCurseBoss extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 244;

   public EntitySasukeCurseBoss(ElementsInfTsukAddon instance) {
      super(instance, 244);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "sasukecurseboss"), 244).name("sasukecurseboss").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SasukeCurseRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class SasukeCurseRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public SasukeCurseRenderer(RenderManager renderManager) {
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
      private int sasukeCurseChidoriCD = 0;
      private int sasukeCurseFireballCD = 0;
      private int sasukeCurseCounterCD = 0;
      private int sasukeCurseComboCD = 0;
      private int sasukeCurseMeleeCD = 0;
      private boolean sasukeCursePhase2 = false;
      private int sasukeCurseComboStep = 0;
      private int sasukeCurseComboDelay = 0;
      private EntityLivingBase sasukeCurseComboTarget = null;
      private boolean sasukeCurseIntroPlayed = false;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (target != null && target.isEntityAlive()) {
            this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            if (!this.sasukeCurseIntroPlayed) {
               this.sasukeCurseIntroPlayed = true;
               this.sasukeCurseSay("§4I'm going to sever those bonds...");
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)1.0F, (double)1.0F, (double)1.0F, (double)0.5F, new int[0]);
               }
            }

            if (this.sasukeCurseComboStep <= 0 || this.sasukeCurseComboTarget == null) {
               double dmgMul = this.getDamageMultiplier();
               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               float cdMul = this.sasukeCursePhase2 ? 0.75F : 1.0F;
               if (this.sasukeCurseChidoriCD <= 0 && dist >= (double)3.0F && dist <= (double)25.0F && this.rand.nextFloat() < 0.22F) {
                  int[] cd = this.getCooldownRange((int)(120.0F * cdMul), (int)(180.0F * cdMul));
                  this.sasukeCurseChidoriCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  if (this.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)this.world;
                     double handX = this.posX - Math.sin(Math.toRadians((double)this.rotationYaw)) * 0.4;
                     double handY = this.posY + 0.85;
                     double handZ = this.posZ + Math.cos(Math.toRadians((double)this.rotationYaw)) * 0.4;
                     ws.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, handX, handY, handZ, 12, 0.08, 0.08, 0.08, 0.02, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, handX, handY, handZ, 8, 0.05, 0.1, 0.05, 0.01, new int[0]);

                     for(int arc = 0; arc < 4; ++arc) {
                        double arcX = handX + (this.rand.nextDouble() - (double)0.5F) * 0.3;
                        double arcY = handY + (this.rand.nextDouble() - (double)0.5F) * (double)0.25F;
                        double arcZ = handZ + (this.rand.nextDouble() - (double)0.5F) * 0.3;
                        ws.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, arcX, arcY, arcZ, 3, 0.02, 0.02, 0.02, 0.01, new int[0]);
                     }
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 1.0F, 2.0F);
                  this.sasukeCurseSay("§bChidori!");
                  float tYaw = target.rotationYaw * ((float)Math.PI / 180F);
                  double dashX = target.posX + Math.sin((double)tYaw) * (double)1.5F;
                  double dashZ = target.posZ - Math.cos((double)tYaw) * (double)1.5F;
                  double dashY = this.findSafeY(dashX, target.posY, dashZ);
                  if (this.world.isBlockLoaded(new BlockPos(dashX, dashY, dashZ)) && this.isPositionSafe(dashX, dashY, dashZ)) {
                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.4, (double)0.5F, 0.4, 0.06, new int[0]);
                     }

                     this.internalReposition = true;
                     this.setPositionAndUpdate(dashX, dashY, dashZ);
                     this.internalReposition = false;
                     if (this.world instanceof WorldServer) {
                        WorldServer ws = (WorldServer)this.world;
                        ws.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY + 0.9, this.posZ, 20, 0.15, 0.2, 0.15, 0.06, new int[0]);
                        ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.3, 0.3, 0.3, 0.1, new int[0]);
                     }
                  }

                  float chidoriNormal = baseDmg * 1.5F * (float)dmgMul;
                  float chidoriTrue = baseDmg * 0.8F;
                  target.attackEntityFrom(DamageSource.causeMobDamage(this), chidoriNormal);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.MAGIC, chidoriTrue);
                  this.swingArm(EnumHand.MAIN_HAND);
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 0.6F, 1.8F);
               } else if (this.sasukeCurseFireballCD <= 0 && dist >= (double)6.0F && dist <= (double)15.0F && this.rand.nextFloat() < 0.2F) {
                  int[] cd = this.getCooldownRange((int)(80.0F * cdMul), (int)(120.0F * cdMul));
                  this.sasukeCurseFireballCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  float fireNormal = baseDmg * 0.8F * (float)dmgMul;
                  float fireTrue = baseDmg * 0.4F * this.trueDamageMultiplier;
                  EntityKatonFireball.EntityCustom fireball = new EntityKatonFireball.EntityCustom(this.world, this, fireNormal, fireTrue);
                  double dx = target.posX - this.posX;
                  double dy = target.posY + (double)target.getEyeHeight() - 0.1 - fireball.posY;
                  double dz = target.posZ - this.posZ;
                  double d = Math.sqrt(dx * dx + dz * dz);
                  fireball.shoot(dx, dy + d * 0.08, dz, 1.2F, 2.0F);
                  this.world.spawnEntity(fireball);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + 1.2, this.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.15, new int[0]);
                  }

                  this.sasukeCurseSay("§6Katon: Goukakyuu no Jutsu!");
                  this.swingArm(EnumHand.MAIN_HAND);
               } else if (this.sasukeCurseComboCD <= 0 && dist <= (double)3.0F && this.rand.nextFloat() < 0.22F) {
                  int[] cd = this.getCooldownRange((int)(100.0F * cdMul), (int)(140.0F * cdMul));
                  this.sasukeCurseComboCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  float kickNormal = baseDmg * 0.5F * (float)dmgMul;
                  float kickTrue = this.combatTier >= 4 ? 4.0F : 2.0F;
                  target.attackEntityFrom(DamageSource.causeMobDamage(this), kickNormal);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.MAGIC, kickTrue);
                  this.swingArm(EnumHand.MAIN_HAND);
                  this.sasukeCurseComboStep = 1;
                  this.sasukeCurseComboDelay = 6;
                  this.sasukeCurseComboTarget = target;
                  this.sasukeCurseSay("§4Shishi Rendan!");
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, target.posX, target.posY + (double)1.0F, target.posZ, 3, 0.3, 0.2, 0.3, 0.1, new int[0]);
                  }

               } else {
                  if (this.sasukeCurseMeleeCD <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
                     float normalDmg;
                     float trueDmg;
                     if (this.trueDamageSplit > 0.0F) {
                        normalDmg = baseDmg * (1.0F - this.trueDamageSplit) * (float)dmgMul;
                        trueDmg = baseDmg * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
                     } else {
                        normalDmg = baseDmg * (float)dmgMul;
                        trueDmg = (this.combatTier >= 4 ? 8.0F : (this.combatTier >= 3 ? 5.0F : (this.combatTier >= 2 ? 3.5F : 2.0F))) * this.trueDamageMultiplier;
                     }

                     target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                     target.hurtResistantTime = 0;
                     target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                     this.swingArm(EnumHand.MAIN_HAND);
                     int[] cd = this.getCooldownRange(15, 20);
                     this.sasukeCurseMeleeCD = cd[0] + this.rand.nextInt(cd[1] - cd[0] + 1);
                  }

               }
            }
         }
      }

      protected void tickStyleCooldowns() {
         if (this.sasukeCurseChidoriCD > 0) {
            --this.sasukeCurseChidoriCD;
         }

         if (this.sasukeCurseFireballCD > 0) {
            --this.sasukeCurseFireballCD;
         }

         if (this.sasukeCurseCounterCD > 0) {
            --this.sasukeCurseCounterCD;
         }

         if (this.sasukeCurseComboCD > 0) {
            --this.sasukeCurseComboCD;
         }

         if (this.sasukeCurseMeleeCD > 0) {
            --this.sasukeCurseMeleeCD;
         }

         if (this.sasukeCurseComboStep > 0 && this.sasukeCurseComboTarget != null) {
            if (this.sasukeCurseComboDelay > 0) {
               --this.sasukeCurseComboDelay;
            } else {
               this.processSasukeCurseComboHit();
            }
         }

         if (this.sasukeCursePhase2 && this.world instanceof WorldServer && this.ticksExisted % 4 == 0) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX + (this.rand.nextDouble() - (double)0.5F) * 1.2, this.posY + (double)0.5F + this.rand.nextDouble() * (double)1.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 1.2, 3, 0.2, 0.3, 0.2, 0.1, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, this.posY + this.rand.nextDouble() * 1.8, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, 2, 0.3, (double)0.0F, (double)0.5F, (double)1.0F, new int[0]);
         }

      }

      protected void resetCombatState() {
         this.sasukeCursePhase2 = false;
         this.sasukeCurseIntroPlayed = false;
         this.sasukeCurseChidoriCD = 0;
         this.sasukeCurseFireballCD = 0;
         this.sasukeCurseCounterCD = 0;
         this.sasukeCurseComboCD = 0;
         this.sasukeCurseMeleeCD = 0;
         this.sasukeCurseComboStep = 0;
         this.sasukeCurseComboTarget = null;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("sasukeCursePhase2", this.sasukeCursePhase2);
         compound.setBoolean("sasukeCurseIntroPlayed", this.sasukeCurseIntroPlayed);
         compound.setInteger("sasukeCurseChidoriCD", this.sasukeCurseChidoriCD);
         compound.setInteger("sasukeCurseFireballCD", this.sasukeCurseFireballCD);
         compound.setInteger("sasukeCurseCounterCD", this.sasukeCurseCounterCD);
         compound.setInteger("sasukeCurseComboCD", this.sasukeCurseComboCD);
         compound.setInteger("sasukeCurseMeleeCD", this.sasukeCurseMeleeCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.sasukeCursePhase2 = compound.getBoolean("sasukeCursePhase2");
         this.sasukeCurseIntroPlayed = compound.getBoolean("sasukeCurseIntroPlayed");
         this.sasukeCurseChidoriCD = compound.hasKey("sasukeCurseChidoriCD") ? compound.getInteger("sasukeCurseChidoriCD") : 0;
         this.sasukeCurseFireballCD = compound.hasKey("sasukeCurseFireballCD") ? compound.getInteger("sasukeCurseFireballCD") : 0;
         this.sasukeCurseCounterCD = compound.hasKey("sasukeCurseCounterCD") ? compound.getInteger("sasukeCurseCounterCD") : 0;
         this.sasukeCurseComboCD = compound.hasKey("sasukeCurseComboCD") ? compound.getInteger("sasukeCurseComboCD") : 0;
         this.sasukeCurseMeleeCD = compound.hasKey("sasukeCurseMeleeCD") ? compound.getInteger("sasukeCurseMeleeCD") : 0;
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         Entity trueSource = source.getTrueSource();
         if (!this.world.isRemote && this.sasukeCurseCounterCD <= 0 && trueSource instanceof EntityLivingBase && (double)this.getDistance(trueSource) <= (double)4.0F) {
            float counterChance = this.sasukeCursePhase2 ? 0.15F : 0.1F;
            if (this.rand.nextFloat() < counterChance) {
               EntityLivingBase attacker = (EntityLivingBase)trueSource;
               float aYaw = attacker.rotationYaw * ((float)Math.PI / 180F);
               double behindX = attacker.posX + Math.sin((double)aYaw) * (double)2.0F;
               double behindZ = attacker.posZ - Math.cos((double)aYaw) * (double)2.0F;
               double behindY = this.findSafeY(behindX, this.posY, behindZ);
               if (this.world.isBlockLoaded(new BlockPos(behindX, behindY, behindZ)) && this.isPositionSafe(behindX, behindY, behindZ)) {
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 12, 0.3, (double)0.5F, 0.3, 0.02, new int[0]);
                  }

                  this.internalReposition = true;
                  this.setPositionAndUpdate(behindX, behindY, behindZ);
                  this.internalReposition = false;
               }

               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               float counterNormal = baseDmg * 0.6F * (float)this.getDamageMultiplier();
               float counterTrue = 4.0F;
               attacker.attackEntityFrom(DamageSource.causeMobDamage(this), counterNormal);
               attacker.hurtResistantTime = 0;
               attacker.attackEntityFrom(DamageSource.MAGIC, counterTrue);
               this.swingArm(EnumHand.MAIN_HAND);
               this.sasukeCurseCounterCD = 200;
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.4, (double)0.5F, 0.4, 0.15, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.8F, 1.5F);
               if (attacker instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)attacker).sendMessage(new TextComponentString("§c§o* Sasuke reads your movements with the Sharingan... *"));
               }

               return -1.0F;
            }
         }

         if (!this.world.isRemote && !this.sasukeCursePhase2 && this.getHealth() / this.getMaxHealth() < 0.4F) {
            this.sasukeCursePhase2 = true;
            IAttributeInstance speedAttr = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            speedAttr.setBaseValue(speedAttr.getBaseValue() * 1.3);
            IAttributeInstance dmgAttr = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
            dmgAttr.setBaseValue(dmgAttr.getBaseValue() * 1.4);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)1.5F, this.posZ, 60, (double)1.5F, (double)1.5F, (double)1.5F, (double)0.5F, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX, this.posY + (double)1.0F, this.posZ, 40, (double)1.0F, (double)1.0F, (double)1.0F, (double)1.0F, new int[0]);
            }

            this.sasukeCurseSay("§5I'll show you real power...");
         }

         return amount;
      }

      private void processSasukeCurseComboHit() {
         if (this.sasukeCurseComboTarget != null && this.sasukeCurseComboTarget.isEntityAlive() && !(this.getDistanceSq(this.sasukeCurseComboTarget) > (double)25.0F)) {
            float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            if (this.sasukeCurseComboStep == 1) {
               float punchNormal;
               float punchTrue;
               if (this.trueDamageSplit > 0.0F) {
                  punchNormal = baseDmg * 0.6F * (1.0F - this.trueDamageSplit) * (float)dmgMul;
                  punchTrue = baseDmg * 0.6F * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
               } else {
                  punchNormal = baseDmg * 0.6F * (float)dmgMul;
                  punchTrue = this.combatTier >= 4 ? 5.0F : 2.5F;
               }

               this.sasukeCurseComboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), punchNormal);
               this.sasukeCurseComboTarget.hurtResistantTime = 0;
               this.sasukeCurseComboTarget.attackEntityFrom(DamageSource.MAGIC, punchTrue);
               this.swingArm(EnumHand.MAIN_HAND);
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.1F);
               this.sasukeCurseComboStep = 2;
               this.sasukeCurseComboDelay = 6;
            } else if (this.sasukeCurseComboStep == 2) {
               float heavyNormal;
               float heavyTrue;
               if (this.trueDamageSplit > 0.0F) {
                  float heavySplit = Math.min(1.0F, this.trueDamageSplit + 0.1F);
                  heavyNormal = baseDmg * 1.2F * (1.0F - heavySplit) * (float)dmgMul;
                  heavyTrue = baseDmg * 1.2F * heavySplit * (float)dmgMul * this.trueDamageMultiplier;
               } else {
                  heavyNormal = baseDmg * 1.2F * (float)dmgMul;
                  heavyTrue = this.combatTier >= 4 ? 8.0F : 4.0F;
               }

               this.sasukeCurseComboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), heavyNormal);
               this.sasukeCurseComboTarget.hurtResistantTime = 0;
               this.sasukeCurseComboTarget.attackEntityFrom(DamageSource.MAGIC, heavyTrue);
               this.swingArm(EnumHand.MAIN_HAND);
               double kbX = this.sasukeCurseComboTarget.posX - this.posX;
               double kbZ = this.sasukeCurseComboTarget.posZ - this.posZ;
               double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
               if (kbDist > 0.01) {
                  EntityLivingBase var10000 = this.sasukeCurseComboTarget;
                  var10000.motionX += kbX / kbDist * 0.7;
                  var10000 = this.sasukeCurseComboTarget;
                  var10000.motionY += (double)0.25F;
                  var10000 = this.sasukeCurseComboTarget;
                  var10000.motionZ += kbZ / kbDist * 0.7;
                  this.sasukeCurseComboTarget.velocityChanged = true;
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.2F, 0.8F);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.sasukeCurseComboTarget.posX, this.sasukeCurseComboTarget.posY + (double)1.0F, this.sasukeCurseComboTarget.posZ, 15, 0.4, (double)0.5F, 0.4, 0.2, new int[0]);
               }

               this.sasukeCurseComboStep = 0;
               this.sasukeCurseComboTarget = null;
            }

         } else {
            this.sasukeCurseComboStep = 0;
            this.sasukeCurseComboTarget = null;
         }
      }

      private void sasukeCurseSay(String msg) {
         if (this.chatCooldown <= 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
               p.sendMessage(new TextComponentString("§4" + this.getCustomNameTag() + ": §c" + msg));
            }

            this.chatCooldown = 80;
         }
      }
   }
}
