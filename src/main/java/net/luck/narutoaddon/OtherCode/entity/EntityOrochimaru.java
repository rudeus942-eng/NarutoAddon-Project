
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
public class EntityOrochimaru extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 17;
   public static final int ENTITYID_RANGED = 18;

   public EntityOrochimaru(ElementsInfTsukAddon instance) {
      super(instance, 36);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "orochimaru"), 17).name("orochimaru").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, OrochimaruRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class OrochimaruRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/orochimaru.png");

      public OrochimaruRenderer(RenderManager renderManager) {
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
         if (attackState == 2 || attackState == 3 || attackState == 8) {
            GlStateManager.color(0.85F, 0.9F, 1.0F, 1.0F);
         }

         if (attackState == 7) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         }

         int phase = entity.getCurrentPhase();
         if (phase == 2) {
            GlStateManager.color(0.85F, 0.7F, 1.0F, 1.0F);
         } else if (phase == 3) {
            float pulse = 0.75F + 0.25F * MathHelper.sin((float)entity.ticksExisted * 0.15F);
            GlStateManager.color(0.7F * pulse, 0.5F * pulse, 1.0F * pulse, 1.0F);
         }

      }
   }

   public static class EntityCustom extends EntityCreature implements INpcConfigurable {
      private static final DataParameter<String> NPC_CONFIG_ID;
      private static final DataParameter<Integer> ATTACK_STATE;
      private static final DataParameter<Boolean> IS_PASSIVE;
      private static final DataParameter<Integer> PHASE;
      public static final int STATE_IDLE = 0;
      public static final int STATE_KICK = 1;
      public static final int STATE_KUSANAGI_SLASH = 2;
      public static final int STATE_KUSANAGI_THRUST = 3;
      public static final int STATE_NECK_LUNGE = 4;
      public static final int STATE_TONGUE_GRAPPLE = 5;
      public static final int STATE_SNAKE_VOMIT = 6;
      public static final int STATE_ORAL_REBIRTH = 7;
      public static final int STATE_KUSANAGI_BARRAGE = 8;
      public static final int STATE_SUMMON_MANDA = 9;
      public static final int PHASE_1 = 1;
      public static final int PHASE_2 = 2;
      public static final int PHASE_3 = 3;
      private static final float PHASE_2_THRESHOLD = 0.6F;
      private static final float PHASE_3_THRESHOLD = 0.3F;
      private static final float KICK_DMG = 12.0F;
      private static final float KICK_TRUE = 4.0F;
      private static final float KUSANAGI_SLASH_DMG = 14.0F;
      private static final float KUSANAGI_SLASH_TRUE = 8.0F;
      private static final float KUSANAGI_THRUST_DMG = 10.0F;
      private static final float KUSANAGI_THRUST_TRUE = 12.0F;
      private static final float NECK_LUNGE_DMG = 16.0F;
      private static final float NECK_LUNGE_TRUE = 6.0F;
      private static final float TONGUE_GRAPPLE_DMG = 6.0F;
      private static final float TONGUE_GRAPPLE_TRUE = 4.0F;
      private static final float SNAKE_VOMIT_DMG = 8.0F;
      private static final float SNAKE_VOMIT_TRUE = 10.0F;
      private static final float BARRAGE_HIT1_DMG = 10.0F;
      private static final float BARRAGE_HIT1_TRUE = 5.0F;
      private static final float BARRAGE_HIT2_DMG = 10.0F;
      private static final float BARRAGE_HIT2_TRUE = 5.0F;
      private static final float BARRAGE_HIT3_DMG = 18.0F;
      private static final float BARRAGE_HIT3_TRUE = 12.0F;
      private static final double KICK_RANGE = (double)2.5F;
      private static final double KUSANAGI_SLASH_RANGE = (double)3.0F;
      private static final double KUSANAGI_THRUST_MIN_RANGE = (double)8.0F;
      private static final double KUSANAGI_THRUST_MAX_RANGE = (double)15.0F;
      private static final double NECK_LUNGE_MIN_RANGE = (double)8.0F;
      private static final double NECK_LUNGE_MAX_RANGE = (double)25.0F;
      private static final double TONGUE_GRAPPLE_MIN_RANGE = (double)5.0F;
      private static final double TONGUE_GRAPPLE_MAX_RANGE = (double)12.0F;
      private static final double SNAKE_VOMIT_RANGE = (double)4.0F;
      private static final double BARRAGE_RANGE = (double)3.0F;
      private static final int[] CD_KICK_P1;
      private static final int[] CD_KUSANAGI_SLASH_P1;
      private static final int[] CD_KUSANAGI_THRUST_P1;
      private static final int[] CD_NECK_LUNGE_P1;
      private static final int[] CD_KICK_P2;
      private static final int[] CD_KUSANAGI_SLASH_P2;
      private static final int[] CD_KUSANAGI_THRUST_P2;
      private static final int[] CD_NECK_LUNGE_P2;
      private static final int[] CD_TONGUE_GRAPPLE_P2;
      private static final int[] CD_SNAKE_VOMIT_P2;
      private static final int[] CD_KICK_P3;
      private static final int[] CD_KUSANAGI_SLASH_P3;
      private static final int[] CD_KUSANAGI_THRUST_P3;
      private static final int[] CD_NECK_LUNGE_P3;
      private static final int[] CD_TONGUE_GRAPPLE_P3;
      private static final int[] CD_SNAKE_VOMIT_P3;
      private static final int[] CD_BARRAGE_P3;
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
      private int kickCooldown = 0;
      private int kusanagiSlashCooldown = 0;
      private int kusanagiThrustCooldown = 0;
      private int neckLungeCooldown = 0;
      private int tongueGrappleCooldown = 0;
      private int snakeVomitCooldown = 0;
      private int barrageCooldown = 0;
      private int attackAnimTimer = 0;
      private int invulnTicks = 0;
      private boolean oralRebirthUsed = false;
      private int oralRebirthTimer = 0;
      private boolean mandaSummoned = false;
      private UUID mandaUUID = null;
      private int comboStep = 0;
      private int comboTickTimer = 0;
      private EntityLivingBase comboTarget = null;
      private int verticalLeapCooldown = 0;
      private static final int LUNGE_DASH_DURATION = 8;
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

      public void setAttackTarget(@Nullable EntityLivingBase target) {
         if (target != null && !(target instanceof EntityPlayer)) {
            String cn = target.getClass().getName().toLowerCase();
            if (!cn.contains("icedome") && !cn.contains("shieldbase")) {
               return;
            }
         }

         super.setAttackTarget(target);
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(NPC_CONFIG_ID, "");
         this.dataManager.register(ATTACK_STATE, 0);
         this.dataManager.register(IS_PASSIVE, false);
         this.dataManager.register(PHASE, 1);
      }

      protected void initEntityAI() {
         this.tasks.addTask(1, new EntityAIOrochimaruCombat(this));
         this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F));
         this.tasks.addTask(6, new EntityAILookIdle(this));
         this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
         this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)30000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.48);
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)6.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.9);
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)12.0F);
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
         if (newPhase == 2 && !this.phase2Announced) {
            this.phase2Announced = true;
            this.broadcastMessage("§5§lOrochimaru: §7\"Kukuku... you're more capable than I thought. Let me show you what desperation looks like.\"");
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)this.height / (double)2.0F, this.posZ, 80, (double)2.0F, (double)1.5F, (double)2.0F, (double)0.0F, 0.15, (double)0.0F, new int[]{815346380, 25, 15, 240, -1, 0});
         }

         if (newPhase == 3 && !this.phase3Announced) {
            this.phase3Announced = true;
            this.broadcastMessage("§5§lOrochimaru: §7\"Enough! I am one of the Legendary Sannin! You will know TRUE fear!\"");
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.56);
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)this.height / (double)2.0F, this.posZ, 120, (double)3.0F, (double)2.0F, (double)3.0F, (double)0.0F, 0.2, (double)0.0F, new int[]{814416011, 35, 20, 240, -1, 0});
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, new SoundEvent(new ResourceLocation("narutomod:chakraflow")), SoundCategory.HOSTILE, 0.8F, 0.7F);
         }

      }

      private int[] getCooldownRange(int attackType) {
         switch (attackType) {
            case 1:
               switch (this.currentPhase) {
                  case 2:
                     return CD_KICK_P2;
                  case 3:
                     return CD_KICK_P3;
                  default:
                     return CD_KICK_P1;
               }
            case 2:
               switch (this.currentPhase) {
                  case 2:
                     return CD_KUSANAGI_SLASH_P2;
                  case 3:
                     return CD_KUSANAGI_SLASH_P3;
                  default:
                     return CD_KUSANAGI_SLASH_P1;
               }
            case 3:
               switch (this.currentPhase) {
                  case 2:
                     return CD_KUSANAGI_THRUST_P2;
                  case 3:
                     return CD_KUSANAGI_THRUST_P3;
                  default:
                     return CD_KUSANAGI_THRUST_P1;
               }
            case 4:
               switch (this.currentPhase) {
                  case 2:
                     return CD_NECK_LUNGE_P2;
                  case 3:
                     return CD_NECK_LUNGE_P3;
                  default:
                     return CD_NECK_LUNGE_P1;
               }
            case 5:
               switch (this.currentPhase) {
                  case 3:
                     return CD_TONGUE_GRAPPLE_P3;
                  default:
                     return CD_TONGUE_GRAPPLE_P2;
               }
            case 6:
               switch (this.currentPhase) {
                  case 3:
                     return CD_SNAKE_VOMIT_P3;
                  default:
                     return CD_SNAKE_VOMIT_P2;
               }
            case 7:
            default:
               return CD_KICK_P1;
            case 8:
               return CD_BARRAGE_P3;
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

            if (this.kickCooldown > 0) {
               --this.kickCooldown;
            }

            if (this.kusanagiSlashCooldown > 0) {
               --this.kusanagiSlashCooldown;
            }

            if (this.kusanagiThrustCooldown > 0) {
               --this.kusanagiThrustCooldown;
            }

            if (this.neckLungeCooldown > 0) {
               --this.neckLungeCooldown;
            }

            if (this.tongueGrappleCooldown > 0) {
               --this.tongueGrappleCooldown;
            }

            if (this.snakeVomitCooldown > 0) {
               --this.snakeVomitCooldown;
            }

            if (this.barrageCooldown > 0) {
               --this.barrageCooldown;
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
               if (this.attackAnimTimer <= 0 && this.comboStep == 0 && this.oralRebirthTimer <= 0) {
                  this.setAttackState(0);
               }
            }

            this.checkPhaseTransition();
            this.processOralRebirth();
            this.processComboTicks();
            this.checkMandaAlive();
            if (!this.isPassive && this.getAttackTarget() != null) {
               this.tryTargetSwitch();
            }

            if (this.currentPhase == 2 && !this.oralRebirthUsed && this.oralRebirthTimer <= 0) {
               float hpPercent = this.getHealth() / this.getMaxHealth();
               if (hpPercent < 0.4F) {
                  this.startOralRebirth();
               }
            }

            if (this.currentPhase == 3 && !this.isPassive && this.ticksExisted % 20 == 0) {
               this.heal(10.0F);
               if (this.ticksExisted % 40 == 0) {
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 8, 0.3, (double)0.5F, 0.3, (double)0.0F, 0.08, (double)0.0F, new int[]{546910924, 14, 12, 240, -1, 0});
               }
            }

            if (this.currentPhase == 3 && !this.isPassive) {
               double angle = (double)(this.ticksExisted * 12) * Math.PI / (double)180.0F;

               for(int i = 0; i < 2; ++i) {
                  double offsetAngle = angle + (double)i * Math.PI;
                  double px = this.posX + Math.cos(offsetAngle) * 0.8;
                  double pz = this.posZ + Math.sin(offsetAngle) * 0.8;
                  Particles.spawnParticle(this.world, Types.SMOKE, px, this.posY + (double)1.0F + this.rand.nextDouble() * (double)0.5F, pz, 3, 0.08, 0.08, 0.08, (double)0.0F, 0.04, (double)0.0F, new int[]{545980555, 15, 8, 240});
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

      private void startOralRebirth() {
         this.oralRebirthUsed = true;
         this.oralRebirthTimer = 40;
         this.invulnTicks = 40;
         this.setAttackState(7);
         this.attackAnimTimer = 45;
         this.broadcastMessage("§5§lOrochimaru: §7\"Oral Rebirth... you cannot kill what sheds its skin!\"");
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)this.height / (double)2.0F, this.posZ, 60, (double)1.0F, (double)1.5F, (double)1.0F, (double)0.0F, 0.2, (double)0.0F, new int[]{1090519039, 30, 15, 240, -1, 0});
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 0.6F, 1.5F);
      }

      private void processOralRebirth() {
         if (this.oralRebirthTimer > 0) {
            --this.oralRebirthTimer;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.END_ROD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posY + this.rand.nextDouble() * (double)2.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, 3, 0.1, 0.1, 0.1, 0.05, new int[0]);
            }

            if (this.oralRebirthTimer <= 0) {
               float healAmount = this.getMaxHealth() * 0.15F;
               this.heal(healAmount);
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)this.height / (double)2.0F, this.posZ, 80, (double)1.5F, (double)2.0F, (double)1.5F, (double)0.0F, 0.2, (double)0.0F, new int[]{1090519039, 35, 20, 240, -1, 0});
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, new SoundEvent(new ResourceLocation("narutomod:windecho")), SoundCategory.HOSTILE, 0.6F, 1.2F);
               this.setAttackState(0);
            }

         }
      }

      public UUID getMandaUUID() {
         return this.mandaUUID;
      }

      private void checkMandaAlive() {
         if (this.mandaUUID != null) {
            if (this.ticksExisted % 20 == 0) {
               if (this.world instanceof WorldServer) {
                  Entity manda = ((WorldServer)this.world).getEntityFromUuid(this.mandaUUID);
                  if (manda == null || !manda.isEntityAlive()) {
                     this.mandaUUID = null;
                  }
               }

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
                        float trueDmg = (float)((double)5.0F * dmgMult);
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
                        float normDmg = (float)((double)18.0F * dmgMult);
                        float trueDmg = (float)((double)12.0F * dmgMult);
                        this.comboTarget.hurtResistantTime = 0;
                        this.comboTarget.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                        this.comboTarget.hurtResistantTime = 0;
                        this.comboTarget.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                        double kdx = this.comboTarget.posX - this.posX;
                        double kdz = this.comboTarget.posZ - this.posZ;
                        double kd = Math.sqrt(kdx * kdx + kdz * kdz);
                        if (kd > (double)0.0F) {
                           this.comboTarget.motionX = kdx / kd * 1.8;
                           this.comboTarget.motionY = 0.4;
                           this.comboTarget.motionZ = kdz / kd * 1.8;
                        }

                        if (this.comboTarget instanceof EntityPlayerMP) {
                           ((EntityPlayerMP)this.comboTarget).velocityChanged = true;
                        }

                        this.swingArm(EnumHand.MAIN_HAND);
                        if (this.world instanceof WorldServer) {
                           ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.comboTarget.posX, this.comboTarget.posY + (double)1.0F, this.comboTarget.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                        }

                        Particles.spawnParticle(this.world, Types.SMOKE, this.comboTarget.posX, this.comboTarget.posY + (double)this.comboTarget.height / (double)2.0F, this.comboTarget.posZ, 35, (double)0.5F, 0.6, (double)0.5F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{816495615, 20, 10, 240, -1, 0});
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

      public void basicKick(EntityLivingBase target) {
         if (this.kickCooldown <= 0) {
            this.kickCooldown = this.randomCooldown(this.getCooldownRange(1));
            this.setAttackState(1);
            this.attackAnimTimer = 12;
            double dmgMult = this.getDamageMultiplier();
            float normDmg = (float)((double)12.0F * dmgMult);
            float trueDmg = (float)((double)4.0F * dmgMult);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            this.swingArm(EnumHand.MAIN_HAND);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 8, 0.3, 0.3, 0.3, 0.2, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 0.8F, 1.0F);
         }
      }

      public void kusanagiSlash(EntityLivingBase target) {
         if (this.kusanagiSlashCooldown <= 0) {
            this.kusanagiSlashCooldown = this.randomCooldown(this.getCooldownRange(2));
            this.setAttackState(2);
            this.attackAnimTimer = 15;
            double dmgMult = this.getDamageMultiplier();
            float normDmg = (float)((double)14.0F * dmgMult);
            float trueDmg = (float)((double)8.0F * dmgMult);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            this.swingArm(EnumHand.MAIN_HAND);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, target.posX, target.posY + (double)1.0F, target.posZ, 2, 0.3, 0.3, 0.3, (double)0.0F, new int[0]);
            }

            Particles.spawnParticle(this.world, Types.SMOKE, target.posX, target.posY + (double)target.height / (double)2.0F, target.posZ, 20, 0.4, (double)0.5F, 0.4, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{682277887, 16, 10, 240, -1, 0});
            this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.2F);
         }
      }

      public void kusanagiThrust(EntityLivingBase target) {
         if (this.kusanagiThrustCooldown <= 0) {
            this.kusanagiThrustCooldown = this.randomCooldown(this.getCooldownRange(3));
            this.setAttackState(3);
            this.attackAnimTimer = 15;
            if (this.canEntityBeSeen(target)) {
               double dmgMult = this.getDamageMultiplier();
               float normDmg = (float)((double)10.0F * dmgMult);
               float trueDmg = (float)((double)12.0F * dmgMult);
               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
               target.hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  double dx = target.posX - this.posX;
                  double dy = target.posY + (double)target.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
                  double dz = target.posZ - this.posZ;

                  for(int i = 0; i < 12; ++i) {
                     double t = (double)i / (double)12.0F;
                     ws.spawnParticle(EnumParticleTypes.END_ROD, this.posX + dx * t, this.posY + (double)this.getEyeHeight() + dy * t, this.posZ + dz * t, 1, 0.02, 0.02, 0.02, (double)0.0F, new int[0]);
                  }
               }

               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.HOSTILE, 0.8F, 0.8F);
            }
         }
      }

      public void neckLunge(EntityLivingBase target) {
         if (this.neckLungeCooldown <= 0 && !this.isDashing) {
            this.neckLungeCooldown = this.randomCooldown(this.getCooldownRange(4));
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
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)1.5F, (double)1.0F, (double)1.5F, (double)0.0F, 0.1, (double)0.0F, new int[]{673352482, 18, 10, 240, -1, 0});
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.8F, 1.3F);
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
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.8, this.posY + this.rand.nextDouble() * (double)1.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.8, 4, 0.1, 0.1, 0.1, (double)0.0F, 0.02, (double)0.0F, new int[]{539134754, 12, 6, 240});
            if (this.dashTicksRemaining <= 0) {
               this.isDashing = false;
               this.dashVelocityX = (double)0.0F;
               this.dashVelocityZ = (double)0.0F;
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 20, (double)1.5F, (double)1.0F, (double)1.5F, (double)0.0F, 0.1, (double)0.0F, new int[]{673352482, 18, 10, 240, -1, 0});
               if (this.dashTarget != null && this.dashTarget.isEntityAlive() && (double)this.getDistance(this.dashTarget) <= (double)3.5F) {
                  double dmgMult = this.getDamageMultiplier();
                  float normDmg = (float)((double)16.0F * dmgMult);
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

      public void tongueGrapple(EntityLivingBase target) {
         if (this.tongueGrappleCooldown <= 0) {
            if (this.currentPhase >= 2) {
               this.tongueGrappleCooldown = this.randomCooldown(this.getCooldownRange(5));
               this.setAttackState(5);
               this.attackAnimTimer = 15;
               if (this.canEntityBeSeen(target)) {
                  double dmgMult = this.getDamageMultiplier();
                  float normDmg = (float)((double)6.0F * dmgMult);
                  float trueDmg = (float)((double)4.0F * dmgMult);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  double dx = this.posX - target.posX;
                  double dz = this.posZ - target.posZ;
                  double dist = Math.sqrt(dx * dx + dz * dz);
                  if (dist > (double)0.0F) {
                     target.motionX = dx / dist * 0.85;
                     target.motionY = 0.2;
                     target.motionZ = dz / dist * 0.85;
                  }

                  if (target instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)target).velocityChanged = true;
                  }

                  target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 0));
                  if (this.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)this.world;
                     double tdx = target.posX - this.posX;
                     double tdy = target.posY + (double)1.0F - (this.posY + (double)1.0F);
                     double tdz = target.posZ - this.posZ;

                     for(int i = 0; i < 10; ++i) {
                        double t = (double)i / (double)10.0F;
                        ws.spawnParticle(EnumParticleTypes.SLIME, this.posX + tdx * t, this.posY + (double)1.0F + tdy * t, this.posZ + tdz * t, 2, 0.05, 0.05, 0.05, (double)0.0F, new int[0]);
                     }
                  }

                  this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.HOSTILE, 1.0F, 0.8F);
               }
            }
         }
      }

      public void snakeVomit(EntityLivingBase target) {
         if (this.snakeVomitCooldown <= 0) {
            if (this.currentPhase >= 2) {
               this.snakeVomitCooldown = this.randomCooldown(this.getCooldownRange(6));
               this.setAttackState(6);
               this.attackAnimTimer = 18;
               double dmgMult = this.getDamageMultiplier();
               float normDmg = (float)((double)8.0F * dmgMult);
               float trueDmg = (float)((double)10.0F * dmgMult);

               for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)4.0F))) {
                  if (p.isEntityAlive() && !p.isSpectator()) {
                     p.hurtResistantTime = 0;
                     p.attackEntityFrom(DamageSource.causeMobDamage(this), normDmg);
                     p.hurtResistantTime = 0;
                     p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                     p.addPotionEffect(new PotionEffect(MobEffects.POISON, 80, 1));
                  }
               }

               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)0.5F, this.posZ, 50, (double)4.0F, (double)0.5F, (double)4.0F, (double)0.0F, 0.05, (double)0.0F, new int[]{681128652, 25, 12, 240, -1, 0});
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SLIME, this.posX, this.posY + (double)1.0F, this.posZ, 30, 3.2, (double)0.5F, 3.2, 0.1, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.HOSTILE, 1.2F, 0.6F);
            }
         }
      }

      public void kusanagiBarrage(EntityLivingBase target) {
         if (this.barrageCooldown <= 0) {
            if (this.currentPhase >= 3) {
               this.barrageCooldown = this.randomCooldown(this.getCooldownRange(8));
               this.setAttackState(8);
               this.attackAnimTimer = 30;
               double dmgMult = this.getDamageMultiplier();
               float normDmg = (float)((double)10.0F * dmgMult);
               float trueDmg = (float)((double)5.0F * dmgMult);
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

      public void summonManda() {
         if (!this.mandaSummoned) {
            if (this.currentPhase >= 3) {
               this.mandaSummoned = true;
               this.broadcastMessage("§5§lOrochimaru: §7\"Kabuto! Summon Manda... NOW!\"");
               EntityOrochimaruSnake.EntityCustom snake = new EntityOrochimaruSnake.EntityCustom(this.world);
               snake.setPosition(this.posX, this.posY, this.posZ);
               snake.setOwnerUUID(this.getUniqueID().toString());
               NBTTagCompound oroData = this.getEntityData();
               if (oroData.getBoolean("questEntity")) {
                  NBTTagCompound snakeData = snake.getEntityData();
                  snakeData.setBoolean("questEntity", true);
                  if (oroData.hasKey("ownerUUID")) {
                     snakeData.setString("ownerUUID", oroData.getString("ownerUUID"));
                  }
               }

               this.world.spawnEntity(snake);
               this.mandaUUID = snake.getUniqueID();
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)2.0F, this.posZ, 100, (double)4.0F, (double)3.0F, (double)4.0F, (double)0.0F, 0.15, (double)0.0F, new int[]{815346380, 40, 20, 240, -1, 0});
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 1.0F, 0.5F);
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

      public void onDeath(DamageSource cause) {
         super.onDeath(cause);
         if (!this.world.isRemote && this.mandaUUID != null && this.world instanceof WorldServer) {
            Entity manda = ((WorldServer)this.world).getEntityFromUuid(this.mandaUUID);
            if (manda != null && manda.isEntityAlive()) {
               manda.setDead();
            }
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
         compound.setBoolean("oralRebirthUsed", this.oralRebirthUsed);
         compound.setBoolean("mandaSummoned", this.mandaSummoned);
         if (this.mandaUUID != null) {
            compound.setString("mandaUUID", this.mandaUUID.toString());
         }

         compound.setInteger("kickCooldown", this.kickCooldown);
         compound.setInteger("kusanagiSlashCooldown", this.kusanagiSlashCooldown);
         compound.setInteger("kusanagiThrustCooldown", this.kusanagiThrustCooldown);
         compound.setInteger("neckLungeCooldown", this.neckLungeCooldown);
         compound.setInteger("tongueGrappleCooldown", this.tongueGrappleCooldown);
         compound.setInteger("snakeVomitCooldown", this.snakeVomitCooldown);
         compound.setInteger("barrageCooldown", this.barrageCooldown);
         compound.setInteger("comboStep", this.comboStep);
         compound.setInteger("oralRebirthTimer", this.oralRebirthTimer);
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
         this.oralRebirthUsed = compound.getBoolean("oralRebirthUsed");
         this.mandaSummoned = compound.getBoolean("mandaSummoned");
         if (compound.hasKey("mandaUUID")) {
            try {
               this.mandaUUID = UUID.fromString(compound.getString("mandaUUID"));
            } catch (IllegalArgumentException var4) {
               this.mandaUUID = null;
            }
         }

         String configId = compound.getString("npcConfigId");
         if (configId != null && !configId.isEmpty()) {
            this.dataManager.set(NPC_CONFIG_ID, configId);
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null) {
               this.applyNpcConfig(config);
            }
         }

         this.kickCooldown = compound.getInteger("kickCooldown");
         this.kusanagiSlashCooldown = compound.getInteger("kusanagiSlashCooldown");
         this.kusanagiThrustCooldown = compound.getInteger("kusanagiThrustCooldown");
         this.neckLungeCooldown = compound.getInteger("neckLungeCooldown");
         this.tongueGrappleCooldown = compound.getInteger("tongueGrappleCooldown");
         this.snakeVomitCooldown = compound.getInteger("snakeVomitCooldown");
         this.barrageCooldown = compound.getInteger("barrageCooldown");
         this.comboStep = compound.getInteger("comboStep");
         this.oralRebirthTimer = compound.getInteger("oralRebirthTimer");
      }

      static {
         NPC_CONFIG_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
         ATTACK_STATE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         IS_PASSIVE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
         PHASE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         CD_KICK_P1 = new int[]{12, 20};
         CD_KUSANAGI_SLASH_P1 = new int[]{40, 65};
         CD_KUSANAGI_THRUST_P1 = new int[]{70, 100};
         CD_NECK_LUNGE_P1 = new int[]{80, 120};
         CD_KICK_P2 = new int[]{8, 15};
         CD_KUSANAGI_SLASH_P2 = new int[]{30, 50};
         CD_KUSANAGI_THRUST_P2 = new int[]{50, 75};
         CD_NECK_LUNGE_P2 = new int[]{55, 85};
         CD_TONGUE_GRAPPLE_P2 = new int[]{100, 140};
         CD_SNAKE_VOMIT_P2 = new int[]{100, 140};
         CD_KICK_P3 = new int[]{6, 12};
         CD_KUSANAGI_SLASH_P3 = new int[]{20, 35};
         CD_KUSANAGI_THRUST_P3 = new int[]{35, 55};
         CD_NECK_LUNGE_P3 = new int[]{40, 60};
         CD_TONGUE_GRAPPLE_P3 = new int[]{70, 100};
         CD_SNAKE_VOMIT_P3 = new int[]{70, 100};
         CD_BARRAGE_P3 = new int[]{50, 75};
         PARALYSIS_MODIFIER_UUID = UUID.fromString("c69af92a-b96d-49b7-a396-9b3b0d77edd5");
         HEAVINESS_SPEED_UUID = UUID.fromString("7d735ff6-8872-482d-ac1f-cd2249e8f584");
      }
   }

   public static class EntityAIOrochimaruCombat extends EntityAIBase {
      private final EntityCustom orochimaru;
      private EntityLivingBase target;

      public EntityAIOrochimaruCombat(EntityCustom orochimaru) {
         this.orochimaru = orochimaru;
         this.setMutexBits(3);
      }

      public boolean shouldExecute() {
         EntityLivingBase t = this.orochimaru.getAttackTarget();
         if (t != null && t.isEntityAlive()) {
            this.target = t;
            return true;
         } else {
            return false;
         }
      }

      public boolean shouldContinueExecuting() {
         return this.target != null && this.target.isEntityAlive() && this.orochimaru.isEntityAlive();
      }

      public void resetTask() {
         this.target = null;
         this.orochimaru.setAttackState(0);
         this.orochimaru.comboStep = 0;
         this.orochimaru.comboTarget = null;
      }

      public void updateTask() {
         if (this.target != null && this.target.isEntityAlive()) {
            if (this.orochimaru.getAttackTarget() != null && this.orochimaru.getAttackTarget() != this.target) {
               this.target = this.orochimaru.getAttackTarget();
            }

            double dist = (double)this.orochimaru.getDistance(this.target);
            this.orochimaru.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
            if (this.orochimaru.isDashing) {
               this.orochimaru.getNavigator().clearPath();
            } else if (this.orochimaru.comboStep > 0) {
               this.orochimaru.getNavigator().clearPath();
            } else if (this.orochimaru.oralRebirthTimer > 0) {
               this.orochimaru.getNavigator().clearPath();
            } else {
               this.handleMovement(dist);
               switch (this.orochimaru.currentPhase) {
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
         if (dist > (double)12.0F) {
            this.orochimaru.getNavigator().tryMoveToEntityLiving(this.target, 1.4);
         } else if (dist > (double)4.0F) {
            this.orochimaru.getNavigator().tryMoveToEntityLiving(this.target, 1.1);
         } else if (dist > (double)2.0F) {
            this.orochimaru.getNavigator().tryMoveToEntityLiving(this.target, 0.7);
         } else {
            this.orochimaru.getNavigator().clearPath();
         }

      }

      private void executePhase1(double dist) {
         if (this.orochimaru.neckLungeCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.orochimaru.neckLunge(this.target);
         } else if (this.orochimaru.kusanagiThrustCooldown <= 0 && dist >= (double)8.0F && dist <= (double)15.0F) {
            this.orochimaru.kusanagiThrust(this.target);
         } else if (this.orochimaru.kusanagiSlashCooldown <= 0 && dist <= (double)3.0F) {
            this.orochimaru.kusanagiSlash(this.target);
         } else {
            if (this.orochimaru.kickCooldown <= 0 && dist <= (double)2.5F) {
               this.orochimaru.basicKick(this.target);
            }

         }
      }

      private void executePhase2(double dist) {
         if (this.orochimaru.tongueGrappleCooldown <= 0 && dist >= (double)5.0F && dist <= (double)12.0F) {
            this.orochimaru.tongueGrapple(this.target);
         } else if (this.orochimaru.neckLungeCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.orochimaru.neckLunge(this.target);
         } else if (this.orochimaru.snakeVomitCooldown <= 0 && dist <= (double)4.0F) {
            this.orochimaru.snakeVomit(this.target);
         } else if (this.orochimaru.kusanagiThrustCooldown <= 0 && dist >= (double)8.0F && dist <= (double)15.0F) {
            this.orochimaru.kusanagiThrust(this.target);
         } else if (this.orochimaru.kusanagiSlashCooldown <= 0 && dist <= (double)3.0F) {
            this.orochimaru.kusanagiSlash(this.target);
         } else {
            if (this.orochimaru.kickCooldown <= 0 && dist <= (double)2.5F) {
               this.orochimaru.basicKick(this.target);
            }

         }
      }

      private void executePhase3(double dist) {
         if (!this.orochimaru.mandaSummoned) {
            this.orochimaru.summonManda();
         } else if (this.orochimaru.barrageCooldown <= 0 && dist <= (double)3.0F) {
            this.orochimaru.kusanagiBarrage(this.target);
         } else if (this.orochimaru.tongueGrappleCooldown <= 0 && dist >= (double)5.0F && dist <= (double)12.0F) {
            this.orochimaru.tongueGrapple(this.target);
         } else if (this.orochimaru.neckLungeCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F) {
            this.orochimaru.neckLunge(this.target);
         } else if (this.orochimaru.snakeVomitCooldown <= 0 && dist <= (double)4.0F) {
            this.orochimaru.snakeVomit(this.target);
         } else if (this.orochimaru.kusanagiThrustCooldown <= 0 && dist >= (double)8.0F && dist <= (double)15.0F) {
            this.orochimaru.kusanagiThrust(this.target);
         } else if (this.orochimaru.kusanagiSlashCooldown <= 0 && dist <= (double)3.0F) {
            this.orochimaru.kusanagiSlash(this.target);
         } else {
            if (this.orochimaru.kickCooldown <= 0 && dist <= (double)2.5F) {
               this.orochimaru.basicKick(this.target);
            }

         }
      }
   }
}
