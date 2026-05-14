
package net.luck.narutoaddon.OtherCode.endgame.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.endgame.bingo.BingoManager;
import net.luck.narutoaddon.OtherCode.endgame.defense.DefenseManager;
import net.luck.narutoaddon.OtherCode.endgame.incursion.IncursionManager;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EndgameActionMessage implements IMessage {
   public static final byte ACCEPT_OUTPOST_MISSION = 0;
   public static final byte LEAVE_OUTPOST = 1;
   public static final byte ACCEPT_BINGO_HUNT = 2;
   public static final byte ABANDON_BINGO_HUNT = 3;
   public static final byte JOIN_INCURSION = 4;
   public static final byte JOIN_DEFENSE = 5;
   public static final byte LEAVE_INCURSION = 6;
   public static final byte REQUEST_SYNC = 10;
   public static final byte REQUEST_LEADERBOARD = 11;
   private byte actionType;
   private String outpostId = "";
   private byte tierOrdinal;
   private byte targetIndex;

   public EndgameActionMessage() {
   }

   public EndgameActionMessage(byte actionType) {
      this.actionType = actionType;
   }

   public EndgameActionMessage(byte actionType, String outpostId, byte tierOrdinal) {
      this.actionType = actionType;
      this.outpostId = outpostId;
      this.tierOrdinal = tierOrdinal;
   }

   public EndgameActionMessage(byte actionType, byte targetIndex) {
      this.actionType = actionType;
      this.targetIndex = targetIndex;
   }

   public static EndgameActionMessage leaderboardRequest(String category) {
      EndgameActionMessage msg = new EndgameActionMessage((byte)11);
      msg.outpostId = category != null ? category : "";
      return msg;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.actionType);
      ByteBufUtils.writeUTF8String(buf, this.outpostId);
      buf.writeByte(this.tierOrdinal);
      buf.writeByte(this.targetIndex);
   }

   public void fromBytes(ByteBuf buf) {
      this.actionType = buf.readByte();
      this.outpostId = ByteBufUtils.readUTF8String(buf);
      this.tierOrdinal = buf.readByte();
      this.targetIndex = buf.readByte();
   }

   public static class Handler implements IMessageHandler<EndgameActionMessage, IMessage> {
      public IMessage onMessage(EndgameActionMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
            switch (message.actionType) {
               case 0:
                  String error = OutpostManager.getInstance().acceptOutpostMission(player);
                  if (error != null) {
                     player.sendMessage(new TextComponentString("§c" + error));
                  }
                  break;
               case 1:
                  OutpostManager.getInstance().leaveOutpost(player);
                  break;
               case 2:
                  BingoManager.getInstance().acceptHunt(player, message.targetIndex);
                  break;
               case 3:
                  BingoManager.getInstance().abandonHunt(player);
                  break;
               case 4:
                  IncursionManager.getInstance().tryJoin(player);
                  EndgameNetworkHelper.sendIncursionSync(player);
                  break;
               case 5:
                  DefenseManager.getInstance().tryJoin(player);
                  break;
               case 6:
                  IncursionManager.getInstance().tryLeave(player);
                  EndgameNetworkHelper.sendIncursionSync(player);
                  break;
               case 7:
               case 8:
               case 9:
               default:
                  player.sendMessage(new TextComponentString("§cUnknown endgame action: " + message.actionType));
                  break;
               case 10:
                  EndgameNetworkHelper.sendFullSync(player);
                  break;
               case 11:
                  EndgameNetworkHelper.sendLeaderboard(player, message.outpostId);
            }

         });
         return null;
      }
   }
}
