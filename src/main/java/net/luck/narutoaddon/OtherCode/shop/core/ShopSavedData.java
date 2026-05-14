
package net.luck.narutoaddon.OtherCode.shop.core;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class ShopSavedData extends WorldSavedData {
   private static final String DATA_NAME = "InfTsukShopData";
   private final Map<UUID, Long> playerRyoBank = new HashMap();
   private final Map<UUID, Map<String, List<ShopHistoryEntry>>> categorizedHistory = new HashMap();
   private final Map<UUID, Map<String, Integer>> pityCounters = new HashMap();
   private final Map<UUID, Map<String, Integer>> sPlusPityCounters = new HashMap();
   private final Map<UUID, Integer> tokenBalance = new HashMap();
   private final Map<UUID, Long> lastLoginTimestamp = new HashMap();
   private final Map<UUID, Integer> loginStreak = new HashMap();
   private final Map<UUID, Long> firstPveWinTimestamp = new HashMap();
   private final Map<UUID, Long> firstPvpWinTimestamp = new HashMap();
   private final Map<UUID, Set<String>> completedFtbQuests = new HashMap();
   private final Map<UUID, NBTTagCompound> pendingCrateResults = new HashMap();
   private final Map<UUID, Set<String>> ownedItems = new HashMap();
   private final Map<UUID, List<NBTTagCompound>> overflowItems = new HashMap();
   private static final int MAX_OVERFLOW_ITEMS = 100;
   private final Map<UUID, Integer> crystalBalance = new HashMap();
   private final Set<UUID> resetApplied = new HashSet();
   private final Set<UUID> usedFreeRoll = new HashSet();
   private final Set<UUID> usedFirstPurchaseBonus = new HashSet();
   private final Map<UUID, Long> lifetimeRyoSpent = new HashMap();
   private final Map<UUID, Integer> lifetimeCratesOpened = new HashMap();
   private final Map<UUID, Integer> totalCrystalsPurchased = new HashMap();
   private final Map<UUID, Integer> totalCrystalsSpent = new HashMap();
   private final Map<UUID, Integer> crystalPurchaseCount = new HashMap();
   private final Map<UUID, Long> firstPurchaseDate = new HashMap();
   private final Map<UUID, Long> lastPurchaseDate = new HashMap();
   private final Map<UUID, Map<String, Integer>> rollsPerCrate = new HashMap();
   private final Map<UUID, Integer> pityHitCount = new HashMap();
   private final Map<UUID, Integer> dupeRefundTotal = new HashMap();
   private final Map<UUID, Boolean> usedFreeRollThenBought = new HashMap();
   private final Map<UUID, Integer> totalCrystalsFromTrades = new HashMap();
   private final Map<UUID, Integer> bpPurchasedTier = new HashMap();
   private final Map<UUID, Integer> bpCurrentLevel = new HashMap();
   private final Map<UUID, Integer> bpCurrentXP = new HashMap();
   private final Map<UUID, Set<Integer>> bpClaimedTiers = new HashMap();
   private final Map<UUID, Integer> bpOtsutsukiSkipsUsed = new HashMap();
   private final Map<UUID, String> activeKillEffect = new HashMap();
   private final Map<UUID, Set<String>> ownedKillEffects = new HashMap();
   private int bpSeasonNumber = 0;
   private long bpSeasonStartTime = 0L;
   private final Map<UUID, Map<String, Integer>> bpDailyXPCaps = new HashMap();
   private long bpDailyCapResetTime = 0L;
   private final Map<UUID, Set<Integer>> bpPendingAutoClaimTiers = new HashMap();
   private final Map<UUID, Integer> bpPendingAutoClaimSeason = new HashMap();
   private final Map<UUID, Integer> bpPendingAutoClaimPass = new HashMap();
   private boolean weekendPromoSO6PEnabled = true;
   private static final int MAX_HISTORY_PER_CATEGORY = 100;
   public static final long MAX_BALANCE = 999999999L;
   public static final int MAX_TOKENS = 999999;
   public static final int MAX_CRYSTALS = 999999;

   public ShopSavedData() {
      super("InfTsukShopData");
   }

   public ShopSavedData(String name) {
      super(name);
   }

   public static ShopSavedData get(World world) {
      MapStorage storage = world.getMapStorage();
      ShopSavedData data = (ShopSavedData)storage.getOrLoadData(ShopSavedData.class, "InfTsukShopData");
      if (data == null) {
         data = new ShopSavedData();
         storage.setData("InfTsukShopData", data);
      }

      return data;
   }

   public long getBalance(UUID playerId) {
      return (Long)this.playerRyoBank.getOrDefault(playerId, 0L);
   }

   public void addBalance(UUID playerId, long amount) {
      if (amount > 0L) {
         long current = this.getBalance(playerId);
         long newBalance = current + amount;
         if (newBalance < current || newBalance > 999999999L) {
            newBalance = 999999999L;
         }

         this.playerRyoBank.put(playerId, newBalance);
         this.markDirty();
      }
   }

   public boolean removeBalance(UUID playerId, long amount) {
      if (amount <= 0L) {
         return false;
      } else {
         long balance = this.getBalance(playerId);
         if (balance < amount) {
            return false;
         } else {
            this.playerRyoBank.put(playerId, balance - amount);
            this.markDirty();
            return true;
         }
      }
   }

   public void setBalance(UUID playerId, long amount) {
      this.playerRyoBank.put(playerId, Math.min(999999999L, Math.max(0L, amount)));
      this.markDirty();
   }

   private static String getHistoryCategory(String crateId) {
      if (crateId != null && !crateId.isEmpty()) {
         if (crateId.startsWith("weapon_crate")) {
            return "ryo_weapon";
         } else if (crateId.startsWith("clan_crate")) {
            return "ryo_clan";
         } else if (crateId.startsWith("so6p_crate")) {
            return "ryo_so6p";
         } else if (crateId.startsWith("jutsu_crate")) {
            return "ryo_jutsu";
         } else if (crateId.startsWith("armor_crate")) {
            return "ryo_armor";
         } else if (!crateId.startsWith("supply_crate") && !crateId.startsWith("xp_scroll")) {
            if (crateId.equals("cash_weapon")) {
               return "crystal_weapon";
            } else if (crateId.equals("cash_clan")) {
               return "crystal_clan";
            } else if (crateId.equals("cash_so6p")) {
               return "crystal_so6p";
            } else if (crateId.equals("cash_jutsu")) {
               return "crystal_jutsu";
            } else if (crateId.equals("cash_mythical")) {
               return "crystal_mythical";
            } else if (crateId.equals("cash_tailed_beast")) {
               return "crystal_tailed_beast";
            } else if (crateId.equals("cash_chakra")) {
               return "crystal_consumable";
            } else {
               return crateId.equals("token_purchase") ? "token" : "other";
            }
         } else {
            return "ryo_supply";
         }
      } else {
         return "other";
      }
   }

   public void addHistoryEntry(UUID playerId, ShopHistoryEntry entry) {
      String category = getHistoryCategory(entry.getCrateId());
      Map<String, List<ShopHistoryEntry>> playerHistory = (Map)this.categorizedHistory.computeIfAbsent(playerId, (k) -> new HashMap());
      List<ShopHistoryEntry> catList = (List)playerHistory.computeIfAbsent(category, (k) -> new ArrayList());
      catList.add(0, entry);

      while(catList.size() > 100) {
         catList.remove(catList.size() - 1);
      }

      this.markDirty();
   }

   public List<ShopHistoryEntry> getHistory(UUID playerId) {
      Map<String, List<ShopHistoryEntry>> playerHistory = (Map)this.categorizedHistory.get(playerId);
      if (playerHistory == null) {
         return new ArrayList();
      } else {
         List<ShopHistoryEntry> merged = new ArrayList();

         for(List<ShopHistoryEntry> catList : playerHistory.values()) {
            merged.addAll(catList);
         }

         merged.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
         return merged;
      }
   }

   public List<ShopHistoryEntry> getHistoryByCategory(UUID playerId, String category) {
      Map<String, List<ShopHistoryEntry>> playerHistory = (Map)this.categorizedHistory.get(playerId);
      return (List<ShopHistoryEntry>)(playerHistory == null ? new ArrayList() : (List)playerHistory.getOrDefault(category, new ArrayList()));
   }

   public void clearHistory(UUID playerId) {
      this.categorizedHistory.remove(playerId);
      this.markDirty();
   }

   public int getPityCount(UUID playerId, String crateId) {
      Map<String, Integer> counters = (Map)this.pityCounters.get(playerId);
      return counters == null ? 0 : (Integer)counters.getOrDefault(crateId, 0);
   }

   public void incrementPity(UUID playerId, String crateId) {
      Map<String, Integer> counters = (Map)this.pityCounters.computeIfAbsent(playerId, (k) -> new HashMap());
      counters.put(crateId, (Integer)counters.getOrDefault(crateId, 0) + 1);
      this.markDirty();
   }

   public void resetPity(UUID playerId, String crateId) {
      Map<String, Integer> counters = (Map)this.pityCounters.get(playerId);
      if (counters != null) {
         counters.remove(crateId);
         this.markDirty();
      }

   }

   public int getSPlusPityCount(UUID playerId, String crateId) {
      Map<String, Integer> counters = (Map)this.sPlusPityCounters.get(playerId);
      return counters == null ? 0 : (Integer)counters.getOrDefault(crateId, 0);
   }

   public void incrementSPlusPity(UUID playerId, String crateId) {
      Map<String, Integer> counters = (Map)this.sPlusPityCounters.computeIfAbsent(playerId, (k) -> new HashMap());
      counters.put(crateId, (Integer)counters.getOrDefault(crateId, 0) + 1);
      this.markDirty();
   }

   public void resetSPlusPity(UUID playerId, String crateId) {
      Map<String, Integer> counters = (Map)this.sPlusPityCounters.get(playerId);
      if (counters != null) {
         counters.remove(crateId);
         this.markDirty();
      }

   }

   public int getTokens(UUID playerId) {
      return (Integer)this.tokenBalance.getOrDefault(playerId, 0);
   }

   public void addTokens(UUID playerId, int amount) {
      if (amount > 0) {
         int current = this.getTokens(playerId);
         int newBalance = current + amount;
         if (newBalance < current || newBalance > 999999) {
            newBalance = 999999;
         }

         this.tokenBalance.put(playerId, newBalance);
         this.markDirty();
      }
   }

   public boolean removeTokens(UUID playerId, int amount) {
      if (amount <= 0) {
         return false;
      } else {
         int balance = this.getTokens(playerId);
         if (balance < amount) {
            return false;
         } else {
            this.tokenBalance.put(playerId, balance - amount);
            this.markDirty();
            return true;
         }
      }
   }

   public boolean hasTokens(UUID playerId, int amount) {
      return this.getTokens(playerId) >= amount;
   }

   public void setTokens(UUID playerId, int amount) {
      this.tokenBalance.put(playerId, Math.max(0, amount));
      this.markDirty();
   }

   public int getCrystals(UUID playerId) {
      return (Integer)this.crystalBalance.getOrDefault(playerId, 0);
   }

   public void addCrystals(UUID playerId, int amount) {
      if (amount > 0) {
         int current = this.getCrystals(playerId);
         int newBalance = current + amount;
         if (newBalance < current || newBalance > 999999) {
            newBalance = 999999;
         }

         this.crystalBalance.put(playerId, newBalance);
         this.markDirty();
         String caller = getCrystalAuditCaller();
         System.out.println("[CrystalAudit] ADD " + amount + " CC to " + playerId + " | " + current + " -> " + newBalance + " | source=" + caller);
      }
   }

   public boolean removeCrystals(UUID playerId, int amount) {
      if (amount <= 0) {
         return false;
      } else {
         int balance = this.getCrystals(playerId);
         if (balance < amount) {
            return false;
         } else {
            int newBalance = balance - amount;
            this.crystalBalance.put(playerId, newBalance);
            this.markDirty();
            String caller = getCrystalAuditCaller();
            System.out.println("[CrystalAudit] REMOVE " + amount + " CC from " + playerId + " | " + balance + " -> " + newBalance + " | source=" + caller);
            return true;
         }
      }
   }

   private static String getCrystalAuditCaller() {
      StackTraceElement[] stack = Thread.currentThread().getStackTrace();

      for(int i = 3; i < Math.min(stack.length, 8); ++i) {
         String cls = stack[i].getClassName();
         if (!cls.contains("ShopSavedData")) {
            String shortClass = cls.substring(cls.lastIndexOf(46) + 1);
            return shortClass + ":" + stack[i].getMethodName() + ":" + stack[i].getLineNumber();
         }
      }

      return "unknown";
   }

   public boolean hasCrystals(UUID playerId, int amount) {
      return this.getCrystals(playerId) >= amount;
   }

   public int getCrystalsFromTrades(UUID playerId) {
      return (Integer)this.totalCrystalsFromTrades.getOrDefault(playerId, 0);
   }

   public void addCrystalsFromTradesStat(UUID playerId, int amount) {
      if (amount > 0) {
         this.totalCrystalsFromTrades.put(playerId, (Integer)this.totalCrystalsFromTrades.getOrDefault(playerId, 0) + amount);
         this.markDirty();
      }
   }

   public void addCrystalsFromTrade(UUID playerId, int amount) {
      if (amount > 0) {
         this.addCrystals(playerId, amount);
         this.addCrystalsFromTradesStat(playerId, amount);
      }
   }

   public boolean hasUsedFreeRoll(UUID playerId) {
      return this.usedFreeRoll.contains(playerId);
   }

   public void markFreeRollUsed(UUID playerId) {
      this.usedFreeRoll.add(playerId);
      this.markDirty();
   }

   public boolean hasResetBeenApplied(UUID playerId) {
      return this.resetApplied.contains(playerId);
   }

   public void markResetApplied(UUID playerId) {
      this.resetApplied.add(playerId);
      this.markDirty();
   }

   public boolean hasUsedFirstPurchaseBonus(UUID playerId) {
      return this.usedFirstPurchaseBonus.contains(playerId);
   }

   public void markFirstPurchaseBonusUsed(UUID playerId) {
      this.usedFirstPurchaseBonus.add(playerId);
      this.markDirty();
   }

   public int getLoginStreak(UUID playerId) {
      return (Integer)this.loginStreak.getOrDefault(playerId, 0);
   }

   public int updateLoginStreak(UUID playerId) {
      long now = System.currentTimeMillis();
      long lastLogin = (Long)this.lastLoginTimestamp.getOrDefault(playerId, 0L);
      long nowDay = now / 86400000L;
      long lastDay = lastLogin / 86400000L;
      long dayDiff = nowDay - lastDay;
      if (dayDiff == 0L) {
         return 0;
      } else {
         int streak;
         if (dayDiff == 1L) {
            streak = (Integer)this.loginStreak.getOrDefault(playerId, 0) + 1;
            if (streak > 7) {
               streak = 1;
            }
         } else {
            streak = 1;
         }

         this.loginStreak.put(playerId, streak);
         this.lastLoginTimestamp.put(playerId, now);
         this.markDirty();
         return streak;
      }
   }

   public boolean canClaimFirstPveWin(UUID playerId) {
      long lastClaim = (Long)this.firstPveWinTimestamp.getOrDefault(playerId, 0L);
      long nowDay = System.currentTimeMillis() / 86400000L;
      long claimDay = lastClaim / 86400000L;
      return nowDay > claimDay;
   }

   public void claimFirstPveWin(UUID playerId) {
      this.firstPveWinTimestamp.put(playerId, System.currentTimeMillis());
      this.markDirty();
   }

   public boolean canClaimFirstPvpWin(UUID playerId) {
      long lastClaim = (Long)this.firstPvpWinTimestamp.getOrDefault(playerId, 0L);
      long nowDay = System.currentTimeMillis() / 86400000L;
      long claimDay = lastClaim / 86400000L;
      return nowDay > claimDay;
   }

   public void claimFirstPvpWin(UUID playerId) {
      this.firstPvpWinTimestamp.put(playerId, System.currentTimeMillis());
      this.markDirty();
   }

   public boolean hasCompletedFtbQuest(UUID playerId, String ftbQuestId) {
      Set<String> quests = (Set)this.completedFtbQuests.get(playerId);
      return quests != null && quests.contains(ftbQuestId);
   }

   public void addCompletedFtbQuest(UUID playerId, String ftbQuestId) {
      ((Set)this.completedFtbQuests.computeIfAbsent(playerId, (k) -> new HashSet())).add(ftbQuestId);
      this.markDirty();
   }

   public int countPlayersWithOwnedItem(String itemKey) {
      int count = 0;

      for(Set<String> items : this.ownedItems.values()) {
         if (items != null && items.contains(itemKey)) {
            ++count;
         }
      }

      return count;
   }

   public boolean hasOwnedItem(UUID playerId, String itemKey) {
      Set<String> items = (Set)this.ownedItems.get(playerId);
      return items != null && items.contains(itemKey);
   }

   public void addOwnedItem(UUID playerId, String itemKey) {
      ((Set)this.ownedItems.computeIfAbsent(playerId, (k) -> new HashSet())).add(itemKey);
      this.markDirty();
   }

   public void removeOwnedItem(UUID playerId, String itemKey) {
      Set<String> items = (Set)this.ownedItems.get(playerId);
      if (items != null && items.remove(itemKey)) {
         this.markDirty();
      }

   }

   public void clearOwnedItems(UUID playerId) {
      Set<String> items = (Set)this.ownedItems.remove(playerId);
      if (items != null && !items.isEmpty()) {
         this.markDirty();
      }

   }

   public int clearAllOwnedItems() {
      int count = this.ownedItems.size();
      if (count > 0) {
         this.ownedItems.clear();
         this.markDirty();
      }

      return count;
   }

   public int clearOwnedItemsByKeys(Set<String> keysToRemove) {
      int playersAffected = 0;
      Iterator<Map.Entry<UUID, Set<String>>> it = this.ownedItems.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<UUID, Set<String>> entry = (Map.Entry)it.next();
         boolean changed = ((Set)entry.getValue()).removeAll(keysToRemove);
         if (changed) {
            ++playersAffected;
            if (((Set)entry.getValue()).isEmpty()) {
               it.remove();
            }
         }
      }

      if (playersAffected > 0) {
         this.markDirty();
      }

      return playersAffected;
   }

   public int resetDuplicateTrackingAll() {
      int keysRemoved = 0;
      Iterator<Map.Entry<UUID, Set<String>>> it = this.ownedItems.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<UUID, Set<String>> entry = (Map.Entry)it.next();
         Set<String> items = (Set)entry.getValue();
         Iterator<String> keyIt = items.iterator();

         while(keyIt.hasNext()) {
            String k = (String)keyIt.next();
            if (k.indexOf(58) >= 0) {
               keyIt.remove();
               ++keysRemoved;
            }
         }

         if (items.isEmpty()) {
            it.remove();
         }
      }

      int ftbPlayersCleared = this.completedFtbQuests.size();
      this.completedFtbQuests.clear();
      if (keysRemoved > 0 || ftbPlayersCleared > 0) {
         this.markDirty();
      }

      return keysRemoved + ftbPlayersCleared;
   }

   public void addOverflowItem(UUID playerId, NBTTagCompound itemNbt) {
      List<NBTTagCompound> items = (List)this.overflowItems.computeIfAbsent(playerId, (k) -> new ArrayList());
      if (items.size() < 100) {
         items.add(itemNbt);
         this.markDirty();
      }

   }

   public List<NBTTagCompound> getOverflowItems(UUID playerId) {
      List<NBTTagCompound> items = (List)this.overflowItems.get(playerId);
      return items != null ? new ArrayList(items) : new ArrayList();
   }

   public List<NBTTagCompound> claimAllOverflow(UUID playerId) {
      List<NBTTagCompound> items = (List)this.overflowItems.remove(playerId);
      if (items != null && !items.isEmpty()) {
         this.markDirty();
         return items;
      } else {
         return new ArrayList();
      }
   }

   public int getOverflowCount(UUID playerId) {
      List<NBTTagCompound> items = (List)this.overflowItems.get(playerId);
      return items != null ? items.size() : 0;
   }

   public void setPendingCrateResult(UUID playerId, NBTTagCompound pendingNbt) {
      if (pendingNbt != null) {
         this.pendingCrateResults.put(playerId, pendingNbt);
      } else {
         this.pendingCrateResults.remove(playerId);
      }

      this.markDirty();
   }

   public NBTTagCompound claimPendingCrateResult(UUID playerId) {
      NBTTagCompound result = (NBTTagCompound)this.pendingCrateResults.remove(playerId);
      if (result != null) {
         this.markDirty();
      }

      return result;
   }

   public boolean hasPendingCrateResult(UUID playerId) {
      return this.pendingCrateResults.containsKey(playerId);
   }

   public Set<String> getOwnedItems(UUID playerId) {
      Set<String> items = (Set)this.ownedItems.get(playerId);
      return items != null ? new HashSet(items) : new HashSet();
   }

   public Set<UUID> getPlayersWithOwnedItem(String marker) {
      Set<UUID> result = new HashSet();

      for(Map.Entry<UUID, Set<String>> entry : this.ownedItems.entrySet()) {
         if (((Set)entry.getValue()).contains(marker)) {
            result.add(entry.getKey());
         }
      }

      return result;
   }

   public long getLifetimeSpent(UUID playerId) {
      return (Long)this.lifetimeRyoSpent.getOrDefault(playerId, 0L);
   }

   public void addLifetimeSpent(UUID playerId, long amount) {
      if (amount > 0L) {
         long current = (Long)this.lifetimeRyoSpent.getOrDefault(playerId, 0L);
         this.lifetimeRyoSpent.put(playerId, current + amount);
         this.markDirty();
      }
   }

   public void subtractLifetimeSpent(UUID playerId, long amount) {
      if (amount > 0L) {
         long current = (Long)this.lifetimeRyoSpent.getOrDefault(playerId, 0L);
         this.lifetimeRyoSpent.put(playerId, Math.max(0L, current - amount));
         this.markDirty();
      }
   }

   public int getLifetimeCratesOpened(UUID playerId) {
      return (Integer)this.lifetimeCratesOpened.getOrDefault(playerId, 0);
   }

   public void incrementLifetimeCratesOpened(UUID playerId) {
      this.lifetimeCratesOpened.put(playerId, (Integer)this.lifetimeCratesOpened.getOrDefault(playerId, 0) + 1);
      this.markDirty();
   }

   public void recordCrystalPurchase(UUID playerId, int amount) {
      if (amount > 0) {
         this.totalCrystalsPurchased.put(playerId, (Integer)this.totalCrystalsPurchased.getOrDefault(playerId, 0) + amount);
         this.crystalPurchaseCount.put(playerId, (Integer)this.crystalPurchaseCount.getOrDefault(playerId, 0) + 1);
         long now = System.currentTimeMillis();
         if (!this.firstPurchaseDate.containsKey(playerId)) {
            this.firstPurchaseDate.put(playerId, now);
         }

         this.lastPurchaseDate.put(playerId, now);
         if (this.usedFreeRoll.contains(playerId) && !this.usedFreeRollThenBought.containsKey(playerId)) {
            this.usedFreeRollThenBought.put(playerId, true);
         }

         this.markDirty();
      }
   }

   public void recordCrystalSpent(UUID playerId, int amount) {
      if (amount > 0) {
         this.totalCrystalsSpent.put(playerId, (Integer)this.totalCrystalsSpent.getOrDefault(playerId, 0) + amount);
         this.markDirty();
      }
   }

   public void recordCrateRoll(UUID playerId, String crateId) {
      Map<String, Integer> playerRolls = (Map)this.rollsPerCrate.computeIfAbsent(playerId, (k) -> new HashMap());
      playerRolls.put(crateId, (Integer)playerRolls.getOrDefault(crateId, 0) + 1);
      this.markDirty();
   }

   public void recordPityHit(UUID playerId) {
      this.pityHitCount.put(playerId, (Integer)this.pityHitCount.getOrDefault(playerId, 0) + 1);
      this.markDirty();
   }

   public void recordDupeRefund(UUID playerId, int crystals) {
      if (crystals > 0) {
         this.dupeRefundTotal.put(playerId, (Integer)this.dupeRefundTotal.getOrDefault(playerId, 0) + crystals);
         this.markDirty();
      }
   }

   public int getTotalCrystalsPurchased(UUID playerId) {
      return (Integer)this.totalCrystalsPurchased.getOrDefault(playerId, 0);
   }

   public int getTotalCrystalsSpent(UUID playerId) {
      return (Integer)this.totalCrystalsSpent.getOrDefault(playerId, 0);
   }

   public int getCrystalPurchaseCount(UUID playerId) {
      return (Integer)this.crystalPurchaseCount.getOrDefault(playerId, 0);
   }

   public long getFirstPurchaseDate(UUID playerId) {
      return (Long)this.firstPurchaseDate.getOrDefault(playerId, 0L);
   }

   public long getLastPurchaseDate(UUID playerId) {
      return (Long)this.lastPurchaseDate.getOrDefault(playerId, 0L);
   }

   public Map<String, Integer> getRollsPerCrate(UUID playerId) {
      Map<String, Integer> rolls = (Map)this.rollsPerCrate.get(playerId);
      return rolls != null ? new HashMap(rolls) : new HashMap();
   }

   public int getPityHitCount(UUID playerId) {
      return (Integer)this.pityHitCount.getOrDefault(playerId, 0);
   }

   public int getDupeRefundTotal(UUID playerId) {
      return (Integer)this.dupeRefundTotal.getOrDefault(playerId, 0);
   }

   public boolean getUsedFreeRollThenBought(UUID playerId) {
      return (Boolean)this.usedFreeRollThenBought.getOrDefault(playerId, false);
   }

   public String getTopCrateForPlayer(UUID playerId) {
      Map<String, Integer> rolls = (Map)this.rollsPerCrate.get(playerId);
      if (rolls != null && !rolls.isEmpty()) {
         String top = null;
         int topCount = 0;

         for(Map.Entry<String, Integer> entry : rolls.entrySet()) {
            if ((Integer)entry.getValue() > topCount) {
               topCount = (Integer)entry.getValue();
               top = (String)entry.getKey();
            }
         }

         return top;
      } else {
         return null;
      }
   }

   public int getCrateRollCount(UUID playerId, String crateId) {
      Map<String, Integer> rolls = (Map)this.rollsPerCrate.get(playerId);
      return rolls != null ? (Integer)rolls.getOrDefault(crateId, 0) : 0;
   }

   public Set<UUID> getAllCrystalBuyers() {
      return new HashSet(this.totalCrystalsPurchased.keySet());
   }

   public int getServerTotalCrystalsPurchased() {
      int total = 0;

      for(int v : this.totalCrystalsPurchased.values()) {
         total += v;
      }

      return total;
   }

   public int getServerTotalCrystalsSpent() {
      int total = 0;

      for(int v : this.totalCrystalsSpent.values()) {
         total += v;
      }

      return total;
   }

   public int getServerTotalDupeRefunds() {
      int total = 0;

      for(int v : this.dupeRefundTotal.values()) {
         total += v;
      }

      return total;
   }

   public int getServerRepeatBuyerCount() {
      int count = 0;

      for(int v : this.crystalPurchaseCount.values()) {
         if (v >= 2) {
            ++count;
         }
      }

      return count;
   }

   public int getServerFreeRollConversions() {
      int count = 0;

      for(Boolean v : this.usedFreeRollThenBought.values()) {
         if (v != null && v) {
            ++count;
         }
      }

      return count;
   }

   public int getServerFreeRollTotal() {
      return this.usedFreeRoll.size();
   }

   public String getServerMostPopularCrate() {
      Map<String, Integer> globalRolls = new HashMap();

      for(Map<String, Integer> playerRolls : this.rollsPerCrate.values()) {
         for(Map.Entry<String, Integer> entry : playerRolls.entrySet()) {
            globalRolls.put(entry.getKey(), (Integer)globalRolls.getOrDefault(entry.getKey(), 0) + (Integer)entry.getValue());
         }
      }

      String top = null;
      int topCount = 0;

      for(Map.Entry<String, Integer> entry : globalRolls.entrySet()) {
         if ((Integer)entry.getValue() > topCount) {
            topCount = (Integer)entry.getValue();
            top = (String)entry.getKey();
         }
      }

      return top != null ? top + " (" + topCount + " rolls)" : "none";
   }

   public int getBpPurchasedTier(UUID playerId) {
      return (Integer)this.bpPurchasedTier.getOrDefault(playerId, 0);
   }

   public void setBpPurchasedTier(UUID playerId, int tier) {
      this.bpPurchasedTier.put(playerId, tier);
      this.markDirty();
   }

   public int getBpCurrentLevel(UUID playerId) {
      return (Integer)this.bpCurrentLevel.getOrDefault(playerId, 0);
   }

   public void setBpCurrentLevel(UUID playerId, int level) {
      this.bpCurrentLevel.put(playerId, level);
      this.markDirty();
   }

   public int getBpCurrentXP(UUID playerId) {
      return (Integer)this.bpCurrentXP.getOrDefault(playerId, 0);
   }

   public void setBpCurrentXP(UUID playerId, int xp) {
      this.bpCurrentXP.put(playerId, xp);
      this.markDirty();
   }

   public boolean hasBpClaimedTier(UUID playerId, int tier) {
      Set<Integer> claimed = (Set)this.bpClaimedTiers.get(playerId);
      return claimed != null && claimed.contains(tier);
   }

   public void addBpClaimedTier(UUID playerId, int tier) {
      ((Set)this.bpClaimedTiers.computeIfAbsent(playerId, (k) -> new HashSet())).add(tier);
      this.markDirty();
   }

   public Set<Integer> getBpClaimedTiers(UUID playerId) {
      Set<Integer> claimed = (Set)this.bpClaimedTiers.get(playerId);
      return claimed != null ? new HashSet(claimed) : new HashSet();
   }

   public int getBpOtsutsukiSkipsUsed(UUID playerId) {
      return (Integer)this.bpOtsutsukiSkipsUsed.getOrDefault(playerId, 0);
   }

   public void setBpOtsutsukiSkipsUsed(UUID playerId, int used) {
      this.bpOtsutsukiSkipsUsed.put(playerId, used);
      this.markDirty();
   }

   public String getActiveKillEffect(UUID playerId) {
      return (String)this.activeKillEffect.getOrDefault(playerId, "");
   }

   public void setActiveKillEffect(UUID playerId, String effectId) {
      this.activeKillEffect.put(playerId, effectId != null ? effectId : "");
      this.markDirty();
   }

   public boolean hasOwnedKillEffect(UUID playerId, String effectId) {
      Set<String> owned = (Set)this.ownedKillEffects.get(playerId);
      return owned != null && owned.contains(effectId);
   }

   public void addOwnedKillEffect(UUID playerId, String effectId) {
      ((Set)this.ownedKillEffects.computeIfAbsent(playerId, (k) -> new HashSet())).add(effectId);
      this.markDirty();
   }

   public Set<String> getOwnedKillEffects(UUID playerId) {
      Set<String> owned = (Set)this.ownedKillEffects.get(playerId);
      return owned != null ? new HashSet(owned) : new HashSet();
   }

   public int getBpSeasonNumber() {
      return this.bpSeasonNumber;
   }

   public void setBpSeasonNumber(int num) {
      this.bpSeasonNumber = num;
      this.markDirty();
   }

   public boolean isWeekendPromoSO6PEnabled() {
      return this.weekendPromoSO6PEnabled;
   }

   public void setWeekendPromoSO6PEnabled(boolean enabled) {
      this.weekendPromoSO6PEnabled = enabled;
      this.markDirty();
   }

   public long getBpSeasonStartTime() {
      return this.bpSeasonStartTime;
   }

   public void setBpSeasonStartTime(long time) {
      this.bpSeasonStartTime = time;
      this.markDirty();
   }

   public int getBpDailyXPForSource(UUID playerId, String source) {
      Map<String, Integer> caps = (Map)this.bpDailyXPCaps.get(playerId);
      return caps != null ? (Integer)caps.getOrDefault(source, 0) : 0;
   }

   public void addBpDailyXP(UUID playerId, String source, int amount) {
      Map<String, Integer> caps = (Map)this.bpDailyXPCaps.computeIfAbsent(playerId, (k) -> new HashMap());
      caps.put(source, (Integer)caps.getOrDefault(source, 0) + amount);
      this.markDirty();
   }

   public long getBpDailyCapResetTime() {
      return this.bpDailyCapResetTime;
   }

   public void setBpDailyCapResetTime(long time) {
      this.bpDailyCapResetTime = time;
      this.markDirty();
   }

   public void resetBpDailyCaps() {
      this.bpDailyXPCaps.clear();
      this.markDirty();
   }

   public Set<Integer> getBpPendingAutoClaimTiers(UUID playerId) {
      Set<Integer> tiers = (Set)this.bpPendingAutoClaimTiers.get(playerId);
      return tiers != null ? new HashSet(tiers) : new HashSet();
   }

   public int getBpPendingAutoClaimSeason(UUID playerId) {
      return (Integer)this.bpPendingAutoClaimSeason.getOrDefault(playerId, 0);
   }

   public int getBpPendingAutoClaimPass(UUID playerId) {
      return (Integer)this.bpPendingAutoClaimPass.getOrDefault(playerId, 0);
   }

   public boolean hasBpPendingAutoClaim(UUID playerId) {
      Set<Integer> tiers = (Set)this.bpPendingAutoClaimTiers.get(playerId);
      return tiers != null && !tiers.isEmpty();
   }

   public void setBpPendingAutoClaim(UUID playerId, Set<Integer> tiers, int seasonNumber, int passTier) {
      if (tiers != null && !tiers.isEmpty()) {
         this.bpPendingAutoClaimTiers.put(playerId, new HashSet(tiers));
         this.bpPendingAutoClaimSeason.put(playerId, seasonNumber);
         this.bpPendingAutoClaimPass.put(playerId, passTier);
      } else {
         this.bpPendingAutoClaimTiers.remove(playerId);
         this.bpPendingAutoClaimSeason.remove(playerId);
         this.bpPendingAutoClaimPass.remove(playerId);
      }

      this.markDirty();
   }

   public void clearBpPendingAutoClaim(UUID playerId) {
      this.bpPendingAutoClaimTiers.remove(playerId);
      this.bpPendingAutoClaimSeason.remove(playerId);
      this.bpPendingAutoClaimPass.remove(playerId);
      this.markDirty();
   }

   public Set<UUID> getAllBattlePassPlayers() {
      Set<UUID> all = new HashSet();
      all.addAll(this.bpCurrentLevel.keySet());
      all.addAll(this.bpClaimedTiers.keySet());
      return all;
   }

   public void resetAllBattlePassData() {
      this.bpPurchasedTier.clear();
      this.bpCurrentLevel.clear();
      this.bpCurrentXP.clear();
      this.bpClaimedTiers.clear();
      this.bpOtsutsukiSkipsUsed.clear();
      this.bpDailyXPCaps.clear();
      this.bpDailyCapResetTime = 0L;

      for(Map.Entry<UUID, Set<String>> entry : this.ownedItems.entrySet()) {
         ((Set)entry.getValue()).removeIf((key) -> key.startsWith("bp_tier_"));
      }

      this.markDirty();
   }

   public void resetBattlePass(UUID playerId) {
      this.bpPurchasedTier.remove(playerId);
      this.bpCurrentLevel.remove(playerId);
      this.bpCurrentXP.remove(playerId);
      this.bpClaimedTiers.remove(playerId);
      this.bpOtsutsukiSkipsUsed.remove(playerId);
      Map<String, Integer> caps = (Map)this.bpDailyXPCaps.get(playerId);
      if (caps != null) {
         caps.clear();
      }

      this.markDirty();
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.playerRyoBank.clear();
      if (nbt.hasKey("ryoBank")) {
         NBTTagCompound bankTag = nbt.getCompoundTag("ryoBank");

         for(String key : bankTag.getKeySet()) {
            try {
               this.playerRyoBank.put(UUID.fromString(key), bankTag.getLong(key));
            } catch (IllegalArgumentException var41) {
            }
         }
      }

      this.categorizedHistory.clear();
      if (nbt.hasKey("categorizedHistory")) {
         NBTTagCompound historyTag = nbt.getCompoundTag("categorizedHistory");

         for(String uuidKey : historyTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(uuidKey);
               NBTTagCompound playerTag = historyTag.getCompoundTag(uuidKey);
               Map<String, List<ShopHistoryEntry>> playerHistory = new HashMap();

               for(String category : playerTag.getKeySet()) {
                  NBTTagList entryList = playerTag.getTagList(category, 10);
                  List<ShopHistoryEntry> entries = new ArrayList();

                  for(int i = 0; i < entryList.tagCount(); ++i) {
                     entries.add(ShopHistoryEntry.readFromNBT(entryList.getCompoundTagAt(i)));
                  }

                  playerHistory.put(category, entries);
               }

               this.categorizedHistory.put(playerId, playerHistory);
            } catch (IllegalArgumentException var53) {
            }
         }
      } else if (nbt.hasKey("history")) {
         NBTTagCompound historyTag = nbt.getCompoundTag("history");

         for(String key : historyTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(key);
               NBTTagList entryList = historyTag.getTagList(key, 10);
               Map<String, List<ShopHistoryEntry>> playerHistory = new HashMap();

               for(int i = 0; i < entryList.tagCount(); ++i) {
                  ShopHistoryEntry entry = ShopHistoryEntry.readFromNBT(entryList.getCompoundTagAt(i));
                  String category = getHistoryCategory(entry.getCrateId());
                  ((List)playerHistory.computeIfAbsent(category, (k) -> new ArrayList())).add(entry);
               }

               this.categorizedHistory.put(playerId, playerHistory);
            } catch (IllegalArgumentException var52) {
            }
         }
      }

      this.pityCounters.clear();
      if (nbt.hasKey("pityCounters")) {
         NBTTagCompound pityTag = nbt.getCompoundTag("pityCounters");

         for(String key : pityTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(key);
               NBTTagCompound playerPity = pityTag.getCompoundTag(key);
               Map<String, Integer> counters = new HashMap();

               for(String crateId : playerPity.getKeySet()) {
                  counters.put(crateId, playerPity.getInteger(crateId));
               }

               this.pityCounters.put(playerId, counters);
            } catch (IllegalArgumentException var51) {
            }
         }
      }

      this.sPlusPityCounters.clear();
      if (nbt.hasKey("sPlusPityCounters")) {
         NBTTagCompound spTag = nbt.getCompoundTag("sPlusPityCounters");

         for(String key : spTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(key);
               NBTTagCompound playerPity = spTag.getCompoundTag(key);
               Map<String, Integer> counters = new HashMap();

               for(String crateId : playerPity.getKeySet()) {
                  counters.put(crateId, playerPity.getInteger(crateId));
               }

               this.sPlusPityCounters.put(playerId, counters);
            } catch (IllegalArgumentException var50) {
            }
         }
      }

      this.tokenBalance.clear();
      if (nbt.hasKey("tokenBalance")) {
         NBTTagCompound tokenTag = nbt.getCompoundTag("tokenBalance");

         for(String key : tokenTag.getKeySet()) {
            try {
               this.tokenBalance.put(UUID.fromString(key), tokenTag.getInteger(key));
            } catch (IllegalArgumentException var40) {
            }
         }
      }

      this.crystalBalance.clear();
      if (nbt.hasKey("crystalBalance")) {
         NBTTagCompound crystalTag = nbt.getCompoundTag("crystalBalance");

         for(String key : crystalTag.getKeySet()) {
            try {
               this.crystalBalance.put(UUID.fromString(key), crystalTag.getInteger(key));
            } catch (IllegalArgumentException var39) {
            }
         }
      }

      this.lastLoginTimestamp.clear();
      if (nbt.hasKey("lastLoginTimestamp")) {
         NBTTagCompound loginTag = nbt.getCompoundTag("lastLoginTimestamp");

         for(String key : loginTag.getKeySet()) {
            try {
               this.lastLoginTimestamp.put(UUID.fromString(key), loginTag.getLong(key));
            } catch (IllegalArgumentException var38) {
            }
         }
      }

      this.loginStreak.clear();
      if (nbt.hasKey("loginStreak")) {
         NBTTagCompound streakTag = nbt.getCompoundTag("loginStreak");

         for(String key : streakTag.getKeySet()) {
            try {
               this.loginStreak.put(UUID.fromString(key), streakTag.getInteger(key));
            } catch (IllegalArgumentException var37) {
            }
         }
      }

      this.firstPveWinTimestamp.clear();
      if (nbt.hasKey("firstPveWinTimestamp")) {
         NBTTagCompound pveTag = nbt.getCompoundTag("firstPveWinTimestamp");

         for(String key : pveTag.getKeySet()) {
            try {
               this.firstPveWinTimestamp.put(UUID.fromString(key), pveTag.getLong(key));
            } catch (IllegalArgumentException var36) {
            }
         }
      }

      this.firstPvpWinTimestamp.clear();
      if (nbt.hasKey("firstPvpWinTimestamp")) {
         NBTTagCompound pvpTag = nbt.getCompoundTag("firstPvpWinTimestamp");

         for(String key : pvpTag.getKeySet()) {
            try {
               this.firstPvpWinTimestamp.put(UUID.fromString(key), pvpTag.getLong(key));
            } catch (IllegalArgumentException var35) {
            }
         }
      }

      this.pendingCrateResults.clear();
      if (nbt.hasKey("pendingCrateResults")) {
         NBTTagCompound pendingTag = nbt.getCompoundTag("pendingCrateResults");

         for(String key : pendingTag.getKeySet()) {
            try {
               this.pendingCrateResults.put(UUID.fromString(key), pendingTag.getCompoundTag(key));
            } catch (IllegalArgumentException var34) {
            }
         }
      }

      this.completedFtbQuests.clear();
      if (nbt.hasKey("completedFtbQuests")) {
         NBTTagCompound ftbTag = nbt.getCompoundTag("completedFtbQuests");

         for(String key : ftbTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(key);
               NBTTagList questList = ftbTag.getTagList(key, 8);
               Set<String> quests = new HashSet();

               for(int i = 0; i < questList.tagCount(); ++i) {
                  quests.add(questList.getStringTagAt(i));
               }

               this.completedFtbQuests.put(playerId, quests);
            } catch (IllegalArgumentException var49) {
            }
         }
      }

      this.overflowItems.clear();
      if (nbt.hasKey("overflowItems")) {
         NBTTagCompound overflowTag = nbt.getCompoundTag("overflowItems");

         for(String key : overflowTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(key);
               NBTTagList itemList = overflowTag.getTagList(key, 10);
               List<NBTTagCompound> items = new ArrayList();

               for(int i = 0; i < itemList.tagCount(); ++i) {
                  items.add(itemList.getCompoundTagAt(i));
               }

               this.overflowItems.put(playerId, items);
            } catch (IllegalArgumentException var48) {
            }
         }
      }

      this.ownedItems.clear();
      if (nbt.hasKey("ownedItems")) {
         NBTTagCompound ownedTag = nbt.getCompoundTag("ownedItems");

         for(String key : ownedTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(key);
               NBTTagList itemList = ownedTag.getTagList(key, 8);
               Set<String> items = new HashSet();

               for(int i = 0; i < itemList.tagCount(); ++i) {
                  items.add(itemList.getStringTagAt(i));
               }

               this.ownedItems.put(playerId, items);
            } catch (IllegalArgumentException var47) {
            }
         }
      }

      this.lifetimeRyoSpent.clear();
      if (nbt.hasKey("lifetimeRyoSpent")) {
         NBTTagCompound spentTag = nbt.getCompoundTag("lifetimeRyoSpent");

         for(String key : spentTag.getKeySet()) {
            try {
               this.lifetimeRyoSpent.put(UUID.fromString(key), spentTag.getLong(key));
            } catch (IllegalArgumentException var33) {
            }
         }
      }

      this.lifetimeCratesOpened.clear();
      if (nbt.hasKey("lifetimeCratesOpened")) {
         NBTTagCompound openedTag = nbt.getCompoundTag("lifetimeCratesOpened");

         for(String key : openedTag.getKeySet()) {
            try {
               this.lifetimeCratesOpened.put(UUID.fromString(key), openedTag.getInteger(key));
            } catch (IllegalArgumentException var32) {
            }
         }
      }

      this.resetApplied.clear();
      if (nbt.hasKey("resetApplied")) {
         NBTTagList resetList = nbt.getTagList("resetApplied", 8);

         for(int i = 0; i < resetList.tagCount(); ++i) {
            try {
               this.resetApplied.add(UUID.fromString(resetList.getStringTagAt(i)));
            } catch (IllegalArgumentException var31) {
            }
         }
      }

      this.usedFreeRoll.clear();
      if (nbt.hasKey("usedFreeRoll")) {
         NBTTagList freeRollList = nbt.getTagList("usedFreeRoll", 8);

         for(int i = 0; i < freeRollList.tagCount(); ++i) {
            try {
               this.usedFreeRoll.add(UUID.fromString(freeRollList.getStringTagAt(i)));
            } catch (IllegalArgumentException var30) {
            }
         }
      }

      this.usedFirstPurchaseBonus.clear();
      if (nbt.hasKey("usedFirstPurchaseBonus")) {
         NBTTagList bonusList = nbt.getTagList("usedFirstPurchaseBonus", 8);

         for(int i = 0; i < bonusList.tagCount(); ++i) {
            try {
               this.usedFirstPurchaseBonus.add(UUID.fromString(bonusList.getStringTagAt(i)));
            } catch (IllegalArgumentException var29) {
            }
         }
      }

      this.totalCrystalsPurchased.clear();
      if (nbt.hasKey("totalCrystalsPurchased")) {
         NBTTagCompound tag = nbt.getCompoundTag("totalCrystalsPurchased");

         for(String key : tag.getKeySet()) {
            try {
               this.totalCrystalsPurchased.put(UUID.fromString(key), tag.getInteger(key));
            } catch (IllegalArgumentException var28) {
            }
         }
      }

      this.totalCrystalsSpent.clear();
      if (nbt.hasKey("totalCrystalsSpent")) {
         NBTTagCompound tag = nbt.getCompoundTag("totalCrystalsSpent");

         for(String key : tag.getKeySet()) {
            try {
               this.totalCrystalsSpent.put(UUID.fromString(key), tag.getInteger(key));
            } catch (IllegalArgumentException var27) {
            }
         }
      }

      this.crystalPurchaseCount.clear();
      if (nbt.hasKey("crystalPurchaseCount")) {
         NBTTagCompound tag = nbt.getCompoundTag("crystalPurchaseCount");

         for(String key : tag.getKeySet()) {
            try {
               this.crystalPurchaseCount.put(UUID.fromString(key), tag.getInteger(key));
            } catch (IllegalArgumentException var26) {
            }
         }
      }

      this.firstPurchaseDate.clear();
      if (nbt.hasKey("firstPurchaseDate")) {
         NBTTagCompound tag = nbt.getCompoundTag("firstPurchaseDate");

         for(String key : tag.getKeySet()) {
            try {
               this.firstPurchaseDate.put(UUID.fromString(key), tag.getLong(key));
            } catch (IllegalArgumentException var25) {
            }
         }
      }

      this.lastPurchaseDate.clear();
      if (nbt.hasKey("lastPurchaseDate")) {
         NBTTagCompound tag = nbt.getCompoundTag("lastPurchaseDate");

         for(String key : tag.getKeySet()) {
            try {
               this.lastPurchaseDate.put(UUID.fromString(key), tag.getLong(key));
            } catch (IllegalArgumentException var24) {
            }
         }
      }

      this.rollsPerCrate.clear();
      if (nbt.hasKey("rollsPerCrate")) {
         NBTTagCompound outerTag = nbt.getCompoundTag("rollsPerCrate");

         for(String key : outerTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(key);
               NBTTagCompound crateTag = outerTag.getCompoundTag(key);
               Map<String, Integer> rolls = new HashMap();

               for(String crateId : crateTag.getKeySet()) {
                  rolls.put(crateId, crateTag.getInteger(crateId));
               }

               this.rollsPerCrate.put(playerId, rolls);
            } catch (IllegalArgumentException var46) {
            }
         }
      }

      this.pityHitCount.clear();
      if (nbt.hasKey("pityHitCount")) {
         NBTTagCompound tag = nbt.getCompoundTag("pityHitCount");

         for(String key : tag.getKeySet()) {
            try {
               this.pityHitCount.put(UUID.fromString(key), tag.getInteger(key));
            } catch (IllegalArgumentException var23) {
            }
         }
      }

      this.dupeRefundTotal.clear();
      if (nbt.hasKey("dupeRefundTotal")) {
         NBTTagCompound tag = nbt.getCompoundTag("dupeRefundTotal");

         for(String key : tag.getKeySet()) {
            try {
               this.dupeRefundTotal.put(UUID.fromString(key), tag.getInteger(key));
            } catch (IllegalArgumentException var22) {
            }
         }
      }

      this.usedFreeRollThenBought.clear();
      if (nbt.hasKey("usedFreeRollThenBought")) {
         NBTTagCompound tag = nbt.getCompoundTag("usedFreeRollThenBought");

         for(String key : tag.getKeySet()) {
            try {
               this.usedFreeRollThenBought.put(UUID.fromString(key), tag.getBoolean(key));
            } catch (IllegalArgumentException var21) {
            }
         }
      }

      this.totalCrystalsFromTrades.clear();
      if (nbt.hasKey("CrystalsFromTrades")) {
         NBTTagCompound tag = nbt.getCompoundTag("CrystalsFromTrades");

         for(String key : tag.getKeySet()) {
            try {
               this.totalCrystalsFromTrades.put(UUID.fromString(key), tag.getInteger(key));
            } catch (IllegalArgumentException var20) {
            }
         }
      }

      this.bpSeasonNumber = nbt.getInteger("bpSeasonNumber");
      this.bpSeasonStartTime = nbt.getLong("bpSeasonStartTime");
      this.bpDailyCapResetTime = nbt.getLong("bpDailyCapResetTime");
      if (nbt.hasKey("weekendPromoSO6PEnabled")) {
         this.weekendPromoSO6PEnabled = nbt.getBoolean("weekendPromoSO6PEnabled");
      } else {
         this.weekendPromoSO6PEnabled = true;
      }

      this.bpPurchasedTier.clear();
      if (nbt.hasKey("bpPurchasedTier")) {
         NBTTagCompound tag = nbt.getCompoundTag("bpPurchasedTier");

         for(String key : tag.getKeySet()) {
            try {
               this.bpPurchasedTier.put(UUID.fromString(key), tag.getInteger(key));
            } catch (IllegalArgumentException var19) {
            }
         }
      }

      this.bpCurrentLevel.clear();
      if (nbt.hasKey("bpCurrentLevel")) {
         NBTTagCompound tag = nbt.getCompoundTag("bpCurrentLevel");

         for(String key : tag.getKeySet()) {
            try {
               this.bpCurrentLevel.put(UUID.fromString(key), tag.getInteger(key));
            } catch (IllegalArgumentException var18) {
            }
         }
      }

      this.bpCurrentXP.clear();
      if (nbt.hasKey("bpCurrentXP")) {
         NBTTagCompound tag = nbt.getCompoundTag("bpCurrentXP");

         for(String key : tag.getKeySet()) {
            try {
               this.bpCurrentXP.put(UUID.fromString(key), tag.getInteger(key));
            } catch (IllegalArgumentException var17) {
            }
         }
      }

      this.bpClaimedTiers.clear();
      if (nbt.hasKey("bpClaimedTiers")) {
         NBTTagCompound outerTag = nbt.getCompoundTag("bpClaimedTiers");

         for(String key : outerTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(key);
               int[] arr = outerTag.getIntArray(key);
               Set<Integer> tiers = new HashSet();

               for(int t : arr) {
                  tiers.add(t);
               }

               this.bpClaimedTiers.put(playerId, tiers);
            } catch (IllegalArgumentException var45) {
            }
         }
      }

      this.bpOtsutsukiSkipsUsed.clear();
      if (nbt.hasKey("bpOtsutsukiSkipsUsed")) {
         NBTTagCompound tag = nbt.getCompoundTag("bpOtsutsukiSkipsUsed");

         for(String key : tag.getKeySet()) {
            try {
               this.bpOtsutsukiSkipsUsed.put(UUID.fromString(key), tag.getInteger(key));
            } catch (IllegalArgumentException var16) {
            }
         }
      }

      this.activeKillEffect.clear();
      if (nbt.hasKey("activeKillEffect")) {
         NBTTagCompound tag = nbt.getCompoundTag("activeKillEffect");

         for(String key : tag.getKeySet()) {
            try {
               this.activeKillEffect.put(UUID.fromString(key), tag.getString(key));
            } catch (IllegalArgumentException var15) {
            }
         }
      }

      this.ownedKillEffects.clear();
      if (nbt.hasKey("ownedKillEffects")) {
         NBTTagCompound outerTag = nbt.getCompoundTag("ownedKillEffects");

         for(String key : outerTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(key);
               NBTTagList list = outerTag.getTagList(key, 8);
               Set<String> effects = new HashSet();

               for(int i = 0; i < list.tagCount(); ++i) {
                  effects.add(list.getStringTagAt(i));
               }

               this.ownedKillEffects.put(playerId, effects);
            } catch (IllegalArgumentException var44) {
            }
         }
      }

      this.bpDailyXPCaps.clear();
      if (nbt.hasKey("bpDailyXPCaps")) {
         NBTTagCompound outerTag = nbt.getCompoundTag("bpDailyXPCaps");

         for(String key : outerTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(key);
               NBTTagCompound innerTag = outerTag.getCompoundTag(key);
               Map<String, Integer> caps = new HashMap();

               for(String source : innerTag.getKeySet()) {
                  caps.put(source, innerTag.getInteger(source));
               }

               this.bpDailyXPCaps.put(playerId, caps);
            } catch (IllegalArgumentException var43) {
            }
         }
      }

      this.bpPendingAutoClaimTiers.clear();
      if (nbt.hasKey("bpPendingAutoClaimTiers")) {
         NBTTagCompound outerTag = nbt.getCompoundTag("bpPendingAutoClaimTiers");

         for(String key : outerTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(key);
               int[] arr = outerTag.getIntArray(key);
               Set<Integer> tiers = new HashSet();

               for(int t : arr) {
                  tiers.add(t);
               }

               this.bpPendingAutoClaimTiers.put(playerId, tiers);
            } catch (IllegalArgumentException var42) {
            }
         }
      }

      this.bpPendingAutoClaimSeason.clear();
      if (nbt.hasKey("bpPendingAutoClaimSeason")) {
         NBTTagCompound tag = nbt.getCompoundTag("bpPendingAutoClaimSeason");

         for(String key : tag.getKeySet()) {
            try {
               this.bpPendingAutoClaimSeason.put(UUID.fromString(key), tag.getInteger(key));
            } catch (IllegalArgumentException var14) {
            }
         }
      }

      this.bpPendingAutoClaimPass.clear();
      if (nbt.hasKey("bpPendingAutoClaimPass")) {
         NBTTagCompound tag = nbt.getCompoundTag("bpPendingAutoClaimPass");

         for(String key : tag.getKeySet()) {
            try {
               this.bpPendingAutoClaimPass.put(UUID.fromString(key), tag.getInteger(key));
            } catch (IllegalArgumentException var13) {
            }
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagCompound bankTag = new NBTTagCompound();

      for(Map.Entry<UUID, Long> entry : this.playerRyoBank.entrySet()) {
         bankTag.setLong(((UUID)entry.getKey()).toString(), (Long)entry.getValue());
      }

      nbt.setTag("ryoBank", bankTag);
      NBTTagCompound historyTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, List<ShopHistoryEntry>>> playerEntry : this.categorizedHistory.entrySet()) {
         NBTTagCompound playerTag = new NBTTagCompound();

         for(Map.Entry<String, List<ShopHistoryEntry>> catEntry : ((Map)playerEntry.getValue()).entrySet()) {
            NBTTagList entryList = new NBTTagList();

            for(ShopHistoryEntry he : (List)catEntry.getValue()) {
               entryList.appendTag(he.writeToNBT());
            }

            playerTag.setTag((String)catEntry.getKey(), entryList);
         }

         historyTag.setTag(((UUID)playerEntry.getKey()).toString(), playerTag);
      }

      nbt.setTag("categorizedHistory", historyTag);
      NBTTagCompound pityTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, Integer>> entry : this.pityCounters.entrySet()) {
         NBTTagCompound playerPity = new NBTTagCompound();

         for(Map.Entry<String, Integer> crateEntry : ((Map)entry.getValue()).entrySet()) {
            playerPity.setInteger((String)crateEntry.getKey(), (Integer)crateEntry.getValue());
         }

         pityTag.setTag(((UUID)entry.getKey()).toString(), playerPity);
      }

      nbt.setTag("pityCounters", pityTag);
      NBTTagCompound spTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, Integer>> entry : this.sPlusPityCounters.entrySet()) {
         NBTTagCompound playerPity = new NBTTagCompound();

         for(Map.Entry<String, Integer> crateEntry : ((Map)entry.getValue()).entrySet()) {
            playerPity.setInteger((String)crateEntry.getKey(), (Integer)crateEntry.getValue());
         }

         spTag.setTag(((UUID)entry.getKey()).toString(), playerPity);
      }

      nbt.setTag("sPlusPityCounters", spTag);
      NBTTagCompound tokenTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.tokenBalance.entrySet()) {
         tokenTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("tokenBalance", tokenTag);
      NBTTagCompound crystalTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.crystalBalance.entrySet()) {
         crystalTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("crystalBalance", crystalTag);
      NBTTagCompound loginTag = new NBTTagCompound();

      for(Map.Entry<UUID, Long> entry : this.lastLoginTimestamp.entrySet()) {
         loginTag.setLong(((UUID)entry.getKey()).toString(), (Long)entry.getValue());
      }

      nbt.setTag("lastLoginTimestamp", loginTag);
      NBTTagCompound streakTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.loginStreak.entrySet()) {
         streakTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("loginStreak", streakTag);
      NBTTagCompound pveTag = new NBTTagCompound();

      for(Map.Entry<UUID, Long> entry : this.firstPveWinTimestamp.entrySet()) {
         pveTag.setLong(((UUID)entry.getKey()).toString(), (Long)entry.getValue());
      }

      nbt.setTag("firstPveWinTimestamp", pveTag);
      NBTTagCompound pvpTag = new NBTTagCompound();

      for(Map.Entry<UUID, Long> entry : this.firstPvpWinTimestamp.entrySet()) {
         pvpTag.setLong(((UUID)entry.getKey()).toString(), (Long)entry.getValue());
      }

      nbt.setTag("firstPvpWinTimestamp", pvpTag);
      NBTTagCompound pendingTag = new NBTTagCompound();

      for(Map.Entry<UUID, NBTTagCompound> entry : this.pendingCrateResults.entrySet()) {
         pendingTag.setTag(((UUID)entry.getKey()).toString(), (NBTBase)entry.getValue());
      }

      nbt.setTag("pendingCrateResults", pendingTag);
      NBTTagCompound ftbTag = new NBTTagCompound();

      for(Map.Entry<UUID, Set<String>> entry : this.completedFtbQuests.entrySet()) {
         NBTTagList questList = new NBTTagList();

         for(String questId : (Set)entry.getValue()) {
            questList.appendTag(new NBTTagString(questId));
         }

         ftbTag.setTag(((UUID)entry.getKey()).toString(), questList);
      }

      nbt.setTag("completedFtbQuests", ftbTag);
      NBTTagCompound overflowTag = new NBTTagCompound();

      for(Map.Entry<UUID, List<NBTTagCompound>> entry : this.overflowItems.entrySet()) {
         NBTTagList itemList = new NBTTagList();

         for(NBTTagCompound itemNbt : (List)entry.getValue()) {
            itemList.appendTag(itemNbt.copy());
         }

         overflowTag.setTag(((UUID)entry.getKey()).toString(), itemList);
      }

      nbt.setTag("overflowItems", overflowTag);
      NBTTagCompound ownedTag = new NBTTagCompound();

      for(Map.Entry<UUID, Set<String>> entry : this.ownedItems.entrySet()) {
         NBTTagList itemList = new NBTTagList();

         for(String itemKey : (Set)entry.getValue()) {
            itemList.appendTag(new NBTTagString(itemKey));
         }

         ownedTag.setTag(((UUID)entry.getKey()).toString(), itemList);
      }

      nbt.setTag("ownedItems", ownedTag);
      NBTTagCompound spentTag = new NBTTagCompound();

      for(Map.Entry<UUID, Long> entry : this.lifetimeRyoSpent.entrySet()) {
         spentTag.setLong(((UUID)entry.getKey()).toString(), (Long)entry.getValue());
      }

      nbt.setTag("lifetimeRyoSpent", spentTag);
      NBTTagCompound openedTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.lifetimeCratesOpened.entrySet()) {
         openedTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("lifetimeCratesOpened", openedTag);
      NBTTagList resetList = new NBTTagList();

      for(UUID uuid : this.resetApplied) {
         resetList.appendTag(new NBTTagString(uuid.toString()));
      }

      nbt.setTag("resetApplied", resetList);
      NBTTagList freeRollList = new NBTTagList();

      for(UUID uuid : this.usedFreeRoll) {
         freeRollList.appendTag(new NBTTagString(uuid.toString()));
      }

      nbt.setTag("usedFreeRoll", freeRollList);
      NBTTagList bonusList = new NBTTagList();

      for(UUID uuid : this.usedFirstPurchaseBonus) {
         bonusList.appendTag(new NBTTagString(uuid.toString()));
      }

      nbt.setTag("usedFirstPurchaseBonus", bonusList);
      NBTTagCompound tcpTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.totalCrystalsPurchased.entrySet()) {
         tcpTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("totalCrystalsPurchased", tcpTag);
      NBTTagCompound tcsTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.totalCrystalsSpent.entrySet()) {
         tcsTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("totalCrystalsSpent", tcsTag);
      NBTTagCompound cpcTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.crystalPurchaseCount.entrySet()) {
         cpcTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("crystalPurchaseCount", cpcTag);
      NBTTagCompound fpdTag = new NBTTagCompound();

      for(Map.Entry<UUID, Long> entry : this.firstPurchaseDate.entrySet()) {
         fpdTag.setLong(((UUID)entry.getKey()).toString(), (Long)entry.getValue());
      }

      nbt.setTag("firstPurchaseDate", fpdTag);
      NBTTagCompound lpdTag = new NBTTagCompound();

      for(Map.Entry<UUID, Long> entry : this.lastPurchaseDate.entrySet()) {
         lpdTag.setLong(((UUID)entry.getKey()).toString(), (Long)entry.getValue());
      }

      nbt.setTag("lastPurchaseDate", lpdTag);
      NBTTagCompound rpcTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, Integer>> entry : this.rollsPerCrate.entrySet()) {
         NBTTagCompound crateTag = new NBTTagCompound();

         for(Map.Entry<String, Integer> crateEntry : ((Map)entry.getValue()).entrySet()) {
            crateTag.setInteger((String)crateEntry.getKey(), (Integer)crateEntry.getValue());
         }

         rpcTag.setTag(((UUID)entry.getKey()).toString(), crateTag);
      }

      nbt.setTag("rollsPerCrate", rpcTag);
      NBTTagCompound phcTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.pityHitCount.entrySet()) {
         phcTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("pityHitCount", phcTag);
      NBTTagCompound drtTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.dupeRefundTotal.entrySet()) {
         drtTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("dupeRefundTotal", drtTag);
      NBTTagCompound ufrtbTag = new NBTTagCompound();

      for(Map.Entry<UUID, Boolean> entry : this.usedFreeRollThenBought.entrySet()) {
         ufrtbTag.setBoolean(((UUID)entry.getKey()).toString(), (Boolean)entry.getValue());
      }

      nbt.setTag("usedFreeRollThenBought", ufrtbTag);
      NBTTagCompound cftTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.totalCrystalsFromTrades.entrySet()) {
         cftTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("CrystalsFromTrades", cftTag);
      nbt.setInteger("bpSeasonNumber", this.bpSeasonNumber);
      nbt.setLong("bpSeasonStartTime", this.bpSeasonStartTime);
      nbt.setLong("bpDailyCapResetTime", this.bpDailyCapResetTime);
      nbt.setBoolean("weekendPromoSO6PEnabled", this.weekendPromoSO6PEnabled);
      NBTTagCompound bpPurchasedTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.bpPurchasedTier.entrySet()) {
         bpPurchasedTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("bpPurchasedTier", bpPurchasedTag);
      NBTTagCompound bpLevelTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.bpCurrentLevel.entrySet()) {
         bpLevelTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("bpCurrentLevel", bpLevelTag);
      NBTTagCompound bpXPTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.bpCurrentXP.entrySet()) {
         bpXPTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("bpCurrentXP", bpXPTag);
      NBTTagCompound bpClaimedTag = new NBTTagCompound();

      for(Map.Entry<UUID, Set<Integer>> entry : this.bpClaimedTiers.entrySet()) {
         Set<Integer> tiers = (Set)entry.getValue();
         int[] arr = new int[tiers.size()];
         int idx = 0;

         for(int t : tiers) {
            arr[idx++] = t;
         }

         bpClaimedTag.setIntArray(((UUID)entry.getKey()).toString(), arr);
      }

      nbt.setTag("bpClaimedTiers", bpClaimedTag);
      NBTTagCompound bpSkipsTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.bpOtsutsukiSkipsUsed.entrySet()) {
         bpSkipsTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("bpOtsutsukiSkipsUsed", bpSkipsTag);
      NBTTagCompound activeEffectTag = new NBTTagCompound();

      for(Map.Entry<UUID, String> entry : this.activeKillEffect.entrySet()) {
         activeEffectTag.setString(((UUID)entry.getKey()).toString(), (String)entry.getValue());
      }

      nbt.setTag("activeKillEffect", activeEffectTag);
      NBTTagCompound ownedEffectsTag = new NBTTagCompound();

      for(Map.Entry<UUID, Set<String>> entry : this.ownedKillEffects.entrySet()) {
         NBTTagList effectList = new NBTTagList();

         for(String effectId : (Set)entry.getValue()) {
            effectList.appendTag(new NBTTagString(effectId));
         }

         ownedEffectsTag.setTag(((UUID)entry.getKey()).toString(), effectList);
      }

      nbt.setTag("ownedKillEffects", ownedEffectsTag);
      NBTTagCompound bpDailyCapsTag = new NBTTagCompound();

      for(Map.Entry<UUID, Map<String, Integer>> entry : this.bpDailyXPCaps.entrySet()) {
         NBTTagCompound innerTag = new NBTTagCompound();

         for(Map.Entry<String, Integer> cap : ((Map)entry.getValue()).entrySet()) {
            innerTag.setInteger((String)cap.getKey(), (Integer)cap.getValue());
         }

         bpDailyCapsTag.setTag(((UUID)entry.getKey()).toString(), innerTag);
      }

      nbt.setTag("bpDailyXPCaps", bpDailyCapsTag);
      NBTTagCompound bpAutoClaimTiersTag = new NBTTagCompound();

      for(Map.Entry<UUID, Set<Integer>> entry : this.bpPendingAutoClaimTiers.entrySet()) {
         Set<Integer> tiers = (Set)entry.getValue();
         int[] arr = new int[tiers.size()];
         int idx = 0;

         for(int t : tiers) {
            arr[idx++] = t;
         }

         bpAutoClaimTiersTag.setIntArray(((UUID)entry.getKey()).toString(), arr);
      }

      nbt.setTag("bpPendingAutoClaimTiers", bpAutoClaimTiersTag);
      NBTTagCompound bpAutoClaimSeasonTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.bpPendingAutoClaimSeason.entrySet()) {
         bpAutoClaimSeasonTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("bpPendingAutoClaimSeason", bpAutoClaimSeasonTag);
      NBTTagCompound bpAutoClaimPassTag = new NBTTagCompound();

      for(Map.Entry<UUID, Integer> entry : this.bpPendingAutoClaimPass.entrySet()) {
         bpAutoClaimPassTag.setInteger(((UUID)entry.getKey()).toString(), (Integer)entry.getValue());
      }

      nbt.setTag("bpPendingAutoClaimPass", bpAutoClaimPassTag);
      return nbt;
   }
}
