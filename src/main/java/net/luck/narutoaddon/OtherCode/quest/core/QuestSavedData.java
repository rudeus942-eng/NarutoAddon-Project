
package net.luck.narutoaddon.OtherCode.quest.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestSavedData extends WorldSavedData {
   private static final String DATA_NAME = "InfTsukQuestData";
   private static final long DAY_MS = 86400000L;
   private static final long WEEK_MS = 604800000L;
   private static final long RESET_ANCHOR_MS;
   private final Map<UUID, List<QuestInstance>> playerActiveQuests = new ConcurrentHashMap();
   private final Map<UUID, Set<String>> playerCompletedQuests = new ConcurrentHashMap();
   private final Map<String, Map<Integer, BlockPos>> coordinateOverrides = new ConcurrentHashMap();
   private final Map<UUID, Map<String, Long>> repeatableCompletionTimes = new ConcurrentHashMap();
   private final Map<UUID, Map<String, Integer>> rerollCounts = new ConcurrentHashMap();
   private final Map<UUID, Map<String, Long>> rerollResetTimes = new ConcurrentHashMap();
   private static final int MAX_REROLLS_DAILY = 5;
   private static final int MAX_REROLLS_WEEKLY = 5;
   private final Map<UUID, Map<String, Integer>> bonusRerolls = new ConcurrentHashMap();
   private final Map<UUID, Integer> playerMissionRanks = new ConcurrentHashMap();
   private final Map<UUID, Map<String, RepeatableQuestGenerator.QuestOffer>> savedOffers = new ConcurrentHashMap();
   private final Map<UUID, Long> randomAbandonTimes = new ConcurrentHashMap();
   private static final long RANDOM_ABANDON_COOLDOWN_MS = 300000L;
   private final Map<UUID, Set<String>> playerFlags = new ConcurrentHashMap();

   public QuestSavedData() {
      super("InfTsukQuestData");
   }

   public QuestSavedData(String name) {
      super(name);
   }

   public static QuestSavedData get(World world) {
      MapStorage storage = world.getMapStorage();
      if (storage == null) {
         return new QuestSavedData();
      } else {
         QuestSavedData data = (QuestSavedData)storage.getOrLoadData(QuestSavedData.class, "InfTsukQuestData");
         if (data == null) {
            data = new QuestSavedData();
            storage.setData("InfTsukQuestData", data);
         }

         return data;
      }
   }

   public List<QuestInstance> getActiveQuests(UUID playerId) {
      return new ArrayList((Collection)this.playerActiveQuests.getOrDefault(playerId, Collections.emptyList()));
   }

   public QuestInstance getActiveQuest(UUID playerId) {
      List<QuestInstance> quests = (List)this.playerActiveQuests.get(playerId);
      return quests != null && !quests.isEmpty() ? (QuestInstance)quests.get(0) : null;
   }

   public void saveQuest(UUID playerId, QuestInstance quest) {
      List<QuestInstance> quests = (List)this.playerActiveQuests.computeIfAbsent(playerId, (k) -> new ArrayList());
      quests.removeIf((q) -> q.getQuestId().equals(quest.getQuestId()));
      if (quest.getState() == QuestInstance.QuestState.ACTIVE) {
         quests.add(quest);
      }

      this.markDirty();
   }

   public void removeActiveQuest(UUID playerId) {
      this.playerActiveQuests.remove(playerId);
      this.markDirty();
   }

   public void removeActiveQuest(UUID playerId, String questId) {
      List<QuestInstance> quests = (List)this.playerActiveQuests.get(playerId);
      if (quests != null) {
         quests.removeIf((q) -> q.getQuestId().equals(questId));
         if (quests.isEmpty()) {
            this.playerActiveQuests.remove(playerId);
         }
      }

      this.markDirty();
   }

   public Set<String> getCompletedQuests(UUID playerId) {
      return new HashSet((Collection)this.playerCompletedQuests.getOrDefault(playerId, Collections.emptySet()));
   }

   public void completeQuest(UUID playerId, String questId) {
      ((Set)this.playerCompletedQuests.computeIfAbsent(playerId, (k) -> new HashSet())).add(questId);
      List<QuestInstance> active = (List)this.playerActiveQuests.get(playerId);
      if (active != null) {
         active.removeIf((q) -> q.getQuestId().equals(questId));
         if (active.isEmpty()) {
            this.playerActiveQuests.remove(playerId);
         }
      }

      this.markDirty();
   }

   public boolean hasCompletedQuest(UUID playerId, String questId) {
      Set<String> completed = (Set)this.playerCompletedQuests.get(playerId);
      return completed != null && completed.contains(questId);
   }

   public void resetQuest(UUID playerId, String questId) {
      Set<String> completed = (Set)this.playerCompletedQuests.get(playerId);
      if (completed != null) {
         completed.remove(questId);
      }

      List<QuestInstance> active = (List)this.playerActiveQuests.get(playerId);
      if (active != null) {
         active.removeIf((q) -> q.getQuestId().equals(questId));
      }

      this.markDirty();
   }

   public void resetAllQuests(UUID playerId) {
      this.playerCompletedQuests.remove(playerId);
      this.playerActiveQuests.remove(playerId);
      this.markDirty();
   }

   public void wipePlayerProgression(UUID playerId) {
      this.playerActiveQuests.remove(playerId);
      this.playerCompletedQuests.remove(playerId);
      this.repeatableCompletionTimes.remove(playerId);
      this.rerollCounts.remove(playerId);
      this.rerollResetTimes.remove(playerId);
      this.bonusRerolls.remove(playerId);
      this.playerMissionRanks.remove(playerId);
      this.savedOffers.remove(playerId);
      this.randomAbandonTimes.remove(playerId);
      this.markDirty();
   }

   public Set<UUID> getAllPlayersWithAnyProgression() {
      Set<UUID> all = new HashSet();
      all.addAll(this.playerActiveQuests.keySet());
      all.addAll(this.playerCompletedQuests.keySet());
      all.addAll(this.repeatableCompletionTimes.keySet());
      all.addAll(this.rerollCounts.keySet());
      all.addAll(this.rerollResetTimes.keySet());
      all.addAll(this.bonusRerolls.keySet());
      all.addAll(this.playerMissionRanks.keySet());
      all.addAll(this.savedOffers.keySet());
      all.addAll(this.randomAbandonTimes.keySet());
      all.addAll(this.playerFlags.keySet());
      return Collections.unmodifiableSet(all);
   }

   public boolean isRepeatableOnCooldown(UUID playerId, String questId) {
      QuestDefinition def = QuestRegistry.getById(questId);
      if (def != null && def.isRepeatable()) {
         if (def.getRepeatType() == QuestDefinition.RepeatType.RANDOM) {
            return false;
         } else {
            Map<String, Long> times = (Map)this.repeatableCompletionTimes.get(playerId);
            if (times == null) {
               return false;
            } else {
               Long lastCompletion = (Long)times.get(questId);
               if (lastCompletion == null) {
                  return false;
               } else {
                  long lastReset = getLastResetForType(def.getRepeatType());
                  return lastCompletion >= lastReset;
               }
            }
         }
      } else {
         return false;
      }
   }

   public long getRepeatableCooldownRemaining(UUID playerId, String questId) {
      QuestDefinition def = QuestRegistry.getById(questId);
      if (def != null && def.isRepeatable()) {
         if (def.getRepeatType() == QuestDefinition.RepeatType.RANDOM) {
            return 0L;
         } else if (!this.isRepeatableOnCooldown(playerId, questId)) {
            return 0L;
         } else {
            long nextReset = getNextResetForType(def.getRepeatType());
            return Math.max(0L, nextReset - System.currentTimeMillis());
         }
      } else {
         return 0L;
      }
   }

   private static long getLastResetForType(QuestDefinition.RepeatType type) {
      switch (type) {
         case DAILY:
            return getLastDailyReset();
         case WEEKLY:
            return getLastWeeklyReset();
         default:
            return 0L;
      }
   }

   private static long getNextResetForType(QuestDefinition.RepeatType type) {
      switch (type) {
         case DAILY:
            return getNextDailyReset();
         case WEEKLY:
            return getNextWeeklyReset();
         default:
            return 0L;
      }
   }

   public void recordRepeatableCompletion(UUID playerId, String questId) {
      ((Map)this.repeatableCompletionTimes.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap())).put(questId, System.currentTimeMillis());
      this.markDirty();
   }

   private static long getLastDailyReset() {
      long now = System.currentTimeMillis();
      long elapsed = now - RESET_ANCHOR_MS;
      if (elapsed < 0L) {
         return RESET_ANCHOR_MS - 86400000L;
      } else {
         long periods = elapsed / 86400000L;
         return RESET_ANCHOR_MS + periods * 86400000L;
      }
   }

   private static long getNextDailyReset() {
      long now = System.currentTimeMillis();
      long elapsed = now - RESET_ANCHOR_MS;
      long periods = elapsed < 0L ? 0L : elapsed / 86400000L + 1L;
      return RESET_ANCHOR_MS + periods * 86400000L;
   }

   private static long getLastWeeklyReset() {
      long now = System.currentTimeMillis();
      long elapsed = now - RESET_ANCHOR_MS;
      if (elapsed < 0L) {
         return RESET_ANCHOR_MS - 604800000L;
      } else {
         long periods = elapsed / 604800000L;
         return RESET_ANCHOR_MS + periods * 604800000L;
      }
   }

   private static long getNextWeeklyReset() {
      long now = System.currentTimeMillis();
      long elapsed = now - RESET_ANCHOR_MS;
      long periods = elapsed < 0L ? 0L : elapsed / 604800000L + 1L;
      return RESET_ANCHOR_MS + periods * 604800000L;
   }

   public boolean isSlotOnCooldown(UUID playerId, String slot) {
      if (slot.equals("random")) {
         return false;
      } else {
         Map<String, Long> times = (Map)this.repeatableCompletionTimes.get(playerId);
         if (times == null) {
            return false;
         } else {
            String key = "_slot_" + slot;
            Long lastCompletion = (Long)times.get(key);
            if (lastCompletion == null) {
               return false;
            } else {
               long lastReset = slot.startsWith("daily") ? getLastDailyReset() : getLastWeeklyReset();
               return lastCompletion >= lastReset;
            }
         }
      }
   }

   public long getSlotCooldownRemaining(UUID playerId, String slot) {
      if (slot.equals("random")) {
         return 0L;
      } else if (!this.isSlotOnCooldown(playerId, slot)) {
         return 0L;
      } else {
         long nextReset = slot.startsWith("daily") ? getNextDailyReset() : getNextWeeklyReset();
         return Math.max(0L, nextReset - System.currentTimeMillis());
      }
   }

   public void recordSlotCompletion(UUID playerId, String slot) {
      if (!slot.equals("random")) {
         String key = "_slot_" + slot;
         ((Map)this.repeatableCompletionTimes.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap())).put(key, System.currentTimeMillis());
         this.markDirty();
      }
   }

   public int getRerollsRemaining(UUID playerId, String slot) {
      if ("random".equals(slot)) {
         return Integer.MAX_VALUE;
      } else {
         this.resetRerollsIfNeeded(playerId, slot);
         int maxRerolls = "daily".equals(slot) ? 5 : 5;
         int bonus = this.getBonusRerolls(playerId, slot);
         Map<String, Integer> playerRerolls = (Map)this.rerollCounts.get(playerId);
         if (playerRerolls == null) {
            return maxRerolls + bonus;
         } else {
            int used = (Integer)playerRerolls.getOrDefault(slot, 0);
            return Math.max(0, maxRerolls + bonus - used);
         }
      }
   }

   public boolean useReroll(UUID playerId, String slot) {
      if ("random".equals(slot)) {
         return true;
      } else {
         this.resetRerollsIfNeeded(playerId, slot);
         int maxRerolls = "daily".equals(slot) ? 5 : 5;
         int bonus = this.getBonusRerolls(playerId, slot);
         Map<String, Integer> playerRerolls = (Map)this.rerollCounts.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap());
         int used = (Integer)playerRerolls.getOrDefault(slot, 0);
         if (used >= maxRerolls + bonus) {
            return false;
         } else {
            playerRerolls.put(slot, used + 1);
            Map<String, Long> resetTimes = (Map)this.rerollResetTimes.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap());
            if (!resetTimes.containsKey(slot)) {
               resetTimes.put(slot, System.currentTimeMillis());
            }

            this.markDirty();
            return true;
         }
      }
   }

   public void addBonusRerolls(UUID playerId, String slot, int count) {
      Map<String, Integer> playerBonus = (Map)this.bonusRerolls.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap());
      int current = (Integer)playerBonus.getOrDefault(slot, 0);
      playerBonus.put(slot, current + count);
      this.markDirty();
   }

   private int getBonusRerolls(UUID playerId, String slot) {
      Map<String, Integer> playerBonus = (Map)this.bonusRerolls.get(playerId);
      return playerBonus == null ? 0 : (Integer)playerBonus.getOrDefault(slot, 0);
   }

   private void resetRerollsIfNeeded(UUID playerId, String slot) {
      Map<String, Long> resetTimes = (Map)this.rerollResetTimes.get(playerId);
      if (resetTimes != null) {
         Long lastRerollTimestamp = (Long)resetTimes.get(slot);
         if (lastRerollTimestamp != null) {
            long lastFixedReset = "daily".equals(slot) ? getLastDailyReset() : getLastWeeklyReset();
            if (lastFixedReset > lastRerollTimestamp) {
               Map<String, Integer> playerRerolls = (Map)this.rerollCounts.get(playerId);
               if (playerRerolls != null) {
                  playerRerolls.remove(slot);
               }

               resetTimes.remove(slot);
               this.markDirty();
            }

         }
      }
   }

   public boolean isRandomAbandonOnCooldown(UUID playerId) {
      Long lastAbandon = (Long)this.randomAbandonTimes.get(playerId);
      if (lastAbandon == null) {
         return false;
      } else {
         return System.currentTimeMillis() - lastAbandon < 300000L;
      }
   }

   public long getRandomAbandonCooldownRemaining(UUID playerId) {
      Long lastAbandon = (Long)this.randomAbandonTimes.get(playerId);
      if (lastAbandon == null) {
         return 0L;
      } else {
         long elapsed = System.currentTimeMillis() - lastAbandon;
         return Math.max(0L, 300000L - elapsed);
      }
   }

   public void setRandomAbandonTime(UUID playerId) {
      this.randomAbandonTimes.put(playerId, System.currentTimeMillis());
      this.markDirty();
   }

   public int getPlayerMissionRankOrdinal(UUID playerId) {
      return (Integer)this.playerMissionRanks.getOrDefault(playerId, 0);
   }

   public QuestDefinition.QuestRank getPlayerMissionRank(UUID playerId) {
      int ordinal = this.getPlayerMissionRankOrdinal(playerId);
      QuestDefinition.QuestRank[] ranks = QuestDefinition.QuestRank.values();
      return ordinal >= 0 && ordinal < ranks.length ? ranks[ordinal] : QuestDefinition.QuestRank.D;
   }

   public void setPlayerMissionRank(UUID playerId, QuestDefinition.QuestRank rank) {
      this.playerMissionRanks.put(playerId, rank.ordinal());
      this.markDirty();
   }

   public void saveOffer(UUID playerId, String subSlot, RepeatableQuestGenerator.QuestOffer offer) {
      ((Map)this.savedOffers.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap())).put(subSlot, offer);
      this.markDirty();
   }

   public void removeOffer(UUID playerId, String subSlot) {
      Map<String, RepeatableQuestGenerator.QuestOffer> playerOffers = (Map)this.savedOffers.get(playerId);
      if (playerOffers != null) {
         playerOffers.remove(subSlot);
         if (playerOffers.isEmpty()) {
            this.savedOffers.remove(playerId);
         }

         this.markDirty();
      }

   }

   public Map<String, RepeatableQuestGenerator.QuestOffer> getSavedOffers(UUID playerId) {
      Map<String, RepeatableQuestGenerator.QuestOffer> offers = (Map)this.savedOffers.get(playerId);
      return offers != null ? new HashMap(offers) : new HashMap();
   }

   public RepeatableQuestGenerator.QuestOffer getSavedOffer(UUID playerId, String subSlot) {
      Map<String, RepeatableQuestGenerator.QuestOffer> playerOffers = (Map)this.savedOffers.get(playerId);
      return playerOffers != null ? (RepeatableQuestGenerator.QuestOffer)playerOffers.get(subSlot) : null;
   }

   public static boolean isOfferStale(String subSlot, long offerCreatedAt) {
      if (subSlot.equals("random")) {
         return false;
      } else {
         long lastReset = subSlot.startsWith("daily") ? getLastDailyReset() : getLastWeeklyReset();
         return offerCreatedAt < lastReset;
      }
   }

   public void setCoordinateOverride(String questId, int stepIndex, BlockPos pos) {
      ((Map)this.coordinateOverrides.computeIfAbsent(questId, (k) -> new ConcurrentHashMap())).put(stepIndex, pos);
      this.markDirty();
   }

   public BlockPos getEffectivePosition(String questId, int stepIndex) {
      Map<Integer, BlockPos> overrides = (Map)this.coordinateOverrides.get(questId);
      if (overrides != null && overrides.containsKey(stepIndex)) {
         return (BlockPos)overrides.get(stepIndex);
      } else {
         QuestDefinition def = QuestRegistry.getById(questId);
         if (def != null) {
            QuestStep step = def.getStep(stepIndex);
            if (step != null) {
               return step.defaultPosition;
            }
         }

         return null;
      }
   }

   public Map<Integer, BlockPos> getCoordinateOverrides(String questId) {
      return (Map)this.coordinateOverrides.getOrDefault(questId, new HashMap());
   }

   public boolean hasFlag(UUID playerId, String flag) {
      Set<String> flags = (Set)this.playerFlags.get(playerId);
      return flags != null && flags.contains(flag);
   }

   public void setFlag(UUID playerId, String flag) {
      ((Set)this.playerFlags.computeIfAbsent(playerId, (k) -> new HashSet())).add(flag);
      this.markDirty();
   }

   public void removeFlag(UUID playerId, String flag) {
      Set<String> flags = (Set)this.playerFlags.get(playerId);
      if (flags != null) {
         flags.remove(flag);
         if (flags.isEmpty()) {
            this.playerFlags.remove(playerId);
         }
      }

      this.markDirty();
   }

   public int removeFlagFromAllPlayers(String flag) {
      int cleared = 0;
      Iterator<Map.Entry<UUID, Set<String>>> it = this.playerFlags.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<UUID, Set<String>> entry = (Map.Entry)it.next();
         Set<String> flags = (Set)entry.getValue();
         if (flags.remove(flag)) {
            ++cleared;
            if (flags.isEmpty()) {
               it.remove();
            }
         }
      }

      if (cleared > 0) {
         this.markDirty();
      }

      return cleared;
   }

   public int removeFlagForAllPlayers(String flag) {
      return this.removeFlagFromAllPlayers(flag);
   }

   public Set<UUID> getAllPlayersWithAnyFlag() {
      return Collections.unmodifiableSet(new HashSet(this.playerFlags.keySet()));
   }

   public Set<UUID> getPlayersWhoCompleted(String questId) {
      Set<UUID> result = new HashSet();

      for(Map.Entry<UUID, Set<String>> entry : this.playerCompletedQuests.entrySet()) {
         if (((Set)entry.getValue()).contains(questId)) {
            result.add(entry.getKey());
         }
      }

      return result;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.playerActiveQuests.clear();
      this.playerCompletedQuests.clear();
      this.coordinateOverrides.clear();
      this.repeatableCompletionTimes.clear();
      this.rerollCounts.clear();
      this.randomAbandonTimes.clear();
      this.rerollResetTimes.clear();
      this.bonusRerolls.clear();
      this.playerFlags.clear();
      this.playerMissionRanks.clear();
      this.savedOffers.clear();
      if (nbt.hasKey("activeQuests")) {
         NBTTagCompound activeTag = nbt.getCompoundTag("activeQuests");

         for(String uuidStr : activeTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagList questList = activeTag.getTagList(uuidStr, 10);
               List<QuestInstance> quests = new ArrayList();

               for(int i = 0; i < questList.tagCount(); ++i) {
                  QuestInstance quest = QuestInstance.readFromNBT(questList.getCompoundTagAt(i));
                  if (quest.isGenerated() || QuestRegistry.exists(quest.getQuestId())) {
                     quests.add(quest);
                  }
               }

               if (!quests.isEmpty()) {
                  this.playerActiveQuests.put(playerId, quests);
               }
            } catch (IllegalArgumentException var23) {
            }
         }
      }

      if (nbt.hasKey("completedQuests")) {
         NBTTagCompound completedTag = nbt.getCompoundTag("completedQuests");

         for(String uuidStr : completedTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagList idList = completedTag.getTagList(uuidStr, 8);
               Set<String> completed = new HashSet();

               for(int i = 0; i < idList.tagCount(); ++i) {
                  completed.add(idList.getStringTagAt(i));
               }

               this.playerCompletedQuests.put(playerId, completed);
            } catch (IllegalArgumentException var22) {
            }
         }
      }

      if (nbt.hasKey("coordOverrides")) {
         NBTTagCompound coordTag = nbt.getCompoundTag("coordOverrides");

         for(String questId : coordTag.getKeySet()) {
            NBTTagCompound stepOverrides = coordTag.getCompoundTag(questId);
            Map<Integer, BlockPos> overrides = new ConcurrentHashMap();

            for(String stepStr : stepOverrides.getKeySet()) {
               try {
                  int stepIndex = Integer.parseInt(stepStr);
                  NBTTagCompound posTag = stepOverrides.getCompoundTag(stepStr);
                  BlockPos pos = new BlockPos(posTag.getInteger("x"), posTag.getInteger("y"), posTag.getInteger("z"));
                  overrides.put(stepIndex, pos);
               } catch (NumberFormatException var15) {
               }
            }

            if (!overrides.isEmpty()) {
               this.coordinateOverrides.put(questId, overrides);
            }
         }
      }

      if (nbt.hasKey("repeatableTimes")) {
         NBTTagCompound timesTag = nbt.getCompoundTag("repeatableTimes");

         for(String uuidStr : timesTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagCompound questTimes = timesTag.getCompoundTag(uuidStr);
               Map<String, Long> times = new ConcurrentHashMap();

               for(String questId : questTimes.getKeySet()) {
                  times.put(questId, questTimes.getLong(questId));
               }

               this.repeatableCompletionTimes.put(playerId, times);
            } catch (IllegalArgumentException var21) {
            }
         }
      }

      if (nbt.hasKey("rerollCounts")) {
         NBTTagCompound rerollTag = nbt.getCompoundTag("rerollCounts");

         for(String uuidStr : rerollTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagCompound slotCounts = rerollTag.getCompoundTag(uuidStr);
               Map<String, Integer> counts = new ConcurrentHashMap();

               for(String slot : slotCounts.getKeySet()) {
                  counts.put(slot, slotCounts.getInteger(slot));
               }

               this.rerollCounts.put(playerId, counts);
            } catch (IllegalArgumentException var20) {
            }
         }
      }

      if (nbt.hasKey("rerollResetTimes")) {
         NBTTagCompound resetTag = nbt.getCompoundTag("rerollResetTimes");

         for(String uuidStr : resetTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagCompound slotTimes = resetTag.getCompoundTag(uuidStr);
               Map<String, Long> times = new ConcurrentHashMap();

               for(String slot : slotTimes.getKeySet()) {
                  times.put(slot, slotTimes.getLong(slot));
               }

               this.rerollResetTimes.put(playerId, times);
            } catch (IllegalArgumentException var19) {
            }
         }
      }

      if (nbt.hasKey("bonusRerolls")) {
         NBTTagCompound bonusTag = nbt.getCompoundTag("bonusRerolls");

         for(String uuidStr : bonusTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagCompound slotCounts = bonusTag.getCompoundTag(uuidStr);
               Map<String, Integer> counts = new ConcurrentHashMap();

               for(String slot : slotCounts.getKeySet()) {
                  counts.put(slot, slotCounts.getInteger(slot));
               }

               this.bonusRerolls.put(playerId, counts);
            } catch (IllegalArgumentException var18) {
            }
         }
      }

      if (nbt.hasKey("missionRanks")) {
         NBTTagCompound ranksTag = nbt.getCompoundTag("missionRanks");

         for(String uuidStr : ranksTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               this.playerMissionRanks.put(playerId, ranksTag.getInteger(uuidStr));
            } catch (IllegalArgumentException var14) {
            }
         }
      }

      if (nbt.hasKey("savedOffers")) {
         NBTTagCompound offersTag = nbt.getCompoundTag("savedOffers");

         for(String uuidStr : offersTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagCompound slotOffers = offersTag.getCompoundTag(uuidStr);
               Map<String, RepeatableQuestGenerator.QuestOffer> offers = new ConcurrentHashMap();

               for(String subSlot : slotOffers.getKeySet()) {
                  try {
                     NBTTagCompound offerNbt = slotOffers.getCompoundTag(subSlot);
                     RepeatableQuestGenerator.QuestOffer offer = RepeatableQuestGenerator.QuestOffer.fromNBT(offerNbt);
                     if (offer.questId != null && !offer.questId.isEmpty()) {
                        offers.put(subSlot, offer);
                     }
                  } catch (Exception var13) {
                  }
               }

               if (!offers.isEmpty()) {
                  this.savedOffers.put(playerId, offers);
               }
            } catch (IllegalArgumentException var17) {
            }
         }
      }

      if (nbt.hasKey("randomAbandonTimes")) {
         NBTTagCompound abandonTag = nbt.getCompoundTag("randomAbandonTimes");

         for(String uuidStr : abandonTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               this.randomAbandonTimes.put(playerId, abandonTag.getLong(uuidStr));
            } catch (IllegalArgumentException var12) {
            }
         }
      }

      if (nbt.hasKey("playerFlags")) {
         NBTTagCompound flagsTag = nbt.getCompoundTag("playerFlags");

         for(String uuidStr : flagsTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagList flagList = flagsTag.getTagList(uuidStr, 8);
               Set<String> flags = new HashSet();

               for(int i = 0; i < flagList.tagCount(); ++i) {
                  flags.add(flagList.getStringTagAt(i));
               }

               if (!flags.isEmpty()) {
                  this.playerFlags.put(playerId, flags);
               }
            } catch (IllegalArgumentException var16) {
            }
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagCompound activeTag = new NBTTagCompound();

      for(Map.Entry<UUID, List<QuestInstance>> entry : this.playerActiveQuests.entrySet()) {
         NBTTagList questList = new NBTTagList();

         for(QuestInstance quest : (List)entry.getValue()) {
            questList.appendTag(quest.writeToNBT(new NBTTagCompound()));
         }

         activeTag.setTag(((UUID)entry.getKey()).toString(), questList);
      }

      nbt.setTag("activeQuests", activeTag);
      NBTTagCompound completedTag = new NBTTagCompound();

      for(Map.Entry<UUID, Set<String>> entry : this.playerCompletedQuests.entrySet()) {
         NBTTagList idList = new NBTTagList();

         for(String questId : (Set)entry.getValue()) {
            idList.appendTag(new NBTTagString(questId));
         }

         completedTag.setTag(((UUID)entry.getKey()).toString(), idList);
      }

      nbt.setTag("completedQuests", completedTag);
      NBTTagCompound coordTag = new NBTTagCompound();

      for(Map.Entry<String, Map<Integer, BlockPos>> entry : this.coordinateOverrides.entrySet()) {
         NBTTagCompound stepOverrides = new NBTTagCompound();

         for(Map.Entry<Integer, BlockPos> stepEntry : ((Map)entry.getValue()).entrySet()) {
            NBTTagCompound posTag = new NBTTagCompound();
            posTag.setInteger("x", ((BlockPos)stepEntry.getValue()).getX());
            posTag.setInteger("y", ((BlockPos)stepEntry.getValue()).getY());
            posTag.setInteger("z", ((BlockPos)stepEntry.getValue()).getZ());
            stepOverrides.setTag(String.valueOf(stepEntry.getKey()), posTag);
         }

         coordTag.setTag((String)entry.getKey(), stepOverrides);
      }

      nbt.setTag("coordOverrides", coordTag);
      NBTTagCompound timesTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, Long>> entry : this.repeatableCompletionTimes.entrySet()) {
         NBTTagCompound questTimes = new NBTTagCompound();

         for(Map.Entry<String, Long> timeEntry : ((Map)entry.getValue()).entrySet()) {
            questTimes.setLong((String)timeEntry.getKey(), (Long)timeEntry.getValue());
         }

         timesTag.setTag(((UUID)entry.getKey()).toString(), questTimes);
      }

      nbt.setTag("repeatableTimes", timesTag);
      NBTTagCompound rerollTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, Integer>> entry : this.rerollCounts.entrySet()) {
         NBTTagCompound slotCounts = new NBTTagCompound();

         for(Map.Entry<String, Integer> slotEntry : ((Map)entry.getValue()).entrySet()) {
            slotCounts.setInteger((String)slotEntry.getKey(), (Integer)slotEntry.getValue());
         }

         rerollTag.setTag(((UUID)entry.getKey()).toString(), slotCounts);
      }

      nbt.setTag("rerollCounts", rerollTag);
      NBTTagCompound resetTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, Long>> entry : this.rerollResetTimes.entrySet()) {
         NBTTagCompound slotTimes = new NBTTagCompound();

         for(Map.Entry<String, Long> slotEntry : ((Map)entry.getValue()).entrySet()) {
            slotTimes.setLong((String)slotEntry.getKey(), (Long)slotEntry.getValue());
         }

         resetTag.setTag(((UUID)entry.getKey()).toString(), slotTimes);
      }

      nbt.setTag("rerollResetTimes", resetTag);
      NBTTagCompound bonusTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, Integer>> entry : this.bonusRerolls.entrySet()) {
         NBTTagCompound slotCounts = new NBTTagCompound();

         for(Map.Entry<String, Integer> slotEntry : ((Map)entry.getValue()).entrySet()) {
            slotCounts.setInteger((String)slotEntry.getKey(), (Integer)slotEntry.getValue());
         }

         bonusTag.setTag(((UUID)entry.getKey()).toString(), slotCounts);
      }

      nbt.setTag("bonusRerolls", bonusTag);
      NBTTagCompound ranksTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.playerMissionRanks.entrySet()) {
         ranksTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("missionRanks", ranksTag);
      NBTTagCompound offersTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, RepeatableQuestGenerator.QuestOffer>> entry : this.savedOffers.entrySet()) {
         NBTTagCompound slotOffers = new NBTTagCompound();

         for(Map.Entry<String, RepeatableQuestGenerator.QuestOffer> slotEntry : ((Map)entry.getValue()).entrySet()) {
            slotOffers.setTag((String)slotEntry.getKey(), ((RepeatableQuestGenerator.QuestOffer)slotEntry.getValue()).toNBT());
         }

         offersTag.setTag(((UUID)entry.getKey()).toString(), slotOffers);
      }

      nbt.setTag("savedOffers", offersTag);
      NBTTagCompound abandonTag = new NBTTagCompound();

      for(Map.Entry<UUID, Long> entry : this.randomAbandonTimes.entrySet()) {
         abandonTag.setLong(((UUID)entry.getKey()).toString(), (Long)entry.getValue());
      }

      nbt.setTag("randomAbandonTimes", abandonTag);
      NBTTagCompound flagsTag = new NBTTagCompound();

      for(Map.Entry<UUID, Set<String>> entry : this.playerFlags.entrySet()) {
         NBTTagList flagList = new NBTTagList();

         for(String flag : (Set)entry.getValue()) {
            flagList.appendTag(new NBTTagString(flag));
         }

         flagsTag.setTag(((UUID)entry.getKey()).toString(), flagList);
      }

      nbt.setTag("playerFlags", flagsTag);
      return nbt;
   }

   static {
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      cal.set(2026, 1, 28, 21, 0, 0);
      cal.set(14, 0);
      RESET_ANCHOR_MS = cal.getTimeInMillis();
   }
}
