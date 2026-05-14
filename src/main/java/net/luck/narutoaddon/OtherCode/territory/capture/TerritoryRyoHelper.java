package net.luck.narutoaddon.OtherCode.territory.capture;

import net.luck.narutoaddon.OtherCode.territory.core.TerritorySavedData;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Collections;
import java.util.List;

public class TerritoryRyoHelper {
   private TerritoryRyoHelper() {
   }

   public static double getRyoMultiplier(EntityPlayerMP player) {
      return (double)0.0F;
   }

   public static int applyMultiplier(int baseRyo, EntityPlayerMP player) {
      return baseRyo;
   }

   public static List<String> getVillageRanking(TerritorySavedData savedData) {
      return Collections.emptyList();
   }
}
