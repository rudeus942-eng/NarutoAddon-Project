
package net.luck.narutoaddon.OtherCode.shop.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ShopCrateResultMessage implements IMessage {
   private String crateId;
   private long newBalance;
   private String wonItemId;
   private int wonItemMeta;
   private int wonItemCount;
   private byte wonRarity;
   private String wonDisplayName;
   private int targetIndex;
   private List<StripEntry> displayStrip;
   private boolean isDuplicate;
   private int duplicateTokensAwarded;
   private int dupeCrystalsAwarded;
   private String wonBundleDisplayName;
   private List<BundleResultItem> wonBundleItems;

   public ShopCrateResultMessage() {
      this.crateId = "";
      this.wonItemId = "";
      this.wonDisplayName = "";
      this.wonBundleDisplayName = "";
      this.displayStrip = new ArrayList();
      this.wonBundleItems = new ArrayList();
   }

   public ShopCrateResultMessage(String crateId, long newBalance, String wonItemId, int wonItemMeta, int wonItemCount, byte wonRarity, String wonDisplayName, int targetIndex, List<StripEntry> displayStrip) {
      this(crateId, newBalance, wonItemId, wonItemMeta, wonItemCount, wonRarity, wonDisplayName, targetIndex, displayStrip, false, 0, 0, (String)null, (List)null);
   }

   public ShopCrateResultMessage(String crateId, long newBalance, String wonItemId, int wonItemMeta, int wonItemCount, byte wonRarity, String wonDisplayName, int targetIndex, List<StripEntry> displayStrip, boolean isDuplicate, int duplicateTokensAwarded) {
      this(crateId, newBalance, wonItemId, wonItemMeta, wonItemCount, wonRarity, wonDisplayName, targetIndex, displayStrip, isDuplicate, duplicateTokensAwarded, 0, (String)null, (List)null);
   }

   public ShopCrateResultMessage(String crateId, long newBalance, String wonItemId, int wonItemMeta, int wonItemCount, byte wonRarity, String wonDisplayName, int targetIndex, List<StripEntry> displayStrip, boolean isDuplicate, int duplicateTokensAwarded, String wonBundleDisplayName, List<BundleResultItem> wonBundleItems) {
      this(crateId, newBalance, wonItemId, wonItemMeta, wonItemCount, wonRarity, wonDisplayName, targetIndex, displayStrip, isDuplicate, duplicateTokensAwarded, 0, wonBundleDisplayName, wonBundleItems);
   }

   public ShopCrateResultMessage(String crateId, long newBalance, String wonItemId, int wonItemMeta, int wonItemCount, byte wonRarity, String wonDisplayName, int targetIndex, List<StripEntry> displayStrip, boolean isDuplicate, int duplicateTokensAwarded, int dupeCrystalsAwarded, String wonBundleDisplayName, List<BundleResultItem> wonBundleItems) {
      this.crateId = crateId != null ? crateId : "";
      this.newBalance = newBalance;
      this.wonItemId = wonItemId != null ? wonItemId : "";
      this.wonItemMeta = wonItemMeta;
      this.wonItemCount = wonItemCount;
      this.wonRarity = wonRarity;
      this.wonDisplayName = wonDisplayName != null ? wonDisplayName : "";
      this.targetIndex = targetIndex;
      this.displayStrip = (List<StripEntry>)(displayStrip != null ? displayStrip : new ArrayList());
      this.isDuplicate = isDuplicate;
      this.duplicateTokensAwarded = duplicateTokensAwarded;
      this.dupeCrystalsAwarded = dupeCrystalsAwarded;
      this.wonBundleDisplayName = wonBundleDisplayName != null ? wonBundleDisplayName : "";
      this.wonBundleItems = (List<BundleResultItem>)(wonBundleItems != null ? wonBundleItems : new ArrayList());
   }

   public void fromBytes(ByteBuf buf) {
      this.crateId = ByteBufUtils.readUTF8String(buf);
      this.newBalance = buf.readLong();
      this.wonItemId = ByteBufUtils.readUTF8String(buf);
      this.wonItemMeta = buf.readInt();
      this.wonItemCount = buf.readInt();
      this.wonRarity = buf.readByte();
      this.wonDisplayName = ByteBufUtils.readUTF8String(buf);
      this.targetIndex = buf.readInt();
      this.isDuplicate = buf.readBoolean();
      this.duplicateTokensAwarded = buf.readInt();
      this.dupeCrystalsAwarded = buf.readInt();
      int stripSize = buf.readInt();
      this.displayStrip = new ArrayList();

      for(int i = 0; i < stripSize; ++i) {
         StripEntry entry = new StripEntry();
         entry.itemId = ByteBufUtils.readUTF8String(buf);
         entry.itemMeta = buf.readInt();
         entry.itemCount = buf.readInt();
         entry.rarityOrdinal = buf.readByte();
         entry.weight = buf.readDouble();
         entry.dropChancePct = buf.readFloat();
         this.displayStrip.add(entry);
      }

      this.wonBundleDisplayName = ByteBufUtils.readUTF8String(buf);
      int bundleCount = buf.readInt();
      this.wonBundleItems = new ArrayList();

      for(int i = 0; i < bundleCount; ++i) {
         BundleResultItem item = new BundleResultItem();
         item.itemId = ByteBufUtils.readUTF8String(buf);
         item.itemMeta = buf.readInt();
         item.itemCount = buf.readInt();
         this.wonBundleItems.add(item);
      }

   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.crateId);
      buf.writeLong(this.newBalance);
      ByteBufUtils.writeUTF8String(buf, this.wonItemId);
      buf.writeInt(this.wonItemMeta);
      buf.writeInt(this.wonItemCount);
      buf.writeByte(this.wonRarity);
      ByteBufUtils.writeUTF8String(buf, this.wonDisplayName);
      buf.writeInt(this.targetIndex);
      buf.writeBoolean(this.isDuplicate);
      buf.writeInt(this.duplicateTokensAwarded);
      buf.writeInt(this.dupeCrystalsAwarded);
      buf.writeInt(this.displayStrip.size());

      for(StripEntry entry : this.displayStrip) {
         ByteBufUtils.writeUTF8String(buf, entry.itemId != null ? entry.itemId : "");
         buf.writeInt(entry.itemMeta);
         buf.writeInt(entry.itemCount);
         buf.writeByte(entry.rarityOrdinal);
         buf.writeDouble(entry.weight);
         buf.writeFloat(entry.dropChancePct);
      }

      ByteBufUtils.writeUTF8String(buf, this.wonBundleDisplayName != null ? this.wonBundleDisplayName : "");
      buf.writeInt(this.wonBundleItems.size());

      for(BundleResultItem item : this.wonBundleItems) {
         ByteBufUtils.writeUTF8String(buf, item.itemId != null ? item.itemId : "");
         buf.writeInt(item.itemMeta);
         buf.writeInt(item.itemCount);
      }

   }

   public static class StripEntry {
      public String itemId;
      public int itemMeta;
      public int itemCount;
      public byte rarityOrdinal;
      public double weight;
      public float dropChancePct;

      public StripEntry() {
      }

      public StripEntry(String itemId, int itemMeta, int itemCount, byte rarityOrdinal, double weight, float dropChancePct) {
         this.itemId = itemId;
         this.itemMeta = itemMeta;
         this.itemCount = itemCount;
         this.rarityOrdinal = rarityOrdinal;
         this.weight = weight;
         this.dropChancePct = dropChancePct;
      }
   }

   public static class BundleResultItem {
      public String itemId;
      public int itemMeta;
      public int itemCount;

      public BundleResultItem() {
      }

      public BundleResultItem(String itemId, int itemMeta, int itemCount) {
         this.itemId = itemId;
         this.itemMeta = itemMeta;
         this.itemCount = itemCount;
      }
   }

   public static class Handler implements IMessageHandler<ShopCrateResultMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(ShopCrateResultMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            List<ShopClientData.LootEntryClientInfo> clientStrip = new ArrayList();

            for(StripEntry entry : message.displayStrip) {
               clientStrip.add(new ShopClientData.LootEntryClientInfo(entry.itemId, entry.itemMeta, entry.itemCount, entry.rarityOrdinal, entry.weight, entry.dropChancePct));
            }

            List<ShopClientData.BundleItemClientInfo> clientBundleItems = new ArrayList();

            for(BundleResultItem item : message.wonBundleItems) {
               clientBundleItems.add(new ShopClientData.BundleItemClientInfo(item.itemId, item.itemMeta, item.itemCount, (NBTTagCompound)null));
            }

            String bundleDisplayName = message.wonBundleDisplayName;
            if (bundleDisplayName != null && bundleDisplayName.isEmpty()) {
               bundleDisplayName = null;
            }

            ShopClientData.ryoBalance = message.newBalance;
            ShopClientData.CrateResultData resultData = new ShopClientData.CrateResultData(message.crateId, message.newBalance, message.wonItemId, message.wonItemMeta, message.wonItemCount, message.wonRarity, message.wonDisplayName, clientStrip, message.targetIndex, message.isDuplicate, message.duplicateTokensAwarded, message.dupeCrystalsAwarded, bundleDisplayName, clientBundleItems);
            if (ShopClientData.pendingResult != null && !ShopClientData.pendingResult.consumed) {
               ShopClientData.multiRollQueue.add(resultData);
            } else {
               ShopClientData.pendingDuplicate = message.isDuplicate;
               ShopClientData.pendingDuplicateTokens = message.duplicateTokensAwarded;
               ShopClientData.pendingDupeCrystals = message.dupeCrystalsAwarded;
               ShopClientData.pendingResult = resultData;
               if (ShopClientData.multiRollTotal == 0) {
                  ShopClientData.multiRollCurrent = 1;
               }
            }

            int totalQueued = 1 + ShopClientData.multiRollQueue.size();
            if (totalQueued > ShopClientData.multiRollTotal) {
               ShopClientData.multiRollTotal = totalQueued;
               ShopClientData.multiRollCurrent = ShopClientData.multiRollTotal - ShopClientData.multiRollQueue.size();
            }

         });
         return null;
      }
   }
}
