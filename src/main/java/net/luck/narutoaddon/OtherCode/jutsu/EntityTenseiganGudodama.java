
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityTenseiganGudodama extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 333;
   public static final int PHASE_ORBIT = 0;
   public static final int PHASE_FIRE = 1;
   public static final int ORBIT_BASE_TICKS = 14;
   public static final int ORBIT_STAGGER_PER_ORB = 3;
   public static final int FIRE_MAX_TICKS = 60;
   public static final double ORBIT_RADIUS = 1.6;
   public static final double HOMING_SPEED = (double)1.5F;
   public static final double HOMING_LERP = 0.22;
   public static final float DAMAGE = 40.0F;
   public static final double DETECT_RANGE = (double)32.0F;
   public static final double HIT_RADIUS = 1.4;

   public EntityTenseiganGudodama(ElementsInfTsukAddon instance) {
      super(instance, 960);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "tenseigan_gudodama"), 333).name("inftsuk_tenseigan_gudodama").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, GudodamaRenderer::new);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final DataParameter<Integer> PHASE;
      private static final DataParameter<Integer> PHASE_TICK;
      private static final DataParameter<Integer> ORB_INDEX;
      private static final DataParameter<String> SHOOTER_ID;
      private EntityLivingBase shooter;
      private UUID shooterUUID;
      private EntityLivingBase target;
      private int totalLifetime;
      private double lerpX;
      private double lerpY;
      private double lerpZ;
      private int lerpSteps;

      public EntityCustom(World world) {
         super(world);
         this.totalLifetime = 0;
         this.setSize(0.3F, 0.3F);
         this.noClip = true;
         this.isImmuneToFire = true;
      }

      public EntityCustom(World world, EntityLivingBase shooter, int index) {
         this(world);
         this.shooter = shooter;
         this.shooterUUID = shooter.getUniqueID();
         this.dataManager.set(SHOOTER_ID, this.shooterUUID.toString());
         this.dataManager.set(ORB_INDEX, index);
      }

      public Type getJutsuType() {
         return Type.YOTON;
      }

      protected void entityInit() {
         this.dataManager.register(PHASE, 0);
         this.dataManager.register(PHASE_TICK, 0);
         this.dataManager.register(ORB_INDEX, 0);
         this.dataManager.register(SHOOTER_ID, "");
      }

      public int getPhase() {
         return (Integer)this.dataManager.get(PHASE);
      }

      public int getPhaseTick() {
         return (Integer)this.dataManager.get(PHASE_TICK);
      }

      public int getOrbIndex() {
         return (Integer)this.dataManager.get(ORB_INDEX);
      }

      @SideOnly(Side.CLIENT)
      public AxisAlignedBB getRenderBoundingBox() {
         return super.getRenderBoundingBox().grow((double)8.0F);
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

      @SideOnly(Side.CLIENT)
      public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
         this.lerpX = x;
         this.lerpY = y;
         this.lerpZ = z;
         this.lerpSteps = posRotationIncrements;
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
                     } catch (IllegalArgumentException var14) {
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
               int idx = this.getOrbIndex();
               int phase = this.getPhase();
               int pt = this.getPhaseTick();
               if (phase == 0) {
                  double t = (double)this.totalLifetime * 0.12 + (double)idx / (double)9.0F * Math.PI * (double)2.0F;
                  double ox = this.shooter.posX + Math.cos(t) * 1.6;
                  double oz = this.shooter.posZ + Math.sin(t) * 1.6;
                  double oy = this.shooter.posY + 1.1 + Math.sin(t * (double)1.5F) * (double)0.25F;
                  this.setPosition(ox, oy, oz);
                  this.motionX = this.motionY = this.motionZ = (double)0.0F;
                  if (!this.world.isRemote) {
                     this.emitOrbitFX();
                     int orbitLife = 14 + idx * 3;
                     if (pt >= orbitLife) {
                        this.target = this.findNearestTarget();
                        this.dataManager.set(PHASE, 1);
                        this.dataManager.set(PHASE_TICK, 0);
                        if (this.target != null) {
                           Vec3d dir = (new Vec3d(this.target.posX - this.posX, this.target.posY + (double)this.target.height * (double)0.5F - this.posY, this.target.posZ - this.posZ)).normalize();
                           this.motionX = dir.x * (double)1.5F;
                           this.motionY = dir.y * (double)1.5F;
                           this.motionZ = dir.z * (double)1.5F;
                        } else {
                           Vec3d look = this.shooter.getLookVec();
                           this.motionX = look.x * (double)1.5F;
                           this.motionY = look.y * (double)1.5F;
                           this.motionZ = look.z * (double)1.5F;
                        }

                        this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SHULKER_BULLET_HIT, SoundCategory.PLAYERS, 0.6F, 1.3F + (float)idx * 0.03F);
                     } else {
                        this.dataManager.set(PHASE_TICK, pt + 1);
                     }
                  }
               } else if (phase == 1) {
                  if (!this.world.isRemote) {
                     if (this.target != null && this.target.isEntityAlive()) {
                        Vec3d toTarget = (new Vec3d(this.target.posX - this.posX, this.target.posY + (double)this.target.height * (double)0.5F - this.posY, this.target.posZ - this.posZ)).normalize();
                        this.motionX = this.motionX * 0.78 + toTarget.x * (double)1.5F * 0.22;
                        this.motionY = this.motionY * 0.78 + toTarget.y * (double)1.5F * 0.22;
                        this.motionZ = this.motionZ * 0.78 + toTarget.z * (double)1.5F * 0.22;
                     }

                     this.checkHit();
                     this.emitTrailFX();
                     this.dataManager.set(PHASE_TICK, pt + 1);
                     if (pt >= 60) {
                        this.detonate();
                        this.setDead();
                        return;
                     }

                     this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
                  } else if (this.lerpSteps > 0) {
                     double dx = (this.lerpX - this.posX) / (double)this.lerpSteps;
                     double dy = (this.lerpY - this.posY) / (double)this.lerpSteps;
                     double dz = (this.lerpZ - this.posZ) / (double)this.lerpSteps;
                     --this.lerpSteps;
                     this.setPosition(this.posX + dx, this.posY + dy, this.posZ + dz);
                  }
               }

            } else {
               this.setDead();
            }
         }
      }

      private EntityLivingBase findNearestTarget() {
         List<EntityLivingBase> candidates = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)32.0F, this.posY - (double)32.0F, this.posZ - (double)32.0F, this.posX + (double)32.0F, this.posY + (double)32.0F, this.posZ + (double)32.0F), (e) -> e != this.shooter && e.isEntityAlive() && ItemJutsu.canTarget(e));
         EntityLivingBase best = null;
         double bestDist = Double.MAX_VALUE;

         for(EntityLivingBase c : candidates) {
            double d = c.getDistanceSq(this.posX, this.posY, this.posZ);
            if (d < bestDist) {
               bestDist = d;
               best = c;
            }
         }

         return best;
      }

      private void checkHit() {
         List<EntityLivingBase> hits = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - 1.4, this.posY - 1.4, this.posZ - 1.4, this.posX + 1.4, this.posY + 1.4, this.posZ + 1.4), (e) -> e != this.shooter && e.isEntityAlive() && ItemJutsu.canTarget(e));
         if (!hits.isEmpty()) {
            for(EntityLivingBase h : hits) {
               float npcDmg = 40.0F;
               float npcTrueDmg = 16.0F;
               if (!(h instanceof EntityPlayer)) {
                  npcDmg = Math.min(npcDmg, 100.0F);
                  npcTrueDmg = Math.min(npcTrueDmg, 100.0F);
               }

               h.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooter), npcDmg);
               h.hurtResistantTime = 0;
               h.attackEntityFrom(DamageSource.OUT_OF_WORLD, npcTrueDmg);
               Vec3d kb = (new Vec3d(h.posX - this.posX, 0.1, h.posZ - this.posZ)).normalize();
               h.motionX += kb.x * 0.6;
               h.motionY += (double)0.25F;
               h.motionZ += kb.z * 0.6;
               h.velocityChanged = true;
            }

            this.detonate();
            this.setDead();
         }

      }

      private void emitOrbitFX() {
         if (this.world instanceof WorldServer) {
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 2, 0.04, 0.04, 0.04, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-14675917, 10, 8, 240});
            if (this.world.rand.nextInt(3) == 0) {
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, 0.1, 0.1, 0.1, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-1062278145, 8, 10, 240});
            }

         }
      }

      private void emitTrailFX() {
         if (this.world instanceof WorldServer) {
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 3, 0.08, 0.08, 0.08, -this.motionX * 0.1, -this.motionY * 0.1, -this.motionZ * 0.1, new int[]{-15726560, 12, 10, 240});
            if (this.world.rand.nextInt(2) == 0) {
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, 0.06, 0.06, 0.06, -this.motionX * 0.15, -this.motionY * 0.15, -this.motionZ * 0.15, new int[]{-525407233, 9, 12, 240});
            }

         }
      }

      private void detonate() {
         if (this.world instanceof WorldServer) {
            for(int i = 0; i < 16; ++i) {
               double theta = this.world.rand.nextDouble() * Math.PI * (double)2.0F;
               double phi = Math.acos((double)2.0F * this.world.rand.nextDouble() - (double)1.0F);
               double spd = 0.3 + this.world.rand.nextDouble() * 0.4;
               double vx = Math.sin(phi) * Math.cos(theta) * spd;
               double vy = Math.cos(phi) * spd;
               double vz = Math.sin(phi) * Math.sin(theta) * spd;
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{-14677709, 18, 14, 240});
            }

            for(int i = 0; i < 10; ++i) {
               double theta = this.world.rand.nextDouble() * Math.PI * (double)2.0F;
               double phi = Math.acos((double)2.0F * this.world.rand.nextDouble() - (double)1.0F);
               double spd = 0.4 + this.world.rand.nextDouble() * 0.3;
               double vx = Math.sin(phi) * Math.cos(theta) * spd;
               double vy = Math.cos(phi) * spd;
               double vz = Math.sin(phi) * Math.sin(theta) * spd;
               int c = i % 2 == 0 ? -5313537 : -14261;
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, vx, vy, vz, new int[]{c, 14, 12, 240});
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.6F, 1.6F);
         }
      }

      protected void readEntityFromNBT(NBTTagCompound c) {
         this.totalLifetime = c.getInteger("gdLife");
         if (c.hasKey("gdShooter")) {
            try {
               this.shooterUUID = UUID.fromString(c.getString("gdShooter"));
            } catch (IllegalArgumentException var3) {
            }
         }

      }

      protected void writeEntityToNBT(NBTTagCompound c) {
         c.setInteger("gdLife", this.totalLifetime);
         if (this.shooterUUID != null) {
            c.setString("gdShooter", this.shooterUUID.toString());
         }

      }

      static {
         PHASE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         PHASE_TICK = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         ORB_INDEX = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         SHOOTER_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();
      private static final int COOLDOWN_TICKS = 120;

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (entity.world.isRemote) {
            return false;
         } else if (power < 0.3F) {
            return false;
         } else {
            UUID uid = entity.getUniqueID();
            long now = entity.world.getTotalWorldTime();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 120L) {
               return false;
            } else {
               cooldownMap.put(uid, now);

               for(int i = 0; i < 9; ++i) {
                  EntityCustom orb = new EntityCustom(entity.world, entity, i);
                  double angle = (double)i / (double)9.0F * Math.PI * (double)2.0F;
                  orb.setPosition(entity.posX + Math.cos(angle) * 1.6, entity.posY + 1.1, entity.posZ + Math.sin(angle) * 1.6);
                  entity.world.spawnEntity(orb);
               }

               entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_SHULKER_SHOOT, SoundCategory.PLAYERS, 1.0F, 0.8F);
               return true;
            }
         }
      }

      public float getBasePower() {
         return 0.3F;
      }

      public float getPowerupDelay() {
         return 18.0F;
      }

      public float getMaxPower() {
         return 10.0F;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class GudodamaRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEX = new ResourceLocation("inftsukaddon", "textures/entity/tenseigan/truthseekerball.png");
      private final ModelTruthSeekerBall model = new ModelTruthSeekerBall();

      public GudodamaRenderer(RenderManager rm) {
         super(rm);
         this.shadowSize = 0.0F;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         float scale = entity.getPhase() == 1 ? 0.95F : 0.75F;
         this.bindEntityTexture(entity);
         GlStateManager.pushMatrix();
         GlStateManager.disableCull();
         GlStateManager.translate(x, y + (double)(0.125F * scale), z);
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.rotate(((float)entity.ticksExisted + partialTicks) * 90.0F, 1.0F, 1.0F, 0.0F);
         GlStateManager.disableLighting();
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEX;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelTruthSeekerBall extends ModelBase {
      private final ModelRenderer bbMain;

      public ModelTruthSeekerBall() {
         this.textureWidth = 16;
         this.textureHeight = 16;
         this.bbMain = new ModelRenderer(this);
         this.bbMain.setRotationPoint(0.0F, 0.0F, 0.0F);
         float[] yaws = new float[]{0.0F, 0.3927F, 0.7854F, 1.1781F, -0.3927F, -0.7854F, -1.1781F, -1.5708F};

         for(float yaw : yaws) {
            this.buildHexGroup(this.bbMain, yaw);
         }

      }

      private void buildHexGroup(ModelRenderer parent, float yaw) {
         ModelRenderer hex = new ModelRenderer(this);
         hex.setRotationPoint(0.0F, 0.0F, 0.0F);
         parent.addChild(hex);
         setRotationAngle(hex, 0.0F, yaw, 0.0F);
         hex.cubeList.add(new ModelBox(hex, 0, 0, -0.5027F, -2.5F, -0.5F, 1, 5, 1, 0.0F, false));
         hex.cubeList.add(new ModelBox(hex, 4, 0, -2.5F, -0.5027F, -0.5F, 5, 1, 1, 0.0F, false));
         this.addRotatedSub(hex, 0.3927F, true);
         this.addRotatedSub(hex, -0.3927F, true);
         this.addRotatedSub(hex, 0.7854F, false);
         this.addRotatedSub(hex, -0.7854F, false);
      }

      private void addRotatedSub(ModelRenderer parent, float zRoll, boolean includeHorizontalBar) {
         ModelRenderer sub = new ModelRenderer(this);
         sub.setRotationPoint(0.0F, 0.0F, 0.0F);
         parent.addChild(sub);
         setRotationAngle(sub, 0.0F, 0.0F, zRoll);
         if (includeHorizontalBar) {
            sub.cubeList.add(new ModelBox(sub, 4, 0, -2.5F, -0.5027F, -0.5F, 5, 1, 1, 0.0F, false));
         }

         sub.cubeList.add(new ModelBox(sub, 0, 0, -0.5027F, -2.5F, -0.5F, 1, 5, 1, 0.0F, false));
      }

      public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         this.bbMain.render(scale);
      }

      private static void setRotationAngle(ModelRenderer mr, float x, float y, float z) {
         mr.rotateAngleX = x;
         mr.rotateAngleY = y;
         mr.rotateAngleZ = z;
      }
   }
}
