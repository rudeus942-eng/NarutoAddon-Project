
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
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

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKatonAshCloud extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 281;

   public EntityKatonAshCloud(ElementsInfTsukAddon instance) {
      super(instance, 921);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "katon_ash_cloud"), 281).name("inftsuk_katon_ash_cloud").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, InvisibleRenderer::new);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final Map<UUID, WeakReference<EntityCustom>> ACTIVE_CLOUDS = new WeakHashMap();
      private int lifetime = 0;
      private int maxLifetime = 200;
      private float damage = 10.0F;
      private float cloudRadius = 5.0F;
      private boolean detonating = false;
      private int detonationTimer = -1;
      private float detonationRadius = 9.0F;
      private EntityLivingBase casterEntity;
      private UUID casterUUID;
      private int secondEmberWaveTick = -1;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.5F, 0.5F);
         this.noClip = true;
         this.setInvisible(true);
      }

      public Type getJutsuType() {
         return Type.KATON;
      }

      protected void entityInit() {
      }

      public void setCaster(EntityLivingBase caster) {
         this.casterEntity = caster;
         this.casterUUID = caster.getUniqueID();
      }

      public void setDamage(float dmg) {
         this.damage = dmg;
      }

      public void registerAsActive() {
         if (this.casterUUID != null) {
            ACTIVE_CLOUDS.put(this.casterUUID, new WeakReference(this));
         }

      }

      public static EntityCustom getActiveCloud(UUID casterUUID) {
         WeakReference<EntityCustom> ref = (WeakReference)ACTIVE_CLOUDS.get(casterUUID);
         if (ref != null) {
            EntityCustom cloud = (EntityCustom)ref.get();
            if (cloud != null && !cloud.isDead) {
               return cloud;
            }

            ACTIVE_CLOUDS.remove(casterUUID);
         }

         return null;
      }

      public void triggerDetonation() {
         if (!this.detonating) {
            this.detonating = true;
            this.detonationTimer = 10;
            SoundEvent hissSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
            if (hissSound != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, hissSound, SoundCategory.PLAYERS, 0.6F, 2.0F);
            }
         }

      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= this.maxLifetime && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               if (this.secondEmberWaveTick >= 0) {
                  ++this.secondEmberWaveTick;
                  if (this.secondEmberWaveTick == 5) {
                     for(int i = 0; i < 30; ++i) {
                        double eAngle = this.rand.nextDouble() * Math.PI * (double)2.0F;
                        double eSpread = this.rand.nextDouble() * (double)4.5F;
                        double ex = this.posX + Math.cos(eAngle) * eSpread;
                        double ez = this.posZ + Math.sin(eAngle) * eSpread;
                        double upVel = (double)0.25F + this.rand.nextDouble() * 0.35;
                        double outX = Math.cos(eAngle) * (0.04 + this.rand.nextDouble() * 0.08);
                        double outZ = Math.sin(eAngle) * (0.04 + this.rand.nextDouble() * 0.08);
                        Particles.spawnParticle(this.world, Types.FLAME, ex, this.posY + (double)2.0F + this.rand.nextDouble() * (double)3.0F, ez, 1, 0.06, 0.06, 0.06, outX, upVel, outZ, new int[]{-26300, 65 + this.rand.nextInt(20)});
                     }

                     for(int i = 0; i < 15; ++i) {
                        Particles.spawnParticle(this.world, Types.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, this.posY + (double)3.0F + this.rand.nextDouble() * (double)2.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, 1, 0.04, 0.04, 0.04, (this.rand.nextDouble() - (double)0.5F) * 0.06, 0.4 + this.rand.nextDouble() * 0.2, (this.rand.nextDouble() - (double)0.5F) * 0.06, new int[]{-17579, 75 + this.rand.nextInt(10)});
                     }

                     this.cleanupActiveCloud();
                     this.setDead();
                  }

                  return;
               }

               if (this.detonating) {
                  this.handleDetonationPhase();
               } else {
                  this.handleAshPhase();
               }
            }

         } else {
            this.cleanupActiveCloud();
            this.setDead();
         }
      }

      private void handleAshPhase() {
         if (this.lifetime % 10 == 0) {
            for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)this.cloudRadius, this.posY - (double)2.0F, this.posZ - (double)this.cloudRadius, this.posX + (double)this.cloudRadius, this.posY + (double)3.0F, this.posZ + (double)this.cloudRadius), (e) -> e != this.casterEntity && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
               double dist = target.getDistance(this.posX, this.posY, this.posZ);
               if (dist <= (double)this.cloudRadius) {
                  target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 40, 0, false, false));
               }
            }
         }

         if (this.lifetime % 2 == 0) {
            double baseAngle = (double)this.lifetime * 0.15;
            int outerCount = 16;

            for(int i = 0; i < outerCount; ++i) {
               double spiralAngle = baseAngle + (double)i / (double)outerCount * Math.PI * (double)2.0F;
               double radius = this.rand.nextDouble() * (double)this.cloudRadius;
               double px = this.posX + Math.cos(spiralAngle) * radius;
               double pz = this.posZ + Math.sin(spiralAngle) * radius;
               double py = this.posY + this.rand.nextDouble() * (double)3.0F - (double)0.5F;
               double swirlSpeed = 0.06 * (radius / (double)this.cloudRadius);
               double swirlX = Math.cos(spiralAngle + (Math.PI / 2D)) * swirlSpeed;
               double swirlZ = Math.sin(spiralAngle + (Math.PI / 2D)) * swirlSpeed;
               double inwardX = (this.posX - px) * 0.005;
               double inwardZ = (this.posZ - pz) * 0.005;
               double jitterX = (this.rand.nextDouble() - (double)0.5F) * 0.08;
               double jitterY = (this.rand.nextDouble() - (double)0.5F) * 0.08;
               double jitterZ = (this.rand.nextDouble() - (double)0.5F) * 0.08;
               boolean fastParticle = this.rand.nextFloat() < 0.3F;
               int particleLife = fastParticle ? 8 + this.rand.nextInt(3) : 30;
               Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, 0.2, 0.2, 0.2, swirlX + inwardX + jitterX, 0.015 + jitterY, swirlZ + inwardZ + jitterZ, new int[]{-865708732, particleLife});
            }

            int innerCount = 8;

            for(int i = 0; i < innerCount; ++i) {
               double innerAngle = baseAngle * (double)1.5F + (double)i / (double)innerCount * Math.PI * (double)2.0F;
               double innerR = this.rand.nextDouble() * (double)this.cloudRadius * 0.3;
               double px = this.posX + Math.cos(innerAngle) * innerR;
               double pz = this.posZ + Math.sin(innerAngle) * innerR;
               double py = this.posY + (double)0.5F + this.rand.nextDouble() * (double)2.0F;
               double fastSwirl = 0.04;
               double jX = (this.rand.nextDouble() - (double)0.5F) * 0.08;
               double jZ = (this.rand.nextDouble() - (double)0.5F) * 0.08;
               boolean fastInner = this.rand.nextFloat() < 0.3F;
               int innerLife = fastInner ? 8 + this.rand.nextInt(3) : 25;
               Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, 0.1, 0.15, 0.1, Math.cos(innerAngle + (Math.PI / 2D)) * fastSwirl + jX, 0.02, Math.sin(innerAngle + (Math.PI / 2D)) * fastSwirl + jZ, new int[]{-1152039885, innerLife});
            }

            if (this.lifetime % 6 == 0) {
               double tendrilAngle = baseAngle + this.rand.nextDouble() * Math.PI * (double)2.0F;

               for(int j = 0; j < 4; ++j) {
                  double tR = (double)this.cloudRadius * (double)0.5F + (double)((float)j * this.cloudRadius) * 0.15;
                  double tpx = this.posX + Math.cos(tendrilAngle) * tR;
                  double tpz = this.posZ + Math.sin(tendrilAngle) * tR;
                  Particles.spawnParticle(this.world, Types.SMOKE, tpx, this.posY + (double)1.0F + (double)j * 0.3, tpz, 1, 0.1, 0.1, 0.1, Math.cos(tendrilAngle) * 0.03, 0.01, Math.sin(tendrilAngle) * 0.03, new int[]{-1719109786, 18});
               }
            }

            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + 0.1, this.posZ, 5, (double)this.cloudRadius * (double)0.5F, 0.15, (double)this.cloudRadius * (double)0.5F, (double)0.0F, -0.003, (double)0.0F, new int[]{-582733022, 40});
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)2.5F, this.posZ, 3, (double)this.cloudRadius * 0.4, 0.3, (double)this.cloudRadius * 0.4, (double)0.0F, 0.025, (double)0.0F, new int[]{-2005440939, 22});
         }

         if (this.lifetime % 3 == 0) {
            int emberCount = 2 + this.rand.nextInt(2);

            for(int i = 0; i < emberCount; ++i) {
               double eAngle = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double eR = this.rand.nextDouble() * (double)this.cloudRadius * 0.8;
               Particles.spawnParticle(this.world, Types.FLAME, this.posX + Math.cos(eAngle) * eR, this.posY + (double)0.5F + this.rand.nextDouble() * (double)2.0F, this.posZ + Math.sin(eAngle) * eR, 1, 0.04, 0.04, 0.04, (this.rand.nextDouble() - (double)0.5F) * 0.04, 0.02 + this.rand.nextDouble() * 0.03, (this.rand.nextDouble() - (double)0.5F) * 0.04, new int[]{-39424, 2 + this.rand.nextInt(2)});
            }
         }

         if (this.lifetime % 40 == 0) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.3F, 0.4F);
         }

      }

      private void handleDetonationPhase() {
         if (this.detonationTimer > 0) {
            --this.detonationTimer;
            float progress = 1.0F - (float)this.detonationTimer / 10.0F;
            int r;
            int g;
            int b;
            if (progress < 0.5F) {
               float t = progress * 2.0F;
               r = (int)(136.0F + 68.0F * t);
               g = (int)(102.0F + 0.0F * t);
               b = (int)(68.0F + -34.0F * t);
            } else {
               float t = (progress - 0.5F) * 2.0F;
               r = (int)(204.0F + 51.0F * t);
               g = (int)(102.0F + -51.0F * t);
               b = (int)(34.0F + -34.0F * t);
            }

            int ashColor = -16777216 | r << 16 | g << 8 | b;
            int countScale = 6 + (int)(14.0F * progress);
            double speedScale = 0.03 + 0.12 * (double)progress;
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, countScale, (double)this.cloudRadius * 0.7, (double)1.5F, (double)this.cloudRadius * 0.7, (double)0.0F, speedScale, (double)0.0F, new int[]{ashColor, 15});

            for(int i = 0; i < (int)(8.0F * progress + 2.0F); ++i) {
               double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double dist = (double)this.cloudRadius * (0.3 + 0.7 * ((double)1.0F - (double)progress));
               double px = this.posX + Math.cos(angle) * dist;
               double pz = this.posZ + Math.sin(angle) * dist;
               double inwardSpeed = 0.04 + 0.08 * (double)progress;
               Particles.spawnParticle(this.world, Types.SMOKE, px, this.posY + (double)0.5F + this.rand.nextDouble() * (double)2.0F, pz, 1, 0.15, 0.2, 0.15, (this.posX - px) * inwardSpeed, 0.02, (this.posZ - pz) * inwardSpeed, new int[]{ashColor, 12});
            }

            int sparkCount = (int)(6.0F + 18.0F * progress * progress);

            for(int i = 0; i < sparkCount; ++i) {
               double sx = this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)this.cloudRadius * 1.6;
               double sy = this.posY + this.rand.nextDouble() * (double)3.0F;
               double sz = this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)this.cloudRadius * 1.6;
               Particles.spawnParticle(this.world, Types.FLAME, sx, sy, sz, 1, 0.08, 0.08, 0.08, (this.rand.nextDouble() - (double)0.5F) * 0.06, this.rand.nextDouble() * 0.04, (this.rand.nextDouble() - (double)0.5F) * 0.06, new int[]{-8892, 4 + this.rand.nextInt(6)});
            }

            int flashCount = (int)(1.0F + 8.0F * progress * progress * progress);

            for(int i = 0; i < flashCount; ++i) {
               double fx = this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)this.cloudRadius * 1.2;
               double fy = this.posY + (double)0.5F + this.rand.nextDouble() * (double)2.0F;
               double fz = this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)this.cloudRadius * 1.2;
               Particles.spawnParticle(this.world, Types.FLAME, fx, fy, fz, 1, 0.15, 0.15, 0.15, (double)0.0F, 0.01, (double)0.0F, new int[]{-52, 2 + this.rand.nextInt(2)});
               if (this.rand.nextFloat() < progress) {
                  Particles.spawnParticle(this.world, Types.FLAME, fx + (this.rand.nextDouble() - (double)0.5F) * 0.3, fy + (this.rand.nextDouble() - (double)0.5F) * 0.2, fz + (this.rand.nextDouble() - (double)0.5F) * 0.3, 1, 0.1, 0.1, 0.1, (double)0.0F, 0.005, (double)0.0F, new int[]{-8824, 3});
               }
            }

            if (this.detonationTimer % 2 == 0) {
               SoundEvent whooshSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
               if (whooshSound != null) {
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, whooshSound, SoundCategory.PLAYERS, 0.3F + 0.5F * progress, 0.8F + 1.2F * progress);
               }
            }

            if (this.detonationTimer % 3 == 0) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.15F + 0.25F * progress, 0.3F + 0.4F * progress);
            }

         } else {
            this.secondEmberWaveTick = 0;

            for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)this.detonationRadius, this.posY - (double)3.0F, this.posZ - (double)this.detonationRadius, this.posX + (double)this.detonationRadius, this.posY + (double)5.0F, this.posZ + (double)this.detonationRadius), (e) -> e != this.casterEntity && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
               double dist = target.getDistance(this.posX, this.posY, this.posZ);
               if (dist <= (double)this.detonationRadius) {
                  float falloff = 1.0F - (float)(dist / (double)this.detonationRadius) * 0.4F;
                  float totalDmg = this.damage * falloff;
                  if (!(target instanceof EntityPlayer)) {
                     totalDmg = Math.min(totalDmg, 100.0F);
                  }

                  target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.casterEntity), totalDmg);
                  target.hurtResistantTime = 0;
                  target.setFire(6);
                  double kbX = target.posX - this.posX;
                  double kbZ = target.posZ - this.posZ;
                  double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
                  if (kbDist > (double)0.0F) {
                     target.motionX += kbX / kbDist * (double)2.0F * (double)falloff;
                     target.motionZ += kbZ / kbDist * (double)2.0F * (double)falloff;
                     target.motionY += (double)0.5F * (double)falloff;
                     target.velocityChanged = true;
                  }
               }
            }

            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 120, (double)0.5F, (double)0.5F, (double)0.5F, 0.6, 0.6, 0.6, new int[]{-18, 2});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 50, 0.8, 0.6, 0.8, 0.7, (double)0.5F, 0.7, new int[]{-8790, 3});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 30, 1.2, 0.8, 1.2, 0.8, 0.4, 0.8, new int[]{-13210, 3});
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 60, (double)4.0F, (double)3.0F, (double)4.0F, 0.6, (double)0.5F, 0.6, new int[]{-1, 2});
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)0.5F, this.posZ, 35, (double)5.0F, (double)1.5F, (double)5.0F, 0.7, 0.24, 0.7, new int[]{-285217076, 3});

            for(int i = 0; i < 64; ++i) {
               double ringAngle = (double)i / (double)64.0F * Math.PI * (double)2.0F;
               double ringR = (double)this.detonationRadius * (double)0.25F;
               double ringX = this.posX + Math.cos(ringAngle) * ringR;
               double ringZ = this.posZ + Math.sin(ringAngle) * ringR;
               double outVelX = Math.cos(ringAngle) * (double)1.0F;
               double outVelZ = Math.sin(ringAngle) * (double)1.0F;
               Particles.spawnParticle(this.world, Types.FLAME, ringX, this.posY + 0.3, ringZ, 1, 0.1, 0.1, 0.1, outVelX, 0.04 + this.rand.nextDouble() * 0.06, outVelZ, new int[]{-48128, 8});
            }

            for(int i = 0; i < 48; ++i) {
               double ringAngle = (double)i / (double)48.0F * Math.PI * (double)2.0F + 0.1;
               double outVelX = Math.cos(ringAngle) * 0.8;
               double outVelZ = Math.sin(ringAngle) * 0.8;
               Particles.spawnParticle(this.world, Types.FLAME, this.posX + Math.cos(ringAngle) * (double)0.5F, this.posY + (double)0.5F, this.posZ + Math.sin(ringAngle) * (double)0.5F, 1, 0.15, 0.15, 0.15, outVelX, 0.08, outVelZ, new int[]{-3386112, 10});
            }

            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 50, 0.7, (double)0.5F, 0.7, (double)0.0F, 1.2, (double)0.0F, new int[]{-48128, 12});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)2.0F, this.posZ, 35, (double)0.5F, 0.4, (double)0.5F, (double)0.0F, 1.4, (double)0.0F, new int[]{-39424, 10});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.5F, this.posZ, 25, 0.3, 0.3, 0.3, (double)0.0F, 1.6, (double)0.0F, new int[]{-21982, 8});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)3.0F, this.posZ, 20, 0.4, 0.3, 0.4, (double)0.0F, (double)1.5F, (double)0.0F, new int[]{-43776, 10});
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)5.0F, this.posZ, 55, (double)5.5F, 0.7, (double)5.5F, 0.3, 0.08, 0.3, new int[]{-866831599, 20});
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)5.5F, this.posZ, 35, (double)6.0F, (double)0.5F, (double)6.0F, 0.36, 0.04, 0.36, new int[]{-1153162752, 22});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)4.5F, this.posZ, 30, (double)4.0F, 0.4, (double)4.0F, 0.2, 0.12, 0.2, new int[]{-3390464, 15});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)5.5F, this.posZ, 15, (double)3.5F, 0.3, (double)3.5F, 0.24, 0.06, 0.24, new int[]{-5623040, 16});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 50, 1.8, 1.8, 1.8, 0.36, 0.44, 0.36, new int[]{-39424, 10});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 30, 1.2, 1.2, 1.2, 0.2, 0.3, 0.2, new int[]{-21965, 8});

            for(int i = 0; i < 35; ++i) {
               double eAngle = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double eSpread = this.rand.nextDouble() * (double)3.0F;
               double ex = this.posX + Math.cos(eAngle) * eSpread;
               double ez = this.posZ + Math.sin(eAngle) * eSpread;
               double upVel = (double)0.5F + this.rand.nextDouble() * 0.4;
               double outX = Math.cos(eAngle) * (0.12 + this.rand.nextDouble() * 0.24);
               double outZ = Math.sin(eAngle) * (0.12 + this.rand.nextDouble() * 0.24);
               Particles.spawnParticle(this.world, Types.FLAME, ex, this.posY + (double)1.5F + this.rand.nextDouble() * (double)2.5F, ez, 1, 0.05, 0.05, 0.05, outX, upVel, outZ, new int[]{-30669, 25 + this.rand.nextInt(10)});
            }

            for(int i = 0; i < 25; ++i) {
               double eAngle = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double eSpread = this.rand.nextDouble() * (double)4.0F;
               Particles.spawnParticle(this.world, Types.FLAME, this.posX + Math.cos(eAngle) * eSpread, this.posY + (double)2.0F + this.rand.nextDouble() * (double)2.0F, this.posZ + Math.sin(eAngle) * eSpread, 1, 0.05, 0.05, 0.05, (this.rand.nextDouble() - (double)0.5F) * 0.2, 0.6 + this.rand.nextDouble() * 0.3, (this.rand.nextDouble() - (double)0.5F) * 0.2, new int[]{-13244, 30 + this.rand.nextInt(10)});
            }

            for(int i = 0; i < 54; ++i) {
               double scorchAngle = (double)i / (double)54.0F * Math.PI * (double)2.0F;
               double scorchR = (double)this.detonationRadius * 0.7 + this.rand.nextDouble() * (double)this.detonationRadius * 0.3;
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX + Math.cos(scorchAngle) * scorchR, this.posY + 0.1, this.posZ + Math.sin(scorchAngle) * scorchR, 1, 0.35, 0.1, 0.35, Math.cos(scorchAngle) * 0.01, 0.003, Math.sin(scorchAngle) * 0.01, new int[]{-1440608000, 65});
            }

            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + 0.15, this.posZ, 30, (double)this.detonationRadius * (double)0.5F, 0.1, (double)this.detonationRadius * (double)0.5F, (double)0.0F, 0.002, (double)0.0F, new int[]{-1156511744, 70});
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)2.5F, this.posZ, 30, (double)3.5F, (double)2.5F, (double)3.5F, 0.06, 0.1, 0.06, new int[]{-870704614, 55});
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)3.5F, this.posZ, 15, (double)2.5F, (double)1.5F, (double)2.5F, 0.04, 0.08, 0.04, new int[]{-1440603614, 65});
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 3.0F, 0.5F);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.8F, 0.8F);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.5F, 0.3F);
            SoundEvent flameSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
            if (flameSound != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, flameSound, SoundCategory.PLAYERS, 2.5F, 0.25F);
            }

         }
      }

      private void cleanupActiveCloud() {
         if (this.casterUUID != null) {
            ACTIVE_CLOUDS.remove(this.casterUUID);
         }

      }

      public void setDead() {
         this.cleanupActiveCloud();
         super.setDead();
      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.lifetime = compound.getInteger("acLifetime");
         this.maxLifetime = compound.getInteger("acMaxLifetime");
         if (compound.hasKey("acDamage")) {
            this.damage = compound.getFloat("acDamage");
         }

         this.cloudRadius = compound.getFloat("acCloudRadius");
         this.detonating = compound.getBoolean("acDetonating");
         this.detonationTimer = compound.getInteger("acDetonationTimer");
         this.detonationRadius = compound.getFloat("acDetonationRadius");
         if (compound.hasKey("acCasterUUID")) {
            try {
               this.casterUUID = UUID.fromString(compound.getString("acCasterUUID"));
               this.registerAsActive();
            } catch (IllegalArgumentException var3) {
            }
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("acLifetime", this.lifetime);
         compound.setInteger("acMaxLifetime", this.maxLifetime);
         compound.setFloat("acDamage", this.damage);
         compound.setFloat("acCloudRadius", this.cloudRadius);
         compound.setBoolean("acDetonating", this.detonating);
         compound.setInteger("acDetonationTimer", this.detonationTimer);
         compound.setFloat("acDetonationRadius", this.detonationRadius);
         if (this.casterUUID != null) {
            compound.setString("acCasterUUID", this.casterUUID.toString());
         }

      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (entity.world.isRemote) {
            return false;
         } else {
            EntityCustom existingCloud = EntityCustom.getActiveCloud(entity.getUniqueID());
            if (existingCloud != null && !existingCloud.isDead) {
               existingCloud.triggerDetonation();
               return true;
            } else {
               Vec3d eyePos = entity.getPositionEyes(1.0F);
               Vec3d lookVec = entity.getLookVec();
               Vec3d endPos = eyePos.add(lookVec.x * (double)15.0F, lookVec.y * (double)15.0F, lookVec.z * (double)15.0F);
               RayTraceResult ray = entity.world.rayTraceBlocks(eyePos, endPos, false, true, false);
               double targetX;
               double targetY;
               double targetZ;
               if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
                  targetX = ray.hitVec.x;
                  targetY = ray.hitVec.y + (double)1.0F;
                  targetZ = ray.hitVec.z;
               } else {
                  targetX = endPos.x;
                  targetY = endPos.y;
                  targetZ = endPos.z;
               }

               EntityCustom cloud = new EntityCustom(entity.world);
               cloud.setPosition(targetX, targetY, targetZ);
               cloud.setCaster(entity);
               cloud.setDamage(80.0F + power * 25.0F);
               cloud.registerAsActive();
               entity.world.spawnEntity(cloud);
               SoundEvent flameSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
               if (flameSound != null) {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, flameSound, SoundCategory.PLAYERS, 0.8F, 0.3F);
               } else {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 0.8F, 0.3F);
               }

               entity.world.playSound((EntityPlayer)null, targetX, targetY, targetZ, SoundEvents.ENTITY_BLAZE_AMBIENT, SoundCategory.PLAYERS, 0.6F, 0.5F);
               return true;
            }
         }
      }

      public float getBasePower() {
         return 2.0F;
      }

      public float getPowerupDelay() {
         return 0.0F;
      }

      public float getMaxPower() {
         return 4.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         super.onUsingTick(stack, player, power);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class InvisibleRenderer extends Render<EntityCustom> {
      public InvisibleRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return null;
      }
   }
}
