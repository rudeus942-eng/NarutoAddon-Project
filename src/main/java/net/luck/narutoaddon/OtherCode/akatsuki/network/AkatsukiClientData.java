package net.luck.narutoaddon.OtherCode.akatsuki.network;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SideOnly(Side.CLIENT)
public class AkatsukiClientData {
   public static boolean isAkatsuki = false;
   public static boolean hasPendingInvite = false;
   public static boolean akatsukiExists = false;
   public static int rankOrdinal = 0;
   public static int ringOrdinal = 0;
   public static int reputation = 0;
   public static int bountyTokens = 0;
   public static int[] ringUpgradeLevels = new int[5];
   public static String partnerName = "";
   public static boolean criticalThreat = false;
   public static int akatsukiZoneCount = 0;
   public static int totalZoneCount = 0;
   public static float akatsukiThreatPercent = 0.0F;
   public static List<BountyClientEntry> bounties = new CopyOnWriteArrayList();
   public static List<String> activityLog = new CopyOnWriteArrayList();
   public static List<RosterEntry> roster = new CopyOnWriteArrayList();
   public static int allianceScore = 0;
   public static List<MissionClientEntry> activeMissions = new CopyOnWriteArrayList();
   public static List<MissionOfferEntry> missionOffers = new CopyOnWriteArrayList();
   public static boolean raidActive = false;
   public static String raidTargetVillage = "";
   public static int raidRallyX = 0;
   public static int raidRallyZ = 0;
   public static long raidTimeRemainingMs = 0L;
   public static int raidKillsAchieved = 0;
   public static int raidKillsRequired = 0;
   public static String raidStatus = "";
   public static int raidParticipantCount = 0;
   public static boolean raidIsMyVillage = false;
   public static long raidSyncTime = 0L;
   public static List<ContractClientEntry> openContracts = new CopyOnWriteArrayList();
   public static List<ContractClientEntry> myContracts = new CopyOnWriteArrayList();
   public static List<ContractClientEntry> assignedContracts = new CopyOnWriteArrayList();
   public static boolean hasLeaderMission = false;
   public static String leaderMissionType = "";
   public static String leaderMissionDesc = "";
   public static String leaderMissionObjective = "";
   public static int leaderMissionTokenReward = 0;
   public static int leaderMissionRepReward = 0;
   public static boolean leaderMissionCompleted = false;
   public static int leaderMissionX = 0;
   public static int leaderMissionZ = 0;

   public static void reset() {
      isAkatsuki = false;
      hasPendingInvite = false;
      akatsukiExists = false;
      rankOrdinal = 0;
      ringOrdinal = 0;
      reputation = 0;
      bountyTokens = 0;
      ringUpgradeLevels = new int[5];
      partnerName = "";
      criticalThreat = false;
      akatsukiZoneCount = 0;
      totalZoneCount = 0;
      akatsukiThreatPercent = 0.0F;
      bounties = new CopyOnWriteArrayList();
      activityLog = new CopyOnWriteArrayList();
      roster = new CopyOnWriteArrayList();
      allianceScore = 0;
      activeMissions = new CopyOnWriteArrayList();
      missionOffers = new CopyOnWriteArrayList();
      openContracts = new CopyOnWriteArrayList();
      myContracts = new CopyOnWriteArrayList();
      assignedContracts = new CopyOnWriteArrayList();
      raidActive = false;
      raidTargetVillage = "";
      raidRallyX = 0;
      raidRallyZ = 0;
      raidTimeRemainingMs = 0L;
      raidKillsAchieved = 0;
      raidKillsRequired = 0;
      raidStatus = "";
      raidParticipantCount = 0;
      raidIsMyVillage = false;
      raidSyncTime = 0L;
      hasLeaderMission = false;
      leaderMissionType = "";
      leaderMissionDesc = "";
      leaderMissionObjective = "";
      leaderMissionTokenReward = 0;
      leaderMissionRepReward = 0;
      leaderMissionCompleted = false;
      leaderMissionX = 0;
      leaderMissionZ = 0;
   }

   public static class BountyClientEntry {
      public final String targetName;
      public final int amount;
      public final String lastKnownRegion;

      public BountyClientEntry(String targetName, int amount, String lastKnownRegion) {
         this.targetName = targetName;
         this.amount = amount;
         this.lastKnownRegion = lastKnownRegion;
      }
   }

   public static class MissionClientEntry {
      public final String templateId;
      public final String templateName;
      public final String description;
      public final String objectiveText;
      public final int targetX;
      public final int targetY;
      public final int targetZ;
      public final long startTime;
      public final boolean completed;
      public final int categoryOrdinal;
      public final int tokenReward;
      public final int repReward;
      public final int xpReward;
      public final int killsAchieved;
      public final int killsRequired;
      public final int missionStateOrdinal;
      public final int ryoReward;
      public final int pveXpReward;
      public final int currentStepIndex;
      public final int totalSteps;
      public final String currentStepObjective;
      public final int currentStepType;
      public final int scoutProgress;
      public final int scoutRequired;

      public MissionClientEntry(String templateId, String templateName, String description, String objectiveText, int targetX, int targetY, int targetZ, long startTime, boolean completed, int categoryOrdinal, int tokenReward, int repReward, int xpReward, int killsAchieved, int killsRequired, int missionStateOrdinal, int ryoReward, int pveXpReward, int currentStepIndex, int totalSteps, String currentStepObjective, int currentStepType, int scoutProgress, int scoutRequired) {
         this.templateId = templateId;
         this.templateName = templateName;
         this.description = description;
         this.objectiveText = objectiveText;
         this.targetX = targetX;
         this.targetY = targetY;
         this.targetZ = targetZ;
         this.startTime = startTime;
         this.completed = completed;
         this.categoryOrdinal = categoryOrdinal;
         this.tokenReward = tokenReward;
         this.repReward = repReward;
         this.xpReward = xpReward;
         this.killsAchieved = killsAchieved;
         this.killsRequired = killsRequired;
         this.missionStateOrdinal = missionStateOrdinal;
         this.ryoReward = ryoReward;
         this.pveXpReward = pveXpReward;
         this.currentStepIndex = currentStepIndex;
         this.totalSteps = totalSteps;
         this.currentStepObjective = currentStepObjective;
         this.currentStepType = currentStepType;
         this.scoutProgress = scoutProgress;
         this.scoutRequired = scoutRequired;
      }

      public String getMissionStateLabel() {
         switch (this.missionStateOrdinal) {
            case 0:
               return "§eTravel";
            case 1:
               return "§6Ready";
            case 2:
               return "§cFight!";
            case 3:
               return "§aVictory";
            case 4:
               return "§4Failed";
            default:
               return "";
         }
      }

      public String getStepLabel() {
         return "Step " + (this.currentStepIndex + 1) + "/" + this.totalSteps;
      }

      public String getStepTypeLabel() {
         switch (this.currentStepType) {
            case 0:
               return "§eTravel";
            case 1:
               return "§cCombat";
            case 2:
               return "§bScout";
            default:
               return "";
         }
      }
   }

   public static class MissionOfferEntry {
      public final String templateId;
      public final String templateName;
      public final String description;
      public final String objectiveText;
      public final int targetX;
      public final int targetY;
      public final int targetZ;
      public final int categoryOrdinal;
      public final int tokenReward;
      public final int repReward;
      public final int xpReward;
      public final int ryoReward;
      public final int pveXpReward;
      public final int totalSteps;

      public MissionOfferEntry(String templateId, String templateName, String description, String objectiveText, int targetX, int targetY, int targetZ, int categoryOrdinal, int tokenReward, int repReward, int xpReward, int ryoReward, int pveXpReward, int totalSteps) {
         this.templateId = templateId;
         this.templateName = templateName;
         this.description = description;
         this.objectiveText = objectiveText;
         this.targetX = targetX;
         this.targetY = targetY;
         this.targetZ = targetZ;
         this.categoryOrdinal = categoryOrdinal;
         this.tokenReward = tokenReward;
         this.repReward = repReward;
         this.xpReward = xpReward;
         this.ryoReward = ryoReward;
         this.pveXpReward = pveXpReward;
         this.totalSteps = totalSteps;
      }
   }

   public static class ContractClientEntry {
      public final String id;
      public final String posterName;
      public final int typeOrdinal;
      public final String targetName;
      public final int ryoAmount;
      public final int statusOrdinal;
      public final String assignee1;
      public final String assignee2;
      public final String targetZoneId;
      public final String posterVillage;

      public ContractClientEntry(String id, String posterName, int typeOrdinal, String targetName, int ryoAmount, int statusOrdinal, String assignee1, String assignee2, String targetZoneId, String posterVillage) {
         this.id = id;
         this.posterName = posterName;
         this.typeOrdinal = typeOrdinal;
         this.targetName = targetName;
         this.ryoAmount = ryoAmount;
         this.statusOrdinal = statusOrdinal;
         this.assignee1 = assignee1;
         this.assignee2 = assignee2;
         this.targetZoneId = targetZoneId;
         this.posterVillage = posterVillage != null ? posterVillage : "";
      }

      public String getTypeName() {
         String[] names = new String[]{"Assassination", "Territory Hire"};
         return this.typeOrdinal >= 0 && this.typeOrdinal < names.length ? names[this.typeOrdinal] : "Unknown";
      }

      public String getStatusName() {
         String[] names = new String[]{"OPEN", "ACCEPTED", "COMPLETED", "FAILED", "EXPIRED", "CANCELLED"};
         return this.statusOrdinal >= 0 && this.statusOrdinal < names.length ? names[this.statusOrdinal] : "Unknown";
      }
   }

   public static class RosterEntry {
      public final String name;
      public final String ringKanji;
      public final String rankName;
      public final boolean online;

      public RosterEntry(String name, String ringKanji, String rankName, boolean online) {
         this.name = name;
         this.ringKanji = ringKanji;
         this.rankName = rankName;
         this.online = online;
      }
   }
}
