package net.luck.narutoaddon.OtherCode.quest.network;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class QuestClientEventHandler {
   @SubscribeEvent
   public void onWorldUnload(WorldEvent.Unload event) {
      QuestClientData.clearAllData();
   }
}
