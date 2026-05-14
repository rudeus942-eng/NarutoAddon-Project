
package net.luck.narutoaddon.OtherCode.quest.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointData;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class QuestWaypointMessage implements IMessage {
   private boolean hasWaypoint;
   private String questId;
   private int x;
   private int y;
   private int z;
   private byte waypointType;
   private String label;

   public QuestWaypointMessage() {
   }

   public QuestWaypointMessage(boolean hasWaypoint, String questId, int x, int y, int z, byte waypointType, String label) {
      this.hasWaypoint = hasWaypoint;
      this.questId = questId;
      this.x = x;
      this.y = y;
      this.z = z;
      this.waypointType = waypointType;
      this.label = label;
   }

   public static QuestWaypointMessage clear(String questId) {
      return new QuestWaypointMessage(false, questId, 0, 0, 0, (byte)0, "");
   }

   public static QuestWaypointMessage from(String questId, WaypointData data) {
      return new QuestWaypointMessage(true, questId, data.getPosition().getX(), data.getPosition().getY(), data.getPosition().getZ(), (byte)data.getType().ordinal(), data.getLabel());
   }

   public static QuestWaypointMessage clear() {
      return clear("_legacy");
   }

   public static QuestWaypointMessage from(WaypointData data) {
      return from("_legacy", data);
   }

   public void fromBytes(ByteBuf buf) {
      this.hasWaypoint = buf.readBoolean();
      this.questId = ByteBufUtils.readUTF8String(buf);
      if (this.hasWaypoint) {
         this.x = buf.readInt();
         this.y = buf.readInt();
         this.z = buf.readInt();
         this.waypointType = buf.readByte();
         this.label = ByteBufUtils.readUTF8String(buf);
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.hasWaypoint);
      ByteBufUtils.writeUTF8String(buf, this.questId != null ? this.questId : "");
      if (this.hasWaypoint) {
         buf.writeInt(this.x);
         buf.writeInt(this.y);
         buf.writeInt(this.z);
         buf.writeByte(this.waypointType);
         ByteBufUtils.writeUTF8String(buf, this.label != null ? this.label : "");
      }

   }

   public static class Handler implements IMessageHandler<QuestWaypointMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(QuestWaypointMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            if (message.hasWaypoint) {
               WaypointData.WaypointType type = message.waypointType < WaypointData.WaypointType.values().length ? WaypointData.WaypointType.values()[message.waypointType] : WaypointData.WaypointType.TRAVEL;
               QuestClientData.setWaypointForQuest(message.questId, new BlockPos(message.x, message.y, message.z), type, message.label);
            } else {
               QuestClientData.clearWaypointForQuest(message.questId);
            }

         });
         return null;
      }
   }
}
