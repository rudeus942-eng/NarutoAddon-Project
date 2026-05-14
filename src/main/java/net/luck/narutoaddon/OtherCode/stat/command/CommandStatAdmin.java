
package net.luck.narutoaddon.OtherCode.stat.command;

import net.luck.narutoaddon.OtherCode.stat.core.*;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandStatAdmin extends CommandBase {
   public String getName() {
      return "statadmin";
   }

   public String getUsage(ICommandSender sender) {
      return "/statadmin <view|grantsp|setsp|reset|granttoken|debug> <player> [amount]";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 1) {
         sender.sendMessage(new TextComponentString(TextFormatting.RED + this.getUsage(sender)));
      } else {
         String action = args[0].toLowerCase();
         if (args.length < 2) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + this.getUsage(sender)));
         } else {
            EntityPlayerMP target = getPlayer(server, sender, args[1]);
            StatManager mgr = StatManager.getInstance();
            switch (action) {
               case "view":
                  this.viewStats(sender, target, mgr);
                  break;
               case "grantsp":
                  if (args.length < 3) {
                     throw new CommandException("Usage: /statadmin grantsp <player> <amount>", new Object[0]);
                  }

                  int amount = parseInt(args[2], 1, 9999);
                  mgr.adminGrantSP(target.getServerWorld(), target.getUniqueID(), amount);
                  mgr.syncToClient(target);
                  sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Granted " + amount + " SP to " + target.getName()));
                  target.sendMessage(new TextComponentString(TextFormatting.GREEN + "An admin granted you " + amount + " Stat Points!"));
                  break;
               case "setsp":
                  if (args.length < 3) {
                     throw new CommandException("Usage: /statadmin setsp <player> <amount>", new Object[0]);
                  }

                  int amount = parseInt(args[2], 0, 99999);
                  mgr.adminSetSP(target.getServerWorld(), target.getUniqueID(), amount);
                  mgr.syncToClient(target);
                  sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Set " + target.getName() + "'s earned SP to " + amount));
                  break;
               case "reset":
                  mgr.adminRespec(target.getServerWorld(), target.getUniqueID());
                  mgr.syncToClient(target);
                  sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Reset all stats for " + target.getName()));
                  target.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Your stats have been reset by an admin."));
                  break;
               case "granttoken":
                  if (args.length < 3) {
                     throw new CommandException("Usage: /statadmin granttoken <player> <amount>", new Object[0]);
                  }

                  int amount = parseInt(args[2], 1, 999);
                  StatSavedData saved = StatSavedData.get((World)target.getServerWorld());
                  PlayerStatData data = saved.getOrCreate(target.getUniqueID());
                  data.grantRespecTokens(amount);
                  saved.markDirty();
                  mgr.syncToClient(target);
                  sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Granted " + amount + " respec token(s) to " + target.getName()));
                  break;
               case "debug":
                  this.debugPlayer(sender, target, mgr);
                  break;
               default:
                  sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown action: " + action));
                  sender.sendMessage(new TextComponentString(TextFormatting.RED + this.getUsage(sender)));
            }

         }
      }
   }

   private void viewStats(ICommandSender sender, EntityPlayerMP target, StatManager mgr) {
      PlayerStatData data = mgr.getPlayerData(target.getServerWorld(), target.getUniqueID());
      sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== Stats: " + target.getName() + " ==="));
      sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "SP: " + data.getAvailableSP() + " available, " + data.getSpEarned() + " earned, " + data.getSpSpent() + " spent"));
      sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "Respec Tokens: " + data.getRespecTokens()));
      sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "Jonin: " + mgr.isJonin(target)));

      for(StatCategory cat : StatCategory.values()) {
         StringBuilder sb = new StringBuilder();
         sb.append(TextFormatting.YELLOW).append(cat.getDisplayName()).append(": ");
         boolean hasAny = false;

         for(StatElement el : StatElement.getElementsForCategory(cat)) {
            int level = data.getLevel(cat, el);
            if (level > 0) {
               if (hasAny) {
                  sb.append(", ");
               }

               sb.append(el.getDisplayName()).append("=").append(level);
               hasAny = true;
            }
         }

         if (!hasAny) {
            sb.append("(none)");
         }

         sender.sendMessage(new TextComponentString(sb.toString()));
      }

   }

   private void debugPlayer(ICommandSender sender, EntityPlayerMP target, StatManager mgr) {
      PlayerStatData data = mgr.getPlayerData(target.getServerWorld(), target.getUniqueID());
      sender.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "=== DEBUG: " + target.getName() + " ==="));
      sender.sendMessage(new TextComponentString("UUID: " + target.getUniqueID()));
      sender.sendMessage(new TextComponentString("SP Earned: " + data.getSpEarned() + " | Spent: " + data.getSpSpent() + " | Available: " + data.getAvailableSP()));
      sender.sendMessage(new TextComponentString("Respec Tokens: " + data.getRespecTokens() + " | Last Respec: " + data.getLastRespecTime()));
      sender.sendMessage(new TextComponentString("Jonin: " + mgr.isJonin(target)));

      for(StatCategory cat : StatCategory.values()) {
         for(StatElement el : StatElement.getElementsForCategory(cat)) {
            int level = data.getLevel(cat, el);
            if (level > 0) {
               double bonus = cat.isOffense() ? data.getOffenseBonus(el) : data.getDefenseReduction(el);
               sender.sendMessage(new TextComponentString("  " + cat.name() + "/" + el.name() + " = Lv" + level + " (" + String.format("%.1f%%", bonus * (double)100.0F) + ")"));
            }
         }
      }

   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"view", "grantsp", "setsp", "reset", "granttoken", "debug"});
      } else {
         return args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.emptyList();
      }
   }
}
