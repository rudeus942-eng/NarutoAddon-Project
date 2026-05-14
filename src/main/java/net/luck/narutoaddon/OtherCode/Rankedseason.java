
package net.luck.narutoaddon.OtherCode;

import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.text.SimpleDateFormat;
import java.util.*;

public class Rankedseason extends WorldSavedData {
   private static final String DATA_NAME = "RankedSeasonData";
   public static final int PLACEMENT_MATCHES_REQUIRED = 5;
   public static final int PLACEMENT_K_FACTOR = 50;
   public static final int MAX_PLACEMENT_ELO = 1900;
   public static final int MIN_PLACEMENT_ELO = 800;
   public static final double SOFT_RESET_PULL = (double)0.5F;
   public static final int SOFT_RESET_ANCHOR = 1200;
   public static final int MIN_GAMES_FOR_REWARDS = 10;
   private int currentSeason = 0;
   private String seasonId = "S0";
   private String seasonName = "The Beginning";
   private long seasonStartTimestamp = System.currentTimeMillis();
   private long seasonEndTimestamp = getDefaultBetaEndTime();
   private boolean isActive = true;
   private int totalMatchesPlayed = 0;
   private boolean autoTransition = true;
   private Map<String, PendingReward> pendingRewards = new HashMap();

   private static long getDefaultBetaEndTime() {
      Calendar cal = Calendar.getInstance();
      cal.set(1, 2026);
      cal.set(2, 0);
      cal.set(5, 24);
      cal.set(11, 13);
      cal.set(12, 0);
      cal.set(13, 0);
      cal.set(14, 0);
      return cal.getTimeInMillis();
   }

   public Rankedseason() {
      super("RankedSeasonData");
   }

   public Rankedseason(String name) {
      super(name);
   }

   public static int getTierRyo(String tier) {
      switch (tier) {
         case "Genin":
            return 800;
         case "Chunin":
            return 2000;
         case "Jonin":
            return 4000;
         case "S. Jonin":
            return 6000;
         case "Elite Jonin":
            return 10000;
         case "ANBU":
            return 16000;
         case "Kage":
            return 28000;
         case "Otsutsuki":
            return 48000;
         default:
            return 0;
      }
   }

   public static String getTierTitle(String tier) {
      switch (tier) {
         case "Jonin":
            return "§6[Jonin] ";
         case "Elite Jonin":
            return "§b[Elite] ";
         case "ANBU":
            return "§5[ANBU] ";
         case "Kage":
            return "§c[Kage] ";
         case "Otsutsuki":
            return "§4§l[OTSUTSUKI] ";
         default:
            return null;
      }
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.currentSeason = nbt.getInteger("currentSeason");
      this.seasonId = nbt.hasKey("seasonId") ? nbt.getString("seasonId") : (this.currentSeason == 0 ? "S0" : "S" + this.currentSeason);
      this.seasonName = nbt.getString("seasonName");
      this.seasonStartTimestamp = nbt.getLong("seasonStartTimestamp");
      this.seasonEndTimestamp = nbt.getLong("seasonEndTimestamp");
      this.isActive = nbt.getBoolean("isActive");
      this.totalMatchesPlayed = nbt.getInteger("totalMatchesPlayed");
      this.autoTransition = nbt.getBoolean("autoTransition");
      this.pendingRewards.clear();
      NBTTagList rewardsList = nbt.getTagList("pendingRewards", 10);

      for(int i = 0; i < rewardsList.tagCount(); ++i) {
         PendingReward reward = PendingReward.readFromNBT(rewardsList.getCompoundTagAt(i));
         String key = reward.uuid + "_" + reward.seasonNumber;
         this.pendingRewards.put(key, reward);
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      nbt.setInteger("currentSeason", this.currentSeason);
      nbt.setString("seasonId", this.seasonId);
      nbt.setString("seasonName", this.seasonName);
      nbt.setLong("seasonStartTimestamp", this.seasonStartTimestamp);
      nbt.setLong("seasonEndTimestamp", this.seasonEndTimestamp);
      nbt.setBoolean("isActive", this.isActive);
      nbt.setInteger("totalMatchesPlayed", this.totalMatchesPlayed);
      nbt.setBoolean("autoTransition", this.autoTransition);
      NBTTagList rewardsList = new NBTTagList();

      for(PendingReward reward : this.pendingRewards.values()) {
         rewardsList.appendTag(reward.writeToNBT());
      }

      nbt.setTag("pendingRewards", rewardsList);
      return nbt;
   }

   public static Rankedseason get(World world) {
      MapStorage storage = world.getMapStorage();
      Rankedseason instance = (Rankedseason)storage.getOrLoadData(Rankedseason.class, "RankedSeasonData");
      if (instance == null) {
         instance = new Rankedseason();
         storage.setData("RankedSeasonData", instance);
      }

      return instance;
   }

   public static int getCurrentSeason(World world) {
      Rankedseason data = get(world);
      return data.currentSeason;
   }

   public static SeasonInfo getSeasonInfo(World world) {
      Rankedseason data = get(world);
      SeasonInfo info = new SeasonInfo();
      info.seasonNumber = data.currentSeason;
      info.seasonId = data.seasonId;
      info.seasonName = data.seasonName;
      info.startTimestamp = data.seasonStartTimestamp;
      info.endTimestamp = data.seasonEndTimestamp;
      info.isActive = data.isActive;
      info.totalMatches = data.totalMatchesPlayed;
      info.autoTransition = data.autoTransition;
      long remaining = data.seasonEndTimestamp - System.currentTimeMillis();
      if (remaining <= 0L) {
         info.timeRemaining = "Season Ended";
         info.urgency = "ended";
      } else {
         long days = remaining / 86400000L;
         long hours = remaining % 86400000L / 3600000L;
         long minutes = remaining % 3600000L / 60000L;
         if (days > 0L) {
            info.timeRemaining = days + "d " + hours + "h";
         } else if (hours > 0L) {
            info.timeRemaining = hours + "h " + minutes + "m";
         } else {
            info.timeRemaining = minutes + "m";
         }

         if (days <= 1L) {
            info.urgency = "high";
         } else if (days <= 3L) {
            info.urgency = "medium";
         } else {
            info.urgency = "low";
         }
      }

      return info;
   }

   public static String getSeasonId(World world) {
      Rankedseason data = get(world);
      return data.seasonId;
   }

   public static boolean isSeasonActive(World world) {
      Rankedseason data = get(world);
      if (!data.isActive) {
         return false;
      } else {
         long now = System.currentTimeMillis();
         return now >= data.seasonStartTimestamp && now <= data.seasonEndTimestamp;
      }
   }

   public static boolean isInPlacements(World world, String uuid) {
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      if (storage == null) {
         return true;
      } else {
         Rankeddatastorage.PlayerRankedData playerData = storage.getPlayerData(uuid);
         return !playerData.isPlaced;
      }
   }

   public static PlacementProgress getPlacementProgress(World world, String uuid) {
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      PlacementProgress progress = new PlacementProgress();
      if (storage == null) {
         progress.isPlaced = false;
         progress.completed = 0;
         progress.required = 5;
         progress.remaining = 5;
         progress.progress = "0/5";
         progress.progressPercent = 0;
         return progress;
      } else {
         Rankeddatastorage.PlayerRankedData playerData = storage.getPlayerData(uuid);
         progress.isPlaced = playerData.isPlaced;
         progress.completed = playerData.placementGamesCompleted;
         progress.required = 5;
         progress.remaining = Math.max(0, 5 - playerData.placementGamesCompleted);
         progress.progress = playerData.placementGamesCompleted + "/" + 5;
         progress.progressPercent = Math.round((float)playerData.placementGamesCompleted / 5.0F * 100.0F);
         progress.provisionalElo = playerData.currentElo;
         Rankedtiers.RankInfo rankInfo = Rankedtiers.eloToRank(playerData.currentElo);
         progress.estimatedRank = rankInfo.fullName;
         return progress;
      }
   }

   public static int getPlacementKFactor(World world, String uuid) {
      if (isInPlacements(world, uuid)) {
         return 50;
      } else {
         Rankeddatastorage storage = Rankeddatastorage.get(world);
         if (storage == null) {
            return 42;
         } else {
            Rankeddatastorage.PlayerRankedData playerData = storage.getPlayerData(uuid);
            return Rankedelocore.getKFactor(playerData.gamesPlayed, playerData.currentElo);
         }
      }
   }

   public static PlacementResult finalizePlacements(World world, String uuid) {
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      PlacementResult result = new PlacementResult();
      if (storage == null) {
         return result;
      } else {
         Rankeddatastorage.PlayerRankedData playerData = storage.getPlayerData(uuid);
         int finalElo = playerData.currentElo;
         if (finalElo > 1900) {
            result.wasClampedHigh = true;
            finalElo = 1900;
         }

         if (finalElo < 800) {
            result.wasClampedLow = true;
            finalElo = 800;
         }

         playerData.currentElo = finalElo;
         playerData.hiddenMmr = finalElo;
         playerData.isPlaced = true;
         playerData.highestEloThisSeason = finalElo;
         storage.savePlayerData(uuid, playerData);
         Rankedtiers.RankInfo rankInfo = Rankedtiers.eloToRank(finalElo);
         result.finalElo = finalElo;
         result.finalRank = rankInfo.fullName;
         result.rankColor = rankInfo.color;
         return result;
      }
   }

   public static SeasonEndResult endSeason(World world) {
      Rankedseason seasonData = get(world);
      SeasonEndResult result = new SeasonEndResult();
      result.seasonNumber = seasonData.currentSeason;
      List<Rankedleaderboard.LeaderboardEntry> standings = Rankedleaderboard.buildLeaderboard(world);
      int rewardsPrepared = 0;

      for(int i = 0; i < standings.size(); ++i) {
         Rankedleaderboard.LeaderboardEntry entry = (Rankedleaderboard.LeaderboardEntry)standings.get(i);
         if (entry.gamesPlayed >= 10) {
            PendingReward reward = calculateSeasonReward(world, entry, i + 1, seasonData.currentSeason, seasonData.seasonId);
            if (reward.ryo > 0 || !reward.items.isEmpty()) {
               String key = entry.uuid + "_" + seasonData.currentSeason;
               seasonData.pendingRewards.put(key, reward);
               ++rewardsPrepared;
            }
         }
      }

      seasonData.isActive = false;
      seasonData.markDirty();
      Rankedleaderboard.invalidateLeaderboardCache();
      result.success = true;
      result.playersArchived = standings.size();
      result.rewardsPrepared = rewardsPrepared;
      result.message = seasonData.seasonId + " has ended!";
      return result;
   }

   private static PendingReward calculateSeasonReward(World world, Rankedleaderboard.LeaderboardEntry player, int position, int seasonNumber, String seasonId) {
      PendingReward reward = new PendingReward();
      reward.uuid = player.uuid;
      reward.name = player.name;
      reward.seasonNumber = seasonNumber;
      reward.position = position;
      reward.tier = player.tierName;
      reward.finalElo = player.elo;
      reward.storedAt = System.currentTimeMillis();
      reward.claimed = false;
      reward.title = null;
      reward.ryo = getTierRyo(player.tierName);
      reward.messages.add("Reached " + player.tierName + " Rank!");
      if (position == 1) {
         reward.ryo += 200000;
         reward.messages.add("§6§lSeason Champion!");
      } else if (position == 2) {
         reward.ryo += 100000;
         reward.messages.add("§5§lSeason Runner-Up!");
      } else if (position == 3) {
         reward.ryo += 60000;
         reward.messages.add("§c§lSeason Third Place!");
      } else if (position <= 10) {
         reward.ryo += 20000;
         reward.messages.add("Top 10 Finish!");
      }

      return reward;
   }

   public static SeasonStartResult startNewSeason(World world, String newSeasonName, long durationDays) {
      return startNewSeason(world, newSeasonName, (String)null, durationDays);
   }

   public static SeasonStartResult startNewSeason(World world, String newSeasonName, String newSeasonId, long durationDays) {
      Rankedseason seasonData = get(world);
      SeasonStartResult result = new SeasonStartResult();
      int newSeasonNumber = seasonData.currentSeason + 1;
      int playersReset = applySoftResetToAllPlayers(world, newSeasonNumber);
      seasonData.currentSeason = newSeasonNumber;
      seasonData.seasonId = newSeasonId != null ? newSeasonId : "S" + newSeasonNumber;
      seasonData.seasonName = newSeasonName != null ? newSeasonName : "Season " + newSeasonNumber;
      seasonData.seasonStartTimestamp = System.currentTimeMillis();
      seasonData.seasonEndTimestamp = System.currentTimeMillis() + durationDays * 24L * 60L * 60L * 1000L;
      seasonData.isActive = true;
      seasonData.totalMatchesPlayed = 0;
      seasonData.markDirty();
      Rankedleaderboard.refreshLeaderboardCache(world);
      result.success = true;
      result.seasonNumber = newSeasonNumber;
      result.seasonName = seasonData.seasonName;
      result.playersReset = playersReset;
      result.message = seasonData.seasonId + " has begun!";
      return result;
   }

   private static int applySoftResetToAllPlayers(World world, int newSeasonNumber) {
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      if (storage == null) {
         return 0;
      } else {
         List<Rankeddatastorage.PlayerRankedData> allPlayers = storage.getAllPlayers();
         int resetCount = 0;

         for(Rankeddatastorage.PlayerRankedData player : allPlayers) {
            int oldElo = player.currentElo;
            int newElo = (int)Math.round((double)oldElo + (double)(1200 - oldElo) * (double)0.5F);
            player.previousSeasonElo = oldElo;
            player.currentElo = newElo;
            player.hiddenMmr = newElo;
            player.isPlaced = false;
            player.placementGamesCompleted = 0;
            player.wins = 0;
            player.losses = 0;
            player.gamesPlayed = 0;
            player.currentWinStreak = 0;
            player.currentLossStreak = 0;
            player.highestEloThisSeason = newElo;
            player.demotionShields = 2;
            player.season = newSeasonNumber;
            storage.savePlayerData(player.uuid, player);
            ++resetCount;
         }

         return resetCount;
      }
   }

   public static List<PendingReward> getPendingRewards(World world, String uuid) {
      Rankedseason seasonData = get(world);
      List<PendingReward> rewards = new ArrayList();

      for(PendingReward reward : seasonData.pendingRewards.values()) {
         if (reward.uuid.equals(uuid) && !reward.claimed) {
            rewards.add(reward);
         }
      }

      return rewards;
   }

   public static boolean hasPendingRewards(World world, String uuid) {
      return !getPendingRewards(world, uuid).isEmpty();
   }

   public static boolean claimSeasonRewards(EntityPlayerMP player, int seasonNumber) {
      World world = player.world;
      Rankedseason seasonData = get(world);
      String uuid = player.getUniqueID().toString();
      String key = uuid + "_" + seasonNumber;
      PendingReward reward = (PendingReward)seasonData.pendingRewards.get(key);
      if (reward != null && !reward.claimed) {
         if (reward.ryo > 0) {
            giveRyoOptimal(player, reward.ryo);
         }

         player.sendMessage(new TextComponentString("§6§l===== SEASON REWARDS ====="));
         player.sendMessage(new TextComponentString("§7Season " + reward.seasonNumber + " Final Rank: §f#" + reward.position));
         player.sendMessage(new TextComponentString("§7Tier: " + reward.tier));
         player.sendMessage(new TextComponentString(""));

         for(String msg : reward.messages) {
            player.sendMessage(new TextComponentString(msg));
         }

         player.sendMessage(new TextComponentString(""));
         player.sendMessage(new TextComponentString("§eReceived: §f" + formatRyo(reward.ryo) + " Ryo"));
         player.sendMessage(new TextComponentString("§6§l=========================="));
         reward.claimed = true;
         seasonData.markDirty();
         return true;
      } else {
         player.sendMessage(new TextComponentString("§cNo rewards found for Season " + seasonNumber));
         return false;
      }
   }

   public static String getTitleColor(String title) {
      if (title.contains("Otsutsuki Deity")) {
         return "§6§l";
      } else if (title.contains("Otsutsuki Prodigy")) {
         return "§5§l";
      } else {
         return title.contains("Otsutsuki") ? "§c§l" : "§f";
      }
   }

   public static String getFormattedTitle(String title) {
      return getTitleColor(title) + title + "§r";
   }

   public static void notifyPendingRewards(EntityPlayerMP player) {
      World world = player.world;
      List<PendingReward> pending = getPendingRewards(world, player.getUniqueID().toString());
      if (!pending.isEmpty()) {
         player.sendMessage(new TextComponentString(""));
         player.sendMessage(new TextComponentString("§6§l★ You have unclaimed season rewards!"));

         for(PendingReward reward : pending) {
            player.sendMessage(new TextComponentString("§7  - Season " + reward.seasonNumber + " (Rank #" + reward.position + ")"));
         }

         player.sendMessage(new TextComponentString("§eOpen the Ranked Menu to claim them!"));
         player.sendMessage(new TextComponentString(""));
      }

   }

   public static void setSeasonName(World world, String name) {
      Rankedseason data = get(world);
      data.seasonName = name;
      data.markDirty();
   }

   public static String getSeasonName(World world) {
      Rankedseason data = get(world);
      return data.seasonName;
   }

   public static void setSeasonEndTimestamp(World world, long timestamp) {
      Rankedseason data = get(world);
      data.seasonEndTimestamp = timestamp;
      data.markDirty();
   }

   public static void setSeasonDuration(World world, int days) {
      Rankedseason data = get(world);
      data.seasonEndTimestamp = System.currentTimeMillis() + (long)days * 24L * 60L * 60L * 1000L;
      data.markDirty();
   }

   public static boolean setSeasonEndDate(World world, int month, int day, int year, int hour, int minute) {
      try {
         Calendar cal = Calendar.getInstance();
         cal.set(1, year);
         cal.set(2, month - 1);
         cal.set(5, day);
         cal.set(11, hour);
         cal.set(12, minute);
         cal.set(13, 0);
         cal.set(14, 0);
         long timestamp = cal.getTimeInMillis();
         if (timestamp <= System.currentTimeMillis()) {
            return false;
         } else {
            Rankedseason data = get(world);
            data.seasonEndTimestamp = timestamp;
            data.markDirty();
            return true;
         }
      } catch (Exception var10) {
         return false;
      }
   }

   public static String getFormattedEndDate(World world) {
      Rankedseason data = get(world);
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
      return sdf.format(new Date(data.seasonEndTimestamp));
   }

   public static String getFormattedStartDate(World world) {
      Rankedseason data = get(world);
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
      return sdf.format(new Date(data.seasonStartTimestamp));
   }

   public static void setSeasonActive(World world, boolean active) {
      Rankedseason data = get(world);
      data.isActive = active;
      data.markDirty();
   }

   public static void setSeasonId(World world, String id) {
      Rankedseason data = get(world);
      data.seasonId = id;
      data.markDirty();
   }

   public static void setAutoTransition(World world, boolean auto) {
      Rankedseason data = get(world);
      data.autoTransition = auto;
      data.markDirty();
   }

   public static boolean isAutoTransitionEnabled(World world) {
      Rankedseason data = get(world);
      return data.autoTransition;
   }

   public static void checkSeasonEnd(World world) {
      Rankedseason data = get(world);
      if (data.isActive) {
         long now = System.currentTimeMillis();
         if (now >= data.seasonEndTimestamp) {
            String oldSeasonId = data.seasonId;
            if (data.autoTransition) {
               endSeason(world);
               startNewSeason(world, (String)null, (String)null, 7L);
               broadcastSeasonTransition(world, oldSeasonId);
            } else {
               data.isActive = false;
               data.markDirty();
               broadcastSeasonEnded(world, oldSeasonId);
            }
         }

      }
   }

   private static void broadcastSeasonTransition(World world, String oldSeasonId) {
      Rankedseason data = get(world);

      for(EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
         player.sendMessage(new TextComponentString("§6§l==========================="));
         player.sendMessage(new TextComponentString("§c" + oldSeasonId + " has ended!"));
         player.sendMessage(new TextComponentString("§a" + data.seasonId + " has begun!"));
         player.sendMessage(new TextComponentString("§7Open the §eRanked Menu §7to claim your rewards!"));
         player.sendMessage(new TextComponentString("§6§l==========================="));
      }

   }

   private static void broadcastSeasonEnded(World world, String seasonId) {
      for(EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
         player.sendMessage(new TextComponentString("§6§l==========================="));
         player.sendMessage(new TextComponentString("§c§l" + seasonId + " has ended!"));
         player.sendMessage(new TextComponentString("§7Open the §eRanked Menu §7to claim your rewards!"));
         player.sendMessage(new TextComponentString("§7Waiting for admin to start next season..."));
         player.sendMessage(new TextComponentString("§6§l==========================="));
      }

   }

   private static void giveRyoOptimal(EntityPlayerMP player, int amount) {
      if (amount > 0) {
         RyoRewardHelper.grantRyoSilent(player, amount);
      }
   }

   public static void incrementSeasonMatches(World world) {
      Rankedseason data = get(world);
      ++data.totalMatchesPlayed;
      data.markDirty();
   }

   public static int clearLegacyItems(World world) {
      Rankedseason data = get(world);
      int cleared = 0;

      for(PendingReward reward : data.pendingRewards.values()) {
         if (!reward.items.isEmpty()) {
            System.out.println("[RankedSeason] Clearing " + reward.items.size() + " legacy items from reward for " + reward.name + " (season " + reward.seasonNumber + ")");
            reward.items.clear();
            ++cleared;
         }
      }

      if (cleared > 0) {
         data.markDirty();
      }

      return cleared;
   }

   public static int countPendingWithLegacyItems(World world) {
      Rankedseason data = get(world);
      int count = 0;

      for(PendingReward reward : data.pendingRewards.values()) {
         if (!reward.items.isEmpty()) {
            ++count;
         }
      }

      return count;
   }

   public static String formatRyo(int amount) {
      return String.format("%,d", amount);
   }

   public static void showSeasonStatus(EntityPlayerMP player) {
      World world = player.world;
      SeasonInfo info = getSeasonInfo(world);
      Rankedleaderboard.LeaderboardStats stats = Rankedleaderboard.getLeaderboardStats(world);
      String urgencyColor = "§a";
      if ("high".equals(info.urgency)) {
         urgencyColor = "§c";
      } else if ("medium".equals(info.urgency)) {
         urgencyColor = "§e";
      }

      player.sendMessage(new TextComponentString("§6§l===== SEASON STATUS ====="));
      player.sendMessage(new TextComponentString(info.getTitle()));
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§7Status: " + (info.isActive ? "§aActive" : "§cInactive")));
      player.sendMessage(new TextComponentString("§7Time Remaining: " + urgencyColor + info.timeRemaining));
      player.sendMessage(new TextComponentString("§7Total Matches: §f" + info.totalMatches));
      player.sendMessage(new TextComponentString("§7Ranked Players: §f" + stats.totalPlayers));
      player.sendMessage(new TextComponentString("§7Average ELO: §f" + stats.averageElo));
      player.sendMessage(new TextComponentString("§6§l========================="));
   }

   public static class SeasonInfo {
      public int seasonNumber;
      public String seasonId;
      public String seasonName;
      public long startTimestamp;
      public long endTimestamp;
      public boolean isActive;
      public int totalMatches;
      public String timeRemaining;
      public String urgency;
      public boolean autoTransition;

      public String getTitle() {
         return "§6" + this.seasonId + ": §f" + this.seasonName;
      }
   }

   public static class PlacementProgress {
      public boolean isPlaced;
      public int completed;
      public int required;
      public int remaining;
      public String progress;
      public int progressPercent;
      public int placementWins;
      public int placementLosses;
      public int provisionalElo;
      public String estimatedRank;
   }

   public static class PlacementResult {
      public int finalElo;
      public String finalRank;
      public String rankColor;
      public int placementWins;
      public int placementLosses;
      public boolean wasClampedHigh;
      public boolean wasClampedLow;
   }

   public static class SeasonEndResult {
      public boolean success;
      public int seasonNumber;
      public int playersArchived;
      public int rewardsPrepared;
      public String message;
   }

   public static class SeasonStartResult {
      public boolean success;
      public int seasonNumber;
      public String seasonName;
      public int playersReset;
      public String startDate;
      public String endDate;
      public String message;
   }

   public static class PendingReward {
      public String uuid;
      public String name;
      public int seasonNumber;
      public int position;
      public String tier;
      public int finalElo;
      public int ryo;
      public List<RewardItem> items = new ArrayList();
      public String title;
      public List<String> messages = new ArrayList();
      public boolean claimed;
      public long storedAt;

      public NBTTagCompound writeToNBT() {
         NBTTagCompound nbt = new NBTTagCompound();
         nbt.setString("uuid", this.uuid);
         nbt.setString("name", this.name != null ? this.name : "");
         nbt.setInteger("seasonNumber", this.seasonNumber);
         nbt.setInteger("position", this.position);
         nbt.setString("tier", this.tier != null ? this.tier : "");
         nbt.setInteger("finalElo", this.finalElo);
         nbt.setInteger("ryo", this.ryo);
         nbt.setString("title", this.title != null ? this.title : "");
         nbt.setBoolean("claimed", this.claimed);
         nbt.setLong("storedAt", this.storedAt);
         NBTTagList itemsList = new NBTTagList();

         for(RewardItem item : this.items) {
            itemsList.appendTag(item.writeToNBT());
         }

         nbt.setTag("items", itemsList);
         NBTTagList msgList = new NBTTagList();

         for(String msg : this.messages) {
            NBTTagCompound msgNbt = new NBTTagCompound();
            msgNbt.setString("msg", msg);
            msgList.appendTag(msgNbt);
         }

         nbt.setTag("messages", msgList);
         return nbt;
      }

      public static PendingReward readFromNBT(NBTTagCompound nbt) {
         PendingReward reward = new PendingReward();
         reward.uuid = nbt.getString("uuid");
         reward.name = nbt.getString("name");
         reward.seasonNumber = nbt.getInteger("seasonNumber");
         reward.position = nbt.getInteger("position");
         reward.tier = nbt.getString("tier");
         reward.finalElo = nbt.getInteger("finalElo");
         reward.ryo = nbt.getInteger("ryo");
         reward.title = nbt.getString("title");
         reward.claimed = nbt.getBoolean("claimed");
         reward.storedAt = nbt.getLong("storedAt");
         NBTTagList itemsList = nbt.getTagList("items", 10);

         for(int i = 0; i < itemsList.tagCount(); ++i) {
            reward.items.add(RewardItem.readFromNBT(itemsList.getCompoundTagAt(i)));
         }

         NBTTagList msgList = nbt.getTagList("messages", 10);

         for(int i = 0; i < msgList.tagCount(); ++i) {
            reward.messages.add(msgList.getCompoundTagAt(i).getString("msg"));
         }

         return reward;
      }
   }

   public static class RewardItem {
      public String itemId;
      public int amount;
      public int metadata;
      public NBTTagCompound itemNbt;
      public String displayName;

      public RewardItem(String itemId, int amount, String displayName) {
         this.itemId = itemId;
         this.amount = amount;
         this.metadata = 0;
         this.itemNbt = null;
         this.displayName = displayName;
      }

      public RewardItem(String itemId, int amount, int metadata, NBTTagCompound itemNbt, String displayName) {
         this.itemId = itemId;
         this.amount = amount;
         this.metadata = metadata;
         this.itemNbt = itemNbt != null ? itemNbt.copy() : null;
         this.displayName = displayName;
      }

      public static RewardItem fromConfiguredReward(Rankedrewardconfig.ConfiguredReward configReward) {
         return new RewardItem(configReward.itemId, configReward.amount, configReward.metadata, configReward.nbt, configReward.getDisplayName());
      }

      public NBTTagCompound writeToNBT() {
         NBTTagCompound nbt = new NBTTagCompound();
         nbt.setString("itemId", this.itemId);
         nbt.setInteger("amount", this.amount);
         nbt.setInteger("metadata", this.metadata);
         nbt.setString("displayName", this.displayName != null ? this.displayName : "");
         if (this.itemNbt != null && !this.itemNbt.isEmpty()) {
            nbt.setTag("itemNbt", this.itemNbt.copy());
         }

         return nbt;
      }

      public static RewardItem readFromNBT(NBTTagCompound nbt) {
         RewardItem item = new RewardItem(nbt.getString("itemId"), nbt.getInteger("amount"), nbt.getString("displayName"));
         item.metadata = nbt.getInteger("metadata");
         if (nbt.hasKey("itemNbt")) {
            item.itemNbt = nbt.getCompoundTag("itemNbt").copy();
         }

         return item;
      }

      public boolean hasNbt() {
         return this.itemNbt != null && !this.itemNbt.isEmpty();
      }
   }
}
