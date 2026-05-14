
package net.luck.narutoaddon.OtherCode.raid.party;

import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class RaidParty {
   public static final int MAX_PARTY_SIZE = 6;
   public static final long INVITE_TIMEOUT = 60000L;
   private UUID partyId;
   private UUID leaderId;
   private Map<UUID, PartyMember> members = new LinkedHashMap();
   private Map<UUID, PendingInvite> pendingInvites = new HashMap();
   private PartyState state;
   private String selectedBossId;
   private RaidDifficulty selectedDifficulty;
   private Map<UUID, RaidDifficulty> difficultyVotes;
   private int currentRaidId;

   public RaidParty(EntityPlayerMP leader) {
      this.state = PartyState.FORMING;
      this.selectedDifficulty = RaidDifficulty.GENIN;
      this.difficultyVotes = new HashMap();
      this.currentRaidId = -1;
      this.partyId = UUID.randomUUID();
      this.leaderId = leader.getUniqueID();
      PartyMember leaderMember = new PartyMember(leader.getUniqueID(), leader.getName());
      leaderMember.ready = true;
      this.members.put(leader.getUniqueID(), leaderMember);
   }

   private RaidParty() {
      this.state = PartyState.FORMING;
      this.selectedDifficulty = RaidDifficulty.GENIN;
      this.difficultyVotes = new HashMap();
      this.currentRaidId = -1;
   }

   public InviteResult invite(EntityPlayerMP inviter, EntityPlayerMP invitee) {
      UUID inviterUUID = inviter.getUniqueID();
      UUID inviteeUUID = invitee.getUniqueID();
      if (!inviterUUID.equals(this.leaderId)) {
         return new InviteResult(false, "Only the party leader can invite players.");
      } else if (this.members.size() >= 6) {
         return new InviteResult(false, "Party is full (max 6 players).");
      } else if (this.members.containsKey(inviteeUUID)) {
         return new InviteResult(false, invitee.getName() + " is already in your party.");
      } else {
         this.pendingInvites.entrySet().removeIf((e) -> ((PendingInvite)e.getValue()).isExpired());
         if (this.pendingInvites.containsKey(inviteeUUID)) {
            return new InviteResult(false, invitee.getName() + " already has a pending invite.");
         } else if (this.state != PartyState.FORMING && this.state != PartyState.READY) {
            return new InviteResult(false, "Cannot invite players while in queue or raid.");
         } else {
            PendingInvite invite = new PendingInvite(inviteeUUID, invitee.getName());
            this.pendingInvites.put(inviteeUUID, invite);
            invitee.sendMessage(new TextComponentString("§e§l[RAID PARTY] §f" + inviter.getName() + " has invited you to their raid party!"));
            invitee.sendMessage(new TextComponentString("§7Check the Party GUI to accept or decline."));
            invitee.sendMessage(new TextComponentString("§7This invite expires in 60 seconds."));
            if (this.state == PartyState.READY) {
               this.state = PartyState.FORMING;
            }

            return new InviteResult(true, "Invited " + invitee.getName() + " to the party.");
         }
      }
   }

   public InviteResult acceptInvite(EntityPlayerMP player) {
      UUID playerUUID = player.getUniqueID();
      PendingInvite invite = (PendingInvite)this.pendingInvites.get(playerUUID);
      if (invite == null) {
         return new InviteResult(false, "You don't have a pending invite to this party.");
      } else if (invite.isExpired()) {
         this.pendingInvites.remove(playerUUID);
         return new InviteResult(false, "The invite has expired.");
      } else if (this.members.size() >= 6) {
         this.pendingInvites.remove(playerUUID);
         return new InviteResult(false, "The party is now full.");
      } else {
         this.pendingInvites.remove(playerUUID);
         PartyMember member = new PartyMember(playerUUID, player.getName());
         this.members.put(playerUUID, member);
         this.broadcastMessage("§a" + player.getName() + " has joined the party!");
         return new InviteResult(true, "You have joined the party!");
      }
   }

   public InviteResult cancelInvite(EntityPlayerMP canceller, UUID inviteeUUID) {
      if (!canceller.getUniqueID().equals(this.leaderId)) {
         return new InviteResult(false, "Only the party leader can cancel invites.");
      } else {
         PendingInvite invite = (PendingInvite)this.pendingInvites.remove(inviteeUUID);
         if (invite == null) {
            return new InviteResult(false, "No pending invite for that player.");
         } else {
            EntityPlayerMP invitee = this.getPlayer(inviteeUUID);
            if (invitee != null) {
               invitee.sendMessage(new TextComponentString("§e§l[RAID PARTY] §eYour party invite from " + canceller.getName() + " was cancelled."));
            }

            return new InviteResult(true, "Invite to " + invite.inviteeName + " cancelled.");
         }
      }
   }

   public boolean hasPendingInvite(UUID playerUUID) {
      PendingInvite invite = (PendingInvite)this.pendingInvites.get(playerUUID);
      if (invite == null) {
         return false;
      } else if (invite.isExpired()) {
         this.pendingInvites.remove(playerUUID);
         return false;
      } else {
         return true;
      }
   }

   public InviteResult declineInvite(EntityPlayerMP player) {
      UUID playerUUID = player.getUniqueID();
      PendingInvite invite = (PendingInvite)this.pendingInvites.remove(playerUUID);
      if (invite == null) {
         return new InviteResult(false, "You don't have a pending invite to this party.");
      } else {
         EntityPlayerMP leader = this.getLeaderPlayer();
         if (leader != null) {
            leader.sendMessage(new TextComponentString("§c" + player.getName() + " declined your party invite."));
         }

         return new InviteResult(true, "You have declined the invite.");
      }
   }

   public InviteResult kick(EntityPlayerMP kicker, UUID targetUUID) {
      if (!kicker.getUniqueID().equals(this.leaderId)) {
         return new InviteResult(false, "Only the party leader can kick players.");
      } else if (targetUUID.equals(this.leaderId)) {
         return new InviteResult(false, "You cannot kick yourself. Use /raid party disband instead.");
      } else {
         PartyMember kicked = (PartyMember)this.members.remove(targetUUID);
         if (kicked == null) {
            return new InviteResult(false, "That player is not in your party.");
         } else {
            EntityPlayerMP kickedPlayer = this.getPlayer(targetUUID);
            if (kickedPlayer != null) {
               kickedPlayer.sendMessage(new TextComponentString("§c§l[RAID PARTY] §fYou have been kicked from the party."));
            }

            this.broadcastMessage("§c" + kicked.name + " has been kicked from the party.");
            this.checkReadyState();
            return new InviteResult(true, "Kicked " + kicked.name + " from the party.");
         }
      }
   }

   public void addMember(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      if (!this.members.containsKey(uuid) && this.members.size() < 6) {
         PartyMember member = new PartyMember(uuid, player.getName());
         member.ready = true;
         this.members.put(uuid, member);
      }

   }

   public boolean removeMemberSilently(UUID uuid) {
      PartyMember removed = (PartyMember)this.members.remove(uuid);
      if (removed != null) {
         this.pendingInvites.remove(uuid);
         this.difficultyVotes.remove(uuid);
         if (uuid.equals(this.leaderId) && !this.members.isEmpty()) {
            this.leaderId = (UUID)this.members.keySet().iterator().next();
         }

         return true;
      } else {
         return false;
      }
   }

   public InviteResult leave(EntityPlayerMP player) {
      UUID playerUUID = player.getUniqueID();
      if (!this.members.containsKey(playerUUID)) {
         return new InviteResult(false, "You are not in this party.");
      } else if (!playerUUID.equals(this.leaderId)) {
         PartyMember left = (PartyMember)this.members.remove(playerUUID);
         this.broadcastMessage("§e" + left.name + " has left the party.");
         this.checkReadyState();
         return new InviteResult(true, "You have left the party.");
      } else {
         if (this.members.size() > 1) {
            UUID newLeaderId = null;

            for(UUID uuid : this.members.keySet()) {
               if (!uuid.equals(this.leaderId)) {
                  newLeaderId = uuid;
                  break;
               }
            }

            if (newLeaderId != null) {
               this.members.remove(this.leaderId);
               this.leaderId = newLeaderId;
               this.broadcastMessage("§e" + player.getName() + " has left. " + ((PartyMember)this.members.get(newLeaderId)).name + " is now the party leader.");
               this.checkReadyState();
               return new InviteResult(true, "You have left the party.");
            }
         }

         this.members.clear();
         this.state = PartyState.COMPLETED;
         return new InviteResult(true, "Party disbanded.");
      }
   }

   public InviteResult promote(EntityPlayerMP promoter, UUID targetUUID) {
      if (!promoter.getUniqueID().equals(this.leaderId)) {
         return new InviteResult(false, "Only the party leader can promote players.");
      } else if (!this.members.containsKey(targetUUID)) {
         return new InviteResult(false, "That player is not in your party.");
      } else if (targetUUID.equals(this.leaderId)) {
         return new InviteResult(false, "You are already the leader.");
      } else {
         this.leaderId = targetUUID;
         this.broadcastMessage("§e" + ((PartyMember)this.members.get(targetUUID)).name + " is now the party leader!");
         return new InviteResult(true, "Promoted " + ((PartyMember)this.members.get(targetUUID)).name + " to leader.");
      }
   }

   public InviteResult setReady(UUID playerUUID, boolean ready) {
      PartyMember member = (PartyMember)this.members.get(playerUUID);
      if (member == null) {
         return new InviteResult(false, "You are not in this party.");
      } else if (this.state != PartyState.QUEUED && this.state != PartyState.IN_RAID) {
         member.ready = ready;
         if (ready) {
            this.broadcastMessage("§a" + member.name + " is ready!");
         } else {
            this.broadcastMessage("§e" + member.name + " is no longer ready.");
         }

         this.checkReadyState();
         return new InviteResult(true, ready ? "You are now ready!" : "You are no longer ready.");
      } else {
         return new InviteResult(false, "Cannot change ready state while queued or in raid.");
      }
   }

   private void checkReadyState() {
      if (this.state != PartyState.QUEUED && this.state != PartyState.IN_RAID) {
         boolean allReady = !this.members.isEmpty();

         for(PartyMember member : this.members.values()) {
            if (!member.ready) {
               allReady = false;
               break;
            }
         }

         this.pendingInvites.entrySet().removeIf((e) -> ((PendingInvite)e.getValue()).isExpired());
         if (allReady && this.pendingInvites.isEmpty()) {
            this.state = PartyState.READY;
            this.broadcastMessage("§a§lAll party members are ready!");
         } else {
            this.state = PartyState.FORMING;
         }

      }
   }

   public boolean isAllReady() {
      for(PartyMember member : this.members.values()) {
         if (!member.ready) {
            return false;
         }
      }

      return !this.members.isEmpty();
   }

   public void voteDifficulty(UUID playerUUID, RaidDifficulty difficulty) {
      if (this.members.containsKey(playerUUID)) {
         this.difficultyVotes.put(playerUUID, difficulty);
         this.updateSelectedDifficulty();
      }

   }

   private void updateSelectedDifficulty() {
      if (!this.difficultyVotes.isEmpty()) {
         Map<RaidDifficulty, Integer> counts = new EnumMap(RaidDifficulty.class);

         for(RaidDifficulty diff : this.difficultyVotes.values()) {
            counts.merge(diff, 1, Integer::sum);
         }

         int maxVotes = 0;
         RaidDifficulty winner = RaidDifficulty.GENIN;

         for(Map.Entry<RaidDifficulty, Integer> entry : counts.entrySet()) {
            if ((Integer)entry.getValue() > maxVotes) {
               maxVotes = (Integer)entry.getValue();
               winner = (RaidDifficulty)entry.getKey();
            }
         }

         this.selectedDifficulty = winner;
      }
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound tag = new NBTTagCompound();
      tag.setString("partyId", this.partyId.toString());
      tag.setString("leaderId", this.leaderId.toString());
      tag.setInteger("state", this.state.ordinal());
      NBTTagList memberList = new NBTTagList();

      for(PartyMember member : this.members.values()) {
         memberList.appendTag(member.writeToNBT());
      }

      tag.setTag("members", memberList);
      if (this.selectedBossId != null) {
         tag.setString("selectedBossId", this.selectedBossId);
      }

      tag.setInteger("selectedDifficulty", this.selectedDifficulty.ordinal());
      tag.setInteger("currentRaidId", this.currentRaidId);
      return tag;
   }

   public static RaidParty readFromNBT(NBTTagCompound tag) {
      RaidParty party = new RaidParty();
      party.partyId = UUID.fromString(tag.getString("partyId"));
      party.leaderId = UUID.fromString(tag.getString("leaderId"));
      party.state = PartyState.values()[tag.getInteger("state")];
      NBTTagList memberList = tag.getTagList("members", 10);

      for(int i = 0; i < memberList.tagCount(); ++i) {
         PartyMember member = PartyMember.readFromNBT(memberList.getCompoundTagAt(i));
         party.members.put(member.uuid, member);
      }

      if (tag.hasKey("selectedBossId")) {
         party.selectedBossId = tag.getString("selectedBossId");
      }

      party.selectedDifficulty = RaidDifficulty.fromOrdinal(tag.getInteger("selectedDifficulty"));
      party.currentRaidId = tag.getInteger("currentRaidId");
      return party;
   }

   public void broadcastMessage(String message) {
      String fullMessage = "§7[Party] " + message;

      for(PartyMember member : this.members.values()) {
         EntityPlayerMP player = this.getPlayer(member.uuid);
         if (player != null) {
            player.sendMessage(new TextComponentString(fullMessage));
         }
      }

   }

   private EntityPlayerMP getPlayer(UUID uuid) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      return server != null ? server.getPlayerList().getPlayerByUUID(uuid) : null;
   }

   private EntityPlayerMP getLeaderPlayer() {
      return this.getPlayer(this.leaderId);
   }

   public UUID getPartyId() {
      return this.partyId;
   }

   public UUID getLeaderId() {
      return this.leaderId;
   }

   public PartyState getState() {
      return this.state;
   }

   public void setState(PartyState state) {
      this.state = state;
   }

   public int getSize() {
      return this.members.size();
   }

   public boolean isFull() {
      return this.members.size() >= 6;
   }

   public String getSelectedBossId() {
      return this.selectedBossId;
   }

   public void setSelectedBossId(String bossId) {
      this.selectedBossId = bossId;
   }

   public RaidDifficulty getSelectedDifficulty() {
      return this.selectedDifficulty;
   }

   public void setSelectedDifficulty(RaidDifficulty difficulty) {
      this.selectedDifficulty = difficulty;
   }

   public int getCurrentRaidId() {
      return this.currentRaidId;
   }

   public void setCurrentRaidId(int raidId) {
      this.currentRaidId = raidId;
   }

   public Set<UUID> getMemberUUIDs() {
      return Collections.unmodifiableSet(this.members.keySet());
   }

   public Collection<PartyMember> getMembers() {
      return Collections.unmodifiableCollection(this.members.values());
   }

   public boolean isMember(UUID uuid) {
      return this.members.containsKey(uuid);
   }

   public boolean isLeader(UUID uuid) {
      return uuid.equals(this.leaderId);
   }

   public PartyMember getMember(UUID uuid) {
      return (PartyMember)this.members.get(uuid);
   }

   public List<EntityPlayerMP> getOnlinePlayers() {
      List<EntityPlayerMP> online = new ArrayList();

      for(UUID uuid : this.members.keySet()) {
         EntityPlayerMP player = this.getPlayer(uuid);
         if (player != null) {
            online.add(player);
         }
      }

      return online;
   }

   public Set<UUID> getMemberIds() {
      return Collections.unmodifiableSet(this.members.keySet());
   }

   public String getMemberName(UUID uuid) {
      PartyMember member = (PartyMember)this.members.get(uuid);
      return member != null ? member.name : "Unknown";
   }

   public boolean isReady(UUID uuid) {
      PartyMember member = (PartyMember)this.members.get(uuid);
      return member != null && member.ready;
   }

   public String getLeaderName() {
      PartyMember leader = (PartyMember)this.members.get(this.leaderId);
      return leader != null ? leader.name : "Unknown";
   }

   public String getTargetBossId() {
      return this.selectedBossId;
   }

   public RaidDifficulty getDifficulty() {
      return this.selectedDifficulty;
   }

   public static enum PartyState {
      FORMING,
      READY,
      QUEUED,
      IN_RAID,
      COMPLETED;
   }

   public static class PartyMember {
      public UUID uuid;
      public String name;
      public boolean ready;
      public long joinTime;

      public PartyMember(UUID uuid, String name) {
         this.uuid = uuid;
         this.name = name;
         this.ready = false;
         this.joinTime = System.currentTimeMillis();
      }

      public NBTTagCompound writeToNBT() {
         NBTTagCompound tag = new NBTTagCompound();
         tag.setString("uuid", this.uuid.toString());
         tag.setString("name", this.name);
         tag.setBoolean("ready", this.ready);
         tag.setLong("joinTime", this.joinTime);
         return tag;
      }

      public static PartyMember readFromNBT(NBTTagCompound tag) {
         UUID uuid = UUID.fromString(tag.getString("uuid"));
         String name = tag.getString("name");
         PartyMember member = new PartyMember(uuid, name);
         member.ready = tag.getBoolean("ready");
         member.joinTime = tag.getLong("joinTime");
         return member;
      }
   }

   public static class PendingInvite {
      public UUID inviteeUUID;
      public String inviteeName;
      public long inviteTime;

      public PendingInvite(UUID uuid, String name) {
         this.inviteeUUID = uuid;
         this.inviteeName = name;
         this.inviteTime = System.currentTimeMillis();
      }

      public boolean isExpired() {
         return System.currentTimeMillis() - this.inviteTime > 60000L;
      }
   }

   public static class InviteResult {
      public final boolean success;
      public final String message;

      public InviteResult(boolean success, String message) {
         this.success = success;
         this.message = message;
      }
   }
}
