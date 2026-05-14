
package net.luck.narutoaddon.OtherCode.raid.core;

import net.luck.narutoaddon.OtherCode.raid.network.RaidClientData;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RaidClientEventHandler {
   @SubscribeEvent
   public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
      RaidClientData.clearAllData();
   }

   @SubscribeEvent
   public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
      RaidClientData.clearAllData();
   }

   @SubscribeEvent
   public void onWorldUnload(WorldEvent.Unload event) {
      if (event.getWorld().isRemote) {
         RaidClientData.clearAllData();
      }

   }
}
