
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.raid.arena.RaidArena;
import net.luck.narutoaddon.OtherCode.raid.boss.BossPhase;
import net.luck.narutoaddon.OtherCode.raid.boss.BossPhaseController;
import net.luck.narutoaddon.OtherCode.raid.boss.IRaidBoss;
import net.luck.narutoaddon.OtherCode.raid.boss.bosses.kimimaro.KimimaroMechanics;
import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.luck.narutoaddon.OtherCode.raid.network.RaidNetworkHelper;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityRaidBossKimimaro extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 501;
   public static final int ENTITYID_RANGED = 502;

   public EntityRaidBossKimimaro(ElementsInfTsukAddon instance) {
      super(instance, 501);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "raid_kimimaro"), 501).name("inftsuk_raid_kimimaro").tracker(128, 1, true).egg(-1, -1).build());
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
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KimimaroRaidRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class KimimaroRaidRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation KIMIMARO_TEXTURE = new ResourceLocation("inftsukaddon:textures/kimimaro.png");
      private static final ResourceLocation KIMIMARO_CM1_TEXTURE = new ResourceLocation("inftsukaddon:textures/kimimaro_stage1.png");
      private static final ResourceLocation KIMIMARO_CM2_TEXTURE = new ResourceLocation("inftsukaddon:textures/kimimaro_stage2.png");

      public KimimaroRaidRenderer(RenderManager renderManager) {
         super(renderManager, new ModelPlayer(0.0F, false), 0.5F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         int curseMarkStage = entity.getCurseMarkStage();
         if (curseMarkStage >= 2) {
            return KIMIMARO_CM2_TEXTURE;
         } else {
            return curseMarkStage >= 1 ? KIMIMARO_CM1_TEXTURE : KIMIMARO_TEXTURE;
         }
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         float scale = 1.2F;
         GlStateManager.scale(scale, scale, scale);
      }
   }

   public static class EntityCustom extends EntityMob implements IRaidBoss {
      private static final DataParameter<Integer> CURSE_MARK_STAGE;
      private static Field maximumValueField;
      private static final float KIMIMARO_BASE_HEALTH = 35000.0F;
      private static final float KIMIMARO_BASE_DAMAGE = 14.0F;
      private static final float KIMIMARO_BASE_ARMOR = 12.0F;
      private static final float BASE_SPEED = 0.4F;
      private static final float SPEED_PER_DIFFICULTY = 0.04F;
      private static final float GENIN_PHASE_2 = 50.0F;
      private static final float GENIN_PHASE_3 = 25.0F;
      private static final float CHUNIN_PHASE_2 = 65.0F;
      private static final float CHUNIN_PHASE_3 = 35.0F;
      private static final float CHUNIN_PHASE_4 = 15.0F;
      private static final float JONIN_PHASE_2 = 65.0F;
      private static final float JONIN_PHASE_3 = 35.0F;
      private static final float JONIN_PHASE_4 = 15.0F;
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
      private int curseMarkStage;
      private boolean boneArmorActive;
      private float boneArmorDR;
      private double prevTrackPosX;
      private double prevTrackPosZ;
      private int stuckTicks;
      private int trapEscapeTick;
      private int chakraDashCooldown;
      private static final int CHAKRA_DASH_COOLDOWN_BASE = 80;
      private static final float CHAKRA_DASH_RANGE = 6.0F;
      private static final float CHAKRA_DASH_MAX_RANGE = 22.0F;
      private static final float CHAKRA_DASH_SPEED = 1.6F;
      private boolean isDashing;
      private int dashTicksRemaining;
      private static final int DASH_DURATION_TICKS = 8;
      private double dashVelocityX;
      private double dashVelocityY;
      private double dashVelocityZ;
      private float lastSyncedHealth;
      private int lastSyncedPhase;
      private int targetSwitchCooldown;
      private static final int TARGET_SWITCH_COOLDOWN_BASE = 80;
      private static final int TARGET_SWITCH_COOLDOWN_MIN = 30;
      private UUID currentTargetUUID;
      private static Item BONE_SWORD_ITEM;
      private static Item BONE_DRILL_ITEM;
      private static boolean weaponItemsResolved;
      private int comboHitCount;
      private int comboResetTimer;
      private int nextAttackTimer;
      private static final int COMBO_RESET_TIME = 50;
      private static final int COMBO_MAX_HITS = 5;
      private Entity lastComboTarget;
      private Entity activeComboTarget;
      private boolean inActiveCombo;
      private static final int[] COMBO_DELAYS;

      private static void resolveWeaponItems() {
         if (!weaponItemsResolved) {
            weaponItemsResolved = true;

            for(Item item : ForgeRegistries.ITEMS) {
               ResourceLocation rl = item.getRegistryName();
               if (rl != null) {
                  String key = rl.toString();
                  if (key.equals("narutomod:bone_sword")) {
                     BONE_SWORD_ITEM = item;
                  } else if (key.equals("narutomod:bone_drill")) {
                     BONE_DRILL_ITEM = item;
                  }

                  if (BONE_SWORD_ITEM != null && BONE_DRILL_ITEM != null) {
                     break;
                  }
               }
            }

            if (BONE_SWORD_ITEM == null) {
               System.out.println("[RaidBoss Kimimaro] WARNING: narutomod:bone_sword not found in registry!");
            }

            if (BONE_DRILL_ITEM == null) {
               System.out.println("[RaidBoss Kimimaro] WARNING: narutomod:bone_drill not found in registry!");
            }

         }
      }

      public EntityCustom(World world) {
         super(world);
         this.difficulty = RaidDifficulty.GENIN;
         this.damageImmune = false;
         this.immunityReason = "";
         this.enrageLevel = 0;
         this.enrageDamageMultiplier = 1.0F;
         this.enrageSpeedMultiplier = 1.0F;
         this.curseMarkStage = 0;
         this.boneArmorActive = false;
         this.boneArmorDR = 0.0F;
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
         this.bossInfo = new BossInfoServer(new TextComponentString(this.getBossDisplayName()), Color.WHITE, Overlay.PROGRESS);
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(CURSE_MARK_STAGE, 0);
      }

      public String getBossId() {
         return "kimimaro";
      }

      public String getBossDisplayName() {
         String name = "Kimimaro Kaguya";
         if (this.curseMarkStage == 1) {
            name = name + " (Curse Mark 1)";
         } else if (this.curseMarkStage >= 2) {
            name = name + " (Curse Mark 2)";
         }

         return name;
      }

      public ResourceLocation getBossBarTexture() {
         return new ResourceLocation("inftsukaddon", "textures/gui/boss_bar_kimimaro.png");
      }

      protected void initEntityAI() {
         super.initEntityAI();
         this.tasks.addTask(0, new EntityAISwimming(this));
         this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.3, false));
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
         raiseHealthCap((double)105000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)35000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)12.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.4F);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)14.0F);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)64.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
      }

      private static void raiseHealthCap(double newMax) {
         try {
            if (maximumValueField == null) {
               maximumValueField = RangedAttribute.class.getDeclaredField("maximumValue");
               maximumValueField.setAccessible(true);
            }

            RangedAttribute attr = (RangedAttribute)SharedMonsterAttributes.MAX_HEALTH;
            double currentMax = maximumValueField.getDouble(attr);
            if (currentMax < newMax) {
               maximumValueField.setDouble(attr, newMax);
            }
         } catch (Exception var7) {
            try {
               if (maximumValueField == null) {
                  maximumValueField = RangedAttribute.class.getDeclaredField("maximumValue");
                  maximumValueField.setAccessible(true);
               }

               RangedAttribute attr = (RangedAttribute)SharedMonsterAttributes.MAX_HEALTH;
               double currentMax = maximumValueField.getDouble(attr);
               if (currentMax < newMax) {
                  maximumValueField.setDouble(attr, newMax);
               }
            } catch (Exception e2) {
               System.err.println("[InfTsukAddon] WARNING: Could not raise MAX_HEALTH cap for Kimimaro. Health may be capped. " + e2.getMessage());
            }
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
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)(14.0F * damageMult));
         float speedWithDifficulty = 0.4F + 0.04F * (float)this.difficulty.ordinal();
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
               this.initAnbuPhases();
         }

      }

      private void initGeninPhases() {
         this.phaseController.addPhase((new BossPhase.Builder()).name("Dance of the Willows").type(BossPhase.PhaseType.DAMAGE).healthThreshold(100.0F).addMechanic(KimimaroMechanics.createFingerBulletBarrage(this.difficulty)).addMechanic(KimimaroMechanics.createBoneSpikeField(this.difficulty)).onStart(() -> {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§7\"I am Kimimaro, of the Kaguya clan. I will not hold back.\"");
            }

         }).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Curse Mark: First State").type(BossPhase.PhaseType.DAMAGE).healthThreshold(50.0F).addMechanic(KimimaroMechanics.createClematisfVineSweep(this.difficulty)).addMechanic(KimimaroMechanics.createFingerBulletBarrage(this.difficulty)).addMechanic(KimimaroMechanics.createBoneSpikeField(this.difficulty)).speedMultiplier(1.15F).damageMultiplier(1.15F).onStart(() -> this.activateCurseMark1()).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Dance of the Seedling Fern").type(BossPhase.PhaseType.DAMAGE).healthThreshold(25.0F).addMechanic(KimimaroMechanics.createSeedlingFern(this.difficulty)).addMechanic(KimimaroMechanics.createClematisfVineSweep(this.difficulty)).addMechanic(KimimaroMechanics.createFingerBulletBarrage(this.difficulty)).speedMultiplier(1.2F).damageMultiplier(1.25F).onStart(() -> {
            this.activateCurseMark2();
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§c§l[ENRAGED] Kimimaro enters his final dance!");
            }

         }).build());
      }

      private void initChuninPhases() {
         this.phaseController.addPhase((new BossPhase.Builder()).name("Dance of the Willows").type(BossPhase.PhaseType.DAMAGE).healthThreshold(100.0F).addMechanic(KimimaroMechanics.createFingerBulletBarrage(this.difficulty)).addMechanic(KimimaroMechanics.createBoneSpikeField(this.difficulty)).onStart(() -> {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§7\"My bones are my weapons. My body is my shield.\"");
            }

         }).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Curse Mark: First State").type(BossPhase.PhaseType.DAMAGE).healthThreshold(65.0F).addMechanic(KimimaroMechanics.createClematisfVineSweep(this.difficulty)).addMechanic(KimimaroMechanics.createBoneDrillCharge(this.difficulty)).addMechanic(KimimaroMechanics.createFingerBulletBarrage(this.difficulty)).speedMultiplier(1.15F).onStart(() -> this.activateCurseMark1()).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Curse Mark: Second State").type(BossPhase.PhaseType.DAMAGE).healthThreshold(35.0F).addMechanic(KimimaroMechanics.createClematisflowerLunge(this.difficulty)).addMechanic(KimimaroMechanics.createBoneForest(this.difficulty)).addMechanic(KimimaroMechanics.createEnhancedFingerBullets(this.difficulty)).damageMultiplier(1.25F).speedMultiplier(1.1F).onStart(() -> this.activateCurseMark2()).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Dance of the Seedling Fern").type(BossPhase.PhaseType.DAMAGE).healthThreshold(15.0F).addMechanic(KimimaroMechanics.createSeedlingFern(this.difficulty)).addMechanic(KimimaroMechanics.createBoneCage(this.difficulty)).addMechanic(KimimaroMechanics.createDigitalShrapnel(this.difficulty)).damageMultiplier(1.35F).speedMultiplier(1.2F).onStart(() -> {
            this.setBoneArmorDR(0.35F);
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§4§lKimimaro: §7\"This is... my final dance. For Lord Orochimaru!\"");
            }

         }).build());
      }

      private void initAnbuPhases() {
         this.phaseController.addPhase((new BossPhase.Builder()).name("Dance of the Willows").type(BossPhase.PhaseType.DAMAGE).healthThreshold(100.0F).addMechanic(KimimaroMechanics.createFingerBulletBarrage(this.difficulty)).addMechanic(KimimaroMechanics.createBoneSpikeField(this.difficulty)).onStart(() -> {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§7\"My bones are my weapons. My body is my shield.\"");
            }

         }).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Curse Mark: First State").type(BossPhase.PhaseType.DAMAGE).healthThreshold(65.0F).addMechanic(KimimaroMechanics.createClematisfVineSweep(this.difficulty)).addMechanic(KimimaroMechanics.createBoneDrillCharge(this.difficulty)).addMechanic(KimimaroMechanics.createFingerBulletBarrage(this.difficulty)).speedMultiplier(1.15F).onStart(() -> this.activateCurseMark1()).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Curse Mark: Second State").type(BossPhase.PhaseType.DAMAGE).healthThreshold(35.0F).addMechanic(KimimaroMechanics.createClematisflowerLunge(this.difficulty)).addMechanic(KimimaroMechanics.createBoneForest(this.difficulty)).addMechanic(KimimaroMechanics.createEnhancedFingerBullets(this.difficulty)).damageMultiplier(1.25F).speedMultiplier(1.1F).onStart(() -> this.activateCurseMark2()).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Dance of the Seedling Fern").type(BossPhase.PhaseType.DAMAGE).healthThreshold(15.0F).addMechanic(KimimaroMechanics.createSeedlingFern(this.difficulty)).addMechanic(KimimaroMechanics.createDigitalShrapnel(this.difficulty)).addMechanic(KimimaroMechanics.createClematisflowerLunge(this.difficulty)).addMechanic(KimimaroMechanics.createBoneForest(this.difficulty)).damageMultiplier(1.35F).speedMultiplier(1.2F).onStart(() -> {
            this.setBoneArmorDR(0.35F);
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§4§lKimimaro: §7\"This is... my final dance. For Lord Orochimaru!\"");
            }

         }).build());
      }

      private void initJoninPhases() {
         this.phaseController.addPhase((new BossPhase.Builder()).name("Dance of the Willows").type(BossPhase.PhaseType.DAMAGE).healthThreshold(100.0F).addMechanic(KimimaroMechanics.createFingerBulletBarrage(this.difficulty)).addMechanic(KimimaroMechanics.createBoneSpikeField(this.difficulty)).onStart(() -> {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§7\"The Kaguya clan's kekkei genkai... Shikotsumyaku. I'll show you its full power.\"");
            }

         }).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Curse Mark: First State").type(BossPhase.PhaseType.DAMAGE).healthThreshold(75.0F).addMechanic(KimimaroMechanics.createClematisfVineSweep(this.difficulty)).addMechanic(KimimaroMechanics.createBoneDrillCharge(this.difficulty)).addMechanic(KimimaroMechanics.createFingerBulletBarrage(this.difficulty)).addMechanic(KimimaroMechanics.createBoneSpikeField(this.difficulty)).speedMultiplier(1.15F).onStart(() -> this.activateCurseMark1()).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Curse Mark: Second State").type(BossPhase.PhaseType.DAMAGE).healthThreshold(50.0F).addMechanic(KimimaroMechanics.createClematisflowerLunge(this.difficulty)).addMechanic(KimimaroMechanics.createBoneForest(this.difficulty)).addMechanic(KimimaroMechanics.createEnhancedFingerBullets(this.difficulty)).addMechanic(KimimaroMechanics.createBoneDrillCharge(this.difficulty)).addMechanic(KimimaroMechanics.createFingerBulletBarrage(this.difficulty)).damageMultiplier(1.25F).speedMultiplier(1.15F).onStart(() -> this.activateCurseMark2()).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Dance of the Clematis").type(BossPhase.PhaseType.DAMAGE).healthThreshold(30.0F).addMechanic(KimimaroMechanics.createClematisflowerLunge(this.difficulty)).addMechanic(KimimaroMechanics.createBoneCage(this.difficulty)).addMechanic(KimimaroMechanics.createBoneForest(this.difficulty)).addMechanic(KimimaroMechanics.createEnhancedFingerBullets(this.difficulty)).addMechanic(KimimaroMechanics.createDigitalShrapnel(this.difficulty)).damageMultiplier(1.35F).speedMultiplier(1.2F).onStart(() -> {
            this.setBoneArmorDR(0.25F);
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§5§lKimimaro: §7\"The curse mark... I will use everything for Lord Orochimaru.\"");
            }

         }).build());
         this.phaseController.addPhase((new BossPhase.Builder()).name("Dance of the Seedling Fern").type(BossPhase.PhaseType.DAMAGE).healthThreshold(15.0F).addMechanic(KimimaroMechanics.createSeedlingFern(this.difficulty)).addMechanic(KimimaroMechanics.createBoneCage(this.difficulty)).addMechanic(KimimaroMechanics.createDigitalShrapnel(this.difficulty)).addMechanic(KimimaroMechanics.createClematisflowerLunge(this.difficulty)).addMechanic(KimimaroMechanics.createBoneForest(this.difficulty)).damageMultiplier(1.5F).speedMultiplier(1.3F).onStart(() -> {
            this.setBoneArmorDR(0.35F);
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§4§lKimimaro: §7\"This body... is my weapon. I will not fall!\"");
               this.raidInstance.broadcastMessage("§c§l[FINAL PHASE] Kimimaro performs the ultimate Shikotsumyaku dance!");
            }

         }).build());
      }

      @Nullable
      public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
         livingdata = super.onInitialSpawn(difficulty, livingdata);
         this.equipBoneSword();
         return livingdata;
      }

      private void equipBoneSword() {
         resolveWeaponItems();
         if (BONE_SWORD_ITEM != null) {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(BONE_SWORD_ITEM));
            this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
         }

      }

      private void equipBoneDrill() {
         resolveWeaponItems();
         if (BONE_DRILL_ITEM != null) {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(BONE_DRILL_ITEM));
            this.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
         }

      }

      private void activateCurseMark1() {
         this.equipBoneDrill();
         if (this.curseMarkStage < 1) {
            this.curseMarkStage = 1;
            this.dataManager.set(CURSE_MARK_STAGE, 1);
            double currentSpeed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(currentSpeed * 1.15);
            this.boneArmorActive = true;
            this.boneArmorDR = 0.15F;
            this.bossInfo.setName(new TextComponentString(this.getBossDisplayName()));
            this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 2.0F, 1.5F);
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int i = 0; i < 50; ++i) {
                  ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, this.posY + this.rand.nextDouble() * (double)3.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{0});
               }

               for(int i = 0; i < 30; ++i) {
                  ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, this.posY + this.rand.nextDouble() * (double)2.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{0});
               }
            }

            this.pushPlayersAway((double)5.0F, 0.7, (double)0.25F);
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§5§lKimimaro: §7\"The curse mark... I will use everything.\"");
               this.raidInstance.broadcastMessage("§e§lKimimaro's attacks now reflect bone spike damage!");
            }
         }

      }

      private void activateCurseMark2() {
         this.equipBoneDrill();
         if (this.curseMarkStage < 2) {
            this.curseMarkStage = 2;
            this.dataManager.set(CURSE_MARK_STAGE, 2);
            double currentArmor = this.getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue();
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(currentArmor * (double)2.0F);
            this.boneArmorActive = true;
            this.boneArmorDR = 0.25F;
            this.bossInfo.setName(new TextComponentString(this.getBossDisplayName()));
            this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 2.0F, 0.8F);
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(int i = 0; i < 70; ++i) {
                  ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)7.0F, this.posY + this.rand.nextDouble() * (double)4.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)7.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{0});
               }

               for(int i = 0; i < 40; ++i) {
                  ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F, this.posY + this.rand.nextDouble() * (double)3.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{0});
               }

               for(int i = 0; i < 25; ++i) {
                  ws.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, this.posY + this.rand.nextDouble() * (double)3.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{0});
               }
            }

            this.pushPlayersAway((double)7.0F, (double)1.0F, 0.4);
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§4§lKimimaro: §7\"This body... is my weapon!\"");
               this.raidInstance.broadcastMessage("§e§lKimimaro's armor has drastically increased!");
            }
         }

      }

      private void pushPlayersAway(double radius, double strength, double upY) {
         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(radius))) {
            if (p.isEntityAlive() && !p.isSpectator()) {
               double dx = p.posX - this.posX;
               double dz = p.posZ - this.posZ;
               double d = Math.sqrt(dx * dx + dz * dz);
               if (d > (double)0.0F) {
                  p.motionX += dx / d * strength;
                  p.motionY += upY;
                  p.motionZ += dz / d * strength;
                  if (p instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)p).velocityChanged = true;
                  }
               }
            }
         }

      }

      public void setBoneArmorDR(float dr) {
         this.boneArmorDR = dr;
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

            double baseSpeed = (double)(0.4F * this.enrageSpeedMultiplier);
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(baseSpeed);
            this.onEnrage(level);
         }

      }

      protected void onEnrage(int newLevel) {
         if (newLevel == 1) {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§7\"You're wasting my time. I must fulfill my purpose for Lord Orochimaru!\"");
            }
         } else if (newLevel == 2) {
            if (this.raidInstance != null) {
               this.raidInstance.broadcastMessage("§c§7\"I WILL NOT FALL! Not until my purpose is complete!\"");
            }

            if (this.curseMarkStage < 2) {
               this.activateCurseMark2();
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
            if (this.curseMarkStage >= 2) {
               attackDamage *= 1.2F;
            } else if (this.curseMarkStage >= 1) {
               attackDamage *= 1.1F;
            }

            boolean isHeavyHit = this.comboHitCount >= 4;
            if (isHeavyHit) {
               attackDamage *= 1.35F;
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
                  knockbackStrength = this.curseMarkStage >= 2 ? 1.0F : 0.7F;
               } else {
                  knockbackStrength = 0.05F;
               }

               livingTarget.knockBack(this, knockbackStrength, this.posX - livingTarget.posX, this.posZ - livingTarget.posZ);
               if (isHeavyHit) {
                  this.world.playSound((EntityPlayer)null, livingTarget.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.7F);
                  this.world.playSound((EntityPlayer)null, livingTarget.getPosition(), SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.HOSTILE, 1.0F, 0.8F);
               } else {
                  float pitch = 1.0F + (float)this.comboHitCount * 0.15F;
                  this.world.playSound((EntityPlayer)null, livingTarget.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.8F, pitch);
               }

               if (livingTarget instanceof EntityPlayerMP) {
                  EntityPlayerMP player = (EntityPlayerMP)livingTarget;
                  float shakeIntensity;
                  if (isHeavyHit) {
                     shakeIntensity = this.curseMarkStage >= 2 ? 0.8F : 0.5F;
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
                  this.nextAttackTimer = 25;
                  this.inActiveCombo = false;
                  this.activeComboTarget = null;
               } else {
                  int delayIndex = Math.min(this.comboHitCount, COMBO_DELAYS.length - 1);
                  this.nextAttackTimer = COMBO_DELAYS[delayIndex];
                  ++this.comboHitCount;
                  this.comboResetTimer = 50;
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
         if (this.curseMarkStage >= 2) {
            attackDamage *= 1.2F;
         } else if (this.curseMarkStage >= 1) {
            attackDamage *= 1.1F;
         }

         boolean isHeavyHit = this.comboHitCount >= 4;
         if (isHeavyHit) {
            attackDamage *= 1.35F;
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
               knockbackStrength = this.curseMarkStage >= 2 ? 1.0F : 0.7F;
            } else {
               knockbackStrength = 0.05F;
            }

            livingTarget.knockBack(this, knockbackStrength, this.posX - livingTarget.posX, this.posZ - livingTarget.posZ);
            if (isHeavyHit) {
               this.world.playSound((EntityPlayer)null, livingTarget.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.5F, 0.7F);
               this.world.playSound((EntityPlayer)null, livingTarget.getPosition(), SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.HOSTILE, 1.0F, 0.8F);
            } else {
               float pitch = 1.0F + (float)this.comboHitCount * 0.15F;
               this.world.playSound((EntityPlayer)null, livingTarget.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.8F, pitch);
            }

            if (livingTarget instanceof EntityPlayerMP) {
               EntityPlayerMP player = (EntityPlayerMP)livingTarget;
               float shakeIntensity;
               if (isHeavyHit) {
                  shakeIntensity = this.curseMarkStage >= 2 ? 0.8F : 0.5F;
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
               this.nextAttackTimer = 25;
               this.inActiveCombo = false;
               this.activeComboTarget = null;
            } else {
               int delayIndex = Math.min(this.comboHitCount, COMBO_DELAYS.length - 1);
               this.nextAttackTimer = COMBO_DELAYS[delayIndex];
               ++this.comboHitCount;
               this.comboResetTimer = 50;
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
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         if (this.damageImmune) {
            if (source.getTrueSource() instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer)source.getTrueSource();
               player.sendStatusMessage(new TextComponentString("§c" + this.getBossDisplayName() + " is immune! " + this.immunityReason), true);
            }

            return false;
         } else {
            if (this.boneArmorDR > 0.0F) {
               amount *= 1.0F - this.boneArmorDR;
            }

            float modifiedAmount = amount * this.enrageDamageMultiplier;
            boolean result = super.attackEntityFrom(source, modifiedAmount);
            if (result && this.boneArmorActive && !this.world.isRemote) {
               Entity attacker = source.getTrueSource();
               if (attacker instanceof EntityLivingBase && !source.isProjectile() && !source.isMagicDamage() && !source.isExplosion() && (double)this.getDistance(attacker) <= (double)4.0F) {
                  float thornsDmg = 3.0F + (float)this.curseMarkStage * 2.0F;
                  attacker.attackEntityFrom(DamageSource.causeThornsDamage(this), thornsDmg);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, attacker.posX, attacker.posY + (double)1.0F, attacker.posZ, 8, 0.3, 0.3, 0.3, 0.1, new int[0]);
                  }
               }
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
            if (this.ticksExisted == 1 && this.getHeldItemMainhand().isEmpty() && this.curseMarkStage == 0) {
               this.equipBoneSword();
            } else if (this.ticksExisted == 1 && this.getHeldItemMainhand().isEmpty() && this.curseMarkStage >= 1) {
               this.equipBoneDrill();
            }

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

                  this.targetSwitchCooldown = 80;
               } else {
                  float switchChance = 0.45F + (float)(alivePlayers.size() - 1) * 0.1F + (float)this.difficulty.ordinal() * 0.1F;
                  switchChance = Math.min(switchChance, 0.85F);
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

                  int cooldown = 80 - this.difficulty.ordinal() * 15;
                  this.targetSwitchCooldown = Math.max(30, cooldown);
               }
            }
         }
      }

      private void tryChakraDash() {
         if (this.chakraDashCooldown <= 0 && this.getAttackTarget() != null) {
            EntityLivingBase target = this.getAttackTarget();
            double distance = (double)this.getDistance(target);
            if (!(distance < (double)6.0F) && !(distance > (double)22.0F)) {
               float dashChance = 0.25F + (float)this.difficulty.ordinal() * 0.15F;
               if (this.curseMarkStage >= 1) {
                  dashChance += 0.1F;
               }

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
               if (dashDistance > (double)0.0F) {
                  nx = clampedDx / dashDistance;
                  nz = clampedDz / dashDistance;
               }

               double totalDist = Math.sqrt(dashDistance * dashDistance + dy * dy);
               ny = totalDist > (double)0.0F ? dy / totalDist : (double)0.0F;
               dashDistance = Math.min(dashDistance, (double)20.0F);
            }

            double velocityPerTick = dashDistance / (double)8.0F;
            this.isDashing = true;
            this.dashTicksRemaining = 8;
            this.dashVelocityX = nx * velocityPerTick * (double)1.6F;
            this.dashVelocityY = ny * velocityPerTick * (double)1.6F * 0.3 + 0.12;
            this.dashVelocityZ = nz * velocityPerTick * (double)1.6F;
            this.motionY = 0.35;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)0.5F, this.posZ, 30, 0.8, 0.8, 0.8, 0.15, new int[0]);
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 15, (double)0.5F, 0.3, (double)0.5F, 0.1, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.5F, 0.9F);
            int cooldown = 80;
            if (this.curseMarkStage >= 1) {
               cooldown = (int)((float)cooldown * 0.7F);
            }

            cooldown -= this.difficulty.ordinal() * 12;
            this.chakraDashCooldown = Math.max(30, cooldown);
         }

      }

      private void processDashTick() {
         --this.dashTicksRemaining;
         this.motionX = this.dashVelocityX;
         this.motionZ = this.dashVelocityZ;
         if (this.dashTicksRemaining > 4) {
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
               ws.spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)1.0F, this.posZ, 3, 0.2, 0.3, 0.2, 0.02, new int[0]);
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
                  Block var10013 = Blocks.BONE_BLOCK;
                  var10010[0] = Block.getStateId(Blocks.BONE_BLOCK.getDefaultState());
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
         compound.setInteger("curseMarkStage", this.curseMarkStage);
         compound.setBoolean("boneArmorActive", this.boneArmorActive);
         compound.setFloat("boneArmorDR", this.boneArmorDR);
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

         this.curseMarkStage = compound.getInteger("curseMarkStage");
         this.boneArmorActive = compound.getBoolean("boneArmorActive");
         this.boneArmorDR = compound.getFloat("boneArmorDR");
         this.dataManager.set(CURSE_MARK_STAGE, this.curseMarkStage);
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

      public int getCurseMarkStage() {
         return (Integer)this.dataManager.get(CURSE_MARK_STAGE);
      }

      public boolean isBoneArmorActive() {
         return this.boneArmorActive;
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

            if (!this.isDashing && (Math.abs(this.motionX) > 0.3 || Math.abs(this.motionZ) > 0.3 || this.motionY > 0.4)) {
               this.motionX *= 0.05;
               this.motionZ *= 0.05;
               if (this.motionY > 0.4) {
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

            this.prevTrackPosX = this.posX;
            this.prevTrackPosZ = this.posZ;
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
         CURSE_MARK_STAGE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         BONE_SWORD_ITEM = null;
         BONE_DRILL_ITEM = null;
         weaponItemsResolved = false;
         COMBO_DELAYS = new int[]{0, 5, 7, 8, 9};
      }
   }
}
