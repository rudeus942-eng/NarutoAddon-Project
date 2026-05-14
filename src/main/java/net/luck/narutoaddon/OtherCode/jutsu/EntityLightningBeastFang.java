
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.EntityRaitonBeam;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Particles;
import net.narutomod.Particles.Types;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemJutsu.JutsuEnum.Type;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityLightningBeastFang extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 272;

   public EntityLightningBeastFang(ElementsInfTsukAddon instance) {
      super(instance, 901);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "lightning_beast_fang"), 272).name("inftsuk_lightning_beast_fang").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, LightningBeastRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable implements ItemJutsu.IJutsu {
      private static final DataParameter<Float> MODEL_SCALE;
      private int lifetime = 0;
      private float damage = 10.0F;
      private Entity shooterEntity;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.6F, 0.6F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(0.6F, 0.6F);
         this.shooterEntity = thrower;
      }

      public Type getJutsuType() {
         return Type.RAITON;
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(MODEL_SCALE, 0.8F);
      }

      public float getEntityScale() {
         return (Float)this.dataManager.get(MODEL_SCALE);
      }

      public void setEntityScale(float scale) {
         this.dataManager.set(MODEL_SCALE, scale);
      }

      public void setDamage(float dmg) {
         this.damage = dmg;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.shooterEntity) {
               float cappedDmg = this.damage;
               if (!(result.entityHit instanceof EntityPlayer)) {
                  cappedDmg = Math.min(cappedDmg, 100.0F);
               }

               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooterEntity), cappedDmg);
               result.entityHit.hurtResistantTime = 0;
               if (result.entityHit instanceof EntityLivingBase) {
                  ((EntityLivingBase)result.entityHit).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1));
               }
            }

            if (this.shooterEntity instanceof EntityLivingBase) {
               for(int i = 0; i < 6; ++i) {
                  double angle = (double)i / (double)6.0F * Math.PI * (double)2.0F;
                  EntityRaitonBeam.EntityCustom smallBolt = new EntityRaitonBeam.EntityCustom(this.world, (EntityLivingBase)this.shooterEntity, 0.0F, 0.0F);
                  smallBolt.setPosition(this.posX, this.posY, this.posZ);
                  smallBolt.motionX = Math.cos(angle) * (double)0.5F;
                  smallBolt.motionY = (this.rand.nextDouble() - 0.3) * 0.4;
                  smallBolt.motionZ = Math.sin(angle) * (double)0.5F;
                  smallBolt.setEntityScale(0.25F);
                  smallBolt.setMaxLifetime(10);
                  this.world.spawnEntity(smallBolt);
               }
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 1.0F, 1.2F + this.rand.nextFloat() * 0.3F);
            SoundEvent elecSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
            if (elecSound != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, elecSound, SoundCategory.PLAYERS, 0.7F, 0.8F);
            }

            if (result.entityHit instanceof EntityLivingBase) {
               EntityLivingBase hit = (EntityLivingBase)result.entityHit;
               double kbX = hit.posX - this.posX;
               double kbZ = hit.posZ - this.posZ;
               double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
               if (kbDist > (double)0.0F) {
                  hit.motionX += kbX / kbDist * 0.6;
                  hit.motionZ += kbZ / kbDist * 0.6;
                  hit.motionY += (double)0.25F;
                  hit.velocityChanged = true;
               }
            }

            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 80 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote && this.lifetime > 2 && this.shooterEntity instanceof EntityLivingBase) {
               Vec3d look = ((EntityLivingBase)this.shooterEntity).getLookVec();
               double speed = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
               this.motionX = this.motionX * (double)0.5F + look.x * speed * (double)0.5F;
               this.motionY = this.motionY * (double)0.5F + look.y * speed * (double)0.5F;
               this.motionZ = this.motionZ * (double)0.5F + look.z * speed * (double)0.5F;
               double newSpeed = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
               if (newSpeed > 0.01) {
                  double ratio = speed / newSpeed;
                  this.motionX *= ratio;
                  this.motionY *= ratio;
                  this.motionZ *= ratio;
               }
            }

            if (!this.world.isRemote) {
               double horizSpeed = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
               this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
               this.rotationPitch = (float)(Math.atan2(this.motionY, horizSpeed) * (180D / Math.PI));
            }

            if (!this.world.isRemote && this.lifetime % 5 == 0) {
               SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
               if (electricity != null) {
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, electricity, SoundCategory.PLAYERS, 0.4F, 0.8F + this.rand.nextFloat() * 0.4F);
               }
            }

            if (!this.world.isRemote && this.lifetime % 4 == 0) {
               EntityRaitonBeam.EntityCustom bolt = new EntityRaitonBeam.EntityCustom(this.world, (EntityLivingBase)this.shooterEntity, 0.0F, 0.0F);
               bolt.setPosition(this.posX + (this.rand.nextDouble() - (double)0.5F) * 1.2, this.posY + (this.rand.nextDouble() - (double)0.5F) * 1.2, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 1.2);
               bolt.motionX = (this.rand.nextDouble() - (double)0.5F) * 0.3;
               bolt.motionY = (this.rand.nextDouble() - (double)0.5F) * 0.3;
               bolt.motionZ = (this.rand.nextDouble() - (double)0.5F) * 0.3;
               bolt.setEntityScale(0.3F);
               bolt.setMaxLifetime(8);
               this.world.spawnEntity(bolt);
            }

         } else {
            this.setDead();
         }
      }

      private Entity findNearestTarget() {
         double searchRadius = (double)20.0F;
         List<EntityLivingBase> nearby = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(searchRadius), (ex) -> ex != this.shooterEntity && ex.isEntityAlive() && ItemJutsu.canTarget(ex));
         Entity closest = null;
         double closestDist = Double.MAX_VALUE;

         for(EntityLivingBase e : nearby) {
            double dist = this.getDistanceSq(e);
            if (dist < closestDist) {
               closestDist = dist;
               closest = e;
            }
         }

         return closest;
      }

      protected float getGravityVelocity() {
         return 0.0F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("lbfDamage", this.damage);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("lbfDamage")) {
            this.damage = compound.getFloat("lbfDamage");
         }

      }

      static {
         MODEL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (power < 0.5F) {
            return false;
         } else if (entity.world.isRemote) {
            return false;
         } else {
            long now = entity.world.getTotalWorldTime();
            UUID uid = entity.getUniqueID();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 60L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               Vec3d look = entity.getLookVec();
               EntityCustom bolt = new EntityCustom(entity.world, entity);
               bolt.setPosition(entity.posX + look.x * (double)1.5F, entity.posY + (double)entity.getEyeHeight() + look.y * (double)1.5F, entity.posZ + look.z * (double)1.5F);
               bolt.setDamage(80.0F + power * 25.0F);
               bolt.motionX = look.x * 1.8;
               bolt.motionY = look.y * 1.8;
               bolt.motionZ = look.z * 1.8;
               entity.world.spawnEntity(bolt);
               SoundEvent lightningShoot = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:lightning_shoot"));
               if (lightningShoot != null) {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, lightningShoot, SoundCategory.PLAYERS, 1.0F, 1.0F);
               } else {
                  SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
                  if (electricity != null) {
                     entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, electricity, SoundCategory.PLAYERS, 1.0F, 1.0F);
                  } else {
                     entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 0.6F, 1.2F);
                  }
               }

               return true;
            }
         }
      }

      public float getBasePower() {
         return 0.5F;
      }

      public float getPowerupDelay() {
         return 30.0F;
      }

      public float getMaxPower() {
         return 8.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote) {
            Particles.spawnParticle(player.world, Types.FLAME, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.6, player.posY + 0.15, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.6, 2, 0.1, 0.1, 0.1, (double)0.0F, 0.02, (double)0.0F, new int[]{-14522625, 15});
         }

         super.onUsingTick(stack, player, power);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelLightningWolf extends ModelBase {
      private final ModelRenderer head;
      private final ModelRenderer body;
      private final ModelRenderer upperBody;
      private final ModelRenderer leg0;
      private final ModelRenderer leg1;
      private final ModelRenderer leg2;
      private final ModelRenderer leg3;
      private final ModelRenderer tail;

      public ModelLightningWolf() {
         this.textureWidth = 64;
         this.textureHeight = 32;
         (this.head = new ModelRenderer(this)).setRotationPoint(-1.0F, 13.5F, -7.0F);
         this.head.cubeList.add(new ModelBox(this.head, 0, 0, -2.0F, -3.0F, -2.0F, 6, 6, 4, 0.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 0, 10, -0.5F, 0.0F, -5.0F, 3, 3, 4, 0.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 16, 14, -2.0F, -5.0F, 0.0F, 2, 2, 1, 0.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 16, 14, 2.0F, -5.0F, 0.0F, 2, 2, 1, 0.0F, false));
         (this.body = new ModelRenderer(this)).setRotationPoint(0.0F, 14.0F, 2.0F);
         setRotationAngle(this.body, 1.5708F, 0.0F, 0.0F);
         this.body.cubeList.add(new ModelBox(this.body, 18, 14, -4.0F, -2.0F, -3.0F, 6, 9, 6, 0.0F, false));
         (this.upperBody = new ModelRenderer(this)).setRotationPoint(-1.0F, 14.0F, 2.0F);
         setRotationAngle(this.upperBody, -1.5708F, 0.0F, 0.0F);
         this.upperBody.cubeList.add(new ModelBox(this.upperBody, 21, 0, -4.0F, 2.0F, -4.0F, 8, 6, 7, 0.0F, false));
         (this.leg0 = new ModelRenderer(this)).setRotationPoint(-2.5F, 16.0F, 7.0F);
         this.leg0.cubeList.add(new ModelBox(this.leg0, 0, 18, -1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F, false));
         (this.leg1 = new ModelRenderer(this)).setRotationPoint(0.5F, 16.0F, 7.0F);
         this.leg1.cubeList.add(new ModelBox(this.leg1, 0, 18, -1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F, false));
         (this.leg2 = new ModelRenderer(this)).setRotationPoint(-2.5F, 16.0F, -4.0F);
         this.leg2.cubeList.add(new ModelBox(this.leg2, 0, 18, -1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F, false));
         (this.leg3 = new ModelRenderer(this)).setRotationPoint(0.5F, 16.0F, -4.0F);
         this.leg3.cubeList.add(new ModelBox(this.leg3, 0, 18, -1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F, false));
         (this.tail = new ModelRenderer(this)).setRotationPoint(-1.0F, 12.0F, 8.0F);
         setRotationAngle(this.tail, 0.9599F, 0.0F, 0.0F);
         this.tail.cubeList.add(new ModelBox(this.tail, 9, 18, -1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F, false));
      }

      private static void setRotationAngle(ModelRenderer modelRenderer, float rx, float ry, float rz) {
         modelRenderer.rotateAngleX = rx;
         modelRenderer.rotateAngleY = ry;
         modelRenderer.rotateAngleZ = rz;
      }

      public void setLegAngles(float limbSwing, float limbSwingAmount) {
         this.leg0.rotateAngleX = MathHelper.cos(limbSwing) * -1.0F * limbSwingAmount;
         this.leg1.rotateAngleX = MathHelper.cos(limbSwing) * 1.0F * limbSwingAmount;
         this.leg2.rotateAngleX = MathHelper.cos(limbSwing) * 1.0F * limbSwingAmount;
         this.leg3.rotateAngleX = MathHelper.cos(limbSwing) * -1.0F * limbSwingAmount;
      }

      public void renderHeadOnly(float scale) {
         this.head.render(scale);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class LightningBeastRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/wolf_lightning.png");
      private final ModelLightningWolf wolfModel = new ModelLightningWolf();

      public LightningBeastRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         float yawAngle = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
         float pitchAngle = -(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks);
         float entityScale = entity.getEntityScale();
         float alpha = Math.min((float)entity.ticksExisted / 5.0F, 1.0F);
         GlStateManager.pushMatrix();
         GlStateManager.translate(x, y, z);
         GlStateManager.rotate(yawAngle, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(pitchAngle, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.enableBlend();
         GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
         GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ONE);
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         this.bindEntityTexture(entity);
         float headScale = entityScale * 6.0F;
         float s = 0.0625F;
         GlStateManager.scale(headScale, headScale, headScale);
         GlStateManager.translate(s * 1.0F, s * -13.5F, s * 7.0F);
         this.wolfModel.renderHeadOnly(s);
         GlStateManager.enableLighting();
         GlStateManager.disableBlend();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }
}
