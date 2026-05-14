
package net.luck.narutoaddon.OtherCode.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.gui.GuiRankedSeason;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RankedSeasonDataMessage implements IMessage {
   private int seasonNumber;
   private String seasonId;
   private String seasonName;
   private int daysRemaining;
   private int hoursRemaining;
   private int minutesRemaining;
   private int totalMatches;
   private int activePlayers;
   private boolean hasPendingRewards;
   private String rewardTier;

   public RankedSeasonDataMessage() {
   }

   public RankedSeasonDataMessage(int number, String id, String name, int days, int hours, int minutes, int matches, int players, boolean pending, String reward) {
      this.seasonNumber = number;
      this.seasonId = id != null ? id : "S" + number;
      this.seasonName = name;
      this.daysRemaining = days;
      this.hoursRemaining = hours;
      this.minutesRemaining = minutes;
      this.totalMatches = matches;
      this.activePlayers = players;
      this.hasPendingRewards = pending;
      this.rewardTier = reward;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.seasonNumber);
      ByteBufUtils.writeUTF8String(buf, this.seasonId != null ? this.seasonId : "");
      ByteBufUtils.writeUTF8String(buf, this.seasonName != null ? this.seasonName : "");
      buf.writeInt(this.daysRemaining);
      buf.writeInt(this.hoursRemaining);
      buf.writeInt(this.minutesRemaining);
      buf.writeInt(this.totalMatches);
      buf.writeInt(this.activePlayers);
      buf.writeBoolean(this.hasPendingRewards);
      ByteBufUtils.writeUTF8String(buf, this.rewardTier != null ? this.rewardTier : "");
   }

   public void fromBytes(ByteBuf buf) {
      this.seasonNumber = buf.readInt();
      this.seasonId = ByteBufUtils.readUTF8String(buf);
      this.seasonName = ByteBufUtils.readUTF8String(buf);
      this.daysRemaining = buf.readInt();
      this.hoursRemaining = buf.readInt();
      this.minutesRemaining = buf.readInt();
      this.totalMatches = buf.readInt();
      this.activePlayers = buf.readInt();
      this.hasPendingRewards = buf.readBoolean();
      this.rewardTier = ByteBufUtils.readUTF8String(buf);
   }

   public static class Handler implements IMessageHandler<RankedSeasonDataMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(RankedSeasonDataMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> this.handleClientSide(message));
         return null;
      }

      @SideOnly(Side.CLIENT)
      private void handleClientSide(RankedSeasonDataMessage msg) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.currentScreen instanceof GuiRankedSeason) {
            GuiRankedSeason gui = (GuiRankedSeason)mc.currentScreen;
            gui.updateData(msg.seasonNumber, msg.seasonId, msg.seasonName, msg.daysRemaining, msg.hoursRemaining, msg.minutesRemaining, msg.totalMatches, msg.activePlayers, msg.hasPendingRewards, msg.rewardTier);
         }

      }
   }
}
