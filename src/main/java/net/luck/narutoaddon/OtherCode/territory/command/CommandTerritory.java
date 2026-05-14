
package net.luck.narutoaddon.OtherCode.territory.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandTerritory extends CommandBase {
   public String getName() {
      return "territory";
   }

   public String getUsage(ICommandSender sender) {
      return "/territory <toggle|info|leaderboard>";
   }

   public int getRequiredPermissionLevel() {
      return 0;
   }

   public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
      return false;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      return Collections.emptyList();
   }
}
