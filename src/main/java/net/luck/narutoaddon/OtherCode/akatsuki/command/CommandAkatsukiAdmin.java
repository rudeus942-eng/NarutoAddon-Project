package net.luck.narutoaddon.OtherCode.akatsuki.command;

import net.luck.narutoaddon.OtherCode.akatsuki.bounty.BountyManager;
import net.luck.narutoaddon.OtherCode.akatsuki.core.*;
import net.luck.narutoaddon.OtherCode.akatsuki.mission.LeaderMission;
import net.luck.narutoaddon.OtherCode.akatsuki.raid.VillageRaid;
import net.luck.narutoaddon.OtherCode.akatsuki.raid.VillageRaidManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandAkatsukiAdmin extends CommandBase {
   public String getName() {
      return "akatsukiadmin";
   }

   public String getUsage(ICommandSender sender) {
      return "/akatsukiadmin <invite|kick|setrank|setrep|setring|settokens|setpartner|list|addbounty|createmission|removemission|listmissions|raid>";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length == 0) {
         this.sendHelp(sender);
      } else {
         switch (args[0].toLowerCase()) {
            case "invite":
               this.handleInvite(server, sender, args);
               break;
            case "kick":
               this.handleKick(server, sender, args);
               break;
            case "setrank":
               this.handleSetRank(server, sender, args);
               break;
            case "setrep":
               this.handleSetRep(server, sender, args);
               break;
            case "setring":
               this.handleSetRing(server, sender, args);
               break;
            case "settokens":
               this.handleSetTokens(server, sender, args);
               break;
            case "setpartner":
               this.handleSetPartner(server, sender, args);
               break;
            case "list":
               this.handleList(server, sender);
               break;
            case "addbounty":
               this.handleAddBounty(server, sender, args);
               break;
            case "createmission":
               this.handleCreateMission(server, sender, args);
               break;
            case "removemission":
               this.handleRemoveMission(server, sender, args);
               break;
            case "listmissions":
               this.handleListMissions(server, sender);
               break;
            case "raid":
               this.handleRaid(server, sender, args);
               break;
            default:
               this.sendHelp(sender);
         }

      }
   }

   private AkatsukiSavedData getData(MinecraftServer server) {
      return AkatsukiSavedData.get(server.getWorld(0));
   }

   private void handleInvite(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /akatsukiadmin invite <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         AkatsukiSavedData data = this.getData(server);
         if (data.isMember(target.getUniqueID())) {
            sender.sendMessage(new TextComponentString("§c" + target.getName() + " is already an Akatsuki member."));
         } else {
            AkatsukiRing assignedRing = this.findAvailableRing(data);
            AkatsukiMember member = new AkatsukiMember(target.getUniqueID(), assignedRing);
            data.addMember(target.getUniqueID(), member);
            Scoreboard scoreboard = server.getWorld(0).getScoreboard();
            if (scoreboard.getTeam("akatsuki") == null) {
               scoreboard.createTeam("akatsuki");
            }

            scoreboard.addPlayerToTeam(target.getName(), "akatsuki");
            sender.sendMessage(new TextComponentString("§a" + target.getName() + " has been inducted into the Akatsuki with ring " + assignedRing.kanji + " " + assignedRing.romajiName + "."));
            target.sendMessage(new TextComponentString("§4§lYou have been inducted into the Akatsuki.§r\n§cYour ring: " + assignedRing.kanji + " " + assignedRing.romajiName + " §7(" + assignedRing.englishName + " - " + assignedRing.fingerPosition + ")\n§cUse §f/akatsuki info§c to view your profile."));
            AkatsukiManager.getInstance().syncToAllOnline(server);
         }
      }
   }

   private AkatsukiRing findAvailableRing(AkatsukiSavedData data) {
      Map<UUID, AkatsukiMember> members = data.getAllMembers();

      for(AkatsukiRing ring : AkatsukiRing.values()) {
         boolean taken = false;

         for(AkatsukiMember m : members.values()) {
            if (m.getRing() == ring) {
               taken = true;
               break;
            }
         }

         if (!taken) {
            return ring;
         }
      }

      return AkatsukiRing.REI;
   }

   private void handleKick(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /akatsukiadmin kick <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         AkatsukiSavedData data = this.getData(server);
         if (!data.isMember(target.getUniqueID())) {
            sender.sendMessage(new TextComponentString("§c" + target.getName() + " is not an Akatsuki member."));
         } else {
            AkatsukiMember member = data.getMember(target.getUniqueID());
            if (member != null && member.getPartnerId() != null) {
               AkatsukiMember partner = data.getMember(member.getPartnerId());
               if (partner != null) {
                  partner.setPartnerId((UUID)null);
               }
            }

            data.removeMember(target.getUniqueID());
            ScorePlayerTeam akatsukiTeam = server.getWorld(0).getScoreboard().getTeam("akatsuki");
            if (akatsukiTeam != null) {
               server.getWorld(0).getScoreboard().removePlayerFromTeam(target.getName(), akatsukiTeam);
            }

            sender.sendMessage(new TextComponentString("§a" + target.getName() + " has been removed from the Akatsuki."));
            target.sendMessage(new TextComponentString("§cYou have been expelled from the Akatsuki."));
            AkatsukiManager.getInstance().syncToAllOnline(server);
         }
      }
   }

   private void handleSetRank(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /akatsukiadmin setrank <player> <INITIATE|OPERATIVE|LIEUTENANT|CAPTAIN|INNER_CIRCLE>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         AkatsukiSavedData data = this.getData(server);
         AkatsukiMember member = data.getMember(target.getUniqueID());
         if (member == null) {
            sender.sendMessage(new TextComponentString("§c" + target.getName() + " is not an Akatsuki member."));
         } else {
            String rankStr = args[2].toUpperCase();
            AkatsukiRank targetRank = null;

            for(AkatsukiRank rank : AkatsukiRank.values()) {
               if (rank.name().equalsIgnoreCase(rankStr) || rank.displayName.equalsIgnoreCase(args[2])) {
                  targetRank = rank;
                  break;
               }
            }

            if (targetRank == null) {
               sender.sendMessage(new TextComponentString("§cInvalid rank: " + args[2] + ". Use: INITIATE, OPERATIVE, LIEUTENANT, CAPTAIN, INNER_CIRCLE"));
            } else {
               member.setReputation(targetRank.repRequired);
               data.markDirty();
               sender.sendMessage(new TextComponentString("§aSet " + target.getName() + "'s rank to " + targetRank.displayName + " (rep: " + targetRank.repRequired + ")"));
               AkatsukiManager.getInstance().syncToClient(target);
            }
         }
      }
   }

   private void handleSetRep(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /akatsukiadmin setrep <player> <amount>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         AkatsukiSavedData data = this.getData(server);
         AkatsukiMember member = data.getMember(target.getUniqueID());
         if (member == null) {
            sender.sendMessage(new TextComponentString("§c" + target.getName() + " is not an Akatsuki member."));
         } else {
            int amount;
            try {
               amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException var9) {
               sender.sendMessage(new TextComponentString("§cInvalid number: " + args[2]));
               return;
            }

            member.setReputation(amount);
            data.markDirty();
            sender.sendMessage(new TextComponentString("§aSet " + target.getName() + "'s reputation to " + amount + " (Rank: " + member.getRank().displayName + ")"));
            AkatsukiManager.getInstance().syncToClient(target);
         }
      }
   }

   private void handleSetRing(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /akatsukiadmin setring <player> <ring>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         AkatsukiSavedData data = this.getData(server);
         AkatsukiMember member = data.getMember(target.getUniqueID());
         if (member == null) {
            sender.sendMessage(new TextComponentString("§c" + target.getName() + " is not an Akatsuki member."));
         } else {
            AkatsukiRing ring = null;

            for(AkatsukiRing r : AkatsukiRing.values()) {
               if (r.name().equalsIgnoreCase(args[2]) || r.romajiName.equalsIgnoreCase(args[2])) {
                  ring = r;
                  break;
               }
            }

            if (ring == null) {
               sender.sendMessage(new TextComponentString("§cInvalid ring: " + args[2] + ". Use: REI, AO, BYAKU, SHU, GAI, KU, NAN, HOKU, SAN, GYOKU"));
            } else {
               member.setRing(ring);
               data.markDirty();
               sender.sendMessage(new TextComponentString("§aSet " + target.getName() + "'s ring to " + ring.kanji + " " + ring.romajiName + " (" + ring.englishName + ")"));
               AkatsukiManager.getInstance().syncToClient(target);
            }
         }
      }
   }

   private void handleSetTokens(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /akatsukiadmin settokens <player> <amount>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         AkatsukiSavedData data = this.getData(server);
         AkatsukiMember member = data.getMember(target.getUniqueID());
         if (member == null) {
            sender.sendMessage(new TextComponentString("§c" + target.getName() + " is not an Akatsuki member."));
         } else {
            int amount;
            try {
               amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException var9) {
               sender.sendMessage(new TextComponentString("§cInvalid number: " + args[2]));
               return;
            }

            member.addBountyTokens(-member.getBountyTokens());
            member.addBountyTokens(amount);
            data.markDirty();
            sender.sendMessage(new TextComponentString("§aSet " + target.getName() + "'s bounty tokens to " + amount));
            AkatsukiManager.getInstance().syncToClient(target);
         }
      }
   }

   private void handleSetPartner(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /akatsukiadmin setpartner <player1> <player2>"));
      } else {
         EntityPlayerMP player1 = getPlayer(server, sender, args[1]);
         EntityPlayerMP player2 = getPlayer(server, sender, args[2]);
         AkatsukiSavedData data = this.getData(server);
         AkatsukiMember member1 = data.getMember(player1.getUniqueID());
         AkatsukiMember member2 = data.getMember(player2.getUniqueID());
         if (member1 == null) {
            sender.sendMessage(new TextComponentString("§c" + player1.getName() + " is not an Akatsuki member."));
         } else if (member2 == null) {
            sender.sendMessage(new TextComponentString("§c" + player2.getName() + " is not an Akatsuki member."));
         } else {
            if (member1.getPartnerId() != null) {
               AkatsukiMember oldPartner = data.getMember(member1.getPartnerId());
               if (oldPartner != null) {
                  oldPartner.setPartnerId((UUID)null);
               }
            }

            if (member2.getPartnerId() != null) {
               AkatsukiMember oldPartner = data.getMember(member2.getPartnerId());
               if (oldPartner != null) {
                  oldPartner.setPartnerId((UUID)null);
               }
            }

            member1.setPartnerId(player2.getUniqueID());
            member2.setPartnerId(player1.getUniqueID());
            data.markDirty();
            sender.sendMessage(new TextComponentString("§aPaired " + player1.getName() + " [" + member1.getRing().kanji + "] and " + player2.getName() + " [" + member2.getRing().kanji + "] as partners."));
            player1.sendMessage(new TextComponentString("§4§l[Akatsuki] §r§cYou have been paired with §f" + player2.getName() + "§c as your partner."));
            player2.sendMessage(new TextComponentString("§4§l[Akatsuki] §r§cYou have been paired with §f" + player1.getName() + "§c as your partner."));
         }
      }
   }

   private void handleList(MinecraftServer server, ICommandSender sender) {
      AkatsukiSavedData data = this.getData(server);
      Map<UUID, AkatsukiMember> members = data.getAllMembers();
      if (members.isEmpty()) {
         sender.sendMessage(new TextComponentString("§cNo Akatsuki members."));
      } else {
         sender.sendMessage(new TextComponentString("§4§l=== Akatsuki Members (" + members.size() + ") ==="));

         for(Map.Entry<UUID, AkatsukiMember> entry : members.entrySet()) {
            AkatsukiMember member = (AkatsukiMember)entry.getValue();
            EntityPlayerMP online = server.getPlayerList().getPlayerByUUID((UUID)entry.getKey());
            String name = online != null ? online.getName() : ((UUID)entry.getKey()).toString().substring(0, 8) + "...";
            String status = online != null ? "§a[ON]" : "§7[OFF]";
            String partnerInfo = "";
            if (member.getPartnerId() != null) {
               EntityPlayerMP partnerPlayer = server.getPlayerList().getPlayerByUUID(member.getPartnerId());
               String partnerName = partnerPlayer != null ? partnerPlayer.getName() : "?";
               partnerInfo = " §7Partner: " + partnerName;
            }

            sender.sendMessage(new TextComponentString(status + " §c" + member.getRing().kanji + " §f" + name + " §7- " + member.getRank().displayName + " (Rep: " + member.getReputation() + ", Tokens: " + member.getBountyTokens() + ")" + partnerInfo));
         }

      }
   }

   private void handleAddBounty(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /akatsukiadmin addbounty <player> <amount>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         AkatsukiSavedData data = this.getData(server);

         int amount;
         try {
            amount = Integer.parseInt(args[2]);
         } catch (NumberFormatException var8) {
            sender.sendMessage(new TextComponentString("§cInvalid number: " + args[2]));
            return;
         }

         if (amount <= 0) {
            sender.sendMessage(new TextComponentString("§cAmount must be positive."));
         } else {
            BountyManager.addBounty(data, target.getUniqueID(), target.getName(), amount, (UUID)null);
            sender.sendMessage(new TextComponentString("§aAdded " + amount + " ryo bounty on " + target.getName() + "."));
         }
      }
   }

   private void handleCreateMission(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 6) {
         sender.sendMessage(new TextComponentString("§cUsage: /akatsukiadmin createmission <type> <player> <tokenReward> <repReward> <description...>"));
      } else {
         String type = args[1].toLowerCase();
         if (!type.equals("assassination") && !type.equals("infiltration") && !type.equals("extraction") && !type.equals("sabotage") && !type.equals("escort")) {
            sender.sendMessage(new TextComponentString("§cInvalid mission type: " + args[1] + ". Use: assassination, infiltration, extraction, sabotage, escort"));
         } else {
            EntityPlayerMP target = getPlayer(server, sender, args[2]);
            AkatsukiSavedData data = this.getData(server);
            if (!data.isMember(target.getUniqueID())) {
               sender.sendMessage(new TextComponentString("§c" + target.getName() + " is not an Akatsuki member."));
            } else {
               int tokenReward;
               int repReward;
               try {
                  tokenReward = Integer.parseInt(args[3]);
                  repReward = Integer.parseInt(args[4]);
               } catch (NumberFormatException var11) {
                  sender.sendMessage(new TextComponentString("§cInvalid number for reward."));
                  return;
               }

               StringBuilder desc = new StringBuilder();

               for(int i = 5; i < args.length; ++i) {
                  if (i > 5) {
                     desc.append(" ");
                  }

                  desc.append(args[i]);
               }

               LeaderMission mission = new LeaderMission(type, desc.toString(), tokenReward, repReward, target.getUniqueID());
               data.setLeaderMission(target.getUniqueID(), mission);
               sender.sendMessage(new TextComponentString("§aCreated " + type + " mission for " + target.getName() + " (Tokens: " + tokenReward + ", Rep: " + repReward + ")"));
               target.sendMessage(new TextComponentString("§4§l[Akatsuki] §r§cYou have been assigned a new mission: §f" + type + "\n§7" + desc.toString()));
            }
         }
      }
   }

   private void handleRemoveMission(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /akatsukiadmin removemission <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         AkatsukiSavedData data = this.getData(server);
         LeaderMission mission = data.getLeaderMission(target.getUniqueID());
         if (mission == null) {
            sender.sendMessage(new TextComponentString("§c" + target.getName() + " has no active leader mission."));
         } else {
            data.removeLeaderMission(target.getUniqueID());
            sender.sendMessage(new TextComponentString("§aRemoved leader mission from " + target.getName() + "."));
         }
      }
   }

   private void handleListMissions(MinecraftServer server, ICommandSender sender) {
      AkatsukiSavedData data = this.getData(server);
      Map<UUID, LeaderMission> missions = data.getAllLeaderMissions();
      if (missions.isEmpty()) {
         sender.sendMessage(new TextComponentString("§cNo active leader missions."));
      } else {
         sender.sendMessage(new TextComponentString("§4§l=== Leader Missions (" + missions.size() + ") ==="));

         for(Map.Entry<UUID, LeaderMission> entry : missions.entrySet()) {
            LeaderMission mission = (LeaderMission)entry.getValue();
            EntityPlayerMP online = server.getPlayerList().getPlayerByUUID((UUID)entry.getKey());
            String name = online != null ? online.getName() : ((UUID)entry.getKey()).toString().substring(0, 8) + "...";
            String status = mission.isCompleted() ? "§a[DONE]" : "§e[ACTIVE]";
            sender.sendMessage(new TextComponentString(status + " §f" + name + " §7- §c" + mission.getType() + " §7(Tokens: " + mission.getTokenReward() + ", Rep: " + mission.getRepReward() + ")\n  §7" + mission.getDescription()));
         }

      }
   }

   private void handleRaid(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /akatsukiadmin raid <start|cancel|status>"));
      } else {
         String action = args[1].toLowerCase();
         VillageRaidManager mgr = VillageRaidManager.getInstance();
         switch (action) {
            case "start":
               if (args.length < 3) {
                  sender.sendMessage(new TextComponentString("§cUsage: /akatsukiadmin raid start <village> [duration_minutes]"));
                  return;
               }

               String village = args[2].toLowerCase();
               if (!VillageRaidManager.isValidVillage(village)) {
                  sender.sendMessage(new TextComponentString("§cInvalid village: " + args[2] + ". Use: leaf, sand, mist, stone, cloud, rain"));
                  return;
               }

               int duration = 30;
               if (args.length >= 4) {
                  try {
                     duration = Integer.parseInt(args[3]);
                     if (duration < 5 || duration > 120) {
                        sender.sendMessage(new TextComponentString("§cDuration must be 5-120 minutes."));
                        return;
                     }
                  } catch (NumberFormatException var12) {
                     sender.sendMessage(new TextComponentString("§cInvalid number: " + args[3]));
                     return;
                  }
               }

               EntityPlayerMP leader = sender instanceof EntityPlayerMP ? (EntityPlayerMP)sender : null;
               VillageRaid raid = mgr.startRaid(village, duration, leader != null ? leader : (EntityPlayerMP)server.getPlayerList().getPlayers().get(0));
               if (raid != null) {
                  sender.sendMessage(new TextComponentString("§aRaid started on " + village + " village. Rally: (" + raid.getRallyX() + ", " + raid.getRallyZ() + "), Duration: " + duration + " min."));
               }
               break;
            case "cancel":
               if (!mgr.isRaidActive()) {
                  sender.sendMessage(new TextComponentString("§cNo active raid to cancel."));
                  return;
               }

               mgr.cancelRaid();
               sender.sendMessage(new TextComponentString("§aRaid cancelled."));
               break;
            case "status":
               if (!mgr.isRaidActive()) {
                  sender.sendMessage(new TextComponentString("§7No raid currently active."));
                  return;
               }

               VillageRaid raid = mgr.getRaid();
               String villageCap = raid.getTargetVillage().substring(0, 1).toUpperCase() + raid.getTargetVillage().substring(1);
               long remainSec = raid.getTimeRemainingMs() / 1000L;
               sender.sendMessage(new TextComponentString("§4§l=== Village Raid ===\n§cTarget: §f" + villageCap + " Village\n§cStatus: §f" + raid.getStatus().name() + "\n§cRally: §f(" + raid.getRallyX() + ", " + raid.getRallyZ() + ")\n§cKills: §f" + raid.getKillsAchieved() + "/" + raid.getKillsRequired() + "\n§cParticipants: §f" + raid.getParticipants().size() + "\n§cTime Left: §f" + remainSec / 60L + "m " + remainSec % 60L + "s"));
               break;
            default:
               sender.sendMessage(new TextComponentString("§cUsage: /akatsukiadmin raid <start|cancel|status>"));
         }

      }
   }

   private void sendHelp(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§4§l=== Akatsuki Admin Commands ==="));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin invite <player> §7- Induct player into Akatsuki"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin kick <player> §7- Remove from Akatsuki"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin setrank <player> <rank> §7- Set rank (sets rep to threshold)"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin setrep <player> <amount> §7- Set exact reputation"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin setring <player> <ring> §7- Assign ring"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin settokens <player> <amount> §7- Set bounty tokens"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin setpartner <p1> <p2> §7- Pair partners"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin list §7- List all members"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin addbounty <player> <amount> §7- Add bounty"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin createmission <type> <player> <tokens> <rep> <desc...> §7- Create leader mission"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin removemission <player> §7- Remove leader mission"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin listmissions §7- List active leader missions"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin raid start <village> [duration] §7- Start village raid"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin raid cancel §7- Cancel active raid"));
      sender.sendMessage(new TextComponentString("§c/akatsukiadmin raid status §7- Show raid info"));
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"invite", "kick", "setrank", "setrep", "setring", "settokens", "setpartner", "list", "addbounty", "createmission", "removemission", "listmissions", "raid"});
      } else {
         if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("invite") || sub.equals("kick") || sub.equals("setrank") || sub.equals("setrep") || sub.equals("setring") || sub.equals("settokens") || sub.equals("setpartner") || sub.equals("addbounty") || sub.equals("removemission")) {
               return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            }

            if (sub.equals("createmission")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"assassination", "infiltration", "extraction", "sabotage", "escort"});
            }

            if (sub.equals("raid")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"start", "cancel", "status"});
            }
         }

         if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("setrank")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"INITIATE", "OPERATIVE", "LIEUTENANT", "CAPTAIN", "INNER_CIRCLE"});
            }

            if (sub.equals("createmission")) {
               return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            }

            if (sub.equals("setring")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"REI", "AO", "BYAKU", "SHU", "GAI", "KU", "NAN", "HOKU", "SAN", "GYOKU"});
            }

            if (sub.equals("setpartner")) {
               return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            }

            if (sub.equals("raid") && args[1].equalsIgnoreCase("start")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"leaf", "sand", "mist", "stone", "cloud", "rain"});
            }
         }

         return Collections.emptyList();
      }
   }
}
