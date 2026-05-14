
package net.luck.narutoaddon.OtherCode.quest.pvp.network;

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

public class TournamentSyncMessage implements IMessage {
   private boolean hasTournament;
   private byte state;
   private String hostName = "";
   private int participantCount;
   private long signupEndTime;
   private byte minRank;
   private byte maxRank;
   private boolean isPlayerSignedUp;
   private int matchTimeoutSeconds;
   private boolean villageOnly;
   private boolean hasBracket;
   private int totalRounds;
   private int currentRound;
   private List<MatchClientInfo> matches = new ArrayList();
   private boolean isInMatch;
   private String opponentName = "";
   private long matchStartTime;

   public static TournamentSyncMessage noTournament() {
      TournamentSyncMessage msg = new TournamentSyncMessage();
      msg.hasTournament = false;
      return msg;
   }

   public void setHasTournament(boolean hasTournament) {
      this.hasTournament = hasTournament;
   }

   public void setTournamentInfo(byte state, String hostName, int participantCount, long signupEndTime, byte minRank, byte maxRank, boolean isPlayerSignedUp, int matchTimeoutSeconds, boolean villageOnly) {
      this.hasTournament = true;
      this.state = state;
      this.hostName = hostName != null ? hostName : "";
      this.participantCount = participantCount;
      this.signupEndTime = signupEndTime;
      this.minRank = minRank;
      this.maxRank = maxRank;
      this.isPlayerSignedUp = isPlayerSignedUp;
      this.matchTimeoutSeconds = matchTimeoutSeconds;
      this.villageOnly = villageOnly;
   }

   public void setBracketData(int totalRounds, int currentRound, List<MatchClientInfo> matches) {
      this.hasBracket = true;
      this.totalRounds = totalRounds;
      this.currentRound = currentRound;
      this.matches = (List<MatchClientInfo>)(matches != null ? matches : new ArrayList());
   }

   public void setActiveMatchInfo(String opponentName, long matchStartTime) {
      this.isInMatch = true;
      this.opponentName = opponentName != null ? opponentName : "";
      this.matchStartTime = matchStartTime;
   }

   public void fromBytes(ByteBuf buf) {
      this.hasTournament = buf.readBoolean();
      if (this.hasTournament) {
         this.state = buf.readByte();
         this.hostName = ByteBufUtils.readUTF8String(buf);
         this.participantCount = buf.readInt();
         this.signupEndTime = buf.readLong();
         this.minRank = buf.readByte();
         this.maxRank = buf.readByte();
         this.isPlayerSignedUp = buf.readBoolean();
         this.matchTimeoutSeconds = buf.readInt();
         this.villageOnly = buf.readBoolean();
         this.hasBracket = buf.readBoolean();
         if (this.hasBracket) {
            this.totalRounds = buf.readInt();
            this.currentRound = buf.readInt();
            int matchCount = buf.readInt();
            this.matches = new ArrayList();

            for(int i = 0; i < matchCount; ++i) {
               MatchClientInfo info = new MatchClientInfo();
               info.matchIndex = buf.readInt();
               info.round = buf.readInt();
               info.position = buf.readInt();
               info.player1Name = ByteBufUtils.readUTF8String(buf);
               info.player2Name = ByteBufUtils.readUTF8String(buf);
               info.matchState = buf.readByte();
               info.winnerName = ByteBufUtils.readUTF8String(buf);
               this.matches.add(info);
            }
         }

         this.isInMatch = buf.readBoolean();
         if (this.isInMatch) {
            this.opponentName = ByteBufUtils.readUTF8String(buf);
            this.matchStartTime = buf.readLong();
         }
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.hasTournament);
      if (this.hasTournament) {
         buf.writeByte(this.state);
         ByteBufUtils.writeUTF8String(buf, this.hostName != null ? this.hostName : "");
         buf.writeInt(this.participantCount);
         buf.writeLong(this.signupEndTime);
         buf.writeByte(this.minRank);
         buf.writeByte(this.maxRank);
         buf.writeBoolean(this.isPlayerSignedUp);
         buf.writeInt(this.matchTimeoutSeconds);
         buf.writeBoolean(this.villageOnly);
         buf.writeBoolean(this.hasBracket);
         if (this.hasBracket) {
            buf.writeInt(this.totalRounds);
            buf.writeInt(this.currentRound);
            buf.writeInt(this.matches.size());

            for(MatchClientInfo info : this.matches) {
               buf.writeInt(info.matchIndex);
               buf.writeInt(info.round);
               buf.writeInt(info.position);
               ByteBufUtils.writeUTF8String(buf, info.player1Name != null ? info.player1Name : "");
               ByteBufUtils.writeUTF8String(buf, info.player2Name != null ? info.player2Name : "");
               buf.writeByte(info.matchState);
               ByteBufUtils.writeUTF8String(buf, info.winnerName != null ? info.winnerName : "");
            }
         }

         buf.writeBoolean(this.isInMatch);
         if (this.isInMatch) {
            ByteBufUtils.writeUTF8String(buf, this.opponentName != null ? this.opponentName : "");
            buf.writeLong(this.matchStartTime);
         }
      }

   }

   public static class MatchClientInfo {
      public int matchIndex;
      public int round;
      public int position;
      public String player1Name = "";
      public String player2Name = "";
      public byte matchState;
      public String winnerName = "";

      public MatchClientInfo() {
      }

      public MatchClientInfo(int matchIndex, int round, int position, String player1Name, String player2Name, byte matchState, String winnerName) {
         this.matchIndex = matchIndex;
         this.round = round;
         this.position = position;
         this.player1Name = player1Name != null ? player1Name : "";
         this.player2Name = player2Name != null ? player2Name : "";
         this.matchState = matchState;
         this.winnerName = winnerName != null ? winnerName : "";
      }
   }

   public static class Handler implements IMessageHandler<TournamentSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(TournamentSyncMessage msg, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            if (!msg.hasTournament) {
               PvpClientData.tournamentInfo = null;
            } else {
               List<PvpClientData.MatchClientInfo> clientMatches = new ArrayList();
               if (msg.hasBracket) {
                  for(MatchClientInfo info : msg.matches) {
                     clientMatches.add(new PvpClientData.MatchClientInfo(info.matchIndex, info.round, info.position, info.player1Name, info.player2Name, info.matchState, info.winnerName));
                  }
               }

               PvpClientData.tournamentInfo = new PvpClientData.TournamentClientInfo(msg.state, msg.hostName, msg.participantCount, msg.signupEndTime, msg.minRank, msg.maxRank, msg.isPlayerSignedUp, msg.matchTimeoutSeconds, msg.hasBracket, msg.totalRounds, msg.currentRound, clientMatches, msg.isInMatch, msg.opponentName, msg.matchStartTime, msg.villageOnly);
            }
         });
         return null;
      }
   }
}
