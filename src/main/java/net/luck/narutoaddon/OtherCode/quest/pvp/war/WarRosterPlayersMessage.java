
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

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

public class WarRosterPlayersMessage implements IMessage {
   private byte warMode;
   private String targetVillageTeamName;
   private List<UUID> playerUUIDs;
   private List<String> playerNames;
   private boolean defenderMode;

   public WarRosterPlayersMessage() {
      this.targetVillageTeamName = "";
      this.playerUUIDs = new ArrayList();
      this.playerNames = new ArrayList();
   }

   public WarRosterPlayersMessage(byte warMode, String targetVillageTeamName, List<UUID> playerUUIDs, List<String> playerNames) {
      this(warMode, targetVillageTeamName, playerUUIDs, playerNames, false);
   }

   public WarRosterPlayersMessage(byte warMode, String targetVillageTeamName, List<UUID> playerUUIDs, List<String> playerNames, boolean defenderMode) {
      this.warMode = warMode;
      this.targetVillageTeamName = targetVillageTeamName != null ? targetVillageTeamName : "";
      this.playerUUIDs = (List<UUID>)(playerUUIDs != null ? playerUUIDs : new ArrayList());
      this.playerNames = (List<String>)(playerNames != null ? playerNames : new ArrayList());
      this.defenderMode = defenderMode;
   }

   public void fromBytes(ByteBuf buf) {
      this.warMode = buf.readByte();
      this.targetVillageTeamName = ByteBufUtils.readUTF8String(buf);
      int count = buf.readInt();
      this.playerUUIDs = new ArrayList(count);
      this.playerNames = new ArrayList(count);

      for(int i = 0; i < count; ++i) {
         long most = buf.readLong();
         long least = buf.readLong();
         this.playerUUIDs.add(new UUID(most, least));
         this.playerNames.add(ByteBufUtils.readUTF8String(buf));
      }

      this.defenderMode = buf.readBoolean();
   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.warMode);
      ByteBufUtils.writeUTF8String(buf, this.targetVillageTeamName != null ? this.targetVillageTeamName : "");
      int count = this.playerUUIDs.size();
      buf.writeInt(count);

      for(int i = 0; i < count; ++i) {
         UUID uuid = (UUID)this.playerUUIDs.get(i);
         buf.writeLong(uuid.getMostSignificantBits());
         buf.writeLong(uuid.getLeastSignificantBits());
         ByteBufUtils.writeUTF8String(buf, this.playerNames.get(i) != null ? (String)this.playerNames.get(i) : "");
      }

      buf.writeBoolean(this.defenderMode);
   }

   public static class Handler implements IMessageHandler<WarRosterPlayersMessage, IMessage> {
      public IMessage onMessage(WarRosterPlayersMessage message, MessageContext ctx) {
         handleClient(message);
         return null;
      }

      @SideOnly(Side.CLIENT)
      private static void handleClient(WarRosterPlayersMessage message) {
         Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(new WarRosterGui(message.warMode, message.targetVillageTeamName, message.playerUUIDs, message.playerNames, message.defenderMode)));
      }
   }
}
