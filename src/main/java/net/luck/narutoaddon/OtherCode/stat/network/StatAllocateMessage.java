
package net.luck.narutoaddon.OtherCode.stat.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.stat.core.StatCategory;
import net.luck.narutoaddon.OtherCode.stat.core.StatElement;
import net.luck.narutoaddon.OtherCode.stat.core.StatManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class StatAllocateMessage implements IMessage {
   public static final byte ACTION_ALLOCATE = 0;
   public static final byte ACTION_RESPEC = 1;
   private byte action;
   private byte categoryId;
   private byte elementId;

   public StatAllocateMessage() {
   }

   public StatAllocateMessage(byte action, byte categoryId, byte elementId) {
      this.action = action;
      this.categoryId = categoryId;
      this.elementId = elementId;
   }

   public static StatAllocateMessage allocate(StatCategory category, StatElement element) {
      return new StatAllocateMessage((byte)0, (byte)category.getId(), (byte)element.getId());
   }

   public static StatAllocateMessage respec() {
      return new StatAllocateMessage((byte)1, (byte)0, (byte)0);
   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.action);
      buf.writeByte(this.categoryId);
      buf.writeByte(this.elementId);
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readByte();
      this.categoryId = buf.readByte();
      this.elementId = buf.readByte();
   }

   public static class Handler implements IMessageHandler<StatAllocateMessage, IMessage> {
      public IMessage onMessage(StatAllocateMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         player.getServerWorld().addScheduledTask(() -> {
            StatManager mgr = StatManager.getInstance();
            if (message.action == 0) {
               StatCategory cat = StatCategory.fromId(message.categoryId);
               StatElement el = StatElement.fromId(message.elementId);
               if (cat == null || el == null) {
                  return;
               }

               if (!mgr.allocate(player, cat, el)) {
                  player.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot allocate: not enough SP or at max level."));
               }
            } else if (message.action == 1) {
               String error = mgr.respec(player);
               if (error != null) {
                  player.sendMessage(new TextComponentString(TextFormatting.RED + error));
               }
            }

         });
         return null;
      }
   }
}
