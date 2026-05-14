
package net.luck.narutoaddon.OtherCode.quest.pvp.tournament;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TournamentBracket {
   private int totalSlots;
   private int rounds;
   private final List<TournamentMatch> matches;

   private TournamentBracket(int totalSlots, int rounds) {
      this.totalSlots = totalSlots;
      this.rounds = rounds;
      this.matches = new ArrayList();
   }

   public static TournamentBracket generate(List<UUID> participantIds, List<String> participantNames) {
      int count = participantIds.size();
      if (count <= 0) {
         TournamentBracket bracket = new TournamentBracket(2, 1);
         TournamentMatch finalMatch = new TournamentMatch(0, 0, 0);
         finalMatch.setMatchState(TournamentMatch.MatchState.BYE);
         bracket.matches.add(finalMatch);
         return bracket;
      } else if (count == 1) {
         TournamentBracket bracket = new TournamentBracket(2, 1);
         TournamentMatch finalMatch = new TournamentMatch(0, 0, 0);
         finalMatch.setPlayer1Id((UUID)participantIds.get(0));
         finalMatch.setPlayer1Name((String)participantNames.get(0));
         finalMatch.setMatchState(TournamentMatch.MatchState.BYE);
         finalMatch.setWinnerId((UUID)participantIds.get(0));
         bracket.matches.add(finalMatch);
         return bracket;
      } else {
         int totalSlots = nextPowerOf2(count);
         int rounds = Integer.numberOfTrailingZeros(totalSlots);
         TournamentBracket bracket = new TournamentBracket(totalSlots, rounds);
         int matchIndex = 0;
         int firstRoundMatchCount = totalSlots / 2;

         for(int i = 0; i < firstRoundMatchCount; ++i) {
            TournamentMatch match = new TournamentMatch(matchIndex, 0, i);
            int seed2 = totalSlots - 1 - i;
            if (i < count) {
               match.setPlayer1Id((UUID)participantIds.get(i));
               match.setPlayer1Name((String)participantNames.get(i));
            }

            if (seed2 < count) {
               match.setPlayer2Id((UUID)participantIds.get(seed2));
               match.setPlayer2Name((String)participantNames.get(seed2));
            } else {
               match.setMatchState(TournamentMatch.MatchState.BYE);
               match.setWinnerId(match.getPlayer1Id());
            }

            bracket.matches.add(match);
            ++matchIndex;
         }

         for(int r = 1; r < rounds; ++r) {
            int matchesInRound = totalSlots / (2 << r);

            for(int p = 0; p < matchesInRound; ++p) {
               TournamentMatch match = new TournamentMatch(matchIndex, r, p);
               bracket.matches.add(match);
               ++matchIndex;
            }
         }

         for(int i = 0; i < firstRoundMatchCount; ++i) {
            TournamentMatch match = (TournamentMatch)bracket.matches.get(i);
            if (match.isBye() && match.getWinnerId() != null) {
               int nextIdx = bracket.getNextMatchIndex(i);
               if (nextIdx >= 0 && nextIdx < bracket.matches.size()) {
                  TournamentMatch nextMatch = (TournamentMatch)bracket.matches.get(nextIdx);
                  if (match.getPosition() % 2 == 0) {
                     nextMatch.setPlayer1Id(match.getWinnerId());
                     nextMatch.setPlayer1Name(match.getPlayer1Name());
                  } else {
                     nextMatch.setPlayer2Id(match.getWinnerId());
                     nextMatch.setPlayer2Name(match.getPlayer1Name());
                  }
               }
            }
         }

         return bracket;
      }
   }

   public List<TournamentMatch> getMatchesForRound(int round) {
      List<TournamentMatch> result = new ArrayList();

      for(TournamentMatch match : this.matches) {
         if (match.getRound() == round) {
            result.add(match);
         }
      }

      return result;
   }

   public int getNextMatchIndex(int matchIndex) {
      if (matchIndex >= 0 && matchIndex < this.matches.size()) {
         TournamentMatch match = (TournamentMatch)this.matches.get(matchIndex);
         int currentRound = match.getRound();
         if (currentRound >= this.rounds - 1) {
            return -1;
         } else {
            int currentRoundStart = this.getRoundStartIndex(currentRound);
            int nextRoundStart = this.getRoundStartIndex(currentRound + 1);
            int positionInRound = matchIndex - currentRoundStart;
            return nextRoundStart + positionInRound / 2;
         }
      } else {
         return -1;
      }
   }

   public void advanceWinner(int matchIndex, UUID winnerId, String winnerName) {
      if (matchIndex >= 0 && matchIndex < this.matches.size()) {
         TournamentMatch match = (TournamentMatch)this.matches.get(matchIndex);
         match.setWinnerId(winnerId);
         match.setMatchState(TournamentMatch.MatchState.FINISHED);
         int nextIdx = this.getNextMatchIndex(matchIndex);
         if (nextIdx >= 0 && nextIdx < this.matches.size()) {
            TournamentMatch nextMatch = (TournamentMatch)this.matches.get(nextIdx);
            if (match.getPosition() % 2 == 0) {
               nextMatch.setPlayer1Id(winnerId);
               nextMatch.setPlayer1Name(winnerName);
            } else {
               nextMatch.setPlayer2Id(winnerId);
               nextMatch.setPlayer2Name(winnerName);
            }
         }

      }
   }

   public boolean isRoundComplete(int round) {
      for(TournamentMatch match : this.matches) {
         if (match.getRound() == round && match.getMatchState() != TournamentMatch.MatchState.FINISHED && match.getMatchState() != TournamentMatch.MatchState.BYE) {
            return false;
         }
      }

      return true;
   }

   public boolean isTournamentComplete() {
      if (this.matches.isEmpty()) {
         return false;
      } else {
         TournamentMatch finalMatch = (TournamentMatch)this.matches.get(this.matches.size() - 1);
         return finalMatch.getWinnerId() != null;
      }
   }

   @Nullable
   public UUID getWinnerId() {
      return this.matches.isEmpty() ? null : ((TournamentMatch)this.matches.get(this.matches.size() - 1)).getWinnerId();
   }

   @Nullable
   public UUID getRunnerUpId() {
      if (this.matches.isEmpty()) {
         return null;
      } else {
         TournamentMatch finalMatch = (TournamentMatch)this.matches.get(this.matches.size() - 1);
         if (finalMatch.getWinnerId() == null) {
            return null;
         } else {
            UUID winner = finalMatch.getWinnerId();
            return winner.equals(finalMatch.getPlayer1Id()) ? finalMatch.getPlayer2Id() : finalMatch.getPlayer1Id();
         }
      }
   }

   public List<UUID> getThirdPlaceIds() {
      List<UUID> result = new ArrayList();
      if (this.rounds < 2) {
         return result;
      } else {
         int semifinalRound = this.rounds - 2;

         for(TournamentMatch match : this.getMatchesForRound(semifinalRound)) {
            if (match.getWinnerId() != null) {
               UUID loser;
               if (match.getWinnerId().equals(match.getPlayer1Id())) {
                  loser = match.getPlayer2Id();
               } else {
                  loser = match.getPlayer1Id();
               }

               if (loser != null) {
                  result.add(loser);
               }
            }
         }

         return result;
      }
   }

   private int getRoundStartIndex(int round) {
      int start = 0;
      int matchesInRound = this.totalSlots / 2;

      for(int r = 0; r < round; ++r) {
         start += matchesInRound;
         matchesInRound /= 2;
      }

      return start;
   }

   public static int nextPowerOf2(int n) {
      int v;
      for(v = 1; v < n; v <<= 1) {
      }

      return Math.max(2, v);
   }

   public int getTotalSlots() {
      return this.totalSlots;
   }

   public int getRounds() {
      return this.rounds;
   }

   public List<TournamentMatch> getMatches() {
      return Collections.unmodifiableList(this.matches);
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setInteger("totalSlots", this.totalSlots);
      nbt.setInteger("rounds", this.rounds);
      NBTTagList matchList = new NBTTagList();

      for(TournamentMatch match : this.matches) {
         matchList.appendTag(match.writeToNBT());
      }

      nbt.setTag("matches", matchList);
      return nbt;
   }

   public static TournamentBracket readFromNBT(NBTTagCompound nbt) {
      int totalSlots = nbt.getInteger("totalSlots");
      int rounds = nbt.getInteger("rounds");
      TournamentBracket bracket = new TournamentBracket(totalSlots, rounds);
      NBTTagList matchList = nbt.getTagList("matches", 10);

      for(int i = 0; i < matchList.tagCount(); ++i) {
         bracket.matches.add(TournamentMatch.readFromNBT(matchList.getCompoundTagAt(i)));
      }

      return bracket;
   }
}
