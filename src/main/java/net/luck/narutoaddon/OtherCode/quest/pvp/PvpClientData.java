package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.tournament.TournamentState;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.WarMode;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.WarSyncMessage;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.WarZoneSyncMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

@SideOnly(Side.CLIENT)
public class PvpClientData {
   private static final Map<String, ActivePvpMissionInfo> activeMissions = new HashMap();
   private static final Map<String, PvpOfferInfo> currentOffers = new HashMap();
   private static final List<BingoInfo> bingoEntries = new ArrayList();
   public static long playerPvpXp = 0L;
   public static int playerPvpRankOrdinal = 0;
   public static boolean isKage = false;
   public static String kageVillage = "";
   public static boolean isAdvisor = false;
   public static String advisorVillage = "";
   public static boolean hasWarAuthority = false;
   private static List<OrderClientInfo> activeOrders = new ArrayList();
   private static List<OperationProgressInfo> operationProgress = new ArrayList();
   public static LeadershipMissionClientInfo leadershipMission = null;
   public static LeadershipOfferClientInfo leadershipOffer = null;
   public static String kageName = "";
   private static List<String> advisorNames = new ArrayList();
   public static TournamentClientInfo tournamentInfo = null;
   private static final List<WarClientInfo> activeWars = new ArrayList();
   private static final List<ZoneClientInfo> warZones = new ArrayList();
   public static long warCooldownRemainingMs = 0L;
   private static final List<TargetPosInfo> targetPositions = new ArrayList();
   private static final String[] PVP_RANK_NAMES = new String[]{"Genin", "Chunin", "Tokubetsu Jonin", "Jonin", "ANBU", "ANBU Captain"};
   private static final long[] PVP_RANK_THRESHOLDS = new long[]{0L, 500L, 2000L, 6000L, 15000L, 40000L};
   public static final String[] PVP_DAILY_SLOTS = new String[]{"pvp_daily_0", "pvp_daily_1", "pvp_daily_2"};
   public static final String[] PVP_WEEKLY_SLOTS = new String[]{"pvp_weekly_0", "pvp_weekly_1", "pvp_weekly_2"};
   public static final String PVP_RANDOM_SLOT = "pvp_random";

   public static List<OrderClientInfo> getActiveOrders() {
      return new ArrayList(activeOrders);
   }

   public static void setActiveOrders(List<OrderClientInfo> orders) {
      activeOrders = new ArrayList(orders);
   }

   public static List<String> getAdvisorNames() {
      return new ArrayList(advisorNames);
   }

   public static void setAdvisorNames(List<String> names) {
      advisorNames = new ArrayList(names);
   }

   public static List<OperationProgressInfo> getOperationProgress() {
      return new ArrayList(operationProgress);
   }

   public static void setOperationProgress(List<OperationProgressInfo> progress) {
      operationProgress = new ArrayList(progress);
   }

   public static OperationProgressInfo getOperationProgressForOrder(String orderId) {
      for(OperationProgressInfo op : operationProgress) {
         if (op.orderId.equals(orderId)) {
            return op;
         }
      }

      return null;
   }

   public static boolean hasActiveMission(String subSlot) {
      return activeMissions.containsKey(subSlot);
   }

   public static ActivePvpMissionInfo getActiveMission(String subSlot) {
      return (ActivePvpMissionInfo)activeMissions.get(subSlot);
   }

   public static Map<String, ActivePvpMissionInfo> getAllActiveMissions() {
      return Collections.unmodifiableMap(activeMissions);
   }

   public static void setActiveMission(String subSlot, ActivePvpMissionInfo info) {
      activeMissions.put(subSlot, info);
   }

   public static void clearActiveMission(String subSlot) {
      activeMissions.remove(subSlot);
   }

   public static boolean hasOffer(String subSlot) {
      return currentOffers.containsKey(subSlot);
   }

   public static PvpOfferInfo getOffer(String subSlot) {
      return (PvpOfferInfo)currentOffers.get(subSlot);
   }

   public static Map<String, PvpOfferInfo> getAllOffers() {
      return Collections.unmodifiableMap(currentOffers);
   }

   public static void setOffer(String subSlot, PvpOfferInfo info) {
      currentOffers.put(subSlot, info);
   }

   public static void clearOffer(String subSlot) {
      currentOffers.remove(subSlot);
   }

   public static List<BingoInfo> getBingoEntries() {
      return Collections.unmodifiableList(bingoEntries);
   }

   public static void setBingoEntries(List<BingoInfo> newEntries) {
      bingoEntries.clear();
      bingoEntries.addAll(newEntries);
   }

   public static void clearBingoEntries() {
      bingoEntries.clear();
   }

   public static List<WarClientInfo> getActiveWars() {
      return Collections.unmodifiableList(activeWars);
   }

   public static void setActiveWars(List<WarClientInfo> wars) {
      activeWars.clear();
      activeWars.addAll(wars);
   }

   public static void clearActiveWars() {
      activeWars.clear();
   }

   public static WarClientInfo getActiveWarForVillage(String village) {
      for(WarClientInfo war : activeWars) {
         if (war.involvesVillage(village)) {
            return war;
         }
      }

      return null;
   }

   public static boolean isInWarRoster() {
      if (kageVillage != null && !kageVillage.isEmpty()) {
         for(WarClientInfo war : activeWars) {
            if (war.involvesVillage(kageVillage)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static List<ZoneClientInfo> getWarZones() {
      return Collections.unmodifiableList(warZones);
   }

   public static void setWarZones(List<ZoneClientInfo> zones) {
      warZones.clear();
      warZones.addAll(zones);
   }

   public static void clearWarZones() {
      warZones.clear();
   }

   public static List<TargetPosInfo> getTargetPositions() {
      return Collections.unmodifiableList(targetPositions);
   }

   public static void setTargetPositions(List<TargetPosInfo> positions) {
      targetPositions.clear();
      targetPositions.addAll(positions);
   }

   public static void clearTargetPositions() {
      targetPositions.clear();
   }

   public static void clear() {
      activeMissions.clear();
      currentOffers.clear();
      bingoEntries.clear();
      playerPvpXp = 0L;
      playerPvpRankOrdinal = 0;
      isKage = false;
      kageVillage = "";
      isAdvisor = false;
      advisorVillage = "";
      hasWarAuthority = false;
      activeOrders.clear();
      operationProgress.clear();
      leadershipMission = null;
      leadershipOffer = null;
      kageName = "";
      advisorNames.clear();
      activeWars.clear();
      warZones.clear();
      warCooldownRemainingMs = 0L;
      targetPositions.clear();
      tournamentInfo = null;
   }

   public static String getSubSlotDisplayName(String subSlot) {
      if (subSlot == null) {
         return "PvP";
      } else {
         switch (subSlot) {
            case "pvp_daily_0":
               return "PvP Daily 1";
            case "pvp_daily_1":
               return "PvP Daily 2";
            case "pvp_daily_2":
               return "PvP Daily 3";
            case "pvp_weekly_0":
               return "PvP Weekly 1";
            case "pvp_weekly_1":
               return "PvP Weekly 2";
            case "pvp_weekly_2":
               return "PvP Weekly 3";
            case "pvp_random":
               return "PvP Random";
            case "pvp_assigned":
               return "Assigned Mission";
            default:
               return "PvP " + subSlot;
         }
      }
   }

   public static String getPvpRankName() {
      return playerPvpRankOrdinal >= 0 && playerPvpRankOrdinal < PVP_RANK_NAMES.length ? PVP_RANK_NAMES[playerPvpRankOrdinal] : "Genin";
   }

   private static String rankNameFromOrdinal(int ordinal) {
      QuestDefinition.QuestRank[] ranks = QuestDefinition.QuestRank.values();
      return ordinal >= 0 && ordinal < ranks.length ? ranks[ordinal].displayName : "D";
   }

   public static int getPvpRankColor() {
      QuestDefinition.QuestRank[] ranks = QuestDefinition.QuestRank.values();
      return playerPvpRankOrdinal >= 0 && playerPvpRankOrdinal < ranks.length ? ranks[playerPvpRankOrdinal].color : -11141291;
   }

   public static long getPvpRankThreshold(int rankOrdinal) {
      if (rankOrdinal < 0) {
         return 0L;
      } else {
         return rankOrdinal >= PVP_RANK_THRESHOLDS.length ? PVP_RANK_THRESHOLDS[PVP_RANK_THRESHOLDS.length - 1] : PVP_RANK_THRESHOLDS[rankOrdinal];
      }
   }

   public static void setWarData(List<WarSyncMessage.WarInfo> warInfos, boolean kageStatus, byte kageVillageOrdinal, long cooldownRemaining) {
      List<WarClientInfo> clientWars = new ArrayList();
      VillageHelper.Village[] villages = VillageHelper.Village.values();

      for(WarSyncMessage.WarInfo info : warInfos) {
         String v1Name = info.village1 >= 0 && info.village1 < villages.length ? villages[info.village1].villageName : "Unknown";
         String v2Name = info.village2 >= 0 && info.village2 < villages.length ? villages[info.village2].villageName : "Unknown";
         List<TopKillerInfo> tk1 = new ArrayList();
         if (info.topKillers1 != null) {
            for(WarSyncMessage.TopKiller tk : info.topKillers1) {
               tk1.add(new TopKillerInfo(tk.playerName, tk.kills));
            }
         }

         List<TopKillerInfo> tk2 = new ArrayList();
         if (info.topKillers2 != null) {
            for(WarSyncMessage.TopKiller tk : info.topKillers2) {
               tk2.add(new TopKillerInfo(tk.playerName, tk.kills));
            }
         }

         int lobbyState = info.hasLobby ? info.lobbyState + 1 : 0;
         List<String> r1 = (List<String>)(info.roster1Names != null ? info.roster1Names : new ArrayList());
         List<String> r2 = (List<String>)(info.roster2Names != null ? info.roster2Names : new ArrayList());
         clientWars.add(new WarClientInfo(info.warId, info.mode, v1Name, v2Name, info.score1, info.score2, info.timeRemainingMs, tk1, tk2, lobbyState, info.currentRound, info.roundsWon1, info.roundsWon2, r1, r2, info.acceptedCount1, info.acceptedCount2, info.hasLobby ? info.challengeTimeRemaining : 0L, info.locked1, info.locked2));
      }

      setActiveWars(clientWars);
      isKage = kageStatus;
      if (kageVillageOrdinal >= 0 && kageVillageOrdinal < villages.length) {
         kageVillage = villages[kageVillageOrdinal].villageName;
      } else {
         kageVillage = "";
      }

      warCooldownRemainingMs = cooldownRemaining;
   }

   public static void setWarZoneData(String warId, List<WarZoneSyncMessage.ZoneData> zoneDataList) {
      VillageHelper.Village[] villages = VillageHelper.Village.values();
      List<ZoneClientInfo> clientZones = new ArrayList();

      for(WarZoneSyncMessage.ZoneData zd : zoneDataList) {
         String controlling = "";
         if (zd.controllingVillage > 0) {
            controlling = resolveVillageFromWarSide(warId, zd.controllingVillage);
         }

         String capturing = "";
         if (zd.capturingVillage > 0) {
            capturing = resolveVillageFromWarSide(warId, zd.capturingVillage);
         }

         clientZones.add(new ZoneClientInfo(zd.zoneIndex, controlling, (double)zd.captureProgress, capturing, zd.contested, zd.centerX, zd.centerZ, zd.radius));
      }

      setWarZones(clientZones);
   }

   private static String resolveVillageFromWarSide(String warId, int side) {
      for(WarClientInfo war : activeWars) {
         if (war.warId.equals(warId)) {
            return side == 1 ? war.village1 : war.village2;
         }
      }

      return side == 1 ? "Village 1" : "Village 2";
   }

   public static String getBaseCategory(String subSlot) {
      if (subSlot != null && subSlot.startsWith("pvp_daily")) {
         return "pvp_daily";
      } else {
         return subSlot != null && subSlot.startsWith("pvp_weekly") ? "pvp_weekly" : subSlot;
      }
   }

   public static class ActivePvpMissionInfo {
      public final String name;
      public final String description;
      public final int progress;
      public final int killsRequired;
      public final int rankOrdinal;
      public final long startTimeMs;
      public final long timeLimitMs;

      public ActivePvpMissionInfo(String name, String description, int progress, int killsRequired, int rankOrdinal, long startTimeMs, long timeLimitMs) {
         this.name = name;
         this.description = description;
         this.progress = progress;
         this.killsRequired = killsRequired;
         this.rankOrdinal = rankOrdinal;
         this.startTimeMs = startTimeMs;
         this.timeLimitMs = timeLimitMs;
      }

      public String getRankName() {
         return PvpClientData.rankNameFromOrdinal(this.rankOrdinal);
      }

      public boolean isTimeLimited() {
         return this.timeLimitMs > 0L;
      }
   }

   public static class PvpOfferInfo {
      public final String name;
      public final String description;
      public final int rankOrdinal;
      public final int killCount;
      public final int ninjaXpReward;
      public final int pvpXpReward;
      public final int ryoReward;
      public final int rerollsRemaining;
      public final long cooldownRemainingMs;

      public PvpOfferInfo(String name, String description, int rankOrdinal, int killCount, int ninjaXpReward, int pvpXpReward, int ryoReward, int rerollsRemaining, long cooldownRemainingMs) {
         this.name = name;
         this.description = description;
         this.rankOrdinal = rankOrdinal;
         this.killCount = killCount;
         this.ninjaXpReward = ninjaXpReward;
         this.pvpXpReward = pvpXpReward;
         this.ryoReward = ryoReward;
         this.rerollsRemaining = rerollsRemaining;
         this.cooldownRemainingMs = cooldownRemainingMs;
      }

      public String getRankName() {
         return PvpClientData.rankNameFromOrdinal(this.rankOrdinal);
      }

      public boolean isOnCooldown() {
         return this.cooldownRemainingMs > 0L;
      }
   }

   public static class BingoInfo {
      public final String targetName;
      public final String targetVillage;
      public final int ninjaXpReward;
      public final boolean claimed;
      public final int pvpXpReward;
      public final int ryoReward;

      public BingoInfo(String targetName, String targetVillage, int ninjaXpReward, boolean claimed, int pvpXpReward, int ryoReward) {
         this.targetName = targetName;
         this.targetVillage = targetVillage;
         this.ninjaXpReward = ninjaXpReward;
         this.claimed = claimed;
         this.pvpXpReward = pvpXpReward;
         this.ryoReward = ryoReward;
      }
   }

   public static class WarClientInfo {
      public final String warId;
      public final int modeOrdinal;
      public final String village1;
      public final String village2;
      public final int score1;
      public final int score2;
      public final long timeRemainingMs;
      public final List<TopKillerInfo> topKillers1;
      public final List<TopKillerInfo> topKillers2;
      public final int lobbyState;
      public final int currentRound;
      public final int roundsWon1;
      public final int roundsWon2;
      public final List<String> roster1Names;
      public final List<String> roster2Names;
      public final int acceptedCount1;
      public final int acceptedCount2;
      public final long challengeTimeRemaining;
      public final boolean locked1;
      public final boolean locked2;

      public WarClientInfo(String warId, int modeOrdinal, String village1, String village2, int score1, int score2, long timeRemainingMs, List<TopKillerInfo> topKillers1, List<TopKillerInfo> topKillers2, int lobbyState, int currentRound, int roundsWon1, int roundsWon2) {
         this(warId, modeOrdinal, village1, village2, score1, score2, timeRemainingMs, topKillers1, topKillers2, lobbyState, currentRound, roundsWon1, roundsWon2, new ArrayList(), new ArrayList(), 0, 0, 0L, false, false);
      }

      public WarClientInfo(String warId, int modeOrdinal, String village1, String village2, int score1, int score2, long timeRemainingMs, List<TopKillerInfo> topKillers1, List<TopKillerInfo> topKillers2, int lobbyState, int currentRound, int roundsWon1, int roundsWon2, List<String> roster1Names, List<String> roster2Names, int acceptedCount1, int acceptedCount2, long challengeTimeRemaining, boolean locked1, boolean locked2) {
         this.warId = warId;
         this.modeOrdinal = modeOrdinal;
         this.village1 = village1;
         this.village2 = village2;
         this.score1 = score1;
         this.score2 = score2;
         this.timeRemainingMs = timeRemainingMs;
         this.topKillers1 = topKillers1;
         this.topKillers2 = topKillers2;
         this.lobbyState = lobbyState;
         this.currentRound = currentRound;
         this.roundsWon1 = roundsWon1;
         this.roundsWon2 = roundsWon2;
         this.roster1Names = roster1Names;
         this.roster2Names = roster2Names;
         this.acceptedCount1 = acceptedCount1;
         this.acceptedCount2 = acceptedCount2;
         this.challengeTimeRemaining = challengeTimeRemaining;
         this.locked1 = locked1;
         this.locked2 = locked2;
      }

      public String getModeName() {
         try {
            return WarMode.fromOrdinal(this.modeOrdinal).displayName;
         } catch (Exception var2) {
            return "Unknown";
         }
      }

      public boolean involvesVillage(String village) {
         return this.village1.equals(village) || this.village2.equals(village);
      }

      public boolean isMyRosterLocked(String village) {
         if (this.village1.equals(village)) {
            return this.locked1;
         } else {
            return this.village2.equals(village) ? this.locked2 : false;
         }
      }
   }

   public static class ZoneClientInfo {
      public final int zoneIndex;
      public final String controllingVillage;
      public final double captureProgress;
      public final String capturingVillage;
      public final boolean contested;
      public final int centerX;
      public final int centerZ;
      public final int radius;

      public ZoneClientInfo(int zoneIndex, String controllingVillage, double captureProgress, String capturingVillage, boolean contested, int centerX, int centerZ, int radius) {
         this.zoneIndex = zoneIndex;
         this.controllingVillage = controllingVillage;
         this.captureProgress = captureProgress;
         this.capturingVillage = capturingVillage;
         this.contested = contested;
         this.centerX = centerX;
         this.centerZ = centerZ;
         this.radius = radius;
      }
   }

   public static class TargetPosInfo {
      public final String missionId;
      public final int x;
      public final int z;
      public final int beaconType;

      public TargetPosInfo(String missionId, int x, int z, int beaconType) {
         this.missionId = missionId;
         this.x = x;
         this.z = z;
         this.beaconType = beaconType;
      }
   }

   public static class TopKillerInfo {
      public final String playerName;
      public final int kills;

      public TopKillerInfo(String playerName, int kills) {
         this.playerName = playerName;
         this.kills = kills;
      }
   }

   public static class OrderClientInfo {
      public final String orderId;
      public final byte orderType;
      public final String targetName;
      public final long timeRemainingMs;
      public final int bonusPvpXp;
      public final String issuerName;
      public final String assignedTemplateId;

      public OrderClientInfo(String orderId, byte orderType, String targetName, long timeRemainingMs, int bonusPvpXp, String issuerName) {
         this(orderId, orderType, targetName, timeRemainingMs, bonusPvpXp, issuerName, "");
      }

      public OrderClientInfo(String orderId, byte orderType, String targetName, long timeRemainingMs, int bonusPvpXp, String issuerName, String assignedTemplateId) {
         this.orderId = orderId;
         this.orderType = orderType;
         this.targetName = targetName;
         this.timeRemainingMs = timeRemainingMs;
         this.bonusPvpXp = bonusPvpXp;
         this.issuerName = issuerName;
         this.assignedTemplateId = assignedTemplateId != null ? assignedTemplateId : "";
      }

      public boolean isAssignedMission() {
         return this.orderType == 2;
      }
   }

   public static class LeadershipMissionClientInfo {
      public final String name;
      public final String description;
      public final int progress;
      public final int killsRequired;
      public final byte rank;

      public LeadershipMissionClientInfo(String name, String description, int progress, int killsRequired, byte rank) {
         this.name = name;
         this.description = description;
         this.progress = progress;
         this.killsRequired = killsRequired;
         this.rank = rank;
      }
   }

   public static class LeadershipOfferClientInfo {
      public final String name;
      public final String description;
      public final byte rank;
      public final int killCount;
      public final int ninjaXpReward;
      public final int pvpXpReward;
      public final int ryoReward;

      public LeadershipOfferClientInfo(String name, String description, byte rank, int killCount, int ninjaXpReward, int pvpXpReward, int ryoReward) {
         this.name = name;
         this.description = description;
         this.rank = rank;
         this.killCount = killCount;
         this.ninjaXpReward = ninjaXpReward;
         this.pvpXpReward = pvpXpReward;
         this.ryoReward = ryoReward;
      }
   }

   public static class OperationProgressInfo {
      public final String orderId;
      public final String templateId;
      public final byte completedTiersBitmask;
      public final int participantCount;
      public final int totalCompletions;

      public OperationProgressInfo(String orderId, String templateId, byte bitmask, int participantCount, int totalCompletions) {
         this.orderId = orderId;
         this.templateId = templateId;
         this.completedTiersBitmask = bitmask;
         this.participantCount = participantCount;
         this.totalCompletions = totalCompletions;
      }

      public boolean isTierComplete(int rankOrdinal) {
         return (this.completedTiersBitmask & 1 << rankOrdinal) != 0;
      }

      public int getCompletedTierCount() {
         return Integer.bitCount(this.completedTiersBitmask & 255);
      }
   }

   public static class TournamentClientInfo {
      public final byte state;
      public final String hostName;
      public final int participantCount;
      public final long signupEndTime;
      public final byte minRank;
      public final byte maxRank;
      public final boolean isPlayerSignedUp;
      public final int matchTimeoutSeconds;
      public final boolean hasBracket;
      public final int totalRounds;
      public final int currentRound;
      public final List<MatchClientInfo> matches;
      public final boolean isInMatch;
      public final String opponentName;
      public final long matchStartTime;
      public final boolean villageOnly;

      public TournamentClientInfo(byte state, String hostName, int participantCount, long signupEndTime, byte minRank, byte maxRank, boolean isPlayerSignedUp, int matchTimeoutSeconds, boolean hasBracket, int totalRounds, int currentRound, List<MatchClientInfo> matches, boolean isInMatch, String opponentName, long matchStartTime, boolean villageOnly) {
         this.state = state;
         this.hostName = hostName;
         this.participantCount = participantCount;
         this.signupEndTime = signupEndTime;
         this.minRank = minRank;
         this.maxRank = maxRank;
         this.isPlayerSignedUp = isPlayerSignedUp;
         this.matchTimeoutSeconds = matchTimeoutSeconds;
         this.hasBracket = hasBracket;
         this.totalRounds = totalRounds;
         this.currentRound = currentRound;
         this.matches = (List<MatchClientInfo>)(matches != null ? matches : new ArrayList());
         this.isInMatch = isInMatch;
         this.opponentName = opponentName;
         this.matchStartTime = matchStartTime;
         this.villageOnly = villageOnly;
      }

      public String getStateName() {
         try {
            return TournamentState.fromOrdinal(this.state).displayName;
         } catch (Exception var2) {
            return "Unknown";
         }
      }
   }

   public static class MatchClientInfo {
      public final int matchIndex;
      public final int round;
      public final int position;
      public final String player1Name;
      public final String player2Name;
      public final byte matchState;
      public final String winnerName;

      public MatchClientInfo(int matchIndex, int round, int position, String player1Name, String player2Name, byte matchState, String winnerName) {
         this.matchIndex = matchIndex;
         this.round = round;
         this.position = position;
         this.player1Name = player1Name != null ? player1Name : "";
         this.player2Name = player2Name != null ? player2Name : "";
         this.matchState = matchState;
         this.winnerName = winnerName != null ? winnerName : "";
      }
   }
}
