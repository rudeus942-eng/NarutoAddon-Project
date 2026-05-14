
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
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
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityQuestShukaku extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 21;
   public static final int ENTITYID_RANGED = 22;

   public EntityQuestShukaku(ElementsInfTsukAddon instance) {
      super(instance, 38);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "questshukaku"), 21).name("questshukaku").tracker(96, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, ShukakuRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class ShukakuRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod", "textures/onetail.png");

      public ShukakuRenderer(RenderManager renderManager) {
         super(renderManager, new ModelShukaku(), 3.0F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         float s = 0.36842105F;
         GlStateManager.scale(s, s, s);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelShukaku extends ModelBiped {
      private final ModelRenderer eyes;
      private final ModelRenderer cube_r1;
      private final ModelRenderer cube_r2;
      private final ModelRenderer cube_r3;
      private final ModelRenderer cube_r4;
      private final ModelRenderer cube_r5;
      private final ModelRenderer jaw;
      private final ModelRenderer bone3;
      private final ModelRenderer cube_r6;
      private final ModelRenderer bone4;
      private final ModelRenderer cube_r7;
      private final ModelRenderer rightArm;
      private final ModelRenderer cube_r8;
      private final ModelRenderer cube_r9;
      private final ModelRenderer cube_r10;
      private final ModelRenderer rightHand;
      private final ModelRenderer bone2;
      private final ModelRenderer bone13;
      private final ModelRenderer bone14;
      private final ModelRenderer bone15;
      private final ModelRenderer bone16;
      private final ModelRenderer bone17;
      private final ModelRenderer bone18;
      private final ModelRenderer bone19;
      private final ModelRenderer leftArm;
      private final ModelRenderer cube_r11;
      private final ModelRenderer cube_r12;
      private final ModelRenderer cube_r13;
      private final ModelRenderer leftHand;
      private final ModelRenderer bone5;
      private final ModelRenderer bone6;
      private final ModelRenderer bone11;
      private final ModelRenderer bone12;
      private final ModelRenderer bone7;
      private final ModelRenderer bone8;
      private final ModelRenderer bone9;
      private final ModelRenderer bone10;
      private final ModelRenderer stomach;
      private final ModelRenderer cube_r22;
      private final ModelRenderer cube_r23;
      private final ModelRenderer upperbody;
      private final ModelRenderer cube_r24;
      private final ModelRenderer cube_r25;
      private final ModelRenderer cube_r26;
      private final ModelRenderer rightFoot;
      private final ModelRenderer cube_r27;
      private final ModelRenderer cube_r28;
      private final ModelRenderer cube_r29;
      private final ModelRenderer cube_r30;
      private final ModelRenderer cube_r31;
      private final ModelRenderer cube_r32;
      private final ModelRenderer leftFoot;
      private final ModelRenderer cube_r33;
      private final ModelRenderer cube_r34;
      private final ModelRenderer cube_r35;
      private final ModelRenderer cube_r36;
      private final ModelRenderer cube_r37;
      private final ModelRenderer[] tail = new ModelRenderer[9];
      private final float[] tailSwayX;
      private final float[] tailSwayY;
      private final float[] tailSwayZ;
      private final Random rand;

      public ModelShukaku() {
         this.tailSwayX = new float[this.tail.length];
         this.tailSwayY = new float[this.tail.length];
         this.tailSwayZ = new float[this.tail.length];
         this.rand = new Random();
         this.textureWidth = 64;
         this.textureHeight = 64;
         (this.bipedHeadwear = new ModelRenderer(this)).setRotationPoint(0.0F, 21.0F, 3.0F);
         (this.eyes = new ModelRenderer(this)).setRotationPoint(0.0F, -9.6F, -7.0F);
         this.bipedHeadwear.addChild(this.eyes);
         this.eyes.cubeList.add(new ModelBox(this.eyes, 41, 4, -1.5F, -3.0F, -4.7F, 3, 1, 0, 0.0F, false));
         (this.bipedBody = new ModelRenderer(this)).setRotationPoint(0.0F, 21.0F, 3.0F);
         (this.bipedHead = new ModelRenderer(this)).setRotationPoint(0.0F, -9.6F, -7.0F);
         this.bipedBody.addChild(this.bipedHead);
         this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 51, 8, -2.0F, -0.72F, -5.4006F, 4, 0, 3, 0.0F, false));
         (this.cube_r1 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.4F, -5.5F);
         this.bipedHead.addChild(this.cube_r1);
         this.setRotationAngle(this.cube_r1, 0.3054F, 0.0F, 0.0F);
         this.cube_r1.cubeList.add(new ModelBox(this.cube_r1, 58, 2, -1.0F, 0.55F, -0.2921F, 2, 1, 1, 0.1F, false));
         (this.cube_r2 = new ModelRenderer(this)).setRotationPoint(0.0F, 12.6F, -1.5F);
         this.bipedHead.addChild(this.cube_r2);
         this.setRotationAngle(this.cube_r2, 0.1309F, 0.0F, 0.0F);
         this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 48, 0, -1.5F, -14.9F, -2.1F, 3, 2, 2, 0.0F, false));
         (this.cube_r3 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -2.5F);
         this.bipedHead.addChild(this.cube_r3);
         this.setRotationAngle(this.cube_r3, -0.1745F, 0.0F, 0.0F);
         this.cube_r3.cubeList.add(new ModelBox(this.cube_r3, 0, 54, -2.0F, -3.2623F, -0.9368F, 4, 4, 3, -0.1F, false));
         (this.cube_r4 = new ModelRenderer(this)).setRotationPoint(0.0F, -4.4193F, -0.8247F);
         this.bipedHead.addChild(this.cube_r4);
         this.setRotationAngle(this.cube_r4, -0.3491F, 0.0F, 0.0F);
         this.cube_r4.cubeList.add(new ModelBox(this.cube_r4, 50, 36, -2.0F, -0.0057F, 0.0F, 4, 4, 2, 0.0F, false));
         (this.cube_r5 = new ModelRenderer(this)).setRotationPoint(0.0F, 13.0F, -1.5F);
         this.bipedHead.addChild(this.cube_r5);
         this.setRotationAngle(this.cube_r5, 0.0873F, 0.0F, 0.0F);
         this.cube_r5.cubeList.add(new ModelBox(this.cube_r5, 32, 35, -2.0F, -17.3F, -1.8F, 4, 4, 4, 0.0F, false));
         (this.jaw = new ModelRenderer(this)).setRotationPoint(0.0F, -0.7346F, -2.3706F);
         this.bipedHead.addChild(this.jaw);
         this.jaw.cubeList.add(new ModelBox(this.jaw, 47, 13, -1.5F, 0.0F, -2.9F, 3, 1, 3, 0.2F, false));
         this.jaw.cubeList.add(new ModelBox(this.jaw, 47, 13, -1.5F, 0.3F, -2.9F, 3, 0, 3, 0.2F, false));
         (this.bone3 = new ModelRenderer(this)).setRotationPoint(-1.1F, -3.5F, -4.2F);
         this.bipedHead.addChild(this.bone3);
         this.setRotationAngle(this.bone3, 0.3491F, -0.1745F, 0.1745F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 58, 0, -1.1F, -0.6F, -0.8F, 2, 1, 1, 0.0F, false));
         (this.cube_r6 = new ModelRenderer(this)).setRotationPoint(-0.8F, -0.3F, 0.1F);
         this.bone3.addChild(this.cube_r6);
         this.setRotationAngle(this.cube_r6, 0.0F, -0.5236F, 0.0F);
         this.cube_r6.cubeList.add(new ModelBox(this.cube_r6, 0, 6, -0.5F, -0.4F, -0.1F, 1, 1, 2, -0.1F, false));
         (this.bone4 = new ModelRenderer(this)).setRotationPoint(1.1F, -3.5F, -4.2F);
         this.bipedHead.addChild(this.bone4);
         this.setRotationAngle(this.bone4, 0.3491F, 0.1745F, -0.1745F);
         this.bone4.cubeList.add(new ModelBox(this.bone4, 58, 0, -0.9F, -0.6F, -0.8F, 2, 1, 1, 0.0F, true));
         (this.cube_r7 = new ModelRenderer(this)).setRotationPoint(0.8F, -0.3F, 0.1F);
         this.bone4.addChild(this.cube_r7);
         this.setRotationAngle(this.cube_r7, 0.0F, 0.5236F, 0.0F);
         this.cube_r7.cubeList.add(new ModelBox(this.cube_r7, 0, 6, -0.5F, -0.4F, -0.1F, 1, 1, 2, -0.1F, true));
         (this.bipedRightArm = new ModelRenderer(this)).setRotationPoint(-4.0F, -9.0F, -4.5F);
         this.bipedBody.addChild(this.bipedRightArm);
         (this.rightArm = new ModelRenderer(this)).setRotationPoint(4.0F, -2.0F, -1.0F);
         this.bipedRightArm.addChild(this.rightArm);
         this.setRotationAngle(this.rightArm, -0.2182F, 0.0F, 0.0873F);
         (this.cube_r8 = new ModelRenderer(this)).setRotationPoint(-5.4741F, 1.5764F, -0.1412F);
         this.rightArm.addChild(this.cube_r8);
         this.setRotationAngle(this.cube_r8, 0.1309F, 0.0F, 0.7854F);
         this.cube_r8.cubeList.add(new ModelBox(this.cube_r8, 20, 39, -0.5F, -2.25F, -2.0F, 2, 3, 4, 0.5F, false));
         (this.cube_r9 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.rightArm.addChild(this.cube_r9);
         this.setRotationAngle(this.cube_r9, 0.1309F, 0.0F, 0.2182F);
         this.cube_r9.cubeList.add(new ModelBox(this.cube_r9, 10, 37, -5.9032F, 2.6821F, -1.9955F, 2, 6, 3, 0.5F, false));
         (this.cube_r10 = new ModelRenderer(this)).setRotationPoint(0.0F, 7.0F, 2.0F);
         this.rightArm.addChild(this.cube_r10);
         this.setRotationAngle(this.cube_r10, -0.2618F, -0.0436F, 0.0F);
         this.cube_r10.cubeList.add(new ModelBox(this.cube_r10, 0, 37, -7.5298F, 0.4835F, -2.5163F, 2, 7, 3, 0.6F, false));
         (this.rightHand = new ModelRenderer(this)).setRotationPoint(-5.35F, 9.25F, -1.75F);
         this.rightArm.addChild(this.rightHand);
         this.setRotationAngle(this.rightHand, 0.0F, -0.3054F, 0.0F);
         (this.bone2 = new ModelRenderer(this)).setRotationPoint(0.0395F, 4.3748F, -0.9884F);
         this.rightHand.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.6392F, 0.1139F, -0.0876F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 1, 18, -0.4582F, -0.5478F, -1.8992F, 1, 1, 2, 0.0F, false));
         (this.bone13 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.65F);
         this.bone2.addChild(this.bone13);
         this.setRotationAngle(this.bone13, 0.4363F, 0.0F, 0.0F);
         this.bone13.cubeList.add(new ModelBox(this.bone13, 1, 18, -0.4582F, -0.5478F, -1.8992F, 1, 1, 2, -0.2F, false));
         (this.bone14 = new ModelRenderer(this)).setRotationPoint(0.5395F, 4.3748F, 0.5116F);
         this.rightHand.addChild(this.bone14);
         this.setRotationAngle(this.bone14, 0.2618F, -0.7418F, 0.2618F);
         this.bone14.cubeList.add(new ModelBox(this.bone14, 1, 18, -0.4582F, -0.5478F, -1.8992F, 1, 1, 2, 0.0F, false));
         (this.bone15 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.65F);
         this.bone14.addChild(this.bone15);
         this.setRotationAngle(this.bone15, 0.5236F, 0.0F, 0.5236F);
         this.bone15.cubeList.add(new ModelBox(this.bone15, 1, 18, -0.4582F, -0.5478F, -1.8992F, 1, 1, 2, -0.2F, false));
         (this.bone16 = new ModelRenderer(this)).setRotationPoint(-1.2605F, 4.4748F, -0.7384F);
         this.rightHand.addChild(this.bone16);
         this.setRotationAngle(this.bone16, 0.6392F, 0.2618F, 0.0F);
         this.bone16.cubeList.add(new ModelBox(this.bone16, 1, 18, -0.4582F, -0.5478F, -1.8992F, 1, 1, 2, 0.0F, false));
         (this.bone17 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.65F);
         this.bone16.addChild(this.bone17);
         this.setRotationAngle(this.bone17, 0.4363F, 0.0F, 0.0F);
         this.bone17.cubeList.add(new ModelBox(this.bone17, 1, 18, -0.4582F, -0.5478F, -1.8992F, 1, 1, 2, -0.2F, false));
         (this.bone18 = new ModelRenderer(this)).setRotationPoint(-2.2605F, 4.2748F, 0.0116F);
         this.rightHand.addChild(this.bone18);
         this.setRotationAngle(this.bone18, 0.6392F, 0.5236F, 0.0F);
         this.bone18.cubeList.add(new ModelBox(this.bone18, 1, 18, -0.4582F, -0.5478F, -1.8992F, 1, 1, 2, 0.0F, false));
         (this.bone19 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.65F);
         this.bone18.addChild(this.bone19);
         this.setRotationAngle(this.bone19, 0.4363F, 0.0F, 0.0F);
         this.bone19.cubeList.add(new ModelBox(this.bone19, 1, 18, -0.4582F, -0.5478F, -1.8992F, 1, 1, 2, -0.2F, false));
         (this.bipedLeftArm = new ModelRenderer(this)).setRotationPoint(4.0F, -9.0F, -4.5F);
         this.bipedBody.addChild(this.bipedLeftArm);
         (this.leftArm = new ModelRenderer(this)).setRotationPoint(-4.0F, -2.0F, -1.0F);
         this.bipedLeftArm.addChild(this.leftArm);
         this.setRotationAngle(this.leftArm, -0.2182F, 0.0F, -0.0873F);
         (this.cube_r11 = new ModelRenderer(this)).setRotationPoint(5.4741F, 1.5764F, -0.1412F);
         this.leftArm.addChild(this.cube_r11);
         this.setRotationAngle(this.cube_r11, 0.1309F, 0.0F, -0.7854F);
         this.cube_r11.cubeList.add(new ModelBox(this.cube_r11, 20, 39, -1.5F, -2.25F, -2.0F, 2, 3, 4, 0.5F, true));
         (this.cube_r12 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.leftArm.addChild(this.cube_r12);
         this.setRotationAngle(this.cube_r12, 0.1309F, 0.0F, -0.2182F);
         this.cube_r12.cubeList.add(new ModelBox(this.cube_r12, 10, 37, 3.9032F, 2.6821F, -1.9955F, 2, 6, 3, 0.5F, true));
         (this.cube_r13 = new ModelRenderer(this)).setRotationPoint(0.0F, 7.0F, 2.0F);
         this.leftArm.addChild(this.cube_r13);
         this.setRotationAngle(this.cube_r13, -0.2618F, 0.0436F, 0.0F);
         this.cube_r13.cubeList.add(new ModelBox(this.cube_r13, 0, 37, 5.5298F, 0.4835F, -2.5163F, 2, 7, 3, 0.6F, true));
         (this.leftHand = new ModelRenderer(this)).setRotationPoint(5.35F, 9.25F, -1.75F);
         this.leftArm.addChild(this.leftHand);
         this.setRotationAngle(this.leftHand, 0.0F, 0.3054F, 0.0F);
         (this.bone5 = new ModelRenderer(this)).setRotationPoint(-0.0395F, 4.3748F, -0.9884F);
         this.leftHand.addChild(this.bone5);
         this.setRotationAngle(this.bone5, 0.6392F, -0.1139F, 0.0876F);
         this.bone5.cubeList.add(new ModelBox(this.bone5, 1, 18, -0.5418F, -0.5478F, -1.8992F, 1, 1, 2, 0.0F, true));
         (this.bone6 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.65F);
         this.bone5.addChild(this.bone6);
         this.setRotationAngle(this.bone6, 0.4363F, 0.0F, 0.0F);
         this.bone6.cubeList.add(new ModelBox(this.bone6, 1, 18, -0.5418F, -0.5478F, -1.8992F, 1, 1, 2, -0.2F, true));
         (this.bone11 = new ModelRenderer(this)).setRotationPoint(-0.5395F, 4.3748F, 0.5116F);
         this.leftHand.addChild(this.bone11);
         this.setRotationAngle(this.bone11, 0.2618F, 0.7418F, -0.2618F);
         this.bone11.cubeList.add(new ModelBox(this.bone11, 1, 18, -0.5418F, -0.5478F, -1.8992F, 1, 1, 2, 0.0F, true));
         (this.bone12 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.65F);
         this.bone11.addChild(this.bone12);
         this.setRotationAngle(this.bone12, 0.5236F, 0.0F, -0.5236F);
         this.bone12.cubeList.add(new ModelBox(this.bone12, 1, 18, -0.5418F, -0.5478F, -1.8992F, 1, 1, 2, -0.2F, true));
         (this.bone7 = new ModelRenderer(this)).setRotationPoint(1.2605F, 4.4748F, -0.7384F);
         this.leftHand.addChild(this.bone7);
         this.setRotationAngle(this.bone7, 0.6392F, -0.2618F, 0.0F);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 1, 18, -0.5418F, -0.5478F, -1.8992F, 1, 1, 2, 0.0F, true));
         (this.bone8 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.65F);
         this.bone7.addChild(this.bone8);
         this.setRotationAngle(this.bone8, 0.4363F, 0.0F, 0.0F);
         this.bone8.cubeList.add(new ModelBox(this.bone8, 1, 18, -0.5418F, -0.5478F, -1.8992F, 1, 1, 2, -0.2F, true));
         (this.bone9 = new ModelRenderer(this)).setRotationPoint(2.2605F, 4.2748F, 0.0116F);
         this.leftHand.addChild(this.bone9);
         this.setRotationAngle(this.bone9, 0.6392F, -0.5236F, 0.0F);
         this.bone9.cubeList.add(new ModelBox(this.bone9, 1, 18, -0.5418F, -0.5478F, -1.8992F, 1, 1, 2, 0.0F, true));
         (this.bone10 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, -1.65F);
         this.bone9.addChild(this.bone10);
         this.setRotationAngle(this.bone10, 0.4363F, 0.0F, 0.0F);
         this.bone10.cubeList.add(new ModelBox(this.bone10, 1, 18, -0.5418F, -0.5478F, -1.8992F, 1, 1, 2, -0.2F, true));
         (this.stomach = new ModelRenderer(this)).setRotationPoint(0.0F, 3.0F, -6.6F);
         this.bipedBody.addChild(this.stomach);
         this.setRotationAngle(this.stomach, -0.2618F, 0.0F, 0.0F);
         (this.cube_r22 = new ModelRenderer(this)).setRotationPoint(0.0F, -12.4F, -3.7F);
         this.stomach.addChild(this.cube_r22);
         this.setRotationAngle(this.cube_r22, 0.2182F, 0.0F, 0.0F);
         this.cube_r22.cubeList.add(new ModelBox(this.cube_r22, 36, 8, -3.0F, 5.0769F, -1.8502F, 6, 7, 1, 0.0F, false));
         (this.cube_r23 = new ModelRenderer(this)).setRotationPoint(4.5F, -5.5F, 1.2F);
         this.stomach.addChild(this.cube_r23);
         this.setRotationAngle(this.cube_r23, 0.2182F, 0.0F, 0.0F);
         this.cube_r23.cubeList.add(new ModelBox(this.cube_r23, 0, 0, -9.0F, -3.7037F, -4.4076F, 9, 8, 9, 0.0F, false));
         (this.upperbody = new ModelRenderer(this)).setRotationPoint(0.0F, 3.0F, -5.5F);
         this.bipedBody.addChild(this.upperbody);
         this.upperbody.cubeList.add(new ModelBox(this.upperbody, 24, 22, -4.0F, -14.8158F, -3.1398F, 8, 3, 8, 0.1F, false));
         (this.cube_r24 = new ModelRenderer(this)).setRotationPoint(0.0F, -15.1581F, 0.4507F);
         this.upperbody.addChild(this.cube_r24);
         this.setRotationAngle(this.cube_r24, -0.3054F, 0.0F, 0.0F);
         this.cube_r24.cubeList.add(new ModelBox(this.cube_r24, 7, 54, -4.0F, -1.0F, -3.3F, 8, 3, 7, -0.05F, false));
         (this.cube_r25 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.upperbody.addChild(this.cube_r25);
         this.setRotationAngle(this.cube_r25, 0.1745F, 0.0F, 0.0F);
         this.cube_r25.cubeList.add(new ModelBox(this.cube_r25, 0, 17, -4.0F, -12.0F, -0.9F, 8, 5, 8, 0.4F, false));
         (this.bipedRightLeg = new ModelRenderer(this)).setRotationPoint(-4.5F, -3.0F, -2.75F);
         this.bipedBody.addChild(this.bipedRightLeg);
         (this.cube_r26 = new ModelRenderer(this)).setRotationPoint(5.5F, 6.0F, -1.75F);
         this.bipedRightLeg.addChild(this.cube_r26);
         this.setRotationAngle(this.cube_r26, -0.1289F, 0.0227F, 0.1731F);
         this.cube_r26.cubeList.add(new ModelBox(this.cube_r26, 44, 53, -9.0F, -6.0F, -1.0F, 5, 6, 5, 0.0F, false));
         (this.rightFoot = new ModelRenderer(this)).setRotationPoint(4.75F, 5.75F, -1.75F);
         this.bipedRightLeg.addChild(this.rightFoot);
         (this.cube_r27 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.1F, 0.0F);
         this.rightFoot.addChild(this.cube_r27);
         this.setRotationAngle(this.cube_r27, 0.0894F, 0.2173F, 0.0193F);
         this.cube_r27.cubeList.add(new ModelBox(this.cube_r27, 27, 43, -8.0F, -0.75F, -4.3F, 1, 1, 5, 0.1F, false));
         (this.cube_r28 = new ModelRenderer(this)).setRotationPoint(-1.0F, -0.1F, 0.0F);
         this.rightFoot.addChild(this.cube_r28);
         this.setRotationAngle(this.cube_r28, 0.088F, 0.1304F, 0.0115F);
         this.cube_r28.cubeList.add(new ModelBox(this.cube_r28, 43, 38, -5.5F, -0.75F, -3.5F, 1, 1, 5, 0.1F, false));
         (this.cube_r29 = new ModelRenderer(this)).setRotationPoint(-1.0F, -0.1F, 0.0F);
         this.rightFoot.addChild(this.cube_r29);
         this.setRotationAngle(this.cube_r29, 0.0876F, 0.0869F, 0.0076F);
         this.cube_r29.cubeList.add(new ModelBox(this.cube_r29, 20, 46, -4.0F, -0.75F, -3.25F, 1, 1, 4, 0.1F, false));
         (this.cube_r30 = new ModelRenderer(this)).setRotationPoint(-1.0F, -0.1F, 0.0F);
         this.rightFoot.addChild(this.cube_r30);
         this.setRotationAngle(this.cube_r30, 0.1526F, -0.4332F, -0.053F);
         this.cube_r30.cubeList.add(new ModelBox(this.cube_r30, 0, 47, -1.95F, -0.8F, -1.1F, 1, 1, 4, 0.1F, false));
         (this.cube_r31 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.rightFoot.addChild(this.cube_r31);
         this.setRotationAngle(this.cube_r31, -0.0175F, 0.0F, 0.0F);
         this.cube_r31.cubeList.add(new ModelBox(this.cube_r31, 34, 44, -8.0F, -1.0F, 1.35F, 5, 1, 2, 0.2F, false));
         (this.bipedLeftLeg = new ModelRenderer(this)).setRotationPoint(4.5F, -3.0F, -2.75F);
         this.bipedBody.addChild(this.bipedLeftLeg);
         (this.cube_r32 = new ModelRenderer(this)).setRotationPoint(-5.5F, 6.0F, -1.75F);
         this.bipedLeftLeg.addChild(this.cube_r32);
         this.setRotationAngle(this.cube_r32, -0.1289F, -0.0227F, -0.1731F);
         this.cube_r32.cubeList.add(new ModelBox(this.cube_r32, 44, 53, 4.0F, -6.0F, -1.0F, 5, 6, 5, 0.0F, true));
         (this.leftFoot = new ModelRenderer(this)).setRotationPoint(-4.75F, 5.75F, -1.75F);
         this.bipedLeftLeg.addChild(this.leftFoot);
         (this.cube_r33 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.1F, 0.0F);
         this.leftFoot.addChild(this.cube_r33);
         this.setRotationAngle(this.cube_r33, 0.0894F, -0.2173F, -0.0193F);
         this.cube_r33.cubeList.add(new ModelBox(this.cube_r33, 27, 43, 7.0F, -0.75F, -4.3F, 1, 1, 5, 0.1F, true));
         (this.cube_r34 = new ModelRenderer(this)).setRotationPoint(1.0F, -0.1F, 0.0F);
         this.leftFoot.addChild(this.cube_r34);
         this.setRotationAngle(this.cube_r34, 0.088F, -0.1304F, -0.0115F);
         this.cube_r34.cubeList.add(new ModelBox(this.cube_r34, 43, 38, 4.5F, -0.75F, -3.5F, 1, 1, 5, 0.1F, true));
         (this.cube_r35 = new ModelRenderer(this)).setRotationPoint(1.0F, -0.1F, 0.0F);
         this.leftFoot.addChild(this.cube_r35);
         this.setRotationAngle(this.cube_r35, 0.0876F, -0.0869F, -0.0076F);
         this.cube_r35.cubeList.add(new ModelBox(this.cube_r35, 20, 46, 3.0F, -0.75F, -3.25F, 1, 1, 4, 0.1F, true));
         (this.cube_r36 = new ModelRenderer(this)).setRotationPoint(1.0F, -0.1F, 0.0F);
         this.leftFoot.addChild(this.cube_r36);
         this.setRotationAngle(this.cube_r36, 0.1526F, 0.4332F, 0.053F);
         this.cube_r36.cubeList.add(new ModelBox(this.cube_r36, 0, 47, 0.95F, -0.8F, -1.1F, 1, 1, 4, 0.1F, true));
         (this.cube_r37 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.leftFoot.addChild(this.cube_r37);
         this.setRotationAngle(this.cube_r37, -0.0175F, 0.0F, 0.0F);
         this.cube_r37.cubeList.add(new ModelBox(this.cube_r37, 34, 44, 3.0F, -1.0F, 1.35F, 5, 1, 2, 0.2F, true));
         (this.tail[0] = new ModelRenderer(this)).setRotationPoint(0.0F, 20.0F, 3.0F);
         this.setRotationAngle(this.tail[0], -1.0472F, 0.0F, 0.0F);
         this.tail[0].cubeList.add(new ModelBox(this.tail[0], 0, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 2.2F, false));
         (this.tail[1] = new ModelRenderer(this)).setRotationPoint(0.0F, -4.0F, 0.0F);
         this.tail[0].addChild(this.tail[1]);
         this.setRotationAngle(this.tail[1], 0.4363F, 0.0F, 0.2618F);
         this.tail[1].cubeList.add(new ModelBox(this.tail[1], 0, 0, -1.0F, -5.5F, -1.0F, 2, 4, 2, 2.0F, false));
         (this.tail[2] = new ModelRenderer(this)).setRotationPoint(0.0F, -6.0F, 0.0F);
         this.tail[1].addChild(this.tail[2]);
         this.setRotationAngle(this.tail[2], 0.4363F, 0.0F, 0.2618F);
         this.tail[2].cubeList.add(new ModelBox(this.tail[2], 0, 0, -1.0F, -5.5F, -1.0F, 2, 4, 2, 1.8F, false));
         (this.tail[3] = new ModelRenderer(this)).setRotationPoint(0.0F, -6.0F, 0.0F);
         this.tail[2].addChild(this.tail[3]);
         this.setRotationAngle(this.tail[3], 0.4363F, 0.0F, 0.2618F);
         this.tail[3].cubeList.add(new ModelBox(this.tail[3], 0, 0, -1.0F, -5.5F, -1.0F, 2, 4, 2, 1.6F, false));
         (this.tail[4] = new ModelRenderer(this)).setRotationPoint(0.0F, -5.0F, 0.0F);
         this.tail[3].addChild(this.tail[4]);
         this.setRotationAngle(this.tail[4], 0.4363F, 0.0F, -0.2618F);
         this.tail[4].cubeList.add(new ModelBox(this.tail[4], 0, 0, -1.0F, -5.5F, -1.0F, 2, 4, 2, 1.4F, false));
         (this.tail[5] = new ModelRenderer(this)).setRotationPoint(0.0F, -5.0F, 0.0F);
         this.tail[4].addChild(this.tail[5]);
         this.setRotationAngle(this.tail[5], 0.4363F, 0.0F, -0.2618F);
         this.tail[5].cubeList.add(new ModelBox(this.tail[5], 0, 0, -1.0F, -5.5F, -1.0F, 2, 4, 2, 1.2F, false));
         (this.tail[6] = new ModelRenderer(this)).setRotationPoint(0.0F, -5.25F, 0.0F);
         this.tail[5].addChild(this.tail[6]);
         this.setRotationAngle(this.tail[6], 0.4363F, 0.0F, -0.2618F);
         this.tail[6].cubeList.add(new ModelBox(this.tail[6], 0, 0, -1.0F, -5.0F, -1.0F, 2, 4, 2, 0.6F, false));
         (this.tail[7] = new ModelRenderer(this)).setRotationPoint(0.0F, -4.5F, 0.0F);
         this.tail[6].addChild(this.tail[7]);
         this.setRotationAngle(this.tail[7], 0.4363F, 0.0F, -0.2618F);
         this.tail[7].cubeList.add(new ModelBox(this.tail[7], 0, 0, -1.0F, -4.5F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.tail[8] = new ModelRenderer(this)).setRotationPoint(0.0F, -4.0F, 0.0F);
         this.tail[7].addChild(this.tail[8]);
         this.setRotationAngle(this.tail[8], 0.3491F, 0.0F, -0.1745F);
         this.tail[8].cubeList.add(new ModelBox(this.tail[8], 0, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, -0.4F, false));

         for(int j = 1; j < this.tail.length; ++j) {
            this.tailSwayX[j] = (this.rand.nextFloat() * 0.1745F + 0.1745F) * (this.rand.nextBoolean() ? -1.0F : 1.0F);
            this.tailSwayZ[j] = (this.rand.nextFloat() * 0.1745F + 0.1745F) * (this.rand.nextBoolean() ? -1.0F : 1.0F);
            this.tailSwayY[j] = this.rand.nextFloat() * 0.0873F + 0.0873F;
         }

      }

      public void render(Entity entity, float f0, float f1, float f2, float f3, float f4, float f5) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, -27.0F * f5, 0.0F);
         GlStateManager.scale(19.0F, 19.0F, 19.0F);
         this.bipedBody.render(f5);
         this.tail[0].render(f5);
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
         var10000.rotationPointY -= 9.6F;
         this.bipedRightArm.setRotationPoint(-4.0F, -9.0F, -4.5F);
         this.bipedLeftArm.setRotationPoint(4.0F, -9.0F, -4.5F);
         this.bipedRightLeg.setRotationPoint(-4.5F, -3.0F, -3.75F);
         this.bipedLeftLeg.setRotationPoint(4.5F, -3.0F, -3.75F);

         for(int j = 1; j < this.tail.length; ++j) {
            this.tail[j].rotateAngleX = 0.2618F + MathHelper.sin((f2 - (float)j) * 0.05F) * this.tailSwayX[j];
            this.tail[j].rotateAngleZ = MathHelper.cos((f2 - (float)j) * 0.05F) * this.tailSwayZ[j];
            this.tail[j].rotateAngleY = MathHelper.sin((f2 - (float)j) * 0.1F) * this.tailSwayY[j];
         }

         EntityCustom shukaku = e instanceof EntityCustom ? (EntityCustom)e : null;
         if (shukaku != null && shukaku.isShooting()) {
            var10000 = this.bipedHead;
            var10000.rotateAngleX -= 0.5236F;
            this.jaw.rotateAngleX = 0.7854F;
         } else {
            this.jaw.rotateAngleX = 0.0F;
         }

         this.bipedBody.rotationPointY = 3.0F;
         this.bipedBody.rotateAngleX = 0.0F;
         this.tail[0].rotationPointY = 3.0F;
         this.tail[0].rotateAngleX = -1.0472F;
         copyModelAngles(this.bipedBody, this.bipedHeadwear);
         copyModelAngles(this.bipedHead, this.eyes);
      }
   }

   public static class EntityCustom extends EntityCreature implements INpcConfigurable {
      private static final DataParameter<String> NPC_CONFIG_ID;
      private static final DataParameter<Boolean> SHOOTING;
      private static final float PHASE_2_THRESHOLD = 0.65F;
      private static final float PHASE_3_THRESHOLD = 0.3F;
      private int currentPhase = 1;
      private boolean phase2Announced = false;
      private boolean phase3Announced = false;
      private int meleeCooldown = 0;
      private int sandBulletCooldown = 0;
      private int sandWaveCooldown = 0;
      private int tailSwipeCooldown = 0;
      private int sandPrisonCooldown = 0;
      private int sandstormCooldown = 0;
      private int quicksandCooldown = 0;
      private int bijuBombCooldown = 0;
      private int globalCooldown = 0;
      private int bijuBombWindup = -1;
      private EntityLivingBase bijuBombTarget = null;
      private double bijuBombTX;
      private double bijuBombTY;
      private double bijuBombTZ;
      private int sandBulletWindup = -1;
      private double sandBulletTX;
      private double sandBulletTY;
      private double sandBulletTZ;
      private int sandPrisonWindup = -1;
      private double sandPrisonTX;
      private double sandPrisonTY;
      private double sandPrisonTZ;
      private double quicksandX;
      private double quicksandY;
      private double quicksandZ;
      private int quicksandTicks = 0;
      private boolean quicksandActive = false;
      private int targetSwitchTimer = 0;
      private boolean frenzyMode = false;
      private static final int SAND_BULLET_CD = 60;
      private static final int SAND_WAVE_CD = 100;
      private static final int TAIL_SWIPE_CD = 80;
      private static final int SAND_PRISON_CD = 200;
      private static final int SANDSTORM_CD = 240;
      private static final int QUICKSAND_CD = 300;
      private static final int BIJU_BOMB_CD = 400;
      private static final int BIJU_BOMB_WINDUP_TICKS = 40;
      private static final float SAND_BULLET_DAMAGE = 18.0F;
      private static final float SAND_WAVE_DAMAGE = 10.0F;
      private static final float TAIL_SWIPE_DAMAGE = 22.0F;
      private static final float SANDSTORM_DAMAGE = 10.0F;
      private static final float QUICKSAND_DAMAGE = 6.0F;
      private static final float BIJU_BOMB_DAMAGE = 60.0F;
      private static final float BIJU_BOMB_RADIUS = 14.0F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(4.0F, 6.0F);
         this.experienceValue = 500;
         this.isImmuneToFire = true;
         this.setNoAI(true);
         this.enablePersistence();
         this.stepHeight = 2.0F;
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(NPC_CONFIG_ID, "");
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
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)30000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4);
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)15.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)25.0F);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)64.0F);
      }

      public EnumCreatureAttribute getCreatureAttribute() {
         return EnumCreatureAttribute.UNDEFINED;
      }

      protected boolean canDespawn() {
         return false;
      }

      public void fall(float distance, float damageMultiplier) {
      }

      protected Item getDropItem() {
         return null;
      }

      public boolean isShooting() {
         return (Boolean)this.dataManager.get(SHOOTING);
      }

      public void setShooting(boolean shooting) {
         this.dataManager.set(SHOOTING, shooting);
      }

      public void applyNpcConfig(NpcConfig config) {
         if (config.getMaxHealth() > (double)0.0F) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(config.getMaxHealth());
            this.setHealth((float)config.getMaxHealth());
         }

         if (config.getAttackDamage() > (double)0.0F) {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(config.getAttackDamage());
         }

         if (config.getMovementSpeed() > (double)0.0F) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(config.getMovementSpeed());
         }

         if (config.getArmor() > (double)0.0F) {
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(config.getArmor());
         }

         this.setConfigId(config.getConfigId());
      }

      public String getNpcConfigId() {
         return (String)this.dataManager.get(NPC_CONFIG_ID);
      }

      public void setConfigId(String id) {
         this.dataManager.set(NPC_CONFIG_ID, id == null ? "" : id);
      }

      public SoundEvent getAmbientSound() {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation(""));
      }

      public SoundEvent getHurtSound(DamageSource ds) {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.hurt"));
      }

      public SoundEvent getDeathSound() {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.death"));
      }

      protected float getSoundVolume() {
         return 2.0F;
      }

      public boolean attackEntityAsMob(Entity target) {
         if (this.meleeCooldown > 0) {
            return false;
         } else {
            float damage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            if (this.frenzyMode) {
               damage *= 1.3F;
            }

            float normalDmg = damage * 0.7F;
            float trueDmg = damage * 0.3F;
            boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
            if (hit && target instanceof EntityLivingBase) {
               ((EntityLivingBase)target).hurtResistantTime = 0;
               target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            }

            if (hit) {
               this.meleeCooldown = 20;
            }

            return hit;
         }
      }

      public void onLivingUpdate() {
         super.onLivingUpdate();
         if (!this.world.isRemote) {
            if (this.ticksExisted % 10 == 0) {
               EntityLivingBase dome = this.findNearbyIceDome((double)20.0F);
               if (dome != null && dome.isEntityAlive()) {
                  this.setAttackTarget(dome);
                  if ((double)this.getDistance(dome) <= (double)4.0F) {
                     dome.hurtResistantTime = 0;
                     dome.attackEntityFrom(DamageSource.causeMobDamage(this), 200.0F);
                  }
               }
            }

            EntityLivingBase currentTarget = this.getAttackTarget();
            if (currentTarget == null || !currentTarget.isEntityAlive()) {
               List<EntityPlayer> nearby = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)64.0F), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator());
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

            if (this.sandBulletCooldown > 0) {
               --this.sandBulletCooldown;
            }

            if (this.sandWaveCooldown > 0) {
               --this.sandWaveCooldown;
            }

            if (this.tailSwipeCooldown > 0) {
               --this.tailSwipeCooldown;
            }

            if (this.sandPrisonCooldown > 0) {
               --this.sandPrisonCooldown;
            }

            if (this.sandstormCooldown > 0) {
               --this.sandstormCooldown;
            }

            if (this.quicksandCooldown > 0) {
               --this.quicksandCooldown;
            }

            if (this.bijuBombCooldown > 0) {
               --this.bijuBombCooldown;
            }

            if (this.globalCooldown > 0) {
               --this.globalCooldown;
            }

            if (++this.targetSwitchTimer >= 100 + this.rand.nextInt(60)) {
               this.targetSwitchTimer = 0;
               List<EntityPlayer> nearbyPlayers = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)64.0F), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator());
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

            this.updatePhase();
            if (this.sandBulletWindup > 0) {
               --this.sandBulletWindup;
               if (this.world instanceof WorldServer && this.sandBulletWindup % 3 == 0) {
                  int sandStateId = Block.getStateId(Blocks.SAND.getDefaultState());
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.sandBulletTX, this.sandBulletTY + (double)0.5F, this.sandBulletTZ, 10, (double)1.5F, (double)0.5F, (double)1.5F, 0.02, new int[]{sandStateId});
               }

               if (this.sandBulletWindup == 0) {
                  this.executeSandBulletImpact();
               }
            }

            if (this.sandPrisonWindup > 0) {
               --this.sandPrisonWindup;
               if (this.world instanceof WorldServer) {
                  int sandStateId = Block.getStateId(Blocks.SAND.getDefaultState());
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.sandPrisonTX, this.sandPrisonTY + 0.3, this.sandPrisonTZ, 8, (double)0.5F, 0.3, (double)0.5F, 0.01, new int[]{sandStateId});
               }

               if (this.sandPrisonWindup == 0) {
                  this.executeSandPrisonImpact();
               }
            }

            if (this.bijuBombWindup > 0) {
               --this.bijuBombWindup;
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  double mouthX = this.posX + this.getLookVec().x * (double)3.0F;
                  double mouthY = this.posY + (double)this.height * 0.7;
                  double mouthZ = this.posZ + this.getLookVec().z * (double)3.0F;
                  float progress = 1.0F - (float)this.bijuBombWindup / 40.0F;
                  double orbSize = (double)0.5F + (double)progress * (double)2.5F;
                  ws.spawnParticle(EnumParticleTypes.PORTAL, mouthX, mouthY, mouthZ, (int)(15.0F + progress * 30.0F), orbSize * (double)0.5F, orbSize * (double)0.5F, orbSize * (double)0.5F, 0.2, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, mouthX, mouthY, mouthZ, (int)(5.0F + progress * 15.0F), orbSize * 0.3, orbSize * 0.3, orbSize * 0.3, 0.02, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.REDSTONE, mouthX, mouthY, mouthZ, (int)(3.0F + progress * 10.0F), orbSize * 0.2, orbSize * 0.2, orbSize * 0.2, (double)0.0F, new int[0]);
                  double markerRadius = (double)(14.0F * progress);
                  int markerCount = (int)(10.0F + progress * 30.0F);
                  ws.spawnParticle(EnumParticleTypes.FLAME, this.bijuBombTX, this.bijuBombTY + 0.3, this.bijuBombTZ, markerCount, markerRadius * 0.4, 0.1, markerRadius * 0.4, 0.01, new int[0]);
               }

               if (this.bijuBombWindup == 0) {
                  this.executeBijuBomb();
               }

            } else {
               if (this.quicksandActive && this.quicksandTicks > 0) {
                  --this.quicksandTicks;
                  this.tickQuicksandZone();
                  if (this.quicksandTicks <= 0) {
                     this.quicksandActive = false;
                  }
               }

               EntityLivingBase target = this.getAttackTarget();
               if (target != null && target.isEntityAlive()) {
                  double dist = (double)this.getDistance(target);
                  if (dist > (double)4.0F && this.bijuBombWindup <= 0) {
                     double dx = target.posX - this.posX;
                     double dz = target.posZ - this.posZ;
                     double len = Math.sqrt(dx * dx + dz * dz);
                     double speed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
                     this.motionX = dx / len * speed;
                     this.motionZ = dz / len * speed;
                  } else if (dist <= (double)4.0F) {
                     this.motionX *= 0.3;
                     this.motionZ *= 0.3;
                  }

                  double faceDx = target.posX - this.posX;
                  double faceDz = target.posZ - this.posZ;
                  float targetYaw = (float)(Math.atan2(-faceDx, faceDz) * (180D / Math.PI));
                  this.rotationYaw = targetYaw;
                  this.renderYawOffset = targetYaw;
                  this.rotationYawHead = targetYaw;
                  if (this.globalCooldown <= 0) {
                     this.executeAttacks(target, dist);
                  }

               }
            }
         }
      }

      private void updatePhase() {
         float hpRatio = this.getHealth() / this.getMaxHealth();
         if (hpRatio <= 0.3F && this.currentPhase < 3) {
            this.currentPhase = 3;
            this.frenzyMode = true;
            if (!this.phase3Announced) {
               this.phase3Announced = true;
               this.announcePhase("§c§lShukaku enters a FRENZY!", "§6The One-Tail is enraged!");
            }
         } else if (hpRatio <= 0.65F && this.currentPhase < 2) {
            this.currentPhase = 2;
            if (!this.phase2Announced) {
               this.phase2Announced = true;
               this.announcePhase("§e§lShukaku grows more powerful!", "§6The desert trembles...");
            }
         }

      }

      private void announcePhase(String title, String subtitle) {
         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)64.0F))) {
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
         if (this.currentPhase >= 3 && this.bijuBombCooldown <= 0 && dist > (double)6.0F && dist < (double)40.0F) {
            this.startBijuBombWindup(target);
         } else if (this.currentPhase >= 2 && this.sandstormCooldown <= 0 && dist < (double)14.0F) {
            this.doSandstorm();
         } else if (this.currentPhase >= 2 && this.quicksandCooldown <= 0 && !this.quicksandActive && dist < (double)20.0F) {
            this.doQuicksandZone(target);
         } else if (this.sandPrisonCooldown <= 0 && dist < (double)8.0F) {
            this.doSandPrison(target);
         } else if (this.tailSwipeCooldown <= 0 && dist < (double)6.0F) {
            this.doTailSwipe();
         } else if (this.sandWaveCooldown <= 0 && dist < (double)10.0F) {
            this.doSandWave();
         } else if (this.sandBulletCooldown <= 0 && dist > (double)4.0F && dist < (double)30.0F) {
            this.doSandBullet(target);
         }
      }

      private void doSandBullet(EntityLivingBase target) {
         this.sandBulletCooldown = 60;
         this.globalCooldown = 20;
         this.sandBulletTX = target.posX;
         this.sandBulletTY = target.posY;
         this.sandBulletTZ = target.posZ;
         this.sandBulletWindup = this.frenzyMode ? 8 : 12;
         if (this.world instanceof WorldServer) {
            int sandStateId = Block.getStateId(Blocks.SAND.getDefaultState());
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + (double)this.height * 0.6, this.posZ, 20, (double)1.0F, (double)0.5F, (double)1.0F, 0.15, new int[]{sandStateId});
         }

      }

      private void executeSandBulletImpact() {
         float dmg = 18.0F;
         if (this.frenzyMode) {
            dmg *= 1.3F;
         }

         float normalDmgB = dmg * 0.65F;
         float trueDmgB = dmg * 0.35F;
         float hitRadius = 3.5F;

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.sandBulletTX - (double)hitRadius, this.sandBulletTY - (double)2.0F, this.sandBulletTZ - (double)hitRadius, this.sandBulletTX + (double)hitRadius, this.sandBulletTY + (double)3.0F, this.sandBulletTZ + (double)hitRadius))) {
            if (p.isEntityAlive() && !p.isCreative() && !p.isSpectator()) {
               p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmgB);
               p.hurtResistantTime = 0;
               p.attackEntityFrom(DamageSource.MAGIC, trueDmgB);
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            int sandStateId = Block.getStateId(Blocks.SAND.getDefaultState());
            ws.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.sandBulletTX, this.sandBulletTY + (double)0.5F, this.sandBulletTZ, 30, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[]{sandStateId});
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.sandBulletTX, this.sandBulletTY + (double)0.5F, this.sandBulletTZ, 5, 0.3, 0.3, 0.3, 0.02, new int[0]);
         }

      }

      private void doSandWave() {
         this.sandWaveCooldown = 100;
         this.globalCooldown = 20;
         float radius = 8.0F;
         float dmg = 10.0F;
         if (this.frenzyMode) {
            dmg *= 1.3F;
         }

         List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)radius), (ex) -> ex != this && ex instanceof EntityPlayer);
         float normalDmgW = dmg * 0.65F;
         float trueDmgW = dmg * 0.35F;

         for(EntityLivingBase e : targets) {
            e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmgW);
            e.hurtResistantTime = 0;
            e.attackEntityFrom(DamageSource.MAGIC, trueDmgW);
            double dx = e.posX - this.posX;
            double dz = e.posZ - this.posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0.01) {
               e.motionX += dx / len * 0.8;
               e.motionY += 0.4;
               e.motionZ += dz / len * 0.8;
               e.velocityChanged = true;
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            int sandStateId = Block.getStateId(Blocks.SAND.getDefaultState());

            for(int i = 0; i < 36; ++i) {
               double angle = Math.toRadians((double)(i * 10));
               double px = this.posX + Math.cos(angle) * (double)radius;
               double pz = this.posZ + Math.sin(angle) * (double)radius;
               ws.spawnParticle(EnumParticleTypes.BLOCK_CRACK, px, this.posY + (double)0.5F, pz, 8, 0.3, (double)0.5F, 0.3, 0.05, new int[]{sandStateId});
            }
         }

      }

      private void doTailSwipe() {
         this.tailSwipeCooldown = 80;
         this.globalCooldown = 15;
         float radius = 5.0F;
         float dmg = 22.0F;
         if (this.frenzyMode) {
            dmg *= 1.3F;
         }

         List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)radius), (ex) -> ex != this && ex instanceof EntityPlayer);
         float yawRad = (float)Math.toRadians((double)this.rotationYaw);

         for(EntityLivingBase e : targets) {
            double dx = e.posX - this.posX;
            double dz = e.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (!(dist > (double)radius)) {
               float angleTo = (float)Math.atan2(dz, dx);
               float diff = MathHelper.wrapDegrees((float)Math.toDegrees((double)(angleTo - yawRad)));
               if (Math.abs(diff) > 30.0F) {
                  float normalDmgT = dmg * 0.6F;
                  float trueDmgT = dmg * 0.4F;
                  e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmgT);
                  e.hurtResistantTime = 0;
                  e.attackEntityFrom(DamageSource.MAGIC, trueDmgT);
                  if (dist > 0.01) {
                     e.motionX += dx / dist * 1.2;
                     e.motionY += (double)0.5F;
                     e.motionZ += dz / dist * 1.2;
                     e.velocityChanged = true;
                  }
               }
            }
         }

         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.posX, this.posY + (double)1.5F, this.posZ, 15, (double)3.0F, (double)1.0F, (double)3.0F, (double)0.0F, new int[0]);
         }

      }

      private void doSandPrison(EntityLivingBase target) {
         this.sandPrisonCooldown = 200;
         this.globalCooldown = 15;
         this.sandPrisonTX = target.posX;
         this.sandPrisonTY = target.posY;
         this.sandPrisonTZ = target.posZ;
         this.sandPrisonWindup = this.frenzyMode ? 10 : 15;
         if (this.world instanceof WorldServer) {
            int sandStateId = Block.getStateId(Blocks.SAND.getDefaultState());
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.sandPrisonTX, this.sandPrisonTY + 0.1, this.sandPrisonTZ, 25, 0.6, 0.1, 0.6, 0.01, new int[]{sandStateId});
         }

      }

      private void executeSandPrisonImpact() {
         float hitRadius = 3.0F;
         List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.sandPrisonTX - (double)hitRadius, this.sandPrisonTY - (double)1.0F, this.sandPrisonTZ - (double)hitRadius, this.sandPrisonTX + (double)hitRadius, this.sandPrisonTY + (double)3.0F, this.sandPrisonTZ + (double)hitRadius));
         boolean hitAnyone = false;

         for(EntityPlayer p : players) {
            if (p.isEntityAlive() && !p.isCreative() && !p.isSpectator()) {
               p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 4, false, true));
               p.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 100, 2, false, true));
               hitAnyone = true;
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            int sandStateId = Block.getStateId(Blocks.SAND.getDefaultState());
            ws.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.sandPrisonTX, this.sandPrisonTY + (double)0.5F, this.sandPrisonTZ, hitAnyone ? 50 : 20, 0.8, (double)1.0F, 0.8, 0.02, new int[]{sandStateId});
         }

      }

      private void doSandstorm() {
         this.sandstormCooldown = 240;
         this.globalCooldown = 30;
         float radius = 12.0F;
         float dmg = 10.0F;
         if (this.frenzyMode) {
            dmg *= 1.3F;
         }

         List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)radius), (ex) -> ex != this && ex instanceof EntityPlayer);
         float normalDmgS = dmg * 0.6F;
         float trueDmgS = dmg * 0.4F;

         for(EntityLivingBase e : targets) {
            e.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmgS);
            e.hurtResistantTime = 0;
            e.attackEntityFrom(DamageSource.MAGIC, trueDmgS);
            e.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 80, 0, false, true));
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            int sandStateId = Block.getStateId(Blocks.SAND.getDefaultState());
            ws.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + (double)3.0F, this.posZ, 200, (double)radius * 0.7, (double)3.0F, (double)radius * 0.7, 0.1, new int[]{sandStateId});
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)2.0F, this.posZ, 40, (double)radius * (double)0.5F, (double)2.0F, (double)radius * (double)0.5F, 0.05, new int[0]);
         }

      }

      private void doQuicksandZone(EntityLivingBase target) {
         this.quicksandCooldown = 300;
         this.globalCooldown = 15;
         this.quicksandX = target.posX;
         this.quicksandY = target.posY;
         this.quicksandZ = target.posZ;
         this.quicksandTicks = 100;
         this.quicksandActive = true;
         if (this.world instanceof WorldServer) {
            int sandStateId = Block.getStateId(Blocks.SAND.getDefaultState());
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.quicksandX, this.quicksandY + 0.2, this.quicksandZ, 60, (double)3.0F, 0.2, (double)3.0F, 0.02, new int[]{sandStateId});
         }

      }

      private void tickQuicksandZone() {
         float radius = 5.0F;
         float dmg = 6.0F;
         if (this.frenzyMode) {
            dmg *= 1.3F;
         }

         AxisAlignedBB zone = new AxisAlignedBB(this.quicksandX - (double)radius, this.quicksandY - (double)1.0F, this.quicksandZ - (double)radius, this.quicksandX + (double)radius, this.quicksandY + (double)2.0F, this.quicksandZ + (double)radius);

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, zone)) {
            float normalDmgQ = dmg * 0.5F;
            float trueDmgQ = dmg * 0.5F;
            p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmgQ);
            p.hurtResistantTime = 0;
            p.attackEntityFrom(DamageSource.MAGIC, trueDmgQ);
            p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 30, 2, false, false));
            p.motionY -= 0.05;
            p.velocityChanged = true;
         }

         if (this.quicksandTicks % 5 == 0 && this.world instanceof WorldServer) {
            int sandStateId = Block.getStateId(Blocks.SAND.getDefaultState());
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.quicksandX, this.quicksandY + 0.2, this.quicksandZ, 20, (double)radius * 0.6, 0.1, (double)radius * 0.6, 0.01, new int[]{sandStateId});
         }

      }

      private void startBijuBombWindup(EntityLivingBase target) {
         this.bijuBombCooldown = 400;
         this.bijuBombWindup = 40;
         this.bijuBombTarget = target;
         this.bijuBombTX = target.posX;
         this.bijuBombTY = target.posY;
         this.bijuBombTZ = target.posZ;
         this.globalCooldown = 50;
         this.setShooting(true);
         SoundEvent chargeSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:bijudama"));
         if (chargeSound != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, chargeSound, SoundCategory.HOSTILE, 10.0F, 1.0F);
         }

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)40.0F))) {
            p.sendMessage(new TextComponentString("§c§lShukaku is charging a Biju Bomb! Get away!"));
         }

      }

      private void executeBijuBomb() {
         this.setShooting(false);
         if (this.bijuBombTarget != null && this.bijuBombTarget.isEntityAlive()) {
            double tx = this.bijuBombTX;
            double ty = this.bijuBombTY;
            double tz = this.bijuBombTZ;
            float dmg = 60.0F;
            if (this.frenzyMode) {
               dmg *= 1.3F;
            }

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(tx - (double)14.0F, ty - (double)14.0F, tz - (double)14.0F, tx + (double)14.0F, ty + (double)14.0F, tz + (double)14.0F))) {
               double d = p.getDistance(tx, ty, tz);
               if (d <= (double)14.0F) {
                  float falloff = 1.0F - (float)(d / (double)14.0F) * 0.5F;
                  float normalDmgBB = dmg * falloff * 0.5F;
                  float trueDmgBB = dmg * falloff * 0.5F;
                  p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmgBB);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, trueDmgBB);
                  double dx = p.posX - tx;
                  double dy = p.posY - ty;
                  double dz = p.posZ - tz;
                  double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                  if (len > 0.01) {
                     double kb = (double)2.0F * ((double)1.0F - d / (double)14.0F);
                     p.motionX += dx / len * kb;
                     p.motionY += 0.8 + dy / len * kb * (double)0.5F;
                     p.motionZ += dz / len * kb;
                     p.velocityChanged = true;
                  }
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, tx, ty + (double)2.0F, tz, 12, (double)4.0F, (double)4.0F, (double)4.0F, (double)0.0F, new int[0]);
               ws.spawnParticle(EnumParticleTypes.FLAME, tx, ty + (double)1.0F, tz, 150, (double)7.0F, (double)3.0F, (double)7.0F, 0.15, new int[0]);
               ws.spawnParticle(EnumParticleTypes.PORTAL, tx, ty + (double)2.0F, tz, 200, 5.6000000000000005, (double)2.0F, 5.6000000000000005, (double)1.0F, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, tx, ty + (double)4.0F, tz, 80, 4.2, (double)4.0F, 4.2, 0.1, new int[0]);
               int sandStateId = Block.getStateId(Blocks.SAND.getDefaultState());
               ws.spawnParticle(EnumParticleTypes.BLOCK_CRACK, tx, ty + (double)0.5F, tz, 150, (double)7.0F, (double)1.0F, (double)7.0F, 0.2, new int[]{sandStateId});
            }

            SoundEvent impactSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:nagiharai"));
            if (impactSound != null) {
               this.world.playSound((EntityPlayer)null, tx, ty, tz, impactSound, SoundCategory.HOSTILE, 10.0F, 1.0F);
            }

            this.world.playSound((EntityPlayer)null, tx, ty, tz, (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.explode")), SoundCategory.HOSTILE, 4.0F, 0.4F);
            this.bijuBombTarget = null;
         } else {
            this.bijuBombTarget = null;
         }
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

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("ShukakuPhase", this.currentPhase);
         compound.setBoolean("Phase2Announced", this.phase2Announced);
         compound.setBoolean("Phase3Announced", this.phase3Announced);
         compound.setBoolean("FrenzyMode", this.frenzyMode);
         compound.setInteger("SandBulletCD", this.sandBulletCooldown);
         compound.setInteger("SandWaveCD", this.sandWaveCooldown);
         compound.setInteger("TailSwipeCD", this.tailSwipeCooldown);
         compound.setInteger("SandPrisonCD", this.sandPrisonCooldown);
         compound.setInteger("SandstormCD", this.sandstormCooldown);
         compound.setInteger("QuicksandCD", this.quicksandCooldown);
         compound.setInteger("BijuBombCD", this.bijuBombCooldown);
         compound.setString("NpcConfigId", this.getNpcConfigId());
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.currentPhase = compound.getInteger("ShukakuPhase");
         if (this.currentPhase < 1) {
            this.currentPhase = 1;
         }

         this.phase2Announced = compound.getBoolean("Phase2Announced");
         this.phase3Announced = compound.getBoolean("Phase3Announced");
         this.frenzyMode = compound.getBoolean("FrenzyMode");
         this.sandBulletCooldown = compound.getInteger("SandBulletCD");
         this.sandWaveCooldown = compound.getInteger("SandWaveCD");
         this.tailSwipeCooldown = compound.getInteger("TailSwipeCD");
         this.sandPrisonCooldown = compound.getInteger("SandPrisonCD");
         this.sandstormCooldown = compound.getInteger("SandstormCD");
         this.quicksandCooldown = compound.getInteger("QuicksandCD");
         this.bijuBombCooldown = compound.getInteger("BijuBombCD");
         if (compound.hasKey("NpcConfigId")) {
            this.setConfigId(compound.getString("NpcConfigId"));
         }

      }

      static {
         NPC_CONFIG_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
         SHOOTING = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
      }
   }
}
