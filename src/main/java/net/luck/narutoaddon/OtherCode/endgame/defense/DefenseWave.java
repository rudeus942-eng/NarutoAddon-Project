package net.luck.narutoaddon.OtherCode.endgame.defense;

import java.util.Map;

public class DefenseWave {
   private final int waveNumber;
   private final Map<Integer, String[]> enemiesBySpawnPoint;
   private final Map<Integer, int[]> countsBySpawnPoint;
   private final int delayBeforeWaveTicks;

   public DefenseWave(int waveNumber, Map<Integer, String[]> enemiesBySpawnPoint, Map<Integer, int[]> countsBySpawnPoint, int delayBeforeWaveTicks) {
      this.waveNumber = waveNumber;
      this.enemiesBySpawnPoint = enemiesBySpawnPoint;
      this.countsBySpawnPoint = countsBySpawnPoint;
      this.delayBeforeWaveTicks = delayBeforeWaveTicks;
   }

   public int getWaveNumber() {
      return this.waveNumber;
   }

   public Map<Integer, String[]> getEnemiesBySpawnPoint() {
      return this.enemiesBySpawnPoint;
   }

   public Map<Integer, int[]> getCountsBySpawnPoint() {
      return this.countsBySpawnPoint;
   }

   public int getDelayBeforeWaveTicks() {
      return this.delayBeforeWaveTicks;
   }

   public int getTotalEnemyCount() {
      int total = 0;

      for(int[] counts : this.countsBySpawnPoint.values()) {
         for(int c : counts) {
            total += c;
         }
      }

      return total;
   }
}
