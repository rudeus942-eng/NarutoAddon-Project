
package net.luck.narutoaddon.OtherCode.quest.npc;

import net.luck.narutoaddon.OtherCode.entity.EntityJiraiya;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelPlayerPoseable extends ModelPlayer {
   public ModelPlayerPoseable(float modelSize, boolean smallArms) {
      super(modelSize, smallArms);
   }

   public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
      super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
      String poseName = null;
      if (entityIn instanceof QuestNpcBase) {
         poseName = ((QuestNpcBase)entityIn).getNpcPoseName();
      } else if (entityIn instanceof EntityJiraiya.EntityCustom) {
         poseName = ((EntityJiraiya.EntityCustom)entityIn).getNpcPoseName();
      }

      if (poseName != null && !poseName.isEmpty()) {
         NpcPose pose = NpcPose.fromName(poseName);
         if (pose.isCustom()) {
            this.bipedRightArm.rotateAngleX = pose.rightArmX;
            this.bipedRightArm.rotateAngleY = pose.rightArmY;
            this.bipedRightArm.rotateAngleZ = pose.rightArmZ;
            this.bipedLeftArm.rotateAngleX = pose.leftArmX;
            this.bipedLeftArm.rotateAngleY = pose.leftArmY;
            this.bipedLeftArm.rotateAngleZ = pose.leftArmZ;
            this.bipedRightLeg.rotateAngleX = pose.rightLegX;
            this.bipedRightLeg.rotateAngleY = pose.rightLegY;
            this.bipedRightLeg.rotateAngleZ = pose.rightLegZ;
            this.bipedLeftLeg.rotateAngleX = pose.leftLegX;
            this.bipedLeftLeg.rotateAngleY = pose.leftLegY;
            this.bipedLeftLeg.rotateAngleZ = pose.leftLegZ;
            this.bipedRightLegwear.rotateAngleX = pose.rightLegX;
            this.bipedRightLegwear.rotateAngleY = pose.rightLegY;
            this.bipedRightLegwear.rotateAngleZ = pose.rightLegZ;
            this.bipedLeftLegwear.rotateAngleX = pose.leftLegX;
            this.bipedLeftLegwear.rotateAngleY = pose.leftLegY;
            this.bipedLeftLegwear.rotateAngleZ = pose.leftLegZ;
            this.bipedRightArmwear.rotateAngleX = pose.rightArmX;
            this.bipedRightArmwear.rotateAngleY = pose.rightArmY;
            this.bipedRightArmwear.rotateAngleZ = pose.rightArmZ;
            this.bipedLeftArmwear.rotateAngleX = pose.leftArmX;
            this.bipedLeftArmwear.rotateAngleY = pose.leftArmY;
            this.bipedLeftArmwear.rotateAngleZ = pose.leftArmZ;
            this.bipedBody.rotateAngleX = pose.bodyX;
         }
      }
   }
}
