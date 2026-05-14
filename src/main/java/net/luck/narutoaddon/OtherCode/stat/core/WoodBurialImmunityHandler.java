
package net.luck.narutoaddon.OtherCode.stat.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WoodBurialImmunityHandler {
   private static final WoodBurialImmunityHandler INSTANCE = new WoodBurialImmunityHandler();

   public static void register() {
      MinecraftForge.EVENT_BUS.register(INSTANCE);
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerAttacked(LivingAttackEvent event) {
      if (!event.isCanceled()) {
         EntityLivingBase victim = event.getEntityLiving();
         if (victim instanceof EntityPlayer) {
            if (!victim.world.isRemote) {
               if (this.isInWoodBurial(victim)) {
                  event.setCanceled(true);
               }

            }
         }
      }
   }

   private boolean isInWoodBurial(EntityLivingBase player) {
      World world = player.world;

      for(Entity entity : world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().grow((double)3.0F))) {
         String className = entity.getClass().getName();
         if (className.contains("EntityWoodBurial")) {
            return true;
         }
      }

      return false;
   }
}
