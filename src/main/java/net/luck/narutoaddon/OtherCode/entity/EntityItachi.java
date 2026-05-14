
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
public class EntityItachi extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 202;
   public static final int ENTITYID_RANGED = 203;

   public EntityItachi(ElementsInfTsukAddon instance) {
      super(instance, 32);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "itachi"), 202).name("inftsuk_itachi").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, ItachiRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class ItachiRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/itachi1.png");

      public ItachiRenderer(RenderManager renderManager) {
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
            GlStateManager.color(1.0F, 0.7F, 0.7F, 1.0F);
         } else if (attackState == 3) {
            GlStateManager.color(0.6F, 0.5F, 0.7F, 1.0F);
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
      public static final int STATE_FIREBALL = 2;
      public static final int STATE_AMATERASU = 3;
      public static final int STATE_GENJUTSU = 4;
      public static final int STATE_CROW_ESCAPE = 5;
      public static final int STATE_BODY_FLICKER = 6;
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
      private static final float MELEE_DMG = 7.5F;
      private static final float MELEE_TRUE = 2.0F;
      private static final float COMBO_LIGHT_DMG = 6.3F;
      private static final float COMBO_LIGHT_TRUE = 2.0F;
      private static final float COMBO_HEAVY_DMG = 12.5F;
      private static final float COMBO_HEAVY_TRUE = 4.0F;
      private static final float FIREBALL_DMG = 17.5F;
      private static final float FIREBALL_TRUE = 6.0F;
      private static final float AMATERASU_DMG = 17.5F;
      private static final float AMATERASU_TRUE = 6.0F;
      private static final float AMATERASU_AOE_DMG = 10.0F;
      private static final float AMATERASU_AOE_TRUE = 4.0F;
      private static final float GENJUTSU_DMG = 8.8F;
      private static final float GENJUTSU_TRUE = 4.0F;
      private static final int[] CD_FIREBALL_P1;
      private static final int[] CD_AMATERASU_P1;
      private static final int[] CD_GENJUTSU_P1;
      private static final int[] CD_CROW_ESCAPE_P1;
      private static final int[] CD_BODY_FLICKER_P1;
      private static final int[] CD_COMBO_P1;
      private static final int[] CD_FIREBALL_P2;
      private static final int[] CD_AMATERASU_P2;
      private static final int[] CD_GENJUTSU_P2;
      private static final int[] CD_CROW_ESCAPE_P2;
      private static final int[] CD_BODY_FLICKER_P2;
      private static final int[] CD_COMBO_P2;
      private static final int[] CD_FIREBALL_P3;
      private static final int[] CD_AMATERASU_P3;
      private static final int[] CD_GENJUTSU_P3;
      private static final int[] CD_CROW_ESCAPE_P3;
      private static final int[] CD_BODY_FLICKER_P3;
      private static final int[] CD_COMBO_P3;
      private static final double[] R_FIREBALL;
      private static final double[] R_AMATERASU;
      private static final double[] R_GENJUTSU;
      private static final double[] R_MELEE;
      private static final double[] R_COMBO;
      private static final float FLICKER_MIN_RANGE = 8.0F;
      private static final float FLICKER_MAX_RANGE = 25.0F;
      private static final int GENJUTSU_DUR = 80;
      private static final int AMATERASU_FIRE_DUR = 5;
      private static final int COMBO_MAX_HITS = 4;
      private static final int[] COMBO_DELAYS;
      private static final int TARGET_SWITCH_COOLDOWN = 120;
      private static final float TARGET_SWITCH_CHANCE = 0.4F;
      private boolean isPassive = false;
      private UUID kisameUUID;
      private int fireballCooldown = 0;
      private int amaterasuCooldown = 0;
      private int genjutsuCooldown = 0;
      private int crowEscapeCooldown = 0;
      private int bodyFlickerCooldown = 0;
      private int comboCooldown = 0;
      private int attackAnimTimer = 0;
      private int invulnTicks = 0;
      private int comboHitCount = 0;
      private int comboTimer = 0;
      private boolean inActiveCombo = false;
      private Entity activeComboTarget = null;
      private int verticalLeapCooldown = 0;
      private int targetSwitchCooldown = 0;
      private UUID currentTargetUUID = null;
      private int currentPhase = 1;
      private boolean phase2Announced = false;
      private boolean phase3Announced = false;
      private double prevPosX;
      private double prevPosZ;
      private int stuckTicks = 0;
      private int trapEscapeTick = 0;
      private int coordComboId = 0;
      private int coordComboTimer = 0;
      private int coordComboDelay = 0;
      private UUID coordComboTargetUUID = null;
      private int coordComboCooldown = 0;
      private boolean isDashing = false;
      private int dashTicksRemaining = 0;
      private double dashVelocityX = (double)0.0F;
      private double dashVelocityZ = (double)0.0F;
      private EntityLivingBase dashTarget = null;
      private static final int FLICKER_DASH_DURATION = 10;
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
         this.tasks.addTask(1, new EntityAIItachiCombat(this));
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
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.32);
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)8.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.85);
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)6.0F);
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
            this.broadcastMessage("§c§lItachi: §7\"You've forced me to use these eyes...\"");
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.36);

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)8.0F))) {
               if (p.isEntityAlive() && !p.isSpectator()) {
                  p.setFire(3);

                  for(int i = 0; i < 15; ++i) {
                     this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, p.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, p.posY + this.rand.nextDouble() * (double)2.0F, p.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, (double)0.0F, 0.05, (double)0.0F, new int[0]);
                  }
               }
            }

            for(int i = 0; i < 40; ++i) {
               this.world.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F, this.posY + this.rand.nextDouble() * (double)2.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F, (this.rand.nextDouble() - (double)0.5F) * 0.1, this.rand.nextDouble() * 0.2, (this.rand.nextDouble() - (double)0.5F) * 0.1, new int[0]);
            }
         }

         if (newPhase == 3 && !this.phase3Announced) {
            this.phase3Announced = true;
            this.broadcastMessage("§4§lItachi: §7\"Forgive me... this is the end.\"");
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4);

            for(int i = 0; i < 60; ++i) {
               this.world.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, this.posY + this.rand.nextDouble() * (double)3.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, (this.rand.nextDouble() - (double)0.5F) * 0.15, this.rand.nextDouble() * 0.3, (this.rand.nextDouble() - (double)0.5F) * 0.15, new int[0]);
            }

            for(int i = 0; i < 30; ++i) {
               this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, this.posY + this.rand.nextDouble() * (double)2.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, (double)0.0F, 0.1, (double)0.0F, new int[0]);
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

            if (this.isDashing) {
               this.processDashTick();
            }

            if (this.fireballCooldown > 0) {
               --this.fireballCooldown;
            }

            if (this.amaterasuCooldown > 0) {
               --this.amaterasuCooldown;
            }

            if (this.genjutsuCooldown > 0) {
               --this.genjutsuCooldown;
            }

            if (this.crowEscapeCooldown > 0) {
               --this.crowEscapeCooldown;
            }

            if (this.bodyFlickerCooldown > 0) {
               --this.bodyFlickerCooldown;
            }

            if (this.comboCooldown > 0) {
               --this.comboCooldown;
            }

            if (this.targetSwitchCooldown > 0) {
               --this.targetSwitchCooldown;
            }

            if (this.verticalLeapCooldown > 0) {
               --this.verticalLeapCooldown;
            }

            if (this.attackAnimTimer > 0) {
               --this.attackAnimTimer;
               if (this.attackAnimTimer <= 0) {
                  this.setAttackState(0);
               }
            }

            if (this.invulnTicks > 0) {
               --this.invulnTicks;
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

            this.checkPhaseTransition();
            if (!this.isPassive && this.getAttackTarget() != null) {
               this.tryTargetSwitch();
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

            if (this.ticksExisted % 10 == 5) {
               this.unstickFromBlocks();
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
         int startY = Math.min((int)referenceY + 10, 255);
         int minY = Math.max((int)referenceY - 20, 1);

         for(int y = startY; y >= minY; --y) {
            BlockPos ground = new BlockPos(x, (double)(y - 1), z);
            BlockPos foot = new BlockPos(x, (double)y, z);
            BlockPos head = new BlockPos(x, (double)(y + 1), z);
            if (this.world.getBlockState(ground).getMaterial().isSolid() && !this.world.getBlockState(foot).getMaterial().isSolid() && !this.world.getBlockState(head).getMaterial().isSolid()) {
               return (double)y;
            }
         }

         return this.posY;
      }

      private boolean isPositionSafe(double x, double y, double z) {
         BlockPos foot = new BlockPos(x, y, z);
         BlockPos head = new BlockPos(x, y + (double)1.0F, z);
         BlockPos ground = new BlockPos(x, y - (double)1.0F, z);
         return this.world.getBlockState(ground).getMaterial().isSolid() && !this.world.getBlockState(foot).getMaterial().isSolid() && !this.world.getBlockState(head).getMaterial().isSolid();
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
                        double checkY = this.findGroundLevel(checkX, this.posY, checkZ);
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

      public boolean processInteract(EntityPlayer player, EnumHand hand) {
         if (hand != EnumHand.MAIN_HAND) {
            return super.processInteract(player, hand);
         } else if (this.world.isRemote) {
            return true;
         } else {
            return !(player instanceof EntityPlayerMP) ? false : NpcInteractionHelper.handleNpcInteraction(this, (EntityPlayerMP)player);
         }
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
            normDmg = (float)((double)12.5F * dmgMult);
            trueDmg = (float)((double)4.0F * dmgMult);
         } else {
            normDmg = (float)((double)6.3F * dmgMult);
            trueDmg = (float)((double)2.0F * dmgMult);
         }

         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         this.swingArm(EnumHand.MAIN_HAND);
         if (isHeavy) {
            float kb = 0.8F;
            target.motionX += (target.posX - this.posX) * 0.1 * (double)kb;
            target.motionY += 0.3;
            target.motionZ += (target.posZ - this.posZ) * 0.1 * (double)kb;
            if (target instanceof EntityPlayerMP) {
               ((EntityPlayerMP)target).velocityChanged = true;
            }

            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 1.2F, 0.8F);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.2, new int[0]);
            }

            this.comboHitCount = 0;
            this.inActiveCombo = false;
            this.activeComboTarget = null;
            this.attackAnimTimer = 15;
         } else {
            float pitch = 1.0F + (float)this.comboHitCount * 0.15F;
            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.7F, pitch);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 5 + this.comboHitCount * 2, 0.2, 0.2, 0.2, 0.05, new int[0]);
            }

            ++this.comboHitCount;
            int delayIndex = Math.min(this.comboHitCount, COMBO_DELAYS.length - 1);
            this.comboTimer = COMBO_DELAYS[delayIndex];
            this.attackAnimTimer = this.comboTimer + 5;
         }

      }

      public void fireKatonFireball(EntityLivingBase target) {
         if (this.fireballCooldown <= 0) {
            int[] cdRange = this.getCooldownRange(CD_FIREBALL_P1, CD_FIREBALL_P2, CD_FIREBALL_P3);
            this.fireballCooldown = this.randomCooldown(cdRange);
            this.setAttackState(2);
            this.attackAnimTimer = 20;
            double dmgMult = this.getDamageMultiplier();
            float normDmg = (float)((double)17.5F * dmgMult);
            float trueDmg = (float)((double)6.0F * dmgMult);
            EntityKatonFireball.EntityCustom fireball = new EntityKatonFireball.EntityCustom(this.world, this, normDmg, trueDmg);
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
            double dz = target.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            fireball.shoot(dx, dy + dist * 0.04, dz, 1.6F, 0.8F);
            this.world.spawnEntity(fireball);

            for(int i = 0; i < 10; ++i) {
               this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F), this.posY + 1.2 + this.rand.nextDouble() * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F), (double)0.0F, 0.05, (double)0.0F, new int[0]);
            }

         }
      }

      public void useAmaterasu(EntityLivingBase target) {
         if (this.amaterasuCooldown <= 0) {
            int[] cdRange = this.getCooldownRange(CD_AMATERASU_P1, CD_AMATERASU_P2, CD_AMATERASU_P3);
            this.amaterasuCooldown = this.randomCooldown(cdRange);
            this.setAttackState(3);
            this.attackAnimTimer = 30;
            double dmgMult = this.getDamageMultiplier();
            target.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((double)17.5F * dmgMult));
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, (float)((double)6.0F * dmgMult));
            Potion amaterasuPotion = (Potion)Potion.REGISTRY.getObject(new ResourceLocation("narutomod", "amaterasuflame"));
            if (amaterasuPotion != null) {
               int duration = this.currentPhase >= 3 ? 300 : 200;
               int amplifier = this.currentPhase >= 3 ? 3 : 2;
               target.addPotionEffect(new PotionEffect(amaterasuPotion, duration, amplifier));
            } else {
               target.setFire(5);

               for(int i = 0; i < 30; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, target.posX + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F, target.posY + this.rand.nextDouble() * (double)2.0F, target.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F, (double)0.0F, 0.05, (double)0.0F, new int[0]);
               }
            }

            double aoeRange = this.currentPhase >= 2 ? (double)4.0F : (double)3.0F;

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, target.getEntityBoundingBox().grow(aoeRange))) {
               if (e instanceof EntityLivingBase && e != this && !(e instanceof EntityKisame.EntityCustom)) {
                  EntityLivingBase living = (EntityLivingBase)e;
                  living.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((double)10.0F * dmgMult));
                  living.hurtResistantTime = 0;
                  living.attackEntityFrom(DamageSource.MAGIC, (float)((double)4.0F * dmgMult));
                  if (amaterasuPotion != null) {
                     living.addPotionEffect(new PotionEffect(amaterasuPotion, 100, 1));
                  } else {
                     living.setFire(3);
                  }
               }
            }

            SoundEvent amaterasuSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "amaterasu2"));
            if (amaterasuSound != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, amaterasuSound, SoundCategory.HOSTILE, 1.5F, 1.0F);
            }

         }
      }

      public void useGenjutsu(EntityLivingBase target) {
         if (this.genjutsuCooldown <= 0) {
            double dist = (double)this.getDistance(target);
            if (!(dist > R_GENJUTSU[1])) {
               int[] cdRange = this.getCooldownRange(CD_GENJUTSU_P1, CD_GENJUTSU_P2, CD_GENJUTSU_P3);
               this.genjutsuCooldown = this.randomCooldown(cdRange);
               this.setAttackState(4);
               this.attackAnimTimer = 30;
               double dmgMult = this.getDamageMultiplier();
               int duration = this.currentPhase >= 3 ? 120 : 80;
               target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, duration, 0));
               if (this.currentPhase >= 2) {
                  target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, duration, 0));
               }

               target.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((double)8.8F * dmgMult));
               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.MAGIC, (float)((double)4.0F * dmgMult));

               for(int i = 0; i < 20; ++i) {
                  double angle = (Math.PI / 10D) * (double)i;
                  double px = target.posX + Math.cos(angle) * (double)2.0F;
                  double pz = target.posZ + Math.sin(angle) * (double)2.0F;
                  this.world.spawnParticle(EnumParticleTypes.REDSTONE, px, target.posY + (double)1.0F + this.rand.nextDouble(), pz, (double)1.0F, (double)0.0F, (double)0.0F, new int[0]);
               }

               EntityKisame.EntityCustom kisame = this.getPartnerKisame();
               if (kisame != null && kisame.isEntityAlive() && !kisame.hasCoordCombo() && this.coordComboCooldown <= 0 && this.rand.nextFloat() < 0.35F) {
                  kisame.requestCoordCombo(1, target.getUniqueID());
               }

            }
         }
      }

      public void useCrowEscape() {
         if (this.crowEscapeCooldown <= 0) {
            float threshold = this.currentPhase >= 2 ? 0.7F : 0.5F;
            if (!(this.getHealth() > this.getMaxHealth() * threshold)) {
               int[] cdRange = this.getCooldownRange(CD_CROW_ESCAPE_P1, CD_CROW_ESCAPE_P2, CD_CROW_ESCAPE_P3);
               this.crowEscapeCooldown = this.randomCooldown(cdRange);
               this.setAttackState(5);
               this.attackAnimTimer = 15;
               this.invulnTicks = 10;
               EntityLivingBase target = this.getAttackTarget();
               double teleX;
               double teleZ;
               if (target != null) {
                  double dx = this.posX - target.posX;
                  double dz = this.posZ - target.posZ;
                  double dist = Math.sqrt(dx * dx + dz * dz);
                  if (dist > (double)0.0F) {
                     teleX = this.posX + dx / dist * (double)10.0F;
                     teleZ = this.posZ + dz / dist * (double)10.0F;
                  } else {
                     teleX = this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)20.0F;
                     teleZ = this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)20.0F;
                  }
               } else {
                  teleX = this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)20.0F;
                  teleZ = this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)20.0F;
               }

               double teleY = this.findGroundLevel(teleX, this.posY, teleZ);
               if (!this.isPositionSafe(teleX, teleY, teleZ)) {
                  boolean found = false;

                  for(int attempt = 0; attempt < 8; ++attempt) {
                     double altX = teleX + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F;
                     double altZ = teleZ + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F;
                     double altY = this.findGroundLevel(altX, this.posY, altZ);
                     if (this.isPositionSafe(altX, altY, altZ)) {
                        teleX = altX;
                        teleY = altY;
                        teleZ = altZ;
                        found = true;
                        break;
                     }
                  }

                  if (!found) {
                     teleX = this.posX;
                     teleY = this.posY;
                     teleZ = this.posZ;
                  }
               }

               for(int i = 0; i < 40; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.5F, this.posY + this.rand.nextDouble() * (double)2.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.5F, (this.rand.nextDouble() - (double)0.5F) * 0.15, this.rand.nextDouble() * (double)0.25F, (this.rand.nextDouble() - (double)0.5F) * 0.15, new int[0]);
               }

               this.setPositionAndUpdate(teleX, teleY, teleZ);

               for(int i = 0; i < 20; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.SPELL_WITCH, teleX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, teleY + this.rand.nextDouble() * (double)2.0F, teleZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, (double)0.0F, 0.1, (double)0.0F, new int[0]);
               }

               if (this.currentPhase >= 3 && target != null && target.isEntityAlive() && this.fireballCooldown <= 0) {
                  this.fireKatonFireball(target);
               }

            }
         }
      }

      public void useBodyFlicker(EntityLivingBase target) {
         if (this.bodyFlickerCooldown <= 0) {
            double dist = (double)this.getDistance(target);
            if (!(dist < (double)8.0F) && !(dist > (double)25.0F)) {
               int[] cdRange = this.getCooldownRange(CD_BODY_FLICKER_P1, CD_BODY_FLICKER_P2, CD_BODY_FLICKER_P3);
               this.bodyFlickerCooldown = this.randomCooldown(cdRange);
               this.setAttackState(6);
               this.attackAnimTimer = 10;

               for(int i = 0; i < 20; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, this.posY + this.rand.nextDouble() * (double)2.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, (double)0.0F, 0.05, (double)0.0F, new int[0]);
               }

               double dx = target.posX - this.posX;
               double dz = target.posZ - this.posZ;
               double d = Math.sqrt(dx * dx + dz * dz);
               double behindX;
               double behindZ;
               if (d > (double)0.0F) {
                  behindX = target.posX - dx / d * (double)2.0F;
                  behindZ = target.posZ - dz / d * (double)2.0F;
               } else {
                  behindX = target.posX + (double)2.0F;
                  behindZ = target.posZ;
               }

               double behindY = this.findGroundLevel(behindX, target.posY, behindZ);
               if (!this.isPositionSafe(behindX, behindY, behindZ)) {
                  double perpX = -dz / d;
                  double perpZ = dx / d;
                  double[][] alternatives = new double[][]{{target.posX + perpX * (double)2.5F, target.posZ + perpZ * (double)2.5F}, {target.posX - perpX * (double)2.5F, target.posZ - perpZ * (double)2.5F}, {target.posX - dx / d * (double)3.0F, target.posZ - dz / d * (double)3.0F}};
                  boolean found = false;

                  for(double[] alt : alternatives) {
                     double altY = this.findGroundLevel(alt[0], target.posY, alt[1]);
                     if (this.isPositionSafe(alt[0], altY, alt[1])) {
                        behindX = alt[0];
                        behindZ = alt[1];
                        found = true;
                        break;
                     }
                  }

                  if (!found) {
                     behindX = this.posX;
                     behindY = this.posY;
                     behindZ = this.posZ;
                  }
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.8F, 1.5F);
               this.dashVelocityX = (behindX - this.posX) / (double)10.0F * 1.4;
               this.dashVelocityZ = (behindZ - this.posZ) / (double)10.0F * 1.4;
               this.isDashing = true;
               this.dashTicksRemaining = 10;
               this.dashTarget = target;
               this.motionY = 0.35;
               this.velocityChanged = true;
            }
         }
      }

      private void processDashTick() {
         if (this.isDashing) {
            --this.dashTicksRemaining;
            this.motionX = this.dashVelocityX;
            this.motionZ = this.dashVelocityZ;
            if (this.dashTicksRemaining > 5) {
               this.motionY = 0.05;
            } else {
               this.motionY = Math.max((double)-0.5F, this.motionY - 0.08);
            }

            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.8, this.posY + this.rand.nextDouble() * (double)1.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.8, 3, 0.1, 0.1, 0.1, 0.02, new int[0]);
               ws.spawnParticle(EnumParticleTypes.CRIT, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.6, this.posY + (double)0.5F + this.rand.nextDouble(), this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.6, 2, 0.1, 0.2, 0.1, 0.01, new int[0]);
            }

            if (this.dashTicksRemaining <= 0) {
               this.isDashing = false;
               this.dashVelocityX = (double)0.0F;
               this.dashVelocityZ = (double)0.0F;
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.8, 0.8, 0.8, 0.05, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_SMALL_FALL, SoundCategory.HOSTILE, 0.8F, 1.0F);
               if (this.dashTarget != null && this.dashTarget.isEntityAlive() && (double)this.getDistance(this.dashTarget) <= (double)3.5F) {
                  double dmgMult = this.getDamageMultiplier();
                  float normDmg = (float)((double)7.5F * dmgMult);
                  this.dashTarget.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                  this.dashTarget.hurtResistantTime = 0;
                  this.dashTarget.attackEntityFrom(DamageSource.MAGIC, 2.0F);
                  this.swingArm(EnumHand.MAIN_HAND);
               }

               EntityKisame.EntityCustom kisame = this.getPartnerKisame();
               if (kisame != null && kisame.isEntityAlive() && !kisame.hasCoordCombo() && this.coordComboCooldown <= 0 && this.rand.nextFloat() < 0.3F && this.dashTarget != null) {
                  kisame.requestCoordCombo(2, this.dashTarget.getUniqueID());
               }

               this.dashTarget = null;
            }

         }
      }

      public void setRevengeTarget(EntityLivingBase target) {
         if (!(target instanceof EntityKisame.EntityCustom)) {
            if (!(target instanceof EntityCustom)) {
               super.setRevengeTarget(target);
            }
         }
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         if (this.isPassive) {
            return false;
         } else if (this.invulnTicks > 0) {
            return false;
         } else {
            Entity src = source.getTrueSource();
            if (src instanceof EntityKisame.EntityCustom) {
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

      public UUID getKisameUUID() {
         return this.kisameUUID;
      }

      public void setKisameUUID(UUID uuid) {
         this.kisameUUID = uuid;
      }

      public EntityKisame.EntityCustom getPartnerKisame() {
         if (this.kisameUUID == null) {
            return null;
         } else {
            for(Entity e : this.world.loadedEntityList) {
               if (e instanceof EntityKisame.EntityCustom && e.getUniqueID().equals(this.kisameUUID)) {
                  return (EntityKisame.EntityCustom)e;
               }
            }

            return null;
         }
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
         if (this.kisameUUID != null) {
            compound.setString("kisameUUID", this.kisameUUID.toString());
         }

         compound.setInteger("fireballCD", this.fireballCooldown);
         compound.setInteger("amaterasuCD", this.amaterasuCooldown);
         compound.setInteger("genjutsuCD", this.genjutsuCooldown);
         compound.setInteger("crowEscapeCD", this.crowEscapeCooldown);
         compound.setInteger("bodyFlickerCD", this.bodyFlickerCooldown);
         compound.setInteger("comboCD", this.comboCooldown);
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

         if (compound.hasKey("kisameUUID")) {
            try {
               this.kisameUUID = UUID.fromString(compound.getString("kisameUUID"));
            } catch (IllegalArgumentException var4) {
            }
         }

         this.fireballCooldown = compound.getInteger("fireballCD");
         this.amaterasuCooldown = compound.getInteger("amaterasuCD");
         this.genjutsuCooldown = compound.getInteger("genjutsuCD");
         this.crowEscapeCooldown = compound.getInteger("crowEscapeCD");
         this.bodyFlickerCooldown = compound.getInteger("bodyFlickerCD");
         this.comboCooldown = compound.getInteger("comboCD");
         this.coordComboCooldown = compound.getInteger("coordComboCD");
      }

      static {
         NPC_CONFIG_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
         ATTACK_STATE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         IS_RETREATING = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
         PHASE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         CD_FIREBALL_P1 = new int[]{50, 70};
         CD_AMATERASU_P1 = new int[]{400, 550};
         CD_GENJUTSU_P1 = new int[]{250, 350};
         CD_CROW_ESCAPE_P1 = new int[]{120, 160};
         CD_BODY_FLICKER_P1 = new int[]{100, 140};
         CD_COMBO_P1 = new int[]{60, 80};
         CD_FIREBALL_P2 = new int[]{35, 50};
         CD_AMATERASU_P2 = new int[]{250, 350};
         CD_GENJUTSU_P2 = new int[]{150, 220};
         CD_CROW_ESCAPE_P2 = new int[]{80, 120};
         CD_BODY_FLICKER_P2 = new int[]{60, 90};
         CD_COMBO_P2 = new int[]{40, 60};
         CD_FIREBALL_P3 = new int[]{20, 35};
         CD_AMATERASU_P3 = new int[]{150, 250};
         CD_GENJUTSU_P3 = new int[]{100, 160};
         CD_CROW_ESCAPE_P3 = new int[]{50, 80};
         CD_BODY_FLICKER_P3 = new int[]{35, 55};
         CD_COMBO_P3 = new int[]{25, 40};
         R_FIREBALL = new double[]{(double)5.0F, (double)30.0F};
         R_AMATERASU = new double[]{(double)4.0F, (double)25.0F};
         R_GENJUTSU = new double[]{(double)2.0F, (double)10.0F};
         R_MELEE = new double[]{(double)0.0F, (double)3.0F};
         R_COMBO = new double[]{(double)0.0F, (double)3.5F};
         COMBO_DELAYS = new int[]{0, 7, 8, 12};
         PARALYSIS_MODIFIER_UUID = UUID.fromString("c69af92a-b96d-49b7-a396-9b3b0d77edd5");
         HEAVINESS_SPEED_UUID = UUID.fromString("7d735ff6-8872-482d-ac1f-cd2249e8f584");
         HEAVINESS_ATKSPD_UUID = UUID.fromString("fedf4303-bc45-4ad8-80e8-2237e9c90a18");
      }
   }

   public static class EntityAIItachiCombat extends EntityAIBase {
      private final EntityCustom itachi;
      private EntityLivingBase target;
      private int attackTick;
      private int strafeTimer;
      private boolean strafingRight;

      public EntityAIItachiCombat(EntityCustom itachi) {
         this.itachi = itachi;
         this.setMutexBits(3);
      }

      public boolean shouldExecute() {
         EntityLivingBase t = this.itachi.getAttackTarget();
         if (t != null && t.isEntityAlive()) {
            this.target = t;
            return true;
         } else {
            return false;
         }
      }

      public boolean shouldContinueExecuting() {
         return this.target != null && this.target.isEntityAlive() && this.itachi.isEntityAlive();
      }

      public void resetTask() {
         this.target = null;
         this.itachi.setAttackState(0);
         this.itachi.inActiveCombo = false;
         this.itachi.activeComboTarget = null;
         this.itachi.comboHitCount = 0;
      }

      public void updateTask() {
         if (this.target != null && this.target.isEntityAlive()) {
            if (this.itachi.getAttackTarget() != null && this.itachi.getAttackTarget() != this.target) {
               this.target = this.itachi.getAttackTarget();
            }

            double dist = (double)this.itachi.getDistance(this.target);
            this.itachi.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
            if (!this.itachi.inActiveCombo) {
               if (this.itachi.isRetreating()) {
                  this.handleRetreating(dist);
               } else {
                  ++this.attackTick;
                  this.handleMovement(dist);
                  if (this.itachi.hasCoordCombo() && this.itachi.coordComboDelay <= 0) {
                     EntityLivingBase comboTarget = this.findCoordTarget(this.itachi.coordComboTargetUUID);
                     if (comboTarget != null && comboTarget.isEntityAlive()) {
                        switch (this.itachi.coordComboId) {
                           case 3:
                              if (this.itachi.amaterasuCooldown <= 0) {
                                 this.itachi.useAmaterasu(comboTarget);
                              } else if (this.itachi.fireballCooldown <= 0) {
                                 this.itachi.fireKatonFireball(comboTarget);
                              }
                              break;
                           case 4:
                              if (this.itachi.fireballCooldown <= 0) {
                                 this.itachi.fireKatonFireball(comboTarget);
                              }
                              break;
                           case 5:
                              if (this.itachi.bodyFlickerCooldown <= 0) {
                                 this.itachi.useBodyFlicker(comboTarget);
                                 if (this.itachi.comboCooldown <= 0) {
                                    this.itachi.comboCooldown = 0;
                                 }
                              }
                        }

                        this.itachi.coordComboCooldown = this.itachi.getCoordGlobalCooldown();
                        this.itachi.cancelCoordCombo();
                        return;
                     }

                     this.itachi.cancelCoordCombo();
                  }

                  if (this.itachi.crowEscapeCooldown <= 0 && dist < (double)5.0F) {
                     float threshold = this.itachi.currentPhase >= 2 ? 0.7F : 0.5F;
                     if (this.itachi.getHealth() < this.itachi.getMaxHealth() * threshold) {
                        this.itachi.useCrowEscape();
                        return;
                     }
                  }

                  switch (this.itachi.currentPhase) {
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
            double dx = this.itachi.posX - this.target.posX;
            double dz = this.itachi.posZ - this.target.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > (double)0.0F) {
               this.itachi.getNavigator().tryMoveToXYZ(this.itachi.posX + dx / d * (double)10.0F, this.itachi.posY, this.itachi.posZ + dz / d * (double)10.0F, 1.3);
            }
         }

         if (this.itachi.crowEscapeCooldown <= 0) {
            float threshold = this.itachi.currentPhase >= 2 ? 0.7F : 0.5F;
            if (this.itachi.getHealth() < this.itachi.getMaxHealth() * threshold) {
               this.itachi.useCrowEscape();
            }
         }

      }

      private void handleMovement(double dist) {
         ++this.strafeTimer;
         switch (this.itachi.currentPhase) {
            case 1:
               if (dist > (double)16.0F) {
                  this.itachi.getNavigator().tryMoveToEntityLiving(this.target, 1.1);
               } else if (dist < (double)6.0F) {
                  this.retreatFrom(this.target, (double)5.0F, (double)1.0F);
               } else {
                  if (this.strafeTimer > 40) {
                     this.strafingRight = !this.strafingRight;
                     this.strafeTimer = 0;
                  }

                  double strafeAngle = Math.atan2(this.target.posZ - this.itachi.posZ, this.target.posX - this.itachi.posX);
                  double perpX;
                  double perpZ;
                  if (this.strafingRight) {
                     perpX = -Math.sin(strafeAngle) * (double)4.0F;
                     perpZ = Math.cos(strafeAngle) * (double)4.0F;
                  } else {
                     perpX = Math.sin(strafeAngle) * (double)4.0F;
                     perpZ = -Math.cos(strafeAngle) * (double)4.0F;
                  }

                  this.itachi.getNavigator().tryMoveToXYZ(this.itachi.posX + perpX, this.itachi.posY, this.itachi.posZ + perpZ, 0.9);
               }
               break;
            case 2:
               if (dist > (double)14.0F) {
                  this.itachi.getNavigator().tryMoveToEntityLiving(this.target, 1.2);
               } else if (dist < (double)5.0F) {
                  this.retreatFrom(this.target, (double)4.0F, 1.1);
               } else {
                  if (this.strafeTimer > 30) {
                     this.strafingRight = !this.strafingRight;
                     this.strafeTimer = 0;
                  }

                  double strafeAngle = Math.atan2(this.target.posZ - this.itachi.posZ, this.target.posX - this.itachi.posX);
                  double perpX = (double)(this.strafingRight ? -1 : 1) * Math.sin(strafeAngle) * (double)5.0F;
                  double perpZ = (double)(this.strafingRight ? 1 : -1) * Math.cos(strafeAngle) * (double)5.0F;
                  this.itachi.getNavigator().tryMoveToXYZ(this.itachi.posX + perpX, this.itachi.posY, this.itachi.posZ + perpZ, (double)1.0F);
               }
               break;
            case 3:
               if (dist > (double)10.0F) {
                  this.itachi.getNavigator().tryMoveToEntityLiving(this.target, 1.4);
               } else if (dist > (double)4.0F) {
                  this.itachi.getNavigator().tryMoveToEntityLiving(this.target, 1.2);
               } else {
                  this.itachi.getNavigator().clearPath();
               }
         }

      }

      private void retreatFrom(EntityLivingBase target, double distance, double speed) {
         double dx = this.itachi.posX - target.posX;
         double dz = this.itachi.posZ - target.posZ;
         double d = Math.sqrt(dx * dx + dz * dz);
         if (d > (double)0.0F) {
            this.itachi.getNavigator().tryMoveToXYZ(this.itachi.posX + dx / d * distance, this.itachi.posY, this.itachi.posZ + dz / d * distance, speed);
         }

      }

      private void executePhase1(double dist) {
         if (this.itachi.bodyFlickerCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.itachi.useBodyFlicker(this.target);
         } else if (this.itachi.fireballCooldown <= 0 && dist >= EntityCustom.R_FIREBALL[0] && dist <= EntityCustom.R_FIREBALL[1]) {
            this.itachi.fireKatonFireball(this.target);
         } else if (this.itachi.genjutsuCooldown <= 0 && dist >= EntityCustom.R_GENJUTSU[0] && dist <= EntityCustom.R_GENJUTSU[1]) {
            this.itachi.useGenjutsu(this.target);
         } else if (this.itachi.comboCooldown <= 0 && dist <= EntityCustom.R_COMBO[1]) {
            this.itachi.startCombo(this.target);
         } else {
            if (dist <= EntityCustom.R_MELEE[1] && this.attackTick >= 15) {
               this.performBasicMelee();
            }

         }
      }

      private void executePhase2(double dist) {
         if (this.itachi.amaterasuCooldown <= 0 && dist >= EntityCustom.R_AMATERASU[0] && dist <= EntityCustom.R_AMATERASU[1]) {
            this.itachi.useAmaterasu(this.target);
         } else if (this.itachi.bodyFlickerCooldown <= 0 && this.itachi.comboCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.itachi.useBodyFlicker(this.target);
            this.itachi.comboCooldown = 0;
         } else if (this.itachi.genjutsuCooldown <= 0 && dist >= EntityCustom.R_GENJUTSU[0] && dist <= EntityCustom.R_GENJUTSU[1]) {
            this.itachi.useGenjutsu(this.target);
         } else if (this.itachi.fireballCooldown <= 0 && dist >= EntityCustom.R_FIREBALL[0] && dist <= EntityCustom.R_FIREBALL[1]) {
            this.itachi.fireKatonFireball(this.target);
         } else if (this.itachi.comboCooldown <= 0 && dist <= EntityCustom.R_COMBO[1]) {
            this.itachi.startCombo(this.target);
         } else if (this.itachi.bodyFlickerCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.itachi.useBodyFlicker(this.target);
         } else {
            if (dist <= EntityCustom.R_MELEE[1] && this.attackTick >= 12) {
               this.performBasicMelee();
            }

         }
      }

      private void executePhase3(double dist) {
         if (this.itachi.amaterasuCooldown <= 0 && dist >= EntityCustom.R_AMATERASU[0] && dist <= EntityCustom.R_AMATERASU[1]) {
            this.itachi.useAmaterasu(this.target);
         } else if (this.itachi.bodyFlickerCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.itachi.useBodyFlicker(this.target);
         } else if (this.itachi.genjutsuCooldown <= 0 && dist <= (double)12.0F) {
            this.itachi.useGenjutsu(this.target);
         } else if (this.itachi.comboCooldown <= 0 && dist <= EntityCustom.R_COMBO[1]) {
            this.itachi.startCombo(this.target);
         } else if (this.itachi.fireballCooldown <= 0 && dist >= EntityCustom.R_FIREBALL[0] && dist <= EntityCustom.R_FIREBALL[1]) {
            this.itachi.fireKatonFireball(this.target);
         } else {
            if (dist <= EntityCustom.R_MELEE[1] && this.attackTick >= 10) {
               this.performBasicMelee();
            }

         }
      }

      private void performBasicMelee() {
         this.attackTick = 0;
         this.itachi.setAttackState(1);
         this.itachi.attackAnimTimer = 10;
         this.itachi.swingArm(EnumHand.MAIN_HAND);
         double dmgMult = this.itachi.getDamageMultiplier();
         float meleeDmg = (float)((double)7.5F * dmgMult);
         float meleeTrueDmg = (float)((double)2.0F * dmgMult);
         this.target.attackEntityFrom(DamageSource.causeMobDamage(this.itachi), meleeDmg);
         this.target.hurtResistantTime = 0;
         this.target.attackEntityFrom(DamageSource.MAGIC, meleeTrueDmg);
      }

      private EntityLivingBase findCoordTarget(UUID targetUUID) {
         if (targetUUID == null) {
            return null;
         } else {
            for(Entity e : this.itachi.world.loadedEntityList) {
               if (e instanceof EntityLivingBase && e.getUniqueID().equals(targetUUID) && e.isEntityAlive()) {
                  return (EntityLivingBase)e;
               }
            }

            return null;
         }
      }
   }
}
