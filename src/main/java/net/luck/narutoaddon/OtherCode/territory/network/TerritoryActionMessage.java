package net.luck.narutoaddon.OtherCode.territory.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TerritoryActionMessage implements IMessage {
   private String action;
   private String zoneId;

   public TerritoryActionMessage() {
      this.action = "";
      this.zoneId = "";
   }

   public TerritoryActionMessage(String action) {
      this.action = action != null ? action : "";
      this.zoneId = "";
   }

   public TerritoryActionMessage(String action, String zoneId) {
      this.action = action != null ? action : "";
      this.zoneId = zoneId != null ? zoneId : "";
   }

   public void toBytes(ByteBuf buf) {
   }

   public void fromBytes(ByteBuf buf) {
      this.action = "";
      this.zoneId = "";
   }

   public String getAction() {
      return this.action;
   }

   public String getZoneId() {
      return this.zoneId;
   }

   public static class Handler implements IMessageHandler<TerritoryActionMessage, IMessage> {
      public IMessage onMessage(TerritoryActionMessage message, MessageContext ctx) {
         return null;
      }
   }
}
