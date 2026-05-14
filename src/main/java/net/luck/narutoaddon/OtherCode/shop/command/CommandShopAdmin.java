
package net.luck.narutoaddon.OtherCode.shop.command;

import com.mojang.authlib.GameProfile;
import net.luck.narutoaddon.OtherCode.shop.core.ItemRarity;
import net.luck.narutoaddon.OtherCode.shop.core.ShopCategory;
import net.luck.narutoaddon.OtherCode.shop.core.ShopHistoryEntry;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateDefinition;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateLootEntry;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateRegistry;
import net.luck.narutoaddon.OtherCode.shop.network.ShopNetworkHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.*;

public class CommandShopAdmin extends CommandBase {
   private static final String PREFIX = "§6[ShopAdmin] §f";

   public String getName() {
      return "shopadmin";
   }

   public String getUsage(ICommandSender sender) {
      return "/shopadmin <player> | setbalance <player> <amount> | addbalance <player> <amount> | settokens <player> <amount> | resethistory <player> | history <player> [page]";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 1) {
         sender.sendMessage(new TextComponentString("§cUsage: " + this.getUsage(sender)));
      } else {
         switch (args[0].toLowerCase()) {
            case "setbalance":
               this.handleSetBalance(server, sender, args);
               return;
            case "addbalance":
               this.handleAddBalance(server, sender, args);
               return;
            case "settokens":
               this.handleSetTokens(server, sender, args);
               return;
            case "resethistory":
               this.handleResetHistory(server, sender, args);
               return;
            case "history":
               this.handleHistory(server, sender, args);
               return;
            case "rollryo":
               this.handleRollTest(server, sender, args, "ryo");
               return;
            case "rolltoken":
               this.handleRollTest(server, sender, args, "token");
               return;
            case "rollcrystal":
               this.handleRollTest(server, sender, args, "crystal");
               return;
            case "cleardupes":
               this.handleClearDupes(server, sender);
               return;
            case "clearclandupes":
               this.handleClearClanDupes(server, sender);
               return;
            default:
               this.handleLookup(server, sender, args[0]);
         }
      }
   }

   private void handleLookup(MinecraftServer server, ICommandSender sender, String playerName) {
      if (!(sender instanceof EntityPlayerMP)) {
         sender.sendMessage(new TextComponentString("§cThis command can only be used by players."));
      } else {
         EntityPlayerMP admin = (EntityPlayerMP)sender;
         UUID targetUUID = this.resolvePlayerUUID(server, playerName);
         if (targetUUID == null) {
            sender.sendMessage(new TextComponentString("§cPlayer not found: " + playerName));
         } else {
            String displayName = playerName;
            EntityPlayerMP onlineTarget = server.getPlayerList().getPlayerByUsername(playerName);
            if (onlineTarget != null) {
               displayName = onlineTarget.getName();
            }

            ShopNetworkHelper.sendAdminSync(server, targetUUID, displayName, admin);
            sender.sendMessage(new TextComponentString("§aOpening shop admin panel for " + displayName + "..."));
         }
      }
   }

   private void handleSetBalance(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /shopadmin setbalance <player> <amount>"));
      } else {
         UUID targetUUID = this.resolvePlayerUUID(server, args[1]);
         if (targetUUID == null) {
            sender.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
         } else {
            long amount;
            try {
               amount = Long.parseLong(args[2]);
            } catch (NumberFormatException var9) {
               sender.sendMessage(new TextComponentString("§cInvalid amount: " + args[2]));
               return;
            }

            ShopSavedData data = ShopSavedData.get(server.getWorld(0));
            data.setBalance(targetUUID, amount);
            EntityPlayerMP target = server.getPlayerList().getPlayerByUUID(targetUUID);
            if (target != null) {
               ShopNetworkHelper.sendShopSync(target);
            }

            sender.sendMessage(new TextComponentString("§aSet " + args[1] + "'s ryo balance to " + data.getBalance(targetUUID)));
         }
      }
   }

   private void handleAddBalance(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /shopadmin addbalance <player> <amount>"));
      } else {
         UUID targetUUID = this.resolvePlayerUUID(server, args[1]);
         if (targetUUID == null) {
            sender.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
         } else {
            long amount;
            try {
               amount = Long.parseLong(args[2]);
            } catch (NumberFormatException var9) {
               sender.sendMessage(new TextComponentString("§cInvalid amount: " + args[2]));
               return;
            }

            ShopSavedData data = ShopSavedData.get(server.getWorld(0));
            if (amount > 0L) {
               data.addBalance(targetUUID, amount);
            } else if (amount < 0L) {
               data.removeBalance(targetUUID, -amount);
            }

            EntityPlayerMP target = server.getPlayerList().getPlayerByUUID(targetUUID);
            if (target != null) {
               ShopNetworkHelper.sendShopSync(target);
            }

            sender.sendMessage(new TextComponentString("§a" + args[1] + "'s ryo balance is now " + data.getBalance(targetUUID)));
         }
      }
   }

   private void handleSetTokens(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /shopadmin settokens <player> <amount>"));
      } else {
         UUID targetUUID = this.resolvePlayerUUID(server, args[1]);
         if (targetUUID == null) {
            sender.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
         } else {
            int amount;
            try {
               amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException var9) {
               sender.sendMessage(new TextComponentString("§cInvalid amount: " + args[2]));
               return;
            }

            ShopSavedData data = ShopSavedData.get(server.getWorld(0));
            int current = data.getTokens(targetUUID);
            if (current > 0) {
               data.removeTokens(targetUUID, current);
            }

            if (amount > 0) {
               data.addTokens(targetUUID, amount);
            }

            EntityPlayerMP target = server.getPlayerList().getPlayerByUUID(targetUUID);
            if (target != null) {
               ShopNetworkHelper.sendShopSync(target);
            }

            sender.sendMessage(new TextComponentString("§aSet " + args[1] + "'s token balance to " + data.getTokens(targetUUID)));
         }
      }
   }

   private void handleClearDupes(MinecraftServer server, ICommandSender sender) {
      ShopSavedData data = ShopSavedData.get(server.getWorld(0));
      int count = data.clearAllOwnedItems();
      sender.sendMessage(new TextComponentString("§aCleared ALL dupe tracking for §e" + count + "§a players. Items will no longer show as duplicates in any crate."));
   }

   private void handleClearClanDupes(MinecraftServer server, ICommandSender sender) {
      CrateRegistry.init();
      ShopSavedData data = ShopSavedData.get(server.getWorld(0));
      Set<String> clanKeys = new HashSet();

      for(CrateDefinition crate : CrateRegistry.getAll()) {
         if (crate.getCategory() == ShopCategory.CLANS) {
            for(CrateLootEntry entry : crate.getEntries()) {
               if (entry.hasDisplayNameOverride()) {
                  clanKeys.add(entry.getDisplayNameOverride());
               }

               String itemKey = entry.getItemId() + ":" + entry.getItemMeta();
               clanKeys.add(itemKey);
            }
         }
      }

      int playersFixed = data.clearOwnedItemsByKeys(clanKeys);
      sender.sendMessage(new TextComponentString("§aCleared §eclan§a dupe tracking for §e" + playersFixed + "§a players (" + clanKeys.size() + " clan keys). SO6P/jutsu dupes untouched."));
   }

   private void handleResetHistory(MinecraftServer server, ICommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /shopadmin resethistory <player>"));
      } else {
         UUID targetUUID = this.resolvePlayerUUID(server, args[1]);
         if (targetUUID == null) {
            sender.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
         } else {
            ShopSavedData data = ShopSavedData.get(server.getWorld(0));
            data.clearHistory(targetUUID);
            sender.sendMessage(new TextComponentString("§aCleared shop history for " + args[1]));
         }
      }
   }

   private void handleHistory(MinecraftServer server, ICommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /shopadmin history <player> [page]"));
      } else {
         UUID targetUUID = this.resolvePlayerUUID(server, args[1]);
         if (targetUUID == null) {
            sender.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
         } else {
            int page = 1;
            if (args.length >= 3) {
               try {
                  page = Integer.parseInt(args[2]);
               } catch (NumberFormatException var20) {
                  sender.sendMessage(new TextComponentString("§cInvalid page number: " + args[2]));
                  return;
               }

               if (page < 1) {
                  page = 1;
               }
            }

            ShopSavedData data = ShopSavedData.get(server.getWorld(0));
            List<ShopHistoryEntry> allHistory = data.getHistory(targetUUID);
            if (allHistory.isEmpty()) {
               sender.sendMessage(new TextComponentString("§7No purchase history for " + args[1] + "."));
            } else {
               int perPage = 10;
               int totalPages = (allHistory.size() + perPage - 1) / perPage;
               if (page > totalPages) {
                  page = totalPages;
               }

               int startIdx = (page - 1) * perPage;
               int endIdx = Math.min(startIdx + perPage, allHistory.size());
               sender.sendMessage(new TextComponentString("§6=== Purchase History: " + args[1] + " (Page " + page + "/" + totalPages + ") ==="));
               long now = System.currentTimeMillis();

               for(int i = startIdx; i < endIdx; ++i) {
                  ShopHistoryEntry entry = (ShopHistoryEntry)allHistory.get(i);
                  String timeAgo = formatTimeAgo(now - entry.getTimestamp());
                  String crateName = formatCrateName(entry.getCrateId());
                  ItemRarity rarity = entry.getRarity();
                  String dupeStr = entry.isDuplicate() ? " §e[DUPE: +" + entry.getTokenAmount() + " CC]" : "";
                  sender.sendMessage(new TextComponentString("§8[" + timeAgo + "] §d" + crateName + ": §f" + entry.getDisplayName() + " §7(" + rarity.displayName + ")" + dupeStr));
               }

               if (page < totalPages) {
                  sender.sendMessage(new TextComponentString("§7Use /shopadmin history " + args[1] + " " + (page + 1) + " for next page"));
               }

            }
         }
      }
   }

   private static String formatTimeAgo(long diffMs) {
      long seconds = diffMs / 1000L;
      if (seconds < 60L) {
         return seconds + "s ago";
      } else {
         long minutes = seconds / 60L;
         if (minutes < 60L) {
            return minutes + "m ago";
         } else {
            long hours = minutes / 60L;
            if (hours < 24L) {
               return hours + "h ago";
            } else {
               long days = hours / 24L;
               return days + "d ago";
            }
         }
      }
   }

   private static String formatCrateName(String crateId) {
      if (crateId == null) {
         return "Unknown";
      } else {
         String name = crateId.startsWith("cash_") ? crateId.substring(5) : crateId;
         StringBuilder sb = new StringBuilder();
         boolean capitalizeNext = true;

         for(char c : name.toCharArray()) {
            if (c == '_') {
               sb.append(' ');
               capitalizeNext = true;
            } else {
               sb.append(capitalizeNext ? Character.toUpperCase(c) : c);
               capitalizeNext = false;
            }
         }

         return sb.toString();
      }
   }

   @Nullable
   private UUID resolvePlayerUUID(MinecraftServer server, String playerName) {
      EntityPlayerMP online = server.getPlayerList().getPlayerByUsername(playerName);
      if (online != null) {
         return online.getUniqueID();
      } else {
         GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(playerName);
         return profile != null ? profile.getId() : null;
      }
   }

   private void handleRollTest(MinecraftServer server, ICommandSender sender, String[] args, String shopType) {
      if (!(sender instanceof EntityPlayerMP)) {
         sender.sendMessage(new TextComponentString("§cThis command can only be used by players."));
      } else if (args.length < 3) {
         String categories = shopType.equals("crystal") ? "clan|so6p|tb|weapon" : "clan|so6p";
         sender.sendMessage(new TextComponentString("§cUsage: /shopadmin roll" + shopType + " <" + categories + "> <item name>"));
         if (shopType.equals("crystal")) {
            sender.sendMessage(new TextComponentString("§7For weapons: /shopadmin rollcrystal weapon <1-5> <weapon name>"));
         }

      } else {
         EntityPlayerMP player = (EntityPlayerMP)sender;
         String category = args[1].toLowerCase();
         int nameStartIndex = 2;
         if (category.equals("weapon")) {
            if (args.length < 4) {
               sender.sendMessage(new TextComponentString("§cUsage: /shopadmin rollcrystal weapon <1-5> <weapon name>"));
               return;
            }

            nameStartIndex = 3;
         }

         StringBuilder nameBuilder = new StringBuilder();

         for(int i = nameStartIndex; i < args.length; ++i) {
            if (i > nameStartIndex) {
               nameBuilder.append(" ");
            }

            nameBuilder.append(args[i]);
         }

         String targetName = nameBuilder.toString();
         CrateRegistry.init();
         List<CrateDefinition> cratesToSearch = new ArrayList();
         if (shopType.equals("crystal")) {
            switch (category) {
               case "clan":
                  addCrateIfExists(cratesToSearch, "cash_clan");
                  break;
               case "so6p":
                  addCrateIfExists(cratesToSearch, "cash_so6p");
                  break;
               case "tb":
                  addCrateIfExists(cratesToSearch, "cash_tailed_beast");
                  break;
               case "weapon":
                  CrateRegistry.ensureWeaponCratesLoaded();

                  int tier;
                  try {
                     tier = Integer.parseInt(args[2]);
                  } catch (NumberFormatException var21) {
                     sender.sendMessage(new TextComponentString("§cInvalid tier. Use 1-5."));
                     return;
                  }

                  if (tier < 1 || tier > 5) {
                     sender.sendMessage(new TextComponentString("§cInvalid tier. Use 1-5."));
                     return;
                  }

                  String[] weaponCrateIds = new String[]{"weapon_crate_1", "weapon_crate_2", "weapon_crate_3", "weapon_crate_4", "weapon_crate_5"};
                  addCrateIfExists(cratesToSearch, weaponCrateIds[tier - 1]);
                  break;
               default:
                  sender.sendMessage(new TextComponentString("§cInvalid category. Use: clan, so6p, tb, or weapon"));
                  return;
            }
         } else {
            switch (category) {
               case "clan":
                  addCrateIfExists(cratesToSearch, "clan_crate_1");
                  addCrateIfExists(cratesToSearch, "clan_crate_2");
                  addCrateIfExists(cratesToSearch, "clan_crate_3");
                  addCrateIfExists(cratesToSearch, "clan_crate_4");
                  addCrateIfExists(cratesToSearch, "clan_crate_5");
                  break;
               case "so6p":
                  addCrateIfExists(cratesToSearch, "so6p_crate_1");
                  addCrateIfExists(cratesToSearch, "so6p_crate_2");
                  addCrateIfExists(cratesToSearch, "so6p_crate_3");
                  break;
               default:
                  sender.sendMessage(new TextComponentString("§cInvalid category. Use: clan or so6p"));
                  return;
            }
         }

         if (cratesToSearch.isEmpty()) {
            sender.sendMessage(new TextComponentString("§cNo crates found for category: " + category));
         } else {
            CrateLootEntry match = null;
            String matchCrateId = null;

            for(CrateDefinition crate : cratesToSearch) {
               for(CrateLootEntry entry : crate.getEntries()) {
                  String itemId = entry.getItemId();
                  String displayOverride = entry.hasDisplayNameOverride() ? entry.getDisplayNameOverride() : "";
                  if (itemId.toLowerCase().contains(targetName.toLowerCase()) || displayOverride.toLowerCase().contains(targetName.toLowerCase())) {
                     match = entry;
                     matchCrateId = crate.getCrateId();
                     break;
                  }
               }

               if (match != null) {
                  break;
               }
            }

            if (match == null) {
               sender.sendMessage(new TextComponentString("§cItem not found: \"" + targetName + "\". Available items:"));
               int count = 0;

               for(CrateDefinition crate : cratesToSearch) {
                  for(CrateLootEntry entry : crate.getEntries()) {
                     if (count >= 20) {
                        sender.sendMessage(new TextComponentString("§7  ... and more (showing first 20)"));
                        return;
                     }

                     String displayName = entry.getDisplayName();
                     sender.sendMessage(new TextComponentString("§7  - " + displayName + " §8(" + entry.getRarity().displayName + ")"));
                     ++count;
                  }
               }

            } else {
               ItemStack mainItem = match.toItemStack();
               if (!mainItem.isEmpty()) {
                  ItemStack copy = mainItem.copy();
                  if (!player.inventory.addItemStackToInventory(copy)) {
                     ShopSavedData overflowData = ShopSavedData.get(server.getWorld(0));
                     NBTTagCompound itemNbt = new NBTTagCompound();
                     itemNbt.setString("itemId", match.getItemId());
                     itemNbt.setInteger("meta", match.getItemMeta());
                     itemNbt.setInteger("count", match.getItemCount());
                     if (mainItem.hasTagCompound()) {
                        itemNbt.setTag("itemNbt", mainItem.getTagCompound().copy());
                     }

                     overflowData.addOverflowItem(player.getUniqueID(), itemNbt);
                     sender.sendMessage(new TextComponentString("§e  (Main item stored in overflow bank - inventory full)"));
                  }
               }

               if (match.hasBundleItems()) {
                  for(CrateLootEntry.BundleItem bi : match.getBundleItems()) {
                     ItemStack bundleStack = bi.toItemStack();
                     if (!bundleStack.isEmpty()) {
                        ItemStack biCopy = bundleStack.copy();
                        if (!player.inventory.addItemStackToInventory(biCopy)) {
                           ShopSavedData overflowData = ShopSavedData.get(server.getWorld(0));
                           NBTTagCompound biNbt = new NBTTagCompound();
                           biNbt.setString("itemId", bi.getItemId());
                           biNbt.setInteger("meta", bi.getItemMeta());
                           biNbt.setInteger("count", bi.getItemCount());
                           if (bundleStack.hasTagCompound()) {
                              biNbt.setTag("itemNbt", bundleStack.getTagCompound().copy());
                           }

                           overflowData.addOverflowItem(player.getUniqueID(), biNbt);
                           sender.sendMessage(new TextComponentString("§e  (Bundle item stored in overflow bank - inventory full)"));
                        }
                     }
                  }
               }

               if (match.hasAdvancement()) {
                  grantAdvancement(server, player, match.getAdvancementId());
                  if (match.getAdvancementId().contains("uchiha_unlocked")) {
                     grantAdvancement(server, player, "clansaddon:uchiha/root");
                  }

                  if (match.getAdvancementId().contains("rinnegan_unlocked")) {
                     grantAdvancement(server, player, "clansaddon:otsutsuki/root");
                  }
               }

               ShopSavedData data = ShopSavedData.get(server.getWorld(0));
               UUID playerId = player.getUniqueID();
               if (match.hasDisplayNameOverride()) {
                  data.addOwnedItem(playerId, match.getDisplayNameOverride());
               } else {
                  data.addOwnedItem(playerId, match.getItemId() + ":" + match.getItemMeta());
               }

               String wonDisplayName = match.getDisplayName();
               ShopHistoryEntry historyEntry = new ShopHistoryEntry(matchCrateId, match.getItemId(), match.getItemMeta(), match.getItemCount(), wonDisplayName, match.getRarity().id, System.currentTimeMillis());
               data.addHistoryEntry(playerId, historyEntry);
               ShopNetworkHelper.sendShopSync(player);
               sender.sendMessage(new TextComponentString("§6[ShopAdmin] §fTest roll from " + shopType + "/" + category + ":"));
               sender.sendMessage(new TextComponentString("§a  Main: §f" + (mainItem.isEmpty() ? match.getItemId() : mainItem.getDisplayName()) + " §7(" + match.getRarity().displayName + ")"));
               if (match.hasBundleItems()) {
                  for(CrateLootEntry.BundleItem bi : match.getBundleItems()) {
                     ItemStack biStack = bi.toItemStack();
                     sender.sendMessage(new TextComponentString("§a  Bundle: §f" + (biStack.isEmpty() ? bi.getItemId() : biStack.getDisplayName())));
                  }
               }

               if (match.hasAdvancement()) {
                  sender.sendMessage(new TextComponentString("§a  Advancement: §f" + match.getAdvancementId()));
               }

               sender.sendMessage(new TextComponentString("§a  Ownership recorded. History entry added."));
            }
         }
      }
   }

   private static void addCrateIfExists(List<CrateDefinition> list, String crateId) {
      CrateDefinition crate = CrateRegistry.getById(crateId);
      if (crate != null) {
         list.add(crate);
      }

   }

   private static void grantAdvancement(MinecraftServer server, EntityPlayerMP player, String advancementId) {
      if (advancementId != null && !advancementId.isEmpty()) {
         try {
            ResourceLocation loc = new ResourceLocation(advancementId);
            Advancement advancement = server.getAdvancementManager().getAdvancement(loc);
            if (advancement != null) {
               AdvancementProgress progress = player.getAdvancements().getProgress(advancement);

               for(String criterion : progress.getRemaningCriteria()) {
                  player.getAdvancements().grantCriterion(advancement, criterion);
               }
            }
         } catch (Exception var8) {
         }

      }
   }

   private static List<String> collectDisplayNames(List<CrateDefinition> crates) {
      Set<String> names = new LinkedHashSet();

      for(CrateDefinition crate : crates) {
         for(CrateLootEntry entry : crate.getEntries()) {
            if (entry.hasDisplayNameOverride()) {
               names.add(entry.getDisplayNameOverride());
            } else {
               names.add(entry.getItemId());
            }
         }
      }

      return new ArrayList(names);
   }

   private static List<CrateDefinition> getCratesForTypeAndCategory(String shopType, String category) {
      CrateRegistry.init();
      List<CrateDefinition> crates = new ArrayList();
      if (shopType.equals("crystal")) {
         switch (category) {
            case "clan":
               addCrateIfExists(crates, "cash_clan");
               break;
            case "so6p":
               addCrateIfExists(crates, "cash_so6p");
               break;
            case "tb":
               addCrateIfExists(crates, "cash_tailed_beast");
               break;
            case "weapon":
               addCrateIfExists(crates, "weapon_crate_1");
               addCrateIfExists(crates, "weapon_crate_2");
               addCrateIfExists(crates, "weapon_crate_3");
               addCrateIfExists(crates, "weapon_crate_4");
               addCrateIfExists(crates, "weapon_crate_5");
         }
      } else {
         switch (category) {
            case "clan":
               addCrateIfExists(crates, "clan_crate_1");
               addCrateIfExists(crates, "clan_crate_2");
               addCrateIfExists(crates, "clan_crate_3");
               addCrateIfExists(crates, "clan_crate_4");
               addCrateIfExists(crates, "clan_crate_5");
               break;
            case "so6p":
               addCrateIfExists(crates, "so6p_crate_1");
               addCrateIfExists(crates, "so6p_crate_2");
               addCrateIfExists(crates, "so6p_crate_3");
         }
      }

      return crates;
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      if (args.length == 1) {
         List<String> completions = new ArrayList();
         completions.add("setbalance");
         completions.add("addbalance");
         completions.add("settokens");
         completions.add("resethistory");
         completions.add("history");
         completions.add("rollryo");
         completions.add("rolltoken");
         completions.add("rollcrystal");
         completions.add("cleardupes");
         completions.add("clearclandupes");
         completions.addAll(Arrays.asList(server.getOnlinePlayerNames()));
         return getListOfStringsMatchingLastWord(args, completions);
      } else {
         if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("setbalance") || sub.equals("addbalance") || sub.equals("settokens") || sub.equals("resethistory") || sub.equals("history")) {
               return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            }

            if (sub.equals("rollryo") || sub.equals("rolltoken")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"clan", "so6p"});
            }

            if (sub.equals("rollcrystal")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"clan", "so6p", "tb", "weapon"});
            }
         }

         if (args.length == 3 && args[0].equalsIgnoreCase("rollcrystal") && args[1].equalsIgnoreCase("weapon")) {
            return getListOfStringsMatchingLastWord(args, new String[]{"1", "2", "3", "4", "5"});
         } else if (args.length >= 4 && args[0].equalsIgnoreCase("rollcrystal") && args[1].equalsIgnoreCase("weapon")) {
            try {
               int tier = Integer.parseInt(args[2]);
               if (tier >= 1 && tier <= 5) {
                  String[] weaponCrateIds = new String[]{"weapon_crate_1", "weapon_crate_2", "weapon_crate_3", "weapon_crate_4", "weapon_crate_5"};
                  CrateRegistry.ensureWeaponCratesLoaded();
                  List<CrateDefinition> crates = new ArrayList();
                  addCrateIfExists(crates, weaponCrateIds[tier - 1]);
                  if (!crates.isEmpty()) {
                     List<String> names = collectDisplayNames(crates);
                     StringBuilder partial = new StringBuilder();

                     for(int i = 3; i < args.length; ++i) {
                        if (i > 3) {
                           partial.append(" ");
                        }

                        partial.append(args[i]);
                     }

                     String partialStr = partial.toString().toLowerCase();
                     List<String> matching = new ArrayList();

                     for(String name : names) {
                        if (name.toLowerCase().contains(partialStr)) {
                           matching.add(name);
                        }
                     }

                     return matching;
                  }
               }
            } catch (NumberFormatException var15) {
            }

            return Collections.emptyList();
         } else {
            if (args.length >= 3) {
               String sub = args[0].toLowerCase();
               if (sub.equals("rollryo") || sub.equals("rolltoken") || sub.equals("rollcrystal")) {
                  String category = args[1].toLowerCase();
                  if (category.equals("weapon")) {
                     return Collections.emptyList();
                  }

                  String shopType = sub.equals("rollcrystal") ? "crystal" : (sub.equals("rollryo") ? "ryo" : "token");
                  List<CrateDefinition> crates = getCratesForTypeAndCategory(shopType, category);
                  if (!crates.isEmpty()) {
                     List<String> names = collectDisplayNames(crates);
                     StringBuilder partial = new StringBuilder();

                     for(int i = 2; i < args.length; ++i) {
                        if (i > 2) {
                           partial.append(" ");
                        }

                        partial.append(args[i]);
                     }

                     String partialStr = partial.toString().toLowerCase();
                     List<String> matching = new ArrayList();

                     for(String name : names) {
                        if (name.toLowerCase().contains(partialStr)) {
                           matching.add(name);
                        }
                     }

                     return matching;
                  }
               }
            }

            return Collections.emptyList();
         }
      }
   }
}
