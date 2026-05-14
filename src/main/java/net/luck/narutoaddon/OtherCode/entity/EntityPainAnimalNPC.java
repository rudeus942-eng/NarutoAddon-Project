
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.ModelPlayerPoseable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcPose;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityPainAnimalNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 320;
   public static final int ENTITYID_SUMMON = 321;
   public static final int ENTITYID_BIRD = 325;
   public static final int ENTITYID_CHAMELEON = 326;

   public EntityPainAnimalNPC(ElementsInfTsukAddon instance) {
      super(instance, 320);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "painanimalnpc"), 320).name("painanimalnpc").tracker(64, 3, true).egg(-1, -1).build());
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntitySummonedBeast.class).id(new ResourceLocation("inftsukaddon", "summoned_beast"), 321).name("summoned_beast").tracker(64, 3, true).build());
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntitySummonedBird.class).id(new ResourceLocation("inftsukaddon", "summoned_bird"), 325).name("summoned_bird").tracker(64, 3, true).build());
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntitySummonedChameleon.class).id(new ResourceLocation("inftsukaddon", "summoned_chameleon"), 326).name("summoned_chameleon").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, AnimalPathNpcRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(EntitySummonedBeast.class, SummonedBeastRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(EntitySummonedBird.class, BirdRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(EntitySummonedChameleon.class, ChameleonRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class AnimalPathNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public AnimalPathNpcRenderer(RenderManager renderManager) {
         super(renderManager, new ModelPlayerPoseable(0.0F, false), 0.5F);
         this.addLayer(new LayerHeldItem(this));
         this.addLayer(new LayerBipedArmor(this));
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         String texOverride = entity.getTextureOverride();
         if (texOverride != null && !texOverride.isEmpty()) {
            return new ResourceLocation(texOverride);
         } else {
            String configId = entity.getNpcConfigId();
            if (configId != null && !configId.isEmpty()) {
               NpcConfig config = NpcConfigRegistry.get(configId);
               if (config != null && config.getTexture() != null) {
                  return config.getTexture();
               }
            }

            return FALLBACK_TEXTURE;
         }
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         String configId = entity.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null) {
               float scale = config.getRenderScale();
               GlStateManager.scale(scale, scale, scale);
            }
         }

         String poseName = entity.getNpcPoseName();
         NpcPose pose = NpcPose.fromName(poseName);
         if (pose.yOffset != 0.0F) {
            GlStateManager.translate(0.0F, pose.yOffset, 0.0F);
         }

      }

      protected void renderModel(EntityCustom entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
         float alpha = 1.0F;
         String configId = entity.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config != null) {
               alpha = config.getGhostAlpha();
            }
         }

         if (alpha < 1.0F) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
            GlStateManager.color(0.6F, 0.4F, 0.8F, alpha);
            if (this.bindEntityTexture(entity)) {
               this.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            }

            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         } else {
            super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelSummonDog extends ModelBase {
      private final ModelRenderer headRight;
      private final ModelRenderer bone6;
      private final ModelRenderer bone8;
      private final ModelRenderer bone3;
      private final ModelRenderer jawRight;
      private final ModelRenderer headLeft;
      private final ModelRenderer bone4;
      private final ModelRenderer bone9;
      private final ModelRenderer bone10;
      private final ModelRenderer jawLeft;
      private final ModelRenderer body;
      private final ModelRenderer tail;
      private final ModelRenderer tail2;
      private final ModelRenderer upperBody;
      private final ModelRenderer wingRight;
      private final ModelRenderer cube_r1;
      private final ModelRenderer cube_r2;
      private final ModelRenderer cube_r3;
      private final ModelRenderer wingLeft;
      private final ModelRenderer cube_r4;
      private final ModelRenderer cube_r5;
      private final ModelRenderer cube_r6;
      private final ModelRenderer leg0;
      private final ModelRenderer bone2;
      private final ModelRenderer leg6;
      private final ModelRenderer leg8;
      private final ModelRenderer foot0;
      private final ModelRenderer bone27;
      private final ModelRenderer bone28;
      private final ModelRenderer bone29;
      private final ModelRenderer bone30;
      private final ModelRenderer bone31;
      private final ModelRenderer bone32;
      private final ModelRenderer bone33;
      private final ModelRenderer bone34;
      private final ModelRenderer leg1;
      private final ModelRenderer bone7;
      private final ModelRenderer leg7;
      private final ModelRenderer leg9;
      private final ModelRenderer foot1;
      private final ModelRenderer bone35;
      private final ModelRenderer bone36;
      private final ModelRenderer bone37;
      private final ModelRenderer bone38;
      private final ModelRenderer bone39;
      private final ModelRenderer bone40;
      private final ModelRenderer bone41;
      private final ModelRenderer bone42;
      private final ModelRenderer leg2;
      private final ModelRenderer bone;
      private final ModelRenderer leg4;
      private final ModelRenderer foot2;
      private final ModelRenderer bone11;
      private final ModelRenderer bone12;
      private final ModelRenderer bone17;
      private final ModelRenderer bone18;
      private final ModelRenderer bone13;
      private final ModelRenderer bone14;
      private final ModelRenderer bone15;
      private final ModelRenderer bone16;
      private final ModelRenderer leg3;
      private final ModelRenderer bone5;
      private final ModelRenderer leg5;
      private final ModelRenderer foot3;
      private final ModelRenderer bone19;
      private final ModelRenderer bone20;
      private final ModelRenderer bone21;
      private final ModelRenderer bone22;
      private final ModelRenderer bone23;
      private final ModelRenderer bone24;
      private final ModelRenderer bone25;
      private final ModelRenderer bone26;

      public ModelSummonDog() {
         this.textureWidth = 64;
         this.textureHeight = 32;
         (this.headRight = new ModelRenderer(this)).setRotationPoint(-2.0F, 13.5F, -2.0F);
         this.setRotationAngle(this.headRight, 0.0F, 0.2618F, 0.0F);
         this.headRight.cubeList.add(new ModelBox(this.headRight, 0, 1, -2.5F, -2.5F, -4.0F, 5, 5, 4, 0.0F, false));
         this.headRight.cubeList.add(new ModelBox(this.headRight, 1, 2, -2.5F, -2.5F, -0.25F, 5, 5, 3, 0.0F, false));
         this.headRight.cubeList.add(new ModelBox(this.headRight, 0, 10, -1.5F, -0.5156F, -7.0F, 3, 3, 4, 0.0F, false));
         this.headRight.cubeList.add(new ModelBox(this.headRight, 10, 10, -0.4461F, -1.5F, -6.0F, 1, 2, 1, 0.0F, false));
         (this.bone6 = new ModelRenderer(this)).setRotationPoint(-1.5F, 1.0F, -5.0F);
         this.headRight.addChild(this.bone6);
         this.setRotationAngle(this.bone6, 0.0F, -0.6981F, 0.0F);
         this.bone6.cubeList.add(new ModelBox(this.bone6, 20, 0, 0.0F, -1.5F, 0.0F, 1, 3, 2, 0.0F, false));
         (this.bone8 = new ModelRenderer(this)).setRotationPoint(1.5F, 1.0F, -5.0F);
         this.headRight.addChild(this.bone8);
         this.setRotationAngle(this.bone8, 0.0F, 0.6981F, 0.0F);
         this.bone8.cubeList.add(new ModelBox(this.bone8, 20, 0, -1.0F, -1.5F, 0.0F, 1, 3, 2, 0.0F, true));
         (this.bone3 = new ModelRenderer(this)).setRotationPoint(5.0F, -0.6F, -5.85F);
         this.headRight.addChild(this.bone3);
         this.setRotationAngle(this.bone3, 0.0F, 0.0F, -3.1416F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 44, 14, 3.5F, -2.5F, -2.1F, 6, 6, 0, -3.9F, true));
         this.bone3.cubeList.add(new ModelBox(this.bone3, 44, 14, 0.5F, -2.5F, -2.1F, 6, 6, 0, -3.9F, false));
         (this.jawRight = new ModelRenderer(this)).setRotationPoint(0.0F, 1.5F, -4.0F);
         this.headRight.addChild(this.jawRight);
         this.setRotationAngle(this.jawRight, 0.7854F, 0.0F, 0.0F);
         this.jawRight.cubeList.add(new ModelBox(this.jawRight, 50, 27, -1.5F, 0.0F, -3.0F, 3, 1, 4, 0.0F, false));
         (this.headLeft = new ModelRenderer(this)).setRotationPoint(2.0F, 13.5F, -2.0F);
         this.setRotationAngle(this.headLeft, 0.0F, -0.2618F, 0.0F);
         this.headLeft.cubeList.add(new ModelBox(this.headLeft, 0, 1, -2.5F, -2.5F, -4.0F, 5, 5, 4, 0.0F, true));
         this.headLeft.cubeList.add(new ModelBox(this.headLeft, 1, 2, -2.5F, -2.5F, -0.25F, 5, 5, 3, 0.0F, true));
         this.headLeft.cubeList.add(new ModelBox(this.headLeft, 0, 10, -1.5F, -0.5156F, -7.0F, 3, 3, 4, 0.0F, true));
         this.headLeft.cubeList.add(new ModelBox(this.headLeft, 10, 10, -0.5539F, -1.5F, -6.0F, 1, 2, 1, 0.0F, true));
         (this.bone4 = new ModelRenderer(this)).setRotationPoint(1.5F, 1.0F, -5.0F);
         this.headLeft.addChild(this.bone4);
         this.setRotationAngle(this.bone4, 0.0F, 0.6981F, 0.0F);
         this.bone4.cubeList.add(new ModelBox(this.bone4, 20, 0, -1.0F, -1.5F, 0.0F, 1, 3, 2, 0.0F, true));
         (this.bone9 = new ModelRenderer(this)).setRotationPoint(-1.5F, 1.0F, -5.0F);
         this.headLeft.addChild(this.bone9);
         this.setRotationAngle(this.bone9, 0.0F, -0.6981F, 0.0F);
         this.bone9.cubeList.add(new ModelBox(this.bone9, 20, 0, 0.0F, -1.5F, 0.0F, 1, 3, 2, 0.0F, false));
         (this.bone10 = new ModelRenderer(this)).setRotationPoint(-5.0F, -0.6F, -5.85F);
         this.headLeft.addChild(this.bone10);
         this.setRotationAngle(this.bone10, 0.0F, 0.0F, 3.1416F);
         this.bone10.cubeList.add(new ModelBox(this.bone10, 44, 14, -9.5F, -2.5F, -2.1F, 6, 6, 0, -3.9F, false));
         this.bone10.cubeList.add(new ModelBox(this.bone10, 44, 14, -6.5F, -2.5F, -2.1F, 6, 6, 0, -3.9F, true));
         (this.jawLeft = new ModelRenderer(this)).setRotationPoint(0.0F, 1.5F, -4.0F);
         this.headLeft.addChild(this.jawLeft);
         this.jawLeft.cubeList.add(new ModelBox(this.jawLeft, 50, 27, -1.5F, 0.0F, -3.0F, 3, 1, 4, 0.0F, true));
         (this.body = new ModelRenderer(this)).setRotationPoint(0.0F, 10.5F, 4.0F);
         this.setRotationAngle(this.body, 1.3963F, 0.0F, 0.0F);
         this.body.cubeList.add(new ModelBox(this.body, 17, 17, -3.0F, 0.0F, -6.0F, 6, 9, 6, 0.0F, false));
         (this.tail = new ModelRenderer(this)).setRotationPoint(0.0F, 8.5F, -1.0F);
         this.body.addChild(this.tail);
         this.setRotationAngle(this.tail, -0.5236F, 0.0F, 0.0F);
         this.tail.cubeList.add(new ModelBox(this.tail, 9, 18, -1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F, false));
         (this.tail2 = new ModelRenderer(this)).setRotationPoint(0.0F, 7.5F, 0.0F);
         this.tail.addChild(this.tail2);
         this.setRotationAngle(this.tail2, 0.5236F, 0.0F, 0.0F);
         this.tail2.cubeList.add(new ModelBox(this.tail2, 9, 18, -1.0F, -0.5F, -1.0F, 2, 8, 2, -0.2F, false));
         (this.upperBody = new ModelRenderer(this)).setRotationPoint(0.0F, 14.0F, 6.0F);
         this.setRotationAngle(this.upperBody, -1.5708F, 0.0F, 0.0F);
         this.upperBody.cubeList.add(new ModelBox(this.upperBody, 21, 0, -4.0F, 2.0F, -4.0F, 8, 6, 7, 0.0F, false));
         (this.wingRight = new ModelRenderer(this)).setRotationPoint(-4.0927F, 5.8734F, -3.9678F);
         this.upperBody.addChild(this.wingRight);
         this.setRotationAngle(this.wingRight, 1.5708F, 0.7854F, 0.0F);
         (this.cube_r1 = new ModelRenderer(this)).setRotationPoint(1.7704F, 7.9911F, 5.1063F);
         this.wingRight.addChild(this.cube_r1);
         this.setRotationAngle(this.cube_r1, -0.9722F, -0.0114F, -0.2253F);
         this.cube_r1.cubeList.add(new ModelBox(this.cube_r1, 52, -6, 0.0F, -10.0F, -12.0F, 0, 10, 6, 0.0F, true));
         (this.cube_r2 = new ModelRenderer(this)).setRotationPoint(2.7704F, 2.7911F, 9.1063F);
         this.wingRight.addChild(this.cube_r2);
         this.setRotationAngle(this.cube_r2, -0.9722F, -0.0114F, -0.2253F);
         this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 0, 18, -3.2F, -4.8F, -11.2F, 2, 8, 2, -0.7F, false));
         (this.cube_r3 = new ModelRenderer(this)).setRotationPoint(2.2927F, 0.7266F, 10.5178F);
         this.wingRight.addChild(this.cube_r3);
         this.setRotationAngle(this.cube_r3, -0.3177F, -0.0114F, -0.2253F);
         this.cube_r3.cubeList.add(new ModelBox(this.cube_r3, 0, 19, -3.2F, -2.8F, -11.2F, 2, 6, 2, -0.7F, false));
         (this.wingLeft = new ModelRenderer(this)).setRotationPoint(4.0927F, 5.8734F, -3.9678F);
         this.upperBody.addChild(this.wingLeft);
         this.setRotationAngle(this.wingLeft, 1.5708F, -0.7854F, 0.0F);
         (this.cube_r4 = new ModelRenderer(this)).setRotationPoint(-1.7704F, 7.9911F, 5.1063F);
         this.wingLeft.addChild(this.cube_r4);
         this.setRotationAngle(this.cube_r4, -0.9722F, 0.0114F, 0.2253F);
         this.cube_r4.cubeList.add(new ModelBox(this.cube_r4, 52, -6, 0.0F, -10.0F, -12.0F, 0, 10, 6, 0.0F, false));
         (this.cube_r5 = new ModelRenderer(this)).setRotationPoint(-2.7704F, 2.7911F, 9.1063F);
         this.wingLeft.addChild(this.cube_r5);
         this.setRotationAngle(this.cube_r5, -0.9722F, 0.0114F, 0.2253F);
         this.cube_r5.cubeList.add(new ModelBox(this.cube_r5, 0, 18, 1.2F, -4.8F, -11.2F, 2, 8, 2, -0.7F, true));
         (this.cube_r6 = new ModelRenderer(this)).setRotationPoint(-2.2927F, 0.7266F, 10.5178F);
         this.wingLeft.addChild(this.cube_r6);
         this.setRotationAngle(this.cube_r6, -0.3177F, 0.0114F, 0.2253F);
         this.cube_r6.cubeList.add(new ModelBox(this.cube_r6, 0, 19, 1.2F, -2.8F, -11.2F, 2, 6, 2, -0.7F, true));
         (this.leg0 = new ModelRenderer(this)).setRotationPoint(-2.5F, 13.0F, 11.0F);
         (this.bone2 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.leg0.addChild(this.bone2);
         this.setRotationAngle(this.bone2, -0.1745F, 0.0F, 0.1745F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, 18, -1.0F, 0.0F, -1.0F, 2, 5, 2, 0.5F, false));
         (this.leg6 = new ModelRenderer(this)).setRotationPoint(-1.0F, 5.5F, -1.5F);
         this.bone2.addChild(this.leg6);
         this.setRotationAngle(this.leg6, 1.0472F, 0.0F, -0.1745F);
         this.leg6.cubeList.add(new ModelBox(this.leg6, 0, 18, 0.0F, 0.0F, 0.0F, 2, 6, 2, 0.0F, false));
         (this.leg8 = new ModelRenderer(this)).setRotationPoint(0.0F, 6.0F, 2.0F);
         this.leg6.addChild(this.leg8);
         this.setRotationAngle(this.leg8, -1.1345F, 0.0436F, 0.0F);
         this.leg8.cubeList.add(new ModelBox(this.leg8, 0, 18, 0.0F, 0.0F, -2.0F, 2, 4, 2, 0.0F, false));
         (this.foot0 = new ModelRenderer(this)).setRotationPoint(1.0F, 3.75F, -2.5F);
         this.leg8.addChild(this.foot0);
         this.setRotationAngle(this.foot0, 0.3054F, 0.0F, -0.0436F);
         this.foot0.cubeList.add(new ModelBox(this.foot0, 0, 28, -1.0F, 0.0F, 0.0F, 2, 1, 2, 0.1F, false));
         (this.bone27 = new ModelRenderer(this)).setRotationPoint(0.25F, -0.25F, 0.1F);
         this.foot0.addChild(this.bone27);
         this.setRotationAngle(this.bone27, 0.2618F, -0.2618F, 0.0873F);
         this.bone27.cubeList.add(new ModelBox(this.bone27, 4, 1, 0.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, false));
         (this.bone28 = new ModelRenderer(this)).setRotationPoint(0.5F, 0.25F, -0.9F);
         this.bone27.addChild(this.bone28);
         this.setRotationAngle(this.bone28, 0.3491F, 0.0F, 0.0F);
         this.bone28.cubeList.add(new ModelBox(this.bone28, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.bone29 = new ModelRenderer(this)).setRotationPoint(1.15F, -0.75F, 1.85F);
         this.foot0.addChild(this.bone29);
         this.setRotationAngle(this.bone29, 0.4363F, -1.8326F, 0.5236F);
         this.bone29.cubeList.add(new ModelBox(this.bone29, 4, 1, 0.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, false));
         (this.bone30 = new ModelRenderer(this)).setRotationPoint(0.5F, 0.25F, -0.9F);
         this.bone29.addChild(this.bone30);
         this.setRotationAngle(this.bone30, 0.4363F, 0.0F, 0.0F);
         this.bone30.cubeList.add(new ModelBox(this.bone30, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.bone31 = new ModelRenderer(this)).setRotationPoint(-0.5F, -0.25F, 0.1F);
         this.foot0.addChild(this.bone31);
         this.setRotationAngle(this.bone31, 0.2618F, 0.0F, 0.0F);
         this.bone31.cubeList.add(new ModelBox(this.bone31, 4, 1, 0.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, false));
         (this.bone32 = new ModelRenderer(this)).setRotationPoint(0.5F, 0.25F, -0.9F);
         this.bone31.addChild(this.bone32);
         this.setRotationAngle(this.bone32, 0.3491F, 0.0F, 0.0F);
         this.bone32.cubeList.add(new ModelBox(this.bone32, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.bone33 = new ModelRenderer(this)).setRotationPoint(-1.25F, -0.15F, 0.35F);
         this.foot0.addChild(this.bone33);
         this.setRotationAngle(this.bone33, 0.2618F, 0.2618F, -0.0873F);
         this.bone33.cubeList.add(new ModelBox(this.bone33, 4, 1, 0.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, false));
         (this.bone34 = new ModelRenderer(this)).setRotationPoint(0.5F, 0.25F, -0.9F);
         this.bone33.addChild(this.bone34);
         this.setRotationAngle(this.bone34, 0.3491F, 0.0F, 0.0F);
         this.bone34.cubeList.add(new ModelBox(this.bone34, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.leg1 = new ModelRenderer(this)).setRotationPoint(2.5F, 13.0F, 11.0F);
         (this.bone7 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.leg1.addChild(this.bone7);
         this.setRotationAngle(this.bone7, -0.1745F, 0.0F, -0.1745F);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 0, 18, -1.0F, 0.0F, -1.0F, 2, 5, 2, 0.5F, true));
         (this.leg7 = new ModelRenderer(this)).setRotationPoint(1.0F, 5.5F, -1.5F);
         this.bone7.addChild(this.leg7);
         this.setRotationAngle(this.leg7, 1.0472F, 0.0F, 0.1745F);
         this.leg7.cubeList.add(new ModelBox(this.leg7, 0, 18, -2.0F, 0.0F, 0.0F, 2, 6, 2, 0.0F, true));
         (this.leg9 = new ModelRenderer(this)).setRotationPoint(0.0F, 6.0F, 2.0F);
         this.leg7.addChild(this.leg9);
         this.setRotationAngle(this.leg9, -1.1345F, -0.0436F, 0.0F);
         this.leg9.cubeList.add(new ModelBox(this.leg9, 0, 18, -2.0F, 0.0F, -2.0F, 2, 4, 2, 0.0F, true));
         (this.foot1 = new ModelRenderer(this)).setRotationPoint(-1.0F, 3.75F, -2.5F);
         this.leg9.addChild(this.foot1);
         this.setRotationAngle(this.foot1, 0.3054F, 0.0F, 0.0436F);
         this.foot1.cubeList.add(new ModelBox(this.foot1, 0, 28, -1.0F, 0.0F, 0.0F, 2, 1, 2, 0.1F, true));
         (this.bone35 = new ModelRenderer(this)).setRotationPoint(-0.25F, -0.25F, 0.1F);
         this.foot1.addChild(this.bone35);
         this.setRotationAngle(this.bone35, 0.2618F, 0.2618F, -0.0873F);
         this.bone35.cubeList.add(new ModelBox(this.bone35, 4, 1, -1.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, true));
         (this.bone36 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.25F, -0.9F);
         this.bone35.addChild(this.bone36);
         this.setRotationAngle(this.bone36, 0.3491F, 0.0F, 0.0F);
         this.bone36.cubeList.add(new ModelBox(this.bone36, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.bone37 = new ModelRenderer(this)).setRotationPoint(-1.15F, -0.75F, 1.85F);
         this.foot1.addChild(this.bone37);
         this.setRotationAngle(this.bone37, 0.4363F, 1.8326F, -0.5236F);
         this.bone37.cubeList.add(new ModelBox(this.bone37, 4, 1, -1.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, true));
         (this.bone38 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.25F, -0.9F);
         this.bone37.addChild(this.bone38);
         this.setRotationAngle(this.bone38, 0.4363F, 0.0F, 0.0F);
         this.bone38.cubeList.add(new ModelBox(this.bone38, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.bone39 = new ModelRenderer(this)).setRotationPoint(0.5F, -0.25F, 0.1F);
         this.foot1.addChild(this.bone39);
         this.setRotationAngle(this.bone39, 0.2618F, 0.0F, 0.0F);
         this.bone39.cubeList.add(new ModelBox(this.bone39, 4, 1, -1.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, true));
         (this.bone40 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.25F, -0.9F);
         this.bone39.addChild(this.bone40);
         this.setRotationAngle(this.bone40, 0.3491F, 0.0F, 0.0F);
         this.bone40.cubeList.add(new ModelBox(this.bone40, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.bone41 = new ModelRenderer(this)).setRotationPoint(1.25F, -0.15F, 0.35F);
         this.foot1.addChild(this.bone41);
         this.setRotationAngle(this.bone41, 0.2618F, -0.2618F, 0.0873F);
         this.bone41.cubeList.add(new ModelBox(this.bone41, 4, 1, -1.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, true));
         (this.bone42 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.25F, -0.9F);
         this.bone41.addChild(this.bone42);
         this.setRotationAngle(this.bone42, 0.3491F, 0.0F, 0.0F);
         this.bone42.cubeList.add(new ModelBox(this.bone42, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.leg2 = new ModelRenderer(this)).setRotationPoint(-3.0F, 13.0F, 0.0F);
         (this.bone = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.leg2.addChild(this.bone);
         this.setRotationAngle(this.bone, 0.2618F, -0.2618F, 0.1309F);
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 18, -1.0F, 0.0F, -1.0F, 2, 6, 2, 0.1F, false));
         (this.leg4 = new ModelRenderer(this)).setRotationPoint(-1.0F, 6.0F, 1.0F);
         this.bone.addChild(this.leg4);
         this.setRotationAngle(this.leg4, -0.6109F, 0.1745F, -0.0873F);
         this.leg4.cubeList.add(new ModelBox(this.leg4, 0, 18, 0.0F, 0.0F, -2.0F, 2, 6, 2, 0.0F, false));
         (this.foot2 = new ModelRenderer(this)).setRotationPoint(1.0F, 5.75F, -2.5F);
         this.leg4.addChild(this.foot2);
         this.setRotationAngle(this.foot2, 0.3054F, 0.0F, -0.0436F);
         this.foot2.cubeList.add(new ModelBox(this.foot2, 0, 28, -1.0F, 0.0F, 0.0F, 2, 1, 2, 0.1F, false));
         (this.bone11 = new ModelRenderer(this)).setRotationPoint(0.25F, -0.25F, 0.1F);
         this.foot2.addChild(this.bone11);
         this.setRotationAngle(this.bone11, 0.2618F, -0.2618F, 0.0873F);
         this.bone11.cubeList.add(new ModelBox(this.bone11, 4, 1, 0.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, false));
         (this.bone12 = new ModelRenderer(this)).setRotationPoint(0.5F, 0.25F, -0.9F);
         this.bone11.addChild(this.bone12);
         this.setRotationAngle(this.bone12, 0.3491F, 0.0F, 0.0F);
         this.bone12.cubeList.add(new ModelBox(this.bone12, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.bone17 = new ModelRenderer(this)).setRotationPoint(1.15F, -0.75F, 1.85F);
         this.foot2.addChild(this.bone17);
         this.setRotationAngle(this.bone17, 0.4363F, -1.8326F, 0.5236F);
         this.bone17.cubeList.add(new ModelBox(this.bone17, 4, 1, 0.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, false));
         (this.bone18 = new ModelRenderer(this)).setRotationPoint(0.5F, 0.25F, -0.9F);
         this.bone17.addChild(this.bone18);
         this.setRotationAngle(this.bone18, 0.4363F, 0.0F, 0.0F);
         this.bone18.cubeList.add(new ModelBox(this.bone18, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.bone13 = new ModelRenderer(this)).setRotationPoint(-0.5F, -0.25F, 0.1F);
         this.foot2.addChild(this.bone13);
         this.setRotationAngle(this.bone13, 0.2618F, 0.0F, 0.0F);
         this.bone13.cubeList.add(new ModelBox(this.bone13, 4, 1, 0.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, false));
         (this.bone14 = new ModelRenderer(this)).setRotationPoint(0.5F, 0.25F, -0.9F);
         this.bone13.addChild(this.bone14);
         this.setRotationAngle(this.bone14, 0.3491F, 0.0F, 0.0F);
         this.bone14.cubeList.add(new ModelBox(this.bone14, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.bone15 = new ModelRenderer(this)).setRotationPoint(-1.25F, -0.15F, 0.35F);
         this.foot2.addChild(this.bone15);
         this.setRotationAngle(this.bone15, 0.2618F, 0.2618F, -0.0873F);
         this.bone15.cubeList.add(new ModelBox(this.bone15, 4, 1, 0.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, false));
         (this.bone16 = new ModelRenderer(this)).setRotationPoint(0.5F, 0.25F, -0.9F);
         this.bone15.addChild(this.bone16);
         this.setRotationAngle(this.bone16, 0.3491F, 0.0F, 0.0F);
         this.bone16.cubeList.add(new ModelBox(this.bone16, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.leg3 = new ModelRenderer(this)).setRotationPoint(3.0F, 13.0F, 0.0F);
         (this.bone5 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.leg3.addChild(this.bone5);
         this.setRotationAngle(this.bone5, 0.2618F, 0.2618F, -0.1309F);
         this.bone5.cubeList.add(new ModelBox(this.bone5, 0, 18, -1.0F, 0.0F, -1.0F, 2, 6, 2, 0.1F, true));
         (this.leg5 = new ModelRenderer(this)).setRotationPoint(1.0F, 6.0F, 1.0F);
         this.bone5.addChild(this.leg5);
         this.setRotationAngle(this.leg5, -0.6109F, -0.1745F, 0.0873F);
         this.leg5.cubeList.add(new ModelBox(this.leg5, 0, 18, -2.0F, 0.0F, -2.0F, 2, 6, 2, 0.0F, true));
         (this.foot3 = new ModelRenderer(this)).setRotationPoint(-1.0F, 5.75F, -2.5F);
         this.leg5.addChild(this.foot3);
         this.setRotationAngle(this.foot3, 0.3054F, 0.0F, 0.0436F);
         this.foot3.cubeList.add(new ModelBox(this.foot3, 0, 28, -1.0F, 0.0F, 0.0F, 2, 1, 2, 0.1F, true));
         (this.bone19 = new ModelRenderer(this)).setRotationPoint(-0.25F, -0.25F, 0.1F);
         this.foot3.addChild(this.bone19);
         this.setRotationAngle(this.bone19, 0.2618F, 0.2618F, -0.0873F);
         this.bone19.cubeList.add(new ModelBox(this.bone19, 4, 1, -1.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, true));
         (this.bone20 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.25F, -0.9F);
         this.bone19.addChild(this.bone20);
         this.setRotationAngle(this.bone20, 0.3491F, 0.0F, 0.0F);
         this.bone20.cubeList.add(new ModelBox(this.bone20, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.bone21 = new ModelRenderer(this)).setRotationPoint(-1.15F, -0.75F, 1.85F);
         this.foot3.addChild(this.bone21);
         this.setRotationAngle(this.bone21, 0.4363F, 1.8326F, -0.5236F);
         this.bone21.cubeList.add(new ModelBox(this.bone21, 4, 1, -1.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, true));
         (this.bone22 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.25F, -0.9F);
         this.bone21.addChild(this.bone22);
         this.setRotationAngle(this.bone22, 0.4363F, 0.0F, 0.0F);
         this.bone22.cubeList.add(new ModelBox(this.bone22, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.bone23 = new ModelRenderer(this)).setRotationPoint(0.5F, -0.25F, 0.1F);
         this.foot3.addChild(this.bone23);
         this.setRotationAngle(this.bone23, 0.2618F, 0.0F, 0.0F);
         this.bone23.cubeList.add(new ModelBox(this.bone23, 4, 1, -1.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, true));
         (this.bone24 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.25F, -0.9F);
         this.bone23.addChild(this.bone24);
         this.setRotationAngle(this.bone24, 0.3491F, 0.0F, 0.0F);
         this.bone24.cubeList.add(new ModelBox(this.bone24, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.bone25 = new ModelRenderer(this)).setRotationPoint(1.25F, -0.15F, 0.35F);
         this.foot3.addChild(this.bone25);
         this.setRotationAngle(this.bone25, 0.2618F, -0.2618F, 0.0873F);
         this.bone25.cubeList.add(new ModelBox(this.bone25, 4, 1, -1.0F, 0.0F, -1.0F, 1, 1, 1, -0.1F, true));
         (this.bone26 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.25F, -0.9F);
         this.bone25.addChild(this.bone26);
         this.setRotationAngle(this.bone26, 0.3491F, 0.0F, 0.0F);
         this.bone26.cubeList.add(new ModelBox(this.bone26, 0, 0, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
      }

      public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
         this.headRight.rotationPointX = -2.0F;
         ModelRenderer var10000 = this.headRight;
         var10000.rotateAngleY += 0.2618F;
         var10000 = this.headLeft;
         var10000.rotateAngleY -= 0.2618F;
         this.headLeft.isHidden = false;
         this.headRight.render(scale);
         this.headLeft.render(scale);
         this.body.render(scale);
         this.upperBody.render(scale);
         this.leg0.render(scale);
         this.leg1.render(scale);
         this.leg2.render(scale);
         this.leg3.render(scale);
      }

      public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
         float adjustedSwing = limbSwing * 2.0F / entity.height;
         super.setRotationAngles(adjustedSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
         this.headRight.rotateAngleY = netHeadYaw / 180.0F * (float)Math.PI;
         this.headRight.rotateAngleX = headPitch / 180.0F * (float)Math.PI;
         this.headLeft.rotateAngleY = netHeadYaw / 180.0F * (float)Math.PI;
         this.headLeft.rotateAngleX = headPitch / 180.0F * (float)Math.PI;
         float walkCycle = MathHelper.cos(adjustedSwing * 1.0F) * limbSwingAmount;
         this.leg0.rotateAngleX = -walkCycle;
         this.leg1.rotateAngleX = walkCycle;
         this.leg2.rotateAngleX = walkCycle;
         this.leg3.rotateAngleX = -walkCycle;
         float wingFlap = MathHelper.sin(ageInTicks * 0.09F) * 0.1F + walkCycle * 0.6F;
         this.wingRight.rotateAngleY = 0.7854F - wingFlap;
         this.wingLeft.rotateAngleY = -0.7854F + wingFlap;
         this.tail.rotateAngleY = ageInTicks * 0.2F;
         this.tail2.rotateAngleY = ageInTicks * 0.1F;
         if (this.swingProgress > 0.0F) {
            this.jawRight.rotateAngleX = 1.0472F * (1.0F - this.swingProgress);
            this.jawLeft.rotateAngleX = 1.0472F * (1.0F - this.swingProgress);
         } else {
            this.jawRight.rotateAngleX = 0.0F;
            this.jawLeft.rotateAngleX = 0.0F;
         }

      }

      private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class SummonedBeastRenderer extends RenderLiving<EntitySummonedBeast> {
      private static final ResourceLocation BEAST_TEXTURE = new ResourceLocation("narutomod:textures/dog.png");

      public SummonedBeastRenderer(RenderManager renderManager) {
         super(renderManager, new ModelSummonDog(), 2.0F);
      }

      protected ResourceLocation getEntityTexture(EntitySummonedBeast entity) {
         return BEAST_TEXTURE;
      }

      protected void preRenderCallback(EntitySummonedBeast entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         GlStateManager.scale(3.0F, 3.0F, 3.0F);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelSummonBird extends ModelBase {
      private final ModelRenderer body;
      private final ModelRenderer head;
      private final ModelRenderer bone36;
      private final ModelRenderer bone2;
      private final ModelRenderer upperBeak;
      private final ModelRenderer bone4;
      private final ModelRenderer bone5;
      private final ModelRenderer bone6;
      private final ModelRenderer bone7;
      private final ModelRenderer bone13;
      private final ModelRenderer bone12;
      private final ModelRenderer bone11;
      private final ModelRenderer lowerBeak;
      private final ModelRenderer bone8;
      private final ModelRenderer bone9;
      private final ModelRenderer bone10;
      private final ModelRenderer bone14;
      private final ModelRenderer bone15;
      private final ModelRenderer bone16;
      private final ModelRenderer bone20;
      private final ModelRenderer spike3;
      private final ModelRenderer spike4;
      private final ModelRenderer torsoRight;
      private final ModelRenderer torsoLeft;
      private final ModelRenderer wingRight;
      private final ModelRenderer bone34;
      private final ModelRenderer wingTipRight;
      private final ModelRenderer wingLeft;
      private final ModelRenderer bone3;
      private final ModelRenderer wingTipLeft;
      private final ModelRenderer legRight;
      private final ModelRenderer bone21;
      private final ModelRenderer bone22;
      private final ModelRenderer bone23;
      private final ModelRenderer bone24;
      private final ModelRenderer bone31;
      private final ModelRenderer bone32;
      private final ModelRenderer bone33;
      private final ModelRenderer bone25;
      private final ModelRenderer bone26;
      private final ModelRenderer bone27;
      private final ModelRenderer bone28;
      private final ModelRenderer bone29;
      private final ModelRenderer bone30;
      private final ModelRenderer legLeft;
      private final ModelRenderer bone19;
      private final ModelRenderer bone37;
      private final ModelRenderer bone38;
      private final ModelRenderer bone39;
      private final ModelRenderer bone40;
      private final ModelRenderer bone41;
      private final ModelRenderer bone42;
      private final ModelRenderer bone43;
      private final ModelRenderer bone44;
      private final ModelRenderer bone45;
      private final ModelRenderer bone46;
      private final ModelRenderer bone47;
      private final ModelRenderer bone48;
      private final ModelRenderer legTop;
      private final ModelRenderer bone17;
      private final ModelRenderer bone51;
      private final ModelRenderer bone52;
      private final ModelRenderer bone53;
      private final ModelRenderer bone54;
      private final ModelRenderer bone55;
      private final ModelRenderer bone56;
      private final ModelRenderer bone57;
      private final ModelRenderer bone58;
      private final ModelRenderer bone59;
      private final ModelRenderer bone60;
      private final ModelRenderer bone61;
      private final ModelRenderer bone62;
      private final ModelRenderer spike;
      private final ModelRenderer spike2;
      private final ModelRenderer tail;
      private final ModelRenderer tail1;
      private final ModelRenderer tail1a;
      private final ModelRenderer tail2;
      private final ModelRenderer tail2a;
      private final ModelRenderer tail3;
      private final ModelRenderer tail3a;

      public ModelSummonBird() {
         this.textureWidth = 64;
         this.textureHeight = 64;
         (this.body = new ModelRenderer(this)).setRotationPoint(0.0F, 11.0F, 0.0F);
         this.setRotationAngle(this.body, -0.7854F, 0.0F, 0.0F);
         (this.head = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.body.addChild(this.head);
         this.setRotationAngle(this.head, 0.7854F, 0.0F, 0.0F);
         this.head.cubeList.add(new ModelBox(this.head, 0, 55, -1.5F, -1.6328F, -3.8875F, 3, 3, 4, -0.1F, false));
         this.head.cubeList.add(new ModelBox(this.head, 0, 12, -1.5F, -1.6328F, -5.6875F, 3, 3, 2, -0.1F, false));
         (this.bone36 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.1328F, -5.1016F);
         this.head.addChild(this.bone36);
         this.setRotationAngle(this.bone36, -1.0472F, 0.0F, 0.0F);
         (this.bone2 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bone36.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.0F, -0.7854F, 0.0F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, 11, -5.0F, -6.0F, -5.0F, 10, 11, 10, -3.5F, false));
         (this.upperBeak = new ModelRenderer(this)).setRotationPoint(0.0F, 0.25F, -6.0F);
         this.head.addChild(this.upperBeak);
         (this.bone4 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -8.0F);
         this.upperBeak.addChild(this.bone4);
         this.setRotationAngle(this.bone4, 0.0F, -0.2618F, 0.7854F);
         this.bone4.cubeList.add(new ModelBox(this.bone4, 50, -4, 0.0F, -2.0F, 0.0F, 0, 4, 4, 0.0F, false));
         (this.bone5 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 4.0F);
         this.bone4.addChild(this.bone5);
         this.setRotationAngle(this.bone5, 0.0F, 0.1745F, 0.0F);
         this.bone5.cubeList.add(new ModelBox(this.bone5, 50, -1, 0.0F, -2.0F, 0.0F, 0, 4, 5, 0.0F, false));
         (this.bone6 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -8.0F);
         this.upperBeak.addChild(this.bone6);
         this.setRotationAngle(this.bone6, 0.0F, 0.2618F, -0.7854F);
         this.bone6.cubeList.add(new ModelBox(this.bone6, 50, -4, 0.0F, -2.0F, 0.0F, 0, 4, 4, 0.0F, true));
         (this.bone7 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 4.0F);
         this.bone6.addChild(this.bone7);
         this.setRotationAngle(this.bone7, 0.0F, -0.1745F, 0.0F);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 50, -1, 0.0F, -2.0F, 0.0F, 0, 4, 5, 0.0F, true));
         (this.bone13 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.75F, -2.2F);
         this.upperBeak.addChild(this.bone13);
         this.setRotationAngle(this.bone13, 0.5236F, 0.0F, 0.0F);
         (this.bone12 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -0.05F);
         this.bone13.addChild(this.bone12);
         this.setRotationAngle(this.bone12, 0.0F, 0.0F, 0.5236F);
         this.bone12.cubeList.add(new ModelBox(this.bone12, 50, 4, 0.0F, 0.0F, 0.0F, 0, 3, 4, 0.0F, true));
         (this.bone11 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -0.05F);
         this.bone13.addChild(this.bone11);
         this.setRotationAngle(this.bone11, 0.0F, 0.0F, -0.5236F);
         this.bone11.cubeList.add(new ModelBox(this.bone11, 50, 4, 0.0F, 0.0F, 0.0F, 0, 3, 4, 0.0F, false));
         (this.lowerBeak = new ModelRenderer(this)).setRotationPoint(0.0F, 0.25F, -6.0F);
         this.head.addChild(this.lowerBeak);
         this.setRotationAngle(this.lowerBeak, -0.5236F, 0.0F, -3.1416F);
         (this.bone8 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -8.0F);
         this.lowerBeak.addChild(this.bone8);
         this.setRotationAngle(this.bone8, 0.0F, -0.2618F, 0.7854F);
         this.bone8.cubeList.add(new ModelBox(this.bone8, 50, -4, 0.0F, -2.0F, 0.0F, 0, 4, 4, 0.0F, false));
         (this.bone9 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 4.0F);
         this.bone8.addChild(this.bone9);
         this.setRotationAngle(this.bone9, 0.0F, 0.1745F, 0.0F);
         this.bone9.cubeList.add(new ModelBox(this.bone9, 50, -1, 0.0F, -2.0F, 0.0F, 0, 4, 5, 0.0F, false));
         (this.bone10 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -8.0F);
         this.lowerBeak.addChild(this.bone10);
         this.setRotationAngle(this.bone10, 0.0F, 0.2618F, -0.7854F);
         this.bone10.cubeList.add(new ModelBox(this.bone10, 50, -4, 0.0F, -2.0F, 0.0F, 0, 4, 4, 0.0F, true));
         (this.bone14 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 4.0F);
         this.bone10.addChild(this.bone14);
         this.setRotationAngle(this.bone14, 0.0F, -0.1745F, 0.0F);
         this.bone14.cubeList.add(new ModelBox(this.bone14, 50, -1, 0.0F, -2.0F, 0.0F, 0, 4, 5, 0.0F, true));
         (this.bone15 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.75F, -2.2F);
         this.lowerBeak.addChild(this.bone15);
         this.setRotationAngle(this.bone15, 0.5236F, 0.0F, 0.0F);
         (this.bone16 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -0.05F);
         this.bone15.addChild(this.bone16);
         this.setRotationAngle(this.bone16, 0.0F, 0.0F, 0.5236F);
         this.bone16.cubeList.add(new ModelBox(this.bone16, 50, 4, 0.0F, 0.0F, 0.0F, 0, 3, 4, 0.0F, true));
         (this.bone20 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -0.05F);
         this.bone15.addChild(this.bone20);
         this.setRotationAngle(this.bone20, 0.0F, 0.0F, -0.5236F);
         this.bone20.cubeList.add(new ModelBox(this.bone20, 50, 4, 0.0F, 0.0F, 0.0F, 0, 3, 4, 0.0F, false));
         (this.spike3 = new ModelRenderer(this)).setRotationPoint(-0.2881F, -1.2407F, -1.2676F);
         this.head.addChild(this.spike3);
         this.setRotationAngle(this.spike3, -0.3491F, -0.7854F, 0.0F);
         this.spike3.cubeList.add(new ModelBox(this.spike3, 26, 56, -1.0F, -2.0F, -1.0F, 2, 4, 2, -0.3F, false));
         (this.spike4 = new ModelRenderer(this)).setRotationPoint(0.0F, 3.25F, 0.0F);
         this.spike3.addChild(this.spike4);
         this.spike4.cubeList.add(new ModelBox(this.spike4, 26, 56, -1.0F, -2.5F, -1.0F, 2, 4, 2, -0.5F, false));
         (this.torsoRight = new ModelRenderer(this)).setRotationPoint(0.5391F, 0.5F, -0.4688F);
         this.body.addChild(this.torsoRight);
         this.setRotationAngle(this.torsoRight, 0.0F, 0.1309F, 0.0F);
         this.torsoRight.cubeList.add(new ModelBox(this.torsoRight, 30, 0, -4.4957F, -2.5F, -0.0653F, 4, 5, 12, 0.0F, false));
         (this.torsoLeft = new ModelRenderer(this)).setRotationPoint(-0.5391F, 0.5F, -0.4688F);
         this.body.addChild(this.torsoLeft);
         this.setRotationAngle(this.torsoLeft, 0.0F, -0.1309F, 0.0F);
         this.torsoLeft.cubeList.add(new ModelBox(this.torsoLeft, 30, 0, 0.4957F, -2.5F, -0.0653F, 4, 5, 12, 0.0F, true));
         (this.wingRight = new ModelRenderer(this)).setRotationPoint(-2.0F, -0.5F, 2.0F);
         this.body.addChild(this.wingRight);
         this.setRotationAngle(this.wingRight, 0.0F, 0.0F, 0.7854F);
         (this.bone34 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.wingRight.addChild(this.bone34);
         this.bone34.cubeList.add(new ModelBox(this.bone34, 0, 32, -13.0234F, -0.9375F, -1.25F, 13, 2, 2, 0.0F, false));
         this.bone34.cubeList.add(new ModelBox(this.bone34, -11, 36, -13.0234F, 0.075F, 0.6875F, 13, 0, 11, 0.001F, false));
         (this.wingTipRight = new ModelRenderer(this)).setRotationPoint(-12.9531F, 0.1156F, -1.2656F);
         this.bone34.addChild(this.wingTipRight);
         this.setRotationAngle(this.wingTipRight, 0.0F, 0.2618F, -2.0944F);
         this.wingTipRight.cubeList.add(new ModelBox(this.wingTipRight, 8, 62, -8.9297F, -0.4594F, 0.3156F, 9, 1, 1, 0.25F, false));
         this.wingTipRight.cubeList.add(new ModelBox(this.wingTipRight, 9, 62, -16.9297F, -0.4594F, 0.0156F, 8, 1, 1, 0.0F, false));
         this.wingTipRight.cubeList.add(new ModelBox(this.wingTipRight, -11, 0, -17.0703F, -0.0406F, 0.9844F, 17, 0, 11, 0.001F, false));
         (this.wingLeft = new ModelRenderer(this)).setRotationPoint(2.0F, -0.5F, 2.0F);
         this.body.addChild(this.wingLeft);
         this.setRotationAngle(this.wingLeft, 0.0F, 0.0F, -0.7854F);
         (this.bone3 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.wingLeft.addChild(this.bone3);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 0, 32, 0.0234F, -0.9375F, -1.25F, 13, 2, 2, 0.0F, true));
         this.bone3.cubeList.add(new ModelBox(this.bone3, -11, 36, 0.0234F, 0.075F, 0.6875F, 13, 0, 11, 0.001F, true));
         (this.wingTipLeft = new ModelRenderer(this)).setRotationPoint(12.9531F, 0.1156F, -1.2656F);
         this.bone3.addChild(this.wingTipLeft);
         this.setRotationAngle(this.wingTipLeft, 0.0F, -0.2618F, 2.0944F);
         this.wingTipLeft.cubeList.add(new ModelBox(this.wingTipLeft, 8, 62, -0.0703F, -0.4594F, 0.3156F, 9, 1, 1, 0.25F, true));
         this.wingTipLeft.cubeList.add(new ModelBox(this.wingTipLeft, 9, 62, 8.9297F, -0.4594F, 0.0156F, 8, 1, 1, 0.0F, true));
         this.wingTipLeft.cubeList.add(new ModelBox(this.wingTipLeft, -11, 0, 0.0703F, -0.0406F, 0.9844F, 17, 0, 11, 0.001F, true));
         (this.legRight = new ModelRenderer(this)).setRotationPoint(-2.6693F, 2.5938F, 7.1771F);
         this.body.addChild(this.legRight);
         this.setRotationAngle(this.legRight, 0.5236F, 0.0F, 0.2618F);
         this.legRight.cubeList.add(new ModelBox(this.legRight, 14, 55, -1.5729F, -1.0938F, -1.3333F, 3, 4, 3, 0.0F, false));
         (this.bone21 = new ModelRenderer(this)).setRotationPoint(-0.2135F, 3.0469F, -0.0833F);
         this.legRight.addChild(this.bone21);
         this.setRotationAngle(this.bone21, 0.2618F, 0.2618F, -0.1745F);
         this.bone21.cubeList.add(new ModelBox(this.bone21, 36, 0, -0.4688F, -1.2031F, -0.3984F, 1, 4, 1, 0.0F, false));
         (this.bone22 = new ModelRenderer(this)).setRotationPoint(0.0688F, 2.7531F, 0.1484F);
         this.bone21.addChild(this.bone22);
         this.setRotationAngle(this.bone22, -0.2618F, 0.0F, 0.0F);
         this.bone22.cubeList.add(new ModelBox(this.bone22, 26, 43, -0.5F, -0.25F, -2.75F, 1, 1, 3, -0.2F, false));
         (this.bone23 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.0F, -2.5F);
         this.bone22.addChild(this.bone23);
         this.setRotationAngle(this.bone23, 0.5236F, 0.0F, 0.0F);
         this.bone23.cubeList.add(new ModelBox(this.bone23, 26, 40, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.3F, false));
         (this.bone24 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.4F);
         this.bone23.addChild(this.bone24);
         this.setRotationAngle(this.bone24, 0.5236F, 0.0F, 0.0F);
         this.bone24.cubeList.add(new ModelBox(this.bone24, 31, 43, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.4F, false));
         (this.bone31 = new ModelRenderer(this)).setRotationPoint(0.0688F, 2.7531F, -0.1016F);
         this.bone21.addChild(this.bone31);
         this.setRotationAngle(this.bone31, 0.0F, -2.618F, 0.0F);
         this.bone31.cubeList.add(new ModelBox(this.bone31, 26, 43, -0.5F, -0.25F, -2.75F, 1, 1, 3, -0.2F, false));
         (this.bone32 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.0F, -2.5F);
         this.bone31.addChild(this.bone32);
         this.setRotationAngle(this.bone32, 0.5236F, 0.0F, 0.0F);
         this.bone32.cubeList.add(new ModelBox(this.bone32, 26, 40, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.3F, false));
         (this.bone33 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.4F);
         this.bone32.addChild(this.bone33);
         this.setRotationAngle(this.bone33, 0.5236F, 0.0F, 0.0F);
         this.bone33.cubeList.add(new ModelBox(this.bone33, 31, 43, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.4F, false));
         (this.bone25 = new ModelRenderer(this)).setRotationPoint(0.0688F, 2.7531F, 0.3984F);
         this.bone21.addChild(this.bone25);
         this.setRotationAngle(this.bone25, -0.1745F, 0.3491F, 0.0F);
         this.bone25.cubeList.add(new ModelBox(this.bone25, 26, 43, -0.5F, -0.25F, -2.75F, 1, 1, 3, -0.2F, false));
         (this.bone26 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.0F, -2.5F);
         this.bone25.addChild(this.bone26);
         this.setRotationAngle(this.bone26, 0.5236F, 0.0F, 0.0F);
         this.bone26.cubeList.add(new ModelBox(this.bone26, 26, 40, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.3F, false));
         (this.bone27 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.4F);
         this.bone26.addChild(this.bone27);
         this.setRotationAngle(this.bone27, 0.5236F, 0.0F, 0.0F);
         this.bone27.cubeList.add(new ModelBox(this.bone27, 31, 43, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.4F, false));
         (this.bone28 = new ModelRenderer(this)).setRotationPoint(0.0688F, 2.7531F, 0.3984F);
         this.bone21.addChild(this.bone28);
         this.setRotationAngle(this.bone28, -0.1745F, -0.3491F, 0.0F);
         this.bone28.cubeList.add(new ModelBox(this.bone28, 26, 43, -0.5F, -0.25F, -2.75F, 1, 1, 3, -0.2F, false));
         (this.bone29 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.0F, -2.5F);
         this.bone28.addChild(this.bone29);
         this.setRotationAngle(this.bone29, 0.5236F, 0.0F, 0.0F);
         this.bone29.cubeList.add(new ModelBox(this.bone29, 26, 40, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.3F, false));
         (this.bone30 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.4F);
         this.bone29.addChild(this.bone30);
         this.setRotationAngle(this.bone30, 0.5236F, 0.0F, 0.0F);
         this.bone30.cubeList.add(new ModelBox(this.bone30, 31, 43, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.4F, false));
         (this.legLeft = new ModelRenderer(this)).setRotationPoint(2.6693F, 2.5938F, 7.1771F);
         this.body.addChild(this.legLeft);
         this.setRotationAngle(this.legLeft, 0.5236F, 0.0F, -0.2618F);
         this.legLeft.cubeList.add(new ModelBox(this.legLeft, 14, 55, -1.4271F, -1.0938F, -1.3333F, 3, 4, 3, 0.0F, true));
         (this.bone19 = new ModelRenderer(this)).setRotationPoint(0.2135F, 3.0469F, -0.0833F);
         this.legLeft.addChild(this.bone19);
         this.setRotationAngle(this.bone19, 0.2618F, -0.2618F, 0.1745F);
         this.bone19.cubeList.add(new ModelBox(this.bone19, 36, 0, -0.5313F, -1.2031F, -0.3984F, 1, 4, 1, 0.0F, true));
         (this.bone37 = new ModelRenderer(this)).setRotationPoint(-0.0688F, 2.7531F, 0.1484F);
         this.bone19.addChild(this.bone37);
         this.setRotationAngle(this.bone37, -0.2618F, 0.0F, 0.0F);
         this.bone37.cubeList.add(new ModelBox(this.bone37, 26, 43, -0.5F, -0.25F, -2.75F, 1, 1, 3, -0.2F, true));
         (this.bone38 = new ModelRenderer(this)).setRotationPoint(0.5F, 0.0F, -2.5F);
         this.bone37.addChild(this.bone38);
         this.setRotationAngle(this.bone38, 0.5236F, 0.0F, 0.0F);
         this.bone38.cubeList.add(new ModelBox(this.bone38, 26, 40, -1.0F, -0.25F, -1.75F, 1, 1, 2, -0.3F, true));
         (this.bone39 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.4F);
         this.bone38.addChild(this.bone39);
         this.setRotationAngle(this.bone39, 0.5236F, 0.0F, 0.0F);
         this.bone39.cubeList.add(new ModelBox(this.bone39, 31, 43, -1.0F, -0.25F, -1.75F, 1, 1, 2, -0.4F, true));
         (this.bone40 = new ModelRenderer(this)).setRotationPoint(-0.0688F, 2.7531F, -0.1016F);
         this.bone19.addChild(this.bone40);
         this.setRotationAngle(this.bone40, 0.0F, 2.618F, 0.0F);
         this.bone40.cubeList.add(new ModelBox(this.bone40, 26, 43, -0.5F, -0.25F, -2.75F, 1, 1, 3, -0.2F, true));
         (this.bone41 = new ModelRenderer(this)).setRotationPoint(0.5F, 0.0F, -2.5F);
         this.bone40.addChild(this.bone41);
         this.setRotationAngle(this.bone41, 0.5236F, 0.0F, 0.0F);
         this.bone41.cubeList.add(new ModelBox(this.bone41, 26, 40, -1.0F, -0.25F, -1.75F, 1, 1, 2, -0.3F, true));
         (this.bone42 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.4F);
         this.bone41.addChild(this.bone42);
         this.setRotationAngle(this.bone42, 0.5236F, 0.0F, 0.0F);
         this.bone42.cubeList.add(new ModelBox(this.bone42, 31, 43, -1.0F, -0.25F, -1.75F, 1, 1, 2, -0.4F, true));
         (this.bone43 = new ModelRenderer(this)).setRotationPoint(-0.0688F, 2.7531F, 0.3984F);
         this.bone19.addChild(this.bone43);
         this.setRotationAngle(this.bone43, -0.1745F, -0.3491F, 0.0F);
         this.bone43.cubeList.add(new ModelBox(this.bone43, 26, 43, -0.5F, -0.25F, -2.75F, 1, 1, 3, -0.2F, true));
         (this.bone44 = new ModelRenderer(this)).setRotationPoint(0.5F, 0.0F, -2.5F);
         this.bone43.addChild(this.bone44);
         this.setRotationAngle(this.bone44, 0.5236F, 0.0F, 0.0F);
         this.bone44.cubeList.add(new ModelBox(this.bone44, 26, 40, -1.0F, -0.25F, -1.75F, 1, 1, 2, -0.3F, true));
         (this.bone45 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.4F);
         this.bone44.addChild(this.bone45);
         this.setRotationAngle(this.bone45, 0.5236F, 0.0F, 0.0F);
         this.bone45.cubeList.add(new ModelBox(this.bone45, 31, 43, -1.0F, -0.25F, -1.75F, 1, 1, 2, -0.4F, true));
         (this.bone46 = new ModelRenderer(this)).setRotationPoint(-0.0688F, 2.7531F, 0.3984F);
         this.bone19.addChild(this.bone46);
         this.setRotationAngle(this.bone46, -0.1745F, 0.3491F, 0.0F);
         this.bone46.cubeList.add(new ModelBox(this.bone46, 26, 43, -0.5F, -0.25F, -2.75F, 1, 1, 3, -0.2F, true));
         (this.bone47 = new ModelRenderer(this)).setRotationPoint(0.5F, 0.0F, -2.5F);
         this.bone46.addChild(this.bone47);
         this.setRotationAngle(this.bone47, 0.5236F, 0.0F, 0.0F);
         this.bone47.cubeList.add(new ModelBox(this.bone47, 26, 40, -1.0F, -0.25F, -1.75F, 1, 1, 2, -0.3F, true));
         (this.bone48 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.4F);
         this.bone47.addChild(this.bone48);
         this.setRotationAngle(this.bone48, 0.5236F, 0.0F, 0.0F);
         this.bone48.cubeList.add(new ModelBox(this.bone48, 31, 43, -1.0F, -0.25F, -1.75F, 1, 1, 2, -0.4F, true));
         (this.legTop = new ModelRenderer(this)).setRotationPoint(-0.1693F, -1.4063F, 8.1771F);
         this.body.addChild(this.legTop);
         this.setRotationAngle(this.legTop, 0.5236F, 0.0F, -3.1416F);
         this.legTop.cubeList.add(new ModelBox(this.legTop, 14, 55, -1.5729F, -1.0938F, -1.3333F, 3, 4, 3, 0.0F, false));
         (this.bone17 = new ModelRenderer(this)).setRotationPoint(-0.2135F, 3.0469F, -0.0833F);
         this.legTop.addChild(this.bone17);
         this.setRotationAngle(this.bone17, 0.4363F, 0.0F, 0.0F);
         this.bone17.cubeList.add(new ModelBox(this.bone17, 36, 0, -0.4688F, -1.2031F, -0.3984F, 1, 4, 1, 0.0F, false));
         (this.bone51 = new ModelRenderer(this)).setRotationPoint(0.0687F, 2.7531F, -0.1016F);
         this.bone17.addChild(this.bone51);
         this.setRotationAngle(this.bone51, 1.0472F, 0.0F, 0.0F);
         this.bone51.cubeList.add(new ModelBox(this.bone51, 26, 43, -0.5F, -0.25F, -2.75F, 1, 1, 3, -0.2F, false));
         (this.bone52 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.0F, -2.5F);
         this.bone51.addChild(this.bone52);
         this.setRotationAngle(this.bone52, 1.0472F, 0.0F, 0.0F);
         this.bone52.cubeList.add(new ModelBox(this.bone52, 26, 40, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.3F, false));
         (this.bone53 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.4F);
         this.bone52.addChild(this.bone53);
         this.setRotationAngle(this.bone53, 1.0472F, 0.0F, 0.0F);
         this.bone53.cubeList.add(new ModelBox(this.bone53, 31, 43, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.4F, false));
         (this.bone54 = new ModelRenderer(this)).setRotationPoint(0.0687F, 2.7531F, -0.1016F);
         this.bone17.addChild(this.bone54);
         this.setRotationAngle(this.bone54, 1.5708F, -0.2618F, -0.6981F);
         this.bone54.cubeList.add(new ModelBox(this.bone54, 26, 43, -0.5F, -0.25F, -2.75F, 1, 1, 3, -0.2F, false));
         (this.bone55 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.0F, -2.5F);
         this.bone54.addChild(this.bone55);
         this.setRotationAngle(this.bone55, 1.0472F, 0.0F, 0.0F);
         this.bone55.cubeList.add(new ModelBox(this.bone55, 26, 40, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.3F, false));
         (this.bone56 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.4F);
         this.bone55.addChild(this.bone56);
         this.setRotationAngle(this.bone56, 1.0472F, 0.0F, 0.0F);
         this.bone56.cubeList.add(new ModelBox(this.bone56, 31, 43, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.4F, false));
         (this.bone57 = new ModelRenderer(this)).setRotationPoint(0.0687F, 2.7531F, 0.3984F);
         this.bone17.addChild(this.bone57);
         this.setRotationAngle(this.bone57, 1.0472F, 0.0F, 0.3491F);
         this.bone57.cubeList.add(new ModelBox(this.bone57, 26, 43, -0.5F, -0.25F, -2.75F, 1, 1, 3, -0.2F, false));
         (this.bone58 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.0F, -2.5F);
         this.bone57.addChild(this.bone58);
         this.setRotationAngle(this.bone58, 1.0472F, 0.0F, 0.0F);
         this.bone58.cubeList.add(new ModelBox(this.bone58, 26, 40, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.3F, false));
         (this.bone59 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.4F);
         this.bone58.addChild(this.bone59);
         this.setRotationAngle(this.bone59, 1.0472F, 0.0F, 0.0F);
         this.bone59.cubeList.add(new ModelBox(this.bone59, 31, 43, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.4F, false));
         (this.bone60 = new ModelRenderer(this)).setRotationPoint(0.0687F, 2.7531F, 0.3984F);
         this.bone17.addChild(this.bone60);
         this.setRotationAngle(this.bone60, 1.0472F, 0.0F, -0.3491F);
         this.bone60.cubeList.add(new ModelBox(this.bone60, 26, 43, -0.5F, -0.25F, -2.75F, 1, 1, 3, -0.2F, false));
         (this.bone61 = new ModelRenderer(this)).setRotationPoint(-0.5F, 0.0F, -2.5F);
         this.bone60.addChild(this.bone61);
         this.setRotationAngle(this.bone61, 1.0472F, 0.0F, 0.0F);
         this.bone61.cubeList.add(new ModelBox(this.bone61, 26, 40, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.3F, false));
         (this.bone62 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.4F);
         this.bone61.addChild(this.bone62);
         this.setRotationAngle(this.bone62, 1.0472F, 0.0F, 0.0F);
         this.bone62.cubeList.add(new ModelBox(this.bone62, 31, 43, 0.0F, -0.25F, -1.75F, 1, 1, 2, -0.4F, false));
         (this.spike = new ModelRenderer(this)).setRotationPoint(0.7119F, -1.2407F, 3.7324F);
         this.body.addChild(this.spike);
         this.setRotationAngle(this.spike, -0.3491F, 0.7854F, 0.0F);
         this.spike.cubeList.add(new ModelBox(this.spike, 26, 56, -1.0F, -2.0F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.spike2 = new ModelRenderer(this)).setRotationPoint(0.0F, 5.25F, 0.0F);
         this.spike.addChild(this.spike2);
         this.spike2.cubeList.add(new ModelBox(this.spike2, 26, 56, -1.0F, -2.5F, -1.0F, 2, 4, 2, -0.4F, false));
         (this.tail = new ModelRenderer(this)).setRotationPoint(0.0781F, 0.3406F, 11.5313F);
         this.body.addChild(this.tail);
         this.setRotationAngle(this.tail, 0.5236F, 0.0F, 0.0F);
         (this.tail1 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.0F, 0.0F);
         this.tail.addChild(this.tail1);
         this.setRotationAngle(this.tail1, 0.0873F, 0.0F, 0.0F);
         this.tail1.cubeList.add(new ModelBox(this.tail1, -8, 47, -4.0F, 0.0F, -1.0F, 8, 0, 8, 0.0F, false));
         (this.tail1a = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.tail1.addChild(this.tail1a);
         this.setRotationAngle(this.tail1a, -0.2618F, 0.0F, 0.0F);
         this.tail1a.cubeList.add(new ModelBox(this.tail1a, 8, 47, -4.0F, 0.0F, 0.0F, 8, 0, 8, 0.0F, false));
         (this.tail2 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.tail.addChild(this.tail2);
         this.setRotationAngle(this.tail2, 0.0F, 0.3491F, -0.2618F);
         this.tail2.cubeList.add(new ModelBox(this.tail2, -8, 47, -4.0F, 0.0F, -1.0F, 8, 0, 8, 0.0F, false));
         (this.tail2a = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.tail2.addChild(this.tail2a);
         this.setRotationAngle(this.tail2a, -0.2618F, 0.0F, 0.0F);
         this.tail2a.cubeList.add(new ModelBox(this.tail2a, 8, 47, -4.0F, 0.0F, 0.0F, 8, 0, 8, 0.0F, false));
         (this.tail3 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.tail.addChild(this.tail3);
         this.setRotationAngle(this.tail3, -0.0873F, -0.3491F, 0.2618F);
         this.tail3.cubeList.add(new ModelBox(this.tail3, -8, 47, -4.0F, 0.0F, -1.0F, 8, 0, 8, 0.0F, false));
         (this.tail3a = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.tail3.addChild(this.tail3a);
         this.setRotationAngle(this.tail3a, -0.2618F, 0.0F, 0.0F);
         this.tail3a.cubeList.add(new ModelBox(this.tail3a, 8, 47, -4.0F, 0.0F, 0.0F, 8, 0, 8, 0.0F, false));
      }

      public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
         this.body.render(scale);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
         if (entity.onGround) {
            this.body.setRotationPoint(0.0F, 11.0F, 0.0F);
            this.setRotationAngle(this.body, -0.7854F, 0.0F, 0.0F);
            this.setRotationAngle(this.head, 0.7854F, 0.0F, 0.0F);
            this.setRotationAngle(this.wingRight, 0.0F, 1.5708F, 0.2618F);
            this.setRotationAngle(this.wingTipRight, -2.4435F, -2.9671F, -0.0873F);
            this.setRotationAngle(this.wingLeft, 0.0F, -1.5708F, -0.2618F);
            this.setRotationAngle(this.wingTipLeft, -2.4435F, 2.9671F, 0.0873F);
            this.setRotationAngle(this.legRight, 0.5236F, 0.0F, 0.2618F);
            this.setRotationAngle(this.legLeft, 0.5236F, 0.0F, -0.2618F);
            this.tail.rotateAngleX = 0.5236F;
            this.lowerBeak.rotateAngleX = -0.5236F;
         } else {
            this.body.setRotationPoint(0.0F, 21.0F, 0.0F);
            this.setRotationAngle(this.body, 0.0F, 0.0F, 0.0F);
            this.setRotationAngle(this.head, 0.0F, 0.0F, 0.0F);
            float wingFlapAngle = MathHelper.cos(ageInTicks * 0.3F) * 0.65F;
            this.setRotationAngle(this.wingRight, 0.0F, 0.0F, wingFlapAngle);
            this.setRotationAngle(this.wingLeft, 0.0F, 0.0F, -wingFlapAngle);
            wingFlapAngle = MathHelper.cos((ageInTicks - 3.0F) * 0.3F) * 0.845F;
            this.setRotationAngle(this.wingTipRight, 0.0F, 0.2618F, wingFlapAngle - 0.2618F);
            this.setRotationAngle(this.wingTipLeft, 0.0F, -0.2618F, -wingFlapAngle + 0.2618F);
            this.setRotationAngle(this.legRight, 1.0472F, 0.0F, 0.2618F);
            this.setRotationAngle(this.legLeft, 1.0472F, 0.0F, -0.2618F);
            this.tail.rotateAngleX = 0.0F;
            this.lowerBeak.rotateAngleX = 0.0F;
         }

         this.tail1.rotateAngleX = MathHelper.sin(ageInTicks * 0.09F) * 0.09F + 0.0873F;
         this.tail2.rotateAngleZ = MathHelper.cos(ageInTicks * 0.067F) * 0.09F - 0.2618F;
         this.tail3.rotateAngleZ = -MathHelper.cos(ageInTicks * 0.067F) * 0.09F + 0.2618F;
         this.head.rotateAngleY = netHeadYaw * ((float)Math.PI / 180F);
         ModelRenderer var10000 = this.head;
         var10000.rotateAngleX += headPitch * ((float)Math.PI / 180F);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelSummonChameleon extends ModelBase {
      private final ModelRenderer head;
      private final ModelRenderer upperJaw;
      private final ModelRenderer bone14;
      private final ModelRenderer bone18;
      private final ModelRenderer bone7;
      private final ModelRenderer bone11;
      private final ModelRenderer bone73;
      private final ModelRenderer rightEye;
      private final ModelRenderer cube_r1;
      private final ModelRenderer leftEye;
      private final ModelRenderer cube_r2;
      private final ModelRenderer lowerJaw;
      private final ModelRenderer bone16;
      private final ModelRenderer bone13;
      private final ModelRenderer bone74;
      private final ModelRenderer bone12;
      private final ModelRenderer bone5;
      private final ModelRenderer hornRight;
      private final ModelRenderer hornLeft;
      private final ModelRenderer hornLeft2;
      private final ModelRenderer hornLeft3;
      private final ModelRenderer hornLeft4;
      private final ModelRenderer body;
      private final ModelRenderer bone9;
      private final ModelRenderer bone10;
      private final ModelRenderer bone61;
      private final ModelRenderer bone8;
      private final ModelRenderer bone3;
      private final ModelRenderer bone6;
      private final ModelRenderer bone4;
      private final ModelRenderer wingRight;
      private final ModelRenderer cube_r3;
      private final ModelRenderer cube_r4;
      private final ModelRenderer cube_r5;
      private final ModelRenderer wingLeft;
      private final ModelRenderer cube_r6;
      private final ModelRenderer cube_r7;
      private final ModelRenderer cube_r8;
      private final ModelRenderer[] tail;
      private final Vector3f[] tailPreset1;
      private final ModelRenderer snakeHead;
      private final ModelRenderer bone;
      private final ModelRenderer bone39;
      private final ModelRenderer bone62;
      private final ModelRenderer bone63;
      private final ModelRenderer snakeJaw;
      private final ModelRenderer bone64;
      private final ModelRenderer bone70;
      private final ModelRenderer bone75;
      private final ModelRenderer bone76;
      private final ModelRenderer leg1;
      private final ModelRenderer bone27;
      private final ModelRenderer cube_r9;
      private final ModelRenderer bone28;
      private final ModelRenderer cube_r10;
      private final ModelRenderer bone2;
      private final ModelRenderer cube_r11;
      private final ModelRenderer bone19;
      private final ModelRenderer bone20;
      private final ModelRenderer bone32;
      private final ModelRenderer bone33;
      private final ModelRenderer bone34;
      private final ModelRenderer bone35;
      private final ModelRenderer bone36;
      private final ModelRenderer bone37;
      private final ModelRenderer leg2;
      private final ModelRenderer bone15;
      private final ModelRenderer cube_r12;
      private final ModelRenderer bone17;
      private final ModelRenderer cube_r13;
      private final ModelRenderer bone21;
      private final ModelRenderer cube_r14;
      private final ModelRenderer bone22;
      private final ModelRenderer bone23;
      private final ModelRenderer bone24;
      private final ModelRenderer bone25;
      private final ModelRenderer bone26;
      private final ModelRenderer bone29;
      private final ModelRenderer bone30;
      private final ModelRenderer bone31;
      private final ModelRenderer leg3;
      private final ModelRenderer bone38;
      private final ModelRenderer cube_r15;
      private final ModelRenderer bone40;
      private final ModelRenderer cube_r16;
      private final ModelRenderer bone41;
      private final ModelRenderer cube_r17;
      private final ModelRenderer bone42;
      private final ModelRenderer bone43;
      private final ModelRenderer bone44;
      private final ModelRenderer bone45;
      private final ModelRenderer bone46;
      private final ModelRenderer bone47;
      private final ModelRenderer bone48;
      private final ModelRenderer bone49;
      private final ModelRenderer leg4;
      private final ModelRenderer bone50;
      private final ModelRenderer cube_r18;
      private final ModelRenderer bone51;
      private final ModelRenderer cube_r19;
      private final ModelRenderer bone52;
      private final ModelRenderer cube_r20;
      private final ModelRenderer bone53;
      private final ModelRenderer bone54;
      private final ModelRenderer bone55;
      private final ModelRenderer bone56;
      private final ModelRenderer bone57;
      private final ModelRenderer bone58;
      private final ModelRenderer bone59;
      private final ModelRenderer bone60;

      public ModelSummonChameleon() {
         this.textureWidth = 64;
         this.textureHeight = 64;
         this.tail = new ModelRenderer[8];
         this.tailPreset1 = new Vector3f[]{new Vector3f(0.5236F, 0.0F, 0.0F), new Vector3f(0.7854F, 0.0F, 0.0F), new Vector3f(0.5236F, 0.0F, 0.0F), new Vector3f(0.5236F, 0.0F, 0.0F), new Vector3f(0.5236F, 0.0F, 0.0F), new Vector3f(0.2618F, 0.0F, 0.0F), new Vector3f(0.2618F, 0.0F, 0.0F), new Vector3f(0.0873F, 0.0F, 0.0F)};
         (this.head = new ModelRenderer(this)).setRotationPoint(0.0F, 18.85F, 0.0F);
         (this.upperJaw = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -4.0F);
         this.head.addChild(this.upperJaw);
         this.setRotationAngle(this.upperJaw, -0.5236F, 0.0F, 0.0F);
         (this.bone14 = new ModelRenderer(this)).setRotationPoint(-2.7F, -1.0F, -0.4F);
         this.upperJaw.addChild(this.bone14);
         this.setRotationAngle(this.bone14, 0.4363F, -0.1745F, 0.0F);
         this.bone14.cubeList.add(new ModelBox(this.bone14, 0, 26, 0.0F, -1.0F, -2.0F, 3, 2, 2, 0.0F, false));
         (this.bone18 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.9F);
         this.bone14.addChild(this.bone18);
         this.setRotationAngle(this.bone18, 0.0873F, -0.2618F, 0.0F);
         this.bone18.cubeList.add(new ModelBox(this.bone18, 8, 49, 0.0F, -1.0F, -2.0F, 3, 2, 2, 0.0F, false));
         (this.bone7 = new ModelRenderer(this)).setRotationPoint(2.7F, -1.0F, -0.4F);
         this.upperJaw.addChild(this.bone7);
         this.setRotationAngle(this.bone7, 0.4363F, 0.1745F, 0.0F);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 0, 26, -3.0F, -1.0F, -2.0F, 3, 2, 2, 0.0F, true));
         (this.bone11 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -1.9F);
         this.bone7.addChild(this.bone11);
         this.setRotationAngle(this.bone11, 0.0873F, 0.2618F, 0.0F);
         this.bone11.cubeList.add(new ModelBox(this.bone11, 8, 49, -3.0F, -1.0F, -2.0F, 3, 2, 2, 0.0F, true));
         (this.bone73 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.1F, -4.1F);
         this.upperJaw.addChild(this.bone73);
         this.setRotationAngle(this.bone73, 0.4363F, 0.0F, 0.0F);
         this.bone73.cubeList.add(new ModelBox(this.bone73, 22, 4, -1.5F, 0.0F, -0.05F, 3, 2, 2, 0.0F, false));
         (this.rightEye = new ModelRenderer(this)).setRotationPoint(-2.3932F, -1.2041F, -1.1912F);
         this.upperJaw.addChild(this.rightEye);
         this.setRotationAngle(this.rightEye, 0.0F, -0.3927F, 0.5236F);
         (this.cube_r1 = new ModelRenderer(this)).setRotationPoint(-1.0F, 1.0F, 1.0F);
         this.rightEye.addChild(this.cube_r1);
         this.setRotationAngle(this.cube_r1, 0.0F, 0.0F, 0.0F);
         this.cube_r1.cubeList.add(new ModelBox(this.cube_r1, 0, 0, 0.0F, -2.0F, -2.0F, 2, 2, 2, -0.2F, false));
         (this.leftEye = new ModelRenderer(this)).setRotationPoint(2.3932F, -1.2041F, -1.1912F);
         this.upperJaw.addChild(this.leftEye);
         this.setRotationAngle(this.leftEye, 0.0F, 0.3927F, -0.5236F);
         (this.cube_r2 = new ModelRenderer(this)).setRotationPoint(5.0F, 5.0F, -3.0F);
         this.leftEye.addChild(this.cube_r2);
         this.setRotationAngle(this.cube_r2, 0.0F, 0.0F, 0.0F);
         this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 24, 44, -10.0F, -10.0F, -2.0F, 10, 10, 10, -4.2F, true));
         (this.lowerJaw = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -4.0F);
         this.head.addChild(this.lowerJaw);
         this.setRotationAngle(this.lowerJaw, 0.5236F, 0.0F, 0.0F);
         this.lowerJaw.cubeList.add(new ModelBox(this.lowerJaw, 17, 46, -2.0F, -1.35F, -3.25F, 4, 2, 3, 0.2F, false));
         (this.bone16 = new ModelRenderer(this)).setRotationPoint(-2.7F, 1.05F, -0.4F);
         this.lowerJaw.addChild(this.bone16);
         this.setRotationAngle(this.bone16, -0.0436F, -0.3491F, 0.0F);
         this.bone16.cubeList.add(new ModelBox(this.bone16, 44, 34, 0.0F, -1.0F, -3.0F, 3, 2, 3, 0.0F, false));
         (this.bone13 = new ModelRenderer(this)).setRotationPoint(2.7F, 1.05F, -0.4F);
         this.lowerJaw.addChild(this.bone13);
         this.setRotationAngle(this.bone13, -0.0436F, 0.3491F, 0.0F);
         this.bone13.cubeList.add(new ModelBox(this.bone13, 44, 34, -3.0F, -1.0F, -3.0F, 3, 2, 3, 0.0F, true));
         (this.bone74 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.92F, -3.75F);
         this.lowerJaw.addChild(this.bone74);
         this.setRotationAngle(this.bone74, 0.4363F, 0.0F, 0.0F);
         this.bone74.cubeList.add(new ModelBox(this.bone74, 40, 21, -1.5F, 0.15F, 0.05F, 3, 1, 1, 0.0F, false));
         (this.bone12 = new ModelRenderer(this)).setRotationPoint(-3.05F, -0.2F, 0.2F);
         this.head.addChild(this.bone12);
         this.setRotationAngle(this.bone12, 0.0F, -0.0873F, 0.0F);
         this.bone12.cubeList.add(new ModelBox(this.bone12, 26, 34, 0.0F, -2.0F, -5.0F, 4, 4, 5, 0.0F, false));
         (this.bone5 = new ModelRenderer(this)).setRotationPoint(3.05F, -0.2F, 0.2F);
         this.head.addChild(this.bone5);
         this.setRotationAngle(this.bone5, 0.0F, 0.0873F, 0.0F);
         this.bone5.cubeList.add(new ModelBox(this.bone5, 26, 34, -4.0F, -2.0F, -5.0F, 4, 4, 5, 0.0F, true));
         (this.hornRight = new ModelRenderer(this)).setRotationPoint(-1.7F, -1.6F, -1.95F);
         this.head.addChild(this.hornRight);
         this.setRotationAngle(this.hornRight, -0.7854F, -0.2618F, -0.5236F);
         this.hornRight.cubeList.add(new ModelBox(this.hornRight, 30, 0, -1.0F, -3.0F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.hornLeft = new ModelRenderer(this)).setRotationPoint(1.7F, -1.6F, -1.95F);
         this.head.addChild(this.hornLeft);
         this.setRotationAngle(this.hornLeft, -0.7854F, 0.2618F, 0.5236F);
         this.hornLeft.cubeList.add(new ModelBox(this.hornLeft, 48, 21, -1.0F, -3.0F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.hornLeft2 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.75F, 0.0F);
         this.hornLeft.addChild(this.hornLeft2);
         this.setRotationAngle(this.hornLeft2, 0.2618F, 0.0F, 0.3927F);
         this.hornLeft2.cubeList.add(new ModelBox(this.hornLeft2, 48, 21, -1.0F, -3.75F, -1.0F, 2, 4, 2, -0.2F, false));
         this.hornLeft2.cubeList.add(new ModelBox(this.hornLeft2, 50, 0, -0.15F, -2.0F, -2.0F, 1, 1, 4, -0.3F, false));
         this.hornLeft2.cubeList.add(new ModelBox(this.hornLeft2, 50, 0, -0.85F, -3.25F, -2.0F, 1, 1, 4, -0.3F, false));
         (this.hornLeft3 = new ModelRenderer(this)).setRotationPoint(0.0F, -3.25F, 0.0F);
         this.hornLeft2.addChild(this.hornLeft3);
         this.setRotationAngle(this.hornLeft3, -0.4363F, 0.0F, -0.5236F);
         this.hornLeft3.cubeList.add(new ModelBox(this.hornLeft3, 48, 21, -1.0F, -3.25F, -1.0F, 2, 4, 2, -0.4F, false));
         this.hornLeft3.cubeList.add(new ModelBox(this.hornLeft3, 51, 1, -0.25F, -1.25F, -1.5F, 1, 1, 3, -0.3F, false));
         this.hornLeft3.cubeList.add(new ModelBox(this.hornLeft3, 51, 1, -0.7F, -2.5F, -1.5F, 1, 1, 3, -0.3F, false));
         (this.hornLeft4 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.75F, 0.0F);
         this.hornLeft3.addChild(this.hornLeft4);
         this.setRotationAngle(this.hornLeft4, -0.3491F, 0.0F, -0.2618F);
         this.hornLeft4.cubeList.add(new ModelBox(this.hornLeft4, 48, 21, -1.0F, -3.25F, -1.0F, 2, 4, 2, -0.6F, false));
         (this.body = new ModelRenderer(this)).setRotationPoint(0.0F, 18.85F, 0.0F);
         this.body.cubeList.add(new ModelBox(this.body, 24, 0, -0.5F, -4.6F, 5.5F, 1, 1, 2, 0.0F, false));
         (this.bone9 = new ModelRenderer(this)).setRotationPoint(0.0F, 1.3F, 0.5F);
         this.body.addChild(this.bone9);
         this.setRotationAngle(this.bone9, -0.0873F, 0.0F, 0.0F);
         this.bone9.cubeList.add(new ModelBox(this.bone9, 32, 0, -3.0F, -1.5F, 0.0F, 6, 2, 6, 0.0F, false));
         (this.bone10 = new ModelRenderer(this)).setRotationPoint(3.0F, 1.8F, 6.4F);
         this.body.addChild(this.bone10);
         this.setRotationAngle(this.bone10, 0.0873F, -0.0873F, 0.0F);
         this.bone10.cubeList.add(new ModelBox(this.bone10, 3, 26, -3.0F, -3.5F, 0.0F, 3, 4, 7, 0.0F, false));
         (this.bone61 = new ModelRenderer(this)).setRotationPoint(-3.0F, 1.8F, 6.4F);
         this.body.addChild(this.bone61);
         this.setRotationAngle(this.bone61, 0.0873F, 0.0873F, 0.0F);
         this.bone61.cubeList.add(new ModelBox(this.bone61, 3, 26, 0.0F, -3.5F, 0.0F, 3, 4, 7, 0.0F, true));
         (this.bone8 = new ModelRenderer(this)).setRotationPoint(-3.35F, 0.3F, 10.75F);
         this.body.addChild(this.bone8);
         this.setRotationAngle(this.bone8, -0.2618F, 0.2618F, -0.5236F);
         this.bone8.cubeList.add(new ModelBox(this.bone8, 0, 13, 0.0F, -2.0F, -5.0F, 6, 4, 9, 0.0F, false));
         (this.bone3 = new ModelRenderer(this)).setRotationPoint(3.35F, 0.3F, 10.75F);
         this.body.addChild(this.bone3);
         this.setRotationAngle(this.bone3, -0.2618F, -0.2618F, 0.5236F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 0, 13, -6.0F, -2.0F, -5.0F, 6, 4, 9, 0.0F, true));
         (this.bone6 = new ModelRenderer(this)).setRotationPoint(-3.35F, 0.3F, 2.5F);
         this.body.addChild(this.bone6);
         this.setRotationAngle(this.bone6, 0.2618F, -0.2618F, -0.5236F);
         this.bone6.cubeList.add(new ModelBox(this.bone6, 0, 0, 0.0F, -2.0F, -4.0F, 6, 4, 9, 0.0F, false));
         (this.bone4 = new ModelRenderer(this)).setRotationPoint(3.35F, 0.3F, 2.5F);
         this.body.addChild(this.bone4);
         this.setRotationAngle(this.bone4, 0.2618F, 0.2618F, 0.5236F);
         this.bone4.cubeList.add(new ModelBox(this.bone4, 0, 0, -6.0F, -2.0F, -4.0F, 6, 4, 9, 0.0F, true));
         (this.wingRight = new ModelRenderer(this)).setRotationPoint(-2.0927F, -2.9766F, 3.0322F);
         this.body.addChild(this.wingRight);
         this.setRotationAngle(this.wingRight, 0.0F, -0.3491F, 0.0F);
         (this.cube_r3 = new ModelRenderer(this)).setRotationPoint(1.7704F, 7.9911F, 5.1063F);
         this.wingRight.addChild(this.cube_r3);
         this.setRotationAngle(this.cube_r3, -0.9722F, -0.0114F, -0.2253F);
         this.cube_r3.cubeList.add(new ModelBox(this.cube_r3, 30, 17, 0.0F, -10.0F, -12.0F, 0, 10, 6, 0.0F, false));
         (this.cube_r4 = new ModelRenderer(this)).setRotationPoint(2.7704F, 2.7911F, 9.1063F);
         this.wingRight.addChild(this.cube_r4);
         this.setRotationAngle(this.cube_r4, -0.9722F, -0.0114F, -0.2253F);
         this.cube_r4.cubeList.add(new ModelBox(this.cube_r4, 0, 49, -3.2F, -4.8F, -11.2F, 2, 8, 2, -0.7F, false));
         (this.cube_r5 = new ModelRenderer(this)).setRotationPoint(2.2927F, 0.7266F, 10.5178F);
         this.wingRight.addChild(this.cube_r5);
         this.setRotationAngle(this.cube_r5, -0.3177F, -0.0114F, -0.2253F);
         this.cube_r5.cubeList.add(new ModelBox(this.cube_r5, 16, 37, -3.2F, -2.8F, -11.2F, 2, 6, 2, -0.7F, false));
         (this.wingLeft = new ModelRenderer(this)).setRotationPoint(2.0927F, -2.9766F, 3.0322F);
         this.body.addChild(this.wingLeft);
         this.setRotationAngle(this.wingLeft, 0.0F, 0.3491F, 0.0F);
         (this.cube_r6 = new ModelRenderer(this)).setRotationPoint(-1.7704F, 7.9911F, 5.1063F);
         this.wingLeft.addChild(this.cube_r6);
         this.setRotationAngle(this.cube_r6, -0.9722F, 0.0114F, 0.2253F);
         this.cube_r6.cubeList.add(new ModelBox(this.cube_r6, 30, 17, 0.0F, -10.0F, -12.0F, 0, 10, 6, 0.0F, true));
         (this.cube_r7 = new ModelRenderer(this)).setRotationPoint(-2.7704F, 2.7911F, 9.1063F);
         this.wingLeft.addChild(this.cube_r7);
         this.setRotationAngle(this.cube_r7, -0.9722F, 0.0114F, 0.2253F);
         this.cube_r7.cubeList.add(new ModelBox(this.cube_r7, 0, 49, 1.2F, -4.8F, -11.2F, 2, 8, 2, -0.7F, true));
         (this.cube_r8 = new ModelRenderer(this)).setRotationPoint(-2.2927F, 0.7266F, 10.5178F);
         this.wingLeft.addChild(this.cube_r8);
         this.setRotationAngle(this.cube_r8, -0.3177F, 0.0114F, 0.2253F);
         this.cube_r8.cubeList.add(new ModelBox(this.cube_r8, 16, 37, 1.2F, -2.8F, -11.2F, 2, 6, 2, -0.7F, true));
         (this.tail[0] = new ModelRenderer(this)).setRotationPoint(0.0F, -0.2F, 13.75F);
         this.body.addChild(this.tail[0]);
         this.setRotationAngle(this.tail[0], 0.5236F, 0.0F, 0.0F);
         this.tail[0].cubeList.add(new ModelBox(this.tail[0], 24, 9, -2.55F, -2.05F, -1.3F, 5, 4, 6, -0.2F, false));
         (this.tail[1] = new ModelRenderer(this)).setRotationPoint(-0.05F, 0.0F, 4.5F);
         this.tail[0].addChild(this.tail[1]);
         this.setRotationAngle(this.tail[1], 0.7854F, 0.0F, 0.0F);
         this.tail[1].cubeList.add(new ModelBox(this.tail[1], 24, 9, -2.5F, -2.0F, -1.0F, 5, 4, 6, -0.4F, false));
         (this.tail[2] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.05F, 4.5F);
         this.tail[1].addChild(this.tail[2]);
         this.setRotationAngle(this.tail[2], 0.5236F, 0.0F, 0.0F);
         this.tail[2].cubeList.add(new ModelBox(this.tail[2], 24, 9, -2.5F, -2.0F, -1.25F, 5, 4, 6, -0.7F, false));
         (this.tail[3] = new ModelRenderer(this)).setRotationPoint(0.05F, -0.1F, 3.95F);
         this.tail[2].addChild(this.tail[3]);
         this.setRotationAngle(this.tail[3], 0.5236F, 0.0F, 0.0F);
         this.tail[3].cubeList.add(new ModelBox(this.tail[3], 24, 9, -2.5F, -2.0F, -1.5F, 5, 4, 6, -0.9F, false));
         (this.tail[4] = new ModelRenderer(this)).setRotationPoint(-0.05F, 0.05F, 3.5F);
         this.tail[3].addChild(this.tail[4]);
         this.setRotationAngle(this.tail[4], 0.5236F, 0.0F, 0.0F);
         this.tail[4].cubeList.add(new ModelBox(this.tail[4], 24, 9, -2.5F, -2.0F, -1.5F, 5, 4, 6, -1.0F, false));
         (this.tail[5] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 3.3F);
         this.tail[4].addChild(this.tail[5]);
         this.setRotationAngle(this.tail[5], 0.2618F, 0.0F, 0.0F);
         this.tail[5].cubeList.add(new ModelBox(this.tail[5], 24, 9, -2.5F, -2.0F, -1.5F, 5, 4, 6, -1.1F, false));
         (this.tail[6] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 3.0F);
         this.tail[5].addChild(this.tail[6]);
         this.setRotationAngle(this.tail[6], 0.2618F, 0.0F, 0.0F);
         this.tail[6].cubeList.add(new ModelBox(this.tail[6], 24, 9, -2.5F, -2.0F, -1.5F, 5, 4, 6, -1.2F, false));
         (this.tail[7] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 3.0F);
         this.tail[6].addChild(this.tail[7]);
         this.setRotationAngle(this.tail[7], 0.0873F, 0.0F, 0.0F);
         this.tail[7].cubeList.add(new ModelBox(this.tail[7], 24, 9, -2.51F, -1.96F, -1.54F, 5, 4, 6, -1.3F, false));
         (this.snakeHead = new ModelRenderer(this)).setRotationPoint(-0.0653F, 0.0138F, 2.8455F);
         this.tail[7].addChild(this.snakeHead);
         this.setRotationAngle(this.snakeHead, 0.0F, 3.1416F, -3.1416F);
         (this.bone = new ModelRenderer(this)).setRotationPoint(-0.5792F, -0.2968F, -0.9687F);
         this.snakeHead.addChild(this.bone);
         this.setRotationAngle(this.bone, -0.1309F, 0.0873F, 3.1416F);
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 44, -2.0F, -0.5F, -2.0F, 4, 1, 4, -1.15F, false));
         (this.bone39 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.6F, -0.65F);
         this.bone.addChild(this.bone39);
         this.setRotationAngle(this.bone39, -0.1745F, 0.1745F, 0.0436F);
         this.bone39.cubeList.add(new ModelBox(this.bone39, 4, 55, -2.0F, -1.1F, -2.9F, 4, 1, 4, -1.15F, false));
         (this.bone62 = new ModelRenderer(this)).setRotationPoint(0.5597F, -0.2968F, -0.9687F);
         this.snakeHead.addChild(this.bone62);
         this.setRotationAngle(this.bone62, -0.1309F, -0.0873F, -3.1416F);
         this.bone62.cubeList.add(new ModelBox(this.bone62, 0, 44, -2.0F, -0.5F, -2.0F, 4, 1, 4, -1.15F, true));
         (this.bone63 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.6F, -0.65F);
         this.bone62.addChild(this.bone63);
         this.setRotationAngle(this.bone63, -0.1745F, -0.1745F, -0.0436F);
         this.bone63.cubeList.add(new ModelBox(this.bone63, 4, 55, -2.0F, -1.1F, -2.9F, 4, 1, 4, -1.15F, true));
         (this.snakeJaw = new ModelRenderer(this)).setRotationPoint(-0.0229F, 0.2494F, 0.0045F);
         this.snakeHead.addChild(this.snakeJaw);
         this.setRotationAngle(this.snakeJaw, 0.7854F, 0.0F, 0.0F);
         (this.bone64 = new ModelRenderer(this)).setRotationPoint(-0.5696F, 0.35F, -0.9232F);
         this.snakeJaw.addChild(this.bone64);
         this.setRotationAngle(this.bone64, 0.0F, -0.0873F, 0.0F);
         this.bone64.cubeList.add(new ModelBox(this.bone64, 40, 8, -2.0F, -1.5F, -2.0F, 4, 3, 4, -1.15F, false));
         (this.bone70 = new ModelRenderer(this)).setRotationPoint(-0.05F, 0.0F, -0.55F);
         this.bone64.addChild(this.bone70);
         this.setRotationAngle(this.bone70, 0.0F, -0.1745F, 0.0F);
         this.bone70.cubeList.add(new ModelBox(this.bone70, 0, 37, -2.0F, -1.5F, -2.85F, 4, 3, 4, -1.15F, false));
         (this.bone75 = new ModelRenderer(this)).setRotationPoint(0.596F, 0.35F, -0.9232F);
         this.snakeJaw.addChild(this.bone75);
         this.setRotationAngle(this.bone75, 0.0F, 0.0873F, 0.0F);
         this.bone75.cubeList.add(new ModelBox(this.bone75, 40, 8, -2.0F, -1.5F, -2.0F, 4, 3, 4, -1.15F, true));
         (this.bone76 = new ModelRenderer(this)).setRotationPoint(0.05F, 0.0F, -0.55F);
         this.bone75.addChild(this.bone76);
         this.setRotationAngle(this.bone76, 0.0F, 0.1745F, 0.0F);
         this.bone76.cubeList.add(new ModelBox(this.bone76, 0, 37, -2.0F, -1.5F, -2.85F, 4, 3, 4, -1.15F, true));
         (this.leg1 = new ModelRenderer(this)).setRotationPoint(-3.2826F, 17.8674F, 0.7F);
         (this.bone27 = new ModelRenderer(this)).setRotationPoint(-0.9545F, -0.0307F, 1.3F);
         this.leg1.addChild(this.bone27);
         this.setRotationAngle(this.bone27, 0.0F, 0.3054F, -0.5236F);
         (this.cube_r9 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bone27.addChild(this.cube_r9);
         this.setRotationAngle(this.cube_r9, 0.0F, 0.0F, 0.0F);
         this.cube_r9.cubeList.add(new ModelBox(this.cube_r9, 48, 15, -4.0F, 0.0F, -2.0F, 6, 2, 2, 0.1F, false));
         (this.bone28 = new ModelRenderer(this)).setRotationPoint(-4.0955F, 0.9807F, -0.1F);
         this.bone27.addChild(this.bone28);
         this.setRotationAngle(this.bone28, 0.0F, -0.3927F, -1.5708F);
         (this.cube_r10 = new ModelRenderer(this)).setRotationPoint(0.15F, 0.0F, 0.1F);
         this.bone28.addChild(this.cube_r10);
         this.setRotationAngle(this.cube_r10, 0.0F, 0.0F, 0.0F);
         this.cube_r10.cubeList.add(new ModelBox(this.cube_r10, 44, 39, -4.0F, -1.0F, -2.0F, 4, 2, 2, -0.1F, false));
         (this.bone2 = new ModelRenderer(this)).setRotationPoint(-4.4174F, 6.6326F, 0.25F);
         this.leg1.addChild(this.bone2);
         (this.cube_r11 = new ModelRenderer(this)).setRotationPoint(1.2F, 0.0F, -0.25F);
         this.bone2.addChild(this.cube_r11);
         this.setRotationAngle(this.cube_r11, 0.0F, 0.7854F, 0.0F);
         this.cube_r11.cubeList.add(new ModelBox(this.cube_r11, 16, 26, -1.7F, -1.3F, -1.7F, 3, 1, 3, -0.1F, false));
         (this.bone19 = new ModelRenderer(this)).setRotationPoint(1.1212F, -0.9F, -1.3029F);
         this.bone2.addChild(this.bone19);
         this.setRotationAngle(this.bone19, 0.1745F, 0.2618F, 0.0F);
         this.bone19.cubeList.add(new ModelBox(this.bone19, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, false));
         (this.bone20 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone19.addChild(this.bone20);
         this.setRotationAngle(this.bone20, 0.5236F, 0.0F, 0.0F);
         this.bone20.cubeList.add(new ModelBox(this.bone20, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.bone32 = new ModelRenderer(this)).setRotationPoint(2.1212F, -0.9F, -0.3029F);
         this.bone2.addChild(this.bone32);
         this.setRotationAngle(this.bone32, 0.1745F, -0.7854F, 0.0F);
         this.bone32.cubeList.add(new ModelBox(this.bone32, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, false));
         (this.bone33 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone32.addChild(this.bone33);
         this.setRotationAngle(this.bone33, 0.5236F, 0.0F, 0.0F);
         this.bone33.cubeList.add(new ModelBox(this.bone33, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.bone34 = new ModelRenderer(this)).setRotationPoint(0.3712F, -0.9F, -0.8029F);
         this.bone2.addChild(this.bone34);
         this.setRotationAngle(this.bone34, 0.1745F, 0.7854F, 0.0F);
         this.bone34.cubeList.add(new ModelBox(this.bone34, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, false));
         (this.bone35 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone34.addChild(this.bone35);
         this.setRotationAngle(this.bone35, 0.5236F, 0.0F, 0.0F);
         this.bone35.cubeList.add(new ModelBox(this.bone35, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.bone36 = new ModelRenderer(this)).setRotationPoint(0.1212F, -0.9F, 0.1971F);
         this.bone2.addChild(this.bone36);
         this.setRotationAngle(this.bone36, 0.1745F, 1.2217F, 0.0F);
         this.bone36.cubeList.add(new ModelBox(this.bone36, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, false));
         (this.bone37 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone36.addChild(this.bone37);
         this.setRotationAngle(this.bone37, 0.5236F, 0.0F, 0.0F);
         this.bone37.cubeList.add(new ModelBox(this.bone37, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.leg2 = new ModelRenderer(this)).setRotationPoint(3.2826F, 17.8674F, 0.7F);
         (this.bone15 = new ModelRenderer(this)).setRotationPoint(0.9545F, -0.0307F, 1.3F);
         this.leg2.addChild(this.bone15);
         this.setRotationAngle(this.bone15, 0.0F, -0.3054F, 0.5236F);
         (this.cube_r12 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bone15.addChild(this.cube_r12);
         this.setRotationAngle(this.cube_r12, 0.0F, 0.0F, 0.0F);
         this.cube_r12.cubeList.add(new ModelBox(this.cube_r12, 48, 15, -2.0F, 0.0F, -2.0F, 6, 2, 2, 0.1F, true));
         (this.bone17 = new ModelRenderer(this)).setRotationPoint(4.0955F, 0.9807F, -0.1F);
         this.bone15.addChild(this.bone17);
         this.setRotationAngle(this.bone17, 0.0F, 0.3927F, 1.5708F);
         (this.cube_r13 = new ModelRenderer(this)).setRotationPoint(-0.15F, 0.0F, 0.1F);
         this.bone17.addChild(this.cube_r13);
         this.setRotationAngle(this.cube_r13, 0.0F, 0.0F, 0.0F);
         this.cube_r13.cubeList.add(new ModelBox(this.cube_r13, 44, 39, 0.0F, -1.0F, -2.0F, 4, 2, 2, -0.1F, true));
         (this.bone21 = new ModelRenderer(this)).setRotationPoint(4.4174F, 6.6326F, 0.25F);
         this.leg2.addChild(this.bone21);
         (this.cube_r14 = new ModelRenderer(this)).setRotationPoint(-1.2F, 0.0F, -0.25F);
         this.bone21.addChild(this.cube_r14);
         this.setRotationAngle(this.cube_r14, 0.0F, -0.7854F, 0.0F);
         this.cube_r14.cubeList.add(new ModelBox(this.cube_r14, 16, 26, -1.3F, -1.3F, -1.7F, 3, 1, 3, -0.1F, true));
         (this.bone22 = new ModelRenderer(this)).setRotationPoint(-1.1212F, -0.9F, -1.3029F);
         this.bone21.addChild(this.bone22);
         this.setRotationAngle(this.bone22, 0.1745F, -0.2618F, 0.0F);
         this.bone22.cubeList.add(new ModelBox(this.bone22, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, true));
         (this.bone23 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone22.addChild(this.bone23);
         this.setRotationAngle(this.bone23, 0.5236F, 0.0F, 0.0F);
         this.bone23.cubeList.add(new ModelBox(this.bone23, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.bone24 = new ModelRenderer(this)).setRotationPoint(-2.1212F, -0.9F, -0.3029F);
         this.bone21.addChild(this.bone24);
         this.setRotationAngle(this.bone24, 0.1745F, 0.7854F, 0.0F);
         this.bone24.cubeList.add(new ModelBox(this.bone24, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, true));
         (this.bone25 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone24.addChild(this.bone25);
         this.setRotationAngle(this.bone25, 0.5236F, 0.0F, 0.0F);
         this.bone25.cubeList.add(new ModelBox(this.bone25, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.bone26 = new ModelRenderer(this)).setRotationPoint(-0.3712F, -0.9F, -0.8029F);
         this.bone21.addChild(this.bone26);
         this.setRotationAngle(this.bone26, 0.1745F, -0.7854F, 0.0F);
         this.bone26.cubeList.add(new ModelBox(this.bone26, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, true));
         (this.bone29 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone26.addChild(this.bone29);
         this.setRotationAngle(this.bone29, 0.5236F, 0.0F, 0.0F);
         this.bone29.cubeList.add(new ModelBox(this.bone29, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.bone30 = new ModelRenderer(this)).setRotationPoint(-0.1212F, -0.9F, 0.1971F);
         this.bone21.addChild(this.bone30);
         this.setRotationAngle(this.bone30, 0.1745F, -1.2217F, 0.0F);
         this.bone30.cubeList.add(new ModelBox(this.bone30, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, true));
         (this.bone31 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone30.addChild(this.bone31);
         this.setRotationAngle(this.bone31, 0.5236F, 0.0F, 0.0F);
         this.bone31.cubeList.add(new ModelBox(this.bone31, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.leg3 = new ModelRenderer(this)).setRotationPoint(-3.2826F, 18.3674F, 9.45F);
         (this.bone38 = new ModelRenderer(this)).setRotationPoint(-1.9545F, -0.5307F, 0.55F);
         this.leg3.addChild(this.bone38);
         this.setRotationAngle(this.bone38, 0.3491F, -0.2618F, -0.5236F);
         (this.cube_r15 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bone38.addChild(this.cube_r15);
         this.setRotationAngle(this.cube_r15, 0.0F, 0.0F, 0.0F);
         this.cube_r15.cubeList.add(new ModelBox(this.cube_r15, 48, 15, -4.0F, 0.0F, -2.0F, 6, 2, 2, 0.1F, false));
         (this.bone40 = new ModelRenderer(this)).setRotationPoint(-4.0955F, 0.8308F, -1.35F);
         this.bone38.addChild(this.bone40);
         this.setRotationAngle(this.bone40, 0.0F, 0.3927F, -1.309F);
         (this.cube_r16 = new ModelRenderer(this)).setRotationPoint(0.15F, 0.15F, 1.35F);
         this.bone40.addChild(this.cube_r16);
         this.setRotationAngle(this.cube_r16, 0.0F, 0.0F, 0.0F);
         this.cube_r16.cubeList.add(new ModelBox(this.cube_r16, 44, 39, -4.0F, -0.25F, -2.25F, 4, 2, 2, -0.1F, false));
         (this.bone41 = new ModelRenderer(this)).setRotationPoint(-4.0002F, 5.3326F, 0.55F);
         this.leg3.addChild(this.bone41);
         this.setRotationAngle(this.bone41, 0.0F, 0.5236F, 0.0F);
         (this.cube_r17 = new ModelRenderer(this)).setRotationPoint(0.2828F, 0.8F, 0.0F);
         this.bone41.addChild(this.cube_r17);
         this.setRotationAngle(this.cube_r17, 0.0F, 0.7854F, 0.0F);
         this.cube_r17.cubeList.add(new ModelBox(this.cube_r17, 16, 26, -1.7F, -1.3F, -1.7F, 3, 1, 3, -0.1F, false));
         (this.bone42 = new ModelRenderer(this)).setRotationPoint(0.2041F, -0.1F, -1.0529F);
         this.bone41.addChild(this.bone42);
         this.setRotationAngle(this.bone42, 0.1745F, 0.2618F, 0.0F);
         this.bone42.cubeList.add(new ModelBox(this.bone42, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, false));
         (this.bone43 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone42.addChild(this.bone43);
         this.setRotationAngle(this.bone43, 0.5236F, 0.0F, 0.0F);
         this.bone43.cubeList.add(new ModelBox(this.bone43, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.bone44 = new ModelRenderer(this)).setRotationPoint(1.2041F, -0.1F, -0.0529F);
         this.bone41.addChild(this.bone44);
         this.setRotationAngle(this.bone44, 0.1745F, -0.7854F, 0.0F);
         this.bone44.cubeList.add(new ModelBox(this.bone44, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, false));
         (this.bone45 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone44.addChild(this.bone45);
         this.setRotationAngle(this.bone45, 0.5236F, 0.0F, 0.0F);
         this.bone45.cubeList.add(new ModelBox(this.bone45, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.bone46 = new ModelRenderer(this)).setRotationPoint(-0.5459F, -0.1F, -0.5529F);
         this.bone41.addChild(this.bone46);
         this.setRotationAngle(this.bone46, 0.1745F, 0.7854F, 0.0F);
         this.bone46.cubeList.add(new ModelBox(this.bone46, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, false));
         (this.bone47 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone46.addChild(this.bone47);
         this.setRotationAngle(this.bone47, 0.5236F, 0.0F, 0.0F);
         this.bone47.cubeList.add(new ModelBox(this.bone47, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.bone48 = new ModelRenderer(this)).setRotationPoint(-0.7959F, -0.1F, 0.4471F);
         this.bone41.addChild(this.bone48);
         this.setRotationAngle(this.bone48, 0.1745F, 1.2217F, 0.0F);
         this.bone48.cubeList.add(new ModelBox(this.bone48, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, false));
         (this.bone49 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone48.addChild(this.bone49);
         this.setRotationAngle(this.bone49, 0.5236F, 0.0F, 0.0F);
         this.bone49.cubeList.add(new ModelBox(this.bone49, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, false));
         (this.leg4 = new ModelRenderer(this)).setRotationPoint(3.2826F, 18.3674F, 9.45F);
         (this.bone50 = new ModelRenderer(this)).setRotationPoint(1.9545F, -0.5307F, 0.55F);
         this.leg4.addChild(this.bone50);
         this.setRotationAngle(this.bone50, 0.3491F, 0.2618F, 0.5236F);
         (this.cube_r18 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bone50.addChild(this.cube_r18);
         this.setRotationAngle(this.cube_r18, 0.0F, 0.0F, 0.0F);
         this.cube_r18.cubeList.add(new ModelBox(this.cube_r18, 48, 15, -2.0F, 0.0F, -2.0F, 6, 2, 2, 0.1F, true));
         (this.bone51 = new ModelRenderer(this)).setRotationPoint(4.0955F, 0.8308F, -1.35F);
         this.bone50.addChild(this.bone51);
         this.setRotationAngle(this.bone51, 0.0F, -0.3927F, 1.309F);
         (this.cube_r19 = new ModelRenderer(this)).setRotationPoint(-0.15F, 0.15F, 1.35F);
         this.bone51.addChild(this.cube_r19);
         this.setRotationAngle(this.cube_r19, 0.0F, 0.0F, 0.0F);
         this.cube_r19.cubeList.add(new ModelBox(this.cube_r19, 44, 39, 0.0F, -0.25F, -2.25F, 4, 2, 2, -0.1F, true));
         (this.bone52 = new ModelRenderer(this)).setRotationPoint(4.0002F, 5.3326F, 0.55F);
         this.leg4.addChild(this.bone52);
         this.setRotationAngle(this.bone52, 0.0F, -0.5236F, 0.0F);
         (this.cube_r20 = new ModelRenderer(this)).setRotationPoint(-0.2828F, 0.8F, 0.0F);
         this.bone52.addChild(this.cube_r20);
         this.setRotationAngle(this.cube_r20, 0.0F, -0.7854F, 0.0F);
         this.cube_r20.cubeList.add(new ModelBox(this.cube_r20, 16, 26, -1.3F, -1.3F, -1.7F, 3, 1, 3, -0.1F, true));
         (this.bone53 = new ModelRenderer(this)).setRotationPoint(-0.2041F, -0.1F, -1.0529F);
         this.bone52.addChild(this.bone53);
         this.setRotationAngle(this.bone53, 0.1745F, -0.2618F, 0.0F);
         this.bone53.cubeList.add(new ModelBox(this.bone53, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, true));
         (this.bone54 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone53.addChild(this.bone54);
         this.setRotationAngle(this.bone54, 0.5236F, 0.0F, 0.0F);
         this.bone54.cubeList.add(new ModelBox(this.bone54, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.bone55 = new ModelRenderer(this)).setRotationPoint(-1.2041F, -0.1F, -0.0529F);
         this.bone52.addChild(this.bone55);
         this.setRotationAngle(this.bone55, 0.1745F, 0.7854F, 0.0F);
         this.bone55.cubeList.add(new ModelBox(this.bone55, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, true));
         (this.bone56 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone55.addChild(this.bone56);
         this.setRotationAngle(this.bone56, 0.5236F, 0.0F, 0.0F);
         this.bone56.cubeList.add(new ModelBox(this.bone56, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.bone57 = new ModelRenderer(this)).setRotationPoint(0.5459F, -0.1F, -0.5529F);
         this.bone52.addChild(this.bone57);
         this.setRotationAngle(this.bone57, 0.1745F, -0.7854F, 0.0F);
         this.bone57.cubeList.add(new ModelBox(this.bone57, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, true));
         (this.bone58 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone57.addChild(this.bone58);
         this.setRotationAngle(this.bone58, 0.5236F, 0.0F, 0.0F);
         this.bone58.cubeList.add(new ModelBox(this.bone58, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
         (this.bone59 = new ModelRenderer(this)).setRotationPoint(0.7959F, -0.1F, 0.4471F);
         this.bone52.addChild(this.bone59);
         this.setRotationAngle(this.bone59, 0.1745F, -1.2217F, 0.0F);
         this.bone59.cubeList.add(new ModelBox(this.bone59, 48, 31, -0.5F, -0.5F, -1.9F, 1, 1, 2, -0.1F, true));
         (this.bone60 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.8F);
         this.bone59.addChild(this.bone60);
         this.setRotationAngle(this.bone60, 0.5236F, 0.0F, 0.0F);
         this.bone60.cubeList.add(new ModelBox(this.bone60, 0, 30, -0.5F, -0.25F, -0.75F, 1, 1, 1, -0.2F, true));
      }

      public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
         this.head.render(scale);
         this.body.render(scale);
         this.leg1.render(scale);
         this.leg2.render(scale);
         this.leg3.render(scale);
         this.leg4.render(scale);
      }

      public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
         float adjustedSwing = limbSwing * 2.0F / entity.height;
         float walkCycle = MathHelper.cos(adjustedSwing * 0.6662F) * 1.4F * limbSwingAmount;
         this.leg1.rotateAngleX = walkCycle;
         this.leg2.rotateAngleX = -walkCycle;
         this.leg3.rotateAngleX = -walkCycle;
         this.leg4.rotateAngleX = walkCycle;
         this.body.rotateAngleX = 0.0F;
         this.wingLeft.rotateAngleZ = MathHelper.cos(ageInTicks * 0.09F) * 0.08F;
         this.wingRight.rotateAngleZ = -MathHelper.cos(ageInTicks * 0.09F) * 0.08F;

         for(int i = 1; i < this.tail.length; ++i) {
            this.tail[i].rotateAngleX = this.tailPreset1[i].x + MathHelper.sin(ageInTicks * 0.067F) * 0.025F;
            this.tail[i].rotateAngleY = this.tailPreset1[i].y + MathHelper.cos(ageInTicks * 0.067F) * 0.1F;
         }

         int tick = (int)ageInTicks;
         if (tick % 55 == 0) {
            this.leftEye.rotateAngleY = 0.2618F + 0.2618F * Math.abs(MathHelper.sin(0.05F * (float)tick));
            this.leftEye.rotateAngleZ = -0.5236F + 0.1745F * Math.abs(MathHelper.cos(0.08F * (float)tick));
         }

         if (this.swingProgress > 0.0F) {
            this.upperJaw.rotateAngleX = -0.5236F;
            this.lowerJaw.rotateAngleX = 0.5236F;
            this.snakeJaw.rotateAngleX = 0.7854F;
         } else {
            this.upperJaw.rotateAngleX = 0.0F;
            this.lowerJaw.rotateAngleX = 0.0F;
            this.snakeJaw.rotateAngleX = 0.0F;
         }

      }

      private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class BirdRenderer extends RenderLiving<EntitySummonedBird> {
      private static final ResourceLocation BIRD_TEXTURE = new ResourceLocation("narutomod:textures/bigbird.png");

      public BirdRenderer(RenderManager renderManager) {
         super(renderManager, new ModelSummonBird(), 2.0F);
      }

      protected ResourceLocation getEntityTexture(EntitySummonedBird entity) {
         return BIRD_TEXTURE;
      }

      protected void preRenderCallback(EntitySummonedBird entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         GlStateManager.scale(2.5F, 2.5F, 2.5F);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ChameleonRenderer extends RenderLiving<EntitySummonedChameleon> {
      private static final ResourceLocation CHAMELEON_TEXTURE = new ResourceLocation("narutomod:textures/chameleon.png");

      public ChameleonRenderer(RenderManager renderManager) {
         super(renderManager, new ModelSummonChameleon(), 3.0F);
      }

      protected ResourceLocation getEntityTexture(EntitySummonedChameleon entity) {
         return CHAMELEON_TEXTURE;
      }

      protected void preRenderCallback(EntitySummonedChameleon entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         GlStateManager.scale(3.0F, 3.0F, 3.0F);
      }

      protected void renderModel(EntitySummonedChameleon entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
         if (entity.isInvisible()) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
            GlStateManager.color(0.5F, 0.8F, 0.5F, 0.25F);
            if (this.bindEntityTexture(entity)) {
               this.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
            }

            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         } else {
            super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
         }

      }
   }

   public static class EntitySummonedBird extends EntityCreature {
      private int lifetime = 0;
      private static final int MAX_LIFETIME = 1200;
      private UUID summonerUUID = null;
      private int swoopCooldown = 0;
      private boolean isSwooping = false;
      private double spawnY = (double)0.0F;

      public EntitySummonedBird(World world) {
         super(world);
         this.setSize(3.0F, 2.0F);
         this.experienceValue = 0;
         this.isImmuneToFire = true;
         this.setNoGravity(true);
      }

      protected void initEntityAI() {
         this.tasks.addTask(0, new EntityAISwimming(this));
         this.tasks.addTask(1, new EntityAIAttackMelee(this, (double)1.5F, false));
         this.tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 1.3, 32.0F));
         this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 12.0F));
         this.targetTasks.addTask(0, new EntityAIHurtByTarget(this, false, new Class[0]));
         this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, false));
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)15000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.5F);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)64.0F);
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)40.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.8);
      }

      public void setSummoner(Entity summoner) {
         if (summoner != null) {
            this.summonerUUID = summoner.getUniqueID();
         }

      }

      public UUID getSummonerUUID() {
         return this.summonerUUID;
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime == 1) {
            this.spawnY = this.posY;
         }

         if (this.lifetime >= 1200) {
            this.despawnWithParticles();
         } else {
            if (!this.world.isRemote && this.lifetime % 10 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 3, (double)0.5F, 0.3, (double)0.5F, 0.02, new int[0]);
            }

         }
      }

      public void onLivingUpdate() {
         super.onLivingUpdate();
         if (!this.world.isRemote) {
            if (this.swoopCooldown > 0) {
               --this.swoopCooldown;
            }

            EntityLivingBase target = this.getAttackTarget();
            if (target != null && target.isEntityAlive()) {
               double dist = (double)this.getDistance(target);
               if (dist > (double)5.0F && dist < (double)40.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, (double)1.5F);
               }
            }

            if (!this.isSwooping) {
               double hoverY = this.spawnY + (double)8.0F;
               if (this.posY < hoverY - (double)1.0F) {
                  this.motionY += 0.04;
               } else if (this.posY > hoverY + (double)1.0F) {
                  this.motionY -= 0.02;
               } else {
                  this.motionY = (double)MathHelper.sin((float)this.ticksExisted * 0.05F) * 0.02;
               }
            }

            if (target != null && target.isEntityAlive() && this.swoopCooldown <= 0) {
               double dx = target.posX - this.posX;
               double dz = target.posZ - this.posZ;
               double dist = Math.sqrt(dx * dx + dz * dz);
               if (dist >= (double)8.0F && dist <= (double)25.0F) {
                  this.swoopAttack(target, dx, dz, dist);
                  this.swoopCooldown = 50;
               }
            }

            if (this.isSwooping) {
               EntityLivingBase target2 = this.getAttackTarget();
               if (target2 != null && target2.isEntityAlive()) {
                  double contactDist = (double)this.getDistance(target2);
                  if (contactDist <= (double)4.0F) {
                     target2.hurtResistantTime = 0;
                     boolean hit = target2.attackEntityFrom(DamageSource.causeMobDamage(this), 55.0F);
                     if (!hit) {
                        target2.setHealth(target2.getHealth() - 55.0F);
                     }

                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, target2.posX, target2.posY + (double)1.0F, target2.posZ, 20, (double)1.0F, (double)0.5F, (double)1.0F, 0.1, new int[0]);
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target2.posX, target2.posY + (double)1.5F, target2.posZ, 10, (double)0.5F, (double)0.5F, (double)0.5F, 0.15, new int[0]);
                     }

                     this.motionY = (double)0.5F;
                     this.velocityChanged = true;
                     this.isSwooping = false;
                  }
               }

               if (this.swoopCooldown <= 80) {
                  this.motionY = (double)0.5F;
                  this.velocityChanged = true;
                  this.isSwooping = false;
               }
            }

         }
      }

      private void swoopAttack(EntityLivingBase target, double dx, double dz, double dist) {
         this.isSwooping = true;
         double hdist = Math.sqrt(dx * dx + dz * dz);
         this.motionX = dx / hdist * (double)2.0F;
         this.motionY = -1.2;
         this.motionZ = dz / hdist * (double)2.0F;
         this.velocityChanged = true;
         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.HOSTILE, 1.5F, 0.6F);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY, this.posZ, 15, (double)0.5F, 0.3, (double)0.5F, 0.1, new int[0]);
         }

      }

      public boolean attackEntityAsMob(Entity target) {
         float damage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.HOSTILE, 1.5F, 0.7F);
         if (target instanceof EntityLivingBase) {
            ((EntityLivingBase)target).hurtResistantTime = 0;
         }

         boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
         if (!hit && target instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase)target;
            living.setHealth(living.getHealth() - damage);
            hit = true;
         }

         if (hit && this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 10, 0.4, 0.4, 0.4, 0.15, new int[0]);
         }

         return hit;
      }

      public void onDeath(DamageSource cause) {
         super.onDeath(cause);
         this.despawnParticlesOnly();
      }

      private void despawnWithParticles() {
         this.despawnParticlesOnly();
         this.setDead();
      }

      private void despawnParticlesOnly() {
         if (!this.world.isRemote && this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)1.0F, (double)1.0F, (double)1.0F, 0.1, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 20, 0.8, 0.8, 0.8, 0.08, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.8F, 1.5F);
      }

      public boolean getCanSpawnHere() {
         return true;
      }

      protected boolean canDespawn() {
         return false;
      }

      public void fall(float distance, float damageMultiplier) {
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("birdLifetime", this.lifetime);
         compound.setInteger("swoopCooldown", this.swoopCooldown);
         compound.setDouble("spawnY", this.spawnY);
         if (this.summonerUUID != null) {
            compound.setLong("summonerMost", this.summonerUUID.getMostSignificantBits());
            compound.setLong("summonerLeast", this.summonerUUID.getLeastSignificantBits());
         }

      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("birdLifetime");
         this.swoopCooldown = compound.getInteger("swoopCooldown");
         this.spawnY = compound.getDouble("spawnY");
         if (compound.hasKey("summonerMost") && compound.hasKey("summonerLeast")) {
            this.summonerUUID = new UUID(compound.getLong("summonerMost"), compound.getLong("summonerLeast"));
         }

      }
   }

   public static class EntitySummonedChameleon extends EntityCreature {
      private int lifetime = 0;
      private static final int MAX_LIFETIME = 1200;
      private UUID summonerUUID = null;
      private int tongueCooldown = 0;
      private boolean isStealth = false;

      public EntitySummonedChameleon(World world) {
         super(world);
         this.setSize(2.0F, 1.8F);
         this.experienceValue = 0;
         this.isImmuneToFire = true;
      }

      protected void initEntityAI() {
         this.tasks.addTask(0, new EntityAISwimming(this));
         this.tasks.addTask(1, new EntityAIAttackMelee(this, (double)1.5F, false));
         this.tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 1.3, 32.0F));
         this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 12.0F));
         this.targetTasks.addTask(0, new EntityAIHurtByTarget(this, false, new Class[0]));
         this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, false));
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)18000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.45);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)48.0F);
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)35.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.9);
      }

      public void setSummoner(Entity summoner) {
         if (summoner != null) {
            this.summonerUUID = summoner.getUniqueID();
         }

      }

      public UUID getSummonerUUID() {
         return this.summonerUUID;
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime >= 1200) {
            this.despawnWithParticles();
         } else {
            if (!this.world.isRemote && this.isStealth && this.lifetime % 15 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.END_ROD, this.posX, this.posY + (double)1.0F, this.posZ, 3, (double)0.5F, (double)0.5F, (double)0.5F, 0.01, new int[0]);
            }

         }
      }

      public void onLivingUpdate() {
         super.onLivingUpdate();
         if (!this.world.isRemote) {
            if (this.tongueCooldown > 0) {
               --this.tongueCooldown;
            }

            EntityLivingBase target = this.getAttackTarget();
            if (target != null && target.isEntityAlive()) {
               double dist = (double)this.getDistance(target);
               if (dist > (double)5.0F && dist < (double)40.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, (double)1.5F);
               }
            }

            if (target != null && target.isEntityAlive() && this.onGround) {
               double dy = target.posY - this.posY;
               double dist = (double)this.getDistance(target);
               if (dy > (double)2.0F && dist < (double)20.0F) {
                  double dx = target.posX - this.posX;
                  double dz = target.posZ - this.posZ;
                  double hdist = Math.sqrt(dx * dx + dz * dz);
                  if (hdist > (double)0.5F) {
                     this.motionX = dx / hdist * 0.8;
                     this.motionZ = dz / hdist * 0.8;
                  }

                  this.motionY = (double)0.5F + Math.min(dy * 0.1, 0.4);
                  this.velocityChanged = true;
               }
            }

            if (target != null && !((double)this.getDistance(target) > (double)8.0F)) {
               if (this.isStealth) {
                  this.isStealth = false;
                  this.setInvisible(false);
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)0.5F, (double)0.5F, (double)0.5F, 0.05, new int[0]);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.8F, 1.2F);
               }
            } else if (!this.isStealth) {
               this.isStealth = true;
               this.setInvisible(true);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.END_ROD, this.posX, this.posY + (double)1.0F, this.posZ, 10, 0.8, 0.8, 0.8, 0.02, new int[0]);
               }
            }

            if (target != null && target.isEntityAlive() && this.tongueCooldown <= 0) {
               double dist = (double)this.getDistance(target);
               if (dist >= (double)6.0F && dist <= (double)15.0F) {
                  this.tongueGrab(target);
                  this.tongueCooldown = 160;
               }
            }

         }
      }

      private void tongueGrab(EntityLivingBase target) {
         double dx = this.posX - target.posX;
         double dz = this.posZ - target.posZ;
         double dist = Math.sqrt(dx * dx + dz * dz);
         if (dist > 0.1) {
            double pullStrength = 1.2;
            target.motionX = dx / dist * pullStrength;
            target.motionY = 0.3;
            target.motionZ = dz / dist * pullStrength;
            target.velocityChanged = true;
         }

         target.hurtResistantTime = 0;
         boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), 35.0F);
         if (!hit) {
            target.setHealth(target.getHealth() - 35.0F);
         }

         target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.HOSTILE, 1.5F, 0.5F);
         if (this.world instanceof WorldServer) {
            double steps = (double)8.0F;

            for(int i = 0; i <= (int)steps; ++i) {
               double t = (double)i / steps;
               double px = this.posX + (target.posX - this.posX) * t;
               double py = this.posY + (double)1.0F + (target.posY + (double)1.0F - (this.posY + (double)1.0F)) * t;
               double pz = this.posZ + (target.posZ - this.posZ) * t;
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SLIME, px, py, pz, 2, 0.1, 0.1, 0.1, (double)0.0F, new int[0]);
            }

            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, target.posX, target.posY + (double)1.0F, target.posZ, 12, 0.4, (double)0.5F, 0.4, 0.15, new int[0]);
         }

      }

      public boolean attackEntityAsMob(Entity target) {
         float damage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.HOSTILE, 1.2F, 0.6F);
         if (target instanceof EntityLivingBase) {
            ((EntityLivingBase)target).hurtResistantTime = 0;
         }

         boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
         if (!hit && target instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase)target;
            living.setHealth(living.getHealth() - damage);
            hit = true;
         }

         if (hit && this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 10, 0.4, 0.4, 0.4, 0.15, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, target.posX, target.posY + (double)1.5F, target.posZ, 5, 0.3, 0.3, 0.3, 0.1, new int[0]);
         }

         return hit;
      }

      public void onDeath(DamageSource cause) {
         super.onDeath(cause);
         this.despawnParticlesOnly();
      }

      private void despawnWithParticles() {
         this.despawnParticlesOnly();
         this.setDead();
      }

      private void despawnParticlesOnly() {
         if (!this.world.isRemote && this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 30, 0.8, (double)1.0F, 0.8, 0.1, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 20, 0.6, 0.8, 0.6, 0.08, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.8F, 1.5F);
      }

      public boolean getCanSpawnHere() {
         return true;
      }

      protected boolean canDespawn() {
         return false;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("chameleonLifetime", this.lifetime);
         compound.setInteger("tongueCooldown", this.tongueCooldown);
         compound.setBoolean("isStealth", this.isStealth);
         if (this.summonerUUID != null) {
            compound.setLong("summonerMost", this.summonerUUID.getMostSignificantBits());
            compound.setLong("summonerLeast", this.summonerUUID.getLeastSignificantBits());
         }

      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("chameleonLifetime");
         this.tongueCooldown = compound.getInteger("tongueCooldown");
         this.isStealth = compound.getBoolean("isStealth");
         if (this.isStealth) {
            this.setInvisible(true);
         }

         if (compound.hasKey("summonerMost") && compound.hasKey("summonerLeast")) {
            this.summonerUUID = new UUID(compound.getLong("summonerMost"), compound.getLong("summonerLeast"));
         }

      }
   }

   public static class EntitySummonedBeast extends EntityCreature {
      private int lifetime = 0;
      private static final int MAX_LIFETIME = 1200;
      private UUID summonerUUID = null;
      private int pounceCooldown = 0;
      private boolean isPouncing = false;

      public EntitySummonedBeast(World world) {
         super(world);
         this.setSize(3.6F, 3.0F);
         this.experienceValue = 0;
         this.isImmuneToFire = true;
      }

      protected void initEntityAI() {
         this.tasks.addTask(0, new EntityAISwimming(this));
         this.tasks.addTask(1, new EntityAIAttackMelee(this, (double)1.5F, false));
         this.tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 1.3, 32.0F));
         this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 12.0F));
         this.targetTasks.addTask(0, new EntityAIHurtByTarget(this, false, new Class[0]));
         this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, false));
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)20000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.45);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)48.0F);
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)26.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.8);
      }

      public void setSummoner(Entity summoner) {
         if (summoner != null) {
            this.summonerUUID = summoner.getUniqueID();
         }

      }

      public UUID getSummonerUUID() {
         return this.summonerUUID;
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime >= 1200) {
            this.despawnWithParticles();
         } else {
            if (!this.world.isRemote && this.lifetime % 10 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 3, 0.3, (double)0.5F, 0.3, 0.02, new int[0]);
            }

         }
      }

      public void onLivingUpdate() {
         super.onLivingUpdate();
         if (!this.world.isRemote) {
            if (this.pounceCooldown > 0) {
               --this.pounceCooldown;
            }

            EntityLivingBase target = this.getAttackTarget();
            if (target != null && target.isEntityAlive()) {
               double dist = (double)this.getDistance(target);
               if (dist > (double)5.0F && dist < (double)40.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, (double)1.5F);
               }
            }

            if (target != null && target.isEntityAlive() && this.onGround) {
               double dy = target.posY - this.posY;
               double dist = (double)this.getDistance(target);
               if (dy > (double)2.0F && dist < (double)20.0F) {
                  double dx = target.posX - this.posX;
                  double dz = target.posZ - this.posZ;
                  double hdist = Math.sqrt(dx * dx + dz * dz);
                  if (hdist > (double)0.5F) {
                     this.motionX = dx / hdist * 0.8;
                     this.motionZ = dz / hdist * 0.8;
                  }

                  this.motionY = (double)0.5F + Math.min(dy * 0.1, 0.4);
                  this.velocityChanged = true;
               }
            }

            if (target != null && target.isEntityAlive() && this.pounceCooldown <= 0) {
               double dx = target.posX - this.posX;
               double dz = target.posZ - this.posZ;
               double dist = Math.sqrt(dx * dx + dz * dz);
               if (dist >= (double)5.0F && dist <= (double)12.0F) {
                  double hdist = Math.sqrt(dx * dx + dz * dz);
                  this.motionX = dx / hdist * 1.2;
                  this.motionY = 0.6;
                  this.motionZ = dz / hdist * 1.2;
                  this.velocityChanged = true;
                  this.pounceCooldown = 160;
                  this.isPouncing = true;
                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 2, (double)0.5F, 0.2, (double)0.5F, (double)0.0F, new int[0]);
                  }
               }
            }

            if (this.isPouncing && this.onGround) {
               this.isPouncing = false;
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 3, (double)1.5F, 0.3, (double)1.5F, (double)0.0F, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)0.5F, this.posZ, 20, (double)1.5F, (double)0.5F, (double)1.5F, 0.08, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 15, (double)1.0F, (double)0.5F, (double)1.0F, 0.05, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.6F, 0.8F);
               AxisAlignedBB aoe = this.getEntityBoundingBox().grow((double)3.0F, (double)2.0F, (double)3.0F);

               for(EntityLivingBase victim : this.world.getEntitiesWithinAABB(EntityLivingBase.class, aoe, (e) -> e != this && e.isEntityAlive() && !(e instanceof EntitySummonedBeast) && !(e instanceof EntitySummonedBird) && !(e instanceof EntitySummonedChameleon))) {
                  if (this.summonerUUID == null || !victim.getUniqueID().equals(this.summonerUUID)) {
                     victim.hurtResistantTime = 0;
                     victim.attackEntityFrom(DamageSource.causeMobDamage(this), 50.0F);
                     double kx = victim.posX - this.posX;
                     double kz = victim.posZ - this.posZ;
                     double kDist = Math.sqrt(kx * kx + kz * kz);
                     if (kDist > 0.01) {
                        victim.motionX += kx / kDist * 0.8;
                        victim.motionY += 0.3;
                        victim.motionZ += kz / kDist * 0.8;
                        victim.velocityChanged = true;
                     }

                     if (this.world instanceof WorldServer) {
                        ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, victim.posX, victim.posY + (double)1.0F, victim.posZ, 10, 0.3, (double)0.5F, 0.3, 0.2, new int[0]);
                     }
                  }
               }
            }

         }
      }

      public void onDeath(DamageSource cause) {
         super.onDeath(cause);
         this.despawnParticlesOnly();
      }

      private void despawnWithParticles() {
         this.despawnParticlesOnly();
         this.setDead();
      }

      private void despawnParticlesOnly() {
         if (!this.world.isRemote && this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 30, 0.8, (double)1.0F, 0.8, 0.1, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 20, 0.6, 0.8, 0.6, 0.08, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.8F, 1.5F);
      }

      public boolean attackEntityAsMob(Entity target) {
         float damage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WOLF_GROWL, SoundCategory.HOSTILE, 1.5F, 0.7F);
         if (target instanceof EntityLivingBase) {
            ((EntityLivingBase)target).hurtResistantTime = 0;
         }

         boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
         if (!hit && target instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase)target;
            living.setHealth(living.getHealth() - damage);
            hit = true;
         }

         if (hit && target instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase)target;
            living.hurtResistantTime = 0;
            boolean trueHit = target.attackEntityFrom(DamageSource.MAGIC, damage * 0.4F);
            if (!trueHit) {
               living.setHealth(living.getHealth() - damage * 0.4F);
            }

            double kbX = target.posX - this.posX;
            double kbZ = target.posZ - this.posZ;
            double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
            if (kbDist > 0.1) {
               target.motionX += kbX / kbDist * (double)0.5F;
               target.motionY += 0.15;
               target.motionZ += kbZ / kbDist * (double)0.5F;
               target.velocityChanged = true;
            }
         }

         if (hit && this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, target.posX, target.posY + (double)1.0F, target.posZ, 12, 0.4, 0.4, 0.4, 0.15, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, target.posX, target.posY + (double)1.5F, target.posZ, 5, 0.3, 0.3, 0.3, 0.1, new int[0]);
         }

         return hit;
      }

      public boolean getCanSpawnHere() {
         return true;
      }

      protected boolean canDespawn() {
         return false;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("beastLifetime", this.lifetime);
         compound.setInteger("pounceCooldown", this.pounceCooldown);
         if (this.summonerUUID != null) {
            compound.setLong("summonerMost", this.summonerUUID.getMostSignificantBits());
            compound.setLong("summonerLeast", this.summonerUUID.getLeastSignificantBits());
         }

      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("beastLifetime");
         this.pounceCooldown = compound.getInteger("pounceCooldown");
         if (compound.hasKey("summonerMost") && compound.hasKey("summonerLeast")) {
            this.summonerUUID = new UUID(compound.getLong("summonerMost"), compound.getLong("summonerLeast"));
         }

      }
   }

   public static class EntityCustom extends QuestNpcBase {
      private final List<EntityCreature> activeSummons = new ArrayList();
      private int summonCooldown = 0;
      private boolean summonBurstUsed = false;
      private boolean animalIntroPlayed = false;
      private static final int SUMMON_BASE_CD = 600;
      private static final int MAX_ACTIVE_SUMMONS = 2;
      private static final float SUMMON_BURST_HP_THRESHOLD = 0.4F;
      private static final double PREFERRED_MIN_DIST = (double)10.0F;
      private static final double PREFERRED_MAX_DIST = (double)15.0F;
      private int rangedAttackTimer = 0;

      public EntityCustom(World world) {
         super(world);
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         this.consecutiveInvulnerableTicks = 0;
         return super.attackEntityFrom(source, amount);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processAnimalPathCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.summonCooldown > 0) {
            --this.summonCooldown;
         }

      }

      protected void resetCombatState() {
         this.summonBurstUsed = false;
         this.animalIntroPlayed = false;
         this.summonCooldown = 0;
         this.rangedAttackTimer = 0;

         for(EntityCreature summon : this.activeSummons) {
            if (summon != null && summon.isEntityAlive()) {
               summon.setDead();
            }
         }

         this.activeSummons.clear();
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("summonBurstUsed", this.summonBurstUsed);
         compound.setBoolean("animalIntroPlayed", this.animalIntroPlayed);
         compound.setInteger("summonCooldown", this.summonCooldown);
         compound.setInteger("rangedAttackTimer", this.rangedAttackTimer);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.summonBurstUsed = compound.getBoolean("summonBurstUsed");
         this.animalIntroPlayed = compound.getBoolean("animalIntroPlayed");
         this.summonCooldown = compound.hasKey("summonCooldown") ? compound.getInteger("summonCooldown") : 0;
         this.rangedAttackTimer = compound.hasKey("rangedAttackTimer") ? compound.getInteger("rangedAttackTimer") : 0;
      }

      protected void onCombatDeath() {
         for(EntityCreature summon : this.activeSummons) {
            if (summon != null && summon.isEntityAlive()) {
               summon.setDead();
            }
         }

         this.activeSummons.clear();
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 50, (double)1.0F, (double)1.5F, (double)1.0F, 0.15, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 30, 0.8, (double)1.0F, 0.8, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.2F, 1.0F);
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         return amount;
      }

      private void processAnimalPathCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            this.setAttackTarget(target);
            ++this.rangedAttackTimer;
            float hpPercent = this.getHealth() / this.getMaxHealth();
            this.cleanupDeadSummons();
            if (!this.animalIntroPlayed) {
               this.animalIntroPlayed = true;
               this.animalBroadcast("§5Pain: §dYou will learn the meaning of pain through my summons.");
            }

            if (!this.summonBurstUsed && hpPercent <= 0.4F) {
               this.summonBurstUsed = true;
               this.animalBroadcast("§5Pain: §dKuchiyose no Jutsu! Come forth, all of you!");
               this.spawnSummonedCreature(target, 0, 0);
               this.spawnSummonedCreature(target, 1, 1);
               this.spawnSummonedCreature(target, 2, 2);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 60, (double)2.0F, (double)2.0F, (double)2.0F, 0.15, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 40, (double)1.5F, (double)1.5F, (double)1.5F, 0.1, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 0.6F);
               this.summonCooldown = this.cdMul(600);
            } else if (this.summonCooldown <= 0 && this.activeSummons.size() < 2 && dist >= (double)15.0F && dist <= (double)25.0F && this.rand.nextFloat() < 0.35F) {
               this.animalBroadcast("§5Pain: §dKuchiyose no Jutsu!");
               this.spawnSummonedCreature(target, 0, this.pickRandomSummonType());
               this.summonCooldown = this.cdMul(600);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)1.0F, 1.2, (double)1.0F, 0.1, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)0.5F, this.posZ, 15, 0.8, 0.8, 0.8, 0.06, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0F, 0.8F);
            } else if (this.summonCooldown <= 0 && this.activeSummons.isEmpty() && dist >= (double)8.0F && dist < (double)15.0F && this.rand.nextFloat() < 0.25F) {
               this.animalBroadcast("§5Pain: §dKuchiyose no Jutsu!");
               this.spawnSummonedCreature(target, 0, this.pickRandomSummonType());
               this.summonCooldown = this.cdMul(600);
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 25, 0.8, (double)1.0F, 0.8, 0.08, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.9F, 0.9F);
            } else if (this.activeSummons.isEmpty() && this.summonCooldown > 0 && dist <= (double)4.0F) {
               if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0) {
                  this.performMeleeSwing(target);
                  this.consecutiveInvulnerableTicks = 0;
               }

               this.getNavigator().tryMoveToEntityLiving(target, 1.2);
            } else {
               if (this.rangedAttackTimer >= 200 && dist >= (double)5.0F && dist <= (double)20.0F) {
                  this.rangedAttackTimer = 0;
                  this.consecutiveInvulnerableTicks = 0;
                  if (this.natureType != 0 && this.jutsuCooldown <= 0) {
                     this.startNatureJutsu(target);
                     this.jutsuCooldown = this.cdMul(100);
                  } else if (dist <= (double)6.0F && this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0) {
                     this.performMeleeSwing(target);
                  }
               }

               if (dist < (double)10.0F) {
                  double dx = this.posX - target.posX;
                  double dz = this.posZ - target.posZ;
                  double horizDist = Math.sqrt(dx * dx + dz * dz);
                  if (horizDist > (double)0.5F) {
                     double nx = dx / horizDist;
                     double nz = dz / horizDist;
                     this.getNavigator().tryMoveToXYZ(this.posX + nx * (double)8.0F, this.posY, this.posZ + nz * (double)8.0F, 1.3);
                  }

                  if (dist < (double)5.0F && this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
                     this.performMeleeSwing(target);
                  }

               } else if (dist > (double)25.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, 1.1);
               } else {
                  if (dist >= (double)10.0F && dist <= (double)15.0F) {
                     double dx = target.posX - this.posX;
                     double dz = target.posZ - this.posZ;
                     double horizDist = Math.sqrt(dx * dx + dz * dz);
                     if (horizDist > (double)0.5F) {
                        double strafeX = -dz / horizDist;
                        double strafeZ = dx / horizDist;
                        if (this.ticksExisted % 80 < 40) {
                           strafeX = -strafeX;
                           strafeZ = -strafeZ;
                        }

                        this.getNavigator().tryMoveToXYZ(this.posX + strafeX * (double)4.0F, this.posY, this.posZ + strafeZ * (double)4.0F, 0.85);
                     }
                  } else if (dist > (double)15.0F && dist <= (double)25.0F) {
                     this.getNavigator().tryMoveToEntityLiving(target, 0.8);
                  }

                  if (this.natureType != 0 && this.jutsuCooldown <= 0 && dist >= (double)5.0F && dist <= (double)18.0F && this.rand.nextFloat() < 0.12F) {
                     this.startNatureJutsu(target);
                     this.jutsuCooldown = this.cdMul(100);
                  }

               }
            }
         }
      }

      private int pickRandomSummonType() {
         float roll = this.rand.nextFloat();
         if (roll < 0.4F) {
            return 0;
         } else {
            return roll < 0.7F ? 1 : 2;
         }
      }

      private void spawnSummonedCreature(EntityLivingBase target, int offsetIndex, int summonType) {
         EntityCreature creature;
         switch (summonType) {
            case 1:
               creature = new EntitySummonedBird(this.world);
               ((EntitySummonedBird)creature).setSummoner(this);
               break;
            case 2:
               creature = new EntitySummonedChameleon(this.world);
               ((EntitySummonedChameleon)creature).setSummoner(this);
               break;
            default:
               creature = new EntitySummonedBeast(this.world);
               ((EntitySummonedBeast)creature).setSummoner(this);
         }

         double angle = 2.0943951023931953 * (double)offsetIndex + this.rand.nextDouble() * (double)0.5F;
         double radius = (double)3.0F + this.rand.nextDouble() * (double)2.0F;
         double spawnX = target.posX + Math.cos(angle) * radius;
         double spawnZ = target.posZ + Math.sin(angle) * radius;
         double spawnY = target.posY;
         if (summonType == 1) {
            spawnY = target.posY + (double)8.0F;
         }

         creature.setPosition(spawnX, spawnY, spawnZ);
         if (creature instanceof EntityLivingBase) {
            creature.setAttackTarget(target);
         }

         this.world.spawnEntity(creature);
         this.activeSummons.add(creature);
         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, spawnX, spawnY + (double)1.0F, spawnZ, 20, (double)0.5F, 0.8, (double)0.5F, 0.08, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, spawnX, spawnY + (double)0.5F, spawnZ, 10, 0.4, (double)0.5F, 0.4, 0.05, new int[0]);
         }

      }

      private void cleanupDeadSummons() {
         Iterator<EntityCreature> iter = this.activeSummons.iterator();

         while(iter.hasNext()) {
            EntityCreature summon = (EntityCreature)iter.next();
            if (summon == null || !summon.isEntityAlive() || summon.isDead) {
               iter.remove();
            }
         }

      }

      private void animalBroadcast(String msg) {
         for(EntityPlayer p : this.world.playerEntities) {
            if ((double)p.getDistance(this) < (double)48.0F && p instanceof EntityPlayerMP) {
               ((EntityPlayerMP)p).sendMessage(new TextComponentString(msg));
            }
         }

      }
   }
}
