
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityClayBirdC1 extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 225;

   public EntityClayBirdC1(ElementsInfTsukAddon instance) {
      super(instance, 225);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "clay_bird_c1"), 225).name("clay_bird_c1").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, RenderCustom::new);
   }

   @SideOnly(Side.CLIENT)
   public static class RenderCustom extends RenderLiving<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/birdy.png");

      public RenderCustom(RenderManager renderManager) {
         super(renderManager, new ModelBirdy(), 0.1F);
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         GlStateManager.scale(0.9F, 0.9F, 0.9F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }

   public static class EntityCustom extends EntityLiving {
      private EntityLivingBase target;
      private EntityLivingBase owner;
      private int lifetime = 200;
      private float explosionDamage = 30.0F;
      private float explosionTrueDamage = 20.0F;
      private float explosionRadius = 4.0F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.3F, 0.6F);
         this.experienceValue = 0;
         this.isImmuneToFire = true;
         this.noClip = true;
         this.setNoAI(true);
      }

      public void setTarget(EntityLivingBase target) {
         this.target = target;
      }

      public void setOwner(EntityLivingBase owner) {
         this.owner = owner;
      }

      public void setExplosionDamage(float normal, float trueDmg, float radius) {
         this.explosionDamage = normal;
         this.explosionTrueDamage = trueDmg;
         this.explosionRadius = radius;
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)10.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.0F);
      }

      protected boolean canDespawn() {
         return false;
      }

      protected Item getDropItem() {
         return null;
      }

      public SoundEvent getAmbientSound() {
         return null;
      }

      public SoundEvent getHurtSound(DamageSource ds) {
         return null;
      }

      public SoundEvent getDeathSound() {
         return null;
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         return false;
      }

      public void onUpdate() {
         super.onUpdate();
         --this.lifetime;
         if (!this.world.isRemote) {
            if (this.lifetime <= 0) {
               this.spawnExplosionParticles();
               this.setDead();
               return;
            }

            if (this.target != null && this.target.isEntityAlive()) {
               double dx = this.target.posX - this.posX;
               double dy = this.target.posY + (double)this.target.getEyeHeight() * (double)0.5F - this.posY;
               double dz = this.target.posZ - this.posZ;
               double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
               if (dist < (double)1.5F) {
                  this.spawnExplosionParticles();
                  this.setDead();
                  return;
               }

               double speed = (double)1.0F;
               this.motionX = dx / dist * speed;
               this.motionY = dy / dist * speed;
               this.motionZ = dz / dist * speed;
               float yaw = (float)(MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
               this.rotationYaw = yaw;
               this.renderYawOffset = yaw;
            }

            this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
         }

      }

      private void spawnExplosionParticles() {
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + 0.3, this.posZ, 2, (double)0.5F, 0.3, (double)0.5F, (double)0.0F, new int[0]);

            for(int i = 0; i < 12; ++i) {
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posY + this.rand.nextDouble() * (double)1.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, 1, (double)0.0F, 0.02, (double)0.0F, (double)0.0F, new int[0]);
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0F, 1.2F);
         if (!this.world.isRemote) {
            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)this.explosionRadius))) {
               if (e != this.owner && e instanceof EntityLivingBase) {
                  double d = (double)e.getDistance(this);
                  if (d <= (double)this.explosionRadius) {
                     float falloff = (float)Math.max(0.4, (double)1.0F - d / ((double)this.explosionRadius + (double)1.0F));
                     ((EntityLivingBase)e).attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase)(this.owner != null ? this.owner : this)), this.explosionDamage * falloff);
                     ((EntityLivingBase)e).hurtResistantTime = 0;
                     ((EntityLivingBase)e).attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), this.explosionTrueDamage * falloff);
                  }
               }
            }
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelBirdy extends ModelBase {
      private final ModelRenderer head;
      private final ModelRenderer beak1;
      private final ModelRenderer beak2;
      private final ModelRenderer body;
      private final ModelRenderer tail;
      private final ModelRenderer tail2;
      private final ModelRenderer leg0;
      private final ModelRenderer leg1;
      private final ModelRenderer rightWing;
      private final ModelRenderer rightWingtip;
      private final ModelRenderer leftWing;
      private final ModelRenderer leftWingtip;

      public ModelBirdy() {
         this.textureWidth = 32;
         this.textureHeight = 32;
         (this.head = new ModelRenderer(this)).setRotationPoint(0.0F, 16.0F, -2.8F);
         this.head.cubeList.add(new ModelBox(this.head, 3, 2, -0.5F, -1.5F, -1.0F, 1, 2, 2, 0.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 11, 0, -0.5F, -2.5F, -3.0F, 1, 1, 4, 0.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 1, 17, 0.0F, -6.55F, -2.6F, 0, 5, 5, 0.0F, false));
         (this.beak1 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.2F, -1.4F);
         this.head.addChild(this.beak1);
         this.setRotationAngle(this.beak1, 1.0472F, 0.0F, 0.0F);
         this.beak1.cubeList.add(new ModelBox(this.beak1, 11, 7, -0.5F, -1.25F, -0.5F, 1, 2, 1, -0.05F, false));
         (this.beak2 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.2F, -2.15F);
         this.head.addChild(this.beak2);
         this.setRotationAngle(this.beak2, 0.7854F, 0.0F, 0.0F);
         this.beak2.cubeList.add(new ModelBox(this.beak2, 16, 7, -0.5F, -1.0F, -1.0F, 1, 1, 1, -0.05F, false));
         (this.body = new ModelRenderer(this)).setRotationPoint(0.0F, 16.5F, -3.0F);
         this.setRotationAngle(this.body, 1.309F, 0.0F, 0.0F);
         this.body.cubeList.add(new ModelBox(this.body, 2, 8, -1.5F, 0.0F, -1.5F, 3, 6, 3, 0.0F, false));
         (this.tail = new ModelRenderer(this)).setRotationPoint(0.0F, 6.0F, 1.45F);
         this.body.addChild(this.tail);
         this.setRotationAngle(this.tail, 0.5236F, 0.0F, -0.2618F);
         this.tail.cubeList.add(new ModelBox(this.tail, 22, 1, -1.5F, -0.4F, -1.0F, 3, 4, 1, 0.0F, false));
         (this.tail2 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.tail.addChild(this.tail2);
         this.setRotationAngle(this.tail2, 0.1309F, 0.2618F, 0.5236F);
         this.tail2.cubeList.add(new ModelBox(this.tail2, 22, 1, -1.5F, -0.4F, -1.0F, 3, 4, 1, 0.0F, true));
         (this.leg0 = new ModelRenderer(this)).setRotationPoint(1.5F, 6.5F, -1.5F);
         this.body.addChild(this.leg0);
         this.setRotationAngle(this.leg0, -0.4363F, 0.0F, 0.0F);
         this.leg0.cubeList.add(new ModelBox(this.leg0, 15, 19, -1.0F, -0.5F, 0.0F, 1, 2, 0, 0.0F, true));
         (this.leg1 = new ModelRenderer(this)).setRotationPoint(-0.5F, 6.5F, -1.5F);
         this.body.addChild(this.leg1);
         this.setRotationAngle(this.leg1, -0.4363F, 0.0F, 0.0F);
         this.leg1.cubeList.add(new ModelBox(this.leg1, 15, 19, -1.0F, -0.5F, 0.0F, 1, 2, 0, 0.0F, false));
         (this.rightWing = new ModelRenderer(this)).setRotationPoint(-1.5F, 16.5F, -3.0F);
         this.setRotationAngle(this.rightWing, 0.4363F, 0.2618F, 1.8326F);
         this.rightWing.cubeList.add(new ModelBox(this.rightWing, 19, 8, -0.5F, 0.0F, -1.5F, 1, 5, 3, 0.0F, true));
         this.rightWing.cubeList.add(new ModelBox(this.rightWing, 19, 8, -0.5F, 0.0F, -0.5F, 1, 5, 3, -0.3F, false));
         (this.rightWingtip = new ModelRenderer(this)).setRotationPoint(0.0F, 4.75F, -1.5F);
         this.rightWing.addChild(this.rightWingtip);
         this.setRotationAngle(this.rightWingtip, 0.0F, 0.0F, -0.3491F);
         this.rightWingtip.cubeList.add(new ModelBox(this.rightWingtip, 24, 13, -0.5F, 0.0F, 0.0F, 1, 5, 3, 0.0F, true));
         this.rightWingtip.cubeList.add(new ModelBox(this.rightWingtip, 24, 13, -0.5F, -0.5F, 0.5F, 1, 5, 3, -0.3F, true));
         (this.leftWing = new ModelRenderer(this)).setRotationPoint(1.5F, 16.5F, -3.0F);
         this.setRotationAngle(this.leftWing, 0.4363F, -0.2618F, -1.8326F);
         this.leftWing.cubeList.add(new ModelBox(this.leftWing, 19, 8, -0.5F, 0.0F, -1.5F, 1, 5, 3, 0.0F, false));
         this.leftWing.cubeList.add(new ModelBox(this.leftWing, 19, 8, -0.5F, 0.0F, -0.5F, 1, 5, 3, -0.3F, true));
         (this.leftWingtip = new ModelRenderer(this)).setRotationPoint(0.0F, 4.75F, -1.5F);
         this.leftWing.addChild(this.leftWingtip);
         this.setRotationAngle(this.leftWingtip, 0.0F, 0.0F, 0.3491F);
         this.leftWingtip.cubeList.add(new ModelBox(this.leftWingtip, 24, 13, -0.5F, 0.0F, 0.0F, 1, 5, 3, 0.0F, false));
         this.leftWingtip.cubeList.add(new ModelBox(this.leftWingtip, 24, 13, -0.5F, -0.5F, 0.5F, 1, 5, 3, -0.3F, false));
      }

      public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
         this.head.render(f5);
         this.body.render(f5);
         this.rightWing.render(f5);
         this.leftWing.render(f5);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
         if (entityIn.onGround) {
            this.body.rotateAngleX = 0.4363F;
            this.rightWing.rotationPointY = -3.0F;
            this.leftWing.rotationPointY = -3.0F;
            this.rightWing.rotateAngleZ = 0.0F;
            this.leftWing.rotateAngleZ = 0.0F;
            this.rightWingtip.showModel = false;
            this.leftWingtip.showModel = false;
         } else {
            this.body.rotateAngleX = 1.309F;
            this.rightWing.rotationPointY = -2.0F;
            this.leftWing.rotationPointY = -2.0F;
            this.rightWing.rotateAngleZ = 1.8326F + MathHelper.cos(ageInTicks * 1.2F) * 0.5585F;
            this.rightWingtip.rotateAngleZ = -0.3491F + MathHelper.cos(ageInTicks * 1.2F) * 0.5585F;
            this.leftWing.rotateAngleZ = -this.rightWing.rotateAngleZ;
            this.leftWingtip.rotateAngleZ = -this.rightWingtip.rotateAngleZ;
            this.rightWingtip.showModel = true;
            this.leftWingtip.showModel = true;
         }

      }
   }
}
