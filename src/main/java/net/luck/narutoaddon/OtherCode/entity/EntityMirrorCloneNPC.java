
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
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityMirrorCloneNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 262;

   public EntityMirrorCloneNPC(ElementsInfTsukAddon instance) {
      super(instance, 262);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "mirrorclone"), 262).name("mirrorclone").tracker(64, 3, true).egg(-1, -1).build());
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
      private int mirrorMeleeCD = 0;
      private int mirrorDashCD = 0;
      private int mirrorDodgeCD = 0;
      private int mirrorComboStep = 0;
      private int mirrorComboDelay = 0;
      private EntityLivingBase mirrorComboTarget = null;
      private boolean mirrorAggressive = false;
      private int mirrorBlockCD = 0;
      private boolean mirrorBlockActive = false;
      private int mirrorBlockTicks = 0;
      private int mirrorCloneCD = 0;
      private int mirrorRasenganCD = 0;
      private int mirrorSubstitutionCD = 0;
      private int mirrorKunaiCD = 0;
      private List<Entity> mirrorSpawnedClones = new ArrayList();
      private static final int MIRROR_CLONE_CD_BASE = 400;
      private static final int MIRROR_RASENGAN_CD_BASE = 200;
      private static final int MIRROR_SUBSTITUTION_CD_BASE = 300;
      private static final float MIRROR_AGGRO_THRESHOLD = 0.5F;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         float hpFrac = this.getHealth() / this.getMaxHealth();
         if (!this.mirrorAggressive && hpFrac <= 0.5F) {
            this.mirrorAggressive = true;
            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               double curSpeed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(curSpeed * (double)1.25F);
            }

            this.broadcastChat((double)30.0F, "§7[§b" + this.getCustomNameTag() + "§7] §fEnough holding back...");
         }

         this.mirrorCleanupClones();
         if (this.mirrorComboStep > 0 && this.mirrorComboDelay <= 0 && this.mirrorComboTarget != null) {
            this.processMirrorCombo();
         } else if (this.mirrorCloneCD <= 0 && !this.getEntityData().getBoolean("mirrorClone")) {
            this.mirrorSpawnClones(target);
         } else if (this.mirrorRasenganCD <= 0) {
            this.performMirrorRasengan(target);
         } else if (this.mirrorKunaiCD > 0) {
            if (this.mirrorDashCD <= 0 && dist >= (double)1.5F && dist <= (double)12.0F && this.rand.nextFloat() < (this.mirrorAggressive ? 0.25F : 0.12F)) {
               this.performMirrorDash(target);
            } else {
               if (this.mirrorMeleeCD <= 0 && dist <= (double)3.0F) {
                  this.performMeleeSwing(target);
                  this.mirrorMeleeCD = this.mirrorAggressive ? 8 + this.rand.nextInt(6) : 14 + this.rand.nextInt(8);
                  this.meleeCooldown = this.mirrorMeleeCD;
                  if (this.mirrorComboStep == 0 && this.rand.nextFloat() < (this.mirrorAggressive ? 0.55F : 0.35F)) {
                     this.mirrorComboStep = 1;
                     this.mirrorComboDelay = this.mirrorAggressive ? 5 : 7;
                     this.mirrorComboTarget = target;
                  }
               }

            }
         } else {
            this.mirrorKunaiCD = this.cdMul(40);
            float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            float total = baseDmg * 0.8F * (float)dmgMul;
            float nDmg = this.trueDamageSplit > 0.0F ? total * (1.0F - this.trueDamageSplit) : total;
            float tDmg = this.trueDamageSplit > 0.0F ? total * this.trueDamageSplit * this.trueDamageMultiplier : 3.0F;
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
            double dz = target.posZ - this.posZ;

            for(int k = 0; k < 3; ++k) {
               EntityKunaiProjectile.EntityCustom kunai = new EntityKunaiProjectile.EntityCustom(this.world, this, nDmg, tDmg);
               double dist2 = Math.sqrt(dx * dx + dz * dz);
               kunai.shoot(dx, dy + dist2 * 0.15, dz, 1.8F, 3.0F);
               this.world.spawnEntity(kunai);
            }

            this.swingArm(EnumHand.MAIN_HAND);
         }
      }

      protected void tickStyleCooldowns() {
         if (this.mirrorMeleeCD > 0) {
            --this.mirrorMeleeCD;
         }

         if (this.mirrorDashCD > 0) {
            --this.mirrorDashCD;
         }

         if (this.mirrorDodgeCD > 0) {
            --this.mirrorDodgeCD;
         }

         if (this.mirrorComboDelay > 0) {
            --this.mirrorComboDelay;
         }

         if (this.mirrorBlockCD > 0) {
            --this.mirrorBlockCD;
         }

         if (this.mirrorCloneCD > 0) {
            --this.mirrorCloneCD;
         }

         if (this.mirrorRasenganCD > 0) {
            --this.mirrorRasenganCD;
         }

         if (this.mirrorSubstitutionCD > 0) {
            --this.mirrorSubstitutionCD;
         }

         if (this.mirrorKunaiCD > 0) {
            --this.mirrorKunaiCD;
         }

         if (this.mirrorBlockTicks > 0) {
            --this.mirrorBlockTicks;
            if (this.mirrorBlockTicks <= 0) {
               this.mirrorBlockActive = false;
            }
         }

      }

      protected void resetCombatState() {
         this.mirrorAggressive = false;
         this.mirrorComboStep = 0;
         this.mirrorBlockActive = false;
         this.mirrorCloneCD = 0;
         this.mirrorRasenganCD = 0;
         this.mirrorSubstitutionCD = 0;
         this.mirrorMeleeCD = 0;
         this.mirrorDashCD = 0;
         this.mirrorDodgeCD = 0;
         this.mirrorComboDelay = 0;
         this.mirrorComboTarget = null;
         this.mirrorBlockCD = 0;
         this.mirrorBlockTicks = 0;
         this.mirrorKunaiCD = 0;
         this.mirrorSpawnedClones.clear();
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("mirrorAggressive", this.mirrorAggressive);
         compound.setInteger("mirrorMeleeCD", this.mirrorMeleeCD);
         compound.setInteger("mirrorDashCD", this.mirrorDashCD);
         compound.setInteger("mirrorDodgeCD", this.mirrorDodgeCD);
         compound.setInteger("mirrorBlockCD", this.mirrorBlockCD);
         compound.setInteger("mirrorCloneCD", this.mirrorCloneCD);
         compound.setInteger("mirrorRasenganCD", this.mirrorRasenganCD);
         compound.setInteger("mirrorSubstitutionCD", this.mirrorSubstitutionCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.mirrorAggressive = compound.getBoolean("mirrorAggressive");
         this.mirrorMeleeCD = compound.hasKey("mirrorMeleeCD") ? compound.getInteger("mirrorMeleeCD") : 0;
         this.mirrorDashCD = compound.hasKey("mirrorDashCD") ? compound.getInteger("mirrorDashCD") : 0;
         this.mirrorDodgeCD = compound.hasKey("mirrorDodgeCD") ? compound.getInteger("mirrorDodgeCD") : 0;
         this.mirrorBlockCD = compound.hasKey("mirrorBlockCD") ? compound.getInteger("mirrorBlockCD") : 0;
         this.mirrorCloneCD = compound.hasKey("mirrorCloneCD") ? compound.getInteger("mirrorCloneCD") : 0;
         this.mirrorRasenganCD = compound.hasKey("mirrorRasenganCD") ? compound.getInteger("mirrorRasenganCD") : 0;
         this.mirrorSubstitutionCD = compound.hasKey("mirrorSubstitutionCD") ? compound.getInteger("mirrorSubstitutionCD") : 0;
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         Entity trueSource = source.getTrueSource();
         if (!this.world.isRemote && trueSource instanceof EntityLivingBase && this.mirrorSubstitutionCD <= 0 && amount > 20.0F && this.rand.nextFloat() < 0.25F) {
            this.mirrorSubstitutionCD = this.cdMul(300);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, 0.8, (double)0.5F, 0.03, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.HOSTILE, 1.0F, 0.8F);
            double behindDist = (double)2.5F;
            double attackerYaw = Math.toRadians((double)trueSource.rotationYaw);
            double newX = trueSource.posX + Math.sin(attackerYaw) * behindDist;
            double newZ = trueSource.posZ - Math.cos(attackerYaw) * behindDist;
            this.internalReposition = true;
            this.setPositionAndUpdate(newX, trueSource.posY, newZ);
            this.internalReposition = false;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, newX, trueSource.posY + (double)1.0F, newZ, 8, 0.2, 0.3, 0.2, 0.02, new int[0]);
            }

            return -1.0F;
         } else if (!this.mirrorAggressive && !this.world.isRemote && this.mirrorDodgeCD <= 0 && this.rand.nextFloat() < 0.2F) {
            this.mirrorDodgeCD = 80;
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double newX = this.posX + Math.cos(angle) * (double)3.0F;
            double newZ = this.posZ + Math.sin(angle) * (double)3.0F;
            this.internalReposition = true;
            this.setPositionAndUpdate(newX, this.posY, newZ);
            this.internalReposition = false;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)1.0F, this.posZ, 8, 0.3, (double)0.5F, 0.3, 0.02, new int[0]);
            }

            return -1.0F;
         } else {
            return amount;
         }
      }

      protected void onCombatDeath() {
         for(Entity clone : this.mirrorSpawnedClones) {
            if (clone != null && clone.isEntityAlive()) {
               clone.setDead();
            }
         }

         this.mirrorSpawnedClones.clear();
      }

      private void mirrorSpawnClones(EntityLivingBase target) {
         this.mirrorCloneCD = this.cdMul(400);
         int cloneCount = 1 + this.rand.nextInt(2);

         for(int i = 0; i < cloneCount; ++i) {
            double angle = (Math.PI * 2D) / (double)cloneCount * (double)i + this.rand.nextDouble() * (double)0.5F;
            double sx = this.posX + Math.cos(angle) * (double)3.0F;
            double sz = this.posZ + Math.sin(angle) * (double)3.0F;
            Entity raw = EntityList.createEntityByIDFromName(new ResourceLocation("inftsukaddon", "mirrorclone"), this.world);
            if (raw instanceof QuestNpcBase) {
               QuestNpcBase clone = (QuestNpcBase)raw;
               clone.setPosition(sx, this.posY, sz);
               String configId = this.getNpcConfigId();
               if (configId != null && !configId.isEmpty()) {
                  NpcConfig cloneCfg = NpcConfigRegistry.get(configId);
                  if (cloneCfg != null) {
                     clone.applyNpcConfig(cloneCfg);
                  }

                  clone.setCustomNameTag(this.getCustomNameTag() + " (Clone)");
                  clone.setAlwaysRenderNameTag(true);
               }

               clone.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)this.getMaxHealth() * (double)0.5F);
               clone.setHealth(clone.getMaxHealth());
               clone.enablePersistence();
               NBTTagCompound cloneData = clone.getEntityData();
               cloneData.setBoolean("questEntity", true);
               cloneData.setBoolean("mirrorClone", true);
               String ownerStr = this.getEntityData().getString("ownerUUID");
               if (!ownerStr.isEmpty()) {
                  cloneData.setString("ownerUUID", ownerStr);
               }

               String questId = this.getEntityData().getString("sharedQuestId");
               if (questId != null && !questId.isEmpty()) {
                  cloneData.setString("sharedQuestId", questId);
               }

               this.world.spawnEntity(clone);
               if (target != null) {
                  clone.setAttackTarget(target);
               }

               this.mirrorSpawnedClones.add(clone);
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, sx, this.posY + (double)1.0F, sz, 15, 0.4, 0.6, 0.4, 0.03, new int[0]);
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.8F, 1.2F);
      }

      private void mirrorCleanupClones() {
         Iterator<Entity> it = this.mirrorSpawnedClones.iterator();

         while(it.hasNext()) {
            Entity clone = (Entity)it.next();
            if (clone != null && !clone.isDead && clone.isEntityAlive()) {
               if (clone.ticksExisted > 200) {
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, clone.posX, clone.posY + (double)1.0F, clone.posZ, 10, 0.3, (double)0.5F, 0.3, 0.02, new int[0]);
                  }

                  clone.setDead();
                  it.remove();
               }
            } else {
               it.remove();
            }
         }

      }

      private void performMirrorRasengan(EntityLivingBase target) {
         this.mirrorRasenganCD = this.cdMul(200);
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double dDist = Math.sqrt(dx * dx + dz * dz);
         if (!(dDist < 0.1)) {
            double speed = (double)2.0F;
            this.isDashing = true;
            this.dashVelX = dx / dDist * speed;
            this.dashVelZ = dz / dDist * speed;
            this.dashTicksRemaining = Math.min(8, (int)(dDist / speed));
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int i = 0; i < 15; ++i) {
                  double angle = (double)i / (double)15.0F * Math.PI * (double)4.0F;
                  double px = this.posX + dx / dDist * (double)i * 0.4;
                  double pz = this.posZ + dz / dDist * (double)i * 0.4;
                  ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, px + Math.cos(angle) * 0.3, this.posY + 0.9, pz + Math.sin(angle) * 0.3, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, px, this.posY + (double)1.0F, pz, 2, 0.15, 0.15, 0.15, 0.05, new int[0]);
               }
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.6F, 1.8F);
            float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            float rasenTotal = baseDmg * 2.0F * (float)dmgMul;
            float trueDmg;
            float normDmg;
            if (this.trueDamageSplit > 0.0F) {
               float rasenSplit = Math.min(1.0F, this.trueDamageSplit + 0.15F);
               normDmg = rasenTotal * (1.0F - rasenSplit);
               trueDmg = rasenTotal * rasenSplit * this.trueDamageMultiplier;
            } else {
               normDmg = rasenTotal;
               trueDmg = baseDmg * 0.5F * this.trueDamageMultiplier;
            }

            if (!this.world.isRemote) {
               EntityRasenganProjectile.EntityCustom rasengan = new EntityRasenganProjectile.EntityCustom(this.world, this, normDmg, trueDmg);
               double rdx = target.posX - this.posX;
               double rdy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
               double rdz = target.posZ - this.posZ;
               rasengan.shoot(rdx, rdy, rdz, 1.8F, 1.0F);
               this.world.spawnEntity(rasengan);
            }

            if ((double)this.getDistance(target) <= (double)5.0F) {
               target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
               target.hurtResistantTime = 0;
               target.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), trueDmg);
               double kbx = dx / dDist * 1.2;
               double kbz = dz / dDist * 1.2;
               target.motionX += kbx;
               target.motionY += 0.3;
               target.motionZ += kbz;
               target.velocityChanged = true;
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 20, 0.4, (double)0.5F, 0.4, 0.15, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + (double)1.0F, target.posZ, 10, 0.3, 0.3, 0.3, 0.05, new int[0]);
               }
            }

            this.swingArm(EnumHand.MAIN_HAND);
         }
      }

      private void performMirrorDash(EntityLivingBase target) {
         this.mirrorDashCD = 60 + this.rand.nextInt(40);
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double dDist = Math.sqrt(dx * dx + dz * dz);
         if (!(dDist < 0.1)) {
            double speed = (double)1.5F;
            this.isDashing = true;
            this.dashVelX = dx / dDist * speed;
            this.dashVelZ = dz / dDist * speed;
            this.dashTicksRemaining = 6;
            if ((double)this.getDistance(target) <= (double)5.0F) {
               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               double dMul = this.getDamageMultiplier();
               float dashTotal = baseDmg * 1.2F * (float)dMul;
               float dashNormal;
               float dashTrue;
               if (this.trueDamageSplit > 0.0F) {
                  dashNormal = dashTotal * (1.0F - this.trueDamageSplit);
                  dashTrue = dashTotal * this.trueDamageSplit * this.trueDamageMultiplier;
               } else {
                  dashNormal = dashTotal;
                  dashTrue = 3.0F * this.trueDamageMultiplier;
               }

               target.attackEntityFrom(DamageSource.causeMobDamage(this), dashNormal);
               target.hurtResistantTime = 0;
               target.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), dashTrue);
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.posX, this.posY + (double)1.0F, this.posZ, 5, 0.3, 0.3, 0.3, (double)0.0F, new int[0]);
            }

            this.swingArm(EnumHand.MAIN_HAND);
         }
      }

      private void processMirrorCombo() {
         if (this.mirrorComboTarget != null && this.mirrorComboTarget.isEntityAlive() && !(this.getDistanceSq(this.mirrorComboTarget) > (double)16.0F)) {
            float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            if (this.mirrorComboStep == 1) {
               float comboNormal;
               float comboTrue;
               if (this.trueDamageSplit > 0.0F) {
                  comboNormal = baseDmg * 0.7F * (1.0F - this.trueDamageSplit) * (float)dmgMul;
                  comboTrue = baseDmg * 0.7F * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
               } else {
                  comboNormal = baseDmg * 0.7F * (float)dmgMul;
                  comboTrue = 2.0F * this.trueDamageMultiplier;
               }

               this.mirrorComboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), comboNormal);
               this.mirrorComboTarget.hurtResistantTime = 0;
               this.mirrorComboTarget.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), comboTrue);
               this.swingArm(EnumHand.MAIN_HAND);
               if (this.mirrorAggressive) {
                  this.mirrorComboStep = 2;
                  this.mirrorComboDelay = 5;
               } else {
                  this.mirrorComboStep = 0;
                  this.mirrorComboTarget = null;
                  this.meleeCooldown = 20;
               }
            } else if (this.mirrorComboStep == 2) {
               float heavyNormal;
               float heavyTrue;
               if (this.trueDamageSplit > 0.0F) {
                  heavyNormal = baseDmg * 1.3F * (1.0F - this.trueDamageSplit) * (float)dmgMul;
                  heavyTrue = baseDmg * 1.3F * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
               } else {
                  heavyNormal = baseDmg * 1.3F * (float)dmgMul;
                  heavyTrue = 4.0F * this.trueDamageMultiplier;
               }

               this.mirrorComboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), heavyNormal);
               this.mirrorComboTarget.hurtResistantTime = 0;
               this.mirrorComboTarget.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), heavyTrue);
               this.swingArm(EnumHand.MAIN_HAND);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.mirrorComboTarget.posX, this.mirrorComboTarget.posY + (double)1.0F, this.mirrorComboTarget.posZ, 3, 0.3, 0.3, 0.3, (double)0.0F, new int[0]);
               }

               this.mirrorComboStep = 0;
               this.mirrorComboTarget = null;
               this.meleeCooldown = 15;
            }

         } else {
            this.mirrorComboStep = 0;
            this.mirrorComboTarget = null;
         }
      }
   }
}
