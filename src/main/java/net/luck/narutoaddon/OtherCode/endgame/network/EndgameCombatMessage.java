
package net.luck.narutoaddon.OtherCode.endgame.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.endgame.gui.EndgameClientData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EndgameCombatMessage implements IMessage {
   public static final byte SYSTEM_OUTPOST = 0;
   public static final byte SYSTEM_INCURSION = 1;
   public static final byte SYSTEM_DEFENSE = 2;
   public static final byte SYSTEM_CONTRACT = 3;
   private int entityId;
   private String entityName;
   private float currentHP;
   private float maxHP;
   private int phase;
   private byte systemType;
   private boolean clearAll;
   private int themeColor;
   private int accentColor;

   public EndgameCombatMessage() {
   }

   public EndgameCombatMessage(int entityId, String entityName, float currentHP, float maxHP, int phase, byte systemType) {
      this(entityId, entityName, currentHP, maxHP, phase, systemType, 0, 0);
   }

   public EndgameCombatMessage(int entityId, String entityName, float currentHP, float maxHP, int phase, byte systemType, int themeColor, int accentColor) {
      this.entityId = entityId;
      this.entityName = entityName;
      this.currentHP = currentHP;
      this.maxHP = maxHP;
      this.phase = phase;
      this.systemType = systemType;
      this.clearAll = false;
      this.themeColor = themeColor;
      this.accentColor = accentColor;
   }

   public static EndgameCombatMessage clearAll() {
      EndgameCombatMessage msg = new EndgameCombatMessage();
      msg.clearAll = true;
      return msg;
   }

   public void fromBytes(ByteBuf buf) {
      this.clearAll = buf.readBoolean();
      if (!this.clearAll) {
         this.entityId = buf.readInt();
         this.entityName = ByteBufUtils.readUTF8String(buf);
         this.currentHP = buf.readFloat();
         this.maxHP = buf.readFloat();
         this.phase = buf.readInt();
         this.systemType = buf.readByte();
         this.themeColor = buf.readInt();
         this.accentColor = buf.readInt();
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.clearAll);
      if (!this.clearAll) {
         buf.writeInt(this.entityId);
         ByteBufUtils.writeUTF8String(buf, this.entityName);
         buf.writeFloat(this.currentHP);
         buf.writeFloat(this.maxHP);
         buf.writeInt(this.phase);
         buf.writeByte(this.systemType);
         buf.writeInt(this.themeColor);
         buf.writeInt(this.accentColor);
      }

   }

   public static class Handler implements IMessageHandler<EndgameCombatMessage, IMessage> {
      public IMessage onMessage(EndgameCombatMessage msg, MessageContext ctx) {
         if (ctx.side == Side.CLIENT) {
            this.handleClient(msg);
         }

         return null;
      }

      @SideOnly(Side.CLIENT)
      private void handleClient(EndgameCombatMessage msg) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            if (msg.clearAll) {
               EndgameClientData.getInstance().clearCombatBars();
            } else {
               EndgameClientData.getInstance().updateCombatBar(msg.entityId, msg.entityName, msg.currentHP, msg.maxHP, msg.phase, msg.themeColor, msg.accentColor);
            }

         });
      }
   }
}
