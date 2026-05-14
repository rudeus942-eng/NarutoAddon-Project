
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityWaterClone extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 210;

   public EntityWaterClone(ElementsInfTsukAddon instance) {
      super(instance, 36);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "water_clone"), 210).name("inftsuk_water_clone").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, WaterCloneRenderer::new);
   }

   public static class EntityCustom extends EntityCreature {
      private static final DataParameter<String> OWNER_UUID;
      private int lifetime = 0;
      private static final int MAX_LIFETIME = 300;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.6F, 1.8F);
         this.experienceValue = 0;
         this.enablePersistence();
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(OWNER_UUID, "");
      }

      protected void initEntityAI() {
         this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.2, true));
         this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
         this.tasks.addTask(6, new EntityAILookIdle(this));
         this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
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

      public void onUpdate() {
         super.onUpdate();
         ++this.lifetime;
         if (!this.world.isRemote && this.ticksExisted % 10 == 0) {
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

         if (!this.world.isRemote && this.lifetime > 300) {
            for(int i = 0; i < 20; ++i) {
               this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, this.posY + this.rand.nextDouble() * 1.8, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, (double)0.0F, 0.1, (double)0.0F, new int[0]);
            }

            this.setDead();
         }

      }

      public void onDeath(DamageSource cause) {
         super.onDeath(cause);
         if (!this.world.isRemote) {
            for(int i = 0; i < 20; ++i) {
               this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, this.posY + this.rand.nextDouble() * 1.8, this.posZ + (this.rand.nextDouble() - (double)0.5F) * (double)1.0F, (double)0.0F, 0.1, (double)0.0F, new int[0]);
            }
         }

      }

      public void setOwnerUUID(String uuid) {
         this.dataManager.set(OWNER_UUID, uuid);
      }

      public String getOwnerUUID() {
         return (String)this.dataManager.get(OWNER_UUID);
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

      public SoundEvent getAmbientSound() {
         return null;
      }

      public SoundEvent getHurtSound(DamageSource ds) {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.hurt"));
      }

      public SoundEvent getDeathSound() {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.generic.death"));
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)4500.0F);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33);
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)3.0F);
         this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.4);
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

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setInteger("cloneLifetime", this.lifetime);
         compound.setString("cloneOwnerUUID", this.getOwnerUUID());
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.lifetime = compound.getInteger("cloneLifetime");
         if (compound.hasKey("cloneOwnerUUID")) {
            this.setOwnerUUID(compound.getString("cloneOwnerUUID"));
         }

      }

      static {
         OWNER_UUID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class WaterCloneRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation TEXTURE = new ResourceLocation("inftsukaddon:textures/kisame1.png");

      public WaterCloneRenderer(RenderManager renderManager) {
         super(renderManager, new ModelPlayer(0.0F, false), 0.5F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return TEXTURE;
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         GlStateManager.color(0.7F, 0.8F, 1.0F, 0.8F);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      }
   }
}
