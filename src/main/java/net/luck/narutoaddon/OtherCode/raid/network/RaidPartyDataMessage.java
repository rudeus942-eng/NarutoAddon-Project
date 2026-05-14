
package net.luck.narutoaddon.OtherCode.raid.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.party.RaidParty;
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

public class RaidPartyDataMessage implements IMessage {
   public static final int ACTION_FULL_SYNC = 0;
   public static final int ACTION_MEMBER_JOIN = 1;
   public static final int ACTION_MEMBER_LEAVE = 2;
   public static final int ACTION_READY_UPDATE = 3;
   public static final int ACTION_STATE_CHANGE = 4;
   public static final int ACTION_PARTY_DISBANDED = 5;
   public static final int ACTION_INVITE_RECEIVED = 6;
   public static final int ACTION_INVITE_CANCELLED = 7;
   private int action;
   private UUID partyId;
   private UUID leaderId;
   private String leaderName;
   private List<PartyMemberData> members;
   private int partyState;
   private String targetBossId;
   private int difficulty;
   private String inviterName;

   public RaidPartyDataMessage() {
      this.members = new ArrayList();
   }

   public RaidPartyDataMessage(int action, RaidParty party) {
      this.action = action;
      this.members = new ArrayList();
      if (party != null) {
         this.partyId = party.getPartyId();
         this.leaderId = party.getLeaderId();
         this.leaderName = party.getLeaderName();
         this.partyState = party.getState().ordinal();
         this.targetBossId = party.getTargetBossId() != null ? party.getTargetBossId() : "";
         this.difficulty = party.getDifficulty() != null ? party.getDifficulty().ordinal() : 0;

         for(UUID memberId : party.getMemberIds()) {
            String name = party.getMemberName(memberId);
            boolean ready = party.isReady(memberId);
            this.members.add(new PartyMemberData(memberId, name, ready));
         }
      }

   }

   public static RaidPartyDataMessage createInviteMessage(String inviterName, UUID partyId) {
      RaidPartyDataMessage msg = new RaidPartyDataMessage();
      msg.action = 6;
      msg.inviterName = inviterName;
      msg.partyId = partyId;
      return msg;
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readInt();
      if (this.action != 5 && this.action != 7) {
         if (this.action == 6) {
            this.inviterName = ByteBufUtils.readUTF8String(buf);
            this.partyId = new UUID(buf.readLong(), buf.readLong());
         } else {
            this.partyId = new UUID(buf.readLong(), buf.readLong());
            this.leaderId = new UUID(buf.readLong(), buf.readLong());
            this.leaderName = ByteBufUtils.readUTF8String(buf);
            this.partyState = buf.readInt();
            this.targetBossId = ByteBufUtils.readUTF8String(buf);
            this.difficulty = buf.readInt();
            int memberCount = buf.readInt();
            this.members = new ArrayList();

            for(int i = 0; i < memberCount; ++i) {
               UUID id = new UUID(buf.readLong(), buf.readLong());
               String name = ByteBufUtils.readUTF8String(buf);
               boolean ready = buf.readBoolean();
               this.members.add(new PartyMemberData(id, name, ready));
            }

         }
      }
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.action);
      if (this.action != 5 && this.action != 7) {
         if (this.action == 6) {
            ByteBufUtils.writeUTF8String(buf, this.inviterName != null ? this.inviterName : "");
            buf.writeLong(this.partyId != null ? this.partyId.getMostSignificantBits() : 0L);
            buf.writeLong(this.partyId != null ? this.partyId.getLeastSignificantBits() : 0L);
         } else {
            buf.writeLong(this.partyId != null ? this.partyId.getMostSignificantBits() : 0L);
            buf.writeLong(this.partyId != null ? this.partyId.getLeastSignificantBits() : 0L);
            buf.writeLong(this.leaderId != null ? this.leaderId.getMostSignificantBits() : 0L);
            buf.writeLong(this.leaderId != null ? this.leaderId.getLeastSignificantBits() : 0L);
            ByteBufUtils.writeUTF8String(buf, this.leaderName != null ? this.leaderName : "");
            buf.writeInt(this.partyState);
            ByteBufUtils.writeUTF8String(buf, this.targetBossId != null ? this.targetBossId : "");
            buf.writeInt(this.difficulty);
            buf.writeInt(this.members.size());

            for(PartyMemberData member : this.members) {
               buf.writeLong(member.id.getMostSignificantBits());
               buf.writeLong(member.id.getLeastSignificantBits());
               ByteBufUtils.writeUTF8String(buf, member.name);
               buf.writeBoolean(member.ready);
            }

         }
      }
   }

   public int getAction() {
      return this.action;
   }

   public UUID getPartyId() {
      return this.partyId;
   }

   public UUID getLeaderId() {
      return this.leaderId;
   }

   public String getLeaderName() {
      return this.leaderName;
   }

   public List<PartyMemberData> getMembers() {
      return this.members;
   }

   public int getPartyState() {
      return this.partyState;
   }

   public String getTargetBossId() {
      return this.targetBossId;
   }

   public RaidDifficulty getDifficulty() {
      return RaidDifficulty.values()[this.difficulty];
   }

   public String getInviterName() {
      return this.inviterName;
   }

   public static class PartyMemberData {
      public final UUID id;
      public final String name;
      public final boolean ready;

      public PartyMemberData(UUID id, String name, boolean ready) {
         this.id = id;
         this.name = name;
         this.ready = ready;
      }
   }

   public static class Handler implements IMessageHandler<RaidPartyDataMessage, IMessage> {
      public IMessage onMessage(RaidPartyDataMessage message, MessageContext ctx) {
         if (ctx.side == Side.CLIENT) {
            Minecraft.getMinecraft().addScheduledTask(() -> this.handleClient(message));
         }

         return null;
      }

      @SideOnly(Side.CLIENT)
      private void handleClient(RaidPartyDataMessage message) {
         RaidClientData.handlePartyUpdate(message);
      }
   }
}
