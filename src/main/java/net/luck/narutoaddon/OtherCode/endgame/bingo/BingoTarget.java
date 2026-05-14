package net.luck.narutoaddon.OtherCode.endgame.bingo;

import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostDifficultyTier;

import java.util.Random;

public class BingoTarget {
   private final String targetId;
   private final String displayName;
   private final String loreHint;
   private final String npcConfigId;
   private final OutpostDifficultyTier tier;
   private final int regionMinX;
   private final int regionMaxX;
   private final int regionMinZ;
   private final int regionMaxZ;
   private final String[] decoyConfigIds;
   private final String[] ambushConfigIds;
   private final double spawnChance;
   private final double ambushChance;
   private final int ryoMin;
   private final int ryoMax;

   public BingoTarget(String targetId, String displayName, String loreHint, String npcConfigId, OutpostDifficultyTier tier, int regionMinX, int regionMaxX, int regionMinZ, int regionMaxZ, String[] decoyConfigIds, String[] ambushConfigIds, double spawnChance, double ambushChance, int ryoMin, int ryoMax) {
      this.targetId = targetId;
      this.displayName = displayName;
      this.loreHint = loreHint;
      this.npcConfigId = npcConfigId;
      this.tier = tier;
      this.regionMinX = regionMinX;
      this.regionMaxX = regionMaxX;
      this.regionMinZ = regionMinZ;
      this.regionMaxZ = regionMaxZ;
      this.decoyConfigIds = decoyConfigIds;
      this.ambushConfigIds = ambushConfigIds;
      this.spawnChance = spawnChance;
      this.ambushChance = ambushChance;
      this.ryoMin = ryoMin;
      this.ryoMax = ryoMax;
   }

   public String getTargetId() {
      return this.targetId;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getLoreHint() {
      return this.loreHint;
   }

   public String getNpcConfigId() {
      return this.npcConfigId;
   }

   public OutpostDifficultyTier getTier() {
      return this.tier;
   }

   public int getRegionMinX() {
      return this.regionMinX;
   }

   public int getRegionMaxX() {
      return this.regionMaxX;
   }

   public int getRegionMinZ() {
      return this.regionMinZ;
   }

   public int getRegionMaxZ() {
      return this.regionMaxZ;
   }

   public String[] getDecoyConfigIds() {
      return this.decoyConfigIds;
   }

   public String[] getAmbushConfigIds() {
      return this.ambushConfigIds;
   }

   public double getSpawnChance() {
      return this.spawnChance;
   }

   public double getAmbushChance() {
      return this.ambushChance;
   }

   public int getRyoMin() {
      return this.ryoMin;
   }

   public int getRyoMax() {
      return this.ryoMax;
   }

   public int getRandomRyo(Random rand) {
      return this.ryoMin + rand.nextInt(this.ryoMax - this.ryoMin + 1);
   }
}
