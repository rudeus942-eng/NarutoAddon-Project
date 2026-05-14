
package net.luck.narutoaddon.OtherCode.quest.pvp.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.pvp.BingoBook;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PvpActionMessage implements IMessage {
   public static final int ACTION_ACCEPT_OFFER = 0;
   public static final int ACTION_REROLL_OFFER = 1;
   public static final int ACTION_ABANDON_MISSION = 2;
   public static final int ACTION_ACCEPT_BINGO = 3;
   public static final int ACTION_ABANDON_BINGO = 4;
   public static final int ACTION_REQUEST_SYNC = 5;
   private byte action;
   private byte subSlotByte;

   public PvpActionMessage() {
   }

   public PvpActionMessage(int action, byte subSlotByte) {
      this.action = (byte)action;
      this.subSlotByte = subSlotByte;
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readByte();
      this.subSlotByte = buf.readByte();
   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.action);
      buf.writeByte(this.subSlotByte);
   }

   public static class Handler implements IMessageHandler<PvpActionMessage, IMessage> {
      public IMessage onMessage(PvpActionMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         if (player == null) {
            return null;
         } else {
            player.getServerWorld().addScheduledTask(() -> {
               String subSlot = PvpSyncMessage.byteToPvpSubSlot(message.subSlotByte);
               if (subSlot != null || message.action == 5 || message.action == 3 || message.action == 4) {
                  PvpManager manager = PvpManager.getInstance();
                  switch (message.action) {
                     case 0:
                        manager.acceptOffer(player.getUniqueID(), subSlot, player.world);
                        PvpNetworkHelper.sendPvpSync(player);
                        break;
                     case 1:
                        manager.rerollOffer(player.getUniqueID(), subSlot, player.world);
                        PvpNetworkHelper.sendPvpSync(player);
                        break;
                     case 2:
                        manager.abandonMission(player.getUniqueID(), subSlot, player.world);
                        PvpNetworkHelper.sendPvpSync(player);
                        break;
                     case 3:
                        int bingoIndex = message.subSlotByte & 255;
                        int result = BingoBook.getInstance().acceptEntry(player.getUniqueID(), bingoIndex, player.world);
                        if (result == 0) {
                           player.sendMessage(new TextComponentString(TextFormatting.GOLD + "[BINGO BOOK] " + TextFormatting.GREEN + "Target accepted! Hunt them down."));
                        } else {
                           String reason;
                           switch (result) {
                              case 2:
                                 reason = "You can't put a bounty on yourself!";
                                 break;
                              case 3:
                                 reason = "This target has already been claimed by another hunter.";
                                 break;
                              case 4:
                                 reason = "You've already accepted a bingo target today. Try again after reset.";
                                 break;
                              default:
                                 reason = "Invalid target.";
                           }

                           player.sendMessage(new TextComponentString(TextFormatting.RED + "[BINGO BOOK] " + TextFormatting.GRAY + reason));
                        }

                        PvpNetworkHelper.sendPvpSync(player);
                        break;
                     case 4:
                        int bingoIdx = message.subSlotByte & 255;
                        BingoBook.getInstance().abandonEntry(player.getUniqueID(), bingoIdx);
                        PvpNetworkHelper.sendPvpSync(player);
                        break;
                     case 5:
                        PvpNetworkHelper.sendPvpSync(player);
                  }

               }
            });
            return null;
         }
      }
   }
}
