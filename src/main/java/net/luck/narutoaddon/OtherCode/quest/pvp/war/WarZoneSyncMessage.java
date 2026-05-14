
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

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

public class WarZoneSyncMessage implements IMessage {
   private String warId;
   private List<ZoneData> zones;

   public WarZoneSyncMessage() {
      this.warId = "";
      this.zones = new ArrayList();
   }

   public WarZoneSyncMessage(String warId, List<ZoneData> zones) {
      this.warId = warId != null ? warId : "";
      this.zones = (List<ZoneData>)(zones != null ? zones : new ArrayList());
   }

   public void fromBytes(ByteBuf buf) {
      this.warId = ByteBufUtils.readUTF8String(buf);
      int numZones = buf.readByte() & 255;
      this.zones = new ArrayList();

      for(int i = 0; i < numZones; ++i) {
         ZoneData data = new ZoneData();
         data.zoneIndex = buf.readByte() & 255;
         data.controllingVillage = buf.readByte();
         data.captureProgress = buf.readFloat();
         data.capturingVillage = buf.readByte();
         data.contested = buf.readBoolean();
         data.centerX = buf.readInt();
         data.centerZ = buf.readInt();
         data.radius = buf.readInt();
         this.zones.add(data);
      }

   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.warId);
      buf.writeByte(this.zones.size());

      for(ZoneData data : this.zones) {
         buf.writeByte(data.zoneIndex);
         buf.writeByte(data.controllingVillage);
         buf.writeFloat(data.captureProgress);
         buf.writeByte(data.capturingVillage);
         buf.writeBoolean(data.contested);
         buf.writeInt(data.centerX);
         buf.writeInt(data.centerZ);
         buf.writeInt(data.radius);
      }

   }

   public String getWarId() {
      return this.warId;
   }

   public List<ZoneData> getZones() {
      return this.zones;
   }

   public static class ZoneData {
      public int zoneIndex;
      public byte controllingVillage;
      public float captureProgress;
      public byte capturingVillage;
      public boolean contested;
      public int centerX;
      public int centerZ;
      public int radius;
   }

   public static class Handler implements IMessageHandler<WarZoneSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(WarZoneSyncMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> PvpClientData.setWarZoneData(message.warId, message.zones));
         return null;
      }
   }
}
