package net.luck.narutoaddon.OtherCode.quest.pvp.war;

public enum WarMode {
   SKIRMISH("Skirmish", 43200000L, 0, false),
   DIVISION("10v10", 480000L, 3, true),
   DOMINATION("Domination", 2700000L, 0, true),
   RUSH("Rush", 3600000L, 0, true),
   FOREST_OF_DEATH("Forest of Death", 3600000L, 0, true),
   INFILTRATION("Infiltration", 900000L, 0, true);

   public final String displayName;
   public final long durationMs;
   public final int bestOfRounds;
   public final boolean requiresLobby;

   private WarMode(String displayName, long durationMs, int bestOfRounds, boolean requiresLobby) {
      this.displayName = displayName;
      this.durationMs = durationMs;
      this.bestOfRounds = bestOfRounds;
      this.requiresLobby = requiresLobby;
   }

   public boolean isAkatsukiOnly() {
      return this == INFILTRATION;
   }

   public static WarMode fromOrdinal(int ordinal) {
      WarMode[] values = values();
      return ordinal >= 0 && ordinal < values.length ? values[ordinal] : SKIRMISH;
   }
}
