package net.luck.narutoaddon.OtherCode.territory.capture;

import net.luck.narutoaddon.OtherCode.territory.core.TerritoryZone;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CaptureLogic {
   private CaptureLogic() {
   }

   public static int processStandingCapture(EntityPlayerMP player, TerritoryZone zone, String attackerVillage) {
      return 0;
   }

   public static int processPvpKill(EntityPlayerMP killer, TerritoryZone zone, String killerVillage) {
      return 0;
   }

   public static int processPveKill(EntityPlayerMP player, TerritoryZone zone, String attackerVillage) {
      return 0;
   }

   public static boolean shouldSpawnDefenders(TerritoryZone zone) {
      return false;
   }

   public static boolean isZoneContested(TerritoryZone zone, MinecraftServer server, String attackerVillage) {
      return false;
   }

   public static double calculateUnderdogMultiplier(int attackerCount, int defenderCount) {
      return (double)1.0F;
   }

   public static void captureComplete(TerritoryZone zone, String newOwner) {
   }
}
