
package net.luck.narutoaddon.OtherCode.endgame;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class EndgameSavedData extends WorldSavedData {
   private static final String DATA_NAME = "InfTsukEndgameData";
   private final Map<UUID, Long> raidCooldowns = new HashMap();
   private final Map<UUID, Map<String, Long>> outpostCooldowns = new HashMap();
   private final Map<UUID, NBTTagCompound> savedBingoBoards = new HashMap();
   private final Map<UUID, Long> bingoResetTimes = new HashMap();
   private final Map<UUID, Map<String, Long>> defenseCompletionTimes = new HashMap();
   private final Map<UUID, Integer> totalOutpostClears = new HashMap();
   private final Map<UUID, Integer> totalBingoCompletions = new HashMap();
   private final Map<UUID, Integer> totalIncursionWaves = new HashMap();
   private final Map<UUID, Integer> totalDefenseWaves = new HashMap();
   private final Map<UUID, Map<String, Long>> fastestOutpostClears = new HashMap();
   private final Map<String, int[]> villagePeakHours = new HashMap();
   private final Map<String, Integer> defenseScheduleOverrides = new HashMap();
   private final Map<UUID, Integer> pveXp = new HashMap();
   private final Map<String, NBTTagCompound> savedOutpostInstances = new HashMap();
   private final Set<String> tailedBeastLoreDropped = new HashSet();

   public EndgameSavedData() {
      super("InfTsukEndgameData");
   }

   public EndgameSavedData(String name) {
      super(name);
   }

   public static EndgameSavedData get(World world) {
      MapStorage storage = world.getMapStorage();
      if (storage == null) {
         return new EndgameSavedData();
      } else {
         EndgameSavedData data = (EndgameSavedData)storage.getOrLoadData(EndgameSavedData.class, "InfTsukEndgameData");
         if (data == null) {
            data = new EndgameSavedData();
            storage.setData("InfTsukEndgameData", data);
         }

         return data;
      }
   }

   public void resetPlayerEndgame(UUID playerId) {
      this.raidCooldowns.remove(playerId);
      this.outpostCooldowns.remove(playerId);
      this.savedBingoBoards.remove(playerId);
      this.bingoResetTimes.remove(playerId);
      this.defenseCompletionTimes.remove(playerId);
      this.totalOutpostClears.remove(playerId);
      this.totalBingoCompletions.remove(playerId);
      this.totalIncursionWaves.remove(playerId);
      this.totalDefenseWaves.remove(playerId);
      this.fastestOutpostClears.remove(playerId);
      this.pveXp.remove(playerId);
      this.markDirty();
   }

   public Set<UUID> getAllPlayersWithAnyEndgameData() {
      Set<UUID> all = new HashSet();
      all.addAll(this.raidCooldowns.keySet());
      all.addAll(this.outpostCooldowns.keySet());
      all.addAll(this.savedBingoBoards.keySet());
      all.addAll(this.bingoResetTimes.keySet());
      all.addAll(this.defenseCompletionTimes.keySet());
      all.addAll(this.totalOutpostClears.keySet());
      all.addAll(this.totalBingoCompletions.keySet());
      all.addAll(this.totalIncursionWaves.keySet());
      all.addAll(this.totalDefenseWaves.keySet());
      all.addAll(this.fastestOutpostClears.keySet());
      all.addAll(this.pveXp.keySet());
      return Collections.unmodifiableSet(all);
   }

   public void setRaidCooldown(UUID player, long endTime) {
      this.raidCooldowns.put(player, endTime);
      this.markDirty();
   }

   public long getRaidCooldownEnd(UUID player) {
      return (Long)this.raidCooldowns.getOrDefault(player, 0L);
   }

   public boolean isRaidOnCooldown(UUID player) {
      return System.currentTimeMillis() < this.getRaidCooldownEnd(player);
   }

   public void setOutpostCooldown(UUID player, String key, long endTime) {
      ((Map)this.outpostCooldowns.computeIfAbsent(player, (k) -> new HashMap())).put(key, endTime);
      this.markDirty();
   }

   public long getOutpostCooldownEnd(UUID player, String key) {
      Map<String, Long> playerCooldowns = (Map)this.outpostCooldowns.get(player);
      return playerCooldowns == null ? 0L : (Long)playerCooldowns.getOrDefault(key, 0L);
   }

   public boolean isOutpostOnCooldown(UUID player, String key) {
      return System.currentTimeMillis() < this.getOutpostCooldownEnd(player, key);
   }

   public void saveBingoBoard(UUID player, NBTTagCompound boardNbt) {
      this.savedBingoBoards.put(player, boardNbt.copy());
      this.markDirty();
   }

   public NBTTagCompound getSavedBingoBoard(UUID player) {
      NBTTagCompound board = (NBTTagCompound)this.savedBingoBoards.get(player);
      return board != null ? board.copy() : null;
   }

   public void removeBingoBoard(UUID player) {
      this.savedBingoBoards.remove(player);
      this.markDirty();
   }

   public void setBingoResetTime(UUID player, long timestamp) {
      this.bingoResetTimes.put(player, timestamp);
      this.markDirty();
   }

   public long getBingoResetTime(UUID player) {
      return (Long)this.bingoResetTimes.getOrDefault(player, 0L);
   }

   public void setDefenseCompletionTime(UUID player, String villageName, long timestamp) {
      ((Map)this.defenseCompletionTimes.computeIfAbsent(player, (k) -> new HashMap())).put(villageName, timestamp);
      this.markDirty();
   }

   public long getDefenseCompletionTime(UUID player, String villageName) {
      Map<String, Long> playerTimes = (Map)this.defenseCompletionTimes.get(player);
      return playerTimes == null ? 0L : (Long)playerTimes.getOrDefault(villageName, 0L);
   }

   public boolean hasCompletedDefenseToday(UUID player, String villageName) {
      long lastCompletion = this.getDefenseCompletionTime(player, villageName);
      if (lastCompletion == 0L) {
         return false;
      } else {
         Calendar lastCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
         lastCal.setTimeInMillis(lastCompletion);
         Calendar nowCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
         return lastCal.get(1) == nowCal.get(1) && lastCal.get(6) == nowCal.get(6);
      }
   }

   public void incrementLeaderboard(String category, UUID player) {
      Map<UUID, Integer> board = this.getLeaderboardMap(category);
      if (board != null) {
         board.put(player, (Integer)board.getOrDefault(player, 0) + 1);
         this.markDirty();
      }

   }

   public int getLeaderboardScore(String category, UUID player) {
      Map<UUID, Integer> board = this.getLeaderboardMap(category);
      return board == null ? 0 : (Integer)board.getOrDefault(player, 0);
   }

   public void recordFastestClear(UUID player, String outpostId, long timeMs) {
      Map<String, Long> playerTimes = (Map)this.fastestOutpostClears.computeIfAbsent(player, (k) -> new HashMap());
      Long existing = (Long)playerTimes.get(outpostId);
      if (existing == null || timeMs < existing) {
         playerTimes.put(outpostId, timeMs);
         this.markDirty();
      }

   }

   public long getFastestClear(UUID player, String outpostId) {
      Map<String, Long> playerTimes = (Map)this.fastestOutpostClears.get(player);
      return playerTimes == null ? -1L : (Long)playerTimes.getOrDefault(outpostId, -1L);
   }

   public List<Entry<UUID, Integer>> getTopPlayers(String category, int n) {
      Map<UUID, Integer> board = this.getLeaderboardMap(category);
      return board != null && !board.isEmpty() ? (List)board.entrySet().stream().sorted(Entry.comparingByValue().reversed()).limit((long)n).collect(Collectors.toList()) : Collections.emptyList();
   }

   private Map<UUID, Integer> getLeaderboardMap(String category) {
      switch (category) {
         case "outpostClears":
            return this.totalOutpostClears;
         case "bingoCompletions":
            return this.totalBingoCompletions;
         case "incursionWaves":
            return this.totalIncursionWaves;
         case "defenseWaves":
            return this.totalDefenseWaves;
         default:
            return null;
      }
   }

   public void updatePeakHours(String village, int hour, int playerCount) {
      if (hour >= 0 && hour <= 23) {
         int[] hours = (int[])this.villagePeakHours.computeIfAbsent(village, (k) -> new int[24]);
         hours[hour] = playerCount;
         this.markDirty();
      }
   }

   public int getPeakHour(String village) {
      int[] hours = (int[])this.villagePeakHours.get(village);
      if (hours == null) {
         return 12;
      } else {
         int peakHour = 0;
         int peakCount = hours[0];

         for(int i = 1; i < 24; ++i) {
            if (hours[i] > peakCount) {
               peakCount = hours[i];
               peakHour = i;
            }
         }

         return peakHour;
      }
   }

   public int[] getPeakHoursData(String village) {
      int[] hours = (int[])this.villagePeakHours.get(village);
      return hours != null ? Arrays.copyOf(hours, 24) : new int[24];
   }

   public Integer getDefenseScheduleOverride(String village) {
      return (Integer)this.defenseScheduleOverrides.get(village);
   }

   public void setDefenseScheduleOverride(String village, int hour) {
      this.defenseScheduleOverrides.put(village, hour);
      this.markDirty();
   }

   public void removeDefenseScheduleOverride(String village) {
      this.defenseScheduleOverrides.remove(village);
      this.markDirty();
   }

   public int getPveXp(UUID player) {
      return (Integer)this.pveXp.getOrDefault(player, 0);
   }

   public int addPveXp(UUID player, int amount) {
      int current = (Integer)this.pveXp.getOrDefault(player, 0);
      int newTotal = current + amount;
      this.pveXp.put(player, newTotal);
      this.markDirty();
      return newTotal;
   }

   public void setPveXp(UUID player, int amount) {
      this.pveXp.put(player, Math.max(0, amount));
      this.markDirty();
   }

   public PveRank getPveRank(UUID player) {
      return PveRank.fromXp(this.getPveXp(player));
   }

   public boolean hasTailedBeastLoreDropped(int beastNumber) {
      return this.tailedBeastLoreDropped.contains("tb_lore_" + beastNumber);
   }

   public void setTailedBeastLoreDropped(int beastNumber) {
      this.tailedBeastLoreDropped.add("tb_lore_" + beastNumber);
      this.markDirty();
   }

   public void saveOutpostInstance(String key, NBTTagCompound instanceNbt) {
      this.savedOutpostInstances.put(key, instanceNbt.copy());
      this.markDirty();
   }

   public Map<String, NBTTagCompound> getSavedOutpostInstances() {
      Map<String, NBTTagCompound> result = new HashMap();

      for(Entry<String, NBTTagCompound> entry : this.savedOutpostInstances.entrySet()) {
         result.put(entry.getKey(), ((NBTTagCompound)entry.getValue()).copy());
      }

      return result;
   }

   public void clearSavedOutpostInstance(String key) {
      this.savedOutpostInstances.remove(key);
      this.markDirty();
   }

   public void clearAllSavedOutpostInstances() {
      this.savedOutpostInstances.clear();
      this.markDirty();
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.raidCooldowns.clear();
      this.outpostCooldowns.clear();
      this.savedBingoBoards.clear();
      this.bingoResetTimes.clear();
      this.defenseCompletionTimes.clear();
      this.totalOutpostClears.clear();
      this.totalBingoCompletions.clear();
      this.totalIncursionWaves.clear();
      this.totalDefenseWaves.clear();
      this.fastestOutpostClears.clear();
      this.villagePeakHours.clear();
      this.defenseScheduleOverrides.clear();
      this.pveXp.clear();
      this.savedOutpostInstances.clear();
      this.tailedBeastLoreDropped.clear();
      if (nbt.hasKey("raidCooldowns")) {
         NBTTagCompound tag = nbt.getCompoundTag("raidCooldowns");

         for(String uuidStr : tag.getKeySet()) {
            try {
               this.raidCooldowns.put(UUID.fromString(uuidStr), tag.getLong(uuidStr));
            } catch (IllegalArgumentException var12) {
            }
         }
      }

      if (nbt.hasKey("outpostCooldowns")) {
         NBTTagCompound tag = nbt.getCompoundTag("outpostCooldowns");

         for(String uuidStr : tag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagCompound inner = tag.getCompoundTag(uuidStr);
               Map<String, Long> cooldowns = new HashMap();

               for(String key : inner.getKeySet()) {
                  cooldowns.put(key, inner.getLong(key));
               }

               if (!cooldowns.isEmpty()) {
                  this.outpostCooldowns.put(playerId, cooldowns);
               }
            } catch (IllegalArgumentException var15) {
            }
         }
      }

      if (nbt.hasKey("bingoBoards")) {
         NBTTagCompound tag = nbt.getCompoundTag("bingoBoards");

         for(String uuidStr : tag.getKeySet()) {
            try {
               this.savedBingoBoards.put(UUID.fromString(uuidStr), tag.getCompoundTag(uuidStr).copy());
            } catch (IllegalArgumentException var11) {
            }
         }
      }

      if (nbt.hasKey("bingoResetTimes")) {
         NBTTagCompound tag = nbt.getCompoundTag("bingoResetTimes");

         for(String uuidStr : tag.getKeySet()) {
            try {
               this.bingoResetTimes.put(UUID.fromString(uuidStr), tag.getLong(uuidStr));
            } catch (IllegalArgumentException var10) {
            }
         }
      }

      if (nbt.hasKey("defenseCompletionTimes")) {
         NBTTagCompound tag = nbt.getCompoundTag("defenseCompletionTimes");

         for(String uuidStr : tag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagCompound inner = tag.getCompoundTag(uuidStr);
               Map<String, Long> times = new HashMap();

               for(String village : inner.getKeySet()) {
                  times.put(village, inner.getLong(village));
               }

               if (!times.isEmpty()) {
                  this.defenseCompletionTimes.put(playerId, times);
               }
            } catch (IllegalArgumentException var14) {
            }
         }
      }

      this.readLeaderboardMap(nbt, "totalOutpostClears", this.totalOutpostClears);
      this.readLeaderboardMap(nbt, "totalBingoCompletions", this.totalBingoCompletions);
      this.readLeaderboardMap(nbt, "totalIncursionWaves", this.totalIncursionWaves);
      this.readLeaderboardMap(nbt, "totalDefenseWaves", this.totalDefenseWaves);
      if (nbt.hasKey("fastestOutpostClears")) {
         NBTTagCompound tag = nbt.getCompoundTag("fastestOutpostClears");

         for(String uuidStr : tag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagCompound inner = tag.getCompoundTag(uuidStr);
               Map<String, Long> times = new HashMap();

               for(String outpostId : inner.getKeySet()) {
                  times.put(outpostId, inner.getLong(outpostId));
               }

               if (!times.isEmpty()) {
                  this.fastestOutpostClears.put(playerId, times);
               }
            } catch (IllegalArgumentException var13) {
            }
         }
      }

      if (nbt.hasKey("villagePeakHours")) {
         NBTTagCompound tag = nbt.getCompoundTag("villagePeakHours");

         for(String village : tag.getKeySet()) {
            int[] hours = tag.getIntArray(village);
            if (hours.length == 24) {
               this.villagePeakHours.put(village, Arrays.copyOf(hours, 24));
            } else {
               int[] fixed = new int[24];
               System.arraycopy(hours, 0, fixed, 0, Math.min(hours.length, 24));
               this.villagePeakHours.put(village, fixed);
            }
         }
      }

      if (nbt.hasKey("defenseScheduleOverrides")) {
         NBTTagCompound tag = nbt.getCompoundTag("defenseScheduleOverrides");

         for(String village : tag.getKeySet()) {
            this.defenseScheduleOverrides.put(village, tag.getInteger(village));
         }
      }

      this.readLeaderboardMap(nbt, "pveXp", this.pveXp);
      if (nbt.hasKey("outpostInstances")) {
         NBTTagList instanceList = nbt.getTagList("outpostInstances", 10);

         for(int i = 0; i < instanceList.tagCount(); ++i) {
            NBTTagCompound instanceNbt = instanceList.getCompoundTagAt(i);
            String key = instanceNbt.getString("instanceKey");
            if (!key.isEmpty()) {
               this.savedOutpostInstances.put(key, instanceNbt.copy());
            }
         }
      }

      if (nbt.hasKey("tailedBeastLoreDropped")) {
         NBTTagCompound tbTag = nbt.getCompoundTag("tailedBeastLoreDropped");

         for(String key : tbTag.getKeySet()) {
            if (tbTag.getBoolean(key)) {
               this.tailedBeastLoreDropped.add(key);
            }
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagCompound raidTag = new NBTTagCompound();

      for(Entry<UUID, Long> entry : this.raidCooldowns.entrySet()) {
         raidTag.setLong(((UUID)entry.getKey()).toString(), (Long)entry.getValue());
      }

      nbt.setTag("raidCooldowns", raidTag);
      NBTTagCompound outpostTag = new NBTTagCompound();

      for(Entry<UUID, Map<String, Long>> entry : this.outpostCooldowns.entrySet()) {
         NBTTagCompound inner = new NBTTagCompound();

         for(Entry<String, Long> cd : ((Map)entry.getValue()).entrySet()) {
            inner.setLong((String)cd.getKey(), (Long)cd.getValue());
         }

         outpostTag.setTag(((UUID)entry.getKey()).toString(), inner);
      }

      nbt.setTag("outpostCooldowns", outpostTag);
      NBTTagCompound bingoTag = new NBTTagCompound();

      for(Entry<UUID, NBTTagCompound> entry : this.savedBingoBoards.entrySet()) {
         bingoTag.setTag(((UUID)entry.getKey()).toString(), ((NBTTagCompound)entry.getValue()).copy());
      }

      nbt.setTag("bingoBoards", bingoTag);
      NBTTagCompound bingoResetTag = new NBTTagCompound();

      for(Entry<UUID, Long> entry : this.bingoResetTimes.entrySet()) {
         bingoResetTag.setLong(((UUID)entry.getKey()).toString(), (Long)entry.getValue());
      }

      nbt.setTag("bingoResetTimes", bingoResetTag);
      NBTTagCompound defenseTag = new NBTTagCompound();

      for(Entry<UUID, Map<String, Long>> entry : this.defenseCompletionTimes.entrySet()) {
         NBTTagCompound inner = new NBTTagCompound();

         for(Entry<String, Long> ve : ((Map)entry.getValue()).entrySet()) {
            inner.setLong((String)ve.getKey(), (Long)ve.getValue());
         }

         defenseTag.setTag(((UUID)entry.getKey()).toString(), inner);
      }

      nbt.setTag("defenseCompletionTimes", defenseTag);
      this.writeLeaderboardMap(nbt, "totalOutpostClears", this.totalOutpostClears);
      this.writeLeaderboardMap(nbt, "totalBingoCompletions", this.totalBingoCompletions);
      this.writeLeaderboardMap(nbt, "totalIncursionWaves", this.totalIncursionWaves);
      this.writeLeaderboardMap(nbt, "totalDefenseWaves", this.totalDefenseWaves);
      NBTTagCompound fastestTag = new NBTTagCompound();

      for(Entry<UUID, Map<String, Long>> entry : this.fastestOutpostClears.entrySet()) {
         NBTTagCompound inner = new NBTTagCompound();

         for(Entry<String, Long> te : ((Map)entry.getValue()).entrySet()) {
            inner.setLong((String)te.getKey(), (Long)te.getValue());
         }

         fastestTag.setTag(((UUID)entry.getKey()).toString(), inner);
      }

      nbt.setTag("fastestOutpostClears", fastestTag);
      NBTTagCompound peakTag = new NBTTagCompound();

      for(Entry<String, int[]> entry : this.villagePeakHours.entrySet()) {
         peakTag.setIntArray((String)entry.getKey(), (int[])entry.getValue());
      }

      nbt.setTag("villagePeakHours", peakTag);
      NBTTagCompound scheduleTag = new NBTTagCompound();

      for(Entry<String, Integer> entry : this.defenseScheduleOverrides.entrySet()) {
         scheduleTag.setInteger((String)entry.getKey(), (Integer)entry.getValue());
      }

      nbt.setTag("defenseScheduleOverrides", scheduleTag);
      this.writeLeaderboardMap(nbt, "pveXp", this.pveXp);
      NBTTagList instanceList = new NBTTagList();

      for(Entry<String, NBTTagCompound> entry : this.savedOutpostInstances.entrySet()) {
         NBTTagCompound instanceNbt = ((NBTTagCompound)entry.getValue()).copy();
         instanceNbt.setString("instanceKey", (String)entry.getKey());
         instanceList.appendTag(instanceNbt);
      }

      nbt.setTag("outpostInstances", instanceList);
      NBTTagCompound tbTag = new NBTTagCompound();

      for(String key : this.tailedBeastLoreDropped) {
         tbTag.setBoolean(key, true);
      }

      nbt.setTag("tailedBeastLoreDropped", tbTag);
      return nbt;
   }

   private void readLeaderboardMap(NBTTagCompound nbt, String key, Map<UUID, Integer> target) {
      if (nbt.hasKey(key)) {
         NBTTagCompound tag = nbt.getCompoundTag(key);

         for(String uuidStr : tag.getKeySet()) {
            try {
               target.put(UUID.fromString(uuidStr), tag.getInteger(uuidStr));
            } catch (IllegalArgumentException var8) {
            }
         }
      }

   }

   private void writeLeaderboardMap(NBTTagCompound nbt, String key, Map<UUID, Integer> source) {
      NBTTagCompound tag = new NBTTagCompound();

      for(Entry<UUID, Integer> entry : source.entrySet()) {
         tag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag(key, tag);
   }
}
