package net.luck.narutoaddon.OtherCode.akatsuki.npc;

import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiSavedData;
import net.luck.narutoaddon.OtherCode.akatsuki.mission.AkatsukiMission;
import net.luck.narutoaddon.OtherCode.akatsuki.mission.AkatsukiMissionManager;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class HideoutInteractionHelper {
   private static final long ZETSU_COOLDOWN_MS = 1800000L;

   public static void handlePainInteraction(EntityPlayerMP player) {
      AkatsukiManager manager = AkatsukiManager.getInstance();
      if (!manager.isAkatsuki(player)) {
         player.sendMessage(new TextComponentString(TextFormatting.DARK_PURPLE + "Pain regards you with cold indifference."));
      } else {
         List<AkatsukiMission> missions = AkatsukiMissionManager.getInstance().getActiveMissions(player.getUniqueID());
         if (missions.isEmpty()) {
            player.sendMessage(new TextComponentString(TextFormatting.DARK_PURPLE + "Pain: " + TextFormatting.GRAY + "No assignments. Check back later."));
         } else {
            player.sendMessage(new TextComponentString(TextFormatting.DARK_PURPLE + "Pain: " + TextFormatting.GRAY + "Your current assignments:"));

            for(AkatsukiMission mission : missions) {
               String status = mission.isCompleted() ? TextFormatting.GREEN + "[DONE]" : TextFormatting.YELLOW + "[ACTIVE]";
               player.sendMessage(new TextComponentString(TextFormatting.GRAY + " - " + TextFormatting.WHITE + mission.getTemplateName() + " " + status));
            }

         }
      }
   }

   public static void handleZetsuInteraction(EntityPlayerMP player) {
      AkatsukiManager manager = AkatsukiManager.getInstance();
      if (manager.isAkatsuki(player)) {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            AkatsukiSavedData data = AkatsukiSavedData.get(server.getWorld(0));
            UUID playerId = player.getUniqueID();
            long lastUse = data.getZetsuCooldown(playerId);
            long now = System.currentTimeMillis();
            if (lastUse > 0L && now - lastUse < 1800000L) {
               long remainingMs = 1800000L - (now - lastUse);
               long remainingMin = remainingMs / 60000L;
               player.sendMessage(new TextComponentString(TextFormatting.DARK_GREEN + "Zetsu: " + TextFormatting.GRAY + "I need more time to gather intel... (" + (remainingMin + 1L) + " min remaining)"));
            } else {
               List<EntityPlayerMP> targets = new ArrayList();

               for(EntityPlayerMP online : server.getPlayerList().getPlayers()) {
                  if (!online.getUniqueID().equals(playerId) && !manager.isAkatsuki(online)) {
                     targets.add(online);
                  }
               }

               if (targets.isEmpty()) {
                  player.sendMessage(new TextComponentString(TextFormatting.DARK_GREEN + "Zetsu: " + TextFormatting.GRAY + "No targets of interest are active right now."));
                  data.setZetsuCooldown(playerId, now);
               } else {
                  Collections.shuffle(targets);
                  int count = Math.min(1 + (new Random()).nextInt(3), targets.size());
                  player.sendMessage(new TextComponentString(TextFormatting.DARK_GREEN + "Zetsu: " + TextFormatting.GRAY + "I've been watching..."));

                  for(int i = 0; i < count; ++i) {
                     EntityPlayerMP target = (EntityPlayerMP)targets.get(i);
                     VillageHelper.Village nearVillage = VillageHelper.getNearestVillage(target.getPosition());
                     String villageName = nearVillage != null ? nearVillage.villageName : "unknown territory";
                     player.sendMessage(new TextComponentString(TextFormatting.DARK_GREEN + "Zetsu whispers: " + TextFormatting.WHITE + target.getName() + TextFormatting.GRAY + " was spotted near " + TextFormatting.GOLD + villageName + TextFormatting.GRAY + "..."));
                  }

                  data.setZetsuCooldown(playerId, now);
               }
            }
         }
      }
   }
}
