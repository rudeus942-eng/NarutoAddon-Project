
package net.luck.narutoaddon.OtherCode.shop.core;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ShopBroadcastHelper {
   public static void checkAndBroadcast(EntityPlayerMP player, String itemDisplayName, ItemRarity rarity, String crateDisplayName) {
      if (rarity == ItemRarity.S_RANK) {
         broadcastSRank(player.getName(), itemDisplayName, crateDisplayName);
      } else if (rarity == ItemRarity.S_PLUS_RANK) {
         broadcastSPlusRank(player.getName(), itemDisplayName, crateDisplayName);
      }

   }

   public static void checkAndBroadcast(EntityPlayerMP player, String itemDisplayName, int rarityId, String crateDisplayName) {
      checkAndBroadcast(player, itemDisplayName, ItemRarity.fromId(rarityId), crateDisplayName);
   }

   private static void broadcastSRank(String playerName, String itemName, String crateName) {
      String msg = String.format("§6[§eCRATE§6] §e%s §7obtained §6S-Rank: §e%s §7from §e%s§7!", playerName, itemName, crateName);
      broadcastToAll(msg);
   }

   private static void broadcastSPlusRank(String playerName, String itemName, String crateName) {
      String msg = String.format("§c[§4§lCRATE§c] §c§l%s §7obtained §c§lS+-Rank: §c%s §7from §c%s§7!", playerName, itemName, crateName);
      broadcastToAll(msg);
   }

   private static void broadcastToAll(String message) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         server.getPlayerList().sendMessage(new TextComponentString(message));
      }

   }
}
