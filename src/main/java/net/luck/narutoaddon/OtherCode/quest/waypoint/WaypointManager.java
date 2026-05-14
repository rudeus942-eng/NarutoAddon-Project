
package net.luck.narutoaddon.OtherCode.quest.waypoint;

import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.quest.network.QuestNetworkHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class WaypointManager {
   private static final int PROXIMITY_RADIUS = 10;
   private static final int PROXIMITY_Y_TOLERANCE = 4;
   private static final int MAP_BOUND = 6000;
   private final Map<UUID, Map<String, WaypointData>> activeWaypoints = new HashMap();

   public void setWaypoint(EntityPlayerMP player, String questId, WaypointData waypoint) {
      ((Map)this.activeWaypoints.computeIfAbsent(player.getUniqueID(), (k) -> new HashMap())).put(questId, waypoint);
      QuestNetworkHelper.sendWaypoint(player, questId, waypoint);
   }

   public void setWaypoint(EntityPlayerMP player, WaypointData waypoint) {
      this.setWaypoint(player, "story", waypoint);
   }

   public void clearWaypoint(EntityPlayerMP player, String questId) {
      UUID playerId = player.getUniqueID();
      Map<String, WaypointData> waypoints = (Map)this.activeWaypoints.get(playerId);
      if (waypoints != null) {
         waypoints.remove(questId);
         if (waypoints.isEmpty()) {
            this.activeWaypoints.remove(playerId);
         }
      }

      QuestNetworkHelper.clearWaypoint(player, questId);
   }

   public void clearWaypoint(EntityPlayerMP player) {
      this.clearAllWaypointsForPlayer(player);
   }

   public void clearAllWaypoints(UUID playerId) {
      this.activeWaypoints.remove(playerId);
   }

   public void clearAllWaypointsForPlayer(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      Map<String, WaypointData> waypoints = (Map)this.activeWaypoints.remove(playerId);
      if (waypoints != null) {
         for(String questId : waypoints.keySet()) {
            QuestNetworkHelper.clearWaypoint(player, questId);
         }
      }

   }

   public void clearWaypointByUUID(UUID playerId) {
      this.activeWaypoints.remove(playerId);
   }

   public void removeWaypointByUUID(UUID playerId, String questId) {
      Map<String, WaypointData> waypoints = (Map)this.activeWaypoints.get(playerId);
      if (waypoints != null) {
         waypoints.remove(questId);
         if (waypoints.isEmpty()) {
            this.activeWaypoints.remove(playerId);
         }
      }

   }

   public WaypointData getWaypoint(UUID playerId, String questId) {
      Map<String, WaypointData> waypoints = (Map)this.activeWaypoints.get(playerId);
      return waypoints == null ? null : (WaypointData)waypoints.get(questId);
   }

   public WaypointData getWaypoint(UUID playerId) {
      Map<String, WaypointData> waypoints = (Map)this.activeWaypoints.get(playerId);
      return waypoints != null && !waypoints.isEmpty() ? (WaypointData)waypoints.values().iterator().next() : null;
   }

   public Map<String, WaypointData> getAllWaypoints(UUID playerId) {
      Map<String, WaypointData> waypoints = (Map)this.activeWaypoints.get(playerId);
      return waypoints != null ? Collections.unmodifiableMap(waypoints) : Collections.emptyMap();
   }

   public void checkProximity(EntityPlayerMP player, World world) {
      if (!player.isDead && !(player.getHealth() <= 0.0F)) {
         UUID playerId = player.getUniqueID();
         Map<String, WaypointData> waypoints = (Map)this.activeWaypoints.get(playerId);
         if (waypoints != null && !waypoints.isEmpty()) {
            BlockPos playerPos = player.getPosition();
            double radiusSq = (double)100.0F;
            String closestQuestId = null;
            double closestDistSq = Double.MAX_VALUE;

            for(Map.Entry<String, WaypointData> entry : new ArrayList(waypoints.entrySet())) {
               BlockPos waypointPos = ((WaypointData)entry.getValue()).getPosition();
               double dx = (double)(playerPos.getX() - waypointPos.getX());
               double dz = (double)(playerPos.getZ() - waypointPos.getZ());
               double distSq = dx * dx + dz * dz;
               int dy = Math.abs(playerPos.getY() - waypointPos.getY());
               boolean isGenerated = ((String)entry.getKey()).startsWith("gen_");
               boolean yOk = isGenerated || dy <= 4;
               if (distSq <= radiusSq && yOk && distSq < closestDistSq) {
                  closestDistSq = distSq;
                  closestQuestId = (String)entry.getKey();
               }
            }

            if (closestQuestId != null) {
               QuestManager.getInstance().onWaypointReached(player, closestQuestId);
            }

         }
      }
   }

   public static BlockPos validatePosition(World world, BlockPos pos) {
      int x = Math.max(-6000, Math.min(6000, pos.getX()));
      int z = Math.max(-6000, Math.min(6000, pos.getZ()));
      return new BlockPos(x, pos.getY(), z);
   }
}
