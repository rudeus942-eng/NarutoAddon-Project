package net.luck.narutoaddon.OtherCode.shop.core;

import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class DailyLoginHandler {
   private static final int[] LOGIN_STREAK_RYO = new int[]{50, 75, 100, 125, 150, 200, 300};

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
      if (event.player instanceof EntityPlayerMP) {
         EntityPlayerMP player = (EntityPlayerMP)event.player;
         ShopSavedData data = ShopSavedData.get(player.world);
         if (data != null) {
            int streakDay = data.updateLoginStreak(player.getUniqueID());
            if (streakDay > 0) {
               int ryoAmount = LOGIN_STREAK_RYO[Math.min(streakDay - 1, LOGIN_STREAK_RYO.length - 1)];
               grantRyo(player, ryoAmount);
               String msg = "§6[§eDaily Login§6] §aDay " + streakDay + " streak! §e+" + ryoAmount + " ryo";
               player.sendMessage(new TextComponentString(msg));

               try {
                  BattlePassManager.getInstance().awardXP(player.getUniqueID(), 100, "login", player.world);
               } catch (Exception var9) {
               }

               if (streakDay == 7) {
                  try {
                     BattlePassManager.getInstance().awardXP(player.getUniqueID(), 250, "login_streak", player.world);
                  } catch (Exception var8) {
                  }
               }
            }

         }
      }
   }

   public static void grantRyo(EntityPlayerMP player, int amount) {
      if (amount > 0) {
         RyoRewardHelper.grantRyoSilent(player, amount);
      }
   }
}
