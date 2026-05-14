
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
class ShrineModelHeianEraFace extends ShrineModelHeianBase {
   private final ModelRenderer cube_r1;

   ShrineModelHeianEraFace() {
      super(0.0F, 64, 16);
      this.bipedHead = new ModelRenderer(this);
      this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F, false));
      this.bipedHeadwear = new ModelRenderer(this);
      this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.bipedHeadwear.cubeList.add(new ModelBox(this.bipedHeadwear, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.025F, false));
      this.cube_r1 = new ModelRenderer(this);
      this.cube_r1.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.setRotationAngle(this.cube_r1, 0.0785F, 0.0F, 0.0F);
      this.cube_r1.cubeList.add(new ModelBox(this.cube_r1, 46, 7, -7.475F, -9.375F, -4.125F, 8, 8, 1, 0.2F, false));
      this.bipedHeadwear.addChild(this.cube_r1);
      this.bipedBody = new ModelRenderer(this);
      this.bipedRightArm = new ModelRenderer(this);
      this.bipedLeftArm = new ModelRenderer(this);
      this.bipedRightLeg = new ModelRenderer(this);
      this.bipedLeftLeg = new ModelRenderer(this);
   }

   void renderAttachedHead(float scale) {
      this.bipedHead.render(scale);
      this.bipedHeadwear.render(scale);
   }
}
