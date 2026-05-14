
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Particles;
import net.narutomod.Particles.Types;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemJutsu.JutsuEnum.Type;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityWaterFormationWall extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 278;

   public EntityWaterFormationWall(ElementsInfTsukAddon instance) {
      super(instance, 910);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "water_formation_wall"), 278).name("inftsuk_water_formation_wall").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, InvisibleRenderer::new);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final int MAX_LIFETIME = 100;
      private static final double WALL_WIDTH = (double)7.0F;
      private static final double WALL_HEIGHT = (double)4.5F;
      private int lifetime;
      private float power;
      private EntityLivingBase caster;
      private float wallFacingAngle;

      public EntityCustom(World world) {
         super(world);
         this.lifetime = 0;
         this.power = 1.0F;
         this.wallFacingAngle = 0.0F;
         this.setSize(7.0F, 4.5F);
         this.noClip = true;
         this.setInvisible(true);
      }

      public EntityCustom(World world, EntityLivingBase caster, double x, double y, double z, float power, float facingAngle) {
         this(world);
         this.caster = caster;
         this.power = power;
         this.wallFacingAngle = facingAngle;
         this.setPosition(x, y, z);
      }

      public Type getJutsuType() {
         return Type.SUITON;
      }

      protected void entityInit() {
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 100 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               if (this.lifetime > 15) {
                  this.interceptProjectiles();
               }

               double riseProgress = Math.min((double)this.lifetime / (double)20.0F, (double)1.0F);
               double currentHeight = (double)4.5F * riseProgress;
               this.spawnWallBody(currentHeight);
               if (this.lifetime == 1) {
                  SoundEvent waterSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:waterstream"));
                  if (waterSound != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, waterSound, SoundCategory.HOSTILE, 2.0F, 0.5F);
                  } else {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 2.0F, 0.5F);
                  }
               }

               if (this.lifetime % 15 == 0 && this.lifetime > 1) {
                  SoundEvent waterLoop = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:waterstream"));
                  if (waterLoop != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, waterLoop, SoundCategory.HOSTILE, 0.7F, 0.6F + this.rand.nextFloat() * 0.2F);
                  }
               }
            }

         } else {
            if (!this.world.isRemote && this.lifetime > 100) {
               this.spawnDissolveBurst();
            }

            this.setDead();
         }
      }

      private void interceptProjectiles() {
         double halfWidth = (double)3.5F;
         double thickness = (double)1.5F;
         double radians = Math.toRadians((double)this.wallFacingAngle);
         double cosA = Math.cos(radians);
         double sinA = Math.sin(radians);
         double minX = this.posX - Math.abs(cosA * halfWidth) - Math.abs(sinA * thickness);
         double maxX = this.posX + Math.abs(cosA * halfWidth) + Math.abs(sinA * thickness);
         double minZ = this.posZ - Math.abs(sinA * halfWidth) - Math.abs(cosA * thickness);
         double maxZ = this.posZ + Math.abs(sinA * halfWidth) + Math.abs(cosA * thickness);
         AxisAlignedBB wallBB = new AxisAlignedBB(minX, this.posY, minZ, maxX, this.posY + (double)4.5F, maxZ);

         for(Entity proj : this.world.getEntitiesWithinAABB(Entity.class, wallBB, (e) -> e != this && e != this.caster && !e.isDead && this.isProjectileEntity(e))) {
            proj.setDead();
            String className = proj.getClass().getSimpleName();
            if (!className.contains("Fire") && !className.contains("Katon") && !className.contains("Flame") && !className.contains("Fireball")) {
               Particles.spawnParticle(this.world, Types.WATER_SPLASH, proj.posX, proj.posY, proj.posZ, 8, 0.3, 0.3, 0.3, 0.05, 0.05, 0.05, new int[]{12});
            } else {
               Particles.spawnParticle(this.world, Types.SMOKE, proj.posX, proj.posY, proj.posZ, 10, (double)0.5F, (double)0.5F, (double)0.5F, 0.05, 0.1, 0.05, new int[]{-857870593, 20});
               this.world.playSound((EntityPlayer)null, proj.posX, proj.posY, proj.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, 1.0F, 0.8F);
            }
         }

      }

      private boolean isProjectileEntity(Entity entity) {
         if (entity instanceof EntityThrowable) {
            return true;
         } else if (entity instanceof EntityArrow) {
            return true;
         } else if (entity instanceof EntityFireball) {
            return true;
         } else if (entity instanceof ItemJutsu.IJutsu) {
            return true;
         } else {
            String className = entity.getClass().getName();
            if (className.contains("narutomod.entity.Entity")) {
               return true;
            } else {
               return className.contains("inftsukaddon.jutsu.Entity");
            }
         }
      }

      private void spawnWallBody(double currentHeight) {
         if (this.lifetime % 2 == 0) {
            double radians = Math.toRadians((double)this.wallFacingAngle);
            double cosA = Math.cos(radians);
            double sinA = Math.sin(radians);
            double normalX = -sinA;
            double normalZ = cosA;
            int coreCount = (int)((double)150.0F * (currentHeight / (double)4.5F));

            for(int i = 0; i < coreCount; ++i) {
               double along = (this.rand.nextDouble() - (double)0.5F) * (double)7.0F;
               double up = this.rand.nextDouble() * currentHeight;
               double px = this.posX + cosA * along;
               double py = this.posY + up;
               double pz = this.posZ + sinA * along;
               Particles.spawnParticle(this.world, Types.WATER_SPLASH, px, py, pz, 1, 0.04, 0.03, 0.04, (double)0.0F, -0.02, (double)0.0F, new int[]{10});
            }

            int streamCount = (int)((double)60.0F * (currentHeight / (double)4.5F));

            for(int i = 0; i < streamCount; ++i) {
               double along = (this.rand.nextDouble() - (double)0.5F) * (double)7.0F;
               double px = this.posX + cosA * along;
               double pz = this.posZ + sinA * along;
               Particles.spawnParticle(this.world, Types.WATER_SPLASH, px, this.posY + currentHeight * (0.3 + this.rand.nextDouble() * 0.7), pz, 1, 0.05, 0.02, 0.05, (double)0.0F, -0.15, (double)0.0F, new int[]{8});
            }

            int mistCount = (int)((double)45.0F * (currentHeight / (double)4.5F));

            for(int i = 0; i < mistCount; ++i) {
               double along = (this.rand.nextDouble() - (double)0.5F) * (double)7.0F;
               double up = this.rand.nextDouble() * currentHeight;
               double px = this.posX + cosA * along;
               double py = this.posY + up;
               double pz = this.posZ + sinA * along;
               double dir = this.rand.nextBoolean() ? (double)1.0F : (double)-1.0F;
               Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, 0.12, 0.06, 0.12, normalX * 0.05 * dir, -0.04, normalZ * 0.05 * dir, new int[]{-2013239621, 10});
            }

            if (this.lifetime < 25 && currentHeight > (double)0.5F) {
               for(int i = 0; i < 8; ++i) {
                  double along = (this.rand.nextDouble() - (double)0.5F) * (double)7.0F;
                  double px = this.posX + cosA * along;
                  double pz = this.posZ + sinA * along;
                  double dir = this.rand.nextBoolean() ? (double)1.0F : (double)-1.0F;
                  Particles.spawnParticle(this.world, Types.WATER_SPLASH, px, this.posY + currentHeight, pz, 1, 0.1, 0.02, 0.1, normalX * 0.06 * dir, 0.08, normalZ * 0.06 * dir, new int[]{8});
               }
            }

         }
      }

      private void spawnDissolveBurst() {
         double radians = Math.toRadians((double)this.wallFacingAngle);
         double cosA = Math.cos(radians);
         double sinA = Math.sin(radians);
         double normalX = -sinA;
         double normalZ = cosA;

         for(int i = 0; i < 30; ++i) {
            double along = (this.rand.nextDouble() - (double)0.5F) * (double)7.0F;
            double up = this.rand.nextDouble() * (double)4.5F;
            double px = this.posX + cosA * along;
            double py = this.posY + up;
            double pz = this.posZ + sinA * along;
            double dir = this.rand.nextBoolean() ? (double)1.0F : (double)-1.0F;
            Particles.spawnParticle(this.world, Types.WATER_SPLASH, px, py, pz, 1, 0.2, 0.1, 0.2, normalX * 0.12 * dir, -0.15, normalZ * 0.12 * dir, new int[]{12});
         }

         Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX, this.posY + 0.1, this.posZ, 20, 2.8000000000000003, 0.05, 2.8000000000000003, 0.08, (double)0.0F, 0.08, new int[]{10});
         SoundEvent waterSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:waterstream"));
         if (waterSound != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, waterSound, SoundCategory.HOSTILE, 1.0F, 0.8F);
         }

      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.lifetime = compound.getInteger("lifetime");
         this.power = compound.getFloat("wfwPower");
         this.wallFacingAngle = compound.getFloat("wfwFacing");
      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("wfwPower", this.power);
         compound.setFloat("wfwFacing", this.wallFacingAngle);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (power < 0.4F) {
            return false;
         } else if (entity.world.isRemote) {
            return false;
         } else {
            Vec3d look = entity.getLookVec();
            double spawnX = entity.posX + look.x * (double)2.0F;
            double spawnY = entity.posY;
            double spawnZ = entity.posZ + look.z * (double)2.0F;
            float yaw = entity.rotationYaw;
            EntityCustom wall = new EntityCustom(entity.world, entity, spawnX, spawnY, spawnZ, power, yaw);
            entity.world.spawnEntity(wall);
            SoundEvent waterSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:waterstream"));
            if (waterSound != null) {
               entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, waterSound, SoundCategory.PLAYERS, 1.8F, 0.5F);
            } else {
               entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.PLAYERS, 1.5F, 0.5F);
            }

            return true;
         }
      }

      public float getBasePower() {
         return 0.4F;
      }

      public float getPowerupDelay() {
         return 15.0F;
      }

      public float getMaxPower() {
         return 6.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote) {
            Particles.spawnParticle(player.world, Types.SMOKE, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.8, player.posY + 0.1, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.8, 2, 0.15, 0.05, 0.15, (double)0.0F, 0.02, (double)0.0F, new int[]{-16764007, 12});
            if (player.ticksExisted % 3 == 0) {
               Particles.spawnParticle(player.world, Types.SMOKE, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * (double)0.5F, player.posY + 0.15, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * (double)0.5F, 1, 0.1, 0.1, 0.1, (double)0.0F, -0.02, (double)0.0F, new int[]{-16755286, 10});
            }
         }

         super.onUsingTick(stack, player, power);
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
