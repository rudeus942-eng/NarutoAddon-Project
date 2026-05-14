
package net.luck.narutoaddon.OtherCode.entity;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.ElementsluckAddonAddon;
import net.luck.narutoaddon.OtherCode.luckAddonAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Particles;
import net.narutomod.Particles.Types;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@ElementsluckAddonAddon.ModElement.Tag
public class EntityOrochimaruSnake extends ElementsluckAddonAddon.ModElement {
   public static final int ENTITYID = 300;

   public EntityOrochimaruSnake(ElementsluckAddonAddon instance) {
      super(instance, 900);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "orochimaru_snake"), 300).name("orochimaru_snake").tracker(128, 1, true).build());
      this.elements.addNetworkMessage(SnakeSegmentSyncMessage.Handler.class, SnakeSegmentSyncMessage.class, Side.CLIENT);
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, RenderOrochimaruSnake::new);
   }

   public static Vec3d getOffsetPoint(float x, float y, float z, float rotateX, float rotateY, float offset) {
      return (new Vec3d((double)(-x) - Math.sin((double)rotateY) * Math.cos((double)rotateX) * (double)offset, (double)(-y) + Math.sin((double)rotateX) * (double)offset, (double)(-z) - Math.cos((double)rotateY) * Math.cos((double)rotateX) * (double)offset)).scale((double)-1.0F);
   }

   public static class Vec2f {
      public static final Vec2f ZERO = new Vec2f(0.0F, 0.0F);
      public final float x;
      public final float y;

      public Vec2f(float x, float y) {
         this.x = x;
         this.y = y;
      }
   }

   public static class EntityCustom extends EntityMob implements IEntityMultiPart {
      private static final DataParameter<String> OWNER_UUID;
      private SnakeSegment[] parts;
      private List<Vec2f> partRot;
      private boolean needsSync;
      private int lifetimeTicks = 0;
      private static final int TAIL_SLAM_INTERVAL = 80;
      private int tailSlamTimer = 0;
      private double lastPosX;
      private double lastPosZ;
      private float lastYaw;

      public EntityCustom(World world) {
         super(world);
         this.setSize(1.8F, 1.5F);
         this.isImmuneToFire = true;
         this.enablePersistence();
         this.stepHeight = 2.0F;
         this.parts = new SnakeSegment[22];
         this.partRot = new ArrayList();
         float scale = 0.75F;
         this.parts[0] = new SnakeSegment(this, "head", 0.3F * scale, 0.3F * scale);

         for(int i = 1; i < this.parts.length; ++i) {
            this.parts[i] = new SnakeSegment(this, "segment" + i, 0.25F * scale, 0.25F * scale);
            this.partRot.add(Vec2f.ZERO);
         }

      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(OWNER_UUID, "");
      }

      protected void initEntityAI() {
         this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.2, false));
         this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F));
         this.tasks.addTask(4, new EntityAILookIdle(this));
         this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      }

      public void setAttackTarget(@Nullable EntityLivingBase target) {
         if (target != null && !(target instanceof EntityPlayer)) {
            String cn = target.getClass().getName().toLowerCase();
            if (!cn.contains("icedome") && !cn.contains("shieldbase")) {
               return;
            }
         }

         super.setAttackTarget(target);
      }

      public void setRevengeTarget(@Nullable EntityLivingBase target) {
         if (target == null || target instanceof EntityPlayer) {
            super.setRevengeTarget(target);
         }
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)5000.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.55);
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)10.0F);
         this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)1.0F);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)20.0F);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)40.0F);
      }

      public Entity[] getParts() {
         return this.parts;
      }

      public World getWorld() {
         return this.world;
      }

      public boolean attackEntityFromPart(MultiPartEntityPart part, DamageSource source, float damage) {
         if (!(part instanceof SnakeSegment)) {
            return this.attackEntityFrom(source, damage);
         } else {
            SnakeSegment seg = (SnakeSegment)part;
            int index = 0;

            for(int i = 0; i < this.parts.length; ++i) {
               if (this.parts[i] == seg) {
                  index = i;
                  break;
               }
            }

            float mult = 1.0F - (float)index / (float)this.parts.length;
            return this.attackEntityFrom(source, damage * mult);
         }
      }

      public boolean attackEntityAsMob(Entity target) {
         if (target instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase)target;
            if (this.isIceDome(living)) {
               living.hurtResistantTime = 0;
               living.attackEntityFrom(DamageSource.causeMobDamage(this), 200.0F);
               return true;
            } else {
               living.hurtResistantTime = 0;
               living.attackEntityFrom(DamageSource.causeMobDamage(this), 20.0F);
               living.hurtResistantTime = 0;
               living.attackEntityFrom(DamageSource.MAGIC, 8.0F);
               double dx = living.posX - this.posX;
               double dz = living.posZ - this.posZ;
               double d = Math.sqrt(dx * dx + dz * dz);
               if (d > (double)0.0F) {
                  living.motionX = dx / d * 0.8;
                  living.motionY = 0.3;
                  living.motionZ = dz / d * 0.8;
               }

               if (living instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)living).velocityChanged = true;
               }

               this.world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.HOSTILE, 1.2F, 0.6F);
               return true;
            }
         } else {
            return false;
         }
      }

      public void onUpdate() {
         super.onUpdate();
         if (!this.world.isRemote) {
            ++this.lifetimeTicks;
            EntityLivingBase target = this.getAttackTarget();
            if (target != null && target.isEntityAlive() && this.ticksExisted % 5 == 0) {
               double dist = (double)this.getDistance(target);
               if (dist > (double)3.0F && dist < (double)30.0F) {
                  double dx = target.posX - this.posX;
                  double dz = target.posZ - this.posZ;
                  double len = Math.sqrt(dx * dx + dz * dz);
                  if (len > (double)0.0F) {
                     double speed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
                     this.motionX = dx / len * speed * (double)1.5F;
                     this.motionZ = dz / len * speed * (double)1.5F;
                     this.rotationYaw = (float)(MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
                  }
               }
            }

            if (this.ticksExisted % 10 == 0) {
               EntityLivingBase dome = this.findNearbyIceDome((double)20.0F);
               if (dome != null && dome.isEntityAlive()) {
                  this.setAttackTarget(dome);
                  if ((double)this.getDistance(dome) <= (double)4.0F) {
                     dome.hurtResistantTime = 0;
                     dome.attackEntityFrom(DamageSource.causeMobDamage(this), 200.0F);
                     this.swingArm(EnumHand.MAIN_HAND);
                  }
               }
            }

            String ownerStr = this.getOwnerUUID();
            if (ownerStr != null && !ownerStr.isEmpty() && this.world instanceof WorldServer && this.lifetimeTicks % 20 == 0) {
               try {
                  UUID ownerUuid = UUID.fromString(ownerStr);
                  Entity owner = ((WorldServer)this.world).getEntityFromUuid(ownerUuid);
                  if (owner == null || !owner.isEntityAlive()) {
                     this.despawnWithSmoke();
                     return;
                  }
               } catch (IllegalArgumentException var12) {
               }
            }

            ++this.tailSlamTimer;
            if (this.tailSlamTimer >= 80) {
               this.tailSlamTimer = 0;
               this.performTailSlam();
            }

            this.updateSegmentRotations();
            if (this.needsSync) {
               SnakeSegmentSyncMessage.sendToTracking(this);
               this.needsSync = false;
            }
         }

         this.updatePartPositions();
      }

      private void updateSegmentRotations() {
         double movedX = this.posX - this.lastPosX;
         double movedZ = this.posZ - this.lastPosZ;
         double movedDist = Math.sqrt(movedX * movedX + movedZ * movedZ);
         float currentYaw = this.rotationYaw;
         float yawDelta = MathHelper.wrapDegrees(currentYaw - this.lastYaw);
         if (movedDist > 0.01 || Math.abs(yawDelta) > 0.5F) {
            if (this.partRot.size() > 0) {
               for(int i = this.partRot.size() - 1; i > 0; --i) {
                  this.partRot.set(i, this.partRot.get(i - 1));
               }

               this.partRot.set(0, new Vec2f(yawDelta, 0.0F));
            }

            this.needsSync = true;
         }

         this.lastPosX = this.posX;
         this.lastPosZ = this.posZ;
         this.lastYaw = currentYaw;
      }

      private void updatePartPositions() {
         float scale = 0.75F;
         Vec3d vec = Vec3d.ZERO;
         float yaw = MathHelper.wrapDegrees((this.prevRenderYawOffset - this.renderYawOffset) * 0.5F + this.renderYawOffset) * ((float)Math.PI / 180F);
         float pitch = this.rotationPitch * ((float)Math.PI / 180F);
         Vec3d headOff = EntityOrochimaruSnake.getOffsetPoint((float)vec.x, (float)vec.y, (float)vec.z, pitch == 0.0F ? -0.2618F : pitch * 0.5F, -yaw, 5.0F * scale);
         headOff = EntityOrochimaruSnake.getOffsetPoint((float)headOff.x, (float)headOff.y, (float)headOff.z, pitch == 0.0F ? 0.2618F : pitch, -this.renderYawOffset * ((float)Math.PI / 180F), 3.0F * scale);
         Vec3d headPos = headOff.add(this.posX, this.posY, this.posZ);
         this.parts[0].setLocationAndAngles(headPos.x, headPos.y, headPos.z, this.renderYawOffset, this.rotationPitch);
         float runningYaw = this.renderYawOffset;
         vec = Vec3d.ZERO;

         for(int i = 1; i < this.parts.length; ++i) {
            this.parts[i].onUpdate();
            Vec2f pr = i - 1 < this.partRot.size() ? (Vec2f)this.partRot.get(i - 1) : Vec2f.ZERO;
            runningYaw = MathHelper.wrapDegrees(runningYaw - pr.x);
            float segPitch = pr.y * ((float)Math.PI / 180F);
            Vec3d segOff = EntityOrochimaruSnake.getOffsetPoint((float)vec.x, (float)vec.y, (float)vec.z, -segPitch, -runningYaw * ((float)Math.PI / 180F), -2.0F * scale);
            Vec3d segPos = segOff.add(this.posX, this.posY, this.posZ);
            this.parts[i].setLocationAndAngles(segPos.x, segPos.y, segPos.z, pr.x, pr.y);
            vec = EntityOrochimaruSnake.getOffsetPoint((float)vec.x, (float)vec.y, (float)vec.z, -segPitch, -runningYaw * ((float)Math.PI / 180F), -4.0F * scale);
         }

      }

      private void performTailSlam() {
         if (this.parts.length >= 22) {
            SnakeSegment tail = this.parts[21];

            for(EntityPlayer p : this.world.getEntitiesWithinAABB(EntityPlayer.class, tail.getEntityBoundingBox().grow((double)3.5F))) {
               if (p.isEntityAlive() && !p.isSpectator()) {
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.causeMobDamage(this), 14.0F);
                  p.hurtResistantTime = 0;
                  p.attackEntityFrom(DamageSource.MAGIC, 6.0F);
                  p.motionY = (double)0.5F;
                  if (p instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)p).velocityChanged = true;
                  }
               }
            }

            if (this.world instanceof WorldServer) {
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, tail.posX, tail.posY, tail.posZ, 30, (double)2.0F, (double)0.5F, (double)2.0F, 0.1, new int[]{Blocks.DIRT.getDefaultState().hashCode()});
               ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, tail.posX, tail.posY + (double)0.5F, tail.posZ, 3, (double)1.0F, (double)0.5F, (double)1.0F, (double)0.0F, new int[0]);
            }

            this.world.playSound((EntityPlayer)null, tail.posX, tail.posY, tail.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 0.8F, 0.5F);
         }
      }

      private void despawnWithSmoke() {
         if (!this.world.isRemote) {
            for(int i = 0; i < this.parts.length; ++i) {
               Particles.spawnParticle(this.world, Types.SMOKE, this.parts[i].posX, this.parts[i].posY + (double)(this.parts[i].height / 2.0F), this.parts[i].posZ, 15, (double)this.parts[i].width * (double)0.5F, (double)this.parts[i].height * 0.3, (double)this.parts[i].width * (double)0.5F, (double)0.0F, (double)0.0F, (double)0.0F, new int[]{681128652, 25, 12, 240});
            }
         }

         this.setDead();
      }

      private boolean isIceDome(EntityLivingBase target) {
         if (target == null) {
            return false;
         } else {
            String cn = target.getClass().getName().toLowerCase();
            return cn.contains("icedome") || cn.contains("shieldbase");
         }
      }

      private EntityLivingBase findNearbyIceDome(double range) {
         List<EntityLivingBase> nearby = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(range), (ex) -> ex != null && ex.isEntityAlive() && this.isIceDome(ex));
         EntityLivingBase nearest = null;
         double nearestDist = Double.MAX_VALUE;

         for(EntityLivingBase e : nearby) {
            double d = this.getDistanceSq(e);
            if (d < nearestDist) {
               nearestDist = d;
               nearest = e;
            }
         }

         return nearest;
      }

      public void setOwnerUUID(String uuid) {
         this.dataManager.set(OWNER_UUID, uuid);
      }

      public String getOwnerUUID() {
         return (String)this.dataManager.get(OWNER_UUID);
      }

      public List<Vec2f> getPartRot() {
         return this.partRot;
      }

      public SnakeSegment[] getSnakeParts() {
         return this.parts;
      }

      public EnumCreatureAttribute getCreatureAttribute() {
         return EnumCreatureAttribute.UNDEFINED;
      }

      protected boolean canDespawn() {
         return false;
      }

      protected Item getDropItem() {
         return null;
      }

      public boolean canBePushed() {
         return false;
      }

      public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
      }

      public SoundEvent getAmbientSound() {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:snake_hiss"));
      }

      public SoundEvent getHurtSound(DamageSource ds) {
         return SoundEvents.BLOCK_SAND_HIT;
      }

      public SoundEvent getDeathSound() {
         return SoundEvents.ENTITY_HOSTILE_DEATH;
      }

      protected float getSoundVolume() {
         return 2.0F;
      }

      protected boolean isValidLightLevel() {
         return true;
      }

      public boolean getCanSpawnHere() {
         return false;
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setString("OwnerUUID", this.getOwnerUUID());
         compound.setInteger("LifetimeTicks", this.lifetimeTicks);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         if (compound.hasKey("OwnerUUID")) {
            this.setOwnerUUID(compound.getString("OwnerUUID"));
         }

         this.lifetimeTicks = compound.getInteger("LifetimeTicks");
      }

      static {
         OWNER_UUID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
      }
   }

   public static class SnakeSegment extends MultiPartEntityPart {
      private final EntityCustom head;

      public SnakeSegment(EntityCustom parent, String partName, float width, float height) {
         super(parent, partName, width, height);
         this.head = parent;
      }
   }

   public static class SnakeSegmentSyncMessage implements IMessage {
      int entityId;
      float[] rotX = new float[21];
      float[] rotY = new float[21];

      public SnakeSegmentSyncMessage() {
      }

      public SnakeSegmentSyncMessage(EntityCustom entity) {
         this.entityId = entity.getEntityId();
         List<Vec2f> partRot = entity.getPartRot();

         for(int i = 0; i < 21 && i < partRot.size(); ++i) {
            Vec2f vec = (Vec2f)partRot.get(i);
            this.rotX[i] = vec.x;
            this.rotY[i] = vec.y;
         }

      }

      public static void sendToTracking(EntityCustom entity) {
         SnakeSegmentSyncMessage msg = new SnakeSegmentSyncMessage(entity);
         luckAddonAddon.PACKET_HANDLER.sendToAllAround(msg, new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, (double)128.0F));
      }

      public void toBytes(ByteBuf buf) {
         buf.writeInt(this.entityId);

         for(int i = 0; i < 21; ++i) {
            buf.writeFloat(this.rotX[i]);
            buf.writeFloat(this.rotY[i]);
         }

      }

      public void fromBytes(ByteBuf buf) {
         this.entityId = buf.readInt();

         for(int i = 0; i < 21; ++i) {
            this.rotX[i] = buf.readFloat();
            this.rotY[i] = buf.readFloat();
         }

      }

      public static class Handler implements IMessageHandler<SnakeSegmentSyncMessage, IMessage> {
         @SideOnly(Side.CLIENT)
         public IMessage onMessage(SnakeSegmentSyncMessage message, MessageContext context) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
               if (mc.world != null) {
                  Entity entity = mc.world.getEntityByID(message.entityId);
                  if (entity instanceof EntityCustom) {
                     EntityCustom snake = (EntityCustom)entity;
                     List<Vec2f> partRot = snake.getPartRot();

                     for(int i = 0; i < partRot.size() && i < 21; ++i) {
                        partRot.set(i, new Vec2f(message.rotX[i], message.rotY[i]));
                     }
                  }

               }
            });
            return null;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public static class RenderOrochimaruSnake extends RenderLiving<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod", "textures/snake_purple.png");
      private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("inftsukaddon", "textures/snake_purple_fallback.png");

      public RenderOrochimaruSnake(RenderManager renderManager) {
         super(renderManager, new ModelOrochimaruSnake(), 0.5F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         GlStateManager.scale(12.0F, 12.0F, 12.0F);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class ModelOrochimaruSnake extends ModelBase {
      private final ModelRenderer headNeck;
      private final ModelRenderer head;
      private final ModelRenderer bone2;
      private final ModelRenderer bone3;
      private final ModelRenderer bone4;
      private final ModelRenderer bone5;
      private final ModelRenderer bone6;
      private final ModelRenderer bone7;
      private final ModelRenderer bone8;
      private final ModelRenderer bone9;
      private final ModelRenderer bone11;
      private final ModelRenderer bone19;
      private final ModelRenderer bone20;
      private final ModelRenderer jaw;
      private final ModelRenderer bone21;
      private final ModelRenderer bone22;
      private final ModelRenderer bone23;
      private final ModelRenderer horns;
      private final ModelRenderer bone24;
      private final ModelRenderer bone25;
      private final ModelRenderer bone26;
      private final ModelRenderer bone37;
      private final ModelRenderer[] segment = new ModelRenderer[21];
      private float partialTicks;

      public ModelOrochimaruSnake() {
         this.textureWidth = 32;
         this.textureHeight = 32;
         (this.headNeck = new ModelRenderer(this)).setRotationPoint(0.0F, 22.0F, 0.0F);
         this.headNeck.cubeList.add(new ModelBox(this.headNeck, 0, 0, -2.5F, -2.0F, -5.0F, 5, 4, 6, 0.0F, false));
         (this.head = new ModelRenderer(this)).setRotationPoint(0.0F, 0.0F, -5.0F);
         this.headNeck.addChild(this.head);
         this.head.cubeList.add(new ModelBox(this.head, 16, 0, -2.5F, -2.0F, 0.0F, 5, 4, 1, 0.1F, false));
         (this.bone2 = new ModelRenderer(this)).setRotationPoint(1.4F, -0.7F, -5.35F);
         this.head.addChild(this.bone2);
         this.setRotationAngle(this.bone2, 0.7854F, 0.0F, 0.6109F);
         this.bone2.cubeList.add(new ModelBox(this.bone2, 17, 22, -0.5F, -0.5F, 0.0F, 1, 1, 3, 0.0F, false));
         (this.bone3 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.5F, 3.0F);
         this.bone2.addChild(this.bone3);
         this.setRotationAngle(this.bone3, -0.9599F, 0.0F, 0.0F);
         this.bone3.cubeList.add(new ModelBox(this.bone3, 22, 5, -0.5F, 0.0F, 0.0F, 1, 1, 3, 0.0F, false));
         (this.bone4 = new ModelRenderer(this)).setRotationPoint(-1.4F, -0.7F, -5.35F);
         this.head.addChild(this.bone4);
         this.setRotationAngle(this.bone4, 0.7854F, 0.0F, -0.6109F);
         this.bone4.cubeList.add(new ModelBox(this.bone4, 17, 22, -0.5F, -0.5F, 0.0F, 1, 1, 3, 0.0F, true));
         (this.bone5 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.5F, 3.0F);
         this.bone4.addChild(this.bone5);
         this.setRotationAngle(this.bone5, -0.9599F, 0.0F, 0.0F);
         this.bone5.cubeList.add(new ModelBox(this.bone5, 22, 5, -0.5F, 0.0F, 0.0F, 1, 1, 3, 0.0F, true));
         (this.bone6 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.0F, 0.0F);
         this.head.addChild(this.bone6);
         this.setRotationAngle(this.bone6, 0.0436F, 0.0873F, 0.0F);
         this.bone6.cubeList.add(new ModelBox(this.bone6, 13, 10, -0.0076F, -1.5F, -3.8257F, 3, 3, 4, 0.0F, false));
         (this.bone7 = new ModelRenderer(this)).setRotationPoint(0.0F, -1.0F, 0.0F);
         this.head.addChild(this.bone7);
         this.setRotationAngle(this.bone7, 0.0436F, -0.0873F, 0.0F);
         this.bone7.cubeList.add(new ModelBox(this.bone7, 13, 10, -2.9924F, -1.5F, -3.8257F, 3, 3, 4, 0.0F, true));
         (this.bone8 = new ModelRenderer(this)).setRotationPoint(-0.15F, -1.1F, -2.5F);
         this.head.addChild(this.bone8);
         this.setRotationAngle(this.bone8, 0.5236F, 0.2618F, 0.0F);
         this.bone8.cubeList.add(new ModelBox(this.bone8, 17, 17, -0.05F, -1.5F, -3.0757F, 3, 2, 3, 0.0F, false));
         (this.bone9 = new ModelRenderer(this)).setRotationPoint(0.15F, -1.1F, -2.5F);
         this.head.addChild(this.bone9);
         this.setRotationAngle(this.bone9, 0.5236F, -0.2618F, 0.0F);
         this.bone9.cubeList.add(new ModelBox(this.bone9, 17, 17, -2.95F, -1.5F, -3.0757F, 3, 2, 3, 0.0F, true));
         (this.bone11 = new ModelRenderer(this)).setRotationPoint(2.6F, 0.1F, -3.95F);
         this.head.addChild(this.bone11);
         this.setRotationAngle(this.bone11, 0.0F, 0.2618F, 0.0F);
         this.bone11.cubeList.add(new ModelBox(this.bone11, 10, 19, -2.0F, -1.0F, -2.75F, 2, 1, 3, 0.0F, false));
         this.bone11.cubeList.add(new ModelBox(this.bone11, 0, 19, -2.0F, -0.4F, -2.75F, 2, 1, 3, 0.0F, false));
         (this.bone19 = new ModelRenderer(this)).setRotationPoint(-2.65F, 0.1F, -3.95F);
         this.head.addChild(this.bone19);
         this.setRotationAngle(this.bone19, 0.0F, -0.2618F, 0.0F);
         this.bone19.cubeList.add(new ModelBox(this.bone19, 10, 19, 0.05F, -1.0F, -2.75F, 2, 1, 3, 0.0F, true));
         this.bone19.cubeList.add(new ModelBox(this.bone19, 0, 19, 0.05F, -0.4F, -2.75F, 2, 1, 3, 0.0F, true));
         (this.bone20 = new ModelRenderer(this)).setRotationPoint(1.6F, 1.8F, -5.95F);
         this.head.addChild(this.bone20);
         this.bone20.cubeList.add(new ModelBox(this.bone20, 0, 1, -0.2F, -1.0F, 0.0F, 0, 1, 1, 0.1F, false));
         this.bone20.cubeList.add(new ModelBox(this.bone20, 0, 1, -3.0F, -1.0F, 0.0F, 0, 1, 1, 0.1F, true));
         (this.jaw = new ModelRenderer(this)).setRotationPoint(0.0F, 0.5F, 0.0F);
         this.head.addChild(this.jaw);
         this.setRotationAngle(this.jaw, 0.0F, 0.0F, 0.0F);
         (this.bone21 = new ModelRenderer(this)).setRotationPoint(3.0F, 0.9F, 0.0F);
         this.jaw.addChild(this.bone21);
         this.setRotationAngle(this.bone21, 0.0F, 0.2182F, 0.0F);
         this.bone21.cubeList.add(new ModelBox(this.bone21, 0, 10, -3.0F, -1.0F, -6.7F, 3, 2, 7, -0.1F, false));
         (this.bone22 = new ModelRenderer(this)).setRotationPoint(-3.0F, 0.9F, 0.0F);
         this.jaw.addChild(this.bone22);
         this.setRotationAngle(this.bone22, 0.0F, -0.2182F, 0.0F);
         this.bone22.cubeList.add(new ModelBox(this.bone22, 0, 10, 0.0F, -1.0F, -6.7F, 3, 2, 7, -0.1F, true));
         (this.bone23 = new ModelRenderer(this)).setRotationPoint(0.0F, -0.2F, -5.5F);
         this.jaw.addChild(this.bone23);
         this.setRotationAngle(this.bone23, 3.1416F, 3.1416F, 0.0F);
         this.bone23.cubeList.add(new ModelBox(this.bone23, 0, 1, 1.2F, -0.5F, -0.5F, 0, 1, 1, 0.1F, false));
         this.bone23.cubeList.add(new ModelBox(this.bone23, 0, 1, -1.2F, -0.5F, -0.5F, 0, 1, 1, 0.1F, true));
         (this.horns = new ModelRenderer(this)).setRotationPoint(0.0F, 0.6F, 0.0F);
         this.head.addChild(this.horns);
         (this.bone24 = new ModelRenderer(this)).setRotationPoint(-2.3F, -2.5F, -1.6F);
         this.horns.addChild(this.bone24);
         this.setRotationAngle(this.bone24, 0.2618F, -0.5236F, 0.0F);
         this.bone24.cubeList.add(new ModelBox(this.bone24, 28, 0, -0.5F, -0.5F, 0.0F, 1, 1, 1, 0.15F, false));
         this.bone24.cubeList.add(new ModelBox(this.bone24, 28, 0, -0.5F, -0.5F, 1.0F, 1, 1, 1, 0.1F, false));
         this.bone24.cubeList.add(new ModelBox(this.bone24, 28, 0, -0.5F, -0.5F, 2.0F, 1, 1, 1, 0.0F, false));
         this.bone24.cubeList.add(new ModelBox(this.bone24, 28, 0, -0.5F, -0.5F, 2.9F, 1, 1, 1, -0.1F, false));
         this.bone24.cubeList.add(new ModelBox(this.bone24, 28, 0, -0.5F, -0.5F, 3.6F, 1, 1, 1, -0.2F, false));
         this.bone24.cubeList.add(new ModelBox(this.bone24, 28, 0, -0.5F, -0.5F, 4.1F, 1, 1, 1, -0.3F, false));
         (this.bone25 = new ModelRenderer(this)).setRotationPoint(-1.2F, -2.5F, -1.2F);
         this.horns.addChild(this.bone25);
         this.setRotationAngle(this.bone25, 0.5236F, -0.3491F, 0.0F);
         this.bone25.cubeList.add(new ModelBox(this.bone25, 28, 0, -0.5F, -0.5F, 0.0F, 1, 1, 1, 0.1F, false));
         this.bone25.cubeList.add(new ModelBox(this.bone25, 28, 0, -0.5F, -0.5F, 0.9F, 1, 1, 1, -0.05F, false));
         this.bone25.cubeList.add(new ModelBox(this.bone25, 28, 0, -0.5F, -0.5F, 1.6F, 1, 1, 1, -0.2F, false));
         this.bone25.cubeList.add(new ModelBox(this.bone25, 28, 0, -0.5F, -0.5F, 2.1F, 1, 1, 1, -0.3F, false));
         (this.bone26 = new ModelRenderer(this)).setRotationPoint(1.2F, -2.5F, -1.2F);
         this.horns.addChild(this.bone26);
         this.setRotationAngle(this.bone26, 0.5236F, 0.3491F, 0.0F);
         this.bone26.cubeList.add(new ModelBox(this.bone26, 28, 0, -0.5F, -0.5F, 0.0F, 1, 1, 1, 0.1F, true));
         this.bone26.cubeList.add(new ModelBox(this.bone26, 28, 0, -0.5F, -0.5F, 0.9F, 1, 1, 1, -0.05F, true));
         this.bone26.cubeList.add(new ModelBox(this.bone26, 28, 0, -0.5F, -0.5F, 1.6F, 1, 1, 1, -0.2F, true));
         this.bone26.cubeList.add(new ModelBox(this.bone26, 28, 0, -0.5F, -0.5F, 2.1F, 1, 1, 1, -0.3F, true));
         (this.bone37 = new ModelRenderer(this)).setRotationPoint(2.3F, -2.5F, -1.6F);
         this.horns.addChild(this.bone37);
         this.setRotationAngle(this.bone37, 0.2618F, 0.5236F, 0.0F);
         this.bone37.cubeList.add(new ModelBox(this.bone37, 28, 0, -0.5F, -0.5F, 0.0F, 1, 1, 1, 0.15F, true));
         this.bone37.cubeList.add(new ModelBox(this.bone37, 28, 0, -0.5F, -0.5F, 1.0F, 1, 1, 1, 0.1F, true));
         this.bone37.cubeList.add(new ModelBox(this.bone37, 28, 0, -0.5F, -0.5F, 2.0F, 1, 1, 1, 0.0F, true));
         this.bone37.cubeList.add(new ModelBox(this.bone37, 28, 0, -0.5F, -0.5F, 2.9F, 1, 1, 1, -0.1F, true));
         this.bone37.cubeList.add(new ModelBox(this.bone37, 28, 0, -0.5F, -0.5F, 3.6F, 1, 1, 1, -0.2F, true));
         this.bone37.cubeList.add(new ModelBox(this.bone37, 28, 0, -0.5F, -0.5F, 4.1F, 1, 1, 1, -0.3F, true));

         for(int i = 0; i < 21; ++i) {
            this.segment[i] = new ModelRenderer(this);
            float taper = i >= 12 ? (float)(11 - i) * 0.2F : 0.0F;
            this.segment[i].cubeList.add(new ModelBox(this.segment[i], 0, 0, -2.5F, -2.0F, -1.0F, 5, 4, 6, taper, false));
         }

      }

      public void setLivingAnimations(EntityLivingBase entityIn, float limbSwing, float limbSwingAmount, float partialTicksIn) {
         this.partialTicks = partialTicksIn;
      }

      public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
         EntityCustom entity = (EntityCustom)entityIn;
         float mscale = 12.0F;
         this.headNeck.rotateAngleY = netHeadYaw * 0.5F * ((float)Math.PI / 180F);
         this.head.rotateAngleY = netHeadYaw * 0.5F * ((float)Math.PI / 180F);
         if (headPitch == 0.0F) {
            this.headNeck.rotateAngleX = -0.2618F;
            this.head.rotateAngleX = 0.2618F;
         } else {
            this.headNeck.rotateAngleX = headPitch * 0.5F * ((float)Math.PI / 180F);
            this.head.rotateAngleX = headPitch * 0.5F * ((float)Math.PI / 180F);
         }

         ModelRenderer var10000 = this.headNeck;
         var10000.rotateAngleX -= 0.2618F;
         var10000 = this.head;
         var10000.rotateAngleX += 0.1745F;
         this.jaw.rotateAngleX = 0.5236F;
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 1.5F - 1.5F * mscale * 0.0625F, 0.0F);
         GlStateManager.scale(mscale * 0.0625F, mscale * 0.0625F, mscale * 0.0625F);
         this.headNeck.render(scale);
         Vec3d vec = new Vec3d((double)this.headNeck.rotationPointX, (double)this.headNeck.rotationPointY, (double)this.headNeck.rotationPointZ);
         float runningYaw = 0.0F;
         List<Vec2f> partRot = entity.getPartRot();

         for(int i = 0; i < this.segment.length && i < partRot.size(); ++i) {
            this.segment[i].setRotationPoint((float)vec.x, (float)vec.y, (float)vec.z);
            Vec2f rot = (Vec2f)partRot.get(i);
            runningYaw = MathHelper.wrapDegrees(runningYaw - rot.x);
            this.setRotationAngle(this.segment[i], -rot.y * ((float)Math.PI / 180F), runningYaw * ((float)Math.PI / 180F), 0.0F);
            this.segment[i].render(scale);
            vec = this.getNextSegmentRotationPoint(this.segment[i], 4.0F);
         }

         GlStateManager.popMatrix();
      }

      private Vec3d getNextSegmentRotationPoint(ModelRenderer modelRenderer, float offset) {
         return EntityOrochimaruSnake.getOffsetPoint(modelRenderer.rotationPointX, modelRenderer.rotationPointY, modelRenderer.rotationPointZ, modelRenderer.rotateAngleX, modelRenderer.rotateAngleY, offset);
      }

      public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
         modelRenderer.rotateAngleX = x;
         modelRenderer.rotateAngleY = y;
         modelRenderer.rotateAngleZ = z;
      }
   }
}
