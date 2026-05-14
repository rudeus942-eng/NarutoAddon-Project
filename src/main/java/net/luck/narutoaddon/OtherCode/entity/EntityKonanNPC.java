
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.ModelPlayerPoseable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcPose;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKonanNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 318;
   public static final int ENTITYID_PAPER = 319;

   public EntityKonanNPC(ElementsInfTsukAddon instance) {
      super(instance, 318);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "konannpc"), 318).name("konannpc").tracker(64, 3, true).egg(-1, -1).build());
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityPaperArrowProjectile.class).id(new ResourceLocation("inftsukaddon", "paper_arrow_proj"), 319).name("paper_arrow_proj").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KonanNpcRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(EntityPaperArrowProjectile.class, PaperArrowRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class KonanNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public KonanNpcRenderer(RenderManager renderManager) {
         super(renderManager, new ModelPlayerPoseable(0.0F, false), 0.5F);
         this.addLayer(new LayerHeldItem(this));
         this.addLayer(new LayerBipedArmor(this));
         this.addLayer(new LayerPaperWings(this));
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
            GlStateManager.color(0.9F, 0.9F, 1.0F, alpha);
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
   public static class ModelPaperWings extends ModelBase {
      private final ModelRenderer rightWing;
      private final ModelRenderer leftWing;
      private final ModelRenderer rightPanel1;
      private final ModelRenderer rightPanel2;
      private final ModelRenderer rightPanel3;
      private final ModelRenderer leftPanel1;
      private final ModelRenderer leftPanel2;
      private final ModelRenderer leftPanel3;

      public ModelPaperWings() {
         this.textureWidth = 128;
         this.textureHeight = 64;
         this.rightWing = new ModelRenderer(this);
         this.rightWing.setRotationPoint(-1.0F, 2.0F, 4.0F);
         this.rightPanel1 = new ModelRenderer(this);
         this.rightPanel1.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.rightPanel1.cubeList.add(new ModelBox(this.rightPanel1, 0, 0, -14.0F, -10.0F, 0.0F, 14, 10, 1, 0.0F, false));
         this.rightPanel1.rotateAngleY = 0.35F;
         this.rightPanel1.rotateAngleZ = -0.3F;
         this.rightWing.addChild(this.rightPanel1);
         this.rightPanel2 = new ModelRenderer(this);
         this.rightPanel2.setRotationPoint(0.0F, 0.0F, 0.5F);
         this.rightPanel2.cubeList.add(new ModelBox(this.rightPanel2, 0, 12, -16.0F, -4.0F, 0.0F, 16, 8, 1, 0.0F, false));
         this.rightPanel2.rotateAngleY = 0.5F;
         this.rightPanel2.rotateAngleZ = -0.05F;
         this.rightWing.addChild(this.rightPanel2);
         this.rightPanel3 = new ModelRenderer(this);
         this.rightPanel3.setRotationPoint(0.0F, 0.0F, 1.0F);
         this.rightPanel3.cubeList.add(new ModelBox(this.rightPanel3, 0, 22, -12.0F, 3.0F, 0.0F, 12, 6, 1, 0.0F, false));
         this.rightPanel3.rotateAngleY = 0.6F;
         this.rightPanel3.rotateAngleZ = 0.15F;
         this.rightWing.addChild(this.rightPanel3);
         this.leftWing = new ModelRenderer(this);
         this.leftWing.setRotationPoint(1.0F, 2.0F, 4.0F);
         this.leftPanel1 = new ModelRenderer(this);
         this.leftPanel1.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.leftPanel1.cubeList.add(new ModelBox(this.leftPanel1, 32, 0, 0.0F, -10.0F, 0.0F, 14, 10, 1, 0.0F, false));
         this.leftPanel1.rotateAngleY = -0.35F;
         this.leftPanel1.rotateAngleZ = 0.3F;
         this.leftWing.addChild(this.leftPanel1);
         this.leftPanel2 = new ModelRenderer(this);
         this.leftPanel2.setRotationPoint(0.0F, 0.0F, 0.5F);
         this.leftPanel2.cubeList.add(new ModelBox(this.leftPanel2, 32, 12, 0.0F, -4.0F, 0.0F, 16, 8, 1, 0.0F, false));
         this.leftPanel2.rotateAngleY = -0.5F;
         this.leftPanel2.rotateAngleZ = 0.05F;
         this.leftWing.addChild(this.leftPanel2);
         this.leftPanel3 = new ModelRenderer(this);
         this.leftPanel3.setRotationPoint(0.0F, 0.0F, 1.0F);
         this.leftPanel3.cubeList.add(new ModelBox(this.leftPanel3, 32, 22, 0.0F, 3.0F, 0.0F, 12, 6, 1, 0.0F, false));
         this.leftPanel3.rotateAngleY = -0.6F;
         this.leftPanel3.rotateAngleZ = -0.15F;
         this.leftWing.addChild(this.leftPanel3);
      }

      public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
         this.rightWing.render(scale);
         this.leftWing.render(scale);
      }

      public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
         float flapCycle = MathHelper.sin(ageInTicks * 0.08F) * 0.12F;
         this.rightPanel1.rotateAngleZ = -0.3F + flapCycle;
         this.rightPanel2.rotateAngleZ = -0.05F + flapCycle * 0.8F;
         this.rightPanel3.rotateAngleZ = 0.15F + flapCycle * 0.6F;
         this.leftPanel1.rotateAngleZ = 0.3F - flapCycle;
         this.leftPanel2.rotateAngleZ = 0.05F - flapCycle * 0.8F;
         this.leftPanel3.rotateAngleZ = -0.15F - flapCycle * 0.6F;
         float sway = MathHelper.sin(ageInTicks * 0.05F) * 0.04F;
         this.rightWing.rotateAngleX = sway;
         this.leftWing.rotateAngleX = sway;
      }

      public void syncBodyRotation(ModelBiped mainModel) {
         ModelRenderer var10000 = this.rightWing;
         var10000.rotateAngleX += mainModel.bipedBody.rotateAngleX;
         var10000 = this.leftWing;
         var10000.rotateAngleX += mainModel.bipedBody.rotateAngleX;
         var10000 = this.rightWing;
         var10000.rotateAngleY += mainModel.bipedBody.rotateAngleY;
         var10000 = this.leftWing;
         var10000.rotateAngleY += mainModel.bipedBody.rotateAngleY;
         this.rightWing.rotateAngleZ = mainModel.bipedBody.rotateAngleZ;
         this.leftWing.rotateAngleZ = mainModel.bipedBody.rotateAngleZ;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class LayerPaperWings implements LayerRenderer<EntityCustom> {
      private static final ResourceLocation WINGS_TEXTURE = new ResourceLocation("narutomod:textures/paperwings.png");
      private final RenderLiving<?> renderer;
      private final ModelPaperWings wingsModel = new ModelPaperWings();

      public LayerPaperWings(RenderLiving<?> renderer) {
         this.renderer = renderer;
      }

      public void doRenderLayer(EntityCustom entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         this.renderer.bindTexture(WINGS_TEXTURE);
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 0.85F);
         if (entity.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
         }

         this.wingsModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
         ModelBase mainModel = this.renderer.getMainModel();
         if (mainModel instanceof ModelBiped) {
            this.wingsModel.syncBodyRotation((ModelBiped)mainModel);
         }

         this.wingsModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }

      public boolean shouldCombineTextures() {
         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class PaperArrowRenderer extends Render<EntityPaperArrowProjectile> {
      private static final ResourceLocation PAPER_TEXTURE = new ResourceLocation("narutomod:textures/paper_arrow.png");
      private final ModelPaperArrow model = new ModelPaperArrow();

      public PaperArrowRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityPaperArrowProjectile entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         GlStateManager.translate(x, y, z);
         float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
         float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
         GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(pitch - 180.0F, 1.0F, 0.0F, 0.0F);
         float flutter = MathHelper.sin(((float)entity.ticksExisted + partialTicks) * 0.5F) * 8.0F;
         GlStateManager.rotate(flutter, 0.0F, 0.0F, 1.0F);
         GlStateManager.scale(0.6F, 0.6F, 0.6F);
         GlStateManager.enableBlend();
         GlStateManager.disableCull();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
         this.model.render(entity, 0.0F, 0.0F, (float)entity.ticksExisted + partialTicks, 0.0F, 0.0F, 0.0625F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected ResourceLocation getEntityTexture(EntityPaperArrowProjectile entity) {
         return PAPER_TEXTURE;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelPaperArrow extends ModelBase {
      private final ModelRenderer sheet;

      public ModelPaperArrow() {
         this.textureWidth = 32;
         this.textureHeight = 32;
         this.sheet = new ModelRenderer(this);
         this.sheet.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.sheet.cubeList.add(new ModelBox(this.sheet, 0, 0, -3.0F, -0.25F, -8.0F, 6, 1, 16, 0.0F, false));
      }

      public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
         this.sheet.render(f5);
      }
   }

   public static class EntityPaperArrowProjectile extends EntityThrowable {
      private int lifetime = 0;
      private float damage = 25.0F;
      private float trueDamage = 15.0F;

      public EntityPaperArrowProjectile(World world) {
         super(world);
         this.setSize(0.4F, 0.2F);
      }

      public EntityPaperArrowProjectile(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(0.4F, 0.2F);
      }

      public EntityPaperArrowProjectile(World world, EntityLivingBase thrower, float normalDmg, float trueDmg) {
         super(world, thrower);
         this.setSize(0.4F, 0.2F);
         this.damage = normalDmg;
         this.trueDamage = trueDmg;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.thrower) {
               result.entityHit.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.thrower), this.damage);
               if (this.trueDamage > 0.0F && result.entityHit instanceof EntityLivingBase) {
                  result.entityHit.hurtResistantTime = 0;
                  result.entityHit.attackEntityFrom(DamageSource.MAGIC, this.trueDamage);
               }

               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, result.entityHit.posX, result.entityHit.posY + (double)1.0F, result.entityHit.posZ, 12, 0.3, 0.4, 0.3, 0.05, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.END_ROD, result.entityHit.posX, result.entityHit.posY + (double)0.5F, result.entityHit.posZ, 6, 0.2, 0.3, 0.2, 0.02, new int[0]);
               }
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY, this.posZ, 8, 0.2, 0.2, 0.2, 0.04, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARROW_HIT, SoundCategory.HOSTILE, 0.6F, 1.6F);
            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 60) {
            this.setDead();
         }

         if (this.world instanceof WorldServer && this.lifetime % 2 == 0) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY, this.posZ, 1, 0.02, 0.02, 0.02, 0.005, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.END_ROD, this.posX, this.posY, this.posZ, 1, 0.05, 0.05, 0.05, (double)0.0F, new int[0]);
         }

      }

      protected float getGravityVelocity() {
         return 0.01F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setFloat("paperDmg", this.damage);
         compound.setFloat("paperTrue", this.trueDamage);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("paperDmg")) {
            this.damage = compound.getFloat("paperDmg");
         }

         if (compound.hasKey("paperTrue")) {
            this.trueDamage = compound.getFloat("paperTrue");
         }

      }
   }

   public static class EntityCustom extends QuestNpcBase {
      private boolean paperShieldActive = false;
      private boolean paperShieldTriggered = false;
      private int paperArrowCooldown = 0;
      private int paperBurstCooldown = 0;
      private int paperStormCooldown = 0;
      private int paperBombCooldown = 0;
      private int paperBarrageCooldown = 0;
      private int barrageArrowsLeft = 0;
      private int barrageTickDelay = 0;
      private EntityLivingBase barrageTarget = null;
      private boolean konanIntroPlayed = false;
      private static final float PAPER_SHIELD_HP_THRESHOLD = 0.5F;
      private static final float PAPER_SHIELD_REDUCTION = 0.15F;
      private static final int PAPER_ARROW_BASE_CD = 40;
      private static final int PAPER_BURST_BASE_CD = 200;
      private static final int PAPER_STORM_BASE_CD = 180;
      private static final int PAPER_BOMB_BASE_CD = 280;
      private static final int PAPER_BARRAGE_BASE_CD = 240;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processKonanCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.paperArrowCooldown > 0) {
            --this.paperArrowCooldown;
         }

         if (this.paperBurstCooldown > 0) {
            --this.paperBurstCooldown;
         }

         if (this.paperStormCooldown > 0) {
            --this.paperStormCooldown;
         }

         if (this.paperBombCooldown > 0) {
            --this.paperBombCooldown;
         }

         if (this.paperBarrageCooldown > 0) {
            --this.paperBarrageCooldown;
         }

         if (this.barrageArrowsLeft > 0 && this.barrageTarget != null && this.barrageTarget.isEntityAlive()) {
            if (this.barrageTickDelay <= 0) {
               this.fireBarrageArrow(this.barrageTarget);
               --this.barrageArrowsLeft;
               this.barrageTickDelay = 3;
               if (this.barrageArrowsLeft <= 0) {
                  this.barrageTarget = null;
               }
            } else {
               --this.barrageTickDelay;
            }
         } else if (this.barrageArrowsLeft > 0) {
            this.barrageArrowsLeft = 0;
            this.barrageTarget = null;
         }

      }

      protected void resetCombatState() {
         this.paperShieldActive = false;
         this.paperShieldTriggered = false;
         this.konanIntroPlayed = false;
         this.paperArrowCooldown = 0;
         this.paperBurstCooldown = 0;
         this.paperStormCooldown = 0;
         this.paperBombCooldown = 0;
         this.paperBarrageCooldown = 0;
         this.barrageArrowsLeft = 0;
         this.barrageTickDelay = 0;
         this.barrageTarget = null;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("paperShieldActive", this.paperShieldActive);
         compound.setBoolean("paperShieldTriggered", this.paperShieldTriggered);
         compound.setBoolean("konanIntroPlayed", this.konanIntroPlayed);
         compound.setInteger("paperArrowCooldown", this.paperArrowCooldown);
         compound.setInteger("paperBurstCooldown", this.paperBurstCooldown);
         compound.setInteger("paperStormCooldown", this.paperStormCooldown);
         compound.setInteger("paperBombCooldown", this.paperBombCooldown);
         compound.setInteger("paperBarrageCooldown", this.paperBarrageCooldown);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.paperShieldActive = compound.getBoolean("paperShieldActive");
         this.paperShieldTriggered = compound.getBoolean("paperShieldTriggered");
         this.konanIntroPlayed = compound.getBoolean("konanIntroPlayed");
         this.paperArrowCooldown = compound.hasKey("paperArrowCooldown") ? compound.getInteger("paperArrowCooldown") : 0;
         this.paperBurstCooldown = compound.hasKey("paperBurstCooldown") ? compound.getInteger("paperBurstCooldown") : 0;
         this.paperStormCooldown = compound.hasKey("paperStormCooldown") ? compound.getInteger("paperStormCooldown") : 0;
         this.paperBombCooldown = compound.hasKey("paperBombCooldown") ? compound.getInteger("paperBombCooldown") : 0;
         this.paperBarrageCooldown = compound.hasKey("paperBarrageCooldown") ? compound.getInteger("paperBarrageCooldown") : 0;
      }

      protected void onCombatDeath() {
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 60, (double)1.0F, (double)1.5F, (double)1.0F, 0.15, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.END_ROD, this.posX, this.posY + (double)1.0F, this.posZ, 30, 0.8, 1.2, 0.8, 0.08, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_FIREWORK_LARGE_BLAST, SoundCategory.HOSTILE, 1.2F, 1.5F);
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         return this.paperShieldActive ? amount * 0.85F : amount;
      }

      private void processKonanCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            float hpPercent = this.getHealth() / this.getMaxHealth();
            if (!this.konanIntroPlayed) {
               this.konanIntroPlayed = true;
               this.konanBroadcast("§d Konan: §fI am God's angel. You will know pain through paper.");
            }

            if (!this.paperShieldTriggered && hpPercent <= 0.5F) {
               this.paperShieldTriggered = true;
               this.paperShieldActive = true;
               this.konanBroadcast("§d Konan: §fPaper Shield... my body becomes one with paper.");
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.0F, this.posZ, 40, 0.8, 1.2, 0.8, 0.1, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.END_ROD, this.posX, this.posY + (double)1.5F, this.posZ, 20, (double)0.5F, 0.8, (double)0.5F, 0.05, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDEREYE_DEATH, SoundCategory.HOSTILE, 1.2F, 1.8F);
            }

            if (this.paperShieldActive && this.ticksExisted % 5 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + 1.2, this.posZ, 3, 0.4, 0.6, 0.4, 0.02, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.END_ROD, this.posX, this.posY + (double)1.5F, this.posZ, 2, 0.3, 0.4, 0.3, 0.01, new int[0]);
            }

            if (dist > (double)20.0F) {
               this.getNavigator().tryMoveToEntityLiving(target, 1.1);
            } else if (this.paperStormCooldown <= 0 && dist < (double)15.0F && this.rand.nextFloat() < 0.3F) {
               System.out.println("[KONAN DEBUG] Triggering Paper Storm! dist=" + dist + " target=" + target.getName());
               this.firePaperStorm();
               this.paperStormCooldown = this.cdMul(180);
            } else if (this.paperBombCooldown <= 0 && dist >= (double)8.0F && dist <= (double)18.0F && this.rand.nextFloat() < 0.25F) {
               System.out.println("[KONAN DEBUG] Triggering Paper Bomb! dist=" + dist + " target=" + target.getName());
               this.firePaperBomb(target);
               this.paperBombCooldown = this.cdMul(280);
            } else if (this.paperBarrageCooldown <= 0 && this.barrageArrowsLeft <= 0 && dist >= (double)6.0F && dist <= (double)16.0F && this.rand.nextFloat() < 0.22F) {
               this.startPaperBarrage(target);
               this.paperBarrageCooldown = this.cdMul(240);
            } else if (dist < (double)4.0F) {
               double dx = this.posX - target.posX;
               double dz = this.posZ - target.posZ;
               double horizDist = Math.sqrt(dx * dx + dz * dz);
               if (horizDist > (double)0.5F) {
                  double nx = dx / horizDist;
                  double nz = dz / horizDist;
                  this.getNavigator().tryMoveToXYZ(this.posX + nx * (double)6.0F, this.posY, this.posZ + nz * (double)6.0F, 1.2);
               }

               if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= (double)2.5F) {
                  this.performMeleeSwing(target);
               }

            } else if (this.paperBurstCooldown <= 0 && dist >= (double)8.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.2F) {
               this.firePaperBurst(target);
               this.paperBurstCooldown = this.cdMul(200);
            } else if (this.paperArrowCooldown <= 0 && dist >= (double)8.0F && dist <= (double)20.0F) {
               this.firePaperArrow(target);
               this.paperArrowCooldown = this.cdMul(this.paperShieldActive ? 30 : 40);
            } else if (this.paperArrowCooldown <= 0 && dist >= (double)4.0F && dist < (double)8.0F && this.rand.nextFloat() < 0.3F) {
               this.firePaperArrow(target);
               this.paperArrowCooldown = this.cdMul(this.paperShieldActive ? 35 : 50);
            } else {
               if (dist >= (double)8.0F && dist <= (double)20.0F) {
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

                     this.getNavigator().tryMoveToXYZ(this.posX + strafeX * (double)4.0F, this.posY, this.posZ + strafeZ * (double)4.0F, 0.9);
                  }
               } else {
                  this.getNavigator().tryMoveToEntityLiving(target, (double)1.0F);
               }

               if (this.natureType != 0 && this.jutsuCooldown <= 0 && dist >= (double)5.0F && dist <= (double)18.0F && this.rand.nextFloat() < 0.12F) {
                  this.startNatureJutsu(target);
                  this.jutsuCooldown = this.cdMul(this.paperShieldActive ? 70 : 100);
               }

            }
         }
      }

      private void firePaperArrow(EntityLivingBase target) {
         double dmgMult = this.getDamageMultiplier();
         float normalDmg = 25.0F * (float)dmgMult;
         if (this.paperShieldActive) {
            normalDmg *= 1.1F;
         }

         float trueDmg = (this.combatTier >= 4 ? 18.0F : (this.combatTier >= 3 ? 15.0F : 12.0F)) * this.trueDamageMultiplier;
         EntityPaperArrowProjectile arrow = new EntityPaperArrowProjectile(this.world, this, normalDmg, trueDmg);
         double dx = target.posX - this.posX;
         double dy = target.posY + (double)target.getEyeHeight() - 0.1 - arrow.posY;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         arrow.shoot(dx, dy + horizDist * 0.04, dz, 1.8F, 1.2F);
         this.world.spawnEntity(arrow);
         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.HOSTILE, 0.7F, 1.8F);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.5F, this.posZ, 6, 0.2, 0.2, 0.2, 0.03, new int[0]);
         }

         if (this.rand.nextFloat() < 0.25F) {
            this.konanBroadcast("§dKonan: §fPaper Shuriken!");
         }

      }

      private void firePaperBurst(EntityLivingBase target) {
         this.konanBroadcast("§dKonan: §fDance of the Shikigami!");
         double dmgMult = this.getDamageMultiplier();
         float normalDmg = 18.0F * (float)dmgMult;
         if (this.paperShieldActive) {
            normalDmg *= 1.1F;
         }

         float trueDmg = (this.combatTier >= 4 ? 12.0F : (this.combatTier >= 3 ? 10.0F : 8.0F)) * this.trueDamageMultiplier;

         for(int i = 0; i < 3; ++i) {
            EntityPaperArrowProjectile arrow = new EntityPaperArrowProjectile(this.world, this, normalDmg, trueDmg);
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.getEyeHeight() - 0.1 - arrow.posY;
            double dz = target.posZ - this.posZ;
            double horizDist = Math.sqrt(dx * dx + dz * dz);
            float spread = 2.5F + (float)i * 1.5F;
            arrow.shoot(dx, dy + horizDist * 0.04, dz, 1.8F, spread);
            this.world.spawnEntity(arrow);
         }

         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.HOSTILE, 1.0F, 2.0F);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.5F, this.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.08, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.END_ROD, this.posX, this.posY + 1.2, this.posZ, 10, 0.3, 0.4, 0.3, 0.04, new int[0]);
         }

      }

      private void firePaperStorm() {
         this.konanBroadcast("§dKonan: §fPaper Style: Dance of the Shikigami!");
         System.out.println("[KONAN DEBUG] firePaperStorm called, pos=" + this.posX + "," + this.posY + "," + this.posZ);
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 80; ++i) {
               double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double elevation = (this.rand.nextDouble() - (double)0.5F) * Math.PI;
               double r = (double)6.0F + this.rand.nextDouble() * (double)2.5F;
               double px = this.posX + Math.cos(angle) * Math.cos(elevation) * r;
               double py = this.posY + (double)1.0F + Math.sin(elevation) * r;
               double pz = this.posZ + Math.sin(angle) * Math.cos(elevation) * r;
               ws.spawnParticle(EnumParticleTypes.CLOUD, px, py, pz, 1, 0.1, 0.1, 0.1, 0.02, new int[0]);
            }

            for(int i = 0; i < 40; ++i) {
               double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double elevation = (this.rand.nextDouble() - (double)0.5F) * Math.PI;
               double r = (double)4.0F + this.rand.nextDouble() * (double)4.0F;
               double px = this.posX + Math.cos(angle) * Math.cos(elevation) * r;
               double py = this.posY + (double)1.0F + Math.sin(elevation) * r;
               double pz = this.posZ + Math.sin(angle) * Math.cos(elevation) * r;
               ws.spawnParticle(EnumParticleTypes.END_ROD, px, py, pz, 1, 0.05, 0.05, 0.05, 0.01, new int[0]);
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_FIREWORK_LARGE_BLAST, SoundCategory.HOSTILE, 1.2F, 1.6F);
         AxisAlignedBB stormBox = new AxisAlignedBB(this.posX - (double)8.0F, this.posY - (double)4.0F, this.posZ - (double)8.0F, this.posX + (double)8.0F, this.posY + (double)8.0F, this.posZ + (double)8.0F);
         List<EntityLivingBase> nearby = this.world.getEntitiesWithinAABB(EntityLivingBase.class, stormBox, (e) -> e != this && e.isEntityAlive() && !(e instanceof QuestNpcBase));
         System.out.println("[KONAN DEBUG] Paper Storm found " + nearby.size() + " targets in 8-block radius");

         for(EntityLivingBase victim : nearby) {
            float damage = 50.0F;
            float hpBefore = victim.getHealth();
            victim.hurtResistantTime = 0;
            boolean hit = victim.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
            System.out.println("[KONAN DEBUG] Paper Storm -> " + victim.getName() + " hit=" + hit + " hpBefore=" + hpBefore + " hpAfter=" + victim.getHealth());
            if (!hit && victim.isEntityAlive()) {
               float newHp = Math.max(0.0F, victim.getHealth() - damage);
               victim.setHealth(newHp);
               System.out.println("[KONAN DEBUG] Paper Storm FALLBACK direct HP set on " + victim.getName() + " to " + newHp);
            }

            victim.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 0));
         }

      }

      private void firePaperBomb(EntityLivingBase target) {
         this.konanBroadcast("§dKonan: §fPaper Bomb Detonation!");
         double tx = target.posX;
         double ty = target.posY + (double)0.5F;
         double tz = target.posZ;
         System.out.println("[KONAN DEBUG] firePaperBomb called, target=" + target.getName() + " at " + tx + "," + ty + "," + tz);
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, tx, ty, tz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.FLAME, tx, ty, tz, 30, (double)1.5F, (double)1.0F, (double)1.5F, 0.1, new int[0]);
            ws.spawnParticle(EnumParticleTypes.CLOUD, tx, ty + (double)0.5F, tz, 20, (double)1.0F, 0.8, (double)1.0F, 0.08, new int[0]);
            ws.spawnParticle(EnumParticleTypes.END_ROD, tx, ty + (double)1.0F, tz, 15, 0.8, 0.6, 0.8, 0.05, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, tx, ty, tz, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 1.2F);
         float bombDmg = 60.0F;
         float hpBefore = target.getHealth();
         target.hurtResistantTime = 0;
         boolean hit = target.attackEntityFrom(DamageSource.causeMobDamage(this), bombDmg);
         System.out.println("[KONAN DEBUG] Paper Bomb direct -> " + target.getName() + " hit=" + hit + " hpBefore=" + hpBefore + " hpAfter=" + target.getHealth());
         if (!hit && target.isEntityAlive()) {
            float newHp = Math.max(0.0F, target.getHealth() - bombDmg);
            target.setHealth(newHp);
            System.out.println("[KONAN DEBUG] Paper Bomb FALLBACK direct HP set on " + target.getName() + " to " + newHp);
         }

         List<EntityLivingBase> splash = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(tx - (double)4.0F, ty - (double)2.0F, tz - (double)4.0F, tx + (double)4.0F, ty + (double)3.0F, tz + (double)4.0F), (e) -> e != this && e != target && e.isEntityAlive() && !(e instanceof QuestNpcBase));
         System.out.println("[KONAN DEBUG] Paper Bomb splash found " + splash.size() + " targets");

         for(EntityLivingBase victim : splash) {
            float splashDmg = 35.0F;
            float victimHpBefore = victim.getHealth();
            victim.hurtResistantTime = 0;
            boolean splashHit = victim.attackEntityFrom(DamageSource.causeMobDamage(this), splashDmg);
            System.out.println("[KONAN DEBUG] Paper Bomb splash -> " + victim.getName() + " hit=" + splashHit + " hpBefore=" + victimHpBefore + " hpAfter=" + victim.getHealth());
            if (!splashHit && victim.isEntityAlive()) {
               float newHp = Math.max(0.0F, victim.getHealth() - splashDmg);
               victim.setHealth(newHp);
               System.out.println("[KONAN DEBUG] Paper Bomb splash FALLBACK on " + victim.getName() + " to " + newHp);
            }
         }

      }

      private void startPaperBarrage(EntityLivingBase target) {
         this.konanBroadcast("§dKonan: §fPaper Barrage!");
         this.barrageArrowsLeft = 6;
         this.barrageTickDelay = 0;
         this.barrageTarget = target;
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.5F, this.posZ, 15, 0.4, (double)0.5F, 0.4, 0.06, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.END_ROD, this.posX, this.posY + 1.2, this.posZ, 8, 0.3, 0.3, 0.3, 0.03, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.HOSTILE, 0.9F, 1.6F);
         this.swingArm(EnumHand.MAIN_HAND);
      }

      private void fireBarrageArrow(EntityLivingBase target) {
         if (!this.world.isRemote) {
            double dmgMult = this.getDamageMultiplier();
            float normalDmg = 20.0F * (float)dmgMult;
            if (this.paperShieldActive) {
               normalDmg *= 1.1F;
            }

            float trueDmg = (this.combatTier >= 4 ? 14.0F : (this.combatTier >= 3 ? 11.0F : 9.0F)) * this.trueDamageMultiplier;
            EntityPaperArrowProjectile arrow = new EntityPaperArrowProjectile(this.world, this, normalDmg, trueDmg);
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.getEyeHeight() - 0.1 - arrow.posY;
            double dz = target.posZ - this.posZ;
            double horizDist = Math.sqrt(dx * dx + dz * dz);
            float spread = 1.5F + this.rand.nextFloat() * 2.0F;
            arrow.shoot(dx, dy + horizDist * 0.04, dz, 2.0F, spread);
            this.world.spawnEntity(arrow);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.HOSTILE, 0.4F, 1.9F + this.rand.nextFloat() * 0.3F);
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CLOUD, this.posX, this.posY + (double)1.5F, this.posZ, 2, 0.15, 0.15, 0.15, 0.02, new int[0]);
            }

         }
      }

      private void konanBroadcast(String msg) {
         for(EntityPlayer p : this.world.playerEntities) {
            if ((double)p.getDistance(this) < (double)48.0F && p instanceof EntityPlayerMP) {
               ((EntityPlayerMP)p).sendMessage(new TextComponentString(msg));
            }
         }

      }
   }
}
