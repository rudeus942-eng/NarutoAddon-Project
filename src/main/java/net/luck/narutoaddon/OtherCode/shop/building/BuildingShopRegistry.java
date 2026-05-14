package net.luck.narutoaddon.OtherCode.shop.building;

import java.util.*;

public class BuildingShopRegistry {
   private static final List<BuildingItem> ALL_ITEMS = new ArrayList();
   private static final Map<BuildingCategory, List<BuildingItem>> BY_CATEGORY = new EnumMap(BuildingCategory.class);
   private static boolean initialized = false;
   private static final String[] COLOR_NAMES = new String[]{"White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime", "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"};

   public static void init() {
      if (!initialized) {
         initialized = true;

         for(BuildingCategory cat : BuildingCategory.values()) {
            BY_CATEGORY.put(cat, new ArrayList());
         }

         registerVanillaBuildingBlocks();
         registerVanillaDecorations();
         registerVanillaMiscellaneous();
         registerVanillaEntryway();
         registerAdditionalLanterns();
         registerMacawFurniture();
         registerVariedCommodities();
         registerTools();
         System.out.println("[BuildingShop] Registered " + ALL_ITEMS.size() + " building items across " + BuildingCategory.values().length + " categories.");
      }
   }

   public static List<BuildingItem> getItems(BuildingCategory category) {
      if (!initialized) {
         init();
      }

      List<BuildingItem> list = (List)BY_CATEGORY.get(category);
      return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
   }

   public static List<BuildingItem> getAllItems() {
      return Collections.unmodifiableList(ALL_ITEMS);
   }

   public static BuildingItem findItem(String registryName, int meta) {
      if (!initialized) {
         init();
      }

      for(BuildingItem item : ALL_ITEMS) {
         if (item.registryName.equals(registryName) && item.meta == meta) {
            return item;
         }
      }

      return null;
   }

   private static void reg(String registryName, int meta, String displayName, int ryoPrice, BuildingCategory category) {
      BuildingItem item = new BuildingItem(registryName, meta, displayName, ryoPrice, category);
      ALL_ITEMS.add(item);
      ((List)BY_CATEGORY.get(category)).add(item);
   }

   private static void reg(String registryName, String displayName, int ryoPrice, BuildingCategory category) {
      reg(registryName, 0, displayName, ryoPrice, category);
   }

   private static void registerVanillaBuildingBlocks() {
      BuildingCategory cat = BuildingCategory.BUILDING_BLOCKS;
      reg("minecraft:stone", 0, "Stone", 3, cat);
      reg("minecraft:stone", 1, "Granite", 3, cat);
      reg("minecraft:stone", 2, "Polished Granite", 4, cat);
      reg("minecraft:stone", 3, "Diorite", 3, cat);
      reg("minecraft:stone", 4, "Polished Diorite", 4, cat);
      reg("minecraft:stone", 5, "Andesite", 3, cat);
      reg("minecraft:stone", 6, "Polished Andesite", 4, cat);
      reg("minecraft:dirt", 0, "Dirt", 2, cat);
      reg("minecraft:dirt", 1, "Coarse Dirt", 2, cat);
      reg("minecraft:dirt", 2, "Podzol", 5, cat);
      reg("minecraft:grass", "Grass Block", 4, cat);
      reg("minecraft:mycelium", "Mycelium", 6, cat);
      reg("minecraft:cobblestone", "Cobblestone", 2, cat);
      reg("minecraft:mossy_cobblestone", "Mossy Cobblestone", 5, cat);
      reg("minecraft:planks", 0, "Oak Planks", 3, cat);
      reg("minecraft:planks", 1, "Spruce Planks", 3, cat);
      reg("minecraft:planks", 2, "Birch Planks", 3, cat);
      reg("minecraft:planks", 3, "Jungle Planks", 3, cat);
      reg("minecraft:planks", 4, "Acacia Planks", 3, cat);
      reg("minecraft:planks", 5, "Dark Oak Planks", 3, cat);
      reg("minecraft:log", 0, "Oak Log", 8, cat);
      reg("minecraft:log", 1, "Spruce Log", 8, cat);
      reg("minecraft:log", 2, "Birch Log", 8, cat);
      reg("minecraft:log", 3, "Jungle Log", 8, cat);
      reg("minecraft:log2", 0, "Acacia Log", 8, cat);
      reg("minecraft:log2", 1, "Dark Oak Log", 8, cat);
      reg("minecraft:sand", 0, "Sand", 2, cat);
      reg("minecraft:sand", 1, "Red Sand", 3, cat);
      reg("minecraft:gravel", "Gravel", 2, cat);
      reg("minecraft:sandstone", 0, "Sandstone", 10, cat);
      reg("minecraft:sandstone", 1, "Chiseled Sandstone", 12, cat);
      reg("minecraft:sandstone", 2, "Smooth Sandstone", 12, cat);
      reg("minecraft:red_sandstone", 0, "Red Sandstone", 14, cat);
      reg("minecraft:red_sandstone", 1, "Chiseled Red Sandstone", 16, cat);
      reg("minecraft:red_sandstone", 2, "Smooth Red Sandstone", 16, cat);
      reg("minecraft:brick_block", "Bricks", 18, cat);
      reg("minecraft:stonebrick", 0, "Stone Bricks", 4, cat);
      reg("minecraft:stonebrick", 1, "Mossy Stone Bricks", 6, cat);
      reg("minecraft:stonebrick", 2, "Cracked Stone Bricks", 5, cat);
      reg("minecraft:stonebrick", 3, "Chiseled Stone Bricks", 6, cat);
      reg("minecraft:nether_brick", "Nether Bricks", 22, cat);
      reg("minecraft:red_nether_brick", "Red Nether Bricks", 25, cat);
      reg("minecraft:quartz_block", 0, "Quartz Block", 35, cat);
      reg("minecraft:quartz_block", 1, "Chiseled Quartz", 38, cat);
      reg("minecraft:quartz_block", 2, "Pillar Quartz", 38, cat);
      reg("minecraft:purpur_block", "Purpur Block", 50, cat);
      reg("minecraft:purpur_pillar", "Purpur Pillar", 55, cat);
      reg("minecraft:prismarine", 0, "Prismarine", 35, cat);
      reg("minecraft:prismarine", 1, "Prismarine Bricks", 75, cat);
      reg("minecraft:prismarine", 2, "Dark Prismarine", 72, cat);
      reg("minecraft:end_stone", "End Stone", 30, cat);
      reg("minecraft:end_bricks", "End Stone Bricks", 35, cat);

      for(int i = 0; i < 16; ++i) {
         reg("minecraft:wool", i, COLOR_NAMES[i] + " Wool", 5, cat);
      }

      reg("minecraft:hardened_clay", "Terracotta", 5, cat);

      for(int i = 0; i < 16; ++i) {
         reg("minecraft:stained_hardened_clay", i, COLOR_NAMES[i] + " Terracotta", 7, cat);
      }

      reg("minecraft:white_glazed_terracotta", "White Glazed Terracotta", 8, cat);
      reg("minecraft:orange_glazed_terracotta", "Orange Glazed Terracotta", 8, cat);
      reg("minecraft:magenta_glazed_terracotta", "Magenta Glazed Terracotta", 8, cat);
      reg("minecraft:light_blue_glazed_terracotta", "Light Blue Glazed Terracotta", 8, cat);
      reg("minecraft:yellow_glazed_terracotta", "Yellow Glazed Terracotta", 8, cat);
      reg("minecraft:lime_glazed_terracotta", "Lime Glazed Terracotta", 8, cat);
      reg("minecraft:pink_glazed_terracotta", "Pink Glazed Terracotta", 8, cat);
      reg("minecraft:gray_glazed_terracotta", "Gray Glazed Terracotta", 8, cat);
      reg("minecraft:silver_glazed_terracotta", "Light Gray Glazed Terracotta", 8, cat);
      reg("minecraft:cyan_glazed_terracotta", "Cyan Glazed Terracotta", 8, cat);
      reg("minecraft:purple_glazed_terracotta", "Purple Glazed Terracotta", 8, cat);
      reg("minecraft:blue_glazed_terracotta", "Blue Glazed Terracotta", 8, cat);
      reg("minecraft:brown_glazed_terracotta", "Brown Glazed Terracotta", 8, cat);
      reg("minecraft:green_glazed_terracotta", "Green Glazed Terracotta", 8, cat);
      reg("minecraft:red_glazed_terracotta", "Red Glazed Terracotta", 8, cat);
      reg("minecraft:black_glazed_terracotta", "Black Glazed Terracotta", 8, cat);

      for(int i = 0; i < 16; ++i) {
         reg("minecraft:concrete", i, COLOR_NAMES[i] + " Concrete", 8, cat);
      }

      for(int i = 0; i < 16; ++i) {
         reg("minecraft:concrete_powder", i, COLOR_NAMES[i] + " Concrete Powder", 6, cat);
      }

      reg("minecraft:glass", "Glass", 4, cat);

      for(int i = 0; i < 16; ++i) {
         reg("minecraft:stained_glass", i, COLOR_NAMES[i] + " Stained Glass", 5, cat);
      }

      reg("minecraft:glass_pane", "Glass Pane", 2, cat);

      for(int i = 0; i < 16; ++i) {
         reg("minecraft:stained_glass_pane", i, COLOR_NAMES[i] + " Stained Glass Pane", 2, cat);
      }

      reg("minecraft:clay", "Clay Block", 12, cat);
      reg("minecraft:coal_ore", "Coal Ore", 15, cat);
      reg("minecraft:iron_ore", "Iron Ore", 20, cat);
      reg("minecraft:gold_ore", "Gold Ore", 30, cat);
      reg("minecraft:diamond_ore", "Diamond Ore", 55, cat);
      reg("minecraft:emerald_ore", "Emerald Ore", 45, cat);
      reg("minecraft:lapis_ore", "Lapis Lazuli Ore", 25, cat);
      reg("minecraft:redstone_ore", "Redstone Ore", 20, cat);
      reg("minecraft:quartz_ore", "Nether Quartz Ore", 25, cat);
      reg("minecraft:iron_block", "Iron Block", 95, cat);
      reg("minecraft:gold_block", "Gold Block", 140, cat);
      reg("minecraft:diamond_block", "Diamond Block", 460, cat);
      reg("minecraft:emerald_block", "Emerald Block", 370, cat);
      reg("minecraft:lapis_block", "Lapis Lazuli Block", 75, cat);
      reg("minecraft:coal_block", "Coal Block", 30, cat);
      reg("minecraft:obsidian", "Obsidian", 25, cat);
      reg("minecraft:glowstone", "Glowstone", 25, cat);
      reg("minecraft:sea_lantern", "Sea Lantern", 85, cat);
      reg("minecraft:bone_block", "Bone Block", 28, cat);
      reg("minecraft:magma", "Magma Block", 20, cat);
      reg("minecraft:hay_block", "Hay Bale", 20, cat);
      reg("minecraft:packed_ice", "Packed Ice", 15, cat);
      reg("minecraft:ice", "Ice", 10, cat);
      reg("minecraft:snow", "Snow Block", 5, cat);
      reg("minecraft:netherrack", "Netherrack", 3, cat);
      reg("minecraft:soul_sand", "Soul Sand", 5, cat);
      reg("minecraft:nether_wart_block", "Nether Wart Block", 20, cat);
      reg("minecraft:sponge", 0, "Sponge", 40, cat);
      reg("minecraft:sponge", 1, "Wet Sponge", 40, cat);
      reg("minecraft:stone_stairs", "Cobblestone Stairs", 4, cat);
      reg("minecraft:brick_stairs", "Brick Stairs", 30, cat);
      reg("minecraft:stone_brick_stairs", "Stone Brick Stairs", 5, cat);
      reg("minecraft:nether_brick_stairs", "Nether Brick Stairs", 35, cat);
      reg("minecraft:sandstone_stairs", "Sandstone Stairs", 18, cat);
      reg("minecraft:red_sandstone_stairs", "Red Sandstone Stairs", 22, cat);
      reg("minecraft:quartz_stairs", "Quartz Stairs", 55, cat);
      reg("minecraft:purpur_stairs", "Purpur Stairs", 80, cat);
      reg("minecraft:oak_stairs", "Oak Stairs", 5, cat);
      reg("minecraft:spruce_stairs", "Spruce Stairs", 5, cat);
      reg("minecraft:birch_stairs", "Birch Stairs", 5, cat);
      reg("minecraft:jungle_stairs", "Jungle Stairs", 5, cat);
      reg("minecraft:acacia_stairs", "Acacia Stairs", 5, cat);
      reg("minecraft:dark_oak_stairs", "Dark Oak Stairs", 5, cat);
      reg("minecraft:stone_slab", 0, "Stone Slab", 2, cat);
      reg("minecraft:stone_slab", 1, "Sandstone Slab", 6, cat);
      reg("minecraft:stone_slab", 3, "Cobblestone Slab", 2, cat);
      reg("minecraft:stone_slab", 4, "Brick Slab", 10, cat);
      reg("minecraft:stone_slab", 5, "Stone Brick Slab", 3, cat);
      reg("minecraft:stone_slab", 6, "Nether Brick Slab", 12, cat);
      reg("minecraft:stone_slab", 7, "Quartz Slab", 20, cat);
      reg("minecraft:stone_slab2", 0, "Red Sandstone Slab", 8, cat);
      reg("minecraft:purpur_slab", "Purpur Slab", 28, cat);
      reg("minecraft:wooden_slab", 0, "Oak Slab", 2, cat);
      reg("minecraft:wooden_slab", 1, "Spruce Slab", 2, cat);
      reg("minecraft:wooden_slab", 2, "Birch Slab", 2, cat);
      reg("minecraft:wooden_slab", 3, "Jungle Slab", 2, cat);
      reg("minecraft:wooden_slab", 4, "Acacia Slab", 2, cat);
      reg("minecraft:wooden_slab", 5, "Dark Oak Slab", 2, cat);
      reg("minecraft:cobblestone_wall", 0, "Cobblestone Wall", 3, cat);
      reg("minecraft:cobblestone_wall", 1, "Mossy Cobblestone Wall", 5, cat);
   }

   private static void registerVanillaDecorations() {
      BuildingCategory cat = BuildingCategory.DECORATIONS;
      reg("minecraft:torch", "Torch", 2, cat);
      reg("minecraft:red_flower", 0, "Poppy", 5, cat);
      reg("minecraft:red_flower", 1, "Blue Orchid", 5, cat);
      reg("minecraft:red_flower", 2, "Allium", 5, cat);
      reg("minecraft:red_flower", 3, "Azure Bluet", 5, cat);
      reg("minecraft:red_flower", 4, "Red Tulip", 5, cat);
      reg("minecraft:red_flower", 5, "Orange Tulip", 5, cat);
      reg("minecraft:red_flower", 6, "White Tulip", 5, cat);
      reg("minecraft:red_flower", 7, "Pink Tulip", 5, cat);
      reg("minecraft:red_flower", 8, "Oxeye Daisy", 5, cat);
      reg("minecraft:yellow_flower", "Dandelion", 5, cat);
      reg("minecraft:double_plant", 0, "Sunflower", 8, cat);
      reg("minecraft:double_plant", 1, "Lilac", 8, cat);
      reg("minecraft:double_plant", 4, "Rose Bush", 8, cat);
      reg("minecraft:double_plant", 5, "Peony", 8, cat);
      reg("minecraft:tallgrass", 1, "Tall Grass", 3, cat);
      reg("minecraft:tallgrass", 2, "Fern", 5, cat);
      reg("minecraft:double_plant", 2, "Double Tallgrass", 5, cat);
      reg("minecraft:double_plant", 3, "Large Fern", 8, cat);
      reg("minecraft:deadbush", "Dead Bush", 5, cat);
      reg("minecraft:sapling", 0, "Oak Sapling", 8, cat);
      reg("minecraft:sapling", 1, "Spruce Sapling", 8, cat);
      reg("minecraft:sapling", 2, "Birch Sapling", 8, cat);
      reg("minecraft:sapling", 3, "Jungle Sapling", 8, cat);
      reg("minecraft:sapling", 4, "Acacia Sapling", 8, cat);
      reg("minecraft:sapling", 5, "Dark Oak Sapling", 8, cat);
      reg("minecraft:vine", "Vines", 5, cat);
      reg("minecraft:waterlily", "Lily Pad", 8, cat);
      reg("minecraft:cactus", "Cactus", 8, cat);
      reg("minecraft:reeds", "Sugar Cane", 5, cat);
      reg("minecraft:flower_pot", "Flower Pot", 13, cat);

      for(int i = 0; i < 16; ++i) {
         reg("minecraft:carpet", i, COLOR_NAMES[i] + " Carpet", 4, cat);
      }

      for(int i = 0; i < 16; ++i) {
         reg("minecraft:banner", i, COLOR_NAMES[i] + " Banner", 15, cat);
      }

      reg("minecraft:sign", "Sign", 7, cat);
      reg("minecraft:ladder", "Ladder", 2, cat);
      reg("minecraft:web", "Cobweb", 15, cat);
      reg("minecraft:bookshelf", "Bookshelf", 25, cat);
      reg("minecraft:lit_pumpkin", "Jack o'Lantern", 15, cat);
      reg("minecraft:pumpkin", "Pumpkin", 10, cat);
      reg("minecraft:melon_block", "Melon Block", 12, cat);
      reg("minecraft:cauldron", "Cauldron", 75, cat);
      reg("minecraft:experience_bottle", "Bottle o' Enchanting", 50, cat);
      String[] shulkerColors = new String[]{"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "silver", "cyan", "purple", "blue", "brown", "green", "red", "black"};

      for(int i = 0; i < 16; ++i) {
         reg("minecraft:" + shulkerColors[i] + "_shulker_box", 0, COLOR_NAMES[i] + " Shulker Box", 500, cat);
      }

      reg("minecraft:chest", "Chest", 12, cat);
      reg("minecraft:ender_chest", "Ender Chest", 500, cat);
      reg("minecraft:leaves", 0, "Oak Leaves", 5, cat);
      reg("minecraft:leaves", 1, "Spruce Leaves", 5, cat);
      reg("minecraft:leaves", 2, "Birch Leaves", 5, cat);
      reg("minecraft:leaves", 3, "Jungle Leaves", 5, cat);
      reg("minecraft:leaves2", 0, "Acacia Leaves", 5, cat);
      reg("minecraft:leaves2", 1, "Dark Oak Leaves", 5, cat);
   }

   private static void registerVanillaMiscellaneous() {
      BuildingCategory cat = BuildingCategory.MISCELLANEOUS;
      reg("minecraft:experience_bottle", "Bottle o' Enchanting", 50, cat);
      String[] dyeNames = new String[]{"Ink Sac", "Red Dye", "Green Dye", "Cocoa Beans", "Lapis Lazuli", "Purple Dye", "Cyan Dye", "Light Gray Dye", "Gray Dye", "Pink Dye", "Lime Dye", "Yellow Dye", "Light Blue Dye", "Magenta Dye", "Orange Dye", "Bone Meal"};

      for(int i = 0; i < 16; ++i) {
         reg("minecraft:dye", i, dyeNames[i], 5, cat);
      }

      reg("minecraft:bucket", "Bucket", 35, cat);
      reg("minecraft:stick", "Stick", 1, cat);
      reg("minecraft:string", "String", 3, cat);
      reg("minecraft:feather", "Feather", 3, cat);
      reg("minecraft:leather", "Leather", 8, cat);
      reg("minecraft:iron_ingot", "Iron Ingot", 10, cat);
      reg("minecraft:gold_ingot", "Gold Ingot", 15, cat);
      reg("minecraft:diamond", "Diamond", 50, cat);
      reg("minecraft:emerald", "Emerald", 40, cat);
      reg("minecraft:coal", 0, "Coal", 3, cat);
      reg("minecraft:coal", 1, "Charcoal", 3, cat);
      reg("minecraft:flint", "Flint", 3, cat);
      reg("minecraft:clay_ball", "Clay Ball", 3, cat);
      reg("minecraft:brick", "Brick", 4, cat);
      reg("minecraft:nether_brick", "Nether Brick", 5, cat);
      reg("minecraft:quartz", "Nether Quartz", 8, cat);
      reg("minecraft:prismarine_shard", "Prismarine Shard", 8, cat);
      reg("minecraft:prismarine_crystals", "Prismarine Crystals", 10, cat);
      reg("minecraft:glowstone_dust", "Glowstone Dust", 6, cat);
      reg("minecraft:gunpowder", "Gunpowder", 5, cat);
      reg("minecraft:bone", "Bone", 3, cat);
      reg("minecraft:crafting_table", "Crafting Table", 5, cat);
      reg("minecraft:furnace", "Furnace", 10, cat);
      reg("minecraft:chest", "Chest", 12, cat);
      reg("minecraft:torch", "Torch", 2, cat);
      reg("minecraft:bed", 0, "White Bed", 15, cat);
      reg("minecraft:bed", 1, "Orange Bed", 15, cat);
      reg("minecraft:bed", 2, "Magenta Bed", 15, cat);
      reg("minecraft:bed", 3, "Light Blue Bed", 15, cat);
      reg("minecraft:bed", 4, "Yellow Bed", 15, cat);
      reg("minecraft:bed", 5, "Lime Bed", 15, cat);
      reg("minecraft:bed", 6, "Pink Bed", 15, cat);
      reg("minecraft:bed", 7, "Gray Bed", 15, cat);
      reg("minecraft:bed", 8, "Light Gray Bed", 15, cat);
      reg("minecraft:bed", 9, "Cyan Bed", 15, cat);
      reg("minecraft:bed", 10, "Purple Bed", 15, cat);
      reg("minecraft:bed", 11, "Blue Bed", 15, cat);
      reg("minecraft:bed", 12, "Brown Bed", 15, cat);
      reg("minecraft:bed", 13, "Green Bed", 15, cat);
      reg("minecraft:bed", 14, "Red Bed", 15, cat);
      reg("minecraft:bed", 15, "Black Bed", 15, cat);
   }

   private static void registerVanillaEntryway() {
      BuildingCategory cat = BuildingCategory.ENTRYWAY;
      reg("minecraft:wooden_door", "Oak Door", 8, cat);
      reg("minecraft:spruce_door", "Spruce Door", 8, cat);
      reg("minecraft:birch_door", "Birch Door", 8, cat);
      reg("minecraft:jungle_door", "Jungle Door", 8, cat);
      reg("minecraft:acacia_door", "Acacia Door", 8, cat);
      reg("minecraft:dark_oak_door", "Dark Oak Door", 8, cat);
      reg("minecraft:iron_door", "Iron Door", 65, cat);
      reg("minecraft:trapdoor", "Oak Trapdoor", 6, cat);
      reg("minecraft:iron_trapdoor", "Iron Trapdoor", 45, cat);
      reg("minecraft:fence_gate", "Oak Fence Gate", 8, cat);
      reg("minecraft:spruce_fence_gate", "Spruce Fence Gate", 8, cat);
      reg("minecraft:birch_fence_gate", "Birch Fence Gate", 8, cat);
      reg("minecraft:jungle_fence_gate", "Jungle Fence Gate", 8, cat);
      reg("minecraft:acacia_fence_gate", "Acacia Fence Gate", 8, cat);
      reg("minecraft:dark_oak_fence_gate", "Dark Oak Fence Gate", 8, cat);
      reg("minecraft:fence", "Oak Fence", 5, cat);
      reg("minecraft:spruce_fence", "Spruce Fence", 5, cat);
      reg("minecraft:birch_fence", "Birch Fence", 5, cat);
      reg("minecraft:jungle_fence", "Jungle Fence", 5, cat);
      reg("minecraft:acacia_fence", "Acacia Fence", 5, cat);
      reg("minecraft:dark_oak_fence", "Dark Oak Fence", 5, cat);
      reg("minecraft:nether_brick_fence", "Nether Brick Fence", 8, cat);
      reg("minecraft:cobblestone_wall", 0, "Cobblestone Wall", 3, cat);
      reg("minecraft:cobblestone_wall", 1, "Mossy Cobblestone Wall", 5, cat);
      reg("minecraft:iron_bars", "Iron Bars", 5, cat);
   }

   private static void registerAdditionalLanterns() {
      BuildingCategory cat = BuildingCategory.ADDITIONAL_LANTERNS;
      String[] materials = new String[]{"normal", "andesite", "bone", "bricks", "cobblestone", "dark_prismarine", "diamond", "diorite", "emerald", "end_stone", "gold", "granite", "iron", "mossy_cobblestone", "normal_nether_bricks", "normal_sandstone", "obsidian", "prismarine", "purpur", "quartz", "red_nether_bricks", "red_sandstone", "smooth_stone", "stone_bricks"};
      String[] lanternColors = new String[]{"", "black", "blue", "brown", "cyan", "gray", "green", "light_blue", "light_gray", "lime", "magenta", "orange", "pink", "purple", "red", "white", "yellow"};
      String[] colorDisplayNames = new String[]{"", "Black", "Blue", "Brown", "Cyan", "Gray", "Green", "Light Blue", "Light Gray", "Lime", "Magenta", "Orange", "Pink", "Purple", "Red", "White", "Yellow"};
      String[] materialDisplayNames = new String[]{"Normal", "Andesite", "Bone", "Bricks", "Cobblestone", "Dark Prismarine", "Diamond", "Diorite", "Emerald", "End Stone", "Gold", "Granite", "Iron", "Mossy Cobblestone", "Nether Bricks", "Sandstone", "Obsidian", "Prismarine", "Purpur", "Quartz", "Red Nether Bricks", "Red Sandstone", "Smooth Stone", "Stone Bricks"};

      for(int m = 0; m < materials.length; ++m) {
         for(int c = 0; c < lanternColors.length; ++c) {
            String regName;
            String dispName;
            if (lanternColors[c].isEmpty()) {
               regName = "additionallanterns:" + materials[m] + "_lantern";
               dispName = materialDisplayNames[m] + " Lantern";
            } else {
               regName = "additionallanterns:" + lanternColors[c] + "_" + materials[m] + "_lantern";
               dispName = colorDisplayNames[c] + " " + materialDisplayNames[m] + " Lantern";
            }

            int price;
            switch (materials[m]) {
               case "diamond":
               case "emerald":
               case "gold":
                  price = 130;
                  break;
               case "dark_prismarine":
               case "prismarine":
               case "purpur":
               case "obsidian":
                  price = 110;
                  break;
               case "quartz":
               case "end_stone":
                  price = 100;
                  break;
               default:
                  price = 85;
            }

            reg(regName, 0, dispName, price, cat);
         }
      }

      for(int m = 0; m < materials.length; ++m) {
         String regName = "additionallanterns:" + materials[m] + "_chain";
         String dispName = materialDisplayNames[m] + " Chain";
         reg(regName, 0, dispName, 55, cat);
      }

   }

   private static void registerMacawFurniture() {
      BuildingCategory cat = BuildingCategory.MACAW_FURNITURE;
      reg("mcwfurnitures:acacia_box", 0, "Acacia Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:acacia_box_2", 0, "Acacia Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:acacia_cupboard", 0, "Acacia Cupboard", 190, cat);
      reg("mcwfurnitures:acacia_cupboard_2", 0, "Acacia Modern Cupboard", 190, cat);
      reg("mcwfurnitures:acacia_cupboard_3", 0, "Acacia Bookshelf", 190, cat);
      reg("mcwfurnitures:acacia_cupboard_4", 0, "Acacia Complex Cupboard", 190, cat);
      reg("mcwfurnitures:acacia_cupboard_5", 0, "Acacia Double Cupboard", 190, cat);
      reg("mcwfurnitures:acacia_cupboard_6", 0, "Acacia Cupboard", 190, cat);
      reg("mcwfurnitures:acacia_cupboard_7", 0, "Acacia Modern Cupboard", 190, cat);
      reg("mcwfurnitures:acacia_cupboard_8", 0, "Acacia Tall Bookshelf", 190, cat);
      reg("mcwfurnitures:acacia_cupboard_9", 0, "Acacia Complex Cupboard", 190, cat);
      reg("mcwfurnitures:acacia_desk", 0, "Acacia Drawer Desk", 145, cat);
      reg("mcwfurnitures:acacia_desk4", 0, "Acacia Table", 145, cat);
      reg("mcwfurnitures:acacia_desk_2", 0, "Acacia Cupboard Desk", 145, cat);
      reg("mcwfurnitures:acacia_desk_3", 0, "Acacia Covered Desk", 145, cat);
      reg("mcwfurnitures:acacia_desk_5", 0, "Acacia Cupboard Desk", 145, cat);
      reg("mcwfurnitures:acacia_desk_6", 0, "Acacia Drawer Desk", 145, cat);
      reg("mcwfurnitures:acacia_desk_7", 0, "Acacia Double Table", 145, cat);
      reg("mcwfurnitures:acacia_desk_8", 0, "Acacia Modern Table", 145, cat);
      reg("mcwfurnitures:acacia_desk_9", 0, "Acacia Glass Table", 145, cat);
      reg("mcwfurnitures:acacia_dresser", 0, "Acacia Classic Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_10", 0, "Acacia Bookshelf Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_11", 0, "Acacia Bookcase Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_12", 0, "Acacia Complex Shelving unit", 160, cat);
      reg("mcwfurnitures:acacia_dresser_13", 0, "Acacia Drawer Shelving unit", 160, cat);
      reg("mcwfurnitures:acacia_dresser_14", 0, "Acacia Empty Shelving unit", 160, cat);
      reg("mcwfurnitures:acacia_dresser_15", 0, "Acacia Sapling Cabinet Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_16", 0, "Acacia Sapling Cabinet Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_17", 0, "Acacia Sapling Drawer Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_18", 0, "Acacia Sapling Drawer Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_3", 0, "Acacia Double Drawer Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_4", 0, "Acacia Triple Drawer Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_5", 0, "Acacia Triple Drawer Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_6", 0, "Acacia Bookcase Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_7", 0, "Acacia Complex Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_8", 0, "Acacia Complex Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_9", 0, "Acacia Bookshelf Dresser", 160, cat);
      reg("mcwfurnitures:acacia_dresser_box", 0, "Acacia Cupboard Dresser", 160, cat);
      reg("mcwfurnitures:acacia_furniture_1", 0, "Acacia Wardrobe", 210, cat);
      reg("mcwfurnitures:acacia_furniture_2", 0, "Acacia Modern Wardrobe", 210, cat);
      reg("mcwfurnitures:acacia_furniture_3", 0, "Acacia Complex Wardrobe", 210, cat);
      reg("mcwfurnitures:acacia_furniture_4", 0, "Acacia Display Wardrobe", 210, cat);
      reg("mcwfurnitures:acacia_furniture_5", 0, "Acacia Shelf Wardrobe", 210, cat);
      reg("mcwfurnitures:acacia_furniture_6", 0, "Acacia Cupboard Wardrobe", 210, cat);
      reg("mcwfurnitures:acacia_furniture_7", 0, "Acacia Tall Bookshelf", 210, cat);
      reg("mcwfurnitures:acacia_furniture_8", 0, "Acacia Display Wardrobe", 210, cat);
      reg("mcwfurnitures:acacia_furniture_9", 0, "Acacia Complex Wardrobe", 210, cat);
      reg("mcwfurnitures:acacia_nightstand", 0, "Acacia Drawer Nightstand", 160, cat);
      reg("mcwfurnitures:acacia_nightstand_10", 0, "Acacia End Table", 160, cat);
      reg("mcwfurnitures:acacia_nightstand_11", 0, "Acacia End Table", 160, cat);
      reg("mcwfurnitures:acacia_nightstand_2", 0, "Acacia Double Drawer Nightstand", 160, cat);
      reg("mcwfurnitures:acacia_nightstand_3", 0, "Acacia Bookcase Nightstand", 160, cat);
      reg("mcwfurnitures:acacia_nightstand_4", 0, "Acacia Double Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:acacia_nightstand_5", 0, "Acacia Drawer Cupboard", 160, cat);
      reg("mcwfurnitures:acacia_nightstand_6", 0, "Acacia Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:acacia_nightstand_7", 0, "Acacia Glass Cabinet", 160, cat);
      reg("mcwfurnitures:acacia_nightstand_8", 0, "Acacia Glass Cabinet", 160, cat);
      reg("mcwfurnitures:acacia_nightstand_9", 0, "Acacia End Table", 160, cat);
      reg("mcwfurnitures:acacia_pult", 0, "Acacia Kitchen Counter", 190, cat);
      reg("mcwfurnitures:acacia_pult_1", 0, "Acacia Cupboard Counter", 190, cat);
      reg("mcwfurnitures:acacia_pult_2", 0, "Acacia Cupboard Counter", 190, cat);
      reg("mcwfurnitures:acacia_pult_3", 0, "Acacia Double Drawer Counter", 190, cat);
      reg("mcwfurnitures:acacia_pult_4", 0, "Acacia Drawer Counter", 190, cat);
      reg("mcwfurnitures:birch_box", 0, "Birch Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:birch_box_2", 0, "Birch Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:birch_cupboard", 0, "Birch Cupboard", 190, cat);
      reg("mcwfurnitures:birch_cupboard_2", 0, "Birch Modern Cupboard", 190, cat);
      reg("mcwfurnitures:birch_cupboard_3", 0, "Birch Bookshelf", 190, cat);
      reg("mcwfurnitures:birch_cupboard_4", 0, "Birch Complex Cupboard", 190, cat);
      reg("mcwfurnitures:birch_cupboard_5", 0, "Birch Double Cupboard", 190, cat);
      reg("mcwfurnitures:birch_cupboard_6", 0, "Birch Cupboard", 190, cat);
      reg("mcwfurnitures:birch_cupboard_7", 0, "Birch Modern Cupboard", 190, cat);
      reg("mcwfurnitures:birch_cupboard_8", 0, "Birch Tall Bookshelf", 190, cat);
      reg("mcwfurnitures:birch_cupboard_9", 0, "Birch Complex Cupboard", 190, cat);
      reg("mcwfurnitures:birch_desk", 0, "Birch Drawer Desk", 145, cat);
      reg("mcwfurnitures:birch_desk4", 0, "Birch Table", 145, cat);
      reg("mcwfurnitures:birch_desk_2", 0, "Birch Cupboard Desk", 145, cat);
      reg("mcwfurnitures:birch_desk_3", 0, "Birch Covered Desk", 145, cat);
      reg("mcwfurnitures:birch_desk_5", 0, "Birch Cupboard Desk", 145, cat);
      reg("mcwfurnitures:birch_desk_6", 0, "Birch Drawer Desk", 145, cat);
      reg("mcwfurnitures:birch_desk_7", 0, "Birch Double Table", 145, cat);
      reg("mcwfurnitures:birch_desk_8", 0, "Birch Modern Table", 145, cat);
      reg("mcwfurnitures:birch_desk_9", 0, "Birch Glass Table", 145, cat);
      reg("mcwfurnitures:birch_dresser", 0, "Birch Classic Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_10", 0, "Birch Bookshelf Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_11", 0, "Birch Bookcase Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_12", 0, "Birch Complex Shelving unit", 160, cat);
      reg("mcwfurnitures:birch_dresser_13", 0, "Birch Drawer Shelving unit", 160, cat);
      reg("mcwfurnitures:birch_dresser_14", 0, "Birch Empty Shelving unit", 160, cat);
      reg("mcwfurnitures:birch_dresser_15", 0, "Birch Sapling Cabinet Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_16", 0, "Birch Sapling Cabinet Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_17", 0, "Birch Sapling Drawer Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_18", 0, "Birch Sapling Drawer Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_3", 0, "Birch Double Drawer Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_4", 0, "Birch Triple Drawer Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_5", 0, "Birch Triple Drawer Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_6", 0, "Birch Bookcase Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_7", 0, "Birch Complex Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_8", 0, "Birch Complex Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_9", 0, "Birch Bookshelf Dresser", 160, cat);
      reg("mcwfurnitures:birch_dresser_box", 0, "Birch Cupboard Dresser", 160, cat);
      reg("mcwfurnitures:birch_furniture_1", 0, "Birch Wardrobe", 210, cat);
      reg("mcwfurnitures:birch_furniture_2", 0, "Birch Modern Wardrobe", 210, cat);
      reg("mcwfurnitures:birch_furniture_3", 0, "Birch Complex Wardrobe", 210, cat);
      reg("mcwfurnitures:birch_furniture_4", 0, "Birch Display Wardrobe", 210, cat);
      reg("mcwfurnitures:birch_furniture_5", 0, "Birch Shelf Wardrobe", 210, cat);
      reg("mcwfurnitures:birch_furniture_6", 0, "Birch Cupboard Wardrobe", 210, cat);
      reg("mcwfurnitures:birch_furniture_7", 0, "Birch Tall Bookshelf", 210, cat);
      reg("mcwfurnitures:birch_furniture_8", 0, "Birch Display Wardrobe", 210, cat);
      reg("mcwfurnitures:birch_furniture_9", 0, "Birch Complex Wardrobe", 210, cat);
      reg("mcwfurnitures:birch_nightstand", 0, "Birch Drawer Nightstand", 160, cat);
      reg("mcwfurnitures:birch_nightstand_10", 0, "Birch End Table", 160, cat);
      reg("mcwfurnitures:birch_nightstand_11", 0, "Birch End Table", 160, cat);
      reg("mcwfurnitures:birch_nightstand_2", 0, "Birch Double Drawer Nightstand", 160, cat);
      reg("mcwfurnitures:birch_nightstand_3", 0, "Birch Bookcase Nightstand", 160, cat);
      reg("mcwfurnitures:birch_nightstand_4", 0, "Birch Double Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:birch_nightstand_5", 0, "Birch Drawer Cupboard", 160, cat);
      reg("mcwfurnitures:birch_nightstand_6", 0, "Birch Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:birch_nightstand_7", 0, "Birch Glass Cabinet", 160, cat);
      reg("mcwfurnitures:birch_nightstand_8", 0, "Birch Glass Cabinet", 160, cat);
      reg("mcwfurnitures:birch_nightstand_9", 0, "Birch End Table", 160, cat);
      reg("mcwfurnitures:birch_pult", 0, "Birch Kitchen Counter", 190, cat);
      reg("mcwfurnitures:birch_pult_1", 0, "Birch Cupboard Counter", 190, cat);
      reg("mcwfurnitures:birch_pult_2", 0, "Birch Cupboard Counter", 190, cat);
      reg("mcwfurnitures:birch_pult_3", 0, "Birch Double Drawer Counter", 190, cat);
      reg("mcwfurnitures:birch_pult_4", 0, "Birch Drawer Counter", 190, cat);
      reg("mcwfurnitures:box", 0, "Oak Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:box_2", 0, "Oak Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:cupboard", 0, "Oak Cupboard", 190, cat);
      reg("mcwfurnitures:cupboard_2", 0, "Oak Modern Cupboard", 190, cat);
      reg("mcwfurnitures:cupboard_3", 0, "Oak Bookshelf", 190, cat);
      reg("mcwfurnitures:cupboard_4", 0, "Oak Complex Cupboard", 190, cat);
      reg("mcwfurnitures:cupboard_5", 0, "Oak Double Cupboard", 190, cat);
      reg("mcwfurnitures:cupboard_6", 0, "Oak Cupboard", 190, cat);
      reg("mcwfurnitures:cupboard_7", 0, "Oak Modern Cupboard", 190, cat);
      reg("mcwfurnitures:cupboard_8", 0, "Oak Tall Bookshelf", 190, cat);
      reg("mcwfurnitures:cupboard_9", 0, "Oak Complex Cupboard", 190, cat);
      reg("mcwfurnitures:dark_oak_box", 0, "Dark Oak Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:dark_oak_box_2", 0, "Dark Oak Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:dark_oak_cupboard", 0, "Dark Oak Cupboard", 190, cat);
      reg("mcwfurnitures:dark_oak_cupboard_2", 0, "Dark Oak Modern Cupboard", 190, cat);
      reg("mcwfurnitures:dark_oak_cupboard_3", 0, "Dark Oak Bookshelf", 190, cat);
      reg("mcwfurnitures:dark_oak_cupboard_4", 0, "Dark Oak Complex Cupboard", 190, cat);
      reg("mcwfurnitures:dark_oak_cupboard_5", 0, "Dark Oak Double Cupboard", 190, cat);
      reg("mcwfurnitures:dark_oak_cupboard_6", 0, "Dark Oak Cupboard", 190, cat);
      reg("mcwfurnitures:dark_oak_cupboard_7", 0, "Dark Oak Modern Cupboard", 190, cat);
      reg("mcwfurnitures:dark_oak_cupboard_8", 0, "Dark Oak Tall Bookshelf", 190, cat);
      reg("mcwfurnitures:dark_oak_cupboard_9", 0, "Dark Oak Complex Cupboard", 190, cat);
      reg("mcwfurnitures:dark_oak_desk", 0, "Dark Oak Drawer Desk", 145, cat);
      reg("mcwfurnitures:dark_oak_desk4", 0, "Dark Oak Table", 145, cat);
      reg("mcwfurnitures:dark_oak_desk_2", 0, "Dark Oak Cupboard Desk", 145, cat);
      reg("mcwfurnitures:dark_oak_desk_3", 0, "Dark Oak Covered Desk", 145, cat);
      reg("mcwfurnitures:dark_oak_desk_5", 0, "Dark Oak Cupboard Desk", 145, cat);
      reg("mcwfurnitures:dark_oak_desk_6", 0, "Dark Oak Drawer Desk", 145, cat);
      reg("mcwfurnitures:dark_oak_desk_7", 0, "Dark Oak Double Table", 145, cat);
      reg("mcwfurnitures:dark_oak_desk_8", 0, "Dark Oak Modern Table", 145, cat);
      reg("mcwfurnitures:dark_oak_desk_9", 0, "Dark Oak Glass Table", 145, cat);
      reg("mcwfurnitures:dark_oak_dresser", 0, "Dark Oak Classic Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_10", 0, "Dark Oak Bookshelf Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_11", 0, "Dark Oak Bookcase Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_12", 0, "Dark Oak Complex Shelving unit", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_13", 0, "Dark Oak Drawer Shelving unit", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_14", 0, "Dark Oak Empty Shelving unit", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_15", 0, "Dark Oak Sapling Cabinet Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_16", 0, "Dark Oak Sapling Cabinet Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_17", 0, "Dark Oak Sapling Drawer Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_18", 0, "Dark Oak Sapling Drawer Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_3", 0, "Dark Oak Double Drawer Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_4", 0, "Dark Oak Triple Drawer Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_5", 0, "Dark Oak Triple Drawer Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_6", 0, "Dark Oak Bookcase Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_7", 0, "Dark Oak Complex Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_8", 0, "Dark Oak Complex Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_9", 0, "Dark Oak Bookshelf Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_dresser_box", 0, "Dark Oak Cupboard Dresser", 160, cat);
      reg("mcwfurnitures:dark_oak_furniture_1", 0, "Dark Oak Wardrobe", 210, cat);
      reg("mcwfurnitures:dark_oak_furniture_2", 0, "Dark Oak Modern Wardrobe", 210, cat);
      reg("mcwfurnitures:dark_oak_furniture_3", 0, "Dark Oak Complex Wardrobe", 210, cat);
      reg("mcwfurnitures:dark_oak_furniture_4", 0, "Dark Oak Display Wardrobe", 210, cat);
      reg("mcwfurnitures:dark_oak_furniture_5", 0, "Dark Oak Shelf Wardrobe", 210, cat);
      reg("mcwfurnitures:dark_oak_furniture_6", 0, "Dark Oak Cupboard Wardrobe", 210, cat);
      reg("mcwfurnitures:dark_oak_furniture_7", 0, "Dark Oak Tall Bookshelf", 210, cat);
      reg("mcwfurnitures:dark_oak_furniture_8", 0, "Dark Oak Display Wardrobe", 210, cat);
      reg("mcwfurnitures:dark_oak_furniture_9", 0, "Dark Oak Complex Wardrobe", 210, cat);
      reg("mcwfurnitures:dark_oak_nightstand", 0, "Dark Oak Drawer Nightstand", 160, cat);
      reg("mcwfurnitures:dark_oak_nightstand_10", 0, "Dark Oak End Table", 160, cat);
      reg("mcwfurnitures:dark_oak_nightstand_11", 0, "Dark Oak End Table", 160, cat);
      reg("mcwfurnitures:dark_oak_nightstand_2", 0, "Dark Oak Double Drawer Nightstand", 160, cat);
      reg("mcwfurnitures:dark_oak_nightstand_3", 0, "Dark Oak Bookcase Nightstand", 160, cat);
      reg("mcwfurnitures:dark_oak_nightstand_4", 0, "Dark Oak Double Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:dark_oak_nightstand_5", 0, "Dark Oak Drawer Cupboard", 160, cat);
      reg("mcwfurnitures:dark_oak_nightstand_6", 0, "Dark Oak Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:dark_oak_nightstand_7", 0, "Dark Oak Glass Cabinet", 160, cat);
      reg("mcwfurnitures:dark_oak_nightstand_8", 0, "Dark Oak Glass Cabinet", 160, cat);
      reg("mcwfurnitures:dark_oak_nightstand_9", 0, "Dark Oak End Table", 160, cat);
      reg("mcwfurnitures:dark_oak_pult", 0, "Dark Oak Kitchen Counter", 190, cat);
      reg("mcwfurnitures:dark_oak_pult_1", 0, "Dark Oak Cupboard Counter", 190, cat);
      reg("mcwfurnitures:dark_oak_pult_2", 0, "Dark Oak Cupboard Counter", 190, cat);
      reg("mcwfurnitures:dark_oak_pult_3", 0, "Dark Oak Double Drawer Counter", 190, cat);
      reg("mcwfurnitures:dark_oak_pult_4", 0, "Dark Oak Drawer Counter", 190, cat);
      reg("mcwfurnitures:desk", 0, "Oak Drawer Desk", 145, cat);
      reg("mcwfurnitures:desk4", 0, "Oak Table", 145, cat);
      reg("mcwfurnitures:desk_2", 0, "Oak Cupboard Desk", 145, cat);
      reg("mcwfurnitures:desk_3", 0, "Oak Covered Desk", 145, cat);
      reg("mcwfurnitures:desk_5", 0, "Oak Cupboard Desk", 145, cat);
      reg("mcwfurnitures:desk_6", 0, "Oak Drawer Desk", 145, cat);
      reg("mcwfurnitures:desk_7", 0, "Oak Double Table", 145, cat);
      reg("mcwfurnitures:desk_8", 0, "Oak Modern Table", 145, cat);
      reg("mcwfurnitures:desk_9", 0, "Oak Glass Table", 145, cat);
      reg("mcwfurnitures:dresser", 0, "Oak Classic Dresser", 160, cat);
      reg("mcwfurnitures:dresser_10", 0, "Oak Bookshelf Dresser", 160, cat);
      reg("mcwfurnitures:dresser_11", 0, "Oak Bookcase Dresser", 160, cat);
      reg("mcwfurnitures:dresser_12", 0, "Oak Complex Shelving unit", 160, cat);
      reg("mcwfurnitures:dresser_13", 0, "Oak Drawer Shelving unit", 160, cat);
      reg("mcwfurnitures:dresser_14", 0, "Oak Empty Shelving unit", 160, cat);
      reg("mcwfurnitures:dresser_15", 0, "Oak Sapling Cabinet Dresser", 160, cat);
      reg("mcwfurnitures:dresser_16", 0, "Oak Sapling Cabinet Dresser", 160, cat);
      reg("mcwfurnitures:dresser_17", 0, "Oak Sapling Drawer Dresser", 160, cat);
      reg("mcwfurnitures:dresser_18", 0, "Oak Sapling Drawer Dresser", 160, cat);
      reg("mcwfurnitures:dresser_3", 0, "Oak Double Drawer Dresser", 160, cat);
      reg("mcwfurnitures:dresser_4", 0, "Oak Triple Drawer Dresser", 160, cat);
      reg("mcwfurnitures:dresser_5", 0, "Oak Triple Drawer Dresser", 160, cat);
      reg("mcwfurnitures:dresser_6", 0, "Oak Bookcase Dresser", 160, cat);
      reg("mcwfurnitures:dresser_7", 0, "Oak Complex Dresser", 160, cat);
      reg("mcwfurnitures:dresser_8", 0, "Oak Complex Dresser", 160, cat);
      reg("mcwfurnitures:dresser_9", 0, "Oak Bookshelf Dresser", 160, cat);
      reg("mcwfurnitures:dresser_box", 0, "Oak Cupboard Dresser", 160, cat);
      reg("mcwfurnitures:furniture_1", 0, "Oak Wardrobe", 210, cat);
      reg("mcwfurnitures:furniture_2", 0, "Oak Modern Wardrobe", 210, cat);
      reg("mcwfurnitures:furniture_3", 0, "Oak Complex Wardrobe", 210, cat);
      reg("mcwfurnitures:furniture_4", 0, "Oak Display Wardrobe", 210, cat);
      reg("mcwfurnitures:furniture_5", 0, "Oak Shelf Wardrobe", 210, cat);
      reg("mcwfurnitures:furniture_6", 0, "Oak Cupboard Wardrobe", 210, cat);
      reg("mcwfurnitures:furniture_7", 0, "Oak Tall Bookshelf", 210, cat);
      reg("mcwfurnitures:furniture_8", 0, "Oak Display Wardrobe", 210, cat);
      reg("mcwfurnitures:furniture_9", 0, "Oak Complex Wardrobe", 210, cat);
      reg("mcwfurnitures:jungle_box", 0, "Jungle Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:jungle_box_2", 0, "Jungle Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:jungle_cupboard", 0, "Jungle Cupboard", 190, cat);
      reg("mcwfurnitures:jungle_cupboard_2", 0, "Jungle Modern Cupboard", 190, cat);
      reg("mcwfurnitures:jungle_cupboard_3", 0, "Jungle Bookshelf", 190, cat);
      reg("mcwfurnitures:jungle_cupboard_4", 0, "Jungle Complex Cupboard", 190, cat);
      reg("mcwfurnitures:jungle_cupboard_5", 0, "Jungle Double Cupboard", 190, cat);
      reg("mcwfurnitures:jungle_cupboard_6", 0, "Jungle Cupboard", 190, cat);
      reg("mcwfurnitures:jungle_cupboard_7", 0, "Jungle Modern Cupboard", 190, cat);
      reg("mcwfurnitures:jungle_cupboard_8", 0, "Jungle Tall Bookshelf", 190, cat);
      reg("mcwfurnitures:jungle_cupboard_9", 0, "Jungle Complex Cupboard", 190, cat);
      reg("mcwfurnitures:jungle_desk", 0, "Jungle Drawer Desk", 145, cat);
      reg("mcwfurnitures:jungle_desk4", 0, "Jungle Table", 145, cat);
      reg("mcwfurnitures:jungle_desk_2", 0, "Jungle Cupboard Desk", 145, cat);
      reg("mcwfurnitures:jungle_desk_3", 0, "Jungle Covered Desk", 145, cat);
      reg("mcwfurnitures:jungle_desk_5", 0, "Jungle Cupboard Desk", 145, cat);
      reg("mcwfurnitures:jungle_desk_6", 0, "Jungle Drawer Desk", 145, cat);
      reg("mcwfurnitures:jungle_desk_7", 0, "Jungle Double Table", 145, cat);
      reg("mcwfurnitures:jungle_desk_8", 0, "Jungle Modern Table", 145, cat);
      reg("mcwfurnitures:jungle_desk_9", 0, "Jungle Glass Table", 145, cat);
      reg("mcwfurnitures:jungle_dresser", 0, "Jungle Classic Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_10", 0, "Jungle Bookshelf Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_11", 0, "Jungle Bookcase Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_12", 0, "Jungle Complex Shelving unit", 160, cat);
      reg("mcwfurnitures:jungle_dresser_13", 0, "Jungle Drawer Shelving unit", 160, cat);
      reg("mcwfurnitures:jungle_dresser_14", 0, "Jungle Empty Shelving unit", 160, cat);
      reg("mcwfurnitures:jungle_dresser_15", 0, "Jungle Sapling Cabinet Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_16", 0, "Jungle Sapling Cabinet Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_17", 0, "Jungle Sapling Drawer Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_18", 0, "Jungle Sapling Drawer Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_3", 0, "Jungle Double Drawer Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_4", 0, "Jungle Triple Drawer Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_5", 0, "Jungle Triple Drawer Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_6", 0, "Jungle Bookcase Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_7", 0, "Jungle Complex Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_8", 0, "Jungle Complex Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_9", 0, "Jungle Bookshelf Dresser", 160, cat);
      reg("mcwfurnitures:jungle_dresser_box", 0, "Jungle Cupboard Dresser", 160, cat);
      reg("mcwfurnitures:jungle_furniture_1", 0, "Jungle Wardrobe", 210, cat);
      reg("mcwfurnitures:jungle_furniture_2", 0, "Jungle Modern Wardrobe", 210, cat);
      reg("mcwfurnitures:jungle_furniture_3", 0, "Jungle Complex Wardrobe", 210, cat);
      reg("mcwfurnitures:jungle_furniture_4", 0, "Jungle Display Wardrobe", 210, cat);
      reg("mcwfurnitures:jungle_furniture_5", 0, "Jungle Shelf Wardrobe", 210, cat);
      reg("mcwfurnitures:jungle_furniture_6", 0, "Jungle Cupboard Wardrobe", 210, cat);
      reg("mcwfurnitures:jungle_furniture_7", 0, "Jungle Tall Bookshelf", 210, cat);
      reg("mcwfurnitures:jungle_furniture_8", 0, "Jungle Display Wardrobe", 210, cat);
      reg("mcwfurnitures:jungle_furniture_9", 0, "Jungle Complex Wardrobe", 210, cat);
      reg("mcwfurnitures:jungle_nightstand", 0, "Jungle Drawer Nightstand", 160, cat);
      reg("mcwfurnitures:jungle_nightstand_10", 0, "Jungle End Table", 160, cat);
      reg("mcwfurnitures:jungle_nightstand_11", 0, "Jungle End Table", 160, cat);
      reg("mcwfurnitures:jungle_nightstand_2", 0, "Jungle Double Drawer Nightstand", 160, cat);
      reg("mcwfurnitures:jungle_nightstand_3", 0, "Jungle Bookcase Nightstand", 160, cat);
      reg("mcwfurnitures:jungle_nightstand_4", 0, "Jungle Double Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:jungle_nightstand_5", 0, "Jungle Drawer Cupboard", 160, cat);
      reg("mcwfurnitures:jungle_nightstand_6", 0, "Jungle Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:jungle_nightstand_7", 0, "Jungle Glass Cabinet", 160, cat);
      reg("mcwfurnitures:jungle_nightstand_8", 0, "Jungle Glass Cabinet", 160, cat);
      reg("mcwfurnitures:jungle_nightstand_9", 0, "Jungle End Table", 160, cat);
      reg("mcwfurnitures:jungle_pult", 0, "Jungle Kitchen Counter", 190, cat);
      reg("mcwfurnitures:jungle_pult_1", 0, "Jungle Cupboard Counter", 190, cat);
      reg("mcwfurnitures:jungle_pult_2", 0, "Jungle Cupboard Counter", 190, cat);
      reg("mcwfurnitures:jungle_pult_3", 0, "Jungle Double Drawer Counter", 190, cat);
      reg("mcwfurnitures:jungle_pult_4", 0, "Jungle Drawer Counter", 190, cat);
      reg("mcwfurnitures:nightstand", 0, "Oak Drawer Nightstand", 160, cat);
      reg("mcwfurnitures:nightstand_10", 0, "Oak End Table", 160, cat);
      reg("mcwfurnitures:nightstand_11", 0, "Oak End Table", 160, cat);
      reg("mcwfurnitures:nightstand_2", 0, "Oak Double Drawer Nightstand", 160, cat);
      reg("mcwfurnitures:nightstand_3", 0, "Oak Bookcase Nightstand", 160, cat);
      reg("mcwfurnitures:nightstand_4", 0, "Oak Double Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:nightstand_5", 0, "Oak Drawer Cupboard", 160, cat);
      reg("mcwfurnitures:nightstand_6", 0, "Oak Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:nightstand_7", 0, "Oak Glass Cabinet", 160, cat);
      reg("mcwfurnitures:nightstand_8", 0, "Oak Glass Cabinet", 160, cat);
      reg("mcwfurnitures:nightstand_9", 0, "Oak End Table", 160, cat);
      reg("mcwfurnitures:pult", 0, "Oak Kitchen Counter", 190, cat);
      reg("mcwfurnitures:pult_1", 0, "Oak Cupboard Counter", 190, cat);
      reg("mcwfurnitures:pult_2", 0, "Oak Cupboard Counter", 190, cat);
      reg("mcwfurnitures:pult_3", 0, "Oak Double Drawer Counter", 190, cat);
      reg("mcwfurnitures:pult_4", 0, "Oak Drawer Counter", 190, cat);
      reg("mcwfurnitures:spruce_box", 0, "Spruce Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:spruce_box_2", 0, "Spruce Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:spruce_cupboard", 0, "Spruce Cupboard", 190, cat);
      reg("mcwfurnitures:spruce_cupboard_2", 0, "Spruce Modern Cupboard", 190, cat);
      reg("mcwfurnitures:spruce_cupboard_3", 0, "Spruce Bookshelf", 190, cat);
      reg("mcwfurnitures:spruce_cupboard_4", 0, "Spruce Complex Cupboard", 190, cat);
      reg("mcwfurnitures:spruce_cupboard_5", 0, "Spruce Double Cupboard", 190, cat);
      reg("mcwfurnitures:spruce_cupboard_6", 0, "Spruce Cupboard", 190, cat);
      reg("mcwfurnitures:spruce_cupboard_7", 0, "Spruce Modern Cupboard", 190, cat);
      reg("mcwfurnitures:spruce_cupboard_8", 0, "Spruce Tall Bookshelf", 190, cat);
      reg("mcwfurnitures:spruce_cupboard_9", 0, "Spruce Complex Cupboard", 190, cat);
      reg("mcwfurnitures:spruce_desk", 0, "Spruce Drawer Desk", 145, cat);
      reg("mcwfurnitures:spruce_desk4", 0, "Spruce Table", 145, cat);
      reg("mcwfurnitures:spruce_desk_2", 0, "Spruce Cupboard Desk", 145, cat);
      reg("mcwfurnitures:spruce_desk_3", 0, "Spruce Covered Desk", 145, cat);
      reg("mcwfurnitures:spruce_desk_5", 0, "Spruce Cupboard Desk", 145, cat);
      reg("mcwfurnitures:spruce_desk_6", 0, "Spruce Drawer Desk", 145, cat);
      reg("mcwfurnitures:spruce_desk_7", 0, "Spruce Double Table", 145, cat);
      reg("mcwfurnitures:spruce_desk_8", 0, "Spruce Modern Table", 145, cat);
      reg("mcwfurnitures:spruce_desk_9", 0, "Spruce Glass Table", 145, cat);
      reg("mcwfurnitures:spruce_dresser", 0, "Spruce Classic Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_10", 0, "Spruce Bookshelf Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_11", 0, "Spruce Bookcase Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_12", 0, "Spruce Complex Shelving unit", 160, cat);
      reg("mcwfurnitures:spruce_dresser_13", 0, "Spruce Drawer Shelving unit", 160, cat);
      reg("mcwfurnitures:spruce_dresser_14", 0, "Spruce Empty Shelving unit", 160, cat);
      reg("mcwfurnitures:spruce_dresser_15", 0, "Spruce Sapling Cabinet Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_16", 0, "Spruce Sapling Cabinet Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_17", 0, "Spruce Sapling Drawer Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_18", 0, "Spruce Sapling Drawer Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_3", 0, "Spruce Double Drawer Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_4", 0, "Spruce Triple Drawer Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_5", 0, "Spruce Triple Drawer Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_6", 0, "Spruce Bookcase Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_7", 0, "Spruce Complex Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_8", 0, "Spruce Complex Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_9", 0, "Spruce Bookshelf Dresser", 160, cat);
      reg("mcwfurnitures:spruce_dresser_box", 0, "Spruce Cupboard Dresser", 160, cat);
      reg("mcwfurnitures:spruce_furniture_1", 0, "Spruce Wardrobe", 210, cat);
      reg("mcwfurnitures:spruce_furniture_2", 0, "Spruce Modern Wardrobe", 210, cat);
      reg("mcwfurnitures:spruce_furniture_3", 0, "Spruce Complex Wardrobe", 210, cat);
      reg("mcwfurnitures:spruce_furniture_4", 0, "Spruce Display Wardrobe", 210, cat);
      reg("mcwfurnitures:spruce_furniture_5", 0, "Spruce Shelf Wardrobe", 210, cat);
      reg("mcwfurnitures:spruce_furniture_6", 0, "Spruce Cupboard Wardrobe", 210, cat);
      reg("mcwfurnitures:spruce_furniture_7", 0, "Spruce Tall Bookshelf", 210, cat);
      reg("mcwfurnitures:spruce_furniture_8", 0, "Spruce Display Wardrobe", 210, cat);
      reg("mcwfurnitures:spruce_furniture_9", 0, "Spruce Complex Wardrobe", 210, cat);
      reg("mcwfurnitures:spruce_nightstand", 0, "Spruce Drawer Nightstand", 160, cat);
      reg("mcwfurnitures:spruce_nightstand_10", 0, "Spruce End Table", 160, cat);
      reg("mcwfurnitures:spruce_nightstand_11", 0, "Spruce End Table", 160, cat);
      reg("mcwfurnitures:spruce_nightstand_2", 0, "Spruce Double Drawer Nightstand", 160, cat);
      reg("mcwfurnitures:spruce_nightstand_3", 0, "Spruce Bookcase Nightstand", 160, cat);
      reg("mcwfurnitures:spruce_nightstand_4", 0, "Spruce Double Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:spruce_nightstand_5", 0, "Spruce Drawer Cupboard", 160, cat);
      reg("mcwfurnitures:spruce_nightstand_6", 0, "Spruce Kitchen Cabinet", 160, cat);
      reg("mcwfurnitures:spruce_nightstand_7", 0, "Spruce Glass Cabinet", 160, cat);
      reg("mcwfurnitures:spruce_nightstand_8", 0, "Spruce Glass Cabinet", 160, cat);
      reg("mcwfurnitures:spruce_nightstand_9", 0, "Spruce End Table", 160, cat);
      reg("mcwfurnitures:spruce_pult", 0, "Spruce Kitchen Counter", 190, cat);
      reg("mcwfurnitures:spruce_pult_1", 0, "Spruce Cupboard Counter", 190, cat);
      reg("mcwfurnitures:spruce_pult_2", 0, "Spruce Cupboard Counter", 190, cat);
      reg("mcwfurnitures:spruce_pult_3", 0, "Spruce Double Drawer Counter", 190, cat);
      reg("mcwfurnitures:spruce_pult_4", 0, "Spruce Drawer Counter", 190, cat);
      reg("mcwfurnitures:oak_plate", 0, "Oak Plate", 85, cat);
      reg("mcwfurnitures:spruce_plate", 0, "Spruce Plate", 85, cat);
      reg("mcwfurnitures:birch_plate", 0, "Birch Plate", 85, cat);
      reg("mcwfurnitures:jungle_plate", 0, "Jungle Plate", 85, cat);
      reg("mcwfurnitures:acacia_plate", 0, "Acacia Plate", 85, cat);
      reg("mcwfurnitures:dark_oak_plate", 0, "Dark Oak Plate", 85, cat);
      reg("mcwfurnitures:cabinet_door", 0, "Cabinet Door", 85, cat);
      reg("mcwfurnitures:drawer", 0, "Drawer", 85, cat);
      reg("mcwfurnitures:iron_handle", 0, "Iron Handle", 85, cat);
   }

   private static void registerVariedCommodities() {
      BuildingCategory cat = BuildingCategory.VARIED_COMMODITIES;
      String[] sixVariantNames = new String[]{"Oak", "Spruce", "Birch", "Jungle", "Acacia", "Dark Oak"};
      String[][] sixVariantBlocks = new String[][]{{"chair", "Chair", "110"}, {"crate", "Crate", "110"}, {"barrel", "Barrel", "110"}, {"table", "Table", "110"}, {"stool", "Stool", "85"}, {"shelf", "Shelf", "100"}, {"beam", "Beam", "110"}, {"couch_wood", "Wood Couch", "130"}, {"couch_wool", "Wool Couch", "135"}, {"weapon_rack", "Weapon Rack", "115"}, {"sign", "Sign", "110"}};

      for(String[] entry : sixVariantBlocks) {
         for(int m = 0; m < 6; ++m) {
            reg("variedcommodities:" + entry[0], m, sixVariantNames[m] + " " + entry[1], Integer.parseInt(entry[2]), cat);
         }
      }

      String[] var10000 = new String[]{"Style 1", "Style 2", "Style 3", "Style 4", "Style 5"};
      reg("variedcommodities:banner", 0, "Banner", 40, cat);
      reg("variedcommodities:banner", 1, "Banner (Alt 1)", 40, cat);
      reg("variedcommodities:banner", 2, "Banner (Alt 2)", 40, cat);
      reg("variedcommodities:banner", 3, "Banner (Alt 3)", 40, cat);
      reg("variedcommodities:banner", 4, "Banner (Alt 4)", 40, cat);
      reg("variedcommodities:wall_banner", 0, "Wall Banner", 40, cat);
      reg("variedcommodities:wall_banner", 1, "Wall Banner (Alt 1)", 40, cat);
      reg("variedcommodities:wall_banner", 2, "Wall Banner (Alt 2)", 40, cat);
      reg("variedcommodities:wall_banner", 3, "Wall Banner (Alt 3)", 40, cat);
      reg("variedcommodities:wall_banner", 4, "Wall Banner (Alt 4)", 40, cat);
      reg("variedcommodities:tall_lamp", 0, "Tall Lamp", 60, cat);
      reg("variedcommodities:tall_lamp", 1, "Tall Lamp (Alt 1)", 60, cat);
      reg("variedcommodities:tall_lamp", 2, "Tall Lamp (Alt 2)", 60, cat);
      reg("variedcommodities:tall_lamp", 3, "Tall Lamp (Alt 3)", 60, cat);
      reg("variedcommodities:tall_lamp", 4, "Tall Lamp (Alt 4)", 60, cat);
      reg("variedcommodities:pedestal", 0, "Pedestal", 55, cat);
      reg("variedcommodities:pedestal", 1, "Pedestal (Alt 1)", 55, cat);
      reg("variedcommodities:pedestal", 2, "Pedestal (Alt 2)", 55, cat);
      reg("variedcommodities:pedestal", 3, "Pedestal (Alt 3)", 55, cat);
      reg("variedcommodities:pedestal", 4, "Pedestal (Alt 4)", 55, cat);
      reg("variedcommodities:tombstone", 0, "Tombstone", 50, cat);
      reg("variedcommodities:tombstone", 1, "Tombstone (Cross)", 50, cat);
      reg("variedcommodities:tombstone", 2, "Tombstone (Round)", 50, cat);
      reg("variedcommodities:lamp", 0, "Lamp", 45, cat);
      reg("variedcommodities:candle", 0, "Candle", 30, cat);
      reg("variedcommodities:big_sign", 0, "Big Sign", 45, cat);
      reg("variedcommodities:campfire", 0, "Campfire", 40, cat);
   }

   private static void registerTools() {
      BuildingCategory cat = BuildingCategory.TOOLS;
      reg("toolkit:enchant1", 0, "§7Enchant I Tool Kit", 10000, cat);
      reg("toolkit:enchant2", 0, "§aEnchant II Tool Kit", 20000, cat);
      reg("toolkit:enchant3", 0, "§bEnchant III Tool Kit", 30000, cat);
      reg("toolkit:enchant4", 0, "§dEnchant IV Tool Kit", 40000, cat);
      reg("toolkit:enchant5", 0, "§6Enchant V Tool Kit", 50000, cat);
   }

   public static boolean isToolkit(String registryName) {
      return registryName != null && registryName.startsWith("toolkit:");
   }

   public static int getToolkitEnchantLevel(String registryName) {
      if (!isToolkit(registryName)) {
         return 0;
      } else {
         String suffix = registryName.substring("toolkit:enchant".length());

         try {
            return Integer.parseInt(suffix);
         } catch (NumberFormatException var3) {
            return 1;
         }
      }
   }

   private static String capitalize(String str) {
      if (str != null && !str.isEmpty()) {
         StringBuilder sb = new StringBuilder();
         boolean capitalizeNext = true;

         for(int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (c == ' ') {
               sb.append(c);
               capitalizeNext = true;
            } else if (capitalizeNext) {
               sb.append(Character.toUpperCase(c));
               capitalizeNext = false;
            } else {
               sb.append(c);
            }
         }

         return sb.toString();
      } else {
         return str;
      }
   }

   public static enum BuildingCategory {
      BUILDING_BLOCKS("Building Blocks", -5601212),
      DECORATIONS("Decorations", -12277112),
      MISCELLANEOUS("Misc", -7820732),
      ENTRYWAY("Entryway", -7846742),
      ADDITIONAL_LANTERNS("Lanterns", -5592508),
      MACAW_FURNITURE("Furniture", -12285782),
      VARIED_COMMODITIES("Decor", -5618552),
      TOOLS("Tools", -3381709);

      public final String displayName;
      public final int color;

      private BuildingCategory(String displayName, int color) {
         this.displayName = displayName;
         this.color = color;
      }
   }

   public static class BuildingItem {
      public final String registryName;
      public final int meta;
      public final String displayName;
      public final int ryoPrice;
      public final BuildingCategory category;

      public BuildingItem(String registryName, int meta, String displayName, int ryoPrice, BuildingCategory category) {
         this.registryName = registryName;
         this.meta = meta;
         this.displayName = displayName;
         this.ryoPrice = ryoPrice;
         this.category = category;
      }
   }
}
