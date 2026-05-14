
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelQuadruped;
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
public class EntityWorldBossKokuo extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 400;

   public EntityWorldBossKokuo(ElementsInfTsukAddon instance) {
      super(instance, 980);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "worldbosskokuo"), 400).name("worldbosskokuo").tracker(96, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KokuoRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class KokuoRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod", "textures/fivetails.png");

      public KokuoRenderer(RenderManager renderManager) {
         super(renderManager, new ModelKokuo(), 10.0F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         float s = 0.35F;
         GlStateManager.scale(s, s, s);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelKokuo extends ModelQuadruped {
      private final ModelRenderer eyesHighlight;
      private final ModelRenderer headsync;
      private final ModelRenderer eye5_r1;
      private final ModelRenderer eye4_r1;
      private final ModelRenderer eye3_r1;
      private final ModelRenderer cube_r1;
      private final ModelRenderer cube_r2;
      private final ModelRenderer bone2;
      private final ModelRenderer bone7;
      private final ModelRenderer bone6;
      private final ModelRenderer cube_r3;
      private final ModelRenderer cube_r4;
      private final ModelRenderer cube_r5;
      private final ModelRenderer cube_r6;
      private final ModelRenderer cube_r7;
      private final ModelRenderer jaw;
      private final ModelRenderer cube_r8;
      private final ModelRenderer cube_r9;
      private final ModelRenderer eyes;
      private final ModelRenderer eye4_r2;
      private final ModelRenderer eye3_r2;
      private final ModelRenderer eye1_r1;
      private final ModelRenderer bone;
      private final ModelRenderer cube_r10;
      private final ModelRenderer cube_r11;
      private final ModelRenderer cube_r12;
      private final ModelRenderer bone3;
      private final ModelRenderer cube_r13;
      private final ModelRenderer cube_r14;
      private final ModelRenderer cube_r15;
      private final ModelRenderer bone4;
      private final ModelRenderer cube_r16;
      private final ModelRenderer cube_r17;
      private final ModelRenderer cube_r18;
      private final ModelRenderer cube_r19;
      private final ModelRenderer bone5;
      private final ModelRenderer cube_r20;
      private final ModelRenderer cube_r21;
      private final ModelRenderer cube_r22;
      private final ModelRenderer cube_r23;
      private final ModelRenderer leg1_1;
      private final ModelRenderer leg1_2;
      private final ModelRenderer leg1_3;
      private final ModelRenderer foot1;
      private final ModelRenderer hoof_r1;
      private final ModelRenderer hoof_r2;
      private final ModelRenderer hoof_r3;
      private final ModelRenderer leg2_1;
      private final ModelRenderer leg2_2;
      private final ModelRenderer leg2_3;
      private final ModelRenderer foot2;
      private final ModelRenderer hoof_r4;
      private final ModelRenderer hoof_r5;
      private final ModelRenderer hoof_r6;
      private final ModelRenderer leg3_1;
      private final ModelRenderer leg3_2;
      private final ModelRenderer leg3_3;
      private final ModelRenderer foot3;
      private final ModelRenderer hoof_r7;
      private final ModelRenderer hoof_r8;
      private final ModelRenderer hoof_r9;
      private final ModelRenderer leg4_1;
      private final ModelRenderer leg4_2;
      private final ModelRenderer leg4_3;
      private final ModelRenderer foot4;
      private final ModelRenderer hoof_r10;
      private final ModelRenderer hoof_r11;
      private final ModelRenderer hoof_r12;
      private final ModelRenderer[][] tail = new ModelRenderer[5][9];
      private final float[][] tailSwayX = new float[5][9];
      private final float[][] tailSwayY = new float[5][9];
      private final float[][] tailSwayZ = new float[5][9];
      private final Random rand = new Random();

      public ModelKokuo() {
         super(12, 0.0F);
         this.textureWidth = 64;
         this.textureHeight = 64;
         (this.eyesHighlight = new ModelRenderer(this)).setRotationPoint(0.0F, 12.75F, 0.0F);
         (this.headsync = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -3.0F);
         this.eyesHighlight.addChild(this.headsync);
         (this.eye5_r1 = new ModelRenderer(this)).setRotationPoint(2.0F, -2.2479F, -7.7954F);
         this.headsync.addChild(this.eye5_r1);
         this.setRotationAngle(this.eye5_r1, 0.2618F, 0.0F, 0.0F);
         this.eye5_r1.cubeList.add(new ModelBox(this.eye5_r1, 48, 9, -0.47F, -1.1F, -0.5F, 1, 2, 2, -0.4F, true));
         this.eye5_r1.cubeList.add(new ModelBox(this.eye5_r1, 48, 9, -4.53F, -1.1F, -0.5F, 1, 2, 2, -0.4F, false));
         (this.eye4_r1 = new ModelRenderer(this)).setRotationPoint(2.0099F, -2.0395F, -8.9564F);
         this.headsync.addChild(this.eye4_r1);
         this.setRotationAngle(this.eye4_r1, 0.2618F, 0.2129F, 0.0436F);
         this.eye4_r1.cubeList.add(new ModelBox(this.eye4_r1, 56, 9, -0.7099F, -1.0F, -0.5F, 1, 2, 2, -0.4F, true));
         (this.eye3_r1 = new ModelRenderer(this)).setRotationPoint(-2.0099F, -2.0395F, -8.9564F);
         this.headsync.addChild(this.eye3_r1);
         this.setRotationAngle(this.eye3_r1, 0.2618F, -0.2129F, -0.0436F);
         this.eye3_r1.cubeList.add(new ModelBox(this.eye3_r1, 56, 9, -0.2901F, -1.0F, -0.5F, 1, 2, 2, -0.4F, false));
         (this.body = new ModelRenderer(this)).setRotationPoint(0.0F, 12.75F, 0.0F);
         this.body.cubeList.add(new ModelBox(this.body, 0, 13, -3.0F, -2.1F, -3.4F, 6, 6, 7, -0.1F, false));
         (this.cube_r1 = new ModelRenderer(this)).setRotationPoint(0.0F, 1.2393F, 8.4817F);
         this.body.addChild(this.cube_r1);
         this.setRotationAngle(this.cube_r1, -0.1745F, 0.0F, 0.0F);
         this.cube_r1.cubeList.add(new ModelBox(this.cube_r1, 0, 0, -3.0F, -2.6F, -6.0F, 6, 5, 8, -0.3F, false));
         (this.cube_r2 = new ModelRenderer(this)).setRotationPoint(0.0F, 3.2643F, 2.7833F);
         this.body.addChild(this.cube_r2);
         this.setRotationAngle(this.cube_r2, 0.1309F, 0.0F, 0.0F);
         this.cube_r2.cubeList.add(new ModelBox(this.cube_r2, 22, 22, -2.5F, -1.4F, 0.6F, 5, 2, 4, 0.0F, false));
         (this.head = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -3.0F);
         this.body.addChild(this.head);
         (this.bone2 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.5F, 0.0F);
         this.head.addChild(this.bone2);
         this.setRotationAngle(this.bone2, -0.5236F, 0.0F, 0.0F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 20, 0, -2.0F, -0.9995F, -5.4071F, 4, 4, 4, 0.1F, false));
         (this.bone7 = new ModelRenderer(this)).setRotationPoint(0.0F, 3.0005F, -1.2571F);
         this.bone2.addChild(this.bone7);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 20, 0, -2.0F, -4.0F, 0.0F, 4, 4, 4, 0.2F, false));
         (this.bone6 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.0995F, -5.2571F);
         this.bone2.addChild(this.bone6);
         this.setRotationAngle(this.bone6, 0.5236F, 0.0F, 0.0F);
         (this.cube_r3 = new ModelRenderer(this)).setRotationPoint(0.0F, 1.6272F, -5.958F);
         this.bone6.addChild(this.cube_r3);
         this.setRotationAngle(this.cube_r3, -0.6109F, 0.1745F, 0.0F);
         this.cube_r3.cubeList.add(new ModelBox(this.cube_r3, 33, 0, -1.0F, -0.3772F, -0.422F, 3, 2, 2, -0.3F, false));
         (this.cube_r4 = new ModelRenderer(this)).setRotationPoint(0.0F, 1.6272F, -5.958F);
         this.bone6.addChild(this.cube_r4);
         this.setRotationAngle(this.cube_r4, -0.6109F, -0.1745F, 0.0F);
         this.cube_r4.cubeList.add(new ModelBox(this.cube_r4, 33, 0, -2.0F, -0.3772F, -0.422F, 3, 2, 2, -0.3F, true));
         (this.cube_r5 = new ModelRenderer(this)).setRotationPoint(0.9F, 1.6F, -5.6F);
         this.bone6.addChild(this.cube_r5);
         this.setRotationAngle(this.cube_r5, 0.3054F, 0.0873F, 0.0F);
         this.cube_r5.cubeList.add(new ModelBox(this.cube_r5, 29, 8, -2.0F, -0.4F, -0.7F, 3, 2, 3, -0.1F, true));
         (this.cube_r6 = new ModelRenderer(this)).setRotationPoint(-0.9F, 1.6F, -5.6F);
         this.bone6.addChild(this.cube_r6);
         this.setRotationAngle(this.cube_r6, 0.3054F, -0.0873F, 0.0F);
         this.cube_r6.cubeList.add(new ModelBox(this.cube_r6, 29, 8, -1.0F, -0.4F, -0.7F, 3, 2, 3, -0.1F, false));
         (this.cube_r7 = new ModelRenderer(this)).setRotationPoint(0.0F, 1.8861F, 2.5902F);
         this.bone6.addChild(this.cube_r7);
         this.setRotationAngle(this.cube_r7, 0.2182F, 0.0F, 0.0F);
         this.cube_r7.cubeList.add(new ModelBox(this.cube_r7, 0, 26, -2.0F, -2.5658F, -6.2052F, 4, 4, 4, 0.0F, false));
         (this.jaw = new ModelRenderer(this)).setRotationPoint(0.0F, 2.5605F, -2.2064F);
         this.bone6.addChild(this.jaw);
         this.jaw.cubeList.add(new ModelBox(this.jaw, 15, 38, -2.2F, 0.0395F, -1.8936F, 1, 2, 3, 0.0F, false));
         this.jaw.cubeList.add(new ModelBox(this.jaw, 15, 38, 1.2F, 0.0395F, -1.8936F, 1, 2, 3, 0.0F, true));
         this.jaw.cubeList.add(new ModelBox(this.jaw, 36, 4, -1.5F, 0.0395F, -4.8936F, 3, 2, 1, 0.0F, false));
         this.jaw.cubeList.add(new ModelBox(this.jaw, 19, 13, -1.5F, 1.3395F, -4.45F, 3, 1, 6, -0.3F, false));
         this.jaw.cubeList.add(new ModelBox(this.jaw, 0, 17, -2.5F, -0.5605F, -1.0936F, 1, 1, 2, -0.3F, false));
         this.jaw.cubeList.add(new ModelBox(this.jaw, 0, 17, 1.5F, -0.5605F, -1.0936F, 1, 1, 2, -0.3F, true));
         (this.cube_r8 = new ModelRenderer(this)).setRotationPoint(-1.7F, 1.0395F, -3.2936F);
         this.jaw.addChild(this.cube_r8);
         this.setRotationAngle(this.cube_r8, 0.0F, -0.2182F, 0.0F);
         this.cube_r8.cubeList.add(new ModelBox(this.cube_r8, 36, 19, -0.184F, -1.0F, -1.52F, 1, 2, 3, 0.0F, false));
         (this.cube_r9 = new ModelRenderer(this)).setRotationPoint(1.7F, 1.0395F, -3.2936F);
         this.jaw.addChild(this.cube_r9);
         this.setRotationAngle(this.cube_r9, 0.0F, 0.2182F, 0.0F);
         this.cube_r9.cubeList.add(new ModelBox(this.cube_r9, 37, 25, -0.816F, -1.0F, -1.52F, 1, 2, 3, 0.0F, true));
         (this.eyes = new ModelRenderer(this)).setRotationPoint(0.0F, 1.5F, 2.0F);
         this.bone6.addChild(this.eyes);
         (this.eye4_r2 = new ModelRenderer(this)).setRotationPoint(-1.9099F, 0.5605F, -6.9564F);
         this.eyes.addChild(this.eye4_r2);
         this.setRotationAngle(this.eye4_r2, 0.2618F, -0.2182F, -0.0585F);
         this.eye4_r2.cubeList.add(new ModelBox(this.eye4_r2, 19, 13, -0.3421F, -1.0075F, -0.5F, 1, 2, 2, -0.4F, false));
         (this.eye3_r2 = new ModelRenderer(this)).setRotationPoint(1.9099F, 0.5605F, -6.9564F);
         this.eyes.addChild(this.eye3_r2);
         this.setRotationAngle(this.eye3_r2, 0.2618F, 0.2182F, 0.0585F);
         this.eye3_r2.cubeList.add(new ModelBox(this.eye3_r2, 19, 13, -0.6579F, -1.0075F, -0.5F, 1, 2, 2, -0.4F, true));
         (this.eye1_r1 = new ModelRenderer(this)).setRotationPoint(-2.0F, 0.3521F, -5.7954F);
         this.eyes.addChild(this.eye1_r1);
         this.setRotationAngle(this.eye1_r1, 0.2618F, 0.0F, 0.0F);
         this.eye1_r1.cubeList.add(new ModelBox(this.eye1_r1, 0, 13, -0.5F, -1.1F, -0.5F, 1, 2, 2, -0.4F, false));
         this.eye1_r1.cubeList.add(new ModelBox(this.eye1_r1, 0, 13, 3.5F, -1.1F, -0.5F, 1, 2, 2, -0.4F, true));
         (this.bone = new ModelRenderer(this)).setRotationPoint(-1.55F, 0.7818F, -2.8788F);
         this.bone6.addChild(this.bone);
         this.setRotationAngle(this.bone, -0.4162F, 0.1666F, -0.5152F);
         (this.cube_r10 = new ModelRenderer(this)).setRotationPoint(0.0F, -3.5F, 1.1F);
         this.bone.addChild(this.cube_r10);
         this.setRotationAngle(this.cube_r10, -0.4363F, 0.0F, 0.0F);
         this.cube_r10.cubeList.add(new ModelBox(this.cube_r10, 20, 37, -0.5F, -1.0F, 0.0F, 1, 2, 1, -0.2F, false));
         (this.cube_r11 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.767F, 0.8706F);
         this.bone.addChild(this.cube_r11);
         this.setRotationAngle(this.cube_r11, -0.3491F, 0.0F, 0.0F);
         this.cube_r11.cubeList.add(new ModelBox(this.cube_r11, 40, 13, -0.5F, -0.5F, -0.1F, 1, 2, 1, -0.1F, false));
         (this.cube_r12 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.6818F, 0.3788F);
         this.bone.addChild(this.cube_r12);
         this.setRotationAngle(this.cube_r12, -0.2618F, 0.0F, 0.0F);
         this.cube_r12.cubeList.add(new ModelBox(this.cube_r12, 19, 17, -0.5F, -1.2F, -0.2F, 1, 1, 1, 0.0F, false));
         this.cube_r12.cubeList.add(new ModelBox(this.cube_r12, 16, 26, -0.5F, -0.2F, -0.2F, 1, 1, 1, 0.1F, false));
         (this.bone3 = new ModelRenderer(this)).setRotationPoint(1.55F, 0.7818F, -2.8788F);
         this.bone6.addChild(this.bone3);
         this.setRotationAngle(this.bone3, -0.4162F, -0.1666F, 0.5152F);
         (this.cube_r13 = new ModelRenderer(this)).setRotationPoint(0.0F, -3.5F, 1.1F);
         this.bone3.addChild(this.cube_r13);
         this.setRotationAngle(this.cube_r13, -0.4363F, 0.0F, 0.0F);
         this.cube_r13.cubeList.add(new ModelBox(this.cube_r13, 20, 37, -0.5F, -1.0F, 0.0F, 1, 2, 1, -0.2F, true));
         (this.cube_r14 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.767F, 0.8706F);
         this.bone3.addChild(this.cube_r14);
         this.setRotationAngle(this.cube_r14, -0.3491F, 0.0F, 0.0F);
         this.cube_r14.cubeList.add(new ModelBox(this.cube_r14, 40, 13, -0.5F, -0.5F, -0.1F, 1, 2, 1, -0.1F, true));
         (this.cube_r15 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.6818F, 0.3788F);
         this.bone3.addChild(this.cube_r15);
         this.setRotationAngle(this.cube_r15, -0.2618F, 0.0F, 0.0F);
         this.cube_r15.cubeList.add(new ModelBox(this.cube_r15, 19, 17, -0.5F, -1.2F, -0.2F, 1, 1, 1, 0.0F, true));
         this.cube_r15.cubeList.add(new ModelBox(this.cube_r15, 16, 26, -0.5F, -0.2F, -0.2F, 1, 1, 1, 0.1F, true));
         (this.bone4 = new ModelRenderer(this)).setRotationPoint(-1.45F, 0.5318F, -0.1288F);
         this.bone6.addChild(this.bone4);
         this.setRotationAngle(this.bone4, -0.6109F, 0.0F, -0.6981F);
         (this.cube_r16 = new ModelRenderer(this)).setRotationPoint(0.0F, -4.5F, 0.9F);
         this.bone4.addChild(this.cube_r16);
         this.setRotationAngle(this.cube_r16, -0.4363F, 0.0F, 0.0F);
         this.cube_r16.cubeList.add(new ModelBox(this.cube_r16, 20, 37, -0.5F, -1.5F, 0.0F, 1, 2, 1, -0.2F, false));
         (this.cube_r17 = new ModelRenderer(this)).setRotationPoint(0.0F, -3.517F, 0.6206F);
         this.bone4.addChild(this.cube_r17);
         this.setRotationAngle(this.cube_r17, -0.3491F, 0.0F, 0.0F);
         this.cube_r17.cubeList.add(new ModelBox(this.cube_r17, 40, 13, -0.5F, -1.0F, -0.1F, 1, 2, 1, -0.1F, false));
         (this.cube_r18 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.6818F, 0.1288F);
         this.bone4.addChild(this.cube_r18);
         this.setRotationAngle(this.cube_r18, -0.2618F, 0.0F, 0.0F);
         this.cube_r18.cubeList.add(new ModelBox(this.cube_r18, 19, 17, -0.5F, -1.2F, -0.2F, 1, 1, 1, 0.0F, false));
         this.cube_r18.cubeList.add(new ModelBox(this.cube_r18, 16, 26, -0.5F, -0.2F, -0.2F, 1, 1, 1, 0.05F, false));
         (this.cube_r19 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.6818F, -0.1212F);
         this.bone4.addChild(this.cube_r19);
         this.setRotationAngle(this.cube_r19, -0.2618F, 0.0F, 0.0F);
         this.cube_r19.cubeList.add(new ModelBox(this.cube_r19, 16, 26, -0.5F, -0.2F, -0.2F, 1, 1, 1, 0.1F, false));
         (this.bone5 = new ModelRenderer(this)).setRotationPoint(1.45F, 0.5318F, -0.1288F);
         this.bone6.addChild(this.bone5);
         this.setRotationAngle(this.bone5, -0.6109F, 0.0F, 0.6981F);
         (this.cube_r20 = new ModelRenderer(this)).setRotationPoint(0.0F, -4.5F, 0.9F);
         this.bone5.addChild(this.cube_r20);
         this.setRotationAngle(this.cube_r20, -0.4363F, 0.0F, 0.0F);
         this.cube_r20.cubeList.add(new ModelBox(this.cube_r20, 20, 37, -0.5F, -1.5F, 0.0F, 1, 2, 1, -0.2F, true));
         (this.cube_r21 = new ModelRenderer(this)).setRotationPoint(0.0F, -3.517F, 0.6206F);
         this.bone5.addChild(this.cube_r21);
         this.setRotationAngle(this.cube_r21, -0.3491F, 0.0F, 0.0F);
         this.cube_r21.cubeList.add(new ModelBox(this.cube_r21, 40, 13, -0.5F, -1.0F, -0.1F, 1, 2, 1, -0.1F, true));
         (this.cube_r22 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.6818F, 0.1288F);
         this.bone5.addChild(this.cube_r22);
         this.setRotationAngle(this.cube_r22, -0.2618F, 0.0F, 0.0F);
         this.cube_r22.cubeList.add(new ModelBox(this.cube_r22, 19, 17, -0.5F, -1.2F, -0.2F, 1, 1, 1, 0.0F, true));
         this.cube_r22.cubeList.add(new ModelBox(this.cube_r22, 16, 26, -0.5F, -0.2F, -0.2F, 1, 1, 1, 0.05F, true));
         (this.cube_r23 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.6818F, -0.1212F);
         this.bone5.addChild(this.cube_r23);
         this.setRotationAngle(this.cube_r23, -0.2618F, 0.0F, 0.0F);
         this.cube_r23.cubeList.add(new ModelBox(this.cube_r23, 16, 26, -0.5F, -0.2F, -0.2F, 1, 1, 1, 0.1F, true));
         (this.leg1 = new ModelRenderer(this)).setRotationPoint(-2.0F, 1.0F, -1.0F);
         this.body.addChild(this.leg1);
         (this.leg1_1 = new ModelRenderer(this)).setRotationPoint(-0.3946F, -0.5729F, 0.0539F);
         this.leg1.addChild(this.leg1_1);
         this.setRotationAngle(this.leg1_1, -0.3927F, 0.0F, 0.2618F);
         this.leg1_1.cubeList.add(new ModelBox(this.leg1_1, 16, 28, -1.2F, -2.3F, -1.5F, 3, 6, 3, 0.1F, false));
         (this.leg1_2 = new ModelRenderer(this)).setRotationPoint(-0.2777F, 2.536F, 0.101F);
         this.leg1_1.addChild(this.leg1_2);
         this.setRotationAngle(this.leg1_2, 0.8727F, 0.0F, -0.0436F);
         this.leg1_2.cubeList.add(new ModelBox(this.leg1_2, 0, 34, -0.9868F, 0.0261F, -1.621F, 2, 4, 3, 0.0F, false));
         (this.leg1_3 = new ModelRenderer(this)).setRotationPoint(0.0354F, 3.913F, 1.1241F);
         this.leg1_2.addChild(this.leg1_3);
         this.setRotationAngle(this.leg1_3, -0.9599F, -0.0436F, -0.1745F);
         this.leg1_3.cubeList.add(new ModelBox(this.leg1_3, 0, 0, -1.0955F, -0.209F, -1.916F, 2, 5, 2, -0.1F, false));
         (this.foot1 = new ModelRenderer(this)).setRotationPoint(-0.0629F, 4.1779F, -1.0035F);
         this.leg1_3.addChild(this.foot1);
         this.setRotationAngle(this.foot1, 0.48F, 0.0F, 0.0436F);
         (this.hoof_r1 = new ModelRenderer(this)).setRotationPoint(0.5947F, 1.8511F, -1.0411F);
         this.foot1.addChild(this.hoof_r1);
         this.setRotationAngle(this.hoof_r1, -1.5708F, 0.0F, -0.0873F);
         this.hoof_r1.cubeList.add(new ModelBox(this.hoof_r1, 0, 47, -1.6F, -2.2F, -0.3F, 2, 3, 1, -0.05F, true));
         (this.hoof_r2 = new ModelRenderer(this)).setRotationPoint(-0.4053F, 0.8511F, -0.0411F);
         this.foot1.addChild(this.hoof_r2);
         this.setRotationAngle(this.hoof_r2, 0.0436F, 0.0F, 0.0122F);
         this.hoof_r2.cubeList.add(new ModelBox(this.hoof_r2, 0, 52, -0.6F, -0.9F, -0.9F, 2, 2, 2, -0.1F, true));
         (this.hoof_r3 = new ModelRenderer(this)).setRotationPoint(0.5947F, 1.8511F, -1.0411F);
         this.foot1.addChild(this.hoof_r3);
         this.setRotationAngle(this.hoof_r3, -1.0908F, 0.0F, -0.0873F);
         this.hoof_r3.cubeList.add(new ModelBox(this.hoof_r3, 8, 47, -1.6F, -1.8F, -0.7F, 2, 3, 1, -0.1F, true));
         (this.leg2 = new ModelRenderer(this)).setRotationPoint(2.0F, 1.0F, -1.0F);
         this.body.addChild(this.leg2);
         (this.leg2_1 = new ModelRenderer(this)).setRotationPoint(0.3946F, -0.5729F, 0.0539F);
         this.leg2.addChild(this.leg2_1);
         this.setRotationAngle(this.leg2_1, -0.3927F, 0.0F, -0.2618F);
         this.leg2_1.cubeList.add(new ModelBox(this.leg2_1, 16, 28, -1.8F, -2.3F, -1.5F, 3, 6, 3, 0.1F, true));
         (this.leg2_2 = new ModelRenderer(this)).setRotationPoint(0.2777F, 2.536F, 0.101F);
         this.leg2_1.addChild(this.leg2_2);
         this.setRotationAngle(this.leg2_2, 0.8727F, 0.0F, 0.0436F);
         this.leg2_2.cubeList.add(new ModelBox(this.leg2_2, 0, 34, -1.0132F, 0.0261F, -1.621F, 2, 4, 3, 0.0F, true));
         (this.leg2_3 = new ModelRenderer(this)).setRotationPoint(-0.0354F, 3.913F, 1.1241F);
         this.leg2_2.addChild(this.leg2_3);
         this.setRotationAngle(this.leg2_3, -0.9599F, 0.0436F, 0.1745F);
         this.leg2_3.cubeList.add(new ModelBox(this.leg2_3, 0, 0, -0.9045F, -0.209F, -1.916F, 2, 5, 2, -0.1F, true));
         (this.foot2 = new ModelRenderer(this)).setRotationPoint(0.0629F, 4.1779F, -1.0035F);
         this.leg2_3.addChild(this.foot2);
         this.setRotationAngle(this.foot2, 0.48F, 0.0F, -0.0436F);
         (this.hoof_r4 = new ModelRenderer(this)).setRotationPoint(-0.5947F, 1.8511F, -1.0411F);
         this.foot2.addChild(this.hoof_r4);
         this.setRotationAngle(this.hoof_r4, -1.5708F, 0.0F, 0.0873F);
         this.hoof_r4.cubeList.add(new ModelBox(this.hoof_r4, 0, 47, -0.4F, -2.2F, -0.3F, 2, 3, 1, -0.05F, false));
         (this.hoof_r5 = new ModelRenderer(this)).setRotationPoint(0.4053F, 0.8511F, -0.0411F);
         this.foot2.addChild(this.hoof_r5);
         this.setRotationAngle(this.hoof_r5, 0.0436F, 0.0F, -0.0122F);
         this.hoof_r5.cubeList.add(new ModelBox(this.hoof_r5, 0, 52, -1.4F, -0.9F, -0.9F, 2, 2, 2, -0.1F, false));
         (this.hoof_r6 = new ModelRenderer(this)).setRotationPoint(-0.5947F, 1.8511F, -1.0411F);
         this.foot2.addChild(this.hoof_r6);
         this.setRotationAngle(this.hoof_r6, -1.0908F, 0.0F, 0.0873F);
         this.hoof_r6.cubeList.add(new ModelBox(this.hoof_r6, 8, 47, -0.4F, -1.8F, -0.7F, 2, 3, 1, -0.1F, false));
         (this.leg3 = new ModelRenderer(this)).setRotationPoint(-2.5F, 3.0F, 9.0F);
         this.body.addChild(this.leg3);
         (this.leg3_1 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.leg3.addChild(this.leg3_1);
         this.setRotationAngle(this.leg3_1, 0.0F, 0.0F, 0.0873F);
         this.leg3_1.cubeList.add(new ModelBox(this.leg3_1, 28, 28, -1.5989F, -2.3433F, -0.6F, 2, 5, 3, 0.3F, false));
         (this.leg3_2 = new ModelRenderer(this)).setRotationPoint(-0.5989F, 2.9567F, -0.75F);
         this.leg3_1.addChild(this.leg3_2);
         this.setRotationAngle(this.leg3_2, 0.9163F, 0.0F, 0.0F);
         this.leg3_2.cubeList.add(new ModelBox(this.leg3_2, 28, 28, -1.5F, -0.1978F, -0.1692F, 3, 5, 3, -0.4F, false));
         (this.leg3_3 = new ModelRenderer(this)).setRotationPoint(0.0F, 4.4022F, 2.3808F);
         this.leg3_2.addChild(this.leg3_3);
         this.setRotationAngle(this.leg3_3, -1.0908F, 0.0F, 0.0F);
         this.leg3_3.cubeList.add(new ModelBox(this.leg3_3, 10, 35, -1.0F, -0.1734F, -1.8357F, 2, 3, 2, -0.1F, false));
         (this.foot3 = new ModelRenderer(this)).setRotationPoint(0.0827F, 2.6135F, -1.7232F);
         this.leg3_3.addChild(this.foot3);
         this.setRotationAngle(this.foot3, 0.1745F, 0.0F, 0.0F);
         (this.hoof_r7 = new ModelRenderer(this)).setRotationPoint(0.7173F, 1.9631F, -0.2625F);
         this.foot3.addChild(this.hoof_r7);
         this.setRotationAngle(this.hoof_r7, -1.0908F, 0.0F, -0.0873F);
         this.hoof_r7.cubeList.add(new ModelBox(this.hoof_r7, 8, 47, -1.78F, -2.15F, -0.8F, 2, 3, 1, -0.1F, true));
         (this.hoof_r8 = new ModelRenderer(this)).setRotationPoint(-0.2827F, 0.9631F, 0.7375F);
         this.foot3.addChild(this.hoof_r8);
         this.setRotationAngle(this.hoof_r8, 0.0436F, 0.0F, -0.0087F);
         this.hoof_r8.cubeList.add(new ModelBox(this.hoof_r8, 0, 52, -0.8F, -1.1F, -0.8F, 2, 2, 2, -0.1F, true));
         (this.hoof_r9 = new ModelRenderer(this)).setRotationPoint(0.7173F, 1.9631F, -0.2625F);
         this.foot3.addChild(this.hoof_r9);
         this.setRotationAngle(this.hoof_r9, -1.5708F, 0.0F, -0.0873F);
         this.hoof_r9.cubeList.add(new ModelBox(this.hoof_r9, 0, 47, -1.78F, -2.4F, -0.55F, 2, 3, 1, -0.05F, true));
         (this.leg4 = new ModelRenderer(this)).setRotationPoint(2.5F, 3.0F, 9.0F);
         this.body.addChild(this.leg4);
         (this.leg4_1 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.leg4.addChild(this.leg4_1);
         this.setRotationAngle(this.leg4_1, 0.0F, 0.0F, -0.0873F);
         this.leg4_1.cubeList.add(new ModelBox(this.leg4_1, 28, 28, -0.4011F, -2.3433F, -0.6F, 2, 5, 3, 0.3F, true));
         (this.leg4_2 = new ModelRenderer(this)).setRotationPoint(0.5989F, 2.9567F, -0.75F);
         this.leg4_1.addChild(this.leg4_2);
         this.setRotationAngle(this.leg4_2, 0.9163F, 0.0F, 0.0F);
         this.leg4_2.cubeList.add(new ModelBox(this.leg4_2, 28, 28, -1.5F, -0.1978F, -0.1692F, 3, 5, 3, -0.4F, true));
         (this.leg4_3 = new ModelRenderer(this)).setRotationPoint(0.0F, 4.4022F, 2.3808F);
         this.leg4_2.addChild(this.leg4_3);
         this.setRotationAngle(this.leg4_3, -1.0908F, 0.0F, 0.0F);
         this.leg4_3.cubeList.add(new ModelBox(this.leg4_3, 10, 35, -1.0F, -0.1734F, -1.8357F, 2, 3, 2, -0.1F, true));
         (this.foot4 = new ModelRenderer(this)).setRotationPoint(-0.0827F, 2.6135F, -1.7232F);
         this.leg4_3.addChild(this.foot4);
         this.setRotationAngle(this.foot4, 0.1745F, 0.0F, 0.0F);
         (this.hoof_r10 = new ModelRenderer(this)).setRotationPoint(-0.7173F, 1.9631F, -0.2625F);
         this.foot4.addChild(this.hoof_r10);
         this.setRotationAngle(this.hoof_r10, -1.0908F, 0.0F, 0.0873F);
         this.hoof_r10.cubeList.add(new ModelBox(this.hoof_r10, 8, 47, -0.22F, -2.15F, -0.8F, 2, 3, 1, -0.1F, false));
         (this.hoof_r11 = new ModelRenderer(this)).setRotationPoint(0.2827F, 0.9631F, 0.7375F);
         this.foot4.addChild(this.hoof_r11);
         this.setRotationAngle(this.hoof_r11, 0.0436F, 0.0F, 0.0087F);
         this.hoof_r11.cubeList.add(new ModelBox(this.hoof_r11, 0, 52, -1.2F, -1.1F, -0.8F, 2, 2, 2, -0.1F, false));
         (this.hoof_r12 = new ModelRenderer(this)).setRotationPoint(-0.7173F, 1.9631F, -0.2625F);
         this.foot4.addChild(this.hoof_r12);
         this.setRotationAngle(this.hoof_r12, -1.5708F, 0.0F, 0.0873F);
         this.hoof_r12.cubeList.add(new ModelBox(this.hoof_r12, 0, 47, -0.22F, -2.4F, -0.55F, 2, 3, 1, -0.05F, false));
         float[][] tailBaseAngles = new float[][]{{0.0F, 0.5F, 10.5F, -0.7854F, 0.0F, 0.0F}, {-1.0F, 0.5F, 10.5F, -1.0472F, -0.2618F, 0.0F}, {1.0F, 0.5F, 10.5F, -1.5708F, 0.2618F, 0.0F}, {-2.0F, 0.5F, 10.5F, -1.3963F, -0.5236F, 0.0F}, {2.0F, 0.5F, 10.5F, -1.2217F, 0.5236F, 0.0F}};

         for(int t = 0; t < 5; ++t) {
            (this.tail[t][0] = new ModelRenderer(this)).setRotationPoint(tailBaseAngles[t][0], tailBaseAngles[t][1], tailBaseAngles[t][2]);
            this.body.addChild(this.tail[t][0]);
            this.setRotationAngle(this.tail[t][0], tailBaseAngles[t][3], tailBaseAngles[t][4], tailBaseAngles[t][5]);
            this.tail[t][0].cubeList.add(new ModelBox(this.tail[t][0], 33, 36, -1.0F, -2.5F, -1.0F, 2, 3, 2, 0.0F, false));

            for(int s = 1; s <= 4; ++s) {
               (this.tail[t][s] = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, 0.0F);
               this.tail[t][s - 1].addChild(this.tail[t][s]);
               this.setRotationAngle(this.tail[t][s], -0.2618F, 0.0F, 0.0F);
               this.tail[t][s].cubeList.add(new ModelBox(this.tail[t][s], 33, 36, -1.0F, -2.5F, -1.0F, 2, 3, 2, 0.0F, false));
            }

            float[] inflations = new float[]{-0.1F, -0.2F, -0.4F, -0.6F};
            int[][] uvs = new int[][]{{24, 42}, {33, 42}, {42, 42}, {42, 42}};
            if (t == 0) {
               uvs[0] = new int[]{33, 36};
            }

            for(int s = 5; s <= 8; ++s) {
               (this.tail[t][s] = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, 0.0F);
               this.tail[t][s - 1].addChild(this.tail[t][s]);
               this.setRotationAngle(this.tail[t][s], 0.2618F, 0.0F, 0.0F);
               int idx = s - 5;
               this.tail[t][s].cubeList.add(new ModelBox(this.tail[t][s], uvs[idx][0], uvs[idx][1], -1.0F, -2.5F, -1.0F, 2, 3, 2, inflations[idx], false));
            }
         }

         for(int i = 0; i < 5; ++i) {
            for(int j = 1; j < 9; ++j) {
               this.tailSwayX[i][j] = (this.rand.nextFloat() * 0.2618F + 0.2618F) * (this.rand.nextBoolean() ? -1.0F : 1.0F);
               this.tailSwayZ[i][j] = (this.rand.nextFloat() * 0.1745F + 0.1745F) * (this.rand.nextBoolean() ? -1.0F : 1.0F);
               this.tailSwayY[i][j] = this.rand.nextFloat() * 0.1745F + 0.1745F;
            }
         }

      }

      public void render(Entity entity, float f0, float f1, float f2, float f3, float f4, float f5) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, -28.5F, 0.0F);
         GlStateManager.scale(20.0F, 20.0F, 20.0F);
         this.body.render(f5);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.disableLighting();
         this.eyesHighlight.render(f5);
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
         this.body.rotateAngleX = 0.0F;
         this.body.rotationPointY = 12.75F;

         for(int i = 0; i < 5; ++i) {
            for(int j = 1; j < 9; ++j) {
               this.tail[i][j].rotateAngleX = MathHelper.sin((f2 - (float)j) * 0.2F) * this.tailSwayX[i][j];
               this.tail[i][j].rotateAngleZ = MathHelper.cos((f2 - (float)j) * 0.2F) * this.tailSwayZ[i][j];
            }
         }

         if (e instanceof EntityCustom && ((EntityCustom)e).isShooting()) {
            ModelRenderer var10000 = this.head;
            var10000.rotateAngleX -= 0.1745F;
            this.jaw.rotateAngleX = 0.7854F;
         } else {
            this.jaw.rotateAngleX = 0.0F;
         }

         copyModelAngles(this.body, this.eyesHighlight);
         copyModelAngles(this.head, this.headsync);
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
      private int chargeCooldown = 0;
      private int tailSwipeCooldown = 0;
      private int steamBreathCooldown = 0;
      private int groundStompCooldown = 0;
      private int bijuBombCooldown = 0;
      private int leapCooldown = 0;
      private int tramplingRushCooldown = 0;
      private int steamBallCooldown = 0;
      private int steamEruptionCooldown = 0;
      private int boilingBreathCooldown = 0;
      private int tailWhirlwindCooldown = 0;
      private int globalCooldown = 0;
      private int antiYCheeseCooldown = 0;
      private int bijuBombWindup = -1;
      private double bijuBombTX;
      private double bijuBombTY;
      private double bijuBombTZ;
      private double steamX;
      private double steamY;
      private double steamZ;
      private float steamYaw;
      private int steamTicks = 0;
      private boolean steamActive = false;
      private boolean isCharging = false;
      private int chargeTicks = 0;
      private double chargeVelX;
      private double chargeVelZ;
      private boolean isLeaping = false;
      private int leapTicks = 0;
      private double leapTargetX;
      private double leapTargetZ;
      private boolean leapAscending = true;
      private boolean isTrampling = false;
      private int tramplingTicks = 0;
      private double tramplingVelX;
      private double tramplingVelZ;
      private int boilingBreathTicks = 0;
      private boolean boilingBreathActive = false;
      private EntityBijuuBombProjectile.EntityCustom activeBijuuBomb;
      private boolean steamBallActive = false;
      private int steamBallTicks = 0;
      private double steamBallX;
      private double steamBallY;
      private double steamBallZ;
      private double steamBallVelX;
      private double steamBallVelY;
      private double steamBallVelZ;
      private int targetSwitchTimer = 0;
      private int despawnTimer = 36000;
      private double spawnAnchorX;
      private double spawnAnchorY;
      private double spawnAnchorZ;
      private boolean hasSpawnAnchor = false;
      private static final double MAX_DRIFT_DISTANCE = (double)200.0F;
      private BossInfoServer bossInfo;
      private static final int CHARGE_CD = 120;
      private static final int TAIL_SWIPE_CD = 80;
      private static final int STEAM_BREATH_CD = 140;
      private static final int GROUND_STOMP_CD = 160;
      private static final int BIJU_BOMB_CD = 400;
      private static final int BIJU_BOMB_WINDUP_TICKS = 100;
      private static final int LEAP_CD = 180;
      private static final int TRAMPLING_RUSH_CD = 200;
      private static final int STEAM_BALL_CD = 100;
      private static final int STEAM_ERUPTION_CD = 160;
      private static final int BOILING_BREATH_CD = 140;
      private static final int TAIL_WHIRLWIND_CD = 120;
      private static final float CHARGE_DAMAGE = 44.0F;
      private static final float TAIL_SWIPE_DAMAGE = 38.4F;
      private static final float STEAM_BREATH_DAMAGE = 8.8F;
      private static final float GROUND_STOMP_DAMAGE = 49.6F;
      private static final float BIJU_BOMB_DAMAGE = 88.0F;
      private static final float BIJU_BOMB_RADIUS = 16.0F;
      private static final float LEAP_DAMAGE = 60.0F;
      private static final float LEAP_RADIUS = 10.0F;
      private static final float TRAMPLING_RUSH_DAMAGE = 32.8F;
      private static final float STEAM_BALL_DAMAGE = 38.4F;
      private static final float STEAM_BALL_RADIUS = 5.0F;
      private static final float STEAM_ERUPTION_DAMAGE = 56.0F;
      private static final float STEAM_ERUPTION_RADIUS = 12.0F;
      private static final float BOILING_BREATH_DAMAGE = 11.2F;
      private static final float BOILING_BREATH_LENGTH = 15.0F;
      private static final float BOILING_BREATH_HALF_ANGLE = 22.5F;
      private static final float TAIL_WHIRLWIND_DAMAGE = 44.0F;
      private static final float TAIL_WHIRLWIND_RADIUS = 15.0F;
      private static final float TRUE_DAMAGE_SPLIT = 0.75F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(10.0F, 17.5F);
         this.experienceValue = 1000;
         this.isImmuneToFire = true;
         this.setNoAI(true);
         this.enablePersistence();
         this.stepHeight = 3.0F;
         this.maxHurtResistantTime = 7;
         if (!world.isRemote) {
            this.bossInfo = new BossInfoServer(new TextComponentString("§5§lKokuo - The Five-Tails"), Color.PURPLE, Overlay.PROGRESS);
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
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)130.0F);
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

               if (this.chargeCooldown > 0) {
                  --this.chargeCooldown;
               }

               if (this.tailSwipeCooldown > 0) {
                  --this.tailSwipeCooldown;
               }

               if (this.steamBreathCooldown > 0) {
                  --this.steamBreathCooldown;
               }

               if (this.groundStompCooldown > 0) {
                  --this.groundStompCooldown;
               }

               if (this.bijuBombCooldown > 0) {
                  --this.bijuBombCooldown;
               }

               if (this.leapCooldown > 0) {
                  --this.leapCooldown;
               }

               if (this.tramplingRushCooldown > 0) {
                  --this.tramplingRushCooldown;
               }

               if (this.steamBallCooldown > 0) {
                  --this.steamBallCooldown;
               }

               if (this.steamEruptionCooldown > 0) {
                  --this.steamEruptionCooldown;
               }

               if (this.boilingBreathCooldown > 0) {
                  --this.boilingBreathCooldown;
               }

               if (this.tailWhirlwindCooldown > 0) {
                  --this.tailWhirlwindCooldown;
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
               if (this.isCharging) {
                  this.processCharge();
               } else if (this.isLeaping) {
                  this.processLeap();
               } else if (this.isTrampling) {
                  this.processTramplingRush();
               } else {
                  if (this.steamBallActive) {
                     this.processSteamBall();
                  }

                  if (this.bijuBombWindup > 0) {
                     --this.bijuBombWindup;
                     this.tickBijuBombWindup();
                     if (this.bijuBombWindup == 0) {
                        this.executeBijuBomb();
                     }

                  } else {
                     if (this.steamActive && this.steamTicks > 0) {
                        --this.steamTicks;
                        this.tickSteamZone();
                        if (this.steamTicks <= 0) {
                           this.steamActive = false;
                        }
                     }

                     if (this.boilingBreathActive && this.boilingBreathTicks > 0) {
                        --this.boilingBreathTicks;
                        this.tickBoilingBreathZone();
                        if (this.boilingBreathTicks <= 0) {
                           this.boilingBreathActive = false;
                        }
                     }

                     if (!this.isLeaping && !this.isCharging && !this.isTrampling) {
                        BlockPos restPos = new BlockPos(this.posX, this.posY, this.posZ);
                        int restGroundY = this.world.getHeight(restPos).getY();
                        if (this.posY < (double)(restGroundY - 1)) {
                           double safeY = this.findSafeY(this.posX, (double)restGroundY + (double)2.0F, this.posZ);
                           this.setPositionAndUpdate(this.posX, safeY, this.posZ);
                           this.motionY = (double)0.0F;
                        } else if (this.posY - (double)restGroundY > (double)5.0F) {
                           this.setPositionAndUpdate(this.posX, (double)restGroundY, this.posZ);
                           this.motionY = (double)0.0F;
                        } else if (this.posY - (double)restGroundY > (double)1.5F) {
                           this.motionY = Math.min(this.motionY, (double)-0.25F);
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
                           System.out.println("[WorldBossKokuo] Boss at 0,0 — position corrupted, removing.");
                           this.spawnDeathParticles();
                           this.setDead();
                           return;
                        }

                        double driftDist = Math.sqrt(Math.pow(this.posX - this.spawnAnchorX, (double)2.0F) + Math.pow(this.posZ - this.spawnAnchorZ, (double)2.0F));
                        if (driftDist > (double)200.0F) {
                           System.out.println("[WorldBossKokuo] Boss drifted " + (int)driftDist + " blocks, returning to spawn.");
                           this.setPositionAndUpdate(this.spawnAnchorX, this.spawnAnchorY, this.spawnAnchorZ);
                           this.motionX = (double)0.0F;
                           this.motionY = (double)0.0F;
                           this.motionZ = (double)0.0F;
                        }

                        if (!this.isLeaping && !this.isCharging && !this.isTrampling) {
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
                           if (edgeDist > (double)4.0F && !this.isCharging && !this.isLeaping && !this.isTrampling) {
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
      }

      private void updatePhase() {
         float hpRatio = this.getHealth() / this.getMaxHealth();
         if (hpRatio <= 0.3F && this.currentPhase < 3) {
            this.currentPhase = 3;
            if (!this.phase3Announced) {
               this.phase3Announced = true;
               this.announcePhase("§d§lKokuo enters BERSERKER mode!", "§5The Five-Tails is consumed by rage!");
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)8.0F, this.posZ, 10, (double)5.0F, (double)5.0F, (double)5.0F, (double)0.0F, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)5.0F, this.posZ, 100, (double)6.0F, (double)4.0F, (double)6.0F, 0.15, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)3.0F, this.posZ, 80, (double)5.0F, (double)3.0F, (double)5.0F, 0.1, new int[0]);
               }
            }
         } else if (hpRatio <= 0.6F && this.currentPhase < 2) {
            this.currentPhase = 2;
            if (!this.phase2Announced) {
               this.phase2Announced = true;
               this.announcePhase("§e§lKokuo unleashes Boil Release!", "§6Steam pours from the Five-Tails!");
               if (this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)6.0F, this.posZ, 60, (double)4.0F, (double)3.0F, (double)4.0F, 0.1, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)4.0F, this.posZ, 40, (double)3.0F, (double)2.0F, (double)3.0F, 0.05, new int[0]);
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
         } else if (this.currentPhase >= 2 && this.steamEruptionCooldown <= 0 && dist < (double)6.0F) {
            this.doSteamEruption();
         } else if (this.currentPhase >= 2 && this.boilingBreathCooldown <= 0 && dist < (double)15.0F && dist > (double)3.0F && !this.boilingBreathActive) {
            this.doBoilingBreath(target);
         } else if (this.currentPhase >= 2 && this.tramplingRushCooldown <= 0 && dist > (double)10.0F && dist < (double)40.0F) {
            this.doTramplingRush(target);
         } else if (this.currentPhase >= 2 && this.groundStompCooldown <= 0 && dist < (double)8.0F) {
            this.doGroundStomp();
         } else if (this.currentPhase >= 2 && this.steamBreathCooldown <= 0 && dist < (double)15.0F && dist > (double)3.0F) {
            this.doSteamBreath(target);
         } else if (this.tailWhirlwindCooldown <= 0 && dist < (double)15.0F) {
            this.doTailWhirlwind();
         } else if (this.leapCooldown <= 0 && dist > (double)6.0F && dist < (double)25.0F) {
            this.doLeapAttack(target);
         } else if (this.steamBallCooldown <= 0 && dist > (double)10.0F && dist < (double)40.0F && !this.steamBallActive) {
            this.doSteamBall(target);
         } else if (this.chargeCooldown <= 0 && dist > (double)8.0F && dist < (double)35.0F) {
            this.doCharge(target);
         } else if (this.tailSwipeCooldown <= 0 && dist < (double)8.0F) {
            this.doTailSwipe();
         } else {
            if (this.meleeCooldown <= 0 && dist <= (double)6.0F) {
               this.attackEntityAsMob(target);
            }

         }
      }

      private void doCharge(EntityLivingBase target) {
         this.chargeCooldown = (int)(120.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 10;
         this.isCharging = true;
         this.chargeTicks = 25;
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double len = Math.sqrt(dx * dx + dz * dz);
         double speed = this.currentPhase >= 3 ? (double)2.5F : 1.8;
         this.chargeVelX = dx / len * speed;
         this.chargeVelZ = dz / len * speed;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 3.0F, 0.5F);
      }

      private void processCharge() {
         --this.chargeTicks;
         if (this.chargeTicks <= 0) {
            this.isCharging = false;
            BlockPos cgp = new BlockPos(this.posX, this.posY, this.posZ);
            int cGroundY = this.world.getHeight(cgp).getY();
            if (cGroundY > 1 && Math.abs(this.posY - (double)cGroundY) > (double)1.0F) {
               this.setPositionAndUpdate(this.posX, (double)cGroundY, this.posZ);
            }

            this.motionY = (double)0.0F;
         } else {
            this.motionX = this.chargeVelX;
            this.motionZ = this.chargeVelZ;
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.velocityChanged = true;
            float phaseMul = this.getPhaseMultiplier();
            float dmg = 44.0F * phaseMul;
            float normalDmg = dmg * 0.25F;
            float trueDmg = dmg * 0.75F;

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)2.0F), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
               p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
               p.hurtResistantTime = 0;
               p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
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

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)3.0F, this.posZ, 15, (double)2.0F, (double)1.0F, (double)2.0F, 0.05, new int[0]);
            }

         }
      }

      private void doTailSwipe() {
         this.tailSwipeCooldown = (int)(80.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 15;
         float radius = 8.0F;
         float dmg = 38.4F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.25F;
         float trueDmg = dmg * 0.75F;

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)radius), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
            double dx = p.posX - this.posX;
            double dz = p.posZ - this.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (!(d > (double)radius)) {
               p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
               p.hurtResistantTime = 0;
               p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
               if (d > 0.01) {
                  p.motionX += dx / d * (double)1.5F;
                  p.motionY += (double)0.5F;
                  p.motionZ += dz / d * (double)1.5F;
                  p.velocityChanged = true;
               }
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.SWEEP_ATTACK, this.posX, this.posY + (double)4.0F, this.posZ, 20, (double)5.0F, (double)1.0F, (double)5.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)3.0F, this.posZ, 30, (double)5.0F, (double)1.5F, (double)5.0F, 0.05, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 3.0F, 0.4F);
      }

      private void doSteamBreath(EntityLivingBase target) {
         this.steamBreathCooldown = (int)(140.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 20;
         this.steamX = this.posX;
         this.steamY = this.posY;
         this.steamZ = this.posZ;
         this.steamYaw = this.rotationYaw;
         this.steamTicks = 60;
         this.steamActive = true;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 3.0F, 0.3F);
      }

      private void tickSteamZone() {
         float coneLength = 12.0F;
         float coneHalfAngle = 35.0F;
         float dmg = 8.8F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.25F;
         float trueDmg = dmg * 0.75F;
         float yawRad = (float)Math.toRadians((double)(-this.rotationYaw));
         double lookX = Math.sin((double)yawRad);
         double lookZ = Math.cos((double)yawRad);

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)coneLength), (pxx) -> pxx.isEntityAlive() && !pxx.isCreative() && !pxx.isSpectator())) {
            double dx = p.posX - this.posX;
            double dz = p.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (!(dist > (double)coneLength) && !(dist < (double)0.5F)) {
               double dot = (dx * lookX + dz * lookZ) / dist;
               double angle = Math.toDegrees(Math.acos(Math.min((double)1.0F, Math.max((double)-1.0F, dot))));
               if (angle <= (double)coneHalfAngle && this.steamTicks % 10 == 0) {
                  p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  p.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 80, 1, false, true));
               }
            }
         }

         if (this.steamTicks % 3 == 0 && this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 5; ++i) {
               double dist = (double)3.0F + this.rand.nextDouble() * ((double)coneLength - (double)3.0F);
               double spreadAngle = (this.rand.nextDouble() - (double)0.5F) * Math.toRadians((double)(coneHalfAngle * 2.0F));
               double cos = Math.cos(spreadAngle);
               double sin = Math.sin(spreadAngle);
               double px = this.posX + (lookX * cos - lookZ * sin) * dist;
               double pz = this.posZ + (lookZ * cos + lookX * sin) * dist;
               ws.spawnParticle(EnumParticleTypes.CLOUD, px, this.posY + (double)2.0F + this.rand.nextDouble() * (double)5.0F, pz, 3, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
            }
         }

      }

      private void doGroundStomp() {
         this.groundStompCooldown = (int)(160.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 25;
         float radius = 10.0F;
         float dmg = 49.6F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.25F;
         float trueDmg = dmg * 0.75F;
         this.motionY = (double)0.5F;
         this.velocityChanged = true;

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)radius), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
            double d = (double)this.getDistance(p);
            if (!(d > (double)radius)) {
               float falloff = 1.0F - (float)(d / (double)radius) * 0.5F;
               p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
               p.hurtResistantTime = 0;
               p.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
               p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 2, false, true));
               p.motionY += 0.6;
               p.velocityChanged = true;
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 8, (double)3.0F, (double)1.0F, (double)3.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)2.0F, this.posZ, 50, (double)radius * (double)0.5F, (double)1.0F, (double)radius * (double)0.5F, 0.1, new int[0]);
            ws.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + (double)0.5F, this.posZ, 80, (double)radius * (double)0.5F, (double)0.5F, (double)radius * (double)0.5F, 0.15, new int[]{Block.getStateId(Blocks.STONE.getDefaultState())});
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 3.0F, 0.5F);
      }

      private void startBijuBombWindup(EntityLivingBase target) {
         this.bijuBombCooldown = (int)(400.0F * this.getPhaseCdMultiplier());
         this.bijuBombWindup = 100;
         this.bijuBombTX = target.posX;
         this.bijuBombTY = target.posY;
         this.bijuBombTZ = target.posZ;
         this.globalCooldown = 110;
         this.setShooting(true);
         float phaseMul = this.getPhaseMultiplier();
         float normalDmg = 88.0F * phaseMul * 0.25F;
         float trueDmg = 88.0F * phaseMul * 0.75F;
         this.activeBijuuBomb = EntityBijuuBombProjectile.EntityCustom.spawnCharging(this.world, this, 4.5F, normalDmg, trueDmg, 100, (double)16.0F);

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F))) {
            p.sendMessage(new TextComponentString("§d§lKokuo is charging a Bijuu Bomb! Move away from the target zone!"));
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
            float progress = 1.0F - (float)this.bijuBombWindup / 100.0F;
            double markerRadius = (double)(16.0F * progress);
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
         if (this.activeBijuuBomb != null && this.activeBijuuBomb.isEntityAlive() && this.activeBijuuBomb.isCharging()) {
            Vec3d mouth = new Vec3d(this.activeBijuuBomb.posX, this.activeBijuuBomb.posY, this.activeBijuuBomb.posZ);
            Vec3d tgt = new Vec3d(this.bijuBombTX, this.bijuBombTY + (double)1.5F, this.bijuBombTZ);
            this.activeBijuuBomb.launchToward(tgt.subtract(mouth), 1.4F);
         }

         this.activeBijuuBomb = null;
      }

      private void doLeapAttack(EntityLivingBase target) {
         this.leapCooldown = (int)(180.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 30;
         this.isLeaping = true;
         this.leapTicks = 30;
         this.leapAscending = true;
         this.leapTargetX = target.posX;
         this.leapTargetZ = target.posZ;
         this.motionY = (double)2.0F;
         this.velocityChanged = true;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.HOSTILE, 5.0F, 0.5F);
      }

      private void processLeap() {
         --this.leapTicks;
         if (this.leapTicks <= 0) {
            this.isLeaping = false;
            this.leapAscending = false;
            BlockPos gp = new BlockPos(this.posX, this.posY, this.posZ);
            int groundY = this.world.getHeight(gp).getY();
            if (groundY > 1 && Math.abs(this.posY - (double)groundY) > (double)2.0F) {
               this.setPositionAndUpdate(this.posX, (double)groundY, this.posZ);
            }

            this.motionY = (double)0.0F;
            float dmg = 60.0F * this.getPhaseMultiplier();
            float normalDmg = dmg * 0.25F;
            float trueDmg = dmg * 0.75F;

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.posX - (double)10.0F, this.posY - (double)2.0F, this.posZ - (double)10.0F, this.posX + (double)10.0F, this.posY + (double)5.0F, this.posZ + (double)10.0F), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
               double d = Math.sqrt(Math.pow(p.posX - this.posX, (double)2.0F) + Math.pow(p.posZ - this.posZ, (double)2.0F));
               if (d <= (double)10.0F) {
                  float falloff = 1.0F - (float)(d / (double)10.0F) * 0.4F;
                  p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
                  double dx = p.posX - this.posX;
                  double dz = p.posZ - this.posZ;
                  double len = Math.sqrt(dx * dx + dz * dz);
                  if (len > 0.01) {
                     p.motionX += dx / len * (double)2.0F;
                     p.motionY += 0.8;
                     p.motionZ += dz / len * (double)2.0F;
                     p.velocityChanged = true;
                  }

                  p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 2, false, true));
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)1.0F, this.posZ, 5, (double)4.0F, (double)1.0F, (double)4.0F, (double)0.0F, new int[0]);
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 60, (double)4.0F, (double)0.5F, (double)4.0F, 0.2, new int[0]);
               ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + (double)0.5F, this.posZ, 80, (double)3.0F, 0.3, (double)3.0F, 0.1, new int[]{Block.getStateId(this.world.getBlockState(new BlockPos(this.posX, this.posY - (double)1.0F, this.posZ)))});
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 4.0F, 0.6F);
         } else {
            if (this.leapTicks > 15) {
               this.motionY = Math.max(this.motionY, (double)0.5F);
               double dx = this.leapTargetX - this.posX;
               double dz = this.leapTargetZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > (double)0.5F) {
                  this.motionX = dx / len * 0.8;
                  this.motionZ = dz / len * 0.8;
               }
            } else {
               double dx = this.leapTargetX - this.posX;
               double dz = this.leapTargetZ - this.posZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > (double)0.5F) {
                  this.motionX = dx / len * (double)2.0F;
                  this.motionZ = dz / len * (double)2.0F;
               }

               this.motionY = (double)-1.5F;
            }

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY, this.posZ, 10, (double)2.0F, (double)0.5F, (double)2.0F, 0.05, new int[0]);
            }

         }
      }

      private void doTramplingRush(EntityLivingBase target) {
         this.tramplingRushCooldown = (int)(200.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 15;
         this.isTrampling = true;
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double len = Math.sqrt(dx * dx + dz * dz);
         double speed = this.currentPhase >= 3 ? (double)2.0F : 1.6;
         double extraOvershoot = (double)4.0F;
         int ticks = (int)Math.round((len + extraOvershoot) / speed);
         this.tramplingTicks = Math.max(18, Math.min(40, ticks));
         this.tramplingVelX = dx / len * speed;
         this.tramplingVelZ = dz / len * speed;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 5.0F, 0.4F);

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F))) {
            p.sendMessage(new TextComponentString("§d§lKokuo begins a Trampling Rush! Get out of the way!"));
         }

      }

      private void processTramplingRush() {
         --this.tramplingTicks;
         if (this.tramplingTicks <= 0) {
            this.isTrampling = false;
            BlockPos tgp = new BlockPos(this.posX, this.posY, this.posZ);
            int tGroundY = this.world.getHeight(tgp).getY();
            if (tGroundY > 1 && Math.abs(this.posY - (double)tGroundY) > (double)1.0F) {
               this.setPositionAndUpdate(this.posX, (double)tGroundY, this.posZ);
            }

            this.motionY = (double)0.0F;
            this.motionX = (double)0.0F;
            this.motionZ = (double)0.0F;
         } else {
            this.motionX = this.tramplingVelX;
            this.motionZ = this.tramplingVelZ;
            this.motionY = (double)0.0F;
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.velocityChanged = true;
            float phaseMul = this.getPhaseMultiplier();
            float dmg = 32.8F * phaseMul;
            float normalDmg = dmg * 0.25F;
            float trueDmg = dmg * 0.75F;

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)4.0F), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
               if (this.tramplingTicks % 10 == 0) {
                  p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  double dx = p.posX - this.posX;
                  double dz = p.posZ - this.posZ;
                  double len = Math.sqrt(dx * dx + dz * dz);
                  if (len > 0.01) {
                     p.motionX += dx / len * 0.45;
                     p.motionY += 0.15;
                     p.motionZ += dz / len * 0.45;
                     p.velocityChanged = true;
                  }
               }
            }

            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 15, (double)3.0F, (double)0.5F, (double)3.0F, 0.1, new int[0]);
               ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + (double)0.5F, this.posZ, 20, (double)2.0F, 0.3, (double)2.0F, 0.1, new int[]{Block.getStateId(this.world.getBlockState(new BlockPos(this.posX, this.posY - (double)1.0F, this.posZ)))});
               if (this.currentPhase >= 2) {
                  ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)3.0F, this.posZ, 10, (double)2.0F, (double)1.0F, (double)2.0F, 0.15, new int[0]);
               }
            }

            if (this.tramplingTicks % 5 == 0) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0F, 0.8F);
            }

         }
      }

      private void doSteamBall(EntityLivingBase target) {
         this.steamBallCooldown = (int)(100.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 15;
         this.steamBallX = this.posX + this.getLookVec().x * (double)4.0F;
         this.steamBallY = this.posY + (double)this.height * 0.3;
         this.steamBallZ = this.posZ + this.getLookVec().z * (double)4.0F;
         double dx = target.posX - this.steamBallX;
         double dy = target.posY - this.steamBallY;
         double dz = target.posZ - this.steamBallZ;
         double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
         double speed = (double)2.5F;
         this.steamBallVelX = dx / len * speed;
         this.steamBallVelY = dy / len * speed + 0.15;
         this.steamBallVelZ = dz / len * speed;
         this.steamBallActive = true;
         this.steamBallTicks = 40;
         this.world.playSound((EntityPlayer)null, this.steamBallX, this.steamBallY, this.steamBallZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 4.0F, 0.5F);
      }

      private void processSteamBall() {
         --this.steamBallTicks;
         if (this.steamBallTicks <= 0) {
            this.detonateSteamBall();
         } else {
            this.steamBallX += this.steamBallVelX;
            this.steamBallY += this.steamBallVelY;
            this.steamBallZ += this.steamBallVelZ;
            this.steamBallVelY -= 0.04;
            List<EntityPlayer> hit = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.steamBallX - (double)1.5F, this.steamBallY - (double)1.5F, this.steamBallZ - (double)1.5F, this.steamBallX + (double)1.5F, this.steamBallY + (double)1.5F, this.steamBallZ + (double)1.5F), (p) -> p.isEntityAlive() && !p.isCreative() && !p.isSpectator());
            if (!hit.isEmpty()) {
               this.detonateSteamBall();
            } else {
               BlockPos ballPos = new BlockPos(this.steamBallX, this.steamBallY, this.steamBallZ);
               if (!this.world.isAirBlock(ballPos)) {
                  this.detonateSteamBall();
               } else {
                  if (this.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)this.world;
                     ws.spawnParticle(EnumParticleTypes.CLOUD, this.steamBallX, this.steamBallY, this.steamBallZ, 8, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.FLAME, this.steamBallX, this.steamBallY, this.steamBallZ, 3, 0.3, 0.3, 0.3, 0.01, new int[0]);
                  }

               }
            }
         }
      }

      private void detonateSteamBall() {
         this.steamBallActive = false;
         float dmg = 38.4F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.25F;
         float trueDmg = dmg * 0.75F;

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.steamBallX - (double)5.0F, this.steamBallY - (double)5.0F, this.steamBallZ - (double)5.0F, this.steamBallX + (double)5.0F, this.steamBallY + (double)5.0F, this.steamBallZ + (double)5.0F), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
            double d = p.getDistance(this.steamBallX, this.steamBallY, this.steamBallZ);
            if (d <= (double)5.0F) {
               float falloff = 1.0F - (float)(d / (double)5.0F) * 0.5F;
               p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
               p.hurtResistantTime = 0;
               p.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
               double dx = p.posX - this.steamBallX;
               double dz = p.posZ - this.steamBallZ;
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len > 0.01) {
                  p.motionX += dx / len * (double)1.0F;
                  p.motionY += 0.4;
                  p.motionZ += dz / len * (double)1.0F;
                  p.velocityChanged = true;
               }
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.steamBallX, this.steamBallY, this.steamBallZ, 3, (double)1.0F, (double)1.0F, (double)1.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.steamBallX, this.steamBallY, this.steamBallZ, 40, (double)1.5F, (double)1.5F, (double)1.5F, 0.15, new int[0]);
            ws.spawnParticle(EnumParticleTypes.FLAME, this.steamBallX, this.steamBallY, this.steamBallZ, 20, (double)1.0F, (double)1.0F, (double)1.0F, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.steamBallX, this.steamBallY, this.steamBallZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 3.0F, 0.8F);
      }

      private void doSteamEruption() {
         this.steamEruptionCooldown = (int)(160.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 25;
         float radius = 12.0F;
         float dmg = 56.0F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.25F;
         float trueDmg = dmg * 0.75F;

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)radius), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
            double dx = p.posX - this.posX;
            double dz = p.posZ - this.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (!(d > (double)radius)) {
               float falloff = 1.0F - (float)(d / (double)radius) * 0.4F;
               p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
               p.hurtResistantTime = 0;
               p.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
               p.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 80, 1, false, true));
               if (d > 0.01) {
                  p.motionX += dx / d * (double)3.0F;
                  ++p.motionY;
                  p.motionZ += dz / d * (double)3.0F;
                  p.velocityChanged = true;
               }
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)3.0F, this.posZ, 150, (double)radius * 0.4, (double)8.0F, (double)radius * 0.4, 0.3, new int[0]);
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 80, (double)radius * (double)0.5F, (double)0.5F, (double)radius * (double)0.5F, 0.2, new int[0]);
            ws.spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)2.0F, this.posZ, 60, (double)radius * 0.3, (double)3.0F, (double)radius * 0.3, 0.15, new int[0]);
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)2.0F, this.posZ, 8, (double)4.0F, (double)2.0F, (double)4.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + (double)0.5F, this.posZ, 100, (double)radius * 0.4, (double)0.5F, (double)radius * 0.4, 0.15, new int[]{Block.getStateId(Blocks.STONE.getDefaultState())});
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 5.0F, 0.4F);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 5.0F, 0.2F);

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)50.0F))) {
            p.sendMessage(new TextComponentString("§d§lKokuo erupts with scalding steam!"));
         }

      }

      private void doBoilingBreath(EntityLivingBase target) {
         this.boilingBreathCooldown = (int)(140.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 20;
         this.boilingBreathTicks = 60;
         this.boilingBreathActive = true;
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 4.0F, 0.2F);
      }

      private void tickBoilingBreathZone() {
         float coneLength = 15.0F;
         float coneHalfAngle = 22.5F;
         float dmg = 11.2F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.25F;
         float trueDmg = dmg * 0.75F;
         float yawRad = (float)Math.toRadians((double)(-this.rotationYaw));
         double lookX = Math.sin((double)yawRad);
         double lookZ = Math.cos((double)yawRad);

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)coneLength), (pxx) -> pxx.isEntityAlive() && !pxx.isCreative() && !pxx.isSpectator())) {
            double dx = p.posX - this.posX;
            double dz = p.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (!(dist > (double)coneLength) && !(dist < (double)0.5F)) {
               double dot = (dx * lookX + dz * lookZ) / dist;
               double angle = Math.toDegrees(Math.acos(Math.min((double)1.0F, Math.max((double)-1.0F, dot))));
               if (angle <= (double)coneHalfAngle && this.boilingBreathTicks % 10 == 0) {
                  p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, trueDmg);
                  p.removePotionEffect(MobEffects.FIRE_RESISTANCE);
                  p.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 1, false, true));
               }
            }
         }

         if (this.boilingBreathTicks % 2 == 0 && this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 8; ++i) {
               double dist = (double)3.0F + this.rand.nextDouble() * ((double)coneLength - (double)3.0F);
               double spreadAngle = (this.rand.nextDouble() - (double)0.5F) * Math.toRadians((double)(coneHalfAngle * 2.0F));
               double cos = Math.cos(spreadAngle);
               double sin = Math.sin(spreadAngle);
               double px = this.posX + (lookX * cos - lookZ * sin) * dist;
               double pz = this.posZ + (lookZ * cos + lookX * sin) * dist;
               ws.spawnParticle(EnumParticleTypes.CLOUD, px, this.posY + (double)2.0F + this.rand.nextDouble() * (double)5.0F, pz, 4, 0.8, 0.8, 0.8, 0.03, new int[0]);
               ws.spawnParticle(EnumParticleTypes.FLAME, px, this.posY + (double)2.0F + this.rand.nextDouble() * (double)3.0F, pz, 2, 0.3, 0.3, 0.3, 0.01, new int[0]);
            }
         }

      }

      private void doTailWhirlwind() {
         this.tailWhirlwindCooldown = (int)(120.0F * this.getPhaseCdMultiplier());
         this.globalCooldown = 20;
         float radius = 15.0F;
         float dmg = 44.0F * this.getPhaseMultiplier();
         float normalDmg = dmg * 0.25F;
         float trueDmg = dmg * 0.75F;

         for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)radius), (px) -> px.isEntityAlive() && !px.isCreative() && !px.isSpectator())) {
            double dx = p.posX - this.posX;
            double dz = p.posZ - this.posZ;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (!(d > (double)radius)) {
               float falloff = 1.0F - (float)(d / (double)radius) * 0.3F;
               p.attackEntityFrom(DamageSource.causeMobDamage(this), normalDmg * falloff);
               p.hurtResistantTime = 0;
               p.attackEntityFrom(DamageSource.MAGIC, trueDmg * falloff);
               if (d > 0.01) {
                  p.motionX += dx / d * (double)2.5F;
                  p.motionY += 0.7;
                  p.motionZ += dz / d * (double)2.5F;
                  p.velocityChanged = true;
               }

               p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1, false, true));
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int t = 0; t < 5; ++t) {
               double angle = 1.2566370614359172 * (double)t;

               for(int s = 3; s <= (int)radius; s += 2) {
                  double sx = this.posX + Math.cos(angle) * (double)s;
                  double sz = this.posZ + Math.sin(angle) * (double)s;
                  ws.spawnParticle(EnumParticleTypes.SWEEP_ATTACK, sx, this.posY + (double)2.0F, sz, 3, (double)0.5F, (double)0.5F, (double)0.5F, (double)0.0F, new int[0]);
                  ws.spawnParticle(EnumParticleTypes.CLOUD, sx, this.posY + (double)2.5F, sz, 2, 0.3, 0.3, 0.3, 0.03, new int[0]);
               }
            }

            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)2.0F, this.posZ, 60, (double)radius * 0.4, (double)1.5F, (double)radius * 0.4, 0.1, new int[0]);
            ws.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + (double)0.5F, this.posZ, 80, (double)radius * 0.4, (double)0.5F, (double)radius * 0.4, 0.1, new int[]{Block.getStateId(this.world.getBlockState(new BlockPos(this.posX, this.posY - (double)1.0F, this.posZ)))});
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 5.0F, 0.3F);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.HOSTILE, 4.0F, 0.6F);
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
            ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)8.0F, this.posZ, 200, (double)8.0F, (double)6.0F, (double)8.0F, 0.2, new int[0]);
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)6.0F, this.posZ, 20, (double)6.0F, (double)4.0F, (double)6.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)4.0F, this.posZ, 100, (double)6.0F, (double)4.0F, (double)6.0F, 0.1, new int[0]);
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
         compound.setInteger("KokuoPhase", this.currentPhase);
         compound.setBoolean("KokuoPhase2Announced", this.phase2Announced);
         compound.setBoolean("KokuoPhase3Announced", this.phase3Announced);
         compound.setInteger("KokuoChargeCooldown", this.chargeCooldown);
         compound.setInteger("KokuoTailSwipeCooldown", this.tailSwipeCooldown);
         compound.setInteger("KokuoSteamBreathCooldown", this.steamBreathCooldown);
         compound.setInteger("KokuoGroundStompCooldown", this.groundStompCooldown);
         compound.setInteger("KokuoBijuBombCooldown", this.bijuBombCooldown);
         compound.setInteger("KokuoLeapCooldown", this.leapCooldown);
         compound.setInteger("KokuoTramplingRushCooldown", this.tramplingRushCooldown);
         compound.setInteger("KokuoSteamBallCooldown", this.steamBallCooldown);
         compound.setInteger("KokuoSteamEruptionCooldown", this.steamEruptionCooldown);
         compound.setInteger("KokuoBoilingBreathCooldown", this.boilingBreathCooldown);
         compound.setInteger("KokuoTailWhirlwindCooldown", this.tailWhirlwindCooldown);
         compound.setInteger("KokuoDespawnTimer", this.despawnTimer);
         if (this.hasSpawnAnchor) {
            compound.setDouble("KokuoAnchorX", this.spawnAnchorX);
            compound.setDouble("KokuoAnchorY", this.spawnAnchorY);
            compound.setDouble("KokuoAnchorZ", this.spawnAnchorZ);
         }

      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.currentPhase = compound.getInteger("KokuoPhase");
         if (this.currentPhase < 1) {
            this.currentPhase = 1;
         }

         this.phase2Announced = compound.getBoolean("KokuoPhase2Announced");
         this.phase3Announced = compound.getBoolean("KokuoPhase3Announced");
         this.chargeCooldown = compound.getInteger("KokuoChargeCooldown");
         this.tailSwipeCooldown = compound.getInteger("KokuoTailSwipeCooldown");
         this.steamBreathCooldown = compound.getInteger("KokuoSteamBreathCooldown");
         this.groundStompCooldown = compound.getInteger("KokuoGroundStompCooldown");
         this.bijuBombCooldown = compound.getInteger("KokuoBijuBombCooldown");
         this.leapCooldown = compound.getInteger("KokuoLeapCooldown");
         this.tramplingRushCooldown = compound.getInteger("KokuoTramplingRushCooldown");
         this.steamBallCooldown = compound.getInteger("KokuoSteamBallCooldown");
         this.steamEruptionCooldown = compound.getInteger("KokuoSteamEruptionCooldown");
         this.boilingBreathCooldown = compound.getInteger("KokuoBoilingBreathCooldown");
         this.tailWhirlwindCooldown = compound.getInteger("KokuoTailWhirlwindCooldown");
         this.despawnTimer = compound.getInteger("KokuoDespawnTimer");
         if (this.despawnTimer <= 0) {
            this.despawnTimer = 36000;
         }

         if (compound.hasKey("KokuoAnchorX")) {
            this.spawnAnchorX = compound.getDouble("KokuoAnchorX");
            this.spawnAnchorY = compound.getDouble("KokuoAnchorY");
            this.spawnAnchorZ = compound.getDouble("KokuoAnchorZ");
            this.hasSpawnAnchor = true;
         }

         if (!this.world.isRemote && this.bossInfo == null) {
            this.bossInfo = new BossInfoServer(new TextComponentString("§5§lKokuo - The Five-Tails"), Color.PURPLE, Overlay.PROGRESS);
         }

      }

      static {
         SHOOTING = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
      }
   }
}
