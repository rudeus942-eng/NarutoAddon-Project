package net.luck.narutoaddon.OtherCode.territory.core;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

public class TerritoryMapGenerator {
   public static final int MAP_SIZE = 1500;
   public static final int BLOCKS_PER_PIXEL = 8;
   public static final int WORLD_MIN = -6000;
   public static final int WORLD_MAX = 6000;

   public static void startGeneration(World world, EntityPlayerMP sender) {
   }

   public static boolean tickGeneration(World world) {
      return false;
   }

   public static boolean isGenerating() {
      return false;
   }
}
