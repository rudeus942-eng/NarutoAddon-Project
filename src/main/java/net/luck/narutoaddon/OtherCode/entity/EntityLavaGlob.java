
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
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
public class EntityLavaGlob extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 219;

   public EntityLavaGlob(ElementsInfTsukAddon instance) {
      super(instance, 57);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "lava_glob"), 219).name("inftsuk_lava_glob").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, LavaGlobRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private static final DataParameter<Float> MODEL_SCALE;
      private int lifetime = 0;
      private float damage = 20.0F;
      private float trueDamage = 8.0F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(1.0F, 1.0F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(1.0F, 1.0F);
      }

      public EntityCustom(World world, EntityLivingBase thrower, float normalDmg, float trueDmg) {
         super(world, thrower);
         this.setSize(1.0F, 1.0F);
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
         float halfWidth = 0.5F * scale;
         this.setSize(halfWidth * 2.0F, halfWidth * 2.0F);
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            float aoeRadius = 3.0F * this.getEntityScale();
            if (result.entityHit != null && result.entityHit != this.thrower) {
               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), this.damage);
               result.entityHit.hurtResistantTime = 0;
               result.entityHit.attackEntityFrom(DamageSource.MAGIC, this.trueDamage);
               result.entityHit.setFire(5);
               if (result.entityHit instanceof EntityLivingBase) {
                  ((EntityLivingBase)result.entityHit).addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0));
               }
            }

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)aoeRadius))) {
               if (e != this.thrower && e instanceof EntityLivingBase) {
                  double dist = (double)e.getDistance(this);
                  float falloff = (float)Math.max(0.3, (double)1.0F - dist / ((double)aoeRadius + (double)1.0F));
                  float aoeDmg = this.damage * 0.6F * falloff;
                  e.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), aoeDmg);
                  e.setFire(5);
                  ((EntityLivingBase)e).addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 0));
               }
            }

            float pScale = Math.max(1.0F, this.getEntityScale() * 0.5F);
            int flameCount = (int)(16.0F * pScale);
            int lavaCount = (int)(14.0F * pScale);
            int smokeCount = (int)(10.0F * pScale);

            for(int i = 0; i < flameCount; ++i) {
               this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F * (double)pScale, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F * (double)pScale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F * (double)pScale, (this.rand.nextDouble() - (double)0.5F) * 0.4, this.rand.nextDouble() * 0.4, (this.rand.nextDouble() - (double)0.5F) * 0.4, new int[0]);
            }

            for(int i = 0; i < lavaCount; ++i) {
               this.world.spawnParticle(EnumParticleTypes.LAVA, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)pScale, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)pScale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)pScale, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

            for(int i = 0; i < smokeCount; ++i) {
               this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)pScale, this.posY + this.rand.nextDouble() * (double)2.0F * (double)pScale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)pScale, (this.rand.nextDouble() - (double)0.5F) * 0.1, this.rand.nextDouble() * 0.15, (this.rand.nextDouble() - (double)0.5F) * 0.1, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.5F, 0.8F + this.rand.nextFloat() * 0.4F);
            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 80 && !this.isInWater()) {
            if (this.rand.nextFloat() <= 0.2F) {
               this.playSound(SoundEvents.BLOCK_LAVA_POP, 1.0F, this.rand.nextFloat() + 0.5F);
            }

            if (this.world.isRemote && this.lifetime % 2 == 0) {
               float ts = this.getEntityScale();
               int trailCount = Math.max(3, (int)(3.0F * ts * 0.4F));

               for(int i = 0; i < trailCount; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F * (double)ts, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F * (double)ts, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F * (double)ts, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
               }

               this.world.spawnParticle(EnumParticleTypes.DRIP_LAVA, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.3 * (double)ts, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.3 * (double)ts, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.3 * (double)ts, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

         } else {
            this.setDead();
         }
      }

      protected float getGravityVelocity() {
         return 0.0F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("lgDamage", this.damage);
         compound.setFloat("lgTrueDamage", this.trueDamage);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("lgDamage")) {
            this.damage = compound.getFloat("lgDamage");
         }

         if (compound.hasKey("lgTrueDamage")) {
            this.trueDamage = compound.getFloat("lgTrueDamage");
         }

      }

      static {
         MODEL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class LavaGlobRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod", "textures/magmaball.png");

      public LavaGlobRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         float scale = entity.getEntityScale();
         GlStateManager.translate(x, y + (double)0.5F * (double)scale, z);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(scale, scale, scale);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         int thirdPerson = Minecraft.getMinecraft().gameSettings.thirdPersonView;
         GlStateManager.rotate((float)(thirdPerson == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(9.0F * (float)entity.ticksExisted, 0.0F, 0.0F, 1.0F);
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         buffer.pos((double)-0.5F, (double)-0.5F, (double)0.0F).tex((double)0.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)0.5F, (double)-0.5F, (double)0.0F).tex((double)1.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)0.5F, (double)0.5F, (double)0.0F).tex((double)1.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)-0.5F, (double)0.5F, (double)0.0F).tex((double)0.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         tessellator.draw();
         GlStateManager.enableLighting();
         GlStateManager.disableRescaleNormal();
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }
}
