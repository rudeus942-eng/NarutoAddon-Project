package net.luck.narutoaddon.OtherCode.akatsuki.raid;

import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiModInit;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiSavedData;
import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class VillageRaidManager {
   private static VillageRaidManager INSTANCE = new VillageRaidManager();
   private static final Random RANDOM = new Random();
   private VillageRaid activeRaid;
   private int syncCounter = 0;

   private VillageRaidManager() {
   }

   public static VillageRaidManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new VillageRaidManager();
      }

      return INSTANCE;
   }

   public static void reset() {
      INSTANCE = null;
   }

   private static int[] getVillageCenter(String village) {
      switch (village.toLowerCase()) {
         case "leaf":
            return new int[]{-947, -843};
         case "sand":
            return new int[]{-2734, 520};
         case "mist":
            return new int[]{3861, -2155};
         case "stone":
            return new int[]{-2500, -2655};
         case "cloud":
            return new int[]{1912, -3066};
         case "rain":
            return new int[]{-2249, -828};
         default:
            return null;
      }
   }

   public static boolean isValidVillage(String village) {
      return getVillageCenter(village) != null;
   }

   public VillageRaid startRaid(String village, int durationMinutes, EntityPlayerMP leader) {
      if (this.activeRaid == null || this.activeRaid.getStatus() != VillageRaid.RaidStatus.PREPARING && this.activeRaid.getStatus() != VillageRaid.RaidStatus.ACTIVE) {
         int[] center = getVillageCenter(village);
         if (center == null) {
            leader.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown village: " + village));
            return null;
         } else {
            double angle = RANDOM.nextDouble() * (double)2.0F * Math.PI;
            int rallyX = center[0] + (int)((double)300.0F * Math.cos(angle));
            int rallyZ = center[1] + (int)((double)300.0F * Math.sin(angle));
            this.activeRaid = new VillageRaid(village, rallyX, rallyZ, durationMinutes);
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               List<EntityPlayerMP> members = AkatsukiManager.getInstance().getOnlineMembers(server);

               for(EntityPlayerMP member : members) {
                  this.activeRaid.addParticipant(member.getUniqueID());
               }

               String villageCap = village.substring(0, 1).toUpperCase() + village.substring(1);

               for(EntityPlayerMP member : members) {
                  member.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "" + TextFormatting.BOLD + "[RAID] " + TextFormatting.RESET + TextFormatting.RED + "A raid on " + TextFormatting.WHITE + villageCap + " Village" + TextFormatting.RED + " has begun! Rally to (" + rallyX + ", " + rallyZ + "). You have 3 minutes."));
               }

               for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                  if (!AkatsukiManager.getInstance().isAkatsuki(player)) {
                     VillageHelper.Village pVillage = VillageHelper.getVillage(player);
                     if (pVillage.teamName.equalsIgnoreCase(village)) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "" + TextFormatting.BOLD + "!! YOUR VILLAGE IS UNDER ATTACK !! " + TextFormatting.RESET + TextFormatting.GRAY + "The Akatsuki have launched a raid! Defend your village!"));
                     }
                  }
               }

               this.syncToAll();
               this.save();
            }

            return this.activeRaid;
         }
      } else {
         leader.sendMessage(new TextComponentString(TextFormatting.RED + "A raid is already in progress."));
         return null;
      }
   }

   public void cancelRaid() {
      if (this.activeRaid != null) {
         this.activeRaid.setStatus(VillageRaid.RaidStatus.CANCELLED);
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
               player.sendMessage(new TextComponentString(TextFormatting.GRAY + "[RAID] The raid has been cancelled."));
            }

            this.syncToAll();
         }

         this.save();
         this.activeRaid = null;
      }
   }

   public void onPlayerKill(EntityPlayerMP killer, EntityPlayerMP victim) {
      if (this.activeRaid != null && this.activeRaid.getStatus() == VillageRaid.RaidStatus.ACTIVE) {
         if (AkatsukiManager.getInstance().isAkatsuki(killer)) {
            VillageHelper.Village victimVillage = VillageHelper.getVillage(victim);
            if (victimVillage.teamName.equalsIgnoreCase(this.activeRaid.getTargetVillage())) {
               this.activeRaid.incrementKills();
               if (!this.activeRaid.isParticipant(killer.getUniqueID())) {
                  this.activeRaid.addParticipant(killer.getUniqueID());
                  this.activeRaid.markArrived(killer.getUniqueID());
               }

               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               if (server != null) {
                  for(EntityPlayerMP member : AkatsukiManager.getInstance().getOnlineMembers(server)) {
                     member.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[RAID] " + TextFormatting.RED + killer.getName() + TextFormatting.GRAY + " eliminated " + TextFormatting.WHITE + victim.getName() + TextFormatting.GRAY + " (" + this.activeRaid.getKillsAchieved() + "/" + this.activeRaid.getKillsRequired() + ")"));
                  }
               }

               this.checkCompletion();
            }
         }
      }
   }

   private void checkCompletion() {
      if (this.activeRaid != null && this.activeRaid.getStatus() == VillageRaid.RaidStatus.ACTIVE) {
         if (this.activeRaid.getKillsAchieved() >= this.activeRaid.getKillsRequired()) {
            this.completeRaid();
         }

      }
   }

   private void completeRaid() {
      if (this.activeRaid != null) {
         this.activeRaid.setStatus(VillageRaid.RaidStatus.COMPLETED);
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server == null) {
            this.save();
            this.activeRaid = null;
         } else {
            String villageCap = this.activeRaid.getTargetVillage().substring(0, 1).toUpperCase() + this.activeRaid.getTargetVillage().substring(1);

            for(UUID participantId : this.activeRaid.getParticipants().keySet()) {
               boolean arrived = this.activeRaid.hasArrived(participantId);
               int tokens = arrived ? this.activeRaid.getTokenReward() : this.activeRaid.getTokenReward() / 2;
               int rep = arrived ? this.activeRaid.getRepReward() : this.activeRaid.getRepReward() / 2;
               AkatsukiManager.getInstance().addBountyTokens(participantId, tokens);
               AkatsukiManager.getInstance().addReputation(participantId, rep, "Village Raid on " + villageCap);
               EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(participantId);
               if (player != null) {
                  int ryoReward = arrived ? 2000 : 1000;
                  RyoRewardHelper.grantRyo(player, ryoReward);

                  try {
                     EndgameSavedData pvpData = EndgameSavedData.get(player.world);
                     int pvpXp = arrived ? 150 : 75;
                     pvpData.addPveXp(participantId, pvpXp);
                  } catch (Exception var12) {
                  }

                  player.sendMessage(new TextComponentString(TextFormatting.GOLD + "" + TextFormatting.BOLD + "[RAID COMPLETE] " + TextFormatting.RESET + TextFormatting.GOLD + "+" + tokens + " tokens, +" + rep + " rep, +" + ryoReward + " Ryo" + (arrived ? "" : TextFormatting.GRAY + " (reduced - did not rally)")));
               }
            }

            for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
               player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "" + TextFormatting.BOLD + "The Akatsuki have completed their raid on " + villageCap + " Village!"));
            }

            this.syncToAll();
            this.save();
            this.activeRaid = null;
         }
      }
   }

   private void failRaid() {
      if (this.activeRaid != null) {
         this.activeRaid.setStatus(VillageRaid.RaidStatus.FAILED);
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            String villageCap = this.activeRaid.getTargetVillage().substring(0, 1).toUpperCase() + this.activeRaid.getTargetVillage().substring(1);

            for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
               if (AkatsukiManager.getInstance().isAkatsuki(player)) {
                  player.sendMessage(new TextComponentString(TextFormatting.RED + "[RAID] The raid on " + villageCap + " has failed. Time expired."));
               } else {
                  VillageHelper.Village pVillage = VillageHelper.getVillage(player);
                  if (pVillage.teamName.equalsIgnoreCase(this.activeRaid.getTargetVillage())) {
                     player.sendMessage(new TextComponentString(TextFormatting.GREEN + "The Akatsuki raid on your village has been repelled!"));
                  }
               }
            }

            this.syncToAll();
         }

         this.save();
         this.activeRaid = null;
      }
   }

   public void tick() {
      if (this.activeRaid != null) {
         if (this.activeRaid.getStatus() == VillageRaid.RaidStatus.PREPARING || this.activeRaid.getStatus() == VillageRaid.RaidStatus.ACTIVE) {
            long now = System.currentTimeMillis();
            if (this.activeRaid.getStatus() == VillageRaid.RaidStatus.PREPARING && now >= this.activeRaid.getPreparingEndTime()) {
               this.activeRaid.setStatus(VillageRaid.RaidStatus.ACTIVE);
               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               if (server != null) {
                  List<EntityPlayerMP> members = AkatsukiManager.getInstance().getOnlineMembers(server);

                  for(EntityPlayerMP member : members) {
                     if (this.activeRaid.isParticipant(member.getUniqueID())) {
                        double dist = Math.sqrt(Math.pow(member.posX - (double)this.activeRaid.getRallyX(), (double)2.0F) + Math.pow(member.posZ - (double)this.activeRaid.getRallyZ(), (double)2.0F));
                        if (dist <= (double)100.0F) {
                           this.activeRaid.markArrived(member.getUniqueID());
                        }
                     }
                  }

                  String villageCap = this.activeRaid.getTargetVillage().substring(0, 1).toUpperCase() + this.activeRaid.getTargetVillage().substring(1);

                  for(EntityPlayerMP member : members) {
                     member.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "" + TextFormatting.BOLD + "[RAID] " + TextFormatting.RESET + TextFormatting.RED + "The raid on " + villageCap + " is now ACTIVE! " + this.activeRaid.getDurationMinutes() + " minutes to reach " + this.activeRaid.getKillsRequired() + " kills."));
                  }
               }

               this.syncToAll();
               this.save();
            } else if (this.activeRaid.getStatus() == VillageRaid.RaidStatus.ACTIVE && this.activeRaid.isTimedOut()) {
               this.failRaid();
            } else {
               ++this.syncCounter;
               if (this.syncCounter >= 600) {
                  this.syncCounter = 0;
                  this.syncToAll();
               }

            }
         }
      }
   }

   public void syncToAll() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            this.syncToPlayer(player);
         }

      }
   }

   public void syncToPlayer(EntityPlayerMP player) {
      VillageRaidSyncMessage msg;
      if (this.activeRaid != null && (this.activeRaid.getStatus() == VillageRaid.RaidStatus.PREPARING || this.activeRaid.getStatus() == VillageRaid.RaidStatus.ACTIVE)) {
         boolean isMyVillage = false;
         if (!AkatsukiManager.getInstance().isAkatsuki(player)) {
            VillageHelper.Village pVillage = VillageHelper.getVillage(player);
            isMyVillage = pVillage.teamName.equalsIgnoreCase(this.activeRaid.getTargetVillage());
         }

         msg = VillageRaidSyncMessage.active(this.activeRaid.getTargetVillage(), this.activeRaid.getRallyX(), this.activeRaid.getRallyZ(), this.activeRaid.getTimeRemainingMs(), this.activeRaid.getKillsAchieved(), this.activeRaid.getKillsRequired(), this.activeRaid.getStatus().name(), this.activeRaid.getParticipants().size(), isMyVillage);
      } else {
         msg = VillageRaidSyncMessage.inactive();
      }

      AkatsukiModInit.NETWORK.sendTo(msg, player);
   }

   public boolean isRaidActive() {
      return this.activeRaid != null && (this.activeRaid.getStatus() == VillageRaid.RaidStatus.PREPARING || this.activeRaid.getStatus() == VillageRaid.RaidStatus.ACTIVE);
   }

   public VillageRaid getRaid() {
      return this.activeRaid;
   }

   public void save() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         AkatsukiSavedData data = AkatsukiSavedData.get(server.getWorld(0));
         if (this.activeRaid != null) {
            data.setRaidNBT(this.activeRaid.writeToNBT());
         } else {
            data.setRaidNBT(new NBTTagCompound());
         }

         data.markDirty();
      }
   }

   public void loadRaid(AkatsukiSavedData data) {
      NBTTagCompound raidNBT = data.getRaidNBT();
      if (raidNBT != null && raidNBT.hasKey("raidId")) {
         VillageRaid raid = new VillageRaid();
         raid.readFromNBT(raidNBT);
         if (raid.getStatus() != VillageRaid.RaidStatus.PREPARING && raid.getStatus() != VillageRaid.RaidStatus.ACTIVE) {
            this.activeRaid = null;
         } else if (raid.isTimedOut()) {
            this.activeRaid = null;
         } else {
            this.activeRaid = raid;
         }

      } else {
         this.activeRaid = null;
      }
   }
}
