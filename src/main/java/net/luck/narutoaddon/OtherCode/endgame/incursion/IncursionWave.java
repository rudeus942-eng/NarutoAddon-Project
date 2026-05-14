package net.luck.narutoaddon.OtherCode.endgame.incursion;

public class IncursionWave {
   private final int waveNumber;
   private final String[] enemyConfigIds;
   private final int[] enemyCounts;
   private final int delayBeforeWaveTicks;

   public IncursionWave(int waveNumber, String[] enemyConfigIds, int[] enemyCounts, int delayBeforeWaveTicks) {
      this.waveNumber = waveNumber;
      this.enemyConfigIds = enemyConfigIds;
      this.enemyCounts = enemyCounts;
      this.delayBeforeWaveTicks = delayBeforeWaveTicks;
   }

   public int getWaveNumber() {
      return this.waveNumber;
   }

   public String[] getEnemyConfigIds() {
      return this.enemyConfigIds;
   }

   public int[] getEnemyCounts() {
      return this.enemyCounts;
   }

   public int getDelayBeforeWaveTicks() {
      return this.delayBeforeWaveTicks;
   }

   public int getTotalEnemyCount() {
      int total = 0;

      for(int count : this.enemyCounts) {
         total += count;
      }

      return total;
   }
}
