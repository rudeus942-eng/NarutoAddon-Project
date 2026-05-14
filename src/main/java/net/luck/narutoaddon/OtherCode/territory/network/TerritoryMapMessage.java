package net.luck.narutoaddon.OtherCode.territory.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TerritoryMapMessage implements IMessage {
   private int mapSize = 0;
   private int[] pixels;

   public TerritoryMapMessage() {
      this.pixels = new int[0];
   }

   public TerritoryMapMessage(int[] pixels) {
      this.pixels = pixels != null ? pixels : new int[0];
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(0);
      buf.writeInt(0);
   }

   public void fromBytes(ByteBuf buf) {
      this.mapSize = buf.readInt();
      int len = buf.readInt();
      if (len > 0) {
         buf.skipBytes(len);
      }

      this.pixels = new int[0];
   }

   public int getMapSize() {
      return this.mapSize;
   }

   public int[] getPixels() {
      return this.pixels;
   }

   public static class Handler implements IMessageHandler<TerritoryMapMessage, IMessage> {
      public IMessage onMessage(TerritoryMapMessage msg, MessageContext ctx) {
         return null;
      }
   }
}
