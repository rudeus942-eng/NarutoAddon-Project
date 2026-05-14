
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
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
import net.narutomod.Particles;
import net.narutomod.Particles.Types;
import net.narutomod.entity.EntityLightningArc;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemJutsu.JutsuEnum.Type;
import net.narutomod.procedure.ProcedureUtils;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityKirin extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 313;

   public EntityKirin(ElementsInfTsukAddon instance) {
      super(instance, 944);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityKirinStorm.class).id(new ResourceLocation("inftsukaddon", "kirin"), 313).name("inftsuk_kirin").tracker(128, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityKirinStorm.class, KirinRenderer::new);
   }

   public static class EntityKirinStorm extends Entity implements ItemJutsu.IJutsu {
      private static final int CHARGE_TICKS = 60;
      private static final int MAX_LIFETIME = 200;
      private static final double SPAWN_HEIGHT = (double)60.0F;
      private static final double DAMAGE_RADIUS = (double)15.0F;
      private static final double DIVE_SPEED = (double)3.0F;
      private static final DataParameter<Float> SCALE;
      private static final DataParameter<Boolean> LAUNCHED;
      private int lifetime;
      private float power;
      private EntityLivingBase caster;
      private UUID casterUUID;
      private boolean hasImpacted;
      private int impactTick;
      private double targetX;
      private double targetY;
      private double targetZ;
      private int groundY;
      private double interpTargetX;
      private double interpTargetY;
      private double interpTargetZ;
      private float interpTargetYaw;
      private float interpTargetPitch;
      private int interpSteps;

      public EntityKirinStorm(World world) {
         super(world);
         this.lifetime = 0;
         this.power = 1.0F;
         this.hasImpacted = false;
         this.impactTick = 0;
         this.setSize(2.0F, 2.0F);
         this.noClip = true;
         this.isImmuneToFire = true;
      }

      public EntityKirinStorm(World world, EntityLivingBase caster, double tx, double ty, double tz, float power) {
         this(world);
         this.caster = caster;
         this.casterUUID = caster.getUniqueID();
         this.power = power;
         this.targetX = tx;
         this.targetY = ty;
         this.targetZ = tz;
         this.groundY = world.getHeight((int)Math.floor(tx), (int)Math.floor(tz));
         this.setPosition(tx, (double)this.groundY + (double)60.0F, tz);
         this.faceTarget();
      }

      public void setTargetPos(double tx, double ty, double tz) {
         this.targetX = tx;
         this.targetY = ty;
         this.targetZ = tz;
         this.groundY = this.world.getHeight((int)Math.floor(tx), (int)Math.floor(tz));
         this.faceTarget();
      }

      private void faceTarget() {
         double dx = this.targetX - this.posX;
         double dy = this.targetY - this.posY;
         double dz = this.targetZ - this.posZ;
         double horizDist = Math.sqrt(dx * dx + dz * dz);
         this.rotationYaw = (float)(Math.atan2(dx, dz) * (180D / Math.PI)) + 180.0F;
         this.rotationPitch = (float)(Math.atan2(dy, horizDist) * (180D / Math.PI));
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
      }

      public Type getJutsuType() {
         return Type.RAITON;
      }

      protected void entityInit() {
         this.dataManager.register(SCALE, 2.0F);
         this.dataManager.register(LAUNCHED, false);
      }

      public float getEntityScale() {
         return (Float)this.dataManager.get(SCALE);
      }

      public boolean isLaunched() {
         return (Boolean)this.dataManager.get(LAUNCHED);
      }

      @SideOnly(Side.CLIENT)
      public AxisAlignedBB getRenderBoundingBox() {
         return super.getRenderBoundingBox().grow((double)100.0F);
      }

      public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotIncrements, boolean teleport) {
         this.interpTargetX = x;
         this.interpTargetY = y;
         this.interpTargetZ = z;
         this.interpTargetYaw = yaw;
         this.interpTargetPitch = pitch;
         this.interpSteps = posRotIncrements + 2;
      }

      public void onUpdate() {
         super.onUpdate();
         if (this.world.isRemote && this.interpSteps > 0) {
            double lx = this.posX + (this.interpTargetX - this.posX) / (double)this.interpSteps;
            double ly = this.posY + (this.interpTargetY - this.posY) / (double)this.interpSteps;
            double lz = this.posZ + (this.interpTargetZ - this.posZ) / (double)this.interpSteps;
            float lyaw = this.rotationYaw + MathHelper.wrapDegrees(this.interpTargetYaw - this.rotationYaw) / (float)this.interpSteps;
            float lpitch = this.rotationPitch + (this.interpTargetPitch - this.rotationPitch) / (float)this.interpSteps;
            --this.interpSteps;
            this.setPosition(lx, ly, lz);
            this.setRotation(lyaw, lpitch);
         }

         ++this.lifetime;
         if (this.lifetime <= 200 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (this.caster == null && this.casterUUID != null && !this.world.isRemote) {
               Entity e = this.world.getPlayerEntityByUUID(this.casterUUID);
               if (e instanceof EntityLivingBase) {
                  this.caster = (EntityLivingBase)e;
               }
            }

            if (!this.world.isRemote) {
               if (this.hasImpacted) {
                  this.doPostImpact();
               } else if (this.lifetime <= 60) {
                  this.doChargePhase();
               } else if (!(Boolean)this.dataManager.get(LAUNCHED)) {
                  this.doLaunch();
               } else {
                  this.doDivePhase();
               }
            }

         } else {
            this.setDead();
         }
      }

      private void doChargePhase() {
         float progress = (float)this.lifetime / 60.0F;
         float currentScale = 2.0F + progress * 8.0F;
         this.dataManager.set(SCALE, currentScale);
         if (this.caster != null) {
            Vec3d eyePos = this.caster.getPositionEyes(1.0F);
            Vec3d lookVec = this.caster.getLookVec();
            double horizLen = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
            if (horizLen > 0.05) {
               double normX = lookVec.x / horizLen;
               double normZ = lookVec.z / horizLen;
               Vec3d endPos = eyePos.add(lookVec.x * (double)40.0F, lookVec.y * (double)40.0F, lookVec.z * (double)40.0F);
               RayTraceResult rt = ProcedureUtils.objectEntityLookingAt(this.caster, (double)40.0F, (double)1.5F);
               if (rt != null && rt.entityHit != null) {
                  this.targetX = rt.entityHit.posX;
                  this.targetZ = rt.entityHit.posZ;
               } else {
                  RayTraceResult blockRay = this.world.rayTraceBlocks(eyePos, endPos, false, true, false);
                  if (blockRay != null && blockRay.typeOfHit == RayTraceResult.Type.BLOCK) {
                     this.targetX = blockRay.hitVec.x;
                     this.targetZ = blockRay.hitVec.z;
                  } else {
                     this.targetX = eyePos.x + normX * (double)40.0F;
                     this.targetZ = eyePos.z + normZ * (double)40.0F;
                  }
               }
            }

            this.groundY = this.world.getHeight((int)Math.floor(this.targetX), (int)Math.floor(this.targetZ));
            this.targetY = (double)this.groundY;
            double desiredX = this.targetX;
            double desiredY = (double)this.groundY + (double)60.0F;
            double desiredZ = this.targetZ;
            this.motionX = (desiredX - this.posX) * 0.3;
            this.motionY = (desiredY - this.posY) * 0.3;
            this.motionZ = (desiredZ - this.posZ) * 0.3;
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
         } else {
            this.motionX = (double)0.0F;
            this.motionY = (double)0.0F;
            this.motionZ = (double)0.0F;
         }

         this.faceTarget();
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, (int)(15.0F * progress), (double)8.0F, (double)2.0F, (double)8.0F, 0.01, (double)0.0F, 0.01, new int[]{-14540237, 30 + this.rand.nextInt(15)});
         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY - (double)3.0F, this.posZ, (int)(10.0F * progress), (double)6.0F, (double)1.5F, (double)6.0F, 0.01, (double)0.0F, 0.01, new int[]{-15066582, 25});
         if (this.lifetime % Math.max(2, 8 - this.lifetime / 8) == 0) {
            Particles.spawnParticle(this.world, Types.FLAME, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)10.0F, this.posY + (this.rand.nextDouble() - (double)0.5F) * (double)5.0F, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)10.0F, (int)(5.0F + progress * 20.0F), (double)1.0F + (double)progress * (double)3.0F, (double)1.0F + (double)progress * (double)2.0F, (double)1.0F + (double)progress * (double)3.0F, 0.2, 0.1, 0.2, new int[]{-6689025, 3});
         }

         if (this.lifetime % 8 == 0 && this.lifetime > 15) {
            int tendrilCount = 1 + (int)(progress * 2.0F);

            for(int t = 0; t < tendrilCount; ++t) {
               Vec3d top = new Vec3d(this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F, this.posY, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)4.0F);
               Vec3d bot = new Vec3d(this.targetX + (this.rand.nextDouble() - (double)0.5F) * (double)15.0F, (double)this.groundY + (double)0.5F, this.targetZ + (this.rand.nextDouble() - (double)0.5F) * (double)15.0F);
               EntityLightningArc.Base arc = new EntityLightningArc.Base(this.world, top, bot, -1073741569, 4, 0.15F);
               this.world.spawnEntity(arc);
            }
         }

         if (this.lifetime % 4 == 0) {
            this.spawnGroundMarker(this.targetX, (double)this.groundY + 0.2, this.targetZ, progress);
         }

         if (this.lifetime % 12 == 0) {
            SoundEvent elec = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
            if (elec != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, elec, SoundCategory.PLAYERS, 0.5F + progress * 1.5F, 0.5F + progress * 0.5F);
            }
         }

         if (this.lifetime % 20 == 0) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 0.5F + progress * 1.5F, 0.3F + progress * 0.4F);
         }

         if (this.lifetime == 30) {
            SoundEvent roar = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:dragon_roar"));
            if (roar != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, roar, SoundCategory.PLAYERS, 4.0F, 0.5F);
            } else {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 3.0F, 0.5F);
            }
         }

      }

      private void spawnGroundMarker(double mx, double my, double mz, float intensity) {
         int arcCount = (int)(4.0F + 5.0F * intensity);

         for(int a = 0; a < arcCount; ++a) {
            double angle1 = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double dist1 = this.rand.nextDouble() * (double)15.0F * 0.8;
            double angle2 = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double dist2 = this.rand.nextDouble() * (double)15.0F * 0.8;
            Vec3d from = new Vec3d(mx + Math.cos(angle1) * dist1, my + 0.1, mz + Math.sin(angle1) * dist1);
            Vec3d to = new Vec3d(mx + Math.cos(angle2) * dist2, my + 0.1 + this.rand.nextDouble() * 0.4, mz + Math.sin(angle2) * dist2);
            EntityLightningArc.Base arc = new EntityLightningArc.Base(this.world, from, to, -1073741569, 3, 0.1F);
            this.world.spawnEntity(arc);
         }

      }

      private void doLaunch() {
         this.dataManager.set(LAUNCHED, true);
         this.dataManager.set(SCALE, 10.0F);
         double dx = this.targetX - this.posX;
         double dy = this.targetY - this.posY;
         double dz = this.targetZ - this.posZ;
         double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (len > (double)0.0F) {
            this.motionX = dx / len * (double)3.0F;
            this.motionY = dy / len * (double)3.0F;
            this.motionZ = dz / len * (double)3.0F;
         }

         SoundEvent roar = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:dragon_roar"));
         if (roar != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, roar, SoundCategory.PLAYERS, 5.0F, 0.4F);
         }

         SoundEvent lShoot = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:lightning_shoot"));
         if (lShoot != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, lShoot, SoundCategory.PLAYERS, 3.0F, 0.6F);
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 3.0F, 0.5F);
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 80, (double)5.0F, (double)3.0F, (double)5.0F, (double)0.5F, 0.3, (double)0.5F, new int[]{-1, 2});
      }

      private void doDivePhase() {
         EntityPlayer nearestPlayer = null;
         double closestDist = (double)40.0F;

         for(EntityPlayer p : this.world.playerEntities) {
            if (p != this.caster && p.isEntityAlive() && !p.isSpectator()) {
               double d = (double)this.getDistance(p);
               if (d < closestDist) {
                  closestDist = d;
                  nearestPlayer = p;
               }
            }
         }

         if (nearestPlayer != null) {
            this.targetX = nearestPlayer.posX;
            this.targetZ = nearestPlayer.posZ;
         }

         this.groundY = this.world.getHeight((int)Math.floor(this.targetX), (int)Math.floor(this.targetZ));
         this.targetY = (double)this.groundY;
         double dx = this.targetX - this.posX;
         double dy = this.targetY - this.posY;
         double dz = this.targetZ - this.posZ;
         double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (len > (double)0.0F) {
            this.motionX = this.motionX * 0.85 + dx / len * (double)3.0F * 0.15;
            this.motionY = this.motionY * 0.85 + dy / len * (double)3.0F * 0.15;
            this.motionZ = this.motionZ * 0.85 + dz / len * (double)3.0F * 0.15;
         }

         this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
         double horizSpeed = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * (180D / Math.PI)) + 180.0F;
         this.rotationPitch = (float)(Math.atan2(this.motionY, horizSpeed) * (180D / Math.PI));
         if (this.lifetime % 2 == 0) {
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 15, (double)2.0F, (double)2.0F, (double)2.0F, 0.15, 0.1, 0.15, new int[]{-6689025, 5});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 8, (double)1.5F, (double)1.5F, (double)1.5F, 0.2, 0.15, 0.2, new int[]{-1, 3});
         }

         if (this.lifetime % 3 == 0) {
            this.spawnGroundMarker(this.targetX, (double)this.groundY + 0.2, this.targetZ, 1.0F);
         }

         if (this.lifetime % 8 == 0) {
            SoundEvent elec = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
            if (elec != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, elec, SoundCategory.PLAYERS, 2.0F, 0.8F + this.rand.nextFloat() * 0.4F);
            }
         }

         if (!this.hasImpacted && this.posY <= (double)(this.groundY + 3)) {
            this.onImpact();
         }

      }

      private boolean isHydrificationActive(EntityLivingBase target) {
         int hydId = target.getEntityData().getInteger("HydrificationEntityIdKey");
         if (hydId == 0) {
            return false;
         } else {
            Entity hydEntity = target.world.getEntityByID(hydId);
            return hydEntity != null && hydEntity.isEntityAlive();
         }
      }

      private void onImpact() {
         if (!this.hasImpacted && !this.world.isRemote) {
            this.hasImpacted = true;
            this.impactTick = 0;
            float totalDmg = 150.0F;

            for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)15.0F, this.posY - (double)5.0F, this.posZ - (double)15.0F, this.posX + (double)15.0F, this.posY + (double)10.0F, this.posZ + (double)15.0F), (e) -> e != this.caster && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
               float cappedDmg = totalDmg;
               if (!(target instanceof EntityPlayer)) {
                  cappedDmg = Math.min(totalDmg, 100.0F);
               }

               target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.caster), cappedDmg);
               target.hurtResistantTime = 0;
               target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1, false, true));
               target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 20, 0, false, true));
               double kbX = target.posX - this.posX;
               double kbZ = target.posZ - this.posZ;
               double kbDist = Math.sqrt(kbX * kbX + kbZ * kbZ);
               if (kbDist > 0.01) {
                  target.motionX += kbX / kbDist * (double)1.5F;
                  target.motionY += (double)0.5F;
                  target.motionZ += kbZ / kbDist * (double)1.5F;
                  target.velocityChanged = true;
               }
            }

            EntityLightningArc.Base centerBolt = new EntityLightningArc.Base(this.world, new Vec3d(this.posX, this.posY, this.posZ), new Vec3d(this.posX, this.posY + (double)40.0F, this.posZ), -1073741569, 12, 0.5F, 0.4F);
            this.world.spawnEntity(centerBolt);
            this.world.addWeatherEffect(new EntityLightningBolt(this.world, this.posX, this.posY, this.posZ, true));
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 4.0F, 0.4F);
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 3.0F, 0.6F);
            SoundEvent elec = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
            if (elec != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, elec, SoundCategory.PLAYERS, 3.0F, 0.3F);
            }

            this.dataManager.set(SCALE, 0.0F);
         }
      }

      private void doPostImpact() {
         ++this.impactTick;
         if (this.impactTick == 2) {
            for(int i = 0; i < 3; ++i) {
               double angle = (double)i / (double)3.0F * Math.PI * (double)2.0F + this.rand.nextDouble() * 0.4;
               double lean = 0.3 + this.rand.nextDouble() * 0.3;
               double height = (double)15.0F + this.rand.nextDouble() * (double)15.0F;
               EntityLightningArc.Base arc = new EntityLightningArc.Base(this.world, new Vec3d(this.posX, this.posY, this.posZ), new Vec3d(this.posX + Math.cos(angle) * lean * height, this.posY + height, this.posZ + Math.sin(angle) * lean * height), -1073741569, 10, 0.5F, 0.25F);
               this.world.spawnEntity(arc);
            }

            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 3.0F, 0.7F);
         }

         if (this.impactTick == 5) {
            for(int i = 0; i < 3; ++i) {
               double angle = (double)i / (double)3.0F * Math.PI * (double)2.0F + (Math.PI / 3D) + this.rand.nextDouble() * 0.4;
               double lean = 0.4 + this.rand.nextDouble() * 0.3;
               double height = (double)10.0F + this.rand.nextDouble() * (double)15.0F;
               EntityLightningArc.Base arc = new EntityLightningArc.Base(this.world, new Vec3d(this.posX, this.posY, this.posZ), new Vec3d(this.posX + Math.cos(angle) * lean * height, this.posY + height, this.posZ + Math.sin(angle) * lean * height), -1073741569, 8, 0.5F, 0.2F);
               this.world.spawnEntity(arc);
            }
         }

         if (this.impactTick == 8) {
            for(int i = 0; i < 6; ++i) {
               double angle = (double)i / (double)6.0F * Math.PI * (double)2.0F + this.rand.nextDouble() * 0.3;
               double arcLen = (double)15.0F * (0.6 + this.rand.nextDouble() * (double)0.5F);
               EntityLightningArc.Base arc = new EntityLightningArc.Base(this.world, new Vec3d(this.posX, this.posY + 0.3, this.posZ), new Vec3d(this.posX + Math.cos(angle) * arcLen, this.posY + 0.2 + this.rand.nextDouble() * (double)0.5F, this.posZ + Math.sin(angle) * arcLen), -1073741569, 8, 0.3F, 0.12F);
               this.world.spawnEntity(arc);
            }

            SoundEvent elec = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
            if (elec != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, elec, SoundCategory.PLAYERS, 2.5F, 0.5F);
            }
         }

         if (this.impactTick == 11) {
            for(int i = 0; i < 6; ++i) {
               double angle = (double)i / (double)6.0F * Math.PI * (double)2.0F + (Math.PI / 6D) + this.rand.nextDouble() * 0.3;
               double arcLen = (double)15.0F * (0.7 + this.rand.nextDouble() * (double)0.5F);
               EntityLightningArc.Base arc = new EntityLightningArc.Base(this.world, new Vec3d(this.posX, this.posY + 0.3, this.posZ), new Vec3d(this.posX + Math.cos(angle) * arcLen, this.posY + 0.2 + this.rand.nextDouble() * (double)0.5F, this.posZ + Math.sin(angle) * arcLen), -1073741569, 6, 0.3F, 0.1F);
               this.world.spawnEntity(arc);
            }
         }

         if (this.impactTick == 14) {
            for(int i = 0; i < 5; ++i) {
               double a1 = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double d1 = this.rand.nextDouble() * (double)15.0F;
               double a2 = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double d2 = this.rand.nextDouble() * (double)15.0F;
               EntityLightningArc.Base arc = new EntityLightningArc.Base(this.world, new Vec3d(this.posX + Math.cos(a1) * d1, this.posY + 0.2, this.posZ + Math.sin(a1) * d1), new Vec3d(this.posX + Math.cos(a2) * d2, this.posY + 0.3 + this.rand.nextDouble() * (double)1.0F, this.posZ + Math.sin(a2) * d2), -1073741569, 5, 0.2F, 0.08F);
               this.world.spawnEntity(arc);
            }
         }

         if (this.impactTick == 16) {
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)0.5F, this.posZ, 30, (double)7.5F, (double)0.5F, (double)7.5F, 0.05, 0.08, 0.05, new int[]{-869059772, 20});
         }

         if (this.impactTick >= 20) {
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
         this.lifetime = compound.getInteger("kirinLifetime");
         this.power = compound.getFloat("kirinPower");
         this.hasImpacted = compound.getBoolean("kirinImpacted");
         this.targetX = compound.getDouble("kirinTargetX");
         this.targetY = compound.getDouble("kirinTargetY");
         this.targetZ = compound.getDouble("kirinTargetZ");
         this.groundY = compound.getInteger("kirinGroundY");
         if (compound.hasKey("kirinCasterUUID")) {
            try {
               this.casterUUID = UUID.fromString(compound.getString("kirinCasterUUID"));
            } catch (IllegalArgumentException var3) {
            }
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("kirinLifetime", this.lifetime);
         compound.setFloat("kirinPower", this.power);
         compound.setBoolean("kirinImpacted", this.hasImpacted);
         compound.setDouble("kirinTargetX", this.targetX);
         compound.setDouble("kirinTargetY", this.targetY);
         compound.setDouble("kirinTargetZ", this.targetZ);
         compound.setInteger("kirinGroundY", this.groundY);
         if (this.casterUUID != null) {
            compound.setString("kirinCasterUUID", this.casterUUID.toString());
         }

      }

      static {
         SCALE = EntityDataManager.createKey(EntityKirinStorm.class, DataSerializers.FLOAT);
         LAUNCHED = EntityDataManager.createKey(EntityKirinStorm.class, DataSerializers.BOOLEAN);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (entity.world.isRemote) {
            return false;
         } else {
            long now = entity.world.getTotalWorldTime();
            UUID uid = entity.getUniqueID();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 600L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               RayTraceResult rt = ProcedureUtils.objectEntityLookingAt(entity, (double)40.0F, (double)1.5F);
               double targetX;
               double targetY;
               double targetZ;
               if (rt != null && rt.entityHit != null) {
                  targetX = rt.entityHit.posX;
                  targetY = rt.entityHit.posY;
                  targetZ = rt.entityHit.posZ;
               } else {
                  Vec3d eyePos = entity.getPositionEyes(1.0F);
                  Vec3d lookVec = entity.getLookVec();
                  Vec3d endPos = eyePos.add(lookVec.x * (double)40.0F, lookVec.y * (double)40.0F, lookVec.z * (double)40.0F);
                  RayTraceResult blockRay = entity.world.rayTraceBlocks(eyePos, endPos, false, true, false);
                  if (blockRay != null && blockRay.typeOfHit == RayTraceResult.Type.BLOCK) {
                     targetX = blockRay.hitVec.x;
                     targetY = blockRay.hitVec.y;
                     targetZ = blockRay.hitVec.z;
                  } else {
                     targetX = endPos.x;
                     targetY = endPos.y;
                     targetZ = endPos.z;
                  }
               }

               EntityKirinStorm storm = new EntityKirinStorm(entity.world, entity, targetX, targetY, targetZ, power);
               entity.world.spawnEntity(storm);
               entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 1.5F, 0.4F);
               SoundEvent elec = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
               if (elec != null) {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, elec, SoundCategory.PLAYERS, 1.0F, 0.6F);
               }

               return true;
            }
         }
      }

      public float getBasePower() {
         return 1.0F;
      }

      public float getPowerupDelay() {
         return 30.0F;
      }

      public float getMaxPower() {
         return 8.0F;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class KirinRenderer extends Render<EntityKirinStorm> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod", "textures/dragon_lightning.png");
      private static final ResourceLocation TEXTURE_ELECTRIC = new ResourceLocation("narutomod", "textures/electric_armor.png");
      private final ModelKirinDragon model = new ModelKirinDragon();

      public KirinRenderer(RenderManager rm) {
         super(rm);
         this.shadowSize = 0.0F;
      }

      public void doRender(EntityKirinStorm entity, double x, double y, double z, float yaw, float pt) {
         float age = (float)entity.ticksExisted + pt;
         float scale = entity.getEntityScale();
         float alpha = Math.min(age / 20.0F, 1.0F);
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)x, (float)y + scale, (float)z);
         float renderYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * pt;
         float renderPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * pt;
         GlStateManager.rotate(renderYaw, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(renderPitch, 1.0F, 0.0F, 0.0F);
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.enableBlend();
         GlStateManager.alphaFunc(516, 0.001F);
         GlStateManager.disableCull();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         this.model.setRotationAngles(0.0F, 0.0F, age, 0.0F, 0.0F, 0.0625F, entity);
         this.bindEntityTexture(entity);
         GlStateManager.color(1.0F, 1.0F, 1.0F, alpha * 0.5F);
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         this.bindTexture(TEXTURE_ELECTRIC);
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         GlStateManager.translate(age * 0.01F, age * 0.01F, 0.0F);
         GlStateManager.matrixMode(5888);
         GlStateManager.color(1.0F, 1.0F, 1.0F, alpha * 0.5F);
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.065625F);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         GlStateManager.matrixMode(5888);
         this.bindEntityTexture(entity);
         GlStateManager.color(0.0F, 0.0F, 1.0F, alpha * 0.3F);
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.06875F);
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }

      protected ResourceLocation getEntityTexture(EntityKirinStorm entity) {
         return TEXTURE;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelKirinDragon extends ModelBase {
      private final ModelRenderer head;
      private final ModelRenderer teethUpper;
      private final ModelRenderer flair;
      private final ModelRenderer bone;
      private final ModelRenderer bone2;
      private final ModelRenderer bone3;
      private final ModelRenderer jaw;
      private final ModelRenderer teethLower;
      private final ModelRenderer hornRight;
      private final ModelRenderer hornRight0;
      private final ModelRenderer hornRight1;
      private final ModelRenderer hornRight2;
      private final ModelRenderer hornRight3;
      private final ModelRenderer hornRight4;
      private final ModelRenderer hornLeft;
      private final ModelRenderer hornLeft0;
      private final ModelRenderer hornLeft1;
      private final ModelRenderer hornLeft2;
      private final ModelRenderer hornLeft3;
      private final ModelRenderer hornLeft4;
      private final ModelRenderer[] whiskerLeft = new ModelRenderer[6];
      private final ModelRenderer[] whiskerRight = new ModelRenderer[6];
      private final ModelRenderer[] spine = new ModelRenderer[10];
      private final ModelRenderer eyes;
      private static final int SPINE_COUNT = 10;

      public ModelKirinDragon() {
         this.textureWidth = 128;
         this.textureHeight = 128;
         (this.head = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.head.cubeList.add(new ModelBox(this.head, 64, 0, -6.0F, 6.0F, -26.0F, 12, 5, 16, 1.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 0, 0, -8.0F, -1.0F, -11.0F, 16, 16, 16, 1.0F, false));
         this.head.cubeList.add(new ModelBox(this.head, 32, 32, 2.0F, 4.0F, -28.0F, 4, 4, 6, 0.0F, true));
         this.head.cubeList.add(new ModelBox(this.head, 32, 32, -6.0F, 4.0F, -28.0F, 4, 4, 6, 0.0F, false));
         (this.teethUpper = new ModelRenderer(this)).setRotationPoint(0.0F, 24.0F, 0.0F);
         this.head.addChild(this.teethUpper);
         this.teethUpper.cubeList.add(new ModelBox(this.teethUpper, 0, 52, -6.0F, -12.0F, -26.0F, 12, 3, 16, 0.5F, false));
         (this.flair = new ModelRenderer(this)).setRotationPoint(0.0F, -2.0F, -12.0F);
         this.head.addChild(this.flair);
         (this.bone = new ModelRenderer(this)).setRotationPoint(9.0F, 9.0F, 0.0F);
         this.flair.addChild(this.bone);
         this.setRotationAngle(this.bone, 0.0F, -0.7854F, 0.0F);
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 52, 0.0F, -8.0F, 0.0F, 10, 16, 0, 0.0F, false));
         this.bone.cubeList.add(new ModelBox(this.bone, 0, 52, -2.0F, -12.0F, 2.0F, 10, 16, 0, 0.0F, false));
         (this.bone2 = new ModelRenderer(this)).setRotationPoint(-9.0F, 9.0F, 0.0F);
         this.flair.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.0F, 0.7854F, 0.0F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, 52, -10.0F, -8.0F, 0.0F, 10, 16, 0, 0.0F, true));
         this.bone2.cubeList.add(new ModelBox(this.bone2, 0, 52, -8.0F, -12.0F, 2.0F, 10, 16, 0, 0.0F, true));
         (this.bone3 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.flair.addChild(this.bone3);
         this.setRotationAngle(this.bone3, -0.8727F, 0.0F, 0.0F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 84, 42, -8.0F, -10.0F, 0.0F, 16, 10, 0, 0.0F, false));
         (this.jaw = new ModelRenderer(this)).setRotationPoint(0.0F, 11.0F, -9.0F);
         this.head.addChild(this.jaw);
         this.setRotationAngle(this.jaw, 0.7854F, 0.0F, 0.0F);
         this.jaw.cubeList.add(new ModelBox(this.jaw, 64, 22, -6.0F, 0.0F, -16.75F, 12, 4, 16, 1.0F, false));
         (this.teethLower = new ModelRenderer(this)).setRotationPoint(0.0F, 13.0F, 9.0F);
         this.jaw.addChild(this.teethLower);
         this.teethLower.cubeList.add(new ModelBox(this.teethLower, 42, 42, -6.0F, -16.0F, -25.75F, 12, 2, 16, 0.5F, false));
         (this.hornRight = new ModelRenderer(this)).setRotationPoint(-6.0F, -2.0F, -13.0F);
         this.head.addChild(this.hornRight);
         this.setRotationAngle(this.hornRight, 0.0873F, -0.5236F, 0.0F);
         this.hornRight.cubeList.add(new ModelBox(this.hornRight, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, false));
         (this.hornRight0 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornRight.addChild(this.hornRight0);
         this.setRotationAngle(this.hornRight0, 0.0873F, 0.0873F, 0.0F);
         this.hornRight0.cubeList.add(new ModelBox(this.hornRight0, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.8F, false));
         (this.hornRight1 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornRight0.addChild(this.hornRight1);
         this.setRotationAngle(this.hornRight1, 0.0873F, 0.0873F, 0.0F);
         this.hornRight1.cubeList.add(new ModelBox(this.hornRight1, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.6F, false));
         (this.hornRight2 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornRight1.addChild(this.hornRight2);
         this.setRotationAngle(this.hornRight2, 0.0873F, 0.0873F, 0.0F);
         this.hornRight2.cubeList.add(new ModelBox(this.hornRight2, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.4F, false));
         (this.hornRight3 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornRight2.addChild(this.hornRight3);
         this.setRotationAngle(this.hornRight3, 0.0873F, 0.0873F, 0.0F);
         this.hornRight3.cubeList.add(new ModelBox(this.hornRight3, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.2F, false));
         (this.hornRight4 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornRight3.addChild(this.hornRight4);
         this.setRotationAngle(this.hornRight4, 0.0873F, 0.0873F, 0.0F);
         this.hornRight4.cubeList.add(new ModelBox(this.hornRight4, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.0F, false));
         (this.hornLeft = new ModelRenderer(this)).setRotationPoint(6.0F, -2.0F, -13.0F);
         this.head.addChild(this.hornLeft);
         this.setRotationAngle(this.hornLeft, 0.0873F, 0.5236F, 0.0F);
         this.hornLeft.cubeList.add(new ModelBox(this.hornLeft, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, true));
         (this.hornLeft0 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornLeft.addChild(this.hornLeft0);
         this.setRotationAngle(this.hornLeft0, 0.0873F, -0.0873F, 0.0F);
         this.hornLeft0.cubeList.add(new ModelBox(this.hornLeft0, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.8F, true));
         (this.hornLeft1 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornLeft0.addChild(this.hornLeft1);
         this.setRotationAngle(this.hornLeft1, 0.0873F, -0.0873F, 0.0F);
         this.hornLeft1.cubeList.add(new ModelBox(this.hornLeft1, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.6F, true));
         (this.hornLeft2 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornLeft1.addChild(this.hornLeft2);
         this.setRotationAngle(this.hornLeft2, 0.0873F, -0.0873F, 0.0F);
         this.hornLeft2.cubeList.add(new ModelBox(this.hornLeft2, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.4F, true));
         (this.hornLeft3 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornLeft2.addChild(this.hornLeft3);
         this.setRotationAngle(this.hornLeft3, 0.0873F, -0.0873F, 0.0F);
         this.hornLeft3.cubeList.add(new ModelBox(this.hornLeft3, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.2F, true));
         (this.hornLeft4 = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 7.0F);
         this.hornLeft3.addChild(this.hornLeft4);
         this.setRotationAngle(this.hornLeft4, 0.0873F, -0.0873F, 0.0F);
         this.hornLeft4.cubeList.add(new ModelBox(this.hornLeft4, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.0F, true));
         (this.whiskerLeft[0] = new ModelRenderer(this)).setRotationPoint(6.0F, 6.0F, -24.0F);
         this.head.addChild(this.whiskerLeft[0]);
         this.setRotationAngle(this.whiskerLeft[0], 0.0F, 1.0472F, 0.0F);
         this.whiskerLeft[0].cubeList.add(new ModelBox(this.whiskerLeft[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, true));
         (this.whiskerLeft[1] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerLeft[0].addChild(this.whiskerLeft[1]);
         this.setRotationAngle(this.whiskerLeft[1], -0.0873F, -0.1745F, 0.0F);
         this.whiskerLeft[1].cubeList.add(new ModelBox(this.whiskerLeft[1], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.7F, true));
         (this.whiskerLeft[2] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerLeft[1].addChild(this.whiskerLeft[2]);
         this.setRotationAngle(this.whiskerLeft[2], -0.0873F, -0.1745F, 0.0F);
         this.whiskerLeft[2].cubeList.add(new ModelBox(this.whiskerLeft[2], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.6F, true));
         (this.whiskerLeft[3] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerLeft[2].addChild(this.whiskerLeft[3]);
         this.setRotationAngle(this.whiskerLeft[3], -0.0873F, -0.1745F, 0.0F);
         this.whiskerLeft[3].cubeList.add(new ModelBox(this.whiskerLeft[3], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.5F, true));
         (this.whiskerLeft[4] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerLeft[3].addChild(this.whiskerLeft[4]);
         this.setRotationAngle(this.whiskerLeft[4], -0.0873F, -0.1745F, 0.0F);
         this.whiskerLeft[4].cubeList.add(new ModelBox(this.whiskerLeft[4], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.4F, true));
         (this.whiskerLeft[5] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerLeft[4].addChild(this.whiskerLeft[5]);
         this.setRotationAngle(this.whiskerLeft[5], -0.0873F, -0.1745F, 0.0F);
         this.whiskerLeft[5].cubeList.add(new ModelBox(this.whiskerLeft[5], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.2F, true));
         (this.whiskerRight[0] = new ModelRenderer(this)).setRotationPoint(-6.0F, 6.0F, -24.0F);
         this.head.addChild(this.whiskerRight[0]);
         this.setRotationAngle(this.whiskerRight[0], 0.0F, -1.0472F, 0.0F);
         this.whiskerRight[0].cubeList.add(new ModelBox(this.whiskerRight[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, false));
         (this.whiskerRight[1] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerRight[0].addChild(this.whiskerRight[1]);
         this.setRotationAngle(this.whiskerRight[1], -0.0873F, 0.1745F, 0.0F);
         this.whiskerRight[1].cubeList.add(new ModelBox(this.whiskerRight[1], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.7F, false));
         (this.whiskerRight[2] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerRight[1].addChild(this.whiskerRight[2]);
         this.setRotationAngle(this.whiskerRight[2], -0.0873F, 0.1745F, 0.0F);
         this.whiskerRight[2].cubeList.add(new ModelBox(this.whiskerRight[2], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.6F, false));
         (this.whiskerRight[3] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerRight[2].addChild(this.whiskerRight[3]);
         this.setRotationAngle(this.whiskerRight[3], -0.0873F, 0.1745F, 0.0F);
         this.whiskerRight[3].cubeList.add(new ModelBox(this.whiskerRight[3], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.5F, false));
         (this.whiskerRight[4] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerRight[3].addChild(this.whiskerRight[4]);
         this.setRotationAngle(this.whiskerRight[4], -0.0873F, 0.1745F, 0.0F);
         this.whiskerRight[4].cubeList.add(new ModelBox(this.whiskerRight[4], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.4F, false));
         (this.whiskerRight[5] = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 6.0F);
         this.whiskerRight[4].addChild(this.whiskerRight[5]);
         this.setRotationAngle(this.whiskerRight[5], -0.0873F, 0.1745F, 0.0F);
         this.whiskerRight[5].cubeList.add(new ModelBox(this.whiskerRight[5], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.2F, false));

         for(int i = 0; i < 10; ++i) {
            this.spine[i] = new ModelRenderer(this);
            this.spine[i].cubeList.add(new ModelBox(this.spine[i], 0, 32, -5.0F, -4.5F, 0.0F, 10, 10, 10, 2.0F, false));
            this.spine[i].cubeList.add(new ModelBox(this.spine[i], 48, 0, -1.0F, -10.5F, 2.0F, 2, 4, 6, 1.0F, false));
            if (i == 0) {
               this.spine[i].setRotationPoint(0.0F, 6.5F, 7.0F);
            } else {
               this.spine[i].setRotationPoint(0.0F, 0.0F, 11.0F);
               this.spine[i - 1].addChild(this.spine[i]);
            }
         }

         (this.eyes = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, 0.0F);
         this.eyes.cubeList.add(new ModelBox(this.eyes, 18, 20, -6.6F, 2.6F, -12.15F, 3, 2, 0, 0.0F, false));
         this.eyes.cubeList.add(new ModelBox(this.eyes, 18, 20, 3.6F, 2.6F, -12.15F, 3, 2, 0, 0.0F, true));
      }

      public void render(Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
         this.head.render(f5);
         this.spine[0].render(f5);
         this.eyes.render(f5);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }

      public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
         super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
         EntityKirinStorm kirin = (EntityKirinStorm)entityIn;
         this.jaw.rotateAngleX = kirin.isLaunched() ? 0.5236F : 0.2F;
         float whiskerWave = ageInTicks * 0.15F;

         for(int i = 2; i < 6; ++i) {
            this.whiskerLeft[i].rotateAngleZ = MathHelper.sin(whiskerWave + (float)i * 0.5F) * 0.2618F;
            this.whiskerRight[i].rotateAngleZ = -MathHelper.sin(whiskerWave + (float)i * 0.5F) * 0.2618F;
         }

         int visibleSpines = kirin.isLaunched() ? 10 : Math.min(10, (int)(ageInTicks / 5.0F));
         float spineWave = ageInTicks * 0.1F;

         for(int i = 0; i < 10; ++i) {
            this.spine[i].showModel = i < visibleSpines;
            this.spine[i].rotateAngleY = MathHelper.sin(spineWave + (float)i * 0.8F) * 0.15F;
            this.spine[i].rotateAngleX = MathHelper.cos(spineWave + (float)i * 0.6F) * 0.05F;
         }

      }
   }
}
