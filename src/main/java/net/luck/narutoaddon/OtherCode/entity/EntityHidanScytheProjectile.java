
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityHidanScytheProjectile extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 288;

   public EntityHidanScytheProjectile(ElementsInfTsukAddon instance) {
      super(instance, 952);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "hidan_scythe_projectile"), 288).name("inftsuk_hidan_scythe_proj").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, ScytheRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private int lifetime = 0;
      private float normalDmg = 10.0F;
      private float trueDmg = 3.0F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.5F, 0.5F);
      }

      public EntityCustom(World world, EntityLivingBase thrower, float normalDmg, float trueDmg) {
         super(world, thrower);
         this.setSize(0.5F, 0.5F);
         this.normalDmg = normalDmg;
         this.trueDmg = trueDmg;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit instanceof EntityLivingBase && result.entityHit != this.thrower) {
               EntityLivingBase target = (EntityLivingBase)result.entityHit;
               target.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.thrower), this.normalDmg);
               if (this.trueDmg > 0.0F) {
                  target.hurtResistantTime = 0;
                  target.attackEntityFrom((new DamageSource("jashinTrueDamage")).setDamageBypassesArmor(), this.trueDmg);
               }

               target.addPotionEffect(new PotionEffect(MobEffects.WITHER, 40, 0));
               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_IRONGOLEM_HURT, SoundCategory.HOSTILE, 1.0F, 1.2F);
            }

            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 120) {
            this.setDead();
         }

      }

      protected float getGravityVelocity() {
         return 0.01F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setFloat("scytheNormalDmg", this.normalDmg);
         compound.setFloat("scytheTrueDmg", this.trueDmg);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("scytheNormalDmg")) {
            this.normalDmg = compound.getFloat("scytheNormalDmg");
         }

         if (compound.hasKey("scytheTrueDmg")) {
            this.trueDmg = compound.getFloat("scytheTrueDmg");
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public static class ScytheRenderer extends Render<EntityCustom> {
      private final RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
      private Item scytheItem = null;
      private boolean itemLookedUp = false;

      public ScytheRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      private Item getScytheItem() {
         if (!this.itemLookedUp) {
            this.scytheItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("narutomod", "scythe_hidan"));
            this.itemLookedUp = true;
         }

         return this.scytheItem;
      }

      public boolean shouldRender(EntityCustom entity, ICamera camera, double camX, double camY, double camZ) {
         return true;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         Item item = this.getScytheItem();
         if (item != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)x, (float)y, (float)z);
            GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks) - 90.0F, 1.0F, 0.0F, 0.0F);
            float spin = ((float)entity.ticksExisted + partialTicks) * 40.0F;
            GlStateManager.rotate(spin, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(1.5F, 1.5F, 1.5F);
            GlStateManager.enableRescaleNormal();
            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            this.itemRenderer.renderItem(new ItemStack(item), TransformType.GROUND);
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
         }
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TextureMap.LOCATION_BLOCKS_TEXTURE;
      }
   }
}
