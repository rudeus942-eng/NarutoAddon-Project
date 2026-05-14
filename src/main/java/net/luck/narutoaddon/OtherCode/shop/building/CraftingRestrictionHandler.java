
package net.luck.narutoaddon.OtherCode.shop.building;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.HashSet;
import java.util.Set;

public class CraftingRestrictionHandler {
   private static final Set<String> RESTRICTED_ITEMS = new HashSet();
   private static final Set<String> NARUTOMOD_ALLOWED = new HashSet();
   private static final Set<String> BLOCKED_MOD_PREFIXES = new HashSet();
   private static final Set<String> NARUTOMOD_WEAPONS = new HashSet();

   @SubscribeEvent
   public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
      EntityPlayer player = event.player;
      if (player != null && !player.world.isRemote) {
         ItemStack result = event.crafting;
         if (!result.isEmpty()) {
            Item item = result.getItem();
            ResourceLocation regName = item.getRegistryName();
            if (regName != null) {
               String regKey = regName.toString();
               String modId = regName.getNamespace();
               if (!NARUTOMOD_ALLOWED.contains(regKey)) {
                  boolean isRestricted = false;
                  if (RESTRICTED_ITEMS.contains(regKey)) {
                     isRestricted = true;
                  }

                  if (!isRestricted && BLOCKED_MOD_PREFIXES.contains(modId)) {
                     isRestricted = true;
                  }

                  if (!isRestricted && NARUTOMOD_WEAPONS.contains(regKey)) {
                     isRestricted = true;
                  }

                  if (!isRestricted && item instanceof ItemArmor) {
                     isRestricted = true;
                  }

                  if (!isRestricted && item instanceof ItemSword) {
                     isRestricted = true;
                  }

                  if (!isRestricted && item instanceof ItemBow) {
                     isRestricted = true;
                  }

                  if (isRestricted) {
                     if (!isPlayerOpped(player)) {
                        result.setCount(0);
                        player.sendMessage(new TextComponentString("§c[Shop] §7You cannot craft §f" + result.getDisplayName() + "§7. Purchase from the shop or earn through gameplay."));
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean isPlayerOpped(EntityPlayer player) {
      return player.canUseCommand(2, "op");
   }

   static {
      RESTRICTED_ITEMS.add("minecraft:enchanting_table");
      RESTRICTED_ITEMS.add("minecraft:repeater");
      RESTRICTED_ITEMS.add("minecraft:comparator");
      RESTRICTED_ITEMS.add("minecraft:piston");
      RESTRICTED_ITEMS.add("minecraft:sticky_piston");
      RESTRICTED_ITEMS.add("minecraft:dispenser");
      RESTRICTED_ITEMS.add("minecraft:dropper");
      RESTRICTED_ITEMS.add("minecraft:observer");
      RESTRICTED_ITEMS.add("minecraft:hopper");
      RESTRICTED_ITEMS.add("minecraft:daylight_detector");
      RESTRICTED_ITEMS.add("minecraft:noteblock");
      RESTRICTED_ITEMS.add("minecraft:tnt");
      RESTRICTED_ITEMS.add("minecraft:redstone_torch");
      RESTRICTED_ITEMS.add("minecraft:redstone_lamp");
      RESTRICTED_ITEMS.add("minecraft:redstone_block");
      RESTRICTED_ITEMS.add("minecraft:redstone");
      RESTRICTED_ITEMS.add("minecraft:lever");
      RESTRICTED_ITEMS.add("minecraft:tripwire_hook");
      RESTRICTED_ITEMS.add("minecraft:trapped_chest");
      RESTRICTED_ITEMS.add("minecraft:item_frame");
      RESTRICTED_ITEMS.add("minecraft:painting");
      RESTRICTED_ITEMS.add("minecraft:armor_stand");
      RESTRICTED_ITEMS.add("minecraft:wooden_sword");
      RESTRICTED_ITEMS.add("minecraft:stone_sword");
      RESTRICTED_ITEMS.add("minecraft:iron_sword");
      RESTRICTED_ITEMS.add("minecraft:golden_sword");
      RESTRICTED_ITEMS.add("minecraft:diamond_sword");
      RESTRICTED_ITEMS.add("minecraft:bow");
      RESTRICTED_ITEMS.add("minecraft:shield");
      String[] armorMaterials = new String[]{"leather", "chainmail", "iron", "golden", "diamond"};
      String[] armorPieces = new String[]{"helmet", "chestplate", "leggings", "boots"};

      for(String mat : armorMaterials) {
         for(String piece : armorPieces) {
            RESTRICTED_ITEMS.add("minecraft:" + mat + "_" + piece);
         }
      }

      BLOCKED_MOD_PREFIXES.add("armourersworkshop");
      NARUTOMOD_ALLOWED.add("narutomod:shuriken");
      NARUTOMOD_ALLOWED.add("narutomod:kunai");
      NARUTOMOD_WEAPONS.add("narutomod:kunai_blade");
      NARUTOMOD_WEAPONS.add("narutomod:katana");
      NARUTOMOD_WEAPONS.add("narutomod:cleaver");
      NARUTOMOD_WEAPONS.add("narutomod:claw");
      NARUTOMOD_WEAPONS.add("narutomod:chokuto");
      NARUTOMOD_WEAPONS.add("narutomod:anbu_sword");
      NARUTOMOD_WEAPONS.add("narutomod:kunai_3prong");
      NARUTOMOD_WEAPONS.add("narutomod:zabuza_sword");
      NARUTOMOD_WEAPONS.add("narutomod:samehada");
      NARUTOMOD_WEAPONS.add("narutomod:hiramekarei_sword");
      NARUTOMOD_WEAPONS.add("narutomod:shibuki_sword");
      NARUTOMOD_WEAPONS.add("narutomod:bone_sword");
      NARUTOMOD_WEAPONS.add("narutomod:bone_drill");
      NARUTOMOD_WEAPONS.add("narutomod:chakra_blades");
      NARUTOMOD_WEAPONS.add("narutomod:yagura_staff");
      NARUTOMOD_WEAPONS.add("narutomod:ishiken");
      NARUTOMOD_WEAPONS.add("narutomod:spear_retractable");
      NARUTOMOD_WEAPONS.add("narutomod:scythe_hidan");
      NARUTOMOD_WEAPONS.add("narutomod:scythe_madara");
      NARUTOMOD_WEAPONS.add("narutomod:fuma_shuriken");
      NARUTOMOD_WEAPONS.add("narutomod:gauntlet");
      NARUTOMOD_WEAPONS.add("narutomod:gauntlet_left");
      NARUTOMOD_WEAPONS.add("narutomod:gauntlet_right");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_war_1helmet");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_konohabody");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_konohalegs");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_sunahelmet");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_sunabody");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_sunalegs");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_kumohelmet");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_kumobody");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_kumolegs");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_kirihelmet");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_kiribody");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_kirilegs");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_iwahelmet");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_iwabody");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_iwalegs");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_amehelmet");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_amelegs");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_fishnetslegs");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_naruto_sbody");
      NARUTOMOD_WEAPONS.add("narutomod:ninja_armor_naruto_slegs");
      NARUTOMOD_WEAPONS.add("narutomod:steam_armorhelmet");
      NARUTOMOD_WEAPONS.add("narutomod:steam_armorbody");
      NARUTOMOD_WEAPONS.add("narutomod:steam_armorlegs");
      NARUTOMOD_WEAPONS.add("kabutoaddon:katana");
      NARUTOMOD_WEAPONS.add("clansaddon:hiramekarei");
      NARUTOMOD_WEAPONS.add("clansaddon:sage_staff");
      NARUTOMOD_WEAPONS.add("clansaddon:ashbones");
      NARUTOMOD_WEAPONS.add("clansaddon:asura_cannon");
      NARUTOMOD_WEAPONS.add("clansaddon:black_receiver");
      NARUTOMOD_WEAPONS.add("clansaddon:hiraishin_kunai");
   }
}
