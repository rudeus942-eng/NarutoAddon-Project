
package net.luck.narutoaddon.OtherCode.quest.command;

import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;
import net.luck.narutoaddon.OtherCode.quest.core.QuestInstance;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.quest.core.QuestRegistry;
import net.luck.narutoaddon.OtherCode.quest.network.QuestNetworkHelper;
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
import java.util.Set;

public class CommandQuest extends CommandBase {
   public String getName() {
      return "quest";
   }

   public String getUsage(ICommandSender sender) {
      return "/quest <list|accept|abandon|track|help>";
   }

   public int getRequiredPermissionLevel() {
      return 0;
   }

   public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
      return sender instanceof EntityPlayerMP;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         throw new CommandException("Players only", new Object[0]);
      } else {
         EntityPlayerMP player = (EntityPlayerMP)sender;
         if (args.length == 0) {
            this.sendHelp(player);
         } else {
            switch (args[0].toLowerCase()) {
               case "list":
                  this.handleList(player);
                  break;
               case "accept":
                  if (args.length < 2) {
                     player.sendMessage(new TextComponentString("§cUsage: /quest accept <questId>"));
                     return;
                  }

                  this.handleAccept(player, args[1]);
                  break;
               case "abandon":
                  String slot = args.length >= 2 ? args[1].toLowerCase() : "story";
                  this.handleAbandon(player, slot);
                  break;
               case "track":
                  this.handleTrack(player);
                  break;
               case "help":
               default:
                  this.sendHelp(player);
            }

         }
      }
   }

   private void handleList(EntityPlayerMP player) {
      QuestManager manager = QuestManager.getInstance();
      Set<String> completed = manager.getCompletedQuests(player.getUniqueID());
      Map<String, QuestInstance> activeSlots = manager.getActiveQuests(player.getUniqueID());
      player.sendMessage(new TextComponentString("§6=== Quest Log ==="));
      if (!activeSlots.isEmpty()) {
         for(Map.Entry<String, QuestInstance> entry : activeSlots.entrySet()) {
            QuestInstance active = (QuestInstance)entry.getValue();
            QuestDefinition def = QuestRegistry.getById(active.getQuestId());
            String name = def != null ? def.getName() : active.getQuestId();
            player.sendMessage(new TextComponentString("§e[" + ((String)entry.getKey()).toUpperCase() + "] §f" + name + " §7(Step " + (active.getCurrentStepIndex() + 1) + "/" + (def != null ? def.getStepCount() : "?") + ")"));
         }
      }

      player.sendMessage(new TextComponentString("§a--- Available ---"));
      boolean hasAvailable = false;

      for(QuestDefinition def : QuestRegistry.getAll()) {
         if (!completed.contains(def.getId())) {
            boolean isActive = false;

            for(QuestInstance q : activeSlots.values()) {
               if (q.getQuestId().equals(def.getId())) {
                  isActive = true;
                  break;
               }
            }

            if (!isActive) {
               boolean prereqsMet = true;

               for(String prereq : def.getPrerequisites()) {
                  if (!completed.contains(prereq)) {
                     prereqsMet = false;
                     break;
                  }
               }

               if (prereqsMet) {
                  player.sendMessage(new TextComponentString("  §a" + def.getId() + " §f- " + def.getName()));
                  hasAvailable = true;
               }
            }
         }
      }

      if (!hasAvailable) {
         player.sendMessage(new TextComponentString("  §7(none)"));
      }

      if (!completed.isEmpty()) {
         player.sendMessage(new TextComponentString("§7--- Completed (" + completed.size() + ") ---"));

         for(String questId : completed) {
            QuestDefinition def = QuestRegistry.getById(questId);
            String name = def != null ? def.getName() : questId;
            player.sendMessage(new TextComponentString("  §7✓ " + name));
         }
      }

      QuestNetworkHelper.sendQuestSync(player);
   }

   private void handleAccept(EntityPlayerMP player, String questId) {
      QuestDefinition def = QuestRegistry.getById(questId);
      if (def != null && def.isRepeatable()) {
         player.sendMessage(new TextComponentString("§cRepeatable quests must be accepted through the Quest Log GUI (press H)."));
      } else {
         QuestManager.getInstance().acceptQuest(player, questId);
      }
   }

   private void handleAbandon(EntityPlayerMP player, String slot) {
      if (!isValidSlot(slot)) {
         player.sendMessage(new TextComponentString("§cInvalid slot. Use: story, daily, daily_0/1/2, weekly, weekly_0/1/2, or random"));
      } else if ("story".equals(slot)) {
         Map<String, QuestInstance> active = QuestManager.getInstance().getActiveQuests(player.getUniqueID());

         for(Map.Entry<String, QuestInstance> entry : active.entrySet()) {
            if (QuestManager.isStorySlot((String)entry.getKey())) {
               QuestManager.getInstance().abandonQuestBySlot(player, (String)entry.getKey());
               return;
            }
         }

         player.sendMessage(new TextComponentString("§cYou don't have an active story quest."));
      } else if (!"daily".equals(slot) && !"weekly".equals(slot)) {
         QuestManager.getInstance().abandonQuestBySlot(player, slot);
      } else {
         String[] subSlots = QuestManager.getSubSlots(slot);
         Map<String, QuestInstance> active = QuestManager.getInstance().getActiveQuests(player.getUniqueID());
         boolean found = false;

         for(String sub : subSlots) {
            if (active.containsKey(sub)) {
               QuestManager.getInstance().abandonQuestBySlot(player, sub);
               found = true;
               break;
            }
         }

         if (!found) {
            player.sendMessage(new TextComponentString("§cYou don't have an active " + slot + " quest."));
         }

      }
   }

   private void handleTrack(EntityPlayerMP player) {
      QuestManager.getInstance().sendQuestTrackInfo(player);
   }

   private void sendHelp(EntityPlayerMP player) {
      player.sendMessage(new TextComponentString("§6=== Quest Commands ==="));
      player.sendMessage(new TextComponentString("§e/quest list §7- Show available & active quests"));
      player.sendMessage(new TextComponentString("§e/quest accept <id> §7- Accept a story quest"));
      player.sendMessage(new TextComponentString("§e/quest abandon [slot] §7- Abandon quest (story/daily/weekly/random)"));
      player.sendMessage(new TextComponentString("§e/quest track §7- Show all active objectives"));
      player.sendMessage(new TextComponentString("§7Press H to open the Quest Log GUI."));
      player.sendMessage(new TextComponentString("§7Daily/Weekly/Random quests are accepted via the GUI."));
   }

   private static boolean isValidSlot(String slot) {
      return "story".equals(slot) || "random".equals(slot) || "daily".equals(slot) || "weekly".equals(slot) || "daily_0".equals(slot) || "daily_1".equals(slot) || "daily_2".equals(slot) || "weekly_0".equals(slot) || "weekly_1".equals(slot) || "weekly_2".equals(slot);
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"list", "accept", "abandon", "track", "help"});
      } else {
         if (args.length == 2) {
            if (args[0].equalsIgnoreCase("accept")) {
               return getListOfStringsMatchingLastWord(args, (String[])QuestRegistry.getAllQuestIds().toArray(new String[0]));
            }

            if (args[0].equalsIgnoreCase("abandon")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"story", "daily", "daily_0", "daily_1", "daily_2", "weekly", "weekly_0", "weekly_1", "weekly_2", "random"});
            }
         }

         return Collections.emptyList();
      }
   }
}
