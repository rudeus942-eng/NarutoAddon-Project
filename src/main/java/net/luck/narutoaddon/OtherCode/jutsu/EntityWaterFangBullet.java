
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
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
import net.narutomod.procedure.ProcedureUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityWaterFangBullet extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 277;

   public EntityWaterFangBullet(ElementsInfTsukAddon instance) {
      super(instance, 910);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "water_fang_bullet"), 277).name("inftsuk_water_fang_bullet").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, InvisibleRenderer::new);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final double BASE_RADIUS = (double)1.5F;
      private static final int MAX_LIFETIME = 40;
      private static final int BASE_GEYSER_HEIGHT = 3;
      private int lifetime;
      private float power;
      private EntityLivingBase caster;
      private UUID shooterUUID;

      public EntityCustom(World world) {
         super(world);
         this.lifetime = 0;
         this.power = 1.0F;
         this.setSize(0.1F, 0.1F);
         this.noClip = true;
         this.setInvisible(true);
      }

      public EntityCustom(World world, EntityLivingBase caster, double x, double y, double z, float power) {
         this(world);
         this.caster = caster;
         this.shooterUUID = caster.getUniqueID();
         this.power = power;
         this.setPosition(x, y, z);
      }

      public Type getJutsuType() {
         return Type.SUITON;
      }

      protected void entityInit() {
      }

      private EntityLivingBase getShooter() {
         if (this.caster != null && this.caster.isEntityAlive()) {
            return this.caster;
         } else {
            if (this.shooterUUID != null && !this.world.isRemote) {
               for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)64.0F))) {
                  if (this.shooterUUID.equals(e.getUniqueID())) {
                     this.caster = e;
                     return this.caster;
                  }
               }
            }

            return null;
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 40 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               EntityLivingBase shooter = this.getShooter();
               if (this.lifetime % 3 == 0) {
                  this.applyGeyserDamage(shooter);
               }

               this.spawnGeyserParticles();
               if (this.lifetime == 1) {
                  SoundEvent waterSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:waterstream"));
                  if (waterSound != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, waterSound, SoundCategory.HOSTILE, 1.5F, 0.8F + this.rand.nextFloat() * 0.3F);
                  } else {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.HOSTILE, 1.5F, 0.6F + this.rand.nextFloat() * 0.2F);
                  }
               }
            }

         } else {
            this.setDead();
         }
      }

      private int getGeyserHeight() {
         return 3 + (int)((double)this.power * (double)0.625F);
      }

      private double getScaledRadius() {
         return (double)1.5F + (double)this.power * (double)0.25F;
      }

      private void applyGeyserDamage(EntityLivingBase shooter) {
         double progress = Math.min((double)this.lifetime / (double)30.0F, (double)1.0F);
         double currentHeight = (double)this.getGeyserHeight() * progress;
         double radius = this.getScaledRadius();
         List<EntityLivingBase> entities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - radius, this.posY, this.posZ - radius, this.posX + radius, this.posY + currentHeight, this.posZ + radius), (e) -> e != this.caster && e.isEntityAlive() && ItemJutsu.canTarget(e));
         float totalDmg = 80.0F + this.power * 20.0F;

         for(EntityLivingBase target : entities) {
            float cappedDmg = totalDmg;
            if (!(target instanceof EntityPlayer)) {
               cappedDmg = Math.min(totalDmg, 100.0F);
            }

            target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, shooter), cappedDmg);
            target.hurtResistantTime = 0;
            target.motionY += 0.8;
            target.velocityChanged = true;
         }

      }

      private void spawnGeyserParticles() {
         double progress = Math.min((double)this.lifetime / (double)30.0F, (double)1.0F);
         double currentHeight = (double)this.getGeyserHeight() * progress;
         double scaledRadius = this.getScaledRadius();
         Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX, this.posY + currentHeight * 0.3, this.posZ, (int)(30.0F + this.power * 5.0F), scaledRadius * 0.15, currentHeight * (double)0.5F, scaledRadius * 0.15, (double)0.0F, 0.4, (double)0.0F, new int[]{15});
         Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX, this.posY + currentHeight * (double)0.5F, this.posZ, (int)(20.0F + this.power * 3.0F), scaledRadius * 0.15, currentHeight * 0.4, scaledRadius * 0.15, (double)0.0F, 0.4, (double)0.0F, new int[]{18});
         if (this.lifetime % 2 == 0) {
            int ringCount = 12 + (int)(this.power * 2.0F);

            for(int i = 0; i < ringCount; ++i) {
               double angle = (double)i / (double)ringCount * Math.PI * (double)2.0F;
               double ringDist = scaledRadius * ((double)0.5F + progress * 0.9);
               Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX + Math.cos(angle) * ringDist, this.posY + currentHeight, this.posZ + Math.sin(angle) * ringDist, 1, 0.15, 0.1, 0.15, Math.cos(angle) * 0.22, 0.1, Math.sin(angle) * 0.22, new int[]{12});
            }
         }

         Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX, this.posY + currentHeight * 0.9, this.posZ, (int)(12.0F + this.power * 3.0F), scaledRadius * 0.3, 0.2, scaledRadius * 0.3, 0.18, 0.15, 0.18, new int[]{12});
         Particles.spawnParticle(this.world, Types.WATER_SPLASH, this.posX, this.posY + 0.1, this.posZ, (int)(14.0F + this.power * 2.0F), scaledRadius * 1.2, 0.02, scaledRadius * 1.2, 0.08, -0.01, 0.08, new int[]{10});
      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.lifetime = compound.getInteger("lifetime");
         this.power = compound.getFloat("wfbPower");
         if (compound.hasUniqueId("shooterUUID")) {
            this.shooterUUID = compound.getUniqueId("shooterUUID");
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("wfbPower", this.power);
         if (this.shooterUUID != null) {
            compound.setUniqueId("shooterUUID", this.shooterUUID);
         }

      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (power < 0.5F) {
            return false;
         } else if (entity.world.isRemote) {
            return false;
         } else {
            long now = entity.world.getTotalWorldTime();
            UUID uid = entity.getUniqueID();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 60L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               RayTraceResult rt = ProcedureUtils.raytraceBlocks(entity, (double)25.0F);
               double tx;
               double ty;
               double tz;
               if (rt != null && rt.typeOfHit == RayTraceResult.Type.BLOCK) {
                  tx = rt.hitVec.x;
                  ty = rt.hitVec.y;
                  tz = rt.hitVec.z;
               } else {
                  Vec3d look = entity.getLookVec();
                  tx = entity.posX + look.x * (double)25.0F;
                  ty = entity.posY + (double)entity.getEyeHeight() + look.y * (double)25.0F;
                  tz = entity.posZ + look.z * (double)25.0F;
               }

               EntityCustom geyser = new EntityCustom(entity.world, entity, tx, ty, tz, power);
               entity.world.spawnEntity(geyser);
               this.playWaterSound(entity);
               return true;
            }
         }
      }

      private void playWaterSound(EntityLivingBase entity) {
         SoundEvent waterSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:waterstream"));
         if (waterSound != null) {
            entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, waterSound, SoundCategory.PLAYERS, 1.2F, 0.7F);
         } else {
            entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.PLAYERS, 1.0F, 0.7F);
         }

      }

      public float getBasePower() {
         return 0.5F;
      }

      public float getPowerupDelay() {
         return 20.0F;
      }

      public float getMaxPower() {
         return 8.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote) {
            Particles.spawnParticle(player.world, Types.FLAME, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.8, player.posY + 0.1, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.8, 2, 0.15, 0.05, 0.15, (double)0.0F, 0.03, (double)0.0F, new int[]{-12285731, 15});
            if (player.ticksExisted % 3 == 0) {
               Particles.spawnParticle(player.world, Types.SMOKE, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * (double)0.5F, player.posY + 0.15, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * (double)0.5F, 1, 0.1, 0.1, 0.1, (double)0.0F, 0.04, (double)0.0F, new int[]{-865691137, 18});
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
