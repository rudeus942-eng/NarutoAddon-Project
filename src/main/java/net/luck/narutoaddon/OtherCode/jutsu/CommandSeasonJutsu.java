
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class CommandSeasonJutsu extends CommandBase {
   private static final SeasonEntry[] SEASONS = new SeasonEntry[]{new SeasonEntry(1, 0, "Blazing Decimation"), new SeasonEntry(2, 1, "Kirin"), new SeasonEntry(3, 2, "Shadow Rend")};
   private static final Map<String, SeasonEntry> BY_KEY = new LinkedHashMap();
   private static final String[] SEASON_KEYS;

   public String getName() {
      return "seasonjutsu";
   }

   public String getUsage(ICommandSender sender) {
      return "/seasonjutsu <give|unlock|bpcheck|fixseasons> <player> [all|" + buildSeasonList() + "]";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 1) {
         this.sendHelp(sender);
      } else {
         switch (args[0].toLowerCase()) {
            case "give":
               this.handleGive(server, sender, args);
               break;
            case "unlock":
               this.handleUnlock(server, sender, args);
               break;
            case "bpcheck":
               this.handleBpCheck(server, sender, args);
               break;
            case "fixseasons":
               this.handleFixSeasons(server, sender);
               break;
            default:
               this.sendHelp(sender);
         }

      }
   }

   private void handleGive(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /seasonjutsu give <player> [all|" + buildSeasonList() + "]"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         String choice = args.length >= 3 ? args[2].toLowerCase() : "all";
         Item seasonalItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("inftsukaddon", "seasonal_release"));
         if (seasonalItem == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Seasonal release item not registered"));
         } else {
            ItemStack stack = new ItemStack(seasonalItem);
            NBTTagCompound nbt = new NBTTagCompound();
            String unlockedDesc;
            if ("all".equals(choice)) {
               for(SeasonEntry s : SEASONS) {
                  nbt.setLong(s.nbtKey(), 0L);
               }

               unlockedDesc = "All seasons (" + buildJutsuNameList() + ")";
            } else {
               SeasonEntry entry = (SeasonEntry)BY_KEY.get(choice);
               if (entry == null) {
                  sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown season: " + choice + ". Use: all, " + buildSeasonList()));
                  return;
               }

               nbt.setLong(entry.nbtKey(), 0L);
               unlockedDesc = entry.key().toUpperCase() + " (" + entry.jutsuName + ")";
            }

            stack.setTagCompound(nbt);
            if (!target.inventory.addItemStackToInventory(stack)) {
               ShopSavedData shopData = ShopSavedData.get(server.getWorld(0));
               if (shopData != null) {
                  NBTTagCompound ov = new NBTTagCompound();
                  ov.setString("itemId", "inftsukaddon:seasonal_release");
                  ov.setInteger("meta", 0);
                  ov.setInteger("count", 1);
                  ov.setTag("itemNbt", nbt);
                  shopData.addOverflowItem(target.getUniqueID(), ov);
                  sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Inventory full - sent to " + target.getName() + "'s bank."));
               }
            }

            target.inventoryContainer.detectAndSendChanges();
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Gave seasonal release to " + target.getName() + " with " + unlockedDesc + " unlocked."));
            target.sendMessage(new TextComponentString(TextFormatting.GOLD + "" + TextFormatting.BOLD + "[JUTSU] " + TextFormatting.YELLOW + "You received a Seasonal Release with " + unlockedDesc + " unlocked!"));
         }
      }
   }

   private void handleUnlock(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /seasonjutsu unlock <player> [all|" + buildSeasonList() + "]"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         String choice = args.length >= 3 ? args[2].toLowerCase() : "all";
         Item seasonalItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("inftsukaddon", "seasonal_release"));
         if (seasonalItem == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Seasonal release item not registered"));
         } else {
            List<SeasonEntry> toUnlock = new ArrayList();
            if ("all".equals(choice)) {
               Collections.addAll(toUnlock, SEASONS);
            } else {
               SeasonEntry entry = (SeasonEntry)BY_KEY.get(choice);
               if (entry == null) {
                  sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown season: " + choice + ". Use: all, " + buildSeasonList()));
                  return;
               }

               toUnlock.add(entry);
            }

            boolean found = false;

            for(int i = 0; i < target.inventory.getSizeInventory(); ++i) {
               ItemStack stack = target.inventory.getStackInSlot(i);
               if (!stack.isEmpty() && stack.getItem() == seasonalItem) {
                  NBTTagCompound nbt = stack.getTagCompound();
                  if (nbt == null) {
                     nbt = new NBTTagCompound();
                     stack.setTagCompound(nbt);
                  }

                  for(SeasonEntry s : toUnlock) {
                     nbt.setLong(s.nbtKey(), 0L);
                  }

                  found = true;
               }
            }

            if (found) {
               target.inventoryContainer.detectAndSendChanges();
               String desc = "all".equals(choice) ? "all seasons" : choice.toUpperCase();
               sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Unlocked " + desc + " on " + target.getName() + "'s seasonal release."));
               target.sendMessage(new TextComponentString(TextFormatting.GOLD + "" + TextFormatting.BOLD + "[JUTSU] " + TextFormatting.YELLOW + "Season jutsu unlocked on your seasonal release!"));
            } else {
               sender.sendMessage(new TextComponentString(TextFormatting.RED + target.getName() + " has no seasonal release in their inventory. Use /seasonjutsu give " + target.getName()));
            }

         }
      }
   }

   private void handleBpCheck(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /seasonjutsu bpcheck <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         UUID uuid = target.getUniqueID();
         World world = server.getWorld(0);
         ShopSavedData data = ShopSavedData.get(world);
         if (data == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not access shop data."));
         } else {
            int currentLevel = data.getBpCurrentLevel(uuid);
            int currentXP = data.getBpCurrentXP(uuid);
            int purchasedTier = data.getBpPurchasedTier(uuid);
            Set<Integer> claimedTiers = data.getBpClaimedTiers(uuid);
            sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "" + TextFormatting.BOLD + "=== Battle Pass: " + target.getName() + " ==="));
            sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "Level: " + TextFormatting.WHITE + currentLevel + TextFormatting.GRAY + " | " + TextFormatting.AQUA + "XP: " + TextFormatting.WHITE + currentXP));
            String passType;
            switch (purchasedTier) {
               case 0:
                  passType = TextFormatting.GRAY + "Free";
                  break;
               case 1:
                  passType = TextFormatting.GOLD + "Kage Pass";
                  break;
               default:
                  passType = TextFormatting.LIGHT_PURPLE + "Otsutsuki Pass";
            }

            sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "Pass: " + passType));
            int unclaimed = 0;
            StringBuilder unclaimedList = new StringBuilder();

            for(int tier = 1; tier <= Math.min(currentLevel, 20); ++tier) {
               if (!claimedTiers.contains(tier)) {
                  ++unclaimed;
                  if (unclaimedList.length() > 0) {
                     unclaimedList.append(", ");
                  }

                  unclaimedList.append(tier);
               }
            }

            if (unclaimed > 0) {
               sender.sendMessage(new TextComponentString(TextFormatting.RED + "" + TextFormatting.BOLD + unclaimed + " UNCLAIMED TIERS: " + TextFormatting.YELLOW + unclaimedList.toString()));
            } else {
               sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "All earned tiers claimed!"));
            }

            sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Claimed tiers: " + claimedTiers.size() + "/" + Math.min(currentLevel, 20)));
         }
      }
   }

   private void handleFixSeasons(MinecraftServer server, ICommandSender sender) {
      World world = server.getWorld(0);
      ShopSavedData data = ShopSavedData.get(world);
      if (data == null) {
         sender.sendMessage(new TextComponentString(TextFormatting.RED + "Could not access shop data."));
      } else {
         BattlePassManager bpm = BattlePassManager.getInstance();
         Set<UUID> allBpPlayers = data.getAllBattlePassPlayers();
         int s2Marked = 0;
         int s3Marked = 0;

         for(UUID playerId : allBpPlayers) {
            Set<Integer> claimed = data.getBpClaimedTiers(playerId);
            if (claimed.contains(20) && !data.hasOwnedItem(playerId, "bp_kirin_eligible")) {
               data.addOwnedItem(playerId, "bp_kirin_eligible");
               ++s2Marked;
            }
         }

         Set<UUID> kirinPlayers = data.getPlayersWithOwnedItem("bp_kirin_eligible");
         int kirinFixed = 0;

         for(UUID playerId : kirinPlayers) {
            bpm.grantOrUnlockSeasonalJutsu(playerId, 2, world);
            ++kirinFixed;
         }

         Set<UUID> shadowPlayers = data.getPlayersWithOwnedItem("bp_season_3_jutsu_eligible");
         int shadowFixed = 0;

         for(UUID playerId : shadowPlayers) {
            bpm.grantOrUnlockSeasonalJutsu(playerId, 3, world);
            ++shadowFixed;
         }

         sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "" + TextFormatting.BOLD + "Season fix applied globally:"));
         if (s2Marked > 0) {
            sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "- Retroactively marked " + TextFormatting.YELLOW + s2Marked + TextFormatting.GRAY + " players as Kirin eligible (had tier 20 but no marker)"));
         }

         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "- Kirin (S2): granted/unlocked for " + TextFormatting.YELLOW + kirinFixed + TextFormatting.GRAY + " eligible players"));
         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "- Shadow Rend (S3): granted/unlocked for " + TextFormatting.YELLOW + shadowFixed + TextFormatting.GRAY + " eligible players"));
         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "- Online players: unlocked on existing items"));
         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "- Offline players: sent to overflow bank"));
      }
   }

   private static String buildSeasonList() {
      StringBuilder sb = new StringBuilder();

      for(SeasonEntry s : SEASONS) {
         if (sb.length() > 0) {
            sb.append("|");
         }

         sb.append(s.key());
      }

      return sb.toString();
   }

   private static String buildJutsuNameList() {
      StringBuilder sb = new StringBuilder();

      for(SeasonEntry s : SEASONS) {
         if (sb.length() > 0) {
            sb.append(" + ");
         }

         sb.append(s.jutsuName);
      }

      return sb.toString();
   }

   private void sendHelp(ICommandSender sender) {
      sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "" + TextFormatting.BOLD + "=== Season Jutsu Commands ==="));
      sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/seasonjutsu give <player> [all|" + buildSeasonList() + "]" + TextFormatting.GRAY + " - Give seasonal release"));
      sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/seasonjutsu unlock <player> [all|" + buildSeasonList() + "]" + TextFormatting.GRAY + " - Unlock seasons on existing item"));
      sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/seasonjutsu bpcheck <player>" + TextFormatting.GRAY + " - Check pending battle pass rewards"));
      sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/seasonjutsu fixseasons" + TextFormatting.GRAY + " - Fix S2/S3 jutsu for ALL players (offline too)"));
      sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Available seasons:"));

      for(SeasonEntry s : SEASONS) {
         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "  " + s.key() + " = " + s.jutsuName));
      }

   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"give", "unlock", "bpcheck", "fixseasons"});
      } else if (args.length == 2) {
         return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
      } else {
         return args.length != 3 || !args[0].equalsIgnoreCase("give") && !args[0].equalsIgnoreCase("unlock") ? Collections.emptyList() : getListOfStringsMatchingLastWord(args, SEASON_KEYS);
      }
   }

   static {
      List<String> keys = new ArrayList();
      keys.add("all");

      for(SeasonEntry s : SEASONS) {
         BY_KEY.put(s.key(), s);
         keys.add(s.key());
      }

      SEASON_KEYS = (String[])keys.toArray(new String[0]);
   }

   private static class SeasonEntry {
      final int seasonNumber;
      final int jutsuIndex;
      final String jutsuName;

      SeasonEntry(int seasonNumber, int jutsuIndex, String jutsuName) {
         this.seasonNumber = seasonNumber;
         this.jutsuIndex = jutsuIndex;
         this.jutsuName = jutsuName;
      }

      String key() {
         return "s" + this.seasonNumber;
      }

      String nbtKey() {
         return "JutsuCDMapKey" + this.jutsuIndex;
      }
   }
}
