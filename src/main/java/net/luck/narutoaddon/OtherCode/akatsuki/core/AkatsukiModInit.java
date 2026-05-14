package net.luck.narutoaddon.OtherCode.akatsuki.core;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.akatsuki.command.CommandAkatsuki;
import net.luck.narutoaddon.OtherCode.akatsuki.command.CommandAkatsukiAdmin;
import net.luck.narutoaddon.OtherCode.akatsuki.contract.ContractActionMessage;
import net.luck.narutoaddon.OtherCode.akatsuki.contract.ContractManager;
import net.luck.narutoaddon.OtherCode.akatsuki.contract.ContractSyncMessage;
import net.luck.narutoaddon.OtherCode.akatsuki.event.AkatsukiEventHandler;
import net.luck.narutoaddon.OtherCode.akatsuki.event.PartnerBuffHandler;
import net.luck.narutoaddon.OtherCode.akatsuki.mission.*;
import net.luck.narutoaddon.OtherCode.akatsuki.network.AkatsukiInviteResponseMessage;
import net.luck.narutoaddon.OtherCode.akatsuki.network.AkatsukiRingUpgradeMessage;
import net.luck.narutoaddon.OtherCode.akatsuki.network.AkatsukiSyncMessage;
import net.luck.narutoaddon.OtherCode.akatsuki.network.LeaderActionMessage;
import net.luck.narutoaddon.OtherCode.akatsuki.raid.VillageRaidEventHandler;
import net.luck.narutoaddon.OtherCode.akatsuki.raid.VillageRaidManager;
import net.luck.narutoaddon.OtherCode.akatsuki.raid.VillageRaidSyncMessage;
import net.luck.narutoaddon.OtherCode.endgame.EndgameModInit;
import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.endgame.PveRank;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameCombatMessage;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointSpawnLogic;
import net.luck.narutoaddon.OtherCode.stat.core.StatManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

@ElementsInfTsukAddon.ModElement.Tag
public class AkatsukiModInit extends ElementsInfTsukAddon.ModElement {
   public static final String NETWORK_CHANNEL = "inftsuk_akatsuki";
   public static SimpleNetworkWrapper NETWORK;
   private static int packetId = 0;

   public AkatsukiModInit(ElementsInfTsukAddon instance) {
      super(instance, 9006);
   }

   public void preInit(FMLPreInitializationEvent event) {
      NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("inftsuk_akatsuki");
      NETWORK.registerMessage(AkatsukiSyncMessage.Handler.class, AkatsukiSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(AkatsukiInviteResponseMessage.Handler.class, AkatsukiInviteResponseMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(AkatsukiRingUpgradeMessage.Handler.class, AkatsukiRingUpgradeMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(AkatsukiMissionSyncMessage.Handler.class, AkatsukiMissionSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(LeaderActionMessage.Handler.class, LeaderActionMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(ContractSyncMessage.Handler.class, ContractSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(ContractActionMessage.Handler.class, ContractActionMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(VillageRaidSyncMessage.Handler.class, VillageRaidSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(AkatsukiMissionActionMessage.Handler.class, AkatsukiMissionActionMessage.class, packetId++, Side.SERVER);
      MinecraftForge.EVENT_BUS.register(new AkatsukiEventHandler());
      MinecraftForge.EVENT_BUS.register(new RingBuffHandler());
      MinecraftForge.EVENT_BUS.register(new PartnerBuffHandler());
      MinecraftForge.EVENT_BUS.register(new VillageRaidEventHandler());
      MinecraftForge.EVENT_BUS.register(new PlayerLoginHandler());
      MinecraftForge.EVENT_BUS.register(new MissionTickHandler());
      MinecraftForge.EVENT_BUS.register(new MissionDeathHandler());
      AkatsukiMissionNpcConfigs.init();
   }

   public void serverLoad(FMLServerStartingEvent event) {
      event.registerServerCommand(new CommandAkatsuki());
      event.registerServerCommand(new CommandAkatsukiAdmin());
      AkatsukiSavedData data = AkatsukiSavedData.get(event.getServer().getWorld(0));
      AkatsukiMissionManager.getInstance().loadMissions(data);
      ContractManager.getInstance().loadContracts(data);
      VillageRaidManager.getInstance().loadRaid(data);
   }

   public static class PlayerLoginHandler {
      @SubscribeEvent
      public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            AkatsukiManager.getInstance().onPlayerLogin((EntityPlayerMP)event.player);
            ContractManager.getInstance().onPlayerLogin((EntityPlayerMP)event.player);
            ContractManager.getInstance().cleanExpired();
            ContractManager.getInstance().syncToClient((EntityPlayerMP)event.player);
            VillageRaidManager.getInstance().syncToPlayer((EntityPlayerMP)event.player);
            AkatsukiMissionManager.getInstance().syncToClient((EntityPlayerMP)event.player);
         }

      }

      @SubscribeEvent
      public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            UUID playerId = player.getUniqueID();
            AkatsukiMissionManager.getInstance().onPlayerLogout(playerId);
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               AkatsukiSavedData data = AkatsukiSavedData.get(server.getWorld(0));
               if (data != null) {
                  LeaderMission mission = data.getLeaderMission(playerId);
                  if (mission != null && !mission.isCompleted() && (mission.getMissionState() == 1 || mission.getMissionState() == 2)) {
                     World world = server.getWorld(0);
                     if (world instanceof WorldServer) {
                        WorldServer ws = (WorldServer)world;

                        for(UUID entityUUID : mission.getSpawnedEntityUUIDs()) {
                           Entity entity = ws.getEntityFromUuid(entityUUID);
                           if (entity != null) {
                              entity.setDead();
                           }
                        }
                     }

                     mission.getSpawnedEntityUUIDs().clear();
                     mission.setMissionState(0);
                     mission.setKillsAchieved(0);
                     mission.setCountdownTicks(0);
                     data.markDirty();
                  }
               }
            }
         }

      }
   }

   public static class MissionTickHandler {
      private int tickCounter = 0;

      @SubscribeEvent
      public void onServerTick(TickEvent.ServerTickEvent event) {
         if (event.phase == Phase.END) {
            ++this.tickCounter;
            if (this.tickCounter >= 20) {
               this.tickCounter = 0;
               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               if (server != null) {
                  for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                     if (AkatsukiManager.getInstance().isAkatsuki(player.getUniqueID())) {
                        AkatsukiMissionManager.getInstance().tickPlayer(player);
                        this.tickLeaderMission(player, server);
                     }
                  }

               }
            }
         }
      }

      private void tickLeaderMission(EntityPlayerMP player, MinecraftServer server) {
         AkatsukiSavedData data = AkatsukiSavedData.get(server.getWorld(0));
         if (data != null) {
            LeaderMission mission = data.getLeaderMission(player.getUniqueID());
            if (mission != null && !mission.isCompleted()) {
               double dx = player.posX - (double)mission.getTargetPos().getX();
               double dz = player.posZ - (double)mission.getTargetPos().getZ();
               double distSq = dx * dx + dz * dz;
               int state = mission.getMissionState();
               switch (state) {
                  case 0:
                     if (distSq <= (double)2500.0F) {
                        mission.setMissionState(1);
                        mission.setCountdownTicks(0);
                        this.sendLeaderTitle(player, TextFormatting.YELLOW + "5", mission.getType());
                        data.markDirty();
                     }
                     break;
                  case 1:
                     mission.incrementCountdownTicks();
                     int ticks = mission.getCountdownTicks();
                     switch (ticks) {
                        case 1:
                           this.sendLeaderTitle(player, TextFormatting.YELLOW + "4", "");
                           break;
                        case 2:
                           this.sendLeaderTitle(player, TextFormatting.GOLD + "3", "");
                           break;
                        case 3:
                           this.sendLeaderTitle(player, TextFormatting.RED + "2", "");
                           break;
                        case 4:
                           this.sendLeaderTitle(player, TextFormatting.DARK_RED + "1", "");
                     }

                     if (ticks >= 5) {
                        this.sendLeaderTitle(player, TextFormatting.RED + "" + TextFormatting.BOLD + "FIGHT!", "");
                        this.spawnLeaderMissionNPCs(player, mission, server);
                        mission.setMissionState(2);
                     }

                     data.markDirty();
                     break;
                  case 2:
                     if (distSq > (double)14400.0F) {
                        mission.setMissionState(0);
                        mission.setKillsAchieved(0);
                        mission.setCountdownTicks(0);
                        this.cleanupLeaderMissionEntities(mission, server);
                        this.sendLeaderTitle(player, TextFormatting.RED + "Mission Failed", TextFormatting.GRAY + "You left the combat area.");
                        this.clearLeaderCombatBars(player);
                        data.markDirty();
                     } else {
                        this.syncLeaderHealthBars(player, mission);
                        if (mission.allEnemiesDefeated()) {
                           this.completeLeaderMission(player, mission, data, server);
                        }
                     }
                  case 3:
               }

            }
         }
      }

      private void spawnLeaderMissionNPCs(EntityPlayerMP player, LeaderMission mission, MinecraftServer server) {
         String[] configIds = mission.getNpcConfigIds();
         if (configIds != null && configIds.length != 0) {
            mission.setKillsRequired(configIds.length);
            BlockPos basePos = mission.getTargetPos();
            World world = player.world;

            for(int i = 0; i < configIds.length; ++i) {
               String configId = configIds[i];
               NpcConfig config = NpcConfigRegistry.get(configId);
               if (config == null) {
                  System.out.println("[LeaderMission] Unknown NPC config: " + configId);
                  mission.incrementKills();
                  player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "Target spawn failed — skipping."));
               } else {
                  Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(config.getEntityRegistryId()), world);
                  if (entity == null) {
                     System.out.println("[LeaderMission] Failed to create entity: " + config.getEntityRegistryId());
                     mission.incrementKills();
                     player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "Target spawn failed — skipping."));
                  } else {
                     double angle = (Math.PI * 2D) * (double)i / (double)configIds.length;
                     double radius = configIds.length > 1 ? (double)4.0F : (double)0.0F;
                     int spawnX = basePos.getX() + (int)(Math.cos(angle) * radius);
                     int spawnZ = basePos.getZ() + (int)(Math.sin(angle) * radius);
                     int spawnY = WaypointSpawnLogic.findGroundY(world, spawnX, spawnZ);
                     entity.setLocationAndAngles((double)spawnX + (double)0.5F, (double)spawnY, (double)spawnZ + (double)0.5F, world.rand.nextFloat() * 360.0F, 0.0F);
                     NBTTagCompound entityData = entity.getEntityData();
                     entityData.setBoolean("leaderMissionEntity", true);
                     entityData.setString("missionOwnerId", mission.getAssignedTo().toString());
                     if (entity instanceof INpcConfigurable) {
                        ((INpcConfigurable)entity).applyNpcConfig(config);
                     }

                     entity.setCustomNameTag(TextFormatting.RED + config.getDisplayName());
                     entity.setAlwaysRenderNameTag(true);
                     if (entity instanceof EntityLiving) {
                        ((EntityLiving)entity).enablePersistence();
                     }

                     world.spawnEntity(entity);
                     mission.addSpawnedEntity(entity.getUniqueID());
                  }
               }
            }

         } else {
            mission.setKillsRequired(0);
         }
      }

      private void syncLeaderHealthBars(EntityPlayerMP player, LeaderMission mission) {
         World world = player.world;
         if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer)world;

            for(UUID entityUUID : mission.getSpawnedEntityUUIDs()) {
               Entity entity = ws.getEntityFromUuid(entityUUID);
               if (entity != null && entity.isEntityAlive() && entity instanceof EntityLivingBase) {
                  EntityLivingBase boss = (EntityLivingBase)entity;
                  String bossName = entity.getName();
                  int themeColor = -3407872;
                  int accentColor = -10092544;
                  if (entity instanceof INpcConfigurable) {
                     String cfgId = ((INpcConfigurable)entity).getNpcConfigId();
                     if (cfgId != null && !cfgId.isEmpty()) {
                        NpcConfig cfg = NpcConfigRegistry.get(cfgId);
                        if (cfg != null) {
                           bossName = cfg.getDisplayName();
                           if (cfg.hasHealthBarColors()) {
                              themeColor = cfg.getThemeColor();
                              accentColor = cfg.getAccentColor();
                           }
                        }
                     }
                  }

                  EndgameCombatMessage msg = new EndgameCombatMessage(entity.getEntityId(), bossName, boss.getHealth(), boss.getMaxHealth(), 0, (byte)0, themeColor, accentColor);
                  EndgameModInit.NETWORK.sendTo(msg, player);
               }
            }

         }
      }

      private void clearLeaderCombatBars(EntityPlayerMP player) {
         try {
            EndgameCombatMessage clearMsg = EndgameCombatMessage.clearAll();
            EndgameModInit.NETWORK.sendTo(clearMsg, player);
         } catch (Exception var3) {
         }

      }

      private void cleanupLeaderMissionEntities(LeaderMission mission, MinecraftServer server) {
         World world = server.getWorld(0);
         if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer)world;

            for(UUID entityUUID : mission.getSpawnedEntityUUIDs()) {
               Entity entity = ws.getEntityFromUuid(entityUUID);
               if (entity != null) {
                  entity.setDead();
               }
            }

            mission.getSpawnedEntityUUIDs().clear();
         }
      }

      private void completeLeaderMission(EntityPlayerMP player, LeaderMission mission, AkatsukiSavedData data, MinecraftServer server) {
         UUID playerId = player.getUniqueID();
         mission.setCompleted(true);
         mission.setMissionState(3);
         this.clearLeaderCombatBars(player);
         this.sendLeaderTitle(player, TextFormatting.GREEN + "" + TextFormatting.BOLD + "VICTORY!", TextFormatting.GOLD + "Pain's Order complete!");
         AkatsukiManager mgr = AkatsukiManager.getInstance();
         mgr.addBountyTokens(playerId, mission.getTokenReward());
         mgr.addReputation(playerId, mission.getRepReward(), "Leader mission: " + mission.getType());
         int ryoReward = mission.getTokenReward() * 30;
         RyoRewardHelper.grantRyo(player, ryoReward);
         int pveXp = mission.getTokenReward() * 2;

         try {
            EndgameSavedData savedData = EndgameSavedData.get(player.world);
            PveRank rankBefore = savedData.getPveRank(playerId);
            savedData.addPveXp(playerId, pveXp);
            PveRank rankAfter = savedData.getPveRank(playerId);
            player.sendMessage(new TextComponentString(TextFormatting.AQUA + "+" + pveXp + " PvE XP"));
            if (rankAfter != rankBefore) {
               player.sendMessage(new TextComponentString(TextFormatting.GOLD + "" + TextFormatting.BOLD + "★ PvE RANK UP! " + TextFormatting.RESET + TextFormatting.YELLOW + rankBefore.getDisplayName() + " → " + rankAfter.getDisplayName()));
            }
         } catch (Exception var13) {
         }

         try {
            StatManager.getInstance().grantSP(player, 1);
            player.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "+1 SP"));
         } catch (Exception var12) {
         }

         QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "leader_mission");
         this.cleanupLeaderMissionEntities(mission, server);
         data.removeLeaderMission(playerId);
         data.markDirty();
         player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GREEN + "Leader mission complete! " + TextFormatting.GRAY + "(+" + mission.getTokenReward() + " tokens, +" + mission.getRepReward() + " rep, +" + ryoReward + " Ryo, +" + pveXp + " PvE XP)"));
         mgr.syncToClient(player);
      }

      private void sendLeaderTitle(EntityPlayerMP player, String title, String subtitle) {
         SPacketTitle titlePacket = new SPacketTitle(Type.TITLE, new TextComponentString(title), 5, 20, 5);
         player.connection.sendPacket(titlePacket);
         if (subtitle != null && !subtitle.isEmpty()) {
            SPacketTitle subtitlePacket = new SPacketTitle(Type.SUBTITLE, new TextComponentString(subtitle), 5, 20, 5);
            player.connection.sendPacket(subtitlePacket);
         }

      }
   }

   public static class MissionDeathHandler {
      @SubscribeEvent
      public void onLivingDeath(LivingDeathEvent event) {
         Entity entity = event.getEntity();
         if (!entity.world.isRemote) {
            NBTTagCompound entityData = entity.getEntityData();
            if (entityData.getBoolean("akatsukiMissionEntity")) {
               EntityPlayerMP killer = null;
               if (event.getSource() != null) {
                  Entity source = event.getSource().getTrueSource();
                  if (source instanceof EntityPlayerMP) {
                     killer = (EntityPlayerMP)source;
                  }
               }

               AkatsukiMissionManager.getInstance().onEntityDeath(entity, killer);
            }

            if (entityData.getBoolean("leaderMissionEntity")) {
               String ownerIdStr = entityData.getString("missionOwnerId");
               if (ownerIdStr.isEmpty()) {
                  return;
               }

               UUID ownerId;
               try {
                  ownerId = UUID.fromString(ownerIdStr);
               } catch (IllegalArgumentException var10) {
                  return;
               }

               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               if (server == null) {
                  return;
               }

               AkatsukiSavedData data = AkatsukiSavedData.get(server.getWorld(0));
               if (data == null) {
                  return;
               }

               LeaderMission mission = data.getLeaderMission(ownerId);
               if (mission == null || mission.isCompleted() || mission.getMissionState() != 2) {
                  return;
               }

               if (mission.getSpawnedEntityUUIDs().contains(entity.getUniqueID())) {
                  mission.incrementKills();
                  data.markDirty();
                  EntityPlayerMP owner = server.getPlayerList().getPlayerByUUID(ownerId);
                  if (owner != null) {
                     owner.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "Target eliminated " + TextFormatting.WHITE + mission.getKillsAchieved() + "/" + mission.getKillsRequired()));
                  }
               }
            }

         }
      }
   }
}
