
package net.luck.narutoaddon.OtherCode.quest.pvp.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpManager;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpMissionTemplate;
import net.luck.narutoaddon.OtherCode.quest.pvp.VillageOrder;
import net.luck.narutoaddon.OtherCode.quest.pvp.VillageOrderManager;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.AdvisorManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class LeadershipActionMessage implements IMessage {
   public static final byte ACTION_ISSUE_ORDER_PLAYER = 0;
   public static final byte ACTION_ISSUE_ORDER_VILLAGE = 1;
   public static final byte ACTION_CANCEL_ORDER = 2;
   public static final byte ACTION_ACCEPT_LEADERSHIP_MISSION = 3;
   public static final byte ACTION_REROLL_LEADERSHIP_MISSION = 4;
   public static final byte ACTION_ABANDON_LEADERSHIP_MISSION = 5;
   public static final byte ACTION_ASSIGN_MISSION = 6;
   public static final byte ACTION_ACCEPT_ASSIGNED_MISSION = 7;
   private byte action;
   private byte targetVillageOrdinal;
   private long targetUUIDMost;
   private long targetUUIDLeast;
   private String orderId = "";
   private String targetPlayerName = "";
   private String templateId = "";

   public static LeadershipActionMessage issueOrderVillage(byte villageOrdinal) {
      LeadershipActionMessage msg = new LeadershipActionMessage();
      msg.action = 1;
      msg.targetVillageOrdinal = villageOrdinal;
      return msg;
   }

   public static LeadershipActionMessage issueOrderPlayer(UUID targetId, String targetName) {
      LeadershipActionMessage msg = new LeadershipActionMessage();
      msg.action = 0;
      msg.targetUUIDMost = targetId.getMostSignificantBits();
      msg.targetUUIDLeast = targetId.getLeastSignificantBits();
      msg.targetPlayerName = targetName != null ? targetName : "";
      return msg;
   }

   public static LeadershipActionMessage cancelOrder(String orderId) {
      LeadershipActionMessage msg = new LeadershipActionMessage();
      msg.action = 2;
      msg.orderId = orderId != null ? orderId : "";
      return msg;
   }

   public static LeadershipActionMessage acceptMission() {
      return simple((byte)3);
   }

   public static LeadershipActionMessage rerollMission() {
      return simple((byte)4);
   }

   public static LeadershipActionMessage abandonMission() {
      return simple((byte)5);
   }

   public static LeadershipActionMessage assignMission(String templateId) {
      LeadershipActionMessage msg = new LeadershipActionMessage();
      msg.action = 6;
      msg.templateId = templateId != null ? templateId : "";
      return msg;
   }

   public static LeadershipActionMessage acceptAssignedMission(String orderId) {
      LeadershipActionMessage msg = new LeadershipActionMessage();
      msg.action = 7;
      msg.orderId = orderId != null ? orderId : "";
      return msg;
   }

   private static LeadershipActionMessage simple(byte action) {
      LeadershipActionMessage msg = new LeadershipActionMessage();
      msg.action = action;
      return msg;
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readByte();
      this.targetVillageOrdinal = buf.readByte();
      this.targetUUIDMost = buf.readLong();
      this.targetUUIDLeast = buf.readLong();
      this.orderId = ByteBufUtils.readUTF8String(buf);
      this.targetPlayerName = ByteBufUtils.readUTF8String(buf);
      this.templateId = ByteBufUtils.readUTF8String(buf);
   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.action);
      buf.writeByte(this.targetVillageOrdinal);
      buf.writeLong(this.targetUUIDMost);
      buf.writeLong(this.targetUUIDLeast);
      ByteBufUtils.writeUTF8String(buf, this.orderId != null ? this.orderId : "");
      ByteBufUtils.writeUTF8String(buf, this.targetPlayerName != null ? this.targetPlayerName : "");
      ByteBufUtils.writeUTF8String(buf, this.templateId != null ? this.templateId : "");
   }

   public static class Handler implements IMessageHandler<LeadershipActionMessage, IMessage> {
      public IMessage onMessage(LeadershipActionMessage msg, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         if (player == null) {
            return null;
         } else {
            player.getServerWorld().addScheduledTask(() -> {
               UUID playerId = player.getUniqueID();
               VillageHelper.Village village = VillageHelper.getVillage(player);
               switch (msg.action) {
                  case 0:
                     if (!AdvisorManager.getInstance().hasLeadershipAuthority(playerId, village)) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have leadership authority."));
                        return;
                     }

                     UUID targetId = new UUID(msg.targetUUIDMost, msg.targetUUIDLeast);
                     VillageOrderManager.getInstance().issueOrder(playerId, village, VillageOrder.OrderType.TARGET_PLAYER, targetId, msg.targetPlayerName, (VillageHelper.Village)null, player.world);
                     break;
                  case 1:
                     if (!AdvisorManager.getInstance().hasLeadershipAuthority(playerId, village)) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have leadership authority."));
                        return;
                     }

                     VillageHelper.Village[] villages = VillageHelper.Village.values();
                     if (msg.targetVillageOrdinal >= 0 && msg.targetVillageOrdinal < villages.length) {
                        VillageHelper.Village target = villages[msg.targetVillageOrdinal];
                        VillageOrderManager.getInstance().issueOrder(playerId, village, VillageOrder.OrderType.TARGET_VILLAGE, (UUID)null, (String)null, target, player.world);
                     }
                     break;
                  case 2:
                     VillageOrderManager.getInstance().cancelOrder(playerId, village, msg.orderId, player.world);
                     break;
                  case 3:
                     PvpManager.getInstance().acceptLeadershipMission(playerId, player.world);
                     break;
                  case 4:
                     PvpManager.getInstance().rerollLeadershipOffer(playerId, player.world);
                     break;
                  case 5:
                     PvpManager.getInstance().abandonLeadershipMission(playerId, player.world);
                     break;
                  case 6:
                     if (!AdvisorManager.getInstance().hasLeadershipAuthority(playerId, village)) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have leadership authority."));
                        return;
                     }

                     PvpMissionTemplate template = PvpMissionTemplate.getLeadershipById(msg.templateId);
                     if (template != null && template.isAssignable()) {
                        VillageOrderManager.getInstance().issueAssignMissionOrder(playerId, village, template.getId(), template.getNamePattern(), player.world);
                     }
                     break;
                  case 7:
                     PvpManager.getInstance().acceptAssignedMission(playerId, msg.orderId, player.world);
               }

               PvpNetworkHelper.sendPvpSync(player);
            });
            return null;
         }
      }
   }
}
