
package net.luck.narutoaddon.OtherCode.raid.core;

import net.luck.narutoaddon.OtherCode.raid.arena.RaidArenaStorage;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class RaidRecoveryData extends WorldSavedData {
   private static final String DATA_NAME = "RaidRecoveryData";
   private Map<Integer, Set<Long>> raidBlockPositions = new HashMap();
   private Map<Integer, Set<String>> raidEntityUUIDs = new HashMap();
   private Map<String, long[]> playerPreRaidPositions = new HashMap();
   private Set<String> disconnectPenaltyPlayers = new HashSet();
   private Set<Integer> activeArenaIds = new HashSet();
   private Map<String, Long> raidCooldowns = new HashMap();

   public RaidRecoveryData() {
      super("RaidRecoveryData");
   }

   public RaidRecoveryData(String name) {
      super(name);
   }

   public static RaidRecoveryData get(World world) {
      MapStorage storage = world.getMapStorage();
      if (storage == null) {
         return new RaidRecoveryData();
      } else {
         RaidRecoveryData data = (RaidRecoveryData)storage.getOrLoadData(RaidRecoveryData.class, "RaidRecoveryData");
         if (data == null) {
            data = new RaidRecoveryData();
            storage.setData("RaidRecoveryData", data);
         }

         return data;
      }
   }

   public void trackBlockForRaid(int raidId, BlockPos pos) {
      ((Set)this.raidBlockPositions.computeIfAbsent(raidId, (k) -> new HashSet())).add(pos.toLong());
      this.markDirty();
   }

   public void trackBlocksForRaid(int raidId, Collection<BlockPos> positions) {
      Set<Long> set = (Set)this.raidBlockPositions.computeIfAbsent(raidId, (k) -> new HashSet());

      for(BlockPos pos : positions) {
         set.add(pos.toLong());
      }

      this.markDirty();
   }

   public void clearRaidBlocks(int raidId) {
      this.raidBlockPositions.remove(raidId);
      this.markDirty();
   }

   public Set<BlockPos> getRaidBlocks(int raidId) {
      Set<Long> longs = (Set)this.raidBlockPositions.get(raidId);
      if (longs == null) {
         return Collections.emptySet();
      } else {
         Set<BlockPos> result = new HashSet();

         for(long l : longs) {
            result.add(BlockPos.fromLong(l));
         }

         return result;
      }
   }

   public void trackEntityForRaid(int raidId, UUID entityUUID) {
      ((Set)this.raidEntityUUIDs.computeIfAbsent(raidId, (k) -> new HashSet())).add(entityUUID.toString());
      this.markDirty();
   }

   public void clearRaidEntities(int raidId) {
      this.raidEntityUUIDs.remove(raidId);
      this.markDirty();
   }

   public void clearRaid(int raidId) {
      this.raidBlockPositions.remove(raidId);
      this.raidEntityUUIDs.remove(raidId);
      this.activeArenaIds.remove(raidId);
      this.markDirty();
   }

   public void savePlayerPreRaidPosition(UUID playerUUID, BlockPos pos) {
      this.playerPreRaidPositions.put(playerUUID.toString(), new long[]{(long)pos.getX(), (long)pos.getY(), (long)pos.getZ()});
      this.markDirty();
   }

   public BlockPos consumePlayerPreRaidPosition(UUID playerUUID) {
      long[] coords = (long[])this.playerPreRaidPositions.remove(playerUUID.toString());
      if (coords != null && coords.length == 3) {
         this.markDirty();
         return new BlockPos((int)coords[0], (int)coords[1], (int)coords[2]);
      } else {
         return null;
      }
   }

   public void clearPlayerPreRaidPosition(UUID playerUUID) {
      if (this.playerPreRaidPositions.remove(playerUUID.toString()) != null) {
         this.markDirty();
      }

   }

   public void addDisconnectPenalty(UUID playerUUID) {
      this.disconnectPenaltyPlayers.add(playerUUID.toString());
      this.markDirty();
   }

   public boolean hasDisconnectPenalty(UUID playerUUID) {
      return this.disconnectPenaltyPlayers.contains(playerUUID.toString());
   }

   public void clearDisconnectPenalty(UUID playerUUID) {
      if (this.disconnectPenaltyPlayers.remove(playerUUID.toString())) {
         this.markDirty();
      }

   }

   public void setRaidCooldown(UUID playerUUID, long endTimeMs) {
      this.raidCooldowns.put(playerUUID.toString(), endTimeMs);
      this.markDirty();
   }

   public long getRaidCooldownEnd(UUID playerUUID) {
      Long end = (Long)this.raidCooldowns.get(playerUUID.toString());
      return end != null ? end : 0L;
   }

   public void clearRaidCooldown(UUID playerUUID) {
      if (this.raidCooldowns.remove(playerUUID.toString()) != null) {
         this.markDirty();
      }

   }

   public Map<String, Long> getAllRaidCooldowns() {
      return Collections.unmodifiableMap(this.raidCooldowns);
   }

   public void cleanExpiredCooldowns() {
      long now = System.currentTimeMillis();
      boolean changed = this.raidCooldowns.entrySet().removeIf((e) -> now >= (Long)e.getValue());
      if (changed) {
         this.markDirty();
      }

   }

   public void markArenaInUse(int arenaId) {
      this.activeArenaIds.add(arenaId);
      this.markDirty();
   }

   public void markArenaFree(int arenaId) {
      this.activeArenaIds.remove(arenaId);
      this.markDirty();
   }

   public void performStartupCleanup(World world) {
      if (world != null) {
         int blocksRemoved = 0;
         int entitiesRemoved = 0;

         for(Map.Entry<Integer, Set<Long>> entry : this.raidBlockPositions.entrySet()) {
            int raidId = (Integer)entry.getKey();

            for(long blockLong : (Set)entry.getValue()) {
               BlockPos pos = BlockPos.fromLong(blockLong);
               Block block = world.getBlockState(pos).getBlock();
               if (block == Blocks.LOG || block == Blocks.LOG2 || block == Blocks.LEAVES || block == Blocks.LEAVES2 || block == Blocks.STAINED_GLASS || block == Blocks.OAK_FENCE) {
                  world.setBlockToAir(pos);
                  ++blocksRemoved;
               }
            }
         }

         if (world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer)world;
            List<Entity> toRemove = new ArrayList();

            for(Entity entity : worldServer.loadedEntityList) {
               if (entity != null && !entity.isDead) {
                  NBTTagCompound entityData = entity.getEntityData();
                  if (entityData.getBoolean("isRaidEntity")) {
                     toRemove.add(entity);
                  }
               }
            }

            for(Entity entity : toRemove) {
               entity.setDead();
               ++entitiesRemoved;
            }
         }

         for(Map.Entry<Integer, Set<String>> entry : this.raidEntityUUIDs.entrySet()) {
            for(String uuidStr : (Set)entry.getValue()) {
               try {
                  UUID entityUUID = UUID.fromString(uuidStr);
                  if (world instanceof WorldServer) {
                     Entity entity = ((WorldServer)world).getEntityFromUuid(entityUUID);
                     if (entity != null && !entity.isDead) {
                        entity.setDead();
                        ++entitiesRemoved;
                     }
                  }
               } catch (IllegalArgumentException var13) {
               }
            }
         }

         if (!this.activeArenaIds.isEmpty()) {
            RaidArenaStorage arenaStorage = RaidArenaStorage.get(world);
            if (arenaStorage != null) {
               arenaStorage.resetAllArenaInUseFlags();
            }

            System.out.println("[RaidRecovery] Reset " + this.activeArenaIds.size() + " arena(s) to available");
         }

         if (blocksRemoved > 0 || entitiesRemoved > 0) {
            System.out.println("[RaidRecovery] Startup cleanup complete:");
            System.out.println("[RaidRecovery]   Blocks removed: " + blocksRemoved);
            System.out.println("[RaidRecovery]   Entities removed: " + entitiesRemoved);
            System.out.println("[RaidRecovery]   Players with saved positions: " + this.playerPreRaidPositions.size());
            System.out.println("[RaidRecovery]   Players with disconnect penalty: " + this.disconnectPenaltyPlayers.size());
         }

         this.raidBlockPositions.clear();
         this.raidEntityUUIDs.clear();
         this.activeArenaIds.clear();
         this.markDirty();
      }
   }

   public void onPlayerLogin(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      BlockPos savedPos = this.consumePlayerPreRaidPosition(uuid);
      if (savedPos != null) {
         player.connection.setPlayerLocation((double)savedPos.getX() + (double)0.5F, (double)savedPos.getY(), (double)savedPos.getZ() + (double)0.5F, player.rotationYaw, player.rotationPitch);
         player.sendMessage(new TextComponentString("§e[Raid Recovery] You were returned to your previous position after a server restart."));
         System.out.println("[RaidRecovery] Restored player " + player.getName() + " to pre-raid position " + savedPos);
      }

      if (this.hasDisconnectPenalty(uuid)) {
         this.clearDisconnectPenalty(uuid);
         player.attackEntityFrom(DamageSource.OUT_OF_WORLD, 10000.0F);
         player.sendMessage(new TextComponentString("§c[Raid] You disconnected during a raid. Combat-logging penalty applied."));
         System.out.println("[RaidRecovery] Applied disconnect penalty to " + player.getName());
      }

   }

   public boolean hasOrphanedData() {
      return !this.raidBlockPositions.isEmpty() || !this.raidEntityUUIDs.isEmpty() || !this.activeArenaIds.isEmpty();
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.raidBlockPositions.clear();
      this.raidEntityUUIDs.clear();
      this.playerPreRaidPositions.clear();
      this.disconnectPenaltyPlayers.clear();
      this.activeArenaIds.clear();
      this.raidCooldowns.clear();
      if (nbt.hasKey("raidBlocks")) {
         NBTTagList raidList = nbt.getTagList("raidBlocks", 10);

         for(int i = 0; i < raidList.tagCount(); ++i) {
            NBTTagCompound raidTag = raidList.getCompoundTagAt(i);
            int raidId = raidTag.getInteger("raidId");
            Set<Long> blocks = new HashSet();
            NBTTagList blockList = raidTag.getTagList("blockList", 4);

            for(int j = 0; j < blockList.tagCount(); ++j) {
               blocks.add(((NBTTagLong)blockList.get(j)).getLong());
            }

            if (!blocks.isEmpty()) {
               this.raidBlockPositions.put(raidId, blocks);
            }
         }
      }

      if (nbt.hasKey("raidEntities")) {
         NBTTagList entityList = nbt.getTagList("raidEntities", 10);

         for(int i = 0; i < entityList.tagCount(); ++i) {
            NBTTagCompound entityTag = entityList.getCompoundTagAt(i);
            int raidId = entityTag.getInteger("raidId");
            Set<String> uuids = new HashSet();
            NBTTagList uuidList = entityTag.getTagList("uuids", 8);

            for(int j = 0; j < uuidList.tagCount(); ++j) {
               uuids.add(uuidList.getStringTagAt(j));
            }

            if (!uuids.isEmpty()) {
               this.raidEntityUUIDs.put(raidId, uuids);
            }
         }
      }

      if (nbt.hasKey("playerPositions")) {
         NBTTagList posList = nbt.getTagList("playerPositions", 10);

         for(int i = 0; i < posList.tagCount(); ++i) {
            NBTTagCompound posTag = posList.getCompoundTagAt(i);
            String uuid = posTag.getString("uuid");
            long[] coords = new long[]{posTag.getLong("x"), posTag.getLong("y"), posTag.getLong("z")};
            this.playerPreRaidPositions.put(uuid, coords);
         }
      }

      if (nbt.hasKey("penalties")) {
         NBTTagList penaltyList = nbt.getTagList("penalties", 8);

         for(int i = 0; i < penaltyList.tagCount(); ++i) {
            this.disconnectPenaltyPlayers.add(penaltyList.getStringTagAt(i));
         }
      }

      if (nbt.hasKey("activeArenas")) {
         int[] arenaIds = nbt.getIntArray("activeArenas");

         for(int id : arenaIds) {
            this.activeArenaIds.add(id);
         }
      }

      if (nbt.hasKey("raidCooldowns")) {
         NBTTagList cooldownList = nbt.getTagList("raidCooldowns", 10);
         long now = System.currentTimeMillis();

         for(int i = 0; i < cooldownList.tagCount(); ++i) {
            NBTTagCompound cdTag = cooldownList.getCompoundTagAt(i);
            String uuid = cdTag.getString("uuid");
            long endTime = cdTag.getLong("endTime");
            if (endTime > now) {
               this.raidCooldowns.put(uuid, endTime);
            }
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagList raidBlockList = new NBTTagList();

      for(Map.Entry<Integer, Set<Long>> entry : this.raidBlockPositions.entrySet()) {
         NBTTagCompound raidTag = new NBTTagCompound();
         raidTag.setInteger("raidId", (Integer)entry.getKey());
         NBTTagList blockList = new NBTTagList();

         for(long l : (Set)entry.getValue()) {
            blockList.appendTag(new NBTTagLong(l));
         }

         raidTag.setTag("blockList", blockList);
         raidBlockList.appendTag(raidTag);
      }

      nbt.setTag("raidBlocks", raidBlockList);
      NBTTagList entityTagList = new NBTTagList();

      for(Map.Entry<Integer, Set<String>> entry : this.raidEntityUUIDs.entrySet()) {
         NBTTagCompound entityTag = new NBTTagCompound();
         entityTag.setInteger("raidId", (Integer)entry.getKey());
         NBTTagList uuidList = new NBTTagList();

         for(String uuid : (Set)entry.getValue()) {
            uuidList.appendTag(new NBTTagString(uuid));
         }

         entityTag.setTag("uuids", uuidList);
         entityTagList.appendTag(entityTag);
      }

      nbt.setTag("raidEntities", entityTagList);
      NBTTagList posList = new NBTTagList();

      for(Map.Entry<String, long[]> entry : this.playerPreRaidPositions.entrySet()) {
         NBTTagCompound posTag = new NBTTagCompound();
         posTag.setString("uuid", (String)entry.getKey());
         posTag.setLong("x", ((long[])entry.getValue())[0]);
         posTag.setLong("y", ((long[])entry.getValue())[1]);
         posTag.setLong("z", ((long[])entry.getValue())[2]);
         posList.appendTag(posTag);
      }

      nbt.setTag("playerPositions", posList);
      NBTTagList penaltyList = new NBTTagList();

      for(String uuid : this.disconnectPenaltyPlayers) {
         penaltyList.appendTag(new NBTTagString(uuid));
      }

      nbt.setTag("penalties", penaltyList);
      int[] arenaIds = new int[this.activeArenaIds.size()];
      int idx = 0;

      for(int id : this.activeArenaIds) {
         arenaIds[idx++] = id;
      }

      nbt.setIntArray("activeArenas", arenaIds);
      NBTTagList cooldownList = new NBTTagList();
      long now = System.currentTimeMillis();

      for(Map.Entry<String, Long> cdEntry : this.raidCooldowns.entrySet()) {
         if ((Long)cdEntry.getValue() > now) {
            NBTTagCompound cdTag = new NBTTagCompound();
            cdTag.setString("uuid", (String)cdEntry.getKey());
            cdTag.setLong("endTime", (Long)cdEntry.getValue());
            cooldownList.appendTag(cdTag);
         }
      }

      nbt.setTag("raidCooldowns", cooldownList);
      return nbt;
   }
}
