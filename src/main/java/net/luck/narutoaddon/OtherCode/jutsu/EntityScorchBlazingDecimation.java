
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
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
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
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
import net.narutomod.procedure.ProcedureUtils;

import java.util.*;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityScorchBlazingDecimation extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 310;
   public static final int ENTITYID_ZONE = 311;

   public EntityScorchBlazingDecimation(ElementsInfTsukAddon instance) {
      super(instance, 940);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityScorchOrb.class).id(new ResourceLocation("inftsukaddon", "scorch_blazing_decimation"), 310).name("inftsuk_scorch_blazing_decimation").tracker(64, 1, true).build());
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityInfernoZone.class).id(new ResourceLocation("inftsukaddon", "scorch_inferno_zone"), 311).name("inftsuk_scorch_inferno_zone").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityScorchOrb.class, ScorchOrbRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(EntityInfernoZone.class, InvisibleRenderer::new);
   }

   public static class EntityScorchOrb extends Entity implements ItemJutsu.IJutsu {
      private static final Set<UUID> zoneSpawned = new HashSet();
      private int lifetime;
      private static final int MAX_LIFETIME = 120;
      private static final int LAUNCH_DELAY = 60;
      private float power;
      private float orbScale;
      private int orbIndex;
      private boolean launched;
      private EntityLivingBase caster;
      private UUID casterUUID;
      private Vec3d targetPos;
      private Entity targetEntity;

      public EntityScorchOrb(World world) {
         super(world);
         this.lifetime = 0;
         this.power = 1.0F;
         this.orbScale = 0.3F;
         this.orbIndex = 0;
         this.launched = false;
         this.setSize(0.5F, 0.5F);
         this.noClip = true;
         this.isImmuneToFire = true;
      }

      public EntityScorchOrb(World world, EntityLivingBase caster, float power, int orbIndex) {
         this(world);
         this.caster = caster;
         this.casterUUID = caster.getUniqueID();
         this.power = power;
         this.orbIndex = orbIndex;
      }

      public Type getJutsuType() {
         return Type.KATON;
      }

      protected void entityInit() {
      }

      public void onUpdate() {
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         ++this.lifetime;
         if (this.lifetime <= 120 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (this.caster == null && this.casterUUID != null && !this.world.isRemote) {
               Entity e = this.world.getPlayerEntityByUUID(this.casterUUID);
               if (e instanceof EntityLivingBase) {
                  this.caster = (EntityLivingBase)e;
               }
            }

            if (!this.world.isRemote) {
               if (!this.launched && this.lifetime < 60) {
                  this.orbitAroundCaster();
                  if (this.orbScale < 0.8F) {
                     this.orbScale = Math.min(0.8F, this.orbScale * 1.025F);
                  }
               } else if (!this.launched && this.lifetime >= 60) {
                  this.launched = true;
                  this.acquireTarget();
               }

               if (this.launched) {
                  this.flyTowardTarget();
                  this.checkHit();
               }

               this.spawnOrbParticles();
            }

         } else {
            this.setDead();
         }
      }

      private void orbitAroundCaster() {
         if (this.caster != null) {
            double baseAngle = (double)this.lifetime * 0.1 + (double)this.orbIndex * 1.2566370614359172;
            double radius = (double)2.0F;
            double bobY = Math.sin((double)this.lifetime * 0.15 + (double)this.orbIndex) * 0.3;
            double ox = this.caster.posX + Math.cos(baseAngle) * radius;
            double oy = this.caster.posY + (double)this.caster.getEyeHeight() + bobY;
            double oz = this.caster.posZ + Math.sin(baseAngle) * radius;
            this.setPosition(ox, oy, oz);
         }
      }

      private void acquireTarget() {
         if (this.caster != null) {
            RayTraceResult rt = ProcedureUtils.objectEntityLookingAt(this.caster, (double)30.0F, (double)1.5F);
            if (rt != null && rt.entityHit != null && rt.entityHit instanceof EntityLivingBase) {
               this.targetEntity = rt.entityHit;
               this.targetPos = rt.entityHit.getPositionVector().add((double)0.0F, (double)rt.entityHit.height / (double)2.0F, (double)0.0F);
            } else {
               Vec3d eyePos = this.caster.getPositionEyes(1.0F);
               Vec3d lookVec = this.caster.getLookVec();
               Vec3d endPos = eyePos.add(lookVec.x * (double)30.0F, lookVec.y * (double)30.0F, lookVec.z * (double)30.0F);
               RayTraceResult blockRay = this.world.rayTraceBlocks(eyePos, endPos, false, true, false);
               if (blockRay != null && blockRay.typeOfHit == RayTraceResult.Type.BLOCK) {
                  this.targetPos = blockRay.hitVec;
               } else {
                  this.targetPos = endPos;
               }
            }

            SoundEvent launchSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
            if (launchSound != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, launchSound, SoundCategory.PLAYERS, 1.5F, 0.8F);
            } else {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.8F, 1.2F);
            }

         }
      }

      private void flyTowardTarget() {
         if (this.targetPos == null) {
            this.setDead();
         } else {
            if (this.targetEntity != null && this.targetEntity.isEntityAlive()) {
               this.targetPos = this.targetEntity.getPositionVector().add((double)0.0F, (double)this.targetEntity.height / (double)2.0F, (double)0.0F);
            }

            Vec3d current = this.getPositionVector();
            Vec3d direction = this.targetPos.subtract(current);
            double dist = direction.length();
            if (dist < 0.8) {
               this.onOrbImpact();
            } else {
               Vec3d normalDir = direction.normalize();
               Vec3d currentVel = new Vec3d(this.motionX, this.motionY, this.motionZ);
               double speed = 0.9;
               if (currentVel.length() > 0.01) {
                  Vec3d blended = (new Vec3d(normalDir.x * 0.7 + currentVel.normalize().x * 0.3, normalDir.y * 0.7 + currentVel.normalize().y * 0.3, normalDir.z * 0.7 + currentVel.normalize().z * 0.3)).normalize();
                  this.motionX = blended.x * speed;
                  this.motionY = blended.y * speed;
                  this.motionZ = blended.z * speed;
               } else {
                  this.motionX = normalDir.x * speed;
                  this.motionY = normalDir.y * speed;
                  this.motionZ = normalDir.z * speed;
               }

               this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            }
         }
      }

      private void checkHit() {
         List<EntityLivingBase> nearby = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(0.8), (e) -> e != this.caster && e.isEntityAlive() && ItemJutsu.canTarget(e));
         if (!nearby.isEmpty()) {
            this.onOrbImpact();
         }

      }

      private void onOrbImpact() {
         if (!this.world.isRemote) {
            float totalDmg = 16.0F + this.power * 8.0F;

            for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)2.0F, this.posY - (double)2.0F, this.posZ - (double)2.0F, this.posX + (double)2.0F, this.posY + (double)2.0F, this.posZ + (double)2.0F), (e) -> e != this.caster && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
               float cappedDmg = totalDmg;
               if (!(target instanceof EntityPlayer)) {
                  cappedDmg = Math.min(totalDmg, 100.0F);
               }

               target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.caster), cappedDmg);
               target.hurtResistantTime = 0;
               target.setFire(4);
            }

            if (this.casterUUID != null && !zoneSpawned.contains(this.casterUUID)) {
               zoneSpawned.add(this.casterUUID);
               EntityInfernoZone zone = new EntityInfernoZone(this.world, this.caster, this.posX, this.posY, this.posZ, this.power, 0);
               this.world.spawnEntity(zone);
            }

            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 30, 0.3, 0.3, 0.3, 0.2, 0.2, 0.2, new int[]{-39424, 6});
            Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 15, 0.2, 0.2, 0.2, 0.15, 0.15, 0.15, new int[]{-21965, 4});
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.8F, 1.2F);
            this.setDead();
         }
      }

      private void spawnOrbParticles() {
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY, this.posZ, 2, 0.15, 0.15, 0.15, (double)0.0F, 0.02, (double)0.0F, new int[]{-30720, 5});
         if (this.lifetime % 2 == 0) {
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + 0.1, this.posZ, 1, 0.1, 0.1, 0.1, (double)0.0F, 0.03, (double)0.0F, new int[]{-855677440, 8});
         }

      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.lifetime = compound.getInteger("sbdLifetime");
         this.power = compound.getFloat("sbdPower");
         this.orbIndex = compound.getInteger("sbdOrbIndex");
         this.orbScale = compound.getFloat("sbdOrbScale");
         this.launched = compound.getBoolean("sbdLaunched");
         if (compound.hasKey("sbdCasterUUID")) {
            try {
               this.casterUUID = UUID.fromString(compound.getString("sbdCasterUUID"));
            } catch (IllegalArgumentException var3) {
            }
         }

         if (compound.hasKey("sbdTargetX")) {
            this.targetPos = new Vec3d(compound.getDouble("sbdTargetX"), compound.getDouble("sbdTargetY"), compound.getDouble("sbdTargetZ"));
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("sbdLifetime", this.lifetime);
         compound.setFloat("sbdPower", this.power);
         compound.setInteger("sbdOrbIndex", this.orbIndex);
         compound.setFloat("sbdOrbScale", this.orbScale);
         compound.setBoolean("sbdLaunched", this.launched);
         if (this.casterUUID != null) {
            compound.setString("sbdCasterUUID", this.casterUUID.toString());
         }

         if (this.targetPos != null) {
            compound.setDouble("sbdTargetX", this.targetPos.x);
            compound.setDouble("sbdTargetY", this.targetPos.y);
            compound.setDouble("sbdTargetZ", this.targetPos.z);
         }

      }

      public float getOrbScale() {
         return this.orbScale;
      }
   }

   public static class EntityInfernoZone extends Entity implements ItemJutsu.IJutsu {
      private static final double ZONE_RADIUS = (double)10.0F;
      private static final int ZONE_DURATION = 100;
      private int lifetime;
      private int activationDelay;
      private float power;
      private EntityLivingBase caster;
      private UUID casterUUID;
      private boolean initialBurstDone;

      public EntityInfernoZone(World world) {
         super(world);
         this.lifetime = 0;
         this.activationDelay = 0;
         this.power = 1.0F;
         this.initialBurstDone = false;
         this.setSize(0.5F, 0.5F);
         this.noClip = true;
         this.setInvisible(true);
         this.isImmuneToFire = true;
      }

      public EntityInfernoZone(World world, EntityLivingBase caster, double x, double y, double z, float power, int delay) {
         this(world);
         this.caster = caster;
         this.casterUUID = caster.getUniqueID();
         this.power = power;
         this.activationDelay = delay;

         BlockPos groundPos;
         for(groundPos = new BlockPos(x, y, z); groundPos.getY() > 0 && world.isAirBlock(groundPos); groundPos = groundPos.down()) {
         }

         this.setPosition(x, (double)groundPos.getY() + (double)1.0F, z);
      }

      public Type getJutsuType() {
         return Type.KATON;
      }

      protected void entityInit() {
      }

      public void onUpdate() {
         super.onUpdate();
         if (!this.world.isBlockLoaded(new BlockPos(this))) {
            this.setDead();
         } else {
            if (this.caster == null && this.casterUUID != null && !this.world.isRemote) {
               Entity e = this.world.getPlayerEntityByUUID(this.casterUUID);
               if (e instanceof EntityLivingBase) {
                  this.caster = (EntityLivingBase)e;
               }
            }

            if (this.activationDelay > 0) {
               --this.activationDelay;
            } else {
               ++this.lifetime;
               if (this.lifetime > 100) {
                  this.setDead();
               } else {
                  if (!this.world.isRemote) {
                     if (!this.initialBurstDone) {
                        this.initialBurstDone = true;
                        this.applyInitialBurst();
                     }

                     if (this.lifetime % 10 == 0) {
                        this.applyOngoingDamage();
                     }

                     this.spawnZoneParticles();
                     if (this.lifetime % 10 == 0) {
                        SoundEvent flameSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
                        if (flameSound != null) {
                           this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, flameSound, SoundCategory.PLAYERS, 1.5F, 0.4F);
                        }
                     }

                     if (this.lifetime % 25 == 0) {
                        this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.6F, 0.4F);
                     }
                  }

               }
            }
         }
      }

      private void applyInitialBurst() {
         float totalDmg = 56.0F + this.power * 32.0F;

         for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)10.0F, this.posY - (double)3.0F, this.posZ - (double)10.0F, this.posX + (double)10.0F, this.posY + (double)5.0F, this.posZ + (double)10.0F), (e) -> e != this.caster && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
            double dist = target.getDistance(this.posX, this.posY, this.posZ);
            if (dist <= (double)10.0F) {
               float cappedDmg = totalDmg;
               if (!(target instanceof EntityPlayer)) {
                  cappedDmg = Math.min(totalDmg, 100.0F);
               }

               target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.caster), cappedDmg);
               target.hurtResistantTime = 0;
               target.setFire(6);
               target.addPotionEffect(new PotionEffect(MobEffects.WITHER, 120, 1, false, true));
               target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 120, 1, false, true));
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 3.0F, 0.5F);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 2.0F, 0.3F);
         SoundEvent flameSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
         if (flameSound != null) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, flameSound, SoundCategory.PLAYERS, 2.5F, 0.25F);
         }

         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)1.0F, this.posZ, 100, (double)1.0F, (double)0.5F, (double)1.0F, (double)0.5F, (double)0.5F, (double)0.5F, new int[]{-18, 3});
         Particles.spawnParticle(this.world, Types.FLAME, this.posX, this.posY + (double)0.5F, this.posZ, 80, (double)2.0F, 0.8, (double)2.0F, 0.4, 0.3, 0.4, new int[]{-39424, 5});
      }

      private void applyOngoingDamage() {
         float totalDmg = 12.0F + this.power * 7.0F;

         for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)10.0F, this.posY - (double)3.0F, this.posZ - (double)10.0F, this.posX + (double)10.0F, this.posY + (double)5.0F, this.posZ + (double)10.0F), (e) -> e != this.caster && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
            double dist = target.getDistance(this.posX, this.posY, this.posZ);
            if (dist <= (double)10.0F) {
               float cappedDmg = totalDmg;
               if (!(target instanceof EntityPlayer)) {
                  cappedDmg = Math.min(totalDmg, 100.0F);
               }

               target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.caster), cappedDmg);
               target.hurtResistantTime = 0;
               target.setFire(4);
               target.addPotionEffect(new PotionEffect(MobEffects.WITHER, 120, 1, false, true));
               target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 120, 1, false, true));
            }
         }

      }

      private void spawnZoneParticles() {
         double baseAngle = (double)this.lifetime * 0.12;

         for(int i = 0; i < 40; ++i) {
            double spiralAngle = baseAngle + (double)i / (double)40.0F * Math.PI * (double)4.0F;
            double height = (double)i / (double)40.0F * (double)10.0F;
            double radius = (double)10.0F * ((double)1.0F - height / (double)14.0F);
            if (radius < (double)0.5F) {
               radius = (double)0.5F;
            }

            double px = this.posX + Math.cos(spiralAngle) * radius;
            double pz = this.posZ + Math.sin(spiralAngle) * radius;
            double swirlSpeed = 0.08 * (radius / (double)10.0F);
            double swirlX = Math.cos(spiralAngle + (Math.PI / 2D)) * swirlSpeed;
            double swirlZ = Math.sin(spiralAngle + (Math.PI / 2D)) * swirlSpeed;
            Particles.spawnParticle(this.world, Types.FLAME, px, this.posY + height, pz, 1, 0.15, 0.2, 0.15, swirlX, 0.08 + this.rand.nextDouble() * 0.06, swirlZ, new int[]{-48128, 8 + this.rand.nextInt(4)});
         }

         for(int i = 0; i < 20; ++i) {
            double innerAngle = baseAngle * (double)1.5F + (double)i / (double)20.0F * Math.PI * (double)2.0F;
            double innerR = this.rand.nextDouble() * (double)10.0F * 0.3;
            double innerHeight = this.rand.nextDouble() * (double)8.0F;
            double px = this.posX + Math.cos(innerAngle) * innerR;
            double pz = this.posZ + Math.sin(innerAngle) * innerR;
            Particles.spawnParticle(this.world, Types.FLAME, px, this.posY + innerHeight, pz, 1, 0.1, 0.15, 0.1, Math.cos(innerAngle + (Math.PI / 2D)) * 0.05, 0.12, Math.sin(innerAngle + (Math.PI / 2D)) * 0.05, new int[]{-30720, 6 + this.rand.nextInt(4)});
         }

         int groundCount = 30;

         for(int i = 0; i < groundCount; ++i) {
            double ringAngle = baseAngle + (double)i / (double)groundCount * Math.PI * (double)2.0F;
            double ringR = (double)10.0F * (0.7 + this.rand.nextDouble() * 0.3);
            double px = this.posX + Math.cos(ringAngle) * ringR;
            double pz = this.posZ + Math.sin(ringAngle) * ringR;
            Particles.spawnParticle(this.world, Types.FLAME, px, this.posY + 0.15, pz, 1, 0.1, 0.05, 0.1, Math.cos(ringAngle + (Math.PI / 2D)) * 0.04, 0.01, Math.sin(ringAngle + (Math.PI / 2D)) * 0.04, new int[]{-3394816, 5 + this.rand.nextInt(3)});
         }

         for(int i = 0; i < 8; ++i) {
            double eAngle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double eDist = this.rand.nextDouble() * (double)10.0F * (double)0.5F;
            double px = this.posX + Math.cos(eAngle) * eDist;
            double pz = this.posZ + Math.sin(eAngle) * eDist;
            Particles.spawnParticle(this.world, Types.FLAME, px, this.posY + this.rand.nextDouble() * (double)6.0F, pz, 1, 0.05, 0.05, 0.05, (this.rand.nextDouble() - (double)0.5F) * 0.06, 0.35 + this.rand.nextDouble() * 0.3, (this.rand.nextDouble() - (double)0.5F) * 0.06, new int[]{-21965, 12 + this.rand.nextInt(8)});
         }

         Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)9.0F, this.posZ, 8, (double)3.0F, (double)0.5F, (double)3.0F, 0.02, 0.03, 0.02, new int[]{-869068544, 18 + this.rand.nextInt(8)});
         if (this.lifetime % 5 == 0) {
            int ringCount = 36;

            for(int i = 0; i < ringCount; ++i) {
               double ringAngle = (double)i / (double)ringCount * Math.PI * (double)2.0F;
               double px = this.posX + Math.cos(ringAngle) * (double)10.0F;
               double pz = this.posZ + Math.sin(ringAngle) * (double)10.0F;
               Particles.spawnParticle(this.world, Types.FLAME, px, this.posY + 0.3 + this.rand.nextDouble() * (double)0.5F, pz, 1, 0.1, 0.15, 0.1, Math.cos(ringAngle) * 0.02, 0.05, Math.sin(ringAngle) * 0.02, new int[]{-48128, 8});
            }
         }

      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.lifetime = compound.getInteger("sizLifetime");
         this.power = compound.getFloat("sizPower");
         this.activationDelay = compound.getInteger("sizDelay");
         this.initialBurstDone = compound.getBoolean("sizBurstDone");
         if (compound.hasKey("sizCasterUUID")) {
            try {
               this.casterUUID = UUID.fromString(compound.getString("sizCasterUUID"));
            } catch (IllegalArgumentException var3) {
            }
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("sizLifetime", this.lifetime);
         compound.setFloat("sizPower", this.power);
         compound.setInteger("sizDelay", this.activationDelay);
         compound.setBoolean("sizBurstDone", this.initialBurstDone);
         if (this.casterUUID != null) {
            compound.setString("sizCasterUUID", this.casterUUID.toString());
         }

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
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 100L) {
               return false;
            } else {
               cooldownMap.put(uid, now);

               for(int i = 0; i < 5; ++i) {
                  EntityScorchOrb orb = new EntityScorchOrb(entity.world, entity, power, i);
                  double angle = (double)i / (double)5.0F * Math.PI * (double)2.0F;
                  double radius = (double)2.0F;
                  double ox = entity.posX + Math.cos(angle) * radius;
                  double oy = entity.posY + (double)entity.getEyeHeight();
                  double oz = entity.posZ + Math.sin(angle) * radius;
                  orb.setPosition(ox, oy, oz);
                  entity.world.spawnEntity(orb);
               }

               SoundEvent flameSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:flamethrow"));
               if (flameSound != null) {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, flameSound, SoundCategory.PLAYERS, 1.5F, 0.6F);
               }

               entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.5F, 0.3F);
               EntityScorchOrb.zoneSpawned.remove(entity.getUniqueID());
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
   public static class ScorchOrbRenderer extends Render<EntityScorchOrb> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/fireball2.png");

      public ScorchOrbRenderer(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.1F;
      }

      public void doRender(EntityScorchOrb entity, double x, double y, double z, float entityYaw, float partialTicks) {
         float scale = entity.getOrbScale();
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         GlStateManager.translate(x, y + (double)0.5F * (double)scale, z);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(9.0F * (partialTicks + (float)entity.ticksExisted), 0.0F, 0.0F, 1.0F);
         GlStateManager.disableLighting();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         GlStateManager.color(1.0F, 0.7F, 0.3F, 0.9F);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder bufferbuilder = tessellator.getBuffer();
         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         bufferbuilder.pos((double)-0.5F, (double)-0.5F, (double)0.0F).tex((double)0.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         bufferbuilder.pos((double)0.5F, (double)-0.5F, (double)0.0F).tex((double)1.0F, (double)1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         bufferbuilder.pos((double)0.5F, (double)0.5F, (double)0.0F).tex((double)1.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         bufferbuilder.pos((double)-0.5F, (double)0.5F, (double)0.0F).tex((double)0.0F, (double)0.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
         tessellator.draw();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.enableLighting();
         GlStateManager.disableRescaleNormal();
         GlStateManager.disableBlend();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();
      }

      protected ResourceLocation getEntityTexture(EntityScorchOrb entity) {
         return TEXTURE;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class InvisibleRenderer extends Render<EntityInfernoZone> {
      public InvisibleRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityInfernoZone entity, double x, double y, double z, float entityYaw, float partialTicks) {
      }

      protected ResourceLocation getEntityTexture(EntityInfernoZone entity) {
         return null;
      }
   }
}
