
package net.luck.narutoaddon.OtherCode.shop.command;

import com.mojang.authlib.GameProfile;
import net.luck.narutoaddon.OtherCode.shop.core.CrystalManager;
import net.luck.narutoaddon.OtherCode.shop.core.ItemRarity;
import net.luck.narutoaddon.OtherCode.shop.core.ShopHistoryEntry;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.luck.narutoaddon.OtherCode.shop.network.ShopNetworkHelper;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassDefinition;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassManager;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassSeason;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommandCrystalAdmin extends CommandBase {
   private static final NumberFormat NUM_FMT;
   private static final SimpleDateFormat DATE_FMT;

   public String getName() {
      return "crystaladmin";
   }

   public String getUsage(ICommandSender sender) {
      return "/crystaladmin <give|remove|check|stats|history|lookup|resetseason|fixbp|givesarutobi|weekendpromo> <player|on|off|status> [amount|page]";
   }

   public int getRequiredPermissionLevel() {
      return 4;
   }

   public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
      return sender.canUseCommand(2, this.getName());
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 1) {
         sender.sendMessage(new TextComponentString("§cUsage: " + this.getUsage(sender)));
      } else {
         switch (args[0].toLowerCase()) {
            case "give":
               this.handleGive(server, sender, args);
               return;
            case "remove":
               this.handleRemove(server, sender, args);
               return;
            case "check":
               if (args.length < 2) {
                  sender.sendMessage(new TextComponentString("§cUsage: /crystaladmin check <player>"));
                  return;
               }

               this.handleCheck(server, sender, args);
               return;
            case "stats":
               this.handleStats(server, sender, args);
               return;
            case "history":
               this.handleHistory(server, sender, args);
               return;
            case "lookup":
               this.handleLookup(server, sender, args);
               return;
            case "resetseason":
               this.handleResetSeason(server, sender);
               return;
            case "fixbp":
               this.handleFixBp(server, sender, args);
               return;
            case "givesarutobi":
               this.handleGiveSarutobi(server, sender, args);
               return;
            case "grantmedical":
               this.handleGrantMedical(server, sender);
               return;
            case "giveeffect":
               this.handleGiveEffect(server, sender, args);
               return;
            case "nextseason":
               this.handleNextSeason(server, sender);
               return;
            case "weekendpromo":
               this.handleWeekendPromo(server, sender, args);
               return;
            case "resetdupes":
               this.handleResetDupes(server, sender);
               return;
            default:
               sender.sendMessage(new TextComponentString("§cUnknown subcommand: " + subcommand + ". Use give, remove, check, stats, history, lookup, resetseason, fixbp, givesarutobi, grantmedical, giveeffect, nextseason, weekendpromo, or resetdupes."));
         }
      }
   }

   private void handleGive(MinecraftServer server, ICommandSender sender, String[] args) {
      if (!sender.canUseCommand(4, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 4 to give crystals."));
      } else if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /crystaladmin give <player> <amount>"));
      } else {
         UUID targetUUID = this.resolvePlayerUUID(server, args[1]);
         if (targetUUID == null) {
            sender.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
         } else {
            int amount;
            try {
               amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException var10) {
               sender.sendMessage(new TextComponentString("§cInvalid amount: " + args[2]));
               return;
            }

            if (amount > 0 && amount <= 100000) {
               String executor = sender.getName();
               CrystalManager manager = CrystalManager.getInstance();
               int result = manager.addCrystals(targetUUID, amount, server.getWorld(0), executor);
               if (result == -1) {
                  sender.sendMessage(new TextComponentString("§cRate limited! Too many crystal grants for this player. Wait 60 seconds."));
               } else {
                  sender.sendMessage(new TextComponentString("§aGave " + amount + " Chakra Crystals to " + args[1] + ". New balance: " + result));
                  EntityPlayerMP target = server.getPlayerList().getPlayerByUUID(targetUUID);
                  if (target != null && target != sender) {
                     target.sendMessage(new TextComponentString("§d[§5CRYSTAL§d] §7You received §d" + amount + " Chakra Crystals§7!"));
                  }

               }
            } else {
               sender.sendMessage(new TextComponentString("§cAmount must be between 1 and 100,000."));
            }
         }
      }
   }

   private void handleRemove(MinecraftServer server, ICommandSender sender, String[] args) {
      if (!sender.canUseCommand(4, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 4 to remove crystals."));
      } else if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /crystaladmin remove <player> <amount>"));
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

            if (amount > 0 && amount <= 100000) {
               ShopSavedData data = ShopSavedData.get(server.getWorld(0));
               boolean success = data.removeCrystals(targetUUID, amount);
               if (!success) {
                  int current = data.getCrystals(targetUUID);
                  sender.sendMessage(new TextComponentString("§cInsufficient crystals. " + args[1] + " only has " + current + " Chakra Crystals."));
               } else {
                  int newBalance = data.getCrystals(targetUUID);
                  sender.sendMessage(new TextComponentString("§aRemoved " + amount + " Chakra Crystals from " + args[1] + ". New balance: " + newBalance));
               }
            } else {
               sender.sendMessage(new TextComponentString("§cAmount must be between 1 and 100,000."));
            }
         }
      }
   }

   private void handleCheck(MinecraftServer server, ICommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /crystaladmin check <player>"));
      } else {
         UUID targetUUID = this.resolvePlayerUUID(server, args[1]);
         if (targetUUID == null) {
            sender.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
         } else {
            ShopSavedData data = ShopSavedData.get(server.getWorld(0));
            int balance = data.getCrystals(targetUUID);
            int fromTrades = data.getCrystalsFromTrades(targetUUID);
            sender.sendMessage(new TextComponentString("§d" + args[1] + "§7's Chakra Crystal balance: §d" + balance));
            sender.sendMessage(new TextComponentString("§dCrystals from Trades: §f" + fromTrades));
         }
      }
   }

   private void handleStats(MinecraftServer server, ICommandSender sender, String[] args) {
      if (!sender.canUseCommand(2, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 2+ to view stats."));
      } else {
         ShopSavedData data = ShopSavedData.get(server.getWorld(0));
         if (args.length >= 2) {
            UUID targetUUID = this.resolvePlayerUUID(server, args[1]);
            if (targetUUID == null) {
               sender.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
               return;
            }

            int purchased = data.getTotalCrystalsPurchased(targetUUID);
            int spent = data.getTotalCrystalsSpent(targetUUID);
            int balance = data.getCrystals(targetUUID);
            int purchaseCount = data.getCrystalPurchaseCount(targetUUID);
            int fromTrades = data.getCrystalsFromTrades(targetUUID);
            long firstDate = data.getFirstPurchaseDate(targetUUID);
            long lastDate = data.getLastPurchaseDate(targetUUID);
            String topCrate = data.getTopCrateForPlayer(targetUUID);
            int topCrateRolls = topCrate != null ? data.getCrateRollCount(targetUUID, topCrate) : 0;
            int pityHits = data.getPityHitCount(targetUUID);
            int dupeRefunds = data.getDupeRefundTotal(targetUUID);
            sender.sendMessage(new TextComponentString("§6=== Player Stats: " + args[1] + " ==="));
            sender.sendMessage(new TextComponentString("§dCrystals Purchased: §f" + NUM_FMT.format((long)purchased)));
            sender.sendMessage(new TextComponentString("§dCrystals from Trades: §f" + NUM_FMT.format((long)fromTrades)));
            sender.sendMessage(new TextComponentString("§dCrystals Spent: §f" + NUM_FMT.format((long)spent)));
            sender.sendMessage(new TextComponentString("§dCrystals Balance: §f" + NUM_FMT.format((long)balance)));
            sender.sendMessage(new TextComponentString("§dPurchase Count: §f" + purchaseCount));
            sender.sendMessage(new TextComponentString("§dFirst Purchase: §f" + (firstDate > 0L ? DATE_FMT.format(new Date(firstDate)) : "never")));
            sender.sendMessage(new TextComponentString("§dLast Purchase: §f" + (lastDate > 0L ? DATE_FMT.format(new Date(lastDate)) : "never")));
            sender.sendMessage(new TextComponentString("§dMost Rolled: §f" + (topCrate != null ? topCrate + " (" + topCrateRolls + " rolls)" : "none")));
            sender.sendMessage(new TextComponentString("§dPity Hits: §f" + pityHits));
            sender.sendMessage(new TextComponentString("§dDupe Refunds: §f" + NUM_FMT.format((long)dupeRefunds) + " crystals"));
         } else {
            Set<UUID> buyers = data.getAllCrystalBuyers();
            int totalSold = data.getServerTotalCrystalsPurchased();
            int totalSpent = data.getServerTotalCrystalsSpent();
            int uniqueBuyers = buyers.size();
            int repeatBuyers = data.getServerRepeatBuyerCount();
            int oneTimeBuyers = uniqueBuyers - repeatBuyers;
            int freeRollConversions = data.getServerFreeRollConversions();
            int freeRollTotal = data.getServerFreeRollTotal();
            String mostPopular = data.getServerMostPopularCrate();
            int totalDupeRefunds = data.getServerTotalDupeRefunds();
            String conversionRate = freeRollTotal > 0 ? (int)((double)freeRollConversions * (double)100.0F / (double)freeRollTotal) + "%" : "N/A";
            sender.sendMessage(new TextComponentString("§6=== Crystal Analytics ==="));
            sender.sendMessage(new TextComponentString("§dTotal Crystals Sold: §f" + NUM_FMT.format((long)totalSold)));
            sender.sendMessage(new TextComponentString("§dTotal Crystals Spent: §f" + NUM_FMT.format((long)totalSpent)));
            sender.sendMessage(new TextComponentString("§dUnique Buyers: §f" + uniqueBuyers));
            sender.sendMessage(new TextComponentString("§dRepeat Buyers (2+): §f" + repeatBuyers));
            sender.sendMessage(new TextComponentString("§dOne-Time Buyers: §f" + oneTimeBuyers));
            sender.sendMessage(new TextComponentString("§dFree Roll Conversions: §f" + freeRollConversions + "/" + freeRollTotal + " (" + conversionRate + ")"));
            sender.sendMessage(new TextComponentString("§dMost Popular Crate: §f" + mostPopular));
            sender.sendMessage(new TextComponentString("§dTotal Dupe Refunds: §f" + NUM_FMT.format((long)totalDupeRefunds) + " crystals"));
         }

      }
   }

   private void handleHistory(MinecraftServer server, ICommandSender sender, String[] args) {
      if (!sender.canUseCommand(2, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 2+ to view history."));
      } else if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /crystaladmin history <player> [page]"));
      } else {
         UUID targetUUID = this.resolvePlayerUUID(server, args[1]);
         if (targetUUID == null) {
            sender.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
         } else {
            int page = 1;
            if (args.length >= 3) {
               try {
                  page = Integer.parseInt(args[2]);
               } catch (NumberFormatException var21) {
                  sender.sendMessage(new TextComponentString("§cInvalid page number: " + args[2]));
                  return;
               }

               if (page < 1) {
                  page = 1;
               }
            }

            ShopSavedData data = ShopSavedData.get(server.getWorld(0));
            List<ShopHistoryEntry> allHistory = data.getHistory(targetUUID);
            List<ShopHistoryEntry> cashHistory = new ArrayList();

            for(ShopHistoryEntry entry : allHistory) {
               if (entry.getCrateId() != null && entry.getCrateId().startsWith("cash_")) {
                  cashHistory.add(entry);
               }
            }

            if (cashHistory.isEmpty()) {
               sender.sendMessage(new TextComponentString("§7No crystal crate history for " + args[1] + "."));
            } else {
               int perPage = 10;
               int totalPages = (cashHistory.size() + perPage - 1) / perPage;
               if (page > totalPages) {
                  page = totalPages;
               }

               int startIdx = (page - 1) * perPage;
               int endIdx = Math.min(startIdx + perPage, cashHistory.size());
               sender.sendMessage(new TextComponentString("§6=== Crystal History: " + args[1] + " (Page " + page + "/" + totalPages + ") ==="));
               long now = System.currentTimeMillis();

               for(int i = startIdx; i < endIdx; ++i) {
                  ShopHistoryEntry entry = (ShopHistoryEntry)cashHistory.get(i);
                  String timeAgo = formatTimeAgo(now - entry.getTimestamp());
                  String crateName = formatCrateName(entry.getCrateId());
                  ItemRarity rarity = entry.getRarity();
                  String dupeStr = entry.isDuplicate() ? " §e[DUPE: +" + entry.getTokenAmount() + " CC]" : "";
                  sender.sendMessage(new TextComponentString("§8[" + timeAgo + "] §d" + crateName + ": §f" + entry.getDisplayName() + " §7(" + rarity.displayName + ")" + dupeStr));
               }

               if (page < totalPages) {
                  sender.sendMessage(new TextComponentString("§7Use /crystaladmin history " + args[1] + " " + (page + 1) + " for next page"));
               }

            }
         }
      }
   }

   private void handleLookup(MinecraftServer server, ICommandSender sender, String[] args) {
      if (!sender.canUseCommand(2, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 2+ to use lookup."));
      } else if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /crystaladmin lookup <player>"));
      } else {
         UUID targetUUID = this.resolvePlayerUUID(server, args[1]);
         if (targetUUID == null) {
            sender.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
         } else {
            ShopSavedData data = ShopSavedData.get(server.getWorld(0));
            int balance = data.getCrystals(targetUUID);
            int purchased = data.getTotalCrystalsPurchased(targetUUID);
            int spent = data.getTotalCrystalsSpent(targetUUID);
            int purchaseCount = data.getCrystalPurchaseCount(targetUUID);
            int fromTrades = data.getCrystalsFromTrades(targetUUID);
            long firstDate = data.getFirstPurchaseDate(targetUUID);
            long lastDate = data.getLastPurchaseDate(targetUUID);
            String topCrate = data.getTopCrateForPlayer(targetUUID);
            int topCrateRolls = topCrate != null ? data.getCrateRollCount(targetUUID, topCrate) : 0;
            int pityHits = data.getPityHitCount(targetUUID);
            int dupeRefunds = data.getDupeRefundTotal(targetUUID);
            int bpTier = data.getBpPurchasedTier(targetUUID);
            int bpLevel = data.getBpCurrentLevel(targetUUID);
            boolean freeRollUsed = data.hasUsedFreeRoll(targetUUID);
            boolean firstBonusUsed = data.hasUsedFirstPurchaseBonus(targetUUID);
            String bpTierName;
            switch (bpTier) {
               case 1:
                  bpTierName = "Kage";
                  break;
               case 2:
                  bpTierName = "Otsutsuki";
                  break;
               default:
                  bpTierName = "Free";
            }

            sender.sendMessage(new TextComponentString("§6=== Crystal Profile: " + args[1] + " ==="));
            sender.sendMessage(new TextComponentString("§dCrystal Balance: §f" + NUM_FMT.format((long)balance)));
            sender.sendMessage(new TextComponentString("§dLifetime Purchased: §f" + NUM_FMT.format((long)purchased)));
            sender.sendMessage(new TextComponentString("§dCrystals from Trades: §f" + NUM_FMT.format((long)fromTrades)));
            sender.sendMessage(new TextComponentString("§dLifetime Spent: §f" + NUM_FMT.format((long)spent)));
            sender.sendMessage(new TextComponentString("§dPurchase Count: §f" + purchaseCount));
            sender.sendMessage(new TextComponentString("§dFirst Purchase: §f" + (firstDate > 0L ? DATE_FMT.format(new Date(firstDate)) : "never")));
            sender.sendMessage(new TextComponentString("§dLast Purchase: §f" + (lastDate > 0L ? DATE_FMT.format(new Date(lastDate)) : "never")));
            sender.sendMessage(new TextComponentString("§dMost Rolled Crate: §f" + (topCrate != null ? formatCrateName(topCrate) + " (" + topCrateRolls + " rolls)" : "none")));
            sender.sendMessage(new TextComponentString("§dPity Hits: §f" + pityHits));
            sender.sendMessage(new TextComponentString("§dDupe Refunds: §f" + NUM_FMT.format((long)dupeRefunds) + " CC"));
            sender.sendMessage(new TextComponentString("§dBattle Pass: §fTier " + bpTierName + " | Level " + bpLevel));
            sender.sendMessage(new TextComponentString("§dFree Roll Used: §f" + (freeRollUsed ? "yes" : "no")));
            sender.sendMessage(new TextComponentString("§dFirst Purchase Bonus: §f" + (firstBonusUsed ? "used" : "available")));
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

   private void handleFixBp(MinecraftServer server, ICommandSender sender, String[] args) {
      if (!sender.canUseCommand(4, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 4 to fix battle pass."));
      } else if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /crystaladmin fixbp <player>"));
      } else {
         UUID targetUUID = this.resolvePlayerUUID(server, args[1]);
         if (targetUUID == null) {
            sender.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
         } else {
            World world = server.getWorld(0);
            ShopSavedData shopData = ShopSavedData.get(world);
            shopData.removeOwnedItem(targetUUID, "bp_reward_fix_v3");
            shopData.removeOwnedItem(targetUUID, "bp_reward_fix_v1");
            shopData.removeOwnedItem(targetUUID, "bp_reward_fix_v2");
            EntityPlayerMP target = server.getPlayerList().getPlayerByUUID(targetUUID);
            if (target != null) {
               int bpLevel = shopData.getBpCurrentLevel(targetUUID);
               int bpPurchased = shopData.getBpPurchasedTier(targetUUID);
               boolean anyFixed = false;
               BattlePassManager bpMgr = BattlePassManager.getInstance();

               for(int t = 1; t <= bpLevel; ++t) {
                  if (shopData.hasBpClaimedTier(targetUUID, t)) {
                     BattlePassDefinition.TierReward freeR = BattlePassDefinition.getFreeTierReward(t);
                     if (freeR != null && !freeR.isEmpty()) {
                        bpMgr.grantRewardPublic(targetUUID, freeR, target.getServerWorld());
                        anyFixed = true;
                     }

                     if (bpPurchased > 0) {
                        BattlePassSeason curSeason = bpMgr.getCurrentSeason(target.getServerWorld());
                        BattlePassDefinition.TierReward premR = BattlePassDefinition.getPremiumTierReward(t, curSeason.getSeasonNumber());
                        if (premR != null && !premR.isEmpty()) {
                           bpMgr.grantRewardPublic(targetUUID, premR, target.getServerWorld());
                           anyFixed = true;
                        }

                        if (t == 15) {
                           BattlePassSeason season = bpMgr.getCurrentSeason(target.getServerWorld());
                           String effectId = season.getKillEffectId();
                           shopData.addOwnedKillEffect(targetUUID, effectId);
                           if (shopData.getActiveKillEffect(targetUUID) == null || shopData.getActiveKillEffect(targetUUID).isEmpty()) {
                              shopData.setActiveKillEffect(targetUUID, effectId);
                           }

                           anyFixed = true;
                        }

                        if (t == 20) {
                           BattlePassSeason season = bpMgr.getCurrentSeason(target.getServerWorld());
                           String jutsuId = season.getExclusiveJutsuItemId();
                           if (jutsuId != null && !jutsuId.isEmpty()) {
                              Item jutsuItem = Item.getByNameOrId(jutsuId);
                              if (jutsuItem != null) {
                                 ItemStack jutsuStack = new ItemStack(jutsuItem, 1, 0);
                                 if (!target.inventory.addItemStackToInventory(jutsuStack)) {
                                    NBTTagCompound ov = new NBTTagCompound();
                                    ov.setString("itemId", jutsuId);
                                    ov.setInteger("meta", 0);
                                    ov.setInteger("count", 1);
                                    shopData.addOverflowItem(targetUUID, ov);
                                 }
                              }
                           }

                           anyFixed = true;
                        }
                     }
                  }
               }

               shopData.addOwnedItem(targetUUID, "bp_reward_fix_v3");
               if (anyFixed) {
                  target.sendMessage(new TextComponentString("§6[Battle Pass] §7An admin has re-granted your missing Battle Pass rewards!"));
                  ShopNetworkHelper.sendBattlePassSync(target);
                  ShopNetworkHelper.sendShopSync(target);
               }

               sender.sendMessage(new TextComponentString("§aBattle Pass fix applied to " + args[1] + " (level=" + bpLevel + ", pass=" + bpPurchased + ", fixed=" + anyFixed + ")"));
            } else {
               sender.sendMessage(new TextComponentString("§a" + args[1] + " is offline. Migration marker reset — fix will run on next login."));
            }

         }
      }
   }

   private void handleGiveSarutobi(MinecraftServer server, ICommandSender sender, String[] args) {
      if (!sender.canUseCommand(4, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 4 to use givesarutobi."));
      } else if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /crystaladmin givesarutobi <player>"));
      } else {
         EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(args[1]);
         if (target == null) {
            sender.sendMessage(new TextComponentString("§cPlayer must be online: " + args[1]));
         } else {
            ShopSavedData data = ShopSavedData.get(server.getWorld(0));
            UUID targetUUID = target.getUniqueID();
            Item bladesItem = Item.getByNameOrId("narutomod:chakra_blades");
            if (bladesItem == null) {
               sender.sendMessage(new TextComponentString("§cERROR: narutomod:chakra_blades item not found!"));
            } else {
               ItemStack rightBlade = new ItemStack(bladesItem, 1, 0);
               NBTTagCompound rightNbt = new NBTTagCompound();
               NBTTagList rightEnchList = new NBTTagList();
               NBTTagCompound rightEnch = new NBTTagCompound();
               rightEnch.setShort("id", (short)16);
               rightEnch.setShort("lvl", (short)100);
               rightEnchList.appendTag(rightEnch);
               rightNbt.setTag("ench", rightEnchList);
               rightNbt.setBoolean("Unbreakable", true);
               NBTTagCompound rightDisplay = new NBTTagCompound();
               rightDisplay.setString("Name", "§lAsuma's Chakra Blades [Right]");
               rightNbt.setTag("display", rightDisplay);
               rightBlade.setTagCompound(rightNbt);
               ItemStack leftBlade = new ItemStack(bladesItem, 1, 0);
               NBTTagCompound leftNbt = new NBTTagCompound();
               NBTTagList leftEnchList = new NBTTagList();
               NBTTagCompound leftEnch = new NBTTagCompound();
               leftEnch.setShort("id", (short)16);
               leftEnch.setShort("lvl", (short)100);
               leftEnchList.appendTag(leftEnch);
               leftNbt.setTag("ench", leftEnchList);
               leftNbt.setBoolean("Unbreakable", true);
               NBTTagCompound leftDisplay = new NBTTagCompound();
               leftDisplay.setString("Name", "§lAsuma's Chakra Blades [Left]");
               leftNbt.setTag("display", leftDisplay);
               leftBlade.setTagCompound(leftNbt);
               Item scrollItem = Item.getByNameOrId("narutomod:scroll_futon_chakra_flow");
               if (scrollItem == null) {
                  sender.sendMessage(new TextComponentString("§cERROR: narutomod:scroll_futon_chakra_flow item not found!"));
               } else {
                  ItemStack scroll = new ItemStack(scrollItem, 1, 0);
                  ItemStack[] items = new ItemStack[]{rightBlade, leftBlade, scroll};
                  String[] itemIds = new String[]{"narutomod:chakra_blades", "narutomod:chakra_blades", "narutomod:scroll_futon_chakra_flow"};

                  for(int i = 0; i < items.length; ++i) {
                     if (!target.inventory.addItemStackToInventory(items[i])) {
                        NBTTagCompound overflowNbt = new NBTTagCompound();
                        overflowNbt.setString("itemId", itemIds[i]);
                        overflowNbt.setInteger("meta", 0);
                        overflowNbt.setInteger("count", 1);
                        if (items[i].getTagCompound() != null) {
                           overflowNbt.setTag("itemNbt", items[i].getTagCompound());
                        }

                        data.addOverflowItem(targetUUID, overflowNbt);
                     }
                  }

                  sender.sendMessage(new TextComponentString("§aGranted Sarutobi items to " + args[1]));
               }
            }
         }
      }
   }

   private void handleGrantMedical(MinecraftServer server, ICommandSender sender) {
      String[] playerNames = new String[]{"alterwrld999", "arcticapollo75", "atheos666", "azeecechoh", "beast_sd", "bigfiercedeity", "blank13412", "dfuzexhunters", "drowsyguy", "happygilmore694", "hechting21", "itzhka", "leigg0", "midnight_reaper", "owenmodz4", "pearince", "rainwolfsky", "subject12", "xarelex", "xenexas"};
      String[] itemIds = new String[]{"narutomod:iryo_jutsu", "narutomod:scroll_healing"};
      ShopSavedData data = ShopSavedData.get(server.getWorld(0));
      int granted = 0;

      for(String name : playerNames) {
         EntityPlayerMP online = server.getPlayerList().getPlayerByUsername(name);
         if (online != null) {
            for(String itemId : itemIds) {
               Item item = Item.getByNameOrId(itemId);
               if (item != null) {
                  ItemStack stack = new ItemStack(item, 1, 0);
                  if (!online.inventory.addItemStackToInventory(stack)) {
                     NBTTagCompound ov = new NBTTagCompound();
                     ov.setString("itemId", itemId);
                     ov.setInteger("meta", 0);
                     ov.setInteger("count", 1);
                     data.addOverflowItem(online.getUniqueID(), ov);
                  }
               }
            }

            ++granted;
         } else {
            GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(name);
            if (profile == null) {
               sender.sendMessage(new TextComponentString("§cPlayer not found: " + name));
            } else {
               UUID uuid = profile.getId();

               for(String itemId : itemIds) {
                  NBTTagCompound ov = new NBTTagCompound();
                  ov.setString("itemId", itemId);
                  ov.setInteger("meta", 0);
                  ov.setInteger("count", 1);
                  data.addOverflowItem(uuid, ov);
               }

               ++granted;
            }
         }
      }

      data.markDirty();
      sender.sendMessage(new TextComponentString("§aGranted Medical items (iryo_jutsu + scroll_healing) to " + granted + "/" + playerNames.length + " players."));
   }

   private void handleResetSeason(MinecraftServer server, ICommandSender sender) {
      if (!sender.canUseCommand(4, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 4 to reset the season."));
      } else {
         ShopSavedData data = ShopSavedData.get(server.getWorld(0));
         data.setBpSeasonNumber(1);
         data.setBpSeasonStartTime(BattlePassSeason.getAlignedStartTime(System.currentTimeMillis()));
         data.resetAllBattlePassData();
         sender.sendMessage(new TextComponentString("§aBattle Pass season reset to Season 1"));
      }
   }

   private void handleNextSeason(MinecraftServer server, ICommandSender sender) {
      if (!sender.canUseCommand(4, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 4 to advance the season."));
      } else {
         ShopSavedData data = ShopSavedData.get(server.getWorld(0));
         int oldSeason = data.getBpSeasonNumber();
         if (oldSeason <= 0) {
            oldSeason = 1;
         }

         int newSeason = oldSeason + 1;
         BattlePassManager bpMgr = BattlePassManager.getInstance();
         bpMgr.queueAutoClaimForReset(server.getWorld(0));
         data.setBpSeasonNumber(newSeason);
         data.setBpSeasonStartTime(BattlePassSeason.getAlignedStartTime(System.currentTimeMillis()));
         data.resetAllBattlePassData();
         String oldName = BattlePassSeason.getSeasonDisplayName(oldSeason);
         String newName = BattlePassSeason.getSeasonDisplayName(newSeason);
         int synced = 0;

         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            bpMgr.processPendingAutoClaim(player.getUniqueID(), server.getWorld(0));
            ShopNetworkHelper.sendShopSync(player);
            ShopNetworkHelper.sendBattlePassSync(player);
            player.sendMessage(new TextComponentString("§6§l" + oldName + " has ended! §eWelcome to §b§l" + newName + "§e."));
            ++synced;
         }

         System.out.println("[BattlePass] Manual advance: " + oldName + " -> " + newName + " (synced " + synced + " online players)");
         sender.sendMessage(new TextComponentString("§aAdvanced from §e" + oldName + "§a to §b" + newName + "§a. All player progress reset. " + synced + " online players synced. Offline players will get unclaimed rewards on next login."));
      }
   }

   private void handleGiveEffect(MinecraftServer server, ICommandSender sender, String[] args) {
      if (!sender.canUseCommand(4, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 4 to give kill effects."));
      } else if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /crystaladmin giveeffect <player> <effectId>"));
         sender.sendMessage(new TextComponentString("§7Available: s1_fire_burst, s2_lightning_strike, s3_shadow_dissolve"));
      } else {
         UUID targetUUID = this.resolvePlayerUUID(server, args[1]);
         if (targetUUID == null) {
            sender.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
         } else {
            String effectId = args[2].toLowerCase();
            ShopSavedData data = ShopSavedData.get(server.getWorld(0));
            data.addOwnedKillEffect(targetUUID, effectId);
            data.setActiveKillEffect(targetUUID, effectId);
            sender.sendMessage(new TextComponentString("§aGranted kill effect '§b" + effectId + "§a' to " + args[1] + " and set as active."));
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target != null) {
               ShopNetworkHelper.sendBattlePassSync(target);
            }

         }
      }
   }

   private void handleResetDupes(MinecraftServer server, ICommandSender sender) {
      if (!sender.canUseCommand(4, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 4 to reset duplicate tracking."));
      } else {
         ShopSavedData data = ShopSavedData.get(server.getWorld(0));
         int totalCleared = data.resetDuplicateTrackingAll();
         data.markDirty();
         sender.sendMessage(new TextComponentString("§aCleared " + totalCleared + " duplicate-tracking entries across all players. Battle pass markers and internal flags preserved. Players can now obtain previously-owned items again."));
      }
   }

   private void handleWeekendPromo(MinecraftServer server, ICommandSender sender, String[] args) {
      if (!sender.canUseCommand(4, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 4 to manage the weekend promo."));
      } else if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /crystaladmin weekendpromo <on|off|status>"));
      } else {
         ShopSavedData data = ShopSavedData.get(server.getWorld(0));
         switch (args[1].toLowerCase()) {
            case "on":
               data.setWeekendPromoSO6PEnabled(true);
               sender.sendMessage(new TextComponentString("§aWeekend Premium SO6P promo: §2ENABLED§a. Returning players will receive a free roll on their next login."));
               return;
            case "off":
               data.setWeekendPromoSO6PEnabled(false);
               sender.sendMessage(new TextComponentString("§eWeekend Premium SO6P promo: §cDISABLED§e. No new grants. Players already granted a free roll keep it."));
               return;
            case "status":
               boolean enabled = data.isWeekendPromoSO6PEnabled();
               int claimed = 0;
               int pending = 0;

               for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                  UUID id = player.getUniqueID();
                  if (data.hasOwnedItem(id, "weekend_promo_so6p_claimed")) {
                     ++claimed;
                  } else if (data.hasOwnedItem(id, "weekend_promo_so6p_available")) {
                     ++pending;
                  }
               }

               int offlineClaimed = data.countPlayersWithOwnedItem("weekend_promo_so6p_claimed");
               int offlinePending = data.countPlayersWithOwnedItem("weekend_promo_so6p_available");
               sender.sendMessage(new TextComponentString("§6=== Weekend SO6P Promo ==="));
               sender.sendMessage(new TextComponentString("§dEnabled: §f" + (enabled ? "§aYES" : "§cNO")));
               sender.sendMessage(new TextComponentString("§dClaimed (consumed): §f" + offlineClaimed + " total (§7" + claimed + " currently online§f)"));
               sender.sendMessage(new TextComponentString("§dPending (unused): §f" + offlinePending + " total (§7" + pending + " currently online§f)"));
               return;
            default:
               sender.sendMessage(new TextComponentString("§cUnknown action: " + action + ". Use on, off, or status."));
         }
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

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"give", "remove", "check", "stats", "history", "lookup", "resetseason", "fixbp", "givesarutobi", "grantmedical", "giveeffect", "nextseason", "weekendpromo"});
      } else if (args.length == 2) {
         return "weekendpromo".equalsIgnoreCase(args[0]) ? getListOfStringsMatchingLastWord(args, new String[]{"on", "off", "status"}) : getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
      } else {
         return Collections.emptyList();
      }
   }

   static {
      NUM_FMT = NumberFormat.getIntegerInstance(Locale.US);
      DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
   }
}
