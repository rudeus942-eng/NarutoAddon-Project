
package net.luck.narutoaddon.OtherCode;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RankedCommands {
   public static class CommandRanked extends CommandBase {
      public String getName() {
         return "ranked";
      }

      public String getUsage(ICommandSender sender) {
         return "/ranked [queue|leave|stats|top|season|help]";
      }

      public int getRequiredPermissionLevel() {
         return 0;
      }

      public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
         return true;
      }

      public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
         if (!(sender instanceof EntityPlayerMP)) {
            sender.sendMessage(new TextComponentString("§cThis command can only be used by players!"));
         } else {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            World world = player.world;
            switch (args.length > 0 ? args[0].toLowerCase() : "") {
               case "":
               case "menu":
                  Rankedgui.showMainMenu(player);
                  break;
               case "queue":
               case "q":
                  this.handleQueue(player, world);
                  break;
               case "leave":
               case "cancel":
                  this.handleLeaveQueue(player);
                  break;
               case "stats":
                  this.handleStats(player, world, args);
                  break;
               case "top":
               case "leaderboard":
               case "lb":
                  this.handleLeaderboard(player, args);
                  break;
               case "season":
                  Rankedseason.showSeasonStatus(player);
                  break;
               case "help":
                  Rankedgui.showHelp(player, Rankedmain.isAdmin(player));
                  break;
               default:
                  player.sendMessage(new TextComponentString("§cUnknown command. Use /ranked help"));
            }

         }
      }

      private void handleQueue(EntityPlayerMP player, World world) {
         String uuid = player.getUniqueID().toString();
         Rankedpenalties.QueueEligibility eligibility = Rankedpenalties.canQueue(uuid);
         if (!eligibility.canQueue) {
            player.sendMessage(new TextComponentString(eligibility.message));
         } else if (Rankedqueue.isInQueue(uuid)) {
            player.sendMessage(new TextComponentString("§eYou are already in queue!"));
         } else if (Rankedqueue.isInMatch(uuid)) {
            player.sendMessage(new TextComponentString("§cYou are already in a match!"));
         } else {
            Rankedqueue.QueueResult result = Rankedqueue.joinQueue(world, player);
            player.sendMessage(new TextComponentString(result.message));
         }
      }

      private void handleLeaveQueue(EntityPlayerMP player) {
         String uuid = player.getUniqueID().toString();
         if (!Rankedqueue.isInQueue(uuid)) {
            player.sendMessage(new TextComponentString("§cYou are not in queue!"));
         } else {
            Rankedqueue.QueueResult result = Rankedqueue.leaveQueue(uuid);
            player.sendMessage(new TextComponentString(result.message));
         }
      }

      private void handleStats(EntityPlayerMP player, World world, String[] args) {
         String targetUUID;
         String targetName;
         if (args.length > 1) {
            EntityPlayerMP target = this.server(player).getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
               player.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
               return;
            }

            targetUUID = target.getUniqueID().toString();
            targetName = target.getName();
         } else {
            targetUUID = player.getUniqueID().toString();
            targetName = player.getName();
         }

         Rankedgui.showPlayerStats(player, targetUUID, targetName);
      }

      private void handleLeaderboard(EntityPlayerMP player, String[] args) {
         int page = 1;
         if (args.length > 1) {
            try {
               page = Integer.parseInt(args[1]);
            } catch (NumberFormatException var5) {
               page = 1;
            }
         }

         Rankedleaderboard.showLeaderboardChat(player, page);
      }

      private MinecraftServer server(EntityPlayerMP player) {
         return player.getServer();
      }

      public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
         List<String> completions = new ArrayList();
         if (args.length == 1) {
            completions.add("queue");
            completions.add("leave");
            completions.add("stats");
            completions.add("top");
            completions.add("season");
            completions.add("help");
         }

         return getListOfStringsMatchingLastWord(args, completions);
      }
   }

   public static class CommandRankedTop extends CommandBase {
      public String getName() {
         return "rankedtop";
      }

      public String getUsage(ICommandSender sender) {
         return "/rankedtop [page]";
      }

      public int getRequiredPermissionLevel() {
         return 0;
      }

      public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
         return true;
      }

      public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
         if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            int page = 1;
            if (args.length > 0) {
               try {
                  page = Integer.parseInt(args[0]);
               } catch (NumberFormatException var7) {
                  page = 1;
               }
            }

            Rankedleaderboard.showLeaderboardChat(player, page);
         }
      }
   }

   public static class CommandRankedAdmin extends CommandBase {
      public String getName() {
         return "rankedadmin";
      }

      public String getUsage(ICommandSender sender) {
         return "/rankedadmin <setelo|ban|unban|resetplayer|startseason|endseason>";
      }

      public int getRequiredPermissionLevel() {
         return 2;
      }

      public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
         if (!(sender instanceof EntityPlayerMP)) {
            sender.sendMessage(new TextComponentString("§cThis command can only be used by players!"));
         } else {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            World world = player.world;
            if (args.length == 0) {
               this.showAdminHelp(player);
            } else {
               switch (args[0].toLowerCase()) {
                  case "setelo":
                     this.handleSetElo(player, world, server, args);
                     break;
                  case "ban":
                     this.handleBan(player, world, server, args);
                     break;
                  case "unban":
                     this.handleUnban(player, world, server, args);
                     break;
                  case "resetplayer":
                     this.handleResetPlayer(player, world, server, args);
                     break;
                  case "startseason":
                     this.handleStartSeason(player, world, args);
                     break;
                  case "endseason":
                     this.handleEndSeason(player, world);
                     break;
                  case "granttitle":
                     this.handleGrantTitle(player, world, server, args);
                     break;
                  case "removetitle":
                     this.handleRemoveTitle(player, world, server, args);
                     break;
                  case "listtitles":
                     this.handleListTitles(player, world, server, args);
                     break;
                  case "seasoninfo":
                     this.handleSeasonInfo(player, world);
                     break;
                  case "setseasonname":
                     this.handleSetSeasonName(player, world, args);
                     break;
                  case "setseasonend":
                     this.handleSetSeasonEnd(player, world, args);
                     break;
                  case "setseasondays":
                     this.handleSetSeasonDays(player, world, args);
                     break;
                  case "pauseseason":
                     this.handlePauseSeason(player, world);
                     break;
                  case "resumeseason":
                     this.handleResumeSeason(player, world);
                     break;
                  case "setseasonid":
                     this.handleSetSeasonId(player, world, args);
                     break;
                  case "autotransition":
                     this.handleAutoTransition(player, world, args);
                     break;
                  case "listarenas":
                     this.handleListArenas(player, world);
                     break;
                  case "addarena":
                     this.handleAddArena(player, world, args);
                     break;
                  case "removearena":
                     this.handleRemoveArena(player, world, args);
                     break;
                  case "setarenasp1":
                  case "setarenaspawn1":
                     this.handleSetArenaSpawn(player, world, args, true);
                     break;
                  case "setarenasp2":
                  case "setarenaspawn2":
                     this.handleSetArenaSpawn(player, world, args, false);
                     break;
                  case "setarenaname":
                     this.handleSetArenaName(player, world, args);
                     break;
                  case "enablearena":
                     this.handleEnableArena(player, world, args, true);
                     break;
                  case "disablearena":
                     this.handleEnableArena(player, world, args, false);
                     break;
                  case "setringout":
                     this.handleSetRingOut(player, world, args);
                     break;
                  case "disableringout":
                     this.handleDisableRingOut(player, world, args);
                     break;
                  case "sethub":
                     this.handleSetHub(player, world);
                     break;
                  case "arenainfo":
                     this.handleArenaInfo(player, world, args);
                     break;
                  case "forceplace":
                     this.handleForcePlace(player, world, server, args);
                     break;
                  default:
                     this.showAdminHelp(player);
               }

            }
         }
      }

      private void showAdminHelp(EntityPlayerMP player) {
         player.sendMessage(new TextComponentString("§6§l===== RANKED ADMIN ====="));
         player.sendMessage(new TextComponentString("§e/rankedadmin setelo <player> <elo>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin ban <player> [minutes] [reason]"));
         player.sendMessage(new TextComponentString("§e/rankedadmin unban <player>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin resetplayer <player>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin forceplace <player>"));
         player.sendMessage(new TextComponentString("§6--- Season Commands ---"));
         player.sendMessage(new TextComponentString("§e/rankedadmin startseason [name]"));
         player.sendMessage(new TextComponentString("§e/rankedadmin endseason"));
         player.sendMessage(new TextComponentString("§e/rankedadmin seasoninfo"));
         player.sendMessage(new TextComponentString("§e/rankedadmin setseasonid <id>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin setseasonname <name>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin setseasonend <MM/DD/YYYY> [HH:MM]"));
         player.sendMessage(new TextComponentString("§e/rankedadmin setseasondays <days>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin pauseseason / resumeseason"));
         player.sendMessage(new TextComponentString("§e/rankedadmin autotransition <on|off>"));
         player.sendMessage(new TextComponentString("§6--- Title Commands ---"));
         player.sendMessage(new TextComponentString("§e/rankedadmin granttitle <player> <1|2|3> [season]"));
         player.sendMessage(new TextComponentString("§e/rankedadmin removetitle <player> <title>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin listtitles <player>"));
         player.sendMessage(new TextComponentString("§6--- Arena Commands ---"));
         player.sendMessage(new TextComponentString("§e/rankedadmin listarenas"));
         player.sendMessage(new TextComponentString("§e/rankedadmin arenainfo <id>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin addarena <name>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin removearena <id>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin setarenasp1 <id> / setarenasp2 <id>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin setarenaname <id> <name>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin enablearena <id> / disablearena <id>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin setringout <id> <y>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin disableringout <id>"));
         player.sendMessage(new TextComponentString("§e/rankedadmin sethub"));
         player.sendMessage(new TextComponentString("§6§l========================"));
      }

      private void handleSetElo(EntityPlayerMP admin, World world, MinecraftServer server, String[] args) {
         if (args.length < 3) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin setelo <player> <elo>"));
         } else {
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
               admin.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
            } else {
               int newElo;
               try {
                  newElo = Integer.parseInt(args[2]);
               } catch (NumberFormatException var10) {
                  admin.sendMessage(new TextComponentString("§cInvalid ELO value!"));
                  return;
               }

               newElo = Math.max(100, Math.min(5000, newElo));
               Rankeddatastorage storage = Rankeddatastorage.get(world);
               if (storage != null) {
                  Rankeddatastorage.PlayerRankedData data = storage.getPlayerData(target.getUniqueID().toString());
                  int oldElo = data.currentElo;
                  data.currentElo = newElo;
                  data.hiddenMmr = newElo;
                  if (newElo > data.peakElo) {
                     data.peakElo = newElo;
                  }

                  storage.savePlayerData(data.uuid, data);
                  admin.sendMessage(new TextComponentString("§aSet " + target.getName() + "'s ELO from " + oldElo + " to " + newElo));
                  target.sendMessage(new TextComponentString("§eYour ELO has been set to " + newElo + " by an admin."));
               }
            }
         }
      }

      private void handleBan(EntityPlayerMP admin, World world, MinecraftServer server, String[] args) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin ban <player> [minutes] [reason]"));
         } else {
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
               admin.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
            } else {
               int minutes = 60;
               if (args.length > 2) {
                  try {
                     minutes = Integer.parseInt(args[2]);
                  } catch (NumberFormatException var10) {
                  }
               }

               String reason = "Admin ban";
               if (args.length > 3) {
                  StringBuilder sb = new StringBuilder();

                  for(int i = 3; i < args.length; ++i) {
                     if (i > 3) {
                        sb.append(" ");
                     }

                     sb.append(args[i]);
                  }

                  reason = sb.toString();
               }

               Rankedpenalties.banFromRanked(target.getUniqueID().toString(), minutes, reason);
               admin.sendMessage(new TextComponentString("§aBanned " + target.getName() + " from ranked for " + minutes + " minutes."));
               target.sendMessage(new TextComponentString("§cYou have been banned from ranked! Reason: " + reason));
            }
         }
      }

      private void handleUnban(EntityPlayerMP admin, World world, MinecraftServer server, String[] args) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin unban <player>"));
         } else {
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
               admin.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
            } else {
               boolean unbanned = Rankedpenalties.unbanFromRanked(target.getUniqueID().toString());
               if (unbanned) {
                  admin.sendMessage(new TextComponentString("§aUnbanned " + target.getName() + " from ranked."));
                  target.sendMessage(new TextComponentString("§aYou have been unbanned from ranked!"));
               } else {
                  admin.sendMessage(new TextComponentString("§c" + target.getName() + " is not banned."));
               }

            }
         }
      }

      private void handleResetPlayer(EntityPlayerMP admin, World world, MinecraftServer server, String[] args) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin resetplayer <player>"));
         } else {
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
               admin.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
            } else {
               Rankeddatastorage storage = Rankeddatastorage.get(world);
               if (storage != null) {
                  storage.initializePlayer(target.getUniqueID().toString(), target.getName());
                  admin.sendMessage(new TextComponentString("§aReset ranked data for " + target.getName()));
                  target.sendMessage(new TextComponentString("§eYour ranked data has been reset by an admin."));
               }
            }
         }
      }

      private void handleForcePlace(EntityPlayerMP admin, World world, MinecraftServer server, String[] args) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin forceplace <player>"));
            admin.sendMessage(new TextComponentString("§7Forces a player to complete their placement matches."));
         } else {
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
               admin.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
            } else {
               Rankeddatastorage storage = Rankeddatastorage.get(world);
               if (storage != null) {
                  Rankeddatastorage.PlayerRankedData data = storage.getPlayerData(target.getUniqueID().toString());
                  if (data.isPlaced) {
                     admin.sendMessage(new TextComponentString("§c" + target.getName() + " has already completed their placements!"));
                  } else {
                     data.isPlaced = true;
                     data.placementGamesCompleted = 5;
                     if (data.gamesPlayed == 0) {
                        data.currentElo = 1000;
                        data.hiddenMmr = 1000;
                     }

                     storage.savePlayerData(data.uuid, data);
                     Rankedtiers.RankInfo rankInfo = Rankedtiers.eloToRank(data.currentElo);
                     admin.sendMessage(new TextComponentString("§aForced " + target.getName() + " to complete placements."));
                     admin.sendMessage(new TextComponentString("§7Placed at: " + rankInfo.color + rankInfo.fullName + " §7(" + data.currentElo + " ELO)"));
                     target.sendMessage(new TextComponentString("§aYour placements have been completed by an admin!"));
                     target.sendMessage(new TextComponentString("§7You are now ranked: " + rankInfo.color + rankInfo.fullName));
                  }
               }
            }
         }
      }

      private void handleStartSeason(EntityPlayerMP admin, World world, String[] args) {
         String seasonName = null;
         if (args.length > 1) {
            StringBuilder sb = new StringBuilder();

            for(int i = 1; i < args.length; ++i) {
               if (i > 1) {
                  sb.append(" ");
               }

               sb.append(args[i]);
            }

            seasonName = sb.toString();
         }

         Rankedseason.SeasonStartResult result = Rankedseason.startNewSeason(world, seasonName, 7L);
         admin.sendMessage(new TextComponentString("§a" + result.message));
         admin.sendMessage(new TextComponentString("§7Season Name: " + result.seasonName));
         admin.sendMessage(new TextComponentString("§7Players Reset: " + result.playersReset));
         Rankedmain.broadcastMessage(world, "§6§l===========================");
         Rankedmain.broadcastMessage(world, "§e§l  NEW RANKED SEASON!");
         Rankedmain.broadcastMessage(world, "§f  Season " + result.seasonNumber + ": " + result.seasonName);
         Rankedmain.broadcastMessage(world, "§6§l===========================");
      }

      private void handleEndSeason(EntityPlayerMP admin, World world) {
         Rankedseason.SeasonEndResult result = Rankedseason.endSeason(world);
         admin.sendMessage(new TextComponentString("§a" + result.message));
         admin.sendMessage(new TextComponentString("§7Players Archived: " + result.playersArchived));
         admin.sendMessage(new TextComponentString("§7Rewards Prepared: " + result.rewardsPrepared));
      }

      private void handleGrantTitle(EntityPlayerMP admin, World world, MinecraftServer server, String[] args) {
         if (args.length < 3) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin granttitle <player> <1|2|3> [seasonId]"));
            admin.sendMessage(new TextComponentString("§7  1 = Otsutsuki Deity (1st place)"));
            admin.sendMessage(new TextComponentString("§7  2 = Otsutsuki Prodigy (2nd place)"));
            admin.sendMessage(new TextComponentString("§7  3 = Otsutsuki (3rd place)"));
            admin.sendMessage(new TextComponentString("§7  seasonId = Beta, S1, S2, etc. (defaults to current)"));
         } else {
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
               admin.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
            } else {
               int placement;
               try {
                  placement = Integer.parseInt(args[2]);
                  if (placement < 1 || placement > 3) {
                     admin.sendMessage(new TextComponentString("§cPlacement must be 1, 2, or 3!"));
                     return;
                  }
               } catch (NumberFormatException var12) {
                  admin.sendMessage(new TextComponentString("§cInvalid placement! Use 1, 2, or 3"));
                  return;
               }

               String seasonId = Rankedseason.getSeasonId(world);
               if (args.length > 3) {
                  seasonId = args[3];
               }

               String title;
               String titleColor;
               switch (placement) {
                  case 1:
                     title = seasonId + " Otsutsuki Deity";
                     titleColor = "§6§l";
                     break;
                  case 2:
                     title = seasonId + " Otsutsuki Prodigy";
                     titleColor = "§5§l";
                     break;
                  case 3:
                  default:
                     title = seasonId + " Otsutsuki";
                     titleColor = "§c§l";
               }

               Rankeddatastorage storage = Rankeddatastorage.get(world);
               if (storage != null) {
                  Rankeddatastorage.PlayerRankedData data = storage.getPlayerData(target.getUniqueID().toString());
                  if (data.collectedTitles.contains(title)) {
                     admin.sendMessage(new TextComponentString("§c" + target.getName() + " already has this title!"));
                  } else {
                     data.collectedTitles.add(title);
                     storage.savePlayerData(data.uuid, data);
                     admin.sendMessage(new TextComponentString("§aGranted title " + titleColor + title + "§a to " + target.getName()));
                     target.sendMessage(new TextComponentString("§aYou have been granted the title: " + titleColor + title));
                     target.sendMessage(new TextComponentString("§7Open the Titles menu to equip it!"));
                  }
               }
            }
         }
      }

      private void handleRemoveTitle(EntityPlayerMP admin, World world, MinecraftServer server, String[] args) {
         if (args.length < 3) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin removetitle <player> <title>"));
            admin.sendMessage(new TextComponentString("§7Example: /rankedadmin removetitle Steve S1 Otsutsuki Deity"));
         } else {
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
               admin.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
            } else {
               StringBuilder sb = new StringBuilder();

               for(int i = 2; i < args.length; ++i) {
                  if (i > 2) {
                     sb.append(" ");
                  }

                  sb.append(args[i]);
               }

               String titleToRemove = sb.toString();
               Rankeddatastorage storage = Rankeddatastorage.get(world);
               if (storage != null) {
                  Rankeddatastorage.PlayerRankedData data = storage.getPlayerData(target.getUniqueID().toString());
                  if (!data.collectedTitles.contains(titleToRemove)) {
                     admin.sendMessage(new TextComponentString("§c" + target.getName() + " doesn't have the title: " + titleToRemove));
                  } else {
                     data.collectedTitles.remove(titleToRemove);
                     if (titleToRemove.equals(data.activeTitle)) {
                        data.activeTitle = "";
                     }

                     storage.savePlayerData(data.uuid, data);
                     admin.sendMessage(new TextComponentString("§aRemoved title '" + titleToRemove + "' from " + target.getName()));
                     target.sendMessage(new TextComponentString("§eYour title '" + titleToRemove + "' has been removed by an admin."));
                  }
               }
            }
         }
      }

      private void handleListTitles(EntityPlayerMP admin, World world, MinecraftServer server, String[] args) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin listtitles <player>"));
         } else {
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
               admin.sendMessage(new TextComponentString("§cPlayer not found: " + args[1]));
            } else {
               Rankeddatastorage storage = Rankeddatastorage.get(world);
               if (storage != null) {
                  Rankeddatastorage.PlayerRankedData data = storage.getPlayerData(target.getUniqueID().toString());
                  admin.sendMessage(new TextComponentString("§6§l===== " + target.getName() + "'s Titles ====="));
                  if (data.collectedTitles.isEmpty()) {
                     admin.sendMessage(new TextComponentString("§7No titles collected."));
                  } else {
                     for(String title : data.collectedTitles) {
                        String coloredTitle = Rankedseason.getFormattedTitle(title);
                        String activeMarker = title.equals(data.activeTitle) ? " §a(ACTIVE)" : "";
                        admin.sendMessage(new TextComponentString("  " + coloredTitle + activeMarker));
                     }
                  }

                  admin.sendMessage(new TextComponentString("§7Total: " + data.collectedTitles.size() + " titles"));
                  admin.sendMessage(new TextComponentString("§6§l============================="));
               }
            }
         }
      }

      private void handleSeasonInfo(EntityPlayerMP admin, World world) {
         Rankedseason.SeasonInfo info = Rankedseason.getSeasonInfo(world);
         admin.sendMessage(new TextComponentString("§6§l===== SEASON CONFIG ====="));
         admin.sendMessage(new TextComponentString("§7Season ID: §e" + info.seasonId + " §8(internal #" + info.seasonNumber + ")"));
         admin.sendMessage(new TextComponentString("§7Season Name: §f" + info.seasonName));
         admin.sendMessage(new TextComponentString("§7Status: " + (info.isActive ? "§aActive" : "§cInactive/Paused")));
         admin.sendMessage(new TextComponentString("§7Start Date: §f" + Rankedseason.getFormattedStartDate(world)));
         admin.sendMessage(new TextComponentString("§7End Date: §f" + Rankedseason.getFormattedEndDate(world)));
         admin.sendMessage(new TextComponentString("§7Time Remaining: §f" + info.timeRemaining));
         admin.sendMessage(new TextComponentString("§7Auto-Transition: " + (info.autoTransition ? "§aON" : "§cOFF")));
         admin.sendMessage(new TextComponentString("§7Total Matches: §f" + info.totalMatches));
         admin.sendMessage(new TextComponentString("§6§l========================="));
      }

      private void handleSetSeasonName(EntityPlayerMP admin, World world, String[] args) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin setseasonname <name>"));
         } else {
            StringBuilder sb = new StringBuilder();

            for(int i = 1; i < args.length; ++i) {
               if (i > 1) {
                  sb.append(" ");
               }

               sb.append(args[i]);
            }

            String newName = sb.toString();
            String oldName = Rankedseason.getSeasonName(world);
            Rankedseason.setSeasonName(world, newName);
            admin.sendMessage(new TextComponentString("§aSeason name changed from '" + oldName + "' to '" + newName + "'"));
         }
      }

      private void handleSetSeasonEnd(EntityPlayerMP admin, World world, String[] args) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin setseasonend <MM/DD/YYYY> [HH:MM]"));
            admin.sendMessage(new TextComponentString("§7Example: /rankedadmin setseasonend 03/15/2026 18:00"));
         } else {
            String dateStr = args[1];
            String timeStr = args.length > 2 ? args[2] : "00:00";
            String[] dateParts = dateStr.split("/");
            if (dateParts.length != 3) {
               admin.sendMessage(new TextComponentString("§cInvalid date format! Use MM/DD/YYYY"));
            } else {
               String[] timeParts = timeStr.split(":");
               if (timeParts.length != 2) {
                  admin.sendMessage(new TextComponentString("§cInvalid time format! Use HH:MM (24-hour)"));
               } else {
                  try {
                     int month = Integer.parseInt(dateParts[0]);
                     int day = Integer.parseInt(dateParts[1]);
                     int year = Integer.parseInt(dateParts[2]);
                     int hour = Integer.parseInt(timeParts[0]);
                     int minute = Integer.parseInt(timeParts[1]);
                     if (month < 1 || month > 12) {
                        admin.sendMessage(new TextComponentString("§cMonth must be between 1 and 12!"));
                        return;
                     }

                     if (day < 1 || day > 31) {
                        admin.sendMessage(new TextComponentString("§cDay must be between 1 and 31!"));
                        return;
                     }

                     if (hour < 0 || hour > 23) {
                        admin.sendMessage(new TextComponentString("§cHour must be between 0 and 23!"));
                        return;
                     }

                     if (minute < 0 || minute > 59) {
                        admin.sendMessage(new TextComponentString("§cMinute must be between 0 and 59!"));
                        return;
                     }

                     boolean success = Rankedseason.setSeasonEndDate(world, month, day, year, hour, minute);
                     if (success) {
                        admin.sendMessage(new TextComponentString("§aSeason end date set to: " + Rankedseason.getFormattedEndDate(world)));
                     } else {
                        admin.sendMessage(new TextComponentString("§cFailed to set date! Date must be in the future."));
                     }
                  } catch (NumberFormatException var14) {
                     admin.sendMessage(new TextComponentString("§cInvalid number format in date/time!"));
                  }

               }
            }
         }
      }

      private void handleSetSeasonDays(EntityPlayerMP admin, World world, String[] args) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin setseasondays <days>"));
            admin.sendMessage(new TextComponentString("§7Sets the season to end X days from now."));
         } else {
            try {
               int days = Integer.parseInt(args[1]);
               if (days < 1) {
                  admin.sendMessage(new TextComponentString("§cDays must be at least 1!"));
                  return;
               }

               if (days > 365) {
                  admin.sendMessage(new TextComponentString("§cDays cannot exceed 365!"));
                  return;
               }

               Rankedseason.setSeasonDuration(world, days);
               admin.sendMessage(new TextComponentString("§aSeason will now end in " + days + " days."));
               admin.sendMessage(new TextComponentString("§7New end date: " + Rankedseason.getFormattedEndDate(world)));
            } catch (NumberFormatException var5) {
               admin.sendMessage(new TextComponentString("§cInvalid number!"));
            }

         }
      }

      private void handlePauseSeason(EntityPlayerMP admin, World world) {
         Rankedseason.setSeasonActive(world, false);
         admin.sendMessage(new TextComponentString("§eSeason has been paused. Players cannot queue for ranked matches."));
         Rankedmain.broadcastMessage(world, "§e§l[Ranked] §cSeason has been paused by an administrator.");
      }

      private void handleResumeSeason(EntityPlayerMP admin, World world) {
         Rankedseason.setSeasonActive(world, true);
         admin.sendMessage(new TextComponentString("§aSeason has been resumed. Players can now queue for ranked matches."));
         Rankedmain.broadcastMessage(world, "§e§l[Ranked] §aSeason has been resumed!");
      }

      private void handleSetSeasonId(EntityPlayerMP admin, World world, String[] args) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin setseasonid <id>"));
            admin.sendMessage(new TextComponentString("§7Examples: Beta, S1, S2, Preseason"));
            admin.sendMessage(new TextComponentString("§7This ID is used for titles/trophies."));
         } else {
            String newId = args[1];
            String oldId = Rankedseason.getSeasonId(world);
            Rankedseason.setSeasonId(world, newId);
            admin.sendMessage(new TextComponentString("§aSeason ID changed from '" + oldId + "' to '" + newId + "'"));
            admin.sendMessage(new TextComponentString("§7Titles will now use: " + newId + " Otsutsuki Deity, etc."));
         }
      }

      private void handleAutoTransition(EntityPlayerMP admin, World world, String[] args) {
         if (args.length < 2) {
            boolean current = Rankedseason.isAutoTransitionEnabled(world);
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin autotransition <on|off>"));
            admin.sendMessage(new TextComponentString("§7Current: " + (current ? "§aON" : "§cOFF")));
            admin.sendMessage(new TextComponentString("§7When ON, next season starts automatically when current ends."));
         } else {
            String value = args[1].toLowerCase();
            boolean enable;
            if (!value.equals("on") && !value.equals("true") && !value.equals("1")) {
               if (!value.equals("off") && !value.equals("false") && !value.equals("0")) {
                  admin.sendMessage(new TextComponentString("§cInvalid value! Use 'on' or 'off'"));
                  return;
               }

               enable = false;
            } else {
               enable = true;
            }

            Rankedseason.setAutoTransition(world, enable);
            admin.sendMessage(new TextComponentString("§aAuto-transition " + (enable ? "enabled" : "disabled") + "."));
            if (enable) {
               admin.sendMessage(new TextComponentString("§7Seasons will automatically transition when they end."));
            } else {
               admin.sendMessage(new TextComponentString("§7An admin must manually start new seasons."));
            }

         }
      }

      private void handleListArenas(EntityPlayerMP admin, World world) {
         Map<Integer, Rankedarena.Arena> arenas = Rankedarena.getAllArenas(world);
         admin.sendMessage(new TextComponentString("§6§l===== ARENAS ====="));
         if (arenas.isEmpty()) {
            admin.sendMessage(new TextComponentString("§7No arenas configured."));
         } else {
            for(Rankedarena.Arena arena : arenas.values()) {
               String status = arena.enabled ? "§a[ON]" : "§c[OFF]";
               admin.sendMessage(new TextComponentString(status + " §f#" + arena.id + " " + arena.color + arena.name));
            }
         }

         BlockPos hub = Rankedarena.getHubLocation(world);
         admin.sendMessage(new TextComponentString("§7Hub: §f" + hub.getX() + ", " + hub.getY() + ", " + hub.getZ()));
         admin.sendMessage(new TextComponentString("§6§l=================="));
      }

      private void handleArenaInfo(EntityPlayerMP admin, World world, String[] args) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin arenainfo <id>"));
         } else {
            int arenaId;
            try {
               arenaId = Integer.parseInt(args[1]);
            } catch (NumberFormatException var6) {
               admin.sendMessage(new TextComponentString("§cInvalid arena ID!"));
               return;
            }

            Rankedarena.Arena arena = Rankedarena.getArena(world, arenaId);
            if (arena == null) {
               admin.sendMessage(new TextComponentString("§cArena not found: #" + arenaId));
            } else {
               admin.sendMessage(new TextComponentString("§6§l===== ARENA #" + arenaId + " ====="));
               admin.sendMessage(new TextComponentString("§7Name: " + arena.color + arena.name));
               admin.sendMessage(new TextComponentString("§7Status: " + (arena.enabled ? "§aEnabled" : "§cDisabled")));
               admin.sendMessage(new TextComponentString("§7Description: §f" + arena.description));
               admin.sendMessage(new TextComponentString("§7Spawn 1: §f" + arena.player1Spawn.getX() + ", " + arena.player1Spawn.getY() + ", " + arena.player1Spawn.getZ()));
               admin.sendMessage(new TextComponentString("§7Spawn 2: §f" + arena.player2Spawn.getX() + ", " + arena.player2Spawn.getY() + ", " + arena.player2Spawn.getZ()));
               admin.sendMessage(new TextComponentString("§7Center: §f" + arena.centerPoint.getX() + ", " + arena.centerPoint.getY() + ", " + arena.centerPoint.getZ()));
               admin.sendMessage(new TextComponentString("§7Ring Out: " + (arena.hasRingOut ? "§aY < " + arena.ringOutY : "§cOFF")));
               admin.sendMessage(new TextComponentString("§6§l======================"));
            }
         }
      }

      private void handleAddArena(EntityPlayerMP admin, World world, String[] args) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin addarena <name>"));
            admin.sendMessage(new TextComponentString("§7Creates arena at your current position (spawn 1)."));
            admin.sendMessage(new TextComponentString("§7Use setarenasp2 to set spawn 2 position."));
         } else {
            StringBuilder sb = new StringBuilder();

            for(int i = 1; i < args.length; ++i) {
               if (i > 1) {
                  sb.append(" ");
               }

               sb.append(args[i]);
            }

            String name = sb.toString();
            BlockPos pos = admin.getPosition();
            BlockPos spawn2 = new BlockPos(pos.getX() + 20, pos.getY(), pos.getZ());
            int newId = Rankedarena.addArena(world, name, pos, spawn2);
            if (newId > 0) {
               admin.sendMessage(new TextComponentString("§aCreated arena #" + newId + ": " + name));
               admin.sendMessage(new TextComponentString("§7Spawn 1 set to your position."));
               admin.sendMessage(new TextComponentString("§7Use §e/rankedadmin setarenasp2 " + newId + " §7to set spawn 2."));
            } else {
               admin.sendMessage(new TextComponentString("§cFailed to create arena!"));
            }

         }
      }

      private void handleRemoveArena(EntityPlayerMP admin, World world, String[] args) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin removearena <id>"));
         } else {
            int arenaId;
            try {
               arenaId = Integer.parseInt(args[1]);
            } catch (NumberFormatException var6) {
               admin.sendMessage(new TextComponentString("§cInvalid arena ID!"));
               return;
            }

            boolean removed = Rankedarena.removeArena(world, arenaId);
            if (removed) {
               admin.sendMessage(new TextComponentString("§aRemoved arena #" + arenaId));
            } else {
               admin.sendMessage(new TextComponentString("§cArena not found: #" + arenaId));
            }

         }
      }

      private void handleSetArenaSpawn(EntityPlayerMP admin, World world, String[] args, boolean isSpawn1) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin " + args[0] + " <arena_id>"));
            admin.sendMessage(new TextComponentString("§7Sets spawn to your current position."));
         } else {
            int arenaId;
            try {
               arenaId = Integer.parseInt(args[1]);
            } catch (NumberFormatException var9) {
               admin.sendMessage(new TextComponentString("§cInvalid arena ID!"));
               return;
            }

            BlockPos pos = admin.getPosition();
            boolean success;
            if (isSpawn1) {
               success = Rankedarena.setArenaSpawn1(world, arenaId, pos);
            } else {
               success = Rankedarena.setArenaSpawn2(world, arenaId, pos);
            }

            if (success) {
               String spawnName = isSpawn1 ? "Spawn 1" : "Spawn 2";
               admin.sendMessage(new TextComponentString("§aSet " + spawnName + " for arena #" + arenaId + " to: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
            } else {
               admin.sendMessage(new TextComponentString("§cArena not found: #" + arenaId));
            }

         }
      }

      private void handleSetArenaName(EntityPlayerMP admin, World world, String[] args) {
         if (args.length < 3) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin setarenaname <id> <name>"));
         } else {
            int arenaId;
            try {
               arenaId = Integer.parseInt(args[1]);
            } catch (NumberFormatException var8) {
               admin.sendMessage(new TextComponentString("§cInvalid arena ID!"));
               return;
            }

            StringBuilder sb = new StringBuilder();

            for(int i = 2; i < args.length; ++i) {
               if (i > 2) {
                  sb.append(" ");
               }

               sb.append(args[i]);
            }

            String name = sb.toString();
            boolean success = Rankedarena.setArenaName(world, arenaId, name);
            if (success) {
               admin.sendMessage(new TextComponentString("§aRenamed arena #" + arenaId + " to: " + name));
            } else {
               admin.sendMessage(new TextComponentString("§cArena not found: #" + arenaId));
            }

         }
      }

      private void handleEnableArena(EntityPlayerMP admin, World world, String[] args, boolean enable) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin " + args[0] + " <id>"));
         } else {
            int arenaId;
            try {
               arenaId = Integer.parseInt(args[1]);
            } catch (NumberFormatException var7) {
               admin.sendMessage(new TextComponentString("§cInvalid arena ID!"));
               return;
            }

            boolean success = Rankedarena.setArenaEnabled(world, arenaId, enable);
            if (success) {
               if (enable) {
                  admin.sendMessage(new TextComponentString("§aEnabled arena #" + arenaId));
               } else {
                  admin.sendMessage(new TextComponentString("§cDisabled arena #" + arenaId));
               }
            } else {
               admin.sendMessage(new TextComponentString("§cArena not found: #" + arenaId));
            }

         }
      }

      private void handleSetHub(EntityPlayerMP admin, World world) {
         BlockPos pos = admin.getPosition();
         Rankedarena.setHubLocation(world, pos);
         admin.sendMessage(new TextComponentString("§aSet hub location to: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
         admin.sendMessage(new TextComponentString("§7Players will return here after matches."));
      }

      private void handleSetRingOut(EntityPlayerMP admin, World world, String[] args) {
         if (args.length < 3) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin setringout <arena_id> <y_level>"));
            admin.sendMessage(new TextComponentString("§7Players below Y level will lose the match."));
         } else {
            int arenaId;
            try {
               arenaId = Integer.parseInt(args[1]);
            } catch (NumberFormatException var8) {
               admin.sendMessage(new TextComponentString("§cInvalid arena ID!"));
               return;
            }

            int ringOutY;
            try {
               ringOutY = Integer.parseInt(args[2]);
            } catch (NumberFormatException var7) {
               admin.sendMessage(new TextComponentString("§cInvalid Y level!"));
               return;
            }

            boolean success = Rankedarena.setArenaRingOut(world, arenaId, true, ringOutY);
            if (success) {
               admin.sendMessage(new TextComponentString("§aEnabled ring out for arena #" + arenaId + " at Y < " + ringOutY));
               admin.sendMessage(new TextComponentString("§7Players falling below Y=" + ringOutY + " will lose."));
            } else {
               admin.sendMessage(new TextComponentString("§cArena not found: #" + arenaId));
            }

         }
      }

      private void handleDisableRingOut(EntityPlayerMP admin, World world, String[] args) {
         if (args.length < 2) {
            admin.sendMessage(new TextComponentString("§cUsage: /rankedadmin disableringout <arena_id>"));
         } else {
            int arenaId;
            try {
               arenaId = Integer.parseInt(args[1]);
            } catch (NumberFormatException var6) {
               admin.sendMessage(new TextComponentString("§cInvalid arena ID!"));
               return;
            }

            boolean success = Rankedarena.setArenaRingOut(world, arenaId, false, 0);
            if (success) {
               admin.sendMessage(new TextComponentString("§aDisabled ring out for arena #" + arenaId));
            } else {
               admin.sendMessage(new TextComponentString("§cArena not found: #" + arenaId));
            }

         }
      }

      public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
         List<String> completions = new ArrayList();
         if (args.length == 1) {
            completions.add("setelo");
            completions.add("ban");
            completions.add("unban");
            completions.add("resetplayer");
            completions.add("forceplace");
            completions.add("startseason");
            completions.add("endseason");
            completions.add("seasoninfo");
            completions.add("setseasonid");
            completions.add("setseasonname");
            completions.add("setseasonend");
            completions.add("setseasondays");
            completions.add("pauseseason");
            completions.add("resumeseason");
            completions.add("autotransition");
            completions.add("granttitle");
            completions.add("removetitle");
            completions.add("listtitles");
            completions.add("listarenas");
            completions.add("arenainfo");
            completions.add("addarena");
            completions.add("removearena");
            completions.add("setarenasp1");
            completions.add("setarenasp2");
            completions.add("setarenaname");
            completions.add("enablearena");
            completions.add("disablearena");
            completions.add("setringout");
            completions.add("disableringout");
            completions.add("sethub");
         } else if (args.length == 2) {
            String cmd = args[0].toLowerCase();
            if (!cmd.equals("setelo") && !cmd.equals("ban") && !cmd.equals("unban") && !cmd.equals("resetplayer") && !cmd.equals("forceplace") && !cmd.equals("granttitle") && !cmd.equals("removetitle") && !cmd.equals("listtitles")) {
               if (cmd.equals("autotransition")) {
                  completions.add("on");
                  completions.add("off");
               } else if (cmd.equals("setseasonid")) {
                  completions.add("Beta");
                  completions.add("S1");
                  completions.add("S2");
                  completions.add("Preseason");
               } else if (cmd.equals("arenainfo") || cmd.equals("removearena") || cmd.equals("setarenasp1") || cmd.equals("setarenasp2") || cmd.equals("setarenaname") || cmd.equals("enablearena") || cmd.equals("disablearena")) {
                  completions.add("1");
                  completions.add("2");
                  completions.add("3");
               }
            } else {
               for(EntityPlayerMP p : server.getPlayerList().getPlayers()) {
                  completions.add(p.getName());
               }
            }
         } else if (args.length == 3 && args[0].equalsIgnoreCase("granttitle")) {
            completions.add("1");
            completions.add("2");
            completions.add("3");
         }

         return getListOfStringsMatchingLastWord(args, completions);
      }
   }

   public static class CommandClaimRewards extends CommandBase {
      public String getName() {
         return "claimrewards";
      }

      public String getUsage(ICommandSender sender) {
         return "/claimrewards";
      }

      public int getRequiredPermissionLevel() {
         return 0;
      }

      public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
         return true;
      }

      public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
         if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            player.sendMessage(new TextComponentString("§eSeason rewards must be claimed through the Ranked Menu."));
            player.sendMessage(new TextComponentString("§7Open the §6Ranked Menu §7and go to the §6Season §7tab to claim your rewards."));
         }
      }
   }
}
