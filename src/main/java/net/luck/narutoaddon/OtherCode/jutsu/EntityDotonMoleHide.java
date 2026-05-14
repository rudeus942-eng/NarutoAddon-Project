
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
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

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityDotonMoleHide extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 287;

   public EntityDotonMoleHide(ElementsInfTsukAddon instance) {
      super(instance, 921);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "doton_mole_hide"), 287).name("inftsuk_doton_mole_hide").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, InvisibleRenderer::new);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final int BURROW_DURATION = 20;
      private static final int MAX_LIFETIME = 60;
      private static final double EMERGE_RADIUS = (double)5.0F;
      private int lifetime;
      private float power;
      private EntityLivingBase caster;
      private UUID casterUUID;
      private double targetX;
      private double targetY;
      private double targetZ;
      private double startX;
      private double startY;
      private double startZ;
      private boolean emerged;
      private boolean burrowed;

      public EntityCustom(World world) {
         super(world);
         this.lifetime = 0;
         this.power = 1.0F;
         this.emerged = false;
         this.burrowed = false;
         this.setSize(0.1F, 0.1F);
         this.noClip = true;
         this.setInvisible(true);
      }

      public EntityCustom(World world, EntityLivingBase caster, double tx, double ty, double tz, float power) {
         this(world);
         this.caster = caster;
         this.casterUUID = caster.getUniqueID();
         this.power = power;
         this.targetX = tx;
         this.targetY = ty;
         this.targetZ = tz;
         this.startX = caster.posX;
         this.startY = caster.posY;
         this.startZ = caster.posZ;
         this.setPosition(caster.posX, caster.posY, caster.posZ);
      }

      public Type getJutsuType() {
         return Type.DOTON;
      }

      protected void entityInit() {
      }

      private EntityLivingBase getCaster() {
         if (this.caster != null && this.caster.isEntityAlive()) {
            return this.caster;
         } else {
            if (this.casterUUID != null && !this.world.isRemote) {
               for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)128.0F))) {
                  if (this.casterUUID.equals(e.getUniqueID())) {
                     this.caster = e;
                     return this.caster;
                  }
               }
            }

            return null;
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 60 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               EntityLivingBase owner = this.getCaster();
               if (owner == null || !owner.isEntityAlive()) {
                  this.setDead();
                  return;
               }

               if (this.lifetime <= 20) {
                  this.handleBurrowPhase(owner);
               } else if (!this.emerged) {
                  this.handleEmergePhase(owner);
               }

               this.setPosition(owner.posX, owner.posY, owner.posZ);
               if (this.emerged && this.lifetime > 30) {
                  this.setDead();
               }
            }

         } else {
            this.cleanupEffects();
            this.setDead();
         }
      }

      private void handleBurrowPhase(EntityLivingBase owner) {
         if (this.lifetime == 1) {
            this.burrowed = true;
            owner.sendMessage(new TextComponentString("§7Burrowing..."));
            owner.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 30, 0, false, false));
            owner.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30, 3, false, false));

            for(int i = 0; i < 15; ++i) {
               double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
               double speed = 0.05 + this.rand.nextDouble() * 0.1;
               double velY = 0.15 + this.rand.nextDouble() * 0.1;
               Particles.spawnParticle(this.world, Types.SMOKE, this.startX + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, this.startY + 0.1 + this.rand.nextDouble() * 0.2, this.startZ + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, 1, 0.08, 0.08, 0.08, Math.cos(angle) * speed, velY, Math.sin(angle) * speed, new int[]{-8960990, 18});
            }

            for(int line = 0; line < 5; ++line) {
               double crackAngle = (double)line / (double)5.0F * Math.PI * (double)2.0F + this.rand.nextDouble() * 0.3;

               for(int p = 1; p <= 3; ++p) {
                  double dist = (double)p * 0.4;
                  Particles.spawnParticle(this.world, Types.SMOKE, this.startX + Math.cos(crackAngle) * dist, this.startY + 0.02, this.startZ + Math.sin(crackAngle) * dist, 1, 0.05, 0.01, 0.05, Math.cos(crackAngle) * 0.02, (double)0.0F, Math.sin(crackAngle) * 0.02, new int[]{-11193583, 22});
               }
            }

            Particles.spawnParticle(this.world, Types.SMOKE, this.startX, this.startY + 0.08, this.startZ, 18, 0.7, 0.05, 0.7, 0.04, -0.01, 0.04, new int[]{-2005440956, 35});
            SoundEvent rockSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:rocks"));
            if (rockSound != null) {
               this.world.playSound((EntityPlayer)null, this.startX, this.startY, this.startZ, rockSound, SoundCategory.PLAYERS, 1.2F, 0.3F);
            }

            this.world.playSound((EntityPlayer)null, this.startX, this.startY, this.startZ, SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.PLAYERS, 1.0F, 0.4F);
         }

         double progress = (double)this.lifetime / (double)20.0F;
         progress = progress < (double)0.5F ? (double)2.0F * progress * progress : (double)1.0F - Math.pow((double)-2.0F * progress + (double)2.0F, (double)2.0F) / (double)2.0F;
         double surfaceX = this.startX + (this.targetX - this.startX) * progress;
         double surfaceY = this.startY + (this.targetY - this.startY) * progress;
         double surfaceZ = this.startZ + (this.targetZ - this.startZ) * progress;

         BlockPos groundPos;
         for(groundPos = new BlockPos(surfaceX, surfaceY + (double)2.0F, surfaceZ); groundPos.getY() > 0 && this.world.isAirBlock(groundPos); groundPos = groundPos.down()) {
         }

         double groundY = (double)groundPos.getY() + (double)1.0F;
         double undergroundY = groundY - (double)2.0F;
         this.teleportReliably(owner, surfaceX, undergroundY, surfaceZ);
         if (this.lifetime == 10) {
            owner.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 30 - this.lifetime, 0, false, false));
            owner.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 - this.lifetime, 3, false, false));
         }

         if (this.lifetime % 3 == 0) {
            Particles.spawnParticle(this.world, Types.SMOKE, surfaceX, groundY + 0.2, surfaceZ, 5, 0.35, 0.08, 0.35, 0.02, -0.08, 0.02, new int[]{-7838157, 15});
            if (this.rand.nextInt(2) == 0) {
               Particles.spawnParticle(this.world, Types.SMOKE, surfaceX + (this.rand.nextDouble() - (double)0.5F) * 0.6, groundY + (double)0.25F, surfaceZ + (this.rand.nextDouble() - (double)0.5F) * 0.6, 1, 0.1, 0.05, 0.1, (this.rand.nextDouble() - (double)0.5F) * 0.02, 0.06, (this.rand.nextDouble() - (double)0.5F) * 0.02, new int[]{-10075102, 12});
            }
         }

         if (this.lifetime % 10 == 0 && this.lifetime > 1) {
            this.world.playSound((EntityPlayer)null, surfaceX, groundY, surfaceZ, SoundEvents.BLOCK_GRAVEL_STEP, SoundCategory.PLAYERS, 0.6F, 0.4F);
         }

      }

      private void handleEmergePhase(EntityLivingBase owner) {
         this.emerged = true;
         double emergeY = this.targetY;
         boolean foundAir = false;

         for(int dy = 0; dy <= 5; ++dy) {
            BlockPos checkPos = new BlockPos(this.targetX, this.targetY + (double)dy, this.targetZ);
            if (this.world.isAirBlock(checkPos)) {
               emergeY = this.targetY + (double)dy;
               foundAir = true;
               break;
            }
         }

         if (!foundAir) {
            emergeY = this.targetY + (double)2.0F;
         }

         this.teleportReliably(owner, this.targetX, emergeY, this.targetZ);
         owner.removePotionEffect(MobEffects.INVISIBILITY);
         owner.removePotionEffect(MobEffects.RESISTANCE);
         owner.fallDistance = 0.0F;
         float totalDmg = 80.0F + this.power * 20.0F;

         for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.targetX - (double)5.0F, this.targetY - (double)1.0F, this.targetZ - (double)5.0F, this.targetX + (double)5.0F, this.targetY + (double)3.0F, this.targetZ + (double)5.0F), (e) -> e != this.caster && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
            double distSq = target.getDistanceSq(this.targetX, this.targetY, this.targetZ);
            if (distSq <= (double)25.0F) {
               float cappedDmg = totalDmg;
               if (!(target instanceof EntityPlayer)) {
                  cappedDmg = Math.min(totalDmg, 100.0F);
               }

               target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.caster), cappedDmg);
               target.hurtResistantTime = 0;
               target.motionY += 0.6;
               target.velocityChanged = true;
               target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 20, 0, false, false));
            }
         }

         for(int i = 0; i < 28; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double speed = 0.08 + this.rand.nextDouble() * 0.16;
            double xMul = (double)0.5F + this.rand.nextDouble() * (double)1.0F;
            double zMul = (double)0.5F + this.rand.nextDouble() * (double)1.0F;
            double vy = 0.2 + this.rand.nextDouble() * 0.2;
            Particles.spawnParticle(this.world, Types.SMOKE, this.targetX + (this.rand.nextDouble() - (double)0.5F) * 0.6, this.targetY + 0.1 + this.rand.nextDouble() * 0.3, this.targetZ + (this.rand.nextDouble() - (double)0.5F) * 0.6, 1, 0.12, 0.12, 0.12, Math.cos(angle) * speed * xMul, vy, Math.sin(angle) * speed * zMul, new int[]{-10075102, 30});
         }

         for(int i = 0; i < 20; ++i) {
            double angle = (double)i / (double)20.0F * Math.PI * (double)2.0F;
            double ringSpeed = 0.1 + this.rand.nextDouble() * 0.04;
            Particles.spawnParticle(this.world, Types.SMOKE, this.targetX + Math.cos(angle) * 0.3, this.targetY + 0.1, this.targetZ + Math.sin(angle) * 0.3, 1, 0.1, 0.05, 0.1, Math.cos(angle) * ringSpeed, -0.02, Math.sin(angle) * ringSpeed, new int[]{-7842509, 28});
         }

         for(int i = 0; i < 10; ++i) {
            double fallVelY = -0.1 - this.rand.nextDouble() * 0.05;
            Particles.spawnParticle(this.world, Types.SMOKE, this.targetX + (this.rand.nextDouble() - (double)0.5F) * 1.8, this.targetY + (double)1.5F + this.rand.nextDouble() * (double)1.0F, this.targetZ + (this.rand.nextDouble() - (double)0.5F) * 1.8, 1, 0.08, 0.04, 0.08, (this.rand.nextDouble() - (double)0.5F) * 0.02, fallVelY, (this.rand.nextDouble() - (double)0.5F) * 0.02, new int[]{-12307695, 22});
         }

         Particles.spawnParticle(this.world, Types.SMOKE, this.targetX, this.targetY + 0.15, this.targetZ, 20, (double)2.0F, 0.12, (double)2.0F, 0.01, -0.005, 0.01, new int[]{1719100996, 55});
         SoundEvent rockSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:rocks"));
         if (rockSound != null) {
            this.world.playSound((EntityPlayer)null, this.targetX, this.targetY, this.targetZ, rockSound, SoundCategory.HOSTILE, 2.0F, 0.6F);
         }

         this.world.playSound((EntityPlayer)null, this.targetX, this.targetY, this.targetZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0F, 0.5F);
         this.world.playSound((EntityPlayer)null, this.targetX, this.targetY, this.targetZ, SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.HOSTILE, 1.5F, 0.3F);
      }

      private void cleanupEffects() {
         EntityLivingBase owner = this.getCaster();
         if (owner != null && !this.emerged) {
            owner.removePotionEffect(MobEffects.INVISIBILITY);
            owner.removePotionEffect(MobEffects.RESISTANCE);
            if (this.burrowed) {
               double emergeY = this.targetY;
               boolean foundAir = false;

               for(int dy = 0; dy <= 5; ++dy) {
                  BlockPos checkPos = new BlockPos(this.targetX, this.targetY + (double)dy, this.targetZ);
                  if (this.world.isAirBlock(checkPos)) {
                     emergeY = this.targetY + (double)dy;
                     foundAir = true;
                     break;
                  }
               }

               if (!foundAir) {
                  emergeY = this.targetY + (double)2.0F;
               }

               this.teleportReliably(owner, this.targetX, emergeY, this.targetZ);
               owner.fallDistance = 0.0F;
            }
         }

      }

      private void teleportReliably(EntityLivingBase entity, double x, double y, double z) {
         entity.setPositionAndUpdate(x, y, z);
         if (entity instanceof EntityPlayerMP) {
            ((EntityPlayerMP)entity).connection.setPlayerLocation(x, y, z, entity.rotationYaw, entity.rotationPitch);
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
         this.power = compound.getFloat("mhPower");
         this.targetX = compound.getDouble("targetX");
         this.targetY = compound.getDouble("targetY");
         this.targetZ = compound.getDouble("targetZ");
         this.startX = compound.getDouble("startX");
         this.startY = compound.getDouble("startY");
         this.startZ = compound.getDouble("startZ");
         this.emerged = compound.getBoolean("emerged");
         this.burrowed = compound.getBoolean("burrowed");
         if (compound.hasUniqueId("casterUUID")) {
            this.casterUUID = compound.getUniqueId("casterUUID");
         }

      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("mhPower", this.power);
         compound.setDouble("targetX", this.targetX);
         compound.setDouble("targetY", this.targetY);
         compound.setDouble("targetZ", this.targetZ);
         compound.setDouble("startX", this.startX);
         compound.setDouble("startY", this.startY);
         compound.setDouble("startZ", this.startZ);
         compound.setBoolean("emerged", this.emerged);
         compound.setBoolean("burrowed", this.burrowed);
         if (this.casterUUID != null) {
            compound.setUniqueId("casterUUID", this.casterUUID);
         }

      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (power < 0.5F) {
            return false;
         } else if (entity.world.isRemote) {
            return false;
         } else {
            long now = entity.world.getTotalWorldTime();
            UUID uid = entity.getUniqueID();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 100L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               RayTraceResult rt = ProcedureUtils.raytraceBlocks(entity, (double)30.0F);
               if (rt != null && rt.typeOfHit == RayTraceResult.Type.BLOCK) {
                  BlockPos hitPos = rt.getBlockPos();
                  IBlockState hitState = entity.world.getBlockState(hitPos);
                  if (hitState.getMaterial() != Material.AIR && hitState.getMaterial() != Material.WATER && hitState.getMaterial() != Material.PLANTS) {
                     BlockPos standPos = hitPos.up();
                     double tx = (double)standPos.getX() + (double)0.5F;
                     double ty = (double)standPos.getY();
                     double tz = (double)standPos.getZ() + (double)0.5F;
                     if (rt.sideHit == EnumFacing.UP) {
                        tx = rt.hitVec.x;
                        ty = (double)hitPos.getY() + (double)1.0F;
                        tz = rt.hitVec.z;
                     }

                     EntityCustom mole = new EntityCustom(entity.world, entity, tx, ty, tz, power);
                     entity.world.spawnEntity(mole);
                     entity.sendMessage(new TextComponentString("§7[Mole Hide] Entity spawned at target."));
                     return true;
                  }
               }

               Vec3d look = entity.getLookVec();
               double farX = entity.posX + look.x * (double)30.0F;
               double farY = entity.posY + (double)entity.getEyeHeight() + look.y * (double)30.0F;
               double farZ = entity.posZ + look.z * (double)30.0F;
               BlockPos groundPos = null;
               BlockPos searchStart = new BlockPos(farX, farY + (double)10.0F, farZ);

               for(int dy = 0; dy < 30; ++dy) {
                  BlockPos check = searchStart.down(dy);
                  IBlockState state = entity.world.getBlockState(check);
                  if (state.getMaterial() != Material.AIR && state.getMaterial() != Material.WATER && state.getMaterial() != Material.PLANTS) {
                     groundPos = check;
                     break;
                  }
               }

               double tx;
               double ty;
               double tz;
               if (groundPos != null) {
                  tx = (double)groundPos.getX() + (double)0.5F;
                  ty = (double)groundPos.getY() + (double)1.0F;
                  tz = (double)groundPos.getZ() + (double)0.5F;
               } else {
                  tx = entity.posX + look.x * (double)10.0F;
                  ty = entity.posY;
                  tz = entity.posZ + look.z * (double)10.0F;
                  BlockPos fallbackSearch = new BlockPos(tx, ty + (double)5.0F, tz);
                  boolean foundFallback = false;

                  for(int dy = 0; dy < 15; ++dy) {
                     BlockPos check = fallbackSearch.down(dy);
                     IBlockState state = entity.world.getBlockState(check);
                     if (state.getMaterial() != Material.AIR && state.getMaterial() != Material.WATER && state.getMaterial() != Material.PLANTS) {
                        tx = (double)check.getX() + (double)0.5F;
                        ty = (double)check.getY() + (double)1.0F;
                        tz = (double)check.getZ() + (double)0.5F;
                        foundFallback = true;
                        break;
                     }
                  }

                  if (!foundFallback) {
                     entity.sendMessage(new TextComponentString("§c[Mole Hide] No valid ground found to burrow to!"));
                     return true;
                  }
               }

               EntityCustom mole = new EntityCustom(entity.world, entity, tx, ty, tz, power);
               entity.world.spawnEntity(mole);
               entity.sendMessage(new TextComponentString("§7[Mole Hide] Entity spawned (fallback target)."));
               return true;
            }
         }
      }

      public float getBasePower() {
         return 0.5F;
      }

      public float getPowerupDelay() {
         return 15.0F;
      }

      public float getMaxPower() {
         return 4.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote) {
            Particles.spawnParticle(player.world, Types.SMOKE, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.6, player.posY + 0.05, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.6, 2, 0.2, 0.03, 0.2, (double)0.0F, -0.02, (double)0.0F, new int[]{-7842509, 12});
            if (player.ticksExisted % 4 == 0) {
               Particles.spawnParticle(player.world, Types.SMOKE, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.4, player.posY + 0.3, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.4, 1, 0.1, 0.1, 0.1, (double)0.0F, -0.03, (double)0.0F, new int[]{-10075102, 15});
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
