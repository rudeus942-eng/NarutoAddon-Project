
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
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
public class EntityWaterCannonball extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 221;

   public EntityWaterCannonball(ElementsInfTsukAddon instance) {
      super(instance, 55);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "water_cannonball"), 221).name("inftsuk_water_cannonball").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, CannonballRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private int lifetime = 0;
      private float damage = 18.0F;
      private float trueDamage = 8.0F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(1.5F, 1.5F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(1.5F, 1.5F);
      }

      public EntityCustom(World world, EntityLivingBase thrower, float normalDmg, float trueDmg) {
         super(world, thrower);
         this.setSize(1.5F, 1.5F);
         this.damage = normalDmg;
         this.trueDamage = trueDmg;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            double aoeRadius = (double)4.0F;
            AxisAlignedBB aoe = new AxisAlignedBB(this.posX - aoeRadius, this.posY - aoeRadius, this.posZ - aoeRadius, this.posX + aoeRadius, this.posY + aoeRadius, this.posZ + aoeRadius);

            for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, aoe)) {
               if (target != this.thrower) {
                  double dist = target.getDistance(this.posX, this.posY, this.posZ);
                  if (!(dist > aoeRadius)) {
                     float falloff = 1.0F - (float)(dist / aoeRadius) * 0.5F;
                     float appliedDmg = this.damage * falloff;
                     target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), appliedDmg);
                     if (this.trueDamage > 0.0F) {
                        float appliedTrue = this.trueDamage * falloff;
                        target.hurtResistantTime = 0;
                        target.attackEntityFrom(DamageSource.MAGIC, appliedTrue);
                     }

                     target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 0));
                  }
               }
            }

            for(int i = 0; i < 60; ++i) {
               this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, this.posY + this.rand.nextDouble() * (double)2.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, (this.rand.nextDouble() - (double)0.5F) * 0.3, this.rand.nextDouble() * 0.2, (this.rand.nextDouble() - (double)0.5F) * 0.3, new int[0]);
            }

            for(int i = 0; i < 20; ++i) {
               this.world.spawnParticle(EnumParticleTypes.WATER_DROP, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F, this.posY + (double)0.5F + this.rand.nextDouble() * (double)1.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F, (double)0.0F, 0.15, (double)0.0F, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 1.5F, 0.8F + this.rand.nextFloat() * 0.3F);
            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 100) {
            this.setDead();
         } else {
            if (this.world.isRemote && this.lifetime > 1 && this.lifetime % 2 == 0) {
               for(int i = 0; i < 3; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.WATER_DROP, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.8, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.8, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.8, (double)0.0F, -0.03, (double)0.0F, new int[0]);
               }

               for(int i = 0; i < 4; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.6, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.6, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.6, (this.rand.nextDouble() - (double)0.5F) * 0.05, -0.02, (this.rand.nextDouble() - (double)0.5F) * 0.05, new int[0]);
               }
            }

         }
      }

      protected float getGravityVelocity() {
         return 0.0F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setFloat("cannonDmg", this.damage);
         compound.setFloat("cannonTrue", this.trueDamage);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("cannonDmg")) {
            this.damage = compound.getFloat("cannonDmg");
         }

         if (compound.hasKey("cannonTrue")) {
            this.trueDamage = compound.getFloat("cannonTrue");
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public static class CannonballRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/water_ball2.png");

      public CannonballRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         float ticks = (float)entity.ticksExisted + partialTicks;
         float rotation = ticks * 20.0F;
         float scale = 2.0F;
         GlStateManager.pushMatrix();
         GlStateManager.translate(x, y + (double)0.75F, z);
         GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(rotation, 0.0F, 0.0F, 1.0F);
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         GlStateManager.disableLighting();
         GlStateManager.depthMask(false);
         GlStateManager.color(0.4F, 0.7F, 1.0F, 0.8F);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         this.bindEntityTexture(entity);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         float half = 0.5F;
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
         buffer.pos((double)(-half), (double)(-half), (double)0.0F).tex((double)0.0F, (double)1.0F).endVertex();
         buffer.pos((double)half, (double)(-half), (double)0.0F).tex((double)1.0F, (double)1.0F).endVertex();
         buffer.pos((double)half, (double)half, (double)0.0F).tex((double)1.0F, (double)0.0F).endVertex();
         buffer.pos((double)(-half), (double)half, (double)0.0F).tex((double)0.0F, (double)0.0F).endVertex();
         tessellator.draw();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.depthMask(true);
         GlStateManager.enableLighting();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }
}
