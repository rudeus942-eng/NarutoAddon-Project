
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityHashiramaClone extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 8;

   public EntityHashiramaClone(ElementsInfTsukAddon instance) {
      super(instance, 26);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "hashiramaclone"), 8).name("hashiramaclone").tracker(64, 1, true).egg(-1, -1).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, CloneRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class CloneRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation HASHIRAMA_TEXTURE = new ResourceLocation("inftsukaddon:textures/hashiramaix.png");

      public CloneRenderer(RenderManager renderManager) {
         super(renderManager, new ModelPlayer(0.0F, false), 0.5F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         return HASHIRAMA_TEXTURE;
      }

      protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
         super.preRenderCallback(entity, partialTickTime);
         float scale = 1.2F;
         GlStateManager.scale(scale, scale, scale);
      }
   }

   public static class EntityCustom extends EntityMob {
      private EntityLivingBase owner;
      private int lifetimeTicks;
      private int maxLifetime;
      private float cloneDamage;

      public EntityCustom(World world) {
         super(world);
         this.lifetimeTicks = 0;
         this.maxLifetime = 600;
         this.cloneDamage = 10.0F;
         this.setSize(0.6F, 1.8F);
         this.experienceValue = 0;
         this.isImmuneToFire = false;
         this.setNoAI(false);
         this.enablePersistence();
      }

      public EntityCustom(World world, EntityLivingBase owner) {
         this(world);
         this.owner = owner;
      }

      protected void initEntityAI() {
         super.initEntityAI();
         this.tasks.addTask(0, new EntityAISwimming(this));
         this.tasks.addTask(1, new EntityAIAttackMelee(this, (double)1.0F, false));
         this.tasks.addTask(2, new EntityAIWander(this, 0.8));
         this.tasks.addTask(3, new EntityAILookIdle(this));
         this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F));
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
         return SoundEvents.ENTITY_PLAYER_HURT;
      }

      public SoundEvent getDeathSound() {
         return SoundEvents.ENTITY_PLAYER_DEATH;
      }

      protected float getSoundVolume() {
         return 1.0F;
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)5.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.28);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)500.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)10.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)32.0F);
         }

      }

      public void setOwner(EntityLivingBase owner) {
         this.owner = owner;
      }

      public EntityLivingBase getOwner() {
         return this.owner;
      }

      public void setMaxLifetime(int ticks) {
         this.maxLifetime = ticks;
      }

      public void setCloneHealth(float health) {
         this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)health);
         this.setHealth(health);
      }

      public void setCloneDamage(float damage) {
         this.cloneDamage = damage;
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)damage);
      }

      public void onUpdate() {
         super.onUpdate();
         if (!this.world.isRemote) {
            ++this.lifetimeTicks;
            if (this.maxLifetime > 0 && this.lifetimeTicks >= this.maxLifetime) {
               this.spawnDeathParticles();
               this.setDead();
            }
         }

         if (this.world.isRemote && this.ticksExisted % 10 == 0) {
            for(int i = 0; i < 2; ++i) {
               double offsetX = (this.rand.nextDouble() - (double)0.5F) * (double)this.width;
               double offsetY = this.rand.nextDouble() * (double)this.height;
               double offsetZ = (this.rand.nextDouble() - (double)0.5F) * (double)this.width;
               this.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX + offsetX, this.posY + offsetY, this.posZ + offsetZ, (double)0.0F, 0.05, (double)0.0F, new int[0]);
            }
         }

      }

      public void onDeath(DamageSource cause) {
         super.onDeath(cause);
         this.spawnDeathParticles();
      }

      private void spawnDeathParticles() {
         if (this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)this.world;
            ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX, this.posY + (double)(this.height / 2.0F), this.posZ, 30, (double)(this.width / 2.0F), (double)(this.height / 2.0F), (double)(this.width / 2.0F), 0.1, new int[0]);
            EnumParticleTypes var10001 = EnumParticleTypes.BLOCK_CRACK;
            double var10002 = this.posX;
            double var10003 = this.posY + (double)(this.height / 2.0F);
            double var10004 = this.posZ;
            double var10006 = (double)(this.width / 2.0F);
            double var10007 = (double)(this.height / 2.0F);
            double var10008 = (double)(this.width / 2.0F);
            int[] var10010 = new int[1];
            Block var10013 = Blocks.LOG;
            var10010[0] = Block.getStateId(Blocks.LOG.getDefaultState());
            ws.spawnParticle(var10001, var10002, var10003, var10004, 20, var10006, var10007, var10008, 0.05, var10010);
         }

      }

      public boolean attackEntityAsMob(Entity target) {
         float attackDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
         DamageSource source = DamageSource.MAGIC;
         if (target instanceof EntityLivingBase) {
            ((EntityLivingBase)target).hurtResistantTime = 0;
         }

         boolean success = target.attackEntityFrom(source, attackDamage);
         if (success && target instanceof EntityLivingBase) {
            EntityLivingBase livingTarget = (EntityLivingBase)target;
            float knockbackStrength = 0.4F;
            livingTarget.knockBack(this, knockbackStrength, this.posX - livingTarget.posX, this.posZ - livingTarget.posZ);
            this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
            if (this.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)this.world;
               ws.spawnParticle(EnumParticleTypes.CRIT, livingTarget.posX, livingTarget.posY + (double)(livingTarget.height / 2.0F), livingTarget.posZ, 5, 0.3, 0.3, 0.3, 0.1, new int[0]);
            }
         }

         return success;
      }

      protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
      }

      public int getExperiencePoints(EntityPlayer player) {
         return 0;
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

      public boolean hasCustomName() {
         return true;
      }

      public String getCustomNameTag() {
         return "§2Wood Clone";
      }
   }
}
