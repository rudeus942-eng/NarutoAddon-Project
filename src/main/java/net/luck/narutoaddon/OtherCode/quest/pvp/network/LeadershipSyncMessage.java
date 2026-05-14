
package net.luck.narutoaddon.OtherCode.quest.pvp.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpClientData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class LeadershipSyncMessage implements IMessage {
   private boolean isAdvisor;
   private byte advisorVillageOrdinal;
   private boolean hasWarAuthority;
   private List<OrderInfo> activeOrders = new ArrayList();
   private boolean hasLeadershipOffer;
   private String offerName = "";
   private String offerDesc = "";
   private byte offerRank;
   private int offerKillCount;
   private int offerNinjaXpReward;
   private int offerPvpXpReward;
   private int offerRyoReward;
   private boolean hasLeadershipMission;
   private String missionName = "";
   private String missionDesc = "";
   private int missionProgress;
   private int missionKillsRequired;
   private byte missionRank;
   private String kageName = "";
   private List<String> advisorNames = new ArrayList();
   private List<OperationProgressInfo> operationProgressList = new ArrayList();

   public void setAdvisor(boolean isAdvisor, byte villageOrdinal) {
      this.isAdvisor = isAdvisor;
      this.advisorVillageOrdinal = villageOrdinal;
   }

   public void setWarAuthority(boolean hasWarAuthority) {
      this.hasWarAuthority = hasWarAuthority;
   }

   public void setActiveOrders(List<OrderInfo> orders) {
      this.activeOrders = (List<OrderInfo>)(orders != null ? orders : new ArrayList());
   }

   public void setLeadershipOffer(String name, String desc, byte rank, int kills, int ninjaXp, int pvpXp, int ryoReward) {
      this.hasLeadershipOffer = true;
      this.offerName = name != null ? name : "";
      this.offerDesc = desc != null ? desc : "";
      this.offerRank = rank;
      this.offerKillCount = kills;
      this.offerNinjaXpReward = ninjaXp;
      this.offerPvpXpReward = pvpXp;
      this.offerRyoReward = ryoReward;
   }

   public void setLeadershipMission(String name, String desc, int progress, int killsReq, byte rank) {
      this.hasLeadershipMission = true;
      this.missionName = name != null ? name : "";
      this.missionDesc = desc != null ? desc : "";
      this.missionProgress = progress;
      this.missionKillsRequired = killsReq;
      this.missionRank = rank;
   }

   public void setKageName(String name) {
      this.kageName = name != null ? name : "";
   }

   public void setAdvisorNames(List<String> names) {
      this.advisorNames = (List<String>)(names != null ? names : new ArrayList());
   }

   public void clearLeadershipOffer() {
      this.hasLeadershipOffer = false;
   }

   public void clearLeadershipMission() {
      this.hasLeadershipMission = false;
   }

   public void setOperationProgress(List<OperationProgressInfo> list) {
      this.operationProgressList = (List<OperationProgressInfo>)(list != null ? list : new ArrayList());
   }

   public void fromBytes(ByteBuf buf) {
      this.isAdvisor = buf.readBoolean();
      this.advisorVillageOrdinal = buf.readByte();
      this.hasWarAuthority = buf.readBoolean();
      int orderCount = buf.readByte() & 255;
      this.activeOrders = new ArrayList();

      for(int i = 0; i < orderCount; ++i) {
         OrderInfo info = new OrderInfo();
         info.orderId = ByteBufUtils.readUTF8String(buf);
         info.orderType = buf.readByte();
         info.targetName = ByteBufUtils.readUTF8String(buf);
         info.timeRemainingMs = buf.readLong();
         info.bonusPvpXp = buf.readInt();
         info.issuerName = ByteBufUtils.readUTF8String(buf);
         info.assignedTemplateId = ByteBufUtils.readUTF8String(buf);
         this.activeOrders.add(info);
      }

      this.hasLeadershipOffer = buf.readBoolean();
      if (this.hasLeadershipOffer) {
         this.offerName = ByteBufUtils.readUTF8String(buf);
         this.offerDesc = ByteBufUtils.readUTF8String(buf);
         this.offerRank = buf.readByte();
         this.offerKillCount = buf.readInt();
         this.offerNinjaXpReward = buf.readInt();
         this.offerPvpXpReward = buf.readInt();
         this.offerRyoReward = buf.readInt();
      }

      this.hasLeadershipMission = buf.readBoolean();
      if (this.hasLeadershipMission) {
         this.missionName = ByteBufUtils.readUTF8String(buf);
         this.missionDesc = ByteBufUtils.readUTF8String(buf);
         this.missionProgress = buf.readInt();
         this.missionKillsRequired = buf.readInt();
         this.missionRank = buf.readByte();
      }

      this.kageName = ByteBufUtils.readUTF8String(buf);
      int advisorCount = buf.readByte() & 255;
      this.advisorNames = new ArrayList();

      for(int i = 0; i < advisorCount; ++i) {
         this.advisorNames.add(ByteBufUtils.readUTF8String(buf));
      }

      int opCount = buf.readByte() & 255;
      this.operationProgressList = new ArrayList();

      for(int i = 0; i < opCount; ++i) {
         OperationProgressInfo op = new OperationProgressInfo();
         op.orderId = ByteBufUtils.readUTF8String(buf);
         op.templateId = ByteBufUtils.readUTF8String(buf);
         op.completedTiersBitmask = buf.readByte();
         op.participantCount = buf.readInt();
         op.totalCompletions = buf.readInt();
         this.operationProgressList.add(op);
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.isAdvisor);
      buf.writeByte(this.advisorVillageOrdinal);
      buf.writeBoolean(this.hasWarAuthority);
      buf.writeByte(this.activeOrders.size());

      for(OrderInfo info : this.activeOrders) {
         ByteBufUtils.writeUTF8String(buf, info.orderId != null ? info.orderId : "");
         buf.writeByte(info.orderType);
         ByteBufUtils.writeUTF8String(buf, info.targetName != null ? info.targetName : "");
         buf.writeLong(info.timeRemainingMs);
         buf.writeInt(info.bonusPvpXp);
         ByteBufUtils.writeUTF8String(buf, info.issuerName != null ? info.issuerName : "");
         ByteBufUtils.writeUTF8String(buf, info.assignedTemplateId != null ? info.assignedTemplateId : "");
      }

      buf.writeBoolean(this.hasLeadershipOffer);
      if (this.hasLeadershipOffer) {
         ByteBufUtils.writeUTF8String(buf, this.offerName);
         ByteBufUtils.writeUTF8String(buf, this.offerDesc);
         buf.writeByte(this.offerRank);
         buf.writeInt(this.offerKillCount);
         buf.writeInt(this.offerNinjaXpReward);
         buf.writeInt(this.offerPvpXpReward);
         buf.writeInt(this.offerRyoReward);
      }

      buf.writeBoolean(this.hasLeadershipMission);
      if (this.hasLeadershipMission) {
         ByteBufUtils.writeUTF8String(buf, this.missionName);
         ByteBufUtils.writeUTF8String(buf, this.missionDesc);
         buf.writeInt(this.missionProgress);
         buf.writeInt(this.missionKillsRequired);
         buf.writeByte(this.missionRank);
      }

      ByteBufUtils.writeUTF8String(buf, this.kageName != null ? this.kageName : "");
      buf.writeByte(this.advisorNames.size());

      for(String name : this.advisorNames) {
         ByteBufUtils.writeUTF8String(buf, name != null ? name : "");
      }

      buf.writeByte(this.operationProgressList.size());

      for(OperationProgressInfo op : this.operationProgressList) {
         ByteBufUtils.writeUTF8String(buf, op.orderId);
         ByteBufUtils.writeUTF8String(buf, op.templateId);
         buf.writeByte(op.completedTiersBitmask);
         buf.writeInt(op.participantCount);
         buf.writeInt(op.totalCompletions);
      }

   }

   public static class OperationProgressInfo {
      public String orderId = "";
      public String templateId = "";
      public byte completedTiersBitmask;
      public int participantCount;
      public int totalCompletions;

      public OperationProgressInfo() {
      }

      public OperationProgressInfo(String orderId, String templateId, byte bitmask, int participants, int totalCompletions) {
         this.orderId = orderId != null ? orderId : "";
         this.templateId = templateId != null ? templateId : "";
         this.completedTiersBitmask = bitmask;
         this.participantCount = participants;
         this.totalCompletions = totalCompletions;
      }
   }

   public static class OrderInfo {
      public String orderId;
      public byte orderType;
      public String targetName;
      public long timeRemainingMs;
      public int bonusPvpXp;
      public String issuerName;
      public String assignedTemplateId;

      public OrderInfo() {
      }

      public OrderInfo(String orderId, byte orderType, String targetName, long timeRemainingMs, int bonusPvpXp, String issuerName) {
         this(orderId, orderType, targetName, timeRemainingMs, bonusPvpXp, issuerName, "");
      }

      public OrderInfo(String orderId, byte orderType, String targetName, long timeRemainingMs, int bonusPvpXp, String issuerName, String assignedTemplateId) {
         this.orderId = orderId;
         this.orderType = orderType;
         this.targetName = targetName;
         this.timeRemainingMs = timeRemainingMs;
         this.bonusPvpXp = bonusPvpXp;
         this.issuerName = issuerName;
         this.assignedTemplateId = assignedTemplateId != null ? assignedTemplateId : "";
      }
   }

   public static class Handler implements IMessageHandler<LeadershipSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(LeadershipSyncMessage msg, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            PvpClientData.isAdvisor = msg.isAdvisor;
            PvpClientData.hasWarAuthority = msg.hasWarAuthority;
            VillageHelper.Village[] villages = VillageHelper.Village.values();
            if (msg.advisorVillageOrdinal >= 0 && msg.advisorVillageOrdinal < villages.length) {
               PvpClientData.advisorVillage = villages[msg.advisorVillageOrdinal].villageName;
            } else {
               PvpClientData.advisorVillage = "";
            }

            List<PvpClientData.OrderClientInfo> clientOrders = new ArrayList();

            for(OrderInfo info : msg.activeOrders) {
               clientOrders.add(new PvpClientData.OrderClientInfo(info.orderId, info.orderType, info.targetName, info.timeRemainingMs, info.bonusPvpXp, info.issuerName, info.assignedTemplateId));
            }

            PvpClientData.setActiveOrders(clientOrders);
            if (msg.hasLeadershipOffer) {
               PvpClientData.leadershipOffer = new PvpClientData.LeadershipOfferClientInfo(msg.offerName, msg.offerDesc, msg.offerRank, msg.offerKillCount, msg.offerNinjaXpReward, msg.offerPvpXpReward, msg.offerRyoReward);
            } else {
               PvpClientData.leadershipOffer = null;
            }

            if (msg.hasLeadershipMission) {
               PvpClientData.leadershipMission = new PvpClientData.LeadershipMissionClientInfo(msg.missionName, msg.missionDesc, msg.missionProgress, msg.missionKillsRequired, msg.missionRank);
            } else {
               PvpClientData.leadershipMission = null;
            }

            PvpClientData.kageName = msg.kageName;
            PvpClientData.setAdvisorNames(msg.advisorNames);
            List<PvpClientData.OperationProgressInfo> clientOps = new ArrayList();

            for(OperationProgressInfo op : msg.operationProgressList) {
               clientOps.add(new PvpClientData.OperationProgressInfo(op.orderId, op.templateId, op.completedTiersBitmask, op.participantCount, op.totalCompletions));
            }

            PvpClientData.setOperationProgress(clientOps);
         });
         return null;
      }
   }
}
