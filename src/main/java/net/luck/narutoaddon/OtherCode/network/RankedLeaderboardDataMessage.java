
package net.luck.narutoaddon.OtherCode.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.gui.GuiRankedLeaderboard;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class RankedLeaderboardDataMessage implements IMessage {
   private List<GuiRankedLeaderboard.LeaderboardEntry> entries = new ArrayList();
   private int currentPage;
   private int totalPages;
   private int playerRank;
   private String playerUUID;

   public RankedLeaderboardDataMessage() {
   }

   public RankedLeaderboardDataMessage(List<GuiRankedLeaderboard.LeaderboardEntry> entries, int page, int total, int playerRank, String playerUUID) {
      this.entries = entries;
      this.currentPage = page;
      this.totalPages = total;
      this.playerRank = playerRank;
      this.playerUUID = playerUUID;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.entries.size());

      for(GuiRankedLeaderboard.LeaderboardEntry entry : this.entries) {
         buf.writeInt(entry.rank);
         ByteBufUtils.writeUTF8String(buf, entry.name);
         ByteBufUtils.writeUTF8String(buf, entry.rankTier);
         buf.writeInt(entry.elo);
         buf.writeInt(entry.wins);
         buf.writeInt(entry.losses);
         ByteBufUtils.writeUTF8String(buf, entry.uuid);
      }

      buf.writeInt(this.currentPage);
      buf.writeInt(this.totalPages);
      buf.writeInt(this.playerRank);
      ByteBufUtils.writeUTF8String(buf, this.playerUUID != null ? this.playerUUID : "");
   }

   public void fromBytes(ByteBuf buf) {
      int size = buf.readInt();
      this.entries = new ArrayList();

      for(int i = 0; i < size; ++i) {
         int rank = buf.readInt();
         String name = ByteBufUtils.readUTF8String(buf);
         String rankTier = ByteBufUtils.readUTF8String(buf);
         int elo = buf.readInt();
         int wins = buf.readInt();
         int losses = buf.readInt();
         String uuid = ByteBufUtils.readUTF8String(buf);
         this.entries.add(new GuiRankedLeaderboard.LeaderboardEntry(rank, name, rankTier, elo, wins, losses, uuid));
      }

      this.currentPage = buf.readInt();
      this.totalPages = buf.readInt();
      this.playerRank = buf.readInt();
      this.playerUUID = ByteBufUtils.readUTF8String(buf);
   }

   public static class Handler implements IMessageHandler<RankedLeaderboardDataMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(RankedLeaderboardDataMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> this.handleClientSide(message));
         return null;
      }

      @SideOnly(Side.CLIENT)
      private void handleClientSide(RankedLeaderboardDataMessage msg) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.currentScreen instanceof GuiRankedLeaderboard) {
            GuiRankedLeaderboard gui = (GuiRankedLeaderboard)mc.currentScreen;
            gui.updateData(msg.entries, msg.currentPage, msg.totalPages, msg.playerRank, msg.playerUUID);
         }

      }
   }
}
