
package net.luck.narutoaddon.OtherCode;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ElementsInfTsukAddon.ModElement.Tag
public class RankedChatBlockHandler extends ElementsInfTsukAddon.ModElement {
   public RankedChatBlockHandler(ElementsInfTsukAddon instance) {
      super(instance, 103);
   }

   public void preInit(FMLPreInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new ChatBlockEventHandler());
   }

   public static boolean shouldBlockChat(EntityPlayer player) {
      String uuid = player.getUniqueID().toString();
      if (!Rankedqueue.isInMatch(uuid)) {
         return false;
      } else {
         Rankedqueue.Match match = Rankedqueue.getPlayerMatch(uuid);
         if (match == null) {
            return false;
         } else {
            return "countdown".equals(match.state) || "active".equals(match.state);
         }
      }
   }

   public static class ChatBlockEventHandler {
      @SubscribeEvent(
         priority = EventPriority.HIGHEST
      )
      public void onServerChat(ServerChatEvent event) {
         EntityPlayerMP player = event.getPlayer();
         if (RankedChatBlockHandler.shouldBlockChat(player)) {
            event.setCanceled(true);
            player.sendMessage(new TextComponentString("§c§lChat is disabled during ranked matches!"));
         }

      }

      @SubscribeEvent(
         priority = EventPriority.HIGHEST
      )
      public void onCommand(CommandEvent event) {
         ICommandSender sender = event.getSender();
         if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            if (RankedChatBlockHandler.shouldBlockChat(player)) {
               event.setCanceled(true);
               player.sendMessage(new TextComponentString("§c§lCommands are disabled during ranked matches!"));
            }

         }
      }
   }
}
