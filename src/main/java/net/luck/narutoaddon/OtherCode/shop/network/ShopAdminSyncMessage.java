
package net.luck.narutoaddon.OtherCode.shop.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.shop.gui.ShopAdminGui;
import net.minecraft.client.Minecraft;
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

public class ShopAdminSyncMessage implements IMessage {
   private String targetName;
   private String targetUUID;
   private long ryoBalance;
   private int tokenBalance;
   private int loginStreak;
   private int overflowCount;
   private long lifetimeSpent;
   private int lifetimeCratesOpened;
   private List<HistoryData> historyList;
   private List<String> ownedItemsList;
   private Map<String, Integer> pityMap;
   private List<TrackedItemData> trackedItemsList;
   private List<RestoreLogData> restoreLogList;

   public ShopAdminSyncMessage() {
      this.targetName = "";
      this.targetUUID = "";
      this.historyList = new ArrayList();
      this.ownedItemsList = new ArrayList();
      this.pityMap = new HashMap();
      this.trackedItemsList = new ArrayList();
      this.restoreLogList = new ArrayList();
   }

   public ShopAdminSyncMessage(String targetName, String targetUUID, long ryoBalance, int tokenBalance, int loginStreak, int overflowCount, long lifetimeSpent, int lifetimeCratesOpened, List<HistoryData> historyList, List<String> ownedItemsList, Map<String, Integer> pityMap) {
      this(targetName, targetUUID, ryoBalance, tokenBalance, loginStreak, overflowCount, lifetimeSpent, lifetimeCratesOpened, historyList, ownedItemsList, pityMap, new ArrayList(), new ArrayList());
   }

   public ShopAdminSyncMessage(String targetName, String targetUUID, long ryoBalance, int tokenBalance, int loginStreak, int overflowCount, long lifetimeSpent, int lifetimeCratesOpened, List<HistoryData> historyList, List<String> ownedItemsList, Map<String, Integer> pityMap, List<TrackedItemData> trackedItemsList, List<RestoreLogData> restoreLogList) {
      this.targetName = targetName != null ? targetName : "";
      this.targetUUID = targetUUID != null ? targetUUID : "";
      this.ryoBalance = ryoBalance;
      this.tokenBalance = tokenBalance;
      this.loginStreak = loginStreak;
      this.overflowCount = overflowCount;
      this.lifetimeSpent = lifetimeSpent;
      this.lifetimeCratesOpened = lifetimeCratesOpened;
      this.historyList = (List<HistoryData>)(historyList != null ? historyList : new ArrayList());
      this.ownedItemsList = (List<String>)(ownedItemsList != null ? ownedItemsList : new ArrayList());
      this.pityMap = (Map<String, Integer>)(pityMap != null ? pityMap : new HashMap());
      this.trackedItemsList = (List<TrackedItemData>)(trackedItemsList != null ? trackedItemsList : new ArrayList());
      this.restoreLogList = (List<RestoreLogData>)(restoreLogList != null ? restoreLogList : new ArrayList());
   }

   public void fromBytes(ByteBuf buf) {
      this.targetName = ByteBufUtils.readUTF8String(buf);
      this.targetUUID = ByteBufUtils.readUTF8String(buf);
      this.ryoBalance = buf.readLong();
      this.tokenBalance = buf.readInt();
      this.loginStreak = buf.readInt();
      this.overflowCount = buf.readInt();
      this.lifetimeSpent = buf.readLong();
      this.lifetimeCratesOpened = buf.readInt();
      int histCount = buf.readInt();
      this.historyList = new ArrayList();

      for(int i = 0; i < histCount; ++i) {
         HistoryData hd = new HistoryData();
         hd.crateId = ByteBufUtils.readUTF8String(buf);
         hd.displayName = ByteBufUtils.readUTF8String(buf);
         hd.rarityOrdinal = buf.readByte();
         hd.timestamp = buf.readLong();
         hd.tokenAmount = buf.readInt();
         this.historyList.add(hd);
      }

      int ownedCount = buf.readInt();
      this.ownedItemsList = new ArrayList();

      for(int i = 0; i < ownedCount; ++i) {
         this.ownedItemsList.add(ByteBufUtils.readUTF8String(buf));
      }

      int pityCount = buf.readInt();
      this.pityMap = new HashMap();

      for(int i = 0; i < pityCount; ++i) {
         String crateId = ByteBufUtils.readUTF8String(buf);
         int count = buf.readInt();
         this.pityMap.put(crateId, count);
      }

      this.trackedItemsList = new ArrayList();
      this.restoreLogList = new ArrayList();
      if (buf.readableBytes() > 0) {
         int trackedCount = buf.readInt();

         for(int i = 0; i < trackedCount; ++i) {
            TrackedItemData td = new TrackedItemData();
            td.regName = ByteBufUtils.readUTF8String(buf);
            td.meta = buf.readInt();
            td.displayName = ByteBufUtils.readUTF8String(buf);
            td.category = ByteBufUtils.readUTF8String(buf);
            td.owned = buf.readBoolean();
            td.firstSeen = buf.readLong();
            td.lastSeen = buf.readLong();
            int xpLen = buf.readInt();
            td.jutsuXp = new int[xpLen];

            for(int j = 0; j < xpLen; ++j) {
               td.jutsuXp[j] = buf.readInt();
            }

            td.hasOwner = buf.readBoolean();
            td.isAffinity = buf.readBoolean();
            this.trackedItemsList.add(td);
         }

         int logCount = buf.readInt();

         for(int i = 0; i < logCount; ++i) {
            RestoreLogData rl = new RestoreLogData();
            rl.timestamp = buf.readLong();
            rl.adminName = ByteBufUtils.readUTF8String(buf);
            rl.itemName = ByteBufUtils.readUTF8String(buf);
            rl.targetPlayerName = ByteBufUtils.readUTF8String(buf);
            this.restoreLogList.add(rl);
         }
      }

   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.targetName);
      ByteBufUtils.writeUTF8String(buf, this.targetUUID);
      buf.writeLong(this.ryoBalance);
      buf.writeInt(this.tokenBalance);
      buf.writeInt(this.loginStreak);
      buf.writeInt(this.overflowCount);
      buf.writeLong(this.lifetimeSpent);
      buf.writeInt(this.lifetimeCratesOpened);
      buf.writeInt(this.historyList.size());

      for(HistoryData hd : this.historyList) {
         ByteBufUtils.writeUTF8String(buf, hd.crateId != null ? hd.crateId : "");
         ByteBufUtils.writeUTF8String(buf, hd.displayName != null ? hd.displayName : "");
         buf.writeByte(hd.rarityOrdinal);
         buf.writeLong(hd.timestamp);
         buf.writeInt(hd.tokenAmount);
      }

      buf.writeInt(this.ownedItemsList.size());

      for(String item : this.ownedItemsList) {
         ByteBufUtils.writeUTF8String(buf, item != null ? item : "");
      }

      buf.writeInt(this.pityMap.size());

      for(Map.Entry<String, Integer> entry : this.pityMap.entrySet()) {
         ByteBufUtils.writeUTF8String(buf, (String)entry.getKey());
         buf.writeInt((Integer)entry.getValue());
      }

      buf.writeInt(this.trackedItemsList.size());

      for(TrackedItemData td : this.trackedItemsList) {
         ByteBufUtils.writeUTF8String(buf, td.regName != null ? td.regName : "");
         buf.writeInt(td.meta);
         ByteBufUtils.writeUTF8String(buf, td.displayName != null ? td.displayName : "");
         ByteBufUtils.writeUTF8String(buf, td.category != null ? td.category : "");
         buf.writeBoolean(td.owned);
         buf.writeLong(td.firstSeen);
         buf.writeLong(td.lastSeen);
         int[] xp = td.jutsuXp != null ? td.jutsuXp : new int[0];
         buf.writeInt(xp.length);

         for(int val : xp) {
            buf.writeInt(val);
         }

         buf.writeBoolean(td.hasOwner);
         buf.writeBoolean(td.isAffinity);
      }

      buf.writeInt(this.restoreLogList.size());

      for(RestoreLogData rl : this.restoreLogList) {
         buf.writeLong(rl.timestamp);
         ByteBufUtils.writeUTF8String(buf, rl.adminName != null ? rl.adminName : "");
         ByteBufUtils.writeUTF8String(buf, rl.itemName != null ? rl.itemName : "");
         ByteBufUtils.writeUTF8String(buf, rl.targetPlayerName != null ? rl.targetPlayerName : "");
      }

   }

   public static class HistoryData {
      public String crateId;
      public String displayName;
      public int rarityOrdinal;
      public long timestamp;
      public int tokenAmount;
   }

   public static class TrackedItemData {
      public String regName;
      public int meta;
      public String displayName;
      public String category;
      public boolean owned;
      public long firstSeen;
      public long lastSeen;
      public int[] jutsuXp;
      public boolean hasOwner;
      public boolean isAffinity;
   }

   public static class RestoreLogData {
      public long timestamp;
      public String adminName;
      public String itemName;
      public String targetPlayerName;
   }

   public static class Handler implements IMessageHandler<ShopAdminSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(ShopAdminSyncMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            ShopAdminClientData.targetName = message.targetName;
            ShopAdminClientData.targetUUID = message.targetUUID;
            ShopAdminClientData.ryoBalance = message.ryoBalance;
            ShopAdminClientData.tokenBalance = message.tokenBalance;
            ShopAdminClientData.loginStreak = message.loginStreak;
            ShopAdminClientData.overflowCount = message.overflowCount;
            ShopAdminClientData.lifetimeSpent = message.lifetimeSpent;
            ShopAdminClientData.lifetimeCratesOpened = message.lifetimeCratesOpened;
            ShopAdminClientData.history = new ArrayList();

            for(HistoryData hd : message.historyList) {
               ShopAdminClientData.history.add(new ShopAdminClientData.AdminHistoryEntry(hd.crateId, hd.displayName, hd.rarityOrdinal, hd.timestamp, hd.tokenAmount));
            }

            ShopAdminClientData.ownedItems = new ArrayList(message.ownedItemsList);
            ShopAdminClientData.pityCounters = new HashMap(message.pityMap);
            ShopAdminClientData.trackedItems = new ArrayList();

            for(TrackedItemData td : message.trackedItemsList) {
               ShopAdminClientData.trackedItems.add(new ShopAdminClientData.AdminTrackedItem(td.regName, td.meta, td.displayName, td.category, td.owned, td.firstSeen, td.lastSeen, td.jutsuXp != null ? td.jutsuXp : new int[0], td.hasOwner, td.isAffinity));
            }

            ShopAdminClientData.restoreLog = new ArrayList();

            for(RestoreLogData rl : message.restoreLogList) {
               ShopAdminClientData.restoreLog.add(new ShopAdminClientData.AdminRestoreLog(rl.timestamp, rl.adminName, rl.itemName, rl.targetPlayerName));
            }

            if (Minecraft.getMinecraft().currentScreen instanceof ShopAdminGui) {
               ((ShopAdminGui)Minecraft.getMinecraft().currentScreen).initGui();
            } else {
               Minecraft.getMinecraft().displayGuiScreen(new ShopAdminGui());
            }

         });
         return null;
      }
   }
}
