package net.luck.narutoaddon.OtherCode.command;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.ProtectionNerfHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ElementsInfTsukAddon.ModElement.Tag
public class CommandProtectionNerf extends ElementsInfTsukAddon.ModElement {
   public CommandProtectionNerf(ElementsInfTsukAddon instance) {
      super(instance, 103);
   }

   public void serverLoad(FMLServerStartingEvent event) {
      event.registerServerCommand(new RankedProtectionCommand());
   }

   public static class RankedProtectionCommand extends CommandBase {
      public String getName() {
         return "rankedprotection";
      }

      public String getUsage(ICommandSender sender) {
         return "/rankedprotection <status|enable|disable|cap <value>>";
      }

      public int getRequiredPermissionLevel() {
         return 2;
      }

      public List<String> getAliases() {
         return Arrays.asList("rprot", "rankedprot");
      }

      public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
         if (args.length == 0) {
            throw new WrongUsageException(this.getUsage(sender), new Object[0]);
         } else {
            switch (args[0].toLowerCase()) {
               case "status":
                  this.showStatus(sender);
                  break;
               case "enable":
                  ProtectionNerfHandler.setEnabled(true);
                  sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "[RankedProtection] " + TextFormatting.WHITE + "EPF cap system enabled."));
                  break;
               case "disable":
                  ProtectionNerfHandler.setEnabled(false);
                  sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "[RankedProtection] " + TextFormatting.WHITE + "EPF cap system disabled."));
                  break;
               case "cap":
                  if (args.length < 2) {
                     throw new WrongUsageException("/rankedprotection cap <0-20>", new Object[0]);
                  }

                  try {
                     int value = Integer.parseInt(args[1]);
                     if (value >= 0 && value <= 20) {
                        ProtectionNerfHandler.setEPFCap(value);
                        float reductionPercent = ProtectionNerfHandler.getReductionPercent(value);
                        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "[RankedProtection] " + TextFormatting.WHITE + "EPF cap set to " + TextFormatting.AQUA + value + TextFormatting.WHITE + "."));
                        sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "  Max damage reduction in ranked: " + String.format("%.0f%%", reductionPercent) + (value == 20 ? " (vanilla max)" : " (vanilla max is 80%)")));
                        if (value == 0) {
                           sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "  Protection enchantments will have no effect in ranked matches."));
                        } else if (value < 20) {
                           sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "  Players with more than EPF " + value + " will be capped."));
                        }
                        break;
                     }

                     throw new CommandException("Value must be between 0 and 20", new Object[0]);
                  } catch (NumberFormatException var9) {
                     throw new CommandException("Invalid number: " + args[1], new Object[0]);
                  }
               default:
                  throw new WrongUsageException(this.getUsage(sender), new Object[0]);
            }

         }
      }

      private void showStatus(ICommandSender sender) {
         boolean enabled = ProtectionNerfHandler.isEnabled();
         int epfCap = ProtectionNerfHandler.getEPFCap();
         float reductionPercent = ProtectionNerfHandler.getReductionPercent(epfCap);
         sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== Ranked Protection EPF Cap ==="));
         sender.sendMessage(new TextComponentString(TextFormatting.WHITE + "Enabled: " + (enabled ? TextFormatting.GREEN + "Yes" : TextFormatting.RED + "No")));
         sender.sendMessage(new TextComponentString(TextFormatting.WHITE + "EPF Cap: " + TextFormatting.AQUA + epfCap + TextFormatting.GRAY + " (max " + String.format("%.0f%%", reductionPercent) + " reduction)"));
         sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Vanilla max: EPF 20 = 80% reduction"));
         sender.sendMessage(new TextComponentString(TextFormatting.DARK_GRAY + "Only affects players in active ranked matches."));
      }

      public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
         if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, new String[]{"status", "enable", "disable", "cap"});
         } else {
            return args.length == 2 && args[0].equalsIgnoreCase("cap") ? getListOfStringsMatchingLastWord(args, new String[]{"0", "8", "12", "16", "20"}) : Collections.emptyList();
         }
      }
   }
}
