
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.ModelPlayerPoseable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcPose;
import net.minecraft.block.Block;
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
import net.minecraft.init.Blocks;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKakuzuStyleNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 246;

   public EntityKakuzuStyleNPC(ElementsInfTsukAddon instance) {
      super(instance, 246);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kakuzustyle"), 246).name("kakuzustyle").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KakuzuNpcRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class KakuzuNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public KakuzuNpcRenderer(RenderManager renderManager) {
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
      private boolean kakuzuHeartsSummoned = false;
      private boolean kakuzuHeartsPhase3Summoned = false;
      private List<Entity> kakuzuHeartEntities = new ArrayList();
      private boolean kakuzuIntroPlayed = false;
      private int kakuzuPhase = 1;
      private int kakuzuJutsuCooldown = 0;
      private int kakuzuTentacleLungeCD = 0;
      private int kakuzuThreadPullCD = 0;
      private int kakuzuTentacleStrikeCD = 0;
      private int kakuzuEarthSpearCD = 0;
      private int kakuzuThreadWebCD = 0;
      private int kakuzuFocusFireCD = 0;
      private int kakuzuTentacleFrenzyCD = 0;
      private boolean kakuzuEarthSpearActive = false;
      private int kakuzuEarthSpearTicks = 0;
      private static final float KAKUZU_PHASE_2 = 0.75F;
      private static final float KAKUZU_PHASE_3 = 0.5F;
      private static final float KAKUZU_PHASE_4 = 0.25F;
      private static final double KAKUZU_BASE_HP = (double)18000.0F;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processKakuzuCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.kakuzuJutsuCooldown > 0) {
            --this.kakuzuJutsuCooldown;
         }

         if (this.kakuzuTentacleLungeCD > 0) {
            --this.kakuzuTentacleLungeCD;
         }

         if (this.kakuzuThreadPullCD > 0) {
            --this.kakuzuThreadPullCD;
         }

         if (this.kakuzuTentacleStrikeCD > 0) {
            --this.kakuzuTentacleStrikeCD;
         }

         if (this.kakuzuEarthSpearCD > 0) {
            --this.kakuzuEarthSpearCD;
         }

         if (this.kakuzuThreadWebCD > 0) {
            --this.kakuzuThreadWebCD;
         }

         if (this.kakuzuFocusFireCD > 0) {
            --this.kakuzuFocusFireCD;
         }

         if (this.kakuzuTentacleFrenzyCD > 0) {
            --this.kakuzuTentacleFrenzyCD;
         }

         if (this.kakuzuEarthSpearActive) {
            --this.kakuzuEarthSpearTicks;
            if (this.kakuzuEarthSpearTicks <= 0) {
               this.kakuzuEarthSpearActive = false;
               if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
                  double curArmor = this.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue();
                  this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(Math.max((double)0.0F, curArmor - (double)10.0F));
               }
            }
         }

         for(int i = this.kakuzuHeartEntities.size() - 1; i >= 0; --i) {
            Entity heart = (Entity)this.kakuzuHeartEntities.get(i);
            if (heart == null || heart.isDead) {
               this.kakuzuHeartEntities.remove(i);
            }
         }

      }

      protected void resetCombatState() {
         this.kakuzuHeartsSummoned = false;
         this.kakuzuHeartsPhase3Summoned = false;
         this.kakuzuHeartEntities.clear();
         this.kakuzuIntroPlayed = false;
         this.kakuzuPhase = 1;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("kakuzuHeartsSummoned", this.kakuzuHeartsSummoned);
         compound.setInteger("kakuzuPhase", this.kakuzuPhase);
         compound.setBoolean("kakuzuIntroPlayed", this.kakuzuIntroPlayed);
         compound.setInteger("kakuzuJutsuCooldown", this.kakuzuJutsuCooldown);
         compound.setBoolean("kakuzuHeartsPhase3Summoned", this.kakuzuHeartsPhase3Summoned);
         compound.setInteger("kakuzuTentacleLungeCD", this.kakuzuTentacleLungeCD);
         compound.setInteger("kakuzuThreadPullCD", this.kakuzuThreadPullCD);
         compound.setInteger("kakuzuTentacleStrikeCD", this.kakuzuTentacleStrikeCD);
         compound.setInteger("kakuzuEarthSpearCD", this.kakuzuEarthSpearCD);
         compound.setInteger("kakuzuThreadWebCD", this.kakuzuThreadWebCD);
         compound.setInteger("kakuzuFocusFireCD", this.kakuzuFocusFireCD);
         compound.setInteger("kakuzuTentacleFrenzyCD", this.kakuzuTentacleFrenzyCD);
         compound.setBoolean("kakuzuEarthSpearActive", this.kakuzuEarthSpearActive);
         compound.setInteger("kakuzuEarthSpearTicks", this.kakuzuEarthSpearTicks);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.kakuzuHeartsSummoned = compound.getBoolean("kakuzuHeartsSummoned");
         this.kakuzuPhase = compound.hasKey("kakuzuPhase") ? compound.getInteger("kakuzuPhase") : 1;
         this.kakuzuIntroPlayed = compound.getBoolean("kakuzuIntroPlayed");
         this.kakuzuJutsuCooldown = compound.hasKey("kakuzuJutsuCooldown") ? compound.getInteger("kakuzuJutsuCooldown") : 0;
         this.kakuzuHeartsPhase3Summoned = compound.getBoolean("kakuzuHeartsPhase3Summoned");
         this.kakuzuTentacleLungeCD = compound.hasKey("kakuzuTentacleLungeCD") ? compound.getInteger("kakuzuTentacleLungeCD") : 0;
         this.kakuzuThreadPullCD = compound.hasKey("kakuzuThreadPullCD") ? compound.getInteger("kakuzuThreadPullCD") : 0;
         this.kakuzuTentacleStrikeCD = compound.hasKey("kakuzuTentacleStrikeCD") ? compound.getInteger("kakuzuTentacleStrikeCD") : 0;
         this.kakuzuEarthSpearCD = compound.hasKey("kakuzuEarthSpearCD") ? compound.getInteger("kakuzuEarthSpearCD") : 0;
         this.kakuzuThreadWebCD = compound.hasKey("kakuzuThreadWebCD") ? compound.getInteger("kakuzuThreadWebCD") : 0;
         this.kakuzuFocusFireCD = compound.hasKey("kakuzuFocusFireCD") ? compound.getInteger("kakuzuFocusFireCD") : 0;
         this.kakuzuTentacleFrenzyCD = compound.hasKey("kakuzuTentacleFrenzyCD") ? compound.getInteger("kakuzuTentacleFrenzyCD") : 0;
         this.kakuzuEarthSpearActive = compound.getBoolean("kakuzuEarthSpearActive");
         this.kakuzuEarthSpearTicks = compound.hasKey("kakuzuEarthSpearTicks") ? compound.getInteger("kakuzuEarthSpearTicks") : 0;
      }

      protected void onCombatDeath() {
         this.kakuzuOnDeath();
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         return amount;
      }

      private void processKakuzuCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            float hpPercent = this.getHealth() / this.getMaxHealth();
            if (!this.kakuzuIntroPlayed) {
               this.kakuzuIntroPlayed = true;
               this.kakuzuBroadcast("§2Kakuzu: §aYou think you can defeat me? I have five hearts!");
            }

            this.kakuzuCheckPhaseTransition(hpPercent, target);
            if (this.kakuzuHeartsSummoned && !this.kakuzuHeartEntities.isEmpty() && this.ticksExisted % 60 == 0) {
               for(Entity e : this.kakuzuHeartEntities) {
                  if (e instanceof EntityGenericNPC.EntityCustom && e.isEntityAlive()) {
                     ((EntityGenericNPC.EntityCustom)e).setAttackTarget(target);
                  }
               }
            }

            if (this.kakuzuEarthSpearCD <= 0 && !this.kakuzuEarthSpearActive && dist <= (double)8.0F && this.rand.nextFloat() < 0.15F) {
               this.kakuzuActivateEarthSpear();
               this.kakuzuEarthSpearCD = this.cdMul(this.kakuzuPhase >= 4 ? 100 : 160);
            }

            if (!this.isDashing && this.kakuzuTentacleLungeCD <= 0 && dist >= (double)6.0F && dist <= (double)20.0F) {
               this.kakuzuTentacleLunge(target);
               this.kakuzuTentacleLungeCD = this.cdMul(this.kakuzuPhase >= 4 ? 50 : (this.kakuzuPhase >= 3 ? 60 : 80));
            } else if (this.kakuzuThreadPullCD <= 0 && dist >= (double)8.0F && dist <= (double)25.0F && this.rand.nextFloat() < 0.3F) {
               this.kakuzuThreadPull(target);
               this.kakuzuThreadPullCD = this.cdMul(this.kakuzuPhase >= 4 ? 80 : 120);
            } else if (this.kakuzuPhase >= 2 && !this.kakuzuHeartEntities.isEmpty() && this.kakuzuThreadWebCD <= 0 && dist <= (double)15.0F && this.rand.nextFloat() < 0.2F) {
               this.kakuzuThreadWeb(target);
               this.kakuzuThreadWebCD = this.cdMul(100);
            } else {
               if (this.kakuzuPhase >= 3 && this.kakuzuFocusFireCD <= 0 && !this.kakuzuHeartEntities.isEmpty() && this.kakuzuHeartEntities.size() >= 2) {
                  this.kakuzuFocusFire(target);
                  this.kakuzuFocusFireCD = this.cdMul(600);
               }

               if (this.kakuzuPhase >= 4 && this.kakuzuTentacleFrenzyCD <= 0 && dist <= (double)5.0F && this.rand.nextFloat() < 0.25F) {
                  this.kakuzuTentacleFrenzy(target);
                  this.kakuzuTentacleFrenzyCD = this.cdMul(120);
               } else if (this.natureType != 0 && this.kakuzuJutsuCooldown <= 0 && dist >= (double)5.0F && dist <= (double)18.0F && this.rand.nextFloat() < 0.25F) {
                  this.startNatureJutsu(target);
                  this.kakuzuJutsuCooldown = this.cdMul(this.kakuzuPhase >= 4 ? 50 : (this.kakuzuPhase >= 3 ? 70 : 100));
               } else if (this.kakuzuTentacleStrikeCD <= 0 && dist <= (double)6.0F && dist > (double)2.5F) {
                  this.kakuzuTentacleStrike(target);
                  this.kakuzuTentacleStrikeCD = this.cdMul(this.kakuzuPhase >= 4 ? 20 : 35);
               } else {
                  if (dist > (double)6.0F) {
                     this.getNavigator().tryMoveToEntityLiving(target, 1.3);
                  } else if (dist > (double)2.5F) {
                     this.getNavigator().tryMoveToEntityLiving(target, 1.1);
                  }

                  if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
                     this.performMeleeSwing(target);
                  }

               }
            }
         }
      }

      private void kakuzuCheckPhaseTransition(float hpPercent, EntityLivingBase target) {
         if (this.kakuzuPhase == 1 && hpPercent <= 0.75F) {
            this.kakuzuPhase = 2;
            this.kakuzuHeartsSummoned = true;
            this.kakuzuBroadcast("§2Kakuzu: §aYou're worth some bounty... Let me show you my collection.");
            this.kakuzuSummonHeartsPhased(target, new String[]{"kakuzu_heart_fire", "kakuzu_heart_wind"});
            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               double s = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(s * 1.05);
            }
         }

         if (this.kakuzuPhase == 2 && hpPercent <= 0.5F) {
            this.kakuzuPhase = 3;
            this.kakuzuHeartsPhase3Summoned = true;
            this.kakuzuBroadcast("§4Kakuzu: §cI've lived too long to die here!");
            this.kakuzuSummonHeartsPhased(target, new String[]{"kakuzu_heart_lightning", "kakuzu_heart_earth"});
            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               double s = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(s * 1.1);
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)0.5F, 0.8, (double)0.5F, 0.1, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 1.5F, 0.5F);
         }

         if (this.kakuzuPhase == 3 && hpPercent <= 0.25F) {
            this.kakuzuPhase = 4;
            this.kakuzuBroadcast("§4§lKakuzu: You... I'll crush you with my own hands!");
            if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
               double d = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(d * 1.4);
            }

            if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
               double s = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(s * (double)1.25F);
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 40, 0.6, (double)1.0F, 0.6, 0.1, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.5F, 0.7F);
         }

      }

      private void kakuzuSummonHeartsPhased(EntityLivingBase target, String[] heartConfigs) {
         double tierRatio = (double)this.getMaxHealth() / (double)18000.0F;
         if (tierRatio < 0.1) {
            tierRatio = 0.1;
         }

         for(int i = 0; i < heartConfigs.length; ++i) {
            NpcConfig config = NpcConfigRegistry.get(heartConfigs[i]);
            if (config != null) {
               EntityGenericNPC.EntityCustom heart = new EntityGenericNPC.EntityCustom(this.world);
               double angle = (double)i * Math.PI + this.rand.nextDouble() * (double)0.5F;
               double radius = (double)4.0F + this.rand.nextDouble() * (double)2.0F;
               double spawnX = this.posX + Math.cos(angle) * radius;
               double spawnZ = this.posZ + Math.sin(angle) * radius;
               double spawnY = this.findSafeY(spawnX, this.posY, spawnZ);
               heart.setPosition(spawnX, spawnY, spawnZ);
               heart.applyNpcConfig(config);
               if (heart.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
                  double scaledHP = config.getMaxHealth() * tierRatio;
                  heart.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(scaledHP);
                  heart.setHealth((float)scaledHP);
               }

               if (heart.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
                  double scaledDmg = config.getAttackDamage() * tierRatio;
                  heart.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(scaledDmg);
               }

               heart.setAttackTarget(target);
               NBTTagCompound nbt = heart.getEntityData();
               nbt.setBoolean("endgameEntity", true);
               nbt.setString("instanceKey", this.getEntityData().getString("instanceKey"));
               nbt.setBoolean("kakuzuHeart", true);
               this.world.spawnEntity(heart);
               this.kakuzuHeartEntities.add(heart);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, spawnX, spawnY + (double)1.0F, spawnZ, 20, 0.4, 0.6, 0.4, 0.08, new int[0]);
               }
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0F, 0.8F);
      }

      private void kakuzuTentacleLunge(EntityLivingBase target) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (!(horizDist < (double)0.5F)) {
            double nx = dx / horizDist;
            double nz = dz / horizDist;
            int duration = 10;
            double dashDist = Math.max(horizDist - (double)2.0F, (double)1.0F);
            double velocityPerTick = dashDist / (double)duration;
            this.isDashing = true;
            this.dashType = 2;
            this.dashDuration = duration;
            this.dashTicksRemaining = duration;
            this.dashVelX = nx * velocityPerTick * 1.4;
            this.dashVelZ = nz * velocityPerTick * 1.4;
            this.dashArcSustain = 0.06F;
            this.dashFallAccel = 0.07F;
            this.dashMaxFall = -0.4F;
            this.dashHasMidairGuidance = false;
            this.motionY = 0.3;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.4, 0.4, 0.4, 0.1, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.2F, 0.7F);
         }
      }

      private void kakuzuThreadPull(EntityLivingBase target) {
         double dx = this.posX - target.posX;
         double dz = this.posZ - target.posZ;
         double pullDist = Math.sqrt(dx * dx + dz * dz);
         if (!(pullDist < (double)1.0F)) {
            double pullStrength = this.kakuzuPhase >= 4 ? 1.8 : 1.2;
            double nx = dx / pullDist;
            double nz = dz / pullDist;
            target.motionX += nx * pullStrength;
            target.motionY += (double)0.25F;
            target.motionZ += nz * pullStrength;
            target.velocityChanged = true;
            float pullDmg = 4.0F * (float)this.getDamageMultiplier();
            target.attackEntityFrom(DamageSource.causeMobDamage(this), pullDmg);
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int step = 0; step <= 5; ++step) {
                  double t = (double)step / (double)5.0F;
                  ws.spawnParticle(EnumParticleTypes.CRIT, this.posX + (target.posX - this.posX) * t, this.posY + (double)1.0F + (target.posY - this.posY) * t, this.posZ + (target.posZ - this.posZ) * t, 3, 0.1, 0.1, 0.1, 0.02, new int[0]);
               }
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.HOSTILE, 1.5F, 0.5F);
            if (target instanceof EntityPlayerMP) {
               ((EntityPlayerMP)target).sendMessage(new TextComponentString("§2§o* Kakuzu's threads yank you forward! *"));
            }

         }
      }

      private void kakuzuTentacleStrike(EntityLivingBase target) {
         float baseDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float normalDmg = baseDamage * 0.8F * (float)this.getDamageMultiplier();
         target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, 3.0F);
         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 8, 0.3, 0.3, 0.3, 0.05, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 0.8F, 1.2F);
      }

      private void kakuzuActivateEarthSpear() {
         this.kakuzuEarthSpearActive = true;
         this.kakuzuEarthSpearTicks = 60;
         if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
            double curArmor = this.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue();
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(curArmor + (double)10.0F);
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + (double)1.0F, this.posZ, 25, (double)0.5F, 0.8, (double)0.5F, 0.05, new int[]{Block.getStateId(Blocks.OBSIDIAN.getDefaultState())});
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.HOSTILE, 1.0F, 0.6F);
         this.kakuzuBroadcast("§8Kakuzu: §7Earth Spear!");
      }

      private void kakuzuThreadWeb(EntityLivingBase target) {
         for(Entity heart : this.kakuzuHeartEntities) {
            if (heart != null && heart.isEntityAlive()) {
               double hx = heart.posX - this.posX;
               double hz = heart.posZ - this.posZ;
               double heartDist = Math.sqrt(hx * hx + hz * hz);
               if (!(heartDist < (double)2.0F)) {
                  double tx = target.posX - this.posX;
                  double tz = target.posZ - this.posZ;
                  double targetDist = Math.sqrt(tx * tx + tz * tz);
                  if (!(targetDist < (double)1.0F) && !(targetDist > heartDist)) {
                     double dot = (hx * tx + hz * tz) / (heartDist * targetDist);
                     if (dot > 0.7) {
                        float webDmg = 6.0F * (float)this.getDamageMultiplier();
                        target.attackEntityFrom(DamageSource.MAGIC, webDmg);
                        target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1));
                        if (this.world instanceof WorldServer) {
                           WorldServer ws = (WorldServer)this.world;

                           for(int step = 0; step <= 8; ++step) {
                              double t = (double)step / (double)8.0F;
                              ws.spawnParticle(EnumParticleTypes.CRIT, this.posX + hx * t, this.posY + (double)1.0F + (heart.posY - this.posY) * t, this.posZ + hz * t, 2, 0.05, 0.05, 0.05, 0.01, new int[0]);
                           }
                        }

                        this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.HOSTILE, 1.0F, 0.8F);
                        break;
                     }
                  }
               }
            }
         }

      }

      private void kakuzuFocusFire(EntityLivingBase target) {
         this.kakuzuBroadcast("§4Kakuzu: §cAll hearts — FIRE!");

         for(Entity e : this.kakuzuHeartEntities) {
            if (e instanceof EntityGenericNPC.EntityCustom && e.isEntityAlive()) {
               ((EntityGenericNPC.EntityCustom)e).setAttackTarget(target);
            }
         }

         if (this.natureType != 0) {
            this.startNatureJutsu(target);
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)2.0F, this.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
         }

      }

      private void kakuzuTentacleFrenzy(EntityLivingBase target) {
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float hitDmg = baseDmg * 0.5F * (float)this.getDamageMultiplier();

         for(int i = 0; i < 4; ++i) {
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), hitDmg);
         }

         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, 5.0F);
         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.2F, 1.5F);
         this.kakuzuBroadcast("§4Kakuzu: §cTentacle Frenzy!");
      }

      private void kakuzuOnDeath() {
         for(Entity e : this.kakuzuHeartEntities) {
            if (e != null && e.isEntityAlive()) {
               e.setDead();
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, e.posX, e.posY + (double)1.0F, e.posZ, 15, 0.3, (double)0.5F, 0.3, 0.05, new int[0]);
               }
            }
         }

         this.kakuzuHeartEntities.clear();
      }

      private void kakuzuBroadcast(String msg) {
         for(EntityPlayer p : this.world.playerEntities) {
            if ((double)p.getDistance(this) < (double)48.0F) {
               ((EntityPlayerMP)p).sendMessage(new TextComponentString(msg));
            }
         }

      }
   }
}
