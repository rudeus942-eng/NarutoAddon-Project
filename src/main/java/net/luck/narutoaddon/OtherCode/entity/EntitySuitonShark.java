
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.item.ItemJutsu;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntitySuitonShark extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 208;

   public EntitySuitonShark(ElementsInfTsukAddon instance) {
      super(instance, 35);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "suiton_shark"), 208).name("inftsuk_suiton_shark").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SharkRenderer::new);
   }

   public static class EntityCustom extends Entity {
      private static final DataParameter<Integer> TARGET_ID;
      private int lifetime = 0;
      private float damage = 9.0F;
      private float trueDamage = 4.0F;
      private EntityLivingBase owner;
      private double speed = 0.7;

      public EntityCustom(World world) {
         super(world);
         this.setSize(1.0F, 0.5F);
         this.noClip = true;
      }

      public EntityCustom(World world, EntityLivingBase owner, EntityLivingBase target, float normalDmg, float trueDmg) {
         super(world);
         this.setSize(1.0F, 0.5F);
         this.noClip = true;
         this.owner = owner;
         this.damage = normalDmg;
         this.trueDamage = trueDmg;
         this.setPosition(owner.posX, owner.posY + (double)owner.getEyeHeight(), owner.posZ);
         if (target != null) {
            this.dataManager.set(TARGET_ID, target.getEntityId());
         }

      }

      protected void entityInit() {
         this.dataManager.register(TARGET_ID, -1);
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 100) {
            this.setDead();
         } else {
            Entity target = null;
            int targetId = (Integer)this.dataManager.get(TARGET_ID);
            if (targetId > 0) {
               target = this.world.getEntityByID(targetId);
            }

            if (!this.world.isRemote) {
               if (target != null && target.isEntityAlive()) {
                  double dx = target.posX - this.posX;
                  double dy = target.posY + (double)(target.height / 2.0F) - this.posY;
                  double dz = target.posZ - this.posZ;
                  double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                  if (dist > 0.1) {
                     this.motionX = dx / dist * this.speed;
                     this.motionY = dy / dist * this.speed;
                     this.motionZ = dz / dist * this.speed;
                  }

                  float yaw = (float)(MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
                  this.rotationYaw = yaw;
                  this.rotationPitch = (float)(-(MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180D / Math.PI)));
               }

               this.posX += this.motionX;
               this.posY += this.motionY;
               this.posZ += this.motionZ;
               this.setPosition(this.posX, this.posY, this.posZ);
               AxisAlignedBB hitBox = this.getEntityBoundingBox().grow(0.3);

               for(Entity hit : this.world.getEntitiesWithinAABBExcludingEntity(this, hitBox)) {
                  if (hit != this.owner && hit instanceof EntityLivingBase) {
                     hit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.owner), this.damage);
                     hit.hurtResistantTime = 0;
                     hit.attackEntityFrom(DamageSource.MAGIC, this.trueDamage);
                     double kbX = hit.posX - this.posX;
                     double kbZ = hit.posZ - this.posZ;
                     double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
                     if (kbDist > (double)0.0F) {
                        hit.motionX += kbX / kbDist * (double)0.5F;
                        hit.motionY += 0.2;
                        hit.motionZ += kbZ / kbDist * (double)0.5F;
                        hit.velocityChanged = true;
                     }

                     for(int i = 0; i < 30; ++i) {
                        this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                     }

                     this.setDead();
                     return;
                  }
               }
            }

            if (this.world.isRemote) {
               for(int i = 0; i < 5; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.8, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.8, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
               }

               this.world.spawnParticle(EnumParticleTypes.WATER_DROP, this.posX, this.posY, this.posZ, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
            }

         }
      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("sharkDmg")) {
            this.damage = compound.getFloat("sharkDmg");
         }

         if (compound.hasKey("sharkTrueDmg")) {
            this.trueDamage = compound.getFloat("sharkTrueDmg");
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("sharkDmg", this.damage);
         compound.setFloat("sharkTrueDmg", this.trueDamage);
      }

      static {
         TARGET_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
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
         this.body.render(f5);
      }

      public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
         super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
         this.tail.rotateAngleY = MathHelper.cos(limbSwing * 0.6662F) * 0.4F * limbSwingAmount;
         this.tailFin.rotateAngleY = MathHelper.cos(limbSwing * 0.6662F) * 0.4F * limbSwingAmount;
         this.foreHead.rotateAngleX = 0.1745F - headPitch * 0.4363F;
         this.jaw.rotateAngleX = headPitch * 0.5236F;
      }

      public void setRotationAngle(ModelRenderer renderer, float x, float y, float z) {
         renderer.rotateAngleX = x;
         renderer.rotateAngleY = y;
         renderer.rotateAngleZ = z;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class SharkRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/shark.png");
      private final ModelShark model = new ModelShark();

      public SharkRenderer(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.5F;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         float limbSwingAmount = 0.8F;
         float limbSwing = (float)entity.ticksExisted + partialTicks;
         float mouthOpen = 0.3F;
         this.model.setRotationAngles(limbSwing, limbSwingAmount, (float)entity.ticksExisted + partialTicks, 0.0F, mouthOpen, 0.0625F, entity);
         this.bindEntityTexture(entity);
         GlStateManager.pushMatrix();
         float scale = 1.5F;
         GlStateManager.translate((float)x, (float)y, (float)z);
         float prevYaw = entity.prevRotationYaw;
         float yaw = prevYaw + MathHelper.wrapDegrees(entity.rotationYaw - prevYaw) * partialTicks;
         GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F);
         float prevPitch = entity.prevRotationPitch;
         float pitch = prevPitch + (entity.rotationPitch - prevPitch) * partialTicks;
         GlStateManager.rotate(pitch - 180.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.disableLighting();
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.enableLighting();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }
}
