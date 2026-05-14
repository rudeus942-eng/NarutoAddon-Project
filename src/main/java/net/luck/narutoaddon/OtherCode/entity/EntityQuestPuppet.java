
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.util.PuppetCombatHelper;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityQuestPuppet extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 23;
   public static final int ENTITYID_RANGED = 24;

   public EntityQuestPuppet(ElementsInfTsukAddon instance) {
      super(instance, 39);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "questpuppet"), 23).name("questpuppet").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, PuppetRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class PuppetRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation KARASU_TEXTURE = new ResourceLocation("narutomod", "textures/karasu.png");

      public PuppetRenderer(RenderManager renderManager) {
         super(renderManager, new ModelKarasu(), 0.5F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return KARASU_TEXTURE;
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         GlStateManager.scale(1.0F, 1.125F, 1.0F);
      }
   }

   public static class EntityCustom extends EntityCreature implements INpcConfigurable {
      private static final DataParameter<String> NPC_CONFIG_ID;
      private static final DataParameter<Boolean> KNIVES_OUT;
      private static final DataParameter<Boolean> MOUTH_OPEN;
      private int meleeCooldown = 0;
      private int rangedCooldown = 0;
      private int maxLifetimeTicks = 0;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.6F, 2.2F);
         this.experienceValue = 0;
         this.isImmuneToFire = false;
         this.setNoAI(false);
         this.enablePersistence();
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(NPC_CONFIG_ID, "");
         this.dataManager.register(KNIVES_OUT, false);
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
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)2.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.34);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)3000.0F);
         }

         if (this.getAttributeMap().getAttributeInstanceByName("generic.attackDamage") == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         }

         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)6.0F);
         if (this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.8);
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

      public boolean isKnivesOut() {
         return (Boolean)this.dataManager.get(KNIVES_OUT);
      }

      public void setKnivesOut(boolean out) {
         this.dataManager.set(KNIVES_OUT, out);
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
         if (this.meleeCooldown > 0) {
            return false;
         } else {
            float damage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
            if (hit) {
               this.meleeCooldown = 15;
               if (target instanceof EntityLivingBase) {
                  ((EntityLivingBase)target).knockBack(this, 0.4F, this.posX - target.posX, this.posZ - target.posZ);
               }
            }

            return hit;
         }
      }

      public void onLivingUpdate() {
         super.onLivingUpdate();
         if (!this.world.isRemote) {
            if (this.meleeCooldown > 0) {
               --this.meleeCooldown;
            }

            if (this.rangedCooldown > 0) {
               --this.rangedCooldown;
            }

            EntityLivingBase target = this.getAttackTarget();
            if (target != null && !this.isKnivesOut()) {
               this.setKnivesOut(true);
            }

            if (target == null && this.isKnivesOut()) {
               this.setKnivesOut(false);
            }

            if (target != null && this.rangedCooldown <= 0) {
               double dist = (double)this.getDistance(target);
               if (dist > (double)4.0F && dist < (double)16.0F && this.getEntitySenses().canSee(target)) {
                  this.throwKunai(target);
                  this.rangedCooldown = 30;
                  this.setMouthOpen(true);
               }
            }

            if (this.isMouthOpen() && this.rangedCooldown < 25) {
               this.setMouthOpen(false);
            }

            if (this.ticksExisted % 20 == 0 && target != null && this.rand.nextFloat() < 0.3F) {
               double jerkX = (this.rand.nextDouble() - (double)0.5F) * 0.15;
               double jerkZ = (this.rand.nextDouble() - (double)0.5F) * 0.15;
               this.motionX += jerkX;
               this.motionZ += jerkZ;
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

      private void throwKunai(EntityLivingBase target) {
         EntityKunaiProjectile.EntityCustom kunai = new EntityKunaiProjectile.EntityCustom(this.world, this);
         Vec3d targetPos = target.getPositionEyes(1.0F);
         Vec3d myPos = this.getPositionEyes(1.0F);
         double dx = targetPos.x - myPos.x;
         double dy = targetPos.y - myPos.y;
         double dz = targetPos.z - myPos.z;
         kunai.shoot(dx, dy, dz, 1.6F, 2.0F);
         this.world.spawnEntity(kunai);
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
         KNIVES_OUT = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
         MOUTH_OPEN = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelKarasu extends ModelBiped {
      private final ModelRenderer jaw;
      private final ModelRenderer bone11;
      private final ModelRenderer shooter;
      private final ModelRenderer bone8;
      private final ModelRenderer bone3;
      private final ModelRenderer bone27;
      private final ModelRenderer bone12;
      private final ModelRenderer bone19;
      private final ModelRenderer bone32;
      private final ModelRenderer bone25;
      private final ModelRenderer bone26;
      private final ModelRenderer bone33;
      private final ModelRenderer bone22;
      private final ModelRenderer bone34;
      private final ModelRenderer bone23;
      private final ModelRenderer bone35;
      private final ModelRenderer bone24;
      private final ModelRenderer bone20;
      private final ModelRenderer bone21;
      private final ModelRenderer bone36;
      private final ModelRenderer bone37;
      private final ModelRenderer bone16;
      private final ModelRenderer bone28;
      private final ModelRenderer bone17;
      private final ModelRenderer bone18;
      private final ModelRenderer bone29;
      private final ModelRenderer bone13;
      private final ModelRenderer bone30;
      private final ModelRenderer bone14;
      private final ModelRenderer bone15;
      private final ModelRenderer bone31;
      private final ModelRenderer rightArm2;
      private final ModelRenderer bone5;
      private final ModelRenderer blade2;
      private final ModelRenderer leftArm2;
      private final ModelRenderer bone7;
      private final ModelRenderer blade3;
      private final ModelRenderer bone4;
      private final ModelRenderer blade0;
      private final ModelRenderer bone6;
      private final ModelRenderer blade1;

      public ModelKarasu() {
         this.textureWidth = 64;
         this.textureHeight = 64;
         (this.bipedHead = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, -0.8F, false));
         (this.jaw = new ModelRenderer(this)).setRotationPoint(0.0F, -0.85F, -1.75F);
         this.bipedHead.addChild(this.jaw);
         (this.bone11 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.3F, -0.1F);
         this.jaw.addChild(this.bone11);
         this.setRotationAngle(this.bone11, -0.2618F, 0.0F, 0.0F);
         this.bone11.cubeList.add(new ModelBox(this.bone11, 28, 0, -2.0F, -1.0F, -2.0F, 4, 3, 2, -0.6F, false));
         this.bone11.cubeList.add(new ModelBox(this.bone11, 48, 0, -4.0F, -3.25F, 1.99F, 8, 8, 0, -3.4F, false));
         (this.shooter = new ModelRenderer(this)).setRotationPoint(0.0F, -1.0F, -4.0F);
         this.bipedHead.addChild(this.shooter);
         this.shooter.cubeList.add(new ModelBox(this.shooter, 11, 16, -0.5F, -0.5F, -1.0F, 1, 1, 2, -0.2F, false));
         (this.bipedHeadwear = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         (this.bone8 = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.5F);
         this.bipedHeadwear.addChild(this.bone8);
         this.setRotationAngle(this.bone8, -0.2618F, 0.0F, 0.0F);
         this.bone8.cubeList.add(new ModelBox(this.bone8, 32, 0, -4.0F, -4.0F, -4.0F, 8, 8, 8, 0.2F, false));
         (this.bone3 = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.5F);
         this.bipedHeadwear.addChild(this.bone3);
         this.setRotationAngle(this.bone3, 0.2618F, 0.0F, 0.0F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 32, 0, -4.0F, -4.0F, -4.0F, 8, 8, 8, 0.2F, true));
         (this.bone27 = new ModelRenderer(this)).setRotationPoint(0.0F, -4.5F, 0.5F);
         this.bipedHeadwear.addChild(this.bone27);
         this.setRotationAngle(this.bone27, 0.2618F, 0.0F, 0.0F);
         (this.bone12 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone12);
         this.setRotationAngle(this.bone12, -0.2618F, 0.0F, -3.1416F);
         this.bone12.cubeList.add(new ModelBox(this.bone12, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone19 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone19);
         this.setRotationAngle(this.bone19, 0.2618F, 0.0F, -2.8798F);
         this.bone19.cubeList.add(new ModelBox(this.bone19, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone32 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone32);
         this.setRotationAngle(this.bone32, 0.2618F, 0.0F, -2.3562F);
         this.bone32.cubeList.add(new ModelBox(this.bone32, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone25 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone25);
         this.setRotationAngle(this.bone25, 0.2618F, 0.0F, -1.8326F);
         this.bone25.cubeList.add(new ModelBox(this.bone25, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone26 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone26);
         this.setRotationAngle(this.bone26, 0.2618F, 0.0F, -1.309F);
         this.bone26.cubeList.add(new ModelBox(this.bone26, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone33 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone33);
         this.setRotationAngle(this.bone33, 0.2618F, 0.0F, -0.7854F);
         this.bone33.cubeList.add(new ModelBox(this.bone33, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone22 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone22);
         this.setRotationAngle(this.bone22, 0.2618F, 0.0F, 2.8798F);
         this.bone22.cubeList.add(new ModelBox(this.bone22, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone34 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone34);
         this.setRotationAngle(this.bone34, 0.2618F, 0.0F, 2.3562F);
         this.bone34.cubeList.add(new ModelBox(this.bone34, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone23 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone23);
         this.setRotationAngle(this.bone23, 0.2618F, 0.0F, 1.8326F);
         this.bone23.cubeList.add(new ModelBox(this.bone23, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone35 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone35);
         this.setRotationAngle(this.bone35, 0.2618F, 0.0F, 1.309F);
         this.bone35.cubeList.add(new ModelBox(this.bone35, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone24 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone24);
         this.setRotationAngle(this.bone24, 0.2618F, 0.0F, 0.7854F);
         this.bone24.cubeList.add(new ModelBox(this.bone24, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone20 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone20);
         this.setRotationAngle(this.bone20, 1.0472F, 0.0F, -3.1416F);
         this.bone20.cubeList.add(new ModelBox(this.bone20, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone21 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone21);
         this.setRotationAngle(this.bone21, 1.5708F, 0.0F, 0.0F);
         this.bone21.cubeList.add(new ModelBox(this.bone21, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone36 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone36);
         this.setRotationAngle(this.bone36, 1.0472F, 0.0F, 0.0F);
         this.bone36.cubeList.add(new ModelBox(this.bone36, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone37 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone37);
         this.setRotationAngle(this.bone37, 0.5236F, 0.0F, 0.0F);
         this.bone37.cubeList.add(new ModelBox(this.bone37, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone16 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone16);
         this.setRotationAngle(this.bone16, -0.2618F, 0.0F, -2.618F);
         this.bone16.cubeList.add(new ModelBox(this.bone16, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, true));
         (this.bone28 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone28);
         this.setRotationAngle(this.bone28, -0.2618F, 0.0F, -2.0944F);
         this.bone28.cubeList.add(new ModelBox(this.bone28, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, true));
         (this.bone17 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone17);
         this.setRotationAngle(this.bone17, -0.2618F, 0.0F, -1.5708F);
         this.bone17.cubeList.add(new ModelBox(this.bone17, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, true));
         (this.bone18 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone18);
         this.setRotationAngle(this.bone18, -0.2618F, 0.0F, -1.0472F);
         this.bone18.cubeList.add(new ModelBox(this.bone18, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, true));
         (this.bone29 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone29);
         this.setRotationAngle(this.bone29, -0.2618F, 0.0F, -0.5236F);
         this.bone29.cubeList.add(new ModelBox(this.bone29, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, true));
         (this.bone13 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone13);
         this.setRotationAngle(this.bone13, -0.2618F, 0.0F, 2.618F);
         this.bone13.cubeList.add(new ModelBox(this.bone13, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone30 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone30);
         this.setRotationAngle(this.bone30, -0.2618F, 0.0F, 2.0944F);
         this.bone30.cubeList.add(new ModelBox(this.bone30, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone14 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone14);
         this.setRotationAngle(this.bone14, -0.2618F, 0.0F, 1.5708F);
         this.bone14.cubeList.add(new ModelBox(this.bone14, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone15 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone15);
         this.setRotationAngle(this.bone15, -0.2618F, 0.0F, 1.0472F);
         this.bone15.cubeList.add(new ModelBox(this.bone15, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bone31 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.0F);
         this.bone27.addChild(this.bone31);
         this.setRotationAngle(this.bone31, -0.2618F, 0.0F, 0.5236F);
         this.bone31.cubeList.add(new ModelBox(this.bone31, 32, 0, -4.0F, -1.0F, -4.0F, 8, 8, 8, -1.0F, false));
         (this.bipedBody = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F, false));
         this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 16, 32, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.6F, false));
         (this.bipedLeftLeg = new ModelRenderer(this)).setRotationPoint(-1.9F, 12.0F, 0.0F);
         this.bipedBody.addChild(this.bipedLeftLeg);
         this.setRotationAngle(this.bipedLeftLeg, 0.0F, 0.0F, 0.0873F);
         this.bipedLeftLeg.cubeList.add(new ModelBox(this.bipedLeftLeg, 1, 17, -1.5F, 0.0F, -1.5F, 3, 12, 3, -0.2F, true));
         this.bipedLeftLeg.cubeList.add(new ModelBox(this.bipedLeftLeg, 0, 32, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.6F, false));
         (this.bipedRightLeg = new ModelRenderer(this)).setRotationPoint(1.9F, 12.0F, 0.0F);
         this.bipedBody.addChild(this.bipedRightLeg);
         this.setRotationAngle(this.bipedRightLeg, 0.0F, 0.0F, -0.0873F);
         this.bipedRightLeg.cubeList.add(new ModelBox(this.bipedRightLeg, 1, 17, -1.5F, 0.0F, -1.5F, 3, 12, 3, -0.2F, false));
         this.bipedRightLeg.cubeList.add(new ModelBox(this.bipedRightLeg, 0, 48, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.6F, false));
         (this.rightArm2 = new ModelRenderer(this)).setRotationPoint(-5.0F, 6.5F, 0.0F);
         this.bipedBody.addChild(this.rightArm2);
         this.setRotationAngle(this.rightArm2, 0.0F, 0.0F, 0.2618F);
         this.rightArm2.cubeList.add(new ModelBox(this.rightArm2, 40, 32, -2.0F, -2.0F, -2.0F, 3, 12, 4, 0.6F, false));
         this.rightArm2.cubeList.add(new ModelBox(this.rightArm2, 52, 21, -2.0F, -2.0F, -1.5F, 3, 8, 3, -0.2F, false));
         (this.bone5 = new ModelRenderer(this)).setRotationPoint(-1.75F, 5.85F, 1.75F);
         this.rightArm2.addChild(this.bone5);
         this.setRotationAngle(this.bone5, -0.0873F, 0.0F, -0.0873F);
         this.bone5.cubeList.add(new ModelBox(this.bone5, 40, 21, -0.2282F, -0.249F, -3.25F, 3, 8, 3, -0.2F, false));
         (this.blade2 = new ModelRenderer(this)).setRotationPoint(0.25F, 12.65F, -1.75F);
         this.bone5.addChild(this.blade2);
         this.blade2.cubeList.add(new ModelBox(this.blade2, 24, 0, 1.0F, -5.0F, -1.0F, 0, 6, 2, 0.0F, false));
         (this.leftArm2 = new ModelRenderer(this)).setRotationPoint(5.0F, 6.5F, 0.0F);
         this.bipedBody.addChild(this.leftArm2);
         this.setRotationAngle(this.leftArm2, 0.0F, 0.0F, -0.2618F);
         this.leftArm2.cubeList.add(new ModelBox(this.leftArm2, 40, 32, -1.0F, -2.0F, -2.0F, 3, 12, 4, 0.6F, true));
         this.leftArm2.cubeList.add(new ModelBox(this.leftArm2, 52, 21, -1.0F, -2.0F, -1.5F, 3, 8, 3, -0.2F, true));
         (this.bone7 = new ModelRenderer(this)).setRotationPoint(1.75F, 5.85F, 1.75F);
         this.leftArm2.addChild(this.bone7);
         this.setRotationAngle(this.bone7, -0.0873F, 0.0F, 0.0873F);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 40, 21, -2.7718F, -0.249F, -3.25F, 3, 8, 3, -0.2F, true));
         (this.blade3 = new ModelRenderer(this)).setRotationPoint(-0.25F, 12.65F, -1.75F);
         this.bone7.addChild(this.blade3);
         this.blade3.cubeList.add(new ModelBox(this.blade3, 24, 0, -1.0F, -5.0F, -1.0F, 0, 6, 2, 0.0F, true));
         (this.bipedRightArm = new ModelRenderer(this)).setRotationPoint(-5.0F, 2.5F, 0.0F);
         this.setRotationAngle(this.bipedRightArm, 0.0F, 0.0F, 0.5236F);
         this.bipedRightArm.cubeList.add(new ModelBox(this.bipedRightArm, 40, 32, -2.0F, -2.0F, -2.0F, 3, 12, 4, 0.6F, false));
         this.bipedRightArm.cubeList.add(new ModelBox(this.bipedRightArm, 52, 21, -2.0F, -2.0F, -1.5F, 3, 8, 3, -0.2F, false));
         (this.bone4 = new ModelRenderer(this)).setRotationPoint(-1.75F, 5.85F, 1.75F);
         this.bipedRightArm.addChild(this.bone4);
         this.setRotationAngle(this.bone4, -0.0873F, 0.0F, -0.0873F);
         this.bone4.cubeList.add(new ModelBox(this.bone4, 40, 21, -0.2282F, -0.249F, -3.25F, 3, 8, 3, -0.2F, false));
         (this.blade0 = new ModelRenderer(this)).setRotationPoint(0.25F, 12.65F, -1.75F);
         this.bone4.addChild(this.blade0);
         this.blade0.cubeList.add(new ModelBox(this.blade0, 24, 0, 1.0F, -5.0F, -1.0F, 0, 6, 2, 0.0F, false));
         (this.bipedLeftArm = new ModelRenderer(this)).setRotationPoint(5.0F, 2.5F, 0.0F);
         this.setRotationAngle(this.bipedLeftArm, 0.0F, 0.0F, -0.5236F);
         this.bipedLeftArm.cubeList.add(new ModelBox(this.bipedLeftArm, 40, 32, -1.0F, -2.0F, -2.0F, 3, 12, 4, 0.6F, true));
         this.bipedLeftArm.cubeList.add(new ModelBox(this.bipedLeftArm, 52, 21, -1.0F, -2.0F, -1.5F, 3, 8, 3, -0.2F, true));
         (this.bone6 = new ModelRenderer(this)).setRotationPoint(1.75F, 5.85F, 1.75F);
         this.bipedLeftArm.addChild(this.bone6);
         this.setRotationAngle(this.bone6, -0.0873F, 0.0F, 0.0873F);
         this.bone6.cubeList.add(new ModelBox(this.bone6, 40, 21, -2.7718F, -0.249F, -3.25F, 3, 8, 3, -0.2F, true));
         (this.blade1 = new ModelRenderer(this)).setRotationPoint(-0.25F, 12.65F, -1.75F);
         this.bone6.addChild(this.blade1);
         this.blade1.cubeList.add(new ModelBox(this.blade1, 24, 0, -1.0F, -5.0F, -1.0F, 0, 6, 2, 0.0F, true));
      }

      public void render(Entity entityIn, float f, float f1, float f2, float f3, float f4, float scale) {
         this.setRotationAngles(f, f1, f2, f3, f4, scale, entityIn);
         this.bipedHead.render(scale);
         this.bipedHeadwear.render(scale);
         this.bipedBody.render(scale);
         this.bipedRightArm.render(scale);
         this.bipedLeftArm.render(scale);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
         super.setRotationAngles(0.0F, 0.0F, f2, f3, f4, f5, e);
         ModelRenderer var10000 = this.bipedRightArm;
         var10000.rotateAngleZ += 0.5236F;
         var10000 = this.bipedLeftArm;
         var10000.rotateAngleZ -= 0.5236F;
         this.bipedLeftLeg.rotateAngleZ = 0.0873F;
         this.bipedRightLeg.rotateAngleZ = -0.0873F;
         if (e instanceof EntityCustom) {
            EntityCustom puppet = (EntityCustom)e;
            boolean knives = puppet.isKnivesOut();
            this.blade0.showModel = knives;
            this.blade1.showModel = knives;
            this.blade2.showModel = knives;
            this.blade3.showModel = knives;
            boolean mouth = puppet.isMouthOpen();
            this.shooter.showModel = mouth;
            this.jaw.rotateAngleX = mouth ? 0.3491F : 0.0F;
            double velocity = puppet.getVelocity();
            if (velocity > 0.001 && puppet.isMovingForward()) {
               float fa = MathHelper.clamp((float)velocity * 2.5F, 0.0F, 1.0F);
               var10000 = this.bipedBody;
               var10000.rotateAngleX += fa * 1.0472F;
               if (this.swingProgress <= 0.0F && this.rightArmPose == ArmPose.EMPTY) {
                  var10000 = this.bipedRightArm;
                  var10000.rotateAngleX += fa;
               }

               if (this.leftArmPose == ArmPose.EMPTY) {
                  var10000 = this.bipedLeftArm;
                  var10000.rotateAngleX += fa;
               }

               this.bipedLeftLeg.rotateAngleX = 0.0F;
               this.bipedRightLeg.rotateAngleX = 0.0F;
            }
         }

         if (this.swingProgress > 0.0F) {
            this.rightArm2.rotateAngleX = this.bipedRightArm.rotateAngleX;
            this.rightArm2.rotateAngleY = this.bipedRightArm.rotateAngleY;
            this.rightArm2.rotateAngleZ = this.bipedRightArm.rotateAngleZ - 0.2618F;
            this.leftArm2.rotateAngleX = this.bipedLeftArm.rotateAngleX;
            this.leftArm2.rotateAngleY = this.bipedLeftArm.rotateAngleY;
            this.leftArm2.rotateAngleZ = this.bipedLeftArm.rotateAngleZ + 0.2618F;
         } else {
            this.rightArm2.rotateAngleX = MathHelper.sin(f2 * 0.067F) * 0.03F;
            this.rightArm2.rotateAngleY = 0.0F;
            this.rightArm2.rotateAngleZ = MathHelper.cos(f2 * 0.09F) * 0.03F + 0.2182F + 0.03F;
            this.leftArm2.rotateAngleX = -MathHelper.sin(f2 * 0.067F) * 0.03F;
            this.leftArm2.rotateAngleY = 0.0F;
            this.leftArm2.rotateAngleZ = -MathHelper.cos(f2 * 0.09F) * 0.03F - 0.2618F - 0.03F;
         }

      }
   }
}
