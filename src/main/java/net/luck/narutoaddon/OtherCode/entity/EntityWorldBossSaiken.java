
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
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
public class EntityWorldBossSaiken extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 401;

   public EntityWorldBossSaiken(ElementsInfTsukAddon instance) {
      super(instance, 981);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "worldbosssaiken"), 401).name("worldbosssaiken").tracker(96, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SaikenRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class SaikenRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod", "textures/sixtails.png");

      public SaikenRenderer(RenderManager renderManager) {
         super(renderManager, new ModelSaiken(), 11.0F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         float s = 0.3181818F;
         GlStateManager.scale(s, s, s);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelSaiken extends ModelBiped {
      private final ModelRenderer cube_r1;
      private final ModelRenderer cube_r2;
      private final ModelRenderer cube_r3;
      private final ModelRenderer[][] horn = new ModelRenderer[2][5];
      private final float[][] hornSwayX = new float[2][5];
      private final float[][] hornSwayZ = new float[2][5];
      private final ModelRenderer cube_r4;
      private final ModelRenderer cube_r5;
      private final ModelRenderer cube_r6;
      private final ModelRenderer bone;
      private final ModelRenderer cube_r7;
      private final ModelRenderer cube_r8;
      private final ModelRenderer cube_r9;
      private final ModelRenderer bone5;
      private final ModelRenderer cube_r10;
      private final ModelRenderer cube_r11;
      private final ModelRenderer cube_r12;
      private final ModelRenderer cube_r13;
      private final ModelRenderer cube_r14;
      private final ModelRenderer rightLeg1;
      private final ModelRenderer cube_r15;
      private final ModelRenderer cube_r16;
      private final ModelRenderer cube_r17;
      private final ModelRenderer cube_r18;
      private final ModelRenderer leftLeg1;
      private final ModelRenderer cube_r19;
      private final ModelRenderer cube_r20;
      private final ModelRenderer tails;
      private final ModelRenderer[][] tail = new ModelRenderer[6][6];
      private final float[][] tailSwayX = new float[6][6];
      private final float[][] tailSwayY = new float[6][6];
      private final float[][] tailSwayZ = new float[6][6];
      private final Random rand = new Random();

      public ModelSaiken() {
         super(0.0F);
         this.textureWidth = 64;
         this.textureHeight = 64;
         this.bipedHeadwear.showModel = false;
         (this.bipedBody = new ModelRenderer(this)).setRotationPoint(0.0F, 22.75F, 3.0F);
         this.bipedBody.cubeList.add(new ModelBox(this.bipedBody, 42, 0, -3.0F, -6.7456F, -4.9001F, 6, 4, 5, 0.0F, false));
         (this.cube_r1 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.25F, -2.4F);
         this.bipedBody.addChild(this.cube_r1);
         this.setRotationAngle(this.cube_r1, -0.0873F, 0.0F, 0.0F);
         this.cube_r1.cubeList.add(new ModelBox(this.cube_r1, 0, 0, -3.5F, -2.5F, -3.6F, 7, 5, 6, 0.0F, false));
         (this.cube_r2 = new ModelRenderer(this)).setRotationPoint(0.0F, -5.2456F, -2.9501F);
         this.bipedBody.addChild(this.cube_r2);
         this.setRotationAngle(this.cube_r2, -0.2618F, 0.0F, 0.0F);
         this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 17, 16, -3.0F, -2.0F, -2.5F, 6, 4, 5, -0.1F, false));
         (this.cube_r3 = new ModelRenderer(this)).setRotationPoint(0.0F, -6.7456F, -2.4001F);
         this.bipedBody.addChild(this.cube_r3);
         this.setRotationAngle(this.cube_r3, 0.1309F, 0.0F, 0.0F);
         this.cube_r3.cubeList.add(new ModelBox(this.cube_r3, 0, 11, -3.0F, -3.6F, -2.5F, 6, 5, 5, -0.2F, false));
         (this.bipedHead = new ModelRenderer(this)).setRotationPoint(0.0F, -8.75F, -2.0F);
         this.bipedBody.addChild(this.bipedHead);
         this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 0, 22, -3.0F, -4.8F, -2.1F, 6, 5, 3, 0.0F, false));
         this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 20, 0, -3.0F, -4.8F, -3.1F, 6, 5, 1, 0.0F, false));
         this.bipedHead.cubeList.add(new ModelBox(this.bipedHead, 17, 11, -3.0F, -3.4F, -4.6F, 6, 3, 2, 0.0F, false));
         (this.cube_r4 = new ModelRenderer(this)).setRotationPoint(0.0F, -3.8499F, -3.4747F);
         this.bipedHead.addChild(this.cube_r4);
         this.setRotationAngle(this.cube_r4, -0.829F, 0.0F, 0.0F);
         this.cube_r4.cubeList.add(new ModelBox(this.cube_r4, 31, 9, -3.0F, -0.9F, -0.4F, 6, 2, 1, 0.0F, false));
         (this.cube_r5 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.4907F, -3.3866F);
         this.bipedHead.addChild(this.cube_r5);
         this.setRotationAngle(this.cube_r5, 1.0472F, 0.0F, 0.0F);
         this.cube_r5.cubeList.add(new ModelBox(this.cube_r5, 26, 6, -3.0F, -1.0F, -0.7F, 6, 2, 1, 0.0F, false));
         (this.cube_r6 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.0169F, 0.9617F);
         this.bipedHead.addChild(this.cube_r6);
         this.setRotationAngle(this.cube_r6, 0.1745F, 0.0F, 0.0F);
         this.cube_r6.cubeList.add(new ModelBox(this.cube_r6, 18, 25, -3.0F, -3.7F, -0.3F, 6, 5, 1, 0.0F, false));
         (this.horn[0][0] = new ModelRenderer(this)).setRotationPoint(-2.5F, -4.75F, -2.5F);
         this.bipedHead.addChild(this.horn[0][0]);
         this.setRotationAngle(this.horn[0][0], 0.0F, 0.0F, -0.4363F);
         this.horn[0][0].cubeList.add(new ModelBox(this.horn[0][0], 0, 0, 0.0F, -0.45F, -0.5F, 1, 1, 1, 0.0F, false));
         (this.horn[0][1] = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, 0.0F);
         this.horn[0][0].addChild(this.horn[0][1]);
         this.setRotationAngle(this.horn[0][1], 0.0F, 0.0F, 0.0873F);
         this.horn[0][1].cubeList.add(new ModelBox(this.horn[0][1], 0, 0, 0.0F, -0.75F, -0.5F, 1, 1, 1, -0.05F, false));
         (this.horn[0][2] = new ModelRenderer(this)).setRotationPoint(0.0F, -0.5F, 0.0F);
         this.horn[0][1].addChild(this.horn[0][2]);
         this.setRotationAngle(this.horn[0][2], 0.0873F, 0.0F, 0.0F);
         this.horn[0][2].cubeList.add(new ModelBox(this.horn[0][2], 0, 0, 0.0F, -0.75F, -0.5F, 1, 1, 1, -0.1F, false));
         (this.horn[0][3] = new ModelRenderer(this)).setRotationPoint(0.0F, -0.5F, 0.0F);
         this.horn[0][2].addChild(this.horn[0][3]);
         this.setRotationAngle(this.horn[0][3], 0.0873F, 0.0F, 0.0F);
         this.horn[0][3].cubeList.add(new ModelBox(this.horn[0][3], 0, 0, 0.0F, -0.75F, -0.5F, 1, 1, 1, -0.15F, false));
         (this.horn[0][4] = new ModelRenderer(this)).setRotationPoint(0.0F, -0.6499F, 0.0253F);
         this.horn[0][3].addChild(this.horn[0][4]);
         this.setRotationAngle(this.horn[0][4], 0.0F, 0.0F, -0.0873F);
         this.horn[0][4].cubeList.add(new ModelBox(this.horn[0][4], 0, 2, 0.0F, -0.75F, -0.5F, 1, 1, 1, -0.05F, false));
         (this.horn[1][0] = new ModelRenderer(this)).setRotationPoint(2.5F, -4.75F, -2.5F);
         this.bipedHead.addChild(this.horn[1][0]);
         this.setRotationAngle(this.horn[1][0], 0.0F, 0.0F, 0.4363F);
         this.horn[1][0].cubeList.add(new ModelBox(this.horn[1][0], 0, 0, -1.0F, -0.45F, -0.5F, 1, 1, 1, 0.0F, true));
         (this.horn[1][1] = new ModelRenderer(this)).setRotationPoint(0.0F, -0.25F, 0.0F);
         this.horn[1][0].addChild(this.horn[1][1]);
         this.setRotationAngle(this.horn[1][1], 0.0F, 0.0F, -0.0873F);
         this.horn[1][1].cubeList.add(new ModelBox(this.horn[1][1], 0, 0, -1.0F, -0.75F, -0.5F, 1, 1, 1, -0.05F, true));
         (this.horn[1][2] = new ModelRenderer(this)).setRotationPoint(0.0F, -0.5F, 0.0F);
         this.horn[1][1].addChild(this.horn[1][2]);
         this.setRotationAngle(this.horn[1][2], -0.0873F, 0.0F, 0.0F);
         this.horn[1][2].cubeList.add(new ModelBox(this.horn[1][2], 0, 0, -1.0F, -0.75F, -0.5F, 1, 1, 1, -0.1F, true));
         (this.horn[1][3] = new ModelRenderer(this)).setRotationPoint(0.0F, -0.5F, 0.0F);
         this.horn[1][2].addChild(this.horn[1][3]);
         this.setRotationAngle(this.horn[1][3], -0.0873F, 0.0F, 0.0F);
         this.horn[1][3].cubeList.add(new ModelBox(this.horn[1][3], 0, 0, -1.0F, -0.75F, -0.5F, 1, 1, 1, -0.15F, true));
         (this.horn[1][4] = new ModelRenderer(this)).setRotationPoint(0.0F, -0.6499F, 0.0253F);
         this.horn[1][3].addChild(this.horn[1][4]);
         this.setRotationAngle(this.horn[1][4], 0.0F, 0.0F, 0.0873F);
         this.horn[1][4].cubeList.add(new ModelBox(this.horn[1][4], 0, 2, -1.0F, -0.75F, -0.5F, 1, 1, 1, -0.05F, true));
         (this.bipedRightArm = new ModelRenderer(this)).setRotationPoint(-3.0F, -6.0F, -1.5F);
         this.bipedBody.addChild(this.bipedRightArm);
         (this.bone = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedRightArm.addChild(this.bone);
         this.setRotationAngle(this.bone, 0.0F, 0.3491F, 0.0F);
         (this.cube_r7 = new ModelRenderer(this)).setRotationPoint(-0.287F, 0.2867F, -1.7153F);
         this.bone.addChild(this.cube_r7);
         this.setRotationAngle(this.cube_r7, 0.2618F, -0.5672F, 0.0F);
         this.cube_r7.cubeList.add(new ModelBox(this.cube_r7, 12, 31, -1.0F, -0.9F, -1.5F, 2, 2, 3, -0.4F, false));
         (this.cube_r8 = new ModelRenderer(this)).setRotationPoint(-0.9F, 0.25F, -0.3F);
         this.bone.addChild(this.cube_r8);
         this.setRotationAngle(this.cube_r8, 0.1309F, 0.0F, 0.0F);
         this.cube_r8.cubeList.add(new ModelBox(this.cube_r8, 30, 35, -0.8F, -1.2F, -1.2F, 2, 2, 2, -0.4F, false));
         (this.cube_r9 = new ModelRenderer(this)).setRotationPoint(-0.5869F, -0.0397F, 0.43F);
         this.bone.addChild(this.cube_r9);
         this.setRotationAngle(this.cube_r9, 0.1309F, -0.5672F, -0.0873F);
         this.cube_r9.cubeList.add(new ModelBox(this.cube_r9, 34, 16, -1.1F, -1.0F, -1.5F, 2, 2, 2, -0.4F, false));
         (this.bipedLeftArm = new ModelRenderer(this)).setRotationPoint(3.0F, -6.0F, -1.5F);
         this.bipedBody.addChild(this.bipedLeftArm);
         (this.bone5 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bipedLeftArm.addChild(this.bone5);
         this.setRotationAngle(this.bone5, 0.0F, -0.3491F, 0.0F);
         (this.cube_r10 = new ModelRenderer(this)).setRotationPoint(0.287F, 0.2867F, -1.7153F);
         this.bone5.addChild(this.cube_r10);
         this.setRotationAngle(this.cube_r10, 0.2618F, 0.5672F, 0.0F);
         this.cube_r10.cubeList.add(new ModelBox(this.cube_r10, 12, 31, -1.0F, -0.9F, -1.5F, 2, 2, 3, -0.4F, true));
         (this.cube_r11 = new ModelRenderer(this)).setRotationPoint(0.9F, 0.25F, -0.3F);
         this.bone5.addChild(this.cube_r11);
         this.setRotationAngle(this.cube_r11, 0.1309F, 0.0F, 0.0F);
         this.cube_r11.cubeList.add(new ModelBox(this.cube_r11, 30, 35, -1.2F, -1.2F, -1.2F, 2, 2, 2, -0.4F, true));
         (this.cube_r12 = new ModelRenderer(this)).setRotationPoint(0.5869F, -0.0397F, 0.43F);
         this.bone5.addChild(this.cube_r12);
         this.setRotationAngle(this.cube_r12, 0.1309F, 0.5672F, 0.0873F);
         this.cube_r12.cubeList.add(new ModelBox(this.cube_r12, 34, 16, -0.9F, -1.0F, -1.5F, 2, 2, 2, -0.4F, true));
         (this.bipedRightLeg = new ModelRenderer(this)).setRotationPoint(-2.75F, -3.75F, -3.0F);
         this.bipedBody.addChild(this.bipedRightLeg);
         (this.cube_r13 = new ModelRenderer(this)).setRotationPoint(-3.0464F, 4.5F, -0.8128F);
         this.bipedRightLeg.addChild(this.cube_r13);
         this.setRotationAngle(this.cube_r13, 0.0F, -0.2618F, 0.0F);
         this.cube_r13.cubeList.add(new ModelBox(this.cube_r13, 22, 32, -1.1F, -0.5F, -1.5372F, 2, 1, 3, -0.1F, false));
         (this.cube_r14 = new ModelRenderer(this)).setRotationPoint(-3.1083F, 4.151F, -0.8684F);
         this.bipedRightLeg.addChild(this.cube_r14);
         this.setRotationAngle(this.cube_r14, -0.1745F, -0.1745F, 0.7418F);
         this.cube_r14.cubeList.add(new ModelBox(this.cube_r14, 33, 12, -0.5F, -0.5F, -1.5F, 1, 1, 3, -0.1F, false));
         (this.rightLeg1 = new ModelRenderer(this)).setRotationPoint(0.5F, 3.6667F, 2.5333F);
         this.bipedRightLeg.addChild(this.rightLeg1);
         this.setRotationAngle(this.rightLeg1, 0.48F, -0.2618F, 0.0436F);
         (this.cube_r15 = new ModelRenderer(this)).setRotationPoint(-2.7866F, -3.3303F, -0.9362F);
         this.rightLeg1.addChild(this.cube_r15);
         this.setRotationAngle(this.cube_r15, -0.3927F, 0.3491F, 0.6545F);
         this.cube_r15.cubeList.add(new ModelBox(this.cube_r15, 29, 28, -1.0F, -2.5F, -1.4F, 3, 4, 3, -0.1F, false));
         (this.cube_r16 = new ModelRenderer(this)).setRotationPoint(-3.0F, -1.2667F, -1.4333F);
         this.rightLeg1.addChild(this.cube_r16);
         this.setRotationAngle(this.cube_r16, -0.5236F, 0.0F, 0.0F);
         this.cube_r16.cubeList.add(new ModelBox(this.cube_r16, 0, 30, -1.5F, -1.7F, -2.0F, 3, 3, 3, -0.1F, false));
         (this.bipedLeftLeg = new ModelRenderer(this)).setRotationPoint(2.75F, -3.75F, -3.0F);
         this.bipedBody.addChild(this.bipedLeftLeg);
         (this.cube_r17 = new ModelRenderer(this)).setRotationPoint(3.0464F, 4.5F, -0.8128F);
         this.bipedLeftLeg.addChild(this.cube_r17);
         this.setRotationAngle(this.cube_r17, 0.0F, 0.2618F, 0.0F);
         this.cube_r17.cubeList.add(new ModelBox(this.cube_r17, 22, 32, -0.9F, -0.5F, -1.5372F, 2, 1, 3, -0.1F, true));
         (this.cube_r18 = new ModelRenderer(this)).setRotationPoint(3.1083F, 4.151F, -0.8684F);
         this.bipedLeftLeg.addChild(this.cube_r18);
         this.setRotationAngle(this.cube_r18, -0.1745F, 0.1745F, -0.7418F);
         this.cube_r18.cubeList.add(new ModelBox(this.cube_r18, 33, 12, -0.5F, -0.5F, -1.5F, 1, 1, 3, -0.1F, true));
         (this.leftLeg1 = new ModelRenderer(this)).setRotationPoint(-0.5F, 3.6667F, 2.5333F);
         this.bipedLeftLeg.addChild(this.leftLeg1);
         this.setRotationAngle(this.leftLeg1, 0.48F, 0.2618F, -0.0436F);
         (this.cube_r19 = new ModelRenderer(this)).setRotationPoint(2.7866F, -3.3303F, -0.9362F);
         this.leftLeg1.addChild(this.cube_r19);
         this.setRotationAngle(this.cube_r19, -0.3927F, -0.3491F, -0.6545F);
         this.cube_r19.cubeList.add(new ModelBox(this.cube_r19, 29, 28, -2.0F, -2.5F, -1.4F, 3, 4, 3, -0.1F, true));
         (this.cube_r20 = new ModelRenderer(this)).setRotationPoint(3.0F, -1.2667F, -1.4333F);
         this.leftLeg1.addChild(this.cube_r20);
         this.setRotationAngle(this.cube_r20, -0.5236F, 0.0F, 0.0F);
         this.cube_r20.cubeList.add(new ModelBox(this.cube_r20, 0, 30, -1.5F, -1.7F, -2.0F, 3, 3, 3, -0.1F, true));
         (this.tails = new ModelRenderer(this)).setRotationPoint(0.0F, 22.75F, 3.0F);
         (this.tail[0][0] = new ModelRenderer(this)).setRotationPoint(1.25F, 0.0F, 0.0F);
         this.tails.addChild(this.tail[0][0]);
         this.setRotationAngle(this.tail[0][0], -1.2217F, 1.309F, 0.0F);
         this.tail[0][0].cubeList.add(new ModelBox(this.tail[0][0], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.2F, false));
         (this.tail[0][1] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[0][0].addChild(this.tail[0][1]);
         this.setRotationAngle(this.tail[0][1], 0.2618F, 0.0F, 0.0F);
         this.tail[0][1].cubeList.add(new ModelBox(this.tail[0][1], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.15F, false));
         (this.tail[0][2] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[0][1].addChild(this.tail[0][2]);
         this.setRotationAngle(this.tail[0][2], 0.2618F, 0.0F, 0.0F);
         this.tail[0][2].cubeList.add(new ModelBox(this.tail[0][2], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.1F, false));
         (this.tail[0][3] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[0][2].addChild(this.tail[0][3]);
         this.setRotationAngle(this.tail[0][3], 0.2618F, 0.0F, 0.0F);
         this.tail[0][3].cubeList.add(new ModelBox(this.tail[0][3], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.05F, false));
         (this.tail[0][4] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[0][3].addChild(this.tail[0][4]);
         this.setRotationAngle(this.tail[0][4], 0.2618F, 0.0F, 0.0F);
         this.tail[0][4].cubeList.add(new ModelBox(this.tail[0][4], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.tail[0][5] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[0][4].addChild(this.tail[0][5]);
         this.setRotationAngle(this.tail[0][5], 0.2618F, 0.0F, 0.0F);
         this.tail[0][5].cubeList.add(new ModelBox(this.tail[0][5], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.tail[1][0] = new ModelRenderer(this)).setRotationPoint(0.75F, 0.0F, 0.0F);
         this.tails.addChild(this.tail[1][0]);
         this.setRotationAngle(this.tail[1][0], -0.7854F, 0.7854F, 0.0F);
         this.tail[1][0].cubeList.add(new ModelBox(this.tail[1][0], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.2F, false));
         (this.tail[1][1] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[1][0].addChild(this.tail[1][1]);
         this.setRotationAngle(this.tail[1][1], 0.2618F, 0.0F, 0.0F);
         this.tail[1][1].cubeList.add(new ModelBox(this.tail[1][1], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.15F, false));
         (this.tail[1][2] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[1][1].addChild(this.tail[1][2]);
         this.setRotationAngle(this.tail[1][2], 0.2618F, 0.0F, 0.0F);
         this.tail[1][2].cubeList.add(new ModelBox(this.tail[1][2], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.1F, false));
         (this.tail[1][3] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[1][2].addChild(this.tail[1][3]);
         this.setRotationAngle(this.tail[1][3], 0.2618F, 0.0F, 0.0F);
         this.tail[1][3].cubeList.add(new ModelBox(this.tail[1][3], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.05F, false));
         (this.tail[1][4] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[1][3].addChild(this.tail[1][4]);
         this.setRotationAngle(this.tail[1][4], -0.2618F, 0.0F, 0.0F);
         this.tail[1][4].cubeList.add(new ModelBox(this.tail[1][4], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.tail[1][5] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[1][4].addChild(this.tail[1][5]);
         this.setRotationAngle(this.tail[1][5], -0.2618F, 0.0F, 0.0F);
         this.tail[1][5].cubeList.add(new ModelBox(this.tail[1][5], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.tail[2][0] = new ModelRenderer(this)).setRotationPoint(0.25F, 0.0F, 0.0F);
         this.tails.addChild(this.tail[2][0]);
         this.setRotationAngle(this.tail[2][0], -1.0472F, 0.2618F, 0.0F);
         this.tail[2][0].cubeList.add(new ModelBox(this.tail[2][0], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.2F, false));
         (this.tail[2][1] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[2][0].addChild(this.tail[2][1]);
         this.setRotationAngle(this.tail[2][1], 0.2618F, 0.0F, 0.0F);
         this.tail[2][1].cubeList.add(new ModelBox(this.tail[2][1], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.15F, false));
         (this.tail[2][2] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[2][1].addChild(this.tail[2][2]);
         this.setRotationAngle(this.tail[2][2], 0.2618F, 0.0F, 0.0F);
         this.tail[2][2].cubeList.add(new ModelBox(this.tail[2][2], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.1F, false));
         (this.tail[2][3] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[2][2].addChild(this.tail[2][3]);
         this.setRotationAngle(this.tail[2][3], 0.2618F, 0.0F, 0.0F);
         this.tail[2][3].cubeList.add(new ModelBox(this.tail[2][3], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.05F, false));
         (this.tail[2][4] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[2][3].addChild(this.tail[2][4]);
         this.setRotationAngle(this.tail[2][4], 0.2618F, 0.0F, 0.0F);
         this.tail[2][4].cubeList.add(new ModelBox(this.tail[2][4], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.tail[2][5] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[2][4].addChild(this.tail[2][5]);
         this.setRotationAngle(this.tail[2][5], -0.2618F, 0.0F, 0.0F);
         this.tail[2][5].cubeList.add(new ModelBox(this.tail[2][5], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.tail[3][0] = new ModelRenderer(this)).setRotationPoint(-0.25F, 0.0F, 0.0F);
         this.tails.addChild(this.tail[3][0]);
         this.setRotationAngle(this.tail[3][0], -0.7854F, -0.2618F, 0.0F);
         this.tail[3][0].cubeList.add(new ModelBox(this.tail[3][0], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.2F, false));
         (this.tail[3][1] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[3][0].addChild(this.tail[3][1]);
         this.setRotationAngle(this.tail[3][1], -0.2618F, 0.0F, 0.0F);
         this.tail[3][1].cubeList.add(new ModelBox(this.tail[3][1], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.15F, false));
         (this.tail[3][2] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[3][1].addChild(this.tail[3][2]);
         this.setRotationAngle(this.tail[3][2], 0.2618F, 0.0F, 0.0F);
         this.tail[3][2].cubeList.add(new ModelBox(this.tail[3][2], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.1F, false));
         (this.tail[3][3] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[3][2].addChild(this.tail[3][3]);
         this.setRotationAngle(this.tail[3][3], 0.2618F, 0.0F, 0.0F);
         this.tail[3][3].cubeList.add(new ModelBox(this.tail[3][3], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.05F, false));
         (this.tail[3][4] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[3][3].addChild(this.tail[3][4]);
         this.setRotationAngle(this.tail[3][4], 0.2618F, 0.0F, 0.0F);
         this.tail[3][4].cubeList.add(new ModelBox(this.tail[3][4], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.tail[3][5] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[3][4].addChild(this.tail[3][5]);
         this.setRotationAngle(this.tail[3][5], -0.2618F, 0.0F, 0.0F);
         this.tail[3][5].cubeList.add(new ModelBox(this.tail[3][5], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.tail[4][0] = new ModelRenderer(this)).setRotationPoint(-0.75F, 0.0F, 0.0F);
         this.tails.addChild(this.tail[4][0]);
         this.setRotationAngle(this.tail[4][0], -0.8727F, -0.7854F, 0.0F);
         this.tail[4][0].cubeList.add(new ModelBox(this.tail[4][0], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.2F, false));
         (this.tail[4][1] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[4][0].addChild(this.tail[4][1]);
         this.setRotationAngle(this.tail[4][1], 0.2618F, 0.0F, 0.0F);
         this.tail[4][1].cubeList.add(new ModelBox(this.tail[4][1], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.15F, false));
         (this.tail[4][2] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[4][1].addChild(this.tail[4][2]);
         this.setRotationAngle(this.tail[4][2], 0.2618F, 0.0F, 0.0F);
         this.tail[4][2].cubeList.add(new ModelBox(this.tail[4][2], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.1F, false));
         (this.tail[4][3] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[4][2].addChild(this.tail[4][3]);
         this.setRotationAngle(this.tail[4][3], -0.2618F, 0.0F, 0.0F);
         this.tail[4][3].cubeList.add(new ModelBox(this.tail[4][3], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.05F, false));
         (this.tail[4][4] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[4][3].addChild(this.tail[4][4]);
         this.setRotationAngle(this.tail[4][4], -0.2618F, 0.0F, 0.0F);
         this.tail[4][4].cubeList.add(new ModelBox(this.tail[4][4], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.tail[4][5] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[4][4].addChild(this.tail[4][5]);
         this.setRotationAngle(this.tail[4][5], -0.2618F, 0.0F, 0.0F);
         this.tail[4][5].cubeList.add(new ModelBox(this.tail[4][5], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.tail[5][0] = new ModelRenderer(this)).setRotationPoint(-1.25F, 0.0F, 0.0F);
         this.tails.addChild(this.tail[5][0]);
         this.setRotationAngle(this.tail[5][0], -1.2217F, -1.309F, 0.0F);
         this.tail[5][0].cubeList.add(new ModelBox(this.tail[5][0], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.2F, false));
         (this.tail[5][1] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[5][0].addChild(this.tail[5][1]);
         this.setRotationAngle(this.tail[5][1], 0.2618F, 0.0F, 0.0F);
         this.tail[5][1].cubeList.add(new ModelBox(this.tail[5][1], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.15F, false));
         (this.tail[5][2] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[5][1].addChild(this.tail[5][2]);
         this.setRotationAngle(this.tail[5][2], 0.2618F, 0.0F, 0.0F);
         this.tail[5][2].cubeList.add(new ModelBox(this.tail[5][2], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.1F, false));
         (this.tail[5][3] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[5][2].addChild(this.tail[5][3]);
         this.setRotationAngle(this.tail[5][3], 0.2618F, 0.0F, 0.0F);
         this.tail[5][3].cubeList.add(new ModelBox(this.tail[5][3], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.05F, false));
         (this.tail[5][4] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[5][3].addChild(this.tail[5][4]);
         this.setRotationAngle(this.tail[5][4], -0.2618F, 0.0F, 0.0F);
         this.tail[5][4].cubeList.add(new ModelBox(this.tail[5][4], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.0F, false));
         (this.tail[5][5] = new ModelRenderer(this)).setRotationPoint(0.0F, -3.0F, 0.0F);
         this.tail[5][4].addChild(this.tail[5][5]);
         this.setRotationAngle(this.tail[5][5], -0.2618F, 0.0F, 0.0F);
         this.tail[5][5].cubeList.add(new ModelBox(this.tail[5][5], 34, 0, -1.0F, -3.5F, -1.0F, 2, 4, 2, 0.0F, false));

         for(int i = 0; i < 2; ++i) {
            for(int j = 1; j < 5; ++j) {
               this.hornSwayX[i][j] = (this.rand.nextFloat() * 0.1745F + 0.0873F) * (this.rand.nextBoolean() ? -1.0F : 1.0F);
               this.hornSwayZ[i][j] = (this.rand.nextFloat() * 0.1745F + 0.0873F) * (this.rand.nextBoolean() ? -1.0F : 1.0F);
            }
         }

         for(int i = 0; i < 6; ++i) {
            for(int j = 1; j < 6; ++j) {
               this.tailSwayX[i][j] = (this.rand.nextFloat() * 0.1745F + 0.1745F) * (this.rand.nextBoolean() ? -1.0F : 1.0F);
               this.tailSwayZ[i][j] = (this.rand.nextFloat() * 0.1745F + 0.1745F) * (this.rand.nextBoolean() ? -1.0F : 1.0F);
               this.tailSwayY[i][j] = this.rand.nextFloat() * 0.1745F + 0.1745F;
            }
         }

      }

      public void render(Entity entity, float f0, float f1, float f2, float f3, float f4, float f5) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, -31.5F * f5, 0.0F);
         GlStateManager.scale(22.0F, 22.0F, 22.0F);
         this.bipedBody.render(f5);
         this.tails.render(f5);
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
         var10000.rotationPointY -= 8.75F;
         this.bipedRightArm.setRotationPoint(-3.0F, -6.0F, -1.5F);
         this.bipedLeftArm.setRotationPoint(3.0F, -6.0F, -1.5F);
         this.bipedRightLeg.setRotationPoint(-2.75F, -3.75F, -3.0F);
         this.bipedLeftLeg.setRotationPoint(2.75F, -3.75F, -3.0F);

         for(int i = 0; i < 2; ++i) {
            for(int j = 1; j < 5; ++j) {
               this.horn[i][j].rotateAngleX = -0.1745F + MathHelper.sin(f2 * 0.1F) * this.hornSwayX[i][j];
               this.horn[i][j].rotateAngleZ = MathHelper.cos(f2 * 0.1F) * this.hornSwayZ[i][j];
            }
         }

         for(int i = 0; i < 6; ++i) {
            for(int j = 1; j < 6; ++j) {
               this.tail[i][j].rotateAngleX = 0.2618F + MathHelper.sin((f2 - (float)j) * 0.05F) * this.tailSwayX[i][j];
               this.tail[i][j].rotateAngleZ = MathHelper.cos((f2 - (float)j) * 0.05F) * this.tailSwayZ[i][j];
               this.tail[i][j].rotateAngleY = MathHelper.sin((f2 - (float)j) * 0.05F) * this.tailSwayY[i][j];
            }
         }

         this.bipedBody.rotationPointY = 3.0F;
         this.bipedBody.rotateAngleX = 0.0F;
         this.tails.rotationPointY = 3.0F;
         this.tails.rotateAngleX = 0.0F;
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
      private int acidSpitCooldown = 0;
      private int tailSlamCooldown = 0;
      private int acidRainCooldown = 0;
      private int corrosivePoolCooldown = 0;
      private int toxicGasCooldown = 0;
      private int bijuBombCooldown = 0;
      private int acidLeapCooldown = 0;
      private int slugSlideCooldown = 0;
      private int globalCooldown = 0;
      private int antiYCheeseCooldown = 0;
      private int bijuBombWindup = -1;
      private double bijuBombTX;
      private double bijuBombTY;
      private double bijuBombTZ;
      private double acidRainX;
      private double acidRainY;
      private double acidRainZ;
      private int acidRainTicks = 0;
      private boolean acidRainActive = false;
      private double poolX;
      private double poolY;
      private double poolZ;
      private int poolTicks = 0;
      private boolean poolActive = false;
      private double gasX;
      private double gasY;
      private double gasZ;
      private int gasTicks = 0;
      private boolean gasActive = false;
      private boolean isAcidLeaping = false;
      private int acidLeapTicks = 0;
      private double acidLeapTargetX;
      private double acidLeapTargetZ;
      private double acidLeapStartX;
      private double acidLeapStartY;
      private double acidLeapStartZ;
      private boolean isSlugSliding = false;
      private int slugSlideTicks = 0;
      private double slugSlideVelX;
      private double slugSlideVelZ;
      private EntityBijuuBombProjectile.EntityCustom activeBijuuBomb;
      private boolean acidSpitActive = false;
      private int acidSpitTicks = 0;
      private double acidSpitX;
      private double acidSpitY;
      private double acidSpitZ;
      private double acidSpitVelX;
      private double acidSpitVelY;
      private double acidSpitVelZ;
      private int ticksSinceLastDamage = 0;
      private static final int REGEN_DELAY_TICKS = 200;
      private static final int REGEN_INTERVAL_TICKS = 100;
      private int regenTickCounter = 0;
      private int targetSwitchTimer = 0;
      private int despawnTimer = 36000;
      private double spawnAnchorX;
      private double spawnAnchorY;
      private double spawnAnchorZ;
      private boolean hasSpawnAnchor = false;
      private static final double MAX_DRIFT_DISTANCE = (double)200.0F;
      private BossInfoServer bossInfo;
      private static final int ACID_SPIT_CD = 60;
      private static final int TAIL_SLAM_CD = 80;
      private static final int ACID_RAIN_CD = 200;
      private static final int CORROSIVE_POOL_CD = 160;
      private static final int TOXIC_GAS_CD = 240;
      private static final int BIJU_BOMB_CD = 400;
      private static final int BIJU_BOMB_WINDUP_TICKS = 110;
      private static final int ACID_LEAP_CD = 160;
      private static final int SLUG_SLIDE_CD = 120;
      private static final float ACID_SPIT_DAMAGE = 32.0F;
      private static final float ACID_SPIT_RADIUS = 4.0F;
      private static final float TAIL_SLAM_DAMAGE = 48.0F;
      private static final float ACID_RAIN_DAMAGE = 6.4F;
      private static final float CORROSIVE_POOL_DAMAGE = 8.8F;
      private static final float TOXIC_GAS_DAMAGE = 10.4F;
      private static final float BIJU_BOMB_DAMAGE = 84.0F;
      private static final float BIJU_BOMB_RADIUS = 16.0F;
      private static final float ACID_LEAP_DAMAGE = 52.8F;
      private static final float ACID_LEAP_RADIUS = 10.0F;
      private static final float ACID_POOL_LINGER_DAMAGE = 6.4F;
      private static final float SLUG_SLIDE_DAMAGE = 26.4F;
      private static final float TRUE_DAMAGE_SPLIT = 0.75F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(6.6F, 19.8F);
         this.experienceValue = 1000;
         this.isImmuneToFire = true;
         this.setNoAI(true);
         this.enablePersistence();
         this.stepHeight = 3.0F;
         this.maxHurtResistantTime = 7;
         if (!world.isRemote) {
            this.bossInfo = new BossInfoServer(new TextComponentString("§2§lSaiken - The Six-Tails"), Color.GREEN, Overlay.PROGRESS);
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
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)150000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.45);
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)25.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)125.0F);
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
         return null;
      }

      public SoundEvent getHurtSound(DamageSource ds) {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.hurt"));
      }

      public SoundEvent getDeathSound() {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.death"));
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
            float normalDmg = damage * 0.25F * phaseMul;
            float trueDmg = damage * 0.75F * phaseMul;
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
         if (source != DamageSource.IN_FIRE && source != DamageSource.ON_FIRE && source != DamageSource.LAVA && source != DamageSource.DROWN && source != DamageSource.FALL) {
            this.ticksSinceLastDamage = 0;
            return super.attackEntityFrom(source, amount);
         } else {
            return false;
         }
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

               if (this.currentPhase < 3) {
                  ++this.ticksSinceLastDamage;
                  if (this.ticksSinceLastDamage >= 200) {
                     ++this.regenTickCounter;
                     if (this.regenTickCounter >= 100) {
                        this.regenTickCounter = 0;
                        float healAmount = this.getMaxHealth() * 0.01F;
                        this.setHealth(Math.min(this.getHealth() + healAmount, this.getMaxHealth()));
                        if (this.world instanceof WorldServer) {
                           WorldServer ws = (WorldServer)this.world;
                           ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + (double)8.0F, this.posZ, 20, (double)3.0F, (double)5.0F, (double)3.0F, 0.1, new int[0]);
                           ws.spawnParticle(EnumParticleTypes.SLIME, this.posX, this.posY + (double)5.0F, this.posZ, 15, (double)4.0F, (double)3.0F, (double)4.0F, 0.05, new int[0]);
                        }
                     }
                  }
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

               if (this.acidSpitCooldown > 0) {
                  --this.acidSpitCooldown;
               }

               if (this.tailSlamCooldown > 0) {
                  --this.tailSlamCooldown;
               }

               if (this.acidRainCooldown > 0) {
                  --this.acidRainCooldown;
               }

               if (this.corrosivePoolCooldown > 0) {
                  --this.corrosivePoolCooldown;
               }

               if (this.toxicGasCooldown > 0) {
                  --this.toxicGasCooldown;
               }

               if (this.bijuBombCooldown > 0) {
                  --this.bijuBombCooldown;
               }

               if (this.acidLeapCooldown > 0) {
                  --this.acidLeapCooldown;
               }

               if (this.slugSlideCooldown > 0) {
                  --this.slugSlideCooldown;
               }

               if (this.globalCooldown > 0) {
                  --this.globalCooldown;
               }

               if (this.antiYCheeseCooldown > 0) {
                  --this.antiYCheeseCooldown;
               }

               if (++this.targetSwitchTimer >= 100 + this.rand.nextInt(60)) {
                  this.targetSwitchTimer = 0;
                  this.switchTarget();
               }

               this.updatePhase();
               if (this.isAcidLeaping) {
                  this.processAcidLeap();
               } else if (this.isSlugSliding) {
                  this.processSlugSlide();
               } else {
                  if (this.acidSpitActive) {
                     this.processAcidSpitProjectile();
                  }

                  if (this.bijuBombWindup > 0) {
                     --this.bijuBombWindup;
                     this.tickBijuBombWindup();
                     if (this.bijuBombWindup == 0) {
                        this.executeBijuBomb();
                     }

                  } else {
                     if (this.acidRainActive && this.acidRainTicks > 0) {
                        --this.acidRainTicks;
                        this.tickAcidRainZone();
                        if (this.acidRainTicks <= 0) {
                           this.acidRainActive = false;
                        }
                     }

                     if (this.poolActive && this.poolTicks > 0) {
                        --this.poolTicks;
                        this.tickCorrosivePool();
                        if (this.poolTicks <= 0) {
                           this.poolActive = false;
                        }
                     }

                     if (this.gasActive && this.gasTicks > 0) {
                        --this.gasTicks;
                        this.tickToxicGas();
                        if (this.gasTicks <= 0) {
                           this.gasActive = false;
                        }
                     }

                     if (!this.hasSpawnAnchor && this.posX != (double)0.0F && this.posZ != (double)0.0F) {
                        this.spawnAnchorX = this.posX;
                        this.spawnAnchorY = this.posY;
                        this.spawnAnchorZ = this.posZ;
                        this.hasSpawnAnchor = true;
                     }

                     if (this.ticksExisted % 100 == 0 && this.hasSpawnAnchor) {
                        if (this.posX == (double)0.0F && this.posZ == (double)0.0F && this.posY < (double)10.0F) {
                           System.out.println("[WorldBossSaiken] Boss at 0,0 — position corrupted, removing.");
                           this.spawnDeathParticles();
                           this.setDead();
                           return;
                        }

                        double driftDist = Math.sqrt(Math.pow(this.posX - this.spawnAnchorX, (double)2.0F) + Math.pow(this.posZ - this.spawnAnchorZ, (double)2.0F));
                        if (driftDist > (double)200.0F) {
                           System.out.println("[WorldBossSaiken] Boss drifted " + (int)driftDist + " blocks, returning to spawn.");
                           this.setPositionAndUpdate(this.spawnAnchorX, this.spawnAnchorY, this.spawnAnchorZ);
                           this.motionX = (double)0.0F;
                           this.motionY = (double)0.0F;
                           this.motionZ = (double)0.0F;
                        }

                        if (!this.isAcidLeaping && !this.isSlugSliding) {
                           BlockPos groundPos = new BlockPos(this.posX, this.posY, this.posZ);
                           int groundY = this.world.getHeight(groundPos).getY();
                           if (groundY > 1 && this.posY - (double)groundY > (double)5.0F) {
                              this.setPositionAndUpdate(this.posX, (double)groundY, this.posZ);
                              this.motionY = (double)0.0F;
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
                           if (edgeDist > (double)4.0F && !this.isAcidLeaping && !this.isSlugSliding) {
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
                           } else {
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
      }

      private void updatePhase() {
         float hpRatio = this.getHealth() / this.getMaxHealth();
         if (hpRatio <= 0.3F && this.currentPhase < 3) {
            this.currentPhase = 3;
            if (!this.phase3Announced) {
               this.phase3Announced = true;
               this.announcePhase("§4§lSaiken enters TOXIC FURY!", "§2The Six-Tails is consumed by corrosive rage!");
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)8.0F, this.posZ, 10, (double)5.0F, (double)5.0F, (double)5.0F, (double)0.0F, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.SLIME, this.posX, this.posY + (double)5.0F, this.posZ, 150, (double)8.0F, (double)6.0F, (double)8.0F, 0.2, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)3.0F, this.posZ, 100, (double)6.0F, (double)4.0F, (double)6.0F, 0.1, new int[0]);
               }
            }
         } else if (hpRatio <= 0.6F && this.currentPhase < 2) {
            this.currentPhase = 2;
            if (!this.phase2Announced) {
               this.phase2Announced = true;
               this.announcePhase("§a§lSaiken unleashes Acid Release!", "§2Corrosive slime pours from the Six-Tails!");
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  ws.spawnParticle(EnumParticleTypes.SLIME, this.posX, this.posY + (double)6.0F, this.posZ, 80, (double)5.0F, (double)4.0F, (double)5.0F, 0.15, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX, this.posY + (double)4.0F, this.posZ, 60, (double)4.0F, (double)3.0F, (double)4.0F, 0.1, new int[0]);
               }
            }
         }

      }

      private float getPhaseMultiplier() {
         if (this.currentPhase >= 3) {
            return 2.0F;
         } else {
            return this.currentPhase >= 2 ? 1.4F : 1.0F;
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
         if (this.currentPhase >= 3 && this.bijuBombCooldown <= 0 && dist > (double)8.0F && dist < (double)50.0F) {
            this.startBijuBombWindup(target);
         } else if (this.currentPhase >= 3 && this.toxicGasCooldown <= 0 && dist < (double)12.0F) {
            this.doToxicGasCloud(target);
         } else if (this.currentPhase >= 2 && this.acidRainCooldown <= 0 && dist > (double)5.0F && dist < (double)30.0F) {
            this.doAcidRain(target);
         } else if (this.currentPhase >= 2 && this.acidLeapCooldown <= 0 && dist > (double)6.0F && dist < (double)25.0F) {
            this.doAcidLeap(target);
         } else if (this.currentPhase >= 2 && this.corrosivePoolCooldown <= 0 && dist < (double)8.0F) {
            this.doCorrosivePool();
         } else if (this.slugSlideCooldown <= 0 && dist > (double)6.0F && dist < (double)30.0F) {
            this.doSlugSlide(target);
         } else if (this.acidSpitCooldown <= 0 && dist > (double)6.0F && dist < (double)35.0F && !this.acidSpitActive) {
            this.doAcidSpit(target);
         } else if (this.tailSlamCooldown <= 0 && dist < (double)8.0F) {
            this.doTailSlam(target);
         } else {
            if (this.meleeCooldown <= 0 && dist <= (double)6.0F) {
               this.attackEntityAsMob(target);
            }

         }
      }

      private void doAcidSpit(EntityLivingBase target) {
         this.acidSpitCooldown = (int)(60.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 15;
         this.acidSpitX = this.posX + this.getLookVec().x * (double)4.0F;
         this.acidSpitY = this.posY + (double)this.height * 0.3;
         this.acidSpitZ = this.posZ + this.getLookVec().z * (double)4.0F;
         double dx = target.posX - this.acidSpitX;
         double dy = target.posY - this.acidSpitY;
         double dz = target.posZ - this.acidSpitZ;
         double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
         double speed = (double)2.0F;
         this.acidSpitVelX = dx / len * speed;
         this.acidSpitVelY = dy / len * speed + 0.2;
         this.acidSpitVelZ = dz / len * speed;
         this.acidSpitActive = true;
         this.acidSpitTicks = 40;
         this.world.playSound((EntityPlayer)null, this.acidSpitX, this.acidSpitY, this.acidSpitZ, SoundEvents.ENTITY_LLAMA_SPIT, SoundCategory.HOSTILE, 4.0F, 0.4F);
      }

      private void processAcidSpitProjectile() {
         --this.acidSpitTicks;
         if (this.acidSpitTicks <= 0) {
            this.detonateAcidSpit();
         } else {
            this.acidSpitX += this.acidSpitVelX;
            this.acidSpitY += this.acidSpitVelY;
            this.acidSpitZ += this.acidSpitVelZ;
            this.acidSpitVelY -= 0.05;
            List<EntityPlayer> hit = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.acidSpitX - (double)1.5F, this.acidSpitY - (double)1.5F, this.acidSpitZ - (double)1.5F, this.acidSpitX + (double)1.5F, this.acidSpitY + (double)1.5F, this.acidSpitZ + (double)1.5F), (p) -> p.isEntityAlive() && !p.isCreative() && !p.isSpectator());
            if (!hit.isEmpty()) {
               this.detonateAcidSpit();
            } else {
               BlockPos ballPos = new BlockPos(this.acidSpitX, this.acidSpitY, this.acidSpitZ);
               if (!this.world.isAirBlock(ballPos)) {
                  this.detonateAcidSpit();
               } else {
                  if (this.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)this.world;
                     ws.spawnParticle(EnumParticleTypes.SLIME, this.acidSpitX, this.acidSpitY, this.acidSpitZ, 6, 0.4, 0.4, 0.4, 0.02, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.SPELL_MOB, this.acidSpitX, this.acidSpitY, this.acidSpitZ, 3, 0.2, 0.2, 0.2, (double)0.0F, new int[0]);
                  }

               }
            }
         }
      }

      private void detonateAcidSpit() {
         this.acidSpitActive = false;
         float dmg = 32.0F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.25F;
         float trueDmg = dmg * 0.75F;

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.acidSpitX - (double)4.0F, this.acidSpitY - (double)4.0F, this.acidSpitZ - (double)4.0F, this.acidSpitX + (double)4.0F, this.acidSpitY + (double)4.0F, this.acidSpitZ + (double)4.0F), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
            double d = p.getDistance(this.acidSpitX, this.acidSpitY, this.acidSpitZ);
            if (d <= (double)4.0F) {
               float falloff = 1.0F - (float)(d / (double)4.0F) * 0.5F;
               p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
               p.hurtResistantTime = 0;
               p.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
               p.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 1, false, true));
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.SLIME, this.acidSpitX, this.acidSpitY + (double)1.0F, this.acidSpitZ, 40, 1.2, (double)1.0F, 1.2, 0.15, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SPELL_MOB, this.acidSpitX, this.acidSpitY + (double)0.5F, this.acidSpitZ, 25, 1.2, (double)0.5F, 1.2, (double)0.0F, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.acidSpitX, this.acidSpitY, this.acidSpitZ, SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.HOSTILE, 3.0F, 0.6F);
      }

      private void doTailSlam(EntityLivingBase target) {
         this.tailSlamCooldown = (int)(80.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 15;
         float dmg = 48.0F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.25F;
         float trueDmg = dmg * 0.75F;
         if (target instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)target;
            p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
            p.hurtResistantTime = 0;
            p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
            double dx = p.posX - this.posX;
            double dz = p.posZ - this.posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0.01) {
               p.motionX += dx / len * (double)2.0F;
               p.motionY += 0.8;
               p.motionZ += dz / len * (double)2.0F;
               p.velocityChanged = true;
            }
         } else {
            target.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
            target.hurtResistantTime = 0;
            target.attackEntityFrom(DamageSource.MAGIC, trueDmg);
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.SWEEP_ATTACK, target.posX, target.posY + (double)1.0F, target.posZ, 10, (double)2.0F, (double)1.0F, (double)2.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SLIME, target.posX, target.posY + (double)0.5F, target.posZ, 25, (double)2.0F, (double)0.5F, (double)2.0F, 0.1, new int[0]);
            ws.spawnParticle(EnumParticleTypes.BLOCK_CRACK, target.posX, target.posY + (double)0.5F, target.posZ, 40, (double)2.0F, 0.3, (double)2.0F, 0.1, new int[]{Block.getStateId(Blocks.STONE.getDefaultState())});
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 3.0F, 0.3F);
      }

      private void doAcidRain(EntityLivingBase target) {
         this.acidRainCooldown = (int)(200.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 20;
         this.acidRainX = target.posX;
         this.acidRainY = target.posY;
         this.acidRainZ = target.posZ;
         this.acidRainTicks = 100;
         this.acidRainActive = true;

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F))) {
            p.sendMessage(new TextComponentString("§a§lAcid Rain is falling! Move away from the target zone!"));
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.WEATHER_RAIN_ABOVE, SoundCategory.HOSTILE, 3.0F, 0.3F);
      }

      private void tickAcidRainZone() {
         float radius = 15.0F;
         float dmg = 6.4F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.25F;
         float trueDmg = dmg * 0.75F;
         if (this.acidRainTicks % 10 == 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.acidRainX - (double)radius, this.acidRainY - (double)5.0F, this.acidRainZ - (double)radius, this.acidRainX + (double)radius, this.acidRainY + (double)20.0F, this.acidRainZ + (double)radius), (pxx) -> pxx.isEntityAlive() && !pxx.isCreative() && !pxx.isSpectator())) {
               double dx = p.posX - this.acidRainX;
               double dz = p.posZ - this.acidRainZ;
               double dist = Math.sqrt(dx * dx + dz * dz);
               if (dist <= (double)radius) {
                  p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  p.addPotionEffect(new PotionEffect(MobEffects.POISON, 60, 0, false, true));
               }
            }
         }

         if (this.acidRainTicks % 2 == 0 && this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 8; ++i) {
               double px = this.acidRainX + (this.rand.nextDouble() - (double)0.5F) * (double)radius * (double)2.0F;
               double pz = this.acidRainZ + (this.rand.nextDouble() - (double)0.5F) * (double)radius * (double)2.0F;
               double py = this.acidRainY + (double)15.0F + this.rand.nextDouble() * (double)5.0F;
               ws.spawnParticle(EnumParticleTypes.DRIP_WATER, px, py, pz, 2, 0.2, (double)0.0F, 0.2, (double)0.0F, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SLIME, px, this.acidRainY + this.rand.nextDouble() * (double)2.0F, pz, 1, (double)0.5F, 0.2, (double)0.5F, 0.01, new int[0]);
            }

            ws.spawnParticle(EnumParticleTypes.SPELL_MOB, this.acidRainX, this.acidRainY + (double)0.5F, this.acidRainZ, 15, (double)radius * 0.4, 0.1, (double)radius * 0.4, (double)0.0F, new int[0]);
         }

      }

      private void doCorrosivePool() {
         this.corrosivePoolCooldown = (int)(160.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 15;
         this.poolX = this.posX;
         this.poolY = this.posY;
         this.poolZ = this.posZ;
         this.poolTicks = 160;
         this.poolActive = true;
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.SLIME, this.poolX, this.poolY + (double)1.0F, this.poolZ, 50, (double)4.0F, (double)0.5F, (double)4.0F, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_SLIME_PLACE, SoundCategory.HOSTILE, 3.0F, 0.5F);
      }

      private void tickCorrosivePool() {
         float radius = 8.0F;
         float dmg = 8.8F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.25F;
         float trueDmg = dmg * 0.75F;
         if (this.poolTicks % 10 == 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.poolX - (double)radius, this.poolY - (double)1.0F, this.poolZ - (double)radius, this.poolX + (double)radius, this.poolY + (double)3.0F, this.poolZ + (double)radius), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
               double dx = p.posX - this.poolX;
               double dz = p.posZ - this.poolZ;
               double dist = Math.sqrt(dx * dx + dz * dz);
               if (dist <= (double)radius) {
                  p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1, false, true));
               }
            }
         }

         if (this.poolTicks % 4 == 0 && this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.SLIME, this.poolX, this.poolY + 0.3, this.poolZ, 8, (double)radius * 0.4, 0.1, (double)radius * 0.4, 0.01, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SPELL_MOB, this.poolX, this.poolY + (double)0.5F, this.poolZ, 5, (double)radius * 0.3, 0.2, (double)radius * 0.3, (double)0.0F, new int[0]);
         }

      }

      private void doToxicGasCloud(EntityLivingBase target) {
         this.toxicGasCooldown = (int)(240.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 20;
         this.gasX = target.posX;
         this.gasY = target.posY;
         this.gasZ = target.posZ;
         this.gasTicks = 120;
         this.gasActive = true;

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F))) {
            p.sendMessage(new TextComponentString("§4§lSaiken releases toxic gas! Escape the cloud!"));
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.gasX, this.gasY + (double)3.0F, this.gasZ, 80, (double)5.0F, (double)3.0F, (double)5.0F, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_CREEPER_PRIMED, SoundCategory.HOSTILE, 3.0F, 0.3F);
      }

      private void tickToxicGas() {
         float radius = 10.0F;
         float dmg = 10.4F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.25F;
         float trueDmg = dmg * 0.75F;
         if (this.gasTicks % 10 == 0) {
            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.gasX - (double)radius, this.gasY - (double)2.0F, this.gasZ - (double)radius, this.gasX + (double)radius, this.gasY + (double)8.0F, this.gasZ + (double)radius), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
               double dx = p.posX - this.gasX;
               double dz = p.posZ - this.gasZ;
               double dist = Math.sqrt(dx * dx + dz * dz);
               if (dist <= (double)radius) {
                  p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  p.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 60, 0, false, true));
                  p.addPotionEffect(new PotionEffect(MobEffects.POISON, 80, 2, false, true));
               }
            }
         }

         if (this.gasTicks % 3 == 0 && this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.gasX, this.gasY + (double)2.0F, this.gasZ, 15, (double)radius * 0.4, (double)2.0F, (double)radius * 0.4, 0.02, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SPELL_MOB, this.gasX, this.gasY + (double)3.0F, this.gasZ, 10, (double)radius * 0.3, (double)1.5F, (double)radius * 0.3, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.gasX, this.gasY + (double)1.5F, this.gasZ, 8, (double)radius * 0.3, (double)1.0F, (double)radius * 0.3, 0.01, new int[0]);
         }

      }

      private void startBijuBombWindup(EntityLivingBase target) {
         this.bijuBombCooldown = (int)(400.0F * this.getPhaseCdMultiplier());
         this.bijuBombWindup = 110;
         this.bijuBombTX = target.posX;
         this.bijuBombTY = target.posY;
         this.bijuBombTZ = target.posZ;
         this.globalCooldown = 120;
         this.setShooting(true);
         float phaseMul = this.getPhaseMultiplier();
         float normalDmg = 84.0F * phaseMul * 0.25F;
         float trueDmg = 84.0F * phaseMul * 0.75F;
         this.activeBijuuBomb = EntityBijuuBombProjectile.EntityCustom.spawnCharging(this.world, this, 5.5F, normalDmg, trueDmg, 110, (double)16.0F);

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F))) {
            p.sendMessage(new TextComponentString("§2§lSaiken is charging a Bijuu Bomb! Move away from the target zone!"));
         }

      }

      private void tickBijuBombWindup() {
         BlockPos windupGroundPos = new BlockPos(this.posX, this.posY, this.posZ);
         int windupGroundY = this.world.getHeight(windupGroundPos).getY();
         if (windupGroundY > 1 && this.posY < (double)windupGroundY) {
            this.setPositionAndUpdate(this.posX, (double)windupGroundY, this.posZ);
         }

         this.motionX = (double)0.0F;
         this.motionY = (double)0.0F;
         this.motionZ = (double)0.0F;
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            float progress = 1.0F - (float)this.bijuBombWindup / 110.0F;
            double markerRadius = (double)(16.0F * progress);
            int markerCount = (int)(10.0F + progress * 40.0F);
            ws.spawnParticle(EnumParticleTypes.SPELL_MOB, this.bijuBombTX, this.bijuBombTY + 0.3, this.bijuBombTZ, markerCount, markerRadius * 0.4, 0.1, markerRadius * 0.4, (double)0.0F, new int[0]);
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
         if (this.activeBijuuBomb != null && this.activeBijuuBomb.isEntityAlive() && this.activeBijuuBomb.isCharging()) {
            Vec3d mouth = new Vec3d(this.activeBijuuBomb.posX, this.activeBijuuBomb.posY, this.activeBijuuBomb.posZ);
            Vec3d tgt = new Vec3d(this.bijuBombTX, this.bijuBombTY + (double)1.5F, this.bijuBombTZ);
            this.activeBijuuBomb.launchToward(tgt.subtract(mouth), 1.4F);
         }

         this.activeBijuuBomb = null;
      }

      private void doAcidLeap(EntityLivingBase target) {
         this.acidLeapCooldown = (int)(160.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 30;
         this.isAcidLeaping = true;
         this.acidLeapTicks = 30;
         this.acidLeapTargetX = target.posX;
         this.acidLeapTargetZ = target.posZ;
         this.acidLeapStartX = this.posX;
         this.acidLeapStartY = this.posY;
         this.acidLeapStartZ = this.posZ;
         this.spawnAcidPoolZone(this.posX, this.posY, this.posZ);
         this.motionY = (double)2.0F;
         this.velocityChanged = true;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SLIME_JUMP, SoundCategory.HOSTILE, 5.0F, 0.3F);
      }

      private void processAcidLeap() {
         --this.acidLeapTicks;
         if (this.acidLeapTicks <= 0) {
            this.isAcidLeaping = false;
            BlockPos gp = new BlockPos(this.posX, this.posY, this.posZ);
            int groundY = this.world.getHeight(gp).getY();
            if (groundY > 1 && Math.abs(this.posY - (double)groundY) > (double)2.0F) {
               this.setPositionAndUpdate(this.posX, (double)groundY, this.posZ);
            }

            this.motionY = (double)0.0F;
            this.spawnAcidPoolZone(this.posX, this.posY, this.posZ);
            float dmg = 52.8F * this.getPhaseMultiplier();
            float normalDmg = dmg * 0.25F;
            float trueDmg = dmg * 0.75F;

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.posX - (double)10.0F, this.posY - (double)2.0F, this.posZ - (double)10.0F, this.posX + (double)10.0F, this.posY + (double)5.0F, this.posZ + (double)10.0F), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
               double d = Math.sqrt(Math.pow(p.posX - this.posX, (double)2.0F) + Math.pow(p.posZ - this.posZ, (double)2.0F));
               if (d <= (double)10.0F) {
                  float falloff = 1.0F - (float)(d / (double)10.0F) * 0.4F;
                  p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
                  p.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 1, false, true));
                  p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 2, false, true));
                  double dx = p.posX - this.posX;
                  double dz = p.posZ - this.posZ;
                  double len = Math.sqrt(dx * dx + dz * dz);
                  if (len > 0.01) {
                     p.motionX += dx / len * (double)2.0F;
                     p.motionY += 0.6;
                     p.motionZ += dz / len * (double)2.0F;
                     p.velocityChanged = true;
                  }
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.SLIME, this.posX, this.posY + (double)1.0F, this.posZ, 80, (double)4.0F, (double)0.5F, (double)4.0F, 0.2, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX, this.posY + (double)0.5F, this.posZ, 40, (double)3.0F, 0.3, (double)3.0F, (double)0.0F, new int[0]);
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 3, (double)3.0F, (double)1.0F, (double)3.0F, (double)0.0F, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.HOSTILE, 5.0F, 0.3F);
         } else {
            if (this.acidLeapTicks > 15) {
               this.motionY = Math.max(this.motionY, 0.4);
               double dx = this.acidLeapTargetX - this.posX;
               double dz = this.acidLeapTargetZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > (double)0.5F) {
                  this.motionX = dx / len * 0.6;
                  this.motionZ = dz / len * 0.6;
               }
            } else {
               double dx = this.acidLeapTargetX - this.posX;
               double dz = this.acidLeapTargetZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > (double)0.5F) {
                  this.motionX = dx / len * 1.8;
                  this.motionZ = dz / len * 1.8;
               }

               this.motionY = (double)-1.5F;
            }

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.SLIME, this.posX, this.posY, this.posZ, 8, (double)1.5F, 0.3, (double)1.5F, 0.05, new int[0]);
            }

         }
      }

      private void spawnAcidPoolZone(double x, double y, double z) {
         if (!this.poolActive) {
            this.poolX = x;
            this.poolY = y;
            this.poolZ = z;
            this.poolTicks = 80;
            this.poolActive = true;
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.SLIME, x, y + (double)0.5F, z, 30, (double)4.0F, 0.2, (double)4.0F, 0.1, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SPELL_MOB, x, y + 0.3, z, 20, (double)3.0F, 0.1, (double)3.0F, (double)0.0F, new int[0]);
         }

      }

      private void doSlugSlide(EntityLivingBase target) {
         this.slugSlideCooldown = (int)(120.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 15;
         this.isSlugSliding = true;
         this.slugSlideTicks = 25;
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double len = Math.sqrt(dx * dx + dz * dz);
         double speed = this.currentPhase >= 3 ? (double)2.5F : (double)2.0F;
         this.slugSlideVelX = dx / len * speed;
         this.slugSlideVelZ = dz / len * speed;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.HOSTILE, 4.0F, 0.5F);
      }

      private void processSlugSlide() {
         --this.slugSlideTicks;
         if (this.slugSlideTicks <= 0) {
            this.isSlugSliding = false;
         } else {
            this.motionX = this.slugSlideVelX;
            this.motionZ = this.slugSlideVelZ;
            this.motionY = (double)0.0F;
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.velocityChanged = true;
            float phaseMul = this.getPhaseMultiplier();
            float dmg = 26.4F * phaseMul;
            float normalDmg = dmg * 0.25F;
            float trueDmg = dmg * 0.75F;

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)3.0F), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
               if (this.slugSlideTicks % 10 == 0) {
                  p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  p.addPotionEffect(new PotionEffect(MobEffects.POISON, 60, 1, false, true));
                  double dx = p.posX - this.posX;
                  double dz = p.posZ - this.posZ;
                  double len = Math.sqrt(dx * dx + dz * dz);
                  if (len > 0.01) {
                     p.motionX += dx / len * (double)1.5F;
                     p.motionY += 0.3;
                     p.motionZ += dz / len * (double)1.5F;
                     p.velocityChanged = true;
                  }
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.SLIME, this.posX, this.posY + (double)0.5F, this.posZ, 15, (double)3.0F, 0.2, (double)3.0F, 0.1, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX, this.posY + 0.3, this.posZ, 8, (double)2.0F, 0.1, (double)2.0F, (double)0.0F, new int[0]);
            }

            if (this.slugSlideTicks % 5 == 0) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.HOSTILE, 2.0F, 0.7F);
            }

         }
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
            ws.spawnParticle(EnumParticleTypes.SLIME, this.posX, this.posY + (double)8.0F, this.posZ, 200, (double)8.0F, (double)6.0F, (double)8.0F, 0.2, new int[0]);
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)6.0F, this.posZ, 20, (double)6.0F, (double)4.0F, (double)6.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY + (double)4.0F, this.posZ, 100, (double)6.0F, (double)4.0F, (double)6.0F, 0.1, new int[0]);
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
         compound.setInteger("SaikenPhase", this.currentPhase);
         compound.setBoolean("SaikenPhase2Announced", this.phase2Announced);
         compound.setBoolean("SaikenPhase3Announced", this.phase3Announced);
         compound.setInteger("SaikenAcidSpitCooldown", this.acidSpitCooldown);
         compound.setInteger("SaikenTailSlamCooldown", this.tailSlamCooldown);
         compound.setInteger("SaikenAcidRainCooldown", this.acidRainCooldown);
         compound.setInteger("SaikenCorrosivePoolCooldown", this.corrosivePoolCooldown);
         compound.setInteger("SaikenToxicGasCooldown", this.toxicGasCooldown);
         compound.setInteger("SaikenBijuBombCooldown", this.bijuBombCooldown);
         compound.setInteger("SaikenAcidLeapCooldown", this.acidLeapCooldown);
         compound.setInteger("SaikenSlugSlideCooldown", this.slugSlideCooldown);
         compound.setInteger("SaikenDespawnTimer", this.despawnTimer);
         compound.setInteger("SaikenTicksSinceLastDmg", this.ticksSinceLastDamage);
         if (this.hasSpawnAnchor) {
            compound.setDouble("SaikenAnchorX", this.spawnAnchorX);
            compound.setDouble("SaikenAnchorY", this.spawnAnchorY);
            compound.setDouble("SaikenAnchorZ", this.spawnAnchorZ);
         }

      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.currentPhase = compound.getInteger("SaikenPhase");
         if (this.currentPhase < 1) {
            this.currentPhase = 1;
         }

         this.phase2Announced = compound.getBoolean("SaikenPhase2Announced");
         this.phase3Announced = compound.getBoolean("SaikenPhase3Announced");
         this.acidSpitCooldown = compound.getInteger("SaikenAcidSpitCooldown");
         this.tailSlamCooldown = compound.getInteger("SaikenTailSlamCooldown");
         this.acidRainCooldown = compound.getInteger("SaikenAcidRainCooldown");
         this.corrosivePoolCooldown = compound.getInteger("SaikenCorrosivePoolCooldown");
         this.toxicGasCooldown = compound.getInteger("SaikenToxicGasCooldown");
         this.bijuBombCooldown = compound.getInteger("SaikenBijuBombCooldown");
         this.acidLeapCooldown = compound.getInteger("SaikenAcidLeapCooldown");
         this.slugSlideCooldown = compound.getInteger("SaikenSlugSlideCooldown");
         this.despawnTimer = compound.getInteger("SaikenDespawnTimer");
         if (this.despawnTimer <= 0) {
            this.despawnTimer = 36000;
         }

         this.ticksSinceLastDamage = compound.getInteger("SaikenTicksSinceLastDmg");
         if (compound.hasKey("SaikenAnchorX")) {
            this.spawnAnchorX = compound.getDouble("SaikenAnchorX");
            this.spawnAnchorY = compound.getDouble("SaikenAnchorY");
            this.spawnAnchorZ = compound.getDouble("SaikenAnchorZ");
            this.hasSpawnAnchor = true;
         }

         if (!this.world.isRemote && this.bossInfo == null) {
            this.bossInfo = new BossInfoServer(new TextComponentString("§2§lSaiken - The Six-Tails"), Color.GREEN, Overlay.PROGRESS);
         }

      }

      static {
         SHOOTING = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
      }
   }
}
