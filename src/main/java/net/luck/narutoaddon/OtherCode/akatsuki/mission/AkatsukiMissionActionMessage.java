package net.luck.narutoaddon.OtherCode.akatsuki.mission;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;
import java.util.UUID;

public class AkatsukiMissionActionMessage implements IMessage {
   public static final int ACCEPT_OFFER = 0;
   public static final int ABANDON_MISSION = 1;
   private int actionType = 0;
   private int intParam = 0;
   private String stringParam = "";

   public static AkatsukiMissionActionMessage acceptOffer(int offerIndex) {
      AkatsukiMissionActionMessage msg = new AkatsukiMissionActionMessage();
      msg.actionType = 0;
      msg.intParam = offerIndex;
      msg.stringParam = "";
      return msg;
   }

   public static AkatsukiMissionActionMessage abandonMission(String templateId) {
      AkatsukiMissionActionMessage msg = new AkatsukiMissionActionMessage();
      msg.actionType = 1;
      msg.intParam = 0;
      msg.stringParam = templateId;
      return msg;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.actionType);
      buf.writeInt(this.intParam);
      ByteBufUtils.writeUTF8String(buf, this.stringParam);
   }

   public void fromBytes(ByteBuf buf) {
      this.actionType = buf.readInt();
      this.intParam = buf.readInt();
      this.stringParam = ByteBufUtils.readUTF8String(buf);
   }

   public static class Handler implements IMessageHandler<AkatsukiMissionActionMessage, IMessage> {
      public IMessage onMessage(AkatsukiMissionActionMessage msg, MessageContext ctx) {
         final EntityPlayerMP player = ctx.getServerHandler().player;
         final int action = msg.actionType;
         final int idx = msg.intParam;
         final String templateId = msg.stringParam;
         FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable() {
            public void run() {
               UUID playerId = player.getUniqueID();
               if (AkatsukiManager.getInstance().isAkatsuki(playerId)) {
                  AkatsukiMissionManager mgr = AkatsukiMissionManager.getInstance();
                  switch (action) {
                     case 0:
                        List<AkatsukiMission> currentOffers = mgr.getOffers(playerId);
                        AkatsukiMission offerMission = null;
                        if (idx >= 0 && idx < currentOffers.size()) {
                           offerMission = (AkatsukiMission)currentOffers.get(idx);
                        }

                        boolean accepted = mgr.acceptOffer(playerId, idx);
                        if (accepted && offerMission != null) {
                           player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.WHITE + "Mission accepted: " + TextFormatting.GOLD + offerMission.getTemplateName()));
                        } else if (!accepted) {
                           player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.RED + "Cannot accept mission. Slot may be full."));
                        }

                        mgr.syncToClient(player);
                        break;
                     case 1:
                        boolean abandoned = mgr.abandonMission(playerId, templateId);
                        if (abandoned) {
                           QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "akatsuki_mission_" + templateId);
                           AkatsukiMissionTemplate.Template tmpl = AkatsukiMissionTemplate.getById(templateId);
                           String name = tmpl != null ? tmpl.name : templateId;
                           player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "Mission abandoned: " + name));
                        } else {
                           player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.RED + "No active mission found to abandon."));
                        }

                        mgr.syncToClient(player);
                  }

               }
            }
         });
         return null;
      }
   }
}
