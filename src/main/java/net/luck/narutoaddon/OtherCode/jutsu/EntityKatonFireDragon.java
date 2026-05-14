
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
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
import net.minecraft.util.math.MathHelper;
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
public class EntityKatonFireDragon extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 280;
   public static final int SUB_ENTITYID = 288;

   public EntityKatonFireDragon(ElementsInfTsukAddon instance) {
      super(instance, 920);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "katon_fire_dragon"), 280).name("inftsuk_katon_fire_dragon").tracker(64, 1, true).build());
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityFireDragonSplit.class).id(new ResourceLocation("inftsukaddon", "katon_fire_dragon_split"), 288).name("inftsuk_katon_fire_dragon_split").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, FireDragonRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(EntityFireDragonSplit.class, FireDragonSplitRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable implements ItemJutsu.IJutsu {
      private static final DataParameter<Float> MODEL_SCALE;
      private int lifetime = 0;
      private float damage = 5.0F;
      private float fullScale = 2.5F;
      private boolean hasSplit = false;
      private EntityLivingBase shooterEntity;
      private UUID targetUUID;

      public EntityCustom(World world) {
         super(world);
         this.setSize(1.0F, 1.0F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(1.0F, 1.0F);
         this.shooterEntity = thrower;
      }

      public Type getJutsuType() {
         return Type.KATON;
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(MODEL_SCALE, 1.0F);
      }

      public float getEntityScale() {
         return (Float)this.dataManager.get(MODEL_SCALE);
      }

      public void setEntityScale(float scale) {
         this.dataManager.set(MODEL_SCALE, scale);
         float halfWidth = 0.5F * scale;
         this.setSize(halfWidth * 2.0F, halfWidth * 2.0F);
      }

      public void setDamage(float dmg) {
         this.damage = dmg;
         this.fullScale = dmg * 0.08F;
         if (this.fullScale < 1.5F) {
            this.fullScale = 1.5F;
         }

         if (this.fullScale > 4.0F) {
            this.fullScale = 4.0F;
         }

      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.shooterEntity) {
               float cappedDmg = this.damage;
               if (!(result.entityHit instanceof EntityPlayer)) {
                  cappedDmg = Math.min(cappedDmg, 100.0F);
               }

               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooterEntity), cappedDmg);
               result.entityHit.hurtResistantTime = 0;
               result.entityHit.setFire(4);
               this.applyKnockback(result.entityHit, (double)1.0F);
            }

            float aoeRadius = 3.0F;

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)aoeRadius))) {
               if (e != this.shooterEntity && e instanceof EntityLivingBase) {
                  double dist = (double)e.getDistance(this);
                  float falloff = (float)Math.max(0.3, (double)1.0F - dist / ((double)aoeRadius + (double)1.0F));
                  float aoeDmg = this.damage * 0.6F * falloff;
                  if (!(e instanceof EntityPlayer)) {
                     aoeDmg = Math.min(aoeDmg, 100.0F);
                  }

                  e.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooterEntity), aoeDmg);
                  e.hurtResistantTime = 0;
                  e.setFire(3);
               }
            }

            this.spawnImpactParticles();
            this.playImpactSound();
            if (!this.hasSplit) {
               this.splitIntoSubProjectiles(result.entityHit instanceof EntityLivingBase ? (EntityLivingBase)result.entityHit : null);
            }

            this.setDead();
         }
      }

      public void onUpdate() {
         float savedYaw = this.rotationYaw;
         float savedPitch = this.rotationPitch;
         float savedPrevYaw = this.prevRotationYaw;
         float savedPrevPitch = this.prevRotationPitch;
         super.onUpdate();
         this.rotationYaw = savedYaw;
         this.rotationPitch = savedPitch;
         this.prevRotationYaw = savedPrevYaw;
         this.prevRotationPitch = savedPrevPitch;
         ++this.lifetime;
         if (this.lifetime <= 120 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               if (this.lifetime <= 15) {
                  this.setEntityScale(0.5F + (this.fullScale - 0.5F) * (float)this.lifetime / 15.0F);
               }

               if (!this.hasSplit && this.lifetime > 15) {
                  Entity target = this.findNearestTarget();
                  if (target != null) {
                     double dist = (double)this.getDistance(target);
                     if (dist < (double)8.0F) {
                        this.splitIntoSubProjectiles(target instanceof EntityLivingBase ? (EntityLivingBase)target : null);
                        this.spawnSplitBurstParticles();
                        this.playSplitSound();
                        this.setDead();
                        return;
                     }
                  }
               }

               if (this.lifetime > 3 && this.lifetime % 4 == 0) {
                  for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)3.0F))) {
                     if (e != this.shooterEntity && e instanceof EntityLivingBase) {
                        double dist = (double)e.getDistance(this);
                        if (dist <= (double)3.0F) {
                           float proxDmg = this.damage * 0.4F;
                           if (!(e instanceof EntityPlayer)) {
                              proxDmg = Math.min(proxDmg, 100.0F);
                           }

                           e.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooterEntity), proxDmg);
                           e.setFire(2);
                           e.hurtResistantTime = 0;
                        }
                     }
                  }
               }

               if (this.lifetime % 3 == 0) {
                  float scale = this.getEntityScale();
                  Particles.spawnParticle(this.world, Types.FLAME, this.posX - this.motionX * 0.8, this.posY - this.motionY * 0.8, this.posZ - this.motionZ * 0.8, 6, 0.3 * (double)scale, 0.3 * (double)scale, 0.3 * (double)scale, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-48128, 25});
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX - this.motionX * 1.2, this.posY - this.motionY * 1.2, this.posZ - this.motionZ * 1.2, 3, 0.2 * (double)scale, 0.2 * (double)scale, 0.2 * (double)scale, (double)0.0F, -0.04, (double)0.0F, new int[]{-869064192, 30});
               }

               if (this.lifetime % 3 == 0 && this.lifetime > 5) {
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, 2, 0.05, 0.05, 0.05, (double)0.0F, -0.1, (double)0.0F, new int[]{-39424, 20});
               }

               if (this.lifetime % 8 == 0) {
                  SoundEvent flameSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
                  if (flameSound != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, flameSound, SoundCategory.PLAYERS, 0.6F, 0.4F + this.rand.nextFloat() * 0.15F);
                  }
               }

               if (this.lifetime % 8 == 4) {
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 1.0F, 0.7F + this.rand.nextFloat() * 0.3F);
               }

               if (this.lifetime == 1) {
                  SoundEvent flameSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
                  if (flameSound != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, flameSound, SoundCategory.PLAYERS, 1.5F, 0.35F);
                  }
               }
            }

            double horizSpeed = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            if (horizSpeed > 0.001) {
               this.prevRotationYaw = this.rotationYaw;
               this.prevRotationPitch = this.rotationPitch;
               this.rotationYaw = (float)(MathHelper.atan2(this.motionZ, this.motionX) * (180D / Math.PI)) - 90.0F;
               this.rotationPitch = (float)(-(MathHelper.atan2(this.motionY, horizSpeed) * (180D / Math.PI)));
               if (this.lifetime <= 1) {
                  this.prevRotationYaw = this.rotationYaw;
                  this.prevRotationPitch = this.rotationPitch;
               }
            }

         } else {
            this.setDead();
         }
      }

      private void splitIntoSubProjectiles(EntityLivingBase target) {
         if (!this.hasSplit && !this.world.isRemote) {
            this.hasSplit = true;
            Entity actualTarget = target;
            if (target == null) {
               actualTarget = this.findNearestTarget();
            }

            Vec3d targetPos;
            if (actualTarget != null) {
               targetPos = actualTarget.getPositionEyes(1.0F);
               this.targetUUID = actualTarget.getUniqueID();
            } else {
               Vec3d look = this.getLookVec();
               targetPos = new Vec3d(this.posX + look.x * (double)10.0F, this.posY + look.y * (double)10.0F, this.posZ + look.z * (double)10.0F);
            }

            double[][] offsets = new double[][]{{(double)-10.0F, (double)0.0F, (double)0.0F}, {(double)10.0F, (double)0.0F, (double)0.0F}, {(double)0.0F, (double)8.0F, (double)0.0F}};
            int[] spawnDelays = new int[]{0, 3, 6};
            Vec3d forward = targetPos.subtract(this.posX, this.posY, this.posZ).normalize();
            Vec3d up = new Vec3d((double)0.0F, (double)1.0F, (double)0.0F);
            Vec3d right = forward.crossProduct(up).normalize();
            Vec3d realUp = right.crossProduct(forward).normalize();

            for(int i = 0; i < 3; ++i) {
               EntityFireDragonSplit split = new EntityFireDragonSplit(this.world);
               double spawnX = this.posX + right.x * offsets[i][0] + realUp.x * offsets[i][1];
               double spawnY = this.posY + right.y * offsets[i][0] + realUp.y * offsets[i][1];
               double spawnZ = this.posZ + right.z * offsets[i][0] + realUp.z * offsets[i][1];
               split.setPosition(spawnX, spawnY, spawnZ);
               split.setDamage(this.damage * 0.6F);
               split.shooterEntity = this.shooterEntity;
               split.spawnDelay = spawnDelays[i];
               Vec3d toTarget = targetPos.subtract(spawnX, spawnY, spawnZ).normalize();
               split.motionX = toTarget.x * (double)1.0F;
               split.motionY = toTarget.y * (double)1.0F;
               split.motionZ = toTarget.z * (double)1.0F;
               split.storedMotionX = split.motionX;
               split.storedMotionY = split.motionY;
               split.storedMotionZ = split.motionZ;
               if (actualTarget != null) {
                  split.targetUUID = actualTarget.getUniqueID();
               }

               this.world.spawnEntity(split);
            }

         }
      }

      private void spawnImpactParticles() {
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 25, (double)1.0F, (double)1.0F, (double)1.0F, 0.06, 0.1, 0.06, new int[]{-39424, 35});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 16, (double)1.5F, 0.3, (double)1.5F, 0.1, 0.02, 0.1, new int[]{-3394816, 30});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)0.5F, this.posZ, 10, 0.4, 0.2, 0.4, (double)0.0F, 0.18, (double)0.0F, new int[]{-30720, 40});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 12, 0.6, 0.6, 0.6, 0.03, 0.12, 0.03, new int[]{-870704614, 35});
      }

      private void spawnSplitBurstParticles() {
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 30, (double)1.5F, (double)1.5F, (double)1.5F, 0.15, 0.15, 0.15, new int[]{-39424, 20});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 15, 0.8, 0.8, 0.8, 0.1, 0.1, 0.1, new int[]{-3394816, 25});
      }

      private void playImpactSound() {
         SoundEvent explosionSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
         if (explosionSound != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, explosionSound, SoundCategory.PLAYERS, 1.0F, 0.4F + this.rand.nextFloat() * 0.2F);
         } else {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0F, 0.8F);
         }

      }

      private void playSplitSound() {
         SoundEvent flameSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
         if (flameSound != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, flameSound, SoundCategory.PLAYERS, 0.8F, 1.0F + this.rand.nextFloat() * 0.3F);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_FIREWORK_BLAST, SoundCategory.PLAYERS, 0.6F, 0.7F);
      }

      private void applyKnockback(Entity target, double strength) {
         if (target instanceof EntityLivingBase) {
            EntityLivingBase hit = (EntityLivingBase)target;
            double kbX = hit.posX - this.posX;
            double kbZ = hit.posZ - this.posZ;
            double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
            if (kbDist > (double)0.0F) {
               hit.motionX += kbX / kbDist * strength * (double)0.5F;
               hit.motionZ += kbZ / kbDist * strength * (double)0.5F;
               hit.motionY += 0.2;
               hit.velocityChanged = true;
            }
         }

      }

      private Entity findNearestTarget() {
         double searchRadius = (double)20.0F;
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
         return 0.0F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("fdLifetime", this.lifetime);
         compound.setFloat("fdDamage", this.damage);
         compound.setFloat("fdFullScale", this.fullScale);
         compound.setBoolean("fdHasSplit", this.hasSplit);
         if (this.targetUUID != null) {
            compound.setString("fdTargetUUID", this.targetUUID.toString());
         }

      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("fdLifetime");
         if (compound.hasKey("fdDamage")) {
            this.damage = compound.getFloat("fdDamage");
         }

         if (compound.hasKey("fdFullScale")) {
            this.fullScale = compound.getFloat("fdFullScale");
         }

         this.hasSplit = compound.getBoolean("fdHasSplit");
         if (compound.hasKey("fdTargetUUID")) {
            try {
               this.targetUUID = UUID.fromString(compound.getString("fdTargetUUID"));
            } catch (IllegalArgumentException var3) {
            }
         }

      }

      static {
         MODEL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   public static class EntityFireDragonSplit extends EntityThrowable implements ItemJutsu.IJutsu {
      private int lifetime = 0;
      private float damage = 3.0F;
      EntityLivingBase shooterEntity;
      UUID targetUUID;
      int spawnDelay = 0;
      double storedMotionX;
      double storedMotionY;
      double storedMotionZ;

      public EntityFireDragonSplit(World world) {
         super(world);
         this.setSize(0.3F, 0.3F);
      }

      public Type getJutsuType() {
         return Type.KATON;
      }

      protected void entityInit() {
         super.entityInit();
      }

      public void setDamage(float dmg) {
         this.damage = dmg;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.shooterEntity) {
               float cappedDmg = this.damage;
               if (!(result.entityHit instanceof EntityPlayer)) {
                  cappedDmg = Math.min(cappedDmg, 100.0F);
               }

               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooterEntity), cappedDmg);
               result.entityHit.hurtResistantTime = 0;
               result.entityHit.setFire(4);
               this.applyKnockback(result.entityHit, 0.6);
            }

            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 12, (double)0.5F, (double)0.5F, (double)0.5F, 0.04, 0.06, 0.04, new int[]{-39424, 25});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 8, 0.8, 0.2, 0.8, 0.06, 0.01, 0.06, new int[]{-3394816, 20});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + 0.3, this.posZ, 5, 0.2, 0.1, 0.2, (double)0.0F, 0.12, (double)0.0F, new int[]{-30720, 30});
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 6, 0.3, 0.3, 0.3, 0.02, 0.08, 0.02, new int[]{-870178270, 25});
            SoundEvent hitSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
            if (hitSound != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, hitSound, SoundCategory.PLAYERS, 0.5F, 1.0F + this.rand.nextFloat() * 0.3F);
            }

            this.setDead();
         }
      }

      public void onUpdate() {
         if (this.spawnDelay > 0) {
            --this.spawnDelay;
            this.motionX = (double)0.0F;
            this.motionY = (double)0.0F;
            this.motionZ = (double)0.0F;
            if (this.spawnDelay == 0) {
               this.motionX = this.storedMotionX;
               this.motionY = this.storedMotionY;
               this.motionZ = this.storedMotionZ;
            }

         } else {
            super.onUpdate();
            ++this.lifetime;
            if (this.lifetime <= 60 && this.world.isBlockLoaded(new BlockPos(this))) {
               if (!this.world.isRemote && this.targetUUID != null) {
                  Entity target = this.findTargetByUUID();
                  if (target != null) {
                     Vec3d toTarget = target.getPositionEyes(1.0F).subtract(this.posX, this.posY, this.posZ).normalize();
                     this.motionX = this.motionX * 0.85 + toTarget.x * 0.2;
                     this.motionY = this.motionY * 0.85 + toTarget.y * 0.2;
                     this.motionZ = this.motionZ * 0.85 + toTarget.z * 0.2;
                  }
               }

               if (!this.world.isRemote && this.lifetime % 2 == 0) {
                  Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 2, 0.08, 0.08, 0.08, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-39424, 18});
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX - this.motionX * 0.3, this.posY - this.motionY * 0.3, this.posZ - this.motionZ * 0.3, 1, 0.05, 0.05, 0.05, (double)0.0F, 0.01, (double)0.0F, new int[]{-1438366652, 15});
               }

            } else {
               this.setDead();
            }
         }
      }

      private Entity findTargetByUUID() {
         for(Entity e : this.world.loadedEntityList) {
            if (e.getUniqueID().equals(this.targetUUID) && e.isEntityAlive()) {
               return e;
            }
         }

         return null;
      }

      private void applyKnockback(Entity target, double strength) {
         if (target instanceof EntityLivingBase) {
            EntityLivingBase hit = (EntityLivingBase)target;
            double kbX = hit.posX - this.posX;
            double kbZ = hit.posZ - this.posZ;
            double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
            if (kbDist > (double)0.0F) {
               hit.motionX += kbX / kbDist * strength * (double)0.5F;
               hit.motionZ += kbZ / kbDist * strength * (double)0.5F;
               hit.motionY += 0.15;
               hit.velocityChanged = true;
            }
         }

      }

      protected float getGravityVelocity() {
         return 0.008F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("splitLifetime", this.lifetime);
         compound.setFloat("splitDamage", this.damage);
         if (this.targetUUID != null) {
            compound.setString("splitTargetUUID", this.targetUUID.toString());
         }

      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("splitLifetime");
         if (compound.hasKey("splitDamage")) {
            this.damage = compound.getFloat("splitDamage");
         }

         if (compound.hasKey("splitTargetUUID")) {
            try {
               this.targetUUID = UUID.fromString(compound.getString("splitTargetUUID"));
            } catch (IllegalArgumentException var3) {
            }
         }

      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (power < 1.0F) {
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
               EntityCustom dragon = new EntityCustom(entity.world, entity);
               Vec3d look = entity.getLookVec();
               dragon.setPosition(entity.posX + look.x * (double)2.0F, entity.posY + (double)entity.getEyeHeight() + look.y * (double)2.0F, entity.posZ + look.z * (double)2.0F);
               dragon.setDamage(60.0F + power * 25.0F);
               dragon.motionX = look.x * (double)1.5F;
               dragon.motionY = look.y * (double)1.5F;
               dragon.motionZ = look.z * (double)1.5F;
               entity.world.spawnEntity(dragon);
               SoundEvent flameSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
               if (flameSound != null) {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, flameSound, SoundCategory.PLAYERS, 1.2F, 0.4F);
               } else {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.2F, 0.5F);
               }

               return true;
            }
         }
      }

      public float getBasePower() {
         return 1.0F;
      }

      public float getPowerupDelay() {
         return 30.0F;
      }

      public float getMaxPower() {
         return 5.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote) {
            Particles.spawnParticle(player.world, Types.FLAME, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.8, player.posY + 0.15, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.8, 3, 0.15, 0.15, 0.15, (double)0.0F, 0.03, (double)0.0F, new int[]{-39424, 18});
         }

         super.onUsingTick(stack, player, power);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class FireDragonRenderer extends Render<EntityCustom> {
      private final ResourceLocation texture = new ResourceLocation("narutomod:textures/dragon_gray.png");
      private final ResourceLocation textureFlame = new ResourceLocation("narutomod:textures/gas256.png");
      private final ModelFireDragonHead model = new ModelFireDragonHead();

      public FireDragonRenderer(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.1F;
      }

      public boolean shouldRender(EntityCustom entity, ICamera camera, double camX, double camY, double camZ) {
         return true;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         float age = (float)entity.ticksExisted + partialTicks;
         float scale = entity.getEntityScale();
         float yaw = -entity.prevRotationYaw - (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
         float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
         this.model.setRotationAngles(0.0F, 0.0F, age, 0.0F, 0.0F, 0.0625F, entity);
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)x, (float)y + scale * 0.5F, (float)z);
         GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(pitch - 180.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.disableCull();
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         this.bindEntityTexture(entity);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         GlStateManager.color(1.0F, 0.45F, 0.0F, 0.7F);
         this.model.teethUpper.isHidden = true;
         this.model.teethLower.isHidden = true;
         this.model.eyes.isHidden = true;
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         GlStateManager.color(1.0F, 0.3F, 0.0F, 0.9F);
         this.bindTexture(this.textureFlame);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         GlStateManager.translate(age * 0.02F, age * 0.025F, 0.0F);
         GlStateManager.matrixMode(5888);
         this.model.teethUpper.isHidden = false;
         this.model.teethLower.isHidden = false;
         this.model.eyes.isHidden = true;
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.059375F);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         GlStateManager.matrixMode(5888);
         this.bindEntityTexture(entity);
         GlStateManager.color(1.0F, 0.9F, 0.3F, 1.0F);
         this.model.eyes.isHidden = false;
         this.model.eyes.render(0.0625F);
         GlStateManager.disableBlend();
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.popMatrix();
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return this.texture;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class FireDragonSplitRenderer extends Render<EntityFireDragonSplit> {
      private final ResourceLocation texture = new ResourceLocation("narutomod:textures/dragon_gray.png");
      private final ResourceLocation textureFlame = new ResourceLocation("narutomod:textures/gas256.png");
      private final ModelFireDragonHead model = new ModelFireDragonHead();
      private static final float SPLIT_SCALE = 0.6F;

      public FireDragonSplitRenderer(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.05F;
      }

      public boolean shouldRender(EntityFireDragonSplit entity, ICamera camera, double camX, double camY, double camZ) {
         return true;
      }

      public void doRender(EntityFireDragonSplit entity, double x, double y, double z, float entityYaw, float partialTicks) {
         float age = (float)entity.ticksExisted + partialTicks;
         float scale = 0.6F;
         double horizSpeed = Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
         float yaw = 0.0F;
         float pitch = 0.0F;
         if (horizSpeed > 0.001) {
            yaw = -((float)(MathHelper.atan2(entity.motionZ, entity.motionX) * (180D / Math.PI)) - 90.0F);
            pitch = (float)(-(MathHelper.atan2(entity.motionY, horizSpeed) * (180D / Math.PI)));
         } else {
            yaw = -entity.prevRotationYaw - (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
            pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
         }

         this.model.setRotationAngles(0.0F, 0.0F, age, 0.0F, 0.0F, 0.0625F, entity);
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)x, (float)y + scale * 0.5F, (float)z);
         GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(pitch - 180.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.disableCull();
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         this.bindEntityTexture(entity);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         GlStateManager.color(1.0F, 0.5F, 0.1F, 0.7F);
         this.model.teethUpper.isHidden = true;
         this.model.teethLower.isHidden = true;
         this.model.eyes.isHidden = true;
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         GlStateManager.color(1.0F, 0.35F, 0.0F, 0.85F);
         this.bindTexture(this.textureFlame);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         GlStateManager.translate(age * 0.02F, age * 0.025F, 0.0F);
         GlStateManager.matrixMode(5888);
         this.model.teethUpper.isHidden = false;
         this.model.teethLower.isHidden = false;
         this.model.eyes.isHidden = true;
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.059375F);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         GlStateManager.matrixMode(5888);
         this.bindEntityTexture(entity);
         GlStateManager.color(1.0F, 0.9F, 0.3F, 1.0F);
         this.model.eyes.isHidden = false;
         this.model.eyes.render(0.0625F);
         GlStateManager.disableBlend();
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.popMatrix();
      }

      protected ResourceLocation getEntityTexture(EntityFireDragonSplit entity) {
         return this.texture;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelFireDragonHead extends ModelBase {
      private final ModelRenderer head;
      private final ModelRenderer flair;
      private final ModelRenderer bone;
      private final ModelRenderer bone2;
      private final ModelRenderer bone3;
      public final ModelRenderer teethUpper;
      public final ModelRenderer teethLower;
      private final ModelRenderer jaw;
      private final ModelRenderer[] hornRight = new ModelRenderer[7];
      private final ModelRenderer[] hornLeft = new ModelRenderer[7];
      private final ModelRenderer[] whiskerLeft = new ModelRenderer[6];
      private final ModelRenderer[] whiskerRight = new ModelRenderer[6];
      private final ModelRenderer[] spine = new ModelRenderer[100];
      public final ModelRenderer eyes;
      private static final int VISIBLE_SPINE_SEGMENTS = 8;

      public ModelFireDragonHead() {
         this.textureWidth = 128;
         this.textureHeight = 128;
         this.head = new ModelRenderer(this);
         this.head.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.head.cubeList.add(new ModelBox(this.head, 64, 0, -6.0F, 6.0F, -26.0F, 12, 5, 16, 1.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 0, 0, -8.0F, -1.0F, -11.0F, 16, 16, 16, 1.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 32, 32, 2.0F, 4.0F, -28.0F, 4, 4, 6, 0.0F, true));
         this.head.cubeList.add(new ModelBox(this.head, 32, 32, -6.0F, 4.0F, -28.0F, 4, 4, 6, 0.0F, false));
         this.teethUpper = new ModelRenderer(this);
         this.teethUpper.setRotationPoint(0.0F, 24.0F, 0.0F);
         this.head.addChild(this.teethUpper);
         this.teethUpper.cubeList.add(new ModelBox(this.teethUpper, 0, 52, -6.0F, -12.0F, -26.0F, 12, 3, 16, 0.5F, false));
         this.flair = new ModelRenderer(this);
         this.flair.setRotationPoint(0.0F, -2.0F, -12.0F);
         this.head.addChild(this.flair);
         this.bone = new ModelRenderer(this);
         this.bone.setRotationPoint(9.0F, 9.0F, 0.0F);
         this.flair.addChild(this.bone);
         this.setRotationAngle(this.bone, 0.0F, -0.7854F, 0.0F);
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 52, 0.0F, -8.0F, 0.0F, 10, 16, 0, 0.0F, false));
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 52, -2.0F, -12.0F, 2.0F, 10, 16, 0, 0.0F, false));
         this.bone2 = new ModelRenderer(this);
         this.bone2.setRotationPoint(-9.0F, 9.0F, 0.0F);
         this.flair.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.0F, 0.7854F, 0.0F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, 52, -10.0F, -8.0F, 0.0F, 10, 16, 0, 0.0F, true));
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, 52, -8.0F, -12.0F, 2.0F, 10, 16, 0, 0.0F, true));
         this.bone3 = new ModelRenderer(this);
         this.bone3.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.flair.addChild(this.bone3);
         this.setRotationAngle(this.bone3, -0.8727F, 0.0F, 0.0F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 84, 42, -8.0F, -10.0F, 0.0F, 16, 10, 0, 0.0F, false));
         this.jaw = new ModelRenderer(this);
         this.jaw.setRotationPoint(0.0F, 11.0F, -9.0F);
         this.head.addChild(this.jaw);
         this.setRotationAngle(this.jaw, 0.7854F, 0.0F, 0.0F);
         this.jaw.cubeList.add(new ModelBox(this.jaw, 64, 22, -6.0F, 0.0F, -16.75F, 12, 4, 16, 1.0F, false));
         this.teethLower = new ModelRenderer(this);
         this.teethLower.setRotationPoint(0.0F, 13.0F, 9.0F);
         this.jaw.addChild(this.teethLower);
         this.teethLower.cubeList.add(new ModelBox(this.teethLower, 42, 42, -6.0F, -16.0F, -25.75F, 12, 2, 16, 0.5F, false));
         this.hornRight[0] = new ModelRenderer(this);
         this.hornRight[0].setRotationPoint(-6.0F, -2.0F, -13.0F);
         this.head.addChild(this.hornRight[0]);
         this.setRotationAngle(this.hornRight[0], 0.0873F, -0.5236F, 0.0F);
         this.hornRight[0].cubeList.add(new ModelBox(this.hornRight[0], 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, false));

         for(int i = 1; i < 7; ++i) {
            this.hornRight[i] = new ModelRenderer(this);
            float zPos = i <= 2 ? 7.0F : (i == 3 ? 6.5F : (i == 4 ? 6.0F : (i == 5 ? 5.5F : 5.0F)));
            float inflate = 1.0F - (float)i * 0.3F;
            if (i >= 4) {
               inflate = 0.1F - (float)(i - 3) * 0.3F;
            }

            this.hornRight[i].setRotationPoint(0.0F, 0.0F, zPos);
            this.hornRight[i - 1].addChild(this.hornRight[i]);
            this.setRotationAngle(this.hornRight[i], 0.0873F, 0.0873F, 0.0F);
            this.hornRight[i].cubeList.add(new ModelBox(this.hornRight[i], 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, inflate, false));
         }

         this.hornLeft[0] = new ModelRenderer(this);
         this.hornLeft[0].setRotationPoint(6.0F, -2.0F, -13.0F);
         this.head.addChild(this.hornLeft[0]);
         this.setRotationAngle(this.hornLeft[0], 0.0873F, 0.5236F, 0.0F);
         this.hornLeft[0].cubeList.add(new ModelBox(this.hornLeft[0], 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, true));

         for(int i = 1; i < 7; ++i) {
            this.hornLeft[i] = new ModelRenderer(this);
            float zPos = i <= 2 ? 7.0F : (i == 3 ? 6.5F : (i == 4 ? 6.0F : (i == 5 ? 5.5F : 5.0F)));
            float inflate = 1.0F - (float)i * 0.3F;
            if (i >= 4) {
               inflate = 0.1F - (float)(i - 3) * 0.3F;
            }

            this.hornLeft[i].setRotationPoint(0.0F, 0.0F, zPos);
            this.hornLeft[i - 1].addChild(this.hornLeft[i]);
            this.setRotationAngle(this.hornLeft[i], 0.0873F, -0.0873F, 0.0F);
            this.hornLeft[i].cubeList.add(new ModelBox(this.hornLeft[i], 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, inflate, true));
         }

         this.whiskerLeft[0] = new ModelRenderer(this);
         this.whiskerLeft[0].setRotationPoint(6.0F, 6.0F, -24.0F);
         this.head.addChild(this.whiskerLeft[0]);
         this.setRotationAngle(this.whiskerLeft[0], 0.0F, 1.0472F, 0.0F);
         this.whiskerLeft[0].cubeList.add(new ModelBox(this.whiskerLeft[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, true));

         for(int i = 1; i < 6; ++i) {
            this.whiskerLeft[i] = new ModelRenderer(this);
            this.whiskerLeft[i].setRotationPoint(0.0F, 0.0F, 6.0F);
            this.whiskerLeft[i - 1].addChild(this.whiskerLeft[i]);
            this.setRotationAngle(this.whiskerLeft[i], -0.0873F, -0.1745F, 0.0F);
            float inflate = 0.8F - (float)i * 0.2F;
            this.whiskerLeft[i].cubeList.add(new ModelBox(this.whiskerLeft[i], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, inflate, true));
         }

         this.whiskerRight[0] = new ModelRenderer(this);
         this.whiskerRight[0].setRotationPoint(-6.0F, 6.0F, -24.0F);
         this.head.addChild(this.whiskerRight[0]);
         this.setRotationAngle(this.whiskerRight[0], 0.0F, -1.0472F, 0.0F);
         this.whiskerRight[0].cubeList.add(new ModelBox(this.whiskerRight[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, false));

         for(int i = 1; i < 6; ++i) {
            this.whiskerRight[i] = new ModelRenderer(this);
            this.whiskerRight[i].setRotationPoint(0.0F, 0.0F, 6.0F);
            this.whiskerRight[i - 1].addChild(this.whiskerRight[i]);
            this.setRotationAngle(this.whiskerRight[i], -0.0873F, 0.1745F, 0.0F);
            float inflate = 0.8F - (float)i * 0.2F;
            this.whiskerRight[i].cubeList.add(new ModelBox(this.whiskerRight[i], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, inflate, false));
         }

         for(int i = 0; i < this.spine.length; ++i) {
            this.spine[i] = new ModelRenderer(this);
            this.spine[i].cubeList.add(new ModelBox(this.spine[i], 0, 32, -5.0F, -4.5F, 0.0F, 10, 10, 10, 2.0F, false));
            this.spine[i].cubeList.add(new ModelBox(this.spine[i], 48, 0, -1.0F, -10.5F, 2.0F, 2, 4, 6, 1.0F, false));
            if (i == 0) {
               this.spine[i].setRotationPoint(0.0F, 6.5F, 7.0F);
            } else {
               this.spine[i].setRotationPoint(0.0F, 0.0F, 11.0F);
               this.spine[i - 1].addChild(this.spine[i]);
            }
         }

         this.eyes = new ModelRenderer(this);
         this.eyes.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.eyes.cubeList.add(new ModelBox(this.eyes, 18, 20, -6.6F, 2.6F, -12.15F, 3, 2, 0, 0.0F, false));
         this.eyes.cubeList.add(new ModelBox(this.eyes, 18, 20, 3.6F, 2.6F, -12.15F, 3, 2, 0, 0.0F, true));
      }

      public void render(Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
         this.head.render(f5);
         this.spine[0].render(f5);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void setRotationAngles(float limbSwing, float f1, float ageInTicks, float f3, float headPitch, float f5, Entity e) {
         super.setRotationAngles(limbSwing, f1, ageInTicks, f3, headPitch, f5, e);

         for(int i = 1; i < this.hornRight.length; ++i) {
            this.hornRight[i].rotateAngleX = 0.0873F + MathHelper.sin((ageInTicks - (float)i) * 0.2F) * 0.0873F;
            this.hornRight[i].rotateAngleY = MathHelper.cos((ageInTicks - (float)i) * 0.3F) * 0.0873F;
            this.hornLeft[i].rotateAngleX = 0.0873F + MathHelper.sin((ageInTicks - (float)i) * 0.2F) * 0.0873F;
            this.hornLeft[i].rotateAngleY = -MathHelper.cos((ageInTicks - (float)i) * 0.3F) * 0.0873F;
         }

         float f6 = ((float)Math.PI / 180F);
         this.head.rotateAngleX = headPitch * f6;
         this.eyes.rotateAngleX = this.head.rotateAngleX;
         this.jaw.rotateAngleX = 0.5236F;

         for(int j = 2; j < this.whiskerRight.length; ++j) {
            this.whiskerLeft[j].rotateAngleZ = MathHelper.sin(ageInTicks * 0.15F + (float)j * 0.5F) * 0.3F;
            this.whiskerRight[j].rotateAngleZ = -MathHelper.sin(ageInTicks * 0.15F + (float)j * 0.5F) * 0.3F;
         }

         for(int j = 0; j < this.spine.length; ++j) {
            if (j < 8) {
               this.spine[j].isHidden = false;
               float wavePhase = ageInTicks * 0.15F - (float)j * 0.6F;
               this.spine[j].rotateAngleY = MathHelper.sin(wavePhase) * 0.15F;
               this.spine[j].rotateAngleX = MathHelper.cos(wavePhase * 0.7F) * 0.08F;
            } else {
               this.spine[j].isHidden = true;
            }
         }

      }
   }
}
