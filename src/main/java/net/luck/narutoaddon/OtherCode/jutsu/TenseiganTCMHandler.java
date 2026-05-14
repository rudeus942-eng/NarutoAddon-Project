
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@EventBusSubscriber(
   modid = "inftsukaddon"
)
public class TenseiganTCMHandler {
   private static final String PLAYER_TCM_ACTIVE = "inftsuk_tenseigan_tcm_active";

   @SubscribeEvent
   public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
      if (event.getSlot() == EntityEquipmentSlot.HEAD) {
         if (!event.getEntityLiving().world.isRemote) {
            if (event.getEntityLiving() instanceof EntityPlayerMP) {
               EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
               boolean wasOurHelmet = !event.getFrom().isEmpty() && event.getFrom().getItem() == ItemluckAddonTenseigan.helmet;
               boolean isOurHelmet = !event.getTo().isEmpty() && event.getTo().getItem() == ItemluckAddonTenseigan.helmet;
               if (wasOurHelmet && !isOurHelmet) {
                  ItemStack removedHelmet = event.getFrom();
                  ItemluckAddonTenseigan.backupTCMToHelmet(player, removedHelmet);
                  ItemStack helmetInInv = ItemluckAddonTenseigan.findHelmetInInventory(player);
                  if (helmetInInv != null && helmetInInv != removedHelmet && removedHelmet.hasTagCompound() && removedHelmet.getTagCompound().hasKey("SavedTCM")) {
                     if (!helmetInInv.hasTagCompound()) {
                        helmetInInv.setTagCompound(new NBTTagCompound());
                     }

                     helmetInInv.getTagCompound().setTag("SavedTCM", removedHelmet.getTagCompound().getCompoundTag("SavedTCM").copy());
                  }

                  ItemluckAddonTenseigan.removeTCMFromInventory(player);
                  player.getEntityData().setBoolean("inftsuk_tenseigan_tcm_active", false);
               }

            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         if (!event.player.world.isRemote) {
            if (event.player instanceof EntityPlayerMP) {
               EntityPlayerMP player = (EntityPlayerMP)event.player;
               if (player.getEntityData().getBoolean("inftsuk_tenseigan_tcm_active")) {
                  ItemStack headSlot = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                  if (headSlot.isEmpty() || headSlot.getItem() != ItemluckAddonTenseigan.helmet) {
                     player.getEntityData().setBoolean("inftsuk_tenseigan_tcm_active", false);
                  }
               }

            }
         }
      }
   }
}
