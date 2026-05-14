
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityDotonEarthSpear extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 286;
   public static final int BARRAGE_ENTITYID = 303;

   public EntityDotonEarthSpear(ElementsInfTsukAddon instance) {
      super(instance, 920);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "doton_earth_spear"), 286).name("inftsuk_doton_earth_spear").tracker(64, 1, true).build());
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(BarrageController.class).id(new ResourceLocation("inftsukaddon", "doton_earth_spear_barrage"), 303).name("inftsuk_doton_earth_spear_barrage").tracker(64, 1, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, EarthSpearRenderer::new);
      RenderingRegistry.registerEntityRenderingHandler(BarrageController.class, BarrageInvisibleRenderer::new);
   }

   public static class EntityCustom extends EntityThrowable implements ItemJutsu.IJutsu {
      private int lifetime = 0;
      private float damage = 5.0F;
      private int pierceCount = 0;
      private static final int MAX_PIERCES = 2;
      private static final int MAX_LIFETIME = 60;
      private Entity shooterEntity;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.4F, 0.4F);
      }

      public EntityCustom(World world, EntityLivingBase thrower) {
         super(world, thrower);
         this.setSize(0.4F, 0.4F);
         this.shooterEntity = thrower;
      }

      public Type getJutsuType() {
         return Type.DOTON;
      }

      protected void entityInit() {
         super.entityInit();
      }

      public void setDamage(float dmg) {
         this.damage = dmg;
      }

      protected void onImpact(RayTraceResult result) {
         if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.shooterEntity && result.entityHit instanceof EntityLivingBase) {
               result.entityHit.attackEntityFrom(ItemJutsu.causeJutsuDamage(this, this.shooterEntity), this.damage);
               double mx = this.motionX;
               double mz = this.motionZ;
               double len = Math.sqrt(mx * mx + mz * mz);
               if (len > 0.01) {
                  Entity var10000 = result.entityHit;
                  var10000.motionX += mx / len * 0.3;
                  var10000 = result.entityHit;
                  var10000.motionY += 0.05;
                  var10000 = result.entityHit;
                  var10000.motionZ += mz / len * 0.3;
                  result.entityHit.velocityChanged = true;
               }

               ++this.pierceCount;
               double hx = result.entityHit.posX;
               double hy = result.entityHit.posY + (double)result.entityHit.height * (double)0.5F;
               double hz = result.entityHit.posZ;
               double footY = result.entityHit.posY;
               this.spawnPierceParticles(hx, hy, hz, footY);
               SoundEvent rockSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:rocks"));
               if (rockSound != null) {
                  this.world.playSound((EntityPlayer)null, hx, hy, hz, rockSound, SoundCategory.PLAYERS, 0.8F, 0.7F + this.rand.nextFloat() * 0.3F);
               }

               this.world.playSound((EntityPlayer)null, hx, hy, hz, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 1.0F, 0.6F);
               if (this.pierceCount >= 2) {
                  this.setDead();
               }

            } else {
               if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
                  this.spawnImpactParticles(this.posX, this.posY, this.posZ);
                  SoundEvent rockSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:rocks"));
                  if (rockSound != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, rockSound, SoundCategory.PLAYERS, 1.0F, 0.6F);
                  }

                  this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 1.2F, 0.5F);
                  this.setDead();
               }

            }
         }
      }

      private void spawnImpactParticles(double hx, double hy, double hz) {
         int shatterCount = 12 + this.rand.nextInt(4);

         for(int i = 0; i < shatterCount; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double speed = 0.1 + this.rand.nextDouble() * 0.12;
            double irregularity = (double)0.5F + this.rand.nextDouble() * (double)1.0F;
            double vx = Math.cos(angle) * speed * irregularity;
            double vy = 0.05 + this.rand.nextDouble() * 0.1;
            double vz = Math.sin(angle) * speed * irregularity;
            int color = i % 2 == 0 ? -10075102 : -11193583;
            Particles.spawnParticle(this.world, Types.SMOKE, hx, hy, hz, 1, 0.15, 0.15, 0.15, vx, vy, vz, new int[]{color, 25});
         }

         for(int i = 0; i < 10; ++i) {
            double angle = (Math.PI / 5D) * (double)i;
            double ringSpeed = 0.04 + this.rand.nextDouble() * 0.03;
            Particles.spawnParticle(this.world, Types.SMOKE, hx + Math.cos(angle) * 0.3, hy, hz + Math.sin(angle) * 0.3, 1, 0.1, 0.02, 0.1, Math.cos(angle) * ringSpeed, -0.01, Math.sin(angle) * ringSpeed, new int[]{-1433897148, 30});
         }

         int debrisCount = 5 + this.rand.nextInt(2);

         for(int i = 0; i < debrisCount; ++i) {
            Particles.spawnParticle(this.world, Types.SMOKE, hx + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, hy + (double)1.0F, hz + (this.rand.nextDouble() - (double)0.5F) * (double)0.5F, 1, 0.1, 0.05, 0.1, (this.rand.nextDouble() - (double)0.5F) * 0.02, -0.1 - this.rand.nextDouble() * 0.05, (this.rand.nextDouble() - (double)0.5F) * 0.02, new int[]{-12307695, 22});
         }

         for(int i = 0; i < 8; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double dist = this.rand.nextDouble() * (double)1.5F;
            Particles.spawnParticle(this.world, Types.SMOKE, hx + Math.cos(angle) * dist, hy + 0.05, hz + Math.sin(angle) * dist, 1, 0.2, 0.02, 0.2, (this.rand.nextDouble() - (double)0.5F) * 0.005, -0.002, (this.rand.nextDouble() - (double)0.5F) * 0.005, new int[]{1432769843, 40});
         }

      }

      private void spawnPierceParticles(double hx, double hy, double hz, double footY) {
         int pierceShatter = 6 + this.rand.nextInt(3);

         for(int i = 0; i < pierceShatter; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            double speed = 0.06 + this.rand.nextDouble() * 0.08;
            double irregularity = (double)0.5F + this.rand.nextDouble() * (double)1.0F;
            int color = i % 2 == 0 ? -10075102 : -11193583;
            Particles.spawnParticle(this.world, Types.SMOKE, hx, hy, hz, 1, 0.1, 0.1, 0.1, Math.cos(angle) * speed * irregularity, 0.03 + this.rand.nextDouble() * 0.05, Math.sin(angle) * speed * irregularity, new int[]{color, 18});
         }

         for(int i = 0; i < 4; ++i) {
            double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
            Particles.spawnParticle(this.world, Types.SMOKE, hx + (this.rand.nextDouble() - (double)0.5F) * 0.4, footY + 0.05, hz + (this.rand.nextDouble() - (double)0.5F) * 0.4, 1, 0.15, 0.02, 0.15, Math.cos(angle) * 0.02, -0.005, Math.sin(angle) * 0.02, new int[]{-1433897148, 15});
         }

      }

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (this.lifetime <= 60 && this.world.isBlockLoaded(new BlockPos(this))) {
            if (!this.world.isRemote) {
               double horizSpeed = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
               this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
               this.rotationPitch = (float)(Math.atan2(this.motionY, horizSpeed) * (180D / Math.PI));
            }

            if (!this.world.isRemote) {
               if (this.lifetime % 2 == 0) {
                  double speed = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                  double backX = (double)0.0F;
                  double backY = (double)0.0F;
                  double backZ = (double)0.0F;
                  if (speed > 0.01) {
                     backX = -this.motionX / speed * 0.3;
                     backY = -this.motionY / speed * 0.3;
                     backZ = -this.motionZ / speed * 0.3;
                  }

                  int chunkCount = 2 + this.rand.nextInt(2);

                  for(int i = 0; i < chunkCount; ++i) {
                     int color = i % 2 == 0 ? -10075102 : -11193583;
                     double fallSpeed = -0.12 - this.rand.nextDouble() * 0.06;
                     Particles.spawnParticle(this.world, Types.SMOKE, this.posX + backX + (this.rand.nextDouble() - (double)0.5F) * 0.15, this.posY + backY + (this.rand.nextDouble() - (double)0.5F) * 0.1, this.posZ + backZ + (this.rand.nextDouble() - (double)0.5F) * 0.15, 1, 0.06, 0.06, 0.06, (this.rand.nextDouble() - (double)0.5F) * 0.02, fallSpeed, (this.rand.nextDouble() - (double)0.5F) * 0.02, new int[]{color, 12});
                  }
               }

               if (this.lifetime % 2 == 0) {
                  int dustCount = 1 + this.rand.nextInt(2);

                  for(int i = 0; i < dustCount; ++i) {
                     Particles.spawnParticle(this.world, Types.SMOKE, this.posX + (this.rand.nextDouble() - (double)0.5F) * 0.1, this.posY + (this.rand.nextDouble() - (double)0.5F) * 0.05, this.posZ + (this.rand.nextDouble() - (double)0.5F) * 0.1, 1, 0.04, 0.04, 0.04, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{-2005440956, 5});
                  }
               }

               if (this.lifetime == 1) {
                  SoundEvent rockSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:rocks"));
                  if (rockSound != null) {
                     this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, rockSound, SoundCategory.PLAYERS, 1.0F, 0.8F + this.rand.nextFloat() * 0.3F);
                  }
               }
            }

         } else {
            this.setDead();
         }
      }

      protected float getGravityVelocity() {
         return 0.02F;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("lifetime", this.lifetime);
         compound.setFloat("esDamage", this.damage);
         compound.setInteger("esPierces", this.pierceCount);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("lifetime");
         if (compound.hasKey("esDamage")) {
            this.damage = compound.getFloat("esDamage");
         }

         this.pierceCount = compound.getInteger("esPierces");
      }
   }

   public static class BarrageController extends Entity {
      private EntityLivingBase caster;
      private int ticksAlive;
      private float power;
      private static final int BARRAGE_DURATION = 60;

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
            if (this.ticksAlive % 2 == 0 && this.ticksAlive <= 60) {
               this.fireSpear(this.caster);
            }

            if (this.ticksAlive > 65) {
               this.setDead();
            }

         } else {
            this.setDead();
         }
      }

      private void fireSpear(EntityLivingBase entity) {
         Vec3d look = entity.getLookVec();
         double spd = (double)2.0F;
         float dmg = 12.0F + this.power * 5.0F;
         double yawOffset = (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * Math.toRadians((double)5.0F);
         double pitchOffset = (this.rand.nextDouble() - (double)0.5F) * (double)2.0F * Math.toRadians((double)3.0F);
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
         EntityCustom spear = new EntityCustom(entity.world, entity);
         spear.setPosition(spawnX, spawnY, spawnZ);
         spear.motionX = newX * spd;
         spear.motionY = newY * spd;
         spear.motionZ = newZ * spd;
         spear.setDamage(dmg);
         entity.world.spawnEntity(spear);
         SoundEvent rockSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:rocks"));
         if (rockSound != null) {
            entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, rockSound, SoundCategory.PLAYERS, 0.4F, 0.7F + this.rand.nextFloat() * 0.6F);
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
            if (cooldownMap.containsKey(uid) && now - (Long)cooldownMap.get(uid) < 60L) {
               return false;
            } else {
               cooldownMap.put(uid, now);
               BarrageController controller = new BarrageController(entity.world, entity, power);
               entity.world.spawnEntity(controller);
               SoundEvent rockSound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:rocks"));
               if (rockSound != null) {
                  entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, rockSound, SoundCategory.PLAYERS, 1.2F, 0.6F);
               }

               entity.world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.PLAYERS, 0.8F, 0.5F);
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
         return 5.0F;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase player, float power) {
         if (!player.world.isRemote) {
            Particles.spawnParticle(player.world, Types.SMOKE, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.6, player.posY + 0.2, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.6, 2, 0.15, 0.05, 0.15, (double)0.0F, 0.04, (double)0.0F, new int[]{-10075102, 15});
            if (player.ticksExisted % 3 == 0) {
               Particles.spawnParticle(player.world, Types.SMOKE, player.posX + (player.getRNG().nextDouble() - (double)0.5F) * 0.4, player.posY + 0.15, player.posZ + (player.getRNG().nextDouble() - (double)0.5F) * 0.4, 1, 0.1, 0.1, 0.1, (double)0.0F, 0.03, (double)0.0F, new int[]{-11193583, 18});
            }
         }

         super.onUsingTick(stack, player, power);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelSpike extends ModelBase {
      private final ModelRenderer bone;

      public ModelSpike() {
         this.textureWidth = 32;
         this.textureHeight = 32;
         this.bone = new ModelRenderer(this);
         this.bone.setRotationPoint(0.0F, 0.0F, 0.0F);
         ModelRenderer bone2 = new ModelRenderer(this);
         bone2.setRotationPoint(0.0F, 0.0F, 4.0F);
         this.bone.addChild(bone2);
         this.setRotationAngle(bone2, 0.1309F, 0.0F, 0.0F);
         bone2.cubeList.add(new ModelBox(bone2, 0, 0, -4.0F, -32.0F, 0.0F, 8, 32, 0, 0.0F, false));
         ModelRenderer bone3 = new ModelRenderer(this);
         bone3.setRotationPoint(0.0F, 0.0F, -4.0F);
         this.bone.addChild(bone3);
         this.setRotationAngle(bone3, -0.1309F, 0.0F, 0.0F);
         bone3.cubeList.add(new ModelBox(bone3, 8, 0, -4.0F, -32.0F, 0.0F, 8, 32, 0, 0.0F, false));
         ModelRenderer bone4 = new ModelRenderer(this);
         bone4.setRotationPoint(4.0F, 0.0F, 0.0F);
         this.bone.addChild(bone4);
         this.setRotationAngle(bone4, -0.1309F, -1.5708F, 0.0F);
         bone4.cubeList.add(new ModelBox(bone4, 8, 0, -4.0F, -32.0F, 0.0F, 8, 32, 0, 0.0F, false));
         ModelRenderer bone5 = new ModelRenderer(this);
         bone5.setRotationPoint(-4.0F, 0.0F, 0.0F);
         this.bone.addChild(bone5);
         this.setRotationAngle(bone5, 0.1309F, -1.5708F, 0.0F);
         bone5.cubeList.add(new ModelBox(bone5, 0, 0, -4.0F, -32.0F, 0.0F, 8, 32, 0, 0.0F, false));
         ModelRenderer bone6 = new ModelRenderer(this);
         bone6.setRotationPoint(0.0F, 0.0F, 0.0F);
         this.bone.addChild(bone6);
         this.setRotationAngle(bone6, -1.5708F, 0.0F, 0.0F);
         bone6.cubeList.add(new ModelBox(bone6, 16, 24, -4.0F, -4.0F, 0.0F, 8, 8, 0, 0.0F, false));
      }

      public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         this.bone.render(scale);
      }

      private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class EarthSpearRenderer extends Render<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod", "textures/woodblock.png");
      private final ModelSpike model = new ModelSpike();

      public EarthSpearRenderer(RenderManager renderManager) {
         super(renderManager);
         this.shadowSize = 0.0F;
      }

      public boolean shouldRender(EntityCustom entity, ICamera camera, double camX, double camY, double camZ) {
         return true;
      }

      public void doRender(EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
         GlStateManager.pushMatrix();
         this.bindEntityTexture(entity);
         GlStateManager.translate(x, y, z);
         float yawAngle = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
         float pitchAngle = -(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks);
         GlStateManager.rotate(yawAngle, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(pitchAngle - 90.0F, 1.0F, 0.0F, 0.0F);
         float scale = 0.75F;
         GlStateManager.scale(scale, scale * 1.5F, scale);
         GlStateManager.disableCull();
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.disableLighting();
         GlStateManager.color(0.55F, 0.35F, 0.2F, 0.95F);
         this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableLighting();
         GlStateManager.disableBlend();
         GlStateManager.enableCull();
         GlStateManager.popMatrix();
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
