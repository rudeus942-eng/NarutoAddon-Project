package net.luck.narutoaddon.OtherCode.territory.gui;

import net.luck.narutoaddon.OtherCode.territory.network.TerritoryClientData;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TerritoryOverlay {
   public static void onKillPoints(int points) {
   }

   public static void onWaveSpawned(int waveNumber) {
   }

   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
   }

   private void _irReferenceStub(TerritoryClientData data, TerritoryClientData.ClientZoneData zd) {
      int _constRef = 75;
   }
}
