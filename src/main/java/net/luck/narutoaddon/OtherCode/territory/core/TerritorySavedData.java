
package net.luck.narutoaddon.OtherCode.territory.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class TerritorySavedData extends WorldSavedData {
   private static final String DATA_NAME = "InfTsukTerritoryData";
   private final Map<String, TerritoryZone> zones = new LinkedHashMap();
   private final Map<UUID, PerPlayerTerritoryData> playerData = new HashMap();
   private final Map<String, Integer> weeklyVillageScores = new HashMap();
   private long lastWeeklyResetMs = 0L;
   private int[] terrainMapPixels;

   public TerritorySavedData() {
      super("InfTsukTerritoryData");
   }

   public TerritorySavedData(String name) {
      super(name);
   }

   public static TerritorySavedData get(World world) {
      MapStorage storage = world.getMapStorage();
      if (storage == null) {
         return new TerritorySavedData();
      } else {
         TerritorySavedData data = (TerritorySavedData)storage.getOrLoadData(TerritorySavedData.class, "InfTsukTerritoryData");
         if (data == null) {
            data = new TerritorySavedData();
            storage.setData("InfTsukTerritoryData", data);
         }

         return data;
      }
   }

   public TerritoryZone getOrCreateZone(String zoneId) {
      return (TerritoryZone)this.zones.get(zoneId);
   }

   public void putZone(TerritoryZone zone) {
   }

   public Collection<TerritoryZone> getAllZones() {
      return this.zones.values();
   }

   public int getZoneCount() {
      return this.zones.size();
   }

   public PerPlayerTerritoryData getOrCreatePlayerData(UUID playerId) {
      return (PerPlayerTerritoryData)this.playerData.computeIfAbsent(playerId, (k) -> new PerPlayerTerritoryData());
   }

   public Map<UUID, PerPlayerTerritoryData> getAllPlayerData() {
      return this.playerData;
   }

   public int getVillageScore(String village) {
      return (Integer)this.weeklyVillageScores.getOrDefault(village, 0);
   }

   public void addVillageScore(String village, int amount) {
   }

   public Map<String, Integer> getWeeklyVillageScores() {
      return Collections.unmodifiableMap(this.weeklyVillageScores);
   }

   public void clearWeeklyScores() {
   }

   public long getLastWeeklyResetMs() {
      return this.lastWeeklyResetMs;
   }

   public void setLastWeeklyResetMs(long ms) {
   }

   public void resetAllPlayerWeekly() {
   }

   public void resetAllZonesToHome() {
   }

   public void setTerrainMap(int[] pixels) {
   }

   public int[] getTerrainMap() {
      return this.terrainMapPixels;
   }

   public boolean hasTerrainMap() {
      return this.terrainMapPixels != null && this.terrainMapPixels.length > 0;
   }

   private void initializeDefaultZones() {
   }

   public void ensureZonesInitialized() {
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.zones.clear();
      this.playerData.clear();
      this.weeklyVillageScores.clear();
      if (nbt.hasKey("zones")) {
         NBTTagList zoneList = nbt.getTagList("zones", 10);

         for(int i = 0; i < zoneList.tagCount(); ++i) {
            NBTTagCompound zoneNbt = zoneList.getCompoundTagAt(i);
            TerritoryZone zone = TerritoryZone.fromNBT(zoneNbt);
            if (zone.getZoneId() != null && !zone.getZoneId().isEmpty()) {
               this.zones.put(zone.getZoneId(), zone);
            }
         }
      }

      if (nbt.hasKey("playerData")) {
         NBTTagCompound pdTag = nbt.getCompoundTag("playerData");

         for(String uuidStr : pdTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               PerPlayerTerritoryData pd = PerPlayerTerritoryData.fromNBT(pdTag.getCompoundTag(uuidStr));
               this.playerData.put(playerId, pd);
            } catch (IllegalArgumentException var7) {
            }
         }
      }

      if (nbt.hasKey("weeklyVillageScores")) {
         NBTTagCompound scoresTag = nbt.getCompoundTag("weeklyVillageScores");

         for(String village : scoresTag.getKeySet()) {
            this.weeklyVillageScores.put(village, scoresTag.getInteger(village));
         }
      }

      if (nbt.hasKey("lastWeeklyResetMs")) {
         this.lastWeeklyResetMs = nbt.getLong("lastWeeklyResetMs");
      }

      if (nbt.hasKey("terrainMap")) {
         byte[] bytes = nbt.getByteArray("terrainMap");
         if (bytes.length >= 4) {
            int pixelCount = bytes.length / 4;
            this.terrainMapPixels = new int[pixelCount];

            for(int i = 0; i < pixelCount; ++i) {
               int offset = i * 4;
               this.terrainMapPixels[i] = (bytes[offset] & 255) << 24 | (bytes[offset + 1] & 255) << 16 | (bytes[offset + 2] & 255) << 8 | bytes[offset + 3] & 255;
            }
         }
      } else {
         this.terrainMapPixels = null;
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      return nbt;
   }
}
