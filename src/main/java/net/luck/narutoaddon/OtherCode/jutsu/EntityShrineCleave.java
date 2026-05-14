
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Chakra;
import net.narutomod.PlayerTracker;
import net.narutomod.item.ItemJutsu;
import net.narutomod.procedure.ProcedureUtils;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityShrineCleave extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 362;
   private static final double MAX_RANGE = (double)16.0F;
   private static final double CHAKRA_PER_BURST;

   public EntityShrineCleave(ElementsInfTsukAddon instance) {
      super(instance, 954);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(VisualSlashEntity.class).id(new ResourceLocation("inftsukaddon", "shrine_cleave"), 362).name("inftsuk_shrine_cleave_visual").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(VisualSlashEntity.class, ShrineCleaveRenderer::new);
   }

   static {
      CHAKRA_PER_BURST = ItemShrineRelease.CLEAVE.chakraUsage * 0.288;
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> lastBurstTick = new WeakHashMap();
      private static final Map<UUID, Long> lastSoundTick = new WeakHashMap();
      private static final Map<UUID, Long> lastLoopTick = new WeakHashMap();
      private static final Map<UUID, Integer> burstCounter = new WeakHashMap();
      private static final float NPC_DAMAGE_CAP = 15.0F;

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         return false;
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

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote && player instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer)player;
            if (PlayerTracker.isNinja(entityPlayer)) {
               EntityLivingBase target = this.getCrosshairTarget(player);
               if (target != null) {
                  Chakra.Pathway pathway = Chakra.pathway(entityPlayer);
                  if (pathway != null && !(pathway.getAmount() < EntityShrineCleave.CHAKRA_PER_BURST)) {
                     long now = player.world.getTotalWorldTime();
                     UUID casterId = player.getUniqueID();
                     Long lastBurst = (Long)lastBurstTick.get(casterId);
                     if (lastBurst == null || now - lastBurst >= 2L) {
                        lastBurstTick.put(casterId, now);
                        pathway.consume(EntityShrineCleave.CHAKRA_PER_BURST);
                        this.applyCleaveDamage(player, target, power);
                        this.spawnCleaveVisuals(player.world, player, target);
                        this.playCleaveSound(player, target, now, casterId);
                        int count = (Integer)burstCounter.getOrDefault(casterId, 0) + 1;
                        if (count >= 4) {
                           if (stack.getItem() instanceof ItemJutsu.Base) {
                              ((ItemJutsu.Base)stack.getItem()).addCurrentJutsuXp(stack, 1);
                           }

                           count = 0;
                        }

                        burstCounter.put(casterId, count);
                     }
                  }
               }
            }
         }
      }

      private void applyCleaveDamage(EntityLivingBase caster, EntityLivingBase target, float power) {
         int armorValue = Math.max(0, target.getTotalArmorValue());
         float damage = 8.0F + target.getMaxHealth() * 0.025F + (float)armorValue * 0.35F;
         if (!(target instanceof EntityPlayer)) {
            damage = Math.min(Math.max(damage, 12.0F), 15.0F);
         }

         target.attackEntityFrom(ItemJutsu.causeJutsuDamage(caster, caster), damage);
         target.hurtResistantTime = 0;
      }

      private void playCleaveSound(EntityLivingBase caster, EntityLivingBase target, long now, UUID casterId) {
         Long lastLoop = (Long)lastLoopTick.get(casterId);
         if (lastLoop == null || now - lastLoop >= 12L) {
            lastLoopTick.put(casterId, now);
            SoundEvent shrineLoop = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("inftsukaddon:shrine_cleave_loop"));
            if (shrineLoop != null) {
               caster.world.playSound((EntityPlayer)null, caster.posX, caster.posY + (double)caster.height * (double)0.5F, caster.posZ, shrineLoop, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
         }

         Long lastSound = (Long)lastSoundTick.get(casterId);
         if (lastSound == null || now - lastSound >= 4L) {
            lastSoundTick.put(casterId, now);
            caster.world.playSound((EntityPlayer)null, target.posX, target.posY + (double)target.height * (double)0.5F, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.8F, 1.45F + caster.getRNG().nextFloat() * 0.18F);
            SoundEvent slice = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
            if (slice != null) {
               caster.world.playSound((EntityPlayer)null, target.posX, target.posY + (double)target.height * (double)0.5F, target.posZ, slice, SoundCategory.PLAYERS, 0.45F, 1.7F + caster.getRNG().nextFloat() * 0.15F);
            }

         }
      }

      private void spawnCleaveVisuals(World world, EntityLivingBase caster, EntityLivingBase target) {
         for(int i = 0; i < 6; ++i) {
            VisualSlashEntity visual = new VisualSlashEntity(world);
            visual.setFrame(world.rand.nextInt(7));
            visual.setRoll(this.randomRoll(world.rand));
            visual.setMaxAge(5 + world.rand.nextInt(3));
            visual.setSizeData(4.6F + world.rand.nextFloat() * 2.8F, 1.05F + world.rand.nextFloat() * 0.46F);
            double radius = (double)0.25F + world.rand.nextDouble() * ((double)target.width + 0.9);
            double angle = world.rand.nextDouble() * Math.PI * (double)2.0F;
            double x = target.posX + Math.cos(angle) * radius;
            double y = target.posY + (double)target.height * ((double)0.25F + world.rand.nextDouble() * 0.55);
            double z = target.posZ + Math.sin(angle) * radius;
            visual.setPosition(x, y, z);
            double towardTargetX = (target.posX - x) * 0.03;
            double towardTargetY = (target.posY + (double)target.height * (double)0.5F - y) * 0.03;
            double towardTargetZ = (target.posZ - z) * 0.03;
            visual.motionX = towardTargetX + (world.rand.nextDouble() - (double)0.5F) * 0.01;
            visual.motionY = towardTargetY + (world.rand.nextDouble() - (double)0.5F) * 0.008;
            visual.motionZ = towardTargetZ + (world.rand.nextDouble() - (double)0.5F) * 0.01;
            world.spawnEntity(visual);
         }

      }

      private EntityLivingBase getCrosshairTarget(EntityLivingBase caster) {
         RayTraceResult rt = ProcedureUtils.objectEntityLookingAt(caster, (double)16.0F, (double)1.5F);
         if (rt != null && rt.entityHit instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase)rt.entityHit;
            return target.isEntityAlive() && ItemJutsu.canTarget(target) && !((double)caster.getDistance(target) > (double)16.0F) ? target : null;
         } else {
            return null;
         }
      }

      private float randomRoll(Random random) {
         float[] rolls = new float[]{-78.0F, -58.0F, -36.0F, -18.0F, 18.0F, 36.0F, 58.0F, 78.0F, 102.0F, -102.0F};
         return rolls[random.nextInt(rolls.length)];
      }
   }

   public static class VisualSlashEntity extends Entity {
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
         this.dataManager.register(FRAME, 0);
         this.dataManager.register(HALF_WIDTH, 3.8F);
         this.dataManager.register(HALF_HEIGHT, 0.35F);
         this.dataManager.register(ROLL, 0.0F);
         this.dataManager.register(MAX_AGE, 6);
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
         this.motionX *= 0.72;
         this.motionY *= 0.72;
         this.motionZ *= 0.72;
         if (this.ticksExisted >= this.getMaxAge() || !this.world.isBlockLoaded(new BlockPos(this))) {
            this.setDead();
         }

      }

      protected void readEntityFromNBT(NBTTagCompound compound) {
         this.setFrame(compound.getInteger("cFrame"));
         this.setSizeData(compound.getFloat("cW"), compound.getFloat("cH"));
         this.setRoll(compound.getFloat("cRoll"));
         this.setMaxAge(compound.getInteger("cAge"));
      }

      protected void writeEntityToNBT(NBTTagCompound compound) {
         compound.setInteger("cFrame", this.getFrame());
         compound.setFloat("cW", this.getHalfWidth());
         compound.setFloat("cH", this.getHalfHeight());
         compound.setFloat("cRoll", this.getRoll());
         compound.setInteger("cAge", this.getMaxAge());
      }

      static {
         FRAME = EntityDataManager.createKey(VisualSlashEntity.class, DataSerializers.VARINT);
         HALF_WIDTH = EntityDataManager.createKey(VisualSlashEntity.class, DataSerializers.FLOAT);
         HALF_HEIGHT = EntityDataManager.createKey(VisualSlashEntity.class, DataSerializers.FLOAT);
         ROLL = EntityDataManager.createKey(VisualSlashEntity.class, DataSerializers.FLOAT);
         MAX_AGE = EntityDataManager.createKey(VisualSlashEntity.class, DataSerializers.VARINT);
      }
   }
}
