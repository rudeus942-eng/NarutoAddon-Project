
package net.luck.narutoaddon.OtherCode.quest.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.gui.KGRollClientData;
import net.luck.narutoaddon.OtherCode.quest.gui.KGRollGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class KGRollResultMessage implements IMessage {
   private String wonItemId;
   private String wonDisplayName;
   private int wonRarityIndex;
   private int targetIndex;
   private List<StripEntryData> displayStrip;

   public KGRollResultMessage() {
   }

   public KGRollResultMessage(String wonItemId, String wonDisplayName, int wonRarityIndex, int targetIndex, List<StripEntryData> displayStrip) {
      this.wonItemId = wonItemId;
      this.wonDisplayName = wonDisplayName;
      this.wonRarityIndex = wonRarityIndex;
      this.targetIndex = targetIndex;
      this.displayStrip = displayStrip;
   }

   public void fromBytes(ByteBuf buf) {
      this.wonItemId = ByteBufUtils.readUTF8String(buf);
      this.wonDisplayName = ByteBufUtils.readUTF8String(buf);
      this.wonRarityIndex = buf.readByte();
      this.targetIndex = buf.readByte();
      int count = buf.readByte();
      this.displayStrip = new ArrayList(count);

      for(int i = 0; i < count; ++i) {
         String itemId = ByteBufUtils.readUTF8String(buf);
         String name = ByteBufUtils.readUTF8String(buf);
         int rarity = buf.readByte();
         this.displayStrip.add(new StripEntryData(itemId, name, rarity));
      }

   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.wonItemId);
      ByteBufUtils.writeUTF8String(buf, this.wonDisplayName);
      buf.writeByte(this.wonRarityIndex);
      buf.writeByte(this.targetIndex);
      buf.writeByte(this.displayStrip.size());

      for(StripEntryData entry : this.displayStrip) {
         ByteBufUtils.writeUTF8String(buf, entry.itemId);
         ByteBufUtils.writeUTF8String(buf, entry.displayName);
         buf.writeByte(entry.rarityIndex);
      }

   }

   public static class StripEntryData {
      public final String itemId;
      public final String displayName;
      public final int rarityIndex;

      public StripEntryData(String itemId, String displayName, int rarityIndex) {
         this.itemId = itemId;
         this.displayName = displayName;
         this.rarityIndex = rarityIndex;
      }
   }

   public static class Handler implements IMessageHandler<KGRollResultMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(KGRollResultMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            List<KGRollClientData.StripEntry> strip = new ArrayList(message.displayStrip.size());

            for(StripEntryData entry : message.displayStrip) {
               strip.add(new KGRollClientData.StripEntry(entry.itemId, entry.displayName, entry.rarityIndex));
            }

            KGRollClientData.pendingResult = new KGRollClientData.KGRollResult(message.wonItemId, message.wonDisplayName, message.wonRarityIndex, strip, message.targetIndex);
            Minecraft.getMinecraft().displayGuiScreen(new KGRollGui());
         });
         return null;
      }
   }
}
