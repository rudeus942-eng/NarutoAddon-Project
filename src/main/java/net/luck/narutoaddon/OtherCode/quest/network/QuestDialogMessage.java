
package net.luck.narutoaddon.OtherCode.quest.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.gui.QuestDialogGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class QuestDialogMessage implements IMessage {
   private String npcName;
   private String[] dialogLines;
   private String[] dialogResponses;
   private int stepIndex;
   private int correctResponseIndex;

   public QuestDialogMessage() {
   }

   public QuestDialogMessage(String npcName, String[] dialogLines, String[] dialogResponses, int stepIndex) {
      this(npcName, dialogLines, dialogResponses, stepIndex, 0);
   }

   public QuestDialogMessage(String npcName, String[] dialogLines, String[] dialogResponses, int stepIndex, int correctResponseIndex) {
      this.npcName = npcName;
      this.dialogLines = dialogLines;
      this.dialogResponses = dialogResponses;
      this.stepIndex = stepIndex;
      this.correctResponseIndex = correctResponseIndex;
   }

   public void fromBytes(ByteBuf buf) {
      this.npcName = ByteBufUtils.readUTF8String(buf);
      this.stepIndex = buf.readInt();
      int lineCount = buf.readInt();
      this.dialogLines = new String[lineCount];

      for(int i = 0; i < lineCount; ++i) {
         this.dialogLines[i] = ByteBufUtils.readUTF8String(buf);
      }

      int responseCount = buf.readInt();
      this.dialogResponses = new String[responseCount];

      for(int i = 0; i < responseCount; ++i) {
         this.dialogResponses[i] = ByteBufUtils.readUTF8String(buf);
      }

      if (buf.isReadable()) {
         this.correctResponseIndex = buf.readInt();
      } else {
         this.correctResponseIndex = 0;
      }

   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.npcName != null ? this.npcName : "");
      buf.writeInt(this.stepIndex);
      buf.writeInt(this.dialogLines != null ? this.dialogLines.length : 0);
      if (this.dialogLines != null) {
         for(String line : this.dialogLines) {
            ByteBufUtils.writeUTF8String(buf, line != null ? line : "");
         }
      }

      buf.writeInt(this.dialogResponses != null ? this.dialogResponses.length : 0);
      if (this.dialogResponses != null) {
         for(String response : this.dialogResponses) {
            ByteBufUtils.writeUTF8String(buf, response != null ? response : "");
         }
      }

      buf.writeInt(this.correctResponseIndex);
   }

   public static class Handler implements IMessageHandler<QuestDialogMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(QuestDialogMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            QuestClientData.setDialog(message.npcName, message.dialogLines, message.dialogResponses, message.stepIndex, message.correctResponseIndex);
            Minecraft.getMinecraft().displayGuiScreen(new QuestDialogGui());
         });
         return null;
      }
   }
}
