package net.luck.narutoaddon.OtherCode.akatsuki.core;

import net.luck.narutoaddon.OtherCode.akatsuki.bounty.BountyEntry;
import net.luck.narutoaddon.OtherCode.akatsuki.mission.LeaderMission;
import net.luck.narutoaddon.OtherCode.akatsuki.network.AkatsukiSyncMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class AkatsukiManager {
   private static AkatsukiManager INSTANCE = new AkatsukiManager();
   private static final String TEAM_NAME = "akatsuki";
   private static final int TOTAL_ZONE_COUNT = 36;

   private AkatsukiManager() {
   }

   public static AkatsukiManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new AkatsukiManager();
      }

      return INSTANCE;
   }

   public static void reset() {
      INSTANCE = null;
   }

   private AkatsukiSavedData getSavedData() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server == null) {
         return null;
      } else {
         World world = server.getWorld(0);
         return AkatsukiSavedData.get(world);
      }
   }

   private Scoreboard getScoreboard() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      return server == null ? null : server.getWorld(0).getScoreboard();
   }

   private void ensureTeamExists() {
      Scoreboard scoreboard = this.getScoreboard();
      if (scoreboard != null) {
         ScorePlayerTeam team = scoreboard.getTeam("akatsuki");
         if (team == null) {
            team = scoreboard.createTeam("akatsuki");
            team.setPrefix(TextFormatting.DARK_RED + "[�?] ");
            team.setColor(TextFormatting.DARK_RED);
         }

      }
   }

   public void invitePlayer(UUID inviter, UUID target) {
      AkatsukiSavedData data = this.getSavedData();
      if (data != null) {
         if (!data.isMember(target)) {
            data.addPendingInvite(target);
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               EntityPlayerMP targetPlayer = server.getPlayerList().getPlayerByUUID(target);
               if (targetPlayer != null) {
                  this.syncToClient(targetPlayer);
                  EntityPlayerMP inviterPlayer = server.getPlayerList().getPlayerByUUID(inviter);
                  String inviterName = inviterPlayer != null ? inviterPlayer.getName() : "The Akatsuki";
                  targetPlayer.sendMessage(new TextComponentString(TextFormatting.DARK_RED + inviterName + " has invited you to join the Akatsuki. " + TextFormatting.GRAY + "Open the Faction War tab to respond."));
               }

            }
         }
      }
   }

   public void acceptInvite(EntityPlayerMP player) {
      AkatsukiSavedData data = this.getSavedData();
      if (data != null) {
         UUID playerId = player.getUniqueID();
         if (data.isPendingInvite(playerId)) {
            if (!data.isMember(playerId)) {
               data.removePendingInvite(playerId);
               AkatsukiRing ring = this.findAvailableRing(data);
               boolean isFirstMember = data.getAllMembers().isEmpty();
               AkatsukiMember member = new AkatsukiMember(playerId, ring);
               if (isFirstMember) {
                  member.setLeader(true);
               }

               data.addMember(playerId, member);
               this.ensureTeamExists();
               Scoreboard scoreboard = player.getWorldScoreboard();
               ScorePlayerTeam oldTeam = scoreboard.getPlayersTeam(player.getName());
               if (oldTeam != null) {
                  scoreboard.removePlayerFromTeam(player.getName(), oldTeam);
               }

               scoreboard.addPlayerToTeam(player.getName(), "akatsuki");
               player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "You have joined the Akatsuki. Your ring: " + ring.kanji + " (" + ring.romajiName + ")"));
               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               if (server != null) {
                  this.syncToAllOnline(server);
               }

            }
         }
      }
   }

   public void declineInvite(EntityPlayerMP player) {
      AkatsukiSavedData data = this.getSavedData();
      if (data != null) {
         data.removePendingInvite(player.getUniqueID());
         player.sendMessage(new TextComponentString(TextFormatting.GRAY + "You have declined the Akatsuki invitation."));
         this.syncToClient(player);
      }
   }

   private AkatsukiRing findAvailableRing(AkatsukiSavedData data) {
      Set<AkatsukiRing> taken = new HashSet();

      for(AkatsukiMember m : data.getAllMembers().values()) {
         taken.add(m.getRing());
      }

      for(AkatsukiRing ring : AkatsukiRing.values()) {
         if (!taken.contains(ring)) {
            return ring;
         }
      }

      return AkatsukiRing.REI;
   }

   public void kickMember(UUID targetId) {
      AkatsukiSavedData data = this.getSavedData();
      if (data != null) {
         AkatsukiMember member = data.getMember(targetId);
         if (member != null) {
            boolean wasLeader = member.isLeader();
            if (member.getPartnerId() != null) {
               AkatsukiMember partner = data.getMember(member.getPartnerId());
               if (partner != null) {
                  partner.setPartnerId((UUID)null);
               }
            }

            data.removeMember(targetId);
            if (wasLeader && !data.getAllMembers().isEmpty()) {
               AkatsukiMember bestCandidate = null;

               for(AkatsukiMember m : data.getAllMembers().values()) {
                  if (bestCandidate == null || m.getReputation() > bestCandidate.getReputation()) {
                     bestCandidate = m;
                  }
               }

               if (bestCandidate != null) {
                  bestCandidate.setLeader(true);
                  data.markDirty();
               }
            }

            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(targetId);
               if (player != null) {
                  Scoreboard scoreboard = player.getWorldScoreboard();
                  scoreboard.removePlayerFromTeam(player.getName(), scoreboard.getTeam("akatsuki"));
                  player.sendMessage(new TextComponentString(TextFormatting.RED + "You have been removed from the Akatsuki."));
                  this.syncToClient(player);
               }

               this.syncToAllOnline(server);
            }

         }
      }
   }

   public boolean isAkatsuki(EntityPlayerMP player) {
      return player.getTeam() == null ? false : "akatsuki".equalsIgnoreCase(player.getTeam().getName());
   }

   public boolean isAkatsuki(UUID playerId) {
      AkatsukiSavedData data = this.getSavedData();
      return data != null && data.isMember(playerId);
   }

   public boolean isPendingInvite(UUID playerId) {
      AkatsukiSavedData data = this.getSavedData();
      return data != null && data.isPendingInvite(playerId);
   }

   public AkatsukiMember getMember(UUID playerId) {
      AkatsukiSavedData data = this.getSavedData();
      return data != null ? data.getMember(playerId) : null;
   }

   public Map<UUID, AkatsukiMember> getAllMembers() {
      AkatsukiSavedData data = this.getSavedData();
      return data != null ? data.getAllMembers() : Collections.emptyMap();
   }

   public List<EntityPlayerMP> getOnlineMembers(MinecraftServer server) {
      List<EntityPlayerMP> online = new ArrayList();
      AkatsukiSavedData data = this.getSavedData();
      if (data == null) {
         return online;
      } else {
         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            if (data.isMember(player.getUniqueID())) {
               online.add(player);
            }
         }

         return online;
      }
   }

   public void addReputation(UUID playerId, int amount, String reason) {
      AkatsukiSavedData data = this.getSavedData();
      if (data != null) {
         AkatsukiMember member = data.getMember(playerId);
         if (member != null) {
            member.addReputation(amount);
            data.markDirty();
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
               if (player != null) {
                  String sign = amount >= 0 ? "+" : "";
                  player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + sign + amount + " reputation (" + reason + ")"));
                  this.syncToClient(player);
               }
            }

         }
      }
   }

   public void addBountyTokens(UUID playerId, int amount) {
      AkatsukiSavedData data = this.getSavedData();
      if (data != null) {
         AkatsukiMember member = data.getMember(playerId);
         if (member != null) {
            member.addBountyTokens(amount);
            data.markDirty();
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
               if (player != null) {
                  this.syncToClient(player);
               }
            }

         }
      }
   }

   public UUID getLeader() {
      AkatsukiSavedData data = this.getSavedData();
      if (data == null) {
         return null;
      } else {
         for(Map.Entry<UUID, AkatsukiMember> entry : data.getAllMembers().entrySet()) {
            if (((AkatsukiMember)entry.getValue()).isLeader()) {
               return (UUID)entry.getKey();
            }
         }

         return null;
      }
   }

   public void setLeader(UUID newLeaderId) {
      AkatsukiSavedData data = this.getSavedData();
      if (data != null) {
         AkatsukiMember newLeader = data.getMember(newLeaderId);
         if (newLeader != null) {
            for(AkatsukiMember m : data.getAllMembers().values()) {
               if (m.isLeader()) {
                  m.setLeader(false);
               }
            }

            newLeader.setLeader(true);
            data.markDirty();
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               EntityPlayerMP leaderPlayer = server.getPlayerList().getPlayerByUUID(newLeaderId);
               if (leaderPlayer != null) {
                  leaderPlayer.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GOLD + "You are now the Leader of the Akatsuki."));
               }

               this.syncToAllOnline(server);
            }

         }
      }
   }

   public boolean isLeader(UUID playerId) {
      AkatsukiSavedData data = this.getSavedData();
      if (data == null) {
         return false;
      } else {
         AkatsukiMember member = data.getMember(playerId);
         return member != null && member.isLeader();
      }
   }

   public void setPartner(UUID playerId1, UUID playerId2) {
      AkatsukiSavedData data = this.getSavedData();
      if (data != null) {
         AkatsukiMember member1 = data.getMember(playerId1);
         AkatsukiMember member2 = data.getMember(playerId2);
         if (member1 != null && member2 != null) {
            this.clearPartner(data, member1);
            this.clearPartner(data, member2);
            member1.setPartnerId(playerId2);
            member2.setPartnerId(playerId1);
            data.markDirty();
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               EntityPlayerMP p1 = server.getPlayerList().getPlayerByUUID(playerId1);
               EntityPlayerMP p2 = server.getPlayerList().getPlayerByUUID(playerId2);
               if (p1 != null) {
                  this.syncToClient(p1);
               }

               if (p2 != null) {
                  this.syncToClient(p2);
               }
            }

         }
      }
   }

   private void clearPartner(AkatsukiSavedData data, AkatsukiMember member) {
      if (member.getPartnerId() != null) {
         AkatsukiMember oldPartner = data.getMember(member.getPartnerId());
         if (oldPartner != null) {
            oldPartner.setPartnerId((UUID)null);
         }

         member.setPartnerId((UUID)null);
      }

   }

   public int getAkatsukiZoneCount() {
      return 0;
   }

   public boolean isCriticalThreat() {
      return false;
   }

   public void syncToClient(EntityPlayerMP player) {
      AkatsukiSavedData data = this.getSavedData();
      if (data != null) {
         UUID playerId = player.getUniqueID();
         boolean isMember = data.isMember(playerId);
         boolean hasPending = data.isPendingInvite(playerId);
         boolean exists = !data.getAllMembers().isEmpty();
         boolean critical = this.isCriticalThreat();
         int zoneCount = this.getAkatsukiZoneCount();
         float threatPercent = (float)zoneCount / 36.0F;
         List<AkatsukiSyncMessage.BountyData> bountyData = new ArrayList();

         for(BountyEntry b : data.getActiveBounties()) {
            if (!b.isExpired()) {
               bountyData.add(new AkatsukiSyncMessage.BountyData(b.getTargetName(), b.getBountyAmount(), "Unknown"));
            }
         }

         List<String> activityLog = new ArrayList();
         AkatsukiSyncMessage msg;
         if (isMember) {
            AkatsukiMember member = data.getMember(playerId);
            List<AkatsukiSyncMessage.RosterData> roster = new ArrayList();
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

            for(Map.Entry<UUID, AkatsukiMember> entry : data.getAllMembers().entrySet()) {
               AkatsukiMember m = (AkatsukiMember)entry.getValue();
               String name = "Unknown";
               boolean online = false;
               if (server != null) {
                  EntityPlayerMP p = server.getPlayerList().getPlayerByUUID((UUID)entry.getKey());
                  if (p != null) {
                     name = p.getName();
                     online = true;
                  } else {
                     name = ((UUID)entry.getKey()).toString().substring(0, 8);
                  }
               }

               roster.add(new AkatsukiSyncMessage.RosterData(name, m.getRing().kanji, AkatsukiRank.fromReputation(m.getReputation()).displayName, online));
            }

            String partnerName = "";
            if (member.getPartnerId() != null && server != null) {
               EntityPlayerMP partner = server.getPlayerList().getPlayerByUUID(member.getPartnerId());
               if (partner != null) {
                  partnerName = partner.getName();
               }
            }

            msg = AkatsukiSyncMessage.forAkatsuki(member.getRank().ordinal(), member.getRing().ordinal(), member.getReputation(), member.getBountyTokens(), member.getRingUpgradeLevels(), partnerName, critical, zoneCount, 36, hasPending, roster, bountyData, activityLog, threatPercent);
            LeaderMission leaderMission = data.getLeaderMission(playerId);
            if (leaderMission != null) {
               msg.setLeaderMission(leaderMission.getType(), leaderMission.getDescription(), leaderMission.getObjectiveText(), leaderMission.getTokenReward(), leaderMission.getRepReward(), leaderMission.isCompleted(), leaderMission.getTargetPos().getX(), leaderMission.getTargetPos().getZ());
            }
         } else {
            int allianceScore = data.getAllianceScore(playerId);
            msg = AkatsukiSyncMessage.forVillage(exists, hasPending, critical, zoneCount, 36, bountyData, activityLog, allianceScore, threatPercent);
         }

         AkatsukiModInit.NETWORK.sendTo(msg, player);
      }
   }

   public void syncToAllOnline(MinecraftServer server) {
      for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
         this.syncToClient(player);
      }

   }

   public void onPlayerLogin(EntityPlayerMP player) {
      this.syncToClient(player);
   }
}
