
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.raid.arena.RaidArena;
import net.luck.narutoaddon.OtherCode.raid.boss.BossPhase;
import net.luck.narutoaddon.OtherCode.raid.boss.BossPhaseController;
import net.luck.narutoaddon.OtherCode.raid.boss.IRaidBoss;
import net.luck.narutoaddon.OtherCode.raid.boss.bosses.hashirama.HashiramaMechanics;
import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.luck.narutoaddon.OtherCode.raid.network.RaidNetworkHelper;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.BossInfo.Overlay;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityHashirama extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 5;
   public static final int ENTITYID_RANGED = 6;

   public EntityHashirama(ElementsInfTsukAddon instance) {
      super(instance, 25);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "hashirama"), 5).name("inftsuk_hashirama").tracker(128, 1, true).egg(-1, -1).build());
   }

   private Biome[] allbiomes(RegistryNamespaced<ResourceLocation, Biome> in) {
      Iterator<Biome> itr = in.iterator();
      ArrayList<Biome> ls = new ArrayList();

      while(itr.hasNext()) {
         ls.add(itr.next());
      }

      return (Biome[])ls.toArray(new Biome[ls.size()]);
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, HashiramaRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class HashiramaRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation HASHIRAMA_TEXTURE = new ResourceLocation("inftsukaddon:textures/hashiramaix.png");
      private static final ResourceLocation HASHIRAMA_SAGE_TEXTURE = new ResourceLocation("inftsukaddon:textures/hashiramasagemode.png");

      public HashiramaRenderer(RenderManager renderManager) {
         super(renderManager, new ModelPlayer(0.0F, false), 0.5F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return entity.isInSageMode() ? HASHIRAMA_SAGE_TEXTURE : HASHIRAMA_TEXTURE;
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         float scale = 1.2F;
         GlStateManager.scale(scale, scale, scale);
      }
   }

   public static class EntityCustom extends EntityMob implements IRaidBoss {
      private static final DataParameter<Boolean> SAGE_MODE;
      private static final float HASHIRAMA_BASE_HEALTH = 35000.0F;
      private static final float HASHIRAMA_BASE_DAMAGE = 8.0F;
      private static final float HASHIRAMA_BASE_ARMOR = 10.0F;
      private static final float BASE_SPEED = 0.35F;
      private static final float SPEED_PER_DIFFICULTY = 0.035F;
      private static final float GENIN_PHASE_2 = 50.0F;
      private static final float GENIN_PHASE_3 = 25.0F;
      private static final float CHUNIN_PHASE_2 = 65.0F;
      private static final float CHUNIN_PHASE_3 = 40.0F;
      private static final float CHUNIN_PHASE_4 = 20.0F;
      private static final float JONIN_PHASE_2 = 75.0F;
      private static final float JONIN_PHASE_3 = 50.0F;
      private static final float JONIN_PHASE_4 = 30.0F;
      private static final float JONIN_PHASE_5 = 15.0F;
      protected RaidInstance raidInstance;
      private RaidArena arena;
      protected RaidDifficulty difficulty;
      protected BossPhaseController phaseController;
      protected boolean damageImmune;
      protected String immunityReason;
      protected int enrageLevel;
      protected float enrageDamageMultiplier;
      protected float enrageSpeedMultiplier;
      protected BossInfoServer bossInfo;
      private boolean inSageMode;
      private boolean woodHumanActive;
      private int sageModeTicks;
      private boolean facingLocked;
      private float lockedYaw;
      private float lockedHeadYaw;
      private double prevTrackPosX;
      private double prevTrackPosZ;
      private int stuckTicks;
      private int trapEscapeTick;
      private int chakraDashCooldown;
      private static final int CHAKRA_DASH_COOLDOWN_BASE = 100;
      private static final float CHAKRA_DASH_RANGE = 8.0F;
      private static final float CHAKRA_DASH_MAX_RANGE = 25.0F;
      private static final float CHAKRA_DASH_SPEED = 1.5F;
      private boolean isDashing;
      private int dashTicksRemaining;
      private static final int DASH_DURATION_TICKS = 10;
      private double dashVelocityX;
      private double dashVelocityY;
      private double dashVelocityZ;
      private float lastSyncedHealth;
      private int lastSyncedPhase;
      private int targetSwitchCooldown;
      private static final int TARGET_SWITCH_COOLDOWN_BASE = 100;
      private static final int TARGET_SWITCH_COOLDOWN_MIN = 40;
      private UUID currentTargetUUID;
      private int comboHitCount;
      private int comboResetTimer;
      private int nextAttackTimer;
      private static final int COMBO_RESET_TIME = 60;
      private static final int COMBO_MAX_HITS = 5;
      private Entity lastComboTarget;
      private Entity activeComboTarget;
      private boolean inActiveCombo;
      private static final int[] COMBO_DELAYS;

      public EntityCustom(World world) {
         super(world);
         this.difficulty = RaidDifficulty.GENIN;
         this.damageImmune = false;
         this.immunityReason = "";
         this.enrageLevel = 0;
         this.enrageDamageMultiplier = 1.0F;
         this.enrageSpeedMultiplier = 1.0F;
         this.inSageMode = false;
         this.woodHumanActive = false;
         this.sageModeTicks = 0;
         this.facingLocked = false;
         this.lockedYaw = 0.0F;
         this.lockedHeadYaw = 0.0F;
         this.stuckTicks = 0;
         this.trapEscapeTick = 0;
         this.chakraDashCooldown = 0;
         this.isDashing = false;
         this.dashTicksRemaining = 0;
         this.dashVelocityX = (double)0.0F;
         this.dashVelocityY = (double)0.0F;
         this.dashVelocityZ = (double)0.0F;
         this.lastSyncedHealth = -1.0F;
         this.lastSyncedPhase = -1;
         this.targetSwitchCooldown = 0;
         this.currentTargetUUID = null;
         this.comboHitCount = 0;
         this.comboResetTimer = 0;
         this.nextAttackTimer = 0;
         this.lastComboTarget = null;
         this.activeComboTarget = null;
         this.inActiveCombo = false;
         this.setSize(0.6F, 1.8F);
         this.experienceValue = 500;
         this.isImmuneToFire = false;
         this.setNoAI(false);
         this.enablePersistence();
         this.phaseController = new BossPhaseController(this);
         this.bossInfo = new BossInfoServer(new TextComponentString(this.getBossDisplayName()), Color.RED, Overlay.PROGRESS);
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(SAGE_MODE, Boolean.FALSE);
      }

      public String getBossId() {
         return "hashirama";
      }

      public String getBossDisplayName() {
         String name = "Hashirama Senju";
         if (this.inSageMode) {
            name = name + " (Sage Mode)";
         }

         return name;
      }

      public ResourceLocation getBossBarTexture() {
         return new ResourceLocation("inftsukaddon", "textures/gui/boss_bar_hashirama.png");
      }

      protected void initEntityAI() {
         super.initEntityAI();
         this.tasks.addTask(0, new EntityAISwimming(this));
         this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.2, false));
         this.tasks.addTask(2, new EntityAIWander(this, (double)1.0F));
         this.tasks.addTask(3, new EntityAILookIdle(this));
         this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 32.0F));
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
         return SoundEvents.ENTITY_PLAYER_HURT;
      }

      public SoundEvent getDeathSound() {
         return SoundEvents.ENTITY_PLAYER_DEATH;
      }

      protected float getSoundVolume() {
         return 1.0F;
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)10.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.35F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)35000.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)8.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)64.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
         }

      }

      public void setDifficulty(RaidDifficulty difficulty) {
         this.difficulty = difficulty;
         this.applyDifficultyScaling();
         this.initPhases();
      }

      protected void applyDifficultyScaling() {
         float healthMult = this.difficulty.getHealthMultiplier();
         float damageMult = this.difficulty.getDamageMultiplier();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)(35000.0F * healthMult));
         this.setHealth(this.getMaxHealth());
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)(8.0F * damageMult));
         float speedWithDifficulty = 0.35F + 0.035F * (float)this.difficulty.ordinal();
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)speedWithDifficulty);
      }

      public void initPhases() {
         this.phaseController.clearPhases();
         switch (this.difficulty) {
            case GENIN:
               this.initGeninPhases();
               break;
            case CHUNIN:
               this.initChuninPhases();
               break;
            case JONIN:
               this.initJoninPhases();
               break;
            case ANBU:
               this.initChuninPhases();
         }

      }

      private void initGeninPhases() {
         this.phaseController.addPhase((new BossPhase.Builder()).name("Wood Release Assault").type(BossPhase.PhaseType.DAMAGE).healthThreshold(100.0F).addMechanic(HashiramaMechanics.createWoodDragon(this.difficulty)).addMechanic(HashiramaMechanics.createHoteiTechnique(this.difficulty)).addMechanic(HashiramaMechanics.createWoodSpearBarrage(this.difficulty)).onStart(() -> {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§6\"I am Hashirama Senju, the First Hokage!\"");
            }

         }).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Wood Clone Technique").type(BossPhase.PhaseType.PUZZLE).healthThreshold(50.0F).addMechanic(HashiramaMechanics.createWoodClones(this.difficulty)).addMechanic(HashiramaMechanics.createWoodSpearBarrage(this.difficulty)).onStart(() -> {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§6\"Let's see if you can find the real me!\"");
            }

         }).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Final Stand").type(BossPhase.PhaseType.DAMAGE).healthThreshold(25.0F).addMechanic(HashiramaMechanics.createWoodDragon(this.difficulty)).addMechanic(HashiramaMechanics.createHoteiTechnique(this.difficulty)).addMechanic(HashiramaMechanics.createWoodSpearBarrage(this.difficulty)).speedMultiplier(1.15F).damageMultiplier(1.15F).onStart(() -> {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§6\"You've done well to push me this far!\"");
            }

         }).build());
      }

      private void initChuninPhases() {
         this.phaseController.addPhase((new BossPhase.Builder()).name("Wood Release Assault").type(BossPhase.PhaseType.DAMAGE).healthThreshold(100.0F).addMechanic(HashiramaMechanics.createWoodDragon(this.difficulty)).addMechanic(HashiramaMechanics.createHoteiTechnique(this.difficulty)).addMechanic(HashiramaMechanics.createWoodSpearBarrage(this.difficulty)).onStart(() -> {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§6\"I am the God of Shinobi. Show me your resolve!\"");
            }

         }).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Advent of Flowering Trees").type(BossPhase.PhaseType.DAMAGE).healthThreshold(65.0F).addMechanic(HashiramaMechanics.createFloweringTrees(this.difficulty)).addMechanic(HashiramaMechanics.createWoodDragon(this.difficulty)).addMechanic(HashiramaMechanics.createWoodSpearBarrage(this.difficulty)).onStart(() -> {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§6\"Wood Release: Advent of a World of Flowering Trees!\"");
            }

         }).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Wood Clone Technique").type(BossPhase.PhaseType.PUZZLE).healthThreshold(40.0F).addMechanic(HashiramaMechanics.createWoodClones(this.difficulty)).addMechanic(HashiramaMechanics.createWoodSpearBarrage(this.difficulty)).onStart(() -> {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§6\"Can you tell which one is the real me?\"");
            }

         }).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Sage Mode").type(BossPhase.PhaseType.DAMAGE).healthThreshold(20.0F).addMechanic(HashiramaMechanics.createWoodDragon(this.difficulty)).addMechanic(HashiramaMechanics.createHoteiTechnique(this.difficulty)).addMechanic(HashiramaMechanics.createFloweringTrees(this.difficulty)).addMechanic(HashiramaMechanics.createWoodSpearBarrage(this.difficulty)).damageMultiplier(1.3F).speedMultiplier(1.2F).onStart(() -> this.activateSageMode()).build());
      }

      private void initJoninPhases() {
         this.phaseController.addPhase((new BossPhase.Builder()).name("Wood Release Mastery").type(BossPhase.PhaseType.DAMAGE).healthThreshold(100.0F).addMechanic(HashiramaMechanics.createWoodDragon(this.difficulty)).addMechanic(HashiramaMechanics.createHoteiTechnique(this.difficulty)).addMechanic(HashiramaMechanics.createWoodPrison(this.difficulty)).addMechanic(HashiramaMechanics.createWoodSpearBarrage(this.difficulty)).onStart(() -> {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§6\"I've fought the greatest shinobi in history. Let's see what you can do!\"");
            }

         }).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("True Several Thousand Hands").type(BossPhase.PhaseType.IMMUNITY).healthThreshold(75.0F).addMechanic(HashiramaMechanics.createTrueSeveralThousandHands(this.difficulty)).addMechanic(HashiramaMechanics.createWoodSpearBarrage(this.difficulty)).duration(350).onStart(() -> {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§c§l§6\"Wood Release: True Several Thousand Hands!\"");
               this.raidInstance.broadcastMessage("§e§lGet behind the boss! Find the safe zone!");
            }

         }).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Wood Human Technique").type(BossPhase.PhaseType.PUZZLE).healthThreshold(50.0F).addMechanic(HashiramaMechanics.createWoodHuman(this.difficulty)).addMechanic(HashiramaMechanics.createWoodSpearBarrage(this.difficulty)).onStart(() -> {
            this.woodHumanActive = true;
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§6\"Wood Release: Wood Human Technique!\"");
               this.raidInstance.broadcastMessage("§eDestroy the Wood Human to damage Hashirama!");
            }

         }).onEnd(() -> this.woodHumanActive = false).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Forest of Confusion").type(BossPhase.PhaseType.DAMAGE).healthThreshold(30.0F).addMechanic(HashiramaMechanics.createWoodClones(this.difficulty)).addMechanic(HashiramaMechanics.createFloweringTrees(this.difficulty)).addMechanic(HashiramaMechanics.createWoodSpearBarrage(this.difficulty)).onStart(() -> {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§6\"This forest will be your grave!\"");
            }

         }).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Sage Mode - Final Stand").type(BossPhase.PhaseType.DAMAGE).healthThreshold(15.0F).addMechanic(HashiramaMechanics.createTrueSeveralThousandHands(this.difficulty)).addMechanic(HashiramaMechanics.createWoodDragon(this.difficulty)).addMechanic(HashiramaMechanics.createFloweringTrees(this.difficulty)).addMechanic(HashiramaMechanics.createWoodSpearBarrage(this.difficulty)).damageMultiplier(1.4F).speedMultiplier(1.25F).onStart(() -> {
            this.activateSageMode();
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§c§l§6\"This is the full power of the First Hokage!\"");
            }

         }).build());
      }

      private void activateSageMode() {
         if (!this.inSageMode) {
            this.inSageMode = true;
            this.sageModeTicks = 0;
            this.dataManager.set(SAGE_MODE, Boolean.TRUE);
            double currentDamage = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(currentDamage * 1.3);
            this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 2.0F, 1.5F);
            this.bossInfo.setName(new TextComponentString(this.getBossDisplayName()));
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§c§l[SAGE MODE] Hashirama enters Sage Mode!");
               this.raidInstance.broadcastMessage("§7His attacks are now significantly stronger and he regenerates health!");
            }
         }

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

            double baseSpeed = (double)(0.35F * this.enrageSpeedMultiplier);
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(baseSpeed);
            this.onEnrage(level);
         }

      }

      protected void onEnrage(int newLevel) {
         if (newLevel == 1) {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§6\"You're taking too long. Allow me to show you true power!\"");
            }
         } else if (newLevel == 2) {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§c§6\"I've had enough of this! WITNESS THE POWER OF THE FIRST HOKAGE!\"");
            }

            if (!this.inSageMode) {
               this.activateSageMode();
            }
         }

      }

      public boolean attackEntityAsMob(Entity target) {
         if (this.nextAttackTimer > 0) {
            return false;
         } else {
            if (this.lastComboTarget != null && this.lastComboTarget != target) {
               this.comboHitCount = 0;
            }

            this.lastComboTarget = target;
            float attackDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            if (this.phaseController != null && this.phaseController.getCurrentPhase() != null) {
               attackDamage *= this.phaseController.getCurrentPhase().getDamageMultiplier();
            }

            attackDamage *= this.enrageDamageMultiplier;
            if (this.inSageMode) {
               attackDamage = Math.max(attackDamage, attackDamage * 1.15F);
            }

            boolean isHeavyHit = this.comboHitCount >= 4;
            if (isHeavyHit) {
               attackDamage *= 1.3F;
            }

            DamageSource source = DamageSource.MAGIC;
            if (target instanceof EntityLivingBase) {
               ((EntityLivingBase)target).hurtResistantTime = 0;
            }

            boolean success = target.attackEntityFrom(source, attackDamage);
            if (success && target instanceof EntityLivingBase) {
               EntityLivingBase livingTarget = (EntityLivingBase)target;
               float knockbackStrength;
               if (isHeavyHit) {
                  knockbackStrength = this.inSageMode ? 0.9F : 0.65F;
               } else {
                  knockbackStrength = 0.05F;
               }

               livingTarget.knockBack(this, knockbackStrength, this.posX - livingTarget.posX, this.posZ - livingTarget.posZ);
               if (isHeavyHit) {
                  this.world.playSound((EntityPlayer)null, livingTarget.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.7F);
                  this.world.playSound((EntityPlayer)null, livingTarget.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.5F, 1.5F);
               } else {
                  float pitch = 1.0F + (float)this.comboHitCount * 0.15F;
                  this.world.playSound((EntityPlayer)null, livingTarget.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.8F, pitch);
               }

               if (livingTarget instanceof EntityPlayerMP) {
                  EntityPlayerMP player = (EntityPlayerMP)livingTarget;
                  float shakeIntensity;
                  if (isHeavyHit) {
                     shakeIntensity = this.inSageMode ? 0.8F : 0.5F;
                  } else {
                     shakeIntensity = 0.08F + (float)this.comboHitCount * 0.04F;
                  }

                  this.sendScreenShake(player, shakeIntensity, isHeavyHit ? 8 : 3);
               }

               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  if (isHeavyHit) {
                     ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, livingTarget.posX, livingTarget.posY + (double)(livingTarget.height / 2.0F), livingTarget.posZ, 25, 0.8, 0.8, 0.8, 0.3, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.CLOUD, livingTarget.posX, livingTarget.posY + (double)(livingTarget.height / 2.0F), livingTarget.posZ, 10, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, livingTarget.posX, livingTarget.posY + (double)(livingTarget.height / 2.0F), livingTarget.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  } else {
                     ws.spawnParticle(EnumParticleTypes.CRIT, livingTarget.posX, livingTarget.posY + (double)(livingTarget.height / 2.0F), livingTarget.posZ, 5 + this.comboHitCount * 2, 0.2, 0.2, 0.2, 0.05, new int[0]);
                  }
               }

               if (isHeavyHit) {
                  this.comboHitCount = 0;
                  this.comboResetTimer = 0;
                  this.nextAttackTimer = 30;
                  this.inActiveCombo = false;
                  this.activeComboTarget = null;
               } else {
                  int delayIndex = Math.min(this.comboHitCount, COMBO_DELAYS.length - 1);
                  this.nextAttackTimer = COMBO_DELAYS[delayIndex];
                  ++this.comboHitCount;
                  this.comboResetTimer = 60;
                  this.inActiveCombo = true;
                  this.activeComboTarget = livingTarget;
               }
            }

            return success;
         }
      }

      private void performComboAttack(Entity target) {
         float attackDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         if (this.phaseController != null && this.phaseController.getCurrentPhase() != null) {
            attackDamage *= this.phaseController.getCurrentPhase().getDamageMultiplier();
         }

         attackDamage *= this.enrageDamageMultiplier;
         if (this.inSageMode) {
            attackDamage = Math.max(attackDamage, attackDamage * 1.15F);
         }

         boolean isHeavyHit = this.comboHitCount >= 4;
         if (isHeavyHit) {
            attackDamage *= 1.3F;
         }

         DamageSource source = DamageSource.MAGIC;
         if (target instanceof EntityLivingBase) {
            ((EntityLivingBase)target).hurtResistantTime = 0;
         }

         boolean success = target.attackEntityFrom(source, attackDamage);
         if (success && target instanceof EntityLivingBase) {
            EntityLivingBase livingTarget = (EntityLivingBase)target;
            float knockbackStrength;
            if (isHeavyHit) {
               knockbackStrength = this.inSageMode ? 0.9F : 0.65F;
            } else {
               knockbackStrength = 0.05F;
            }

            livingTarget.knockBack(this, knockbackStrength, this.posX - livingTarget.posX, this.posZ - livingTarget.posZ);
            if (isHeavyHit) {
               this.world.playSound((EntityPlayer)null, livingTarget.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.7F);
               this.world.playSound((EntityPlayer)null, livingTarget.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.5F, 1.5F);
            } else {
               float pitch = 1.0F + (float)this.comboHitCount * 0.15F;
               this.world.playSound((EntityPlayer)null, livingTarget.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.8F, pitch);
            }

            if (livingTarget instanceof EntityPlayerMP) {
               EntityPlayerMP player = (EntityPlayerMP)livingTarget;
               float shakeIntensity;
               if (isHeavyHit) {
                  shakeIntensity = this.inSageMode ? 0.8F : 0.5F;
               } else {
                  shakeIntensity = 0.08F + (float)this.comboHitCount * 0.04F;
               }

               this.sendScreenShake(player, shakeIntensity, isHeavyHit ? 8 : 3);
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               if (isHeavyHit) {
                  ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, livingTarget.posX, livingTarget.posY + (double)(livingTarget.height / 2.0F), livingTarget.posZ, 25, 0.8, 0.8, 0.8, 0.3, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.CLOUD, livingTarget.posX, livingTarget.posY + (double)(livingTarget.height / 2.0F), livingTarget.posZ, 10, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, livingTarget.posX, livingTarget.posY + (double)(livingTarget.height / 2.0F), livingTarget.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
               } else {
                  ws.spawnParticle(EnumParticleTypes.CRIT, livingTarget.posX, livingTarget.posY + (double)(livingTarget.height / 2.0F), livingTarget.posZ, 5 + this.comboHitCount * 2, 0.2, 0.2, 0.2, 0.05, new int[0]);
               }
            }

            if (isHeavyHit) {
               this.comboHitCount = 0;
               this.comboResetTimer = 0;
               this.nextAttackTimer = 30;
               this.inActiveCombo = false;
               this.activeComboTarget = null;
            } else {
               int delayIndex = Math.min(this.comboHitCount, COMBO_DELAYS.length - 1);
               this.nextAttackTimer = COMBO_DELAYS[delayIndex];
               ++this.comboHitCount;
               this.comboResetTimer = 60;
            }
         } else {
            this.inActiveCombo = false;
            this.activeComboTarget = null;
            this.comboHitCount = 0;
         }

      }

      private void sendScreenShake(EntityPlayerMP player, float intensity, int durationTicks) {
         double shakeX = (this.rand.nextDouble() - (double)0.5F) * (double)intensity * (double)0.5F;
         double shakeY = this.rand.nextDouble() * (double)intensity * 0.2;
         double shakeZ = (this.rand.nextDouble() - (double)0.5F) * (double)intensity * (double)0.5F;
         player.motionX += shakeX;
         player.motionY += shakeY;
         player.motionZ += shakeZ;
         player.velocityChanged = true;
         if (intensity > 0.3F) {
         }

      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         if (this.damageImmune) {
            if (source.getTrueSource() instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer)source.getTrueSource();
               player.sendStatusMessage(new TextComponentString("§c" + this.getBossDisplayName() + " is immune! " + this.immunityReason), true);
            }

            return false;
         } else {
            float modifiedAmount = amount * this.enrageDamageMultiplier;
            return super.attackEntityFrom(source, modifiedAmount);
         }
      }

      public void setDamageImmune(boolean immune, String reason) {
         this.damageImmune = immune;
         this.immunityReason = reason != null ? reason : "";
      }

      public void onUpdate() {
         super.onUpdate();
         if (!this.world.isRemote) {
            if (!this.getActivePotionEffects().isEmpty()) {
               this.clearActivePotions();
            }

            this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
            if (this.phaseController != null && this.raidInstance != null) {
               this.phaseController.tick(this.raidInstance);
               float healthDiff = Math.abs(this.getHealth() - this.lastSyncedHealth);
               if (healthDiff > this.getMaxHealth() * 0.01F || this.lastSyncedHealth < 0.0F) {
                  this.lastSyncedHealth = this.getHealth();
                  RaidNetworkHelper.sendHealthUpdate(this.raidInstance, this.getHealth(), this.getMaxHealth());
               }

               int currentPhase = this.phaseController.getCurrentPhaseNumber();
               if (currentPhase != this.lastSyncedPhase) {
                  this.lastSyncedPhase = currentPhase;
                  RaidNetworkHelper.sendPhaseChange(this.raidInstance, currentPhase, this.phaseController.getTotalPhases(), this.phaseController.getCurrentPhaseName());
               }
            }

            if (this.inSageMode) {
               ++this.sageModeTicks;
               if (this.sageModeTicks % 200 == 0) {
                  float healAmount = this.getMaxHealth() * 0.002F;
                  this.heal(healAmount);
               }
            }

            if (this.comboResetTimer > 0) {
               --this.comboResetTimer;
               if (this.comboResetTimer <= 0) {
                  this.comboHitCount = 0;
                  this.lastComboTarget = null;
                  this.inActiveCombo = false;
                  this.activeComboTarget = null;
               }
            }

            if (this.nextAttackTimer > 0) {
               --this.nextAttackTimer;
            }

            if (this.inActiveCombo && this.nextAttackTimer <= 0 && this.activeComboTarget != null) {
               if (!this.activeComboTarget.isDead && this.getDistanceSq(this.activeComboTarget) <= (double)9.0F) {
                  this.performComboAttack(this.activeComboTarget);
               } else {
                  this.inActiveCombo = false;
                  this.activeComboTarget = null;
                  this.comboHitCount = 0;
               }
            }

            if (this.chakraDashCooldown > 0) {
               --this.chakraDashCooldown;
            }

            if (this.isDashing && this.dashTicksRemaining > 0) {
               this.processDashTick();
            } else if (!this.isDashing) {
               this.tryChakraDash();
            }

            if (this.arena != null && !this.arena.isInBounds(this.posX, this.posY, this.posZ)) {
               if (this.isDashing) {
                  this.isDashing = false;
                  this.dashTicksRemaining = 0;
                  this.dashVelocityX = (double)0.0F;
                  this.dashVelocityY = (double)0.0F;
                  this.dashVelocityZ = (double)0.0F;
               }

               AxisAlignedBB bounds = this.arena.getBounds();
               double clampedX = Math.max(bounds.minX + (double)1.0F, Math.min(bounds.maxX - (double)1.0F, this.posX));
               double clampedZ = Math.max(bounds.minZ + (double)1.0F, Math.min(bounds.maxZ - (double)1.0F, this.posZ));
               this.setPositionAndUpdate(clampedX, this.posY, clampedZ);
               this.motionX = (double)0.0F;
               this.motionZ = (double)0.0F;
               this.velocityChanged = true;
            }

            if (this.arena != null) {
               double floorY = this.arena.getBounds().minY;
               if (this.posY < floorY - (double)2.0F) {
                  BlockPos bossSpawn = this.arena.getBossSpawn();
                  this.setPositionAndUpdate((double)bossSpawn.getX() + (double)0.5F, (double)bossSpawn.getY(), (double)bossSpawn.getZ() + (double)0.5F);
                  this.motionX = (double)0.0F;
                  this.motionY = (double)0.0F;
                  this.motionZ = (double)0.0F;
               }
            }

            if (!this.isDashing && this.ticksExisted % 5 == 0) {
               EntityLivingBase chaseTarget = this.getAttackTarget();
               if (chaseTarget != null && chaseTarget.isEntityAlive()) {
                  double chaseDist = (double)this.getDistance(chaseTarget);
                  if (chaseDist > (double)3.0F && chaseDist < (double)40.0F) {
                     double cdx = chaseTarget.posX - this.posX;
                     double cdz = chaseTarget.posZ - this.posZ;
                     double cHorizDist = Math.sqrt(cdx * cdx + cdz * cdz);
                     if (cHorizDist > (double)0.0F) {
                        boolean targetInBounds = this.arena == null || this.arena.isInBounds(chaseTarget.posX, chaseTarget.posY, chaseTarget.posZ);
                        if (targetInBounds) {
                           double chaseSpeed = 0.35;
                           this.motionX = cdx / cHorizDist * chaseSpeed;
                           this.motionZ = cdz / cHorizDist * chaseSpeed;
                           this.velocityChanged = true;
                           double yawRad = Math.atan2(cdx, cdz);
                           this.rotationYaw = (float)(yawRad * (double)180.0F / Math.PI);
                           this.rotationYawHead = this.rotationYaw;
                        }
                     }
                  }
               }
            }

            if (this.targetSwitchCooldown > 0) {
               --this.targetSwitchCooldown;
            } else {
               this.tryTargetSwitch();
            }
         }

      }

      private void tryTargetSwitch() {
         if (this.raidInstance != null) {
            List<EntityPlayerMP> alivePlayers = new ArrayList();

            for(EntityPlayerMP player : this.raidInstance.getParticipants()) {
               if (player != null && !player.isDead && this.raidInstance.isPlayerAlive(player.getUniqueID())) {
                  alivePlayers.add(player);
               }
            }

            if (!alivePlayers.isEmpty()) {
               if (alivePlayers.size() == 1) {
                  if (this.getAttackTarget() == null || this.getAttackTarget() != alivePlayers.get(0)) {
                     this.setAttackTarget((EntityLivingBase)alivePlayers.get(0));
                     this.currentTargetUUID = ((EntityPlayerMP)alivePlayers.get(0)).getUniqueID();
                  }

                  this.targetSwitchCooldown = 100;
               } else {
                  float switchChance = 0.4F + (float)(alivePlayers.size() - 1) * 0.1F + (float)this.difficulty.ordinal() * 0.1F;
                  switchChance = Math.min(switchChance, 0.8F);
                  if (this.rand.nextFloat() < switchChance) {
                     EntityPlayerMP newTarget = null;
                     List<EntityPlayerMP> otherTargets = new ArrayList();

                     for(EntityPlayerMP player : alivePlayers) {
                        if (this.currentTargetUUID == null || !player.getUniqueID().equals(this.currentTargetUUID)) {
                           otherTargets.add(player);
                        }
                     }

                     if (!otherTargets.isEmpty()) {
                        newTarget = (EntityPlayerMP)otherTargets.get(this.rand.nextInt(otherTargets.size()));
                     } else {
                        newTarget = (EntityPlayerMP)alivePlayers.get(this.rand.nextInt(alivePlayers.size()));
                     }

                     if (newTarget != null) {
                        this.setAttackTarget(newTarget);
                        this.currentTargetUUID = newTarget.getUniqueID();
                     }
                  }

                  int cooldown = 100 - this.difficulty.ordinal() * 20;
                  this.targetSwitchCooldown = Math.max(40, cooldown);
               }
            }
         }
      }

      private void tryChakraDash() {
         if (this.chakraDashCooldown <= 0 && this.getAttackTarget() != null) {
            EntityLivingBase target = this.getAttackTarget();
            double distance = (double)this.getDistance(target);
            if (!(distance < (double)8.0F) && !(distance > (double)25.0F)) {
               float dashChance = 0.2F + (float)this.difficulty.ordinal() * 0.15F;
               if (!(this.rand.nextFloat() > dashChance)) {
                  this.performChakraDash(target);
               }
            }
         }
      }

      private void performChakraDash(EntityLivingBase target) {
         double dx = target.posX - this.posX;
         double dy = target.posY - this.posY;
         double dz = target.posZ - this.posZ;
         double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (distance > (double)0.0F) {
            double dashDistance = Math.min(distance - (double)2.0F, (double)20.0F);
            if (dashDistance <= (double)0.0F) {
               return;
            }

            double nx = dx / distance;
            double ny = dy / distance;
            double nz = dz / distance;
            if (this.arena != null) {
               double destX = this.posX + nx * dashDistance;
               double destZ = this.posZ + nz * dashDistance;
               AxisAlignedBB bounds = this.arena.getBounds();
               double margin = (double)1.0F;
               destX = Math.max(bounds.minX + margin, Math.min(bounds.maxX - margin, destX));
               destZ = Math.max(bounds.minZ + margin, Math.min(bounds.maxZ - margin, destZ));
               double clampedDx = destX - this.posX;
               double clampedDz = destZ - this.posZ;
               double clampedDist = Math.sqrt(clampedDx * clampedDx + dy * dy + clampedDz * clampedDz);
               if (clampedDist <= (double)1.0F) {
                  return;
               }

               dashDistance = Math.sqrt(clampedDx * clampedDx + clampedDz * clampedDz);
               double horizDist = Math.sqrt(clampedDx * clampedDx + clampedDz * clampedDz);
               if (horizDist > (double)0.0F) {
                  nx = clampedDx / horizDist;
                  nz = clampedDz / horizDist;
               }

               double totalDist = Math.sqrt(horizDist * horizDist + dy * dy);
               ny = totalDist > (double)0.0F ? dy / totalDist : (double)0.0F;
               dashDistance = Math.min(dashDistance, (double)20.0F);
            }

            double velocityPerTick = dashDistance / (double)10.0F;
            this.isDashing = true;
            this.dashTicksRemaining = 10;
            this.dashVelocityX = nx * velocityPerTick * (double)1.5F;
            this.dashVelocityY = ny * velocityPerTick * (double)1.5F * 0.3 + 0.15;
            this.dashVelocityZ = nz * velocityPerTick * (double)1.5F;
            this.motionY = 0.4;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + (double)0.5F, this.posZ, 30, 0.8, 0.8, 0.8, 0.15, new int[0]);
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 15, (double)0.5F, 0.3, (double)0.5F, 0.1, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.5F, 0.8F);
            int cooldown = 100;
            if (this.inSageMode) {
               cooldown = (int)((float)cooldown * 0.6F);
            }

            cooldown -= this.difficulty.ordinal() * 15;
            this.chakraDashCooldown = Math.max(40, cooldown);
         }

      }

      private void processDashTick() {
         --this.dashTicksRemaining;
         this.motionX = this.dashVelocityX;
         this.motionZ = this.dashVelocityZ;
         if (this.dashTicksRemaining > 5) {
            this.motionY = this.dashVelocityY;
         } else {
            this.motionY = Math.max((double)-0.5F, this.motionY - 0.08);
         }

         this.velocityChanged = true;
         if (this.arena != null && !this.arena.isInBounds(this.posX, this.posY, this.posZ)) {
            this.isDashing = false;
            this.dashTicksRemaining = 0;
            this.dashVelocityX = (double)0.0F;
            this.dashVelocityY = (double)0.0F;
            this.dashVelocityZ = (double)0.0F;
            this.motionX = (double)0.0F;
            this.motionY = (double)0.0F;
            this.motionZ = (double)0.0F;
            this.velocityChanged = true;
            AxisAlignedBB bounds = this.arena.getBounds();
            double margin = (double)1.0F;
            this.posX = Math.max(bounds.minX + margin, Math.min(bounds.maxX - margin, this.posX));
            this.posZ = Math.max(bounds.minZ + margin, Math.min(bounds.maxZ - margin, this.posZ));
            this.setPosition(this.posX, this.posY, this.posZ);
         } else {
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + (double)1.0F, this.posZ, 3, 0.2, 0.3, 0.2, 0.02, new int[0]);
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX - this.dashVelocityX * (double)0.5F, this.posY + (double)0.5F, this.posZ - this.dashVelocityZ * (double)0.5F, 2, 0.1, 0.1, 0.1, 0.01, new int[0]);
            }

            if (this.dashTicksRemaining <= 0) {
               this.isDashing = false;
               this.dashVelocityX = (double)0.0F;
               this.dashVelocityY = (double)0.0F;
               this.dashVelocityZ = (double)0.0F;
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  EnumParticleTypes var10001 = EnumParticleTypes.BLOCK_DUST;
                  double var10002 = this.posX;
                  double var10003 = this.posY + 0.1;
                  double var10004 = this.posZ;
                  int[] var10010 = new int[1];
                  BlockGrass var10013 = Blocks.GRASS;
                  var10010[0] = BlockGrass.getStateId(Blocks.GRASS.getDefaultState());
                  ws.spawnParticle(var10001, var10002, var10003, var10004, 20, (double)0.5F, 0.1, (double)0.5F, 0.1, var10010);
               }

               this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_PLAYER_SMALL_FALL, SoundCategory.HOSTILE, 1.0F, 1.0F);
            }

         }
      }

      public void onPhaseTransition(int oldPhase, int newPhase, BossPhase oldPhaseObj, BossPhase newPhaseObj) {
         if (this.raidInstance != null) {
            this.raidInstance.broadcastMessage("§e§l[PHASE " + newPhase + "] " + newPhaseObj.getName());
         }

         this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 1.5F, 1.2F);
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

      public void clearBossBar() {
         this.bossInfo.setVisible(false);

         for(EntityPlayerMP player : new ArrayList(this.bossInfo.getPlayers())) {
            this.bossInfo.removePlayer(player);
         }

      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("difficulty", this.difficulty.ordinal());
         compound.setBoolean("damageImmune", this.damageImmune);
         compound.setString("immunityReason", this.immunityReason);
         compound.setInteger("enrageLevel", this.enrageLevel);
         compound.setBoolean("inSageMode", this.inSageMode);
         compound.setInteger("sageModeTicks", this.sageModeTicks);
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

         this.inSageMode = compound.getBoolean("inSageMode");
         this.sageModeTicks = compound.getInteger("sageModeTicks");
         this.dataManager.set(SAGE_MODE, this.inSageMode);
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

      public void setDead() {
         super.setDead();
         this.clearBossBar();
      }

      public boolean isNonBoss() {
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

      public void addPotionEffect(PotionEffect effect) {
      }

      public boolean isPotionApplicable(PotionEffect effect) {
         return false;
      }

      public void clearActivePotions() {
         super.clearActivePotions();
      }

      public void setFire(int seconds) {
      }

      public RaidInstance getRaidInstance() {
         return this.raidInstance;
      }

      public void setRaidInstance(RaidInstance instance) {
         this.raidInstance = instance;
         if (instance != null) {
            this.arena = instance.getArena();

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

      public boolean isInSageMode() {
         return (Boolean)this.dataManager.get(SAGE_MODE);
      }

      public boolean isWoodHumanActive() {
         return this.woodHumanActive;
      }

      public boolean isFacingLocked() {
         return this.facingLocked;
      }

      public void setFacingLocked(boolean locked, float yaw) {
         this.facingLocked = locked;
         if (locked) {
            this.lockedYaw = yaw;
            this.lockedHeadYaw = yaw;
            this.rotationYaw = yaw;
            this.prevRotationYaw = yaw;
            this.renderYawOffset = yaw;
            this.rotationYawHead = yaw;
            this.prevRotationYawHead = yaw;
            this.prevRenderYawOffset = yaw;
         }

      }

      public void setRotationYawHead(float rotation) {
         if (this.facingLocked) {
            super.setRotationYawHead(this.lockedHeadYaw);
         } else {
            super.setRotationYawHead(rotation);
         }

      }

      public void onLivingUpdate() {
         super.onLivingUpdate();
         if (this.facingLocked) {
            this.rotationYaw = this.lockedYaw;
            this.prevRotationYaw = this.lockedYaw;
            this.renderYawOffset = this.lockedYaw;
            this.rotationYawHead = this.lockedHeadYaw;
            this.prevRotationYawHead = this.lockedHeadYaw;
            this.prevRenderYawOffset = this.lockedYaw;
         }

         if (!this.world.isRemote) {
            if (this.ticksExisted % 10 == 0) {
               EntityLivingBase dome = this.findNearbyIceDome((double)20.0F);
               if (dome != null && dome.isEntityAlive()) {
                  this.setAttackTarget(dome);
                  if ((double)this.getDistance(dome) <= (double)4.0F) {
                     dome.hurtResistantTime = 0;
                     dome.attackEntityFrom(DamageSource.causeMobDamage(this), 160.0F);
                     this.swingArm(EnumHand.MAIN_HAND);
                  }
               }
            }

            if (!this.escapeFromBlock()) {
               if (!this.isDashing && (Math.abs(this.motionX) > (double)0.5F || Math.abs(this.motionZ) > (double)0.5F || this.motionY > (double)0.5F)) {
                  this.motionX *= 0.05;
                  this.motionZ *= 0.05;
                  if (this.motionY > (double)0.5F) {
                     this.motionY = 0.05;
                  }

                  this.velocityChanged = true;
               }

               Entity trappingEntity = this.findNearbyTrapProjectile();
               if (trappingEntity != null) {
                  ++this.trapEscapeTick;
                  if (this.trapEscapeTick >= 2) {
                     this.trapEscapeTick = 0;
                     this.nudgeTowardTarget((double)2.0F);
                  }
               } else {
                  this.trapEscapeTick = 0;
                  EntityLivingBase stuckTarget = this.getAttackTarget();
                  double targetDist = stuckTarget != null ? (double)this.getDistance(stuckTarget) : (double)999.0F;
                  if (targetDist > (double)4.0F) {
                     double movedX = Math.abs(this.posX - this.prevTrackPosX);
                     double movedZ = Math.abs(this.posZ - this.prevTrackPosZ);
                     if (movedX < 0.005 && movedZ < 0.005 && stuckTarget != null) {
                        ++this.stuckTicks;
                        if (this.stuckTicks >= 4) {
                           this.nudgeTowardTarget((double)2.0F);
                           this.stuckTicks = 0;
                        }
                     } else {
                        this.stuckTicks = 0;
                     }
                  } else {
                     this.stuckTicks = 0;
                  }
               }
            }

            this.prevTrackPosX = this.posX;
            this.prevTrackPosZ = this.posZ;
         }

      }

      public void faceEntity(Entity entityIn, float maxYawIncrease, float maxPitchIncrease) {
         if (!this.facingLocked) {
            super.faceEntity(entityIn, maxYawIncrease, maxPitchIncrease);
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

      private boolean escapeFromBlock() {
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
               boolean testFootClear = !this.world.getBlockState(testFoot).getMaterial().isSolid();
               boolean testHeadClear = !this.world.getBlockState(testHead).getMaterial().isSolid();
               boolean testHasFloor = this.world.getBlockState(testBelow).getMaterial().isSolid();
               if (testFootClear && testHeadClear && testHasFloor && (this.arena == null || this.arena.isInBounds(tx, ty, tz))) {
                  double destX = Math.floor(tx) + (double)0.5F;
                  double destZ = Math.floor(tz) + (double)0.5F;
                  this.setPositionAndUpdate(destX, ty, destZ);
                  this.motionX = (double)0.0F;
                  this.motionY = (double)0.0F;
                  this.motionZ = (double)0.0F;
                  return true;
               }
            }

            if (this.arena != null) {
               BlockPos center = this.arena.getCenter();
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

      private void nudgeTowardTarget(double distance) {
         EntityLivingBase target = this.getAttackTarget();
         if (target != null) {
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (!(dist < (double)1.0F)) {
               double maxNudge = Math.max((double)0.0F, dist - (double)2.0F);
               double actualDistance = Math.min(distance, maxNudge);
               if (!(actualDistance < (double)0.5F)) {
                  double moveX = this.posX + dx / dist * actualDistance;
                  double moveZ = this.posZ + dz / dist * actualDistance;
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
                                 AxisAlignedBB destBox = this.getEntityBoundingBox().offset(moveX - this.posX, (double)0.0F, moveZ - this.posZ);
                                 List<EntityPlayer> playersAtDest = this.world.getEntitiesWithinAABB(EntityPlayer.class, destBox);
                                 if (playersAtDest.isEmpty()) {
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
         }
      }

      public World getWorld() {
         return this.world;
      }

      static {
         SAGE_MODE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
         COMBO_DELAYS = new int[]{0, 6, 8, 9, 10};
      }
   }
}
