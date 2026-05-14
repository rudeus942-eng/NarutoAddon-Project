
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityBossBuddha1000 extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 9;

   public EntityBossBuddha1000(ElementsInfTsukAddon instance) {
      super(instance, 28);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "boss_buddha_1000"), 9).name("boss_buddha_1000").tracker(256, 3, false).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, BuddhaRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class BuddhaRenderer extends RenderLivingBase<EntityCustom> {
      private static final ResourceLocation BUDDHA_TEXTURE = new ResourceLocation("narutomod:textures/budha1000.png");
      private static ModelBuddha1000 cachedModel = null;

      private static ModelBuddha1000 getOrCreateModel() {
         if (cachedModel == null) {
            cachedModel = new ModelBuddha1000();
         }

         return cachedModel;
      }

      public BuddhaRenderer(RenderManager renderManager) {
         super(renderManager, getOrCreateModel(), 10.0F);
      }

      public boolean shouldRender(EntityCustom livingEntity, ICamera camera, double camX, double camY, double camZ) {
         return true;
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return BUDDHA_TEXTURE;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelBuddha1000 extends ModelBiped {
      private final ModelRenderer rightUpperArm;
      private final ModelRenderer rightForeArm;
      private final ModelRenderer leftUpperArm;
      private final ModelRenderer leftForeArm;
      private final ModelRenderer rightThigh;
      private final ModelRenderer rightCalf;
      private final ModelRenderer leftThigh;
      private final ModelRenderer leftCalf;
      private final ModelRenderer bone351;
      private final ModelRenderer bone25;
      private final ModelRenderer bone352;
      private final ModelRenderer bone353;
      private final ModelRenderer bone22;
      private final ModelRenderer bone23;
      private final ModelRenderer bone24;
      private final ModelRenderer armStand;
      private final ModelRenderer base;
      private final ModelRenderer bone5;
      private final ModelRenderer bone7;
      private final ModelRenderer bone8;
      private final ModelRenderer bone9;
      private final ModelRenderer bone10;
      private final ModelRenderer bone11;
      private final ModelRenderer bone12;
      private final ModelRenderer bone13;
      private final ModelRenderer bone14;
      private final ModelRenderer bone15;
      private final ModelRenderer bone16;
      private final ModelRenderer bone17;
      private final ModelRenderer arms1;
      private final ModelRenderer arms2;
      private final ModelRenderer arms3;
      private final ModelRenderer arms4;
      private final ModelRenderer arms5;
      private final ModelRenderer arms6;
      private final ModelRenderer arms7;
      private final ModelRenderer arms8;
      private final ModelRenderer arms9;
      private final ModelRenderer arms10;

      public ModelBuddha1000() {
         this.textureWidth = 64;
         this.textureHeight = 64;
         (this.bipedHead = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 16, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F, false));
         (this.bipedHeadwear = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         (this.bone351 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHeadwear.addChild(this.bone351);
         this.setRotationAngle(this.bone351, 0.0873F, 0.0F, 0.0F);
         this.bone351.cubeList.add(new ModelBox(this.bone351, 16, 0, -4.0F, -12.0F, -7.75F, 8, 8, 8, -2.75F, false));
         (this.bone25 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHeadwear.addChild(this.bone25);
         this.setRotationAngle(this.bone25, 0.0873F, 0.7854F, 0.0F);
         this.bone25.cubeList.add(new ModelBox(this.bone25, 16, 0, -4.0F, -12.0F, -7.75F, 8, 8, 8, -2.75F, false));
         (this.bone352 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHeadwear.addChild(this.bone352);
         this.setRotationAngle(this.bone352, 0.0873F, 1.5708F, 0.0F);
         this.bone352.cubeList.add(new ModelBox(this.bone352, 16, 0, -4.0F, -12.0F, -7.75F, 8, 8, 8, -2.75F, false));
         (this.bone353 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHeadwear.addChild(this.bone353);
         this.setRotationAngle(this.bone353, 0.0873F, 2.3562F, 0.0F);
         this.bone353.cubeList.add(new ModelBox(this.bone353, 16, 0, -4.0F, -12.0F, -7.75F, 8, 8, 8, -2.75F, false));
         (this.bone22 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHeadwear.addChild(this.bone22);
         this.setRotationAngle(this.bone22, 0.0873F, 3.1416F, 0.0F);
         this.bone22.cubeList.add(new ModelBox(this.bone22, 16, 0, -4.0F, -12.0F, -7.75F, 8, 8, 8, -2.75F, false));
         (this.bone23 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHeadwear.addChild(this.bone23);
         this.setRotationAngle(this.bone23, 0.0873F, 3.927F, 0.0F);
         this.bone23.cubeList.add(new ModelBox(this.bone23, 16, 0, -4.0F, -12.0F, -7.75F, 8, 8, 8, -2.75F, false));
         (this.bone24 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHeadwear.addChild(this.bone24);
         this.setRotationAngle(this.bone24, 0.0873F, 4.7124F, 0.0F);
         this.bone24.cubeList.add(new ModelBox(this.bone24, 16, 0, -4.0F, -12.0F, -7.75F, 8, 8, 8, -2.75F, false));
         (this.bipedBody = new ModelRenderer(this)).setRotationPoint(0.0F, 12.0F, 0.0F);
         this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 16, 16, -4.0F, -12.0F, -2.0F, 8, 12, 4, 0.0F, false));
         (this.bipedRightArm = new ModelRenderer(this)).setRotationPoint(-5.0F, 2.0F, 0.0F);
         (this.rightUpperArm = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedRightArm.addChild(this.rightUpperArm);
         this.setRotationAngle(this.rightUpperArm, -1.0472F, -0.2618F, 0.0F);
         this.rightUpperArm.cubeList.add(new ModelBox(this.rightUpperArm, 0, 36, -3.0F, -2.0F, -2.0F, 4, 8, 4, 0.0F, false));
         (this.rightForeArm = new ModelRenderer(this)).setRotationPoint(-3.0F, 6.0F, 2.0F);
         this.rightUpperArm.addChild(this.rightForeArm);
         this.setRotationAngle(this.rightForeArm, 0.0F, 0.0F, -1.0472F);
         this.rightForeArm.cubeList.add(new ModelBox(this.rightForeArm, 28, 36, 0.0F, 0.0F, -4.0F, 4, 6, 4, 0.0F, false));
         (this.bipedLeftArm = new ModelRenderer(this)).setRotationPoint(5.0F, 2.0F, 0.0F);
         (this.leftUpperArm = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedLeftArm.addChild(this.leftUpperArm);
         this.setRotationAngle(this.leftUpperArm, -1.0472F, 0.2618F, 0.0F);
         this.leftUpperArm.cubeList.add(new ModelBox(this.leftUpperArm, 0, 36, -1.0F, -2.0F, -2.0F, 4, 8, 4, 0.0F, true));
         (this.leftForeArm = new ModelRenderer(this)).setRotationPoint(3.0F, 6.0F, 2.0F);
         this.leftUpperArm.addChild(this.leftForeArm);
         this.setRotationAngle(this.leftForeArm, 0.0F, 0.0F, 1.0472F);
         this.leftForeArm.cubeList.add(new ModelBox(this.leftForeArm, 28, 36, -4.0F, 0.0F, -4.0F, 4, 6, 4, 0.0F, true));
         (this.bipedRightLeg = new ModelRenderer(this)).setRotationPoint(-1.9F, 12.0F, 0.0F);
         (this.rightThigh = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedRightLeg.addChild(this.rightThigh);
         this.setRotationAngle(this.rightThigh, -2.0944F, 0.1745F, -1.309F);
         this.rightThigh.cubeList.add(new ModelBox(this.rightThigh, 40, 28, -2.1F, 0.0F, -2.0F, 4, 6, 4, 0.0F, false));
         (this.rightCalf = new ModelRenderer(this)).setRotationPoint(-0.1F, 6.0F, -2.0F);
         this.rightThigh.addChild(this.rightCalf);
         this.setRotationAngle(this.rightCalf, 1.5272F, 0.0F, 0.0F);
         this.rightCalf.cubeList.add(new ModelBox(this.rightCalf, 40, 16, -2.0F, 0.0F, -0.2F, 4, 7, 4, 0.0F, false));
         (this.bipedLeftLeg = new ModelRenderer(this)).setRotationPoint(1.9F, 12.0F, 0.0F);
         (this.leftThigh = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedLeftLeg.addChild(this.leftThigh);
         this.setRotationAngle(this.leftThigh, -2.0944F, -0.1745F, 1.309F);
         this.leftThigh.cubeList.add(new ModelBox(this.leftThigh, 40, 28, -1.9F, 0.0F, -2.0F, 4, 6, 4, 0.0F, true));
         (this.leftCalf = new ModelRenderer(this)).setRotationPoint(0.1F, 6.0F, -2.0F);
         this.leftThigh.addChild(this.leftCalf);
         this.setRotationAngle(this.leftCalf, 1.5272F, 0.0F, 0.0F);
         this.leftCalf.cubeList.add(new ModelBox(this.leftCalf, 40, 16, -2.0F, 0.0F, -0.2F, 4, 7, 4, 0.0F, true));
         (this.armStand = new ModelRenderer(this)).setRotationPoint(0.0F, 12.0F, 0.0F);
         (this.base = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         (this.bone5 = new ModelRenderer(this)).setRotationPoint(0.0F, 4.0F, 6.0F);
         this.setRotationAngle(this.bone5, 0.1745F, 0.0F, 0.0F);
         this.bone5.cubeList.add(new ModelBox(this.bone5, 0, 0, -2.0F, -8.0F, -2.0F, 4, 12, 4, 0.0F, false));
         (this.bone7 = new ModelRenderer(this)).setRotationPoint(0.0F, 4.0F, 0.0F);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 0, 0, -8.0F, 0.0F, -8.0F, 16, 4, 16, 0.0F, false));
         (this.bone8 = new ModelRenderer(this)).setRotationPoint(0.0F, 8.0F, 0.0F);
         this.bone8.cubeList.add(new ModelBox(this.bone8, 0, 0, -6.0F, 0.0F, -6.0F, 12, 2, 12, 0.0F, false));
         (this.bone9 = new ModelRenderer(this)).setRotationPoint(4.0F, 0.0F, 4.0F);
         this.bone9.cubeList.add(new ModelBox(this.bone9, 0, 0, -1.0F, -4.0F, -1.0F, 2, 8, 2, 0.0F, false));
         (this.bone10 = new ModelRenderer(this)).setRotationPoint(-4.0F, 0.0F, 4.0F);
         this.bone10.cubeList.add(new ModelBox(this.bone10, 0, 0, -1.0F, -4.0F, -1.0F, 2, 8, 2, 0.0F, false));
         (this.bone11 = new ModelRenderer(this)).setRotationPoint(4.0F, 0.0F, -4.0F);
         this.bone11.cubeList.add(new ModelBox(this.bone11, 0, 0, -1.0F, -4.0F, -1.0F, 2, 8, 2, 0.0F, false));
         (this.bone12 = new ModelRenderer(this)).setRotationPoint(-4.0F, 0.0F, -4.0F);
         this.bone12.cubeList.add(new ModelBox(this.bone12, 0, 0, -1.0F, -4.0F, -1.0F, 2, 8, 2, 0.0F, false));
         (this.bone13 = new ModelRenderer(this)).setRotationPoint(6.0F, 2.0F, 0.0F);
         this.bone13.cubeList.add(new ModelBox(this.bone13, 0, 0, -1.0F, -6.0F, -1.0F, 2, 10, 2, 0.0F, false));
         (this.bone14 = new ModelRenderer(this)).setRotationPoint(-6.0F, 2.0F, 0.0F);
         this.bone14.cubeList.add(new ModelBox(this.bone14, 0, 0, -1.0F, -6.0F, -1.0F, 2, 10, 2, 0.0F, false));
         (this.bone15 = new ModelRenderer(this)).setRotationPoint(0.0F, 2.0F, 6.0F);
         this.bone15.cubeList.add(new ModelBox(this.bone15, 0, 0, -1.0F, -6.0F, -1.0F, 2, 10, 2, 0.0F, false));
         (this.bone16 = new ModelRenderer(this)).setRotationPoint(0.0F, 2.0F, -6.0F);
         this.bone16.cubeList.add(new ModelBox(this.bone16, 0, 0, -1.0F, -6.0F, -1.0F, 2, 10, 2, 0.0F, false));
         (this.bone17 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, 8.0F);
         this.setRotationAngle(this.bone17, 0.2618F, 0.0F, 0.0F);
         this.bone17.cubeList.add(new ModelBox(this.bone17, 0, 0, -3.0F, -8.0F, -1.0F, 6, 10, 2, 0.0F, false));
         this.arms1 = this.createNarutoArmLayer(0.0F, 0.0F, 2.0F, 28, 0.1309F, -28.0F, 2.0944F);
         this.armStand.addChild(this.arms1);
         this.arms2 = this.createNarutoArmLayer(0.0F, 0.0F, 2.0F, 32, 0.0436F, -40.0F, 2.0944F);
         this.armStand.addChild(this.arms2);
         this.arms3 = this.createNarutoArmLayer(0.0F, 0.0F, 2.0F, 40, -0.0175F, -52.0F, 2.0944F);
         this.armStand.addChild(this.arms3);
         (this.arms4 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 2.0F);
         this.setRotationAngle(this.arms4, 0.0F, 0.0F, 0.0436F);
         this.addNarutoArmsToLayer(this.arms4, 40, -0.0873F, -52.0F, 2.0944F);
         this.armStand.addChild(this.arms4);
         (this.arms5 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 2.0F);
         this.setRotationAngle(this.arms5, 0.0F, 0.0F, -0.0436F);
         this.addNarutoArmsToLayer(this.arms5, 40, -0.1745F, -52.0F, 2.0944F);
         this.armStand.addChild(this.arms5);
         (this.arms6 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 2.0F);
         this.setRotationAngle(this.arms6, 0.0F, 0.0F, 0.0873F);
         this.addNarutoArmsToLayer(this.arms6, 40, -0.2618F, -52.0F, 2.0944F);
         this.armStand.addChild(this.arms6);
         (this.arms7 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 2.0F);
         this.setRotationAngle(this.arms7, 0.0F, 0.0F, -0.0873F);
         this.addNarutoArmsToLayer(this.arms7, 40, -0.3491F, -52.0F, 2.0944F);
         this.armStand.addChild(this.arms7);
         (this.arms8 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 2.0F);
         this.setRotationAngle(this.arms8, 0.0F, 0.0F, 0.1309F);
         this.addNarutoArmsToLayer(this.arms8, 40, -0.4363F, -52.0F, 2.0944F);
         this.armStand.addChild(this.arms8);
         (this.arms9 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 2.0F);
         this.setRotationAngle(this.arms9, 0.0F, 0.0F, -0.1309F);
         this.addNarutoArmsToLayer(this.arms9, 40, -0.5236F, -52.0F, 2.0944F);
         this.armStand.addChild(this.arms9);
         (this.arms10 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 2.0F);
         this.setRotationAngle(this.arms10, 0.0F, 0.0F, 0.1745F);
         this.addNarutoArmsToLayer(this.arms10, 40, -0.6109F, -52.0F, 2.0944F);
         this.armStand.addChild(this.arms10);
      }

      private ModelRenderer createNarutoArmLayer(float x, float y, float z, int armCount, float pitchAngle, float armY, float arcSpan) {
         ModelRenderer layer = new ModelRenderer(this);
         layer.setRotationPoint(x, y, z);
         this.addNarutoArmsToLayer(layer, armCount, pitchAngle, armY, arcSpan);
         return layer;
      }

      private void addNarutoArmsToLayer(ModelRenderer layer, int armCount, float pitchAngle, float armY, float arcSpan) {
         float angleStep = arcSpan * 2.0F / (float)(armCount - 1);

         for(int i = 0; i < armCount; ++i) {
            ModelRenderer arm = new ModelRenderer(this);
            arm.setRotationPoint(0.0F, 0.0F, -3.0F);
            float zRotation = -arcSpan + angleStep * (float)i;
            this.setRotationAngle(arm, pitchAngle, 0.0F, zRotation);
            arm.cubeList.add(new ModelBox(arm, 16, 32, -2.0F, armY, 2.0F, 4, 12, 2, 0.1F, false));
            layer.addChild(arm);
         }

      }

      private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         EntityCustom entity = (EntityCustom)entityIn;
         float growth = entity.getGrowth();
         float f6 = (entity.isBuddhaSitting() ? 1.0F : 1.5F) * 20.0F * growth;
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 1.5F - f6, 0.0F);
         GlStateManager.scale(20.0F, 20.0F, 20.0F);
         super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
         if (entity.isBuddhaSitting()) {
            this.armStand.render(scale);
         }

         GlStateManager.popMatrix();
      }

      public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
         EntityCustom entity = (EntityCustom)entityIn;
         boolean sitting = entity.isBuddhaSitting();
         if (!sitting) {
            this.poseSitting(false);
         }

         super.setRotationAngles(limbSwing * 2.0F / entityIn.height, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
         if (sitting) {
            this.poseSitting(true);
         }

      }

      private void poseSitting(boolean isSitting) {
         if (isSitting) {
            this.setRotationAngle(this.bipedRightArm, 0.0F, 0.0F, 0.0F);
            this.setRotationAngle(this.bipedLeftArm, 0.0F, 0.0F, 0.0F);
            this.setRotationAngle(this.bipedRightLeg, 0.0F, 0.0F, 0.0F);
            this.setRotationAngle(this.bipedLeftLeg, 0.0F, 0.0F, 0.0F);
            this.setRotationAngle(this.rightUpperArm, -1.0472F, -0.2618F, 0.0F);
            this.setRotationAngle(this.rightForeArm, 0.0F, 0.0F, -1.0472F);
            this.setRotationAngle(this.leftUpperArm, -1.0472F, 0.2618F, 0.0F);
            this.setRotationAngle(this.leftForeArm, 0.0F, 0.0F, 1.0472F);
            this.setRotationAngle(this.rightThigh, -2.0944F, 0.1745F, -1.309F);
            this.setRotationAngle(this.rightCalf, 1.5272F, 0.0F, 0.0F);
            this.setRotationAngle(this.leftThigh, -2.0944F, -0.1745F, 1.309F);
            this.setRotationAngle(this.leftCalf, 1.5272F, 0.0F, 0.0F);
         } else {
            this.setRotationAngle(this.rightUpperArm, 0.0F, -0.5236F, 0.2618F);
            this.setRotationAngle(this.rightForeArm, -0.5236F, 0.0F, -0.0873F);
            this.setRotationAngle(this.leftUpperArm, 0.0F, 0.5236F, -0.2618F);
            this.setRotationAngle(this.leftForeArm, -0.5236F, 0.0F, 0.0873F);
            this.setRotationAngle(this.rightThigh, -0.1745F, 0.3491F, 0.0F);
            this.setRotationAngle(this.rightCalf, 0.2618F, 0.0F, 0.0F);
            this.setRotationAngle(this.leftThigh, -0.1745F, -0.3491F, 0.0F);
            this.setRotationAngle(this.leftCalf, 0.2618F, 0.0F, 0.0F);
         }

      }
   }

   public static class EntityCustom extends EntityMob {
      private static final DataParameter<Float> SCALE;
      private static final DataParameter<Boolean> SITTING;
      private static final DataParameter<Integer> TICKS_ALIVE;
      private static final int GROW_TIME = 40;
      private EntityLivingBase owner;
      private int maxLifetime;
      private int attackCooldown;
      private static final int ATTACK_COOLDOWN_TICKS = 20;
      private boolean facingLocked;
      private float lockedYaw;

      public EntityCustom(World world) {
         super(world);
         this.maxLifetime = 600;
         this.attackCooldown = 0;
         this.facingLocked = false;
         this.lockedYaw = 0.0F;
         this.setSize(4.0F, 10.0F);
         this.isImmuneToFire = true;
         this.noClip = false;
         this.enablePersistence();
      }

      public EntityCustom(World world, EntityLivingBase owner) {
         this(world);
         this.owner = owner;
         this.setBuddhaSitting(true);
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(SCALE, 1.0F);
         this.dataManager.register(SITTING, true);
         this.dataManager.register(TICKS_ALIVE, 0);
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)20000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)0.0F);
      }

      protected void initEntityAI() {
      }

      protected PathNavigate createNavigator(World worldIn) {
         PathNavigateGround nav = new PathNavigateGround(this, worldIn);
         nav.setCanSwim(false);
         return nav;
      }

      public float getGrowth() {
         int ticks = this.getTicksAlive();
         return Math.min((float)ticks / 40.0F, 1.0F);
      }

      public int getTicksAlive() {
         return (Integer)this.dataManager.get(TICKS_ALIVE);
      }

      public void setTicksAlive(int ticks) {
         this.dataManager.set(TICKS_ALIVE, ticks);
      }

      public float getBuddhaScale() {
         return (Float)this.dataManager.get(SCALE);
      }

      public void setBuddhaScale(float scale) {
         this.dataManager.set(SCALE, scale);
      }

      public boolean isBuddhaSitting() {
         return (Boolean)this.dataManager.get(SITTING);
      }

      public void setBuddhaSitting(boolean sitting) {
         this.dataManager.set(SITTING, sitting);
      }

      public void setOwner(EntityLivingBase owner) {
         this.owner = owner;
      }

      public EntityLivingBase getOwner() {
         return this.owner;
      }

      public void setMaxLifetime(int ticks) {
         this.maxLifetime = ticks;
      }

      public double getMountedYOffset() {
         float growth = this.getGrowth();
         return (this.isBuddhaSitting() ? (double)30.0F : (double)35.0F) * (double)growth + (double)0.5F;
      }

      protected boolean canFitPassenger(Entity passenger) {
         return this.getPassengers().size() < 2;
      }

      public void updatePassenger(Entity passenger) {
         if (this.isPassenger(passenger)) {
            double yOffset = this.getMountedYOffset();
            double angle = Math.toRadians((double)this.rotationYaw);
            double forwardOffset = (double)3.0F;
            double offsetX = -Math.sin(angle) * forwardOffset;
            double offsetZ = Math.cos(angle) * forwardOffset;
            passenger.setPosition(this.posX + offsetX, this.posY + yOffset, this.posZ + offsetZ);
         }

      }

      public void mountRider(Entity rider) {
         if (rider != null && !rider.isDead) {
            rider.startRiding(this, true);
         }

      }

      public void performArmAttack() {
         if (this.attackCooldown <= 0 && !this.world.isRemote) {
            this.attackCooldown = 20;
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SHULKER_SHOOT, SoundCategory.HOSTILE, 3.0F, 0.5F);
            if (this.isBuddhaSitting()) {
               this.shootArmsSpread(100, 200.0F);
            } else {
               this.shootSingleArm(500.0F);
            }

         }
      }

      public void shootArmsSpread(int count, float damage) {
         count = Math.min(count, 25);
         double yawRad = Math.toRadians((double)this.rotationYaw);
         double forwardX = -Math.sin(yawRad);
         double forwardZ = Math.cos(yawRad);
         double baseSpawnY = this.posY + (double)this.height * (double)0.5F;

         for(int i = 0; i < count; ++i) {
            float spreadAngle = (this.rand.nextFloat() - 0.5F) * (float)Math.PI * 0.83F;
            double spreadX = forwardX * Math.cos((double)spreadAngle) - forwardZ * Math.sin((double)spreadAngle);
            double spreadZ = forwardX * Math.sin((double)spreadAngle) + forwardZ * Math.cos((double)spreadAngle);
            double spawnDist = (double)2.0F + this.rand.nextDouble() * (double)3.0F;
            double armSpawnX = this.posX + spreadX * spawnDist;
            double armSpawnZ = this.posZ + spreadZ * spawnDist;
            double heightVariance = (this.rand.nextDouble() - 0.3) * (double)15.0F;
            double armSpawnY = baseSpawnY + heightVariance;
            EntityBuddhaArm.EntityCustom arm = new EntityBuddhaArm.EntityCustom(this.world, (EntityLivingBase)(this.owner != null ? this.owner : this), armSpawnX, armSpawnY, armSpawnZ, damage);
            double normalizedHeight = (heightVariance + (double)5.0F) / (double)20.0F;
            double shootY = 0.3 - normalizedHeight * 1.1 + (this.rand.nextDouble() - (double)0.5F) * 0.3;
            double shootForward = (double)1.5F + (this.rand.nextDouble() - (double)0.5F) * 0.3;
            arm.shoot(spreadX * shootForward, shootY, spreadZ * shootForward, 2.0F, 0.0F);
            this.world.spawnEntity(arm);
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)this.height * (double)0.5F, this.posZ, 1, (double)2.0F, (double)1.0F, (double)2.0F, (double)0.0F, new int[0]);
         }

      }

      public void shootSingleArm(float damage) {
         double yawRad = Math.toRadians((double)this.rotationYaw);
         double forwardX = -Math.sin(yawRad);
         double forwardZ = Math.cos(yawRad);
         double spawnX = this.posX + forwardX * (double)this.width;
         double spawnY = this.posY + (double)0.625F * (double)this.height;
         double spawnZ = this.posZ + forwardZ * (double)this.width;
         EntityBuddhaArm.EntityCustom arm = new EntityBuddhaArm.EntityCustom(this.world, (EntityLivingBase)(this.owner != null ? this.owner : this), spawnX, spawnY, spawnZ, damage);
         arm.shoot(forwardX, (double)-1.0F, forwardZ, 2.0F, 0.0F);
         arm.setGrow(false);
         this.world.spawnEntity(arm);
      }

      public void onUpdate() {
         if (this.facingLocked) {
            this.rotationYaw = this.lockedYaw;
            this.prevRotationYaw = this.lockedYaw;
            this.renderYawOffset = this.lockedYaw;
            this.rotationYawHead = this.lockedYaw;
         }

         super.onUpdate();
         if (this.facingLocked) {
            this.rotationYaw = this.lockedYaw;
            this.prevRotationYaw = this.lockedYaw;
            this.renderYawOffset = this.lockedYaw;
            this.rotationYawHead = this.lockedYaw;
            this.prevRenderYawOffset = this.lockedYaw;
            this.prevRotationYawHead = this.lockedYaw;
         } else if (this.isBeingRidden() && !this.getPassengers().isEmpty()) {
            Entity rider = (Entity)this.getPassengers().get(0);
            if (rider instanceof EntityLivingBase) {
               this.rotationYaw = rider.rotationYaw;
               this.prevRotationYaw = rider.prevRotationYaw;
               this.renderYawOffset = rider.rotationYaw;
               this.rotationYawHead = rider.rotationYaw;
            }
         }

         int ticks = this.getTicksAlive();
         this.setTicksAlive(ticks + 1);
         if (this.attackCooldown > 0) {
            --this.attackCooldown;
         }

         if (!this.world.isRemote) {
            if (this.maxLifetime > 0 && ticks >= this.maxLifetime) {
               this.despawnBuddha();
               return;
            }

            if (ticks % 80 == 0 && this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + (double)5.0F, this.posZ, 1, (double)2.0F, (double)3.0F, (double)2.0F, (double)0.0F, new int[0]);
            }
         }

      }

      public void setFacingLocked(boolean locked, float yaw) {
         this.facingLocked = locked;
         this.lockedYaw = yaw;
         if (locked) {
            this.rotationYaw = yaw;
            this.prevRotationYaw = yaw;
            this.renderYawOffset = yaw;
            this.rotationYawHead = yaw;
         }

      }

      public boolean isFacingLocked() {
         return this.facingLocked;
      }

      public float getLockedYaw() {
         return this.lockedYaw;
      }

      public void setRotationYawHead(float rotation) {
         if (this.facingLocked) {
            super.setRotationYawHead(this.lockedYaw);
         } else {
            super.setRotationYawHead(rotation);
         }

      }

      public void onLivingUpdate() {
         super.onLivingUpdate();
         if (this.facingLocked) {
            this.rotationYaw = this.lockedYaw;
            this.prevRotationYaw = this.lockedYaw;
            this.renderYawOffset = this.lockedYaw;
            this.rotationYawHead = this.lockedYaw;
         }

      }

      public void faceEntity(Entity entityIn, float maxYawIncrease, float maxPitchIncrease) {
         if (!this.facingLocked) {
            super.faceEntity(entityIn, maxYawIncrease, maxPitchIncrease);
         }

      }

      public void despawnBuddha() {
         for(Entity passenger : this.getPassengers()) {
            passenger.dismountRidingEntity();
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)20.0F, this.posZ, 5, (double)5.0F, (double)10.0F, (double)5.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)10.0F, this.posZ, 100, (double)8.0F, (double)15.0F, (double)8.0F, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 3.0F, 0.5F);
         this.setDead();
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setFloat("buddhaScale", this.getBuddhaScale());
         compound.setBoolean("sitting", this.isBuddhaSitting());
         compound.setInteger("ticksAlive", this.getTicksAlive());
         compound.setInteger("maxLifetime", this.maxLifetime);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("buddhaScale")) {
            this.setBuddhaScale(compound.getFloat("buddhaScale"));
         }

         if (compound.hasKey("sitting")) {
            this.setBuddhaSitting(compound.getBoolean("sitting"));
         }

         if (compound.hasKey("ticksAlive")) {
            this.setTicksAlive(compound.getInteger("ticksAlive"));
         }

         if (compound.hasKey("maxLifetime")) {
            this.maxLifetime = compound.getInteger("maxLifetime");
         }

      }

      protected boolean canDespawn() {
         return false;
      }

      protected Item getDropItem() {
         return null;
      }

      public boolean canBePushed() {
         return false;
      }

      public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
      }

      protected boolean canBeRidden(Entity entityIn) {
         return true;
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         return source == DamageSource.OUT_OF_WORLD ? super.attackEntityFrom(source, amount) : false;
      }

      protected boolean isValidLightLevel() {
         return true;
      }

      static {
         SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         SITTING = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
         TICKS_ALIVE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
      }
   }
}
