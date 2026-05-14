
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
public class EntityLimitlessRed extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 327;
   public static final int PHASE_CHARGE = 0;
   public static final int PHASE_TRAVEL = 1;
   public static final int PHASE_EXPLODE = 2;
   public static final int CHARGE_TICKS_PER_FRAME = 3;
   public static final int CHARGE_FRAMES = 6;
   public static final int CHARGE_DURATION = 18;
   public static final int TRAVEL_TICKS_PER_FRAME = 2;
   public static final int TRAVEL_FRAME_START = 7;
   public static final int TRAVEL_FRAME_END = 10;
   public static final int TRAVEL_MAX_TICKS = 60;
   public static final int EXPLODE_TICKS_PER_FRAME = 2;
   public static final int EXPLODE_FRAME_START = 11;
   public static final int EXPLODE_FRAME_END = 20;
   public static final int EXPLODE_SPRITE_TICKS = 20;
   public static final int EXPLODE_DURATION = 45;
   public static final double TRAVEL_SPEED = 1.6;
   public static final double AOE_RADIUS = (double)6.0F;
   public static final double SHOCKWAVE_KB_STRENGTH = (double)3.0F;
   public static final float DIRECT_DMG = 603.0F;
   public static final float AOE_DMG = 361.8F;

   public EntityLimitlessRed(ElementsInfTsukAddon instance) {
      super(instance, 945);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "limitless_red"), 327).name("inftsuk_limitless_red").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, RedRenderer::new);
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
         this.dataManager.register(RENDER_SCALE, 0.15F);
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
                  String s;
                  try {
                     s = (String)this.dataManager.get(SHOOTER_ID);
                  } catch (ClassCastException ex) {
                     if (this.world.isRemote) {
                        this.setDead();
                        return;
                     }

                     throw ex;
                  }

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
                  this.doExplodePhase(pt);
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
            double hx = this.shooter.posX + look.x * 0.9 + right.x * 0.3;
            double hy = this.shooter.posY + (double)this.shooter.getEyeHeight() - (double)0.25F + look.y * (double)0.5F;
            double hz = this.shooter.posZ + look.z * 0.9 + right.z * 0.3;
            this.setPosition(hx, hy, hz);
            this.motionX = this.motionY = this.motionZ = (double)0.0F;
            float progress = Math.min(1.0F, (float)pt / 18.0F);
            this.dataManager.set(RENDER_SCALE, 0.15F + progress * 0.65F);
            if (pt == 1) {
               SoundEvent charge = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:chakra_charge"));
               if (charge != null) {
                  this.world.playSound((EntityPlayer)null, hx, hy, hz, charge, SoundCategory.PLAYERS, 0.7F, 1.3F);
               } else {
                  this.world.playSound((EntityPlayer)null, hx, hy, hz, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 0.5F, 1.8F);
               }
            }

            if (pt >= 18) {
               this.launch();
            }

         }
      }

      private void launch() {
         this.dataManager.set(PHASE, 1);
         this.dataManager.set(PHASE_TICK, 0);
         this.dataManager.set(RENDER_SCALE, 0.8F);
         this.noClip = false;
         if (this.shooter != null) {
            Vec3d look = this.shooter.getLookVec();
            this.motionX = look.x * 1.6;
            this.motionY = look.y * 1.6;
            this.motionZ = look.z * 1.6;
         }

         SoundEvent launchSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
         if (launchSound != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, launchSound, SoundCategory.PLAYERS, 1.2F, 1.4F);
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
         if (pt % 2 == 0) {
            Particles.spawnParticle(this.world, Types.SUSPENDED, this.posX, this.posY, this.posZ, 3, 0.08, 0.08, 0.08, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-2877416, 10, 12});
         }

         if (entityHit != null) {
            this.setPosition(entityHit.posX, entityHit.posY + (double)entityHit.height * (double)0.5F, entityHit.posZ);
            this.explode(entityHit);
         } else if (blockHit != null && blockHit.typeOfHit == RayTraceResult.Type.BLOCK) {
            double mlen = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
            double offset = 0.6;
            if (mlen > 0.01) {
               this.setPosition(blockHit.hitVec.x - this.motionX / mlen * offset, blockHit.hitVec.y - this.motionY / mlen * offset, blockHit.hitVec.z - this.motionZ / mlen * offset);
            } else {
               this.setPosition(blockHit.hitVec.x, blockHit.hitVec.y, blockHit.hitVec.z);
            }

            this.explode((Entity)null);
         } else {
            if (pt >= 60) {
               this.explode((Entity)null);
            }

         }
      }

      private void explode(Entity directHit) {
         this.dataManager.set(PHASE, 2);
         this.dataManager.set(PHASE_TICK, 0);
         this.dataManager.set(RENDER_SCALE, 0.8F);
         this.motionX = this.motionY = this.motionZ = (double)0.0F;
         if (directHit instanceof EntityLivingBase) {
            EntityLivingBase lt = (EntityLivingBase)directHit;
            float cappedDmg = 603.0F;
            if (!(lt instanceof EntityPlayer)) {
               cappedDmg = Math.min(cappedDmg, 100.0F);
            }

            lt.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooter), cappedDmg);
            Vec3d look = this.shooter != null ? this.shooter.getLookVec() : (new Vec3d(this.motionX, this.motionY, this.motionZ)).normalize();
            lt.motionX = look.x * (double)3.0F;
            lt.motionY = 0.7;
            lt.motionZ = look.z * (double)3.0F;
            lt.velocityChanged = true;
         }

         for(EntityLivingBase t : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)6.0F, this.posY - (double)6.0F, this.posZ - (double)6.0F, this.posX + (double)6.0F, this.posY + (double)6.0F, this.posZ + (double)6.0F), (e) -> e != this.shooter && e != directHit && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
            double d = t.getDistance(this.posX, this.posY, this.posZ);
            if (!(d > (double)6.0F)) {
               float falloff = (float)((double)1.0F - d / (double)6.0F);
               float aoeDmg = 361.8F * falloff;
               if (!(t instanceof EntityPlayer)) {
                  aoeDmg = Math.min(aoeDmg, 100.0F);
               }

               t.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooter), aoeDmg);
               double dx = t.posX - this.posX;
               double dz = t.posZ - this.posZ;
               double horizLen = Math.sqrt(dx * dx + dz * dz);
               if (horizLen < 0.01) {
                  t.motionX = (double)0.0F;
                  t.motionZ = (double)0.0F;
                  t.motionY = (double)3.0F;
               } else {
                  double kb = (double)3.0F * (double)(0.5F + 0.5F * falloff);
                  t.motionX = dx / horizLen * kb;
                  t.motionZ = dz / horizLen * kb;
                  t.motionY = 0.55 + 0.3 * (double)falloff;
               }

               t.velocityChanged = true;
               t.fallDistance = 0.0F;
            }
         }

         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, 0.05, (double)0.0F, new int[]{-8739, 85, 6, 240});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-251680086, 65, 10, 240});

         for(int i = 0; i < 90; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
            double spd = 0.4 + this.rand.nextDouble() * 2.6;
            double vx = Math.sin(phi) * Math.cos(theta) * spd;
            double vy = Math.cos(phi) * spd;
            double vz = Math.sin(phi) * Math.sin(theta) * spd;
            int c = this.pickRedPaletteExtended(i);
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 40 + this.rand.nextInt(15), 20 + this.rand.nextInt(12), 240});
         }

         for(int i = 0; i < 36; ++i) {
            double theta = (double)i / (double)36.0F * Math.PI * (double)4.0F;
            double heightOffset = -0.8 + (double)i / (double)36.0F * 1.6;
            double spd = 1.4 + this.rand.nextDouble() * 0.4;
            double vx = Math.cos(theta) * spd;
            double vz = Math.sin(theta) * spd;
            double vy = (this.rand.nextDouble() - (double)0.5F) * 0.2;
            int c = i % 3 == 0 ? -21846 : (i % 3 == 1 ? -53200 : -2291704);
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + heightOffset, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 50, 24, 240});
         }

         for(int i = 0; i < 36; ++i) {
            double theta = -((double)i / (double)36.0F) * Math.PI * (double)4.0F;
            double heightOffset = 0.8 - (double)i / (double)36.0F * 1.6;
            double spd = 1.1 + this.rand.nextDouble() * 0.4;
            double vx = Math.cos(theta) * spd;
            double vz = Math.sin(theta) * spd;
            double vy = (this.rand.nextDouble() - (double)0.5F) * 0.15;
            int c = i % 3 == 0 ? -2291704 : (i % 3 == 1 ? -6289400 : -9435128);
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + heightOffset, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 45, 22, 240});
         }

         for(int i = 0; i < 55; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
            double spd = 0.7 + this.rand.nextDouble() * (double)2.0F;
            double vx = Math.sin(phi) * Math.cos(theta) * spd;
            double vy = Math.cos(phi) * spd;
            double vz = Math.sin(phi) * Math.sin(theta) * spd;
            int c = this.pickRedPaletteExtended(i);
            Particles.spawnParticle(this.world, Types.FALLING_DUST, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 240, 28 + this.rand.nextInt(15)});
         }

         for(int i = 0; i < 18; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            int ring = i % 4;
            double phi;
            if (ring == 0) {
               phi = (Math.PI / 2D);
            } else if (ring == 1) {
               phi = 0.9424777960769379;
            } else if (ring == 2) {
               phi = 2.199114857512855;
            } else {
               phi = Math.PI * (0.2 + this.rand.nextDouble() * 0.6);
            }

            double spd = 1.4 + this.rand.nextDouble() * 0.6;
            double dx = Math.sin(phi) * Math.cos(theta) * spd;
            double dy = Math.cos(phi) * spd;
            double dz = Math.sin(phi) * Math.sin(theta) * spd;
            int c = i % 2 == 0 ? -53200 : -2291704;
            this.spawnShockwaveFan(this.posX, this.posY, this.posZ, dx, dy, dz, c, 10);
         }

         for(int i = 0; i < 40; ++i) {
            double theta = (double)i / (double)40.0F * Math.PI * (double)2.0F + (this.rand.nextDouble() - (double)0.5F) * 0.15;
            double spd = 1.6 + this.rand.nextDouble() * 0.8;
            double vx = Math.cos(theta) * spd;
            double vz = Math.sin(theta) * spd;
            int c = i % 3 == 0 ? -526383096 : (i % 3 == 1 ? -529528824 : -532674552);
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, 0.03, vz, new int[]{c, 55, 28, 240});
         }

         for(int i = 0; i < 35; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double horizSpd = 0.15 + this.rand.nextDouble() * 0.35;
            double vy = ((double)1.5F + this.rand.nextDouble() * 1.8) * (i % 2 == 0 ? (double)1.0F : -0.7);
            int r = this.rand.nextInt(5);
            int c;
            if (r == 0) {
               c = -21846;
            } else if (r == 1) {
               c = -53200;
            } else if (r == 2) {
               c = -2291704;
            } else if (r == 3) {
               c = -6289400;
            } else {
               c = -11532280;
            }

            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, Math.cos(theta) * horizSpd, vy, Math.sin(theta) * horizSpd, new int[]{c, 35, 30, 240});
         }

         for(int i = 0; i < 24; ++i) {
            double ox = (this.rand.nextDouble() - (double)0.5F) * (double)4.0F;
            double oy = (this.rand.nextDouble() - (double)0.5F) * (double)4.0F;
            double oz = (this.rand.nextDouble() - (double)0.5F) * (double)4.0F;
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double spd = 0.2 + this.rand.nextDouble() * 0.35;
            int c = i % 2 == 0 ? -8739 : -57312;
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX + ox, this.posY + oy, this.posZ + oz, 1, (double)0.0F, (double)0.0F, (double)0.0F, Math.cos(theta) * spd, (this.rand.nextDouble() - (double)0.5F) * 0.2, Math.sin(theta) * spd, new int[]{c, 15, 12, 240});
         }

         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 45, 1.6, 1.2, 1.6, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-1071643387, 55, 50, 0});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 25, (double)2.5F, (double)2.0F, (double)2.5F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-1605367800, 70, 65, 0});
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.8F, 0.65F);
         SoundEvent boom = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:bijuuexplode"));
         if (boom != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, boom, SoundCategory.PLAYERS, 1.2F, 1.0F);
         }

      }

      private int pickRedPalette(int seed) {
         int r = (seed + this.rand.nextInt(5)) % 5;
         if (r == 0) {
            return -21846;
         } else if (r == 1) {
            return -49088;
         } else if (r == 2) {
            return -2287592;
         } else {
            return r == 3 ? -6289400 : -9435128;
         }
      }

      private int pickRedPaletteExtended(int seed) {
         int r = (seed + this.rand.nextInt(7)) % 7;
         if (r == 0) {
            return -8739;
         } else if (r == 1) {
            return -21846;
         } else if (r == 2) {
            return -43691;
         } else if (r == 3) {
            return -57312;
         } else if (r == 4) {
            return -2291704;
         } else {
            return r == 5 ? -6289400 : -10483704;
         }
      }

      private void spawnShockwaveFan(double cx, double cy, double cz, double dirX, double dirY, double dirZ, int color, int particleCount) {
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

      private void spawnChainDetonation(double ox, double oy, double oz, float sizeMul, int count) {
         double cx = this.posX + ox;
         double cy = this.posY + oy;
         double cz = this.posZ + oz;
         Particles.spawnParticle(this.world, Types.SMOKE, cx, cy, cz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-251680086, (int)(40.0F * sizeMul), 8, 240});

         for(int i = 0; i < count; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
            double spd = 0.4 + this.rand.nextDouble() * 1.2;
            double vx = Math.sin(phi) * Math.cos(theta) * spd;
            double vy = Math.cos(phi) * spd;
            double vz = Math.sin(phi) * Math.sin(theta) * spd;
            int c = this.pickRedPaletteExtended(i);
            Particles.spawnParticle(this.world, Types.SMOKE, cx, cy, cz, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, (int)(30.0F * sizeMul), 18, 240});
         }

         for(int i = 0; i < count / 3; ++i) {
            double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double spd = 0.6 + this.rand.nextDouble() * (double)1.0F;
            double vy = (this.rand.nextDouble() - (double)0.5F) * 0.8;
            int c = i % 2 == 0 ? -2291704 : -6289400;
            Particles.spawnParticle(this.world, Types.FALLING_DUST, cx, cy, cz, 1, (double)0.0F, (double)0.0F, (double)0.0F, Math.cos(theta) * spd, vy, Math.sin(theta) * spd, new int[]{c, 240, (int)(22.0F * sizeMul)});
         }

      }

      private void doExplodePhase(int pt) {
         if (pt <= 20) {
            float progress = Math.min(1.0F, (float)pt / 20.0F);
            float eased = 1.0F - (1.0F - progress) * (1.0F - progress);
            this.dataManager.set(RENDER_SCALE, 0.8F + eased * 3.0F);
         } else {
            this.dataManager.set(RENDER_SCALE, 0.0F);
         }

         if (!this.world.isRemote) {
            if (pt < 20 && pt % 2 == 0) {
               float edgeProg = (float)pt / 20.0F;
               float edgeEased = 1.0F - (float)Math.pow((double)1.0F - (double)edgeProg, (double)3.0F);
               float radius = 1.5F + edgeEased * 12.0F;

               for(int i = 0; i < 24; ++i) {
                  double theta = (double)i / (double)24.0F * Math.PI * (double)2.0F + (this.rand.nextDouble() - (double)0.5F) * 0.15;
                  double px = this.posX + Math.cos(theta) * (double)radius;
                  double py = this.posY + (this.rand.nextDouble() - 0.3) * 1.6;
                  double pz = this.posZ + Math.sin(theta) * (double)radius;
                  double vx = Math.cos(theta) * 0.45;
                  double vz = Math.sin(theta) * 0.45;
                  double vy = (this.rand.nextDouble() - 0.2) * 0.2;
                  int c = this.pickRedPalette(i);
                  Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c & 16777215 | -536870912, 35, 20, 240});
               }

               for(int i = 0; i < 4; ++i) {
                  double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
                  double px = this.posX + Math.cos(theta) * (double)radius;
                  double py = this.posY + this.rand.nextDouble() * (double)1.0F;
                  double pz = this.posZ + Math.sin(theta) * (double)radius;
                  this.spawnShockwaveFan(px, py, pz, Math.cos(theta) * 1.2, 0.12, Math.sin(theta) * 1.2, -57312, 8);
               }
            }

            if (pt == 2) {
               double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
               this.spawnChainDetonation(Math.cos(theta) * (double)2.5F, 0.3, Math.sin(theta) * (double)2.5F, 0.7F, 18);
            }

            if (pt == 4) {
               double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
               this.spawnChainDetonation(Math.cos(theta) * (double)3.5F, 0.2, Math.sin(theta) * (double)3.5F, 0.8F, 20);
            }

            if (pt == 7) {
               double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
               this.spawnChainDetonation(Math.cos(theta) * (double)4.0F, 1.2, Math.sin(theta) * (double)4.0F, 1.1F, 30);
            }

            if (pt == 10) {
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-251680086, 75, 8, 240});

               for(int i = 0; i < 45; ++i) {
                  double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
                  double phi = Math.acos((double)2.0F * this.rand.nextDouble() - (double)1.0F);
                  double spd = 0.6 + this.rand.nextDouble() * 1.4;
                  double vx = Math.sin(phi) * Math.cos(theta) * spd;
                  double vy = Math.cos(phi) * spd;
                  double vz = Math.sin(phi) * Math.sin(theta) * spd;
                  int c = this.pickRedPaletteExtended(i);
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 40, 22, 240});
               }

               for(int i = 0; i < 8; ++i) {
                  double theta = (double)i / (double)8.0F * Math.PI * (double)2.0F;
                  this.spawnShockwaveFan(this.posX, this.posY, this.posZ, Math.cos(theta) * 1.3, (double)0.0F, Math.sin(theta) * 1.3, -53200, 9);
               }

               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.5F, 0.55F);
            }

            if (pt == 13) {
               double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
               this.spawnChainDetonation(Math.cos(theta) * (double)5.0F, (double)0.5F, Math.sin(theta) * (double)5.0F, 0.9F, 22);
            }

            if (pt == 16) {
               double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
               this.spawnChainDetonation(Math.cos(theta) * (double)2.0F, (double)1.5F, Math.sin(theta) * (double)2.0F, 1.0F, 25);
            }

            if (pt == 19) {
               double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
               this.spawnChainDetonation(Math.cos(theta) * (double)6.0F, 0.3, Math.sin(theta) * (double)6.0F, 0.85F, 18);
            }

            if (pt < 24 && pt % 3 == 0) {
               for(int i = 0; i < 8; ++i) {
                  double theta = this.rand.nextDouble() * Math.PI * (double)2.0F;
                  double r = (double)1.0F + this.rand.nextDouble() * (double)4.0F;
                  double px = this.posX + Math.cos(theta) * r;
                  double py = this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)5.5F;
                  double pz = this.posZ + Math.sin(theta) * r;
                  int c = this.pickRedPalette(i);
                  Particles.spawnParticle(this.world, Types.FALLING_DUST, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (this.rand.nextDouble() - (double)0.5F) * 0.3, (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, (this.rand.nextDouble() - (double)0.5F) * 0.3, new int[]{c, 240, 20});
               }
            }

            if (pt >= 20 && pt % 4 == 0) {
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 6, (double)2.5F, (double)2.5F, (double)2.5F, (this.rand.nextDouble() - (double)0.5F) * 0.05, (double)0.0F, (this.rand.nextDouble() - (double)0.5F) * 0.05, new int[]{-2143287288, 45, 55, 0});
            }
         }

         if (pt >= 45) {
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
         this.power = compound.getFloat("redPower");
         this.totalLifetime = compound.getInteger("redLifetime");
         if (compound.hasKey("redShooterUUID")) {
            try {
               this.shooterUUID = UUID.fromString(compound.getString("redShooterUUID"));
            } catch (IllegalArgumentException var3) {
            }
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setFloat("redPower", this.power);
         compound.setInteger("redLifetime", this.totalLifetime);
         if (this.shooterUUID != null) {
            compound.setString("redShooterUUID", this.shooterUUID.toString());
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
      private static final int COOLDOWN_TICKS = 160;

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (entity.world.isRemote) {
            return false;
         } else if (power < 0.3F) {
            return false;
         } else {
            UUID uid = entity.getUniqueID();
            long now = entity.world.getTotalWorldTime();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 160L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               EntityCustom orb = new EntityCustom(entity.world, entity);
               Vec3d look = entity.getLookVec();
               orb.setPosition(entity.posX + look.x * 0.9, entity.posY + (double)entity.getEyeHeight() - (double)0.25F + look.y * (double)0.5F, entity.posZ + look.z * 0.9);
               orb.setPower(power);
               entity.world.spawnEntity(orb);
               SoundEvent cast = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
               if (cast != null) {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, cast, SoundCategory.PLAYERS, 0.8F, 1.8F);
               }

               return true;
            }
         }
      }

      public float getBasePower() {
         return 0.3F;
      }

      public float getPowerupDelay() {
         return 30.0F;
      }

      public float getMaxPower() {
         return 10.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         super.onUsingTick(stack, player, power);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class RedRenderer extends Render<EntityCustom> {
      private static final ResourceLocation[] FRAMES = new ResourceLocation[20];

      public RedRenderer(RenderManager rm) {
         super(rm);
         this.shadowSize = 0.0F;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         int phase = entity.getPhase();
         int pt = entity.getPhaseTick();
         int frameIdx;
         if (phase == 0) {
            int f = Math.min(5, pt / 3);
            frameIdx = f;
         } else if (phase == 1) {
            int span = 4;
            int f = pt / 2 % span;
            frameIdx = 6 + f;
         } else {
            int f = Math.min(9, pt / 2);
            frameIdx = 10 + f;
         }

         ResourceLocation tex = FRAMES[Math.max(0, Math.min(19, frameIdx))];
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
         for(int i = 0; i < 20; ++i) {
            String name = String.format("orb_%02d.png", i + 1);
            FRAMES[i] = new ResourceLocation("inftsukaddon", "textures/entity/red/" + name);
         }

      }
   }
}
