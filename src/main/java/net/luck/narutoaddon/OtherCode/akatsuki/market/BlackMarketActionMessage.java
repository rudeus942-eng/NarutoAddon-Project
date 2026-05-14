package net.luck.narutoaddon.OtherCode.akatsuki.market;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class BlackMarketActionMessage implements IMessage {
   private String itemId;
   private String targetZoneId;

   public BlackMarketActionMessage() {
   }

   public BlackMarketActionMessage(String itemId, String targetZoneId) {
      this.itemId = itemId;
      this.targetZoneId = targetZoneId != null ? targetZoneId : "";
   }

   public void fromBytes(ByteBuf buf) {
      this.itemId = ByteBufUtils.readUTF8String(buf);
      this.targetZoneId = ByteBufUtils.readUTF8String(buf);
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.itemId != null ? this.itemId : "");
      ByteBufUtils.writeUTF8String(buf, this.targetZoneId != null ? this.targetZoneId : "");
   }

   public static class Handler implements IMessageHandler<BlackMarketActionMessage, IMessage> {
      public IMessage onMessage(BlackMarketActionMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         player.getServerWorld().addScheduledTask(() -> {
            String itemId = message.itemId;
            String zoneId = message.targetZoneId;
            if (itemId != null && !itemId.isEmpty()) {
               BlackMarketItem item = BlackMarketRegistry.get(itemId);
               if (item == null) {
                  System.err.println("[BlackMarket] Unknown item: " + itemId + " from " + player.getName());
               } else if (item.isBuff() || zoneId != null && !zoneId.isEmpty()) {
                  String result = BlackMarketManager.getInstance().executePurchase(player, itemId, zoneId, player.getServerWorld());
                  if (result != null) {
                     AkatsukiManager.getInstance().syncToAllOnline(player.server);
                  }

               } else {
                  player.sendMessage(new TextComponentString("§cYou must select a target zone."));
               }
            } else {
               System.err.println("[BlackMarket] Received empty itemId from " + player.getName());
            }
         });
         return null;
      }
   }
}
