
package net.luck.narutoaddon.OtherCode.raid.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyPlayerListMessage implements IMessage {
   private List<PlayerEntry> players;

   public PartyPlayerListMessage() {
      this.players = new ArrayList();
   }

   public PartyPlayerListMessage(List<PlayerEntry> players) {
      this.players = (List<PlayerEntry>)(players != null ? players : new ArrayList());
   }

   public void fromBytes(ByteBuf buf) {
      int count = buf.readInt();
      this.players = new ArrayList(count);

      for(int i = 0; i < count; ++i) {
         UUID uuid = new UUID(buf.readLong(), buf.readLong());
         String name = ByteBufUtils.readUTF8String(buf);
         boolean hasPendingInvite = buf.readBoolean();
         this.players.add(new PlayerEntry(uuid, name, hasPendingInvite));
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.players.size());

      for(PlayerEntry entry : this.players) {
         buf.writeLong(entry.uuid.getMostSignificantBits());
         buf.writeLong(entry.uuid.getLeastSignificantBits());
         ByteBufUtils.writeUTF8String(buf, entry.name);
         buf.writeBoolean(entry.hasPendingInvite);
      }

   }

   public List<PlayerEntry> getPlayers() {
      return this.players;
   }

   public static class PlayerEntry {
      public final UUID uuid;
      public final String name;
      public final boolean hasPendingInvite;

      public PlayerEntry(UUID uuid, String name) {
         this(uuid, name, false);
      }

      public PlayerEntry(UUID uuid, String name, boolean hasPendingInvite) {
         this.uuid = uuid;
         this.name = name;
         this.hasPendingInvite = hasPendingInvite;
      }
   }

   public static class Handler implements IMessageHandler<PartyPlayerListMessage, IMessage> {
      public IMessage onMessage(PartyPlayerListMessage message, MessageContext ctx) {
         if (ctx.side == Side.CLIENT) {
            Minecraft.getMinecraft().addScheduledTask(() -> this.handleClient(message));
         }

         return null;
      }

      @SideOnly(Side.CLIENT)
      private void handleClient(PartyPlayerListMessage message) {
         RaidClientData.setAvailablePlayers(message.getPlayers());
      }
   }
}
