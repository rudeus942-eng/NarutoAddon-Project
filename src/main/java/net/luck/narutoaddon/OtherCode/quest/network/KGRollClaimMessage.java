
package net.luck.narutoaddon.OtherCode.quest.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class KGRollClaimMessage implements IMessage {
   public void fromBytes(ByteBuf buf) {
   }

   public void toBytes(ByteBuf buf) {
   }

   public static class Handler implements IMessageHandler<KGRollClaimMessage, IMessage> {
      public IMessage onMessage(KGRollClaimMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         player.getServerWorld().addScheduledTask(() -> QuestManager.getInstance().claimKGRoll(player));
         return null;
      }
   }
}
