package net.luck.narutoaddon.OtherCode.territory.network;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

@SideOnly(Side.CLIENT)
public class TerritoryClientData {
   private static final TerritoryClientData INSTANCE = new TerritoryClientData();
   private final Map<String, ClientZoneData> zoneStates = new HashMap();
   private final Map<String, Integer> weeklyScores = new HashMap();
   private final Set<String> zetsuTargetZoneIds = new HashSet();
   private static String trackedZoneId = null;

   public static TerritoryClientData getInstance() {
      return INSTANCE;
   }

   public static String getTrackedZoneId() {
      return null;
   }

   public static void setTrackedZoneId(String zoneId) {
   }

   public void applyFullSync(List<TerritorySyncMessage.ZoneData> zones, List<TerritorySyncMessage.VillageScore> scores) {
   }

   public void applyDeltaUpdate(String zoneId, String newOwner, String captureAttacker, float captureProgress, boolean contested, int wallLevel, int garrisonLevel, int trainingLevel, int watchtowerLevel, int specialistLevel) {
   }

   public void applyDeltaUpdate(String zoneId, String newOwner, float captureProgress) {
   }

   public void setPlayerToggleState(boolean state) {
   }

   public void setPlayerStats(int kills, int captures, int points, int rank) {
   }

   public Map<String, ClientZoneData> getZoneStates() {
      return Collections.emptyMap();
   }

   public boolean isPlayerToggleState() {
      return false;
   }

   public Map<String, Integer> getWeeklyScores() {
      return Collections.emptyMap();
   }

   public ClientZoneData getZone(String zoneId) {
      return null;
   }

   public int getZoneCount() {
      return 0;
   }

   public int getMyKills() {
      return 0;
   }

   public int getMyCaptures() {
      return 0;
   }

   public int getMyPoints() {
      return 0;
   }

   public int getMyRank() {
      return 0;
   }

   public String getMyVillage() {
      return "";
   }

   public void setMyVillage(String village) {
   }

   public Set<String> getZetsuTargetZoneIds() {
      return Collections.emptySet();
   }

   public boolean isZetsuTarget(String zoneId) {
      return false;
   }

   public void setZetsuTargetZoneIds(Set<String> zoneIds) {
   }

   public static void setTerrainMap(int[] pixels) {
   }

   public static int[] getTerrainPixels() {
      return null;
   }

   public static boolean hasTerrainData() {
      return false;
   }

   public static boolean isTerrainTextureReady() {
      return false;
   }

   public static void markTerrainTextureReady() {
   }

   public static void reset() {
   }

   public static class ClientZoneData {
      public String zoneId;
      public String displayName;
      public int centerX;
      public int centerY;
      public int centerZ;
      public int radius;
      public String ownerVillage;
      public String captureAttackerVillage = "";
      public float captureProgress;
      public int strategicValue;
      public boolean contested;
      public int fortifyLevel;
      public int wallLevel;
      public int garrisonLevel;
      public int trainingLevel;
      public int watchtowerLevel;
      public int specialistLevel;
   }
}
