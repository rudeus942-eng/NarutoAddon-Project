
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
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
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

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityLimitlessBlueNormal extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 332;
   public static final int DURATION_TICKS = 120;
   public static final int CYCLE_TICKS = 32;
   public static final double PULL_RADIUS = (double)10.0F;
   public static final double PULL_STRENGTH = 1.2;
   public static final int DAMAGE_INTERVAL = 20;
   public static final float DAMAGE_PER_PULSE = 16.0F;
   public static final double VORTEX_OUTER_RADIUS = (double)3.5F;
   public static final double VORTEX_INNER_RADIUS = (double)2.0F;

   public EntityLimitlessBlueNormal(ElementsInfTsukAddon instance) {
      super(instance, 956);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "limitless_blue_normal"), 332).name("inftsuk_limitless_blue_normal").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, BlueNormalRenderer::new);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final DataParameter<String> SHOOTER_ID;
      private EntityLivingBase shooter;
      private UUID shooterUUID;
      private int totalLifetime;
      private final Random rand;

      public EntityCustom(World world) {
         super(world);
         this.totalLifetime = 0;
         this.rand = new Random();
         this.setSize(0.1F, 0.1F);
         this.noClip = true;
         this.isImmuneToFire = true;
      }

      public EntityCustom(World world, EntityLivingBase shooter) {
         this(world);
         this.shooter = shooter;
         this.shooterUUID = shooter.getUniqueID();
         this.dataManager.set(SHOOTER_ID, this.shooterUUID.toString());
      }

      public Type getJutsuType() {
         return Type.SHAKUTON;
      }

      protected void entityInit() {
         this.dataManager.register(SHOOTER_ID, "");
      }

      @SideOnly(Side.CLIENT)
      public AxisAlignedBB getRenderBoundingBox() {
         return super.getRenderBoundingBox().grow((double)16.0F);
      }

      public boolean isInRangeToRenderDist(double distance) {
         return distance < (double)4096.0F;
      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.totalLifetime;
         if (!this.world.isBlockLoaded(new BlockPos(this))) {
            this.setDead();
         } else {
            if (this.shooter == null) {
               if (this.shooterUUID == null) {
                  String s = (String)this.dataManager.get(SHOOTER_ID);
                  if (!s.isEmpty()) {
                     try {
                        this.shooterUUID = UUID.fromString(s);
                     } catch (IllegalArgumentException var13) {
                     }
                  }
               }

               if (this.shooterUUID != null) {
                  Entity e = this.world.getPlayerEntityByUUID(this.shooterUUID);
                  if (e instanceof EntityLivingBase) {
                     this.shooter = (EntityLivingBase)e;
                  }
               }
            }

            if (this.shooter != null && this.shooter.isEntityAlive()) {
               Vec3d look = this.shooter.getLookVec();
               Vec3d right = (new Vec3d(-look.z, (double)0.0F, look.x)).normalize();
               double hx = this.shooter.posX + look.x * 0.6 + right.x * 0.45;
               double hy = this.shooter.posY + (double)this.shooter.getEyeHeight() - 0.35 + look.y * 0.3;
               double hz = this.shooter.posZ + look.z * 0.6 + right.z * 0.45;
               this.setPosition(hx, hy, hz);
               this.motionX = this.motionY = this.motionZ = (double)0.0F;
               if (!this.world.isRemote) {
                  int cycle = this.totalLifetime % 32;
                  float half = 16.0F;
                  float p = (float)cycle <= half ? (float)cycle / half : (float)(32 - cycle) / half;
                  this.spawnHandVacuum(this.totalLifetime, p);
                  this.pullToCaster(p);
                  if (cycle == (int)half && this.totalLifetime >= 20) {
                     this.damagePulse();
                     this.world.playSound((EntityPlayer)null, hx, hy, hz, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.4F, 1.6F);
                  }

                  if (this.totalLifetime == 1) {
                     SoundEvent charge = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:chakra_charge"));
                     if (charge != null) {
                        this.world.playSound((EntityPlayer)null, hx, hy, hz, charge, SoundCategory.PLAYERS, 0.5F, 1.1F);
                     }
                  }

                  if (this.totalLifetime >= 120) {
                     this.setDead();
                  }
               }

            } else {
               this.setDead();
            }
         }
      }

      private int pickBluePalette(int seed) {
         int r = (seed + this.rand.nextInt(5)) % 5;
         if (r == 0) {
            return -5579265;
         } else if (r == 1) {
            return -10048769;
         } else if (r == 2) {
            return -13408513;
         } else {
            return r == 3 ? -15649844 : -14544487;
         }
      }

      private void spawnHandVacuum(int t, float pulseStrength) {
         double baseAngle = (double)t * 0.35;

         for(int i = 0; i < 6; ++i) {
            double angle = baseAngle + (double)i / (double)6.0F * Math.PI * (double)2.0F;
            double radius = (double)2.0F + this.rand.nextDouble() * (double)1.5F;
            double yOff = (this.rand.nextDouble() - (double)0.5F) * 1.2;
            this.spawnSpiralInward(radius, angle, yOff, 0.9, this.pickBluePalette(i), 18, 16);
         }

         if (t % 2 == 0) {
            double counterAngle = -((double)t * 0.45);

            for(int i = 0; i < 4; ++i) {
               double angle = counterAngle + (double)i / (double)4.0F * Math.PI * (double)2.0F;
               double radius = 1.2 + this.rand.nextDouble() * (double)1.0F;
               double yOff = (this.rand.nextDouble() - (double)0.5F) * 0.9;
               int c = i % 2 == 0 ? -15649844 : -14544487;
               this.spawnSpiralInward(radius, angle, yOff, 0.8, c, 15, 14);
            }
         }

         for(int i = 0; i < 3; ++i) {
            double theta = (double)t * 0.6 + (double)i / (double)3.0F * Math.PI * (double)2.0F;
            double rr = 0.3 + this.rand.nextDouble() * (double)0.5F;
            double yOff = (this.rand.nextDouble() - (double)0.5F) * 0.4;
            double px = this.posX + Math.cos(theta) * rr;
            double py = this.posY + yOff;
            double pz = this.posZ + Math.sin(theta) * rr;
            double tanX = -Math.sin(theta) * (double)0.25F;
            double tanZ = Math.cos(theta) * (double)0.25F;
            double inX = (this.posX - px) * 0.3;
            double inZ = (this.posZ - pz) * 0.3;
            int c = i % 2 == 0 ? -1 : -5579265;
            Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, tanX + inX, (this.posY - py) * 0.2, tanZ + inZ, new int[]{c, 14, 10, 240});
         }

         if (t % 4 == 0) {
            int flashSize = 16 + (int)(pulseStrength * 14.0F);
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-794108417, flashSize, 8, 240});
         }

      }

      private void spawnSpiralInward(double radius, double angle, double heightOffset, double spdMul, int color, int scaleT, int age) {
         double px = this.posX + Math.cos(angle) * radius;
         double py = this.posY + heightOffset;
         double pz = this.posZ + Math.sin(angle) * radius;
         double inX = this.posX - px;
         double inZ = this.posZ - pz;
         double inLen = Math.sqrt(inX * inX + inZ * inZ);
         if (!(inLen < 0.01)) {
            double tanX = -inZ / inLen;
            double tanZ = inX / inLen;
            double spd = (0.45 + this.rand.nextDouble() * (double)0.25F) * spdMul;
            double vx = inX / inLen * spd * 0.65 + tanX * spd * 0.35;
            double vz = inZ / inLen * spd * 0.65 + tanZ * spd * 0.35;
            double vy = (this.posY - py) * 0.05 + (this.rand.nextDouble() - (double)0.5F) * 0.02;
            Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{color, scaleT, age, 240});
         }
      }

      private void pullToCaster(float strength) {
         if (!(strength < 0.05F)) {
            double casterX = this.shooter.posX;
            double casterY = this.shooter.posY + (double)this.shooter.height * (double)0.5F;
            double casterZ = this.shooter.posZ;

            for(EntityLivingBase t : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(casterX - (double)10.0F, casterY - (double)10.0F, casterZ - (double)10.0F, casterX + (double)10.0F, casterY + (double)10.0F, casterZ + (double)10.0F), (e) -> e != this.shooter && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
               double dx = casterX - t.posX;
               double dy = casterY - (t.posY + (double)t.height * (double)0.5F);
               double dz = casterZ - t.posZ;
               double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
               if (!(len < (double)1.5F) && !(len > (double)10.0F)) {
                  float falloff = (float)((double)1.0F - len / (double)10.0F);
                  double pull = 1.2 * (double)strength * (double)falloff * 0.18;
                  t.motionX += dx / len * pull;
                  t.motionY += dy / len * pull;
                  t.motionZ += dz / len * pull;
                  t.fallDistance = 0.0F;
                  t.velocityChanged = true;
               }
            }

         }
      }

      private void damagePulse() {
         double casterX = this.shooter.posX;
         double casterY = this.shooter.posY + (double)this.shooter.height * (double)0.5F;
         double casterZ = this.shooter.posZ;

         for(EntityLivingBase t : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(casterX - (double)10.0F, casterY - (double)10.0F, casterZ - (double)10.0F, casterX + (double)10.0F, casterY + (double)10.0F, casterZ + (double)10.0F), (e) -> e != this.shooter && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
            double d = (double)t.getDistance(this.shooter);
            if (!(d > (double)10.0F)) {
               float falloff = (float)((double)1.0F - d / (double)10.0F);
               t.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooter), 16.0F * falloff);
            }
         }

         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 8, 0.3, 0.3, 0.3, 0.08, 0.08, 0.08, new int[]{-533502209, 15, 12, 240});
      }

      protected void readEntityFromNBT(NBTTagCompound c) {
         this.totalLifetime = c.getInteger("nbLife");
         if (c.hasKey("nbShooter")) {
            try {
               this.shooterUUID = UUID.fromString(c.getString("nbShooter"));
            } catch (IllegalArgumentException var3) {
            }
         }

      }

      protected void writeEntityToNBT(NBTTagCompound c) {
         c.setInteger("nbLife", this.totalLifetime);
         if (this.shooterUUID != null) {
            c.setString("nbShooter", this.shooterUUID.toString());
         }

      }

      static {
         SHOOTER_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();
      private static final int COOLDOWN_TICKS = 180;

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (entity.world.isRemote) {
            return false;
         } else if (power < 0.3F) {
            return false;
         } else {
            UUID uid = entity.getUniqueID();
            long now = entity.world.getTotalWorldTime();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 180L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               EntityCustom orb = new EntityCustom(entity.world, entity);
               Vec3d look = entity.getLookVec();
               orb.setPosition(entity.posX + look.x * 0.6, entity.posY + (double)entity.getEyeHeight() - 0.35, entity.posZ + look.z * 0.6);
               entity.world.spawnEntity(orb);
               return true;
            }
         }
      }

      public float getBasePower() {
         return 0.3F;
      }

      public float getPowerupDelay() {
         return 15.0F;
      }

      public float getMaxPower() {
         return 10.0F;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class BlueNormalRenderer extends Render<EntityCustom> {
      private static final ResourceLocation BLANK = new ResourceLocation("inftsukaddon", "textures/entity/blue/maxblue_frame1.png");

      public BlueNormalRenderer(RenderManager rm) {
         super(rm);
         this.shadowSize = 0.0F;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return BLANK;
      }
   }
}
