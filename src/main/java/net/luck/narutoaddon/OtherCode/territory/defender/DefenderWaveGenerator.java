package net.luck.narutoaddon.OtherCode.territory.defender;

import net.luck.narutoaddon.OtherCode.territory.core.TerritoryZone;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DefenderWaveGenerator {
   public static int getWaveForProgress(float captureProgress) {
      return 1;
   }

   public static void recordWaveDefeated(String zoneId, int waveNumber, long worldTick) {
   }

   public static boolean isWaveOnCooldown(String zoneId, int waveNumber, long currentTick) {
      return false;
   }

   public static void clearCooldowns(String zoneId) {
   }

   public static List<UUID> spawnWave(World world, TerritoryZone zone, int waveNumber, String defenderVillage) {
      return Collections.emptyList();
   }

   public static List<UUID> spawnWave(World world, TerritoryZone zone, int waveNumber, String defenderVillage, double spawnCenterX, double spawnCenterZ) {
      return Collections.emptyList();
   }

   public static List<UUID> spawnWave(World world, TerritoryZone zone, int waveNumber, String defenderVillage, double spawnCenterX, double spawnCenterZ, int countOverride) {
      return Collections.emptyList();
   }

   public static String[] getConfigsForWave(int waveNumber, String village) {
      return new String[0];
   }

   public static List<UUID> spawnSpecialists(World world, TerritoryZone zone, String defenderVillage, double spawnCenterX, double spawnCenterZ, int count) {
      return Collections.emptyList();
   }

   public static void despawnAll(World world, List<UUID> entityUUIDs) {
   }

   public static int findSafeSpawnY(World world, int x, int z) {
      return 64;
   }
}
