
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
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
public class EntityWeightedBoulder extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 274;

   public EntityWeightedBoulder(ElementsInfTsukAddon instance) {
      super(instance, 905);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "weighted_boulder"), 274).name("inftsuk_weighted_boulder").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, InvisibleRenderer::new);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final double RADIUS = (double)7.0F;
      private static final int MAX_LIFETIME = 100;
      private int lifetime;
      private float power;
      private EntityLivingBase caster;

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
         this.power = power;
         this.setPosition(x, y, z);
      }

      public Type getJutsuType() {
         return Type.DOTON;
      }

      protected void entityInit() {
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 100 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               if (this.lifetime % 5 == 0) {
                  this.applyGravityEffects();
               }

               this.spawnZoneParticles();
               if (this.lifetime % 20 == 0) {
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.HOSTILE, 1.5F, 0.4F + this.rand.nextFloat() * 0.2F);
               }

               if (this.lifetime == 1) {
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + 0.3, this.posZ, 25, (double)2.5F, 0.3, (double)2.5F, 0.06, 0.12, 0.06, new int[]{-7838157, 35});

                  for(int i = 0; i < 16; ++i) {
                     double angle = (double)i / (double)16.0F * Math.PI * (double)2.0F;
                     double ringDist = 4.8999999999999995;
                     Particles.spawnParticle(this.world, Types.SMOKE, this.posX + Math.cos(angle) * ringDist, this.posY + 0.15, this.posZ + Math.sin(angle) * ringDist, 2, 0.15, 0.1, 0.15, Math.cos(angle) * 0.05, 0.02, Math.sin(angle) * 0.05, new int[]{-11189214, 28});
                  }

                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)0.5F, this.posZ, 12, (double)1.5F, 0.2, (double)1.5F, (double)0.0F, 0.2, (double)0.0F, new int[]{-5601195, 40});
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + 0.2, this.posZ, 15, (double)2.0F, (double)0.5F, (double)2.0F, 0.01, 0.08, 0.01, new int[]{-1439489519, 30});
                  SoundEvent rockSlam = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:rocks"));
                  if (rockSlam != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, rockSlam, SoundCategory.HOSTILE, 2.0F, 0.4F);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.6F, 0.3F);
               }
            }

         } else {
            this.setDead();
         }
      }

      private void applyGravityEffects() {
         List<EntityLivingBase> entities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)7.0F, this.posY - (double)1.0F, this.posZ - (double)7.0F, this.posX + (double)7.0F, this.posY + (double)4.0F, this.posZ + (double)7.0F), (e) -> e != this.caster && e.isEntityAlive() && ItemJutsu.canTarget(e));
         float totalDmg = 80.0F + this.power * 20.0F;

         for(EntityLivingBase target : entities) {
            double distSq = target.getDistanceSq(this.posX, this.posY, this.posZ);
            if (distSq <= (double)49.0F) {
               float cappedDmg = totalDmg;
               if (!(target instanceof EntityPlayer)) {
                  cappedDmg = Math.min(totalDmg, 100.0F);
               }

               target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.caster), cappedDmg);
               target.hurtResistantTime = 0;
               target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 2, false, false));
               target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 40, 1, false, false));
               target.motionY -= 0.15;
               target.velocityChanged = true;
               Particles.spawnParticle(this.world, Types.SMOKE, target.posX, target.posY + (double)0.5F, target.posZ, 4, 0.3, 0.2, 0.3, (double)0.0F, -0.05, (double)0.0F, new int[]{-863476173, 15});
            }
         }

      }

      private void spawnZoneParticles() {
         for(int i = 0; i < 6; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double dist = this.rand.nextDouble() * (double)7.0F;
            double px = this.posX + Math.cos(angle) * dist;
            double pz = this.posZ + Math.sin(angle) * dist;
            double py = this.posY + this.rand.nextDouble() * (double)0.5F;
            Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, 0.1, 0.2, 0.1, (double)0.0F, 0.08, (double)0.0F, new int[]{-7838157, 25});
         }

         for(int i = 0; i < 3; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double dist = this.rand.nextDouble() * (double)7.0F * 0.8;
            double px = this.posX + Math.cos(angle) * dist;
            double pz = this.posZ + Math.sin(angle) * dist;
            Particles.spawnParticle(this.world, Types.SMOKE, px, this.posY + 0.1, pz, 1, 0.2, 0.05, 0.2, (double)0.0F, 0.01, (double)0.0F, new int[]{-11189214, 15});
         }

         if (this.lifetime % 5 == 0) {
            for(int i = 0; i < 12; ++i) {
               double angle = (double)i / (double)12.0F * Math.PI * (double)2.0F;
               double px = this.posX + Math.cos(angle) * (double)7.0F;
               double pz = this.posZ + Math.sin(angle) * (double)7.0F;
               Particles.spawnParticle(this.world, Types.SMOKE, px, this.posY + 0.2, pz, 1, 0.05, 0.1, 0.05, (double)0.0F, 0.03, (double)0.0F, new int[]{-8952252, 20});
            }
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
         this.power = compound.getFloat("wbPower");
      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("wbPower", this.power);
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
               RayTraceResult rt = ProcedureUtils.raytraceBlocks(entity, (double)30.0F);
               double tx;
               double ty;
               double tz;
               if (rt != null && rt.typeOfHit == RayTraceResult.Type.BLOCK) {
                  tx = rt.hitVec.x;
                  ty = rt.hitVec.y;
                  tz = rt.hitVec.z;
               } else {
                  Vec3d look = entity.getLookVec();
                  tx = entity.posX + look.x * (double)30.0F;
                  ty = entity.posY + (double)entity.getEyeHeight() + look.y * (double)30.0F;
                  tz = entity.posZ + look.z * (double)30.0F;
               }

               EntityCustom zone = new EntityCustom(entity.world, entity, tx, ty, tz, power);
               entity.world.spawnEntity(zone);
               this.world_playEarthSound(entity);
               return true;
            }
         }
      }

      private void world_playEarthSound(EntityLivingBase entity) {
         SoundEvent rockSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:rocks"));
         if (rockSound != null) {
            entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, rockSound, SoundCategory.PLAYERS, 1.5F, 0.5F);
         } else {
            entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.8F, 0.5F);
         }

      }

      public float getBasePower() {
         return 0.5F;
      }

      public float getPowerupDelay() {
         return 35.0F;
      }

      public float getMaxPower() {
         return 6.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote) {
            Particles.spawnParticle(player.world, Types.SMOKE, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.8, player.posY + 0.1, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.8, 2, 0.15, 0.05, 0.15, (double)0.0F, 0.03, (double)0.0F, new int[]{-7838157, 15});
            if (player.ticksExisted % 3 == 0) {
               Particles.spawnParticle(player.world, Types.SMOKE, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * (double)0.5F, player.posY + 0.15, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * (double)0.5F, 1, 0.1, 0.1, 0.1, (double)0.0F, 0.05, (double)0.0F, new int[]{-11189214, 20});
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
