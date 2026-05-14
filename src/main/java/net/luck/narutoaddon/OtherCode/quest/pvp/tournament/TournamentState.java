package net.luck.narutoaddon.OtherCode.quest.pvp.tournament;

public enum TournamentState {
   SETUP("Setup"),
   SIGNUP("Sign-Up Open"),
   BRACKET_READY("Bracket Ready"),
   IN_PROGRESS("In Progress"),
   COMPLETE("Complete"),
   CANCELLED("Cancelled");

   public final String displayName;

   private TournamentState(String displayName) {
      this.displayName = displayName;
   }

   public static TournamentState fromOrdinal(int ordinal) {
      TournamentState[] values = values();
      return ordinal >= 0 && ordinal < values.length ? values[ordinal] : CANCELLED;
   }
}
