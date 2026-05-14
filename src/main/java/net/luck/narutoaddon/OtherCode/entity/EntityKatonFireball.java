
package net.luck.narutoaddon.OtherCode.entity;

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
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.item.ItemJutsu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKatonFireball extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 206;

   public EntityKatonFireball(ElementsInfTsukAddon instance) {
      super(instance, 34);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "katon_fireball"), 206).name("inftsuk_katon_fireball").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, FireballRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private static final DataParameter<Float> MODEL_SCALE;
      private static final List<PendingFireCleanup> pendingFireCleanups;
      private static boolean cleanupHandlerRegistered;
      private int lifetime = 0;
      private float damage = 14.0F;
      private float trueDamage = 6.0F;
      private int fireDuration = 5;
      private float fullScale = 3.5F;

      private static void registerCleanupHandler() {
         if (!cleanupHandlerRegistered) {
            cleanupHandlerRegistered = true;
            MinecraftForge.EVENT_BUS.register(new Object() {
               @SubscribeEvent
               public void onServerTick(TickEvent.ServerTickEvent event) {
                  if (event.phase == Phase.END) {
                     synchronized(EntityCustom.pendingFireCleanups) {
                        Iterator<PendingFireCleanup> it = EntityCustom.pendingFireCleanups.iterator();

                        while(it.hasNext()) {
                           PendingFireCleanup cleanup = (PendingFireCleanup)it.next();
                           if (cleanup.world != null && cleanup.world.isBlockLoaded(cleanup.pos)) {
                              --cleanup.ticksRemaining;
                              if (cleanup.ticksRemaining <= 0) {
                                 if (cleanup.world.getBlockState(cleanup.pos).getBlock() == Blocks.FIRE) {
                                    cleanup.world.setBlockToAir(cleanup.pos);
                                 }

                                 it.remove();
                              }
                           } else {
                              it.remove();
                           }
                        }

                     }
                  }
               }

               @SubscribeEvent
               public void onWorldUnload(WorldEvent.Unload event) {
                  synchronized(EntityCustom.pendingFireCleanups) {
                     EntityCustom.pendingFireCleanups.removeIf((c) -> c.world == event.getWorld());
                  }
               }
            });
         }
      }

      private static void scheduleFireRemoval(World world, BlockPos pos, int delayTicks) {
         registerCleanupHandler();
         synchronized(pendingFireCleanups) {
            pendingFireCleanups.add(new PendingFireCleanup(world, pos.toImmutable(), delayTicks));
         }
      }

      public EntityCustom(World world) {
         super(world);
         this.setSize(1.2F, 1.2F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(1.2F, 1.2F);
      }

      public EntityCustom(World world, EntityLivingBase thrower, float normalDmg, float trueDmg) {
         super(world, thrower);
         this.setSize(1.2F, 1.2F);
         this.damage = normalDmg;
         this.trueDamage = trueDmg;
         float totalDmg = normalDmg + trueDmg;
         this.fullScale = totalDmg * 0.2F;
         if (this.fullScale < 3.0F) {
            this.fullScale = 3.0F;
         }

         if (this.fullScale > 10.0F) {
            this.fullScale = 10.0F;
         }

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
         float halfWidth = 0.6F * scale;
         this.setSize(halfWidth * 2.0F, halfWidth * 2.0F);
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            float scale = this.getEntityScale();
            float aoeRadius = scale * 0.5F;
            if (result.entityHit != null && result.entityHit != this.thrower && (this.thrower == null || result.entityHit != this.thrower.getRidingEntity())) {
               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), this.damage);
               result.entityHit.hurtResistantTime = 0;
               result.entityHit.attackEntityFrom(DamageSource.MAGIC, this.trueDamage);
               result.entityHit.setFire(this.fireDuration);
            }

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)aoeRadius))) {
               if (e != this.thrower && (this.thrower == null || e != this.thrower.getRidingEntity()) && !(e instanceof EntityItachi.EntityCustom) && !(e instanceof EntityKisame.EntityCustom) && !(e instanceof EntityWaterClone.EntityCustom) && e instanceof EntityLivingBase) {
                  double dist = (double)e.getDistance(this);
                  float falloff = (float)Math.max(0.3, (double)1.0F - dist / ((double)aoeRadius + (double)1.0F));
                  float aoeDmg = this.damage * 0.6F * falloff;
                  e.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), aoeDmg);
                  e.setFire(this.fireDuration - 1);
               }
            }

            float explosionSize = Math.max(scale - 1.0F, 0.5F);
            this.world.createExplosion(this, this.posX, this.posY, this.posZ, explosionSize, false);
            if (scale >= 2.0F) {
               int fireRadius = (int)Math.ceil((double)aoeRadius);
               BlockPos center = new BlockPos(this.posX, this.posY, this.posZ);

               for(int dx = -fireRadius; dx <= fireRadius; ++dx) {
                  for(int dz = -fireRadius; dz <= fireRadius; ++dz) {
                     for(int dy = -1; dy <= 1; ++dy) {
                        BlockPos pos = center.add(dx, dy, dz);
                        if (this.world.isAirBlock(pos) && this.world.getBlockState(pos.down()).isFullBlock() && this.rand.nextFloat() < 0.35F) {
                           this.world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                           scheduleFireRemoval(this.world, pos, 60);
                        }
                     }
                  }
               }
            }

            int particleCount = (int)(40.0F * scale);

            for(int i = 0; i < particleCount; ++i) {
               this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)scale, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)scale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)scale, (this.rand.nextDouble() - (double)0.5F) * 0.4, this.rand.nextDouble() * 0.4, (this.rand.nextDouble() - (double)0.5F) * 0.4, new int[0]);
            }

            for(int i = 0; i < (int)(20.0F * scale); ++i) {
               this.world.spawnParticle(EnumParticleTypes.LAVA, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)scale, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)scale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)scale, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

            for(int i = 0; i < (int)(15.0F * scale); ++i) {
               this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.5F * (double)scale, this.posY + this.rand.nextDouble() * (double)2.0F * (double)scale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.5F * (double)scale, (this.rand.nextDouble() - (double)0.5F) * 0.1, this.rand.nextDouble() * 0.15, (this.rand.nextDouble() - (double)0.5F) * 0.1, new int[0]);
            }

            SoundEvent flamethrow = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "flamethrow"));
            if (flamethrow != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, flamethrow, SoundCategory.HOSTILE, 2.0F, 0.8F + this.rand.nextFloat() * 0.4F);
            }

            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 120) {
            this.setDead();
         } else {
            if (!this.world.isRemote) {
               if (this.lifetime == 1) {
                  SoundEvent flamethrow = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "flamethrow"));
                  if (flamethrow != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, flamethrow, SoundCategory.HOSTILE, 1.5F, 1.2F);
                  }
               }

               if (this.lifetime <= 15) {
                  this.setEntityScale(1.5F + (this.fullScale - 1.5F) * (float)this.lifetime / 15.0F);
               }
            }

            if (this.world.isRemote) {
               float scale = this.getEntityScale();
               int trailCount = (int)(5.0F * scale);
               if (trailCount < 3) {
                  trailCount = 3;
               }

               for(int i = 0; i < trailCount; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.7 * (double)scale, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.7 * (double)scale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.7 * (double)scale, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
               }

               if (this.lifetime % 2 == 0) {
                  for(int i = 0; i < (int)(2.0F * scale); ++i) {
                     this.world.spawnParticle(EnumParticleTypes.LAVA, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.3 * (double)scale, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.3 * (double)scale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.3 * (double)scale, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  }
               }

               if (this.lifetime > 3) {
                  this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX - this.motionX * (double)0.5F, this.posY - this.motionY * (double)0.5F, this.posZ - this.motionZ * (double)0.5F, (double)0.0F, 0.02, (double)0.0F, new int[0]);
               }
            }

         }
      }

      protected float getGravityVelocity() {
         return 0.0F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("fbDamage", this.damage);
         compound.setFloat("fbTrueDamage", this.trueDamage);
         compound.setFloat("fbFullScale", this.fullScale);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("fbDamage")) {
            this.damage = compound.getFloat("fbDamage");
         }

         if (compound.hasKey("fbTrueDamage")) {
            this.trueDamage = compound.getFloat("fbTrueDamage");
         }

         if (compound.hasKey("fbFullScale")) {
            this.fullScale = compound.getFloat("fbFullScale");
         }

      }

      static {
         MODEL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         pendingFireCleanups = new ArrayList();
         cleanupHandlerRegistered = false;
      }

      private static class PendingFireCleanup {
         final World world;
         final BlockPos pos;
         int ticksRemaining;

         PendingFireCleanup(World world, BlockPos pos, int ticks) {
            this.world = world;
            this.pos = pos;
            this.ticksRemaining = ticks;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public static class FireballRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/fireball.png");

      public FireballRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         float scale = entity.getEntityScale();
         GlStateManager.translate(x, y + (double)0.375F * (double)scale, z);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(scale, scale, scale);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         int thirdPerson = Minecraft.getMinecraft().gameSettings.thirdPersonView;
         GlStateManager.rotate((float)(thirdPerson == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(30.0F * (partialTicks + (float)entity.ticksExisted), 0.0F, 0.0F, 1.0F);
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
