
package net.luck.narutoaddon.OtherCode.territory.core;

import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.UUID;

public class TerritoryConversionPayout {
   private static final String PAYOUT_MARKER = "territory_conversion_paid";
   private static final String TITLE_MARKER = "vet_border_wars";
   private static final int DIAMOND_PAYOUT = 100000;
   private static final int GOLD_PAYOUT = 50000;
   private static final int SILVER_PAYOUT = 20000;
   private static final int BRONZE_PAYOUT = 10000;
   private static final int IRON_PAYOUT = 5000;
   private static final int PARTICIPATION_PAYOUT = 2000;

   @SubscribeEvent
   public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
      if (event.player instanceof EntityPlayerMP) {
         EntityPlayerMP player = (EntityPlayerMP)event.player;
         ShopSavedData shop = ShopSavedData.get(player.world);
         if (shop != null) {
            UUID playerId = player.getUniqueID();
            if (!shop.hasOwnedItem(playerId, "territory_conversion_paid")) {
               TerritorySavedData terr = TerritorySavedData.get(player.world);
               if (terr != null) {
                  PerPlayerTerritoryData data = (PerPlayerTerritoryData)terr.getAllPlayerData().get(playerId);
                  if (data == null) {
                     shop.addOwnedItem(playerId, "territory_conversion_paid");
                  } else {
                     int contribution = data.getWeeklyPoints();
                     String tier;
                     int payout;
                     if (contribution >= 40000) {
                        tier = "Diamond";
                        payout = 100000;
                     } else if (contribution >= 28000) {
                        tier = "Gold";
                        payout = 50000;
                     } else if (contribution >= 16000) {
                        tier = "Silver";
                        payout = 20000;
                     } else if (contribution >= 6000) {
                        tier = "Bronze";
                        payout = 10000;
                     } else if (contribution >= 1500) {
                        tier = "Iron";
                        payout = 5000;
                     } else {
                        if (contribution <= 0 && data.getWeeklyKills() <= 0 && data.getWeeklyCaptures() <= 0) {
                           shop.addOwnedItem(playerId, "territory_conversion_paid");
                           return;
                        }

                        tier = "Participation";
                        payout = 2000;
                     }

                     RyoRewardHelper.grantRyoSilent(player, payout);
                     shop.addOwnedItem(playerId, "territory_conversion_paid");
                     shop.addOwnedItem(playerId, "vet_border_wars");
                     player.sendMessage(new TextComponentString("§6§l=== Border Wars Conversion ==="));
                     player.sendMessage(new TextComponentString("§7Your " + tier + "-tier contribution to the Border Wars has been recognized."));
                     player.sendMessage(new TextComponentString("§a+" + payout + " Ryo"));
                     player.sendMessage(new TextComponentString("§eTitle unlocked: §b[Veteran of the Border Wars]"));
                     player.sendMessage(new TextComponentString("§6§l============================="));
                  }
               }
            }
         }
      }
   }
}
