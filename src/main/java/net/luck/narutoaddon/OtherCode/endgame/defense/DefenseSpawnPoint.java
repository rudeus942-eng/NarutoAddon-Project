package net.luck.narutoaddon.OtherCode.endgame.defense;

import net.minecraft.util.math.BlockPos;

public class DefenseSpawnPoint {
   private final String name;
   private final BlockPos position;
   private final float directionTowardCenter;

   public DefenseSpawnPoint(String name, BlockPos position, float directionTowardCenter) {
      this.name = name;
      this.position = position;
      this.directionTowardCenter = directionTowardCenter;
   }

   public String getName() {
      return this.name;
   }

   public BlockPos getPosition() {
      return this.position;
   }

   public float getDirectionTowardCenter() {
      return this.directionTowardCenter;
   }
}
