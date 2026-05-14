package net.luck.narutoaddon.OtherCode.akatsuki.command;

import net.luck.narutoaddon.OtherCode.akatsuki.bounty.BountyEntry;
import net.luck.narutoaddon.OtherCode.akatsuki.bounty.HeatTracker;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiMember;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiRank;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiSavedData;
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
import java.util.Map;

public class CommandAkatsuki extends CommandBase {
   public String getName() {
      return "akatsuki";
   }

   public String getUsage(ICommandSender sender) {
      return "/akatsuki <info|partner|bounty>";
   }

   public int getRequiredPermissionLevel() {
      return 0;
   }

   public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
      return sender instanceof EntityPlayerMP;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         sender.sendMessage(new TextComponentString("§cThis command can only be used by players."));
      } else {
         EntityPlayerMP player = (EntityPlayerMP)sender;
         AkatsukiSavedData data = AkatsukiSavedData.get(player.getServerWorld());
         if (args.length == 0) {
            this.sendHelp(sender);
         } else {
            switch (args[0].toLowerCase()) {
               case "info":
                  this.handleInfo(player, data);
                  break;
               case "partner":
                  this.handlePartner(player, data, server);
                  break;
               case "bounty":
                  this.handleBounty(player, data);
                  break;
               default:
                  this.sendHelp(sender);
            }

         }
      }
   }

   private void handleInfo(EntityPlayerMP player, AkatsukiSavedData data) {
      AkatsukiMember member = data.getMember(player.getUniqueID());
      if (member == null) {
         player.sendMessage(new TextComponentString("§cYou are not a member of the Akatsuki."));
      } else {
         AkatsukiRank rank = member.getRank();
         AkatsukiRank nextRank = rank.next();
         int repToNext = rank.repToNext();
         player.sendMessage(new TextComponentString("§4§l=== Akatsuki Member Info ==="));
         player.sendMessage(new TextComponentString("§cRing: §f" + member.getRing().kanji + " " + member.getRing().romajiName + " §7(" + member.getRing().englishName + " - " + member.getRing().fingerPosition + ")"));
         player.sendMessage(new TextComponentString("§cRank: §f" + rank.displayName));
         player.sendMessage(new TextComponentString("§cReputation: §f" + member.getReputation() + (repToNext > 0 ? " §7(" + repToNext + " to " + nextRank.displayName + ")" : " §7(Max Rank)")));
         player.sendMessage(new TextComponentString("§cBounty Tokens: §f" + member.getBountyTokens()));
         if (member.getPartnerId() != null) {
            AkatsukiMember partner = data.getMember(member.getPartnerId());
            if (partner != null) {
               EntityPlayerMP partnerPlayer = player.getServerWorld().getMinecraftServer().getPlayerList().getPlayerByUUID(member.getPartnerId());
               String partnerName = partnerPlayer != null ? partnerPlayer.getName() : "(offline)";
               player.sendMessage(new TextComponentString("§cPartner: §f" + partnerName + " §7[" + partner.getRing().kanji + "]"));
            }
         } else {
            player.sendMessage(new TextComponentString("§cPartner: §7None"));
         }

         Map<String, Integer> heatMap = member.getHeatMap();
         if (!heatMap.isEmpty()) {
            StringBuilder heatLine = new StringBuilder("§cHeat: ");
            boolean first = true;

            for(Map.Entry<String, Integer> entry : heatMap.entrySet()) {
               if (!first) {
                  heatLine.append("§7, ");
               }

               String color;
               switch (HeatTracker.getHeatLevel(member, (String)entry.getKey())) {
                  case "CRITICAL":
                     color = "§4";
                     break;
                  case "HIGH":
                     color = "§c";
                     break;
                  case "MEDIUM":
                     color = "§e";
                     break;
                  default:
                     color = "§a";
               }

               heatLine.append("§f").append((String)entry.getKey()).append(" ").append(color).append(entry.getValue()).append(" §7(").append(level).append(")");
               first = false;
            }

            player.sendMessage(new TextComponentString(heatLine.toString()));
         }

      }
   }

   private void handlePartner(EntityPlayerMP player, AkatsukiSavedData data, MinecraftServer server) {
      AkatsukiMember member = data.getMember(player.getUniqueID());
      if (member == null) {
         player.sendMessage(new TextComponentString("§cYou are not a member of the Akatsuki."));
      } else if (member.getPartnerId() == null) {
         player.sendMessage(new TextComponentString("§cYou do not have an assigned partner."));
      } else {
         AkatsukiMember partner = data.getMember(member.getPartnerId());
         if (partner == null) {
            player.sendMessage(new TextComponentString("§cYour partner's data could not be found."));
         } else {
            EntityPlayerMP partnerPlayer = server.getPlayerList().getPlayerByUUID(member.getPartnerId());
            String partnerName = partnerPlayer != null ? partnerPlayer.getName() : "(offline)";
            boolean online = partnerPlayer != null;
            player.sendMessage(new TextComponentString("§4§l=== Partner Info ==="));
            player.sendMessage(new TextComponentString("§cName: §f" + partnerName + (online ? " §a[Online]" : " §7[Offline]")));
            player.sendMessage(new TextComponentString("§cRing: §f" + partner.getRing().kanji + " " + partner.getRing().romajiName));
            player.sendMessage(new TextComponentString("§cRank: §f" + partner.getRank().displayName));
            player.sendMessage(new TextComponentString("§cReputation: §f" + partner.getReputation()));
         }
      }
   }

   private void handleBounty(EntityPlayerMP player, AkatsukiSavedData data) {
      List<BountyEntry> bounties = data.getActiveBounties();
      if (bounties.isEmpty()) {
         player.sendMessage(new TextComponentString("§cNo active bounties."));
      } else {
         player.sendMessage(new TextComponentString("§4§l=== Active Bounties ==="));
         int shown = 0;

         for(BountyEntry entry : bounties) {
            if (!entry.isExpired()) {
               long remainingMs = entry.getPlacedTimestamp() + 604800000L - System.currentTimeMillis();
               long remainingHours = remainingMs / 3600000L;
               long remainingDays = remainingHours / 24L;
               String timeStr;
               if (remainingDays > 0L) {
                  timeStr = remainingDays + "d " + remainingHours % 24L + "h";
               } else {
                  timeStr = remainingHours + "h";
               }

               String source = entry.getPlacedBy() != null ? "Player" : "Auto";
               player.sendMessage(new TextComponentString("§c" + entry.getTargetName() + " §7- §6" + entry.getBountyAmount() + " ryo §7[" + source + "] §8(" + timeStr + " left)"));
               ++shown;
            }
         }

         if (shown == 0) {
            player.sendMessage(new TextComponentString("§cNo active bounties."));
         }

      }
   }

   private void sendHelp(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§4§l=== Akatsuki Commands ==="));
      sender.sendMessage(new TextComponentString("§c/akatsuki info §7- View your Akatsuki profile"));
      sender.sendMessage(new TextComponentString("§c/akatsuki partner §7- View your partner's info"));
      sender.sendMessage(new TextComponentString("§c/akatsuki bounty §7- List active bounties"));
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[]{"info", "partner", "bounty"}) : Collections.emptyList();
   }
}
