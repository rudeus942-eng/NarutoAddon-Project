package net.luck.narutoaddon.OtherCode.quest.network;

import net.luck.narutoaddon.OtherCode.quest.core.QuestInstance;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointData;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

@SideOnly(Side.CLIENT)
public class QuestClientData {
   private static final Map<String, ActiveQuestState> activeQuests = new HashMap();
   private static final Map<String, WaypointInfo> waypoints = new HashMap();
   private static String dialogNpcName = null;
   private static String[] dialogLines = null;
   private static String[] dialogResponses = null;
   private static int dialogStepIndex = 0;
   private static int dialogCorrectIndex = 0;
   private static List<QuestListEntry> availableQuests = new ArrayList();
   private static List<String> completedQuestIds = new ArrayList();
   private static final Map<String, OfferState> currentOffers = new HashMap();
   private static final List<String> DAILY_SUB_SLOTS = Arrays.asList("daily_0", "daily_1", "daily_2");
   private static final List<String> WEEKLY_SUB_SLOTS = Arrays.asList("weekly_0", "weekly_1", "weekly_2");
   private static boolean beginnerComplete = false;
   private static boolean forceBeginner = false;

   public static boolean hasActiveQuest() {
      return !activeQuests.isEmpty();
   }

   public static boolean hasActiveQuestInSlot(String slot) {
      return "story".equals(slot) ? hasAnyStoryQuest() : activeQuests.containsKey(slot);
   }

   public static ActiveQuestState getActiveQuestInSlot(String slot) {
      return "story".equals(slot) ? findFirstStoryQuest() : (ActiveQuestState)activeQuests.get(slot);
   }

   public static Map<String, ActiveQuestState> getAllActiveQuests() {
      return Collections.unmodifiableMap(activeQuests);
   }

   public static void setActiveQuestInSlot(String slot, String questId, String questName, int stepIndex, String stepDesc, int totalSteps, QuestInstance.QuestState state, int killProgress, int killsRequired) {
      activeQuests.put(slot, new ActiveQuestState(questId, questName, stepIndex, stepDesc, totalSteps, state, killProgress, killsRequired));
   }

   public static void setActiveQuestInSlot(String slot, String questId, String questName, int stepIndex, String stepDesc, int totalSteps, QuestInstance.QuestState state, int killProgress, int killsRequired, String rewardSummary) {
      activeQuests.put(slot, new ActiveQuestState(questId, questName, stepIndex, stepDesc, totalSteps, state, killProgress, killsRequired, rewardSummary));
   }

   public static void clearActiveQuestInSlot(String slot) {
      activeQuests.remove(slot);
   }

   public static void clearAllActiveQuests() {
      ActiveQuestState savedBingo = (ActiveQuestState)activeQuests.get("bingo_hunt");
      ActiveQuestState savedOutpost = (ActiveQuestState)activeQuests.get("outpost_mission");
      ActiveQuestState savedIncursion = (ActiveQuestState)activeQuests.get("incursion_event");
      activeQuests.clear();
      if (savedBingo != null) {
         activeQuests.put("bingo_hunt", savedBingo);
      }

      if (savedOutpost != null) {
         activeQuests.put("outpost_mission", savedOutpost);
      }

      if (savedIncursion != null) {
         activeQuests.put("incursion_event", savedIncursion);
      }

   }

   public static boolean isStorySlot(String slot) {
      return slot != null && slot.startsWith("story:");
   }

   private static ActiveQuestState findFirstStoryQuest() {
      for(Map.Entry<String, ActiveQuestState> entry : activeQuests.entrySet()) {
         if (isStorySlot((String)entry.getKey())) {
            return (ActiveQuestState)entry.getValue();
         }
      }

      return null;
   }

   public static boolean hasAnyStoryQuest() {
      for(String slot : activeQuests.keySet()) {
         if (isStorySlot(slot)) {
            return true;
         }
      }

      return false;
   }

   public static List<ActiveQuestState> getAllStoryQuests() {
      List<ActiveQuestState> result = new ArrayList();

      for(Map.Entry<String, ActiveQuestState> entry : activeQuests.entrySet()) {
         if (isStorySlot((String)entry.getKey())) {
            result.add(entry.getValue());
         }
      }

      return result;
   }

   public static String getActiveQuestId() {
      ActiveQuestState s = findFirstStoryQuest();
      if (s != null) {
         return s.questId;
      } else {
         return !activeQuests.isEmpty() ? ((ActiveQuestState)activeQuests.values().iterator().next()).questId : null;
      }
   }

   public static String getActiveQuestName() {
      ActiveQuestState s = findFirstStoryQuest();
      if (s != null) {
         return s.questName;
      } else {
         return !activeQuests.isEmpty() ? ((ActiveQuestState)activeQuests.values().iterator().next()).questName : null;
      }
   }

   public static int getCurrentStepIndex() {
      ActiveQuestState s = findFirstStoryQuest();
      if (s != null) {
         return s.stepIndex;
      } else {
         return !activeQuests.isEmpty() ? ((ActiveQuestState)activeQuests.values().iterator().next()).stepIndex : 0;
      }
   }

   public static String getStepDescription() {
      ActiveQuestState s = findFirstStoryQuest();
      if (s != null) {
         return s.stepDesc;
      } else {
         return !activeQuests.isEmpty() ? ((ActiveQuestState)activeQuests.values().iterator().next()).stepDesc : null;
      }
   }

   public static int getTotalSteps() {
      ActiveQuestState s = findFirstStoryQuest();
      if (s != null) {
         return s.totalSteps;
      } else {
         return !activeQuests.isEmpty() ? ((ActiveQuestState)activeQuests.values().iterator().next()).totalSteps : 0;
      }
   }

   public static QuestInstance.QuestState getQuestState() {
      ActiveQuestState s = findFirstStoryQuest();
      if (s != null) {
         return s.state;
      } else {
         return !activeQuests.isEmpty() ? ((ActiveQuestState)activeQuests.values().iterator().next()).state : null;
      }
   }

   public static int getKillProgress() {
      ActiveQuestState s = findFirstStoryQuest();
      if (s != null) {
         return s.killProgress;
      } else {
         return !activeQuests.isEmpty() ? ((ActiveQuestState)activeQuests.values().iterator().next()).killProgress : 0;
      }
   }

   public static int getKillsRequired() {
      ActiveQuestState s = findFirstStoryQuest();
      if (s != null) {
         return s.killsRequired;
      } else {
         return !activeQuests.isEmpty() ? ((ActiveQuestState)activeQuests.values().iterator().next()).killsRequired : 0;
      }
   }

   public static void setActiveQuest(String questId, String questName, int stepIndex, String description, int steps, QuestInstance.QuestState state, int kills, int killsReq) {
      setActiveQuestInSlot("story:leaf_story", questId, questName, stepIndex, description, steps, state, kills, killsReq);
   }

   public static void clearActiveQuest() {
      activeQuests.entrySet().removeIf((e) -> isStorySlot((String)e.getKey()));
   }

   public static boolean hasWaypoint() {
      return !waypoints.isEmpty();
   }

   public static boolean hasWaypointForQuest(String questId) {
      return waypoints.containsKey(questId);
   }

   public static WaypointInfo getWaypointForQuest(String questId) {
      return (WaypointInfo)waypoints.get(questId);
   }

   public static Map<String, WaypointInfo> getAllWaypoints() {
      return Collections.unmodifiableMap(waypoints);
   }

   public static void setWaypointForQuest(String questId, BlockPos pos, WaypointData.WaypointType type, String label) {
      waypoints.put(questId, new WaypointInfo(pos, type, label));
   }

   public static void clearWaypointForQuest(String questId) {
      waypoints.remove(questId);
   }

   public static void clearAllWaypoints() {
      waypoints.clear();
   }

   public static BlockPos getWaypointPos() {
      return waypoints.isEmpty() ? null : ((WaypointInfo)waypoints.values().iterator().next()).pos;
   }

   public static WaypointData.WaypointType getWaypointType() {
      return waypoints.isEmpty() ? null : ((WaypointInfo)waypoints.values().iterator().next()).type;
   }

   public static String getWaypointLabel() {
      return waypoints.isEmpty() ? null : ((WaypointInfo)waypoints.values().iterator().next()).label;
   }

   public static void setWaypoint(BlockPos pos, WaypointData.WaypointType type, String label) {
      waypoints.put("_legacy", new WaypointInfo(pos, type, label));
   }

   public static void clearWaypoint() {
      waypoints.clear();
   }

   public static boolean hasDialog() {
      return dialogNpcName != null && dialogLines != null;
   }

   public static String getDialogNpcName() {
      return dialogNpcName;
   }

   public static String[] getDialogLines() {
      return dialogLines;
   }

   public static String[] getDialogResponses() {
      return dialogResponses;
   }

   public static int getDialogStepIndex() {
      return dialogStepIndex;
   }

   public static int getDialogCorrectIndex() {
      return dialogCorrectIndex;
   }

   public static void setDialog(String npcName, String[] lines, String[] responses, int stepIdx, int correctIndex) {
      dialogNpcName = npcName;
      dialogLines = lines;
      dialogResponses = responses;
      dialogStepIndex = stepIdx;
      dialogCorrectIndex = correctIndex;
   }

   public static void clearDialog() {
      dialogNpcName = null;
      dialogLines = null;
      dialogResponses = null;
      dialogStepIndex = 0;
      dialogCorrectIndex = 0;
   }

   public static List<QuestListEntry> getAvailableQuests() {
      return availableQuests;
   }

   public static List<String> getCompletedQuestIds() {
      return completedQuestIds;
   }

   public static void setAvailableQuests(List<QuestListEntry> quests) {
      availableQuests = quests;
   }

   public static void setCompletedQuestIds(List<String> ids) {
      completedQuestIds = ids;
   }

   public static boolean hasOffer(String slot) {
      return currentOffers.containsKey(slot);
   }

   public static OfferState getOffer(String slot) {
      return (OfferState)currentOffers.get(slot);
   }

   public static void setOffer(String slot, OfferState offer) {
      currentOffers.put(slot, offer);
   }

   public static void clearOffer(String slot) {
      currentOffers.remove(slot);
   }

   public static void clearAllOffers() {
      currentOffers.clear();
   }

   public static List<String> getDailySubSlots() {
      return DAILY_SUB_SLOTS;
   }

   public static List<String> getWeeklySubSlots() {
      return WEEKLY_SUB_SLOTS;
   }

   public static String getBaseCategory(String subSlot) {
      if (subSlot.startsWith("daily")) {
         return "daily";
      } else if (subSlot.startsWith("weekly")) {
         return "weekly";
      } else if (isStorySlot(subSlot)) {
         return "story";
      } else if ("bingo_hunt".equals(subSlot)) {
         return "bingo";
      } else if ("outpost_mission".equals(subSlot)) {
         return "outpost";
      } else {
         return "incursion_event".equals(subSlot) ? "incursion" : subSlot;
      }
   }

   public static boolean isBingoSlot(String slot) {
      return "bingo_hunt".equals(slot);
   }

   public static boolean isOutpostSlot(String slot) {
      return "outpost_mission".equals(slot);
   }

   public static void setOutpostMission(String locationName, String encounterName, String stateName) {
      String description = "Travel to " + locationName;
      if ("ACTIVE".equals(stateName)) {
         description = "Defeat " + encounterName;
      } else if ("VICTORY".equals(stateName)) {
         description = "Victory!";
      }

      setActiveQuestInSlot("outpost_mission", "outpost_mission", encounterName + " - " + locationName, 0, description, 1, QuestInstance.QuestState.ACTIVE, 0, 0);
   }

   public static void clearOutpostMission() {
      clearActiveQuestInSlot("outpost_mission");
   }

   public static void setBingoHunt(String targetName, String loreHint) {
      setActiveQuestInSlot("bingo_hunt", "bingo_hunt", targetName, 0, loreHint, 1, QuestInstance.QuestState.ACTIVE, 0, 0);
   }

   public static void clearBingoHunt() {
      clearActiveQuestInSlot("bingo_hunt");
   }

   public static boolean isIncursionSlot(String slot) {
      return "incursion_event".equals(slot);
   }

   public static void setIncursionWaypoint(String name, double x, double z) {
      setActiveQuestInSlot("incursion_event", "incursion_event", name, 0, "Travel to incursion", 1, QuestInstance.QuestState.ACTIVE, 0, 0);
      setWaypointForQuest("incursion_event", new BlockPos((int)x, 64, (int)z), WaypointData.WaypointType.COMBAT, name);
   }

   public static void clearIncursionWaypoint() {
      clearActiveQuestInSlot("incursion_event");
      clearWaypointForQuest("incursion_event");
   }

   public static boolean isContractSlot(String slot) {
      return "contract_mission".equals(slot);
   }

   public static void setContractMission(String targetName, String locationHint) {
      setActiveQuestInSlot("contract_mission", "contract_mission", targetName, 0, locationHint != null && !locationHint.isEmpty() ? locationHint : "Track the target", 1, QuestInstance.QuestState.ACTIVE, 0, 0);
   }

   public static void clearContractMission() {
      clearActiveQuestInSlot("contract_mission");
   }

   public static boolean isBeginnerComplete() {
      return beginnerComplete;
   }

   public static void setBeginnerComplete(boolean complete) {
      beginnerComplete = complete;
   }

   public static boolean isForceBeginner() {
      return forceBeginner;
   }

   public static void setForceBeginner(boolean force) {
      forceBeginner = force;
   }

   public static void clearAllData() {
      activeQuests.clear();
      waypoints.clear();
      clearDialog();
      availableQuests = new ArrayList();
      completedQuestIds = new ArrayList();
      currentOffers.clear();
      beginnerComplete = false;
      forceBeginner = false;
   }

   public static class ActiveQuestState {
      public final String questId;
      public final String questName;
      public final int stepIndex;
      public final String stepDesc;
      public final int totalSteps;
      public final QuestInstance.QuestState state;
      public final int killProgress;
      public final int killsRequired;
      public final String rewardSummary;

      public ActiveQuestState(String questId, String questName, int stepIndex, String stepDesc, int totalSteps, QuestInstance.QuestState state, int killProgress, int killsRequired) {
         this(questId, questName, stepIndex, stepDesc, totalSteps, state, killProgress, killsRequired, "");
      }

      public ActiveQuestState(String questId, String questName, int stepIndex, String stepDesc, int totalSteps, QuestInstance.QuestState state, int killProgress, int killsRequired, String rewardSummary) {
         this.questId = questId;
         this.questName = questName;
         this.stepIndex = stepIndex;
         this.stepDesc = stepDesc;
         this.totalSteps = totalSteps;
         this.state = state;
         this.killProgress = killProgress;
         this.killsRequired = killsRequired;
         this.rewardSummary = rewardSummary != null ? rewardSummary : "";
      }
   }

   public static class WaypointInfo {
      public final BlockPos pos;
      public final WaypointData.WaypointType type;
      public final String label;

      public WaypointInfo(BlockPos pos, WaypointData.WaypointType type, String label) {
         this.pos = pos;
         this.type = type;
         this.label = label;
      }
   }

   public static class OfferState {
      public final String name;
      public final String description;
      public final byte rank;
      public final int killCount;
      public final int xpReward;
      public final int ryoReward;
      public final int rerollsRemaining;
      public final long cooldownRemaining;

      public OfferState(String name, String description, byte rank, int killCount, int xpReward, int ryoReward, int rerollsRemaining, long cooldownRemaining) {
         this.name = name;
         this.description = description;
         this.rank = rank;
         this.killCount = killCount;
         this.xpReward = xpReward;
         this.ryoReward = ryoReward;
         this.rerollsRemaining = rerollsRemaining;
         this.cooldownRemaining = cooldownRemaining;
      }
   }

   public static class QuestListEntry {
      public final String id;
      public final String name;
      public final String description;
      public final int arc;
      public final boolean available;
      public final byte category;
      public final long cooldownRemaining;
      public final byte rank;
      public final String storyline;
      public final String rewardSummary;

      public QuestListEntry(String id, String name, String description, int arc, boolean available) {
         this(id, name, description, arc, available, (byte)0, 0L, (byte)0, "leaf_story", "");
      }

      public QuestListEntry(String id, String name, String description, int arc, boolean available, byte category, long cooldownRemaining) {
         this(id, name, description, arc, available, category, cooldownRemaining, (byte)0, "leaf_story", "");
      }

      public QuestListEntry(String id, String name, String description, int arc, boolean available, byte category, long cooldownRemaining, byte rank) {
         this(id, name, description, arc, available, category, cooldownRemaining, rank, "leaf_story", "");
      }

      public QuestListEntry(String id, String name, String description, int arc, boolean available, byte category, long cooldownRemaining, byte rank, String storyline) {
         this(id, name, description, arc, available, category, cooldownRemaining, rank, storyline, "");
      }

      public QuestListEntry(String id, String name, String description, int arc, boolean available, byte category, long cooldownRemaining, byte rank, String storyline, String rewardSummary) {
         this.id = id;
         this.name = name;
         this.description = description;
         this.arc = arc;
         this.available = available;
         this.category = category;
         this.cooldownRemaining = cooldownRemaining;
         this.rank = rank;
         this.storyline = storyline != null ? storyline : "leaf_story";
         this.rewardSummary = rewardSummary != null ? rewardSummary : "";
      }
   }
}
