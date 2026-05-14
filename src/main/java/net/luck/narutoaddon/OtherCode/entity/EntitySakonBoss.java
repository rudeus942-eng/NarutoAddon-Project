
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.*;
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
public class EntitySakonBoss extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 242;

   public EntitySakonBoss(ElementsInfTsukAddon instance) {
      super(instance, 242);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "sakonboss"), 242).name("sakonboss").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SakonBossRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class SakonBossRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public SakonBossRenderer(RenderManager renderManager) {
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
      private boolean sakonUkonSpawned = false;
      private Entity sakonUkonEntity = null;
      private boolean sakonIntroPlayed = false;
      private boolean sakonCurseMark1 = false;
      private boolean sakonCurseMark2 = false;
      private int sakonPhase = 1;
      private int sakonFlickerDashCD = 0;
      private int sakonRushingTackleCD = 0;
      private int sakonCoordinatedRushCD = 0;
      private int sakonBerserkerLeapCD = 0;
      private int sakonMeleeCD = 0;
      private int sakonComboStep = 0;
      private int sakonComboDelay = 0;
      private EntityLivingBase sakonComboTarget = null;
      private static final float SAKON_PHASE_2 = 0.65F;
      private static final float SAKON_PHASE_3 = 0.3F;
      private static final double SAKON_BASE_HP = (double)11000.0F;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processSakonCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.sakonFlickerDashCD > 0) {
            --this.sakonFlickerDashCD;
         }

         if (this.sakonRushingTackleCD > 0) {
            --this.sakonRushingTackleCD;
         }

         if (this.sakonCoordinatedRushCD > 0) {
            --this.sakonCoordinatedRushCD;
         }

         if (this.sakonBerserkerLeapCD > 0) {
            --this.sakonBerserkerLeapCD;
         }

         if (this.sakonMeleeCD > 0) {
            --this.sakonMeleeCD;
         }

         if (this.sakonUkonEntity != null && (this.sakonUkonEntity.isDead || !this.sakonUkonEntity.isEntityAlive())) {
            this.sakonUkonEntity = null;
         }

         if (this.sakonComboStep > 0 && this.sakonComboTarget != null) {
            if (this.sakonComboDelay > 0) {
               --this.sakonComboDelay;
            } else {
               this.processSakonComboHit();
            }
         }

      }

      protected void resetCombatState() {
         this.sakonUkonSpawned = false;
         this.sakonUkonEntity = null;
         this.sakonIntroPlayed = false;
         this.sakonCurseMark1 = false;
         this.sakonCurseMark2 = false;
         this.sakonPhase = 1;
      }

      protected boolean usesVanillaMeleeAI() {
         return true;
      }

      protected void onCombatDeath() {
         this.sakonOnDeath();
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("sakonUkonSpawned", this.sakonUkonSpawned);
         compound.setBoolean("sakonIntroPlayed", this.sakonIntroPlayed);
         compound.setBoolean("sakonCurseMark1", this.sakonCurseMark1);
         compound.setBoolean("sakonCurseMark2", this.sakonCurseMark2);
         compound.setInteger("sakonPhase", this.sakonPhase);
         compound.setInteger("sakonFlickerDashCD", this.sakonFlickerDashCD);
         compound.setInteger("sakonRushingTackleCD", this.sakonRushingTackleCD);
         compound.setInteger("sakonCoordinatedRushCD", this.sakonCoordinatedRushCD);
         compound.setInteger("sakonBerserkerLeapCD", this.sakonBerserkerLeapCD);
         compound.setInteger("sakonMeleeCD", this.sakonMeleeCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.sakonUkonSpawned = compound.getBoolean("sakonUkonSpawned");
         this.sakonIntroPlayed = compound.getBoolean("sakonIntroPlayed");
         this.sakonCurseMark1 = compound.getBoolean("sakonCurseMark1");
         this.sakonCurseMark2 = compound.getBoolean("sakonCurseMark2");
         this.sakonPhase = compound.hasKey("sakonPhase") ? compound.getInteger("sakonPhase") : 1;
         this.sakonFlickerDashCD = compound.hasKey("sakonFlickerDashCD") ? compound.getInteger("sakonFlickerDashCD") : 0;
         this.sakonRushingTackleCD = compound.hasKey("sakonRushingTackleCD") ? compound.getInteger("sakonRushingTackleCD") : 0;
         this.sakonCoordinatedRushCD = compound.hasKey("sakonCoordinatedRushCD") ? compound.getInteger("sakonCoordinatedRushCD") : 0;
         this.sakonBerserkerLeapCD = compound.hasKey("sakonBerserkerLeapCD") ? compound.getInteger("sakonBerserkerLeapCD") : 0;
         this.sakonMeleeCD = compound.hasKey("sakonMeleeCD") ? compound.getInteger("sakonMeleeCD") : 0;
      }

      private void processSakonCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            float hpPercent = this.getHealth() / this.getMaxHealth();
            if (!this.sakonIntroPlayed) {
               this.sakonIntroPlayed = true;
               this.sakonBroadcast("§5Sakon: §dYou're not going to last long against us...");
            }

            this.sakonCheckPhaseTransition(hpPercent, target);
            if (this.sakonPhase >= 3 && !this.isDashing && this.sakonBerserkerLeapCD <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
               this.sakonBerserkerLeap(target);
               this.sakonBerserkerLeapCD = this.cdMul(100);
            } else if (!this.isDashing && this.sakonRushingTackleCD <= 0 && dist >= (double)6.0F && dist <= (double)20.0F) {
               this.sakonRushingTackle(target);
               this.sakonRushingTackleCD = this.cdMul(this.sakonPhase >= 3 ? 50 : (this.sakonPhase >= 2 ? 60 : 80));
            } else if (this.sakonPhase >= 2 && this.sakonUkonEntity != null && this.sakonUkonEntity.isEntityAlive() && this.sakonCoordinatedRushCD <= 0 && dist >= (double)5.0F && dist <= (double)18.0F && this.rand.nextFloat() < 0.3F) {
               this.sakonCoordinatedRush(target);
               this.sakonCoordinatedRushCD = this.cdMul(this.sakonPhase >= 3 ? 80 : 120);
            } else if (!this.isDashing && this.sakonFlickerDashCD <= 0 && dist >= (double)3.0F && dist <= (double)8.0F) {
               this.sakonFlickerDash(target);
               this.sakonFlickerDashCD = this.cdMul(this.sakonPhase >= 3 ? 30 : (this.sakonPhase >= 2 ? 40 : 50));
            } else {
               if (dist > (double)5.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, 1.3);
               } else if (dist > (double)2.5F) {
                  this.getNavigator().tryMoveToEntityLiving(target, 1.1);
               }

               if (this.sakonMeleeCD <= 0 && dist <= (double)2.5F) {
                  this.sakonMeleeAttack(target);
               }
            }
         }
      }

      private void sakonCheckPhaseTransition(float hpPercent, EntityLivingBase target) {
         if (this.sakonPhase == 1 && hpPercent <= 0.65F) {
            this.sakonPhase = 2;
            this.sakonCurseMark1 = true;
            this.sakonBroadcast("§5Sakon: §dUkon... let's tear them apart together!");
            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               double curSpd = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(curSpd * 1.1);
            }

            if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
               double curDmg = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(curDmg * 1.15);
            }

            this.sakonSpawnUkon(target);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)0.5F, 0.8, (double)0.5F, 0.1, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.5F, 0.5F);
         }

         if (this.sakonPhase == 2 && hpPercent <= 0.3F) {
            this.sakonPhase = 3;
            this.sakonCurseMark2 = true;
            this.sakonBroadcast("§4§lSakon: You'll be absorbed into our body!");
            if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
               double curDmg = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(curDmg * 1.35);
            }

            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               double curSpd = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(curSpd * 1.2);
            }

            if (this.sakonUkonEntity != null && this.sakonUkonEntity.isEntityAlive()) {
               if (this.sakonUkonEntity instanceof EntityLivingBase) {
                  EntityLivingBase ukon = (EntityLivingBase)this.sakonUkonEntity;
                  if (ukon.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
                     double ud = ukon.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                     ukon.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ud * 1.3);
                  }

                  if (ukon.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
                     double us = ukon.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
                     ukon.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(us * 1.15);
                  }
               }
            } else {
               this.sakonSpawnUkon(target);
               if (this.sakonUkonEntity instanceof EntityLivingBase) {
                  EntityLivingBase ukon = (EntityLivingBase)this.sakonUkonEntity;
                  ukon.setHealth(ukon.getMaxHealth() * 0.5F);
               }
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 50, 0.6, (double)1.0F, 0.6, 0.15, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 20, 0.4, 0.6, 0.4, 0.05, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.5F, 0.7F);
         }

      }

      private void sakonSpawnUkon(EntityLivingBase target) {
         NpcConfig config = NpcConfigRegistry.get("sakon_split");
         if (config != null) {
            double tierRatio = (double)this.getMaxHealth() / (double)11000.0F;
            if (tierRatio < 0.1) {
               tierRatio = 0.1;
            }

            Entity ukonEntity = EntityList.createEntityByIDFromName(new ResourceLocation("inftsukaddon", "questnpc1"), this.world);
            if (ukonEntity != null) {
               if (ukonEntity instanceof EntityCreature) {
                  EntityCreature ukon = (EntityCreature)ukonEntity;
                  double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
                  double radius = (double)2.0F + this.rand.nextDouble();
                  double spawnX = this.posX + Math.cos(angle) * radius;
                  double spawnZ = this.posZ + Math.sin(angle) * radius;
                  double spawnY = this.findSafeY(spawnX, this.posY, spawnZ);
                  ukon.setPosition(spawnX, spawnY, spawnZ);
                  if (ukon instanceof INpcConfigurable) {
                     ((INpcConfigurable)ukon).applyNpcConfig(config);
                  }

                  if (ukon.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
                     double scaledHP = config.getMaxHealth() * tierRatio;
                     ukon.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(scaledHP);
                     ukon.setHealth((float)scaledHP);
                  }

                  if (ukon.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
                     double scaledDmg = config.getAttackDamage() * tierRatio;
                     ukon.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(scaledDmg);
                  }

                  if (target instanceof EntityLivingBase) {
                     ukon.setAttackTarget(target);
                  }

                  ukon.enablePersistence();
                  NBTTagCompound nbt = ukon.getEntityData();
                  nbt.setBoolean("sakonUkon", true);
                  NBTTagCompound parentNbt = this.getEntityData();
                  if (parentNbt.getBoolean("questEntity")) {
                     nbt.setBoolean("questEntity", true);
                     nbt.setString("questId", parentNbt.getString("questId"));
                     String parentOwner = parentNbt.getString("ownerUUID");
                     if (parentOwner != null && !parentOwner.isEmpty()) {
                        nbt.setString("ownerUUID", parentOwner);
                     }

                     String parentSharedQuestId = parentNbt.getString("sharedQuestId");
                     if (parentSharedQuestId != null && !parentSharedQuestId.isEmpty()) {
                        nbt.setString("sharedQuestId", parentSharedQuestId);
                     }
                  }

                  if (parentNbt.getBoolean("endgameEntity")) {
                     nbt.setBoolean("endgameEntity", true);
                     nbt.setString("instanceKey", parentNbt.getString("instanceKey"));
                  }

                  if (parentNbt.getBoolean("akatsukiMissionEntity")) {
                     nbt.setBoolean("akatsukiMissionEntity", true);
                     nbt.setString("missionOwnerId", parentNbt.getString("missionOwnerId"));
                     nbt.setString("missionTemplateId", parentNbt.getString("missionTemplateId"));
                  }

                  this.world.spawnEntity(ukon);
                  this.sakonUkonEntity = ukon;
                  this.sakonUkonSpawned = true;
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, spawnX, spawnY + (double)1.0F, spawnZ, 25, 0.4, 0.6, 0.4, 0.08, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.0F, 0.8F);
               }
            }
         }
      }

      private void sakonRushingTackle(EntityLivingBase target) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (!(horizDist < (double)0.5F)) {
            double nx = dx / horizDist;
            double nz = dz / horizDist;
            int duration = 8;
            double dashDist = Math.max(horizDist - (double)1.5F, (double)1.0F);
            double velocityPerTick = dashDist / (double)duration;
            this.isDashing = true;
            this.dashType = 2;
            this.dashDuration = duration;
            this.dashTicksRemaining = duration;
            this.dashVelX = nx * velocityPerTick * (double)1.5F;
            this.dashVelZ = nz * velocityPerTick * (double)1.5F;
            this.dashArcSustain = 0.06F;
            this.dashFallAccel = 0.07F;
            this.dashMaxFall = -0.4F;
            this.dashHasMidairGuidance = false;
            this.motionY = 0.3;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.4, 0.4, 0.4, 0.12, new int[0]);
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + 0.3, this.posZ, 8, 0.3, 0.2, 0.3, 0.04, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.3F, 0.8F);
         }
      }

      private void sakonFlickerDash(EntityLivingBase target) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (!(horizDist < (double)0.5F)) {
            double nx = dx / horizDist;
            double nz = dz / horizDist;
            int duration = 5;
            double dashDist = Math.min(horizDist - (double)1.0F, (double)4.0F);
            double velocityPerTick = dashDist / (double)duration;
            this.isDashing = true;
            this.dashType = 1;
            this.dashDuration = duration;
            this.dashTicksRemaining = duration;
            this.dashVelX = nx * velocityPerTick * 1.2;
            this.dashVelZ = nz * velocityPerTick * 1.2;
            this.dashArcSustain = 0.04F;
            this.dashFallAccel = 0.05F;
            this.dashMaxFall = -0.3F;
            this.dashHasMidairGuidance = false;
            this.motionY = 0.2;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 6, 0.2, 0.2, 0.2, 0.03, new int[0]);
            }

         }
      }

      private void sakonCoordinatedRush(EntityLivingBase target) {
         this.sakonRushingTackle(target);
         if (this.sakonUkonEntity instanceof EntityCreature && this.sakonUkonEntity.isEntityAlive()) {
            ((EntityCreature)this.sakonUkonEntity).setAttackTarget(target);
         }

         this.sakonBroadcast("§5Sakon & Ukon: §dTogether!");
      }

      private void sakonBerserkerLeap(EntityLivingBase target) {
         double dx = target.posX - this.posX;
         double dy = target.posY - this.posY;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (!(horizDist < (double)0.5F)) {
            double nx = dx / horizDist;
            double nz = dz / horizDist;
            int duration = 14;
            double velocityPerTick = horizDist / (double)duration;
            this.isDashing = true;
            this.isLeaping = true;
            this.dashType = 4;
            this.dashDuration = duration;
            this.dashTicksRemaining = duration;
            this.dashVelX = nx * velocityPerTick * 0.85;
            this.dashVelZ = nz * velocityPerTick * 0.85;
            this.dashArcSustain = 0.0F;
            this.dashFallAccel = 0.06F;
            this.dashMaxFall = -1.0F;
            this.dashHasMidairGuidance = true;
            this.motionY = Math.max(0.85, Math.min((double)2.5F, 0.7 + dy * 0.14));
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)0.5F, this.posZ, 20, (double)0.5F, 0.4, (double)0.5F, 0.1, new int[0]);
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + 0.3, this.posZ, 12, (double)0.5F, 0.3, (double)0.5F, 0.06, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.6F, 1.3F);
            this.sakonBroadcast("§4§lSakon: §cBerserker Leap!");
         }
      }

      private void sakonMeleeAttack(EntityLivingBase target) {
         float baseDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float dmgMul = (float)this.getDamageMultiplier();
         float normalDmg;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            normalDmg = baseDamage * (1.0F - this.trueDamageSplit) * dmgMul;
            trueDmg = baseDamage * this.trueDamageSplit * dmgMul * this.trueDamageMultiplier;
         } else {
            normalDmg = baseDamage * dmgMul;
            trueDmg = (this.combatTier >= 4 ? 8.0F : (this.combatTier >= 3 ? 5.0F : 3.0F)) * this.trueDamageMultiplier;
         }

         target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         this.swingArm(EnumHand.MAIN_HAND);
         float parasiteChance = this.sakonPhase >= 3 ? 0.35F : (this.sakonPhase >= 2 ? 0.25F : 0.15F);
         if (this.rand.nextFloat() < parasiteChance) {
            target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 60, 0));
            target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0));
            if (target instanceof EntityPlayerMP) {
               ((EntityPlayerMP)target).sendMessage(new TextComponentString("§5§o* Sakon's parasitic punch saps your strength! *"));
            }
         }

         if (this.sakonPhase >= 3 && this.rand.nextFloat() < 0.3F) {
            target.addPotionEffect(new PotionEffect(MobEffects.WITHER, 60, 1));
            if (target instanceof EntityPlayerMP) {
               ((EntityPlayerMP)target).sendMessage(new TextComponentString("§4§o* Sakon attempts to merge with your body! *"));
            }
         }

         float comboChance = this.sakonPhase >= 3 ? 0.45F : (this.sakonPhase >= 2 ? 0.35F : 0.25F);
         if (this.sakonComboStep == 0 && this.rand.nextFloat() < comboChance) {
            this.sakonComboStep = 1;
            this.sakonComboDelay = 6;
            this.sakonComboTarget = target;
            this.sakonMeleeCD = 30;
         } else {
            this.sakonMeleeCD = this.sakonPhase >= 3 ? 15 : (this.sakonPhase >= 2 ? 20 : 25);
         }

      }

      private void processSakonComboHit() {
         if (this.sakonComboTarget != null && this.sakonComboTarget.isEntityAlive() && !(this.getDistanceSq(this.sakonComboTarget) > (double)16.0F)) {
            float baseDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            double dmgMul = this.getDamageMultiplier();
            int maxComboHits = this.sakonPhase >= 3 ? 4 : 2;
            if (this.sakonComboStep <= maxComboHits) {
               float normalDmg;
               float trueDmg;
               if (this.trueDamageSplit > 0.0F) {
                  normalDmg = baseDamage * 0.7F * (1.0F - this.trueDamageSplit) * (float)dmgMul;
                  trueDmg = baseDamage * 0.7F * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
               } else {
                  normalDmg = baseDamage * 0.7F * (float)dmgMul;
                  trueDmg = this.combatTier >= 3 ? 5.0F : 2.0F;
               }

               this.sakonComboTarget.hurtResistantTime = 0;
               this.sakonComboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
               this.sakonComboTarget.hurtResistantTime = 0;
               this.sakonComboTarget.attackEntityFrom(DamageSource.MAGIC, trueDmg);
               this.swingArm(this.sakonComboStep % 2 == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.9F, 1.0F + (float)this.sakonComboStep * 0.1F);
               ++this.sakonComboStep;
               this.sakonComboDelay = this.sakonPhase >= 3 ? 5 : 7;
            } else {
               this.sakonComboTarget.hurtResistantTime = 0;
               float finNormal;
               float finTrue;
               if (this.trueDamageSplit > 0.0F) {
                  finNormal = baseDamage * 1.4F * (1.0F - this.trueDamageSplit) * (float)dmgMul;
                  finTrue = baseDamage * 1.4F * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
               } else {
                  finNormal = baseDamage * 1.4F * (float)dmgMul;
                  finTrue = this.combatTier >= 3 ? 8.0F : 5.0F;
               }

               this.sakonComboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), finNormal);
               this.sakonComboTarget.hurtResistantTime = 0;
               this.sakonComboTarget.attackEntityFrom(DamageSource.MAGIC, finTrue);
               double kbx = this.sakonComboTarget.posX - this.posX;
               double kbz = this.sakonComboTarget.posZ - this.posZ;
               double kbDist = Math.sqrt(kbx * kbx + kbz * kbz);
               if (kbDist > 0.1) {
                  EntityLivingBase var10000 = this.sakonComboTarget;
                  var10000.motionX += kbx / kbDist * 0.8;
                  var10000 = this.sakonComboTarget;
                  var10000.motionY += 0.3;
                  var10000 = this.sakonComboTarget;
                  var10000.motionZ += kbz / kbDist * 0.8;
                  this.sakonComboTarget.velocityChanged = true;
               }

               this.swingArm(EnumHand.MAIN_HAND);
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.2F, 0.7F);
               this.sakonComboStep = 0;
               this.sakonComboTarget = null;
            }

         } else {
            this.sakonComboStep = 0;
            this.sakonComboTarget = null;
         }
      }

      private void sakonOnDeath() {
         if (this.sakonUkonEntity instanceof EntityLivingBase && this.sakonUkonEntity.isEntityAlive()) {
            EntityLivingBase ukon = (EntityLivingBase)this.sakonUkonEntity;
            if (ukon.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
               double ud = ukon.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
               ukon.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ud * (double)1.5F);
            }

            if (ukon.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               double us = ukon.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
               ukon.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(us * 1.2);
            }

            this.sakonBroadcast("§4§lUkon: SAKON! I'll kill you for this!");
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, ukon.posX, ukon.posY + (double)1.0F, ukon.posZ, 15, 0.3, 0.4, 0.3, 0.05, new int[0]);
            }
         }

      }

      private void sakonBroadcast(String msg) {
         for(EntityPlayer p : this.world.playerEntities) {
            if ((double)p.getDistance(this) < (double)48.0F) {
               ((EntityPlayerMP)p).sendMessage(new TextComponentString(msg));
            }
         }

      }
   }
}
