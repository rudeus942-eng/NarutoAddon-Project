
package net.luck.narutoaddon.OtherCode.raid.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RaidWindowSyncMessage implements IMessage {
   private boolean windowOpen;
   private long nextEventTimeMs;

   public RaidWindowSyncMessage() {
   }

   public RaidWindowSyncMessage(boolean windowOpen, long nextEventTimeMs) {
      this.windowOpen = windowOpen;
      this.nextEventTimeMs = nextEventTimeMs;
   }

   public void fromBytes(ByteBuf buf) {
      this.windowOpen = buf.readBoolean();
      this.nextEventTimeMs = buf.readLong();
   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.windowOpen);
      buf.writeLong(this.nextEventTimeMs);
   }

   public boolean isWindowOpen() {
      return this.windowOpen;
   }

   public long getNextEventTimeMs() {
      return this.nextEventTimeMs;
   }

   public static class Handler implements IMessageHandler<RaidWindowSyncMessage, IMessage> {
      public IMessage onMessage(RaidWindowSyncMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> handleOnClient(message));
         return null;
      }

      @SideOnly(Side.CLIENT)
      private static void handleOnClient(RaidWindowSyncMessage message) {
         RaidClientData.setQueueWindowState(message.windowOpen, message.nextEventTimeMs);
      }
   }
}
