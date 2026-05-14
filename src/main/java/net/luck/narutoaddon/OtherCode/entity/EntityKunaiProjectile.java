
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKunaiProjectile extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 207;

   public EntityKunaiProjectile(ElementsInfTsukAddon instance) {
      super(instance, 38);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kunai_projectile"), 207).name("inftsuk_kunai_proj").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, KunaiRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable {
      private int lifetime = 0;
      private float damage = 5.0F;
      private float trueDamage = 0.0F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.25F, 0.25F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(0.25F, 0.25F);
      }

      public EntityCustom(World world, EntityLivingBase thrower, float normalDmg, float trueDmg) {
         super(world, thrower);
         this.setSize(0.25F, 0.25F);
         this.damage = normalDmg;
         this.trueDamage = trueDmg;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.thrower) {
               result.entityHit.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.thrower), this.damage);
               if (this.trueDamage > 0.0F) {
                  result.entityHit.hurtResistantTime = 0;
                  result.entityHit.attackEntityFrom(DamageSource.MAGIC, this.trueDamage);
               }
            }

            this.setDead();
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime > 100) {
            this.setDead();
         }

      }

      protected float getGravityVelocity() {
         return 0.03F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setFloat("kunaiDmg", this.damage);
         compound.setFloat("kunaiTrue", this.trueDamage);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("kunaiDmg")) {
            this.damage = compound.getFloat("kunaiDmg");
         }

         if (compound.hasKey("kunaiTrue")) {
            this.trueDamage = compound.getFloat("kunaiTrue");
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public static class KunaiRenderer extends Render<EntityCustom> {
      private ItemStack kunaiStack = null;

      public KunaiRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      private ItemStack getKunaiStack() {
         if (this.kunaiStack == null || this.kunaiStack.isEmpty()) {
            Item kunaiItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("narutomod", "kunai"));
            if (kunaiItem != null) {
               this.kunaiStack = new ItemStack(kunaiItem);
            }
         }

         return this.kunaiStack;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         ItemStack stack = this.getKunaiStack();
         if (stack != null && !stack.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(0.6F, 0.6F, 0.6F);
            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, TransformType.GROUND);
            GlStateManager.popMatrix();
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
         }
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TextureMap.LOCATION_BLOCKS_TEXTURE;
      }
   }
}
