
package net.luck.narutoaddon.OtherCode.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.RankedHudRenderer;
import net.luck.narutoaddon.OtherCode.gui.GuiRankedMenu;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RankedGuiDataMessage implements IMessage {
   private String rank;
   private int elo;
   private int wins;
   private int losses;
   private int winStreak;
   private boolean isQueued;
   private boolean isInMatch;
   private int queueTime;
   private int queueSize;
   private String placement;
   private int dailyWinsToday;
   private int dailyWinsRequired;
   private boolean dailyTaskCompleted;
   private boolean hasPendingRewards;
   private boolean isInRaid;
   private String currentRaidBoss;

   public RankedGuiDataMessage() {
   }

   public RankedGuiDataMessage(String rank, int elo, int wins, int losses, int winStreak, boolean isQueued, boolean isInMatch, int queueTime, int queueSize, String placement, int dailyWinsToday, int dailyWinsRequired, boolean dailyTaskCompleted, boolean hasPendingRewards, boolean isInRaid, String currentRaidBoss) {
      this.rank = rank;
      this.elo = elo;
      this.wins = wins;
      this.losses = losses;
      this.winStreak = winStreak;
      this.isQueued = isQueued;
      this.isInMatch = isInMatch;
      this.queueTime = queueTime;
      this.queueSize = queueSize;
      this.placement = placement != null ? placement : "";
      this.dailyWinsToday = dailyWinsToday;
      this.dailyWinsRequired = dailyWinsRequired;
      this.dailyTaskCompleted = dailyTaskCompleted;
      this.hasPendingRewards = hasPendingRewards;
      this.isInRaid = isInRaid;
      this.currentRaidBoss = currentRaidBoss != null ? currentRaidBoss : "";
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.rank != null ? this.rank : "");
      buf.writeInt(this.elo);
      buf.writeInt(this.wins);
      buf.writeInt(this.losses);
      buf.writeInt(this.winStreak);
      buf.writeBoolean(this.isQueued);
      buf.writeBoolean(this.isInMatch);
      buf.writeInt(this.queueTime);
      buf.writeInt(this.queueSize);
      ByteBufUtils.writeUTF8String(buf, this.placement != null ? this.placement : "");
      buf.writeInt(this.dailyWinsToday);
      buf.writeInt(this.dailyWinsRequired);
      buf.writeBoolean(this.dailyTaskCompleted);
      buf.writeBoolean(this.hasPendingRewards);
      buf.writeBoolean(this.isInRaid);
      ByteBufUtils.writeUTF8String(buf, this.currentRaidBoss != null ? this.currentRaidBoss : "");
   }

   public void fromBytes(ByteBuf buf) {
      this.rank = ByteBufUtils.readUTF8String(buf);
      this.elo = buf.readInt();
      this.wins = buf.readInt();
      this.losses = buf.readInt();
      this.winStreak = buf.readInt();
      this.isQueued = buf.readBoolean();
      this.isInMatch = buf.readBoolean();
      this.queueTime = buf.readInt();
      this.queueSize = buf.readInt();
      this.placement = ByteBufUtils.readUTF8String(buf);
      this.dailyWinsToday = buf.readInt();
      this.dailyWinsRequired = buf.readInt();
      this.dailyTaskCompleted = buf.readBoolean();
      this.hasPendingRewards = buf.readBoolean();
      this.isInRaid = buf.readBoolean();
      this.currentRaidBoss = ByteBufUtils.readUTF8String(buf);
   }

   public static class Handler implements IMessageHandler<RankedGuiDataMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(RankedGuiDataMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> this.handleClientSide(message));
         return null;
      }

      @SideOnly(Side.CLIENT)
      private void handleClientSide(RankedGuiDataMessage message) {
         boolean isPlaced = message.placement == null || message.placement.isEmpty();
         RankedHudRenderer.updatePlayerData(message.rank, message.elo, (String)null, isPlaced);
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.currentScreen instanceof GuiRankedMenu) {
            GuiRankedMenu gui = (GuiRankedMenu)mc.currentScreen;
            gui.updateData(message.rank, message.elo, message.wins, message.losses, message.winStreak, message.isQueued, message.isInMatch, message.queueTime, message.queueSize, message.placement.isEmpty() ? null : message.placement);
            gui.updateDailyTask(message.dailyWinsToday, message.dailyWinsRequired, message.dailyTaskCompleted);
            gui.updatePendingRewards(message.hasPendingRewards);
            gui.updateRaidState(message.isInRaid, message.currentRaidBoss);
         }

      }
   }
}
