
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
class ShrineModelHeianEraBody extends ShrineModelHeianBase {
   private final ModelRenderer hatLayer;
   private final ModelRenderer rightArmPose;
   private final ModelRenderer leftArmPose;
   private final ShrineModelHeianEraFace faceOverlay = new ShrineModelHeianEraFace();

   ShrineModelHeianEraBody() {
      super(0.0F, 64, 64);
      this.bipedHead = new ModelRenderer(this);
      this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F, false));
      this.bipedHeadwear = new ModelRenderer(this);
      this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.bipedHeadwear.cubeList.add(new ModelBox(this.bipedHeadwear, 32, 0, -4.0F, -9.0F, -4.0F, 8, 8, 8, 0.25F, false));
      this.hatLayer = new ModelRenderer(this);
      this.hatLayer.setRotationPoint(0.0F, -5.675F, 0.15F);
      this.setRotationAngle(this.hatLayer, -0.1134F, 0.0F, 0.0F);
      this.hatLayer.cubeList.add(new ModelBox(this.hatLayer, 32, 0, -4.0F, -4.0F, -4.0F, 8, 8, 8, 0.225F, false));
      this.bipedHeadwear.addChild(this.hatLayer);
      this.bipedBody = new ModelRenderer(this);
      this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F, false));
      this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 40, 32, -4.0F, 9.925F, -2.0F, 8, 2, 4, 0.175F, false));
      this.bipedRightArm = new ModelRenderer(this);
      this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
      this.bipedRightArm.cubeList.add(new ModelBox(this.bipedRightArm, 40, 16, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, false));
      this.rightArmPose = new ModelRenderer(this);
      this.rightArmPose.setRotationPoint(2.0F, 3.85F, 0.0F);
      this.setRotationAngle(this.rightArmPose, -1.0099F, 0.2597F, -0.0084F);
      this.rightArmPose.cubeList.add(new ModelBox(this.rightArmPose, 40, 16, -3.0F, -3.6F, -2.0F, 4, 12, 4, 0.0F, false));
      this.bipedRightArm.addChild(this.rightArmPose);
      this.bipedLeftArm = new ModelRenderer(this);
      this.bipedLeftArm.mirror = true;
      this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
      this.bipedLeftArm.cubeList.add(new ModelBox(this.bipedLeftArm, 40, 16, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F, true));
      this.leftArmPose = new ModelRenderer(this);
      this.leftArmPose.setRotationPoint(-2.0F, 3.85F, 0.0F);
      this.setRotationAngle(this.leftArmPose, -1.0099F, -0.2597F, 0.0084F);
      this.leftArmPose.cubeList.add(new ModelBox(this.leftArmPose, 40, 16, -1.0F, -3.6F, -2.0F, 4, 12, 4, 0.0F, true));
      this.bipedLeftArm.addChild(this.leftArmPose);
      this.bipedLeftLeg = new ModelRenderer(this);
      this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
      this.bipedLeftLeg.cubeList.add(new ModelBox(this.bipedLeftLeg, 16, 48, -2.1F, 0.0F, -2.0F, 4, 12, 4, 0.0F, false));
      this.bipedLeftLeg.cubeList.add(new ModelBox(this.bipedLeftLeg, 32, 48, -2.1F, 0.1F, -2.0F, 4, 12, 4, 0.15F, false));
      this.bipedRightLeg = new ModelRenderer(this);
      this.bipedRightLeg.mirror = true;
      this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
      this.bipedRightLeg.cubeList.add(new ModelBox(this.bipedRightLeg, 16, 48, -1.9F, 0.0F, -2.0F, 4, 12, 4, 0.0F, true));
      this.bipedRightLeg.cubeList.add(new ModelBox(this.bipedRightLeg, 32, 48, -1.9F, 0.1F, -2.0F, 4, 12, 4, 0.15F, true));
   }

   public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      this.restoreFullVisibility();
      super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
      this.faceOverlay.setModelAttributes(this);
      this.faceOverlay.setLivingAnimations((EntityLivingBase)entityIn, limbSwing, limbSwingAmount, ageInTicks - (float)entityIn.ticksExisted);
      Minecraft.getMinecraft().getTextureManager().bindTexture(EntityShrineHeianEraTransformation.FACE_TEXTURE);
      GlStateManager.enableBlend();
      GlStateManager.pushMatrix();
      if (!this.isChild && entityIn.isSneaking()) {
         GlStateManager.translate(0.0F, 0.2F, 0.0F);
      }

      this.bipedHead.postRender(scale);
      this.faceOverlay.renderAttachedHead(scale);
      GlStateManager.popMatrix();
      GlStateManager.disableBlend();
      Minecraft.getMinecraft().getTextureManager().bindTexture(EntityShrineHeianEraTransformation.BODY_TEXTURE);
   }

   void restoreFullVisibility() {
      this.setVisible(true);
      this.bipedHead.showModel = true;
      this.bipedHeadwear.showModel = true;
      this.bipedBody.showModel = true;
      this.bipedRightArm.showModel = true;
      this.bipedLeftArm.showModel = true;
      this.bipedRightLeg.showModel = true;
      this.bipedLeftLeg.showModel = true;
      this.rightArmPose.showModel = true;
      this.leftArmPose.showModel = true;
   }

   void renderFirstPersonArm(AbstractClientPlayer player, ModelPlayer defaultModel, EnumHandSide side, float scale) {
      this.setModelAttributes(defaultModel);
      this.setVisible(false);
      this.rightArmPose.showModel = false;
      this.leftArmPose.showModel = false;
      this.bipedRightArm.showModel = side == EnumHandSide.RIGHT;
      this.bipedLeftArm.showModel = side == EnumHandSide.LEFT;
      this.swingProgress = 0.0F;
      this.isSneak = false;
      this.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, scale, player);
      if (side == EnumHandSide.RIGHT) {
         this.bipedRightArm.rotateAngleX = 0.0F;
         this.bipedRightArm.render(scale);
      } else {
         this.bipedLeftArm.rotateAngleX = 0.0F;
         this.bipedLeftArm.render(scale);
      }

   }
}
