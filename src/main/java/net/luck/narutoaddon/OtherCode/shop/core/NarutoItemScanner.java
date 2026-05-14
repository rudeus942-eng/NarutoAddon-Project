
package net.luck.narutoaddon.OtherCode.shop.core;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class NarutoItemScanner {
   private static Class<?> jutsuBaseClass;
   private static boolean jutsuBaseAttempted = false;
   private static final Set<String> DOJUTSU_ITEMS = new HashSet(Arrays.asList("narutomod:byakuganhelmet", "narutomod:sharinganhelmet", "narutomod:rinneganhelmet", "narutomod:rinneganbody", "narutomod:rinneganlegs", "narutomod:mangekyo_sharingan", "narutomod:mangekyo_sharingan_2", "narutomod:mangekyo_sharingan_3", "narutomod:mangekyo_sharingan_4", "narutomod:mangekyo_sharingan_5", "narutomod:mangekyo_sharingan_6", "narutomod:mangekyo_sharingan_7", "narutomod:mangekyo_sharingan_8", "narutomod:mangekyo_sharingan_9", "narutomod:mangekyo_sharingan_10", "narutomod:eternal_mangekyo", "narutomod:eternal_mangekyo_2", "narutomod:eternal_mangekyo_3", "narutomod:tenseigan", "narutomod:tenseigan_helmet", "narutomod:byakurinnesharingan", "narutomod:jougan", "clansaddon:sharingan", "clansaddon:eternal_mangekyo_sharingan", "clansaddon:itachims", "clansaddon:saradams", "clansaddon:obitoms", "clansaddon:kakashims", "clansaddon:sasukems", "clansaddon:madarams", "clansaddon:shisuims", "clansaddon:rinnegan", "clansaddon:rinnegan_deva_path", "clansaddon:rinnegan_asura_path", "clansaddon:rinnegan_human_path", "clansaddon:rinnegan_animal_path", "clansaddon:rinnegan_preta_path", "clansaddon:rinnegan_naraka_path", "clansaddon:rinnegan_outer_path", "clansaddon:tenseigan", "clansaddon:tenseigan_deva_path", "clansaddon:tenseigan_asura_path", "clansaddon:tenseigan_human_path", "clansaddon:tenseigan_animal_path", "clansaddon:tenseigan_preta_path", "clansaddon:tenseigan_naraka_path", "clansaddon:tenseigan_outer_path", "clansaddon:kekkei_mora", "clansaddon:so6p"));
   private static final Set<String> SPECIAL_ITEMS = new HashSet(Arrays.asList("narutomod:ninjutsu", "narutomod:iryo_jutsu", "narutomod:eight_gates", "narutomod:biju_cloak", "narutomod:samehada", "narutomod:kusanagi", "narutomod:kubikiribocho", "narutomod:nuibari", "narutomod:kabutowari", "narutomod:shibuki", "narutomod:hiramekarei", "narutomod:kiba_swords", "narutomod:gunbai", "narutomod:sage_mode", "narutomod:summoning_jutsu", "narutomod:genjutsu", "narutomod:fuinjutsu", "narutomod:puppet_jutsu", "narutomod:senjutsu", "clansaddon:hiraishin_kunai", "clansaddon:hiramekarei", "clansaddon:ashbones", "clansaddon:asura_cannon", "clansaddon:black_receiver", "clansaddon:sage_staff", "clansaddon:eight_gates", "clansaddon:tenseigan_chakra_mode", "clansaddon:raiton_chakra_mode", "clansaddon:uchiha_stone_tablet", "clansaddon:hidden_uchiha_scripture", "clansaddon:chakra_fruit", "clansaddon:path_eye_relic", "clansaddon:ankle_weights", "clansaddon:so6pbody", "clansaddon:so6plegs", "clansaddon:asurapath", "clansaddon:tenseigan_chakra_body", "clansaddon:tenseigan_chakra_legs", "clansaddon:clothes_hokagehelmet", "clansaddon:clothes_hokagebody", "clansaddon:clothes_kazekagehelmet", "clansaddon:clothes_kazekagebody", "clansaddon:clothes_mizukagehelmet", "clansaddon:clothes_mizukagebody", "clansaddon:clothes_raikagehelmet", "clansaddon:clothes_raikagebody", "clansaddon:clothes_tsuchikagehelmet", "clansaddon:clothes_tsuchikagebody", "clansaddon:clothes_amekagehelmet", "clansaddon:clothes_amekagebody"));
   private static final Set<String> DIAMOND_ARMOR_ITEMS = new HashSet(Arrays.asList("minecraft:diamond_helmet", "minecraft:diamond_chestplate", "minecraft:diamond_leggings", "minecraft:diamond_boots"));
   private static final Set<String> EXCLUDED_PREFIXES = new HashSet(Arrays.asList("narutomod:kunai", "narutomod:shuriken", "narutomod:senbon", "narutomod:paper_bomb", "narutomod:smoke_bomb", "narutomod:food_", "narutomod:ramen", "narutomod:headband", "narutomod:flak_jacket"));
   private static final Set<String> EXCLUDED_ITEMS = new HashSet(Arrays.asList("clansaddon:icon_gates", "clansaddon:icon_byakugan", "clansaddon:icon_rinnegan", "clansaddon:icon_mokuton", "clansaddon:icon_sharingan", "clansaddon:icon_ems"));

   private static Class<?> getJutsuBaseClass() {
      if (!jutsuBaseAttempted) {
         jutsuBaseAttempted = true;

         try {
            jutsuBaseClass = Class.forName("net.narutomod.item.ItemJutsu$Base");
         } catch (ClassNotFoundException var1) {
         }
      }

      return jutsuBaseClass;
   }

   public static ItemCategory classifyItem(ItemStack stack) {
      if (stack.isEmpty()) {
         return null;
      } else {
         Item item = stack.getItem();
         ResourceLocation regName = item.getRegistryName();
         if (regName == null) {
            return null;
         } else {
            String fullName = regName.toString();
            String namespace = regName.getNamespace();
            String path = regName.getPath();
            if (namespace.equals("minecraft") && DIAMOND_ARMOR_ITEMS.contains(fullName)) {
               return ItemCategory.ARMOR;
            } else if (!namespace.equals("narutomod") && !namespace.equals("clansaddon")) {
               return null;
            } else if (EXCLUDED_ITEMS.contains(fullName)) {
               return null;
            } else {
               for(String prefix : EXCLUDED_PREFIXES) {
                  if (fullName.startsWith(prefix)) {
                     return null;
                  }
               }

               if (DOJUTSU_ITEMS.contains(fullName)) {
                  return ItemCategory.DOJUTSU;
               } else if (path.contains("scroll_")) {
                  return ItemCategory.SCROLL;
               } else if (SPECIAL_ITEMS.contains(fullName)) {
                  return ItemCategory.SPECIAL;
               } else {
                  Class<?> baseClass = getJutsuBaseClass();
                  if (baseClass != null && baseClass.isAssignableFrom(item.getClass())) {
                     return ItemCategory.NATURE;
                  } else {
                     return null;
                  }
               }
            }
         }
      }
   }

   public static boolean isTrackedItem(ItemStack stack) {
      return classifyItem(stack) != null;
   }

   public static List<TrackedItem> scanPlayer(EntityPlayerMP player) {
      List<TrackedItem> found = new ArrayList();
      Map<String, TrackedItem> dedup = new HashMap();

      for(List<ItemStack> inv : Arrays.asList(player.inventory.mainInventory, player.inventory.armorInventory, player.inventory.offHandInventory)) {
         for(ItemStack stack : inv) {
            if (!stack.isEmpty()) {
               ItemCategory category = classifyItem(stack);
               if (category != null) {
                  ResourceLocation regName = stack.getItem().getRegistryName();
                  if (regName != null) {
                     String key = regName.toString() + ":" + stack.getMetadata() + "|" + stack.getDisplayName();
                     if (!dedup.containsKey(key)) {
                        NBTTagCompound nbtSnapshot = null;
                        if (stack.hasTagCompound()) {
                           nbtSnapshot = stack.getTagCompound().copy();
                        }

                        TrackedItem tracked = new TrackedItem(regName.toString(), stack.getMetadata(), stack.getDisplayName(), nbtSnapshot, category.name());
                        dedup.put(key, tracked);
                        found.add(tracked);
                     }
                  }
               }
            }
         }
      }

      return found;
   }

   public static void scanAndUpdateTracker(EntityPlayerMP player, PlayerItemTracker tracker) {
      UUID playerId = player.getUniqueID();
      List<TrackedItem> scanned = scanPlayer(player);
      Set<String> foundKeys = new HashSet();

      for(TrackedItem scannedItem : scanned) {
         String key = scannedItem.getKey();
         foundKeys.add(key);
         TrackedItem existing = tracker.findTrackedItem(playerId, scannedItem.itemRegistryName, scannedItem.meta, scannedItem.displayName);
         if (existing != null) {
            existing.lastSeenTimestamp = System.currentTimeMillis();
            existing.nbtSnapshot = scannedItem.nbtSnapshot;
            existing.displayName = scannedItem.displayName;
            existing.currentlyOwned = true;
            existing.extractNbtFields();
            tracker.addOrUpdateItem(playerId, existing);
         } else {
            tracker.addOrUpdateItem(playerId, scannedItem);
         }
      }

      tracker.reconcileOwnership(playerId, foundKeys);
   }
}
