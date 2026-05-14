
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.BossInfo.Overlay;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityWorldBossGyuki extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 402;

   public EntityWorldBossGyuki(ElementsInfTsukAddon instance) {
      super(instance, 982);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "worldbossgyuki"), 402).name("worldbossgyuki").tracker(96, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, GyukiRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class GyukiRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod", "textures/eighttails.png");

      public GyukiRenderer(RenderManager renderManager) {
         super(renderManager, new ModelGyuki(), 5.0F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         float s = 0.7F;
         GlStateManager.scale(s, s, s);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelGyuki extends ModelBiped {
      private final ModelRenderer eyes;
      private final ModelRenderer bipedBody_r1;
      private final ModelRenderer bipedBody_r2;
      private final ModelRenderer bipedHead_r1;
      private final ModelRenderer hornRight1;
      private final ModelRenderer hornRight1_r1;
      private final ModelRenderer hornRight2;
      private final ModelRenderer hornRight2_r1;
      private final ModelRenderer hornRight3;
      private final ModelRenderer hornRight3_r1;
      private final ModelRenderer hornRight4;
      private final ModelRenderer hornRight4_r1;
      private final ModelRenderer hornRight5;
      private final ModelRenderer hornRight5_r1;
      private final ModelRenderer hornRight6;
      private final ModelRenderer hornRight7_r1;
      private final ModelRenderer hornLeft1;
      private final ModelRenderer hornRight2_r2;
      private final ModelRenderer hornLeft2;
      private final ModelRenderer hornRight3_r2;
      private final ModelRenderer hornLeft3;
      private final ModelRenderer hornRight4_r2;
      private final ModelRenderer hornLeft4;
      private final ModelRenderer hornRight5_r2;
      private final ModelRenderer hornLeft5;
      private final ModelRenderer hornRight6_r1;
      private final ModelRenderer hornLeft6;
      private final ModelRenderer hornRight8_r1;
      private final ModelRenderer hornRight13;
      private final ModelRenderer hornRight13_r1;
      private final ModelRenderer hornRight14;
      private final ModelRenderer hornRight14_r1;
      private final ModelRenderer hornRight15;
      private final ModelRenderer hornRight15_r1;
      private final ModelRenderer hornRight16;
      private final ModelRenderer hornRight16_r1;
      private final ModelRenderer hornRight17;
      private final ModelRenderer hornRight16_r2;
      private final ModelRenderer hornRight18;
      private final ModelRenderer hornRight18_r1;
      private final ModelRenderer hornRight17_r1;
      private final ModelRenderer hornLeft13;
      private final ModelRenderer hornRight14_r2;
      private final ModelRenderer hornLeft14;
      private final ModelRenderer hornRight15_r2;
      private final ModelRenderer hornLeft15;
      private final ModelRenderer hornRight16_r3;
      private final ModelRenderer hornLeft16;
      private final ModelRenderer hornRight17_r2;
      private final ModelRenderer hornLeft17;
      private final ModelRenderer hornRight17_r3;
      private final ModelRenderer hornLeft18;
      private final ModelRenderer hornRight19_r1;
      private final ModelRenderer hornRight18_r2;
      private final ModelRenderer snout;
      private final ModelRenderer bone3;
      private final ModelRenderer bone6;
      private final ModelRenderer bone;
      private final ModelRenderer bone4;
      private final ModelRenderer bone2;
      private final ModelRenderer bone5;
      private final ModelRenderer jaw;
      private final ModelRenderer bone9;
      private final ModelRenderer bone10;
      private final ModelRenderer bone8;
      private final ModelRenderer dick;
      private final ModelRenderer dick1;
      private final ModelRenderer chest;
      private final ModelRenderer chest_r1;
      private final ModelRenderer chest_r2;
      private final ModelRenderer chest_r3;
      private final ModelRenderer hump;
      private final ModelRenderer hump_r1;
      private final ModelRenderer bone7;
      private final ModelRenderer upperArmRight;
      private final ModelRenderer foreArmRight;
      private final ModelRenderer foreArmRight_r1;
      private final ModelRenderer foreArmRight_r2;
      private final ModelRenderer foreArmRight_r3;
      private final ModelRenderer upperArmLeft;
      private final ModelRenderer foreArmLeft;
      private final ModelRenderer foreArmRight_r4;
      private final ModelRenderer foreArmRight_r5;
      private final ModelRenderer foreArmRight_r6;
      private final ModelRenderer[][] tail = new ModelRenderer[8][8];
      private final float[][] tailSwayX = new float[8][8];
      private final float[][] tailSwayY = new float[8][8];
      private final float[][] tailSwayZ = new float[8][8];
      private final Random rand = new Random();

      public ModelGyuki() {
         this.textureWidth = 64;
         this.textureHeight = 64;
         (this.bipedHeadwear = new ModelRenderer(this)).setRotationPoint(0.0F, 23.5F, 0.0F);
         (this.eyes = new ModelRenderer(this)).setRotationPoint(0.0F, -19.5F, -5.0F);
         this.bipedHeadwear.addChild(this.eyes);
         this.eyes.cubeList.add(new ModelBox(this.eyes, 32, 18, -3.0F, -6.0F, -6.1F, 6, 2, 0, 0.0F, false));
         (this.bipedBody = new ModelRenderer(this)).setRotationPoint(0.0F, 23.5F, 0.0F);
         this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 18, 48, -6.7F, -9.5F, -4.5F, 7, 8, 1, -0.3F, false));
         this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 18, 48, -0.3F, -9.5F, -4.5F, 7, 8, 1, -0.3F, true));
         (this.bipedBody_r1 = new ModelRenderer(this)).setRotationPoint(7.75F, -11.4F, 0.0F);
         this.bipedBody.addChild(this.bipedBody_r1);
         this.setRotationAngle(this.bipedBody_r1, 0.0F, 0.0F, -0.1745F);
         this.bipedBody_r1.cubeList.add(new ModelBox(this.bipedBody_r1, 0, 0, -7.0F, 0.0F, -4.0F, 7, 10, 8, 0.0F, true));
         (this.bipedBody_r2 = new ModelRenderer(this)).setRotationPoint(-7.75F, -11.4F, 0.0F);
         this.bipedBody.addChild(this.bipedBody_r2);
         this.setRotationAngle(this.bipedBody_r2, 0.0F, 0.0F, 0.1745F);
         this.bipedBody_r2.cubeList.add(new ModelBox(this.bipedBody_r2, 0, 0, 0.0F, 0.0F, -4.0F, 7, 10, 8, 0.0F, false));
         (this.bipedHead = new ModelRenderer(this)).setRotationPoint(0.0F, -19.5F, -5.0F);
         this.bipedBody.addChild(this.bipedHead);
         this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 0, 34, -3.0F, -6.0F, -6.0F, 6, 8, 6, 0.0F, false));
         (this.bipedHead_r1 = new ModelRenderer(this)).setRotationPoint(0.0F, -6.0F, -5.0F);
         this.bipedHead.addChild(this.bipedHead_r1);
         this.setRotationAngle(this.bipedHead_r1, 0.096F, 0.0F, 0.0F);
         this.bipedHead_r1.cubeList.add(new ModelBox(this.bipedHead_r1, 0, 34, -3.0F, -0.5777F, -1.229F, 6, 2, 6, -0.1F, false));
         (this.hornRight1 = new ModelRenderer(this)).setRotationPoint(-3.1F, -6.4F, -4.0F);
         this.bipedHead.addChild(this.hornRight1);
         this.setRotationAngle(this.hornRight1, 0.3334F, 0.4354F, 0.2261F);
         (this.hornRight1_r1 = new ModelRenderer(this)).setRotationPoint(2.5F, -2.9623F, -0.5008F);
         this.hornRight1.addChild(this.hornRight1_r1);
         this.setRotationAngle(this.hornRight1_r1, 0.0F, 0.0F, 0.1309F);
         this.hornRight1_r1.cubeList.add(new ModelBox(this.hornRight1_r1, 0, 20, -2.6314F, 2.804F, -0.7839F, 2, 2, 2, 0.0F, false));
         (this.hornRight2 = new ModelRenderer(this)).setRotationPoint(-1.0F, -0.1895F, 3.1566F);
         this.hornRight1.addChild(this.hornRight2);
         this.setRotationAngle(this.hornRight2, 0.0F, -0.3491F, 0.0F);
         (this.hornRight2_r1 = new ModelRenderer(this)).setRotationPoint(0.2591F, 0.7156F, -3.2926F);
         this.hornRight2.addChild(this.hornRight2_r1);
         this.setRotationAngle(this.hornRight2_r1, 0.0698F, 0.1309F, 0.0873F);
         this.hornRight2_r1.cubeList.add(new ModelBox(this.hornRight2_r1, 0, 20, -2.5572F, -0.9354F, -1.222F, 2, 2, 2, -0.05F, false));
         (this.hornRight3 = new ModelRenderer(this)).setRotationPoint(-1.0F, 0.0F, 0.0F);
         this.hornRight2.addChild(this.hornRight3);
         this.setRotationAngle(this.hornRight3, 0.0F, -0.3491F, 0.0F);
         (this.hornRight3_r1 = new ModelRenderer(this)).setRotationPoint(-1.7F, 0.5F, -2.6F);
         this.hornRight3.addChild(this.hornRight3_r1);
         this.setRotationAngle(this.hornRight3_r1, 0.1275F, -1.0348F, -0.1029F);
         this.hornRight3_r1.cubeList.add(new ModelBox(this.hornRight3_r1, 0, 20, -1.1721F, -1.0071F, -0.2325F, 2, 2, 2, -0.1F, false));
         (this.hornRight4 = new ModelRenderer(this)).setRotationPoint(-1.0F, 0.0F, 0.0F);
         this.hornRight3.addChild(this.hornRight4);
         this.setRotationAngle(this.hornRight4, 0.0F, -0.3491F, 0.0F);
         (this.hornRight4_r1 = new ModelRenderer(this)).setRotationPoint(-2.5F, 1.0F, -1.9F);
         this.hornRight4.addChild(this.hornRight4_r1);
         this.setRotationAngle(this.hornRight4_r1, 0.0F, -1.0036F, 0.0F);
         this.hornRight4_r1.cubeList.add(new ModelBox(this.hornRight4_r1, 0, 20, -2.0076F, -1.4756F, -0.9969F, 2, 2, 2, -0.15F, false));
         (this.hornRight5 = new ModelRenderer(this)).setRotationPoint(-1.0F, 0.0F, 0.0F);
         this.hornRight4.addChild(this.hornRight5);
         this.setRotationAngle(this.hornRight5, 0.0F, -0.3491F, 0.0F);
         (this.hornRight5_r1 = new ModelRenderer(this)).setRotationPoint(-3.4F, 0.7F, -0.3F);
         this.hornRight5.addChild(this.hornRight5_r1);
         this.setRotationAngle(this.hornRight5_r1, 0.0F, -0.1745F, 0.0F);
         this.hornRight5_r1.cubeList.add(new ModelBox(this.hornRight5_r1, 0, 20, -1.9463F, -1.1756F, -2.9677F, 2, 2, 2, -0.2F, false));
         (this.hornRight6 = new ModelRenderer(this)).setRotationPoint(-1.5F, 0.0F, 0.0F);
         this.hornRight5.addChild(this.hornRight6);
         this.setRotationAngle(this.hornRight6, 0.0F, -0.3491F, 0.0F);
         (this.hornRight7_r1 = new ModelRenderer(this)).setRotationPoint(-3.8932F, 1.5F, 0.4376F);
         this.hornRight6.addChild(this.hornRight7_r1);
         this.setRotationAngle(this.hornRight7_r1, 0.0F, 0.3927F, 0.0F);
         this.hornRight7_r1.cubeList.add(new ModelBox(this.hornRight7_r1, 0, 20, -1.6783F, -1.9756F, -2.5373F, 2, 2, 2, -0.6F, false));
         this.hornRight7_r1.cubeList.add(new ModelBox(this.hornRight7_r1, 0, 20, -0.8783F, -1.9756F, -2.5373F, 2, 2, 2, -0.4F, false));
         (this.hornLeft1 = new ModelRenderer(this)).setRotationPoint(3.1F, -6.4F, -4.0F);
         this.bipedHead.addChild(this.hornLeft1);
         this.setRotationAngle(this.hornLeft1, 0.3334F, -0.4354F, -0.2261F);
         (this.hornRight2_r2 = new ModelRenderer(this)).setRotationPoint(-2.5F, -2.9623F, -0.5008F);
         this.hornLeft1.addChild(this.hornRight2_r2);
         this.setRotationAngle(this.hornRight2_r2, 0.0F, 0.0F, -0.1309F);
         this.hornRight2_r2.cubeList.add(new ModelBox(this.hornRight2_r2, 0, 20, 0.6314F, 2.804F, -0.7839F, 2, 2, 2, 0.0F, true));
         (this.hornLeft2 = new ModelRenderer(this)).setRotationPoint(1.0F, -0.1895F, 3.1566F);
         this.hornLeft1.addChild(this.hornLeft2);
         this.setRotationAngle(this.hornLeft2, 0.0F, 0.3491F, 0.0F);
         (this.hornRight3_r2 = new ModelRenderer(this)).setRotationPoint(-0.2591F, 0.7156F, -3.2926F);
         this.hornLeft2.addChild(this.hornRight3_r2);
         this.setRotationAngle(this.hornRight3_r2, 0.0698F, -0.1309F, -0.0873F);
         this.hornRight3_r2.cubeList.add(new ModelBox(this.hornRight3_r2, 0, 20, 0.5572F, -0.9354F, -1.222F, 2, 2, 2, -0.05F, true));
         (this.hornLeft3 = new ModelRenderer(this)).setRotationPoint(1.0F, 0.0F, 0.0F);
         this.hornLeft2.addChild(this.hornLeft3);
         this.setRotationAngle(this.hornLeft3, 0.0F, 0.3491F, 0.0F);
         (this.hornRight4_r2 = new ModelRenderer(this)).setRotationPoint(1.7F, 0.5F, -2.6F);
         this.hornLeft3.addChild(this.hornRight4_r2);
         this.setRotationAngle(this.hornRight4_r2, 0.1275F, 1.0348F, 0.1029F);
         this.hornRight4_r2.cubeList.add(new ModelBox(this.hornRight4_r2, 0, 20, -0.8279F, -1.0071F, -0.2325F, 2, 2, 2, -0.1F, true));
         (this.hornLeft4 = new ModelRenderer(this)).setRotationPoint(1.0F, 0.0F, 0.0F);
         this.hornLeft3.addChild(this.hornLeft4);
         this.setRotationAngle(this.hornLeft4, 0.0F, 0.3491F, 0.0F);
         (this.hornRight5_r2 = new ModelRenderer(this)).setRotationPoint(2.5F, 1.0F, -1.9F);
         this.hornLeft4.addChild(this.hornRight5_r2);
         this.setRotationAngle(this.hornRight5_r2, 0.0F, 1.0036F, 0.0F);
         this.hornRight5_r2.cubeList.add(new ModelBox(this.hornRight5_r2, 0, 20, 0.0076F, -1.4756F, -0.9969F, 2, 2, 2, -0.15F, true));
         (this.hornLeft5 = new ModelRenderer(this)).setRotationPoint(1.0F, 0.0F, 0.0F);
         this.hornLeft4.addChild(this.hornLeft5);
         this.setRotationAngle(this.hornLeft5, 0.0F, 0.3491F, 0.0F);
         (this.hornRight6_r1 = new ModelRenderer(this)).setRotationPoint(3.4F, 0.7F, -0.3F);
         this.hornLeft5.addChild(this.hornRight6_r1);
         this.setRotationAngle(this.hornRight6_r1, 0.0F, 0.1745F, 0.0F);
         this.hornRight6_r1.cubeList.add(new ModelBox(this.hornRight6_r1, 0, 20, -0.0537F, -1.1756F, -2.9677F, 2, 2, 2, -0.2F, true));
         (this.hornLeft6 = new ModelRenderer(this)).setRotationPoint(1.5F, 0.0F, 0.0F);
         this.hornLeft5.addChild(this.hornLeft6);
         this.setRotationAngle(this.hornLeft6, 0.0F, 0.3491F, 0.0F);
         (this.hornRight8_r1 = new ModelRenderer(this)).setRotationPoint(3.8932F, 1.5F, 0.4376F);
         this.hornLeft6.addChild(this.hornRight8_r1);
         this.setRotationAngle(this.hornRight8_r1, 0.0F, -0.3927F, 0.0F);
         this.hornRight8_r1.cubeList.add(new ModelBox(this.hornRight8_r1, 0, 20, -0.3217F, -1.9756F, -2.5373F, 2, 2, 2, -0.6F, true));
         this.hornRight8_r1.cubeList.add(new ModelBox(this.hornRight8_r1, 0, 20, -1.1217F, -1.9756F, -2.5373F, 2, 2, 2, -0.4F, true));
         (this.hornRight13 = new ModelRenderer(this)).setRotationPoint(-1.5F, -7.5F, 1.0F);
         this.bipedHead.addChild(this.hornRight13);
         this.setRotationAngle(this.hornRight13, 0.0F, 0.5236F, 1.309F);
         (this.hornRight13_r1 = new ModelRenderer(this)).setRotationPoint(3.6355F, 0.3154F, -0.6952F);
         this.hornRight13.addChild(this.hornRight13_r1);
         this.setRotationAngle(this.hornRight13_r1, 0.0F, 0.0F, -0.5236F);
         this.hornRight13_r1.cubeList.add(new ModelBox(this.hornRight13_r1, 0, 20, -2.1426F, -1.3981F, -1.6582F, 2, 2, 2, 0.2F, false));
         (this.hornRight14 = new ModelRenderer(this)).setRotationPoint(-1.0F, 0.0F, 0.0F);
         this.hornRight13.addChild(this.hornRight14);
         this.setRotationAngle(this.hornRight14, 0.0F, 0.0436F, 0.0F);
         (this.hornRight14_r1 = new ModelRenderer(this)).setRotationPoint(1.2508F, 0.186F, 4.2042F);
         this.hornRight14.addChild(this.hornRight14_r1);
         this.setRotationAngle(this.hornRight14_r1, 0.0F, 0.0F, -0.2182F);
         this.hornRight14_r1.cubeList.add(new ModelBox(this.hornRight14_r1, 0, 20, -0.3729F, 0.1031F, -6.2971F, 2, 2, 2, 0.1F, false));
         (this.hornRight15 = new ModelRenderer(this)).setRotationPoint(-1.0F, 0.0F, 0.0F);
         this.hornRight14.addChild(this.hornRight15);
         this.setRotationAngle(this.hornRight15, 0.0F, 0.0436F, 0.0F);
         (this.hornRight15_r1 = new ModelRenderer(this)).setRotationPoint(1.0993F, 1.0595F, 4.8237F);
         this.hornRight15.addChild(this.hornRight15_r1);
         this.setRotationAngle(this.hornRight15_r1, 0.0F, 0.0F, -0.0873F);
         this.hornRight15_r1.cubeList.add(new ModelBox(this.hornRight15_r1, 0, 20, -0.8987F, -0.5986F, -6.7779F, 2, 2, 2, 0.0F, false));
         (this.hornRight16 = new ModelRenderer(this)).setRotationPoint(-1.0F, 0.0F, 0.0F);
         this.hornRight15.addChild(this.hornRight16);
         this.setRotationAngle(this.hornRight16, 0.0F, 0.0436F, 0.0F);
         (this.hornRight16_r1 = new ModelRenderer(this)).setRotationPoint(0.7279F, 2.4387F, 4.0239F);
         this.hornRight16.addChild(this.hornRight16_r1);
         this.setRotationAngle(this.hornRight16_r1, 0.0F, 0.0F, -0.48F);
         this.hornRight16_r1.cubeList.add(new ModelBox(this.hornRight16_r1, 0, 20, -0.4778F, -1.4562F, -5.9402F, 2, 2, 2, -0.06F, false));
         (this.hornRight17 = new ModelRenderer(this)).setRotationPoint(-1.0F, 0.0F, 0.0F);
         this.hornRight16.addChild(this.hornRight17);
         this.setRotationAngle(this.hornRight17, 0.0F, 0.0436F, 0.0F);
         (this.hornRight16_r2 = new ModelRenderer(this)).setRotationPoint(0.8171F, 3.3884F, 4.0309F);
         this.hornRight17.addChild(this.hornRight16_r2);
         this.setRotationAngle(this.hornRight16_r2, 0.0F, 0.0F, -0.9599F);
         this.hornRight16_r2.cubeList.add(new ModelBox(this.hornRight16_r2, 0, 20, -0.1233F, -1.3028F, -5.8717F, 2, 2, 2, -0.15F, false));
         (this.hornRight18 = new ModelRenderer(this)).setRotationPoint(-1.0F, 0.0F, 0.0F);
         this.hornRight17.addChild(this.hornRight18);
         this.setRotationAngle(this.hornRight18, 0.0F, 0.0436F, 0.0F);
         (this.hornRight18_r1 = new ModelRenderer(this)).setRotationPoint(1.4163F, 3.6913F, 4.2057F);
         this.hornRight18.addChild(this.hornRight18_r1);
         this.setRotationAngle(this.hornRight18_r1, 0.0F, 0.0F, -1.1345F);
         this.hornRight18_r1.cubeList.add(new ModelBox(this.hornRight18_r1, 0, 20, -1.793F, -1.1029F, -5.9694F, 2, 2, 2, -0.6F, false));
         (this.hornRight17_r1 = new ModelRenderer(this)).setRotationPoint(1.8171F, 3.3884F, 4.0309F);
         this.hornRight18.addChild(this.hornRight17_r1);
         this.setRotationAngle(this.hornRight17_r1, 0.0F, 0.0F, -1.1345F);
         this.hornRight17_r1.cubeList.add(new ModelBox(this.hornRight17_r1, 0, 20, -1.2479F, -1.3053F, -5.7907F, 2, 2, 2, -0.4F, false));
         (this.hornLeft13 = new ModelRenderer(this)).setRotationPoint(1.5F, -7.5F, 1.0F);
         this.bipedHead.addChild(this.hornLeft13);
         this.setRotationAngle(this.hornLeft13, 0.0F, -0.5236F, -1.309F);
         (this.hornRight14_r2 = new ModelRenderer(this)).setRotationPoint(-3.6355F, 0.3154F, -0.6952F);
         this.hornLeft13.addChild(this.hornRight14_r2);
         this.setRotationAngle(this.hornRight14_r2, 0.0F, 0.0F, 0.5236F);
         this.hornRight14_r2.cubeList.add(new ModelBox(this.hornRight14_r2, 0, 20, 0.1426F, -1.3981F, -1.6582F, 2, 2, 2, 0.2F, true));
         (this.hornLeft14 = new ModelRenderer(this)).setRotationPoint(1.0F, 0.0F, 0.0F);
         this.hornLeft13.addChild(this.hornLeft14);
         this.setRotationAngle(this.hornLeft14, 0.0F, -0.0436F, 0.0F);
         (this.hornRight15_r2 = new ModelRenderer(this)).setRotationPoint(-1.2508F, 0.186F, 4.2042F);
         this.hornLeft14.addChild(this.hornRight15_r2);
         this.setRotationAngle(this.hornRight15_r2, 0.0F, 0.0F, 0.2182F);
         this.hornRight15_r2.cubeList.add(new ModelBox(this.hornRight15_r2, 0, 20, -1.6271F, 0.1031F, -6.2971F, 2, 2, 2, 0.1F, true));
         (this.hornLeft15 = new ModelRenderer(this)).setRotationPoint(1.0F, 0.0F, 0.0F);
         this.hornLeft14.addChild(this.hornLeft15);
         this.setRotationAngle(this.hornLeft15, 0.0F, -0.0436F, 0.0F);
         (this.hornRight16_r3 = new ModelRenderer(this)).setRotationPoint(-1.0993F, 1.0595F, 4.8237F);
         this.hornLeft15.addChild(this.hornRight16_r3);
         this.setRotationAngle(this.hornRight16_r3, 0.0F, 0.0F, 0.0873F);
         this.hornRight16_r3.cubeList.add(new ModelBox(this.hornRight16_r3, 0, 20, -1.1013F, -0.5986F, -6.7779F, 2, 2, 2, 0.0F, true));
         (this.hornLeft16 = new ModelRenderer(this)).setRotationPoint(1.0F, 0.0F, 0.0F);
         this.hornLeft15.addChild(this.hornLeft16);
         this.setRotationAngle(this.hornLeft16, 0.0F, -0.0436F, 0.0F);
         (this.hornRight17_r2 = new ModelRenderer(this)).setRotationPoint(-0.7279F, 2.4387F, 4.0239F);
         this.hornLeft16.addChild(this.hornRight17_r2);
         this.setRotationAngle(this.hornRight17_r2, 0.0F, 0.0F, 0.48F);
         this.hornRight17_r2.cubeList.add(new ModelBox(this.hornRight17_r2, 0, 20, -1.5222F, -1.4562F, -5.9402F, 2, 2, 2, -0.06F, true));
         (this.hornLeft17 = new ModelRenderer(this)).setRotationPoint(1.0F, 0.0F, 0.0F);
         this.hornLeft16.addChild(this.hornLeft17);
         this.setRotationAngle(this.hornLeft17, 0.0F, -0.0436F, 0.0F);
         (this.hornRight17_r3 = new ModelRenderer(this)).setRotationPoint(-0.8171F, 3.3884F, 4.0309F);
         this.hornLeft17.addChild(this.hornRight17_r3);
         this.setRotationAngle(this.hornRight17_r3, 0.0F, 0.0F, 0.9599F);
         this.hornRight17_r3.cubeList.add(new ModelBox(this.hornRight17_r3, 0, 20, -1.8767F, -1.3028F, -5.8717F, 2, 2, 2, -0.15F, true));
         (this.hornLeft18 = new ModelRenderer(this)).setRotationPoint(1.0F, 0.0F, 0.0F);
         this.hornLeft17.addChild(this.hornLeft18);
         this.setRotationAngle(this.hornLeft18, 0.0F, -0.0436F, 0.0F);
         (this.hornRight19_r1 = new ModelRenderer(this)).setRotationPoint(-1.4163F, 3.6913F, 4.2057F);
         this.hornLeft18.addChild(this.hornRight19_r1);
         this.setRotationAngle(this.hornRight19_r1, 0.0F, 0.0F, 1.1345F);
         this.hornRight19_r1.cubeList.add(new ModelBox(this.hornRight19_r1, 0, 20, -0.207F, -1.1029F, -5.9694F, 2, 2, 2, -0.6F, true));
         (this.hornRight18_r2 = new ModelRenderer(this)).setRotationPoint(-1.8171F, 3.3884F, 4.0309F);
         this.hornLeft18.addChild(this.hornRight18_r2);
         this.setRotationAngle(this.hornRight18_r2, 0.0F, 0.0F, 1.1345F);
         this.hornRight18_r2.cubeList.add(new ModelBox(this.hornRight18_r2, 0, 20, -0.7521F, -1.3053F, -5.7907F, 2, 2, 2, -0.4F, true));
         (this.snout = new ModelRenderer(this)).setRotationPoint(0.0F, -4.0F, -3.0F);
         this.bipedHead.addChild(this.snout);
         (this.bone3 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, 0.5F);
         this.snout.addChild(this.bone3);
         this.setRotationAngle(this.bone3, 0.2618F, 0.0F, 0.0F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 46, 44, -1.0F, 1.1036F, -7.6086F, 2, 2, 4, 0.1F, false));
         (this.bone6 = new ModelRenderer(this)).setRotationPoint(0.5F, -1.0F, 0.0F);
         this.snout.addChild(this.bone6);
         this.bone6.cubeList.add(new ModelBox(this.bone6, 24, 20, -2.0F, 2.0F, -7.0F, 3, 3, 4, 0.0F, false));
         (this.bone = new ModelRenderer(this)).setRotationPoint(-3.0F, -2.0F, 0.0F);
         this.snout.addChild(this.bone);
         this.setRotationAngle(this.bone, 0.2618F, -0.3491F, 0.0F);
         this.bone.cubeList.add(new ModelBox(this.bone, 50, 0, -1.0262F, 1.2022F, -7.2406F, 2, 2, 4, 0.0F, false));
         (this.bone4 = new ModelRenderer(this)).setRotationPoint(3.0F, -2.0F, 0.0F);
         this.snout.addChild(this.bone4);
         this.setRotationAngle(this.bone4, 0.2618F, 0.3491F, 0.0F);
         this.bone4.cubeList.add(new ModelBox(this.bone4, 50, 0, -0.9738F, 1.2022F, -7.2406F, 2, 2, 4, 0.0F, true));
         (this.bone2 = new ModelRenderer(this)).setRotationPoint(-3.0F, 2.0F, 0.0F);
         this.snout.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.0F, -0.3491F, 0.0F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 34, 48, -1.0262F, 0.0F, -6.819F, 2, 2, 4, 0.0F, false));
         (this.bone5 = new ModelRenderer(this)).setRotationPoint(3.0F, 2.0F, 0.0F);
         this.snout.addChild(this.bone5);
         this.setRotationAngle(this.bone5, 0.0F, 0.3491F, 0.0F);
         this.bone5.cubeList.add(new ModelBox(this.bone5, 34, 48, -0.9738F, 0.0F, -6.819F, 2, 2, 4, 0.0F, true));
         (this.jaw = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -5.85F);
         this.bipedHead.addChild(this.jaw);
         this.jaw.cubeList.add(new ModelBox(this.jaw, 24, 0, -1.5F, -0.1112F, -4.0845F, 3, 2, 4, 0.0F, false));
         (this.bone9 = new ModelRenderer(this)).setRotationPoint(-2.5F, 0.8888F, -0.0845F);
         this.jaw.addChild(this.bone9);
         this.setRotationAngle(this.bone9, 0.0F, -0.3491F, 0.0F);
         this.bone9.cubeList.add(new ModelBox(this.bone9, 50, 30, -0.5F, -1.0F, -4.0F, 2, 2, 4, 0.0F, false));
         (this.bone10 = new ModelRenderer(this)).setRotationPoint(2.5F, 0.8888F, -0.0845F);
         this.jaw.addChild(this.bone10);
         this.setRotationAngle(this.bone10, 0.0F, 0.3491F, 0.0F);
         this.bone10.cubeList.add(new ModelBox(this.bone10, 50, 30, -1.5F, -1.0F, -4.0F, 2, 2, 4, 0.0F, true));
         (this.bone8 = new ModelRenderer(this)).setRotationPoint(0.0F, 1.5F, 1.5F);
         this.jaw.addChild(this.bone8);
         this.setRotationAngle(this.bone8, -0.2618F, 0.0F, 0.0F);
         this.bone8.cubeList.add(new ModelBox(this.bone8, 0, 24, -1.0F, 1.3033F, -5.3618F, 2, 2, 2, 0.0F, false));
         (this.dick = new ModelRenderer(this)).setRotationPoint(-1.0F, -1.85F, -3.8F);
         this.bipedBody.addChild(this.dick);
         this.setRotationAngle(this.dick, 0.5236F, 0.0F, 0.0F);
         this.dick.cubeList.add(new ModelBox(this.dick, 0, 59, -2.0F, 0.0F, 0.0F, 6, 2, 2, 0.0F, false));
         (this.dick1 = new ModelRenderer(this)).setRotationPoint(0.0F, 2.0F, 0.0F);
         this.dick.addChild(this.dick1);
         this.setRotationAngle(this.dick1, 1.0908F, 0.0F, 0.0F);
         this.dick1.cubeList.add(new ModelBox(this.dick1, 40, 51, -2.0F, 0.0F, 0.0F, 6, 7, 6, 0.0F, false));
         (this.chest = new ModelRenderer(this)).setRotationPoint(1.0F, -12.5F, 4.0F);
         this.bipedBody.addChild(this.chest);
         this.setRotationAngle(this.chest, 0.5236F, 0.0F, 0.0F);
         (this.chest_r1 = new ModelRenderer(this)).setRotationPoint(3.15F, -6.0888F, -8.0189F);
         this.chest.addChild(this.chest_r1);
         this.setRotationAngle(this.chest_r1, -0.1309F, -0.1745F, 0.0F);
         this.chest_r1.cubeList.add(new ModelBox(this.chest_r1, 0, 48, -4.0F, -4.0F, -0.5F, 8, 8, 1, 0.3F, true));
         (this.chest_r2 = new ModelRenderer(this)).setRotationPoint(-5.15F, -6.0888F, -8.0189F);
         this.chest.addChild(this.chest_r2);
         this.setRotationAngle(this.chest_r2, -0.1309F, 0.1745F, 0.0F);
         this.chest_r2.cubeList.add(new ModelBox(this.chest_r2, 0, 48, -4.0F, -4.0F, -0.5F, 8, 8, 1, 0.3F, false));
         (this.chest_r3 = new ModelRenderer(this)).setRotationPoint(-1.0F, 13.0F, -6.4F);
         this.chest.addChild(this.chest_r3);
         this.setRotationAngle(this.chest_r3, -0.1309F, 0.0F, 0.0F);
         this.chest_r3.cubeList.add(new ModelBox(this.chest_r3, 0, 0, 0.0F, -24.0F, -4.0F, 8, 12, 8, 0.0F, true));
         this.chest_r3.cubeList.add(new ModelBox(this.chest_r3, 0, 0, -8.0F, -24.0F, -4.0F, 8, 12, 8, 0.0F, false));
         (this.hump = new ModelRenderer(this)).setRotationPoint(-1.0F, -6.7785F, -0.2804F);
         this.chest.addChild(this.hump);
         this.setRotationAngle(this.hump, -0.5236F, -0.6981F, 0.3491F);
         this.hump.cubeList.add(new ModelBox(this.hump, 0, 20, -3.9676F, 3.2835F, -4.2957F, 8, 6, 8, -0.5F, false));
         this.hump.cubeList.add(new ModelBox(this.hump, 0, 20, -4.3176F, -1.8165F, -4.6457F, 8, 6, 8, 0.0F, false));
         (this.hump_r1 = new ModelRenderer(this)).setRotationPoint(3.1824F, 8.8335F, 2.7543F);
         this.hump.addChild(this.hump_r1);
         this.setRotationAngle(this.hump_r1, -0.2499F, 0.061F, 0.3F);
         this.hump_r1.cubeList.add(new ModelBox(this.hump_r1, 0, 20, -7.25F, -0.75F, -7.25F, 8, 6, 8, -0.5F, false));
         (this.bone7 = new ModelRenderer(this)).setRotationPoint(1.8824F, -1.2165F, 1.5543F);
         this.hump.addChild(this.bone7);
         this.setRotationAngle(this.bone7, 0.5528F, 0.1534F, -0.5279F);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 0, 20, -8.0F, -6.0F, -8.0F, 8, 6, 8, 1.5F, false));
         (this.bipedRightArm = new ModelRenderer(this)).setRotationPoint(-7.0F, -17.5F, -5.0F);
         this.bipedBody.addChild(this.bipedRightArm);
         this.bipedRightArm.cubeList.add(new ModelBox(this.bipedRightArm, 0, 5, -1.0F, -1.0F, 0.0F, 1, 1, 1, 0.0F, false));
         (this.upperArmRight = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedRightArm.addChild(this.upperArmRight);
         this.setRotationAngle(this.upperArmRight, 0.0F, 0.0F, 0.3491F);
         this.upperArmRight.cubeList.add(new ModelBox(this.upperArmRight, 32, 0, -6.0F, -2.0F, -3.0F, 6, 12, 6, 0.0F, false));
         (this.foreArmRight = new ModelRenderer(this)).setRotationPoint(-3.0F, 8.0F, 2.0F);
         this.upperArmRight.addChild(this.foreArmRight);
         this.setRotationAngle(this.foreArmRight, -0.5236F, 0.0F, -0.5236F);
         this.foreArmRight.cubeList.add(new ModelBox(this.foreArmRight, 26, 28, -3.0F, 0.0F, -5.0F, 6, 14, 6, -0.25F, false));
         this.foreArmRight.cubeList.add(new ModelBox(this.foreArmRight, 0, 0, -2.75F, -3.0F, -1.0F, 2, 3, 2, 0.0F, false));
         this.foreArmRight.cubeList.add(new ModelBox(this.foreArmRight, 0, 0, -2.75F, -5.5F, -1.0F, 2, 3, 2, -0.3F, false));
         this.foreArmRight.cubeList.add(new ModelBox(this.foreArmRight, 0, 0, -2.75F, -7.5F, -1.0F, 2, 3, 2, -0.6F, false));
         (this.foreArmRight_r1 = new ModelRenderer(this)).setRotationPoint(9.337F, 8.1206F, 3.0F);
         this.foreArmRight.addChild(this.foreArmRight_r1);
         this.setRotationAngle(this.foreArmRight_r1, 0.0F, 0.0F, -0.3054F);
         this.foreArmRight_r1.cubeList.add(new ModelBox(this.foreArmRight_r1, 26, 28, -8.6774F, -0.1203F, -7.9728F, 2, 2, 6, -0.1F, false));
         (this.foreArmRight_r2 = new ModelRenderer(this)).setRotationPoint(10.0F, 10.0F, 3.0F);
         this.foreArmRight.addChild(this.foreArmRight_r2);
         this.setRotationAngle(this.foreArmRight_r2, 0.0F, 0.0F, -0.1309F);
         this.foreArmRight_r2.cubeList.add(new ModelBox(this.foreArmRight_r2, 26, 28, -12.6774F, 0.8797F, -7.9728F, 6, 2, 6, -0.1F, false));
         (this.foreArmRight_r3 = new ModelRenderer(this)).setRotationPoint(10.0F, 10.0F, 3.0F);
         this.foreArmRight.addChild(this.foreArmRight_r3);
         this.setRotationAngle(this.foreArmRight_r3, 0.0F, 0.2182F, 0.0F);
         this.foreArmRight_r3.cubeList.add(new ModelBox(this.foreArmRight_r3, 28, 40, -10.1038F, -0.3403F, -10.2795F, 6, 3, 4, -0.2F, false));
         (this.bipedLeftArm = new ModelRenderer(this)).setRotationPoint(7.0F, -17.5F, -5.0F);
         this.bipedBody.addChild(this.bipedLeftArm);
         this.bipedLeftArm.cubeList.add(new ModelBox(this.bipedLeftArm, 0, 5, 0.0F, -1.0F, 0.0F, 1, 1, 1, 0.0F, true));
         (this.upperArmLeft = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedLeftArm.addChild(this.upperArmLeft);
         this.setRotationAngle(this.upperArmLeft, 0.0F, 0.0F, -0.3491F);
         this.upperArmLeft.cubeList.add(new ModelBox(this.upperArmLeft, 32, 0, 0.0F, -2.0F, -3.0F, 6, 12, 6, 0.0F, true));
         (this.foreArmLeft = new ModelRenderer(this)).setRotationPoint(3.0F, 8.0F, 2.0F);
         this.upperArmLeft.addChild(this.foreArmLeft);
         this.setRotationAngle(this.foreArmLeft, -0.5236F, 0.0F, 0.5236F);
         this.foreArmLeft.cubeList.add(new ModelBox(this.foreArmLeft, 26, 28, -3.0F, 0.0F, -5.0F, 6, 14, 6, -0.25F, true));
         this.foreArmLeft.cubeList.add(new ModelBox(this.foreArmLeft, 0, 0, 0.75F, -3.0F, -1.0F, 2, 3, 2, 0.0F, true));
         this.foreArmLeft.cubeList.add(new ModelBox(this.foreArmLeft, 0, 0, 0.75F, -5.5F, -1.0F, 2, 3, 2, -0.3F, true));
         this.foreArmLeft.cubeList.add(new ModelBox(this.foreArmLeft, 0, 0, 0.75F, -7.5F, -1.0F, 2, 3, 2, -0.6F, true));
         (this.foreArmRight_r4 = new ModelRenderer(this)).setRotationPoint(-9.337F, 8.1206F, 3.0F);
         this.foreArmLeft.addChild(this.foreArmRight_r4);
         this.setRotationAngle(this.foreArmRight_r4, 0.0F, 0.0F, 0.3054F);
         this.foreArmRight_r4.cubeList.add(new ModelBox(this.foreArmRight_r4, 26, 28, 6.6774F, -0.1203F, -7.9728F, 2, 2, 6, -0.1F, true));
         (this.foreArmRight_r5 = new ModelRenderer(this)).setRotationPoint(-10.0F, 10.0F, 3.0F);
         this.foreArmLeft.addChild(this.foreArmRight_r5);
         this.setRotationAngle(this.foreArmRight_r5, 0.0F, -0.2182F, 0.0F);
         this.foreArmRight_r5.cubeList.add(new ModelBox(this.foreArmRight_r5, 28, 40, 4.1038F, -0.3403F, -10.2795F, 6, 3, 4, -0.2F, true));
         (this.foreArmRight_r6 = new ModelRenderer(this)).setRotationPoint(-10.0F, 10.0F, 3.0F);
         this.foreArmLeft.addChild(this.foreArmRight_r6);
         this.setRotationAngle(this.foreArmRight_r6, 0.0F, 0.0F, 0.1309F);
         this.foreArmRight_r6.cubeList.add(new ModelBox(this.foreArmRight_r6, 26, 28, 6.6774F, 0.8797F, -7.9728F, 6, 2, 6, -0.1F, true));
         float[][] tailBaseData = new float[][]{{4.0F, 23.5F, 0.0F, -1.4835F, 1.8326F, 0.0F}, {3.0F, 23.5F, 0.0F, -1.4835F, 1.309F, 0.0F}, {2.0F, 23.5F, 0.0F, -1.4835F, 0.7854F, 0.0F}, {1.0F, 23.5F, 0.0F, -1.4835F, 0.2618F, 0.0F}, {-1.0F, 23.5F, 0.0F, -1.4835F, -0.2618F, 0.0F}, {-2.0F, 23.5F, 0.0F, -1.4835F, -0.7854F, 0.0F}, {-3.0F, 23.5F, 0.0F, -1.4835F, -1.309F, 0.0F}, {-4.0F, 23.5F, 0.0F, -1.4835F, -1.8326F, 0.0F}};

         for(int t = 0; t < 8; ++t) {
            (this.tail[t][0] = new ModelRenderer(this)).setRotationPoint(tailBaseData[t][0], tailBaseData[t][1], tailBaseData[t][2]);
            this.setRotationAngle(this.tail[t][0], tailBaseData[t][3], tailBaseData[t][4], tailBaseData[t][5]);
            this.tail[t][0].cubeList.add(new ModelBox(this.tail[t][0], 44, 18, -2.0F, -7.5F, -2.0F, 4, 8, 4, 1.0F, false));
            float[] inflations = new float[]{0.9F, 0.8F, 0.7F, 0.6F, 0.5F, 0.0F, -0.4F};

            for(int s = 1; s <= 7; ++s) {
               (this.tail[t][s] = new ModelRenderer(this)).setRotationPoint(0.0F, -7.0F, 0.0F);
               this.tail[t][s - 1].addChild(this.tail[t][s]);
               this.setRotationAngle(this.tail[t][s], 0.2618F, 0.0F, 0.0F);
               this.tail[t][s].cubeList.add(new ModelBox(this.tail[t][s], 44, 18, -2.0F, -7.5F, -2.0F, 4, 8, 4, inflations[s - 1], false));
            }
         }

         for(int i = 0; i < 8; ++i) {
            for(int j = 1; j < 8; ++j) {
               this.tailSwayX[i][j] = (this.rand.nextFloat() * 0.1309F + 0.1309F) * (float)Math.sqrt((double)j) * (this.rand.nextBoolean() ? -1.0F : 1.0F);
               this.tailSwayZ[i][j] = (this.rand.nextFloat() * 0.1309F + 0.1309F) * (float)Math.sqrt((double)j) * (this.rand.nextBoolean() ? -1.0F : 1.0F);
               this.tailSwayY[i][j] = this.rand.nextFloat() * 0.1745F + 0.1745F;
            }
         }

      }

      public void render(Entity entity, float f0, float f1, float f2, float f3, float f4, float f5) {
         this.bipedRightLeg.isHidden = true;
         this.bipedLeftLeg.isHidden = true;
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, -13.5F * f5, 0.0F);
         GlStateManager.translate(0.0F, 0.0F, 3.75F * f5);
         GlStateManager.scale(10.0F, 10.0F, 10.0F);
         this.bipedBody.render(f5);

         for(int i = 0; i < 8; ++i) {
            this.tail[i][0].render(f5);
         }

         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.disableLighting();
         this.bipedHeadwear.render(f5);
         GlStateManager.enableLighting();
         GlStateManager.popMatrix();
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void setRotationAngles(float f0, float f1, float f2, float f3, float f4, float f5, Entity e) {
         super.setRotationAngles(f0 * 2.0F / e.height, f1, f2, f3, f4, f5, e);
         ModelRenderer var10000 = this.bipedHead;
         var10000.rotationPointY -= 19.5F;
         var10000 = this.bipedRightArm;
         var10000.rotationPointZ -= 5.0F;
         var10000 = this.bipedRightArm;
         var10000.rotationPointX -= 2.0F;
         var10000 = this.bipedLeftArm;
         var10000.rotationPointZ -= 5.0F;
         var10000 = this.bipedLeftArm;
         var10000.rotationPointX += 2.0F;

         for(int i = 0; i < 8; ++i) {
            for(int j = 1; j < 8; ++j) {
               this.tail[i][j].rotateAngleX = 0.2618F + MathHelper.sin((f2 - (float)j) * 0.08F) * this.tailSwayX[i][j];
               this.tail[i][j].rotateAngleZ = MathHelper.cos((f2 - (float)j) * 0.08F) * this.tailSwayZ[i][j];
               this.tail[i][j].rotateAngleY = MathHelper.sin((f2 - (float)j) * 0.08F) * this.tailSwayY[i][j];
            }
         }

         if (e instanceof EntityCustom && ((EntityCustom)e).isShooting()) {
            var10000 = this.bipedHead;
            var10000.rotateAngleX -= 0.5236F;
            var10000 = this.bipedHeadwear;
            var10000.rotateAngleX -= 0.5236F;
            this.jaw.rotateAngleX = 1.0472F;
         } else {
            this.jaw.rotateAngleX = 0.0F;
         }

         this.bipedBody.rotationPointY = 0.0F;
         this.bipedBody.rotateAngleX = 0.0F;

         for(int i = 0; i < 8; ++i) {
            this.tail[i][0].rotationPointY = 0.0F;
         }

         copyModelAngles(this.bipedBody, this.bipedHeadwear);
         copyModelAngles(this.bipedHead, this.eyes);
      }
   }

   public static class EntityCustom extends EntityCreature {
      private static final DataParameter<Boolean> SHOOTING;
      private static final float PHASE_2_THRESHOLD = 0.6F;
      private static final float PHASE_3_THRESHOLD = 0.3F;
      private int currentPhase = 1;
      private boolean phase2Announced = false;
      private boolean phase3Announced = false;
      private int meleeCooldown = 0;
      private int hornChargeCooldown = 0;
      private int tentacleSweepCooldown = 0;
      private int inkBlindCooldown = 0;
      private int tentacleGrabCooldown = 0;
      private int lariatCooldown = 0;
      private int bijuBombBarrageCooldown = 0;
      private int tentacleSlamLeapCooldown = 0;
      private int lightningFangCooldown = 0;
      private int inkSprayCooldown = 0;
      private int tentacleVolleyCooldown = 0;
      private int globalCooldown = 0;
      private int antiYCheeseCooldown = 0;
      private int bijuBombWindup = -1;
      private int bijuBombBarrageCount = 0;
      private int bijuBombBarrageDelay = 0;
      private double bijuBombTX;
      private double bijuBombTY;
      private double bijuBombTZ;
      private EntityBijuuBombProjectile.EntityCustom activeBijuuBomb;
      private boolean isCharging = false;
      private int chargeTicks = 0;
      private double chargeVelX;
      private double chargeVelZ;
      private EntityPlayer grabbedPlayer = null;
      private int grabTicks = 0;
      private boolean isLariating = false;
      private int lariatTicks = 0;
      private double lariatVelX;
      private double lariatVelZ;
      private EntityLivingBase lariatTarget = null;
      private boolean isTentacleLeaping = false;
      private int tentacleLeapTicks = 0;
      private double tentacleLeapTargetX;
      private double tentacleLeapTargetZ;
      private static final int MAX_TENTACLE_PROJECTILES = 3;
      private final double[] tpX = new double[3];
      private final double[] tpY = new double[3];
      private final double[] tpZ = new double[3];
      private final double[] tpVX = new double[3];
      private final double[] tpVY = new double[3];
      private final double[] tpVZ = new double[3];
      private final int[] tpTicks = new int[3];
      private final boolean[] tpActive = new boolean[3];
      private int targetSwitchTimer = 0;
      private int despawnTimer = 36000;
      private double spawnAnchorX;
      private double spawnAnchorY;
      private double spawnAnchorZ;
      private boolean hasSpawnAnchor = false;
      private static final double MAX_DRIFT_DISTANCE = (double)200.0F;
      private BossInfoServer bossInfo;
      private static final int HORN_CHARGE_CD = 100;
      private static final int TENTACLE_SWEEP_CD = 80;
      private static final int INK_BLIND_CD = 200;
      private static final int TENTACLE_GRAB_CD = 160;
      private static final int LARIAT_CD = 140;
      private static final int BIJU_BOMB_BARRAGE_CD = 500;
      private static final int BIJU_BOMB_WINDUP_TICKS = 50;
      private static final int TENTACLE_SLAM_LEAP_CD = 200;
      private static final int LIGHTNING_FANG_CD = 160;
      private static final int INK_SPRAY_CD = 240;
      private static final int TENTACLE_VOLLEY_CD = 200;
      private static final float HORN_CHARGE_DAMAGE = 54.4F;
      private static final float TENTACLE_SWEEP_DAMAGE = 48.8F;
      private static final float INK_BLIND_DAMAGE = 16.0F;
      private static final float TENTACLE_GRAB_DPS = 12.8F;
      private static final float LARIAT_DAMAGE = 76.0F;
      private static final float BIJU_BOMB_DAMAGE = 97.6F;
      private static final float BIJU_BOMB_RADIUS = 12.0F;
      private static final float TENTACLE_SLAM_LEAP_DAMAGE = 87.2F;
      private static final float TENTACLE_SLAM_LEAP_RADIUS = 14.0F;
      private static final float LIGHTNING_FANG_DAMAGE = 43.2F;
      private static final float INK_SPRAY_DAMAGE = 33.6F;
      private static final float TENTACLE_VOLLEY_DAMAGE = 30.4F;
      private static final float TRUE_DAMAGE_SPLIT = 0.8F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(6.0F, 17.0F);
         this.experienceValue = 1200;
         this.isImmuneToFire = true;
         this.setNoAI(true);
         this.enablePersistence();
         this.stepHeight = 3.0F;
         this.maxHurtResistantTime = 7;
         if (!world.isRemote) {
            this.bossInfo = new BossInfoServer(new TextComponentString("§8§lGyuki - The Eight-Tails"), Color.WHITE, Overlay.PROGRESS);
         }

      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(SHOOTING, false);
      }

      protected void initEntityAI() {
         this.tasks.addTask(0, new EntityAISwimming(this));
      }

      public void setAttackTarget(@Nullable EntityLivingBase target) {
         if (target != null && !(target instanceof EntityPlayer)) {
            String cn = target.getClass().getName().toLowerCase();
            if (!cn.contains("icedome") && !cn.contains("shieldbase")) {
               return;
            }
         }

         super.setAttackTarget(target);
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)175000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.42);
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)30.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)150.0F);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)96.0F);
      }

      public EnumCreatureAttribute getCreatureAttribute() {
         return EnumCreatureAttribute.UNDEFINED;
      }

      protected boolean canDespawn() {
         return false;
      }

      public void fall(float distance, float damageMultiplier) {
      }

      public boolean isShooting() {
         return (Boolean)this.dataManager.get(SHOOTING);
      }

      public void setShooting(boolean shooting) {
         this.dataManager.set(SHOOTING, shooting);
      }

      public SoundEvent getAmbientSound() {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation(this.rand.nextFloat() < 0.2F ? "narutomod:gyuki_roar" : "narutomod:gyuki_snort"));
      }

      public SoundEvent getHurtSound(DamageSource ds) {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.hurt"));
      }

      public SoundEvent getDeathSound() {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:gyuki_roar"));
      }

      protected float getSoundVolume() {
         return 3.0F;
      }

      public void addTrackingPlayer(EntityPlayerMP player) {
         super.addTrackingPlayer(player);
         if (this.bossInfo != null && !this.getEntityData().getBoolean("inftsuk_contract_spawn")) {
            this.bossInfo.addPlayer(player);
         }

      }

      public void removeTrackingPlayer(EntityPlayerMP player) {
         super.removeTrackingPlayer(player);
         if (this.bossInfo != null) {
            this.bossInfo.removePlayer(player);
         }

      }

      private double getEdgeDistance(Entity target) {
         double centerDist = (double)this.getDistance(target);
         double halfWidth = (double)this.width * (double)0.5F;
         return Math.max((double)0.0F, centerDist - halfWidth);
      }

      public boolean attackEntityAsMob(Entity target) {
         if (this.meleeCooldown > 0) {
            return false;
         } else {
            float damage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            float phaseMul = this.getPhaseMultiplier();
            float normalDmg = damage * 0.19999999F * phaseMul;
            float trueDmg = damage * 0.8F * phaseMul;
            boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
            if (hit && target instanceof EntityLivingBase) {
               ((EntityLivingBase)target).hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            }

            if (hit) {
               this.meleeCooldown = 15;
            }

            return hit;
         }
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         return source != DamageSource.IN_FIRE && source != DamageSource.ON_FIRE && source != DamageSource.LAVA && source != DamageSource.DROWN && source != DamageSource.FALL ? super.attackEntityFrom(source, amount) : false;
      }

      public void onLivingUpdate() {
         super.onLivingUpdate();
         if (!this.world.isRemote) {
            --this.despawnTimer;
            if (this.despawnTimer <= 0) {
               this.spawnDeathParticles();
               this.setDead();
            } else {
               if (this.bossInfo != null) {
                  this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
               }

               if (this.ticksExisted % 10 == 0) {
                  EntityLivingBase dome = this.findNearbyIceDome((double)20.0F);
                  if (dome != null && dome.isEntityAlive()) {
                     this.setAttackTarget(dome);
                     if (this.getEdgeDistance(dome) <= (double)4.0F) {
                        dome.hurtResistantTime = 0;
                        dome.attackEntityFrom(DamageSource.causeMobDamage(this), 240.0F);
                     }
                  }
               }

               EntityLivingBase currentTarget = this.getAttackTarget();
               if (currentTarget == null || !currentTarget.isEntityAlive()) {
                  List<EntityPlayer> nearby = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)96.0F), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator());
                  if (!nearby.isEmpty()) {
                     EntityPlayer nearest = null;
                     double nearestDist = Double.MAX_VALUE;

                     for(EntityPlayer p : nearby) {
                        double d = this.getDistanceSq(p);
                        if (d < nearestDist) {
                           nearestDist = d;
                           nearest = p;
                        }
                     }

                     if (nearest != null) {
                        this.setAttackTarget(nearest);
                     }
                  }
               }

               if (this.getRevengeTarget() != null && this.getRevengeTarget().isEntityAlive() && this.getRevengeTarget() instanceof EntityPlayer && this.getRevengeTarget() != this.getAttackTarget()) {
                  this.setAttackTarget(this.getRevengeTarget());
               }

               if (this.meleeCooldown > 0) {
                  --this.meleeCooldown;
               }

               if (this.hornChargeCooldown > 0) {
                  --this.hornChargeCooldown;
               }

               if (this.tentacleSweepCooldown > 0) {
                  --this.tentacleSweepCooldown;
               }

               if (this.inkBlindCooldown > 0) {
                  --this.inkBlindCooldown;
               }

               if (this.tentacleGrabCooldown > 0) {
                  --this.tentacleGrabCooldown;
               }

               if (this.lariatCooldown > 0) {
                  --this.lariatCooldown;
               }

               if (this.bijuBombBarrageCooldown > 0) {
                  --this.bijuBombBarrageCooldown;
               }

               if (this.tentacleSlamLeapCooldown > 0) {
                  --this.tentacleSlamLeapCooldown;
               }

               if (this.lightningFangCooldown > 0) {
                  --this.lightningFangCooldown;
               }

               if (this.inkSprayCooldown > 0) {
                  --this.inkSprayCooldown;
               }

               if (this.tentacleVolleyCooldown > 0) {
                  --this.tentacleVolleyCooldown;
               }

               if (this.globalCooldown > 0) {
                  --this.globalCooldown;
               }

               if (this.antiYCheeseCooldown > 0) {
                  --this.antiYCheeseCooldown;
               }

               for(int i = 0; i < 3; ++i) {
                  if (this.tpActive[i]) {
                     this.processTentacleProjectile(i);
                  }
               }

               if (++this.targetSwitchTimer >= 100 + this.rand.nextInt(60)) {
                  this.targetSwitchTimer = 0;
                  this.switchTarget();
               }

               this.updatePhase();
               if (this.isCharging) {
                  this.processHornCharge();
               } else if (this.isTentacleLeaping) {
                  this.processTentacleSlamLeap();
               } else if (this.isLariating) {
                  this.processLariat();
               } else if (this.grabbedPlayer != null && this.grabTicks > 0) {
                  this.processTentacleGrab();
               } else if (this.bijuBombWindup > 0) {
                  --this.bijuBombWindup;
                  this.tickBijuBombWindup();
                  if (this.bijuBombWindup == 0) {
                     this.executeBijuBomb();
                  }

               } else if (this.bijuBombBarrageCount > 0 && this.bijuBombBarrageDelay > 0) {
                  --this.bijuBombBarrageDelay;
                  if (this.bijuBombBarrageDelay <= 0) {
                     EntityLivingBase target = this.getAttackTarget();
                     if (target != null && target.isEntityAlive()) {
                        this.bijuBombTX = target.posX;
                        this.bijuBombTY = target.posY;
                        this.bijuBombTZ = target.posZ;
                     }

                     this.startBijuBombSubShot();
                  }

               } else {
                  if (!this.hasSpawnAnchor && this.posX != (double)0.0F && this.posZ != (double)0.0F) {
                     this.spawnAnchorX = this.posX;
                     this.spawnAnchorY = this.posY;
                     this.spawnAnchorZ = this.posZ;
                     this.hasSpawnAnchor = true;
                  }

                  if (this.ticksExisted % 100 == 0 && this.hasSpawnAnchor) {
                     if (this.posX == (double)0.0F && this.posZ == (double)0.0F && this.posY < (double)10.0F) {
                        System.out.println("[WorldBossGyuki] Boss at 0,0 — position corrupted, removing.");
                        this.spawnDeathParticles();
                        this.setDead();
                        return;
                     }

                     double driftDist = Math.sqrt(Math.pow(this.posX - this.spawnAnchorX, (double)2.0F) + Math.pow(this.posZ - this.spawnAnchorZ, (double)2.0F));
                     if (driftDist > (double)200.0F) {
                        System.out.println("[WorldBossGyuki] Boss drifted " + (int)driftDist + " blocks from spawn, returning.");
                        this.setPositionAndUpdate(this.spawnAnchorX, this.spawnAnchorY, this.spawnAnchorZ);
                        this.motionX = (double)0.0F;
                        this.motionY = (double)0.0F;
                        this.motionZ = (double)0.0F;
                     }

                     if (!this.isTentacleLeaping && !this.isCharging && !this.isLariating) {
                        BlockPos groundPos = new BlockPos(this.posX, this.posY, this.posZ);
                        int groundY = this.world.getHeight(groundPos).getY();
                        if (groundY > 1) {
                           double heightAboveGround = this.posY - (double)groundY;
                           if (heightAboveGround > (double)5.0F) {
                              this.setPositionAndUpdate(this.posX, (double)groundY, this.posZ);
                              this.motionY = (double)0.0F;
                           }
                        }
                     }
                  }

                  EntityLivingBase target = this.getAttackTarget();
                  if (target != null && target.isEntityAlive()) {
                     double edgeDist = this.getEdgeDistance(target);
                     double yDiff = target.posY - this.posY;
                     double horizDist = Math.sqrt(Math.pow(target.posX - this.posX, (double)2.0F) + Math.pow(target.posZ - this.posZ, (double)2.0F));
                     if (horizDist > (double)100.0F) {
                        this.setAttackTarget((EntityLivingBase)null);
                     } else if (yDiff > (double)10.0F && horizDist < (double)60.0F && this.antiYCheeseCooldown <= 0) {
                        double safeY = this.findSafeY(target.posX, target.posY, target.posZ);
                        this.setPositionAndUpdate(target.posX, safeY, target.posZ);
                        this.motionX = (double)0.0F;
                        this.motionY = (double)0.0F;
                        this.motionZ = (double)0.0F;
                        this.velocityChanged = true;
                        this.antiYCheeseCooldown = 60;
                     } else {
                        if (edgeDist > (double)4.0F && !this.isCharging && !this.isLariating && !this.isTentacleLeaping) {
                           double dx = target.posX - this.posX;
                           double dz = target.posZ - this.posZ;
                           double len = Math.sqrt(dx * dx + dz * dz);
                           if (len > 0.01) {
                              double speed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
                              if (this.currentPhase >= 3) {
                                 speed *= 1.8;
                              }

                              this.motionX = dx / len * speed;
                              this.motionZ = dz / len * speed;
                              if (this.collidedHorizontally && this.onGround) {
                                 this.motionY = 0.42;
                              }
                           }
                        } else if (edgeDist <= (double)4.0F) {
                           this.motionX *= 0.3;
                           this.motionZ *= 0.3;
                        }

                        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                        this.velocityChanged = true;
                        double faceDx = target.posX - this.posX;
                        double faceDz = target.posZ - this.posZ;
                        float targetYaw = (float)(Math.atan2(-faceDx, faceDz) * (180D / Math.PI));
                        this.rotationYaw = targetYaw;
                        this.renderYawOffset = targetYaw;
                        this.rotationYawHead = targetYaw;
                        if (this.globalCooldown <= 0) {
                           this.executeAttacks(target, edgeDist);
                        }

                     }
                  }
               }
            }
         }
      }

      private void updatePhase() {
         float hpRatio = this.getHealth() / this.getMaxHealth();
         if (hpRatio <= 0.3F && this.currentPhase < 3) {
            this.currentPhase = 3;
            if (!this.phase3Announced) {
               this.phase3Announced = true;
               this.announcePhase("§8§lGyuki enters RAMPAGE mode!", "§7The Eight-Tails unleashes its full power!");
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)8.0F, this.posZ, 10, (double)5.0F, (double)5.0F, (double)5.0F, (double)0.0F, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)5.0F, this.posZ, 100, (double)6.0F, (double)4.0F, (double)6.0F, 0.15, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)3.0F, this.posZ, 80, (double)5.0F, (double)3.0F, (double)5.0F, 0.1, new int[0]);
               }
            }
         } else if (hpRatio <= 0.6F && this.currentPhase < 2) {
            this.currentPhase = 2;
            if (!this.phase2Announced) {
               this.phase2Announced = true;
               this.announcePhase("§8§lGyuki awakens its Ink Release!", "§7Dark ink surges from the Eight-Tails!");
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)6.0F, this.posZ, 60, (double)4.0F, (double)3.0F, (double)4.0F, 0.1, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)4.0F, this.posZ, 40, (double)3.0F, (double)2.0F, (double)3.0F, 0.05, new int[0]);
               }
            }
         }

      }

      private float getPhaseMultiplier() {
         if (this.currentPhase >= 3) {
            return 2.2F;
         } else {
            return this.currentPhase >= 2 ? 1.5F : 1.0F;
         }
      }

      private float getPhaseCdMultiplier() {
         if (this.currentPhase >= 3) {
            return 0.5F;
         } else {
            return this.currentPhase >= 2 ? 0.7F : 1.0F;
         }
      }

      private void announcePhase(String title, String subtitle) {
         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)96.0F))) {
            if (p instanceof EntityPlayerMP) {
               EntityPlayerMP mp = (EntityPlayerMP)p;
               SPacketTitle titlePacket = new SPacketTitle(Type.TITLE, new TextComponentString(title));
               SPacketTitle subtitlePacket = new SPacketTitle(Type.SUBTITLE, new TextComponentString(subtitle));
               SPacketTitle timesPacket = new SPacketTitle(Type.TIMES, (ITextComponent)null, 10, 40, 20);
               mp.connection.sendPacket(timesPacket);
               mp.connection.sendPacket(subtitlePacket);
               mp.connection.sendPacket(titlePacket);
            }
         }

      }

      private void executeAttacks(EntityLivingBase target, double dist) {
         float cdMul = this.getPhaseCdMultiplier();
         if (this.currentPhase >= 3 && this.bijuBombBarrageCooldown <= 0 && dist > (double)8.0F && dist < (double)50.0F) {
            this.startBijuBombBarrage(target);
         } else if (this.currentPhase >= 3 && this.tentacleSlamLeapCooldown <= 0 && dist > (double)8.0F && dist < (double)30.0F) {
            this.doTentacleSlamLeap(target);
         } else if (this.currentPhase >= 3 && this.lariatCooldown <= 0 && dist > (double)6.0F && dist < (double)30.0F) {
            this.doLariat(target);
         } else if (this.currentPhase >= 2 && this.inkBlindCooldown <= 0 && dist < (double)15.0F) {
            this.doInkBlind();
         } else if (this.currentPhase >= 2 && this.tentacleGrabCooldown <= 0 && dist < (double)10.0F && target instanceof EntityPlayer) {
            this.doTentacleGrab((EntityPlayer)target);
         } else if (this.currentPhase >= 2 && this.inkSprayCooldown <= 0 && dist > (double)6.0F && dist < (double)30.0F) {
            this.doInkSpray(target);
         } else if (this.lightningFangCooldown <= 0 && dist > (double)10.0F && dist < (double)40.0F) {
            this.doLightningFang(target);
         } else if (this.tentacleVolleyCooldown <= 0 && dist > (double)8.0F && dist < (double)45.0F && !this.tpActive[0] && !this.tpActive[1] && !this.tpActive[2]) {
            this.doTentacleVolley(target);
         } else if (this.hornChargeCooldown <= 0 && dist > (double)8.0F && dist < (double)35.0F) {
            this.doHornCharge(target);
         } else if (this.tentacleSweepCooldown <= 0 && dist < (double)12.0F) {
            this.doTentacleSweep();
         } else {
            if (this.meleeCooldown <= 0 && dist <= (double)6.0F) {
               this.attackEntityAsMob(target);
            }

         }
      }

      private void doHornCharge(EntityLivingBase target) {
         this.hornChargeCooldown = (int)(100.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 10;
         this.isCharging = true;
         this.chargeTicks = 20;
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double len = Math.sqrt(dx * dx + dz * dz);
         double speed = this.currentPhase >= 3 ? (double)2.5F : (double)2.0F;
         this.chargeVelX = dx / len * speed;
         this.chargeVelZ = dz / len * speed;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 3.0F, 0.5F);
      }

      private void processHornCharge() {
         --this.chargeTicks;
         if (this.chargeTicks <= 0) {
            this.isCharging = false;
         } else {
            this.motionX = this.chargeVelX;
            this.motionZ = this.chargeVelZ;
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.velocityChanged = true;
            float phaseMul = this.getPhaseMultiplier();
            float dmg = 54.4F * phaseMul;
            float normalDmg = dmg * 0.19999999F;
            float trueDmg = dmg * 0.8F;

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)3.0F), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
               p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
               p.hurtResistantTime = 0;
               p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
               p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 3, false, true));
               p.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 60, 0, false, true));
               double dx = p.posX - this.posX;
               double dz = p.posZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > 0.01) {
                  p.motionX += dx / len * (double)2.5F;
                  p.motionY += 0.8;
                  p.motionZ += dz / len * (double)2.5F;
                  p.velocityChanged = true;
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)3.0F, this.posZ, 15, (double)2.0F, (double)1.0F, (double)2.0F, 0.05, new int[0]);
            }

         }
      }

      private void doTentacleSweep() {
         this.tentacleSweepCooldown = (int)(80.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 15;
         float radius = 12.0F;
         float dmg = 48.8F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.19999999F;
         float trueDmg = dmg * 0.8F;

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)radius), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
            double dx = p.posX - this.posX;
            double dz = p.posZ - this.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (!(d > (double)radius)) {
               p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
               p.hurtResistantTime = 0;
               p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
               if (d > 0.01) {
                  p.motionX += dx / d * (double)2.0F;
                  p.motionY += 0.6;
                  p.motionZ += dz / d * (double)2.0F;
                  p.velocityChanged = true;
               }
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.posX, this.posY + (double)4.0F, this.posZ, 30, (double)8.0F, (double)1.0F, (double)8.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)3.0F, this.posZ, 40, (double)6.0F, (double)1.5F, (double)6.0F, 0.05, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 3.0F, 0.3F);
      }

      private void doInkBlind() {
         this.inkBlindCooldown = (int)(200.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 20;
         float radius = 15.0F;
         float dmg = 16.0F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.19999999F;
         float trueDmg = dmg * 0.8F;

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)radius), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
            double d = (double)this.getDistance(p);
            if (!(d > (double)radius)) {
               p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
               p.hurtResistantTime = 0;
               p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
               p.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 2, false, true));
               p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1, false, true));
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)5.0F, this.posZ, 80, (double)radius * (double)0.5F, (double)3.0F, (double)radius * (double)0.5F, 0.15, new int[0]);
            ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)3.0F, this.posZ, 60, (double)radius * 0.4, (double)2.0F, (double)radius * 0.4, (double)0.5F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)2.0F, this.posZ, 100, (double)radius * (double)0.5F, (double)2.0F, (double)radius * (double)0.5F, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 5.0F, 0.3F);
      }

      private void doTentacleGrab(EntityPlayer target) {
         this.tentacleGrabCooldown = (int)(160.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 60;
         this.grabbedPlayer = target;
         this.grabTicks = 60;
         double dx = this.posX - target.posX;
         double dz = this.posZ - target.posZ;
         double len = Math.sqrt(dx * dx + dz * dz);
         if (len > 0.01) {
            target.motionX = dx / len * (double)1.5F;
            target.motionY = 0.3;
            target.motionZ = dz / len * (double)1.5F;
            target.velocityChanged = true;
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.HOSTILE, 3.0F, 0.5F);
      }

      private void processTentacleGrab() {
         --this.grabTicks;
         if (this.grabTicks > 0 && this.grabbedPlayer != null && this.grabbedPlayer.isEntityAlive() && !this.grabbedPlayer.isCreative() && !this.grabbedPlayer.isSpectator()) {
            double dx = this.posX - this.grabbedPlayer.posX;
            double dz = this.posZ - this.grabbedPlayer.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > (double)5.0F) {
               this.grabbedPlayer.motionX = dx / dist * (double)0.5F;
               this.grabbedPlayer.motionZ = dz / dist * (double)0.5F;
            } else {
               this.grabbedPlayer.motionX = (double)0.0F;
               this.grabbedPlayer.motionZ = (double)0.0F;
            }

            this.grabbedPlayer.motionY = Math.max(this.grabbedPlayer.motionY, -0.08);
            this.grabbedPlayer.velocityChanged = true;
            if (this.grabTicks % 10 == 0) {
               float dmg = 12.8F * this.getPhaseMultiplier();
               float normalDmg = dmg * 0.19999999F;
               float trueDmg = dmg * 0.8F;
               this.grabbedPlayer.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
               this.grabbedPlayer.hurtResistantTime = 0;
               this.grabbedPlayer.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            }

            if (this.grabTicks % 5 == 0 && this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.PORTAL, this.grabbedPlayer.posX, this.grabbedPlayer.posY + (double)1.0F, this.grabbedPlayer.posZ, 5, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
            }

         } else {
            this.grabbedPlayer = null;
            this.grabTicks = 0;
         }
      }

      private void doLariat(EntityLivingBase target) {
         this.lariatCooldown = (int)(140.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 10;
         this.isLariating = true;
         this.lariatTicks = 15;
         this.lariatTarget = target;
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double len = Math.sqrt(dx * dx + dz * dz);
         double speed = (double)3.0F;
         this.lariatVelX = dx / len * speed;
         this.lariatVelZ = dz / len * speed;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_IRONGOLEM_ATTACK, SoundCategory.HOSTILE, 5.0F, 0.3F);
      }

      private void processLariat() {
         --this.lariatTicks;
         if (this.lariatTicks <= 0) {
            this.isLariating = false;
            this.lariatTarget = null;
         } else {
            this.motionX = this.lariatVelX;
            this.motionZ = this.lariatVelZ;
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.velocityChanged = true;
            if (this.lariatTarget != null && this.lariatTarget.isEntityAlive() && this.getEdgeDistance(this.lariatTarget) <= (double)3.0F) {
               float dmg = 76.0F * this.getPhaseMultiplier();
               float normalDmg = dmg * 0.19999999F;
               float trueDmg = dmg * 0.8F;
               this.lariatTarget.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
               if (this.lariatTarget instanceof EntityLivingBase) {
                  this.lariatTarget.hurtResistantTime = 0;
               }

               this.lariatTarget.attackEntityFrom(DamageSource.MAGIC, trueDmg);
               double dx = this.lariatTarget.posX - this.posX;
               double dz = this.lariatTarget.posZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > 0.01) {
                  EntityLivingBase var10000 = this.lariatTarget;
                  var10000.motionX += dx / len * (double)4.0F;
                  ++this.lariatTarget.motionY;
                  var10000 = this.lariatTarget;
                  var10000.motionZ += dz / len * (double)4.0F;
                  if (this.lariatTarget instanceof EntityPlayer) {
                     ((EntityPlayer)this.lariatTarget).velocityChanged = true;
                  }
               }

               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.lariatTarget.posX, this.lariatTarget.posY + (double)1.0F, this.lariatTarget.posZ, 5, (double)1.0F, (double)1.0F, (double)1.0F, (double)0.0F, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.lariatTarget.posX, this.lariatTarget.posY, this.lariatTarget.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 5.0F, 0.5F);
               this.isLariating = false;
               this.lariatTarget = null;
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)5.0F, this.posZ, 20, (double)2.0F, (double)2.0F, (double)2.0F, 0.1, new int[0]);
            }

         }
      }

      private void startBijuBombBarrage(EntityLivingBase target) {
         this.bijuBombBarrageCooldown = (int)(500.0F * this.getPhaseCdMultiplier());
         this.bijuBombWindup = 50;
         this.bijuBombTX = target.posX;
         this.bijuBombTY = target.posY;
         this.bijuBombTZ = target.posZ;
         this.bijuBombBarrageCount = 3;
         this.globalCooldown = 110;
         this.setShooting(true);
         this.spawnChargingBomb();

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F))) {
            p.sendMessage(new TextComponentString("§8§lGyuki is charging a Bijuu Bomb Barrage! Spread out!"));
         }

      }

      private void spawnChargingBomb() {
         float phaseMul = this.getPhaseMultiplier();
         float normalDmg = 97.6F * phaseMul * 0.19999999F;
         float trueDmg = 97.6F * phaseMul * 0.8F;
         this.activeBijuuBomb = EntityBijuuBombProjectile.EntityCustom.spawnCharging(this.world, this, 4.0F, normalDmg, trueDmg, 50, (double)12.0F, 0.55F);
      }

      private void startBijuBombSubShot() {
         this.bijuBombWindup = Math.max(20, 25);
         this.setShooting(true);
         this.spawnChargingBomb();
      }

      private void tickBijuBombWindup() {
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            BlockPos windupGroundPos = new BlockPos(this.posX, this.posY, this.posZ);
            int windupGroundY = this.world.getHeight(windupGroundPos).getY();
            if (windupGroundY > 1 && this.posY < (double)windupGroundY) {
               this.setPositionAndUpdate(this.posX, (double)windupGroundY, this.posZ);
            }

            this.motionX = (double)0.0F;
            this.motionY = (double)0.0F;
            this.motionZ = (double)0.0F;
            float progress = 1.0F - (float)this.bijuBombWindup / 50.0F;
            double markerRadius = (double)(12.0F * progress);
            int markerCount = (int)(10.0F + progress * 40.0F);
            ws.spawnParticle(EnumParticleTypes.FLAME, this.bijuBombTX, this.bijuBombTY + 0.3, this.bijuBombTZ, markerCount, markerRadius * 0.4, 0.1, markerRadius * 0.4, 0.01, new int[0]);
            if ((double)progress > 0.3) {
               for(int r = 0; r < 20; ++r) {
                  double rAngle = (Math.PI / 10D) * (double)r;
                  double rx = this.bijuBombTX + Math.cos(rAngle) * markerRadius;
                  double rz = this.bijuBombTZ + Math.sin(rAngle) * markerRadius;
                  ws.spawnParticle(EnumParticleTypes.REDSTONE, rx, this.bijuBombTY + (double)0.5F, rz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
               }
            }

         }
      }

      private void executeBijuBomb() {
         this.setShooting(false);
         --this.bijuBombBarrageCount;
         if (this.activeBijuuBomb != null && this.activeBijuuBomb.isEntityAlive() && this.activeBijuuBomb.isCharging()) {
            Vec3d mouth = new Vec3d(this.activeBijuuBomb.posX, this.activeBijuuBomb.posY, this.activeBijuuBomb.posZ);
            Vec3d tgt = new Vec3d(this.bijuBombTX, this.bijuBombTY + (double)1.5F, this.bijuBombTZ);
            this.activeBijuuBomb.launchToward(tgt.subtract(mouth), 1.4F);
         }

         this.activeBijuuBomb = null;
         if (this.bijuBombBarrageCount > 0) {
            this.bijuBombBarrageDelay = 20;
         }

      }

      private void doTentacleSlamLeap(EntityLivingBase target) {
         this.tentacleSlamLeapCooldown = (int)(200.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 35;
         this.isTentacleLeaping = true;
         this.tentacleLeapTicks = 30;
         this.tentacleLeapTargetX = target.posX;
         this.tentacleLeapTargetZ = target.posZ;
         this.motionY = (double)2.5F;
         this.velocityChanged = true;

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F))) {
            p.sendMessage(new TextComponentString("§8§lGyuki leaps into the air! All 8 tentacles prepare to slam!"));
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.HOSTILE, 6.0F, 0.3F);
      }

      private void processTentacleSlamLeap() {
         --this.tentacleLeapTicks;
         if (this.tentacleLeapTicks > 0) {
            if (this.tentacleLeapTicks > 15) {
               this.motionY = Math.max(this.motionY, 0.6);
               double dx = this.tentacleLeapTargetX - this.posX;
               double dz = this.tentacleLeapTargetZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > (double)0.5F) {
                  this.motionX = dx / len * (double)1.0F;
                  this.motionZ = dz / len * (double)1.0F;
               }
            } else {
               double dx = this.tentacleLeapTargetX - this.posX;
               double dz = this.tentacleLeapTargetZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > (double)0.5F) {
                  this.motionX = dx / len * (double)2.5F;
                  this.motionZ = dz / len * (double)2.5F;
               }

               this.motionY = (double)-2.0F;
            }

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY, this.posZ, 15, (double)3.0F, (double)1.0F, (double)3.0F, 0.1, new int[0]);
               ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY, this.posZ, 10, (double)2.0F, (double)1.0F, (double)2.0F, 0.05, new int[0]);
            }

         } else {
            this.isTentacleLeaping = false;
            BlockPos gp = new BlockPos(this.posX, this.posY, this.posZ);
            int groundY = this.world.getHeight(gp).getY();
            if (groundY > 1 && Math.abs(this.posY - (double)groundY) > (double)2.0F) {
               this.setPositionAndUpdate(this.posX, (double)groundY, this.posZ);
            }

            this.motionY = (double)0.0F;
            float dmg = 87.2F * this.getPhaseMultiplier();
            float normalDmg = dmg * 0.19999999F;
            float trueDmg = dmg * 0.8F;

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.posX - (double)14.0F, this.posY - (double)3.0F, this.posZ - (double)14.0F, this.posX + (double)14.0F, this.posY + (double)8.0F, this.posZ + (double)14.0F), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
               double d = Math.sqrt(Math.pow(p.posX - this.posX, (double)2.0F) + Math.pow(p.posZ - this.posZ, (double)2.0F));
               if (d <= (double)14.0F) {
                  float falloff = 1.0F - (float)(d / (double)14.0F) * 0.3F;
                  p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
                  double dx = p.posX - this.posX;
                  double dz = p.posZ - this.posZ;
                  double len = Math.sqrt(dx * dx + dz * dz);
                  if (len > 0.01) {
                     p.motionX += dx / len * (double)3.0F;
                     ++p.motionY;
                     p.motionZ += dz / len * (double)3.0F;
                     p.velocityChanged = true;
                  }

                  p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 3, false, true));
                  p.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 80, 0, false, true));
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)2.0F, this.posZ, 8, (double)5.0F, (double)2.0F, (double)5.0F, (double)0.0F, new int[0]);

               for(int t = 0; t < 8; ++t) {
                  double angle = (Math.PI / 4D) * (double)t;

                  for(int s = 2; s <= 14; s += 2) {
                     double sx = this.posX + Math.cos(angle) * (double)s;
                     double sz = this.posZ + Math.sin(angle) * (double)s;
                     ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, sx, this.posY + (double)1.0F, sz, 5, (double)0.5F, (double)0.5F, (double)0.5F, 0.05, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.SWEEP_ATTACK, sx, this.posY + (double)1.5F, sz, 2, 0.3, 0.3, 0.3, (double)0.0F, new int[0]);
                  }
               }

               ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + (double)0.5F, this.posZ, 100, 5.6000000000000005, (double)0.5F, 5.6000000000000005, 0.15, new int[]{Block.getStateId(this.world.getBlockState(new BlockPos(this.posX, this.posY - (double)1.0F, this.posZ)))});
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 6.0F, 0.4F);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_IRONGOLEM_ATTACK, SoundCategory.HOSTILE, 5.0F, 0.3F);
         }
      }

      private void doLightningFang(EntityLivingBase target) {
         this.lightningFangCooldown = (int)(160.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 12;
         float phaseMul = this.getPhaseMultiplier();
         float dmg = 43.2F * phaseMul;
         float normalDmg = dmg * 0.19999999F;
         float trueDmg = dmg * 0.8F;
         double originX = this.posX;
         double originY = this.posY + (double)this.height * 0.3;
         double originZ = this.posZ;
         double tx = target.posX;
         double ty = target.posY + (double)target.height * (double)0.5F;
         double tz = target.posZ;
         target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
         target.hurtResistantTime = 0;
         target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         if (target instanceof EntityPlayer) {
            ((EntityPlayer)target).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1, false, true));
         }

         List<EntityPlayer> chainCandidates = this.world.getEntitiesWithinAABB(EntityPlayer.class, target.getEntityBoundingBox().grow((double)8.0F), (px) -> px != target && px.isEntityAlive() && !px.isCreative() && !px.isSpectator());
         int chained = 0;

         for(EntityPlayer p : chainCandidates) {
            if (chained >= 2) {
               break;
            }

            float chainDmg = dmg * 0.6F;
            float chainNormal = chainDmg * 0.19999999F;
            float chainTrue = chainDmg * 0.8F;
            p.attackEntityFrom(DamageSource.causeMobDamage(this), chainNormal);
            p.hurtResistantTime = 0;
            p.attackEntityFrom(DamageSource.MAGIC, chainTrue);
            p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 30, 1, false, true));
            this.drawLightningArc(tx, ty, tz, p.posX, p.posY + (double)p.height * (double)0.5F, p.posZ, 0.7F);
            ++chained;
         }

         this.drawLightningArc(originX, originY, originZ, tx, ty, tz, 1.0F);
         this.world.playSound((EntityPlayer)null, tx, ty, tz, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 2.5F, 1.2F);
         this.world.playSound((EntityPlayer)null, originX, originY, originZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 3.0F, 1.6F);
      }

      private void drawLightningArc(double x1, double y1, double z1, double x2, double y2, double z2, float density) {
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            double dx = x2 - x1;
            double dy = y2 - y1;
            double dz = z2 - z1;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (!(len < 0.1)) {
               int segments = (int)(len * (double)density * (double)1.5F);
               if (segments < 4) {
                  segments = 4;
               }

               for(int s = 0; s <= segments; ++s) {
                  double t = (double)s / (double)segments;
                  double jx = (this.rand.nextDouble() - (double)0.5F) * 0.6;
                  double jy = (this.rand.nextDouble() - (double)0.5F) * 0.6;
                  double jz = (this.rand.nextDouble() - (double)0.5F) * 0.6;
                  double px = x1 + dx * t + jx;
                  double py = y1 + dy * t + jy;
                  double pz = z1 + dz * t + jz;
                  ws.spawnParticle(EnumParticleTypes.END_ROD, px, py, pz, 2, 0.05, 0.05, 0.05, (double)0.0F, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, px, py, pz, 3, 0.1, 0.1, 0.1, 0.15, new int[0]);
                  if (s % 3 == 0) {
                     ws.spawnParticle(EnumParticleTypes.CLOUD, px, py, pz, 1, 0.1, 0.1, 0.1, (double)0.0F, new int[0]);
                  }
               }

            }
         }
      }

      private void doInkSpray(EntityLivingBase target) {
         this.inkSprayCooldown = (int)(240.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 25;
         float phaseMul = this.getPhaseMultiplier();
         float dmg = 33.6F * phaseMul;
         float normalDmg = dmg * 0.19999999F;
         float trueDmg = dmg * 0.8F;
         double mouthX = this.posX + this.getLookVec().x * (double)6.0F;
         double mouthY = this.posY + (double)this.height * 0.3;
         double mouthZ = this.posZ + this.getLookVec().z * (double)6.0F;
         double fx = target.posX - this.posX;
         double fz = target.posZ - this.posZ;
         double fl = Math.sqrt(fx * fx + fz * fz);
         if (!(fl < 0.01)) {
            double fnx = fx / fl;
            double fnz = fz / fl;
            double coneRange = (double)25.0F;
            double coneCosHalfAngle = Math.cos(Math.toRadians((double)20.0F));

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.posX - coneRange, this.posY - (double)5.0F, this.posZ - coneRange, this.posX + coneRange, this.posY + (double)10.0F, this.posZ + coneRange), (pxx) -> pxx.isEntityAlive() && !pxx.isCreative() && !pxx.isSpectator())) {
               double px = p.posX - this.posX;
               double pz = p.posZ - this.posZ;
               double pl = Math.sqrt(px * px + pz * pz);
               if (!(pl < 0.01) && !(pl > coneRange)) {
                  double dot = (px * fnx + pz * fnz) / pl;
                  if (!(dot < coneCosHalfAngle)) {
                     p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                     p.hurtResistantTime = 0;
                     p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                     p.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 80, 1, false, true));
                     p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 2, false, true));
                  }
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;

               for(double d = (double)1.0F; d < coneRange; ++d) {
                  double cx = mouthX + fnx * d;
                  double cz = mouthZ + fnz * d;
                  double cy = mouthY - d * 0.08;
                  double spread = (double)0.5F + d * (double)0.25F;
                  int count = Math.max(4, 14 - (int)(d * 0.4));
                  ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, cx, cy, cz, count, spread, spread * 0.4, spread, 0.02, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, cx, cy, cz, count, spread, spread * 0.4, spread, 0.02, new int[0]);
                  if ((int)d % 2 == 0) {
                     ws.spawnParticle(EnumParticleTypes.PORTAL, cx, cy, cz, 6, spread, spread * 0.3, spread, 0.05, new int[0]);
                  }
               }
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 4.0F, 0.4F);
         }
      }

      private void doTentacleVolley(EntityLivingBase target) {
         this.tentacleVolleyCooldown = (int)(200.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 20;
         double fx = target.posX - this.posX;
         double fz = target.posZ - this.posZ;
         double fl = Math.sqrt(fx * fx + fz * fz);
         if (!(fl < 0.01)) {
            double baseAngle = Math.atan2(fz, fx);
            double speed = 2.4;
            double spreadRad = Math.toRadians((double)15.0F);

            for(int i = 0; i < 3; ++i) {
               double a = baseAngle + (double)(i - 1) * spreadRad;
               double dy = target.posY + (double)target.height * (double)0.5F - (this.posY + (double)this.height * 0.3);
               double vy = dy / Math.max(fl, (double)1.0F) * speed + 0.3;
               this.tpX[i] = this.posX + Math.cos(a) * (double)6.0F;
               this.tpY[i] = this.posY + (double)this.height * 0.3;
               this.tpZ[i] = this.posZ + Math.sin(a) * (double)6.0F;
               this.tpVX[i] = Math.cos(a) * speed;
               this.tpVY[i] = vy;
               this.tpVZ[i] = Math.sin(a) * speed;
               this.tpTicks[i] = 50;
               this.tpActive[i] = true;
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0F, 1.4F);
         }
      }

      private void processTentacleProjectile(int i) {
         int var10002 = this.tpTicks[i]--;
         if (this.tpTicks[i] <= 0) {
            this.tpActive[i] = false;
         } else {
            double[] var10000 = this.tpX;
            var10000[i] += this.tpVX[i];
            var10000 = this.tpY;
            var10000[i] += this.tpVY[i];
            var10000 = this.tpZ;
            var10000[i] += this.tpVZ[i];
            var10000 = this.tpVY;
            var10000[i] -= 0.05;
            BlockPos pos = new BlockPos(this.tpX[i], this.tpY[i], this.tpZ[i]);
            if (!this.world.isAirBlock(pos)) {
               this.detonateTentacleProjectile(i);
            } else {
               List<EntityPlayer> hit = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.tpX[i] - (double)1.5F, this.tpY[i] - (double)1.5F, this.tpZ[i] - (double)1.5F, this.tpX[i] + (double)1.5F, this.tpY[i] + (double)1.5F, this.tpZ[i] + (double)1.5F), (p) -> p.isEntityAlive() && !p.isCreative() && !p.isSpectator());
               if (!hit.isEmpty()) {
                  this.detonateTentacleProjectile(i);
               } else {
                  if (this.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)this.world;
                     ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.tpX[i], this.tpY[i], this.tpZ[i], 4, 0.3, 0.3, 0.3, 0.02, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.tpX[i], this.tpY[i], this.tpZ[i], 6, 0.3, 0.3, 0.3, 0.03, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.PORTAL, this.tpX[i], this.tpY[i], this.tpZ[i], 3, 0.2, 0.2, 0.2, 0.01, new int[0]);
                  }

               }
            }
         }
      }

      private void detonateTentacleProjectile(int i) {
         this.tpActive[i] = false;
         float radius = 3.5F;
         float phaseMul = this.getPhaseMultiplier();
         float dmg = 30.4F * phaseMul;
         float normalDmg = dmg * 0.19999999F;
         float trueDmg = dmg * 0.8F;

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.tpX[i] - (double)radius, this.tpY[i] - (double)radius, this.tpZ[i] - (double)radius, this.tpX[i] + (double)radius, this.tpY[i] + (double)radius, this.tpZ[i] + (double)radius), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
            double d = p.getDistance(this.tpX[i], this.tpY[i], this.tpZ[i]);
            if (!(d > (double)radius)) {
               p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
               p.hurtResistantTime = 0;
               p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
               p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1, false, true));
               double dx = p.posX - this.tpX[i];
               double dy = p.posY - this.tpY[i];
               double dz = p.posZ - this.tpZ[i];
               double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
               if (len > 0.01) {
                  p.motionX += dx / len * 1.2;
                  p.motionY += 0.35;
                  p.motionZ += dz / len * 1.2;
                  p.velocityChanged = true;
               }
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.tpX[i], this.tpY[i], this.tpZ[i], 3, (double)1.0F, (double)1.0F, (double)1.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.tpX[i], this.tpY[i], this.tpZ[i], 25, (double)2.0F, (double)1.5F, (double)2.0F, 0.08, new int[0]);
            ws.spawnParticle(EnumParticleTypes.PORTAL, this.tpX[i], this.tpY[i], this.tpZ[i], 20, (double)1.5F, (double)1.0F, (double)1.5F, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.tpX[i], this.tpY[i], this.tpZ[i], SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 1.6F);
      }

      private void switchTarget() {
         List<EntityPlayer> nearbyPlayers = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)96.0F), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator());
         if (nearbyPlayers.size() > 1) {
            EntityLivingBase current = this.getAttackTarget();
            if (this.rand.nextBoolean()) {
               EntityPlayer nearest = null;
               double nearestDist = Double.MAX_VALUE;

               for(EntityPlayer p : nearbyPlayers) {
                  if (p != current) {
                     double d = this.getDistanceSq(p);
                     if (d < nearestDist) {
                        nearestDist = d;
                        nearest = p;
                     }
                  }
               }

               if (nearest != null) {
                  this.setAttackTarget(nearest);
               }
            } else {
               nearbyPlayers.removeIf((px) -> px == current);
               if (!nearbyPlayers.isEmpty()) {
                  this.setAttackTarget((EntityLivingBase)nearbyPlayers.get(this.rand.nextInt(nearbyPlayers.size())));
               }
            }
         }

      }

      private double findSafeY(double x, double y, double z) {
         int bx = MathHelper.floor(x);
         int bz = MathHelper.floor(z);

         for(int by = MathHelper.floor(y); by > 0; --by) {
            BlockPos pos = new BlockPos(bx, by, bz);
            if (!this.world.isAirBlock(pos) && this.world.isAirBlock(pos.up())) {
               return (double)by + (double)1.0F;
            }
         }

         return y;
      }

      private boolean isIceDome(EntityLivingBase target) {
         if (target == null) {
            return false;
         } else {
            String cn = target.getClass().getName().toLowerCase();
            return cn.contains("icedome") || cn.contains("shieldbase");
         }
      }

      private EntityLivingBase findNearbyIceDome(double range) {
         List<EntityLivingBase> nearby = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(range), (ex) -> ex != null && ex.isEntityAlive() && this.isIceDome(ex));
         EntityLivingBase nearest = null;
         double nearestDist = Double.MAX_VALUE;

         for(EntityLivingBase e : nearby) {
            double d = this.getDistanceSq(e);
            if (d < nearestDist) {
               nearestDist = d;
               nearest = e;
            }
         }

         return nearest;
      }

      private void spawnDeathParticles() {
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)8.0F, this.posZ, 200, (double)8.0F, (double)6.0F, (double)8.0F, 0.2, new int[0]);
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)6.0F, this.posZ, 20, (double)6.0F, (double)4.0F, (double)6.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + (double)4.0F, this.posZ, 100, (double)6.0F, (double)4.0F, (double)6.0F, 0.1, new int[0]);
            ws.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY + (double)10.0F, this.posZ, 80, (double)4.0F, (double)6.0F, (double)4.0F, 0.3, new int[0]);
         }
      }

      public void onDeath(DamageSource cause) {
         this.spawnDeathParticles();
         if (this.bossInfo != null) {
            this.bossInfo.setVisible(false);

            for(EntityPlayerMP player : new ArrayList(this.bossInfo.getPlayers())) {
               this.bossInfo.removePlayer(player);
            }
         }

         super.onDeath(cause);
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("GyukiPhase", this.currentPhase);
         compound.setBoolean("GyukiPhase2Announced", this.phase2Announced);
         compound.setBoolean("GyukiPhase3Announced", this.phase3Announced);
         compound.setInteger("GyukiHornChargeCd", this.hornChargeCooldown);
         compound.setInteger("GyukiTentacleSweepCd", this.tentacleSweepCooldown);
         compound.setInteger("GyukiInkBlindCd", this.inkBlindCooldown);
         compound.setInteger("GyukiTentacleGrabCd", this.tentacleGrabCooldown);
         compound.setInteger("GyukiLariatCd", this.lariatCooldown);
         compound.setInteger("GyukiBijuBombBarrageCd", this.bijuBombBarrageCooldown);
         compound.setInteger("GyukiTentacleSlamLeapCd", this.tentacleSlamLeapCooldown);
         compound.setInteger("GyukiLightningFangCd", this.lightningFangCooldown);
         compound.setInteger("GyukiInkSprayCd", this.inkSprayCooldown);
         compound.setInteger("GyukiTentacleVolleyCd", this.tentacleVolleyCooldown);
         compound.setInteger("GyukiDespawnTimer", this.despawnTimer);
         if (this.hasSpawnAnchor) {
            compound.setDouble("GyukiAnchorX", this.spawnAnchorX);
            compound.setDouble("GyukiAnchorY", this.spawnAnchorY);
            compound.setDouble("GyukiAnchorZ", this.spawnAnchorZ);
         }

      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.currentPhase = compound.getInteger("GyukiPhase");
         if (this.currentPhase < 1) {
            this.currentPhase = 1;
         }

         this.phase2Announced = compound.getBoolean("GyukiPhase2Announced");
         this.phase3Announced = compound.getBoolean("GyukiPhase3Announced");
         this.hornChargeCooldown = compound.getInteger("GyukiHornChargeCd");
         this.tentacleSweepCooldown = compound.getInteger("GyukiTentacleSweepCd");
         this.inkBlindCooldown = compound.getInteger("GyukiInkBlindCd");
         this.tentacleGrabCooldown = compound.getInteger("GyukiTentacleGrabCd");
         this.lariatCooldown = compound.getInteger("GyukiLariatCd");
         this.bijuBombBarrageCooldown = compound.getInteger("GyukiBijuBombBarrageCd");
         this.tentacleSlamLeapCooldown = compound.getInteger("GyukiTentacleSlamLeapCd");
         this.lightningFangCooldown = compound.getInteger("GyukiLightningFangCd");
         this.inkSprayCooldown = compound.getInteger("GyukiInkSprayCd");
         this.tentacleVolleyCooldown = compound.getInteger("GyukiTentacleVolleyCd");
         this.despawnTimer = compound.getInteger("GyukiDespawnTimer");
         if (this.despawnTimer <= 0) {
            this.despawnTimer = 36000;
         }

         if (compound.hasKey("GyukiAnchorX")) {
            this.spawnAnchorX = compound.getDouble("GyukiAnchorX");
            this.spawnAnchorY = compound.getDouble("GyukiAnchorY");
            this.spawnAnchorZ = compound.getDouble("GyukiAnchorZ");
            this.hasSpawnAnchor = true;
         }

         if (!this.world.isRemote && this.bossInfo == null) {
            this.bossInfo = new BossInfoServer(new TextComponentString("§8§lGyuki - The Eight-Tails"), Color.WHITE, Overlay.PROGRESS);
         }

      }

      static {
         SHOOTING = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
      }
   }
}
