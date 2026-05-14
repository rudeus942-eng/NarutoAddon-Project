
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityIceMirrors extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 216;

   public EntityIceMirrors(ElementsInfTsukAddon instance) {
      super(instance, 44);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "ice_mirrors"), 216).name("inftsuk_ice_mirrors").tracker(64, 3, false).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, IceMirrorsRenderer::new);
   }

   public static class EntityCustom extends Entity {
      private int lifetime;
      private int maxLifetime;
      private float damagePerTick;
      private float radius;
      private UUID casterUUID;

      public EntityCustom(World world) {
         super(world);
         this.lifetime = 0;
         this.maxLifetime = 100;
         this.damagePerTick = 3.0F;
         this.radius = 6.0F;
         this.casterUUID = null;
         this.setSize(12.8F, 6.4F);
         this.noClip = true;
      }

      public EntityCustom(World world, EntityLivingBase caster, double x, double y, double z) {
         this(world);
         this.casterUUID = caster.getUniqueID();
         this.setPosition(x, y, z);
      }

      protected void entityInit() {
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > this.maxLifetime) {
            if (!this.world.isRemote) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 1.5F, 0.6F);
            }

            this.setDead();
         } else {
            if (!this.world.isRemote) {
               if (this.lifetime % 10 == 0) {
                  EntityLivingBase caster = this.findCaster();
                  AxisAlignedBB aoe = new AxisAlignedBB(this.posX - (double)this.radius, this.posY - (double)0.5F, this.posZ - (double)this.radius, this.posX + (double)this.radius, this.posY + (double)3.0F, this.posZ + (double)this.radius);

                  for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, aoe, (e) -> e != null && !e.isDead && !this.isCaster(e))) {
                     double dx = target.posX - this.posX;
                     double dz = target.posZ - this.posZ;
                     if (dx * dx + dz * dz <= (double)(this.radius * this.radius)) {
                        target.hurtResistantTime = 0;
                        target.attackEntityFrom(DamageSource.MAGIC, this.damagePerTick);
                        target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1));
                     }
                  }
               }

               if (this.lifetime % 20 == 0) {
                  EntityLivingBase caster = this.findCaster();
                  if (caster != null) {
                     double centerY = this.posY + (double)1.0F;
                     double[][] offsets = new double[][]{{(double)0.0F, (double)this.radius}, {(double)this.radius, (double)0.0F}, {(double)0.0F, (double)(-this.radius)}, {(double)(-this.radius), (double)0.0F}};

                     for(double[] off : offsets) {
                        double nx = this.posX + off[0];
                        double nz = this.posZ + off[1];
                        EntityIceNeedle.EntityCustom needle = new EntityIceNeedle.EntityCustom(this.world, caster, 4.0F, 2.0F);
                        needle.setPosition(nx, centerY, nz);
                        double dx = this.posX - nx;
                        double dy = centerY - centerY;
                        double dz = this.posZ - nz;
                        double dist = Math.sqrt(dx * dx + dz * dz);
                        if (dist > (double)0.0F) {
                           double speed = 0.8;
                           needle.shoot(dx / dist, dy, dz / dist, (float)speed, 0.0F);
                        }

                        this.world.spawnEntity(needle);
                     }

                     SoundEvent iceShoot = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod", "ice_shoot_small"));
                     if (iceShoot != null) {
                        this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, iceShoot, SoundCategory.HOSTILE, 1.0F, 0.8F + this.rand.nextFloat() * 0.4F);
                     }
                  }
               }
            } else {
               for(int i = 0; i < 8; ++i) {
                  double angle = (double)this.lifetime * 0.1 + (double)i * Math.PI * (double)2.0F / (double)8.0F;
                  double px = this.posX + Math.cos(angle) * (double)this.radius;
                  double pz = this.posZ + Math.sin(angle) * (double)this.radius;
                  double py = this.posY + this.rand.nextDouble() * (double)2.5F;
                  this.world.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, px, py, pz, (this.rand.nextDouble() - (double)0.5F) * 0.05, this.rand.nextDouble() * 0.1, (this.rand.nextDouble() - (double)0.5F) * 0.05, new int[0]);
               }

               for(int i = 0; i < 4; ++i) {
                  this.world.spawnParticle(EnumParticleTypes.SNOWBALL, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posY + this.rand.nextDouble() * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, (double)0.0F, 0.05 + this.rand.nextDouble() * 0.05, (double)0.0F, new int[0]);
               }

               if (this.lifetime % 10 == 0) {
                  for(int i = 0; i < 20; ++i) {
                     double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
                     double r = this.rand.nextDouble() * (double)this.radius;
                     this.world.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, this.posX + Math.cos(angle) * r, this.posY + this.rand.nextDouble() * (double)2.5F, this.posZ + Math.sin(angle) * r, (this.rand.nextDouble() - (double)0.5F) * 0.2, this.rand.nextDouble() * 0.15, (this.rand.nextDouble() - (double)0.5F) * 0.2, new int[0]);
                  }
               }
            }

         }
      }

      private boolean isCaster(EntityLivingBase entity) {
         return this.casterUUID == null ? false : this.casterUUID.equals(entity.getUniqueID());
      }

      private EntityLivingBase findCaster() {
         if (this.casterUUID == null) {
            return null;
         } else {
            EntityPlayer player = this.world.getPlayerEntityByUUID(this.casterUUID);
            if (player != null) {
               return player;
            } else {
               List<EntityLivingBase> nearby = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)32.0F, this.posY - (double)16.0F, this.posZ - (double)32.0F, this.posX + (double)32.0F, this.posY + (double)16.0F, this.posZ + (double)32.0F), (e) -> e != null && this.casterUUID.equals(e.getUniqueID()));
               return nearby.isEmpty() ? null : (EntityLivingBase)nearby.get(0);
            }
         }
      }

      public boolean isEntityInvulnerable(DamageSource source) {
         return true;
      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      public boolean canBeAttackedWithItem() {
         return false;
      }

      protected boolean canTriggerWalking() {
         return false;
      }

      public AxisAlignedBB getCollisionBoundingBox() {
         return null;
      }

      public AxisAlignedBB getCollisionBox(Entity entityIn) {
         return null;
      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("lifetime", this.lifetime);
         compound.setInteger("maxLifetime", this.maxLifetime);
         compound.setFloat("damagePerTick", this.damagePerTick);
         compound.setFloat("radius", this.radius);
         if (this.casterUUID != null) {
            compound.setLong("casterUUIDMost", this.casterUUID.getMostSignificantBits());
            compound.setLong("casterUUIDLeast", this.casterUUID.getLeastSignificantBits());
         }

      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("maxLifetime")) {
            this.maxLifetime = compound.getInteger("maxLifetime");
         }

         if (compound.hasKey("damagePerTick")) {
            this.damagePerTick = compound.getFloat("damagePerTick");
         }

         if (compound.hasKey("radius")) {
            this.radius = compound.getFloat("radius");
         }

         if (compound.hasKey("casterUUIDMost") && compound.hasKey("casterUUIDLeast")) {
            this.casterUUID = new UUID(compound.getLong("casterUUIDMost"), compound.getLong("casterUUIDLeast"));
         }

      }

      public float getRadius() {
         return this.radius;
      }

      public void setMaxLifetime(int ticks) {
         this.maxLifetime = ticks;
      }

      public void setDamagePerTick(float damage) {
         this.damagePerTick = damage;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelDome extends ModelBase {
      private final ModelRenderer dome;

      public ModelDome() {
         this.textureWidth = 16;
         this.textureHeight = 16;
         this.dome = new ModelRenderer(this);
         this.dome.setRotationPoint(0.0F, 0.0F, 0.0F);
         ModelRenderer wall = new ModelRenderer(this);
         wall.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.dome.addChild(wall);
         this.addWallPanel(wall, 0.0F, -4.0F, -13.5F, false);
         this.addWallPanel(wall, -0.3927F, -4.0F, -13.5F, false);
         this.addWallPanel(wall, 0.0F, -4.0F, 13.7F, false);
         this.addWallPanel(wall, -0.3927F, -4.0F, 13.7F, false);
         this.addWallPanel(wall, -0.7854F, -3.8891F, -13.5459F, false);
         this.addWallPanel(wall, -1.1781F, -3.8891F, -13.5459F, false);
         this.addWallPanel(wall, -0.7854F, -3.8891F, 13.6874F, false);
         this.addWallPanel(wall, -1.1781F, -3.8891F, 13.6874F, false);
         this.addWallPanel(wall, -1.5708F, -3.85F, -13.65F, false);
         this.addWallPanel(wall, -1.9635F, -3.85F, -13.65F, false);
         this.addWallPanel(wall, -1.5708F, -3.9F, 13.6F, false);
         this.addWallPanel(wall, -1.9635F, -3.9F, 13.6F, false);
         this.addWallPanel(wall, 0.7854F, -4.1316F, 13.7331F, false);
         this.addWallPanel(wall, 0.3927F, -4.1316F, 13.7331F, false);
         this.addWallPanel(wall, 0.7854F, -4.0609F, -13.5709F, false);
         this.addWallPanel(wall, 0.3927F, -4.0609F, -13.5709F, false);
         ModelRenderer roof = new ModelRenderer(this);
         roof.setRotationPoint(0.0F, -16.0F, -9.5F);
         this.dome.addChild(roof);
         this.addRoofFlat(roof, -3.95F, -8.15F);
         this.addRoofFlat(roof, -8.95F, -4.15F);
         this.addRoofFlat(roof, -3.95F, -0.15F);
         this.addRoofFlat(roof, 1.05F, -4.15F);
         float[] roofAngles = new float[]{1.5708F, 2.0944F, 3.1416F, -2.618F, -2.0944F, -1.5708F, 2.618F, 1.0472F, 0.5236F, 0.0F, -0.5236F, -1.0472F};

         for(float angle : roofAngles) {
            this.addRoofAngled(roof, angle);
         }

         ModelRenderer bottom = new ModelRenderer(this);
         bottom.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.dome.addChild(bottom);
         this.addFloorPanel(bottom, -1.5708F, -4.0074F);
         this.addFloorPanel(bottom, -0.7854F, 4.9926F);
         this.addFloorPanel(bottom, -1.5708F, 4.9926F);
         this.addFloorPanel(bottom, 0.7854F, 4.75F);
         this.addFloorPanel(bottom, 0.0F, 4.75F);
         this.addFloorPanel(bottom, -2.3562F, 4.75F);
         this.addFloorPanel(bottom, 3.1416F, 4.75F);
         this.addFloorPanel(bottom, 2.3562F, 4.75F);
         this.addFloorPanel(bottom, 1.5708F, 4.75F);
      }

      private void addWallPanel(ModelRenderer parent, float yRot, float boxX, float boxZ, boolean mirror) {
         ModelRenderer panel = new ModelRenderer(this);
         panel.setRotationPoint(0.0F, 0.0F, 0.0F);
         if (yRot != 0.0F) {
            panel.rotateAngleY = yRot;
         }

         panel.cubeList.add(new ModelBox(panel, 0, 0, boxX, -8.0F, boxZ, 8, 8, 0, 0.0F, mirror));
         parent.addChild(panel);
      }

      private void addRoofFlat(ModelRenderer parent, float boxX, float boxY) {
         ModelRenderer panel = new ModelRenderer(this);
         panel.setRotationPoint(0.0F, 2.7F, 9.5F);
         panel.rotateAngleX = -1.5708F;
         panel.cubeList.add(new ModelBox(panel, 0, 8, boxX, boxY, 0.0F, 8, 8, 0, 0.0F, false));
         parent.addChild(panel);
      }

      private void addRoofAngled(ModelRenderer parent, float yRot) {
         ModelRenderer panel = new ModelRenderer(this);
         panel.setRotationPoint(0.0F, 0.0F, 9.5F);
         panel.rotateAngleX = -0.7854F;
         panel.rotateAngleY = yRot;
         panel.cubeList.add(new ModelBox(panel, 0, 8, -4.1499F, 7.3089F, -3.8951F, 8, 8, 0, 0.0F, true));
         parent.addChild(panel);
      }

      private void addFloorPanel(ModelRenderer parent, float yRot, float boxZ) {
         ModelRenderer panel = new ModelRenderer(this);
         panel.setRotationPoint(0.0F, 0.0F, 0.0F);
         if (yRot != 0.0F) {
            panel.rotateAngleY = yRot;
         }

         panel.cubeList.add(new ModelBox(panel, 0, 0, -4.0F, 0.0F, boxZ, 8, 0, 8, 0.0F, false));
         parent.addChild(panel);
      }

      public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(8.0F, 8.0F, 8.0F);
         this.dome.render(scale);
         GlStateManager.popMatrix();
      }
   }

   @SideOnly(Side.CLIENT)
   public static class IceMirrorsRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod", "textures/dome_ice.png");
      private final ModelDome model = new ModelDome();

      public IceMirrorsRenderer(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.0F;
      }

      public boolean shouldRender(EntityCustom entity, ICamera camera, double camX, double camY, double camZ) {
         return true;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         GlStateManager.translate(x, y, z);
         GlStateManager.rotate(-180.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.enableBlend();
         GlStateManager.disableCull();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.disableLighting();
         float alpha = MathHelper.clamp((float)entity.ticksExisted / 56.0F, 0.0F, 1.0F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }
}
