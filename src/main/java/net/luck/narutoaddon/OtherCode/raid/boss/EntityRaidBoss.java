
package net.luck.narutoaddon.OtherCode.raid.boss;

import com.google.common.base.Predicate;
import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.BossInfo.Overlay;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;

import java.util.ArrayList;

public abstract class EntityRaidBoss extends EntityCreature implements IRaidBoss {
   protected static final float BASE_HEALTH = 1000.0F;
   protected static final float BASE_DAMAGE = 8.0F;
   protected static final float BASE_ARMOR = 10.0F;
   protected static final float BASE_SPEED = 0.25F;
   protected RaidInstance raidInstance;
   protected RaidDifficulty difficulty;
   protected BossPhaseController phaseController;
   protected boolean damageImmune;
   protected String immunityReason;
   protected int enrageLevel;
   protected float enrageDamageMultiplier;
   protected float enrageSpeedMultiplier;
   protected BossInfoServer bossInfo;
   private static final int MIN_HURT_COOLDOWN = 10;
   private static final float MAX_DAMAGE_PER_HIT = 125.0F;
   private static final double BOSS_Y_LEASH_HARD = (double)30.0F;
   private double spawnY;

   public EntityRaidBoss(World worldIn) {
      super(worldIn);
      this.difficulty = RaidDifficulty.GENIN;
      this.damageImmune = false;
      this.immunityReason = "";
      this.enrageLevel = 0;
      this.enrageDamageMultiplier = 1.0F;
      this.enrageSpeedMultiplier = 1.0F;
      this.spawnY = Double.NaN;
      this.phaseController = new BossPhaseController(this);
      this.experienceValue = 500;
      this.bossInfo = new BossInfoServer(new TextComponentString(this.getBossDisplayName()), Color.RED, Overlay.PROGRESS);
      this.setSize(1.0F, 2.5F);
   }

   public abstract String getBossId();

   public abstract String getBossDisplayName();

   public abstract void initPhases();

   public abstract ResourceLocation getBossBarTexture();

   protected void initEntityAI() {
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 32.0F));
      this.tasks.addTask(9, new EntityAILookIdle(this));
      Predicate<EntityPlayer> activeParticipantSelector = new Predicate<EntityPlayer>() {
         public boolean apply(EntityPlayer target) {
            return EntityRaidBoss.this.isValidTarget(target);
         }
      };
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]) {
         protected boolean isSuitableTarget(EntityLivingBase target, boolean includeInvincibles) {
            if (!(target instanceof EntityPlayer)) {
               return false;
            } else {
               return EntityRaidBoss.this.isValidTarget((EntityPlayer)target) && super.isSuitableTarget(target, includeInvincibles);
            }
         }
      });
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 10, true, false, activeParticipantSelector));
   }

   public boolean isValidTarget(EntityPlayer player) {
      if (player != null && !player.isDead && !player.isSpectator()) {
         return this.raidInstance == null ? true : this.raidInstance.isActiveParticipant(player.getUniqueID());
      } else {
         return false;
      }
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)1000.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)8.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)10.0F);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.25F);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)64.0F);
      this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
   }

   public void setDifficulty(RaidDifficulty difficulty) {
      this.difficulty = difficulty;
      this.applyDifficultyScaling();
      this.initPhases();
   }

   protected void applyDifficultyScaling() {
      float healthMult = this.difficulty.getHealthMultiplier();
      float damageMult = this.difficulty.getDamageMultiplier();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)(1000.0F * healthMult));
      this.setHealth(this.getMaxHealth());
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)(8.0F * damageMult));
   }

   public void setEnrageLevel(int level) {
      if (level > this.enrageLevel) {
         this.enrageLevel = level;
         switch (level) {
            case 1:
               this.enrageDamageMultiplier = 1.15F;
               this.enrageSpeedMultiplier = 1.1F;
               break;
            case 2:
               this.enrageDamageMultiplier = 1.3F;
               this.enrageSpeedMultiplier = 1.2F;
               break;
            default:
               this.enrageDamageMultiplier = 1.0F;
               this.enrageSpeedMultiplier = 1.0F;
         }

         double baseSpeed = (double)(0.25F * this.enrageSpeedMultiplier);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(baseSpeed);
         this.onEnrage(level);
      }

   }

   protected void onEnrage(int newLevel) {
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
      if (this.damageImmune) {
         if (source.getTrueSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)source.getTrueSource();
            player.sendStatusMessage(new TextComponentString("§c" + this.getBossDisplayName() + " is immune! " + this.immunityReason), true);
         }

         return false;
      } else if (this.hurtResistantTime > 0) {
         return false;
      } else {
         if (amount > 125.0F) {
            amount = 125.0F;
         }

         float modifiedAmount = amount * this.enrageDamageMultiplier;
         boolean result = super.attackEntityFrom(source, modifiedAmount);
         if (result) {
            this.hurtResistantTime = Math.max(this.hurtResistantTime, 10);
         }

         return result;
      }
   }

   public void setDamageImmune(boolean immune, String reason) {
      this.damageImmune = immune;
      this.immunityReason = reason != null ? reason : "";
   }

   public void onUpdate() {
      super.onUpdate();
      if (!this.world.isRemote) {
         if (Double.isNaN(this.spawnY)) {
            this.spawnY = this.posY;
         }

         if (this.ticksExisted % 10 == 0 && !Double.isNaN(this.spawnY)) {
            double yDist = this.spawnY - this.posY;
            if (yDist > (double)30.0F) {
               this.setPositionAndUpdate(this.posX, this.spawnY, this.posZ);
               System.out.println("[RaidBoss] " + this.getBossDisplayName() + " teleported back to spawn Y (fell " + (int)yDist + " blocks)");
            }
         }

         this.escapeFromBlock();
         this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
         if (this.phaseController != null && this.raidInstance != null) {
            this.phaseController.tick(this.raidInstance);
         }
      }

   }

   public void setCustomNameTag(String name) {
      super.setCustomNameTag(name);
      this.bossInfo.setName(this.getDisplayName());
   }

   public void onPhaseTransition(int oldPhase, int newPhase, BossPhase oldPhaseObj, BossPhase newPhaseObj) {
      if (this.raidInstance != null) {
         this.raidInstance.broadcastMessage("§e§l[PHASE " + newPhase + "] " + newPhaseObj.getName());
      }

   }

   public void addPlayerToBossBar(EntityPlayer player) {
      if (player instanceof EntityPlayerMP) {
         this.bossInfo.addPlayer((EntityPlayerMP)player);
      }

   }

   public void removePlayerFromBossBar(EntityPlayer player) {
      if (player instanceof EntityPlayerMP) {
         this.bossInfo.removePlayer((EntityPlayerMP)player);
      }

   }

   public void writeEntityToNBT(NBTTagCompound compound) {
      super.writeEntityToNBT(compound);
      compound.setInteger("difficulty", this.difficulty.ordinal());
      compound.setBoolean("damageImmune", this.damageImmune);
      compound.setString("immunityReason", this.immunityReason);
      compound.setInteger("enrageLevel", this.enrageLevel);
   }

   public void readEntityFromNBT(NBTTagCompound compound) {
      super.readEntityFromNBT(compound);
      if (compound.hasKey("difficulty")) {
         this.setDifficulty(RaidDifficulty.fromOrdinal(compound.getInteger("difficulty")));
      }

      this.damageImmune = compound.getBoolean("damageImmune");
      this.immunityReason = compound.getString("immunityReason");
      if (compound.hasKey("enrageLevel")) {
         this.setEnrageLevel(compound.getInteger("enrageLevel"));
      }

   }

   public void onDeath(DamageSource cause) {
      super.onDeath(cause);
      this.clearBossBar();
   }

   protected void onDeathUpdate() {
      super.onDeathUpdate();
      if (this.deathTime == 1) {
         this.clearBossBar();
      }

   }

   public void clearBossBar() {
      this.bossInfo.setVisible(false);

      for(EntityPlayerMP player : new ArrayList(this.bossInfo.getPlayers())) {
         this.bossInfo.removePlayer(player);
      }

   }

   public void setDead() {
      super.setDead();
      this.clearBossBar();
   }

   public boolean isNonBoss() {
      return false;
   }

   protected boolean canDespawn() {
      return false;
   }

   public boolean canBePushed() {
      return false;
   }

   public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
   }

   public void applyEntityCollision(Entity entityIn) {
   }

   protected void collideWithNearbyEntities() {
   }

   protected boolean escapeFromBlock() {
      BlockPos foot = new BlockPos(this.posX, this.posY, this.posZ);
      BlockPos head = new BlockPos(this.posX, this.posY + (double)1.0F, this.posZ);
      boolean footSolid = this.world.getBlockState(foot).getMaterial().isSolid();
      boolean headSolid = this.world.getBlockState(head).getMaterial().isSolid();
      if (!footSolid && !headSolid) {
         return false;
      } else {
         int[][] offsets = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {2, 0, 0}, {-2, 0, 0}, {0, 0, 2}, {0, 0, -2}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {3, 0, 0}, {-3, 0, 0}, {0, 0, 3}, {0, 0, -3}, {0, 1, 0}, {0, 2, 0}, {0, 3, 0}};

         for(int[] off : offsets) {
            double tx = this.posX + (double)off[0];
            double ty = this.posY + (double)off[1];
            double tz = this.posZ + (double)off[2];
            BlockPos testFoot = new BlockPos(tx, ty, tz);
            BlockPos testHead = new BlockPos(tx, ty + (double)1.0F, tz);
            BlockPos testBelow = new BlockPos(tx, ty - (double)1.0F, tz);
            if (!this.world.getBlockState(testFoot).getMaterial().isSolid() && !this.world.getBlockState(testHead).getMaterial().isSolid() && this.world.getBlockState(testBelow).getMaterial().isSolid()) {
               double destX = Math.floor(tx) + (double)0.5F;
               double destZ = Math.floor(tz) + (double)0.5F;
               this.setPositionAndUpdate(destX, ty, destZ);
               this.motionX = (double)0.0F;
               this.motionY = (double)0.0F;
               this.motionZ = (double)0.0F;
               return true;
            }
         }

         if (this.raidInstance != null && this.raidInstance.getArena() != null) {
            BlockPos center = this.raidInstance.getArena().getCenter();
            this.setPositionAndUpdate((double)center.getX() + (double)0.5F, (double)(center.getY() + 1), (double)center.getZ() + (double)0.5F);
            this.motionX = (double)0.0F;
            this.motionY = (double)0.0F;
            this.motionZ = (double)0.0F;
            return true;
         } else {
            return false;
         }
      }
   }

   public void setAttackTarget(EntityLivingBase entitylivingbaseIn) {
      if (entitylivingbaseIn instanceof EntityPlayer && !this.isValidTarget((EntityPlayer)entitylivingbaseIn)) {
         super.setAttackTarget((EntityLivingBase)null);
      } else {
         super.setAttackTarget(entitylivingbaseIn);
      }
   }

   public void onPlayerRemovedFromRaid(EntityPlayerMP player) {
      if (player != null) {
         this.removePlayerFromBossBar(player);
         if (this.getAttackTarget() != null && this.getAttackTarget().getUniqueID().equals(player.getUniqueID())) {
            this.setAttackTarget((EntityLivingBase)null);
         }
      }

   }

   public RaidInstance getRaidInstance() {
      return this.raidInstance;
   }

   public void setRaidInstance(RaidInstance instance) {
      this.raidInstance = instance;
      if (instance != null) {
         for(EntityPlayer player : instance.getParticipants()) {
            this.addPlayerToBossBar(player);
         }
      }

   }

   public RaidDifficulty getDifficulty() {
      return this.difficulty;
   }

   public BossPhaseController getPhaseController() {
      return this.phaseController;
   }

   public int getCurrentPhase() {
      return this.phaseController.getCurrentPhaseNumber();
   }

   public int getTotalPhases() {
      return this.phaseController.getTotalPhases();
   }

   public String getCurrentPhaseName() {
      return this.phaseController.getCurrentPhaseName();
   }

   public int getEnrageLevel() {
      return this.enrageLevel;
   }

   public boolean isDamageImmune() {
      return this.damageImmune;
   }

   public String getImmunityReason() {
      return this.immunityReason;
   }

   public BossInfoServer getBossInfo() {
      return this.bossInfo;
   }

   public World getWorld() {
      return this.world;
   }
}
