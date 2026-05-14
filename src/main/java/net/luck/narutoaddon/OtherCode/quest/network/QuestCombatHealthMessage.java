
package net.luck.narutoaddon.OtherCode.quest.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class QuestCombatHealthMessage implements IMessage {
   public static final int ACTION_UPDATE = 0;
   public static final int ACTION_CLEAR = 1;
   private int action;
   private EnemyHealthEntry[] entries;

   public static QuestCombatHealthMessage update(EnemyHealthEntry[] entries) {
      QuestCombatHealthMessage msg = new QuestCombatHealthMessage();
      msg.action = 0;
      msg.entries = entries;
      return msg;
   }

   public static QuestCombatHealthMessage clear() {
      QuestCombatHealthMessage msg = new QuestCombatHealthMessage();
      msg.action = 1;
      msg.entries = new EnemyHealthEntry[0];
      return msg;
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readByte();
      int count = buf.readInt();
      this.entries = new EnemyHealthEntry[count];

      for(int i = 0; i < count; ++i) {
         this.entries[i] = new EnemyHealthEntry();
         this.entries[i].entityName = ByteBufUtils.readUTF8String(buf);
         this.entries[i].entityId = buf.readInt();
         this.entries[i].currentHP = buf.readFloat();
         this.entries[i].maxHP = buf.readFloat();
         this.entries[i].themeColor = buf.readInt();
         this.entries[i].accentColor = buf.readInt();
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.action);
      int count = Math.min(this.entries != null ? this.entries.length : 0, 64);
      buf.writeInt(count);

      for(int i = 0; i < count; ++i) {
         ByteBufUtils.writeUTF8String(buf, this.entries[i].entityName != null ? this.entries[i].entityName : "Enemy");
         buf.writeInt(this.entries[i].entityId);
         buf.writeFloat(this.entries[i].currentHP);
         buf.writeFloat(this.entries[i].maxHP);
         buf.writeInt(this.entries[i].themeColor);
         buf.writeInt(this.entries[i].accentColor);
      }

   }

   public static class EnemyHealthEntry {
      public String entityName;
      public int entityId;
      public float currentHP;
      public float maxHP;
      public int themeColor;
      public int accentColor;

      public EnemyHealthEntry() {
      }

      public EnemyHealthEntry(String entityName, int entityId, float currentHP, float maxHP, int themeColor, int accentColor) {
         this.entityName = entityName;
         this.entityId = entityId;
         this.currentHP = currentHP;
         this.maxHP = maxHP;
         this.themeColor = themeColor;
         this.accentColor = accentColor;
      }
   }

   public static class Handler implements IMessageHandler<QuestCombatHealthMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(QuestCombatHealthMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            if (message.action == 1) {
               QuestCombatClientData.clear();
            } else if (message.action == 0) {
               QuestCombatClientData.update(message.entries);
            }

         });
         return null;
      }
   }
}
