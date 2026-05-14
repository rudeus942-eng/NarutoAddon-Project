
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointSpawnLogic;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class WarZoneGenerator {
   private static final int ZONE_RADIUS = 30;
   private static final int SPIRAL_MAX_ATTEMPTS = 50;
   private static final int SPIRAL_STEP = 16;

   public static List<WarZone> generateDominationZones(VillageHelper.Village v1, VillageHelper.Village v2, World world) {
      List<WarZone> zones = new ArrayList();
      double[] fractions = new double[]{(double)0.25F, (double)0.5F, (double)0.75F};

      for(int i = 0; i < fractions.length; ++i) {
         BlockPos pos = interpolatePosition(v1, v2, fractions[i]);
         pos = validateZonePosition(world, pos);
         zones.add(new WarZone(pos, 30, i));
      }

      return zones;
   }

   public static List<WarZone> generateRushCheckpoints(VillageHelper.Village v1, VillageHelper.Village v2, World world) {
      List<WarZone> zones = new ArrayList();
      double[] fractions = new double[]{0.2, 0.4, 0.6, 0.8};

      for(int i = 0; i < fractions.length; ++i) {
         BlockPos pos = interpolatePosition(v1, v2, fractions[i]);
         pos = validateZonePosition(world, pos);
         zones.add(new WarZone(pos, 30, i));
      }

      return zones;
   }

   private static BlockPos interpolatePosition(VillageHelper.Village v1, VillageHelper.Village v2, double fraction) {
      int x = (int)((double)v1.centerX + (double)(v2.centerX - v1.centerX) * fraction);
      int z = (int)((double)v1.centerZ + (double)(v2.centerZ - v1.centerZ) * fraction);
      return new BlockPos(x, 64, z);
   }

   public static BlockPos validateZonePosition(World world, BlockPos pos) {
      int x = pos.getX();
      int z = pos.getZ();
      x = Math.max(-6000, Math.min(6000, x));
      z = Math.max(-6000, Math.min(6000, z));
      int groundY = WaypointSpawnLogic.findGroundY(world, x, z);
      BlockPos groundPos = new BlockPos(x, groundY, z);
      return !isWaterPosition(world, groundPos) ? groundPos : adjustForTerrain(groundPos, world);
   }

   public static BlockPos adjustForTerrain(BlockPos pos, World world) {
      int centerX = pos.getX();
      int centerZ = pos.getZ();

      for(int radius = 1; radius <= 50; ++radius) {
         int offset = radius * 16;
         int[][] offsets = new int[][]{{offset, 0}, {-offset, 0}, {0, offset}, {0, -offset}, {offset, offset}, {-offset, offset}, {offset, -offset}, {-offset, -offset}};

         for(int[] off : offsets) {
            int testX = centerX + off[0];
            int testZ = centerZ + off[1];
            if (testX >= -6000 && testX <= 6000 && testZ >= -6000 && testZ <= 6000) {
               int testY = WaypointSpawnLogic.findGroundY(world, testX, testZ);
               BlockPos testPos = new BlockPos(testX, testY, testZ);
               if (!isWaterPosition(world, testPos)) {
                  return testPos;
               }
            }
         }
      }

      return pos;
   }

   private static boolean isWaterPosition(World world, BlockPos pos) {
      BlockPos belowPos = pos.down();
      Material material = world.getBlockState(belowPos).getMaterial();
      return material == Material.WATER;
   }
}
