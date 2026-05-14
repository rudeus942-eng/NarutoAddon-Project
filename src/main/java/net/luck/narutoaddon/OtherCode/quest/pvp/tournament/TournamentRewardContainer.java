
package net.luck.narutoaddon.OtherCode.quest.pvp.tournament;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class TournamentRewardContainer extends Container {
   private final IInventory rewardInventory;
   private final String villageName;
   private final int place;

   public TournamentRewardContainer(EntityPlayer player, String villageName, int place) {
      this.villageName = villageName;
      this.place = place;
      this.rewardInventory = new InventoryBasic("Tournament Rewards", true, 9);
      TournamentInstance inst = TournamentManager.getInstance().getTournamentForVillage(villageName);
      if (inst != null) {
         ItemStack[] existing;
         switch (place) {
            case 1:
               existing = inst.getRewards1st();
               break;
            case 2:
               existing = inst.getRewards2nd();
               break;
            case 3:
               existing = inst.getRewards3rd();
               break;
            default:
               existing = new ItemStack[9];
         }

         if (existing != null) {
            for(int i = 0; i < 9 && i < existing.length; ++i) {
               if (existing[i] != null && !existing[i].isEmpty()) {
                  this.rewardInventory.setInventorySlotContents(i, existing[i].copy());
               }
            }
         }
      }

      for(int i = 0; i < 9; ++i) {
         this.addSlotToContainer(new Slot(this.rewardInventory, i, 8 + i * 18, 20));
      }

      for(int row = 0; row < 3; ++row) {
         for(int col = 0; col < 9; ++col) {
            this.addSlotToContainer(new Slot(player.inventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
         }
      }

      for(int i = 0; i < 9; ++i) {
         this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 109));
      }

   }

   public boolean canInteractWith(EntityPlayer playerIn) {
      return true;
   }

   public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
      ItemStack result = ItemStack.EMPTY;
      Slot slot = (Slot)this.inventorySlots.get(index);
      if (slot != null && slot.getHasStack()) {
         ItemStack slotStack = slot.getStack();
         result = slotStack.copy();
         if (index < 9) {
            if (!this.mergeItemStack(slotStack, 9, this.inventorySlots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.mergeItemStack(slotStack, 0, 9, false)) {
            return ItemStack.EMPTY;
         }

         if (slotStack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
         } else {
            slot.onSlotChanged();
         }
      }

      return result;
   }

   public void onContainerClosed(EntityPlayer playerIn) {
      super.onContainerClosed(playerIn);
      ItemStack[] items = new ItemStack[9];

      for(int i = 0; i < 9; ++i) {
         ItemStack stack = this.rewardInventory.getStackInSlot(i);
         items[i] = stack != null && !stack.isEmpty() ? stack.copy() : ItemStack.EMPTY;
      }

      TournamentManager.getInstance().setRewards(this.villageName, this.place, items);
      if (playerIn instanceof EntityPlayerMP) {
         TournamentManager.getInstance().saveTournament(this.villageName, playerIn.world);
      }

   }

   public int getPlace() {
      return this.place;
   }
}
