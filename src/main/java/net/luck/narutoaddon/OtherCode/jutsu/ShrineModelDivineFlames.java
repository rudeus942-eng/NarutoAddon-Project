
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ShrineModelDivineFlames extends ModelBase {
   private final ModelRenderer flames;
   private final ModelRenderer bone;
   private final ModelRenderer cube_r1;
   private final ModelRenderer cube_r2;
   private final ModelRenderer bone4;
   private final ModelRenderer cube_r3;
   private final ModelRenderer cube_r4;
   private final ModelRenderer bone2;
   private final ModelRenderer cube_r5;
   private final ModelRenderer cube_r6;
   private final ModelRenderer bone3;
   private final ModelRenderer cube_r7;
   private final ModelRenderer cube_r8;

   public ShrineModelDivineFlames() {
      this.textureWidth = 128;
      this.textureHeight = 128;
      this.flames = new ModelRenderer(this);
      this.flames.setRotationPoint(0.0F, 23.5F, -18.0F);
      this.bone = new ModelRenderer(this);
      this.bone.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.flames.addChild(this.bone);
      this.cube_r1 = new ModelRenderer(this);
      this.cube_r1.setRotationPoint(0.0F, 0.0F, 18.0F);
      this.bone.addChild(this.cube_r1);
      this.setRotationAngle(this.cube_r1, 0.0F, -1.5708F, 0.0F);
      this.cube_r1.cubeList.add(new ModelBox(this.cube_r1, 0, 8, -18.0F, -0.5F, -14.0F, 36, 1, 28, 0.0F, false));
      this.cube_r2 = new ModelRenderer(this);
      this.cube_r2.setRotationPoint(0.0F, 0.0F, -18.0F);
      this.bone.addChild(this.cube_r2);
      this.setRotationAngle(this.cube_r2, 0.0F, -1.5708F, 0.0F);
      this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 37, -18.0F, -0.5F, -14.0F, 36, 1, 28, 0.0F, false));
      this.bone4 = new ModelRenderer(this);
      this.bone4.setRotationPoint(0.0F, -0.95F, 0.0F);
      this.flames.addChild(this.bone4);
      this.setRotationAngle(this.bone4, 0.0F, 0.0F, -3.1416F);
      this.cube_r3 = new ModelRenderer(this);
      this.cube_r3.setRotationPoint(0.0F, 0.0F, 18.0F);
      this.bone4.addChild(this.cube_r3);
      this.setRotationAngle(this.cube_r3, 0.0F, -1.5708F, 0.0F);
      this.cube_r3.cubeList.add(new ModelBox(this.cube_r3, 0, 8, -18.0F, -0.5F, -14.0F, 36, 1, 28, 0.0F, false));
      this.cube_r4 = new ModelRenderer(this);
      this.cube_r4.setRotationPoint(0.0F, 0.0F, -18.0F);
      this.bone4.addChild(this.cube_r4);
      this.setRotationAngle(this.cube_r4, 0.0F, -1.5708F, 0.0F);
      this.cube_r4.cubeList.add(new ModelBox(this.cube_r4, 0, 37, -18.0F, -0.5F, -14.0F, 36, 1, 28, 0.0F, false));
      this.bone2 = new ModelRenderer(this);
      this.bone2.setRotationPoint(0.425F, 0.0F, 0.0F);
      this.flames.addChild(this.bone2);
      this.setRotationAngle(this.bone2, 0.0F, 0.0F, -1.5708F);
      this.cube_r5 = new ModelRenderer(this);
      this.cube_r5.setRotationPoint(0.0F, 0.0F, 18.0F);
      this.bone2.addChild(this.cube_r5);
      this.setRotationAngle(this.cube_r5, 0.0F, -1.5708F, 0.0F);
      this.cube_r5.cubeList.add(new ModelBox(this.cube_r5, 0, 8, -18.0F, -0.5F, -14.0F, 36, 1, 28, 0.0F, false));
      this.cube_r6 = new ModelRenderer(this);
      this.cube_r6.setRotationPoint(0.0F, 0.0F, -18.0F);
      this.bone2.addChild(this.cube_r6);
      this.setRotationAngle(this.cube_r6, 0.0F, -1.5708F, 0.0F);
      this.cube_r6.cubeList.add(new ModelBox(this.cube_r6, 0, 37, -18.0F, -0.5F, -14.0F, 36, 1, 28, 0.0F, false));
      this.bone3 = new ModelRenderer(this);
      this.bone3.setRotationPoint(-0.5F, 0.0F, 0.0F);
      this.flames.addChild(this.bone3);
      this.setRotationAngle(this.bone3, 0.0F, 0.0F, 1.5708F);
      this.cube_r7 = new ModelRenderer(this);
      this.cube_r7.setRotationPoint(0.0F, 0.0F, 18.0F);
      this.bone3.addChild(this.cube_r7);
      this.setRotationAngle(this.cube_r7, 0.0F, 1.5708F, 0.0F);
      this.cube_r7.cubeList.add(new ModelBox(this.cube_r7, 0, 8, -18.0F, -0.5F, -14.0F, 36, 1, 28, 0.0F, true));
      this.cube_r8 = new ModelRenderer(this);
      this.cube_r8.setRotationPoint(0.0F, 0.0F, -18.0F);
      this.bone3.addChild(this.cube_r8);
      this.setRotationAngle(this.cube_r8, 0.0F, 1.5708F, 0.0F);
      this.cube_r8.cubeList.add(new ModelBox(this.cube_r8, 0, 37, -18.0F, -0.5F, -14.0F, 36, 1, 28, 0.0F, true));
   }

   public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      this.flames.render(scale);
   }

   private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
   }
}
