
package net.luck.narutoaddon.OtherCode.raid.party;

import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class RaidQueue {
   private List<QueueEntry> queue = new ArrayList();
   private Map<UUID, QueueEntry> playerToEntry = new HashMap();
   private Map<UUID, QueueEntry> partyIdToEntry = new HashMap();

   public QueueEntry addSoloPlayer(EntityPlayerMP player, String bossId, RaidDifficulty difficulty) {
      if (this.playerToEntry.containsKey(player.getUniqueID())) {
         return null;
      } else {
         RaidParty soloParty = new RaidParty(player);
         soloParty.setState(RaidParty.PartyState.QUEUED);
         QueueEntry entry = new QueueEntry(soloParty, bossId, difficulty, true);
         this.queue.add(entry);
         this.playerToEntry.put(player.getUniqueID(), entry);
         this.partyIdToEntry.put(soloParty.getPartyId(), entry);
         return entry;
      }
   }

   public QueueEntry addParty(RaidParty party, String bossId, RaidDifficulty difficulty) {
      for(UUID memberUUID : party.getMemberUUIDs()) {
         if (this.playerToEntry.containsKey(memberUUID)) {
            return null;
         }
      }

      QueueEntry entry = new QueueEntry(party, bossId, difficulty, false);
      this.queue.add(entry);

      for(UUID memberUUID : party.getMemberUUIDs()) {
         this.playerToEntry.put(memberUUID, entry);
      }

      this.partyIdToEntry.put(party.getPartyId(), entry);
      return entry;
   }

   public void addParty(RaidParty party, String bossId, RaidDifficulty difficulty, boolean ignored) {
      this.addParty(party, bossId, difficulty);
   }

   public boolean removePlayer(UUID playerUUID) {
      QueueEntry entry = (QueueEntry)this.playerToEntry.get(playerUUID);
      return entry == null ? false : this.removeEntry(entry);
   }

   public boolean removeParty(UUID partyId) {
      QueueEntry entry = (QueueEntry)this.partyIdToEntry.get(partyId);
      return entry == null ? false : this.removeEntry(entry);
   }

   private boolean removeEntry(QueueEntry entry) {
      this.queue.remove(entry);
      this.partyIdToEntry.remove(entry.party.getPartyId());

      for(UUID memberUUID : entry.party.getMemberUUIDs()) {
         this.playerToEntry.remove(memberUUID);
      }

      return true;
   }

   public boolean isPlayerInQueue(UUID playerUUID) {
      return this.playerToEntry.containsKey(playerUUID);
   }

   public boolean isInQueue(UUID partyId) {
      return this.partyIdToEntry.containsKey(partyId);
   }

   public QueueEntry getEntryForPlayer(UUID playerUUID) {
      return (QueueEntry)this.playerToEntry.get(playerUUID);
   }

   public MatchedGroup tryMatchGroup(String bossId, RaidDifficulty difficulty, int requiredPlayers) {
      List<QueueEntry> candidates = new ArrayList();
      int totalPlayers = 0;

      for(QueueEntry entry : this.queue) {
         if (entry.bossId.equals(bossId) && entry.difficulty == difficulty) {
            candidates.add(entry);
            totalPlayers += entry.getPlayerCount();
         }
      }

      if (totalPlayers < requiredPlayers) {
         return null;
      } else {
         candidates.sort(Comparator.comparingLong((e) -> e.queueTime));
         List<QueueEntry> selected = new ArrayList();
         int selectedPlayers = 0;

         for(QueueEntry entry : candidates) {
            if (selectedPlayers + entry.getPlayerCount() <= requiredPlayers) {
               selected.add(entry);
               selectedPlayers += entry.getPlayerCount();
               if (selectedPlayers == requiredPlayers) {
                  break;
               }
            }
         }

         if (selectedPlayers < requiredPlayers) {
            return null;
         } else {
            RaidParty mergedParty = this.mergeEntries(selected);
            if (mergedParty == null) {
               return null;
            } else {
               for(QueueEntry entry : selected) {
                  this.removeEntry(entry);
               }

               return new MatchedGroup(selected, bossId, difficulty, mergedParty);
            }
         }
      }
   }

   private RaidParty mergeEntries(List<QueueEntry> entries) {
      if (entries.isEmpty()) {
         return null;
      } else {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server == null) {
            return null;
         } else {
            EntityPlayerMP leader = null;
            List<EntityPlayerMP> allPlayers = new ArrayList();

            for(QueueEntry entry : entries) {
               for(UUID memberUUID : entry.party.getMemberUUIDs()) {
                  EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(memberUUID);
                  if (player != null) {
                     if (leader == null) {
                        leader = player;
                     }

                     allPlayers.add(player);
                  }
               }
            }

            if (leader != null && !allPlayers.isEmpty()) {
               RaidParty mergedParty = new RaidParty(leader);

               for(int i = 1; i < allPlayers.size(); ++i) {
                  mergedParty.addMember((EntityPlayerMP)allPlayers.get(i));
               }

               for(UUID memberUUID : mergedParty.getMemberUUIDs()) {
                  mergedParty.setReady(memberUUID, true);
               }

               mergedParty.setState(RaidParty.PartyState.READY);

               for(EntityPlayerMP player : allPlayers) {
                  player.sendMessage(new TextComponentString("§a§lMATCH FOUND! §fPreparing raid..."));
               }

               return mergedParty;
            } else {
               return null;
            }
         }
      }
   }

   public List<QueuedParty> getQueuedParties() {
      List<QueuedParty> result = new ArrayList();

      for(QueueEntry entry : this.queue) {
         result.add(new QueuedParty(entry.party, entry.bossId, entry.difficulty));
      }

      return result;
   }

   public List<QueuedParty> getQueuedPartiesForBoss(String bossId) {
      List<QueuedParty> result = new ArrayList();

      for(QueueEntry entry : this.queue) {
         if (entry.bossId.equals(bossId)) {
            result.add(new QueuedParty(entry.party, entry.bossId, entry.difficulty));
         }
      }

      return result;
   }

   public QueuedParty getQueuedParty(UUID partyId) {
      QueueEntry entry = (QueueEntry)this.partyIdToEntry.get(partyId);
      return entry != null ? new QueuedParty(entry.party, entry.bossId, entry.difficulty) : null;
   }

   public int getQueuePosition(UUID playerUUID) {
      QueueEntry playerEntry = (QueueEntry)this.playerToEntry.get(playerUUID);
      if (playerEntry == null) {
         return -1;
      } else {
         int position = 0;

         for(QueueEntry entry : this.queue) {
            if (entry.bossId.equals(playerEntry.bossId) && entry.difficulty == playerEntry.difficulty) {
               ++position;
               if (entry == playerEntry) {
                  return position;
               }
            }
         }

         return -1;
      }
   }

   public int getPlayerCountForBossDifficulty(String bossId, RaidDifficulty difficulty) {
      int count = 0;

      for(QueueEntry entry : this.queue) {
         if (entry.bossId.equals(bossId) && entry.difficulty == difficulty) {
            count += entry.getPlayerCount();
         }
      }

      return count;
   }

   public int size() {
      return this.queue.size();
   }

   public int sizeForBoss(String bossId) {
      int count = 0;

      for(QueueEntry entry : this.queue) {
         if (entry.bossId.equals(bossId)) {
            ++count;
         }
      }

      return count;
   }

   public void clear() {
      this.queue.clear();
      this.playerToEntry.clear();
      this.partyIdToEntry.clear();
   }

   public List<QueueEntry> getAllEntries() {
      return Collections.unmodifiableList(this.queue);
   }

   public static class QueueEntry {
      public final UUID entryId = UUID.randomUUID();
      public final RaidParty party;
      public final String bossId;
      public final RaidDifficulty difficulty;
      public final long queueTime;
      public final boolean isSoloQueue;

      public QueueEntry(RaidParty party, String bossId, RaidDifficulty difficulty, boolean isSoloQueue) {
         this.party = party;
         this.bossId = bossId;
         this.difficulty = difficulty;
         this.queueTime = System.currentTimeMillis();
         this.isSoloQueue = isSoloQueue;
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

      public int getPlayerCount() {
         return this.party.getSize();
      }
   }

   public static class MatchedGroup {
      public final List<QueueEntry> entries;
      public final String bossId;
      public final RaidDifficulty difficulty;
      public final RaidParty mergedParty;

      public MatchedGroup(List<QueueEntry> entries, String bossId, RaidDifficulty difficulty, RaidParty mergedParty) {
         this.entries = entries;
         this.bossId = bossId;
         this.difficulty = difficulty;
         this.mergedParty = mergedParty;
      }

      public int getTotalPlayers() {
         return this.mergedParty.getSize();
      }
   }

   public static class QueuedParty {
      public final RaidParty party;
      public final String bossId;
      public final RaidDifficulty difficulty;
      public final long queueTime;

      public QueuedParty(RaidParty party, String bossId, RaidDifficulty difficulty) {
         this.party = party;
         this.bossId = bossId;
         this.difficulty = difficulty;
         this.queueTime = System.currentTimeMillis();
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
}
