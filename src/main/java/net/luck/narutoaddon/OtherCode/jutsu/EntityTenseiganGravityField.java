
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
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

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityTenseiganGravityField extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 334;
   public static final int MODE_UP = 0;
   public static final int MODE_DOWN = 1;
   public static final int DURATION_TICKS = 160;
   public static final double RADIUS = (double)10.0F;
   public static final double UP_VELOCITY = (double)1.25F;
   public static final double DOWN_VELOCITY = (double)-1.5F;
   public static final float DOWN_DAMAGE_PER_TICK = 0.8F;
   public static final int DAMAGE_INTERVAL = 5;

   public EntityTenseiganGravityField(ElementsInfTsukAddon instance) {
      super(instance, 961);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "tenseigan_gravity_field"), 334).name("inftsuk_tenseigan_gravity_field").tracker(80, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, FieldRenderer::new);
   }

   public static class EntityCustom extends Entity implements ItemJutsu.IJutsu {
      private static final DataParameter<Integer> MODE;
      private static final DataParameter<Integer> TICK;
      private static final DataParameter<String> SHOOTER_ID;
      private EntityLivingBase shooter;
      private UUID shooterUUID;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.2F, 0.2F);
         this.noClip = true;
         this.isImmuneToFire = true;
      }

      public EntityCustom(World world, EntityLivingBase shooter, int mode) {
         this(world);
         this.shooter = shooter;
         this.shooterUUID = shooter.getUniqueID();
         this.dataManager.set(SHOOTER_ID, this.shooterUUID.toString());
         this.dataManager.set(MODE, mode);
      }

      public Type getJutsuType() {
         return Type.YOTON;
      }

      protected void entityInit() {
         this.dataManager.register(MODE, 0);
         this.dataManager.register(TICK, 0);
         this.dataManager.register(SHOOTER_ID, "");
      }

      public int getMode() {
         return (Integer)this.dataManager.get(MODE);
      }

      public int getFieldTick() {
         return (Integer)this.dataManager.get(TICK);
      }

      @SideOnly(Side.CLIENT)
      public AxisAlignedBB getRenderBoundingBox() {
         return super.getRenderBoundingBox().grow((double)14.0F);
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

      public void onUpdate() {
         super.onUpdate();
         if (!this.world.isBlockLoaded(new BlockPos(this))) {
            this.setDead();
         } else {
            if (this.shooter == null) {
               if (this.shooterUUID == null) {
                  String s = (String)this.dataManager.get(SHOOTER_ID);
                  if (!s.isEmpty()) {
                     try {
                        this.shooterUUID = UUID.fromString(s);
                     } catch (IllegalArgumentException var3) {
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
               this.setPosition(this.shooter.posX, this.shooter.posY, this.shooter.posZ);
            }

            int t = this.getFieldTick();
            int mode = this.getMode();
            if (!this.world.isRemote) {
               if (this.shooter == null || !this.shooter.isEntityAlive()) {
                  this.setDead();
                  return;
               }

               this.applyGravityPulse(mode, t);
               if (t == 0) {
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.PLAYERS, 1.2F, 0.4F);
               }

               if (t % 20 == 0) {
                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERDRAGON_FLAP, SoundCategory.PLAYERS, 0.8F, mode == 0 ? 1.4F : 0.6F);
               }

               this.dataManager.set(TICK, t + 1);
               if (t >= 160) {
                  this.setDead();
               }
            }

            if (!this.world.isRemote && this.world instanceof WorldServer) {
               this.emitPillars(mode, t);
            }

         }
      }

      private void applyGravityPulse(int mode, int t) {
         for(EntityLivingBase e : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX - (double)10.0F, this.posY - (double)10.0F, this.posZ - (double)10.0F, this.posX + (double)10.0F, this.posY + (double)10.0F, this.posZ + (double)10.0F), (ex) -> ex != this.shooter && ex.isEntityAlive() && ItemJutsu.canTarget(ex))) {
            double dx = e.posX - this.posX;
            double dy = e.posY + (double)e.height * (double)0.5F - (this.posY + (double)0.5F);
            double dz = e.posZ - this.posZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (!(distSq > (double)100.0F)) {
               if (mode == 0) {
                  if (e.motionY < (double)1.25F) {
                     e.motionY = (double)1.25F;
                  }

                  e.fallDistance = 0.0F;
                  e.velocityChanged = true;
                  if (t % 20 == 0) {
                     e.addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 25, 2, true, false));
                  }
               } else {
                  e.motionY = (double)-1.5F;
                  e.velocityChanged = true;
                  if (t % 5 == 0) {
                     e.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooter), 0.8F);
                     e.hurtResistantTime = 0;
                     e.attackEntityFrom(DamageSource.OUT_OF_WORLD, 0.4F);
                  }

                  if (t % 10 == 0) {
                     e.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 25, 2, true, false));
                  }
               }
            }
         }

      }

      private void emitPillars(int mode, int t) {
         Random rand = this.world.rand;
         int count = 16;

         for(int i = 0; i < count; ++i) {
            double theta = rand.nextDouble() * Math.PI * (double)2.0F;
            double r = rand.nextDouble() * (double)10.0F;
            double px = this.posX + Math.cos(theta) * r;
            double pz = this.posZ + Math.sin(theta) * r;
            double py;
            double vy;
            if (mode == 0) {
               py = this.posY + rand.nextDouble() * (double)0.5F;
               vy = 0.55 + rand.nextDouble() * (double)0.25F;
            } else {
               py = this.posY + (double)4.5F + rand.nextDouble() * (double)2.0F;
               vy = -0.55 - rand.nextDouble() * (double)0.25F;
            }

            int c = this.pickTenseiganColor(rand);
            Particles.spawnParticle(this.world, Types.SMOKE, px, py, pz, 1, 0.06, 0.02, 0.06, (double)0.0F, vy, (double)0.0F, new int[]{c, 22, 20, 240});
         }

         if (t % 3 == 0) {
            int ringCount = 18;
            double ringR = (double)9.5F;
            double ringY = mode == 0 ? this.posY + 0.1 : this.posY + (double)6.0F;

            for(int i = 0; i < ringCount; ++i) {
               double theta = (double)i / (double)ringCount * Math.PI * (double)2.0F;
               double px = this.posX + Math.cos(theta) * ringR;
               double pz = this.posZ + Math.sin(theta) * ringR;
               Particles.spawnParticle(this.world, Types.SMOKE, px, ringY, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-5313537, 20, 14, 240});
            }
         }

         if (t % 10 == 0) {
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-14261, 45, 14, 240});
            Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + (double)1.0F, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-5313537, 38, 16, 240});
         }

      }

      private int pickTenseiganColor(Random rand) {
         int r = rand.nextInt(5);
         if (r == 0) {
            return -1306;
         } else if (r == 1) {
            return -7286;
         } else if (r == 2) {
            return -14261;
         } else {
            return r == 3 ? -5313537 : -8399105;
         }
      }

      protected void readEntityFromNBT(NBTTagCompound c) {
         this.dataManager.set(TICK, c.getInteger("gfTick"));
         this.dataManager.set(MODE, c.getInteger("gfMode"));
         if (c.hasKey("gfShooter")) {
            try {
               this.shooterUUID = UUID.fromString(c.getString("gfShooter"));
            } catch (IllegalArgumentException var3) {
            }
         }

      }

      protected void writeEntityToNBT(NBTTagCompound c) {
         c.setInteger("gfTick", this.getFieldTick());
         c.setInteger("gfMode", this.getMode());
         if (this.shooterUUID != null) {
            c.setString("gfShooter", this.shooterUUID.toString());
         }

      }

      static {
         MODE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         TICK = EntityDataManager.createKey(EntityCustom.class, DataSerializers.VARINT);
         SHOOTER_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();
      private static final int COOLDOWN_TICKS = 300;

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (entity.world.isRemote) {
            return false;
         } else if (power < 0.3F) {
            return false;
         } else {
            UUID uid = entity.getUniqueID();
            long now = entity.world.getTotalWorldTime();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 300L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               int mode = power < 0.5F ? 0 : 1;
               EntityCustom field = new EntityCustom(entity.world, entity, mode);
               field.setPosition(entity.posX, entity.posY, entity.posZ);
               entity.world.spawnEntity(field);
               if (entity instanceof EntityPlayer) {
                  ((EntityPlayer)entity).sendMessage(new TextComponentString(mode == 0 ? "§b§l↑ Cosmic Gravity: LAUNCH" : "§6§l↓ Cosmic Gravity: CRUSH"));
               }

               return true;
            }
         }
      }

      public float getBasePower() {
         return 0.3F;
      }

      public float getPowerupDelay() {
         return 25.0F;
      }

      public float getMaxPower() {
         return 10.0F;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class FieldRenderer extends Render<EntityCustom> {
      private static final ResourceLocation BLANK = new ResourceLocation("inftsukaddon", "textures/entity/blue/maxblue_frame1.png");

      public FieldRenderer(RenderManager rm) {
         super(rm);
         this.shadowSize = 0.0F;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return BLANK;
      }
   }
}
