
package net.luck.narutoaddon.OtherCode.raid.arena;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class RaidArenaStorage extends WorldSavedData {
   private static final String DATA_NAME = "RaidArenaStorage";
   private Map<Integer, RaidArena> arenas = new HashMap();
   private int arenaIdCounter = 0;
   private BlockPos hubLocation = new BlockPos(0, 65, 0);

   public RaidArenaStorage() {
      super("RaidArenaStorage");
   }

   public RaidArenaStorage(String name) {
      super(name);
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.arenas.clear();
      this.arenaIdCounter = nbt.getInteger("arenaIdCounter");
      if (nbt.hasKey("hubX")) {
         this.hubLocation = new BlockPos(nbt.getInteger("hubX"), nbt.getInteger("hubY"), nbt.getInteger("hubZ"));
      }

      NBTTagList arenaList = nbt.getTagList("arenas", 10);

      for(int i = 0; i < arenaList.tagCount(); ++i) {
         NBTTagCompound arenaTag = arenaList.getCompoundTagAt(i);
         RaidArena arena = RaidArena.readFromNBT(arenaTag);
         this.arenas.put(arena.getArenaId(), arena);
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      nbt.setInteger("arenaIdCounter", this.arenaIdCounter);
      nbt.setInteger("hubX", this.hubLocation.getX());
      nbt.setInteger("hubY", this.hubLocation.getY());
      nbt.setInteger("hubZ", this.hubLocation.getZ());
      NBTTagList arenaList = new NBTTagList();

      for(RaidArena arena : this.arenas.values()) {
         arenaList.appendTag(arena.writeToNBT());
      }

      nbt.setTag("arenas", arenaList);
      return nbt;
   }

   public RaidArena createArena(String name, String bossId, BlockPos corner1, BlockPos corner2) {
      int arenaId = ++this.arenaIdCounter;
      RaidArena arena = new RaidArena(arenaId, name, bossId, corner1, corner2);
      this.arenas.put(arenaId, arena);
      this.markDirty();
      return arena;
   }

   public boolean removeArena(int arenaId) {
      RaidArena removed = (RaidArena)this.arenas.remove(arenaId);
      if (removed != null) {
         this.markDirty();
         return true;
      } else {
         return false;
      }
   }

   public RaidArena getArena(int arenaId) {
      return (RaidArena)this.arenas.get(arenaId);
   }

   public RaidArena getArenaByName(String name) {
      for(RaidArena arena : this.arenas.values()) {
         if (arena.getName().equalsIgnoreCase(name)) {
            return arena;
         }
      }

      return null;
   }

   public RaidArena findAvailableArena(String bossId) {
      for(RaidArena arena : this.arenas.values()) {
         if (arena.getBossId().equals(bossId) && arena.isAvailable()) {
            return arena;
         }
      }

      return null;
   }

   public Collection<RaidArena> getAllArenas() {
      return Collections.unmodifiableCollection(this.arenas.values());
   }

   public List<RaidArena> getArenasForBoss(String bossId) {
      List<RaidArena> result = new ArrayList();

      for(RaidArena arena : this.arenas.values()) {
         if (arena.getBossId().equals(bossId)) {
            result.add(arena);
         }
      }

      return result;
   }

   public int getArenaCount() {
      return this.arenas.size();
   }

   public int countAvailableArenas(String bossId) {
      int count = 0;

      for(RaidArena arena : this.arenas.values()) {
         if (arena.getBossId().equals(bossId) && arena.isAvailable()) {
            ++count;
         }
      }

      return count;
   }

   public BlockPos getHubLocation() {
      return this.hubLocation;
   }

   public void setHubLocation(BlockPos pos) {
      this.hubLocation = pos;
      this.markDirty();
   }

   public boolean setPlayerSpawn(int arenaId, int spawnIndex, BlockPos pos) {
      RaidArena arena = (RaidArena)this.arenas.get(arenaId);
      if (arena != null && spawnIndex >= 0 && spawnIndex < 6) {
         arena.setPlayerSpawn(spawnIndex, pos);
         this.markDirty();
         return true;
      } else {
         return false;
      }
   }

   public boolean setBossSpawn(int arenaId, BlockPos pos) {
      RaidArena arena = (RaidArena)this.arenas.get(arenaId);
      if (arena != null) {
         arena.setBossSpawn(pos);
         this.markDirty();
         return true;
      } else {
         return false;
      }
   }

   public boolean setArenaEnabled(int arenaId, boolean enabled) {
      RaidArena arena = (RaidArena)this.arenas.get(arenaId);
      if (arena != null) {
         arena.setEnabled(enabled);
         this.markDirty();
         return true;
      } else {
         return false;
      }
   }

   public RaidArena getArenaAtPosition(BlockPos pos) {
      for(RaidArena arena : this.arenas.values()) {
         if (arena.isInBounds(pos)) {
            return arena;
         }
      }

      return null;
   }

   public boolean doesOverlap(BlockPos corner1, BlockPos corner2) {
      for(RaidArena arena : this.arenas.values()) {
         if (this.boundariesOverlap(corner1, corner2, arena.getCorner1(), arena.getCorner2())) {
            return true;
         }
      }

      return false;
   }

   private boolean boundariesOverlap(BlockPos a1, BlockPos a2, BlockPos b1, BlockPos b2) {
      int aMinX = Math.min(a1.getX(), a2.getX());
      int aMaxX = Math.max(a1.getX(), a2.getX());
      int aMinY = Math.min(a1.getY(), a2.getY());
      int aMaxY = Math.max(a1.getY(), a2.getY());
      int aMinZ = Math.min(a1.getZ(), a2.getZ());
      int aMaxZ = Math.max(a1.getZ(), a2.getZ());
      int bMinX = Math.min(b1.getX(), b2.getX());
      int bMaxX = Math.max(b1.getX(), b2.getX());
      int bMinY = Math.min(b1.getY(), b2.getY());
      int bMaxY = Math.max(b1.getY(), b2.getY());
      int bMinZ = Math.min(b1.getZ(), b2.getZ());
      int bMaxZ = Math.max(b1.getZ(), b2.getZ());
      return aMinX <= bMaxX && aMaxX >= bMinX && aMinY <= bMaxY && aMaxY >= bMinY && aMinZ <= bMaxZ && aMaxZ >= bMinZ;
   }

   public void resetAllArenaInUseFlags() {
      for(RaidArena arena : this.arenas.values()) {
         arena.setInUse(false);
      }

   }

   public static RaidArenaStorage get(World world) {
      if (world != null && world.getMapStorage() != null) {
         MapStorage storage = world.getMapStorage();
         RaidArenaStorage instance = (RaidArenaStorage)storage.getOrLoadData(RaidArenaStorage.class, "RaidArenaStorage");
         if (instance == null) {
            instance = new RaidArenaStorage();
            storage.setData("RaidArenaStorage", instance);
         }

         return instance;
      } else {
         return null;
      }
   }
}
