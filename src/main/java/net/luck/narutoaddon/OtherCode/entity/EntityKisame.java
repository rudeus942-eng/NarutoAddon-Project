
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
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
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKisame extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 204;
   public static final int ENTITYID_RANGED = 205;

   public EntityKisame(ElementsInfTsukAddon instance) {
      super(instance, 33);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kisame"), 204).name("inftsuk_kisame").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KisameRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class KisameRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/kisame1.png");

      public KisameRenderer(RenderManager renderManager) {
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
         if (attackState == 4) {
            GlStateManager.color(0.7F, 0.8F, 1.0F, 1.0F);
         } else if (attackState == 5) {
            GlStateManager.color(0.8F, 0.7F, 1.0F, 1.0F);
         }

      }
   }

   public static class EntityCustom extends EntityCreature implements INpcConfigurable {
      private static final DataParameter<String> NPC_CONFIG_ID;
      private static final DataParameter<Integer> ATTACK_STATE;
      private static final DataParameter<Boolean> IS_RETREATING;
      private static final DataParameter<Integer> PHASE;
      public static final int STATE_IDLE = 0;
      public static final int STATE_MELEE = 1;
      public static final int STATE_WATER_SHARK = 2;
      public static final int STATE_WATER_CLONE = 3;
      public static final int STATE_WATER_PRISON = 4;
      public static final int STATE_CHAKRA_DRAIN = 5;
      public static final int STATE_RUSH = 6;
      public static final int STATE_COMBO = 7;
      public static final int PHASE_1 = 1;
      public static final int PHASE_2 = 2;
      public static final int PHASE_3 = 3;
      private static final float PHASE_2_THRESHOLD = 0.6F;
      private static final float PHASE_3_THRESHOLD = 0.3F;
      private static final int SEARCH_RADIUS = 50;
      private static final double BASE_DEF = (double)1.25F;
      private static final double PLAYER_DEF_BONUS = 0.09;
      private static final double PLAYER_DEF_CAP = 0.36;
      private static final double PLAYER_DMG_BONUS = 0.12;
      private static final double PLAYER_DMG_CAP = 0.6;
      private static final float MELEE_DMG = 10.0F;
      private static final float MELEE_TRUE = 3.0F;
      private static final float COMBO_LIGHT_DMG = 8.8F;
      private static final float COMBO_LIGHT_TRUE = 3.0F;
      private static final float COMBO_HEAVY_DMG = 17.5F;
      private static final float COMBO_HEAVY_TRUE = 5.0F;
      private static final float SHARK_DMG = 15.0F;
      private static final float SHARK_TRUE = 5.0F;
      private static final float PRISON_DMG = 7.5F;
      private static final float PRISON_TRUE = 3.0F;
      private static final float DRAIN_DMG = 11.3F;
      private static final float DRAIN_TRUE = 4.0F;
      private static final int[] CD_WATER_SHARK_P1;
      private static final int[] CD_WATER_CLONE_P1;
      private static final int[] CD_WATER_PRISON_P1;
      private static final int[] CD_CHAKRA_DRAIN_P1;
      private static final int[] CD_RUSH_P1;
      private static final int[] CD_COMBO_P1;
      private static final int[] CD_WATER_SHARK_P2;
      private static final int[] CD_WATER_CLONE_P2;
      private static final int[] CD_WATER_PRISON_P2;
      private static final int[] CD_CHAKRA_DRAIN_P2;
      private static final int[] CD_RUSH_P2;
      private static final int[] CD_COMBO_P2;
      private static final int[] CD_WATER_SHARK_P3;
      private static final int[] CD_WATER_CLONE_P3;
      private static final int[] CD_WATER_PRISON_P3;
      private static final int[] CD_CHAKRA_DRAIN_P3;
      private static final int[] CD_RUSH_P3;
      private static final int[] CD_COMBO_P3;
      private static final double[] R_SHARK;
      private static final double[] R_CLONE;
      private static final double[] R_PRISON;
      private static final double[] R_DRAIN;
      private static final double[] R_MELEE;
      private static final double[] R_COMBO;
      private static final float RUSH_MIN_RANGE = 6.0F;
      private static final float RUSH_MAX_RANGE = 20.0F;
      private boolean isRushing = false;
      private int rushTicksRemaining = 0;
      private static final int RUSH_DURATION_TICKS = 8;
      private double rushVelocityX = (double)0.0F;
      private double rushVelocityZ = (double)0.0F;
      private static final int WATER_PRISON_DURATION = 60;
      private static final int COMBO_MAX_HITS = 4;
      private static final int[] COMBO_DELAYS;
      private static final int TARGET_SWITCH_COOLDOWN = 100;
      private static final float TARGET_SWITCH_CHANCE = 0.35F;
      private boolean isPassive = false;
      private UUID itachiUUID;
      private int waterSharkCooldown = 0;
      private int waterCloneCooldown = 0;
      private int waterPrisonCooldown = 0;
      private int chakraDrainCooldown = 0;
      private int rushCooldown = 0;
      private int comboCooldown = 0;
      private int attackAnimTimer = 0;
      private int comboHitCount = 0;
      private int comboTimer = 0;
      private boolean inActiveCombo = false;
      private Entity activeComboTarget = null;
      private boolean waterPrisonActive = false;
      private int waterPrisonTimer = 0;
      private UUID waterPrisonTargetUUID;
      private int targetSwitchCooldown = 0;
      private UUID currentTargetUUID = null;
      private int currentPhase = 1;
      private boolean phase2Announced = false;
      private boolean phase3Announced = false;
      private int regenTimer = 0;
      private double prevPosX;
      private double prevPosZ;
      private int stuckTicks = 0;
      private int trapEscapeTick = 0;
      private int coordComboId = 0;
      private int coordComboTimer = 0;
      private int coordComboDelay = 0;
      private UUID coordComboTargetUUID = null;
      private int coordComboCooldown = 0;
      public static final int STATE_LEAP_SLAM = 8;
      private static final int LEAP_SLAM_DURATION = 12;
      private static final float LEAP_SLAM_NORMAL_DMG = 22.5F;
      private static final float LEAP_SLAM_TRUE_DMG = 8.0F;
      private static final float LEAP_SLAM_AOE_RADIUS = 3.5F;
      private static final float LEAP_SLAM_MIN_RANGE = 5.0F;
      private static final float LEAP_SLAM_MAX_RANGE = 16.0F;
      private static final int[] CD_LEAP_SLAM_P1;
      private static final int[] CD_LEAP_SLAM_P2;
      private static final int[] CD_LEAP_SLAM_P3;
      private boolean isLeaping = false;
      private int leapTicksRemaining = 0;
      private double leapVelocityX = (double)0.0F;
      private double leapVelocityZ = (double)0.0F;
      private int leapSlamCooldown = 0;
      private int verticalLeapCooldown = 0;
      private static final UUID PARALYSIS_MODIFIER_UUID;
      private static final UUID HEAVINESS_SPEED_UUID;
      private static final UUID HEAVINESS_ATKSPD_UUID;

      public void requestCoordCombo(int comboId, UUID targetUUID) {
         if (this.coordComboCooldown <= 0 && !this.isPassive && !this.isRetreating()) {
            this.coordComboId = comboId;
            this.coordComboTimer = 60;
            this.coordComboDelay = 10 + this.rand.nextInt(21);
            this.coordComboTargetUUID = targetUUID;
         }
      }

      public boolean hasCoordCombo() {
         return this.coordComboId != 0 && this.coordComboTimer > 0;
      }

      public void cancelCoordCombo() {
         this.coordComboId = 0;
         this.coordComboTimer = 0;
         this.coordComboDelay = 0;
         this.coordComboTargetUUID = null;
      }

      public int getCoordGlobalCooldown() {
         switch (this.currentPhase) {
            case 2:
               return 150;
            case 3:
               return 100;
            default:
               return 200;
         }
      }

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
         this.dataManager.register(IS_RETREATING, false);
         this.dataManager.register(PHASE, 1);
      }

      protected void initEntityAI() {
         this.tasks.addTask(1, new EntityAIKisameCombat(this));
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
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)15000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35);
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)10.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.9);
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)8.0F);
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
         this.inActiveCombo = false;
         this.activeComboTarget = null;
         this.comboHitCount = 0;
         if (newPhase == 2 && !this.phase2Announced) {
            this.phase2Announced = true;
            this.broadcastMessage("§9§lKisame: §7\"Time to get serious... Water Style!\"");
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.38);

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)6.0F))) {
               if (p.isEntityAlive() && !p.isSpectator()) {
                  double dx = p.posX - this.posX;
                  double dz = p.posZ - this.posZ;
                  double d = Math.sqrt(dx * dx + dz * dz);
                  if (d > (double)0.0F) {
                     p.motionX += dx / d * 0.8;
                     p.motionY += 0.3;
                     p.motionZ += dz / d * 0.8;
                     if (p instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)p).velocityChanged = true;
                     }
                  }
               }
            }

            for(int i = 0; i < 50; ++i) {
               this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F, this.posY + this.rand.nextDouble() * (double)3.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F, (this.rand.nextDouble() - (double)0.5F) * 0.3, this.rand.nextDouble() * 0.3, (this.rand.nextDouble() - (double)0.5F) * 0.3, new int[0]);
            }
         }

         if (newPhase == 3 && !this.phase3Announced) {
            this.phase3Announced = true;
            this.broadcastMessage("§1§lKisame: §7\"Samehada and I are one! This is my true form!\"");
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.42);
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)14.0F);

            for(int i = 0; i < 70; ++i) {
               this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)8.0F, this.posY + this.rand.nextDouble() * (double)4.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)8.0F, (this.rand.nextDouble() - (double)0.5F) * 0.4, this.rand.nextDouble() * 0.4, (this.rand.nextDouble() - (double)0.5F) * 0.4, new int[0]);
            }

            for(int i = 0; i < 30; ++i) {
               this.world.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, this.posY + this.rand.nextDouble() * (double)3.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, (double)0.0F, 0.1, (double)0.0F, new int[0]);
            }
         }

      }

      private int[] getCooldownRange(int[] p1, int[] p2, int[] p3) {
         switch (this.currentPhase) {
            case 2:
               return p2;
            case 3:
               return p3;
            default:
               return p1;
         }
      }

      private float getPhaseDamageMultiplier() {
         switch (this.currentPhase) {
            case 2:
               return 1.15F;
            case 3:
               return 1.4F;
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

      public double getDamageMultiplier() {
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
               EntityItachi.EntityCustom itachi = this.getPartnerItachi();
               UUID itachiTargetUUID = null;
               if (itachi != null && itachi.getAttackTarget() != null) {
                  itachiTargetUUID = itachi.getAttackTarget().getUniqueID();
               }

               float switchChance = 0.35F + (float)(validTargets.size() - 1) * 0.1F;
               switchChance = Math.min(switchChance, 0.7F);
               if (this.rand.nextFloat() < switchChance) {
                  List<EntityPlayer> otherTargets = new ArrayList();

                  for(EntityPlayer p : validTargets) {
                     if (this.currentTargetUUID == null || !p.getUniqueID().equals(this.currentTargetUUID)) {
                        otherTargets.add(p);
                     }
                  }

                  if (itachiTargetUUID != null && otherTargets.size() > 1) {
                     List<EntityPlayer> nonItachiTargets = new ArrayList();

                     for(EntityPlayer p : otherTargets) {
                        if (!p.getUniqueID().equals(itachiTargetUUID)) {
                           nonItachiTargets.add(p);
                        }
                     }

                     if (!nonItachiTargets.isEmpty()) {
                        otherTargets = nonItachiTargets;
                     }
                  }

                  EntityPlayer newTarget;
                  if (!otherTargets.isEmpty()) {
                     newTarget = (EntityPlayer)otherTargets.get(this.rand.nextInt(otherTargets.size()));
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

            if (this.waterSharkCooldown > 0) {
               --this.waterSharkCooldown;
            }

            if (this.waterCloneCooldown > 0) {
               --this.waterCloneCooldown;
            }

            if (this.waterPrisonCooldown > 0) {
               --this.waterPrisonCooldown;
            }

            if (this.chakraDrainCooldown > 0) {
               --this.chakraDrainCooldown;
            }

            if (this.rushCooldown > 0) {
               --this.rushCooldown;
            }

            if (this.comboCooldown > 0) {
               --this.comboCooldown;
            }

            if (this.targetSwitchCooldown > 0) {
               --this.targetSwitchCooldown;
            }

            if (this.leapSlamCooldown > 0) {
               --this.leapSlamCooldown;
            }

            if (this.verticalLeapCooldown > 0) {
               --this.verticalLeapCooldown;
            }

            if (this.coordComboCooldown > 0) {
               --this.coordComboCooldown;
            }

            if (this.coordComboTimer > 0) {
               --this.coordComboTimer;
               if (this.coordComboDelay > 0) {
                  --this.coordComboDelay;
               }

               if (this.coordComboTimer <= 0) {
                  this.cancelCoordCombo();
               }
            }

            if (this.attackAnimTimer > 0) {
               --this.attackAnimTimer;
               if (this.attackAnimTimer <= 0) {
                  this.setAttackState(0);
               }
            }

            if (this.isLeaping) {
               this.processLeapTick();
            }

            this.checkPhaseTransition();
            if (!this.isPassive && this.getAttackTarget() != null) {
               this.tryTargetSwitch();
            }

            if (this.waterPrisonActive) {
               --this.waterPrisonTimer;
               if (this.waterPrisonTimer <= 0) {
                  this.waterPrisonActive = false;
                  this.waterPrisonTargetUUID = null;
               } else if (this.waterPrisonTargetUUID != null) {
                  Entity prisonEntity = this.getEntityByUUID(this.waterPrisonTargetUUID);
                  if (prisonEntity instanceof EntityLivingBase && this.ticksExisted % 5 == 0) {
                     for(int i = 0; i < 10; ++i) {
                        double angle = (Math.PI / 5D) * (double)i;
                        double px = prisonEntity.posX + Math.cos(angle) * (double)1.5F;
                        double pz = prisonEntity.posZ + Math.sin(angle) * (double)1.5F;
                        this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, px, prisonEntity.posY + (double)1.0F, pz, (double)0.0F, 0.05, (double)0.0F, new int[0]);
                     }
                  }
               }
            }

            if (this.isRushing) {
               this.processRushTick();
            }

            if (this.inActiveCombo && this.comboTimer <= 0 && this.activeComboTarget != null) {
               if (!this.activeComboTarget.isDead && this.getDistanceSq(this.activeComboTarget) <= (double)16.0F) {
                  this.performComboHit((EntityLivingBase)this.activeComboTarget);
               } else {
                  this.inActiveCombo = false;
                  this.activeComboTarget = null;
                  this.comboHitCount = 0;
               }
            }

            if (this.comboTimer > 0) {
               --this.comboTimer;
            }

            if (this.currentPhase >= 3) {
               ++this.regenTimer;
               if (this.regenTimer >= 1200) {
                  this.regenTimer = 0;
                  float healAmount = this.getMaxHealth() * 0.01F;
                  this.heal(healAmount);
               }
            }

            if (!this.isPassive && this.getHeldItemMainhand().isEmpty() && this.ticksExisted % 20 == 0) {
               this.equipWeapon();
            }

            if (!this.isPassive && this.isKnockbackImmune() && (Math.abs(this.motionX) > 0.3 || Math.abs(this.motionZ) > 0.3 || this.motionY > 0.4)) {
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

            if (this.ticksExisted % 10 == 5) {
               this.unstickFromBlocks();
            }

            if (!this.isPassive && !this.isLeaping && this.onGround && this.verticalLeapCooldown <= 0) {
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

            if (!this.onGround && !this.isLeaping && this.verticalLeapCooldown > 0) {
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
               BlockPos checkPos = new BlockPos(moveX, moveY, moveZ);
               if (!this.world.getBlockState(checkPos.down()).getMaterial().isSolid()) {
                  for(int i = 0; i < 5; ++i) {
                     BlockPos below = checkPos.down(i + 1);
                     if (this.world.getBlockState(below).getMaterial().isSolid()) {
                        moveY = (double)below.getY() + (double)1.0F;
                        break;
                     }
                  }
               }

               BlockPos foot = new BlockPos(moveX, moveY, moveZ);
               BlockPos head = new BlockPos(moveX, moveY + (double)1.0F, moveZ);
               if (!this.world.getBlockState(foot).getMaterial().isSolid() && !this.world.getBlockState(head).getMaterial().isSolid()) {
                  this.setPositionAndUpdate(moveX, moveY, moveZ);
                  this.motionX = (double)0.0F;
                  this.motionY = (double)0.0F;
                  this.motionZ = (double)0.0F;
               }
            }
         }
      }

      private boolean isPositionSafe(double x, double y, double z) {
         BlockPos foot = new BlockPos(x, y, z);
         BlockPos head = new BlockPos(x, y + (double)1.0F, z);
         BlockPos ground = new BlockPos(x, y - (double)1.0F, z);
         return this.world.getBlockState(ground).getMaterial().isSolid() && !this.world.getBlockState(foot).getMaterial().isSolid() && !this.world.getBlockState(head).getMaterial().isSolid();
      }

      private double findSafeY(double x, double referenceY, double z) {
         int startY = Math.min((int)referenceY + 10, 255);
         int minY = Math.max((int)referenceY - 20, 1);

         for(int y = startY; y >= minY; --y) {
            if (this.isPositionSafe(x, (double)y, z)) {
               return (double)y;
            }
         }

         return referenceY;
      }

      private void unstickFromBlocks() {
         BlockPos foot = new BlockPos(this.posX, this.posY, this.posZ);
         BlockPos head = new BlockPos(this.posX, this.posY + (double)1.0F, this.posZ);
         boolean feetStuck = this.world.getBlockState(foot).getMaterial().isSolid();
         boolean headStuck = this.world.getBlockState(head).getMaterial().isSolid();
         if (feetStuck || headStuck) {
            for(int y = (int)this.posY; (double)y < this.posY + (double)10.0F && y < 255; ++y) {
               if (this.isPositionSafe(this.posX, (double)y, this.posZ)) {
                  this.setPositionAndUpdate(this.posX, (double)y, this.posZ);
                  return;
               }
            }

            for(int r = 1; r <= 5; ++r) {
               for(int dx = -r; dx <= r; ++dx) {
                  for(int dz = -r; dz <= r; ++dz) {
                     if (Math.abs(dx) == r || Math.abs(dz) == r) {
                        double checkX = this.posX + (double)dx;
                        double checkZ = this.posZ + (double)dz;
                        double checkY = this.findSafeY(checkX, this.posY, checkZ);
                        if (this.isPositionSafe(checkX, checkY, checkZ)) {
                           this.setPositionAndUpdate(checkX, checkY, checkZ);
                           return;
                        }
                     }
                  }
               }
            }

         }
      }

      private Entity getEntityByUUID(UUID uuid) {
         for(Entity e : this.world.loadedEntityList) {
            if (e.getUniqueID().equals(uuid)) {
               return e;
            }
         }

         return null;
      }

      public boolean attackEntityAsMob(EntityLivingBase target) {
         double dmgMult = this.getDamageMultiplier();
         float normDmg = (float)((double)10.0F * dmgMult);
         float trueDmg = (float)((double)3.0F * dmgMult);
         boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
         if (hit) {
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            float drainPercent = this.currentPhase >= 3 ? 0.15F : 0.1F;
            this.heal((normDmg + trueDmg) * drainPercent);
         }

         return hit;
      }

      public void startCombo(EntityLivingBase target) {
         if (this.comboCooldown <= 0) {
            int[] cdRange = this.getCooldownRange(CD_COMBO_P1, CD_COMBO_P2, CD_COMBO_P3);
            this.comboCooldown = this.randomCooldown(cdRange);
            this.comboHitCount = 0;
            this.inActiveCombo = true;
            this.activeComboTarget = target;
            this.comboTimer = 0;
            this.setAttackState(7);
         }
      }

      private void performComboHit(EntityLivingBase target) {
         double dmgMult = this.getDamageMultiplier();
         boolean isHeavy = this.comboHitCount >= 3;
         float normDmg;
         float trueDmg;
         if (isHeavy) {
            normDmg = (float)((double)17.5F * dmgMult);
            trueDmg = (float)((double)5.0F * dmgMult);
         } else {
            normDmg = (float)((double)8.8F * dmgMult);
            trueDmg = (float)((double)3.0F * dmgMult);
         }

         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         float drainPercent = this.currentPhase >= 3 ? 0.12F : 0.08F;
         this.heal((normDmg + trueDmg) * drainPercent);
         this.swingArm(EnumHand.MAIN_HAND);
         if (isHeavy) {
            float kb = 1.0F;
            target.motionX += (target.posX - this.posX) * 0.12 * (double)kb;
            target.motionY += 0.4;
            target.motionZ += (target.posZ - this.posZ) * 0.12 * (double)kb;
            if (target instanceof EntityPlayerMP) {
               ((EntityPlayerMP)target).velocityChanged = true;
            }

            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.7F);
            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.4F, 1.5F);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 20, 0.6, 0.6, 0.6, (double)0.25F, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, target.posX, target.posY + (double)1.0F, target.posZ, 10, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
            }

            this.comboHitCount = 0;
            this.inActiveCombo = false;
            this.activeComboTarget = null;
            this.attackAnimTimer = 15;
         } else {
            float pitch = 0.8F + (float)this.comboHitCount * 0.12F;
            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.8F, pitch);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 5 + this.comboHitCount * 3, 0.3, 0.3, 0.3, 0.05, new int[0]);
            }

            ++this.comboHitCount;
            int delayIndex = Math.min(this.comboHitCount, COMBO_DELAYS.length - 1);
            this.comboTimer = COMBO_DELAYS[delayIndex];
            this.attackAnimTimer = this.comboTimer + 5;
         }

      }

      public void fireWaterShark(EntityLivingBase target) {
         if (this.waterSharkCooldown <= 0) {
            int[] cdRange = this.getCooldownRange(CD_WATER_SHARK_P1, CD_WATER_SHARK_P2, CD_WATER_SHARK_P3);
            this.waterSharkCooldown = this.randomCooldown(cdRange);
            this.setAttackState(2);
            this.attackAnimTimer = 20;
            double dmgMult = this.getDamageMultiplier();
            float normDmg = (float)((double)15.0F * dmgMult);
            float trueDmg = (float)((double)5.0F * dmgMult);
            EntitySuitonShark.EntityCustom shark = new EntitySuitonShark.EntityCustom(this.world, this, target, normDmg, trueDmg);
            this.world.spawnEntity(shark);

            for(int i = 0; i < 15; ++i) {
               this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posY + (double)0.5F + this.rand.nextDouble(), this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, (double)0.0F, 0.1, (double)0.0F, new int[0]);
            }

         }
      }

      private boolean hasActiveWaterClone() {
         String myUUID = this.getUniqueID().toString();

         for(Entity e : this.world.loadedEntityList) {
            if (e instanceof EntityWaterClone.EntityCustom && e.isEntityAlive() && myUUID.equals(((EntityWaterClone.EntityCustom)e).getOwnerUUID())) {
               return true;
            }
         }

         return false;
      }

      public void spawnWaterClone() {
         if (!this.getEntityData().getBoolean("questEntity")) {
            if (this.waterCloneCooldown <= 0) {
               if (!this.hasActiveWaterClone()) {
                  int[] cdRange = this.getCooldownRange(CD_WATER_CLONE_P1, CD_WATER_CLONE_P2, CD_WATER_CLONE_P3);
                  this.waterCloneCooldown = this.randomCooldown(cdRange);
                  this.setAttackState(3);
                  this.attackAnimTimer = 20;
                  double offsetX = (this.rand.nextDouble() - (double)0.5F) * (double)4.0F;
                  double offsetZ = (this.rand.nextDouble() - (double)0.5F) * (double)4.0F;
                  double spawnX = this.posX + offsetX;
                  double spawnZ = this.posZ + offsetZ;
                  double spawnY = this.posY;
                  EntityWaterClone.EntityCustom clone = new EntityWaterClone.EntityCustom(this.world);
                  clone.setPosition(spawnX, spawnY, spawnZ);
                  clone.setOwnerUUID(this.getUniqueID().toString());
                  clone.setCustomNameTag("Water Clone");
                  clone.setAlwaysRenderNameTag(true);

                  for(int i = 0; i < 20; ++i) {
                     this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, spawnX + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, spawnY + this.rand.nextDouble() * 1.8, spawnZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, (double)0.0F, 0.1, (double)0.0F, new int[0]);
                  }

                  this.world.spawnEntity(clone);
                  EntityLivingBase attackTarget = this.getAttackTarget();
                  EntityItachi.EntityCustom itachi = this.getPartnerItachi();
                  if (attackTarget != null && itachi != null && itachi.isEntityAlive() && !itachi.hasCoordCombo() && this.coordComboCooldown <= 0 && this.rand.nextFloat() < 0.3F) {
                     itachi.requestCoordCombo(5, attackTarget.getUniqueID());
                  }

               }
            }
         }
      }

      public void useWaterPrison(EntityLivingBase target) {
         if (this.waterPrisonCooldown <= 0) {
            double dist = (double)this.getDistance(target);
            if (!(dist > R_PRISON[1])) {
               int[] cdRange = this.getCooldownRange(CD_WATER_PRISON_P1, CD_WATER_PRISON_P2, CD_WATER_PRISON_P3);
               this.waterPrisonCooldown = this.randomCooldown(cdRange);
               this.setAttackState(4);
               this.attackAnimTimer = 30;
               int duration = this.currentPhase >= 3 ? 80 : 60;
               this.waterPrisonActive = true;
               this.waterPrisonTimer = duration;
               this.waterPrisonTargetUUID = target.getUniqueID();
               double dmgMult = this.getDamageMultiplier();
               target.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((double)7.5F * dmgMult));
               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.MAGIC, (float)((double)3.0F * dmgMult));
               int slowLevel = this.currentPhase >= 2 ? 5 : 4;
               target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, duration, slowLevel));
               target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, duration, 2));
               target.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, duration, 0));

               for(int i = 0; i < 30; ++i) {
                  double angle = 0.20943951023931953 * (double)i;
                  double px = target.posX + Math.cos(angle) * (double)2.0F;
                  double pz = target.posZ + Math.sin(angle) * (double)2.0F;
                  this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, px, target.posY + this.rand.nextDouble() * (double)2.0F, pz, (double)0.0F, 0.1, (double)0.0F, new int[0]);
               }

               EntityItachi.EntityCustom itachi = this.getPartnerItachi();
               if (itachi != null && itachi.isEntityAlive() && !itachi.hasCoordCombo() && this.coordComboCooldown <= 0) {
                  itachi.requestCoordCombo(3, target.getUniqueID());
               }

            }
         }
      }

      public void useChakraDrain(EntityLivingBase target) {
         if (this.chakraDrainCooldown <= 0) {
            double dist = (double)this.getDistance(target);
            if (!(dist > R_DRAIN[1])) {
               int[] cdRange = this.getCooldownRange(CD_CHAKRA_DRAIN_P1, CD_CHAKRA_DRAIN_P2, CD_CHAKRA_DRAIN_P3);
               this.chakraDrainCooldown = this.randomCooldown(cdRange);
               this.setAttackState(5);
               this.attackAnimTimer = 25;
               double dmgMult = this.getDamageMultiplier();
               float normDmg = (float)((double)11.3F * dmgMult);
               float trueDmg = (float)((double)4.0F * dmgMult);
               float totalDamage = 0.0F;
               double aoeRange = this.currentPhase >= 3 ? (double)5.0F : (double)4.0F;

               for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(aoeRange))) {
                  if (e instanceof EntityLivingBase && !(e instanceof EntityItachi.EntityCustom) && !(e instanceof EntityWaterClone.EntityCustom)) {
                     EntityLivingBase living = (EntityLivingBase)e;
                     living.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                     living.hurtResistantTime = 0;
                     living.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                     living.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 1));
                     totalDamage += normDmg + trueDmg;
                  }
               }

               float healPercent = this.currentPhase >= 3 ? 0.2F : 0.15F;
               this.heal(totalDamage * healPercent);
               int particleCount = this.currentPhase >= 3 ? 35 : 25;

               for(int i = 0; i < particleCount; ++i) {
                  double angle = (Math.PI * 2D) / (double)particleCount * (double)i;
                  double px = this.posX + Math.cos(angle) * (aoeRange - (double)0.5F);
                  double pz = this.posZ + Math.sin(angle) * (aoeRange - (double)0.5F);
                  this.world.spawnParticle(EnumParticleTypes.WATER_DROP, px, this.posY + (double)1.0F, pz, (this.posX - px) * 0.1, 0.1, (this.posZ - pz) * 0.1, new int[0]);
               }

               EntityItachi.EntityCustom itachi = this.getPartnerItachi();
               if (itachi != null && itachi.isEntityAlive() && !itachi.hasCoordCombo() && this.coordComboCooldown <= 0 && this.rand.nextFloat() < 0.4F) {
                  itachi.requestCoordCombo(4, target.getUniqueID());
               }

            }
         }
      }

      public void performRush(EntityLivingBase target) {
         if (this.rushCooldown <= 0) {
            double dist = (double)this.getDistance(target);
            if (!(dist < (double)6.0F) && !(dist > (double)20.0F)) {
               int[] cdRange = this.getCooldownRange(CD_RUSH_P1, CD_RUSH_P2, CD_RUSH_P3);
               this.rushCooldown = this.randomCooldown(cdRange);
               this.setAttackState(6);
               this.attackAnimTimer = 13;
               double dx = target.posX - this.posX;
               double dz = target.posZ - this.posZ;
               double d = Math.sqrt(dx * dx + dz * dz);
               if (d > (double)0.0F) {
                  double rushDist = Math.min(d - (double)1.5F, (double)18.0F);
                  if (rushDist <= (double)0.0F) {
                     return;
                  }

                  double nx = dx / d;
                  double nz = dz / d;
                  double velocityPerTick = rushDist / (double)8.0F;
                  this.isRushing = true;
                  this.rushTicksRemaining = 8;
                  this.rushVelocityX = nx * velocityPerTick * 1.3;
                  this.rushVelocityZ = nz * velocityPerTick * 1.3;
                  this.motionY = 0.2;
                  this.velocityChanged = true;
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + (double)0.5F, this.posZ, 25, 0.8, (double)0.5F, 0.8, 0.15, new int[0]);
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 10, 0.4, 0.3, 0.4, 0.1, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.5F, 0.7F);
               }

            }
         }
      }

      private void processRushTick() {
         --this.rushTicksRemaining;
         this.motionX = this.rushVelocityX;
         this.motionZ = this.rushVelocityZ;
         if (this.rushTicksRemaining > 4) {
            this.motionY = 0.05;
         } else {
            this.motionY = Math.max(-0.3, this.motionY - 0.06);
         }

         this.velocityChanged = true;
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + (double)0.5F, this.posZ, 3, 0.2, 0.2, 0.2, 0.02, new int[0]);
         }

         for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)1.0F))) {
            if (e instanceof EntityLivingBase && !(e instanceof EntityItachi.EntityCustom) && !(e instanceof EntityWaterClone.EntityCustom)) {
               float rushDmg = (float)((double)5.0F * this.getDamageMultiplier());
               e.attackEntityFrom(DamageSource.causeMobDamage(this), rushDmg);
            }
         }

         if (this.rushTicksRemaining <= 0) {
            this.isRushing = false;
            this.rushVelocityX = (double)0.0F;
            this.rushVelocityZ = (double)0.0F;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + 0.1, this.posZ, 15, (double)0.5F, 0.1, (double)0.5F, 0.1, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_SMALL_FALL, SoundCategory.HOSTILE, 1.0F, 1.0F);
         }

      }

      public void performLeapSlam(EntityLivingBase target) {
         if (this.leapSlamCooldown <= 0 && !this.isLeaping && !this.isRushing) {
            double dist = (double)this.getDistance(target);
            if (!(dist < (double)5.0F) && !(dist > (double)16.0F)) {
               int[] cdRange = this.getCooldownRange(CD_LEAP_SLAM_P1, CD_LEAP_SLAM_P2, CD_LEAP_SLAM_P3);
               this.leapSlamCooldown = this.randomCooldown(cdRange);
               this.setAttackState(8);
               this.attackAnimTimer = 22;
               double dx = target.posX - this.posX;
               double dz = target.posZ - this.posZ;
               double d = Math.sqrt(dx * dx + dz * dz);
               if (d > (double)0.0F) {
                  double leapDist = Math.min(d - (double)1.0F, (double)15.0F);
                  if (leapDist <= (double)0.0F) {
                     return;
                  }

                  double nx = dx / d;
                  double nz = dz / d;
                  double velocityPerTick = leapDist / (double)12.0F * (double)1.5F;
                  this.isLeaping = true;
                  this.leapTicksRemaining = 12;
                  this.leapVelocityX = nx * velocityPerTick;
                  this.leapVelocityZ = nz * velocityPerTick;
                  this.motionY = 0.6;
                  this.velocityChanged = true;
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + (double)0.5F, this.posZ, 30, (double)1.0F, (double)0.5F, (double)1.0F, 0.2, new int[0]);
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 15, (double)0.5F, 0.3, (double)0.5F, 0.1, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.8F, 0.5F);
               }

            }
         }
      }

      private void processLeapTick() {
         --this.leapTicksRemaining;
         this.motionX = this.leapVelocityX;
         this.motionZ = this.leapVelocityZ;
         if (this.leapTicksRemaining > 6) {
            this.motionY = 0.1;
         } else {
            this.motionY = Math.max(-0.8, this.motionY - 0.12);
         }

         this.velocityChanged = true;
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + (double)0.5F, this.posZ, 5, 0.3, 0.3, 0.3, 0.05, new int[0]);
         }

         if (this.leapTicksRemaining <= 0) {
            this.isLeaping = false;
            this.leapVelocityX = (double)0.0F;
            this.leapVelocityZ = (double)0.0F;
            double dmgMult = this.getDamageMultiplier();
            float normDmg = (float)((double)22.5F * dmgMult);
            float trueDmg = (float)((double)8.0F * dmgMult);

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)3.5F))) {
               if (e instanceof EntityLivingBase && !(e instanceof EntityItachi.EntityCustom) && !(e instanceof EntityWaterClone.EntityCustom)) {
                  EntityLivingBase living = (EntityLivingBase)e;
                  living.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                  living.hurtResistantTime = 0;
                  living.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  double kbX = living.posX - this.posX;
                  double kbZ = living.posZ - this.posZ;
                  double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
                  if (kbDist > (double)0.0F) {
                     living.motionX += kbX / kbDist * 0.8;
                     living.motionY += 0.3;
                     living.motionZ += kbZ / kbDist * 0.8;
                     living.velocityChanged = true;
                  }

                  float drainPercent = 0.2F + this.rand.nextFloat() * 0.1F;
                  this.heal((normDmg + trueDmg) * drainPercent);
               }
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + 0.1, this.posZ, 40, (double)1.5F, 0.2, (double)1.5F, 0.15, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + 0.2, this.posZ, 20, (double)1.0F, 0.3, (double)1.0F, 0.1, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.1, this.posZ, 15, 1.2, 0.1, 1.2, 0.05, new int[]{Blocks.WATER.getDefaultState().hashCode()});
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.2F, 0.8F);
         }

      }

      public boolean isWaterPrisonActive() {
         return this.waterPrisonActive;
      }

      public Entity getWaterPrisonTarget() {
         return this.waterPrisonTargetUUID == null ? null : this.getEntityByUUID(this.waterPrisonTargetUUID);
      }

      public UUID getItachiUUID() {
         return this.itachiUUID;
      }

      public void setItachiUUID(UUID uuid) {
         this.itachiUUID = uuid;
      }

      public EntityItachi.EntityCustom getPartnerItachi() {
         if (this.itachiUUID == null) {
            return null;
         } else {
            for(Entity e : this.world.loadedEntityList) {
               if (e instanceof EntityItachi.EntityCustom && e.getUniqueID().equals(this.itachiUUID)) {
                  return (EntityItachi.EntityCustom)e;
               }
            }

            return null;
         }
      }

      public void heal(float amount) {
         if (this.getEntityData().getBoolean("questEntity")) {
            float maxAllowed = this.getMaxHealth() * 0.5F;
            if (this.getHealth() >= maxAllowed) {
               return;
            }

            float headroom = maxAllowed - this.getHealth();
            amount = Math.min(amount, headroom);
            if (amount <= 0.0F) {
               return;
            }
         }

         super.heal(amount);
      }

      public void setRevengeTarget(EntityLivingBase target) {
         if (!(target instanceof EntityItachi.EntityCustom)) {
            if (!(target instanceof EntityCustom)) {
               super.setRevengeTarget(target);
            }
         }
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         if (this.isPassive) {
            return false;
         } else {
            Entity src = source.getTrueSource();
            if (src instanceof EntityItachi.EntityCustom) {
               return false;
            } else {
               double defMult = this.getDefenseMultiplier();
               amount = (float)((double)amount / defMult);
               return super.attackEntityFrom(source, amount);
            }
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

      public boolean isRetreating() {
         return (Boolean)this.dataManager.get(IS_RETREATING);
      }

      public void setRetreating(boolean retreating) {
         this.dataManager.set(IS_RETREATING, retreating);
      }

      public int getCurrentPhase() {
         return (Integer)this.dataManager.get(PHASE);
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
         if (config.isPassive()) {
            this.setNoAI(true);
         } else {
            this.setNoAI(false);
         }

         this.setCustomNameTag(config.getDisplayName());
         this.setAlwaysRenderNameTag(true);
         if (!config.isPassive()) {
            this.equipWeapon();
         }

      }

      private void equipWeapon() {
         Item weapon = (Item)Item.REGISTRY.getObject(new ResourceLocation("inftsukaddon", "samehada"));
         if (weapon != null) {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(weapon));
            this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
         }

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
         compound.setBoolean("isRetreating", this.isRetreating());
         compound.setInteger("currentPhase", this.currentPhase);
         compound.setBoolean("phase2Announced", this.phase2Announced);
         compound.setBoolean("phase3Announced", this.phase3Announced);
         if (this.itachiUUID != null) {
            compound.setString("itachiUUID", this.itachiUUID.toString());
         }

         compound.setInteger("waterSharkCD", this.waterSharkCooldown);
         compound.setInteger("waterCloneCD", this.waterCloneCooldown);
         compound.setInteger("waterPrisonCD", this.waterPrisonCooldown);
         compound.setInteger("chakraDrainCD", this.chakraDrainCooldown);
         compound.setInteger("rushCD", this.rushCooldown);
         compound.setInteger("comboCD", this.comboCooldown);
         compound.setBoolean("waterPrisonActive", this.waterPrisonActive);
         compound.setInteger("waterPrisonTimer", this.waterPrisonTimer);
         compound.setInteger("leapSlamCD", this.leapSlamCooldown);
         compound.setInteger("coordComboCD", this.coordComboCooldown);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.isPassive = compound.getBoolean("isPassive");
         this.setRetreating(compound.getBoolean("isRetreating"));
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

         if (compound.hasKey("itachiUUID")) {
            try {
               this.itachiUUID = UUID.fromString(compound.getString("itachiUUID"));
            } catch (IllegalArgumentException var4) {
            }
         }

         this.waterSharkCooldown = compound.getInteger("waterSharkCD");
         this.waterCloneCooldown = compound.getInteger("waterCloneCD");
         this.waterPrisonCooldown = compound.getInteger("waterPrisonCD");
         this.chakraDrainCooldown = compound.getInteger("chakraDrainCD");
         this.rushCooldown = compound.getInteger("rushCD");
         this.comboCooldown = compound.getInteger("comboCD");
         this.waterPrisonActive = compound.getBoolean("waterPrisonActive");
         this.waterPrisonTimer = compound.getInteger("waterPrisonTimer");
         this.leapSlamCooldown = compound.getInteger("leapSlamCD");
         this.coordComboCooldown = compound.getInteger("coordComboCD");
      }

      static {
         NPC_CONFIG_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
         ATTACK_STATE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         IS_RETREATING = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
         PHASE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         CD_WATER_SHARK_P1 = new int[]{45, 65};
         CD_WATER_CLONE_P1 = new int[]{80, 110};
         CD_WATER_PRISON_P1 = new int[]{350, 500};
         CD_CHAKRA_DRAIN_P1 = new int[]{55, 75};
         CD_RUSH_P1 = new int[]{80, 120};
         CD_COMBO_P1 = new int[]{40, 60};
         CD_WATER_SHARK_P2 = new int[]{30, 50};
         CD_WATER_CLONE_P2 = new int[]{50, 75};
         CD_WATER_PRISON_P2 = new int[]{200, 350};
         CD_CHAKRA_DRAIN_P2 = new int[]{35, 55};
         CD_RUSH_P2 = new int[]{50, 80};
         CD_COMBO_P2 = new int[]{30, 45};
         CD_WATER_SHARK_P3 = new int[]{20, 35};
         CD_WATER_CLONE_P3 = new int[]{35, 55};
         CD_WATER_PRISON_P3 = new int[]{150, 250};
         CD_CHAKRA_DRAIN_P3 = new int[]{25, 40};
         CD_RUSH_P3 = new int[]{30, 50};
         CD_COMBO_P3 = new int[]{20, 35};
         R_SHARK = new double[]{(double)4.0F, (double)30.0F};
         R_CLONE = new double[]{(double)0.0F, (double)30.0F};
         R_PRISON = new double[]{(double)2.0F, (double)6.0F};
         R_DRAIN = new double[]{(double)2.0F, (double)5.0F};
         R_MELEE = new double[]{(double)0.0F, (double)3.0F};
         R_COMBO = new double[]{(double)0.0F, (double)3.5F};
         COMBO_DELAYS = new int[]{0, 8, 10, 14};
         CD_LEAP_SLAM_P1 = new int[]{200, 280};
         CD_LEAP_SLAM_P2 = new int[]{140, 200};
         CD_LEAP_SLAM_P3 = new int[]{80, 120};
         PARALYSIS_MODIFIER_UUID = UUID.fromString("c69af92a-b96d-49b7-a396-9b3b0d77edd5");
         HEAVINESS_SPEED_UUID = UUID.fromString("7d735ff6-8872-482d-ac1f-cd2249e8f584");
         HEAVINESS_ATKSPD_UUID = UUID.fromString("fedf4303-bc45-4ad8-80e8-2237e9c90a18");
      }
   }

   public static class EntityAIKisameCombat extends EntityAIBase {
      private final EntityCustom kisame;
      private EntityLivingBase target;
      private int attackTick;

      public EntityAIKisameCombat(EntityCustom kisame) {
         this.kisame = kisame;
         this.setMutexBits(3);
      }

      public boolean shouldExecute() {
         EntityLivingBase t = this.kisame.getAttackTarget();
         if (t != null && t.isEntityAlive()) {
            this.target = t;
            return true;
         } else {
            return false;
         }
      }

      public boolean shouldContinueExecuting() {
         return this.target != null && this.target.isEntityAlive() && this.kisame.isEntityAlive();
      }

      public void resetTask() {
         this.target = null;
         this.kisame.setAttackState(0);
         this.kisame.inActiveCombo = false;
         this.kisame.activeComboTarget = null;
         this.kisame.comboHitCount = 0;
      }

      public void updateTask() {
         if (this.target != null && this.target.isEntityAlive()) {
            if (this.kisame.getAttackTarget() != null && this.kisame.getAttackTarget() != this.target) {
               this.target = this.kisame.getAttackTarget();
            }

            double dist = (double)this.kisame.getDistance(this.target);
            this.kisame.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
            if (!this.kisame.inActiveCombo && !this.kisame.isRushing && !this.kisame.isLeaping) {
               if (this.kisame.isRetreating()) {
                  this.handleRetreating(dist);
               } else {
                  if (this.kisame.hasCoordCombo() && this.kisame.coordComboDelay <= 0) {
                     EntityLivingBase comboTarget = this.findCoordTarget(this.kisame.coordComboTargetUUID);
                     if (comboTarget != null && comboTarget.isEntityAlive()) {
                        double comboDist = (double)this.kisame.getDistance(comboTarget);
                        switch (this.kisame.coordComboId) {
                           case 1:
                              if (comboDist >= (double)6.0F && this.kisame.rushCooldown <= 0) {
                                 this.kisame.performRush(comboTarget);
                              } else if (comboDist <= EntityCustom.R_COMBO[1] && this.kisame.comboCooldown <= 0) {
                                 this.kisame.startCombo(comboTarget);
                              }
                              break;
                           case 2:
                              if (comboDist >= (double)6.0F && this.kisame.rushCooldown <= 0) {
                                 this.kisame.performRush(comboTarget);
                              } else if (this.kisame.leapSlamCooldown <= 0 && comboDist >= (double)5.0F) {
                                 this.kisame.performLeapSlam(comboTarget);
                              } else if (comboDist <= EntityCustom.R_COMBO[1] && this.kisame.comboCooldown <= 0) {
                                 this.kisame.startCombo(comboTarget);
                              }
                        }

                        this.kisame.coordComboCooldown = this.kisame.getCoordGlobalCooldown();
                        this.kisame.cancelCoordCombo();
                        return;
                     }

                     this.kisame.cancelCoordCombo();
                  }

                  ++this.attackTick;
                  this.handleMovement(dist);
                  switch (this.kisame.currentPhase) {
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
      }

      private void handleRetreating(double dist) {
         if (dist < (double)5.0F) {
            double dx = this.kisame.posX - this.target.posX;
            double dz = this.kisame.posZ - this.target.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > (double)0.0F) {
               this.kisame.getNavigator().tryMoveToXYZ(this.kisame.posX + dx / d * (double)10.0F, this.kisame.posY, this.kisame.posZ + dz / d * (double)10.0F, 1.3);
            }
         }

      }

      private void handleMovement(double dist) {
         switch (this.kisame.currentPhase) {
            case 1:
               if (dist > (double)6.0F) {
                  this.kisame.getNavigator().tryMoveToEntityLiving(this.target, 1.3);
               } else if (dist < (double)2.0F) {
                  this.kisame.getNavigator().clearPath();
               } else {
                  this.kisame.getNavigator().tryMoveToEntityLiving(this.target, (double)1.0F);
               }
               break;
            case 2:
               if (dist > (double)10.0F) {
                  this.kisame.getNavigator().tryMoveToEntityLiving(this.target, 1.2);
               } else if (dist < (double)4.0F) {
                  double dx = this.kisame.posX - this.target.posX;
                  double dz = this.kisame.posZ - this.target.posZ;
                  double d = Math.sqrt(dx * dx + dz * dz);
                  if (d > (double)0.0F) {
                     this.kisame.getNavigator().tryMoveToXYZ(this.kisame.posX + dx / d * (double)4.0F, this.kisame.posY, this.kisame.posZ + dz / d * (double)4.0F, 1.1);
                  }
               } else {
                  this.kisame.getNavigator().tryMoveToEntityLiving(this.target, 0.9);
               }
               break;
            case 3:
               if (dist > (double)5.0F) {
                  this.kisame.getNavigator().tryMoveToEntityLiving(this.target, (double)1.5F);
               } else {
                  this.kisame.getNavigator().tryMoveToEntityLiving(this.target, 1.1);
               }
         }

      }

      private void executePhase1(double dist) {
         if (this.kisame.rushCooldown <= 0 && dist >= (double)6.0F && dist <= (double)20.0F) {
            this.kisame.performRush(this.target);
         } else if (this.kisame.waterPrisonCooldown <= 0 && dist >= EntityCustom.R_PRISON[0] && dist <= EntityCustom.R_PRISON[1]) {
            this.kisame.useWaterPrison(this.target);
         } else if (this.kisame.comboCooldown <= 0 && dist <= EntityCustom.R_COMBO[1]) {
            this.kisame.startCombo(this.target);
         } else if (this.kisame.chakraDrainCooldown <= 0 && dist >= EntityCustom.R_DRAIN[0] && dist <= EntityCustom.R_DRAIN[1]) {
            this.kisame.useChakraDrain(this.target);
         } else if (this.kisame.waterSharkCooldown <= 0 && dist >= EntityCustom.R_SHARK[0] && dist <= EntityCustom.R_SHARK[1]) {
            this.kisame.fireWaterShark(this.target);
         } else if (this.kisame.waterCloneCooldown <= 0) {
            this.kisame.spawnWaterClone();
         } else {
            if (dist <= EntityCustom.R_MELEE[1] && this.attackTick >= 18) {
               this.performBasicMelee();
            }

         }
      }

      private void executePhase2(double dist) {
         if (this.kisame.waterPrisonCooldown <= 0 && dist >= EntityCustom.R_PRISON[0] && dist <= EntityCustom.R_PRISON[1]) {
            this.kisame.useWaterPrison(this.target);
         } else if (this.kisame.chakraDrainCooldown <= 0 && dist >= EntityCustom.R_DRAIN[0] && dist <= EntityCustom.R_DRAIN[1]) {
            this.kisame.useChakraDrain(this.target);
         } else if (this.kisame.waterSharkCooldown <= 0 && dist >= EntityCustom.R_SHARK[0] && dist <= EntityCustom.R_SHARK[1]) {
            this.kisame.fireWaterShark(this.target);
         } else if (this.kisame.waterCloneCooldown <= 0) {
            this.kisame.spawnWaterClone();
         } else if (this.kisame.rushCooldown <= 0 && dist >= (double)6.0F && dist <= (double)20.0F) {
            this.kisame.performRush(this.target);
         } else if (this.kisame.leapSlamCooldown <= 0 && dist >= (double)5.0F && dist <= (double)16.0F) {
            this.kisame.performLeapSlam(this.target);
         } else if (this.kisame.comboCooldown <= 0 && dist <= EntityCustom.R_COMBO[1]) {
            this.kisame.startCombo(this.target);
         } else {
            if (dist <= EntityCustom.R_MELEE[1] && this.attackTick >= 15) {
               this.performBasicMelee();
            }

         }
      }

      private void executePhase3(double dist) {
         if (this.kisame.leapSlamCooldown <= 0 && dist >= (double)5.0F && dist <= (double)16.0F) {
            this.kisame.performLeapSlam(this.target);
         } else if (this.kisame.rushCooldown <= 0 && this.kisame.comboCooldown <= 0 && dist >= (double)6.0F && dist <= (double)20.0F) {
            this.kisame.performRush(this.target);
         } else if (this.kisame.chakraDrainCooldown <= 0 && dist >= EntityCustom.R_DRAIN[0] && dist <= EntityCustom.R_DRAIN[1]) {
            this.kisame.useChakraDrain(this.target);
         } else if (this.kisame.waterPrisonCooldown <= 0 && dist >= EntityCustom.R_PRISON[0] && dist <= EntityCustom.R_PRISON[1]) {
            this.kisame.useWaterPrison(this.target);
         } else if (this.kisame.comboCooldown <= 0 && dist <= EntityCustom.R_COMBO[1]) {
            this.kisame.startCombo(this.target);
         } else if (this.kisame.waterSharkCooldown <= 0 && dist >= EntityCustom.R_SHARK[0] && dist <= EntityCustom.R_SHARK[1]) {
            this.kisame.fireWaterShark(this.target);
         } else if (this.kisame.waterCloneCooldown <= 0) {
            this.kisame.spawnWaterClone();
         } else if (this.kisame.rushCooldown <= 0 && dist >= (double)6.0F && dist <= (double)20.0F) {
            this.kisame.performRush(this.target);
         } else {
            if (dist <= EntityCustom.R_MELEE[1] && this.attackTick >= 12) {
               this.performBasicMelee();
            }

         }
      }

      private void performBasicMelee() {
         this.attackTick = 0;
         this.kisame.setAttackState(1);
         this.kisame.attackAnimTimer = 10;
         this.kisame.swingArm(EnumHand.MAIN_HAND);
         this.kisame.attackEntityAsMob(this.target);
      }

      private EntityLivingBase findCoordTarget(UUID targetUUID) {
         if (targetUUID == null) {
            return null;
         } else {
            for(Entity e : this.kisame.world.loadedEntityList) {
               if (e instanceof EntityLivingBase && e.getUniqueID().equals(targetUUID) && e.isEntityAlive()) {
                  return (EntityLivingBase)e;
               }
            }

            return null;
         }
      }
   }
}
