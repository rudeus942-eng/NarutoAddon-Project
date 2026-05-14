
package net.luck.narutoaddon.OtherCode.quest.pvp.network;

import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.*;
import net.luck.narutoaddon.OtherCode.quest.pvp.tournament.*;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class PvpNetworkHelper {
   private static final Random RANDOM = new Random();
   private static final int FUZZY_OFFSET = 150;

   public static void sendPvpSync(EntityPlayerMP player) {
      if (PvpModInit.NETWORK != null) {
         UUID playerId = player.getUniqueID();
         PvpSavedData data = PvpSavedData.get(player.world);
         PvpManager manager = PvpManager.getInstance();
         List<PvpSyncMessage.ActivePvpMissionInfo> activeMissions = new ArrayList();
         Map<String, PvpMissionInstance> instances = data.getActiveInstances(playerId);

         for(Map.Entry<String, PvpMissionInstance> entry : instances.entrySet()) {
            byte slotByte = PvpSyncMessage.pvpSubSlotToByte((String)entry.getKey());
            if (slotByte >= 0) {
               PvpMissionInstance inst = (PvpMissionInstance)entry.getValue();
               PvpSyncMessage.ActivePvpMissionInfo info = new PvpSyncMessage.ActivePvpMissionInfo();
               info.subSlotByte = slotByte;
               info.name = inst.getName();
               info.description = inst.getProgressText();
               info.progress = inst.getUniqueVictimsKilled().size();
               info.killsRequired = inst.getObjective().getKillsRequired();
               info.rank = (byte)inst.getRank().ordinal();
               info.startTimeMs = inst.getStartTime();
               info.timeLimitMs = inst.getObjective().getTimeLimit() > 0 ? (long)inst.getObjective().getTimeLimit() * 50L : 0L;
               activeMissions.add(info);
            }
         }

         List<PvpSyncMessage.PvpOfferInfo> offers = new ArrayList();

         for(String subSlot : PvpManager.getAllSubSlots()) {
            byte slotByte = PvpSyncMessage.pvpSubSlotToByte(subSlot);
            if (slotByte >= 0 && !instances.containsKey(subSlot)) {
               PvpMissionOffer offer = manager.getCurrentOffer(playerId, subSlot, player.world);
               if (offer != null) {
                  PvpSyncMessage.PvpOfferInfo oi = new PvpSyncMessage.PvpOfferInfo();
                  oi.subSlotByte = slotByte;
                  oi.name = offer.getName();
                  oi.description = offer.getDescription();
                  oi.rank = (byte)offer.getRank().ordinal();
                  oi.killCount = offer.getObjective().getKillsRequired();
                  oi.ninjaXpReward = offer.getNinjaXpReward();
                  oi.pvpXpReward = offer.getPvpXpReward();
                  oi.ryoReward = offer.getRyoReward();
                  oi.rerollsRemaining = manager.getRerollsRemaining(playerId, subSlot, player.world);
                  oi.cooldownRemaining = data.getSlotCooldownRemaining(playerId, subSlot);
                  offers.add(oi);
               }
            }
         }

         List<PvpSyncMessage.BingoInfo> bingoEntries = new ArrayList();

         try {
            for(BingoBook.BingoEntry entry : BingoBook.getInstance().getActiveEntries()) {
               PvpSyncMessage.BingoInfo bi = new PvpSyncMessage.BingoInfo();
               bi.targetName = entry.targetName;
               bi.targetVillage = entry.targetVillage;
               bi.reward = entry.ninjaXpReward;
               bi.pvpXpReward = entry.pvpXpReward;
               bi.ryoReward = entry.ryoReward;
               bi.claimed = entry.claimedBy.contains(playerId);
               bingoEntries.add(bi);
            }
         } catch (Exception var16) {
         }

         long pvpXp = data.getPvpXp(playerId);
         byte pvpRank = (byte)data.getPvpRank(playerId).ordinal();
         VillageHelper.Village village = VillageHelper.getVillage(player);
         UUID kageId = KageManager.getInstance().getKage(village);
         boolean isKage = kageId != null && kageId.equals(playerId);
         String kageVillage = isKage ? village.villageName : "";
         PvpSyncMessage msg = new PvpSyncMessage(activeMissions, offers, bingoEntries, pvpXp, pvpRank, isKage, kageVillage);
         PvpModInit.NETWORK.sendTo(msg, player);
         sendLeadershipSync(player);
         sendTournamentSync(player);
         sendWarSync(player);
      }
   }

   public static void sendLeadershipSync(EntityPlayerMP player) {
      if (PvpModInit.NETWORK != null) {
         UUID playerId = player.getUniqueID();
         VillageHelper.Village village = VillageHelper.getVillage(player);
         AdvisorManager am = AdvisorManager.getInstance();
         KageManager km = KageManager.getInstance();
         PvpSavedData data = PvpSavedData.get(player.world);
         LeadershipSyncMessage msg = new LeadershipSyncMessage();
         boolean isAdvisor = am.isAdvisor(playerId);
         byte advisorOrdinal = -1;
         if (isAdvisor) {
            VillageHelper.Village av = am.getAdvisorVillage(playerId);
            if (av != null) {
               advisorOrdinal = (byte)av.ordinal();
            }
         }

         msg.setAdvisor(isAdvisor, advisorOrdinal);
         msg.setWarAuthority(am.hasLeadershipAuthority(playerId, village));
         if (village != VillageHelper.Village.UNKNOWN) {
            List<VillageOrder> orders = VillageOrderManager.getInstance().getActiveOrders(village.teamName, player.world);
            List<LeadershipSyncMessage.OrderInfo> orderInfos = new ArrayList();

            for(VillageOrder order : orders) {
               String assignedTemplateId = "";
               String targetName;
               switch (order.getOrderType()) {
                  case TARGET_PLAYER:
                     targetName = order.getTargetPlayerName() != null ? order.getTargetPlayerName() : "Unknown";
                     break;
                  case ASSIGN_MISSION:
                     targetName = order.getAssignedMissionName() != null ? order.getAssignedMissionName() : "Unknown";
                     assignedTemplateId = order.getAssignedTemplateId() != null ? order.getAssignedTemplateId() : "";
                     break;
                  default:
                     targetName = order.getTargetVillage() != null ? order.getTargetVillage() : "Unknown";
               }

               orderInfos.add(new LeadershipSyncMessage.OrderInfo(order.getOrderId(), (byte)order.getOrderType().ordinal(), targetName, order.getExpiresAt() - System.currentTimeMillis(), order.getBonusPvpXp(), order.getIssuerName(), assignedTemplateId));
            }

            msg.setActiveOrders(orderInfos);
            List<LeadershipSyncMessage.OperationProgressInfo> opProgressList = new ArrayList();

            for(VillageOrder order : orders) {
               if (order.getOrderType() == VillageOrder.OrderType.ASSIGN_MISSION) {
                  OperationTracker tracker = data.getOperationTracker(order.getOrderId());
                  if (tracker != null) {
                     byte bitmask = 0;
                     QuestDefinition.QuestRank[] ranks = QuestDefinition.QuestRank.values();
                     int totalCompletions = 0;

                     for(int r = 0; r < Math.min(ranks.length, 6); ++r) {
                        if (tracker.hasTierCompletion(ranks[r])) {
                           bitmask = (byte)(bitmask | 1 << r);
                           ++totalCompletions;
                        }
                     }

                     opProgressList.add(new LeadershipSyncMessage.OperationProgressInfo(order.getOrderId(), order.getAssignedTemplateId() != null ? order.getAssignedTemplateId() : "", bitmask, tracker.getParticipants().size(), totalCompletions));
                  }
               }
            }

            msg.setOperationProgress(opProgressList);
         }

         if (am.isLeadership(playerId)) {
            PvpMissionInstance leaderInst = data.getLeadershipInstance(playerId);
            if (leaderInst != null) {
               msg.setLeadershipMission(leaderInst.getName(), leaderInst.getProgressText(), leaderInst.getUniqueVictimsKilled().size(), leaderInst.getObjective().getKillsRequired(), (byte)leaderInst.getRank().ordinal());
            } else {
               PvpMissionOffer leaderOffer = PvpManager.getInstance().getLeadershipOffer(playerId, player.world);
               if (leaderOffer != null) {
                  msg.setLeadershipOffer(leaderOffer.getName(), leaderOffer.getDescription(), (byte)leaderOffer.getRank().ordinal(), leaderOffer.getObjective().getKillsRequired(), leaderOffer.getNinjaXpReward(), leaderOffer.getPvpXpReward(), leaderOffer.getRyoReward());
               }
            }
         }

         if (village != VillageHelper.Village.UNKNOWN) {
            UUID kageId = km.getKage(village);
            if (kageId != null) {
               EntityPlayerMP kagePlayer = km.getKagePlayer(village);
               msg.setKageName(kagePlayer != null ? kagePlayer.getName() : kageId.toString().substring(0, 8) + " (offline)");
            } else {
               msg.setKageName("None");
            }

            List<UUID> advisors = am.getAdvisors(village);
            List<String> advisorNamesList = new ArrayList();

            for(UUID aid : advisors) {
               EntityPlayerMP ap = player.getServer().getPlayerList().getPlayerByUUID(aid);
               advisorNamesList.add(ap != null ? ap.getName() : aid.toString().substring(0, 8) + " (offline)");
            }

            msg.setAdvisorNames(advisorNamesList);
         }

         PvpModInit.NETWORK.sendTo(msg, player);
      }
   }

   public static void sendTournamentSync(EntityPlayerMP player) {
      if (PvpModInit.NETWORK != null) {
         UUID playerId = player.getUniqueID();
         VillageHelper.Village village = VillageHelper.getVillage(player);
         TournamentManager tm = TournamentManager.getInstance();
         TournamentInstance inst = tm.getTournamentForVillage(village.teamName);
         if (inst == null) {
            for(TournamentInstance t : tm.getAllTournaments().values()) {
               if (t.hasParticipant(playerId)) {
                  inst = t;
                  break;
               }

               if (!t.isVillageOnly() && t.getState() == TournamentState.SIGNUP) {
                  inst = t;
               }
            }
         }

         TournamentSyncMessage msg;
         if (inst == null) {
            msg = TournamentSyncMessage.noTournament();
         } else {
            msg = new TournamentSyncMessage();
            msg.setTournamentInfo((byte)inst.getState().ordinal(), inst.getHostName(), inst.getParticipantCount(), inst.getSignupEndTime(), (byte)inst.getMinRankOrdinal(), (byte)inst.getMaxRankOrdinal(), inst.hasParticipant(playerId), inst.getMatchTimeoutSeconds(), inst.isVillageOnly());
            TournamentBracket bracket = inst.getBracket();
            if (bracket != null && inst.getState().ordinal() >= TournamentState.BRACKET_READY.ordinal()) {
               List<TournamentSyncMessage.MatchClientInfo> matchInfos = new ArrayList();

               for(TournamentMatch match : bracket.getMatches()) {
                  String winnerName = "";
                  if (match.getWinnerId() != null) {
                     if (match.getWinnerId().equals(match.getPlayer1Id())) {
                        winnerName = match.getPlayer1Name();
                     } else {
                        winnerName = match.getPlayer2Name();
                     }
                  }

                  matchInfos.add(new TournamentSyncMessage.MatchClientInfo(match.getMatchIndex(), match.getRound(), match.getPosition(), match.getPlayer1Name(), match.getPlayer2Name(), (byte)match.getMatchState().ordinal(), winnerName));
               }

               msg.setBracketData(bracket.getRounds(), inst.getCurrentRound(), matchInfos);
            }

            TournamentMatch activeMatch = inst.getMatchForPlayer(playerId);
            if (activeMatch != null && (activeMatch.getMatchState() == TournamentMatch.MatchState.ACTIVE || activeMatch.getMatchState() == TournamentMatch.MatchState.COUNTDOWN)) {
               UUID opponentId = activeMatch.getOpponent(playerId);
               String opponentName;
               if (opponentId != null && opponentId.equals(activeMatch.getPlayer1Id())) {
                  opponentName = activeMatch.getPlayer1Name();
               } else {
                  opponentName = activeMatch.getPlayer2Name();
               }

               msg.setActiveMatchInfo(opponentName, System.currentTimeMillis());
            }
         }

         PvpModInit.NETWORK.sendTo(msg, player);
      }
   }

   public static void sendTargetLocations(EntityPlayerMP player) {
      if (PvpModInit.NETWORK != null) {
         UUID playerId = player.getUniqueID();
         PvpSavedData data = PvpSavedData.get(player.world);
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            List<PvpTargetLocationMessage.TargetEntry> targets = new ArrayList();
            Map<String, PvpMissionInstance> instances = data.getActiveInstances(playerId);

            for(Map.Entry<String, PvpMissionInstance> entry : instances.entrySet()) {
               PvpMissionInstance inst = (PvpMissionInstance)entry.getValue();
               if (!inst.isCompleted() && !inst.isFailed()) {
                  UUID targetId = inst.getMutualTarget();
                  if (targetId != null) {
                     EntityPlayerMP target = server.getPlayerList().getPlayerByUUID(targetId);
                     if (target != null) {
                        int fuzzyX = (int)target.posX + (RANDOM.nextInt(301) - 150);
                        int fuzzyZ = (int)target.posZ + (RANDOM.nextInt(301) - 150);
                        byte beaconType = 0;
                        if (inst.getObjective().getType().name().contains("MUTUAL")) {
                           beaconType = 2;
                        }

                        targets.add(new PvpTargetLocationMessage.TargetEntry(inst.getInstanceId(), fuzzyX, fuzzyZ, beaconType));
                     }
                  }
               }
            }

            UUID mutualTarget = data.getMutualHuntTarget(playerId);
            if (mutualTarget != null) {
               EntityPlayerMP target = server.getPlayerList().getPlayerByUUID(mutualTarget);
               if (target != null) {
                  int fuzzyX = (int)target.posX + (RANDOM.nextInt(301) - 150);
                  int fuzzyZ = (int)target.posZ + (RANDOM.nextInt(301) - 150);
                  targets.add(new PvpTargetLocationMessage.TargetEntry("mutual_" + mutualTarget, fuzzyX, fuzzyZ, (byte)2));
               }
            }

            if (!targets.isEmpty()) {
               PvpModInit.NETWORK.sendTo(new PvpTargetLocationMessage(targets), player);
            }

         }
      }
   }

   public static void sendWarSync(EntityPlayerMP player) {
      if (PvpModInit.NETWORK != null) {
         UUID playerId = player.getUniqueID();
         VillageHelper.Village village = VillageHelper.getVillage(player);
         KageManager km = KageManager.getInstance();
         AdvisorManager am = AdvisorManager.getInstance();
         WarManager warManager = WarManager.getInstance();
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         List<WarSyncMessage.WarInfo> warInfos = new ArrayList();

         for(WarInstance war : warManager.getAllActiveWars()) {
            WarSyncMessage.WarInfo info = new WarSyncMessage.WarInfo();
            info.warId = war.getWarId();
            info.mode = (byte)war.getMode().ordinal();
            info.village1 = (byte)war.getVillage1().ordinal();
            info.village2 = (byte)war.getVillage2().ordinal();
            info.score1 = war.getScore1();
            info.score2 = war.getScore2();
            info.timeRemainingMs = war.getRemainingMs();
            info.topKillers1 = new ArrayList();

            for(Map.Entry<UUID, Integer> entry : war.getTopKillers(1, 5)) {
               WarSyncMessage.TopKiller tk = new WarSyncMessage.TopKiller();
               if (server != null) {
                  EntityPlayerMP p = server.getPlayerList().getPlayerByUUID((UUID)entry.getKey());
                  tk.playerName = p != null ? p.getName() : ((UUID)entry.getKey()).toString().substring(0, 8);
               } else {
                  tk.playerName = ((UUID)entry.getKey()).toString().substring(0, 8);
               }

               tk.kills = (Integer)entry.getValue();
               info.topKillers1.add(tk);
            }

            info.topKillers2 = new ArrayList();

            for(Map.Entry<UUID, Integer> entry : war.getTopKillers(2, 5)) {
               WarSyncMessage.TopKiller tk = new WarSyncMessage.TopKiller();
               if (server != null) {
                  EntityPlayerMP p = server.getPlayerList().getPlayerByUUID((UUID)entry.getKey());
                  tk.playerName = p != null ? p.getName() : ((UUID)entry.getKey()).toString().substring(0, 8);
               } else {
                  tk.playerName = ((UUID)entry.getKey()).toString().substring(0, 8);
               }

               tk.kills = (Integer)entry.getValue();
               info.topKillers2.add(tk);
            }

            WarLobby lobby = war.getLobby();
            if (lobby == null) {
               info.hasLobby = false;
               info.roster1Names = new ArrayList();
               info.roster2Names = new ArrayList();
            } else {
               info.hasLobby = true;
               info.lobbyState = (byte)lobby.getState().ordinal();
               info.roster1Names = new ArrayList();

               for(UUID uid : lobby.getRoster1()) {
                  if (server != null) {
                     EntityPlayerMP p = server.getPlayerList().getPlayerByUUID(uid);
                     info.roster1Names.add(p != null ? p.getName() : uid.toString().substring(0, 8));
                  } else {
                     info.roster1Names.add(uid.toString().substring(0, 8));
                  }
               }

               info.roster2Names = new ArrayList();

               for(UUID uid : lobby.getRoster2()) {
                  if (server != null) {
                     EntityPlayerMP p = server.getPlayerList().getPlayerByUUID(uid);
                     info.roster2Names.add(p != null ? p.getName() : uid.toString().substring(0, 8));
                  } else {
                     info.roster2Names.add(uid.toString().substring(0, 8));
                  }
               }

               info.acceptedCount1 = lobby.getAccepted1().size();
               info.acceptedCount2 = lobby.getAccepted2().size();
               long challengeRemaining = 900000L - (System.currentTimeMillis() - lobby.getChallengeTimeMs());
               info.challengeTimeRemaining = Math.max(0L, challengeRemaining);
               info.locked1 = lobby.isLocked1();
               info.locked2 = lobby.isLocked2();
            }

            info.currentRound = war.getCurrentRound();
            info.roundsWon1 = war.getRoundsWon1();
            info.roundsWon2 = war.getRoundsWon2();
            warInfos.add(info);
         }

         UUID kageId = km.getKage(village);
         boolean isKage = kageId != null && kageId.equals(playerId);
         boolean hasAuthority = am.hasLeadershipAuthority(playerId, village);
         byte kageVillageByte = !isKage && !hasAuthority ? -1 : (byte)village.ordinal();
         long cooldown = warManager.getRemainingCooldown(village);
         WarSyncMessage msg = new WarSyncMessage(warInfos, isKage, kageVillageByte, cooldown);
         msg.setAdvisorInfo(am.isAdvisor(playerId), am.hasLeadershipAuthority(playerId, village));
         PvpModInit.NETWORK.sendTo(msg, player);
      }
   }

   public static void sendWarSyncToAll() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            sendWarSync(player);
         }

      }
   }
}
