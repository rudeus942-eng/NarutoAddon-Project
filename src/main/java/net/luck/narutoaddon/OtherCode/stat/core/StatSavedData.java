
package net.luck.narutoaddon.OtherCode.stat.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class StatSavedData extends WorldSavedData {
   public static final String DATA_NAME = "InfTsukStatData";
   private final Map<UUID, PlayerStatData> playerStats = new HashMap();

   public StatSavedData() {
      super("InfTsukStatData");
   }

   public StatSavedData(String name) {
      super(name);
   }

   public PlayerStatData getOrCreate(UUID playerId) {
      PlayerStatData data = (PlayerStatData)this.playerStats.get(playerId);
      if (data == null) {
         data = new PlayerStatData();
         this.playerStats.put(playerId, data);
      }

      return data;
   }

   public void resetPlayerStats(UUID playerId) {
      this.playerStats.put(playerId, new PlayerStatData());
      this.markDirty();
   }

   public Set<UUID> getAllPlayerUuids() {
      return Collections.unmodifiableSet(new HashSet(this.playerStats.keySet()));
   }

   public PlayerStatData get(UUID playerId) {
      return (PlayerStatData)this.playerStats.get(playerId);
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.playerStats.clear();
      NBTTagList playerList = nbt.getTagList("players", 10);

      for(int i = 0; i < playerList.tagCount(); ++i) {
         NBTTagCompound playerTag = playerList.getCompoundTagAt(i);
         UUID uuid = UUID.fromString(playerTag.getString("uuid"));
         PlayerStatData data = new PlayerStatData();
         data.readFromNBT(playerTag.getCompoundTag("stats"));
         this.playerStats.put(uuid, data);
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagList playerList = new NBTTagList();

      for(Map.Entry<UUID, PlayerStatData> entry : this.playerStats.entrySet()) {
         NBTTagCompound playerTag = new NBTTagCompound();
         playerTag.setString("uuid", ((UUID)entry.getKey()).toString());
         playerTag.setTag("stats", ((PlayerStatData)entry.getValue()).writeToNBT());
         playerList.appendTag(playerTag);
      }

      nbt.setTag("players", playerList);
      return nbt;
   }

   public static StatSavedData get(World world) {
      MapStorage storage = world.getMapStorage();
      StatSavedData data = (StatSavedData)storage.getOrLoadData(StatSavedData.class, "InfTsukStatData");
      if (data == null) {
         data = new StatSavedData();
         storage.setData("InfTsukStatData", data);
      }

      return data;
   }
}
