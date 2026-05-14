package net.luck.narutoaddon.OtherCode.territory.zetsu;

import net.minecraft.world.World;

import java.util.Collections;
import java.util.Set;

public class ZetsuManager {
   private static ZetsuManager INSTANCE;

   public static ZetsuManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ZetsuManager();
      }

      return INSTANCE;
   }

   public static void reset() {
      INSTANCE = null;
   }

   public void tick(World world) {
   }

   public void onZetsuDeath(String zoneId) {
   }

   public Set<String> getActiveTargetZoneIds() {
      return Collections.emptySet();
   }
}
