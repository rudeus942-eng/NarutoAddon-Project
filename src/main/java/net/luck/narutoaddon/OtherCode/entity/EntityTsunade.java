
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
public class EntityTsunade extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 15;
   public static final int ENTITYID_RANGED = 16;

   public EntityTsunade(ElementsInfTsukAddon instance) {
      super(instance, 35);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "tsunade"), 15).name("tsunade").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, TsunadeRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class TsunadeRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/tsunade.png");

      public TsunadeRenderer(RenderManager renderManager) {
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
         if (attackState == 3) {
            GlStateManager.color(1.0F, 0.9F, 0.7F, 1.0F);
         } else if (attackState == 4) {
            GlStateManager.color(0.8F, 1.0F, 0.9F, 1.0F);
         } else if (attackState == 6) {
            GlStateManager.color(1.0F, 0.85F, 0.6F, 1.0F);
         } else if (attackState == 7) {
            GlStateManager.color(1.0F, 0.7F, 0.6F, 1.0F);
         }

      }
   }

   public static class EntityCustom extends EntityCreature implements INpcConfigurable {
      private static final DataParameter<String> NPC_CONFIG_ID;
      private static final DataParameter<Integer> ATTACK_STATE;
      private static final DataParameter<Boolean> IS_PASSIVE;
      public static final int STATE_IDLE = 0;
      public static final int STATE_MELEE = 1;
      public static final int STATE_FINGER_FLICK = 2;
      public static final int STATE_GROUND_PUNCH = 3;
      public static final int STATE_CHAKRA_PUNCH = 4;
      public static final int STATE_DASH = 5;
      public static final int STATE_UPPERCUT_COMBO = 6;
      public static final int STATE_FURY_COMBO = 7;
      private static final float MELEE_DMG = 16.0F;
      private static final float MELEE_TRUE = 6.0F;
      private static final float FINGER_FLICK_DMG = 24.0F;
      private static final float FINGER_FLICK_TRUE = 14.0F;
      private static final float GROUND_PUNCH_DMG = 22.0F;
      private static final float GROUND_PUNCH_TRUE = 12.0F;
      private static final float CHAKRA_PUNCH_DMG = 34.0F;
      private static final float CHAKRA_PUNCH_TRUE = 18.0F;
      private static final float DASH_DMG = 20.0F;
      private static final float DASH_TRUE = 10.0F;
      private static final float UPPERCUT_PUNCH_DMG = 16.0F;
      private static final float UPPERCUT_PUNCH_TRUE = 6.0F;
      private static final float UPPERCUT_KICK_DMG = 22.0F;
      private static final float UPPERCUT_KICK_TRUE = 12.0F;
      private static final float FURY_HIT1_DMG = 12.0F;
      private static final float FURY_HIT1_TRUE = 5.0F;
      private static final float FURY_HIT2_DMG = 12.0F;
      private static final float FURY_HIT2_TRUE = 5.0F;
      private static final float FURY_HIT3_DMG = 18.0F;
      private static final float FURY_HIT3_TRUE = 10.0F;
      private static final float FURY_HIT4_DMG = 26.0F;
      private static final float FURY_HIT4_TRUE = 14.0F;
      private static final double MELEE_RANGE = (double)2.5F;
      private static final double FINGER_FLICK_RANGE = (double)2.0F;
      private static final double GROUND_PUNCH_RANGE = (double)5.0F;
      private static final double CHAKRA_PUNCH_RANGE = (double)3.0F;
      private static final double DASH_MIN_RANGE = (double)5.0F;
      private static final double DASH_MAX_RANGE = (double)18.0F;
      private static final double UPPERCUT_RANGE = (double)2.5F;
      private static final double FURY_RANGE = (double)3.0F;
      private static final int[] CD_MELEE;
      private static final int[] CD_FINGER_FLICK;
      private static final int[] CD_GROUND_PUNCH;
      private static final int[] CD_CHAKRA_PUNCH;
      private static final int[] CD_DASH;
      private static final int[] CD_UPPERCUT_COMBO;
      private static final int[] CD_FURY_COMBO;
      private static final int SEARCH_RADIUS = 50;
      private static final double BASE_DEF = (double)1.25F;
      private static final double PLAYER_DEF_BONUS = 0.09;
      private static final double PLAYER_DEF_CAP = 0.36;
      private static final double PLAYER_DMG_BONUS = 0.12;
      private static final double PLAYER_DMG_CAP = 0.6;
      private static final int STAGGER_MIN = 15;
      private static final int STAGGER_MAX = 30;
      private static final float MELEE_STAGGER_CHANCE = 0.15F;
      private static final int TARGET_SWITCH_COOLDOWN = 120;
      private static final float TARGET_SWITCH_CHANCE = 0.4F;
      private static final UUID PARALYSIS_MODIFIER_UUID;
      private static final UUID HEAVINESS_SPEED_UUID;
      private boolean isPassive = false;
      private int meleeCooldown = 0;
      private int fingerFlickCooldown = 0;
      private int groundPunchCooldown = 0;
      private int chakraPunchCooldown = 0;
      private int dashCooldown = 0;
      private int uppercutComboCooldown = 0;
      private int furyComboCooldown = 0;
      private int attackAnimTimer = 0;
      private int staggerTimer = 0;
      private int comboStep = 0;
      private int comboTickTimer = 0;
      private EntityLivingBase comboTarget = null;
      private UUID heldPlayerUUID = null;
      private int airHoldTimer = 0;
      private int verticalLeapCooldown = 0;
      private int targetSwitchCooldown = 0;
      private UUID currentTargetUUID = null;
      private double prevPosX;
      private double prevPosZ;
      private int stuckTicks = 0;
      private int trapEscapeTick = 0;
      private EntityLivingBase dashLandingTarget = null;
      private int dashLandingTimer = 0;

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
      }

      protected void initEntityAI() {
         this.tasks.addTask(1, new EntityAITsunadeCombat(this));
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
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)22500.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.56);
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)6.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.9);
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)14.0F);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)40.0F);
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
         return (double)1.0F + Math.min((double)players * 0.12, 0.6);
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
               this.targetSwitchCooldown = 120;
            } else {
               float switchChance = 0.4F + (float)(validTargets.size() - 1) * 0.1F;
               switchChance = Math.min(switchChance, 0.75F);
               if (this.rand.nextFloat() < switchChance) {
                  List<EntityPlayer> otherTargets = new ArrayList();

                  for(EntityPlayer p : validTargets) {
                     if (this.currentTargetUUID == null || !p.getUniqueID().equals(this.currentTargetUUID)) {
                        otherTargets.add(p);
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

               this.targetSwitchCooldown = 120;
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

            if (this.fingerFlickCooldown > 0) {
               --this.fingerFlickCooldown;
            }

            if (this.groundPunchCooldown > 0) {
               --this.groundPunchCooldown;
            }

            if (this.chakraPunchCooldown > 0) {
               --this.chakraPunchCooldown;
            }

            if (this.dashCooldown > 0) {
               --this.dashCooldown;
            }

            if (this.uppercutComboCooldown > 0) {
               --this.uppercutComboCooldown;
            }

            if (this.furyComboCooldown > 0) {
               --this.furyComboCooldown;
            }

            if (this.targetSwitchCooldown > 0) {
               --this.targetSwitchCooldown;
            }

            if (this.staggerTimer > 0) {
               --this.staggerTimer;
            }

            if (this.verticalLeapCooldown > 0) {
               --this.verticalLeapCooldown;
            }

            if (this.attackAnimTimer > 0) {
               --this.attackAnimTimer;
               if (this.attackAnimTimer <= 0 && this.comboStep == 0) {
                  this.setAttackState(0);
               }
            }

            this.processDashLanding();
            this.processComboTicks();
            this.processAirHold();
            if (!this.isPassive && this.getAttackTarget() != null) {
               this.tryTargetSwitch();
            }

            if (this.isPassive && (Math.abs(this.motionX) > 0.1 || Math.abs(this.motionZ) > 0.1)) {
               this.motionX = (double)0.0F;
               this.motionZ = (double)0.0F;
               this.velocityChanged = true;
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

            if (!this.isPassive && this.onGround && this.verticalLeapCooldown <= 0) {
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

            if (!this.onGround && this.verticalLeapCooldown > 0) {
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
            this.meleeCooldown = this.randomCooldown(CD_MELEE);
            this.setAttackState(1);
            this.attackAnimTimer = 12;
            double dmgMult = this.getDamageMultiplier();
            float normDmg = (float)((double)16.0F * dmgMult);
            float trueDmg = (float)((double)6.0F * dmgMult);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            this.swingArm(EnumHand.MAIN_HAND);
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 8, 0.3, 0.3, 0.3, 0.2, new int[0]);
               ws.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + (double)1.0F, this.posZ, 4, 0.3, 0.2, 0.3, 0.2, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.8F, 1.0F);
            if (this.rand.nextFloat() < 0.15F) {
               this.staggerTimer = 15 + this.rand.nextInt(16);
            }

         }
      }

      public void fingerFlick(EntityLivingBase target) {
         if (this.fingerFlickCooldown <= 0) {
            this.fingerFlickCooldown = this.randomCooldown(CD_FINGER_FLICK);
            this.setAttackState(2);
            this.attackAnimTimer = 15;
            double dmgMult = this.getDamageMultiplier();
            float normDmg = (float)((double)24.0F * dmgMult);
            float trueDmg = (float)((double)14.0F * dmgMult);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            this.swingArm(EnumHand.MAIN_HAND);
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > (double)0.0F) {
               target.motionX = dx / d * (double)2.5F;
               target.motionY = 0.8;
               target.motionZ = dz / d * (double)2.5F;
            } else {
               target.motionX = (double)2.5F;
               target.motionY = 0.8;
               target.motionZ = (double)0.0F;
            }

            if (target instanceof EntityPlayerMP) {
               ((EntityPlayerMP)target).velocityChanged = true;
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 30, 0.6, 0.6, 0.6, (double)0.5F, new int[0]);
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, target.posX, target.posY + (double)1.0F, target.posZ, 5, 0.2, 0.2, 0.2, 0.05, new int[0]);
               double dx2 = target.posX - this.posX;
               double dz2 = target.posZ - this.posZ;
               double d2 = Math.sqrt(dx2 * dx2 + dz2 * dz2);
               if (d2 > (double)0.0F) {
                  for(int i = 1; i <= 4; ++i) {
                     ws.spawnParticle(EnumParticleTypes.CRIT, target.posX + dx2 / d2 * (double)i * (double)0.5F, target.posY + (double)1.0F, target.posZ + dz2 / d2 * (double)i * (double)0.5F, 5, 0.1, 0.1, 0.1, 0.1, new int[0]);
                  }
               }
            }

            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.6F);
            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.5F, 1.5F);
            this.staggerTimer = 15 + this.rand.nextInt(16);
         }
      }

      public void groundPunch(EntityLivingBase primaryTarget) {
         if (this.groundPunchCooldown <= 0) {
            this.groundPunchCooldown = this.randomCooldown(CD_GROUND_PUNCH);
            this.setAttackState(3);
            this.attackAnimTimer = 25;
            double dmgMult = this.getDamageMultiplier();
            float normDmg = (float)((double)22.0F * dmgMult);
            float trueDmg = (float)((double)12.0F * dmgMult);

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)5.0F))) {
               if (p.isEntityAlive() && !p.isSpectator()) {
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int i = 0; i < 40; ++i) {
                  double angle = 0.15707963267948966 * (double)i;
                  double radius = (double)1.0F + this.rand.nextDouble() * (double)4.0F;
                  double px = this.posX + Math.cos(angle) * radius;
                  double pz = this.posZ + Math.sin(angle) * radius;
                  ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, px, this.posY + 0.1, pz, 1, 0.1, 0.05, 0.1, 0.01, new int[0]);
               }

               for(int i = 0; i < 24; ++i) {
                  double angle = 0.2617993877991494 * (double)i;
                  double radius = (double)0.5F + this.rand.nextDouble() * (double)2.5F;
                  double px = this.posX + Math.cos(angle) * radius;
                  double pz = this.posZ + Math.sin(angle) * radius;
                  ws.spawnParticle(EnumParticleTypes.FLAME, px, this.posY + 0.2, pz, 1, 0.05, 0.08, 0.05, 0.01, new int[0]);
               }

               ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 3, (double)0.5F, 0.3, (double)0.5F, (double)0.0F, new int[0]);
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + 0.3, this.posZ, 15, (double)1.5F, (double)0.5F, (double)1.5F, 0.08, new int[0]);
            }

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)15.0F))) {
               if (p.isEntityAlive() && !p.isSpectator()) {
                  p.motionY += 0.15;
                  if (p instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)p).velocityChanged = true;
                  }
               }
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 0.7F);
            this.staggerTimer = 15 + this.rand.nextInt(16);
         }
      }

      public void chakraPunch(EntityLivingBase target) {
         if (this.chakraPunchCooldown <= 0) {
            this.chakraPunchCooldown = this.randomCooldown(CD_CHAKRA_PUNCH);
            this.setAttackState(4);
            this.attackAnimTimer = 15;
            double dmgMult = this.getDamageMultiplier();
            float normDmg = (float)((double)34.0F * dmgMult);
            float trueDmg = (float)((double)18.0F * dmgMult);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            this.swingArm(EnumHand.MAIN_HAND);
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > (double)0.0F) {
               target.motionX = dx / d * (double)1.0F;
               target.motionY = 0.4;
               target.motionZ = dz / d * (double)1.0F;
            }

            if (target instanceof EntityPlayerMP) {
               ((EntityPlayerMP)target).velocityChanged = true;
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 30, 0.6, 0.6, 0.6, 0.4, new int[0]);
               ws.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, target.posX, target.posY + (double)1.5F, target.posZ, 12, (double)0.5F, (double)0.5F, (double)0.5F, (double)0.5F, new int[0]);

               for(int i = 0; i < 8; ++i) {
                  double angle = (Math.PI / 4D) * (double)i;
                  ws.spawnParticle(EnumParticleTypes.FLAME, target.posX + Math.cos(angle) * 0.6, target.posY + (double)1.0F, target.posZ + Math.sin(angle) * 0.6, 2, 0.05, 0.05, 0.05, 0.02, new int[0]);
               }
            }

            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.2F, 0.8F);
            this.staggerTimer = 15 + this.rand.nextInt(16);
         }
      }

      public void dashLeap(EntityLivingBase target) {
         if (this.dashCooldown <= 0) {
            this.dashCooldown = this.randomCooldown(CD_DASH);
            this.setAttackState(5);
            this.attackAnimTimer = 18;
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < (double)1.0F) {
               dist = (double)1.0F;
            }

            double speed = Math.min(dist * 0.12, (double)2.0F);
            this.motionX = dx / dist * speed;
            this.motionY = 0.6;
            this.motionZ = dz / dist * speed;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY, this.posZ, 15, 0.8, 0.3, 0.8, 0.05, new int[0]);
            }

            this.dashLandingTarget = target;
            this.dashLandingTimer = 10;
         }
      }

      private void processDashLanding() {
         if (this.dashLandingTimer > 0) {
            --this.dashLandingTimer;
            if (this.dashLandingTimer <= 0 && this.dashLandingTarget != null) {
               double dmgMult = this.getDamageMultiplier();
               float normDmg = (float)((double)20.0F * dmgMult);
               float trueDmg = (float)((double)10.0F * dmgMult);

               for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)3.5F))) {
                  if (p.isEntityAlive() && !p.isSpectator()) {
                     p.hurtResistantTime = 0;
                     p.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                     p.hurtResistantTime = 0;
                     p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                     double kbx = p.posX - this.posX;
                     double kbz = p.posZ - this.posZ;
                     double kd = Math.sqrt(kbx * kbx + kbz * kbz);
                     if (kd > (double)0.0F) {
                        p.motionX += kbx / kd * 0.8;
                        p.motionY += 0.3;
                        p.motionZ += kbz / kd * 0.8;
                     }

                     if (p instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)p).velocityChanged = true;
                     }
                  }
               }

               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + 0.2, this.posZ, 2, 0.3, 0.1, 0.3, (double)0.0F, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + 0.1, this.posZ, 20, (double)1.5F, 0.2, (double)1.5F, 0.02, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0F, 0.9F);
               this.dashLandingTarget = null;
            }
         }

      }

      public void uppercutCombo(EntityLivingBase target) {
         if (this.uppercutComboCooldown <= 0) {
            this.uppercutComboCooldown = this.randomCooldown(CD_UPPERCUT_COMBO);
            this.setAttackState(6);
            this.attackAnimTimer = 30;
            double dmgMult = this.getDamageMultiplier();
            float normDmg = (float)((double)16.0F * dmgMult);
            float trueDmg = (float)((double)6.0F * dmgMult);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            this.swingArm(EnumHand.MAIN_HAND);
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 15, 0.3, 0.3, 0.3, 0.4, new int[0]);
               ws.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX, this.posY + 1.2, this.posZ, 8, 0.4, 0.3, 0.4, 0.3, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 1.0F, 0.9F);
            this.comboStep = 1;
            this.comboTickTimer = 8;
            this.comboTarget = target;
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
                  int currentState = this.getAttackState();
                  if (currentState == 6 && this.comboStep == 1) {
                     double dist = (double)this.getDistance(this.comboTarget);
                     if (dist > (double)4.0F) {
                        this.comboStep = 0;
                        this.comboTarget = null;
                     } else {
                        double dmgMult = this.getDamageMultiplier();
                        float normDmg = (float)((double)22.0F * dmgMult);
                        float trueDmg = (float)((double)12.0F * dmgMult);
                        this.comboTarget.hurtResistantTime = 0;
                        this.comboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                        this.comboTarget.hurtResistantTime = 0;
                        this.comboTarget.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                        this.swingArm(EnumHand.OFF_HAND);
                        this.comboTarget.motionX = (double)0.0F;
                        this.comboTarget.motionY = 1.2;
                        this.comboTarget.motionZ = (double)0.0F;
                        if (this.comboTarget instanceof EntityPlayerMP) {
                           ((EntityPlayerMP)this.comboTarget).velocityChanged = true;
                        }

                        if (this.world instanceof WorldServer) {
                           WorldServer ws = (WorldServer)this.world;

                           for(int i = 0; i < 12; ++i) {
                              double angle = (Math.PI / 6D) * (double)i;
                              double px = this.comboTarget.posX + Math.cos(angle) * (double)0.5F;
                              double pz = this.comboTarget.posZ + Math.sin(angle) * (double)0.5F;
                              ws.spawnParticle(EnumParticleTypes.FLAME, px, this.comboTarget.posY + (double)0.5F + (double)i * 0.15, pz, 1, 0.05, 0.1, 0.05, 0.02, new int[0]);
                           }

                           ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.comboTarget.posX, this.comboTarget.posY + (double)1.0F, this.comboTarget.posZ, 25, 0.4, (double)0.5F, 0.4, (double)0.5F, new int[0]);
                        }

                        this.world.playSound((EntityPlayer)null, this.comboTarget.posX, this.comboTarget.posY, this.comboTarget.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 1.2F);
                        if (this.comboTarget instanceof EntityPlayer) {
                           this.heldPlayerUUID = this.comboTarget.getUniqueID();
                           this.airHoldTimer = 5;
                        }

                        this.comboStep = 0;
                        this.comboTarget = null;
                        this.staggerTimer = 15 + this.rand.nextInt(16);
                     }
                  } else {
                     if (currentState == 7) {
                        double dist = (double)this.getDistance(this.comboTarget);
                        if (dist > (double)5.0F) {
                           this.comboStep = 0;
                           this.comboTarget = null;
                           this.setAttackState(0);
                           return;
                        }

                        double dmgMult = this.getDamageMultiplier();
                        if (this.comboStep == 2) {
                           float normDmg = (float)((double)12.0F * dmgMult);
                           float trueDmg = (float)((double)5.0F * dmgMult);
                           this.comboTarget.hurtResistantTime = 0;
                           this.comboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                           this.comboTarget.hurtResistantTime = 0;
                           this.comboTarget.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                           this.swingArm(EnumHand.OFF_HAND);
                           if (this.world instanceof WorldServer) {
                              WorldServer ws = (WorldServer)this.world;
                              ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.comboTarget.posX, this.comboTarget.posY + (double)1.0F, this.comboTarget.posZ, 12, 0.3, 0.3, 0.3, 0.3, new int[0]);
                              ws.spawnParticle(EnumParticleTypes.FLAME, this.comboTarget.posX, this.comboTarget.posY + 0.8, this.comboTarget.posZ, 5, 0.2, 0.2, 0.2, 0.05, new int[0]);
                           }

                           this.world.playSound((EntityPlayer)null, this.comboTarget.posX, this.comboTarget.posY, this.comboTarget.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 1.0F, 1.1F);
                           this.comboStep = 3;
                           this.comboTickTimer = 7;
                        } else if (this.comboStep == 3) {
                           float normDmg = (float)((double)18.0F * dmgMult);
                           float trueDmg = (float)((double)10.0F * dmgMult);
                           this.comboTarget.hurtResistantTime = 0;
                           this.comboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                           this.comboTarget.hurtResistantTime = 0;
                           this.comboTarget.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                           this.comboTarget.motionY = (double)0.5F;
                           if (this.comboTarget instanceof EntityPlayerMP) {
                              ((EntityPlayerMP)this.comboTarget).velocityChanged = true;
                           }

                           this.swingArm(EnumHand.MAIN_HAND);
                           if (this.world instanceof WorldServer) {
                              WorldServer ws = (WorldServer)this.world;
                              ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.comboTarget.posX, this.comboTarget.posY + (double)1.0F, this.comboTarget.posZ, 20, 0.4, (double)0.5F, 0.4, 0.4, new int[0]);
                              ws.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.comboTarget.posX, this.comboTarget.posY + (double)1.5F, this.comboTarget.posZ, 10, (double)0.5F, (double)0.5F, (double)0.5F, (double)0.5F, new int[0]);

                              for(int i = 0; i < 6; ++i) {
                                 ws.spawnParticle(EnumParticleTypes.FLAME, this.comboTarget.posX, this.comboTarget.posY + 0.3 * (double)i, this.comboTarget.posZ, 2, 0.15, 0.05, 0.15, 0.02, new int[0]);
                              }
                           }

                           this.world.playSound((EntityPlayer)null, this.comboTarget.posX, this.comboTarget.posY, this.comboTarget.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.2F, 1.3F);
                           this.comboStep = 4;
                           this.comboTickTimer = 8;
                        } else if (this.comboStep == 4) {
                           float normDmg = (float)((double)26.0F * dmgMult);
                           float trueDmg = (float)((double)14.0F * dmgMult);
                           this.comboTarget.hurtResistantTime = 0;
                           this.comboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                           this.comboTarget.hurtResistantTime = 0;
                           this.comboTarget.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                           double kdx = this.comboTarget.posX - this.posX;
                           double kdz = this.comboTarget.posZ - this.posZ;
                           double kd = Math.sqrt(kdx * kdx + kdz * kdz);
                           if (kd > (double)0.0F) {
                              this.comboTarget.motionX = kdx / kd * 1.8;
                              this.comboTarget.motionZ = kdz / kd * 1.8;
                           }

                           this.comboTarget.motionY = -0.3;
                           if (this.comboTarget instanceof EntityPlayerMP) {
                              ((EntityPlayerMP)this.comboTarget).velocityChanged = true;
                           }

                           this.swingArm(EnumHand.MAIN_HAND);
                           if (this.world instanceof WorldServer) {
                              WorldServer ws = (WorldServer)this.world;
                              ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.comboTarget.posX, this.comboTarget.posY + (double)0.5F, this.comboTarget.posZ, 2, 0.3, 0.2, 0.3, (double)0.0F, new int[0]);

                              for(int i = 0; i < 20; ++i) {
                                 double angle = (Math.PI / 10D) * (double)i;
                                 double px = this.comboTarget.posX + Math.cos(angle) * (double)1.5F;
                                 double pz = this.comboTarget.posZ + Math.sin(angle) * (double)1.5F;
                                 ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, px, this.comboTarget.posY + 0.1, pz, 1, 0.05, 0.02, 0.05, 0.01, new int[0]);
                              }

                              ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.comboTarget.posX, this.comboTarget.posY + (double)0.5F, this.comboTarget.posZ, 30, 0.6, 0.4, 0.6, (double)0.5F, new int[0]);
                           }

                           this.world.playSound((EntityPlayer)null, this.comboTarget.posX, this.comboTarget.posY, this.comboTarget.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.2F, 1.1F);
                           this.comboStep = 0;
                           this.comboTarget = null;
                           this.staggerTimer = 15 + this.rand.nextInt(16);
                        }
                     }

                  }
               }
            }
         }
      }

      private void processAirHold() {
         if (this.heldPlayerUUID != null && this.airHoldTimer > 0) {
            EntityPlayer heldPlayer = this.world.getPlayerEntityByUUID(this.heldPlayerUUID);
            if (heldPlayer != null && heldPlayer.isEntityAlive()) {
               heldPlayer.motionY = 0.01;
               heldPlayer.motionX *= 0.1;
               heldPlayer.motionZ *= 0.1;
               heldPlayer.fallDistance = 0.0F;
               if (heldPlayer instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)heldPlayer).velocityChanged = true;
               }

               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  double angle = 1.2566370614359172 * (double)(5 - this.airHoldTimer);
                  double px = heldPlayer.posX + Math.cos(angle) * 0.8;
                  double pz = heldPlayer.posZ + Math.sin(angle) * 0.8;
                  ws.spawnParticle(EnumParticleTypes.FLAME, px, heldPlayer.posY + (double)1.0F, pz, 3, 0.1, 0.1, 0.1, 0.02, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, heldPlayer.posX, heldPlayer.posY + (double)0.5F, heldPlayer.posZ, 5, 0.3, (double)0.5F, 0.3, 0.2, new int[0]);
               }

               --this.airHoldTimer;
               if (this.airHoldTimer <= 0) {
                  this.heldPlayerUUID = null;
               }

            } else {
               this.heldPlayerUUID = null;
               this.airHoldTimer = 0;
            }
         }
      }

      public void furyCombo(EntityLivingBase target) {
         if (this.furyComboCooldown <= 0) {
            this.furyComboCooldown = this.randomCooldown(CD_FURY_COMBO);
            this.setAttackState(7);
            this.attackAnimTimer = 40;
            double dmgMult = this.getDamageMultiplier();
            float normDmg = (float)((double)12.0F * dmgMult);
            float trueDmg = (float)((double)5.0F * dmgMult);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            this.swingArm(EnumHand.MAIN_HAND);
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 10, 0.3, 0.3, 0.3, 0.3, new int[0]);
               ws.spawnParticle(EnumParticleTypes.FLAME, target.posX, target.posY + 0.8, target.posZ, 3, 0.15, 0.15, 0.15, 0.03, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 1.0F, 1.0F);
            this.comboStep = 2;
            this.comboTickTimer = 7;
            this.comboTarget = target;
         }
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         if (this.isPassive) {
            return false;
         } else {
            double defMult = this.getDefenseMultiplier();
            amount = (float)((double)amount / defMult);
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
         compound.setInteger("meleeCooldown", this.meleeCooldown);
         compound.setInteger("fingerFlickCooldown", this.fingerFlickCooldown);
         compound.setInteger("groundPunchCooldown", this.groundPunchCooldown);
         compound.setInteger("chakraPunchCooldown", this.chakraPunchCooldown);
         compound.setInteger("dashCooldown", this.dashCooldown);
         compound.setInteger("uppercutComboCooldown", this.uppercutComboCooldown);
         compound.setInteger("furyComboCooldown", this.furyComboCooldown);
         compound.setInteger("staggerTimer", this.staggerTimer);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.isPassive = compound.getBoolean("isPassive");
         this.dataManager.set(IS_PASSIVE, this.isPassive);
         String configId = compound.getString("npcConfigId");
         if (configId != null && !configId.isEmpty()) {
            this.dataManager.set(NPC_CONFIG_ID, configId);
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null) {
               this.applyNpcConfig(config);
            }
         }

         this.meleeCooldown = compound.getInteger("meleeCooldown");
         this.fingerFlickCooldown = compound.getInteger("fingerFlickCooldown");
         this.groundPunchCooldown = compound.getInteger("groundPunchCooldown");
         this.chakraPunchCooldown = compound.getInteger("chakraPunchCooldown");
         this.dashCooldown = compound.getInteger("dashCooldown");
         this.uppercutComboCooldown = compound.getInteger("uppercutComboCooldown");
         this.furyComboCooldown = compound.getInteger("furyComboCooldown");
         this.staggerTimer = compound.getInteger("staggerTimer");
      }

      static {
         NPC_CONFIG_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
         ATTACK_STATE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         IS_PASSIVE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
         CD_MELEE = new int[]{12, 20};
         CD_FINGER_FLICK = new int[]{50, 80};
         CD_GROUND_PUNCH = new int[]{70, 110};
         CD_CHAKRA_PUNCH = new int[]{35, 60};
         CD_DASH = new int[]{50, 90};
         CD_UPPERCUT_COMBO = new int[]{60, 100};
         CD_FURY_COMBO = new int[]{90, 140};
         PARALYSIS_MODIFIER_UUID = UUID.fromString("c69af92a-b96d-49b7-a396-9b3b0d77edd5");
         HEAVINESS_SPEED_UUID = UUID.fromString("7d735ff6-8872-482d-ac1f-cd2249e8f584");
      }
   }

   public static class EntityAITsunadeCombat extends EntityAIBase {
      private final EntityCustom tsunade;
      private EntityLivingBase target;

      public EntityAITsunadeCombat(EntityCustom tsunade) {
         this.tsunade = tsunade;
         this.setMutexBits(3);
      }

      public boolean shouldExecute() {
         EntityLivingBase t = this.tsunade.getAttackTarget();
         if (t != null && t.isEntityAlive()) {
            this.target = t;
            return true;
         } else {
            return false;
         }
      }

      public boolean shouldContinueExecuting() {
         return this.target != null && this.target.isEntityAlive() && this.tsunade.isEntityAlive();
      }

      public void resetTask() {
         this.target = null;
         this.tsunade.setAttackState(0);
      }

      public void updateTask() {
         if (this.target != null && this.target.isEntityAlive()) {
            if (this.tsunade.getAttackTarget() != null && this.tsunade.getAttackTarget() != this.target) {
               this.target = this.tsunade.getAttackTarget();
            }

            double dist = (double)this.tsunade.getDistance(this.target);
            this.tsunade.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
            if (this.tsunade.comboStep > 0) {
               this.tsunade.getNavigator().clearPath();
               this.tsunade.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
            } else if (this.tsunade.staggerTimer > 0) {
               this.tsunade.getNavigator().clearPath();
            } else {
               if (dist > (double)12.0F) {
                  this.tsunade.getNavigator().tryMoveToEntityLiving(this.target, 1.2);
               } else if (dist > (double)4.0F) {
                  this.tsunade.getNavigator().tryMoveToEntityLiving(this.target, (double)1.0F);
               } else {
                  this.tsunade.getNavigator().clearPath();
               }

               int nearbyPlayerCount = this.countPlayersInRange((double)5.0F);
               if (dist >= (double)5.0F && dist <= (double)18.0F && this.tsunade.dashCooldown <= 0 && this.tsunade.dashLandingTimer <= 0) {
                  this.tsunade.dashLeap(this.target);
               } else if (nearbyPlayerCount >= 2 && dist <= (double)5.0F && this.tsunade.groundPunchCooldown <= 0) {
                  this.tsunade.groundPunch(this.target);
               } else if (dist <= (double)3.0F && this.tsunade.furyComboCooldown <= 0) {
                  this.tsunade.furyCombo(this.target);
               } else if (dist <= (double)2.5F && this.tsunade.uppercutComboCooldown <= 0) {
                  this.tsunade.uppercutCombo(this.target);
               } else if (dist >= (double)2.0F && dist <= (double)4.0F && this.tsunade.fingerFlickCooldown <= 0) {
                  this.tsunade.fingerFlick(this.target);
               } else if (dist <= (double)3.0F && this.tsunade.chakraPunchCooldown <= 0) {
                  this.tsunade.chakraPunch(this.target);
               } else if (dist <= (double)5.0F && this.tsunade.groundPunchCooldown <= 0) {
                  this.tsunade.groundPunch(this.target);
               } else {
                  if (dist <= (double)2.5F && this.tsunade.meleeCooldown <= 0) {
                     this.tsunade.basicMelee(this.target);
                  }

               }
            }
         }
      }

      private int countPlayersInRange(double range) {
         List<EntityPlayer> players = this.tsunade.world.getEntitiesWithinAABB(EntityPlayer.class, this.tsunade.getEntityBoundingBox().grow(range));
         int count = 0;

         for(EntityPlayer p : players) {
            if (p.isEntityAlive() && !p.isSpectator()) {
               ++count;
            }
         }

         return count;
      }
   }
}
