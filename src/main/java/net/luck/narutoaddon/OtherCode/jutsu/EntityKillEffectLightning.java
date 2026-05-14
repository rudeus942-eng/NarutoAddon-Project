
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.entity.EntityLightningArc;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKillEffectLightning extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 325;

   public EntityKillEffectLightning(ElementsInfTsukAddon instance) {
      super(instance, 943);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "kill_effect_lightning"), 325).name("inftsuk_kill_effect_lightning").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, InvisibleRenderer::new);
   }

   public static class EntityCustom extends Entity {
      private int lifetime;
      private static final int MAX_LIFETIME = 45;
      private static final int ARC_COLOR = -1073741569;

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

               if (this.lifetime == 10) {
                  this.spawnPhase3();
               }

               if (this.lifetime > 14 && this.lifetime <= 35 && this.lifetime % 3 == 0) {
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
         int arcCount = 2 + this.lifetime;

         for(int i = 0; i < arcCount; ++i) {
            double a1 = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double d1 = this.rand.nextDouble() * ringRadius;
            double a2 = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double d2 = this.rand.nextDouble() * ringRadius;
            Vec3d from = new Vec3d(this.posX + Math.cos(a1) * d1, this.posY + 0.1 + this.rand.nextDouble() * 0.3, this.posZ + Math.sin(a1) * d1);
            Vec3d to = new Vec3d(this.posX + Math.cos(a2) * d2, this.posY + 0.1 + this.rand.nextDouble() * 0.3, this.posZ + Math.sin(a2) * d2);
            EntityLightningArc.Base arc = new EntityLightningArc.Base(this.world, from, to, -1073741569, 3, 0.08F);
            this.world.spawnEntity(arc);
         }

      }

      private void spawnPhase2() {
         float progress = (float)(this.lifetime - 5) / 5.0F;
         double ringRadius = (double)3.0F;
         int count = 4 + (int)(progress * 4.0F);

         for(int i = 0; i < count; ++i) {
            double angle = (double)i / (double)count * Math.PI * (double)2.0F + this.rand.nextDouble() * 0.3;
            double px = this.posX + Math.cos(angle) * ringRadius;
            double pz = this.posZ + Math.sin(angle) * ringRadius;
            double height = (double)2.0F + (double)progress * (double)4.0F + this.rand.nextDouble() * (double)2.0F;
            Vec3d from = new Vec3d(px, this.posY + 0.2, pz);
            Vec3d to = new Vec3d(px + (this.rand.nextDouble() - (double)0.5F) * 0.8, this.posY + height, pz + (this.rand.nextDouble() - (double)0.5F) * 0.8);
            EntityLightningArc.Base arc = new EntityLightningArc.Base(this.world, from, to, -1073741569, 4, 0.12F);
            this.world.spawnEntity(arc);
         }

      }

      private void spawnPhase3() {
         EntityLightningArc.Base centerBolt = new EntityLightningArc.Base(this.world, new Vec3d(this.posX, this.posY, this.posZ), new Vec3d(this.posX, this.posY + (double)35.0F, this.posZ), -1073741569, 10, 0.4F, 0.35F);
         this.world.spawnEntity(centerBolt);
         EntityLightningBolt bolt = new EntityLightningBolt(this.world, this.posX, this.posY, this.posZ, true);
         this.world.addWeatherEffect(bolt);

         for(int i = 0; i < 4; ++i) {
            double angle = (double)i / (double)4.0F * Math.PI * (double)2.0F + this.rand.nextDouble() * 0.4;
            double lean = 0.3 + this.rand.nextDouble() * 0.3;
            double height = (double)12.0F + this.rand.nextDouble() * (double)18.0F;
            EntityLightningArc.Base arc = new EntityLightningArc.Base(this.world, new Vec3d(this.posX, this.posY, this.posZ), new Vec3d(this.posX + Math.cos(angle) * lean * height, this.posY + height, this.posZ + Math.sin(angle) * lean * height), -1073741569, 8, 0.3F, 0.2F);
            this.world.spawnEntity(arc);
         }

         for(int i = 0; i < 6; ++i) {
            double angle = (double)i / (double)6.0F * Math.PI * (double)2.0F + this.rand.nextDouble() * 0.3;
            double arcLen = (double)2.0F + this.rand.nextDouble() * (double)2.5F;
            EntityLightningArc.Base arc = new EntityLightningArc.Base(this.world, new Vec3d(this.posX, this.posY + 0.2, this.posZ), new Vec3d(this.posX + Math.cos(angle) * arcLen, this.posY + 0.2 + this.rand.nextDouble() * 0.4, this.posZ + Math.sin(angle) * arcLen), -1073741569, 6, 0.15F, 0.1F);
            this.world.spawnEntity(arc);
         }

      }

      private void spawnPhase4() {
         int arcCount = 2 + this.rand.nextInt(3);

         for(int i = 0; i < arcCount; ++i) {
            double a1 = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double d1 = this.rand.nextDouble() * (double)3.5F;
            double a2 = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double d2 = this.rand.nextDouble() * (double)3.5F;
            Vec3d from = new Vec3d(this.posX + Math.cos(a1) * d1, this.posY + 0.2 + this.rand.nextDouble() * (double)2.0F, this.posZ + Math.sin(a1) * d1);
            Vec3d to = new Vec3d(this.posX + Math.cos(a2) * d2, this.posY + 0.2 + this.rand.nextDouble() * (double)2.0F, this.posZ + Math.sin(a2) * d2);
            EntityLightningArc.Base arc = new EntityLightningArc.Base(this.world, from, to, -1073741569, 3, 0.08F);
            this.world.spawnEntity(arc);
         }

      }

      private void spawnPhase5() {
         for(int i = 0; i < 8; ++i) {
            double angle = (double)i / (double)8.0F * Math.PI * (double)2.0F;
            double dist = (double)3.0F + this.rand.nextDouble() * (double)2.0F;
            EntityLightningArc.Base arc = new EntityLightningArc.Base(this.world, new Vec3d(this.posX, this.posY + (double)0.5F, this.posZ), new Vec3d(this.posX + Math.cos(angle) * dist, this.posY + 0.3 + this.rand.nextDouble() * (double)1.5F, this.posZ + Math.sin(angle) * dist), -1073741569, 5, 0.12F, 0.15F);
            this.world.spawnEntity(arc);
         }

         EntityLightningArc.Base finalBolt = new EntityLightningArc.Base(this.world, new Vec3d(this.posX, this.posY, this.posZ), new Vec3d(this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F, this.posY + (double)15.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F), -1073741569, 4, 0.2F, 0.25F);
         this.world.spawnEntity(finalBolt);
      }

      private void spawnSounds() {
         if (this.lifetime == 1) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_IMPACT, SoundCategory.PLAYERS, 0.9F, 1.4F);
         }

         if (this.lifetime == 5) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_FIREWORK_LARGE_BLAST, SoundCategory.PLAYERS, 1.0F, 1.6F);
         }

         if (this.lifetime == 10) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 2.5F, 0.6F);
         }

         if (this.lifetime == 20) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 1.2F, 1.2F);
         }

         if (this.lifetime == 35) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 0.7F);
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
