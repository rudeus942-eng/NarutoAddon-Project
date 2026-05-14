
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PlayerLocationTracker {
   private static PlayerLocationTracker INSTANCE;
   private final Map<UUID, BlockPos> playerPositions = new HashMap();
   private final Map<UUID, BlockPos> fuzzyOffsets = new HashMap();
   private final Random random = new Random();
   private static final int FUZZY_RANGE = 150;
   private static final int UPDATE_INTERVAL_TICKS = 1200;
   private int tickCounter = 0;

   private PlayerLocationTracker() {
   }

   public static PlayerLocationTracker getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new PlayerLocationTracker();
      }

      return INSTANCE;
   }

   public void tick(World world) {
      ++this.tickCounter;
      if (this.tickCounter >= 1200) {
         this.tickCounter = 0;
         this.updatePositions(world);
      }

   }

   public void updatePositions(World world) {
      MinecraftServer server = world.getMinecraftServer();
      if (server != null) {
         this.playerPositions.clear();
         this.fuzzyOffsets.clear();

         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUniqueID();
            this.playerPositions.put(uuid, player.getPosition());
            this.fuzzyOffsets.put(uuid, new BlockPos(this.randomOffset(), 0, this.randomOffset()));
         }

      }
   }

   private int randomOffset() {
      return this.random.nextInt(301) - 150;
   }

   @Nullable
   public BlockPos getFuzzyPosition(UUID targetUUID) {
      BlockPos actual = (BlockPos)this.playerPositions.get(targetUUID);
      if (actual == null) {
         return null;
      } else {
         BlockPos offset = (BlockPos)this.fuzzyOffsets.get(targetUUID);
         return offset == null ? actual : actual.add(offset);
      }
   }

   @Nullable
   public BlockPos getActualPosition(UUID targetUUID) {
      return (BlockPos)this.playerPositions.get(targetUUID);
   }

   public static String getCardinalDirection(BlockPos from, BlockPos to) {
      int dx = to.getX() - from.getX();
      int dz = to.getZ() - from.getZ();
      double angle = Math.toDegrees(Math.atan2((double)dx, (double)(-dz)));
      if (angle < (double)0.0F) {
         angle += (double)360.0F;
      }

      if (!(angle < (double)22.5F) && !(angle >= (double)337.5F)) {
         if (angle < (double)67.5F) {
            return "Northeast";
         } else if (angle < (double)112.5F) {
            return "East";
         } else if (angle < (double)157.5F) {
            return "Southeast";
         } else if (angle < (double)202.5F) {
            return "South";
         } else if (angle < (double)247.5F) {
            return "Southwest";
         } else {
            return angle < (double)292.5F ? "West" : "Northwest";
         }
      } else {
         return "North";
      }
   }

   public static String getApproximateDistance(BlockPos from, BlockPos to) {
      double dx = (double)(to.getX() - from.getX());
      double dz = (double)(to.getZ() - from.getZ());
      double dist = Math.sqrt(dx * dx + dz * dz);
      int rounded = (int)dist / 50 * 50;
      if (rounded < 50) {
         rounded = 50;
      }

      return "~" + rounded + " blocks";
   }

   public static int getBeaconColor(TrackingType type) {
      switch (type) {
         case HUNT:
            return -52429;
         case BINGO:
            return -30720;
         case MUTUAL:
            return -5622785;
         default:
            return -1;
      }
   }

   public boolean isTracked(UUID targetUUID) {
      return this.playerPositions.containsKey(targetUUID);
   }

   public void removePlayer(UUID playerUUID) {
      this.playerPositions.remove(playerUUID);
      this.fuzzyOffsets.remove(playerUUID);
   }

   public static enum TrackingType {
      HUNT,
      BINGO,
      MUTUAL;
   }
}
