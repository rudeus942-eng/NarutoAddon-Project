
package net.luck.narutoaddon.OtherCode.endgame.bingo;

import net.luck.narutoaddon.OtherCode.endgame.network.EndgameNetworkHelper;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostDifficultyTier;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.quest.core.QuestModInit;
import net.luck.narutoaddon.OtherCode.quest.network.QuestCombatHealthMessage;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointData;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointSpawnLogic;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.*;

public class BingoInstance {
   private final UUID playerUUID;
   private final BingoBoard.BingoSlot slot;
   private final BingoTarget targetDef;
   private final OutpostDifficultyTier boardTier;
   private BlockPos spawnLocation;
   private double searchRadius = (double)200.0F;
   private boolean targetSpawned = false;
   private boolean decoySpawned = false;
   private final Set<UUID> spawnedEntityUUIDs = new HashSet();
   private UUID targetEntityUUID = null;
   private final long startTime;
   private boolean complete = false;
   private boolean failed = false;
   private static final int MAX_LOCATION_ATTEMPTS = 3;
   private int locationAttempts = 0;
   private boolean pendingLocationAdvance = false;
   private static final double SPAWN_TRIGGER_DISTANCE = (double)30.0F;
   private static final int SPAWN_DELAY_TICKS = 60;
   private int proximityEnteredTick = -1;
   private int tickCount = 0;
   private boolean targetConfirmedDead = false;
   private static final int STALENESS_TIMEOUT_TICKS = 6000;
   private int targetNullTicks = 0;

   public BingoInstance(UUID playerUUID, BingoBoard.BingoSlot slot, BingoTarget targetDef, OutpostDifficultyTier boardTier) {
      this.playerUUID = playerUUID;
      this.slot = slot;
      this.targetDef = targetDef;
      this.boardTier = boardTier;
      this.spawnLocation = new BlockPos(slot.getSpawnX(), 64, slot.getSpawnZ());
      this.startTime = System.currentTimeMillis();
   }

   public UUID getPlayerUUID() {
      return this.playerUUID;
   }

   public BingoBoard.BingoSlot getSlot() {
      return this.slot;
   }

   public BingoTarget getTargetDef() {
      return this.targetDef;
   }

   public BlockPos getSpawnLocation() {
      return this.spawnLocation;
   }

   public double getSearchRadius() {
      return this.searchRadius;
   }

   public boolean isTargetSpawned() {
      return this.targetSpawned;
   }

   public UUID getTargetEntityUUID() {
      return this.targetEntityUUID;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public boolean isComplete() {
      return this.complete;
   }

   public void tick(World world, EntityPlayerMP player) {
      if (!this.complete && !this.failed) {
         ++this.tickCount;
         double dist = this.getDistanceToSpawn(player);
         if (dist < (double)200.0F) {
            this.searchRadius = (double)50.0F;
         }

         if (!this.targetSpawned && !this.decoySpawned) {
            if (dist < (double)30.0F) {
               if (this.proximityEnteredTick < 0) {
                  this.proximityEnteredTick = this.tickCount;
               }

               int ticksSinceEntry = this.tickCount - this.proximityEnteredTick;
               if (ticksSinceEntry >= 3) {
                  this.doSpawn(world, player);
               }
            } else {
               this.proximityEnteredTick = -1;
            }
         }

         if (this.targetSpawned || this.decoySpawned) {
            List<QuestCombatHealthMessage.EnemyHealthEntry> entries = new ArrayList();

            for(UUID entityUUID : this.spawnedEntityUUIDs) {
               Entity entity = this.findEntityByUUID(world, entityUUID);
               if (entity instanceof EntityLivingBase && !entity.isDead) {
                  EntityLivingBase living = (EntityLivingBase)entity;
                  boolean isTarget = entityUUID.equals(this.targetEntityUUID);
                  String displayName = isTarget ? this.targetDef.getDisplayName() : "Enemy";
                  int themeColor = isTarget ? -30669 : -7829368;
                  int accentColor = isTarget ? -7846912 : -12303292;
                  String configId = entity.getEntityData().getString("npcConfigId");
                  if (configId != null && !configId.isEmpty()) {
                     NpcConfig cfg = NpcConfigRegistry.get(configId);
                     if (cfg != null) {
                        displayName = cfg.getDisplayName();
                        if (cfg.hasHealthBarColors()) {
                           themeColor = isTarget ? cfg.getThemeColor() : cfg.getThemeColor();
                           accentColor = isTarget ? cfg.getAccentColor() : cfg.getAccentColor();
                        }
                     }
                  }

                  entries.add(new QuestCombatHealthMessage.EnemyHealthEntry(displayName, living.getEntityId(), living.getHealth(), living.getMaxHealth(), themeColor, accentColor));
               }
            }

            if (!entries.isEmpty()) {
               QuestModInit.NETWORK.sendTo(QuestCombatHealthMessage.update((QuestCombatHealthMessage.EnemyHealthEntry[])entries.toArray(new QuestCombatHealthMessage.EnemyHealthEntry[0])), player);
            }
         }

         if (this.decoySpawned && !this.targetSpawned && !this.pendingLocationAdvance) {
            boolean allDead = true;

            for(UUID entityUUID : this.spawnedEntityUUIDs) {
               Entity entity = this.findEntityByUUID(world, entityUUID);
               if (entity != null && !entity.isDead) {
                  allDead = false;
                  break;
               }
            }

            if (allDead && !this.spawnedEntityUUIDs.isEmpty()) {
               this.pendingLocationAdvance = true;
               this.advanceToNextLocation(world, player);
            }
         }

         if (this.targetSpawned && this.targetEntityUUID != null) {
            if (this.targetConfirmedDead) {
               this.complete = true;
               return;
            }

            Entity target = this.findEntityByUUID(world, this.targetEntityUUID);
            if (target == null) {
               this.targetNullTicks += 20;
               if (this.targetNullTicks >= 6000) {
                  this.failed = true;
                  player.sendMessage(new TextComponentString("§c§lHunt Failed: §rYour target escaped. The hunt has been cancelled."));
               }
            } else if (target.isDead) {
               this.complete = true;
            } else {
               this.targetNullTicks = 0;
            }
         }

      }
   }

   private void doSpawn(World world, EntityPlayerMP player) {
      BlockPos safePos = new BlockPos(this.spawnLocation.getX(), WaypointSpawnLogic.findGroundY(world, this.spawnLocation.getX(), this.spawnLocation.getZ()), this.spawnLocation.getZ());
      Random rand = world.rand;
      boolean forceTarget = this.locationAttempts >= 3;
      if (!forceTarget && !(rand.nextDouble() < this.targetDef.getSpawnChance())) {
         int decoyCount = 2 + rand.nextInt(2);
         this.spawnDecoys(world, player, safePos, decoyCount);
      } else {
         this.spawnTarget(world, player, safePos);
         int decoyCount = 1 + rand.nextInt(2);
         this.spawnDecoys(world, player, safePos, decoyCount);
      }

      this.decoySpawned = true;
      QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "bingo_hunt");
      if (rand.nextDouble() < this.targetDef.getAmbushChance()) {
         int ambushCount = 1 + rand.nextInt(2);
         this.spawnAmbush(world, player, ambushCount);
      }

   }

   private void advanceToNextLocation(World world, EntityPlayerMP player) {
      ++this.locationAttempts;
      this.spawnedEntityUUIDs.clear();
      this.decoySpawned = false;
      this.targetSpawned = false;
      this.proximityEnteredTick = -1;
      this.searchRadius = (double)200.0F;
      this.pendingLocationAdvance = false;
      Random rand = world.rand;
      int newX = this.targetDef.getRegionMinX() + rand.nextInt(Math.max(1, this.targetDef.getRegionMaxX() - this.targetDef.getRegionMinX()));
      int newZ = this.targetDef.getRegionMinZ() + rand.nextInt(Math.max(1, this.targetDef.getRegionMaxZ() - this.targetDef.getRegionMinZ()));
      this.spawnLocation = new BlockPos(newX, 64, newZ);
      int safeY = WaypointSpawnLogic.findGroundY(world, newX, newZ);
      BlockPos waypointPos = new BlockPos(newX, safeY, newZ);
      QuestManager.getInstance().getWaypointManager().setWaypoint(player, "bingo_hunt", new WaypointData(waypointPos, WaypointData.WaypointType.PVP_BINGO, this.targetDef.getDisplayName()));
      int remaining = 3 - this.locationAttempts;
      if (remaining > 0) {
         player.sendMessage(new TextComponentString("§eYour target wasn't here... A new lead has appeared. Follow the waypoint."));
      } else {
         player.sendMessage(new TextComponentString("§6Final lead confirmed! Your target has been cornered. Follow the waypoint."));
      }

      EndgameNetworkHelper.sendBingoSync(player);
   }

   private void spawnTarget(World world, EntityPlayerMP player, BlockPos pos) {
      Entity entity = this.spawnNpcFromConfig(world, player, this.targetDef.getNpcConfigId(), pos);
      if (entity != null) {
         entity.getEntityData().setBoolean("bingoTarget", true);
         entity.getEntityData().setString("bingoTargetId", this.targetDef.getTargetId());
         this.targetEntityUUID = entity.getUniqueID();
         this.spawnedEntityUUIDs.add(entity.getUniqueID());
         this.targetSpawned = true;
      }

   }

   private void spawnDecoys(World world, EntityPlayerMP player, BlockPos center, int count) {
      String[] decoyIds = this.targetDef.getDecoyConfigIds();
      if (decoyIds != null && decoyIds.length != 0) {
         Random rand = world.rand;

         for(int i = 0; i < count; ++i) {
            String configId = decoyIds[rand.nextInt(decoyIds.length)];
            double offsetX = (rand.nextDouble() - (double)0.5F) * (double)16.0F;
            double offsetZ = (rand.nextDouble() - (double)0.5F) * (double)16.0F;
            BlockPos decoyPos = new BlockPos(center.getX() + (int)offsetX, WaypointSpawnLogic.findGroundY(world, center.getX() + (int)offsetX, center.getZ() + (int)offsetZ), center.getZ() + (int)offsetZ);
            Entity entity = this.spawnNpcFromConfig(world, player, configId, decoyPos);
            if (entity != null) {
               entity.getEntityData().setBoolean("bingoDecoy", true);
               this.spawnedEntityUUIDs.add(entity.getUniqueID());
            }
         }

      }
   }

   private void spawnAmbush(World world, EntityPlayerMP player, int count) {
      String[] ambushIds = this.targetDef.getAmbushConfigIds();
      if (ambushIds != null && ambushIds.length != 0) {
         Random rand = world.rand;
         double behindAngle = Math.toRadians((double)player.rotationYaw + (double)180.0F);

         for(int i = 0; i < count; ++i) {
            String configId = ambushIds[rand.nextInt(ambushIds.length)];
            double dist = (double)8.0F + rand.nextDouble() * (double)8.0F;
            double spread = (rand.nextDouble() - (double)0.5F) * (double)6.0F;
            int ax = (int)(player.posX + Math.sin(behindAngle) * dist + spread);
            int az = (int)(player.posZ - Math.cos(behindAngle) * dist + spread);
            int ay = WaypointSpawnLogic.findGroundY(world, ax, az);
            BlockPos ambushPos = new BlockPos(ax, ay, az);
            Entity entity = this.spawnNpcFromConfig(world, player, configId, ambushPos);
            if (entity != null) {
               entity.getEntityData().setBoolean("bingoAmbush", true);
               this.spawnedEntityUUIDs.add(entity.getUniqueID());
            }
         }

      }
   }

   private Entity spawnNpcFromConfig(World world, EntityPlayerMP player, String npcConfigId, BlockPos pos) {
      NpcConfig config = NpcConfigRegistry.get(npcConfigId);
      if (config == null) {
         System.out.println("[BingoSpawn] Unknown NPC config: " + npcConfigId);
         return null;
      } else {
         Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(config.getEntityRegistryId()), world);
         if (entity == null) {
            System.out.println("[BingoSpawn] Failed to create entity: " + config.getEntityRegistryId());
            return null;
         } else {
            double spawnX = (double)pos.getX() + (double)0.5F;
            double spawnZ = (double)pos.getZ() + (double)0.5F;
            double spawnY = (double)pos.getY();
            double dx = player.posX - spawnX;
            double dz = player.posZ - spawnZ;
            float yaw = (float)(MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
            entity.setLocationAndAngles(spawnX, spawnY, spawnZ, yaw, 0.0F);
            if (entity instanceof EntityLivingBase) {
               ((EntityLivingBase)entity).rotationYawHead = yaw;
               ((EntityLivingBase)entity).renderYawOffset = yaw;
            }

            NBTTagCompound entityData = entity.getEntityData();
            entityData.setBoolean("questEntity", true);
            entityData.setString("ownerUUID", player.getUniqueID().toString());
            entityData.setString("npcConfigId", npcConfigId);
            entityData.setBoolean("bingoEntity", true);
            if (entity instanceof INpcConfigurable) {
               ((INpcConfigurable)entity).applyNpcConfig(config);
            }

            if (entity instanceof EntityLivingBase && this.boardTier != null) {
               EntityLivingBase living = (EntityLivingBase)entity;
               double hpMult = this.boardTier.getHpMultiplier();
               double dmgMult = this.boardTier.getDmgMultiplier();
               if (hpMult != (double)1.0F) {
                  double scaledMaxHp = living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() * hpMult;
                  living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(scaledMaxHp);
                  living.setHealth((float)scaledMaxHp);
               }

               if (dmgMult != (double)1.0F && living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
                  double scaledDmg = living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue() * dmgMult;
                  living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(scaledDmg);
               }
            }

            if (entity instanceof EntityLiving) {
               ((EntityLiving)entity).enablePersistence();
            }

            entity.getEntityData().setBoolean("PersistenceRequired", true);
            world.spawnEntity(entity);
            return entity;
         }
      }
   }

   public void cleanup(World world) {
      WaypointSpawnLogic.cleanupQuestEntities(world, this.spawnedEntityUUIDs);
      this.spawnedEntityUUIDs.clear();
   }

   public boolean isTargetEntity(UUID entityUUID) {
      return this.targetEntityUUID != null && this.targetEntityUUID.equals(entityUUID);
   }

   public void confirmTargetKilled() {
      this.targetConfirmedDead = true;
   }

   public boolean isFailed() {
      return this.failed;
   }

   private double getDistanceToSpawn(EntityPlayerMP player) {
      double dx = player.posX - (double)this.spawnLocation.getX();
      double dz = player.posZ - (double)this.spawnLocation.getZ();
      return Math.sqrt(dx * dx + dz * dz);
   }

   private Entity findEntityByUUID(World world, UUID uuid) {
      if (world instanceof WorldServer) {
         return ((WorldServer)world).getEntityFromUuid(uuid);
      } else {
         for(Entity entity : world.loadedEntityList) {
            if (entity != null && !entity.isDead && entity.getUniqueID().equals(uuid)) {
               return entity;
            }
         }

         return null;
      }
   }
}
