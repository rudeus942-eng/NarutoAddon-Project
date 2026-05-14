
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityBuddhaArm extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 10;

   public EntityBuddhaArm(ElementsInfTsukAddon instance) {
      super(instance, 29);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "buddha_arm"), 10).name("buddha_arm").tracker(128, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, ArmRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class ArmRenderer extends Render<EntityCustom> {
      private static final ResourceLocation ARM_TEXTURE = new ResourceLocation("narutomod:textures/woodfist.png");
      private static final float RENDER_SCALE = 20.0F;
      private static final Map<Integer, ModelWoodFist> modelCache = new HashMap();
      private static final int MIN_ARM_LENGTH = 8;
      private static final int MAX_ARM_LENGTH = 20;

      public ArmRenderer(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.5F;

         for(int len = 8; len <= 20; len += 2) {
            modelCache.put(len, new ModelWoodFist(len));
         }

      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         this.bindEntityTexture(entity);
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)x, (float)y + 2.5F, (float)z);
         float yawInterp = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
         float pitchInterp = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
         GlStateManager.rotate(yawInterp + 90.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(-pitchInterp + 90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.scale(20.0F, 20.0F, 20.0F);
         double velocity = this.getVelocity(entity);
         int rawLength = (int)Math.max(velocity * (double)10.0F, (double)8.0F);
         int armLength = Math.min(20, rawLength / 2 * 2);
         if (armLength < 8) {
            armLength = 8;
         }

         ModelWoodFist model = (ModelWoodFist)modelCache.get(armLength);
         if (model == null) {
            model = new ModelWoodFist(armLength);
            modelCache.put(armLength, model);
         }

         model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.popMatrix();
      }

      private double getVelocity(Entity entity) {
         return Math.sqrt(entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return ARM_TEXTURE;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelWoodFist extends ModelBase {
      private final ModelRenderer arm;

      public ModelWoodFist(int length) {
         this.textureWidth = 16;
         this.textureHeight = 16;
         this.arm = new ModelRenderer(this);
         this.arm.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.arm.rotateAngleX = 0.0F;
         this.arm.rotateAngleY = 0.0F;
         this.arm.rotateAngleZ = 0.0F;
         this.arm.cubeList.add(new ModelBox(this.arm, 0, 0, -2.0F, -2.0F, 0.0F, 4, 4, length, 0.0F, false));
      }

      public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         this.arm.render(scale);
      }
   }

   public static class EntityCustom extends Entity {
      private static final DataParameter<Boolean> SHOULD_GROW;
      private static final DataParameter<Integer> SHOOTER_ID;
      private float impactDamage;
      private int maxLifetime;
      private EntityLivingBase shooter;
      private boolean hasImpacted;

      public EntityCustom(World world) {
         super(world);
         this.impactDamage = 200.0F;
         this.maxLifetime = 100;
         this.hasImpacted = false;
         this.setSize(1.0F, 1.0F);
         this.noClip = true;
      }

      public EntityCustom(World world, EntityLivingBase shooter, double x, double y, double z, float damage) {
         this(world);
         this.shooter = shooter;
         this.impactDamage = damage;
         this.setPosition(x, y, z);
         if (shooter != null) {
            this.dataManager.set(SHOOTER_ID, shooter.getEntityId());
         }

      }

      protected void entityInit() {
         this.dataManager.register(SHOULD_GROW, true);
         this.dataManager.register(SHOOTER_ID, -1);
      }

      public boolean shouldGrow() {
         return (Boolean)this.dataManager.get(SHOULD_GROW);
      }

      public void setGrow(boolean grow) {
         this.dataManager.set(SHOULD_GROW, grow);
         if (!grow) {
            this.maxLifetime = 20;
         }

      }

      public void setDamage(float damage) {
         this.impactDamage = damage;
      }

      public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
         double length = Math.sqrt(x * x + y * y + z * z);
         if (length > (double)0.0F) {
            x /= length;
            y /= length;
            z /= length;
         }

         if (inaccuracy > 0.0F) {
            x += this.rand.nextGaussian() * 0.0075 * (double)inaccuracy;
            y += this.rand.nextGaussian() * 0.0075 * (double)inaccuracy;
            z += this.rand.nextGaussian() * 0.0075 * (double)inaccuracy;
         }

         this.motionX = x * (double)velocity;
         this.motionY = y * (double)velocity;
         this.motionZ = z * (double)velocity;
         float horizDist = MathHelper.sqrt(x * x + z * z);
         this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
         this.rotationPitch = (float)(MathHelper.atan2(y, (double)horizDist) * (180D / Math.PI));
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
      }

      public void onUpdate() {
         super.onUpdate();
         int lifetime = this.shouldGrow() ? 60 : 20;
         if (this.ticksExisted > lifetime) {
            if (!this.world.isRemote && !this.hasImpacted) {
               this.onImpact();
            }

         } else {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            float gravity = this.shouldGrow() ? 0.05F : 0.08F;
            this.motionY -= (double)gravity;
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            this.setPosition(this.posX, this.posY, this.posZ);
            float horizDist = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
            this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)horizDist) * (180D / Math.PI));
            if (!this.world.isRemote && !this.hasImpacted) {
               BlockPos blockPos = new BlockPos(this.posX, this.posY, this.posZ);
               if (!this.world.isAirBlock(blockPos)) {
                  this.onImpact();
                  return;
               }
            }

            if (!this.world.isRemote && !this.hasImpacted) {
               this.checkEntityCollision();
            }

            if (this.posY < (double)-64.0F) {
               this.setDead();
            }

         }
      }

      private void checkEntityCollision() {
         if (this.ticksExisted >= 5) {
            double aoeRadius = (double)4.0F;
            AxisAlignedBB hitBox = new AxisAlignedBB(this.posX - aoeRadius, this.posY - aoeRadius, this.posZ - aoeRadius, this.posX + aoeRadius, this.posY + aoeRadius, this.posZ + aoeRadius);
            if (this.shooter == null) {
               int shooterId = (Integer)this.dataManager.get(SHOOTER_ID);
               if (shooterId != -1) {
                  Entity e = this.world.getEntityByID(shooterId);
                  if (e instanceof EntityLivingBase) {
                     this.shooter = (EntityLivingBase)e;
                  }
               }
            }

            List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, hitBox);
            boolean hitSomething = false;

            for(Entity target : entities) {
               if ((this.shooter == null || target != this.shooter) && (this.shooter == null || this.shooter.getRidingEntity() == null || target != this.shooter.getRidingEntity()) && !(target instanceof EntityBossBuddha1000.EntityCustom) && !target.getClass().getName().contains("Buddha") && !(target instanceof EntityCustom) && !(target instanceof EntityHashirama.EntityCustom) && !target.getClass().getName().contains("Hashirama") && !(target instanceof EntityBossWoodGolem.EntityCustom) && !target.getClass().getName().contains("WoodGolem") && !target.getClass().getName().toLowerCase().contains("clone") && target instanceof EntityLivingBase) {
                  EntityLivingBase livingTarget = (EntityLivingBase)target;
                  livingTarget.hurtResistantTime = 0;
                  boolean damaged = livingTarget.attackEntityFrom(DamageSource.MAGIC, this.impactDamage);
                  if (damaged) {
                     hitSomething = true;
                  }
               }
            }

            if (hitSomething) {
               this.onImpact();
            }

         }
      }

      private void onImpact() {
         if (!this.hasImpacted) {
            this.hasImpacted = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
               EnumParticleTypes var10001 = EnumParticleTypes.BLOCK_CRACK;
               double var10002 = this.posX;
               double var10003 = this.posY;
               double var10004 = this.posZ;
               int[] var10010 = new int[1];
               Block var10013 = Blocks.LOG;
               var10010[0] = Block.getStateId(Blocks.LOG.getDefaultState());
               ws.spawnParticle(var10001, var10002, var10003, var10004, 5, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, var10010);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.5F, 1.2F + this.rand.nextFloat() * 0.4F);
            this.setDead();
         }
      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         if (compound.hasKey("damage")) {
            this.impactDamage = compound.getFloat("damage");
         }

         if (compound.hasKey("grow")) {
            this.setGrow(compound.getBoolean("grow"));
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setFloat("damage", this.impactDamage);
         compound.setBoolean("grow", this.shouldGrow());
      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      public boolean canBeAttackedWithItem() {
         return false;
      }

      static {
         SHOULD_GROW = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
         SHOOTER_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
      }
   }
}
