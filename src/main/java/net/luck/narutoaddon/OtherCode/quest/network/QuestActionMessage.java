
package net.luck.narutoaddon.OtherCode.quest.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class QuestActionMessage implements IMessage {
   public static final int ACTION_ACCEPT_QUEST = 0;
   public static final int ACTION_ABANDON_QUEST = 1;
   public static final int ACTION_DIALOG_RESPONSE = 2;
   public static final int ACTION_TRACK_QUEST = 3;
   public static final int ACTION_REQUEST_SYNC = 4;
   public static final int ACTION_RANDOM_QUEST = 5;
   public static final int ACTION_REROLL = 6;
   public static final int ACTION_ACCEPT_OFFER = 7;
   private byte action;
   private String questId;
   private int responseIndex;

   public QuestActionMessage() {
   }

   public QuestActionMessage(int action) {
      this.action = (byte)action;
      this.questId = "";
      this.responseIndex = 0;
   }

   public QuestActionMessage(int action, String questId) {
      this.action = (byte)action;
      this.questId = questId != null ? questId : "";
      this.responseIndex = 0;
   }

   public QuestActionMessage(int action, int responseIndex) {
      this.action = (byte)action;
      this.questId = "";
      this.responseIndex = responseIndex;
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readByte();
      this.questId = ByteBufUtils.readUTF8String(buf);
      this.responseIndex = buf.readInt();
   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.action);
      ByteBufUtils.writeUTF8String(buf, this.questId != null ? this.questId : "");
      buf.writeInt(this.responseIndex);
   }

   public static class Handler implements IMessageHandler<QuestActionMessage, IMessage> {
      public IMessage onMessage(QuestActionMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         if (player == null) {
            return null;
         } else {
            player.getServerWorld().addScheduledTask(() -> {
               QuestManager manager = QuestManager.getInstance();
               switch (message.action) {
                  case 0:
                     manager.acceptQuest(player, message.questId);
                     break;
                  case 1:
                     if (message.questId != null && !message.questId.isEmpty()) {
                        manager.abandonQuestBySlot(player, message.questId);
                     } else {
                        manager.abandonQuest(player);
                     }
                     break;
                  case 2:
                     manager.onDialogResponse(player, message.responseIndex);
                     break;
                  case 3:
                     manager.sendQuestTrackInfo(player);
                     break;
                  case 4:
                     QuestNetworkHelper.sendQuestSync(player);
                     break;
                  case 5:
                     manager.acceptRandomQuest(player);
                     break;
                  case 6:
                     if (message.questId != null && !message.questId.isEmpty()) {
                        manager.rerollOffer(player, message.questId);
                     }
                     break;
                  case 7:
                     if (message.questId != null && !message.questId.isEmpty()) {
                        manager.acceptOffer(player, message.questId);
                     }
               }

            });
            return null;
         }
      }
   }
}
