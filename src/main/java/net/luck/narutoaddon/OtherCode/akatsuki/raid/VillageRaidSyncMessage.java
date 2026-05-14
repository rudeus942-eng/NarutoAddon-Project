package net.luck.narutoaddon.OtherCode.akatsuki.raid;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.akatsuki.network.AkatsukiClientData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class VillageRaidSyncMessage implements IMessage {
   private boolean raidActive;
   private String targetVillage = "";
   private int rallyX;
   private int rallyZ;
   private long timeRemainingMs;
   private int killsAchieved;
   private int killsRequired;
   private String raidStatus = "";
   private int participantCount;
   private boolean isMyVillage;

   public static VillageRaidSyncMessage active(String targetVillage, int rallyX, int rallyZ, long timeRemainingMs, int killsAchieved, int killsRequired, String status, int participantCount, boolean isMyVillage) {
      VillageRaidSyncMessage msg = new VillageRaidSyncMessage();
      msg.raidActive = true;
      msg.targetVillage = targetVillage != null ? targetVillage : "";
      msg.rallyX = rallyX;
      msg.rallyZ = rallyZ;
      msg.timeRemainingMs = timeRemainingMs;
      msg.killsAchieved = killsAchieved;
      msg.killsRequired = killsRequired;
      msg.raidStatus = status != null ? status : "";
      msg.participantCount = participantCount;
      msg.isMyVillage = isMyVillage;
      return msg;
   }

   public static VillageRaidSyncMessage inactive() {
      VillageRaidSyncMessage msg = new VillageRaidSyncMessage();
      msg.raidActive = false;
      return msg;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.raidActive);
      if (this.raidActive) {
         ByteBufUtils.writeUTF8String(buf, this.targetVillage);
         buf.writeInt(this.rallyX);
         buf.writeInt(this.rallyZ);
         buf.writeLong(this.timeRemainingMs);
         buf.writeInt(this.killsAchieved);
         buf.writeInt(this.killsRequired);
         ByteBufUtils.writeUTF8String(buf, this.raidStatus);
         buf.writeInt(this.participantCount);
         buf.writeBoolean(this.isMyVillage);
      }

   }

   public void fromBytes(ByteBuf buf) {
      this.raidActive = buf.readBoolean();
      if (this.raidActive) {
         this.targetVillage = ByteBufUtils.readUTF8String(buf);
         this.rallyX = buf.readInt();
         this.rallyZ = buf.readInt();
         this.timeRemainingMs = buf.readLong();
         this.killsAchieved = buf.readInt();
         this.killsRequired = buf.readInt();
         this.raidStatus = ByteBufUtils.readUTF8String(buf);
         this.participantCount = buf.readInt();
         this.isMyVillage = buf.readBoolean();
      }

   }

   public static class Handler implements IMessageHandler<VillageRaidSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(VillageRaidSyncMessage msg, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            AkatsukiClientData.raidActive = msg.raidActive;
            if (msg.raidActive) {
               AkatsukiClientData.raidTargetVillage = msg.targetVillage;
               AkatsukiClientData.raidRallyX = msg.rallyX;
               AkatsukiClientData.raidRallyZ = msg.rallyZ;
               AkatsukiClientData.raidTimeRemainingMs = msg.timeRemainingMs;
               AkatsukiClientData.raidKillsAchieved = msg.killsAchieved;
               AkatsukiClientData.raidKillsRequired = msg.killsRequired;
               AkatsukiClientData.raidStatus = msg.raidStatus;
               AkatsukiClientData.raidParticipantCount = msg.participantCount;
               AkatsukiClientData.raidIsMyVillage = msg.isMyVillage;
               AkatsukiClientData.raidSyncTime = System.currentTimeMillis();
            } else {
               AkatsukiClientData.raidTargetVillage = "";
               AkatsukiClientData.raidRallyX = 0;
               AkatsukiClientData.raidRallyZ = 0;
               AkatsukiClientData.raidTimeRemainingMs = 0L;
               AkatsukiClientData.raidKillsAchieved = 0;
               AkatsukiClientData.raidKillsRequired = 0;
               AkatsukiClientData.raidStatus = "";
               AkatsukiClientData.raidParticipantCount = 0;
               AkatsukiClientData.raidIsMyVillage = false;
               AkatsukiClientData.raidSyncTime = 0L;
            }

         });
         return null;
      }
   }
}
