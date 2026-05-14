
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.jutsu.EntityWaterFangBullet;
import net.luck.narutoaddon.OtherCode.jutsu.EntityWaterNeedles;
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
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
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

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKisameNPC extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 316;
   public static final int ENTITYID_SHARK = 317;

   public EntityKisameNPC(ElementsInfTsukAddon instance) {
      super(instance, 316);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kisamenpc"), 316).name("kisamenpc").tracker(64, 3, true).egg(-1, -1).build());
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityWaterSharkProjectile.class).id(new ResourceLocation("inftsukaddon", "water_shark_proj"), 317).name("water_shark_proj").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KisameNpcRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(EntityWaterSharkProjectile.class, WaterSharkRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class KisameNpcRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon:textures/bandit1.png");

      public KisameNpcRenderer(RenderManager renderManager) {
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
            GlStateManager.color(0.4F, 0.6F, 1.0F, alpha);
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
   public static class WaterSharkRenderer extends Render<EntityWaterSharkProjectile> {
      private static final ResourceLocation SHARK_TEXTURE = new ResourceLocation("narutomod:textures/shark.png");
      private final ModelShark model = new ModelShark();

      public WaterSharkRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityWaterSharkProjectile entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         GlStateManager.translate(x, y, z);
         float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
         float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
         GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(pitch - 180.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.scale(0.5F, 0.5F, 0.5F);
         GlStateManager.enableBlend();
         GlStateManager.disableCull();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.color(0.3F, 0.5F, 1.0F, 0.75F);
         this.model.render(entity, 0.0F, 0.0F, (float)entity.ticksExisted + partialTicks, 0.0F, 0.0F, 0.0625F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected ResourceLocation getEntityTexture(EntityWaterSharkProjectile entity) {
         return SHARK_TEXTURE;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelShark extends ModelBase {
      private final ModelRenderer body;
      private final ModelRenderer head;
      private final ModelRenderer foreHead;
      private final ModelRenderer jaw;
      private final ModelRenderer tail;
      private final ModelRenderer tailFin;
      private final ModelRenderer tailFinUpper;
      private final ModelRenderer tailFinLower;
      private final ModelRenderer backFin;
      private final ModelRenderer leftFin;
      private final ModelRenderer rightFin;

      public ModelShark() {
         this.textureWidth = 64;
         this.textureHeight = 64;
         this.body = new ModelRenderer(this);
         this.body.setRotationPoint(0.0F, 0.0F, -5.0F);
         this.body.cubeList.add(new ModelBox(this.body, 0, 0, -4.0F, -7.0F, 0.0F, 8, 7, 13, 0.0F, false));
         this.head = new ModelRenderer(this);
         this.head.setRotationPoint(0.0F, -3.0F, 0.0F);
         this.body.addChild(this.head);
         this.foreHead = new ModelRenderer(this);
         this.foreHead.setRotationPoint(0.0F, -3.5F, 0.0F);
         this.head.addChild(this.foreHead);
         this.setRotationAngle(this.foreHead, 0.1745F, 0.0F, 0.0F);
         this.foreHead.cubeList.add(new ModelBox(this.foreHead, 19, 20, -4.0F, 0.0F, -6.0F, 8, 4, 6, 0.0F, false));
         this.jaw = new ModelRenderer(this);
         this.jaw.setRotationPoint(0.0F, 1.5F, 0.25F);
         this.head.addChild(this.jaw);
         this.jaw.cubeList.add(new ModelBox(this.jaw, 29, 0, -3.5F, -1.5F, -4.75F, 7, 2, 5, 0.0F, false));
         this.tail = new ModelRenderer(this);
         this.tail.setRotationPoint(0.0F, -3.5F, 13.0F);
         this.body.addChild(this.tail);
         this.tail.cubeList.add(new ModelBox(this.tail, 0, 20, -2.0F, -2.5F, -1.0F, 4, 5, 11, 0.0F, false));
         this.tailFin = new ModelRenderer(this);
         this.tailFin.setRotationPoint(0.0F, -0.5F, 8.0F);
         this.tail.addChild(this.tailFin);
         this.tailFinUpper = new ModelRenderer(this);
         this.tailFinUpper.setRotationPoint(0.0F, -1.0F, 1.0F);
         this.tailFin.addChild(this.tailFinUpper);
         this.setRotationAngle(this.tailFinUpper, -0.6109F, 0.0F, 0.0F);
         this.tailFinUpper.cubeList.add(new ModelBox(this.tailFinUpper, 0, 20, -0.5F, -6.9924F, -1.1743F, 1, 8, 3, 0.0F, false));
         this.tailFinLower = new ModelRenderer(this);
         this.tailFinLower.setRotationPoint(0.0F, 1.0F, 1.0F);
         this.tailFin.addChild(this.tailFinLower);
         this.setRotationAngle(this.tailFinLower, 0.5236F, 0.0F, 0.0F);
         this.tailFinLower.cubeList.add(new ModelBox(this.tailFinLower, 0, 36, -0.5F, -1.4924F, -1.0403F, 1, 6, 3, 0.0F, false));
         this.backFin = new ModelRenderer(this);
         this.backFin.setRotationPoint(0.0F, -6.0F, 6.0F);
         this.body.addChild(this.backFin);
         this.setRotationAngle(this.backFin, -0.5236F, 0.0F, 0.0F);
         this.backFin.cubeList.add(new ModelBox(this.backFin, 0, 0, -0.5F, -7.75F, -1.5F, 1, 8, 4, 0.0F, false));
         this.leftFin = new ModelRenderer(this);
         this.leftFin.setRotationPoint(3.0F, -3.0F, 8.0F);
         this.body.addChild(this.leftFin);
         this.setRotationAngle(this.leftFin, 0.9599F, 0.0F, 1.8675F);
         this.leftFin.cubeList.add(new ModelBox(this.leftFin, 32, 34, 0.0F, -4.0F, -1.5F, 1, 4, 7, 0.0F, false));
         this.rightFin = new ModelRenderer(this);
         this.rightFin.setRotationPoint(-3.0F, -3.0F, 8.0F);
         this.body.addChild(this.rightFin);
         this.setRotationAngle(this.rightFin, 0.9599F, 0.0F, -1.8675F);
         this.rightFin.cubeList.add(new ModelBox(this.rightFin, 32, 34, -1.0F, -4.0F, -1.5F, 1, 4, 7, 0.0F, false));
      }

      public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
         this.tail.rotateAngleY = MathHelper.cos(f2 * 0.15F) * 0.25F;
         this.tailFin.rotateAngleY = MathHelper.cos(f2 * 0.15F) * 0.2F;
         this.body.render(f5);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }
   }

   public static class EntityWaterSharkProjectile extends EntityThrowable {
      private int lifetime = 0;
      private float damage = 10.0F;
      private float trueDamage = 3.0F;

      public EntityWaterSharkProjectile(World world) {
         super(world);
         this.setSize(0.6F, 0.4F);
      }

      public EntityWaterSharkProjectile(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(0.6F, 0.4F);
      }

      public EntityWaterSharkProjectile(World world, EntityLivingBase thrower, float normalDmg, float trueDmg) {
         super(world, thrower);
         this.setSize(0.6F, 0.4F);
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
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, result.entityHit.posX, result.entityHit.posY + (double)1.0F, result.entityHit.posZ, 20, 0.4, 0.6, 0.4, 0.1, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_BUBBLE, result.entityHit.posX, result.entityHit.posY + (double)0.5F, result.entityHit.posZ, 10, 0.3, 0.4, 0.3, 0.05, new int[0]);
               }
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY, this.posZ, 15, 0.3, 0.3, 0.3, 0.08, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 0.8F, 1.3F);
            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 80) {
            this.setDead();
         }

         if (this.world instanceof WorldServer && this.lifetime % 2 == 0) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, this.posY, this.posZ, 3, 0.05, 0.05, 0.05, 0.02, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.DRIP_WATER, this.posX, this.posY, this.posZ, 1, 0.1, 0.1, 0.1, (double)0.0F, new int[0]);
         }

      }

      protected float getGravityVelocity() {
         return 0.015F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setFloat("sharkDmg", this.damage);
         compound.setFloat("sharkTrue", this.trueDamage);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("sharkDmg")) {
            this.damage = compound.getFloat("sharkDmg");
         }

         if (compound.hasKey("sharkTrue")) {
            this.trueDamage = compound.getFloat("sharkTrue");
         }

      }
   }

   public static class EntityCustom extends QuestNpcBase {
      private boolean sharkFusionActive = false;
      private boolean sharkFusionTriggered = false;
      private int waterFieldCooldown = 0;
      private int waterSharkCooldown = 0;
      private int samehadaCooldown = 0;
      private int waterFangCD = 0;
      private int waterNeedlesCD = 0;
      private boolean kisameIntroPlayed = false;
      private static final float SHARK_FUSION_HP_THRESHOLD = 0.4F;
      private static final float SHARK_FUSION_DAMAGE_MULT = 1.3F;
      private static final float SHARK_FUSION_SPEED_MULT = 1.2F;
      private static final float SHARK_FUSION_REDUCTION = 0.25F;
      private static final int WATER_FIELD_BASE_CD = 250;
      private static final int WATER_FIELD_SLOWNESS_DURATION = 80;
      private static final int WATER_FIELD_SPEED_DURATION = 80;
      private static final int WATER_FANG_BASE_CD = 300;
      private static final int WATER_NEEDLES_BASE_CD = 400;

      public EntityCustom(World world) {
         super(world);
      }

      protected void processCombat(EntityLivingBase target, double dist) {
         this.processKisameCombat(target, dist);
      }

      protected void tickStyleCooldowns() {
         if (this.waterFieldCooldown > 0) {
            --this.waterFieldCooldown;
         }

         if (this.waterSharkCooldown > 0) {
            --this.waterSharkCooldown;
         }

         if (this.samehadaCooldown > 0) {
            --this.samehadaCooldown;
         }

         if (this.waterFangCD > 0) {
            --this.waterFangCD;
         }

         if (this.waterNeedlesCD > 0) {
            --this.waterNeedlesCD;
         }

      }

      protected void resetCombatState() {
         this.sharkFusionActive = false;
         this.sharkFusionTriggered = false;
         this.kisameIntroPlayed = false;
         this.waterFieldCooldown = 0;
         this.waterSharkCooldown = 0;
         this.samehadaCooldown = 0;
         this.waterFangCD = 0;
         this.waterNeedlesCD = 0;
      }

      protected void writeCombatNBT(NBTTagCompound compound) {
         compound.setBoolean("sharkFusionActive", this.sharkFusionActive);
         compound.setBoolean("sharkFusionTriggered", this.sharkFusionTriggered);
         compound.setBoolean("kisameIntroPlayed", this.kisameIntroPlayed);
         compound.setInteger("waterFieldCooldown", this.waterFieldCooldown);
         compound.setInteger("waterSharkCooldown", this.waterSharkCooldown);
         compound.setInteger("samehadaCooldown", this.samehadaCooldown);
         compound.setInteger("waterFangCD", this.waterFangCD);
         compound.setInteger("waterNeedlesCD", this.waterNeedlesCD);
      }

      protected void readCombatNBT(NBTTagCompound compound) {
         this.sharkFusionActive = compound.getBoolean("sharkFusionActive");
         this.sharkFusionTriggered = compound.getBoolean("sharkFusionTriggered");
         this.kisameIntroPlayed = compound.getBoolean("kisameIntroPlayed");
         this.waterFieldCooldown = compound.hasKey("waterFieldCooldown") ? compound.getInteger("waterFieldCooldown") : 0;
         this.waterSharkCooldown = compound.hasKey("waterSharkCooldown") ? compound.getInteger("waterSharkCooldown") : 0;
         this.samehadaCooldown = compound.hasKey("samehadaCooldown") ? compound.getInteger("samehadaCooldown") : 0;
         this.waterFangCD = compound.hasKey("waterFangCD") ? compound.getInteger("waterFangCD") : 0;
         this.waterNeedlesCD = compound.hasKey("waterNeedlesCD") ? compound.getInteger("waterNeedlesCD") : 0;
      }

      protected void onCombatDeath() {
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + (double)1.0F, this.posZ, 50, 0.8, 1.2, 0.8, 0.15, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, this.posY + (double)0.5F, this.posZ, 30, 0.6, 0.8, 0.6, 0.1, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 1.5F, 0.5F);
      }

      protected float onStyleDamage(DamageSource source, float amount) {
         return this.sharkFusionActive ? amount * 0.75F : amount;
      }

      private void processKisameCombat(EntityLivingBase target, double dist) {
         if (!this.world.isRemote && target != null) {
            float hpPercent = this.getHealth() / this.getMaxHealth();
            if (!this.kisameIntroPlayed) {
               this.kisameIntroPlayed = true;
               this.kisameBroadcast("§9Kisame: §bSamehada is eager to shave you to ribbons!");
            }

            if (!this.sharkFusionTriggered && hpPercent <= 0.4F) {
               this.sharkFusionTriggered = true;
               this.sharkFusionActive = true;
               this.kisameBroadcast("§9Kisame: §bThis is my true form... Shark Fusion!");
               if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
                  double curSpeed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
                  this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(curSpeed * (double)1.2F);
               }

               if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
                  double curDmg = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                  this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(curDmg * (double)1.3F);
               }

               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + (double)1.0F, this.posZ, 40, 0.6, (double)1.0F, 0.6, 0.12, new int[0]);
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.DRIP_WATER, this.posX, this.posY + (double)1.5F, this.posZ, 20, (double)0.5F, 0.8, (double)0.5F, (double)0.0F, new int[0]);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 1.5F, 0.6F);
            }

            if (this.sharkFusionActive && this.ticksExisted % 5 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, this.posY + 1.2, this.posZ, 4, 0.3, (double)0.5F, 0.3, 0.02, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.DRIP_WATER, this.posX, this.posY + 1.8, this.posZ, 2, 0.2, 0.3, 0.2, (double)0.0F, new int[0]);
            }

            if (this.waterFieldCooldown <= 0 && dist <= (double)12.0F && this.rand.nextFloat() < 0.3F) {
               this.waterFieldAttack(target);
               this.waterFieldCooldown = this.cdMul(250 + this.rand.nextInt(50));
            } else if (this.waterFangCD <= 0 && dist >= (double)10.0F && dist <= (double)18.0F && this.rand.nextFloat() < 0.3F) {
               this.fireWaterFangBullet(target);
               this.waterFangCD = this.cdMul(this.sharkFusionActive ? 240 : 300);
            } else if (this.waterNeedlesCD <= 0 && dist >= (double)8.0F && dist <= (double)16.0F && this.rand.nextFloat() < 0.25F) {
               this.fireWaterNeedles(target);
               this.waterNeedlesCD = this.cdMul(this.sharkFusionActive ? 320 : 400);
            } else if (this.waterSharkCooldown <= 0 && dist >= (double)8.0F && dist <= (double)25.0F && this.rand.nextFloat() < 0.35F) {
               this.fireWaterShark(target);
               this.waterSharkCooldown = this.cdMul(this.sharkFusionActive ? 40 : 60);
            } else if (this.waterSharkCooldown <= 0 && dist >= (double)5.0F && dist < (double)8.0F && this.rand.nextFloat() < 0.2F) {
               this.fireWaterShark(target);
               this.waterSharkCooldown = this.cdMul(this.sharkFusionActive ? 50 : 70);
            } else if (!this.isDashing && this.dashCooldown <= 0 && dist >= (double)8.0F && dist <= (double)20.0F && this.rand.nextFloat() < 0.25F) {
               this.kisameLunge(target, dist);
               this.dashCooldown = this.cdMul(this.sharkFusionActive ? 50 : 80);
            } else {
               double moveSpeed = this.sharkFusionActive ? 1.4 : 1.2;
               if (dist > (double)6.0F) {
                  this.getNavigator().tryMoveToEntityLiving(target, moveSpeed);
               } else if (dist > (double)2.5F) {
                  this.getNavigator().tryMoveToEntityLiving(target, moveSpeed * 0.85);
               }

               double meleeRange = this.sharkFusionActive ? 3.2 : (double)2.5F;
               if (this.meleeCooldown <= 0 && this.attackStaggerDelay <= 0 && dist <= meleeRange) {
                  this.performMeleeSwing(target);
                  if (target instanceof EntityLivingBase) {
                     target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 30, 0));
                  }

                  if (this.world instanceof WorldServer) {
                     ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, target.posX, target.posY + (double)1.0F, target.posZ, 8, 0.2, 0.3, 0.2, 0.05, new int[0]);
                  }
               }

               if (this.natureType != 0 && this.jutsuCooldown <= 0 && dist >= (double)5.0F && dist <= (double)18.0F && this.rand.nextFloat() < 0.15F) {
                  this.startNatureJutsu(target);
                  this.jutsuCooldown = this.cdMul(this.sharkFusionActive ? 60 : 100);
               }

            }
         }
      }

      private void fireWaterShark(EntityLivingBase target) {
         double dmgMult = this.getDamageMultiplier();
         float normalDmg = 10.0F * (float)dmgMult;
         if (this.sharkFusionActive) {
            normalDmg *= 1.3F;
         }

         float trueDmg = (this.combatTier >= 4 ? 5.0F : (this.combatTier >= 3 ? 4.0F : 3.0F)) * this.trueDamageMultiplier;
         EntityWaterSharkProjectile shark = new EntityWaterSharkProjectile(this.world, this, normalDmg, trueDmg);
         double dx = target.posX - this.posX;
         double dy = target.posY + (double)target.getEyeHeight() - 0.1 - shark.posY;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         shark.shoot(dx, dy + horizDist * 0.06, dz, 1.5F, 1.5F);
         this.world.spawnEntity(shark);
         this.swingArm(EnumHand.MAIN_HAND);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 0.8F, 1.4F);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + (double)1.5F, this.posZ, 10, 0.3, 0.3, 0.3, 0.06, new int[0]);
         }

         if (this.rand.nextFloat() < 0.3F) {
            this.kisameBroadcast("§9Kisame: §bSuiton: Water Shark Bomb!");
         }

      }

      private void waterFieldAttack(EntityLivingBase target) {
         if (target instanceof EntityLivingBase) {
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 0));
         }

         this.addPotionEffect(new PotionEffect(MobEffects.SPEED, 80, 0));
         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int i = 0; i < 16; ++i) {
               double angle = (double)i / (double)16.0F * Math.PI * (double)2.0F;
               double px = target.posX + Math.cos(angle) * (double)3.0F;
               double pz = target.posZ + Math.sin(angle) * (double)3.0F;
               ws.spawnParticle(EnumParticleTypes.DRIP_WATER, px, target.posY + (double)1.0F, pz, 3, 0.2, 0.4, 0.2, (double)0.0F, new int[0]);
               ws.spawnParticle(EnumParticleTypes.WATER_BUBBLE, px, target.posY + 0.3, pz, 2, 0.15, 0.2, 0.15, 0.01, new int[0]);
            }

            for(int j = 0; j < 5; ++j) {
               ws.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + (double)j * (double)0.5F, this.posZ, 6, (double)0.5F, 0.1, (double)0.5F, 0.03, new int[0]);
            }
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 1.2F, 0.7F);
         this.kisameBroadcast("§9Kisame: §bYou're in my domain now!");
         if (target instanceof EntityPlayerMP) {
            ((EntityPlayerMP)target).sendMessage(new TextComponentString("§9§o* The ground around you floods with water, slowing your movement! *"));
         }

      }

      private void fireWaterFangBullet(EntityLivingBase target) {
         EntityWaterFangBullet.EntityCustom fang = new EntityWaterFangBullet.EntityCustom(this.world, this, target.posX, target.posY, target.posZ, 5.0F);
         this.world.spawnEntity(fang);
         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + (double)1.0F, this.posZ, 12, 0.4, (double)0.5F, 0.4, 0.08, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 1.0F, 0.8F);
         this.kisameBroadcast("§9Kisame: §bWater Style: Water Fang Bullet!");
      }

      private void fireWaterNeedles(EntityLivingBase target) {
         for(int i = 0; i < 8; ++i) {
            EntityWaterNeedles.EntityCustom needle = new EntityWaterNeedles.EntityCustom(this.world);
            double angle = (Math.PI / 4D) * (double)i;
            double radius = (double)1.5F;
            double nx = target.posX + Math.cos(angle) * radius;
            double nz = target.posZ + Math.sin(angle) * radius;
            needle.setPosition(nx, target.posY + (double)1.5F, nz);
            needle.setCaster(this);
            needle.setCenterPoint(target.posX, target.posY, target.posZ);
            needle.setDamage(20.0F);
            needle.setSpawnDelay(5 + i * 1);
            if (i == 7) {
               needle.setLastNeedle(true);
            }

            this.world.spawnEntity(needle);
         }

         this.swingArm(EnumHand.MAIN_HAND);
         if (this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_BUBBLE, target.posX, target.posY + (double)1.0F, target.posZ, 20, (double)1.5F, (double)0.5F, (double)1.5F, 0.03, new int[0]);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, target.posX, target.posY + (double)0.5F, target.posZ, 15, (double)2.0F, 0.3, (double)2.0F, 0.05, new int[0]);
         }

         this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 1.2F, 1.5F);
         this.kisameBroadcast("§9Kisame: §bWater Style: Thousand Needles of Death!");
      }

      private void kisameLunge(EntityLivingBase target, double dist) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         if (!(horizDist < (double)0.5F)) {
            double nx = dx / horizDist;
            double nz = dz / horizDist;
            int duration = 8;
            double dashDist = Math.max(horizDist - (double)2.0F, (double)1.0F);
            double velocityPerTick = dashDist / (double)duration;
            this.isDashing = true;
            this.dashType = 2;
            this.dashDuration = duration;
            this.dashTicksRemaining = duration;
            this.dashVelX = nx * velocityPerTick * 1.4;
            this.dashVelZ = nz * velocityPerTick * 1.4;
            this.dashArcSustain = 0.05F;
            this.dashFallAccel = 0.06F;
            this.dashMaxFall = -0.35F;
            this.dashHasMidairGuidance = false;
            this.motionY = 0.3;
            this.velocityChanged = true;
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY + (double)1.0F, this.posZ, 15, 0.3, 0.3, 0.3, 0.1, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 1.1F);
         }
      }

      private void kisameBroadcast(String msg) {
         for(EntityPlayer p : this.world.playerEntities) {
            if ((double)p.getDistance(this) < (double)48.0F && p instanceof EntityPlayerMP) {
               ((EntityPlayerMP)p).sendMessage(new TextComponentString(msg));
            }
         }

      }
   }
}
