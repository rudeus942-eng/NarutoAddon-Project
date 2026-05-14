
package net.luck.narutoaddon.OtherCode.quest.core;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;

public class VillageHelper {
   public static Village getVillage(EntityPlayerMP player) {
      Team team = player.getTeam();
      if (team == null) {
         return Village.UNKNOWN;
      } else {
         String teamName = team.getName();
         if (teamName != null && !teamName.isEmpty()) {
            if (teamName.equalsIgnoreCase("Akatsuki")) {
               return Village.AKATSUKI;
            } else {
               for(Village v : Village.values()) {
                  if (v != Village.UNKNOWN && v != Village.AKATSUKI && (teamName.equalsIgnoreCase(v.teamName) || teamName.equalsIgnoreCase(v.villageName))) {
                     return v;
                  }
               }

               return Village.UNKNOWN;
            }
         } else {
            return Village.UNKNOWN;
         }
      }
   }

   public static String getVillageName(EntityPlayerMP player) {
      return getVillage(player).villageName;
   }

   public static String getLandName(EntityPlayerMP player) {
      return getVillage(player).landName;
   }

   public static String getTerrainName(EntityPlayerMP player) {
      return getVillage(player).terrainName;
   }

   public static boolean isInTerritory(BlockPos pos, Village village) {
      if (village == Village.UNKNOWN) {
         return false;
      } else {
         return pos.getX() >= village.landMinX && pos.getX() <= village.landMaxX && pos.getZ() >= village.landMinZ && pos.getZ() <= village.landMaxZ;
      }
   }

   public static Village getNearestVillage(BlockPos pos) {
      Village nearest = null;
      double nearestDist = Double.MAX_VALUE;

      for(Village v : Village.values()) {
         if (v != Village.UNKNOWN && v != Village.AKATSUKI) {
            double dx = (double)(pos.getX() - v.centerX);
            double dz = (double)(pos.getZ() - v.centerZ);
            double dist = dx * dx + dz * dz;
            if (dist < nearestDist) {
               nearestDist = dist;
               nearest = v;
            }
         }
      }

      return nearest;
   }

   public static double getDistanceToNearestVillageCenter(BlockPos pos) {
      double nearestDistSq = Double.MAX_VALUE;

      for(Village v : Village.values()) {
         if (v != Village.UNKNOWN && v != Village.AKATSUKI) {
            double dx = (double)(pos.getX() - v.centerX);
            double dz = (double)(pos.getZ() - v.centerZ);
            double distSq = dx * dx + dz * dz;
            if (distSq < nearestDistSq) {
               nearestDistSq = distSq;
            }
         }
      }

      return Math.sqrt(nearestDistSq);
   }

   public static boolean isInBorderZone(BlockPos pos, Village village1, Village village2) {
      if (village1 != Village.UNKNOWN && village2 != Village.UNKNOWN && village1 != Village.AKATSUKI && village2 != Village.AKATSUKI) {
         if (village1 == village2) {
            return false;
         } else {
            double midX = (double)(village1.centerX + village2.centerX) / (double)2.0F;
            double midZ = (double)(village1.centerZ + village2.centerZ) / (double)2.0F;
            double dx = (double)pos.getX() - midX;
            double dz = (double)pos.getZ() - midZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            return dist <= (double)500.0F;
         }
      } else {
         return false;
      }
   }

   public static boolean isAkatsuki(EntityPlayerMP player) {
      return getVillage(player) == Village.AKATSUKI;
   }

   public static enum Village {
      LEAF("Leaf", "Konoha", "the Land of Fire", "Fire Country forests", -947, -843, -1600, 800, -1500, 800),
      SAND("Sand", "Sunagakure", "the Land of Wind", "the desert dunes", -2734, 520, -3600, -1800, 0, 2000),
      MIST("Mist", "Kirigakure", "the Land of Water", "the coastlands", 3861, -2155, 3000, 4500, -2800, -1400),
      STONE("Stone", "Iwagakure", "the Land of Earth", "the mountain passes", -2500, -2655, -3200, -1600, -3500, -1800),
      CLOUD("Cloud", "Kumogakure", "the Land of Lightning", "the highlands", 1912, -3066, 800, 3000, -4000, -2200),
      RAIN("Rain", "Amegakure", "the Land of Rain", "the wetlands", -2249, -828, -2700, -1600, -1300, -200),
      AKATSUKI("Akatsuki", "Akatsuki", "the shadows", "the hidden lairs", -2249, -828, -6000, 6000, -6000, 6000),
      UNKNOWN("Unknown", "Unknown", "the surrounding lands", "the area", 0, 0, -6000, 6000, -6000, 6000);

      public final String teamName;
      public final String villageName;
      public final String landName;
      public final String terrainName;
      public final int centerX;
      public final int centerZ;
      public final int landMinX;
      public final int landMaxX;
      public final int landMinZ;
      public final int landMaxZ;

      private Village(String teamName, String villageName, String landName, String terrainName, int centerX, int centerZ, int landMinX, int landMaxX, int landMinZ, int landMaxZ) {
         this.teamName = teamName;
         this.villageName = villageName;
         this.landName = landName;
         this.terrainName = terrainName;
         this.centerX = centerX;
         this.centerZ = centerZ;
         this.landMinX = landMinX;
         this.landMaxX = landMaxX;
         this.landMinZ = landMinZ;
         this.landMaxZ = landMaxZ;
      }
   }
}
