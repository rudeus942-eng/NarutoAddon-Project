
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.EntityRaitonBeam;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
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
import net.minecraft.util.math.MathHelper;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityRaitonElectromagneticMurder extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 284;

   public EntityRaitonElectromagneticMurder(ElementsInfTsukAddon instance) {
      super(instance, 920);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "raiton_electromagnetic_murder"), 284).name("inftsuk_raiton_electromagnetic_murder").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, InvisibleRenderer::new);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final double RANGE = (double)8.0F;
      private static final double HALF_ANGLE = (double)60.0F;
      private static final int MAX_LIFETIME = 30;
      private int lifetime;
      private float power;
      private float casterYaw;
      private EntityLivingBase caster;
      private final List<double[]> impactPoints;

      public EntityCustom(World world) {
         super(world);
         this.lifetime = 0;
         this.power = 1.0F;
         this.casterYaw = 0.0F;
         this.impactPoints = new ArrayList();
         this.setSize(0.1F, 0.1F);
         this.noClip = true;
         this.setInvisible(true);
      }

      public EntityCustom(World world, EntityLivingBase caster, float power) {
         this(world);
         this.caster = caster;
         this.power = power;
         this.casterYaw = caster.rotationYaw;
         this.setPosition(caster.posX, caster.posY, caster.posZ);
      }

      public Type getJutsuType() {
         return Type.RAITON;
      }

      protected void entityInit() {
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 30 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               if (this.lifetime == 5 || this.lifetime == 15 || this.lifetime == 25) {
                  this.applyConeDamage();
               }

               if (this.lifetime % 3 == 0) {
                  this.spawnGroundLightningBolts();
               }

               if (this.lifetime % 2 == 0) {
                  this.spawnZigzagChains();
               }

               if (this.lifetime % 3 == 0) {
                  SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
                  if (electricity != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, electricity, SoundCategory.PLAYERS, 1.2F, 0.7F + this.rand.nextFloat() * 0.6F);
                  }
               }
            }

         } else {
            if (!this.world.isRemote && this.lifetime > 30) {
               this.spawnGroundScorch();
            }

            this.setDead();
         }
      }

      private boolean isInCone(Entity target) {
         double dx = target.posX - this.posX;
         double dz = target.posZ - this.posZ;
         double distSq = dx * dx + dz * dz;
         if (distSq > (double)64.0F) {
            return false;
         } else if (distSq < (double)0.25F) {
            return true;
         } else {
            double angleToTarget = Math.toDegrees(Math.atan2(-dx, dz));
            double angleDiff = MathHelper.wrapDegrees(angleToTarget - (double)this.casterYaw);
            return Math.abs(angleDiff) <= (double)60.0F;
         }
      }

      private void applyConeDamage() {
         float dmg = 75.0F + this.power * 20.0F;
         List<EntityLivingBase> entities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)8.0F, this.posY - (double)2.0F, this.posZ - (double)8.0F, this.posX + (double)8.0F, this.posY + (double)4.0F, this.posZ + (double)8.0F), (e) -> e != this.caster && e.isEntityAlive() && ItemJutsu.canTarget(e));
         List<EntityLivingBase> hitTargets = new ArrayList();

         for(EntityLivingBase target : entities) {
            if (this.isInCone(target)) {
               float finalDmg = dmg;
               if (this.world.isRainingAt(new BlockPos(target)) || target.isInWater()) {
                  finalDmg = dmg * 1.2F;
               }

               if (!(target instanceof EntityPlayer)) {
                  finalDmg = Math.min(finalDmg, 100.0F);
               }

               target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.caster), finalDmg);
               target.hurtResistantTime = 0;
               target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1, false, false));
               target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 40, 0, false, false));
               hitTargets.add(target);
               this.impactPoints.add(new double[]{target.posX, target.posY, target.posZ});
               Particles.spawnParticle(this.world, Types.FLAME, target.posX, target.posY + (double)target.height * (double)0.5F, target.posZ, 25, (double)0.5F, 0.7, (double)0.5F, 0.08, 0.08, 0.08, new int[]{-1, 3});
               Particles.spawnParticle(this.world, Types.FLAME, target.posX, target.posY + 0.1, target.posZ, 15, 0.8, 0.1, 0.8, 0.12, 0.01, 0.12, new int[]{-7816193, 5});
               SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
               if (electricity != null) {
                  this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, electricity, SoundCategory.PLAYERS, 1.5F, 1.5F + this.rand.nextFloat() * 0.5F);
               }
            }
         }

         for(int i = 0; i < hitTargets.size(); ++i) {
            for(int j = i + 1; j < hitTargets.size(); ++j) {
               EntityLivingBase a = (EntityLivingBase)hitTargets.get(i);
               EntityLivingBase b = (EntityLivingBase)hitTargets.get(j);
               double dist = (double)a.getDistance(b);
               if (dist < 6.4) {
                  this.spawnChainBetween(a.posX, a.posY + (double)a.height * (double)0.5F, a.posZ, b.posX, b.posY + (double)b.height * (double)0.5F, b.posZ);
               }
            }
         }

      }

      private void spawnChainBetween(double x1, double y1, double z1, double x2, double y2, double z2) {
         int steps = 8;

         for(int i = 0; i <= steps; ++i) {
            double t = (double)i / (double)steps;
            double px = x1 + (x2 - x1) * t + (this.rand.nextDouble() - (double)0.5F) * 0.3;
            double py = y1 + (y2 - y1) * t + (this.rand.nextDouble() - (double)0.5F) * 0.3;
            double pz = z1 + (z2 - z1) * t + (this.rand.nextDouble() - (double)0.5F) * 0.3;
            Particles.spawnParticle(this.world, Types.FLAME, px, py, pz, 1, 0.02, 0.02, 0.02, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-1, 4});
         }

      }

      private void spawnGroundLightningBolts() {
         if (this.caster != null) {
            double facingRad = Math.toRadians((double)(-this.casterYaw));
            double facingX = Math.sin(facingRad);
            double facingZ = Math.cos(facingRad);
            int boltCount = 2 + this.rand.nextInt(2);

            for(int i = 0; i < boltCount; ++i) {
               double spreadAngle = (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * Math.toRadians((double)60.0F);
               double dist = (double)1.0F + this.rand.nextDouble() * (double)7.0F;
               double cos = Math.cos(spreadAngle);
               double sin = Math.sin(spreadAngle);
               double dirX = facingX * cos - facingZ * sin;
               double dirZ = facingX * sin + facingZ * cos;
               double bx = this.posX + dirX * dist;
               double bz = this.posZ + dirZ * dist;
               double by = this.posY + 0.1;
               EntityRaitonBeam.EntityCustom bolt = new EntityRaitonBeam.EntityCustom(this.world, this.caster, 0.0F, 0.0F);
               bolt.setPosition(bx, by, bz);
               bolt.motionX = dirX * 0.15 + (this.rand.nextDouble() - (double)0.5F) * 0.2;
               bolt.motionY = (this.rand.nextDouble() - 0.3) * 0.1;
               bolt.motionZ = dirZ * 0.15 + (this.rand.nextDouble() - (double)0.5F) * 0.2;
               bolt.setEntityScale(0.15F + this.rand.nextFloat() * 0.1F);
               bolt.setMaxLifetime(15);
               this.world.spawnEntity(bolt);
            }

         }
      }

      private void spawnZigzagChains() {
         double facingRad = Math.toRadians((double)(-this.casterYaw));
         double facingX = Math.sin(facingRad);
         double facingZ = Math.cos(facingRad);
         int lineCount = 3 + this.rand.nextInt(2);

         for(int line = 0; line < lineCount; ++line) {
            double spreadAngle = (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * Math.toRadians((double)60.0F);
            double cos = Math.cos(spreadAngle);
            double sin = Math.sin(spreadAngle);
            double dirX = facingX * cos - facingZ * sin;
            double dirZ = facingX * sin + facingZ * cos;
            double perpX = -dirZ;
            double perpZ = dirX;
            int segments = 5 + this.rand.nextInt(4);
            double segLen = (double)8.0F / (double)segments;
            double zigSign = this.rand.nextBoolean() ? (double)1.0F : (double)-1.0F;

            for(int s = 0; s < segments; ++s) {
               double dist = ((double)s + (double)0.5F) * segLen;
               double zigOffset = zigSign * (0.2 + this.rand.nextDouble() * 0.4);
               zigSign = -zigSign;
               double px = this.posX + dirX * dist + perpX * zigOffset;
               double pz = this.posZ + dirZ * dist + perpZ * zigOffset;
               double py = this.posY + 0.1 + this.rand.nextDouble() * 0.15;
               Particles.spawnParticle(this.world, Types.FLAME, px, py, pz, 1, 0.05, 0.03, 0.05, dirX * 0.04, 0.005, dirZ * 0.04, new int[]{-5579265, 5});
            }
         }

         for(int i = 0; i < 5; ++i) {
            double spreadAngle = (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * Math.toRadians((double)60.0F);
            double dist = this.rand.nextDouble() * (double)8.0F;
            double cos = Math.cos(spreadAngle);
            double sin = Math.sin(spreadAngle);
            double dirX = facingX * cos - facingZ * sin;
            double dirZ = facingX * sin + facingZ * cos;
            Particles.spawnParticle(this.world, Types.FLAME, this.posX + dirX * dist, this.posY + 0.05 + this.rand.nextDouble() * 0.3, this.posZ + dirZ * dist, 2, 0.08, 0.08, 0.08, (double)0.0F, 0.02, (double)0.0F, new int[]{-14522625, 6});
         }

      }

      private void spawnGroundScorch() {
         for(double[] pt : this.impactPoints) {
            Particles.spawnParticle(this.world, Types.FLAME, pt[0], pt[1] + 0.1, pt[2], 3, 0.3, 0.05, 0.3, (double)0.0F, 0.005, (double)0.0F, new int[]{-14527028, 15});
         }

         double facingRad = Math.toRadians((double)(-this.casterYaw));
         double facingX = Math.sin(facingRad);
         double facingZ = Math.cos(facingRad);

         for(int i = 0; i < 6; ++i) {
            double spreadAngle = (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * Math.toRadians((double)60.0F);
            double dist = (double)1.0F + this.rand.nextDouble() * (double)7.0F;
            double cos = Math.cos(spreadAngle);
            double sin = Math.sin(spreadAngle);
            double dirX = facingX * cos - facingZ * sin;
            double dirZ = facingX * sin + facingZ * cos;
            Particles.spawnParticle(this.world, Types.FLAME, this.posX + dirX * dist, this.posY + 0.05, this.posZ + dirZ * dist, 5, 0.3, 0.03, 0.3, (double)0.0F, 0.005, (double)0.0F, new int[]{-14527028, 25});
         }

      }

      public boolean canBeCollidedWith() {
         return false;
      }

      public boolean canBePushed() {
         return false;
      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.lifetime = compound.getInteger("lifetime");
         this.power = compound.getFloat("emPower");
         this.casterYaw = compound.getFloat("emCasterYaw");
      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("emPower", this.power);
         compound.setFloat("emCasterYaw", this.casterYaw);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final WeakHashMap<UUID, Long> COOLDOWN_MAP = new WeakHashMap();
      private static final int COOLDOWN_TICKS = 60;

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (power < 0.5F) {
            return false;
         } else if (entity.world.isRemote) {
            return false;
         } else {
            UUID casterId = entity.getUniqueID();
            long currentTick = entity.world.getTotalWorldTime();
            Long lastCast = (Long)COOLDOWN_MAP.get(casterId);
            if (lastCast != null && currentTick - lastCast < 60L) {
               return false;
            } else {
               COOLDOWN_MAP.put(casterId, currentTick);
               EntityCustom zone = new EntityCustom(entity.world, entity, power);
               entity.world.spawnEntity(zone);
               SoundEvent electricity = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:electricity"));
               if (electricity != null) {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, electricity, SoundCategory.PLAYERS, 1.5F, 0.8F);
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, electricity, SoundCategory.PLAYERS, 1.0F, 1.4F);
               } else {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.PLAYERS, 1.0F, 1.1F);
               }

               return true;
            }
         }
      }

      public float getBasePower() {
         return 0.5F;
      }

      public float getPowerupDelay() {
         return 10.0F;
      }

      public float getMaxPower() {
         return 4.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote) {
            Particles.spawnParticle(player.world, Types.FLAME, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.6, player.posY + 0.15, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.6, 2, 0.1, 0.1, 0.1, (double)0.0F, 0.02, (double)0.0F, new int[]{-14522625, 15});
            if (player.ticksExisted % 3 == 0) {
               Particles.spawnParticle(player.world, Types.FLAME, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.4, player.posY + 0.1, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.4, 1, 0.2, 0.02, 0.2, (double)0.0F, 0.01, (double)0.0F, new int[]{-7816193, 10});
            }
         }

         super.onUsingTick(stack, player, power);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class InvisibleRenderer extends Render<EntityCustom> {
      public InvisibleRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return null;
      }
   }
}
