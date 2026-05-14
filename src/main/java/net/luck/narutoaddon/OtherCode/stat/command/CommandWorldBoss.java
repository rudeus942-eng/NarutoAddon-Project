
package net.luck.narutoaddon.OtherCode.stat.command;

import net.luck.narutoaddon.OtherCode.entity.*;
import net.luck.narutoaddon.OtherCode.stat.core.ChakraBossSpawner;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandWorldBoss extends CommandBase {
   public String getName() {
      return "worldboss";
   }

   public String getUsage(ICommandSender sender) {
      return "/worldboss <spawn|kill> [all|saiken|kokuo|gyuki|hidan|zabuza]";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 1) {
         sender.sendMessage(new TextComponentString(TextFormatting.RED + this.getUsage(sender)));
      } else {
         switch (args[0].toLowerCase()) {
            case "spawn":
               ChakraBossSpawner.getInstance().forceSpawn();
               sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "World boss spawned!"));
               break;
            case "kill":
               String target = args.length >= 2 ? args[1].toLowerCase() : "all";
               int killed = this.killBosses(server, target);
               if (killed < 0) {
                  sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown boss type: " + target + ". Use: all, saiken, kokuo, gyuki, hidan, zabuza"));
               } else {
                  sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Killed " + killed + " " + ("all".equals(target) ? "world boss" : target) + " entities across all dimensions."));
               }
               break;
            default:
               sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown action: " + action + ". " + this.getUsage(sender)));
         }

      }
   }

   private int killBosses(MinecraftServer server, String type) {
      int killed = 0;

      for(WorldServer ws : server.worlds) {
         List<Entity> toRemove = new ArrayList();

         for(Entity e : ws.loadedEntityList) {
            if (this.matchesBossType(e, type)) {
               toRemove.add(e);
            }
         }

         for(Entity e : toRemove) {
            e.setDead();
            ++killed;
         }
      }

      return killed;
   }

   private boolean matchesBossType(Entity e, String type) {
      switch (type) {
         case "all":
            return e instanceof EntityWorldBossSaiken.EntityCustom || e instanceof EntityWorldBossKokuo.EntityCustom || e instanceof EntityWorldBossGyuki.EntityCustom || e instanceof EntityWorldBossHidan.EntityCustom || e instanceof EntityWorldBossZabuza.EntityCustom;
         case "saiken":
            return e instanceof EntityWorldBossSaiken.EntityCustom;
         case "kokuo":
            return e instanceof EntityWorldBossKokuo.EntityCustom;
         case "gyuki":
            return e instanceof EntityWorldBossGyuki.EntityCustom;
         case "hidan":
            return e instanceof EntityWorldBossHidan.EntityCustom;
         case "zabuza":
            return e instanceof EntityWorldBossZabuza.EntityCustom;
         default:
            return false;
      }
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"spawn", "kill"});
      } else {
         return args.length == 2 && "kill".equalsIgnoreCase(args[0]) ? getListOfStringsMatchingLastWord(args, new String[]{"all", "saiken", "kokuo", "gyuki", "hidan", "zabuza"}) : Collections.emptyList();
      }
   }
}
