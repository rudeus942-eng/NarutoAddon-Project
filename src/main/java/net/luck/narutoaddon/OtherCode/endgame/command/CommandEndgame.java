
package net.luck.narutoaddon.OtherCode.endgame.command;

import net.luck.narutoaddon.OtherCode.endgame.defense.DefenseManager;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostDefinition;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostManager;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandEndgame extends CommandBase {
   private static final String[] LEADERBOARD_CATEGORIES = new String[]{"outposts", "bingo", "incursions", "defense"};

   public String getName() {
      return "endgame";
   }

   public String getUsage(ICommandSender sender) {
      return "/endgame <status|outpost|bingo|defense|leaderboard|help>";
   }

   public int getRequiredPermissionLevel() {
      return 0;
   }

   public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
      return sender instanceof EntityPlayerMP;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         throw new CommandException("Players only", new Object[0]);
      } else {
         EntityPlayerMP player = (EntityPlayerMP)sender;
         if (args.length == 0) {
            this.sendHelp(player);
         } else {
            switch (args[0].toLowerCase()) {
               case "status":
                  this.handleStatus(player);
                  break;
               case "outpost":
                  this.handleOutpost(player, args);
                  break;
               case "bingo":
                  this.handleBingo(player, args);
                  break;
               case "defense":
                  this.handleDefense(player, args);
                  break;
               case "leaderboard":
               case "lb":
                  this.handleLeaderboard(player, args);
                  break;
               case "help":
               default:
                  this.sendHelp(player);
            }

         }
      }
   }

   private void handleStatus(EntityPlayerMP player) {
      player.sendMessage(new TextComponentString("§6=== Endgame Status ==="));
      player.sendMessage(new TextComponentString("§7Raid Cooldown: §aNone"));
      int totalOutposts = 0;
      int available = 0;

      for(OutpostDefinition def : OutpostRegistry.getAll()) {
         ++totalOutposts;
         ++available;
      }

      player.sendMessage(new TextComponentString("§7Outposts: §a" + available + " available §7| §c" + (totalOutposts - available) + " on cooldown"));
      player.sendMessage(new TextComponentString("§7Bingo Board: §eUse /endgame bingo to view"));
      player.sendMessage(new TextComponentString("§7Active Events: §7None"));
   }

   private void handleOutpost(EntityPlayerMP player, String[] args) {
      if (args.length < 2) {
         player.sendMessage(new TextComponentString("§cUsage: /endgame outpost <list|accept|leave>"));
      } else {
         switch (args[1].toLowerCase()) {
            case "list":
               this.handleOutpostList(player);
               break;
            case "accept":
               this.handleOutpostAccept(player);
               break;
            case "leave":
               this.handleOutpostLeave(player);
               break;
            default:
               player.sendMessage(new TextComponentString("§cUsage: /endgame outpost <list|accept|leave>"));
         }

      }
   }

   private void handleOutpostList(EntityPlayerMP player) {
      player.sendMessage(new TextComponentString("§6=== Outpost Locations ==="));

      for(OutpostDefinition def : OutpostRegistry.getAll()) {
         BlockPos loc = def.getLocation();
         player.sendMessage(new TextComponentString("  §7• §f" + def.getLocationName() + " §8(X=" + loc.getX() + " Z=" + loc.getZ() + ")"));
      }

      player.sendMessage(new TextComponentString("§6=== Encounter Groups ==="));

      for(OutpostRegistry.EncounterGroup group : OutpostRegistry.getAllEncounterGroups()) {
         player.sendMessage(new TextComponentString("  §7• §f" + group.getDisplayName() + " §8(" + group.getBossCount() + " boss" + (group.getBossCount() == 1 ? "" : "es") + ")"));
      }

      player.sendMessage(new TextComponentString("§7Use §e/endgame outpost accept§7 to start a random outpost mission."));
      player.sendMessage(new TextComponentString("§7Location, difficulty, and enemies are randomly assigned."));
   }

   private void handleOutpostAccept(EntityPlayerMP player) {
      String error = OutpostManager.getInstance().acceptOutpostMission(player);
      if (error != null) {
         player.sendMessage(new TextComponentString("§c" + error));
      }

   }

   private void handleOutpostLeave(EntityPlayerMP player) {
      OutpostManager.getInstance().leaveOutpost(player);
   }

   private void handleBingo(EntityPlayerMP player, String[] args) {
      if (args.length < 2) {
         this.handleBingoShow(player);
      } else {
         switch (args[1].toLowerCase()) {
            case "hunt":
               this.handleBingoHunt(player, args);
               break;
            case "abandon":
               this.handleBingoAbandon(player);
               break;
            default:
               this.handleBingoShow(player);
         }

      }
   }

   private void handleBingoShow(EntityPlayerMP player) {
      player.sendMessage(new TextComponentString("§6=== Bingo Book ==="));
      player.sendMessage(new TextComponentString("§7No active bingo board. Check back after system initialization."));
   }

   private void handleBingoHunt(EntityPlayerMP player, String[] args) {
      if (args.length < 3) {
         player.sendMessage(new TextComponentString("§cUsage: /endgame bingo hunt <1-5>"));
      } else {
         int slot;
         try {
            slot = Integer.parseInt(args[2]);
         } catch (NumberFormatException var5) {
            player.sendMessage(new TextComponentString("§cInvalid slot number: " + args[2]));
            return;
         }

         if (slot >= 1 && slot <= 5) {
            player.sendMessage(new TextComponentString("§aStarting bingo hunt for slot §e" + slot + "§a..."));
         } else {
            player.sendMessage(new TextComponentString("§cSlot must be between 1 and 5."));
         }
      }
   }

   private void handleBingoAbandon(EntityPlayerMP player) {
      player.sendMessage(new TextComponentString("§eAbandoning current bingo hunt..."));
   }

   private void handleDefense(EntityPlayerMP player, String[] args) {
      if (args.length >= 2 && !"join".equalsIgnoreCase(args[1])) {
         player.sendMessage(new TextComponentString("§cUsage: /endgame defense join"));
      } else {
         DefenseManager.getInstance().tryJoin(player);
      }

   }

   private void handleLeaderboard(EntityPlayerMP player, String[] args) {
      if (args.length < 2) {
         player.sendMessage(new TextComponentString("§cUsage: /endgame leaderboard <outposts|bingo|incursions|defense>"));
      } else {
         String category = args[1].toLowerCase();
         boolean valid = false;

         for(String cat : LEADERBOARD_CATEGORIES) {
            if (cat.equals(category)) {
               valid = true;
               break;
            }
         }

         if (!valid) {
            player.sendMessage(new TextComponentString("§cInvalid category. Use: outposts, bingo, incursions, defense"));
         } else {
            player.sendMessage(new TextComponentString("§6=== Leaderboard: " + capitalize(category) + " ==="));
            player.sendMessage(new TextComponentString("§7No leaderboard data available yet."));
         }
      }
   }

   private static String capitalize(String s) {
      return s != null && !s.isEmpty() ? Character.toUpperCase(s.charAt(0)) + s.substring(1) : s;
   }

   private void sendHelp(EntityPlayerMP player) {
      player.sendMessage(new TextComponentString("§6=== Endgame Commands ==="));
      player.sendMessage(new TextComponentString("§e/endgame status §7- Show cooldowns, active events, bingo summary"));
      player.sendMessage(new TextComponentString("§e/endgame outpost list §7- List all outposts"));
      player.sendMessage(new TextComponentString("§e/endgame outpost accept §7- Accept a random outpost mission"));
      player.sendMessage(new TextComponentString("§e/endgame outpost leave §7- Abandon current outpost mission"));
      player.sendMessage(new TextComponentString("§e/endgame bingo §7- Show current bingo board"));
      player.sendMessage(new TextComponentString("§e/endgame bingo hunt <1-5> §7- Accept hunt for a bingo slot"));
      player.sendMessage(new TextComponentString("§e/endgame bingo abandon §7- Abandon current hunt"));
      player.sendMessage(new TextComponentString("§e/endgame defense join §7- Join active village defense"));
      player.sendMessage(new TextComponentString("§e/endgame leaderboard <category> §7- Show top 10"));
      player.sendMessage(new TextComponentString("§7Categories: outposts, bingo, incursions, defense"));
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"status", "outpost", "bingo", "defense", "leaderboard", "lb", "help"});
      } else {
         String sub = args[0].toLowerCase();
         if (args.length == 2) {
            if ("outpost".equals(sub)) {
               return getListOfStringsMatchingLastWord(args, new String[]{"list", "accept", "leave"});
            }

            if ("bingo".equals(sub)) {
               return getListOfStringsMatchingLastWord(args, new String[]{"hunt", "abandon"});
            }

            if ("defense".equals(sub)) {
               return getListOfStringsMatchingLastWord(args, new String[]{"join"});
            }

            if ("leaderboard".equals(sub) || "lb".equals(sub)) {
               return getListOfStringsMatchingLastWord(args, LEADERBOARD_CATEGORIES);
            }
         }

         if (args.length == 3 && "bingo".equals(sub) && "hunt".equalsIgnoreCase(args[1])) {
            return getListOfStringsMatchingLastWord(args, new String[]{"1", "2", "3", "4", "5"});
         } else {
            return Collections.emptyList();
         }
      }
   }
}
