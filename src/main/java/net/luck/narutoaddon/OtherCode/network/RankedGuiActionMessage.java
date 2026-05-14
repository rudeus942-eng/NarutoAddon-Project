
package net.luck.narutoaddon.OtherCode.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.*;
import net.luck.narutoaddon.OtherCode.gui.GuiRankedLeaderboard;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.luck.narutoaddon.OtherCode.raid.core.RaidManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class RankedGuiActionMessage implements IMessage {
   private String action;

   public RankedGuiActionMessage() {
      this.action = "";
   }

   public RankedGuiActionMessage(String action) {
      this.action = action;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.action);
   }

   public void fromBytes(ByteBuf buf) {
      this.action = ByteBufUtils.readUTF8String(buf);
   }

   public static class Handler implements IMessageHandler<RankedGuiActionMessage, IMessage> {
      public IMessage onMessage(RankedGuiActionMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         player.getServerWorld().addScheduledTask(() -> this.handleAction(player, message.action));
         return null;
      }

      private void handleAction(EntityPlayerMP player, String action) {
         World world = player.world;
         String uuid = player.getUniqueID().toString();
         switch (action) {
            case "open":
               this.sendGuiData(player);
               break;
            case "queue":
               Rankedpenalties.QueueEligibility eligibility = Rankedpenalties.canQueue(uuid);
               if (!eligibility.canQueue) {
                  Rankedgui.showError(player, eligibility.message);
                  return;
               }

               if (Rankedqueue.isInQueue(uuid)) {
                  Rankedgui.showInfo(player, "You are already in queue!");
                  return;
               }

               if (Rankedqueue.isInMatch(uuid)) {
                  Rankedgui.showError(player, "You are already in a match!");
                  return;
               }

               Rankedqueue.QueueResult result = Rankedqueue.joinQueue(world, player);
               Rankedgui.showInfo(player, result.message);
               this.sendGuiData(player);
               break;
            case "leave":
               if (!Rankedqueue.isInQueue(uuid)) {
                  Rankedgui.showError(player, "You are not in queue!");
                  return;
               }

               Rankedqueue.QueueResult leaveResult = Rankedqueue.leaveQueue(uuid);
               Rankedgui.showInfo(player, leaveResult.message);
               this.sendGuiData(player);
               break;
            case "stats":
               Rankedgui.showPlayerStats(player, uuid, player.getName());
               break;
            case "leaderboard":
               Rankedleaderboard.showLeaderboardChat(player, 1);
               break;
            case "fullstats":
               this.sendFullStats(player);
               break;
            case "season":
               this.sendSeasonData(player);
               break;
            case "claimrewards":
               List<Rankedseason.PendingReward> pending = Rankedseason.getPendingRewards(world, uuid);
               if (!pending.isEmpty()) {
                  for(Rankedseason.PendingReward reward : pending) {
                     Rankedseason.claimSeasonRewards(player, reward.seasonNumber);
                  }

                  this.sendSeasonData(player);
               }
               break;
            default:
               if (action.startsWith("leaderboard:")) {
                  try {
                     int page = Integer.parseInt(action.substring(12));
                     this.sendLeaderboardData(player, page);
                  } catch (NumberFormatException var13) {
                     this.sendLeaderboardData(player, 1);
                  }
               }
         }

      }

      private void sendFullStats(EntityPlayerMP player) {
         World world = player.world;
         String uuid = player.getUniqueID().toString();
         Rankeddatastorage storage = Rankeddatastorage.get(world);
         if (storage != null) {
            Rankeddatastorage.PlayerRankedData data = storage.getPlayerData(uuid);
            Rankedtiers.RankInfo rankInfo = Rankedtiers.eloToRank(data.currentElo);
            Rankedleaderboard.PlayerRankResult lbResult = Rankedleaderboard.getPlayerRank(world, uuid);
            String placement = null;
            if (!data.isPlaced) {
               placement = data.placementGamesCompleted + "/5";
            }

            luckAddonAddon.PACKET_HANDLER.sendTo(new RankedStatsDataMessage(player.getName(), rankInfo.color + rankInfo.fullName, data.currentElo, data.peakElo, data.highestEloThisSeason, data.wins, data.losses, data.currentWinStreak, data.bestWinStreak, data.gamesPlayed, lbResult.found ? lbResult.rank : 0, placement), player);
         }
      }

      private void sendLeaderboardData(EntityPlayerMP player, int page) {
         World world = player.world;
         String uuid = player.getUniqueID().toString();
         Rankeddatastorage storage = Rankeddatastorage.get(world);
         if (storage != null) {
            List<Rankeddatastorage.PlayerRankedData> allPlayers = storage.getAllPlayers();
            List<Rankeddatastorage.PlayerRankedData> rankedPlayers = new ArrayList();

            for(Rankeddatastorage.PlayerRankedData p : allPlayers) {
               if (p.isPlaced) {
                  rankedPlayers.add(p);
               }
            }

            rankedPlayers.sort((a, b) -> Integer.compare(b.currentElo, a.currentElo));
            int perPage = 10;
            int totalPages = Math.max(1, (rankedPlayers.size() + perPage - 1) / perPage);
            page = Math.max(1, Math.min(page, totalPages));
            int startIdx = (page - 1) * perPage;
            int endIdx = Math.min(startIdx + perPage, rankedPlayers.size());
            List<GuiRankedLeaderboard.LeaderboardEntry> entries = new ArrayList();

            for(int i = startIdx; i < endIdx; ++i) {
               Rankeddatastorage.PlayerRankedData p = (Rankeddatastorage.PlayerRankedData)rankedPlayers.get(i);
               Rankedtiers.RankInfo ri = Rankedtiers.eloToRank(p.currentElo);
               entries.add(new GuiRankedLeaderboard.LeaderboardEntry(i + 1, p.playerName.isEmpty() ? "Unknown" : p.playerName, ri.color + ri.fullName, p.currentElo, p.wins, p.losses, p.uuid));
            }

            int playerRank = 0;
            Rankeddatastorage.PlayerRankedData playerData = storage.getPlayerData(uuid);
            if (playerData.isPlaced) {
               for(int i = 0; i < rankedPlayers.size(); ++i) {
                  if (((Rankeddatastorage.PlayerRankedData)rankedPlayers.get(i)).uuid.equals(uuid)) {
                     playerRank = i + 1;
                     break;
                  }
               }
            }

            luckAddonAddon.PACKET_HANDLER.sendTo(new RankedLeaderboardDataMessage(entries, page, totalPages, playerRank, uuid), player);
         }
      }

      private void sendSeasonData(EntityPlayerMP player) {
         World world = player.world;
         String uuid = player.getUniqueID().toString();
         Rankedseason.SeasonInfo info = Rankedseason.getSeasonInfo(world);
         Rankedleaderboard.LeaderboardStats lbStats = Rankedleaderboard.getLeaderboardStats(world);
         List<Rankedseason.PendingReward> pending = Rankedseason.getPendingRewards(world, uuid);
         String rewardTier = "";
         if (!pending.isEmpty()) {
            rewardTier = ((Rankedseason.PendingReward)pending.get(0)).tier;
         }

         long remaining = info.endTimestamp - System.currentTimeMillis();
         int daysRemaining = 0;
         int hoursRemaining = 0;
         int minutesRemaining = 0;
         if (remaining > 0L) {
            daysRemaining = (int)(remaining / 86400000L);
            long remainderMs = remaining % 86400000L;
            hoursRemaining = (int)(remainderMs / 3600000L);
            remainderMs %= 3600000L;
            minutesRemaining = (int)(remainderMs / 60000L);
         }

         luckAddonAddon.PACKET_HANDLER.sendTo(new RankedSeasonDataMessage(info.seasonNumber, info.seasonId, info.seasonName, daysRemaining, hoursRemaining, minutesRemaining, info.totalMatches, lbStats.totalPlayers, !pending.isEmpty(), rewardTier), player);
      }

      private void sendGuiData(EntityPlayerMP player) {
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
            List<Rankedseason.PendingReward> pendingRewards = Rankedseason.getPendingRewards(world, uuid);
            boolean hasPendingRewards = !pendingRewards.isEmpty();
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
   }
}
