
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

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityStormBolt extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 220;

   public EntityStormBolt(ElementsInfTsukAddon instance) {
      super(instance, 58);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "storm_bolt"), 220).name("inftsuk_storm_bolt").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, StormBoltRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private static final DataParameter<Float> MODEL_SCALE;
      private int lifetime = 0;
      private float damage = 22.0F;
      private float trueDamage = 10.0F;

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
            float aoeRadius = 4.0F;
            if (result.entityHit != null && result.entityHit != this.thrower) {
               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), this.damage);
               result.entityHit.hurtResistantTime = 0;
               result.entityHit.attackEntityFrom(DamageSource.MAGIC, this.trueDamage);
               if (result.entityHit instanceof EntityLivingBase) {
                  ((EntityLivingBase)result.entityHit).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
                  ((EntityLivingBase)result.entityHit).addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0));
               }
            }

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)aoeRadius))) {
               if (e != this.thrower && e instanceof EntityLivingBase) {
                  double dist = (double)e.getDistance(this);
                  float falloff = (float)Math.max(0.3, (double)1.0F - dist / ((double)aoeRadius + (double)1.0F));
                  float aoeDmg = this.damage * 0.6F * falloff;
                  e.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), aoeDmg);
                  ((EntityLivingBase)e).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
                  ((EntityLivingBase)e).addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0));
               }
            }

            for(int i = 0; i < 20; ++i) {
               this.world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, this.rand.nextDouble() * (double)0.5F, (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, new int[0]);
            }

            for(int i = 0; i < 20; ++i) {
               this.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, (this.rand.nextDouble() - (double)0.5F) * 0.4, this.rand.nextDouble() * 0.4, (this.rand.nextDouble() - (double)0.5F) * 0.4, new int[0]);
            }

            for(int i = 0; i < 20; ++i) {
               this.world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, (this.rand.nextDouble() - (double)0.5F) * 0.3, this.rand.nextDouble() * 0.3, (this.rand.nextDouble() - (double)0.5F) * 0.3, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 0.7F, 0.8F + this.rand.nextFloat() * 0.4F);
            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 60) {
            this.setDead();
         } else {
            if (!this.world.isRemote && this.lifetime == 1) {
               SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "electricity"));
               if (electricity != null) {
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, electricity, SoundCategory.HOSTILE, 1.0F, 1.2F);
               }
            }

            if (this.world.isRemote) {
               for(int i = 0; i < 3; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.7, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.7, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.7, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
               }

               if (this.lifetime % 2 == 0) {
                  for(int i = 0; i < 2; ++i) {
                     this.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.4, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.4, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.4, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
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
         compound.setFloat("sbDamage", this.damage);
         compound.setFloat("sbTrueDamage", this.trueDamage);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("sbDamage")) {
            this.damage = compound.getFloat("sbDamage");
         }

         if (compound.hasKey("sbTrueDamage")) {
            this.trueDamage = compound.getFloat("sbTrueDamage");
         }

      }

      static {
         MODEL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class StormBoltRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/ring_lightning.png");

      public StormBoltRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         GlStateManager.translate(x, y + (double)0.5F, z);
         GlStateManager.enableRescaleNormal();
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         int thirdPerson = Minecraft.getMinecraft().gameSettings.thirdPersonView;
         GlStateManager.rotate((float)(thirdPerson == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.enableBlend();
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         float rotation = 40.0F * (partialTicks + (float)entity.ticksExisted);
         GlStateManager.pushMatrix();
         float outerScale = 2.2F;
         GlStateManager.scale(outerScale, outerScale, outerScale);
         GlStateManager.rotate(rotation * 0.7F, 0.0F, 0.0F, 1.0F);
         GlStateManager.color(0.7F, 0.2F, 1.0F, 0.3F);
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         buffer.pos((double)-0.375F, (double)-0.375F, (double)0.0F).tex((double)0.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)0.375F, (double)-0.375F, (double)0.0F).tex((double)1.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)0.375F, (double)0.375F, (double)0.0F).tex((double)1.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)-0.375F, (double)0.375F, (double)0.0F).tex((double)0.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         tessellator.draw();
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         float innerScale = 1.8F;
         GlStateManager.scale(innerScale, innerScale, innerScale);
         GlStateManager.rotate(rotation, 0.0F, 0.0F, 1.0F);
         GlStateManager.color(0.7F, 0.2F, 1.0F, 0.9F);
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         buffer.pos((double)-0.375F, (double)-0.375F, (double)0.0F).tex((double)0.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)0.375F, (double)-0.375F, (double)0.0F).tex((double)1.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)0.375F, (double)0.375F, (double)0.0F).tex((double)1.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)-0.375F, (double)0.375F, (double)0.0F).tex((double)0.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         tessellator.draw();
         GlStateManager.popMatrix();
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
