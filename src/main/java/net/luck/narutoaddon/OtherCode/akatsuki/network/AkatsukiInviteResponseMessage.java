package net.luck.narutoaddon.OtherCode.akatsuki.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class AkatsukiInviteResponseMessage implements IMessage {
   private boolean accepted;

   public AkatsukiInviteResponseMessage() {
   }

   public AkatsukiInviteResponseMessage(boolean accepted) {
      this.accepted = accepted;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.accepted);
   }

   public void fromBytes(ByteBuf buf) {
      this.accepted = buf.readBoolean();
   }

   public static class Handler implements IMessageHandler<AkatsukiInviteResponseMessage, IMessage> {
      public IMessage onMessage(AkatsukiInviteResponseMessage msg, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
            if (msg.accepted) {
               AkatsukiManager.getInstance().acceptInvite(player);
            } else {
               AkatsukiManager.getInstance().declineInvite(player);
            }

         });
         return null;
      }
   }
}
