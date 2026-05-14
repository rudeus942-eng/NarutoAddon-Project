
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber
public class JutsuLockHandler {
   private static final Set<UUID> migratedPlayers = new HashSet();
   private static final String[] RELEASE_ITEMS = new String[]{"narutomod:katon", "narutomod:raiton", "narutomod:futon", "narutomod:doton", "narutomod:suiton"};
   private static final int[][] OUR_INDICES = new int[][]{{6, 7, 8}, {7, 8, 9}, {6, 7, 8}, {7, 8, 9}, {10, 11, 12}};

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         if (!event.player.world.isRemote) {
            EntityPlayer player = event.player;
            if (!player.isCreative()) {
               if (!migratedPlayers.contains(player.getUniqueID())) {
                  migratedPlayers.add(player.getUniqueID());

                  for(int r = 0; r < RELEASE_ITEMS.length; ++r) {
                     Item releaseItem = (Item)Item.REGISTRY.getObject(new ResourceLocation(RELEASE_ITEMS[r]));
                     if (releaseItem != null) {
                        for(int slot = 0; slot < player.inventory.getSizeInventory(); ++slot) {
                           ItemStack stack = player.inventory.getStackInSlot(slot);
                           if (!stack.isEmpty() && stack.getItem() == releaseItem && stack.hasTagCompound()) {
                              NBTTagCompound nbt = stack.getTagCompound();

                              for(int index : OUR_INDICES[r]) {
                                 String key = "JutsuCDMapKey" + index;
                                 if (!nbt.hasKey(key)) {
                                    nbt.setLong(key, -1L);
                                 }
                              }
                           }
                        }
                     }
                  }

               }
            }
         }
      }
   }
}
