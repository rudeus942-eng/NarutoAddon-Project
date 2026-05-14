package net.luck.narutoaddon.OtherCode.akatsuki.raid;

import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class VillageRaidEventHandler {
   @SubscribeEvent
   public void onLivingDeath(LivingDeathEvent event) {
      if (event.getEntityLiving() instanceof EntityPlayerMP) {
         if (event.getSource().getTrueSource() != null) {
            if (event.getSource().getTrueSource() instanceof EntityPlayerMP) {
               EntityPlayerMP victim = (EntityPlayerMP)event.getEntityLiving();
               EntityPlayerMP killer = (EntityPlayerMP)event.getSource().getTrueSource();
               if (AkatsukiManager.getInstance().isAkatsuki(killer)) {
                  if (!AkatsukiManager.getInstance().isAkatsuki(victim)) {
                     VillageRaidManager.getInstance().onPlayerKill(killer, victim);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         VillageRaidManager.getInstance().tick();
      }
   }
}
