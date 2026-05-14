
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
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
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
import java.util.UUID;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityShadowBoss extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 232;

   public EntityShadowBoss(ElementsInfTsukAddon instance) {
      super(instance, 37);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "shadowboss"), 232).name("shadowboss").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, ShadowBossRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class ShadowBossRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public ShadowBossRenderer(RenderManager renderManager) {
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
      private int shadowPhase = 1;
      private boolean shadowPhaseTransitioning = false;
      private int shadowPhaseTransitionTicks = 0;
      private int shadowStrafeDir = 1;
      private int shadowStrafeTicks = 0;
      private boolean shadowSprinting = false;
      private int shadowSprintTicks = 0;
      private boolean shadowRetreating = false;
      private boolean shadowAdvancing = false;
      private boolean shadowInPressure = true;
      private int shadowRhythmTicks = 0;
      private int shadowRhythmDuration = 80;
      private int shadowAggression = 50;
      private int shadowSubCD = 0;
      private boolean shadowJustSubbed = false;
      private int shadowSubCounterWindow = 0;
      private boolean shadowInCombo = false;
      private int shadowComboStep = 0;
      private int shadowComboLength = 0;
      private int shadowComboTicks = 0;
      private int shadowComboInterval = 10;
      private int shadowGlobalJutsuCD = 0;
      private int shadowFireCD1 = 0;
      private int shadowFireCD2 = 0;
      private int shadowFireCD3 = 0;
      private int shadowWindCD1 = 0;
      private int shadowWindCD2 = 0;
      private int shadowWaterCD1 = 0;
      private int shadowLightningCD1 = 0;
      private int shadowLavaCD = 0;
      private int shadowIceCD = 0;
      private int shadowStormCD = 0;
      private int shadowMeleeCD = 0;
      private int shadowHeavyMeleeCD = 0;
      private boolean shadowHeavyWindup = false;
      private int shadowHeavyWindupTicks = 0;
      private int shadowDialogueCD = 0;
      private boolean shadowIntroPlayed = false;
      private boolean shadowLowHPPlayed = false;
      private float shadowRecentDamage = 0.0F;
      private int shadowDamageDecayTimer = 0;
      private boolean shadowMirrorStance = false;
      private int shadowMirrorTicks = 0;
      private int shadowMirrorCD = 0;
      private int shadowCloneCD = 0;
      private EntityLivingBase shadowLockedTarget = null;
      private int shadowTargetLockTicks = 0;
      private int shadowFlickerCD = 0;
      private static final float SHADOW_PHASE_2 = 0.75F;
      private static final float SHADOW_PHASE_3 = 0.5F;
      private static final float SHADOW_PHASE_4 = 0.25F;
      private static final int SHADOW_MELEE_RANGE = 4;
      private static final int SHADOW_CLOSE_RANGE = 6;
      private static final int SHADOW_MID_RANGE = 12;
      private static final int SHADOW_OPTIMAL_RANGE = 5;
      private static final int SHADOW_FAR_RANGE = 20;
      private static final int SHADOW_SUB_CD = 120;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         if (this.shadowLockedTarget != null && this.shadowLockedTarget.isEntityAlive() && (double)this.getDistance(this.shadowLockedTarget) < (double)40.0F && this.shadowTargetLockTicks > 0) {
            target = this.shadowLockedTarget;
            this.setAttackTarget(target);
         } else {
            EntityLivingBase bestTarget = this.findBestShadowTarget(target);
            if (bestTarget != null && bestTarget != this.shadowLockedTarget) {
               this.shadowInCombo = false;
               this.shadowComboStep = 0;
               this.shadowSprinting = false;
               this.shadowSprintTicks = 0;
               this.shadowAdvancing = false;
               this.shadowRetreating = false;
               this.shadowHeavyWindup = false;
               target = bestTarget;
            } else if (bestTarget != null) {
               target = bestTarget;
            }

            this.shadowLockedTarget = target;
            this.setAttackTarget(target);
            this.shadowTargetLockTicks = this.shadowPhase >= 3 ? 80 : 100;
         }

         dist = (double)this.getDistance(target);
         this.processShadowCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.shadowPhaseTransitioning) {
            ++this.shadowPhaseTransitionTicks;
            if (this.shadowPhaseTransitionTicks > 60) {
               System.out.println("[ShadowBoss] Safety: phase transition exceeded 60 ticks, force-clearing");
               this.shadowPhaseTransitioning = false;
               this.shadowPhaseTransitionTicks = 0;
            }
         }

         if (this.shadowSubCD > 0) {
            --this.shadowSubCD;
         }

         if (this.shadowGlobalJutsuCD > 0) {
            --this.shadowGlobalJutsuCD;
         }

         if (this.shadowFireCD1 > 0) {
            --this.shadowFireCD1;
         }

         if (this.shadowFireCD2 > 0) {
            --this.shadowFireCD2;
         }

         if (this.shadowFireCD3 > 0) {
            --this.shadowFireCD3;
         }

         if (this.shadowWindCD1 > 0) {
            --this.shadowWindCD1;
         }

         if (this.shadowWindCD2 > 0) {
            --this.shadowWindCD2;
         }

         if (this.shadowWaterCD1 > 0) {
            --this.shadowWaterCD1;
         }

         if (this.shadowLightningCD1 > 0) {
            --this.shadowLightningCD1;
         }

         if (this.shadowLavaCD > 0) {
            --this.shadowLavaCD;
         }

         if (this.shadowIceCD > 0) {
            --this.shadowIceCD;
         }

         if (this.shadowStormCD > 0) {
            --this.shadowStormCD;
         }

         if (this.shadowMeleeCD > 0) {
            --this.shadowMeleeCD;
         }

         if (this.shadowHeavyMeleeCD > 0) {
            --this.shadowHeavyMeleeCD;
         }

         if (this.shadowDialogueCD > 0) {
            --this.shadowDialogueCD;
         }

         if (this.shadowSubCounterWindow > 0) {
            --this.shadowSubCounterWindow;
         }

         if (this.shadowDamageDecayTimer > 0) {
            --this.shadowDamageDecayTimer;
         } else if (this.shadowRecentDamage > 0.0F) {
            this.shadowRecentDamage -= 2.0F;
            this.shadowDamageDecayTimer = 10;
         }

         if (this.shadowAggression > 30) {
            --this.shadowAggression;
         }

         if (this.shadowMirrorCD > 0) {
            --this.shadowMirrorCD;
         }

         if (this.shadowCloneCD > 0) {
            --this.shadowCloneCD;
         }

         if (this.shadowFlickerCD > 0) {
            --this.shadowFlickerCD;
         }

         if (this.shadowTargetLockTicks > 0) {
            --this.shadowTargetLockTicks;
         }

      }

      protected void onForceResetCombatState() {
         super.onForceResetCombatState();
         if (this.shadowPhaseTransitioning) {
            System.out.println("[ShadowBoss] Safety: clearing shadowPhaseTransitioning (was at tick " + this.shadowPhaseTransitionTicks + ")");
            this.shadowPhaseTransitioning = false;
            this.shadowPhaseTransitionTicks = 0;
         }

         if (this.shadowMirrorStance) {
            System.out.println("[ShadowBoss] Safety: clearing shadowMirrorStance");
            this.shadowMirrorStance = false;
            this.shadowMirrorTicks = 0;
         }

         this.shadowInCombo = false;
         this.shadowHeavyWindup = false;
         this.shadowSprinting = false;
      }

      protected void resetCombatState() {
         this.shadowPhase = 1;
         this.shadowPhaseTransitioning = false;
         this.shadowPhaseTransitionTicks = 0;
         this.shadowStrafeDir = 1;
         this.shadowStrafeTicks = 0;
         this.shadowSprinting = false;
         this.shadowSprintTicks = 0;
         this.shadowRetreating = false;
         this.shadowAdvancing = false;
         this.shadowInPressure = true;
         this.shadowRhythmTicks = 0;
         this.shadowRhythmDuration = 80;
         this.shadowAggression = 50;
         this.shadowSubCD = 0;
         this.shadowJustSubbed = false;
         this.shadowSubCounterWindow = 0;
         this.shadowInCombo = false;
         this.shadowComboStep = 0;
         this.shadowComboLength = 0;
         this.shadowComboTicks = 0;
         this.shadowComboInterval = 10;
         this.shadowGlobalJutsuCD = 0;
         this.shadowFireCD1 = 0;
         this.shadowFireCD2 = 0;
         this.shadowFireCD3 = 0;
         this.shadowWindCD1 = 0;
         this.shadowWindCD2 = 0;
         this.shadowWaterCD1 = 0;
         this.shadowLightningCD1 = 0;
         this.shadowLavaCD = 0;
         this.shadowIceCD = 0;
         this.shadowStormCD = 0;
         this.shadowMeleeCD = 0;
         this.shadowHeavyMeleeCD = 0;
         this.shadowHeavyWindup = false;
         this.shadowHeavyWindupTicks = 0;
         this.shadowDialogueCD = 0;
         this.shadowIntroPlayed = false;
         this.shadowLowHPPlayed = false;
         this.shadowRecentDamage = 0.0F;
         this.shadowDamageDecayTimer = 0;
         this.shadowMirrorStance = false;
         this.shadowMirrorTicks = 0;
         this.shadowMirrorCD = 0;
         this.shadowCloneCD = 0;
         this.shadowLockedTarget = null;
         this.shadowTargetLockTicks = 0;
         this.shadowFlickerCD = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setInteger("shadowPhase", this.shadowPhase);
         compound.setBoolean("shadowIntroPlayed", this.shadowIntroPlayed);
         compound.setBoolean("shadowLowHPPlayed", this.shadowLowHPPlayed);
         compound.setInteger("shadowSubCD", this.shadowSubCD);
         compound.setInteger("shadowGlobalJutsuCD", this.shadowGlobalJutsuCD);
         compound.setInteger("shadowMirrorCD", this.shadowMirrorCD);
         compound.setInteger("shadowCloneCD", this.shadowCloneCD);
         compound.setInteger("shadowAggression", this.shadowAggression);
         compound.setBoolean("shadowInPressure", this.shadowInPressure);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.shadowPhase = compound.hasKey("shadowPhase") ? compound.getInteger("shadowPhase") : 1;
         this.shadowIntroPlayed = compound.getBoolean("shadowIntroPlayed");
         this.shadowLowHPPlayed = compound.getBoolean("shadowLowHPPlayed");
         this.shadowSubCD = compound.hasKey("shadowSubCD") ? compound.getInteger("shadowSubCD") : 0;
         this.shadowGlobalJutsuCD = compound.hasKey("shadowGlobalJutsuCD") ? compound.getInteger("shadowGlobalJutsuCD") : 0;
         this.shadowMirrorCD = compound.hasKey("shadowMirrorCD") ? compound.getInteger("shadowMirrorCD") : 0;
         this.shadowCloneCD = compound.hasKey("shadowCloneCD") ? compound.getInteger("shadowCloneCD") : 0;
         this.shadowAggression = compound.hasKey("shadowAggression") ? compound.getInteger("shadowAggression") : 50;
         this.shadowInPressure = compound.hasKey("shadowInPressure") ? compound.getBoolean("shadowInPressure") : true;
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         Entity trueSource = source.getTrueSource();
         if (!this.world.isRemote && this.shadowSubCD <= 0 && trueSource instanceof EntityLivingBase && !this.shadowPhaseTransitioning) {
            float subChance = 0.3F + (float)this.shadowPhase * 0.05F;
            if (this.rand.nextFloat() < subChance) {
               EntityLivingBase attacker = (EntityLivingBase)trueSource;
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 25, (double)0.5F, 0.6, (double)0.5F, 0.08, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)0.5F, this.posZ, 15, 0.4, 0.4, 0.4, 0.05, new int[0]);
               }

               try {
                  Entity log = EntityList.createEntityByIDFromName(new ResourceLocation("inftsukaddon", "sublog"), this.world);
                  if (log != null) {
                     log.setPosition(this.posX, this.posY, this.posZ);
                     this.world.spawnEntity(log);
                  }
               } catch (Exception e) {
                  System.err.println("[InfTsuk] ShadowBoss failed to spawn SubLog: " + e.getMessage());
               }

               double dx2 = this.posX - attacker.posX;
               double dz2 = this.posZ - attacker.posZ;
               double len2 = Math.sqrt(dx2 * dx2 + dz2 * dz2);
               if (len2 < 0.1) {
                  len2 = (double)1.0F;
               }

               double perpX;
               double perpZ;
               if (this.rand.nextBoolean()) {
                  perpX = -dz2 / len2;
                  perpZ = dx2 / len2;
               } else {
                  perpX = dz2 / len2;
                  perpZ = -dx2 / len2;
               }

               if (this.rand.nextFloat() < 0.3F) {
                  float aYaw = attacker.rotationYaw * ((float)Math.PI / 180F);
                  perpX = Math.sin((double)aYaw);
                  perpZ = -Math.cos((double)aYaw);
               }

               double newX = this.posX + perpX * (double)5.0F;
               double newZ = this.posZ + perpZ * (double)5.0F;
               if (this.world.isBlockLoaded(new BlockPos(newX, this.posY, newZ)) && this.isPositionSafe(newX, this.posY, newZ)) {
                  this.internalReposition = true;
                  this.setPositionAndUpdate(newX, this.posY, newZ);
                  this.internalReposition = false;
               }

               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)0.5F, this.posZ, 12, 0.3, 0.4, 0.3, 0.06, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)1.0F, this.posZ, 8, 0.3, 0.3, 0.3, 0.08, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.7F, 1.2F);
               this.shadowSubCD = 120;
               this.shadowJustSubbed = true;
               this.shadowSubCounterWindow = 15;
               this.shadowSay("§8" + this.getSubTaunt());
               return -1.0F;
            }
         }

         if (this.shadowPhaseTransitioning && this.shadowPhaseTransitionTicks <= 40) {
            if (this.ticksExisted % 20 == 0) {
               System.out.println("[ShadowBoss] Damage blocked: phase transition (tick " + this.shadowPhaseTransitionTicks + "/40, phase " + this.shadowPhase + ")");
            }

            return -1.0F;
         } else {
            if (this.shadowMirrorStance && !this.world.isRemote) {
               if (source.getImmediateSource() != null && !(source.getImmediateSource() instanceof EntityLivingBase)) {
                  Entity proj = source.getImmediateSource();
                  if (trueSource instanceof EntityLivingBase) {
                     EntityLivingBase attacker = (EntityLivingBase)trueSource;
                     attacker.attackEntityFrom(DamageSource.causeMobDamage(this), amount * 0.6F);
                     this.shadowSay("§dReflected!");
                  }

                  proj.setDead();
                  this.shadowMirrorStance = false;
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.15, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.HOSTILE, 1.0F, 1.5F);
                  return -1.0F;
               }

               if (trueSource instanceof EntityLivingBase && this.getDistance(trueSource) <= 5.0F) {
                  EntityLivingBase attacker = (EntityLivingBase)trueSource;
                  float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                  attacker.attackEntityFrom(DamageSource.causeMobDamage(this), baseDmg * 1.2F);
                  attacker.hurtResistantTime = 0;
                  attacker.attackEntityFrom(DamageSource.MAGIC, 4.0F);
                  this.swingArm(EnumHand.MAIN_HAND);
                  this.shadowMirrorStance = false;
                  this.shadowSay("§dPredictable.");
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.4, 0.4, 0.4, 0.15, new int[0]);
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.posX, this.posY + (double)1.0F, this.posZ, 3, 0.3, 0.2, 0.3, 0.1, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.HOSTILE, 1.0F, 0.8F);
                  return -1.0F;
               }
            }

            if (!this.world.isRemote) {
               this.shadowRecentDamage += amount * 3.0F;
               this.shadowDamageDecayTimer = 20;
               this.shadowAggression = Math.min(100, this.shadowAggression + 10);
               this.checkShadowPhaseTransition();
               if (!this.shadowLowHPPlayed && this.getHealth() <= this.getMaxHealth() * 0.15F) {
                  this.shadowLowHPPlayed = true;
                  String[] lowLines;
                  if (this.isShadowQuestBoss()) {
                     lowLines = new String[]{"Hah... hahaha... you really have surpassed me...", "So this is it... this is how our story ends...", "Maybe... maybe you made the right choice after all..."};
                  } else if (this.isAkatsukiShadowNpc()) {
                     lowLines = new String[]{"The forbidden scrolls... all that research... for nothing?", "Impossible... the Akatsuki's techniques should be invincible...", "No... my research can't end here... not like this..."};
                  } else {
                     lowLines = new String[]{"Tch... you're stronger than I thought...", "So this is how it ends...", "Heh... not bad... not bad at all..."};
                  }

                  this.shadowSay("§7§o" + lowLines[this.rand.nextInt(lowLines.length)]);
               }
            }

            return amount;
         }
      }

      protected boolean usesVanillaMeleeAI() {
         return false;
      }

      private EntityLivingBase findBestShadowTarget(EntityLivingBase fallback) {
         List<EntityPlayer> nearby = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)40.0F), (px) -> px != null && px.isEntityAlive() && !px.isSpectator());
         if (nearby.isEmpty()) {
            return fallback;
         } else if (nearby.size() == 1) {
            return (EntityLivingBase)nearby.get(0);
         } else {
            EntityLivingBase best = fallback;
            double bestScore = (double)-1.0F;

            for(EntityPlayer p : nearby) {
               double d = (double)this.getDistance(p);
               if (!(d > (double)40.0F)) {
                  double score = (double)40.0F - d;
                  if (this.shadowLockedTarget != null && !p.getUniqueID().equals(this.shadowLockedTarget.getUniqueID())) {
                     score += (double)15.0F;
                  }

                  if (p.getUniqueID().equals(this.getLastAttackerUUID())) {
                     score += (double)10.0F;
                  }

                  if (score > bestScore) {
                     bestScore = score;
                     best = p;
                  }
               }
            }

            return best;
         }
      }

      private UUID getLastAttackerUUID() {
         EntityLivingBase attacker = this.getRevengeTarget();
         return attacker != null ? attacker.getUniqueID() : null;
      }

      private void processShadowCombat(EntityLivingBase target, double dist) {
         if (this.shadowPhaseTransitioning) {
            if (this.shadowPhaseTransitionTicks % 5 == 0 && this.world instanceof WorldServer) {
               this.spawnShadowAura();
            }

            if (this.shadowPhaseTransitionTicks > 40) {
               this.shadowPhaseTransitioning = false;
            }

            this.faceEntity(target, 30.0F, 30.0F);
            this.shadowPerformMovement(target, dist);
         } else if (this.shadowHeavyWindup) {
            ++this.shadowHeavyWindupTicks;
            if (this.shadowHeavyWindupTicks % 4 == 0) {
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)1.5F, this.posZ, 4, 0.2, 0.2, 0.2, 0.05, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + 1.2, this.posZ, 3, 0.15, 0.15, 0.15, 0.03, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.HOSTILE, 0.3F, 1.4F);
            }

            if (this.shadowHeavyWindupTicks >= 12) {
               this.shadowHeavyWindup = false;
               this.shadowHeavyMeleeCD = 35;
               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               float heavyNormal;
               float heavyTrue;
               if (this.trueDamageSplit > 0.0F) {
                  float heavySplit = Math.min(1.0F, this.trueDamageSplit + 0.1F);
                  heavyNormal = baseDmg * 1.8F * (1.0F - heavySplit);
                  heavyTrue = baseDmg * 1.8F * heavySplit * this.trueDamageMultiplier;
               } else {
                  heavyNormal = baseDmg * 1.8F;
                  heavyTrue = (this.combatTier >= 4 ? 16.0F : (this.combatTier >= 3 ? 10.0F : (this.combatTier >= 2 ? 6.0F : 4.0F))) * this.trueDamageMultiplier;
               }

               for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)5.0F))) {
                  if (p.isEntityAlive()) {
                     p.attackEntityFrom(DamageSource.causeMobDamage(this), heavyNormal);
                     p.hurtResistantTime = 0;
                     p.attackEntityFrom(DamageSource.MAGIC, heavyTrue);
                     double dx = p.posX - this.posX;
                     double dz = p.posZ - this.posZ;
                     double len = Math.sqrt(dx * dx + dz * dz);
                     if (len > 0.1) {
                        p.motionX += dx / len * (double)1.0F;
                        p.motionY += 0.35;
                        p.motionZ += dz / len * (double)1.0F;
                        p.velocityChanged = true;
                     }
                  }
               }

               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.4, 0.4, 0.4, 0.15, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.posX, this.posY + (double)1.0F, this.posZ, 4, 0.3, 0.2, 0.3, 0.12, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 1.0F, 0.9F);
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.HOSTILE, 0.7F, 1.0F);
               this.shadowSayIfReady(this.getShadowHitTaunt());
            }

         } else if (this.shadowInCombo) {
            ++this.shadowComboTicks;
            this.faceEntity(target, 30.0F, 30.0F);
            this.shadowPerformMovement(target, dist);
            if (this.shadowComboTicks >= this.shadowComboInterval) {
               this.shadowComboTicks = 0;
               ++this.shadowComboStep;
               if (this.shadowComboStep < this.shadowComboLength) {
                  if (dist <= (double)4.0F && this.rand.nextFloat() < 0.6F) {
                     this.doShadowMelee(target);
                     this.shadowComboInterval = 8 + this.rand.nextInt(8);
                  } else {
                     if (!this.tryShadowKG() && !this.tryShadowJutsu(target, dist)) {
                        this.shadowSprinting = true;
                        this.shadowSprintTicks = 0;
                     }

                     this.shadowComboInterval = 15 + this.rand.nextInt(11);
                  }
               } else {
                  if (!this.tryShadowKG()) {
                     if (dist <= (double)6.0F) {
                        this.startShadowHeavyMelee(target);
                     } else {
                        this.tryShadowJutsu(target, dist);
                     }
                  }

                  this.shadowInCombo = false;
               }
            }

         } else if (this.shadowJustSubbed && this.shadowSubCounterWindow > 0) {
            this.shadowJustSubbed = false;
            this.shadowSprinting = true;
            this.shadowSprintTicks = 0;
            if (dist <= (double)4.0F) {
               this.doShadowMelee(target);
               this.startShadowCombo(target);
            }

         } else {
            if (!this.shadowIntroPlayed) {
               this.shadowIntroPlayed = true;
               String[] intros;
               if (this.isShadowQuestBoss()) {
                  intros = new String[]{"So... we finally meet. Do you remember me?", "I've been waiting for this moment... waiting to see what you've become.", "Look at you now. You've grown... but are you strong enough to face your past?", "Every choice you made led you here. Every path not taken... led to me."};
               } else if (this.isAkatsukiShadowNpc()) {
                  intros = new String[]{"So, a hunter comes for me... The Akatsuki warned me this day would come.", "You're after the forbidden scrolls? You have no idea what you're dealing with.", "Another shinobi sent to silence my research... How predictable.", "The secrets I've uncovered will reshape the shinobi world. You won't stop that."};
               } else {
                  intros = new String[]{"Another one... You really think you can take me down?", "I've been expecting someone like you. Let's see what you're made of.", "You've found me. But finding me and defeating me are very different things.", "So the hunter becomes the hunted... or so you think."};
               }

               this.shadowSay("§5§l" + intros[this.rand.nextInt(intros.length)]);
            }

            this.shadowUpdateRhythm();
            this.shadowPerformMovement(target, dist);
            if (this.shadowPhase >= 2 && this.rand.nextFloat() < 0.15F) {
               this.spawnShadowAura();
            }

            this.faceEntity(target, 30.0F, 30.0F);
            if (this.shadowInPressure) {
               if (this.tryShadowKG()) {
                  return;
               }

               if (dist <= (double)4.0F) {
                  if (this.shadowMeleeCD <= 0) {
                     if (this.rand.nextFloat() < 0.4F + (float)this.shadowAggression * 0.005F) {
                        this.startShadowCombo(target);
                        if (!this.shadowInCombo) {
                           this.doShadowMelee(target);
                        }
                     } else {
                        this.doShadowMelee(target);
                     }
                  } else if (this.shadowHeavyMeleeCD <= 0 && this.rand.nextFloat() < 0.3F) {
                     this.startShadowHeavyMelee(target);
                  } else {
                     float roll = this.rand.nextFloat();
                     if (roll < 0.25F) {
                        this.tryShadowJutsu(target, dist);
                     } else if (roll < 0.6F) {
                        this.doShadowQuickJab(target);
                     }
                  }
               } else if (dist <= (double)6.0F) {
                  float closeRoll = this.rand.nextFloat();
                  if (closeRoll < 0.4F) {
                     this.doShadowFlicker(target);
                  } else if (closeRoll < 0.7F) {
                     this.shadowSprinting = true;
                     this.shadowSprintTicks = 0;
                  } else if (!this.tryShadowJutsu(target, dist)) {
                     this.doShadowFlicker(target);
                     if (this.shadowFlickerCD > 0) {
                        this.shadowSprinting = true;
                        this.shadowSprintTicks = 0;
                     }
                  }
               } else if (dist <= (double)12.0F) {
                  float midRoll = this.rand.nextFloat();
                  if (midRoll < 0.35F) {
                     this.doShadowFlicker(target);
                     if (this.shadowFlickerCD > 0 && dist > (double)6.0F) {
                        this.shadowSprinting = true;
                        this.shadowSprintTicks = 0;
                     }
                  } else if (midRoll < 0.65F) {
                     if (!this.tryShadowJutsu(target, dist)) {
                        this.shadowSprinting = true;
                        this.shadowSprintTicks = 0;
                     }
                  } else {
                     this.shadowSprinting = true;
                     this.shadowSprintTicks = 0;
                  }
               } else {
                  this.shadowSprinting = true;
                  this.shadowSprintTicks = 0;
                  if (this.shadowFlickerCD <= 0 && dist <= (double)20.0F) {
                     this.doShadowFlicker(target);
                  } else {
                     this.tryShadowJutsu(target, dist);
                  }
               }
            } else {
               this.processShadowMirrorStance();
               if (this.rand.nextFloat() < 0.2F && this.tryShadowKG()) {
                  return;
               }

               if (!this.shadowMirrorStance) {
                  this.tryShadowMirrorStance();
               }

               if (dist <= (double)4.0F && this.shadowMeleeCD <= 0) {
                  this.doShadowMelee(target);
               } else if (dist <= (double)4.0F && this.shadowMeleeCD > 0) {
                  if (this.rand.nextFloat() < 0.4F) {
                     this.doShadowQuickJab(target);
                  }
               } else if (dist > (double)12.0F) {
                  this.shadowSprinting = true;
                  this.shadowSprintTicks = 0;
                  if (this.rand.nextFloat() < 0.25F) {
                     this.tryShadowJutsu(target, dist);
                  }

                  if (this.rand.nextFloat() < 0.1F) {
                     this.tryShadowCloneDecoy(target);
                  }
               } else if (this.shadowFlickerCD <= 0 && this.rand.nextFloat() < 0.3F) {
                  this.doShadowFlicker(target);
               } else if (!this.tryShadowJutsu(target, dist)) {
                  this.shadowSprinting = true;
                  this.shadowSprintTicks = 0;
                  this.getNavigator().tryMoveToEntityLiving(target, 1.4);
               }
            }

            if (this.shadowInPressure && this.shadowPhase >= 3 && this.getHealth() / this.getMaxHealth() < 0.4F && this.rand.nextFloat() < 0.05F) {
               this.tryShadowCloneDecoy(target);
            }

            this.shadowPlayCombatLine();
         }
      }

      private void shadowPerformMovement(EntityLivingBase target, double dist) {
         if (!this.shadowHeavyWindup) {
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 0.1) {
               len = (double)1.0F;
            }

            double moveX = (double)0.0F;
            double moveZ = (double)0.0F;
            double speed = (double)0.25F;
            if (this.shadowSprinting) {
               ++this.shadowSprintTicks;
               speed = (double)0.5F;
               if (this.shadowSprintTicks >= 15) {
                  this.shadowSprinting = false;
                  this.shadowSprintTicks = 0;
               }
            }

            if (this.shadowRecentDamage > 20.0F && dist < (double)12.0F) {
               this.shadowRetreating = true;
               this.shadowAdvancing = false;
            }

            if (dist > (double)5.0F && this.shadowRecentDamage < 10.0F && (this.shadowInPressure || dist > (double)12.0F && this.shadowSprinting)) {
               this.shadowAdvancing = true;
               this.shadowRetreating = false;
            }

            if (this.shadowRetreating && dist < (double)20.0F) {
               moveX = -dx / len * 0.35;
               moveZ = -dz / len * 0.35;
               double perpX = -dz / len;
               double perpZ = dx / len;
               moveX += perpX * (double)this.shadowStrafeDir * (double)0.125F;
               moveZ += perpZ * (double)this.shadowStrafeDir * (double)0.125F;
               if (dist >= (double)12.0F) {
                  this.shadowRetreating = false;
               }
            } else if (this.shadowAdvancing && dist > (double)4.0F) {
               moveX = dx / len * 0.3;
               moveZ = dz / len * 0.3;
               if (dist <= (double)4.0F) {
                  this.shadowAdvancing = false;
               }
            } else {
               ++this.shadowStrafeTicks;
               if (this.shadowStrafeTicks >= 40) {
                  this.shadowStrafeTicks = 0;
                  this.shadowStrafeDir = this.rand.nextBoolean() ? 1 : -1;
               }

               double perpX = -dz / len;
               double perpZ = dx / len;
               moveX = perpX * (double)this.shadowStrafeDir * speed;
               moveZ = perpZ * (double)this.shadowStrafeDir * speed;
               if (!(dist < (double)3.0F) || this.shadowInPressure && dist <= (double)4.0F) {
                  if (dist > (double)7.0F) {
                     moveX += dx / len * speed * 0.3;
                     moveZ += dz / len * speed * 0.3;
                  }
               } else {
                  moveX -= dx / len * speed * 0.3;
                  moveZ -= dz / len * speed * 0.3;
               }
            }

            double moveSpeed = this.shadowSprinting ? 0.35 : 0.18;
            double mvLen = Math.sqrt(moveX * moveX + moveZ * moveZ);
            if (mvLen > 0.01) {
               this.motionX = moveX / mvLen * moveSpeed;
               this.motionZ = moveZ / mvLen * moveSpeed;
               this.velocityChanged = true;
            }

            this.getNavigator().clearPath();
            if (this.onGround) {
               boolean shouldJump = false;
               if (this.shadowSprinting && dist > (double)6.0F && this.rand.nextFloat() < 0.15F) {
                  shouldJump = true;
               } else if (this.shadowRetreating && this.rand.nextFloat() < 0.1F) {
                  shouldJump = true;
               } else if (this.collidedHorizontally && this.rand.nextFloat() < 0.5F) {
                  shouldJump = true;
               } else if (this.shadowInPressure && dist <= (double)6.0F && this.rand.nextFloat() < 0.03F) {
                  shouldJump = true;
               }

               if (shouldJump) {
                  this.motionY = 0.42;
                  this.velocityChanged = true;
               }
            }

            if (this.shadowSprinting && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.3, this.posZ, 2, 0.2, 0.15, 0.2, 0.04, new int[0]);
            }

         }
      }

      private void shadowUpdateRhythm() {
         ++this.shadowRhythmTicks;
         if (this.shadowInPressure) {
            if (this.shadowRhythmTicks >= this.shadowRhythmDuration) {
               this.shadowInPressure = false;
               this.shadowRhythmTicks = 0;
               this.shadowRhythmDuration = 30 + this.rand.nextInt(31);
            }
         } else if (this.shadowRhythmTicks >= this.shadowRhythmDuration) {
            this.shadowInPressure = true;
            this.shadowRhythmTicks = 0;
            this.shadowRhythmDuration = 60 + this.rand.nextInt(61);
            if (this.rand.nextFloat() < 0.5F) {
               this.shadowSprinting = true;
               this.shadowSprintTicks = 0;
            }
         }

      }

      private void doShadowMelee(EntityLivingBase target) {
         if (target != null && target.isEntityAlive()) {
            if (!(this.getDistance(target) > 5.0F)) {
               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               float dmgMul = (float)this.getDamageMultiplier();
               float meleeDmg;
               float meleeTrue;
               if (this.trueDamageSplit > 0.0F) {
                  meleeDmg = baseDmg * (1.0F - this.trueDamageSplit) * dmgMul;
                  meleeTrue = baseDmg * this.trueDamageSplit * dmgMul * this.trueDamageMultiplier;
               } else {
                  meleeDmg = baseDmg * dmgMul;
                  meleeTrue = (this.combatTier >= 4 ? 8.0F : (this.combatTier >= 3 ? 5.0F : (this.combatTier >= 2 ? 3.5F : 2.0F))) * this.trueDamageMultiplier;
               }

               target.attackEntityFrom(DamageSource.causeMobDamage(this), meleeDmg);
               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.MAGIC, meleeTrue);
               double dx = target.posX - this.posX;
               double dz = target.posZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > 0.1) {
                  target.motionX += dx / len * 0.4;
                  target.motionY += 0.15;
                  target.motionZ += dz / len * 0.4;
                  target.velocityChanged = true;
               }

               this.swingArm(EnumHand.MAIN_HAND);
               this.shadowMeleeCD = 8;
               this.shadowAggression = Math.min(100, this.shadowAggression + 15);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 6, (double)0.25F, (double)0.25F, (double)0.25F, 0.08, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, target.posX, target.posY + (double)1.0F, target.posZ, 2, 0.2, 0.15, 0.2, 0.1, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 0.8F, 1.1F);
            }
         }
      }

      private void doShadowQuickJab(EntityLivingBase target) {
         if (target != null && target.isEntityAlive()) {
            if (!(this.getDistance(target) > 5.0F)) {
               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               float dmgMul = (float)this.getDamageMultiplier();
               float jabDmg;
               float jabTrue;
               if (this.trueDamageSplit > 0.0F) {
                  jabDmg = baseDmg * 0.4F * (1.0F - this.trueDamageSplit) * dmgMul;
                  jabTrue = baseDmg * 0.4F * this.trueDamageSplit * dmgMul * this.trueDamageMultiplier;
               } else {
                  jabDmg = baseDmg * 0.4F * dmgMul;
                  jabTrue = (this.combatTier >= 4 ? 4.0F : (this.combatTier >= 3 ? 2.5F : (this.combatTier >= 2 ? 1.5F : 1.0F))) * this.trueDamageMultiplier;
               }

               target.attackEntityFrom(DamageSource.causeMobDamage(this), jabDmg);
               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.MAGIC, jabTrue);
               this.swingArm(this.rand.nextBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
               double dx = target.posX - this.posX;
               double dz = target.posZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > 0.1) {
                  target.motionX += dx / len * 0.15;
                  target.motionZ += dz / len * 0.15;
                  target.velocityChanged = true;
               }

               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 2, 0.15, 0.15, 0.15, 0.05, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, SoundCategory.HOSTILE, 0.6F, 1.3F);
            }
         }
      }

      private void doShadowFlicker(EntityLivingBase target) {
         if (target != null && target.isEntityAlive()) {
            if (this.shadowFlickerCD <= 0) {
               double dist = (double)this.getDistance(target);
               if (!(dist < (double)3.0F) && !(dist > (double)20.0F)) {
                  double closeFraction = 0.6 + this.rand.nextDouble() * 0.2;
                  double dx = target.posX - this.posX;
                  double dz = target.posZ - this.posZ;
                  double newX = this.posX + dx * closeFraction;
                  double newZ = this.posZ + dz * closeFraction;
                  double newY = this.posY;
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 12, 0.4, 0.6, 0.4, 0.03, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.6F, 1.4F);
                  this.setPositionAndUpdate(newX, newY, newZ);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, newX, newY + (double)1.0F, newZ, 8, 0.3, (double)0.5F, 0.3, 0.03, new int[0]);
                  }

                  if (this.getDistance(target) <= 5.0F) {
                     this.doShadowMelee(target);
                  }

                  this.shadowFlickerCD = 50 + this.rand.nextInt(30);
               }
            }
         }
      }

      private void startShadowHeavyMelee(EntityLivingBase target) {
         if (this.shadowHeavyMeleeCD <= 0 && !this.shadowHeavyWindup) {
            if (target != null && !(this.getDistance(target) > 6.0F)) {
               this.shadowHeavyWindup = true;
               this.shadowHeavyWindupTicks = 0;
               this.faceEntity(target, 30.0F, 30.0F);
            }
         }
      }

      private boolean startShadowCombo(EntityLivingBase target) {
         if (this.shadowInCombo) {
            return false;
         } else {
            float comboChance = 0.35F + (float)this.shadowPhase * 0.08F + (float)this.shadowAggression * 0.003F;
            if (this.rand.nextFloat() > comboChance) {
               return false;
            } else {
               this.shadowInCombo = true;
               this.shadowComboStep = 0;
               this.shadowComboLength = 2 + Math.min(this.rand.nextInt(3), this.shadowPhase);
               this.shadowComboTicks = 0;
               this.shadowComboInterval = 8 + this.rand.nextInt(11);
               return true;
            }
         }
      }

      private boolean tryShadowJutsu(EntityLivingBase target, double dist) {
         if (this.shadowGlobalJutsuCD > 0) {
            return false;
         } else if (target != null && target.isEntityAlive()) {
            ArrayList<int[]> available = new ArrayList();
            if (this.shadowPhase >= 1 && this.shadowFireCD1 <= 0) {
               available.add(new int[]{1, 80});
            }

            if (this.shadowPhase >= 1 && this.shadowWindCD1 <= 0) {
               available.add(new int[]{4, 100});
            }

            if (this.shadowPhase >= 2 && this.shadowFireCD2 <= 0) {
               available.add(new int[]{2, 140});
            }

            if (this.shadowPhase >= 2 && this.shadowWaterCD1 <= 0) {
               available.add(new int[]{6, 120});
            }

            if (this.shadowPhase >= 3 && this.shadowLightningCD1 <= 0) {
               available.add(new int[]{7, 160});
            }

            if (this.shadowPhase >= 3 && this.shadowWindCD2 <= 0) {
               available.add(new int[]{5, 150});
            }

            if (this.shadowPhase >= 4 && this.shadowFireCD3 <= 0) {
               available.add(new int[]{3, 50});
            }

            if (available.isEmpty()) {
               return false;
            } else {
               int[] chosen = (int[])available.get(this.rand.nextInt(available.size()));
               return this.executeShadowJutsu(target, chosen[0], chosen[1]);
            }
         } else {
            return false;
         }
      }

      private boolean executeShadowJutsu(EntityLivingBase target, int jutsuType, int cooldown) {
         double dx = target.posX - this.posX;
         double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
         double dz = target.posZ - this.posZ;
         double dist = Math.sqrt(dx * dx + dz * dz);
         float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         float jutsuTrue = (this.combatTier >= 4 ? 8.0F : (this.combatTier >= 3 ? 5.0F : (this.combatTier >= 2 ? 3.5F : 2.0F))) * this.trueDamageMultiplier;
         switch (jutsuType) {
            case 1:
               EntityKatonFireball.EntityCustom fb = new EntityKatonFireball.EntityCustom(this.world, this, baseDmg * 0.8F, jutsuTrue);
               fb.shoot(dx, dy + dist * 0.01, dz, 1.2F, 0.8F);
               this.world.spawnEntity(fb);
               this.shadowFireCD1 = cooldown;
               break;
            case 2:
               EntityKatonFireball.EntityCustom fb = new EntityKatonFireball.EntityCustom(this.world, this, baseDmg * 1.2F, jutsuTrue * 1.3F);
               fb.shoot(dx, dy + dist * 0.01, dz, 1.4F, 0.6F);
               this.world.spawnEntity(fb);
               this.shadowFireCD2 = cooldown;
               break;
            case 3:
               EntityKatonFireball.EntityCustom fb = new EntityKatonFireball.EntityCustom(this.world, this, baseDmg * 0.6F, jutsuTrue * 0.7F);
               fb.shoot(dx, dy + dist * 0.01, dz, 1.6F, 1.0F);
               this.world.spawnEntity(fb);
               this.shadowFireCD3 = cooldown;
               break;
            case 4:
               EntityFutonBullet.EntityCustom wind = new EntityFutonBullet.EntityCustom(this.world, this, baseDmg * 0.7F, jutsuTrue);
               wind.shoot(dx, dy + dist * 0.01, dz, 1.3F, 0.8F);
               this.world.spawnEntity(wind);
               this.shadowWindCD1 = cooldown;
               break;
            case 5:
               EntityVacuumWave.EntityCustom vw = new EntityVacuumWave.EntityCustom(this.world, this, baseDmg * 1.0F, jutsuTrue * 1.2F);
               vw.shoot(dx, dy + dist * 0.01, dz, 1.8F, 0.4F);
               this.world.spawnEntity(vw);
               this.shadowWindCD2 = cooldown;
               break;
            case 6:
               EntityWaterCannonball.EntityCustom wc = new EntityWaterCannonball.EntityCustom(this.world, this, baseDmg * 1.0F, jutsuTrue);
               wc.shoot(dx, dy + dist * 0.01, dz, 1.1F, 0.6F);
               this.world.spawnEntity(wc);
               this.shadowWaterCD1 = cooldown;
               break;
            case 7:
               EntityRaitonBeam.EntityCustom bolt = new EntityRaitonBeam.EntityCustom(this.world, this, baseDmg * 1.1F, jutsuTrue * 1.3F);
               bolt.shoot(dx, dy + dist * 0.01, dz, 2.0F, 0.3F);
               this.world.spawnEntity(bolt);
               this.shadowLightningCD1 = cooldown;
               break;
            default:
               return false;
         }

         this.shadowGlobalJutsuCD = 8;
         this.swingArm(EnumHand.MAIN_HAND);
         return true;
      }

      private boolean tryShadowKG() {
         if (this.shadowGlobalJutsuCD > 0) {
            return false;
         } else if (this.rand.nextInt(100) >= 40) {
            return false;
         } else {
            EntityLivingBase target = this.getAttackTarget();
            if (target != null && target.isEntityAlive()) {
               double dx = target.posX - this.posX;
               double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
               double dz = target.posZ - this.posZ;
               double dist = Math.sqrt(dx * dx + dz * dz);
               float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               if (this.shadowPhase >= 4 && this.shadowStormCD <= 0) {
                  EntityStormBolt.EntityCustom sb = new EntityStormBolt.EntityCustom(this.world, this, baseDmg * 1.5F, 5.0F);
                  sb.shoot(dx, dy + dist * 0.01, dz, 1.8F, 0.8F);
                  this.world.spawnEntity(sb);
                  this.shadowStormCD = 240;
                  this.shadowGlobalJutsuCD = 8;
                  this.spawnKGParticles(3);
                  this.shadowSay("§d§lStorm Release!");
                  return true;
               } else if (this.shadowPhase >= 3 && this.shadowIceCD <= 0) {
                  for(int i = 0; i < 3; ++i) {
                     double ndx = dx + (this.rand.nextDouble() - (double)0.5F) * 0.8;
                     double ndz = dz + (this.rand.nextDouble() - (double)0.5F) * 0.8;
                     EntityIceNeedle.EntityCustom needle = new EntityIceNeedle.EntityCustom(this.world, this, baseDmg * 0.8F, 3.0F);
                     needle.shoot(ndx, dy + dist * 0.01, ndz, 1.6F, 1.5F);
                     this.world.spawnEntity(needle);
                  }

                  this.shadowIceCD = 220;
                  this.shadowGlobalJutsuCD = 8;
                  this.spawnKGParticles(2);
                  this.shadowSay("§b§lIce Release!");
                  return true;
               } else if (this.shadowPhase >= 2 && this.shadowLavaCD <= 0) {
                  EntityLavaGlob.EntityCustom lava = new EntityLavaGlob.EntityCustom(this.world, this, baseDmg * 1.3F, 4.0F);
                  lava.setEntityScale(15.0F);
                  lava.shoot(dx, dy + dist * 0.01, dz, 1.2F, 1.0F);
                  this.world.spawnEntity(lava);
                  this.shadowLavaCD = 200;
                  this.shadowGlobalJutsuCD = 8;
                  this.spawnKGParticles(1);
                  this.shadowSay("§6§lLava Release!");
                  return true;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      }

      private void spawnKGParticles(int kgType) {
         if (this.world instanceof WorldServer) {
            switch (kgType) {
               case 1:
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 35, 0.8, 0.8, 0.8, 0.12, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.LAVA, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.08, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)1.5F, this.posZ, 10, 0.4, 0.4, 0.4, 0.05, new int[0]);
                  break;
               case 2:
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SNOWBALL, this.posX, this.posY + (double)1.0F, this.posZ, 40, 0.8, 0.8, 0.8, 0.1, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)0.5F, 0.6, (double)0.5F, 0.06, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.5F, this.posZ, 12, 0.4, 0.4, 0.4, 0.08, new int[0]);
                  break;
               case 3:
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)1.0F, (double)1.0F, (double)1.0F, 0.2, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)2.0F, this.posZ, 25, 0.8, (double)1.0F, 0.8, 0.15, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.6, 0.6, 0.6, 0.08, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)2.0F, this.posZ, 10, (double)1.0F, (double)0.5F, (double)1.0F, 0.1, new int[0]);
                  break;
               default:
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 30, 0.8, 0.8, 0.8, 0.1, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.5F, this.posZ, 20, 0.6, 0.6, 0.6, 0.15, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.12, new int[0]);
            }
         }

         if (kgType == 2) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SNOWMAN_HURT, SoundCategory.HOSTILE, 1.0F, 0.6F);
         } else if (kgType == 3) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 0.5F, 1.5F);
         } else {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 0.8F, 0.8F);
         }

      }

      private void checkShadowPhaseTransition() {
         float hpPercent = this.getHealth() / this.getMaxHealth();
         int newPhase = this.shadowPhase;
         if (hpPercent <= 0.25F && this.shadowPhase < 4) {
            newPhase = 4;
         } else if (hpPercent <= 0.5F && this.shadowPhase < 3) {
            newPhase = 3;
         } else if (hpPercent <= 0.75F && this.shadowPhase < 2) {
            newPhase = 2;
         }

         if (newPhase > this.shadowPhase) {
            this.triggerShadowPhaseTransition(newPhase);
         }

      }

      private void triggerShadowPhaseTransition(int newPhase) {
         this.shadowPhase = newPhase;
         this.shadowPhaseTransitioning = true;
         this.shadowPhaseTransitionTicks = 0;
         this.shadowInPressure = true;
         this.shadowRhythmDuration = 60 + this.rand.nextInt(61);
         this.shadowRhythmTicks = 0;
         this.shadowAggression = Math.min(100, 80);
         this.shadowMirrorStance = false;
         String[][] phaseLines;
         if (this.isShadowQuestBoss()) {
            phaseLines = new String[][]{new String[0], new String[0], {"Heh... you've actually improved. Time to stop playing around.", "Now you're starting to remind me of... us.", "Let's see how you handle THIS!"}, {"ENOUGH! You want to see real power?!", "I won't hold back anymore... neither should you!", "This is what we were MEANT to become!"}, {"NO! I won't disappear! I WON'T!", "If I'm going down... I'M TAKING YOU WITH ME!", "THIS IS MY FINAL STAND!"}};
         } else if (this.isAkatsukiShadowNpc()) {
            phaseLines = new String[][]{new String[0], new String[0], {"You're persistent... but you know nothing of true power.", "The Akatsuki's research cannot be stopped by the likes of you!", "Time to show you what forbidden jutsu really means!"}, {"These forbidden techniques... I'll show you why they were forbidden!", "You've forced my hand. Witness the fruits of my research!", "Every scroll I stole... every secret I uncovered... ALL OF IT LEADS HERE!"}, {"THE AKATSUKI WILL NOT FALL! NOT WHILE I STILL BREATHE!", "THESE FORBIDDEN JUTSU ARE MY LEGACY... AND YOUR GRAVE!", "I'LL UNLEASH EVERYTHING... EVERY LAST FORBIDDEN TECHNIQUE!"}};
         } else {
            phaseLines = new String[][]{new String[0], new String[0], {"Heh... not bad. But I'm just getting started!", "You've forced me to get serious. Bad move.", "Let's see how you handle THIS!"}, {"ENOUGH! No more holding back!", "You want to see what I'm really capable of?!", "This is my true power!"}, {"I WON'T GO DOWN LIKE THIS!", "If I'm going down... I'M TAKING YOU WITH ME!", "THIS IS MY FINAL STAND!"}};
         }

         if (newPhase >= 2 && newPhase <= 4) {
            String[] lines = phaseLines[newPhase];
            this.shadowSay("§5§l" + lines[this.rand.nextInt(lines.length)]);
            if (this.isShadowQuestBoss()) {
               String[][] memoryFlashes = new String[][]{new String[0], new String[0], {"§7§oYou remember... the wanderer's words by the campfire...", "§7§oThe face in the river... it was always yours..."}, {"§7§oThe missing-nin's conviction echoes in your mind...", "§7§oThe test subject screamed with a voice that wasn't its own..."}, {"§7§oEvery spirit, every trial, every choice — they all led here.", "§7§oThis is the path you chose. No regrets."}};
               String[] memories = memoryFlashes[newPhase];
               if (memories.length > 0) {
                  String memory = memories[this.rand.nextInt(memories.length)];

                  for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)40.0F))) {
                     p.sendMessage(new TextComponentString(memory));
                  }
               }
            }
         }

         String[] phaseTitles = new String[]{"", "", "Lava Release", "Ice Release", "Storm Release"};
         String[] phaseSubtitles = new String[]{"", "", "§6The earth itself burns!", "§b Frozen beyond time!", "§dAll elements converge!"};
         if (newPhase >= 2 && newPhase <= 4) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)40.0F))) {
               if (p instanceof EntityPlayerMP) {
                  EntityPlayerMP mp = (EntityPlayerMP)p;
                  mp.connection.sendPacket(new SPacketTitle(Type.SUBTITLE, new TextComponentString(phaseSubtitles[newPhase])));
                  mp.connection.sendPacket(new SPacketTitle(Type.TITLE, new TextComponentString("§5§l" + phaseTitles[newPhase])));
                  mp.connection.sendPacket(new SPacketTitle(Type.TIMES, (ITextComponent)null, 10, 40, 20));
               }
            }
         }

         if (this.world instanceof WorldServer) {
            for(int i = 0; i < 50; ++i) {
               double angle = (double)i / (double)50.0F * Math.PI * (double)2.0F;
               double radius = (double)(3 + i % 5);
               double px = this.posX + Math.cos(angle) * radius;
               double pz = this.posZ + Math.sin(angle) * radius;
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, px, this.posY + (double)0.5F + (double)i * 0.1, pz, 2, 0.1, 0.1, 0.1, 0.02, new int[0]);
            }

            if (newPhase == 2) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.LAVA, this.posX, this.posY + (double)1.0F, this.posZ, 50, (double)2.0F, (double)2.0F, (double)2.0F, 0.1, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 80, (double)1.5F, (double)1.5F, (double)1.5F, 0.15, new int[0]);
            } else if (newPhase == 3) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SNOWBALL, this.posX, this.posY + (double)1.0F, this.posZ, 60, (double)2.0F, (double)2.0F, (double)2.0F, 0.1, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 40, (double)1.5F, (double)1.5F, (double)1.5F, 0.08, new int[0]);
            } else if (newPhase == 4) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.0F, this.posZ, 100, (double)2.5F, (double)2.5F, (double)2.5F, 0.2, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)1.0F, this.posZ, 50, (double)2.0F, (double)2.0F, (double)2.0F, 0.15, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)2.0F, this.posZ, 60, (double)3.0F, (double)3.0F, (double)3.0F, 0.3, new int[0]);
            }

            for(int r = 1; r <= 8; ++r) {
               for(int a = 0; a < 20; ++a) {
                  double ang = (double)a / (double)20.0F * Math.PI * (double)2.0F;
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX + Math.cos(ang) * (double)r, this.posY + (double)0.5F, this.posZ + Math.sin(ang) * (double)r, 1, 0.1, 0.1, 0.1, 0.05, new int[0]);
               }
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 0.8F, 1.2F);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.6F, 1.0F);
         SoundEvent portalSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("block.end_portal.spawn"));
         if (portalSound != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, portalSound, SoundCategory.HOSTILE, 0.5F, 1.5F);
         }

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)8.0F))) {
            if (p.isEntityAlive()) {
               double dx = p.posX - this.posX;
               double dz = p.posZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > 0.1) {
                  p.motionX += dx / len * (double)2.0F;
                  p.motionY += 0.6;
                  p.motionZ += dz / len * (double)2.0F;
                  p.velocityChanged = true;
               }
            }
         }

         if (newPhase == 2 && this.shadowLavaCD <= 0) {
            this.shadowLavaCD = 0;
            this.tryShadowKGForced(1);
         } else if (newPhase == 3 && this.shadowIceCD <= 0) {
            this.shadowIceCD = 0;
            this.tryShadowKGForced(2);
         } else if (newPhase == 4 && this.shadowStormCD <= 0) {
            this.shadowStormCD = 0;
            this.tryShadowKGForced(3);
         }

      }

      private void tryShadowKGForced(int kgType) {
         EntityLivingBase target = this.getAttackTarget();
         if (target != null && target.isEntityAlive()) {
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
            double dz = target.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            if (kgType == 1) {
               EntityLavaGlob.EntityCustom lava = new EntityLavaGlob.EntityCustom(this.world, this, baseDmg * 1.3F, 4.0F);
               lava.setEntityScale(15.0F);
               lava.shoot(dx, dy + dist * 0.01, dz, 1.2F, 1.0F);
               this.world.spawnEntity(lava);
               this.shadowLavaCD = 200;
            } else if (kgType == 2) {
               for(int i = 0; i < 3; ++i) {
                  double ndx = dx + (this.rand.nextDouble() - (double)0.5F) * 0.8;
                  double ndz = dz + (this.rand.nextDouble() - (double)0.5F) * 0.8;
                  EntityIceNeedle.EntityCustom needle = new EntityIceNeedle.EntityCustom(this.world, this, baseDmg * 0.8F, 3.0F);
                  needle.shoot(ndx, dy + dist * 0.01, ndz, 1.6F, 1.5F);
                  this.world.spawnEntity(needle);
               }

               this.shadowIceCD = 220;
            } else if (kgType == 3) {
               EntityStormBolt.EntityCustom sb = new EntityStormBolt.EntityCustom(this.world, this, baseDmg * 1.5F, 5.0F);
               sb.shoot(dx, dy + dist * 0.01, dz, 1.8F, 0.8F);
               this.world.spawnEntity(sb);
               this.shadowStormCD = 240;
            }

            this.shadowGlobalJutsuCD = 8;
            this.spawnKGParticles(kgType);
         }
      }

      private void spawnShadowAura() {
         if (this.world instanceof WorldServer) {
            if (this.shadowPhase == 2) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 3, 0.3, 0.4, 0.3, 0.02, new int[0]);
            } else if (this.shadowPhase == 3) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SNOWBALL, this.posX, this.posY + (double)1.0F, this.posZ, 2, 0.3, 0.4, 0.3, 0.02, new int[0]);
            } else if (this.shadowPhase == 4) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.0F, this.posZ, 4, 0.4, (double)0.5F, 0.4, 0.05, new int[0]);
               if (this.rand.nextFloat() < 0.1F) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.5F, this.posZ, 2, 0.2, 0.2, 0.2, 0.03, new int[0]);
               }
            }

         }
      }

      private boolean isShadowQuestBoss() {
         String cid = this.getNpcConfigId();
         return "the_shadow".equals(cid);
      }

      private boolean isAkatsukiShadowNpc() {
         String cid = this.getNpcConfigId();
         if (cid == null) {
            return false;
         } else {
            return cid.contains("akatsuki") || cid.contains("itachi") || "bingo_kage_3".equals(cid);
         }
      }

      private String getShadowDialogueName() {
         if (this.isShadowQuestBoss()) {
            return "The Shadow";
         } else {
            String name = this.getCustomNameTag();
            return name != null && !name.isEmpty() ? name : "???";
         }
      }

      private void shadowSay(String msg) {
         String prefix = this.getShadowDialogueName();

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)40.0F))) {
            p.sendMessage(new TextComponentString("§8" + prefix + ": " + msg));
         }

         this.shadowDialogueCD = 160;
      }

      private void shadowSayIfReady(String msg) {
         if (this.shadowDialogueCD <= 0) {
            this.shadowSay(msg);
         }

      }

      private void shadowPlayCombatLine() {
         if (this.shadowDialogueCD <= 0 && this.rand.nextInt(100) < 8) {
            String[][] combatLines;
            if (this.isShadowQuestBoss()) {
               combatLines = new String[][]{{"Show me what you've learned!", "Is that how you fight now?", "I remember being faster than that...", "Come on, I know you can do better!", "You're holding back. Don't."}, {"Feel the heat of our potential!", "We could have been unstoppable!", "This is the power you abandoned!", "Remember this feeling?", "Our fire burns eternal!"}, {"FREEZE!", "You can't escape your past!", "I am everything you could have been!", "Feel the cold truth of what you left behind!", "Our bond transcends time itself!"}, {"RAAAAGH!", "I REFUSE TO FADE!", "YOU CAN'T ERASE ME!", "WE ARE ONE AND THE SAME!", "FEEL THE STORM OF OUR EXISTENCE!", "I... WILL... NOT... LOSE!"}};
            } else if (this.isAkatsukiShadowNpc()) {
               combatLines = new String[][]{{"You dare challenge the Akatsuki?", "Your intel is outdated... my jutsu are not.", "These forbidden techniques are beyond your comprehension.", "The Akatsuki doesn't recruit the weak.", "Interesting... but futile."}, {"You're persistent... but you know nothing of true power.", "These scrolls hold secrets villages tried to erase!", "The Akatsuki's research cannot be stopped by the likes of you!", "I've studied jutsu you can't even imagine!", "Every forbidden scroll I've collected has led to THIS!"}, {"These forbidden techniques... I'll show you why they were forbidden!", "You've forced my hand. Witness the fruits of my research!", "The Akatsuki trusted me with these secrets for a reason!", "This is the power nations feared enough to ban!", "No village could contain this knowledge... and neither can you!"}, {"I WILL NOT BE SILENCED!", "THE AKATSUKI'S WILL LIVES THROUGH ME!", "THESE JUTSU WERE SEALED AWAY FOR A REASON... THIS REASON!", "YOU CANNOT DESTROY WHAT I'VE BECOME!", "EVERY FORBIDDEN SCROLL... EVERY SACRIFICE... IT ALL ENDS HERE!", "I... AM... THE AKATSUKI'S LEGACY!"}};
            } else {
               combatLines = new String[][]{{"You think you can take me?", "Is that all you've got?", "I've killed stronger shinobi than you.", "Don't waste my time.", "You should have brought backup."}, {"Now I'm getting serious!", "You're tougher than I expected...", "Time to stop holding back!", "Not bad... but not enough!", "Let me show you real power!"}, {"You'll regret pushing me this far!", "No more games!", "I didn't survive this long by being weak!", "Feel the difference between us!", "This ends NOW!"}, {"RAAAAGH!", "I REFUSE TO DIE HERE!", "I'LL TAKE YOU DOWN WITH ME!", "YOU HAVEN'T SEEN ANYTHING YET!", "EVERYTHING I HAVE... RIGHT HERE, RIGHT NOW!", "I... WON'T... FALL!"}};
            }

            int idx = Math.min(this.shadowPhase - 1, 3);
            String[] lines = combatLines[idx];
            String color = this.shadowPhase >= 4 ? "§4§l" : (this.shadowPhase >= 3 ? "§b" : (this.shadowPhase >= 2 ? "§6" : "§7"));
            this.shadowSay(color + lines[this.rand.nextInt(lines.length)]);
         }
      }

      private String getShadowHitTaunt() {
         if (this.isAkatsukiShadowNpc()) {
            String[] taunts = new String[]{"Predictable!", "Too slow!", "The Akatsuki trained me better!", "Pathetic!", "You'll need more than that!"};
            return "§7" + taunts[this.rand.nextInt(taunts.length)];
         } else if (!this.isShadowQuestBoss()) {
            String[] taunts = new String[]{"Gotcha!", "Too slow!", "Pathetic!", "Saw that coming!", "Nice try!"};
            return "§7" + taunts[this.rand.nextInt(taunts.length)];
         } else {
            String[] taunts = new String[]{"Gotcha!", "Too slow!", "Predictable!", "Read you like a book!", "Nice try!"};
            return "§7" + taunts[this.rand.nextInt(taunts.length)];
         }
      }

      private String getSubTaunt() {
         if (this.isAkatsukiShadowNpc()) {
            String[] taunts = new String[]{"A forbidden substitution technique!", "You'll have to do better than that!", "Behind you!", "The Akatsuki anticipates everything!", "Amateur!"};
            return taunts[this.rand.nextInt(taunts.length)];
         } else if (!this.isShadowQuestBoss()) {
            String[] taunts = new String[]{"You think that would work?", "Substitution!", "Behind you!", "Too predictable!", "Amateur!"};
            return taunts[this.rand.nextInt(taunts.length)];
         } else {
            String[] taunts = new String[]{"You think I'd fall for that?", "Substitution jutsu! Classic!", "Behind you!", "Wrong move!", "Amateur hour!"};
            return taunts[this.rand.nextInt(taunts.length)];
         }
      }

      private void tryShadowMirrorStance() {
         if (this.shadowMirrorCD <= 0 && !this.shadowMirrorStance) {
            if (this.shadowPhase >= 2) {
               if (!(this.rand.nextFloat() > 0.15F + (float)this.shadowPhase * 0.05F)) {
                  this.shadowMirrorStance = true;
                  this.shadowMirrorTicks = 40 + this.rand.nextInt(21);
                  this.shadowMirrorCD = 200;
                  this.shadowSay("§d...");
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.3, (double)0.5F, 0.3, 0.05, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.HOSTILE, 0.6F, 1.2F);
               }
            }
         }
      }

      private void processShadowMirrorStance() {
         if (this.shadowMirrorStance) {
            --this.shadowMirrorTicks;
            if (this.world instanceof WorldServer && this.ticksExisted % 3 == 0) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.0F, this.posZ, 3, 0.2, 0.4, 0.2, 0.03, new int[0]);
            }

            if (this.shadowMirrorTicks <= 0) {
               this.shadowMirrorStance = false;
            }

         }
      }

      private void tryShadowCloneDecoy(EntityLivingBase target) {
         if (this.shadowCloneCD <= 0 && this.shadowPhase >= 3) {
            if (!(this.rand.nextFloat() > 0.12F)) {
               this.shadowCloneCD = 300;
               this.shadowSay("§8§oShadow Clone Jutsu!");
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 30, 0.6, (double)0.5F, 0.6, 0.08, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.8F, 1.0F);
               int cloneCount = 2 + (this.shadowPhase >= 4 ? 1 : 0);
               double[][] positions = new double[cloneCount + 1][2];
               positions[0] = new double[]{this.posX, this.posZ};

               for(int i = 1; i <= cloneCount; ++i) {
                  double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
                  double radius = (double)4.0F + this.rand.nextDouble() * (double)4.0F;
                  positions[i] = new double[]{this.posX + Math.cos(angle) * radius, this.posZ + Math.sin(angle) * radius};
               }

               for(double[] pos : positions) {
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, pos[0], this.posY + (double)1.0F, pos[1], 20, 0.4, (double)0.5F, 0.4, 0.06, new int[0]);
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos[0], this.posY + (double)0.5F, pos[1], 10, 0.3, 0.3, 0.3, 0.04, new int[0]);
                  }
               }

               int teleIdx = 1 + this.rand.nextInt(cloneCount);
               double newX = positions[teleIdx][0];
               double newZ = positions[teleIdx][1];
               double newY = this.findSafeY(newX, this.posY, newZ);
               if (this.isPositionSafe(newX, newY, newZ)) {
                  this.internalReposition = true;
                  this.setPositionAndUpdate(newX, newY, newZ);
                  this.internalReposition = false;
               }

               this.shadowSprinting = true;
               this.shadowSprintTicks = 0;
               this.shadowAggression = Math.min(100, this.shadowAggression + 20);
            }
         }
      }
   }
}
