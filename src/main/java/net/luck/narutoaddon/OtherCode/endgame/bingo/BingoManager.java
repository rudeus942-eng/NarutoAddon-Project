
package net.luck.narutoaddon.OtherCode.endgame.bingo;

import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.endgame.PveRank;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameNetworkHelper;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostDifficultyTier;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.quest.core.QuestModInit;
import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.quest.network.QuestCombatHealthMessage;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointData;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointSpawnLogic;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BingoManager {
   private static BingoManager instance;
   private final Map<UUID, BingoBoard> boards = new ConcurrentHashMap();
   private final Map<UUID, BingoInstance> activeHunts = new ConcurrentHashMap();
   private int tickCounter = 0;
   private static final long DAY_MS = 86400000L;
   private static final long RESET_ANCHOR_MS;

   private BingoManager() {
   }

   public static BingoManager getInstance() {
      if (instance == null) {
         instance = new BingoManager();
      }

      return instance;
   }

   public static void reset() {
      instance = null;
   }

   public void onServerTick(World world) {
      ++this.tickCounter;
      if (this.tickCounter % 20 == 0) {
         for(Map.Entry<UUID, BingoInstance> entry : new ArrayList(this.activeHunts.entrySet())) {
            UUID playerUUID = (UUID)entry.getKey();
            BingoInstance hunt = (BingoInstance)entry.getValue();
            EntityPlayerMP player = this.findPlayer(world, playerUUID);
            if (player != null) {
               hunt.tick(world, player);
               if (hunt.isComplete()) {
                  this.completeHunt(world, player, hunt);
               } else if (hunt.isFailed()) {
                  this.failHunt(world, player, hunt);
               }
            }
         }

         if (this.tickCounter % 1200 == 0) {
            this.checkDailyResets(world);
         }

      }
   }

   public BingoBoard getOrGenerateBoard(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      if (this.isDailyResetDue(uuid, player.world)) {
         this.boards.remove(uuid);
      }

      BingoBoard board = (BingoBoard)this.boards.get(uuid);
      if (board == null) {
         board = this.generateBoard(player);
         this.boards.put(uuid, board);
      }

      return board;
   }

   public BingoBoard generateBoard(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      OutpostDifficultyTier tier = this.getPlayerTier(player);
      BingoTargetRegistry.init();
      Random rand = new Random();
      List<BingoTarget> targets = BingoTargetRegistry.getRandomTargets(tier, 5, rand);
      List<BingoBoard.BingoSlot> slots = new ArrayList();

      for(BingoTarget target : targets) {
         int spawnX = target.getRegionMinX() + rand.nextInt(Math.max(1, target.getRegionMaxX() - target.getRegionMinX()));
         int spawnZ = target.getRegionMinZ() + rand.nextInt(Math.max(1, target.getRegionMaxZ() - target.getRegionMinZ()));
         int baseRyo = target.getRandomRyo(rand);
         int ryo = (int)((double)baseRyo * tier.getDmgMultiplier());
         slots.add(new BingoBoard.BingoSlot(target.getTargetId(), spawnX, spawnZ, ryo));
      }

      BingoBoard board = new BingoBoard(slots, System.currentTimeMillis(), tier.getDisplayName(), tier.ordinal());
      EndgameSavedData data = EndgameSavedData.get(player.world);
      data.saveBingoBoard(uuid, board.toNBT());
      data.setBingoResetTime(uuid, System.currentTimeMillis());
      return board;
   }

   public boolean acceptHunt(EntityPlayerMP player, int slotIndex) {
      UUID uuid = player.getUniqueID();
      if (this.activeHunts.containsKey(uuid)) {
         player.sendMessage(new TextComponentString("§cYou already have an active bingo hunt. Abandon it first."));
         return false;
      } else {
         BingoBoard board = (BingoBoard)this.boards.get(uuid);
         if (board == null) {
            player.sendMessage(new TextComponentString("§cNo bingo board found. Open the bingo book first."));
            return false;
         } else {
            BingoBoard.BingoSlot slot = board.getSlot(slotIndex);
            if (slot == null) {
               player.sendMessage(new TextComponentString("§cInvalid bingo slot."));
               return false;
            } else if (slot.isCompleted()) {
               player.sendMessage(new TextComponentString("§cThis target has already been eliminated."));
               return false;
            } else if (slot.isActive()) {
               player.sendMessage(new TextComponentString("§cThis target is already being hunted."));
               return false;
            } else {
               BingoTarget target = BingoTargetRegistry.get(slot.getTargetId());
               if (target == null) {
                  player.sendMessage(new TextComponentString("§cTarget data not found."));
                  return false;
               } else {
                  slot.setActive(true);
                  OutpostDifficultyTier boardTier = OutpostDifficultyTier.CHUNIN;
                  if (board != null) {
                     int tierOrd = board.getTierOrdinal();
                     OutpostDifficultyTier[] tiers = OutpostDifficultyTier.values();
                     if (tierOrd >= 0 && tierOrd < tiers.length) {
                        boardTier = tiers[tierOrd];
                     }
                  }

                  BingoInstance hunt = new BingoInstance(uuid, slot, target, boardTier);
                  this.activeHunts.put(uuid, hunt);
                  player.sendMessage(new TextComponentString("§6Bingo Hunt started: §e" + target.getDisplayName()));
                  player.sendMessage(new TextComponentString("§7" + target.getLoreHint()));
                  int safeY = WaypointSpawnLogic.findGroundY(player.world, slot.getSpawnX(), slot.getSpawnZ());
                  BlockPos waypointPos = new BlockPos(slot.getSpawnX(), safeY, slot.getSpawnZ());
                  QuestManager.getInstance().getWaypointManager().setWaypoint(player, "bingo_hunt", new WaypointData(waypointPos, WaypointData.WaypointType.PVP_BINGO, target.getDisplayName()));
                  EndgameSavedData data = EndgameSavedData.get(player.world);
                  data.saveBingoBoard(uuid, board.toNBT());
                  EndgameNetworkHelper.sendBingoSync(player);
                  return true;
               }
            }
         }
      }
   }

   public void abandonHunt(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      BingoInstance hunt = (BingoInstance)this.activeHunts.remove(uuid);
      if (hunt == null) {
         player.sendMessage(new TextComponentString("§cNo active bingo hunt to abandon."));
      } else {
         hunt.getSlot().setActive(false);
         hunt.cleanup(player.world);
         QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "bingo_hunt");
         QuestModInit.NETWORK.sendTo(QuestCombatHealthMessage.clear(), player);
         player.sendMessage(new TextComponentString("§eBingo hunt abandoned: " + hunt.getTargetDef().getDisplayName()));
         BingoBoard board = (BingoBoard)this.boards.get(uuid);
         if (board != null) {
            EndgameSavedData data = EndgameSavedData.get(player.world);
            data.saveBingoBoard(uuid, board.toNBT());
         }

         EndgameNetworkHelper.sendBingoSync(player);
      }
   }

   public void onTargetKilled(UUID playerUUID, UUID entityUUID) {
      BingoInstance hunt = (BingoInstance)this.activeHunts.get(playerUUID);
      if (hunt != null) {
         if (hunt.isTargetEntity(entityUUID)) {
            hunt.confirmTargetKilled();
         }

      }
   }

   private void completeHunt(World world, EntityPlayerMP player, BingoInstance hunt) {
      UUID uuid = player.getUniqueID();
      this.activeHunts.remove(uuid);
      BingoBoard.BingoSlot slot = hunt.getSlot();
      slot.setCompleted(true);
      slot.setActive(false);
      QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "bingo_hunt");
      QuestModInit.NETWORK.sendTo(QuestCombatHealthMessage.clear(), player);
      int ryo = slot.getRyoReward();
      RyoRewardHelper.grantRyo(player, ryo);
      player.sendMessage(new TextComponentString("§a§lBingo Target Eliminated! §r§e" + hunt.getTargetDef().getDisplayName()));
      EndgameSavedData xpData = EndgameSavedData.get(world);
      BingoBoard boardRef = (BingoBoard)this.boards.get(uuid);
      int tierOrd = boardRef != null ? boardRef.getTierOrdinal() : 0;
      int pveXpAmount = PveRank.getBingoXp(tierOrd);
      PveRank rankBefore = xpData.getPveRank(uuid);
      xpData.addPveXp(uuid, pveXpAmount);
      PveRank rankAfter = xpData.getPveRank(uuid);
      player.sendMessage(new TextComponentString("§b+" + pveXpAmount + " PvE XP"));
      if (rankAfter != rankBefore) {
         player.sendMessage(new TextComponentString("§6§l★ PvE RANK UP! §r§e" + rankBefore.getDisplayName() + " → " + rankAfter.getDisplayName()));
      }

      BingoBoard board = (BingoBoard)this.boards.get(uuid);
      if (board != null && board.isAllComplete()) {
         int jackpot = board.getJackpotRyo();
         RyoRewardHelper.grantRyo(player, jackpot);
         player.sendMessage(new TextComponentString("§6§l★ BINGO BOOK COMPLETE! §r§aJackpot Bonus: §e" + jackpot + " Ryo"));
         xpData.addPveXp(uuid, 150);
         player.sendMessage(new TextComponentString("§b+150 PvE XP (Jackpot Bonus)"));
         EndgameSavedData data = EndgameSavedData.get(world);
         data.incrementLeaderboard("bingoCompletions", uuid);
      }

      hunt.cleanup(world);
      if (board != null) {
         EndgameSavedData data = EndgameSavedData.get(world);
         data.saveBingoBoard(uuid, board.toNBT());
      }

      EndgameNetworkHelper.sendBingoSync(player);
   }

   private void failHunt(World world, EntityPlayerMP player, BingoInstance hunt) {
      UUID uuid = player.getUniqueID();
      this.activeHunts.remove(uuid);
      BingoBoard.BingoSlot slot = hunt.getSlot();
      slot.setActive(false);
      QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "bingo_hunt");
      QuestModInit.NETWORK.sendTo(QuestCombatHealthMessage.clear(), player);
      hunt.cleanup(world);
      BingoBoard board = (BingoBoard)this.boards.get(uuid);
      if (board != null) {
         EndgameSavedData data = EndgameSavedData.get(world);
         data.saveBingoBoard(uuid, board.toNBT());
      }

      EndgameNetworkHelper.sendBingoSync(player);
   }

   public void onPlayerLogin(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      EndgameSavedData data = EndgameSavedData.get(player.world);
      NBTTagCompound boardNbt = data.getSavedBingoBoard(uuid);
      boolean needsGenerate = false;
      if (boardNbt != null) {
         BingoBoard board = BingoBoard.fromNBT(boardNbt);
         if (this.isDailyResetDue(uuid, player.world)) {
            this.boards.remove(uuid);
            data.removeBingoBoard(uuid);
            needsGenerate = true;
         } else {
            this.boards.put(uuid, board);
         }
      } else {
         needsGenerate = true;
      }

      if (needsGenerate) {
         BingoBoard board = this.generateBoard(player);
         this.boards.put(uuid, board);
      }

   }

   public void onPlayerDisconnect(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      BingoInstance hunt = (BingoInstance)this.activeHunts.remove(uuid);
      if (hunt != null) {
         hunt.getSlot().setActive(false);
         hunt.cleanup(player.world);
         QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "bingo_hunt");
         QuestModInit.NETWORK.sendTo(QuestCombatHealthMessage.clear(), player);
         BingoBoard board = (BingoBoard)this.boards.get(uuid);
         if (board != null) {
            EndgameSavedData data = EndgameSavedData.get(player.world);
            data.saveBingoBoard(uuid, board.toNBT());
         }

         EndgameNetworkHelper.sendBingoSync(player);
      }

   }

   public boolean isDailyResetDue(UUID player, World world) {
      EndgameSavedData data = EndgameSavedData.get(world);
      long lastReset = data.getBingoResetTime(player);
      if (lastReset == 0L) {
         return true;
      } else {
         long now = System.currentTimeMillis();
         long lastResetDay = (lastReset - RESET_ANCHOR_MS) / 86400000L;
         long currentDay = (now - RESET_ANCHOR_MS) / 86400000L;
         return currentDay > lastResetDay;
      }
   }

   public long getNextDailyReset() {
      long now = System.currentTimeMillis();
      long elapsed = now - RESET_ANCHOR_MS;
      long daysPassed = elapsed / 86400000L;
      return RESET_ANCHOR_MS + (daysPassed + 1L) * 86400000L;
   }

   private void checkDailyResets(World world) {
      for(UUID uuid : new ArrayList(this.boards.keySet())) {
         if (this.isDailyResetDue(uuid, world)) {
            BingoInstance hunt = (BingoInstance)this.activeHunts.remove(uuid);
            if (hunt != null) {
               hunt.getSlot().setActive(false);
               hunt.cleanup(world);
            }

            this.boards.remove(uuid);
            EndgameSavedData data = EndgameSavedData.get(world);
            data.removeBingoBoard(uuid);
         }
      }

   }

   private OutpostDifficultyTier getPlayerTier(EntityPlayerMP player) {
      OutpostDifficultyTier[] tiers = OutpostDifficultyTier.values();
      return tiers[(new Random()).nextInt(tiers.length)];
   }

   public BingoBoard getBoard(UUID playerUUID) {
      return (BingoBoard)this.boards.get(playerUUID);
   }

   public BingoInstance getActiveHunt(UUID playerUUID) {
      return (BingoInstance)this.activeHunts.get(playerUUID);
   }

   public boolean hasActiveHunt(UUID playerUUID) {
      return this.activeHunts.containsKey(playerUUID);
   }

   private EntityPlayerMP findPlayer(World world, UUID uuid) {
      for(EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
         if (player.getUniqueID().equals(uuid)) {
            return player;
         }
      }

      return null;
   }

   static {
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      cal.set(2026, 1, 28, 21, 0, 0);
      cal.set(14, 0);
      RESET_ANCHOR_MS = cal.getTimeInMillis();
   }
}
