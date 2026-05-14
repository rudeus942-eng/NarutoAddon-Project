package net.luck.narutoaddon.OtherCode.command;

import net.luck.narutoaddon.OtherCode.Rankedrewardconfig;
import net.luck.narutoaddon.OtherCode.Rankedseason;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandRankedReward extends CommandBase {
   private static final String[] CATEGORIES = new String[]{"rankup", "streak", "position", "season", "debug", "forcesave", "clearold", "clearlegacy"};
   private static final String[] ACTIONS = new String[]{"add", "list", "remove", "clear"};
   private static final int[] VALID_STREAKS = new int[]{3, 5, 7, 10, 15};

   public String getName() {
      return "rankedreward";
   }

   public String getUsage(ICommandSender sender) {
      return "/rankedreward <rankup|streak|position> <key> <add|list|remove|clear> [args]";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public List<String> getAliases() {
      return Arrays.asList("rreward", "rankedr");
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 1) {
         throw new WrongUsageException(this.getUsage(sender), new Object[0]);
      } else {
         String category = args[0].toLowerCase();
         if (category.equals("debug")) {
            this.handleDebug(sender, server);
         } else if (category.equals("forcesave")) {
            this.handleForceSave(sender, server);
         } else if (category.equals("clearold")) {
            this.handleClearOld(sender, server);
         } else if (category.equals("clearlegacy")) {
            this.handleClearLegacy(sender, server);
         } else if (!this.isValidCategory(category)) {
            throw new CommandException("Invalid category. Use: rankup, streak, position, season, debug, forcesave, clearold, or clearlegacy", new Object[0]);
         } else {
            Rankedrewardconfig config = Rankedrewardconfig.get(server.getWorld(0));
            if (config == null) {
               throw new CommandException("Could not access reward configuration", new Object[0]);
            } else if (category.equals("season")) {
               this.handleSeasonCommand(sender, config, args);
            } else if (category.equals("position")) {
               this.handlePositionCommand(sender, config, args);
            } else if (args.length < 2) {
               throw new WrongUsageException(this.getUsage(sender), new Object[0]);
            } else {
               String key = args[1];
               if (category.equals("rankup")) {
                  key = Rankedrewardconfig.normalizeTierName(key);
                  if (!Rankedrewardconfig.isValidTier(key)) {
                     throw new CommandException("Invalid tier name. Valid tiers: Genin, Chunin, Jonin, S. Jonin, Elite Jonin, ANBU, Kage, Otsutsuki", new Object[0]);
                  }
               } else if (category.equals("streak")) {
                  try {
                     int streak = Integer.parseInt(key);
                     if (!this.isValidStreak(streak)) {
                        throw new CommandException("Invalid streak number. Valid streaks: 3, 5, 7, 10, 15", new Object[0]);
                     }
                  } catch (NumberFormatException var10) {
                     throw new CommandException("Streak must be a number (3, 5, 7, 10, or 15)", new Object[0]);
                  }
               }

               if (args.length < 3) {
                  throw new WrongUsageException(this.getUsage(sender), new Object[0]);
               } else {
                  switch (args[2].toLowerCase()) {
                     case "add":
                        this.handleAdd(sender, config, category, key, args);
                        break;
                     case "list":
                        this.handleList(sender, config, category, key);
                        break;
                     case "remove":
                        this.handleRemove(sender, config, category, key, args);
                        break;
                     case "clear":
                        this.handleClear(sender, config, category, key);
                        break;
                     default:
                        throw new CommandException("Invalid action. Use: add, list, remove, or clear", new Object[0]);
                  }

               }
            }
         }
      }
   }

   private void handleSeasonCommand(ICommandSender sender, Rankedrewardconfig config, String[] args) throws CommandException {
      if (args.length < 3) {
         throw new WrongUsageException("/rankedreward season <tier> <add|list|remove|clear> [args]", new Object[0]);
      } else {
         String tier = Rankedrewardconfig.normalizeTierName(args[1]);
         if (!Rankedrewardconfig.isValidTier(tier)) {
            throw new CommandException("Invalid tier name. Valid tiers: Genin, Chunin, Jonin, S. Jonin, Elite Jonin, ANBU, Kage, Otsutsuki", new Object[0]);
         } else {
            switch (args[2].toLowerCase()) {
               case "add":
                  this.handleSeasonAdd(sender, config, tier, args);
                  break;
               case "list":
                  this.handleSeasonList(sender, config, tier);
                  break;
               case "remove":
                  this.handleSeasonRemove(sender, config, tier, args);
                  break;
               case "clear":
                  this.handleSeasonClear(sender, config, tier);
                  break;
               default:
                  throw new CommandException("Invalid action. Use: add, list, remove, or clear", new Object[0]);
            }

         }
      }
   }

   private void handleSeasonAdd(ICommandSender sender, Rankedrewardconfig config, String tier, String[] args) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         throw new CommandException("This command must be run by a player", new Object[0]);
      } else {
         EntityPlayerMP player = (EntityPlayerMP)sender;
         ItemStack held = player.getHeldItemMainhand();
         if (held.isEmpty()) {
            throw new CommandException("You must be holding an item to add as a reward", new Object[0]);
         } else {
            Rankedrewardconfig.ConfiguredReward reward = new Rankedrewardconfig.ConfiguredReward(held);
            config.addSeasonReward(tier, reward);
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Added " + TextFormatting.GOLD + held.getDisplayName() + " x" + held.getCount() + TextFormatting.GREEN + " to Season " + TextFormatting.YELLOW + tier + TextFormatting.GREEN + " rewards (applies to all seasons)"));
         }
      }
   }

   private void handleSeasonList(ICommandSender sender, Rankedrewardconfig config, String tier) {
      List<Rankedrewardconfig.ConfiguredReward> rewards = config.getSeasonRewards(tier);
      sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== Season " + tier + " Rewards (All Seasons) ==="));
      if (rewards.isEmpty()) {
         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "No rewards configured"));
      } else {
         for(int i = 0; i < rewards.size(); ++i) {
            Rankedrewardconfig.ConfiguredReward reward = (Rankedrewardconfig.ConfiguredReward)rewards.get(i);
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "[" + i + "] " + TextFormatting.WHITE + reward.getDisplayName()));
         }

         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Total: " + rewards.size() + " reward(s) - All items will be given"));
      }
   }

   private void handleSeasonRemove(ICommandSender sender, Rankedrewardconfig config, String tier, String[] args) throws CommandException {
      if (args.length < 4) {
         throw new WrongUsageException("/rankedreward season " + tier + " remove <index>", new Object[0]);
      } else {
         int index;
         try {
            index = Integer.parseInt(args[3]);
         } catch (NumberFormatException var8) {
            throw new CommandException("Index must be a number", new Object[0]);
         }

         List<Rankedrewardconfig.ConfiguredReward> rewards = config.getSeasonRewards(tier);
         if (index >= 0 && index < rewards.size()) {
            String removedName = ((Rankedrewardconfig.ConfiguredReward)rewards.get(index)).getDisplayName();
            if (config.removeSeasonReward(tier, index)) {
               sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Removed " + TextFormatting.GOLD + removedName + TextFormatting.GREEN + " from Season " + tier + " rewards"));
            } else {
               throw new CommandException("Failed to remove reward", new Object[0]);
            }
         } else {
            throw new CommandException("Invalid index. Use /rankedreward season " + tier + " list to see valid indices", new Object[0]);
         }
      }
   }

   private void handleSeasonClear(ICommandSender sender, Rankedrewardconfig config, String tier) {
      config.clearSeasonRewards(tier);
      sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Cleared all Season " + tier + " rewards"));
   }

   private void handlePositionCommand(ICommandSender sender, Rankedrewardconfig config, String[] args) throws CommandException {
      if (args.length < 4) {
         throw new WrongUsageException("/rankedreward position <seasonNumber> <position> <add|list|remove|clear> [args]", new Object[0]);
      } else {
         int seasonNumber;
         try {
            seasonNumber = Integer.parseInt(args[1]);
            if (seasonNumber < 0) {
               throw new CommandException("Season number must be 0 or higher", new Object[0]);
            }
         } catch (NumberFormatException var9) {
            throw new CommandException("Season number must be a valid number (0, 1, 2, etc.)", new Object[0]);
         }

         int position;
         try {
            position = Integer.parseInt(args[2]);
            if (position < 1 || position > 10) {
               throw new CommandException("Position must be between 1 and 10", new Object[0]);
            }
         } catch (NumberFormatException var10) {
            throw new CommandException("Position must be a number (1-10)", new Object[0]);
         }

         switch (args[3].toLowerCase()) {
            case "add":
               this.handlePositionAdd(sender, config, seasonNumber, position, args);
               break;
            case "list":
               this.handlePositionList(sender, config, seasonNumber, position);
               break;
            case "remove":
               this.handlePositionRemove(sender, config, seasonNumber, position, args);
               break;
            case "clear":
               this.handlePositionClear(sender, config, seasonNumber, position);
               break;
            default:
               throw new CommandException("Invalid action. Use: add, list, remove, or clear", new Object[0]);
         }

      }
   }

   private void handlePositionAdd(ICommandSender sender, Rankedrewardconfig config, int seasonNumber, int position, String[] args) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         throw new CommandException("This command must be run by a player", new Object[0]);
      } else {
         EntityPlayerMP player = (EntityPlayerMP)sender;
         ItemStack held = player.getHeldItemMainhand();
         if (held.isEmpty()) {
            throw new CommandException("You must be holding an item to add as a reward", new Object[0]);
         } else {
            Rankedrewardconfig.ConfiguredReward reward = new Rankedrewardconfig.ConfiguredReward(held);
            config.addPositionReward(seasonNumber, position, reward);
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Added " + TextFormatting.GOLD + held.getDisplayName() + " x" + held.getCount() + TextFormatting.GREEN + " to " + TextFormatting.AQUA + "Season " + seasonNumber + TextFormatting.GREEN + " Position " + TextFormatting.YELLOW + "#" + position + TextFormatting.GREEN + " rewards"));
         }
      }
   }

   private void handlePositionList(ICommandSender sender, Rankedrewardconfig config, int seasonNumber, int position) {
      List<Rankedrewardconfig.ConfiguredReward> rewards = config.getPositionRewards(seasonNumber, position);
      sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== Season " + seasonNumber + " Position #" + position + " Rewards ==="));
      if (rewards.isEmpty()) {
         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "No rewards configured"));
      } else {
         for(int i = 0; i < rewards.size(); ++i) {
            Rankedrewardconfig.ConfiguredReward reward = (Rankedrewardconfig.ConfiguredReward)rewards.get(i);
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "[" + i + "] " + TextFormatting.WHITE + reward.getDisplayName()));
         }

         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Total: " + rewards.size() + " reward(s) - All items will be given"));
      }
   }

   private void handlePositionRemove(ICommandSender sender, Rankedrewardconfig config, int seasonNumber, int position, String[] args) throws CommandException {
      if (args.length < 5) {
         throw new WrongUsageException("/rankedreward position " + seasonNumber + " " + position + " remove <index>", new Object[0]);
      } else {
         int index;
         try {
            index = Integer.parseInt(args[4]);
         } catch (NumberFormatException var9) {
            throw new CommandException("Index must be a number", new Object[0]);
         }

         List<Rankedrewardconfig.ConfiguredReward> rewards = config.getPositionRewards(seasonNumber, position);
         if (index >= 0 && index < rewards.size()) {
            String removedName = ((Rankedrewardconfig.ConfiguredReward)rewards.get(index)).getDisplayName();
            if (config.removePositionReward(seasonNumber, position, index)) {
               sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Removed " + TextFormatting.GOLD + removedName + TextFormatting.GREEN + " from Season " + seasonNumber + " Position #" + position + " rewards"));
            } else {
               throw new CommandException("Failed to remove reward", new Object[0]);
            }
         } else {
            throw new CommandException("Invalid index. Use /rankedreward position " + seasonNumber + " " + position + " list to see valid indices", new Object[0]);
         }
      }
   }

   private void handlePositionClear(ICommandSender sender, Rankedrewardconfig config, int seasonNumber, int position) {
      config.clearPositionRewards(seasonNumber, position);
      sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Cleared all Season " + seasonNumber + " Position #" + position + " rewards"));
   }

   private void handleAdd(ICommandSender sender, Rankedrewardconfig config, String category, String key, String[] args) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         throw new CommandException("This command must be run by a player", new Object[0]);
      } else {
         EntityPlayerMP player = (EntityPlayerMP)sender;
         ItemStack held = player.getHeldItemMainhand();
         if (held.isEmpty()) {
            throw new CommandException("You must be holding an item to add as a reward", new Object[0]);
         } else {
            Rankedrewardconfig.ConfiguredReward reward = new Rankedrewardconfig.ConfiguredReward(held);
            config.addReward(category, key, reward);
            String categoryDisplay = this.getCategoryDisplay(category);
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Added " + TextFormatting.GOLD + held.getDisplayName() + " x" + held.getCount() + TextFormatting.GREEN + " to " + categoryDisplay + " " + TextFormatting.YELLOW + key + TextFormatting.GREEN + " rewards"));
         }
      }
   }

   private void handleList(ICommandSender sender, Rankedrewardconfig config, String category, String key) {
      List<Rankedrewardconfig.ConfiguredReward> rewards = config.getRewards(category, key);
      String categoryDisplay = this.getCategoryDisplay(category);
      sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== " + categoryDisplay + " " + key + " Rewards ==="));
      if (rewards.isEmpty()) {
         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "No rewards configured"));
      } else {
         for(int i = 0; i < rewards.size(); ++i) {
            Rankedrewardconfig.ConfiguredReward reward = (Rankedrewardconfig.ConfiguredReward)rewards.get(i);
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "[" + i + "] " + TextFormatting.WHITE + reward.getDisplayName()));
         }

         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Total: " + rewards.size() + " reward(s) - All items will be given"));
      }
   }

   private void handleRemove(ICommandSender sender, Rankedrewardconfig config, String category, String key, String[] args) throws CommandException {
      if (args.length < 4) {
         throw new WrongUsageException("/rankedreward " + category + " " + key + " remove <index>", new Object[0]);
      } else {
         int index;
         try {
            index = Integer.parseInt(args[3]);
         } catch (NumberFormatException var9) {
            throw new CommandException("Index must be a number", new Object[0]);
         }

         List<Rankedrewardconfig.ConfiguredReward> rewards = config.getRewards(category, key);
         if (index >= 0 && index < rewards.size()) {
            String removedName = ((Rankedrewardconfig.ConfiguredReward)rewards.get(index)).getDisplayName();
            if (config.removeReward(category, key, index)) {
               sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Removed " + TextFormatting.GOLD + removedName + TextFormatting.GREEN + " from " + this.getCategoryDisplay(category) + " " + key + " rewards"));
            } else {
               throw new CommandException("Failed to remove reward", new Object[0]);
            }
         } else {
            throw new CommandException("Invalid index. Use /rankedreward " + category + " " + key + " list to see valid indices", new Object[0]);
         }
      }
   }

   private void handleClear(ICommandSender sender, Rankedrewardconfig config, String category, String key) {
      config.clearRewards(category, key);
      sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Cleared all " + this.getCategoryDisplay(category) + " " + key + " rewards"));
   }

   private void handleDebug(ICommandSender sender, MinecraftServer server) throws CommandException {
      Rankedrewardconfig config = Rankedrewardconfig.get(server.getWorld(0));
      if (config == null) {
         throw new CommandException("Could not access reward configuration", new Object[0]);
      } else {
         sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== Ranked Rewards Debug Info ==="));
         sender.sendMessage(new TextComponentString(TextFormatting.WHITE + "Total configured rewards: " + TextFormatting.GREEN + config.getTotalRewardCount()));
         String[] categories = new String[]{"rankup", "streak", "position", "season"};

         for(String cat : categories) {
            List<String> keys = config.getKeysForCategory(cat);
            if (!keys.isEmpty()) {
               sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + this.getCategoryDisplay(cat) + ":"));

               for(String key : keys) {
                  int count = config.getRewards(cat, key).size();
                  sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "  " + key + ": " + TextFormatting.WHITE + count + " reward(s)"));
               }
            }
         }

         config.debugPrintAllRewards();
         sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Debug info printed to console as well."));
      }
   }

   private void handleForceSave(ICommandSender sender, MinecraftServer server) throws CommandException {
      Rankedrewardconfig config = Rankedrewardconfig.get(server.getWorld(0));
      if (config == null) {
         throw new CommandException("Could not access reward configuration", new Object[0]);
      } else {
         config.forceSave();
         sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Force saved " + config.getTotalRewardCount() + " rewards to disk and backup files."));
         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Backup files saved to world folder."));
      }
   }

   private void handleClearOld(ICommandSender sender, MinecraftServer server) throws CommandException {
      Rankedrewardconfig config = Rankedrewardconfig.get(server.getWorld(0));
      if (config == null) {
         throw new CommandException("Could not access reward configuration", new Object[0]);
      } else {
         int cleared = config.clearOldSeasonFormat();
         if (cleared > 0) {
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Cleared " + cleared + " old per-season format reward entries."));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "These were rewards stored under old keys like '0:Otsutsuki' instead of 'Otsutsuki'."));
         } else {
            sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "No old format rewards found to clear."));
         }

      }
   }

   private void handleClearLegacy(ICommandSender sender, MinecraftServer server) throws CommandException {
      int countBefore = Rankedseason.countPendingWithLegacyItems(server.getWorld(0));
      if (countBefore == 0) {
         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "No pending rewards have legacy items to clear."));
         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "All rewards will use the dynamic config system."));
      } else {
         sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Found " + countBefore + " pending rewards with legacy items stored."));
         sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "These are old items from before the dynamic reward config system."));
         int cleared = Rankedseason.clearLegacyItems(server.getWorld(0));
         if (cleared > 0) {
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Successfully cleared legacy items from " + cleared + " pending rewards."));
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Players will now only receive rewards from the current config."));
         }

      }
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, CATEGORIES);
      } else {
         String category = args[0].toLowerCase();
         if (args.length == 2) {
            if (category.equals("rankup") || category.equals("season")) {
               return getListOfStringsMatchingLastWord(args, Rankedrewardconfig.VALID_TIERS);
            }

            if (category.equals("streak")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"3", "5", "7", "10", "15"});
            }

            if (category.equals("position")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"0", "1", "2", "3", "4", "5"});
            }
         }

         if (args.length == 3) {
            if (category.equals("position")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
            } else {
               return getListOfStringsMatchingLastWord(args, ACTIONS);
            }
         } else if (args.length == 4 && category.equals("position")) {
            return getListOfStringsMatchingLastWord(args, ACTIONS);
         } else {
            return Collections.emptyList();
         }
      }
   }

   private boolean isValidCategory(String category) {
      for(String c : CATEGORIES) {
         if (c.equals(category)) {
            return true;
         }
      }

      return false;
   }

   private boolean isValidStreak(int streak) {
      for(int s : VALID_STREAKS) {
         if (s == streak) {
            return true;
         }
      }

      return false;
   }

   private String getCategoryDisplay(String category) {
      switch (category) {
         case "rankup":
            return "Rank-Up";
         case "streak":
            return "Streak";
         case "position":
            return "Position";
         case "season":
            return "Season";
         default:
            return category;
      }
   }
}
