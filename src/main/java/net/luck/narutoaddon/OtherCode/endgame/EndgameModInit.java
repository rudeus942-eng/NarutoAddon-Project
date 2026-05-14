
package net.luck.narutoaddon.OtherCode.endgame;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.endgame.bingo.BingoManager;
import net.luck.narutoaddon.OtherCode.endgame.bingo.BingoTargetRegistry;
import net.luck.narutoaddon.OtherCode.endgame.command.CommandEndgame;
import net.luck.narutoaddon.OtherCode.endgame.command.CommandEndgameAdmin;
import net.luck.narutoaddon.OtherCode.endgame.config.EndgameNpcConfigs;
import net.luck.narutoaddon.OtherCode.endgame.contract.CommandContract;
import net.luck.narutoaddon.OtherCode.endgame.contract.ContractManager;
import net.luck.narutoaddon.OtherCode.endgame.contract.ContractNetworkMessage;
import net.luck.narutoaddon.OtherCode.endgame.contract.ContractSavedData;
import net.luck.narutoaddon.OtherCode.endgame.defense.DefenseDefinition;
import net.luck.narutoaddon.OtherCode.endgame.defense.DefenseManager;
import net.luck.narutoaddon.OtherCode.endgame.gui.EndgameOverlay;
import net.luck.narutoaddon.OtherCode.endgame.incursion.IncursionDefinition;
import net.luck.narutoaddon.OtherCode.endgame.incursion.IncursionManager;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameActionMessage;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameCombatMessage;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameNetworkHelper;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameSyncMessage;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostInstance;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostManager;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

@ElementsInfTsukAddon.ModElement.Tag
public class EndgameModInit extends ElementsInfTsukAddon.ModElement {
   public static final String NETWORK_CHANNEL = "inftsuk_endgame";
   public static SimpleNetworkWrapper NETWORK;
   private static int packetId = 0;

   public EndgameModInit(ElementsInfTsukAddon instance) {
      super(instance, 9004);
   }

   public void preInit(FMLPreInitializationEvent event) {
      NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("inftsuk_endgame");
      NETWORK.registerMessage(EndgameSyncMessage.Handler.class, EndgameSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(EndgameCombatMessage.Handler.class, EndgameCombatMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(EndgameActionMessage.Handler.class, EndgameActionMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(ContractNetworkMessage.SyncMessage.Handler.class, ContractNetworkMessage.SyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(ContractNetworkMessage.ActionMessage.Handler.class, ContractNetworkMessage.ActionMessage.class, packetId++, Side.SERVER);
      MinecraftForge.EVENT_BUS.register(new EndgameEventHandler());
      OutpostRegistry.init();
      EndgameNpcConfigs.init();
      IncursionDefinition.init();
      DefenseDefinition.init();
      BingoTargetRegistry.init();
   }

   public void init(FMLInitializationEvent event) {
      if (event.getSide() == Side.CLIENT) {
         this.initClient();
      }

   }

   @SideOnly(Side.CLIENT)
   private void initClient() {
      MinecraftForge.EVENT_BUS.register(new EndgameOverlay());
   }

   public void serverLoad(FMLServerStartingEvent event) {
      event.registerServerCommand(new CommandEndgame());
      event.registerServerCommand(new CommandEndgameAdmin());
      event.registerServerCommand(new CommandContract());
      OutpostManager.reset();
      BingoManager.reset();
      IncursionManager.reset();
      DefenseManager.reset();
      ContractManager.reset();
      MinecraftServer server = event.getServer();
      if (server != null) {
         World world = server.getWorld(0);
         if (world != null) {
            EndgameSavedData.get(world);
            ContractSavedData.get(world);
            ContractManager.getInstance().maintainPool(world);
            BingoTargetRegistry.init();
            System.out.println("[EndgameSystem] Endgame PvE system initialized.");
         }
      }

   }

   public static class EndgameEventHandler {
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
               OutpostManager.getInstance().onServerTick(this.cachedWorld);
               BingoManager.getInstance().onServerTick(this.cachedWorld);
               IncursionManager.getInstance().onServerTick(this.cachedWorld);
               DefenseManager.getInstance().onServerTick(this.cachedWorld);
               ContractManager.getInstance().onServerTick(this.cachedWorld);
            }

         }
      }

      @SubscribeEvent
      public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            BingoManager.getInstance().onPlayerLogin(player);
            EndgameNetworkHelper.sendFullSync(player);
            OutpostManager.getInstance().onPlayerReconnect(player);
            ContractNetworkMessage.sendSync(player);
         }

      }

      @SubscribeEvent
      public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            OutpostManager.getInstance().onPlayerDisconnect(player);
            BingoManager.getInstance().onPlayerDisconnect(player);
            IncursionManager.getInstance().onPlayerDisconnect(player);
            DefenseManager.getInstance().onPlayerDisconnect(player);
            ContractManager.getInstance().onPlayerLogout(player);
         }

      }

      @SubscribeEvent
      public void onContractHolderDeath(LivingDeathEvent event) {
         Entity entity = event.getEntity();
         if (!entity.world.isRemote) {
            if (entity instanceof EntityPlayerMP) {
               ContractManager.getInstance().onPlayerDeath((EntityPlayerMP)entity);
            }
         }
      }

      @SubscribeEvent
      public void onBingoTargetDeath(LivingDeathEvent event) {
         Entity entity = event.getEntity();
         if (!entity.world.isRemote) {
            NBTTagCompound data = entity.getEntityData();
            if (data.getBoolean("bingoEntity")) {
               if (data.getBoolean("bingoTarget")) {
                  String ownerStr = data.getString("ownerUUID");
                  if (!ownerStr.isEmpty()) {
                     try {
                        UUID ownerUUID = UUID.fromString(ownerStr);
                        BingoManager.getInstance().onTargetKilled(ownerUUID, entity.getUniqueID());
                     } catch (IllegalArgumentException var6) {
                     }

                  }
               }
            }
         }
      }

      @SubscribeEvent
      public void onWorldUnload(WorldEvent.Unload event) {
         World world = event.getWorld();
         if (!world.isRemote) {
            if (world.provider.getDimension() == 0) {
               OutpostManager.getInstance().onServerStopping(world);
            }
         }
      }

      @SubscribeEvent
      public void onLivingHurt(LivingHurtEvent event) {
         Entity target = event.getEntity();
         if (!target.world.isRemote) {
            NBTTagCompound data = target.getEntityData();
            if (data.getBoolean("endgameEntity")) {
               Entity attacker = event.getSource() != null ? event.getSource().getTrueSource() : null;
               if (attacker instanceof EntityPlayerMP) {
                  EntityPlayerMP player = (EntityPlayerMP)attacker;
                  String instanceKey = data.getString("instanceKey");
                  if (!instanceKey.isEmpty()) {
                     OutpostInstance oi = OutpostManager.getInstance().getActiveOutpost(instanceKey);
                     if (oi != null) {
                        oi.onBossDamaged(player.getUniqueID(), (double)event.getAmount());
                     }

                  }
               }
            }
         }
      }

      @SubscribeEvent
      public void onContractKill(LivingDeathEvent event) {
         Entity entity = event.getEntity();
         if (!entity.world.isRemote) {
            Entity attacker = event.getSource() != null ? event.getSource().getTrueSource() : null;
            if (attacker instanceof EntityPlayerMP) {
               EntityPlayerMP killer = (EntityPlayerMP)attacker;
               if (!(entity instanceof EntityPlayerMP)) {
                  NBTTagCompound data = entity.getEntityData();
                  if (!data.getBoolean("inftsuk_contract_vanished")) {
                     String configId = data.getString("npcConfigId");
                     if (configId != null && !configId.isEmpty()) {
                        ContractManager.getInstance().onNpcKilled(killer, configId);
                     }

                  }
               }
            }
         }
      }
   }
}
