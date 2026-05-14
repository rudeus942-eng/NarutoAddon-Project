
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemJutsu.JutsuEnum.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityShrineDismantle extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 360;
   public static final int VISUAL_ENTITYID = 361;

   public EntityShrineDismantle(ElementsInfTsukAddon instance) {
      super(instance, 953);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "shrine_dismantle"), 360).name("inftsuk_shrine_dismantle").tracker(64, 1, true).build());
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(VisualSlashEntity.class).id(new ResourceLocation("inftsukaddon", "shrine_dismantle_visual"), 361).name("inftsuk_shrine_dismantle_visual").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, ShrineInvisibleWaveRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(VisualSlashEntity.class, ShrineDismantleRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable implements ItemJutsu.IJutsu {
      private static final DataParameter<Float> VISUAL_SCALE;
      private static final DataParameter<Float> VISUAL_DENSITY;
      private int lifetime = 0;
      private int maxLifetime = 18;
      private float damagePerTick = 2.4F;
      private float waveRadius = 1.0F;
      private float waveLength = 3.2F;
      private float chargePower = 1.0F;
      private EntityLivingBase casterEntity;
      private UUID casterUUID;
      private final Map<Integer, Long> lastHitTickByEntity = new HashMap();

      public EntityCustom(World world) {
         super(world);
         this.setSize(1.4F, 1.0F);
      }

      public EntityCustom(World world, EntityLivingBase thrower, float power) {
         super(world, thrower);
         this.casterEntity = thrower;
         this.casterUUID = thrower.getUniqueID();
         this.configureFromPower(power);
      }

      public Type getJutsuType() {
         return Type.OTHER;
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(VISUAL_SCALE, 1.0F);
         this.dataManager.register(VISUAL_DENSITY, 1.0F);
      }

      public void configureFromPower(float power) {
         this.chargePower = power;
         this.damagePerTick = (4.0F + power * 1.2F) * 1.6F;
         this.waveRadius = 0.9F + power * 0.16F;
         this.waveLength = 3.4F + power * 0.32F;
         this.maxLifetime = 28 + MathHelper.floor(power * 3.0F);
         float visualScale = 0.9F + power * 0.22F;
         this.dataManager.set(VISUAL_SCALE, visualScale);
         this.dataManager.set(VISUAL_DENSITY, 0.8F + power * 0.08F);
         this.setSize(Math.max(1.4F, this.waveRadius * 1.9F), Math.max(1.0F, this.waveRadius * 1.15F));
      }

      public float getVisualScale() {
         return (Float)this.dataManager.get(VISUAL_SCALE);
      }

      public float getVisualDensity() {
         return (Float)this.dataManager.get(VISUAL_DENSITY);
      }

      public int getMaxLifetime() {
         return this.maxLifetime;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
               this.spawnTerminationBurst(this.posX, this.posY + 0.1, this.posZ);
               this.setDead();
            }

         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.casterEntity == null && this.casterUUID != null) {
            Entity resolved = this.world.getPlayerEntityByUUID(this.casterUUID);
            if (resolved instanceof EntityLivingBase) {
               this.casterEntity = (EntityLivingBase)resolved;
            }
         }

         if (this.lifetime <= this.maxLifetime && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               if (this.casterEntity == null || !this.casterEntity.isEntityAlive()) {
                  this.setDead();
                  return;
               }

               this.applyWaveDamage();
               this.spawnVisualBursts();
               if (this.lifetime == 1) {
                  this.playCastSound();
               } else if (this.lifetime % 4 == 0) {
                  this.playFlightSound();
               }
            }

         } else {
            this.setDead();
         }
      }

      private void applyWaveDamage() {
         Vec3d dir = new Vec3d(this.motionX, this.motionY, this.motionZ);
         if (!(dir.lengthSquared() < 1.0E-4)) {
            Vec3d direction = dir.normalize();
            Vec3d start = new Vec3d(this.posX, this.posY + (double)this.height * (double)0.5F, this.posZ);
            Vec3d end = start.add(direction.scale((double)this.waveLength));
            AxisAlignedBB hitBox = this.getEntityBoundingBox().grow((double)this.waveRadius).expand(direction.x * (double)this.waveLength, direction.y * (double)this.waveLength, direction.z * (double)this.waveLength);
            long now = this.world.getTotalWorldTime();

            for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, hitBox, (e) -> e != this.casterEntity && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
               if (this.isTargetInsideWave(target, start, end)) {
                  Long lastHitTick = (Long)this.lastHitTickByEntity.get(target.getEntityId());
                  if (lastHitTick == null || lastHitTick != now) {
                     this.lastHitTickByEntity.put(target.getEntityId(), now);
                     float dmg = this.damagePerTick;
                     if (!(target instanceof EntityPlayer)) {
                        dmg = Math.min(dmg, 20.0F);
                     }

                     target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.casterEntity), dmg);
                     target.hurtResistantTime = 0;
                  }
               }
            }

         }
      }

      private boolean isTargetInsideWave(EntityLivingBase target, Vec3d start, Vec3d end) {
         Vec3d targetCenter = new Vec3d(target.posX, target.posY + (double)target.height * (double)0.5F, target.posZ);
         Vec3d segment = end.subtract(start);
         double segmentLengthSq = segment.lengthSquared();
         if (segmentLengthSq < 1.0E-4) {
            return false;
         } else {
            double progress = targetCenter.subtract(start).dotProduct(segment) / segmentLengthSq;
            progress = MathHelper.clamp(progress, -0.1, 1.1);
            Vec3d closest = start.add(segment.scale(progress));
            double allowedRadius = (double)this.waveRadius + (double)target.width * 0.6;
            return targetCenter.squareDistanceTo(closest) <= allowedRadius * allowedRadius;
         }
      }

      private void spawnVisualBursts() {
         Vec3d forward = new Vec3d(this.motionX, this.motionY, this.motionZ);
         if (!(forward.lengthSquared() < 1.0E-4)) {
            forward = forward.normalize();
            Vec3d side = forward.crossProduct(new Vec3d((double)0.0F, (double)1.0F, (double)0.0F));
            if (side.lengthSquared() < 1.0E-4) {
               side = new Vec3d((double)1.0F, (double)0.0F, (double)0.0F);
            } else {
               side = side.normalize();
            }

            Vec3d up = side.crossProduct(forward).normalize();
            int count = 4 + MathHelper.floor(this.getVisualDensity() * 3.0F);

            for(int i = 0; i < count; ++i) {
               boolean useStar = this.rand.nextInt(3) == 0;
               int frame = useStar ? this.rand.nextInt(6) : this.rand.nextInt(7);
               float width = this.getVisualScale() * (useStar ? 0.9F + this.rand.nextFloat() * 0.8F : 2.0F + this.rand.nextFloat() * 2.4F);
               float height = this.getVisualScale() * (useStar ? 0.9F + this.rand.nextFloat() * 0.8F : 0.18F + this.rand.nextFloat() * 0.3F);
               Vec3d offset = side.scale((this.rand.nextDouble() - (double)0.5F) * (double)this.waveRadius * 2.4).add(up.scale((this.rand.nextDouble() - 0.35) * (double)this.waveRadius * 1.3)).add(forward.scale(this.rand.nextDouble() * (double)this.waveLength));
               VisualSlashEntity visual = new VisualSlashEntity(this.world);
               visual.setSlashStyle(useStar ? 1 : 0);
               visual.setFrame(frame);
               visual.setSizeData(width, height);
               visual.setRoll(this.rand.nextFloat() * 360.0F);
               visual.setMaxAge(5 + this.rand.nextInt(4));
               visual.setPosition(this.posX + offset.x, this.posY + 0.2 + offset.y, this.posZ + offset.z);
               visual.motionX = forward.x * 0.025 + side.x * (this.rand.nextDouble() - (double)0.5F) * 0.025;
               visual.motionY = forward.y * 0.025 + up.y * (this.rand.nextDouble() - (double)0.5F) * 0.015;
               visual.motionZ = forward.z * 0.025 + side.z * (this.rand.nextDouble() - (double)0.5F) * 0.025;
               this.world.spawnEntity(visual);
            }

         }
      }

      private void spawnTerminationBurst(double x, double y, double z) {
         this.world.playSound((EntityPlayer)null, x, y, z, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.85F, 0.75F + this.rand.nextFloat() * 0.1F);
      }

      private void playCastSound() {
         SoundEvent shrineCast = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("inftsukaddon:shrine_dismantle"));
         if (shrineCast != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, shrineCast, SoundCategory.PLAYERS, 1.0F, 1.0F);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 0.55F + this.rand.nextFloat() * 0.15F);
         SoundEvent slice = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
         if (slice != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, slice, SoundCategory.PLAYERS, 0.7F, 1.45F + this.rand.nextFloat() * 0.2F);
         }

      }

      private void playFlightSound() {
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.3F, 0.85F + this.rand.nextFloat() * 0.2F);
      }

      protected float getGravityVelocity() {
         return 0.0F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("sdLife", this.lifetime);
         compound.setInteger("sdMaxLife", this.maxLifetime);
         compound.setFloat("sdDamage", this.damagePerTick);
         compound.setFloat("sdRadius", this.waveRadius);
         compound.setFloat("sdLength", this.waveLength);
         compound.setFloat("sdPower", this.chargePower);
         if (this.casterUUID != null) {
            compound.setString("sdCaster", this.casterUUID.toString());
         }

      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("sdLife");
         this.maxLifetime = compound.getInteger("sdMaxLife");
         this.damagePerTick = compound.getFloat("sdDamage");
         this.waveRadius = compound.getFloat("sdRadius");
         this.waveLength = compound.getFloat("sdLength");
         this.chargePower = compound.getFloat("sdPower");
         this.configureFromPower(this.chargePower);
         if (compound.hasKey("sdCaster")) {
            try {
               this.casterUUID = UUID.fromString(compound.getString("sdCaster"));
            } catch (IllegalArgumentException var3) {
            }
         }

      }

      static {
         VISUAL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         VISUAL_DENSITY = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   public static class VisualSlashEntity extends Entity {
      private static final DataParameter<Integer> SLASH_STYLE;
      private static final DataParameter<Integer> FRAME;
      private static final DataParameter<Float> HALF_WIDTH;
      private static final DataParameter<Float> HALF_HEIGHT;
      private static final DataParameter<Float> ROLL;
      private static final DataParameter<Integer> MAX_AGE;

      public VisualSlashEntity(World world) {
         super(world);
         this.noClip = true;
         this.ignoreFrustumCheck = true;
         this.setSize(0.1F, 0.1F);
      }

      public boolean isInRangeToRenderDist(double distance) {
         return distance < (double)262144.0F;
      }

      protected void entityInit() {
         this.dataManager.register(SLASH_STYLE, 0);
         this.dataManager.register(FRAME, 0);
         this.dataManager.register(HALF_WIDTH, 2.0F);
         this.dataManager.register(HALF_HEIGHT, 0.3F);
         this.dataManager.register(ROLL, 0.0F);
         this.dataManager.register(MAX_AGE, 5);
      }

      public void setSlashStyle(int style) {
         this.dataManager.set(SLASH_STYLE, style);
      }

      public int getSlashStyle() {
         return (Integer)this.dataManager.get(SLASH_STYLE);
      }

      public void setFrame(int frame) {
         this.dataManager.set(FRAME, frame);
      }

      public int getFrame() {
         return (Integer)this.dataManager.get(FRAME);
      }

      public void setSizeData(float halfWidth, float halfHeight) {
         this.dataManager.set(HALF_WIDTH, halfWidth);
         this.dataManager.set(HALF_HEIGHT, halfHeight);
      }

      public float getHalfWidth() {
         return (Float)this.dataManager.get(HALF_WIDTH);
      }

      public float getHalfHeight() {
         return (Float)this.dataManager.get(HALF_HEIGHT);
      }

      public void setRoll(float roll) {
         this.dataManager.set(ROLL, roll);
      }

      public float getRoll() {
         return (Float)this.dataManager.get(ROLL);
      }

      public void setMaxAge(int maxAge) {
         this.dataManager.set(MAX_AGE, maxAge);
      }

      public int getMaxAge() {
         return (Integer)this.dataManager.get(MAX_AGE);
      }

      public void onUpdate() {
         super.onUpdate();
         this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
         this.setRoll(this.getRoll() + 1.5F);
         this.motionX *= 0.78;
         this.motionY *= 0.78;
         this.motionZ *= 0.78;
         if (this.ticksExisted >= this.getMaxAge() || !this.world.isBlockLoaded(new BlockPos(this))) {
            this.setDead();
         }

      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.setSlashStyle(compound.getInteger("sStyle"));
         this.setFrame(compound.getInteger("sFrame"));
         this.setSizeData(compound.getFloat("sW"), compound.getFloat("sH"));
         this.setRoll(compound.getFloat("sRoll"));
         this.setMaxAge(compound.getInteger("sAge"));
      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("sStyle", this.getSlashStyle());
         compound.setInteger("sFrame", this.getFrame());
         compound.setFloat("sW", this.getHalfWidth());
         compound.setFloat("sH", this.getHalfHeight());
         compound.setFloat("sRoll", this.getRoll());
         compound.setInteger("sAge", this.getMaxAge());
      }

      static {
         SLASH_STYLE = EntityDataManager.createKey(VisualSlashEntity.class, DataSerializers.VARINT);
         FRAME = EntityDataManager.createKey(VisualSlashEntity.class, DataSerializers.VARINT);
         HALF_WIDTH = EntityDataManager.createKey(VisualSlashEntity.class, DataSerializers.FLOAT);
         HALF_HEIGHT = EntityDataManager.createKey(VisualSlashEntity.class, DataSerializers.FLOAT);
         ROLL = EntityDataManager.createKey(VisualSlashEntity.class, DataSerializers.FLOAT);
         MAX_AGE = EntityDataManager.createKey(VisualSlashEntity.class, DataSerializers.VARINT);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (!(power < this.getBasePower()) && !entity.world.isRemote) {
            long now = entity.world.getTotalWorldTime();
            UUID uid = entity.getUniqueID();
            Long lastUse = (Long)cooldownMap.get(uid);
            if (lastUse != null && now - lastUse < 25L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               Vec3d look = entity.getLookVec();
               Vec3d spawn = entity.getPositionEyes(1.0F).add(look.scale(1.4));
               float clampedPower = MathHelper.clamp(power, this.getBasePower(), this.getMaxPower());
               EntityCustom wave = new EntityCustom(entity.world, entity, clampedPower);
               wave.setPosition(spawn.x, spawn.y - 0.2, spawn.z);
               double speed = 0.72 + (double)clampedPower * 0.07;
               wave.motionX = look.x * speed;
               wave.motionY = look.y * speed;
               wave.motionZ = look.z * speed;
               wave.rotationYaw = entity.rotationYaw;
               wave.rotationPitch = entity.rotationPitch;
               entity.world.spawnEntity(wave);
               return true;
            }
         } else {
            return false;
         }
      }

      public float getBasePower() {
         return 1.0F;
      }

      public float getPowerupDelay() {
         return 18.0F;
      }

      public float getMaxPower() {
         return 6.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         super.onUsingTick(stack, player, power);
      }
   }
}
