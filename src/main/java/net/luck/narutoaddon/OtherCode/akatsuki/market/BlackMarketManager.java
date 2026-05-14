package net.luck.narutoaddon.OtherCode.akatsuki.market;

import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiMember;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.territory.core.TerritoryManager;
import net.luck.narutoaddon.OtherCode.territory.core.TerritorySavedData;
import net.luck.narutoaddon.OtherCode.territory.core.TerritoryZone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlackMarketManager {
   private static final BlackMarketManager INSTANCE = new BlackMarketManager();
   private final Map<String, BlackMarketDeployment> activeDeployments = new ConcurrentHashMap();
   private final Map<UUID, Map<String, Long>> playerCooldowns = new ConcurrentHashMap();
   private final Map<String, Long> activeBuffs = new ConcurrentHashMap();
   private int tickCounter = 0;
   private static final Random rand = new Random();

   public static BlackMarketManager getInstance() {
      return INSTANCE;
   }

   private BlackMarketManager() {
   }

   public String validatePurchase(UUID playerId, String itemId) {
      BlackMarketItem item = BlackMarketRegistry.get(itemId);
      if (item == null) {
         return "Unknown item: " + itemId;
      } else {
         AkatsukiMember member = AkatsukiManager.getInstance().getMember(playerId);
         if (member == null) {
            return "You must be an Akatsuki member.";
         } else if (member.getBountyTokens() < item.getTokenCost()) {
            return "Not enough tokens. Need " + item.getTokenCost() + ", have " + member.getBountyTokens() + ".";
         } else if (this.isOnCooldown(playerId, itemId)) {
            long remaining = this.getCooldownRemaining(playerId, itemId);
            return "On cooldown. " + formatMs(remaining) + " remaining.";
         } else {
            return null;
         }
      }
   }

   public String executePurchase(EntityPlayerMP player, String itemId, String targetZoneId, World world) {
      return null;
   }

   private void spawnDeploymentNpcs(BlackMarketDeployment deployment, BlackMarketItem item, World world) {
      String zoneId = deployment.getCurrentZoneId();
      TerritorySavedData savedData = TerritoryManager.getInstance().getSavedData();
      if (savedData == null) {
         System.err.println("[BlackMarket] Cannot spawn NPCs — TerritorySavedData is null");
      } else {
         TerritoryZone zone = savedData.getOrCreateZone(zoneId);
         if (zone == null) {
            System.err.println("[BlackMarket] Cannot spawn NPCs — zone not found: " + zoneId);
         } else {
            String[] npcConfigIds = item.getNpcConfigIds();
            if (npcConfigIds != null && npcConfigIds.length != 0) {
               int chunkX = zone.getCenterX() >> 4;
               int chunkZ = zone.getCenterZ() >> 4;
               if (!world.isBlockLoaded(new BlockPos(zone.getCenterX(), 64, zone.getCenterZ()))) {
                  world.getChunkProvider().provideChunk(chunkX, chunkZ);
                  System.out.println("[BlackMarket] Force-loaded chunk at " + chunkX + ", " + chunkZ + " for zone " + zoneId);
               }

               for(String configId : npcConfigIds) {
                  NpcConfig config = NpcConfigRegistry.get(configId);
                  if (config == null) {
                     System.err.println("[BlackMarket] NPC config not found: " + configId + " — skipping");
                  } else {
                     Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(config.getEntityRegistryId()), world);
                     if (entity == null) {
                        System.err.println("[BlackMarket] Failed to create entity for config: " + configId);
                     } else {
                        double offsetX = (rand.nextDouble() - (double)0.5F) * (double)6.0F;
                        double offsetZ = (rand.nextDouble() - (double)0.5F) * (double)6.0F;
                        double spawnX = (double)zone.getCenterX() + (double)0.5F + offsetX;
                        double spawnZ = (double)zone.getCenterZ() + (double)0.5F + offsetZ;
                        int safeY = findSafeSpawnY(world, (int)spawnX, (int)spawnZ);
                        if (safeY <= 0) {
                           safeY = 64;
                        }

                        float yaw = (float)(rand.nextDouble() * (double)360.0F);
                        entity.setLocationAndAngles(spawnX, (double)safeY, spawnZ, yaw, 0.0F);
                        if (entity instanceof EntityLivingBase) {
                           ((EntityLivingBase)entity).rotationYawHead = yaw;
                           ((EntityLivingBase)entity).renderYawOffset = yaw;
                        }

                        NBTTagCompound entityData = entity.getEntityData();
                        entityData.setBoolean("blackMarketNpc", true);
                        entityData.setString("blackMarketDeploymentId", deployment.getDeploymentId());
                        entityData.setString("npcConfigId", configId);
                        entityData.setBoolean("PersistenceRequired", true);
                        entityData.setBoolean("territoryEntity", true);
                        if (entity instanceof INpcConfigurable) {
                           ((INpcConfigurable)entity).applyNpcConfig(config);
                        }

                        if (config.getTexture() != null && entity instanceof QuestNpcBase) {
                           ((QuestNpcBase)entity).setTextureOverride(config.getTexture().toString());
                        }

                        if (entity instanceof EntityCreature) {
                           ((EntityCreature)entity).getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)80.0F);
                        }

                        if (entity instanceof EntityLiving) {
                           ((EntityLiving)entity).enablePersistence();
                        }

                        world.spawnEntity(entity);
                        deployment.addNpcUUID(entity.getUniqueID());
                        System.out.println("[BlackMarket] Spawned " + configId + " at " + (int)spawnX + ", " + safeY + ", " + (int)spawnZ + " in zone " + zoneId + " (deployment: " + deployment.getDeploymentId() + ")");
                     }
                  }
               }

            } else {
               System.out.println("[BlackMarket] No NPC configs for item " + item.getId() + " — skipping spawn");
            }
         }
      }
   }

   private void relocateNpcsToZone(BlackMarketDeployment deployment, TerritoryZone newZone, World world) {
      for(UUID npcUUID : deployment.getSpawnedNpcUUIDs()) {
         Entity entity = findEntityByUUID(world, npcUUID);
         if (entity != null && entity.isEntityAlive()) {
            double offsetX = (rand.nextDouble() - (double)0.5F) * (double)6.0F;
            double offsetZ = (rand.nextDouble() - (double)0.5F) * (double)6.0F;
            double newX = (double)newZone.getCenterX() + (double)0.5F + offsetX;
            double newZ = (double)newZone.getCenterZ() + (double)0.5F + offsetZ;
            int newY = findSafeSpawnY(world, (int)newX, (int)newZ);
            if (newY <= 0) {
               newY = 64;
            }

            entity.setPositionAndUpdate(newX, (double)newY, newZ);
         }
      }

   }

   private int countAliveNpcs(BlackMarketDeployment deployment, World world) {
      int count = 0;

      for(UUID npcUUID : deployment.getSpawnedNpcUUIDs()) {
         Entity entity = findEntityByUUID(world, npcUUID);
         if (entity != null && entity.isEntityAlive()) {
            ++count;
         }
      }

      return count;
   }

   private void executeSabotage(String zoneId, World world) {
      TerritorySavedData savedData = TerritoryManager.getInstance().getSavedData();
      if (savedData != null) {
         TerritoryZone zone = savedData.getOrCreateZone(zoneId);
         if (zone != null) {
            zone.setWallLevel(0);
            zone.setGarrisonLevel(0);
            zone.setTrainingLevel(0);
            zone.setWatchtowerLevel(0);
            zone.setSpecialistLevel(0);
            savedData.markDirty();
            System.out.println("[BlackMarket] Sabotaged zone " + zoneId + " — all fortifications destroyed");
         }
      }

   }

   private void activateBuff(String itemId, long durationMs) {
      this.activeBuffs.put(itemId, System.currentTimeMillis() + durationMs);
   }

   public boolean isBuffActive(String itemId) {
      Long expiry = (Long)this.activeBuffs.get(itemId);
      if (expiry == null) {
         return false;
      } else if (System.currentTimeMillis() >= expiry) {
         this.activeBuffs.remove(itemId);
         return false;
      } else {
         return true;
      }
   }

   public boolean isBountyBoostActive() {
      return this.isBuffActive("bounty_boost");
   }

   public boolean isEspionageNetworkActive() {
      return this.isBuffActive("espionage_network");
   }

   public float getTerritoryPointMultiplier() {
      return 1.0F;
   }

   private boolean isOnCooldown(UUID playerId, String itemId) {
      Map<String, Long> cooldowns = (Map)this.playerCooldowns.get(playerId);
      if (cooldowns == null) {
         return false;
      } else {
         Long expiry = (Long)cooldowns.get(itemId);
         if (expiry == null) {
            return false;
         } else if (System.currentTimeMillis() >= expiry) {
            cooldowns.remove(itemId);
            return false;
         } else {
            return true;
         }
      }
   }

   private long getCooldownRemaining(UUID playerId, String itemId) {
      Map<String, Long> cooldowns = (Map)this.playerCooldowns.get(playerId);
      if (cooldowns == null) {
         return 0L;
      } else {
         Long expiry = (Long)cooldowns.get(itemId);
         return expiry == null ? 0L : Math.max(0L, expiry - System.currentTimeMillis());
      }
   }

   private void setCooldown(UUID playerId, String itemId, long durationMs) {
      ((Map)this.playerCooldowns.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap())).put(itemId, System.currentTimeMillis() + durationMs);
   }

   public void tick(World world) {
   }

   private String findNearestNonAkatsukiZone(TerritorySavedData savedData, TerritoryZone fromZone) {
      double bestDistSq = Double.MAX_VALUE;
      String bestZoneId = null;
      double fx = (double)fromZone.getCenterX();
      double fz = (double)fromZone.getCenterZ();

      for(TerritoryZone zone : savedData.getAllZones()) {
         if (!zone.getZoneId().equals(fromZone.getZoneId())) {
            String owner = zone.getCurrentOwner();
            if (!"akatsuki".equalsIgnoreCase(owner)) {
               double dx = (double)zone.getCenterX() - fx;
               double dz = (double)zone.getCenterZ() - fz;
               double distSq = dx * dx + dz * dz;
               if (distSq < bestDistSq) {
                  bestDistSq = distSq;
                  bestZoneId = zone.getZoneId();
               }
            }
         }
      }

      return bestZoneId;
   }

   private void despawnDeploymentNpcs(BlackMarketDeployment dep, World world) {
      for(UUID npcUUID : dep.getSpawnedNpcUUIDs()) {
         Entity entity = findEntityByUUID(world, npcUUID);
         if (entity != null && entity.isEntityAlive()) {
            entity.setDead();
         }
      }

   }

   public List<BlackMarketDeployment> getActiveDeployments() {
      return new ArrayList(this.activeDeployments.values());
   }

   public List<BlackMarketDeployment> getDeploymentsByPlayer(UUID playerId) {
      List<BlackMarketDeployment> result = new ArrayList();

      for(BlackMarketDeployment dep : this.activeDeployments.values()) {
         if (dep.getPurchaserId().equals(playerId)) {
            result.add(dep);
         }
      }

      return result;
   }

   public int getActiveDeploymentCount() {
      return this.activeDeployments.size();
   }

   private static Entity findEntityByUUID(World world, UUID uuid) {
      for(Entity entity : world.loadedEntityList) {
         if (entity.getUniqueID().equals(uuid)) {
            return entity;
         }
      }

      return null;
   }

   private static int findSafeSpawnY(World world, int x, int z) {
      int topY = world.getHeight(x, z);
      if (topY <= 1) {
         topY = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
         if (topY <= 1) {
            return 64;
         }
      }

      for(int y = topY - 1; y > 0; --y) {
         IBlockState state = world.getBlockState(new BlockPos(x, y, z));
         Material mat = state.getMaterial();
         if (mat != Material.AIR && mat != Material.PLANTS && mat != Material.VINE && mat != Material.LEAVES && mat != Material.WOOD && mat != Material.SNOW && mat != Material.CARPET && mat != Material.WEB && mat != Material.WATER) {
            int spawnY = y + 1;
            Material feetMat = world.getBlockState(new BlockPos(x, spawnY, z)).getMaterial();
            Material headMat = world.getBlockState(new BlockPos(x, spawnY + 1, z)).getMaterial();
            boolean feetClear = feetMat == Material.AIR || feetMat == Material.PLANTS || feetMat == Material.VINE || feetMat == Material.SNOW;
            boolean headClear = headMat == Material.AIR || headMat == Material.PLANTS || headMat == Material.VINE || headMat == Material.SNOW;
            if (feetClear && headClear) {
               return spawnY;
            }
         }
      }

      return 64;
   }

   private static String formatMs(long ms) {
      long seconds = ms / 1000L % 60L;
      long minutes = ms / 60000L % 60L;
      return minutes > 0L ? minutes + "m " + seconds + "s" : seconds + "s";
   }
}
