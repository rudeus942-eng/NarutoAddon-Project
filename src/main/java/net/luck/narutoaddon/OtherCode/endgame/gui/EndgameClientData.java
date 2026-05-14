package net.luck.narutoaddon.OtherCode.endgame.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class EndgameClientData {
   private static final EndgameClientData INSTANCE = new EndgameClientData();
   private Map<String, Map<Integer, Long>> outpostCooldowns = new HashMap();
   private List<BingoSlotClient> bingoSlots = new ArrayList();
   private boolean bingoComplete = false;
   private long bingoResetTime = 0L;
   private String boardTierName = "";
   private int activeHuntSlot = -1;
   private double huntWaypointX;
   private double huntWaypointZ;
   private double huntSearchRadius;
   private boolean incursionActive = false;
   private boolean hasJoinedIncursion = false;
   private String incursionName = "";
   private String incursionStateName = "";
   private int incursionCountdown = -1;
   private double incursionX;
   private double incursionZ;
   private int incursionWave;
   private int incursionMaxWaves;
   private int incursionEnemiesRemaining;
   private boolean defenseActive = false;
   private String defenseVillage = "";
   private int defenseWave;
   private int defenseMaxWaves;
   private int defenseVillageHP;
   private int defenseMaxHP;
   private List<CombatBarData> combatBars = new ArrayList();
   private Map<String, List<LeaderboardEntry>> leaderboards = new HashMap();
   private long raidCooldownEnd = 0L;
   private int pveXp = 0;
   private String pveRankName = "Genin";
   private int pveXpToNext = 500;

   public static EndgameClientData getInstance() {
      return INSTANCE;
   }

   private EndgameClientData() {
   }

   public void updateOutpostCooldowns(Map<String, Map<Integer, Long>> cooldowns) {
      this.outpostCooldowns = (Map<String, Map<Integer, Long>>)(cooldowns != null ? cooldowns : new HashMap());
   }

   public void updateBingo(List<BingoSlotClient> slots, boolean complete, long resetTime, int activeSlot, double wpX, double wpZ, double searchRadius, String boardTierName) {
      this.bingoSlots = (List<BingoSlotClient>)(slots != null ? slots : new ArrayList());
      this.bingoComplete = complete;
      this.bingoResetTime = resetTime;
      this.activeHuntSlot = activeSlot;
      this.huntWaypointX = wpX;
      this.huntWaypointZ = wpZ;
      this.huntSearchRadius = searchRadius;
      this.boardTierName = boardTierName != null ? boardTierName : "";
   }

   public void updateIncursion(boolean active, String name, double x, double z, int wave, int maxWaves, int enemiesRemaining, boolean hasJoined, String stateName, int countdown) {
      this.incursionActive = active;
      this.hasJoinedIncursion = hasJoined;
      this.incursionName = name != null ? name : "";
      this.incursionStateName = stateName != null ? stateName : "";
      this.incursionCountdown = countdown;
      this.incursionX = x;
      this.incursionZ = z;
      this.incursionWave = wave;
      this.incursionMaxWaves = maxWaves;
      this.incursionEnemiesRemaining = enemiesRemaining;
   }

   public void updateDefense(boolean active, String village, int wave, int maxWaves, int villageHP, int maxHP) {
      this.defenseActive = active;
      this.defenseVillage = village != null ? village : "";
      this.defenseWave = wave;
      this.defenseMaxWaves = maxWaves;
      this.defenseVillageHP = villageHP;
      this.defenseMaxHP = maxHP;
   }

   public void updateCombatBars(List<CombatBarData> bars) {
      this.combatBars = (List<CombatBarData>)(bars != null ? bars : new ArrayList());
   }

   public void updateLeaderboard(String category, List<LeaderboardEntry> entries) {
      if (category != null) {
         this.leaderboards.put(category, entries != null ? entries : new ArrayList());
      }

   }

   public void updateRaidCooldown(long cooldownEnd) {
      this.raidCooldownEnd = cooldownEnd;
   }

   public void updatePveRank(int xp, String rankName, int xpToNext) {
      this.pveXp = xp;
      this.pveRankName = rankName != null ? rankName : "Genin";
      this.pveXpToNext = xpToNext;
   }

   public Map<String, Map<Integer, Long>> getOutpostCooldowns() {
      return this.outpostCooldowns;
   }

   public long getOutpostCooldown(String outpostId, int tier) {
      Map<Integer, Long> tierMap = (Map)this.outpostCooldowns.get(outpostId);
      if (tierMap == null) {
         return 0L;
      } else {
         Long end = (Long)tierMap.get(tier);
         return end != null ? end : 0L;
      }
   }

   public boolean isOutpostOnCooldown(String outpostId, int tier) {
      return this.getOutpostCooldown(outpostId, tier) > System.currentTimeMillis();
   }

   public long getOutpostCooldownRemaining(String outpostId, int tier) {
      long end = this.getOutpostCooldown(outpostId, tier);
      return Math.max(0L, end - System.currentTimeMillis());
   }

   public List<BingoSlotClient> getBingoSlots() {
      return this.bingoSlots;
   }

   public boolean isBingoComplete() {
      return this.bingoComplete;
   }

   public long getBingoResetTime() {
      return this.bingoResetTime;
   }

   public String getBoardTierName() {
      return this.boardTierName;
   }

   public int getActiveHuntSlot() {
      return this.activeHuntSlot;
   }

   public boolean hasActiveHunt() {
      return this.activeHuntSlot >= 0;
   }

   public double getHuntWaypointX() {
      return this.huntWaypointX;
   }

   public double getHuntWaypointZ() {
      return this.huntWaypointZ;
   }

   public double getHuntSearchRadius() {
      return this.huntSearchRadius;
   }

   public boolean isIncursionActive() {
      return this.incursionActive;
   }

   public boolean hasJoinedIncursion() {
      return this.hasJoinedIncursion;
   }

   public String getIncursionName() {
      return this.incursionName;
   }

   public String getIncursionStateName() {
      return this.incursionStateName;
   }

   public int getIncursionCountdown() {
      return this.incursionCountdown;
   }

   public double getIncursionX() {
      return this.incursionX;
   }

   public double getIncursionZ() {
      return this.incursionZ;
   }

   public int getIncursionWave() {
      return this.incursionWave;
   }

   public int getIncursionMaxWaves() {
      return this.incursionMaxWaves;
   }

   public int getIncursionEnemiesRemaining() {
      return this.incursionEnemiesRemaining;
   }

   public boolean isDefenseActive() {
      return this.defenseActive;
   }

   public String getDefenseVillage() {
      return this.defenseVillage;
   }

   public int getDefenseWave() {
      return this.defenseWave;
   }

   public int getDefenseMaxWaves() {
      return this.defenseMaxWaves;
   }

   public int getDefenseVillageHP() {
      return this.defenseVillageHP;
   }

   public int getDefenseMaxHP() {
      return this.defenseMaxHP;
   }

   public List<CombatBarData> getCombatBars() {
      return this.combatBars;
   }

   public void clearCombatBars() {
      this.combatBars.clear();
   }

   public void updateCombatBar(int entityId, String entityName, float currentHP, float maxHP, int phase, int themeColor, int accentColor) {
      for(CombatBarData bar : this.combatBars) {
         if (bar.entityId == entityId) {
            bar.entityName = entityName;
            bar.currentHP = currentHP;
            bar.maxHP = maxHP;
            bar.phase = phase;
            bar.themeColor = themeColor;
            bar.accentColor = accentColor;
            bar.lastUpdateTick = System.currentTimeMillis();
            return;
         }
      }

      if (this.combatBars.size() < 4) {
         CombatBarData bar = new CombatBarData();
         bar.entityId = entityId;
         bar.entityName = entityName;
         bar.currentHP = currentHP;
         bar.maxHP = maxHP;
         bar.phase = phase;
         bar.themeColor = themeColor;
         bar.accentColor = accentColor;
         bar.lastUpdateTick = System.currentTimeMillis();
         this.combatBars.add(bar);
      }

   }

   public Map<String, List<LeaderboardEntry>> getLeaderboards() {
      return this.leaderboards;
   }

   public List<LeaderboardEntry> getLeaderboard(String category) {
      return (List)this.leaderboards.getOrDefault(category, new ArrayList());
   }

   public long getRaidCooldownEnd() {
      return this.raidCooldownEnd;
   }

   public boolean isRaidOnCooldown() {
      return this.raidCooldownEnd > System.currentTimeMillis();
   }

   public long getRaidCooldownRemaining() {
      return Math.max(0L, this.raidCooldownEnd - System.currentTimeMillis());
   }

   public int getPveXp() {
      return this.pveXp;
   }

   public String getPveRankName() {
      return this.pveRankName;
   }

   public int getPveXpToNext() {
      return this.pveXpToNext;
   }

   public void clear() {
      this.outpostCooldowns.clear();
      this.bingoSlots.clear();
      this.bingoComplete = false;
      this.bingoResetTime = 0L;
      this.boardTierName = "";
      this.activeHuntSlot = -1;
      this.huntWaypointX = (double)0.0F;
      this.huntWaypointZ = (double)0.0F;
      this.huntSearchRadius = (double)0.0F;
      this.incursionActive = false;
      this.hasJoinedIncursion = false;
      this.incursionName = "";
      this.incursionStateName = "";
      this.incursionCountdown = -1;
      this.incursionX = (double)0.0F;
      this.incursionZ = (double)0.0F;
      this.incursionWave = 0;
      this.incursionMaxWaves = 0;
      this.incursionEnemiesRemaining = 0;
      this.defenseActive = false;
      this.defenseVillage = "";
      this.defenseWave = 0;
      this.defenseMaxWaves = 0;
      this.defenseVillageHP = 0;
      this.defenseMaxHP = 0;
      this.combatBars.clear();
      this.leaderboards.clear();
      this.raidCooldownEnd = 0L;
      this.pveXp = 0;
      this.pveRankName = "Genin";
      this.pveXpToNext = 500;
   }

   public static class BingoSlotClient {
      public String targetName = "";
      public String loreHint = "";
      public String regionName = "";
      public int ryoReward;
      public boolean completed;
      public boolean active;
   }

   public static class CombatBarData {
      public int entityId;
      public String entityName = "";
      public float currentHP;
      public float maxHP;
      public int phase;
      public long lastUpdateTick;
      public int themeColor;
      public int accentColor;
   }

   public static class LeaderboardEntry {
      public String playerName;
      public int value;

      public LeaderboardEntry() {
         this.playerName = "";
      }

      public LeaderboardEntry(String playerName, int value) {
         this.playerName = playerName;
         this.value = value;
      }
   }
}
