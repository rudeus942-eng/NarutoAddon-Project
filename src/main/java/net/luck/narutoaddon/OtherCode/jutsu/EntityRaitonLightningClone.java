
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.EntityRaitonBeam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityRaitonLightningClone extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 285;

   public EntityRaitonLightningClone(ElementsInfTsukAddon instance) {
      super(instance, 921);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "raiton_lightning_clone"), 285).name("inftsuk_raiton_lightning_clone").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, LightningCloneRenderer::new);
   }

   public static class EntityCustom extends EntityCreature implements ItemJutsu.IJutsu {
      private static final int MAX_LIFETIME = 300;
      private static final Map<UUID, EntityCustom> ACTIVE_CLONES = new WeakHashMap();
      private static final DataParameter<String> OWNER_NAME;
      private int lifetime = 0;
      private float power = 1.0F;
      private UUID ownerUUID;
      private int attackCooldown = 0;
      private boolean hasExploded = false;
      private int leapCooldown = 0;
      private boolean isLeaping = false;
      private int leapTicks = 0;
      private int dashCooldown = 0;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.6F, 1.8F);
      }

      public EntityCustom(World world, EntityLivingBase owner, float power) {
         super(world);
         this.setSize(0.6F, 1.8F);
         this.power = power;
         this.ownerUUID = owner.getUniqueID();
         if (owner instanceof EntityPlayer) {
            this.setOwnerName(owner.getName());
         }

         this.getEntityData().setString("SummonerID", owner.getUniqueID().toString());
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)50.0F + (double)power * (double)50.0F);
         this.setHealth(this.getMaxHealth());
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(OWNER_NAME, "");
      }

      public void setOwnerName(String name) {
         this.dataManager.set(OWNER_NAME, name);
      }

      public String getOwnerName() {
         return (String)this.dataManager.get(OWNER_NAME);
      }

      public Type getJutsuType() {
         return Type.RAITON;
      }

      private EntityPlayer getOwnerPlayer() {
         if (this.ownerUUID != null && !this.world.isRemote) {
            for(EntityPlayer player : this.world.playerEntities) {
               if (player.getUniqueID().equals(this.ownerUUID)) {
                  return player;
               }
            }

            return null;
         } else {
            return null;
         }
      }

      protected void initEntityAI() {
         this.tasks.addTask(0, new EntityAISwimming(this));
         this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.2, false));
         this.tasks.addTask(2, new EntityAIWanderAvoidWater(this, 0.8));
         this.tasks.addTask(3, new EntityAILookIdle(this));
         this.targetTasks.addTask(0, new EntityAIHurtByTarget(this, false, new Class[0]) {
            public boolean shouldExecute() {
               return this.taskOwner.getRevengeTarget() != null && EntityCustom.this.ownerUUID != null && this.taskOwner.getRevengeTarget().getUniqueID().equals(EntityCustom.this.ownerUUID) ? false : super.shouldExecute();
            }
         });
         this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 10, true, false, (e) -> e.isEntityAlive() && this.ownerUUID != null && !e.getUniqueID().equals(this.ownerUUID)));
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)50.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.45);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)12.0F);
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)5.0F);
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 300 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               if (this.attackCooldown > 0) {
                  --this.attackCooldown;
               }

               if (this.leapCooldown > 0) {
                  --this.leapCooldown;
               }

               if (this.dashCooldown > 0) {
                  --this.dashCooldown;
               }

               EntityLivingBase target = this.getAttackTarget();
               if (this.isLeaping) {
                  ++this.leapTicks;
                  if (this.leapTicks > 5 && target != null && target.isEntityAlive() && this.getDistanceSq(target) <= (double)4.0F) {
                     float leapDmg = this.power * 3.0F;
                     EntityPlayer ownerPlayer = this.getOwnerPlayer();
                     Entity dmgSource = (Entity)(ownerPlayer != null ? ownerPlayer : this);
                     target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, dmgSource), leapDmg);
                     target.hurtResistantTime = 0;
                     Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)0.5F, this.posZ, 10, (double)0.5F, 0.3, (double)0.5F, 0.1, 0.05, 0.1, new int[]{-7816193, 8});
                     SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
                     if (electricity != null) {
                        this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, electricity, SoundCategory.PLAYERS, 0.8F, 0.9F + this.rand.nextFloat() * 0.3F);
                     }

                     this.isLeaping = false;
                  }

                  if (this.leapTicks > 20) {
                     this.isLeaping = false;
                  }
               }

               if (!this.isLeaping && this.leapCooldown <= 0 && target != null && target.isEntityAlive()) {
                  double dist = (double)this.getDistance(target);
                  if (dist >= (double)4.0F && dist <= (double)8.0F) {
                     double dx = target.posX - this.posX;
                     double dz = target.posZ - this.posZ;
                     double horizDist = Math.sqrt(dx * dx + dz * dz);
                     if (horizDist > (double)0.5F) {
                        double nx = dx / horizDist;
                        double nz = dz / horizDist;
                        double speed = 0.8 + this.rand.nextDouble() * 0.2;
                        this.motionX = nx * speed;
                        this.motionZ = nz * speed;
                        this.motionY = 0.4;
                        this.velocityChanged = true;
                        this.isLeaping = true;
                        this.leapTicks = 0;
                        this.leapCooldown = 60 + this.rand.nextInt(21);
                        Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + 0.3, this.posZ, 12, 0.4, 0.2, 0.4, 0.1, 0.08, 0.1, new int[]{-14522625, 10});
                        SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
                        if (electricity != null) {
                           this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, electricity, SoundCategory.PLAYERS, 1.0F, 0.8F + this.rand.nextFloat() * 0.3F);
                        }
                     }
                  }
               }

               if (!this.isLeaping && this.dashCooldown <= 0 && target != null && target.isEntityAlive()) {
                  double dist = (double)this.getDistance(target);
                  if (dist <= (double)3.0F && dist > (double)0.5F) {
                     double dx = target.posX - this.posX;
                     double dz = target.posZ - this.posZ;
                     double horizDist = Math.sqrt(dx * dx + dz * dz);
                     if (horizDist > 0.3) {
                        double nx = dx / horizDist;
                        double nz = dz / horizDist;
                        boolean goLeft = this.rand.nextBoolean();
                        double perpX = goLeft ? -nz : nz;
                        double perpZ = goLeft ? nx : -nx;
                        double dashDist = (double)2.0F + this.rand.nextDouble();
                        double newX = this.posX + perpX * dashDist;
                        double newZ = this.posZ + perpZ * dashDist;
                        int steps = 5;

                        for(int i = 0; i < steps; ++i) {
                           double t = (double)i / (double)steps;
                           double px = this.posX + (newX - this.posX) * t;
                           double pz = this.posZ + (newZ - this.posZ) * t;
                           Particles.spawnParticle(this.world, Types.FLAME, px, this.posY + 0.9, pz, 3, 0.1, 0.3, 0.1, 0.02, 0.02, 0.02, new int[]{-7816193, 6});
                        }

                        this.setPositionAndUpdate(newX, this.posY, newZ);
                        this.dashCooldown = 40 + this.rand.nextInt(21);
                        Particles.spawnParticle(this.world, Types.FLAME, newX, this.posY + 0.9, newZ, 6, 0.2, 0.4, 0.2, 0.05, 0.03, 0.05, new int[]{-14522625, 8});
                        SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
                        if (electricity != null) {
                           this.world.playSound((EntityPlayer)null, newX, this.posY, newZ, electricity, SoundCategory.PLAYERS, 0.6F, 1.2F + this.rand.nextFloat() * 0.4F);
                        }
                     }
                  }
               }

               if (this.attackCooldown <= 0 && target != null && target.isEntityAlive() && this.getDistanceSq(target) <= (double)4.0F) {
                  this.applyMeleeDamage(target);
                  this.attackCooldown = 20;
               }

               if (this.lifetime % 5 == 0) {
                  EntityRaitonBeam.EntityCustom trailBolt = new EntityRaitonBeam.EntityCustom(this.world, this, 0.0F, 0.0F);
                  trailBolt.setPosition(this.posX, this.posY + 0.1, this.posZ);
                  trailBolt.motionX = (double)0.0F;
                  trailBolt.motionY = (double)0.0F;
                  trailBolt.motionZ = (double)0.0F;
                  trailBolt.setEntityScale(0.15F);
                  trailBolt.setMaxLifetime(6);
                  this.world.spawnEntity(trailBolt);
               }

               if (this.lifetime % 10 == 0) {
                  SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
                  if (electricity != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, electricity, SoundCategory.PLAYERS, 0.3F, 1.0F + this.rand.nextFloat() * 0.5F);
                  }
               }

               if (this.lifetime % 3 == 0) {
                  Particles.spawnParticle(this.world, Types.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.8, this.posY + this.rand.nextDouble() * 1.8, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.8, 2, 0.1, 0.1, 0.1, (this.rand.nextDouble() - (double)0.5F) * 0.08, (this.rand.nextDouble() - (double)0.5F) * 0.08, (this.rand.nextDouble() - (double)0.5F) * 0.08, new int[]{-14522625, 8});
               }

               if (this.lifetime % 10 == 0) {
                  Particles.spawnParticle(this.world, Types.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.6, this.posY + 0.9 + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.6, 4, 0.2, 0.3, 0.2, (double)0.0F, 0.04, (double)0.0F, new int[]{-7816193, 10});
               }
            }

         } else {
            if (!this.hasExploded && !this.world.isRemote) {
               this.deathBurst();
            }

            this.setDead();
         }
      }

      public boolean attackEntityAsMob(Entity target) {
         if (target instanceof EntityLivingBase) {
            this.applyMeleeDamage((EntityLivingBase)target);
            this.attackCooldown = 20;
            return true;
         } else {
            return false;
         }
      }

      private void applyMeleeDamage(EntityLivingBase target) {
         float dmg = 20.0F + this.power * 10.0F;
         EntityPlayer ownerPlayer = this.getOwnerPlayer();
         Entity dmgSource = (Entity)(ownerPlayer != null ? ownerPlayer : this);
         if (!(target instanceof EntityPlayer)) {
            dmg = Math.min(dmg, 100.0F);
         }

         target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, dmgSource), dmg);
         target.hurtResistantTime = 0;
         SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
         if (electricity != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, electricity, SoundCategory.PLAYERS, 0.5F, 1.0F + this.rand.nextFloat() * 0.4F);
         }

         Particles.spawnParticle(this.world, Types.FLAME, target.posX, target.posY + (double)target.height * (double)0.5F, target.posZ, 5, 0.3, 0.3, 0.3, 0.05, 0.05, 0.05, new int[]{-7816193, 5});
      }

      public void onDeath(DamageSource cause) {
         if (!this.hasExploded && !this.world.isRemote) {
            this.deathBurst();
         }

         super.onDeath(cause);
      }

      private void deathBurst() {
         this.hasExploded = true;
         float burstDmg = 50.0F + this.power * 20.0F;
         EntityPlayer ownerPlayer = this.getOwnerPlayer();
         Entity dmgSource = (Entity)(ownerPlayer != null ? ownerPlayer : this);

         for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)6.0F, this.posY - (double)3.0F, this.posZ - (double)6.0F, this.posX + (double)6.0F, this.posY + (double)5.0F, this.posZ + (double)6.0F), (e) -> e != this && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
            double dist = (double)this.getDistance(target);
            if (dist <= (double)6.0F) {
               float falloff = (float)Math.max(0.3, (double)1.0F - dist / (double)7.0F);
               float totalDmg = burstDmg * falloff;
               if (!(target instanceof EntityPlayer)) {
                  totalDmg = Math.min(totalDmg, 100.0F);
               }

               target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, dmgSource), totalDmg);
               target.hurtResistantTime = 0;
               target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 2, false, false));
            }
         }

         for(int i = 0; i < 10; ++i) {
            double angle = (double)i / (double)10.0F * Math.PI * (double)2.0F;
            EntityRaitonBeam.EntityCustom smallBolt = new EntityRaitonBeam.EntityCustom(this.world, this, 0.0F, 0.0F);
            smallBolt.setPosition(this.posX, this.posY + 0.9, this.posZ);
            smallBolt.motionX = Math.cos(angle) * (double)0.5F;
            smallBolt.motionY = (this.rand.nextDouble() - 0.3) * 0.4;
            smallBolt.motionZ = Math.sin(angle) * (double)0.5F;
            smallBolt.setEntityScale(0.25F);
            smallBolt.setMaxLifetime(12);
            this.world.spawnEntity(smallBolt);
         }

         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + 0.9, this.posZ, 25, (double)1.5F, (double)1.5F, (double)1.5F, 0.1, 0.1, 0.1, new int[]{-5579265, 10});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + 0.9, this.posZ, 20, (double)2.0F, (double)1.0F, (double)2.0F, 0.15, 0.1, 0.15, new int[]{-14522625, 15});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)0.5F, this.posZ, 15, (double)2.5F, (double)0.5F, (double)2.5F, 0.08, 0.12, 0.08, new int[]{-7816193, 20});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + 0.1, this.posZ, 10, (double)3.0F, 0.1, (double)3.0F, 0.02, 0.01, 0.02, new int[]{-14527028, 25});
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 2.0F, 0.7F + this.rand.nextFloat() * 0.3F);
         SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
         if (electricity != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, electricity, SoundCategory.PLAYERS, 1.5F, 0.6F);
         }

      }

      public static void removeExistingClone(UUID ownerUUID) {
         EntityCustom existing = (EntityCustom)ACTIVE_CLONES.get(ownerUUID);
         if (existing != null && existing.isEntityAlive()) {
            existing.hasExploded = true;
            existing.setDead();
         }

         ACTIVE_CLONES.remove(ownerUUID);
      }

      public static void registerClone(UUID ownerUUID, EntityCustom clone) {
         ACTIVE_CLONES.put(ownerUUID, clone);
      }

      public boolean canBeCollidedWith() {
         return true;
      }

      public boolean canBePushed() {
         return true;
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("lcLifetime");
         this.power = compound.getFloat("lcPower");
         this.hasExploded = compound.getBoolean("lcExploded");
         if (compound.hasUniqueId("lcOwner")) {
            this.ownerUUID = compound.getUniqueId("lcOwner");
         }

         if (compound.hasKey("lcOwnerName")) {
            this.setOwnerName(compound.getString("lcOwnerName"));
         }

         this.leapCooldown = compound.getInteger("lcLeapCooldown");
         this.dashCooldown = compound.getInteger("lcDashCooldown");
         if (this.ownerUUID != null) {
            this.getEntityData().setString("SummonerID", this.ownerUUID.toString());
         }

      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("lcLifetime", this.lifetime);
         compound.setFloat("lcPower", this.power);
         compound.setBoolean("lcExploded", this.hasExploded);
         if (this.ownerUUID != null) {
            compound.setUniqueId("lcOwner", this.ownerUUID);
         }

         compound.setString("lcOwnerName", this.getOwnerName());
         compound.setInteger("lcLeapCooldown", this.leapCooldown);
         compound.setInteger("lcDashCooldown", this.dashCooldown);
      }

      static {
         OWNER_NAME = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (power < 1.0F) {
            return false;
         } else if (entity.world.isRemote) {
            return false;
         } else {
            EntityCustom.removeExistingClone(entity.getUniqueID());
            double lookX = -Math.sin(Math.toRadians((double)entity.rotationYaw));
            double lookZ = Math.cos(Math.toRadians((double)entity.rotationYaw));
            double spawnX = entity.posX + lookX * (double)2.0F;
            double spawnZ = entity.posZ + lookZ * (double)2.0F;
            EntityCustom clone = new EntityCustom(entity.world, entity, power);
            clone.setPosition(spawnX, entity.posY, spawnZ);
            clone.rotationYaw = entity.rotationYaw;
            entity.world.spawnEntity(clone);
            EntityCustom.registerClone(entity.getUniqueID(), clone);
            Particles.spawnParticle(entity.world, Types.FLAME, spawnX, entity.posY + 0.9, spawnZ, 15, 0.4, 0.8, 0.4, 0.05, 0.1, 0.05, new int[]{-5579265, 8});
            Particles.spawnParticle(entity.world, Types.FLAME, spawnX, entity.posY + (double)0.5F, spawnZ, 10, 0.6, 0.3, 0.6, 0.08, 0.02, 0.08, new int[]{-14522625, 12});
            Particles.spawnParticle(entity.world, Types.FLAME, spawnX, entity.posY + 0.1, spawnZ, 8, (double)0.5F, 0.05, (double)0.5F, 0.02, 0.01, 0.02, new int[]{-7816193, 15});
            Particles.spawnParticle(entity.world, Types.FLAME, spawnX, entity.posY + (double)1.0F, spawnZ, 5, 0.3, (double)0.5F, 0.3, (double)0.0F, 0.03, (double)0.0F, new int[]{-14527028, 18});
            SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
            if (electricity != null) {
               entity.world.playSound((EntityPlayer)null, spawnX, entity.posY, spawnZ, electricity, SoundCategory.PLAYERS, 1.0F, 1.2F);
            }

            entity.world.playSound((EntityPlayer)null, spawnX, entity.posY, spawnZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 0.6F, 1.4F);
            return true;
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
            Particles.spawnParticle(player.world, Types.FLAME, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.6, player.posY + 0.15, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.6, 3, 0.15, 0.15, 0.15, (double)0.0F, 0.03, (double)0.0F, new int[]{-14522625, 12});
            if (player.ticksExisted % 4 == 0) {
               Particles.spawnParticle(player.world, Types.FLAME, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.8, player.posY + (double)0.5F + player.getRNG().nextDouble() * (double)1.0F, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.8, 1, 0.1, 0.2, 0.1, (double)0.0F, 0.02, (double)0.0F, new int[]{-7816193, 15});
            }
         }

         super.onUsingTick(stack, player, power);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class LightningCloneRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation STEVE_TEXTURE = new ResourceLocation("textures/entity/steve.png");

      public LightningCloneRenderer(RenderManager renderManager) {
         super(renderManager, new ModelPlayer(0.0F, false), 0.5F);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         GlStateManager.disableLighting();
         GlStateManager.depthMask(false);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         float flicker = 0.85F + (float)Math.sin((double)entity.ticksExisted * (double)0.5F) * 0.15F;
         GlStateManager.color(0.3F * flicker, 0.5F * flicker, 1.0F * flicker, 0.15F);
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
         GlStateManager.depthMask(true);
         GlStateManager.enableLighting();
         GlStateManager.disableBlend();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         String ownerName = entity.getOwnerName();
         if (ownerName != null && !ownerName.isEmpty()) {
            ResourceLocation skin = this.getSkinForPlayer(ownerName);
            if (skin != null) {
               return skin;
            }
         }

         return STEVE_TEXTURE;
      }

      private ResourceLocation getSkinForPlayer(String playerName) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.world != null) {
            EntityPlayer player = mc.world.getPlayerEntityByName(playerName);
            if (player instanceof AbstractClientPlayer) {
               return ((AbstractClientPlayer)player).getLocationSkin();
            }
         }

         return STEVE_TEXTURE;
      }
   }
}
