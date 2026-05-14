
package net.luck.narutoaddon.OtherCode.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.gui.GuiRankedTitles;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class RankedTitlesDataMessage implements IMessage {
   private List<String> collectedTitles;
   private String activeTitle;

   public RankedTitlesDataMessage() {
      this.collectedTitles = new ArrayList();
      this.activeTitle = "";
   }

   public RankedTitlesDataMessage(List<String> titles, String active) {
      this.collectedTitles = (List<String>)(titles != null ? titles : new ArrayList());
      this.activeTitle = active != null ? active : "";
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.collectedTitles.size());

      for(String title : this.collectedTitles) {
         ByteBufUtils.writeUTF8String(buf, title != null ? title : "");
      }

      ByteBufUtils.writeUTF8String(buf, this.activeTitle != null ? this.activeTitle : "");
   }

   public void fromBytes(ByteBuf buf) {
      int size = buf.readInt();
      this.collectedTitles = new ArrayList();

      for(int i = 0; i < size; ++i) {
         this.collectedTitles.add(ByteBufUtils.readUTF8String(buf));
      }

      this.activeTitle = ByteBufUtils.readUTF8String(buf);
   }

   public static class Handler implements IMessageHandler<RankedTitlesDataMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(RankedTitlesDataMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> this.handleClientSide(message));
         return null;
      }

      @SideOnly(Side.CLIENT)
      private void handleClientSide(RankedTitlesDataMessage msg) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.currentScreen instanceof GuiRankedTitles) {
            GuiRankedTitles gui = (GuiRankedTitles)mc.currentScreen;
            gui.updateData(msg.collectedTitles, msg.activeTitle);
         } else {
            mc.displayGuiScreen(new GuiRankedTitles(msg.collectedTitles, msg.activeTitle));
         }

      }
   }
}
