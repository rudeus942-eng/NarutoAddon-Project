
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcInteractionHelper;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Particles;
import net.narutomod.Particles.Types;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKabuto extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 13;
   public static final int ENTITYID_RANGED = 14;

   public EntityKabuto(ElementsInfTsukAddon instance) {
      super(instance, 34);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kabuto"), 13).name("kabuto").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KabutoRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class KabutoRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/kabuto1.png");

      public KabutoRenderer(RenderManager renderManager) {
         super(renderManager, new ModelPlayer(0.0F, false), 0.5F);
         this.addLayer(new LayerHeldItem(this));
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         String configId = entity.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null && config.getTexture() != null) {
               return config.getTexture();
            }
         }

         return FALLBACK_TEXTURE;
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

         int attackState = entity.getAttackState();
         if (attackState != 2 && attackState != 6) {
            if (attackState == 5) {
               GlStateManager.color(0.7F, 1.0F, 0.7F, 1.0F);
            } else if (attackState == 4) {
               GlStateManager.color(0.9F, 0.9F, 1.0F, 1.0F);
            }
         } else {
            GlStateManager.color(0.7F, 1.0F, 0.9F, 1.0F);
         }

         int phase = entity.getCurrentPhase();
         if (phase == 3) {
            float pulse = 0.85F + 0.15F * MathHelper.sin((float)entity.ticksExisted * 0.1F);
            GlStateManager.color(0.6F * pulse, 0.8F * pulse, 1.0F * pulse, 1.0F);
         }

      }
   }

   public static class EntityCustom extends EntityCreature implements INpcConfigurable {
      private static final DataParameter<String> NPC_CONFIG_ID;
      private static final DataParameter<Integer> ATTACK_STATE;
      private static final DataParameter<Boolean> IS_PASSIVE;
      private static final DataParameter<Integer> PHASE;
      public static final int STATE_IDLE = 0;
      public static final int STATE_MELEE = 1;
      public static final int STATE_CHAKRA_SCALPEL = 2;
      public static final int STATE_POISON_SENBON = 3;
      public static final int STATE_BODY_FLICKER = 4;
      public static final int STATE_HEALING_PALM = 5;
      public static final int STATE_SCALPEL_COMBO = 6;
      public static final int STATE_CHAKRA_FLOW = 7;
      public static final int PHASE_1 = 1;
      public static final int PHASE_2 = 2;
      public static final int PHASE_3 = 3;
      private static final float PHASE_2_THRESHOLD = 0.6F;
      private static final float PHASE_3_THRESHOLD = 0.3F;
      private static final float MELEE_DMG = 12.5F;
      private static final float MELEE_TRUE = 4.0F;
      private static final float SCALPEL_DMG = 7.5F;
      private static final float SCALPEL_TRUE = 16.0F;
      private static final float SENBON_DMG = 10.0F;
      private static final float SENBON_TRUE = 2.0F;
      private static final float FLICKER_DMG = 17.5F;
      private static final float FLICKER_TRUE = 6.0F;
      private static final float COMBO_HIT1_DMG = 10.0F;
      private static final float COMBO_HIT1_TRUE = 3.0F;
      private static final float COMBO_HIT2_DMG = 10.0F;
      private static final float COMBO_HIT2_TRUE = 3.0F;
      private static final float COMBO_HIT3_DMG = 20.0F;
      private static final float COMBO_HIT3_TRUE = 10.0F;
      private static final float FLOW_STRIKE_DMG = 20.0F;
      private static final float FLOW_STRIKE_TRUE = 10.0F;
      private static final double MELEE_RANGE = (double)2.5F;
      private static final double SCALPEL_RANGE = (double)2.0F;
      private static final double SENBON_RANGE = (double)15.0F;
      private static final double FLICKER_MIN_RANGE = (double)8.0F;
      private static final double FLICKER_MAX_RANGE = (double)25.0F;
      private static final double COMBO_RANGE = (double)2.5F;
      private static final double FLOW_STRIKE_RANGE = (double)3.0F;
      private static final int[] CD_MELEE_P1;
      private static final int[] CD_SCALPEL_P1;
      private static final int[] CD_SENBON_P1;
      private static final int[] CD_FLICKER_P1;
      private static final int[] CD_MELEE_P2;
      private static final int[] CD_SCALPEL_P2;
      private static final int[] CD_SENBON_P2;
      private static final int[] CD_FLICKER_P2;
      private static final int[] CD_HEALING_P2;
      private static final int[] CD_COMBO_P2;
      private static final int[] CD_MELEE_P3;
      private static final int[] CD_SCALPEL_P3;
      private static final int[] CD_SENBON_P3;
      private static final int[] CD_FLICKER_P3;
      private static final int[] CD_HEALING_P3;
      private static final int[] CD_COMBO_P3;
      private static final int[] CD_FLOW_P3;
      private static final int SEARCH_RADIUS = 50;
      private static final double BASE_DEF = (double)1.25F;
      private static final double PLAYER_DEF_BONUS = 0.09;
      private static final double PLAYER_DEF_CAP = 0.36;
      private static final double PLAYER_DMG_BONUS = 0.12;
      private static final double PLAYER_DMG_CAP = 0.6;
      private static final int TARGET_SWITCH_COOLDOWN = 100;
      private static final float TARGET_SWITCH_CHANCE = 0.35F;
      private static final UUID PARALYSIS_MODIFIER_UUID;
      private static final UUID HEAVINESS_SPEED_UUID;
      private boolean isPassive = false;
      private int meleeCooldown = 0;
      private int scalpelCooldown = 0;
      private int senbonCooldown = 0;
      private int flickerCooldown = 0;
      private int healingCooldown = 0;
      private int comboCooldown = 0;
      private int flowStrikeCooldown = 0;
      private int attackAnimTimer = 0;
      private int invulnTicks = 0;
      private int healingChannel = 0;
      private int comboStep = 0;
      private int comboTickTimer = 0;
      private EntityLivingBase comboTarget = null;
      private int verticalLeapCooldown = 0;
      private static final int FLICKER_DASH_DURATION = 8;
      private boolean isDashing = false;
      private int dashTicksRemaining = 0;
      private double dashVelocityX = (double)0.0F;
      private double dashVelocityZ = (double)0.0F;
      private EntityLivingBase dashTarget = null;
      private int targetSwitchCooldown = 0;
      private UUID currentTargetUUID = null;
      private int currentPhase = 1;
      private boolean phase2Announced = false;
      private boolean phase3Announced = false;
      private double prevPosX;
      private double prevPosZ;
      private int stuckTicks = 0;
      private int trapEscapeTick = 0;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.6F, 1.8F);
         this.experienceValue = 0;
         this.isImmuneToFire = false;
         this.enablePersistence();
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(NPC_CONFIG_ID, "");
         this.dataManager.register(ATTACK_STATE, 0);
         this.dataManager.register(IS_PASSIVE, false);
         this.dataManager.register(PHASE, 1);
      }

      protected void initEntityAI() {
         this.tasks.addTask(1, new EntityAIKabutoCombat(this));
         this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F));
         this.tasks.addTask(6, new EntityAILookIdle(this));
         this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
         this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      }

      public void setAttackTarget(@Nullable EntityLivingBase target) {
         if (target != null && !(target instanceof EntityPlayer)) {
            String cn = target.getClass().getName().toLowerCase();
            if (!cn.contains("icedome") && !cn.contains("shieldbase")) {
               return;
            }
         }

         super.setAttackTarget(target);
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)25000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.5F);
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)8.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.9);
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)10.0F);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)40.0F);
      }

      private void checkPhaseTransition() {
         float healthPercent = this.getHealth() / this.getMaxHealth();
         if (this.currentPhase == 1 && healthPercent <= 0.6F) {
            this.transitionToPhase(2);
         } else if (this.currentPhase == 2 && healthPercent <= 0.3F) {
            this.transitionToPhase(3);
         }

      }

      private void transitionToPhase(int newPhase) {
         this.currentPhase = newPhase;
         this.dataManager.set(PHASE, newPhase);
         this.comboStep = 0;
         this.comboTarget = null;
         this.healingChannel = 0;
         if (newPhase == 2 && !this.phase2Announced) {
            this.phase2Announced = true;
            this.broadcastMessage("§5§lKabuto: §7\"Hmph... you're not bad. I underestimated you. Time to stop holding back.\"");
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.54);
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)this.height / (double)2.0F, this.posZ, 80, (double)2.0F, (double)1.5F, (double)2.0F, (double)0.0F, 0.15, (double)0.0F, new int[]{'\ufff6' | 32 + this.rand.nextInt(32) << 24, 25, 15, 240, -1, 0});
         }

         if (newPhase == 3 && !this.phase3Announced) {
            this.phase3Announced = true;
            this.broadcastMessage("§9§lKabuto: §7\"You've pushed me this far... Orochimaru-sama's chakra flow technique will finish this!\"");
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.58);
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)this.height / (double)2.0F, this.posZ, 120, (double)3.0F, (double)2.0F, (double)3.0F, (double)0.0F, 0.2, (double)0.0F, new int[]{141218303, 35, 20, 240, -1, 0});
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, new SoundEvent(new ResourceLocation("narutomod:chakraflow")), SoundCategory.HOSTILE, 0.8F, 1.0F);
         }

      }

      private int[] getCooldownRange(int attackType) {
         switch (attackType) {
            case 1:
               switch (this.currentPhase) {
                  case 2:
                     return CD_MELEE_P2;
                  case 3:
                     return CD_MELEE_P3;
                  default:
                     return CD_MELEE_P1;
               }
            case 2:
               switch (this.currentPhase) {
                  case 2:
                     return CD_SCALPEL_P2;
                  case 3:
                     return CD_SCALPEL_P3;
                  default:
                     return CD_SCALPEL_P1;
               }
            case 3:
               switch (this.currentPhase) {
                  case 2:
                     return CD_SENBON_P2;
                  case 3:
                     return CD_SENBON_P3;
                  default:
                     return CD_SENBON_P1;
               }
            case 4:
               switch (this.currentPhase) {
                  case 2:
                     return CD_FLICKER_P2;
                  case 3:
                     return CD_FLICKER_P3;
                  default:
                     return CD_FLICKER_P1;
               }
            case 5:
               switch (this.currentPhase) {
                  case 3:
                     return CD_HEALING_P3;
                  default:
                     return CD_HEALING_P2;
               }
            case 6:
               switch (this.currentPhase) {
                  case 3:
                     return CD_COMBO_P3;
                  default:
                     return CD_COMBO_P2;
               }
            case 7:
               return CD_FLOW_P3;
            default:
               return CD_MELEE_P1;
         }
      }

      private float getPhaseDamageMultiplier() {
         switch (this.currentPhase) {
            case 2:
               return 1.15F;
            case 3:
               return 1.35F;
            default:
               return 1.0F;
         }
      }

      private void broadcastMessage(String msg) {
         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F))) {
            p.sendMessage(new TextComponentString(msg));
         }

      }

      private int countNearbyPlayers() {
         List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F));
         int count = 0;

         for(EntityPlayer p : players) {
            if (p.isEntityAlive() && !p.isSpectator()) {
               ++count;
            }
         }

         return count;
      }

      private double getDamageMultiplier() {
         int players = this.countNearbyPlayers();
         double base = (double)1.0F + Math.min((double)players * 0.12, 0.6);
         return base * (double)this.getPhaseDamageMultiplier();
      }

      private double getDefenseMultiplier() {
         int players = this.countNearbyPlayers();
         return (double)1.25F + Math.min((double)players * 0.09, 0.36);
      }

      private int randomCooldown(int[] range) {
         return range[0] + this.rand.nextInt(range[1] - range[0] + 1);
      }

      private void tryTargetSwitch() {
         if (this.targetSwitchCooldown <= 0) {
            List<EntityPlayer> alivePlayers = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F));
            List<EntityPlayer> validTargets = new ArrayList();

            for(EntityPlayer p : alivePlayers) {
               if (p.isEntityAlive() && !p.isSpectator()) {
                  validTargets.add(p);
               }
            }

            if (validTargets.size() <= 1) {
               this.targetSwitchCooldown = 100;
            } else {
               float switchChance = 0.35F + (float)(validTargets.size() - 1) * 0.1F;
               switchChance = Math.min(switchChance, 0.75F);
               if (this.rand.nextFloat() < switchChance) {
                  EntityPlayer lowestHpTarget = null;
                  float lowestHp = Float.MAX_VALUE;

                  for(EntityPlayer p : validTargets) {
                     if ((this.currentTargetUUID == null || !p.getUniqueID().equals(this.currentTargetUUID)) && p.getHealth() < lowestHp) {
                        lowestHp = p.getHealth();
                        lowestHpTarget = p;
                     }
                  }

                  EntityPlayer newTarget;
                  if (lowestHpTarget != null) {
                     newTarget = lowestHpTarget;
                  } else {
                     newTarget = (EntityPlayer)validTargets.get(this.rand.nextInt(validTargets.size()));
                  }

                  this.setAttackTarget(newTarget);
                  this.currentTargetUUID = newTarget.getUniqueID();
               }

               this.targetSwitchCooldown = 100;
            }
         }
      }

      private void purgeMovementDebuffs() {
         List<Potion> toRemove = new ArrayList();

         for(PotionEffect effect : this.getActivePotionEffects()) {
            Potion potion = effect.getPotion();
            ResourceLocation id = (ResourceLocation)Potion.REGISTRY.getNameForObject(potion);
            if (id != null) {
               String name = id.toString();
               if (name.contains("paralysis") || name.contains("heaviness")) {
                  toRemove.add(potion);
                  continue;
               }
            }

            if (potion == MobEffects.SLOWNESS || potion == MobEffects.MINING_FATIGUE) {
               toRemove.add(potion);
            }
         }

         for(Potion p : toRemove) {
            this.removePotionEffect(p);
         }

         IAttributeInstance speedAttr = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
         speedAttr.removeModifier(PARALYSIS_MODIFIER_UUID);
         speedAttr.removeModifier(HEAVINESS_SPEED_UUID);
         NBTTagCompound entityData = this.getEntityData();
         if (entityData.hasKey("FearEffect")) {
            entityData.removeTag("FearEffect");
         }

         if (entityData.hasKey("tempDisableAI")) {
            entityData.removeTag("tempDisableAI");
         }

         if (entityData.hasKey("paperbindSlownessAmplitude")) {
            entityData.removeTag("paperbindSlownessAmplitude");
         }

         if (entityData.hasKey("kikaichuSlownessAmplitude")) {
            entityData.removeTag("kikaichuSlownessAmplitude");
         }

         if (!this.isPassive && this.isAIDisabled()) {
            this.setNoAI(false);
         }

      }

      public void onLivingUpdate() {
         super.onLivingUpdate();
         if (!this.world.isRemote) {
            if (this.ticksExisted % 10 == 0) {
               EntityLivingBase dome = this.findNearbyIceDome((double)20.0F);
               if (dome != null && dome.isEntityAlive()) {
                  this.setAttackTarget(dome);
                  if ((double)this.getDistance(dome) <= (double)4.0F) {
                     dome.hurtResistantTime = 0;
                     dome.attackEntityFrom(DamageSource.causeMobDamage(this), 200.0F);
                     this.swingArm(EnumHand.MAIN_HAND);
                  }
               }
            }

            if (!this.isPassive) {
               this.purgeMovementDebuffs();
            }

            if (this.meleeCooldown > 0) {
               --this.meleeCooldown;
            }

            if (this.scalpelCooldown > 0) {
               --this.scalpelCooldown;
            }

            if (this.senbonCooldown > 0) {
               --this.senbonCooldown;
            }

            if (this.flickerCooldown > 0) {
               --this.flickerCooldown;
            }

            if (this.healingCooldown > 0) {
               --this.healingCooldown;
            }

            if (this.comboCooldown > 0) {
               --this.comboCooldown;
            }

            if (this.flowStrikeCooldown > 0) {
               --this.flowStrikeCooldown;
            }

            if (this.targetSwitchCooldown > 0) {
               --this.targetSwitchCooldown;
            }

            if (this.verticalLeapCooldown > 0) {
               --this.verticalLeapCooldown;
            }

            if (this.invulnTicks > 0) {
               --this.invulnTicks;
            }

            if (this.attackAnimTimer > 0) {
               --this.attackAnimTimer;
               if (this.attackAnimTimer <= 0 && this.comboStep == 0 && this.healingChannel <= 0) {
                  this.setAttackState(0);
               }
            }

            this.checkPhaseTransition();
            this.processHealingChannel();
            this.processComboTicks();
            if (!this.isPassive && this.getAttackTarget() != null) {
               this.tryTargetSwitch();
            }

            if (this.currentPhase == 3 && !this.isPassive && this.ticksExisted % 20 == 0) {
               this.heal(2.0F);
               if (this.ticksExisted % 40 == 0) {
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 8, 0.3, (double)0.5F, 0.3, (double)0.0F, 0.08, (double)0.0F, new int[]{'\ufff6' | 32 + this.rand.nextInt(32) << 24, 14, 12, 240, -1, 0});
               }
            }

            if (this.currentPhase == 3 && !this.isPassive) {
               double angle = (double)(this.ticksExisted * 15) * Math.PI / (double)180.0F;

               for(int i = 0; i < 3; ++i) {
                  double offsetAngle = angle + (double)i * Math.PI * (double)2.0F / (double)3.0F;
                  double px = this.posX + Math.cos(offsetAngle) * 0.8;
                  double pz = this.posZ + Math.sin(offsetAngle) * 0.8;
                  Particles.spawnParticle(this.world, Types.SMOKE, px, this.posY + (double)1.0F + this.rand.nextDouble() * (double)0.5F, pz, 3, 0.08, 0.08, 0.08, (double)0.0F, 0.04, (double)0.0F, new int[]{141218303, 15, 8, 240});
               }

               if (this.ticksExisted % 2 == 0) {
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, this.posY + 0.9 + this.rand.nextDouble() * 0.3, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, 3, 0.05, 0.05, 0.05, (double)0.0F, 0.02, (double)0.0F, new int[]{141218303, 12, 6, 240});
               }
            }

            if (this.isPassive && (Math.abs(this.motionX) > 0.1 || Math.abs(this.motionZ) > 0.1)) {
               this.motionX = (double)0.0F;
               this.motionZ = (double)0.0F;
               this.velocityChanged = true;
            }

            if (!this.isPassive && !this.isDashing && this.isKnockbackImmune() && (Math.abs(this.motionX) > 0.3 || Math.abs(this.motionZ) > 0.3 || this.motionY > 0.4)) {
               this.motionX *= 0.05;
               this.motionZ *= 0.05;
               if (this.motionY > 0.4) {
                  this.motionY = 0.05;
               }

               this.velocityChanged = true;
            }

            if (!this.isPassive) {
               Entity trappingEntity = this.findNearbyTrapProjectile();
               if (trappingEntity != null) {
                  ++this.trapEscapeTick;
                  if (this.trapEscapeTick >= 2) {
                     this.trapEscapeTick = 0;
                     this.nudgeTowardTarget((double)2.0F);
                  }
               } else {
                  this.trapEscapeTick = 0;
                  double movedX = Math.abs(this.posX - this.prevPosX);
                  double movedZ = Math.abs(this.posZ - this.prevPosZ);
                  if (movedX < 0.005 && movedZ < 0.005 && this.getAttackTarget() != null) {
                     ++this.stuckTicks;
                     if (this.stuckTicks >= 4) {
                        this.nudgeTowardTarget((double)2.0F);
                        this.stuckTicks = 0;
                     }
                  } else {
                     this.stuckTicks = 0;
                  }
               }

               this.prevPosX = this.posX;
               this.prevPosZ = this.posZ;
            }

            if (this.isDashing) {
               this.processDashTick();
            }

            if (!this.isPassive && !this.isDashing && this.onGround && this.verticalLeapCooldown <= 0) {
               EntityLivingBase leapTarget = this.getAttackTarget();
               if (leapTarget != null && leapTarget.isEntityAlive()) {
                  double dy = leapTarget.posY - this.posY;
                  double ldx = leapTarget.posX - this.posX;
                  double ldz = leapTarget.posZ - this.posZ;
                  double horizDist = Math.sqrt(ldx * ldx + ldz * ldz);
                  if (dy > (double)2.0F && horizDist < (double)16.0F) {
                     this.motionX = (double)0.0F;
                     this.motionY = Math.max(0.85, Math.min(1.8, (double)0.5F + dy * 0.11));
                     this.motionZ = (double)0.0F;
                     this.velocityChanged = true;
                     this.verticalLeapCooldown = 35;
                  }
               }
            }

            if (!this.onGround && !this.isDashing && this.verticalLeapCooldown > 0) {
               EntityLivingBase leapTarget = this.getAttackTarget();
               if (leapTarget != null && leapTarget.isEntityAlive()) {
                  double gdx = leapTarget.posX - this.posX;
                  double gdz = leapTarget.posZ - this.posZ;
                  double gDist = Math.sqrt(gdx * gdx + gdz * gdz);
                  if (gDist > (double)0.5F) {
                     this.motionX += gdx / gDist * 0.1;
                     this.motionZ += gdz / gDist * 0.1;
                     this.velocityChanged = true;
                  }
               }
            }
         }

      }

      private boolean isKnockbackImmune() {
         String configId = this.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            NpcConfig config = NpcConfigRegistry.get(configId);
            return config != null && config.isKnockbackImmune();
         } else {
            return false;
         }
      }

      private Entity findNearbyTrapProjectile() {
         for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)15.0F))) {
            String className = e.getClass().getName().toLowerCase();
            if ((className.contains("rasenshuriken") || className.contains("rasengan") || className.contains("rasendama") || className.contains("bijuudama") || className.contains("truthseeker")) && this.getDistanceSq(e) < (double)144.0F) {
               return e;
            }
         }

         return null;
      }

      private boolean isIceDome(EntityLivingBase target) {
         if (target == null) {
            return false;
         } else {
            String cn = target.getClass().getName().toLowerCase();
            return cn.contains("icedome") || cn.contains("shieldbase");
         }
      }

      private EntityLivingBase findNearbyIceDome(double range) {
         List<EntityLivingBase> nearby = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(range), (ex) -> ex != null && ex.isEntityAlive() && this.isIceDome(ex));
         EntityLivingBase nearest = null;
         double nearestDist = Double.MAX_VALUE;

         for(EntityLivingBase e : nearby) {
            double d = this.getDistanceSq(e);
            if (d < nearestDist) {
               nearestDist = d;
               nearest = e;
            }
         }

         return nearest;
      }

      private void nudgeTowardTarget(double distance) {
         EntityLivingBase target = this.getAttackTarget();
         if (target != null) {
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (!(dist < (double)1.0F)) {
               double moveX = this.posX + dx / dist * distance;
               double moveZ = this.posZ + dz / dist * distance;
               double moveY = this.posY;
               BlockPos below = new BlockPos(moveX, moveY - (double)1.0F, moveZ);
               if (this.world.getBlockState(below).getMaterial().isSolid()) {
                  BlockPos foot = new BlockPos(moveX, moveY, moveZ);
                  BlockPos head = new BlockPos(moveX, moveY + (double)1.0F, moveZ);
                  if (!this.world.getBlockState(foot).getMaterial().isSolid()) {
                     if (!this.world.getBlockState(head).getMaterial().isSolid()) {
                        double midX = (this.posX + moveX) / (double)2.0F;
                        double midZ = (this.posZ + moveZ) / (double)2.0F;
                        BlockPos midFoot = new BlockPos(midX, moveY, midZ);
                        BlockPos midHead = new BlockPos(midX, moveY + (double)1.0F, midZ);
                        if (!this.world.getBlockState(midFoot).getMaterial().isSolid()) {
                           if (!this.world.getBlockState(midHead).getMaterial().isSolid()) {
                              this.setPositionAndUpdate(moveX, moveY, moveZ);
                              this.motionX = (double)0.0F;
                              this.motionY = (double)0.0F;
                              this.motionZ = (double)0.0F;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      private double findGroundLevel(double x, double referenceY, double z) {
         int startY = (int)referenceY + 5;
         int minY = Math.max((int)referenceY - 10, 1);

         for(int y = startY; y >= minY; --y) {
            BlockPos ground = new BlockPos(x, (double)(y - 1), z);
            BlockPos footPos = new BlockPos(x, (double)y, z);
            BlockPos headPos = new BlockPos(x, (double)(y + 1), z);
            if (this.world.getBlockState(ground).getMaterial().isSolid() && !this.world.getBlockState(footPos).getMaterial().isSolid() && !this.world.getBlockState(headPos).getMaterial().isSolid()) {
               return (double)y;
            }
         }

         return referenceY;
      }

      private void processHealingChannel() {
         if (this.healingChannel > 0) {
            --this.healingChannel;
            double angle = (double)((15 - this.healingChannel) * 24) * Math.PI / (double)180.0F;
            double px = this.posX + Math.cos(angle) * 0.6;
            double pz = this.posZ + Math.sin(angle) * 0.6;
            Particles.spawnParticle(this.world, Types.SMOKE, px, this.posY + (double)0.5F + (double)(15 - this.healingChannel) * 0.1, pz, 15, 0.15, 0.15, 0.15, (double)0.0F, 0.04, (double)0.0F, new int[]{'\ufff6' | 32 + this.rand.nextInt(32) << 24, 15 + this.rand.nextInt(25), 0, 240, -1, 0});
            if (this.healingChannel <= 0) {
               float healAmount;
               if (this.currentPhase == 3) {
                  healAmount = this.getMaxHealth() * 0.0075F;
               } else {
                  healAmount = this.getMaxHealth() * 0.005F;
               }

               this.heal(healAmount);
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)this.height / (double)2.0F, this.posZ, 50, 0.8, 1.2, 0.8, (double)0.0F, 0.15, (double)0.0F, new int[]{'\ufff6' | 48 + this.rand.nextInt(16) << 24, 30, 15, 240, -1, 0});
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, new SoundEvent(new ResourceLocation("narutomod:windecho")), SoundCategory.HOSTILE, 0.5F, 1.5F);
               this.setAttackState(0);
            }

         }
      }

      private void processComboTicks() {
         if (this.comboStep != 0 && this.comboTarget != null) {
            if (!this.comboTarget.isEntityAlive()) {
               this.comboStep = 0;
               this.comboTarget = null;
            } else {
               --this.comboTickTimer;
               if (this.comboTickTimer <= 0) {
                  double dist = (double)this.getDistance(this.comboTarget);
                  if (dist > (double)5.0F) {
                     this.comboStep = 0;
                     this.comboTarget = null;
                     this.setAttackState(0);
                  } else {
                     double dmgMult = this.getDamageMultiplier();
                     if (this.comboStep == 2) {
                        float normDmg = (float)((double)10.0F * dmgMult);
                        float trueDmg = (float)((double)3.0F * dmgMult);
                        this.comboTarget.hurtResistantTime = 0;
                        this.comboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                        this.comboTarget.hurtResistantTime = 0;
                        this.comboTarget.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                        this.swingArm(EnumHand.OFF_HAND);
                        if (this.world instanceof WorldServer) {
                           ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.comboTarget.posX, this.comboTarget.posY + (double)1.0F, this.comboTarget.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                        }

                        this.world.playSound((EntityPlayer)null, this.comboTarget.posX, this.comboTarget.posY, this.comboTarget.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.8F, 1.1F);
                        this.comboStep = 3;
                        this.comboTickTimer = 8;
                     } else if (this.comboStep == 3) {
                        float normDmg = (float)((double)20.0F * dmgMult);
                        float trueDmg = (float)((double)10.0F * dmgMult);
                        this.comboTarget.hurtResistantTime = 0;
                        this.comboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                        this.comboTarget.hurtResistantTime = 0;
                        this.comboTarget.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                        double kdx = this.comboTarget.posX - this.posX;
                        double kdz = this.comboTarget.posZ - this.posZ;
                        double kd = Math.sqrt(kdx * kdx + kdz * kdz);
                        if (kd > (double)0.0F) {
                           this.comboTarget.motionX = kdx / kd * (double)1.5F;
                           this.comboTarget.motionY = 0.3;
                           this.comboTarget.motionZ = kdz / kd * (double)1.5F;
                        }

                        if (this.comboTarget instanceof EntityPlayerMP) {
                           ((EntityPlayerMP)this.comboTarget).velocityChanged = true;
                        }

                        this.swingArm(EnumHand.MAIN_HAND);
                        if (this.world instanceof WorldServer) {
                           ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.comboTarget.posX, this.comboTarget.posY + (double)1.0F, this.comboTarget.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                        }

                        Particles.spawnParticle(this.world, Types.SMOKE, this.comboTarget.posX, this.comboTarget.posY + (double)this.comboTarget.height / (double)2.0F, this.comboTarget.posZ, 35, (double)0.5F, 0.6, (double)0.5F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{'\ufff6' | 32 + this.rand.nextInt(32) << 24, 20, 10, 240, -1, 0});
                        this.world.playSound((EntityPlayer)null, this.comboTarget.posX, this.comboTarget.posY, this.comboTarget.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.2F, 0.9F);
                        this.comboStep = 0;
                        this.comboTarget = null;
                     }

                  }
               }
            }
         }
      }

      public boolean processInteract(EntityPlayer player, EnumHand hand) {
         if (hand != EnumHand.MAIN_HAND) {
            return super.processInteract(player, hand);
         } else if (this.world.isRemote) {
            return true;
         } else {
            return !(player instanceof EntityPlayerMP) ? false : NpcInteractionHelper.handleNpcInteraction(this, (EntityPlayerMP)player);
         }
      }

      public void basicMelee(EntityLivingBase target) {
         if (this.meleeCooldown <= 0) {
            this.meleeCooldown = this.randomCooldown(this.getCooldownRange(1));
            this.setAttackState(1);
            this.attackAnimTimer = 12;
            double dmgMult = this.getDamageMultiplier();
            float normDmg = (float)((double)12.5F * dmgMult);
            float trueDmg = (float)((double)4.0F * dmgMult);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            this.swingArm(EnumHand.MAIN_HAND);
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 8, 0.3, 0.3, 0.3, 0.2, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.8F, 1.0F);
         }
      }

      public void chakraScalpel(EntityLivingBase target) {
         if (this.scalpelCooldown <= 0) {
            this.scalpelCooldown = this.randomCooldown(this.getCooldownRange(2));
            this.setAttackState(2);
            this.attackAnimTimer = 15;
            double dmgMult = this.getDamageMultiplier();
            float normDmg = (float)((double)7.5F * dmgMult);
            float trueDmg = (float)((double)16.0F * dmgMult);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 0));
            this.swingArm(EnumHand.MAIN_HAND);
            Particles.spawnParticle(this.world, Types.SMOKE, target.posX, target.posY + (double)target.height / (double)2.0F, target.posZ, 25, 0.4, (double)0.5F, 0.4, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{'\ufff6' | 32 + this.rand.nextInt(32) << 24, 16, 10, 240, -1, 0});
            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.3F);
         }
      }

      public void poisonSenbon(EntityLivingBase target) {
         if (this.senbonCooldown <= 0) {
            this.senbonCooldown = this.randomCooldown(this.getCooldownRange(3));
            this.setAttackState(3);
            this.attackAnimTimer = 15;
            if (this.canEntityBeSeen(target)) {
               double dmgMult = this.getDamageMultiplier();
               float normDmg = (float)((double)10.0F * dmgMult);
               float trueDmg = (float)((double)2.0F * dmgMult);
               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
               target.addPotionEffect(new PotionEffect(MobEffects.POISON, 80, 1));
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  double dx = target.posX - this.posX;
                  double dy = target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
                  double dz = target.posZ - this.posZ;

                  for(int i = 0; i < 10; ++i) {
                     double t = (double)i / (double)10.0F;
                     ws.spawnParticle(EnumParticleTypes.CRIT, this.posX + dx * t, this.posY + (double)this.getEyeHeight() + dy * t, this.posZ + dz * t, 1, 0.02, 0.02, 0.02, (double)0.0F, new int[0]);
                  }
               }

               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.HOSTILE, 0.8F, 1.2F);
            }
         }
      }

      public void bodyFlicker(EntityLivingBase target) {
         if (this.flickerCooldown <= 0 && !this.isDashing) {
            this.flickerCooldown = this.randomCooldown(this.getCooldownRange(4));
            this.setAttackState(4);
            this.attackAnimTimer = 13;
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            double destX;
            double destZ;
            if (d > (double)0.0F) {
               destX = target.posX - dx / d * (double)2.0F;
               destZ = target.posZ - dz / d * (double)2.0F;
            } else {
               destX = target.posX + (double)2.0F;
               destZ = target.posZ;
            }

            this.dashVelocityX = (destX - this.posX) / (double)8.0F * 1.4;
            this.dashVelocityZ = (destZ - this.posZ) / (double)8.0F * 1.4;
            this.isDashing = true;
            this.dashTicksRemaining = 8;
            this.dashTarget = target;
            this.motionY = 0.35;

            for(int i = 0; i < 15; ++i) {
               this.world.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, this.posY + this.rand.nextDouble() * (double)2.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, (double)0.0F, 0.05, (double)0.0F, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.8F, 1.5F);
         }
      }

      private void processDashTick() {
         if (this.isDashing) {
            --this.dashTicksRemaining;
            this.motionX = this.dashVelocityX;
            this.motionZ = this.dashVelocityZ;
            if (this.dashTicksRemaining > 4) {
               this.motionY = 0.05;
            } else {
               this.motionY = Math.max(-0.6, this.motionY - 0.1);
            }

            this.velocityChanged = true;

            for(int i = 0; i < 4; ++i) {
               this.world.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.8, this.posY + this.rand.nextDouble() * (double)1.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.8, (double)0.0F, 0.02, (double)0.0F, new int[0]);
            }

            if (this.dashTicksRemaining <= 0) {
               this.isDashing = false;
               this.dashVelocityX = (double)0.0F;
               this.dashVelocityZ = (double)0.0F;

               for(int i = 0; i < 15; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, this.posY + this.rand.nextDouble() * (double)2.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, (double)0.0F, 0.05, (double)0.0F, new int[0]);
               }

               if (this.dashTarget != null && this.dashTarget.isEntityAlive() && (double)this.getDistance(this.dashTarget) <= (double)3.5F) {
                  double dmgMult = this.getDamageMultiplier();
                  float normDmg = (float)((double)17.5F * dmgMult);
                  float trueDmg = (float)((double)6.0F * dmgMult);
                  this.dashTarget.hurtResistantTime = 0;
                  this.dashTarget.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                  this.dashTarget.hurtResistantTime = 0;
                  this.dashTarget.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  this.swingArm(EnumHand.MAIN_HAND);
               }

               this.invulnTicks = 5;
               this.dashTarget = null;
            }

         }
      }

      public void healingPalm() {
         if (this.healingCooldown <= 0) {
            if (this.currentPhase >= 2) {
               this.healingCooldown = this.randomCooldown(this.getCooldownRange(5));
               this.setAttackState(5);
               this.attackAnimTimer = 20;
               this.healingChannel = 15;
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, new SoundEvent(new ResourceLocation("narutomod:windecho")), SoundCategory.HOSTILE, 0.5F, (float)(Math.sin((double)this.ticksExisted * 0.1) * 0.8 + (double)1.5F));
            }
         }
      }

      public void scalpelCombo(EntityLivingBase target) {
         if (this.comboCooldown <= 0) {
            if (this.currentPhase >= 2) {
               this.comboCooldown = this.randomCooldown(this.getCooldownRange(6));
               this.setAttackState(6);
               this.attackAnimTimer = 30;
               double dmgMult = this.getDamageMultiplier();
               float normDmg = (float)((double)10.0F * dmgMult);
               float trueDmg = (float)((double)3.0F * dmgMult);
               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
               this.swingArm(EnumHand.MAIN_HAND);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, target.posX, target.posY + (double)1.0F, target.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 0.8F, 1.0F);
               this.comboStep = 2;
               this.comboTickTimer = 8;
               this.comboTarget = target;
            }
         }
      }

      public void chakraFlowStrike(EntityLivingBase target) {
         if (this.flowStrikeCooldown <= 0) {
            if (this.currentPhase >= 3) {
               this.flowStrikeCooldown = this.randomCooldown(this.getCooldownRange(7));
               this.setAttackState(7);
               this.attackAnimTimer = 15;
               double dmgMult = this.getDamageMultiplier();
               float normDmg = (float)((double)20.0F * dmgMult);
               float trueDmg = (float)((double)10.0F * dmgMult);
               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
               this.swingArm(EnumHand.MAIN_HAND);
               double dx = target.posX - this.posX;
               double dz = target.posZ - this.posZ;
               double d = Math.sqrt(dx * dx + dz * dz);
               if (d > (double)0.0F) {
                  target.motionX = dx / d * 0.8;
                  target.motionY = 0.3;
                  target.motionZ = dz / d * 0.8;
               }

               if (target instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)target).velocityChanged = true;
               }

               double tdx = target.posX - this.posX;
               double tdy = target.posY + (double)1.0F - (this.posY + (double)1.0F);
               double tdz = target.posZ - this.posZ;

               for(int i = 0; i < 15; ++i) {
                  double t = (double)i / (double)15.0F;
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX + tdx * t, this.posY + (double)1.0F + tdy * t, this.posZ + tdz * t, 3, 0.05, 0.05, 0.05, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{141218303, 16, 8, 240});
               }

               Particles.spawnParticle(this.world, Types.SMOKE, target.posX, target.posY + (double)target.height / (double)2.0F, target.posZ, 40, 0.6, 0.6, 0.6, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{141218303, 28, 12, 240, -1, 0});
               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, new SoundEvent(new ResourceLocation("narutomod:chakraflow")), SoundCategory.HOSTILE, 1.0F, 0.8F);
            }
         }
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         if (this.isPassive) {
            return false;
         } else if (this.invulnTicks > 0) {
            return false;
         } else {
            double defMult = this.getDefenseMultiplier();
            amount = (float)((double)amount / defMult);
            if (this.healingChannel > 0) {
               float partialHeal;
               if (this.currentPhase == 3) {
                  partialHeal = this.getMaxHealth() * 0.00375F;
               } else {
                  partialHeal = this.getMaxHealth() * 0.0025F;
               }

               this.heal(partialHeal);
               this.healingChannel = 0;
               this.setAttackState(0);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, this.posX, this.posY + (double)2.0F, this.posZ, 5, 0.3, 0.3, 0.3, 0.1, new int[0]);
               }
            }

            return super.attackEntityFrom(source, amount);
         }
      }

      public boolean isPotionApplicable(PotionEffect effect) {
         if (this.isPassive) {
            return false;
         } else if (effect.getPotion() == MobEffects.SLOWNESS) {
            return false;
         } else if (effect.getPotion() == MobEffects.BLINDNESS) {
            return false;
         } else if (effect.getPotion() == MobEffects.NAUSEA) {
            return false;
         } else if (effect.getPotion() == MobEffects.MINING_FATIGUE) {
            return false;
         } else if (effect.getPotion() == MobEffects.WEAKNESS) {
            return false;
         } else if (effect.getPotion() == MobEffects.LEVITATION) {
            return false;
         } else {
            ResourceLocation potionId = (ResourceLocation)Potion.REGISTRY.getNameForObject(effect.getPotion());
            if (potionId != null && potionId.getNamespace().equals("narutomod")) {
               String name = potionId.getPath();
               if (name.contains("paralysis") || name.contains("heaviness") || name.contains("stun") || name.contains("bind") || name.contains("fear") || name.contains("freeze")) {
                  return false;
               }
            }

            return super.isPotionApplicable(effect);
         }
      }

      public int getAttackState() {
         return (Integer)this.dataManager.get(ATTACK_STATE);
      }

      public void setAttackState(int state) {
         this.dataManager.set(ATTACK_STATE, state);
      }

      public int getCurrentPhase() {
         return (Integer)this.dataManager.get(PHASE);
      }

      public boolean getIsPassive() {
         return (Boolean)this.dataManager.get(IS_PASSIVE);
      }

      public EnumCreatureAttribute getCreatureAttribute() {
         return EnumCreatureAttribute.UNDEFINED;
      }

      protected boolean canDespawn() {
         return false;
      }

      protected Item getDropItem() {
         return null;
      }

      public SoundEvent getAmbientSound() {
         return null;
      }

      public SoundEvent getHurtSound(DamageSource ds) {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.hurt"));
      }

      public SoundEvent getDeathSound() {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.death"));
      }

      protected float getSoundVolume() {
         return 1.0F;
      }

      public void applyNpcConfig(NpcConfig config) {
         this.dataManager.set(NPC_CONFIG_ID, config.getConfigId());
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(config.getMaxHealth());
         this.setHealth((float)config.getMaxHealth());
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(config.getMovementSpeed());
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(config.getArmor());
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(config.getAttackDamage());
         this.isPassive = config.isPassive();
         this.dataManager.set(IS_PASSIVE, config.isPassive());
         if (config.isPassive()) {
            this.setNoAI(true);
         } else {
            this.setNoAI(false);
         }

         this.setCustomNameTag(config.getDisplayName());
         this.setAlwaysRenderNameTag(true);
      }

      public String getNpcConfigId() {
         return (String)this.dataManager.get(NPC_CONFIG_ID);
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         String configId = this.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            compound.setString("npcConfigId", configId);
         }

         compound.setBoolean("isPassive", this.isPassive);
         compound.setInteger("currentPhase", this.currentPhase);
         compound.setBoolean("phase2Announced", this.phase2Announced);
         compound.setBoolean("phase3Announced", this.phase3Announced);
         compound.setInteger("meleeCooldown", this.meleeCooldown);
         compound.setInteger("scalpelCooldown", this.scalpelCooldown);
         compound.setInteger("senbonCooldown", this.senbonCooldown);
         compound.setInteger("flickerCooldown", this.flickerCooldown);
         compound.setInteger("healingCooldown", this.healingCooldown);
         compound.setInteger("comboCooldown", this.comboCooldown);
         compound.setInteger("flowStrikeCooldown", this.flowStrikeCooldown);
         compound.setInteger("comboStep", this.comboStep);
         compound.setInteger("healingChannel", this.healingChannel);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.isPassive = compound.getBoolean("isPassive");
         this.dataManager.set(IS_PASSIVE, this.isPassive);
         this.currentPhase = compound.getInteger("currentPhase");
         if (this.currentPhase < 1) {
            this.currentPhase = 1;
         }

         this.dataManager.set(PHASE, this.currentPhase);
         this.phase2Announced = compound.getBoolean("phase2Announced");
         this.phase3Announced = compound.getBoolean("phase3Announced");
         String configId = compound.getString("npcConfigId");
         if (configId != null && !configId.isEmpty()) {
            this.dataManager.set(NPC_CONFIG_ID, configId);
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null) {
               this.applyNpcConfig(config);
            }
         }

         this.meleeCooldown = compound.getInteger("meleeCooldown");
         this.scalpelCooldown = compound.getInteger("scalpelCooldown");
         this.senbonCooldown = compound.getInteger("senbonCooldown");
         this.flickerCooldown = compound.getInteger("flickerCooldown");
         this.healingCooldown = compound.getInteger("healingCooldown");
         this.comboCooldown = compound.getInteger("comboCooldown");
         this.flowStrikeCooldown = compound.getInteger("flowStrikeCooldown");
         this.comboStep = compound.getInteger("comboStep");
         this.healingChannel = compound.getInteger("healingChannel");
      }

      static {
         NPC_CONFIG_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
         ATTACK_STATE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         IS_PASSIVE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
         PHASE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         CD_MELEE_P1 = new int[]{15, 25};
         CD_SCALPEL_P1 = new int[]{60, 90};
         CD_SENBON_P1 = new int[]{80, 120};
         CD_FLICKER_P1 = new int[]{90, 130};
         CD_MELEE_P2 = new int[]{10, 18};
         CD_SCALPEL_P2 = new int[]{40, 65};
         CD_SENBON_P2 = new int[]{55, 85};
         CD_FLICKER_P2 = new int[]{60, 90};
         CD_HEALING_P2 = new int[]{160, 200};
         CD_COMBO_P2 = new int[]{70, 100};
         CD_MELEE_P3 = new int[]{8, 14};
         CD_SCALPEL_P3 = new int[]{30, 50};
         CD_SENBON_P3 = new int[]{40, 65};
         CD_FLICKER_P3 = new int[]{40, 65};
         CD_HEALING_P3 = new int[]{200, 260};
         CD_COMBO_P3 = new int[]{45, 70};
         CD_FLOW_P3 = new int[]{35, 55};
         PARALYSIS_MODIFIER_UUID = UUID.fromString("c69af92a-b96d-49b7-a396-9b3b0d77edd5");
         HEAVINESS_SPEED_UUID = UUID.fromString("7d735ff6-8872-482d-ac1f-cd2249e8f584");
      }
   }

   public static class EntityAIKabutoCombat extends EntityAIBase {
      private final EntityCustom kabuto;
      private EntityLivingBase target;

      public EntityAIKabutoCombat(EntityCustom kabuto) {
         this.kabuto = kabuto;
         this.setMutexBits(3);
      }

      public boolean shouldExecute() {
         EntityLivingBase t = this.kabuto.getAttackTarget();
         if (t != null && t.isEntityAlive()) {
            this.target = t;
            return true;
         } else {
            return false;
         }
      }

      public boolean shouldContinueExecuting() {
         return this.target != null && this.target.isEntityAlive() && this.kabuto.isEntityAlive();
      }

      public void resetTask() {
         this.target = null;
         this.kabuto.setAttackState(0);
         this.kabuto.comboStep = 0;
         this.kabuto.comboTarget = null;
         this.kabuto.healingChannel = 0;
      }

      public void updateTask() {
         if (this.target != null && this.target.isEntityAlive()) {
            if (this.kabuto.getAttackTarget() != null && this.kabuto.getAttackTarget() != this.target) {
               this.target = this.kabuto.getAttackTarget();
            }

            double dist = (double)this.kabuto.getDistance(this.target);
            this.kabuto.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
            if (this.kabuto.isDashing) {
               this.kabuto.getNavigator().clearPath();
            } else if (this.kabuto.comboStep > 0) {
               this.kabuto.getNavigator().clearPath();
            } else if (this.kabuto.healingChannel > 0) {
               this.kabuto.getNavigator().clearPath();
            } else {
               this.handleMovement(dist);
               switch (this.kabuto.currentPhase) {
                  case 1:
                     this.executePhase1(dist);
                     break;
                  case 2:
                     this.executePhase2(dist);
                     break;
                  case 3:
                     this.executePhase3(dist);
               }

            }
         }
      }

      private void handleMovement(double dist) {
         if (dist > (double)15.0F) {
            this.kabuto.getNavigator().tryMoveToEntityLiving(this.target, 1.3);
         } else if (dist > (double)5.0F) {
            this.kabuto.getNavigator().tryMoveToEntityLiving(this.target, (double)1.0F);
         } else if (dist > (double)2.0F) {
            this.kabuto.getNavigator().tryMoveToEntityLiving(this.target, 0.6);
         } else {
            this.kabuto.getNavigator().clearPath();
         }

      }

      private void executePhase1(double dist) {
         if (this.kabuto.flickerCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.kabuto.bodyFlicker(this.target);
         } else if (this.kabuto.senbonCooldown <= 0 && dist >= (double)5.0F && dist <= (double)15.0F) {
            this.kabuto.poisonSenbon(this.target);
         } else if (this.kabuto.scalpelCooldown <= 0 && dist <= (double)2.0F) {
            this.kabuto.chakraScalpel(this.target);
         } else {
            if (this.kabuto.meleeCooldown <= 0 && dist <= (double)2.5F) {
               this.kabuto.basicMelee(this.target);
            }

         }
      }

      private void executePhase2(double dist) {
         float hpPercent = this.kabuto.getHealth() / this.kabuto.getMaxHealth();
         if (this.kabuto.healingCooldown <= 0 && hpPercent < 0.54F) {
            this.kabuto.healingPalm();
         } else if (this.kabuto.flickerCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.kabuto.bodyFlicker(this.target);
         } else if (this.kabuto.senbonCooldown <= 0 && dist >= (double)5.0F && dist <= (double)15.0F) {
            this.kabuto.poisonSenbon(this.target);
         } else if (this.kabuto.comboCooldown <= 0 && dist <= (double)2.5F) {
            this.kabuto.scalpelCombo(this.target);
         } else if (this.kabuto.scalpelCooldown <= 0 && dist <= (double)2.0F) {
            this.kabuto.chakraScalpel(this.target);
         } else {
            if (this.kabuto.meleeCooldown <= 0 && dist <= (double)2.5F) {
               this.kabuto.basicMelee(this.target);
            }

         }
      }

      private void executePhase3(double dist) {
         float hpPercent = this.kabuto.getHealth() / this.kabuto.getMaxHealth();
         if (this.kabuto.healingCooldown <= 0 && hpPercent < 0.27F) {
            this.kabuto.healingPalm();
         } else if (this.kabuto.flowStrikeCooldown <= 0 && dist <= (double)3.0F) {
            this.kabuto.chakraFlowStrike(this.target);
         } else if (this.kabuto.flickerCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.kabuto.bodyFlicker(this.target);
         } else if (this.kabuto.senbonCooldown <= 0 && dist >= (double)5.0F && dist <= (double)15.0F) {
            this.kabuto.poisonSenbon(this.target);
         } else if (this.kabuto.comboCooldown <= 0 && dist <= (double)2.5F) {
            this.kabuto.scalpelCombo(this.target);
         } else if (this.kabuto.scalpelCooldown <= 0 && dist <= (double)2.0F) {
            this.kabuto.chakraScalpel(this.target);
         } else {
            if (this.kabuto.meleeCooldown <= 0 && dist <= (double)2.5F) {
               this.kabuto.basicMelee(this.target);
            }

         }
      }
   }
}
