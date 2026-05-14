
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKillEffectShadow extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 335;

   public EntityKillEffectShadow(ElementsInfTsukAddon instance) {
      super(instance, 963);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kill_effect_shadow"), 335).name("inftsuk_kill_effect_shadow").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, InvisibleRenderer::new);
   }

   public static class EntityCustom extends Entity {
      private int lifetime;
      private static final int MAX_LIFETIME = 50;

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
         if (this.lifetime <= 50 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote && this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               if (this.lifetime <= 6) {
                  this.spawnPhase1(ws);
               }

               if (this.lifetime > 6 && this.lifetime <= 14) {
                  this.spawnPhase2(ws);
               }

               if (this.lifetime == 15) {
                  this.spawnPhase3(ws);
               }

               if (this.lifetime > 17 && this.lifetime <= 40 && this.lifetime % 2 == 0) {
                  this.spawnPhase4(ws);
               }

               if (this.lifetime == 42) {
                  this.spawnPhase5(ws);
               }

               this.spawnSounds();
            }

         } else {
            this.setDead();
         }
      }

      private void spawnPhase1(WorldServer ws) {
         float progress = (float)this.lifetime / 6.0F;
         double radius = (double)3.0F - (double)progress * (double)1.5F;
         int count = 12 + this.lifetime * 3;
         double baseAngle = (double)this.lifetime * 0.8;

         for(int i = 0; i < count; ++i) {
            double angle = baseAngle + (double)i / (double)count * Math.PI * (double)2.0F;
            double height = this.rand.nextDouble() * (double)2.0F;
            double x = this.posX + Math.cos(angle) * radius;
            double z = this.posZ + Math.sin(angle) * radius;
            double vx = -Math.sin(angle) * 0.35;
            double vz = Math.cos(angle) * 0.35;
            ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, this.posY + height, z, 1, vx, 0.02, vz, 0.01, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, this.posY + 0.1 + height, z, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
         }

      }

      private void spawnPhase2(WorldServer ws) {
         float progress = (float)(this.lifetime - 6) / 8.0F;
         double maxHeight = (double)12.0F;
         double currentHeight = (double)progress * maxHeight;
         int ringsPerTick = 3;

         for(int r = 0; r < ringsPerTick; ++r) {
            double h = this.rand.nextDouble() * currentHeight;
            double widen = 0.8 + h / maxHeight * 1.6;
            int perRing = 14;
            double spin = (double)this.lifetime * 1.2 + h * 0.4;

            for(int i = 0; i < perRing; ++i) {
               double angle = spin + (double)i / (double)perRing * Math.PI * (double)2.0F;
               double x = this.posX + Math.cos(angle) * widen;
               double z = this.posZ + Math.sin(angle) * widen;
               double vx = -Math.sin(angle) * 0.35;
               double vz = Math.cos(angle) * 0.35;
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, this.posY + h, z, 1, vx, 0.05, vz, (double)0.0F, new int[0]);
               if ((i & 1) == 0) {
                  ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, this.posY + h, z, 1, vx * (double)0.5F, 0.02, vz * (double)0.5F, (double)0.0F, new int[0]);
               }
            }
         }

      }

      private void spawnPhase3(WorldServer ws) {
         for(int i = 0; i < 60; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double r = this.rand.nextDouble() * 0.6;
            double vx = Math.cos(angle) * 0.3 + (this.rand.nextDouble() - (double)0.5F) * 0.2;
            double vz = Math.sin(angle) * 0.3 + (this.rand.nextDouble() - (double)0.5F) * 0.2;
            double vy = 0.8 + this.rand.nextDouble() * 0.6;
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX + Math.cos(angle) * r, this.posY + 0.2, this.posZ + Math.sin(angle) * r, 1, vx, vy, vz, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + Math.cos(angle) * r, this.posY + 0.2, this.posZ + Math.sin(angle) * r, 1, vx * (double)0.5F, vy * (double)0.5F, vz * (double)0.5F, (double)0.0F, new int[0]);
         }

         int ringCount = 48;

         for(int i = 0; i < ringCount; ++i) {
            double angle = (double)i / (double)ringCount * Math.PI * (double)2.0F;
            double vx = Math.cos(angle) * 0.9;
            double vz = Math.sin(angle) * 0.9;
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX + Math.cos(angle) * 0.4, this.posY + 0.3, this.posZ + Math.sin(angle) * 0.4, 1, vx, 0.05, vz, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + Math.cos(angle) * 0.4, this.posY + 0.3, this.posZ + Math.sin(angle) * 0.4, 1, vx * 0.6, 0.02, vz * 0.6, (double)0.0F, new int[0]);
         }

      }

      private void spawnPhase4(WorldServer ws) {
         int count = 8 + this.rand.nextInt(6);

         for(int i = 0; i < count; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double dist = (double)1.0F + this.rand.nextDouble() * (double)4.0F;
            double height = (double)3.0F + this.rand.nextDouble() * (double)6.0F;
            double x = this.posX + Math.cos(angle) * dist;
            double z = this.posZ + Math.sin(angle) * dist;
            double tangentVx = -Math.sin(angle) * 0.1;
            double tangentVz = Math.cos(angle) * 0.1;
            ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, this.posY + height, z, 1, tangentVx, -0.15, tangentVz, (double)0.0F, new int[0]);
            if (i % 3 == 0) {
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, this.posY + height, z, 1, tangentVx * (double)2.0F, -0.1, tangentVz * (double)2.0F, (double)0.0F, new int[0]);
            }
         }

      }

      private void spawnPhase5(WorldServer ws) {
         int ringCount = 64;

         for(int i = 0; i < ringCount; ++i) {
            double angle = (double)i / (double)ringCount * Math.PI * (double)2.0F;
            double vx = Math.cos(angle) * 1.2;
            double vz = Math.sin(angle) * 1.2;
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + 0.6, this.posZ, 1, vx, (double)0.0F, vz, (double)0.0F, new int[0]);
            ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.8, this.posZ, 1, vx * 0.6, (double)0.0F, vz * 0.6, (double)0.0F, new int[0]);
         }

         for(int i = 0; i < 20; ++i) {
            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + 0.4, this.posZ, 1, (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, 0.6 + this.rand.nextDouble() * 0.4, (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, (double)0.0F, new int[0]);
         }

      }

      private void spawnSounds() {
         if (this.lifetime == 1) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.PLAYERS, 0.9F, 1.4F);
         }

         if (this.lifetime == 7) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 1.0F, 0.6F);
         }

         if (this.lifetime == 15) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 1.0F, 1.0F);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_FIREWORK_LARGE_BLAST, SoundCategory.PLAYERS, 1.0F, 0.7F);
         }

         if (this.lifetime == 30) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.PLAYERS, 0.6F, 0.8F);
         }

         if (this.lifetime == 42) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 0.8F);
         }

      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.lifetime = compound.getInteger("kesLifetime");
      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("kesLifetime", this.lifetime);
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
