
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
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
public class EntityQuestDog extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 217;

   public EntityQuestDog(ElementsInfTsukAddon instance) {
      super(instance, 60);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "questdog"), 217).name("questdog").tracker(64, 3, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, QuestDogRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class QuestDogRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation WOLF_TAME_TEXTURE = new ResourceLocation("minecraft", "textures/entity/wolf/wolf_tame.png");

      public QuestDogRenderer(RenderManager renderManager) {
         super(renderManager, new ModelWolf(), 0.3F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return WOLF_TAME_TEXTURE;
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         float scale = entity.getRenderScale();
         GlStateManager.scale(scale, scale, scale);
      }
   }

   public static class EntityCustom extends EntityCreature implements INpcConfigurable {
      private static final DataParameter<String> NPC_CONFIG_ID;
      private boolean isPassive = false;
      private float renderScale = 0.2F;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.3F, 0.2F);
         this.experienceValue = 0;
         this.isImmuneToFire = false;
         this.setNoAI(true);
         this.enablePersistence();
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(NPC_CONFIG_ID, "");
      }

      protected void initEntityAI() {
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)0.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)0.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)20.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)0.0F);
         }

         if (this.getAttributeMap().getAttributeInstanceByName("generic.attackDamage") == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
         }

         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)3.0F);
         this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)48.0F);
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
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.wolf.ambient"));
      }

      public SoundEvent getHurtSound(DamageSource ds) {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.wolf.hurt"));
      }

      public SoundEvent getDeathSound() {
         return (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.wolf.death"));
      }

      protected float getSoundVolume() {
         return 1.0F;
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         if (this.isPassive) {
            return false;
         } else {
            Entity trueSource = source.getTrueSource();
            if (trueSource instanceof EntityCustom) {
               return false;
            } else {
               return trueSource instanceof QuestNpcBase ? false : super.attackEntityFrom(source, amount);
            }
         }
      }

      public float getRenderScale() {
         return this.renderScale;
      }

      public void applyNpcConfig(NpcConfig config) {
         this.dataManager.set(NPC_CONFIG_ID, config.getConfigId());
         if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(config.getMaxHealth());
            this.setHealth((float)config.getMaxHealth());
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(config.getMovementSpeed());
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(config.getArmor());
         }

         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(config.getAttackDamage());
         this.isPassive = config.isPassive();
         this.renderScale = config.getRenderScale();
         this.setNoAI(false);
         this.tasks.taskEntries.clear();
         this.targetTasks.taskEntries.clear();
         this.tasks.addTask(0, new EntityAISwimming(this));
         if (!config.isPassive()) {
            this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.2, false));
            this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            this.tasks.addTask(3, new EntityAILookIdle(this));
            this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
            this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
         } else {
            this.tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
            this.tasks.addTask(2, new EntityAILookIdle(this));
         }

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

      public String getNpcConfigId() {
         return (String)this.dataManager.get(NPC_CONFIG_ID);
      }

      public void onLivingUpdate() {
         super.onLivingUpdate();
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
         String configId = this.getNpcConfigId();
         if (configId != null && !configId.isEmpty()) {
            compound.setString("npcConfigId", configId);
         }

         compound.setBoolean("isPassive", this.isPassive);
         compound.setFloat("renderScale", this.renderScale);
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.isPassive = compound.getBoolean("isPassive");
         if (compound.hasKey("renderScale")) {
            this.renderScale = compound.getFloat("renderScale");
         }

         if (compound.hasKey("npcConfigId")) {
            String configId = compound.getString("npcConfigId");
            if (!configId.isEmpty()) {
               this.dataManager.set(NPC_CONFIG_ID, configId);
               NpcConfig config = NpcConfigRegistry.get(configId);
               if (config != null) {
                  this.applyNpcConfig(config);
               }
            }
         }

      }

      static {
         NPC_CONFIG_ID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
      }
   }
}
