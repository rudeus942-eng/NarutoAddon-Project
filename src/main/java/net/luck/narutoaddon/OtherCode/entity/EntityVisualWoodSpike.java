
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityVisualWoodSpike extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 11;

   public EntityVisualWoodSpike(ElementsInfTsukAddon instance) {
      super(instance, 30);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "visual_wood_spike"), 11).name("visual_wood_spike").tracker(64, 5, false).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, SpikeRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class SpikeRenderer extends Render<EntityCustom> {
      public SpikeRenderer(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.5F;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
         IBlockState logState = Blocks.LOG.getDefaultState();
         IBlockState fenceState = Blocks.OAK_FENCE.getDefaultState();
         this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)x - 0.5F, (float)y, (float)z - 0.5F);
         int height = entity.getSpikeHeight();

         for(int i = 0; i < height; ++i) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, (float)i, 0.0F);
            blockRenderer.renderBlockBrightness(logState, 1.0F);
            GlStateManager.popMatrix();
         }

         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, (float)height, 0.0F);
         blockRenderer.renderBlockBrightness(fenceState, 1.0F);
         GlStateManager.popMatrix();
         GlStateManager.popMatrix();
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TextureMap.LOCATION_BLOCKS_TEXTURE;
      }
   }

   public static class EntityCustom extends Entity {
      private static final DataParameter<Integer> SPIKE_HEIGHT;
      private static final DataParameter<Integer> LIFETIME;
      private float damage;
      private float lingeringDamage;
      private int maxLifetime;
      private Entity owner;

      public EntityCustom(World world) {
         super(world);
         this.damage = 20.0F;
         this.lingeringDamage = 5.0F;
         this.maxLifetime = 100;
         this.setSize(1.0F, 4.0F);
         this.noClip = true;
      }

      public EntityCustom(World world, double x, double y, double z, int height, float damage, int lifetime) {
         this(world);
         this.setPosition(x, y, z);
         this.damage = damage;
         this.maxLifetime = lifetime;
         this.dataManager.set(SPIKE_HEIGHT, height);
         this.dataManager.set(LIFETIME, lifetime);
         this.setSize(1.0F, (float)(height + 1));
      }

      public void setOwner(Entity owner) {
         this.owner = owner;
      }

      protected void entityInit() {
         this.dataManager.register(SPIKE_HEIGHT, 4);
         this.dataManager.register(LIFETIME, 100);
      }

      public int getSpikeHeight() {
         return (Integer)this.dataManager.get(SPIKE_HEIGHT);
      }

      public int getLifetime() {
         return (Integer)this.dataManager.get(LIFETIME);
      }

      public void onUpdate() {
         super.onUpdate();
         if (this.ticksExisted > this.maxLifetime) {
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               int height = this.getSpikeHeight();
               ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + (double)height * (double)0.5F, this.posZ, 15, 0.3, (double)height * 0.3, 0.3, 0.02, new int[0]);
               EnumParticleTypes var10001 = EnumParticleTypes.BLOCK_CRACK;
               double var10002 = this.posX;
               double var10003 = this.posY + (double)1.0F;
               double var10004 = this.posZ;
               int[] var10010 = new int[1];
               Block var10013 = Blocks.LOG;
               var10010[0] = Block.getStateId(Blocks.LOG.getDefaultState());
               ws.spawnParticle(var10001, var10002, var10003, var10004, 10, 0.3, (double)0.5F, 0.3, 0.05, var10010);
            }

            this.setDead();
         } else {
            if (!this.world.isRemote && this.ticksExisted % 20 == 0) {
               this.applyLingeringDamage();
            }

            if (this.world.isRemote && this.ticksExisted % 10 == 0) {
               int height = this.getSpikeHeight();
               this.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX + (this.rand.nextDouble() - (double)0.5F), this.posY + this.rand.nextDouble() * (double)height, this.posZ + (this.rand.nextDouble() - (double)0.5F), (double)0.0F, 0.02, (double)0.0F, new int[0]);
            }

         }
      }

      public void applyInitialDamage() {
         if (!this.world.isRemote) {
            int height = this.getSpikeHeight();
            AxisAlignedBB damageBox = new AxisAlignedBB(this.posX - (double)1.5F, this.posY, this.posZ - (double)1.5F, this.posX + (double)1.5F, this.posY + (double)height + (double)1.0F, this.posZ + (double)1.5F);

            for(EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, damageBox, (entityx) -> entityx != this.owner && !(entityx instanceof EntityBossWoodGolem.EntityCustom))) {
               entity.hurtResistantTime = 0;
               entity.attackEntityFrom(DamageSource.MAGIC, this.damage);
               entity.motionY += (double)0.5F;
               if (entity instanceof EntityPlayer) {
                  ((EntityPlayer)entity).velocityChanged = true;
               }
            }

         }
      }

      private void applyLingeringDamage() {
         int height = this.getSpikeHeight();
         AxisAlignedBB damageBox = new AxisAlignedBB(this.posX - (double)0.5F, this.posY, this.posZ - (double)0.5F, this.posX + (double)0.5F, this.posY + (double)height, this.posZ + (double)0.5F);

         for(EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, damageBox, (entityx) -> entityx != this.owner && !(entityx instanceof EntityBossWoodGolem.EntityCustom))) {
            entity.hurtResistantTime = 0;
            entity.attackEntityFrom(DamageSource.MAGIC, this.lingeringDamage);
         }

      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         if (compound.hasKey("spikeHeight")) {
            this.dataManager.set(SPIKE_HEIGHT, compound.getInteger("spikeHeight"));
         }

         if (compound.hasKey("lifetime")) {
            this.maxLifetime = compound.getInteger("lifetime");
         }

         if (compound.hasKey("damage")) {
            this.damage = compound.getFloat("damage");
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("spikeHeight", this.getSpikeHeight());
         compound.setInteger("lifetime", this.maxLifetime);
         compound.setFloat("damage", this.damage);
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

      public AxisAlignedBB getCollisionBoundingBox() {
         return null;
      }

      public AxisAlignedBB getCollisionBox(Entity entityIn) {
         return null;
      }

      static {
         SPIKE_HEIGHT = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         LIFETIME = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
      }
   }
}
