
package net.luck.narutoaddon.OtherCode.quest.pvp.tournament;

import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;
import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpSavedData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextComponent.Serializer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.*;

public class TournamentManager {
   private static TournamentManager instance;
   private final Map<String, TournamentInstance> tournaments = new HashMap();
   private final Map<UUID, double[]> preTournamentPositions = new HashMap();
   private final Set<UUID> activeMatchPlayers = new HashSet();
   private final Map<UUID, List<ItemStack>> pendingRewards = new HashMap();
   private final Map<Integer, Long> matchTimestamps = new HashMap();
   private final Map<String, Long> bracketReadyTimestamps = new HashMap();
   private int tickCounter = 0;
   private final Random random = new Random();
   private static final long COOLDOWN_SMALL_MS = 2700000L;
   private static final long COOLDOWN_LARGE_MS = 1800000L;
   private final Map<UUID, Long> tournamentCooldowns = new HashMap();
   private static final double ARENA_X1 = (double)-2188.0F;
   private static final double ARENA_Y1 = (double)164.0F;
   private static final double ARENA_Z1 = (double)-73.0F;
   private static final double ARENA_X2 = (double)-2255.0F;
   private static final double ARENA_Y2 = (double)164.0F;
   private static final double ARENA_Z2 = (double)-73.0F;
   private static final float YAW_P1 = 90.0F;
   private static final float YAW_P2 = -90.0F;
   private final Map<Integer, Integer> countdownPhase = new HashMap();
   private final Map<Integer, Integer> countdownTickOffset = new HashMap();

   public static TournamentManager getInstance() {
      if (instance == null) {
         instance = new TournamentManager();
      }

      return instance;
   }

   public static void reset() {
      instance = null;
   }

   @Nullable
   public TournamentInstance createTournament(UUID hostId, String hostName, String villageName, int minRank, int maxRank, long signupDurationMs, int matchTimeoutSec) {
      if (this.tournaments.containsKey(villageName)) {
         return null;
      } else {
         String tournamentId = "tourn_" + villageName + "_" + System.currentTimeMillis();
         long now = System.currentTimeMillis();
         TournamentInstance inst = new TournamentInstance(tournamentId, TournamentState.SETUP, villageName, hostId, hostName, now, now + signupDurationMs, minRank, maxRank, matchTimeoutSec);
         this.tournaments.put(villageName, inst);
         return inst;
      }
   }

   public boolean setRewards(String villageName, int place, ItemStack[] items) {
      TournamentInstance inst = (TournamentInstance)this.tournaments.get(villageName);
      if (inst != null && inst.getState() == TournamentState.SETUP) {
         if (items == null) {
            return false;
         } else {
            ItemStack[] target;
            switch (place) {
               case 1:
                  target = inst.getRewards1st();
                  break;
               case 2:
                  target = inst.getRewards2nd();
                  break;
               case 3:
                  target = inst.getRewards3rd();
                  break;
               default:
                  return false;
            }

            for(int i = 0; i < target.length; ++i) {
               if (i < items.length && items[i] != null && !items[i].isEmpty()) {
                  target[i] = items[i].copy();
               } else {
                  target[i] = ItemStack.EMPTY;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean finalizeTournament(String villageName, World world) {
      TournamentInstance inst = (TournamentInstance)this.tournaments.get(villageName);
      if (inst != null && inst.getState() == TournamentState.SETUP) {
         inst.setState(TournamentState.SIGNUP);
         this.broadcastToVillage(villageName, "§6[Tournament] §e" + inst.getHostName() + " has opened tournament signups! Type /tournament or check the PvP tab.", world);
         this.saveTournament(villageName, world);
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public TournamentInstance findOpenTournament(String playerVillage) {
      for(TournamentInstance inst : this.tournaments.values()) {
         if (inst.getState() == TournamentState.SIGNUP && System.currentTimeMillis() < inst.getSignupEndTime() && !inst.isVillageOnly()) {
            return inst;
         }
      }

      return null;
   }

   public String signUp(UUID playerId, String playerName, String villageName, World world) {
      TournamentInstance inst = (TournamentInstance)this.tournaments.get(villageName);
      if (inst == null) {
         inst = this.findOpenTournament(villageName);
         if (inst == null) {
            return "No tournament found.";
         }
      }

      if (inst.getState() != TournamentState.SIGNUP) {
         return "Signups are not currently open.";
      } else if (System.currentTimeMillis() >= inst.getSignupEndTime()) {
         return "Signup period has ended.";
      } else if (inst.isVillageOnly() && !inst.getVillageName().equals(villageName)) {
         return "This tournament is restricted to " + inst.getVillageName() + " only.";
      } else {
         PvpSavedData data = PvpSavedData.get(world);
         QuestDefinition.QuestRank playerRank = data.getPvpRank(playerId);
         if (!inst.isPlayerEligible(playerRank)) {
            return "Your PvP rank (" + playerRank.name() + ") is not eligible for this tournament.";
         } else if (inst.hasParticipant(playerId)) {
            return "You are already signed up.";
         } else {
            long now = System.currentTimeMillis();
            Long cooldownEnd = (Long)this.tournamentCooldowns.get(playerId);
            if (cooldownEnd != null && now < cooldownEnd) {
               long remainingMs = cooldownEnd - now;
               long remainingMin = remainingMs / 60000L + 1L;
               return "§cYou must wait " + remainingMin + " minutes before joining another tournament.";
            } else {
               inst.addParticipant(playerId, playerName);
               this.saveTournament(villageName, world);
               return null;
            }
         }
      }
   }

   public boolean withdrawSignup(UUID playerId, String villageName, World world) {
      TournamentInstance inst = (TournamentInstance)this.tournaments.get(villageName);
      if (inst != null && inst.getState() == TournamentState.SIGNUP) {
         inst.removeParticipant(playerId);
         this.saveTournament(villageName, world);
         return true;
      } else {
         return false;
      }
   }

   public boolean hasTournament(String villageName) {
      return this.tournaments.containsKey(villageName);
   }

   public Set<String> getActiveVillages() {
      return new HashSet(this.tournaments.keySet());
   }

   public void cancelTournament(String villageName, World world) {
      TournamentInstance inst = (TournamentInstance)this.tournaments.get(villageName);
      if (inst != null) {
         if (inst.getState() == TournamentState.IN_PROGRESS) {
            this.teleportActivePlayersBack(inst, world);
         }

         inst.setState(TournamentState.CANCELLED);
         this.tournaments.remove(villageName);
         this.bracketReadyTimestamps.remove(villageName);
         this.broadcastToVillage(villageName, "§6[Tournament] §cThe tournament has been cancelled.", world);
         PvpSavedData data = PvpSavedData.get(world);
         data.removeTournament(villageName);
      }
   }

   public void onServerTick(World world) {
      ++this.tickCounter;
      if (this.tickCounter % 1 == 0) {
         this.tickCountdownPhases(world);
      }

      if (this.tickCounter % 20 == 0) {
         for(String villageName : new ArrayList(this.tournaments.keySet())) {
            TournamentInstance inst = (TournamentInstance)this.tournaments.get(villageName);
            if (inst != null) {
               switch (inst.getState()) {
                  case SIGNUP:
                     if (System.currentTimeMillis() >= inst.getSignupEndTime()) {
                        this.closeSignupAndGenerateBracket(inst, world);
                     }
                     break;
                  case BRACKET_READY:
                     Long bracketTime = (Long)this.bracketReadyTimestamps.get(villageName);
                     if (bracketTime != null && System.currentTimeMillis() - bracketTime >= 5000L) {
                        inst.setState(TournamentState.IN_PROGRESS);
                        this.startNextMatch(inst, world);
                        this.saveTournament(villageName, world);
                     }
                     break;
                  case IN_PROGRESS:
                     this.checkActiveMatchTimeout(inst, world);
               }
            }
         }

      }
   }

   private void tickCountdownPhases(World world) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         List<Integer> toRemove = new ArrayList();

         for(Map.Entry<Integer, Integer> entry : (new HashMap(this.countdownPhase)).entrySet()) {
            int matchIdx = (Integer)entry.getKey();
            int phase = (Integer)entry.getValue();
            int ticks = (Integer)this.countdownTickOffset.getOrDefault(matchIdx, 0) + 1;
            this.countdownTickOffset.put(matchIdx, ticks);
            TournamentMatch match = this.findMatchByIndex(matchIdx);
            if (match != null && match.getMatchState() == TournamentMatch.MatchState.COUNTDOWN) {
               EntityPlayerMP p1 = match.getPlayer1Id() != null ? server.getPlayerList().getPlayerByUUID(match.getPlayer1Id()) : null;
               EntityPlayerMP p2 = match.getPlayer2Id() != null ? server.getPlayerList().getPlayerByUUID(match.getPlayer2Id()) : null;
               if (phase == 0 && ticks == 20) {
                  this.countdownPhase.put(matchIdx, 1);
                  if (p1 != null) {
                     this.sendCountdownNumber(p1, 2, "gold");
                  }

                  if (p2 != null) {
                     this.sendCountdownNumber(p2, 2, "gold");
                  }
               } else if (phase == 1 && ticks == 40) {
                  this.countdownPhase.put(matchIdx, 2);
                  if (p1 != null) {
                     this.sendCountdownNumber(p1, 1, "red");
                  }

                  if (p2 != null) {
                     this.sendCountdownNumber(p2, 1, "red");
                  }
               } else if (phase == 2 && ticks == 60) {
                  this.countdownPhase.put(matchIdx, 3);
                  if (p1 != null) {
                     this.sendFightTitle(p1);
                  }

                  if (p2 != null) {
                     this.sendFightTitle(p2);
                  }

                  match.setMatchState(TournamentMatch.MatchState.ACTIVE);
                  this.matchTimestamps.put(matchIdx, System.currentTimeMillis());
                  toRemove.add(matchIdx);
               }
            } else {
               toRemove.add(matchIdx);
            }
         }

         for(int idx : toRemove) {
            this.countdownPhase.remove(idx);
            this.countdownTickOffset.remove(idx);
         }

      }
   }

   @Nullable
   private TournamentMatch findMatchByIndex(int matchIndex) {
      for(TournamentInstance inst : this.tournaments.values()) {
         if (inst.getBracket() != null) {
            List<TournamentMatch> matches = inst.getBracket().getMatches();
            if (matchIndex >= 0 && matchIndex < matches.size()) {
               return (TournamentMatch)matches.get(matchIndex);
            }
         }
      }

      return null;
   }

   private void closeSignupAndGenerateBracket(TournamentInstance inst, World world) {
      if (inst.getParticipantCount() < 4) {
         this.broadcastToVillage(inst.getVillageName(), "§6[Tournament] §cMinimum 4 players required. Tournament cancelled.", world);
         this.tournaments.remove(inst.getVillageName());
         PvpSavedData data = PvpSavedData.get(world);
         data.removeTournament(inst.getVillageName());
      } else {
         inst.generateBracket();
         inst.setState(TournamentState.BRACKET_READY);
         this.bracketReadyTimestamps.put(inst.getVillageName(), System.currentTimeMillis());
         this.broadcastToVillage(inst.getVillageName(), "§6[Tournament] §aBracket generated! " + inst.getParticipantCount() + " participants. Matches begin shortly.", world);
         this.saveTournament(inst.getVillageName(), world);
      }
   }

   private void startNextMatch(TournamentInstance inst, World world) {
      TournamentBracket bracket = inst.getBracket();
      if (bracket != null) {
         int currentRound = inst.getCurrentRound();
         TournamentMatch nextMatch = null;

         for(TournamentMatch match : bracket.getMatchesForRound(currentRound)) {
            if (match.getMatchState() == TournamentMatch.MatchState.PENDING && match.isReady()) {
               nextMatch = match;
               break;
            }
         }

         if (nextMatch != null) {
            this.startMatch(inst, nextMatch, world);
         } else {
            if (bracket.isRoundComplete(currentRound)) {
               int nextRound = currentRound + 1;
               if (nextRound >= bracket.getRounds()) {
                  this.completeTournament(inst, world);
                  return;
               }

               inst.setCurrentRound(nextRound);
               this.resolveByes(inst, nextRound, world);
               this.startNextMatch(inst, world);
            }

         }
      }
   }

   private void resolveByes(TournamentInstance inst, int round, World world) {
      TournamentBracket bracket = inst.getBracket();
      if (bracket != null) {
         for(TournamentMatch match : bracket.getMatchesForRound(round)) {
            if (match.getMatchState() == TournamentMatch.MatchState.PENDING) {
               UUID p1 = match.getPlayer1Id();
               UUID p2 = match.getPlayer2Id();
               if (p1 != null && p2 == null) {
                  match.setMatchState(TournamentMatch.MatchState.BYE);
                  match.setWinnerId(p1);
                  bracket.advanceWinner(match.getMatchIndex(), p1, match.getPlayer1Name());
               } else if (p1 == null && p2 != null) {
                  match.setMatchState(TournamentMatch.MatchState.BYE);
                  match.setWinnerId(p2);
                  bracket.advanceWinner(match.getMatchIndex(), p2, match.getPlayer2Name());
               }
            }
         }

      }
   }

   private void startMatch(TournamentInstance inst, TournamentMatch match, World world) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         UUID p1Id = match.getPlayer1Id();
         UUID p2Id = match.getPlayer2Id();
         EntityPlayerMP player1 = p1Id != null ? server.getPlayerList().getPlayerByUUID(p1Id) : null;
         EntityPlayerMP player2 = p2Id != null ? server.getPlayerList().getPlayerByUUID(p2Id) : null;
         if (player1 == null && player2 == null) {
            UUID winnerId = this.random.nextBoolean() ? p1Id : p2Id;
            String winnerName = winnerId != null && winnerId.equals(p1Id) ? match.getPlayer1Name() : match.getPlayer2Name();
            match.setWinnerId(winnerId);
            match.setMatchState(TournamentMatch.MatchState.FINISHED);
            if (winnerId != null) {
               inst.getBracket().advanceWinner(match.getMatchIndex(), winnerId, winnerName);
            }

            this.broadcastToVillage(inst.getVillageName(), "§6[Tournament] §e" + match.getPlayer1Name() + " vs " + match.getPlayer2Name() + " - Both offline. " + winnerName + " wins by coin flip.", world);
            this.checkPostMatchProgression(inst, match.getMatchIndex(), world);
         } else if (player1 == null) {
            match.setWinnerId(p2Id);
            match.setMatchState(TournamentMatch.MatchState.FINISHED);
            inst.getBracket().advanceWinner(match.getMatchIndex(), p2Id, match.getPlayer2Name());
            this.broadcastToVillage(inst.getVillageName(), "§6[Tournament] §e" + match.getPlayer2Name() + " wins by forfeit (" + match.getPlayer1Name() + " offline).", world);
            this.checkPostMatchProgression(inst, match.getMatchIndex(), world);
         } else if (player2 == null) {
            match.setWinnerId(p1Id);
            match.setMatchState(TournamentMatch.MatchState.FINISHED);
            inst.getBracket().advanceWinner(match.getMatchIndex(), p1Id, match.getPlayer1Name());
            this.broadcastToVillage(inst.getVillageName(), "§6[Tournament] §e" + match.getPlayer1Name() + " wins by forfeit (" + match.getPlayer2Name() + " offline).", world);
            this.checkPostMatchProgression(inst, match.getMatchIndex(), world);
         } else {
            this.preTournamentPositions.put(p1Id, new double[]{player1.posX, player1.posY, player1.posZ});
            this.preTournamentPositions.put(p2Id, new double[]{player2.posX, player2.posY, player2.posZ});
            player1.connection.setPlayerLocation((double)-2188.0F, (double)164.0F, (double)-73.0F, 90.0F, 0.0F);
            player1.setPositionAndUpdate((double)-2188.0F, (double)164.0F, (double)-73.0F);
            player2.connection.setPlayerLocation((double)-2255.0F, (double)164.0F, (double)-73.0F, -90.0F, 0.0F);
            player2.setPositionAndUpdate((double)-2255.0F, (double)164.0F, (double)-73.0F);
            match.setMatchState(TournamentMatch.MatchState.COUNTDOWN);
            this.matchTimestamps.put(match.getMatchIndex(), System.currentTimeMillis());
            this.countdownPhase.put(match.getMatchIndex(), 0);
            this.countdownTickOffset.put(match.getMatchIndex(), 0);
            this.activeMatchPlayers.add(p1Id);
            this.activeMatchPlayers.add(p2Id);
            player1.setHealth(player1.getMaxHealth());
            player2.setHealth(player2.getMaxHealth());
            this.sendCountdownNumber(player1, 3, "yellow");
            this.sendCountdownNumber(player2, 3, "yellow");
            this.broadcastToVillage(inst.getVillageName(), "§6[Tournament] §b" + match.getPlayer1Name() + " §7vs §b" + match.getPlayer2Name() + " §7- Match starting!", world);
            this.saveTournament(inst.getVillageName(), world);
         }
      }
   }

   private void checkActiveMatchTimeout(TournamentInstance inst, World world) {
      TournamentBracket bracket = inst.getBracket();
      if (bracket != null) {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            for(TournamentMatch match : bracket.getMatches()) {
               if (match.getMatchState() == TournamentMatch.MatchState.ACTIVE) {
                  Long timestamp = (Long)this.matchTimestamps.get(match.getMatchIndex());
                  if (timestamp != null) {
                     long elapsedSec = (System.currentTimeMillis() - timestamp) / 1000L;
                     if (elapsedSec >= (long)inst.getMatchTimeoutSeconds()) {
                        this.resolveMatchByHealth(inst, match, server, world);
                     }
                  }
               }
            }

         }
      }
   }

   private void resolveMatchByHealth(TournamentInstance inst, TournamentMatch match, MinecraftServer server, World world) {
      EntityPlayerMP p1 = match.getPlayer1Id() != null ? server.getPlayerList().getPlayerByUUID(match.getPlayer1Id()) : null;
      EntityPlayerMP p2 = match.getPlayer2Id() != null ? server.getPlayerList().getPlayerByUUID(match.getPlayer2Id()) : null;
      float p1HealthPct = p1 != null ? p1.getHealth() / p1.getMaxHealth() : 0.0F;
      float p2HealthPct = p2 != null ? p2.getHealth() / p2.getMaxHealth() : 0.0F;
      UUID winnerId;
      String winnerName;
      String reason;
      if (p1HealthPct > p2HealthPct) {
         winnerId = match.getPlayer1Id();
         winnerName = match.getPlayer1Name();
         reason = "higher health";
      } else if (p2HealthPct > p1HealthPct) {
         winnerId = match.getPlayer2Id();
         winnerName = match.getPlayer2Name();
         reason = "higher health";
      } else {
         if (this.random.nextBoolean()) {
            winnerId = match.getPlayer1Id();
            winnerName = match.getPlayer1Name();
         } else {
            winnerId = match.getPlayer2Id();
            winnerName = match.getPlayer2Name();
         }

         reason = "tiebreaker (coin flip)";
      }

      this.completeMatch(inst, match.getMatchIndex(), winnerId, winnerName, reason, world);
   }

   public void onPlayerDeath(EntityPlayerMP victim, World world) {
      UUID victimId = victim.getUniqueID();
      if (this.activeMatchPlayers.contains(victimId)) {
         for(TournamentInstance inst : this.tournaments.values()) {
            if (inst.getState() == TournamentState.IN_PROGRESS) {
               TournamentMatch match = inst.getMatchForPlayer(victimId);
               if (match != null && (match.getMatchState() == TournamentMatch.MatchState.ACTIVE || match.getMatchState() == TournamentMatch.MatchState.COUNTDOWN)) {
                  UUID opponentId = match.getOpponent(victimId);
                  String opponentName;
                  if (opponentId != null && opponentId.equals(match.getPlayer1Id())) {
                     opponentName = match.getPlayer1Name();
                  } else {
                     opponentName = match.getPlayer2Name();
                  }

                  this.completeMatch(inst, match.getMatchIndex(), opponentId, opponentName, "knockout", world);
                  return;
               }
            }
         }

      }
   }

   public void onPlayerDisconnect(EntityPlayerMP player, World world) {
      UUID playerId = player.getUniqueID();
      if (this.activeMatchPlayers.contains(playerId)) {
         for(TournamentInstance inst : this.tournaments.values()) {
            if (inst.getState() == TournamentState.IN_PROGRESS) {
               TournamentMatch match = inst.getMatchForPlayer(playerId);
               if (match != null && (match.getMatchState() == TournamentMatch.MatchState.ACTIVE || match.getMatchState() == TournamentMatch.MatchState.COUNTDOWN)) {
                  UUID opponentId = match.getOpponent(playerId);
                  String opponentName;
                  if (opponentId != null && opponentId.equals(match.getPlayer1Id())) {
                     opponentName = match.getPlayer1Name();
                  } else {
                     opponentName = match.getPlayer2Name();
                  }

                  this.completeMatch(inst, match.getMatchIndex(), opponentId, opponentName, "disconnect forfeit", world);
                  return;
               }
            }
         }

      }
   }

   public void onPlayerLogin(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      List<ItemStack> pending = (List)this.pendingRewards.remove(playerId);
      if (pending != null && !pending.isEmpty()) {
         player.sendMessage(new TextComponentString("§6[Tournament] §aYou have pending tournament rewards!"));

         for(ItemStack stack : pending) {
            if (!stack.isEmpty() && !player.inventory.addItemStackToInventory(stack.copy())) {
               EntityItem drop = new EntityItem(player.world, player.posX, player.posY, player.posZ, stack.copy());
               drop.setNoPickupDelay();
               player.world.spawnEntity(drop);
            }
         }
      }

      PvpSavedData data = PvpSavedData.get(player.world);
      int pendingRyo = data.removePendingTournamentRyo(playerId);
      if (pendingRyo > 0) {
         player.sendMessage(new TextComponentString("§6[Tournament] §aYou have pending tournament ryo!"));
         RyoRewardHelper.grantRyo(player, pendingRyo);
      }

   }

   private void completeMatch(TournamentInstance inst, int matchIndex, UUID winnerId, String winnerName, String reason, World world) {
      TournamentBracket bracket = inst.getBracket();
      if (bracket != null) {
         List<TournamentMatch> matches = bracket.getMatches();
         if (matchIndex >= 0 && matchIndex < matches.size()) {
            TournamentMatch match = (TournamentMatch)matches.get(matchIndex);
            match.setWinnerId(winnerId);
            match.setMatchState(TournamentMatch.MatchState.FINISHED);
            if (winnerId != null) {
               bracket.advanceWinner(matchIndex, winnerId, winnerName);
            }

            UUID p1Id = match.getPlayer1Id();
            UUID p2Id = match.getPlayer2Id();
            if (p1Id != null) {
               this.activeMatchPlayers.remove(p1Id);
            }

            if (p2Id != null) {
               this.activeMatchPlayers.remove(p2Id);
            }

            this.matchTimestamps.remove(matchIndex);
            this.countdownPhase.remove(matchIndex);
            this.countdownTickOffset.remove(matchIndex);
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               this.teleportPlayerBack(p1Id, server);
               this.teleportPlayerBack(p2Id, server);
               EntityPlayerMP p1Player = p1Id != null ? server.getPlayerList().getPlayerByUUID(p1Id) : null;
               EntityPlayerMP p2Player = p2Id != null ? server.getPlayerList().getPlayerByUUID(p2Id) : null;
               if (p1Player != null) {
                  server.getCommandManager().executeCommand(server, "clearcooldowns " + p1Player.getName());
               }

               if (p2Player != null) {
                  server.getCommandManager().executeCommand(server, "clearcooldowns " + p2Player.getName());
               }

               EntityPlayerMP winnerPlayer = winnerId != null ? server.getPlayerList().getPlayerByUUID(winnerId) : null;
               UUID loserId = winnerId != null && winnerId.equals(p1Id) ? p2Id : p1Id;
               EntityPlayerMP loserPlayer = loserId != null ? server.getPlayerList().getPlayerByUUID(loserId) : null;
               if (winnerPlayer != null) {
                  this.sendTitle(winnerPlayer, "§a§lVICTORY", "§7You won by " + reason);
               }

               if (loserPlayer != null) {
                  this.sendTitle(loserPlayer, "§c§lDEFEATED", "§7Lost by " + reason);
               }
            }

            String loserName;
            if (winnerId != null && winnerId.equals(p1Id)) {
               loserName = match.getPlayer2Name();
            } else {
               loserName = match.getPlayer1Name();
            }

            this.broadcastToVillage(inst.getVillageName(), "§6[Tournament] §a" + winnerName + " §7defeated §c" + loserName + " §7(" + reason + ")", world);
            this.checkPostMatchProgression(inst, matchIndex, world);
         }
      }
   }

   private void checkPostMatchProgression(TournamentInstance inst, int matchIndex, World world) {
      TournamentBracket bracket = inst.getBracket();
      if (bracket != null) {
         if (bracket.isTournamentComplete()) {
            this.completeTournament(inst, world);
         } else {
            this.startNextMatch(inst, world);
         }

         this.saveTournament(inst.getVillageName(), world);
      }
   }

   private void completeTournament(TournamentInstance inst, World world) {
      inst.setState(TournamentState.COMPLETE);
      TournamentBracket bracket = inst.getBracket();
      if (bracket != null) {
         UUID firstId = bracket.getWinnerId();
         UUID secondId = bracket.getRunnerUpId();
         List<UUID> thirdIds = bracket.getThirdPlaceIds();
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         Map<UUID, Integer> ryoEarnings = this.distributeRyoRewards(inst, world);
         long now = System.currentTimeMillis();
         long cooldownMs = inst.getParticipantCount() >= 9 ? 1800000L : 2700000L;

         for(UUID participantId : inst.getParticipants()) {
            this.tournamentCooldowns.put(participantId, now + cooldownMs);
         }

         this.saveCooldowns(world);
         String firstName = this.getPlayerNameFromBracket(bracket, firstId);
         String secondName = this.getPlayerNameFromBracket(bracket, secondId);
         StringBuilder thirdNames = new StringBuilder();

         for(int i = 0; i < thirdIds.size(); ++i) {
            if (i > 0) {
               thirdNames.append(", ");
            }

            thirdNames.append(this.getPlayerNameFromBracket(bracket, (UUID)thirdIds.get(i)));
         }

         int firstRyo = ryoEarnings.containsKey(firstId) ? (Integer)ryoEarnings.get(firstId) : 0;
         int secondRyo = ryoEarnings.containsKey(secondId) ? (Integer)ryoEarnings.get(secondId) : 0;
         String message = "§6[Tournament] §a§lComplete!\n§6  1st: §e" + firstName + " §a(" + firstRyo + " Ryo)\n§6  2nd: §e" + secondName + " §a(" + secondRyo + " Ryo)";
         if (thirdNames.length() > 0) {
            StringBuilder thirdRyoStr = new StringBuilder();

            for(int i = 0; i < thirdIds.size(); ++i) {
               if (i > 0) {
                  thirdRyoStr.append(", ");
               }

               String name = this.getPlayerNameFromBracket(bracket, (UUID)thirdIds.get(i));
               int ryo = ryoEarnings.containsKey(thirdIds.get(i)) ? (Integer)ryoEarnings.get(thirdIds.get(i)) : 0;
               thirdRyoStr.append(name).append(" (").append(ryo).append(" Ryo)");
            }

            message = message + "\n§6  3rd: §e" + thirdRyoStr;
         }

         this.broadcastToVillage(inst.getVillageName(), message, world);
         this.tournaments.remove(inst.getVillageName());
         this.bracketReadyTimestamps.remove(inst.getVillageName());
         this.saveTournament(inst.getVillageName(), world);
      }
   }

   private Map<UUID, Integer> distributeRyoRewards(TournamentInstance inst, World world) {
      TournamentBracket bracket = inst.getBracket();
      Map<UUID, Integer> ryoEarnings = new HashMap();
      if (bracket == null) {
         return ryoEarnings;
      } else {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         PvpSavedData pvpData = PvpSavedData.get(world);
         Set<UUID> allParticipants = new HashSet(inst.getParticipants());
         Map<UUID, Integer> rawRyoSum = new HashMap();

         for(TournamentMatch match : bracket.getMatches()) {
            if (!match.isBye() && match.getMatchState() == TournamentMatch.MatchState.FINISHED) {
               UUID winnerId = match.getWinnerId();
               if (winnerId != null) {
                  int roundRyo = 500 + match.getRound() * 250;
                  rawRyoSum.put(winnerId, (Integer)rawRyoSum.getOrDefault(winnerId, 0) + roundRyo);
               }
            }
         }

         double sizeMultiplier = this.getSizeMultiplier(inst.getParticipantCount());

         for(UUID participantId : allParticipants) {
            int rawSum = (Integer)rawRyoSum.getOrDefault(participantId, 0);
            int finalRyo;
            if (rawSum == 0) {
               finalRyo = (int)((double)300.0F * sizeMultiplier);
            } else {
               finalRyo = (int)((double)rawSum * sizeMultiplier);
            }

            ryoEarnings.put(participantId, finalRyo);
         }

         UUID firstId = bracket.getWinnerId();
         UUID secondId = bracket.getRunnerUpId();
         List<UUID> thirdIds = bracket.getThirdPlaceIds();
         Set<UUID> thirdSet = new HashSet(thirdIds);
         Map<UUID, Integer> eliminationRound = new HashMap();

         for(TournamentMatch match : bracket.getMatches()) {
            if (!match.isBye() && match.getMatchState() == TournamentMatch.MatchState.FINISHED) {
               UUID winnerId = match.getWinnerId();
               if (winnerId != null) {
                  UUID loserId;
                  if (winnerId.equals(match.getPlayer1Id())) {
                     loserId = match.getPlayer2Id();
                  } else {
                     loserId = match.getPlayer1Id();
                  }

                  if (loserId != null) {
                     eliminationRound.put(loserId, match.getRound());
                  }
               }
            }
         }

         for(UUID participantId : allParticipants) {
            int ryo = (Integer)ryoEarnings.getOrDefault(participantId, 0);
            EntityPlayerMP player = server != null ? server.getPlayerList().getPlayerByUUID(participantId) : null;
            String placementMsg;
            if (participantId.equals(firstId)) {
               placementMsg = "§6[Tournament] §aYou placed §e1st §aand earned §e" + ryo + " Ryo!";
            } else if (participantId.equals(secondId)) {
               placementMsg = "§6[Tournament] §aYou placed §e2nd §aand earned §e" + ryo + " Ryo!";
            } else if (thirdSet.contains(participantId)) {
               placementMsg = "§6[Tournament] §aYou placed §e3rd §aand earned §e" + ryo + " Ryo!";
            } else {
               int elimRound = (Integer)eliminationRound.getOrDefault(participantId, 0);
               placementMsg = "§6[Tournament] §7You were eliminated in Round " + (elimRound + 1) + ". §aEarned §e" + ryo + " Ryo.";
            }

            if (player != null) {
               RyoRewardHelper.grantRyo(player, ryo);
               player.sendMessage(new TextComponentString(placementMsg));
            } else {
               int existing = pvpData.getPendingTournamentRyo(participantId);
               pvpData.setPendingTournamentRyo(participantId, existing + ryo);
            }
         }

         return ryoEarnings;
      }
   }

   private double getSizeMultiplier(int participantCount) {
      if (participantCount >= 33) {
         return 2.8;
      } else if (participantCount >= 17) {
         return 2.2;
      } else if (participantCount >= 9) {
         return 1.7;
      } else {
         return participantCount >= 6 ? 1.3 : (double)1.0F;
      }
   }

   private String getPlayerNameFromBracket(TournamentBracket bracket, @Nullable UUID playerId) {
      if (playerId == null) {
         return "???";
      } else {
         for(TournamentMatch match : bracket.getMatches()) {
            if (playerId.equals(match.getPlayer1Id())) {
               return match.getPlayer1Name();
            }

            if (playerId.equals(match.getPlayer2Id())) {
               return match.getPlayer2Name();
            }
         }

         return "???";
      }
   }

   private void teleportPlayerBack(@Nullable UUID playerId, MinecraftServer server) {
      if (playerId != null) {
         double[] pos = (double[])this.preTournamentPositions.remove(playerId);
         if (pos != null) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
            if (player != null) {
               player.connection.setPlayerLocation(pos[0], pos[1], pos[2], 0.0F, 0.0F);
               player.setPositionAndUpdate(pos[0], pos[1], pos[2]);
            }

         }
      }
   }

   private void teleportActivePlayersBack(TournamentInstance inst, World world) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         TournamentBracket bracket = inst.getBracket();
         if (bracket != null) {
            for(TournamentMatch match : bracket.getMatches()) {
               if (match.getMatchState() == TournamentMatch.MatchState.ACTIVE || match.getMatchState() == TournamentMatch.MatchState.COUNTDOWN) {
                  UUID p1 = match.getPlayer1Id();
                  UUID p2 = match.getPlayer2Id();
                  if (p1 != null) {
                     this.activeMatchPlayers.remove(p1);
                     this.teleportPlayerBack(p1, server);
                  }

                  if (p2 != null) {
                     this.activeMatchPlayers.remove(p2);
                     this.teleportPlayerBack(p2, server);
                  }

                  this.matchTimestamps.remove(match.getMatchIndex());
               }
            }

         }
      }
   }

   @Nullable
   public TournamentInstance getTournamentForVillage(String villageName) {
      return (TournamentInstance)this.tournaments.get(villageName);
   }

   public boolean isPlayerInTournamentMatch(UUID playerId) {
      return this.activeMatchPlayers.contains(playerId);
   }

   public Map<String, TournamentInstance> getAllTournaments() {
      return new HashMap(this.tournaments);
   }

   public void saveTournament(String villageName, World world) {
      PvpSavedData data = PvpSavedData.get(world);
      TournamentInstance inst = (TournamentInstance)this.tournaments.get(villageName);
      if (inst != null) {
         data.setTournament(villageName, inst);
      } else {
         data.removeTournament(villageName);
      }

   }

   public void loadTournaments(World world) {
      PvpSavedData data = PvpSavedData.get(world);
      Map<String, TournamentInstance> saved = data.getAllTournaments();
      this.tournaments.clear();

      for(Map.Entry<String, TournamentInstance> entry : saved.entrySet()) {
         TournamentInstance inst = (TournamentInstance)entry.getValue();
         if (inst.getState() != TournamentState.COMPLETE && inst.getState() != TournamentState.CANCELLED) {
            this.tournaments.put(entry.getKey(), inst);
            if (inst.getState() == TournamentState.BRACKET_READY) {
               this.bracketReadyTimestamps.put(entry.getKey(), System.currentTimeMillis());
            }
         }
      }

      this.loadCooldowns(data);
      System.out.println("[TournamentSystem] Loaded " + this.tournaments.size() + " active tournaments.");
   }

   private void saveCooldowns(World world) {
      PvpSavedData data = PvpSavedData.get(world);
      long now = System.currentTimeMillis();

      for(Map.Entry<UUID, Long> entry : this.tournamentCooldowns.entrySet()) {
         if ((Long)entry.getValue() > now) {
            data.setTournamentCooldown((UUID)entry.getKey(), (Long)entry.getValue());
         }
      }

      data.markDirty();
   }

   private void loadCooldowns(PvpSavedData data) {
      this.tournamentCooldowns.clear();
      long now = System.currentTimeMillis();
      Map<UUID, Long> saved = data.getAllTournamentCooldowns();

      for(Map.Entry<UUID, Long> entry : saved.entrySet()) {
         if ((Long)entry.getValue() > now) {
            this.tournamentCooldowns.put(entry.getKey(), entry.getValue());
         }
      }

   }

   private void sendTitle(EntityPlayerMP player, String title, String subtitle) {
      SPacketTitle timesPacket = new SPacketTitle(5, 20, 5);
      SPacketTitle subtitlePacket = new SPacketTitle(Type.SUBTITLE, new TextComponentString(subtitle));
      SPacketTitle titlePacket = new SPacketTitle(Type.TITLE, new TextComponentString(title));
      player.connection.sendPacket(timesPacket);
      player.connection.sendPacket(subtitlePacket);
      player.connection.sendPacket(titlePacket);
   }

   private void sendCountdownNumber(EntityPlayerMP player, int number, String color) {
      if (player.connection != null) {
         player.connection.sendPacket(new SPacketTitle(Type.TIMES, (ITextComponent)null, 0, 20, 10));
         String titleJson = "{\"text\":\"" + number + "\",\"color\":\"" + color + "\",\"bold\":true}";
         ITextComponent titleText = Serializer.jsonToComponent(titleJson);
         player.connection.sendPacket(new SPacketTitle(Type.TITLE, titleText));
      }
   }

   private void sendFightTitle(EntityPlayerMP player) {
      if (player.connection != null) {
         player.connection.sendPacket(new SPacketTitle(Type.TIMES, (ITextComponent)null, 0, 30, 10));
         String titleJson = "{\"text\":\"⚔ FIGHT! ⚔\",\"color\":\"green\",\"bold\":true}";
         ITextComponent titleText = Serializer.jsonToComponent(titleJson);
         player.connection.sendPacket(new SPacketTitle(Type.TITLE, titleText));
      }
   }

   private void broadcastToVillage(String villageName, String message, World world) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         for(EntityPlayerMP p : server.getPlayerList().getPlayers()) {
            VillageHelper.Village v = VillageHelper.getVillage(p);
            if (v.teamName.equals(villageName)) {
               p.sendMessage(new TextComponentString(message));
            }
         }

      }
   }
}
