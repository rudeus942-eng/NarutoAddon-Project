
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
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
public class EntityFutonBullet extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 211;

   public EntityFutonBullet(ElementsInfTsukAddon instance) {
      super(instance, 39);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "futon_bullet"), 211).name("inftsuk_futon_bullet").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, FutonRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private int lifetime = 0;
      private float damage = 4.0F;
      private float trueDamage = 1.0F;
      private float knockbackStrength = 0.8F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.8F, 0.8F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(0.8F, 0.8F);
      }

      public EntityCustom(World world, EntityLivingBase thrower, float normalDmg, float trueDmg) {
         super(world, thrower);
         this.setSize(0.8F, 0.8F);
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

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)2.5F))) {
               if (e != this.thrower && e instanceof EntityLivingBase) {
                  double dx = e.posX - this.posX;
                  double dz = e.posZ - this.posZ;
                  double dist = Math.sqrt(dx * dx + dz * dz);
                  if (dist > 0.1) {
                     e.motionX += dx / dist * (double)this.knockbackStrength;
                     e.motionY += (double)0.25F;
                     e.motionZ += dz / dist * (double)this.knockbackStrength;
                     e.velocityChanged = true;
                  }
               }
            }

            for(int i = 0; i < 15; ++i) {
               this.world.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posY + this.rand.nextDouble(), this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, (this.rand.nextDouble() - (double)0.5F) * 0.3, this.rand.nextDouble() * 0.2, (this.rand.nextDouble() - (double)0.5F) * 0.3, new int[0]);
            }

            SoundEvent windSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "windecho"));
            if (windSound != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, windSound, SoundCategory.HOSTILE, 1.0F, 1.0F + this.rand.nextFloat() * 0.3F);
            }

            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 80) {
            this.setDead();
         } else {
            if (this.world.isRemote && this.lifetime > 1) {
               for(int i = 0; i < 3; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, (double)0.0F, 0.01, (double)0.0F, new int[0]);
               }
            }

         }
      }

      protected float getGravityVelocity() {
         return 0.0F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setFloat("futonDmg", this.damage);
         compound.setFloat("futonTrue", this.trueDamage);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("futonDmg")) {
            this.damage = compound.getFloat("futonDmg");
         }

         if (compound.hasKey("futonTrue")) {
            this.trueDamage = compound.getFloat("futonTrue");
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public static class FutonRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/disk.png");

      public FutonRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         float scale = 1.2F;
         GlStateManager.translate(x, y + (double)0.25F, z);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         int thirdPerson = Minecraft.getMinecraft().gameSettings.thirdPersonView;
         GlStateManager.rotate((float)(thirdPerson == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(45.0F * (partialTicks + (float)entity.ticksExisted), 0.0F, 0.0F, 1.0F);
         GlStateManager.enableBlend();
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         buffer.pos(-0.4, -0.4, (double)0.0F).tex((double)0.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos(0.4, -0.4, (double)0.0F).tex((double)1.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos(0.4, 0.4, (double)0.0F).tex((double)1.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos(-0.4, 0.4, (double)0.0F).tex((double)0.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
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
