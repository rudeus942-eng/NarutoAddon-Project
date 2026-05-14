
package net.luck.narutoaddon.OtherCode.entity.base;

import net.luck.narutoaddon.OtherCode.entity.*;
import net.luck.narutoaddon.OtherCode.jutsu.EntityDotonEarthSpear;
import net.luck.narutoaddon.OtherCode.jutsu.EntityWeightedBoulder;
import net.luck.narutoaddon.OtherCode.quest.core.QuestInstance;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcInteractionHelper;
import net.luck.narutoaddon.OtherCode.quest.npc.QuestNpcTargetingAI;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
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
import net.minecraft.scoreboard.Team;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;

public abstract class QuestNpcBase extends EntityCreature implements INpcConfigurable {
   private static final double GLOBAL_DAMAGE_NERF = 0.8;
   private static final Set<String> PRE_CHUNIN_EXEMPT_IDS = new HashSet(Arrays.asList("gozu_demon", "haku_bridge", "kakashi_bell_test", "meizu_demon", "zabuza_bridge", "zabuza_first"));
   private static final DataParameter<String> NPC_CONFIG_ID;
   private static final DataParameter<String> NPC_POSE;
   private static final DataParameter<String> TEXTURE_OVERRIDE;
   private static final UUID PARALYSIS_MODIFIER_UUID;
   private static final UUID HEAVINESS_SPEED_UUID;
   private static final double BASE_DEF = 1.08;
   private static final double PLAYER_DAMAGE_BONUS = 0.05;
   private static final double PLAYER_DAMAGE_BONUS_CAP = 0.3;
   private static final double PLAYER_COOLDOWN_REDUCTION = 0.04;
   private static final double PLAYER_COOLDOWN_REDUCTION_CAP = 0.2;
   private int noPlayerTicks = 0;
   private static final int DESPAWN_NO_PLAYER_TICKS = 1200;
   protected double spawnOriginX = Double.NaN;
   protected double spawnOriginY = Double.NaN;
   protected double spawnOriginZ = Double.NaN;
   private int leashReturnCooldown = 0;
   private static final double LEASH_SOFT = (double)50.0F;
   private static final double LEASH_HARD = (double)75.0F;
   private static final double LEASH_Y_SOFT = (double)15.0F;
   private static final double LEASH_Y_HARD = (double)25.0F;
   protected boolean isTerritoryDefender = false;
   protected boolean isPassive = false;
   protected boolean questRetreating = false;
   protected int questRetreatingTicks = 0;
   private static final int RETREAT_SAFETY_TIMEOUT = 200;
   protected int consecutiveInvulnerableTicks = 0;
   private static final int INVULNERABLE_SAFETY_TIMEOUT = 300;
   private int safetyResetCount = 0;
   protected int combatTier = 0;
   protected float trueDamageMultiplier = 1.0F;
   protected float trueDamageSplit = 0.0F;
   protected boolean hasRangedAttack = false;
   protected int natureType = 0;
   protected String weaponItemId = null;
   protected String offhandItemId = null;
   protected String helmetItemId = null;
   protected String chestplateItemId = null;
   protected String leggingsItemId = null;
   protected float jutsuPower = 0.0F;
   protected float kunaiSpeed = 1.6F;
   protected float kunaiInaccuracy = 2.0F;
   protected String combatStyle = "STANDARD";
   protected String teamRole = null;
   protected int themeColor = 0;
   protected int accentColor = 0;
   protected float ghostAlpha = 1.0F;
   protected float rageThreshold = 0.0F;
   protected float rageDamageMultiplier = 1.0F;
   protected float rageSpeedMultiplier = 1.0F;
   protected boolean rageTriggered = false;
   protected float configCooldownMultiplier = 1.0F;
   protected float configDamageMultiplier = 1.0F;
   protected double configLeashRange = (double)0.0F;
   protected double configLeashRangeX = (double)0.0F;
   protected int meleeCooldown = 0;
   protected int kunaiCooldown = 0;
   protected int dashCooldown = 0;
   protected int jutsuCooldown = 0;
   protected int verticalLeapCooldown = 0;
   protected int substitutionCooldown = 0;
   protected static final int SUBSTITUTION_COOLDOWN_MAX = 600;
   protected int chatCooldown = 0;
   protected int attackStaggerDelay = 0;
   protected int horizontalLeapCooldown = 0;
   protected boolean hasHeadhunter = false;
   protected int headhunterCooldown = 0;
   protected boolean useIceNeedles = false;
   protected boolean hasIceDomePhase = false;
   protected float iceDomeHealthThreshold = 0.3F;
   protected boolean iceDomeTriggered = false;
   protected Entity activeDome = null;
   protected boolean hasWaterDragon = false;
   protected float waterDragonPower = 1.5F;
   protected int waterDragonCooldown = 0;
   protected boolean hasSwordCombo = false;
   protected boolean hasHiddenMist = false;
   protected float hiddenMistThreshold = 0.6F;
   protected boolean hiddenMistTriggered = false;
   protected boolean hasWaterPrison = false;
   protected int waterPrisonCooldown = 0;
   protected int spawnDelayRemaining = 0;
   protected boolean spawnDelayActive = false;
   protected int switchRetreatTicks = 0;
   protected int targetSwitchCooldown = 0;
   protected static final int TARGET_SWITCH_INTERVAL = 120;
   protected static final int DASH_NONE = 0;
   protected static final int DASH_LUNGE = 1;
   protected static final int DASH_STRIKE = 2;
   protected static final int DASH_LEAP = 3;
   protected static final int DASH_POWER_LEAP = 4;
   protected boolean isDashing = false;
   protected int dashTicksRemaining = 0;
   protected double dashVelX;
   protected double dashVelZ;
   protected int dashType = 0;
   protected int dashDuration = 0;
   protected float dashArcSustain = 0.0F;
   protected float dashFallAccel = 0.0F;
   protected float dashMaxFall = 0.0F;
   protected boolean dashHasMidairGuidance = false;
   protected boolean isLeaping = false;
   protected int failedLeapAttempts = 0;
   protected int leapSuccessCheckTicks = 0;
   protected EntityLivingBase lastAttackTarget = null;
   protected int targetElevatedTicks = 0;
   protected int trapEscapeTick = 0;
   protected int stuckTicks = 0;
   protected double prevPosX;
   protected double prevPosZ;
   protected boolean internalReposition = false;
   private int iceDomeScanCooldown = 0;
   private EntityLivingBase cachedIceDome = null;
   private int woodTrapScanCooldown = 0;
   private boolean cachedWoodTrapped = false;
   protected int jutsuWindup = 0;
   protected Vec3d jutsuTargetVec = null;
   protected int strafeTicksRemaining = 0;
   protected int strafeDirection = 1;
   protected int strafeCooldown = 0;
   protected int repositionCooldown = 0;

   public void setLeashRange(double range) {
      this.configLeashRange = range;
   }

   public void setLeashRangeX(double range) {
      this.configLeashRangeX = range;
   }

   public QuestNpcBase(World world) {
      super(world);
      this.setSize(0.6F, 1.8F);
      this.experienceValue = 0;
      this.isImmuneToFire = false;
      this.setNoAI(true);
      this.enablePersistence();
   }

   protected abstract void processCombat(EntityLivingBase var1, double var2);

   protected abstract void tickStyleCooldowns();

   protected abstract void resetCombatState();

   protected abstract void writeCombatNBT(NBTTagCompound var1);

   protected abstract void readCombatNBT(NBTTagCompound var1);

   protected void onCombatDeath() {
   }

   protected float onStyleDamage(DamageSource source, float amount) {
      return amount;
   }

   protected boolean usesVanillaMeleeAI() {
      return true;
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(NPC_CONFIG_ID, "");
      this.dataManager.register(NPC_POSE, "STANDING");
      this.dataManager.register(TEXTURE_OVERRIDE, "");
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)0.0F);
      }

      if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.0F);
      }

      if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)20.0F);
      }

      if (this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) != null) {
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
      }

      if (this.getAttributeMap().getAttributeInstanceByName("generic.attackDamage") == null) {
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
      }

      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)3.0F);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)48.0F);
   }

   protected void initEntityAI() {
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

   protected boolean isIceDome(EntityLivingBase target) {
      if (target == null) {
         return false;
      } else {
         String cn = target.getClass().getName().toLowerCase();
         return cn.contains("icedome") || cn.contains("shieldbase");
      }
   }

   protected EntityLivingBase findNearbyIceDome(double range) {
      if (this.iceDomeScanCooldown <= 0) {
         this.iceDomeScanCooldown = 10;
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

         this.cachedIceDome = nearest;
         return nearest;
      } else {
         if (this.cachedIceDome != null && (!this.cachedIceDome.isEntityAlive() || this.cachedIceDome.isDead)) {
            this.cachedIceDome = null;
         }

         return this.cachedIceDome;
      }
   }

   public void setAttackTarget(@Nullable EntityLivingBase target) {
      if (target != null && !(target instanceof EntityPlayer) && !this.isIceDome(target)) {
         EntityPlayer owner = this.resolveEntityOwner(target);
         if (owner == null) {
            return;
         }

         target = owner;
      }

      if (!(target instanceof EntityPlayer) || !this.isTerritoryDefender || !this.isDefenderFriendly((EntityPlayer)target)) {
         if (!(target instanceof EntityPlayer) || !this.getEntityData().getBoolean("territoryDefender") || !this.isDefenderFriendly((EntityPlayer)target)) {
            super.setAttackTarget(target);
         }
      }
   }

   @Nullable
   protected EntityPlayer resolveEntityOwner(Entity entity) {
      if (entity == null) {
         return null;
      } else if (entity instanceof EntityPlayer) {
         return (EntityPlayer)entity;
      } else {
         if (entity instanceof IEntityOwnable) {
            Entity owner = ((IEntityOwnable)entity).getOwner();
            if (owner instanceof EntityPlayer) {
               return (EntityPlayer)owner;
            }
         }

         for(Entity passenger : entity.getPassengers()) {
            if (passenger instanceof EntityPlayer) {
               return (EntityPlayer)passenger;
            }
         }

         Entity riding = entity.getRidingEntity();
         if (riding instanceof EntityPlayer) {
            return (EntityPlayer)riding;
         } else {
            NBTTagCompound data = entity.getEntityData();
            String[] ownerKeys = new String[]{"OwnerUUID", "ownerUUID", "SummonerUUID", "summonerUUID"};

            for(String key : ownerKeys) {
               if (data.hasKey(key)) {
                  try {
                     UUID ownerUUID = UUID.fromString(data.getString(key));
                     EntityPlayer owner = entity.world.getPlayerEntityByUUID(ownerUUID);
                     if (owner != null) {
                        return owner;
                     }
                  } catch (IllegalArgumentException var12) {
                  }
               }
            }

            try {
               Method getOwner = entity.getClass().getMethod("getOwner");
               Object result = getOwner.invoke(entity);
               if (result instanceof EntityPlayer) {
                  return (EntityPlayer)result;
               }
            } catch (Exception var11) {
            }

            return null;
         }
      }
   }

   private boolean isDefenderFriendly(EntityPlayer player) {
      String defVillage = this.getEntityData().getString("defenderVillage");
      if (defVillage != null && !defVillage.isEmpty()) {
         Team team = player.getTeam();
         if (team != null) {
            String teamName = team.getName().toLowerCase();
            String defLower = defVillage.toLowerCase();
            if (teamName.equals(defLower) || teamName.contains(defLower)) {
               return true;
            }
         }
      }

      return false;
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

   public void setRetreating(boolean retreating) {
      this.questRetreating = retreating;
      this.questRetreatingTicks = 0;
      if (retreating) {
         this.setAttackTarget((EntityLivingBase)null);
      }

   }

   public boolean isRetreating() {
      return this.questRetreating;
   }

   protected void onForceResetCombatState() {
      this.hurtResistantTime = 0;
      this.spawnDelayActive = false;
      this.questRetreating = false;
      this.questRetreatingTicks = 0;
      this.isDashing = false;
      this.isLeaping = false;
      if (this.getEntityData().getBoolean("noAI_stuck")) {
         this.setNoAI(false);
         this.getEntityData().removeTag("noAI_stuck");
      }

   }

   protected void tickRetreatSafety() {
      if (this.questRetreating) {
         ++this.questRetreatingTicks;
         if (this.questRetreatingTicks >= 200) {
            System.out.println("[QuestNPC] Safety timeout: clearing questRetreating on " + this.getName() + " after " + 200 + " ticks");
            this.questRetreating = false;
            this.questRetreatingTicks = 0;
         }
      }

   }

   public void setCombatTier(int tier) {
      this.combatTier = Math.max(0, Math.min(tier, 4));
   }

   public void setTrueDamageMultiplier(float mult) {
      this.trueDamageMultiplier = Math.max(0.1F, mult);
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

   protected void purgeMovementDebuffs() {
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

   protected boolean isTrappedByWoodJutsu() {
      if (this.woodTrapScanCooldown > 0) {
         return this.cachedWoodTrapped;
      } else {
         this.woodTrapScanCooldown = 10;
         this.cachedWoodTrapped = false;

         for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)12.0F))) {
            String cn = e.getClass().getName().toLowerCase();
            if (cn.contains("woodburial") || cn.contains("woodprison") || cn.contains("woodforest") || cn.contains("mokuton") || cn.contains("woodsegment")) {
               this.cachedWoodTrapped = true;
               return true;
            }
         }

         return false;
      }
   }

   public void setPositionAndUpdate(double x, double y, double z) {
      if (this.world.isRemote || this.isPassive || this.internalReposition || !this.isTrappedByWoodJutsu()) {
         super.setPositionAndUpdate(x, y, z);
      }
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
      if (this.isPassive) {
         return false;
      } else if (this.questRetreating) {
         return false;
      } else if (this.spawnDelayActive) {
         return false;
      } else {
         Entity trueSource = source.getTrueSource();
         if (trueSource instanceof QuestNpcBase) {
            return false;
         } else {
            if (trueSource != null && !(trueSource instanceof EntityPlayer) && !(trueSource instanceof QuestNpcBase)) {
               EntityPlayer puppetOwner = this.resolveEntityOwner(trueSource);
               if (puppetOwner != null) {
                  if (this.getAttackTarget() == null || !this.getAttackTarget().isEntityAlive()) {
                     this.setAttackTarget(puppetOwner);
                  }

                  this.setRevengeTarget(puppetOwner);
               }
            }

            if ((this.isTerritoryDefender || this.getEntityData().getBoolean("territoryDefender")) && trueSource instanceof EntityPlayer && this.isDefenderFriendly((EntityPlayer)trueSource)) {
               return false;
            } else if (source == DamageSource.IN_WALL && this.isTrappedByWoodJutsu()) {
               return false;
            } else {
               Entity immediate = source.getImmediateSource();
               if (trueSource != null) {
                  String cn = trueSource.getClass().getName().toLowerCase();
                  if (cn.contains("woodburial") || cn.contains("woodprison") || cn.contains("woodforest") || cn.contains("mokuton") || cn.contains("woodsegment")) {
                     return false;
                  }
               }

               if (immediate != null) {
                  String cn = immediate.getClass().getName().toLowerCase();
                  if (cn.contains("woodburial") || cn.contains("woodprison") || cn.contains("woodforest") || cn.contains("mokuton") || cn.contains("woodsegment")) {
                     return false;
                  }
               }

               if (source.getDamageType() != null && source.getDamageType().equals("ninjutsu")) {
                  Entity src = source.getImmediateSource();
                  if (src != null) {
                     String cn = src.getClass().getName().toLowerCase();
                     if (cn.contains("wood") || cn.contains("mokuton")) {
                        return false;
                     }
                  }
               }

               if (!this.world.isRemote && this.combatTier >= 4 && this.substitutionCooldown <= 0 && trueSource instanceof EntityLivingBase) {
                  float healthPercent = amount / this.getMaxHealth();
                  if (healthPercent > 0.15F && this.rand.nextFloat() < 0.25F) {
                     this.performSubstitution((EntityLivingBase)trueSource);
                     return false;
                  }
               }

               if (this.hurtResistantTime > 0) {
                  return false;
               } else {
                  float styledAmount = this.onStyleDamage(source, amount);
                  if (styledAmount < 0.0F) {
                     return false;
                  } else {
                     double defMul = this.getDefenseMultiplier();
                     amount = (float)((double)styledAmount / defMul);
                     boolean result = super.attackEntityFrom(source, amount);
                     if (result) {
                        this.consecutiveInvulnerableTicks = 0;
                        this.safetyResetCount = 0;
                     }

                     if (result) {
                        this.hurtResistantTime = Math.max(this.hurtResistantTime, 10);
                     }

                     return result;
                  }
               }
            }
         }
      }
   }

   private void performSubstitution(EntityLivingBase attacker) {
      if (this.world instanceof WorldServer) {
         ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)0.5F, 0.8, (double)0.5F, 0.02, new int[0]);
         ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 15, 0.4, 0.6, 0.4, 0.02, new int[0]);
      }

      try {
         Entity log = EntityList.createEntityByIDFromName(new ResourceLocation("inftsukaddon", "sublog"), this.world);
         if (log != null) {
            log.setPosition(this.posX, this.posY, this.posZ);
            this.world.spawnEntity(log);
         }
      } catch (Exception e) {
         System.err.println("[InfTsuk] Failed to spawn SubLog: " + e.getMessage());
      }

      double behindDist = (double)6.0F + this.rand.nextDouble() * (double)2.0F;
      float attackerYaw = attacker.rotationYaw * ((float)Math.PI / 180F);
      double teleX = attacker.posX + Math.sin((double)attackerYaw) * behindDist;
      double teleZ = attacker.posZ - Math.cos((double)attackerYaw) * behindDist;
      double teleY = attacker.posY;
      if (this.world.isBlockLoaded(new BlockPos(teleX, teleY, teleZ)) && this.isPositionSafe(teleX, teleY, teleZ) && !this.isEnclosed(teleX, teleY, teleZ)) {
         this.internalReposition = true;
         this.setPositionAndUpdate(teleX, teleY, teleZ);
         this.internalReposition = false;
      } else {
         double sideX = attacker.posX + Math.cos((double)attackerYaw) * (double)5.0F;
         double sideZ = attacker.posZ + Math.sin((double)attackerYaw) * (double)5.0F;
         if (this.world.isBlockLoaded(new BlockPos(sideX, teleY, sideZ)) && this.isPositionSafe(sideX, teleY, sideZ) && !this.isEnclosed(sideX, teleY, sideZ)) {
            this.internalReposition = true;
            this.setPositionAndUpdate(sideX, teleY, sideZ);
            this.internalReposition = false;
         }
      }

      if (this.world instanceof WorldServer) {
         ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 10, 0.3, (double)0.5F, 0.3, 0.02, new int[0]);
      }

      this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.HOSTILE, 1.5F, 0.8F);
      this.substitutionCooldown = 600;
   }

   public void onDeath(DamageSource cause) {
      if (!this.world.isRemote) {
         this.onCombatDeath();
      }

      super.onDeath(cause);
   }

   private void invalidateSharedEncounterOnDespawn() {
      NBTTagCompound data = this.getEntityData();
      String questId = data.getString("sharedQuestId");
      if (questId != null && !questId.isEmpty()) {
         try {
            QuestManager qm = QuestManager.getInstance();
            if (qm != null) {
               int stepIdx = data.getInteger("sharedStepIndex");
               String encKey = questId + "_" + stepIdx;
               qm.removeSharedEncounter(encKey);
               System.out.println("[QuestNPC] Invalidated SharedEncounter " + encKey + " due to NPC despawn (prevents credit exploit)");
            }
         } catch (Exception var6) {
         }

      }
   }

   public boolean attackEntityAsMob(Entity target) {
      if (target instanceof EntityLivingBase && this.meleeCooldown <= 0) {
         this.performMeleeSwing((EntityLivingBase)target);
         return true;
      } else {
         return false;
      }
   }

   protected void performMeleeSwing(EntityLivingBase target) {
      if (this.isIceDome(target)) {
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), 200.0F);
         this.swingArm(EnumHand.MAIN_HAND);
         this.meleeCooldown = 20;
      } else {
         float baseDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         double dmgMul = this.getDamageMultiplier();
         float normalDmg;
         float trueDmg;
         if (this.trueDamageSplit > 0.0F) {
            normalDmg = baseDamage * (1.0F - this.trueDamageSplit) * (float)dmgMul;
            trueDmg = baseDamage * this.trueDamageSplit * (float)dmgMul * this.trueDamageMultiplier;
         } else {
            normalDmg = baseDamage * (float)dmgMul;
            trueDmg = (this.combatTier >= 4 ? 8.0F : (this.combatTier >= 3 ? 5.0F : (this.combatTier >= 2 ? 3.5F : 2.0F))) * this.trueDamageMultiplier;
         }

         target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         this.swingArm(EnumHand.MAIN_HAND);
         int[] cdRange = this.getCooldownRange(20, 35);
         this.meleeCooldown = cdRange[0] + this.rand.nextInt(cdRange[1] - cdRange[0] + 1);
      }
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      if (!this.world.isRemote) {
         if (!this.isPassive && this.ticksExisted % 2 == 0) {
            this.purgeMovementDebuffs();
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

         if (this.meleeCooldown > 0) {
            --this.meleeCooldown;
         }

         if (this.kunaiCooldown > 0) {
            --this.kunaiCooldown;
         }

         if (this.dashCooldown > 0) {
            --this.dashCooldown;
         }

         if (this.jutsuCooldown > 0) {
            --this.jutsuCooldown;
         }

         if (this.verticalLeapCooldown > 0) {
            --this.verticalLeapCooldown;
         }

         if (this.substitutionCooldown > 0) {
            --this.substitutionCooldown;
         }

         if (this.headhunterCooldown > 0) {
            --this.headhunterCooldown;
         }

         if (this.attackStaggerDelay > 0) {
            --this.attackStaggerDelay;
         }

         if (this.strafeCooldown > 0) {
            --this.strafeCooldown;
         }

         if (this.repositionCooldown > 0) {
            --this.repositionCooldown;
         }

         if (this.horizontalLeapCooldown > 0) {
            --this.horizontalLeapCooldown;
         }

         if (this.chatCooldown > 0) {
            --this.chatCooldown;
         }

         if (this.waterDragonCooldown > 0) {
            --this.waterDragonCooldown;
         }

         if (this.targetSwitchCooldown > 0) {
            --this.targetSwitchCooldown;
         }

         if (this.waterPrisonCooldown > 0) {
            --this.waterPrisonCooldown;
         }

         if (this.iceDomeScanCooldown > 0) {
            --this.iceDomeScanCooldown;
         }

         if (this.woodTrapScanCooldown > 0) {
            --this.woodTrapScanCooldown;
         }

         this.tickStyleCooldowns();
         if (this.leapSuccessCheckTicks > 0) {
            --this.leapSuccessCheckTicks;
            if (this.leapSuccessCheckTicks == 0) {
               EntityLivingBase leapTarget = this.getAttackTarget();
               if (leapTarget != null && Math.abs(leapTarget.posY - this.posY) <= (double)2.0F) {
                  this.failedLeapAttempts = 0;
               }
            }
         }

         if (!this.isPassive && this.getHeldItemMainhand().isEmpty() && this.ticksExisted % 20 == 0) {
            this.equipWeapon();
         }

         if (this.isPassive) {
            if (Math.abs(this.motionX) > 0.1 || Math.abs(this.motionZ) > 0.1) {
               this.motionX = (double)0.0F;
               this.motionZ = (double)0.0F;
               this.velocityChanged = true;
            }

            return;
         }

         this.tickRetreatSafety();
         if (this.questRetreating) {
            this.setAttackTarget((EntityLivingBase)null);
            return;
         }

         if (this.getAttackTarget() != null && this.getAttackTarget().isEntityAlive()) {
            ++this.consecutiveInvulnerableTicks;
            if (this.consecutiveInvulnerableTicks >= 300) {
               ++this.safetyResetCount;
               boolean isHighValue = this.getEntityData().getBoolean("chakraWorldBoss") || this.getEntityData().getBoolean("bingoEntity") || this.getEntityData().getBoolean("raidEntity") || this.getEntityData().getBoolean("questEntity");
               int killThreshold = isHighValue ? 10 : 3;
               if (this.safetyResetCount >= killThreshold) {
                  System.out.println("[QuestNPC] KILLING stuck NPC: " + this.getName() + " (config=" + this.getNpcConfigId() + ") after " + this.safetyResetCount + " safety resets");
                  this.setHealth(0.0F);
                  this.setDead();
                  return;
               }

               System.out.println("[QuestNPC] Safety reset #" + this.safetyResetCount + ": " + this.getName() + " (config=" + this.getNpcConfigId() + ") invulnerable for " + 15 + "s");
               this.onForceResetCombatState();
               this.consecutiveInvulnerableTicks = 0;
            }
         } else {
            this.consecutiveInvulnerableTicks = 0;
         }

         NBTTagCompound selfData = this.getEntityData();
         boolean isSystemManaged = selfData.getBoolean("tayuyaDoki") || selfData.getBoolean("kakuzuHeart") || selfData.getBoolean("sakonUkon") || selfData.getBoolean("territoryDefender") || selfData.getBoolean("defenseEntity") || selfData.getBoolean("incursionEntity") || selfData.getBoolean("bingoEntity") || selfData.getBoolean("chakraWorldBoss") || selfData.getBoolean("raidEntity") || selfData.getBoolean("questEntity");
         if (isSystemManaged) {
            this.noPlayerTicks = 0;
         } else if (this.ticksExisted % 40 == 20) {
            EntityPlayer nearest = this.world.getClosestPlayerToEntity(this, (double)48.0F);
            if (nearest != null && nearest.isEntityAlive()) {
               this.noPlayerTicks = 0;
            } else {
               this.noPlayerTicks += 40;
               if (this.noPlayerTicks >= 1200) {
                  this.getEntityData().setBoolean("systemCleanup", true);
                  this.invalidateSharedEncounterOnDespawn();
                  this.setDead();
                  return;
               }
            }
         }

         if (!this.world.isRemote && this.ticksExisted > 100 && this.ticksExisted % 200 == 50) {
            NBTTagCompound entityData = this.getEntityData();
            if (entityData.getBoolean("questEntity")) {
               String ownerStr = entityData.getString("ownerUUID");
               String questId = entityData.getString("sharedQuestId");
               if (ownerStr != null && !ownerStr.isEmpty() && questId != null && !questId.isEmpty()) {
                  try {
                     UUID ownerId = UUID.fromString(ownerStr);
                     QuestManager qm = QuestManager.getInstance();
                     if (qm != null) {
                        Map<String, QuestInstance> slots = qm.getActiveQuests(ownerId);
                        boolean questStillActive = false;
                        boolean isSummon = entityData.getBoolean("tayuyaDoki") || entityData.getBoolean("sakonUkon") || entityData.getBoolean("sasoriPuppet");

                        for(QuestInstance qi : slots.values()) {
                           if (qi.getQuestId().equals(questId)) {
                              if (isSummon) {
                                 questStillActive = true;
                              } else if (qi.getSpawnedEntities().contains(this.getUniqueID())) {
                                 questStillActive = true;
                              }
                              break;
                           }
                        }

                        if (!questStillActive) {
                           System.out.println("[QuestNPC] Orphan detected: " + this.getCustomNameTag() + " (config=" + this.getNpcConfigId() + ", quest=" + questId + (isSummon ? ", summon" : "") + ") — despawning");
                           this.getEntityData().setBoolean("systemCleanup", true);
                           this.invalidateSharedEncounterOnDespawn();
                           this.setDead();
                           return;
                        }
                     }
                  } catch (IllegalArgumentException var24) {
                  }
               }
            }
         }

         if (this.spawnDelayActive) {
            if (this.spawnDelayRemaining > 0) {
               --this.spawnDelayRemaining;
               return;
            }

            this.spawnDelayActive = false;
            this.setInvisible(false);
            this.setNoAI(false);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)0.5F, 0.8, (double)0.5F, 0.03, new int[0]);
            }
         }

         if (this.switchRetreatTicks > 0) {
            --this.switchRetreatTicks;
            if (this.switchRetreatTicks > 0) {
               this.setAttackTarget((EntityLivingBase)null);
               return;
            }

            EntityPlayer nearestPlayer = this.world.getClosestPlayerToEntity(this, (double)30.0F);
            if (nearestPlayer != null && nearestPlayer.isEntityAlive()) {
               this.setAttackTarget(nearestPlayer);
            }
         }

         if (!this.isDashing && (Math.abs(this.motionX) > 0.3 || Math.abs(this.motionZ) > 0.3 || this.motionY > 0.4)) {
            this.motionX *= 0.05;
            this.motionZ *= 0.05;
            if (this.motionY > 0.4 && !this.isLeaping) {
               this.motionY = 0.05;
            }

            this.velocityChanged = true;
         }

         Entity trappingEntity = this.findNearbyTrapProjectile();
         if (trappingEntity != null) {
            if (trappingEntity instanceof EntityIceMirrors.EntityCustom) {
               ++this.trapEscapeTick;
               if (this.trapEscapeTick >= 20) {
                  this.trapEscapeTick = 0;
                  this.escapeIceMirrors((EntityIceMirrors.EntityCustom)trappingEntity);
               }
            } else if (trappingEntity instanceof EntityLivingBase && trappingEntity.getClass().getName().toLowerCase().contains("icedome")) {
               ++this.trapEscapeTick;
               if (this.trapEscapeTick % 10 == 0) {
                  trappingEntity.hurtResistantTime = 0;
                  ((EntityLivingBase)trappingEntity).attackEntityFrom(DamageSource.MAGIC, 50.0F);
                  this.swingArm(EnumHand.MAIN_HAND);
                  this.getLookHelper().setLookPositionWithEntity(trappingEntity, 30.0F, 30.0F);
               }
            } else {
               String trapClassName = trappingEntity.getClass().getName().toLowerCase();
               boolean isWoodTrap = trapClassName.contains("wood") || trapClassName.contains("mokuton");
               if (isWoodTrap) {
                  this.noClip = true;
                  this.nudgeTowardTarget((double)5.0F, true);
               } else {
                  this.noClip = false;
                  ++this.trapEscapeTick;
                  if (this.trapEscapeTick >= 2) {
                     this.trapEscapeTick = 0;
                     this.nudgeTowardTarget((double)2.0F, false);
                  }
               }
            }
         } else {
            if (this.noClip) {
               this.noClip = false;
               this.unstickFromBlocks();
            }

            this.trapEscapeTick = 0;
            double movedX = Math.abs(this.posX - this.prevPosX);
            double movedZ = Math.abs(this.posZ - this.prevPosZ);
            if (movedX < 0.005 && movedZ < 0.005 && this.getAttackTarget() != null) {
               ++this.stuckTicks;
               if (this.stuckTicks >= 4) {
                  EntityLivingBase stuckTarget = this.getAttackTarget();
                  double stuckDy = stuckTarget != null ? stuckTarget.posY - this.posY : (double)0.0F;
                  double stuckHdx = stuckTarget != null ? stuckTarget.posX - this.posX : (double)0.0F;
                  double stuckHdz = stuckTarget != null ? stuckTarget.posZ - this.posZ : (double)0.0F;
                  double stuckHDist = Math.sqrt(stuckHdx * stuckHdx + stuckHdz * stuckHdz);
                  if (stuckDy > (double)2.0F && stuckHDist < (double)4.0F && this.failedLeapAttempts >= 2) {
                     this.nudgeUpwardToTarget();
                  } else if (stuckDy > (double)2.0F) {
                     this.nudgeTowardTarget((double)3.0F, false);
                  } else {
                     this.nudgeTowardTarget((double)2.0F, false);
                  }

                  this.stuckTicks = 0;
               }
            } else {
               this.stuckTicks = 0;
            }
         }

         this.prevPosX = this.posX;
         this.prevPosZ = this.posZ;
         if (this.ticksExisted % 10 == 5) {
            this.unstickFromBlocks();
         }

         if (this.isDashing) {
            this.processDashTick();
         }

         if (this.strafeTicksRemaining > 0) {
            --this.strafeTicksRemaining;
            EntityLivingBase strafeTarget = this.getAttackTarget();
            if (strafeTarget != null && strafeTarget.isEntityAlive()) {
               double sdx = strafeTarget.posX - this.posX;
               double sdz = strafeTarget.posZ - this.posZ;
               double sDist = Math.sqrt(sdx * sdx + sdz * sdz);
               if (sDist > (double)1.0F) {
                  double perpX = -sdz / sDist * (double)this.strafeDirection;
                  double perpZ = sdx / sDist * (double)this.strafeDirection;
                  double strafeSpeed = (double)0.25F;
                  this.motionX = perpX * strafeSpeed;
                  this.motionZ = perpZ * strafeSpeed;
                  this.velocityChanged = true;
                  this.getLookHelper().setLookPositionWithEntity(strafeTarget, 30.0F, 30.0F);
               }
            }
         }

         if (this.isLeaping && this.onGround && !this.isDashing) {
            this.isLeaping = false;
         }

         EntityLivingBase elevTarget = this.getAttackTarget();
         if (elevTarget != null && elevTarget.isEntityAlive()) {
            double elevDy = elevTarget.posY - this.posY;
            if (elevDy > (double)1.5F && elevTarget.onGround) {
               ++this.targetElevatedTicks;
            } else if (elevDy > (double)1.5F) {
               this.targetElevatedTicks = Math.max(0, this.targetElevatedTicks - 1);
            } else {
               this.targetElevatedTicks = Math.max(0, this.targetElevatedTicks - 3);
            }
         } else {
            this.targetElevatedTicks = Math.max(0, this.targetElevatedTicks - 3);
         }

         if (!this.isDashing && this.onGround && this.horizontalLeapCooldown <= 0 && this.verticalLeapCooldown <= 0 && this.targetElevatedTicks >= 5) {
            elevTarget = this.getAttackTarget();
            if (elevTarget != null && elevTarget.isEntityAlive()) {
               double dy = elevTarget.posY - this.posY;
               double hdx = elevTarget.posX - this.posX;
               double hdz = elevTarget.posZ - this.posZ;
               double hDist = Math.sqrt(hdx * hdx + hdz * hdz);
               if (dy > (double)1.5F && dy <= (double)30.0F) {
                  if (hDist >= (double)2.0F && hDist < (double)14.0F) {
                     this.startArcLeap(elevTarget);
                  } else if (hDist < (double)2.0F) {
                     this.startVerticalJump(dy);
                  }
               }
            }
         }

         if (!this.isDashing && this.onGround && this.verticalLeapCooldown <= 0 && this.failedLeapAttempts >= 2 && this.targetElevatedTicks >= 12) {
            elevTarget = this.getAttackTarget();
            if (elevTarget != null && elevTarget.isEntityAlive()) {
               double dy = elevTarget.posY - this.posY;
               double ldx = elevTarget.posX - this.posX;
               double ldz = elevTarget.posZ - this.posZ;
               double horizDist = Math.sqrt(ldx * ldx + ldz * ldz);
               if (dy > (double)2.0F && horizDist < (double)18.0F) {
                  if (horizDist >= (double)1.0F) {
                     this.startPowerLeap(elevTarget);
                  } else {
                     this.startVerticalJump(dy);
                  }
               }
            }
         }

         if (!this.isDashing && this.onGround && this.failedLeapAttempts >= 2 && this.targetElevatedTicks >= 30) {
            elevTarget = this.getAttackTarget();
            if (elevTarget != null && elevTarget.isEntityAlive()) {
               double dy = elevTarget.posY - this.posY;
               double cdx = elevTarget.posX - this.posX;
               double cdz = elevTarget.posZ - this.posZ;
               double cHDist = Math.sqrt(cdx * cdx + cdz * cdz);
               if (dy > (double)2.0F && dy <= (double)50.0F && cHDist < (double)8.0F) {
                  this.nudgeUpwardToTarget();
                  this.failedLeapAttempts = 0;
                  this.targetElevatedTicks = 0;
                  this.verticalLeapCooldown = 40;
               }
            }
         }

         if (!this.isTerritoryDefender && !Double.isNaN(this.spawnOriginX) && this.ticksExisted % 20 == 0) {
            double ldx = this.posX - this.spawnOriginX;
            double ldy = this.posY - this.spawnOriginY;
            double ldz = this.posZ - this.spawnOriginZ;
            double yDist = Math.abs(ldy);
            double zSoft = this.configLeashRange > (double)0.0F ? this.configLeashRange : (double)50.0F;
            double zHard = this.configLeashRange > (double)0.0F ? this.configLeashRange * (double)1.5F : (double)75.0F;
            double xSoft = this.configLeashRangeX > (double)0.0F ? this.configLeashRangeX : zSoft;
            double xHard = this.configLeashRangeX > (double)0.0F ? this.configLeashRangeX * (double)1.5F : zHard;
            boolean hardViolation = Math.abs(ldx) > xHard || Math.abs(ldz) > zHard || yDist > (double)25.0F;
            boolean softViolation = Math.abs(ldx) > xSoft || Math.abs(ldz) > zSoft || yDist > (double)15.0F;
            if (hardViolation) {
               double safeY = this.isPositionSafe(this.spawnOriginX, this.spawnOriginY, this.spawnOriginZ) ? this.spawnOriginY : this.findSafeY(this.spawnOriginX, this.spawnOriginY, this.spawnOriginZ);
               this.broadcastNearby((double)60.0F, "§e" + this.getCustomNameTag() + " retreated to their post!");
               this.internalReposition = true;
               this.setPositionAndUpdate(this.spawnOriginX, safeY, this.spawnOriginZ);
               this.internalReposition = false;
               this.setAttackTarget((EntityLivingBase)null);
               this.leashReturnCooldown = 100;
            } else if (softViolation) {
               this.setAttackTarget((EntityLivingBase)null);
               this.leashReturnCooldown = 100;
            }
         }

         if (this.leashReturnCooldown > 0) {
            --this.leashReturnCooldown;
            return;
         }

         if (this.activeDome != null && this.activeDome.isDead) {
            this.activeDome = null;
         }

         if (this.jutsuWindup > 0) {
            --this.jutsuWindup;
            if (this.jutsuTargetVec != null) {
               double aimDx = this.jutsuTargetVec.x - this.posX;
               double aimDy = this.jutsuTargetVec.y - (this.posY + (double)this.getEyeHeight());
               double aimDz = this.jutsuTargetVec.z - this.posZ;
               double aimHoriz = Math.sqrt(aimDx * aimDx + aimDz * aimDz);
               this.rotationYaw = (float)(Math.atan2(-aimDx, aimDz) * (180D / Math.PI));
               this.rotationPitch = (float)(-Math.atan2(aimDy, aimHoriz) * (180D / Math.PI));
               this.rotationYawHead = this.rotationYaw;
            }

            if (this.jutsuWindup % 2 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.6, this.posY + (double)1.0F + this.rand.nextDouble() * 0.3, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.6, 3, 0.2, 0.1, 0.2, 0.01, new int[0]);
            }

            if (this.jutsuWindup == 0 && this.jutsuTargetVec != null) {
               this.executeNatureJutsu();
            }
         }

         if (!this.isPassive && this.ticksExisted % 10 == 0) {
            elevTarget = this.findNearbyIceDome((double)20.0F);
            if (elevTarget != null && elevTarget.isEntityAlive()) {
               this.setAttackTarget(elevTarget);
               if ((double)this.getDistance(elevTarget) <= (double)4.0F) {
                  elevTarget.hurtResistantTime = 0;
                  elevTarget.attackEntityFrom(DamageSource.causeMobDamage(this), 200.0F);
                  this.swingArm(EnumHand.MAIN_HAND);
               }
            }
         }

         elevTarget = this.getAttackTarget();
         if ((elevTarget == null || !elevTarget.isEntityAlive()) && !this.isPassive) {
            EntityPlayer nearestPlayer = this.world.getClosestPlayerToEntity(this, (double)30.0F);
            if (nearestPlayer != null && nearestPlayer.isEntityAlive() && !nearestPlayer.isSpectator() && !nearestPlayer.isCreative()) {
               this.setAttackTarget(nearestPlayer);
               elevTarget = this.getAttackTarget();
            }
         }

         if (elevTarget == null || !elevTarget.isEntityAlive()) {
            return;
         }

         double dist = (double)this.getDistance(elevTarget);
         if (this.targetSwitchCooldown <= 0) {
            this.targetSwitchCooldown = 120;
            List<EntityPlayer> nearby = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F), (px) -> px != null && px.isEntityAlive() && !px.isSpectator());
            if (nearby.size() >= 2) {
               EntityLivingBase revenge = this.getRevengeTarget();
               if (revenge != null && !(revenge instanceof EntityPlayer)) {
                  EntityPlayer revengeOwner = this.resolveEntityOwner(revenge);
                  if (revengeOwner != null) {
                     revenge = revengeOwner;
                  }
               }

               if (revenge instanceof EntityPlayer && revenge.isEntityAlive() && revenge != elevTarget && (double)this.getDistance(revenge) <= (double)30.0F) {
                  this.setAttackTarget(revenge);
                  elevTarget = this.getAttackTarget();
               } else {
                  ArrayList<EntityPlayer> others = new ArrayList();

                  for(EntityPlayer p : nearby) {
                     if (p != elevTarget) {
                        others.add(p);
                     }
                  }

                  if (!others.isEmpty()) {
                     EntityPlayer candidate = (EntityPlayer)others.get(this.rand.nextInt(others.size()));
                     this.setAttackTarget(candidate);
                     elevTarget = this.getAttackTarget();
                  }
               }

               if (elevTarget == null || !elevTarget.isEntityAlive()) {
                  return;
               }

               dist = (double)this.getDistance(elevTarget);
            }
         }

         if (dist <= (double)3.0F) {
            this.failedLeapAttempts = 0;
         }

         if (this.lastAttackTarget != elevTarget) {
            this.failedLeapAttempts = 0;
            this.targetElevatedTicks = 0;
            this.lastAttackTarget = elevTarget;
         }

         this.processCombat(elevTarget, dist);
      }

   }

   protected void checkRageTrigger() {
      if (!this.rageTriggered && !(this.rageThreshold <= 0.0F)) {
         float hpPercent = this.getHealth() / this.getMaxHealth();
         if (!(hpPercent > this.rageThreshold)) {
            this.rageTriggered = true;
            IAttributeInstance speedAttr = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
               AttributeModifier speedMod = new AttributeModifier(UUID.fromString("e1a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b5c6"), "feral_rage_speed", (double)(this.rageSpeedMultiplier - 1.0F), 2);
               speedAttr.removeModifier(speedMod.getID());
               speedAttr.applyModifier(speedMod);
            }

            IAttributeInstance dmgAttr = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
            if (dmgAttr != null) {
               AttributeModifier dmgMod = new AttributeModifier(UUID.fromString("a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6"), "feral_rage_damage", (double)(this.rageDamageMultiplier - 1.0F), 2);
               dmgAttr.removeModifier(dmgMod.getID());
               dmgAttr.applyModifier(dmgMod);
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, this.posX, this.posY + (double)1.5F, this.posZ, 20, 0.8, 0.8, 0.8, 0.1, new int[0]);
            }

            this.broadcastNearby((double)48.0F, "§4§l* " + this.getCustomNameTag() + " enters a rage! *");
         }
      }
   }

   protected void processDashTick() {
      if (this.dashTicksRemaining <= 0) {
         this.endDash();
      } else {
         this.motionX = this.dashVelX;
         this.motionZ = this.dashVelZ;
         int halfDuration = this.dashDuration / 2;
         if (this.dashType != 3 && this.dashType != 4) {
            if (this.dashTicksRemaining > halfDuration) {
               this.motionY = (double)this.dashArcSustain;
            } else {
               this.motionY = Math.max((double)this.dashMaxFall, this.motionY - (double)this.dashFallAccel);
            }
         } else if (this.motionY < (double)0.0F) {
            this.motionY = Math.max((double)this.dashMaxFall, this.motionY - (double)this.dashFallAccel);
         }

         if (this.dashHasMidairGuidance && this.dashTicksRemaining <= halfDuration) {
            EntityLivingBase target = this.getAttackTarget();
            if (target != null && target.isEntityAlive()) {
               double gdx = target.posX - this.posX;
               double gdz = target.posZ - this.posZ;
               double gDist = Math.sqrt(gdx * gdx + gdz * gdz);
               if (gDist > (double)0.5F) {
                  this.motionX += gdx / gDist * 0.1;
                  this.motionZ += gdz / gDist * 0.1;
               }
            }
         }

         this.velocityChanged = true;
         if (!this.isTerritoryDefender && !Double.isNaN(this.spawnOriginX)) {
            double ldx = Math.abs(this.posX - this.spawnOriginX);
            double ldz = Math.abs(this.posZ - this.spawnOriginZ);
            double dashZ = this.configLeashRange > (double)0.0F ? this.configLeashRange * (double)1.5F : (double)75.0F;
            double dashX = this.configLeashRangeX > (double)0.0F ? this.configLeashRangeX * (double)1.5F : dashZ;
            if (ldx > dashX || ldz > dashZ) {
               this.endDash();
               return;
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            int grassStateId = Block.getStateId(Blocks.GRASS.getDefaultState());
            switch (this.dashType) {
               case 1:
                  ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.1, this.posZ, 2, 0.2, 0.05, 0.2, 0.01, new int[]{grassStateId});
                  break;
               case 2:
                  ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + 0.8, this.posZ, 3, 0.2, 0.3, 0.2, 0.05, new int[0]);
                  break;
               case 3:
                  ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.2, this.posZ, 4, 0.3, 0.1, 0.3, 0.02, new int[]{grassStateId});
                  break;
               case 4:
                  ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 3, 0.3, 0.2, 0.3, 0.02, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.1, this.posZ, 3, 0.4, 0.1, 0.4, 0.02, new int[]{grassStateId});
            }
         }

         --this.dashTicksRemaining;
         if (this.dashTicksRemaining <= 0) {
            this.endDash();
         }

      }
   }

   protected void endDash() {
      int endedType = this.dashType;
      this.isDashing = false;
      this.isLeaping = false;
      this.dashTicksRemaining = 0;
      this.dashType = 0;
      this.dashDuration = 0;
      this.dashVelX = (double)0.0F;
      this.dashVelZ = (double)0.0F;
      this.dashArcSustain = 0.0F;
      this.dashFallAccel = 0.0F;
      this.dashMaxFall = 0.0F;
      this.dashHasMidairGuidance = false;
      if (this.world instanceof WorldServer) {
         WorldServer ws = (WorldServer)this.world;
         int grassStateId = Block.getStateId(Blocks.GRASS.getDefaultState());
         switch (endedType) {
            case 1:
               ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.1, this.posZ, 8, 0.4, 0.1, 0.4, 0.02, new int[]{grassStateId});
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SMALL_FALL, SoundCategory.HOSTILE, 0.6F, 1.0F);
               break;
            case 2:
               ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)0.5F, this.posZ, 15, (double)0.5F, 0.3, (double)0.5F, 0.1, new int[0]);
               ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.1, this.posZ, 10, (double)0.5F, 0.1, (double)0.5F, 0.02, new int[]{grassStateId});
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 1.0F, 0.8F);
               break;
            case 3:
               ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.1, this.posZ, 12, 0.6, 0.15, 0.6, 0.03, new int[]{grassStateId});
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SMALL_FALL, SoundCategory.HOSTILE, 0.8F, 0.9F);
               break;
            case 4:
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + 0.3, this.posZ, 20, 0.8, 0.3, 0.8, 0.05, new int[0]);
               ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.1, this.posZ, 15, 0.8, 0.15, 0.8, 0.04, new int[]{grassStateId});
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.7F, 1.2F);
         }
      }

      if (endedType == 2) {
         this.applyDashStrikeDamage();
      }

   }

   protected void applyDashStrikeDamage() {
      EntityLivingBase target = this.getAttackTarget();
      if (target != null && (double)this.getDistance(target) <= (double)3.0F) {
         float dashBase = this.combatTier >= 4 ? 6.0F : (this.combatTier >= 3 ? 5.0F : 4.0F);
         float normalDmg = dashBase * (float)this.getDamageMultiplier();
         float trueDmg = (this.combatTier >= 4 ? 4.0F : (this.combatTier >= 3 ? 3.0F : 2.0F)) * this.trueDamageMultiplier;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         this.swingArm(EnumHand.MAIN_HAND);
      }

   }

   protected void startLungeDash(EntityLivingBase target) {
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
         this.dashType = 1;
         this.dashDuration = duration;
         this.dashTicksRemaining = duration;
         this.dashVelX = nx * velocityPerTick * (double)1.0F;
         this.dashVelZ = nz * velocityPerTick * (double)1.0F;
         this.dashArcSustain = 0.05F;
         this.dashFallAccel = 0.06F;
         this.dashMaxFall = -0.3F;
         this.dashHasMidairGuidance = false;
         this.motionY = (double)0.25F;
         this.velocityChanged = true;
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.1, this.posZ, 6, 0.3, 0.1, 0.3, 0.02, new int[]{Block.getStateId(Blocks.GRASS.getDefaultState())});
         }

         this.dashCooldown = 80 + this.rand.nextInt(60);
      }
   }

   protected void startArcLeap(EntityLivingBase target) {
      double dx = target.posX - this.posX;
      double dy = target.posY - this.posY;
      double dz = target.posZ - this.posZ;
      double horizDist = Math.sqrt(dx * dx + dz * dz);
      if (!(horizDist < (double)0.5F)) {
         double nx = dx / horizDist;
         double nz = dz / horizDist;
         int duration = 12;
         double velocityPerTick = horizDist / (double)duration;
         this.isDashing = true;
         this.isLeaping = true;
         this.dashType = 3;
         this.dashDuration = duration;
         this.dashTicksRemaining = duration;
         this.dashVelX = nx * velocityPerTick * 1.2;
         this.dashVelZ = nz * velocityPerTick * 1.2;
         this.dashArcSustain = 0.0F;
         this.dashFallAccel = 0.06F;
         this.dashMaxFall = -0.8F;
         this.dashHasMidairGuidance = false;
         double yScale = horizDist < (double)3.0F ? 0.15 : 0.08;
         double yBase = horizDist < (double)3.0F ? 0.65 : 0.55;
         this.motionY = Math.min((double)2.0F, yBase + dy * yScale);
         this.velocityChanged = true;
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.1, this.posZ, 12, (double)0.5F, 0.15, (double)0.5F, 0.03, new int[]{Block.getStateId(Blocks.GRASS.getDefaultState())});
         }

         this.horizontalLeapCooldown = 25 + this.rand.nextInt(15);
         this.leapSuccessCheckTicks = 20;
         ++this.failedLeapAttempts;
      }
   }

   protected void startPowerLeap(EntityLivingBase target) {
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
         this.dashVelX = nx * velocityPerTick * 0.8;
         this.dashVelZ = nz * velocityPerTick * 0.8;
         this.dashArcSustain = 0.0F;
         this.dashFallAccel = 0.06F;
         this.dashMaxFall = -1.0F;
         this.dashHasMidairGuidance = true;
         this.motionY = Math.max(0.85, Math.min((double)2.5F, 0.65 + dy * 0.14));
         this.velocityChanged = true;
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 15, (double)0.5F, 0.4, (double)0.5F, 0.08, new int[0]);
            ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + 0.1, this.posZ, 15, 0.6, 0.15, 0.6, 0.04, new int[]{Block.getStateId(Blocks.GRASS.getDefaultState())});
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.5F, 1.4F);
         this.verticalLeapCooldown = 35;
         this.failedLeapAttempts = 0;
      }
   }

   protected void startVerticalJump(double dy) {
      EntityLivingBase target = this.getAttackTarget();
      this.isDashing = true;
      this.isLeaping = true;
      this.dashType = 4;
      this.dashDuration = 10;
      this.dashTicksRemaining = 10;
      if (target != null) {
         double jdx = target.posX - this.posX;
         double jdz = target.posZ - this.posZ;
         double jHDist = Math.sqrt(jdx * jdx + jdz * jdz);
         if (jHDist > 0.1) {
            double hSpeed = Math.min(jHDist * 0.06, 0.3);
            this.dashVelX = jdx / jHDist * hSpeed;
            this.dashVelZ = jdz / jHDist * hSpeed;
         } else {
            this.dashVelX = (double)0.0F;
            this.dashVelZ = (double)0.0F;
         }
      } else {
         this.dashVelX = (double)0.0F;
         this.dashVelZ = (double)0.0F;
      }

      this.dashArcSustain = 0.0F;
      this.dashFallAccel = 0.06F;
      this.dashMaxFall = -0.8F;
      this.dashHasMidairGuidance = false;
      this.motionY = Math.max(0.7, Math.min((double)1.5F, 0.42 + dy * 0.15));
      this.velocityChanged = true;
      if (this.world instanceof WorldServer) {
         ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + 0.3, this.posZ, 8, 0.3, 0.2, 0.3, 0.05, new int[0]);
      }

      this.horizontalLeapCooldown = 20;
      this.verticalLeapCooldown = 25;
   }

   protected Entity findNearbyTrapProjectile() {
      for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)15.0F))) {
         if (e instanceof EntityIceMirrors.EntityCustom) {
            EntityIceMirrors.EntityCustom dome = (EntityIceMirrors.EntityCustom)e;
            double dx = this.posX - e.posX;
            double dz = this.posZ - e.posZ;
            if (dx * dx + dz * dz <= (double)(dome.getRadius() * dome.getRadius())) {
               return e;
            }
         } else {
            String className = e.getClass().getName().toLowerCase();
            if (className.contains("icedome")) {
               if (this.getDistanceSq(e) < (double)100.0F) {
                  return e;
               }
            } else if ((className.contains("rasenshuriken") || className.contains("rasengan") || className.contains("rasendama") || className.contains("bijuudama") || className.contains("truthseeker") || className.contains("woodburial") || className.contains("woodprison") || className.contains("woodforest") || className.contains("mokuton") || className.contains("woodsegment")) && this.getDistanceSq(e) < (double)144.0F) {
               return e;
            }
         }
      }

      return null;
   }

   protected void escapeIceMirrors(EntityIceMirrors.EntityCustom dome) {
      double escapeRadius = (double)dome.getRadius() + (double)2.0F;
      EntityLivingBase target = this.getAttackTarget();
      double escX;
      double escZ;
      if (target != null) {
         double dx = target.posX - dome.posX;
         double dz = target.posZ - dome.posZ;
         double d = Math.sqrt(dx * dx + dz * dz);
         if (d > 0.1) {
            escX = dome.posX + dx / d * escapeRadius;
            escZ = dome.posZ + dz / d * escapeRadius;
         } else {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            escX = dome.posX + Math.cos(angle) * escapeRadius;
            escZ = dome.posZ + Math.sin(angle) * escapeRadius;
         }
      } else {
         double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
         escX = dome.posX + Math.cos(angle) * escapeRadius;
         escZ = dome.posZ + Math.sin(angle) * escapeRadius;
      }

      double escY = this.findSafeY(escX, this.posY, escZ);
      if (escY < (double)0.0F) {
         escY = this.posY;
      }

      this.internalReposition = true;
      this.setPositionAndUpdate(escX, escY, escZ);
      this.internalReposition = false;
   }

   protected void nudgeTowardTarget(double distance, boolean skipBlockChecks) {
      EntityLivingBase target = this.getAttackTarget();
      if (target != null) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double dist = Math.sqrt(dx * dx + dz * dz);
         if (!(dist < (double)1.0F) || !(Math.abs(target.posY - this.posY) < (double)1.5F)) {
            double hNudgeDist = dist < (double)1.0F ? (double)0.5F : distance;
            double normDist = dist < 0.1 ? 0.1 : dist;
            double moveX = this.posX + dx / normDist * hNudgeDist;
            double moveZ = this.posZ + dz / normDist * hNudgeDist;
            double moveY = this.posY;
            if (!skipBlockChecks) {
               double dy = target.posY - this.posY;
               if (dy > (double)1.5F) {
                  double candidateY = this.findSafeY(moveX, target.posY, moveZ);
                  if (candidateY > this.posY + (double)0.5F && this.isPositionSafe(moveX, candidateY, moveZ) && !this.isEnclosed(moveX, candidateY, moveZ)) {
                     moveY = candidateY;
                  }
               }
            }

            if (!skipBlockChecks) {
               BlockPos below = new BlockPos(moveX, moveY - (double)1.0F, moveZ);
               if (!this.world.getBlockState(below).getMaterial().isSolid()) {
                  return;
               }

               BlockPos foot = new BlockPos(moveX, moveY, moveZ);
               BlockPos head = new BlockPos(moveX, moveY + (double)1.0F, moveZ);
               if (this.world.getBlockState(foot).getMaterial().isSolid()) {
                  return;
               }

               if (this.world.getBlockState(head).getMaterial().isSolid()) {
                  return;
               }

               if (Math.abs(moveY - this.posY) < (double)1.0F) {
                  double midX = (this.posX + moveX) / (double)2.0F;
                  double midZ = (this.posZ + moveZ) / (double)2.0F;
                  BlockPos midFoot = new BlockPos(midX, moveY, midZ);
                  BlockPos midHead = new BlockPos(midX, moveY + (double)1.0F, midZ);
                  if (this.world.getBlockState(midFoot).getMaterial().isSolid()) {
                     return;
                  }

                  if (this.world.getBlockState(midHead).getMaterial().isSolid()) {
                     return;
                  }
               }
            }

            this.internalReposition = true;
            this.setPositionAndUpdate(moveX, moveY, moveZ);
            this.internalReposition = false;
         }
      }
   }

   protected void nudgeUpwardToTarget() {
      EntityLivingBase target = this.getAttackTarget();
      if (target != null) {
         double[] offsets = new double[]{(double)0.0F, (double)1.0F, (double)-1.0F, (double)2.0F, (double)-2.0F};

         for(double ox : offsets) {
            for(double oz : offsets) {
               double testX = target.posX + ox;
               double testZ = target.posZ + oz;

               for(int yOff = 3; yOff >= -6; --yOff) {
                  double testY = target.posY + (double)yOff;
                  if (!(testY <= this.posY) && this.isPositionSafe(testX, testY, testZ) && !this.isEnclosed(testX, testY, testZ)) {
                     this.internalReposition = true;
                     this.setPositionAndUpdate(testX, testY, testZ);
                     this.internalReposition = false;
                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, testX, testY + 0.3, testZ, 10, 0.4, 0.3, 0.4, 0.04, new int[0]);
                     }

                     return;
                  }
               }
            }
         }

      }
   }

   protected void startNatureJutsu(EntityLivingBase target) {
      String[] fireLines = new String[]{"Fire Style!", "Katon!", "Burn!"};
      String[] windLines = new String[]{"Wind Style!", "Futon!", "Take this!"};
      String[] waterLines = new String[]{"Water Style!", "Suiton!", "Drown!"};
      String[] lightningLines = new String[]{"Lightning Style!", "Raiton!", "Fall!"};
      String[] iceLines = new String[]{"Ice Style!", "Hyoton!", "Freeze!"};
      String[] earthLines = new String[]{"Earth Style!", "Doton!", "Crumble!"};
      String line;
      switch (this.natureType) {
         case 1:
            line = fireLines[this.rand.nextInt(fireLines.length)];
            break;
         case 2:
            line = windLines[this.rand.nextInt(windLines.length)];
            break;
         case 3:
            line = waterLines[this.rand.nextInt(waterLines.length)];
            break;
         case 4:
            line = lightningLines[this.rand.nextInt(lightningLines.length)];
            break;
         case 5:
            line = iceLines[this.rand.nextInt(iceLines.length)];
            break;
         case 6:
            line = earthLines[this.rand.nextInt(earthLines.length)];
            break;
         default:
            return;
      }

      if (this.chatCooldown <= 0) {
         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)30.0F))) {
            p.sendMessage(new TextComponentString("§c" + this.getCustomNameTag() + ": §f" + line));
         }

         this.chatCooldown = 400 + this.rand.nextInt(200);
      }

      this.jutsuWindup = 10;
      this.jutsuTargetVec = new Vec3d(target.posX, target.posY + (double)target.getEyeHeight() * (double)0.5F, target.posZ);
      int[] cdRange = this.getCooldownRange(120, 200);
      this.jutsuCooldown = cdRange[0] + this.rand.nextInt(cdRange[1] - cdRange[0] + 1);
   }

   protected void executeNatureJutsu() {
      EntityLivingBase target = this.getAttackTarget();
      if (target != null && target.isEntityAlive()) {
         double dmgMult = this.getDamageMultiplier();
         float normalDmg = this.jutsuPower * (float)dmgMult;
         float trueDmg = (this.combatTier >= 4 ? 6.0F : (this.combatTier >= 3 ? 5.0F : 3.0F)) * this.trueDamageMultiplier;
         switch (this.natureType) {
            case 1:
               this.spawnFireJutsu(target, normalDmg, trueDmg);
               break;
            case 2:
               this.spawnWindJutsu(target, normalDmg, trueDmg);
               break;
            case 3:
               this.spawnWaterJutsu(target, normalDmg, trueDmg);
               break;
            case 4:
               this.spawnLightningJutsu(target, normalDmg, trueDmg);
               break;
            case 5:
               this.spawnIceJutsu(target, normalDmg, trueDmg);
               break;
            case 6:
               this.spawnEarthJutsu(target, normalDmg, trueDmg);
         }

         this.jutsuTargetVec = null;
      } else {
         this.jutsuTargetVec = null;
      }
   }

   protected void spawnFireJutsu(EntityLivingBase target, float normalDmg, float trueDmg) {
      EntityKatonFireball.EntityCustom fireball = new EntityKatonFireball.EntityCustom(this.world, this, normalDmg, trueDmg);
      double dx = target.posX - this.posX;
      double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
      double dz = target.posZ - this.posZ;
      double dist = Math.sqrt(dx * dx + dz * dz);
      fireball.shoot(dx, dy + dist * 0.01, dz, 1.0F, 3.0F);
      this.world.spawnEntity(fireball);

      for(int i = 0; i < 8; ++i) {
         this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F), this.posY + 1.2 + this.rand.nextDouble() * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F), (double)0.0F, 0.05, (double)0.0F, new int[0]);
      }

   }

   protected void spawnWindJutsu(EntityLivingBase target, float normalDmg, float trueDmg) {
      EntityFutonBullet.EntityCustom wind = new EntityFutonBullet.EntityCustom(this.world, this, normalDmg, trueDmg);
      double dx = target.posX - this.posX;
      double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
      double dz = target.posZ - this.posZ;
      double dist = Math.sqrt(dx * dx + dz * dz);
      wind.shoot(dx, dy + dist * 0.01, dz, 0.9F, 2.5F);
      this.world.spawnEntity(wind);
   }

   protected void spawnWaterJutsu(EntityLivingBase target, float normalDmg, float trueDmg) {
      EntitySuitonBullet.EntityCustom water = new EntitySuitonBullet.EntityCustom(this.world, this, normalDmg, trueDmg);
      double dx = target.posX - this.posX;
      double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
      double dz = target.posZ - this.posZ;
      double dist = Math.sqrt(dx * dx + dz * dz);
      water.shoot(dx, dy + dist * 0.01, dz, 0.85F, 3.0F);
      this.world.spawnEntity(water);
   }

   protected void spawnLightningJutsu(EntityLivingBase target, float normalDmg, float trueDmg) {
      EntityRaitonBeam.EntityCustom beam = new EntityRaitonBeam.EntityCustom(this.world, this, normalDmg, trueDmg);
      double dx = target.posX - this.posX;
      double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
      double dz = target.posZ - this.posZ;
      beam.shoot(dx, dy, dz, 1.4F, 1.5F);
      this.world.spawnEntity(beam);
   }

   protected void spawnIceJutsu(EntityLivingBase target, float normalDmg, float trueDmg) {
      for(int i = 0; i < 3; ++i) {
         EntityIceNeedle.EntityCustom needle = new EntityIceNeedle.EntityCustom(this.world, this, normalDmg * 0.5F, trueDmg * 0.5F);
         double dx = target.posX - this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.8;
         double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.getEyeHeight());
         double dz = target.posZ - this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.8;
         double dist = Math.sqrt(dx * dx + dz * dz);
         needle.shoot(dx, dy + dist * 0.02, dz, 1.2F, 2.0F);
         this.world.spawnEntity(needle);
      }

   }

   protected void spawnEarthJutsu(EntityLivingBase target, float normalDmg, float trueDmg) {
      double dx = target.posX - this.posX;
      double dy = target.posY + (double)(target.height / 2.0F) - (this.posY + (double)this.getEyeHeight());
      double dz = target.posZ - this.posZ;
      if (this.rand.nextBoolean()) {
         EntityDotonEarthSpear.EntityCustom spear = new EntityDotonEarthSpear.EntityCustom(this.world, this);
         spear.setDamage(normalDmg);
         spear.shoot(dx, dy, dz, 1.0F, 2.0F);
         spear.setPosition(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
         this.world.spawnEntity(spear);
      } else {
         EntityWeightedBoulder.EntityCustom boulder = new EntityWeightedBoulder.EntityCustom(this.world, this, target.posX, target.posY, target.posZ, normalDmg * 0.1F);
         this.world.spawnEntity(boulder);
      }

      target.hurtResistantTime = 0;
      target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
      if (trueDmg > 0.0F) {
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
      }

      if (this.world instanceof WorldServer) {
         WorldServer ws = (WorldServer)this.world;
         ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, target.posX, target.posY + (double)0.5F, target.posZ, 30, (double)1.0F, (double)0.5F, (double)1.0F, 0.15, new int[]{Block.getStateId(Blocks.DIRT.getDefaultState())});
      }

      this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.HOSTILE, 1.5F, 0.7F);
   }

   protected void throwKunai(EntityLivingBase target) {
      if (this.useIceNeedles) {
         this.throwIceNeedles(target);
      } else {
         double dmgMult = this.getDamageMultiplier();
         float normalDmg = 5.0F * (float)dmgMult;
         float trueDmg = (this.combatTier >= 4 ? 2.5F : (this.combatTier >= 3 ? 2.0F : 1.5F)) * this.trueDamageMultiplier;
         EntityKunaiProjectile.EntityCustom kunai = new EntityKunaiProjectile.EntityCustom(this.world, this, normalDmg, trueDmg);
         double dx = target.posX - this.posX;
         double dy = target.posY + (double)target.getEyeHeight() - 0.1 - kunai.posY;
         double dz = target.posZ - this.posZ;
         double dist = Math.sqrt(dx * dx + dz * dz);
         kunai.shoot(dx, dy + dist * 0.15, dz, this.kunaiSpeed, this.kunaiInaccuracy);
         this.world.spawnEntity(kunai);
         this.swingArm(EnumHand.MAIN_HAND);
         int[] cdRange = this.getCooldownRange(60, 100);
         this.kunaiCooldown = cdRange[0] + this.rand.nextInt(cdRange[1] - cdRange[0] + 1);
      }
   }

   protected void throwIceNeedles(EntityLivingBase target) {
      double dmgMult = this.getDamageMultiplier();
      float normalDmg = 4.0F * (float)dmgMult;
      float trueDmg = this.combatTier >= 3 ? 2.5F : 2.0F;

      for(int i = 0; i < 3; ++i) {
         EntityIceNeedle.EntityCustom needle = new EntityIceNeedle.EntityCustom(this.world, this, normalDmg, trueDmg);
         double dx = target.posX - this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F;
         double dy = target.posY + (double)target.getEyeHeight() * (double)0.5F - (this.posY + (double)this.getEyeHeight());
         double dz = target.posZ - this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F;
         double dist = Math.sqrt(dx * dx + dz * dz);
         needle.shoot(dx, dy + dist * 0.02, dz, this.kunaiSpeed, this.kunaiInaccuracy);
         this.world.spawnEntity(needle);
      }

      this.swingArm(EnumHand.MAIN_HAND);
      int[] cdRange = this.getCooldownRange(40, 70);
      this.kunaiCooldown = cdRange[0] + this.rand.nextInt(cdRange[1] - cdRange[0] + 1);
   }

   public boolean isPositionSafe(double x, double y, double z) {
      BlockPos foot = new BlockPos(x, y, z);
      BlockPos head = new BlockPos(x, y + (double)1.0F, z);
      BlockPos ground = new BlockPos(x, y - (double)1.0F, z);
      return this.world.getBlockState(ground).getMaterial().isSolid() && !this.world.getBlockState(foot).getMaterial().isSolid() && !this.world.getBlockState(head).getMaterial().isSolid();
   }

   public boolean isEnclosed(double x, double y, double z) {
      int enclosedCount = 0;

      for(int i = 1; i <= 3; ++i) {
         if (this.world.getBlockState(new BlockPos(x + (double)i, y, z)).getMaterial().isSolid()) {
            ++enclosedCount;
            break;
         }
      }

      for(int i = 1; i <= 3; ++i) {
         if (this.world.getBlockState(new BlockPos(x - (double)i, y, z)).getMaterial().isSolid()) {
            ++enclosedCount;
            break;
         }
      }

      for(int i = 1; i <= 3; ++i) {
         if (this.world.getBlockState(new BlockPos(x, y, z + (double)i)).getMaterial().isSolid()) {
            ++enclosedCount;
            break;
         }
      }

      for(int i = 1; i <= 3; ++i) {
         if (this.world.getBlockState(new BlockPos(x, y, z - (double)i)).getMaterial().isSolid()) {
            ++enclosedCount;
            break;
         }
      }

      return enclosedCount >= 4;
   }

   public double findSafeY(double x, double referenceY, double z) {
      int startY = Math.min((int)referenceY + 10, 255);
      int minY = Math.max((int)referenceY - 20, 1);

      for(int y = startY; y >= minY; --y) {
         if (this.isPositionSafe(x, (double)y, z)) {
            return (double)y;
         }
      }

      return referenceY;
   }

   protected void unstickFromBlocks() {
      BlockPos foot = new BlockPos(this.posX, this.posY, this.posZ);
      BlockPos head = new BlockPos(this.posX, this.posY + (double)1.0F, this.posZ);
      boolean feetStuck = this.world.getBlockState(foot).getMaterial().isSolid();
      boolean headStuck = this.world.getBlockState(head).getMaterial().isSolid();
      if (feetStuck || headStuck) {
         for(int y = (int)this.posY; (double)y < this.posY + (double)10.0F && y < 255; ++y) {
            if (this.isPositionSafe(this.posX, (double)y, this.posZ)) {
               this.internalReposition = true;
               this.setPositionAndUpdate(this.posX, (double)y, this.posZ);
               this.internalReposition = false;
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
                     if (this.isPositionSafe(checkX, checkY, checkZ) && !this.isEnclosed(checkX, checkY, checkZ)) {
                        this.internalReposition = true;
                        this.setPositionAndUpdate(checkX, checkY, checkZ);
                        this.internalReposition = false;
                        return;
                     }
                  }
               }
            }
         }

      }
   }

   protected void equipWeapon() {
      if (this.weaponItemId != null && !this.weaponItemId.isEmpty()) {
         String[] parts = this.weaponItemId.split(":");
         if (parts.length == 2) {
            Item weapon = (Item)Item.REGISTRY.getObject(new ResourceLocation(parts[0], parts[1]));
            if (weapon != null) {
               this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(weapon));
               this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
            }

            if (this.offhandItemId != null && !this.offhandItemId.isEmpty()) {
               String[] ohParts = this.offhandItemId.split(":");
               if (ohParts.length == 2) {
                  Item offhand = (Item)Item.REGISTRY.getObject(new ResourceLocation(ohParts[0], ohParts[1]));
                  if (offhand != null) {
                     this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(offhand));
                     this.setDropChance(EntityEquipmentSlot.OFFHAND, 0.0F);
                  }
               }
            }

         }
      }
   }

   protected void broadcastNearby(double range, String message) {
      for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(range))) {
         if (p instanceof EntityPlayerMP) {
            ((EntityPlayerMP)p).sendStatusMessage(new TextComponentString(message), true);
         }
      }

   }

   protected void broadcastChat(double range, String message) {
      for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(range))) {
         p.sendMessage(new TextComponentString(message));
      }

   }

   protected boolean isKnockbackImmune() {
      return true;
   }

   protected int countNearbyPlayers() {
      List<EntityPlayer> nearby = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F));
      return nearby.size();
   }

   public double getDamageMultiplier() {
      int extra = Math.max(0, this.countNearbyPlayers() - 1);
      double mul = (double)1.0F + Math.min((double)extra * 0.05, 0.3);
      return mul * (double)this.configDamageMultiplier;
   }

   protected double getDefenseMultiplier() {
      int extra = Math.max(0, this.countNearbyPlayers() - 1);
      return 1.08 + Math.min((double)extra * 0.05, 0.3);
   }

   protected int cdMul(int baseCd) {
      return Math.max(10, (int)((float)baseCd * this.configCooldownMultiplier));
   }

   public int[] getCooldownRange(int baseMin, int baseMax) {
      int extra = Math.max(0, this.countNearbyPlayers() - 1);
      double reduction = (double)1.0F - Math.min((double)extra * 0.04, 0.2);
      double cdMul = reduction * (double)this.configCooldownMultiplier;
      return new int[]{Math.max(10, (int)((double)baseMin * cdMul)), Math.max(15, (int)((double)baseMax * cdMul))};
   }

   public void applyNpcConfig(NpcConfig config) {
      this.dataManager.set(NPC_CONFIG_ID, config.getConfigId());
      this.dataManager.set(NPC_POSE, config.getPose().name());
      double damageNerf = PRE_CHUNIN_EXEMPT_IDS.contains(config.getConfigId()) ? (double)1.0F : 0.8;
      if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(config.getMaxHealth());
         this.setHealth((float)config.getMaxHealth());
      }

      if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(config.getMovementSpeed());
      }

      if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(config.getArmor());
      }

      if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(config.getAttackDamage() * damageNerf);
      }

      if (this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) != null) {
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
      }

      this.maxHurtResistantTime = 7;
      this.isPassive = config.isPassive();
      this.combatTier = config.getCombatTier();
      this.trueDamageSplit = config.getTrueDamageSplit();
      this.hasRangedAttack = config.hasRangedAttack();
      this.natureType = config.getNatureType();
      this.weaponItemId = config.getWeaponItemId();
      this.offhandItemId = config.getOffhandItemId();
      this.helmetItemId = config.getHelmetItemId();
      this.chestplateItemId = config.getChestplateItemId();
      this.leggingsItemId = config.getLeggingsItemId();
      this.jutsuPower = config.getJutsuPower();
      this.kunaiSpeed = config.getKunaiSpeed();
      this.kunaiInaccuracy = config.getKunaiInaccuracy();
      this.teamRole = config.getTeamRole();
      this.hasHeadhunter = config.hasHeadhunter();
      this.useIceNeedles = config.isUseIceNeedles();
      this.hasIceDomePhase = config.isHasIceDomePhase();
      this.iceDomeHealthThreshold = config.getIceDomeHealthThreshold();
      this.hasWaterDragon = config.isHasWaterDragon();
      this.waterDragonPower = config.getWaterDragonPower();
      this.hasSwordCombo = config.hasSwordCombo();
      this.hasHiddenMist = config.hasHiddenMist();
      this.hiddenMistThreshold = config.getHiddenMistThreshold();
      this.hasWaterPrison = config.hasWaterPrison();
      this.combatStyle = config.getCombatStyle();
      this.rageThreshold = config.getRageThreshold();
      this.rageDamageMultiplier = config.getRageDamageMultiplier();
      this.rageSpeedMultiplier = config.getRageSpeedMultiplier();
      this.configCooldownMultiplier = config.getCooldownMultiplier();
      this.configDamageMultiplier = (float)((double)config.getDamageMultiplier() * damageNerf);
      this.configLeashRange = config.getLeashRange();
      this.configLeashRangeX = config.getLeashRangeX();
      this.themeColor = config.getThemeColor();
      this.accentColor = config.getAccentColor();
      this.ghostAlpha = config.getGhostAlpha();
      this.resetCombatState();
      if (!config.isPassive()) {
         this.setNoAI(false);
         this.tasks.taskEntries.clear();
         this.targetTasks.taskEntries.clear();
         if (this.usesVanillaMeleeAI()) {
            this.tasks.addTask(1, new EntityAIAttackMelee(this, (double)1.0F, true));
         }

         this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 32.0F));
         this.tasks.addTask(6, new EntityAILookIdle(this));
         this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
         boolean checkSight = !this.getEntityData().getBoolean("territoryDefender");
         this.targetTasks.addTask(2, new QuestNpcTargetingAI(this, EntityPlayer.class, checkSight));
         this.equipWeapon();
      } else {
         this.setNoAI(false);
         this.tasks.taskEntries.clear();
         this.targetTasks.taskEntries.clear();
         this.tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 12.0F, 1.0F));
         this.tasks.addTask(2, new EntityAILookIdle(this));
      }

      this.equipArmorSlot(this.helmetItemId, EntityEquipmentSlot.HEAD);
      this.equipArmorSlot(this.chestplateItemId, EntityEquipmentSlot.CHEST);
      this.equipArmorSlot(this.leggingsItemId, EntityEquipmentSlot.LEGS);
      if (config.getSpawnDelayTicks() > 0) {
         this.spawnDelayRemaining = config.getSpawnDelayTicks();
         this.spawnDelayActive = true;
         this.setInvisible(true);
         this.setNoAI(true);
      }

      this.setCustomNameTag(config.getDisplayName());
      this.setAlwaysRenderNameTag(true);
      this.attackStaggerDelay = this.rand.nextInt(15) + 5;
      if (Double.isNaN(this.spawnOriginX)) {
         this.spawnOriginX = this.posX;
         this.spawnOriginY = this.posY;
         this.spawnOriginZ = this.posZ;
      }

      this.isTerritoryDefender = this.getEntityData().getBoolean("territoryDefender");
   }

   private void equipArmorSlot(String itemId, EntityEquipmentSlot slot) {
      if (itemId != null && !itemId.isEmpty()) {
         String[] parts = itemId.split(":");
         if (parts.length == 2) {
            Item item = (Item)Item.REGISTRY.getObject(new ResourceLocation(parts[0], parts[1]));
            if (item != null) {
               this.setItemStackToSlot(slot, new ItemStack(item));
               this.setDropChance(slot, 0.0F);
            }
         }
      }

   }

   public String getNpcConfigId() {
      return (String)this.dataManager.get(NPC_CONFIG_ID);
   }

   public String getNpcPoseName() {
      return (String)this.dataManager.get(NPC_POSE);
   }

   public void setTextureOverride(String textureResLoc) {
      this.dataManager.set(TEXTURE_OVERRIDE, textureResLoc != null ? textureResLoc : "");
   }

   public String getTextureOverride() {
      return (String)this.dataManager.get(TEXTURE_OVERRIDE);
   }

   public String getCombatStyle() {
      return this.combatStyle;
   }

   public int getThemeColor() {
      return this.themeColor;
   }

   public int getAccentColor() {
      return this.accentColor;
   }

   public boolean hasHealthBarColors() {
      return this.themeColor != 0;
   }

   public float getGhostAlpha() {
      return this.ghostAlpha;
   }

   public boolean isGhost() {
      return this.ghostAlpha < 1.0F;
   }

   public void writeEntityToNBT(NBTTagCompound compound) {
      super.writeEntityToNBT(compound);
      String configId = this.getNpcConfigId();
      if (configId != null && !configId.isEmpty()) {
         compound.setString("npcConfigId", configId);
      }

      String texOverride = this.getTextureOverride();
      if (texOverride != null && !texOverride.isEmpty()) {
         compound.setString("textureOverride", texOverride);
      }

      compound.setBoolean("isPassive", this.isPassive);
      compound.setInteger("combatTier", this.combatTier);
      compound.setBoolean("hasRangedAttack", this.hasRangedAttack);
      compound.setInteger("natureType", this.natureType);
      if (this.weaponItemId != null) {
         compound.setString("weaponItemId", this.weaponItemId);
      }

      if (this.chestplateItemId != null) {
         compound.setString("chestplateItemId", this.chestplateItemId);
      }

      if (this.leggingsItemId != null) {
         compound.setString("leggingsItemId", this.leggingsItemId);
      }

      compound.setFloat("jutsuPower", this.jutsuPower);
      compound.setFloat("kunaiSpeed", this.kunaiSpeed);
      compound.setFloat("kunaiInaccuracy", this.kunaiInaccuracy);
      compound.setFloat("trueDamageSplit", this.trueDamageSplit);
      compound.setFloat("trueDamageMultiplier", this.trueDamageMultiplier);
      compound.setFloat("configCooldownMultiplier", this.configCooldownMultiplier);
      compound.setFloat("configDamageMultiplier", this.configDamageMultiplier);
      compound.setDouble("configLeashRange", this.configLeashRange);
      compound.setDouble("configLeashRangeX", this.configLeashRangeX);
      if (!Double.isNaN(this.spawnOriginX)) {
         compound.setDouble("spawnOriginX", this.spawnOriginX);
         compound.setDouble("spawnOriginY", this.spawnOriginY);
         compound.setDouble("spawnOriginZ", this.spawnOriginZ);
      }

      compound.setString("combatStyle", this.combatStyle);
      compound.setFloat("rageThreshold", this.rageThreshold);
      compound.setFloat("rageDamageMultiplier", this.rageDamageMultiplier);
      compound.setFloat("rageSpeedMultiplier", this.rageSpeedMultiplier);
      compound.setBoolean("rageTriggered", this.rageTriggered);
      compound.setInteger("substitutionCD", this.substitutionCooldown);
      compound.setBoolean("hasHeadhunter", this.hasHeadhunter);
      compound.setInteger("headhunterCD", this.headhunterCooldown);
      compound.setBoolean("useIceNeedles", this.useIceNeedles);
      compound.setBoolean("hasIceDomePhase", this.hasIceDomePhase);
      compound.setFloat("iceDomeHealthThreshold", this.iceDomeHealthThreshold);
      compound.setBoolean("iceDomeTriggered", this.iceDomeTriggered);
      compound.setBoolean("hasWaterDragon", this.hasWaterDragon);
      compound.setFloat("waterDragonPower", this.waterDragonPower);
      compound.setBoolean("hasSwordCombo", this.hasSwordCombo);
      compound.setBoolean("hasHiddenMist", this.hasHiddenMist);
      compound.setFloat("hiddenMistThreshold", this.hiddenMistThreshold);
      compound.setBoolean("hiddenMistTriggered", this.hiddenMistTriggered);
      compound.setBoolean("hasWaterPrison", this.hasWaterPrison);
      if (this.teamRole != null) {
         compound.setString("teamRole", this.teamRole);
      }

      compound.setInteger("switchRetreat", this.switchRetreatTicks);
      compound.setInteger("spawnDelayRemain", this.spawnDelayRemaining);
      compound.setBoolean("spawnDelayActive", this.spawnDelayActive);
      compound.setInteger("themeColor", this.themeColor);
      compound.setInteger("accentColor", this.accentColor);
      compound.setFloat("ghostAlpha", this.ghostAlpha);
      this.writeCombatNBT(compound);
   }

   public void readEntityFromNBT(NBTTagCompound compound) {
      super.readEntityFromNBT(compound);
      this.isPassive = compound.getBoolean("isPassive");
      this.combatTier = compound.getInteger("combatTier");
      this.hasRangedAttack = compound.getBoolean("hasRangedAttack");
      this.natureType = compound.getInteger("natureType");
      if (compound.hasKey("weaponItemId")) {
         this.weaponItemId = compound.getString("weaponItemId");
      }

      if (compound.hasKey("chestplateItemId")) {
         this.chestplateItemId = compound.getString("chestplateItemId");
      }

      if (compound.hasKey("leggingsItemId")) {
         this.leggingsItemId = compound.getString("leggingsItemId");
      }

      this.jutsuPower = compound.hasKey("jutsuPower") ? compound.getFloat("jutsuPower") : 0.0F;
      this.kunaiSpeed = compound.hasKey("kunaiSpeed") ? compound.getFloat("kunaiSpeed") : 1.6F;
      this.kunaiInaccuracy = compound.hasKey("kunaiInaccuracy") ? compound.getFloat("kunaiInaccuracy") : 2.0F;
      this.trueDamageSplit = compound.hasKey("trueDamageSplit") ? compound.getFloat("trueDamageSplit") : 0.0F;
      this.trueDamageMultiplier = compound.hasKey("trueDamageMultiplier") ? compound.getFloat("trueDamageMultiplier") : 1.0F;
      this.configCooldownMultiplier = compound.hasKey("configCooldownMultiplier") ? compound.getFloat("configCooldownMultiplier") : 1.0F;
      this.configDamageMultiplier = compound.hasKey("configDamageMultiplier") ? compound.getFloat("configDamageMultiplier") : 1.0F;
      this.configLeashRange = compound.hasKey("configLeashRange") ? compound.getDouble("configLeashRange") : (double)0.0F;
      this.configLeashRangeX = compound.hasKey("configLeashRangeX") ? compound.getDouble("configLeashRangeX") : (double)0.0F;
      this.isTerritoryDefender = this.getEntityData().getBoolean("territoryDefender");
      if (compound.hasKey("spawnOriginX")) {
         this.spawnOriginX = compound.getDouble("spawnOriginX");
         this.spawnOriginY = compound.getDouble("spawnOriginY");
         this.spawnOriginZ = compound.getDouble("spawnOriginZ");
      }

      this.combatStyle = compound.hasKey("combatStyle") ? compound.getString("combatStyle") : "STANDARD";
      this.rageThreshold = compound.hasKey("rageThreshold") ? compound.getFloat("rageThreshold") : 0.0F;
      this.rageDamageMultiplier = compound.hasKey("rageDamageMultiplier") ? compound.getFloat("rageDamageMultiplier") : 1.0F;
      this.rageSpeedMultiplier = compound.hasKey("rageSpeedMultiplier") ? compound.getFloat("rageSpeedMultiplier") : 1.0F;
      this.rageTriggered = compound.getBoolean("rageTriggered");
      this.substitutionCooldown = compound.getInteger("substitutionCD");
      this.hasHeadhunter = compound.getBoolean("hasHeadhunter");
      this.headhunterCooldown = compound.getInteger("headhunterCD");
      this.useIceNeedles = compound.getBoolean("useIceNeedles");
      this.hasIceDomePhase = compound.getBoolean("hasIceDomePhase");
      this.iceDomeHealthThreshold = compound.hasKey("iceDomeHealthThreshold") ? compound.getFloat("iceDomeHealthThreshold") : 0.3F;
      this.iceDomeTriggered = compound.getBoolean("iceDomeTriggered");
      this.hasWaterDragon = compound.getBoolean("hasWaterDragon");
      this.waterDragonPower = compound.hasKey("waterDragonPower") ? compound.getFloat("waterDragonPower") : 1.5F;
      this.hasSwordCombo = compound.getBoolean("hasSwordCombo");
      this.hasHiddenMist = compound.getBoolean("hasHiddenMist");
      this.hiddenMistThreshold = compound.hasKey("hiddenMistThreshold") ? compound.getFloat("hiddenMistThreshold") : 0.6F;
      this.hiddenMistTriggered = compound.getBoolean("hiddenMistTriggered");
      this.hasWaterPrison = compound.getBoolean("hasWaterPrison");
      if (compound.hasKey("teamRole")) {
         this.teamRole = compound.getString("teamRole");
      }

      this.switchRetreatTicks = compound.getInteger("switchRetreat");
      this.spawnDelayRemaining = compound.getInteger("spawnDelayRemain");
      this.spawnDelayActive = compound.getBoolean("spawnDelayActive");
      this.themeColor = compound.hasKey("themeColor") ? compound.getInteger("themeColor") : 0;
      this.accentColor = compound.hasKey("accentColor") ? compound.getInteger("accentColor") : 0;
      this.ghostAlpha = compound.hasKey("ghostAlpha") ? compound.getFloat("ghostAlpha") : 1.0F;
      if (this.spawnDelayActive) {
         this.setInvisible(true);
         this.setNoAI(true);
      }

      this.readCombatNBT(compound);
   }

   static {
      NPC_CONFIG_ID = EntityDataManager.createKey(QuestNpcBase.class, DataSerializers.STRING);
      NPC_POSE = EntityDataManager.createKey(QuestNpcBase.class, DataSerializers.STRING);
      TEXTURE_OVERRIDE = EntityDataManager.createKey(QuestNpcBase.class, DataSerializers.STRING);
      PARALYSIS_MODIFIER_UUID = UUID.fromString("c69af92a-b96d-49b7-a396-9b3b0d77edd5");
      HEAVINESS_SPEED_UUID = UUID.fromString("7d735ff6-8872-482d-ac1f-cd2249e8f584");
   }
}
