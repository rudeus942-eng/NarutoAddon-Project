
package net.luck.narutoaddon.OtherCode.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.gui.GuiRankedStats;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RankedStatsDataMessage implements IMessage {
   private String playerName;
   private String playerRank;
   private int elo;
   private int peakElo;
   private int seasonHigh;
   private int wins;
   private int losses;
   private int currentStreak;
   private int bestStreak;
   private int gamesPlayed;
   private int leaderboardRank;
   private String placementProgress;

   public RankedStatsDataMessage() {
   }

   public RankedStatsDataMessage(String name, String rank, int elo, int peak, int seasonHigh, int wins, int losses, int streak, int bestStreak, int games, int lbRank, String placement) {
      this.playerName = name;
      this.playerRank = rank;
      this.elo = elo;
      this.peakElo = peak;
      this.seasonHigh = seasonHigh;
      this.wins = wins;
      this.losses = losses;
      this.currentStreak = streak;
      this.bestStreak = bestStreak;
      this.gamesPlayed = games;
      this.leaderboardRank = lbRank;
      this.placementProgress = placement != null ? placement : "";
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.playerName != null ? this.playerName : "");
      ByteBufUtils.writeUTF8String(buf, this.playerRank != null ? this.playerRank : "");
      buf.writeInt(this.elo);
      buf.writeInt(this.peakElo);
      buf.writeInt(this.seasonHigh);
      buf.writeInt(this.wins);
      buf.writeInt(this.losses);
      buf.writeInt(this.currentStreak);
      buf.writeInt(this.bestStreak);
      buf.writeInt(this.gamesPlayed);
      buf.writeInt(this.leaderboardRank);
      ByteBufUtils.writeUTF8String(buf, this.placementProgress != null ? this.placementProgress : "");
   }

   public void fromBytes(ByteBuf buf) {
      this.playerName = ByteBufUtils.readUTF8String(buf);
      this.playerRank = ByteBufUtils.readUTF8String(buf);
      this.elo = buf.readInt();
      this.peakElo = buf.readInt();
      this.seasonHigh = buf.readInt();
      this.wins = buf.readInt();
      this.losses = buf.readInt();
      this.currentStreak = buf.readInt();
      this.bestStreak = buf.readInt();
      this.gamesPlayed = buf.readInt();
      this.leaderboardRank = buf.readInt();
      this.placementProgress = ByteBufUtils.readUTF8String(buf);
   }

   public static class Handler implements IMessageHandler<RankedStatsDataMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(RankedStatsDataMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> this.handleClientSide(message));
         return null;
      }

      @SideOnly(Side.CLIENT)
      private void handleClientSide(RankedStatsDataMessage msg) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.currentScreen instanceof GuiRankedStats) {
            GuiRankedStats gui = (GuiRankedStats)mc.currentScreen;
            gui.updateData(msg.playerName, msg.playerRank, msg.elo, msg.peakElo, msg.seasonHigh, msg.wins, msg.losses, msg.currentStreak, msg.bestStreak, msg.gamesPlayed, msg.leaderboardRank, msg.placementProgress.isEmpty() ? null : msg.placementProgress);
         }

      }
   }
}
