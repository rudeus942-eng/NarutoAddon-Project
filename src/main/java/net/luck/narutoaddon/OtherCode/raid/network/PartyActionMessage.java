
package net.luck.narutoaddon.OtherCode.raid.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.raid.core.RaidModInit;
import net.luck.narutoaddon.OtherCode.raid.party.RaidParty;
import net.luck.narutoaddon.OtherCode.raid.party.RaidPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyActionMessage implements IMessage {
   public static final int ACTION_CREATE = 0;
   public static final int ACTION_INVITE = 1;
   public static final int ACTION_ACCEPT = 2;
   public static final int ACTION_DECLINE = 3;
   public static final int ACTION_KICK = 4;
   public static final int ACTION_LEAVE = 5;
   public static final int ACTION_READY = 6;
   public static final int ACTION_UNREADY = 7;
   public static final int ACTION_DISBAND = 8;
   public static final int ACTION_REQUEST_PLAYER_LIST = 9;
   public static final int ACTION_PROMOTE = 10;
   public static final int ACTION_CANCEL_INVITE = 11;
   private int action;
   private String targetPlayerName;
   private UUID targetUUID;

   public PartyActionMessage() {
   }

   public PartyActionMessage(int action) {
      this.action = action;
      this.targetPlayerName = "";
      this.targetUUID = null;
   }

   public PartyActionMessage(int action, String targetPlayerName) {
      this.action = action;
      this.targetPlayerName = targetPlayerName != null ? targetPlayerName : "";
      this.targetUUID = null;
   }

   public PartyActionMessage(int action, UUID targetUUID) {
      this.action = action;
      this.targetPlayerName = "";
      this.targetUUID = targetUUID;
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readInt();
      this.targetPlayerName = ByteBufUtils.readUTF8String(buf);
      boolean hasUUID = buf.readBoolean();
      if (hasUUID) {
         this.targetUUID = new UUID(buf.readLong(), buf.readLong());
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.action);
      ByteBufUtils.writeUTF8String(buf, this.targetPlayerName != null ? this.targetPlayerName : "");
      buf.writeBoolean(this.targetUUID != null);
      if (this.targetUUID != null) {
         buf.writeLong(this.targetUUID.getMostSignificantBits());
         buf.writeLong(this.targetUUID.getLeastSignificantBits());
      }

   }

   public static class Handler implements IMessageHandler<PartyActionMessage, IMessage> {
      public IMessage onMessage(PartyActionMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         player.getServerWorld().addScheduledTask(() -> this.handleAction(player, message));
         return null;
      }

      private void handleAction(EntityPlayerMP player, PartyActionMessage message) {
         RaidPartyStorage storage = RaidPartyStorage.get(player.world);
         if (storage != null) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               switch (message.action) {
                  case 0:
                     this.handleCreate(player, storage);
                     break;
                  case 1:
                     this.handleInvite(player, storage, server, message.targetPlayerName);
                     break;
                  case 2:
                     this.handleAcceptWithParty(player, storage, server, message.targetUUID);
                     break;
                  case 3:
                     this.handleDecline(player, storage, server);
                     break;
                  case 4:
                     this.handleKick(player, storage, message.targetUUID);
                     break;
                  case 5:
                     this.handleLeave(player, storage);
                     break;
                  case 6:
                     this.handleReady(player, storage, true);
                     break;
                  case 7:
                     this.handleReady(player, storage, false);
                     break;
                  case 8:
                     this.handleDisband(player, storage);
                     break;
                  case 9:
                     this.handleRequestPlayerList(player, storage, server);
                     break;
                  case 10:
                     this.handlePromote(player, storage, message.targetUUID);
                     break;
                  case 11:
                     this.handleCancelInvite(player, storage, server, message.targetPlayerName);
               }

            }
         }
      }

      private void handleCreate(EntityPlayerMP player, RaidPartyStorage storage) {
         if (storage.isInParty(player.getUniqueID())) {
            player.sendMessage(new TextComponentString("§cYou are already in a party."));
         } else {
            RaidParty party = new RaidParty(player);
            storage.registerParty(party);
            player.sendMessage(new TextComponentString("§aParty created! Invite players from the party menu."));
            RaidNetworkHelper.sendPartySync(party);
         }
      }

      private void handleInvite(EntityPlayerMP player, RaidPartyStorage storage, MinecraftServer server, String targetName) {
         RaidParty party = storage.getPlayerParty(player.getUniqueID());
         if (party == null) {
            player.sendMessage(new TextComponentString("§cYou are not in a party. Create one first."));
         } else {
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(targetName);
            if (target == null) {
               player.sendMessage(new TextComponentString("§cPlayer '" + targetName + "' not found or offline."));
            } else if (storage.isInParty(target.getUniqueID())) {
               player.sendMessage(new TextComponentString("§c" + targetName + " is already in a party."));
            } else {
               RaidParty.InviteResult result = party.invite(player, target);
               player.sendMessage(new TextComponentString(result.success ? "§a" + result.message : "§c" + result.message));
               if (result.success) {
                  storage.markDirty();
                  RaidPartyDataMessage inviteMsg = RaidPartyDataMessage.createInviteMessage(player.getName(), party.getPartyId());
                  RaidModInit.NETWORK.sendTo(inviteMsg, target);
                  target.sendMessage(new TextComponentString("§d§l[Party] §r§d" + player.getName() + " has invited you to their party! Check the Party GUI to respond."));
                  RaidNetworkHelper.sendPartySync(party);
               }

            }
         }
      }

      private void handleAccept(EntityPlayerMP player, RaidPartyStorage storage, MinecraftServer server) {
         this.handleAcceptWithParty(player, storage, server, (UUID)null);
      }

      private void handleAcceptWithParty(EntityPlayerMP player, RaidPartyStorage storage, MinecraftServer server, UUID specificPartyId) {
         UUID playerUUID = player.getUniqueID();
         RaidParty targetParty = null;
         if (specificPartyId != null) {
            for(RaidParty party : storage.getAllParties()) {
               if (party.getPartyId().equals(specificPartyId)) {
                  RaidParty.InviteResult result = party.acceptInvite(player);
                  if (result.success) {
                     targetParty = party;
                  }
                  break;
               }
            }
         }

         if (targetParty == null) {
            for(RaidParty party : storage.getAllParties()) {
               RaidParty.InviteResult result = party.acceptInvite(player);
               if (result.success) {
                  targetParty = party;
                  break;
               }
            }
         }

         if (targetParty == null) {
            player.sendMessage(new TextComponentString("§cYou don't have any pending party invites."));
         } else {
            storage.updatePlayerMapping(playerUUID, targetParty.getPartyId());
            storage.markDirty();
            RaidNetworkHelper.sendPartySync(targetParty);
         }
      }

      private void handleDecline(EntityPlayerMP player, RaidPartyStorage storage, MinecraftServer server) {
         for(RaidParty party : storage.getAllParties()) {
            RaidParty.InviteResult result = party.declineInvite(player);
            if (result.success) {
               storage.markDirty();
               player.sendMessage(new TextComponentString("§eInvite declined."));
               RaidNetworkHelper.sendPartySync(party);
               return;
            }
         }

         player.sendMessage(new TextComponentString("§cYou don't have any pending party invites."));
      }

      private void handleKick(EntityPlayerMP player, RaidPartyStorage storage, UUID targetUUID) {
         RaidParty party = storage.getPlayerParty(player.getUniqueID());
         if (party == null) {
            player.sendMessage(new TextComponentString("§cYou are not in a party."));
         } else if (targetUUID == null) {
            player.sendMessage(new TextComponentString("§cNo target specified."));
         } else {
            RaidParty.InviteResult result = party.kick(player, targetUUID);
            player.sendMessage(new TextComponentString(result.success ? "§a" + result.message : "§c" + result.message));
            if (result.success) {
               storage.updatePlayerMapping(targetUUID, (UUID)null);
               storage.markDirty();
               RaidNetworkHelper.sendPartySync(party);
               EntityPlayerMP kicked = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(targetUUID);
               if (kicked != null) {
                  RaidPartyDataMessage disbandMsg = new RaidPartyDataMessage(5, (RaidParty)null);
                  RaidModInit.NETWORK.sendTo(disbandMsg, kicked);
               }
            }

         }
      }

      private void handleLeave(EntityPlayerMP player, RaidPartyStorage storage) {
         RaidParty party = storage.getPlayerParty(player.getUniqueID());
         if (party == null) {
            player.sendMessage(new TextComponentString("§cYou are not in a party."));
         } else {
            RaidParty.InviteResult result = party.leave(player);
            player.sendMessage(new TextComponentString(result.success ? "§e" + result.message : "§c" + result.message));
            if (result.success) {
               storage.updatePlayerMapping(player.getUniqueID(), (UUID)null);
               storage.markDirty();
               RaidPartyDataMessage disbandMsg = new RaidPartyDataMessage(5, (RaidParty)null);
               RaidModInit.NETWORK.sendTo(disbandMsg, player);
               if (party.getState() != RaidParty.PartyState.COMPLETED && party.getSize() > 0) {
                  RaidNetworkHelper.sendPartySync(party);
               } else {
                  storage.removeParty(party.getPartyId());
               }
            }

         }
      }

      private void handleReady(EntityPlayerMP player, RaidPartyStorage storage, boolean ready) {
         RaidParty party = storage.getPlayerParty(player.getUniqueID());
         if (party == null) {
            player.sendMessage(new TextComponentString("§cYou are not in a party."));
         } else {
            RaidParty.InviteResult result = party.setReady(player.getUniqueID(), ready);
            if (!result.success) {
               player.sendMessage(new TextComponentString("§c" + result.message));
            }

            storage.markDirty();
            RaidNetworkHelper.sendPartySync(party);
         }
      }

      private void handleDisband(EntityPlayerMP player, RaidPartyStorage storage) {
         RaidParty party = storage.getPlayerParty(player.getUniqueID());
         if (party == null) {
            player.sendMessage(new TextComponentString("§cYou are not in a party."));
         } else if (!party.isLeader(player.getUniqueID())) {
            player.sendMessage(new TextComponentString("§cOnly the party leader can disband."));
         } else {
            party.broadcastMessage("§cThe party has been disbanded.");
            RaidPartyDataMessage disbandMsg = new RaidPartyDataMessage(5, (RaidParty)null);

            for(UUID memberId : party.getMemberUUIDs()) {
               storage.updatePlayerMapping(memberId, (UUID)null);
               EntityPlayerMP member = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(memberId);
               if (member != null) {
                  RaidModInit.NETWORK.sendTo(disbandMsg, member);
               }
            }

            storage.removeParty(party.getPartyId());
         }
      }

      private void handlePromote(EntityPlayerMP player, RaidPartyStorage storage, UUID targetUUID) {
         RaidParty party = storage.getPlayerParty(player.getUniqueID());
         if (party == null) {
            player.sendMessage(new TextComponentString("§cYou are not in a party."));
         } else if (targetUUID == null) {
            player.sendMessage(new TextComponentString("§cNo target specified."));
         } else {
            RaidParty.InviteResult result = party.promote(player, targetUUID);
            player.sendMessage(new TextComponentString(result.success ? "§a" + result.message : "§c" + result.message));
            if (result.success) {
               storage.markDirty();
               RaidNetworkHelper.sendPartySync(party);
            }

         }
      }

      private void handleCancelInvite(EntityPlayerMP player, RaidPartyStorage storage, MinecraftServer server, String targetName) {
         RaidParty party = storage.getPlayerParty(player.getUniqueID());
         if (party == null) {
            player.sendMessage(new TextComponentString("§cYou are not in a party."));
         } else {
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(targetName);
            if (target == null) {
               player.sendMessage(new TextComponentString("§cPlayer '" + targetName + "' not found."));
            } else {
               RaidParty.InviteResult result = party.cancelInvite(player, target.getUniqueID());
               player.sendMessage(new TextComponentString(result.success ? "§e" + result.message : "§c" + result.message));
               if (result.success) {
                  storage.markDirty();
                  RaidPartyDataMessage cancelMsg = new RaidPartyDataMessage(7, (RaidParty)null);
                  RaidModInit.NETWORK.sendTo(cancelMsg, target);
                  this.handleRequestPlayerList(player, storage, server);
                  RaidNetworkHelper.sendPartySync(party);
               }

            }
         }
      }

      private void handleRequestPlayerList(EntityPlayerMP player, RaidPartyStorage storage, MinecraftServer server) {
         RaidParty myParty = storage.getPlayerParty(player.getUniqueID());
         List<PartyPlayerListMessage.PlayerEntry> entries = new ArrayList();

         for(EntityPlayerMP onlinePlayer : server.getPlayerList().getPlayers()) {
            if (!onlinePlayer.getUniqueID().equals(player.getUniqueID()) && !storage.isInParty(onlinePlayer.getUniqueID())) {
               boolean hasPending = myParty != null && myParty.hasPendingInvite(onlinePlayer.getUniqueID());
               entries.add(new PartyPlayerListMessage.PlayerEntry(onlinePlayer.getUniqueID(), onlinePlayer.getName(), hasPending));
            }
         }

         entries.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
         PartyPlayerListMessage msg = new PartyPlayerListMessage(entries);
         RaidModInit.NETWORK.sendTo(msg, player);
      }
   }
}
