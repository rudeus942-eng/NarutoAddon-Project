
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

public class PvpTargetLocationMessage implements IMessage {
   public static final byte BEACON_HUNT = 0;
   public static final byte BEACON_BINGO = 1;
   public static final byte BEACON_MUTUAL = 2;
   private List<TargetEntry> targets;

   public PvpTargetLocationMessage() {
      this.targets = new ArrayList();
   }

   public PvpTargetLocationMessage(List<TargetEntry> targets) {
      this.targets = (List<TargetEntry>)(targets != null ? targets : new ArrayList());
   }

   public void fromBytes(ByteBuf buf) {
      int count = buf.readInt();
      this.targets = new ArrayList();

      for(int i = 0; i < count; ++i) {
         TargetEntry entry = new TargetEntry();
         entry.missionId = ByteBufUtils.readUTF8String(buf);
         entry.targetX = buf.readInt();
         entry.targetZ = buf.readInt();
         entry.beaconType = buf.readByte();
         this.targets.add(entry);
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.targets.size());

      for(TargetEntry entry : this.targets) {
         ByteBufUtils.writeUTF8String(buf, entry.missionId != null ? entry.missionId : "");
         buf.writeInt(entry.targetX);
         buf.writeInt(entry.targetZ);
         buf.writeByte(entry.beaconType);
      }

   }

   public static class TargetEntry {
      public String missionId;
      public int targetX;
      public int targetZ;
      public byte beaconType;

      public TargetEntry() {
      }

      public TargetEntry(String missionId, int targetX, int targetZ, byte beaconType) {
         this.missionId = missionId;
         this.targetX = targetX;
         this.targetZ = targetZ;
         this.beaconType = beaconType;
      }
   }

   public static class Handler implements IMessageHandler<PvpTargetLocationMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(PvpTargetLocationMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            List<PvpClientData.TargetPosInfo> positions = new ArrayList();

            for(TargetEntry entry : message.targets) {
               positions.add(new PvpClientData.TargetPosInfo(entry.missionId, entry.targetX, entry.targetZ, entry.beaconType));
            }

            PvpClientData.setTargetPositions(positions);
         });
         return null;
      }
   }
}
