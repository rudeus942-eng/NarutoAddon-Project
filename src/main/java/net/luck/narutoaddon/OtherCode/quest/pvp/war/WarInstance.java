
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.*;

public class WarInstance {
   private final String warId;
   private WarMode mode;
   private VillageHelper.Village village1;
   private VillageHelper.Village village2;
   private UUID declaredByKage;
   private long startTimeMs;
   private long endTimeMs;
   private int score1;
   private int score2;
   private final Map<UUID, Integer> playerKills1;
   private final Map<UUID, Integer> playerKills2;
   private boolean surrendered;
   private VillageHelper.Village surrenderedBy;
   private WarLobby lobby;
   private List<WarZone> zones;
   private List<InfiltrationObjective> infiltrationObjectives;
   private int roundsWon1;
   private int roundsWon2;
   private int currentRound;

   public WarInstance(String warId, WarMode mode, VillageHelper.Village village1, VillageHelper.Village village2, UUID declaredByKage) {
      this.warId = warId;
      this.mode = mode;
      this.village1 = village1;
      this.village2 = village2;
      this.declaredByKage = declaredByKage;
      this.startTimeMs = System.currentTimeMillis();
      this.endTimeMs = this.startTimeMs + mode.durationMs;
      this.score1 = 0;
      this.score2 = 0;
      this.playerKills1 = new HashMap();
      this.playerKills2 = new HashMap();
      this.surrendered = false;
      this.surrenderedBy = null;
      this.lobby = null;
      this.zones = null;
      this.infiltrationObjectives = null;
      this.roundsWon1 = 0;
      this.roundsWon2 = 0;
      this.currentRound = 1;
   }

   private WarInstance(String warId, WarMode mode, VillageHelper.Village village1, VillageHelper.Village village2, UUID declaredByKage, long startTimeMs, long endTimeMs, int score1, int score2, Map<UUID, Integer> playerKills1, Map<UUID, Integer> playerKills2, boolean surrendered, VillageHelper.Village surrenderedBy, WarLobby lobby, List<WarZone> zones, int roundsWon1, int roundsWon2, int currentRound) {
      this.warId = warId;
      this.mode = mode;
      this.village1 = village1;
      this.village2 = village2;
      this.declaredByKage = declaredByKage;
      this.startTimeMs = startTimeMs;
      this.endTimeMs = endTimeMs;
      this.score1 = score1;
      this.score2 = score2;
      this.playerKills1 = playerKills1;
      this.playerKills2 = playerKills2;
      this.surrendered = surrendered;
      this.surrenderedBy = surrenderedBy;
      this.lobby = lobby;
      this.zones = zones;
      this.roundsWon1 = roundsWon1;
      this.roundsWon2 = roundsWon2;
      this.currentRound = currentRound;
   }

   public String getWarId() {
      return this.warId;
   }

   public WarMode getMode() {
      return this.mode;
   }

   public VillageHelper.Village getVillage1() {
      return this.village1;
   }

   public VillageHelper.Village getVillage2() {
      return this.village2;
   }

   public UUID getDeclaredByKage() {
      return this.declaredByKage;
   }

   public long getStartTimeMs() {
      return this.startTimeMs;
   }

   public long getEndTimeMs() {
      return this.endTimeMs;
   }

   public int getScore1() {
      return this.score1;
   }

   public int getScore2() {
      return this.score2;
   }

   public void addScore1(int points) {
      this.score1 += points;
   }

   public void addScore2(int points) {
      this.score2 += points;
   }

   public Map<UUID, Integer> getPlayerKills1() {
      return this.playerKills1;
   }

   public Map<UUID, Integer> getPlayerKills2() {
      return this.playerKills2;
   }

   public boolean isSurrendered() {
      return this.surrendered;
   }

   public VillageHelper.Village getSurrenderedBy() {
      return this.surrenderedBy;
   }

   public WarLobby getLobby() {
      return this.lobby;
   }

   public void setLobby(WarLobby lobby) {
      this.lobby = lobby;
   }

   public List<WarZone> getZones() {
      return this.zones;
   }

   public void setZones(List<WarZone> zones) {
      this.zones = zones;
   }

   public List<InfiltrationObjective> getInfiltrationObjectives() {
      return this.infiltrationObjectives;
   }

   public void setInfiltrationObjectives(List<InfiltrationObjective> objectives) {
      this.infiltrationObjectives = objectives;
   }

   public int getCompletedObjectiveCount() {
      if (this.infiltrationObjectives == null) {
         return 0;
      } else {
         int count = 0;

         for(InfiltrationObjective obj : this.infiltrationObjectives) {
            if (obj.isComplete()) {
               ++count;
            }
         }

         return count;
      }
   }

   public boolean checkAssassinationKill(UUID killedEntityUUID) {
      if (this.infiltrationObjectives == null) {
         return false;
      } else {
         for(InfiltrationObjective obj : this.infiltrationObjectives) {
            if (obj.getType() == InfiltrationObjective.ObjectiveType.ASSASSINATE && obj.isPending() && killedEntityUUID.equals(obj.getAssassinationTarget())) {
               obj.markAssassinationComplete();
               this.addScore1(obj.getType().points);
               return true;
            }
         }

         return false;
      }
   }

   public int getRoundsWon1() {
      return this.roundsWon1;
   }

   public int getRoundsWon2() {
      return this.roundsWon2;
   }

   public int getCurrentRound() {
      return this.currentRound;
   }

   public void addRoundWin1() {
      ++this.roundsWon1;
   }

   public void addRoundWin2() {
      ++this.roundsWon2;
   }

   public void advanceRound() {
      ++this.currentRound;
      this.score1 = 0;
      this.score2 = 0;
      this.startTimeMs = System.currentTimeMillis();
      this.endTimeMs = this.startTimeMs + this.mode.durationMs;
   }

   public void resetRoundTimer() {
      this.startTimeMs = System.currentTimeMillis();
      this.endTimeMs = this.startTimeMs + this.mode.durationMs;
   }

   public void recordKill(VillageHelper.Village killerVillage, UUID killerUUID) {
      if (killerVillage == this.village1) {
         this.playerKills1.merge(killerUUID, 1, Integer::sum);
      } else if (killerVillage == this.village2) {
         this.playerKills2.merge(killerUUID, 1, Integer::sum);
      }

   }

   public void surrender(VillageHelper.Village village) {
      this.surrendered = true;
      this.surrenderedBy = village;
   }

   public long getRemainingMs() {
      long remaining = this.endTimeMs - System.currentTimeMillis();
      return Math.max(0L, remaining);
   }

   public String getFormattedTimeRemaining() {
      long remaining = this.getRemainingMs();
      long totalSeconds = remaining / 1000L;
      long hours = totalSeconds / 3600L;
      long minutes = totalSeconds % 3600L / 60L;
      long seconds = totalSeconds % 60L;
      return hours > 0L ? String.format("%dh %02dm %02ds", hours, minutes, seconds) : String.format("%02dm %02ds", minutes, seconds);
   }

   public boolean isTimeUp() {
      return System.currentTimeMillis() >= this.endTimeMs;
   }

   public VillageHelper.Village getWinningVillage() {
      if (this.surrendered && this.surrenderedBy != null) {
         return this.surrenderedBy == this.village1 ? this.village2 : this.village1;
      } else if (this.mode == WarMode.DIVISION) {
         if (this.roundsWon1 > this.roundsWon2) {
            return this.village1;
         } else {
            return this.roundsWon2 > this.roundsWon1 ? this.village2 : null;
         }
      } else if (this.score1 > this.score2) {
         return this.village1;
      } else {
         return this.score2 > this.score1 ? this.village2 : null;
      }
   }

   public List<Map.Entry<UUID, Integer>> getTopKillers(int villageSide, int count) {
      Map<UUID, Integer> kills = villageSide == 1 ? this.playerKills1 : this.playerKills2;
      List<Map.Entry<UUID, Integer>> sorted = new ArrayList(kills.entrySet());
      sorted.sort(Comparator.comparingInt(Map.Entry::getValue).reversed());
      return sorted.subList(0, Math.min(count, sorted.size()));
   }

   public int getPlayerSide(VillageHelper.Village playerVillage) {
      if (playerVillage == this.village1) {
         return 1;
      } else {
         return playerVillage == this.village2 ? 2 : 0;
      }
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("warId", this.warId);
      nbt.setInteger("mode", this.mode.ordinal());
      nbt.setInteger("village1", this.village1.ordinal());
      nbt.setInteger("village2", this.village2.ordinal());
      nbt.setString("declaredByKage", this.declaredByKage.toString());
      nbt.setLong("startTimeMs", this.startTimeMs);
      nbt.setLong("endTimeMs", this.endTimeMs);
      nbt.setInteger("score1", this.score1);
      nbt.setInteger("score2", this.score2);
      nbt.setBoolean("surrendered", this.surrendered);
      if (this.surrenderedBy != null) {
         nbt.setInteger("surrenderedBy", this.surrenderedBy.ordinal());
      }

      nbt.setInteger("roundsWon1", this.roundsWon1);
      nbt.setInteger("roundsWon2", this.roundsWon2);
      nbt.setInteger("currentRound", this.currentRound);
      nbt.setTag("playerKills1", writeKillsMap(this.playerKills1));
      nbt.setTag("playerKills2", writeKillsMap(this.playerKills2));
      if (this.lobby != null) {
         nbt.setTag("lobby", this.lobby.writeToNBT());
      }

      if (this.zones != null && !this.zones.isEmpty()) {
         NBTTagList zoneList = new NBTTagList();

         for(WarZone zone : this.zones) {
            zoneList.appendTag(zone.writeToNBT());
         }

         nbt.setTag("zones", zoneList);
      }

      return nbt;
   }

   public static WarInstance readFromNBT(NBTTagCompound nbt) {
      String warId = nbt.getString("warId");
      WarMode mode = WarMode.fromOrdinal(nbt.getInteger("mode"));
      VillageHelper.Village[] villages = VillageHelper.Village.values();
      int v1Ord = nbt.getInteger("village1");
      int v2Ord = nbt.getInteger("village2");
      VillageHelper.Village village1 = v1Ord >= 0 && v1Ord < villages.length ? villages[v1Ord] : VillageHelper.Village.UNKNOWN;
      VillageHelper.Village village2 = v2Ord >= 0 && v2Ord < villages.length ? villages[v2Ord] : VillageHelper.Village.UNKNOWN;

      UUID declaredByKage;
      try {
         declaredByKage = UUID.fromString(nbt.getString("declaredByKage"));
      } catch (IllegalArgumentException var26) {
         declaredByKage = new UUID(0L, 0L);
      }

      long startTimeMs = nbt.getLong("startTimeMs");
      long endTimeMs = nbt.getLong("endTimeMs");
      int score1 = nbt.getInteger("score1");
      int score2 = nbt.getInteger("score2");
      boolean surrendered = nbt.getBoolean("surrendered");
      VillageHelper.Village surrenderedBy = null;
      if (nbt.hasKey("surrenderedBy")) {
         int sOrd = nbt.getInteger("surrenderedBy");
         surrenderedBy = sOrd >= 0 && sOrd < villages.length ? villages[sOrd] : null;
      }

      int roundsWon1 = nbt.getInteger("roundsWon1");
      int roundsWon2 = nbt.getInteger("roundsWon2");
      int currentRound = nbt.getInteger("currentRound");
      Map<UUID, Integer> playerKills1 = readKillsMap(nbt.getTagList("playerKills1", 10));
      Map<UUID, Integer> playerKills2 = readKillsMap(nbt.getTagList("playerKills2", 10));
      WarLobby lobby = null;
      if (nbt.hasKey("lobby")) {
         lobby = WarLobby.readFromNBT(nbt.getCompoundTag("lobby"));
      }

      List<WarZone> zones = null;
      if (nbt.hasKey("zones")) {
         NBTTagList zoneList = nbt.getTagList("zones", 10);
         zones = new ArrayList();

         for(int i = 0; i < zoneList.tagCount(); ++i) {
            zones.add(WarZone.readFromNBT(zoneList.getCompoundTagAt(i)));
         }
      }

      return new WarInstance(warId, mode, village1, village2, declaredByKage, startTimeMs, endTimeMs, score1, score2, playerKills1, playerKills2, surrendered, surrenderedBy, lobby, zones, roundsWon1, roundsWon2, currentRound);
   }

   private static NBTTagList writeKillsMap(Map<UUID, Integer> kills) {
      NBTTagList tagList = new NBTTagList();

      for(Map.Entry<UUID, Integer> entry : kills.entrySet()) {
         NBTTagCompound entryNbt = new NBTTagCompound();
         entryNbt.setString("uuid", ((UUID)entry.getKey()).toString());
         entryNbt.setInteger("kills", (Integer)entry.getValue());
         tagList.appendTag(entryNbt);
      }

      return tagList;
   }

   private static Map<UUID, Integer> readKillsMap(NBTTagList tagList) {
      Map<UUID, Integer> map = new HashMap();

      for(int i = 0; i < tagList.tagCount(); ++i) {
         NBTTagCompound entryNbt = tagList.getCompoundTagAt(i);

         try {
            UUID uuid = UUID.fromString(entryNbt.getString("uuid"));
            int kills = entryNbt.getInteger("kills");
            map.put(uuid, kills);
         } catch (IllegalArgumentException var6) {
         }
      }

      return map;
   }
}
