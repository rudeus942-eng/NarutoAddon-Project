
package net.luck.narutoaddon.OtherCode;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

import java.util.HashMap;
import java.util.Map;

public class Rankedpenalties {
   public static final int[] DODGE_BAN_MINUTES = new int[]{5, 15, 30, 60, 120};
   public static final int[] DODGE_ELO_PENALTY = new int[]{0, 0, 5, 10, 15};
   public static final int DODGE_RESET_HOURS = 24;
   public static final int MAX_DAILY_DODGES = 10;
   public static final int AFK_WARNING_THRESHOLD = 30;
   public static final int AFK_KICK_THRESHOLD = 60;
   public static final int DISCONNECT_GRACE_PERIOD = 30;
   public static final int MAX_DAILY_DISCONNECTS = 5;
   public static final int EXCESSIVE_DISCONNECT_BAN_HOURS = 2;
   private static Map<String, DodgeData> dodgeData = new HashMap();
   private static Map<String, Long> queueBans = new HashMap();
   private static Map<String, Long> rankedBans = new HashMap();
   private static Map<String, String> banReasons = new HashMap();
   private static Map<String, Long> lastActivity = new HashMap();
   private static Map<String, DisconnectData> disconnectData = new HashMap();

   public static PenaltyResult recordDodge(String uuid) {
      DodgeData data = (DodgeData)dodgeData.get(uuid);
      if (data == null) {
         data = new DodgeData();
         dodgeData.put(uuid, data);
      }

      if (System.currentTimeMillis() - data.dailyReset > 86400000L) {
         data.dodgeCount = 0;
         data.dailyReset = System.currentTimeMillis();
      }

      ++data.dodgeCount;
      data.lastDodge = System.currentTimeMillis();
      int index = Math.min(data.dodgeCount - 1, DODGE_BAN_MINUTES.length - 1);
      int banMinutes = DODGE_BAN_MINUTES[index];
      int eloPenalty = DODGE_ELO_PENALTY[index];
      applyQueueBan(uuid, banMinutes, "dodge");
      if (data.dodgeCount >= 10) {
         banFromRanked(uuid, 1440, "Excessive queue dodging");
      }

      String message = "§c§lPENALTY: Queue Dodge\n§fQueue Ban: §e" + banMinutes + " minutes";
      if (eloPenalty > 0) {
         message = message + "\n§fELO Penalty: §c-" + eloPenalty;
      }

      return new PenaltyResult(data.dodgeCount, banMinutes, eloPenalty, message);
   }

   public static void applyQueueBan(String uuid, int minutes, String reason) {
      long endTime = System.currentTimeMillis() + (long)(minutes * 60) * 1000L;
      queueBans.put(uuid, endTime);
      banReasons.put(uuid + "_queue", reason);
   }

   public static QueueEligibility canQueue(String uuid) {
      if (isRankedBanned(uuid)) {
         String reason = (String)banReasons.get(uuid + "_ranked");
         long remaining = (Long)rankedBans.get(uuid) - System.currentTimeMillis();
         return new QueueEligibility(false, "ranked_ban", "§cYou are banned from ranked: " + reason, formatTime(remaining));
      } else {
         Long banEnd = (Long)queueBans.get(uuid);
         if (banEnd != null) {
            long remaining = banEnd - System.currentTimeMillis();
            if (remaining > 0L) {
               String reason = (String)banReasons.get(uuid + "_queue");
               return new QueueEligibility(false, reason, "§cQueue banned: " + reason, formatTime(remaining));
            }

            queueBans.remove(uuid);
            banReasons.remove(uuid + "_queue");
         }

         return new QueueEligibility(true, (String)null, (String)null, (String)null);
      }
   }

   public static void updatePlayerActivity(String uuid) {
      lastActivity.put(uuid, System.currentTimeMillis());
   }

   public static long getInactiveSeconds(String uuid) {
      Long last = (Long)lastActivity.get(uuid);
      return last == null ? 0L : (System.currentTimeMillis() - last) / 1000L;
   }

   public static boolean shouldWarnAFK(String uuid) {
      long inactive = getInactiveSeconds(uuid);
      return inactive >= 30L && inactive < 60L;
   }

   public static boolean shouldKickAFK(String uuid) {
      return getInactiveSeconds(uuid) >= 60L;
   }

   public static int getSecondsUntilKick(String uuid) {
      long inactive = getInactiveSeconds(uuid);
      return (int)Math.max(0L, 60L - inactive);
   }

   public static void recordDisconnect(String uuid) {
      DisconnectData data = (DisconnectData)disconnectData.get(uuid);
      if (data == null) {
         data = new DisconnectData();
         disconnectData.put(uuid, data);
      }

      if (System.currentTimeMillis() - data.dailyReset > 86400000L) {
         data.disconnectCount = 0;
         data.dailyReset = System.currentTimeMillis();
      }

      ++data.disconnectCount;
      data.lastDisconnect = System.currentTimeMillis();
      if (data.disconnectCount >= 5) {
         banFromRanked(uuid, 120, "Excessive disconnections");
      }

   }

   public static boolean isRankedBanned(String uuid) {
      Long banEnd = (Long)rankedBans.get(uuid);
      if (banEnd == null) {
         return false;
      } else if (banEnd < System.currentTimeMillis()) {
         rankedBans.remove(uuid);
         banReasons.remove(uuid + "_ranked");
         return false;
      } else {
         return true;
      }
   }

   public static void banFromRanked(String uuid, int durationMinutes, String reason) {
      long endTime = System.currentTimeMillis() + (long)(durationMinutes * 60) * 1000L;
      rankedBans.put(uuid, endTime);
      banReasons.put(uuid + "_ranked", reason);
      Rankedqueue.leaveQueue(uuid);
   }

   public static boolean unbanFromRanked(String uuid) {
      if (rankedBans.remove(uuid) != null) {
         banReasons.remove(uuid + "_ranked");
         return true;
      } else {
         return false;
      }
   }

   public static String getBanTimeRemaining(String uuid) {
      Long banEnd = (Long)rankedBans.get(uuid);
      return banEnd != null && banEnd >= System.currentTimeMillis() ? formatTime(banEnd - System.currentTimeMillis()) : null;
   }

   private static String formatTime(long ms) {
      if (ms < 60000L) {
         return ms / 1000L + "s";
      } else {
         long minutes = ms / 60000L;
         if (minutes < 60L) {
            return minutes + "m";
         } else {
            long hours = minutes / 60L;
            minutes %= 60L;
            return hours + "h " + minutes + "m";
         }
      }
   }

   public static void clearPlayerPenalties(String uuid) {
      dodgeData.remove(uuid);
      disconnectData.remove(uuid);
      queueBans.remove(uuid);
      rankedBans.remove(uuid);
      banReasons.remove(uuid + "_queue");
      banReasons.remove(uuid + "_ranked");
      lastActivity.remove(uuid);
   }

   public static void sendPenaltyMessage(EntityPlayerMP player, PenaltyResult result) {
      player.sendMessage(new TextComponentString(result.message));
   }

   public static class DodgeData {
      public int dodgeCount = 0;
      public long lastDodge = 0L;
      public long dailyReset = System.currentTimeMillis();
   }

   public static class DisconnectData {
      public int disconnectCount = 0;
      public long lastDisconnect = 0L;
      public long dailyReset = System.currentTimeMillis();
   }

   public static class QueueEligibility {
      public boolean canQueue;
      public String reason;
      public String message;
      public String timeRemaining;

      public QueueEligibility(boolean canQueue, String reason, String message, String timeRemaining) {
         this.canQueue = canQueue;
         this.reason = reason;
         this.message = message;
         this.timeRemaining = timeRemaining;
      }
   }

   public static class PenaltyResult {
      public int dodgeCount;
      public int banMinutes;
      public int eloPenalty;
      public String message;

      public PenaltyResult(int dodgeCount, int banMinutes, int eloPenalty, String message) {
         this.dodgeCount = dodgeCount;
         this.banMinutes = banMinutes;
         this.eloPenalty = eloPenalty;
         this.message = message;
      }
   }
}
