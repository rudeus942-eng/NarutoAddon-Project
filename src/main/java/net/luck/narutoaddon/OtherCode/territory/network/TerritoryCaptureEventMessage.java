package net.luck.narutoaddon.OtherCode.territory.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TerritoryCaptureEventMessage implements IMessage {
   public static final byte ZONE_CAPTURED = 0;
   public static final byte KILL_POINTS = 1;
   public static final byte CAPTURE_PROGRESS = 2;
   public static final byte DEFENDER_WAVE_SPAWNED = 3;
   private byte eventType;
   private String zoneId;
   private String villageName;
   private int pointsGained;
   private float captureProgress;

   public TerritoryCaptureEventMessage() {
      this.zoneId = "";
      this.villageName = "";
   }

   public TerritoryCaptureEventMessage(byte eventType, String zoneId, String villageName, int pointsGained, float captureProgress) {
      this.eventType = eventType;
      this.zoneId = zoneId != null ? zoneId : "";
      this.villageName = villageName != null ? villageName : "";
      this.pointsGained = pointsGained;
      this.captureProgress = captureProgress;
   }

   public void toBytes(ByteBuf buf) {
   }

   public void fromBytes(ByteBuf buf) {
      this.zoneId = "";
      this.villageName = "";
   }

   public static class Handler implements IMessageHandler<TerritoryCaptureEventMessage, IMessage> {
      public IMessage onMessage(TerritoryCaptureEventMessage msg, MessageContext ctx) {
         return null;
      }
   }
}
