
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemJutsu.JutsuEnum.Type;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityWindSlash extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 289;

   public EntityWindSlash(ElementsInfTsukAddon instance) {
      super(instance, 950);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "wind_slash"), 289).name("inftsuk_wind_slash").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SlashRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable implements ItemJutsu.IJutsu {
      private int maxAge = 10;
      private float damage = 0.0F;
      private EntityLivingBase shooterEntity;
      public float renderScale = 8.0F;
      public float rotationRoll = 0.0F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(1.0F, 0.5F);
         this.noClip = true;
      }

      public EntityCustom(World world, EntityLivingBase thrower, float damage, float roll) {
         super(world, thrower);
         this.setSize(1.0F, 0.5F);
         this.shooterEntity = thrower;
         this.damage = damage;
         this.rotationRoll = roll;
         this.noClip = true;
      }

      public Type getJutsuType() {
         return Type.FUTON;
      }

      protected void entityInit() {
         super.entityInit();
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.shooterEntity && result.entityHit instanceof EntityLivingBase) {
               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooterEntity), this.damage);
            }

         }
      }

      public void onUpdate() {
         super.onUpdate();
         if (this.ticksExisted > this.maxAge || !this.world.isBlockLoaded(new BlockPos(this))) {
            this.setDead();
         }

      }

      protected float getGravityVelocity() {
         return 0.0F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class SlashRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/vacuumwave.png");
      private final ModelSweep model = new ModelSweep();

      public SlashRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         if (entity.ticksExisted >= 1) {
            float ageInTicks = partialTicks + (float)entity.ticksExisted;
            float scale = entity.renderScale;
            this.bindEntityTexture(entity);
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)x, (float)y, (float)z);
            float interpYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
            float interpPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            GlStateManager.rotate(-interpYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(interpPitch - 180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(entity.rotationRoll, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.alphaFunc(516, 0.01F);
            GlStateManager.enableBlend();
            GlStateManager.disableCull();
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.color(0.9F, 0.95F, 1.0F, 0.8F);
            float progress = ageInTicks / (float)(entity.maxAge + 1);

            for(int i = 0; i < this.model.segment.length; ++i) {
               this.model.segment[i].isHidden = true;
            }

            int start = Math.max(0, (int)(progress * 17.0F) + 1 - 4);
            int end = Math.min((int)(progress * 17.0F) + 1, this.model.segment.length);

            for(int k = start; k < end; ++k) {
               this.model.segment[k].isHidden = false;
            }

            this.model.render(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F, 0.0625F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.popMatrix();
         }
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelSweep extends ModelBase {
      private final ModelRenderer bone;
      public final ModelRenderer[] segment = new ModelRenderer[17];

      public ModelSweep() {
         this.textureWidth = 16;
         this.textureHeight = 16;
         this.bone = new ModelRenderer(this);
         this.bone.setRotationPoint(0.0F, 0.0F, 0.0F);
         setRotationAngle(this.bone, 0.0F, -0.7854F, 0.0F);

         for(int j = 0; j < this.segment.length; ++j) {
            this.segment[j] = new ModelRenderer(this);
            this.segment[j].setRotationPoint(0.0F, 0.0F, 0.0F);
            this.bone.addChild(this.segment[j]);
            setRotationAngle(this.segment[j], 0.0F, -0.1963F * (float)j, 0.0F);
            this.segment[j].cubeList.add(new ModelBox(this.segment[j], -8, 0, -8.0F, 0.0F, 0.0F, 8, 0, 8, 0.0F, false));
         }

      }

      public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
         this.bone.render(f5);
      }

      private static void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }
   }
}
