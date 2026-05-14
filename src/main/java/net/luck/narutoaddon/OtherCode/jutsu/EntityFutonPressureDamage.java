
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
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
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

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityFutonPressureDamage extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 282;
   public static final int BARRAGE_ENTITYID = 304;

   public EntityFutonPressureDamage(ElementsInfTsukAddon instance) {
      super(instance, 912);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "futon_pressure_damage"), 282).name("inftsuk_futon_pressure_damage").tracker(64, 1, true).build());
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(BarrageController.class).id(new ResourceLocation("inftsukaddon", "futon_pressure_damage_barrage"), 304).name("inftsuk_futon_pressure_damage_barrage").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, PressureDamageRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(BarrageController.class, BarrageInvisibleRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable implements ItemJutsu.IJutsu {
      private static final DataParameter<Float> MODEL_SCALE;
      private int lifetime = 0;
      private float power = 1.0F;
      private static final int MAX_LIFETIME = 100;
      private static final double INNER_RADIUS = (double)3.0F;
      private static final double OUTER_RADIUS = (double)6.0F;
      private EntityLivingBase shooterEntity;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.6F, 0.6F);
      }

      public EntityCustom(World world, EntityLivingBase thrower, float power) {
         super(world, thrower);
         this.setSize(0.6F, 0.6F);
         this.shooterEntity = thrower;
         this.power = power;
      }

      public Type getJutsuType() {
         return Type.FUTON;
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(MODEL_SCALE, 1.5F);
      }

      public float getEntityScale() {
         return (Float)this.dataManager.get(MODEL_SCALE);
      }

      public void setEntityScale(float scale) {
         this.dataManager.set(MODEL_SCALE, scale);
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            boolean shouldDetonate = false;
            if (result.entityHit != null && result.entityHit != this.shooterEntity && result.entityHit instanceof EntityLivingBase) {
               shouldDetonate = true;
            } else if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
               shouldDetonate = true;
            }

            if (shouldDetonate) {
               this.detonate();
               this.setDead();
            }

         }
      }

      private void detonate() {
         double dx = this.posX;
         double dy = this.posY;
         double dz = this.posZ;
         float fullDamage = 25.0F + this.power * 15.0F;

         for(EntityLivingBase target : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(dx - (double)6.0F, dy - (double)6.0F, dz - (double)6.0F, dx + (double)6.0F, dy + (double)6.0F, dz + (double)6.0F), (e) -> e != this.shooterEntity && e.isEntityAlive() && ItemJutsu.canTarget(e))) {
            double dist = target.getDistance(dx, dy, dz);
            if (!(dist > (double)6.0F)) {
               float dmg;
               if (dist <= (double)3.0F) {
                  dmg = fullDamage;
               } else {
                  dmg = fullDamage * 0.6F;
               }

               if (!(target instanceof EntityPlayer)) {
                  dmg = Math.min(dmg, 100.0F);
               }

               target.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooterEntity), dmg);
               target.hurtResistantTime = 0;
               target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1, false, false));
            }
         }

         Particles.spawnParticle(this.world, Types.SMOKE, dx, dy, dz, 12, 0.2, 0.2, 0.2, 0.15, 0.15, 0.15, new int[]{-1, 2});
         Particles.spawnParticle(this.world, Types.SMOKE, dx, dy, dz, 25, 0.8, 0.8, 0.8, (double)0.25F, (double)0.25F, (double)0.25F, new int[]{-1118465, 6});

         for(int i = 0; i < 24; ++i) {
            double angle = (double)i / (double)24.0F * Math.PI * (double)2.0F;
            Particles.spawnParticle(this.world, Types.SMOKE, dx + Math.cos(angle) * 0.3, dy + 0.1, dz + Math.sin(angle) * 0.3, 1, 0.03, 0.01, 0.03, Math.cos(angle) * (double)0.5F, (double)0.0F, Math.sin(angle) * (double)0.5F, new int[]{-2232577, 8});
         }

         Particles.spawnParticle(this.world, Types.SMOKE, dx, dy + 0.3, dz, 15, 0.3, 0.1, 0.3, 0.02, 0.6, 0.02, new int[]{-2236946, 10});
         Particles.spawnParticle(this.world, Types.SMOKE, dx, dy + 0.05, dz, 12, (double)2.0F, 0.03, (double)2.0F, 0.12, (double)0.0F, 0.12, new int[]{-1429418804, 12});
         Particles.spawnParticle(this.world, Types.SMOKE, dx, dy + (double)0.5F, dz, 8, (double)2.5F, (double)1.0F, (double)2.5F, 0.01, 0.02, 0.01, new int[]{1156509439, 20});
         SoundEvent windHit = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
         if (windHit != null) {
            this.world.playSound((EntityPlayer)null, dx, dy, dz, windHit, SoundCategory.PLAYERS, 1.2F, 0.6F + this.rand.nextFloat() * 0.4F);
         }

      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 100 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               double angle = (double)this.lifetime * 0.6;
               double offX = Math.cos(angle) * (double)0.25F;
               double offZ = Math.sin(angle) * (double)0.25F;
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX + offX, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.2, this.posZ + offZ, 8, 0.08, 0.08, 0.08, -offX * 0.05, (double)0.0F, -offZ * 0.05, new int[]{-1141969153, 10});
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 2, 0.15, 0.1, 0.15, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{1728053247, 8});
               Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 3, 0.03, 0.03, 0.03, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-1, 6});
               if (this.lifetime == 1) {
                  SoundEvent windSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
                  if (windSound != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, windSound, SoundCategory.PLAYERS, 0.8F, 0.5F);
                  }
               }

               if (this.lifetime % 15 == 0) {
                  SoundEvent windLoop = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
                  if (windLoop != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, windLoop, SoundCategory.PLAYERS, 0.5F, 0.6F + this.rand.nextFloat() * 0.2F);
                  }
               }
            }

         } else {
            this.setDead();
         }
      }

      protected float getGravityVelocity() {
         return 0.005F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("pdPower", this.power);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("pdPower")) {
            this.power = compound.getFloat("pdPower");
         }

      }

      static {
         MODEL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (power < 0.3F) {
            return false;
         } else if (entity.world.isRemote) {
            return false;
         } else {
            long now = entity.world.getTotalWorldTime();
            UUID uid = entity.getUniqueID();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 60L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               BarrageController controller = new BarrageController(entity.world, entity, power);
               entity.world.spawnEntity(controller);
               SoundEvent windCast = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
               if (windCast != null) {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, windCast, SoundCategory.PLAYERS, 1.2F, 0.5F);
               }

               return true;
            }
         }
      }

      public float getBasePower() {
         return 1.0F;
      }

      public float getPowerupDelay() {
         return 25.0F;
      }

      public float getMaxPower() {
         return 5.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote) {
            double angle = (double)player.ticksExisted * 0.4;
            double offX = Math.cos(angle) * 0.35;
            double offZ = Math.sin(angle) * 0.35;
            Particles.spawnParticle(player.world, Types.SMOKE, player.posX + offX, player.posY + 0.15, player.posZ + offZ, 2, 0.05, 0.05, 0.05, -offX * 0.1, 0.01, -offZ * 0.1, new int[]{-1998725650, 10});
            if (player.ticksExisted % 3 == 0) {
               double a2 = angle + Math.PI;
               Particles.spawnParticle(player.world, Types.SMOKE, player.posX + Math.cos(a2) * 0.7, player.posY + 0.15, player.posZ + Math.sin(a2) * 0.7, 1, 0.02, 0.02, 0.02, -Math.cos(a2) * 0.1, (double)0.0F, -Math.sin(a2) * 0.1, new int[]{-1997607186, 8});
            }
         }

         super.onUsingTick(stack, player, power);
      }
   }

   public static class BarrageController extends Entity {
      private EntityLivingBase caster;
      private int ticksAlive;
      private float power;
      private static final int BARRAGE_DURATION = 40;

      public BarrageController(World world) {
         super(world);
         this.ticksAlive = 0;
         this.setSize(0.1F, 0.1F);
         this.noClip = true;
         this.setInvisible(true);
      }

      public BarrageController(World world, EntityLivingBase caster, float power) {
         this(world);
         this.caster = caster;
         this.power = power;
         this.setPosition(caster.posX, caster.posY, caster.posZ);
      }

      protected void entityInit() {
      }

      protected void readEntityFromNBT(NBTTagCompound c) {
         this.setDead();
      }

      protected void writeEntityToNBT(NBTTagCompound c) {
      }

      public void onUpdate() {
         if (!this.world.isRemote && this.caster != null && this.caster.isEntityAlive() && this.world.isBlockLoaded(new BlockPos(this))) {
            ++this.ticksAlive;
            if (this.ticksAlive % 4 == 0 && this.ticksAlive <= 40) {
               this.fireOrb(this.caster);
            }

            if (this.ticksAlive > 45) {
               this.setDead();
            }

         } else {
            this.setDead();
         }
      }

      private void fireOrb(EntityLivingBase entity) {
         Vec3d look = entity.getLookVec();
         double spd = 1.8;
         double yawOffset = (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * Math.toRadians((double)4.0F);
         double pitchOffset = (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * Math.toRadians((double)2.0F);
         double cosYaw = Math.cos(yawOffset);
         double sinYaw = Math.sin(yawOffset);
         double newX = look.x * cosYaw - look.z * sinYaw;
         double newZ = look.x * sinYaw + look.z * cosYaw;
         double horizLen = Math.sqrt(newX * newX + newZ * newZ);
         double currentPitch = Math.atan2(look.y, horizLen);
         double newPitch = currentPitch + pitchOffset;
         double newHoriz = Math.cos(newPitch);
         double newY = Math.sin(newPitch);
         if (horizLen > 0.001) {
            double scale = newHoriz / horizLen;
            newX *= scale;
            newZ *= scale;
         }

         double spawnX = entity.posX + newX * (double)1.5F;
         double spawnY = entity.posY + (double)entity.getEyeHeight() + newY * (double)1.5F;
         double spawnZ = entity.posZ + newZ * (double)1.5F;
         EntityCustom orb = new EntityCustom(entity.world, entity, this.power);
         orb.setPosition(spawnX, spawnY, spawnZ);
         orb.motionX = newX * spd;
         orb.motionY = newY * spd;
         orb.motionZ = newZ * spd;
         entity.world.spawnEntity(orb);
         SoundEvent windSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
         if (windSound != null) {
            entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, windSound, SoundCategory.PLAYERS, 0.4F, 0.5F + this.rand.nextFloat() * 0.4F);
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public static class PressureDamageRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/disk.png");

      public PressureDamageRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         float scale = entity.getEntityScale();
         GlStateManager.translate(x, y + 0.3 * (double)scale, z);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(scale, scale, scale);
         float spin = ((float)entity.ticksExisted + partialTicks) * 3.0F;
         GlStateManager.rotate(spin, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.enableBlend();
         GlStateManager.disableLighting();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.color(0.9F, 0.95F, 1.0F, 0.9F);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         float hw = 1.8F;
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         buffer.pos((double)(-hw), (double)(-hw), (double)0.0F).tex((double)0.0F, (double)1.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
         buffer.pos((double)hw, (double)(-hw), (double)0.0F).tex((double)1.0F, (double)1.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
         buffer.pos((double)hw, (double)hw, (double)0.0F).tex((double)1.0F, (double)0.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
         buffer.pos((double)(-hw), (double)hw, (double)0.0F).tex((double)0.0F, (double)0.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
         tessellator.draw();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableLighting();
         GlStateManager.disableBlend();
         GlStateManager.disableRescaleNormal();
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class BarrageInvisibleRenderer extends Render<BarrageController> {
      public BarrageInvisibleRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(BarrageController entity, double x, double y, double z, float entityYaw, float partialTicks) {
      }

      protected ResourceLocation getEntityTexture(BarrageController entity) {
         return null;
      }
   }
}
