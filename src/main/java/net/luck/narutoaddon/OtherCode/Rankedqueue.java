
package net.luck.narutoaddon.OtherCode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.*;

public class Rankedqueue {
   public static final int BASE_MMR_RANGE = 100;
   public static final int MMR_RANGE_EXPANSION = 50;
   public static final long MMR_EXPANSION_INTERVAL = 30000L;
   public static final int MAX_MMR_RANGE = 400;
   public static final long MATCH_TIMEOUT = 600000L;
   private static List<QueueEntry> queue = new ArrayList();
   private static Map<Integer, Match> activeMatches = new HashMap();
   private static Map<String, Integer> playerToMatch = new HashMap();
   private static int matchCounter = 0;
   private static Set<Integer> arenasInUse = new HashSet();

   public static QueueResult joinQueue(World world, EntityPlayer player) {
      String uuid = player.getUniqueID().toString();
      String name = player.getName();
      double battleXp = player.getEntityData().getDouble("battle_experience");
      if (battleXp < (double)10000.0F) {
         return new QueueResult(false, "§cYou need at least 10,000 ninja XP to queue for ranked matches!");
      } else if (isInQueue(uuid)) {
         return new QueueResult(false, "You are already in queue!");
      } else if (isInMatch(uuid)) {
         return new QueueResult(false, "You are already in a match!");
      } else {
         Rankeddatastorage storage = Rankeddatastorage.get(world);
         if (storage == null) {
            return new QueueResult(false, "Error: Could not access ranked data!");
         } else {
            Rankeddatastorage.PlayerRankedData data = storage.getPlayerData(uuid);
            int mmr = data.hiddenMmr;
            QueueEntry entry = new QueueEntry(uuid, name, mmr);
            queue.add(entry);
            return new QueueResult(true, "§aYou have joined the ranked queue!");
         }
      }
   }

   public static QueueResult leaveQueue(String uuid) {
      Iterator<QueueEntry> iter = queue.iterator();

      while(iter.hasNext()) {
         QueueEntry entry = (QueueEntry)iter.next();
         if (entry.uuid.equals(uuid)) {
            iter.remove();
            return new QueueResult(true, "§eYou have left the ranked queue.");
         }
      }

      return new QueueResult(false, "You are not in queue!");
   }

   public static boolean isInQueue(String uuid) {
      for(QueueEntry entry : queue) {
         if (entry.uuid.equals(uuid)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isInMatch(String uuid) {
      return playerToMatch.containsKey(uuid);
   }

   public static QueueEntry getQueueEntry(String uuid) {
      for(QueueEntry entry : queue) {
         if (entry.uuid.equals(uuid)) {
            return entry;
         }
      }

      return null;
   }

   public static int getQueueSize() {
      return queue.size();
   }

   public static QueueStatus getQueueStatus(String uuid) {
      QueueEntry entry = getQueueEntry(uuid);
      return entry == null ? new QueueStatus(false, 0, 0L, 0, "") : new QueueStatus(true, queue.size(), entry.getWaitTime(), entry.currentRange, entry.getWaitTimeFormatted());
   }

   public static void processQueue(World world) {
      if (queue.size() >= 2) {
         long now = System.currentTimeMillis();

         for(QueueEntry entry : queue) {
            long waitTime = now - entry.queueTime;
            int expectedExpansions = (int)(waitTime / 30000L);
            int currentExpansions = (entry.currentRange - 100) / 50;
            if (expectedExpansions > currentExpansions) {
               entry.expandRange();
            }
         }

         List<QueueEntry> toRemove = new ArrayList();

         for(int i = 0; i < queue.size(); ++i) {
            if (!toRemove.contains(queue.get(i))) {
               QueueEntry p1 = (QueueEntry)queue.get(i);

               for(int j = i + 1; j < queue.size(); ++j) {
                  if (!toRemove.contains(queue.get(j))) {
                     QueueEntry p2 = (QueueEntry)queue.get(j);
                     int maxRange = Math.max(p1.currentRange, p2.currentRange);
                     int mmrDiff = Math.abs(p1.mmr - p2.mmr);
                     if (mmrDiff <= maxRange) {
                        int arenaId = findAvailableArena(world);
                        if (arenaId == -1) {
                           return;
                        }

                        Match match = createMatch(p1, p2, arenaId);
                        if (match != null) {
                           toRemove.add(p1);
                           toRemove.add(p2);
                           notifyMatchFound(world, match);
                           break;
                        }
                     }
                  }
               }
            }
         }

         queue.removeAll(toRemove);
      }
   }

   private static Match createMatch(QueueEntry p1, QueueEntry p2, int arenaId) {
      ++matchCounter;
      Match match = new Match(matchCounter, p1, p2, arenaId);
      activeMatches.put(matchCounter, match);
      playerToMatch.put(p1.uuid, matchCounter);
      playerToMatch.put(p2.uuid, matchCounter);
      arenasInUse.add(arenaId);
      return match;
   }

   private static int findAvailableArena(World world) {
      List<Rankedarena.Arena> enabledArenas = Rankedarena.getEnabledArenas(world);
      System.out.println("[Ranked] findAvailableArena - Found " + enabledArenas.size() + " enabled arenas");
      System.out.println("[Ranked] Arenas currently in use: " + arenasInUse);

      for(Rankedarena.Arena arena : enabledArenas) {
         System.out.println("[Ranked]   Checking arena " + arena.id + " (" + arena.name + ")");
         if (!arenasInUse.contains(arena.id)) {
            System.out.println("[Ranked]   -> Arena " + arena.id + " is available!");
            return arena.id;
         }

         System.out.println("[Ranked]   -> Arena " + arena.id + " is in use");
      }

      System.out.println("[Ranked] No available arenas found!");
      return -1;
   }

   private static void notifyMatchFound(World world, Match match) {
      System.out.println("[Ranked] notifyMatchFound called - Match ID: " + match.matchId);
      System.out.println("[Ranked] Arena ID: " + match.arenaId);
      System.out.println("[Ranked] Player 1: " + match.player1Name + " (" + match.player1UUID + ")");
      System.out.println("[Ranked] Player 2: " + match.player2Name + " (" + match.player2UUID + ")");
      EntityPlayerMP p1 = getPlayer(world, match.player1UUID);
      EntityPlayerMP p2 = getPlayer(world, match.player2UUID);
      String message = "§6§lMATCH FOUND! §fPreparing arena...";
      if (p1 != null) {
         p1.sendMessage(new TextComponentString(message));
      }

      if (p2 != null) {
         p2.sendMessage(new TextComponentString(message));
      }

      System.out.println("[Ranked] Calling startCountdown...");
      Rankedarena.startCountdown(world, match);
   }

   public static Match getPlayerMatch(String uuid) {
      Integer matchId = (Integer)playerToMatch.get(uuid);
      return matchId == null ? null : (Match)activeMatches.get(matchId);
   }

   public static Match getMatch(int matchId) {
      return (Match)activeMatches.get(matchId);
   }

   public static void endMatch(int matchId, String winnerUUID, String loserUUID) {
      Match match = (Match)activeMatches.get(matchId);
      if (match != null) {
         match.state = "finished";
         match.winnerUUID = winnerUUID;
         match.loserUUID = loserUUID;
         arenasInUse.remove(match.arenaId);
         playerToMatch.remove(match.player1UUID);
         playerToMatch.remove(match.player2UUID);
         activeMatches.remove(matchId);
      }
   }

   public static void cancelMatch(int matchId, String reason) {
      Match match = (Match)activeMatches.get(matchId);
      if (match != null) {
         match.state = "cancelled";
         arenasInUse.remove(match.arenaId);
         playerToMatch.remove(match.player1UUID);
         playerToMatch.remove(match.player2UUID);
         activeMatches.remove(matchId);
      }
   }

   public static List<Match> getActiveMatches() {
      return new ArrayList(activeMatches.values());
   }

   public static int getActiveMatchCount() {
      return activeMatches.size();
   }

   private static EntityPlayerMP getPlayer(World world, String uuid) {
      return world.getMinecraftServer() == null ? null : world.getMinecraftServer().getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
   }

   public static void clearAll() {
      queue.clear();
      activeMatches.clear();
      playerToMatch.clear();
      arenasInUse.clear();
   }

   public static class QueueEntry {
      public String uuid;
      public String name;
      public int mmr;
      public long queueTime;
      public int currentRange;

      public QueueEntry(String uuid, String name, int mmr) {
         this.uuid = uuid;
         this.name = name;
         this.mmr = mmr;
         this.queueTime = System.currentTimeMillis();
         this.currentRange = 100;
      }

      public void expandRange() {
         if (this.currentRange < 400) {
            this.currentRange = Math.min(this.currentRange + 50, 400);
         }

      }

      public long getWaitTime() {
         return System.currentTimeMillis() - this.queueTime;
      }

      public String getWaitTimeFormatted() {
         long waitTime = this.getWaitTime();
         long minutes = waitTime / 60000L;
         long seconds = waitTime % 60000L / 1000L;
         return String.format("%d:%02d", minutes, seconds);
      }
   }

   public static class Match {
      public int matchId;
      public String player1UUID;
      public String player2UUID;
      public String player1Name;
      public String player2Name;
      public int player1Elo;
      public int player2Elo;
      public int arenaId;
      public long startTime;
      public String state;
      public String winnerUUID;
      public String loserUUID;

      public Match(int matchId, QueueEntry p1, QueueEntry p2, int arenaId) {
         this.matchId = matchId;
         this.player1UUID = p1.uuid;
         this.player2UUID = p2.uuid;
         this.player1Name = p1.name;
         this.player2Name = p2.name;
         this.player1Elo = p1.mmr;
         this.player2Elo = p2.mmr;
         this.arenaId = arenaId;
         this.startTime = System.currentTimeMillis();
         this.state = "countdown";
      }
   }

   public static class QueueResult {
      public boolean success;
      public String message;

      public QueueResult(boolean success, String message) {
         this.success = success;
         this.message = message;
      }
   }

   public static class QueueStatus {
      public boolean inQueue;
      public int queueSize;
      public long waitTime;
      public int searchRange;
      public String waitTimeFormatted;

      public QueueStatus(boolean inQueue, int queueSize, long waitTime, int searchRange, String waitTimeFormatted) {
         this.inQueue = inQueue;
         this.queueSize = queueSize;
         this.waitTime = waitTime;
         this.searchRange = searchRange;
         this.waitTimeFormatted = waitTimeFormatted;
      }
   }
}
