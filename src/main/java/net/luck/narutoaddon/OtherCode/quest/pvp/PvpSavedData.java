
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.tournament.TournamentInstance;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class PvpSavedData extends WorldSavedData {
   private static final String DATA_NAME = "InfTsukPvpData";
   private static final long[] RANK_THRESHOLDS = new long[]{0L, 500L, 2000L, 6000L, 15000L, 40000L};
   private static final long DAY_MS = 86400000L;
   private static final long WEEK_MS = 604800000L;
   private static final long RESET_ANCHOR_MS;
   private final Map<UUID, Long> playerPvpXp = new HashMap();
   private final Map<UUID, Integer> playerPvpRanks = new HashMap();
   private final Map<UUID, Map<String, PvpMissionInstance>> activeInstances = new HashMap();
   private final Map<UUID, Map<String, PvpMissionOffer>> savedOffers = new HashMap();
   private final Map<UUID, Map<String, Long>> slotCooldowns = new HashMap();
   private final Map<UUID, Map<String, Integer>> rerollCounts = new HashMap();
   private final Map<UUID, Map<String, Long>> rerollResetTimes = new HashMap();
   private static final int MAX_PVP_REROLLS_DAILY = 5;
   private static final int MAX_PVP_REROLLS_WEEKLY = 5;
   private final Map<String, UUID> villageKages = new HashMap();
   private final List<WarInstance> activeWars = new ArrayList();
   private static final int MAX_ACTIVE_WARS = 3;
   private final Map<String, Long> warCooldowns = new HashMap();
   private final List<BlockPos> battlefieldPositions = new ArrayList();
   private BlockPos warTeam1Spawn = null;
   private BlockPos warTeam2Spawn = null;
   private final Map<UUID, UUID> mutualHunts = new HashMap();
   private final Map<UUID, Long> bingoClaimedTimes = new HashMap();
   private long lastBingoRotation = 0L;
   private NBTTagCompound bingoBookNBT = new NBTTagCompound();
   private final Map<String, List<UUID>> villageAdvisors = new HashMap();
   private final Map<String, List<VillageOrder>> villageOrders = new HashMap();
   private final Map<UUID, PvpMissionInstance> leadershipInstances = new HashMap();
   private final Map<UUID, PvpMissionOffer> leadershipOffers = new HashMap();
   private final Map<String, OperationTracker> operationTrackers = new HashMap();
   private final Map<String, TournamentInstance> activeTournaments = new HashMap();
   private final Map<UUID, Long> tournamentCooldowns = new HashMap();
   private final Map<UUID, Integer> pendingTournamentRyo = new HashMap();

   public PvpSavedData() {
      super("InfTsukPvpData");
   }

   public PvpSavedData(String name) {
      super(name);
   }

   public static PvpSavedData get(World world) {
      MapStorage storage = world.getMapStorage();
      if (storage == null) {
         return new PvpSavedData();
      } else {
         PvpSavedData data = (PvpSavedData)storage.getOrLoadData(PvpSavedData.class, "InfTsukPvpData");
         if (data == null) {
            data = new PvpSavedData();
            storage.setData("InfTsukPvpData", data);
         }

         return data;
      }
   }

   public void resetPlayerPvp(UUID playerId) {
      this.playerPvpXp.remove(playerId);
      this.playerPvpRanks.remove(playerId);
      this.activeInstances.remove(playerId);
      this.savedOffers.remove(playerId);
      this.slotCooldowns.remove(playerId);
      this.rerollCounts.remove(playerId);
      this.rerollResetTimes.remove(playerId);
      this.mutualHunts.remove(playerId);
      this.bingoClaimedTimes.remove(playerId);
      this.leadershipInstances.remove(playerId);
      this.leadershipOffers.remove(playerId);
      this.tournamentCooldowns.remove(playerId);
      this.pendingTournamentRyo.remove(playerId);
      this.markDirty();
   }

   public Set<UUID> getAllPlayersWithAnyPvpData() {
      Set<UUID> all = new HashSet();
      all.addAll(this.playerPvpXp.keySet());
      all.addAll(this.playerPvpRanks.keySet());
      all.addAll(this.activeInstances.keySet());
      all.addAll(this.savedOffers.keySet());
      all.addAll(this.slotCooldowns.keySet());
      all.addAll(this.rerollCounts.keySet());
      all.addAll(this.rerollResetTimes.keySet());
      all.addAll(this.leadershipInstances.keySet());
      all.addAll(this.leadershipOffers.keySet());
      all.addAll(this.tournamentCooldowns.keySet());
      all.addAll(this.pendingTournamentRyo.keySet());
      return Collections.unmodifiableSet(all);
   }

   public long getPvpXp(UUID playerId) {
      return (Long)this.playerPvpXp.getOrDefault(playerId, 0L);
   }

   public boolean addPvpXp(UUID playerId, long amount) {
      long oldXp = this.getPvpXp(playerId);
      int oldRank = this.calculateRankFromXp(oldXp);
      long newXp = oldXp + amount;
      this.playerPvpXp.put(playerId, newXp);
      int newRank = this.calculateRankFromXp(newXp);
      if (!this.playerPvpRanks.containsKey(playerId) || newRank > (Integer)this.playerPvpRanks.get(playerId)) {
         this.playerPvpRanks.put(playerId, newRank);
      }

      this.markDirty();
      return newRank > oldRank;
   }

   public QuestDefinition.QuestRank getPvpRank(UUID playerId) {
      int ordinal;
      if (this.playerPvpRanks.containsKey(playerId)) {
         ordinal = (Integer)this.playerPvpRanks.get(playerId);
      } else {
         ordinal = this.calculateRankFromXp(this.getPvpXp(playerId));
      }

      QuestDefinition.QuestRank[] ranks = QuestDefinition.QuestRank.values();
      return ordinal >= 0 && ordinal < ranks.length ? ranks[ordinal] : QuestDefinition.QuestRank.D;
   }

   public void setPvpRank(UUID playerId, QuestDefinition.QuestRank rank) {
      this.playerPvpRanks.put(playerId, rank.ordinal());
      this.markDirty();
   }

   private int calculateRankFromXp(long xp) {
      for(int i = RANK_THRESHOLDS.length - 1; i >= 0; --i) {
         if (xp >= RANK_THRESHOLDS[i]) {
            return i;
         }
      }

      return 0;
   }

   public PvpMissionInstance getActiveInstance(UUID playerId, String subSlot) {
      Map<String, PvpMissionInstance> slots = (Map)this.activeInstances.get(playerId);
      return slots != null ? (PvpMissionInstance)slots.get(subSlot) : null;
   }

   public Map<String, PvpMissionInstance> getActiveInstances(UUID playerId) {
      Map<String, PvpMissionInstance> slots = (Map)this.activeInstances.get(playerId);
      return slots != null ? new HashMap(slots) : new HashMap();
   }

   public void setActiveInstance(UUID playerId, String subSlot, PvpMissionInstance instance) {
      ((Map)this.activeInstances.computeIfAbsent(playerId, (k) -> new HashMap())).put(subSlot, instance);
      this.markDirty();
   }

   public void removeActiveInstance(UUID playerId, String subSlot) {
      Map<String, PvpMissionInstance> slots = (Map)this.activeInstances.get(playerId);
      if (slots != null) {
         slots.remove(subSlot);
         if (slots.isEmpty()) {
            this.activeInstances.remove(playerId);
         }
      }

      this.markDirty();
   }

   public PvpMissionOffer getSavedOffer(UUID playerId, String subSlot) {
      Map<String, PvpMissionOffer> slots = (Map)this.savedOffers.get(playerId);
      return slots != null ? (PvpMissionOffer)slots.get(subSlot) : null;
   }

   public Map<String, PvpMissionOffer> getSavedOffers(UUID playerId) {
      Map<String, PvpMissionOffer> slots = (Map)this.savedOffers.get(playerId);
      return slots != null ? new HashMap(slots) : new HashMap();
   }

   public void saveOffer(UUID playerId, String subSlot, PvpMissionOffer offer) {
      ((Map)this.savedOffers.computeIfAbsent(playerId, (k) -> new HashMap())).put(subSlot, offer);
      this.markDirty();
   }

   public void removeOffer(UUID playerId, String subSlot) {
      Map<String, PvpMissionOffer> slots = (Map)this.savedOffers.get(playerId);
      if (slots != null) {
         slots.remove(subSlot);
         if (slots.isEmpty()) {
            this.savedOffers.remove(playerId);
         }
      }

      this.markDirty();
   }

   public boolean isSlotOnCooldown(UUID playerId, String slot) {
      if (slot.startsWith("pvp_random")) {
         return false;
      } else {
         Map<String, Long> times = (Map)this.slotCooldowns.get(playerId);
         if (times == null) {
            return false;
         } else {
            Long completionTime = (Long)times.get(slot);
            if (completionTime == null) {
               return false;
            } else {
               long lastReset = slot.startsWith("pvp_daily") ? getLastDailyReset() : getLastWeeklyReset();
               return completionTime >= lastReset;
            }
         }
      }
   }

   public long getSlotCooldownRemaining(UUID playerId, String slot) {
      if (!this.isSlotOnCooldown(playerId, slot)) {
         return 0L;
      } else {
         long nextReset = slot.startsWith("pvp_daily") ? getNextDailyReset() : getNextWeeklyReset();
         return Math.max(0L, nextReset - System.currentTimeMillis());
      }
   }

   public void recordSlotCompletion(UUID playerId, String slot) {
      if (!slot.startsWith("pvp_random")) {
         ((Map)this.slotCooldowns.computeIfAbsent(playerId, (k) -> new HashMap())).put(slot, System.currentTimeMillis());
         this.markDirty();
      }
   }

   public int getRerollsRemaining(UUID playerId, String baseCategory) {
      if ("pvp_random".equals(baseCategory)) {
         return Integer.MAX_VALUE;
      } else {
         this.resetRerollsIfNeeded(playerId, baseCategory);
         int max = "pvp_daily".equals(baseCategory) ? 5 : 5;
         Map<String, Integer> counts = (Map)this.rerollCounts.get(playerId);
         return counts == null ? max : Math.max(0, max - (Integer)counts.getOrDefault(baseCategory, 0));
      }
   }

   public boolean useReroll(UUID playerId, String baseCategory) {
      if ("pvp_random".equals(baseCategory)) {
         return true;
      } else {
         this.resetRerollsIfNeeded(playerId, baseCategory);
         int max = "pvp_daily".equals(baseCategory) ? 5 : 5;
         Map<String, Integer> counts = (Map)this.rerollCounts.computeIfAbsent(playerId, (k) -> new HashMap());
         int used = (Integer)counts.getOrDefault(baseCategory, 0);
         if (used >= max) {
            return false;
         } else {
            counts.put(baseCategory, used + 1);
            ((Map)this.rerollResetTimes.computeIfAbsent(playerId, (k) -> new HashMap())).putIfAbsent(baseCategory, System.currentTimeMillis());
            this.markDirty();
            return true;
         }
      }
   }

   private void resetRerollsIfNeeded(UUID playerId, String baseCategory) {
      Map<String, Long> resetTimes = (Map)this.rerollResetTimes.get(playerId);
      if (resetTimes != null) {
         Long lastTimestamp = (Long)resetTimes.get(baseCategory);
         if (lastTimestamp != null) {
            long lastReset = "pvp_daily".equals(baseCategory) ? getLastDailyReset() : getLastWeeklyReset();
            if (lastReset > lastTimestamp) {
               Map<String, Integer> counts = (Map)this.rerollCounts.get(playerId);
               if (counts != null) {
                  counts.remove(baseCategory);
               }

               resetTimes.remove(baseCategory);
               this.markDirty();
            }

         }
      }
   }

   public UUID getKage(String villageName) {
      return (UUID)this.villageKages.get(villageName);
   }

   public void setKage(String villageName, UUID kageId) {
      this.villageKages.put(villageName, kageId);
      this.markDirty();
   }

   public void removeKage(String villageName) {
      this.villageKages.remove(villageName);
      this.markDirty();
   }

   public Map<String, UUID> getAllKages() {
      return new HashMap(this.villageKages);
   }

   public List<WarInstance> getActiveWars() {
      return this.activeWars;
   }

   public WarInstance getWarById(String warId) {
      for(WarInstance war : this.activeWars) {
         if (war.getWarId().equals(warId)) {
            return war;
         }
      }

      return null;
   }

   public boolean addWar(WarInstance war) {
      if (this.activeWars.size() >= 3) {
         return false;
      } else {
         this.activeWars.add(war);
         this.markDirty();
         return true;
      }
   }

   public void removeWar(String warId) {
      this.activeWars.removeIf((w) -> w.getWarId().equals(warId));
      this.markDirty();
   }

   public boolean isVillageAtWar(String villageName) {
      for(WarInstance war : this.activeWars) {
         if (war.getVillage1().teamName.equals(villageName) || war.getVillage2().teamName.equals(villageName)) {
            return true;
         }
      }

      return false;
   }

   public boolean isWarOnCooldown(String villageName) {
      Long cooldownEnd = (Long)this.warCooldowns.get(villageName);
      return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd;
   }

   public long getWarCooldownRemaining(String villageName) {
      Long cooldownEnd = (Long)this.warCooldowns.get(villageName);
      return cooldownEnd == null ? 0L : Math.max(0L, cooldownEnd - System.currentTimeMillis());
   }

   public void setWarCooldown(String villageName, long cooldownEndMs) {
      this.warCooldowns.put(villageName, cooldownEndMs);
      this.markDirty();
   }

   public List<BlockPos> getBattlefieldPositions() {
      return new ArrayList(this.battlefieldPositions);
   }

   public void addBattlefieldPosition(BlockPos pos) {
      this.battlefieldPositions.add(pos);
      this.markDirty();
   }

   public void clearBattlefieldPositions() {
      this.battlefieldPositions.clear();
      this.markDirty();
   }

   public BlockPos getWarTeam1Spawn() {
      return this.warTeam1Spawn;
   }

   public BlockPos getWarTeam2Spawn() {
      return this.warTeam2Spawn;
   }

   public void setWarTeam1Spawn(BlockPos pos) {
      this.warTeam1Spawn = pos;
      WarBattlefield.setTeam1Spawn(pos);
      this.markDirty();
   }

   public void setWarTeam2Spawn(BlockPos pos) {
      this.warTeam2Spawn = pos;
      WarBattlefield.setTeam2Spawn(pos);
      this.markDirty();
   }

   public UUID getMutualHuntTarget(UUID playerId) {
      return (UUID)this.mutualHunts.get(playerId);
   }

   public void setMutualHunt(UUID player1, UUID player2) {
      this.mutualHunts.put(player1, player2);
      this.mutualHunts.put(player2, player1);
      this.markDirty();
   }

   public void removeMutualHunt(UUID playerId) {
      UUID other = (UUID)this.mutualHunts.remove(playerId);
      if (other != null) {
         this.mutualHunts.remove(other);
      }

      this.markDirty();
   }

   public long getLastBingoRotation() {
      return this.lastBingoRotation;
   }

   public void setLastBingoRotation(long time) {
      this.lastBingoRotation = time;
      this.markDirty();
   }

   public boolean hasBingoClaimed(UUID playerId) {
      Long claimed = (Long)this.bingoClaimedTimes.get(playerId);
      if (claimed == null) {
         return false;
      } else {
         return claimed >= getLastDailyReset();
      }
   }

   public void recordBingoClaimed(UUID playerId) {
      this.bingoClaimedTimes.put(playerId, System.currentTimeMillis());
      this.markDirty();
   }

   public NBTTagCompound getBingoBookNBT() {
      return this.bingoBookNBT;
   }

   public void clearBingoBookCache() {
      this.bingoBookNBT = new NBTTagCompound();
      this.markDirty();
   }

   public List<UUID> getAdvisors(String village) {
      List<UUID> advisors = (List)this.villageAdvisors.get(village);
      return advisors != null ? new ArrayList(advisors) : new ArrayList();
   }

   public void setAdvisors(String village, List<UUID> advisors) {
      this.villageAdvisors.put(village, new ArrayList(advisors));
      this.markDirty();
   }

   public void removeAdvisor(String village, UUID playerId) {
      List<UUID> advisors = (List)this.villageAdvisors.get(village);
      if (advisors != null) {
         advisors.remove(playerId);
         if (advisors.isEmpty()) {
            this.villageAdvisors.remove(village);
         }
      }

      this.markDirty();
   }

   public Map<String, List<UUID>> getAllAdvisors() {
      Map<String, List<UUID>> copy = new HashMap();

      for(Map.Entry<String, List<UUID>> entry : this.villageAdvisors.entrySet()) {
         copy.put(entry.getKey(), new ArrayList((Collection)entry.getValue()));
      }

      return copy;
   }

   public List<VillageOrder> getOrders(String village) {
      List<VillageOrder> orders = (List)this.villageOrders.get(village);
      return orders != null ? new ArrayList(orders) : new ArrayList();
   }

   public void addOrder(String village, VillageOrder order) {
      ((List)this.villageOrders.computeIfAbsent(village, (k) -> new ArrayList())).add(order);
      this.markDirty();
   }

   public void removeOrder(String village, String orderId) {
      List<VillageOrder> orders = (List)this.villageOrders.get(village);
      if (orders != null) {
         orders.removeIf((o) -> o.getOrderId().equals(orderId));
         if (orders.isEmpty()) {
            this.villageOrders.remove(village);
         }
      }

      this.markDirty();
   }

   public void setOrders(String village, List<VillageOrder> orders) {
      if (orders.isEmpty()) {
         this.villageOrders.remove(village);
      } else {
         this.villageOrders.put(village, new ArrayList(orders));
      }

      this.markDirty();
   }

   public PvpMissionInstance getLeadershipInstance(UUID playerId) {
      return (PvpMissionInstance)this.leadershipInstances.get(playerId);
   }

   public void setLeadershipInstance(UUID playerId, PvpMissionInstance instance) {
      this.leadershipInstances.put(playerId, instance);
      this.markDirty();
   }

   public void removeLeadershipInstance(UUID playerId) {
      this.leadershipInstances.remove(playerId);
      this.markDirty();
   }

   public PvpMissionOffer getLeadershipOffer(UUID playerId) {
      return (PvpMissionOffer)this.leadershipOffers.get(playerId);
   }

   public void saveLeadershipOffer(UUID playerId, PvpMissionOffer offer) {
      this.leadershipOffers.put(playerId, offer);
      this.markDirty();
   }

   public void removeLeadershipOffer(UUID playerId) {
      this.leadershipOffers.remove(playerId);
      this.markDirty();
   }

   public OperationTracker getOperationTracker(String orderId) {
      return (OperationTracker)this.operationTrackers.get(orderId);
   }

   public void setOperationTracker(String orderId, OperationTracker tracker) {
      this.operationTrackers.put(orderId, tracker);
      this.markDirty();
   }

   public void removeOperationTracker(String orderId) {
      this.operationTrackers.remove(orderId);
      this.markDirty();
   }

   public TournamentInstance getTournament(String village) {
      return (TournamentInstance)this.activeTournaments.get(village);
   }

   public void setTournament(String village, TournamentInstance inst) {
      this.activeTournaments.put(village, inst);
      this.markDirty();
   }

   public void removeTournament(String village) {
      this.activeTournaments.remove(village);
      this.markDirty();
   }

   public Map<String, TournamentInstance> getAllTournaments() {
      return new HashMap(this.activeTournaments);
   }

   public long getTournamentCooldown(UUID playerId) {
      Long cd = (Long)this.tournamentCooldowns.get(playerId);
      return cd != null ? cd : 0L;
   }

   public void setTournamentCooldown(UUID playerId, long timestamp) {
      this.tournamentCooldowns.put(playerId, timestamp);
      this.markDirty();
   }

   public Map<UUID, Long> getAllTournamentCooldowns() {
      return new HashMap(this.tournamentCooldowns);
   }

   public int getPendingTournamentRyo(UUID playerId) {
      Integer ryo = (Integer)this.pendingTournamentRyo.get(playerId);
      return ryo != null ? ryo : 0;
   }

   public void setPendingTournamentRyo(UUID playerId, int amount) {
      if (amount <= 0) {
         this.pendingTournamentRyo.remove(playerId);
      } else {
         this.pendingTournamentRyo.put(playerId, amount);
      }

      this.markDirty();
   }

   public int removePendingTournamentRyo(UUID playerId) {
      Integer ryo = (Integer)this.pendingTournamentRyo.remove(playerId);
      if (ryo != null && ryo > 0) {
         this.markDirty();
         return ryo;
      } else {
         return 0;
      }
   }

   public static boolean isOfferStale(String subSlot, long offerCreatedAt) {
      if (subSlot.startsWith("pvp_random")) {
         return false;
      } else {
         long lastReset = subSlot.startsWith("pvp_daily") ? getLastDailyReset() : getLastWeeklyReset();
         return offerCreatedAt < lastReset;
      }
   }

   private static long getLastDailyReset() {
      long now = System.currentTimeMillis();
      long elapsed = now - RESET_ANCHOR_MS;
      return elapsed < 0L ? RESET_ANCHOR_MS - 86400000L : RESET_ANCHOR_MS + elapsed / 86400000L * 86400000L;
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
      return elapsed < 0L ? RESET_ANCHOR_MS - 604800000L : RESET_ANCHOR_MS + elapsed / 604800000L * 604800000L;
   }

   private static long getNextWeeklyReset() {
      long now = System.currentTimeMillis();
      long elapsed = now - RESET_ANCHOR_MS;
      long periods = elapsed < 0L ? 0L : elapsed / 604800000L + 1L;
      return RESET_ANCHOR_MS + periods * 604800000L;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.playerPvpXp.clear();
      this.playerPvpRanks.clear();
      this.activeInstances.clear();
      this.savedOffers.clear();
      this.slotCooldowns.clear();
      this.rerollCounts.clear();
      this.rerollResetTimes.clear();
      this.villageKages.clear();
      this.activeWars.clear();
      this.warCooldowns.clear();
      this.battlefieldPositions.clear();
      this.mutualHunts.clear();
      this.bingoClaimedTimes.clear();
      this.villageAdvisors.clear();
      this.villageOrders.clear();
      this.leadershipInstances.clear();
      this.leadershipOffers.clear();
      this.operationTrackers.clear();
      this.activeTournaments.clear();
      this.tournamentCooldowns.clear();
      this.pendingTournamentRyo.clear();
      if (nbt.hasKey("pvpXp")) {
         NBTTagCompound xpTag = nbt.getCompoundTag("pvpXp");

         for(String uuidStr : xpTag.getKeySet()) {
            try {
               this.playerPvpXp.put(UUID.fromString(uuidStr), xpTag.getLong(uuidStr));
            } catch (IllegalArgumentException var30) {
            }
         }
      }

      if (nbt.hasKey("pvpRanks")) {
         NBTTagCompound ranksTag = nbt.getCompoundTag("pvpRanks");

         for(String uuidStr : ranksTag.getKeySet()) {
            try {
               this.playerPvpRanks.put(UUID.fromString(uuidStr), ranksTag.getInteger(uuidStr));
            } catch (IllegalArgumentException var29) {
            }
         }
      }

      if (nbt.hasKey("activeInstances")) {
         NBTTagCompound instTag = nbt.getCompoundTag("activeInstances");

         for(String uuidStr : instTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagCompound slotTag = instTag.getCompoundTag(uuidStr);
               Map<String, PvpMissionInstance> slots = new HashMap();

               for(String subSlot : slotTag.getKeySet()) {
                  try {
                     slots.put(subSlot, PvpMissionInstance.readFromNBT(slotTag.getCompoundTag(subSlot)));
                  } catch (Exception var28) {
                  }
               }

               if (!slots.isEmpty()) {
                  this.activeInstances.put(playerId, slots);
               }
            } catch (IllegalArgumentException var35) {
            }
         }
      }

      if (nbt.hasKey("savedOffers")) {
         NBTTagCompound offersTag = nbt.getCompoundTag("savedOffers");

         for(String uuidStr : offersTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagCompound slotTag = offersTag.getCompoundTag(uuidStr);
               Map<String, PvpMissionOffer> slots = new HashMap();

               for(String subSlot : slotTag.getKeySet()) {
                  try {
                     slots.put(subSlot, PvpMissionOffer.readFromNBT(slotTag.getCompoundTag(subSlot)));
                  } catch (Exception var27) {
                  }
               }

               if (!slots.isEmpty()) {
                  this.savedOffers.put(playerId, slots);
               }
            } catch (IllegalArgumentException var34) {
            }
         }
      }

      if (nbt.hasKey("slotCooldowns")) {
         NBTTagCompound cdTag = nbt.getCompoundTag("slotCooldowns");

         for(String uuidStr : cdTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagCompound slotTag = cdTag.getCompoundTag(uuidStr);
               Map<String, Long> slots = new HashMap();

               for(String slot : slotTag.getKeySet()) {
                  slots.put(slot, slotTag.getLong(slot));
               }

               this.slotCooldowns.put(playerId, slots);
            } catch (IllegalArgumentException var33) {
            }
         }
      }

      if (nbt.hasKey("pvpRerollCounts")) {
         NBTTagCompound rerollTag = nbt.getCompoundTag("pvpRerollCounts");

         for(String uuidStr : rerollTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagCompound catTag = rerollTag.getCompoundTag(uuidStr);
               Map<String, Integer> counts = new HashMap();

               for(String cat : catTag.getKeySet()) {
                  counts.put(cat, catTag.getInteger(cat));
               }

               this.rerollCounts.put(playerId, counts);
            } catch (IllegalArgumentException var32) {
            }
         }
      }

      if (nbt.hasKey("pvpRerollResetTimes")) {
         NBTTagCompound resetTag = nbt.getCompoundTag("pvpRerollResetTimes");

         for(String uuidStr : resetTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               NBTTagCompound catTag = resetTag.getCompoundTag(uuidStr);
               Map<String, Long> times = new HashMap();

               for(String cat : catTag.getKeySet()) {
                  times.put(cat, catTag.getLong(cat));
               }

               this.rerollResetTimes.put(playerId, times);
            } catch (IllegalArgumentException var31) {
            }
         }
      }

      if (nbt.hasKey("villageKages")) {
         NBTTagCompound kageTag = nbt.getCompoundTag("villageKages");

         for(String village : kageTag.getKeySet()) {
            try {
               this.villageKages.put(village, UUID.fromString(kageTag.getString(village)));
            } catch (IllegalArgumentException var26) {
            }
         }
      }

      KageManager kageManager = KageManager.getInstance();

      for(Map.Entry<String, UUID> entry : this.villageKages.entrySet()) {
         VillageHelper.Village v = villageFromTeamName((String)entry.getKey());
         if (v != null) {
            kageManager.setKageDirect(v, (UUID)entry.getValue());
         }
      }

      if (nbt.hasKey("activeWars")) {
         NBTTagList warList = nbt.getTagList("activeWars", 10);

         for(int i = 0; i < warList.tagCount(); ++i) {
            try {
               this.activeWars.add(WarInstance.readFromNBT(warList.getCompoundTagAt(i)));
            } catch (Exception var25) {
            }
         }
      }

      if (nbt.hasKey("warCooldowns")) {
         NBTTagCompound cdTag = nbt.getCompoundTag("warCooldowns");

         for(String village : cdTag.getKeySet()) {
            this.warCooldowns.put(village, cdTag.getLong(village));
         }
      }

      WarManager warManager = WarManager.getInstance();

      for(Map.Entry<String, Long> entry : this.warCooldowns.entrySet()) {
         if ((Long)entry.getValue() > System.currentTimeMillis()) {
            VillageHelper.Village v = villageFromTeamName((String)entry.getKey());
            if (v != null) {
               warManager.setCooldown(v, (Long)entry.getValue());
            }
         }
      }

      if (nbt.hasKey("battlefieldPositions")) {
         NBTTagList posList = nbt.getTagList("battlefieldPositions", 10);

         for(int i = 0; i < posList.tagCount(); ++i) {
            NBTTagCompound posTag = posList.getCompoundTagAt(i);
            this.battlefieldPositions.add(new BlockPos(posTag.getInteger("x"), posTag.getInteger("y"), posTag.getInteger("z")));
         }
      }

      if (nbt.hasKey("warTeam1Spawn")) {
         NBTTagCompound t = nbt.getCompoundTag("warTeam1Spawn");
         this.warTeam1Spawn = new BlockPos(t.getInteger("x"), t.getInteger("y"), t.getInteger("z"));
         WarBattlefield.setTeam1Spawn(this.warTeam1Spawn);
      }

      if (nbt.hasKey("warTeam2Spawn")) {
         NBTTagCompound t = nbt.getCompoundTag("warTeam2Spawn");
         this.warTeam2Spawn = new BlockPos(t.getInteger("x"), t.getInteger("y"), t.getInteger("z"));
         WarBattlefield.setTeam2Spawn(this.warTeam2Spawn);
      }

      if (nbt.hasKey("mutualHunts")) {
         NBTTagCompound huntTag = nbt.getCompoundTag("mutualHunts");

         for(String uuidStr : huntTag.getKeySet()) {
            try {
               this.mutualHunts.put(UUID.fromString(uuidStr), UUID.fromString(huntTag.getString(uuidStr)));
            } catch (IllegalArgumentException var24) {
            }
         }
      }

      this.lastBingoRotation = nbt.getLong("lastBingoRotation");
      if (this.lastBingoRotation == 0L) {
         this.lastBingoRotation = nbt.getLong("lastBountyRotation");
      }

      String bingoClaimedKey = nbt.hasKey("bingoClaimedTimes") ? "bingoClaimedTimes" : "bountyClaimedTimes";
      if (nbt.hasKey(bingoClaimedKey)) {
         NBTTagCompound claimedTag = nbt.getCompoundTag(bingoClaimedKey);

         for(String uuidStr : claimedTag.getKeySet()) {
            try {
               this.bingoClaimedTimes.put(UUID.fromString(uuidStr), claimedTag.getLong(uuidStr));
            } catch (IllegalArgumentException var23) {
            }
         }
      }

      String bingoBookKey = nbt.hasKey("bingoBook") ? "bingoBook" : "bountyBoard";
      if (nbt.hasKey(bingoBookKey)) {
         this.bingoBookNBT = nbt.getCompoundTag(bingoBookKey);

         try {
            BingoBook.getInstance().readFromNBT(this.bingoBookNBT);
         } catch (Exception var22) {
         }
      }

      if (nbt.hasKey("villageAdvisors")) {
         NBTTagCompound advTag = nbt.getCompoundTag("villageAdvisors");

         for(String village : advTag.getKeySet()) {
            NBTTagList list = advTag.getTagList(village, 8);
            List<UUID> advisors = new ArrayList();

            for(int i = 0; i < list.tagCount(); ++i) {
               try {
                  advisors.add(UUID.fromString(list.getStringTagAt(i)));
               } catch (IllegalArgumentException var21) {
               }
            }

            if (!advisors.isEmpty()) {
               this.villageAdvisors.put(village, advisors);
            }
         }
      }

      AdvisorManager advisorManager = AdvisorManager.getInstance();

      for(Map.Entry<String, List<UUID>> entry : this.villageAdvisors.entrySet()) {
         VillageHelper.Village v = villageFromTeamName((String)entry.getKey());
         if (v != null) {
            for(UUID advisorId : (List)entry.getValue()) {
               advisorManager.addAdvisorDirect(v, advisorId);
            }
         }
      }

      if (nbt.hasKey("villageOrders")) {
         NBTTagCompound ordTag = nbt.getCompoundTag("villageOrders");

         for(String village : ordTag.getKeySet()) {
            NBTTagList list = ordTag.getTagList(village, 10);
            List<VillageOrder> orders = new ArrayList();

            for(int i = 0; i < list.tagCount(); ++i) {
               try {
                  orders.add(VillageOrder.readFromNBT(list.getCompoundTagAt(i)));
               } catch (Exception var20) {
               }
            }

            if (!orders.isEmpty()) {
               this.villageOrders.put(village, orders);
            }
         }
      }

      if (nbt.hasKey("leadershipInstances")) {
         NBTTagCompound ldrTag = nbt.getCompoundTag("leadershipInstances");

         for(String uuidStr : ldrTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               this.leadershipInstances.put(playerId, PvpMissionInstance.readFromNBT(ldrTag.getCompoundTag(uuidStr)));
            } catch (Exception var19) {
            }
         }
      }

      if (nbt.hasKey("leadershipOffers")) {
         NBTTagCompound ldrOffTag = nbt.getCompoundTag("leadershipOffers");

         for(String uuidStr : ldrOffTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidStr);
               this.leadershipOffers.put(playerId, PvpMissionOffer.readFromNBT(ldrOffTag.getCompoundTag(uuidStr)));
            } catch (Exception var18) {
            }
         }
      }

      if (nbt.hasKey("operationTrackers")) {
         NBTTagCompound opTag = nbt.getCompoundTag("operationTrackers");

         for(String orderId : opTag.getKeySet()) {
            try {
               this.operationTrackers.put(orderId, OperationTracker.readFromNBT(opTag.getCompoundTag(orderId)));
            } catch (Exception var17) {
            }
         }
      }

      if (nbt.hasKey("activeTournaments")) {
         NBTTagCompound tournTag = nbt.getCompoundTag("activeTournaments");

         for(String key : tournTag.getKeySet()) {
            try {
               this.activeTournaments.put(key, TournamentInstance.readFromNBT(tournTag.getCompoundTag(key)));
            } catch (Exception var16) {
            }
         }
      }

      if (nbt.hasKey("tournamentCooldowns")) {
         NBTTagCompound cdTag = nbt.getCompoundTag("tournamentCooldowns");

         for(String uuidStr : cdTag.getKeySet()) {
            try {
               this.tournamentCooldowns.put(UUID.fromString(uuidStr), cdTag.getLong(uuidStr));
            } catch (IllegalArgumentException var15) {
            }
         }
      }

      if (nbt.hasKey("pendingTournamentRyo")) {
         NBTTagCompound ryoTag = nbt.getCompoundTag("pendingTournamentRyo");

         for(String uuidStr : ryoTag.getKeySet()) {
            try {
               this.pendingTournamentRyo.put(UUID.fromString(uuidStr), ryoTag.getInteger(uuidStr));
            } catch (IllegalArgumentException var14) {
            }
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagCompound xpTag = new NBTTagCompound();

      for(Map.Entry<UUID, Long> entry : this.playerPvpXp.entrySet()) {
         xpTag.setLong(((UUID)entry.getKey()).toString(), (Long)entry.getValue());
      }

      nbt.setTag("pvpXp", xpTag);
      NBTTagCompound ranksTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.playerPvpRanks.entrySet()) {
         ranksTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("pvpRanks", ranksTag);
      NBTTagCompound instTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, PvpMissionInstance>> entry : this.activeInstances.entrySet()) {
         NBTTagCompound slotTag = new NBTTagCompound();

         for(Map.Entry<String, PvpMissionInstance> slotEntry : ((Map)entry.getValue()).entrySet()) {
            slotTag.setTag((String)slotEntry.getKey(), ((PvpMissionInstance)slotEntry.getValue()).writeToNBT());
         }

         instTag.setTag(((UUID)entry.getKey()).toString(), slotTag);
      }

      nbt.setTag("activeInstances", instTag);
      NBTTagCompound offersTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, PvpMissionOffer>> entry : this.savedOffers.entrySet()) {
         NBTTagCompound slotTag = new NBTTagCompound();

         for(Map.Entry<String, PvpMissionOffer> slotEntry : ((Map)entry.getValue()).entrySet()) {
            slotTag.setTag((String)slotEntry.getKey(), ((PvpMissionOffer)slotEntry.getValue()).writeToNBT());
         }

         offersTag.setTag(((UUID)entry.getKey()).toString(), slotTag);
      }

      nbt.setTag("savedOffers", offersTag);
      NBTTagCompound cdTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, Long>> entry : this.slotCooldowns.entrySet()) {
         NBTTagCompound slotTag = new NBTTagCompound();

         for(Map.Entry<String, Long> slotEntry : ((Map)entry.getValue()).entrySet()) {
            slotTag.setLong((String)slotEntry.getKey(), (Long)slotEntry.getValue());
         }

         cdTag.setTag(((UUID)entry.getKey()).toString(), slotTag);
      }

      nbt.setTag("slotCooldowns", cdTag);
      NBTTagCompound rerollTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, Integer>> entry : this.rerollCounts.entrySet()) {
         NBTTagCompound catTag = new NBTTagCompound();

         for(Map.Entry<String, Integer> catEntry : ((Map)entry.getValue()).entrySet()) {
            catTag.setInteger((String)catEntry.getKey(), (Integer)catEntry.getValue());
         }

         rerollTag.setTag(((UUID)entry.getKey()).toString(), catTag);
      }

      nbt.setTag("pvpRerollCounts", rerollTag);
      NBTTagCompound resetTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, Long>> entry : this.rerollResetTimes.entrySet()) {
         NBTTagCompound catTag = new NBTTagCompound();

         for(Map.Entry<String, Long> catEntry : ((Map)entry.getValue()).entrySet()) {
            catTag.setLong((String)catEntry.getKey(), (Long)catEntry.getValue());
         }

         resetTag.setTag(((UUID)entry.getKey()).toString(), catTag);
      }

      nbt.setTag("pvpRerollResetTimes", resetTag);
      this.villageKages.clear();
      this.villageKages.putAll(KageManager.getInstance().getAllKages());
      NBTTagCompound kageTag = new NBTTagCompound();

      for(Map.Entry<String, UUID> entry : this.villageKages.entrySet()) {
         kageTag.setString((String)entry.getKey(), ((UUID)entry.getValue()).toString());
      }

      nbt.setTag("villageKages", kageTag);
      NBTTagList warList = new NBTTagList();

      for(WarInstance war : this.activeWars) {
         warList.appendTag(war.writeToNBT());
      }

      nbt.setTag("activeWars", warList);
      this.warCooldowns.clear();
      WarManager warMgr = WarManager.getInstance();

      for(VillageHelper.Village v : VillageHelper.Village.values()) {
         if (v != VillageHelper.Village.UNKNOWN) {
            long remaining = warMgr.getRemainingCooldown(v);
            if (remaining > 0L) {
               this.warCooldowns.put(v.teamName, System.currentTimeMillis() + remaining);
            }
         }
      }

      NBTTagCompound warCdTag = new NBTTagCompound();

      for(Map.Entry<String, Long> entry : this.warCooldowns.entrySet()) {
         warCdTag.setLong((String)entry.getKey(), (Long)entry.getValue());
      }

      nbt.setTag("warCooldowns", warCdTag);
      NBTTagList posList = new NBTTagList();

      for(BlockPos pos : this.battlefieldPositions) {
         NBTTagCompound posTag = new NBTTagCompound();
         posTag.setInteger("x", pos.getX());
         posTag.setInteger("y", pos.getY());
         posTag.setInteger("z", pos.getZ());
         posList.appendTag(posTag);
      }

      nbt.setTag("battlefieldPositions", posList);
      if (this.warTeam1Spawn != null) {
         NBTTagCompound t = new NBTTagCompound();
         t.setInteger("x", this.warTeam1Spawn.getX());
         t.setInteger("y", this.warTeam1Spawn.getY());
         t.setInteger("z", this.warTeam1Spawn.getZ());
         nbt.setTag("warTeam1Spawn", t);
      }

      if (this.warTeam2Spawn != null) {
         NBTTagCompound t = new NBTTagCompound();
         t.setInteger("x", this.warTeam2Spawn.getX());
         t.setInteger("y", this.warTeam2Spawn.getY());
         t.setInteger("z", this.warTeam2Spawn.getZ());
         nbt.setTag("warTeam2Spawn", t);
      }

      NBTTagCompound huntTag = new NBTTagCompound();

      for(Map.Entry<UUID, UUID> entry : this.mutualHunts.entrySet()) {
         huntTag.setString(((UUID)entry.getKey()).toString(), ((UUID)entry.getValue()).toString());
      }

      nbt.setTag("mutualHunts", huntTag);
      nbt.setLong("lastBingoRotation", this.lastBingoRotation);
      NBTTagCompound claimedTag = new NBTTagCompound();

      for(Map.Entry<UUID, Long> entry : this.bingoClaimedTimes.entrySet()) {
         claimedTag.setLong(((UUID)entry.getKey()).toString(), (Long)entry.getValue());
      }

      nbt.setTag("bingoClaimedTimes", claimedTag);

      try {
         nbt.setTag("bingoBook", BingoBook.getInstance().writeToNBT());
      } catch (Exception var27) {
      }

      this.villageAdvisors.clear();
      AdvisorManager advMgr = AdvisorManager.getInstance();

      for(VillageHelper.Village v : VillageHelper.Village.values()) {
         if (v != VillageHelper.Village.UNKNOWN) {
            List<UUID> advList = advMgr.getAdvisors(v);
            if (!advList.isEmpty()) {
               this.villageAdvisors.put(v.teamName, advList);
            }
         }
      }

      NBTTagCompound advTag = new NBTTagCompound();

      for(Map.Entry<String, List<UUID>> entry : this.villageAdvisors.entrySet()) {
         NBTTagList list = new NBTTagList();

         for(UUID uuid : (List)entry.getValue()) {
            list.appendTag(new NBTTagString(uuid.toString()));
         }

         advTag.setTag((String)entry.getKey(), list);
      }

      nbt.setTag("villageAdvisors", advTag);
      NBTTagCompound ordTag = new NBTTagCompound();

      for(Map.Entry<String, List<VillageOrder>> entry : this.villageOrders.entrySet()) {
         NBTTagList list = new NBTTagList();

         for(VillageOrder order : (List)entry.getValue()) {
            list.appendTag(order.writeToNBT());
         }

         ordTag.setTag((String)entry.getKey(), list);
      }

      nbt.setTag("villageOrders", ordTag);
      NBTTagCompound ldrTag = new NBTTagCompound();

      for(Map.Entry<UUID, PvpMissionInstance> entry : this.leadershipInstances.entrySet()) {
         ldrTag.setTag(((UUID)entry.getKey()).toString(), ((PvpMissionInstance)entry.getValue()).writeToNBT());
      }

      nbt.setTag("leadershipInstances", ldrTag);
      NBTTagCompound ldrOffTag = new NBTTagCompound();

      for(Map.Entry<UUID, PvpMissionOffer> entry : this.leadershipOffers.entrySet()) {
         ldrOffTag.setTag(((UUID)entry.getKey()).toString(), ((PvpMissionOffer)entry.getValue()).writeToNBT());
      }

      nbt.setTag("leadershipOffers", ldrOffTag);
      NBTTagCompound opTag = new NBTTagCompound();

      for(Map.Entry<String, OperationTracker> entry : this.operationTrackers.entrySet()) {
         opTag.setTag((String)entry.getKey(), ((OperationTracker)entry.getValue()).writeToNBT());
      }

      nbt.setTag("operationTrackers", opTag);
      NBTTagCompound tournTag = new NBTTagCompound();

      for(Map.Entry<String, TournamentInstance> entry : this.activeTournaments.entrySet()) {
         tournTag.setTag((String)entry.getKey(), ((TournamentInstance)entry.getValue()).writeToNBT());
      }

      nbt.setTag("activeTournaments", tournTag);
      NBTTagCompound tCdTag = new NBTTagCompound();

      for(Map.Entry<UUID, Long> entry : this.tournamentCooldowns.entrySet()) {
         tCdTag.setLong(((UUID)entry.getKey()).toString(), (Long)entry.getValue());
      }

      nbt.setTag("tournamentCooldowns", tCdTag);
      NBTTagCompound pRyoTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.pendingTournamentRyo.entrySet()) {
         pRyoTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("pendingTournamentRyo", pRyoTag);
      return nbt;
   }

   private static VillageHelper.Village villageFromTeamName(String teamName) {
      for(VillageHelper.Village v : VillageHelper.Village.values()) {
         if (v != VillageHelper.Village.UNKNOWN && v.teamName.equals(teamName)) {
            return v;
         }
      }

      return null;
   }

   static {
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      cal.set(2026, 1, 28, 21, 0, 0);
      cal.set(14, 0);
      RESET_ANCHOR_MS = cal.getTimeInMillis();
   }
}
