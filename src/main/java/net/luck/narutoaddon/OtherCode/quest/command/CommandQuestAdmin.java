
package net.luck.narutoaddon.OtherCode.quest.command;

import net.luck.narutoaddon.OtherCode.Rankeddatastorage;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiSavedData;
import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.quest.core.*;
import net.luck.narutoaddon.OtherCode.quest.network.QuestNetworkHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpSavedData;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.luck.narutoaddon.OtherCode.stat.core.StatManager;
import net.luck.narutoaddon.OtherCode.stat.core.StatSavedData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.*;

public class CommandQuestAdmin extends CommandBase {
   public String getName() {
      return "questadmin";
   }

   public String getUsage(ICommandSender sender) {
      return "/questadmin <give|complete|reset|resetarc|setcoord|listcoords|setrank|addrerolls|resetbeginner|resetbeginnerall|resetprogressall|resetkg|fixkgall|cleannpcs>";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length == 0) {
         this.sendHelp(sender);
      } else {
         switch (args[0].toLowerCase()) {
            case "give":
               this.handleGive(server, sender, args);
               break;
            case "complete":
               this.handleComplete(server, sender, args);
               break;
            case "reset":
               this.handleReset(server, sender, args);
               break;
            case "setcoord":
               this.handleSetCoord(server, sender, args);
               break;
            case "listcoords":
               this.handleListCoords(sender, args);
               break;
            case "setrank":
               this.handleSetRank(server, sender, args);
               break;
            case "addrerolls":
               this.handleAddRerolls(server, sender, args);
               break;
            case "resetbeginner":
               this.handleResetBeginner(server, sender, args);
               break;
            case "resetbeginnerall":
               this.handleResetBeginnerAll(server, sender);
               break;
            case "resetprogressall":
               this.handleResetProgressAll(server, sender);
               break;
            case "resetkg":
               this.handleResetKG(server, sender, args);
               break;
            case "fixkgall":
               this.handleFixKGAll(server, sender);
               break;
            case "resetarc":
               this.handleResetArc(server, sender, args);
               break;
            case "cleannpcs":
               this.handleCleanNpcs(server, sender, args);
               break;
            default:
               this.sendHelp(sender);
         }

      }
   }

   private void handleGive(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /questadmin give <player> <questId>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         String questId = args[2];
         if (!QuestRegistry.exists(questId)) {
            sender.sendMessage(new TextComponentString("§cQuest not found: " + questId));
         } else {
            QuestManager.getInstance().forceGiveQuest(target, questId);
            sender.sendMessage(new TextComponentString("§aGave quest '" + questId + "' to " + target.getName()));
         }
      }
   }

   private void handleComplete(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /questadmin complete <player> [slot]"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         String slot = args.length >= 3 ? args[2].toLowerCase() : "story";
         if (!isValidSlot(slot)) {
            sender.sendMessage(new TextComponentString("§cInvalid slot. Use: story, daily, weekly, random, daily_0-2, weekly_0-2"));
         } else {
            if ("story".equals(slot)) {
               Map<String, QuestInstance> active = QuestManager.getInstance().getActiveQuests(target.getUniqueID());
               boolean found = false;

               for(Map.Entry<String, QuestInstance> entry : active.entrySet()) {
                  if (QuestManager.isStorySlot((String)entry.getKey())) {
                     QuestManager.getInstance().forceCompleteQuest(target, (String)entry.getKey());
                     sender.sendMessage(new TextComponentString("§aForce-completed " + (String)entry.getKey() + " quest for " + target.getName()));
                     found = true;
                     break;
                  }
               }

               if (!found) {
                  sender.sendMessage(new TextComponentString("§cNo active story quest found for " + target.getName()));
               }
            } else if (!"daily".equals(slot) && !"weekly".equals(slot)) {
               QuestManager.getInstance().forceCompleteQuest(target, slot);
               sender.sendMessage(new TextComponentString("§aForce-completed " + slot + " quest for " + target.getName()));
            } else {
               String[] subSlots = QuestManager.getSubSlots(slot);
               boolean found = false;

               for(String sub : subSlots) {
                  if (QuestManager.getInstance().getActiveQuestInSlot(target.getUniqueID(), sub) != null) {
                     QuestManager.getInstance().forceCompleteQuest(target, sub);
                     sender.sendMessage(new TextComponentString("§aForce-completed " + sub + " quest for " + target.getName()));
                     found = true;
                  }
               }

               if (!found) {
                  sender.sendMessage(new TextComponentString("§cNo active " + slot + " quest found for " + target.getName()));
               }
            }

         }
      }
   }

   private void handleReset(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /questadmin reset <player> [questId]"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         String questId = args.length >= 3 ? args[2] : null;
         QuestManager.getInstance().resetQuest(target, questId);
         sender.sendMessage(new TextComponentString("§aReset quest progress for " + target.getName() + (questId != null ? " (quest: " + questId + ")" : " (all quests)")));
      }
   }

   private void handleSetCoord(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 6) {
         sender.sendMessage(new TextComponentString("§cUsage: /questadmin setcoord <questId> <stepIndex> <x> <y> <z>"));
      } else {
         String questId = args[1];
         if (!QuestRegistry.exists(questId)) {
            sender.sendMessage(new TextComponentString("§cQuest not found: " + questId));
         } else {
            int stepIndex;
            int x;
            int y;
            int z;
            try {
               stepIndex = Integer.parseInt(args[2]);
               x = Integer.parseInt(args[3]);
               y = Integer.parseInt(args[4]);
               z = Integer.parseInt(args[5]);
            } catch (NumberFormatException var11) {
               sender.sendMessage(new TextComponentString("§cInvalid number format."));
               return;
            }

            World world = server.getWorld(0);
            QuestSavedData savedData = QuestSavedData.get(world);
            savedData.setCoordinateOverride(questId, stepIndex, new BlockPos(x, y, z));
            sender.sendMessage(new TextComponentString("§aSet coord override for " + questId + " step " + stepIndex + " to (" + x + ", " + y + ", " + z + ")"));
         }
      }
   }

   private void handleListCoords(ICommandSender sender, String[] args) {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /questadmin listcoords <questId>"));
      } else {
         String questId = args[1];
         if (!QuestRegistry.exists(questId)) {
            sender.sendMessage(new TextComponentString("§cQuest not found: " + questId));
         } else {
            World world = null;
            if (sender instanceof EntityPlayerMP) {
               world = ((EntityPlayerMP)sender).world;
            } else {
               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               if (server != null) {
                  world = server.getWorld(0);
               }
            }

            if (world == null) {
               sender.sendMessage(new TextComponentString("§cCould not access world data."));
            } else {
               QuestSavedData savedData = QuestSavedData.get(world);
               Map<Integer, BlockPos> overrides = savedData.getCoordinateOverrides(questId);
               sender.sendMessage(new TextComponentString("§6=== Coordinates for " + questId + " ==="));
               QuestDefinition def = QuestRegistry.getById(questId);
               if (def != null) {
                  for(int i = 0; i < def.getStepCount(); ++i) {
                     QuestStep step = def.getStep(i);
                     BlockPos effectivePos = savedData.getEffectivePosition(questId, i);
                     boolean isOverride = overrides.containsKey(i);
                     sender.sendMessage(new TextComponentString("  §eStep " + i + " [" + step.type + "]: §f(" + effectivePos.getX() + ", " + effectivePos.getY() + ", " + effectivePos.getZ() + ")" + (isOverride ? " §a(overridden)" : " §7(default)")));
                  }

               }
            }
         }
      }
   }

   private void handleSetRank(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /questadmin setrank <player> <D|C|B|A|S|S+>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         QuestDefinition.QuestRank rank;
         switch (args[2].toUpperCase()) {
            case "D":
               rank = QuestDefinition.QuestRank.D;
               break;
            case "C":
               rank = QuestDefinition.QuestRank.C;
               break;
            case "B":
               rank = QuestDefinition.QuestRank.B;
               break;
            case "A":
               rank = QuestDefinition.QuestRank.A;
               break;
            case "S":
               rank = QuestDefinition.QuestRank.S;
               break;
            case "S+":
               rank = QuestDefinition.QuestRank.S_PLUS;
               break;
            default:
               sender.sendMessage(new TextComponentString("§cInvalid rank: " + rankStr + ". Use D, C, B, A, S, or S+"));
               return;
         }

         QuestSavedData savedData = QuestSavedData.get(target.world);
         savedData.setPlayerMissionRank(target.getUniqueID(), rank);
         sender.sendMessage(new TextComponentString("§aSet mission rank for " + target.getName() + " to §e" + rank.displayName));
         target.sendMessage(new TextComponentString("§d§l[Quest] §r§dYour mission rank has been set to §e" + rank.displayName + "§d. New missions will match this rank."));
      }
   }

   private void handleAddRerolls(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 4) {
         sender.sendMessage(new TextComponentString("§cUsage: /questadmin addrerolls <player> <daily|weekly> <count>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         String slot = args[2].toLowerCase();
         if (!"daily".equals(slot) && !"weekly".equals(slot)) {
            sender.sendMessage(new TextComponentString("§cSlot must be 'daily' or 'weekly'."));
         } else {
            int count;
            try {
               count = Integer.parseInt(args[3]);
            } catch (NumberFormatException var9) {
               sender.sendMessage(new TextComponentString("§cInvalid number: " + args[3]));
               return;
            }

            if (count <= 0) {
               sender.sendMessage(new TextComponentString("§cCount must be positive."));
            } else {
               QuestSavedData savedData = QuestSavedData.get(target.world);
               savedData.addBonusRerolls(target.getUniqueID(), slot, count);
               int remaining = savedData.getRerollsRemaining(target.getUniqueID(), slot);
               sender.sendMessage(new TextComponentString("§aAdded " + count + " bonus " + slot + " rerolls for " + target.getName() + ". They now have " + remaining + " total rerolls remaining."));
               target.sendMessage(new TextComponentString("§d§l[Quest] §r§dYou received §e" + count + "§d bonus " + slot + " rerolls! (" + remaining + " total remaining)"));
            }
         }
      }
   }

   private void handleResetBeginner(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /questadmin resetbeginner <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         QuestSavedData savedData = QuestSavedData.get(target.world);
         savedData.removeFlag(target.getUniqueID(), "beginner_v2_complete");
         savedData.removeFlag(target.getUniqueID(), "beginner_v2_stage1_done");
         QuestNetworkHelper.sendQuestSync(target);
         sender.sendMessage(new TextComponentString("§aReset beginner flags for" + target.getName() + ". They will see the Beginner GUI on next H press."));
      }
   }

   private void handleResetBeginnerAll(MinecraftServer server, ICommandSender sender) {
      if (!sender.canUseCommand(4, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 4 to reset the beginner menu for everyone."));
      } else {
         QuestSavedData savedData = QuestSavedData.get(server.getWorld(0));
         int completeCleared = savedData.removeFlagFromAllPlayers("beginner_v2_complete");
         int stage1Cleared = savedData.removeFlagFromAllPlayers("beginner_v2_stage1_done");
         int totalAffected = Math.max(completeCleared, stage1Cleared);
         int syncedOnline = 0;

         for(EntityPlayerMP online : server.getPlayerList().getPlayers()) {
            QuestNetworkHelper.sendQuestSync(online);
            online.sendMessage(new TextComponentString("§6§l[Beginner Reset] §eThe Beginner menu has been reset. Press §fH§e to go through the intro again."));
            ++syncedOnline;
         }

         sender.sendMessage(new TextComponentString("§aReset beginner flags for up to " + totalAffected + " players (" + completeCleared + " with beginner_v2_complete, " + stage1Cleared + " with beginner_v2_stage1_done). Synced " + syncedOnline + " online players."));
      }
   }

   private void handleResetProgressAll(MinecraftServer server, ICommandSender sender) {
      if (!sender.canUseCommand(4, this.getName())) {
         sender.sendMessage(new TextComponentString("§cYou need OP level 4 to reset progression for everyone."));
      } else {
         World world = server.getWorld(0);
         QuestSavedData questData = QuestSavedData.get(world);
         StatSavedData statData = StatSavedData.get(world);
         AkatsukiSavedData akatsukiData = AkatsukiSavedData.get(world);
         PvpSavedData pvpData = PvpSavedData.get(world);
         EndgameSavedData endgameData = EndgameSavedData.get(world);
         Rankeddatastorage rankedData = Rankeddatastorage.get(world);
         Set<UUID> allPlayers = new HashSet();
         allPlayers.addAll(questData.getAllPlayersWithAnyProgression());
         allPlayers.addAll(statData.getAllPlayerUuids());
         allPlayers.addAll(akatsukiData.getAllMembers().keySet());
         allPlayers.addAll(pvpData.getAllPlayersWithAnyPvpData());
         allPlayers.addAll(endgameData.getAllPlayersWithAnyEndgameData());
         if (rankedData != null) {
            for(String s : rankedData.getAllPlayerUuids()) {
               try {
                  allPlayers.add(UUID.fromString(s));
               } catch (IllegalArgumentException var24) {
               }
            }
         }

         for(EntityPlayerMP online : server.getPlayerList().getPlayers()) {
            allPlayers.add(online.getUniqueID());
         }

         int dupesCleared = 0;

         try {
            ShopSavedData shopData = ShopSavedData.get(world);
            if (shopData != null) {
               dupesCleared = shopData.resetDuplicateTrackingAll();
            }
         } catch (Exception var23) {
         }

         int questWiped = 0;
         int statWiped = 0;
         int akatsukiWiped = 0;
         int pvpWiped = 0;
         int endgameWiped = 0;
         int rankedWiped = 0;

         for(UUID playerId : allPlayers) {
            questData.wipePlayerProgression(playerId);
            ++questWiped;
            statData.resetPlayerStats(playerId);
            ++statWiped;
            if (akatsukiData.isMember(playerId)) {
               akatsukiData.resetMemberProgression(playerId);
               ++akatsukiWiped;
            }

            pvpData.resetPlayerPvp(playerId);
            ++pvpWiped;
            endgameData.resetPlayerEndgame(playerId);
            ++endgameWiped;
            if (rankedData != null) {
               rankedData.resetPlayer(playerId.toString());
               ++rankedWiped;
            }
         }

         int syncedOnline = 0;

         for(EntityPlayerMP online : server.getPlayerList().getPlayers()) {
            try {
               QuestNetworkHelper.sendQuestSync(online);
               StatManager.getInstance().syncToClient(online);
            } catch (Exception var22) {
            }

            online.sendMessage(new TextComponentString("§c§l[Progress Reset] §eYour quest progress, PvE/PvP ranks, Akatsuki ring upgrades, and stats have been reset by an admin. Ryo, crystals, inventory, and seasonal items are untouched."));
            ++syncedOnline;
         }

         sender.sendMessage(new TextComponentString("§aReset progression for " + allPlayers.size() + " players (online + offline). Quest=" + questWiped + ", Stat=" + statWiped + ", Akatsuki=" + akatsukiWiped + ", PvP=" + pvpWiped + ", Endgame=" + endgameWiped + ", Ranked=" + rankedWiped + ", Dupes=" + dupesCleared + ". Synced " + syncedOnline + " online."));
      }
   }

   private void handleResetKG(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /questadmin resetkg <player>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);
         QuestSavedData savedData = QuestSavedData.get(target.world);
         savedData.removeFlag(target.getUniqueID(), "kg_received");
         sender.sendMessage(new TextComponentString("§aReset kg_received flag for " + target.getName() + ". They can receive KG again from Surge of Rebirth."));
      }
   }

   private void handleFixKGAll(MinecraftServer server, ICommandSender sender) {
      QuestSavedData savedData = QuestSavedData.get(server.getWorld(0));
      int cleared = savedData.removeFlagForAllPlayers("kg_received");
      Set<UUID> completedPlayers = savedData.getPlayersWhoCompleted("surge_of_rebirth");
      int owed = 0;

      for(UUID playerId : completedPlayers) {
         if (!savedData.hasFlag(playerId, "kg_owed")) {
            savedData.setFlag(playerId, "kg_owed");
            ++owed;
         }
      }

      sender.sendMessage(new TextComponentString("§aKG fix applied globally:"));
      sender.sendMessage(new TextComponentString("§7- Cleared kg_received from §e" + cleared + "§7 players"));
      sender.sendMessage(new TextComponentString("§7- Set kg_owed for §e" + owed + "§7 players who completed Surge of Rebirth"));
      sender.sendMessage(new TextComponentString("§7- Affected players will receive their KG roll on next login"));
   }

   private void handleResetArc(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /questadmin resetarc <player> <arcNumber>"));
      } else {
         EntityPlayerMP target = getPlayer(server, sender, args[1]);

         int arcNumber;
         try {
            arcNumber = Integer.parseInt(args[2]);
         } catch (NumberFormatException var10) {
            sender.sendMessage(new TextComponentString("§cInvalid arc number: " + args[2]));
            return;
         }

         List<QuestDefinition> arcQuests = QuestRegistry.getByArc(arcNumber);
         if (arcQuests.isEmpty()) {
            sender.sendMessage(new TextComponentString("§cNo quests found for arc " + arcNumber));
         } else {
            int resetCount = 0;

            for(QuestDefinition quest : arcQuests) {
               QuestManager.getInstance().resetQuest(target, quest.getId());
               ++resetCount;
            }

            sender.sendMessage(new TextComponentString("§aReset " + resetCount + " quests in Arc " + arcNumber + " for " + target.getName()));
         }
      }
   }

   private static boolean isValidSlot(String slot) {
      return "story".equals(slot) || "daily".equals(slot) || "weekly".equals(slot) || "random".equals(slot) || slot.matches("daily_[0-2]") || slot.matches("weekly_[0-2]");
   }

   private void handleCleanNpcs(MinecraftServer server, ICommandSender sender, String[] args) {
      int radius = 0;
      if (args.length >= 2) {
         try {
            radius = Integer.parseInt(args[1]);
         } catch (NumberFormatException var14) {
            sender.sendMessage(new TextComponentString("§cInvalid radius. Usage: /questadmin cleannpcs [radius]"));
            return;
         }
      }

      WorldServer worldServer = server.getWorld(0);
      Set<UUID> activeTrackedEntities = new HashSet();

      for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
         QuestSavedData savedData = QuestSavedData.get(player.world);

         for(QuestInstance quest : savedData.getActiveQuests(player.getUniqueID())) {
            activeTrackedEntities.addAll(quest.getSpawnedEntities());
         }
      }

      int removed = 0;
      int preserved = 0;

      for(Entity entity : new ArrayList(worldServer.loadedEntityList)) {
         if (entity != null && !entity.isDead) {
            NBTTagCompound data = entity.getEntityData();
            if (data.getBoolean("questEntity")) {
               if (radius > 0 && sender instanceof Entity) {
                  double dist = (double)entity.getDistance((Entity)sender);
                  if (dist > (double)radius) {
                     continue;
                  }
               }

               if (activeTrackedEntities.contains(entity.getUniqueID())) {
                  ++preserved;
               } else {
                  entity.setDead();
                  ++removed;
               }
            }
         }
      }

      sender.sendMessage(new TextComponentString("§aCleaned " + removed + " stale quest NPCs. " + preserved + " active NPCs preserved."));
      if (removed > 0) {
         System.out.println("[QuestAdmin] cleannpcs: removed " + removed + " stale quest NPCs, preserved " + preserved);
      }

   }

   private void sendHelp(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§6=== Quest Admin Commands ==="));
      sender.sendMessage(new TextComponentString("§e/questadmin give <player> <questId> §7- Force-give quest"));
      sender.sendMessage(new TextComponentString("§e/questadmin complete <player> [slot] §7- Force-complete (story/daily/weekly/random)"));
      sender.sendMessage(new TextComponentString("§e/questadmin reset <player> [questId] §7- Reset progress"));
      sender.sendMessage(new TextComponentString("§e/questadmin setcoord <questId> <step> <x> <y> <z> §7- Set coords"));
      sender.sendMessage(new TextComponentString("§e/questadmin listcoords <questId> §7- Show coord overrides"));
      sender.sendMessage(new TextComponentString("§e/questadmin setrank <player> <D|C|B|A|S|S+> §7- Set mission rank"));
      sender.sendMessage(new TextComponentString("§e/questadmin addrerolls <player> <daily|weekly> <count> §7- Give bonus rerolls"));
      sender.sendMessage(new TextComponentString("§e/questadmin resetarc <player> <arcNumber> §7- Reset entire arc"));
      sender.sendMessage(new TextComponentString("§e/questadmin resetbeginner <player> §7- Reset beginner GUI flag"));
      sender.sendMessage(new TextComponentString("§e/questadmin resetbeginnerall §7- Reset Beginner menu for ALL players (OP 4)"));
      sender.sendMessage(new TextComponentString("§e/questadmin resetprogressall §7- Reset quests, stats, Akatsuki upgrades, PvP/PvE ranks for ALL (OP 4)"));
      sender.sendMessage(new TextComponentString("§e/questadmin resetkg <player>§7- Reset KG roll flag"));
      sender.sendMessage(new TextComponentString("§e/questadmin resetkg <player> §7- Reset KG roll flag"));
      sender.sendMessage(new TextComponentString("§e/questadmin fixkgall §7- Clear KG flags + queue rolls for ALL players (offline too)"));
      sender.sendMessage(new TextComponentString("§e/questadmin cleannpcs [radius] §7- Remove stale quest NPCs (preserves active ones)"));
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"give", "complete", "reset", "resetarc", "setcoord", "listcoords", "setrank", "addrerolls", "resetbeginner", "resetbeginnerall", "resetprogressall", "resetkg", "fixkgall", "cleannpcs"});
      } else {
         if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("complete") || sub.equals("reset") || sub.equals("resetarc") || sub.equals("setrank") || sub.equals("addrerolls") || sub.equals("resetbeginner") || sub.equals("resetkg")) {
               return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            }

            if (sub.equals("setcoord") || sub.equals("listcoords")) {
               return getListOfStringsMatchingLastWord(args, (String[])QuestRegistry.getAllQuestIds().toArray(new String[0]));
            }
         }

         if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give")) {
               return getListOfStringsMatchingLastWord(args, (String[])QuestRegistry.getAllQuestIds().toArray(new String[0]));
            }

            if (sub.equals("complete")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"story", "daily", "weekly", "random", "daily_0", "daily_1", "daily_2", "weekly_0", "weekly_1", "weekly_2"});
            }

            if (sub.equals("reset")) {
               return getListOfStringsMatchingLastWord(args, (String[])QuestRegistry.getAllQuestIds().toArray(new String[0]));
            }

            if (sub.equals("setrank")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"D", "C", "B", "A", "S", "S+"});
            }

            if (sub.equals("addrerolls")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"daily", "weekly"});
            }

            if (sub.equals("resetarc")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"1", "2", "3", "4", "99"});
            }
         }

         return Collections.emptyList();
      }
   }
}
