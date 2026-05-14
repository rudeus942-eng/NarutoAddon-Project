
package net.luck.narutoaddon.OtherCode.endgame.command;

import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.endgame.PveRank;
import net.luck.narutoaddon.OtherCode.endgame.defense.DefenseInstance;
import net.luck.narutoaddon.OtherCode.endgame.defense.DefenseManager;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameNetworkHelper;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostDefinition;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostDifficultyTier;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandEndgameAdmin extends CommandBase {
   private static final String[] VILLAGES = new String[]{"leaf", "sand", "mist", "stone", "cloud", "rain"};
   private static final String[] DEFENSE_DIFFICULTIES = new String[]{"normal", "hard", "nightmare"};

   public String getName() {
      return "endgameadmin";
   }

   public String getUsage(ICommandSender sender) {
      return "/endgameadmin <outpost|bingo|incursion|defense|cooldown|leaderboard|pverank|status>";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length == 0) {
         this.sendHelp(sender);
      } else {
         switch (args[0].toLowerCase()) {
            case "outpost":
               this.handleOutpost(server, sender, args);
               break;
            case "bingo":
               this.handleBingo(server, sender, args);
               break;
            case "incursion":
               this.handleIncursion(server, sender, args);
               break;
            case "defense":
               this.handleDefense(server, sender, args);
               break;
            case "cooldown":
               this.handleCooldown(server, sender, args);
               break;
            case "leaderboard":
            case "lb":
               this.handleLeaderboard(server, sender, args);
               break;
            case "pverank":
               this.handlePveRank(server, sender, args);
               break;
            case "status":
               this.handleStatus(server, sender);
               break;
            default:
               this.sendHelp(sender);
         }

      }
   }

   private void handleOutpost(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin outpost <start|reset>"));
      } else {
         switch (args[1].toLowerCase()) {
            case "start":
               this.handleOutpostStart(server, sender, args);
               break;
            case "reset":
               this.handleOutpostReset(server, sender, args);
               break;
            default:
               sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin outpost <start|reset>"));
         }

      }
   }

   private void handleOutpostStart(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 4) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin outpost start <outpostId> <chunin|jonin|anbu>"));
      } else {
         String outpostId = args[2].toLowerCase();
         String tierName = args[3].toLowerCase();
         OutpostDefinition def = OutpostRegistry.get(outpostId);
         if (def == null) {
            sender.sendMessage(new TextComponentString("§cOutpost not found: " + outpostId));
         } else {
            OutpostDifficultyTier tier = parseTier(tierName);
            if (tier == null) {
               sender.sendMessage(new TextComponentString("§cInvalid tier: " + tierName + ". Use chunin, jonin, or anbu"));
            } else {
               sender.sendMessage(new TextComponentString("§aForce-starting outpost §e" + def.getDisplayName() + "§a at §e" + tier.getDisplayName() + "§a difficulty."));
            }
         }
      }
   }

   private void handleOutpostReset(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin outpost reset <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[2]);
         sender.sendMessage(new TextComponentString("§aCleared all outpost cooldowns for " + target.getName()));
      }
   }

   private void handleBingo(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin bingo <reset|complete>"));
      } else {
         switch (args[1].toLowerCase()) {
            case "reset":
               this.handleBingoReset(server, sender, args);
               break;
            case "complete":
               this.handleBingoComplete(server, sender, args);
               break;
            default:
               sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin bingo <reset|complete>"));
         }

      }
   }

   private void handleBingoReset(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin bingo reset <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[2]);
         sender.sendMessage(new TextComponentString("§aForce-regenerated bingo board for " + target.getName()));
      }
   }

   private void handleBingoComplete(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 4) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin bingo complete <player> <slot 1-5>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[2]);

         int slot;
         try {
            slot = Integer.parseInt(args[3]);
         } catch (NumberFormatException var7) {
            sender.sendMessage(new TextComponentString("§cInvalid slot number: " + args[3]));
            return;
         }

         if (slot >= 1 && slot <= 5) {
            sender.sendMessage(new TextComponentString("§aForce-completed bingo slot " + slot + " for " + target.getName()));
         } else {
            sender.sendMessage(new TextComponentString("§cSlot must be between 1 and 5."));
         }
      }
   }

   private void handleIncursion(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin incursion <start|stop>"));
      } else {
         switch (args[1].toLowerCase()) {
            case "start":
               this.handleIncursionStart(server, sender, args);
               break;
            case "stop":
               this.handleIncursionStop(server, sender);
               break;
            default:
               sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin incursion <start|stop>"));
         }

      }
   }

   private void handleIncursionStart(MinecraftServer server, ICommandSender sender, String[] args) {
      String template = args.length >= 3 ? args[2] : null;
      String hotspot = args.length >= 4 ? args[3] : null;
      if (template != null) {
         sender.sendMessage(new TextComponentString("§aForce-starting incursion: §e" + template + (hotspot != null ? " §aat hotspot §e" + hotspot : "")));
      } else {
         sender.sendMessage(new TextComponentString("§aForce-starting random incursion..."));
      }

   }

   private void handleIncursionStop(MinecraftServer server, ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§aForce-stopped active incursion."));
   }

   private void handleDefense(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin defense <start|stop|schedule>"));
      } else {
         switch (args[1].toLowerCase()) {
            case "start":
               this.handleDefenseStart(server, sender, args);
               break;
            case "stop":
               this.handleDefenseStop(server, sender, args);
               break;
            case "schedule":
               this.handleDefenseSchedule(server, sender, args);
               break;
            default:
               sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin defense <start|stop|schedule>"));
         }

      }
   }

   private void handleDefenseStart(MinecraftServer server, ICommandSender sender, String[] args) {
      if (args.length < 4) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin defense start <village> <normal|hard|nightmare>"));
      } else {
         String village = args[2].toLowerCase();
         String difficulty = args[3].toLowerCase();
         if (!isValidVillage(village)) {
            sender.sendMessage(new TextComponentString("§cInvalid village: " + village + ". Use: leaf, sand, mist, stone, cloud, rain"));
         } else if (!isValidDefenseDifficulty(difficulty)) {
            sender.sendMessage(new TextComponentString("§cInvalid difficulty: " + difficulty + ". Use: normal, hard, nightmare"));
         } else {
            String villageKey = village.toUpperCase();
            DefenseManager.DefenseDifficulty diff;
            switch (difficulty) {
               case "hard":
                  diff = DefenseManager.DefenseDifficulty.HARD;
                  break;
               case "nightmare":
                  diff = DefenseManager.DefenseDifficulty.NIGHTMARE;
                  break;
               default:
                  diff = DefenseManager.DefenseDifficulty.NORMAL;
            }

            DefenseManager.getInstance().startDefense(server.getWorld(0), villageKey, diff);

            for(EntityPlayerMP online : server.getPlayerList().getPlayers()) {
               EndgameNetworkHelper.sendDefenseSync(online);
            }

            sender.sendMessage(new TextComponentString("§aForce-starting village defense at §e" + capitalize(village) + "§a (§e" + capitalize(difficulty) + "§a difficulty)."));
         }
      }
   }

   private void handleDefenseStop(MinecraftServer server, ICommandSender sender, String[] args) {
      String village = args.length >= 3 ? args[2].toLowerCase() : null;
      if (village != null) {
         String villageKey = village.toUpperCase();
         DefenseInstance inst = DefenseManager.getInstance().getActiveDefense(villageKey);
         if (inst != null) {
            inst.cleanup();
         }

         DefenseManager.getInstance().removeDefense(villageKey);
         sender.sendMessage(new TextComponentString("§aForce-stopped defense at §e" + capitalize(village) + "§a."));
      } else {
         DefenseManager.getInstance().stopAll();
         sender.sendMessage(new TextComponentString("§aForce-stopped all active defenses."));
      }

      for(EntityPlayerMP online : server.getPlayerList().getPlayers()) {
         EndgameNetworkHelper.sendDefenseSync(online);
      }

   }

   private void handleDefenseSchedule(MinecraftServer server, ICommandSender sender, String[] args) {
      if (args.length < 4) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin defense schedule <village> <hour 0-23>"));
      } else {
         String village = args[2].toLowerCase();
         if (!isValidVillage(village)) {
            sender.sendMessage(new TextComponentString("§cInvalid village: " + village));
         } else {
            int hour;
            try {
               hour = Integer.parseInt(args[3]);
            } catch (NumberFormatException var7) {
               sender.sendMessage(new TextComponentString("§cInvalid hour: " + args[3]));
               return;
            }

            if (hour >= 0 && hour <= 23) {
               DefenseManager.getInstance().scheduleDefense(server.getWorld(0), village.toUpperCase(), hour);
               sender.sendMessage(new TextComponentString("§aSet defense schedule for §e" + capitalize(village) + "§a to hour §e" + hour + "§a."));
            } else {
               sender.sendMessage(new TextComponentString("§cHour must be between 0 and 23."));
            }
         }
      }
   }

   private void handleCooldown(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin cooldown clear <player>"));
      } else if (!"clear".equalsIgnoreCase(args[1])) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin cooldown clear <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[2]);
         sender.sendMessage(new TextComponentString("§aCleared all cooldowns (raid + outpost) for " + target.getName()));
      }
   }

   private void handleLeaderboard(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin leaderboard reset <player>"));
      } else if (!"reset".equalsIgnoreCase(args[1])) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin leaderboard reset <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[2]);
         sender.sendMessage(new TextComponentString("§aReset all leaderboard scores for " + target.getName()));
      }
   }

   private void handleStatus(MinecraftServer server, ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§6=== Endgame Admin Status ==="));
      int outpostCount = 0;

      for(OutpostDefinition def : OutpostRegistry.getAll()) {
         ++outpostCount;
      }

      sender.sendMessage(new TextComponentString("§7Registered Outposts: §e" + outpostCount));
      sender.sendMessage(new TextComponentString("§7Active Incursion: §7None"));
      sender.sendMessage(new TextComponentString("§7Active Defenses: §7None"));
      int playerCount = server.getPlayerList().getPlayers().size();
      sender.sendMessage(new TextComponentString("§7Online Players: §e" + playerCount));
   }

   private void handlePveRank(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin pverank <get|set|addxp> <player> [value]"));
      } else {
         String action = args[1].toLowerCase();
         EntityPlayerMP target = getPlayer(server, sender, args[2]);
         EndgameSavedData data = EndgameSavedData.get(target.getServerWorld());
         switch (action) {
            case "get":
               int xp = data.getPveXp(target.getUniqueID());
               PveRank rank = PveRank.fromXp(xp);
               PveRank nextRank = rank.getNextRank();
               String nextInfo = nextRank != null ? " (" + rank.xpToNextRank(xp) + " XP to " + nextRank.getDisplayName() + ")" : " (MAX RANK)";
               sender.sendMessage(new TextComponentString("§e" + target.getName() + "§7: PvE Rank = §a" + rank.getDisplayName() + "§7, XP = §b" + xp + "§7" + nextInfo));
               break;
            case "set":
               if (args.length < 4) {
                  sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin pverank set <player> <rank|xp_amount>"));
                  return;
               }

               String value = args[3];
               PveRank rank = PveRank.byName(value);
               if (rank != null) {
                  data.setPveXp(target.getUniqueID(), rank.getXpThreshold());
                  sender.sendMessage(new TextComponentString("§aSet " + target.getName() + "'s PvE rank to §e" + rank.getDisplayName() + "§a (XP=" + rank.getXpThreshold() + ")"));
               } else {
                  try {
                     int xp = Integer.parseInt(value);
                     data.setPveXp(target.getUniqueID(), xp);
                     PveRank newRank = PveRank.fromXp(xp);
                     sender.sendMessage(new TextComponentString("§aSet " + target.getName() + "'s PvE XP to §b" + xp + "§a (rank: §e" + newRank.getDisplayName() + "§a)"));
                  } catch (NumberFormatException var14) {
                     sender.sendMessage(new TextComponentString("§cInvalid value: " + value + ". Use a rank name (genin/chunin/jonin/anbu/kage) or XP number."));
                  }
               }
               break;
            case "addxp":
               if (args.length < 4) {
                  sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin pverank addxp <player> <amount>"));
                  return;
               }

               try {
                  int amount = Integer.parseInt(args[3]);
                  PveRank rankBefore = data.getPveRank(target.getUniqueID());
                  int newTotal = data.addPveXp(target.getUniqueID(), amount);
                  PveRank rankAfter = PveRank.fromXp(newTotal);
                  sender.sendMessage(new TextComponentString("§aAdded §b" + amount + " PvE XP§a to " + target.getName() + " (total: §b" + newTotal + "§a, rank: §e" + rankAfter.getDisplayName() + "§a)"));
                  if (rankAfter != rankBefore) {
                     target.sendMessage(new TextComponentString("§6§l★ PvE RANK UP! §r§e" + rankBefore.getDisplayName() + " → " + rankAfter.getDisplayName()));
                  }
               } catch (NumberFormatException var13) {
                  sender.sendMessage(new TextComponentString("§cInvalid XP amount: " + args[3]));
               }
               break;
            default:
               sender.sendMessage(new TextComponentString("§cUsage: /endgameadmin pverank <get|set|addxp> <player> [value]"));
         }

      }
   }

   private void sendHelp(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§6=== Endgame Admin Commands ==="));
      sender.sendMessage(new TextComponentString("§e/endgameadmin outpost start <id> <tier> §7- Force start outpost"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin outpost reset <player> §7- Clear outpost cooldowns"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin bingo reset <player> §7- Regenerate bingo board"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin bingo complete <player> <slot> §7- Force complete slot"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin incursion start [template] [hotspot] §7- Force incursion"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin incursion stop §7- Stop active incursion"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin defense start <village> <difficulty> §7- Force defense"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin defense stop [village] §7- Stop defense"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin defense schedule <village> <hour> §7- Set schedule"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin cooldown clear <player> §7- Clear all cooldowns"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin leaderboard reset <player> §7- Reset leaderboard"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin pverank get <player> §7- View PvE rank/XP"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin pverank set <player> <rank|xp> §7- Set PvE rank"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin pverank addxp <player> <amount> §7- Add PvE XP"));
      sender.sendMessage(new TextComponentString("§e/endgameadmin status §7- Show all systems state"));
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"outpost", "bingo", "incursion", "defense", "cooldown", "leaderboard", "lb", "pverank", "status"});
      } else {
         String sub = args[0].toLowerCase();
         if (args.length == 2) {
            switch (sub) {
               case "outpost":
                  return getListOfStringsMatchingLastWord(args, new String[]{"start", "reset"});
               case "bingo":
                  return getListOfStringsMatchingLastWord(args, new String[]{"reset", "complete"});
               case "incursion":
                  return getListOfStringsMatchingLastWord(args, new String[]{"start", "stop"});
               case "defense":
                  return getListOfStringsMatchingLastWord(args, new String[]{"start", "stop", "schedule"});
               case "cooldown":
                  return getListOfStringsMatchingLastWord(args, new String[]{"clear"});
               case "leaderboard":
               case "lb":
                  return getListOfStringsMatchingLastWord(args, new String[]{"reset"});
               case "pverank":
                  return getListOfStringsMatchingLastWord(args, new String[]{"get", "set", "addxp"});
            }
         }

         if (args.length == 3) {
            switch (sub) {
               case "outpost":
                  if ("start".equalsIgnoreCase(args[1])) {
                     List<String> ids = new ArrayList();

                     for(OutpostDefinition def : OutpostRegistry.getAll()) {
                        ids.add(def.getOutpostId());
                     }

                     return getListOfStringsMatchingLastWord(args, (String[])ids.toArray(new String[0]));
                  }

                  if ("reset".equalsIgnoreCase(args[1])) {
                     return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
                  }
                  break;
               case "bingo":
                  if ("reset".equalsIgnoreCase(args[1]) || "complete".equalsIgnoreCase(args[1])) {
                     return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
                  }
                  break;
               case "defense":
                  if ("start".equalsIgnoreCase(args[1]) || "schedule".equalsIgnoreCase(args[1])) {
                     return getListOfStringsMatchingLastWord(args, VILLAGES);
                  }

                  if ("stop".equalsIgnoreCase(args[1])) {
                     return getListOfStringsMatchingLastWord(args, VILLAGES);
                  }
                  break;
               case "cooldown":
                  if ("clear".equalsIgnoreCase(args[1])) {
                     return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
                  }
                  break;
               case "leaderboard":
               case "lb":
                  if ("reset".equalsIgnoreCase(args[1])) {
                     return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
                  }
                  break;
               case "pverank":
                  return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            }
         }

         if (args.length == 4) {
            switch (sub) {
               case "outpost":
                  if ("start".equalsIgnoreCase(args[1])) {
                     return getListOfStringsMatchingLastWord(args, new String[]{"chunin", "jonin", "anbu"});
                  }
                  break;
               case "bingo":
                  if ("complete".equalsIgnoreCase(args[1])) {
                     return getListOfStringsMatchingLastWord(args, new String[]{"1", "2", "3", "4", "5"});
                  }
                  break;
               case "pverank":
                  if ("set".equalsIgnoreCase(args[1])) {
                     return getListOfStringsMatchingLastWord(args, new String[]{"genin", "chunin", "jonin", "anbu", "kage"});
                  }
                  break;
               case "defense":
                  if ("start".equalsIgnoreCase(args[1])) {
                     return getListOfStringsMatchingLastWord(args, DEFENSE_DIFFICULTIES);
                  }

                  if ("schedule".equalsIgnoreCase(args[1])) {
                     return getListOfStringsMatchingLastWord(args, new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"});
                  }
            }
         }

         return Collections.emptyList();
      }
   }

   private static OutpostDifficultyTier parseTier(String name) {
      switch (name.toLowerCase()) {
         case "chunin":
            return OutpostDifficultyTier.CHUNIN;
         case "jonin":
            return OutpostDifficultyTier.JONIN;
         case "anbu":
            return OutpostDifficultyTier.ANBU;
         default:
            return null;
      }
   }

   private static boolean isValidVillage(String village) {
      for(String v : VILLAGES) {
         if (v.equals(village)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isValidDefenseDifficulty(String diff) {
      for(String d : DEFENSE_DIFFICULTIES) {
         if (d.equals(diff)) {
            return true;
         }
      }

      return false;
   }

   private static String capitalize(String s) {
      return s != null && !s.isEmpty() ? Character.toUpperCase(s.charAt(0)) + s.substring(1) : s;
   }
}
