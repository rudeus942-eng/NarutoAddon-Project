
package net.luck.narutoaddon.OtherCode.quest.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.core.QuestInstance;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class QuestSyncMessage implements IMessage {
   private List<ActiveQuestInfo> activeQuestInfos;
   private int completedCount;
   private List<QuestEntry> questEntries;
   private List<String> completedIds;
   private List<OfferInfo> offerInfos;
   private boolean beginnerComplete;
   private boolean forceBeginner;

   public QuestSyncMessage() {
      this.activeQuestInfos = new ArrayList();
      this.questEntries = new ArrayList();
      this.completedIds = new ArrayList();
      this.offerInfos = new ArrayList();
   }

   public QuestSyncMessage(List<ActiveQuestInfo> activeQuestInfos, int completedCount, List<QuestEntry> questEntries, List<String> completedIds, List<OfferInfo> offerInfos, boolean beginnerComplete) {
      this(activeQuestInfos, completedCount, questEntries, completedIds, offerInfos, beginnerComplete, false);
   }

   public QuestSyncMessage(List<ActiveQuestInfo> activeQuestInfos, int completedCount, List<QuestEntry> questEntries, List<String> completedIds, List<OfferInfo> offerInfos, boolean beginnerComplete, boolean forceBeginner) {
      this.activeQuestInfos = (List<ActiveQuestInfo>)(activeQuestInfos != null ? activeQuestInfos : new ArrayList());
      this.completedCount = completedCount;
      this.questEntries = (List<QuestEntry>)(questEntries != null ? questEntries : new ArrayList());
      this.completedIds = (List<String>)(completedIds != null ? completedIds : new ArrayList());
      this.offerInfos = (List<OfferInfo>)(offerInfos != null ? offerInfos : new ArrayList());
      this.beginnerComplete = beginnerComplete;
      this.forceBeginner = forceBeginner;
   }

   public void fromBytes(ByteBuf buf) {
      int activeCount = buf.readInt();
      this.activeQuestInfos = new ArrayList();

      for(int i = 0; i < activeCount; ++i) {
         ActiveQuestInfo info = new ActiveQuestInfo();
         info.questId = ByteBufUtils.readUTF8String(buf);
         info.questName = ByteBufUtils.readUTF8String(buf);
         info.stepIndex = buf.readInt();
         info.stepDesc = ByteBufUtils.readUTF8String(buf);
         info.totalSteps = buf.readInt();
         info.state = buf.readByte();
         info.killProgress = buf.readInt();
         info.killsRequired = buf.readInt();
         info.category = buf.readByte();
         info.slotKey = ByteBufUtils.readUTF8String(buf);
         info.rewardSummary = ByteBufUtils.readUTF8String(buf);
         this.activeQuestInfos.add(info);
      }

      this.completedCount = buf.readInt();
      int entryCount = buf.readInt();
      this.questEntries = new ArrayList();

      for(int i = 0; i < entryCount; ++i) {
         String id = ByteBufUtils.readUTF8String(buf);
         String name = ByteBufUtils.readUTF8String(buf);
         String desc = ByteBufUtils.readUTF8String(buf);
         int arc = buf.readInt();
         boolean available = buf.readBoolean();
         byte category = buf.readByte();
         long cooldownRemaining = buf.readLong();
         byte rank = buf.readByte();
         String storyline = ByteBufUtils.readUTF8String(buf);
         String rewardSummary = ByteBufUtils.readUTF8String(buf);
         this.questEntries.add(new QuestEntry(id, name, desc, arc, available, category, cooldownRemaining, rank, storyline, rewardSummary));
      }

      int compCount = buf.readInt();
      this.completedIds = new ArrayList();

      for(int i = 0; i < compCount; ++i) {
         this.completedIds.add(ByteBufUtils.readUTF8String(buf));
      }

      int offerCount = buf.readInt();
      this.offerInfos = new ArrayList();

      for(int i = 0; i < offerCount; ++i) {
         OfferInfo oi = new OfferInfo();
         oi.slotType = buf.readByte();
         oi.name = ByteBufUtils.readUTF8String(buf);
         oi.description = ByteBufUtils.readUTF8String(buf);
         oi.rank = buf.readByte();
         oi.killCount = buf.readInt();
         oi.xpReward = buf.readInt();
         oi.ryoReward = buf.readInt();
         oi.rerollsRemaining = buf.readInt();
         oi.cooldownRemaining = buf.readLong();
         this.offerInfos.add(oi);
      }

      this.beginnerComplete = buf.isReadable() ? buf.readBoolean() : false;
      this.forceBeginner = buf.isReadable() ? buf.readBoolean() : false;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.activeQuestInfos.size());

      for(ActiveQuestInfo info : this.activeQuestInfos) {
         ByteBufUtils.writeUTF8String(buf, info.questId != null ? info.questId : "");
         ByteBufUtils.writeUTF8String(buf, info.questName != null ? info.questName : "");
         buf.writeInt(info.stepIndex);
         ByteBufUtils.writeUTF8String(buf, info.stepDesc != null ? info.stepDesc : "");
         buf.writeInt(info.totalSteps);
         buf.writeByte(info.state);
         buf.writeInt(info.killProgress);
         buf.writeInt(info.killsRequired);
         buf.writeByte(info.category);
         ByteBufUtils.writeUTF8String(buf, info.slotKey != null ? info.slotKey : "");
         ByteBufUtils.writeUTF8String(buf, info.rewardSummary != null ? info.rewardSummary : "");
      }

      buf.writeInt(this.completedCount);
      buf.writeInt(this.questEntries.size());

      for(QuestEntry entry : this.questEntries) {
         ByteBufUtils.writeUTF8String(buf, entry.id);
         ByteBufUtils.writeUTF8String(buf, entry.name);
         ByteBufUtils.writeUTF8String(buf, entry.description);
         buf.writeInt(entry.arc);
         buf.writeBoolean(entry.available);
         buf.writeByte(entry.category);
         buf.writeLong(entry.cooldownRemaining);
         buf.writeByte(entry.rank);
         ByteBufUtils.writeUTF8String(buf, entry.storyline != null ? entry.storyline : "leaf_story");
         ByteBufUtils.writeUTF8String(buf, entry.rewardSummary != null ? entry.rewardSummary : "");
      }

      buf.writeInt(this.completedIds.size());

      for(String id : this.completedIds) {
         ByteBufUtils.writeUTF8String(buf, id);
      }

      buf.writeInt(this.offerInfos.size());

      for(OfferInfo oi : this.offerInfos) {
         buf.writeByte(oi.slotType);
         ByteBufUtils.writeUTF8String(buf, oi.name != null ? oi.name : "");
         ByteBufUtils.writeUTF8String(buf, oi.description != null ? oi.description : "");
         buf.writeByte(oi.rank);
         buf.writeInt(oi.killCount);
         buf.writeInt(oi.xpReward);
         buf.writeInt(oi.ryoReward);
         buf.writeInt(oi.rerollsRemaining);
         buf.writeLong(oi.cooldownRemaining);
      }

      buf.writeBoolean(this.beginnerComplete);
      buf.writeBoolean(this.forceBeginner);
   }

   public static byte subSlotToByte(String subSlot) {
      switch (subSlot) {
         case "daily_0":
            return 10;
         case "daily_1":
            return 11;
         case "daily_2":
            return 12;
         case "weekly_0":
            return 20;
         case "weekly_1":
            return 21;
         case "weekly_2":
            return 22;
         case "random":
            return 3;
         default:
            return 0;
      }
   }

   public static String byteToSubSlot(byte b) {
      switch (b) {
         case 3:
            return "random";
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         default:
            return "story:leaf_story";
         case 10:
            return "daily_0";
         case 11:
            return "daily_1";
         case 12:
            return "daily_2";
         case 20:
            return "weekly_0";
         case 21:
            return "weekly_1";
         case 22:
            return "weekly_2";
      }
   }

   public static class ActiveQuestInfo {
      public String questId;
      public String questName;
      public int stepIndex;
      public String stepDesc;
      public int totalSteps;
      public byte state;
      public int killProgress;
      public int killsRequired;
      public byte category;
      public String slotKey = "";
      public String rewardSummary = "";
   }

   public static class OfferInfo {
      public byte slotType;
      public String name;
      public String description;
      public byte rank;
      public int killCount;
      public int xpReward;
      public int ryoReward;
      public int rerollsRemaining;
      public long cooldownRemaining;
   }

   public static class QuestEntry {
      public final String id;
      public final String name;
      public final String description;
      public final int arc;
      public final boolean available;
      public final byte category;
      public final long cooldownRemaining;
      public final byte rank;
      public final String storyline;
      public final String rewardSummary;

      public QuestEntry(String id, String name, String description, int arc, boolean available) {
         this(id, name, description, arc, available, (byte)0, 0L, (byte)0, "leaf_story", "");
      }

      public QuestEntry(String id, String name, String description, int arc, boolean available, byte category, long cooldownRemaining) {
         this(id, name, description, arc, available, category, cooldownRemaining, (byte)0, "leaf_story", "");
      }

      public QuestEntry(String id, String name, String description, int arc, boolean available, byte category, long cooldownRemaining, byte rank) {
         this(id, name, description, arc, available, category, cooldownRemaining, rank, "leaf_story", "");
      }

      public QuestEntry(String id, String name, String description, int arc, boolean available, byte category, long cooldownRemaining, byte rank, String storyline) {
         this(id, name, description, arc, available, category, cooldownRemaining, rank, storyline, "");
      }

      public QuestEntry(String id, String name, String description, int arc, boolean available, byte category, long cooldownRemaining, byte rank, String storyline, String rewardSummary) {
         this.id = id;
         this.name = name;
         this.description = description;
         this.arc = arc;
         this.available = available;
         this.category = category;
         this.cooldownRemaining = cooldownRemaining;
         this.rank = rank;
         this.storyline = storyline != null ? storyline : "leaf_story";
         this.rewardSummary = rewardSummary != null ? rewardSummary : "";
      }
   }

   public static class Handler implements IMessageHandler<QuestSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(QuestSyncMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            QuestClientData.clearAllActiveQuests();

            for(ActiveQuestInfo info : message.activeQuestInfos) {
               QuestInstance.QuestState state = info.state >= 0 && info.state < QuestInstance.QuestState.values().length ? QuestInstance.QuestState.values()[info.state] : QuestInstance.QuestState.ACTIVE;
               String slot;
               if (info.category == 0 && info.slotKey != null && !info.slotKey.isEmpty()) {
                  slot = info.slotKey;
               } else {
                  slot = QuestSyncMessage.byteToSubSlot(info.category);
               }

               QuestClientData.setActiveQuestInSlot(slot, info.questId, info.questName, info.stepIndex, info.stepDesc, info.totalSteps, state, info.killProgress, info.killsRequired, info.rewardSummary);
            }

            List<QuestClientData.QuestListEntry> entries = new ArrayList();

            for(QuestEntry entry : message.questEntries) {
               entries.add(new QuestClientData.QuestListEntry(entry.id, entry.name, entry.description, entry.arc, entry.available, entry.category, entry.cooldownRemaining, entry.rank, entry.storyline, entry.rewardSummary));
            }

            QuestClientData.setAvailableQuests(entries);
            QuestClientData.setCompletedQuestIds(message.completedIds);
            QuestClientData.clearAllOffers();

            for(OfferInfo oi : message.offerInfos) {
               String slot = QuestSyncMessage.byteToSubSlot(oi.slotType);
               if (!QuestClientData.isStorySlot(slot)) {
                  QuestClientData.setOffer(slot, new QuestClientData.OfferState(oi.name, oi.description, oi.rank, oi.killCount, oi.xpReward, oi.ryoReward, oi.rerollsRemaining, oi.cooldownRemaining));
               }
            }

            QuestClientData.setBeginnerComplete(message.beginnerComplete);
            QuestClientData.setForceBeginner(message.forceBeginner);
         });
         return null;
      }
   }
}
