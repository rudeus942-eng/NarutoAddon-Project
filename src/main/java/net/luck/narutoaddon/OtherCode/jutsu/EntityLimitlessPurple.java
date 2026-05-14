
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityLimitlessPurple extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 330;
   public static final int PHASE_CHARGE = 0;
   public static final int PHASE_TRAVEL = 1;
   public static final int PHASE_ZONE = 2;
   public static final int TOTAL_FRAMES = 8;
   public static final int FRAME_TICKS = 2;
   public static final int CHARGE_DURATION = 50;
   public static final int TRAVEL_MAX_TICKS = 50;
   public static final double TRAVEL_SPEED = (double)2.0F;
   public static final double CHARGE_MAX_SEPARATION = 1.8;
   public static final int ZONE_DURATION = 100;
   public static final double ZONE_RADIUS = (double)14.0F;
   public static final float ZONE_DMG_PER_TICK = 50.0F;
   public static final int ZONE_DAMAGE_INTERVAL = 5;
   public static final int PURPLE_BRIGHT = -3381505;
   public static final int PURPLE_MID = -6737204;
   public static final int PURPLE_DEEP = -10092391;
   public static final int PURPLE_DARK = -13434778;

   public EntityLimitlessPurple(ElementsInfTsukAddon instance) {
      super(instance, 951);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "limitless_purple"), 330).name("inftsuk_limitless_purple").tracker(96, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, PurpleRenderer::new);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final DataParameter<Integer> PHASE;
      private static final DataParameter<Integer> PHASE_TICK;
      private static final DataParameter<Float> RENDER_SCALE;
      private static final DataParameter<Float> CHARGE_RIGHT_X;
      private static final DataParameter<Float> CHARGE_RIGHT_Z;
      private static final DataParameter<String> SHOOTER_ID;
      private EntityLivingBase shooter;
      private UUID shooterUUID;
      private float power;
      private int totalLifetime;
      private double interpX;
      private double interpY;
      private double interpZ;
      private int interpSteps;

      public EntityCustom(World world) {
         super(world);
         this.power = 1.0F;
         this.totalLifetime = 0;
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
         this.dataManager.register(PHASE, 0);
         this.dataManager.register(PHASE_TICK, 0);
         this.dataManager.register(RENDER_SCALE, 0.0F);
         this.dataManager.register(CHARGE_RIGHT_X, 1.0F);
         this.dataManager.register(CHARGE_RIGHT_Z, 0.0F);
         this.dataManager.register(SHOOTER_ID, "");
      }

      public int getPhase() {
         return (Integer)this.dataManager.get(PHASE);
      }

      public int getPhaseTick() {
         return (Integer)this.dataManager.get(PHASE_TICK);
      }

      public float getRenderScale() {
         return (Float)this.dataManager.get(RENDER_SCALE);
      }

      public float getChargeRightX() {
         return (Float)this.dataManager.get(CHARGE_RIGHT_X);
      }

      public float getChargeRightZ() {
         return (Float)this.dataManager.get(CHARGE_RIGHT_Z);
      }

      public void setPower(float p) {
         this.power = p;
      }

      @SideOnly(Side.CLIENT)
      public AxisAlignedBB getRenderBoundingBox() {
         return super.getRenderBoundingBox().grow((double)24.0F);
      }

      public boolean isInRangeToRenderDist(double distance) {
         return distance < (double)4096.0F;
      }

      public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotIncrements, boolean teleport) {
         this.interpX = x;
         this.interpY = y;
         this.interpZ = z;
         this.interpSteps = posRotIncrements + 2;
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
                     } catch (IllegalArgumentException var7) {
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

            if (this.world.isRemote && this.getPhase() == 0 && this.shooter != null) {
               Vec3d look = this.shooter.getLookVec();
               Vec3d right = (new Vec3d(-look.z, (double)0.0F, look.x)).normalize();
               this.setPosition(this.shooter.posX + look.x * (double)1.0F, this.shooter.posY + (double)this.shooter.getEyeHeight() - (double)0.25F + look.y * (double)0.5F, this.shooter.posZ + look.z * (double)1.0F);
               this.dataManager.set(CHARGE_RIGHT_X, (float)right.x);
               this.dataManager.set(CHARGE_RIGHT_Z, (float)right.z);
            }

            if (this.world.isRemote && this.interpSteps > 0 && this.getPhase() == 1) {
               double lx = this.posX + (this.interpX - this.posX) / (double)this.interpSteps;
               double ly = this.posY + (this.interpY - this.posY) / (double)this.interpSteps;
               double lz = this.posZ + (this.interpZ - this.posZ) / (double)this.interpSteps;
               --this.interpSteps;
               this.setPosition(lx, ly, lz);
            }

            if (!this.world.isRemote) {
               int phase = this.getPhase();
               int pt = this.getPhaseTick();
               this.dataManager.set(PHASE_TICK, pt + 1);
               if (phase == 0) {
                  this.doChargePhase(pt);
               } else if (phase == 1) {
                  this.doTravelPhase(pt);
               } else if (phase == 2) {
                  this.doZonePhase(pt);
               }
            }

            if (this.totalLifetime > 200) {
               this.setDead();
            }

         }
      }

      private void doChargePhase(int pt) {
         if (this.shooter == null) {
            this.launch();
         } else {
            Vec3d look = this.shooter.getLookVec();
            Vec3d right = (new Vec3d(-look.z, (double)0.0F, look.x)).normalize();
            double hx = this.shooter.posX + look.x * (double)1.0F;
            double hy = this.shooter.posY + (double)this.shooter.getEyeHeight() - (double)0.25F + look.y * (double)0.5F;
            double hz = this.shooter.posZ + look.z * (double)1.0F;
            this.setPosition(hx, hy, hz);
            this.motionX = this.motionY = this.motionZ = (double)0.0F;
            this.dataManager.set(CHARGE_RIGHT_X, (float)right.x);
            this.dataManager.set(CHARGE_RIGHT_Z, (float)right.z);
            float progress = Math.min(1.0F, (float)pt / 50.0F);
            float purpleProgress = Math.max(0.0F, (progress - 0.45F) / 0.55F);
            this.dataManager.set(RENDER_SCALE, purpleProgress * 0.9F);
            if (pt % 2 == 0 && pt < 48) {
               double sep = ((double)1.0F - (double)progress) * 1.8;
               double redX = hx + right.x * sep;
               double redZ = hz + right.z * sep;
               Particles.spawnParticle(this.world, Types.SMOKE, redX, hy, redZ, 1, 0.15, 0.15, 0.15, -right.x * 0.06, (double)0.0F, -right.z * 0.06, new int[]{-53200, 16, 10, 240});
               double blueX = hx - right.x * sep;
               double blueZ = hz - right.z * sep;
               Particles.spawnParticle(this.world, Types.SMOKE, blueX, hy, blueZ, 1, 0.15, 0.15, 0.15, right.x * 0.06, (double)0.0F, right.z * 0.06, new int[]{-13408513, 16, 10, 240});
            }

            if (pt == 32) {
               Particles.spawnParticle(this.world, Types.SMOKE, hx, hy, hz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-1, 60, 8, 240});

               for(int i = 0; i < 20; ++i) {
                  double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
                  double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
                  double spd = 0.3 + this.rand.nextDouble() * 0.4;
                  Particles.spawnParticle(this.world, Types.SMOKE, hx, hy, hz, 1, (double)0.0F, (double)0.0F, (double)0.0F, Math.sin(phi) * Math.cos(theta) * spd, Math.cos(phi) * spd, Math.sin(phi) * Math.sin(theta) * spd, new int[]{-3381505, 25, 14, 240});
               }

               SoundEvent boom = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:shockwave"));
               if (boom != null) {
                  this.world.playSound((EntityPlayer)null, hx, hy, hz, boom, SoundCategory.PLAYERS, 0.8F, 1.3F);
               }
            }

            if (pt == 1) {
               SoundEvent charge = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:chakra_charge"));
               if (charge != null) {
                  this.world.playSound((EntityPlayer)null, hx, hy, hz, charge, SoundCategory.PLAYERS, 0.9F, 1.0F);
               }
            }

            if (pt >= 50) {
               this.launch();
            }

         }
      }

      private void launch() {
         this.dataManager.set(PHASE, 1);
         this.dataManager.set(PHASE_TICK, 0);
         this.dataManager.set(RENDER_SCALE, 1.0F);
         this.noClip = false;
         if (this.shooter != null) {
            Vec3d look = this.shooter.getLookVec();
            this.motionX = look.x * (double)2.0F;
            this.motionY = look.y * (double)2.0F;
            this.motionZ = look.z * (double)2.0F;
         }

         SoundEvent launchSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:shockwave"));
         if (launchSound != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, launchSound, SoundCategory.PLAYERS, 1.4F, 0.8F);
         }

      }

      private void doTravelPhase(int pt) {
         Vec3d from = new Vec3d(this.posX, this.posY, this.posZ);
         Vec3d to = from.add(this.motionX, this.motionY, this.motionZ);
         RayTraceResult blockHit = this.world.rayTraceBlocks(from, to, false, true, false);
         Entity entityHit = null;
         List<Entity> nearby = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(0.7), (ex) -> ex instanceof EntityLivingBase && ex != this.shooter && ex.isEntityAlive() && ItemJutsu.canTarget((EntityLivingBase)ex));
         double closest = Double.MAX_VALUE;

         for(Entity e : nearby) {
            AxisAlignedBB aabb = e.getEntityBoundingBox().grow(0.4);
            RayTraceResult r = aabb.calculateIntercept(from, to);
            if (r != null) {
               double d = from.squareDistanceTo(r.hitVec);
               if (d < closest) {
                  closest = d;
                  entityHit = e;
               }
            }
         }

         this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

         for(int step = 0; step < 4; ++step) {
            double t = (double)step / (double)4.0F;
            double tx = this.posX - this.motionX * t;
            double ty = this.posY - this.motionY * t;
            double tz = this.posZ - this.motionZ * t;
            int c;
            if (step == 0) {
               c = -1131521;
            } else if (step == 1) {
               c = -3381505;
            } else if (step == 2) {
               c = -6737204;
            } else {
               c = -10092391;
            }

            Particles.spawnParticle(this.world, Types.SMOKE, tx + (this.rand.nextDouble() - (double)0.5F) * 0.15, ty + (this.rand.nextDouble() - (double)0.5F) * 0.15, tz + (this.rand.nextDouble() - (double)0.5F) * 0.15, 1, 0.1, 0.1, 0.1, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{c, 35, 26, 240});
         }

         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 6, 0.55, 0.55, 0.55, (this.rand.nextDouble() - (double)0.5F) * 0.03, (this.rand.nextDouble() - (double)0.5F) * 0.03, (this.rand.nextDouble() - (double)0.5F) * 0.03, new int[]{-13434778, 28, 24, 240});
         if (pt > 2) {
            for(int step = 0; step < 3; ++step) {
               double t = (double)0.5F + (double)step * (double)0.5F;
               double tx = this.posX - this.motionX * t;
               double ty = this.posY - this.motionY * t;
               double tz = this.posZ - this.motionZ * t;
               Particles.spawnParticle(this.world, Types.SMOKE, tx + (this.rand.nextDouble() - (double)0.5F) * 0.4, ty + (this.rand.nextDouble() - (double)0.5F) * 0.4, tz + (this.rand.nextDouble() - (double)0.5F) * 0.4, 1, 0.2, 0.2, 0.2, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-10092391, 40, 30, 240});
            }
         }

         if (pt % 2 == 0) {
            Vec3d motionVec = (new Vec3d(this.motionX, this.motionY, this.motionZ)).normalize();
            double backX = -motionVec.x * 0.4;
            double backY = -motionVec.y * 0.4;
            double backZ = -motionVec.z * 0.4;

            for(int i = 0; i < 5; ++i) {
               double ox = (this.rand.nextDouble() - (double)0.5F) * 0.6;
               double oy = (this.rand.nextDouble() - (double)0.5F) * 0.6;
               double oz = (this.rand.nextDouble() - (double)0.5F) * 0.6;
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX + ox, this.posY + oy, this.posZ + oz, 1, (double)0.0F, (double)0.0F, (double)0.0F, backX + (this.rand.nextDouble() - (double)0.5F) * 0.15, backY + (this.rand.nextDouble() - (double)0.5F) * 0.15, backZ + (this.rand.nextDouble() - (double)0.5F) * 0.15, new int[]{i % 2 == 0 ? -791910657 : -795266356, 24, 14, 240});
            }
         }

         if (pt % 2 == 0) {
            for(int i = 0; i < 3; ++i) {
               double dx = (this.rand.nextDouble() - (double)0.5F) * 0.8;
               double dy = (this.rand.nextDouble() - (double)0.5F) * 0.8;
               double dz = (this.rand.nextDouble() - (double)0.5F) * 0.8;
               int c = this.rand.nextBoolean() ? -3381505 : -6737204;
               Particles.spawnParticle(this.world, Types.FALLING_DUST, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, -this.motionX * 0.3 + dx, -this.motionY * 0.3 + dy, -this.motionZ * 0.3 + dz, new int[]{c, 240, 18});
            }
         }

         if (entityHit != null) {
            this.setPosition(entityHit.posX, entityHit.posY + (double)entityHit.height * (double)0.5F, entityHit.posZ);
            this.enterZone(entityHit);
         } else if (blockHit != null && blockHit.typeOfHit == RayTraceResult.Type.BLOCK) {
            double mlen = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
            double offset = 0.8;
            if (mlen > 0.01) {
               this.setPosition(blockHit.hitVec.x - this.motionX / mlen * offset, blockHit.hitVec.y - this.motionY / mlen * offset, blockHit.hitVec.z - this.motionZ / mlen * offset);
            } else {
               this.setPosition(blockHit.hitVec.x, blockHit.hitVec.y, blockHit.hitVec.z);
            }

            this.enterZone((Entity)null);
         } else {
            if (pt >= 50) {
               this.enterZone((Entity)null);
            }

         }
      }

      private void enterZone(Entity directHit) {
         this.dataManager.set(PHASE, 2);
         this.dataManager.set(PHASE_TICK, 0);
         this.dataManager.set(RENDER_SCALE, 5.0F);
         this.motionX = this.motionY = this.motionZ = (double)0.0F;
         this.noClip = true;
         if (directHit instanceof EntityLivingBase) {
            EntityLivingBase lt = (EntityLivingBase)directHit;
            float directDmg = 260.0F;
            if (!(lt instanceof EntityPlayer)) {
               directDmg = Math.min(directDmg, 100.0F);
            }

            lt.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooter), directDmg);
         }

         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-1, 100, 6, 240});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-252789761, 75, 10, 240});

         for(int i = 0; i < 80; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
            double spd = 0.6 + this.rand.nextDouble() * 2.4;
            double vx = Math.sin(phi) * Math.cos(theta) * spd;
            double vy = Math.cos(phi) * spd;
            double vz = Math.sin(phi) * Math.sin(theta) * spd;
            int c = this.pickPurplePaletteExtended(i);
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 45, 25, 240});
         }

         for(int i = 0; i < 40; ++i) {
            double theta = (double)i / (double)40.0F * Math.PI * (double)2.0F + (this.rand.nextDouble() - (double)0.5F) * 0.12;
            double spd = 1.6 + this.rand.nextDouble() * 0.8;
            int c = i % 3 == 0 ? -3381505 : (i % 3 == 1 ? -6737204 : -10092391);
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, Math.cos(theta) * spd, (double)0.0F, Math.sin(theta) * spd, new int[]{c, 50, 24, 240});
         }

         for(int i = 0; i < 40; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double horizSpd = 0.1 + this.rand.nextDouble() * 0.3;
            double vy = ((double)1.0F + this.rand.nextDouble() * (double)2.5F) * (i % 2 == 0 ? (double)1.0F : -0.85);
            int c = this.pickPurplePaletteExtended(i);
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, Math.cos(theta) * horizSpd, vy, Math.sin(theta) * horizSpd, new int[]{c, 35, 30, 240});
         }

         for(int i = 0; i < 35; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
            double spd = 0.9 + this.rand.nextDouble() * 1.8;
            double vx = Math.sin(phi) * Math.cos(theta) * spd;
            double vy = Math.cos(phi) * spd;
            double vz = Math.sin(phi) * Math.sin(theta) * spd;
            int c = this.rand.nextBoolean() ? -3381505 : -6737204;
            Particles.spawnParticle(this.world, Types.FALLING_DUST, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 240, 26});
         }

         for(int i = 0; i < 20; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.PI * (0.2 + this.rand.nextDouble() * 0.6);
            double spd = 1.3 + this.rand.nextDouble() * 0.6;
            double dx = Math.sin(phi) * Math.cos(theta) * spd;
            double dy = Math.cos(phi) * spd;
            double dz = Math.sin(phi) * Math.sin(theta) * spd;
            int c = i % 2 == 0 ? -3381505 : -6737204;
            this.spawnPurpleShockwaveFan(this.posX, this.posY, this.posZ, dx, dy, dz, c, 10);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 3.0F, 0.45F);
         SoundEvent boom = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:bijuuexplode"));
         if (boom != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, boom, SoundCategory.PLAYERS, 2.0F, 0.65F);
         }

         SoundEvent shockwave = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:shockwave"));
         if (shockwave != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, shockwave, SoundCategory.PLAYERS, 1.5F, 0.9F);
         }

      }

      private void spawnPurpleShockwaveFan(double cx, double cy, double cz, double dirX, double dirY, double dirZ, int color, int particleCount) {
         double dlen = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
         if (!(dlen < 0.01)) {
            double nx = dirX / dlen;
            double ny = dirY / dlen;
            double nz = dirZ / dlen;
            double speed = dlen;

            for(int i = 0; i < particleCount; ++i) {
               double ox = (this.rand.nextDouble() - (double)0.5F) * (double)0.5F;
               double oy = (this.rand.nextDouble() - (double)0.5F) * (double)0.5F;
               double oz = (this.rand.nextDouble() - (double)0.5F) * (double)0.5F;
               double vx = nx * speed + (this.rand.nextDouble() - (double)0.5F) * (double)0.25F;
               double vy = ny * speed + (this.rand.nextDouble() - (double)0.5F) * (double)0.25F;
               double vz = nz * speed + (this.rand.nextDouble() - (double)0.5F) * (double)0.25F;
               Particles.spawnParticle(this.world, Types.SMOKE, cx + ox, cy + oy, cz + oz, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{color, 24, 14, 240});
            }

         }
      }

      private int pickPurplePaletteExtended(int seed) {
         int r = (seed + this.rand.nextInt(7)) % 7;
         if (r == 0) {
            return -1;
         } else if (r == 1) {
            return -1131521;
         } else if (r == 2) {
            return -3381505;
         } else if (r == 3) {
            return -4692225;
         } else if (r == 4) {
            return -6737204;
         } else {
            return r == 5 ? -10092391 : -13434778;
         }
      }

      private void doZonePhase(int pt) {
         float pulsePhase = (float)(pt % 20) / 20.0F;
         float baseScale = 5.0F + 1.5F * (float)Math.sin((double)pulsePhase * Math.PI * (double)2.0F);
         int tickMod20 = pt % 20;
         if (tickMod20 < 4 && pt > 0) {
            float spike = 1.0F - (float)tickMod20 / 4.0F;
            baseScale += spike * 3.0F;
         }

         this.dataManager.set(RENDER_SCALE, baseScale);

         for(int i = 0; i < 7; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
            double spd = 0.3 + this.rand.nextDouble() * 0.7;
            double vx = Math.sin(phi) * Math.cos(theta) * spd;
            double vy = Math.cos(phi) * spd;
            double vz = Math.sin(phi) * Math.sin(theta) * spd;
            int c = this.pickPurplePaletteExtended(i);
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 35, 22, 240});
         }

         for(int i = 0; i < 6; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
            double r = (double)14.0F * (0.7 + this.rand.nextDouble() * 0.3);
            double px = this.posX + Math.sin(phi) * Math.cos(theta) * r;
            double py = this.posY + Math.cos(phi) * r;
            double pz = this.posZ + Math.sin(phi) * Math.sin(theta) * r;
            double dx = this.posX - px;
            double dy = this.posY - py;
            double dz = this.posZ - pz;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double spd = 0.6 + this.rand.nextDouble() * 0.4;
            int c = this.pickPurplePaletteExtended(i + 3);
            Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, dx / len * spd, dy / len * spd, dz / len * spd, new int[]{c, 28, 18, 240});
         }

         if (pt % 3 == 0 && pt > 0) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double r = this.rand.nextDouble() * (double)14.0F * 0.85;
            double bx = this.posX + Math.cos(theta) * r;
            double by = this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)6.0F;
            double bz = this.posZ + Math.sin(theta) * r;
            Particles.spawnParticle(this.world, Types.SMOKE, bx, by, bz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, 0.08, (double)0.0F, new int[]{-252789761, 42, 8, 240});

            for(int i = 0; i < 20; ++i) {
               double t2 = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double p2 = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
               double spd = 0.3 + this.rand.nextDouble() * 0.8;
               double vx = Math.sin(p2) * Math.cos(t2) * spd;
               double vy = Math.cos(p2) * spd + 0.1;
               double vz = Math.sin(p2) * Math.sin(t2) * spd;
               int c = this.pickPurplePaletteExtended(i);
               Particles.spawnParticle(this.world, Types.SMOKE, bx, by, bz, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 25, 16, 240});
            }
         }

         if (pt % 6 == 0 && pt > 0) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double r = this.rand.nextDouble() * (double)14.0F * 0.7;
            double cx = this.posX + Math.cos(theta) * r;
            double cz = this.posZ + Math.sin(theta) * r;

            for(int i = 0; i < 10; ++i) {
               double vy = (0.8 + this.rand.nextDouble() * (double)2.0F) * (i % 2 == 0 ? (double)1.0F : (double)-1.0F);
               double spread = 0.1 + this.rand.nextDouble() * 0.2;
               double st = this.rand.nextDouble() * Math.PI * (double)2.0F;
               int c = this.pickPurplePaletteExtended(i);
               Particles.spawnParticle(this.world, Types.SMOKE, cx, this.posY, cz, 1, (double)0.0F, (double)0.0F, (double)0.0F, Math.cos(st) * spread, vy, Math.sin(st) * spread, new int[]{c, 30, 25, 240});
            }
         }

         if (pt % 5 == 0 && pt < 80) {
            double yH = (this.rand.nextDouble() - (double)0.5F) * (double)4.0F;
            double rStart = (double)1.0F + this.rand.nextDouble() * (double)2.0F;

            for(int i = 0; i < 18; ++i) {
               double theta = (double)i / (double)18.0F * Math.PI * (double)2.0F;
               double spd = 0.8 + this.rand.nextDouble() * 0.4;
               int c = i % 3 == 0 ? -3381505 : (i % 3 == 1 ? -6737204 : -10092391);
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX + Math.cos(theta) * rStart, this.posY + yH, this.posZ + Math.sin(theta) * rStart, 1, (double)0.0F, (double)0.0F, (double)0.0F, Math.cos(theta) * spd, 0.05, Math.sin(theta) * spd, new int[]{c, 32, 20, 240});
            }
         }

         if (pt % 3 == 0) {
            for(int i = 0; i < 3; ++i) {
               double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
               double spd = 0.9 + this.rand.nextDouble() * (double)0.5F;
               double dx = Math.sin(phi) * Math.cos(theta) * spd;
               double dy = Math.cos(phi) * spd;
               double dz = Math.sin(phi) * Math.sin(theta) * spd;
               this.spawnPurpleShockwaveFan(this.posX, this.posY, this.posZ, dx, dy, dz, -3381505, 8);
            }
         }

         if (pt % 4 == 0) {
            for(int i = 0; i < 6; ++i) {
               double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double edge = (double)14.0F * (0.6 + this.rand.nextDouble() * 0.4);
               double px = this.posX + Math.cos(theta) * edge;
               double py = this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)3.0F;
               double pz = this.posZ + Math.sin(theta) * edge;
               double vy;
               double vxMul;
               if (this.rand.nextBoolean()) {
                  vy = 0.3 + this.rand.nextDouble() * 0.4;
                  vxMul = (double)1.0F;
               } else {
                  vy = -0.05;
                  vxMul = -0.3;
               }

               int c = this.rand.nextBoolean() ? -3381505 : -6737204;
               Particles.spawnParticle(this.world, Types.FALLING_DUST, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (this.rand.nextDouble() - (double)0.5F) * 0.3 * vxMul, vy, (this.rand.nextDouble() - (double)0.5F) * 0.3 * vxMul, new int[]{c, 240, 22});
            }
         }

         if (pt > 0 && pt % 20 == 0) {
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-1, 120, 6, 240});
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-255039745, 100, 10, 240});

            for(int i = 0; i < 60; ++i) {
               double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
               double spd = 0.7 + this.rand.nextDouble() * (double)1.5F;
               double vx = Math.sin(phi) * Math.cos(theta) * spd;
               double vy = Math.cos(phi) * spd;
               double vz = Math.sin(phi) * Math.sin(theta) * spd;
               int c = this.pickPurplePaletteExtended(i);
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 45, 25, 240});
            }

            for(int i = 0; i < 8; ++i) {
               double theta = (double)i / (double)8.0F * Math.PI * (double)2.0F;
               this.spawnPurpleShockwaveFan(this.posX, this.posY, this.posZ, Math.cos(theta) * 1.4, (double)0.0F, Math.sin(theta) * 1.4, -1131521, 10);
            }

            SoundEvent boom = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:bijuuexplode"));
            if (boom != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, boom, SoundCategory.PLAYERS, 1.5F, 0.55F);
            }
         }

         if (pt % 5 == 0) {
            for(EntityLivingBase t : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)14.0F, this.posY - (double)14.0F, this.posZ - (double)14.0F, this.posX + (double)14.0F, this.posY + (double)14.0F, this.posZ + (double)14.0F), (e) -> e != this.shooter && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
               double d = t.getDistance(this.posX, this.posY, this.posZ);
               if (!(d > (double)14.0F)) {
                  float falloff = (float)((double)1.0F - d / (double)14.0F);
                  float zoneDmg = 50.0F * falloff;
                  if (!(t instanceof EntityPlayer)) {
                     zoneDmg = Math.min(zoneDmg, 100.0F);
                  }

                  t.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooter), zoneDmg);
                  double dx = t.posX - this.posX;
                  double dz = t.posZ - this.posZ;
                  double len = Math.sqrt(dx * dx + dz * dz);
                  if (len > 0.01) {
                     double sign = pt / 5 % 2 == 0 ? 0.15 : -0.1;
                     t.motionX += dx / len * sign * (double)falloff;
                     t.motionY += 0.08;
                     t.motionZ += dz / len * sign * (double)falloff;
                     t.velocityChanged = true;
                  }
               }
            }
         }

         if (pt % 10 == 0) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.3F, 0.5F + this.rand.nextFloat() * 0.3F);
         }

         if (pt >= 100) {
            this.setDead();
         }

      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.power = compound.getFloat("purplePower");
         this.totalLifetime = compound.getInteger("purpleLifetime");
         if (compound.hasKey("purpleShooterUUID")) {
            try {
               this.shooterUUID = UUID.fromString(compound.getString("purpleShooterUUID"));
            } catch (IllegalArgumentException var3) {
            }
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setFloat("purplePower", this.power);
         compound.setInteger("purpleLifetime", this.totalLifetime);
         if (this.shooterUUID != null) {
            compound.setString("purpleShooterUUID", this.shooterUUID.toString());
         }

      }

      static {
         PHASE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         PHASE_TICK = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         RENDER_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         CHARGE_RIGHT_X = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         CHARGE_RIGHT_Z = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         SHOOTER_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();
      private static final int COOLDOWN_TICKS = 600;

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (entity.world.isRemote) {
            return false;
         } else if (power < 0.3F) {
            return false;
         } else {
            UUID uid = entity.getUniqueID();
            long now = entity.world.getTotalWorldTime();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 600L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               EntityCustom orb = new EntityCustom(entity.world, entity);
               Vec3d look = entity.getLookVec();
               orb.setPosition(entity.posX + look.x * 0.9, entity.posY + (double)entity.getEyeHeight() - (double)0.25F + look.y * (double)0.5F, entity.posZ + look.z * 0.9);
               orb.setPower(power);
               entity.world.spawnEntity(orb);
               return true;
            }
         }
      }

      public float getBasePower() {
         return 0.3F;
      }

      public float getPowerupDelay() {
         return 40.0F;
      }

      public float getMaxPower() {
         return 10.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         super.onUsingTick(stack, player, power);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class PurpleRenderer extends Render<EntityCustom> {
      private static final ResourceLocation[] FRAMES = new ResourceLocation[8];
      private static final ResourceLocation[] RED_CHARGE_FRAMES = new ResourceLocation[]{new ResourceLocation("inftsukaddon", "textures/entity/red/orb_04.png"), new ResourceLocation("inftsukaddon", "textures/entity/red/orb_05.png"), new ResourceLocation("inftsukaddon", "textures/entity/red/orb_06.png")};
      private static final ResourceLocation[] BLUE_CHARGE_FRAMES = new ResourceLocation[]{new ResourceLocation("inftsukaddon", "textures/entity/blue/maxblue_frame6.png"), new ResourceLocation("inftsukaddon", "textures/entity/blue/maxblue_frame7.png"), new ResourceLocation("inftsukaddon", "textures/entity/blue/maxblue_frame8.png")};

      public PurpleRenderer(RenderManager rm) {
         super(rm);
         this.shadowSize = 0.0F;
      }

      private void renderSpriteAt(ResourceLocation tex, double ox, double oy, double oz, float scale, float alpha) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(ox, oy, oz);
         GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         int thirdPerson = Minecraft.getMinecraft().gameSettings.thirdPersonView;
         GlStateManager.rotate((float)(thirdPerson == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
         this.bindTexture(tex);
         Tessellator tess = Tessellator.getInstance();
         BufferBuilder buf = tess.getBuffer();
         buf.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         buf.pos((double)-0.5F, (double)-0.5F, (double)0.0F).tex((double)0.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buf.pos((double)0.5F, (double)-0.5F, (double)0.0F).tex((double)1.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buf.pos((double)0.5F, (double)0.5F, (double)0.0F).tex((double)1.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buf.pos((double)-0.5F, (double)0.5F, (double)0.0F).tex((double)0.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         tess.draw();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.disableRescaleNormal();
         GlStateManager.popMatrix();
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         int phase = entity.getPhase();
         int pt = entity.getPhaseTick();
         GlStateManager.enableBlend();
         GlStateManager.disableLighting();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         if (phase == 0) {
            float progress = Math.min(1.0F, (float)pt / 50.0F);
            float convergeProgress = Math.min(1.0F, progress / 0.75F);
            double sep = ((double)1.0F - (double)convergeProgress) * 1.8;
            double rx = (double)entity.getChargeRightX() * sep;
            double rz = (double)entity.getChargeRightZ() * sep;
            float sideAlpha = progress < 0.75F ? 1.0F : Math.max(0.0F, 1.0F - (progress - 0.75F) / 0.25F);
            int animIdx = pt / 3 % 3;
            if (sideAlpha > 0.01F) {
               this.renderSpriteAt(RED_CHARGE_FRAMES[animIdx], x + rx, y, z + rz, 0.7F, sideAlpha);
               this.renderSpriteAt(BLUE_CHARGE_FRAMES[animIdx], x - rx, y, z - rz, 0.7F, sideAlpha);
            }

            float purpleScale = entity.getRenderScale();
            if (purpleScale > 0.01F) {
               int frameIdx = pt / 2 % 8;
               this.renderSpriteAt(FRAMES[frameIdx], x, y, z, purpleScale, 1.0F);
            }
         } else {
            int frameIdx = entity.ticksExisted / 2 % 8;
            float scale = entity.getRenderScale();
            if (scale > 0.01F) {
               this.renderSpriteAt(FRAMES[frameIdx], x, y, z, scale, 1.0F);
            }
         }

         GlStateManager.enableLighting();
         GlStateManager.disableBlend();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return FRAMES[0];
      }

      static {
         for(int i = 0; i < 8; ++i) {
            String name = String.format("purple_orb_%02d.png", i + 1);
            FRAMES[i] = new ResourceLocation("inftsukaddon", "textures/entity/purple/" + name);
         }

      }
   }
}
