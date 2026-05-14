
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class NatureReleaseDetector {
   public static boolean hasNatureRelease(EntityPlayer player, NatureType nature) {
      Class<? extends Item> itemClass = nature.getItemClass();
      if (itemClass == null) {
         return false;
      } else {
         for(List<ItemStack> list : Arrays.asList(player.inventory.mainInventory, player.inventory.armorInventory, player.inventory.offHandInventory)) {
            for(ItemStack stack : list) {
               if (!stack.isEmpty() && itemClass.isAssignableFrom(stack.getItem().getClass())) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public static Set<NatureType> getPlayerNatureReleases(EntityPlayer player) {
      Set<NatureType> result = EnumSet.noneOf(NatureType.class);

      for(NatureType nature : NatureType.values()) {
         if (hasNatureRelease(player, nature)) {
            result.add(nature);
         }
      }

      return result;
   }

   public static NatureType getNatureFromDamageSource(DamageSource source) {
      Entity immediate = source.getImmediateSource();
      if (immediate == null) {
         return null;
      } else {
         String className = immediate.getClass().getName().toLowerCase();
         if (!className.contains("katon") && !className.contains("fireball") && !className.contains("fire")) {
            if (!className.contains("suiton") && !className.contains("water")) {
               if (!className.contains("futon") && !className.contains("wind")) {
                  if (!className.contains("raiton") && !className.contains("lightning")) {
                     return !className.contains("doton") && !className.contains("earth") && !className.contains("rock") ? null : NatureType.DOTON;
                  } else {
                     return NatureType.RAITON;
                  }
               } else {
                  return NatureType.FUTON;
               }
            } else {
               return NatureType.SUITON;
            }
         } else {
            return NatureType.KATON;
         }
      }
   }

   public static NatureType fromInt(int natureType) {
      switch (natureType) {
         case 1:
            return NatureType.KATON;
         case 2:
            return NatureType.FUTON;
         case 3:
            return NatureType.SUITON;
         case 4:
            return NatureType.RAITON;
         case 5:
            return NatureType.DOTON;
         default:
            return null;
      }
   }

   public static int toInt(NatureType nature) {
      if (nature == null) {
         return 0;
      } else {
         switch (nature) {
            case KATON:
               return 1;
            case FUTON:
               return 2;
            case SUITON:
               return 3;
            case RAITON:
               return 4;
            case DOTON:
               return 5;
            default:
               return 0;
         }
      }
   }

   public static enum NatureType {
      KATON("net.narutomod.item.ItemKaton$RangedItem"),
      SUITON("net.narutomod.item.ItemSuiton$RangedItem"),
      FUTON("net.narutomod.item.ItemFuton$RangedItem"),
      RAITON("net.narutomod.item.ItemRaiton$RangedItem"),
      DOTON("net.narutomod.item.ItemDoton$RangedItem");

      private final String className;
      private Class<?> resolvedClass;
      private boolean attempted;

      private NatureType(String className) {
         this.className = className;
      }

      public Class<? extends Item> getItemClass() {
         if (!this.attempted) {
            this.attempted = true;

            try {
               this.resolvedClass = Class.forName(this.className);
            } catch (ClassNotFoundException var2) {
            }
         }

         return this.resolvedClass != null ? this.resolvedClass : null;
      }
   }
}
