
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.item.ItemJutsu;

import java.util.Random;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityRaitonBeam extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 212;

   public EntityRaitonBeam(ElementsInfTsukAddon instance) {
      super(instance, 40);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "raiton_beam"), 212).name("inftsuk_raiton_beam").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, BeamRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private static final DataParameter<Float> MODEL_SCALE;
      private int lifetime = 0;
      private int maxLifetime = 80;
      private float damage = 16.0F;
      private float trueDamage = 4.0F;
      private float fullScale = 3.0F;

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
         this.fullScale = totalDmg * 0.15F;
         if (this.fullScale < 2.0F) {
            this.fullScale = 2.0F;
         }

         if (this.fullScale > 6.0F) {
            this.fullScale = 6.0F;
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

      public void setMaxLifetime(int ticks) {
         this.maxLifetime = ticks;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            float scale = this.getEntityScale();
            float aoeRadius = scale * 0.3F;
            if (result.entityHit != null && result.entityHit != this.thrower) {
               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), this.damage);
               result.entityHit.hurtResistantTime = 0;
               result.entityHit.attackEntityFrom(DamageSource.MAGIC, this.trueDamage);
               if (result.entityHit instanceof EntityLivingBase) {
                  ((EntityLivingBase)result.entityHit).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1));
               }
            }

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)aoeRadius))) {
               if (e != this.thrower && !(e instanceof EntityItachi.EntityCustom) && !(e instanceof EntityKisame.EntityCustom) && !(e instanceof EntityWaterClone.EntityCustom) && e instanceof EntityLivingBase) {
                  double dist = (double)e.getDistance(this);
                  float falloff = (float)Math.max(0.3, (double)1.0F - dist / ((double)aoeRadius + (double)1.0F));
                  float aoeDmg = this.damage * 0.6F * falloff;
                  e.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), aoeDmg);
                  ((EntityLivingBase)e).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1));
               }
            }

            int particleCount = (int)(40.0F * scale);

            for(int i = 0; i < particleCount; ++i) {
               this.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)scale, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)scale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)scale, (this.rand.nextDouble() - (double)0.5F) * 0.4, this.rand.nextDouble() * 0.4, (this.rand.nextDouble() - (double)0.5F) * 0.4, new int[0]);
            }

            for(int i = 0; i < (int)(20.0F * scale); ++i) {
               this.world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)scale, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)scale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)scale, (this.rand.nextDouble() - (double)0.5F) * 0.3, this.rand.nextDouble() * 0.3, (this.rand.nextDouble() - (double)0.5F) * 0.3, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 0.5F, 0.8F + this.rand.nextFloat() * 0.4F);
            SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "electricity"));
            if (electricity != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, electricity, SoundCategory.HOSTILE, 1.0F, 0.8F + this.rand.nextFloat() * 0.4F);
            }

            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > this.maxLifetime) {
            this.setDead();
         } else {
            if (!this.world.isRemote) {
               if (this.lifetime == 1) {
                  SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "electricity"));
                  if (electricity != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, electricity, SoundCategory.HOSTILE, 1.0F, 1.5F);
                  }
               }

               if (this.lifetime <= 8) {
                  this.setEntityScale(1.5F + (this.fullScale - 1.5F) * (float)this.lifetime / 8.0F);
               }
            }

            if (this.world.isRemote) {
               float scale = this.getEntityScale();
               int trailCount = (int)(5.0F * scale);
               if (trailCount < 3) {
                  trailCount = 3;
               }

               for(int i = 0; i < trailCount; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.7 * (double)scale, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.7 * (double)scale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.7 * (double)scale, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
               }

               if (this.lifetime % 2 == 0) {
                  for(int i = 0; i < (int)(2.0F * scale); ++i) {
                     this.world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.3 * (double)scale, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.3 * (double)scale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.3 * (double)scale, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  }
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
         compound.setFloat("rbDamage", this.damage);
         compound.setFloat("rbTrueDamage", this.trueDamage);
         compound.setFloat("rbFullScale", this.fullScale);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("rbDamage")) {
            this.damage = compound.getFloat("rbDamage");
         }

         if (compound.hasKey("rbTrueDamage")) {
            this.trueDamage = compound.getFloat("rbTrueDamage");
         }

         if (compound.hasKey("rbFullScale")) {
            this.fullScale = compound.getFloat("rbFullScale");
         }

      }

      static {
         MODEL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class BeamRenderer extends Render<EntityCustom> {
      private static final int RECURSIVE_DEPTH = 4;
      private static final int MAX_SEGMENTS = 16;
      private static final int REGEN_INTERVAL = 2;
      private static final int MAX_BRANCHES = 8;
      private int lastRegenTick = -1;
      private int lastEntityId = -1;
      private double[][][] mainSegments = new double[16][2][3];
      private int mainSegmentCount;
      private double[][][][] branchSegments = new double[8][16][2][3];
      private int[] branchSegmentCounts = new int[8];
      private int branchCount;

      public BeamRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      private int calcSections(double[] from, double[] to, int depth, double[][][] segments, int segIdx, Random rng) {
         if (depth == 4) {
            segments[segIdx][0][0] = from[0];
            segments[segIdx][0][1] = from[1];
            segments[segIdx][0][2] = from[2];
            segments[segIdx][1][0] = to[0];
            segments[segIdx][1][1] = to[1];
            segments[segIdx][1][2] = to[2];
            return segIdx + 1;
         } else {
            double dx = to[0] - from[0];
            double dy = to[1] - from[1];
            double dz = to[2] - from[2];
            double halfLen = Math.sqrt(dx * dx + dy * dy + dz * dz) * (double)0.5F;
            double offset = halfLen * 0.15;
            double mx = (from[0] + to[0]) * (double)0.5F + rng.nextGaussian() * offset;
            double my = (from[1] + to[1]) * (double)0.5F + rng.nextGaussian() * offset;
            double mz = (from[2] + to[2]) * (double)0.5F + rng.nextGaussian() * offset;
            double[] mid = new double[]{mx, my, mz};
            segIdx = this.calcSections(from, mid, depth + 1, segments, segIdx, rng);
            segIdx = this.calcSections(mid, to, depth + 1, segments, segIdx, rng);
            return segIdx;
         }
      }

      private void spawnBranches(double[] from, double[] to, int depth, Random rng) {
         if (depth != 4 && this.branchCount < 8) {
            double dx = to[0] - from[0];
            double dy = to[1] - from[1];
            double dz = to[2] - from[2];
            double halfLen = Math.sqrt(dx * dx + dy * dy + dz * dz) * (double)0.5F;
            double offset = halfLen * 0.15;
            double mx = (from[0] + to[0]) * (double)0.5F + rng.nextGaussian() * offset;
            double my = (from[1] + to[1]) * (double)0.5F + rng.nextGaussian() * offset;
            double mz = (from[2] + to[2]) * (double)0.5F + rng.nextGaussian() * offset;
            double[] mid = new double[]{mx, my, mz};
            if (rng.nextInt(5) == 0 && this.branchCount < 8) {
               double bx = mx + (mx - from[0]) * 0.8;
               double by = my + (my - from[1]) * 0.8;
               double bz = mz + (mz - from[2]) * 0.8;
               double[] branchEnd = new double[]{bx, by, bz};
               int idx = this.branchCount;
               this.branchSegmentCounts[idx] = this.calcSections(mid, branchEnd, 0, this.branchSegments[idx], 0, rng);
               ++this.branchCount;
            }

            this.spawnBranches(from, mid, depth + 1, rng);
            this.spawnBranches(mid, to, depth + 1, rng);
         }
      }

      private void ensureArcData(EntityCustom entity) {
         int tick = entity.ticksExisted;
         int regenTick = tick / 2;
         if (regenTick != this.lastRegenTick || entity.getEntityId() != this.lastEntityId) {
            this.lastRegenTick = regenTick;
            this.lastEntityId = entity.getEntityId();
            Random rng = new Random((long)entity.getEntityId() * 31L + (long)regenTick);
            float scale = entity.getEntityScale();
            double beamLength = (double)3.5F * (double)scale;
            double[] from = new double[]{(double)0.0F, (double)0.0F, (double)0.0F};
            double[] to = new double[]{(double)0.0F, (double)0.0F, beamLength};
            this.mainSegmentCount = this.calcSections(from, to, 0, this.mainSegments, 0, rng);
            this.branchCount = 0;
            this.spawnBranches(from, to, 0, rng);
         }
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         this.ensureArcData(entity);
         double vmx = entity.motionX;
         double vmy = entity.motionY;
         double vmz = entity.motionZ;
         double speed = Math.sqrt(vmx * vmx + vmy * vmy + vmz * vmz);
         if (speed < 0.01) {
            float yaw = (float)Math.toRadians((double)entity.rotationYaw);
            float pitch = (float)Math.toRadians((double)(-entity.rotationPitch));
            vmx = -Math.sin((double)yaw) * Math.cos((double)pitch);
            vmy = Math.sin((double)pitch);
            vmz = Math.cos((double)yaw) * Math.cos((double)pitch);
         }

         float yawAngle = (float)(Math.atan2(vmx, vmz) * (180D / Math.PI));
         double horizLen = Math.sqrt(vmx * vmx + vmz * vmz);
         float pitchAngle = (float)(-Math.atan2(vmy, horizLen) * (180D / Math.PI));
         float scale = entity.getEntityScale();
         double thickness = Math.max(0.03 * (double)scale, 0.006);
         int colorR = 50;
         int colorG = 100;
         int colorB = 255;
         GlStateManager.pushMatrix();
         GlStateManager.translate(x, y, z);
         GlStateManager.rotate(yawAngle, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(pitchAngle, 1.0F, 0.0F, 0.0F);
         GlStateManager.disableTexture2D();
         GlStateManager.enableBlend();
         GlStateManager.alphaFunc(516, 0.0F);
         GlStateManager.disableLighting();
         GlStateManager.depthMask(false);
         GlStateManager.disableCull();
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();

         for(int i = 0; i < this.mainSegmentCount; ++i) {
            this.renderSection(tessellator, buffer, this.mainSegments[i], thickness, colorR, colorG, colorB, false);
         }

         for(int b = 0; b < this.branchCount; ++b) {
            for(int i = 0; i < this.branchSegmentCounts[b]; ++i) {
               this.renderSection(tessellator, buffer, this.branchSegments[b][i], thickness * 0.6, colorR, colorG, colorB, true);
            }
         }

         GlStateManager.enableLighting();
         GlStateManager.depthMask(true);
         GlStateManager.enableCull();
         GlStateManager.disableBlend();
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.enableTexture2D();
         GlStateManager.popMatrix();
      }

      private void renderSection(Tessellator tessellator, BufferBuilder buffer, double[][] segment, double w, int r, int g, int b, boolean isBranch) {
         double fx = segment[0][0];
         double fy = segment[0][1];
         double fz = segment[0][2];
         double tx = segment[1][0];
         double ty = segment[1][1];
         double tz = segment[1][2];
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, isBranch ? 160.0F : 240.0F, isBranch ? 160.0F : 240.0F);

         for(int layer = 1; layer <= 3; ++layer) {
            double lw = w * (double)layer;
            int lr;
            int lg;
            int lb;
            int alpha;
            if (layer == 1) {
               lr = 255;
               lg = 255;
               lb = 255;
               alpha = isBranch ? 64 : 240;
               GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
            } else {
               lr = r;
               lg = g;
               lb = b;
               alpha = layer == 2 ? 32 : 16;
               if (isBranch) {
                  alpha = Math.max(alpha / 2, 8);
               }

               GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            }

            buffer.begin(5, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(fx - lw, fy - lw, fz).color(lr, lg, lb, alpha).endVertex();
            buffer.pos(tx - lw, ty - lw, tz).color(lr, lg, lb, alpha).endVertex();
            buffer.pos(fx - lw, fy + lw, fz).color(lr, lg, lb, alpha).endVertex();
            buffer.pos(tx - lw, ty + lw, tz).color(lr, lg, lb, alpha).endVertex();
            buffer.pos(fx + lw, fy + lw, fz).color(lr, lg, lb, alpha).endVertex();
            buffer.pos(tx + lw, ty + lw, tz).color(lr, lg, lb, alpha).endVertex();
            buffer.pos(fx + lw, fy - lw, fz).color(lr, lg, lb, alpha).endVertex();
            buffer.pos(tx + lw, ty - lw, tz).color(lr, lg, lb, alpha).endVertex();
            buffer.pos(fx - lw, fy - lw, fz).color(lr, lg, lb, alpha).endVertex();
            buffer.pos(tx - lw, ty - lw, tz).color(lr, lg, lb, alpha).endVertex();
            tessellator.draw();
         }

      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return null;
      }
   }
}
