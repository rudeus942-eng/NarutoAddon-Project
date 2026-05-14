
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.item.ItemJutsu;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityBijuuBombProjectile extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 403;

   public EntityBijuuBombProjectile(ElementsInfTsukAddon instance) {
      super(instance, 403);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "bijuu_bomb_projectile"), 403).name("bijuu_bomb_projectile").tracker(128, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, RenderBijuuBomb::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private static final DataParameter<Float> MODEL_SCALE;
      private static final DataParameter<Integer> PHASE;
      public static final int PHASE_BUILDUP = 0;
      public static final int PHASE_LAUNCHED = 1;
      private float maxScale = 3.0F;
      private float normalDmg = 60.0F;
      private float trueDmg = 30.0F;
      private int buildupTicks = 60;
      private double explosionRadius = (double)8.0F;
      private int aliveTicks = 0;
      private int launchedAge = -1;
      private float yAnchorMultiplier = 0.3F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.5F, 0.5F);
         this.noClip = true;
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(0.5F, 0.5F);
         this.noClip = true;
      }

      public EntityCustom(World world, EntityLivingBase thrower, float maxScale, float normalDmg, float trueDmg, int buildupTicks, double explosionRadius) {
         super(world, thrower);
         this.setSize(0.5F, 0.5F);
         this.noClip = true;
         this.maxScale = Math.max(0.5F, maxScale);
         this.normalDmg = normalDmg;
         this.trueDmg = trueDmg;
         this.buildupTicks = Math.max(10, buildupTicks);
         this.explosionRadius = Math.max((double)1.0F, explosionRadius);
         this.motionX = (double)0.0F;
         this.motionY = (double)0.0F;
         this.motionZ = (double)0.0F;
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(MODEL_SCALE, 0.05F);
         this.dataManager.register(PHASE, 0);
      }

      public float getEntityScale() {
         return (Float)this.dataManager.get(MODEL_SCALE);
      }

      public void setEntityScale(float scale) {
         this.dataManager.set(MODEL_SCALE, scale);
      }

      public int getPhase() {
         return (Integer)this.dataManager.get(PHASE);
      }

      private void setPhase(int phase) {
         this.dataManager.set(PHASE, phase);
      }

      public boolean isCharging() {
         return this.getPhase() == 0;
      }

      public boolean isLaunched() {
         return this.getPhase() == 1;
      }

      public void launchToward(Vec3d direction, float speed) {
         if (!this.isLaunched()) {
            this.setPhase(1);
            this.launchedAge = 0;
            Vec3d dir = direction.normalize();
            float s = Math.max(0.4F, speed);
            this.motionX = dir.x * (double)s;
            this.motionY = dir.y * (double)s;
            this.motionZ = dir.z * (double)s;
            float yaw = (float)(Math.atan2(dir.x, dir.z) * (180D / Math.PI));
            float pitch = (float)(Math.atan2(-dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)) * (180D / Math.PI));
            this.rotationYaw = this.prevRotationYaw = yaw;
            this.rotationPitch = this.prevRotationPitch = pitch;
            SoundEvent launchSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:nagiharai"));
            if (launchSound != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, launchSound, SoundCategory.HOSTILE, 10.0F, 1.0F);
            }

         }
      }

      public void setAnchor(double x, double y, double z) {
         this.setPosition(x, y, z);
         this.prevPosX = x;
         this.prevPosY = y;
         this.prevPosZ = z;
         this.lastTickPosX = x;
         this.lastTickPosY = y;
         this.lastTickPosZ = z;
      }

      public void onUpdate() {
         ++this.aliveTicks;
         if (!this.isCharging()) {
            ++this.launchedAge;
            super.onUpdate();
            if (!this.world.isRemote && this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               float scale = this.getEntityScale();
               ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY, this.posZ, (int)(6.0F + scale * 4.0F), (double)scale * (double)0.25F, (double)scale * (double)0.25F, (double)scale * (double)0.25F, 0.02, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY, this.posZ, 2, (double)scale * 0.2, (double)scale * 0.2, (double)scale * 0.2, 0.01, new int[0]);
               ws.spawnParticle(EnumParticleTypes.REDSTONE, this.posX, this.posY, this.posZ, 3, (double)scale * 0.3, (double)scale * 0.3, (double)scale * 0.3, (double)0.0F, new int[0]);
            }

            if (this.launchedAge > 200 && !this.world.isRemote) {
               this.detonate();
            }

         } else {
            EntityLivingBase shooter = this.thrower;
            if (shooter != null && shooter.isEntityAlive()) {
               Vec3d look = shooter.getLookVec();
               double mouthX = shooter.posX + look.x * (double)shooter.width * 1.2;
               double mouthY = shooter.posY + (double)(shooter.height * this.yAnchorMultiplier);
               double mouthZ = shooter.posZ + look.z * (double)shooter.width * 1.2;
               this.setAnchor(mouthX, mouthY, mouthZ);
               this.motionX = (double)0.0F;
               this.motionY = (double)0.0F;
               this.motionZ = (double)0.0F;
               float progress = Math.min(1.0F, (float)this.aliveTicks / (float)this.buildupTicks);
               float scale = 0.05F + (this.maxScale - 0.05F) * progress;
               this.setEntityScale(scale);
               this.setSize(0.5F * scale, 0.5F * scale);
               if (this.aliveTicks == 1) {
                  SoundEvent chargeSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:bijudama"));
                  if (chargeSound != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, chargeSound, SoundCategory.HOSTILE, 10.0F, 1.0F);
                  }
               }

               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  int streakCount = (int)(10.0F + progress * 30.0F);

                  for(int i = 0; i < streakCount; ++i) {
                     double ang = this.rand.nextDouble() * Math.PI * (double)2.0F;
                     double elev = (this.rand.nextDouble() - 0.3) * Math.PI;
                     double ringDist = (double)scale * ((double)2.0F + ((double)1.0F - (double)progress) * (double)4.0F);
                     double px = mouthX + Math.cos(ang) * Math.cos(elev) * ringDist;
                     double py = mouthY + Math.sin(elev) * ringDist;
                     double pz = mouthZ + Math.sin(ang) * Math.cos(elev) * ringDist;
                     double vx = (mouthX - px) * 0.35;
                     double vy = (mouthY - py) * 0.35;
                     double vz = (mouthZ - pz) * 0.35;
                     int pick = i % 3;
                     if (pick == 0) {
                        ws.spawnParticle(EnumParticleTypes.REDSTONE, px, py, pz, 0, vx, vy, vz, (double)0.0F, new int[0]);
                     } else if (pick == 1) {
                        ws.spawnParticle(EnumParticleTypes.PORTAL, px, py, pz, 0, vx, vy, vz, (double)1.0F, new int[0]);
                     } else {
                        ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px, py, pz, 0, vx, vy, vz, 0.3, new int[0]);
                     }
                  }

                  ws.spawnParticle(EnumParticleTypes.PORTAL, mouthX, mouthY, mouthZ, (int)(6.0F + progress * 20.0F), (double)scale * 0.15, (double)scale * 0.15, (double)scale * 0.15, 0.02, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, mouthX, mouthY, mouthZ, (int)(3.0F + progress * 8.0F), (double)scale * 0.1, (double)scale * 0.1, (double)scale * 0.1, 0.01, new int[0]);
               }

               if (this.aliveTicks > this.buildupTicks + 200) {
                  EntityLivingBase tgt = null;
                  if (shooter instanceof EntityLiving) {
                     tgt = ((EntityLiving)shooter).getAttackTarget();
                  }

                  if (tgt != null) {
                     Vec3d to = new Vec3d(tgt.posX - this.posX, tgt.posY + (double)tgt.height * (double)0.5F - this.posY, tgt.posZ - this.posZ);
                     this.launchToward(to, 1.2F);
                  } else if (!this.world.isRemote) {
                     this.setDead();
                  }
               }

            } else {
               if (!this.world.isRemote) {
                  this.setDead();
               }

            }
         }
      }

      protected void onImpact(RayTraceResult result) {
         if (this.isLaunched()) {
            if (!this.world.isRemote) {
               if (result.entityHit == null || !result.entityHit.equals(this.thrower)) {
                  this.detonate();
               }
            }
         }
      }

      private void detonate() {
         if (!this.world.isRemote && !this.isDead) {
            float scale = this.getEntityScale();
            float radius = (float)(this.explosionRadius * Math.max((double)0.4F, Math.sqrt((double)Math.max(0.1F, scale))));
            AxisAlignedBB box = new AxisAlignedBB(this.posX - (double)radius, this.posY - (double)radius, this.posZ - (double)radius, this.posX + (double)radius, this.posY + (double)radius, this.posZ + (double)radius);

            for(EntityLivingBase ent : this.world.getEntitiesWithinAABB(EntityLivingBase.class, box)) {
               if (ent != null && ent.isEntityAlive() && !ent.equals(this.thrower)) {
                  double d = ent.getDistance(this.posX, this.posY, this.posZ);
                  if (!(d > (double)radius)) {
                     float falloff = (float)Math.max((double)0.25F, (double)1.0F - d / (double)radius);
                     ent.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), this.normalDmg * falloff);
                     if (this.trueDmg > 0.0F) {
                        ent.hurtResistantTime = 0;
                        ent.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), this.trueDmg * falloff);
                     }
                  }
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 6, (double)radius * 0.3, (double)radius * 0.3, (double)radius * 0.3, (double)0.0F, new int[0]);
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 40, (double)radius * (double)0.5F, (double)radius * (double)0.5F, (double)radius * (double)0.5F, 0.1, new int[0]);
               ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY, this.posZ, 200, (double)radius * 0.6, (double)radius * 0.6, (double)radius * 0.6, (double)1.0F, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY, this.posZ, 120, (double)radius * 0.7, (double)radius * 0.4, (double)radius * 0.7, 0.2, new int[0]);
               ws.spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY, this.posZ, 60, (double)radius * (double)0.5F, (double)radius * 0.3, (double)radius * (double)0.5F, (double)0.25F, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 8.0F, 0.5F);
            this.setDead();
         }
      }

      protected float getGravityVelocity() {
         return 0.0F;
      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      public void applyEntityCollision(Entity entityIn) {
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setFloat("maxScale", this.maxScale);
         compound.setFloat("normalDmg", this.normalDmg);
         compound.setFloat("trueDmg", this.trueDmg);
         compound.setInteger("buildupTicks", this.buildupTicks);
         compound.setDouble("explRad", this.explosionRadius);
         compound.setInteger("aliveTicks", this.aliveTicks);
         compound.setInteger("launchedAge", this.launchedAge);
         compound.setFloat("scale", this.getEntityScale());
         compound.setInteger("phase", this.getPhase());
         compound.setFloat("yAnchorMul", this.yAnchorMultiplier);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("maxScale")) {
            this.maxScale = compound.getFloat("maxScale");
         }

         if (compound.hasKey("normalDmg")) {
            this.normalDmg = compound.getFloat("normalDmg");
         }

         if (compound.hasKey("trueDmg")) {
            this.trueDmg = compound.getFloat("trueDmg");
         }

         if (compound.hasKey("buildupTicks")) {
            this.buildupTicks = compound.getInteger("buildupTicks");
         }

         if (compound.hasKey("explRad")) {
            this.explosionRadius = compound.getDouble("explRad");
         }

         if (compound.hasKey("aliveTicks")) {
            this.aliveTicks = compound.getInteger("aliveTicks");
         }

         if (compound.hasKey("launchedAge")) {
            this.launchedAge = compound.getInteger("launchedAge");
         }

         if (compound.hasKey("scale")) {
            this.setEntityScale(compound.getFloat("scale"));
         }

         if (compound.hasKey("phase")) {
            this.setPhase(compound.getInteger("phase"));
         }

         if (compound.hasKey("yAnchorMul")) {
            this.yAnchorMultiplier = compound.getFloat("yAnchorMul");
         }

      }

      public static EntityCustom spawnCharging(World world, EntityLivingBase shooter, float maxScale, float normalDmg, float trueDmg, int buildupTicks, double explosionRadius) {
         return spawnCharging(world, shooter, maxScale, normalDmg, trueDmg, buildupTicks, explosionRadius, 0.3F);
      }

      public static EntityCustom spawnCharging(World world, EntityLivingBase shooter, float maxScale, float normalDmg, float trueDmg, int buildupTicks, double explosionRadius, float yAnchorMultiplier) {
         if (world.isRemote) {
            return null;
         } else {
            EntityCustom bomb = new EntityCustom(world, shooter, maxScale, normalDmg, trueDmg, buildupTicks, explosionRadius);
            bomb.yAnchorMultiplier = yAnchorMultiplier;
            Vec3d look = shooter.getLookVec();
            double mouthX = shooter.posX + look.x * (double)shooter.width * 1.2;
            double mouthY = shooter.posY + (double)(shooter.height * yAnchorMultiplier);
            double mouthZ = shooter.posZ + look.z * (double)shooter.width * 1.2;
            bomb.setAnchor(mouthX, mouthY, mouthZ);
            world.spawnEntity(bomb);
            return bomb;
         }
      }

      static {
         MODEL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         PHASE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class RenderBijuuBomb extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/bijudama1.png");
      private final ModelBijudama model = new ModelBijudama();

      public RenderBijuuBomb(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.0F;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         float age = (float)entity.ticksExisted + partialTicks;
         float scale = entity.getEntityScale();
         if (!(scale <= 0.001F)) {
            GlStateManager.pushMatrix();
            this.bindEntityTexture(entity);
            GlStateManager.translate(x, y + (double)0.15625F * (double)scale, z);
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            float alpha = 1.0F;
            GlStateManager.color(0.18F, 0.05F, 0.35F, alpha);
            this.model.render(entity, 0.0F, 0.0F, age, 0.0F, 0.0F, 0.0625F);
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
            GlStateManager.scale(1.12F, 1.12F, 1.12F);
            GlStateManager.color(0.55F, 0.1F, 0.75F, 0.55F);
            this.model.render(entity, 0.0F, 0.0F, age, 0.0F, 0.0F, 0.0625F);
            GlStateManager.scale(1.18F, 1.18F, 1.18F);
            GlStateManager.color(0.8F, 0.25F, 0.95F, 0.25F);
            this.model.render(entity, 0.0F, 0.0F, age, 0.0F, 0.0F, 0.0625F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.popMatrix();
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
         }
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelBijudama extends ModelBase {
      public final ModelRenderer bb_main;
      private static final float[] RING_Y_ROT = new float[]{0.0F, -0.3927F, -0.7854F, -1.1781F, -1.5708F, -1.9635F, -2.3562F, -2.7489F};
      private static final float[] BAR_Z_ROT = new float[]{0.0F, 1.9635F, 1.5708F, 1.1781F, 0.7854F, 0.3927F, -0.3927F, -0.7854F};

      public ModelBijudama() {
         this.textureWidth = 4;
         this.textureHeight = 4;
         this.bb_main = new ModelRenderer(this);
         this.bb_main.setRotationPoint(0.0F, 0.0F, 0.0F);

         for(float yRot : RING_Y_ROT) {
            ModelRenderer ring = new ModelRenderer(this);
            ring.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.setRotationAngle(ring, 0.0F, yRot, 0.0F);

            for(float zRot : BAR_Z_ROT) {
               ModelRenderer bar = new ModelRenderer(this);
               bar.setRotationPoint(0.0F, 0.0F, 0.0F);
               this.setRotationAngle(bar, 0.0F, 0.0F, zRot);
               bar.cubeList.add(new ModelBox(bar, 0, 0, -0.5027F, -2.5F, -0.5F, 1, 5, 1, 0.0F, false));
               ring.addChild(bar);
            }

            this.bb_main.addChild(ring);
         }

      }

      public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
         this.bb_main.render(f5);
      }

      public void setRotationAngle(ModelRenderer mr, float x, float y, float z) {
         mr.rotateAngleX = x;
         mr.rotateAngleY = y;
         mr.rotateAngleZ = z;
      }
   }
}
