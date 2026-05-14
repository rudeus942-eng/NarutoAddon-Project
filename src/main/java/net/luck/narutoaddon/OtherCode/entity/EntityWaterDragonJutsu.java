
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
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
public class EntityWaterDragonJutsu extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 215;

   public EntityWaterDragonJutsu(ElementsInfTsukAddon instance) {
      super(instance, 43);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "water_dragon_jutsu"), 215).name("inftsuk_water_dragon_jutsu").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, WaterDragonRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private static final DataParameter<Float> MODEL_SCALE;
      private int lifetime = 0;
      private float damage = 20.0F;
      private float trueDamage = 10.0F;
      private float fullScale = 2.5F;

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
         this.setSize(1.5F, 1.5F);
         this.damage = normalDmg;
         this.trueDamage = trueDmg;
         float totalDmg = normalDmg + trueDmg;
         this.fullScale = totalDmg * 0.08F;
         if (this.fullScale < 1.5F) {
            this.fullScale = 1.5F;
         }

         if (this.fullScale > 4.0F) {
            this.fullScale = 4.0F;
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
         float halfWidth = 0.5F * scale;
         this.setSize(halfWidth * 2.0F, halfWidth * 2.0F);
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            float scale = this.getEntityScale();
            float aoeRadius = 3.0F;
            if (result.entityHit != null && result.entityHit != this.thrower) {
               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), this.damage);
               result.entityHit.hurtResistantTime = 0;
               result.entityHit.attackEntityFrom(DamageSource.MAGIC, this.trueDamage);
            }

            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)aoeRadius))) {
               if (e != this.thrower && e instanceof EntityLivingBase) {
                  double dist = (double)e.getDistance(this);
                  float falloff = (float)Math.max(0.3, (double)1.0F - dist / ((double)aoeRadius + (double)1.0F));
                  float aoeDmg = this.damage * 0.6F * falloff;
                  e.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), aoeDmg);
               }
            }

            for(int i = 0; i < 100; ++i) {
               this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)scale, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)scale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F * (double)scale, (this.rand.nextDouble() - (double)0.5F) * 0.4, this.rand.nextDouble() * 0.4, (this.rand.nextDouble() - (double)0.5F) * 0.4, new int[0]);
            }

            SoundEvent suiton = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "suiton_suiryuudan"));
            if (suiton != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, suiton, SoundCategory.HOSTILE, 3.0F, 0.9F);
            }

            this.setDead();
         }
      }

      public void onUpdate() {
         float savedYaw = this.rotationYaw;
         float savedPitch = this.rotationPitch;
         float savedPrevYaw = this.prevRotationYaw;
         float savedPrevPitch = this.prevRotationPitch;
         super.onUpdate();
         this.rotationYaw = savedYaw;
         this.rotationPitch = savedPitch;
         this.prevRotationYaw = savedPrevYaw;
         this.prevRotationPitch = savedPrevPitch;
         ++this.lifetime;
         if (this.lifetime > 120) {
            this.setDead();
         } else {
            if (!this.world.isRemote) {
               if (this.lifetime > 3 && this.lifetime % 4 == 0) {
                  for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)3.0F))) {
                     if (e != this.thrower && e instanceof EntityLivingBase && !(e instanceof EntityClayBirdC1.EntityCustom) && !(e instanceof EntityClayDragonC2.EntityCustom) && !(e instanceof EntityClayBombC3.EntityCustom)) {
                        double dist = (double)e.getDistance(this);
                        if (dist <= (double)3.0F) {
                           float proxDmg = this.damage * 0.5F;
                           e.attackEntityFrom(DamageSource.causeMobDamage(this.thrower), proxDmg);
                           e.hurtResistantTime = 0;
                           if (this.trueDamage > 0.0F) {
                              e.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), this.trueDamage * 0.5F);
                           }
                        }
                     }
                  }
               }

               if (this.lifetime == 1) {
                  SoundEvent suiton = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "suiton_suiryuudan"));
                  if (suiton != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, suiton, SoundCategory.HOSTILE, 5.0F, 1.0F);
                  }
               }

               if (this.lifetime <= 15) {
                  this.setEntityScale(0.5F + (this.fullScale - 0.5F) * (float)this.lifetime / 15.0F);
               }
            }

            double horizSpeed = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            if (horizSpeed > 0.001) {
               this.prevRotationYaw = this.rotationYaw;
               this.prevRotationPitch = this.rotationPitch;
               this.rotationYaw = (float)(MathHelper.atan2(this.motionZ, this.motionX) * (180D / Math.PI)) - 90.0F;
               this.rotationPitch = (float)(-(MathHelper.atan2(this.motionY, horizSpeed) * (180D / Math.PI)));
               if (this.lifetime <= 1) {
                  this.prevRotationYaw = this.rotationYaw;
                  this.prevRotationPitch = this.rotationPitch;
               }
            }

            if (this.world.isRemote) {
               float scale = this.getEntityScale();
               int trailCount = (int)(3.0F * scale);
               if (trailCount < 2) {
                  trailCount = 2;
               }

               for(int i = 0; i < trailCount; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F * (double)scale, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F * (double)scale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F * (double)scale, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
               }

               if (this.lifetime % 2 == 0) {
                  this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.2 * (double)scale, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.2 * (double)scale, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.2 * (double)scale, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
               }

               if (this.lifetime > 3) {
                  this.world.spawnParticle(EnumParticleTypes.DRIP_WATER, this.posX - this.motionX * (double)0.5F, this.posY - this.motionY * (double)0.5F, this.posZ - this.motionZ * (double)0.5F, (double)0.0F, -0.02, (double)0.0F, new int[0]);
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
         compound.setFloat("wdDamage", this.damage);
         compound.setFloat("wdTrueDamage", this.trueDamage);
         compound.setFloat("wdFullScale", this.fullScale);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("wdDamage")) {
            this.damage = compound.getFloat("wdDamage");
         }

         if (compound.hasKey("wdTrueDamage")) {
            this.trueDamage = compound.getFloat("wdTrueDamage");
         }

         if (compound.hasKey("wdFullScale")) {
            this.fullScale = compound.getFloat("wdFullScale");
         }

      }

      static {
         MODEL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class WaterDragonRenderer extends Render<EntityCustom> {
      private final ResourceLocation texture = new ResourceLocation("narutomod:textures/dragon_gray.png");
      private final ResourceLocation texture2 = new ResourceLocation("narutomod:textures/gas256.png");
      private final ModelWaterDragonHead model = new ModelWaterDragonHead();

      public WaterDragonRenderer(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.1F;
      }

      public boolean shouldRender(EntityCustom entity, ICamera camera, double camX, double camY, double camZ) {
         return true;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         float age = (float)entity.ticksExisted + partialTicks;
         float scale = entity.getEntityScale();
         float yaw = -entity.prevRotationYaw - (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
         float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
         this.model.setRotationAngles(0.0F, 0.0F, age, 0.0F, 0.0F, 0.0625F, entity);
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)x, (float)y + scale * 0.5F, (float)z);
         GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(pitch - 180.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.disableCull();
         this.bindEntityTexture(entity);
         GlStateManager.color(0.0F, 0.35F, 0.85F, 1.0F);
         this.model.teethUpper.isHidden = true;
         this.model.teethLower.isHidden = true;
         this.model.eyes.isHidden = true;
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.color(0.3F, 0.7F, 1.0F, 0.45F);
         this.bindTexture(this.texture2);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         GlStateManager.translate(0.0F, age * 0.015F, 0.0F);
         GlStateManager.matrixMode(5888);
         this.model.teethUpper.isHidden = false;
         this.model.teethLower.isHidden = false;
         this.model.eyes.isHidden = true;
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.059375F);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         GlStateManager.matrixMode(5888);
         GlStateManager.disableBlend();
         this.bindEntityTexture(entity);
         GlStateManager.disableLighting();
         GlStateManager.color(0.6F, 1.0F, 1.0F, 1.0F);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         this.model.eyes.isHidden = false;
         this.model.eyes.render(0.0625F);
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.popMatrix();
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return this.texture;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelWaterDragonHead extends ModelBase {
      private final ModelRenderer head;
      private final ModelRenderer flair;
      private final ModelRenderer bone;
      private final ModelRenderer bone2;
      private final ModelRenderer bone3;
      public final ModelRenderer teethUpper;
      public final ModelRenderer teethLower;
      private final ModelRenderer jaw;
      private final ModelRenderer[] hornRight = new ModelRenderer[7];
      private final ModelRenderer[] hornLeft = new ModelRenderer[7];
      private final ModelRenderer[] whiskerLeft = new ModelRenderer[6];
      private final ModelRenderer[] whiskerRight = new ModelRenderer[6];
      private final ModelRenderer[] spine = new ModelRenderer[100];
      public final ModelRenderer eyes;
      private static final int VISIBLE_SPINE_SEGMENTS = 8;

      public ModelWaterDragonHead() {
         this.textureWidth = 128;
         this.textureHeight = 128;
         this.head = new ModelRenderer(this);
         this.head.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.head.cubeList.add(new ModelBox(this.head, 64, 0, -6.0F, 6.0F, -26.0F, 12, 5, 16, 1.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 0, 0, -8.0F, -1.0F, -11.0F, 16, 16, 16, 1.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 32, 32, 2.0F, 4.0F, -28.0F, 4, 4, 6, 0.0F, true));
         this.head.cubeList.add(new ModelBox(this.head, 32, 32, -6.0F, 4.0F, -28.0F, 4, 4, 6, 0.0F, false));
         this.teethUpper = new ModelRenderer(this);
         this.teethUpper.setRotationPoint(0.0F, 24.0F, 0.0F);
         this.head.addChild(this.teethUpper);
         this.teethUpper.cubeList.add(new ModelBox(this.teethUpper, 0, 52, -6.0F, -12.0F, -26.0F, 12, 3, 16, 0.5F, false));
         this.flair = new ModelRenderer(this);
         this.flair.setRotationPoint(0.0F, -2.0F, -12.0F);
         this.head.addChild(this.flair);
         this.bone = new ModelRenderer(this);
         this.bone.setRotationPoint(9.0F, 9.0F, 0.0F);
         this.flair.addChild(this.bone);
         this.setRotationAngle(this.bone, 0.0F, -0.7854F, 0.0F);
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 52, 0.0F, -8.0F, 0.0F, 10, 16, 0, 0.0F, false));
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 52, -2.0F, -12.0F, 2.0F, 10, 16, 0, 0.0F, false));
         this.bone2 = new ModelRenderer(this);
         this.bone2.setRotationPoint(-9.0F, 9.0F, 0.0F);
         this.flair.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.0F, 0.7854F, 0.0F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, 52, -10.0F, -8.0F, 0.0F, 10, 16, 0, 0.0F, true));
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, 52, -8.0F, -12.0F, 2.0F, 10, 16, 0, 0.0F, true));
         this.bone3 = new ModelRenderer(this);
         this.bone3.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.flair.addChild(this.bone3);
         this.setRotationAngle(this.bone3, -0.8727F, 0.0F, 0.0F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 84, 42, -8.0F, -10.0F, 0.0F, 16, 10, 0, 0.0F, false));
         this.jaw = new ModelRenderer(this);
         this.jaw.setRotationPoint(0.0F, 11.0F, -9.0F);
         this.head.addChild(this.jaw);
         this.setRotationAngle(this.jaw, 0.7854F, 0.0F, 0.0F);
         this.jaw.cubeList.add(new ModelBox(this.jaw, 64, 22, -6.0F, 0.0F, -16.75F, 12, 4, 16, 1.0F, false));
         this.teethLower = new ModelRenderer(this);
         this.teethLower.setRotationPoint(0.0F, 13.0F, 9.0F);
         this.jaw.addChild(this.teethLower);
         this.teethLower.cubeList.add(new ModelBox(this.teethLower, 42, 42, -6.0F, -16.0F, -25.75F, 12, 2, 16, 0.5F, false));
         this.hornRight[0] = new ModelRenderer(this);
         this.hornRight[0].setRotationPoint(-6.0F, -2.0F, -13.0F);
         this.head.addChild(this.hornRight[0]);
         this.setRotationAngle(this.hornRight[0], 0.0873F, -0.5236F, 0.0F);
         this.hornRight[0].cubeList.add(new ModelBox(this.hornRight[0], 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, false));

         for(int i = 1; i < 7; ++i) {
            this.hornRight[i] = new ModelRenderer(this);
            float zPos = i <= 2 ? 7.0F : (i == 3 ? 6.5F : (i == 4 ? 6.0F : (i == 5 ? 5.5F : 5.0F)));
            float inflate = 1.0F - (float)i * 0.3F;
            if (i >= 4) {
               inflate = 0.1F - (float)(i - 3) * 0.3F;
            }

            this.hornRight[i].setRotationPoint(0.0F, 0.0F, zPos);
            this.hornRight[i - 1].addChild(this.hornRight[i]);
            this.setRotationAngle(this.hornRight[i], 0.0873F, 0.0873F, 0.0F);
            this.hornRight[i].cubeList.add(new ModelBox(this.hornRight[i], 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, inflate, false));
         }

         this.hornLeft[0] = new ModelRenderer(this);
         this.hornLeft[0].setRotationPoint(6.0F, -2.0F, -13.0F);
         this.head.addChild(this.hornLeft[0]);
         this.setRotationAngle(this.hornLeft[0], 0.0873F, 0.5236F, 0.0F);
         this.hornLeft[0].cubeList.add(new ModelBox(this.hornLeft[0], 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, true));

         for(int i = 1; i < 7; ++i) {
            this.hornLeft[i] = new ModelRenderer(this);
            float zPos = i <= 2 ? 7.0F : (i == 3 ? 6.5F : (i == 4 ? 6.0F : (i == 5 ? 5.5F : 5.0F)));
            float inflate = 1.0F - (float)i * 0.3F;
            if (i >= 4) {
               inflate = 0.1F - (float)(i - 3) * 0.3F;
            }

            this.hornLeft[i].setRotationPoint(0.0F, 0.0F, zPos);
            this.hornLeft[i - 1].addChild(this.hornLeft[i]);
            this.setRotationAngle(this.hornLeft[i], 0.0873F, -0.0873F, 0.0F);
            this.hornLeft[i].cubeList.add(new ModelBox(this.hornLeft[i], 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, inflate, true));
         }

         this.whiskerLeft[0] = new ModelRenderer(this);
         this.whiskerLeft[0].setRotationPoint(6.0F, 6.0F, -24.0F);
         this.head.addChild(this.whiskerLeft[0]);
         this.setRotationAngle(this.whiskerLeft[0], 0.0F, 1.0472F, 0.0F);
         this.whiskerLeft[0].cubeList.add(new ModelBox(this.whiskerLeft[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, true));

         for(int i = 1; i < 6; ++i) {
            this.whiskerLeft[i] = new ModelRenderer(this);
            this.whiskerLeft[i].setRotationPoint(0.0F, 0.0F, 6.0F);
            this.whiskerLeft[i - 1].addChild(this.whiskerLeft[i]);
            this.setRotationAngle(this.whiskerLeft[i], -0.0873F, -0.1745F, 0.0F);
            float inflate = 0.8F - (float)i * 0.2F;
            this.whiskerLeft[i].cubeList.add(new ModelBox(this.whiskerLeft[i], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, inflate, true));
         }

         this.whiskerRight[0] = new ModelRenderer(this);
         this.whiskerRight[0].setRotationPoint(-6.0F, 6.0F, -24.0F);
         this.head.addChild(this.whiskerRight[0]);
         this.setRotationAngle(this.whiskerRight[0], 0.0F, -1.0472F, 0.0F);
         this.whiskerRight[0].cubeList.add(new ModelBox(this.whiskerRight[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, false));

         for(int i = 1; i < 6; ++i) {
            this.whiskerRight[i] = new ModelRenderer(this);
            this.whiskerRight[i].setRotationPoint(0.0F, 0.0F, 6.0F);
            this.whiskerRight[i - 1].addChild(this.whiskerRight[i]);
            this.setRotationAngle(this.whiskerRight[i], -0.0873F, 0.1745F, 0.0F);
            float inflate = 0.8F - (float)i * 0.2F;
            this.whiskerRight[i].cubeList.add(new ModelBox(this.whiskerRight[i], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, inflate, false));
         }

         for(int i = 0; i < this.spine.length; ++i) {
            this.spine[i] = new ModelRenderer(this);
            this.spine[i].cubeList.add(new ModelBox(this.spine[i], 0, 32, -5.0F, -4.5F, 0.0F, 10, 10, 10, 2.0F, false));
            this.spine[i].cubeList.add(new ModelBox(this.spine[i], 48, 0, -1.0F, -10.5F, 2.0F, 2, 4, 6, 1.0F, false));
            if (i == 0) {
               this.spine[i].setRotationPoint(0.0F, 6.5F, 7.0F);
            } else {
               this.spine[i].setRotationPoint(0.0F, 0.0F, 11.0F);
               this.spine[i - 1].addChild(this.spine[i]);
            }
         }

         this.eyes = new ModelRenderer(this);
         this.eyes.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.eyes.cubeList.add(new ModelBox(this.eyes, 18, 20, -6.6F, 2.6F, -12.15F, 3, 2, 0, 0.0F, false));
         this.eyes.cubeList.add(new ModelBox(this.eyes, 18, 20, 3.6F, 2.6F, -12.15F, 3, 2, 0, 0.0F, true));
      }

      public void render(Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
         this.head.render(f5);
         this.spine[0].render(f5);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void setRotationAngles(float limbSwing, float f1, float ageInTicks, float f3, float headPitch, float f5, Entity e) {
         super.setRotationAngles(limbSwing, f1, ageInTicks, f3, headPitch, f5, e);

         for(int i = 1; i < this.hornRight.length; ++i) {
            this.hornRight[i].rotateAngleX = 0.0873F + MathHelper.sin((ageInTicks - (float)i) * 0.2F) * 0.0873F;
            this.hornRight[i].rotateAngleY = MathHelper.cos((ageInTicks - (float)i) * 0.3F) * 0.0873F;
            this.hornLeft[i].rotateAngleX = 0.0873F + MathHelper.sin((ageInTicks - (float)i) * 0.2F) * 0.0873F;
            this.hornLeft[i].rotateAngleY = -MathHelper.cos((ageInTicks - (float)i) * 0.3F) * 0.0873F;
         }

         float f6 = ((float)Math.PI / 180F);
         this.head.rotateAngleX = headPitch * f6;
         this.eyes.rotateAngleX = this.head.rotateAngleX;
         this.jaw.rotateAngleX = 0.5236F;

         for(int j = 2; j < this.whiskerRight.length; ++j) {
            this.whiskerLeft[j].rotateAngleZ = MathHelper.sin(ageInTicks * 0.15F + (float)j * 0.5F) * 0.3F;
            this.whiskerRight[j].rotateAngleZ = -MathHelper.sin(ageInTicks * 0.15F + (float)j * 0.5F) * 0.3F;
         }

         for(int j = 0; j < this.spine.length; ++j) {
            if (j < 8) {
               this.spine[j].isHidden = false;
               float wavePhase = ageInTicks * 0.15F - (float)j * 0.6F;
               this.spine[j].rotateAngleY = MathHelper.sin(wavePhase) * 0.15F;
               this.spine[j].rotateAngleX = MathHelper.cos(wavePhase * 0.7F) * 0.08F;
            } else {
               this.spine[j].isHidden = true;
            }
         }

      }
   }
}
