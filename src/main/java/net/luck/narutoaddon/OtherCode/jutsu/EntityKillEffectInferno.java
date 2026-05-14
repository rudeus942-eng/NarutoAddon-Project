
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Particles;
import net.narutomod.Particles.Types;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKillEffectInferno extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 312;

   public EntityKillEffectInferno(ElementsInfTsukAddon instance) {
      super(instance, 941);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kill_effect_inferno"), 312).name("inftsuk_kill_effect_inferno").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, InvisibleRenderer::new);
   }

   public static class EntityCustom extends Entity {
      private int lifetime;
      private static final int MAX_LIFETIME = 45;

      public EntityCustom(World world) {
         super(world);
         this.lifetime = 0;
         this.setSize(0.1F, 0.1F);
         this.noClip = true;
         this.setInvisible(true);
      }

      public EntityCustom(World world, double x, double y, double z) {
         this(world);
         this.setPosition(x, y, z);
      }

      protected void entityInit() {
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 45 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               if (this.lifetime <= 5) {
                  this.spawnPhase1();
               }

               if (this.lifetime > 5 && this.lifetime <= 10) {
                  this.spawnPhase2();
               }

               if (this.lifetime > 10 && this.lifetime <= 20) {
                  this.spawnPhase3();
               }

               if (this.lifetime > 20 && this.lifetime <= 35) {
                  this.spawnPhase4();
               }

               if (this.lifetime == 35) {
                  this.spawnPhase5();
               }

               this.spawnSounds();
            }

         } else {
            this.setDead();
         }
      }

      private void spawnPhase1() {
         float progress = (float)this.lifetime / 5.0F;
         double ringRadius = (double)0.5F + (double)progress * (double)2.5F;
         int count = 24;

         for(int i = 0; i < count; ++i) {
            double angle = (double)i / (double)count * Math.PI * (double)2.0F;
            double px = this.posX + Math.cos(angle) * ringRadius;
            double pz = this.posZ + Math.sin(angle) * ringRadius;
            double outX = Math.cos(angle) * 0.05 * (double)progress;
            double outZ = Math.sin(angle) * 0.05 * (double)progress;
            Particles.spawnParticle(this.world, Types.FLAME, px, this.posY + 0.1, pz, 1, 0.05, 0.02, 0.05, outX, 0.01, outZ, new int[]{-3399168, 5 + this.rand.nextInt(3)});
         }

      }

      private void spawnPhase2() {
         float progress = (float)(this.lifetime - 5) / 5.0F;
         double ringRadius = (double)3.0F;
         int count = 30;

         for(int i = 0; i < count; ++i) {
            double angle = (double)i / (double)count * Math.PI * (double)2.0F;
            double px = this.posX + Math.cos(angle) * ringRadius;
            double pz = this.posZ + Math.sin(angle) * ringRadius;
            Particles.spawnParticle(this.world, Types.FLAME, px, this.posY + 0.2 + (double)progress * (double)2.0F, pz, 1, 0.08, 0.15, 0.08, (double)0.0F, 0.15 + (double)progress * 0.2, (double)0.0F, new int[]{-3399168, 6 + this.rand.nextInt(3)});
            if (i % 3 == 0) {
               Particles.spawnParticle(this.world, Types.FLAME, px, this.posY + (double)0.5F + (double)progress * (double)2.5F, pz, 1, 0.06, 0.1, 0.06, (double)0.0F, 0.2 + (double)progress * 0.15, (double)0.0F, new int[]{-21965, 4 + this.rand.nextInt(2)});
            }
         }

         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)0.5F, this.posZ, 8, ringRadius * 0.8, 0.3, ringRadius * 0.8, (double)0.0F, 0.03, (double)0.0F, new int[]{-870182656, 12});
      }

      private void spawnPhase3() {
         float progress = (float)(this.lifetime - 10) / 10.0F;
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 30, 0.2, (double)0.5F, 0.2, (double)0.0F, 1.8, (double)0.0F, new int[]{-18, 3});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + 0.8, this.posZ, 45, (double)0.5F, 0.6, (double)0.5F, (double)0.0F, 1.4, (double)0.0F, new int[]{-39424, 6});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)0.5F, this.posZ, 35, 0.8, 0.7, 0.8, (double)0.0F, (double)1.0F, (double)0.0F, new int[]{-3394816, 10});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + 0.3, this.posZ, 25, 1.2, 0.8, 1.2, (double)0.0F, 0.6, (double)0.0F, new int[]{-5631744, 14});
         if (progress > 0.4F) {
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)8.0F, this.posZ, 40, (double)3.0F, (double)0.5F, (double)3.0F, 0.15, 0.04, 0.15, new int[]{-869068544, 18});
         }

      }

      private void spawnPhase4() {
         int emberCount = 3 + this.rand.nextInt(3);

         for(int i = 0; i < emberCount; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double dist = this.rand.nextDouble() * (double)3.0F;
            double px = this.posX + Math.cos(angle) * dist;
            double pz = this.posZ + Math.sin(angle) * dist;
            int color = i % 2 == 0 ? -39424 : -3394816;
            Particles.spawnParticle(this.world, Types.FLAME, px, this.posY + (double)5.0F + this.rand.nextDouble() * (double)4.0F, pz, 1, 0.05, 0.05, 0.05, (this.rand.nextDouble() - (double)0.5F) * 0.08, 0.1 + this.rand.nextDouble() * 0.15, (this.rand.nextDouble() - (double)0.5F) * 0.08, new int[]{color, 20 + this.rand.nextInt(10)});
         }

         if (this.lifetime % 3 == 0) {
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + 0.1, this.posZ, 5, (double)2.5F, 0.05, (double)2.5F, (double)0.0F, 0.001, (double)0.0F, new int[]{-870182656, 60 + this.rand.nextInt(20)});
         }

      }

      private void spawnPhase5() {
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 40, (double)0.5F, (double)0.5F, (double)0.5F, 0.3, 0.3, 0.3, new int[]{-52, 2});
      }

      private void spawnSounds() {
         if (this.lifetime == 1) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.8F, 1.5F);
         }

         if (this.lifetime == 5) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.2F, 0.8F);
         }

         if (this.lifetime == 10) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.5F, 0.5F);
         }

         if (this.lifetime == 20) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_FIREWORK_BLAST, SoundCategory.PLAYERS, 1.5F, 0.7F);
         }

         if (this.lifetime == 35) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.0F, 0.5F);
         }

      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.lifetime = compound.getInteger("keiLifetime");
      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("keiLifetime", this.lifetime);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class InvisibleRenderer extends Render<EntityCustom> {
      public InvisibleRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return null;
      }
   }
}
