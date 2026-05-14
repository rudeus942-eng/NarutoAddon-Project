
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
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
public class EntityFingerBullet extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 222;

   public EntityFingerBullet(ElementsInfTsukAddon instance) {
      super(instance, 45);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "finger_bullet"), 222).name("inftsuk_finger_bullet").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, FingerBulletRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private static final DataParameter<Float> MODEL_SCALE;
      private int lifetime = 0;
      private float damage = 4.0F;
      private float trueDamage = 1.2F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.2F, 0.2F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(0.2F, 0.2F);
      }

      public EntityCustom(World world, EntityLivingBase thrower, float normalDmg, float trueDmg) {
         super(world, thrower);
         this.setSize(0.2F, 0.2F);
         this.damage = normalDmg;
         this.trueDamage = trueDmg;
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(MODEL_SCALE, 0.3F);
      }

      public float getEntityScale() {
         return (Float)this.dataManager.get(MODEL_SCALE);
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.thrower) {
               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.thrower), this.damage);
               result.entityHit.hurtResistantTime = 0;
               result.entityHit.attackEntityFrom(DamageSource.MAGIC, this.trueDamage);
               if (result.entityHit instanceof EntityLivingBase) {
                  ((EntityLivingBase)result.entityHit).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 20, 0));
               }
            }

            for(int i = 0; i < 8; ++i) {
               this.world.spawnParticle(EnumParticleTypes.CRIT, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.4, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.4, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.4, (this.rand.nextDouble() - (double)0.5F) * 0.3, this.rand.nextDouble() * 0.3, (this.rand.nextDouble() - (double)0.5F) * 0.3, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SKELETON_HURT, SoundCategory.HOSTILE, 0.8F, 1.4F + this.rand.nextFloat() * 0.3F);
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
         if (this.lifetime > 60) {
            this.setDead();
         } else {
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

            if (!this.world.isRemote && this.lifetime == 1) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SKELETON_SHOOT, SoundCategory.HOSTILE, 1.0F, 1.3F + this.rand.nextFloat() * 0.2F);
            }

            if (this.world.isRemote && this.lifetime % 2 == 0) {
               this.world.spawnParticle(EnumParticleTypes.CRIT, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.1, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.1, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.1, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

         }
      }

      protected float getGravityVelocity() {
         return 0.01F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("fbDamage", this.damage);
         compound.setFloat("fbTrueDamage", this.trueDamage);
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

      }

      static {
         MODEL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelBoneSpike extends ModelBase {
      private final ModelRenderer bone;

      public ModelBoneSpike() {
         this.textureWidth = 32;
         this.textureHeight = 32;
         this.bone = new ModelRenderer(this);
         this.bone.setRotationPoint(0.0F, 0.0F, 0.0F);
         ModelRenderer bone2 = new ModelRenderer(this);
         bone2.setRotationPoint(0.0F, 0.0F, 4.0F);
         this.bone.addChild(bone2);
         this.setRotationAngle(bone2, 0.1309F, 0.0F, 0.0F);
         bone2.cubeList.add(new ModelBox(bone2, 0, 0, -4.0F, -32.0F, 0.0F, 8, 32, 0, 0.0F, false));
         ModelRenderer bone3 = new ModelRenderer(this);
         bone3.setRotationPoint(0.0F, 0.0F, -4.0F);
         this.bone.addChild(bone3);
         this.setRotationAngle(bone3, -0.1309F, 0.0F, 0.0F);
         bone3.cubeList.add(new ModelBox(bone3, 8, 0, -4.0F, -32.0F, 0.0F, 8, 32, 0, 0.0F, false));
         ModelRenderer bone4 = new ModelRenderer(this);
         bone4.setRotationPoint(4.0F, 0.0F, 0.0F);
         this.bone.addChild(bone4);
         this.setRotationAngle(bone4, -0.1309F, -1.5708F, 0.0F);
         bone4.cubeList.add(new ModelBox(bone4, 8, 0, -4.0F, -32.0F, 0.0F, 8, 32, 0, 0.0F, false));
         ModelRenderer bone5 = new ModelRenderer(this);
         bone5.setRotationPoint(-4.0F, 0.0F, 0.0F);
         this.bone.addChild(bone5);
         this.setRotationAngle(bone5, 0.1309F, -1.5708F, 0.0F);
         bone5.cubeList.add(new ModelBox(bone5, 0, 0, -4.0F, -32.0F, 0.0F, 8, 32, 0, 0.0F, false));
         ModelRenderer bone6 = new ModelRenderer(this);
         bone6.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bone.addChild(bone6);
         this.setRotationAngle(bone6, -1.5708F, 0.0F, 0.0F);
         bone6.cubeList.add(new ModelBox(bone6, 16, 24, -4.0F, -4.0F, 0.0F, 8, 8, 0, 0.0F, false));
      }

      public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         this.bone.render(scale);
      }

      private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class FingerBulletRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod", "textures/spike_wood.png");
      private static final int BONE_COLOR = -1516344;
      private final ModelBoneSpike model = new ModelBoneSpike();

      public FingerBulletRenderer(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.0F;
      }

      public boolean shouldRender(EntityCustom entity, ICamera camera, double camX, double camY, double camZ) {
         return true;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         if (entity.prevRotationYaw == 0.0F && entity.prevRotationPitch == 0.0F) {
            entity.prevRotationYaw = entity.rotationYaw;
            entity.prevRotationPitch = entity.rotationPitch;
         }

         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         GlStateManager.translate(x, y, z);
         float yaw = -entity.prevRotationYaw - (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
         float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
         GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(pitch - 90.0F, 1.0F, 0.0F, 0.0F);
         float scale = entity.getEntityScale();
         GlStateManager.scale(scale * 0.3F, scale, scale * 0.3F);
         float alpha = 1.0F;
         float red = 0.9098039F;
         float green = 0.8627451F;
         float blue = 0.78431374F;
         GlStateManager.disableCull();
         GlStateManager.disableLighting();
         GlStateManager.color(red, green, blue, alpha);
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.popMatrix();
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }
}
