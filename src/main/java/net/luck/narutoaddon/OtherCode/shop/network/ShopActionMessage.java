
package net.luck.narutoaddon.OtherCode.shop.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.shop.core.*;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateDefinition;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateLootEntry;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateRegistry;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.narutomod.item.ItemJutsu;

import java.util.*;

public class ShopActionMessage implements IMessage {
   public static final byte ACTION_REQUEST_SYNC = 0;
   public static final byte ACTION_DEPOSIT_RYO = 1;
   public static final byte ACTION_OPEN_CRATE = 2;
   public static final byte ACTION_REQUEST_HISTORY = 3;
   public static final byte ACTION_TOKEN_PURCHASE = 4;
   public static final byte ACTION_MULTI_OPEN = 5;
   public static final byte ACTION_CLAIM_RESULT = 6;
   public static final byte ACTION_CLAIM_OVERFLOW = 7;
   public static final byte ACTION_TRAVEL_WARP = 8;
   public static final byte ACTION_BP_PURCHASE = 9;
   public static final byte ACTION_BP_CLAIM = 10;
   public static final byte ACTION_BP_SKIP = 11;
   public static final byte ACTION_REDEEM_AKAMICHI = 12;
   public static final int TOKEN_COST_D_RANK = 10;
   public static final int TOKEN_COST_C_RANK = 30;
   public static final int TOKEN_COST_B_RANK = 80;
   public static final int TOKEN_COST_A_RANK = 200;
   public static final int TOKEN_COST_S_RANK = 500;
   public static final int TOKEN_COST_S_PLUS_RANK = 1200;
   public static final int TOKENS_PER_SINGLE_ROLL = 1;
   public static final int TOKENS_PER_MULTI_ROLL = 11;
   public static final int TEN_ROLL_GUARANTEED_MIN_TIER = 2;
   public static final long MIN_CRATE_PRICE_FOR_TOKENS = 5000L;
   private byte action;
   private String payload;

   public ShopActionMessage() {
      this.payload = "";
   }

   public ShopActionMessage(byte action, String payload) {
      this.action = action;
      this.payload = payload != null ? payload : "";
   }

   public ShopActionMessage(byte action) {
      this(action, "");
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readByte();
      this.payload = ByteBufUtils.readUTF8String(buf);
   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.action);
      ByteBufUtils.writeUTF8String(buf, this.payload != null ? this.payload : "");
   }

   public static class Handler implements IMessageHandler<ShopActionMessage, IMessage> {
      private static final Random RNG = new Random();
      private static final Map<UUID, PendingCrateResult> pendingResults = new HashMap();
      private static final Map<UUID, Long> lastCrateOpenTime = new HashMap();
      private static final Map<UUID, Long> lastActionTime = new HashMap();
      private static final long CRATE_OPEN_COOLDOWN_MS = 500L;
      private static final long GENERAL_ACTION_COOLDOWN_MS = 200L;
      private static final String[] RYO_ITEM_NAMES = new String[]{"narutomod:ryo_100", "narutomod:ryo_1000", "narutomod:ryo_10000", "narutomod:ryo_1_m"};
      private static final long[] RYO_VALUES = new long[]{100L, 1000L, 10000L, 1000000L};
      private static final String[] VALID_VILLAGES = new String[]{"leaf", "sand", "mist", "cloud", "stone", "rain"};

      public IMessage onMessage(ShopActionMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         WorldServer world = (WorldServer)player.world;
         world.addScheduledTask(() -> {
            if (message.action == 6) {
               handleClaimResult(player);
            } else if (message.action == 7) {
               handleClaimOverflow(player);
            } else {
               UUID rateLimitId = player.getUniqueID();
               long actionNow = System.currentTimeMillis();
               Long lastAction = (Long)lastActionTime.get(rateLimitId);
               if (lastAction == null || actionNow - lastAction >= 200L) {
                  lastActionTime.put(rateLimitId, actionNow);
                  switch (message.action) {
                     case 0:
                        ShopNetworkHelper.sendShopSync(player);
                     case 1:
                     case 6:
                     case 7:
                     case 9:
                     case 10:
                     case 11:
                     default:
                        break;
                     case 2:
                        handleOpenCrate(player, message.payload);
                        break;
                     case 3:
                        ShopNetworkHelper.sendHistorySync(player);
                        break;
                     case 4:
                        handleTokenPurchase(player, message.payload);
                        break;
                     case 5:
                        handleMultiOpen(player, message.payload);
                        break;
                     case 8:
                        handleTravelWarp(player, message.payload);
                        break;
                     case 12:
                        handleRedeemAkamichi(player);
                  }

               }
            }
         });
         return null;
      }

      private static void handleDeposit(EntityPlayerMP player) {
         long totalDeposited = 0L;

         for(int r = 0; r < RYO_ITEM_NAMES.length; ++r) {
            Item ryoItem = Item.getByNameOrId(RYO_ITEM_NAMES[r]);
            if (ryoItem != null) {
               for(int slot = 0; slot < player.inventory.getSizeInventory(); ++slot) {
                  ItemStack stack = player.inventory.getStackInSlot(slot);
                  if (!stack.isEmpty() && stack.getItem() == ryoItem) {
                     totalDeposited += (long)stack.getCount() * RYO_VALUES[r];
                     player.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
                  }
               }
            }
         }

         if (totalDeposited > 0L) {
            ShopSavedData data = ShopSavedData.get(player.world);
            data.addBalance(player.getUniqueID(), totalDeposited);
            ShopNetworkHelper.sendShopSync(player);
         }

      }

      private static void handleOpenCrate(EntityPlayerMP player, String crateId) {
         if (crateId != null && crateId.length() <= 64) {
            CrateRegistry.init();
            CrateDefinition crate = CrateRegistry.getById(crateId);
            if (crate != null) {
               if (isCategoryUnlocked(player, crate.getCategory())) {
                  UUID playerId = player.getUniqueID();
                  long now = System.currentTimeMillis();
                  Long lastOpen = (Long)lastCrateOpenTime.get(playerId);
                  if (lastOpen == null || now - lastOpen >= 500L) {
                     lastCrateOpenTime.put(playerId, now);
                     ShopSavedData data = ShopSavedData.get(player.world);
                     boolean isCashCrate = crate.usesCrystals();
                     autoGrantPending(player);
                     boolean isFreeRoll = false;
                     if (isCashCrate && crateId.equals("cash_clan") && !data.hasUsedFreeRoll(playerId)) {
                        isFreeRoll = true;
                        data.markFreeRollUsed(playerId);
                     } else if (isCashCrate && crateId.equals("cash_so6p") && data.hasOwnedItem(playerId, "weekend_promo_so6p_available") && !data.hasOwnedItem(playerId, "weekend_promo_so6p_claimed")) {
                        isFreeRoll = true;
                        data.addOwnedItem(playerId, "weekend_promo_so6p_claimed");
                        data.removeOwnedItem(playerId, "weekend_promo_so6p_available");
                        player.sendMessage(new TextComponentString("§6[Weekend Promo] §eFree Premium SO6P roll consumed."));
                     } else if (isCashCrate) {
                        if (!data.hasCrystals(playerId, crate.getCrystalPrice())) {
                           return;
                        }

                        if (!data.removeCrystals(playerId, crate.getCrystalPrice())) {
                           return;
                        }

                        data.recordCrystalSpent(playerId, crate.getCrystalPrice());
                     } else {
                        long balance = data.getBalance(playerId);
                        if (balance < crate.getPrice()) {
                           return;
                        }

                        if (!data.removeBalance(playerId, crate.getPrice())) {
                           return;
                        }
                     }

                     if (!isCashCrate) {
                        data.addLifetimeSpent(playerId, crate.getPrice());
                     }

                     data.incrementLifetimeCratesOpened(playerId);
                     CrateLootEntry winner;
                     if (isCashCrate && crate.isDirectPurchase()) {
                        winner = crate.getEntries().isEmpty() ? null : (CrateLootEntry)crate.getEntries().get(0);
                     } else if (isCashCrate) {
                        int rollsSinceNonFiller = data.getPityCount(playerId, crateId);
                        if (crate.hasSPity() && rollsSinceNonFiller >= crate.getSHardPity()) {
                           data.recordPityHit(playerId);
                        }

                        winner = crate.rollRewardWithCashPity(RNG, rollsSinceNonFiller);
                     } else {
                        int rollsSinceS = data.getPityCount(playerId, crateId);
                        winner = crate.rollRewardWithPity(RNG, rollsSinceS, 0);
                     }

                     if (winner == null) {
                        if (!isFreeRoll) {
                           if (isCashCrate) {
                              data.addCrystals(playerId, crate.getCrystalPrice());
                           } else {
                              data.addBalance(playerId, crate.getPrice());
                              data.subtractLifetimeSpent(playerId, crate.getPrice());
                           }
                        }

                        System.out.println("[RyoShop] WARNING: Null roll for " + player.getName() + " on crate " + crateId + " — refunded");
                     } else {
                        if (!isCashCrate && crate.getPrice() >= 5000L) {
                           data.addTokens(playerId, 1);
                        }

                        if (isCashCrate) {
                           try {
                              BattlePassManager.getInstance().awardXP(playerId, 40, "crate_roll", player.world);
                           } catch (Exception var28) {
                           }
                        }

                        data.recordCrateRoll(playerId, crateId);
                        if (!crate.isDirectPurchase()) {
                           data.incrementPity(playerId, crateId);
                           if (isCashCrate) {
                              if (winner.getRarity().id >= ItemRarity.B_RANK.id) {
                                 data.resetPity(playerId, crateId);
                              }
                           } else if (winner.getRarity().id >= ItemRarity.S_RANK.id) {
                              data.resetPity(playerId, crateId);
                           }
                        }

                        boolean dupeEligible;
                        if (isCashCrate) {
                           dupeEligible = !crateId.equals("cash_chakra") && !crateId.equals("cash_mythical") && !crateId.equals("cash_tailed_beast");
                        } else {
                           dupeEligible = crate.getCategory() == ShopCategory.JUTSU || crate.getCategory() == ShopCategory.CLANS || crate.getCategory() == ShopCategory.KEKKEI_GENKAI;
                        }

                        String dupeKey;
                        if (winner.hasDisplayNameOverride()) {
                           dupeKey = winner.getDisplayNameOverride();
                        } else {
                           dupeKey = winner.getItemId() + ":" + winner.getItemMeta();
                        }

                        boolean isDuplicate = dupeEligible && !winner.allowsDuplicates() && data.hasOwnedItem(playerId, dupeKey);
                        int dupeTokens = 0;
                        int dupeCrystals = 0;
                        if (isCashCrate) {
                           System.out.println("[CrystalShop] Dupe check: " + player.getName() + " rolled " + dupeKey + " | eligible=" + dupeEligible + " | allowDupes=" + winner.allowsDuplicates() + " | owned=" + data.hasOwnedItem(playerId, dupeKey) + " | isDuplicate=" + isDuplicate);
                        }

                        String wonDisplayName = winner.getDisplayName();
                        if (isDuplicate) {
                           if (isCashCrate) {
                              dupeCrystals = (int)((double)crate.getCrystalPrice() * getDupeRefundRate(winner.getRarity()));
                              if (dupeCrystals > 0) {
                                 data.addCrystals(playerId, dupeCrystals);
                                 data.recordDupeRefund(playerId, dupeCrystals);
                              }
                           } else {
                              dupeTokens = DuplicateDetector.handleDuplicate(player, winner.getRarity());
                           }
                        } else {
                           PendingCrateResult pending = new PendingCrateResult(winner, crateId);
                           pendingResults.put(playerId, pending);
                           data.setPendingCrateResult(playerId, serializePending(pending));
                           if (winner.hasDisplayNameOverride()) {
                              data.addOwnedItem(playerId, winner.getDisplayNameOverride());
                           } else {
                              data.addOwnedItem(playerId, winner.getItemId() + ":" + winner.getItemMeta());
                           }
                        }

                        if (!isCashCrate) {
                           ShopBroadcastHelper.checkAndBroadcast(player, wonDisplayName, winner.getRarity(), crate.getDisplayName());
                        }

                        ShopHistoryEntry historyEntry = new ShopHistoryEntry(crateId, winner.getItemId(), winner.getItemMeta(), winner.getItemCount(), wonDisplayName, winner.getRarity().id, System.currentTimeMillis(), dupeTokens);
                        data.addHistoryEntry(playerId, historyEntry);
                        int STRIP_SIZE = crate.isDirectPurchase() ? 10 : 60;
                        int TARGET_POS = crate.isDirectPurchase() ? 5 : 45;
                        List<ShopCrateResultMessage.StripEntry> strip = new ArrayList(STRIP_SIZE);
                        double totalWeight = crate.getTotalWeight();

                        for(int i = 0; i < STRIP_SIZE; ++i) {
                           if (i == TARGET_POS) {
                              strip.add(entryToStripEntry(winner, totalWeight));
                           } else {
                              CrateLootEntry visual = crate.rollReward(RNG);
                              strip.add(entryToStripEntry(visual, totalWeight));
                           }
                        }

                        String bundleDisplayName = winner.hasDisplayNameOverride() ? winner.getDisplayNameOverride() : null;
                        List<ShopCrateResultMessage.BundleResultItem> bundleResultItems = new ArrayList();
                        if (winner.hasBundleItems()) {
                           for(CrateLootEntry.BundleItem bi : winner.getBundleItems()) {
                              bundleResultItems.add(new ShopCrateResultMessage.BundleResultItem(bi.getItemId(), bi.getItemMeta(), bi.getItemCount()));
                           }
                        }

                        long newBalance = data.getBalance(playerId);
                        ShopCrateResultMessage resultMsg = new ShopCrateResultMessage(crateId, newBalance, winner.getItemId(), winner.getItemMeta(), winner.getItemCount(), (byte)winner.getRarity().id, wonDisplayName, TARGET_POS, strip, isDuplicate, dupeTokens, dupeCrystals, bundleDisplayName, bundleResultItems);
                        ShopNetworkHelper.sendCrateResult(player, resultMsg);
                        if (!isCashCrate) {
                           ShopNetworkHelper.sendShopSync(player);
                        }

                     }
                  }
               }
            }
         }
      }

      private static void handleMultiOpen(EntityPlayerMP player, String crateId) {
         if (crateId != null && crateId.length() <= 64) {
            CrateRegistry.init();
            CrateDefinition crate = CrateRegistry.getById(crateId);
            if (crate != null) {
               if (isCategoryUnlocked(player, crate.getCategory())) {
                  UUID playerId = player.getUniqueID();
                  long now = System.currentTimeMillis();
                  Long lastOpen = (Long)lastCrateOpenTime.get(playerId);
                  if (lastOpen == null || now - lastOpen >= 500L) {
                     lastCrateOpenTime.put(playerId, now);
                     ShopSavedData data = ShopSavedData.get(player.world);
                     boolean isCashCrate = crate.usesCrystals();
                     if (!crate.isDirectPurchase()) {
                        autoGrantPending(player);
                        long ryoRefundAmount = 0L;
                        int crystalRefundAmount = 0;
                        if (isCashCrate) {
                           int multiCrystalPrice = crate.getCrystalPrice() * 10;
                           if (!data.hasCrystals(playerId, multiCrystalPrice)) {
                              return;
                           }

                           if (!data.removeCrystals(playerId, multiCrystalPrice)) {
                              return;
                           }

                           data.recordCrystalSpent(playerId, multiCrystalPrice);
                           System.out.println("[RyoShop] 10-roll: " + player.getName() + " paid " + multiCrystalPrice + " crystals for " + crateId);
                        } else {
                           long multiPrice = crate.getPrice() * 9L;
                           long balance = data.getBalance(playerId);
                           if (balance < multiPrice) {
                              return;
                           }

                           if (!data.removeBalance(playerId, multiPrice)) {
                              return;
                           }

                           data.addLifetimeSpent(playerId, multiPrice);
                           System.out.println("[RyoShop] 10-roll: " + player.getName() + " paid " + multiPrice + " ryo for " + crateId + " (lifetime=" + data.getLifetimeSpent(playerId) + ")");
                        }

                        List<CrateLootEntry> results = new ArrayList(10);

                        try {
                           for(int lt = 0; lt < 10; ++lt) {
                              data.incrementLifetimeCratesOpened(playerId);
                           }

                           int highestTierRolled = -1;
                           int lowestTierIndex = 0;

                           for(int i = 0; i < 10; ++i) {
                              CrateLootEntry winner;
                              if (isCashCrate) {
                                 int rollsSinceNonFiller = data.getPityCount(playerId, crateId);
                                 if (crate.hasSPity() && rollsSinceNonFiller >= crate.getSHardPity()) {
                                    data.recordPityHit(playerId);
                                 }

                                 winner = crate.rollRewardWithCashPity(RNG, rollsSinceNonFiller);
                              } else {
                                 int rollsSinceS = data.getPityCount(playerId, crateId);
                                 winner = crate.rollRewardWithPity(RNG, rollsSinceS, 0);
                              }

                              if (winner == null) {
                                 if (isCashCrate) {
                                    data.addCrystals(playerId, crate.getCrystalPrice() * 10);
                                 } else {
                                    data.addBalance(playerId, crate.getPrice() * 9L);
                                    data.subtractLifetimeSpent(playerId, crate.getPrice() * 9L);
                                 }

                                 System.out.println("[RyoShop] WARNING: Null roll on 10-roll for " + player.getName() + " crate " + crateId + " at roll " + i + " — fully refunded");
                                 return;
                              }

                              data.recordCrateRoll(playerId, crateId);
                              data.incrementPity(playerId, crateId);
                              if (isCashCrate) {
                                 if (winner.getRarity().id >= ItemRarity.B_RANK.id) {
                                    data.resetPity(playerId, crateId);
                                 }
                              } else if (winner.getRarity().id >= ItemRarity.S_RANK.id) {
                                 data.resetPity(playerId, crateId);
                              }

                              results.add(winner);
                              int tierId = winner.getRarity().id;
                              if (tierId > highestTierRolled) {
                                 highestTierRolled = tierId;
                              }

                              if (tierId < ((CrateLootEntry)results.get(lowestTierIndex)).getRarity().id) {
                                 lowestTierIndex = i;
                              }
                           }

                           if (!isCashCrate && crate.getPrice() >= 5000L) {
                              data.addTokens(playerId, 11);
                           }

                           if (isCashCrate) {
                              for(int bpR = 0; bpR < 10; ++bpR) {
                                 try {
                                    BattlePassManager.getInstance().awardXP(playerId, 40, "crate_roll", player.world);
                                 } catch (Exception var35) {
                                 }
                              }
                           }

                           if (highestTierRolled < 2) {
                              CrateLootEntry upgrade = null;

                              for(CrateLootEntry entry : crate.getEntries()) {
                                 if (entry.getRarity().id >= 2 && (upgrade == null || entry.getRarity().id < upgrade.getRarity().id)) {
                                    upgrade = entry;
                                 }
                              }

                              if (upgrade != null) {
                                 results.set(lowestTierIndex, upgrade);
                              }
                           }

                           List<CrateLootEntry> pendingWinners = new ArrayList();
                           boolean dupeEligible;
                           if (isCashCrate) {
                              dupeEligible = !crateId.equals("cash_chakra") && !crateId.equals("cash_mythical") && !crateId.equals("cash_tailed_beast");
                           } else {
                              dupeEligible = crate.getCategory() == ShopCategory.JUTSU || crate.getCategory() == ShopCategory.CLANS || crate.getCategory() == ShopCategory.KEKKEI_GENKAI;
                           }

                           int totalDupeCrystals = 0;

                           for(CrateLootEntry winner : results) {
                              String wonDisplayName = winner.getDisplayName();
                              String dupeKey;
                              if (winner.hasDisplayNameOverride()) {
                                 dupeKey = winner.getDisplayNameOverride();
                              } else {
                                 dupeKey = winner.getItemId() + ":" + winner.getItemMeta();
                              }

                              boolean isDupe = dupeEligible && !winner.allowsDuplicates() && data.hasOwnedItem(playerId, dupeKey);
                              int dupeTokens = 0;
                              if (isDupe) {
                                 if (isCashCrate) {
                                    int dupeCrystals = (int)((double)crate.getCrystalPrice() * getDupeRefundRate(winner.getRarity()));
                                    if (dupeCrystals > 0) {
                                       data.addCrystals(playerId, dupeCrystals);
                                       data.recordDupeRefund(playerId, dupeCrystals);
                                       totalDupeCrystals += dupeCrystals;
                                    }
                                 } else {
                                    dupeTokens = DuplicateDetector.handleDuplicate(player, winner.getRarity());
                                 }
                              } else {
                                 pendingWinners.add(winner);
                                 if (winner.hasDisplayNameOverride()) {
                                    data.addOwnedItem(playerId, winner.getDisplayNameOverride());
                                 } else {
                                    data.addOwnedItem(playerId, winner.getItemId() + ":" + winner.getItemMeta());
                                 }
                              }

                              if (!isCashCrate) {
                                 ShopBroadcastHelper.checkAndBroadcast(player, wonDisplayName, winner.getRarity(), crate.getDisplayName());
                              }

                              ShopHistoryEntry historyEntry = new ShopHistoryEntry(crateId, winner.getItemId(), winner.getItemMeta(), winner.getItemCount(), wonDisplayName, winner.getRarity().id, System.currentTimeMillis(), dupeTokens);
                              data.addHistoryEntry(playerId, historyEntry);
                           }

                           if (!pendingWinners.isEmpty()) {
                              PendingCrateResult pending = new PendingCrateResult(pendingWinners, crateId);
                              pendingResults.put(playerId, pending);
                              data.setPendingCrateResult(playerId, serializePending(pending));
                              System.out.println("[RyoShop] 10-roll: stored " + pendingWinners.size() + " pending items for " + player.getName() + " (dupes=" + (10 - pendingWinners.size()) + ")");
                           } else {
                              System.out.println("[RyoShop] 10-roll: all 10 items were dupes for " + player.getName());
                           }

                           double totalWeight = crate.getTotalWeight();
                           long newBalance = data.getBalance(playerId);
                           int STRIP_SIZE = 60;
                           int TARGET_POS = 45;

                           for(int r = 0; r < results.size(); ++r) {
                              CrateLootEntry winner = (CrateLootEntry)results.get(r);
                              String wonDisplayName = winner.getDisplayName();
                              List<ShopCrateResultMessage.StripEntry> strip = new ArrayList(STRIP_SIZE);

                              for(int i = 0; i < STRIP_SIZE; ++i) {
                                 if (i == TARGET_POS) {
                                    strip.add(entryToStripEntry(winner, totalWeight));
                                 } else {
                                    CrateLootEntry visual = crate.rollReward(RNG);
                                    strip.add(entryToStripEntry(visual, totalWeight));
                                 }
                              }

                              String bundleDisplayName = winner.hasDisplayNameOverride() ? winner.getDisplayNameOverride() : null;
                              List<ShopCrateResultMessage.BundleResultItem> bundleResultItems = new ArrayList();
                              if (winner.hasBundleItems()) {
                                 for(CrateLootEntry.BundleItem bi : winner.getBundleItems()) {
                                    bundleResultItems.add(new ShopCrateResultMessage.BundleResultItem(bi.getItemId(), bi.getItemMeta(), bi.getItemCount()));
                                 }
                              }

                              int msgDupeCrystals = r == results.size() - 1 ? totalDupeCrystals : 0;
                              ShopCrateResultMessage resultMsg = new ShopCrateResultMessage(crateId, newBalance, winner.getItemId(), winner.getItemMeta(), winner.getItemCount(), (byte)winner.getRarity().id, wonDisplayName, TARGET_POS, strip, false, 0, msgDupeCrystals, bundleDisplayName, bundleResultItems);
                              ShopNetworkHelper.sendCrateResult(player, resultMsg);
                           }

                           if (!isCashCrate) {
                              ShopNetworkHelper.sendShopSync(player);
                           }
                        } catch (Exception e) {
                           System.out.println("[RyoShop] CRITICAL: 10-roll failed for " + player.getName() + " on crate " + crateId + " — recovering items to bank! Error: " + e.getMessage());
                           e.printStackTrace();
                           pendingResults.remove(playerId);

                           try {
                              data.claimPendingCrateResult(playerId);
                           } catch (Exception var34) {
                           }

                           int recovered = 0;
                           if (results != null && !results.isEmpty()) {
                              for(CrateLootEntry winner : results) {
                                 try {
                                    ItemStack mainStack = winner.toItemStack();
                                    if (!mainStack.isEmpty()) {
                                       NBTTagCompound itemNbt = new NBTTagCompound();
                                       itemNbt.setString("itemId", winner.getItemId());
                                       itemNbt.setInteger("meta", winner.getItemMeta());
                                       itemNbt.setInteger("count", winner.getItemCount());
                                       if (mainStack.hasTagCompound()) {
                                          itemNbt.setTag("itemNbt", mainStack.getTagCompound().copy());
                                       }

                                       data.addOverflowItem(playerId, itemNbt);
                                       ++recovered;
                                    }

                                    if (winner.hasBundleItems()) {
                                       for(CrateLootEntry.BundleItem bi : winner.getBundleItems()) {
                                          ItemStack biStack = bi.toItemStack();
                                          if (!biStack.isEmpty()) {
                                             NBTTagCompound biNbt = new NBTTagCompound();
                                             biNbt.setString("itemId", bi.getItemId());
                                             biNbt.setInteger("meta", bi.getItemMeta());
                                             biNbt.setInteger("count", bi.getItemCount());
                                             if (biStack.hasTagCompound()) {
                                                biNbt.setTag("itemNbt", biStack.getTagCompound().copy());
                                             }

                                             data.addOverflowItem(playerId, biNbt);
                                          }
                                       }
                                    }

                                    try {
                                       ShopHistoryEntry historyEntry = new ShopHistoryEntry(crateId, winner.getItemId(), winner.getItemMeta(), winner.getItemCount(), winner.getDisplayName(), winner.getRarity().id, System.currentTimeMillis(), 0);
                                       data.addHistoryEntry(playerId, historyEntry);
                                    } catch (Exception var33) {
                                    }

                                    try {
                                       if (winner.hasDisplayNameOverride()) {
                                          data.addOwnedItem(playerId, winner.getDisplayNameOverride());
                                       } else {
                                          data.addOwnedItem(playerId, winner.getItemId() + ":" + winner.getItemMeta());
                                       }
                                    } catch (Exception var32) {
                                    }
                                 } catch (Exception itemEx) {
                                    System.out.println("[RyoShop] Failed to recover item: " + itemEx.getMessage());
                                 }
                              }
                           }

                           System.out.println("[RyoShop] Recovered " + recovered + " items to bank for " + player.getName());
                           player.sendMessage(new TextComponentString("§e[Shinobi Shop] An error occurred during your 10-roll. " + recovered + " item(s) have been placed in your bank."));
                           ShopNetworkHelper.sendShopSync(player);
                        }

                     }
                  }
               }
            }
         }
      }

      private static void handleClaimResult(EntityPlayerMP player) {
         UUID playerId = player.getUniqueID();
         PendingCrateResult pending = (PendingCrateResult)pendingResults.remove(playerId);
         if (pending != null) {
            System.out.println("[RyoShop] Claim: granting " + pending.winners.size() + " pending items to " + player.getName());
            grantPendingItems(player, pending);
         } else {
            System.out.println("[RyoShop] Claim: no in-memory pending for " + player.getName() + " — checking persisted");
         }

         ShopSavedData data = ShopSavedData.get(player.world);
         NBTTagCompound persistedPending = data.claimPendingCrateResult(playerId);
         if (pending == null && persistedPending != null) {
            System.out.println("[RyoShop] Claim: recovering from persisted data for " + player.getName());
            grantPersistedPending(player, persistedPending);
         }

         ShopNetworkHelper.sendShopSync(player);
      }

      private static void autoGrantPending(EntityPlayerMP player) {
         grantAllPending(player);
      }

      public static void grantAllPending(EntityPlayerMP player) {
         UUID playerId = player.getUniqueID();
         PendingCrateResult pending = (PendingCrateResult)pendingResults.remove(playerId);
         if (pending != null) {
            grantPendingItems(player, pending);
         }

         ShopSavedData data = ShopSavedData.get(player.world);
         NBTTagCompound persistedPending = data.claimPendingCrateResult(playerId);
         if (persistedPending != null && pending == null) {
            grantPersistedPending(player, persistedPending);
            System.out.println("[RyoShop] Crash recovery: granted pending crate items to " + player.getName());
         }

      }

      private static void grantPendingItems(EntityPlayerMP player, PendingCrateResult pending) {
         for(CrateLootEntry winner : pending.winners) {
            ItemStack mainStack = winner.toItemStack();
            if (mainStack.isEmpty()) {
               System.out.println("[RyoShop] WARNING: Failed to resolve item '" + winner.getItemId() + "' for player " + player.getName() + " — item not granted!");
            }

            unlockAllJutsu(mainStack);
            giveItem(player, mainStack);
            if (winner.hasBundleItems()) {
               for(CrateLootEntry.BundleItem bi : winner.getBundleItems()) {
                  ItemStack biStack = bi.toItemStack();
                  if (biStack.isEmpty()) {
                     System.out.println("[RyoShop] WARNING: Failed to resolve bundle item '" + bi.getItemId() + "' for player " + player.getName() + " — item not granted!");
                  }

                  unlockAllJutsu(biStack);
                  giveItem(player, biStack);
               }
            }

            if (winner.hasAdvancement()) {
               grantAdvancement(player, winner.getAdvancementId());
               grantLegacyClansAddonAdvancements(player, winner.getAdvancementId());
            }

            if (winner.hasDisplayNameOverride()) {
               ShopSavedData data = ShopSavedData.get(player.world);
               if (data != null) {
                  data.addOwnedItem(player.getUniqueID(), winner.getDisplayNameOverride());
               }
            } else {
               DuplicateDetector.recordItemOwnership(player, winner.getItemId(), winner.getItemMeta());
            }
         }

      }

      private static NBTTagCompound serializePending(PendingCrateResult pending) {
         NBTTagCompound nbt = new NBTTagCompound();
         nbt.setString("crateId", pending.crateId);
         nbt.setLong("timestamp", pending.timestamp);
         NBTTagList winnerList = new NBTTagList();

         for(CrateLootEntry winner : pending.winners) {
            NBTTagCompound winnerNbt = new NBTTagCompound();
            winnerNbt.setString("itemId", winner.getItemId());
            winnerNbt.setInteger("itemMeta", winner.getItemMeta());
            winnerNbt.setInteger("itemCount", winner.getItemCount());
            if (winner.getItemNbt() != null) {
               winnerNbt.setTag("itemNbt", winner.getItemNbt().copy());
            }

            if (winner.hasBundleItems()) {
               NBTTagList bundleList = new NBTTagList();

               for(CrateLootEntry.BundleItem bi : winner.getBundleItems()) {
                  NBTTagCompound biNbt = new NBTTagCompound();
                  biNbt.setString("itemId", bi.getItemId());
                  biNbt.setInteger("itemMeta", bi.getItemMeta());
                  biNbt.setInteger("itemCount", bi.getItemCount());
                  if (bi.getItemNbt() != null) {
                     biNbt.setTag("itemNbt", bi.getItemNbt().copy());
                  }

                  bundleList.appendTag(biNbt);
               }

               winnerNbt.setTag("bundleItems", bundleList);
            }

            if (winner.hasAdvancement()) {
               winnerNbt.setString("advancementId", winner.getAdvancementId());
            }

            if (winner.hasFtbQuest()) {
               winnerNbt.setString("ftbQuestId", winner.getFtbQuestId());
            }

            if (winner.hasDisplayNameOverride()) {
               winnerNbt.setString("displayNameOverride", winner.getDisplayNameOverride());
            }

            winnerList.appendTag(winnerNbt);
         }

         nbt.setTag("winners", winnerList);
         return nbt;
      }

      private static void grantPersistedPending(EntityPlayerMP player, NBTTagCompound nbt) {
         if (nbt.hasKey("winners")) {
            NBTTagList winnerList = nbt.getTagList("winners", 10);

            for(int i = 0; i < winnerList.tagCount(); ++i) {
               NBTTagCompound winnerNbt = winnerList.getCompoundTagAt(i);
               ItemStack mainStack = createItemStack(winnerNbt.getString("itemId"), winnerNbt.getInteger("itemMeta"), winnerNbt.getInteger("itemCount"), winnerNbt.hasKey("itemNbt") ? winnerNbt.getCompoundTag("itemNbt") : null);
               giveItem(player, mainStack);
               if (winnerNbt.hasKey("bundleItems")) {
                  NBTTagList bundleList = winnerNbt.getTagList("bundleItems", 10);

                  for(int j = 0; j < bundleList.tagCount(); ++j) {
                     NBTTagCompound biNbt = bundleList.getCompoundTagAt(j);
                     ItemStack biStack = createItemStack(biNbt.getString("itemId"), biNbt.getInteger("itemMeta"), biNbt.getInteger("itemCount"), biNbt.hasKey("itemNbt") ? biNbt.getCompoundTag("itemNbt") : null);
                     giveItem(player, biStack);
                  }
               }

               if (winnerNbt.hasKey("advancementId")) {
                  String advId = winnerNbt.getString("advancementId");
                  grantAdvancement(player, advId);
                  grantLegacyClansAddonAdvancements(player, advId);
               }

               if (winnerNbt.hasKey("ftbQuestId")) {
                  try {
                     String ftbQuestId = winnerNbt.getString("ftbQuestId");
                     MinecraftServer server = player.getServer();
                     if (server != null) {
                        server.getCommandManager().executeCommand(server, "ftbquests complete " + player.getName() + " " + ftbQuestId);
                     }
                  } catch (Exception var10) {
                  }
               }

               if (winnerNbt.hasKey("displayNameOverride")) {
                  ShopSavedData sData = ShopSavedData.get(player.world);
                  if (sData != null) {
                     sData.addOwnedItem(player.getUniqueID(), winnerNbt.getString("displayNameOverride"));
                  }
               } else {
                  String persistedItemId = winnerNbt.getString("itemId");
                  int persistedMeta = winnerNbt.getInteger("itemMeta");
                  DuplicateDetector.recordItemOwnership(player, persistedItemId, persistedMeta);
               }
            }

         }
      }

      private static ItemStack createItemStack(String itemId, int meta, int count, NBTTagCompound nbt) {
         Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
         if (item != null && item != Items.AIR) {
            ItemStack stack = new ItemStack(item, Math.max(1, count), meta);
            if (nbt != null) {
               stack.setTagCompound(nbt.copy());
            }

            return stack;
         } else {
            return ItemStack.EMPTY;
         }
      }

      private static void handleTokenPurchase(EntityPlayerMP player, String itemId) {
         if (itemId != null && !itemId.isEmpty() && itemId.length() <= 128) {
            CrateRegistry.init();
            UUID playerId = player.getUniqueID();
            ShopSavedData data = ShopSavedData.get(player.world);
            String targetItemId = itemId;
            int targetMeta = -1;
            int colonIdx = itemId.lastIndexOf(58);
            if (colonIdx > 0) {
               String afterColon = itemId.substring(colonIdx + 1);

               try {
                  targetMeta = Integer.parseInt(afterColon);
                  targetItemId = itemId.substring(0, colonIdx);
               } catch (NumberFormatException var14) {
               }
            }

            CrateLootEntry targetEntry = null;
            ShopCategory targetCategory = ShopCategory.CONSUMABLES;

            for(CrateDefinition crate : CrateRegistry.getAll()) {
               for(CrateLootEntry entry : crate.getEntries()) {
                  if (targetItemId.equals(entry.getItemId()) && (targetMeta < 0 || targetMeta == entry.getItemMeta())) {
                     targetEntry = entry;
                     targetCategory = crate.getCategory();
                     break;
                  }
               }

               if (targetEntry != null) {
                  break;
               }
            }

            if (targetEntry != null) {
               if (isCategoryUnlocked(player, targetCategory)) {
                  int tokenCost = getTokenCost(targetEntry.getRarity(), targetCategory);
                  if (tokenCost > 0) {
                     if (data.hasTokens(playerId, tokenCost)) {
                        if (data.removeTokens(playerId, tokenCost)) {
                           ItemStack itemStack = targetEntry.toItemStack();
                           if (!itemStack.isEmpty()) {
                              unlockAllJutsu(itemStack);
                              giveItem(player, itemStack);
                           }

                           if (targetEntry.hasBundleItems()) {
                              for(CrateLootEntry.BundleItem bi : targetEntry.getBundleItems()) {
                                 ItemStack biStack = bi.toItemStack();
                                 if (biStack.isEmpty()) {
                                    System.out.println("[TokenShop] WARNING: Failed to resolve bundle item '" + bi.getItemId() + "' for player " + player.getName() + " — item not granted!");
                                 }

                                 unlockAllJutsu(biStack);
                                 giveItem(player, biStack);
                              }
                           }

                           if (targetEntry.hasAdvancement()) {
                              grantAdvancement(player, targetEntry.getAdvancementId());
                              grantLegacyClansAddonAdvancements(player, targetEntry.getAdvancementId());
                           }

                           if (targetEntry.hasDisplayNameOverride()) {
                              data.addOwnedItem(playerId, targetEntry.getDisplayNameOverride());
                           } else {
                              data.addOwnedItem(playerId, targetEntry.getItemId() + ":" + targetEntry.getItemMeta());
                           }

                           String displayName = itemStack.isEmpty() ? "Unknown" : itemStack.getDisplayName();
                           ShopHistoryEntry historyEntry = new ShopHistoryEntry("token_shop", targetEntry.getItemId(), targetEntry.getItemMeta(), targetEntry.getItemCount(), displayName, targetEntry.getRarity().id, System.currentTimeMillis());
                           data.addHistoryEntry(playerId, historyEntry);
                           ShopNetworkHelper.sendShopSync(player);
                        }
                     }
                  }
               }
            }
         }
      }

      static int getTokenCost(ItemRarity rarity, ShopCategory category) {
         if (rarity != ItemRarity.S_PLUS_RANK && rarity != ItemRarity.OTSUTSUKI_RANK) {
            if (rarity != ItemRarity.S_RANK || category != ShopCategory.CLANS && category != ShopCategory.KEKKEI_GENKAI) {
               int baseCost;
               switch (rarity) {
                  case D_RANK:
                     baseCost = 10;
                     break;
                  case C_RANK:
                     baseCost = 30;
                     break;
                  case B_RANK:
                     baseCost = 80;
                     break;
                  case A_RANK:
                     baseCost = 200;
                     break;
                  case S_RANK:
                     baseCost = 500;
                     break;
                  default:
                     return 0;
               }

               int mult10;
               switch (category) {
                  case JUTSU:
                     mult10 = 20;
                     break;
                  case KEKKEI_GENKAI:
                     mult10 = 40;
                     break;
                  case CLANS:
                     mult10 = 50;
                     break;
                  default:
                     mult10 = 5;
               }

               return Math.max(1, baseCost * mult10 / 10);
            } else {
               return 0;
            }
         } else {
            return 0;
         }
      }

      private static ShopCrateResultMessage.StripEntry entryToStripEntry(CrateLootEntry entry, double totalWeight) {
         return new ShopCrateResultMessage.StripEntry(entry.getItemId(), entry.getItemMeta(), entry.getItemCount(), (byte)entry.getRarity().id, entry.getWeight(), entry.getDropChance(totalWeight));
      }

      private static void unlockAllJutsu(ItemStack stack) {
         if (!stack.isEmpty() && stack.getItem() instanceof ItemJutsu.Base) {
            ItemJutsu.Base jutsuItem = (ItemJutsu.Base)stack.getItem();
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) {
               tag = new NBTTagCompound();
               stack.setTagCompound(tag);
            }

            int count = jutsuItem.getAllJutsus(stack).size();

            for(int i = 0; i < count; ++i) {
               String key = "JutsuCDMapKey" + i;
               if (!tag.hasKey(key) || tag.getLong(key) < 0L) {
                  tag.setLong(key, 0L);
               }
            }

         }
      }

      private static void giveItem(EntityPlayerMP player, ItemStack stack) {
         if (!stack.isEmpty()) {
            ItemStack copy = stack.copy();
            if (!player.inventory.addItemStackToInventory(copy)) {
               int remaining = copy.getCount();
               if (remaining <= 0) {
                  System.out.println("[RyoShop] WARNING: addItemStackToInventory returned false but count=0 for " + stack.getItem().getRegistryName() + " — player: " + player.getName());
                  return;
               }

               ShopSavedData data = ShopSavedData.get(player.world);
               NBTTagCompound itemNbt = new NBTTagCompound();
               itemNbt.setString("itemId", stack.getItem().getRegistryName().toString());
               itemNbt.setInteger("meta", stack.getMetadata());
               itemNbt.setInteger("count", remaining);
               if (stack.hasTagCompound()) {
                  itemNbt.setTag("itemNbt", stack.getTagCompound().copy());
               }

               data.addOverflowItem(player.getUniqueID(), itemNbt);
               System.out.println("[RyoShop] Stored " + remaining + "x " + stack.getItem().getRegistryName() + " in overflow bank for " + player.getName());
            }

         }
      }

      private static void handleClaimOverflow(EntityPlayerMP player) {
         ShopSavedData data = ShopSavedData.get(player.world);
         List<NBTTagCompound> overflowItems = data.claimAllOverflow(player.getUniqueID());
         if (!overflowItems.isEmpty()) {
            int claimed = 0;
            int failed = 0;

            for(NBTTagCompound itemNbt : overflowItems) {
               ItemStack stack = createItemStack(itemNbt.getString("itemId"), itemNbt.getInteger("meta"), itemNbt.getInteger("count"), itemNbt.hasKey("itemNbt") ? itemNbt.getCompoundTag("itemNbt") : null);
               if (!stack.isEmpty()) {
                  int countBefore = stack.getCount();
                  if (player.inventory.addItemStackToInventory(stack)) {
                     ++claimed;
                  } else {
                     int remaining = stack.getCount();
                     if (remaining < countBefore) {
                        ++claimed;
                        NBTTagCompound reducedNbt = itemNbt.copy();
                        reducedNbt.setInteger("count", remaining);
                        data.addOverflowItem(player.getUniqueID(), reducedNbt);
                     } else {
                        data.addOverflowItem(player.getUniqueID(), itemNbt);
                     }

                     ++failed;
                  }
               }
            }

            if (failed > 0 && claimed == 0) {
               player.sendMessage(new TextComponentString("§c[Bank] Your inventory is full! Make room and try again."));
            } else if (failed > 0) {
               int remaining = data.getOverflowCount(player.getUniqueID());
               player.sendMessage(new TextComponentString("§e[Bank] Claimed some items. " + remaining + " items still in bank (inventory full)."));
            } else if (claimed > 0) {
               player.sendMessage(new TextComponentString("§a[Bank] Claimed " + claimed + " item(s) from bank!"));
            }

            ShopNetworkHelper.sendShopSync(player);
         }
      }

      private static void handleTravelWarp(EntityPlayerMP player, String villageId) {
         if (villageId != null && !villageId.isEmpty()) {
            boolean valid = false;

            for(String v : VALID_VILLAGES) {
               if (v.equals(villageId)) {
                  valid = true;
                  break;
               }
            }

            if (valid) {
               UUID playerId = player.getUniqueID();
               ShopSavedData data = ShopSavedData.get(player.world);
               long balance = data.getBalance(playerId);
               if (balance < 500L) {
                  player.sendMessage(new TextComponentString("§c[Travel] Not enough ryo! Need ¥500."));
               } else {
                  long now = System.currentTimeMillis();
                  long cooldownEnd = player.getEntityData().getLong("travel_warp_cooldown");
                  if (now < cooldownEnd) {
                     long remainMs = cooldownEnd - now;
                     int remainSec = (int)(remainMs / 1000L);
                     int min = remainSec / 60;
                     int sec = remainSec % 60;
                     player.sendMessage(new TextComponentString("§c[Travel] Cooldown: " + min + ":" + String.format("%02d", sec) + " remaining."));
                  } else if (data.removeBalance(playerId, 500L)) {
                     player.getEntityData().setLong("travel_warp_cooldown", now + 300000L);
                     player.getEntityData().setLong("travel_pvp_protect", player.world.getTotalWorldTime() + 300L);
                     MinecraftServer server = player.getServer();
                     if (server != null) {
                        String playerName = player.getName();
                        server.commandManager.executeCommand(server, "warp " + villageId + " " + playerName);
                     }

                     String villageName = villageId.substring(0, 1).toUpperCase() + villageId.substring(1);
                     player.sendMessage(new TextComponentString("§a[Travel] §fWarped to " + villageName + " Village! PvP immunity for 15 seconds."));
                     player.closeScreen();
                     ShopNetworkHelper.sendShopSync(player);
                  }
               }
            }
         }
      }

      private static boolean isCategoryUnlocked(EntityPlayerMP player, ShopCategory category) {
         if (category == ShopCategory.CLANS) {
            return hasAdvancement(player, "inftsukaddon:arc3_complete") || QuestManager.getInstance().hasCompletedQuestForAdvancement(player.getUniqueID(), "inftsukaddon:arc3_complete");
         } else if (category != ShopCategory.KEKKEI_GENKAI) {
            return true;
         } else {
            return hasAdvancement(player, "inftsukaddon:shinobi_way") || QuestManager.getInstance().hasCompletedQuestForAdvancement(player.getUniqueID(), "inftsukaddon:shinobi_way");
         }
      }

      private static double getDupeRefundRate(ItemRarity rarity) {
         switch (rarity) {
            case C_RANK:
               return 0.1;
            case B_RANK:
               return 0.2;
            case A_RANK:
               return 0.3;
            case S_RANK:
               return 0.4;
            case S_PLUS_RANK:
               return (double)0.5F;
            default:
               return 0.1;
         }
      }

      private static void handleRedeemAkamichi(EntityPlayerMP player) {
         if (!hasAdvancement(player, "clansaddon:clans/akamichi_unlocked")) {
            player.sendMessage(new TextComponentString("§c[Redeem] You must unlock the Akamichi Clan first."));
         } else {
            Item pill = Item.getByNameOrId("kabutoaddon:red_chakra_pill");
            if (pill != null) {
               ItemStack pillStack = new ItemStack(pill, 4);
               if (!player.inventory.addItemStackToInventory(pillStack)) {
                  player.entityDropItem(pillStack, 0.5F);
               }
            }

            player.sendMessage(new TextComponentString("§a[Redeem] Claimed 4 Red Chakra Pills!"));
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

      private static void grantAdvancement(EntityPlayerMP player, String advancementId) {
         if (advancementId != null && !advancementId.isEmpty()) {
            try {
               ResourceLocation loc = new ResourceLocation(advancementId);
               Advancement advancement = player.getServer().getAdvancementManager().getAdvancement(loc);
               if (advancement != null) {
                  AdvancementProgress progress = player.getAdvancements().getProgress(advancement);

                  for(String criterion : progress.getRemaningCriteria()) {
                     player.getAdvancements().grantCriterion(advancement, criterion);
                  }
               }
            } catch (Exception var7) {
            }

         }
      }

      private static void grantLegacyClansAddonAdvancements(EntityPlayerMP player, String newAdvId) {
         if (newAdvId != null) {
            if (newAdvId.contains("uchiha_unlocked")) {
               grantAdvancement(player, "clansaddon:uchiha/root");
            }

            if (newAdvId.contains("rinnegan_unlocked")) {
               grantAdvancement(player, "clansaddon:otsutsuki/root");
            }

            if (newAdvId.contains("TCM_unlocked")) {
               grantAdvancement(player, "clansaddon:so6p_tab/tenseigan_unlocked");
               grantAdvancement(player, "clansaddon:otsutsuki/root");
            }

         }
      }

      private static class PendingCrateResult {
         final List<CrateLootEntry> winners;
         final String crateId;
         final long timestamp;

         PendingCrateResult(CrateLootEntry winner, String crateId) {
            this.winners = new ArrayList(1);
            this.winners.add(winner);
            this.crateId = crateId;
            this.timestamp = System.currentTimeMillis();
         }

         PendingCrateResult(List<CrateLootEntry> winners, String crateId) {
            this.winners = new ArrayList(winners);
            this.crateId = crateId;
            this.timestamp = System.currentTimeMillis();
         }
      }
   }
}
