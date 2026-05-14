package net.luck.narutoaddon.OtherCode.endgame;

import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EndgameLeaderboardHelper {
   public static final String CAT_OUTPOST_CLEARS = "outpostClears";
   public static final String CAT_BINGO_COMPLETIONS = "bingoCompletions";
   public static final String CAT_INCURSION_WAVES = "incursionWaves";
   public static final String CAT_DEFENSE_WAVES = "defenseWaves";

   public static void recordOutpostClear(World world, UUID playerUUID, String outpostId, long clearTimeMs) {
      EndgameSavedData data = EndgameSavedData.get(world);
      data.incrementLeaderboard("outpostClears", playerUUID);
      data.recordFastestClear(playerUUID, outpostId, clearTimeMs);
   }

   public static void recordBingoCompletion(World world, UUID playerUUID) {
      EndgameSavedData data = EndgameSavedData.get(world);
      data.incrementLeaderboard("bingoCompletions", playerUUID);
   }

   public static void recordIncursionWave(World world, UUID playerUUID) {
      EndgameSavedData data = EndgameSavedData.get(world);
      data.incrementLeaderboard("incursionWaves", playerUUID);
   }

   public static void recordDefenseWave(World world, UUID playerUUID) {
      EndgameSavedData data = EndgameSavedData.get(world);
      data.incrementLeaderboard("defenseWaves", playerUUID);
   }

   public static List<Map.Entry<UUID, Integer>> getTopPlayers(World world, String category, int count) {
      return EndgameSavedData.get(world).getTopPlayers(category, count);
   }

   public static String[] getAllCategories() {
      return new String[]{"outpostClears", "bingoCompletions", "incursionWaves", "defenseWaves"};
   }

   public static String getCategoryDisplayName(String category) {
      switch (category) {
         case "outpostClears":
            return "Outpost Clears";
         case "bingoCompletions":
            return "Bingo Completions";
         case "incursionWaves":
            return "Incursion Waves";
         case "defenseWaves":
            return "Defense Waves";
         default:
            return category;
      }
   }
}
