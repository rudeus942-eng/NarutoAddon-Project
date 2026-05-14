
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Particles;
import net.narutomod.Particles.Types;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemJutsu.JutsuEnum.Type;

import java.util.*;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityShadowRend extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 336;
   public static final int PHASE_CHARGE = 0;
   public static final int PHASE_BLAST = 1;
   public static final int PHASE_AFTERMATH = 2;
   public static final int CHARGE_TICKS = 35;
   public static final int BLAST_TICKS = 40;
   public static final int AFTERMATH_TICKS = 30;
   public static final double MAX_RANGE = (double)48.0F;
   public static final double HALF_ANGLE_RAD = Math.toRadians((double)22.0F);
   public static final double TAN_HALF_ANGLE;
   public static final float DAMAGE = 218.0F;
   private static final int COLOR_CORE = -16119286;
   private static final int COLOR_BLADE = -14013910;
   private static final int COLOR_EDGE = -535291880;
   private static final int COLOR_PERIM = -1070254795;
   private static final int COLOR_SHARD = -16777216;
   private static final int COLOR_TENDRIL = -535163105;
   private static final int COLOR_ACCENT = -14020822;
   private static final int COLOR_CRIMSON = -13631473;

   public EntityShadowRend(ElementsInfTsukAddon instance) {
      super(instance, 964);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "shadow_rend"), 336).name("inftsuk_shadow_rend").tracker(96, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, ShadowRendRenderer::new);
   }

   static {
      TAN_HALF_ANGLE = Math.tan(HALF_ANGLE_RAD);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final DataParameter<Integer> PHASE;
      private static final DataParameter<Integer> PHASE_TICK;
      private static final DataParameter<String> SHOOTER_ID;
      private Vec3d lockedDir;
      private EntityLivingBase shooter;
      private UUID shooterUUID;
      private final Set<UUID> hitEntities;
      private int totalLifetime;

      public EntityCustom(World world) {
         super(world);
         this.hitEntities = new HashSet();
         this.totalLifetime = 0;
         this.setSize(0.1F, 0.1F);
         this.noClip = true;
         this.isImmuneToFire = true;
      }

      public EntityCustom(World world, EntityLivingBase caster) {
         this(world);
         this.shooter = caster;
         this.shooterUUID = caster.getUniqueID();
         this.dataManager.set(SHOOTER_ID, this.shooterUUID.toString());
         this.setPosition(caster.posX, caster.posY, caster.posZ);
         this.lockedDir = caster.getLookVec().normalize();
      }

      public Type getJutsuType() {
         return Type.YOTON;
      }

      protected void entityInit() {
         this.dataManager.register(PHASE, 0);
         this.dataManager.register(PHASE_TICK, 0);
         this.dataManager.register(SHOOTER_ID, "");
      }

      public int getPhase() {
         return (Integer)this.dataManager.get(PHASE);
      }

      public int getPhaseTick() {
         return (Integer)this.dataManager.get(PHASE_TICK);
      }

      @SideOnly(Side.CLIENT)
      public AxisAlignedBB getRenderBoundingBox() {
         return super.getRenderBoundingBox().grow((double)64.0F);
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
                     } catch (IllegalArgumentException var6) {
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
               if (this.getPhase() == 0) {
                  Vec3d look = this.shooter.getLookVec();
                  this.setPosition(this.shooter.posX + look.x * 0.4, this.shooter.posY + (double)this.shooter.getEyeHeight() - 0.4, this.shooter.posZ + look.z * 0.4);
                  if (!this.world.isRemote) {
                     this.lockedDir = look.normalize();
                  }
               }

               if (!this.world.isRemote) {
                  int pt = this.getPhaseTick() + 1;
                  this.dataManager.set(PHASE_TICK, pt);
                  switch (this.getPhase()) {
                     case 0:
                        if (pt == 1) {
                           SoundEvent gather = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:chakra_charge"));
                           if (gather != null) {
                              this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, gather, SoundCategory.PLAYERS, 1.2F, 0.55F);
                           }
                        }

                        if (pt >= 35) {
                           this.dataManager.set(PHASE, 1);
                           this.dataManager.set(PHASE_TICK, 0);
                           this.setPosition(this.shooter.posX + this.lockedDir.x * 0.4, this.shooter.posY + (double)this.shooter.getEyeHeight() - 0.4, this.shooter.posZ + this.lockedDir.z * 0.4);
                           this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 1.4F, 0.8F);
                           this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.4F, 0.6F);
                        }
                        break;
                     case 1:
                        double progress = (double)pt / (double)40.0F;
                        double blastLength = progress * (double)48.0F;
                        this.damageEntitiesInCone(blastLength);
                        if (pt >= 40) {
                           this.dataManager.set(PHASE, 2);
                           this.dataManager.set(PHASE_TICK, 0);
                        }
                        break;
                     case 2:
                        if (pt >= 30) {
                           this.setDead();
                           return;
                        }
                  }
               }

               if (!this.world.isRemote && this.world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)this.world;
                  switch (this.getPhase()) {
                     case 0:
                        this.spawnChargeFX(ws);
                        break;
                     case 1:
                        this.spawnBlastFX(ws);
                        break;
                     case 2:
                        this.spawnAftermathFX(ws);
                  }
               }

            } else {
               this.setDead();
            }
         }
      }

      private void damageEntitiesInCone(double blastLength) {
         double boxReach = Math.min(blastLength, (double)48.0F) + (double)2.0F;
         double boxPad = (double)48.0F * EntityShadowRend.TAN_HALF_ANGLE + (double)2.0F;
         Vec3d origin = new Vec3d(this.posX, this.posY, this.posZ);
         Vec3d far = origin.add(this.lockedDir.x * boxReach, this.lockedDir.y * boxReach, this.lockedDir.z * boxReach);
         double minX = Math.min(origin.x, far.x) - boxPad;
         double maxX = Math.max(origin.x, far.x) + boxPad;
         double minY = Math.min(origin.y, far.y) - boxPad;
         double maxY = Math.max(origin.y, far.y) + boxPad;
         double minZ = Math.min(origin.z, far.z) - boxPad;
         double maxZ = Math.max(origin.z, far.z) + boxPad;
         AxisAlignedBB scan = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);

         for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, scan, (ex) -> ex != this.shooter && ex.isEntityAlive() && ItemJutsu.canTarget(ex))) {
            if (!this.hitEntities.contains(e.getUniqueID())) {
               Vec3d toTarget = new Vec3d(e.posX - origin.x, e.posY + (double)e.height * (double)0.5F - origin.y, e.posZ - origin.z);
               double axial = toTarget.dotProduct(this.lockedDir);
               if (!(axial <= (double)0.0F) && !(axial > blastLength)) {
                  Vec3d axisPoint = new Vec3d(this.lockedDir.x * axial, this.lockedDir.y * axial, this.lockedDir.z * axial);
                  Vec3d perp = toTarget.subtract(axisPoint);
                  double perpDist = perp.length();
                  double coneRadius = axial * EntityShadowRend.TAN_HALF_ANGLE;
                  if (!(perpDist > coneRadius)) {
                     this.hitEntities.add(e.getUniqueID());
                     float dmg = 218.0F;
                     if (!(e instanceof EntityPlayer)) {
                        dmg = Math.min(dmg, 100.0F);
                     }

                     e.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooter), dmg);
                     e.motionX += this.lockedDir.x * 2.4;
                     e.motionY += 0.45;
                     e.motionZ += this.lockedDir.z * 2.4;
                     e.fallDistance = 0.0F;
                     e.velocityChanged = true;
                     this.world.playSound((EntityPlayer)null, e.posX, e.posY, e.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 1.35F);
                  }
               }
            }
         }

      }

      private void spawnChargeFX(WorldServer ws) {
         int pt = this.getPhaseTick();
         float buildup = (float)pt / 35.0F;
         double spiralAngle = (double)this.totalLifetime * 0.45;
         double outerRadius = 1.8 * ((double)1.0F - (double)buildup * (double)0.75F);

         for(int i = 0; i < 16; ++i) {
            double angle = spiralAngle + (double)i / (double)16.0F * Math.PI * (double)2.0F;
            double ox = this.posX + Math.cos(angle) * outerRadius;
            double oy = this.posY + (ws.rand.nextDouble() - (double)0.5F) * 0.8;
            double oz = this.posZ + Math.sin(angle) * outerRadius;
            double vx = (this.posX - ox) * (double)0.5F;
            double vy = (this.posY - oy) * (double)0.5F;
            double vz = (this.posZ - oz) * (double)0.5F;
            int c = i % 3 == 0 ? -14013910 : (i % 3 == 1 ? -1070254795 : -535163105);
            Particles.spawnParticle(ws, Types.SMOKE, ox, oy, oz, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 16, 12, 240});
         }

         if (pt % 2 == 0) {
            double counterAngle = -((double)this.totalLifetime * 0.55);
            double counterRadius = (double)1.0F * ((double)1.0F - (double)buildup * 0.6);

            for(int i = 0; i < 10; ++i) {
               double angle = counterAngle + (double)i / (double)10.0F * Math.PI * (double)2.0F;
               double ox = this.posX + Math.cos(angle) * counterRadius;
               double oy = this.posY + (ws.rand.nextDouble() - (double)0.5F) * (double)0.5F;
               double oz = this.posZ + Math.sin(angle) * counterRadius;
               double vx = (this.posX - ox) * 0.35;
               double vy = (this.posY - oy) * 0.35;
               double vz = (this.posZ - oz) * 0.35;
               Particles.spawnParticle(ws, Types.SMOKE, ox, oy, oz, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{-535163105, 13, 10, 240});
            }
         }

         int tendrilCount = 4 + (int)(buildup * 6.0F);

         for(int i = 0; i < tendrilCount; ++i) {
            double theta = ws.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = ws.rand.nextDouble() * Math.PI;
            double spd = 0.15 + ws.rand.nextDouble() * (double)0.25F;
            double vx = Math.sin(phi) * Math.cos(theta) * spd;
            double vy = Math.cos(phi) * spd * 0.6;
            double vz = Math.sin(phi) * Math.sin(theta) * spd;
            Particles.spawnParticle(ws, Types.FALLING_DUST, this.posX, this.posY + 0.2, this.posZ, 1, 0.05, 0.05, 0.05, vx, vy, vz, new int[]{-535163105, 240, 10});
         }

         if (pt % 6 == 0) {
            int ringCount = 24;
            double ringRadius = 2.2 + (double)buildup * 0.8;

            for(int i = 0; i < ringCount; ++i) {
               double angle = (double)i / (double)ringCount * Math.PI * (double)2.0F;
               double px = this.posX + Math.cos(angle) * ringRadius;
               double py = this.posY + 0.2;
               double pz = this.posZ + Math.sin(angle) * ringRadius;
               double vx = Math.cos(angle) * (double)-0.25F;
               double vz = Math.sin(angle) * (double)-0.25F;
               int c = i % 2 == 0 ? -14013910 : -14020822;
               Particles.spawnParticle(ws, Types.SMOKE, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, (double)0.0F, vz, new int[]{c, 14, 16, 240});
            }
         }

         if (pt % 3 == 0) {
            int scaleT = 16 + (int)(buildup * 40.0F);
            Particles.spawnParticle(ws, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-16119286, scaleT, 10, 240});
            if (ws.rand.nextInt(2) == 0) {
               Particles.spawnParticle(ws, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-14020822, 20 + (int)(buildup * 20.0F), 8, 240});
            }
         }

         if (pt > 0 && pt % 10 == 0) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.PLAYERS, 0.6F + buildup * 0.3F, 0.4F + buildup * 0.2F);
         }

      }

      private void spawnBlastFX(WorldServer ws) {
         int pt = this.getPhaseTick();
         double progress = (double)pt / (double)40.0F;
         double blastLength = progress * (double)48.0F;
         Vec3d up = Math.abs(this.lockedDir.y) > 0.9 ? new Vec3d((double)1.0F, (double)0.0F, (double)0.0F) : new Vec3d((double)0.0F, (double)1.0F, (double)0.0F);
         Vec3d right = this.lockedDir.crossProduct(up).normalize();
         Vec3d trueUp = right.crossProduct(this.lockedDir).normalize();
         if (pt == 0) {
            this.spawnReleaseShockwave(ws, right, trueUp);
         }

         if (pt == 39) {
            this.spawnImpactCloud(ws, right, trueUp);
         }

         int coreSteps = 16;

         for(int i = 0; i < coreSteps; ++i) {
            double t = ((double)i + (double)0.5F) * (blastLength / (double)coreSteps);
            double px = this.posX + this.lockedDir.x * t;
            double py = this.posY + this.lockedDir.y * t;
            double pz = this.posZ + this.lockedDir.z * t;
            Particles.spawnParticle(ws, Types.SMOKE, px, py, pz, 1, 0.1, 0.1, 0.1, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-16119286, 12, 6, 240});
         }

         Vec3d[] spokeDirs = new Vec3d[]{trueUp, trueUp.scale((double)-1.0F), right, right.scale((double)-1.0F)};
         int spokeSteps = 10;

         for(Vec3d sp : spokeDirs) {
            for(int i = 1; i <= spokeSteps; ++i) {
               double t = (double)i / (double)spokeSteps * blastLength;
               double sr = t * EntityShadowRend.TAN_HALF_ANGLE;
               double px = this.posX + this.lockedDir.x * t + sp.x * sr;
               double py = this.posY + this.lockedDir.y * t + sp.y * sr;
               double pz = this.posZ + this.lockedDir.z * t + sp.z * sr;
               int c = i % 3 == 0 ? -14020822 : -14013910;
               Particles.spawnParticle(ws, Types.SMOKE, px, py, pz, 1, 0.05, 0.05, 0.05, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{c, 15, 8, 240});
            }
         }

         double frontRadius = blastLength * EntityShadowRend.TAN_HALF_ANGLE;
         int ringCount = 24;

         for(int i = 0; i < ringCount; ++i) {
            double a = (double)i / (double)ringCount * Math.PI * (double)2.0F;
            double ex = Math.cos(a) * frontRadius;
            double ey = Math.sin(a) * frontRadius;
            double px = this.posX + this.lockedDir.x * blastLength + right.x * ex + trueUp.x * ey;
            double py = this.posY + this.lockedDir.y * blastLength + right.y * ex + trueUp.y * ey;
            double pz = this.posZ + this.lockedDir.z * blastLength + right.z * ex + trueUp.z * ey;
            Particles.spawnParticle(ws, Types.SMOKE, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-535291880, 17, 10, 240});
         }

         int perimCount = 24;

         for(int i = 0; i < perimCount; ++i) {
            double t = ws.rand.nextDouble() * blastLength;
            double sr = t * EntityShadowRend.TAN_HALF_ANGLE;
            double a = ws.rand.nextDouble() * Math.PI * (double)2.0F;
            double ex = Math.cos(a) * sr;
            double ey = Math.sin(a) * sr;
            double px = this.posX + this.lockedDir.x * t + right.x * ex + trueUp.x * ey;
            double py = this.posY + this.lockedDir.y * t + right.y * ex + trueUp.y * ey;
            double pz = this.posZ + this.lockedDir.z * t + right.z * ex + trueUp.z * ey;
            Particles.spawnParticle(ws, Types.SMOKE, px, py, pz, 1, 0.12, 0.12, 0.12, (ws.rand.nextDouble() - (double)0.5F) * 0.1, (ws.rand.nextDouble() - (double)0.5F) * 0.1, (ws.rand.nextDouble() - (double)0.5F) * 0.1, new int[]{-1070254795, 20, 16, 240});
         }

         for(int i = 0; i < 8; ++i) {
            double t = ws.rand.nextDouble() * blastLength;
            double sr = t * EntityShadowRend.TAN_HALF_ANGLE * ws.rand.nextDouble();
            double a = ws.rand.nextDouble() * Math.PI * (double)2.0F;
            double ex = Math.cos(a) * sr;
            double ey = Math.sin(a) * sr;
            double px = this.posX + this.lockedDir.x * t + right.x * ex + trueUp.x * ey;
            double py = this.posY + this.lockedDir.y * t + right.y * ex + trueUp.y * ey;
            double pz = this.posZ + this.lockedDir.z * t + right.z * ex + trueUp.z * ey;
            Particles.spawnParticle(ws, Types.FALLING_DUST, px, py, pz, 1, 0.02, 0.02, 0.02, this.lockedDir.x * 0.6, this.lockedDir.y * 0.6, this.lockedDir.z * 0.6, new int[]{-16777216, 240, 8});
         }

         int tendrilCount = 8;

         for(int n = 0; n < tendrilCount; ++n) {
            double phaseStart = (double)this.totalLifetime * 0.12 + (double)n / (double)tendrilCount * Math.PI * (double)2.0F;
            double t = (ws.rand.nextDouble() * 0.9 + 0.05) * blastLength;
            double twist = phaseStart + t * 0.35;
            double sr = t * EntityShadowRend.TAN_HALF_ANGLE * (0.55 + ws.rand.nextDouble() * 0.35);
            double ex = Math.cos(twist) * sr;
            double ey = Math.sin(twist) * sr;
            double px = this.posX + this.lockedDir.x * t + right.x * ex + trueUp.x * ey;
            double py = this.posY + this.lockedDir.y * t + right.y * ex + trueUp.y * ey;
            double pz = this.posZ + this.lockedDir.z * t + right.z * ex + trueUp.z * ey;
            double tanx = -Math.sin(twist) * 0.12;
            double tany = Math.cos(twist) * 0.12;
            double vx = right.x * tanx + trueUp.x * tany;
            double vy = right.y * tanx + trueUp.y * tany;
            double vz = right.z * tanx + trueUp.z * tany;
            Particles.spawnParticle(ws, Types.SMOKE, px, py, pz, 1, 0.03, 0.03, 0.03, vx, vy, vz, new int[]{-535163105, 14, 18, 240});
         }

         if (pt % 4 == 0 && blastLength > (double)2.0F) {
            int rippleCount = 20;
            double rippleRadius = frontRadius * 1.05;

            for(int i = 0; i < rippleCount; ++i) {
               double a = (double)i / (double)rippleCount * Math.PI * (double)2.0F;
               double ex = Math.cos(a) * rippleRadius;
               double ey = Math.sin(a) * rippleRadius;
               double px = this.posX + this.lockedDir.x * blastLength + right.x * ex + trueUp.x * ey;
               double py = this.posY + this.lockedDir.y * blastLength + right.y * ex + trueUp.y * ey;
               double pz = this.posZ + this.lockedDir.z * blastLength + right.z * ex + trueUp.z * ey;
               double ovx = (right.x * ex + trueUp.x * ey) * 0.15;
               double ovy = (right.y * ex + trueUp.y * ey) * 0.15;
               double ovz = (right.z * ex + trueUp.z * ey) * 0.15;
               Particles.spawnParticle(ws, Types.SMOKE, px, py, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, ovx, ovy, ovz, new int[]{-1070254795, 18, 14, 240});
            }
         }

         if (pt % 3 == 0) {
            double px = this.posX + this.lockedDir.x * blastLength;
            double py = this.posY + this.lockedDir.y * blastLength;
            double pz = this.posZ + this.lockedDir.z * blastLength;
            Particles.spawnParticle(ws, Types.SMOKE, px, py, pz, 1, 0.2, 0.2, 0.2, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-14020822, 28, 10, 240});
            if (ws.rand.nextInt(8) == 0) {
               Particles.spawnParticle(ws, Types.SMOKE, px, py, pz, 1, 0.15, 0.15, 0.15, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-13631473, 24, 8, 240});
            }
         }

         if (pt % 2 == 0) {
            double tipX = this.posX + this.lockedDir.x * blastLength;
            double tipY = this.posY + this.lockedDir.y * blastLength;
            double tipZ = this.posZ + this.lockedDir.z * blastLength;
            BlockPos groundProbe = new BlockPos(tipX, tipY - (double)1.5F, tipZ);
            if (this.world.getBlockState(groundProbe).getMaterial().isSolid()) {
               for(int i = 0; i < 3; ++i) {
                  double jx = (ws.rand.nextDouble() - (double)0.5F) * frontRadius;
                  double jz = (ws.rand.nextDouble() - (double)0.5F) * frontRadius;
                  Particles.spawnParticle(ws, Types.SMOKE, tipX + jx, tipY - (double)1.0F + ws.rand.nextDouble() * 0.3, tipZ + jz, 1, 0.15, 0.1, 0.15, -this.lockedDir.x * 0.2, 0.08, -this.lockedDir.z * 0.2, new int[]{-1070254795, 22, 20, 240});
               }
            }
         }

      }

      private void spawnReleaseShockwave(WorldServer ws, Vec3d right, Vec3d trueUp) {
         for(int i = 0; i < 80; ++i) {
            double theta = ws.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.acos((double)2.0F * ws.rand.nextDouble() - (double)1.0F);
            double spd = 0.8 + ws.rand.nextDouble() * 0.9;
            double vx = Math.sin(phi) * Math.cos(theta) * spd;
            double vy = Math.cos(phi) * spd;
            double vz = Math.sin(phi) * Math.sin(theta) * spd;
            int c = i % 4 == 0 ? -14020822 : -14013910;
            Particles.spawnParticle(ws, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 22, 14, 240});
         }

         Particles.spawnParticle(ws, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-16119286, 85, 12, 240});
         Particles.spawnParticle(ws, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-14020822, 55, 10, 240});
         int ringCount = 32;

         for(int i = 0; i < ringCount; ++i) {
            double a = (double)i / (double)ringCount * Math.PI * (double)2.0F;
            double ex = Math.cos(a);
            double ey = Math.sin(a);
            Particles.spawnParticle(ws, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, right.x * ex * 1.2 + trueUp.x * ey * 1.2, right.y * ex * 1.2 + trueUp.y * ey * 1.2, right.z * ex * 1.2 + trueUp.z * ey * 1.2, new int[]{-535291880, 18, 16, 240});
         }

         Particles.spawnParticle(ws, Types.SONIC_BOOM, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-535291880, 30, 10, 240});
      }

      private void spawnImpactCloud(WorldServer ws, Vec3d right, Vec3d trueUp) {
         double tipX = this.posX + this.lockedDir.x * (double)48.0F;
         double tipY = this.posY + this.lockedDir.y * (double)48.0F;
         double tipZ = this.posZ + this.lockedDir.z * (double)48.0F;

         for(int i = 0; i < 60; ++i) {
            double theta = ws.rand.nextDouble() * Math.PI * (double)2.0F;
            double phi = Math.acos((double)2.0F * ws.rand.nextDouble() - (double)1.0F);
            double spd = 0.3 + ws.rand.nextDouble() * 0.6;
            double vx = Math.sin(phi) * Math.cos(theta) * spd;
            double vy = Math.cos(phi) * spd;
            double vz = Math.sin(phi) * Math.sin(theta) * spd;
            int c = i % 5 == 0 ? -14020822 : -14013910;
            Particles.spawnParticle(ws, Types.SMOKE, tipX, tipY, tipZ, 1, 0.1, 0.1, 0.1, vx, vy, vz, new int[]{c, 24, 18, 240});
         }

         Particles.spawnParticle(ws, Types.SMOKE, tipX, tipY, tipZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-16119286, 55, 12, 240});
         this.world.playSound((EntityPlayer)null, tipX, tipY, tipZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.1F, 0.5F);
      }

      private void spawnAftermathFX(WorldServer ws) {
         int pt = this.getPhaseTick();
         float fade = 1.0F - (float)pt / 30.0F;

         for(int i = 0; i < 6; ++i) {
            double theta = ws.rand.nextDouble() * Math.PI * (double)2.0F;
            double rr = ws.rand.nextDouble() * (double)3.0F;
            double px = this.posX + Math.cos(theta) * rr;
            double py = this.posY + ws.rand.nextDouble() * (double)2.0F;
            double pz = this.posZ + Math.sin(theta) * rr;
            Particles.spawnParticle(ws, Types.SMOKE, px, py, pz, 1, 0.15, 0.15, 0.15, (double)0.0F, 0.03, (double)0.0F, new int[]{-1070254795, 20, 24, 240});
         }

         int driftCount = Math.max(2, (int)(10.0F * fade));

         for(int i = 0; i < driftCount; ++i) {
            double t = ws.rand.nextDouble() * (double)48.0F;
            double sr = t * EntityShadowRend.TAN_HALF_ANGLE * ws.rand.nextDouble() * 0.8;
            double a = ws.rand.nextDouble() * Math.PI * (double)2.0F;
            Vec3d up = Math.abs(this.lockedDir.y) > 0.9 ? new Vec3d((double)1.0F, (double)0.0F, (double)0.0F) : new Vec3d((double)0.0F, (double)1.0F, (double)0.0F);
            Vec3d right = this.lockedDir.crossProduct(up).normalize();
            Vec3d trueUp = right.crossProduct(this.lockedDir).normalize();
            double ex = Math.cos(a) * sr;
            double ey = Math.sin(a) * sr;
            double px = this.posX + this.lockedDir.x * t + right.x * ex + trueUp.x * ey;
            double py = this.posY + this.lockedDir.y * t + right.y * ex + trueUp.y * ey;
            double pz = this.posZ + this.lockedDir.z * t + right.z * ex + trueUp.z * ey;
            Particles.spawnParticle(ws, Types.SMOKE, px, py, pz, 1, 0.08, 0.08, 0.08, (double)0.0F, 0.04, (double)0.0F, new int[]{-535163105, 16, 28, 240});
         }

         if (pt % 10 == 5) {
            Particles.spawnParticle(ws, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-16119286, (int)(30.0F * fade + 10.0F), 10, 240});
         }

      }

      protected void readEntityFromNBT(NBTTagCompound c) {
         this.totalLifetime = c.getInteger("dwLife");
         if (c.hasKey("dwShooter")) {
            try {
               this.shooterUUID = UUID.fromString(c.getString("dwShooter"));
            } catch (IllegalArgumentException var3) {
            }
         }

         if (c.hasKey("dwDirX")) {
            this.lockedDir = new Vec3d(c.getDouble("dwDirX"), c.getDouble("dwDirY"), c.getDouble("dwDirZ"));
         }

      }

      protected void writeEntityToNBT(NBTTagCompound c) {
         c.setInteger("dwLife", this.totalLifetime);
         if (this.shooterUUID != null) {
            c.setString("dwShooter", this.shooterUUID.toString());
         }

         if (this.lockedDir != null) {
            c.setDouble("dwDirX", this.lockedDir.x);
            c.setDouble("dwDirY", this.lockedDir.y);
            c.setDouble("dwDirZ", this.lockedDir.z);
         }

      }

      static {
         PHASE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         PHASE_TICK = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         SHOOTER_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();
      private static final int COOLDOWN_TICKS = 160;

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (entity.world.isRemote) {
            return false;
         } else if (power < 0.5F) {
            return false;
         } else {
            UUID uid = entity.getUniqueID();
            long now = entity.world.getTotalWorldTime();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 160L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               EntityCustom dw = new EntityCustom(entity.world, entity);
               entity.world.spawnEntity(dw);
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
   }

   @SideOnly(Side.CLIENT)
   public static class ShadowRendRenderer extends Render<EntityCustom> {
      private static final ResourceLocation BLANK = new ResourceLocation("inftsukaddon", "textures/entity/blue/maxblue_frame1.png");

      public ShadowRendRenderer(RenderManager rm) {
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
