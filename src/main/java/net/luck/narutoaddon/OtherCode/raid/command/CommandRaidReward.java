
package net.luck.narutoaddon.OtherCode.raid.command;

import net.luck.narutoaddon.OtherCode.raid.boss.BossRegistry;
import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.rewards.RaidRewardConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandRaidReward extends CommandBase {
   public String getName() {
      return "raidreward";
   }

   public String getUsage(ICommandSender sender) {
      return "/raidreward <boss|firstclear|debug|forcesave>";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length == 0) {
         this.sendHelp(sender);
      } else {
         switch (args[0].toLowerCase()) {
            case "boss":
               this.handleBossReward(sender, args);
               break;
            case "firstclear":
               this.handleFirstClearReward(sender, args);
               break;
            case "debug":
               this.handleDebug(sender);
               break;
            case "forcesave":
               this.handleForceSave(sender);
               break;
            default:
               this.sendHelp(sender);
         }

      }
   }

   private void handleBossReward(ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 4) {
         sender.sendMessage(new TextComponentString("§cUsage: /raidreward boss <bossId> <difficulty> <add|list|remove|clear>"));
      } else {
         String bossId = args[1].toLowerCase();
         if (!BossRegistry.bossExists(bossId)) {
            sender.sendMessage(new TextComponentString("§cUnknown boss: " + bossId));
         } else {
            RaidDifficulty difficulty = RaidDifficulty.fromString(args[2]);
            if (difficulty == null) {
               sender.sendMessage(new TextComponentString("§cInvalid difficulty. Use: genin, chunin, or jonin"));
            } else {
               String action = args[3].toLowerCase();
               String category = "boss:" + bossId + ":" + difficulty.name().toLowerCase();
               this.handleRewardAction(sender, args, category, action, "Boss " + bossId + " (" + difficulty.getDisplayName() + ")");
            }
         }
      }
   }

   private void handleFirstClearReward(ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 4) {
         sender.sendMessage(new TextComponentString("§cUsage: /raidreward firstclear <bossId> <difficulty> <add|list|remove|clear>"));
      } else {
         String bossId = args[1].toLowerCase();
         if (!BossRegistry.bossExists(bossId)) {
            sender.sendMessage(new TextComponentString("§cUnknown boss: " + bossId));
         } else {
            RaidDifficulty difficulty = RaidDifficulty.fromString(args[2]);
            if (difficulty == null) {
               sender.sendMessage(new TextComponentString("§cInvalid difficulty. Use: genin, chunin, or jonin"));
            } else {
               String action = args[3].toLowerCase();
               String category = "firstclear:" + bossId + ":" + difficulty.name().toLowerCase();
               this.handleRewardAction(sender, args, category, action, "First Clear " + bossId + " (" + difficulty.getDisplayName() + ")");
            }
         }
      }
   }

   private void handleRewardAction(ICommandSender sender, String[] args, String category, String action, String displayName) throws CommandException {
      RaidRewardConfig config = RaidRewardConfig.get(sender.getEntityWorld());
      if (config == null) {
         sender.sendMessage(new TextComponentString("§cError: Could not access reward config."));
      } else {
         switch (action) {
            case "add":
               this.addReward(sender, config, category, displayName);
               break;
            case "list":
               this.listRewards(sender, config, category, displayName);
               break;
            case "remove":
               if (args.length < 5) {
                  sender.sendMessage(new TextComponentString("§cUsage: ... remove <index>"));
                  return;
               }

               int index = parseInt(args[4]);
               this.removeReward(sender, config, category, displayName, index);
               break;
            case "clear":
               this.clearRewards(sender, config, category, displayName);
               break;
            default:
               sender.sendMessage(new TextComponentString("§cUnknown action: " + action + ". Use: add, list, remove, or clear"));
         }

      }
   }

   private void addReward(ICommandSender sender, RaidRewardConfig config, String category, String displayName) {
      if (!(sender instanceof EntityPlayerMP)) {
         sender.sendMessage(new TextComponentString("§cThis command must be run by a player (need held item)."));
      } else {
         EntityPlayerMP player = (EntityPlayerMP)sender;
         ItemStack heldItem = player.getHeldItemMainhand();
         if (heldItem.isEmpty()) {
            sender.sendMessage(new TextComponentString("§cYou must hold an item to add as a reward."));
         } else {
            RaidRewardConfig.ConfiguredReward reward = new RaidRewardConfig.ConfiguredReward(heldItem);
            config.addReward(category, reward);
            sender.sendMessage(new TextComponentString("§aAdded reward to " + displayName + ": " + reward.getDisplayName()));
         }
      }
   }

   private void listRewards(ICommandSender sender, RaidRewardConfig config, String category, String displayName) {
      List<RaidRewardConfig.ConfiguredReward> rewards = config.getRewards(category);
      sender.sendMessage(new TextComponentString("§e§l=== " + displayName + " REWARDS ==="));
      if (rewards.isEmpty()) {
         sender.sendMessage(new TextComponentString("§7No rewards configured."));
      } else {
         for(int i = 0; i < rewards.size(); ++i) {
            RaidRewardConfig.ConfiguredReward reward = (RaidRewardConfig.ConfiguredReward)rewards.get(i);
            sender.sendMessage(new TextComponentString("§6" + (i + 1) + ". §f" + reward.getDisplayName()));
         }

      }
   }

   private void removeReward(ICommandSender sender, RaidRewardConfig config, String category, String displayName, int index) {
      List<RaidRewardConfig.ConfiguredReward> rewards = config.getRewards(category);
      if (index >= 1 && index <= rewards.size()) {
         RaidRewardConfig.ConfiguredReward removed = (RaidRewardConfig.ConfiguredReward)rewards.get(index - 1);
         config.removeReward(category, index - 1);
         sender.sendMessage(new TextComponentString("§aRemoved reward from " + displayName + ": " + removed.getDisplayName()));
      } else {
         sender.sendMessage(new TextComponentString("§cInvalid index. Must be 1-" + rewards.size()));
      }
   }

   private void clearRewards(ICommandSender sender, RaidRewardConfig config, String category, String displayName) {
      config.clearRewards(category);
      sender.sendMessage(new TextComponentString("§aCleared all rewards for " + displayName));
   }

   private void handleDebug(ICommandSender sender) {
      RaidRewardConfig config = RaidRewardConfig.get(sender.getEntityWorld());
      if (config == null) {
         sender.sendMessage(new TextComponentString("§cError: Could not access reward config."));
      } else {
         sender.sendMessage(new TextComponentString("§e§l=== RAID REWARD DEBUG ==="));
         int totalRewards = 0;

         for(String bossId : BossRegistry.getAllBossIds()) {
            for(RaidDifficulty diff : RaidDifficulty.values()) {
               String category = "boss:" + bossId + ":" + diff.name().toLowerCase();
               List<RaidRewardConfig.ConfiguredReward> rewards = config.getRewards(category);
               if (!rewards.isEmpty()) {
                  sender.sendMessage(new TextComponentString("§7" + bossId + " " + diff.getDisplayName() + ": §f" + rewards.size() + " rewards"));
                  totalRewards += rewards.size();
               }

               String fcCategory = "firstclear:" + bossId + ":" + diff.name().toLowerCase();
               List<RaidRewardConfig.ConfiguredReward> fcRewards = config.getRewards(fcCategory);
               if (!fcRewards.isEmpty()) {
                  sender.sendMessage(new TextComponentString("§7" + bossId + " " + diff.getDisplayName() + " (First Clear): §f" + fcRewards.size() + " rewards"));
                  totalRewards += fcRewards.size();
               }
            }
         }

         sender.sendMessage(new TextComponentString("§7Total configured rewards: §f" + totalRewards));
      }
   }

   private void handleForceSave(ICommandSender sender) {
      RaidRewardConfig config = RaidRewardConfig.get(sender.getEntityWorld());
      if (config == null) {
         sender.sendMessage(new TextComponentString("§cError: Could not access reward config."));
      } else {
         config.forceSave();
         sender.sendMessage(new TextComponentString("§aForce saved raid reward configuration."));
      }
   }

   private void sendHelp(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§e§l=== RAID REWARD COMMANDS ==="));
      sender.sendMessage(new TextComponentString("§6/raidreward boss <bossId> <difficulty> <add|list|remove|clear>"));
      sender.sendMessage(new TextComponentString("§6/raidreward firstclear <bossId> <difficulty> <add|list|remove|clear>"));
      sender.sendMessage(new TextComponentString("§6/raidreward debug §7- Show all configured rewards"));
      sender.sendMessage(new TextComponentString("§6/raidreward forcesave §7- Force save to disk"));
      sender.sendMessage(new TextComponentString(""));
      sender.sendMessage(new TextComponentString("§7To add a reward, hold the item and use the 'add' action."));
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"boss", "firstclear", "debug", "forcesave"});
      } else if (args.length != 2 || !args[0].equalsIgnoreCase("boss") && !args[0].equalsIgnoreCase("firstclear")) {
         if (args.length != 3 || !args[0].equalsIgnoreCase("boss") && !args[0].equalsIgnoreCase("firstclear")) {
            return args.length != 4 || !args[0].equalsIgnoreCase("boss") && !args[0].equalsIgnoreCase("firstclear") ? Collections.emptyList() : getListOfStringsMatchingLastWord(args, new String[]{"add", "list", "remove", "clear"});
         } else {
            return getListOfStringsMatchingLastWord(args, new String[]{"genin", "chunin", "jonin", "anbu"});
         }
      } else {
         return getListOfStringsMatchingLastWord(args, (String[])BossRegistry.getAllBossIds().toArray(new String[0]));
      }
   }
}
