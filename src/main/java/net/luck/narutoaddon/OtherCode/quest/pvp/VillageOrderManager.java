
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.AdvisorManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class VillageOrderManager {
   private static VillageOrderManager instance;
   public static final int MAX_ORDERS_PER_VILLAGE = 2;
   public static final long PLAYER_ORDER_DURATION = 14400000L;
   public static final long VILLAGE_ORDER_DURATION = 28800000L;
   public static final long ASSIGN_MISSION_DURATION = 21600000L;
   public static final int ORDER_BONUS_PVP_XP = 15;
   public static final int ORDER_BONUS_NINJA_XP = 25;

   public static VillageOrderManager getInstance() {
      if (instance == null) {
         instance = new VillageOrderManager();
      }

      return instance;
   }

   public static void reset() {
      instance = null;
   }

   @Nullable
   public VillageOrder issueOrder(UUID issuerId, VillageHelper.Village issuerVillage, VillageOrder.OrderType type, @Nullable UUID targetPlayerId, @Nullable String targetPlayerName, @Nullable VillageHelper.Village targetVillage, World world) {
      if (issuerVillage == VillageHelper.Village.UNKNOWN) {
         return null;
      } else if (!AdvisorManager.getInstance().hasLeadershipAuthority(issuerId, issuerVillage)) {
         return null;
      } else {
         List<VillageOrder> activeOrders = this.getActiveOrders(issuerVillage.teamName, world);
         if (activeOrders.size() >= 2) {
            return null;
         } else {
            long duration;
            switch (type) {
               case TARGET_PLAYER:
                  duration = 14400000L;
                  break;
               case ASSIGN_MISSION:
                  duration = 21600000L;
                  break;
               default:
                  duration = 28800000L;
            }

            long now = System.currentTimeMillis();
            String orderId = "order_" + issuerVillage.teamName + "_" + now + "_" + (new Random()).nextInt(1000);
            String issuerName = this.resolvePlayerName(issuerId);
            VillageOrder order = new VillageOrder(orderId, issuerVillage.teamName, issuerId, issuerName, type, targetVillage != null ? targetVillage.teamName : null, targetPlayerId, targetPlayerName, now, now + duration, 15, 25);
            PvpSavedData data = PvpSavedData.get(world);
            data.addOrder(issuerVillage.teamName, order);
            this.broadcastToVillage(issuerVillage, order);
            return order;
         }
      }
   }

   @Nullable
   public VillageOrder issueAssignMissionOrder(UUID issuerId, VillageHelper.Village issuerVillage, String templateId, String missionName, World world) {
      if (issuerVillage == VillageHelper.Village.UNKNOWN) {
         return null;
      } else if (!AdvisorManager.getInstance().hasLeadershipAuthority(issuerId, issuerVillage)) {
         return null;
      } else {
         List<VillageOrder> activeOrders = this.getActiveOrders(issuerVillage.teamName, world);
         if (activeOrders.size() >= 2) {
            return null;
         } else {
            long now = System.currentTimeMillis();
            String orderId = "order_" + issuerVillage.teamName + "_" + now + "_" + (new Random()).nextInt(1000);
            String issuerName = this.resolvePlayerName(issuerId);
            VillageOrder order = new VillageOrder(orderId, issuerVillage.teamName, issuerId, issuerName, VillageOrder.OrderType.ASSIGN_MISSION, (String)null, (UUID)null, (String)null, now, now + 21600000L, 15, 25, templateId, missionName);
            PvpSavedData data = PvpSavedData.get(world);
            data.addOrder(issuerVillage.teamName, order);
            this.broadcastToVillage(issuerVillage, order);
            return order;
         }
      }
   }

   public boolean cancelOrder(UUID issuerId, VillageHelper.Village issuerVillage, String orderId, World world) {
      if (issuerVillage == VillageHelper.Village.UNKNOWN) {
         return false;
      } else if (!AdvisorManager.getInstance().hasLeadershipAuthority(issuerId, issuerVillage)) {
         return false;
      } else {
         PvpSavedData data = PvpSavedData.get(world);
         data.removeOrder(issuerVillage.teamName, orderId);
         return true;
      }
   }

   public List<VillageOrder> getActiveOrders(String villageName, World world) {
      PvpSavedData data = PvpSavedData.get(world);
      List<VillageOrder> orders = data.getOrders(villageName);
      List<VillageOrder> active = new ArrayList();

      for(VillageOrder order : orders) {
         if (!order.isExpired()) {
            active.add(order);
         }
      }

      return active;
   }

   @Nullable
   public VillageOrder getMatchingOrder(String killerVillage, UUID victimId, String victimVillage, World world) {
      for(VillageOrder order : this.getActiveOrders(killerVillage, world)) {
         if (order.matchesKill(victimId, victimVillage)) {
            return order;
         }
      }

      return null;
   }

   @Nullable
   public VillageOrder getAssignedMissionOrder(String villageName, String orderId, World world) {
      for(VillageOrder order : this.getActiveOrders(villageName, world)) {
         if (order.getOrderType() == VillageOrder.OrderType.ASSIGN_MISSION && order.getOrderId().equals(orderId)) {
            return order;
         }
      }

      return null;
   }

   public void tickOrders(World world) {
      PvpSavedData data = PvpSavedData.get(world);

      for(VillageHelper.Village v : VillageHelper.Village.values()) {
         if (v != VillageHelper.Village.UNKNOWN) {
            List<VillageOrder> orders = data.getOrders(v.teamName);
            boolean changed = false;
            List<VillageOrder> active = new ArrayList();

            for(VillageOrder order : orders) {
               if (!order.isExpired()) {
                  active.add(order);
               } else {
                  changed = true;
               }
            }

            if (changed) {
               data.setOrders(v.teamName, active);
            }
         }
      }

   }

   private void broadcastToVillage(VillageHelper.Village village, VillageOrder order) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         String message;
         switch (order.getOrderType()) {
            case TARGET_PLAYER:
               message = TextFormatting.GOLD + "[Village Order] " + TextFormatting.RESET + order.getIssuerName() + " has issued a hunt order on " + TextFormatting.RED + order.getTargetPlayerName() + TextFormatting.RESET + "! (+" + order.getBonusPvpXp() + " PvP XP bonus)";
               break;
            case ASSIGN_MISSION:
               message = TextFormatting.GOLD + "[Village Order] " + TextFormatting.RESET + order.getIssuerName() + " has assigned a mission: " + TextFormatting.AQUA + order.getAssignedMissionName() + TextFormatting.RESET + "! Open PvP tab to accept.";
               break;
            default:
               message = TextFormatting.GOLD + "[Village Order] " + TextFormatting.RESET + order.getIssuerName() + " has issued an order against " + TextFormatting.RED + order.getTargetVillage() + TextFormatting.RESET + " village! (+" + order.getBonusPvpXp() + " PvP XP bonus)";
         }

         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            if (VillageHelper.getVillage(player) == village) {
               player.sendMessage(new TextComponentString(message));
            }
         }

      }
   }

   private String resolvePlayerName(UUID playerId) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
         if (player != null) {
            return player.getName();
         }
      }

      return playerId.toString().substring(0, 8) + "...";
   }
}
