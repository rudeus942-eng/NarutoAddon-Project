
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.network.PvpNetworkHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class WarRosterSubmitMessage implements IMessage {
   private List<UUID> rosterUUIDs = new ArrayList();

   public static WarRosterSubmitMessage create(Collection<UUID> roster) {
      WarRosterSubmitMessage msg = new WarRosterSubmitMessage();
      msg.rosterUUIDs = new ArrayList(roster);
      return msg;
   }

   public void fromBytes(ByteBuf buf) {
      int count = buf.readByte() & 255;
      this.rosterUUIDs = new ArrayList();

      for(int i = 0; i < count; ++i) {
         long most = buf.readLong();
         long least = buf.readLong();
         this.rosterUUIDs.add(new UUID(most, least));
      }

   }

   public void toBytes(ByteBuf buf) {
      int count = Math.min(this.rosterUUIDs.size(), 10);
      buf.writeByte(count);

      for(int i = 0; i < count; ++i) {
         UUID uuid = (UUID)this.rosterUUIDs.get(i);
         buf.writeLong(uuid.getMostSignificantBits());
         buf.writeLong(uuid.getLeastSignificantBits());
      }

   }

   public static class Handler implements IMessageHandler<WarRosterSubmitMessage, IMessage> {
      public IMessage onMessage(WarRosterSubmitMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         if (player == null) {
            return null;
         } else {
            player.getServerWorld().addScheduledTask(() -> this.handleSubmit(player, message));
            return null;
         }
      }

      private void handleSubmit(EntityPlayerMP player, WarRosterSubmitMessage msg) {
         UUID playerId = player.getUniqueID();
         VillageHelper.Village playerVillage = VillageHelper.getVillage(player);
         if (!AdvisorManager.getInstance().hasLeadershipAuthority(playerId, playerVillage)) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "[WAR] You must be a Kage or Advisor to select a roster."));
         } else {
            WarManager warManager = WarManager.getInstance();
            WarInstance war = warManager.getActiveWar(playerVillage);
            if (war != null && war.getLobby() != null) {
               WarLobby lobby = war.getLobby();
               WarLobby.LobbyState state = lobby.getState();
               if (state != WarLobby.LobbyState.SELECTING && state != WarLobby.LobbyState.LOCKED_WAITING) {
                  player.sendMessage(new TextComponentString(TextFormatting.RED + "[WAR] Roster selection is not open."));
               } else {
                  boolean isVillage1 = playerVillage == war.getVillage1();
                  if (isVillage1 && lobby.isLocked1()) {
                     player.sendMessage(new TextComponentString(TextFormatting.RED + "[WAR] Your roster is already locked."));
                  } else if (!isVillage1 && lobby.isLocked2()) {
                     player.sendMessage(new TextComponentString(TextFormatting.RED + "[WAR] Your roster is already locked."));
                  } else if (msg.rosterUUIDs.isEmpty()) {
                     player.sendMessage(new TextComponentString(TextFormatting.RED + "[WAR] You must select at least one warrior."));
                  } else {
                     int added = 0;

                     for(UUID uuid : msg.rosterUUIDs) {
                        EntityPlayerMP rosterPlayer = player.getServerWorld().getMinecraftServer().getPlayerList().getPlayerByUUID(uuid);
                        if (rosterPlayer != null) {
                           VillageHelper.Village rosterVillage = VillageHelper.getVillage(rosterPlayer);
                           if (rosterVillage == playerVillage && lobby.selectPlayer(playerVillage, uuid)) {
                              ++added;
                           }
                        }
                     }

                     player.sendMessage(new TextComponentString(TextFormatting.GREEN + "[WAR] Roster submitted with " + added + " warriors."));
                     PvpNetworkHelper.sendWarSyncToAll();
                  }
               }
            } else {
               player.sendMessage(new TextComponentString(TextFormatting.RED + "[WAR] No active war lobby found."));
            }
         }
      }
   }
}
