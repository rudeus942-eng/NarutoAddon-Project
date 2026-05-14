package net.luck.narutoaddon.OtherCode.territory.core;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TerritoryEventHandler {
   @SubscribeEvent
   public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
   }

   @SubscribeEvent
   public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
   }

   @SubscribeEvent
   public void onServerTick(TickEvent.ServerTickEvent event) {
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public void onLivingAttack(LivingAttackEvent event) {
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public void onLivingDeath(LivingDeathEvent event) {
   }
}
