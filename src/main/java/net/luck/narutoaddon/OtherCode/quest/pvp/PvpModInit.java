
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.InfTsukAddon;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.network.*;
import net.luck.narutoaddon.OtherCode.quest.pvp.tournament.TournamentGuiHandler;
import net.luck.narutoaddon.OtherCode.quest.pvp.tournament.TournamentManager;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ElementsInfTsukAddon.ModElement.Tag
public class PvpModInit extends ElementsInfTsukAddon.ModElement {
   public static final String NETWORK_CHANNEL = "inftsuk_pvp";
   public static SimpleNetworkWrapper NETWORK;
   private static int packetId = 0;

   public PvpModInit(ElementsInfTsukAddon instance) {
      super(instance, 9002);
   }

   public void preInit(FMLPreInitializationEvent event) {
      NetworkRegistry.INSTANCE.registerGuiHandler(InfTsukAddon.instance, new CustomGuiHandler());
      NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("inftsuk_pvp");
      NETWORK.registerMessage(PvpSyncMessage.Handler.class, PvpSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(PvpTargetLocationMessage.Handler.class, PvpTargetLocationMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(WarSyncMessage.Handler.class, WarSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(WarZoneSyncMessage.Handler.class, WarZoneSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(PvpActionMessage.Handler.class, PvpActionMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(WarActionMessage.Handler.class, WarActionMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(WarRosterMessage.Handler.class, WarRosterMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(WarRosterPlayersMessage.Handler.class, WarRosterPlayersMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(WarRosterRequestMessage.Handler.class, WarRosterRequestMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(WarRosterSubmitMessage.Handler.class, WarRosterSubmitMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(LeadershipSyncMessage.Handler.class, LeadershipSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(LeadershipActionMessage.Handler.class, LeadershipActionMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(TournamentSyncMessage.Handler.class, TournamentSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(TournamentActionMessage.Handler.class, TournamentActionMessage.class, packetId++, Side.SERVER);
      MinecraftForge.EVENT_BUS.register(new PvpEventHandler());
   }

   public void init(FMLInitializationEvent event) {
      if (event.getSide() == Side.CLIENT) {
         this.initClient();
      }

   }

   @SideOnly(Side.CLIENT)
   private void initClient() {
      MinecraftForge.EVENT_BUS.register(new PvpTrackerOverlay());
      MinecraftForge.EVENT_BUS.register(new PvpTargetRenderer());
      MinecraftForge.EVENT_BUS.register(new WarHudOverlay());
      MinecraftForge.EVENT_BUS.register(new WarZoneRenderer());
   }

   public void serverLoad(FMLServerStartingEvent event) {
      event.registerServerCommand(new CommandPvpAdmin());
      PvpManager.reset();
      WarManager.reset();
      TournamentManager.reset();
      MinecraftServer server = event.getServer();
      if (server != null) {
         World world = server.getWorld(0);
         if (world != null) {
            PvpSavedData data = PvpSavedData.get(world);
            TournamentManager.getInstance().loadTournaments(world);

            try {
               NBTTagCompound bingoNBT = data.getBingoBookNBT();
               if (bingoNBT != null && bingoNBT.getSize() > 0) {
                  BingoBook.getInstance().readFromNBT(bingoNBT);
                  System.out.println("[PvPSystem] Bingo Book loaded: " + BingoBook.getInstance().getEntryCount() + " entries.");
               }
            } catch (Exception var6) {
            }

            System.out.println("[PvPSystem] PvP system initialized.");
         }
      }

   }

   public static class CustomGuiHandler implements IGuiHandler {
      private static final TournamentGuiHandler tournamentHandler = new TournamentGuiHandler();

      public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
         Object result = tournamentHandler.getServerGuiElement(id, player, world, x, y, z);
         return result != null ? result : null;
      }

      public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
         Object result = tournamentHandler.getClientGuiElement(id, player, world, x, y, z);
         return result != null ? result : null;
      }
   }

   public static class PvpEventHandler {
      private World cachedWorld = null;

      @SubscribeEvent
      public void onServerTick(TickEvent.ServerTickEvent event) {
         if (event.phase == Phase.END) {
            if (this.cachedWorld == null) {
               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               if (server != null) {
                  this.cachedWorld = server.getWorld(0);
               }
            }

            if (this.cachedWorld != null) {
               PvpManager.getInstance().onServerTick(this.cachedWorld);
               TournamentManager.getInstance().onServerTick(this.cachedWorld);
            }

         }
      }

      @SubscribeEvent
      public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP)event.player;
            LuckPermsSync.syncOnLogin(mp);
            PvpManager.getInstance().onPlayerLogin(mp);
            TournamentManager.getInstance().onPlayerLogin(mp);
         }

      }

      @SubscribeEvent
      public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            PvpManager.getInstance().onPlayerLogout((EntityPlayerMP)event.player);
            TournamentManager.getInstance().onPlayerDisconnect((EntityPlayerMP)event.player, event.player.world);
         }

      }

      @SubscribeEvent
      public void onEntityDeath(LivingDeathEvent event) {
         Entity deadEntity = event.getEntity();
         if (deadEntity != null && !deadEntity.world.isRemote) {
            if (deadEntity instanceof EntityPlayerMP) {
               EntityPlayerMP victim = (EntityPlayerMP)deadEntity;
               if (event.getSource() != null) {
                  Entity trueSource = event.getSource().getTrueSource();
                  if (trueSource instanceof EntityPlayerMP) {
                     EntityPlayerMP killer = (EntityPlayerMP)trueSource;
                     PvpKillTracker.onPlayerKill(killer, victim, event.getSource());
                  }
               }

               PvpManager.getInstance().onPlayerDeath(victim);
               TournamentManager.getInstance().onPlayerDeath(victim, deadEntity.world);
               WarManager warMgr = WarManager.getInstance();
               VillageHelper.Village victimVillage = VillageHelper.getVillage(victim);
               if (victimVillage != null) {
                  WarInstance activeWar = warMgr.getActiveWar(victimVillage);
                  if (activeWar != null && activeWar.getMode() == WarMode.DIVISION) {
                     warMgr.onDivisionPlayerDeath(victim, activeWar, deadEntity.world);
                  }
               }
            }

         }
      }
   }
}
