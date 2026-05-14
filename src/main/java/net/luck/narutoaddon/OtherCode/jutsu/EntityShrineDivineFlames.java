
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Chakra;
import net.narutomod.Particles;
import net.narutomod.Particles.Types;
import net.narutomod.PlayerTracker;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemJutsu.JutsuEnum.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityShrineDivineFlames extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 363;
   public static final float MIN_CHARGE_POWER = 0.1F;
   public static final float MAX_CHARGE_POWER = 10.0F;
   public static final int FULL_CHARGE_TICKS = 90;
   public static final int START_ANIMATION_TICKS = 58;
   private static final double CHARGE_DRAIN_PER_TICK_BASE = 0.2;
   private static final double CHARGE_DRAIN_PER_TICK_SCALE = 0.08;
   private static final ResourceLocation CAST_SOUND_ID = new ResourceLocation("inftsukaddon:shrine_divineflames_cast");

   public EntityShrineDivineFlames(ElementsInfTsukAddon instance) {
      super(instance, 955);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "shrine_divine_flames"), 363).name("inftsuk_shrine_divine_flames").tracker(128, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, ShrineDivineFlamesRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable implements ItemJutsu.IJutsu {
      private static final DataParameter<Integer> PHASE;
      private static final DataParameter<Float> CHARGE_POWER;
      private static final DataParameter<Integer> CHARGE_TICKS;
      private static final DataParameter<Float> RENDER_SCALE;
      private static final DataParameter<Integer> OWNER_ID;
      private static final int PHASE_CHARGING = 0;
      private static final int PHASE_LAUNCHED = 1;
      private int launchedAge;
      private float normalDamage;
      private float impactRadius;
      private UUID ownerUUID;

      public EntityCustom(World world) {
         super(world);
         this.launchedAge = 0;
         this.normalDamage = 336.7F;
         this.impactRadius = 6.7F;
         this.noClip = true;
         this.setSize(0.8F, 0.8F);
      }

      public EntityCustom(World world, EntityLivingBase owner) {
         this(world);
         this.thrower = owner;
         this.ownerUUID = owner.getUniqueID();
         this.dataManager.set(OWNER_ID, owner.getEntityId());
      }

      public Type getJutsuType() {
         return Type.OTHER;
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(PHASE, 0);
         this.dataManager.register(CHARGE_POWER, 0.1F);
         this.dataManager.register(CHARGE_TICKS, 0);
         this.dataManager.register(RENDER_SCALE, 0.18F);
         this.dataManager.register(OWNER_ID, -1);
      }

      public boolean isCharging() {
         return (Integer)this.dataManager.get(PHASE) == 0;
      }

      public boolean isLaunched() {
         return (Integer)this.dataManager.get(PHASE) == 1;
      }

      public float getChargePower() {
         return (Float)this.dataManager.get(CHARGE_POWER);
      }

      public int getChargeTicks() {
         return (Integer)this.dataManager.get(CHARGE_TICKS);
      }

      public float getRenderScale() {
         return (Float)this.dataManager.get(RENDER_SCALE);
      }

      public void setChargePower(float power) {
         float clamped = MathHelper.clamp(power, 0.1F, 10.0F);
         this.dataManager.set(CHARGE_POWER, clamped);
         int ticks = MathHelper.clamp(Math.round((clamped - 0.1F) / 9.9F * 90.0F), 0, 90);
         this.dataManager.set(CHARGE_TICKS, ticks);
         float visual = (0.22F + clamped / 10.0F * 0.8F) * 0.7F;
         this.dataManager.set(RENDER_SCALE, visual);
         this.normalDamage = (48.0F + clamped * 10.0F) * 1.3F * 1.75F;
         this.impactRadius = 4.5F + clamped * 0.22F;
      }

      public EntityLivingBase getOwnerEntity() {
         if (this.thrower != null && this.thrower.isEntityAlive()) {
            return this.thrower;
         } else {
            Entity entity = this.world.getEntityByID((Integer)this.dataManager.get(OWNER_ID));
            if (entity instanceof EntityLivingBase) {
               this.thrower = (EntityLivingBase)entity;
               this.ownerUUID = entity.getUniqueID();
               return this.thrower;
            } else {
               if (this.ownerUUID != null) {
                  EntityPlayer player = this.world.getPlayerEntityByUUID(this.ownerUUID);
                  if (player != null) {
                     this.thrower = player;
                     this.dataManager.set(OWNER_ID, player.getEntityId());
                     return player;
                  }
               }

               return null;
            }
         }
      }

      public void refreshAnchor() {
         EntityLivingBase owner = this.getOwnerEntity();
         if (owner != null) {
            Vec3d look = owner.getLookVec();
            Vec3d side = look.crossProduct(new Vec3d((double)0.0F, (double)1.0F, (double)0.0F));
            if (side.lengthSquared() < 1.0E-4) {
               side = new Vec3d((double)1.0F, (double)0.0F, (double)0.0F);
            } else {
               side = side.normalize();
            }

            Vec3d anchor = owner.getPositionEyes(1.0F).add(look.scale(1.18 + (double)this.getRenderScale() * 0.18)).add(side.scale(0.06)).add(new Vec3d((double)0.0F, -0.92, (double)0.0F));
            this.setPosition(anchor.x, anchor.y, anchor.z);
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.lastTickPosX = this.posX;
            this.lastTickPosY = this.posY;
            this.lastTickPosZ = this.posZ;
            this.motionX = (double)0.0F;
            this.motionY = (double)0.0F;
            this.motionZ = (double)0.0F;
            this.rotationYaw = owner.rotationYaw;
            this.prevRotationYaw = owner.rotationYaw;
            this.rotationPitch = owner.rotationPitch;
            this.prevRotationPitch = owner.rotationPitch;
         }
      }

      public void launchToward(Vec3d look) {
         Vec3d direction = look.normalize();
         this.dataManager.set(PHASE, 1);
         this.noClip = false;
         this.launchedAge = 0;
         float speed = 1.875F;
         this.motionX = direction.x * (double)speed;
         this.motionY = direction.y * (double)speed;
         this.motionZ = direction.z * (double)speed;
         this.rotationYaw = this.prevRotationYaw = (float)(Math.atan2(direction.x, direction.z) * (180D / Math.PI));
         this.rotationPitch = this.prevRotationPitch = (float)(Math.atan2(-direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)) * (180D / Math.PI));
         this.setSize(2.6F, 2.6F);
      }

      public void onUpdate() {
         if (this.isCharging()) {
            ++this.ticksExisted;
            EntityLivingBase owner = this.getOwnerEntity();
            if (owner != null && this.isValidHolder(owner)) {
               this.refreshAnchor();
               if (!this.world.isRemote) {
                  this.spawnChargeAura();
               }

            } else {
               this.setDead();
            }
         } else {
            super.onUpdate();
            ++this.launchedAge;
            Vec3d motion = new Vec3d(this.motionX, this.motionY, this.motionZ);
            if (motion.lengthSquared() > 1.0E-4) {
               Vec3d direction = motion.normalize();
               this.rotationYaw = this.prevRotationYaw = (float)(Math.atan2(direction.x, direction.z) * (180D / Math.PI));
               this.rotationPitch = this.prevRotationPitch = (float)(Math.atan2(-direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)) * (180D / Math.PI));
            }

            if (!this.world.isRemote) {
               this.spawnFlightTrail();
               if (this.launchedAge > 80) {
                  this.detonate();
               }
            }

         }
      }

      private boolean isValidHolder(EntityLivingBase owner) {
         if (owner.isEntityAlive() && owner.isHandActive()) {
            ItemStack active = owner.getActiveItemStack();
            if (!active.isEmpty() && active.getItem() instanceof ItemShrineRelease.RangedItem) {
               ItemShrineRelease.RangedItem item = (ItemShrineRelease.RangedItem)active.getItem();
               ItemJutsu.JutsuEnum current = item.getCurrentJutsu(active);
               return current != null && current.index == ItemShrineRelease.DIVINE_FLAMES.index;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private void spawnChargeAura() {
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 1, 0.11, 0.11, 0.11, (double)0.0F, 0.03, (double)0.0F, new int[]{-26081, 20});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 1, 0.06, 0.06, 0.06, (double)0.0F, 0.08, (double)0.0F, new int[]{-46062, 18});
      }

      private void spawnFlightTrail() {
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 6, 0.24, 0.24, 0.24, (double)0.0F, 0.03, (double)0.0F, new int[]{-23262, 26});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 4, 0.16, 0.16, 0.16, (double)0.0F, 0.02, (double)0.0F, new int[]{-46321, 22});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX - this.motionX * (double)0.25F, this.posY - this.motionY * (double)0.25F, this.posZ - this.motionZ * (double)0.25F, 2, 0.08, 0.08, 0.08, (double)0.0F, 0.03, (double)0.0F, new int[]{-1440079334, 20});
      }

      protected void onImpact(RayTraceResult result) {
         if (this.isLaunched() && !this.world.isRemote) {
            EntityLivingBase owner = this.getOwnerEntity();
            if (result.entityHit == null || result.entityHit != owner) {
               this.detonate();
            }
         }
      }

      private void detonate() {
         if (!this.world.isRemote && !this.isDead) {
            EntityLivingBase owner = this.getOwnerEntity();
            float radius = this.impactRadius * 2.0F;
            AxisAlignedBB box = new AxisAlignedBB(this.posX - (double)radius, this.posY - (double)radius, this.posZ - (double)radius, this.posX + (double)radius, this.posY + (double)radius, this.posZ + (double)radius);

            for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, box, (targetx) -> targetx != null && targetx.isEntityAlive() && targetx != owner && ItemJutsu.canTarget(targetx))) {
               double distance = target.getDistance(this.posX, this.posY, this.posZ);
               if (!(distance > (double)radius)) {
                  float falloff = (float)Math.max((double)0.25F, (double)1.0F - distance / (double)radius);
                  float dmg = this.normalDamage * falloff;
                  if (!(target instanceof EntityPlayer)) {
                     dmg = Math.min(dmg, 70.0F);
                  }

                  target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, owner), dmg);
                  target.setFire(7);
                  this.applyExplosionPush(target);
               }
            }

            this.spawnImpactPlume(radius);
            SoundEvent impact = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("inftsukaddon:shrine_divineflames_impact"));
            if (impact != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, impact, SoundCategory.PLAYERS, 1.5F, 1.0F);
            } else {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.4F, 0.9F);
            }

            this.setDead();
         }
      }

      private void applyExplosionPush(EntityLivingBase target) {
         Vec3d push = new Vec3d(target.posX - this.posX, (double)0.0F, target.posZ - this.posZ);
         if (push.lengthSquared() < 1.0E-4) {
            push = new Vec3d(this.rand.nextDouble() - (double)0.5F, (double)0.0F, this.rand.nextDouble() - (double)0.5F);
         }

         push = push.normalize();
         target.motionX += push.x * (double)1.25F;
         target.motionY += 0.45;
         target.motionZ += push.z * (double)1.25F;
         target.velocityChanged = true;
      }

      private void spawnImpactPlume(float radius) {
         float visualRadius = radius * 3.6F;
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 1800, (double)visualRadius * 0.28, 1.6, (double)visualRadius * 0.28, 0.02, 0.36, 0.02, new int[]{-22998, 52});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + 0.3, this.posZ, 1500, (double)visualRadius * 0.24, 4.2, (double)visualRadius * 0.24, (double)0.0F, 0.72, (double)0.0F, new int[]{-45550, 48});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + 0.6, this.posZ, 2200, (double)visualRadius * 0.18, 19.6, (double)visualRadius * 0.18, (double)0.0F, 0.52, (double)0.0F, new int[]{-20432, 54});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + 1.4, this.posZ, 1800, (double)visualRadius * 0.14, 24.8, (double)visualRadius * 0.14, (double)0.0F, 0.42, (double)0.0F, new int[]{-31716, 54});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 680, (double)visualRadius * 0.3, 2.6, (double)visualRadius * 0.3, (double)0.0F, 0.32, (double)0.0F, new int[]{-1439947747, 52});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 980, (double)visualRadius * 0.12, (double)9.0F, (double)visualRadius * 0.12, (double)0.0F, 0.58, (double)0.0F, new int[]{-34280, 50});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + 0.7, this.posZ, 520, (double)visualRadius * 0.18, (double)8.0F, (double)visualRadius * 0.18, (double)0.0F, 0.28, (double)0.0F, new int[]{-1439487969, 52});
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;

            for(int layer = 0; layer < 52; ++layer) {
               double progress = (double)layer / (double)51.0F;
               double layerY = this.posY + 0.15 + progress * (double)visualRadius * 5.1;
               double layerRadius = (double)visualRadius * (0.14 + progress * 0.2);

               for(int point = 0; point < 48; ++point) {
                  double angle = 0.1308996938995747 * (double)point + progress * Math.PI * 2.8;
                  double px = this.posX + Math.cos(angle) * layerRadius;
                  double pz = this.posZ + Math.sin(angle) * layerRadius;
                  double swirl = 0.16 + progress * 0.1;
                  double rise = 0.84 + progress * 1.1;
                  ws.spawnParticle(EnumParticleTypes.FLAME, px, layerY, pz, 0, -Math.sin(angle) * swirl, rise, Math.cos(angle) * swirl, (double)1.0F, new int[0]);
                  if ((point + layer) % 2 == 0) {
                     ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, px, layerY - 0.08, pz, 0, -Math.sin(angle) * swirl * 0.55, rise * (double)0.75F, Math.cos(angle) * swirl * 0.55, (double)1.0F, new int[0]);
                  }
               }
            }

            for(int arm = 0; arm < 10; ++arm) {
               double baseAngle = (Math.PI / 5D) * (double)arm;

               for(int step = 0; step < 78; ++step) {
                  double progress = (double)step / (double)77.0F;
                  double angle = baseAngle + progress * Math.PI * 3.4;
                  double spiralRadius = (double)visualRadius * (0.1 + progress * 0.18);
                  double px = this.posX + Math.cos(angle) * spiralRadius;
                  double pz = this.posZ + Math.sin(angle) * spiralRadius;
                  double py = this.posY + 0.2 + progress * (double)visualRadius * (double)5.5F;
                  double twist = 0.22 + progress * 0.14;
                  double rise = 1.1 + progress * 1.44;
                  ws.spawnParticle(EnumParticleTypes.FLAME, px, py, pz, 0, -Math.sin(angle) * twist, rise, Math.cos(angle) * twist, (double)1.0F, new int[0]);
                  if (step % 2 == 0) {
                     ws.spawnParticle(EnumParticleTypes.LAVA, px, py - 0.03, pz, 0, -Math.sin(angle) * twist * 0.14, rise * 0.42, Math.cos(angle) * twist * 0.14, (double)1.0F, new int[0]);
                  }
               }
            }

            for(int coreBand = 0; coreBand < 30; ++coreBand) {
               double bandHeight = this.posY + 0.18 + (double)coreBand * (double)visualRadius * 0.17;

               for(int i = 0; i < 160; ++i) {
                  double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
                  double bandRadius = (double)visualRadius * (0.04 + (double)coreBand * 0.004) * Math.sqrt(this.rand.nextDouble());
                  double px = this.posX + Math.cos(angle) * bandRadius;
                  double pz = this.posZ + Math.sin(angle) * bandRadius;
                  double swirl = 0.08 + this.rand.nextDouble() * 0.08;
                  double rise = 0.88 + this.rand.nextDouble() * 0.72 + (double)coreBand * 0.024;
                  ws.spawnParticle(EnumParticleTypes.FLAME, px, bandHeight, pz, 0, -Math.sin(angle) * swirl, rise, Math.cos(angle) * swirl, (double)1.0F, new int[0]);
               }
            }

            for(int i = 0; i < 54; ++i) {
               double columnX = this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)visualRadius * 0.18;
               double columnZ = this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)visualRadius * 0.18;
               ws.spawnParticle(EnumParticleTypes.FLAME, columnX, this.posY + 0.2, columnZ, 0, (double)0.0F, 7.6 + this.rand.nextDouble() * 4.8, (double)0.0F, (double)1.0F, new int[0]);
               ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, columnX, this.posY + 0.15, columnZ, 0, (double)0.0F, 3.9 + this.rand.nextDouble() * 2.3, (double)0.0F, (double)1.0F, new int[0]);
            }

            ws.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + 0.2, this.posZ, 84, (double)visualRadius * 0.28, 0.85, (double)visualRadius * 0.28, (double)0.0F, new int[0]);
         }

      }

      protected float getGravityVelocity() {
         return 0.0F;
      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      public void setDead() {
         if (!this.world.isRemote) {
            EntityLivingBase owner = this.getOwnerEntity();
            UUID playerId = owner != null ? owner.getUniqueID() : this.ownerUUID;
            if (playerId != null) {
               Jutsu.clearIfMatches(playerId, this);
            }
         }

         super.setDead();
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("sdfPhase", (Integer)this.dataManager.get(PHASE));
         compound.setFloat("sdfPower", this.getChargePower());
         compound.setInteger("sdfTicks", this.getChargeTicks());
         compound.setFloat("sdfScale", this.getRenderScale());
         compound.setInteger("sdfOwnerId", (Integer)this.dataManager.get(OWNER_ID));
         if (this.ownerUUID != null) {
            compound.setString("sdfOwnerUUID", this.ownerUUID.toString());
         }

         compound.setInteger("sdfLaunchAge", this.launchedAge);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.dataManager.set(PHASE, compound.getInteger("sdfPhase"));
         this.dataManager.set(CHARGE_POWER, compound.getFloat("sdfPower"));
         this.dataManager.set(CHARGE_TICKS, compound.getInteger("sdfTicks"));
         this.dataManager.set(RENDER_SCALE, compound.getFloat("sdfScale"));
         this.dataManager.set(OWNER_ID, compound.getInteger("sdfOwnerId"));
         if (compound.hasKey("sdfOwnerUUID")) {
            try {
               this.ownerUUID = UUID.fromString(compound.getString("sdfOwnerUUID"));
            } catch (IllegalArgumentException var3) {
            }
         }

         this.launchedAge = compound.getInteger("sdfLaunchAge");
      }

      static {
         PHASE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         CHARGE_POWER = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         CHARGE_TICKS = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         RENDER_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
         OWNER_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, EntityCustom> ACTIVE_ARROWS = new HashMap();

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (entity.world.isRemote) {
            return false;
         } else {
            EntityCustom arrow = (EntityCustom)ACTIVE_ARROWS.get(entity.getUniqueID());
            if (arrow != null && !arrow.isDead) {
               if (power < this.getMaxPower(stack, entity)) {
                  arrow.setDead();
                  ACTIVE_ARROWS.remove(entity.getUniqueID());
                  return false;
               } else {
                  arrow.launchToward(entity.getLookVec());
                  ACTIVE_ARROWS.remove(entity.getUniqueID());
                  return true;
               }
            } else {
               return false;
            }
         }
      }

      public float getBasePower() {
         return 0.1F;
      }

      public float getPowerupDelay() {
         return 9.090909F;
      }

      public float getMaxPower() {
         return 10.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote && player instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer)player;
            if (!PlayerTracker.isNinja(entityPlayer)) {
               entityPlayer.stopActiveHand();
               clearIfMatches(entityPlayer.getUniqueID(), (EntityCustom)ACTIVE_ARROWS.get(entityPlayer.getUniqueID()));
               return;
            }

            Chakra.Pathway pathway = Chakra.pathway(entityPlayer);
            double drain = 0.2 + (double)power * 0.08;
            if (pathway == null || pathway.getAmount() < drain) {
               entityPlayer.stopActiveHand();
               clearIfMatches(entityPlayer.getUniqueID(), (EntityCustom)ACTIVE_ARROWS.get(entityPlayer.getUniqueID()));
               return;
            }

            pathway.consume(drain);
            EntityCustom arrow = (EntityCustom)ACTIVE_ARROWS.get(entityPlayer.getUniqueID());
            if (arrow != null && !arrow.isDead && arrow.isCharging()) {
               arrow.setChargePower(Math.max(power, 0.1F));
               arrow.refreshAnchor();
            } else {
               arrow = new EntityCustom(entityPlayer.world, entityPlayer);
               arrow.setChargePower(Math.max(power, 0.1F));
               arrow.refreshAnchor();
               entityPlayer.world.spawnEntity(arrow);
               ACTIVE_ARROWS.put(entityPlayer.getUniqueID(), arrow);
            }

            if (entityPlayer.ticksExisted % 2 == 0) {
               spawnHandFlames(entityPlayer.world, entityPlayer);
            }
         }

         super.onUsingTick(stack, player, power);
      }

      private static void spawnHandFlames(World world, EntityLivingBase player) {
         Vec3d look = player.getLookVec();
         Vec3d side = look.crossProduct(new Vec3d((double)0.0F, (double)1.0F, (double)0.0F));
         if (side.lengthSquared() < 1.0E-4) {
            side = new Vec3d((double)1.0F, (double)0.0F, (double)0.0F);
         } else {
            side = side.normalize();
         }

         Vec3d up = new Vec3d((double)0.0F, (double)1.0F, (double)0.0F);
         Vec3d center = player.getPositionEyes(1.0F).add(look.scale(0.3)).add(up.scale(-0.28));
         Vec3d left = center.add(side.scale(-0.28));
         Vec3d right = center.add(side.scale(0.28));
         Particles.spawnParticle(world, Types.FLAME, left.x, left.y, left.z, 1, 0.04, 0.04, 0.04, (double)0.0F, 0.03, (double)0.0F, new int[]{-30182, 18});
         Particles.spawnParticle(world, Types.FLAME, right.x, right.y, right.z, 1, 0.04, 0.04, 0.04, (double)0.0F, 0.03, (double)0.0F, new int[]{-46065, 18});
      }

      public static void clearIfMatches(UUID playerId, EntityCustom entity) {
         if (playerId != null) {
            EntityCustom current = (EntityCustom)ACTIVE_ARROWS.get(playerId);
            if (current == entity) {
               ACTIVE_ARROWS.remove(playerId);
            }

         }
      }
   }
}
