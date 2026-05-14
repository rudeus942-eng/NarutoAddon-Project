
package net.luck.narutoaddon.OtherCode.quest.pvp.network;

import io.netty.buffer.ByteBuf;
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

public class PvpSyncMessage implements IMessage {
   private List<ActivePvpMissionInfo> activeMissions;
   private List<PvpOfferInfo> offers;
   private List<BingoInfo> bingoEntries;
   private long playerPvpXp;
   private byte playerPvpRank;
   private boolean isKage;
   private String kageVillage;

   public PvpSyncMessage() {
      this.activeMissions = new ArrayList();
      this.offers = new ArrayList();
      this.bingoEntries = new ArrayList();
      this.kageVillage = "";
   }

   public PvpSyncMessage(List<ActivePvpMissionInfo> activeMissions, List<PvpOfferInfo> offers, List<BingoInfo> bingoEntries, long playerPvpXp, byte playerPvpRank, boolean isKage, String kageVillage) {
      this.activeMissions = (List<ActivePvpMissionInfo>)(activeMissions != null ? activeMissions : new ArrayList());
      this.offers = (List<PvpOfferInfo>)(offers != null ? offers : new ArrayList());
      this.bingoEntries = (List<BingoInfo>)(bingoEntries != null ? bingoEntries : new ArrayList());
      this.playerPvpXp = playerPvpXp;
      this.playerPvpRank = playerPvpRank;
      this.isKage = isKage;
      this.kageVillage = kageVillage != null ? kageVillage : "";
   }

   public static byte pvpSubSlotToByte(String subSlot) {
      switch (subSlot) {
         case "pvp_daily_0":
            return 30;
         case "pvp_daily_1":
            return 31;
         case "pvp_daily_2":
            return 32;
         case "pvp_weekly_0":
            return 40;
         case "pvp_weekly_1":
            return 41;
         case "pvp_weekly_2":
            return 42;
         case "pvp_random":
            return 50;
         case "pvp_assigned":
            return 60;
         default:
            return -1;
      }
   }

   public static String byteToPvpSubSlot(byte b) {
      switch (b) {
         case 30:
            return "pvp_daily_0";
         case 31:
            return "pvp_daily_1";
         case 32:
            return "pvp_daily_2";
         case 40:
            return "pvp_weekly_0";
         case 41:
            return "pvp_weekly_1";
         case 42:
            return "pvp_weekly_2";
         case 50:
            return "pvp_random";
         case 60:
            return "pvp_assigned";
         default:
            return null;
      }
   }

   public void fromBytes(ByteBuf buf) {
      int activeCount = buf.readInt();
      this.activeMissions = new ArrayList();

      for(int i = 0; i < activeCount; ++i) {
         ActivePvpMissionInfo info = new ActivePvpMissionInfo();
         info.subSlotByte = buf.readByte();
         info.name = ByteBufUtils.readUTF8String(buf);
         info.description = ByteBufUtils.readUTF8String(buf);
         info.progress = buf.readInt();
         info.killsRequired = buf.readInt();
         info.rank = buf.readByte();
         info.startTimeMs = buf.readLong();
         info.timeLimitMs = buf.readLong();
         this.activeMissions.add(info);
      }

      int offerCount = buf.readInt();
      this.offers = new ArrayList();

      for(int i = 0; i < offerCount; ++i) {
         PvpOfferInfo oi = new PvpOfferInfo();
         oi.subSlotByte = buf.readByte();
         oi.name = ByteBufUtils.readUTF8String(buf);
         oi.description = ByteBufUtils.readUTF8String(buf);
         oi.rank = buf.readByte();
         oi.killCount = buf.readInt();
         oi.ninjaXpReward = buf.readInt();
         oi.pvpXpReward = buf.readInt();
         oi.ryoReward = buf.readInt();
         oi.rerollsRemaining = buf.readInt();
         oi.cooldownRemaining = buf.readLong();
         this.offers.add(oi);
      }

      int bingoCount = buf.readInt();
      this.bingoEntries = new ArrayList();

      for(int i = 0; i < bingoCount; ++i) {
         BingoInfo bi = new BingoInfo();
         bi.targetName = ByteBufUtils.readUTF8String(buf);
         bi.targetVillage = ByteBufUtils.readUTF8String(buf);
         bi.reward = buf.readInt();
         bi.pvpXpReward = buf.readInt();
         bi.ryoReward = buf.readInt();
         bi.claimed = buf.readBoolean();
         this.bingoEntries.add(bi);
      }

      this.playerPvpXp = buf.readLong();
      this.playerPvpRank = buf.readByte();
      this.isKage = buf.readBoolean();
      this.kageVillage = ByteBufUtils.readUTF8String(buf);
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.activeMissions.size());

      for(ActivePvpMissionInfo info : this.activeMissions) {
         buf.writeByte(info.subSlotByte);
         ByteBufUtils.writeUTF8String(buf, info.name != null ? info.name : "");
         ByteBufUtils.writeUTF8String(buf, info.description != null ? info.description : "");
         buf.writeInt(info.progress);
         buf.writeInt(info.killsRequired);
         buf.writeByte(info.rank);
         buf.writeLong(info.startTimeMs);
         buf.writeLong(info.timeLimitMs);
      }

      buf.writeInt(this.offers.size());

      for(PvpOfferInfo oi : this.offers) {
         buf.writeByte(oi.subSlotByte);
         ByteBufUtils.writeUTF8String(buf, oi.name != null ? oi.name : "");
         ByteBufUtils.writeUTF8String(buf, oi.description != null ? oi.description : "");
         buf.writeByte(oi.rank);
         buf.writeInt(oi.killCount);
         buf.writeInt(oi.ninjaXpReward);
         buf.writeInt(oi.pvpXpReward);
         buf.writeInt(oi.ryoReward);
         buf.writeInt(oi.rerollsRemaining);
         buf.writeLong(oi.cooldownRemaining);
      }

      buf.writeInt(this.bingoEntries.size());

      for(BingoInfo bi : this.bingoEntries) {
         ByteBufUtils.writeUTF8String(buf, bi.targetName != null ? bi.targetName : "");
         ByteBufUtils.writeUTF8String(buf, bi.targetVillage != null ? bi.targetVillage : "");
         buf.writeInt(bi.reward);
         buf.writeInt(bi.pvpXpReward);
         buf.writeInt(bi.ryoReward);
         buf.writeBoolean(bi.claimed);
      }

      buf.writeLong(this.playerPvpXp);
      buf.writeByte(this.playerPvpRank);
      buf.writeBoolean(this.isKage);
      ByteBufUtils.writeUTF8String(buf, this.kageVillage != null ? this.kageVillage : "");
   }

   public static class ActivePvpMissionInfo {
      public byte subSlotByte;
      public String name;
      public String description;
      public int progress;
      public int killsRequired;
      public byte rank;
      public long startTimeMs;
      public long timeLimitMs;
   }

   public static class PvpOfferInfo {
      public byte subSlotByte;
      public String name;
      public String description;
      public byte rank;
      public int killCount;
      public int ninjaXpReward;
      public int pvpXpReward;
      public int ryoReward;
      public int rerollsRemaining;
      public long cooldownRemaining;
   }

   public static class BingoInfo {
      public String targetName;
      public String targetVillage;
      public int reward;
      public int pvpXpReward;
      public int ryoReward;
      public boolean claimed;
   }

   public static class Handler implements IMessageHandler<PvpSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(PvpSyncMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            PvpClientData.playerPvpXp = message.playerPvpXp;
            PvpClientData.playerPvpRankOrdinal = message.playerPvpRank;
            PvpClientData.isKage = message.isKage;
            PvpClientData.kageVillage = message.kageVillage;

            for(String slot : PvpClientData.PVP_DAILY_SLOTS) {
               PvpClientData.clearActiveMission(slot);
            }

            for(String slot : PvpClientData.PVP_WEEKLY_SLOTS) {
               PvpClientData.clearActiveMission(slot);
            }

            PvpClientData.clearActiveMission("pvp_random");
            PvpClientData.clearActiveMission("pvp_assigned");

            for(ActivePvpMissionInfo info : message.activeMissions) {
               String subSlot = PvpSyncMessage.byteToPvpSubSlot(info.subSlotByte);
               if (subSlot != null) {
                  PvpClientData.setActiveMission(subSlot, new PvpClientData.ActivePvpMissionInfo(info.name, info.description, info.progress, info.killsRequired, info.rank, info.startTimeMs, info.timeLimitMs));
               }
            }

            for(String slot : PvpClientData.PVP_DAILY_SLOTS) {
               PvpClientData.clearOffer(slot);
            }

            for(String slot : PvpClientData.PVP_WEEKLY_SLOTS) {
               PvpClientData.clearOffer(slot);
            }

            PvpClientData.clearOffer("pvp_random");
            PvpClientData.clearOffer("pvp_assigned");

            for(PvpOfferInfo oi : message.offers) {
               String subSlot = PvpSyncMessage.byteToPvpSubSlot(oi.subSlotByte);
               if (subSlot != null) {
                  PvpClientData.setOffer(subSlot, new PvpClientData.PvpOfferInfo(oi.name, oi.description, oi.rank, oi.killCount, oi.ninjaXpReward, oi.pvpXpReward, oi.ryoReward, oi.rerollsRemaining, oi.cooldownRemaining));
               }
            }

            List<PvpClientData.BingoInfo> clientBingoEntries = new ArrayList();

            for(BingoInfo bi : message.bingoEntries) {
               clientBingoEntries.add(new PvpClientData.BingoInfo(bi.targetName, bi.targetVillage, bi.reward, bi.claimed, bi.pvpXpReward, bi.ryoReward));
            }

            PvpClientData.setBingoEntries(clientBingoEntries);
         });
         return null;
      }
   }
}
