
package net.luck.narutoaddon.OtherCode.shop.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ShopHistorySyncMessage implements IMessage {
   private List<HistoryEntry> entries;

   public ShopHistorySyncMessage() {
      this.entries = new ArrayList();
   }

   public ShopHistorySyncMessage(List<HistoryEntry> entries) {
      this.entries = (List<HistoryEntry>)(entries != null ? entries : new ArrayList());
   }

   public void fromBytes(ByteBuf buf) {
      int count = buf.readInt();
      this.entries = new ArrayList();

      for(int i = 0; i < count; ++i) {
         HistoryEntry entry = new HistoryEntry();
         entry.crateDisplayName = ByteBufUtils.readUTF8String(buf);
         entry.itemDisplayName = ByteBufUtils.readUTF8String(buf);
         entry.rarityOrdinal = buf.readByte();
         entry.timestamp = buf.readLong();
         this.entries.add(entry);
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.entries.size());

      for(HistoryEntry entry : this.entries) {
         ByteBufUtils.writeUTF8String(buf, entry.crateDisplayName != null ? entry.crateDisplayName : "");
         ByteBufUtils.writeUTF8String(buf, entry.itemDisplayName != null ? entry.itemDisplayName : "");
         buf.writeByte(entry.rarityOrdinal);
         buf.writeLong(entry.timestamp);
      }

   }

   public static class HistoryEntry {
      public String crateDisplayName;
      public String itemDisplayName;
      public byte rarityOrdinal;
      public long timestamp;

      public HistoryEntry() {
      }

      public HistoryEntry(String crateDisplayName, String itemDisplayName, byte rarityOrdinal, long timestamp) {
         this.crateDisplayName = crateDisplayName;
         this.itemDisplayName = itemDisplayName;
         this.rarityOrdinal = rarityOrdinal;
         this.timestamp = timestamp;
      }
   }

   public static class Handler implements IMessageHandler<ShopHistorySyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(ShopHistorySyncMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            List<ShopClientData.HistoryEntryInfo> clientHistory = new ArrayList();

            for(HistoryEntry entry : message.entries) {
               clientHistory.add(new ShopClientData.HistoryEntryInfo(entry.crateDisplayName, entry.itemDisplayName, entry.rarityOrdinal, entry.timestamp));
            }

            ShopClientData.history = clientHistory;
         });
         return null;
      }
   }
}
