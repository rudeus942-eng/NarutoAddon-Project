
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
public class EntityPuppetKazekage extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 229;

   public EntityPuppetKazekage(ElementsInfTsukAddon instance) {
      super(instance, 229);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "puppetkaze"), 229).name("puppetkaze").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KazeRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class KazeRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod", "textures/puppet_3rdkazekage.png");

      public KazeRenderer(RenderManager renderManager) {
         super(renderManager, new ModelKazekage(), 0.5F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         float f = 1.0625F;
         GlStateManager.scale(f, f, f);
      }
   }

   public static class EntityCustom extends EntityCreature implements INpcConfigurable {
      private static final DataParameter<String> NPC_CONFIG_ID;
      private static final DataParameter<Boolean> MOUTH_OPEN;
      private int meleeCooldown = 0;
      private int ironSandCD = 0;
      private int senbonCD = 0;
      private int dashCD = 0;
      private int maxLifetimeTicks = 0;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.6F, 2.125F);
         this.experienceValue = 0;
         this.isImmuneToFire = false;
         this.setNoAI(false);
         this.enablePersistence();
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(NPC_CONFIG_ID, "");
         this.dataManager.register(MOUTH_OPEN, false);
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
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)10.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)12000.0F);
         }

         if (this.getAttributeMap().getAttributeInstanceByName("generic.attackDamage") == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         }

         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)30.0F);
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

      public boolean isMouthOpen() {
         return (Boolean)this.dataManager.get(MOUTH_OPEN);
      }

      public void setMouthOpen(boolean open) {
         this.dataManager.set(MOUTH_OPEN, open);
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
            living.attackEntityFrom(DamageSource.causeMobDamage(this), baseDmg * 0.5F);
            living.hurtResistantTime = 0;
            living.attackEntityFrom(DamageSource.MAGIC, baseDmg * 0.5F);
            this.swingArm(EnumHand.MAIN_HAND);
            this.meleeCooldown = 15;
            return true;
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

            if (this.ironSandCD > 0) {
               --this.ironSandCD;
            }

            if (this.senbonCD > 0) {
               --this.senbonCD;
            }

            if (this.dashCD > 0) {
               --this.dashCD;
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
               if (this.dashCD <= 0 && dist >= (double)4.0F && dist <= (double)15.0F) {
                  double behindX = target.posX - target.getLookVec().x * (double)2.0F;
                  double behindZ = target.posZ - target.getLookVec().z * (double)2.0F;
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 8, 0.3, (double)0.5F, 0.3, 0.02, new int[0]);
                  }

                  this.setPositionAndUpdate(behindX, target.posY, behindZ);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 8, 0.3, (double)0.5F, 0.3, 0.02, new int[0]);
                  }

                  if (target instanceof EntityLivingBase) {
                     target.hurtResistantTime = 0;
                  }

                  target.attackEntityFrom(DamageSource.causeMobDamage(this), atkDmg * 1.5F);
                  this.swingArm(EnumHand.MAIN_HAND);
                  this.dashCD = 160;
               }

               if (this.ironSandCD <= 0 && dist <= (double)20.0F) {
                  float projDmg = atkDmg * 1.2F;

                  for(int i = 0; i < 3; ++i) {
                     EntityKunaiProjectile.EntityCustom sand = new EntityKunaiProjectile.EntityCustom(this.world, this, projDmg, 5.0F);
                     double dx = target.posX - this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F;
                     double dy = target.posY + (double)target.getEyeHeight() - 0.1 - sand.posY;
                     double dz = target.posZ - this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F;
                     double d = Math.sqrt(dx * dx + dz * dz);
                     sand.shoot(dx, dy + d * 0.12, dz, 1.2F, 1.5F);
                     this.world.spawnEntity(sand);
                  }

                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.REDSTONE, this.posX, this.posY + (double)1.5F, this.posZ, 12, (double)0.5F, (double)0.5F, (double)0.5F, (double)0.0F, new int[0]);
                  }

                  this.setMouthOpen(true);
                  this.swingArm(EnumHand.MAIN_HAND);
                  this.ironSandCD = 60 + this.rand.nextInt(21);
               } else if (this.ironSandCD < 50) {
                  this.setMouthOpen(false);
               }

               if (this.senbonCD <= 0 && dist <= (double)25.0F) {
                  float senbonDmg = atkDmg * 0.6F;

                  for(int i = 0; i < 4; ++i) {
                     EntityKunaiProjectile.EntityCustom senbon = new EntityKunaiProjectile.EntityCustom(this.world, this, senbonDmg, 5.0F);
                     double dx = target.posX - this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F;
                     double dy = target.posY + (double)target.getEyeHeight() - 0.1 - senbon.posY;
                     double dz = target.posZ - this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F;
                     double d = Math.sqrt(dx * dx + dz * dz);
                     senbon.shoot(dx, dy + d * 0.1, dz, 2.0F, 0.5F);
                     this.world.spawnEntity(senbon);
                  }

                  this.swingArm(EnumHand.MAIN_HAND);
                  this.senbonCD = 80 + this.rand.nextInt(21);
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

      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("maxLifetimeTicks")) {
            this.maxLifetimeTicks = compound.getInteger("maxLifetimeTicks");
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
         MOUTH_OPEN = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelKazekage extends ModelBiped {
      private final ModelRenderer jaw;
      private final ModelRenderer jaw2;
      private final ModelRenderer niceHair;
      private final ModelRenderer bone17;
      private final ModelRenderer bone18;
      private final ModelRenderer bone19;
      private final ModelRenderer bone20;
      private final ModelRenderer bone21;
      private final ModelRenderer bone22;
      private final ModelRenderer bone23;
      private final ModelRenderer bone24;
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

      public ModelKazekage() {
         this.textureWidth = 64;
         this.textureHeight = 64;
         (this.bipedHead = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, -0.5F, false));
         (this.jaw = new ModelRenderer(this)).setRotationPoint(0.5F, -1.0F, 0.0F);
         this.bipedHead.addChild(this.jaw);
         this.jaw.cubeList.add(new ModelBox(this.jaw, 48, 24, -2.5F, -1.0F, -4.01F, 4, 2, 4, -0.5F, false));
         (this.jaw2 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.0F, 0.0F);
         this.bipedHead.addChild(this.jaw2);
         this.jaw2.cubeList.add(new ModelBox(this.jaw2, 32, 24, -4.0F, -3.0F, -4.0F, 3, 4, 4, -0.51F, false));
         this.jaw2.cubeList.add(new ModelBox(this.jaw2, 32, 24, 1.0F, -3.0F, -4.0F, 3, 4, 4, -0.51F, true));
         (this.bipedHeadwear = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         (this.niceHair = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHeadwear.addChild(this.niceHair);
         (this.bone17 = new ModelRenderer(this)).setRotationPoint(1.005F, -7.9697F, -3.8572F);
         this.niceHair.addChild(this.bone17);
         this.setRotationAngle(this.bone17, -1.4399F, -0.48F, 0.0873F);
         this.bone17.cubeList.add(new ModelBox(this.bone17, 32, 11, -4.0F, -7.15F, 0.35F, 8, 6, 7, -0.4F, true));
         (this.bone18 = new ModelRenderer(this)).setRotationPoint(-1.005F, -7.9697F, -3.8572F);
         this.niceHair.addChild(this.bone18);
         this.setRotationAngle(this.bone18, -1.4399F, 0.48F, -0.0873F);
         this.bone18.cubeList.add(new ModelBox(this.bone18, 32, 11, -4.0F, -7.15F, 0.35F, 8, 6, 7, -0.4F, false));
         (this.bone19 = new ModelRenderer(this)).setRotationPoint(0.005F, -7.9697F, -3.8572F);
         this.niceHair.addChild(this.bone19);
         this.setRotationAngle(this.bone19, -1.4399F, -0.1745F, 0.0F);
         this.bone19.cubeList.add(new ModelBox(this.bone19, 32, 11, -4.0F, -7.15F, 0.35F, 8, 6, 7, -0.2F, false));
         (this.bone20 = new ModelRenderer(this)).setRotationPoint(0.005F, -7.9697F, -3.8572F);
         this.niceHair.addChild(this.bone20);
         this.setRotationAngle(this.bone20, -1.2654F, 0.1745F, 0.0F);
         this.bone20.cubeList.add(new ModelBox(this.bone20, 32, 11, -4.0F, -6.65F, 0.25F, 8, 6, 7, -0.25F, true));
         (this.bone21 = new ModelRenderer(this)).setRotationPoint(0.005F, -7.7197F, -3.8572F);
         this.niceHair.addChild(this.bone21);
         this.setRotationAngle(this.bone21, -1.0908F, -0.0873F, 0.0F);
         this.bone21.cubeList.add(new ModelBox(this.bone21, 32, 11, -4.0F, -6.15F, 0.15F, 8, 6, 7, -0.3F, false));
         (this.bone22 = new ModelRenderer(this)).setRotationPoint(0.005F, -7.7197F, -3.8572F);
         this.niceHair.addChild(this.bone22);
         this.setRotationAngle(this.bone22, -0.8727F, 0.0873F, 0.0F);
         this.bone22.cubeList.add(new ModelBox(this.bone22, 32, 11, -4.0F, -6.15F, 0.15F, 8, 6, 7, -0.35F, true));
         (this.bone23 = new ModelRenderer(this)).setRotationPoint(-2.9118F, -7.6983F, -1.7837F);
         this.niceHair.addChild(this.bone23);
         this.setRotationAngle(this.bone23, -0.0873F, -0.7854F, 0.0873F);
         this.bone23.cubeList.add(new ModelBox(this.bone23, 16, 16, -1.0536F, 0.1951F, -0.6367F, 2, 10, 2, 0.5F, true));
         (this.bone24 = new ModelRenderer(this)).setRotationPoint(2.9118F, -7.6983F, -1.7837F);
         this.niceHair.addChild(this.bone24);
         this.setRotationAngle(this.bone24, -0.0873F, 0.7854F, -0.0873F);
         this.bone24.cubeList.add(new ModelBox(this.bone24, 16, 16, -0.9464F, 0.1951F, -0.6367F, 2, 10, 2, 0.5F, false));
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
         this.bipedHead.render(scale);
         this.bipedBody.render(scale);
         this.bipedRightArm.render(scale);
         this.bipedLeftArm.render(scale);
         this.bipedHeadwear.render(scale);
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

            if (puppet.isMouthOpen()) {
               this.jaw.rotateAngleX = 0.2618F;
               this.jaw2.rotateAngleX = 0.1745F;
            } else {
               this.jaw.rotateAngleX = 0.0F;
               this.jaw2.rotateAngleX = 0.0F;
            }
         }

      }
   }
}
