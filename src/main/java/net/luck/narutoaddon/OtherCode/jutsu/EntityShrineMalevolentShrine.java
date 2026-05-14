
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
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
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.PlayerTracker;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemJutsu.JutsuEnum.Type;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityShrineMalevolentShrine extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 364;
   public static final int WINDUP_TICKS = 204;
   public static final int ACTIVE_TICKS = 1200;
   public static final float DOMAIN_RADIUS = 64.0F;
   public static final float SHRINE_MAX_HEALTH = 5000.0F;
   private static final float FLAT_DAMAGE_PER_HIT = 12.0F;
   private static final float MAX_HEALTH_DAMAGE_FRACTION = 0.005F;
   static final Set<UUID> WINDUP_PROTECTED = Collections.newSetFromMap(new ConcurrentHashMap());
   private static boolean serverHandlerRegistered = false;

   public EntityShrineMalevolentShrine(ElementsInfTsukAddon instance) {
      super(instance, 956);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "shrine_malevolent_shrine"), 364).name("inftsuk_shrine_malevolent_shrine").tracker(160, 1, true).build());
      registerServerHandler();
   }

   private static void registerServerHandler() {
      if (!serverHandlerRegistered) {
         MinecraftForge.EVENT_BUS.register(new ShrineDomainProtectionHandler());
         serverHandlerRegistered = true;
      }

   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, ShrineMalevolentShrineRenderer::new);
      MinecraftForge.EVENT_BUS.register(new ShrineDomainClientEffects());
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final DataParameter<Integer> OWNER_ID;
      private static final DataParameter<Boolean> SHRINE_VISIBLE;
      private static final DataParameter<Float> RADIUS;
      private static final DataParameter<Float> CAST_YAW;
      private static final DataParameter<Float> SHRINE_HEALTH;
      private static final float DOMAIN_SLASH_SCALE = 10.0F;
      private static final ResourceLocation CLEAVE_LOOP_SOUND;
      private UUID ownerUUID;
      private boolean lockSet;
      private double lockX;
      private double lockY;
      private double lockZ;
      private float lockYaw;
      private float lockPitch;

      public EntityCustom(World world) {
         super(world);
         this.lockSet = false;
         this.noClip = true;
         this.ignoreFrustumCheck = true;
         this.setSize(6.0F, 8.0F);
      }

      public EntityCustom(World world, EntityLivingBase owner) {
         this(world);
         this.ownerUUID = owner.getUniqueID();
         this.dataManager.set(OWNER_ID, owner.getEntityId());
         this.dataManager.set(CAST_YAW, owner.rotationYaw);
         this.setPosition(owner.posX, owner.posY, owner.posZ);
         this.rememberLock(owner);
      }

      protected void entityInit() {
         this.dataManager.register(OWNER_ID, -1);
         this.dataManager.register(SHRINE_VISIBLE, false);
         this.dataManager.register(RADIUS, 64.0F);
         this.dataManager.register(CAST_YAW, 0.0F);
         this.dataManager.register(SHRINE_HEALTH, 5000.0F);
      }

      public Type getJutsuType() {
         return Type.OTHER;
      }

      public boolean isShrineVisible() {
         return (Boolean)this.dataManager.get(SHRINE_VISIBLE);
      }

      public float getDomainRadius() {
         return (Float)this.dataManager.get(RADIUS);
      }

      public float getCastYaw() {
         return (Float)this.dataManager.get(CAST_YAW);
      }

      public float getShrineHealth() {
         return (Float)this.dataManager.get(SHRINE_HEALTH);
      }

      public float getShrineMaxHealth() {
         return 5000.0F;
      }

      public int getActiveAge() {
         return Math.max(0, this.ticksExisted - 204);
      }

      EntityLivingBase getOwnerEntity() {
         Entity byId = this.world.getEntityByID((Integer)this.dataManager.get(OWNER_ID));
         if (byId instanceof EntityLivingBase) {
            return (EntityLivingBase)byId;
         } else {
            if (this.ownerUUID != null) {
               EntityPlayer player = this.world.getPlayerEntityByUUID(this.ownerUUID);
               if (player != null) {
                  this.dataManager.set(OWNER_ID, player.getEntityId());
                  return player;
               }
            }

            return null;
         }
      }

      public void onUpdate() {
         super.onUpdate();
         if (!this.world.isBlockLoaded(new BlockPos(this))) {
            this.setDead();
         } else if (!this.world.isRemote) {
            EntityLivingBase owner = this.getOwnerEntity();
            if (owner != null && owner.isEntityAlive()) {
               if (this.ticksExisted <= 204) {
                  this.freezeAndProtect(owner);
                  if (this.ticksExisted == 204) {
                     this.spawnShrineBehind(owner);
                  }

               } else {
                  this.releaseWindupProtection();
                  int activeAge = this.getActiveAge();
                  if (activeAge > 1200) {
                     this.setDead();
                  } else {
                     if (activeAge % 4 == 0) {
                        this.applyDomainBarrage(owner);
                     }

                     if (activeAge % 2 == 0) {
                        this.spawnAmbientDomainSlashes(activeAge);
                     }

                  }
               }
            } else {
               this.setDead();
            }
         }
      }

      private void rememberLock(EntityLivingBase owner) {
         this.lockSet = true;
         this.lockX = owner.posX;
         this.lockY = owner.posY;
         this.lockZ = owner.posZ;
         this.lockYaw = owner.rotationYaw;
         this.lockPitch = owner.rotationPitch;
      }

      private void freezeAndProtect(EntityLivingBase owner) {
         if (!this.lockSet) {
            this.rememberLock(owner);
         }

         EntityShrineMalevolentShrine.WINDUP_PROTECTED.add(owner.getUniqueID());
         owner.motionX = (double)0.0F;
         owner.motionY = (double)0.0F;
         owner.motionZ = (double)0.0F;
         owner.fallDistance = 0.0F;
         owner.hurtResistantTime = Math.max(owner.hurtResistantTime, 20);
         owner.velocityChanged = true;
         owner.setPositionAndRotation(this.lockX, this.lockY, this.lockZ, this.lockYaw, this.lockPitch);
      }

      private void spawnShrineBehind(EntityLivingBase owner) {
         Vec3d forward = owner.getLookVec();
         forward = new Vec3d(forward.x, (double)0.0F, forward.z);
         if (forward.lengthSquared() < 1.0E-4) {
            double yawRad = Math.toRadians((double)owner.rotationYaw);
            forward = new Vec3d(-Math.sin(yawRad), (double)0.0F, Math.cos(yawRad));
         } else {
            forward = forward.normalize();
         }

         Vec3d shrinePos = (new Vec3d(owner.posX, owner.posY, owner.posZ)).subtract(forward.scale((double)5.0F));
         this.setPosition(shrinePos.x, shrinePos.y, shrinePos.z);
         this.dataManager.set(CAST_YAW, owner.rotationYaw);
         this.dataManager.set(SHRINE_VISIBLE, true);
         this.dataManager.set(SHRINE_HEALTH, 5000.0F);
         this.setSize(10.0F, 12.0F);
         this.releaseWindupProtection();
      }

      private void releaseWindupProtection() {
         if (this.ownerUUID != null) {
            EntityShrineMalevolentShrine.WINDUP_PROTECTED.remove(this.ownerUUID);
         }

      }

      private void applyDomainBarrage(EntityLivingBase owner) {
         float radius = this.getDomainRadius();
         double radiusSq = (double)(radius * radius);
         AxisAlignedBB area = new AxisAlignedBB(this.posX - (double)radius, this.posY - (double)radius, this.posZ - (double)radius, this.posX + (double)radius, this.posY + (double)radius, this.posZ + (double)radius);

         for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, area, (targetx) -> targetx != owner && targetx.isEntityAlive() && ItemJutsu.canTarget(targetx))) {
            double dx = target.posX - this.posX;
            double dy = target.posY + (double)target.height * (double)0.5F - this.posY;
            double dz = target.posZ - this.posZ;
            if (!(dx * dx + dy * dy + dz * dz > radiusSq)) {
               float damage = 12.0F + target.getMaxHealth() * 0.005F;
               if (!(target instanceof EntityPlayer)) {
                  damage = Math.min(damage, 28.0F);
               }

               target.hurtResistantTime = 0;
               target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, owner), damage);
               target.hurtResistantTime = 0;
               this.spawnDomainSlashes(target, false);
            }
         }

      }

      private void spawnAmbientDomainSlashes(int activeAge) {
         float radius = this.getDomainRadius();
         int count = 18 + this.rand.nextInt(11);

         for(int i = 0; i < count; ++i) {
            Vec3d offset = this.randomPointInDomain(radius);
            double x = this.posX + offset.x;
            double y = this.posY + (double)1.0F + offset.y;
            double z = this.posZ + offset.z;
            Vec3d motion = this.randomSlashMotion((double)0.75F, 1.65);
            this.spawnDomainSlashAt(x, y, z, true, motion.x, motion.y, motion.z);
            if ((activeAge + i) % 12 == 0) {
               this.playCleaveLoopAt(x, y, z, 1.45F, 0.9F + this.rand.nextFloat() * 0.18F);
            }
         }

         if (activeAge % 12 == 0) {
            this.playCleaveLoopAt(this.posX, this.posY + (double)2.0F, this.posZ, 2.6F, 0.82F + this.rand.nextFloat() * 0.12F);
         }

      }

      private Vec3d randomPointInDomain(float radius) {
         double x;
         double y;
         double z;
         do {
            x = (this.rand.nextDouble() * (double)2.0F - (double)1.0F) * (double)radius;
            y = (this.rand.nextDouble() * (double)2.0F - (double)1.0F) * (double)radius;
            z = (this.rand.nextDouble() * (double)2.0F - (double)1.0F) * (double)radius;
         } while(x * x + y * y + z * z > (double)(radius * radius));

         return new Vec3d(x, y, z);
      }

      private void spawnDomainSlashes(EntityLivingBase target, boolean ambient) {
         int count = 5 + this.rand.nextInt(3);

         for(int i = 0; i < count; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double radius = (double)target.width * (double)0.75F + 0.45 + this.rand.nextDouble() * 2.2;
            double x = target.posX + Math.cos(angle) * radius;
            double y = target.posY + (double)target.height * (0.15 + this.rand.nextDouble() * 0.85) + (this.rand.nextDouble() - (double)0.5F) * 1.2;
            double z = target.posZ + Math.sin(angle) * radius;
            double motionX = ambient ? (double)0.0F : (target.posX - x) * 0.11 + (this.rand.nextDouble() - (double)0.5F) * 0.28;
            double motionY = ambient ? (double)0.0F : (target.posY + (double)target.height * (double)0.5F - y) * 0.08 + (this.rand.nextDouble() - (double)0.5F) * 0.18;
            double motionZ = ambient ? (double)0.0F : (target.posZ - z) * 0.11 + (this.rand.nextDouble() - (double)0.5F) * 0.28;
            this.spawnDomainSlashAt(x, y, z, ambient, motionX, motionY, motionZ);
         }

      }

      private Vec3d randomSlashMotion(double minSpeed, double maxSpeed) {
         Vec3d direction;
         do {
            direction = new Vec3d(this.rand.nextDouble() * (double)2.0F - (double)1.0F, (this.rand.nextDouble() * (double)2.0F - (double)1.0F) * 0.55, this.rand.nextDouble() * (double)2.0F - (double)1.0F);
         } while(direction.lengthSquared() < 1.0E-4);

         double speed = minSpeed + this.rand.nextDouble() * (maxSpeed - minSpeed);
         return direction.normalize().scale(speed);
      }

      private void spawnDomainSlashAt(double x, double y, double z, boolean ambient, double motionX, double motionY, double motionZ) {
         EntityShrineDismantle.VisualSlashEntity visual = new EntityShrineDismantle.VisualSlashEntity(this.world);
         boolean star = this.rand.nextInt(ambient ? 5 : 4) == 0;
         visual.setSlashStyle(star ? 1 : 0);
         visual.setFrame(star ? this.rand.nextInt(6) : this.rand.nextInt(7));
         visual.setRoll(this.rand.nextFloat() * 360.0F);
         if (star) {
            visual.setMaxAge(ambient ? 6 + this.rand.nextInt(5) : 5 + this.rand.nextInt(4));
            float size = ambient ? (3.0F + this.rand.nextFloat() * 4.5F) * 10.0F : 0.9F + this.rand.nextFloat() * 1.1F;
            visual.setSizeData(size, size);
         } else {
            visual.setMaxAge(ambient ? 4 + this.rand.nextInt(4) : 4 + this.rand.nextInt(3));
            if (ambient) {
               float width = (10.0F + this.rand.nextFloat() * 18.0F) * 10.0F;
               int heightBand = this.rand.nextInt(5);
               float height;
               if (heightBand <= 1) {
                  height = (0.22F + this.rand.nextFloat() * 0.55F) * 10.0F;
               } else if (heightBand <= 3) {
                  height = (0.85F + this.rand.nextFloat() * 1.55F) * 10.0F;
               } else {
                  height = (2.2F + this.rand.nextFloat() * 3.6F) * 10.0F;
               }

               visual.setSizeData(width, height);
            } else {
               visual.setSizeData(4.6F + this.rand.nextFloat() * 2.8F, 1.05F + this.rand.nextFloat() * 0.46F);
            }
         }

         visual.setPosition(x, y, z);
         visual.motionX = motionX;
         visual.motionY = motionY;
         visual.motionZ = motionZ;
         this.world.spawnEntity(visual);
      }

      private void playSoundAtOwner(String name, SoundCategory category, float volume, float pitch) {
         EntityLivingBase owner = this.getOwnerEntity();
         if (owner != null) {
            SoundEvent sound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation(name));
            if (sound != null) {
               this.world.playSound((EntityPlayer)null, owner.posX, owner.posY + (double)owner.height * (double)0.5F, owner.posZ, sound, category, volume, pitch);
            }

         }
      }

      private void playSoundAtShrine(String name, SoundCategory category, float volume, float pitch) {
         SoundEvent sound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation(name));
         if (sound != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY + (double)2.0F, this.posZ, sound, category, volume, pitch);
         }

      }

      private void playCleaveLoopAt(double x, double y, double z, float volume, float pitch) {
         SoundEvent sound = (SoundEvent)SoundEvent.REGISTRY.getObject(CLEAVE_LOOP_SOUND);
         if (sound != null) {
            this.world.playSound((EntityPlayer)null, x, y, z, sound, SoundCategory.PLAYERS, volume, pitch);
         }

      }

      public boolean canBeCollidedWith() {
         return this.isShrineVisible() && !this.isDead;
      }

      public boolean canBeAttackedWithItem() {
         return this.isShrineVisible() && !this.isDead;
      }

      public float getCollisionBorderSize() {
         return this.isShrineVisible() ? 1.0F : 0.0F;
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         if (!this.world.isRemote && this.isShrineVisible() && !this.isDead && !(amount <= 0.0F)) {
            Entity trueSource = source.getTrueSource();
            EntityLivingBase owner = this.getOwnerEntity();
            if (trueSource == owner) {
               return false;
            } else if (!(trueSource instanceof EntityLivingBase) && !source.isExplosion()) {
               return false;
            } else {
               float newHealth = MathHelper.clamp(this.getShrineHealth() - amount, 0.0F, 5000.0F);
               this.dataManager.set(SHRINE_HEALTH, newHealth);
               if (newHealth <= 0.0F) {
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY + (double)2.0F, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0F, 0.7F);
                  this.setDead();
               }

               return true;
            }
         } else {
            return false;
         }
      }

      public void setDead() {
         this.releaseWindupProtection();
         super.setDead();
      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.dataManager.set(SHRINE_VISIBLE, compound.getBoolean("shrineVisible"));
         this.dataManager.set(RADIUS, compound.getFloat("domainRadius") <= 0.0F ? 64.0F : compound.getFloat("domainRadius"));
         this.dataManager.set(CAST_YAW, compound.getFloat("castYaw"));
         this.dataManager.set(SHRINE_HEALTH, compound.hasKey("shrineHealth") ? compound.getFloat("shrineHealth") : 5000.0F);
         if (compound.hasKey("ownerUUID")) {
            try {
               this.ownerUUID = UUID.fromString(compound.getString("ownerUUID"));
            } catch (IllegalArgumentException var3) {
            }
         }

         this.lockSet = compound.getBoolean("lockSet");
         this.lockX = compound.getDouble("lockX");
         this.lockY = compound.getDouble("lockY");
         this.lockZ = compound.getDouble("lockZ");
         this.lockYaw = compound.getFloat("lockYaw");
         this.lockPitch = compound.getFloat("lockPitch");
      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setBoolean("shrineVisible", this.isShrineVisible());
         compound.setFloat("domainRadius", this.getDomainRadius());
         compound.setFloat("castYaw", this.getCastYaw());
         compound.setFloat("shrineHealth", this.getShrineHealth());
         if (this.ownerUUID != null) {
            compound.setString("ownerUUID", this.ownerUUID.toString());
         }

         compound.setBoolean("lockSet", this.lockSet);
         compound.setDouble("lockX", this.lockX);
         compound.setDouble("lockY", this.lockY);
         compound.setDouble("lockZ", this.lockZ);
         compound.setFloat("lockYaw", this.lockYaw);
         compound.setFloat("lockPitch", this.lockPitch);
      }

      static {
         OWNER_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         SHRINE_VISIBLE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.BOOLEAN);
         RADIUS = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         CAST_YAW = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         SHRINE_HEALTH = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         CLEAVE_LOOP_SOUND = new ResourceLocation("inftsukaddon:shrine_cleave_loop");
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (!entity.world.isRemote && entity instanceof EntityPlayer && PlayerTracker.isNinja((EntityPlayer)entity)) {
            EntityCustom domain = new EntityCustom(entity.world, entity);
            entity.world.spawnEntity(domain);
            return true;
         } else {
            return false;
         }
      }

      public float getBasePower() {
         return 1.0F;
      }

      public float getPowerupDelay() {
         return 0.0F;
      }

      public float getMaxPower() {
         return 1.0F;
      }
   }
}
