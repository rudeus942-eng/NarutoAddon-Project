
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
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

import java.util.*;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityWaterNeedles extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 279;

   public EntityWaterNeedles(ElementsInfTsukAddon instance) {
      super(instance, 905);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "water_needles"), 279).name("inftsuk_water_needles").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, WaterNeedleRenderer::new);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final DataParameter<Float> MODEL_SCALE;
      private int lifetime = 0;
      private float damage = 5.0F;
      private EntityLivingBase shooterEntity;
      private int spawnDelay = 25;
      private double centerX;
      private double centerY;
      private double centerZ;
      private double deathDist;
      private double convergenceSpeed;
      private boolean lastNeedle;
      private double origCenterX;
      private double origCenterY;
      private double origCenterZ;
      private double stepX;
      private double stepY;
      private double stepZ;
      private boolean launched = false;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.1F, 0.1F);
         this.noClip = true;
         this.setInvisible(false);
         this.deathDist = (double)1.5F + this.rand.nextDouble() * (double)2.5F;
         this.convergenceSpeed = 0.6 + this.rand.nextDouble() * 0.4;
      }

      public void setCaster(EntityLivingBase caster) {
         this.shooterEntity = caster;
      }

      public Type getJutsuType() {
         return Type.SUITON;
      }

      protected void entityInit() {
         this.dataManager.register(MODEL_SCALE, 0.8F);
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

      public void setSpawnDelay(int ticks) {
         this.spawnDelay = ticks;
      }

      public void setCenterPoint(double x, double y, double z) {
         this.origCenterX = x;
         this.origCenterY = y;
         this.origCenterZ = z;
         this.centerX = x + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F;
         this.centerY = y + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F;
         this.centerZ = z + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F;
      }

      public void setLastNeedle(boolean last) {
         this.lastNeedle = last;
      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      public void onUpdate() {
         super.onUpdate();
         if (this.spawnDelay > 0) {
            --this.spawnDelay;
            if (this.world.isBlockLoaded(new BlockPos(this)) && this.ticksExisted <= 80) {
               double dx = this.centerX - this.posX;
               double dz = this.centerZ - this.posZ;
               this.rotationYaw = (float)(Math.atan2(dx, dz) * (180D / Math.PI));
               double dy = this.centerY - this.posY;
               double horizDist = Math.sqrt(dx * dx + dz * dz);
               this.rotationPitch = -((float)(Math.atan2(dy, horizDist) * (180D / Math.PI)));
               if (!this.world.isRemote) {
                  Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX, this.posY, this.posZ, 12, 0.15, 0.15, 0.15, (double)0.0F, -0.01, (double)0.0F, new int[]{8});
                  Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX, this.posY, this.posZ, 8, (double)0.25F, (double)0.25F, (double)0.25F, (double)0.0F, -0.02, (double)0.0F, new int[]{5});
                  if (this.ticksExisted % 2 == 0) {
                     Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX, this.posY - 0.15, this.posZ, 3, 0.05, 0.02, 0.05, (double)0.0F, -0.08, (double)0.0F, new int[]{4});
                  }
               }

               if (!this.world.isRemote && this.ticksExisted % 10 == 0) {
                  SoundEvent windEcho = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
                  if (windEcho != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, windEcho, SoundCategory.PLAYERS, 0.15F, 1.5F);
                  }
               }

               if (this.spawnDelay == 0) {
                  if (!this.world.isRemote) {
                     for(int i = 0; i < 8; ++i) {
                        double burstAngle = (Math.PI * 2D) * (double)i / (double)8.0F;
                        double burstSpeed = 0.12 + this.rand.nextDouble() * 0.06;
                        double sign = i % 2 == 0 ? (double)1.0F : -0.6;
                        Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX, this.posY, this.posZ, 1, 0.02, 0.02, 0.02, Math.cos(burstAngle) * burstSpeed * sign, -0.04, Math.sin(burstAngle) * burstSpeed * sign, new int[]{8});
                     }

                     SoundEvent waterStream = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:waterstream"));
                     if (waterStream == null) {
                        waterStream = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:water_bullet"));
                     }

                     if (waterStream != null) {
                        this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, waterStream, SoundCategory.PLAYERS, 0.6F, 1.2F + this.rand.nextFloat() * 0.3F);
                     }
                  }

                  double launchDx = this.centerX - this.posX;
                  double launchDy = this.centerY - this.posY;
                  double launchDz = this.centerZ - this.posZ;
                  double launchDist = Math.sqrt(launchDx * launchDx + launchDy * launchDy + launchDz * launchDz);
                  if (launchDist > 0.1) {
                     this.stepX = launchDx / launchDist * this.convergenceSpeed;
                     this.stepY = launchDy / launchDist * this.convergenceSpeed;
                     this.stepZ = launchDz / launchDist * this.convergenceSpeed;
                  }

                  this.launched = true;
                  double horizSpeed = Math.sqrt(this.stepX * this.stepX + this.stepZ * this.stepZ);
                  this.rotationYaw = (float)(Math.atan2(this.stepX, this.stepZ) * (180D / Math.PI));
                  this.rotationPitch = -((float)(Math.atan2(this.stepY, horizSpeed) * (180D / Math.PI)));
               }

            } else {
               this.setDead();
            }
         } else if (!this.launched) {
            this.setDead();
         } else {
            ++this.lifetime;
            if (this.lifetime <= 40 && this.world.isBlockLoaded(new BlockPos(this))) {
               if (!this.world.isRemote) {
                  List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.origCenterX - (double)10.0F, this.origCenterY - (double)3.0F, this.origCenterZ - (double)10.0F, this.origCenterX + (double)10.0F, this.origCenterY + (double)7.0F, this.origCenterZ + (double)10.0F), (ex) -> ex != this.shooterEntity && ex.isEntityAlive() && ItemJutsu.canTarget(ex));
                  if (!targets.isEmpty()) {
                     Entity closest = null;
                     double closestDist = Double.MAX_VALUE;

                     for(EntityLivingBase e : targets) {
                        double d = this.getDistanceSq(e);
                        if (d < closestDist) {
                           closestDist = d;
                           closest = e;
                        }
                     }

                     if (closest != null) {
                        double tdx = closest.posX - this.posX;
                        double tdy = closest.posY + (double)closest.height * (double)0.5F - this.posY;
                        double tdz = closest.posZ - this.posZ;
                        double tdist = Math.sqrt(tdx * tdx + tdy * tdy + tdz * tdz);
                        if (tdist > 0.1) {
                           this.stepX = tdx / tdist * this.convergenceSpeed;
                           this.stepY = tdy / tdist * this.convergenceSpeed;
                           this.stepZ = tdz / tdist * this.convergenceSpeed;
                        }
                     }
                  }
               }

               this.setPosition(this.posX + this.stepX, this.posY + this.stepY, this.posZ + this.stepZ);
               if (!this.world.isRemote) {
                  Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX, this.posY, this.posZ, 12, 0.12, 0.12, 0.12, (double)0.0F, -0.01, (double)0.0F, new int[]{6});
                  Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX, this.posY, this.posZ, 6, 0.2, 0.2, 0.2, (double)0.0F, -0.02, (double)0.0F, new int[]{4});
                  Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX - this.stepX * 0.8, this.posY - this.stepY * 0.8, this.posZ - this.stepZ * 0.8, 3, 0.06, 0.06, 0.06, (double)0.0F, -0.03, (double)0.0F, new int[]{3});
               }

               if (!this.world.isRemote) {
                  List<EntityLivingBase> nearby = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(0.8), (ex) -> ex != this.shooterEntity && ex.isEntityAlive());
                  Iterator var33 = nearby.iterator();
                  if (var33.hasNext()) {
                     EntityLivingBase hit = (EntityLivingBase)var33.next();
                     hit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooterEntity), this.damage);
                     Particles.spawnParticle(this.world, Types.WATER_SPLASH, hit.posX, hit.posY + (double)hit.height * (double)0.5F, hit.posZ, 5, 0.15, 0.15, 0.15, (double)0.0F, -0.03, (double)0.0F, new int[]{6});
                     this.setDead();
                     return;
                  }
               }

               double dx = this.centerX - this.posX;
               double dy = this.centerY - this.posY;
               double dz = this.centerZ - this.posZ;
               double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
               if (dist < this.deathDist) {
                  if (!this.world.isRemote) {
                     double stepMag = Math.sqrt(this.stepX * this.stepX + this.stepY * this.stepY + this.stepZ * this.stepZ);
                     double dirX = stepMag > (double)0.0F ? this.stepX / stepMag : (double)0.0F;
                     double dirY = stepMag > (double)0.0F ? this.stepY / stepMag : (double)0.0F;
                     double dirZ = stepMag > (double)0.0F ? this.stepZ / stepMag : (double)0.0F;

                     for(int i = 0; i < 5; ++i) {
                        double spread = 0.15;
                        double sprayVelX = dirX * 0.2 + (this.rand.nextDouble() - (double)0.5F) * spread;
                        double sprayVelY = dirY * 0.2 + (this.rand.nextDouble() - (double)0.5F) * spread - 0.04;
                        double sprayVelZ = dirZ * 0.2 + (this.rand.nextDouble() - (double)0.5F) * spread;
                        Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX, this.posY, this.posZ, 1, 0.03, 0.03, 0.03, sprayVelX, sprayVelY, sprayVelZ, new int[]{8});
                     }

                     if (this.lastNeedle) {
                        for(int i = 0; i < 25; ++i) {
                           double phi = this.rand.nextDouble() * Math.PI * (double)2.0F;
                           double theta = this.rand.nextDouble() * Math.PI;
                           double burstSpeed = 0.12 + this.rand.nextDouble() * 0.1;
                           double bvx = Math.sin(theta) * Math.cos(phi) * burstSpeed;
                           double bvy = Math.cos(theta) * burstSpeed * 0.6 - 0.05;
                           double bvz = Math.sin(theta) * Math.sin(phi) * burstSpeed;
                           Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.origCenterX, this.origCenterY, this.origCenterZ, 1, 0.1, 0.1, 0.1, bvx, bvy, bvz, new int[]{15});
                        }
                     }
                  }

                  this.setDead();
               } else {
                  if (!this.world.isRemote) {
                     double trailVelX = -(this.stepX * 0.15);
                     double trailVelY = -(this.stepY * 0.15) - 0.02;
                     double trailVelZ = -(this.stepZ * 0.15);
                     int trailCount = 2 + this.rand.nextInt(2);

                     for(int i = 0; i < trailCount; ++i) {
                        Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.05, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.05, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.05, 1, 0.01, 0.01, 0.01, trailVelX, trailVelY, trailVelZ, new int[]{5});
                     }
                  }

               }
            } else {
               this.setDead();
            }
         }
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("wnDamage", this.damage);
         compound.setInteger("wnSpawnDelay", this.spawnDelay);
         compound.setDouble("wnCenterX", this.centerX);
         compound.setDouble("wnCenterY", this.centerY);
         compound.setDouble("wnCenterZ", this.centerZ);
         compound.setDouble("wnDeathDist", this.deathDist);
         compound.setDouble("wnConvSpeed", this.convergenceSpeed);
         compound.setBoolean("wnLastNeedle", this.lastNeedle);
         compound.setDouble("wnOrigCenterX", this.origCenterX);
         compound.setDouble("wnOrigCenterY", this.origCenterY);
         compound.setDouble("wnOrigCenterZ", this.origCenterZ);
         compound.setDouble("wnStepX", this.stepX);
         compound.setDouble("wnStepY", this.stepY);
         compound.setDouble("wnStepZ", this.stepZ);
         compound.setBoolean("wnLaunched", this.launched);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("wnDamage")) {
            this.damage = compound.getFloat("wnDamage");
         }

         this.spawnDelay = compound.getInteger("wnSpawnDelay");
         this.centerX = compound.getDouble("wnCenterX");
         this.centerY = compound.getDouble("wnCenterY");
         this.centerZ = compound.getDouble("wnCenterZ");
         if (compound.hasKey("wnDeathDist")) {
            this.deathDist = compound.getDouble("wnDeathDist");
         }

         if (compound.hasKey("wnConvSpeed")) {
            this.convergenceSpeed = compound.getDouble("wnConvSpeed");
         }

         this.lastNeedle = compound.getBoolean("wnLastNeedle");
         this.origCenterX = compound.getDouble("wnOrigCenterX");
         this.origCenterY = compound.getDouble("wnOrigCenterY");
         this.origCenterZ = compound.getDouble("wnOrigCenterZ");
         this.stepX = compound.getDouble("wnStepX");
         this.stepY = compound.getDouble("wnStepY");
         this.stepZ = compound.getDouble("wnStepZ");
         this.launched = compound.getBoolean("wnLaunched");
      }

      static {
         MODEL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (power < 0.5F) {
            return false;
         } else if (entity.world.isRemote) {
            return false;
         } else {
            long now = entity.world.getTotalWorldTime();
            UUID uid = entity.getUniqueID();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 60L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               Vec3d eyePos = entity.getPositionEyes(1.0F);
               Vec3d lookVec = entity.getLookVec();
               Vec3d traceEnd = new Vec3d(eyePos.x + lookVec.x * (double)20.0F, eyePos.y + lookVec.y * (double)20.0F, eyePos.z + lookVec.z * (double)20.0F);
               RayTraceResult rayResult = entity.world.rayTraceBlocks(eyePos, traceEnd, false, true, false);
               double targetX;
               double targetY;
               double targetZ;
               if (rayResult != null && rayResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                  targetX = rayResult.hitVec.x;
                  targetY = rayResult.hitVec.y;
                  targetZ = rayResult.hitVec.z;
               } else {
                  targetX = eyePos.x + lookVec.x * (double)20.0F;
                  targetY = eyePos.y + lookVec.y * (double)20.0F;
                  targetZ = eyePos.z + lookVec.z * (double)20.0F;
               }

               List<EntityLivingBase> nearbyTargets = entity.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(targetX - (double)3.0F, targetY - (double)3.0F, targetZ - (double)3.0F, targetX + (double)3.0F, targetY + (double)3.0F, targetZ + (double)3.0F), (ex) -> ex != entity && ex.isEntityAlive() && ItemJutsu.canTarget(ex));
               if (!nearbyTargets.isEmpty()) {
                  Entity closest = (Entity)nearbyTargets.get(0);
                  double closestDist = Double.MAX_VALUE;

                  for(EntityLivingBase e : nearbyTargets) {
                     double d = e.getDistanceSq(targetX, targetY, targetZ);
                     if (d < closestDist) {
                        closestDist = d;
                        closest = e;
                     }
                  }

                  targetX = closest.posX;
                  targetY = closest.posY + (double)closest.height * (double)0.5F;
                  targetZ = closest.posZ;
               }

               int needleCount = (int)(24.0F + power * 1.5F);
               if (needleCount < 24) {
                  needleCount = 24;
               }

               if (needleCount > 36) {
                  needleCount = 36;
               }

               float dmgPerNeedle = 13.5F + power * 7.2F;

               for(int i = 0; i < needleCount; ++i) {
                  double angle = entity.getRNG().nextDouble() * Math.PI * (double)2.0F;
                  double needleRadius = (double)4.0F + entity.getRNG().nextDouble() * (double)6.0F;
                  double nx = targetX + Math.cos(angle) * needleRadius;
                  double nz = targetZ + Math.sin(angle) * needleRadius;
                  double ny = targetY - (double)1.0F + entity.getRNG().nextDouble() * (double)6.0F;
                  EntityCustom needle = new EntityCustom(entity.world);
                  needle.setCaster(entity);
                  needle.setPosition(nx, ny, nz);
                  needle.setDamage(dmgPerNeedle);
                  needle.setSpawnDelay(10 + entity.getRNG().nextInt(20));
                  needle.setCenterPoint(targetX, targetY + (double)1.0F, targetZ);
                  if (i == needleCount - 1) {
                     needle.setLastNeedle(true);
                  }

                  entity.world.spawnEntity(needle);
               }

               SoundEvent waterSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:waterstream"));
               if (waterSound != null) {
                  entity.world.playSound((EntityPlayer)null, targetX, targetY, targetZ, waterSound, SoundCategory.PLAYERS, 1.2F, 1.3F);
               } else {
                  entity.world.playSound((EntityPlayer)null, targetX, targetY, targetZ, SoundEvents.ENTITY_PLAYER_SPLASH, SoundCategory.PLAYERS, 1.0F, 1.4F);
               }

               Particles.spawnParticle(entity.world, Types.SMOKE, targetX, targetY + (double)1.0F, targetZ, 15, (double)2.0F, (double)1.5F, (double)2.0F, 0.02, -0.02, 0.02, new int[]{-2013252728, 50});
               return true;
            }
         }
      }

      public float getBasePower() {
         return 0.5F;
      }

      public float getPowerupDelay() {
         return 25.0F;
      }

      public float getMaxPower() {
         return 8.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote) {
            double angle = (double)player.ticksExisted * 0.4;
            double offX = Math.cos(angle) * (double)0.5F;
            double offZ = Math.sin(angle) * (double)0.5F;
            Particles.spawnParticle(player.world, Types.SMOKE, player.posX + offX, player.posY + 0.15, player.posZ + offZ, 2, 0.05, 0.05, 0.05, (double)0.0F, -0.03, (double)0.0F, new int[]{-2013252728, 12});
         }

         super.onUsingTick(stack, player, power);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class WaterNeedleRenderer extends Render<EntityCustom> {
      public WaterNeedleRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return null;
      }
   }
}
