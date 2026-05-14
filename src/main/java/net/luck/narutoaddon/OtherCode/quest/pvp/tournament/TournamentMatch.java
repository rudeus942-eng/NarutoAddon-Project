
package net.luck.narutoaddon.OtherCode.quest.pvp.tournament;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.UUID;

public class TournamentMatch {
   private int matchIndex;
   private int round;
   private int position;
   @Nullable
   private UUID player1Id;
   @Nullable
   private UUID player2Id;
   private String player1Name;
   private String player2Name;
   @Nullable
   private UUID winnerId;
   private MatchState matchState;

   public TournamentMatch(int matchIndex, int round, int position) {
      this.matchIndex = matchIndex;
      this.round = round;
      this.position = position;
      this.player1Id = null;
      this.player2Id = null;
      this.player1Name = "";
      this.player2Name = "";
      this.winnerId = null;
      this.matchState = MatchState.PENDING;
   }

   public boolean isBye() {
      return this.matchState == MatchState.BYE;
   }

   public boolean isReady() {
      return this.player1Id != null && this.player2Id != null && this.matchState == MatchState.PENDING;
   }

   public boolean involvesPlayer(@Nullable UUID playerId) {
      if (playerId == null) {
         return false;
      } else {
         return playerId.equals(this.player1Id) || playerId.equals(this.player2Id);
      }
   }

   @Nullable
   public UUID getOpponent(@Nullable UUID playerId) {
      if (playerId == null) {
         return null;
      } else if (playerId.equals(this.player1Id)) {
         return this.player2Id;
      } else {
         return playerId.equals(this.player2Id) ? this.player1Id : null;
      }
   }

   public int getMatchIndex() {
      return this.matchIndex;
   }

   public int getRound() {
      return this.round;
   }

   public int getPosition() {
      return this.position;
   }

   @Nullable
   public UUID getPlayer1Id() {
      return this.player1Id;
   }

   public void setPlayer1Id(@Nullable UUID player1Id) {
      this.player1Id = player1Id;
   }

   @Nullable
   public UUID getPlayer2Id() {
      return this.player2Id;
   }

   public void setPlayer2Id(@Nullable UUID player2Id) {
      this.player2Id = player2Id;
   }

   public String getPlayer1Name() {
      return this.player1Name;
   }

   public void setPlayer1Name(String player1Name) {
      this.player1Name = player1Name;
   }

   public String getPlayer2Name() {
      return this.player2Name;
   }

   public void setPlayer2Name(String player2Name) {
      this.player2Name = player2Name;
   }

   @Nullable
   public UUID getWinnerId() {
      return this.winnerId;
   }

   public void setWinnerId(@Nullable UUID winnerId) {
      this.winnerId = winnerId;
   }

   public MatchState getMatchState() {
      return this.matchState;
   }

   public void setMatchState(MatchState matchState) {
      this.matchState = matchState;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setInteger("matchIndex", this.matchIndex);
      nbt.setInteger("round", this.round);
      nbt.setInteger("position", this.position);
      if (this.player1Id != null) {
         nbt.setString("player1Id", this.player1Id.toString());
      }

      if (this.player2Id != null) {
         nbt.setString("player2Id", this.player2Id.toString());
      }

      nbt.setString("player1Name", this.player1Name);
      nbt.setString("player2Name", this.player2Name);
      if (this.winnerId != null) {
         nbt.setString("winnerId", this.winnerId.toString());
      }

      nbt.setByte("matchState", (byte)this.matchState.ordinal());
      return nbt;
   }

   public static TournamentMatch readFromNBT(NBTTagCompound nbt) {
      int matchIndex = nbt.getInteger("matchIndex");
      int round = nbt.getInteger("round");
      int position = nbt.getInteger("position");
      TournamentMatch match = new TournamentMatch(matchIndex, round, position);
      if (nbt.hasKey("player1Id")) {
         match.player1Id = UUID.fromString(nbt.getString("player1Id"));
      }

      if (nbt.hasKey("player2Id")) {
         match.player2Id = UUID.fromString(nbt.getString("player2Id"));
      }

      match.player1Name = nbt.getString("player1Name");
      match.player2Name = nbt.getString("player2Name");
      if (nbt.hasKey("winnerId")) {
         match.winnerId = UUID.fromString(nbt.getString("winnerId"));
      }

      match.matchState = MatchState.fromOrdinal(nbt.getByte("matchState"));
      return match;
   }

   public String toString() {
      return "Match[" + this.matchIndex + "] R" + this.round + "P" + this.position + " " + this.player1Name + " vs " + this.player2Name + " state=" + this.matchState + (this.winnerId != null ? " winner=" + this.winnerId : "");
   }

   public static enum MatchState {
      PENDING,
      COUNTDOWN,
      ACTIVE,
      FINISHED,
      BYE;

      public static MatchState fromOrdinal(int ordinal) {
         MatchState[] values = values();
         return ordinal >= 0 && ordinal < values.length ? values[ordinal] : PENDING;
      }
   }
}
