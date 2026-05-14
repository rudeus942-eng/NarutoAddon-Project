
package net.luck.narutoaddon.OtherCode.stat.command;

import net.luck.narutoaddon.OtherCode.stat.core.PlayerStatData;
import net.luck.narutoaddon.OtherCode.stat.core.StatCategory;
import net.luck.narutoaddon.OtherCode.stat.core.StatElement;
import net.luck.narutoaddon.OtherCode.stat.core.StatManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandStat extends CommandBase {
   public String getName() {
      return "stats";
   }

   public String getUsage(ICommandSender sender) {
      return "/stats [respec]";
   }

   public int getRequiredPermissionLevel() {
      return 0;
   }

   public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
      return sender instanceof EntityPlayerMP;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      EntityPlayerMP player = getCommandSenderAsPlayer(sender);
      StatManager mgr = StatManager.getInstance();
      if (!mgr.isJonin(player)) {
         player.sendMessage(new TextComponentString(TextFormatting.RED + "Stats unlock at Jonin rank (complete Konoha Crush)."));
      } else if (args.length >= 1 && "respec".equalsIgnoreCase(args[0])) {
         String error = mgr.respec(player);
         if (error != null) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + error));
         }

      } else {
         PlayerStatData data = mgr.getPlayerData(player.getServerWorld(), player.getUniqueID());
         this.showStatSummary(player, data);
      }
   }

   private void showStatSummary(EntityPlayerMP player, PlayerStatData data) {
      player.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== " + TextFormatting.YELLOW + "Your Stats" + TextFormatting.GOLD + " ==="));
      player.sendMessage(new TextComponentString(TextFormatting.AQUA + "SP: " + TextFormatting.WHITE + data.getAvailableSP() + "/" + data.getSpEarned() + TextFormatting.GRAY + " (" + data.getSpSpent() + " spent)"));
      this.showCategory(player, data, StatCategory.NATURE_OFFENSE, "Nature Mastery");
      this.showCategory(player, data, StatCategory.KG_OFFENSE, "KG Mastery");
      this.showCategory(player, data, StatCategory.NATURE_DEFENSE, "Nature Defense");
      this.showCategory(player, data, StatCategory.KG_DEFENSE, "KG Defense");
      player.sendMessage(new TextComponentString(TextFormatting.GRAY + "Respec Tokens: " + data.getRespecTokens() + " | Use /stats respec"));
   }

   private void showCategory(EntityPlayerMP player, PlayerStatData data, StatCategory category, String label) {
      StringBuilder sb = new StringBuilder();
      sb.append(TextFormatting.YELLOW).append(label).append(": ");
      boolean hasAny = false;

      for(StatElement el : StatElement.getElementsForCategory(category)) {
         int level = data.getLevel(category, el);
         if (level > 0) {
            if (hasAny) {
               sb.append(TextFormatting.GRAY).append(", ");
            }

            sb.append(TextFormatting.WHITE).append(el.getDisplayName()).append(" ").append(level);
            hasAny = true;
         }
      }

      if (!hasAny) {
         sb.append(TextFormatting.GRAY).append("None");
      }

      player.sendMessage(new TextComponentString(sb.toString()));
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
      return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[]{"respec"}) : Collections.emptyList();
   }
}
