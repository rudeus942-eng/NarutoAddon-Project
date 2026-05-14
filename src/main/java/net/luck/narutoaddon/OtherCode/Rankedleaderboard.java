
package net.luck.narutoaddon.OtherCode;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Rankedleaderboard {
   public static final long CACHE_DURATION = 300000L;
   public static final long TOP_10_CACHE_DURATION = 30000L;
   public static final int PLAYERS_PER_PAGE = 10;
   public static final int MAX_LEADERBOARD_SIZE = 100;
   private static List<LeaderboardEntry> cachedLeaderboard = null;
   private static long cacheTimestamp = 0L;
   private static List<LeaderboardEntry> cachedTop10 = null;
   private static long top10CacheTimestamp = 0L;

   public static List<LeaderboardEntry> getTopPlayers(World world, int count, boolean useCache) {
      if (useCache) {
         List<LeaderboardEntry> cached = getCachedLeaderboard();
         if (cached != null) {
            return cached.subList(0, Math.min(count, cached.size()));
         }
      }

      List<LeaderboardEntry> leaderboard = buildLeaderboard(world);
      cacheLeaderboard(leaderboard);
      return leaderboard.subList(0, Math.min(count, leaderboard.size()));
   }

   public static List<LeaderboardEntry> buildLeaderboard(World world) {
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      if (storage == null) {
         return new ArrayList();
      } else {
         List<Rankeddatastorage.PlayerRankedData> allPlayers = storage.getAllPlayers();
         List<LeaderboardEntry> leaderboard = new ArrayList();

         for(Rankeddatastorage.PlayerRankedData player : allPlayers) {
            if (player.isPlaced) {
               LeaderboardEntry entry = new LeaderboardEntry();
               entry.uuid = player.uuid;
               entry.name = player.playerName != null ? player.playerName : "Unknown";
               entry.elo = player.currentElo;
               entry.wins = player.wins;
               entry.losses = player.losses;
               entry.gamesPlayed = player.gamesPlayed;
               if (player.gamesPlayed > 0) {
                  entry.winRate = Math.round((float)player.wins / (float)player.gamesPlayed * 100.0F);
               } else {
                  entry.winRate = 0;
               }

               Rankedtiers.RankInfo rankInfo = Rankedtiers.eloToRank(player.currentElo);
               entry.tierName = rankInfo.tier.name;
               entry.rankName = rankInfo.fullName;
               entry.rankColor = rankInfo.color;
               entry.highestElo = player.highestEloThisSeason;
               entry.winStreak = player.currentWinStreak;
               leaderboard.add(entry);
            }
         }

         Collections.sort(leaderboard, new Comparator<LeaderboardEntry>() {
            public int compare(LeaderboardEntry a, LeaderboardEntry b) {
               if (b.elo != a.elo) {
                  return b.elo - a.elo;
               } else {
                  return b.wins != a.wins ? b.wins - a.wins : b.winRate - a.winRate;
               }
            }
         });

         for(int i = 0; i < leaderboard.size(); ++i) {
            ((LeaderboardEntry)leaderboard.get(i)).rank = i + 1;
         }

         return leaderboard;
      }
   }

   public static PlayerRankResult getPlayerRank(World world, String uuid) {
      List<LeaderboardEntry> leaderboard = getTopPlayers(world, 100, true);

      for(LeaderboardEntry entry : leaderboard) {
         if (entry.uuid.equals(uuid)) {
            PlayerRankResult result = new PlayerRankResult(true);
            result.rank = entry.rank;
            result.entry = entry;
            result.totalPlayers = leaderboard.size();
            result.percentile = Math.round((1.0F - (float)entry.rank / (float)leaderboard.size()) * 100.0F);
            result.page = (int)Math.ceil((double)entry.rank / (double)10.0F);
            result.outsideTop100 = false;
            return result;
         }
      }

      List<LeaderboardEntry> fullLeaderboard = buildLeaderboard(world);

      for(LeaderboardEntry entry : fullLeaderboard) {
         if (entry.uuid.equals(uuid)) {
            PlayerRankResult result = new PlayerRankResult(true);
            result.rank = entry.rank;
            result.entry = entry;
            result.totalPlayers = fullLeaderboard.size();
            result.percentile = Math.round((1.0F - (float)entry.rank / (float)fullLeaderboard.size()) * 100.0F);
            result.page = (int)Math.ceil((double)entry.rank / (double)10.0F);
            result.outsideTop100 = true;
            return result;
         }
      }

      Rankeddatastorage storage = Rankeddatastorage.get(world);
      PlayerRankResult result = new PlayerRankResult(false);
      result.rank = -1;
      result.totalPlayers = fullLeaderboard.size();
      if (storage != null) {
         Rankeddatastorage.PlayerRankedData playerData = storage.getPlayerData(uuid);
         result.isPlaced = playerData.isPlaced;
         result.message = playerData.isPlaced ? "Not ranked yet" : "Complete placements to appear on leaderboard";
      } else {
         result.message = "Complete placements to appear on leaderboard";
      }

      return result;
   }

   public static int getTotalRankedPlayers(World world) {
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      if (storage == null) {
         return 0;
      } else {
         int count = 0;

         for(Rankeddatastorage.PlayerRankedData player : storage.getAllPlayers()) {
            if (player.isPlaced) {
               ++count;
            }
         }

         return count;
      }
   }

   public static List<LeaderboardEntry> getTopTen(World world) {
      if (cachedTop10 != null && System.currentTimeMillis() - top10CacheTimestamp < 30000L) {
         return cachedTop10;
      } else {
         List<LeaderboardEntry> top10 = getTopPlayers(world, 10, false);
         cachedTop10 = top10;
         top10CacheTimestamp = System.currentTimeMillis();
         return top10;
      }
   }

   public static boolean isInTopTen(World world, String uuid) {
      for(LeaderboardEntry entry : getTopTen(world)) {
         if (entry.uuid.equals(uuid)) {
            return true;
         }
      }

      return false;
   }

   public static int getTopTenPosition(World world, String uuid) {
      List<LeaderboardEntry> top10 = getTopTen(world);

      for(int i = 0; i < top10.size(); ++i) {
         if (((LeaderboardEntry)top10.get(i)).uuid.equals(uuid)) {
            return i + 1;
         }
      }

      return -1;
   }

   private static List<LeaderboardEntry> getCachedLeaderboard() {
      if (cachedLeaderboard == null) {
         return null;
      } else {
         return System.currentTimeMillis() - cacheTimestamp > 300000L ? null : cachedLeaderboard;
      }
   }

   private static void cacheLeaderboard(List<LeaderboardEntry> leaderboard) {
      cachedLeaderboard = leaderboard;
      cacheTimestamp = System.currentTimeMillis();
   }

   public static List<LeaderboardEntry> refreshLeaderboardCache(World world) {
      cachedLeaderboard = null;
      cacheTimestamp = 0L;
      cachedTop10 = null;
      top10CacheTimestamp = 0L;
      List<LeaderboardEntry> leaderboard = buildLeaderboard(world);
      cacheLeaderboard(leaderboard);
      return leaderboard;
   }

   public static void invalidateLeaderboardCache() {
      cacheTimestamp = 0L;
      top10CacheTimestamp = 0L;
   }

   public static void showLeaderboardChat(EntityPlayerMP player, int page) {
      if (page < 1) {
         page = 1;
      }

      World world = player.world;
      List<LeaderboardEntry> leaderboard = getTopPlayers(world, 100, true);
      int totalPages = (int)Math.ceil((double)leaderboard.size() / (double)10.0F);
      if (totalPages < 1) {
         totalPages = 1;
      }

      if (page > totalPages) {
         page = totalPages;
      }

      int startIndex = (page - 1) * 10;
      int endIndex = Math.min(startIndex + 10, leaderboard.size());
      player.sendMessage(new TextComponentString("§6§l===== RANKED LEADERBOARD ====="));
      player.sendMessage(new TextComponentString("§7Page " + page + "/" + totalPages + " | " + leaderboard.size() + " total players"));
      player.sendMessage(new TextComponentString(""));
      if (leaderboard.isEmpty()) {
         player.sendMessage(new TextComponentString("§7No ranked players yet!"));
      } else {
         for(int i = startIndex; i < endIndex; ++i) {
            LeaderboardEntry entry = (LeaderboardEntry)leaderboard.get(i);
            String rankColor = "§f";
            if (entry.rank == 1) {
               rankColor = "§6§l";
            } else if (entry.rank == 2) {
               rankColor = "§7§l";
            } else if (entry.rank == 3) {
               rankColor = "§c§l";
            } else if (entry.rank <= 10) {
               rankColor = "§e";
            }

            String wrColor = entry.winRate >= 50 ? "§a" : "§c";
            String line = rankColor + "#" + entry.rank + " " + entry.rankColor + entry.name + " §7- §f" + entry.elo + " ELO §7(§a" + entry.wins + "§7/§c" + entry.losses + "§7) " + wrColor + entry.winRate + "%";
            player.sendMessage(new TextComponentString(line));
         }
      }

      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§7Use /rankedtop " + (page + 1) + " for next page"));
      player.sendMessage(new TextComponentString("§6§l=============================="));
   }

   public static void showPlayerRankChat(EntityPlayerMP sender, String targetUUID, String displayName) {
      World world = sender.world;
      PlayerRankResult rankInfo = getPlayerRank(world, targetUUID);
      sender.sendMessage(new TextComponentString("§6§l===== PLAYER RANK ====="));
      if (!rankInfo.found) {
         sender.sendMessage(new TextComponentString("§7Player: §f" + displayName));
         sender.sendMessage(new TextComponentString("§7Status: §c" + rankInfo.message));
      } else {
         LeaderboardEntry entry = rankInfo.entry;
         sender.sendMessage(new TextComponentString("§7Player: " + entry.rankColor + displayName));
         sender.sendMessage(new TextComponentString("§7Rank: §e#" + entry.rank + " §7of §f" + rankInfo.totalPlayers));
         sender.sendMessage(new TextComponentString("§7Percentile: §aTop " + rankInfo.percentile + "%"));
         sender.sendMessage(new TextComponentString(""));
         sender.sendMessage(new TextComponentString("§7ELO: §f" + entry.elo + " §7(" + entry.rankColor + entry.rankName + "§7)"));
         String wrColor = entry.winRate >= 50 ? "§a" : "§c";
         sender.sendMessage(new TextComponentString("§7W/L: §a" + entry.wins + "§7/§c" + entry.losses + " §7(" + wrColor + entry.winRate + "% WR§7)"));
         sender.sendMessage(new TextComponentString("§7Peak ELO: §f" + entry.highestElo));
         if (entry.winStreak >= 3) {
            sender.sendMessage(new TextComponentString("§7Win Streak: §e" + entry.winStreak + " ★"));
         }

         if (entry.rank <= 10) {
            sender.sendMessage(new TextComponentString("§d§l★ TOP 10 - HOKAGE ELIGIBLE ★"));
         }
      }

      sender.sendMessage(new TextComponentString("§6§l======================="));
   }

   public static LeaderboardStats getLeaderboardStats(World world) {
      List<LeaderboardEntry> leaderboard = buildLeaderboard(world);
      LeaderboardStats stats = new LeaderboardStats();
      if (leaderboard.isEmpty()) {
         return stats;
      } else {
         int totalElo = 0;
         int totalGames = 0;
         int totalWinRate = 0;
         int highestElo = 0;
         int lowestElo = 99999;

         for(LeaderboardEntry entry : leaderboard) {
            totalElo += entry.elo;
            totalGames += entry.gamesPlayed;
            totalWinRate += entry.winRate;
            if (entry.elo > highestElo) {
               highestElo = entry.elo;
            }

            if (entry.elo < lowestElo) {
               lowestElo = entry.elo;
            }
         }

         stats.totalPlayers = leaderboard.size();
         stats.averageElo = Math.round((float)totalElo / (float)leaderboard.size());
         stats.highestElo = highestElo;
         stats.lowestElo = lowestElo;
         stats.totalGames = Math.round((float)totalGames / 2.0F);
         stats.averageWinRate = Math.round((float)totalWinRate / (float)leaderboard.size());
         return stats;
      }
   }

   public static RankDistribution getRankDistribution(World world) {
      List<LeaderboardEntry> leaderboard = buildLeaderboard(world);
      RankDistribution dist = new RankDistribution();

      for(LeaderboardEntry entry : leaderboard) {
         switch (entry.tierName) {
            case "Genin":
               ++dist.genin;
               break;
            case "Chunin":
               ++dist.chunin;
               break;
            case "Jonin":
               ++dist.jonin;
               break;
            case "S. Jonin":
               ++dist.specialJonin;
               break;
            case "Elite Jonin":
               ++dist.eliteJonin;
               break;
            case "ANBU":
               ++dist.anbu;
               break;
            case "Kage":
               ++dist.kage;
               break;
            case "Hokage":
               ++dist.hokage;
         }
      }

      return dist;
   }

   public static void showLeaderboardStatsChat(EntityPlayerMP player) {
      World world = player.world;
      LeaderboardStats stats = getLeaderboardStats(world);
      RankDistribution dist = getRankDistribution(world);
      player.sendMessage(new TextComponentString("§6§l===== LEADERBOARD STATS ====="));
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§7Total Ranked Players: §f" + stats.totalPlayers));
      player.sendMessage(new TextComponentString("§7Total Matches Played: §f" + stats.totalGames));
      player.sendMessage(new TextComponentString("§7Average ELO: §f" + stats.averageElo));
      player.sendMessage(new TextComponentString("§7Highest ELO: §a" + stats.highestElo));
      player.sendMessage(new TextComponentString("§7Lowest ELO: §c" + stats.lowestElo));
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§e§lRank Distribution:"));
      player.sendMessage(new TextComponentString("§4Hokage: §f" + dist.hokage + " §8| §cKage: §f" + dist.kage));
      player.sendMessage(new TextComponentString("§5ANBU: §f" + dist.anbu + " §8| §bElite Jonin: §f" + dist.eliteJonin));
      player.sendMessage(new TextComponentString("§6Jonin: §f" + dist.jonin + " §8| §eS. Jonin: §f" + dist.specialJonin));
      player.sendMessage(new TextComponentString("§aChunin: §f" + dist.chunin + " §8| §7Genin: §f" + dist.genin));
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§6§l============================="));
   }

   public static class LeaderboardEntry {
      public String uuid;
      public String name;
      public int elo;
      public int wins;
      public int losses;
      public int gamesPlayed;
      public int winRate;
      public String tierName;
      public String rankName;
      public String rankColor;
      public int highestElo;
      public int winStreak;
      public int rank;
   }

   public static class PlayerRankResult {
      public boolean found;
      public int rank;
      public LeaderboardEntry entry;
      public int totalPlayers;
      public int percentile;
      public int page;
      public boolean outsideTop100;
      public boolean isPlaced;
      public String message;

      public PlayerRankResult(boolean found) {
         this.found = found;
      }
   }

   public static class LeaderboardStats {
      public int totalPlayers;
      public int averageElo;
      public int highestElo;
      public int lowestElo;
      public int totalGames;
      public int averageWinRate;
   }

   public static class RankDistribution {
      public int genin;
      public int chunin;
      public int specialJonin;
      public int jonin;
      public int eliteJonin;
      public int anbu;
      public int kage;
      public int hokage;
   }
}
