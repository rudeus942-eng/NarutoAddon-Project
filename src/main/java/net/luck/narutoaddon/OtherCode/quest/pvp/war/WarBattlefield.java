
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextComponent.Serializer;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.*;

public class WarBattlefield {
   private static final Map<UUID, double[]> savedPositions = new HashMap();
   private static BlockPos battlefieldCenter = new BlockPos(-2444, 60, -1293);
   private static BlockPos team1Spawn = new BlockPos(-2325, 49, -1276);
   private static BlockPos team2Spawn = new BlockPos(-2563, 71, -1309);
   private static final List<PendingTeleport> teleportQueue = new ArrayList();
   private static final int TELEPORT_TICK_DELAY = 7;
   private static int teleportTickCounter = 0;
   private static boolean teleportingInProgress = false;
   private static int countdownTicks = -1;
   private static String countdownWarId = null;
   private static boolean arenaInUse = false;
   private static String arenaWarId = null;
   private static final int BOUNDS_RADIUS = 50;
   private static final int DEAD_HEIGHT_OFFSET = 30;
   private static final String WAR_DEAD_TAG = "pvpWarDead";
   private static final String WAR_GAMEMODE_TAG = "pvpWarOrigGamemode";

   public static void setBattlefieldCenter(BlockPos center) {
      battlefieldCenter = center;
   }

   public static BlockPos getBattlefieldCenter() {
      return battlefieldCenter;
   }

   public static void setTeam1Spawn(BlockPos pos) {
      team1Spawn = pos;
   }

   public static void setTeam2Spawn(BlockPos pos) {
      team2Spawn = pos;
   }

   public static BlockPos getTeam1Spawn() {
      return team1Spawn;
   }

   public static BlockPos getTeam2Spawn() {
      return team2Spawn;
   }

   public static boolean isArenaInUse() {
      return arenaInUse;
   }

   public static String getArenaWarId() {
      return arenaWarId;
   }

   public static boolean reserveArena(String warId) {
      if (arenaInUse) {
         return false;
      } else {
         arenaInUse = true;
         arenaWarId = warId;
         return true;
      }
   }

   public static void releaseArena() {
      arenaInUse = false;
      arenaWarId = null;
   }

   public static void teleportTeams(List<UUID> team1, List<UUID> team2, World world) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         BlockPos center = battlefieldCenter;
         BlockPos spawn1 = team1Spawn != null ? team1Spawn : new BlockPos(center.getX() - 25, center.getY(), center.getZ());
         BlockPos spawn2 = team2Spawn != null ? team2Spawn : new BlockPos(center.getX() + 25, center.getY(), center.getZ());
         float yaw1 = calculateYaw(spawn1, spawn2);
         float yaw2 = yaw1 + 180.0F;
         System.out.println("[WAR] teleportTeams: team1=" + team1.size() + " -> " + spawn1 + " | team2=" + team2.size() + " -> " + spawn2);
         teleportOneTeam(server, team1, spawn1, yaw1, 0);
         teleportOneTeam(server, team2, spawn2, yaw2, 1);
         teleportQueue.clear();
         teleportingInProgress = false;
         teleportTickCounter = 0;
      }
   }

   private static void teleportOneTeam(MinecraftServer server, List<UUID> team, BlockPos spawn, float yaw, int spreadAxis) {
      int spacing = 0;

      for(UUID uuid : team) {
         EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
         if (player == null) {
            System.out.println("[WAR] WARNING: player UUID " + uuid + " not online — skipping teleport");
         } else {
            savedPositions.put(uuid, new double[]{player.posX, player.posY, player.posZ, (double)player.rotationYaw, (double)player.rotationPitch});
            double spawnX = (double)spawn.getX() + (double)0.5F + (double)(spreadAxis == 0 ? spacing * 2 : 0);
            double spawnY = (double)spawn.getY();
            double spawnZ = (double)spawn.getZ() + (double)0.5F + (double)(spreadAxis == 1 ? spacing * 2 : 0);
            System.out.println("[WAR] Teleporting " + player.getName() + " to " + spawnX + ", " + spawnY + ", " + spawnZ + " yaw=" + yaw);
            if (player.isRiding()) {
               player.dismountRidingEntity();
            }

            if (player.isBeingRidden()) {
               player.removePassengers();
            }

            int chunkX = (int)spawnX >> 4;
            int chunkZ = (int)spawnZ >> 4;
            player.getServerWorld().getChunkProvider().provideChunk(chunkX, chunkZ);
            player.connection.setPlayerLocation(spawnX, spawnY, spawnZ, yaw, player.rotationPitch);
            player.rotationYaw = yaw;
            player.rotationPitch = 0.0F;
            player.setPositionAndUpdate(spawnX, spawnY, spawnZ);
            player.motionX = (double)0.0F;
            player.motionY = (double)0.0F;
            player.motionZ = (double)0.0F;
            player.velocityChanged = true;
            ++spacing;
         }
      }

   }

   private static float calculateYaw(BlockPos from, BlockPos to) {
      double dx = (double)(to.getX() - from.getX());
      double dz = (double)(to.getZ() - from.getZ());
      return (float)(-Math.toDegrees(Math.atan2(dx, dz)));
   }

   public static boolean tickTeleportQueue() {
      teleportingInProgress = false;
      teleportQueue.clear();
      return true;
   }

   public static boolean isTeleportInProgress() {
      return teleportingInProgress;
   }

   public static void startCountdown(String warId) {
      countdownTicks = 0;
      countdownWarId = warId;
   }

   public static boolean tickCountdown(List<UUID> allPlayers) {
      if (countdownTicks < 0) {
         return false;
      } else {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server == null) {
            return false;
         } else {
            if (countdownTicks == 0) {
               sendCountdownToPlayers(server, allPlayers, 3, "yellow");
            } else if (countdownTicks == 20) {
               sendCountdownToPlayers(server, allPlayers, 2, "gold");
            } else if (countdownTicks == 40) {
               sendCountdownToPlayers(server, allPlayers, 1, "red");
            } else if (countdownTicks == 60) {
               sendFightToPlayers(server, allPlayers);
               countdownTicks = -1;
               countdownWarId = null;
               return true;
            }

            ++countdownTicks;
            return false;
         }
      }
   }

   public static boolean isCountdownActive() {
      return countdownTicks >= 0;
   }

   @Nullable
   public static String getCountdownWarId() {
      return countdownWarId;
   }

   public static void cancelCountdown() {
      countdownTicks = -1;
      countdownWarId = null;
      teleportQueue.clear();
      teleportingInProgress = false;
   }

   private static void sendCountdownToPlayers(MinecraftServer server, List<UUID> players, int number, String color) {
      for(UUID uuid : players) {
         EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
         if (player != null && player.connection != null) {
            player.connection.sendPacket(new SPacketTitle(Type.TIMES, (ITextComponent)null, 0, 20, 10));
            String titleJson = "{\"text\":\"" + number + "\",\"color\":\"" + color + "\",\"bold\":true}";
            ITextComponent titleText = Serializer.jsonToComponent(titleJson);
            player.connection.sendPacket(new SPacketTitle(Type.TITLE, titleText));
         }
      }

   }

   private static void sendFightToPlayers(MinecraftServer server, List<UUID> players) {
      for(UUID uuid : players) {
         EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
         if (player != null && player.connection != null) {
            player.connection.sendPacket(new SPacketTitle(Type.TIMES, (ITextComponent)null, 0, 30, 10));
            String titleJson = "{\"text\":\"⚔ FIGHT! ⚔\",\"color\":\"green\",\"bold\":true}";
            ITextComponent titleText = Serializer.jsonToComponent(titleJson);
            player.connection.sendPacket(new SPacketTitle(Type.TITLE, titleText));
         }
      }

   }

   public static void returnPlayers(List<UUID> players, World world) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         for(UUID uuid : players) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
            if (player != null) {
               double[] saved = (double[])savedPositions.remove(uuid);
               if (saved != null) {
                  player.connection.setPlayerLocation(saved[0], saved[1], saved[2], (float)saved[3], (float)saved[4]);
                  player.setPositionAndUpdate(saved[0], saved[1], saved[2]);
               }

               NBTTagCompound persistentData = player.getEntityData();
               if (persistentData.hasKey("pvpWarOrigGamemode")) {
                  int origMode = persistentData.getInteger("pvpWarOrigGamemode");
                  GameType gameType = GameType.getByID(origMode);
                  if (gameType == null) {
                     gameType = GameType.SURVIVAL;
                  }

                  player.setGameType(gameType);
                  persistentData.removeTag("pvpWarOrigGamemode");
               } else if (player.interactionManager.getGameType() == GameType.ADVENTURE) {
                  player.setGameType(GameType.SURVIVAL);
               }

               persistentData.removeTag("pvpWarDead");
               clearWarEffects(player);
            }
         }

      }
   }

   public static void enterDeadState(EntityPlayerMP player) {
      NBTTagCompound persistentData = player.getEntityData();
      persistentData.setInteger("pvpWarOrigGamemode", player.interactionManager.getGameType().getID());
      persistentData.setBoolean("pvpWarDead", true);
      player.connection.setPlayerLocation(player.posX, player.posY + (double)30.0F, player.posZ, player.rotationYaw, player.rotationPitch);
      player.setPositionAndUpdate(player.posX, player.posY + (double)30.0F, player.posZ);
      player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 999999, 254, false, false));
      player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 999999, 254, false, false));
      player.setGameType(GameType.ADVENTURE);
   }

   public static void exitDeadState(EntityPlayerMP player) {
      NBTTagCompound persistentData = player.getEntityData();
      if (persistentData.getBoolean("pvpWarDead")) {
         int origMode = persistentData.getInteger("pvpWarOrigGamemode");
         GameType gameType = GameType.getByID(origMode);
         if (gameType == null) {
            gameType = GameType.SURVIVAL;
         }

         player.setGameType(gameType);
         clearWarEffects(player);
         persistentData.removeTag("pvpWarDead");
         persistentData.removeTag("pvpWarOrigGamemode");
         double[] saved = (double[])savedPositions.get(player.getUniqueID());
         if (saved != null) {
            double cx = (double)battlefieldCenter.getX() + (double)0.5F;
            double cy = (double)battlefieldCenter.getY();
            double cz = (double)battlefieldCenter.getZ() + (double)0.5F;
            player.connection.setPlayerLocation(cx, cy, cz, player.rotationYaw, player.rotationPitch);
            player.setPositionAndUpdate(cx, cy, cz);
         }

      }
   }

   public static void forceCleanup(EntityPlayerMP player) {
      NBTTagCompound persistentData = player.getEntityData();
      if (persistentData.hasKey("pvpWarOrigGamemode")) {
         int origMode = persistentData.getInteger("pvpWarOrigGamemode");
         GameType gameType = GameType.getByID(origMode);
         if (gameType == null || gameType == GameType.ADVENTURE) {
            gameType = GameType.SURVIVAL;
         }

         player.setGameType(gameType);
         persistentData.removeTag("pvpWarOrigGamemode");
      } else {
         GameType current = player.interactionManager.getGameType();
         if (current == GameType.ADVENTURE) {
            player.setGameType(GameType.SURVIVAL);
         }
      }

      persistentData.removeTag("pvpWarDead");
      clearWarEffects(player);
      savedPositions.remove(player.getUniqueID());
   }

   public static boolean isInBounds(EntityPlayerMP player) {
      double dx = player.posX - (double)battlefieldCenter.getX();
      double dz = player.posZ - (double)battlefieldCenter.getZ();
      return dx * dx + dz * dz <= (double)2500.0F;
   }

   public static boolean isDeadState(EntityPlayerMP player) {
      return player.getEntityData().getBoolean("pvpWarDead");
   }

   private static void clearWarEffects(EntityPlayerMP player) {
      player.removePotionEffect(MobEffects.INVISIBILITY);
      player.removePotionEffect(MobEffects.RESISTANCE);
      player.removePotionEffect(MobEffects.SPEED);
      player.removePotionEffect(MobEffects.STRENGTH);
   }

   private static class PendingTeleport {
      final UUID uuid;
      final double x;
      final double y;
      final double z;
      final float yaw;

      PendingTeleport(UUID uuid, double x, double y, double z, float yaw) {
         this.uuid = uuid;
         this.x = x;
         this.y = y;
         this.z = z;
         this.yaw = yaw;
      }
   }
}
