
package net.luck.narutoaddon.OtherCode.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.RankedMatchHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RankedMatchHudMessage implements IMessage {
   private boolean inMatch;
   private String matchState;
   private int timeRemainingSeconds;
   private String player1Name;
   private String player2Name;
   private String player1Rank;
   private String player2Rank;
   private String player1UUID;
   private String player2UUID;
   private String arenaName;

   public RankedMatchHudMessage() {
   }

   public RankedMatchHudMessage(boolean inMatch) {
      this.inMatch = false;
      this.matchState = "";
      this.timeRemainingSeconds = 0;
      this.player1Name = "";
      this.player2Name = "";
      this.player1Rank = "";
      this.player2Rank = "";
      this.player1UUID = "";
      this.player2UUID = "";
      this.arenaName = "";
   }

   public RankedMatchHudMessage(String matchState, int timeRemainingSeconds, String player1Name, String player2Name, String player1Rank, String player2Rank, String player1UUID, String player2UUID, String arenaName) {
      this.inMatch = true;
      this.matchState = matchState != null ? matchState : "";
      this.timeRemainingSeconds = timeRemainingSeconds;
      this.player1Name = player1Name != null ? player1Name : "";
      this.player2Name = player2Name != null ? player2Name : "";
      this.player1Rank = player1Rank != null ? player1Rank : "";
      this.player2Rank = player2Rank != null ? player2Rank : "";
      this.player1UUID = player1UUID != null ? player1UUID : "";
      this.player2UUID = player2UUID != null ? player2UUID : "";
      this.arenaName = arenaName != null ? arenaName : "";
   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.inMatch);
      ByteBufUtils.writeUTF8String(buf, this.matchState);
      buf.writeInt(this.timeRemainingSeconds);
      ByteBufUtils.writeUTF8String(buf, this.player1Name);
      ByteBufUtils.writeUTF8String(buf, this.player2Name);
      ByteBufUtils.writeUTF8String(buf, this.player1Rank);
      ByteBufUtils.writeUTF8String(buf, this.player2Rank);
      ByteBufUtils.writeUTF8String(buf, this.player1UUID);
      ByteBufUtils.writeUTF8String(buf, this.player2UUID);
      ByteBufUtils.writeUTF8String(buf, this.arenaName);
   }

   public void fromBytes(ByteBuf buf) {
      this.inMatch = buf.readBoolean();
      this.matchState = ByteBufUtils.readUTF8String(buf);
      this.timeRemainingSeconds = buf.readInt();
      this.player1Name = ByteBufUtils.readUTF8String(buf);
      this.player2Name = ByteBufUtils.readUTF8String(buf);
      this.player1Rank = ByteBufUtils.readUTF8String(buf);
      this.player2Rank = ByteBufUtils.readUTF8String(buf);
      this.player1UUID = ByteBufUtils.readUTF8String(buf);
      this.player2UUID = ByteBufUtils.readUTF8String(buf);
      this.arenaName = ByteBufUtils.readUTF8String(buf);
   }

   public static class Handler implements IMessageHandler<RankedMatchHudMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(RankedMatchHudMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> this.handleClientSide(message));
         return null;
      }

      @SideOnly(Side.CLIENT)
      private void handleClientSide(RankedMatchHudMessage msg) {
         if (msg.inMatch) {
            RankedMatchHudOverlay.updateMatchData(msg.matchState, msg.timeRemainingSeconds, msg.player1Name, msg.player2Name, msg.player1Rank, msg.player2Rank, msg.player1UUID, msg.player2UUID, msg.arenaName);
         } else {
            RankedMatchHudOverlay.clearMatchData();
         }

      }
   }
}
