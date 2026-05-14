
package net.luck.narutoaddon.OtherCode.endgame.contract;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.endgame.EndgameModInit;
import net.luck.narutoaddon.OtherCode.quest.network.QuestClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ContractNetworkMessage {
   public static void sendSync(EntityPlayerMP player) {
      ContractSavedData data = ContractSavedData.get(player.world);
      String acceptedId = data.getAcceptedContractId(player.getUniqueID());
      int[] used = data.getUsedToday(player.getUniqueID());
      SyncMessage msg = new SyncMessage(data.getActiveContracts(), acceptedId, used);
      EndgameModInit.NETWORK.sendTo(msg, player);
   }

   public static class SyncMessage implements IMessage {
      public List<ContractClientEntry> entries = new ArrayList();
      public String acceptedId = "";
      public int[] perRankUsedToday = new int[5];

      public SyncMessage() {
      }

      public SyncMessage(List<ContractDefinition> contracts, String acceptedId, int[] perRankUsedToday) {
         this.acceptedId = acceptedId == null ? "" : acceptedId;
         if (perRankUsedToday != null && perRankUsedToday.length == 5) {
            this.perRankUsedToday = perRankUsedToday;
         }

         for(ContractDefinition c : contracts) {
            this.entries.add(new ContractClientEntry(c.getId(), c.getRank().name(), c.getTargetName(), c.getLocationHint(), c.getHintX(), c.getHintZ()));
         }

      }

      public void toBytes(ByteBuf buf) {
         ByteBufUtils.writeUTF8String(buf, this.acceptedId);
         buf.writeInt(this.entries.size());

         for(ContractClientEntry e : this.entries) {
            ByteBufUtils.writeUTF8String(buf, e.id);
            ByteBufUtils.writeUTF8String(buf, e.rank);
            ByteBufUtils.writeUTF8String(buf, e.targetName);
            ByteBufUtils.writeUTF8String(buf, e.locationHint);
            buf.writeInt(e.hintX);
            buf.writeInt(e.hintZ);
         }

         for(int i = 0; i < 5; ++i) {
            buf.writeInt(this.perRankUsedToday[i]);
         }

      }

      public void fromBytes(ByteBuf buf) {
         this.acceptedId = ByteBufUtils.readUTF8String(buf);
         int n = buf.readInt();
         this.entries = new ArrayList(n);

         for(int i = 0; i < n; ++i) {
            String id = ByteBufUtils.readUTF8String(buf);
            String rank = ByteBufUtils.readUTF8String(buf);
            String targetName = ByteBufUtils.readUTF8String(buf);
            String locationHint = ByteBufUtils.readUTF8String(buf);
            int hintX = buf.readInt();
            int hintZ = buf.readInt();
            this.entries.add(new ContractClientEntry(id, rank, targetName, locationHint, hintX, hintZ));
         }

         this.perRankUsedToday = new int[5];
         if (buf.readableBytes() >= 20) {
            for(int i = 0; i < 5; ++i) {
               this.perRankUsedToday[i] = buf.readInt();
            }
         }

      }

      public static class Handler implements IMessageHandler<SyncMessage, IMessage> {
         @SideOnly(Side.CLIENT)
         public IMessage onMessage(SyncMessage msg, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
               ContractClientData.entries = msg.entries;
               ContractClientData.acceptedId = msg.acceptedId;
               ContractClientData.perRankUsedToday = msg.perRankUsedToday;
               if (msg.acceptedId != null && !msg.acceptedId.isEmpty()) {
                  ContractClientEntry active = null;

                  for(ContractClientEntry e : msg.entries) {
                     if (msg.acceptedId.equals(e.id)) {
                        active = e;
                        break;
                     }
                  }

                  if (active != null) {
                     QuestClientData.setContractMission(active.targetName, active.locationHint);
                  } else {
                     QuestClientData.clearContractMission();
                  }
               } else {
                  QuestClientData.clearContractMission();
               }

            });
            return null;
         }
      }
   }

   public static class ActionMessage implements IMessage {
      public static final int ACTION_REQUEST_SYNC = 0;
      public static final int ACTION_ACCEPT = 1;
      public static final int ACTION_ABANDON = 2;
      private int action;
      private String contractId = "";

      public static ActionMessage requestSync() {
         ActionMessage m = new ActionMessage();
         m.action = 0;
         return m;
      }

      public static ActionMessage accept(String contractId) {
         ActionMessage m = new ActionMessage();
         m.action = 1;
         m.contractId = contractId == null ? "" : contractId;
         return m;
      }

      public static ActionMessage abandon() {
         ActionMessage m = new ActionMessage();
         m.action = 2;
         return m;
      }

      public void toBytes(ByteBuf buf) {
         buf.writeInt(this.action);
         ByteBufUtils.writeUTF8String(buf, this.contractId);
      }

      public void fromBytes(ByteBuf buf) {
         this.action = buf.readInt();
         this.contractId = ByteBufUtils.readUTF8String(buf);
      }

      public static class Handler implements IMessageHandler<ActionMessage, IMessage> {
         public IMessage onMessage(ActionMessage msg, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
               switch (msg.action) {
                  case 0:
                  default:
                     break;
                  case 1:
                     ContractManager.getInstance().acceptContract(player, msg.contractId);
                     break;
                  case 2:
                     ContractManager.getInstance().abandonContract(player);
               }

               ContractNetworkMessage.sendSync(player);
            });
            return null;
         }
      }
   }

   public static class ContractClientEntry {
      public final String id;
      public final String rank;
      public final String targetName;
      public final String locationHint;
      public final int hintX;
      public final int hintZ;

      public ContractClientEntry(String id, String rank, String targetName, String locationHint, int hintX, int hintZ) {
         this.id = id;
         this.rank = rank;
         this.targetName = targetName;
         this.locationHint = locationHint;
         this.hintX = hintX;
         this.hintZ = hintZ;
      }
   }
}
