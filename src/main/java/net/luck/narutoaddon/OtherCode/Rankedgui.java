
package net.luck.narutoaddon.OtherCode;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class Rankedgui {
   private static Map<String, String> playerMenuState = new HashMap();
   private static Map<String, Integer> playerLeaderboardPage = new HashMap();
   private static Map<String, Long> lastActionBarUpdate = new HashMap();
   public static final long ACTION_BAR_UPDATE_INTERVAL = 1000L;

   public static void showMainMenu(EntityPlayerMP player) {
      World world = player.world;
      String uuid = player.getUniqueID().toString();
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      if (storage == null) {
         player.sendMessage(new TextComponentString("§cError: Could not load ranked data!"));
      } else {
         Rankeddatastorage.PlayerRankedData playerData = storage.getPlayerData(uuid);
         Rankedtiers.RankInfo rankInfo = Rankedtiers.eloToRank(playerData.currentElo);
         boolean isQueued = Rankedqueue.isInQueue(uuid);
         boolean isInMatch = Rankedqueue.isInMatch(uuid);
         player.sendMessage(new TextComponentString("§6§l===== RANKED 1v1 ARENA ====="));
         player.sendMessage(new TextComponentString(""));
         String rankText = rankInfo.color + rankInfo.fullName + " §7- §f" + playerData.currentElo + " ELO";
         player.sendMessage(new TextComponentString("§7Rank: " + rankText));
         if (rankInfo.tier.hasDivisions) {
            player.sendMessage(new TextComponentString("§eLP: " + rankInfo.lp + "/100"));
         }

         int winRate = 0;
         if (playerData.gamesPlayed > 0) {
            winRate = Math.round((float)playerData.wins / (float)playerData.gamesPlayed * 100.0F);
         }

         String recordText = "§aW: " + playerData.wins + " §7| §cL: " + playerData.losses + " §7| §fWin Rate: §e" + winRate + "%";
         player.sendMessage(new TextComponentString(recordText));
         if (!playerData.isPlaced) {
            player.sendMessage(new TextComponentString("§d§lPlacements: §f" + playerData.placementGamesCompleted + "/" + 5));
         }

         player.sendMessage(new TextComponentString(""));
         if (isInMatch) {
            player.sendMessage(new TextComponentString("§c§lCurrently in a match!"));
         } else if (isQueued) {
            Rankedqueue.QueueStatus status = Rankedqueue.getQueueStatus(uuid);
            player.sendMessage(new TextComponentString("§e§lSearching for match..."));
            player.sendMessage(new TextComponentString("§7Time: §f" + status.waitTimeFormatted + " §7| Players: §f" + status.queueSize + " §7| Range: §f±" + status.searchRange));
         }

         player.sendMessage(new TextComponentString(""));
         player.sendMessage(new TextComponentString("§e§lCommands:"));
         if (isQueued) {
            player.sendMessage(new TextComponentString("§7  /ranked leave §8- Leave queue"));
         } else if (!isInMatch) {
            player.sendMessage(new TextComponentString("§7  /ranked queue §8- Join queue"));
         }

         player.sendMessage(new TextComponentString("§7  /ranked top [page] §8- Leaderboard"));
         player.sendMessage(new TextComponentString("§7  /ranked stats [player] §8- View stats"));
         player.sendMessage(new TextComponentString("§7  /ranked season §8- Season info"));
         player.sendMessage(new TextComponentString("§7  /ranked help §8- All commands"));
         player.sendMessage(new TextComponentString(""));
         player.sendMessage(new TextComponentString("§6§l============================"));
         playerMenuState.put(uuid, "main");
      }
   }

   public static void showPlayerStats(EntityPlayerMP viewer, String targetUUID, String targetName) {
      World world = viewer.world;
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      if (storage == null) {
         viewer.sendMessage(new TextComponentString("§cError: Could not load ranked data!"));
      } else {
         Rankeddatastorage.PlayerRankedData data = storage.getPlayerData(targetUUID);
         Rankedtiers.RankInfo rankInfo = Rankedtiers.eloToRank(data.currentElo);
         Rankedleaderboard.PlayerRankResult rankResult = Rankedleaderboard.getPlayerRank(world, targetUUID);
         int winRate = 0;
         if (data.gamesPlayed > 0) {
            winRate = Math.round((float)data.wins / (float)data.gamesPlayed * 100.0F);
         }

         viewer.sendMessage(new TextComponentString("§6§l===== " + targetName + "'s Stats ====="));
         viewer.sendMessage(new TextComponentString(""));
         viewer.sendMessage(new TextComponentString("§fRank: " + rankInfo.color + rankInfo.fullName));
         viewer.sendMessage(new TextComponentString("§fELO: §e" + data.currentElo + " §7(Peak: " + data.highestEloThisSeason + ")"));
         if (rankResult.found) {
            viewer.sendMessage(new TextComponentString("§fLeaderboard: §e#" + rankResult.rank));
         } else {
            viewer.sendMessage(new TextComponentString("§fLeaderboard: §7" + rankResult.message));
         }

         viewer.sendMessage(new TextComponentString(""));
         viewer.sendMessage(new TextComponentString("§fGames: §a" + data.wins + "W §7/ §c" + data.losses + "L §7(" + data.gamesPlayed + " total)"));
         String wrColor = winRate >= 50 ? "§a" : "§c";
         viewer.sendMessage(new TextComponentString("§fWin Rate: " + wrColor + winRate + "%"));
         viewer.sendMessage(new TextComponentString("§fWin Streak: §a" + data.currentWinStreak));
         if (!data.isPlaced) {
            viewer.sendMessage(new TextComponentString(""));
            viewer.sendMessage(new TextComponentString("§dPlacements: " + data.placementGamesCompleted + "/" + 5));
         }

         viewer.sendMessage(new TextComponentString(""));
         viewer.sendMessage(new TextComponentString("§6§l============================"));
      }
   }

   public static void showMatchFoundNotification(EntityPlayerMP player, String opponentName, int opponentElo, int arenaId) {
      Rankedtiers.RankInfo opponentRank = Rankedtiers.eloToRank(opponentElo);
      Rankedarena.Arena arena = Rankedarena.getArena(arenaId);
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§6§l================================"));
      player.sendMessage(new TextComponentString("§e§l         MATCH FOUND!"));
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§7  Opponent: " + opponentRank.color + opponentName));
      player.sendMessage(new TextComponentString("§7  Rank: " + opponentRank.color + opponentRank.fullName));
      if (arena != null) {
         player.sendMessage(new TextComponentString("§7  Arena: " + arena.color + arena.name));
      }

      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§c  Match starting in 5 seconds..."));
      player.sendMessage(new TextComponentString("§6§l================================"));
      player.sendMessage(new TextComponentString(""));
   }

   public static void showCountdown(EntityPlayerMP player, int seconds) {
      String color = seconds <= 2 ? "§c" : "§e";
      player.sendMessage(new TextComponentString(color + "§l" + seconds + "..."));
   }

   public static void showFightMessage(EntityPlayerMP player) {
      player.sendMessage(new TextComponentString("§a§lFIGHT!"));
   }

   public static void showVictoryMessage(EntityPlayerMP player, int eloChange, int newElo, String newRank) {
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§a§l================================"));
      player.sendMessage(new TextComponentString("§a§l          VICTORY!"));
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§fELO: §a+" + eloChange + " §7→ §f" + newElo));
      player.sendMessage(new TextComponentString("§fRank: " + newRank));
      player.sendMessage(new TextComponentString("§a§l================================"));
      player.sendMessage(new TextComponentString(""));
   }

   public static void showDefeatMessage(EntityPlayerMP player, int eloChange, int newElo, String newRank) {
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§c§l================================"));
      player.sendMessage(new TextComponentString("§c§l           DEFEAT"));
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§fELO: §c" + eloChange + " §7→ §f" + newElo));
      player.sendMessage(new TextComponentString("§fRank: " + newRank));
      player.sendMessage(new TextComponentString("§c§l================================"));
      player.sendMessage(new TextComponentString(""));
   }

   public static void updateQueueActionBar(EntityPlayerMP player) {
      String uuid = player.getUniqueID().toString();
      Long lastUpdate = (Long)lastActionBarUpdate.get(uuid);
      long now = System.currentTimeMillis();
      if (lastUpdate == null || now - lastUpdate >= 1000L) {
         if (Rankedqueue.isInQueue(uuid)) {
            Rankedqueue.QueueStatus status = Rankedqueue.getQueueStatus(uuid);
            (new StringBuilder()).append("§e§lIN QUEUE §7| §fTime: ").append(status.waitTimeFormatted).append(" §7| §fPlayers: ").append(status.queueSize).append(" §7| §fRange: ±").append(status.searchRange).toString();
            lastActionBarUpdate.put(uuid, now);
         }
      }
   }

   public static void showHelp(EntityPlayerMP player, boolean isAdmin) {
      player.sendMessage(new TextComponentString("§6§l===== RANKED COMMANDS ====="));
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§e/ranked §7- Open ranked menu"));
      player.sendMessage(new TextComponentString("§e/ranked queue §7- Join matchmaking queue"));
      player.sendMessage(new TextComponentString("§e/ranked leave §7- Leave queue"));
      player.sendMessage(new TextComponentString("§e/ranked stats [player] §7- View stats"));
      player.sendMessage(new TextComponentString("§e/ranked top [page] §7- View leaderboard"));
      player.sendMessage(new TextComponentString("§e/ranked season §7- Season information"));
      if (isAdmin) {
         player.sendMessage(new TextComponentString(""));
         player.sendMessage(new TextComponentString("§c§lAdmin Commands:"));
         player.sendMessage(new TextComponentString("§e/rankedadmin setelo <player> <elo>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin ban <player> [minutes] [reason]"));
         player.sendMessage(new TextComponentString("§e/rankedadmin unban <player>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin resetplayer <player>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin endseason"));
         player.sendMessage(new TextComponentString("§e/rankedadmin startseason [name]"));
      }

      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§6§l==========================="));
   }

   public static void showPromotionNotification(EntityPlayerMP player, String oldRank, String newRank) {
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§a§l★ PROMOTED! ★"));
      player.sendMessage(new TextComponentString("§7" + oldRank + " §a→ " + newRank));
      player.sendMessage(new TextComponentString(""));
   }

   public static void showDemotionNotification(EntityPlayerMP player, String oldRank, String newRank) {
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§c§l↓ Demoted ↓"));
      player.sendMessage(new TextComponentString("§7" + oldRank + " §c→ " + newRank));
      player.sendMessage(new TextComponentString(""));
   }

   public static void showPlacementComplete(EntityPlayerMP player, int finalElo, String finalRank) {
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§6§l================================"));
      player.sendMessage(new TextComponentString("§e§l  PLACEMENTS COMPLETE!"));
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§fYou have been placed at:"));
      player.sendMessage(new TextComponentString("§f" + finalRank + " §7(" + finalElo + " ELO)"));
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§aGood luck on the ladder!"));
      player.sendMessage(new TextComponentString("§6§l================================"));
      player.sendMessage(new TextComponentString(""));
   }

   public static void showWinStreakNotification(EntityPlayerMP player, int streak) {
      String message;
      if (streak >= 10) {
         message = "§d§l★ " + streak + " WIN STREAK! ★";
         String color = "§d";
      } else if (streak >= 7) {
         message = "§c§l★ " + streak + " Win Streak! ★";
         String color = "§c";
      } else if (streak >= 5) {
         message = "§6§l" + streak + " Win Streak!";
         String color = "§6";
      } else {
         if (streak < 3) {
            return;
         }

         message = "§e" + streak + " Win Streak!";
         String color = "§e";
      }

      player.sendMessage(new TextComponentString(message));
   }

   public static void showError(EntityPlayerMP player, String message) {
      player.sendMessage(new TextComponentString("§c" + message));
   }

   public static void showSuccess(EntityPlayerMP player, String message) {
      player.sendMessage(new TextComponentString("§a" + message));
   }

   public static void showInfo(EntityPlayerMP player, String message) {
      player.sendMessage(new TextComponentString("§e" + message));
   }

   public static String getMenuState(String uuid) {
      return (String)playerMenuState.getOrDefault(uuid, "none");
   }

   public static void clearMenuState(String uuid) {
      playerMenuState.remove(uuid);
      playerLeaderboardPage.remove(uuid);
   }

   public static int getLeaderboardPage(String uuid) {
      return (Integer)playerLeaderboardPage.getOrDefault(uuid, 1);
   }

   public static void setLeaderboardPage(String uuid, int page) {
      playerLeaderboardPage.put(uuid, page);
   }
}
