
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Particles;
import net.narutomod.Particles.Types;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemJutsu.JutsuEnum.Type;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityPhoenixSageFire extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 271;

   public EntityPhoenixSageFire(ElementsInfTsukAddon instance) {
      super(instance, 900);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "phoenix_sage_fire"), 271).name("inftsuk_phoenix_sage_fire").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, PhoenixFireRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable implements ItemJutsu.IJutsu {
      private static final DataParameter<Float> MODEL_SCALE;
      private int lifetime = 0;
      private int spawnDelay = 0;
      private float damage = 5.0F;
      private boolean hasTracking = false;
      private Entity shooterEntity;
      private boolean hasPendingMotion = false;
      private double pendingMotionX;
      private double pendingMotionY;
      private double pendingMotionZ;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.4F, 0.4F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(0.4F, 0.4F);
         this.shooterEntity = thrower;
      }

      public Type getJutsuType() {
         return Type.KATON;
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(MODEL_SCALE, 0.55F);
      }

      public float getEntityScale() {
         return (Float)this.dataManager.get(MODEL_SCALE);
      }

      public void setEntityScale(float scale) {
         this.dataManager.set(MODEL_SCALE, scale);
      }

      public void setDamage(float dmg) {
         this.damage = dmg;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.shooterEntity) {
               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooterEntity), this.damage);
               result.entityHit.hurtResistantTime = 0;
               result.entityHit.setFire(3);
            }

            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 20, 0.8, 0.8, 0.8, 0.05, 0.08, 0.05, new int[]{-49152, 35});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 12, 1.2, 0.3, 1.2, 0.08, 0.02, 0.08, new int[]{-60160, 25});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)0.5F, this.posZ, 8, 0.3, 0.1, 0.3, (double)0.0F, 0.15, (double)0.0F, new int[]{-22016, 40});
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 10, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, 0.1, 0.02, new int[]{-870704614, 30});
            SoundEvent explosionSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
            if (explosionSound != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, explosionSound, SoundCategory.PLAYERS, 0.8F, 0.6F + this.rand.nextFloat() * 0.3F);
            }

            if (result.entityHit instanceof EntityLivingBase) {
               EntityLivingBase hit = (EntityLivingBase)result.entityHit;
               double kbX = hit.posX - this.posX;
               double kbZ = hit.posZ - this.posZ;
               double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
               if (kbDist > (double)0.0F) {
                  hit.motionX += kbX / kbDist * 0.4;
                  hit.motionZ += kbZ / kbDist * 0.4;
                  hit.motionY += 0.15;
                  hit.velocityChanged = true;
               }
            }

            this.setDead();
         }
      }

      public void setSpawnDelay(int delay) {
         this.spawnDelay = delay;
      }

      public void setDelayedMotion(double mx, double my, double mz) {
         this.hasPendingMotion = true;
         this.pendingMotionX = mx;
         this.pendingMotionY = my;
         this.pendingMotionZ = mz;
      }

      public void onUpdate() {
         if (this.spawnDelay > 0) {
            --this.spawnDelay;
            this.motionX = (double)0.0F;
            this.motionY = (double)0.0F;
            this.motionZ = (double)0.0F;
            this.setInvisible(true);
            this.noClip = true;
         } else {
            this.setInvisible(false);
            this.noClip = false;
            if (this.hasPendingMotion) {
               this.motionX = this.pendingMotionX;
               this.motionY = this.pendingMotionY;
               this.motionZ = this.pendingMotionZ;
               this.hasPendingMotion = false;
            }

            super.onUpdate();
            ++this.lifetime;
            if (this.lifetime <= 60 && this.world.isBlockLoaded(new BlockPos(this))) {
               if (!this.world.isRemote && this.hasTracking && this.lifetime > 5) {
                  Entity target = this.findNearestTarget();
                  if (target != null) {
                     Vec3d toTarget = target.getPositionEyes(1.0F).subtract(this.posX, this.posY, this.posZ).normalize();
                     this.motionX = this.motionX * 0.7 + toTarget.x * 0.36;
                     this.motionY = this.motionY * 0.7 + toTarget.y * 0.36;
                     this.motionZ = this.motionZ * 0.7 + toTarget.z * 0.36;
                  }
               }

               if (!this.world.isRemote && this.lifetime % 10 == 0) {
                  SoundEvent flameSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
                  if (flameSound != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, flameSound, SoundCategory.PLAYERS, 0.3F, 1.2F + this.rand.nextFloat() * 0.3F);
                  }
               }

               if (!this.world.isRemote && this.lifetime % 2 == 0) {
                  int trailColor = -65536 | 64 + this.rand.nextInt(128) << 8;
                  Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 3, 0.08, 0.08, 0.08, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{trailColor, 22});
               }

            } else {
               this.setDead();
            }
         }
      }

      private Entity findNearestTarget() {
         double searchRadius = (double)15.0F;
         List<EntityLivingBase> nearby = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(searchRadius), (ex) -> ex != this.shooterEntity && ex.isEntityAlive() && ItemJutsu.canTarget(ex));
         Entity closest = null;
         double closestDist = Double.MAX_VALUE;

         for(EntityLivingBase e : nearby) {
            double dist = this.getDistanceSq(e);
            if (dist < closestDist) {
               closestDist = dist;
               closest = e;
            }
         }

         return closest;
      }

      protected float getGravityVelocity() {
         return 0.01F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("lifetime", this.lifetime);
         compound.setInteger("spawnDelay", this.spawnDelay);
         compound.setFloat("psfDamage", this.damage);
         compound.setBoolean("psfTracking", this.hasTracking);
         compound.setBoolean("psfHasPending", this.hasPendingMotion);
         compound.setDouble("psfPendingMX", this.pendingMotionX);
         compound.setDouble("psfPendingMY", this.pendingMotionY);
         compound.setDouble("psfPendingMZ", this.pendingMotionZ);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("lifetime");
         this.spawnDelay = compound.getInteger("spawnDelay");
         if (compound.hasKey("psfDamage")) {
            this.damage = compound.getFloat("psfDamage");
         }

         this.hasTracking = compound.getBoolean("psfTracking");
         this.hasPendingMotion = compound.getBoolean("psfHasPending");
         this.pendingMotionX = compound.getDouble("psfPendingMX");
         this.pendingMotionY = compound.getDouble("psfPendingMY");
         this.pendingMotionZ = compound.getDouble("psfPendingMZ");
      }

      static {
         MODEL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();
      private static final int COOLDOWN_TICKS = 100;

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (power < 0.3F) {
            return false;
         } else if (entity.world.isRemote) {
            return false;
         } else {
            UUID uid = entity.getUniqueID();
            long now = entity.world.getTotalWorldTime();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 100L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               int count = 3 + (int)(power / 10.0F * 5.0F);
               if (count < 3) {
                  count = 3;
               }

               if (count > 8) {
                  count = 8;
               }

               Vec3d look = entity.getLookVec();

               for(int i = 0; i < count; ++i) {
                  EntityCustom fireball = new EntityCustom(entity.world, entity);
                  fireball.setPosition(entity.posX + look.x * (double)1.5F, entity.posY + (double)entity.getEyeHeight() + look.y * (double)1.5F, entity.posZ + look.z * (double)1.5F);
                  double spreadX = look.x + (entity.getRNG().nextDouble() - (double)0.5F) * 0.4;
                  double spreadY = look.y + (entity.getRNG().nextDouble() - (double)0.5F) * 0.2;
                  double spreadZ = look.z + (entity.getRNG().nextDouble() - (double)0.5F) * 0.4;
                  fireball.setDamage(28.0F + power * 12.0F);
                  fireball.hasTracking = power >= 1.0F;
                  int delay = i * (20 / count);
                  fireball.setSpawnDelay(delay);
                  if (delay == 0) {
                     fireball.motionX = spreadX * 1.2;
                     fireball.motionY = spreadY * 1.2;
                     fireball.motionZ = spreadZ * 1.2;
                  } else {
                     fireball.setDelayedMotion(spreadX * 1.2, spreadY * 1.2, spreadZ * 1.2);
                  }

                  entity.world.spawnEntity(fireball);
               }

               SoundEvent flameSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
               if (flameSound != null) {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, flameSound, SoundCategory.PLAYERS, 1.0F, 1.2F);
               } else {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.2F);
               }

               return true;
            }
         }
      }

      public float getBasePower() {
         return 0.3F;
      }

      public float getPowerupDelay() {
         return 25.0F;
      }

      public float getMaxPower() {
         return 10.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote) {
            Particles.spawnParticle(player.world, Types.FLAME, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.6, player.posY + 0.15, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.6, 2, 0.1, 0.1, 0.1, (double)0.0F, 0.02, (double)0.0F, new int[]{-48128, 15});
         }

         super.onUsingTick(stack, player, power);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class PhoenixFireRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/fireball.png");

      public PhoenixFireRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         float scale = entity.getEntityScale();
         GlStateManager.translate(x, y + 0.15 * (double)scale, z);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(scale, scale, scale);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         int thirdPerson = Minecraft.getMinecraft().gameSettings.thirdPersonView;
         GlStateManager.rotate((float)(thirdPerson == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(45.0F * (partialTicks + (float)entity.ticksExisted), 0.0F, 0.0F, 1.0F);
         GlStateManager.enableBlend();
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         buffer.pos((double)-0.375F, (double)-0.375F, (double)0.0F).tex((double)0.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)0.375F, (double)-0.375F, (double)0.0F).tex((double)1.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)0.375F, (double)0.375F, (double)0.0F).tex((double)1.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)-0.375F, (double)0.375F, (double)0.0F).tex((double)0.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         tessellator.draw();
         GlStateManager.enableLighting();
         GlStateManager.disableBlend();
         GlStateManager.disableRescaleNormal();
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }
}
