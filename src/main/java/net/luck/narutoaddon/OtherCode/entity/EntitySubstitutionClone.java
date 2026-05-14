
package net.luck.narutoaddon.OtherCode.entity;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.SoundSubstitution;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class EntitySubstitutionClone extends ElementsInfTsukAddon.ModElement {
   public static final int ENTITYID = 100;

   public EntitySubstitutionClone(ElementsInfTsukAddon instance) {
      super(instance, 100);
   }

   public void initElements() {
      this.elements.entities.add((Supplier)() -> EntityEntryBuilder.create().entity(EntityCustom.class).id(new ResourceLocation("inftsukaddon", "substitution_clone"), 100).name("substitution_clone").tracker(64, 3, true).build());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, CloneRenderer::new);
   }

   @SideOnly(Side.CLIENT)
   public static class CloneRenderer extends RenderLiving<EntityCustom> {
      private static final ResourceLocation STEVE_TEXTURE = new ResourceLocation("textures/entity/steve.png");

      public CloneRenderer(RenderManager renderManager) {
         super(renderManager, new ModelPlayer(0.0F, false), 0.5F);
      }

      protected ResourceLocation getEntityTexture(EntityCustom entity) {
         String ownerName = entity.getOwnerName();
         if (ownerName != null && !ownerName.isEmpty()) {
            try {
               ResourceLocation skinLocation = this.getSkinForPlayer(ownerName);
               if (skinLocation != null) {
                  return skinLocation;
               }
            } catch (Exception var4) {
            }
         }

         return STEVE_TEXTURE;
      }

      private ResourceLocation getSkinForPlayer(String playerName) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.world != null) {
            EntityPlayer player = mc.world.getPlayerEntityByName(playerName);
            if (player != null && player instanceof AbstractClientPlayer) {
               AbstractClientPlayer clientPlayer = (AbstractClientPlayer)player;
               return clientPlayer.getLocationSkin();
            }
         }

         return STEVE_TEXTURE;
      }
   }

   public static class EntityCustom extends EntityMob {
      private static final DataParameter<String> OWNER_UUID;
      private static final DataParameter<String> OWNER_NAME;
      private long spawnTime;
      private boolean isDying = false;
      private int runTimer = 0;

      public EntityCustom(World world) {
         super(world);
         this.setSize(0.6F, 1.8F);
         this.experienceValue = 0;
         this.isImmuneToFire = false;
         this.setNoAI(false);
         this.enablePersistence();
         this.spawnTime = world.getTotalWorldTime();
      }

      protected void entityInit() {
         super.entityInit();
         this.dataManager.register(OWNER_UUID, "");
         this.dataManager.register(OWNER_NAME, "");
      }

      protected void initEntityAI() {
         this.tasks.addTask(0, new EntityAIPanic(this, (double)1.5F));
         this.tasks.addTask(1, new EntityAIWanderAvoidWater(this, 1.2));
         this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
         this.tasks.addTask(3, new EntityAILookIdle(this));
      }

      public void onUpdate() {
         super.onUpdate();
         if (!this.isDying) {
            if (!this.world.isRemote) {
               ++this.runTimer;
               if (this.runTimer % 20 == 0 || this.getNavigator().noPath()) {
                  double angle = this.rand.nextDouble() * Math.PI * (double)2.0F;
                  double distance = (double)5.0F + this.rand.nextDouble() * (double)5.0F;
                  double targetX = this.posX + Math.cos(angle) * distance;
                  double targetZ = this.posZ + Math.sin(angle) * distance;
                  this.getNavigator().tryMoveToXYZ(targetX, this.posY, targetZ, 1.2);
               }
            }

            if (!this.world.isRemote && this.world.getTotalWorldTime() - this.spawnTime > 200L) {
               if (this.world instanceof WorldServer) {
                  WorldServer worldServer = (WorldServer)this.world;
                  worldServer.spawnParticle(EnumParticleTypes.CLOUD, false, this.posX, this.posY + (double)1.0F, this.posZ, 10, (double)0.5F, (double)0.5F, (double)0.5F, 0.05, new int[0]);
                  worldServer.spawnParticle(EnumParticleTypes.SMOKE_LARGE, false, this.posX, this.posY + (double)1.0F, this.posZ, 10, (double)0.5F, (double)0.5F, (double)0.5F, 0.05, new int[0]);
               }

               this.setDead();
            }
         }
      }

      protected void applyEntityAttributes() {
         super.applyEntityAttributes();
         if (this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)50.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)0.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.ARMOR) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)0.0F);
         }

         if (this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) != null) {
            this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue((double)0.0F);
         }

      }

      public void setOwnerUUID(String uuid) {
         this.dataManager.set(OWNER_UUID, uuid);
      }

      public String getOwnerUUID() {
         return (String)this.dataManager.get(OWNER_UUID);
      }

      public void setOwnerName(String name) {
         this.dataManager.set(OWNER_NAME, name);
         this.setCustomNameTag(name);
      }

      public String getOwnerName() {
         return (String)this.dataManager.get(OWNER_NAME);
      }

      public void copyTeamFromOwner(EntityPlayer owner) {
         if (owner != null && !this.world.isRemote) {
            Team ownerTeam = owner.getTeam();
            if (ownerTeam != null) {
               this.world.getScoreboard().addPlayerToTeam(this.getCachedUniqueIdString(), ownerTeam.getName());
            }

         }
      }

      public Team getTeam() {
         return this.world.getScoreboard().getPlayersTeam(this.getCachedUniqueIdString());
      }

      public boolean isOnSameTeam(Entity other) {
         Team myTeam = this.getTeam();
         Team otherTeam = other.getTeam();
         if (myTeam != null && otherTeam != null) {
            return myTeam.isSameTeam(otherTeam);
         } else {
            if (other instanceof EntityPlayer) {
               String otherUUID = other.getCachedUniqueIdString();
               if (otherUUID != null && otherUUID.equals(this.getOwnerUUID())) {
                  return true;
               }
            }

            return super.isOnSameTeam(other);
         }
      }

      public boolean attackEntityFrom(DamageSource source, float amount) {
         Entity attacker = source.getTrueSource();
         return attacker != null && this.isOnSameTeam(attacker) ? false : super.attackEntityFrom(source, amount);
      }

      public void writeEntityToNBT(NBTTagCompound compound) {
         super.writeEntityToNBT(compound);
         compound.setString("OwnerUUID", this.getOwnerUUID());
         compound.setString("OwnerName", this.getOwnerName());
         compound.setLong("SpawnTime", this.spawnTime);
         compound.setLong("WorldTimeAtSave", this.world.getTotalWorldTime());
      }

      public void readEntityFromNBT(NBTTagCompound compound) {
         super.readEntityFromNBT(compound);
         this.setOwnerUUID(compound.getString("OwnerUUID"));
         this.setOwnerName(compound.getString("OwnerName"));
         long savedSpawnTime = compound.getLong("SpawnTime");
         long elapsed = compound.getLong("WorldTimeAtSave") - savedSpawnTime;
         if (compound.hasKey("WorldTimeAtSave") && elapsed >= 0L) {
            long remaining = 200L - elapsed;
            if (remaining <= 0L) {
               remaining = 1L;
            }

            this.spawnTime = this.world.getTotalWorldTime() - (200L - remaining);
         } else {
            this.spawnTime = this.world.getTotalWorldTime() - 180L;
         }

      }

      public void onDeath(DamageSource cause) {
         this.isDying = true;
         if (!this.world.isRemote) {
            SoundEvent substitutionSound = SoundSubstitution.getSubstitutionSound();
            if (substitutionSound != null) {
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, substitutionSound, SoundCategory.PLAYERS, 4.0F, 1.0F);
            }

            this.spawnDeathParticles();
            EntitySubLog.EntityCustom subLog = new EntitySubLog.EntityCustom(this.world);
            subLog.setPosition(this.posX, this.posY + (double)4.0F, this.posZ);
            this.world.spawnEntity(subLog);
            Entity attacker = cause.getTrueSource();
            if (attacker != null && attacker instanceof EntityLivingBase) {
               this.applyParalysis((EntityLivingBase)attacker);
            }
         }

         this.setDead();
      }

      private void spawnDeathParticles() {
         if (this.world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer)this.world;

            for(int i = 0; i < 30; ++i) {
               double offsetX = (this.rand.nextDouble() - (double)0.5F) * (double)2.0F;
               double offsetY = this.rand.nextDouble() * (double)2.0F;
               double offsetZ = (this.rand.nextDouble() - (double)0.5F) * (double)2.0F;
               worldServer.spawnParticle(EnumParticleTypes.SMOKE_LARGE, false, this.posX + offsetX, this.posY + offsetY, this.posZ + offsetZ, 5, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
               worldServer.spawnParticle(EnumParticleTypes.CLOUD, false, this.posX + offsetX, this.posY + offsetY, this.posZ + offsetZ, 5, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
            }

            worldServer.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, false, this.posX, this.posY + (double)1.0F, this.posZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
         }

      }

      private void applyParalysis(EntityLivingBase target) {
         target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 6, false, false));
         target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 255, false, false));
         target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 100, 255, false, false));
         target.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 100, 128, false, false));
         if (target instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)target;
            player.getEntityData().setLong("ParalysisEndTime", target.world.getTotalWorldTime() + 100L);
            player.getEntityData().setFloat("ParalysisYaw", player.rotationYaw);
            player.getEntityData().setFloat("ParalysisPitch", player.rotationPitch);
         }

      }

      protected boolean canDespawn() {
         return false;
      }

      protected Item getDropItem() {
         return null;
      }

      public boolean getCanSpawnHere() {
         return false;
      }

      protected boolean canDropLoot() {
         return false;
      }

      public SoundEvent getAmbientSound() {
         return null;
      }

      public SoundEvent getHurtSound(DamageSource ds) {
         return SoundEvents.ENTITY_PLAYER_HURT;
      }

      public SoundEvent getDeathSound() {
         return null;
      }

      static {
         OWNER_UUID = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
         OWNER_NAME = EntityDataManager.createKey(EntityCustom.class, DataSerializers.STRING);
      }
   }
}
