
package net.luck.narutoaddon.OtherCode.raid.core;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.raid.boss.BossRegistry;
import net.luck.narutoaddon.OtherCode.raid.command.CommandRaid;
import net.luck.narutoaddon.OtherCode.raid.command.CommandRaidAdmin;
import net.luck.narutoaddon.OtherCode.raid.command.CommandRaidReward;
import net.luck.narutoaddon.OtherCode.raid.gui.RaidBossHealthOverlay;
import net.luck.narutoaddon.OtherCode.raid.integration.NarutoModIntegration;
import net.luck.narutoaddon.OtherCode.raid.network.*;
import net.luck.narutoaddon.OtherCode.raid.party.RaidParty;
import net.luck.narutoaddon.OtherCode.raid.party.RaidPartyStorage;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
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
public class RaidModInit extends ElementsInfTsukAddon.ModElement {
   public static final String NETWORK_CHANNEL = "inftsuk_raid";
   public static SimpleNetworkWrapper NETWORK;
   private static int packetId = 0;

   public RaidModInit(ElementsInfTsukAddon instance) {
      super(instance, 9000);
   }

   public void preInit(FMLPreInitializationEvent event) {
      NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("inftsuk_raid");
      NETWORK.registerMessage(RaidPartyDataMessage.Handler.class, RaidPartyDataMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(RaidBossDataMessage.Handler.class, RaidBossDataMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(RaidMechanicMessage.Handler.class, RaidMechanicMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(RaidQueueStatusMessage.Handler.class, RaidQueueStatusMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(RaidWindowSyncMessage.Handler.class, RaidWindowSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(RaidQueueMessage.Handler.class, RaidQueueMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(PartyActionMessage.Handler.class, PartyActionMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(PartyPlayerListMessage.Handler.class, PartyPlayerListMessage.class, packetId++, Side.CLIENT);
      MinecraftForge.EVENT_BUS.register(new RaidEventHandler());
      BossRegistry.init();
   }

   public void init(FMLInitializationEvent event) {
      if (event.getSide() == Side.CLIENT) {
         this.initClient();
      }

   }

   @SideOnly(Side.CLIENT)
   private void initClient() {
      MinecraftForge.EVENT_BUS.register(new RaidBossHealthOverlay());
      MinecraftForge.EVENT_BUS.register(new RaidClientEventHandler());
   }

   public void serverLoad(FMLServerStartingEvent event) {
      event.registerServerCommand(new CommandRaid());
      event.registerServerCommand(new CommandRaidAdmin());
      event.registerServerCommand(new CommandRaidReward());
      RaidManager.reset();
      MinecraftServer server = event.getServer();
      if (server != null) {
         World world = server.getWorld(0);
         if (world != null) {
            RaidRecoveryData recoveryData = RaidRecoveryData.get(world);
            if (recoveryData.hasOrphanedData()) {
               System.out.println("[RaidRecovery] Orphaned raid data detected - performing startup cleanup...");
               recoveryData.performStartupCleanup(world);
            } else {
               System.out.println("[RaidRecovery] No orphaned raid data found - clean start.");
            }

            RaidManager.getInstance().loadCooldownsFromSavedData(world);
         }
      }

   }

   public static class RaidEventHandler {
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
               RaidManager.getInstance().onServerTick(this.cachedWorld);
               if (this.cachedWorld.getTotalWorldTime() % 20L == 0L) {
                  RaidQueueManager.getInstance().tick();
                  NarutoModIntegration.tickFakePlayerChakra();
               }
            }

         }
      }

      @SubscribeEvent
      public void onPlayerDeath(LivingDeathEvent event) {
         if (event.getEntity() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
            RaidManager.getInstance().onPlayerDeath(player);
         }

      }

      @SubscribeEvent
      public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            RaidManager.getInstance().onPlayerDisconnect(player);
            RaidQueueManager.getInstance().onPlayerDisconnect(player);
            if (!player.world.isRemote) {
               RaidPartyStorage storage = RaidPartyStorage.get(player.world);
               if (storage != null && storage.isInParty(player.getUniqueID())) {
                  RaidParty party = storage.getPlayerParty(player.getUniqueID());
                  if (party != null) {
                     party.removeMemberSilently(player.getUniqueID());
                     storage.updatePlayerMapping(player.getUniqueID(), (UUID)null);
                     if (party.getMemberUUIDs().isEmpty()) {
                        storage.removeParty(party.getPartyId());
                     }
                  } else {
                     storage.updatePlayerMapping(player.getUniqueID(), (UUID)null);
                  }
               }
            }
         }

      }

      @SubscribeEvent
      public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            RaidManager.getInstance().onPlayerLogin(player);
            if (!player.world.isRemote) {
               RaidRecoveryData recoveryData = RaidRecoveryData.get(player.world);
               recoveryData.onPlayerLogin(player);
            }

            RaidQueueManager.getInstance().sendWindowStateTo(player);
         }

      }

      @SubscribeEvent(
         priority = EventPriority.HIGHEST
      )
      public void onLivingAttack(LivingAttackEvent event) {
         if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer target = (EntityPlayer)event.getEntity();
            Entity attacker = event.getSource().getTrueSource();
            if (attacker instanceof EntityPlayer) {
               EntityPlayer attackingPlayer = (EntityPlayer)attacker;
               RaidManager manager = RaidManager.getInstance();
               boolean targetInRaid = manager.isPlayerInRaid(target.getUniqueID());
               boolean attackerInRaid = manager.isPlayerInRaid(attackingPlayer.getUniqueID());
               if (!targetInRaid && !attackerInRaid) {
                  if (!target.world.isRemote) {
                     RaidPartyStorage storage = RaidPartyStorage.get(target.world);
                     RaidParty targetParty = storage.getPlayerParty(target.getUniqueID());
                     if (targetParty != null && targetParty.isMember(attackingPlayer.getUniqueID())) {
                        event.setCanceled(true);
                     }
                  }

               } else {
                  event.setCanceled(true);
               }
            }
         }
      }

      @SubscribeEvent(
         priority = EventPriority.HIGHEST
      )
      public void onCommand(CommandEvent event) {
         ICommandSender sender = event.getSender();
         if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            if (RaidManager.getInstance().isPlayerInRaid(player.getUniqueID())) {
               String commandName = event.getCommand().getName().toLowerCase();
               if (!commandName.equals("raid") && !commandName.equals("raidadmin") && !commandName.equals("raidreward") && !commandName.equals("tell") && !commandName.equals("msg") && !commandName.equals("w") && !commandName.equals("r") && !commandName.equals("reply") && !commandName.equals("feed") && !commandName.equals("bp") && !commandName.equals("vault") && !commandName.equals("ec") && !commandName.equals("suicide") && !commandName.equals("kill")) {
                  event.setCanceled(true);
                  player.sendMessage(new TextComponentString("§cYou cannot use that command while in a raid!"));
               }
            }
         }
      }

      @SubscribeEvent
      public void onChunkLoad(ChunkEvent.Load event) {
         World world = event.getWorld();
         if (!world.isRemote) {
            if (RaidManager.getInstance().getActiveRaidCount() <= 0) {
               ClassInheritanceMultiMap<Entity>[] entityLists = event.getChunk().getEntityLists();
               ClassInheritanceMultiMap[] var4 = entityLists;
               int var5 = entityLists.length;

               for(int var6 = 0; var6 < var5; ++var6) {
                  for(Entity entity : var4[var6]) {
                     if (entity != null && !entity.isDead) {
                        NBTTagCompound entityData = entity.getEntityData();
                        if (entityData.getBoolean("isRaidEntity")) {
                           entity.setDead();
                           System.out.println("[RaidRecovery] Removed orphaned raid entity " + entity.getClass().getSimpleName() + " at " + String.format("%.0f, %.0f, %.0f", entity.posX, entity.posY, entity.posZ));
                        }
                     }
                  }
               }

            }
         }
      }

      @SubscribeEvent(
         priority = EventPriority.HIGHEST
      )
      public void onChat(ServerChatEvent event) {
         EntityPlayerMP player = event.getPlayer();
         if (player != null) {
            if (RaidManager.getInstance().isPlayerInRaid(player.getUniqueID())) {
               String message = event.getMessage();
               if (message != null && message.startsWith("/")) {
                  String cmdLower = message.toLowerCase();
                  if (!cmdLower.startsWith("/raid") && !cmdLower.startsWith("/tell") && !cmdLower.startsWith("/msg") && !cmdLower.startsWith("/w ") && !cmdLower.startsWith("/r ") && !cmdLower.startsWith("/reply") && !cmdLower.startsWith("/feed") && !cmdLower.startsWith("/bp") && !cmdLower.startsWith("/vault") && !cmdLower.startsWith("/ec") && !cmdLower.startsWith("/suicide") && !cmdLower.startsWith("/kill")) {
                     event.setCanceled(true);
                     player.sendMessage(new TextComponentString("§cYou cannot use commands while in a raid!"));
                     return;
                  }

                  return;
               }

               event.setCanceled(true);
               player.sendMessage(new TextComponentString("§cYou cannot use chat while in a raid!"));
            }

         }
      }
   }
}
