
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityDeepForest extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 12;

   public EntityDeepForest(ElementsInfTsukAddon instance) {
      super(instance, 29);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "deep_forest"), 12).name("deep_forest").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, RenderWoodPillar::new);
   }

   @SideOnly(Side.CLIENT)
   public static class RenderWoodPillar extends Render<EntityCustom> {
      private final ResourceLocation texture = new ResourceLocation("minecraft:textures/blocks/log_oak.png");
      private final ModelWoodPillar model = new ModelWoodPillar();

      public RenderWoodPillar(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.5F;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         GlStateManager.disableCull();
         this.bindEntityTexture(entity);
         GlStateManager.translate(x, y, z);
         GlStateManager.rotate(-entity.rotationYaw, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(entity.rotationPitch, 1.0F, 0.0F, 0.0F);
         float growthProgress = entity.getGrowthProgress();
         float baseScale = entity.getPillarScale();
         GlStateManager.scale(baseScale, baseScale * growthProgress, baseScale);
         GlStateManager.translate((double)0.0F, (double)-0.5F, (double)0.0F);
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.enableCull();
         GlStateManager.popMatrix();
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return this.texture;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelWoodPillar extends ModelBase {
      private final ModelRenderer pillar;
      private final ModelRenderer branch1;
      private final ModelRenderer branch2;

      public ModelWoodPillar() {
         this.textureWidth = 16;
         this.textureHeight = 16;
         this.pillar = new ModelRenderer(this);
         this.pillar.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.pillar.cubeList.add(new ModelBox(this.pillar, 0, 0, -2.0F, -16.0F, -2.0F, 4, 16, 4, 0.0F, false));
         this.branch1 = new ModelRenderer(this);
         this.branch1.setRotationPoint(0.0F, -8.0F, 0.0F);
         this.branch1.rotateAngleX = 0.4F;
         this.branch1.rotateAngleZ = 0.6F;
         this.branch1.cubeList.add(new ModelBox(this.branch1, 0, 0, -1.0F, -6.0F, -1.0F, 2, 6, 2, 0.0F, false));
         this.pillar.addChild(this.branch1);
         this.branch2 = new ModelRenderer(this);
         this.branch2.setRotationPoint(0.0F, -12.0F, 0.0F);
         this.branch2.rotateAngleX = -0.3F;
         this.branch2.rotateAngleZ = -0.5F;
         this.branch2.cubeList.add(new ModelBox(this.branch2, 0, 0, -1.0F, -5.0F, -1.0F, 2, 5, 2, 0.0F, false));
         this.pillar.addChild(this.branch2);
      }

      public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         this.pillar.render(scale);
      }
   }

   public static class EntityCustom extends Entity {
      private static final DataParameter<Float> SCALE;
      private static final DataParameter<Float> GROWTH;
      private static final DataParameter<Integer> MAX_LIFE;
      private EntityLivingBase caster;
      private float targetGrowth = 1.0F;
      private int growthTicks = 20;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.5F, 2.0F);
         this.noClip = true;
      }

      protected void entityInit() {
         this.dataManager.register(SCALE, 1.0F);
         this.dataManager.register(GROWTH, 0.0F);
         this.dataManager.register(MAX_LIFE, 200);
      }

      public void setPillarScale(float scale) {
         this.dataManager.set(SCALE, scale);
         this.setSize(0.5F * scale, 2.0F * scale);
      }

      public float getPillarScale() {
         return (Float)this.dataManager.get(SCALE);
      }

      public void setGrowthProgress(float growth) {
         this.dataManager.set(GROWTH, MathHelper.clamp(growth, 0.0F, 1.0F));
      }

      public float getGrowthProgress() {
         return (Float)this.dataManager.get(GROWTH);
      }

      public void setMaxLifetime(int ticks) {
         this.dataManager.set(MAX_LIFE, ticks);
      }

      public int getMaxLifetime() {
         return (Integer)this.dataManager.get(MAX_LIFE);
      }

      public void setCaster(EntityLivingBase caster) {
         this.caster = caster;
      }

      public void onUpdate() {
         super.onUpdate();
         if (this.ticksExisted <= this.growthTicks) {
            float progress = (float)this.ticksExisted / (float)this.growthTicks;
            this.setGrowthProgress(progress * this.targetGrowth);
            if (this.world.isRemote && this.ticksExisted % 2 == 0) {
               for(int i = 0; i < 2; ++i) {
                  double px = this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F;
                  double py = this.posY + this.rand.nextDouble() * (double)this.height * (double)this.getGrowthProgress();
                  double pz = this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F;
                  this.world.spawnParticle(EnumParticleTypes.BLOCK_DUST, px, py, pz, (double)0.0F, 0.05, (double)0.0F, new int[]{Block.getStateId(Blocks.LOG.getDefaultState())});
               }
            }
         }

         if (!this.world.isRemote) {
            if (this.ticksExisted > this.getMaxLifetime()) {
               this.setDead();
               return;
            }

            if (this.ticksExisted < 5 && this.caster != null) {
               for(EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)0.5F))) {
                  if (entity != this.caster && !entity.isDead) {
                     entity.attackEntityFrom(DamageSource.MAGIC, 5.0F * this.getPillarScale());
                  }
               }
            }
         }

         if (this.world.isRemote && this.ticksExisted > this.growthTicks) {
            float sway = MathHelper.sin((float)(this.ticksExisted + this.getEntityId()) * 0.05F) * 2.0F;
            this.rotationPitch = sway;
         }

      }

      public AxisAlignedBB getCollisionBoundingBox() {
         return null;
      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.setPillarScale(compound.getFloat("Scale"));
         this.setMaxLifetime(compound.getInteger("MaxLife"));
      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setFloat("Scale", this.getPillarScale());
         compound.setInteger("MaxLife", this.getMaxLifetime());
      }

      public static void spawn(World world, @Nullable EntityLivingBase caster, BlockPos center, float diameter, int lifespan) {
         if (!world.isRemote) {
            System.out.println("[EntityDeepForest] spawn() called at " + center + " with diameter=" + diameter + ", lifespan=" + lifespan);
            int pillarCount = (int)(diameter * 0.8F);
            pillarCount = MathHelper.clamp(pillarCount, 8, 40);
            System.out.println("[EntityDeepForest] Spawning " + pillarCount + " wood pillars");
            int ringsCount = 3;
            int pillarsPerRing = pillarCount / ringsCount;

            for(int ring = 1; ring <= ringsCount; ++ring) {
               float ringRadius = diameter * 0.5F * ((float)ring / (float)ringsCount);

               for(int i = 0; i < pillarsPerRing; ++i) {
                  float baseAngle = (float)((Math.PI * 2D) * (double)i / (double)pillarsPerRing);
                  float angleVariation = (world.rand.nextFloat() - 0.5F) * 0.5F;
                  float angle = baseAngle + angleVariation;
                  float radiusVariation = (world.rand.nextFloat() - 0.5F) * 3.0F;
                  float finalRadius = ringRadius + radiusVariation;
                  double x = (double)center.getX() + (double)0.5F + (double)(MathHelper.cos(angle) * finalRadius);
                  double z = (double)center.getZ() + (double)0.5F + (double)(MathHelper.sin(angle) * finalRadius);

                  BlockPos groundPos;
                  for(groundPos = new BlockPos(x, (double)center.getY(), z); groundPos.getY() > 1 && world.isAirBlock(groundPos.down()); groundPos = groundPos.down()) {
                  }

                  while(groundPos.getY() < 256 && !world.isAirBlock(groundPos)) {
                     groundPos = groundPos.up();
                  }

                  EntityCustom pillar = new EntityCustom(world);
                  pillar.setPosition(x, (double)groundPos.getY(), z);
                  pillar.setCaster(caster);
                  pillar.setMaxLifetime(lifespan);
                  pillar.rotationYaw = world.rand.nextFloat() * 360.0F;
                  float baseScale = 0.6F + (float)(ringsCount - ring + 1) * 0.3F;
                  float scaleVariation = world.rand.nextFloat() * 0.4F;
                  pillar.setPillarScale(baseScale + scaleVariation);
                  world.spawnEntity(pillar);
               }
            }

            world.playSound((EntityPlayer)null, (double)center.getX(), (double)center.getY(), (double)center.getZ(), SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.HOSTILE, 2.0F, 0.5F + world.rand.nextFloat() * 0.3F);
            world.playSound((EntityPlayer)null, (double)center.getX(), (double)center.getY(), (double)center.getZ(), SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.HOSTILE, 1.5F, 0.6F);
            System.out.println("[EntityDeepForest] Forest spawned successfully");
         }
      }

      static {
         SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         GROWTH = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         MAX_LIFE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
      }
   }
}
