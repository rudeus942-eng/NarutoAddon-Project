package net.luck.narutoaddon.OtherCode.akatsuki.network;

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

public class AkatsukiSyncMessage implements IMessage {
   private boolean isAkatsuki;
   private boolean hasPendingInvite;
   private boolean akatsukiExists;
   private boolean criticalThreat;
   private int akatsukiZoneCount;
   private int totalZoneCount;
   private float akatsukiThreatPercent;
   private int rankOrdinal;
   private int ringOrdinal;
   private int reputation;
   private int bountyTokens;
   private int[] ringUpgradeLevels = new int[5];
   private String partnerName = "";
   private List<RosterData> roster = new ArrayList();
   private boolean hasLeaderMission;
   private String leaderMissionType = "";
   private String leaderMissionDesc = "";
   private String leaderMissionObjective = "";
   private int leaderMissionTokenReward;
   private int leaderMissionRepReward;
   private boolean leaderMissionCompleted;
   private int leaderMissionX;
   private int leaderMissionZ;
   private List<BountyData> bounties = new ArrayList();
   private List<String> activityLog = new ArrayList();
   private int allianceScore;

   public static AkatsukiSyncMessage forAkatsuki(int rankOrdinal, int ringOrdinal, int reputation, int bountyTokens, int[] ringUpgradeLevels, String partnerName, boolean criticalThreat, int akatsukiZoneCount, int totalZoneCount, boolean hasPendingInvite, List<RosterData> roster, List<BountyData> bounties, List<String> activityLog, float akatsukiThreatPercent) {
      AkatsukiSyncMessage msg = new AkatsukiSyncMessage();
      msg.isAkatsuki = true;
      msg.hasPendingInvite = hasPendingInvite;
      msg.akatsukiExists = true;
      msg.criticalThreat = criticalThreat;
      msg.akatsukiZoneCount = akatsukiZoneCount;
      msg.totalZoneCount = totalZoneCount;
      msg.akatsukiThreatPercent = akatsukiThreatPercent;
      msg.rankOrdinal = rankOrdinal;
      msg.ringOrdinal = ringOrdinal;
      msg.reputation = reputation;
      msg.bountyTokens = bountyTokens;
      if (ringUpgradeLevels != null && ringUpgradeLevels.length == 5) {
         System.arraycopy(ringUpgradeLevels, 0, msg.ringUpgradeLevels, 0, 5);
      }

      msg.partnerName = partnerName != null ? partnerName : "";
      msg.roster = (List<RosterData>)(roster != null ? roster : new ArrayList());
      msg.bounties = (List<BountyData>)(bounties != null ? bounties : new ArrayList());
      msg.activityLog = (List<String>)(activityLog != null ? activityLog : new ArrayList());
      return msg;
   }

   public void setLeaderMission(String type, String description, String objective, int tokenReward, int repReward, boolean completed, int targetX, int targetZ) {
      this.hasLeaderMission = true;
      this.leaderMissionType = type != null ? type : "";
      this.leaderMissionDesc = description != null ? description : "";
      this.leaderMissionObjective = objective != null ? objective : "";
      this.leaderMissionTokenReward = tokenReward;
      this.leaderMissionRepReward = repReward;
      this.leaderMissionCompleted = completed;
      this.leaderMissionX = targetX;
      this.leaderMissionZ = targetZ;
   }

   public static AkatsukiSyncMessage forVillage(boolean akatsukiExists, boolean hasPendingInvite, boolean criticalThreat, int akatsukiZoneCount, int totalZoneCount, List<BountyData> bounties, List<String> activityLog, int allianceScore, float akatsukiThreatPercent) {
      AkatsukiSyncMessage msg = new AkatsukiSyncMessage();
      msg.isAkatsuki = false;
      msg.hasPendingInvite = hasPendingInvite;
      msg.akatsukiExists = akatsukiExists;
      msg.criticalThreat = criticalThreat;
      msg.akatsukiZoneCount = akatsukiZoneCount;
      msg.totalZoneCount = totalZoneCount;
      msg.akatsukiThreatPercent = akatsukiThreatPercent;
      msg.bounties = (List<BountyData>)(bounties != null ? bounties : new ArrayList());
      msg.activityLog = (List<String>)(activityLog != null ? activityLog : new ArrayList());
      msg.allianceScore = allianceScore;
      return msg;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.isAkatsuki);
      buf.writeBoolean(this.hasPendingInvite);
      buf.writeBoolean(this.akatsukiExists);
      buf.writeBoolean(this.criticalThreat);
      buf.writeInt(this.akatsukiZoneCount);
      buf.writeInt(this.totalZoneCount);
      buf.writeFloat(this.akatsukiThreatPercent);
      if (this.isAkatsuki) {
         buf.writeInt(this.rankOrdinal);
         buf.writeInt(this.ringOrdinal);
         buf.writeInt(this.reputation);
         buf.writeInt(this.bountyTokens);

         for(int i = 0; i < 5; ++i) {
            buf.writeInt(this.ringUpgradeLevels[i]);
         }

         ByteBufUtils.writeUTF8String(buf, this.partnerName);
         buf.writeInt(this.roster.size());

         for(RosterData entry : this.roster) {
            ByteBufUtils.writeUTF8String(buf, entry.name);
            ByteBufUtils.writeUTF8String(buf, entry.ringKanji);
            ByteBufUtils.writeUTF8String(buf, entry.rankName);
            buf.writeBoolean(entry.online);
         }

         buf.writeInt(this.bounties.size());

         for(BountyData b : this.bounties) {
            ByteBufUtils.writeUTF8String(buf, b.targetName);
            buf.writeInt(b.amount);
            ByteBufUtils.writeUTF8String(buf, b.region);
         }

         buf.writeInt(this.activityLog.size());

         for(String entry : this.activityLog) {
            ByteBufUtils.writeUTF8String(buf, entry);
         }

         buf.writeBoolean(this.hasLeaderMission);
         if (this.hasLeaderMission) {
            ByteBufUtils.writeUTF8String(buf, this.leaderMissionType);
            ByteBufUtils.writeUTF8String(buf, this.leaderMissionDesc);
            ByteBufUtils.writeUTF8String(buf, this.leaderMissionObjective);
            buf.writeInt(this.leaderMissionTokenReward);
            buf.writeInt(this.leaderMissionRepReward);
            buf.writeBoolean(this.leaderMissionCompleted);
            buf.writeInt(this.leaderMissionX);
            buf.writeInt(this.leaderMissionZ);
         }
      } else {
         buf.writeInt(this.bounties.size());

         for(BountyData b : this.bounties) {
            ByteBufUtils.writeUTF8String(buf, b.targetName);
            buf.writeInt(b.amount);
            ByteBufUtils.writeUTF8String(buf, b.region);
         }

         buf.writeInt(this.activityLog.size());

         for(String entry : this.activityLog) {
            ByteBufUtils.writeUTF8String(buf, entry);
         }

         buf.writeInt(this.allianceScore);
      }

   }

   public void fromBytes(ByteBuf buf) {
      this.isAkatsuki = buf.readBoolean();
      this.hasPendingInvite = buf.readBoolean();
      this.akatsukiExists = buf.readBoolean();
      this.criticalThreat = buf.readBoolean();
      this.akatsukiZoneCount = buf.readInt();
      this.totalZoneCount = buf.readInt();
      this.akatsukiThreatPercent = buf.readFloat();
      if (this.isAkatsuki) {
         this.rankOrdinal = buf.readInt();
         this.ringOrdinal = buf.readInt();
         this.reputation = buf.readInt();
         this.bountyTokens = buf.readInt();
         this.ringUpgradeLevels = new int[5];

         for(int i = 0; i < 5; ++i) {
            this.ringUpgradeLevels[i] = buf.readInt();
         }

         this.partnerName = ByteBufUtils.readUTF8String(buf);
         int rosterCount = buf.readInt();
         this.roster = new ArrayList(rosterCount);

         for(int i = 0; i < rosterCount; ++i) {
            String name = ByteBufUtils.readUTF8String(buf);
            String ringKanji = ByteBufUtils.readUTF8String(buf);
            String rankName = ByteBufUtils.readUTF8String(buf);
            boolean online = buf.readBoolean();
            this.roster.add(new RosterData(name, ringKanji, rankName, online));
         }

         int bountyCount = buf.readInt();
         this.bounties = new ArrayList(bountyCount);

         for(int i = 0; i < bountyCount; ++i) {
            String targetName = ByteBufUtils.readUTF8String(buf);
            int amount = buf.readInt();
            String region = ByteBufUtils.readUTF8String(buf);
            this.bounties.add(new BountyData(targetName, amount, region));
         }

         int logCount = buf.readInt();
         this.activityLog = new ArrayList(logCount);

         for(int i = 0; i < logCount; ++i) {
            this.activityLog.add(ByteBufUtils.readUTF8String(buf));
         }

         this.hasLeaderMission = buf.readBoolean();
         if (this.hasLeaderMission) {
            this.leaderMissionType = ByteBufUtils.readUTF8String(buf);
            this.leaderMissionDesc = ByteBufUtils.readUTF8String(buf);
            this.leaderMissionObjective = ByteBufUtils.readUTF8String(buf);
            this.leaderMissionTokenReward = buf.readInt();
            this.leaderMissionRepReward = buf.readInt();
            this.leaderMissionCompleted = buf.readBoolean();
            this.leaderMissionX = buf.readInt();
            this.leaderMissionZ = buf.readInt();
         }
      } else {
         int bountyCount = buf.readInt();
         this.bounties = new ArrayList(bountyCount);

         for(int i = 0; i < bountyCount; ++i) {
            String targetName = ByteBufUtils.readUTF8String(buf);
            int amount = buf.readInt();
            String region = ByteBufUtils.readUTF8String(buf);
            this.bounties.add(new BountyData(targetName, amount, region));
         }

         int logCount = buf.readInt();
         this.activityLog = new ArrayList(logCount);

         for(int i = 0; i < logCount; ++i) {
            this.activityLog.add(ByteBufUtils.readUTF8String(buf));
         }

         this.allianceScore = buf.readInt();
      }

   }

   public static class BountyData {
      public final String targetName;
      public final int amount;
      public final String region;

      public BountyData(String targetName, int amount, String region) {
         this.targetName = targetName;
         this.amount = amount;
         this.region = region;
      }
   }

   public static class RosterData {
      public final String name;
      public final String ringKanji;
      public final String rankName;
      public final boolean online;

      public RosterData(String name, String ringKanji, String rankName, boolean online) {
         this.name = name;
         this.ringKanji = ringKanji;
         this.rankName = rankName;
         this.online = online;
      }
   }

   public static class Handler implements IMessageHandler<AkatsukiSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(AkatsukiSyncMessage msg, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            AkatsukiClientData.isAkatsuki = msg.isAkatsuki;
            AkatsukiClientData.hasPendingInvite = msg.hasPendingInvite;
            AkatsukiClientData.akatsukiExists = msg.akatsukiExists;
            AkatsukiClientData.criticalThreat = msg.criticalThreat;
            AkatsukiClientData.akatsukiZoneCount = msg.akatsukiZoneCount;
            AkatsukiClientData.totalZoneCount = msg.totalZoneCount;
            AkatsukiClientData.akatsukiThreatPercent = msg.akatsukiThreatPercent;
            AkatsukiClientData.bounties.clear();
            AkatsukiClientData.activityLog.clear();
            AkatsukiClientData.roster.clear();
            if (msg.isAkatsuki) {
               AkatsukiClientData.rankOrdinal = msg.rankOrdinal;
               AkatsukiClientData.ringOrdinal = msg.ringOrdinal;
               AkatsukiClientData.reputation = msg.reputation;
               AkatsukiClientData.bountyTokens = msg.bountyTokens;
               System.arraycopy(msg.ringUpgradeLevels, 0, AkatsukiClientData.ringUpgradeLevels, 0, 5);
               AkatsukiClientData.partnerName = msg.partnerName;

               for(RosterData r : msg.roster) {
                  AkatsukiClientData.roster.add(new AkatsukiClientData.RosterEntry(r.name, r.ringKanji, r.rankName, r.online));
               }

               AkatsukiClientData.hasLeaderMission = msg.hasLeaderMission;
               if (msg.hasLeaderMission) {
                  AkatsukiClientData.leaderMissionType = msg.leaderMissionType;
                  AkatsukiClientData.leaderMissionDesc = msg.leaderMissionDesc;
                  AkatsukiClientData.leaderMissionObjective = msg.leaderMissionObjective;
                  AkatsukiClientData.leaderMissionTokenReward = msg.leaderMissionTokenReward;
                  AkatsukiClientData.leaderMissionRepReward = msg.leaderMissionRepReward;
                  AkatsukiClientData.leaderMissionCompleted = msg.leaderMissionCompleted;
                  AkatsukiClientData.leaderMissionX = msg.leaderMissionX;
                  AkatsukiClientData.leaderMissionZ = msg.leaderMissionZ;
               } else {
                  AkatsukiClientData.leaderMissionType = "";
                  AkatsukiClientData.leaderMissionDesc = "";
                  AkatsukiClientData.leaderMissionObjective = "";
                  AkatsukiClientData.leaderMissionTokenReward = 0;
                  AkatsukiClientData.leaderMissionRepReward = 0;
                  AkatsukiClientData.leaderMissionCompleted = false;
                  AkatsukiClientData.leaderMissionX = 0;
                  AkatsukiClientData.leaderMissionZ = 0;
               }
            } else {
               AkatsukiClientData.allianceScore = msg.allianceScore;
            }

            for(BountyData b : msg.bounties) {
               AkatsukiClientData.bounties.add(new AkatsukiClientData.BountyClientEntry(b.targetName, b.amount, b.region));
            }

            for(String entry : msg.activityLog) {
               AkatsukiClientData.activityLog.add(entry);
            }

         });
         return null;
      }
   }
}
