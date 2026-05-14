package net.luck.narutoaddon.OtherCode.stat.core;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.stat.command.CommandStat;
import net.luck.narutoaddon.OtherCode.stat.command.CommandStatAdmin;
import net.luck.narutoaddon.OtherCode.stat.command.CommandWorldBoss;
import net.luck.narutoaddon.OtherCode.stat.network.StatAllocateMessage;
import net.luck.narutoaddon.OtherCode.stat.network.StatSyncMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@ElementsInfTsukAddon.ModElement.Tag
public class StatModInit extends ElementsInfTsukAddon.ModElement {
   public static final String NETWORK_CHANNEL = "inftsuk_stat";
   public static SimpleNetworkWrapper NETWORK;
   private static int packetId = 0;

   public StatModInit(ElementsInfTsukAddon instance) {
      super(instance, 9003);
   }

   public void preInit(FMLPreInitializationEvent event) {
      NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("inftsuk_stat");
      NETWORK.registerMessage(StatSyncMessage.Handler.class, StatSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(StatAllocateMessage.Handler.class, StatAllocateMessage.class, packetId++, Side.SERVER);
      MinecraftForge.EVENT_BUS.register(StatManager.getInstance());
      MinecraftForge.EVENT_BUS.register(new PlayerLoginHandler());
      ChakraBossSpawner.getInstance().register();
      IronBodyRefundLogger.register();
   }

   public void init(FMLInitializationEvent event) {
      WoodBurialImmunityHandler.register();
      if (event.getSide() == Side.CLIENT) {
      }

   }

   public void serverLoad(FMLServerStartingEvent event) {
      event.registerServerCommand(new CommandStat());
      event.registerServerCommand(new CommandStatAdmin());
      event.registerServerCommand(new CommandWorldBoss());
   }

   public static class PlayerLoginHandler {
      @SubscribeEvent
      public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            StatManager.getInstance().onPlayerLogin((EntityPlayerMP)event.player);
         }

      }

      @SubscribeEvent
      public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
      }
   }
}
