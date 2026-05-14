
package net.luck.narutoaddon.OtherCode.quest.core;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;

public class TerrainCache extends WorldSavedData {
   private static final String DATA_ID = "InfTsukTerrainCache";
   private static final int GRID_SHIFT = 3;
   private final Map<Long, Boolean> cache = new HashMap();

   public TerrainCache(String name) {
      super(name);
   }

   public TerrainCache() {
      super("InfTsukTerrainCache");
   }

   public static TerrainCache get(World world) {
      MapStorage storage = world.getMapStorage();
      if (storage == null) {
         return new TerrainCache();
      } else {
         TerrainCache instance = (TerrainCache)storage.getOrLoadData(TerrainCache.class, "InfTsukTerrainCache");
         if (instance == null) {
            instance = new TerrainCache();
            storage.setData("InfTsukTerrainCache", instance);
         }

         return instance;
      }
   }

   public boolean isWater(World world, int x, int z) {
      long key = this.packKey(x, z);
      Boolean cached = (Boolean)this.cache.get(key);
      if (cached != null) {
         return cached;
      } else {
         boolean water = this.checkSurfaceBlock(world, x, z);
         this.cache.put(key, water);
         this.markDirty();
         return water;
      }
   }

   private boolean checkSurfaceBlock(World world, int x, int z) {
      if (!(world instanceof WorldServer)) {
         return false;
      } else {
         WorldServer ws = (WorldServer)world;
         ChunkProviderServer provider = ws.getChunkProvider();
         int cx = x >> 4;
         int cz = z >> 4;
         Chunk chunk = provider.getLoadedChunk(cx, cz);
         if (chunk == null) {
            chunk = provider.loadChunk(cx, cz);
         }

         if (chunk == null) {
            return false;
         } else {
            int localX = x & 15;
            int localZ = z & 15;
            int surfaceY = chunk.getHeightValue(localX, localZ);
            if (surfaceY <= 0) {
               return false;
            } else {
               IBlockState state = chunk.getBlockState(x, surfaceY - 1, z);
               return state.getMaterial() == Material.WATER;
            }
         }
      }
   }

   public int getCacheSize() {
      return this.cache.size();
   }

   private long packKey(int x, int z) {
      int gx = x >> 3;
      int gz = z >> 3;
      return (long)gx << 32 | (long)gz & 4294967295L;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.cache.clear();
      if (nbt.hasKey("tcKeys")) {
         int[] keys = nbt.getIntArray("tcKeys");
         byte[] values = nbt.getByteArray("tcValues");

         for(int i = 0; i < values.length && i * 2 + 1 < keys.length; ++i) {
            int gx = keys[i * 2];
            int gz = keys[i * 2 + 1];
            long key = (long)gx << 32 | (long)gz & 4294967295L;
            this.cache.put(key, values[i] != 0);
         }

         System.out.println("[TerrainCache] Loaded " + this.cache.size() + " cached terrain entries");
      }
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      int[] keys = new int[this.cache.size() * 2];
      byte[] values = new byte[this.cache.size()];
      int i = 0;

      for(Map.Entry<Long, Boolean> entry : this.cache.entrySet()) {
         long packed = (Long)entry.getKey();
         keys[i * 2] = (int)(packed >> 32);
         keys[i * 2 + 1] = (int)packed;
         values[i] = (byte)((Boolean)entry.getValue() ? 1 : 0);
         ++i;
      }

      nbt.setIntArray("tcKeys", keys);
      nbt.setByteArray("tcValues", values);
      return nbt;
   }
}
