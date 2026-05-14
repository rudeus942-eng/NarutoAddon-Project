
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityWindCutter extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 273;

   public EntityWindCutter(ElementsInfTsukAddon instance) {
      super(instance, 903);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "wind_cutter"), 273).name("inftsuk_wind_cutter").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, WindCutterRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable implements ItemJutsu.IJutsu {
      private static final DataParameter<Float> MODEL_SCALE;
      private int lifetime = 0;
      private float damage = 5.0F;
      private int hitCount = 0;
      private static final int MAX_HITS = 3;
      private static final int MAX_LIFETIME = 40;
      private Entity shooterEntity;

      public EntityCustom(World world) {
         super(world);
         this.setSize(1.5F, 0.3F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(1.5F, 0.3F);
         this.shooterEntity = thrower;
      }

      public Type getJutsuType() {
         return Type.FUTON;
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(MODEL_SCALE, 0.8F);
      }

      public float getEntityScale() {
         return (Float)this.dataManager.get(MODEL_SCALE);
      }

      public void setEntityScale(float scale) {
         this.dataManager.set(MODEL_SCALE, scale);
      }

      public void setDamage(float dmg) {
         this.damage = dmg;
      }

      public void setLifetime(int ticks) {
         this.lifetime = ticks;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.shooterEntity && result.entityHit instanceof EntityLivingBase) {
               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooterEntity), this.damage);
               result.entityHit.hurtResistantTime = 0;
               double mx = this.motionX;
               double mz = this.motionZ;
               double len = Math.sqrt(mx * mx + mz * mz);
               if (len > 0.01) {
                  Entity var10000 = result.entityHit;
                  var10000.motionX += mx / len * (double)0.5F;
                  var10000 = result.entityHit;
                  var10000.motionY += 0.2;
                  var10000 = result.entityHit;
                  var10000.motionZ += mz / len * (double)0.5F;
                  result.entityHit.velocityChanged = true;
               }

               ++this.hitCount;
               double hx = result.entityHit.posX;
               double hy = result.entityHit.posY + (double)result.entityHit.height * (double)0.5F;
               double hz = result.entityHit.posZ;
               Particles.spawnParticle(this.world, Types.SMOKE, hx, hy, hz, 15, 1.2, 0.15, 1.2, 0.1, 0.02, 0.1, new int[]{-2232577, 25});
               Particles.spawnParticle(this.world, Types.SMOKE, hx, hy, hz, 10, (double)1.5F, 0.1, (double)1.5F, 0.15, (double)0.0F, 0.15, new int[]{-2232577, 18});
               Particles.spawnParticle(this.world, Types.SMOKE, hx, hy + 0.3, hz, 8, 0.2, 0.1, 0.2, (double)0.0F, 0.18, (double)0.0F, new int[]{-2232577, 28});
               Particles.spawnParticle(this.world, Types.SMOKE, hx, hy, hz, 6, 0.4, 0.3, 0.4, 0.01, 0.06, 0.01, new int[]{-1118465, 20});
               SoundEvent windHit = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
               if (windHit != null) {
                  this.world.playSound((EntityPlayer)null, hx, hy, hz, windHit, SoundCategory.PLAYERS, 0.7F, 1.3F + this.rand.nextFloat() * 0.4F);
               }

               for(int s = 0; s < 5; ++s) {
                  EntityWindSlash.EntityCustom slash = new EntityWindSlash.EntityCustom(this.world, (EntityLivingBase)this.shooterEntity, this.damage * 0.3F, this.rand.nextFloat() * 360.0F);
                  slash.renderScale = 3.0F;
                  slash.setPosition(result.entityHit.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F, result.entityHit.posY + this.rand.nextDouble() * (double)result.entityHit.height, result.entityHit.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.5F);
                  slash.rotationYaw = this.rand.nextFloat() * 360.0F;
                  slash.rotationPitch = -90.0F + this.rand.nextFloat() * 180.0F;
                  this.world.spawnEntity(slash);
               }

               if (this.hitCount >= 3) {
                  this.setDead();
               }

            } else {
               if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY, this.posZ, 12, 0.8, 0.4, 0.8, 0.08, 0.06, 0.08, new int[]{-2232577, 18});
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX, this.posY + 0.2, this.posZ, 5, 0.2, 0.1, 0.2, (double)0.0F, 0.12, (double)0.0F, new int[]{-1118465, 22});
                  SoundEvent windBlock = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
                  if (windBlock != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, windBlock, SoundCategory.PLAYERS, 0.5F, 1.5F);
                  }

                  this.setDead();
               }

            }
         }
      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 40 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               if (this.lifetime > 2) {
                  List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow((double)15.0F), (ex) -> ex != this.shooterEntity && ex.isEntityAlive() && ex instanceof EntityLivingBase && ItemJutsu.canTarget(ex));
                  if (!targets.isEmpty()) {
                     Entity closest = null;
                     double closestDist = Double.MAX_VALUE;

                     for(EntityLivingBase e : targets) {
                        double d = this.getDistanceSq(e);
                        if (d < closestDist) {
                           closestDist = d;
                           closest = e;
                        }
                     }

                     if (closest != null) {
                        double tdx = closest.posX - this.posX;
                        double tdy = closest.posY + (double)closest.height * (double)0.5F - this.posY;
                        double tdz = closest.posZ - this.posZ;
                        double tdist = Math.sqrt(tdx * tdx + tdy * tdy + tdz * tdz);
                        if (tdist > (double)0.5F) {
                           double speed = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                           double aimX = tdx / tdist * speed;
                           double aimY = tdy / tdist * speed;
                           double aimZ = tdz / tdist * speed;
                           this.motionX = this.motionX * 0.8 + aimX * 0.2;
                           this.motionY = this.motionY * 0.8 + aimY * 0.2;
                           this.motionZ = this.motionZ * 0.8 + aimZ * 0.2;
                           double newSpeed = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                           if (newSpeed > 0.01) {
                              double ratio = speed / newSpeed;
                              this.motionX *= ratio;
                              this.motionY *= ratio;
                              this.motionZ *= ratio;
                           }
                        }
                     }
                  }
               }

               if (this.lifetime % 2 == 0) {
                  Particles.spawnParticle(this.world, Types.SMOKE, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.8, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.3, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.8, 3, 0.08, 0.08, 0.08, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-2232577, 12});
               }

               if (this.lifetime == 1) {
                  SoundEvent windSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
                  if (windSound != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, windSound, SoundCategory.PLAYERS, 0.6F, 1.2F + this.rand.nextFloat() * 0.3F);
                  }
               }
            }

         } else {
            this.setDead();
         }
      }

      protected float getGravityVelocity() {
         return 0.0F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("wcDamage", this.damage);
         compound.setInteger("wcHits", this.hitCount);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("wcDamage")) {
            this.damage = compound.getFloat("wcDamage");
         }

         this.hitCount = compound.getInteger("wcHits");
      }

      static {
         MODEL_SCALE = EntityDataManager.createKey(EntityCustom.class, DataSerializers.FLOAT);
      }
   }

   public static class WaveController extends Entity {
      private EntityLivingBase caster;
      private int ticksAlive;
      private float power;
      private boolean wave1Fired;
      private boolean wave2Fired;
      private boolean wave3Fired;

      public WaveController(World world) {
         super(world);
         this.ticksAlive = 0;
         this.wave1Fired = false;
         this.wave2Fired = false;
         this.wave3Fired = false;
         this.setSize(0.1F, 0.1F);
         this.noClip = true;
         this.setInvisible(true);
      }

      public WaveController(World world, EntityLivingBase caster, float power) {
         this(world);
         this.caster = caster;
         this.power = power;
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
            double spd = (double)2.0F;
            float dmg = 21.0F + this.power * 8.4F;
            if (this.ticksAlive == 1 && !this.wave1Fired) {
               this.wave1Fired = true;
               this.fireBlades(this.caster, 1, dmg, spd, false);
               this.playWindSound(this.caster);
            }

            boolean fullCharge = this.power >= 7.2F;
            if (this.ticksAlive == 16 && !this.wave2Fired && fullCharge) {
               this.wave2Fired = true;
               this.fireBlades(this.caster, 2, dmg, spd, false);
               this.playWindSound(this.caster);
            }

            if (this.ticksAlive == 31 && !this.wave3Fired && fullCharge) {
               this.wave3Fired = true;
               this.fireBlades(this.caster, 3, dmg, spd, true);
               this.playWindSound(this.caster);
            }

            if (this.ticksAlive > 35) {
               this.setDead();
            }

         } else {
            this.setDead();
         }
      }

      private void fireBlades(EntityLivingBase entity, int count, float dmg, double spd, boolean fanSpread) {
         Vec3d look = entity.getLookVec();
         double spawnX = entity.posX + look.x * (double)1.5F;
         double spawnY = entity.posY + (double)entity.getEyeHeight() + look.y * (double)1.5F;
         double spawnZ = entity.posZ + look.z * (double)1.5F;
         if (fanSpread) {
            double[] angles = new double[]{-0.14, (double)0.0F, 0.14};

            for(double angle : angles) {
               EntityCustom blade = new EntityCustom(entity.world, entity);
               blade.setPosition(spawnX, spawnY, spawnZ);
               double cosA = Math.cos(angle);
               double sinA = Math.sin(angle);
               blade.motionX = (look.x * cosA - look.z * sinA) * spd;
               blade.motionY = look.y * spd;
               blade.motionZ = (look.x * sinA + look.z * cosA) * spd;
               blade.setDamage(dmg);
               entity.world.spawnEntity(blade);
            }
         } else {
            for(int i = 0; i < count; ++i) {
               EntityCustom blade = new EntityCustom(entity.world, entity);
               blade.setPosition(spawnX, spawnY + (i == 0 ? 0.15 : -0.15), spawnZ);
               blade.motionX = look.x * spd;
               blade.motionY = look.y * spd;
               blade.motionZ = look.z * spd;
               blade.setDamage(dmg);
               entity.world.spawnEntity(blade);
            }
         }

      }

      private void playWindSound(EntityLivingBase entity) {
         SoundEvent windSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:windecho"));
         if (windSound != null) {
            entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, windSound, SoundCategory.PLAYERS, 1.0F, 0.9F + entity.getRNG().nextFloat() * 0.3F);
         }

      }
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      private static final Map<UUID, Long> cooldownMap = new WeakHashMap();

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (power < 0.4F) {
            return false;
         } else if (entity.world.isRemote) {
            return false;
         } else {
            long now = entity.world.getTotalWorldTime();
            UUID uid = entity.getUniqueID();
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 15L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               WaveController controller = new WaveController(entity.world, entity, power);
               controller.setPosition(entity.posX, entity.posY, entity.posZ);
               entity.world.spawnEntity(controller);
               return true;
            }
         }
      }

      public float getBasePower() {
         return 0.4F;
      }

      public float getPowerupDelay() {
         return 20.0F;
      }

      public float getMaxPower() {
         return 8.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote) {
            double angle = (double)player.ticksExisted * 0.3;
            double offX = Math.cos(angle) * 0.4;
            double offZ = Math.sin(angle) * 0.4;
            Particles.spawnParticle(player.world, Types.SMOKE, player.posX + offX, player.posY + 0.15, player.posZ + offZ, 1, 0.05, 0.05, 0.05, (double)0.0F, 0.02, (double)0.0F, new int[]{-2232577, 10});
         }

         super.onUsingTick(stack, player, power);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class WindCutterRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/disk.png");

      public WindCutterRenderer(RenderManager renderManager) {
         super(renderManager);
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         float scale = entity.getEntityScale();
         GlStateManager.translate(x, y + 0.15 * (double)scale, z);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(scale, scale, scale);
         double vmx = entity.posX - entity.prevPosX;
         double vmy = entity.posY - entity.prevPosY;
         double vmz = entity.posZ - entity.prevPosZ;
         double horizLen = Math.sqrt(vmx * vmx + vmz * vmz);
         if (horizLen > 0.001) {
            float yawAngle = -((float)(Math.atan2(vmx, vmz) * (180D / Math.PI)));
            float pitchAngle = (float)(Math.atan2(vmy, horizLen) * (180D / Math.PI));
            GlStateManager.rotate(yawAngle, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(pitchAngle, 1.0F, 0.0F, 0.0F);
         }

         GlStateManager.rotate(70.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.enableBlend();
         GlStateManager.disableLighting();
         GlStateManager.disableCull();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.color(0.8F, 0.85F, 0.95F, 0.95F);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         float hw = 2.8F;
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
         buffer.pos((double)(-hw), (double)(-hw), (double)0.0F).tex((double)0.0F, (double)1.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
         buffer.pos((double)hw, (double)(-hw), (double)0.0F).tex((double)1.0F, (double)1.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
         buffer.pos((double)hw, (double)hw, (double)0.0F).tex((double)1.0F, (double)0.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
         buffer.pos((double)(-hw), (double)hw, (double)0.0F).tex((double)0.0F, (double)0.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
         tessellator.draw();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.disableBlend();
         GlStateManager.disableRescaleNormal();
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }
   }
}
