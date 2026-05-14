
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.network.PvpNetworkHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class WarRosterMessage implements IMessage {
   private byte warMode;
   private String targetVillage = "";
   private List<UUID> rosterUUIDs = new ArrayList();

   public static WarRosterMessage create(byte warMode, String targetVillage, Collection<UUID> roster) {
      WarRosterMessage msg = new WarRosterMessage();
      msg.warMode = warMode;
      msg.targetVillage = targetVillage != null ? targetVillage : "";
      msg.rosterUUIDs = new ArrayList(roster);
      return msg;
   }

   public void fromBytes(ByteBuf buf) {
      this.warMode = buf.readByte();
      this.targetVillage = ByteBufUtils.readUTF8String(buf);
      int count = buf.readByte() & 255;
      this.rosterUUIDs = new ArrayList();

      for(int i = 0; i < count; ++i) {
         long most = buf.readLong();
         long least = buf.readLong();
         this.rosterUUIDs.add(new UUID(most, least));
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.warMode);
      ByteBufUtils.writeUTF8String(buf, this.targetVillage != null ? this.targetVillage : "");
      int count = Math.min(this.rosterUUIDs.size(), 10);
      buf.writeByte(count);

      for(int i = 0; i < count; ++i) {
         UUID uuid = (UUID)this.rosterUUIDs.get(i);
         buf.writeLong(uuid.getMostSignificantBits());
         buf.writeLong(uuid.getLeastSignificantBits());
      }

   }

   public static class Handler implements IMessageHandler<WarRosterMessage, IMessage> {
      public IMessage onMessage(WarRosterMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         if (player == null) {
            return null;
         } else {
            player.getServerWorld().addScheduledTask(() -> this.handleRoster(player, message));
            return null;
         }
      }

      private void handleRoster(EntityPlayerMP player, WarRosterMessage msg) {
         UUID playerId = player.getUniqueID();
         VillageHelper.Village playerVillage = VillageHelper.getVillage(player);
         if (!AdvisorManager.getInstance().hasLeadershipAuthority(playerId, playerVillage)) {
            this.sendError(player, "You must be a Kage or Advisor to declare war.");
         } else {
            VillageHelper.Village target = this.findVillageByTeamName(msg.targetVillage);
            if (target != null && target != VillageHelper.Village.UNKNOWN) {
               if (msg.rosterUUIDs.isEmpty()) {
                  this.sendError(player, "You must select at least one warrior.");
               } else if (msg.rosterUUIDs.size() > 10) {
                  this.sendError(player, "Maximum 10 warriors allowed.");
               } else {
                  WarMode mode = WarMode.fromOrdinal(msg.warMode);
                  WarManager warManager = WarManager.getInstance();
                  WarInstance war = warManager.declareWar(playerId, playerVillage, target, mode, player.getServerWorld());
                  if (war == null) {
                     this.sendError(player, "Cannot declare war. Check cooldowns or existing wars.");
                  } else {
                     WarLobby lobby = war.getLobby();
                     if (lobby != null) {
                        lobby.setState(WarLobby.LobbyState.SELECTING);

                        for(UUID uuid : msg.rosterUUIDs) {
                           EntityPlayerMP rosterPlayer = player.getServerWorld().getMinecraftServer().getPlayerList().getPlayerByUUID(uuid);
                           if (rosterPlayer != null) {
                              VillageHelper.Village rosterVillage = VillageHelper.getVillage(rosterPlayer);
                              if (rosterVillage == playerVillage) {
                                 lobby.selectPlayer(playerVillage, uuid);
                              }
                           }
                        }

                        int rosterCount = playerVillage == war.getVillage1() ? lobby.getRoster1().size() : lobby.getRoster2().size();
                        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "[WAR] Roster submitted with " + rosterCount + " warriors. Waiting for " + target.villageName + " to respond."));
                     }

                     PvpNetworkHelper.sendWarSyncToAll();
                  }
               }
            } else {
               this.sendError(player, "Invalid target village.");
            }
         }
      }

      private VillageHelper.Village findVillageByTeamName(String teamName) {
         if (teamName != null && !teamName.isEmpty()) {
            for(VillageHelper.Village v : VillageHelper.Village.values()) {
               if (v != VillageHelper.Village.UNKNOWN && v.teamName.equalsIgnoreCase(teamName)) {
                  return v;
               }
            }

            return null;
         } else {
            return null;
         }
      }

      private void sendError(EntityPlayerMP player, String message) {
         player.sendMessage(new TextComponentString(TextFormatting.RED + "[WAR] " + message));
      }
   }
}
