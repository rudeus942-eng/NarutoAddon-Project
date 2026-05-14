
package net.luck.narutoaddon.OtherCode.endgame.outpost;

import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameNetworkHelper;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointData;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointSpawnLogic;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OutpostManager {
   private static OutpostManager instance;
   private final Map<String, OutpostInstance> activeOutposts = new ConcurrentHashMap();
   private final Map<UUID, String> playerToInstance = new ConcurrentHashMap();
   private int tickCounter = 0;
   private int saveTickCounter = 0;
   private boolean savedInstancesLoaded = false;
   private static final int SAVE_INTERVAL_TICKS = 1200;

   public static OutpostManager getInstance() {
      if (instance == null) {
         instance = new OutpostManager();
      }

      return instance;
   }

   public static void reset() {
      instance = null;
   }

   public String acceptOutpostMission(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      if (this.isInOutpost(uuid)) {
         return "You already have an active outpost mission.";
      } else {
         OutpostDifficultyTier tier = this.determineTier(player);
         EndgameSavedData data = EndgameSavedData.get(player.getServerWorld());
         List<OutpostDefinition> available = new ArrayList();

         for(OutpostDefinition def : OutpostRegistry.getAll()) {
            String cooldownKey = def.getOutpostId() + "_" + tier.ordinal();
            if (!data.isOutpostOnCooldown(uuid, cooldownKey)) {
               available.add(def);
            }
         }

         if (available.isEmpty()) {
            return "All outpost locations are on cooldown. Try again later.";
         } else {
            Random rand = player.getServerWorld().rand;
            OutpostDefinition pickedLocation = (OutpostDefinition)available.get(rand.nextInt(available.size()));
            List<OutpostRegistry.EncounterGroup> groups = OutpostRegistry.getAllEncounterGroups();
            if (groups.isEmpty()) {
               return "No encounter groups are configured.";
            } else {
               OutpostRegistry.EncounterGroup pickedGroup = (OutpostRegistry.EncounterGroup)groups.get(rand.nextInt(groups.size()));
               String instanceKey = pickedLocation.getOutpostId() + "_" + tier.ordinal() + "_" + uuid.toString().substring(0, 8);
               OutpostInstance oi = new OutpostInstance(pickedLocation, tier, pickedGroup, player.getServerWorld(), uuid);
               this.activeOutposts.put(instanceKey, oi);
               this.playerToInstance.put(uuid, instanceKey);
               BlockPos loc = pickedLocation.getLocation();
               player.sendMessage(new TextComponentString(TextFormatting.GOLD + "Outpost Mission: " + TextFormatting.WHITE + "Unknown Enemy" + TextFormatting.GRAY + " (" + tier.getDisplayName() + " difficulty)"));
               player.sendMessage(new TextComponentString(TextFormatting.GOLD + "Location: " + TextFormatting.WHITE + pickedLocation.getLocationName()));
               player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Travel to the outpost location to begin the encounter."));
               player.sendMessage(new TextComponentString(TextFormatting.GRAY + "Coordinates: X=" + loc.getX() + " Z=" + loc.getZ() + " (within " + pickedLocation.getActivationRadius() + " blocks)"));
               int safeY = WaypointSpawnLogic.findGroundY(player.getServerWorld(), loc.getX(), loc.getZ());
               BlockPos safeLoc = new BlockPos(loc.getX(), safeY, loc.getZ());
               QuestManager.getInstance().getWaypointManager().setWaypoint(player, "outpost_mission", new WaypointData(safeLoc, WaypointData.WaypointType.COMBAT, pickedLocation.getLocationName()));
               EndgameNetworkHelper.sendOutpostSync(player);
               return null;
            }
         }
      }
   }

   public void leaveOutpost(EntityPlayerMP player) {
      String instanceKey = (String)this.playerToInstance.remove(player.getUniqueID());
      if (instanceKey != null) {
         OutpostInstance oi = (OutpostInstance)this.activeOutposts.get(instanceKey);
         if (oi != null) {
            oi.removeParticipant(player.getUniqueID());
            player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Outpost mission abandoned."));
         }

         QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "outpost_mission");
         EndgameNetworkHelper.sendOutpostSync(player);
      }
   }

   public void onServerTick(World world) {
      ++this.tickCounter;
      if (!this.savedInstancesLoaded) {
         this.savedInstancesLoaded = true;
         this.loadSavedInstances(world);
      }

      if (this.tickCounter % 20 == 0) {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            Iterator<Map.Entry<String, OutpostInstance>> it = this.activeOutposts.entrySet().iterator();

            while(it.hasNext()) {
               Map.Entry<String, OutpostInstance> entry = (Map.Entry)it.next();
               OutpostInstance oi = (OutpostInstance)entry.getValue();
               if (oi.getState() == OutpostInstance.OutpostState.TRAVELING) {
                  this.checkProximityActivation(oi, server);
               }

               oi.tick(world);
               if (oi.getState() == OutpostInstance.OutpostState.CLEANUP) {
                  for(UUID pUUID : oi.getAllParticipantUUIDs()) {
                     this.playerToInstance.remove(pUUID);
                  }

                  it.remove();
               }
            }

            this.saveTickCounter += 20;
            if (this.saveTickCounter >= 1200) {
               this.saveTickCounter = 0;
               this.saveAllInstances(world);
            }

         }
      }
   }

   private void checkProximityActivation(OutpostInstance oi, MinecraftServer server) {
      BlockPos outpostLoc = oi.getDefinition().getLocation();
      int radius = oi.getDefinition().getActivationRadius();
      double radiusSq = (double)(radius * radius);

      for(UUID pUUID : oi.getAllParticipantUUIDs()) {
         EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(pUUID);
         if (player != null && player.isEntityAlive()) {
            double dx = player.posX - (double)outpostLoc.getX();
            double dz = player.posZ - (double)outpostLoc.getZ();
            double distSq = dx * dx + dz * dz;
            if (distSq <= radiusSq) {
               oi.onPlayerArrived();
               QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "outpost_mission");
               player.sendMessage(new TextComponentString(TextFormatting.RED + "" + TextFormatting.BOLD + "You've reached the outpost! Enemies approaching..."));

               for(UUID syncUUID : oi.getAllParticipantUUIDs()) {
                  EntityPlayerMP syncPlayer = server.getPlayerList().getPlayerByUUID(syncUUID);
                  if (syncPlayer != null) {
                     EndgameNetworkHelper.sendOutpostSync(syncPlayer);
                  }
               }

               return;
            }
         }
      }

   }

   public void onPlayerDisconnect(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      String instanceKey = (String)this.playerToInstance.get(uuid);
      if (instanceKey != null) {
         OutpostInstance oi = (OutpostInstance)this.activeOutposts.get(instanceKey);
         if (oi != null) {
            if (oi.getState() != OutpostInstance.OutpostState.TRAVELING) {
               this.playerToInstance.remove(uuid);
               oi.removeParticipant(uuid);
            }
         } else {
            this.playerToInstance.remove(uuid);
         }

         QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "outpost_mission");
      }
   }

   public void onPlayerReconnect(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      String instanceKey = (String)this.playerToInstance.get(uuid);
      if (instanceKey != null) {
         OutpostInstance oi = (OutpostInstance)this.activeOutposts.get(instanceKey);
         if (oi == null) {
            this.playerToInstance.remove(uuid);
         } else {
            if (oi.getState() == OutpostInstance.OutpostState.TRAVELING) {
               BlockPos loc = oi.getDefinition().getLocation();
               QuestManager.getInstance().getWaypointManager().setWaypoint(player, "outpost_mission", new WaypointData(loc, WaypointData.WaypointType.COMBAT, oi.getDefinition().getLocationName()));
               player.sendMessage(new TextComponentString(TextFormatting.GOLD + "Outpost Mission resumed: " + TextFormatting.WHITE + oi.getDefinition().getLocationName() + TextFormatting.GRAY + " (" + oi.getTier().getDisplayName() + " difficulty)"));
            }

            EndgameNetworkHelper.sendOutpostSync(player);
         }
      }
   }

   public OutpostInstance getPlayerOutpost(UUID playerUUID) {
      String instanceKey = (String)this.playerToInstance.get(playerUUID);
      return instanceKey == null ? null : (OutpostInstance)this.activeOutposts.get(instanceKey);
   }

   public boolean isInOutpost(UUID playerUUID) {
      return this.playerToInstance.containsKey(playerUUID);
   }

   public String getPlayerInstanceKey(UUID playerUUID) {
      return (String)this.playerToInstance.get(playerUUID);
   }

   public OutpostInstance getActiveOutpost(String instanceKey) {
      return (OutpostInstance)this.activeOutposts.get(instanceKey);
   }

   public Collection<OutpostInstance> getActiveInstances() {
      return this.activeOutposts.values();
   }

   private OutpostDifficultyTier determineTier(EntityPlayerMP player) {
      OutpostDifficultyTier[] tiers = OutpostDifficultyTier.values();
      return tiers[player.getServerWorld().rand.nextInt(tiers.length)];
   }

   public void saveAllInstances(World world) {
      EndgameSavedData savedData = EndgameSavedData.get(world);
      savedData.clearAllSavedOutpostInstances();
      int savedCount = 0;

      for(Map.Entry<String, OutpostInstance> entry : this.activeOutposts.entrySet()) {
         OutpostInstance oi = (OutpostInstance)entry.getValue();
         if (oi.getState() == OutpostInstance.OutpostState.TRAVELING) {
            NBTTagCompound nbt = oi.writeToNBT();
            savedData.saveOutpostInstance((String)entry.getKey(), nbt);
            ++savedCount;
         }
      }

      if (savedCount > 0) {
         System.out.println("[OutpostManager] Saved " + savedCount + " TRAVELING outpost instance(s) to NBT.");
      }

   }

   public void loadSavedInstances(World world) {
      EndgameSavedData savedData = EndgameSavedData.get(world);
      Map<String, NBTTagCompound> saved = savedData.getSavedOutpostInstances();
      if (!saved.isEmpty()) {
         int restoredCount = 0;

         for(Map.Entry<String, NBTTagCompound> entry : saved.entrySet()) {
            String key = (String)entry.getKey();
            NBTTagCompound nbt = (NBTTagCompound)entry.getValue();
            OutpostInstance restored = OutpostInstance.fromNBT(nbt, world);
            if (restored == null) {
               System.out.println("[OutpostManager] Skipped invalid saved instance: " + key);
            } else {
               this.activeOutposts.put(restored.getInstanceKey(), restored);

               for(UUID participantUUID : restored.getAllParticipantUUIDs()) {
                  this.playerToInstance.put(participantUUID, restored.getInstanceKey());
               }

               ++restoredCount;
            }
         }

         savedData.clearAllSavedOutpostInstances();
         if (restoredCount > 0) {
            System.out.println("[OutpostManager] Restored " + restoredCount + " outpost instance(s) from NBT.");
         }

      }
   }

   public void onServerStopping(World world) {
      this.saveAllInstances(world);
      System.out.println("[OutpostManager] Saved outpost instances on server stop.");
   }
}
