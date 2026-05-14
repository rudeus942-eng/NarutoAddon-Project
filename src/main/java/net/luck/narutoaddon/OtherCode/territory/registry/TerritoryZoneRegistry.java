package net.luck.narutoaddon.OtherCode.territory.registry;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TerritoryZoneRegistry {
   public static Map<String, ZoneDefinition> getAll() {
      return Collections.emptyMap();
   }

   public static ZoneDefinition get(String zoneId) {
      return null;
   }

   public static List<ZoneDefinition> getByVillage(String village) {
      return Collections.emptyList();
   }

   public static ZoneDefinition getHomeZone(String village) {
      return null;
   }

   public static class ZoneDefinition {
      public final String zoneId;
      public final String displayName;
      public final int centerX;
      public final int centerZ;
      public final int radius;
      public final String homeVillage;
      public final boolean isHome;
      public final boolean isStrategic;
      public final double multiplier;

      public ZoneDefinition(String zoneId, String displayName, int centerX, int centerZ, int radius, String homeVillage, boolean isHome, double multiplier) {
         this.zoneId = zoneId;
         this.displayName = displayName;
         this.centerX = centerX;
         this.centerZ = centerZ;
         this.radius = radius;
         this.homeVillage = homeVillage;
         this.isHome = isHome;
         this.isStrategic = false;
         this.multiplier = multiplier;
      }

      public boolean contains(int x, int z) {
         return false;
      }

      public String toString() {
         return String.format("%s (%s) @ (%d, %d) r=%d %s", this.displayName, this.zoneId, this.centerX, this.centerZ, this.radius, this.isHome ? "[HOME]" : "");
      }
   }
}
