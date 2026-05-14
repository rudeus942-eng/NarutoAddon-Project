
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityWoodSpear extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 14;

   public EntityWoodSpear(ElementsInfTsukAddon instance) {
      super(instance, 28);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "wood_spear"), 14).name("wood_spear").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, RenderWoodSpear::new);
   }

   @SideOnly(Side.CLIENT)
   public static class RenderWoodSpear extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("inftsukaddon:textures/entity/spike_wood.png");
      private final ModelSpike model = new ModelSpike();

      public RenderWoodSpear(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.1F;
      }

      public boolean shouldRender(EntityCustom entity, ICamera camera, double camX, double camY, double camZ) {
         return true;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float pt) {
         if (entity.prevRotationYaw == 0.0F && entity.prevRotationPitch == 0.0F) {
            entity.prevRotationYaw = entity.rotationYaw;
            entity.prevRotationPitch = entity.rotationPitch;
         }

         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         GlStateManager.translate(x, y, z);
         float yaw = interpolateRotation(entity.prevRotationYaw, entity.rotationYaw, pt);
         float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * pt;
         GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(pitch - 180.0F, 1.0F, 0.0F, 0.0F);
         float scale = entity.getEntityScale();
         GlStateManager.scale(scale * 0.5F, scale, scale * 0.5F);
         int color = entity.getColor();
         float alpha = (float)(color >> 24 & 255) / 255.0F;
         float red = (float)(color >> 16 & 255) / 255.0F;
         float green = (float)(color >> 8 & 255) / 255.0F;
         float blue = (float)(color & 255) / 255.0F;
         GlStateManager.disableCull();
         if (alpha < 1.0F) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableLighting();
         }

         GlStateManager.color(red, green, blue, alpha);
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         if (alpha < 1.0F) {
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
         }

         GlStateManager.enableCull();
         GlStateManager.popMatrix();
      }

      private static float interpolateRotation(float prevYaw, float yaw, float pt) {
         float f;
         for(f = yaw - prevYaw; f < -180.0F; f += 360.0F) {
         }

         while(f >= 180.0F) {
            f -= 360.0F;
         }

         return prevYaw + pt * f;
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelSpike extends ModelBase {
      private final ModelRenderer bone;
      private final ModelRenderer bone2;
      private final ModelRenderer bone3;
      private final ModelRenderer bone4;
      private final ModelRenderer bone5;
      private final ModelRenderer bone6;

      public ModelSpike() {
         this.textureWidth = 32;
         this.textureHeight = 32;
         this.bone = new ModelRenderer(this);
         this.bone.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bone2 = new ModelRenderer(this);
         this.bone2.setRotationPoint(0.0F, 0.0F, 4.0F);
         this.bone.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.1309F, 0.0F, 0.0F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, 0, -4.0F, -32.0F, 0.0F, 8, 32, 0, 0.0F, false));
         this.bone3 = new ModelRenderer(this);
         this.bone3.setRotationPoint(0.0F, 0.0F, -4.0F);
         this.bone.addChild(this.bone3);
         this.setRotationAngle(this.bone3, -0.1309F, 0.0F, 0.0F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 8, 0, -4.0F, -32.0F, 0.0F, 8, 32, 0, 0.0F, false));
         this.bone4 = new ModelRenderer(this);
         this.bone4.setRotationPoint(4.0F, 0.0F, 0.0F);
         this.bone.addChild(this.bone4);
         this.setRotationAngle(this.bone4, -0.1309F, -1.5708F, 0.0F);
         this.bone4.cubeList.add(new ModelBox(this.bone4, 8, 0, -4.0F, -32.0F, 0.0F, 8, 32, 0, 0.0F, false));
         this.bone5 = new ModelRenderer(this);
         this.bone5.setRotationPoint(-4.0F, 0.0F, 0.0F);
         this.bone.addChild(this.bone5);
         this.setRotationAngle(this.bone5, 0.1309F, -1.5708F, 0.0F);
         this.bone5.cubeList.add(new ModelBox(this.bone5, 0, 0, -4.0F, -32.0F, 0.0F, 8, 32, 0, 0.0F, false));
         this.bone6 = new ModelRenderer(this);
         this.bone6.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bone.addChild(this.bone6);
         this.setRotationAngle(this.bone6, -1.5708F, 0.0F, 0.0F);
         this.bone6.cubeList.add(new ModelBox(this.bone6, 16, 24, -4.0F, -4.0F, 0.0F, 8, 8, 0, 0.0F, false));
      }

      public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         this.bone.render(scale);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }
   }

   public static class EntityCustom extends Entity {
      private static final DataParameter<Integer> MODEL_SCALE_INT;
      private static final DataParameter<Integer> COLOR;
      private static final DataParameter<Integer> TARGET_ID;
      public static final int WOOD_COLOR = -7375032;
      private float ogWidth;
      private float ogHeight;
      public EntityLivingBase shootingEntity;
      protected int ticksAlive;
      protected int ticksInAir;
      protected int ticksInGround;
      protected int maxInGroundTime;
      private float motionFactor;
      private Vec3d tipOffset;
      protected int inTargetTime;
      private static final int BURST_DELAY = 40;
      private float damage;
      private float burstDamage;

      public EntityCustom(World world) {
         super(world);
         this.ogWidth = 0.25F;
         this.ogHeight = 1.825F;
         this.maxInGroundTime = 200;
         this.tipOffset = new Vec3d((double)0.0F, 1.825, (double)0.0F);
         this.inTargetTime = 0;
         this.damage = 20.0F;
         this.burstDamage = 50.0F;
         this.setSize(this.ogWidth, this.ogHeight);
         this.noClip = false;
         this.setColor(-7375032);
      }

      public EntityCustom(EntityLivingBase shooter) {
         this(shooter.world);
         this.shootingEntity = shooter;
         this.setPosition(shooter.posX, shooter.posY + (double)shooter.getEyeHeight(), shooter.posZ);
      }

      protected void entityInit() {
         this.dataManager.register(MODEL_SCALE_INT, 100);
         this.dataManager.register(COLOR, -7375032);
         this.dataManager.register(TARGET_ID, -1);
      }

      public float getEntityScale() {
         return (float)(Integer)this.dataManager.get(MODEL_SCALE_INT) / 100.0F;
      }

      public void setEntityScale(float scale) {
         this.setSize(this.ogWidth * scale, this.ogHeight * scale);
         if (!this.world.isRemote) {
            this.dataManager.set(MODEL_SCALE_INT, (int)(scale * 100.0F));
         }

      }

      public void notifyDataManagerChange(DataParameter<?> key) {
         super.notifyDataManagerChange(key);
         if (MODEL_SCALE_INT.equals(key) && this.world.isRemote) {
            float scale = this.getEntityScale();
            this.setSize(this.ogWidth * scale, this.ogHeight * scale);
         }

      }

      public int getColor() {
         return (Integer)this.dataManager.get(COLOR);
      }

      public void setColor(int color) {
         this.dataManager.set(COLOR, color);
      }

      @Nullable
      public EntityLivingBase getTarget() {
         int id = (Integer)this.dataManager.get(TARGET_ID);
         if (id == -1) {
            return null;
         } else {
            Entity entity = this.world.getEntityByID(id);
            return entity instanceof EntityLivingBase ? (EntityLivingBase)entity : null;
         }
      }

      protected void setTarget(@Nullable EntityLivingBase target) {
         if (!this.world.isRemote) {
            if (target != null) {
               this.dataManager.set(TARGET_ID, target.getEntityId());
               this.inTargetTime = this.ticksAlive;
            } else {
               this.dataManager.set(TARGET_ID, -1);
            }
         }

      }

      public void setMaxInGroundTime(int ticks) {
         this.maxInGroundTime = ticks;
      }

      public void shoot(double x, double y, double z, float speed, float inaccuracy) {
         x += this.rand.nextGaussian() * (double)inaccuracy;
         y += this.rand.nextGaussian() * (double)inaccuracy;
         z += this.rand.nextGaussian() * (double)inaccuracy;
         float f1 = MathHelper.sqrt(x * x + z * z);
         this.rotationYaw = (float)(-MathHelper.atan2(x, z) * (180D / Math.PI));
         this.rotationPitch = (float)(-MathHelper.atan2(y, (double)f1) * (180D / Math.PI));
         this.rotationPitch = MathHelper.wrapDegrees(this.rotationPitch + 90.0F);
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
         double d0 = (double)MathHelper.sqrt(x * x + y * y + z * z);
         this.motionX = x / d0 * (double)speed;
         this.motionY = y / d0 * (double)speed;
         this.motionZ = z / d0 * (double)speed;
         this.motionFactor = speed;
      }

      public void setDamage(float damage) {
         this.damage = damage;
      }

      public void setBurstDamage(float burstDamage) {
         this.burstDamage = burstDamage;
      }

      public void setShooter(EntityLivingBase shooter) {
         this.shootingEntity = shooter;
      }

      public boolean isLaunched() {
         return this.motionFactor > 0.0F;
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.ticksAlive;
         if ((this.world.isRemote || this.shootingEntity == null || !this.shootingEntity.isDead) && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote && this.inTargetTime > 0 && this.ticksAlive == this.inTargetTime + 40) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.HOSTILE, 1.0F, 0.9F);
               EntityLivingBase target = this.getTarget();
               if (target != null && target.isEntityAlive()) {
                  int burstCount = 5 + this.rand.nextInt(5);

                  for(int i = 0; i < burstCount; ++i) {
                     EntityCustom burstSpike = new EntityCustom(this.world);
                     burstSpike.setMaxInGroundTime(60);
                     burstSpike.shootingEntity = this.shootingEntity;
                     float burstYaw = (this.rand.nextFloat() - 0.5F) * 360.0F;
                     float burstPitch = 15.0F + this.rand.nextFloat() * 100.0F;
                     burstSpike.setTarget(target);
                     burstSpike.setPositionAndRotation(this.posX, this.posY, this.posZ, burstYaw, burstPitch);
                     burstSpike.setEntityScale(0.4F + this.rand.nextFloat() * 0.4F);
                     this.world.spawnEntity(burstSpike);
                  }

                  DamageSource source = DamageSource.MAGIC;
                  float finalBurstDamage = this.burstDamage * (1.0F + this.rand.nextFloat() * 0.5F);
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom(source, finalBurstDamage);
               }

               this.setDead();
            } else {
               this.checkOnGround();
               if (this.onGround) {
                  this.motionFactor = 0.0F;
                  this.ticksInAir = 0;
                  if (++this.ticksInGround > this.maxInGroundTime) {
                     this.setDead();
                  }
               } else {
                  if (this.motionFactor > 0.0F) {
                     ++this.ticksInAir;
                     RayTraceResult result = this.forwardsRaycast();
                     if (result != null) {
                        this.onImpact(result);
                     }
                  }

                  this.posX += this.motionX;
                  this.posY += this.motionY;
                  this.posZ += this.motionZ;
                  if (this.motionFactor > 0.0F) {
                     this.updateInFlightRotations();
                  }

                  this.motionX *= 0.98;
                  this.motionZ *= 0.98;
                  this.motionY = this.motionY * 0.98 - 0.04;
                  this.isAirBorne = true;
                  this.setPosition(this.posX, this.posY, this.posZ);
               }

            }
         } else {
            this.setDead();
         }
      }

      protected void checkOnGround() {
         EntityLivingBase target = this.getTarget();
         if (target != null) {
            this.onGround = true;
            this.setPosition(target.posX, target.posY + (double)target.height * (double)0.5F, target.posZ);
         } else {
            this.onGround = false;
            Vec3d vec1 = this.getPositionVector();
            Vec3d vec2 = this.getTransformedTip().subtract(vec1);
            double d = (double)1.0F / vec2.length();
            double d2 = (double)0.0F;

            while(true) {
               if (d2 > (double)1.0F) {
                  d2 = (double)1.0F;
               }

               if (this.isOnGround(vec1.add(vec2.scale(d2)))) {
                  this.onGround = true;
                  break;
               }

               if (d2 >= (double)1.0F) {
                  break;
               }

               d2 += d;
            }
         }

      }

      protected boolean isOnGround(Vec3d vec) {
         BlockPos pos = new BlockPos(vec);
         if (!this.world.isAirBlock(pos)) {
            AxisAlignedBB aabb = this.world.getBlockState(pos).getCollisionBoundingBox(this.world, pos);
            if (aabb != Block.NULL_AABB && aabb.offset(pos).contains(vec)) {
               return true;
            }
         }

         return false;
      }

      protected Vec3d getTransformedTip() {
         return this.tipOffset.scale((double)this.getEntityScale()).rotatePitch(-this.rotationPitch * ((float)Math.PI / 180F)).rotateYaw(-this.rotationYaw * ((float)Math.PI / 180F)).add(this.getPositionVector());
      }

      protected void updateInFlightRotations() {
         double d = (double)MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         float yaw = -((float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI)));
         float pitch = -((float)(MathHelper.atan2(this.motionY, d) * (180D / Math.PI))) + 90.0F;
         float deltaYaw = subtractDegreesWrap(yaw, this.rotationYaw);
         float deltaPitch = subtractDegreesWrap(pitch, this.rotationPitch);
         this.prevRotationYaw = yaw - deltaYaw;
         this.prevRotationPitch = pitch - deltaPitch;
         this.rotationPitch = this.prevRotationPitch + (pitch - this.prevRotationPitch) * 0.2F;
         this.rotationYaw = this.prevRotationYaw + (yaw - this.prevRotationYaw) * 0.2F;
      }

      private static float subtractDegreesWrap(float a, float b) {
         float f;
         for(f = a - b; f < -180.0F; f += 360.0F) {
         }

         while(f >= 180.0F) {
            f -= 360.0F;
         }

         return f;
      }

      @Nullable
      protected RayTraceResult forwardsRaycast() {
         Vec3d vec1 = this.getTransformedTip();
         Vec3d vec2 = vec1.add(this.motionX, this.motionY, this.motionZ);
         RayTraceResult result = this.world.rayTraceBlocks(vec1, vec2, false, true, false);
         if (result != null) {
            vec2 = result.hitVec;
         }

         Entity hitEntity = null;
         Vec3d hitVec = null;
         double closestDist = (double)0.0F;
         AxisAlignedBB searchBox = this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow((double)1.0F);

         for(Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, searchBox)) {
            if (entity.canBeCollidedWith() && !entity.noClip && (this.shootingEntity == null || !entity.equals(this.shootingEntity))) {
               RayTraceResult entityResult = entity.getEntityBoundingBox().calculateIntercept(vec1, vec2);
               if (entityResult != null) {
                  double dist = vec1.distanceTo(entityResult.hitVec);
                  if (dist < closestDist || closestDist == (double)0.0F) {
                     hitEntity = entity;
                     hitVec = entityResult.hitVec;
                     closestDist = dist;
                  }
               }
            }
         }

         if (hitEntity != null) {
            result = new RayTraceResult(hitEntity, hitVec);
         }

         return result;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote && result.entityHit instanceof EntityLivingBase && !result.entityHit.equals(this.shootingEntity)) {
            EntityLivingBase target = (EntityLivingBase)result.entityHit;
            DamageSource source = DamageSource.MAGIC;
            target.hurtResistantTime = 0;
            if (target.attackEntityFrom(source, this.damage)) {
               this.setTarget(target);
               this.motionX = (double)0.0F;
               this.motionY = (double)0.0F;
               this.motionZ = (double)0.0F;
               this.motionFactor = 0.0F;
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARROW_HIT, SoundCategory.HOSTILE, 1.0F, 1.0F);
            } else if (!result.entityHit.noClip) {
               this.motionX *= -0.1;
               this.motionY *= -0.1;
               this.motionZ *= -0.1;
               this.rotationYaw += 180.0F;
               this.prevRotationYaw += 180.0F;
            }
         }

      }

      public static void spawnShotgunSpread(World world, @Nullable EntityLivingBase shooter, Vec3d fromPos, Vec3d targetPos, int spearCount, float spreadAngle, float speed, float damage, float scale) {
         if (!world.isRemote) {
            world.playSound((EntityPlayer)null, fromPos.x, fromPos.y, fromPos.z, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.HOSTILE, 1.0F, 0.8F + world.rand.nextFloat() * 0.4F);

            for(int i = 0; i < spearCount; ++i) {
               EntityCustom spear = shooter != null ? new EntityCustom(shooter) : new EntityCustom(world);
               spear.setEntityScale(scale + (world.rand.nextFloat() - 0.5F) * 0.3F);
               spear.setDamage(damage);
               spear.setBurstDamage(damage * 1.5F);
               spear.setPosition(fromPos.x, fromPos.y, fromPos.z);
               Vec3d dir = targetPos.subtract(fromPos);
               double dx = dir.x + (world.rand.nextDouble() - (double)0.5F) * (double)spreadAngle * 0.1;
               double dy = dir.y + (world.rand.nextDouble() - (double)0.5F) * (double)spreadAngle * 0.1;
               double dz = dir.z + (world.rand.nextDouble() - (double)0.5F) * (double)spreadAngle * 0.1;
               spear.shoot(dx, dy, dz, speed, 0.05F);
               world.spawnEntity(spear);
            }

            if (world instanceof WorldServer) {
               WorldServer ws = (WorldServer)world;
               ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, fromPos.x, fromPos.y, fromPos.z, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
            }

         }
      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.setEntityScale(compound.getFloat("scale"));
         this.motionFactor = compound.getFloat("speed");
         this.ticksAlive = compound.getInteger("life");
         this.ticksInAir = compound.getInteger("flighttime");
         this.ticksInGround = compound.getInteger("groundtime");
         this.damage = compound.getFloat("damage");
         if (compound.hasKey("direction", 9)) {
            NBTTagList dirList = compound.getTagList("direction", 6);
            if (dirList.tagCount() == 3) {
               this.motionX = dirList.getDoubleAt(0);
               this.motionY = dirList.getDoubleAt(1);
               this.motionZ = dirList.getDoubleAt(2);
            }
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setFloat("scale", this.getEntityScale());
         compound.setFloat("speed", this.motionFactor);
         compound.setInteger("life", this.ticksAlive);
         compound.setInteger("flighttime", this.ticksInAir);
         compound.setInteger("groundtime", this.ticksInGround);
         compound.setFloat("damage", this.damage);
         compound.setTag("direction", this.newDoubleNBTList(new double[]{this.motionX, this.motionY, this.motionZ}));
      }

      public boolean canBeCollidedWith() {
         return true;
      }

      public boolean canBePushed() {
         return false;
      }

      protected boolean canTriggerWalking() {
         return false;
      }

      @Nullable
      public AxisAlignedBB getCollisionBox(Entity entityIn) {
         if (this.getTarget() != null) {
            return null;
         } else {
            return this.onGround && this.getEntityBoundingBox().getAverageEdgeLength() > 0.6 ? this.getEntityBoundingBox() : null;
         }
      }

      @SideOnly(Side.CLIENT)
      public boolean isInRangeToRenderDist(double distance) {
         return distance <= (double)4096.0F || super.isInRangeToRenderDist(distance);
      }

      static {
         MODEL_SCALE_INT = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         COLOR = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         TARGET_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
      }
   }
}
