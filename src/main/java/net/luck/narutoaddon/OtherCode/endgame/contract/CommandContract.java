
package net.luck.narutoaddon.OtherCode.endgame.contract;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayList;

public class CommandContract extends CommandBase {
   public String getName() {
      return "contract";
   }

   public String getUsage(ICommandSender sender) {
      return "/contract <spawn|list|clear|refresh>";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length == 0) {
         sender.sendMessage(new TextComponentString("§e" + this.getUsage(sender)));
      } else if (!sender.canUseCommand(2, this.getName())) {
         throw new CommandException("You do not have permission to run that.", new Object[0]);
      } else {
         switch (args[0].toLowerCase()) {
            case "spawn":
               this.handleSpawn(sender);
               break;
            case "list":
               this.handleList(sender);
               break;
            case "clear":
               this.handleClear(sender);
               break;
            case "refresh":
               this.handleRefresh(sender);
               break;
            default:
               sender.sendMessage(new TextComponentString("§cUnknown: " + sub));
         }

      }
   }

   private void handleSpawn(ICommandSender sender) {
      World world = sender.getEntityWorld();
      ContractManager.getInstance().maintainPool(world);
      ContractSavedData data = ContractSavedData.get(world);
      sender.sendMessage(new TextComponentString("§aPool size: §e" + data.getActiveContracts().size() + "/" + 20));
   }

   private void handleList(ICommandSender sender) {
      ContractSavedData data = ContractSavedData.get(sender.getEntityWorld());

      for(ContractDefinition c : data.getActiveContracts()) {
         sender.sendMessage(new TextComponentString("§7[" + c.getRank().name() + "] §f" + c.getTargetName() + " §7" + c.getLocationHint()));
      }

   }

   private void handleClear(ICommandSender sender) {
      ContractSavedData data = ContractSavedData.get(sender.getEntityWorld());
      data.replaceActiveContracts(new ArrayList());
      sender.sendMessage(new TextComponentString("§ePool cleared."));
   }

   private void handleRefresh(ICommandSender sender) {
      World world = sender.getEntityWorld();
      ContractSavedData data = ContractSavedData.get(world);
      int before = data.getActiveContracts().size();
      data.replaceActiveContracts(new ArrayList());
      ContractManager.getInstance().maintainPool(world);
      int after = data.getActiveContracts().size();
      sender.sendMessage(new TextComponentString("§aContract pool refreshed: §7" + before + " §7-> §e" + after + "§7/§e" + 20));
   }
}
