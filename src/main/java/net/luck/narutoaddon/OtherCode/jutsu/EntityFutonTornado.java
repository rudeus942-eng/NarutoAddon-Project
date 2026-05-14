
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityFutonTornado extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 283;

   public EntityFutonTornado(ElementsInfTsukAddon instance) {
      super(instance, 913);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "futon_tornado"), 283).name("inftsuk_futon_tornado").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, InvisibleRenderer::new);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final double SUCTION_RADIUS = (double)7.0F;
      private static final double CORE_RADIUS = (double)4.0F;
      private static final int FORMATION_DELAY = 20;
      private static final int TORNADO_DURATION = 80;
      private static final int MAX_LIFETIME = 105;
      private static final int DAMAGE_INTERVAL = 10;
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
         return Type.FUTON;
      }

      protected void entityInit() {
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 105 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               if (this.lifetime <= 20) {
                  this.spawnFormationParticles();
                  if (this.lifetime == 1 || this.lifetime % 5 == 0) {
                     SoundEvent windSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
                     if (windSound != null) {
                        float pitch = 0.8F - (float)this.lifetime / 20.0F * 0.3F;
                        this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, windSound, SoundCategory.PLAYERS, 0.6F + (float)this.lifetime / 20.0F * 0.8F, pitch);
                     }
                  }

                  return;
               }

               int activeTick = this.lifetime - 20;
               this.applySuction();
               if (activeTick % 10 == 0) {
                  this.applyCoreDamage();
               }

               this.applyLift();
               this.spawnTornadoParticles(activeTick);
               if (activeTick % 2 == 0 && this.caster != null) {
                  double slashX = this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)4.0F;
                  double slashY = this.posY + this.rand.nextDouble() * (double)7.0F;
                  double slashZ = this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * (double)4.0F;
                  float roll = this.rand.nextFloat() * 360.0F;
                  EntityWindSlash.EntityCustom slash = new EntityWindSlash.EntityCustom(this.world, this.caster, this.power * 1.0F, roll);
                  slash.renderScale = 5.0F;
                  slash.setPosition(slashX, slashY, slashZ);
                  slash.rotationYaw = this.rand.nextFloat() * 360.0F;
                  slash.rotationPitch = -90.0F + this.rand.nextFloat() * 180.0F;
                  double slashSpeed = 0.6 + this.rand.nextDouble() * 0.6;
                  double slashAngle = this.rand.nextDouble() * Math.PI * (double)2.0F;
                  double slashPitchRad = (this.rand.nextDouble() - (double)0.5F) * Math.PI;
                  slash.motionX = Math.cos(slashAngle) * Math.cos(slashPitchRad) * slashSpeed;
                  slash.motionY = Math.sin(slashPitchRad) * slashSpeed;
                  slash.motionZ = Math.sin(slashAngle) * Math.cos(slashPitchRad) * slashSpeed;
                  this.world.spawnEntity(slash);
               }

               if (activeTick % 10 == 0) {
                  SoundEvent windSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
                  if (windSound != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, windSound, SoundCategory.PLAYERS, 1.5F, 0.5F);
                  }
               }

               if (activeTick >= 80) {
                  this.expiryBurst();
                  this.setDead();
               }
            }

         } else {
            this.setDead();
         }
      }

      private void spawnFormationParticles() {
         float intensity = (float)this.lifetime / 20.0F;
         int count = (int)(5.0F + intensity * 10.0F);

         for(int i = 0; i < count; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double dist = (double)7.0F * (0.3 + this.rand.nextDouble() * 0.7) * (double)intensity;
            double px = this.posX + Math.cos(angle) * dist;
            double pz = this.posZ + Math.sin(angle) * dist;
            double vx = (this.posX - px) * 0.03;
            double vz = (this.posZ - pz) * 0.03;
            Particles.spawnParticle(this.world, Types.SMOKE, px, this.posY + 0.2, pz, 1, 0.1, 0.1, 0.1, vx, 0.02 * (double)intensity, vz, new int[]{-2228225, 15});
         }

         if (this.lifetime > 10) {
            double height = (double)4.0F * (double)intensity;

            for(int i = 0; i < (int)(3.0F * intensity); ++i) {
               double py = this.posY + this.rand.nextDouble() * height;
               double a = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double r = (double)0.5F * (double)intensity;
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX + Math.cos(a) * r, py, this.posZ + Math.sin(a) * r, 1, 0.1, 0.2, 0.1, (double)0.0F, 0.05, (double)0.0F, new int[]{-1427181842, 12});
            }
         }

      }

      private void applySuction() {
         for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)7.0F, this.posY - (double)1.0F, this.posZ - (double)7.0F, this.posX + (double)7.0F, this.posY + (double)10.0F, this.posZ + (double)7.0F), (e) -> e != this.caster && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
            double dx = this.posX - target.posX;
            double dz = this.posZ - target.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (!(dist > (double)7.0F) && !(dist < (double)0.5F)) {
               if (dist > (double)1.0F) {
                  double pullStrength = 0.35;
                  target.motionX = dx / dist * pullStrength;
                  target.motionZ = dz / dist * pullStrength;
               } else {
                  target.motionX *= 0.3;
                  target.motionZ *= 0.3;
               }

               target.motionY = -0.05;
               target.velocityChanged = true;
            }
         }

      }

      private void applyCoreDamage() {
         float totalDmg = 75.0F + this.power * 20.0F;

         for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)4.0F, this.posY - (double)1.0F, this.posZ - (double)4.0F, this.posX + (double)4.0F, this.posY + (double)10.0F, this.posZ + (double)4.0F), (e) -> e != this.caster && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
            double dist = target.getDistance(this.posX, this.posY, this.posZ);
            if (dist <= (double)4.0F) {
               float cappedDmg = totalDmg;
               if (!(target instanceof EntityPlayer)) {
                  cappedDmg = Math.min(totalDmg, 100.0F);
               }

               target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.caster), cappedDmg);
               target.hurtResistantTime = 0;
            }
         }

      }

      private void applyLift() {
         for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)4.0F, this.posY - (double)1.0F, this.posZ - (double)4.0F, this.posX + (double)4.0F, this.posY + (double)10.0F, this.posZ + (double)4.0F), (e) -> e != this.caster && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
            double dist = target.getDistance(this.posX, this.posY, this.posZ);
            if (dist <= (double)4.0F) {
               target.motionY += 0.15;
               target.velocityChanged = true;
            }
         }

      }

      private void spawnTornadoParticles(int activeTick) {
         double tornadoHeight = (double)8.0F;

         for(int i = 0; i < 30; ++i) {
            double t = (double)i / (double)30.0F;
            double py = this.posY + t * tornadoHeight;
            double radius = (double)4.0F * ((double)1.0F - t * 0.6);
            double spiralAngle = (double)activeTick * 0.3 + t * Math.PI * (double)4.0F + (double)i * 0.2;
            double px = this.posX + Math.cos(spiralAngle) * radius;
            double pz = this.posZ + Math.sin(spiralAngle) * radius;
            double vx = -Math.sin(spiralAngle) * 0.1;
            double vz = Math.cos(spiralAngle) * 0.1;
            Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, 0.15, 0.2, 0.15, vx, 0.05, vz, new int[]{-2232577, 12});
         }

         for(int i = 0; i < 8; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double dist = (double)7.0F * ((double)0.5F + this.rand.nextDouble() * (double)0.5F);
            double px = this.posX + Math.cos(angle) * dist;
            double pz = this.posZ + Math.sin(angle) * dist;
            double vx = (this.posX - px) * 0.05;
            double vz = (this.posZ - pz) * 0.05;
            Particles.spawnParticle(this.world, Types.SMOKE, px, this.posY + 0.15, pz, 1, 0.15, 0.05, 0.15, vx, 0.01, vz, new int[]{-2228225, 18});
         }

         for(int i = 0; i < 6; ++i) {
            double orbitAngle = (double)activeTick * 0.15 + (double)i / (double)6.0F * Math.PI * (double)2.0F;
            double orbitRadX = 4.8;
            double orbitRadZ = 3.2;
            double px = this.posX + Math.cos(orbitAngle) * orbitRadX;
            double py = this.posY + tornadoHeight * 0.4 + Math.sin(orbitAngle * (double)2.0F) * (double)1.0F;
            double pz = this.posZ + Math.sin(orbitAngle) * orbitRadZ;
            double vx = -Math.sin(orbitAngle) * 0.12;
            double vz = Math.cos(orbitAngle) * 0.12;
            Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, 0.2, 0.15, 0.2, vx, 0.02, vz, new int[]{-2232577, 15});
         }

         if (activeTick % 3 == 0) {
            for(int i = 0; i < 4; ++i) {
               double capAngle = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double capDist = this.rand.nextDouble() * (double)4.0F * (double)1.5F;
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX + Math.cos(capAngle) * capDist, this.posY + tornadoHeight + this.rand.nextDouble() * (double)1.5F, this.posZ + Math.sin(capAngle) * capDist, 2, (double)0.5F, 0.3, (double)0.5F, -Math.sin(capAngle) * 0.03, 0.01, Math.cos(capAngle) * 0.03, new int[]{-1427181842, 25});
            }
         }

      }

      private void expiryBurst() {
         for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)7.0F, this.posY - (double)1.0F, this.posZ - (double)7.0F, this.posX + (double)7.0F, this.posY + (double)10.0F, this.posZ + (double)7.0F), (e) -> e != this.caster && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
            double dx = target.posX - this.posX;
            double dz = target.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < (double)0.5F) {
               dist = (double)0.5F;
            }

            if (dist <= (double)7.0F) {
               target.motionX += dx / dist * (double)1.5F;
               target.motionY += (double)0.5F;
               target.motionZ += dz / dist * (double)1.5F;
               target.velocityChanged = true;
            }
         }

         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)2.0F, this.posZ, 50, (double)4.0F, (double)4.0F, (double)4.0F, 0.2, 0.15, 0.2, new int[]{-2232577, 30});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 30, (double)5.0F, (double)1.0F, (double)5.0F, 0.15, 0.05, 0.15, new int[]{-2228225, 25});
         SoundEvent windBoom = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
         if (windBoom != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, windBoom, SoundCategory.PLAYERS, 2.0F, 0.3F);
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
         this.power = compound.getFloat("ftPower");
      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("ftPower", this.power);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (power < 0.3F) {
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
               RayTraceResult rt = ProcedureUtils.raytraceBlocks(entity, (double)20.0F);
               double tx;
               double ty;
               double tz;
               if (rt != null && rt.typeOfHit == RayTraceResult.Type.BLOCK) {
                  tx = rt.hitVec.x;
                  ty = rt.hitVec.y;
                  tz = rt.hitVec.z;
               } else {
                  Vec3d look = entity.getLookVec();
                  tx = entity.posX + look.x * (double)20.0F;
                  ty = entity.posY + (double)entity.getEyeHeight() + look.y * (double)20.0F;
                  tz = entity.posZ + look.z * (double)20.0F;
               }

               EntityCustom tornado = new EntityCustom(entity.world, entity, tx, ty, tz, power);
               entity.world.spawnEntity(tornado);
               SoundEvent windCast = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
               if (windCast != null) {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, windCast, SoundCategory.PLAYERS, 1.5F, 0.5F);
               }

               return true;
            }
         }
      }

      public float getBasePower() {
         return 1.0F;
      }

      public float getPowerupDelay() {
         return 20.0F;
      }

      public float getMaxPower() {
         return 5.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote) {
            double angle = (double)player.ticksExisted * 0.35;

            for(int i = 0; i < 2; ++i) {
               double a = angle + (double)i * Math.PI;
               double offX = Math.cos(a) * (double)0.5F;
               double offZ = Math.sin(a) * (double)0.5F;
               Particles.spawnParticle(player.world, Types.SMOKE, player.posX + offX, player.posY + (double)0.5F + (double)(player.ticksExisted % 20) / (double)20.0F, player.posZ + offZ, 1, 0.05, 0.1, 0.05, -Math.sin(a) * 0.05, 0.03, Math.cos(a) * 0.05, new int[]{-2132939009, 10});
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
