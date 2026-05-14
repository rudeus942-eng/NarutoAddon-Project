package net.luck.narutoaddon.OtherCode;

import net.luck.narutoaddon.OtherCode.command.CommandRankedReward;
import net.luck.narutoaddon.OtherCode.network.*;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;

@ElementsInfTsukAddon.ModElement.Tag
public class RankedModInit extends ElementsInfTsukAddon.ModElement {
   public RankedModInit(ElementsInfTsukAddon instance) {
      super(instance, 1);
   }

   public void preInit(FMLPreInitializationEvent event) {
      this.elements.addNetworkMessage(RankedGuiActionMessage.Handler.class, RankedGuiActionMessage.class, Side.SERVER);
      this.elements.addNetworkMessage(RankedGuiDataMessage.Handler.class, RankedGuiDataMessage.class, Side.CLIENT);
      this.elements.addNetworkMessage(RankedStatsDataMessage.Handler.class, RankedStatsDataMessage.class, Side.CLIENT);
      this.elements.addNetworkMessage(RankedLeaderboardDataMessage.Handler.class, RankedLeaderboardDataMessage.class, Side.CLIENT);
      this.elements.addNetworkMessage(RankedSeasonDataMessage.Handler.class, RankedSeasonDataMessage.class, Side.CLIENT);
      this.elements.addNetworkMessage(RankedTitlesDataMessage.Handler.class, RankedTitlesDataMessage.class, Side.CLIENT);
      this.elements.addNetworkMessage(RankedMatchHudMessage.Handler.class, RankedMatchHudMessage.class, Side.CLIENT);
   }

   public void init(FMLInitializationEvent event) {
      Rankedmain.init();
   }

   public void serverLoad(FMLServerStartingEvent event) {
      event.registerServerCommand(new RankedCommands.CommandRanked());
      event.registerServerCommand(new RankedCommands.CommandRankedTop());
      event.registerServerCommand(new RankedCommands.CommandRankedAdmin());
      event.registerServerCommand(new RankedCommands.CommandClaimRewards());
      event.registerServerCommand(new CommandRankedReward());
      System.out.println("[Ranked] Commands registered successfully!");
   }
}
