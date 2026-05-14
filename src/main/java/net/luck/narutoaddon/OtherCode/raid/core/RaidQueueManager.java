
package net.luck.narutoaddon.OtherCode.raid.core;

import net.luck.narutoaddon.OtherCode.raid.arena.RaidArena;
import net.luck.narutoaddon.OtherCode.raid.arena.RaidArenaStorage;
import net.luck.narutoaddon.OtherCode.raid.network.RaidQueueStatusMessage;
import net.luck.narutoaddon.OtherCode.raid.network.RaidWindowSyncMessage;
import net.luck.narutoaddon.OtherCode.raid.party.RaidParty;
import net.luck.narutoaddon.OtherCode.raid.party.RaidPartyStorage;
import net.luck.narutoaddon.OtherCode.raid.rewards.RaidRewardDistributor;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class RaidQueueManager {
   private static RaidQueueManager instance;
   private Map<String, List<QueueEntry>> queues = new HashMap();
   private Map<UUID, QueueEntry> playerQueues = new HashMap();
   private Map<String, Long> lastArenaUnavailableNotify = new HashMap();
   public static final long QUEUE_WINDOW_OPEN_MS = 1800000L;
   public static final long QUEUE_CYCLE_INTERVAL_MS = 7200000L;
   private WindowOverride windowOverride;
   private long overrideUntilMs;
   private boolean lastWindowOpenSeen;
   private boolean lastWindowSeenInitialized;
   private long lastNextEventMsSeen;

   public boolean isQueueWindowOpen() {
      long now = System.currentTimeMillis();
      if (this.windowOverride != WindowOverride.AUTO && this.overrideUntilMs > 0L && now >= this.overrideUntilMs) {
         this.windowOverride = WindowOverride.AUTO;
         this.overrideUntilMs = 0L;
      }

      if (this.windowOverride == WindowOverride.FORCE_OPEN) {
         return true;
      } else if (this.windowOverride == WindowOverride.FORCE_CLOSE) {
         return false;
      } else {
         return now % 7200000L < 1800000L;
      }
   }

   public long getNextWindowEventTimeMs() {
      long now = System.currentTimeMillis();
      boolean openNow = this.isQueueWindowOpen();
      if (this.windowOverride != WindowOverride.AUTO && this.overrideUntilMs > now) {
         long expiry = this.overrideUntilMs;
         long expiryPhase = expiry % 7200000L;
         boolean naturalOpenAtExpiry = expiryPhase < 1800000L;
         if (this.windowOverride == WindowOverride.FORCE_OPEN) {
            return naturalOpenAtExpiry ? expiry + (1800000L - expiryPhase) : expiry;
         } else {
            return naturalOpenAtExpiry ? expiry : expiry + (7200000L - expiryPhase);
         }
      } else {
         long phase = now % 7200000L;
         return openNow ? now + (1800000L - phase) : now + (7200000L - phase);
      }
   }

   public WindowOverride getWindowOverride() {
      return this.windowOverride;
   }

   public long getOverrideUntilMs() {
      return this.overrideUntilMs;
   }

   public void forceWindowOpen(long durationMs) {
      long now = System.currentTimeMillis();
      this.overrideUntilMs = 0L;
      this.windowOverride = WindowOverride.FORCE_OPEN;
      this.overrideUntilMs = durationMs > 0L ? now + durationMs : this.getNextNaturalTransitionMs();
      this.broadcastWindowStateToAll();
   }

   public void forceWindowClose(long durationMs) {
      long now = System.currentTimeMillis();
      this.overrideUntilMs = 0L;
      this.windowOverride = WindowOverride.FORCE_CLOSE;
      this.overrideUntilMs = durationMs > 0L ? now + durationMs : this.getNextNaturalTransitionMs();
      this.broadcastWindowStateToAll();
   }

   private long getNextNaturalTransitionMs() {
      long now = System.currentTimeMillis();
      long phase = now % 7200000L;
      return phase < 1800000L ? now + (1800000L - phase) : now + (7200000L - phase);
   }

   public void clearWindowOverride() {
      this.windowOverride = WindowOverride.AUTO;
      this.overrideUntilMs = 0L;
      this.broadcastWindowStateToAll();
   }

   private RaidQueueManager() {
      this.windowOverride = WindowOverride.AUTO;
      this.overrideUntilMs = 0L;
      this.lastWindowOpenSeen = false;
      this.lastWindowSeenInitialized = false;
      this.lastNextEventMsSeen = 0L;
   }

   public static RaidQueueManager getInstance() {
      if (instance == null) {
         instance = new RaidQueueManager();
      }

      return instance;
   }

   public boolean joinQueue(EntityPlayerMP player, String bossId, RaidDifficulty difficulty) {
      UUID playerId = player.getUniqueID();
      if (this.playerQueues.containsKey(playerId)) {
         player.sendMessage(new TextComponentString("§cYou are already in a queue."));
         return false;
      } else {
         RaidManager raidManager = RaidManager.getInstance();
         if (raidManager.getPlayerRaid(playerId) != null) {
            player.sendMessage(new TextComponentString("§cYou are already in a raid."));
            return false;
         } else if (!this.isQueueWindowOpen() && !player.canUseCommand(2, "raidadmin")) {
            long remainingMs = this.getNextWindowEventTimeMs() - System.currentTimeMillis();
            player.sendMessage(new TextComponentString("§cThe raid queue is currently §4CLOSED§c. Next opening in §e" + formatHM(remainingMs) + "§c."));
            return false;
         } else if (raidManager.isOnRaidCooldown(playerId)) {
            long remaining = raidManager.getRaidCooldownRemaining(playerId);
            int minutes = (int)(remaining / 60000L) + 1;
            player.sendMessage(new TextComponentString("§cYou must wait " + minutes + " minutes before joining another raid."));
            return false;
         } else if (raidManager.getQueue().isPlayerInQueue(playerId)) {
            player.sendMessage(new TextComponentString("§cYou are already in a queue."));
            return false;
         } else {
            WorldServer preCheckWorld = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
            RaidArenaStorage preCheckStorage = RaidArenaStorage.get(preCheckWorld);
            if (preCheckStorage == null || preCheckStorage.findAvailableArena(bossId) == null) {
               boolean anyExist = false;
               if (preCheckStorage != null) {
                  for(RaidArena a : preCheckStorage.getArenasForBoss(bossId)) {
                     if (a.isEnabled()) {
                        anyExist = true;
                        break;
                     }
                  }
               }

               if (!anyExist) {
                  player.sendMessage(new TextComponentString("§cNo arenas are set up for this boss and difficulty. Contact an admin."));
                  return false;
               }
            }

            if (difficulty.getEntryCost() > 0) {
               ShopSavedData shopData = ShopSavedData.get(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
               if (shopData != null) {
                  long balance = shopData.getBalance(playerId);
                  if (balance < (long)difficulty.getEntryCost()) {
                     player.sendMessage(new TextComponentString("§cYou need " + difficulty.getEntryCost() + " ryo to enter this raid. You have " + balance + " ryo."));
                     return false;
                  }
               }
            }

            preCheckWorld = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
            RaidPartyStorage partyStorage = RaidPartyStorage.get(preCheckWorld);
            RaidParty party = null;
            if (partyStorage != null) {
               party = partyStorage.getPlayerParty(playerId);
               if (party != null && !party.isLeader(playerId)) {
                  player.sendMessage(new TextComponentString("§cOnly the party leader can queue for raids."));
                  return false;
               }
            }

            if (party != null) {
               UUID leaderUUID = party.getLeaderId();
               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               String queueKey = bossId + "_" + difficulty.ordinal();
               System.out.println("[RaidQueue] Party queue: leader=" + player.getName() + " partySize=" + party.getSize() + " memberUUIDs=" + party.getMemberUUIDs().size() + " bossId=" + bossId + " difficulty=" + difficulty.name() + " queueKey=" + queueKey);

               for(UUID memberUUID : party.getMemberUUIDs()) {
                  if (!this.playerQueues.containsKey(memberUUID) && raidManager.getPlayerRaid(memberUUID) == null && !raidManager.getQueue().isPlayerInQueue(memberUUID)) {
                     if (raidManager.isOnRaidCooldown(memberUUID)) {
                        EntityPlayerMP memberCheck = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(memberUUID);
                        if (memberCheck != null) {
                           long rem = raidManager.getRaidCooldownRemaining(memberUUID);
                           int mins = (int)(rem / 60000L) + 1;
                           memberCheck.sendMessage(new TextComponentString("§cYou are still on raid cooldown (" + mins + " min remaining)."));
                        }
                     } else {
                        EntityPlayerMP member = server.getPlayerList().getPlayerByUUID(memberUUID);
                        if (member != null) {
                           QueueEntry entry = new QueueEntry(member, bossId, difficulty);
                           entry.partyId = leaderUUID;
                           entry.partySize = party.getMemberUUIDs().size();
                           ((List)this.queues.computeIfAbsent(queueKey, (k) -> new ArrayList())).add(entry);
                           this.playerQueues.put(memberUUID, entry);
                           this.sendQueueStatus(member);
                           if (!memberUUID.equals(playerId)) {
                              member.sendMessage(new TextComponentString("§aYour party leader has queued for a raid!"));
                           }
                        }
                     }
                  }
               }

               List<QueueEntry> currentQueue = (List)this.queues.get(queueKey);
               int entriesAdded = currentQueue != null ? currentQueue.size() : 0;
               System.out.println("[RaidQueue] Party queue complete: " + entriesAdded + " entries in queue (required: " + difficulty.getMinPartySize() + ")");
               this.checkAndStartRaid(queueKey);
               return true;
            } else {
               QueueEntry entry = new QueueEntry(player, bossId, difficulty);
               String queueKey = entry.getQueueKey();
               ((List)this.queues.computeIfAbsent(queueKey, (k) -> new ArrayList())).add(entry);
               this.playerQueues.put(playerId, entry);
               this.sendQueueStatus(player);
               this.checkAndStartRaid(queueKey);
               return true;
            }
         }
      }
   }

   public boolean leaveQueue(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      QueueEntry entry = (QueueEntry)this.playerQueues.get(playerId);
      if (entry == null) {
         return false;
      } else {
         if (entry.partyId != null) {
            UUID partyId = entry.partyId;
            String queueKey = entry.getQueueKey();
            List<UUID> toRemove = new ArrayList();

            for(Map.Entry<UUID, QueueEntry> e : this.playerQueues.entrySet()) {
               if (partyId.equals(((QueueEntry)e.getValue()).partyId)) {
                  toRemove.add(e.getKey());
               }
            }

            for(UUID memberUUID : toRemove) {
               this.playerQueues.remove(memberUUID);
               EntityPlayerMP member = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(memberUUID);
               if (member != null) {
                  this.sendQueueStatus(member);
                  if (!memberUUID.equals(playerId)) {
                     member.sendMessage(new TextComponentString("§eYour party has left the raid queue."));
                  }
               }
            }

            List<QueueEntry> queue = (List)this.queues.get(queueKey);
            if (queue != null) {
               queue.removeIf((ex) -> partyId.equals(ex.partyId));
               if (queue.isEmpty()) {
                  this.queues.remove(queueKey);
               }
            }
         } else {
            this.playerQueues.remove(playerId);
            String queueKey = entry.getQueueKey();
            List<QueueEntry> queue = (List)this.queues.get(queueKey);
            if (queue != null) {
               queue.removeIf((ex) -> ex.playerId.equals(playerId));
               if (queue.isEmpty()) {
                  this.queues.remove(queueKey);
               }
            }

            this.sendQueueStatus(player);
         }

         return true;
      }
   }

   public boolean isInQueue(EntityPlayerMP player) {
      return this.playerQueues.containsKey(player.getUniqueID());
   }

   public QueueEntry getQueueEntry(EntityPlayerMP player) {
      return (QueueEntry)this.playerQueues.get(player.getUniqueID());
   }

   public int getQueueSize(String bossId, RaidDifficulty difficulty) {
      String queueKey = bossId + "_" + difficulty.ordinal();
      List<QueueEntry> queue = (List)this.queues.get(queueKey);
      return queue != null ? queue.size() : 0;
   }

   public int getTotalQueueSize() {
      return this.playerQueues.size();
   }

   public void sendQueueStatus(EntityPlayerMP player) {
      QueueEntry entry = (QueueEntry)this.playerQueues.get(player.getUniqueID());
      boolean inQueue = entry != null;
      int queueTime = inQueue ? entry.getQueueTimeSeconds() : 0;
      String bossId = inQueue ? entry.bossId : "";
      int difficulty = inQueue ? entry.difficulty.ordinal() : 0;
      int queueSize = 0;
      int requiredPlayers = RaidDifficulty.GENIN.getMinPartySize();
      if (inQueue) {
         String queueKey = entry.getQueueKey();
         List<QueueEntry> queue = (List)this.queues.get(queueKey);
         queueSize = queue != null ? queue.size() : 0;
         requiredPlayers = entry.difficulty.getMinPartySize();
      }

      boolean inRaid = RaidManager.getInstance().isPlayerInRaid(player.getUniqueID());
      String currentRaidBoss = "";
      if (inRaid) {
         RaidInstance raid = RaidManager.getInstance().getPlayerRaid(player.getUniqueID());
         if (raid != null && raid.getBoss() != null) {
            currentRaidBoss = raid.getBoss().getBossDisplayName();
         }
      }

      boolean hasPendingRewards = false;
      RaidRewardDistributor distributor = RaidRewardDistributor.get(player.world);
      if (distributor != null) {
         hasPendingRewards = distributor.hasPendingRewards(player.getUniqueID());
      }

      RaidModInit.NETWORK.sendTo(new RaidQueueStatusMessage(inQueue, queueTime, queueSize, bossId, difficulty, inRaid, currentRaidBoss, hasPendingRewards), player);
   }

   private void checkAndStartRaid(String queueKey) {
      List<QueueEntry> queue = (List)this.queues.get(queueKey);
      if (queue != null && !queue.isEmpty()) {
         RaidDifficulty difficulty = ((QueueEntry)queue.get(0)).difficulty;
         int requiredPlayers = difficulty.getMinPartySize();
         if (queue.size() >= requiredPlayers) {
            String bossId = ((QueueEntry)queue.get(0)).bossId;
            System.out.println("[RaidQueue] Checking match for " + queueKey + ": queueSize=" + queue.size() + " required=" + requiredPlayers + " bossId=" + bossId);
            WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
            RaidArenaStorage arenaStorage = RaidArenaStorage.get(world);
            RaidArena arena = null;
            if (arenaStorage != null) {
               arena = arenaStorage.findAvailableArena(bossId);
               if (arena == null) {
                  int totalArenas = arenaStorage.getArenaCount();
                  List<RaidArena> bossArenas = arenaStorage.getArenasForBoss(bossId);
                  System.out.println("[RaidQueue] Arena lookup FAILED: bossId='" + bossId + "' totalArenas=" + totalArenas + " arenasForBoss=" + bossArenas.size());

                  for(RaidArena a : bossArenas) {
                     System.out.println("[RaidQueue]   Arena '" + a.getName() + "' enabled=" + a.isEnabled() + " inUse=" + a.isInUse() + " available=" + a.isAvailable());
                  }

                  if (bossArenas.isEmpty()) {
                     System.out.println("[RaidQueue] All arena bossIds in storage:");

                     for(RaidArena a : arenaStorage.getAllArenas()) {
                        System.out.println("[RaidQueue]   Arena '" + a.getName() + "' bossId='" + a.getBossId() + "'");
                     }
                  }
               }
            } else {
               System.out.println("[RaidQueue] Arena storage is null!");
            }

            if (arena == null) {
               long now = System.currentTimeMillis();
               Long lastNotify = (Long)this.lastArenaUnavailableNotify.get(queueKey);
               if (lastNotify == null || now - lastNotify > 30000L) {
                  this.lastArenaUnavailableNotify.put(queueKey, now);

                  for(QueueEntry entry : queue) {
                     EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(entry.playerId);
                     if (player != null) {
                        player.sendMessage(new TextComponentString("§c§l[RAID] §cNo arena available for boss '" + bossId + "'. Please contact an admin."));
                     }
                  }
               }

            } else {
               this.lastArenaUnavailableNotify.remove(queueKey);
               System.out.println("[RaidQueue] Arena found: '" + arena.getName() + "' id=" + arena.getArenaId());
               queue.sort(Comparator.comparingLong((ex) -> ex.joinTime));
               List<QueueEntry> raidPlayers = new ArrayList();
               Set<UUID> selectedParties = new HashSet();
               int selectedCount = 0;

               for(QueueEntry entry : queue) {
                  if (selectedCount >= requiredPlayers) {
                     break;
                  }

                  if (entry.partyId == null) {
                     if (selectedCount + 1 <= requiredPlayers) {
                        raidPlayers.add(entry);
                        ++selectedCount;
                     }
                  } else if (!selectedParties.contains(entry.partyId)) {
                     int partyMembersInQueue = 0;
                     List<QueueEntry> partyEntries = new ArrayList();

                     for(QueueEntry pe : queue) {
                        if (entry.partyId.equals(pe.partyId)) {
                           ++partyMembersInQueue;
                           partyEntries.add(pe);
                        }
                     }

                     System.out.println("[RaidQueue] Party " + entry.partyId + " has " + partyMembersInQueue + " members in queue (need " + (requiredPlayers - selectedCount) + " more)");
                     if (selectedCount + partyMembersInQueue <= requiredPlayers) {
                        raidPlayers.addAll(partyEntries);
                        selectedParties.add(entry.partyId);
                        selectedCount += partyMembersInQueue;
                     } else {
                        System.out.println("[RaidQueue] Party too large to fit: " + selectedCount + "+" + partyMembersInQueue + " > " + requiredPlayers);
                     }
                  }
               }

               System.out.println("[RaidQueue] Selection result: selectedCount=" + selectedCount + " requiredPlayers=" + requiredPlayers);
               if (selectedCount < requiredPlayers) {
                  System.out.println("[RaidQueue] Not enough players selected, aborting match");
               } else {
                  List<EntityPlayerMP> players = new ArrayList();

                  for(QueueEntry entry : raidPlayers) {
                     EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(entry.playerId);
                     if (player != null) {
                        players.add(player);
                     } else {
                        System.out.println("[RaidQueue] WARNING: Player " + entry.playerName + " (" + entry.playerId + ") is offline, skipping");
                     }
                  }

                  if (players.isEmpty()) {
                     System.out.println("[RaidQueue] All matched players are offline!");
                  } else {
                     System.out.println("[RaidQueue] MATCH FOUND: " + players.size() + " players, starting raid");

                     for(QueueEntry entry : raidPlayers) {
                        this.playerQueues.remove(entry.playerId);
                     }

                     queue.removeAll(raidPlayers);
                     if (queue.isEmpty()) {
                        this.queues.remove(queueKey);
                     }

                     for(EntityPlayerMP player : players) {
                        player.sendMessage(new TextComponentString("§a§lMATCH FOUND! §fPreparing raid..."));
                     }

                     try {
                        this.startRaidWithArena(players, bossId, difficulty, arena, world);
                     } catch (Exception e) {
                        System.out.println("[RaidQueue] ERROR starting raid: " + e.getMessage());
                        e.printStackTrace();

                        for(EntityPlayerMP player : players) {
                           player.sendMessage(new TextComponentString("§c§l[RAID] §cFailed to start raid due to an error."));
                        }
                     }

                  }
               }
            }
         }
      }
   }

   private void startRaidWithArena(List<EntityPlayerMP> players, String bossId, RaidDifficulty difficulty, RaidArena arena, WorldServer world) {
      if (difficulty.getEntryCost() > 0) {
         ShopSavedData shopData = ShopSavedData.get(world);
         if (shopData != null) {
            for(EntityPlayerMP p : players) {
               long balance = shopData.getBalance(p.getUniqueID());
               if (balance < (long)difficulty.getEntryCost()) {
                  for(EntityPlayerMP notify : players) {
                     notify.sendMessage(new TextComponentString("§c§l[RAID] §c" + p.getName() + " can no longer afford the entry fee. Raid cancelled."));
                  }

                  return;
               }
            }

            for(EntityPlayerMP p : players) {
               shopData.removeBalance(p.getUniqueID(), (long)difficulty.getEntryCost());
               p.sendMessage(new TextComponentString("§e" + difficulty.getEntryCost() + " ryo entry fee deducted."));
            }
         }
      }

      RaidManager raidManager = RaidManager.getInstance();
      EntityPlayerMP leader = (EntityPlayerMP)players.get(0);
      RaidParty party = new RaidParty(leader);

      for(int i = 1; i < players.size(); ++i) {
         party.addMember((EntityPlayerMP)players.get(i));
      }

      for(UUID memberUUID : party.getMemberUUIDs()) {
         party.setReady(memberUUID, true);
      }

      party.setState(RaidParty.PartyState.READY);
      RaidInstance raid = raidManager.startRaid((World)world, (RaidParty)party, bossId, (RaidDifficulty)difficulty, (RaidArena)arena);
      if (raid == null) {
         for(EntityPlayerMP player : players) {
            player.sendMessage(new TextComponentString("§c§l[RAID] §cFailed to start raid. Please try again."));
         }
      }

   }

   public void onPlayerDisconnect(EntityPlayerMP player) {
      this.leaveQueue(player);
   }

   public void tick() {
      boolean openNow = this.isQueueWindowOpen();
      long nextMsNow = this.getNextWindowEventTimeMs();
      if (!this.lastWindowSeenInitialized) {
         this.lastWindowOpenSeen = openNow;
         this.lastNextEventMsSeen = nextMsNow;
         this.lastWindowSeenInitialized = true;
         this.broadcastWindowStateToAll();
      } else if (openNow != this.lastWindowOpenSeen) {
         this.lastWindowOpenSeen = openNow;
         this.lastNextEventMsSeen = nextMsNow;
         this.announceWindowTransition(openNow);
         this.broadcastWindowStateToAll();
      } else if (Math.abs(nextMsNow - this.lastNextEventMsSeen) > 1000L) {
         this.lastNextEventMsSeen = nextMsNow;
         this.broadcastWindowStateToAll();
      }

      for(Map.Entry<UUID, QueueEntry> entry : this.playerQueues.entrySet()) {
         EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID((UUID)entry.getKey());
         if (player != null) {
            this.sendQueueStatus(player);
         }
      }

      for(String queueKey : new ArrayList(this.queues.keySet())) {
         this.checkAndStartRaid(queueKey);
      }

   }

   public void sendWindowStateTo(EntityPlayerMP player) {
      if (player != null) {
         RaidModInit.NETWORK.sendTo(new RaidWindowSyncMessage(this.isQueueWindowOpen(), this.getNextWindowEventTimeMs()), player);
      }
   }

   private void broadcastWindowStateToAll() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         RaidWindowSyncMessage msg = new RaidWindowSyncMessage(this.isQueueWindowOpen(), this.getNextWindowEventTimeMs());

         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            RaidModInit.NETWORK.sendTo(msg, player);
         }

      }
   }

   private void announceWindowTransition(boolean openNow) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         String text = openNow ? "§6§l[Raids] §aRaid queue is now §2§lOPEN§a! Queue up via the raid GUI — closes in §e" + formatHM(1800000L) + "§a." : "§6§l[Raids] §cRaid queue has §4§lCLOSED§c. Next opening in §e" + formatHM(5400000L) + "§c.";
         TextComponentString msg = new TextComponentString(text);

         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            player.sendMessage(msg);
         }

      }
   }

   public static String formatHM(long ms) {
      if (ms < 0L) {
         ms = 0L;
      }

      long totalSec = ms / 1000L;
      long h = totalSec / 3600L;
      long m = totalSec % 3600L / 60L;
      long s = totalSec % 60L;
      if (h > 0L) {
         return h + "h " + m + "m";
      } else {
         return m > 0L ? m + "m " + s + "s" : s + "s";
      }
   }

   public void reset() {
      this.queues.clear();
      this.playerQueues.clear();
   }

   public static enum WindowOverride {
      AUTO,
      FORCE_OPEN,
      FORCE_CLOSE;
   }

   public static class QueueEntry {
      public UUID playerId;
      public String playerName;
      public String bossId;
      public RaidDifficulty difficulty;
      public long joinTime;
      public UUID partyId;
      public int partySize;

      public QueueEntry(EntityPlayerMP player, String bossId, RaidDifficulty difficulty) {
         this.playerId = player.getUniqueID();
         this.playerName = player.getName();
         this.bossId = bossId;
         this.difficulty = difficulty;
         this.joinTime = System.currentTimeMillis();
         this.partyId = null;
         this.partySize = 1;
      }

      public String getQueueKey() {
         return this.bossId + "_" + this.difficulty.ordinal();
      }

      public int getQueueTimeSeconds() {
         return (int)((System.currentTimeMillis() - this.joinTime) / 1000L);
      }
   }
}
