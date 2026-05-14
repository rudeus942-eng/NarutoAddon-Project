
package net.luck.narutoaddon.OtherCode.raid.network;

import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class RaidClientData {
   private static UUID currentPartyId = null;
   private static UUID partyLeaderId = null;
   private static String partyLeaderName = "";
   private static List<RaidPartyDataMessage.PartyMemberData> partyMembers = new ArrayList();
   private static int partyState = 0;
   private static String targetBossId = "";
   private static RaidDifficulty difficulty;
   private static List<PendingInviteData> pendingInvites;
   private static List<PartyPlayerListMessage.PlayerEntry> availablePlayers;
   private static boolean queueWindowOpen;
   private static long nextQueueEventTimeMs;
   private static boolean inRaid;
   private static int currentRaidId;
   private static String bossId;
   private static String bossDisplayName;
   private static int bossEntityId;
   private static float bossCurrentHealth;
   private static float bossMaxHealth;
   private static float bossHealthPercent;
   private static int currentPhase;
   private static int totalPhases;
   private static String phaseName;
   private static boolean bossImmune;
   private static String immunityReason;
   private static int enrageLevel;
   private static long raidTimeElapsed;
   private static int bossDifficulty;
   private static String currentMechanicName;
   private static String currentWarningMessage;
   private static int currentMechanicType;
   private static int warningTicksRemaining;
   private static boolean mechanicActive;
   private static long mechanicStartTime;
   private static List<RaidMechanicMessage.ZoneData> safeZones;
   private static List<RaidMechanicMessage.ZoneData> dangerZones;
   private static String puzzleObjective;
   private static int puzzleProgress;
   private static int puzzleTotal;
   private static boolean puzzleActive;

   public static boolean isQueueWindowOpen() {
      return queueWindowOpen;
   }

   public static long getNextQueueEventTimeMs() {
      return nextQueueEventTimeMs;
   }

   public static void setQueueWindowState(boolean open, long nextEventMs) {
      queueWindowOpen = open;
      nextQueueEventTimeMs = nextEventMs;
   }

   public static void handlePartyUpdate(RaidPartyDataMessage message) {
      switch (message.getAction()) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
            currentPartyId = message.getPartyId();
            partyLeaderId = message.getLeaderId();
            partyLeaderName = message.getLeaderName();
            partyMembers = new ArrayList(message.getMembers());
            partyState = message.getPartyState();
            targetBossId = message.getTargetBossId();
            difficulty = message.getDifficulty();
            break;
         case 5:
            clearPartyData();
            break;
         case 6:
            pendingInvites.add(new PendingInviteData(message.getInviterName(), message.getPartyId(), System.currentTimeMillis()));
            break;
         case 7:
            pendingInvites.clear();
      }

   }

   public static void handleBossUpdate(RaidBossDataMessage message) {
      switch (message.getAction()) {
         case 0:
            inRaid = true;
            currentRaidId = message.getRaidId();
            bossId = message.getBossId();
            bossDisplayName = message.getBossDisplayName();
            bossEntityId = message.getEntityId();
            bossMaxHealth = message.getMaxHealth();
            bossCurrentHealth = message.getCurrentHealth();
            bossHealthPercent = message.getHealthPercent();
            totalPhases = message.getTotalPhases();
            currentPhase = message.getCurrentPhase();
            bossDifficulty = message.getDifficulty();
            bossImmune = false;
            immunityReason = "";
            enrageLevel = 0;
            break;
         case 1:
            bossCurrentHealth = message.getCurrentHealth();
            bossMaxHealth = message.getMaxHealth();
            bossHealthPercent = message.getHealthPercent();
            break;
         case 2:
            currentPhase = message.getCurrentPhase();
            totalPhases = message.getTotalPhases();
            phaseName = message.getPhaseName();
            break;
         case 3:
            bossImmune = message.isImmune();
            immunityReason = message.getImmunityReason();
            break;
         case 4:
            enrageLevel = message.getEnrageLevel();
            raidTimeElapsed = message.getRaidTimeElapsed();
            break;
         case 5:
         case 6:
            clearBossData();
      }

   }

   public static void handleMechanicUpdate(RaidMechanicMessage message) {
      switch (message.getAction()) {
         case 0:
            currentMechanicName = message.getMechanicName();
            currentWarningMessage = message.getWarningMessage();
            currentMechanicType = message.getMechanicType();
            warningTicksRemaining = message.getWarningTicks();
            mechanicActive = false;
            break;
         case 1:
            currentMechanicName = message.getMechanicName();
            currentMechanicType = message.getMechanicType();
            mechanicActive = true;
            mechanicStartTime = System.currentTimeMillis();
            warningTicksRemaining = 0;
            break;
         case 2:
            if (currentMechanicName.equals(message.getMechanicName())) {
               clearMechanicData();
            }
            break;
         case 3:
            safeZones = new ArrayList(message.getSafeZones());
            break;
         case 4:
            dangerZones = new ArrayList(message.getDangerZones());
            break;
         case 5:
            puzzleObjective = message.getPuzzleObjective();
            puzzleProgress = message.getPuzzleProgress();
            puzzleTotal = message.getPuzzleTotal();
            puzzleActive = puzzleTotal > 0;
            break;
         case 6:
            clearMechanicData();
            safeZones.clear();
            dangerZones.clear();
            puzzleActive = false;
      }

   }

   public static void clearPartyData() {
      currentPartyId = null;
      partyLeaderId = null;
      partyLeaderName = "";
      partyMembers.clear();
      partyState = 0;
      targetBossId = "";
      difficulty = RaidDifficulty.GENIN;
   }

   public static void clearBossData() {
      inRaid = false;
      currentRaidId = -1;
      bossId = "";
      bossDisplayName = "";
      bossEntityId = -1;
      bossCurrentHealth = 0.0F;
      bossMaxHealth = 0.0F;
      bossHealthPercent = 0.0F;
      currentPhase = 1;
      totalPhases = 1;
      phaseName = "";
      bossImmune = false;
      immunityReason = "";
      enrageLevel = 0;
      raidTimeElapsed = 0L;
   }

   public static void clearMechanicData() {
      currentMechanicName = "";
      currentWarningMessage = "";
      currentMechanicType = 0;
      warningTicksRemaining = 0;
      mechanicActive = false;
   }

   public static void clearInvite() {
      pendingInvites.clear();
   }

   public static void clearInvite(UUID partyId) {
      pendingInvites.removeIf((inv) -> inv.partyId != null && inv.partyId.equals(partyId));
   }

   public static void clearAllData() {
      clearPartyData();
      clearBossData();
      clearMechanicData();
      clearInvite();
      availablePlayers.clear();
      safeZones.clear();
      dangerZones.clear();
      puzzleActive = false;
      puzzleObjective = "";
      puzzleProgress = 0;
      puzzleTotal = 0;
   }

   public static boolean hasParty() {
      return currentPartyId != null;
   }

   public static UUID getCurrentPartyId() {
      return currentPartyId;
   }

   public static UUID getPartyLeaderId() {
      return partyLeaderId;
   }

   public static String getPartyLeaderName() {
      return partyLeaderName;
   }

   public static List<RaidPartyDataMessage.PartyMemberData> getPartyMembers() {
      return partyMembers;
   }

   public static int getPartyState() {
      return partyState;
   }

   public static String getTargetBossId() {
      return targetBossId;
   }

   public static RaidDifficulty getDifficulty() {
      return difficulty;
   }

   public static boolean hasPendingInvite() {
      long now = System.currentTimeMillis();
      pendingInvites.removeIf((inv) -> now - inv.receivedTime >= 60000L);
      return !pendingInvites.isEmpty();
   }

   public static List<PendingInviteData> getPendingInvites() {
      long now = System.currentTimeMillis();
      pendingInvites.removeIf((inv) -> now - inv.receivedTime >= 60000L);
      return pendingInvites;
   }

   public static String getPendingInviterName() {
      List<PendingInviteData> invites = getPendingInvites();
      return invites.isEmpty() ? null : ((PendingInviteData)invites.get(0)).inviterName;
   }

   public static UUID getPendingInvitePartyId() {
      List<PendingInviteData> invites = getPendingInvites();
      return invites.isEmpty() ? null : ((PendingInviteData)invites.get(0)).partyId;
   }

   public static List<PartyPlayerListMessage.PlayerEntry> getAvailablePlayers() {
      return availablePlayers;
   }

   public static void setAvailablePlayers(List<PartyPlayerListMessage.PlayerEntry> players) {
      availablePlayers = players != null ? new ArrayList(players) : new ArrayList();
   }

   public static boolean isInRaid() {
      return inRaid;
   }

   public static int getCurrentRaidId() {
      return currentRaidId;
   }

   public static String getBossId() {
      return bossId;
   }

   public static String getBossDisplayName() {
      return bossDisplayName;
   }

   public static int getBossEntityId() {
      return bossEntityId;
   }

   public static float getBossCurrentHealth() {
      return bossCurrentHealth;
   }

   public static float getBossMaxHealth() {
      return bossMaxHealth;
   }

   public static float getBossHealthPercent() {
      return bossHealthPercent;
   }

   public static int getCurrentPhase() {
      return currentPhase;
   }

   public static int getTotalPhases() {
      return totalPhases;
   }

   public static String getPhaseName() {
      return phaseName;
   }

   public static boolean isBossImmune() {
      return bossImmune;
   }

   public static String getImmunityReason() {
      return immunityReason;
   }

   public static int getEnrageLevel() {
      return enrageLevel;
   }

   public static long getRaidTimeElapsed() {
      return raidTimeElapsed;
   }

   public static int getBossDifficulty() {
      return bossDifficulty;
   }

   public static String getCurrentMechanicName() {
      return currentMechanicName;
   }

   public static String getCurrentWarningMessage() {
      return currentWarningMessage;
   }

   public static int getCurrentMechanicType() {
      return currentMechanicType;
   }

   public static int getWarningTicksRemaining() {
      return warningTicksRemaining;
   }

   public static boolean isMechanicActive() {
      return mechanicActive;
   }

   public static boolean hasWarning() {
      return !currentWarningMessage.isEmpty() && warningTicksRemaining > 0;
   }

   public static List<RaidMechanicMessage.ZoneData> getSafeZones() {
      return safeZones;
   }

   public static List<RaidMechanicMessage.ZoneData> getDangerZones() {
      return dangerZones;
   }

   public static boolean isPuzzleActive() {
      return puzzleActive;
   }

   public static String getPuzzleObjective() {
      return puzzleObjective;
   }

   public static int getPuzzleProgress() {
      return puzzleProgress;
   }

   public static int getPuzzleTotal() {
      return puzzleTotal;
   }

   public static boolean isInSafeZone(BlockPos playerPos) {
      for(RaidMechanicMessage.ZoneData zone : safeZones) {
         double dist = playerPos.getDistance(zone.pos.getX(), zone.pos.getY(), zone.pos.getZ());
         if (dist <= zone.radius) {
            return true;
         }
      }

      return false;
   }

   public static boolean isInDangerZone(BlockPos playerPos) {
      for(RaidMechanicMessage.ZoneData zone : dangerZones) {
         double dist = playerPos.getDistance(zone.pos.getX(), zone.pos.getY(), zone.pos.getZ());
         if (dist <= zone.radius) {
            return true;
         }
      }

      return false;
   }

   public static void tickWarning() {
      if (warningTicksRemaining > 0) {
         --warningTicksRemaining;
      }

   }

   static {
      difficulty = RaidDifficulty.GENIN;
      pendingInvites = new ArrayList();
      availablePlayers = new ArrayList();
      queueWindowOpen = true;
      nextQueueEventTimeMs = 0L;
      inRaid = false;
      currentRaidId = -1;
      bossId = "";
      bossDisplayName = "";
      bossEntityId = -1;
      bossCurrentHealth = 0.0F;
      bossMaxHealth = 0.0F;
      bossHealthPercent = 0.0F;
      currentPhase = 1;
      totalPhases = 1;
      phaseName = "";
      bossImmune = false;
      immunityReason = "";
      enrageLevel = 0;
      raidTimeElapsed = 0L;
      bossDifficulty = 0;
      currentMechanicName = "";
      currentWarningMessage = "";
      currentMechanicType = 0;
      warningTicksRemaining = 0;
      mechanicActive = false;
      mechanicStartTime = 0L;
      safeZones = new ArrayList();
      dangerZones = new ArrayList();
      puzzleObjective = "";
      puzzleProgress = 0;
      puzzleTotal = 0;
      puzzleActive = false;
   }

   public static class PendingInviteData {
      public final String inviterName;
      public final UUID partyId;
      public final long receivedTime;

      public PendingInviteData(String inviterName, UUID partyId, long receivedTime) {
         this.inviterName = inviterName;
         this.partyId = partyId;
         this.receivedTime = receivedTime;
      }
   }
}
