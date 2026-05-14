
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopSyncMessage implements IMessage {
   private long ryoBalance;
   private int tokenBalance;
   private int crystalBalance;
   private int loginStreak;
   private int overflowCount;
   private boolean clanTabUnlocked;
   private boolean so6pTabUnlocked;
   private boolean akamichiUnlocked;
   private boolean freeRollAvailable;
   private boolean firstPurchaseBonusAvailable;
   private boolean weekendPromoSO6PAvailable;
   private Map<String, Integer> pityCounters;
   private List<CrateSyncInfo> crates;

   public ShopSyncMessage() {
      this.crates = new ArrayList();
      this.pityCounters = new HashMap();
   }

   public ShopSyncMessage(long ryoBalance, int tokenBalance, int crystalBalance, int loginStreak, int overflowCount, boolean clanTabUnlocked, boolean so6pTabUnlocked, boolean akamichiUnlocked, boolean freeRollAvailable, boolean firstPurchaseBonusAvailable, boolean weekendPromoSO6PAvailable, Map<String, Integer> pityCounters, List<CrateSyncInfo> crates) {
      this.ryoBalance = ryoBalance;
      this.tokenBalance = tokenBalance;
      this.crystalBalance = crystalBalance;
      this.loginStreak = loginStreak;
      this.overflowCount = overflowCount;
      this.clanTabUnlocked = clanTabUnlocked;
      this.so6pTabUnlocked = so6pTabUnlocked;
      this.akamichiUnlocked = akamichiUnlocked;
      this.freeRollAvailable = freeRollAvailable;
      this.firstPurchaseBonusAvailable = firstPurchaseBonusAvailable;
      this.weekendPromoSO6PAvailable = weekendPromoSO6PAvailable;
      this.pityCounters = (Map<String, Integer>)(pityCounters != null ? pityCounters : new HashMap());
      this.crates = (List<CrateSyncInfo>)(crates != null ? crates : new ArrayList());
   }

   public void fromBytes(ByteBuf buf) {
      this.ryoBalance = buf.readLong();
      this.tokenBalance = buf.readInt();
      this.crystalBalance = buf.readInt();
      this.loginStreak = buf.readInt();
      this.overflowCount = buf.readInt();
      this.clanTabUnlocked = buf.readBoolean();
      this.so6pTabUnlocked = buf.readBoolean();
      this.akamichiUnlocked = buf.readBoolean();
      this.freeRollAvailable = buf.readBoolean();
      this.firstPurchaseBonusAvailable = buf.readBoolean();
      this.weekendPromoSO6PAvailable = buf.readBoolean();
      int pityCount = buf.readInt();
      this.pityCounters = new HashMap();

      for(int i = 0; i < pityCount; ++i) {
         String crateId = ByteBufUtils.readUTF8String(buf);
         int count = buf.readInt();
         this.pityCounters.put(crateId, count);
      }

      int crateCount = buf.readInt();
      this.crates = new ArrayList();

      for(int i = 0; i < crateCount; ++i) {
         CrateSyncInfo info = new CrateSyncInfo();
         info.crateId = ByteBufUtils.readUTF8String(buf);
         info.displayName = ByteBufUtils.readUTF8String(buf);
         info.description = ByteBufUtils.readUTF8String(buf);
         info.categoryOrdinal = buf.readByte();
         info.price = buf.readLong();
         info.crystalPrice = buf.readInt();
         info.directPurchase = buf.readBoolean();
         info.iconItemId = ByteBufUtils.readUTF8String(buf);
         info.iconMeta = buf.readInt();
         int entryCount = buf.readInt();
         info.lootEntries = new ArrayList();

         for(int j = 0; j < entryCount; ++j) {
            LootEntrySyncInfo entry = new LootEntrySyncInfo();
            entry.itemId = ByteBufUtils.readUTF8String(buf);
            entry.itemMeta = buf.readInt();
            entry.itemCount = buf.readInt();
            entry.rarityOrdinal = buf.readByte();
            entry.weight = buf.readDouble();
            entry.dropChancePct = buf.readFloat();
            entry.hasDisplayNameOverride = buf.readBoolean();
            if (entry.hasDisplayNameOverride) {
               entry.displayNameOverride = ByteBufUtils.readUTF8String(buf);
            }

            entry.hasItemNbt = buf.readBoolean();
            if (entry.hasItemNbt) {
               entry.itemNbt = ByteBufUtils.readTag(buf);
            }

            int bundleCount = buf.readInt();
            entry.bundleItems = new ArrayList();

            for(int k = 0; k < bundleCount; ++k) {
               BundleItemSyncInfo bi = new BundleItemSyncInfo();
               bi.itemId = ByteBufUtils.readUTF8String(buf);
               bi.itemMeta = buf.readInt();
               bi.itemCount = buf.readInt();
               bi.hasNbt = buf.readBoolean();
               if (bi.hasNbt) {
                  bi.itemNbt = ByteBufUtils.readTag(buf);
               }

               entry.bundleItems.add(bi);
            }

            info.lootEntries.add(entry);
         }

         this.crates.add(info);
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeLong(this.ryoBalance);
      buf.writeInt(this.tokenBalance);
      buf.writeInt(this.crystalBalance);
      buf.writeInt(this.loginStreak);
      buf.writeInt(this.overflowCount);
      buf.writeBoolean(this.clanTabUnlocked);
      buf.writeBoolean(this.so6pTabUnlocked);
      buf.writeBoolean(this.akamichiUnlocked);
      buf.writeBoolean(this.freeRollAvailable);
      buf.writeBoolean(this.firstPurchaseBonusAvailable);
      buf.writeBoolean(this.weekendPromoSO6PAvailable);
      buf.writeInt(this.pityCounters.size());

      for(Map.Entry<String, Integer> entry : this.pityCounters.entrySet()) {
         ByteBufUtils.writeUTF8String(buf, (String)entry.getKey());
         buf.writeInt((Integer)entry.getValue());
      }

      buf.writeInt(this.crates.size());

      for(CrateSyncInfo info : this.crates) {
         ByteBufUtils.writeUTF8String(buf, info.crateId != null ? info.crateId : "");
         ByteBufUtils.writeUTF8String(buf, info.displayName != null ? info.displayName : "");
         ByteBufUtils.writeUTF8String(buf, info.description != null ? info.description : "");
         buf.writeByte(info.categoryOrdinal);
         buf.writeLong(info.price);
         buf.writeInt(info.crystalPrice);
         buf.writeBoolean(info.directPurchase);
         ByteBufUtils.writeUTF8String(buf, info.iconItemId != null ? info.iconItemId : "");
         buf.writeInt(info.iconMeta);
         List<LootEntrySyncInfo> entries = (List<LootEntrySyncInfo>)(info.lootEntries != null ? info.lootEntries : new ArrayList());
         buf.writeInt(entries.size());

         for(LootEntrySyncInfo entry : entries) {
            ByteBufUtils.writeUTF8String(buf, entry.itemId != null ? entry.itemId : "");
            buf.writeInt(entry.itemMeta);
            buf.writeInt(entry.itemCount);
            buf.writeByte(entry.rarityOrdinal);
            buf.writeDouble(entry.weight);
            buf.writeFloat(entry.dropChancePct);
            buf.writeBoolean(entry.hasDisplayNameOverride);
            if (entry.hasDisplayNameOverride) {
               ByteBufUtils.writeUTF8String(buf, entry.displayNameOverride != null ? entry.displayNameOverride : "");
            }

            buf.writeBoolean(entry.hasItemNbt);
            if (entry.hasItemNbt && entry.itemNbt != null) {
               ByteBufUtils.writeTag(buf, entry.itemNbt);
            }

            List<BundleItemSyncInfo> bundleItems = (List<BundleItemSyncInfo>)(entry.bundleItems != null ? entry.bundleItems : new ArrayList());
            buf.writeInt(bundleItems.size());

            for(BundleItemSyncInfo bi : bundleItems) {
               ByteBufUtils.writeUTF8String(buf, bi.itemId != null ? bi.itemId : "");
               buf.writeInt(bi.itemMeta);
               buf.writeInt(bi.itemCount);
               buf.writeBoolean(bi.hasNbt);
               if (bi.hasNbt && bi.itemNbt != null) {
                  ByteBufUtils.writeTag(buf, bi.itemNbt);
               }
            }
         }
      }

   }

   public static class CrateSyncInfo {
      public String crateId;
      public String displayName;
      public String description;
      public byte categoryOrdinal;
      public long price;
      public int crystalPrice;
      public boolean directPurchase;
      public String iconItemId;
      public int iconMeta;
      public List<LootEntrySyncInfo> lootEntries;
   }

   public static class LootEntrySyncInfo {
      public String itemId;
      public int itemMeta;
      public int itemCount;
      public byte rarityOrdinal;
      public double weight;
      public float dropChancePct;
      public boolean hasDisplayNameOverride;
      public String displayNameOverride;
      public boolean hasItemNbt;
      public NBTTagCompound itemNbt;
      public List<BundleItemSyncInfo> bundleItems;
   }

   public static class BundleItemSyncInfo {
      public String itemId;
      public int itemMeta;
      public int itemCount;
      public boolean hasNbt;
      public NBTTagCompound itemNbt;
   }

   public static class Handler implements IMessageHandler<ShopSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(ShopSyncMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            ShopClientData.ryoBalance = message.ryoBalance;
            ShopClientData.tokenBalance = message.tokenBalance;
            ShopClientData.crystalBalance = message.crystalBalance;
            ShopClientData.loginStreak = message.loginStreak;
            ShopClientData.overflowCount = message.overflowCount;
            ShopClientData.clanTabUnlocked = message.clanTabUnlocked;
            ShopClientData.so6pTabUnlocked = message.so6pTabUnlocked;
            ShopClientData.akamichiUnlocked = message.akamichiUnlocked;
            ShopClientData.freeRollAvailable = message.freeRollAvailable;
            ShopClientData.firstPurchaseBonusAvailable = message.firstPurchaseBonusAvailable;
            ShopClientData.weekendPromoSO6PAvailable = message.weekendPromoSO6PAvailable;
            ShopClientData.pityCounters = new HashMap(message.pityCounters);
            List<ShopClientData.CrateClientInfo> clientCrates = new ArrayList();

            for(CrateSyncInfo info : message.crates) {
               List<ShopClientData.LootEntryClientInfo> clientEntries = new ArrayList();
               if (info.lootEntries != null) {
                  for(LootEntrySyncInfo entry : info.lootEntries) {
                     List<ShopClientData.BundleItemClientInfo> clientBundles = new ArrayList();
                     if (entry.bundleItems != null) {
                        for(BundleItemSyncInfo bi : entry.bundleItems) {
                           clientBundles.add(new ShopClientData.BundleItemClientInfo(bi.itemId, bi.itemMeta, bi.itemCount, bi.hasNbt ? bi.itemNbt : null));
                        }
                     }

                     clientEntries.add(new ShopClientData.LootEntryClientInfo(entry.itemId, entry.itemMeta, entry.itemCount, entry.rarityOrdinal, entry.weight, entry.dropChancePct, entry.hasDisplayNameOverride ? entry.displayNameOverride : null, entry.hasItemNbt ? entry.itemNbt : null, clientBundles));
                  }
               }

               clientCrates.add(new ShopClientData.CrateClientInfo(info.crateId, info.displayName, info.description, info.categoryOrdinal, info.price, info.crystalPrice, info.directPurchase, info.iconItemId, info.iconMeta, clientEntries));
            }

            ShopClientData.availableCrates = clientCrates;
         });
         return null;
      }
   }
}
