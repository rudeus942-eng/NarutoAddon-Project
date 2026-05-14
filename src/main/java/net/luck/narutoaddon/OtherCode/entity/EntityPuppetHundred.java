
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.util.PuppetCombatHelper;
import net.luck.narutoaddon.OtherCode.quest.core.QuestInstance;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityPuppetHundred extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 230;

   public EntityPuppetHundred(ElementsInfTsukAddon instance) {
      super(instance, 230);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "puppethundred"), 230).name("puppethundred").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, HundredRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class HundredRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation[] TEXTURES = new ResourceLocation[]{new ResourceLocation("narutomod", "textures/puppet_hundred1.png"), new ResourceLocation("narutomod", "textures/puppet_hundred2.png"), new ResourceLocation("narutomod", "textures/puppet_hundred3.png")};

      public HundredRenderer(RenderManager renderManager) {
         super(renderManager, new ModelHundred(), 0.5F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         int style = entity.getStyle();
         if (style < 0 || style >= TEXTURES.length) {
            style = 0;
         }

         return TEXTURES[style];
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         float f = 0.9375F;
         GlStateManager.scale(f, f, f);
         ModelHundred model = (ModelHundred)this.mainModel;

         for(int i = 0; i < model.hair.length; ++i) {
            model.hair[i].showModel = false;
         }

         int style = entity.getStyle();
         if (style >= 0 && style < model.hair.length) {
            model.hair[style].showModel = true;
         }

      }
   }

   public static class EntityCustom extends EntityCreature implements INpcConfigurable {
      private static final DataParameter<String> NPC_CONFIG_ID;
      private static final DataParameter<Integer> STYLE;
      private int meleeCooldown = 0;
      private int kunaiCD = 0;
      private int rushCD = 0;
      private int maxLifetimeTicks = 0;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.6F, 2.0F);
         this.experienceValue = 0;
         this.isImmuneToFire = false;
         this.setNoAI(false);
         this.enablePersistence();
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(NPC_CONFIG_ID, "");
         this.dataManager.register(STYLE, this.rand.nextInt(3));
      }

      public int getStyle() {
         return (Integer)this.dataManager.get(STYLE);
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

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)3.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)3000.0F);
         }

         if (this.getAttributeMap().getAttributeInstanceByName("generic.attackDamage") == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         }

         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)18.0F);
         if (this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
         }

      }

      protected void initEntityAI() {
         this.tasks.addTask(0, new EntityAISwimming(this));
         this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.2, true));
         this.tasks.addTask(4, new EntityAIWander(this, 0.8));
         this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
         this.tasks.addTask(6, new EntityAILookIdle(this));
         this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
         this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         return PuppetCombatHelper.shouldBlockFriendlyFire(source.getTrueSource()) ? false : super.attackEntityFrom(source, amount);
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

      public double getVelocity() {
         return Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      }

      public boolean isMovingForward() {
         return this.getVelocity() > 0.001;
      }

      public boolean attackEntityAsMob(Entity target) {
         if (this.meleeCooldown <= 0 && target instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase)target;
            float baseDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            living.attackEntityFrom(DamageSource.causeMobDamage(this), baseDmg * 0.6F);
            living.hurtResistantTime = 0;
            living.attackEntityFrom(DamageSource.MAGIC, baseDmg * 0.4F);
            this.swingArm(EnumHand.MAIN_HAND);
            boolean hit = true;
            if (hit) {
               this.meleeCooldown = 8;
               if (target instanceof EntityLivingBase) {
                  ((EntityLivingBase)target).knockBack(this, 0.3F, this.posX - target.posX, this.posZ - target.posZ);
               }
            }

            return hit;
         } else {
            return false;
         }
      }

      public void onLivingUpdate() {
         super.onLivingUpdate();
         if (!this.world.isRemote) {
            if (this.meleeCooldown > 0) {
               --this.meleeCooldown;
            }

            if (this.kunaiCD > 0) {
               --this.kunaiCD;
            }

            if (this.rushCD > 0) {
               --this.rushCD;
            }

            EntityLivingBase target = this.getAttackTarget();
            if (target == null || !target.isEntityAlive()) {
               EntityPlayer nearest = this.world.getClosestPlayerToEntity(this, (double)30.0F);
               if (nearest != null && nearest.isEntityAlive()) {
                  target = nearest;
               }
            }

            if (target != null && target.isEntityAlive()) {
               double dist = (double)this.getDistance(target);
               float atkDmg = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
               this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
               this.getNavigator().tryMoveToEntityLiving(target, 1.2);
               if (this.kunaiCD <= 0 && dist <= (double)20.0F) {
                  float kunaiDmg = atkDmg * 0.5F;

                  for(int i = 0; i < 2; ++i) {
                     EntityKunaiProjectile.EntityCustom kunai = new EntityKunaiProjectile.EntityCustom(this.world, this, kunaiDmg, 5.0F);
                     double dx = target.posX - this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F;
                     double dy = target.posY + (double)target.getEyeHeight() - 0.1 - kunai.posY;
                     double dz = target.posZ - this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F;
                     double d = Math.sqrt(dx * dx + dz * dz);
                     kunai.shoot(dx, dy + d * 0.1, dz, 1.8F, 1.0F);
                     this.world.spawnEntity(kunai);
                  }

                  this.swingArm(EnumHand.MAIN_HAND);
                  this.kunaiCD = 40;
               }

               if (this.rushCD <= 0 && dist >= (double)3.0F && dist <= (double)12.0F && this.rand.nextInt(4) == 0) {
                  double dx = target.posX - this.posX;
                  double dz = target.posZ - this.posZ;
                  double d = Math.sqrt(dx * dx + dz * dz);
                  if (d > (double)0.0F) {
                     double speed = 1.8;
                     this.motionX = dx / d * speed;
                     this.motionY = (double)0.25F;
                     this.motionZ = dz / d * speed;
                     this.velocityChanged = true;
                  }

                  if (target instanceof EntityLivingBase) {
                     target.hurtResistantTime = 0;
                  }

                  target.attackEntityFrom(DamageSource.causeMobDamage(this), atkDmg * 1.2F);
                  this.swingArm(EnumHand.MAIN_HAND);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY + (double)1.0F, this.posZ, 10, 0.3, (double)0.5F, 0.3, 0.1, new int[0]);
                  }

                  this.rushCD = 100;
               }
            }

            if (Math.abs(this.motionX) > 0.3 || Math.abs(this.motionZ) > 0.3 || this.motionY > 0.4) {
               this.motionX *= 0.1;
               this.motionZ *= 0.1;
               if (this.motionY > 0.4) {
                  this.motionY = 0.1;
               }

               this.velocityChanged = true;
            }

            if (this.maxLifetimeTicks > 0 && this.ticksExisted >= this.maxLifetimeTicks) {
               this.setDead();
            }

            if (this.ticksExisted > 100 && this.ticksExisted % 200 == 50) {
               NBTTagCompound data = this.getEntityData();
               if (data.getBoolean("questEntity")) {
                  String ownerStr = data.getString("ownerUUID");
                  String questId = data.getString("sharedQuestId");
                  if (ownerStr != null && !ownerStr.isEmpty() && questId != null && !questId.isEmpty()) {
                     try {
                        UUID ownerId = UUID.fromString(ownerStr);
                        QuestManager qm = QuestManager.getInstance();
                        if (qm != null) {
                           Map<String, QuestInstance> slots = qm.getActiveQuests(ownerId);
                           boolean questActive = false;

                           for(QuestInstance qi : slots.values()) {
                              if (qi.getQuestId().equals(questId)) {
                                 questActive = true;
                                 break;
                              }
                           }

                           if (!questActive) {
                              this.setDead();
                              return;
                           }
                        }
                     } catch (Exception var16) {
                     }
                  }
               }

               EntityPlayer nearest = this.world.getClosestPlayerToEntity(this, (double)48.0F);
               if (nearest == null || !nearest.isEntityAlive()) {
                  this.setDead();
                  return;
               }
            }

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
         }

      }

      public void setMaxLifetime(int ticks) {
         this.maxLifetimeTicks = ticks;
      }

      public void applyNpcConfig(NpcConfig config) {
         this.dataManager.set(NPC_CONFIG_ID, config.getConfigId());
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

         if (this.getAttributeMap().getAttributeInstanceByName("generic.attackDamage") == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         }

         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(config.getAttackDamage());
         if (!config.isPassive()) {
            this.setNoAI(false);
         }

         this.setCustomNameTag(config.getDisplayName());
         this.setAlwaysRenderNameTag(true);
      }

      public String getNpcConfigId() {
         return (String)this.dataManager.get(NPC_CONFIG_ID);
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

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         String configId = this.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            compound.setString("npcConfigId", configId);
         }

         if (this.maxLifetimeTicks > 0) {
            compound.setInteger("maxLifetimeTicks", this.maxLifetimeTicks);
         }

         compound.setInteger("style", this.getStyle());
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("maxLifetimeTicks")) {
            this.maxLifetimeTicks = compound.getInteger("maxLifetimeTicks");
         }

         if (compound.hasKey("style")) {
            this.dataManager.set(STYLE, compound.getInteger("style"));
         }

         String configId = compound.getString("npcConfigId");
         if (configId != null && !configId.isEmpty()) {
            this.dataManager.set(NPC_CONFIG_ID, configId);
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null) {
               this.applyNpcConfig(config);
            }
         }

      }

      static {
         NPC_CONFIG_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
         STYLE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelHundred extends ModelBiped {
      private final ModelRenderer jaw;
      public final ModelRenderer[] hair = new ModelRenderer[3];
      private final ModelRenderer bone3;
      private final ModelRenderer bone9;
      private final ModelRenderer bone15;
      private final ModelRenderer bone16;
      private final ModelRenderer bone4;
      private final ModelRenderer bone5;
      private final ModelRenderer bone6;
      private final ModelRenderer bone7;
      private final ModelRenderer bone8;
      private final ModelRenderer bone10;
      private final ModelRenderer bone11;
      private final ModelRenderer bone12;
      private final ModelRenderer bone13;
      private final ModelRenderer bone14;
      private final ModelRenderer bone25;
      private final ModelRenderer bone26;
      private final ModelRenderer bone27;
      private final ModelRenderer bone28;
      private final ModelRenderer bone29;
      private final ModelRenderer bone30;
      private final ModelRenderer collar;
      private final ModelRenderer collar1;
      private final ModelRenderer collar2;
      private final ModelRenderer collar3;
      private final ModelRenderer collar4;
      private final ModelRenderer collar5;
      private final ModelRenderer collar6;
      private final ModelRenderer collar7;
      private final ModelRenderer collar8;
      private final ModelRenderer collar9;
      private final ModelRenderer collar10;
      private final ModelRenderer collar11;
      private final ModelRenderer collar12;
      private final ModelRenderer collar13;
      private final ModelRenderer collar14;
      private final ModelRenderer collar15;
      private final ModelRenderer collar16;
      private final ModelRenderer collar17;
      private final ModelRenderer collar18;
      private final ModelRenderer bone;
      private final ModelRenderer bone2;

      public ModelHundred() {
         this.textureWidth = 64;
         this.textureHeight = 64;
         (this.bipedHead = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, -0.5F, false));
         (this.jaw = new ModelRenderer(this)).setRotationPoint(0.5F, -1.0F, 0.0F);
         this.bipedHead.addChild(this.jaw);
         this.jaw.cubeList.add(new ModelBox(this.jaw, 48, 24, -2.5F, -1.0F, -4.01F, 4, 2, 4, -0.5F, false));
         (this.bipedHeadwear = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         (this.hair[0] = new ModelRenderer(this)).setRotationPoint(0.0F, -5.5F, -5.5F);
         this.bipedHeadwear.addChild(this.hair[0]);
         (this.bone3 = new ModelRenderer(this)).setRotationPoint(-0.5F, -1.25F, 4.5F);
         this.hair[0].addChild(this.bone3);
         this.setRotationAngle(this.bone3, 0.2618F, 0.0F, -0.3491F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 32, 11, -4.0872F, -5.0F, -3.7481F, 8, 6, 7, -1.0F, true));
         (this.bone9 = new ModelRenderer(this)).setRotationPoint(0.5F, -1.25F, 4.5F);
         this.hair[0].addChild(this.bone9);
         this.setRotationAngle(this.bone9, 0.2618F, 0.0F, 0.3491F);
         this.bone9.cubeList.add(new ModelBox(this.bone9, 32, 11, -3.9128F, -5.0F, -3.7481F, 8, 6, 7, -1.0F, true));
         (this.bone15 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.0F, 0.0F);
         this.hair[0].addChild(this.bone15);
         this.setRotationAngle(this.bone15, -0.5236F, -0.1309F, -0.1745F);
         this.bone15.cubeList.add(new ModelBox(this.bone15, 32, 11, -4.0F, -6.0F, 0.25F, 8, 6, 7, -1.0F, true));
         (this.bone16 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.0F, 0.0F);
         this.hair[0].addChild(this.bone16);
         this.setRotationAngle(this.bone16, -0.5236F, 0.1309F, 0.1745F);
         this.bone16.cubeList.add(new ModelBox(this.bone16, 32, 11, -4.0F, -6.0F, 0.25F, 8, 6, 7, -1.0F, false));
         (this.bone4 = new ModelRenderer(this)).setRotationPoint(-0.5F, -2.0F, 1.0F);
         this.hair[0].addChild(this.bone4);
         this.setRotationAngle(this.bone4, -0.7854F, -0.1309F, -0.3054F);
         this.bone4.cubeList.add(new ModelBox(this.bone4, 32, 11, -4.0F, -6.0F, 0.25F, 8, 6, 7, -1.0F, true));
         (this.bone5 = new ModelRenderer(this)).setRotationPoint(-1.0F, -3.0F, 3.0F);
         this.hair[0].addChild(this.bone5);
         this.setRotationAngle(this.bone5, -1.0472F, -0.2182F, -0.3491F);
         this.bone5.cubeList.add(new ModelBox(this.bone5, 32, 11, -4.0F, -6.0F, 0.25F, 8, 6, 7, -1.0F, false));
         (this.bone6 = new ModelRenderer(this)).setRotationPoint(-1.25F, -3.25F, 5.0F);
         this.hair[0].addChild(this.bone6);
         this.setRotationAngle(this.bone6, -1.309F, -0.1309F, -0.3054F);
         this.bone6.cubeList.add(new ModelBox(this.bone6, 32, 11, -4.0F, -6.0F, 0.25F, 8, 6, 7, -1.0F, false));
         (this.bone7 = new ModelRenderer(this)).setRotationPoint(-1.25F, -4.25F, 9.75F);
         this.hair[0].addChild(this.bone7);
         this.setRotationAngle(this.bone7, -2.618F, -0.0436F, -0.0873F);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 32, 11, -4.0F, -6.0F, 0.25F, 8, 6, 7, -1.0F, false));
         (this.bone8 = new ModelRenderer(this)).setRotationPoint(-1.25F, -2.0F, 10.5F);
         this.hair[0].addChild(this.bone8);
         this.setRotationAngle(this.bone8, -2.9671F, 0.0F, -0.0436F);
         this.bone8.cubeList.add(new ModelBox(this.bone8, 32, 11, -4.0F, -6.0F, 0.25F, 8, 6, 7, -1.0F, true));
         (this.bone10 = new ModelRenderer(this)).setRotationPoint(0.5F, -2.0F, 1.0F);
         this.hair[0].addChild(this.bone10);
         this.setRotationAngle(this.bone10, -0.7854F, 0.1309F, 0.3054F);
         this.bone10.cubeList.add(new ModelBox(this.bone10, 32, 11, -4.0F, -6.0F, 0.25F, 8, 6, 7, -1.0F, false));
         (this.bone11 = new ModelRenderer(this)).setRotationPoint(1.0F, -3.0F, 3.0F);
         this.hair[0].addChild(this.bone11);
         this.setRotationAngle(this.bone11, -1.0472F, 0.2182F, 0.3491F);
         this.bone11.cubeList.add(new ModelBox(this.bone11, 32, 11, -4.0F, -6.0F, 0.25F, 8, 6, 7, -1.0F, true));
         (this.bone12 = new ModelRenderer(this)).setRotationPoint(1.25F, -3.25F, 5.0F);
         this.hair[0].addChild(this.bone12);
         this.setRotationAngle(this.bone12, -1.309F, 0.1309F, 0.3054F);
         this.bone12.cubeList.add(new ModelBox(this.bone12, 32, 11, -4.0F, -6.0F, 0.25F, 8, 6, 7, -1.0F, true));
         (this.bone13 = new ModelRenderer(this)).setRotationPoint(1.25F, -4.25F, 9.75F);
         this.hair[0].addChild(this.bone13);
         this.setRotationAngle(this.bone13, -2.618F, 0.0436F, 0.0873F);
         this.bone13.cubeList.add(new ModelBox(this.bone13, 32, 11, -4.0F, -6.0F, 0.25F, 8, 6, 7, -1.0F, true));
         (this.bone14 = new ModelRenderer(this)).setRotationPoint(1.25F, -2.0F, 10.5F);
         this.hair[0].addChild(this.bone14);
         this.setRotationAngle(this.bone14, -2.9671F, 0.0F, 0.0436F);
         this.bone14.cubeList.add(new ModelBox(this.bone14, 32, 11, -4.0F, -6.0F, 0.25F, 8, 6, 7, -1.0F, false));
         (this.hair[1] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.5F, 0.75F);
         this.bipedHeadwear.addChild(this.hair[1]);
         this.setRotationAngle(this.hair[1], 0.5236F, 0.0F, 0.0F);
         (this.bone25 = new ModelRenderer(this)).setRotationPoint(0.0F, -6.5F, 0.75F);
         this.hair[1].addChild(this.bone25);
         this.setRotationAngle(this.bone25, 0.0F, -0.2618F, -0.0873F);
         this.bone25.cubeList.add(new ModelBox(this.bone25, 32, 11, -4.0F, -3.0F, -1.0F, 8, 6, 7, -0.6F, true));
         (this.bone26 = new ModelRenderer(this)).setRotationPoint(0.0F, -5.5F, 0.75F);
         this.hair[1].addChild(this.bone26);
         this.setRotationAngle(this.bone26, 0.0F, 0.2618F, 0.0873F);
         this.bone26.cubeList.add(new ModelBox(this.bone26, 32, 11, -4.0F, -4.0F, -1.0F, 8, 6, 7, -0.6F, true));
         (this.hair[2] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.5F, 0.0F);
         this.bipedHeadwear.addChild(this.hair[2]);
         this.setRotationAngle(this.hair[2], -0.2618F, 0.0F, 0.0F);
         (this.bone27 = new ModelRenderer(this)).setRotationPoint(-1.0F, -9.75F, -0.5F);
         this.hair[2].addChild(this.bone27);
         this.setRotationAngle(this.bone27, 0.0F, 0.0F, -0.3491F);
         this.bone27.cubeList.add(new ModelBox(this.bone27, 32, 11, -4.0F, -3.0F, -3.5F, 8, 6, 7, -1.0F, false));
         (this.bone28 = new ModelRenderer(this)).setRotationPoint(-1.0F, -9.75F, 1.0F);
         this.hair[2].addChild(this.bone28);
         this.setRotationAngle(this.bone28, -0.5236F, 0.0F, -0.5236F);
         this.bone28.cubeList.add(new ModelBox(this.bone28, 32, 11, -4.0F, -3.0F, -3.5F, 8, 6, 7, -1.0F, true));
         (this.bone29 = new ModelRenderer(this)).setRotationPoint(1.0F, -9.75F, -0.5F);
         this.hair[2].addChild(this.bone29);
         this.setRotationAngle(this.bone29, 0.0F, 0.0F, 0.3491F);
         this.bone29.cubeList.add(new ModelBox(this.bone29, 32, 11, -4.0F, -3.0F, -3.5F, 8, 6, 7, -1.0F, true));
         (this.bone30 = new ModelRenderer(this)).setRotationPoint(1.0F, -9.75F, 1.0F);
         this.hair[2].addChild(this.bone30);
         this.setRotationAngle(this.bone30, -0.5236F, 0.0F, 0.5236F);
         this.bone30.cubeList.add(new ModelBox(this.bone30, 32, 11, -4.0F, -3.0F, -3.5F, 8, 6, 7, -1.0F, false));
         (this.bipedBody = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 0, 0, -1.0F, 0.9F, -1.0F, 2, 4, 2, 1.4F, false));
         (this.collar = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedBody.addChild(this.collar);
         (this.collar1 = new ModelRenderer(this)).setRotationPoint(-5.75F, -3.366F, 1.116F);
         this.collar.addChild(this.collar1);
         this.setRotationAngle(this.collar1, -1.6036F, 0.5164F, -1.8574F);
         this.collar1.cubeList.add(new ModelBox(this.collar1, 44, 0, -1.0F, -5.0F, -0.5F, 2, 10, 1, -0.2F, false));
         (this.collar2 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.116F, -2.884F);
         this.collar.addChild(this.collar2);
         this.setRotationAngle(this.collar2, -1.0821F, -0.0873F, 0.2618F);
         this.collar2.cubeList.add(new ModelBox(this.collar2, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, false));
         (this.collar3 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.116F, -2.884F);
         this.collar.addChild(this.collar3);
         this.setRotationAngle(this.collar3, -1.0996F, 0.0873F, -0.2618F);
         this.collar3.cubeList.add(new ModelBox(this.collar3, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, false));
         (this.collar4 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.016F, -2.884F);
         this.collar.addChild(this.collar4);
         this.setRotationAngle(this.collar4, -1.117F, -0.0873F, 0.2182F);
         this.collar4.cubeList.add(new ModelBox(this.collar4, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, true));
         (this.collar5 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.016F, -2.634F);
         this.collar.addChild(this.collar5);
         this.setRotationAngle(this.collar5, -1.1345F, 0.0873F, -0.2182F);
         this.collar5.cubeList.add(new ModelBox(this.collar5, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, true));
         (this.collar6 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.084F, -2.634F);
         this.collar.addChild(this.collar6);
         this.setRotationAngle(this.collar6, -1.1519F, -0.0873F, 0.1745F);
         this.collar6.cubeList.add(new ModelBox(this.collar6, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, false));
         (this.collar7 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.084F, -2.634F);
         this.collar.addChild(this.collar7);
         this.setRotationAngle(this.collar7, -1.1694F, 0.0873F, -0.1745F);
         this.collar7.cubeList.add(new ModelBox(this.collar7, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, false));
         (this.collar8 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.184F, -2.634F);
         this.collar.addChild(this.collar8);
         this.setRotationAngle(this.collar8, -1.1868F, -0.0873F, 0.1309F);
         this.collar8.cubeList.add(new ModelBox(this.collar8, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, true));
         (this.collar9 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.184F, -2.884F);
         this.collar.addChild(this.collar9);
         this.setRotationAngle(this.collar9, -1.2043F, 0.0873F, -0.1309F);
         this.collar9.cubeList.add(new ModelBox(this.collar9, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, true));
         (this.collar10 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.284F, -2.884F);
         this.collar.addChild(this.collar10);
         this.setRotationAngle(this.collar10, -1.2217F, -0.0873F, 0.0873F);
         this.collar10.cubeList.add(new ModelBox(this.collar10, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, false));
         (this.collar11 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.284F, -2.884F);
         this.collar.addChild(this.collar11);
         this.setRotationAngle(this.collar11, -1.2392F, 0.0873F, -0.0873F);
         this.collar11.cubeList.add(new ModelBox(this.collar11, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, false));
         (this.collar12 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.384F, -2.884F);
         this.collar.addChild(this.collar12);
         this.setRotationAngle(this.collar12, -1.2566F, -0.0873F, 0.0436F);
         this.collar12.cubeList.add(new ModelBox(this.collar12, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, true));
         (this.collar13 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.384F, -2.884F);
         this.collar.addChild(this.collar13);
         this.setRotationAngle(this.collar13, -1.2741F, 0.0873F, -0.0436F);
         this.collar13.cubeList.add(new ModelBox(this.collar13, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, true));
         (this.collar14 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.484F, -2.884F);
         this.collar.addChild(this.collar14);
         this.setRotationAngle(this.collar14, -1.2915F, -0.0873F, 0.0F);
         this.collar14.cubeList.add(new ModelBox(this.collar14, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, false));
         (this.collar15 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.484F, -2.884F);
         this.collar.addChild(this.collar15);
         this.setRotationAngle(this.collar15, -1.309F, 0.0873F, 0.0F);
         this.collar15.cubeList.add(new ModelBox(this.collar15, 32, 0, -7.0F, -10.0F, 0.0F, 14, 10, 1, 0.2F, true));
         (this.collar16 = new ModelRenderer(this)).setRotationPoint(5.75F, -3.366F, 1.116F);
         this.collar.addChild(this.collar16);
         this.setRotationAngle(this.collar16, -1.6036F, -0.5164F, 1.8574F);
         this.collar16.cubeList.add(new ModelBox(this.collar16, 44, 0, -1.0F, -5.0F, -0.5F, 2, 10, 1, -0.2F, true));
         (this.collar17 = new ModelRenderer(this)).setRotationPoint(-5.75F, -2.366F, 1.116F);
         this.collar.addChild(this.collar17);
         this.setRotationAngle(this.collar17, -1.6036F, 0.5164F, -1.8574F);
         this.collar17.cubeList.add(new ModelBox(this.collar17, 44, 0, -1.0F, -5.0F, -0.5F, 2, 10, 1, -0.2F, false));
         (this.collar18 = new ModelRenderer(this)).setRotationPoint(5.75F, -2.366F, 1.116F);
         this.collar.addChild(this.collar18);
         this.setRotationAngle(this.collar18, -1.6036F, -0.5164F, 1.8574F);
         this.collar18.cubeList.add(new ModelBox(this.collar18, 44, 0, -1.0F, -5.0F, -0.5F, 2, 10, 1, -0.2F, true));
         (this.bone = new ModelRenderer(this)).setRotationPoint(0.0F, -1.0F, -3.0F);
         this.bipedBody.addChild(this.bone);
         this.setRotationAngle(this.bone, -0.0873F, 0.0F, 0.0F);
         this.bone.cubeList.add(new ModelBox(this.bone, 16, 36, -4.0F, 1.7347F, 1.0757F, 8, 24, 4, 1.0F, false));
         (this.bone2 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.0F, 3.0F);
         this.bipedBody.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.0873F, 0.0F, 0.0F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 40, 36, -4.0F, 0.7385F, -4.9886F, 8, 24, 4, 1.0F, false));
         (this.bipedLeftLeg = new ModelRenderer(this)).setRotationPoint(-1.9F, 12.0F, 0.0F);
         this.bipedBody.addChild(this.bipedLeftLeg);
         this.bipedLeftLeg.cubeList.add(new ModelBox(this.bipedLeftLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, false));
         (this.bipedRightLeg = new ModelRenderer(this)).setRotationPoint(1.9F, 12.0F, 0.0F);
         this.bipedBody.addChild(this.bipedRightLeg);
         this.bipedRightLeg.cubeList.add(new ModelBox(this.bipedRightLeg, 0, 16, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F, true));
         (this.bipedRightArm = new ModelRenderer(this)).setRotationPoint(-5.0F, 2.0F, 0.0F);
         this.bipedRightArm.cubeList.add(new ModelBox(this.bipedRightArm, 0, 32, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, false));
         this.bipedRightArm.cubeList.add(new ModelBox(this.bipedRightArm, 0, 48, -3.0F, -2.0F, -2.0F, 4, 12, 4, 1.0F, false));
         (this.bipedLeftArm = new ModelRenderer(this)).setRotationPoint(5.0F, 2.0F, 0.0F);
         this.bipedLeftArm.cubeList.add(new ModelBox(this.bipedLeftArm, 0, 32, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, true));
         this.bipedLeftArm.cubeList.add(new ModelBox(this.bipedLeftArm, 0, 48, -1.0F, -2.0F, -2.0F, 4, 12, 4, 1.0F, true));
      }

      public void render(Entity entityIn, float f, float f1, float f2, float f3, float f4, float scale) {
         this.setRotationAngles(f, f1, f2, f3, f4, scale, entityIn);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         this.bipedHead.render(scale);
         this.bipedBody.render(scale);
         this.bipedRightArm.render(scale);
         this.bipedLeftArm.render(scale);
         this.bipedHeadwear.render(scale);
         GlStateManager.disableBlend();
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
         super.setRotationAngles(0.0F, 0.0F, f2, f3, f4, f5, e);
         if (e instanceof EntityCustom) {
            EntityCustom puppet = (EntityCustom)e;
            double d = puppet.getVelocity();
            if (d > 0.001 && puppet.isMovingForward()) {
               float fa = MathHelper.clamp((float)d * 2.5F, 0.0F, 1.0F);
               ModelRenderer var10000 = this.bipedBody;
               var10000.rotateAngleX += fa * 1.0472F;
               this.collar.rotateAngleX = fa * -0.2618F;
               if (this.swingProgress <= 0.0F && this.rightArmPose == ArmPose.EMPTY) {
                  var10000 = this.bipedRightArm;
                  var10000.rotateAngleZ += fa * 1.3963F;
               }

               if (this.leftArmPose == ArmPose.EMPTY) {
                  var10000 = this.bipedLeftArm;
                  var10000.rotateAngleZ += fa * -1.3963F;
               }

               this.bipedLeftLeg.rotateAngleX = 0.0F;
               this.bipedRightLeg.rotateAngleX = 0.0F;
            }
         }

      }
   }
}
