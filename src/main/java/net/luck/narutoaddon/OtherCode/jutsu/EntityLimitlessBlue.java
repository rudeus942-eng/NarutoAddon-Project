
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
public class EntityLimitlessBlue extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 329;
   public static final int PHASE_CHARGE = 0;
   public static final int PHASE_TRAVEL = 1;
   public static final int PHASE_ANCHOR = 2;
   public static final int TOTAL_FRAMES = 16;
   public static final int CHARGE_TICKS_PER_FRAME = 3;
   public static final int CHARGE_FRAMES = 4;
   public static final int CHARGE_DURATION = 12;
   public static final int TRAVEL_TICKS_PER_FRAME = 2;
   public static final int TRAVEL_FRAME_START = 5;
   public static final int TRAVEL_FRAME_END = 16;
   public static final int TRAVEL_MAX_TICKS = 60;
   public static final double TRAVEL_SPEED = 1.4;
   public static final int ANCHOR_TICKS_PER_FRAME = 1;
   public static final int PULSE_CYCLE_TICKS = 16;
   public static final int PULSE_COUNT = 10;
   public static final int ANCHOR_DURATION = 160;
   public static final int PULSE_PEAK_FRAME_OFFSET = 7;
   public static final double ATTRACT_RADIUS = (double)14.0F;
   public static final double ATTRACT_STRENGTH = 2.2;
   public static final double AIM_MAX_RANGE = (double)30.0F;
   public static final double AIM_MOVE_SPEED = 1.1;
   public static final float PULSE_DMG = 78.0F;

   public EntityLimitlessBlue(ElementsInfTsukAddon instance) {
      super(instance, 948);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "limitless_blue"), 329).name("inftsuk_limitless_blue").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, BlueRenderer::new);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final DataParameter<Integer> PHASE;
      private static final DataParameter<Integer> PHASE_TICK;
      private static final DataParameter<Float> RENDER_SCALE;
      private static final DataParameter<String> SHOOTER_ID;
      private EntityLivingBase shooter;
      private UUID shooterUUID;
      private float power;
      private int totalLifetime;
      private int pulsesFired;
      private double interpX;
      private double interpY;
      private double interpZ;
      private int interpSteps;

      public EntityCustom(World world) {
         super(world);
         this.power = 1.0F;
         this.totalLifetime = 0;
         this.pulsesFired = 0;
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
         this.dataManager.register(RENDER_SCALE, 0.2F);
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

      public void setPower(float p) {
         this.power = p;
      }

      @SideOnly(Side.CLIENT)
      public AxisAlignedBB getRenderBoundingBox() {
         return super.getRenderBoundingBox().grow((double)16.0F);
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
               this.setPosition(this.shooter.posX + look.x * 0.9 + right.x * 0.3, this.shooter.posY + (double)this.shooter.getEyeHeight() - (double)0.25F + look.y * (double)0.5F, this.shooter.posZ + look.z * 0.9 + right.z * 0.3);
            }

            if (this.world.isRemote && this.interpSteps > 0 && (this.getPhase() == 1 || this.getPhase() == 2)) {
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
                  this.doAnchorPhase(pt);
               }
            }

            if (this.totalLifetime > 300) {
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
            double hx = this.shooter.posX + look.x * 0.9 + right.x * 0.3;
            double hy = this.shooter.posY + (double)this.shooter.getEyeHeight() - (double)0.25F + look.y * (double)0.5F;
            double hz = this.shooter.posZ + look.z * 0.9 + right.z * 0.3;
            this.setPosition(hx, hy, hz);
            this.motionX = this.motionY = this.motionZ = (double)0.0F;
            float progress = Math.min(1.0F, (float)pt / 12.0F);
            this.dataManager.set(RENDER_SCALE, 0.2F + progress * 0.5F);
            if (pt == 1) {
               SoundEvent charge = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:chakra_charge"));
               if (charge != null) {
                  this.world.playSound((EntityPlayer)null, hx, hy, hz, charge, SoundCategory.PLAYERS, 0.7F, 0.9F);
               } else {
                  this.world.playSound((EntityPlayer)null, hx, hy, hz, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 0.5F, 0.8F);
               }
            }

            if (pt >= 12) {
               this.launch();
            }

         }
      }

      private void launch() {
         this.dataManager.set(PHASE, 1);
         this.dataManager.set(PHASE_TICK, 0);
         this.dataManager.set(RENDER_SCALE, 0.7F);
         this.noClip = false;
         if (this.shooter != null) {
            Vec3d look = this.shooter.getLookVec();
            this.motionX = look.x * 1.4;
            this.motionY = look.y * 1.4;
            this.motionZ = look.z * 1.4;
         }

         SoundEvent launchSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:throw"));
         if (launchSound != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, launchSound, SoundCategory.PLAYERS, 1.0F, 1.2F);
         } else {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 0.8F, 0.9F);
         }

      }

      private void doTravelPhase(int pt) {
         Vec3d from = new Vec3d(this.posX, this.posY, this.posZ);
         Vec3d to = from.add(this.motionX, this.motionY, this.motionZ);
         RayTraceResult blockHit = this.world.rayTraceBlocks(from, to, false, true, false);
         Entity entityHit = null;
         List<Entity> nearby = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow((double)0.5F), (ex) -> ex instanceof EntityLivingBase && ex != this.shooter && ex.isEntityAlive() && ItemJutsu.canTarget((EntityLivingBase)ex));
         double closest = Double.MAX_VALUE;

         for(Entity e : nearby) {
            AxisAlignedBB aabb = e.getEntityBoundingBox().grow(0.3);
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
         if (entityHit != null) {
            this.setPosition(entityHit.posX, entityHit.posY + (double)entityHit.height * (double)0.5F, entityHit.posZ);
            this.anchor();
         } else if (blockHit != null && blockHit.typeOfHit == RayTraceResult.Type.BLOCK) {
            double mlen = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
            double offset = 0.6;
            if (mlen > 0.01) {
               this.setPosition(blockHit.hitVec.x - this.motionX / mlen * offset, blockHit.hitVec.y - this.motionY / mlen * offset, blockHit.hitVec.z - this.motionZ / mlen * offset);
            } else {
               this.setPosition(blockHit.hitVec.x, blockHit.hitVec.y, blockHit.hitVec.z);
            }

            this.anchor();
         } else {
            if (pt >= 60) {
               this.anchor();
            }

         }
      }

      private void anchor() {
         this.dataManager.set(PHASE, 2);
         this.dataManager.set(PHASE_TICK, 0);
         this.dataManager.set(RENDER_SCALE, 1.2F);
         this.motionX = this.motionY = this.motionZ = (double)0.0F;
         this.noClip = true;
         this.pulsesFired = 0;

         for(int i = 0; i < 80; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
            double r = (double)10.5F + this.rand.nextDouble() * (double)2.0F;
            double px = this.posX + Math.sin(phi) * Math.cos(theta) * r;
            double py = this.posY + Math.cos(phi) * r * 0.6;
            double pz = this.posZ + Math.sin(phi) * Math.sin(theta) * r;
            double dx = this.posX - px;
            double dy = this.posY - py;
            double dz = this.posZ - pz;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double spd = 1.4 + this.rand.nextDouble() * 0.8;
            int c = this.pickBluePalette(i);
            Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, dx / len * spd, dy / len * spd, dz / len * spd, new int[]{c, 38, 22, 240});
         }

         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-1, 75, 6, 240});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-257237505, 60, 10, 240});
         SoundEvent anchorSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:chakra_charge"));
         if (anchorSound != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, anchorSound, SoundCategory.PLAYERS, 1.4F, 0.7F);
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

      private void spawnBlueShockwaveFan(double cx, double cy, double cz, double dirX, double dirY, double dirZ, int color, int particleCount) {
         double dlen = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
         if (!(dlen < 0.01)) {
            double nx = dirX / dlen;
            double ny = dirY / dlen;
            double nz = dirZ / dlen;
            double speed = dlen;

            for(int i = 0; i < particleCount; ++i) {
               double ox = (this.rand.nextDouble() - (double)0.5F) * 0.4;
               double oy = (this.rand.nextDouble() - (double)0.5F) * 0.4;
               double oz = (this.rand.nextDouble() - (double)0.5F) * 0.4;
               double vx = nx * speed + (this.rand.nextDouble() - (double)0.5F) * 0.2;
               double vy = ny * speed + (this.rand.nextDouble() - (double)0.5F) * 0.2;
               double vz = nz * speed + (this.rand.nextDouble() - (double)0.5F) * 0.2;
               Particles.spawnParticle(this.world, Types.SMOKE, cx + ox, cy + oy, cz + oz, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{color, 22, 14, 240});
            }

         }
      }

      private void spawnSpiralInward(double radius, double angle, double heightOffset, double spdMul, int color, int scaleT, int age) {
         double px = this.posX + Math.cos(angle) * radius;
         double py = this.posY + heightOffset;
         double pz = this.posZ + Math.sin(angle) * radius;
         double inX = this.posX - px;
         double inZ = this.posZ - pz;
         double inLen = Math.sqrt(inX * inX + inZ * inZ);
         double tanX = -inZ / inLen;
         double tanZ = inX / inLen;
         double spd = (0.55 + this.rand.nextDouble() * 0.3) * spdMul;
         double vx = inX / inLen * spd * 0.65 + tanX * spd * 0.35;
         double vz = inZ / inLen * spd * 0.65 + tanZ * spd * 0.35;
         double vy = (this.posY - py) * 0.05 + (this.rand.nextDouble() - (double)0.5F) * 0.03;
         Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{color, scaleT, age, 240});
      }

      private void doAnchorPhase(int pt) {
         int cycleTick = pt % 16;
         int frameInCycle = cycleTick / 1;
         float pulseStrength;
         if (frameInCycle <= 8) {
            pulseStrength = (float)frameInCycle / 8.0F;
         } else {
            pulseStrength = (float)(16 - frameInCycle) / 8.0F;
         }

         this.dataManager.set(RENDER_SCALE, 1.5F + pulseStrength * 2.5F);
         if (!this.world.isRemote && this.shooter != null) {
            Vec3d eye = this.shooter.getPositionEyes(1.0F);
            Vec3d look = this.shooter.getLookVec();
            Vec3d farPoint = eye.add(look.x * (double)30.0F, look.y * (double)30.0F, look.z * (double)30.0F);
            RayTraceResult blockRay = this.world.rayTraceBlocks(eye, farPoint, false, true, false);
            double aimX;
            double aimY;
            double aimZ;
            if (blockRay != null && blockRay.typeOfHit == RayTraceResult.Type.BLOCK) {
               aimX = blockRay.hitVec.x - look.x * 0.6;
               aimY = blockRay.hitVec.y - look.y * 0.6;
               aimZ = blockRay.hitVec.z - look.z * 0.6;
            } else {
               aimX = farPoint.x;
               aimY = farPoint.y;
               aimZ = farPoint.z;
            }

            double dx = aimX - this.posX;
            double dy = aimY - this.posY;
            double dz = aimZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 0.1) {
               double step = Math.min(1.1, dist);
               this.setPosition(this.posX + dx / dist * step, this.posY + dy / dist * step, this.posZ + dz / dist * step);
            }
         }

         this.pullEntities(pulseStrength);
         double baseAngle = (double)pt * 0.35;

         for(int i = 0; i < 12; ++i) {
            double angle = baseAngle + (double)i / (double)12.0F * Math.PI * (double)2.0F;
            double radius = (double)10.0F + this.rand.nextDouble() * (double)1.5F;
            double yOff = (this.rand.nextDouble() - (double)0.5F) * (double)4.0F;
            int c = this.pickBluePalette(i);
            this.spawnSpiralInward(radius, angle, yOff, 1.1, c, 28, 20);
         }

         if (pt % 2 == 0) {
            double counterAngle = -((double)pt * 0.4);

            for(int i = 0; i < 10; ++i) {
               double angle = counterAngle + (double)i / (double)10.0F * Math.PI * (double)2.0F;
               double radius = (double)7.5F + this.rand.nextDouble() * (double)1.5F;
               double yOff = (this.rand.nextDouble() - (double)0.5F) * (double)3.0F;
               int c = i % 2 == 0 ? -15649844 : -14544487;
               this.spawnSpiralInward(radius, angle, yOff, 0.9, c, 25, 18);
            }
         }

         if (pt % 3 == 0) {
            for(int i = 0; i < 8; ++i) {
               double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double r = (double)3.0F + this.rand.nextDouble() * (double)5.0F;
               double px = this.posX + Math.cos(theta) * r;
               double py = this.posY + (this.rand.nextDouble() - 0.4) * (double)4.0F;
               double pz = this.posZ + Math.sin(theta) * r;
               double dx = this.posX - px;
               double dz = this.posZ - pz;
               double len = Math.sqrt(dx * dx + dz * dz);
               Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, dx / len * 0.18, -0.02, dz / len * 0.18, new int[]{-1335448833, 22, 30, 240});
            }
         }

         for(int i = 0; i < 4; ++i) {
            double theta = (double)pt * 0.6 + (double)i / (double)4.0F * Math.PI * (double)2.0F;
            double rr = 0.6 + this.rand.nextDouble() * 0.8;
            double yOff = (this.rand.nextDouble() - (double)0.5F) * (double)1.0F;
            double px = this.posX + Math.cos(theta) * rr;
            double py = this.posY + yOff;
            double pz = this.posZ + Math.sin(theta) * rr;
            double tanX = -Math.sin(theta) * 0.4;
            double tanZ = Math.cos(theta) * 0.4;
            double inX = (this.posX - px) * 0.3;
            double inZ = (this.posZ - pz) * 0.3;
            int c = i % 2 == 0 ? -1 : -5579265;
            Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, tanX + inX, (this.posY - py) * 0.2, tanZ + inZ, new int[]{c, 20, 12, 240});
         }

         if (pt % 4 == 0) {
            int flashSize = 45 + (int)(pulseStrength * 40.0F);
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-525672961, flashSize, 10, 240});

            for(int i = 0; i < 8; ++i) {
               double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
               double startR = (double)2.0F;
               double sx = this.posX + Math.sin(phi) * Math.cos(theta) * startR;
               double sy = this.posY + Math.cos(phi) * startR * 0.7;
               double sz = this.posZ + Math.sin(phi) * Math.sin(theta) * startR;
               double vx = (this.posX - sx) * 0.8;
               double vy = (this.posY - sy) * 0.8;
               double vz = (this.posZ - sz) * 0.8;
               Particles.spawnParticle(this.world, Types.SMOKE, sx, sy, sz, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{-530142465, 22, 10, 240});
            }
         }

         if (pt % 5 == 0) {
            for(int i = 0; i < 6; ++i) {
               double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double r = (double)5.0F + this.rand.nextDouble() * (double)5.0F;
               double px = this.posX + Math.cos(theta) * r;
               double py = this.posY + (double)1.0F + this.rand.nextDouble() * (double)3.5F;
               double pz = this.posZ + Math.sin(theta) * r;
               double dx = this.posX - px;
               double dz = this.posZ - pz;
               double len = Math.sqrt(dx * dx + dz * dz);
               int c = i % 2 == 0 ? -5579265 : -13408513;
               Particles.spawnParticle(this.world, Types.FALLING_DUST, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, dx / len * (double)0.25F, -0.05, dz / len * (double)0.25F, new int[]{c, 240, 22});
            }
         }

         if (cycleTick == 7 && this.pulsesFired < 10) {
            this.firePulseDamage();
            ++this.pulsesFired;
         }

         if (pt >= 160) {
            this.setDead();
         }

      }

      private void pullEntities(float strength) {
         if (!(strength < 0.05F)) {
            for(EntityLivingBase t : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)14.0F, this.posY - (double)14.0F, this.posZ - (double)14.0F, this.posX + (double)14.0F, this.posY + (double)14.0F, this.posZ + (double)14.0F), (e) -> e != this.shooter && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
               double dx = this.posX - t.posX;
               double dy = this.posY - (t.posY + (double)t.height * (double)0.5F);
               double dz = this.posZ - t.posZ;
               double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
               if (!(len < (double)0.5F) && !(len > (double)14.0F)) {
                  float distFactor = (float)((double)1.0F - len / (double)14.0F);
                  double pull = 2.2 * (double)strength * (double)distFactor * 0.15;
                  t.motionX += dx / len * pull;
                  t.motionY += dy / len * pull;
                  t.motionZ += dz / len * pull;
                  t.fallDistance = 0.0F;
                  t.velocityChanged = true;
               }
            }

         }
      }

      private void firePulseDamage() {
         for(EntityLivingBase t : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)14.0F, this.posY - (double)14.0F, this.posZ - (double)14.0F, this.posX + (double)14.0F, this.posY + (double)14.0F, this.posZ + (double)14.0F), (e) -> e != this.shooter && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
            double d = t.getDistance(this.posX, this.posY, this.posZ);
            if (!(d > (double)14.0F)) {
               float falloff = (float)((double)1.0F - d / (double)14.0F);
               float pulseDmg = 78.0F * falloff;
               if (!(t instanceof EntityPlayer)) {
                  pulseDmg = Math.min(pulseDmg, 100.0F);
               }

               t.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooter), pulseDmg);
               int beamSegments = 22;
               double tEyeX = t.posX;
               double tEyeY = t.posY + (double)t.height * (double)0.5F;
               double tEyeZ = t.posZ;

               for(int s = 0; s < beamSegments; ++s) {
                  double lerp = (double)s / (double)beamSegments;
                  double px = tEyeX + (this.posX - tEyeX) * lerp;
                  double py = tEyeY + (this.posY - tEyeY) * lerp;
                  double pz = tEyeZ + (this.posZ - tEyeZ) * lerp;
                  double bx = (this.posX - px) * 0.15;
                  double by = (this.posY - py) * 0.15;
                  double bz = (this.posZ - pz) * 0.15;
                  int c;
                  if (s % 3 == 0) {
                     c = -1;
                  } else if (s % 3 == 1) {
                     c = -5579265;
                  } else {
                     c = -10048769;
                  }

                  Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, 0.07, 0.07, 0.07, bx, by, bz, new int[]{c, 22, 12, 240});
               }
            }
         }

         for(int i = 0; i < 80; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
            double r = (double)11.0F + this.rand.nextDouble() * (double)1.5F;
            double px = this.posX + Math.sin(phi) * Math.cos(theta) * r;
            double py = this.posY + Math.cos(phi) * r * 0.7;
            double pz = this.posZ + Math.sin(phi) * Math.sin(theta) * r;
            double dx = this.posX - px;
            double dy = this.posY - py;
            double dz = this.posZ - pz;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double spd = 2.2 + this.rand.nextDouble() * (double)1.0F;
            int c = this.pickBluePalette(i);
            Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, dx / len * spd, dy / len * spd, dz / len * spd, new int[]{c, 32, 15, 240});
         }

         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-1, 110, 8, 240});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-255004929, 85, 12, 240});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-533502209, 65, 18, 240});

         for(int i = 0; i < 40; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
            double spd = 0.8 + this.rand.nextDouble() * (double)0.5F;
            double vx = Math.sin(phi) * Math.cos(theta) * spd;
            double vy = Math.cos(phi) * spd;
            double vz = Math.sin(phi) * Math.sin(theta) * spd;
            int r = i % 3;
            int c;
            if (r == 0) {
               c = -1062543873;
            } else if (r == 1) {
               c = -1070373121;
            } else {
               c = -1072614452;
            }

            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 25, 16, 240});
         }

         for(int i = 0; i < 6; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
            double spd = (double)1.0F + this.rand.nextDouble() * 0.4;
            double dx = Math.sin(phi) * Math.cos(theta) * spd;
            double dy = Math.cos(phi) * spd;
            double dz = Math.sin(phi) * Math.sin(theta) * spd;
            int c = i % 2 == 0 ? -5579265 : -13408513;
            this.spawnBlueShockwaveFan(this.posX, this.posY, this.posZ, dx, dy, dz, c, 9);
         }

         SoundEvent pulseSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:shockwave"));
         if (pulseSound != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, pulseSound, SoundCategory.PLAYERS, 1.2F, 0.85F);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.8F, 1.5F);
      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.power = compound.getFloat("bluePower");
         this.totalLifetime = compound.getInteger("blueLifetime");
         this.pulsesFired = compound.getInteger("bluePulses");
         if (compound.hasKey("blueShooterUUID")) {
            try {
               this.shooterUUID = UUID.fromString(compound.getString("blueShooterUUID"));
            } catch (IllegalArgumentException var3) {
            }
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setFloat("bluePower", this.power);
         compound.setInteger("blueLifetime", this.totalLifetime);
         compound.setInteger("bluePulses", this.pulsesFired);
         if (this.shooterUUID != null) {
            compound.setString("blueShooterUUID", this.shooterUUID.toString());
         }

      }

      static {
         PHASE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         PHASE_TICK = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         RENDER_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         SHOOTER_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();
      private static final int COOLDOWN_TICKS = 240;

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (entity.world.isRemote) {
            return false;
         } else if (power < 0.3F) {
            return false;
         } else {
            UUID uid = entity.getUniqueID();
            long now = entity.world.getTotalWorldTime();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 240L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               EntityCustom orb = new EntityCustom(entity.world, entity);
               Vec3d look = entity.getLookVec();
               orb.setPosition(entity.posX + look.x * 0.9, entity.posY + (double)entity.getEyeHeight() - (double)0.25F + look.y * (double)0.5F, entity.posZ + look.z * 0.9);
               orb.setPower(power);
               entity.world.spawnEntity(orb);
               SoundEvent cast = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:chakra_charge"));
               if (cast != null) {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, cast, SoundCategory.PLAYERS, 0.8F, 1.1F);
               }

               return true;
            }
         }
      }

      public float getBasePower() {
         return 0.3F;
      }

      public float getPowerupDelay() {
         return 32.0F;
      }

      public float getMaxPower() {
         return 10.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         super.onUsingTick(stack, player, power);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class BlueRenderer extends Render<EntityCustom> {
      private static final ResourceLocation[] FRAMES = new ResourceLocation[16];

      public BlueRenderer(RenderManager rm) {
         super(rm);
         this.shadowSize = 0.0F;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         int phase = entity.getPhase();
         int pt = entity.getPhaseTick();
         int frameIdx;
         if (phase == 0) {
            int f = Math.min(3, pt / 3);
            frameIdx = f;
         } else if (phase == 1) {
            int span = 12;
            int f = pt / 2 % span;
            frameIdx = 4 + f;
         } else {
            int cycleTick = pt % 16;
            frameIdx = cycleTick / 1;
         }

         ResourceLocation tex = FRAMES[Math.max(0, Math.min(15, frameIdx))];
         float scale = entity.getRenderScale();
         GlStateManager.pushMatrix();
         GlStateManager.translate(x, y, z);
         GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         int thirdPerson = Minecraft.getMinecraft().gameSettings.thirdPersonView;
         GlStateManager.rotate((float)(thirdPerson == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.enableBlend();
         GlStateManager.disableLighting();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         this.bindTexture(tex);
         Tessellator tess = Tessellator.getInstance();
         BufferBuilder buf = tess.getBuffer();
         buf.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         buf.pos((double)-0.5F, (double)-0.5F, (double)0.0F).tex((double)0.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buf.pos((double)0.5F, (double)-0.5F, (double)0.0F).tex((double)1.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buf.pos((double)0.5F, (double)0.5F, (double)0.0F).tex((double)1.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         buf.pos((double)-0.5F, (double)0.5F, (double)0.0F).tex((double)0.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         tess.draw();
         GlStateManager.enableLighting();
         GlStateManager.disableBlend();
         GlStateManager.disableRescaleNormal();
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return FRAMES[0];
      }

      static {
         for(int i = 0; i < 16; ++i) {
            String name = String.format("maxblue_frame%d.png", i + 1);
            FRAMES[i] = new ResourceLocation("inftsukaddon", "textures/entity/blue/" + name);
         }

      }
   }
}
