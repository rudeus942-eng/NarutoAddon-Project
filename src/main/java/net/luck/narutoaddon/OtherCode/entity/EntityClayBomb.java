
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.item.ItemJutsu;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityClayBomb extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 224;

   public EntityClayBomb(ElementsInfTsukAddon instance) {
      super(instance, 46);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "clay_bomb"), 224).name("inftsuk_clay_bomb").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, ClayBombRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private static final DataParameter<Byte> BOMB_TIER;
      private int lifetime = 0;
      private float damage = 15.0F;
      private float trueDamage = 4.0F;
      private float explosionRadius = 3.0F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.4F, 0.4F);
      }

      public EntityCustom(World world, EntityLivingBase thrower, float normalDmg, float trueDmg, float radius) {
         super(world, thrower);
         this.setSize(0.8F, 0.8F);
         this.damage = normalDmg;
         this.trueDamage = trueDmg;
         this.explosionRadius = radius;
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(BOMB_TIER, (byte)1);
      }

      public void setBombTier(int tier) {
         this.dataManager.set(BOMB_TIER, (byte)tier);
      }

      public int getBombTier() {
         return (Byte)this.dataManager.get(BOMB_TIER);
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            int tier = this.getBombTier();
            float searchRadius = this.explosionRadius * 1.5F;
            AxisAlignedBB aoe = new AxisAlignedBB(this.posX - (double)searchRadius, this.posY - (double)searchRadius, this.posZ - (double)searchRadius, this.posX + (double)searchRadius, this.posY + (double)searchRadius, this.posZ + (double)searchRadius);

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, aoe)) {
               if (e != this.thrower && e instanceof EntityLivingBase) {
                  double dist = (double)e.getDistance(this);
                  if (!(dist > (double)searchRadius)) {
                     float falloff = (float)Math.max(0.3, (double)1.0F - dist / ((double)this.explosionRadius + (double)0.5F));
                     float aoeDmg = this.damage * falloff;
                     float aoeTrueDmg = this.trueDamage * falloff;
                     e.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), aoeDmg);
                     e.hurtResistantTime = 0;
                     e.attackEntityFrom(DamageSource.MAGIC, aoeTrueDmg);
                     int slowDuration = tier >= 3 ? 40 : (tier >= 2 ? 30 : 20);
                     int slowLevel = tier >= 3 ? 1 : 0;
                     ((EntityLivingBase)e).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, slowDuration, slowLevel));
                  }
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               int cloudCount = tier >= 3 ? 60 : (tier >= 2 ? 45 : 30);
               int explCount = tier >= 3 ? 30 : (tier >= 2 ? 20 : 15);
               int smokeCount = tier >= 3 ? 25 : (tier >= 2 ? 15 : 10);

               for(int i = 0; i < cloudCount; ++i) {
                  ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)this.explosionRadius * (double)1.5F, this.posY + this.rand.nextDouble() * (double)this.explosionRadius, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)this.explosionRadius * (double)1.5F, 1, (this.rand.nextDouble() - (double)0.5F) * 0.2, this.rand.nextDouble() * 0.15, (this.rand.nextDouble() - (double)0.5F) * 0.2, 0.02, new int[0]);
               }

               for(int i = 0; i < explCount; ++i) {
                  ws.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)this.explosionRadius, this.posY + this.rand.nextDouble() * (double)this.explosionRadius * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)this.explosionRadius, 1, (this.rand.nextDouble() - (double)0.5F) * 0.1, this.rand.nextDouble() * 0.1, (this.rand.nextDouble() - (double)0.5F) * 0.1, 0.01, new int[0]);
               }

               for(int i = 0; i < smokeCount; ++i) {
                  ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)this.explosionRadius, this.posY + this.rand.nextDouble() * (double)this.explosionRadius, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)this.explosionRadius, 1, (double)0.0F, 0.05, (double)0.0F, 0.01, new int[0]);
               }

               if (tier >= 2) {
                  int flameCount = tier >= 3 ? 40 : 20;

                  for(int i = 0; i < flameCount; ++i) {
                     ws.spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)this.explosionRadius * 1.2, this.posY + this.rand.nextDouble() * (double)this.explosionRadius * 0.8, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)this.explosionRadius * 1.2, 1, (this.rand.nextDouble() - (double)0.5F) * 0.15, this.rand.nextDouble() * 0.2, (this.rand.nextDouble() - (double)0.5F) * 0.15, 0.03, new int[0]);
                  }
               }

               if (tier >= 3) {
                  ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)1.0F, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);

                  for(int i = 0; i < 8; ++i) {
                     ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)this.explosionRadius, this.posY + this.rand.nextDouble() * (double)this.explosionRadius * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)this.explosionRadius, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  }

                  for(int i = 0; i < 15; ++i) {
                     ws.spawnParticle(EnumParticleTypes.LAVA, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)this.explosionRadius, this.posY + this.rand.nextDouble() * (double)this.explosionRadius, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)this.explosionRadius, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  }
               }
            }

            float volume = tier >= 3 ? 2.0F : (tier >= 2 ? 1.5F : 1.2F);
            float pitch = tier >= 3 ? 0.7F : (tier >= 2 ? 0.85F : 1.0F + this.rand.nextFloat() * 0.3F);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, volume, pitch);
            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 100) {
            this.setDead();
         } else {
            int tier = this.getBombTier();
            if (this.world.isRemote) {
               this.world.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.15, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.15, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.15, (double)0.0F, 0.01, (double)0.0F, new int[0]);
               if (this.lifetime % 2 == 0) {
                  this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY, this.posZ, (double)0.0F, -0.01, (double)0.0F, new int[0]);
               }

               if (tier >= 2) {
                  this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.3, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.3, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.3, (double)0.0F, 0.02, (double)0.0F, new int[0]);
                  this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.2, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.2, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.2, (double)0.0F, 0.01, (double)0.0F, new int[0]);
               }

               if (tier >= 3) {
                  for(int i = 0; i < 3; ++i) {
                     this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, (double)0.0F, -0.02, (double)0.0F, new int[0]);
                  }

                  this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY, this.posZ, (double)0.0F, 0.01, (double)0.0F, new int[0]);
                  if (this.lifetime % 3 == 0) {
                     this.world.spawnParticle(EnumParticleTypes.LAVA, this.posX, this.posY, this.posZ, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  }
               }
            }

         }
      }

      protected float getGravityVelocity() {
         return 0.04F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("cbDamage", this.damage);
         compound.setFloat("cbTrueDamage", this.trueDamage);
         compound.setFloat("cbRadius", this.explosionRadius);
         compound.setByte("bombTier", (byte)this.getBombTier());
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("cbDamage")) {
            this.damage = compound.getFloat("cbDamage");
         }

         if (compound.hasKey("cbTrueDamage")) {
            this.trueDamage = compound.getFloat("cbTrueDamage");
         }

         if (compound.hasKey("cbRadius")) {
            this.explosionRadius = compound.getFloat("cbRadius");
         }

         if (compound.hasKey("bombTier")) {
            this.setBombTier(compound.getByte("bombTier"));
         }

      }

      static {
         BOMB_TIER = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BYTE);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ClayBombRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod", "textures/fireball.png");

      public ClayBombRenderer(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.0F;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         int tier = entity.getBombTier();
         float scale;
         float r;
         float g;
         float b;
         float a;
         switch (tier) {
            case 2:
               scale = 1.2F;
               r = 1.0F;
               g = 0.95F;
               b = 0.8F;
               a = 0.95F;
               break;
            case 3:
               scale = 2.0F;
               r = 1.0F;
               g = 0.85F;
               b = 0.6F;
               a = 0.95F;
               break;
            default:
               scale = 0.6F;
               r = 0.95F;
               g = 0.92F;
               b = 0.85F;
               a = 1.0F;
         }

         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         GlStateManager.translate(x, y + 0.2, z);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         int thirdPerson = Minecraft.getMinecraft().gameSettings.thirdPersonView;
         GlStateManager.rotate((float)(thirdPerson == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         GlStateManager.disableLighting();
         GlStateManager.depthMask(false);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.color(r, g, b, a);
         float half = 0.5F;
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         buffer.pos((double)(-half), (double)(-half), (double)0.0F).tex((double)0.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)half, (double)(-half), (double)0.0F).tex((double)1.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)half, (double)half, (double)0.0F).tex((double)1.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buffer.pos((double)(-half), (double)half, (double)0.0F).tex((double)0.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         tessellator.draw();
         if (tier >= 2) {
            float glowAlpha = tier >= 3 ? 0.4F : 0.3F;
            float glowScale = tier >= 3 ? 1.4F : 1.2F;
            GlStateManager.scale(glowScale, glowScale, glowScale);
            GlStateManager.color(1.0F, 0.9F, 0.5F, glowAlpha);
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
            buffer.pos((double)(-half), (double)(-half), (double)0.0F).tex((double)0.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
            buffer.pos((double)half, (double)(-half), (double)0.0F).tex((double)1.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
            buffer.pos((double)half, (double)half, (double)0.0F).tex((double)1.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
            buffer.pos((double)(-half), (double)half, (double)0.0F).tex((double)0.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
            tessellator.draw();
         }

         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.depthMask(true);
         GlStateManager.enableLighting();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
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
