
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpClientData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class WarSyncMessage implements IMessage {
   private List<WarInfo> wars;
   private boolean isKage;
   private byte kageVillage;
   private long warCooldownRemaining;
   private boolean isAdvisor = false;
   private boolean hasWarAuthority = false;

   public WarSyncMessage() {
      this.wars = new ArrayList();
      this.isKage = false;
      this.kageVillage = -1;
      this.warCooldownRemaining = 0L;
      this.isAdvisor = false;
      this.hasWarAuthority = false;
   }

   public WarSyncMessage(List<WarInfo> wars, boolean isKage, byte kageVillage, long warCooldownRemaining) {
      this.wars = (List<WarInfo>)(wars != null ? wars : new ArrayList());
      this.isKage = isKage;
      this.kageVillage = kageVillage;
      this.warCooldownRemaining = warCooldownRemaining;
   }

   public void setAdvisorInfo(boolean isAdvisor, boolean hasWarAuthority) {
      this.isAdvisor = isAdvisor;
      this.hasWarAuthority = hasWarAuthority;
   }

   public void fromBytes(ByteBuf buf) {
      int numWars = buf.readByte() & 255;
      this.wars = new ArrayList();

      for(int i = 0; i < numWars; ++i) {
         WarInfo info = new WarInfo();
         info.warId = ByteBufUtils.readUTF8String(buf);
         info.mode = buf.readByte();
         info.village1 = buf.readByte();
         info.village2 = buf.readByte();
         info.score1 = buf.readInt();
         info.score2 = buf.readInt();
         info.timeRemainingMs = buf.readLong();
         int numKillers1 = buf.readByte() & 255;
         info.topKillers1 = new ArrayList();

         for(int j = 0; j < numKillers1; ++j) {
            TopKiller tk = new TopKiller();
            tk.playerName = ByteBufUtils.readUTF8String(buf);
            tk.kills = buf.readInt();
            info.topKillers1.add(tk);
         }

         int numKillers2 = buf.readByte() & 255;
         info.topKillers2 = new ArrayList();

         for(int j = 0; j < numKillers2; ++j) {
            TopKiller tk = new TopKiller();
            tk.playerName = ByteBufUtils.readUTF8String(buf);
            tk.kills = buf.readInt();
            info.topKillers2.add(tk);
         }

         info.hasLobby = buf.readBoolean();
         if (info.hasLobby) {
            info.lobbyState = buf.readByte();
            int r1Count = buf.readByte() & 255;
            info.roster1Names = new ArrayList();

            for(int j = 0; j < r1Count; ++j) {
               info.roster1Names.add(ByteBufUtils.readUTF8String(buf));
            }

            int r2Count = buf.readByte() & 255;
            info.roster2Names = new ArrayList();

            for(int j = 0; j < r2Count; ++j) {
               info.roster2Names.add(ByteBufUtils.readUTF8String(buf));
            }

            info.acceptedCount1 = buf.readByte() & 255;
            info.acceptedCount2 = buf.readByte() & 255;
            info.challengeTimeRemaining = buf.readLong();
            info.locked1 = buf.readBoolean();
            info.locked2 = buf.readBoolean();
         }

         info.currentRound = buf.readByte() & 255;
         info.roundsWon1 = buf.readByte() & 255;
         info.roundsWon2 = buf.readByte() & 255;
         this.wars.add(info);
      }

      this.isKage = buf.readBoolean();
      this.kageVillage = buf.readByte();
      this.warCooldownRemaining = buf.readLong();
      if (buf.readableBytes() >= 2) {
         this.isAdvisor = buf.readBoolean();
         this.hasWarAuthority = buf.readBoolean();
      } else {
         this.isAdvisor = false;
         this.hasWarAuthority = false;
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.wars.size());

      for(WarInfo info : this.wars) {
         ByteBufUtils.writeUTF8String(buf, info.warId != null ? info.warId : "");
         buf.writeByte(info.mode);
         buf.writeByte(info.village1);
         buf.writeByte(info.village2);
         buf.writeInt(info.score1);
         buf.writeInt(info.score2);
         buf.writeLong(info.timeRemainingMs);
         List<TopKiller> tk1 = (List<TopKiller>)(info.topKillers1 != null ? info.topKillers1 : new ArrayList());
         buf.writeByte(tk1.size());

         for(TopKiller tk : tk1) {
            ByteBufUtils.writeUTF8String(buf, tk.playerName != null ? tk.playerName : "");
            buf.writeInt(tk.kills);
         }

         List<TopKiller> tk2 = (List<TopKiller>)(info.topKillers2 != null ? info.topKillers2 : new ArrayList());
         buf.writeByte(tk2.size());

         for(TopKiller tk : tk2) {
            ByteBufUtils.writeUTF8String(buf, tk.playerName != null ? tk.playerName : "");
            buf.writeInt(tk.kills);
         }

         buf.writeBoolean(info.hasLobby);
         if (info.hasLobby) {
            buf.writeByte(info.lobbyState);
            List<String> r1 = (List<String>)(info.roster1Names != null ? info.roster1Names : new ArrayList());
            buf.writeByte(r1.size());

            for(String name : r1) {
               ByteBufUtils.writeUTF8String(buf, name != null ? name : "");
            }

            List<String> r2 = (List<String>)(info.roster2Names != null ? info.roster2Names : new ArrayList());
            buf.writeByte(r2.size());

            for(String name : r2) {
               ByteBufUtils.writeUTF8String(buf, name != null ? name : "");
            }

            buf.writeByte(info.acceptedCount1);
            buf.writeByte(info.acceptedCount2);
            buf.writeLong(info.challengeTimeRemaining);
            buf.writeBoolean(info.locked1);
            buf.writeBoolean(info.locked2);
         }

         buf.writeByte(info.currentRound);
         buf.writeByte(info.roundsWon1);
         buf.writeByte(info.roundsWon2);
      }

      buf.writeBoolean(this.isKage);
      buf.writeByte(this.kageVillage);
      buf.writeLong(this.warCooldownRemaining);
      buf.writeBoolean(this.isAdvisor);
      buf.writeBoolean(this.hasWarAuthority);
   }

   public List<WarInfo> getWars() {
      return this.wars;
   }

   public boolean isKage() {
      return this.isKage;
   }

   public byte getKageVillage() {
      return this.kageVillage;
   }

   public long getWarCooldownRemaining() {
      return this.warCooldownRemaining;
   }

   public static class WarInfo {
      public String warId;
      public byte mode;
      public byte village1;
      public byte village2;
      public int score1;
      public int score2;
      public long timeRemainingMs;
      public List<TopKiller> topKillers1;
      public List<TopKiller> topKillers2;
      public boolean hasLobby;
      public byte lobbyState;
      public List<String> roster1Names;
      public List<String> roster2Names;
      public int acceptedCount1;
      public int acceptedCount2;
      public long challengeTimeRemaining;
      public boolean locked1;
      public boolean locked2;
      public int currentRound;
      public int roundsWon1;
      public int roundsWon2;
   }

   public static class TopKiller {
      public String playerName;
      public int kills;
   }

   public static class Handler implements IMessageHandler<WarSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(WarSyncMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            PvpClientData.setWarData(message.wars, message.isKage, message.kageVillage, message.warCooldownRemaining);
            PvpClientData.isAdvisor = message.isAdvisor;
            PvpClientData.hasWarAuthority = message.hasWarAuthority;
         });
         return null;
      }
   }
}
