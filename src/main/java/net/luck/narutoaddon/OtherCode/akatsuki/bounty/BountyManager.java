package net.luck.narutoaddon.OtherCode.akatsuki.bounty;

import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiSavedData;
import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.endgame.PveRank;
import net.luck.narutoaddon.OtherCode.stat.core.PlayerStatData;
import net.luck.narutoaddon.OtherCode.stat.core.StatSavedData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class BountyManager {
   private static long lastBountyGenTime = 0L;
   private static final long BOUNTY_GEN_COOLDOWN_MS = 21600000L;

   private BountyManager() {
   }

   public static void addBounty(AkatsukiSavedData data, UUID targetId, String targetName, int amount, UUID placedBy) {
      BountyEntry existing = getBountyOn(data, targetId);
      if (existing != null) {
         existing.addAmount(amount);
      } else {
         data.addBounty(new BountyEntry(targetId, targetName, amount, placedBy));
      }

      data.markDirty();
   }

   public static BountyEntry claimBounty(AkatsukiSavedData data, UUID targetId, UUID killerId) {
      BountyEntry entry = getBountyOn(data, targetId);
      if (entry == null) {
         return null;
      } else if (entry.isExpired()) {
         data.removeBounty(targetId);
         data.markDirty();
         return null;
      } else {
         data.removeBounty(targetId);
         data.markDirty();
         return entry;
      }
   }

   public static void autoIncreaseBounty(AkatsukiSavedData data, UUID targetId, String targetName, int baseAmount) {
      BountyEntry existing = getBountyOn(data, targetId);
      if (existing != null) {
         existing.addAmount(baseAmount);
      } else {
         data.addBounty(new BountyEntry(targetId, targetName, baseAmount, (UUID)null));
      }

      data.markDirty();
   }

   public static BountyEntry getBountyOn(AkatsukiSavedData data, UUID targetId) {
      for(BountyEntry entry : data.getActiveBounties()) {
         if (entry.getTargetId().equals(targetId)) {
            return entry;
         }
      }

      return null;
   }

   public static void cleanExpired(AkatsukiSavedData data) {
      List<BountyEntry> bounties = data.getActiveBounties();
      List<BountyEntry> expired = new ArrayList();

      for(BountyEntry entry : bounties) {
         if (entry.isExpired()) {
            expired.add(entry);
         }
      }

      if (!expired.isEmpty()) {
         bounties.removeAll(expired);
         data.markDirty();
      }

   }

   public static double getMultiplier(boolean criticalThreat) {
      return criticalThreat ? (double)2.0F : (double)1.0F;
   }

   public static void tickAutoBounties() {
      long now = System.currentTimeMillis();
      if (now - lastBountyGenTime >= 21600000L) {
         lastBountyGenTime = now;
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            World world = server.getWorld(0);
            if (world != null) {
               AkatsukiSavedData akData = AkatsukiSavedData.get(world);
               generateAutoBounties(akData, server, world);
            }
         }
      }
   }

   public static void generateAutoBounties(AkatsukiSavedData akData, MinecraftServer server, World world) {
      cleanExpired(akData);
      EndgameSavedData endgameData = EndgameSavedData.get(world);
      StatSavedData statData = StatSavedData.get(world);
      List<PlayerThreat> threats = new ArrayList();

      for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
         UUID playerId = player.getUniqueID();
         if (!akData.isMember(playerId) && getBountyOn(akData, playerId) == null) {
            PveRank rank = endgameData.getPveRank(playerId);
            int rankScore = (rank.ordinal() + 1) * 100;
            int pveXp = endgameData.getPveXp(playerId);
            int statPoints = 0;
            PlayerStatData psd = statData.get(playerId);
            if (psd != null) {
               statPoints = psd.getSpSpent();
            }

            int threatScore = rankScore + pveXp + statPoints;
            if (threatScore >= 200) {
               threats.add(new PlayerThreat(playerId, player.getName(), rank, threatScore));
            }
         }
      }

      if (!threats.isEmpty()) {
         threats.sort((a, b) -> Integer.compare(b.score, a.score));
         int count = Math.min(5, threats.size());

         for(int i = 0; i < count; ++i) {
            PlayerThreat pt = (PlayerThreat)threats.get(i);
            int bountyAmount = calculateBountyAmount(pt.rank);
            akData.addBounty(new BountyEntry(pt.playerId, pt.playerName, bountyAmount, (UUID)null));
         }

         akData.markDirty();
      }
   }

   private static int calculateBountyAmount(PveRank rank) {
      Random rand = new Random();
      switch (rank) {
         case KAGE:
            return 8000 + rand.nextInt(8001);
         case ANBU:
            return 4000 + rand.nextInt(4001);
         case JONIN:
            return 2000 + rand.nextInt(2001);
         default:
            return 1000 + rand.nextInt(1001);
      }
   }

   private static class PlayerThreat {
      final UUID playerId;
      final String playerName;
      final PveRank rank;
      final int score;

      PlayerThreat(UUID playerId, String playerName, PveRank rank, int score) {
         this.playerId = playerId;
         this.playerName = playerName;
         this.rank = rank;
         this.score = score;
      }
   }
}
