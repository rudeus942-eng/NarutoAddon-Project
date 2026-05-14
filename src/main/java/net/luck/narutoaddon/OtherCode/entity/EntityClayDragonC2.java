
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
public class EntityClayDragonC2 extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 226;

   public EntityClayDragonC2(ElementsInfTsukAddon instance) {
      super(instance, 226);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "clay_dragon_c2"), 226).name("clay_dragon_c2").tracker(96, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, RenderCustom::new);
   }

   @SideOnly(Side.CLIENT)
   public static class RenderCustom extends RenderLiving<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/dragon.png");

      public RenderCustom(RenderManager renderManager) {
         super(renderManager, new ModelDragon(), 0.5F);
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         float scale = 0.15F + 0.85F * Math.min(1.0F, (float)entity.ticksExisted / 30.0F);
         GlStateManager.scale(scale, scale, scale);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }

   public static class EntityCustom extends EntityLiving {
      private double targetX;
      private double targetY;
      private double targetZ;
      private boolean hasTarget = false;
      private int lifetime = 100;
      private EntityLivingBase owner;
      private float explosionDamage = 50.0F;
      private float explosionTrueDamage = 35.0F;
      private float explosionRadius = 6.0F;

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
         this.setSize(0.5F, 0.8F);
         this.experienceValue = 0;
         this.isImmuneToFire = true;
         this.noClip = true;
         this.setNoAI(true);
      }

      public void setFlyTarget(double x, double y, double z) {
         this.targetX = x;
         this.targetY = y;
         this.targetZ = z;
         this.hasTarget = true;
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)100.0F);
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
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
         ++this.ticksExisted;
         --this.lifetime;
         if (!this.world.isRemote) {
            if (this.lifetime <= 0) {
               this.spawnExplosionParticles();
               this.setDead();
               return;
            }

            EntityPlayer nearestPlayer = this.world.getClosestPlayerToEntity(this, (double)50.0F);
            double flyToX = this.hasTarget ? this.targetX : this.posX;
            double flyToY = this.hasTarget ? this.targetY : this.posY;
            double flyToZ = this.hasTarget ? this.targetZ : this.posZ;
            if (nearestPlayer != null && nearestPlayer.isEntityAlive()) {
               flyToX = nearestPlayer.posX;
               flyToY = nearestPlayer.posY + (double)1.0F;
               flyToZ = nearestPlayer.posZ;
               double playerDist = (double)this.getDistance(nearestPlayer);
               if (playerDist < (double)4.0F && this.ticksExisted > 40) {
                  this.spawnExplosionParticles();
                  this.setDead();
                  return;
               }
            }

            double dx = flyToX - this.posX;
            double dy = flyToY - this.posY;
            double dz = flyToZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 0.1) {
               double speed = this.ticksExisted < 30 ? 0.4 : 1.2;
               double mx = dx / dist * speed;
               double my = dy / dist * speed;
               double mz = dz / dist * speed;
               this.setPosition(this.posX + mx, this.posY + my, this.posZ + mz);
            }

            if (dist > (double)0.5F) {
               float yaw = (float)(MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
               this.rotationYaw = yaw;
               this.renderYawOffset = yaw;
            }
         }

      }

      private void spawnExplosionParticles() {
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY + (double)3.0F, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);

            for(int i = 0; i < 12; ++i) {
               ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)8.0F, this.posY + this.rand.nextDouble() * (double)5.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)8.0F, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

            for(int i = 0; i < 40; ++i) {
               ws.spawnParticle(EnumParticleTypes.CLOUD, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)10.0F, this.posY + this.rand.nextDouble() * (double)6.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)10.0F, 1, (double)0.0F, 0.05, (double)0.0F, (double)0.0F, new int[0]);
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 0.8F);
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
   public static class ModelDragon extends ModelBase {
      private final ModelRenderer[] neck = new ModelRenderer[4];
      private final ModelRenderer head;
      private final ModelRenderer bone5;
      private final ModelRenderer bone6;
      private final ModelRenderer bone;
      private final ModelRenderer jaw;
      private final ModelRenderer bone3;
      private final ModelRenderer bone4;
      private final ModelRenderer bone2;
      private final ModelRenderer bone7;
      private final ModelRenderer body;
      private final ModelRenderer bone42;
      private final ModelRenderer rightWing;
      private final ModelRenderer rightWingtip;
      private final ModelRenderer bone9;
      private final ModelRenderer leftWing;
      private final ModelRenderer leftWingtip;
      private final ModelRenderer bone8;
      private final ModelRenderer rightRearLeg;
      private final ModelRenderer rightRearlegtip;
      private final ModelRenderer rightRearfoot;
      private final ModelRenderer bone26;
      private final ModelRenderer bone27;
      private final ModelRenderer bone28;
      private final ModelRenderer bone29;
      private final ModelRenderer bone30;
      private final ModelRenderer bone31;
      private final ModelRenderer bone32;
      private final ModelRenderer bone33;
      private final ModelRenderer leftRearLeg;
      private final ModelRenderer leftRearlegtip;
      private final ModelRenderer leftRearfoot;
      private final ModelRenderer bone40;
      private final ModelRenderer bone41;
      private final ModelRenderer bone34;
      private final ModelRenderer bone35;
      private final ModelRenderer bone36;
      private final ModelRenderer bone37;
      private final ModelRenderer bone38;
      private final ModelRenderer bone39;
      private final ModelRenderer rightFrontLeg;
      private final ModelRenderer rightFrontlegtip;
      private final ModelRenderer rightFrontfoot;
      private final ModelRenderer bone18;
      private final ModelRenderer bone19;
      private final ModelRenderer bone20;
      private final ModelRenderer bone21;
      private final ModelRenderer bone22;
      private final ModelRenderer bone23;
      private final ModelRenderer bone24;
      private final ModelRenderer bone25;
      private final ModelRenderer leftFrontLeg;
      private final ModelRenderer leftFrontlegtip;
      private final ModelRenderer leftFrontfoot;
      private final ModelRenderer bone16;
      private final ModelRenderer bone17;
      private final ModelRenderer bone10;
      private final ModelRenderer bone11;
      private final ModelRenderer bone12;
      private final ModelRenderer bone13;
      private final ModelRenderer bone14;
      private final ModelRenderer bone15;
      private final ModelRenderer[] tail = new ModelRenderer[12];

      public ModelDragon() {
         this.textureWidth = 256;
         this.textureHeight = 256;
         this.neck[0] = new ModelRenderer(this);
         this.neck[0].setRotationPoint(0.0F, -22.0F, -8.0F);
         this.neck[0].cubeList.add(new ModelBox(this.neck[0], 216, 144, -5.0F, -5.0F, -10.0F, 10, 10, 10, 2.5F, false));
         this.neck[0].cubeList.add(new ModelBox(this.neck[0], 48, 0, -2.0F, -11.5F, -8.0F, 4, 4, 4, 0.5F, false));
         this.neck[1] = new ModelRenderer(this);
         this.neck[1].setRotationPoint(0.0F, -22.0F, -18.0F);
         this.setRotationAngle(this.neck[1], -0.0873F, -0.1745F, 0.0F);
         this.neck[1].cubeList.add(new ModelBox(this.neck[1], 216, 144, -5.0F, -5.0F, -10.0F, 10, 10, 10, 2.0F, false));
         this.neck[1].cubeList.add(new ModelBox(this.neck[1], 48, 0, -2.0F, -11.0F, -8.0F, 4, 4, 4, 0.5F, false));
         (this.neck[2] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -10.0F);
         this.setRotationAngle(this.neck[2], 0.0873F, -0.1745F, 0.0F);
         this.neck[2].cubeList.add(new ModelBox(this.neck[2], 216, 144, -5.0F, -5.0F, -10.0F, 10, 10, 10, 1.5F, false));
         this.neck[2].cubeList.add(new ModelBox(this.neck[2], 48, 0, -2.0F, -10.5F, -8.0F, 4, 4, 4, 0.4F, false));
         (this.neck[3] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -10.0F);
         this.setRotationAngle(this.neck[3], 0.0873F, -0.1745F, 0.0F);
         this.neck[3].cubeList.add(new ModelBox(this.neck[3], 216, 144, -5.0F, -5.0F, -10.0F, 10, 10, 10, 1.0F, false));
         this.neck[3].cubeList.add(new ModelBox(this.neck[3], 48, 0, -2.0F, -10.0F, -8.0F, 4, 4, 4, 0.4F, false));
         (this.head = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -10.0F);
         this.setRotationAngle(this.head, 0.0873F, 0.0F, 0.0F);
         this.head.cubeList.add(new ModelBox(this.head, 112, 30, -8.0F, -8.0F, -16.0F, 16, 16, 16, 0.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 0, 0, -7.0F, -9.0F, -7.0F, 2, 1, 6, 1.0F, true));
         this.head.cubeList.add(new ModelBox(this.head, 0, 0, 5.0F, -9.0F, -7.0F, 2, 1, 6, 1.0F, false));
         (this.bone5 = new ModelRenderer(this)).setRotationPoint(-8.0F, -0.5F, -16.0F);
         this.head.addChild(this.bone5);
         this.setRotationAngle(this.bone5, 0.0F, -0.3491F, 0.0F);
         this.bone5.cubeList.add(new ModelBox(this.bone5, 184, 47, 0.0F, -2.5F, -12.0F, 8, 6, 12, -0.1F, false));
         (this.bone6 = new ModelRenderer(this)).setRotationPoint(8.0F, -0.5F, -16.0F);
         this.head.addChild(this.bone6);
         this.setRotationAngle(this.bone6, 0.0F, 0.3491F, 0.0F);
         this.bone6.cubeList.add(new ModelBox(this.bone6, 184, 47, -8.0F, -2.5F, -12.0F, 8, 6, 12, -0.1F, true));
         (this.bone = new ModelRenderer(this)).setRotationPoint(4.0F, -10.0F, 0.0F);
         this.head.addChild(this.bone);
         this.setRotationAngle(this.bone, -0.5236F, 0.0F, 0.0F);
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 50, 0.0F, 0.0F, -10.0F, 4, 4, 10, 0.0F, false));
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 50, -12.0F, 0.0F, -10.0F, 4, 4, 10, 0.0F, true));
         (this.jaw = new ModelRenderer(this)).setRotationPoint(0.0F, 4.0F, -14.0F);
         this.head.addChild(this.jaw);
         (this.bone3 = new ModelRenderer(this)).setRotationPoint(-8.0F, 0.5F, -2.0F);
         this.jaw.addChild(this.bone3);
         this.setRotationAngle(this.bone3, 0.0F, -0.2618F, 0.0F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 184, 65, 0.0F, -3.5F, -12.0F, 8, 7, 16, 0.0F, false));
         (this.bone4 = new ModelRenderer(this)).setRotationPoint(8.0F, 0.5F, -2.0F);
         this.jaw.addChild(this.bone4);
         this.setRotationAngle(this.bone4, 0.0F, 0.2618F, 0.0F);
         this.bone4.cubeList.add(new ModelBox(this.bone4, 184, 65, -8.0F, -3.5F, -12.0F, 8, 7, 16, 0.0F, true));
         (this.bone2 = new ModelRenderer(this)).setRotationPoint(-3.0F, 0.5F, -13.0F);
         this.jaw.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.5236F, -0.2618F, -0.1309F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 112, 30, 0.0F, 0.0F, -4.0F, 4, 4, 4, 0.0F, false));
         (this.bone7 = new ModelRenderer(this)).setRotationPoint(3.0F, 0.5F, -13.0F);
         this.jaw.addChild(this.bone7);
         this.setRotationAngle(this.bone7, 0.5236F, 0.2618F, 0.1309F);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 112, 30, -4.0F, 0.0F, -4.0F, 4, 4, 4, 0.0F, true));
         (this.body = new ModelRenderer(this)).setRotationPoint(0.0F, -24.0F, -8.0F);
         this.body.cubeList.add(new ModelBox(this.body, 0, 0, -12.0F, -6.0F, 0.0F, 24, 24, 64, 0.0F, false));
         (this.bone42 = new ModelRenderer(this)).setRotationPoint(0.0F, 10.0F, 0.0F);
         this.body.addChild(this.bone42);
         this.setRotationAngle(this.bone42, 0.9599F, 0.0F, 0.0F);
         this.bone42.cubeList.add(new ModelBox(this.bone42, 167, 144, -12.0F, 0.0F, 0.0F, 24, 14, 1, 0.0F, false));
         (this.rightWing = new ModelRenderer(this)).setRotationPoint(-12.0F, -29.0F, 2.0F);
         this.setRotationAngle(this.rightWing, 0.0F, -0.5236F, -0.5236F);
         this.rightWing.cubeList.add(new ModelBox(this.rightWing, 112, 88, -56.0F, -4.0F, -4.0F, 56, 8, 8, 0.0F, false));
         this.rightWing.cubeList.add(new ModelBox(this.rightWing, -56, 88, -56.0F, 0.0F, 2.0F, 56, 0, 56, 0.01F, false));
         (this.rightWingtip = new ModelRenderer(this)).setRotationPoint(-56.0F, 0.0F, -2.0F);
         this.rightWing.addChild(this.rightWingtip);
         this.setRotationAngle(this.rightWingtip, 0.0F, 1.2217F, -0.2618F);
         this.rightWingtip.cubeList.add(new ModelBox(this.rightWingtip, 112, 136, -68.0F, -2.0F, 0.0F, 68, 4, 4, 0.0F, false));
         this.rightWingtip.cubeList.add(new ModelBox(this.rightWingtip, -56, 144, -72.0F, 0.0F, 4.0F, 72, 0, 56, 0.01F, false));
         (this.bone9 = new ModelRenderer(this)).setRotationPoint(1.0F, 2.0F, 0.0F);
         this.rightWingtip.addChild(this.bone9);
         this.setRotationAngle(this.bone9, 0.0F, 0.2618F, 0.0F);
         this.bone9.cubeList.add(new ModelBox(this.bone9, 112, 6, -5.0F, -4.0F, -6.0F, 4, 4, 8, 0.5F, true));
         this.bone9.cubeList.add(new ModelBox(this.bone9, 112, 6, -13.0F, -4.0F, -8.0F, 4, 4, 8, 0.5F, true));
         this.bone9.cubeList.add(new ModelBox(this.bone9, 112, 6, -21.0F, -4.0F, -10.0F, 4, 4, 8, 0.5F, true));
         this.bone9.cubeList.add(new ModelBox(this.bone9, 112, 6, -29.0F, -4.0F, -12.0F, 4, 4, 8, 0.5F, true));
         this.bone9.cubeList.add(new ModelBox(this.bone9, 112, 6, -37.0F, -4.0F, -14.0F, 4, 4, 8, 0.5F, true));
         (this.leftWing = new ModelRenderer(this)).setRotationPoint(12.0F, -29.0F, 2.0F);
         this.setRotationAngle(this.leftWing, 0.0F, -0.1745F, -0.1745F);
         this.leftWing.cubeList.add(new ModelBox(this.leftWing, 112, 88, 0.0F, -4.0F, -4.0F, 56, 8, 8, 0.0F, true));
         this.leftWing.cubeList.add(new ModelBox(this.leftWing, -56, 88, 0.0F, 0.0F, 2.0F, 56, 0, 56, 0.01F, true));
         (this.leftWingtip = new ModelRenderer(this)).setRotationPoint(56.0F, 0.0F, -2.0F);
         this.leftWing.addChild(this.leftWingtip);
         this.setRotationAngle(this.leftWingtip, 0.0F, 0.0F, 0.3491F);
         this.leftWingtip.cubeList.add(new ModelBox(this.leftWingtip, 112, 136, 1.0F, -2.0F, 0.0F, 68, 4, 4, 0.0F, true));
         this.leftWingtip.cubeList.add(new ModelBox(this.leftWingtip, -56, 144, 0.0F, 0.0F, 4.0F, 72, 0, 56, 0.01F, true));
         (this.bone8 = new ModelRenderer(this)).setRotationPoint(-1.0F, 2.0F, 0.0F);
         this.leftWingtip.addChild(this.bone8);
         this.setRotationAngle(this.bone8, 0.0F, -0.2618F, 0.0F);
         this.bone8.cubeList.add(new ModelBox(this.bone8, 112, 6, 1.0F, -4.0F, -6.0F, 4, 4, 8, 0.5F, false));
         this.bone8.cubeList.add(new ModelBox(this.bone8, 112, 6, 9.0F, -4.0F, -8.0F, 4, 4, 8, 0.5F, false));
         this.bone8.cubeList.add(new ModelBox(this.bone8, 112, 6, 17.0F, -4.0F, -10.0F, 4, 4, 8, 0.5F, false));
         this.bone8.cubeList.add(new ModelBox(this.bone8, 112, 6, 25.0F, -4.0F, -12.0F, 4, 4, 8, 0.5F, false));
         this.bone8.cubeList.add(new ModelBox(this.bone8, 112, 6, 33.0F, -4.0F, -14.0F, 4, 4, 8, 0.5F, false));
         (this.rightRearLeg = new ModelRenderer(this)).setRotationPoint(-16.0F, -18.0F, 42.0F);
         this.setRotationAngle(this.rightRearLeg, -0.5236F, 0.5236F, 0.0F);
         this.rightRearLeg.cubeList.add(new ModelBox(this.rightRearLeg, 0, 0, -8.0F, -4.0F, -8.0F, 16, 32, 16, 0.0F, false));
         (this.rightRearlegtip = new ModelRenderer(this)).setRotationPoint(0.0F, 28.0F, -8.0F);
         this.rightRearLeg.addChild(this.rightRearlegtip);
         this.setRotationAngle(this.rightRearlegtip, 1.4835F, 0.0F, 0.0F);
         this.rightRearlegtip.cubeList.add(new ModelBox(this.rightRearlegtip, 196, 0, -6.0F, 0.0F, 0.0F, 12, 32, 12, 0.0F, false));
         (this.rightRearfoot = new ModelRenderer(this)).setRotationPoint(0.0F, 32.0F, 5.0F);
         this.rightRearlegtip.addChild(this.rightRearfoot);
         this.setRotationAngle(this.rightRearfoot, -0.9599F, 0.0F, 0.0F);
         this.rightRearfoot.cubeList.add(new ModelBox(this.rightRearfoot, 126, 8, -6.0F, 0.0F, -12.0F, 12, 6, 16, 0.0F, false));
         (this.bone26 = new ModelRenderer(this)).setRotationPoint(4.0F, 3.0F, 2.0F);
         this.rightRearfoot.addChild(this.bone26);
         this.setRotationAngle(this.bone26, 2.3562F, 0.7854F, 0.0F);
         this.bone26.cubeList.add(new ModelBox(this.bone26, 20, 48, -1.0F, -1.0F, -6.0F, 2, 2, 6, 0.8F, true));
         (this.bone27 = new ModelRenderer(this)).setRotationPoint(1.0F, 1.0F, -6.0F);
         this.bone26.addChild(this.bone27);
         this.setRotationAngle(this.bone27, -0.7854F, 0.0F, 0.0F);
         this.bone27.cubeList.add(new ModelBox(this.bone27, 20, 48, -2.0F, -2.0F, -6.0F, 2, 2, 6, 0.8F, true));
         (this.bone28 = new ModelRenderer(this)).setRotationPoint(5.0F, 3.0F, -11.0F);
         this.rightRearfoot.addChild(this.bone28);
         this.setRotationAngle(this.bone28, 0.3491F, -0.1745F, 0.0F);
         this.bone28.cubeList.add(new ModelBox(this.bone28, 20, 48, -2.0F, -1.0F, -6.0F, 2, 2, 6, 0.8F, true));
         (this.bone29 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -6.0F);
         this.bone28.addChild(this.bone29);
         this.setRotationAngle(this.bone29, 0.3491F, 0.0F, 0.0F);
         this.bone29.cubeList.add(new ModelBox(this.bone29, 20, 48, -2.0F, 1.0F, -6.0F, 2, 2, 6, 0.8F, true));
         (this.bone30 = new ModelRenderer(this)).setRotationPoint(1.0F, 3.0F, -11.0F);
         this.rightRearfoot.addChild(this.bone30);
         this.setRotationAngle(this.bone30, 0.2618F, 0.0F, 0.0F);
         this.bone30.cubeList.add(new ModelBox(this.bone30, 20, 48, -2.0F, -1.0F, -6.0F, 2, 2, 6, 0.8F, true));
         (this.bone31 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -6.0F);
         this.bone30.addChild(this.bone31);
         this.setRotationAngle(this.bone31, 0.3491F, 0.0F, 0.0F);
         this.bone31.cubeList.add(new ModelBox(this.bone31, 20, 48, -2.0F, 1.0F, -6.0F, 2, 2, 6, 0.8F, true));
         (this.bone32 = new ModelRenderer(this)).setRotationPoint(-3.0F, 3.0F, -11.0F);
         this.rightRearfoot.addChild(this.bone32);
         this.setRotationAngle(this.bone32, 0.3491F, 0.1745F, 0.0F);
         this.bone32.cubeList.add(new ModelBox(this.bone32, 20, 48, -2.0F, -1.0F, -6.0F, 2, 2, 6, 0.8F, true));
         (this.bone33 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -6.0F);
         this.bone32.addChild(this.bone33);
         this.setRotationAngle(this.bone33, 0.3491F, 0.0F, 0.0F);
         this.bone33.cubeList.add(new ModelBox(this.bone33, 20, 48, -2.0F, 1.0F, -6.0F, 2, 2, 6, 0.8F, true));
         (this.leftRearLeg = new ModelRenderer(this)).setRotationPoint(16.0F, -18.0F, 42.0F);
         this.setRotationAngle(this.leftRearLeg, 1.0472F, 0.0F, 0.0F);
         this.leftRearLeg.cubeList.add(new ModelBox(this.leftRearLeg, 0, 0, -8.0F, -4.0F, -8.0F, 16, 32, 16, 0.0F, true));
         (this.leftRearlegtip = new ModelRenderer(this)).setRotationPoint(0.0F, 28.0F, -8.0F);
         this.leftRearLeg.addChild(this.leftRearlegtip);
         this.setRotationAngle(this.leftRearlegtip, 0.4363F, 0.0F, 0.0F);
         this.leftRearlegtip.cubeList.add(new ModelBox(this.leftRearlegtip, 196, 0, -6.0F, 0.0F, 0.0F, 12, 32, 12, 0.0F, true));
         (this.leftRearfoot = new ModelRenderer(this)).setRotationPoint(0.0F, 32.0F, 5.0F);
         this.leftRearlegtip.addChild(this.leftRearfoot);
         this.setRotationAngle(this.leftRearfoot, 0.7854F, 0.0F, 0.0F);
         this.leftRearfoot.cubeList.add(new ModelBox(this.leftRearfoot, 126, 8, -6.0F, 0.0F, -12.0F, 12, 6, 16, 0.0F, true));
         (this.bone40 = new ModelRenderer(this)).setRotationPoint(-4.0F, 3.0F, 2.0F);
         this.leftRearfoot.addChild(this.bone40);
         this.setRotationAngle(this.bone40, 2.3562F, -0.7854F, 0.0F);
         this.bone40.cubeList.add(new ModelBox(this.bone40, 20, 48, -1.0F, -1.0F, -6.0F, 2, 2, 6, 0.8F, false));
         (this.bone41 = new ModelRenderer(this)).setRotationPoint(-1.0F, 1.0F, -6.0F);
         this.bone40.addChild(this.bone41);
         this.setRotationAngle(this.bone41, -0.7854F, 0.0F, 0.0F);
         this.bone41.cubeList.add(new ModelBox(this.bone41, 20, 48, 0.0F, -2.0F, -6.0F, 2, 2, 6, 0.8F, false));
         (this.bone34 = new ModelRenderer(this)).setRotationPoint(-5.0F, 3.0F, -11.0F);
         this.leftRearfoot.addChild(this.bone34);
         this.setRotationAngle(this.bone34, 0.3491F, 0.1745F, 0.0F);
         this.bone34.cubeList.add(new ModelBox(this.bone34, 20, 48, 0.0F, -1.0F, -6.0F, 2, 2, 6, 0.8F, false));
         (this.bone35 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -6.0F);
         this.bone34.addChild(this.bone35);
         this.setRotationAngle(this.bone35, 0.3491F, 0.0F, 0.0F);
         this.bone35.cubeList.add(new ModelBox(this.bone35, 20, 48, 0.0F, 1.0F, -6.0F, 2, 2, 6, 0.8F, false));
         (this.bone36 = new ModelRenderer(this)).setRotationPoint(-1.0F, 3.0F, -11.0F);
         this.leftRearfoot.addChild(this.bone36);
         this.setRotationAngle(this.bone36, 0.2618F, 0.0F, 0.0F);
         this.bone36.cubeList.add(new ModelBox(this.bone36, 20, 48, 0.0F, -1.0F, -6.0F, 2, 2, 6, 0.8F, false));
         (this.bone37 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -6.0F);
         this.bone36.addChild(this.bone37);
         this.setRotationAngle(this.bone37, 0.3491F, 0.0F, 0.0F);
         this.bone37.cubeList.add(new ModelBox(this.bone37, 20, 48, 0.0F, 1.0F, -6.0F, 2, 2, 6, 0.8F, false));
         (this.bone38 = new ModelRenderer(this)).setRotationPoint(3.0F, 3.0F, -11.0F);
         this.leftRearfoot.addChild(this.bone38);
         this.setRotationAngle(this.bone38, 0.3491F, -0.1745F, 0.0F);
         this.bone38.cubeList.add(new ModelBox(this.bone38, 20, 48, 0.0F, -1.0F, -6.0F, 2, 2, 6, 0.8F, false));
         (this.bone39 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -6.0F);
         this.bone38.addChild(this.bone39);
         this.setRotationAngle(this.bone39, 0.3491F, 0.0F, 0.0F);
         this.bone39.cubeList.add(new ModelBox(this.bone39, 20, 48, 0.0F, 1.0F, -6.0F, 2, 2, 6, 0.8F, false));
         (this.rightFrontLeg = new ModelRenderer(this)).setRotationPoint(-12.0F, -14.0F, 2.0F);
         this.setRotationAngle(this.rightFrontLeg, 0.3491F, 0.0F, 0.0F);
         this.rightFrontLeg.cubeList.add(new ModelBox(this.rightFrontLeg, 112, 104, -4.0F, -4.0F, -4.0F, 8, 24, 8, 0.0F, false));
         (this.rightFrontlegtip = new ModelRenderer(this)).setRotationPoint(0.0F, 20.0F, 3.0F);
         this.rightFrontLeg.addChild(this.rightFrontlegtip);
         this.setRotationAngle(this.rightFrontlegtip, -1.0472F, 0.0F, 0.0F);
         this.rightFrontlegtip.cubeList.add(new ModelBox(this.rightFrontlegtip, 232, 104, -3.0F, -1.0F, -6.0F, 6, 24, 6, 0.0F, false));
         (this.rightFrontfoot = new ModelRenderer(this)).setRotationPoint(0.0F, 22.0F, -3.0F);
         this.rightFrontlegtip.addChild(this.rightFrontfoot);
         this.setRotationAngle(this.rightFrontfoot, 0.6981F, 0.0F, 0.0F);
         this.rightFrontfoot.cubeList.add(new ModelBox(this.rightFrontfoot, 148, 108, -4.0F, 0.0F, -8.0F, 8, 4, 12, 0.0F, false));
         (this.bone18 = new ModelRenderer(this)).setRotationPoint(3.0F, 2.0F, 3.0F);
         this.rightFrontfoot.addChild(this.bone18);
         this.setRotationAngle(this.bone18, 2.3562F, 0.7854F, 0.0F);
         this.bone18.cubeList.add(new ModelBox(this.bone18, 20, 48, -1.0F, -1.0F, -6.0F, 2, 2, 6, 0.2F, true));
         (this.bone19 = new ModelRenderer(this)).setRotationPoint(1.0F, 1.0F, -6.0F);
         this.bone18.addChild(this.bone19);
         this.setRotationAngle(this.bone19, -0.7854F, 0.0F, 0.0F);
         this.bone19.cubeList.add(new ModelBox(this.bone19, 20, 48, -2.0F, -2.0F, -6.0F, 2, 2, 6, 0.0F, true));
         (this.bone20 = new ModelRenderer(this)).setRotationPoint(4.0F, 2.0F, -6.0F);
         this.rightFrontfoot.addChild(this.bone20);
         this.setRotationAngle(this.bone20, 0.3491F, -0.1745F, 0.0F);
         this.bone20.cubeList.add(new ModelBox(this.bone20, 20, 48, -2.0F, -1.0F, -6.0F, 2, 2, 6, 0.2F, true));
         (this.bone21 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -6.0F);
         this.bone20.addChild(this.bone21);
         this.setRotationAngle(this.bone21, 0.3491F, 0.0F, 0.0F);
         this.bone21.cubeList.add(new ModelBox(this.bone21, 20, 48, -2.0F, 1.0F, -6.0F, 2, 2, 6, 0.0F, true));
         (this.bone22 = new ModelRenderer(this)).setRotationPoint(1.0F, 2.0F, -6.0F);
         this.rightFrontfoot.addChild(this.bone22);
         this.setRotationAngle(this.bone22, 0.1745F, 0.0F, 0.0F);
         this.bone22.cubeList.add(new ModelBox(this.bone22, 20, 48, -2.0F, -1.0F, -6.0F, 2, 2, 6, 0.2F, true));
         (this.bone23 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -6.0F);
         this.bone22.addChild(this.bone23);
         this.setRotationAngle(this.bone23, 0.3491F, 0.0F, 0.0F);
         this.bone23.cubeList.add(new ModelBox(this.bone23, 20, 48, -2.0F, 1.0F, -6.0F, 2, 2, 6, 0.0F, true));
         (this.bone24 = new ModelRenderer(this)).setRotationPoint(-2.0F, 2.0F, -6.0F);
         this.rightFrontfoot.addChild(this.bone24);
         this.setRotationAngle(this.bone24, 0.3491F, 0.1745F, 0.0F);
         this.bone24.cubeList.add(new ModelBox(this.bone24, 20, 48, -2.0F, -1.0F, -6.0F, 2, 2, 6, 0.2F, true));
         (this.bone25 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -6.0F);
         this.bone24.addChild(this.bone25);
         this.setRotationAngle(this.bone25, 0.3491F, 0.0F, 0.0F);
         this.bone25.cubeList.add(new ModelBox(this.bone25, 20, 48, -2.0F, 1.0F, -6.0F, 2, 2, 6, 0.0F, true));
         (this.leftFrontLeg = new ModelRenderer(this)).setRotationPoint(12.0F, -14.0F, 2.0F);
         this.setRotationAngle(this.leftFrontLeg, 1.1345F, 0.0F, 0.0F);
         this.leftFrontLeg.cubeList.add(new ModelBox(this.leftFrontLeg, 112, 104, -4.0F, -4.0F, -4.0F, 8, 24, 8, 0.0F, true));
         (this.leftFrontlegtip = new ModelRenderer(this)).setRotationPoint(0.0F, 20.0F, 3.0F);
         this.leftFrontLeg.addChild(this.leftFrontlegtip);
         this.setRotationAngle(this.leftFrontlegtip, -0.3491F, 0.0F, 0.0F);
         this.leftFrontlegtip.cubeList.add(new ModelBox(this.leftFrontlegtip, 232, 104, -3.0F, -1.0F, -6.0F, 6, 24, 6, 0.0F, true));
         (this.leftFrontfoot = new ModelRenderer(this)).setRotationPoint(0.0F, 22.0F, -3.0F);
         this.leftFrontlegtip.addChild(this.leftFrontfoot);
         this.setRotationAngle(this.leftFrontfoot, 0.7854F, 0.0F, 0.0F);
         this.leftFrontfoot.cubeList.add(new ModelBox(this.leftFrontfoot, 148, 108, -4.0F, 0.0F, -8.0F, 8, 4, 12, 0.0F, true));
         (this.bone16 = new ModelRenderer(this)).setRotationPoint(-3.0F, 2.0F, 3.0F);
         this.leftFrontfoot.addChild(this.bone16);
         this.setRotationAngle(this.bone16, 2.3562F, -0.7854F, 0.0F);
         this.bone16.cubeList.add(new ModelBox(this.bone16, 20, 48, -1.0F, -1.0F, -6.0F, 2, 2, 6, 0.2F, false));
         (this.bone17 = new ModelRenderer(this)).setRotationPoint(-1.0F, 1.0F, -6.0F);
         this.bone16.addChild(this.bone17);
         this.setRotationAngle(this.bone17, -0.7854F, 0.0F, 0.0F);
         this.bone17.cubeList.add(new ModelBox(this.bone17, 20, 48, 0.0F, -2.0F, -6.0F, 2, 2, 6, 0.0F, false));
         (this.bone10 = new ModelRenderer(this)).setRotationPoint(-4.0F, 2.0F, -6.0F);
         this.leftFrontfoot.addChild(this.bone10);
         this.setRotationAngle(this.bone10, 0.3491F, 0.1745F, 0.0F);
         this.bone10.cubeList.add(new ModelBox(this.bone10, 20, 48, 0.0F, -1.0F, -6.0F, 2, 2, 6, 0.2F, false));
         (this.bone11 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -6.0F);
         this.bone10.addChild(this.bone11);
         this.setRotationAngle(this.bone11, 0.3491F, 0.0F, 0.0F);
         this.bone11.cubeList.add(new ModelBox(this.bone11, 20, 48, 0.0F, 1.0F, -6.0F, 2, 2, 6, 0.0F, false));
         (this.bone12 = new ModelRenderer(this)).setRotationPoint(-1.0F, 2.0F, -6.0F);
         this.leftFrontfoot.addChild(this.bone12);
         this.setRotationAngle(this.bone12, 0.1745F, 0.0F, 0.0F);
         this.bone12.cubeList.add(new ModelBox(this.bone12, 20, 48, 0.0F, -1.0F, -6.0F, 2, 2, 6, 0.2F, false));
         (this.bone13 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -6.0F);
         this.bone12.addChild(this.bone13);
         this.setRotationAngle(this.bone13, 0.3491F, 0.0F, 0.0F);
         this.bone13.cubeList.add(new ModelBox(this.bone13, 20, 48, 0.0F, 1.0F, -6.0F, 2, 2, 6, 0.0F, false));
         (this.bone14 = new ModelRenderer(this)).setRotationPoint(2.0F, 2.0F, -6.0F);
         this.leftFrontfoot.addChild(this.bone14);
         this.setRotationAngle(this.bone14, 0.3491F, -0.1745F, 0.0F);
         this.bone14.cubeList.add(new ModelBox(this.bone14, 20, 48, 0.0F, -1.0F, -6.0F, 2, 2, 6, 0.2F, false));
         (this.bone15 = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -6.0F);
         this.bone14.addChild(this.bone15);
         this.setRotationAngle(this.bone15, 0.3491F, 0.0F, 0.0F);
         this.bone15.cubeList.add(new ModelBox(this.bone15, 20, 48, 0.0F, 1.0F, -6.0F, 2, 2, 6, 0.0F, false));

         for(int i = 0; i < this.tail.length; ++i) {
            this.tail[i] = new ModelRenderer(this);
            this.tail[i].setRotationPoint(0.0F, -24.0F + (float)i * 2.0F, 56.0F + (float)i * 10.0F);
            this.tail[i].cubeList.add(new ModelBox(this.tail[i], 192, 104, -5.0F, -5.0F, 0.0F, 10, 10, 10, 0.0F, false));
         }

      }

      public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
         for(int i = 0; i < this.neck.length; ++i) {
            this.neck[i].render(f5);
         }

         this.head.render(f5);
         this.body.render(f5);
         this.rightWing.render(f5);
         this.leftWing.render(f5);
         this.rightRearLeg.render(f5);
         this.leftRearLeg.render(f5);
         this.rightFrontLeg.render(f5);
         this.leftFrontLeg.render(f5);

         for(int i = 0; i < this.tail.length; ++i) {
            this.tail[i].render(f5);
         }

      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
         float f3 = ((float)(entityIn.getEntityId() * 3) + ageInTicks) * 0.13F;
         this.setRotationAngle(this.leftWing, 0.0F, -0.1745F, MathHelper.cos(f3) * 32.0F * ((float)Math.PI / 180F));
         this.setRotationAngle(this.rightWing, 0.0F, 0.1745F, -this.leftWing.rotateAngleZ);
         this.setRotationAngle(this.leftWingtip, 0.0F, 0.0F, this.leftWing.rotateAngleZ);
         this.setRotationAngle(this.rightWingtip, 0.0F, 0.0F, -this.leftWingtip.rotateAngleZ);
         float f4 = MathHelper.cos(ageInTicks * 0.09F) * 0.15F;
         this.setRotationAngle(this.rightFrontLeg, 1.1345F + f4, -0.1745F, 0.0F);
         this.setRotationAngle(this.rightFrontlegtip, -0.3491F, 0.0F, 0.0F);
         this.setRotationAngle(this.rightFrontfoot, 1.0472F, 0.0F, 0.0F);
         this.setRotationAngle(this.leftFrontLeg, 1.1345F + f4, 0.1745F, 0.0F);
         this.setRotationAngle(this.leftFrontlegtip, -0.3491F, 0.0F, 0.0F);
         this.setRotationAngle(this.leftFrontfoot, 1.0472F, 0.0F, 0.0F);
         this.setRotationAngle(this.rightRearLeg, 1.0472F + f4, 0.0F, 0.0F);
         this.setRotationAngle(this.rightRearlegtip, 0.4363F, 0.0F, 0.0F);
         this.setRotationAngle(this.rightRearfoot, 0.7854F, 0.0F, 0.0F);
         this.setRotationAngle(this.leftRearLeg, 1.0472F + f4, 0.0F, 0.0F);
         this.setRotationAngle(this.leftRearlegtip, 0.4363F, 0.0F, 0.0F);
         this.setRotationAngle(this.leftRearfoot, 0.7854F, 0.0F, 0.0F);

         for(int i = 0; i < this.tail.length; ++i) {
            float weight = ((float)i + 1.0F) / (float)this.tail.length;
            float sway = MathHelper.sin(ageInTicks * 0.1F - (float)i * 0.4F) * 0.05F * weight;
            this.tail[i].rotateAngleY = sway;
            this.tail[i].rotateAngleX = -0.05F * weight;
         }

         this.head.rotateAngleX = 0.0873F + MathHelper.sin(ageInTicks * 0.08F) * 0.02F;
      }
   }
}
