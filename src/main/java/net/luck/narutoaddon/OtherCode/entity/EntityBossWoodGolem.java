
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.raid.util.KnockbackHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityBossWoodGolem extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 6;

   public EntityBossWoodGolem(ElementsInfTsukAddon instance) {
      super(instance, 26);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "boss_wood_golem"), 6).name("boss_wood_golem").tracker(128, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, WoodGolemRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class WoodGolemRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation NARUTO_TEXTURE = new ResourceLocation("narutomod", "textures/woodgolem.png");
      private static final ResourceLocation LOCAL_TEXTURE = new ResourceLocation("inftsukaddon", "textures/woodgolem.png");

      public WoodGolemRenderer(RenderManager renderManager) {
         super(renderManager, new ModelWoodGolem(), 1.5F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return NARUTO_TEXTURE;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         if (entity.isBeingRidden() && !entity.getPassengers().isEmpty()) {
            Entity rider = (Entity)entity.getPassengers().get(0);
            if (rider instanceof EntityLivingBase) {
               EntityLivingBase livingRider = (EntityLivingBase)rider;
               entity.limbSwing = livingRider.limbSwing;
               entity.limbSwingAmount = livingRider.limbSwingAmount;
               entity.prevLimbSwingAmount = livingRider.prevLimbSwingAmount;
            }
         }

         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         float scale = entity.getGolemScale();
         GlStateManager.scale(scale / 8.0F, scale / 8.0F, scale / 8.0F);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelWoodGolem extends ModelBiped {
      private final ModelRenderer bone9;
      private final ModelRenderer bone2;
      private final ModelRenderer bone3;
      private final ModelRenderer bone4;
      private final ModelRenderer bone5;
      private final ModelRenderer bone6;
      private final ModelRenderer bone7;
      private final ModelRenderer bone45;
      private final ModelRenderer HatLayer_r1;
      private final ModelRenderer bone46;
      private final ModelRenderer HatLayer_r2;
      private final ModelRenderer bone44;
      private final ModelRenderer HatLayer_r3;
      private final ModelRenderer HatLayer_r4;
      private final ModelRenderer bone43;
      private final ModelRenderer HatLayer_r5;
      private final ModelRenderer HatLayer_r6;
      private final ModelRenderer bone;
      private final ModelRenderer bone8;
      private final ModelRenderer bone47;
      private final ModelRenderer bone48;
      private final ModelRenderer rightUpperArm;
      private final ModelRenderer rightForeArm;
      private final ModelRenderer leftUpperArm;
      private final ModelRenderer leftForeArm;
      private final ModelRenderer rightThigh;
      private final ModelRenderer rightCalf;
      private final ModelRenderer leftThigh;
      private final ModelRenderer leftCalf;
      private final ModelRenderer dragon;
      private final ModelRenderer bone10;
      private final ModelRenderer bone11;
      private final ModelRenderer bone12;
      private final ModelRenderer bone13;
      private final ModelRenderer bone14;
      private final ModelRenderer bone15;
      private final ModelRenderer bone16;
      private final ModelRenderer bone17;
      private final ModelRenderer bone18;
      private final ModelRenderer bone19;
      private final ModelRenderer bone20;
      private final ModelRenderer bone21;
      private final ModelRenderer dragonHead;
      private final ModelRenderer bone22;
      private final ModelRenderer bone39;
      private final ModelRenderer bone50;
      private final ModelRenderer bone49;
      private final ModelRenderer bone51;
      private final ModelRenderer dragonEyes;

      public ModelWoodGolem() {
         this.textureWidth = 64;
         this.textureHeight = 64;
         this.bipedHead = new ModelRenderer(this);
         this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 0, 16, -4.0F, -8.5F, -4.0F, 8, 8, 8, 0.0F, false));
         this.bone9 = new ModelRenderer(this);
         this.bone9.setRotationPoint(0.0F, -8.5F, -4.0F);
         this.bipedHead.addChild(this.bone9);
         this.setRotationAngle(this.bone9, -0.5236F, 0.0F, 0.0F);
         this.bone9.cubeList.add(new ModelBox(this.bone9, 46, 32, -4.0F, -5.0F, 0.0F, 8, 5, 0, 0.0F, false));
         this.bone2 = new ModelRenderer(this);
         this.bone2.setRotationPoint(0.0F, -8.5F, -1.5F);
         this.bipedHead.addChild(this.bone2);
         this.setRotationAngle(this.bone2, -0.6109F, 0.0F, 0.0F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 46, 32, -4.0F, -5.0F, 0.0F, 8, 5, 0, 0.0F, true));
         this.bone3 = new ModelRenderer(this);
         this.bone3.setRotationPoint(0.0F, -7.5F, 0.5F);
         this.bipedHead.addChild(this.bone3);
         this.setRotationAngle(this.bone3, -0.6981F, 0.0F, 0.0F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 46, 32, -4.0F, -5.0F, 0.0F, 8, 5, 0, 0.0F, false));
         this.bone4 = new ModelRenderer(this);
         this.bone4.setRotationPoint(0.0F, -6.5F, 2.0F);
         this.bipedHead.addChild(this.bone4);
         this.setRotationAngle(this.bone4, -0.7854F, 0.0F, 0.0F);
         this.bone4.cubeList.add(new ModelBox(this.bone4, 46, 32, -4.0F, -5.0F, 0.0F, 8, 5, 0, 0.0F, true));
         this.bone5 = new ModelRenderer(this);
         this.bone5.setRotationPoint(0.0F, -4.5F, 2.0F);
         this.bipedHead.addChild(this.bone5);
         this.setRotationAngle(this.bone5, -0.8727F, 0.0F, 0.0F);
         this.bone5.cubeList.add(new ModelBox(this.bone5, 46, 32, -4.0F, -5.0F, 0.0F, 8, 5, 0, 0.0F, false));
         this.bone6 = new ModelRenderer(this);
         this.bone6.setRotationPoint(0.0F, -2.75F, 2.0F);
         this.bipedHead.addChild(this.bone6);
         this.setRotationAngle(this.bone6, -0.9599F, 0.0F, 0.0F);
         this.bone6.cubeList.add(new ModelBox(this.bone6, 46, 32, -4.0F, -5.0F, 0.0F, 8, 5, 0, 0.0F, true));
         this.bone7 = new ModelRenderer(this);
         this.bone7.setRotationPoint(0.0F, -1.0F, 2.0F);
         this.bipedHead.addChild(this.bone7);
         this.setRotationAngle(this.bone7, -1.0472F, 0.0F, 0.0F);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 46, 32, -4.0F, -5.0F, 0.0F, 8, 5, 0, 0.0F, false));
         this.bone45 = new ModelRenderer(this);
         this.bone45.setRotationPoint(-3.7F, -10.7815F, -3.0149F);
         this.bipedHead.addChild(this.bone45);
         this.setRotationAngle(this.bone45, -1.0036F, 0.7854F, -1.1781F);
         this.HatLayer_r1 = new ModelRenderer(this);
         this.HatLayer_r1.setRotationPoint(-1.4091F, 0.2641F, -0.1594F);
         this.bone45.addChild(this.HatLayer_r1);
         this.setRotationAngle(this.HatLayer_r1, 0.1309F, -0.5236F, 0.0F);
         this.HatLayer_r1.cubeList.add(new ModelBox(this.HatLayer_r1, 59, 44, -0.725F, -1.775F, -0.625F, 2, 5, 0, 0.0F, false));
         this.bone46 = new ModelRenderer(this);
         this.bone46.setRotationPoint(3.7F, -10.7815F, -3.0149F);
         this.bipedHead.addChild(this.bone46);
         this.setRotationAngle(this.bone46, -1.0036F, -0.7854F, 1.1781F);
         this.HatLayer_r2 = new ModelRenderer(this);
         this.HatLayer_r2.setRotationPoint(1.4091F, 0.2641F, -0.1594F);
         this.bone46.addChild(this.HatLayer_r2);
         this.setRotationAngle(this.HatLayer_r2, 0.1309F, 0.5236F, 0.0F);
         this.HatLayer_r2.cubeList.add(new ModelBox(this.HatLayer_r2, 59, 44, -1.275F, -1.775F, -0.625F, 2, 5, 0, 0.0F, true));
         this.bone44 = new ModelRenderer(this);
         this.bone44.setRotationPoint(-4.6479F, -9.2565F, 0.5796F);
         this.bipedHead.addChild(this.bone44);
         this.setRotationAngle(this.bone44, 0.7418F, 1.309F, 0.0F);
         this.HatLayer_r3 = new ModelRenderer(this);
         this.HatLayer_r3.setRotationPoint(-0.3121F, 3.6916F, -1.7356F);
         this.bone44.addChild(this.HatLayer_r3);
         this.setRotationAngle(this.HatLayer_r3, -0.3927F, -0.3491F, -0.7854F);
         this.HatLayer_r3.cubeList.add(new ModelBox(this.HatLayer_r3, 13, 0, -4.5F, -4.5F, 0.0F, 9, 5, 0, 0.0F, false));
         this.HatLayer_r4 = new ModelRenderer(this);
         this.HatLayer_r4.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bone44.addChild(this.HatLayer_r4);
         this.setRotationAngle(this.HatLayer_r4, -0.3054F, -0.3491F, -0.7854F);
         this.HatLayer_r4.cubeList.add(new ModelBox(this.HatLayer_r4, 13, 0, -6.425F, -1.225F, -0.8F, 9, 5, 0, 0.0F, true));
         this.bone43 = new ModelRenderer(this);
         this.bone43.setRotationPoint(4.6479F, -9.2565F, 0.5796F);
         this.bipedHead.addChild(this.bone43);
         this.setRotationAngle(this.bone43, 0.7418F, -1.309F, 0.0F);
         this.HatLayer_r5 = new ModelRenderer(this);
         this.HatLayer_r5.setRotationPoint(0.3121F, 3.6916F, -1.7356F);
         this.bone43.addChild(this.HatLayer_r5);
         this.setRotationAngle(this.HatLayer_r5, -0.3927F, 0.3491F, 0.7854F);
         this.HatLayer_r5.cubeList.add(new ModelBox(this.HatLayer_r5, 13, 0, -4.5F, -4.5F, 0.0F, 9, 5, 0, 0.0F, true));
         this.HatLayer_r6 = new ModelRenderer(this);
         this.HatLayer_r6.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bone43.addChild(this.HatLayer_r6);
         this.setRotationAngle(this.HatLayer_r6, -0.3054F, 0.3491F, 0.7854F);
         this.HatLayer_r6.cubeList.add(new ModelBox(this.HatLayer_r6, 13, 0, -2.575F, -1.225F, -0.8F, 9, 5, 0, 0.0F, false));
         this.bone = new ModelRenderer(this);
         this.bone.setRotationPoint(-1.9F, -2.2F, -4.0F);
         this.bipedHead.addChild(this.bone);
         this.setRotationAngle(this.bone, 0.2618F, 0.0F, 0.0F);
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 21, -1.0F, -2.0F, 0.0F, 2, 2, 0, 0.0F, false));
         this.bone8 = new ModelRenderer(this);
         this.bone8.setRotationPoint(1.9F, -2.2F, -4.0F);
         this.bipedHead.addChild(this.bone8);
         this.setRotationAngle(this.bone8, 0.2618F, 0.0F, 0.0F);
         this.bone8.cubeList.add(new ModelBox(this.bone8, 0, 21, -1.0F, -2.0F, 0.0F, 2, 2, 0, 0.0F, true));
         this.bone47 = new ModelRenderer(this);
         this.bone47.setRotationPoint(-1.65F, -6.35F, -4.0F);
         this.bipedHead.addChild(this.bone47);
         this.setRotationAngle(this.bone47, 0.1745F, 0.0F, 0.0F);
         this.bone47.cubeList.add(new ModelBox(this.bone47, 0, 18, -2.0F, -2.0F, 0.0F, 4, 3, 0, 0.0F, false));
         this.bone48 = new ModelRenderer(this);
         this.bone48.setRotationPoint(1.65F, -6.35F, -4.0F);
         this.bipedHead.addChild(this.bone48);
         this.setRotationAngle(this.bone48, 0.1745F, 0.0F, 0.0F);
         this.bone48.cubeList.add(new ModelBox(this.bone48, 0, 18, -2.0F, -2.0F, 0.0F, 4, 3, 0, 0.0F, true));
         this.bipedHeadwear = new ModelRenderer(this);
         this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedHeadwear.cubeList.add(new ModelBox(this.bipedHeadwear, 42, 46, -4.025F, -6.925F, -4.005F, 8, 2, 0, 0.0F, false));
         this.bipedBody = new ModelRenderer(this);
         this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 0, 32, -4.0F, -0.5F, -2.0F, 8, 12, 4, 0.6F, false));
         this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 6, 6, -4.0F, 4.25F, -3.425F, 8, 7, 2, 0.0F, false));
         this.bipedRightArm = new ModelRenderer(this);
         this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
         this.rightUpperArm = new ModelRenderer(this);
         this.rightUpperArm.setRotationPoint(-1.0F, 0.0F, 0.0F);
         this.bipedRightArm.addChild(this.rightUpperArm);
         this.setRotationAngle(this.rightUpperArm, -1.0472F, -0.5236F, 0.2618F);
         this.rightUpperArm.cubeList.add(new ModelBox(this.rightUpperArm, 32, 0, -3.1121F, -2.483F, -1.9353F, 4, 8, 4, 0.0F, false));
         this.rightForeArm = new ModelRenderer(this);
         this.rightForeArm.setRotationPoint(-1.0F, 5.0F, 0.0F);
         this.rightUpperArm.addChild(this.rightForeArm);
         this.setRotationAngle(this.rightForeArm, -0.5236F, 0.0F, -0.5236F);
         this.rightForeArm.cubeList.add(new ModelBox(this.rightForeArm, 28, 12, -2.1121F, -0.4506F, -2.1854F, 4, 8, 4, 0.0F, false));
         this.bipedLeftArm = new ModelRenderer(this);
         this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
         this.leftUpperArm = new ModelRenderer(this);
         this.leftUpperArm.setRotationPoint(1.0F, 0.0F, 0.0F);
         this.bipedLeftArm.addChild(this.leftUpperArm);
         this.setRotationAngle(this.leftUpperArm, -1.0472F, 0.5236F, -0.2618F);
         this.leftUpperArm.cubeList.add(new ModelBox(this.leftUpperArm, 32, 0, -0.8879F, -2.483F, -1.9353F, 4, 8, 4, 0.0F, true));
         this.leftForeArm = new ModelRenderer(this);
         this.leftForeArm.setRotationPoint(1.0F, 5.0F, 0.0F);
         this.leftUpperArm.addChild(this.leftForeArm);
         this.setRotationAngle(this.leftForeArm, -0.5236F, 0.0F, 0.5236F);
         this.leftForeArm.cubeList.add(new ModelBox(this.leftForeArm, 28, 12, -1.8879F, -0.4506F, -2.1854F, 4, 8, 4, 0.0F, true));
         this.bipedRightLeg = new ModelRenderer(this);
         this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
         this.rightThigh = new ModelRenderer(this);
         this.rightThigh.setRotationPoint(-0.1F, -1.0F, 0.0F);
         this.bipedRightLeg.addChild(this.rightThigh);
         this.setRotationAngle(this.rightThigh, -2.0944F, 0.2618F, -1.4835F);
         this.rightThigh.cubeList.add(new ModelBox(this.rightThigh, 20, 44, -1.9F, 0.5076F, -2.0868F, 4, 6, 4, 0.4F, false));
         this.rightCalf = new ModelRenderer(this);
         this.rightCalf.setRotationPoint(0.0F, 7.0F, -2.2F);
         this.rightThigh.addChild(this.rightCalf);
         this.setRotationAngle(this.rightCalf, 1.309F, 0.0F, 0.0F);
         this.rightCalf.cubeList.add(new ModelBox(this.rightCalf, 44, 8, -1.9F, -0.4981F, 0.0436F, 4, 7, 4, 0.2F, false));
         this.bipedLeftLeg = new ModelRenderer(this);
         this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
         this.leftThigh = new ModelRenderer(this);
         this.leftThigh.setRotationPoint(0.1F, -1.0F, 0.0F);
         this.bipedLeftLeg.addChild(this.leftThigh);
         this.setRotationAngle(this.leftThigh, -2.0944F, -0.2618F, 1.4835F);
         this.leftThigh.cubeList.add(new ModelBox(this.leftThigh, 20, 44, -2.1F, 0.5076F, -2.0868F, 4, 6, 4, 0.4F, true));
         this.leftCalf = new ModelRenderer(this);
         this.leftCalf.setRotationPoint(0.0F, 7.0F, -2.2F);
         this.leftThigh.addChild(this.leftCalf);
         this.setRotationAngle(this.leftCalf, 1.309F, 0.0F, 0.0F);
         this.leftCalf.cubeList.add(new ModelBox(this.leftCalf, 44, 8, -2.1F, -0.4981F, 0.0436F, 4, 7, 4, 0.2F, true));
         this.dragon = new ModelRenderer(this);
         this.dragon.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.dragon.cubeList.add(new ModelBox(this.dragon, 44, 19, -4.0F, 15.5F, 3.0F, 4, 4, 4, 0.0F, false));
         this.dragon.cubeList.add(new ModelBox(this.dragon, 44, 19, -4.0F, 17.875F, 3.0F, 4, 4, 4, -0.5F, false));
         this.dragon.cubeList.add(new ModelBox(this.dragon, 44, 19, -4.0F, 19.875F, 3.0F, 4, 4, 4, -1.0F, false));
         this.dragon.cubeList.add(new ModelBox(this.dragon, 44, 19, -4.05F, 21.3F, 3.0F, 4, 4, 4, -1.2F, false));
         this.bone10 = new ModelRenderer(this);
         this.bone10.setRotationPoint(-2.0F, 15.0F, 5.0F);
         this.dragon.addChild(this.bone10);
         this.setRotationAngle(this.bone10, 0.4363F, 0.0F, -0.4363F);
         this.bone10.cubeList.add(new ModelBox(this.bone10, 44, 19, -1.8706F, -5.4665F, -1.875F, 4, 8, 4, 0.0F, false));
         this.bone11 = new ModelRenderer(this);
         this.bone11.setRotationPoint(0.0F, -6.0F, 0.0F);
         this.bone10.addChild(this.bone11);
         this.setRotationAngle(this.bone11, 0.6981F, 0.0F, -0.4363F);
         this.bone11.cubeList.add(new ModelBox(this.bone11, 44, 19, -1.7543F, -5.3706F, -1.7713F, 4, 8, 4, 0.0F, false));
         this.bone12 = new ModelRenderer(this);
         this.bone12.setRotationPoint(0.0F, -6.0F, 0.0F);
         this.bone11.addChild(this.bone12);
         this.setRotationAngle(this.bone12, 0.4363F, 0.0F, 1.0472F);
         this.bone12.cubeList.add(new ModelBox(this.bone12, 44, 19, -1.6667F, -5.2251F, -1.7029F, 4, 8, 4, 0.0F, false));
         this.bone13 = new ModelRenderer(this);
         this.bone13.setRotationPoint(0.0F, -6.0F, 0.0F);
         this.bone12.addChild(this.bone13);
         this.setRotationAngle(this.bone13, 0.2618F, 0.0F, 0.8727F);
         this.bone13.cubeList.add(new ModelBox(this.bone13, 44, 19, -1.6667F, -5.1555F, -1.6612F, 4, 8, 4, 0.0F, false));
         this.bone14 = new ModelRenderer(this);
         this.bone14.setRotationPoint(0.0F, -6.0F, 0.0F);
         this.bone13.addChild(this.bone14);
         this.setRotationAngle(this.bone14, -0.2618F, 0.0F, 0.2618F);
         this.bone14.cubeList.add(new ModelBox(this.bone14, 44, 19, -1.7183F, -5.1407F, -1.6116F, 4, 8, 4, 0.0F, false));
         this.bone15 = new ModelRenderer(this);
         this.bone15.setRotationPoint(0.0F, -6.0F, 0.0F);
         this.bone14.addChild(this.bone15);
         this.setRotationAngle(this.bone15, -0.5236F, 0.0F, 0.6109F);
         this.bone15.cubeList.add(new ModelBox(this.bone15, 44, 19, -1.7834F, -5.2285F, -1.6116F, 4, 8, 4, 0.0F, false));
         this.bone16 = new ModelRenderer(this);
         this.bone16.setRotationPoint(0.0F, -6.0F, 0.0F);
         this.bone15.addChild(this.bone16);
         this.setRotationAngle(this.bone16, 0.0F, 0.0F, 0.5236F);
         this.bone16.cubeList.add(new ModelBox(this.bone16, 44, 19, -1.8747F, -5.2888F, -1.6116F, 4, 8, 4, 0.0F, false));
         this.bone17 = new ModelRenderer(this);
         this.bone17.setRotationPoint(0.0F, -6.0F, 0.0F);
         this.bone16.addChild(this.bone17);
         this.setRotationAngle(this.bone17, -0.4363F, 0.2618F, 0.5236F);
         this.bone17.cubeList.add(new ModelBox(this.bone17, 44, 19, -1.981F, -5.3143F, -1.6116F, 4, 8, 4, 0.0F, false));
         this.bone18 = new ModelRenderer(this);
         this.bone18.setRotationPoint(0.0F, -6.0F, 0.0F);
         this.bone17.addChild(this.bone18);
         this.setRotationAngle(this.bone18, -0.3491F, 0.0F, 0.5236F);
         this.bone18.cubeList.add(new ModelBox(this.bone18, 44, 19, -2.0896F, -5.191F, -1.5467F, 4, 8, 4, 0.0F, false));
         this.bone19 = new ModelRenderer(this);
         this.bone19.setRotationPoint(0.0F, -6.0F, 0.0F);
         this.bone18.addChild(this.bone19);
         this.setRotationAngle(this.bone19, -0.3491F, 0.0F, 0.5236F);
         this.bone19.cubeList.add(new ModelBox(this.bone19, 44, 19, -2.136F, -5.1613F, -1.5467F, 4, 8, 4, 0.0F, false));
         this.bone20 = new ModelRenderer(this);
         this.bone20.setRotationPoint(0.0F, -6.0F, 0.0F);
         this.bone19.addChild(this.bone20);
         this.setRotationAngle(this.bone20, -0.7854F, 0.0F, 0.0F);
         this.bone20.cubeList.add(new ModelBox(this.bone20, 44, 19, -2.136F, -5.1613F, -1.5467F, 4, 8, 4, 0.0F, false));
         this.bone21 = new ModelRenderer(this);
         this.bone21.setRotationPoint(0.0F, -6.0F, 0.0F);
         this.bone20.addChild(this.bone21);
         this.setRotationAngle(this.bone21, -0.5236F, 0.2618F, 0.2618F);
         this.bone21.cubeList.add(new ModelBox(this.bone21, 44, 19, -2.136F, -5.1613F, -1.5467F, 4, 8, 4, 0.0F, false));
         this.dragonHead = new ModelRenderer(this);
         this.dragonHead.setRotationPoint(-0.1133F, -4.793F, 0.4301F);
         this.bone21.addChild(this.dragonHead);
         this.setRotationAngle(this.dragonHead, 0.0F, 0.3491F, 0.3491F);
         this.dragonHead.cubeList.add(new ModelBox(this.dragonHead, 48, 0, -2.0F, -3.5F, -2.0F, 4, 4, 4, -0.2F, false));
         this.dragonHead.cubeList.add(new ModelBox(this.dragonHead, 16, 56, -2.0F, -9.5F, -2.0F, 4, 4, 4, -0.8F, false));
         this.dragonHead.cubeList.add(new ModelBox(this.dragonHead, 0, 56, -2.0F, -7.5F, -2.0F, 4, 4, 4, -0.6F, false));
         this.dragonHead.cubeList.add(new ModelBox(this.dragonHead, 0, 48, -2.0F, -5.5F, -2.0F, 4, 4, 4, -0.4F, false));
         this.dragonHead.cubeList.add(new ModelBox(this.dragonHead, 0, 9, 0.9733F, -8.9052F, -0.5101F, 1, 1, 1, -0.2F, false));
         this.bone22 = new ModelRenderer(this);
         this.bone22.setRotationPoint(0.4058F, -3.1763F, -0.0092F);
         this.dragonHead.addChild(this.bone22);
         this.bone22.cubeList.add(new ModelBox(this.bone22, 28, 28, -2.4058F, -2.3237F, -1.9908F, 4, 4, 4, -0.4F, false));
         this.bone22.cubeList.add(new ModelBox(this.bone22, 32, 56, -2.4058F, -4.3237F, -1.9908F, 4, 4, 4, -0.6F, false));
         this.bone22.cubeList.add(new ModelBox(this.bone22, 48, 56, -2.4058F, -6.3237F, -1.9908F, 4, 4, 4, -0.8F, false));
         this.bone39 = new ModelRenderer(this);
         this.bone39.setRotationPoint(-1.3656F, -2.0827F, -1.0801F);
         this.dragonHead.addChild(this.bone39);
         this.setRotationAngle(this.bone39, 0.1745F, 0.0F, -2.0944F);
         this.bone39.cubeList.add(new ModelBox(this.bone39, 0, 0, -0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F, false));
         this.bone50 = new ModelRenderer(this);
         this.bone50.setRotationPoint(-1.3656F, -2.1827F, 1.1199F);
         this.dragonHead.addChild(this.bone50);
         this.setRotationAngle(this.bone50, -0.1745F, 0.0F, -2.0944F);
         this.bone50.cubeList.add(new ModelBox(this.bone50, 0, 0, -0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F, false));
         this.bone49 = new ModelRenderer(this);
         this.bone49.setRotationPoint(-1.0472F, -1.0806F, -0.8266F);
         this.dragonHead.addChild(this.bone49);
         this.setRotationAngle(this.bone49, 0.5236F, 0.0F, -2.5307F);
         this.bone49.cubeList.add(new ModelBox(this.bone49, 0, 4, -0.5F, -3.0F, -0.5F, 1, 3, 1, 0.0F, false));
         this.bone51 = new ModelRenderer(this);
         this.bone51.setRotationPoint(-1.0472F, -1.0806F, 0.8734F);
         this.dragonHead.addChild(this.bone51);
         this.setRotationAngle(this.bone51, -0.5236F, 0.0F, -2.5307F);
         this.bone51.cubeList.add(new ModelBox(this.bone51, 0, 4, -0.5F, -3.0F, -0.5F, 1, 3, 1, 0.0F, false));
         this.dragonEyes = new ModelRenderer(this);
         this.dragonEyes.setRotationPoint(2.3133F, -5.707F, -8.3301F);
         this.dragonHead.addChild(this.dragonEyes);
         this.dragonEyes.cubeList.add(new ModelBox(this.dragonEyes, 43, 52, -4.0133F, 0.407F, 6.7F, 2, 2, 0, 0.0F, false));
         this.dragonEyes.cubeList.add(new ModelBox(this.dragonEyes, 41, 52, -4.0133F, 0.407F, 9.95F, 2, 2, 0, 0.0F, true));
      }

      private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, -10.5F, 0.0F);
         GlStateManager.scale(8.0F, 8.0F, 8.0F);
         super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
         this.dragon.render(scale);
         GlStateManager.popMatrix();
      }

      public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
         float adjustedLimbSwing = limbSwing * 2.0F / entityIn.height;
         float actualHeadYaw = netHeadYaw;
         float actualHeadPitch = headPitch;
         if (entityIn instanceof EntityCustom) {
            EntityCustom golem = (EntityCustom)entityIn;
            if (golem.isBeingRidden() && !golem.getPassengers().isEmpty()) {
               Entity rider = (Entity)golem.getPassengers().get(0);
               if (rider instanceof EntityLivingBase) {
                  EntityLivingBase livingRider = (EntityLivingBase)rider;
                  actualHeadYaw = MathHelper.wrapDegrees(livingRider.rotationYawHead - golem.renderYawOffset);
                  actualHeadPitch = livingRider.rotationPitch;
               }
            }
         }

         super.setRotationAngles(adjustedLimbSwing, limbSwingAmount, ageInTicks, actualHeadYaw, actualHeadPitch, scaleFactor, entityIn);
         this.poseSitting(false);
         this.dragonHead.rotateAngleX = actualHeadYaw * ((float)Math.PI / 180F);
         this.dragonHead.rotateAngleZ = 0.3491F + actualHeadPitch * ((float)Math.PI / 180F);
      }

      private void poseSitting(boolean isSitting) {
         if (isSitting) {
            this.setRotationAngle(this.bipedRightArm, 0.0F, 0.0F, 0.0F);
            this.setRotationAngle(this.rightUpperArm, -1.0472F, -0.5236F, 0.2618F);
            this.setRotationAngle(this.rightForeArm, -0.5236F, 0.0F, -0.5236F);
            this.setRotationAngle(this.bipedLeftArm, 0.0F, 0.0F, 0.0F);
            this.setRotationAngle(this.leftUpperArm, -1.0472F, 0.5236F, -0.2618F);
            this.setRotationAngle(this.leftForeArm, -0.5236F, 0.0F, 0.5236F);
            this.setRotationAngle(this.rightThigh, -2.0944F, 0.2618F, -1.4835F);
            this.setRotationAngle(this.rightCalf, 1.309F, 0.0F, 0.0F);
            this.setRotationAngle(this.leftThigh, -2.0944F, -0.2618F, 1.4835F);
            this.setRotationAngle(this.leftCalf, 1.309F, 0.0F, 0.0F);
         } else {
            this.setRotationAngle(this.rightUpperArm, 0.0F, -0.5236F, 0.2618F);
            this.setRotationAngle(this.rightForeArm, -0.5236F, 0.0F, 0.0F);
            this.setRotationAngle(this.leftUpperArm, 0.0F, 0.5236F, -0.2618F);
            this.setRotationAngle(this.leftForeArm, -0.5236F, 0.0F, 0.0F);
            this.setRotationAngle(this.rightThigh, -0.1745F, 0.3491F, 0.0F);
            this.setRotationAngle(this.rightCalf, 0.2618F, 0.0F, 0.0F);
            this.setRotationAngle(this.leftThigh, -0.1745F, -0.3491F, 0.0F);
            this.setRotationAngle(this.leftCalf, 0.2618F, 0.0F, 0.0F);
         }

      }

      public void setModelAttributes(ModelBase model) {
         super.setModelAttributes(model);
         this.dragon.showModel = true;
      }
   }

   public static class EntityCustom extends EntityMob {
      private static final DataParameter<String> OWNER_UUID;
      private static final DataParameter<Float> GOLEM_SCALE;
      private static final float DEFAULT_SCALE = 3.0F;
      private static final float BASE_HEALTH = 1500.0F;
      private static final float BASE_DAMAGE = 25.0F;
      private EntityLivingBase owner;
      private int lifetimeTicks = 0;
      private int maxLifetimeTicks = -1;
      private int groundSlamCooldown = 0;
      private int woodSpikeCooldown = 0;
      private int swipeCooldown = 0;
      private int rageCooldown = 0;
      private static final int GROUND_SLAM_COOLDOWN = 200;
      private static final int WOOD_SPIKE_COOLDOWN = 80;
      private static final int SWIPE_COOLDOWN = 60;
      private static final int RAGE_COOLDOWN = 400;
      private static final float GROUND_SLAM_DAMAGE = 15.0F;
      private static final float GROUND_SLAM_RADIUS = 6.0F;
      private static final float WOOD_SPIKE_DAMAGE = 40.0F;
      private static final float SWIPE_DAMAGE = 12.0F;
      private static final float SWIPE_RADIUS = 4.0F;
      private boolean rageMode = false;
      private int rageTicks = 0;
      private static final int RAGE_DURATION = 200;
      private boolean facingLocked = false;
      private float lockedYaw = 0.0F;
      private double spawnCenterX;
      private double spawnCenterY;
      private double spawnCenterZ;
      private boolean spawnCenterSet = false;
      private static final double MOVEMENT_RADIUS = (double)20.0F;
      private boolean isDashing = false;
      private double dashTargetX;
      private double dashTargetY;
      private double dashTargetZ;
      private int dashTicksRemaining = 0;
      private int dashCooldown = 0;
      private static final int DASH_COOLDOWN_MIN = 15;
      private static final int DASH_COOLDOWN_MAX = 40;
      private static final double DASH_SPEED = (double)1.0F;
      private static final double DASH_MIN_DISTANCE = (double)6.0F;
      private static final double DASH_MAX_DISTANCE = (double)18.0F;
      private DashAttackType currentDashAttack;
      private EntityLivingBase dashTarget;
      private int dashAttackCooldown;
      private static final int DASH_ATTACK_COOLDOWN = 80;
      private List<BlockPos> placedSpikeBlocks;
      private static final int SPIKE_HEIGHT = 4;
      private static final int SPIKE_LIFETIME = 100;
      private Map<BlockPos, Integer> spikeTimers;
      private static final int SPIKE_WARNING_TICKS = 15;
      private static final int SPIKES_PER_PLAYER = 3;
      private static final double SPIKE_SPAWN_RADIUS = (double)10.0F;
      private static final double SPIKE_DAMAGE_RADIUS = (double)3.0F;
      private List<PendingSpike> pendingSpikes;

      public EntityCustom(World world) {
         super(world);
         this.currentDashAttack = DashAttackType.NONE;
         this.dashTarget = null;
         this.dashAttackCooldown = 0;
         this.placedSpikeBlocks = new ArrayList();
         this.spikeTimers = new HashMap();
         this.pendingSpikes = new ArrayList();
         this.setSize(4.2F, 8.1F);
         this.experienceValue = 0;
         this.isImmuneToFire = true;
         this.setNoAI(true);
         this.enablePersistence();
         this.stepHeight = 2.0F;
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(OWNER_UUID, "");
         this.dataManager.register(GOLEM_SCALE, 3.0F);
      }

      protected void initEntityAI() {
         this.tasks.addTask(0, new EntityAISwimming(this));
      }

      public void setSpawnCenter(double x, double y, double z) {
         this.spawnCenterX = x;
         this.spawnCenterY = y;
         this.spawnCenterZ = z;
         this.spawnCenterSet = true;
      }

      private void ensureSpawnCenterSet() {
         if (!this.spawnCenterSet) {
            this.spawnCenterX = this.posX;
            this.spawnCenterY = this.posY;
            this.spawnCenterZ = this.posZ;
            this.spawnCenterSet = true;
         }

      }

      public int getMaxFallHeight() {
         return 5;
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)1500.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.25F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)32.0F);
         if (this.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         }

         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)25.0F);
      }

      public void onUpdate() {
         boolean isRidden = this.isBeingRidden();
         super.onUpdate();
         this.ensureSpawnCenterSet();
         if (this.facingLocked) {
            this.rotationYaw = this.lockedYaw;
            this.prevRotationYaw = this.lockedYaw;
            this.renderYawOffset = this.lockedYaw;
            this.rotationYawHead = this.lockedYaw;
         }

         if (!this.world.isRemote) {
            ++this.lifetimeTicks;
            if (this.groundSlamCooldown > 0) {
               --this.groundSlamCooldown;
            }

            if (this.woodSpikeCooldown > 0) {
               --this.woodSpikeCooldown;
            }

            if (this.swipeCooldown > 0) {
               --this.swipeCooldown;
            }

            if (this.rageCooldown > 0) {
               --this.rageCooldown;
            }

            if (this.dashCooldown > 0) {
               --this.dashCooldown;
            }

            if (this.dashAttackCooldown > 0) {
               --this.dashAttackCooldown;
            }

            if (this.rageMode) {
               ++this.rageTicks;
               if (this.rageTicks >= 200) {
                  this.endRageMode();
               }
            }

            if (this.maxLifetimeTicks > 0 && this.lifetimeTicks >= this.maxLifetimeTicks) {
               if (this.world instanceof WorldServer) {
                  WorldServer worldServer = (WorldServer)this.world;
                  worldServer.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.5F, this.posZ, 10, (double)1.5F, (double)2.0F, (double)1.5F, 0.05, new int[0]);
               }

               this.setDead();
               return;
            }

            this.updateSpikeTimers();
            this.updatePendingSpikes();
            this.updateDashMovement();
            EntityPlayer nearestPlayer = this.world.getClosestPlayerToEntity(this, (double)32.0F);
            if (nearestPlayer != null && !this.isOwner(nearestPlayer)) {
               double distance = (double)this.getDistance(nearestPlayer);
               this.performCombatAI(nearestPlayer, distance);
               if (!this.isDashing && !this.facingLocked) {
                  this.faceEntity(nearestPlayer);
               }
            }

            if (!isRidden && this.lifetimeTicks % 60 == 0 && this.world instanceof WorldServer) {
               WorldServer worldServer = (WorldServer)this.world;
               worldServer.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + (double)2.5F, this.posZ, 1, (double)0.5F, (double)1.0F, (double)0.5F, (double)0.0F, new int[0]);
               if (this.rageMode) {
                  worldServer.spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)3.0F, this.posZ, 3, (double)1.0F, (double)1.5F, (double)1.0F, 0.01, new int[0]);
               }
            }
         }

      }

      private void updateDashMovement() {
         if (this.lifetimeTicks % 100 == 0) {
            System.out.println("[WoodGolem] updateDashMovement - isDashing=" + this.isDashing + ", dashCooldown=" + this.dashCooldown + ", isRidden=" + this.isBeingRidden() + ", pos=" + String.format("%.1f, %.1f", this.posX, this.posZ));
         }

         if (this.isDashing) {
            --this.dashTicksRemaining;
            double dx = this.dashTargetX - this.posX;
            double dz = this.dashTargetZ - this.posZ;
            double distToTarget = Math.sqrt(dx * dx + dz * dz);
            if (this.currentDashAttack == DashAttackType.CHARGE_SLAM && this.dashTarget != null && !this.dashTarget.isDead) {
               this.dashTargetX = this.dashTarget.posX;
               this.dashTargetZ = this.dashTarget.posZ;
               dx = this.dashTargetX - this.posX;
               dz = this.dashTargetZ - this.posZ;
               distToTarget = Math.sqrt(dx * dx + dz * dz);
            }

            if (!(distToTarget < (double)2.5F) && this.dashTicksRemaining > 0) {
               double speed = this.rageMode ? 1.3 : (double)1.0F;
               if (this.currentDashAttack == DashAttackType.CHARGE_SLAM) {
                  speed *= (double)1.5F;
               }

               this.motionX = dx / distToTarget * speed;
               this.motionZ = dz / distToTarget * speed;
               float targetYaw = (float)(Math.atan2(-dx, dz) * (180D / Math.PI));
               this.rotationYaw = targetYaw;
               this.renderYawOffset = targetYaw;
               if (this.collidedHorizontally && this.onGround) {
                  this.motionY = (double)0.5F;
               }

               if (this.currentDashAttack == DashAttackType.LEAP_SLAM && distToTarget < (double)8.0F && this.onGround) {
                  this.motionY = 0.8;
                  this.motionX *= 1.3;
                  this.motionZ *= 1.3;
               }

               if (this.world instanceof WorldServer && this.lifetimeTicks % 2 == 0) {
                  WorldServer ws = (WorldServer)this.world;
                  int particleCount = this.currentDashAttack != DashAttackType.NONE ? 8 : 3;
                  ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, particleCount, (double)0.5F, 0.2, (double)0.5F, 0.03, new int[0]);
                  if (this.currentDashAttack == DashAttackType.CHARGE_SLAM) {
                     ws.spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)1.5F, this.posZ, 2, 0.3, 0.3, 0.3, 0.02, new int[0]);
                  }
               }
            } else {
               this.endDash();
            }
         } else {
            if (this.dashCooldown <= 0) {
               this.startNewDash();
            }

            if (this.woodSpikeCooldown <= 0) {
               EntityPlayer spikeTarget = this.world.getClosestPlayerToEntity(this, (double)32.0F);
               if (spikeTarget != null && !this.isOwner(spikeTarget)) {
                  this.performWoodSpike(spikeTarget);
               }
            }

            this.motionX *= (double)0.5F;
            this.motionZ *= (double)0.5F;
         }

      }

      private void startNewDash() {
         this.currentDashAttack = DashAttackType.NONE;
         this.dashTarget = null;
         EntityPlayer nearestPlayer = this.world.getClosestPlayerToEntity(this, (double)40.0F);
         if (nearestPlayer != null && !this.isOwner(nearestPlayer)) {
            double distToPlayer = (double)this.getDistance(nearestPlayer);
            boolean canSpecialAttack = this.dashAttackCooldown <= 0;
            float attackRoll = this.rand.nextFloat();
            if (canSpecialAttack) {
               if (distToPlayer > (double)10.0F && attackRoll < 0.4F) {
                  this.startChargeAttack(nearestPlayer);
                  return;
               }

               if (attackRoll < 0.7F) {
                  this.startLeapSlamAttack(nearestPlayer);
                  return;
               }
            }

            this.dashTargetX = nearestPlayer.posX;
            this.dashTargetY = this.posY;
            this.dashTargetZ = nearestPlayer.posZ;
            if (this.spawnCenterSet) {
               double offsetX = this.dashTargetX - this.spawnCenterX;
               double offsetZ = this.dashTargetZ - this.spawnCenterZ;
               double distFromCenter = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
               if (distFromCenter > (double)20.0F) {
                  double scale = (double)20.0F / distFromCenter;
                  this.dashTargetX = this.spawnCenterX + offsetX * scale * 0.95;
                  this.dashTargetZ = this.spawnCenterZ + offsetZ * scale * 0.95;
               }
            }

            double actualDist = Math.sqrt(Math.pow(this.dashTargetX - this.posX, (double)2.0F) + Math.pow(this.dashTargetZ - this.posZ, (double)2.0F));
            if (actualDist < (double)3.0F) {
               double angle = Math.atan2(nearestPlayer.posX - this.posX, nearestPlayer.posZ - this.posZ);
               this.dashTargetX = nearestPlayer.posX + Math.sin(angle) * (double)5.0F;
               this.dashTargetZ = nearestPlayer.posZ + Math.cos(angle) * (double)5.0F;
               actualDist = (double)8.0F;
            }

            this.dashTicksRemaining = (int)(actualDist / (double)1.0F) + 10;
            this.isDashing = true;
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_IRONGOLEM_STEP, SoundCategory.HOSTILE, 1.5F, 0.6F);
            System.out.println("[WoodGolem] Starting dash toward player at " + nearestPlayer.posX + ", " + nearestPlayer.posZ + " (dist=" + String.format("%.1f", actualDist) + ", attack=" + this.currentDashAttack + ")");
         } else {
            this.dashCooldown = 20;
         }
      }

      private void startChargeAttack(EntityLivingBase target) {
         this.currentDashAttack = DashAttackType.CHARGE_SLAM;
         this.dashTarget = target;
         this.dashAttackCooldown = this.rageMode ? 40 : 80;
         this.dashTargetX = target.posX;
         this.dashTargetY = this.posY;
         this.dashTargetZ = target.posZ;
         double dist = (double)this.getDistance(target);
         this.dashTicksRemaining = (int)(dist / (double)1.5F) + 20;
         this.isDashing = true;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_IRONGOLEM_ATTACK, SoundCategory.HOSTILE, 2.0F, 0.4F);
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)2.0F, this.posZ, 15, (double)1.0F, (double)1.0F, (double)1.0F, 0.1, new int[0]);
         }

      }

      private void startLeapSlamAttack(EntityLivingBase target) {
         this.currentDashAttack = DashAttackType.LEAP_SLAM;
         this.dashTarget = target;
         this.dashAttackCooldown = this.rageMode ? 40 : 80;
         this.dashTargetX = target.posX;
         this.dashTargetY = this.posY;
         this.dashTargetZ = target.posZ;
         double dist = (double)this.getDistance(target);
         this.dashTicksRemaining = (int)(dist / (double)1.0F) + 15;
         this.isDashing = true;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.HOSTILE, 2.0F, 0.5F);
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 10, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
         }

      }

      private void endDash() {
         this.isDashing = false;
         this.dashTicksRemaining = 0;
         switch (this.currentDashAttack) {
            case CHARGE_SLAM:
               this.performChargeSlam();
               break;
            case LEAP_SLAM:
               this.performLeapSlam();
            case SPIKE_BARRAGE:
               break;
            case NONE:
            default:
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + 0.3, this.posZ, 15, (double)1.5F, 0.3, (double)1.5F, 0.05, new int[0]);
               }
         }

         this.currentDashAttack = DashAttackType.NONE;
         this.dashTarget = null;
         int cooldownRange = 25;
         this.dashCooldown = 15 + this.rand.nextInt(cooldownRange);
         if (this.rageMode) {
            this.dashCooldown /= 2;
         }

      }

      private void performChargeSlam() {
         float damage = this.rageMode ? 37.5F : 30.0F;
         float radius = 9.0F;
         this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.5F, 0.4F);
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)0.5F, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 50, (double)radius * (double)0.5F, (double)0.5F, (double)radius * (double)0.5F, 0.1, new int[0]);

            for(int angle = 0; angle < 360; angle += 10) {
               double rad = Math.toRadians((double)angle);
               double px = this.posX + (double)radius * Math.cos(rad);
               double pz = this.posZ + (double)radius * Math.sin(rad);
               ws.spawnParticle(EnumParticleTypes.CLOUD, px, this.posY + (double)0.5F, pz, 2, 0.2, 0.1, 0.2, 0.05, new int[0]);
            }
         }

         for(EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)radius), (entityx) -> entityx != this && !this.isOwner(entityx) && !(entityx instanceof EntityCustom))) {
            entity.hurtResistantTime = 0;
            entity.attackEntityFrom(DamageSource.MAGIC, damage);
            double dx = entity.posX - this.posX;
            double dz = entity.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > (double)0.0F) {
               double knockbackStrength = this.rageMode ? (double)2.5F : (double)2.0F;
               KnockbackHelper.applyWallSafeKnockback(entity, dx / dist * knockbackStrength, 0.8, dz / dist * knockbackStrength);
            }
         }

      }

      private void performLeapSlam() {
         float damage = this.rageMode ? 30.0F : 22.5F;
         float radius = 7.2000003F;
         this.motionY = (double)-0.5F;
         this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_IRONGOLEM_ATTACK, SoundCategory.HOSTILE, 2.0F, 0.3F);
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 5, (double)1.0F, (double)0.5F, (double)1.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + 0.3, this.posZ, 40, (double)radius * 0.4, 0.3, (double)radius * 0.4, 0.08, new int[0]);
         }

         for(EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)radius), (entityx) -> entityx != this && !this.isOwner(entityx) && !(entityx instanceof EntityCustom))) {
            entity.hurtResistantTime = 0;
            entity.attackEntityFrom(DamageSource.MAGIC, damage);
            double dx = entity.posX - this.posX;
            double dz = entity.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > (double)0.0F) {
               double knockbackStrength = this.rageMode ? 1.8 : 1.2;
               KnockbackHelper.applyWallSafeKnockback(entity, dx / dist * knockbackStrength, 0.6, dz / dist * knockbackStrength);
            }
         }

      }

      private void faceEntity(Entity target) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         float targetYaw = (float)(Math.atan2(-dx, dz) * (180D / Math.PI));
         float diff = MathHelper.wrapDegrees(targetYaw - this.rotationYaw);
         if (diff > 10.0F) {
            diff = 10.0F;
         }

         if (diff < -10.0F) {
            diff = -10.0F;
         }

         this.rotationYaw += diff;
         this.renderYawOffset = this.rotationYaw;
      }

      public boolean isDashing() {
         return this.isDashing;
      }

      public void dashToward(double x, double z) {
         this.dashTargetX = x;
         this.dashTargetZ = z;
         this.dashTargetY = this.posY;
         double dist = Math.sqrt(Math.pow(x - this.posX, (double)2.0F) + Math.pow(z - this.posZ, (double)2.0F));
         this.dashTicksRemaining = (int)(dist / (double)1.0F) + 10;
         this.isDashing = true;
         this.dashCooldown = 0;
         this.currentDashAttack = DashAttackType.NONE;
      }

      public void forceChargeAttack(EntityLivingBase target) {
         if (target != null && !target.isDead) {
            this.startChargeAttack(target);
         }

      }

      public void forceLeapSlam(EntityLivingBase target) {
         if (target != null && !target.isDead) {
            this.startLeapSlamAttack(target);
         }

      }

      public void forceSpikeBarrage() {
         EntityPlayer target = this.world.getClosestPlayerToEntity(this, (double)20.0F);
         if (target != null && !this.isOwner(target)) {
            this.performWoodSpike(target);
         }

      }

      public void forceGroundSlam() {
         this.performGroundSlam();
      }

      public void forceSwipe() {
         this.performSwipe();
      }

      public void setFacingLocked(boolean locked, float yaw) {
         this.facingLocked = locked;
         this.lockedYaw = yaw;
         if (locked) {
            this.rotationYaw = yaw;
            this.prevRotationYaw = yaw;
            this.renderYawOffset = yaw;
         }

      }

      public boolean isFacingLocked() {
         return this.facingLocked;
      }

      private void performCombatAI(EntityLivingBase target, double distance) {
         if (!this.rageMode && this.getHealth() < this.getMaxHealth() * 0.3F && this.rageCooldown <= 0) {
            this.startRageMode();
         } else {
            if (distance <= (double)8.0F && this.groundSlamCooldown <= 0) {
               this.performGroundSlam();
            } else if (distance <= (double)10.0F && this.woodSpikeCooldown <= 0) {
               this.performWoodSpike(target);
            } else if (distance <= (double)5.0F && this.swipeCooldown <= 0) {
               this.performSwipe();
            }

         }
      }

      private void performGroundSlam() {
         this.groundSlamCooldown = this.rageMode ? 100 : 200;
         float damage = this.rageMode ? 22.5F : 15.0F;
         this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_IRONGOLEM_ATTACK, SoundCategory.HOSTILE, 2.0F, 0.5F);
         if (this.world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer)this.world;
            worldServer.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 15, (double)3.0F, (double)0.5F, (double)3.0F, (double)0.0F, new int[0]);
            worldServer.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 30, (double)3.0F, 0.3, (double)3.0F, 0.05, new int[0]);
         }

         for(EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)6.0F), (entityx) -> entityx != this && !this.isOwner(entityx) && !(entityx instanceof EntityCustom))) {
            entity.hurtResistantTime = 0;
            entity.attackEntityFrom(DamageSource.MAGIC, damage);
            double dx = entity.posX - this.posX;
            double dz = entity.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > (double)0.0F) {
               double knockbackStrength = this.rageMode ? (double)1.5F : (double)1.0F;
               KnockbackHelper.applyWallSafeKnockback(entity, dx / dist * knockbackStrength, (double)0.5F, dz / dist * knockbackStrength);
            }
         }

         this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 0.8F);
      }

      private void performWoodSpike(EntityLivingBase target) {
         this.woodSpikeCooldown = this.rageMode ? 40 : 80;
         float damage = this.rageMode ? 60.0F : 40.0F;
         List<EntityPlayer> playersInRange = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)32.0F), (p) -> p != null && !p.isDead && !this.isOwner(p));
         if (!playersInRange.isEmpty()) {
            int spawnTick = this.ticksExisted + 15;

            for(EntityPlayer player : playersInRange) {
               BlockPos playerPos = player.getPosition();
               BlockPos directSpikePos = this.findGroundAt(playerPos);
               if (directSpikePos != null) {
                  this.pendingSpikes.add(new PendingSpike(directSpikePos, spawnTick, damage));
               }

               for(int i = 1; i < 3; ++i) {
                  double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
                  double distance = (double)2.0F + this.rand.nextDouble() * (double)8.0F;
                  int offsetX = (int)Math.round(Math.cos(angle) * distance);
                  int offsetZ = (int)Math.round(Math.sin(angle) * distance);
                  BlockPos spikePos = this.findGroundAt(playerPos.add(offsetX, 0, offsetZ));
                  if (spikePos != null) {
                     this.pendingSpikes.add(new PendingSpike(spikePos, spawnTick, damage));
                  }
               }
            }

            this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.HOSTILE, 2.0F, 1.5F);
         }
      }

      private BlockPos findGroundAt(BlockPos pos) {
         for(int y = 0; y < 10; ++y) {
            BlockPos checkPos = pos.down(y);
            BlockPos abovePos = checkPos.up();
            if (!this.world.isAirBlock(checkPos) && this.world.isAirBlock(abovePos)) {
               return abovePos;
            }
         }

         return null;
      }

      private void spawnWoodSpike(BlockPos basePos, float damage) {
         if (!this.world.isRemote) {
            int height = 4 + this.rand.nextInt(2);

            for(int y = 0; y < height; ++y) {
               BlockPos spikePos = basePos.up(y);
               if (this.world.isAirBlock(spikePos)) {
                  this.world.setBlockState(spikePos, Blocks.LOG.getDefaultState());
                  this.placedSpikeBlocks.add(spikePos);
                  this.spikeTimers.put(spikePos, this.ticksExisted + 100);
               }
            }

            BlockPos topPos = basePos.up(height);
            if (this.world.isAirBlock(topPos)) {
               this.world.setBlockState(topPos, Blocks.OAK_FENCE.getDefaultState());
               this.placedSpikeBlocks.add(topPos);
               this.spikeTimers.put(topPos, this.ticksExisted + 100);
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, (double)basePos.getX() + (double)0.5F, (double)basePos.getY() + (double)1.0F, (double)basePos.getZ() + (double)0.5F, 2, 0.3, (double)0.5F, 0.3, (double)0.0F, new int[0]);
               ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)basePos.getX() + (double)0.5F, (double)basePos.getY() + (double)height * (double)0.5F, (double)basePos.getZ() + (double)0.5F, 10, 0.3, (double)height * 0.3, 0.3, (double)0.0F, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, basePos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.HOSTILE, 1.5F, 0.6F);
            AxisAlignedBB damageBox = new AxisAlignedBB((double)basePos.getX() + (double)0.5F - (double)3.0F, (double)basePos.getY(), (double)basePos.getZ() + (double)0.5F - (double)3.0F, (double)basePos.getX() + (double)0.5F + (double)3.0F, (double)(basePos.getY() + height + 1), (double)basePos.getZ() + (double)0.5F + (double)3.0F);

            for(EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, damageBox, (entityx) -> entityx != this && !this.isOwner(entityx) && !(entityx instanceof EntityCustom))) {
               double dist = Math.sqrt(Math.pow(entity.posX - ((double)basePos.getX() + (double)0.5F), (double)2.0F) + Math.pow(entity.posZ - ((double)basePos.getZ() + (double)0.5F), (double)2.0F));
               float damageMultiplier = 1.0F - (float)(dist / (double)3.0F) * 0.5F;
               damageMultiplier = Math.max(0.5F, damageMultiplier);
               entity.hurtResistantTime = 0;
               entity.attackEntityFrom(DamageSource.MAGIC, damage * damageMultiplier);
               entity.motionY += (double)0.5F + (double)0.5F * (double)damageMultiplier;
               if (entity instanceof EntityPlayer) {
                  ((EntityPlayer)entity).velocityChanged = true;
               }
            }

         }
      }

      private void updateSpikeTimers() {
         if (!this.world.isRemote && !this.spikeTimers.isEmpty()) {
            Iterator<Map.Entry<BlockPos, Integer>> iterator = this.spikeTimers.entrySet().iterator();

            while(iterator.hasNext()) {
               Map.Entry<BlockPos, Integer> entry = (Map.Entry)iterator.next();
               if (this.ticksExisted >= (Integer)entry.getValue()) {
                  BlockPos pos = (BlockPos)entry.getKey();
                  this.removeSpikeBlock(pos);
                  this.placedSpikeBlocks.remove(pos);
                  iterator.remove();
               }
            }

         }
      }

      private void removeSpikeBlock(BlockPos pos) {
         IBlockState state = this.world.getBlockState(pos);
         if (state.getBlock() == Blocks.LOG || state.getBlock() == Blocks.OAK_FENCE) {
            this.world.setBlockToAir(pos);
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, 5, 0.3, 0.3, 0.3, 0.02, new int[0]);
            }
         }

      }

      public void cleanupAllSpikes() {
         if (!this.world.isRemote) {
            for(BlockPos pos : this.placedSpikeBlocks) {
               this.removeSpikeBlock(pos);
            }

            this.placedSpikeBlocks.clear();
            this.spikeTimers.clear();
            this.pendingSpikes.clear();
         }
      }

      private void updatePendingSpikes() {
         if (!this.world.isRemote && !this.pendingSpikes.isEmpty()) {
            Iterator<PendingSpike> iterator = this.pendingSpikes.iterator();

            while(iterator.hasNext()) {
               PendingSpike pending = (PendingSpike)iterator.next();
               int ticksRemaining = pending.spawnTick - this.ticksExisted;
               if (ticksRemaining <= 0) {
                  this.spawnWoodSpike(pending.position, pending.damage);
                  iterator.remove();
               } else {
                  this.spawnSpikeWarningParticles(pending.position, ticksRemaining);
               }
            }

         }
      }

      private void spawnSpikeWarningParticles(BlockPos pos, int ticksRemaining) {
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            float intensity = 1.0F - (float)ticksRemaining / 15.0F;
            int outerParticleCount = 8 + (int)(intensity * 12.0F);

            for(int i = 0; i < outerParticleCount; ++i) {
               double angle = (double)i / (double)outerParticleCount * Math.PI * (double)2.0F;
               double px = (double)pos.getX() + (double)0.5F + Math.cos(angle) * (double)3.0F;
               double pz = (double)pos.getZ() + (double)0.5F + Math.sin(angle) * (double)3.0F;
               ws.spawnParticle(EnumParticleTypes.REDSTONE, px, (double)pos.getY() + 0.1, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

            int centerParticleCount = 4 + (int)(intensity * 4.0F);

            for(int i = 0; i < centerParticleCount; ++i) {
               double angle = (double)i / (double)centerParticleCount * Math.PI * (double)2.0F;
               double radius = 0.3 + (double)intensity * 0.2;
               double px = (double)pos.getX() + (double)0.5F + Math.cos(angle) * radius;
               double pz = (double)pos.getZ() + (double)0.5F + Math.sin(angle) * radius;
               ws.spawnParticle(EnumParticleTypes.REDSTONE, px, (double)pos.getY() + 0.15, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

            if (ticksRemaining <= 7) {
               ws.spawnParticle(EnumParticleTypes.CRIT, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, 5, 0.8999999999999999, (double)0.5F, 0.8999999999999999, 0.1, new int[0]);
            }

            if (ticksRemaining <= 3) {
               ws.spawnParticle(EnumParticleTypes.FLAME, (double)pos.getX() + (double)0.5F, (double)pos.getY() + 0.2, (double)pos.getZ() + (double)0.5F, 10, (double)1.5F, 0.1, (double)1.5F, 0.02, new int[0]);
            }

         }
      }

      private void performSwipe() {
         this.swipeCooldown = this.rageMode ? 30 : 60;
         float damage = this.rageMode ? 18.0F : 12.0F;
         float yaw = this.rotationYaw;
         double lookX = -Math.sin(Math.toRadians((double)yaw));
         double lookZ = Math.cos(Math.toRadians((double)yaw));
         if (this.world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer)this.world;

            for(int i = -45; i <= 45; i += 15) {
               double angle = Math.toRadians((double)(yaw + (float)i));
               double px = this.posX - Math.sin(angle) * (double)4.0F;
               double pz = this.posZ + Math.cos(angle) * (double)4.0F;
               worldServer.spawnParticle(EnumParticleTypes.SWEEP_ATTACK, px, this.posY + (double)1.5F, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }
         }

         for(EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)4.0F), (entityx) -> entityx != this && !this.isOwner(entityx))) {
            double dx = entity.posX - this.posX;
            double dz = entity.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > (double)0.0F && dist <= (double)4.0F) {
               double dot = (dx * lookX + dz * lookZ) / dist;
               if (dot > (double)0.5F) {
                  entity.hurtResistantTime = 0;
                  entity.attackEntityFrom(DamageSource.MAGIC, damage);
                  KnockbackHelper.applyWallSafeKnockback(entity, dx / dist * 0.6, 0.2, dz / dist * 0.6);
               }
            }
         }

         this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.5F, 0.7F);
      }

      private void startRageMode() {
         this.rageMode = true;
         this.rageTicks = 0;
         this.rageCooldown = 400;
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)37.5F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35);
         if (this.world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer)this.world;
            worldServer.spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)2.0F, this.posZ, 50, (double)2.0F, (double)3.0F, (double)2.0F, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 2.0F, 0.5F);
      }

      private void endRageMode() {
         this.rageMode = false;
         this.rageTicks = 0;
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)25.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.25F);
      }

      public boolean isInRageMode() {
         return this.rageMode;
      }

      public boolean attackEntityAsMob(Entity target) {
         boolean result = super.attackEntityAsMob(target);
         if (result && target instanceof EntityLivingBase) {
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > (double)0.0F) {
               KnockbackHelper.applyWallSafeKnockback(target, dx / dist * 0.8, 0.3, dz / dist * 0.8);
            }

            if (this.world instanceof WorldServer) {
               WorldServer worldServer = (WorldServer)this.world;
               worldServer.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, target.posX, target.posY, target.posZ, 5, (double)0.5F, (double)0.5F, (double)0.5F, (double)0.0F, new int[0]);
            }
         }

         return result;
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         return source.getTrueSource() != null && this.isOwner(source.getTrueSource()) ? false : super.attackEntityFrom(source, amount);
      }

      public void onDeath(DamageSource cause) {
         super.onDeath(cause);
         this.cleanupAllSpikes();
         if (this.world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer)this.world;
            worldServer.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)2.0F, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            worldServer.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + (double)1.0F, this.posZ, 100, (double)2.0F, (double)3.0F, (double)2.0F, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0F, 0.7F);
      }

      public void setDead() {
         this.cleanupAllSpikes();
         super.setDead();
      }

      public void setOwner(EntityLivingBase owner) {
         this.owner = owner;
         if (owner != null) {
            this.dataManager.set(OWNER_UUID, owner.getCachedUniqueIdString());
         }

      }

      public EntityLivingBase getOwner() {
         return this.owner;
      }

      public String getOwnerUUID() {
         return (String)this.dataManager.get(OWNER_UUID);
      }

      private boolean isOwner(Entity entity) {
         if (entity == null) {
            return false;
         } else if (entity == this.owner) {
            return true;
         } else {
            String uuid = this.getOwnerUUID();
            return uuid != null && !uuid.isEmpty() && uuid.equals(entity.getCachedUniqueIdString());
         }
      }

      public void setGolemScale(float scale) {
         this.dataManager.set(GOLEM_SCALE, scale);
         this.setSize(1.4F * scale, 2.7F * scale);
         this.stepHeight = Math.max(1.0F, scale * 0.7F);
      }

      public float getGolemScale() {
         return (Float)this.dataManager.get(GOLEM_SCALE);
      }

      public void setMaxLifetime(int ticks) {
         this.maxLifetimeTicks = ticks;
      }

      public int getLifetimeTicks() {
         return this.lifetimeTicks;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setString("OwnerUUID", this.getOwnerUUID());
         compound.setFloat("GolemScale", this.getGolemScale());
         compound.setInteger("LifetimeTicks", this.lifetimeTicks);
         compound.setInteger("MaxLifetimeTicks", this.maxLifetimeTicks);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.dataManager.set(OWNER_UUID, compound.getString("OwnerUUID"));
         if (compound.hasKey("GolemScale")) {
            this.setGolemScale(compound.getFloat("GolemScale"));
         }

         this.lifetimeTicks = compound.getInteger("LifetimeTicks");
         this.maxLifetimeTicks = compound.getInteger("MaxLifetimeTicks");
      }

      public double getMountedYOffset() {
         float scale = this.getGolemScale();
         return (double)scale * 2.2;
      }

      public boolean canBeRidden(Entity entityIn) {
         return true;
      }

      public void updatePassenger(Entity passenger) {
         if (this.isPassenger(passenger)) {
            float scale = this.getGolemScale();
            double yOffset = this.getMountedYOffset();
            double angle = Math.toRadians((double)this.rotationYaw);
            double forwardOffset = -0.1 * (double)scale;
            double offsetX = -Math.sin(angle) * forwardOffset;
            double offsetZ = Math.cos(angle) * forwardOffset;
            passenger.setPosition(this.posX + offsetX, this.posY + yOffset, this.posZ + offsetZ);
            if (passenger instanceof EntityLivingBase) {
               EntityLivingBase rider = (EntityLivingBase)passenger;
               rider.renderYawOffset = this.renderYawOffset;
            }
         }

      }

      public void mountRider(Entity rider) {
         if (rider != null && !rider.isDead) {
            rider.startRiding(this, true);
         }

      }

      public void dismountRider() {
         for(Entity passenger : this.getPassengers()) {
            passenger.dismountRidingEntity();
         }

      }

      protected boolean canDespawn() {
         return false;
      }

      protected Item getDropItem() {
         return null;
      }

      public boolean getCanSpawnHere() {
         return false;
      }

      protected boolean canDropLoot() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
      }

      public void travel(float strafe, float vertical, float forward) {
         if (this.isBeingRidden()) {
            this.moveRelative(strafe, vertical, forward, 0.02F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9;
            this.motionY *= 0.98;
            this.motionZ *= 0.9;
            if (!this.hasNoGravity()) {
               this.motionY -= 0.08;
            }
         } else {
            super.travel(strafe, vertical, forward);
         }

      }

      public Entity getControllingPassenger() {
         return null;
      }

      public SoundEvent getAmbientSound() {
         return null;
      }

      public SoundEvent getHurtSound(DamageSource ds) {
         return SoundEvents.BLOCK_WOOD_HIT;
      }

      public SoundEvent getDeathSound() {
         return SoundEvents.BLOCK_WOOD_BREAK;
      }

      protected float getSoundVolume() {
         return 2.0F;
      }

      protected boolean isValidLightLevel() {
         return true;
      }

      static {
         OWNER_UUID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
         GOLEM_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }

      private static enum DashAttackType {
         NONE,
         CHARGE_SLAM,
         SPIKE_BARRAGE,
         LEAP_SLAM;
      }

      private static class PendingSpike {
         final BlockPos position;
         final int spawnTick;
         final float damage;

         PendingSpike(BlockPos pos, int spawnTick, float damage) {
            this.position = pos;
            this.spawnTick = spawnTick;
            this.damage = damage;
         }
      }
   }
}
