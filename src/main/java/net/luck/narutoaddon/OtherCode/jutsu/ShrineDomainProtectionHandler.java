
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ShrineDomainProtectionHandler {
   @SubscribeEvent
   public void onLivingAttack(LivingAttackEvent event) {
      if (event.getEntityLiving() != null && EntityShrineMalevolentShrine.WINDUP_PROTECTED.contains(event.getEntityLiving().getUniqueID())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public void onLivingHurt(LivingHurtEvent event) {
      if (event.getEntityLiving() != null && EntityShrineMalevolentShrine.WINDUP_PROTECTED.contains(event.getEntityLiving().getUniqueID())) {
         event.setAmount(0.0F);
         event.setCanceled(true);
      }

   }
}
