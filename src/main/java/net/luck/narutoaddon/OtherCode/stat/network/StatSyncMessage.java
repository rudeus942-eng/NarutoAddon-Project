
package net.luck.narutoaddon.OtherCode.stat.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.stat.core.PlayerStatData;
import net.luck.narutoaddon.OtherCode.stat.core.StatCategory;
import net.luck.narutoaddon.OtherCode.stat.core.StatElement;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumMap;
import java.util.Map;

public class StatSyncMessage implements IMessage {
   private int spEarned;
   private int spSpent;
   private int respecTokens;
   private long lastRespecTime;
   private boolean statsUnlocked;
   private int ceCharges;
   private int ceCap;
   private byte[] allocationData;

   public static StatSyncMessage fromPlayerData(PlayerStatData data, boolean unlocked, int ceCap) {
      StatSyncMessage msg = new StatSyncMessage();
      msg.spEarned = data.getSpEarned();
      msg.spSpent = data.getSpSpent();
      msg.respecTokens = data.getRespecTokens();
      msg.ceCap = ceCap;
      msg.lastRespecTime = data.getLastRespecTime();
      msg.statsUnlocked = unlocked;
      msg.ceCharges = data.getChakraEnhancementCharges();
      int count = 0;

      for(StatCategory cat : StatCategory.values()) {
         EnumMap<StatElement, Integer> map = data.getAllocationsForCategory(cat);
         if (map != null) {
            for(Integer val : map.values()) {
               if (val != null && val > 0) {
                  ++count;
               }
            }
         }
      }

      msg.allocationData = new byte[count * 3];
      int idx = 0;

      for(StatCategory cat : StatCategory.values()) {
         EnumMap<StatElement, Integer> map = data.getAllocationsForCategory(cat);
         if (map != null) {
            for(Map.Entry<StatElement, Integer> entry : map.entrySet()) {
               if (entry.getValue() != null && (Integer)entry.getValue() > 0) {
                  msg.allocationData[idx++] = (byte)cat.getId();
                  msg.allocationData[idx++] = (byte)((StatElement)entry.getKey()).getId();
                  msg.allocationData[idx++] = (byte)(Integer)entry.getValue();
               }
            }
         }
      }

      return msg;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.spEarned);
      buf.writeInt(this.spSpent);
      buf.writeInt(this.respecTokens);
      buf.writeLong(this.lastRespecTime);
      buf.writeBoolean(this.statsUnlocked);
      buf.writeInt(this.ceCharges);
      buf.writeInt(this.ceCap);
      buf.writeShort(this.allocationData != null ? this.allocationData.length : 0);
      if (this.allocationData != null) {
         buf.writeBytes(this.allocationData);
      }

   }

   public void fromBytes(ByteBuf buf) {
      this.spEarned = buf.readInt();
      this.spSpent = buf.readInt();
      this.respecTokens = buf.readInt();
      this.lastRespecTime = buf.readLong();
      this.statsUnlocked = buf.readBoolean();
      this.ceCharges = buf.readInt();
      this.ceCap = buf.readInt();
      int len = buf.readShort() & '\uffff';
      this.allocationData = new byte[len];
      if (len > 0) {
         buf.readBytes(this.allocationData);
      }

   }

   public PlayerStatData toPlayerData() {
      PlayerStatData data = new PlayerStatData();
      data.setSPEarned(this.spEarned);
      if (this.allocationData != null) {
         for(int i = 0; i + 2 < this.allocationData.length; i += 3) {
            StatCategory cat = StatCategory.fromId(this.allocationData[i]);
            StatElement el = StatElement.fromId(this.allocationData[i + 1]);
            int level = this.allocationData[i + 2] & 255;
            if (cat != null && el != null && level > 0) {
               data.getAllocationsForCategory(cat).put(el, level);
            }
         }
      }

      return data;
   }

   public int getSpEarned() {
      return this.spEarned;
   }

   public int getSpSpent() {
      return this.spSpent;
   }

   public int getAvailableSP() {
      return this.spEarned - this.spSpent;
   }

   public int getRespecTokens() {
      return this.respecTokens;
   }

   public long getLastRespecTime() {
      return this.lastRespecTime;
   }

   public boolean isStatsUnlocked() {
      return this.statsUnlocked;
   }

   public int getCeCharges() {
      return this.ceCharges;
   }

   public int getCeCap() {
      return this.ceCap;
   }

   public static class Handler implements IMessageHandler<StatSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(StatSyncMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> StatClientData.receive(message));
         return null;
      }
   }
}
