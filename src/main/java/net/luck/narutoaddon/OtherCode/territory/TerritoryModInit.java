package net.luck.narutoaddon.OtherCode.territory;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.territory.core.TerritoryConversionPayout;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ElementsInfTsukAddon.ModElement.Tag
public class TerritoryModInit extends ElementsInfTsukAddon.ModElement {
   public static final String NETWORK_CHANNEL = "inftsuk_territory";
   public static SimpleNetworkWrapper NETWORK;
   private static int packetId = 0;

   public TerritoryModInit(ElementsInfTsukAddon instance) {
      super(instance, 9005);
   }

   public void preInit(FMLPreInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new TerritoryConversionPayout());
   }

   public void init(FMLInitializationEvent event) {
   }

   @SideOnly(Side.CLIENT)
   private void initClient() {
   }

   public void serverLoad(FMLServerStartingEvent event) {
   }
}
