
package net.luck.narutoaddon.OtherCode.raid.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.gui.GuiRaidMenu;
import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RaidQueueStatusMessage implements IMessage {
   private boolean inQueue;
   private int queueTime;
   private int queueSize;
   private String bossId;
   private int difficulty;
   private boolean inRaid;
   private String currentRaidBoss;
   private boolean hasPendingRewards;

   public RaidQueueStatusMessage() {
      this.inQueue = false;
      this.queueTime = 0;
      this.queueSize = 0;
      this.bossId = "";
      this.difficulty = 0;
      this.inRaid = false;
      this.currentRaidBoss = "";
      this.hasPendingRewards = false;
   }

   public RaidQueueStatusMessage(boolean inQueue, int queueTime, int queueSize, String bossId, int difficulty, boolean inRaid, String currentRaidBoss, boolean hasPendingRewards) {
      this.inQueue = inQueue;
      this.queueTime = queueTime;
      this.queueSize = queueSize;
      this.bossId = bossId;
      this.difficulty = difficulty;
      this.inRaid = inRaid;
      this.currentRaidBoss = currentRaidBoss != null ? currentRaidBoss : "";
      this.hasPendingRewards = hasPendingRewards;
   }

   public void fromBytes(ByteBuf buf) {
      this.inQueue = buf.readBoolean();
      this.queueTime = buf.readInt();
      this.queueSize = buf.readInt();
      this.bossId = ByteBufUtils.readUTF8String(buf);
      this.difficulty = buf.readInt();
      this.inRaid = buf.readBoolean();
      this.currentRaidBoss = ByteBufUtils.readUTF8String(buf);
      this.hasPendingRewards = buf.readBoolean();
   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.inQueue);
      buf.writeInt(this.queueTime);
      buf.writeInt(this.queueSize);
      ByteBufUtils.writeUTF8String(buf, this.bossId);
      buf.writeInt(this.difficulty);
      buf.writeBoolean(this.inRaid);
      ByteBufUtils.writeUTF8String(buf, this.currentRaidBoss);
      buf.writeBoolean(this.hasPendingRewards);
   }

   public static class Handler implements IMessageHandler<RaidQueueStatusMessage, IMessage> {
      public IMessage onMessage(RaidQueueStatusMessage message, MessageContext ctx) {
         if (ctx.side != Side.CLIENT) {
            return null;
         } else {
            Minecraft.getMinecraft().addScheduledTask(() -> this.handleClientMessage(message));
            return null;
         }
      }

      @SideOnly(Side.CLIENT)
      private void handleClientMessage(RaidQueueStatusMessage message) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.currentScreen instanceof GuiRaidMenu) {
            GuiRaidMenu gui = (GuiRaidMenu)mc.currentScreen;
            RaidDifficulty diff = RaidDifficulty.values()[Math.min(message.difficulty, RaidDifficulty.values().length - 1)];
            gui.updateQueueData(message.inQueue, message.queueTime, message.queueSize, message.bossId, diff);
            gui.updateRaidState(message.inRaid, message.currentRaidBoss);
            gui.updatePendingRewards(message.hasPendingRewards);
         }

      }
   }
}
