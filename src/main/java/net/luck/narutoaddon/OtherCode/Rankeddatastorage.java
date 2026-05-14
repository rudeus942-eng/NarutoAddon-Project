
package net.luck.narutoaddon.OtherCode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class Rankeddatastorage extends WorldSavedData {
   private static final String DATA_NAME = "RankedPlayerData";
   public static final int DEFAULT_ELO = 1200;
   public static final int DEFAULT_MMR = 1200;
   public static final int DEFAULT_LP = 0;
   public static final int PLACEMENT_GAMES_REQUIRED = 5;
   public static final int MAX_DEMOTION_SHIELDS = 2;
   private Map<String, PlayerRankedData> playerData = new HashMap();

   public Rankeddatastorage() {
      super("RankedPlayerData");
   }

   public Rankeddatastorage(String name) {
      super(name);
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.playerData.clear();
      NBTTagCompound playersNBT = nbt.getCompoundTag("players");

      for(String uuid : playersNBT.getKeySet()) {
         PlayerRankedData data = new PlayerRankedData();
         data.readFromNBT(playersNBT.getCompoundTag(uuid));
         this.playerData.put(uuid, data);
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagCompound playersNBT = new NBTTagCompound();

      for(Map.Entry<String, PlayerRankedData> entry : this.playerData.entrySet()) {
         playersNBT.setTag((String)entry.getKey(), ((PlayerRankedData)entry.getValue()).writeToNBT());
      }

      nbt.setTag("players", playersNBT);
      return nbt;
   }

   public static Rankeddatastorage get(World world) {
      if (world.isRemote) {
         return null;
      } else {
         WorldSavedData data = world.getMapStorage().getOrLoadData(Rankeddatastorage.class, "RankedPlayerData");
         if (data == null) {
            data = new Rankeddatastorage();
            world.getMapStorage().setData("RankedPlayerData", data);
         }

         return (Rankeddatastorage)data;
      }
   }

   public void resetPlayer(String uuid) {
      if (this.playerData.remove(uuid) != null) {
         this.markDirty();
      }

   }

   public Set<String> getAllPlayerUuids() {
      return Collections.unmodifiableSet(new HashSet(this.playerData.keySet()));
   }

   public PlayerRankedData getPlayerData(String uuid) {
      PlayerRankedData data = (PlayerRankedData)this.playerData.get(uuid);
      if (data == null) {
         data = new PlayerRankedData(uuid);
         this.playerData.put(uuid, data);
         this.markDirty();
      }

      if (!data.isPlaced && data.gamesPlayed > 0 && data.placementGamesCompleted < data.gamesPlayed) {
         data.placementGamesCompleted = Math.min(data.gamesPlayed, 5);
         this.markDirty();
      }

      if (!data.isPlaced && data.placementGamesCompleted >= 5) {
         data.isPlaced = true;
         data.highestEloThisSeason = data.currentElo;
         this.markDirty();
      }

      return data;
   }

   public PlayerRankedData getPlayerData(EntityPlayer player) {
      String uuid = player.getUniqueID().toString();
      PlayerRankedData data = this.getPlayerData(uuid);
      data.playerName = player.getName();
      return data;
   }

   public void savePlayerData(String uuid, PlayerRankedData data) {
      this.playerData.put(uuid, data);
      this.markDirty();
   }

   public void deletePlayerData(String uuid) {
      this.playerData.remove(uuid);
      this.markDirty();
   }

   public Map<String, PlayerRankedData> getAllPlayerData() {
      return new HashMap(this.playerData);
   }

   public List<PlayerRankedData> getAllPlayers() {
      return new ArrayList(this.playerData.values());
   }

   public int getPlayerCount() {
      return this.playerData.size();
   }

   public void initializePlayer(String uuid, String name) {
      PlayerRankedData data = new PlayerRankedData(uuid);
      data.playerName = name;
      data.currentElo = 1200;
      data.hiddenMmr = 1200;
      data.peakElo = 1200;
      data.highestEloThisSeason = 1200;
      this.playerData.put(uuid, data);
      this.markDirty();
   }

   public static PlayerRankedData getOrCreatePlayerData(World world, EntityPlayer player) {
      Rankeddatastorage storage = get(world);
      return storage == null ? new PlayerRankedData(player.getUniqueID().toString()) : storage.getPlayerData(player);
   }

   public static void savePlayerData(World world, PlayerRankedData data) {
      Rankeddatastorage storage = get(world);
      if (storage != null) {
         storage.savePlayerData(data.uuid, data);
      }

   }

   public static void updatePlayerAfterMatch(World world, String uuid, boolean won, int eloChange, int newElo) {
      Rankeddatastorage storage = get(world);
      if (storage != null) {
         PlayerRankedData data = storage.getPlayerData(uuid);
         data.currentElo = newElo;
         data.hiddenMmr = newElo;
         ++data.gamesPlayed;
         data.lastMatchTimestamp = System.currentTimeMillis();
         if (!data.isPlaced) {
            ++data.placementGamesCompleted;
         }

         if (won) {
            ++data.wins;
            ++data.currentWinStreak;
            data.currentLossStreak = 0;
            if (data.currentWinStreak > data.bestWinStreak) {
               data.bestWinStreak = data.currentWinStreak;
            }

            if (newElo > data.highestEloThisSeason) {
               data.highestEloThisSeason = newElo;
            }

            if (newElo > data.peakElo) {
               data.peakElo = newElo;
            }
         } else {
            ++data.losses;
            ++data.currentLossStreak;
            data.currentWinStreak = 0;
         }

         Rankedelocore.RankTier tier = Rankedelocore.getRankTier(newElo);
         data.visibleRank = tier.name;
         storage.savePlayerData(uuid, data);
      }
   }

   public static class PlayerRankedData {
      public String uuid;
      public int currentElo = 1200;
      public int hiddenMmr = 1200;
      public String visibleRank = "Unranked";
      public int lp = 0;
      public int wins = 0;
      public int losses = 0;
      public int gamesPlayed = 0;
      public int placementGamesCompleted = 0;
      public boolean isPlaced = false;
      public int currentWinStreak = 0;
      public int currentLossStreak = 0;
      public int bestWinStreak = 0;
      public int highestEloThisSeason = 1200;
      public int peakElo = 1200;
      public int previousSeasonElo = 1200;
      public long lastMatchTimestamp = 0L;
      public int demotionShields = 2;
      public int season = 1;
      public long createdTimestamp = System.currentTimeMillis();
      public String playerName = "";
      public int dailyWinsToday = 0;
      public boolean dailyTaskCompleted = false;
      public boolean dailyTaskClaimed = false;
      public long dailyTaskLastReset = 0L;
      public Set<String> claimedRankRewards = new HashSet();
      public Set<String> collectedTitles = new HashSet();
      public String activeTitle = "";

      public PlayerRankedData() {
      }

      public PlayerRankedData(String uuid) {
         this.uuid = uuid;
         this.createdTimestamp = System.currentTimeMillis();
      }

      public NBTTagCompound writeToNBT() {
         NBTTagCompound nbt = new NBTTagCompound();
         nbt.setString("uuid", this.uuid != null ? this.uuid : "");
         nbt.setInteger("currentElo", this.currentElo);
         nbt.setInteger("hiddenMmr", this.hiddenMmr);
         nbt.setString("visibleRank", this.visibleRank != null ? this.visibleRank : "Unranked");
         nbt.setInteger("lp", this.lp);
         nbt.setInteger("wins", this.wins);
         nbt.setInteger("losses", this.losses);
         nbt.setInteger("gamesPlayed", this.gamesPlayed);
         nbt.setInteger("placementGamesCompleted", this.placementGamesCompleted);
         nbt.setBoolean("isPlaced", this.isPlaced);
         nbt.setInteger("currentWinStreak", this.currentWinStreak);
         nbt.setInteger("currentLossStreak", this.currentLossStreak);
         nbt.setInteger("highestEloThisSeason", this.highestEloThisSeason);
         nbt.setLong("lastMatchTimestamp", this.lastMatchTimestamp);
         nbt.setInteger("demotionShields", this.demotionShields);
         nbt.setInteger("season", this.season);
         nbt.setLong("createdTimestamp", this.createdTimestamp);
         nbt.setString("playerName", this.playerName != null ? this.playerName : "");
         nbt.setInteger("bestWinStreak", this.bestWinStreak);
         nbt.setInteger("peakElo", this.peakElo);
         nbt.setInteger("previousSeasonElo", this.previousSeasonElo);
         nbt.setInteger("dailyWinsToday", this.dailyWinsToday);
         nbt.setBoolean("dailyTaskCompleted", this.dailyTaskCompleted);
         nbt.setBoolean("dailyTaskClaimed", this.dailyTaskClaimed);
         nbt.setLong("dailyTaskLastReset", this.dailyTaskLastReset);
         NBTTagList claimedList = new NBTTagList();
         if (this.claimedRankRewards != null) {
            for(String rank : this.claimedRankRewards) {
               claimedList.appendTag(new NBTTagString(rank));
            }
         }

         nbt.setTag("claimedRankRewards", claimedList);
         NBTTagList titlesList = new NBTTagList();
         if (this.collectedTitles != null) {
            for(String title : this.collectedTitles) {
               titlesList.appendTag(new NBTTagString(title));
            }
         }

         nbt.setTag("collectedTitles", titlesList);
         nbt.setString("activeTitle", this.activeTitle != null ? this.activeTitle : "");
         return nbt;
      }

      public void readFromNBT(NBTTagCompound nbt) {
         this.uuid = nbt.getString("uuid");
         this.currentElo = nbt.getInteger("currentElo");
         this.hiddenMmr = nbt.getInteger("hiddenMmr");
         this.visibleRank = nbt.getString("visibleRank");
         this.lp = nbt.getInteger("lp");
         this.wins = nbt.getInteger("wins");
         this.losses = nbt.getInteger("losses");
         this.gamesPlayed = nbt.getInteger("gamesPlayed");
         this.placementGamesCompleted = nbt.getInteger("placementGamesCompleted");
         this.isPlaced = nbt.getBoolean("isPlaced");
         this.currentWinStreak = nbt.getInteger("currentWinStreak");
         this.currentLossStreak = nbt.getInteger("currentLossStreak");
         this.highestEloThisSeason = nbt.getInteger("highestEloThisSeason");
         this.lastMatchTimestamp = nbt.getLong("lastMatchTimestamp");
         this.demotionShields = nbt.getInteger("demotionShields");
         this.season = nbt.getInteger("season");
         this.createdTimestamp = nbt.getLong("createdTimestamp");
         this.playerName = nbt.getString("playerName");
         this.bestWinStreak = nbt.getInteger("bestWinStreak");
         this.peakElo = nbt.getInteger("peakElo");
         this.previousSeasonElo = nbt.getInteger("previousSeasonElo");
         this.dailyWinsToday = nbt.getInteger("dailyWinsToday");
         this.dailyTaskCompleted = nbt.getBoolean("dailyTaskCompleted");
         this.dailyTaskClaimed = nbt.getBoolean("dailyTaskClaimed");
         this.dailyTaskLastReset = nbt.getLong("dailyTaskLastReset");
         this.claimedRankRewards = new HashSet();
         NBTTagList claimedList = nbt.getTagList("claimedRankRewards", 8);

         for(int i = 0; i < claimedList.tagCount(); ++i) {
            this.claimedRankRewards.add(claimedList.getStringTagAt(i));
         }

         this.collectedTitles = new HashSet();
         NBTTagList titlesList = nbt.getTagList("collectedTitles", 8);

         for(int i = 0; i < titlesList.tagCount(); ++i) {
            this.collectedTitles.add(titlesList.getStringTagAt(i));
         }

         this.activeTitle = nbt.getString("activeTitle");
      }

      public int getWinRate() {
         return this.gamesPlayed == 0 ? 0 : (int)Math.round((double)this.wins * (double)100.0F / (double)this.gamesPlayed);
      }
   }
}
