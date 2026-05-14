
package net.luck.narutoaddon.OtherCode.entity;

import com.google.common.collect.Lists;
import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.raid.util.KnockbackHelper;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityWoodDragon extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 7;

   public EntityWoodDragon(ElementsInfTsukAddon instance) {
      super(instance, 27);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "wood_dragon"), 7).name("wood_dragon").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, RenderWoodDragon::new);
   }

   public static class Vec2f {
      public static final Vec2f ZERO = new Vec2f(0.0F, 0.0F);
      public float x;
      public float y;

      public Vec2f(float x, float y) {
         this.x = x;
         this.y = y;
      }

      public Vec2f add(Vec2f other) {
         return new Vec2f(this.x + other.x, this.y + other.y);
      }

      public Vec2f subtract(float x, float y) {
         return new Vec2f(this.x - x, this.y - y);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class RenderWoodDragon extends Render<EntityCustom> {
      private final ResourceLocation texture = new ResourceLocation("narutomod:textures/dragon_gray.png");
      private final ResourceLocation texture2 = new ResourceLocation("narutomod:textures/gas256.png");
      private final ModelDragonHead model = new ModelDragonHead();

      public RenderWoodDragon(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.1F;
      }

      public boolean shouldRender(EntityCustom entity, ICamera camera, double camX, double camY, double camZ) {
         return true;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float yaw, float pt) {
         float animSpeedMult = entity.getAnimSpeedMultiplier();
         float age = ((float)entity.ticksExisted + pt) * animSpeedMult;
         float f5 = 0.0F;
         float f6 = 0.0F;
         float f7 = -entity.prevRotationYaw - MathHelper.wrapDegrees(entity.rotationYaw - entity.prevRotationYaw) * pt;
         float f8 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * pt;
         float scale = entity.getEntityScale();
         this.model.setRotationAngles(f6, f5, age, 0.0F, 0.0F, 0.0625F, entity);
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)x, (float)y + scale, (float)z);
         GlStateManager.rotate(f7, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(f8 - 180.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.disableCull();
         this.bindEntityTexture(entity);
         GlStateManager.color(0.55F, 0.4F, 0.25F, 1.0F);
         this.model.teethUpper.isHidden = true;
         this.model.teethLower.isHidden = true;
         this.model.eyes.isHidden = true;
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.color(0.7F, 0.55F, 0.35F, 0.4F);
         this.bindTexture(this.texture2);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         GlStateManager.translate(0.0F, age * 0.01F, 0.0F);
         GlStateManager.matrixMode(5888);
         this.model.teethUpper.isHidden = false;
         this.model.teethLower.isHidden = false;
         this.model.eyes.isHidden = true;
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.059375F);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         GlStateManager.matrixMode(5888);
         GlStateManager.disableBlend();
         this.bindEntityTexture(entity);
         GlStateManager.disableLighting();
         GlStateManager.color(1.0F, 0.9F, 0.2F, 1.0F);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         this.model.eyes.isHidden = false;
         this.model.eyes.render(0.0625F);
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.popMatrix();
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return this.texture;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelDragonHead extends ModelBase {
      private final ModelRenderer head;
      private final ModelRenderer flair;
      private final ModelRenderer bone;
      private final ModelRenderer bone2;
      private final ModelRenderer bone3;
      public final ModelRenderer teethUpper;
      public final ModelRenderer teethLower;
      private final ModelRenderer jaw;
      private final ModelRenderer[] hornRight = new ModelRenderer[7];
      private final ModelRenderer[] hornLeft = new ModelRenderer[7];
      private final ModelRenderer[] whiskerLeft = new ModelRenderer[6];
      private final ModelRenderer[] whiskerRight = new ModelRenderer[6];
      private final ModelRenderer[] spine = new ModelRenderer[100];
      public final ModelRenderer eyes;

      public ModelDragonHead() {
         this.textureWidth = 128;
         this.textureHeight = 128;
         this.head = new ModelRenderer(this);
         this.head.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.head.cubeList.add(new ModelBox(this.head, 64, 0, -6.0F, 6.0F, -26.0F, 12, 5, 16, 1.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 0, 0, -8.0F, -1.0F, -11.0F, 16, 16, 16, 1.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 32, 32, 2.0F, 4.0F, -28.0F, 4, 4, 6, 0.0F, true));
         this.head.cubeList.add(new ModelBox(this.head, 32, 32, -6.0F, 4.0F, -28.0F, 4, 4, 6, 0.0F, false));
         this.teethUpper = new ModelRenderer(this);
         this.teethUpper.setRotationPoint(0.0F, 24.0F, 0.0F);
         this.head.addChild(this.teethUpper);
         this.teethUpper.cubeList.add(new ModelBox(this.teethUpper, 0, 52, -6.0F, -12.0F, -26.0F, 12, 3, 16, 0.5F, false));
         this.flair = new ModelRenderer(this);
         this.flair.setRotationPoint(0.0F, -2.0F, -12.0F);
         this.head.addChild(this.flair);
         this.bone = new ModelRenderer(this);
         this.bone.setRotationPoint(9.0F, 9.0F, 0.0F);
         this.flair.addChild(this.bone);
         this.setRotationAngle(this.bone, 0.0F, -0.7854F, 0.0F);
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 52, 0.0F, -8.0F, 0.0F, 10, 16, 0, 0.0F, false));
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 52, -2.0F, -12.0F, 2.0F, 10, 16, 0, 0.0F, false));
         this.bone2 = new ModelRenderer(this);
         this.bone2.setRotationPoint(-9.0F, 9.0F, 0.0F);
         this.flair.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.0F, 0.7854F, 0.0F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, 52, -10.0F, -8.0F, 0.0F, 10, 16, 0, 0.0F, true));
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, 52, -8.0F, -12.0F, 2.0F, 10, 16, 0, 0.0F, true));
         this.bone3 = new ModelRenderer(this);
         this.bone3.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.flair.addChild(this.bone3);
         this.setRotationAngle(this.bone3, -0.8727F, 0.0F, 0.0F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 84, 42, -8.0F, -10.0F, 0.0F, 16, 10, 0, 0.0F, false));
         this.jaw = new ModelRenderer(this);
         this.jaw.setRotationPoint(0.0F, 11.0F, -9.0F);
         this.head.addChild(this.jaw);
         this.setRotationAngle(this.jaw, 0.7854F, 0.0F, 0.0F);
         this.jaw.cubeList.add(new ModelBox(this.jaw, 64, 22, -6.0F, 0.0F, -16.75F, 12, 4, 16, 1.0F, false));
         this.teethLower = new ModelRenderer(this);
         this.teethLower.setRotationPoint(0.0F, 13.0F, 9.0F);
         this.jaw.addChild(this.teethLower);
         this.teethLower.cubeList.add(new ModelBox(this.teethLower, 42, 42, -6.0F, -16.0F, -25.75F, 12, 2, 16, 0.5F, false));
         this.hornRight[0] = new ModelRenderer(this);
         this.hornRight[0].setRotationPoint(-6.0F, -2.0F, -13.0F);
         this.head.addChild(this.hornRight[0]);
         this.setRotationAngle(this.hornRight[0], 0.0873F, -0.5236F, 0.0F);
         this.hornRight[0].cubeList.add(new ModelBox(this.hornRight[0], 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, false));

         for(int i = 1; i < 7; ++i) {
            this.hornRight[i] = new ModelRenderer(this);
            float zPos = i <= 2 ? 7.0F : (i == 3 ? 6.5F : (i == 4 ? 6.0F : (i == 5 ? 5.5F : 5.0F)));
            float inflate = 1.0F - (float)i * 0.3F;
            if (i >= 4) {
               inflate = 0.1F - (float)(i - 3) * 0.3F;
            }

            this.hornRight[i].setRotationPoint(0.0F, 0.0F, zPos);
            this.hornRight[i - 1].addChild(this.hornRight[i]);
            this.setRotationAngle(this.hornRight[i], 0.0873F, 0.0873F, 0.0F);
            this.hornRight[i].cubeList.add(new ModelBox(this.hornRight[i], 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, inflate, false));
         }

         this.hornLeft[0] = new ModelRenderer(this);
         this.hornLeft[0].setRotationPoint(6.0F, -2.0F, -13.0F);
         this.head.addChild(this.hornLeft[0]);
         this.setRotationAngle(this.hornLeft[0], 0.0873F, 0.5236F, 0.0F);
         this.hornLeft[0].cubeList.add(new ModelBox(this.hornLeft[0], 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, true));

         for(int i = 1; i < 7; ++i) {
            this.hornLeft[i] = new ModelRenderer(this);
            float zPos = i <= 2 ? 7.0F : (i == 3 ? 6.5F : (i == 4 ? 6.0F : (i == 5 ? 5.5F : 5.0F)));
            float inflate = 1.0F - (float)i * 0.3F;
            if (i >= 4) {
               inflate = 0.1F - (float)(i - 3) * 0.3F;
            }

            this.hornLeft[i].setRotationPoint(0.0F, 0.0F, zPos);
            this.hornLeft[i - 1].addChild(this.hornLeft[i]);
            this.setRotationAngle(this.hornLeft[i], 0.0873F, -0.0873F, 0.0F);
            this.hornLeft[i].cubeList.add(new ModelBox(this.hornLeft[i], 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, inflate, true));
         }

         this.whiskerLeft[0] = new ModelRenderer(this);
         this.whiskerLeft[0].setRotationPoint(6.0F, 6.0F, -24.0F);
         this.head.addChild(this.whiskerLeft[0]);
         this.setRotationAngle(this.whiskerLeft[0], 0.0F, 1.0472F, 0.0F);
         this.whiskerLeft[0].cubeList.add(new ModelBox(this.whiskerLeft[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, true));

         for(int i = 1; i < 6; ++i) {
            this.whiskerLeft[i] = new ModelRenderer(this);
            this.whiskerLeft[i].setRotationPoint(0.0F, 0.0F, 6.0F);
            this.whiskerLeft[i - 1].addChild(this.whiskerLeft[i]);
            this.setRotationAngle(this.whiskerLeft[i], -0.0873F, -0.1745F, 0.0F);
            float inflate = 0.8F - (float)i * 0.2F;
            this.whiskerLeft[i].cubeList.add(new ModelBox(this.whiskerLeft[i], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, inflate, true));
         }

         this.whiskerRight[0] = new ModelRenderer(this);
         this.whiskerRight[0].setRotationPoint(-6.0F, 6.0F, -24.0F);
         this.head.addChild(this.whiskerRight[0]);
         this.setRotationAngle(this.whiskerRight[0], 0.0F, -1.0472F, 0.0F);
         this.whiskerRight[0].cubeList.add(new ModelBox(this.whiskerRight[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, false));

         for(int i = 1; i < 6; ++i) {
            this.whiskerRight[i] = new ModelRenderer(this);
            this.whiskerRight[i].setRotationPoint(0.0F, 0.0F, 6.0F);
            this.whiskerRight[i - 1].addChild(this.whiskerRight[i]);
            this.setRotationAngle(this.whiskerRight[i], -0.0873F, 0.1745F, 0.0F);
            float inflate = 0.8F - (float)i * 0.2F;
            this.whiskerRight[i].cubeList.add(new ModelBox(this.whiskerRight[i], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, inflate, false));
         }

         for(int i = 0; i < this.spine.length; ++i) {
            this.spine[i] = new ModelRenderer(this);
            this.spine[i].cubeList.add(new ModelBox(this.spine[i], 0, 32, -5.0F, -4.5F, 0.0F, 10, 10, 10, 2.0F, false));
            this.spine[i].cubeList.add(new ModelBox(this.spine[i], 48, 0, -1.0F, -10.5F, 2.0F, 2, 4, 6, 1.0F, false));
            if (i == 0) {
               this.spine[i].setRotationPoint(0.0F, 6.5F, 7.0F);
            } else {
               this.spine[i].setRotationPoint(0.0F, 0.0F, 11.0F);
               this.spine[i - 1].addChild(this.spine[i]);
            }
         }

         this.eyes = new ModelRenderer(this);
         this.eyes.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.eyes.cubeList.add(new ModelBox(this.eyes, 18, 20, -6.6F, 2.6F, -12.15F, 3, 2, 0, 0.0F, false));
         this.eyes.cubeList.add(new ModelBox(this.eyes, 18, 20, 3.6F, 2.6F, -12.15F, 3, 2, 0, 0.0F, true));
      }

      public void render(Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
         this.head.render(f5);
         this.spine[0].render(f5);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void setRotationAngles(float limbSwing, float f1, float ageInTicks, float f3, float headPitch, float f5, Entity e) {
         super.setRotationAngles(limbSwing, f1, ageInTicks, f3, headPitch, f5, e);

         for(int i = 1; i < this.hornRight.length; ++i) {
            this.hornRight[i].rotateAngleX = 0.0873F + MathHelper.sin((ageInTicks - (float)i) * 0.2F) * 0.0873F;
            this.hornRight[i].rotateAngleY = MathHelper.cos((ageInTicks - (float)i) * 0.3F) * 0.0873F;
            this.hornLeft[i].rotateAngleX = 0.0873F + MathHelper.sin((ageInTicks - (float)i) * 0.2F) * 0.0873F;
            this.hornLeft[i].rotateAngleY = -MathHelper.cos((ageInTicks - (float)i) * 0.3F) * 0.0873F;
         }

         EntityCustom entity = (EntityCustom)e;
         float f6 = ((float)Math.PI / 180F);
         this.head.rotateAngleX = headPitch * f6;
         this.eyes.rotateAngleX = this.head.rotateAngleX;
         if (entity.ticksExisted > 60) {
            this.jaw.rotateAngleX = 0.5236F;
         }

         for(int j = 2; j < this.whiskerRight.length; ++j) {
            this.whiskerLeft[j].rotateAngleZ = 0.2618F * ageInTicks;
            this.whiskerRight[j].rotateAngleZ = -0.2618F * ageInTicks;
         }

         for(int j = 0; j < this.spine.length; ++j) {
            if (j < entity.partRot.size()) {
               this.spine[j].isHidden = false;
               Vec2f vec = (Vec2f)entity.partRot.get(j);
               this.spine[j].rotateAngleX = -vec.y * f6;
               this.spine[j].rotateAngleY = -vec.x * f6;
            } else {
               this.spine[j].isHidden = true;
            }
         }

      }
   }

   public static class EntityCustom extends Entity {
      private static final DataParameter<Float> SCALE;
      private static final DataParameter<Integer> OWNER_ID;
      private static final DataParameter<Integer> TARGET_ID;
      private static final DataParameter<Float> SPEED_MULTIPLIER;
      private static final DataParameter<Float> ANIM_SPEED_MULT;
      private static final int WAIT_TICKS = 60;
      private static final float BASE_SPEED = 0.95F;
      private EntityLivingBase owner;
      private EntityLivingBase target;
      private Vec3d shootVec;
      private float prevHeadYaw;
      private float prevHeadPitch;
      private Vec3d lastVec;
      private double yOrigin;
      public final List<Vec2f> partRot;
      private float damage;
      private float chakraDrain;
      private int attackCooldown;
      private int maxLifetime;
      private int blockCollisionTimer;
      private static final int BLOCK_COLLISION_DELAY = 20;
      private static final int MIN_TICKS_BEFORE_COLLISION = 20;

      public EntityCustom(World world) {
         super(world);
         this.damage = 15.0F;
         this.chakraDrain = 50.0F;
         this.attackCooldown = 0;
         this.maxLifetime = 400;
         this.blockCollisionTimer = -1;
         this.partRot = Lists.newArrayList(new Vec2f[]{new Vec2f(0.0F, 0.0F), new Vec2f(0.0F, 30.0F), new Vec2f(0.0F, 30.0F), new Vec2f(0.0F, 30.0F), new Vec2f(0.0F, 30.0F), new Vec2f(0.0F, -15.0F), new Vec2f(0.0F, -15.0F), new Vec2f(0.0F, 0.0F)});
         this.setSize(1.0F, 1.0F);
         this.noClip = false;
         this.isImmuneToFire = true;
      }

      public EntityCustom(World world, EntityLivingBase owner, EntityLivingBase target) {
         this(world);
         this.owner = owner;
         this.target = target;
         if (owner != null) {
            this.setEntityScale(1.5F);
            this.setPosition(owner.posX, owner.posY, owner.posZ);
            this.rotationYaw = owner.rotationYaw;
            this.rotationPitch = owner.rotationPitch;
            this.yOrigin = owner.posY;
            this.dataManager.set(OWNER_ID, owner.getEntityId());
         }

         if (target != null) {
            this.dataManager.set(TARGET_ID, target.getEntityId());
         }

      }

      public EntityCustom(World world, EntityLivingBase owner, double shootX, double shootY, double shootZ, float scale) {
         this(world, owner, (EntityLivingBase)null);
         this.shootVec = new Vec3d(shootX, shootY, shootZ);
         this.setEntityScale(scale);
      }

      protected void entityInit() {
         this.dataManager.register(SCALE, 1.5F);
         this.dataManager.register(OWNER_ID, -1);
         this.dataManager.register(TARGET_ID, -1);
         this.dataManager.register(SPEED_MULTIPLIER, 1.0F);
         this.dataManager.register(ANIM_SPEED_MULT, 1.0F);
      }

      public float getEntityScale() {
         return (Float)this.dataManager.get(SCALE);
      }

      public void setEntityScale(float scale) {
         this.dataManager.set(SCALE, scale);
         this.setSize(scale, scale);
      }

      public float getSpeedMultiplier() {
         return (Float)this.dataManager.get(SPEED_MULTIPLIER);
      }

      public void setSpeedMultiplier(float multiplier) {
         this.dataManager.set(SPEED_MULTIPLIER, multiplier);
         this.dataManager.set(ANIM_SPEED_MULT, multiplier);
      }

      public float getAnimSpeedMultiplier() {
         return (Float)this.dataManager.get(ANIM_SPEED_MULT);
      }

      private void setWaitPosition() {
         Vec3d targetVec = null;
         if (this.shootVec != null) {
            targetVec = this.shootVec;
         } else if (this.target != null && !this.target.isDead) {
            targetVec = this.target.getPositionVector().subtract(this.getPositionVector());
         } else if (this.owner != null) {
            if (this.owner instanceof EntityLiving && ((EntityLiving)this.owner).getAttackTarget() != null) {
               targetVec = ((EntityLiving)this.owner).getAttackTarget().getPositionVector().subtract(this.getPositionVector());
            } else {
               targetVec = this.owner.getLookVec().scale((double)50.0F);
            }
         }

         if (targetVec != null) {
            float yaw = (float)(MathHelper.atan2(targetVec.z, targetVec.x) * (180D / Math.PI)) - 90.0F;
            float pitch = (float)(-(MathHelper.atan2(targetVec.y, Math.sqrt(targetVec.x * targetVec.x + targetVec.z * targetVec.z)) * (180D / Math.PI)));
            this.rotationYaw = yaw;
            this.rotationPitch = pitch;
         }

         if (this.ticksExisted <= 30) {
            this.motionY = (double)3.0F * (double)this.getEntityScale() / (double)60.0F * (double)2.0F;
         } else {
            this.motionY = (double)0.0F;
         }

      }

      public boolean isLaunched() {
         return this.ticksExisted > 60 && (Math.abs(this.motionX) > 0.01 || Math.abs(this.motionZ) > 0.01);
      }

      public void updateSegments() {
         Vec3d cposvec = this.getPositionVector();
         float slength = this.getEntityScale() * 11.0F * 0.0625F;
         Vec2f vec = (new Vec2f(this.rotationYaw, this.rotationPitch)).subtract(this.prevRotationYaw, this.prevHeadPitch);
         if (this.lastVec != null) {
            Vec3d vec2 = cposvec.subtract(this.lastVec);
            double d4 = vec2.length();
            if (d4 >= (double)slength && this.ticksExisted > 60) {
               this.partRot.add(0, vec);

               int i;
               for(i = 1; i < (int)(d4 / (double)slength); ++i) {
                  this.partRot.add(0, Vec2f.ZERO);
               }

               this.lastVec = vec2.normalize().scale((double)(slength * (float)i)).add(this.lastVec);
               return;
            }
         }

         if (this.partRot.size() > 0) {
            this.partRot.set(0, ((Vec2f)this.partRot.get(0)).add(vec));
         }

      }

      public void onUpdate() {
         if (this.prevHeadYaw == 0.0F && this.prevHeadPitch == 0.0F) {
            this.prevHeadYaw = this.rotationYaw;
            this.prevHeadPitch = this.rotationPitch;
         }

         super.onUpdate();
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
         if (this.owner == null) {
            int ownerId = (Integer)this.dataManager.get(OWNER_ID);
            if (ownerId != -1) {
               Entity e = this.world.getEntityByID(ownerId);
               if (e instanceof EntityLivingBase) {
                  this.owner = (EntityLivingBase)e;
               }
            }
         }

         if (this.target == null || this.target.isDead) {
            int targetId = (Integer)this.dataManager.get(TARGET_ID);
            if (targetId != -1) {
               Entity e = this.world.getEntityByID(targetId);
               if (e instanceof EntityLivingBase && !e.isDead) {
                  this.target = (EntityLivingBase)e;
               }
            }
         }

         if (this.ticksExisted <= 60) {
            this.lastVec = this.getPositionVector();
         }

         if (!this.world.isRemote) {
            if (this.ticksExisted > this.maxLifetime || this.ticksExisted > 100 && (this.owner == null || !this.owner.isEntityAlive())) {
               this.setDead();
               return;
            }

            if (this.ticksExisted <= 60) {
               this.setWaitPosition();
            } else if (!this.isLaunched()) {
               Vec3d targetVec = null;
               if (this.shootVec != null) {
                  targetVec = this.shootVec;
               } else if (this.target != null && !this.target.isDead) {
                  targetVec = this.target.getPositionVector().subtract(this.getPositionVector());
               } else if (this.owner != null) {
                  if (this.owner instanceof EntityLiving && ((EntityLiving)this.owner).getAttackTarget() != null) {
                     targetVec = ((EntityLiving)this.owner).getAttackTarget().getPositionVector().subtract(this.getPositionVector());
                  } else {
                     targetVec = this.owner.getLookVec().scale((double)50.0F);
                  }
               }

               if (targetVec != null) {
                  targetVec = targetVec.normalize();
                  float speed = 0.95F * this.getSpeedMultiplier();
                  this.motionX = targetVec.x * (double)speed;
                  this.motionY = targetVec.y * (double)speed;
                  this.motionZ = targetVec.z * (double)speed;
               }
            }

            if (this.isLaunched()) {
               int ticksSinceLaunch = this.ticksExisted - 60;
               boolean collisionActive = ticksSinceLaunch >= 20;
               if (collisionActive && this.blockCollisionTimer < 0 && (this.collidedHorizontally || this.collidedVertically)) {
                  this.blockCollisionTimer = 20;
                  this.noClip = true;
               }

               if (this.blockCollisionTimer >= 0) {
                  --this.blockCollisionTimer;
                  if (this.blockCollisionTimer <= 0) {
                     this.onImpact((EntityPlayer)null);
                     this.setDead();
                     return;
                  }
               }

               if (collisionActive) {
                  float hitRadius = 2.5F * this.getEntityScale();
                  List<EntityPlayer> nearbyPlayers = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)hitRadius), (px) -> px != null && !px.isDead && (this.owner == null || px != this.owner));
                  if (!nearbyPlayers.isEmpty()) {
                     EntityPlayer hitPlayer = (EntityPlayer)nearbyPlayers.get(0);
                     double closestDist = (double)this.getDistance(hitPlayer);

                     for(EntityPlayer p : nearbyPlayers) {
                        double d = (double)this.getDistance(p);
                        if (d < closestDist) {
                           closestDist = d;
                           hitPlayer = p;
                        }
                     }

                     this.onImpact(hitPlayer);
                     this.setDead();
                     return;
                  }
               }
            }
         }

         this.updateSegments();
         this.prevHeadYaw = this.rotationYaw;
         this.prevHeadPitch = this.rotationPitch;
         this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
         if (this.world.isRemote) {
            this.spawnWoodParticles();
         }

         if (this.attackCooldown > 0) {
            --this.attackCooldown;
         }

      }

      private void spawnWoodParticles() {
         if (this.isLaunched()) {
            for(int i = 0; i < 5; ++i) {
               double ox = (this.rand.nextDouble() - (double)0.5F) * (double)this.width;
               double oy = (this.rand.nextDouble() - (double)0.5F) * (double)this.height;
               double oz = (this.rand.nextDouble() - (double)0.5F) * (double)this.width;
               this.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX + ox, this.posY + (double)(this.height / 2.0F) + oy, this.posZ + oz, -this.motionX * 0.1, -this.motionY * 0.1, -this.motionZ * 0.1, new int[0]);
            }
         } else if (this.lastVec != null) {
            Vec3d vec = (new Vec3d((double)0.0F, (double)0.0F, (double)-1.75F * (double)this.getEntityScale())).rotateYaw(-this.rotationYaw * ((float)Math.PI / 180F)).add(this.lastVec.x, this.yOrigin, this.lastVec.z);

            for(int i = 0; i < this.ticksExisted / 4; ++i) {
               double ox = (this.rand.nextDouble() - (double)0.5F) * (double)this.width;
               double oy = (this.rand.nextDouble() - (double)0.5F) * (double)this.height;
               double oz = (this.rand.nextDouble() - (double)0.5F) * (double)this.width;
               this.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, vec.x + ox, vec.y + oy, vec.z + oz, (double)0.0F, 0.05, (double)0.0F, new int[0]);
            }
         }

      }

      private void onImpact(@Nullable EntityPlayer hitPlayer) {
         if (!this.world.isRemote) {
            float size = this.getEntityScale();
            DamageSource source = DamageSource.MAGIC;
            float impactDamage = this.damage * size;
            float aoeRadius = 3.0F * size;
            if (hitPlayer != null) {
               hitPlayer.hurtResistantTime = 0;
               hitPlayer.attackEntityFrom(source, impactDamage);
               this.drainChakra(hitPlayer, this.chakraDrain);
            }

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)aoeRadius), (px) -> px != null && !px.isDead && px != hitPlayer && (this.owner == null || px != this.owner))) {
               p.hurtResistantTime = 0;
               p.attackEntityFrom(source, impactDamage * 0.5F);
               this.drainChakra(p, this.chakraDrain * 0.5F);
               double dx = p.posX - this.posX;
               double dz = p.posZ - this.posZ;
               double dist = Math.sqrt(dx * dx + dz * dz);
               if (dist > (double)0.0F) {
                  KnockbackHelper.applyWallSafeKnockback(p, dx / dist * (double)1.5F, (double)0.5F, dz / dist * (double)1.5F);
               }
            }

            this.spawnImpactParticles();
         }
      }

      private void spawnImpactParticles() {
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            float scale = this.getEntityScale();
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)(scale / 2.0F), this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)(scale / 2.0F), this.posZ, 8, (double)scale * (double)0.5F, (double)scale * (double)0.5F, (double)scale * (double)0.5F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + (double)(scale / 2.0F), this.posZ, 40, (double)scale * 0.8, (double)scale * 0.8, (double)scale * 0.8, 0.15, new int[0]);
            EnumParticleTypes var10001 = EnumParticleTypes.BLOCK_CRACK;
            double var10002 = this.posX;
            double var10003 = this.posY + (double)(scale / 2.0F);
            double var10004 = this.posZ;
            double var10006 = (double)scale * (double)1.5F;
            double var10007 = (double)scale * (double)1.5F;
            double var10008 = (double)scale * (double)1.5F;
            int[] var10010 = new int[1];
            Block var10013 = Blocks.LOG;
            var10010[0] = Block.getStateId(Blocks.LOG.getDefaultState());
            ws.spawnParticle(var10001, var10002, var10003, var10004, 80, var10006, var10007, var10008, (double)0.5F, var10010);
            var10001 = EnumParticleTypes.BLOCK_CRACK;
            var10002 = this.posX;
            var10003 = this.posY + (double)(scale / 2.0F);
            var10004 = this.posZ;
            var10006 = (double)scale;
            var10007 = (double)scale;
            var10008 = (double)scale;
            var10010 = new int[1];
            var10013 = Blocks.PLANKS;
            var10010[0] = Block.getStateId(Blocks.PLANKS.getDefaultState());
            ws.spawnParticle(var10001, var10002, var10003, var10004, 50, var10006, var10007, var10008, 0.4, var10010);
            ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + (double)(scale / 2.0F), this.posZ, 30, (double)scale * 0.7, (double)scale * 0.7, (double)scale * 0.7, 0.2, new int[0]);
            ws.spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + (double)(scale / 2.0F), this.posZ, 25, (double)scale * 0.4, (double)scale * 0.4, (double)scale * 0.4, 0.1, new int[0]);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 3.0F, 0.8F + this.rand.nextFloat() * 0.3F);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.HOSTILE, 2.5F, 0.5F + this.rand.nextFloat() * 0.3F);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.HOSTILE, 2.0F, 0.6F);
         }

      }

      private void drainChakra(EntityPlayer player, float amount) {
         try {
            Class<?> chakraClass = Class.forName("net.narutomod.Chakra");
            Object pathway = chakraClass.getMethod("pathway", EntityLivingBase.class).invoke((Object)null, player);
            if (pathway != null) {
               pathway.getClass().getMethod("consume", Double.TYPE).invoke(pathway, (double)amount);
            }
         } catch (Exception var5) {
         }

      }

      public void setDamage(float damage) {
         this.damage = damage;
      }

      public void setChakraDrain(float drain) {
         this.chakraDrain = drain;
      }

      public void setMaxLifetime(int ticks) {
         this.maxLifetime = ticks;
      }

      public void setOwner(EntityLivingBase owner) {
         this.owner = owner;
         if (owner != null) {
            this.dataManager.set(OWNER_ID, owner.getEntityId());
         }

      }

      public void setTarget(EntityLivingBase target) {
         this.target = target;
         if (target != null) {
            this.dataManager.set(TARGET_ID, target.getEntityId());
         }

      }

      public void setDragonScale(float scale) {
         this.setEntityScale(scale);
      }

      public float getDragonScale() {
         return this.getEntityScale();
      }

      public void setMoveSpeed(float speed) {
         float multiplier = speed / 0.95F;
         this.setSpeedMultiplier(multiplier);
      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.setEntityScale(compound.getFloat("Scale"));
         this.damage = compound.getFloat("Damage");
         this.chakraDrain = compound.getFloat("ChakraDrain");
         this.maxLifetime = compound.getInteger("MaxLifetime");
         if (compound.hasKey("SpeedMult")) {
            this.setSpeedMultiplier(compound.getFloat("SpeedMult"));
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setFloat("Scale", this.getEntityScale());
         compound.setFloat("Damage", this.damage);
         compound.setFloat("ChakraDrain", this.chakraDrain);
         compound.setInteger("MaxLifetime", this.maxLifetime);
         compound.setFloat("SpeedMult", this.getSpeedMultiplier());
      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      protected boolean canTriggerWalking() {
         return false;
      }

      public boolean isInRangeToRenderDist(double distance) {
         return distance < (double)16384.0F;
      }

      @Nullable
      public AxisAlignedBB getCollisionBoundingBox() {
         return null;
      }

      static {
         SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         OWNER_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         TARGET_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         SPEED_MULTIPLIER = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         ANIM_SPEED_MULT = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }
}
