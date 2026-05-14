
package net.luck.narutoaddon.OtherCode.shop.core;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

public class DuplicateDetector {
   private static final int[] DUPE_TOKENS = new int[]{5, 15, 40, 100, 250, 600};

   public static boolean isDuplicate(EntityPlayerMP player, String ftbQuestId) {
      if (ftbQuestId != null && !ftbQuestId.isEmpty()) {
         try {
            ShopSavedData data = ShopSavedData.get(player.world);
            if (data != null) {
               return data.hasCompletedFtbQuest(player.getUniqueID(), ftbQuestId);
            }
         } catch (Exception var3) {
         }

         return false;
      } else {
         return false;
      }
   }

   public static int getDuplicateTokens(ItemRarity rarity) {
      return rarity.id >= 0 && rarity.id < DUPE_TOKENS.length ? DUPE_TOKENS[rarity.id] : DUPE_TOKENS[0];
   }

   public static int getDuplicateTokens(int rarityId) {
      return rarityId >= 0 && rarityId < DUPE_TOKENS.length ? DUPE_TOKENS[rarityId] : DUPE_TOKENS[0];
   }

   public static int handleDuplicate(EntityPlayerMP player, ItemRarity rarity) {
      int tokens = getDuplicateTokens(rarity);
      ShopSavedData data = ShopSavedData.get(player.world);
      if (data != null) {
         data.addTokens(player.getUniqueID(), tokens);
      }

      String msg = String.format("§6[§eCRATE§6] §7Duplicate! Converted to §e%d tokens§7.", tokens);
      player.sendMessage(new TextComponentString(msg));
      return tokens;
   }

   public static int handleDuplicate(EntityPlayerMP player, int rarityId) {
      return handleDuplicate(player, ItemRarity.fromId(rarityId));
   }

   public static boolean isDuplicateByItemKey(EntityPlayerMP player, String itemId, int itemMeta) {
      if (itemId != null && !itemId.isEmpty()) {
         String key = itemId + ":" + itemMeta;

         try {
            ShopSavedData data = ShopSavedData.get(player.world);
            if (data != null) {
               return data.hasOwnedItem(player.getUniqueID(), key);
            }
         } catch (Exception var5) {
         }

         return false;
      } else {
         return false;
      }
   }

   public static void recordItemOwnership(EntityPlayerMP player, String itemId, int itemMeta) {
      if (itemId != null && !itemId.isEmpty()) {
         String key = itemId + ":" + itemMeta;

         try {
            ShopSavedData data = ShopSavedData.get(player.world);
            if (data != null) {
               data.addOwnedItem(player.getUniqueID(), key);
            }
         } catch (Exception var5) {
         }

      }
   }

   public static void recordFtbQuestCompletion(EntityPlayerMP player, String ftbQuestId) {
      if (ftbQuestId != null && !ftbQuestId.isEmpty()) {
         try {
            ShopSavedData data = ShopSavedData.get(player.world);
            if (data != null) {
               data.addCompletedFtbQuest(player.getUniqueID(), ftbQuestId);
            }
         } catch (Exception var3) {
         }

      }
   }
}
