
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
public class EntityClayBombC3 extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 227;

   public EntityClayBombC3(ElementsInfTsukAddon instance) {
      super(instance, 227);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "clay_bomb_c3"), 227).name("clay_bomb_c3").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, RenderCustom::new);
   }

   @SideOnly(Side.CLIENT)
   public static class RenderCustom extends RenderLiving<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/c3.png");

      public RenderCustom(RenderManager renderManager) {
         super(renderManager, new ModelC3(), 0.5F);
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         float scale = 0.5F + 7.5F * MathHelper.clamp((float)entity.ticksExisted / 30.0F, 0.0F, 1.0F);
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.translate((double)0.0F, 0.1 * (double)(scale - 1.0F), (double)0.0F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }

   public static class EntityCustom extends EntityLiving {
      private int lifetime = 70;
      private EntityLivingBase owner;
      private float explosionDamage = 80.0F;
      private float explosionTrueDamage = 60.0F;
      private float explosionRadius = 10.0F;

      public void setOwner(EntityLivingBase owner) {
         this.owner = owner;
      }

      public void setExplosionDamage(float normal, float trueDmg, float radius) {
         this.explosionDamage = normal;
         this.explosionTrueDamage = trueDmg;
         this.explosionRadius = radius;
      }

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.6F, 1.8F);
         this.experienceValue = 0;
         this.isImmuneToFire = true;
         this.setNoAI(true);
         this.enablePersistence();
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)20.0F);
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
         if (source.isExplosion()) {
            return false;
         } else {
            return source == DamageSource.OUT_OF_WORLD ? super.attackEntityFrom(source, amount) : false;
         }
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

            if (this.ticksExisted > 30 && this.ticksExisted % 4 == 0 && this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY + (double)2.0F, this.posZ, 5, 0.3, (double)0.5F, 0.3, 0.05, new int[0]);
            }
         }

      }

      private void spawnExplosionParticles() {
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)2.0F, this.posZ, 3, (double)2.0F, (double)2.0F, (double)2.0F, (double)0.0F, new int[0]);

            for(int i = 0; i < 15; ++i) {
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)12.0F, this.posY + this.rand.nextDouble() * (double)6.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)12.0F, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

            for(int i = 0; i < 50; ++i) {
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)14.0F, this.posY + this.rand.nextDouble() * (double)8.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)14.0F, 1, (double)0.0F, 0.05, (double)0.0F, (double)0.0F, new int[0]);
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.0F, 0.6F);
         if (!this.world.isRemote) {
            for(Entity e : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow((double)this.explosionRadius))) {
               if (e != this.owner && e instanceof EntityLivingBase) {
                  double d = (double)e.getDistance(this);
                  if (d <= (double)this.explosionRadius) {
                     float falloff = (float)Math.max(0.3, (double)1.0F - d / ((double)this.explosionRadius + (double)1.0F));
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
   public static class ModelC3 extends ModelBase {
      private final ModelRenderer body;
      private final ModelRenderer leftWing;
      private final ModelRenderer leftWingTip;
      private final ModelRenderer bone6;
      private final ModelRenderer bone7;
      private final ModelRenderer bone8;
      private final ModelRenderer rightWing;
      private final ModelRenderer rightWingTip;
      private final ModelRenderer bone11;
      private final ModelRenderer bone12;
      private final ModelRenderer bone13;
      private final ModelRenderer head;
      private final ModelRenderer hump;
      private final ModelRenderer bone2;

      public ModelC3() {
         this.textureWidth = 64;
         this.textureHeight = 64;
         (this.body = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.body.cubeList.add(new ModelBox(this.body, 0, 22, -5.0F, 4.0F, -5.0F, 10, 10, 10, 0.0F, false));
         this.body.cubeList.add(new ModelBox(this.body, 0, 0, -6.0F, 14.0F, -6.0F, 12, 10, 12, 0.0F, false));
         (this.leftWing = new ModelRenderer(this)).setRotationPoint(5.0F, 9.0F, 0.0F);
         this.body.addChild(this.leftWing);
         this.setRotationAngle(this.leftWing, -0.6981F, 0.0F, 0.0F);
         this.leftWing.cubeList.add(new ModelBox(this.leftWing, 0, 42, 0.0F, -2.0F, -2.0F, 4, 6, 4, 0.4F, false));
         (this.leftWingTip = new ModelRenderer(this)).setRotationPoint(2.0F, 4.0F, 0.0F);
         this.leftWing.addChild(this.leftWingTip);
         this.setRotationAngle(this.leftWingTip, 0.0F, 0.0F, 0.7854F);
         this.leftWingTip.cubeList.add(new ModelBox(this.leftWingTip, 48, 12, -2.5F, -2.0F, -1.5F, 5, 6, 3, 0.4F, false));
         (this.bone6 = new ModelRenderer(this)).setRotationPoint(2.5F, 7.0F, 0.0F);
         this.leftWingTip.addChild(this.bone6);
         this.setRotationAngle(this.bone6, 0.0F, 0.0F, -0.2618F);
         this.bone6.cubeList.add(new ModelBox(this.bone6, 0, 54, -1.0F, -4.0F, -1.0F, 2, 8, 2, 0.4F, false));
         (this.bone7 = new ModelRenderer(this)).setRotationPoint(0.0F, 7.0F, 0.0F);
         this.leftWingTip.addChild(this.bone7);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 0, 54, -1.0F, -4.0F, -1.0F, 2, 8, 2, 0.4F, false));
         (this.bone8 = new ModelRenderer(this)).setRotationPoint(-2.5F, 7.0F, 0.0F);
         this.leftWingTip.addChild(this.bone8);
         this.setRotationAngle(this.bone8, 0.0F, 0.0F, 0.2618F);
         this.bone8.cubeList.add(new ModelBox(this.bone8, 0, 54, -1.0F, -4.0F, -1.0F, 2, 8, 2, 0.4F, false));
         (this.rightWing = new ModelRenderer(this)).setRotationPoint(-5.0F, 9.0F, 0.0F);
         this.body.addChild(this.rightWing);
         this.setRotationAngle(this.rightWing, -0.8727F, 0.0F, 0.0F);
         this.rightWing.cubeList.add(new ModelBox(this.rightWing, 0, 42, -4.0F, -2.0F, -2.0F, 4, 6, 4, 0.4F, true));
         (this.rightWingTip = new ModelRenderer(this)).setRotationPoint(-2.0F, 4.0F, 0.0F);
         this.rightWing.addChild(this.rightWingTip);
         this.setRotationAngle(this.rightWingTip, 0.0F, 0.0F, -0.7854F);
         this.rightWingTip.cubeList.add(new ModelBox(this.rightWingTip, 48, 12, -2.5F, -2.0F, -1.5F, 5, 6, 3, 0.4F, true));
         (this.bone11 = new ModelRenderer(this)).setRotationPoint(-2.5F, 7.0F, 0.0F);
         this.rightWingTip.addChild(this.bone11);
         this.setRotationAngle(this.bone11, 0.0F, 0.0F, 0.2618F);
         this.bone11.cubeList.add(new ModelBox(this.bone11, 0, 54, -1.0F, -4.0F, -1.0F, 2, 8, 2, 0.4F, true));
         (this.bone12 = new ModelRenderer(this)).setRotationPoint(0.0F, 7.0F, 0.0F);
         this.rightWingTip.addChild(this.bone12);
         this.bone12.cubeList.add(new ModelBox(this.bone12, 0, 54, -1.0F, -4.0F, -1.0F, 2, 8, 2, 0.4F, true));
         (this.bone13 = new ModelRenderer(this)).setRotationPoint(2.5F, 7.0F, 0.0F);
         this.rightWingTip.addChild(this.bone13);
         this.setRotationAngle(this.bone13, 0.0F, 0.0F, -0.2618F);
         this.bone13.cubeList.add(new ModelBox(this.bone13, 0, 54, -1.0F, -4.0F, -1.0F, 2, 8, 2, 0.4F, true));
         (this.head = new ModelRenderer(this)).setRotationPoint(0.0F, 4.5F, -4.5F);
         this.body.addChild(this.head);
         this.head.cubeList.add(new ModelBox(this.head, 44, 31, -2.5F, -3.5F, -2.5F, 5, 6, 5, 0.0F, false));
         (this.hump = new ModelRenderer(this)).setRotationPoint(0.0F, 4.0F, -0.5F);
         this.body.addChild(this.hump);
         this.hump.cubeList.add(new ModelBox(this.hump, 30, 22, -4.5F, -2.0F, -3.0F, 9, 2, 7, 0.0F, false));
         (this.bone2 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -0.45F);
         this.hump.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.0F, 0.0F, 0.7854F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 36, 0, -3.0F, -3.0F, -2.05F, 6, 6, 6, 0.0F, false));
      }

      public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
         this.body.render(f5);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
         super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
         EntityCustom entity = (EntityCustom)e;
         if (entity.ticksExisted >= 30) {
            int extendTicks = 40;
            float swing = MathHelper.clamp((float)(entity.ticksExisted - 30) / (float)extendTicks, 0.0F, 1.0F);
            this.leftWing.rotateAngleX = (swing - 1.0F) * 0.6981F;
            this.leftWing.rotateAngleZ = swing * -1.7453F;
            this.leftWingTip.rotateAngleZ = (1.0F - swing) * 0.7854F;
            this.rightWing.rotateAngleX = (swing - 1.0F) * 0.8727F;
            this.rightWing.rotateAngleZ = swing * 1.7453F;
            this.rightWingTip.rotateAngleZ = (swing - 1.0F) * 0.7854F;
         } else {
            this.leftWing.rotateAngleX = -0.6981F;
            this.leftWing.rotateAngleZ = 0.0F;
            this.leftWingTip.rotateAngleZ = 0.7854F;
            this.rightWing.rotateAngleX = -0.8727F;
            this.rightWing.rotateAngleZ = 0.0F;
            this.rightWingTip.rotateAngleZ = -0.7854F;
         }

      }
   }
}
