
package net.luck.narutoaddon.OtherCode;

import net.luck.narutoaddon.OtherCode.network.RankedGuiDataMessage;
import net.luck.narutoaddon.OtherCode.network.RankedMatchHudMessage;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.luck.narutoaddon.OtherCode.raid.core.RaidManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextComponent.Serializer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Rankedmain {
   public static final String VERSION = "1.0.0";
   public static boolean DEBUG_MODE = false;
   public static final int QUEUE_TICK_INTERVAL = 20;
   public static final int AFK_CHECK_INTERVAL = 100;
   public static final int LEADERBOARD_CACHE_INTERVAL = 600;
   public static final int HUD_SYNC_INTERVAL = 60;
   public static final int MATCH_TICK_INTERVAL = 20;
   public static final int MATCH_HUD_SYNC_INTERVAL = 20;
   public static final int SEASON_CHECK_INTERVAL = 1200;
   private static int tickCounter = 0;
   private static boolean initialized = false;
   private static Map<String, Integer> pendingHudSyncs = new ConcurrentHashMap();

   public static void init() {
      if (!initialized) {
         logInfo("Initializing Ranked 1v1 System v1.0.0...");
         MinecraftForge.EVENT_BUS.register(new RankedEventHandler());
         registerClientHud();
         initialized = true;
         logInfo("Ranked 1v1 System initialized successfully!");
      }
   }

   public static boolean isInitialized() {
      return initialized;
   }

   public static void logInfo(String message) {
      System.out.println("[Ranked] " + message);
   }

   public static void logDebug(String message) {
      if (DEBUG_MODE) {
         System.out.println("[Ranked/DEBUG] " + message);
      }

   }

   public static void logError(String message) {
      System.err.println("[Ranked/ERROR] " + message);
   }

   public static void logEvent(String eventType, String details) {
      logInfo("[EVENT:" + eventType + "] " + details);
   }

   private static void registerClientHud() {
      try {
         if (FMLCommonHandler.instance().getSide().isClient()) {
            registerClientHudInternal();
         }
      } catch (Exception var1) {
         logDebug("Client HUD registration skipped (server-side)");
      }

   }

   @SideOnly(Side.CLIENT)
   private static void registerClientHudInternal() {
      MinecraftForge.EVENT_BUS.register(RankedHudRenderer.getInstance());
      MinecraftForge.EVENT_BUS.register(RankedMatchHudOverlay.getInstance());
      RankedInventoryOverlay.register();
      logInfo("Client HUD renderer, match HUD overlay, and inventory overlay registered");
   }

   public static boolean isAdmin(EntityPlayer player) {
      if (player instanceof EntityPlayerMP) {
         EntityPlayerMP mp = (EntityPlayerMP)player;
         if (mp.getServer() != null) {
            return mp.getServer().getPlayerList().canSendCommands(mp.getGameProfile());
         }
      }

      return false;
   }

   public static void broadcastMessage(World world, String message) {
      for(EntityPlayer player : world.playerEntities) {
         player.sendMessage(new TextComponentString(message));
      }

   }

   public static void broadcastMatchResult(World world, String winnerName, String loserName, String winnerRank) {
      String announcement = "§6[Ranked] §a" + winnerName + " §7defeated §c" + loserName + " §7(" + winnerRank + ")";
      broadcastMessage(world, announcement);
   }

   public static void processMatchResult(World world, String winnerUUID, String loserUUID, String reason) {
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      if (storage == null) {
         logError("Could not process match result - storage unavailable");
      } else {
         Rankeddatastorage.PlayerRankedData winnerData = storage.getPlayerData(winnerUUID);
         Rankeddatastorage.PlayerRankedData loserData = storage.getPlayerData(loserUUID);
         String oldWinnerRank = Rankedtiers.eloToRank(winnerData.currentElo).fullName;
         String oldLoserRank = Rankedtiers.eloToRank(loserData.currentElo).fullName;
         int winnerK = Rankedelocore.getKFactor(winnerData.gamesPlayed, winnerData.currentElo);
         int loserK = Rankedelocore.getKFactor(loserData.gamesPlayed, loserData.currentElo);
         double winnerExpected = Rankedelocore.calculateExpectedScore(winnerData.currentElo, loserData.currentElo);
         double loserExpected = Rankedelocore.calculateExpectedScore(loserData.currentElo, winnerData.currentElo);
         int winnerNewElo = (int)Math.round(Rankedelocore.calculateNewRating(winnerData.currentElo, winnerK, 1, winnerExpected));
         int loserNewElo = (int)Math.round(Rankedelocore.calculateNewRating(loserData.currentElo, loserK, 0, loserExpected));
         winnerNewElo = Rankedelocore.clampElo((double)winnerNewElo);
         loserNewElo = Rankedelocore.clampElo((double)loserNewElo);
         int winnerChange = winnerNewElo - winnerData.currentElo;
         int loserChange = loserNewElo - loserData.currentElo;
         double streakMult = Rankedelocore.getStreakMultiplier(winnerData.currentWinStreak + 1);
         winnerChange = (int)Math.round((double)winnerChange * streakMult);
         winnerNewElo = winnerData.currentElo + winnerChange;
         Rankeddatastorage.updatePlayerAfterMatch(world, winnerUUID, true, winnerChange, winnerNewElo);
         Rankeddatastorage.updatePlayerAfterMatch(world, loserUUID, false, loserChange, loserNewElo);
         winnerData = storage.getPlayerData(winnerUUID);
         loserData = storage.getPlayerData(loserUUID);
         checkPlacementCompletion(world, winnerUUID, winnerData);
         checkPlacementCompletion(world, loserUUID, loserData);
         Rankedtiers.RankInfo newWinnerRank = Rankedtiers.eloToRank(winnerNewElo);
         Rankedtiers.RankInfo newLoserRank = Rankedtiers.eloToRank(loserNewElo);
         EntityPlayerMP winner = getPlayer(world, winnerUUID);
         EntityPlayerMP loser = getPlayer(world, loserUUID);
         if (winner != null) {
            Rankedgui.showVictoryMessage(winner, winnerChange, winnerNewElo, newWinnerRank.getDisplayName());
            if (Rankedtiers.wouldPromote(winnerData.currentElo - winnerChange, winnerNewElo)) {
               Rankedgui.showPromotionNotification(winner, oldWinnerRank, newWinnerRank.fullName);
            }

            if (winnerData.currentWinStreak >= 3) {
               Rankedgui.showWinStreakNotification(winner, winnerData.currentWinStreak);
            }
         }

         if (loser != null) {
            Rankedgui.showDefeatMessage(loser, loserChange, loserNewElo, newLoserRank.getDisplayName());
            if (Rankedtiers.wouldDemote(loserData.currentElo - loserChange, loserNewElo)) {
               Rankedgui.showDemotionNotification(loser, oldLoserRank, newLoserRank.fullName);
            }
         }

         Rankedleaderboard.invalidateLeaderboardCache();
         Rankedseason.incrementSeasonMatches(world);
         logEvent("MATCH_COMPLETE", "Winner: " + winnerUUID + " +" + winnerChange + ", Loser: " + loserUUID + " " + loserChange + ", Reason: " + reason);
      }
   }

   private static void checkPlacementCompletion(World world, String uuid, Rankeddatastorage.PlayerRankedData ignoredData) {
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      if (storage != null) {
         Rankeddatastorage.PlayerRankedData data = storage.getPlayerData(uuid);
         if (!data.isPlaced) {
            if (data.placementGamesCompleted >= 5) {
               Rankedseason.PlacementResult result = Rankedseason.finalizePlacements(world, uuid);
               EntityPlayerMP player = getPlayer(world, uuid);
               if (player != null) {
                  Rankedgui.showPlacementComplete(player, result.finalElo, result.rankColor + result.finalRank);
               }

               logEvent("PLACEMENT_COMPLETE", "Player: " + uuid + ", ELO: " + result.finalElo + ", Rank: " + result.finalRank);
            }

         }
      }
   }

   public static void onWorldTick(World world) {
      if (!world.isRemote) {
         ++tickCounter;
         if (tickCounter % 20 == 0) {
            processQueueTick(world);
         }

         if (tickCounter % 20 == 0) {
            processMatchTick(world);
         }

         if (tickCounter % 100 == 0) {
            processAFKTick(world);
         }

         if (tickCounter % 600 == 0) {
            Rankedleaderboard.refreshLeaderboardCache(world);
            logDebug("Leaderboard cache refreshed");
         }

         if (tickCounter % 60 == 0) {
            sendPeriodicHudUpdates(world);
         }

         if (tickCounter % 20 == 0) {
            sendMatchHudUpdates(world);
         }

         if (tickCounter % 1200 == 0) {
            Rankedseason.checkSeasonEnd(world);
         }

         processPendingHudSyncs(world);
         Rankedarena.processPendingTeleports(world);
         if (tickCounter >= 72000) {
            tickCounter = 0;
         }

      }
   }

   private static void processQueueTick(World world) {
      try {
         Rankedqueue.processQueue(world);
      } catch (Exception e) {
         logDebug("Queue tick error: " + e.getMessage());
      }

   }

   private static void processMatchTick(World world) {
      long now = System.currentTimeMillis();

      for(Rankedqueue.Match match : Rankedqueue.getActiveMatches()) {
         if ("countdown".equals(match.state)) {
            long countdownElapsed = now - match.startTime;
            EntityPlayerMP p1 = getPlayer(world, match.player1UUID);
            EntityPlayerMP p2 = getPlayer(world, match.player2UUID);
            int secondsRemaining = (int)((3000L - countdownElapsed) / 1000L) + 1;
            int prevSecondsRemaining = (int)((3000L - (countdownElapsed - 1000L)) / 1000L) + 1;
            if (secondsRemaining != prevSecondsRemaining && secondsRemaining > 0 && secondsRemaining <= 3) {
               String color = secondsRemaining == 1 ? "red" : (secondsRemaining == 2 ? "gold" : "yellow");
               if (p1 != null) {
                  sendCountdownTitle(p1, secondsRemaining, color);
               }

               if (p2 != null) {
                  sendCountdownTitle(p2, secondsRemaining, color);
               }
            }

            if (countdownElapsed >= 3000L) {
               Rankedarena.startMatch(match);
               if (p1 != null) {
                  sendFightTitle(p1);
               }

               if (p2 != null) {
                  sendFightTitle(p2);
               }

               logEvent("MATCH_START", "Match " + match.matchId + " is now active");
            }
         }

         if ("active".equals(match.state)) {
            long matchDuration = now - match.startTime;
            if (matchDuration >= 300000L) {
               Rankedarena.endMatchDraw(world, match.matchId, "Time limit reached (5 minutes)");
               logEvent("MATCH_DRAW", "Match " + match.matchId + " ended in draw - time limit reached");
            } else {
               Rankedarena.Arena arena = Rankedarena.getArena(world, match.arenaId);
               if (arena != null && arena.hasRingOut) {
                  EntityPlayerMP p1 = getPlayer(world, match.player1UUID);
                  EntityPlayerMP p2 = getPlayer(world, match.player2UUID);
                  if (p1 != null && Rankedarena.checkRingOut(p1, match.arenaId)) {
                     Rankedarena.endMatch(world, match.matchId, match.player2UUID, match.player1UUID, "ringout");
                     logEvent("RING_OUT", "Player " + match.player1Name + " fell below Y=" + arena.ringOutY);
                  } else if (p2 != null && Rankedarena.checkRingOut(p2, match.arenaId)) {
                     Rankedarena.endMatch(world, match.matchId, match.player1UUID, match.player2UUID, "ringout");
                     logEvent("RING_OUT", "Player " + match.player2Name + " fell below Y=" + arena.ringOutY);
                  }
               }
            }
         }
      }

   }

   private static void sendCountdownTitle(EntityPlayerMP player, int number, String color) {
      if (player.connection != null) {
         player.connection.sendPacket(new SPacketTitle(Type.TIMES, (ITextComponent)null, 0, 20, 10));
         String titleJson = "{\"text\":\"" + number + "\",\"color\":\"" + color + "\",\"bold\":true}";
         ITextComponent titleText = Serializer.jsonToComponent(titleJson);
         player.connection.sendPacket(new SPacketTitle(Type.TITLE, titleText));
      }
   }

   private static void sendFightTitle(EntityPlayerMP player) {
      if (player.connection != null) {
         player.connection.sendPacket(new SPacketTitle(Type.TIMES, (ITextComponent)null, 0, 30, 10));
         String titleJson = "{\"text\":\"⚔ FIGHT! ⚔\",\"color\":\"green\",\"bold\":true}";
         ITextComponent titleText = Serializer.jsonToComponent(titleJson);
         player.connection.sendPacket(new SPacketTitle(Type.TITLE, titleText));
      }
   }

   private static void processAFKTick(World world) {
      for(Rankedqueue.Match match : Rankedqueue.getActiveMatches()) {
         if ("active".equals(match.state)) {
            if (Rankedpenalties.shouldKickAFK(match.player1UUID)) {
               Rankedarena.endMatch(world, match.matchId, match.player2UUID, match.player1UUID, "afk");
               logEvent("AFK_KICK", "Player: " + match.player1UUID);
            } else if (Rankedpenalties.shouldWarnAFK(match.player1UUID)) {
               EntityPlayerMP p1 = getPlayer(world, match.player1UUID);
               if (p1 != null) {
                  int seconds = Rankedpenalties.getSecondsUntilKick(match.player1UUID);
                  Rankedgui.showInfo(p1, "§cWarning: AFK kick in " + seconds + " seconds!");
               }
            }

            if (Rankedpenalties.shouldKickAFK(match.player2UUID)) {
               Rankedarena.endMatch(world, match.matchId, match.player1UUID, match.player2UUID, "afk");
               logEvent("AFK_KICK", "Player: " + match.player2UUID);
            } else if (Rankedpenalties.shouldWarnAFK(match.player2UUID)) {
               EntityPlayerMP p2 = getPlayer(world, match.player2UUID);
               if (p2 != null) {
                  int seconds = Rankedpenalties.getSecondsUntilKick(match.player2UUID);
                  Rankedgui.showInfo(p2, "§cWarning: AFK kick in " + seconds + " seconds!");
               }
            }
         }
      }

   }

   public static void onPlayerJoin(EntityPlayerMP player, World world) {
      String uuid = player.getUniqueID().toString();
      String name = player.getName();
      logDebug("Player joined: " + name);
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      if (storage != null) {
         Rankeddatastorage.PlayerRankedData data = storage.getPlayerData(uuid);
         if (data.uuid.isEmpty()) {
            storage.initializePlayer(uuid, name);
            logDebug("Initialized ranked data for new player: " + name);
         } else if (!name.equals(data.playerName)) {
            data.playerName = name;
            storage.savePlayerData(uuid, data);
         }

         if (Rankedseason.hasPendingRewards(world, uuid)) {
            Rankedseason.notifyPendingRewards(player);
         }

         if (data.gamesPlayed > 0) {
            Rankedtiers.RankInfo rank = Rankedtiers.eloToRank(data.currentElo);
            player.sendMessage(new TextComponentString("§7Welcome back! Ranked: " + rank.color + rank.fullName + " §7(" + data.currentElo + " ELO)"));
         }
      }

      Rankedqueue.Match activeMatch = Rankedqueue.getPlayerMatch(uuid);
      if (activeMatch != null) {
         logInfo("Player " + name + " reconnecting to match " + activeMatch.matchId);
         handleMatchReconnection(player, world, activeMatch);
      }

      scheduleHudSync(player, uuid, 40);
   }

   private static void scheduleHudSync(EntityPlayerMP player, String uuid, int tickDelay) {
      pendingHudSyncs.put(uuid, tickDelay);
   }

   private static void processPendingHudSyncs(World world) {
      Iterator<Map.Entry<String, Integer>> it = pendingHudSyncs.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<String, Integer> entry = (Map.Entry)it.next();
         int remaining = (Integer)entry.getValue() - 1;
         if (remaining <= 0) {
            String uuid = (String)entry.getKey();
            EntityPlayerMP player = getPlayer(world, uuid);
            if (player != null) {
               sendHudData(player);
            }

            it.remove();
         } else {
            entry.setValue(remaining);
         }
      }

   }

   private static void sendPeriodicHudUpdates(World world) {
      if (world.getMinecraftServer() != null) {
         for(EntityPlayer player : world.playerEntities) {
            if (player instanceof EntityPlayerMP) {
               sendHudData((EntityPlayerMP)player);
            }
         }

      }
   }

   private static void sendMatchHudUpdates(World world) {
      if (world.getMinecraftServer() != null) {
         long now = System.currentTimeMillis();

         for(Rankedqueue.Match match : Rankedqueue.getActiveMatches()) {
            if ("countdown".equals(match.state) || "active".equals(match.state)) {
               EntityPlayerMP p1 = getPlayer(world, match.player1UUID);
               EntityPlayerMP p2 = getPlayer(world, match.player2UUID);
               int timeRemainingSeconds;
               if ("countdown".equals(match.state)) {
                  long countdownElapsed = now - match.startTime;
                  timeRemainingSeconds = Math.max(0, (int)((3000L - countdownElapsed) / 1000L));
               } else {
                  long matchElapsed = now - match.startTime;
                  timeRemainingSeconds = Math.max(0, (int)((300000L - matchElapsed) / 1000L));
               }

               Rankeddatastorage storage = Rankeddatastorage.get(world);
               String p1Rank = "";
               String p2Rank = "";
               if (storage != null) {
                  Rankeddatastorage.PlayerRankedData p1Data = storage.getPlayerData(match.player1UUID);
                  Rankeddatastorage.PlayerRankedData p2Data = storage.getPlayerData(match.player2UUID);
                  Rankedtiers.RankInfo p1RankInfo = Rankedtiers.eloToRank(p1Data.currentElo);
                  Rankedtiers.RankInfo p2RankInfo = Rankedtiers.eloToRank(p2Data.currentElo);
                  p1Rank = p1RankInfo.color + p1RankInfo.fullName;
                  p2Rank = p2RankInfo.color + p2RankInfo.fullName;
               }

               String arenaName = "";
               Rankedarena.Arena arena = Rankedarena.getArena(world, match.arenaId);
               if (arena != null) {
                  arenaName = arena.name;
               }

               RankedMatchHudMessage hudMsg = new RankedMatchHudMessage(match.state, timeRemainingSeconds, match.player1Name, match.player2Name, p1Rank, p2Rank, match.player1UUID, match.player2UUID, arenaName);
               if (p1 != null) {
                  luckAddonAddon.PACKET_HANDLER.sendTo(hudMsg, p1);
               }

               if (p2 != null) {
                  luckAddonAddon.PACKET_HANDLER.sendTo(hudMsg, p2);
               }
            }
         }

         for(EntityPlayer player : world.playerEntities) {
            if (player instanceof EntityPlayerMP) {
               String uuid = player.getUniqueID().toString();
               if (!Rankedqueue.isInMatch(uuid)) {
               }
            }
         }

      }
   }

   public static void sendHudData(EntityPlayerMP player) {
      World world = player.world;
      String uuid = player.getUniqueID().toString();
      Rankeddatastorage storage = Rankeddatastorage.get(world);
      if (storage != null) {
         Rankeddatastorage.PlayerRankedData data = storage.getPlayerData(uuid);
         Rankedtiers.RankInfo rankInfo = Rankedtiers.eloToRank(data.currentElo);
         boolean isQueued = Rankedqueue.isInQueue(uuid);
         boolean isInMatch = Rankedqueue.isInMatch(uuid);
         int queueTime = 0;
         int queueSize = 0;
         if (isQueued) {
            Rankedqueue.QueueStatus status = Rankedqueue.getQueueStatus(uuid);
            queueTime = (int)(status.waitTime / 1000L);
            queueSize = status.queueSize;
         }

         String placement = null;
         if (!data.isPlaced) {
            placement = data.placementGamesCompleted + "/5";
         }

         Rankedrewards.DailyTaskProgress dailyTask = Rankedrewards.getDailyTaskProgress(world, uuid);
         boolean hasPendingRewards = Rankedseason.hasPendingRewards(world, uuid);
         boolean isInRaid = RaidManager.getInstance().isPlayerInRaid(player.getUniqueID());
         String currentRaidBoss = "";
         if (isInRaid) {
            RaidInstance raid = RaidManager.getInstance().getPlayerRaid(player.getUniqueID());
            if (raid != null && raid.getBoss() != null) {
               currentRaidBoss = raid.getBoss().getBossDisplayName();
            }
         }

         luckAddonAddon.PACKET_HANDLER.sendTo(new RankedGuiDataMessage(rankInfo.color + rankInfo.fullName, data.currentElo, data.wins, data.losses, data.currentWinStreak, isQueued, isInMatch, queueTime, queueSize, placement, dailyTask.winsToday, dailyTask.winsRequired, dailyTask.completed, hasPendingRewards, isInRaid, currentRaidBoss), player);
      }
   }

   public static void onPlayerLeave(EntityPlayer player, World world) {
      String uuid = player.getUniqueID().toString();
      String name = player.getName();
      logDebug("Player leaving: " + name);
      if (Rankedqueue.isInQueue(uuid)) {
         Rankedqueue.leaveQueue(uuid);
         logDebug("Removed " + name + " from queue on disconnect");
      }

      Rankedqueue.Match activeMatch = Rankedqueue.getPlayerMatch(uuid);
      if (activeMatch != null) {
         logInfo("Player " + name + " disconnected during match " + activeMatch.matchId);
         if ("countdown".equals(activeMatch.state)) {
            Rankedqueue.cancelMatch(activeMatch.matchId, "Player disconnected during countdown");
            logEvent("MATCH_CANCELLED", "Reason: disconnect_countdown");
         } else if ("active".equals(activeMatch.state)) {
            Rankedpenalties.recordDisconnect(uuid);
            String opponentUUID = uuid.equals(activeMatch.player1UUID) ? activeMatch.player2UUID : activeMatch.player1UUID;
            Rankedarena.endMatch(world, activeMatch.matchId, opponentUUID, uuid, "disconnect");
            EntityPlayerMP opponent = getPlayer(world, opponentUUID);
            if (opponent != null) {
               Rankedgui.showInfo(opponent, "§aYour opponent disconnected! You win!");
            }

            logEvent("MATCH_DISCONNECT", "Player: " + uuid);
         }
      }

      Rankedgui.clearMenuState(uuid);
   }

   public static void onPlayerDeath(EntityPlayer player, World world) {
      String uuid = player.getUniqueID().toString();
      Rankedqueue.Match activeMatch = Rankedqueue.getPlayerMatch(uuid);
      if (activeMatch != null && "active".equals(activeMatch.state)) {
         logDebug("Ranked match death: " + player.getName());
         String winnerUUID = uuid.equals(activeMatch.player1UUID) ? activeMatch.player2UUID : activeMatch.player1UUID;
         Rankedarena.endMatch(world, activeMatch.matchId, winnerUUID, uuid, "death");
         logEvent("MATCH_END", "Winner: " + winnerUUID + ", Loser: " + uuid + ", Reason: death");
      }
   }

   private static void handleMatchReconnection(EntityPlayerMP player, World world, Rankedqueue.Match match) {
      String uuid = player.getUniqueID().toString();
      Rankedarena.teleportToArena(player, match.arenaId, uuid.equals(match.player1UUID));
      player.sendMessage(new TextComponentString("§a§lReconnected to ranked match!"));
      player.sendMessage(new TextComponentString("§7Match will continue."));
      String opponentUUID = uuid.equals(match.player1UUID) ? match.player2UUID : match.player1UUID;
      EntityPlayerMP opponent = getPlayer(world, opponentUUID);
      if (opponent != null) {
         opponent.sendMessage(new TextComponentString("§aYour opponent has reconnected!"));
      }

      Rankedpenalties.updatePlayerActivity(uuid);
      logEvent("MATCH_RECONNECT", "Player: " + uuid + ", Match: " + match.matchId);
   }

   public static void onPlayerActivity(EntityPlayer player) {
      String uuid = player.getUniqueID().toString();
      if (Rankedqueue.isInMatch(uuid)) {
         Rankedpenalties.updatePlayerActivity(uuid);
      }

   }

   private static EntityPlayerMP getPlayer(World world, String uuid) {
      return world.getMinecraftServer() == null ? null : world.getMinecraftServer().getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
   }

   public static class RankedEventHandler {
      @SubscribeEvent
      public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            Rankedmain.onPlayerJoin(player, player.world);
         }

      }

      @SubscribeEvent
      public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
         if (event.player != null) {
            Rankedmain.onPlayerLeave(event.player, event.player.world);
         }

      }

      @SubscribeEvent
      public void onLivingDeath(LivingDeathEvent event) {
         if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.getEntityLiving();
            Rankedmain.onPlayerDeath(player, player.world);
         }

      }

      @SubscribeEvent
      public void onWorldTick(TickEvent.WorldTickEvent event) {
         if (event.phase == Phase.END && !event.world.isRemote) {
            Rankedmain.onWorldTick(event.world);
         }

      }

      @SubscribeEvent
      public void onPlayerTick(TickEvent.PlayerTickEvent event) {
         if (event.phase == Phase.START) {
            if (!event.player.world.isRemote) {
               if (event.player instanceof EntityPlayerMP) {
                  EntityPlayerMP player = (EntityPlayerMP)event.player;
                  String uuid = player.getUniqueID().toString();
                  Rankedqueue.Match match = Rankedqueue.getPlayerMatch(uuid);
                  if (match != null && "countdown".equals(match.state)) {
                     player.motionX = (double)0.0F;
                     player.motionY = (double)0.0F;
                     player.motionZ = (double)0.0F;
                     player.velocityChanged = true;
                     Double[] spawnPos = this.getPlayerArenaSpawnPos(player, match, uuid);
                     if (spawnPos != null) {
                        double dx = player.posX - spawnPos[0];
                        double dz = player.posZ - spawnPos[2];
                        double distSq = dx * dx + dz * dz;
                        if (distSq > (double)0.25F) {
                           player.setPositionAndUpdate(spawnPos[0], spawnPos[1], spawnPos[2]);
                        }
                     }
                  }

               }
            }
         }
      }

      private Double[] getPlayerArenaSpawnPos(EntityPlayerMP player, Rankedqueue.Match match, String uuid) {
         Rankedarena.Arena arena = Rankedarena.getArena(player.world, match.arenaId);
         if (arena == null) {
            return null;
         } else {
            boolean isPlayer1 = uuid.equals(match.player1UUID);
            return isPlayer1 ? new Double[]{(double)arena.player1Spawn.getX() + (double)0.5F, (double)arena.player1Spawn.getY(), (double)arena.player1Spawn.getZ() + (double)0.5F} : new Double[]{(double)arena.player2Spawn.getX() + (double)0.5F, (double)arena.player2Spawn.getY(), (double)arena.player2Spawn.getZ() + (double)0.5F};
         }
      }
   }
}
