
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

import java.util.UUID;

public class WarActionMessage implements IMessage {
   public static final byte ACTION_DECLARE_WAR = 0;
   public static final byte ACTION_SURRENDER = 1;
   public static final byte ACTION_SELECT_PLAYER = 2;
   public static final byte ACTION_DESELECT_PLAYER = 3;
   public static final byte ACTION_LOCK_ROSTER = 4;
   public static final byte ACTION_ACCEPT = 5;
   public static final byte ACTION_DECLINE = 6;
   public static final byte ACTION_START = 7;
   private byte action;
   private byte modeByte;
   private String targetVillage;
   private long targetUUIDMost;
   private long targetUUIDLeast;

   public WarActionMessage() {
      this.targetVillage = "";
   }

   public WarActionMessage(byte action) {
      this.action = action;
      this.targetVillage = "";
   }

   public static WarActionMessage declareWar(byte warMode, String targetVillage) {
      WarActionMessage msg = new WarActionMessage((byte)0);
      msg.modeByte = warMode;
      msg.targetVillage = targetVillage != null ? targetVillage : "";
      return msg;
   }

   public static WarActionMessage selectPlayer(byte action, UUID targetPlayer) {
      WarActionMessage msg = new WarActionMessage(action);
      msg.targetUUIDMost = targetPlayer.getMostSignificantBits();
      msg.targetUUIDLeast = targetPlayer.getLeastSignificantBits();
      return msg;
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readByte();
      this.modeByte = buf.readByte();
      this.targetVillage = ByteBufUtils.readUTF8String(buf);
      this.targetUUIDMost = buf.readLong();
      this.targetUUIDLeast = buf.readLong();
   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.action);
      buf.writeByte(this.modeByte);
      ByteBufUtils.writeUTF8String(buf, this.targetVillage != null ? this.targetVillage : "");
      buf.writeLong(this.targetUUIDMost);
      buf.writeLong(this.targetUUIDLeast);
   }

   public static class Handler implements IMessageHandler<WarActionMessage, IMessage> {
      public IMessage onMessage(WarActionMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         if (player == null) {
            return null;
         } else {
            player.getServerWorld().addScheduledTask(() -> this.handleAction(player, message));
            return null;
         }
      }

      private void handleAction(EntityPlayerMP player, WarActionMessage msg) {
         WarManager warManager = WarManager.getInstance();
         UUID playerId = player.getUniqueID();
         VillageHelper.Village playerVillage = VillageHelper.getVillage(player);
         switch (msg.action) {
            case 0:
               if (!AdvisorManager.getInstance().hasLeadershipAuthority(playerId, playerVillage)) {
                  this.sendError(player, "You must be a Kage or Advisor to declare war.");
                  return;
               }

               VillageHelper.Village target = this.findVillageByTeamName(msg.targetVillage);
               if (target == null || target == VillageHelper.Village.UNKNOWN) {
                  this.sendError(player, "Invalid target village.");
                  return;
               }

               WarMode mode = WarMode.fromOrdinal(msg.modeByte);
               WarInstance war = warManager.declareWar(playerId, playerVillage, target, mode, player.getServerWorld());
               if (war == null) {
                  if (playerVillage == VillageHelper.Village.UNKNOWN) {
                     this.sendError(player, "You are not in a recognized village.");
                  } else if (playerVillage == target) {
                     this.sendError(player, "You cannot declare war on your own village.");
                  } else if (!warManager.canDeclareWar(playerVillage)) {
                     long cd = warManager.getRemainingCooldown(playerVillage);
                     if (cd > 0L) {
                        this.sendError(player, "Your village is on war cooldown (" + cd / 60000L + " min remaining).");
                     } else {
                        this.sendError(player, "Your village (" + playerVillage.villageName + ") is already at war.");
                     }
                  } else if (!warManager.canDeclareWar(target)) {
                     long cd = warManager.getRemainingCooldown(target);
                     if (cd > 0L) {
                        this.sendError(player, target.villageName + " is on war cooldown (" + cd / 60000L + " min remaining).");
                     } else {
                        this.sendError(player, target.villageName + " is already at war.");
                     }
                  } else {
                     KageManager km = KageManager.getInstance();
                     UUID kage = km.getKage(playerVillage);
                     if (kage != null && kage.equals(playerId)) {
                        if (km.getKage(target) == null) {
                           this.sendError(player, target.villageName + " has no Kage — cannot declare war against a leaderless village.");
                        } else {
                           this.sendError(player, "Cannot declare war. Unknown reason — check server logs.");
                        }
                     } else {
                        this.sendError(player, "Kage validation failed. You may not be recognized as " + playerVillage.villageName + "'s Kage.");
                     }
                  }
               } else {
                  PvpNetworkHelper.sendWarSyncToAll();
               }
               break;
            case 1:
               if (!AdvisorManager.getInstance().hasLeadershipAuthority(playerId, playerVillage)) {
                  this.sendError(player, "Only the Kage or Advisor can surrender.");
                  return;
               }

               if (!warManager.surrenderWar(playerId)) {
                  this.sendError(player, "No active war to surrender.");
               } else {
                  PvpNetworkHelper.sendWarSyncToAll();
               }
               break;
            case 2:
               if (!AdvisorManager.getInstance().hasLeadershipAuthority(playerId, playerVillage)) {
                  this.sendError(player, "Only the Kage or Advisor can select roster members.");
                  return;
               }

               UUID targetId = new UUID(msg.targetUUIDMost, msg.targetUUIDLeast);
               this.handleRosterSelect(player, playerVillage, targetId, true);
               break;
            case 3:
               if (!AdvisorManager.getInstance().hasLeadershipAuthority(playerId, playerVillage)) {
                  this.sendError(player, "Only the Kage or Advisor can deselect roster members.");
                  return;
               }

               UUID targetId = new UUID(msg.targetUUIDMost, msg.targetUUIDLeast);
               this.handleRosterSelect(player, playerVillage, targetId, false);
               break;
            case 4:
               if (!AdvisorManager.getInstance().hasLeadershipAuthority(playerId, playerVillage)) {
                  this.sendError(player, "Only the Kage or Advisor can lock the roster.");
                  return;
               }

               WarInstance war = warManager.getActiveWar(playerVillage);
               if (war == null || war.getLobby() == null) {
                  this.sendError(player, "No war lobby found.");
                  return;
               }

               if (!war.getLobby().lockRoster(playerVillage, playerId)) {
                  this.sendError(player, "Cannot lock roster. Make sure players are selected.");
               } else {
                  PvpNetworkHelper.sendWarSyncToAll();
               }
               break;
            case 5:
               WarInstance war = warManager.getActiveWar(playerVillage);
               if (war == null || war.getLobby() == null) {
                  this.sendError(player, "No war lobby found.");
                  return;
               }

               if (!war.getLobby().playerAccept(playerId)) {
                  this.sendError(player, "You are not on a roster.");
               } else {
                  PvpNetworkHelper.sendWarSyncToAll();
               }
               break;
            case 6:
               WarInstance war = warManager.getActiveWar(playerVillage);
               if (war == null || war.getLobby() == null) {
                  this.sendError(player, "No war lobby found.");
                  return;
               }

               war.getLobby().removePlayer(playerVillage, playerId);
               PvpNetworkHelper.sendWarSyncToAll();
               break;
            case 7:
               if (!AdvisorManager.getInstance().hasLeadershipAuthority(playerId, playerVillage)) {
                  this.sendError(player, "Only the Kage or Advisor can start the war.");
                  return;
               }

               WarInstance war = warManager.getActiveWar(playerVillage);
               if (war == null || war.getLobby() == null) {
                  this.sendError(player, "No war lobby found.");
                  return;
               }

               if (!war.getLobby().isReady()) {
                  this.sendError(player, "Both rosters must be locked and all players must accept.");
               }
         }

      }

      private void handleRosterSelect(EntityPlayerMP kage, VillageHelper.Village kageVillage, UUID targetId, boolean select) {
         WarManager warManager = WarManager.getInstance();
         WarInstance war = warManager.getActiveWar(kageVillage);
         if (war != null && war.getLobby() != null) {
            WarLobby lobby = war.getLobby();
            if (select) {
               if (!lobby.selectPlayer(kageVillage, targetId)) {
                  this.sendError(kage, "Cannot add player to roster. Roster may be full or locked.");
               } else {
                  PvpNetworkHelper.sendWarSyncToAll();
               }
            } else if (!lobby.removePlayer(kageVillage, targetId)) {
               this.sendError(kage, "Cannot remove player from roster.");
            } else {
               PvpNetworkHelper.sendWarSyncToAll();
            }

         } else {
            this.sendError(kage, "No war lobby found.");
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
