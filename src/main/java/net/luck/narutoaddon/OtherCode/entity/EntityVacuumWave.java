
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.Minecraft;
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
public class EntityVacuumWave extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 218;

   public EntityVacuumWave(ElementsInfTsukAddon instance) {
      super(instance, 56);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "vacuum_wave"), 218).name("inftsuk_vacuum_wave").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, VacuumWaveRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private int lifetime = 0;
      private float damage = 14.0F;
      private float trueDamage = 5.0F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(2.0F, 0.4F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(2.0F, 0.4F);
      }

      public EntityCustom(World world, EntityLivingBase thrower, float normalDmg, float trueDmg) {
         super(world, thrower);
         this.setSize(2.0F, 0.4F);
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

               double mx = this.motionX;
               double mz = this.motionZ;
               double len = Math.sqrt(mx * mx + mz * mz);
               if (len > 0.01) {
                  Entity var10000 = result.entityHit;
                  var10000.motionX += mx / len * 0.8;
                  var10000 = result.entityHit;
                  var10000.motionY += 0.2;
                  var10000 = result.entityHit;
                  var10000.motionZ += mz / len * 0.8;
                  result.entityHit.velocityChanged = true;
               }
            }

            for(int i = 0; i < 20; ++i) {
               this.world.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.5F, this.posY + this.rand.nextDouble() * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.5F, (this.rand.nextDouble() - (double)0.5F) * 0.4, this.rand.nextDouble() * 0.2, (this.rand.nextDouble() - (double)0.5F) * 0.4, new int[0]);
            }

            for(int i = 0; i < 10; ++i) {
               this.world.spawnParticle(EnumParticleTypes.CRIT, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posY + this.rand.nextDouble() * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, this.rand.nextDouble() * 0.3, (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, new int[0]);
            }

            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 60) {
            this.setDead();
         } else {
            if (this.world.isRemote && this.lifetime > 1) {
               if (this.lifetime % 2 == 0) {
                  for(int i = 0; i < 4; ++i) {
                     this.world.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.3, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, (double)0.0F, 0.01, (double)0.0F, new int[0]);
                  }
               }

               this.world.spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.8, this.posY, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.8, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

         }
      }

      protected float getGravityVelocity() {
         return 0.0F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setFloat("vacuumDmg", this.damage);
         compound.setFloat("vacuumTrue", this.trueDamage);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("vacuumDmg")) {
            this.damage = compound.getFloat("vacuumDmg");
         }

         if (compound.hasKey("vacuumTrue")) {
            this.trueDamage = compound.getFloat("vacuumTrue");
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public static class VacuumWaveRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/disk.png");

      public VacuumWaveRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         GlStateManager.translate(x, y + 0.2, z);
         GlStateManager.enableRescaleNormal();
         GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         int thirdPerson = Minecraft.getMinecraft().gameSettings.thirdPersonView;
         GlStateManager.rotate((float)(thirdPerson == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(60.0F * (partialTicks + (float)entity.ticksExisted), 0.0F, 0.0F, 1.0F);
         GlStateManager.enableBlend();
         GlStateManager.disableLighting();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.color(0.8F, 1.0F, 0.8F, 0.7F);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         float hw = 1.25F;
         float hh = 0.25F;
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         buffer.pos((double)(-hw), (double)(-hh), (double)0.0F).tex((double)0.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)hw, (double)(-hh), (double)0.0F).tex((double)1.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)hw, (double)hh, (double)0.0F).tex((double)1.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)(-hw), (double)hh, (double)0.0F).tex((double)0.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         tessellator.draw();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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
