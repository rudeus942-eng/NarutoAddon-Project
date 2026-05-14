
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityItachiSusanoo extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 26;

   public EntityItachiSusanoo(ElementsInfTsukAddon instance) {
      super(instance, 226);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "itachi_susanoo"), 26).name("itachi_susanoo").tracker(128, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SusanooRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class SusanooRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation MAIN_TEXTURE = new ResourceLocation("narutomod", "textures/susanooskeleton.png");
      private static final ResourceLocation FLAME_TEXTURE = new ResourceLocation("narutomod", "textures/gas256.png");

      public SusanooRenderer(RenderManager renderManager) {
         super(renderManager, new ModelSusanooSkeleton(), 1.5F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return MAIN_TEXTURE;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         this.setModelVisibilities(entity);
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      private void setModelVisibilities(EntityCustom entity) {
         ModelSusanooSkeleton model = (ModelSusanooSkeleton)this.getMainModel();
         model.bipedRightLeg.showModel = false;
         model.bipedLeftLeg.showModel = false;
         boolean full = entity.isFullBody();
         model.bipedHead.showModel = full;
         model.bipedHeadwear.showModel = full;
         model.bipedRightArm.showModel = full;
         model.bipedLeftArm.showModel = full;
      }

      protected void renderModel(EntityCustom entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
         if (this.bindEntityTexture(entity)) {
            ModelSusanooSkeleton model = (ModelSusanooSkeleton)this.getMainModel();
            model.renderFlame = false;
            this.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            this.bindTexture(FLAME_TEXTURE);
            model.renderFlame = true;
            this.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor * 0.99F);
         }

      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         float scale = entity.getSusanooScale();
         GlStateManager.scale(scale, scale, scale);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelSusanooSkeleton extends ModelBiped {
      private final ModelRenderer head7_r1;
      private final ModelRenderer head6_r1;
      private final ModelRenderer head5_r1;
      private final ModelRenderer head_r1;
      private final ModelRenderer HornStyle1;
      private final ModelRenderer right;
      private final ModelRenderer cube_r1;
      private final ModelRenderer cube_r2;
      private final ModelRenderer cube_r3;
      private final ModelRenderer left;
      private final ModelRenderer cube_r4;
      private final ModelRenderer cube_r5;
      private final ModelRenderer cube_r6;
      private final ModelRenderer HornStyle2;
      private final ModelRenderer right5;
      private final ModelRenderer cube_r7;
      private final ModelRenderer cube_r8;
      private final ModelRenderer cube_r9;
      private final ModelRenderer left8;
      private final ModelRenderer cube_r10;
      private final ModelRenderer cube_r11;
      private final ModelRenderer cube_r12;
      private final ModelRenderer cube_r13;
      private final ModelRenderer cube_r14;
      private final ModelRenderer cube_r15;
      private final ModelRenderer rightHand;
      private final ModelRenderer rightFingers;
      private final ModelRenderer cube_r16;
      private final ModelRenderer cube_r17;
      private final ModelRenderer cube_r18;
      private final ModelRenderer leftHand;
      private final ModelRenderer leftFingers;
      private final float maxAlpha = 0.8F;
      boolean renderFlame;
      private final float[][] rightArmPreset = new float[][]{{-1.0472F, 1.0472F, 0.5236F}, {-1.0472F, -1.0472F, 0.0F}, {-0.9599F, 0.0F, 0.0F}};

      public ModelSusanooSkeleton() {
         this.textureWidth = 512;
         this.textureHeight = 512;
         this.bipedHead = new ModelRenderer(this);
         this.bipedHead.setRotationPoint(0.0F, -8.0F, 0.0F);
         this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 36, 147, -10.0F, -23.0F, -10.0F, 20, 14, 20, 0.0F, false));
         this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 0, 203, -9.0F, -24.0F, -9.0F, 18, 1, 18, 0.0F, false));
         this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 85, 201, -9.0F, -21.0F, -11.0F, 18, 12, 3, 0.0F, false));
         this.head7_r1 = new ModelRenderer(this);
         this.head7_r1.setRotationPoint(0.0F, -0.5F, -9.0F);
         this.bipedHead.addChild(this.head7_r1);
         this.setRotationAngle(this.head7_r1, 0.1745F, 0.0F, 0.0F);
         this.head7_r1.cubeList.add(new ModelBox(this.head7_r1, 152, 76, -4.0F, -1.5F, 0.5F, 8, 3, 2, 0.0F, false));
         this.head6_r1 = new ModelRenderer(this);
         this.head6_r1.setRotationPoint(0.0F, -1.3025F, 4.122F);
         this.bipedHead.addChild(this.head6_r1);
         this.setRotationAngle(this.head6_r1, 0.4363F, 0.0F, 0.0F);
         this.head6_r1.cubeList.add(new ModelBox(this.head6_r1, 115, 43, -4.0F, -5.0F, -0.622F, 8, 10, 8, 0.0F, false));
         this.head5_r1 = new ModelRenderer(this);
         this.head5_r1.setRotationPoint(0.0F, -7.6846F, -8.9841F);
         this.bipedHead.addChild(this.head5_r1);
         this.setRotationAngle(this.head5_r1, 0.2182F, 0.0F, 0.0F);
         this.head5_r1.cubeList.add(new ModelBox(this.head5_r1, 141, 108, -7.0F, -2.5F, -1.5F, 14, 3, 3, 0.0F, false));
         this.head_r1 = new ModelRenderer(this);
         this.head_r1.setRotationPoint(0.0F, -15.0F, 23.0F);
         this.bipedHead.addChild(this.head_r1);
         this.setRotationAngle(this.head_r1, 0.1309F, 0.0F, 0.0F);
         this.head_r1.cubeList.add(new ModelBox(this.head_r1, 132, 149, -9.0F, 1.0F, -33.0F, 18, 9, 18, 0.0F, false));
         this.HornStyle1 = new ModelRenderer(this);
         this.HornStyle1.setRotationPoint(0.0F, 32.0F, 0.0F);
         this.bipedHead.addChild(this.HornStyle1);
         this.right = new ModelRenderer(this);
         this.right.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.HornStyle1.addChild(this.right);
         this.cube_r1 = new ModelRenderer(this);
         this.cube_r1.setRotationPoint(-19.0517F, -58.4594F, -0.5F);
         this.right.addChild(this.cube_r1);
         this.setRotationAngle(this.cube_r1, 0.0F, 0.0F, 1.1781F);
         this.cube_r1.cubeList.add(new ModelBox(this.cube_r1, 0, 312, -17.0F, -15.5406F, -15.0F, 33, 30, 30, -14.0F, false));
         this.cube_r2 = new ModelRenderer(this);
         this.cube_r2.setRotationPoint(-13.1084F, -54.1158F, -0.5F);
         this.right.addChild(this.cube_r2);
         this.setRotationAngle(this.cube_r2, 0.0F, 0.0F, 0.1745F);
         this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 312, -14.0F, -15.0F, -15.0F, 31, 30, 30, -12.0F, false));
         this.cube_r3 = new ModelRenderer(this);
         this.cube_r3.setRotationPoint(-15.0F, -54.5F, -0.5F);
         this.right.addChild(this.cube_r3);
         this.setRotationAngle(this.cube_r3, 0.0F, 0.0F, 0.7854F);
         this.cube_r3.cubeList.add(new ModelBox(this.cube_r3, 0, 312, -17.5F, -15.0F, -15.0F, 32, 30, 30, -13.0F, false));
         this.left = new ModelRenderer(this);
         this.left.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.HornStyle1.addChild(this.left);
         this.cube_r4 = new ModelRenderer(this);
         this.cube_r4.setRotationPoint(19.0517F, -58.4594F, -0.5F);
         this.left.addChild(this.cube_r4);
         this.setRotationAngle(this.cube_r4, 0.0F, 0.0F, -1.1781F);
         this.cube_r4.cubeList.add(new ModelBox(this.cube_r4, 0, 312, -16.0F, -15.5406F, -15.0F, 33, 30, 30, -14.0F, true));
         this.cube_r5 = new ModelRenderer(this);
         this.cube_r5.setRotationPoint(15.0F, -54.5F, -0.5F);
         this.left.addChild(this.cube_r5);
         this.setRotationAngle(this.cube_r5, 0.0F, 0.0F, -0.7854F);
         this.cube_r5.cubeList.add(new ModelBox(this.cube_r5, 0, 312, -14.5F, -15.0F, -15.0F, 32, 30, 30, -13.0F, true));
         this.cube_r6 = new ModelRenderer(this);
         this.cube_r6.setRotationPoint(13.1084F, -54.1158F, -0.5F);
         this.left.addChild(this.cube_r6);
         this.setRotationAngle(this.cube_r6, 0.0F, 0.0F, -0.1745F);
         this.cube_r6.cubeList.add(new ModelBox(this.cube_r6, 0, 312, -17.0F, -15.0F, -15.0F, 31, 30, 30, -12.0F, true));
         this.HornStyle2 = new ModelRenderer(this);
         this.HornStyle2.setRotationPoint(0.0F, 35.0F, -2.0F);
         this.bipedHead.addChild(this.HornStyle2);
         this.right5 = new ModelRenderer(this);
         this.right5.setRotationPoint(-5.8867F, -56.8719F, -5.5F);
         this.HornStyle2.addChild(this.right5);
         this.setRotationAngle(this.right5, 0.0F, -1.2217F, 0.0F);
         this.cube_r7 = new ModelRenderer(this);
         this.cube_r7.setRotationPoint(-10.165F, -4.5875F, 0.0F);
         this.right5.addChild(this.cube_r7);
         this.setRotationAngle(this.cube_r7, 0.0F, 0.0F, 1.1781F);
         this.cube_r7.cubeList.add(new ModelBox(this.cube_r7, 0, 312, -17.0F, -15.5406F, -15.0F, 33, 30, 30, -14.0F, false));
         this.cube_r8 = new ModelRenderer(this);
         this.cube_r8.setRotationPoint(-4.2217F, -0.2439F, 0.0F);
         this.right5.addChild(this.cube_r8);
         this.setRotationAngle(this.cube_r8, 0.0F, 0.0F, 0.1745F);
         this.cube_r8.cubeList.add(new ModelBox(this.cube_r8, 0, 312, -14.0F, -15.0F, -15.0F, 31, 30, 30, -12.0F, false));
         this.cube_r9 = new ModelRenderer(this);
         this.cube_r9.setRotationPoint(-6.1133F, -0.6281F, 0.0F);
         this.right5.addChild(this.cube_r9);
         this.setRotationAngle(this.cube_r9, 0.0F, 0.0F, 0.7854F);
         this.cube_r9.cubeList.add(new ModelBox(this.cube_r9, 0, 312, -17.5F, -15.0F, -15.0F, 32, 30, 30, -13.0F, false));
         this.left8 = new ModelRenderer(this);
         this.left8.setRotationPoint(5.8867F, -56.8719F, -5.5F);
         this.HornStyle2.addChild(this.left8);
         this.setRotationAngle(this.left8, 0.0F, 1.2217F, 0.0F);
         this.cube_r10 = new ModelRenderer(this);
         this.cube_r10.setRotationPoint(10.165F, -4.5875F, 0.0F);
         this.left8.addChild(this.cube_r10);
         this.setRotationAngle(this.cube_r10, 0.0F, 0.0F, -1.1781F);
         this.cube_r10.cubeList.add(new ModelBox(this.cube_r10, 0, 312, -16.0F, -15.5406F, -15.0F, 33, 30, 30, -14.0F, true));
         this.cube_r11 = new ModelRenderer(this);
         this.cube_r11.setRotationPoint(4.2217F, -0.2439F, 0.0F);
         this.left8.addChild(this.cube_r11);
         this.setRotationAngle(this.cube_r11, 0.0F, 0.0F, -0.1745F);
         this.cube_r11.cubeList.add(new ModelBox(this.cube_r11, 0, 312, -17.0F, -15.0F, -15.0F, 31, 30, 30, -12.0F, true));
         this.cube_r12 = new ModelRenderer(this);
         this.cube_r12.setRotationPoint(6.1133F, -0.6281F, 0.0F);
         this.left8.addChild(this.cube_r12);
         this.setRotationAngle(this.cube_r12, 0.0F, 0.0F, -0.7854F);
         this.cube_r12.cubeList.add(new ModelBox(this.cube_r12, 0, 312, -14.5F, -15.0F, -15.0F, 32, 30, 30, -13.0F, true));
         this.bipedHeadwear = new ModelRenderer(this);
         this.bipedHeadwear.setRotationPoint(0.0F, -8.0F, 0.0F);
         this.bipedHeadwear.cubeList.add(new ModelBox(this.bipedHeadwear, 177, 202, -9.0F, -21.0F, -11.05F, 18, 12, 0, 0.0F, false));
         this.bipedBody = new ModelRenderer(this);
         this.bipedBody.setRotationPoint(0.0F, -8.0F, 0.0F);
         this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 0, 50, -16.0F, 0.0F, -14.0F, 32, 24, 27, 0.0F, true));
         this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 0, 0, -13.0F, 19.0F, -11.0F, 26, 18, 22, 0.0F, false));
         this.bipedRightArm = new ModelRenderer(this);
         this.bipedRightArm.setRotationPoint(-17.0F, -7.0F, -1.0F);
         this.cube_r13 = new ModelRenderer(this);
         this.cube_r13.setRotationPoint(-8.25F, 33.9513F, -3.6693F);
         this.bipedRightArm.addChild(this.cube_r13);
         this.setRotationAngle(this.cube_r13, -0.6109F, 0.0F, 0.0F);
         this.cube_r13.cubeList.add(new ModelBox(this.cube_r13, 0, 115, -3.0F, -8.0F, -3.0F, 6, 16, 6, 0.0F, false));
         this.cube_r14 = new ModelRenderer(this);
         this.cube_r14.setRotationPoint(-4.0F, 3.0F, 0.0F);
         this.bipedRightArm.addChild(this.cube_r14);
         this.setRotationAngle(this.cube_r14, 0.0F, 0.0F, 0.1745F);
         this.cube_r14.cubeList.add(new ModelBox(this.cube_r14, 0, 142, -3.0F, 0.0F, -3.0F, 6, 28, 6, 0.0F, false));
         this.cube_r15 = new ModelRenderer(this);
         this.cube_r15.setRotationPoint(-3.3421F, 0.9674F, 0.0F);
         this.bipedRightArm.addChild(this.cube_r15);
         this.setRotationAngle(this.cube_r15, 0.0F, 0.0F, 1.1345F);
         this.cube_r15.cubeList.add(new ModelBox(this.cube_r15, 114, 0, -3.0F, -8.0F, -5.0F, 6, 16, 10, 0.0F, false));
         this.rightHand = new ModelRenderer(this);
         this.rightHand.setRotationPoint(-8.25F, 40.6069F, -8.3621F);
         this.bipedRightArm.addChild(this.rightHand);
         this.setRotationAngle(this.rightHand, -0.6109F, 0.0F, 0.0F);
         this.rightHand.cubeList.add(new ModelBox(this.rightHand, 2, 246, -8.0F, -3.1436F, -11.9735F, 19, 23, 21, -3.0F, false));
         this.rightFingers = new ModelRenderer(this);
         this.rightFingers.setRotationPoint(-5.0F, 16.8564F, -1.4735F);
         this.rightHand.addChild(this.rightFingers);
         this.setRotationAngle(this.rightFingers, 0.0F, 0.0F, 1.0472F);
         this.rightFingers.cubeList.add(new ModelBox(this.rightFingers, 97, 246, -3.0F, -20.0F, -10.5F, 19, 23, 21, -3.0F, false));
         this.bipedLeftArm = new ModelRenderer(this);
         this.bipedLeftArm.setRotationPoint(17.0F, -7.0F, -1.0F);
         this.cube_r16 = new ModelRenderer(this);
         this.cube_r16.setRotationPoint(8.25F, 33.9513F, -3.6693F);
         this.bipedLeftArm.addChild(this.cube_r16);
         this.setRotationAngle(this.cube_r16, -0.6109F, 0.0F, 0.0F);
         this.cube_r16.cubeList.add(new ModelBox(this.cube_r16, 0, 115, -3.0F, -8.0F, -3.0F, 6, 16, 6, 0.0F, true));
         this.cube_r17 = new ModelRenderer(this);
         this.cube_r17.setRotationPoint(4.0F, 3.0F, 0.0F);
         this.bipedLeftArm.addChild(this.cube_r17);
         this.setRotationAngle(this.cube_r17, 0.0F, 0.0F, -0.1745F);
         this.cube_r17.cubeList.add(new ModelBox(this.cube_r17, 0, 142, -3.0F, 0.0F, -3.0F, 6, 28, 6, 0.0F, true));
         this.cube_r18 = new ModelRenderer(this);
         this.cube_r18.setRotationPoint(3.3421F, 0.9674F, 0.0F);
         this.bipedLeftArm.addChild(this.cube_r18);
         this.setRotationAngle(this.cube_r18, 0.0F, 0.0F, -1.1345F);
         this.cube_r18.cubeList.add(new ModelBox(this.cube_r18, 114, 0, -3.0F, -8.0F, -5.0F, 6, 16, 10, 0.0F, true));
         this.leftHand = new ModelRenderer(this);
         this.leftHand.setRotationPoint(8.25F, 40.6069F, -8.3621F);
         this.bipedLeftArm.addChild(this.leftHand);
         this.setRotationAngle(this.leftHand, -0.6109F, 0.0F, 0.0F);
         this.leftHand.cubeList.add(new ModelBox(this.leftHand, 2, 246, -11.0F, -3.1436F, -11.9735F, 19, 23, 21, -3.0F, true));
         this.leftFingers = new ModelRenderer(this);
         this.leftFingers.setRotationPoint(5.0F, 16.8564F, -1.4735F);
         this.leftHand.addChild(this.leftFingers);
         this.leftFingers.cubeList.add(new ModelBox(this.leftFingers, 97, 246, -16.0F, -20.0F, -10.5F, 19, 23, 21, -3.0F, true));
         this.bipedRightLeg = new ModelRenderer(this);
         this.bipedRightLeg.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedLeftLeg = new ModelRenderer(this);
         this.bipedLeftLeg.setRotationPoint(0.0F, 0.0F, 0.0F);
      }

      public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         this.bipedRightLeg.showModel = false;
         this.bipedLeftLeg.showModel = false;
         this.HornStyle1.showModel = false;
         int color = 13369344;
         if (entity instanceof EntityCustom) {
            color = ((EntityCustom)entity).getFlameColor();
         }

         float red = (float)(color >> 16 & 255) / 255.0F;
         float green = (float)(color >> 8 & 255) / 255.0F;
         float blue = (float)(color & 255) / 255.0F;
         GlStateManager.enableBlend();
         if (this.renderFlame) {
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, ageInTicks * 0.01F, 0.0F);
            GlStateManager.matrixMode(5888);
            this.bipedHeadwear.showModel = false;
            this.bipedBody.showModel = false;
         }

         float alpha = 0.8F * Math.min(ageInTicks / 60.0F, 1.0F);
         GlStateManager.color(red, green, blue, alpha);
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.bipedHeadwear.render(scale);
         GlStateManager.enableLighting();
         if (this.renderFlame) {
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(5888);
            this.bipedHeadwear.showModel = true;
            this.bipedBody.showModel = true;
         }

         GlStateManager.disableBlend();
      }

      public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
         super.setRotationAngles(limbSwing * 2.0F / entityIn.height, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
         ModelRenderer var10000 = this.bipedHead;
         var10000.rotationPointY -= 8.0F;
         var10000 = this.bipedHeadwear;
         var10000.rotationPointY -= 8.0F;
         --this.bipedRightArm.rotationPointZ;
         var10000 = this.bipedRightArm;
         var10000.rotationPointX -= 12.0F;
         --this.bipedLeftArm.rotationPointZ;
         var10000 = this.bipedLeftArm;
         var10000.rotationPointX += 12.0F;
         if (this.swingProgress > 0.0F) {
            if (this.swingProgress <= 0.4F) {
               this.bipedRightArm.rotateAngleX = this.rightArmPreset[0][0] * this.swingProgress / 0.4F;
               this.bipedRightArm.rotateAngleY = this.rightArmPreset[0][1] * this.swingProgress / 0.4F;
               this.bipedRightArm.rotateAngleZ = this.rightArmPreset[0][2] * this.swingProgress / 0.4F;
            } else if (this.swingProgress <= 0.6F) {
               float t = (this.swingProgress - 0.4F) / 0.2F;
               this.bipedRightArm.rotateAngleX = this.rightArmPreset[0][0] + (this.rightArmPreset[1][0] - this.rightArmPreset[0][0]) * t;
               this.bipedRightArm.rotateAngleY = this.rightArmPreset[0][1] + (this.rightArmPreset[1][1] - this.rightArmPreset[0][1]) * t;
               this.bipedRightArm.rotateAngleZ = this.rightArmPreset[0][2] + (this.rightArmPreset[1][2] - this.rightArmPreset[0][2]) * t;
            } else {
               float t = (this.swingProgress - 0.6F) / 0.4F;
               this.bipedRightArm.rotateAngleX = this.rightArmPreset[1][0] * (1.0F - t);
               this.bipedRightArm.rotateAngleY = this.rightArmPreset[1][1] * (1.0F - t);
               this.bipedRightArm.rotateAngleZ = this.rightArmPreset[1][2] * (1.0F - t);
            }

            var10000 = this.bipedRightArm;
            var10000.rotateAngleY += this.bipedBody.rotateAngleY * 2.0F;
         }

      }

      private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }
   }

   public static class EntityCustom extends EntityMob {
      private static final DataParameter<Boolean> FULL_BODY;
      private static final DataParameter<Integer> FLAME_COLOR;
      private static final DataParameter<Float> SUSANOO_SCALE;
      private EntityLivingBase ownerEntity;
      private int ownerEntityId = -1;
      private static final float DEFAULT_HEALTH = 1000.0F;
      private static final float DEFAULT_DAMAGE = 20.0F;
      private static final float DEFAULT_SCALE = 1.0F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(2.4F, 3.6F);
         this.experienceValue = 0;
         this.isImmuneToFire = true;
         this.setNoAI(true);
         this.enablePersistence();
         this.stepHeight = 2.0F;
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(FULL_BODY, true);
         this.dataManager.register(FLAME_COLOR, 13369344);
         this.dataManager.register(SUSANOO_SCALE, 1.0F);
      }

      protected void initEntityAI() {
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)1000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)0.0F);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)20.0F);
      }

      public void setOwnerEntity(EntityLivingBase owner) {
         this.ownerEntity = owner;
         this.ownerEntityId = owner.getEntityId();
      }

      public void setSusanooHealth(float health) {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)health);
         this.setHealth(health);
      }

      public void setSusanooDamage(float damage) {
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)damage);
      }

      public void setSusanooScale(float scale) {
         this.dataManager.set(SUSANOO_SCALE, scale);
         this.setSize(2.4F * scale, 3.6F * scale);
      }

      public float getSusanooScale() {
         return (Float)this.dataManager.get(SUSANOO_SCALE);
      }

      public boolean isFullBody() {
         return (Boolean)this.dataManager.get(FULL_BODY);
      }

      public void setFullBody(boolean full) {
         this.dataManager.set(FULL_BODY, full);
         float scale = this.getSusanooScale();
         this.setSize(2.4F * scale, (full ? 3.6F : 2.4F) * scale);
      }

      public void setFlameColor(int color) {
         this.dataManager.set(FLAME_COLOR, color);
      }

      public int getFlameColor() {
         return (Integer)this.dataManager.get(FLAME_COLOR);
      }

      public double getMountedYOffset() {
         return 0.35;
      }

      public boolean shouldRiderSit() {
         return false;
      }

      public boolean shouldDismountInWater(Entity rider) {
         return false;
      }

      public boolean canBeSteered() {
         return false;
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         if (source.getTrueSource() != null && this.ownerEntity != null && source.getTrueSource().equals(this.ownerEntity)) {
            return false;
         } else {
            return source != DamageSource.FALL && source != DamageSource.DROWN && source != DamageSource.IN_WALL && source != DamageSource.CACTUS && source != DamageSource.WITHER ? super.attackEntityFrom(source, amount) : false;
         }
      }

      public boolean attackEntityAsMob(Entity target) {
         float damage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         return target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
      }

      public void applyEntityCollision(Entity entityIn) {
         if (!this.isRidingSameEntity(entityIn) && !entityIn.noClip && !entityIn.isBeingRidden()) {
            double dx = entityIn.posX - this.posX;
            double dz = entityIn.posZ - this.posZ;
            double distSq = MathHelper.absMax(dx, dz);
            if (distSq >= 0.01) {
               double dist = (double)MathHelper.sqrt(distSq);
               dx /= dist;
               dz /= dist;
               double push = (double)1.0F / dist;
               if (push > (double)1.0F) {
                  push = (double)1.0F;
               }

               dx *= push * 0.05 * (double)(1.0F - this.entityCollisionReduction);
               dz *= push * 0.05 * (double)(1.0F - this.entityCollisionReduction);
               entityIn.addVelocity(dx, (double)0.0F, dz);
            }
         }

      }

      public void onUpdate() {
         super.onUpdate();
         if (!this.world.isRemote) {
            if (this.ownerEntity != null && !this.ownerEntity.isEntityAlive()) {
               this.setDead();
               return;
            }

            if (this.ownerEntity != null && this.getPassengers().isEmpty() && this.ticksExisted > 20) {
               this.setDead();
               return;
            }

            if (this.ownerEntity == null && this.ownerEntityId > 0) {
               Entity e = this.world.getEntityByID(this.ownerEntityId);
               if (e instanceof EntityLivingBase) {
                  this.ownerEntity = (EntityLivingBase)e;
               }
            }

            if (this.ticksExisted % 30 == 0) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("block.fire.ambient")), SoundCategory.HOSTILE, 1.0F, this.rand.nextFloat() * 0.7F + 0.3F);
            }
         }

      }

      public void writeEntityToNBT(NBTTagCompound nbt) {
         super.writeEntityToNBT(nbt);
         nbt.setBoolean("fullBody", this.isFullBody());
         nbt.setInteger("flameColor", this.getFlameColor());
         nbt.setFloat("susanooScale", this.getSusanooScale());
      }

      public void readEntityFromNBT(NBTTagCompound nbt) {
         super.readEntityFromNBT(nbt);
         if (nbt.hasKey("fullBody")) {
            this.setFullBody(nbt.getBoolean("fullBody"));
         }

         if (nbt.hasKey("flameColor")) {
            this.setFlameColor(nbt.getInteger("flameColor"));
         }

         if (nbt.hasKey("susanooScale")) {
            this.setSusanooScale(nbt.getFloat("susanooScale"));
         }

      }

      static {
         FULL_BODY = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
         FLAME_COLOR = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         SUSANOO_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }
}
