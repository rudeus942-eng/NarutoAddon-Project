
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.Item;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntitySubLog extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 1;
   public static final int ENTITYID_RANGED = 2;

   public EntitySubLog(ElementsInfTsukAddon instance) {
      super(instance, 1);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "sublog"), 1).name("sublog").tracker(64, 3, true).build());
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
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, (renderManager) -> new RenderLiving(renderManager, new Modellog(), 0.0F) {
            protected ResourceLocation getEntityTexture(Entity entity) {
               return new ResourceLocation("inftsukaddon:textures/log.png");
            }
         });
   }

   public static class EntityCustom extends EntityMob {
      private static final DataParameter<Float> TILT_X;
      private static final DataParameter<Float> TILT_Z;
      private long spawnTime;
      private boolean hasLanded = false;
      private int teetertotterTimer = 0;
      private boolean animationComplete = false;
      private double landedX;
      private double landedY;
      private double landedZ;
      private float tiltAngleX = 0.0F;
      private float tiltAngleZ = 0.0F;
      private float tiltVelocityX = 0.0F;
      private float tiltVelocityZ = 0.0F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.6F, 1.8F);
         this.experienceValue = 0;
         this.isImmuneToFire = true;
         this.setNoAI(true);
         this.enablePersistence();
         this.spawnTime = world.getTotalWorldTime();
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(TILT_X, 0.0F);
         this.dataManager.register(TILT_Z, 0.0F);
      }

      public float getTiltX() {
         return (Float)this.dataManager.get(TILT_X);
      }

      public float getTiltZ() {
         return (Float)this.dataManager.get(TILT_Z);
      }

      public void fall(float distance, float damageMultiplier) {
      }

      public void onUpdate() {
         super.onUpdate();
         if (this.world.getTotalWorldTime() - this.spawnTime > 100L) {
            this.setDead();
         } else if (this.animationComplete) {
            this.motionX = (double)0.0F;
            this.motionY = (double)0.0F;
            this.motionZ = (double)0.0F;
            this.posX = this.landedX;
            this.posY = this.landedY;
            this.posZ = this.landedZ;
            this.setPosition(this.landedX, this.landedY, this.landedZ);
         } else {
            if (!this.onGround && !this.isInWater()) {
               this.motionY -= 0.03;
               this.motionY *= 0.95;
               if (this.motionY < -0.3) {
                  this.motionY = -0.3;
               }
            }

            if (this.onGround) {
               if (!this.hasLanded) {
                  this.hasLanded = true;
                  this.teetertotterTimer = 60;
                  this.landedX = this.posX;
                  this.landedY = this.posY;
                  this.landedZ = this.posZ;
                  this.tiltVelocityX = (this.rand.nextFloat() - 0.5F) * 15.0F;
                  this.tiltVelocityZ = (this.rand.nextFloat() - 0.5F) * 15.0F;
                  this.tiltAngleX = (this.rand.nextFloat() - 0.5F) * 20.0F;
                  this.tiltAngleZ = (this.rand.nextFloat() - 0.5F) * 20.0F;
               }

               this.posX = this.landedX;
               this.posY = this.landedY;
               this.posZ = this.landedZ;
               this.motionX = (double)0.0F;
               this.motionZ = (double)0.0F;
               if (this.teetertotterTimer > 0) {
                  --this.teetertotterTimer;
                  float springForce = 0.3F;
                  float damping = 0.92F;
                  this.tiltVelocityX += -this.tiltAngleX * springForce;
                  this.tiltVelocityZ += -this.tiltAngleZ * springForce;
                  this.tiltVelocityX *= damping;
                  this.tiltVelocityZ *= damping;
                  this.tiltAngleX += this.tiltVelocityX;
                  this.tiltAngleZ += this.tiltVelocityZ;
                  this.tiltAngleX = Math.max(-25.0F, Math.min(25.0F, this.tiltAngleX));
                  this.tiltAngleZ = Math.max(-25.0F, Math.min(25.0F, this.tiltAngleZ));
                  if (!this.world.isRemote) {
                     this.dataManager.set(TILT_X, this.tiltAngleX);
                     this.dataManager.set(TILT_Z, this.tiltAngleZ);
                  }
               } else {
                  this.animationComplete = true;
                  this.tiltAngleX = 0.0F;
                  this.tiltAngleZ = 0.0F;
                  this.dataManager.set(TILT_X, 0.0F);
                  this.dataManager.set(TILT_Z, 0.0F);
                  this.motionX = (double)0.0F;
                  this.motionY = (double)0.0F;
                  this.motionZ = (double)0.0F;
               }
            }

            if (!this.hasLanded) {
               this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            }

         }
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         return false;
      }

      public boolean isEntityInvulnerable(DamageSource source) {
         return true;
      }

      public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
      }

      public void addVelocity(double x, double y, double z) {
         if (!this.hasLanded) {
            super.addVelocity(x, y, z);
         }
      }

      public boolean canBePushed() {
         return false;
      }

      protected void collideWithEntity(Entity entityIn) {
      }

      public void applyEntityCollision(Entity entityIn) {
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
         return null;
      }

      public SoundEvent getDeathSound() {
         return null;
      }

      protected float getSoundVolume() {
         return 1.0F;
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
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)10.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)0.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
         }

      }

      static {
         TILT_X = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         TILT_Z = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   public static class Modellog extends ModelBase {
      private final ModelRenderer log;

      public Modellog() {
         this.textureWidth = 8;
         this.textureHeight = 4;
         this.log = new ModelRenderer(this);
         this.log.setRotationPoint(0.0F, 24.0F, 0.0F);
         this.log.cubeList.add(new ModelBox(this.log, 0, 0, -1.0F, -3.625F, -1.0F, 2, 2, 2, 1.75F, false));
         this.log.cubeList.add(new ModelBox(this.log, 0, 0, -1.0F, -9.125F, -1.0F, 2, 2, 2, 1.75F, false));
      }

      public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
         if (entity instanceof EntityCustom) {
            EntityCustom logEntity = (EntityCustom)entity;
            float tiltX = logEntity.getTiltX();
            float tiltZ = logEntity.getTiltZ();
            this.log.rotateAngleX = (float)Math.toRadians((double)tiltX);
            this.log.rotateAngleZ = (float)Math.toRadians((double)tiltZ);
         }

         this.log.render(f5);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }
   }
}
