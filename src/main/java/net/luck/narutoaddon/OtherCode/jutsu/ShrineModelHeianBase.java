
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
abstract class ShrineModelHeianBase extends ModelBiped {
   protected ModelBiped wearerModel;

   protected ShrineModelHeianBase(float modelSize, int texWidth, int texHeight) {
      super(modelSize, 0.0F, texWidth, texHeight);
   }

   public void setModelAttributes(ModelBase model) {
      super.setModelAttributes(model);
      if (model instanceof ModelBiped) {
         this.wearerModel = (ModelBiped)model;
      }

   }

   protected void setRotationAngle(ModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }

   public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
      if (this.wearerModel != null) {
         copyModelAngles(this.wearerModel.bipedHead, this.bipedHead);
         copyModelAngles(this.wearerModel.bipedHeadwear, this.bipedHeadwear);
         copyModelAngles(this.wearerModel.bipedBody, this.bipedBody);
         copyModelAngles(this.wearerModel.bipedRightArm, this.bipedRightArm);
         copyModelAngles(this.wearerModel.bipedLeftArm, this.bipedLeftArm);
         copyModelAngles(this.wearerModel.bipedRightLeg, this.bipedRightLeg);
         copyModelAngles(this.wearerModel.bipedLeftLeg, this.bipedLeftLeg);
         this.bipedHead.rotationPointX = this.wearerModel.bipedHead.rotationPointX;
         this.bipedHead.rotationPointY = this.wearerModel.bipedHead.rotationPointY;
         this.bipedHead.rotationPointZ = this.wearerModel.bipedHead.rotationPointZ;
         this.bipedHeadwear.rotationPointX = this.wearerModel.bipedHeadwear.rotationPointX;
         this.bipedHeadwear.rotationPointY = this.wearerModel.bipedHeadwear.rotationPointY;
         this.bipedHeadwear.rotationPointZ = this.wearerModel.bipedHeadwear.rotationPointZ;
         this.bipedBody.rotationPointX = this.wearerModel.bipedBody.rotationPointX;
         this.bipedBody.rotationPointY = this.wearerModel.bipedBody.rotationPointY;
         this.bipedBody.rotationPointZ = this.wearerModel.bipedBody.rotationPointZ;
         this.bipedRightArm.rotationPointX = this.wearerModel.bipedRightArm.rotationPointX;
         this.bipedRightArm.rotationPointY = this.wearerModel.bipedRightArm.rotationPointY;
         this.bipedRightArm.rotationPointZ = this.wearerModel.bipedRightArm.rotationPointZ;
         this.bipedLeftArm.rotationPointX = this.wearerModel.bipedLeftArm.rotationPointX;
         this.bipedLeftArm.rotationPointY = this.wearerModel.bipedLeftArm.rotationPointY;
         this.bipedLeftArm.rotationPointZ = this.wearerModel.bipedLeftArm.rotationPointZ;
         this.bipedRightLeg.rotationPointX = this.wearerModel.bipedRightLeg.rotationPointX;
         this.bipedRightLeg.rotationPointY = this.wearerModel.bipedRightLeg.rotationPointY;
         this.bipedRightLeg.rotationPointZ = this.wearerModel.bipedRightLeg.rotationPointZ;
         this.bipedLeftLeg.rotationPointX = this.wearerModel.bipedLeftLeg.rotationPointX;
         this.bipedLeftLeg.rotationPointY = this.wearerModel.bipedLeftLeg.rotationPointY;
         this.bipedLeftLeg.rotationPointZ = this.wearerModel.bipedLeftLeg.rotationPointZ;
      } else {
         super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
      }

   }
}
