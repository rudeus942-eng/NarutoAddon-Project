package net.luck.narutoaddon.OtherCode.akatsuki.contract;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.akatsuki.network.AkatsukiClientData;
import net.luck.narutoaddon.OtherCode.quest.gui.QuestLogGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ContractSyncMessage implements IMessage {
   private boolean isAkatsuki;
   private List<ContractEntry> entries = new ArrayList();

   public void setIsAkatsuki(boolean isAkatsuki) {
      this.isAkatsuki = isAkatsuki;
   }

   public void addEntry(ContractEntry entry) {
      this.entries.add(entry);
   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.isAkatsuki);
      buf.writeInt(this.entries.size());

      for(ContractEntry e : this.entries) {
         ByteBufUtils.writeUTF8String(buf, e.id);
         ByteBufUtils.writeUTF8String(buf, e.posterName);
         buf.writeInt(e.typeOrdinal);
         ByteBufUtils.writeUTF8String(buf, e.targetName);
         buf.writeInt(e.ryoAmount);
         buf.writeInt(e.statusOrdinal);
         ByteBufUtils.writeUTF8String(buf, e.assignee1);
         ByteBufUtils.writeUTF8String(buf, e.assignee2);
         buf.writeInt(e.assigneeCount);
         ByteBufUtils.writeUTF8String(buf, e.targetZoneId);
         ByteBufUtils.writeUTF8String(buf, e.posterVillage);
      }

   }

   public void fromBytes(ByteBuf buf) {
      this.isAkatsuki = buf.readBoolean();
      int count = buf.readInt();
      this.entries = new ArrayList(count);

      for(int i = 0; i < count; ++i) {
         String id = ByteBufUtils.readUTF8String(buf);
         String posterName = ByteBufUtils.readUTF8String(buf);
         int typeOrd = buf.readInt();
         String targetName = ByteBufUtils.readUTF8String(buf);
         int ryoAmount = buf.readInt();
         int statusOrd = buf.readInt();
         String a1 = ByteBufUtils.readUTF8String(buf);
         String a2 = ByteBufUtils.readUTF8String(buf);
         int aC = buf.readInt();
         String zoneId = ByteBufUtils.readUTF8String(buf);
         String posterVillage = ByteBufUtils.readUTF8String(buf);
         this.entries.add(new ContractEntry(id, posterName, typeOrd, targetName, ryoAmount, statusOrd, a1, a2, aC, zoneId, posterVillage));
      }

   }

   public static class ContractEntry {
      public final String id;
      public final String posterName;
      public final int typeOrdinal;
      public final String targetName;
      public final int ryoAmount;
      public final int statusOrdinal;
      public final String assignee1;
      public final String assignee2;
      public final int assigneeCount;
      public final String targetZoneId;
      public final String posterVillage;

      public ContractEntry(String id, String posterName, int typeOrdinal, String targetName, int ryoAmount, int statusOrdinal, String assignee1, String assignee2, int assigneeCount, String targetZoneId, String posterVillage) {
         this.id = id;
         this.posterName = posterName;
         this.typeOrdinal = typeOrdinal;
         this.targetName = targetName;
         this.ryoAmount = ryoAmount;
         this.statusOrdinal = statusOrdinal;
         this.assignee1 = assignee1;
         this.assignee2 = assignee2;
         this.assigneeCount = assigneeCount;
         this.targetZoneId = targetZoneId;
         this.posterVillage = posterVillage != null ? posterVillage : "";
      }
   }

   public static class Handler implements IMessageHandler<ContractSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(ContractSyncMessage msg, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            if (msg.isAkatsuki) {
               AkatsukiClientData.openContracts.clear();
               AkatsukiClientData.assignedContracts.clear();
               String myName = Minecraft.getMinecraft().player != null ? Minecraft.getMinecraft().player.getName() : "";

               for(ContractEntry e : msg.entries) {
                  AkatsukiClientData.ContractClientEntry cce = new AkatsukiClientData.ContractClientEntry(e.id, e.posterName, e.typeOrdinal, e.targetName, e.ryoAmount, e.statusOrdinal, e.assignee1, e.assignee2, e.targetZoneId, e.posterVillage);
                  boolean isMine = myName.equals(e.assignee1) || myName.equals(e.assignee2);
                  if (isMine) {
                     AkatsukiClientData.assignedContracts.add(cce);
                  } else if (e.statusOrdinal == Contract.ContractStatus.OPEN.ordinal()) {
                     AkatsukiClientData.openContracts.add(cce);
                  } else {
                     AkatsukiClientData.assignedContracts.add(cce);
                  }
               }
            } else {
               AkatsukiClientData.myContracts.clear();

               for(ContractEntry e : msg.entries) {
                  AkatsukiClientData.myContracts.add(new AkatsukiClientData.ContractClientEntry(e.id, e.posterName, e.typeOrdinal, e.targetName, e.ryoAmount, e.statusOrdinal, e.assignee1, e.assignee2, e.targetZoneId, e.posterVillage));
               }
            }

            if (Minecraft.getMinecraft().currentScreen instanceof QuestLogGui) {
               ((QuestLogGui)Minecraft.getMinecraft().currentScreen).rebuildButtons();
            }

         });
         return null;
      }
   }
}
