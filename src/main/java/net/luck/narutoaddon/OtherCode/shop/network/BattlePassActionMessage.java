
package net.luck.narutoaddon.OtherCode.shop.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class BattlePassActionMessage implements IMessage {
   public static final byte ACTION_PURCHASE_PASS = 0;
   public static final byte ACTION_CLAIM_TIER = 1;
   public static final byte ACTION_SKIP_LEVEL = 2;
   public static final byte ACTION_TOGGLE_KILL_EFFECT = 3;
   private byte action;
   private String payload;

   public BattlePassActionMessage() {
      this.payload = "";
   }

   public BattlePassActionMessage(byte action, String payload) {
      this.action = action;
      this.payload = payload != null ? payload : "";
   }

   public BattlePassActionMessage(byte action) {
      this(action, "");
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readByte();
      this.payload = ByteBufUtils.readUTF8String(buf);
   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.action);
      ByteBufUtils.writeUTF8String(buf, this.payload != null ? this.payload : "");
   }

   public static class Handler implements IMessageHandler<BattlePassActionMessage, IMessage> {
      public IMessage onMessage(BattlePassActionMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         WorldServer world = (WorldServer)player.world;
         world.addScheduledTask(() -> {
            UUID playerId = player.getUniqueID();
            BattlePassManager bpManager = BattlePassManager.getInstance();
            switch (message.action) {
               case 0:
                  int tier = parseIntSafe(message.payload, 0);
                  if (tier >= 1 && tier <= 2) {
                     bpManager.purchasePass(playerId, tier, world);
                  }
                  break;
               case 1:
                  int tier = parseIntSafe(message.payload, 0);
                  if (tier >= 1 && tier <= 20) {
                     bpManager.claimTier(playerId, tier, world);
                  }
                  break;
               case 2:
                  bpManager.skipLevel(playerId, world);
                  break;
               case 3:
                  if (message.payload != null && !message.payload.isEmpty() && message.payload.length() <= 64) {
                     bpManager.toggleKillEffect(playerId, message.payload, world);
                  }
            }

            ShopNetworkHelper.sendBattlePassSync(player);
            ShopNetworkHelper.sendShopSync(player);
         });
         return null;
      }

      private static int parseIntSafe(String s, int defaultVal) {
         try {
            return Integer.parseInt(s);
         } catch (NumberFormatException var3) {
            return defaultVal;
         }
      }
   }
}
