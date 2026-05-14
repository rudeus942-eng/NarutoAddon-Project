package net.luck.narutoaddon.OtherCode.akatsuki.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.akatsuki.bounty.BountyManager;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiSavedData;
import net.luck.narutoaddon.OtherCode.akatsuki.mission.LeaderMission;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Random;
import java.util.UUID;

public class LeaderActionMessage implements IMessage {
   public static final int START_RAID = 0;
   public static final int CANCEL_RAID = 1;
   public static final int ASSIGN_PARTNER = 2;
   public static final int CREATE_MISSION = 3;
   public static final int REMOVE_MISSION = 4;
   public static final int ADD_BOUNTY = 5;
   private int actionType;
   private String param1;
   private String param2;
   private int intParam1;
   private int intParam2;

   public LeaderActionMessage() {
      this.param1 = "";
      this.param2 = "";
   }

   public LeaderActionMessage(int actionType, String param1, String param2) {
      this.actionType = actionType;
      this.param1 = param1 != null ? param1 : "";
      this.param2 = param2 != null ? param2 : "";
   }

   public LeaderActionMessage(int actionType, String param1, String param2, int intParam1, int intParam2) {
      this.actionType = actionType;
      this.param1 = param1 != null ? param1 : "";
      this.param2 = param2 != null ? param2 : "";
      this.intParam1 = intParam1;
      this.intParam2 = intParam2;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.actionType);
      ByteBufUtils.writeUTF8String(buf, this.param1);
      ByteBufUtils.writeUTF8String(buf, this.param2);
      buf.writeInt(this.intParam1);
      buf.writeInt(this.intParam2);
   }

   public void fromBytes(ByteBuf buf) {
      this.actionType = buf.readInt();
      this.param1 = ByteBufUtils.readUTF8String(buf);
      this.param2 = ByteBufUtils.readUTF8String(buf);
      this.intParam1 = buf.readInt();
      this.intParam2 = buf.readInt();
   }

   public static class Handler implements IMessageHandler<LeaderActionMessage, IMessage> {
      public IMessage onMessage(LeaderActionMessage msg, MessageContext ctx) {
         EntityPlayerMP sender = ctx.getServerHandler().player;
         FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
            if (!sender.canUseCommand(2, "")) {
               sender.sendMessage(new TextComponentString(TextFormatting.RED + "You do not have permission to use the Leader Panel."));
            } else if (!AkatsukiManager.getInstance().isAkatsuki(sender.getUniqueID())) {
               sender.sendMessage(new TextComponentString(TextFormatting.RED + "You must be an Akatsuki member."));
            } else {
               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               if (server != null) {
                  switch (msg.actionType) {
                     case 0:
                        this.handleStartRaid(sender, server, msg.param1);
                        break;
                     case 1:
                        this.handleCancelRaid(sender, server);
                        break;
                     case 2:
                        this.handleAssignPartner(sender, server, msg.param1, msg.param2);
                        break;
                     case 3:
                        this.handleCreateMission(sender, server, msg.param1, msg.param2, msg.intParam1, msg.intParam2);
                        break;
                     case 4:
                        this.handleRemoveMission(sender, server, msg.param1);
                        break;
                     case 5:
                        this.handleAddBounty(sender, server, msg.param1, msg.intParam1);
                        break;
                     default:
                        sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown leader action."));
                  }

               }
            }
         });
         return null;
      }

      private void handleStartRaid(EntityPlayerMP sender, MinecraftServer server, String villageName) {
         if (villageName != null && !villageName.isEmpty()) {
            try {
               Class<?> vrmClass = Class.forName("net.luck.narutoaddon.OtherCode.akatsuki.raid.VillageRaidManager");
               Object instance = vrmClass.getMethod("getInstance").invoke((Object)null);
               vrmClass.getMethod("startRaid", String.class, Integer.TYPE, EntityPlayerMP.class).invoke(instance, villageName, 30, sender);
            } catch (Exception var6) {
               sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "[Leader] Raid system not yet initialized. Attempted raid on: " + villageName));
            }

         } else {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "No village specified for raid."));
         }
      }

      private void handleCancelRaid(EntityPlayerMP sender, MinecraftServer server) {
         try {
            Class<?> vrmClass = Class.forName("net.luck.narutoaddon.OtherCode.akatsuki.raid.VillageRaidManager");
            Object instance = vrmClass.getMethod("getInstance").invoke((Object)null);
            vrmClass.getMethod("cancelRaid").invoke(instance);
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "[Leader] Raid cancelled."));
         } catch (Exception var5) {
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "[Leader] Raid system not yet initialized."));
         }

      }

      private void handleAssignPartner(EntityPlayerMP sender, MinecraftServer server, String name1, String name2) {
         if (name1 != null && !name1.isEmpty() && name2 != null && !name2.isEmpty()) {
            if (name1.equals(name2)) {
               sender.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot partner a player with themselves."));
            } else {
               EntityPlayerMP p1 = server.getPlayerList().getPlayerByUsername(name1);
               EntityPlayerMP p2 = server.getPlayerList().getPlayerByUsername(name2);
               if (p1 != null && p2 != null) {
                  AkatsukiManager mgr = AkatsukiManager.getInstance();
                  if (mgr.isAkatsuki(p1.getUniqueID()) && mgr.isAkatsuki(p2.getUniqueID())) {
                     mgr.setPartner(p1.getUniqueID(), p2.getUniqueID());
                     sender.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Leader] " + TextFormatting.GRAY + "Partnered " + name1 + " with " + name2 + "."));
                     p1.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "You have been partnered with " + name2 + "."));
                     p2.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "You have been partnered with " + name1 + "."));
                  } else {
                     sender.sendMessage(new TextComponentString(TextFormatting.RED + "Both players must be Akatsuki members."));
                  }
               } else {
                  sender.sendMessage(new TextComponentString(TextFormatting.RED + "Both players must be online."));
               }
            }
         } else {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Two player names required for partner assignment."));
         }
      }

      private void handleCreateMission(EntityPlayerMP sender, MinecraftServer server, String missionType, String targetName, int tokenReward, int repReward) {
         if (targetName != null && !targetName.isEmpty()) {
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(targetName);
            if (target == null) {
               sender.sendMessage(new TextComponentString(TextFormatting.RED + targetName + " is not online."));
            } else {
               AkatsukiSavedData data = AkatsukiSavedData.get(server.getWorld(0));
               if (data != null) {
                  if (data.getLeaderMission(target.getUniqueID()) != null) {
                     sender.sendMessage(new TextComponentString(TextFormatting.RED + targetName + " already has an active leader mission."));
                  } else {
                     String type = missionType != null ? missionType.toLowerCase() : "assassination";
                     Random rand = new Random();
                     double angle = rand.nextDouble() * (double)2.0F * Math.PI;
                     int distance = 1000 + rand.nextInt(1001);
                     int tx = (int)(target.posX + Math.cos(angle) * (double)distance);
                     int tz = (int)(target.posZ + Math.sin(angle) * (double)distance);
                     tx = Math.max(-5500, Math.min(5500, tx));
                     tz = Math.max(-5500, Math.min(5500, tz));
                     int ty = server.getWorld(0).getHeight(tx, tz);
                     if (ty < 1) {
                        ty = 64;
                     }

                     BlockPos targetPos = new BlockPos(tx, ty, tz);
                     LeaderMission mission = new LeaderMission(type, this.capitalize(type) + " mission assigned by " + sender.getName(), tokenReward, repReward, target.getUniqueID(), targetPos);
                     data.setLeaderMission(target.getUniqueID(), mission);
                     sender.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Leader] " + TextFormatting.GRAY + "Assigned " + type + " mission to " + targetName + " (" + tokenReward + "T, " + repReward + " rep) at (" + tx + ", " + tz + ")"));
                     target.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "You've been assigned a " + type + " mission by Pain. Check your Assignments tab for details."));
                     QuestManager.getInstance().getWaypointManager().setWaypoint(target, "leader_mission", new WaypointData(targetPos, WaypointData.WaypointType.COMBAT, this.capitalize(type) + " Mission"));
                     AkatsukiManager.getInstance().syncToClient(target);
                     AkatsukiManager.getInstance().syncToClient(sender);
                  }
               }
            }
         } else {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Select a member to assign the mission to."));
         }
      }

      private void handleRemoveMission(EntityPlayerMP sender, MinecraftServer server, String targetName) {
         if (targetName != null && !targetName.isEmpty()) {
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(targetName);
            if (target == null) {
               sender.sendMessage(new TextComponentString(TextFormatting.RED + targetName + " is not online."));
            } else {
               AkatsukiSavedData data = AkatsukiSavedData.get(server.getWorld(0));
               if (data != null) {
                  if (data.getLeaderMission(target.getUniqueID()) == null) {
                     sender.sendMessage(new TextComponentString(TextFormatting.RED + targetName + " has no active leader mission."));
                  } else {
                     data.removeLeaderMission(target.getUniqueID());
                     QuestManager.getInstance().getWaypointManager().clearWaypoint(target, "leader_mission");
                     sender.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Leader] " + TextFormatting.GRAY + "Removed mission from " + targetName + "."));
                     AkatsukiManager.getInstance().syncToClient(target);
                     AkatsukiManager.getInstance().syncToClient(sender);
                  }
               }
            }
         } else {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Select a member to remove the mission from."));
         }
      }

      private void handleAddBounty(EntityPlayerMP sender, MinecraftServer server, String targetName, int amount) {
         if (targetName != null && !targetName.isEmpty()) {
            if (amount > 0 && amount <= 10000) {
               EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(targetName);
               if (target == null) {
                  sender.sendMessage(new TextComponentString(TextFormatting.RED + targetName + " is not online."));
               } else {
                  AkatsukiSavedData data = AkatsukiSavedData.get(server.getWorld(0));
                  if (data != null) {
                     BountyManager.addBounty(data, target.getUniqueID(), targetName, amount, (UUID)null);
                     sender.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Leader] " + TextFormatting.GRAY + "Added " + amount + " ryo bounty on " + targetName + "."));
                     AkatsukiManager.getInstance().syncToAllOnline(server);
                  }
               }
            } else {
               sender.sendMessage(new TextComponentString(TextFormatting.RED + "Bounty amount must be 1-10000."));
            }
         } else {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Select a member to place a bounty on."));
         }
      }

      private String capitalize(String s) {
         return s != null && !s.isEmpty() ? s.substring(0, 1).toUpperCase() + s.substring(1) : s;
      }
   }
}
