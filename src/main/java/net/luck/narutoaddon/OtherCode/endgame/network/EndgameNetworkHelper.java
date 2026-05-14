
package net.luck.narutoaddon.OtherCode.endgame.network;

import com.mojang.authlib.GameProfile;
import net.luck.narutoaddon.OtherCode.endgame.EndgameModInit;
import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.endgame.PveRank;
import net.luck.narutoaddon.OtherCode.endgame.bingo.*;
import net.luck.narutoaddon.OtherCode.endgame.defense.DefenseInstance;
import net.luck.narutoaddon.OtherCode.endgame.defense.DefenseManager;
import net.luck.narutoaddon.OtherCode.endgame.incursion.IncursionInstance;
import net.luck.narutoaddon.OtherCode.endgame.incursion.IncursionManager;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostInstance;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostManager;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EndgameNetworkHelper {
   public static void sendFullSync(EntityPlayerMP player) {
      EndgameSyncMessage msg = new EndgameSyncMessage((byte)0);
      populateBingoData(msg, player);
      EndgameSavedData savedData = EndgameSavedData.get(player.getServerWorld());
      int xp = savedData.getPveXp(player.getUniqueID());
      PveRank rank = PveRank.fromXp(xp);
      msg.setPveRankData(xp, rank.getDisplayName(), rank.xpToNextRank(xp));
      populateOutpostData(msg, player);
      populateIncursionData(msg, player);
      populateDefenseData(msg, player);
      EndgameModInit.NETWORK.sendTo(msg, player);
   }

   private static void populateOutpostData(EndgameSyncMessage msg, EntityPlayerMP player) {
      try {
         OutpostManager om = OutpostManager.getInstance();
         UUID uuid = player.getUniqueID();
         String instanceKey = om.getPlayerInstanceKey(uuid);
         if (instanceKey != null) {
            OutpostInstance oi = om.getActiveOutpost(instanceKey);
            if (oi != null) {
               msg.outpostActive = true;
               msg.outpostLocationName = oi.getDefinition().getLocationName();
               OutpostInstance.OutpostState state = oi.getState();
               if (state != OutpostInstance.OutpostState.ACTIVE && state != OutpostInstance.OutpostState.VICTORY) {
                  msg.outpostEncounterName = "???";
               } else {
                  msg.outpostEncounterName = oi.getEncounterGroup().getDisplayName();
               }

               msg.outpostState = state.name();
            }
         }
      } catch (Exception var7) {
      }

   }

   public static void sendPveRankSync(EntityPlayerMP player) {
      EndgameSavedData data = EndgameSavedData.get(player.getServerWorld());
      int xp = data.getPveXp(player.getUniqueID());
      PveRank rank = PveRank.fromXp(xp);
      int xpToNext = rank.xpToNextRank(xp);
      EndgameModInit.NETWORK.sendTo(EndgameSyncMessage.pveRank(xp, rank.getDisplayName(), xpToNext), player);
   }

   public static void sendBingoSync(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      BingoManager bm = BingoManager.getInstance();
      BingoBoard board = bm.getBoard(uuid);
      List<EndgameSyncMessage.BingoSlotData> slotData = new ArrayList();
      boolean allComplete = false;
      int activeSlot = -1;
      double wpX = (double)0.0F;
      double wpZ = (double)0.0F;
      double searchRadius = (double)0.0F;
      if (board != null) {
         allComplete = board.isAllComplete();
         List<BingoBoard.BingoSlot> slots = board.getSlots();

         for(int i = 0; i < slots.size(); ++i) {
            BingoBoard.BingoSlot slot = (BingoBoard.BingoSlot)slots.get(i);
            EndgameSyncMessage.BingoSlotData sd = new EndgameSyncMessage.BingoSlotData();
            BingoTarget target = BingoTargetRegistry.get(slot.getTargetId());
            sd.targetName = target != null ? target.getDisplayName() : slot.getTargetId();
            sd.loreHint = target != null ? target.getLoreHint() : "";
            sd.regionName = target != null ? target.getTier().getDisplayName() : "";
            sd.ryoReward = slot.getRyoReward();
            sd.completed = slot.isCompleted();
            sd.active = slot.isActive();
            slotData.add(sd);
         }
      }

      BingoInstance hunt = bm.getActiveHunt(uuid);
      if (hunt != null) {
         if (board != null) {
            List<BingoBoard.BingoSlot> slots = board.getSlots();

            for(int i = 0; i < slots.size(); ++i) {
               if (((BingoBoard.BingoSlot)slots.get(i)).isActive()) {
                  activeSlot = i;
                  break;
               }
            }
         }

         wpX = (double)hunt.getSpawnLocation().getX();
         wpZ = (double)hunt.getSpawnLocation().getZ();
         searchRadius = hunt.getSearchRadius();
      }

      String boardTierName = board != null ? board.getTierName() : "";
      EndgameSyncMessage msg = EndgameSyncMessage.bingo(slotData, allComplete, bm.getNextDailyReset(), activeSlot, wpX, wpZ, searchRadius, boardTierName);
      EndgameModInit.NETWORK.sendTo(msg, player);
   }

   public static void sendCombatUpdate(EntityPlayerMP player, int entityId, String name, float hp, float maxHp, int phase, byte systemType) {
      EndgameModInit.NETWORK.sendTo(new EndgameCombatMessage(entityId, name, hp, maxHp, phase, systemType), player);
   }

   public static void sendLeaderboard(EntityPlayerMP player, String category) {
      String dataKey = mapCategoryToDataKey(category);
      EndgameSavedData data = EndgameSavedData.get(player.getServerWorld());
      List<Map.Entry<UUID, Integer>> topPlayers = data.getTopPlayers(dataKey, 10);
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      List<EndgameSyncMessage.LeaderboardEntryData> entries = new ArrayList();

      for(Map.Entry<UUID, Integer> entry : topPlayers) {
         EndgameSyncMessage.LeaderboardEntryData ed = new EndgameSyncMessage.LeaderboardEntryData();
         String name = null;
         if (server != null) {
            GameProfile profile = server.getPlayerProfileCache().getProfileByUUID((UUID)entry.getKey());
            if (profile != null) {
               name = profile.getName();
            }
         }

         ed.playerName = name != null ? name : ((UUID)entry.getKey()).toString().substring(0, 8);
         ed.value = (Integer)entry.getValue();
         entries.add(ed);
      }

      EndgameModInit.NETWORK.sendTo(EndgameSyncMessage.leaderboard(category, entries), player);
   }

   private static String mapCategoryToDataKey(String guiCategory) {
      if (guiCategory == null) {
         return "outpostClears";
      } else {
         switch (guiCategory) {
            case "outposts":
               return "outpostClears";
            case "bingo":
               return "bingoCompletions";
            case "incursions":
               return "incursionWaves";
            case "defense":
               return "defenseWaves";
            default:
               return guiCategory;
         }
      }
   }

   public static void sendOutpostSync(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      OutpostManager om = OutpostManager.getInstance();
      String instanceKey = om.getPlayerInstanceKey(uuid);
      EndgameSyncMessage msg = new EndgameSyncMessage((byte)8);
      if (instanceKey != null) {
         OutpostInstance oi = om.getActiveOutpost(instanceKey);
         if (oi != null) {
            msg.outpostActive = true;
            msg.outpostLocationName = oi.getDefinition().getLocationName();
            OutpostInstance.OutpostState state = oi.getState();
            if (state != OutpostInstance.OutpostState.ACTIVE && state != OutpostInstance.OutpostState.VICTORY) {
               msg.outpostEncounterName = "???";
            } else {
               msg.outpostEncounterName = oi.getEncounterGroup().getDisplayName();
            }

            msg.outpostState = state.name();
         }
      }

      EndgameModInit.NETWORK.sendTo(msg, player);
   }

   public static void sendIncursionSync(EntityPlayerMP player) {
      IncursionManager im = IncursionManager.getInstance();
      IncursionInstance incursion = im.getActiveIncursion();
      EndgameSyncMessage msg;
      if (incursion != null) {
         msg = EndgameSyncMessage.incursion(true, incursion.getDefinition().getDisplayName(), (double)incursion.getSpawnLocation().getX(), (double)incursion.getSpawnLocation().getZ(), incursion.getCurrentWaveIndex() + 1, incursion.getTotalWaves(), incursion.getSpawnedEntities().size(), im.isPlayerInIncursion(player), incursion.getState().name(), incursion.getCountdownSeconds());
      } else {
         msg = EndgameSyncMessage.incursion(false, "", (double)0.0F, (double)0.0F, 0, 0, 0, false, "", -1);
      }

      EndgameModInit.NETWORK.sendTo(msg, player);
   }

   private static void populateIncursionData(EndgameSyncMessage msg, EntityPlayerMP player) {
      IncursionManager im = IncursionManager.getInstance();
      IncursionInstance incursion = im.getActiveIncursion();
      if (incursion != null) {
         msg.setIncursionData(true, incursion.getDefinition().getDisplayName(), (double)incursion.getSpawnLocation().getX(), (double)incursion.getSpawnLocation().getZ(), incursion.getCurrentWaveIndex() + 1, incursion.getTotalWaves(), incursion.getSpawnedEntities().size(), im.isPlayerInIncursion(player), incursion.getState().name(), incursion.getCountdownSeconds());
      }

   }

   public static void sendDefenseSync(EntityPlayerMP player) {
      VillageHelper.Village village = VillageHelper.getVillage(player);
      EndgameSyncMessage msg;
      if (village != VillageHelper.Village.UNKNOWN) {
         DefenseInstance inst = DefenseManager.getInstance().getActiveDefense(village.name());
         if (inst != null) {
            msg = EndgameSyncMessage.defense(true, village.name(), inst.getCurrentWaveIndex() + 1, inst.getTotalWaves(), inst.getVillageHP(), inst.getDefinition().getVillageMaxHP());
         } else {
            msg = EndgameSyncMessage.defense(false, "", 0, 0, 0, 0);
         }
      } else {
         msg = EndgameSyncMessage.defense(false, "", 0, 0, 0, 0);
      }

      EndgameModInit.NETWORK.sendTo(msg, player);
   }

   private static void populateDefenseData(EndgameSyncMessage msg, EntityPlayerMP player) {
      VillageHelper.Village village = VillageHelper.getVillage(player);
      if (village != VillageHelper.Village.UNKNOWN) {
         DefenseInstance inst = DefenseManager.getInstance().getActiveDefense(village.name());
         if (inst != null) {
            msg.setDefenseData(true, village.name(), inst.getCurrentWaveIndex() + 1, inst.getTotalWaves(), inst.getVillageHP(), inst.getDefinition().getVillageMaxHP());
         }
      }

   }

   private static void populateBingoData(EndgameSyncMessage msg, EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      BingoManager bm = BingoManager.getInstance();
      BingoBoard board = bm.getBoard(uuid);
      if (board != null) {
         msg.setBingoData(board, bm.getActiveHunt(uuid), bm.getNextDailyReset());
      }

   }
}
