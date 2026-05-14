
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.item.ItemJutsu;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntitySuitonBullet extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 209;

   public EntitySuitonBullet(ElementsInfTsukAddon instance) {
      super(instance, 40);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "suiton_bullet"), 209).name("inftsuk_suiton_bullet").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SuitonRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private int lifetime = 0;
      private float damage = 4.0F;
      private float trueDamage = 1.0F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.6F, 0.6F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(0.6F, 0.6F);
      }

      public EntityCustom(World world, EntityLivingBase thrower, float normalDmg, float trueDmg) {
         super(world, thrower);
         this.setSize(0.6F, 0.6F);
         this.damage = normalDmg;
         this.trueDamage = trueDmg;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.thrower) {
               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), this.damage);
               if (this.trueDamage > 0.0F) {
                  result.entityHit.hurtResistantTime = 0;
                  result.entityHit.attackEntityFrom(DamageSource.MAGIC, this.trueDamage);
               }
            }

            for(int i = 0; i < 20; ++i) {
               this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posY + this.rand.nextDouble(), this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, (this.rand.nextDouble() - (double)0.5F) * 0.2, this.rand.nextDouble() * 0.15, (this.rand.nextDouble() - (double)0.5F) * 0.2, new int[0]);
            }

            for(int i = 0; i < 8; ++i) {
               this.world.spawnParticle(EnumParticleTypes.WATER_DROP, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, this.posY + (double)0.5F + this.rand.nextDouble(), this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, (double)0.0F, 0.1, (double)0.0F, new int[0]);
            }

            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 100) {
            this.setDead();
         } else {
            if (this.world.isRemote && this.lifetime > 1) {
               for(int i = 0; i < 2; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.3, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.3, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.3, (double)0.0F, -0.02, (double)0.0F, new int[0]);
               }

               double speed = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
               if (speed > 0.05) {
                  int count = 3 + this.rand.nextInt(3);

                  for(int i = 0; i < count; ++i) {
                     double spread = 0.15;
                     this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX - this.motionX * (double)0.5F + (this.rand.nextDouble() - (double)0.5F) * spread, this.posY - this.motionY * (double)0.5F + (this.rand.nextDouble() - (double)0.5F) * spread, this.posZ - this.motionZ * (double)0.5F + (this.rand.nextDouble() - (double)0.5F) * spread, -this.motionX * 0.1 + (this.rand.nextDouble() - (double)0.5F) * 0.03, -this.motionY * 0.1 + (this.rand.nextDouble() - (double)0.5F) * 0.03, -this.motionZ * 0.1 + (this.rand.nextDouble() - (double)0.5F) * 0.03, new int[0]);
                  }

                  this.world.spawnParticle(EnumParticleTypes.WATER_DROP, this.posX - this.motionX * 0.3 + (this.rand.nextDouble() - (double)0.5F) * 0.1, this.posY - this.motionY * 0.3 + this.rand.nextDouble() * 0.2, this.posZ - this.motionZ * 0.3 + (this.rand.nextDouble() - (double)0.5F) * 0.1, (double)0.0F, -0.05, (double)0.0F, new int[0]);
               }
            }

         }
      }

      protected float getGravityVelocity() {
         return 0.01F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setFloat("suitonDmg", this.damage);
         compound.setFloat("suitonTrue", this.trueDamage);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("suitonDmg")) {
            this.damage = compound.getFloat("suitonDmg");
         }

         if (compound.hasKey("suitonTrue")) {
            this.trueDamage = compound.getFloat("suitonTrue");
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public static class SuitonRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/water_ball2.png");
      private static final int LAT_BANDS = 10;
      private static final int LON_SEGS = 16;
      private static final float RADIUS = 0.4F;
      private static final float[][] sphereX;
      private static final float[][] sphereY;
      private static final float[][] sphereZ;
      private static final float[][] sphereU;
      private static final float[][] sphereV;
      private static final float[][] sphereNX;
      private static final float[][] sphereNY;
      private static final float[][] sphereNZ;

      public SuitonRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         float ticks = (float)entity.ticksExisted + partialTicks;
         double mx = entity.motionX;
         double my = entity.motionY;
         double mz = entity.motionZ;
         float speed = (float)Math.sqrt(mx * mx + my * my + mz * mz);
         GlStateManager.pushMatrix();
         GlStateManager.translate(x, y + 0.3, z);
         GlStateManager.enableRescaleNormal();
         if (speed > 0.01F) {
            float yaw = (float)Math.toDegrees(Math.atan2(mx, mz));
            float horizSpeed = (float)Math.sqrt(mx * mx + mz * mz);
            float pitch = (float)Math.toDegrees(Math.atan2(my, (double)horizSpeed));
            GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-pitch, 1.0F, 0.0F, 0.0F);
         }

         float elongation = 1.0F + speed * 0.5F;
         GlStateManager.scale(1.0F, 1.0F, elongation);
         GlStateManager.matrixMode(5890);
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, -ticks * 0.2F, 0.0F);
         GlStateManager.matrixMode(5888);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.disableLighting();
         GlStateManager.depthMask(false);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 0.6F);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         this.bindEntityTexture(entity);
         this.drawSphere();
         GlStateManager.matrixMode(5890);
         GlStateManager.popMatrix();
         GlStateManager.matrixMode(5888);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.depthMask(true);
         GlStateManager.enableLighting();
         GlStateManager.disableBlend();
         GlStateManager.disableRescaleNormal();
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      private void drawSphere() {
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();

         for(int lat = 0; lat < 10; ++lat) {
            buffer.begin(5, DefaultVertexFormats.POSITION_TEX_NORMAL);

            for(int lon = 0; lon <= 16; ++lon) {
               buffer.pos((double)sphereX[lat][lon], (double)sphereY[lat][lon], (double)sphereZ[lat][lon]).tex((double)sphereU[lat][lon], (double)sphereV[lat][lon]).normal(sphereNX[lat][lon], sphereNY[lat][lon], sphereNZ[lat][lon]).endVertex();
               buffer.pos((double)sphereX[lat + 1][lon], (double)sphereY[lat + 1][lon], (double)sphereZ[lat + 1][lon]).tex((double)sphereU[lat + 1][lon], (double)sphereV[lat + 1][lon]).normal(sphereNX[lat + 1][lon], sphereNY[lat + 1][lon], sphereNZ[lat + 1][lon]).endVertex();
            }

            tessellator.draw();
         }

      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }

      static {
         int rows = 11;
         int cols = 17;
         sphereX = new float[rows][cols];
         sphereY = new float[rows][cols];
         sphereZ = new float[rows][cols];
         sphereU = new float[rows][cols];
         sphereV = new float[rows][cols];
         sphereNX = new float[rows][cols];
         sphereNY = new float[rows][cols];
         sphereNZ = new float[rows][cols];

         for(int lat = 0; lat <= 10; ++lat) {
            float theta = (float)((double)lat * Math.PI / (double)10.0F);
            float sinTheta = (float)Math.sin((double)theta);
            float cosTheta = (float)Math.cos((double)theta);

            for(int lon = 0; lon <= 16; ++lon) {
               float phi = (float)((double)lon * (double)2.0F * Math.PI / (double)16.0F);
               float sinPhi = (float)Math.sin((double)phi);
               float cosPhi = (float)Math.cos((double)phi);
               float nx = cosPhi * sinTheta;
               float nz = sinPhi * sinTheta;
               sphereX[lat][lon] = 0.4F * nx;
               sphereY[lat][lon] = 0.4F * cosTheta;
               sphereZ[lat][lon] = 0.4F * nz;
               sphereU[lat][lon] = (float)lon / 16.0F;
               sphereV[lat][lon] = (float)lat / 10.0F;
               sphereNX[lat][lon] = nx;
               sphereNY[lat][lon] = cosTheta;
               sphereNZ[lat][lon] = nz;
            }
         }

      }
   }
}
