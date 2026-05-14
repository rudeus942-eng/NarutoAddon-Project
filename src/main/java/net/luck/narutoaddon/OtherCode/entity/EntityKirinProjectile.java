
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
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
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
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
public class EntityKirinProjectile extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 270;

   public EntityKirinProjectile(ElementsInfTsukAddon instance) {
      super(instance, 270);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kirinprojectile"), 270).name("inftsuk_kirin_projectile").tracker(128, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KirinRenderer::new);
   }

   public static class EntityCustom extends Entity {
      private static final DataParameter<Float> SCALE;
      private int lifetime;
      private float damage;
      private float trueDamage;
      private boolean hasImpacted;
      private static final int CHARGE_TICKS = 40;
      private boolean launched;
      private double targetX;
      private double targetY;
      private double targetZ;

      public EntityCustom(World world) {
         super(world);
         this.lifetime = 0;
         this.damage = 100.0F;
         this.trueDamage = 50.0F;
         this.hasImpacted = false;
         this.launched = false;
         this.setSize(2.0F, 2.0F);
         this.noClip = true;
         this.isImmuneToFire = true;
      }

      public EntityCustom(World world, double x, double y, double z, float normalDmg, float trueDmg) {
         this(world);
         this.setPosition(x, y, z);
         this.damage = normalDmg;
         this.trueDamage = trueDmg;
      }

      public void setTargetPos(double tx, double ty, double tz) {
         this.targetX = tx;
         this.targetY = ty;
         this.targetZ = tz;
         this.faceTarget();
      }

      private void faceTarget() {
         double dx = this.targetX - this.posX;
         double dy = this.targetY - this.posY;
         double dz = this.targetZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         this.rotationYaw = (float)(Math.atan2(dx, dz) * (180D / Math.PI));
         this.rotationPitch = (float)(-(Math.atan2(dy, horizDist) * (180D / Math.PI)));
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
      }

      protected void entityInit() {
         this.dataManager.register(SCALE, 2.0F);
      }

      public boolean isLaunched() {
         return this.launched;
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 40) {
            this.motionX = (double)0.0F;
            this.motionY = (double)0.0F;
            this.motionZ = (double)0.0F;
            float growProgress = (float)this.lifetime / 40.0F;
            float currentScale = 2.0F + growProgress * 6.0F;
            this.dataManager.set(SCALE, currentScale);
            this.faceTarget();
            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY, this.posZ, (int)(5.0F + growProgress * 25.0F), (double)1.0F + (double)growProgress * (double)3.0F, (double)1.0F + (double)growProgress * (double)2.0F, (double)1.0F + (double)growProgress * (double)3.0F, 0.2, new int[0]);
               if (this.lifetime % 5 == 0) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY, this.posZ, (int)(3.0F + growProgress * 15.0F), (double)2.0F + (double)growProgress * (double)4.0F, (double)1.5F + (double)growProgress * (double)3.0F, (double)2.0F + (double)growProgress * (double)4.0F, 0.3, new int[0]);
               }
            }

            if (this.lifetime % 15 == 0) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 0.5F + growProgress * 1.5F, 0.5F + growProgress * 0.5F);
            }
         } else if (!this.launched) {
            this.launched = true;
            this.dataManager.set(SCALE, 8.0F);
            double dx = this.targetX - this.posX;
            double dy = this.targetY - this.posY;
            double dz = this.targetZ - this.posZ;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > (double)0.0F) {
               double speed = (double)3.0F;
               this.motionX = dx / len * speed;
               this.motionY = dy / len * speed;
               this.motionZ = dz / len * speed;
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 3.0F, 0.5F);
         } else {
            EntityPlayer nearest = this.world.getClosestPlayerToEntity(this, (double)30.0F);
            if (nearest != null && nearest.isEntityAlive()) {
               this.targetX = nearest.posX;
               this.targetY = nearest.posY;
               this.targetZ = nearest.posZ;
            }

            double dx = this.targetX - this.posX;
            double dy = this.targetY - this.posY;
            double dz = this.targetZ - this.posZ;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > (double)0.0F) {
               double speed = (double)3.0F;
               this.motionX = this.motionX * 0.85 + dx / len * speed * 0.15;
               this.motionY = this.motionY * 0.85 + dy / len * speed * 0.15;
               this.motionZ = this.motionZ * 0.85 + dz / len * speed * 0.15;
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            this.setPosition(this.posX, this.posY, this.posZ);
            double horizSpeed = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
            this.rotationPitch = (float)(-(Math.atan2(this.motionY, horizSpeed) * (180D / Math.PI)));
            if (this.world instanceof WorldServer && this.lifetime % 2 == 0) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY, this.posZ, 15, (double)2.0F, (double)2.0F, (double)2.0F, 0.15, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY, this.posZ, 5, (double)1.5F, (double)1.5F, (double)1.5F, 0.2, new int[0]);
            }

            if (!this.world.isRemote && !this.hasImpacted) {
               int groundY = this.world.getHeight((int)Math.floor(this.posX), (int)Math.floor(this.posZ));
               if (this.posY <= (double)(groundY + 3)) {
                  this.onImpact();
               }
            }
         }

         if (this.lifetime > 150 && !this.hasImpacted) {
            this.setDead();
         }

      }

      private void onImpact() {
         if (!this.hasImpacted && !this.world.isRemote) {
            this.hasImpacted = true;

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow((double)8.0F))) {
               if (p.isEntityAlive() && !p.isSpectator()) {
                  p.attackEntityFrom(DamageSource.MAGIC, this.damage);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom((new DamageSource("trueDamage")).setDamageBypassesArmor(), this.trueDamage);
                  double kbX = p.posX - this.posX;
                  double kbZ = p.posZ - this.posZ;
                  double dist = Math.sqrt(kbX * kbX + kbZ * kbZ);
                  if (dist > (double)0.0F) {
                     p.motionX = kbX / dist * (double)1.5F;
                     p.motionZ = kbZ / dist * (double)1.5F;
                  }

                  p.motionY = 0.8;
                  p.velocityChanged = true;
                  p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 2));
               }
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + (double)1.0F, this.posZ, 100, (double)6.0F, (double)5.0F, (double)6.0F, (double)0.5F, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY + (double)2.0F, this.posZ, 60, (double)5.0F, (double)4.0F, (double)5.0F, (double)0.5F, new int[0]);
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)1.0F, this.posZ, 8, (double)4.0F, (double)3.0F, (double)4.0F, (double)0.0F, new int[0]);
            }

            this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX, this.posY, this.posZ, true));
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 3.0F, 0.4F);
            this.setDead();
         }
      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.lifetime = compound.getInteger("lifetime");
         this.damage = compound.getFloat("damage");
         this.trueDamage = compound.getFloat("trueDamage");
         this.hasImpacted = compound.getBoolean("hasImpacted");
      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("damage", this.damage);
         compound.setFloat("trueDamage", this.trueDamage);
         compound.setBoolean("hasImpacted", this.hasImpacted);
      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      public float getEntityScale() {
         return (Float)this.dataManager.get(SCALE);
      }

      static {
         SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class KirinRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/dragon_lightning.png");
      private static final ResourceLocation TEXTURE_ELECTRIC = new ResourceLocation("narutomod:textures/electric_armor.png");
      private final ModelKirinDragon model = new ModelKirinDragon();

      public KirinRenderer(RenderManager rm) {
         super(rm);
         this.shadowSize = 0.0F;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float yaw, float pt) {
         float age = (float)entity.ticksExisted + pt;
         float scale = entity.getEntityScale();
         float alpha = Math.min(age / 20.0F, 1.0F);
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)x, (float)y + scale, (float)z);
         float renderYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * pt;
         float renderPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * pt;
         GlStateManager.rotate(renderYaw, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(renderPitch, 1.0F, 0.0F, 0.0F);
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.enableBlend();
         GlStateManager.alphaFunc(516, 0.001F);
         GlStateManager.disableCull();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         this.model.setRotationAngles(0.0F, 0.0F, age, 0.0F, 0.0F, 0.0625F, entity);
         this.bindEntityTexture(entity);
         GlStateManager.color(1.0F, 1.0F, 1.0F, alpha * 0.5F);
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         this.bindTexture(TEXTURE_ELECTRIC);
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         GlStateManager.translate(age * 0.01F, age * 0.01F, 0.0F);
         GlStateManager.matrixMode(5888);
         GlStateManager.color(1.0F, 1.0F, 1.0F, alpha * 0.5F);
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.065625F);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         GlStateManager.matrixMode(5888);
         this.bindEntityTexture(entity);
         GlStateManager.color(0.0F, 0.0F, 1.0F, alpha * 0.3F);
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.06875F);
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelKirinDragon extends ModelBase {
      private final ModelRenderer head;
      private final ModelRenderer teethUpper;
      private final ModelRenderer flair;
      private final ModelRenderer bone;
      private final ModelRenderer bone2;
      private final ModelRenderer bone3;
      private final ModelRenderer jaw;
      private final ModelRenderer teethLower;
      private final ModelRenderer hornRight;
      private final ModelRenderer hornRight0;
      private final ModelRenderer hornRight1;
      private final ModelRenderer hornRight2;
      private final ModelRenderer hornRight3;
      private final ModelRenderer hornRight4;
      private final ModelRenderer hornLeft;
      private final ModelRenderer hornLeft0;
      private final ModelRenderer hornLeft1;
      private final ModelRenderer hornLeft2;
      private final ModelRenderer hornLeft3;
      private final ModelRenderer hornLeft4;
      private final ModelRenderer[] whiskerLeft = new ModelRenderer[6];
      private final ModelRenderer[] whiskerRight = new ModelRenderer[6];
      private final ModelRenderer[] spine = new ModelRenderer[10];
      private final ModelRenderer eyes;
      private static final int SPINE_COUNT = 10;

      public ModelKirinDragon() {
         this.textureWidth = 128;
         this.textureHeight = 128;
         (this.head = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.head.cubeList.add(new ModelBox(this.head, 64, 0, -6.0F, 6.0F, -26.0F, 12, 5, 16, 1.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 0, 0, -8.0F, -1.0F, -11.0F, 16, 16, 16, 1.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 32, 32, 2.0F, 4.0F, -28.0F, 4, 4, 6, 0.0F, true));
         this.head.cubeList.add(new ModelBox(this.head, 32, 32, -6.0F, 4.0F, -28.0F, 4, 4, 6, 0.0F, false));
         (this.teethUpper = new ModelRenderer(this)).setRotationPoint(0.0F, 24.0F, 0.0F);
         this.head.addChild(this.teethUpper);
         this.teethUpper.cubeList.add(new ModelBox(this.teethUpper, 0, 52, -6.0F, -12.0F, -26.0F, 12, 3, 16, 0.5F, false));
         (this.flair = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -12.0F);
         this.head.addChild(this.flair);
         (this.bone = new ModelRenderer(this)).setRotationPoint(9.0F, 9.0F, 0.0F);
         this.flair.addChild(this.bone);
         this.setRotationAngle(this.bone, 0.0F, -0.7854F, 0.0F);
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 52, 0.0F, -8.0F, 0.0F, 10, 16, 0, 0.0F, false));
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 52, -2.0F, -12.0F, 2.0F, 10, 16, 0, 0.0F, false));
         (this.bone2 = new ModelRenderer(this)).setRotationPoint(-9.0F, 9.0F, 0.0F);
         this.flair.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.0F, 0.7854F, 0.0F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, 52, -10.0F, -8.0F, 0.0F, 10, 16, 0, 0.0F, true));
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, 52, -8.0F, -12.0F, 2.0F, 10, 16, 0, 0.0F, true));
         (this.bone3 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.flair.addChild(this.bone3);
         this.setRotationAngle(this.bone3, -0.8727F, 0.0F, 0.0F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 84, 42, -8.0F, -10.0F, 0.0F, 16, 10, 0, 0.0F, false));
         (this.jaw = new ModelRenderer(this)).setRotationPoint(0.0F, 11.0F, -9.0F);
         this.head.addChild(this.jaw);
         this.setRotationAngle(this.jaw, 0.7854F, 0.0F, 0.0F);
         this.jaw.cubeList.add(new ModelBox(this.jaw, 64, 22, -6.0F, 0.0F, -16.75F, 12, 4, 16, 1.0F, false));
         (this.teethLower = new ModelRenderer(this)).setRotationPoint(0.0F, 13.0F, 9.0F);
         this.jaw.addChild(this.teethLower);
         this.teethLower.cubeList.add(new ModelBox(this.teethLower, 42, 42, -6.0F, -16.0F, -25.75F, 12, 2, 16, 0.5F, false));
         (this.hornRight = new ModelRenderer(this)).setRotationPoint(-6.0F, -2.0F, -13.0F);
         this.head.addChild(this.hornRight);
         this.setRotationAngle(this.hornRight, 0.0873F, -0.5236F, 0.0F);
         this.hornRight.cubeList.add(new ModelBox(this.hornRight, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, false));
         (this.hornRight0 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornRight.addChild(this.hornRight0);
         this.setRotationAngle(this.hornRight0, 0.0873F, 0.0873F, 0.0F);
         this.hornRight0.cubeList.add(new ModelBox(this.hornRight0, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.8F, false));
         (this.hornRight1 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornRight0.addChild(this.hornRight1);
         this.setRotationAngle(this.hornRight1, 0.0873F, 0.0873F, 0.0F);
         this.hornRight1.cubeList.add(new ModelBox(this.hornRight1, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.6F, false));
         (this.hornRight2 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornRight1.addChild(this.hornRight2);
         this.setRotationAngle(this.hornRight2, 0.0873F, 0.0873F, 0.0F);
         this.hornRight2.cubeList.add(new ModelBox(this.hornRight2, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.4F, false));
         (this.hornRight3 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornRight2.addChild(this.hornRight3);
         this.setRotationAngle(this.hornRight3, 0.0873F, 0.0873F, 0.0F);
         this.hornRight3.cubeList.add(new ModelBox(this.hornRight3, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.2F, false));
         (this.hornRight4 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornRight3.addChild(this.hornRight4);
         this.setRotationAngle(this.hornRight4, 0.0873F, 0.0873F, 0.0F);
         this.hornRight4.cubeList.add(new ModelBox(this.hornRight4, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.0F, false));
         (this.hornLeft = new ModelRenderer(this)).setRotationPoint(6.0F, -2.0F, -13.0F);
         this.head.addChild(this.hornLeft);
         this.setRotationAngle(this.hornLeft, 0.0873F, 0.5236F, 0.0F);
         this.hornLeft.cubeList.add(new ModelBox(this.hornLeft, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, true));
         (this.hornLeft0 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornLeft.addChild(this.hornLeft0);
         this.setRotationAngle(this.hornLeft0, 0.0873F, -0.0873F, 0.0F);
         this.hornLeft0.cubeList.add(new ModelBox(this.hornLeft0, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.8F, true));
         (this.hornLeft1 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornLeft0.addChild(this.hornLeft1);
         this.setRotationAngle(this.hornLeft1, 0.0873F, -0.0873F, 0.0F);
         this.hornLeft1.cubeList.add(new ModelBox(this.hornLeft1, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.6F, true));
         (this.hornLeft2 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornLeft1.addChild(this.hornLeft2);
         this.setRotationAngle(this.hornLeft2, 0.0873F, -0.0873F, 0.0F);
         this.hornLeft2.cubeList.add(new ModelBox(this.hornLeft2, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.4F, true));
         (this.hornLeft3 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornLeft2.addChild(this.hornLeft3);
         this.setRotationAngle(this.hornLeft3, 0.0873F, -0.0873F, 0.0F);
         this.hornLeft3.cubeList.add(new ModelBox(this.hornLeft3, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.2F, true));
         (this.hornLeft4 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornLeft3.addChild(this.hornLeft4);
         this.setRotationAngle(this.hornLeft4, 0.0873F, -0.0873F, 0.0F);
         this.hornLeft4.cubeList.add(new ModelBox(this.hornLeft4, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.0F, true));
         (this.whiskerLeft[0] = new ModelRenderer(this)).setRotationPoint(6.0F, 6.0F, -24.0F);
         this.head.addChild(this.whiskerLeft[0]);
         this.setRotationAngle(this.whiskerLeft[0], 0.0F, 1.0472F, 0.0F);
         this.whiskerLeft[0].cubeList.add(new ModelBox(this.whiskerLeft[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, true));
         (this.whiskerLeft[1] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerLeft[0].addChild(this.whiskerLeft[1]);
         this.setRotationAngle(this.whiskerLeft[1], -0.0873F, -0.1745F, 0.0F);
         this.whiskerLeft[1].cubeList.add(new ModelBox(this.whiskerLeft[1], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.7F, true));
         (this.whiskerLeft[2] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerLeft[1].addChild(this.whiskerLeft[2]);
         this.setRotationAngle(this.whiskerLeft[2], -0.0873F, -0.1745F, 0.0F);
         this.whiskerLeft[2].cubeList.add(new ModelBox(this.whiskerLeft[2], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.6F, true));
         (this.whiskerLeft[3] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerLeft[2].addChild(this.whiskerLeft[3]);
         this.setRotationAngle(this.whiskerLeft[3], -0.0873F, -0.1745F, 0.0F);
         this.whiskerLeft[3].cubeList.add(new ModelBox(this.whiskerLeft[3], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.5F, true));
         (this.whiskerLeft[4] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerLeft[3].addChild(this.whiskerLeft[4]);
         this.setRotationAngle(this.whiskerLeft[4], -0.0873F, -0.1745F, 0.0F);
         this.whiskerLeft[4].cubeList.add(new ModelBox(this.whiskerLeft[4], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.4F, true));
         (this.whiskerLeft[5] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerLeft[4].addChild(this.whiskerLeft[5]);
         this.setRotationAngle(this.whiskerLeft[5], -0.0873F, -0.1745F, 0.0F);
         this.whiskerLeft[5].cubeList.add(new ModelBox(this.whiskerLeft[5], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.2F, true));
         (this.whiskerRight[0] = new ModelRenderer(this)).setRotationPoint(-6.0F, 6.0F, -24.0F);
         this.head.addChild(this.whiskerRight[0]);
         this.setRotationAngle(this.whiskerRight[0], 0.0F, -1.0472F, 0.0F);
         this.whiskerRight[0].cubeList.add(new ModelBox(this.whiskerRight[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, false));
         (this.whiskerRight[1] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerRight[0].addChild(this.whiskerRight[1]);
         this.setRotationAngle(this.whiskerRight[1], -0.0873F, 0.1745F, 0.0F);
         this.whiskerRight[1].cubeList.add(new ModelBox(this.whiskerRight[1], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.7F, false));
         (this.whiskerRight[2] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerRight[1].addChild(this.whiskerRight[2]);
         this.setRotationAngle(this.whiskerRight[2], -0.0873F, 0.1745F, 0.0F);
         this.whiskerRight[2].cubeList.add(new ModelBox(this.whiskerRight[2], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.6F, false));
         (this.whiskerRight[3] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerRight[2].addChild(this.whiskerRight[3]);
         this.setRotationAngle(this.whiskerRight[3], -0.0873F, 0.1745F, 0.0F);
         this.whiskerRight[3].cubeList.add(new ModelBox(this.whiskerRight[3], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.5F, false));
         (this.whiskerRight[4] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerRight[3].addChild(this.whiskerRight[4]);
         this.setRotationAngle(this.whiskerRight[4], -0.0873F, 0.1745F, 0.0F);
         this.whiskerRight[4].cubeList.add(new ModelBox(this.whiskerRight[4], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.4F, false));
         (this.whiskerRight[5] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerRight[4].addChild(this.whiskerRight[5]);
         this.setRotationAngle(this.whiskerRight[5], -0.0873F, 0.1745F, 0.0F);
         this.whiskerRight[5].cubeList.add(new ModelBox(this.whiskerRight[5], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.2F, false));

         for(int i = 0; i < 10; ++i) {
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

         (this.eyes = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.eyes.cubeList.add(new ModelBox(this.eyes, 18, 20, -6.6F, 2.6F, -12.15F, 3, 2, 0, 0.0F, false));
         this.eyes.cubeList.add(new ModelBox(this.eyes, 18, 20, 3.6F, 2.6F, -12.15F, 3, 2, 0, 0.0F, true));
      }

      public void render(Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
         this.head.render(f5);
         this.spine[0].render(f5);
         this.eyes.render(f5);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
         super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
         this.jaw.rotateAngleX = 0.5236F;
         float whiskerWave = ageInTicks * 0.15F;

         for(int i = 2; i < 6; ++i) {
            this.whiskerLeft[i].rotateAngleZ = MathHelper.sin(whiskerWave + (float)i * 0.5F) * 0.2618F;
            this.whiskerRight[i].rotateAngleZ = -MathHelper.sin(whiskerWave + (float)i * 0.5F) * 0.2618F;
         }

         float spineWave = ageInTicks * 0.1F;

         for(int i = 0; i < 10; ++i) {
            this.spine[i].showModel = true;
            this.spine[i].rotateAngleY = MathHelper.sin(spineWave + (float)i * 0.8F) * 0.15F;
            this.spine[i].rotateAngleX = MathHelper.cos(spineWave + (float)i * 0.6F) * 0.05F;
         }

      }
   }
}
