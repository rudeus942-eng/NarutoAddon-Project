
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpModInit;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WarRosterRequestMessage implements IMessage {
   private byte warMode;
   private String targetVillageTeamName;
   private boolean defenderMode;

   public WarRosterRequestMessage() {
      this.targetVillageTeamName = "";
   }

   public WarRosterRequestMessage(byte warMode, String targetVillageTeamName) {
      this.warMode = warMode;
      this.targetVillageTeamName = targetVillageTeamName != null ? targetVillageTeamName : "";
      this.defenderMode = false;
   }

   public WarRosterRequestMessage(byte warMode, String targetVillageTeamName, boolean defenderMode) {
      this.warMode = warMode;
      this.targetVillageTeamName = targetVillageTeamName != null ? targetVillageTeamName : "";
      this.defenderMode = defenderMode;
   }

   public void fromBytes(ByteBuf buf) {
      this.warMode = buf.readByte();
      this.targetVillageTeamName = ByteBufUtils.readUTF8String(buf);
      this.defenderMode = buf.readBoolean();
   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.warMode);
      ByteBufUtils.writeUTF8String(buf, this.targetVillageTeamName != null ? this.targetVillageTeamName : "");
      buf.writeBoolean(this.defenderMode);
   }

   public static class Handler implements IMessageHandler<WarRosterRequestMessage, IMessage> {
      public IMessage onMessage(WarRosterRequestMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         if (player == null) {
            return null;
         } else {
            player.getServerWorld().addScheduledTask(() -> this.handleRequest(player, message));
            return null;
         }
      }

      private void handleRequest(EntityPlayerMP player, WarRosterRequestMessage msg) {
         UUID playerId = player.getUniqueID();
         VillageHelper.Village playerVillage = VillageHelper.getVillage(player);
         if (!AdvisorManager.getInstance().hasLeadershipAuthority(playerId, playerVillage)) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "[WAR] You must be a Kage or Advisor to select a roster."));
         } else {
            List<UUID> playerUUIDs = new ArrayList();
            List<String> playerNames = new ArrayList();

            for(EntityPlayerMP onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
               VillageHelper.Village onlineVillage = VillageHelper.getVillage(onlinePlayer);
               if (onlineVillage == playerVillage && onlineVillage != VillageHelper.Village.UNKNOWN) {
                  playerUUIDs.add(onlinePlayer.getUniqueID());
                  playerNames.add(onlinePlayer.getName());
               }
            }

            WarRosterPlayersMessage response = new WarRosterPlayersMessage(msg.warMode, msg.targetVillageTeamName, playerUUIDs, playerNames, msg.defenderMode);
            PvpModInit.NETWORK.sendTo(response, player);
         }
      }
   }
}
