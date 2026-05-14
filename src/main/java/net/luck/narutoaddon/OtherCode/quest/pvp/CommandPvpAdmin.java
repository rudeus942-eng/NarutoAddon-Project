
package net.luck.narutoaddon.OtherCode.quest.pvp;

import com.mojang.authlib.GameProfile;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.network.PvpNetworkHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.*;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandPvpAdmin extends CommandBase {
   private static final String PREFIX = "§6[PvP] §r";
   private static final String ERR = "§c";
   private static final String[] SUBCOMMANDS = new String[]{"setkage", "removekage", "listkages", "setadvisor", "removeadvisor", "listadvisors", "autosync", "setpvprank", "addpvpxp", "setbattlefield", "setspawn1", "setspawn2", "setspawnhere1", "setspawnhere2", "endwar", "forcewar", "listwars", "exitwar", "refreshbingobook", "setbingo", "removebingo", "clearallbingo", "listbingobook", "togglepvp", "resetpvp", "warcooldown", "fixgamemode", "testdivision", "winwar", "villagedetect"};
   private static final String[] VILLAGE_NAMES = new String[]{"Leaf", "Sand", "Mist", "Stone", "Cloud", "Rain"};
   private static final String[] RANK_NAMES = new String[]{"D", "C", "B", "A", "S", "S+"};
   private static final String[] WAR_MODES = new String[]{"SKIRMISH", "DIVISION", "DOMINATION", "RUSH", "FOREST_OF_DEATH", "INFILTRATION"};

   public String getName() {
      return "pvpadmin";
   }

   public String getUsage(ICommandSender sender) {
      return "/pvpadmin <subcommand> [args...]";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length == 0) {
         this.sendHelp(sender);
      } else {
         switch (args[0].toLowerCase()) {
            case "setkage":
               this.handleSetKage(server, sender, args);
               break;
            case "removekage":
               this.handleRemoveKage(sender, args);
               break;
            case "listkages":
               this.handleListKages(server, sender);
               break;
            case "setadvisor":
               this.handleSetAdvisor(server, sender, args);
               break;
            case "removeadvisor":
               this.handleRemoveAdvisor(server, sender, args);
               break;
            case "listadvisors":
               this.handleListAdvisors(server, sender);
               break;
            case "autosync":
               this.handleAutoSync(server, sender);
               break;
            case "setpvprank":
               this.handleSetPvpRank(server, sender, args);
               break;
            case "addpvpxp":
               this.handleAddPvpXp(server, sender, args);
               break;
            case "setbattlefield":
               this.handleSetBattlefield(server, sender, args);
               break;
            case "setspawn1":
               this.handleSetSpawn(server, sender, args, 1);
               break;
            case "setspawn2":
               this.handleSetSpawn(server, sender, args, 2);
               break;
            case "setspawnhere1":
               this.handleSetSpawnHere(server, sender, 1);
               break;
            case "setspawnhere2":
               this.handleSetSpawnHere(server, sender, 2);
               break;
            case "endwar":
               this.handleEndWar(sender, args);
               break;
            case "exitwar":
               this.handleExitWar(server, sender, args);
               break;
            case "forcewar":
               this.handleForceWar(server, sender, args);
               break;
            case "listwars":
               this.handleListWars(sender);
               break;
            case "refreshbingobook":
               this.handleRefreshBingoBook(server, sender);
               break;
            case "setbingo":
               this.handleSetBingo(server, sender, args);
               break;
            case "removebingo":
               this.handleRemoveBingo(server, sender, args);
               break;
            case "clearallbingo":
               this.handleClearAllBingo(server, sender);
               break;
            case "listbingobook":
               this.handleListBingoBook(sender);
               break;
            case "togglepvp":
               this.handleTogglePvp(sender);
               break;
            case "resetpvp":
               this.handleResetPvp(server, sender, args);
               break;
            case "warcooldown":
               this.handleWarCooldown(server, sender, args);
               break;
            case "fixgamemode":
               this.handleFixGamemode(server, sender, args);
               break;
            case "testdivision":
               this.handleTestDivision(server, sender, args);
               break;
            case "winwar":
               this.handleWinWar(server, sender);
               break;
            case "villagedetect":
               this.handleVillageDetect(server, sender, args);
               break;
            default:
               this.sendHelp(sender);
         }

      }
   }

   private void handleSetKage(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin setkage <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         VillageHelper.Village village = VillageHelper.getVillage(target);
         if (village == VillageHelper.Village.UNKNOWN) {
            sender.sendMessage(new TextComponentString("§c" + target.getName() + " is not on any village team."));
         } else {
            boolean success = KageManager.getInstance().setKage(village, target);
            if (success) {
               PvpSavedData.get(server.getWorld(0)).markDirty();
               sender.sendMessage(new TextComponentString("§6[PvP] §r" + target.getName() + " is now the Kage of " + village.villageName + "."));
            } else {
               sender.sendMessage(new TextComponentString("§cFailed to set Kage. Player may not be on the correct team."));
            }

         }
      }
   }

   private void handleRemoveKage(ICommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin removekage <village>"));
      } else {
         VillageHelper.Village village = parseVillage(args[1]);
         if (village == null) {
            sender.sendMessage(new TextComponentString("§cUnknown village: " + args[1] + ". Valid: Leaf, Sand, Mist, Stone, Cloud, Rain, Akatsuki"));
         } else {
            KageManager.getInstance().removeKage(village);
            PvpSavedData.get(sender.getEntityWorld()).markDirty();
            sender.sendMessage(new TextComponentString("§6[PvP] §rKage removed from " + village.villageName + "."));
         }
      }
   }

   private void handleListKages(MinecraftServer server, ICommandSender sender) {
      Map<String, UUID> kages = KageManager.getInstance().getAllKages();
      if (kages.isEmpty()) {
         sender.sendMessage(new TextComponentString("§6[PvP] §rNo Kages assigned."));
      } else {
         sender.sendMessage(new TextComponentString("§6[PvP] §r§l--- Kage Assignments ---"));

         for(Map.Entry<String, UUID> entry : kages.entrySet()) {
            String playerName = resolvePlayerName(server, (UUID)entry.getValue());
            sender.sendMessage(new TextComponentString("  §e" + (String)entry.getKey() + "§r: " + playerName + " §7(" + ((UUID)entry.getValue()).toString().substring(0, 8) + "...)"));
         }

      }
   }

   private void handleSetAdvisor(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin setadvisor <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         VillageHelper.Village village = VillageHelper.getVillage(target);
         if (village == VillageHelper.Village.UNKNOWN) {
            sender.sendMessage(new TextComponentString("§c" + target.getName() + " is not on any village team."));
         } else {
            boolean success = AdvisorManager.getInstance().addAdvisor(village, target);
            if (success) {
               PvpSavedData.get(server.getWorld(0)).markDirty();
               sender.sendMessage(new TextComponentString("§6[PvP] §r" + target.getName() + " is now an Advisor of " + village.villageName + "."));
            } else {
               sender.sendMessage(new TextComponentString("§cVillage already has 3 advisors."));
            }

         }
      }
   }

   private void handleRemoveAdvisor(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin removeadvisor <player> [village]"));
      } else {
         String targetName = args[1];
         EntityPlayerMP onlineTarget = server.getPlayerList().getPlayerByUsername(targetName);
         UUID targetUUID;
         if (onlineTarget != null) {
            targetUUID = onlineTarget.getUniqueID();
         } else {
            GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(targetName);
            if (profile == null) {
               sender.sendMessage(new TextComponentString("§cPlayer not found: " + targetName));
               return;
            }

            targetUUID = profile.getId();
         }

         VillageHelper.Village village;
         if (args.length >= 3) {
            village = parseVillage(args[2]);
            if (village == null) {
               sender.sendMessage(new TextComponentString("§cUnknown village: " + args[2] + ". Valid: Leaf, Sand, Mist, Stone, Cloud, Rain, Akatsuki"));
               return;
            }
         } else {
            if (onlineTarget == null) {
               sender.sendMessage(new TextComponentString("§c" + targetName + " is offline. Specify village: /pvpadmin removeadvisor " + targetName + " <village>"));
               return;
            }

            village = VillageHelper.getVillage(onlineTarget);
            if (village == VillageHelper.Village.UNKNOWN) {
               sender.sendMessage(new TextComponentString("§c" + targetName + " is not on any village team. Specify village: /pvpadmin removeadvisor " + targetName + " <village>"));
               return;
            }
         }

         boolean removed = AdvisorManager.getInstance().removeAdvisor(village, targetUUID);
         if (removed) {
            PvpSavedData.get(server.getWorld(0)).markDirty();
            sender.sendMessage(new TextComponentString("§6[PvP] §r" + targetName + " removed as Advisor of " + village.villageName + "."));
         } else {
            sender.sendMessage(new TextComponentString("§c" + targetName + " is not an advisor of " + village.villageName + "."));
         }

      }
   }

   private void handleListAdvisors(MinecraftServer server, ICommandSender sender) {
      boolean any = false;
      sender.sendMessage(new TextComponentString("§6[PvP] §r§l--- Advisor Assignments ---"));

      for(VillageHelper.Village v : VillageHelper.Village.values()) {
         if (v != VillageHelper.Village.UNKNOWN) {
            List<UUID> advisors = AdvisorManager.getInstance().getAdvisors(v);
            if (!advisors.isEmpty()) {
               any = true;
               StringBuilder sb = new StringBuilder("  §e" + v.villageName + "§r: ");

               for(int i = 0; i < advisors.size(); ++i) {
                  if (i > 0) {
                     sb.append(", ");
                  }

                  sb.append(resolvePlayerName(server, (UUID)advisors.get(i)));
               }

               sender.sendMessage(new TextComponentString(sb.toString()));
            }
         }
      }

      if (!any) {
         sender.sendMessage(new TextComponentString("§6[PvP] §rNo advisors assigned."));
      }

   }

   private void handleAutoSync(MinecraftServer server, ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§6[PvP] §rScanning LuckPerms groups for kage/advisor roles..."));
      String result = LuckPermsSync.syncAllOnlinePlayers(server);
      sender.sendMessage(new TextComponentString("§6[PvP] §r" + result));
   }

   private void handleSetPvpRank(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin setpvprank <player> <D|C|B|A|S|S+>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         QuestDefinition.QuestRank rank = parseRank(args[2]);
         if (rank == null) {
            sender.sendMessage(new TextComponentString("§cInvalid rank: " + args[2] + ". Valid: D, C, B, A, S, S+"));
         } else {
            PvpSavedData data = PvpSavedData.get(target.getServerWorld());
            data.setPvpRank(target.getUniqueID(), rank);
            sender.sendMessage(new TextComponentString("§6[PvP] §r" + target.getName() + "'s PvP rank set to " + rank.displayName + "."));
         }
      }
   }

   private void handleAddPvpXp(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin addpvpxp <player> <amount>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);

         long amount;
         try {
            amount = Long.parseLong(args[2]);
         } catch (NumberFormatException var10) {
            sender.sendMessage(new TextComponentString("§cInvalid amount: " + args[2]));
            return;
         }

         PvpSavedData data = PvpSavedData.get(target.getServerWorld());
         boolean rankedUp = data.addPvpXp(target.getUniqueID(), amount);
         QuestDefinition.QuestRank currentRank = data.getPvpRank(target.getUniqueID());
         sender.sendMessage(new TextComponentString("§6[PvP] §rAdded " + amount + " PvP XP to " + target.getName() + ". Total: " + data.getPvpXp(target.getUniqueID()) + " (Rank: " + currentRank.displayName + ")" + (rankedUp ? " §a§lRANK UP!" : "")));
      }
   }

   private void handleSetBattlefield(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 4) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin setbattlefield <x> <y> <z>"));
      } else {
         int x;
         int y;
         int z;
         try {
            x = Integer.parseInt(args[1]);
            y = Integer.parseInt(args[2]);
            z = Integer.parseInt(args[3]);
         } catch (NumberFormatException var9) {
            sender.sendMessage(new TextComponentString("§cInvalid coordinates."));
            return;
         }

         BlockPos pos = new BlockPos(x, y, z);
         PvpSavedData data = PvpSavedData.get(server.worlds[0]);
         data.addBattlefieldPosition(pos);
         sender.sendMessage(new TextComponentString("§6[PvP] §rBattlefield center set to " + x + ", " + y + ", " + z + "."));
      }
   }

   private void handleSetSpawn(MinecraftServer server, ICommandSender sender, String[] args, int team) throws CommandException {
      if (args.length < 4) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin setspawn" + team + " <x> <y> <z>"));
      } else {
         int x;
         int y;
         int z;
         try {
            x = Integer.parseInt(args[1]);
            y = Integer.parseInt(args[2]);
            z = Integer.parseInt(args[3]);
         } catch (NumberFormatException var9) {
            sender.sendMessage(new TextComponentString("§cInvalid coordinates."));
            return;
         }

         this.applyWarSpawn(server, sender, team, new BlockPos(x, y, z));
      }
   }

   private void handleSetSpawnHere(MinecraftServer server, ICommandSender sender, int team) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         sender.sendMessage(new TextComponentString("§cThis command must be run by a player in-game."));
      } else {
         EntityPlayerMP p = (EntityPlayerMP)sender;
         BlockPos pos = new BlockPos(p.posX, p.posY, p.posZ);
         this.applyWarSpawn(server, sender, team, pos);
      }
   }

   private void applyWarSpawn(MinecraftServer server, ICommandSender sender, int team, BlockPos pos) {
      PvpSavedData data = PvpSavedData.get(server.worlds[0]);
      if (team == 1) {
         data.setWarTeam1Spawn(pos);
      } else {
         data.setWarTeam2Spawn(pos);
      }

      sender.sendMessage(new TextComponentString("§6[PvP] §rTeam " + team + " spawn point set to " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " (persisted)."));
   }

   private void handleExitWar(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin exitwar <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         WarBattlefield.forceCleanup(target);
         sender.sendMessage(new TextComponentString("§6[PvP] §rForce-cleaned war state for " + target.getName() + "."));
      }
   }

   private void handleEndWar(ICommandSender sender, String[] args) {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin endwar <village1> <village2>"));
      } else {
         VillageHelper.Village v1 = parseVillage(args[1]);
         VillageHelper.Village v2 = parseVillage(args[2]);
         if (v1 != null && v2 != null) {
            WarInstance war = WarManager.getInstance().findWarBetween(v1, v2);
            if (war == null) {
               sender.sendMessage(new TextComponentString("§cNo active war between " + v1.villageName + " and " + v2.villageName + "."));
            } else {
               WarManager.getInstance().endWar(war, true);
               sender.sendMessage(new TextComponentString("§6[PvP] §rWar between " + v1.villageName + " and " + v2.villageName + " force-ended."));
            }
         } else {
            sender.sendMessage(new TextComponentString("§cUnknown village. Valid: Leaf, Sand, Mist, Stone, Cloud, Rain, Akatsuki"));
         }
      }
   }

   private void handleForceWar(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 4) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin forcewar <village1> <village2> <mode>"));
      } else {
         VillageHelper.Village v1 = parseVillage(args[1]);
         VillageHelper.Village v2 = parseVillage(args[2]);
         if (v1 != null && v2 != null) {
            if (v1 == v2) {
               sender.sendMessage(new TextComponentString("§cCannot start a war between the same village."));
            } else {
               WarMode mode = parseWarMode(args[3]);
               if (mode == null) {
                  sender.sendMessage(new TextComponentString("§cInvalid mode: " + args[3] + ". Valid: " + String.join(", ", WAR_MODES)));
               } else {
                  UUID adminUUID = new UUID(0L, 0L);
                  WarInstance war = WarManager.getInstance().declareWar(adminUUID, v1, v2, mode, server.worlds[0]);
                  if (war != null) {
                     sender.sendMessage(new TextComponentString("§6[PvP] §rWar started: " + v1.villageName + " vs " + v2.villageName + " (" + mode.displayName + "). ID: " + war.getWarId()));
                  } else {
                     sender.sendMessage(new TextComponentString("§cFailed to start war. Possible conflict or cooldown."));
                  }

               }
            }
         } else {
            sender.sendMessage(new TextComponentString("§cUnknown village. Valid: Leaf, Sand, Mist, Stone, Cloud, Rain, Akatsuki"));
         }
      }
   }

   private void handleTestDivision(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         sender.sendMessage(new TextComponentString("§cMust be run by a player."));
      } else if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin testdivision <village>"));
      } else {
         EntityPlayerMP admin = (EntityPlayerMP)sender;
         VillageHelper.Village targetVillage = parseVillage(args[1]);
         if (targetVillage == null) {
            sender.sendMessage(new TextComponentString("§cUnknown village. Valid: Leaf, Sand, Mist, Stone, Cloud, Rain, Akatsuki"));
         } else {
            VillageHelper.Village adminVillage = VillageHelper.getVillage(admin);
            if (adminVillage == VillageHelper.Village.UNKNOWN) {
               adminVillage = VillageHelper.Village.LEAF;
            }

            if (adminVillage == targetVillage) {
               sender.sendMessage(new TextComponentString("§cCannot test against your own village."));
            } else {
               EntityPlayerMP opponent = null;

               for(EntityPlayerMP p : server.getPlayerList().getPlayers()) {
                  if (p != admin && VillageHelper.getVillage(p) == targetVillage) {
                     opponent = p;
                     break;
                  }
               }

               if (opponent == null) {
                  sender.sendMessage(new TextComponentString("§cNo online players found in " + targetVillage.villageName + "."));
               } else if (!WarBattlefield.reserveArena("test_division_" + System.currentTimeMillis())) {
                  sender.sendMessage(new TextComponentString("§cArena already in use."));
               } else {
                  List<UUID> team1 = Collections.singletonList(admin.getUniqueID());
                  List<UUID> team2 = Collections.singletonList(opponent.getUniqueID());
                  WarBattlefield.teleportTeams(team1, team2, server.worlds[0]);
                  admin.getEntityData().setInteger("pvpWarOrigGamemode", admin.interactionManager.getGameType().getID());
                  admin.setGameType(GameType.ADVENTURE);
                  opponent.getEntityData().setInteger("pvpWarOrigGamemode", opponent.interactionManager.getGameType().getID());
                  opponent.setGameType(GameType.ADVENTURE);
                  sender.sendMessage(new TextComponentString("§6[PvP] §rTest division started! You vs " + opponent.getName() + ". Use /pvpadmin winwar to end and test gamemode restore."));
                  opponent.sendMessage(new TextComponentString("§6[PvP] §rYou've been teleported for a test war by an admin."));
               }
            }
         }
      }
   }

   private void handleWinWar(MinecraftServer server, ICommandSender sender) {
      if (!(sender instanceof EntityPlayerMP)) {
         sender.sendMessage(new TextComponentString("§cMust be run by a player."));
      } else {
         EntityPlayerMP admin = (EntityPlayerMP)sender;
         WarBattlefield.forceCleanup(admin);

         for(EntityPlayerMP p : server.getPlayerList().getPlayers()) {
            if (p != admin && p.getEntityData().hasKey("pvpWarOrigGamemode")) {
               WarBattlefield.forceCleanup(p);
               p.sendMessage(new TextComponentString("§6[PvP] §rTest war ended. Your gamemode has been restored."));
            }
         }

         WarBattlefield.releaseArena();
         sender.sendMessage(new TextComponentString("§6[PvP] §rTest war ended. Gamemode restored. Arena released."));
      }
   }

   private void handleVillageDetect(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin villagedetect <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         String name = target.getName();
         sender.sendMessage(new TextComponentString("§6[PvP] §r§lVillage Detection for §e" + name));
         Team rawTeam = target.getTeam();
         String rawTeamName = rawTeam != null ? rawTeam.getName() : "§c(none)";
         sender.sendMessage(new TextComponentString("  §7Scoreboard Team: §f" + rawTeamName));
         VillageHelper.Village village = VillageHelper.getVillage(target);
         sender.sendMessage(new TextComponentString("  §7VillageHelper: §f" + village.villageName + " §8(teamName=" + village.teamName + ")"));
         boolean isAkatsuki = false;

         try {
            isAkatsuki = AkatsukiManager.getInstance().isAkatsuki(target.getUniqueID());
         } catch (Exception var21) {
         }

         sender.sendMessage(new TextComponentString("  §7AkatsukiManager.isAkatsuki: §f" + isAkatsuki));
         boolean isKage = false;

         try {
            isKage = KageManager.getInstance().isKage(target.getUniqueID());
         } catch (Exception var20) {
         }

         sender.sendMessage(new TextComponentString("  §7Is Kage/Leader: §f" + isKage));
         boolean inWar = false;

         for(WarInstance w : WarManager.getInstance().getAllActiveWars()) {
            if (w.getVillage1() == village || w.getVillage2() == village) {
               sender.sendMessage(new TextComponentString("  §7Active War: §f" + w.getVillage1().villageName + " vs " + w.getVillage2().villageName + " (" + w.getMode().displayName + ")"));
               inWar = true;
            }
         }

         if (!inWar) {
            sender.sendMessage(new TextComponentString("  §7Active War: §8(none for their village)"));
         }

         boolean hasWarTag = target.getEntityData().hasKey("pvpWarOrigGamemode");
         boolean hasDeadTag = target.getEntityData().hasKey("pvpWarDead");
         String gameMode = target.interactionManager.getGameType().getName();
         sender.sendMessage(new TextComponentString("  §7GameMode: §f" + gameMode + " §8| WarTag=" + hasWarTag + " DeadTag=" + hasDeadTag));

         try {
            PvpSavedData pvpData = PvpSavedData.get(server.getWorld(0));
            long pvpXp = pvpData.getPvpXp(target.getUniqueID());
            String pvpRank = pvpData.getPvpRank(target.getUniqueID()).name();
            sender.sendMessage(new TextComponentString("  §7PvP Rank: §f" + pvpRank + " §8(" + pvpXp + " XP)"));
         } catch (Exception var19) {
            sender.sendMessage(new TextComponentString("  §7PvP Rank: §8(error)"));
         }

      }
   }

   private void handleListWars(ICommandSender sender) {
      List<WarInstance> wars = WarManager.getInstance().getAllActiveWars();
      if (wars.isEmpty()) {
         sender.sendMessage(new TextComponentString("§6[PvP] §rNo active wars."));
      } else {
         sender.sendMessage(new TextComponentString("§6[PvP] §r§l--- Active Wars ---"));

         for(WarInstance war : wars) {
            sender.sendMessage(new TextComponentString(String.format("  §e%s§r: %s vs %s [%s] Score: %d-%d Time: %s", war.getWarId().substring(0, 8), war.getVillage1().villageName, war.getVillage2().villageName, war.getMode().displayName, war.getScore1(), war.getScore2(), war.getFormattedTimeRemaining())));
         }

      }
   }

   private void handleRefreshBingoBook(MinecraftServer server, ICommandSender sender) {
      BingoBook.getInstance().refresh(server.worlds[0]);
      int count = BingoBook.getInstance().getEntryCount();
      PvpSavedData.get(server.worlds[0]).clearBingoBookCache();
      this.syncBingoToAllPlayers(server);
      sender.sendMessage(new TextComponentString("§6[PvP] §rBingo Book refreshed. " + count + " entries active."));
   }

   private void handleSetBingo(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin setbingo <player> <xpReward>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);

         int reward;
         try {
            reward = Integer.parseInt(args[2]);
         } catch (NumberFormatException var8) {
            sender.sendMessage(new TextComponentString("§cInvalid XP reward amount: " + args[2]));
            return;
         }

         VillageHelper.Village village = VillageHelper.getVillage(target);
         String vName = village != VillageHelper.Village.UNKNOWN ? village.teamName : "Unknown";
         BingoBook.getInstance().setEntry(target.getUniqueID(), target.getName(), vName, reward, reward / 2);
         PvpSavedData.get(server.worlds[0]).clearBingoBookCache();
         this.syncBingoToAllPlayers(server);
         sender.sendMessage(new TextComponentString("§6[PvP] §rBingo Book entry set on " + target.getName() + " (" + vName + ") for " + reward + " Ninja XP, " + reward / 2 + " PvP XP."));
      }
   }

   private void handleRemoveBingo(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin removebingo <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         boolean had = BingoBook.getInstance().getActiveEntries().stream().anyMatch((b) -> b.targetUUID.equals(target.getUniqueID()));
         BingoBook.getInstance().removeEntry(target.getUniqueID());
         PvpSavedData data = PvpSavedData.get(server.worlds[0]);
         data.clearBingoBookCache();
         if (had) {
            this.syncBingoToAllPlayers(server);
            sender.sendMessage(new TextComponentString("§6[PvP] §rBingo Book entry removed for " + target.getName() + "."));
         } else {
            sender.sendMessage(new TextComponentString("§c" + target.getName() + " is not in the Bingo Book."));
         }

      }
   }

   private void handleClearAllBingo(MinecraftServer server, ICommandSender sender) {
      int count = BingoBook.getInstance().getEntryCount();
      BingoBook.getInstance().clearAll();
      PvpSavedData data = PvpSavedData.get(server.worlds[0]);
      data.clearBingoBookCache();
      this.syncBingoToAllPlayers(server);
      sender.sendMessage(new TextComponentString("§6[PvP] §rCleared all " + count + " Bingo Book entries."));
   }

   private void handleListBingoBook(ICommandSender sender) {
      List<BingoBook.BingoEntry> entries = BingoBook.getInstance().getActiveEntries();
      if (entries.isEmpty()) {
         sender.sendMessage(new TextComponentString("§6[PvP] §rNo active Bingo Book entries."));
      } else {
         sender.sendMessage(new TextComponentString("§6[PvP] §r§l--- Bingo Book ---"));

         for(int i = 0; i < entries.size(); ++i) {
            BingoBook.BingoEntry b = (BingoBook.BingoEntry)entries.get(i);
            sender.sendMessage(new TextComponentString(String.format("  §e%d. §r%s §7(%s)§r — %d NXP, %d PXP, %d hunters", i + 1, b.targetName, b.targetVillage, b.ninjaXpReward, b.pvpXpReward, b.claimedBy.size())));
         }

      }
   }

   private void handleTogglePvp(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§6[PvP] §rPvP mission system toggled. (PvpManager not yet implemented)"));
   }

   private void handleResetPvp(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin resetpvp <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         PvpSavedData data = PvpSavedData.get(target.getServerWorld());
         Map<String, PvpMissionInstance> missions = data.getActiveInstances(target.getUniqueID());
         if (missions != null) {
            for(String slot : (String[])missions.keySet().toArray(new String[0])) {
               data.removeActiveInstance(target.getUniqueID(), slot);
            }
         }

         Map<String, PvpMissionOffer> offers = data.getSavedOffers(target.getUniqueID());
         if (offers != null) {
            for(String slot : (String[])offers.keySet().toArray(new String[0])) {
               data.removeOffer(target.getUniqueID(), slot);
            }
         }

         data.setPvpRank(target.getUniqueID(), QuestDefinition.QuestRank.D);
         sender.sendMessage(new TextComponentString("§6[PvP] §rPvP data reset for " + target.getName() + "."));
      }
   }

   private void handleWarCooldown(MinecraftServer server, ICommandSender sender, String[] args) {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin warcooldown <village> <minutes>"));
      } else {
         VillageHelper.Village village = parseVillage(args[1]);
         if (village == null) {
            sender.sendMessage(new TextComponentString("§cUnknown village: " + args[1]));
         } else {
            int minutes;
            try {
               minutes = Integer.parseInt(args[2]);
            } catch (NumberFormatException var8) {
               sender.sendMessage(new TextComponentString("§cInvalid minutes: " + args[2]));
               return;
            }

            if (minutes <= 0) {
               WarManager.getInstance().clearCooldown(village);
               sender.sendMessage(new TextComponentString("§6[PvP] §rWar cooldown cleared for " + village.villageName + "."));
            } else {
               long cooldownEnd = System.currentTimeMillis() + (long)minutes * 60L * 1000L;
               WarManager.getInstance().setCooldown(village, cooldownEnd);
               sender.sendMessage(new TextComponentString("§6[PvP] §rWar cooldown set for " + village.villageName + ": " + minutes + " minutes."));
            }

            PvpSavedData.get(server.worlds[0]).markDirty();
         }
      }
   }

   private void handleFixGamemode(MinecraftServer server, ICommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /pvpadmin fixgamemode <player>"));
      } else {
         EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(args[1]);
         if (target == null) {
            sender.sendMessage(new TextComponentString("§cPlayer not found or offline: " + args[1]));
         } else {
            WarBattlefield.forceCleanup(target);
            if (target.interactionManager.getGameType() == GameType.ADVENTURE) {
               target.setGameType(GameType.SURVIVAL);
            }

            sender.sendMessage(new TextComponentString("§6[PvP] §rFixed gamemode for " + target.getName() + " (now " + target.interactionManager.getGameType().getName() + "). War tags and effects cleared."));
         }
      }
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
      } else {
         String sub = args[0].toLowerCase();
         if (args.length == 2) {
            switch (sub) {
               case "setkage":
               case "setadvisor":
               case "removeadvisor":
               case "setpvprank":
               case "addpvpxp":
               case "setbingo":
               case "removebingo":
               case "resetpvp":
               case "fixgamemode":
               case "villagedetect":
                  return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
               case "removekage":
               case "warcooldown":
                  return getListOfStringsMatchingLastWord(args, VILLAGE_NAMES);
               case "endwar":
               case "forcewar":
               case "testdivision":
                  return getListOfStringsMatchingLastWord(args, VILLAGE_NAMES);
               default:
                  return Collections.emptyList();
            }
         } else if (args.length == 3) {
            switch (sub) {
               case "setpvprank":
                  return getListOfStringsMatchingLastWord(args, RANK_NAMES);
               case "endwar":
               case "forcewar":
               case "removeadvisor":
                  return getListOfStringsMatchingLastWord(args, VILLAGE_NAMES);
               default:
                  return Collections.emptyList();
            }
         } else {
            return args.length == 4 && "forcewar".equals(sub) ? getListOfStringsMatchingLastWord(args, WAR_MODES) : Collections.emptyList();
         }
      }
   }

   private void sendHelp(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§6[PvP] §r§l--- PvP Admin Commands ---"));
      sender.sendMessage(new TextComponentString("  /pvpadmin setkage <player>"));
      sender.sendMessage(new TextComponentString("  /pvpadmin removekage <village>"));
      sender.sendMessage(new TextComponentString("  /pvpadmin listkages"));
      sender.sendMessage(new TextComponentString("  /pvpadmin setadvisor <player>"));
      sender.sendMessage(new TextComponentString("  /pvpadmin removeadvisor <player> [village]"));
      sender.sendMessage(new TextComponentString("  /pvpadmin listadvisors"));
      sender.sendMessage(new TextComponentString("  /pvpadmin autosync §7(sync kage/advisor from LP groups)"));
      sender.sendMessage(new TextComponentString("  /pvpadmin setpvprank <player> <D|C|B|A|S|S+>"));
      sender.sendMessage(new TextComponentString("  /pvpadmin addpvpxp <player> <amount>"));
      sender.sendMessage(new TextComponentString("  /pvpadmin setbattlefield <x> <y> <z>"));
      sender.sendMessage(new TextComponentString("  /pvpadmin endwar <village1> <village2>"));
      sender.sendMessage(new TextComponentString("  /pvpadmin forcewar <village1> <village2> <mode>"));
      sender.sendMessage(new TextComponentString("  /pvpadmin listwars"));
      sender.sendMessage(new TextComponentString("  /pvpadmin refreshbingobook"));
      sender.sendMessage(new TextComponentString("  /pvpadmin setbingo <player> <xpReward>"));
      sender.sendMessage(new TextComponentString("  /pvpadmin removebingo <player>"));
      sender.sendMessage(new TextComponentString("  /pvpadmin clearallbingo §7(remove all bingo entries)"));
      sender.sendMessage(new TextComponentString("  /pvpadmin listbingobook"));
      sender.sendMessage(new TextComponentString("  /pvpadmin togglepvp"));
      sender.sendMessage(new TextComponentString("  /pvpadmin resetpvp <player>"));
      sender.sendMessage(new TextComponentString("  /pvpadmin warcooldown <village> <minutes>"));
   }

   @Nullable
   private static VillageHelper.Village parseVillage(String name) {
      String lower = name.toLowerCase();

      for(VillageHelper.Village v : VillageHelper.Village.values()) {
         if (v != VillageHelper.Village.UNKNOWN && (v.teamName.toLowerCase().equals(lower) || v.villageName.toLowerCase().equals(lower))) {
            return v;
         }
      }

      return null;
   }

   @Nullable
   private static QuestDefinition.QuestRank parseRank(String name) {
      switch (name.toUpperCase()) {
         case "D":
            return QuestDefinition.QuestRank.D;
         case "C":
            return QuestDefinition.QuestRank.C;
         case "B":
            return QuestDefinition.QuestRank.B;
         case "A":
            return QuestDefinition.QuestRank.A;
         case "S":
            return QuestDefinition.QuestRank.S;
         case "S+":
            return QuestDefinition.QuestRank.S_PLUS;
         default:
            return null;
      }
   }

   @Nullable
   private static WarMode parseWarMode(String name) {
      try {
         return WarMode.valueOf(name.toUpperCase());
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   private void syncBingoToAllPlayers(MinecraftServer server) {
      for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
         try {
            PvpNetworkHelper.sendPvpSync(player);
         } catch (Exception var5) {
         }
      }

   }

   private static String resolvePlayerName(MinecraftServer server, UUID uuid) {
      EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
      return player != null ? player.getName() : uuid.toString().substring(0, 8) + "... (offline)";
   }
}
