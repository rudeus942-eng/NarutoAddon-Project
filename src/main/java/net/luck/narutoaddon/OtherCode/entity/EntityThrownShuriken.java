
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.item.ItemGiantShuriken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityThrownShuriken extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 50;

   public EntityThrownShuriken(ElementsInfTsukAddon instance) {
      super(instance, 500);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "thrownshuriken"), 50).name("thrownshuriken").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, (renderManager) -> new RenderSnowball<EntityCustom>(renderManager, ItemGiantShuriken.block, Minecraft.getMinecraft().getRenderItem()) {
            public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
               GlStateManager.pushMatrix();
               GlStateManager.translate(x, y, z);
               GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
               GlStateManager.scale(2.0F, 2.0F, 2.0F);
               GlStateManager.translate(-x, -y, -z);
               super.doRender(entity, x, y, z, entityYaw, partialTicks);
               GlStateManager.popMatrix();
            }
         });
   }

   public static class EntityCustom extends EntityThrowable {
      private int ticksInAir = 0;
      private static final int MAX_LIFETIME = 200;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.5F, 0.5F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(0.5F, 0.5F);
      }

      public EntityCustom(World world, double x, double y, double z) {
         super(world, x, y, z);
         this.setSize(0.5F, 0.5F);
      }

      protected float getGravityVelocity() {
         return 0.0F;
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.ticksInAir;
         if (this.ticksInAir > 200 && !this.world.isRemote) {
            this.setDead();
         }

         if (this.world.isRemote && this.ticksInAir % 2 == 0) {
            this.world.spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY, this.posZ, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
         }

      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null) {
               if (result.entityHit == this.thrower) {
                  return;
               }

               DamageSource trueDamage = (new DamageSource("thrownShuriken")).setDamageBypassesArmor().setDamageIsAbsolute().setProjectile();
               if (result.entityHit instanceof EntityLivingBase) {
                  EntityLivingBase target = (EntityLivingBase)result.entityHit;
                  target.hurtResistantTime = 0;
                  target.setAbsorptionAmount(0.0F);
               }

               result.entityHit.attackEntityFrom(trueDamage, 5.0F);
               this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 1.2F);
            }

            if (!this.world.isRemote) {
               if (this.world instanceof WorldServer) {
                  ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY, this.posZ, 10, 0.2, 0.2, 0.2, 0.1, new int[0]);
               }

               this.setDead();
            }

         }
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("ticksInAir", this.ticksInAir);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.ticksInAir = compound.getInteger("ticksInAir");
      }
   }
}
