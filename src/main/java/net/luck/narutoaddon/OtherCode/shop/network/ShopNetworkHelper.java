
package net.luck.narutoaddon.OtherCode.shop.network;

import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.shop.core.*;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateDefinition;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateLootEntry;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateRegistry;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassManager;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassSeason;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class ShopNetworkHelper {
   public static void sendShopSync(EntityPlayerMP player) {
      if (ShopModInit.NETWORK != null) {
         CrateRegistry.init();
         UUID playerId = player.getUniqueID();
         ShopSavedData data = ShopSavedData.get(player.world);
         long balance = data.getBalance(playerId);
         int tokens = data.getTokens(playerId);
         int crystals = data.getCrystals(playerId);
         int streak = data.getLoginStreak(playerId);
         int overflowCount = data.getOverflowCount(playerId);
         Map<String, Integer> playerPity = new HashMap();

         for(CrateDefinition crate : CrateRegistry.getAll()) {
            String crateId = crate.getCrateId();
            int pity = data.getPityCount(playerId, crateId);
            if (pity > 0) {
               playerPity.put(crateId, pity);
            }
         }

         List<CrateDefinition> allCrates = CrateRegistry.getAll();
         List<ShopSyncMessage.CrateSyncInfo> crateSyncList = new ArrayList();

         for(CrateDefinition crate : allCrates) {
            ShopSyncMessage.CrateSyncInfo info = new ShopSyncMessage.CrateSyncInfo();
            info.crateId = crate.getCrateId();
            info.displayName = crate.getDisplayName();
            info.description = crate.getDescription();
            info.categoryOrdinal = (byte)crate.getCategory().id;
            info.price = crate.getPrice();
            info.crystalPrice = crate.getCrystalPrice();
            info.directPurchase = crate.isDirectPurchase();
            info.iconItemId = crate.getIconItemId();
            info.iconMeta = crate.getIconItemMeta();
            info.lootEntries = new ArrayList();

            for(CrateLootEntry entry : crate.getEntries()) {
               ShopSyncMessage.LootEntrySyncInfo lootInfo = new ShopSyncMessage.LootEntrySyncInfo();
               lootInfo.itemId = entry.getItemId();
               lootInfo.itemMeta = entry.getItemMeta();
               lootInfo.itemCount = 1;
               lootInfo.rarityOrdinal = (byte)entry.getRarity().id;
               lootInfo.weight = (double)0.0F;
               if (crate.usesCrystals()) {
                  lootInfo.dropChancePct = 0.0F;
               } else {
                  lootInfo.dropChancePct = entry.getDropChance(crate.getTotalWeight());
               }

               lootInfo.hasDisplayNameOverride = entry.hasDisplayNameOverride();
               if (lootInfo.hasDisplayNameOverride) {
                  lootInfo.displayNameOverride = entry.getDisplayNameOverride();
               }

               lootInfo.hasItemNbt = false;
               lootInfo.itemNbt = null;
               lootInfo.bundleItems = new ArrayList();
               info.lootEntries.add(lootInfo);
            }

            crateSyncList.add(info);
         }

         boolean clanUnlocked = hasAdvancement(player, "inftsukaddon:arc3_complete") || QuestManager.getInstance().hasCompletedQuestForAdvancement(player.getUniqueID(), "inftsukaddon:arc3_complete");
         boolean so6pUnlocked = hasAdvancement(player, "inftsukaddon:shinobi_way") || QuestManager.getInstance().hasCompletedQuestForAdvancement(player.getUniqueID(), "inftsukaddon:shinobi_way");
         boolean akamichiUnlocked = hasAdvancement(player, "clansaddon:clans/akamichi_unlocked");
         boolean freeRollAvailable = !data.hasUsedFreeRoll(playerId);
         boolean firstPurchaseBonusAvailable = !data.hasUsedFirstPurchaseBonus(playerId);
         boolean weekendPromoSO6PAvailable = data.hasOwnedItem(playerId, "weekend_promo_so6p_available") && !data.hasOwnedItem(playerId, "weekend_promo_so6p_claimed");
         ShopSyncMessage msg = new ShopSyncMessage(balance, tokens, crystals, streak, overflowCount, clanUnlocked, so6pUnlocked, akamichiUnlocked, freeRollAvailable, firstPurchaseBonusAvailable, weekendPromoSO6PAvailable, playerPity, crateSyncList);
         ShopModInit.NETWORK.sendTo(msg, player);
      }
   }

   public static void sendCrateResult(EntityPlayerMP player, ShopCrateResultMessage resultMsg) {
      if (ShopModInit.NETWORK != null) {
         ShopModInit.NETWORK.sendTo(resultMsg, player);
      }
   }

   public static void sendHistorySync(EntityPlayerMP player) {
      if (ShopModInit.NETWORK != null) {
         UUID playerId = player.getUniqueID();
         ShopSavedData data = ShopSavedData.get(player.world);
         List<ShopHistoryEntry> serverHistory = data.getHistory(playerId);
         List<ShopHistorySyncMessage.HistoryEntry> entries = new ArrayList();

         for(ShopHistoryEntry he : serverHistory) {
            CrateDefinition crate = CrateRegistry.getById(he.getCrateId());
            String crateDisplayName = crate != null ? crate.getDisplayName() : he.getCrateId();
            entries.add(new ShopHistorySyncMessage.HistoryEntry(crateDisplayName, he.getDisplayName(), (byte)he.getRarityOrdinal(), he.getTimestamp()));
         }

         ShopHistorySyncMessage msg = new ShopHistorySyncMessage(entries);
         ShopModInit.NETWORK.sendTo(msg, player);
      }
   }

   public static void sendAdminSync(MinecraftServer server, UUID targetId, String targetName, EntityPlayerMP admin) {
      if (ShopModInit.NETWORK != null) {
         ShopSavedData data = ShopSavedData.get(server.getWorld(0));
         PlayerItemTracker tracker = PlayerItemTracker.get(server.getWorld(0));
         CrateRegistry.init();
         EntityPlayerMP onlineTarget = server.getPlayerList().getPlayerByUUID(targetId);
         String displayName = targetName;
         if (onlineTarget != null) {
            displayName = onlineTarget.getName();
            NarutoItemScanner.scanAndUpdateTracker(onlineTarget, tracker);
         }

         long ryoBalance = data.getBalance(targetId);
         int tokenBalance = data.getTokens(targetId);
         int loginStreak = data.getLoginStreak(targetId);
         int overflowCount = data.getOverflowCount(targetId);
         long lifetimeSpent = data.getLifetimeSpent(targetId);
         int lifetimeCratesOpened = data.getLifetimeCratesOpened(targetId);
         List<ShopHistoryEntry> history = data.getHistory(targetId);
         List<ShopAdminSyncMessage.HistoryData> historyData = new ArrayList();

         for(ShopHistoryEntry entry : history) {
            ShopAdminSyncMessage.HistoryData hd = new ShopAdminSyncMessage.HistoryData();
            hd.crateId = entry.getCrateId();
            hd.displayName = entry.getDisplayName();
            hd.rarityOrdinal = entry.getRarityOrdinal();
            hd.timestamp = entry.getTimestamp();
            hd.tokenAmount = entry.getTokenAmount();
            historyData.add(hd);
         }

         Set<String> ownedSet = data.getOwnedItems(targetId);
         List<String> ownedList = new ArrayList(ownedSet);
         Collections.sort(ownedList);
         Map<String, Integer> pityMap = new HashMap();

         for(CrateDefinition crate : CrateRegistry.getAll()) {
            int sPity = data.getPityCount(targetId, crate.getCrateId());
            if (sPity > 0) {
               pityMap.put(crate.getCrateId(), sPity);
            }
         }

         List<TrackedItem> tracked = tracker.getTrackedItems(targetId);
         List<ShopAdminSyncMessage.TrackedItemData> trackedData = new ArrayList();

         for(TrackedItem ti : tracked) {
            ShopAdminSyncMessage.TrackedItemData td = new ShopAdminSyncMessage.TrackedItemData();
            td.regName = ti.itemRegistryName;
            td.meta = ti.meta;
            td.displayName = ti.displayName;
            td.category = ti.category;
            td.owned = ti.currentlyOwned;
            td.firstSeen = ti.firstSeenTimestamp;
            td.lastSeen = ti.lastSeenTimestamp;
            td.jutsuXp = ti.jutsuXp;
            td.hasOwner = ti.hasOwner;
            td.isAffinity = ti.isAffinity;
            trackedData.add(td);
         }

         List<RestoreLogEntry> restoreEntries = tracker.getRestoreLog(targetId);
         List<ShopAdminSyncMessage.RestoreLogData> restoreData = new ArrayList();

         for(RestoreLogEntry re : restoreEntries) {
            ShopAdminSyncMessage.RestoreLogData rl = new ShopAdminSyncMessage.RestoreLogData();
            rl.timestamp = re.timestamp;
            rl.adminName = re.adminName;
            rl.itemName = re.itemDisplayName;
            rl.targetPlayerName = re.targetPlayerName;
            restoreData.add(rl);
         }

         ShopAdminSyncMessage msg = new ShopAdminSyncMessage(displayName, targetId.toString(), ryoBalance, tokenBalance, loginStreak, overflowCount, lifetimeSpent, lifetimeCratesOpened, historyData, ownedList, pityMap, trackedData, restoreData);
         ShopModInit.NETWORK.sendTo(msg, admin);
      }
   }

   public static void sendBattlePassSync(EntityPlayerMP player) {
      if (ShopModInit.NETWORK != null) {
         UUID playerId = player.getUniqueID();
         ShopSavedData data = ShopSavedData.get(player.world);
         BattlePassManager bpManager = BattlePassManager.getInstance();
         BattlePassSeason season = bpManager.getCurrentSeason(player.world);
         int otsutsukiSkipsRemaining = 0;
         if (data.getBpPurchasedTier(playerId) == 2) {
            otsutsukiSkipsRemaining = 5 - data.getBpOtsutsukiSkipsUsed(playerId);
            if (otsutsukiSkipsRemaining < 0) {
               otsutsukiSkipsRemaining = 0;
            }
         }

         BattlePassSyncMessage msg = new BattlePassSyncMessage(season.getSeasonNumber(), season.getSeasonName(), season.getEndTime(), data.getBpCurrentLevel(playerId), data.getBpCurrentXP(playerId), data.getBpPurchasedTier(playerId), data.getBpClaimedTiers(playerId), data.getActiveKillEffect(playerId), data.getOwnedKillEffects(playerId), otsutsukiSkipsRemaining);
         ShopModInit.NETWORK.sendTo(msg, player);
      }
   }

   private static boolean hasAdvancement(EntityPlayerMP player, String advancementId) {
      try {
         ResourceLocation loc = new ResourceLocation(advancementId);
         Advancement advancement = player.getServer().getAdvancementManager().getAdvancement(loc);
         if (advancement != null) {
            AdvancementProgress progress = player.getAdvancements().getProgress(advancement);
            return progress.isDone();
         }
      } catch (Exception var5) {
      }

      return false;
   }
}
