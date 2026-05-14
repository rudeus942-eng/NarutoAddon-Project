package net.luck.narutoaddon.OtherCode.shop.building;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BuildingCartData {
   private static final List<CartEntry> entries = new ArrayList();
   private static final int MAX_ENTRIES = 50;
   private static final int MAX_QUANTITY_PER_ITEM = 576;

   public static boolean addShulkerBox(String registryName, int meta, String displayName, int unitPrice) {
      for(CartEntry entry : entries) {
         if (entry.registryName.equals(registryName) && entry.meta == meta && entry.isShulkerBox) {
            if (entry.quantity < 10) {
               ++entry.quantity;
               return true;
            }

            return false;
         }
      }

      if (entries.size() >= 50) {
         return false;
      } else {
         CartEntry entry = new CartEntry(registryName, meta, displayName + " [Box]", unitPrice, 1);
         entry.isShulkerBox = true;
         entries.add(entry);
         return true;
      }
   }

   public static boolean addToCart(String registryName, int meta, String displayName, int unitPrice, int quantity) {
      if (quantity <= 0) {
         return false;
      } else {
         for(CartEntry entry : entries) {
            if (entry.registryName.equals(registryName) && entry.meta == meta) {
               int newQty = Math.min(entry.quantity + quantity, 576);
               if (newQty == entry.quantity) {
                  return false;
               }

               entry.quantity = newQty;
               return true;
            }
         }

         if (entries.size() >= 50) {
            return false;
         } else {
            entries.add(new CartEntry(registryName, meta, displayName, unitPrice, Math.min(quantity, 576)));
            return true;
         }
      }
   }

   public static void removeOne(String registryName, int meta) {
      Iterator<CartEntry> it = entries.iterator();

      while(it.hasNext()) {
         CartEntry entry = (CartEntry)it.next();
         if (entry.registryName.equals(registryName) && entry.meta == meta) {
            --entry.quantity;
            if (entry.quantity <= 0) {
               it.remove();
            }

            return;
         }
      }

   }

   public static void setQuantity(String registryName, int meta, int quantity) {
      if (quantity <= 0) {
         removeEntry(registryName, meta);
      } else {
         quantity = Math.min(quantity, 576);

         for(CartEntry entry : entries) {
            if (entry.registryName.equals(registryName) && entry.meta == meta) {
               entry.quantity = quantity;
               return;
            }
         }

      }
   }

   public static void removeEntry(String registryName, int meta) {
      entries.removeIf((e) -> e.registryName.equals(registryName) && e.meta == meta);
   }

   public static int getQuantity(String registryName, int meta) {
      for(CartEntry entry : entries) {
         if (entry.registryName.equals(registryName) && entry.meta == meta) {
            return entry.quantity;
         }
      }

      return 0;
   }

   public static long getTotalCost() {
      long total = 0L;

      for(CartEntry entry : entries) {
         total += (long)entry.getTotalPrice();
      }

      return total;
   }

   public static int getTotalItemCount() {
      int total = 0;

      for(CartEntry entry : entries) {
         total += entry.quantity;
      }

      return total;
   }

   public static List<CartEntry> getEntries() {
      return entries;
   }

   public static int getEntryCount() {
      return entries.size();
   }

   public static boolean isEmpty() {
      return entries.isEmpty();
   }

   public static void clear() {
      entries.clear();
   }

   public static class CartEntry {
      public final String registryName;
      public final int meta;
      public final String displayName;
      public final int unitPrice;
      public int quantity;
      public boolean isShulkerBox;

      public CartEntry(String registryName, int meta, String displayName, int unitPrice, int quantity) {
         this.registryName = registryName;
         this.meta = meta;
         this.displayName = displayName;
         this.unitPrice = unitPrice;
         this.quantity = quantity;
         this.isShulkerBox = false;
      }

      public int getTotalPrice() {
         return this.isShulkerBox ? this.unitPrice * this.quantity * 27 * 64 : this.unitPrice * this.quantity;
      }

      public String getKey() {
         return this.registryName + ":" + this.meta + (this.isShulkerBox ? ":shulker" : "");
      }
   }
}
