
package net.luck.narutoaddon.OtherCode.shop.pass;

import net.luck.narutoaddon.OtherCode.jutsu.ItemSeasonalRelease;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateDefinition;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateLootEntry;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateRegistry;
import net.luck.narutoaddon.OtherCode.shop.network.ShopCrateResultMessage;
import net.luck.narutoaddon.OtherCode.shop.network.ShopNetworkHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class BattlePassManager {
   private static BattlePassManager INSTANCE;
   public static final int KIRIN_SEASON = 2;
   public static final int SHADOW_JUTSU_SEASON = 3;

   private BattlePassManager() {
   }

   public static BattlePassManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new BattlePassManager();
      }

      return INSTANCE;
   }

   public BattlePassSeason getCurrentSeason(World world) {
      ShopSavedData data = ShopSavedData.get(world);
      int seasonNum = data.getBpSeasonNumber();
      long startTime = data.getBpSeasonStartTime();
      long now = System.currentTimeMillis();
      long correctStart = BattlePassSeason.getAlignedStartTime(now);
      if (seasonNum > 0 && startTime > 0L) {
         if (Math.abs(startTime - correctStart) > 1209600000L) {
            startTime = correctStart;
            data.setBpSeasonStartTime(correctStart);
            System.out.println("[BattlePass] Auto-corrected season start time (was stale, now: " + correctStart + ")");
         }
      } else {
         startTime = correctStart;
         data.setBpSeasonNumber(3);
         data.setBpSeasonStartTime(correctStart);
         seasonNum = 3;
         System.out.println("[BattlePass] Auto-initialized Season 3 (start: " + correctStart + ")");
      }

      return new BattlePassSeason(seasonNum, BattlePassSeason.getSeasonDisplayName(seasonNum), startTime);
   }

   public int awardXP(UUID playerId, int amount, String source, World world) {
      if (amount <= 0) {
         return 0;
      } else {
         ShopSavedData data = ShopSavedData.get(world);
         BattlePassSeason season = this.getCurrentSeason(world);
         long now = System.currentTimeMillis();
         if (!season.canEarnXP(now)) {
            return 0;
         } else {
            this.checkDailyCapReset(data);
            int dailyCap = BattlePassDefinition.getDailyCap(source);
            int actualAmount = amount;
            if (dailyCap >= 0) {
               int usedToday = data.getBpDailyXPForSource(playerId, source);
               int remaining = dailyCap - usedToday;
               if (remaining <= 0) {
                  return 0;
               }

               actualAmount = Math.min(amount, remaining);
               data.addBpDailyXP(playerId, source, actualAmount);
            }

            int purchasedTier = data.getBpPurchasedTier(playerId);
            if (purchasedTier == 2) {
               actualAmount = (int)((double)actualAmount * (double)1.5F);
            }

            int currentLevel = data.getBpCurrentLevel(playerId);
            int currentXP = data.getBpCurrentXP(playerId);
            if (currentLevel >= 20) {
               return 0;
            } else {
               for(currentXP += actualAmount; currentLevel < 20; ++currentLevel) {
                  int xpNeeded = BattlePassDefinition.getXPForLevel(currentLevel + 1);
                  if (currentXP < xpNeeded) {
                     break;
                  }

                  currentXP -= xpNeeded;
               }

               if (currentLevel >= 20) {
                  currentXP = 0;
               }

               data.setBpCurrentLevel(playerId, currentLevel);
               data.setBpCurrentXP(playerId, currentXP);
               if (actualAmount > 0 && world instanceof WorldServer) {
                  EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(playerId);
                  if (player != null) {
                     ShopNetworkHelper.sendBattlePassSync(player);
                  }
               }

               return actualAmount;
            }
         }
      }
   }

   public boolean purchasePass(UUID playerId, int tier, World world) {
      if (tier >= 1 && tier <= 2) {
         ShopSavedData data = ShopSavedData.get(world);
         int currentTier = data.getBpPurchasedTier(playerId);
         if (currentTier >= tier) {
            return false;
         } else {
            int cost;
            if (currentTier == 1 && tier == 2) {
               cost = 200;
            } else {
               cost = tier == 1 ? 300 : 500;
            }

            if (!data.hasCrystals(playerId, cost)) {
               return false;
            } else if (!data.removeCrystals(playerId, cost)) {
               return false;
            } else {
               data.recordCrystalSpent(playerId, cost);
               data.setBpPurchasedTier(playerId, tier);
               int seasonNum = data.getBpSeasonNumber();
               int retroLevel = data.getBpCurrentLevel(playerId);

               for(int t = 1; t <= retroLevel; ++t) {
                  if (data.hasBpClaimedTier(playerId, t)) {
                     String grantedKey = "bp_tier_" + t + "_premium_granted";
                     if (!data.hasOwnedItem(playerId, grantedKey)) {
                        BattlePassDefinition.TierReward premR = BattlePassDefinition.getPremiumTierReward(t, seasonNum);
                        if (premR != null && !premR.isEmpty()) {
                           this.grantReward(playerId, premR, world);
                        }

                        if (t == 15) {
                           BattlePassSeason season = this.getCurrentSeason(world);
                           String effectId = season.getKillEffectId();
                           data.addOwnedKillEffect(playerId, effectId);
                           if (data.getActiveKillEffect(playerId) == null || data.getActiveKillEffect(playerId).isEmpty()) {
                              data.setActiveKillEffect(playerId, effectId);
                           }
                        }

                        if (t == 20) {
                           String jutsuKey = "bp_tier_20_jutsu_granted";
                           if (!data.hasOwnedItem(playerId, jutsuKey)) {
                              this.grantOrUnlockSeasonalJutsu(playerId, seasonNum, world);
                              data.addOwnedItem(playerId, jutsuKey);
                           }
                        }

                        data.addOwnedItem(playerId, grantedKey);
                     }

                     data.addOwnedItem(playerId, "bp_tier_" + t + "_granted");
                     data.addOwnedItem(playerId, "bp_tier_" + t + "_xp_granted");
                  }
               }

               System.out.println("[BattlePass] Retroactively granted premium rewards for " + retroLevel + " claimed tiers.");
               if (tier == 2 && currentTier < 2) {
                  int currentLevel = data.getBpCurrentLevel(playerId);
                  int skipsToApply = Math.min(5, 20 - currentLevel);
                  if (skipsToApply > 0) {
                     data.setBpCurrentLevel(playerId, currentLevel + skipsToApply);
                     data.setBpCurrentXP(playerId, 0);
                     data.setBpOtsutsukiSkipsUsed(playerId, skipsToApply);
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   public boolean skipLevel(UUID playerId, World world) {
      ShopSavedData data = ShopSavedData.get(world);
      int purchasedTier = data.getBpPurchasedTier(playerId);
      if (purchasedTier <= 0) {
         return false;
      } else {
         int currentLevel = data.getBpCurrentLevel(playerId);
         if (currentLevel >= 20) {
            return false;
         } else {
            if (purchasedTier == 2) {
               int skipsUsed = data.getBpOtsutsukiSkipsUsed(playerId);
               if (skipsUsed < 5) {
                  data.setBpCurrentLevel(playerId, currentLevel + 1);
                  data.setBpCurrentXP(playerId, 0);
                  data.setBpOtsutsukiSkipsUsed(playerId, skipsUsed + 1);
                  return true;
               }
            }

            int cost = BattlePassDefinition.getSkipCost(currentLevel);
            if (cost <= 0) {
               return false;
            } else if (!data.hasCrystals(playerId, cost)) {
               return false;
            } else if (!data.removeCrystals(playerId, cost)) {
               return false;
            } else {
               data.recordCrystalSpent(playerId, cost);
               data.setBpCurrentLevel(playerId, currentLevel + 1);
               data.setBpCurrentXP(playerId, 0);
               return true;
            }
         }
      }
   }

   public boolean claimTier(UUID playerId, int tier, World world) {
      if (tier >= 1 && tier <= 20) {
         ShopSavedData data = ShopSavedData.get(world);
         int currentLevel = data.getBpCurrentLevel(playerId);
         if (currentLevel < tier) {
            return false;
         } else if (data.hasBpClaimedTier(playerId, tier)) {
            return false;
         } else {
            int purchasedTier = data.getBpPurchasedTier(playerId);
            BattlePassDefinition.TierReward freeReward = BattlePassDefinition.getFreeTierReward(tier);
            boolean freeOk = this.grantReward(playerId, freeReward, world);
            boolean premOk = true;
            if (purchasedTier > 0) {
               int seasonNum = data.getBpSeasonNumber();
               BattlePassDefinition.TierReward premiumReward = BattlePassDefinition.getPremiumTierReward(tier, seasonNum);
               premOk = this.grantReward(playerId, premiumReward, world);
            }

            boolean killEffectOk = true;
            if (tier == 15 && purchasedTier > 0) {
               BattlePassSeason season = this.getCurrentSeason(world);
               String effectId = season.getKillEffectId();
               data.addOwnedKillEffect(playerId, effectId);
               if (data.getActiveKillEffect(playerId) == null || data.getActiveKillEffect(playerId).isEmpty()) {
                  data.setActiveKillEffect(playerId, effectId);
               }

               System.out.println("[BattlePass] Granted kill effect '" + effectId + "' to " + playerId);
            }

            boolean jutsuOk = true;
            if (tier == 20 && purchasedTier > 0) {
               BattlePassSeason season = this.getCurrentSeason(world);
               jutsuOk = this.grantOrUnlockSeasonalJutsu(playerId, season.getSeasonNumber(), world);
               if (!jutsuOk) {
                  System.out.println("[BattlePass] ERROR: Failed to grant/unlock seasonal jutsu for " + playerId);
               }
            }

            if (freeOk && premOk && killEffectOk && jutsuOk) {
               data.addBpClaimedTier(playerId, tier);
               data.addOwnedItem(playerId, "bp_tier_" + tier + "_crate_granted");
               data.addOwnedItem(playerId, "bp_tier_" + tier + "_xp_granted");
               data.addOwnedItem(playerId, "bp_tier_" + tier + "_item_granted");
               if (tier == 15) {
                  data.addOwnedItem(playerId, "bp_tier_15_effect_granted");
               }

               if (tier == 20) {
                  data.addOwnedItem(playerId, "bp_tier_20_jutsu_granted");
               }

               System.out.println("[BattlePass] Tier " + tier + " claimed successfully by " + playerId);
            } else {
               System.out.println("[BattlePass] ERROR: Tier " + tier + " claim INCOMPLETE for " + playerId + " (free=" + freeOk + ", prem=" + premOk + ", killEffect=" + killEffectOk + ", jutsu=" + jutsuOk + ") — tier NOT marked as claimed, player can retry");
            }

            EntityPlayerMP claimPlayer = world.getMinecraftServer().getPlayerList().getPlayerByUUID(playerId);
            if (claimPlayer != null) {
               ShopNetworkHelper.sendBattlePassSync(claimPlayer);
               ShopNetworkHelper.sendShopSync(claimPlayer);
            }

            return freeOk && premOk && killEffectOk && jutsuOk;
         }
      } else {
         return false;
      }
   }

   public boolean grantRewardPublic(UUID playerId, BattlePassDefinition.TierReward reward, World world) {
      return this.grantReward(playerId, reward, world);
   }

   private boolean grantReward(UUID playerId, BattlePassDefinition.TierReward reward, World world) {
      if (reward != null && !reward.isEmpty()) {
         ShopSavedData data = ShopSavedData.get(world);
         boolean allOk = true;
         if (reward.ryo > 0) {
            data.addBalance(playerId, (long)reward.ryo);
            System.out.println("[BattlePass] Granted " + reward.ryo + " ryo to " + playerId);
         }

         if (reward.crystals > 0) {
            data.addCrystals(playerId, reward.crystals);
            System.out.println("[BattlePass] Granted " + reward.crystals + " CC to " + playerId);
         }

         if (reward.jutsuXP > 0) {
            EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(playerId);
            if (player != null) {
               MinecraftServer srv = world.getMinecraftServer();
               if (srv != null) {
                  String cmd = "runasop " + player.getName() + " addninjaxp " + player.getName() + " " + reward.jutsuXP;
                  srv.getCommandManager().executeCommand(srv, cmd);
                  System.out.println("[BattlePass] Granted " + reward.jutsuXP + " jutsu XP to " + player.getName());
               }
            } else {
               System.out.println("[BattlePass] ERROR: Player not found for jutsu XP grant, UUID: " + playerId);
            }
         }

         if (reward.hasItem()) {
            Item rewardItem = Item.getByNameOrId(reward.itemId);
            if (rewardItem != null) {
               EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(playerId);
               if (player != null) {
                  ItemStack stack = new ItemStack(rewardItem, reward.itemCount, reward.itemMeta);
                  if (!player.inventory.addItemStackToInventory(stack)) {
                     NBTTagCompound overflowNbt = new NBTTagCompound();
                     overflowNbt.setString("itemId", reward.itemId);
                     overflowNbt.setInteger("meta", reward.itemMeta);
                     overflowNbt.setInteger("count", reward.itemCount);
                     data.addOverflowItem(playerId, overflowNbt);
                  }

                  player.sendMessage(new TextComponentString("§6[Battle Pass] §7Received " + reward.itemCount + "x " + rewardItem.getItemStackDisplayName(new ItemStack(rewardItem, 1, reward.itemMeta))));
                  System.out.println("[BattlePass] Granted " + reward.itemCount + "x " + reward.itemId + " to " + playerId);
               } else {
                  NBTTagCompound overflowNbt = new NBTTagCompound();
                  overflowNbt.setString("itemId", reward.itemId);
                  overflowNbt.setInteger("meta", reward.itemMeta);
                  overflowNbt.setInteger("count", reward.itemCount);
                  data.addOverflowItem(playerId, overflowNbt);
                  System.out.println("[BattlePass] Player offline, added " + reward.itemCount + "x " + reward.itemId + " to overflow for " + playerId);
               }
            } else {
               System.out.println("[BattlePass] ERROR: Item not found: " + reward.itemId + " for player " + playerId);
               allOk = false;
            }
         }

         if (reward.hasCrate() && !this.grantCrateReward(playerId, reward.crateId, reward.crateCount, world)) {
            allOk = false;
         }

         if (reward.hasCrate2() && !this.grantCrateReward(playerId, reward.crateId2, reward.crateCount2, world)) {
            allOk = false;
         }

         return allOk;
      } else {
         return true;
      }
   }

   private boolean grantCrateReward(UUID playerId, String crateId, int count, World world) {
      System.out.println("[BattlePass] Granting crate " + crateId + " x" + count + " to " + playerId);
      CrateRegistry.init();
      CrateDefinition crate = CrateRegistry.getById(crateId);
      if (crate == null) {
         System.out.println("[BattlePass] ERROR: Crate not found: " + crateId + " for player " + playerId);
         return false;
      } else {
         EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(playerId);
         if (player == null) {
            ShopSavedData data = ShopSavedData.get(world);
            Random rng = new Random();

            for(int i = 0; i < count; ++i) {
               CrateLootEntry winner = crate.rollReward(rng);
               if (winner != null) {
                  NBTTagCompound overflowNbt = new NBTTagCompound();
                  overflowNbt.setString("itemId", winner.getItemId());
                  overflowNbt.setInteger("meta", winner.getItemMeta());
                  overflowNbt.setInteger("count", winner.getItemCount());
                  if (winner.getItemNbt() != null) {
                     overflowNbt.setTag("itemNbt", winner.getItemNbt());
                  }

                  data.addOverflowItem(playerId, overflowNbt);
                  if (winner.hasBundleItems()) {
                     for(CrateLootEntry.BundleItem bi : winner.getBundleItems()) {
                        NBTTagCompound biOverflow = new NBTTagCompound();
                        biOverflow.setString("itemId", bi.getItemId());
                        biOverflow.setInteger("meta", bi.getItemMeta());
                        biOverflow.setInteger("count", bi.getItemCount());
                        data.addOverflowItem(playerId, biOverflow);
                     }
                  }
               }
            }

            System.out.println("[BattlePass] Offline crate grant: " + count + "x " + crateId + " to overflow for " + playerId);
            return true;
         } else {
            System.out.println("[BattlePass] Granting " + count + "x crate '" + crateId + "' to " + player.getName());
            Random rng = new Random();

            for(int i = 0; i < count; ++i) {
               CrateLootEntry winner = crate.rollReward(rng);
               if (winner != null) {
                  ItemStack stack = winner.toItemStack();
                  if (!stack.isEmpty() && !player.inventory.addItemStackToInventory(stack)) {
                     ShopSavedData data = ShopSavedData.get(world);
                     NBTTagCompound overflowNbt = new NBTTagCompound();
                     overflowNbt.setString("itemId", winner.getItemId());
                     overflowNbt.setInteger("meta", winner.getItemMeta());
                     overflowNbt.setInteger("count", winner.getItemCount());
                     if (winner.getItemNbt() != null) {
                        overflowNbt.setTag("itemNbt", winner.getItemNbt());
                     }

                     data.addOverflowItem(playerId, overflowNbt);
                  }

                  if (winner.hasBundleItems()) {
                     for(CrateLootEntry.BundleItem bi : winner.getBundleItems()) {
                        ItemStack biStack = bi.toItemStack();
                        if (!biStack.isEmpty() && !player.inventory.addItemStackToInventory(biStack)) {
                           ShopSavedData data = ShopSavedData.get(world);
                           NBTTagCompound biOverflow = new NBTTagCompound();
                           biOverflow.setString("itemId", bi.getItemId());
                           biOverflow.setInteger("meta", bi.getItemMeta());
                           biOverflow.setInteger("count", bi.getItemCount());
                           data.addOverflowItem(playerId, biOverflow);
                        }
                     }
                  }

                  int STRIP_SIZE = 60;
                  int TARGET_POS = 45;
                  List<ShopCrateResultMessage.StripEntry> strip = new ArrayList(STRIP_SIZE);
                  double totalWeight = crate.getTotalWeight();

                  for(int s = 0; s < STRIP_SIZE; ++s) {
                     if (s == TARGET_POS) {
                        strip.add(makeStripEntry(winner, totalWeight));
                     } else {
                        CrateLootEntry visual = crate.rollReward(rng);
                        strip.add(makeStripEntry(visual, totalWeight));
                     }
                  }

                  String bundleDisplayName = winner.hasDisplayNameOverride() ? winner.getDisplayNameOverride() : null;
                  List<ShopCrateResultMessage.BundleResultItem> bundleResultItems = new ArrayList();
                  if (winner.hasBundleItems()) {
                     for(CrateLootEntry.BundleItem bi : winner.getBundleItems()) {
                        bundleResultItems.add(new ShopCrateResultMessage.BundleResultItem(bi.getItemId(), bi.getItemMeta(), bi.getItemCount()));
                     }
                  }

                  String wonDisplayName = winner.getDisplayName();
                  ShopCrateResultMessage resultMsg = new ShopCrateResultMessage(crateId, 0L, winner.getItemId(), winner.getItemMeta(), winner.getItemCount(), (byte)winner.getRarity().id, wonDisplayName, TARGET_POS, strip, false, 0, 0, bundleDisplayName, bundleResultItems);
                  ShopNetworkHelper.sendCrateResult(player, resultMsg);
                  player.sendMessage(new TextComponentString("§6[Battle Pass] §7Rolled " + crate.getDisplayName() + ": §e" + winner.getDisplayName()));
               }
            }

            return true;
         }
      }
   }

   private static ShopCrateResultMessage.StripEntry makeStripEntry(CrateLootEntry entry, double totalWeight) {
      return new ShopCrateResultMessage.StripEntry(entry.getItemId(), entry.getItemMeta(), entry.getItemCount(), (byte)entry.getRarity().id, entry.getWeight(), entry.getDropChance(totalWeight));
   }

   public static boolean hasSeasonalReleaseItem(EntityPlayerMP player) {
      Item seasonalItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("inftsukaddon", "seasonal_release"));
      if (seasonalItem == null) {
         return false;
      } else {
         for(int i = 0; i < player.inventory.getSizeInventory(); ++i) {
            ItemStack s = player.inventory.getStackInSlot(i);
            if (!s.isEmpty() && s.getItem() == seasonalItem) {
               return true;
            }
         }

         InventoryEnderChest ec = player.getInventoryEnderChest();

         for(int i = 0; i < ec.getSizeInventory(); ++i) {
            ItemStack s = ec.getStackInSlot(i);
            if (!s.isEmpty() && s.getItem() == seasonalItem) {
               return true;
            }
         }

         return false;
      }
   }

   private static NBTTagCompound buildSeasonalReleaseNbt(int seasonNumber) {
      NBTTagCompound nbt = new NBTTagCompound();
      int bdIndex = ItemSeasonalRelease.BLAZING_DECIMATION.index;
      int kirinIndex = ItemSeasonalRelease.KIRIN.index;
      int shadowIndex = ItemSeasonalRelease.SHADOW_REND.index;
      boolean bdUnlocked;
      boolean kirinUnlocked;
      boolean shadowUnlocked;
      int activeIndex;
      switch (seasonNumber) {
         case 1:
            bdUnlocked = true;
            kirinUnlocked = false;
            shadowUnlocked = false;
            activeIndex = bdIndex;
            break;
         case 2:
            bdUnlocked = false;
            kirinUnlocked = true;
            shadowUnlocked = false;
            activeIndex = kirinIndex;
            break;
         case 3:
            bdUnlocked = false;
            kirinUnlocked = false;
            shadowUnlocked = true;
            activeIndex = shadowIndex;
            break;
         default:
            bdUnlocked = true;
            kirinUnlocked = false;
            shadowUnlocked = false;
            activeIndex = bdIndex;
      }

      nbt.setLong("JutsuCDMapKey" + bdIndex, bdUnlocked ? 0L : -1L);
      nbt.setLong("JutsuCDMapKey" + kirinIndex, kirinUnlocked ? 0L : -1L);
      nbt.setLong("JutsuCDMapKey" + shadowIndex, shadowUnlocked ? 0L : -1L);
      nbt.setInteger("JutsuIndexKey", activeIndex);
      return nbt;
   }

   public boolean grantOrUnlockSeasonalJutsu(UUID playerId, int seasonNumber, World world) {
      Item seasonalItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("inftsukaddon", "seasonal_release"));
      if (seasonalItem == null) {
         return false;
      } else {
         ShopSavedData data = ShopSavedData.get(world);
         EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(playerId);
         if (seasonNumber == 2) {
            data.addOwnedItem(playerId, "bp_kirin_eligible");
         }

         if (seasonNumber == 3) {
            data.addOwnedItem(playerId, "bp_season_3_jutsu_eligible");
         }

         if (player != null && hasSeasonalReleaseItem(player)) {
            if (seasonNumber == 2) {
               unlockKirinOnAllItems(player);
            }

            if (seasonNumber == 3) {
               unlockShadowRendOnAllItems(player);
            }

            System.out.println("[BattlePass] Player " + player.getName() + " already has seasonal_release — unlocked jutsu on existing item(s) instead of duplicating");
            return true;
         } else {
            ItemStack stack = new ItemStack(seasonalItem, 1, 0);
            stack.setTagCompound(buildSeasonalReleaseNbt(seasonNumber));
            if (player != null) {
               if (!player.inventory.addItemStackToInventory(stack)) {
                  NBTTagCompound ov = new NBTTagCompound();
                  ov.setString("itemId", "inftsukaddon:seasonal_release");
                  ov.setInteger("meta", 0);
                  ov.setInteger("count", 1);
                  ov.setTag("itemNbt", buildSeasonalReleaseNbt(seasonNumber));
                  data.addOverflowItem(playerId, ov);
               }

               player.sendMessage(new TextComponentString("§6§l[BATTLE PASS] §eYou unlocked the exclusive Season Jutsu!"));
            } else {
               NBTTagCompound ov = new NBTTagCompound();
               ov.setString("itemId", "inftsukaddon:seasonal_release");
               ov.setInteger("meta", 0);
               ov.setInteger("count", 1);
               ov.setTag("itemNbt", buildSeasonalReleaseNbt(seasonNumber));
               data.addOverflowItem(playerId, ov);
            }

            System.out.println("[BattlePass] Granted seasonal_release (season " + seasonNumber + " jutsu only) to " + playerId);
            return true;
         }
      }
   }

   public static int unlockKirinOnAllItems(EntityPlayerMP player) {
      Item seasonalItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("inftsukaddon", "seasonal_release"));
      if (seasonalItem == null) {
         return 0;
      } else {
         int kirinIndex = ItemSeasonalRelease.KIRIN.index;
         String nbtKey = "JutsuCDMapKey" + kirinIndex;
         int unlocked = 0;

         for(int i = 0; i < player.inventory.getSizeInventory(); ++i) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == seasonalItem) {
               NBTTagCompound nbt = stack.getTagCompound();
               if (nbt == null) {
                  nbt = new NBTTagCompound();
                  stack.setTagCompound(nbt);
               }

               if (!nbt.hasKey(nbtKey) || nbt.getLong(nbtKey) < 0L) {
                  nbt.setLong(nbtKey, 0L);
                  nbt.setInteger("JutsuIndexKey", kirinIndex);
                  ++unlocked;
               }
            }
         }

         InventoryEnderChest enderChest = player.getInventoryEnderChest();

         for(int i = 0; i < enderChest.getSizeInventory(); ++i) {
            ItemStack stack = enderChest.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == seasonalItem) {
               NBTTagCompound nbt = stack.getTagCompound();
               if (nbt == null) {
                  nbt = new NBTTagCompound();
                  stack.setTagCompound(nbt);
               }

               if (!nbt.hasKey(nbtKey) || nbt.getLong(nbtKey) < 0L) {
                  nbt.setLong(nbtKey, 0L);
                  nbt.setInteger("JutsuIndexKey", kirinIndex);
                  ++unlocked;
               }
            }
         }

         if (unlocked > 0) {
            player.inventoryContainer.detectAndSendChanges();
         }

         return unlocked;
      }
   }

   public static int unlockShadowRendOnAllItems(EntityPlayerMP player) {
      Item seasonalItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("inftsukaddon", "seasonal_release"));
      if (seasonalItem == null) {
         return 0;
      } else {
         int shadowIndex = ItemSeasonalRelease.SHADOW_REND.index;
         String nbtKey = "JutsuCDMapKey" + shadowIndex;
         int unlocked = 0;

         for(int i = 0; i < player.inventory.getSizeInventory(); ++i) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == seasonalItem) {
               NBTTagCompound nbt = stack.getTagCompound();
               if (nbt == null) {
                  nbt = new NBTTagCompound();
                  stack.setTagCompound(nbt);
               }

               if (!nbt.hasKey(nbtKey) || nbt.getLong(nbtKey) < 0L) {
                  nbt.setLong(nbtKey, 0L);
                  nbt.setInteger("JutsuIndexKey", shadowIndex);
                  ++unlocked;
               }
            }
         }

         InventoryEnderChest enderChest = player.getInventoryEnderChest();

         for(int i = 0; i < enderChest.getSizeInventory(); ++i) {
            ItemStack stack = enderChest.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == seasonalItem) {
               NBTTagCompound nbt = stack.getTagCompound();
               if (nbt == null) {
                  nbt = new NBTTagCompound();
                  stack.setTagCompound(nbt);
               }

               if (!nbt.hasKey(nbtKey) || nbt.getLong(nbtKey) < 0L) {
                  nbt.setLong(nbtKey, 0L);
                  nbt.setInteger("JutsuIndexKey", shadowIndex);
                  ++unlocked;
               }
            }
         }

         if (unlocked > 0) {
            player.inventoryContainer.detectAndSendChanges();
         }

         return unlocked;
      }
   }

   public void queueAutoClaimForReset(World world) {
      ShopSavedData data = ShopSavedData.get(world);
      int currentSeason = data.getBpSeasonNumber();
      if (currentSeason > 0) {
         int queued = 0;
         int kirinMarked = 0;

         for(UUID playerId : data.getAllBattlePassPlayers()) {
            int level = data.getBpCurrentLevel(playerId);
            if (level > 0) {
               int passTier = data.getBpPurchasedTier(playerId);
               Set<Integer> claimed = data.getBpClaimedTiers(playerId);
               if (currentSeason == 2 && claimed.contains(20) && !data.hasOwnedItem(playerId, "bp_kirin_eligible")) {
                  data.addOwnedItem(playerId, "bp_kirin_eligible");
                  ++kirinMarked;
               }

               if (currentSeason == 3 && claimed.contains(20) && !data.hasOwnedItem(playerId, "bp_season_3_jutsu_eligible")) {
                  data.addOwnedItem(playerId, "bp_season_3_jutsu_eligible");
               }

               Set<Integer> unclaimed = new HashSet();

               for(int t = 1; t <= level; ++t) {
                  if (!claimed.contains(t)) {
                     unclaimed.add(t);
                  }
               }

               if (!unclaimed.isEmpty()) {
                  data.setBpPendingAutoClaim(playerId, unclaimed, currentSeason, passTier);
                  ++queued;
               }
            }
         }

         System.out.println("[BattlePass] Queued auto-claim for " + queued + " players (season " + currentSeason + "), marked " + kirinMarked + " for Kirin eligibility");
      }
   }

   public void processPendingAutoClaim(UUID playerId, World world) {
      ShopSavedData data = ShopSavedData.get(world);
      if (data.hasBpPendingAutoClaim(playerId)) {
         Set<Integer> tiers = data.getBpPendingAutoClaimTiers(playerId);
         int oldSeason = data.getBpPendingAutoClaimSeason(playerId);
         int oldPassTier = data.getBpPendingAutoClaimPass(playerId);
         BattlePassSeason oldSeasonInst = new BattlePassSeason(oldSeason, BattlePassSeason.getSeasonDisplayName(oldSeason), 0L);
         EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(playerId);
         String playerName = player != null ? player.getName() : playerId.toString();
         List<Integer> sortedTiers = new ArrayList(tiers);
         Collections.sort(sortedTiers);
         int granted = 0;

         for(int tier : sortedTiers) {
            BattlePassDefinition.TierReward freeR = BattlePassDefinition.getFreeTierReward(tier);
            if (freeR != null && !freeR.isEmpty()) {
               this.grantReward(playerId, freeR, world);
            }

            if (oldPassTier > 0) {
               BattlePassDefinition.TierReward premR = BattlePassDefinition.getPremiumTierReward(tier, oldSeason);
               if (premR != null && !premR.isEmpty()) {
                  this.grantReward(playerId, premR, world);
               }

               if (tier == 15) {
                  String effectId = oldSeasonInst.getKillEffectId();
                  data.addOwnedKillEffect(playerId, effectId);
                  if (data.getActiveKillEffect(playerId).isEmpty()) {
                     data.setActiveKillEffect(playerId, effectId);
                  }
               }

               if (tier == 20) {
                  this.grantOrUnlockSeasonalJutsu(playerId, oldSeason, world);
               }
            }

            ++granted;
         }

         data.clearBpPendingAutoClaim(playerId);
         if (player != null) {
            player.sendMessage(new TextComponentString("§6[Battle Pass] §eAuto-claimed " + granted + " unclaimed tier reward(s) from " + BattlePassSeason.getSeasonDisplayName(oldSeason) + ". Check your inventory and overflow bank!"));
            ShopNetworkHelper.sendBattlePassSync(player);
            ShopNetworkHelper.sendShopSync(player);
         }

         System.out.println("[BattlePass] Auto-claimed " + granted + " tiers for " + playerName + " (from season " + oldSeason + ")");
      }
   }

   public void checkSeasonReset(World world) {
      ShopSavedData data = ShopSavedData.get(world);
      int seasonNum = data.getBpSeasonNumber();
      long startTime = data.getBpSeasonStartTime();
      if (seasonNum > 0 && startTime > 0L) {
         BattlePassSeason season = new BattlePassSeason(seasonNum, BattlePassSeason.getSeasonDisplayName(seasonNum), startTime);
         long now = System.currentTimeMillis();
         if (now >= season.getEndTime() + 172800000L) {
            int newSeasonNum = seasonNum + 1;
            this.queueAutoClaimForReset(world);
            data.setBpSeasonNumber(newSeasonNum);
            data.setBpSeasonStartTime(BattlePassSeason.getAlignedStartTime(now));
            data.resetAllBattlePassData();
            System.out.println("[BattlePass] " + season.getSeasonName() + " ended. Starting " + BattlePassSeason.getSeasonDisplayName(newSeasonNum) + ".");
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                  this.processPendingAutoClaim(player.getUniqueID(), world);
                  ShopNetworkHelper.sendShopSync(player);
                  ShopNetworkHelper.sendBattlePassSync(player);
               }
            }
         }

      } else {
         data.setBpSeasonNumber(3);
         data.setBpSeasonStartTime(BattlePassSeason.getAlignedStartTime(System.currentTimeMillis()));
      }
   }

   public boolean toggleKillEffect(UUID playerId, String effectId, World world) {
      if (effectId != null && !effectId.isEmpty()) {
         ShopSavedData data = ShopSavedData.get(world);
         if (!data.hasOwnedKillEffect(playerId, effectId)) {
            return false;
         } else {
            String currentEffect = data.getActiveKillEffect(playerId);
            if (effectId.equals(currentEffect)) {
               data.setActiveKillEffect(playerId, "");
            } else {
               data.setActiveKillEffect(playerId, effectId);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private void checkDailyCapReset(ShopSavedData data) {
      long now = System.currentTimeMillis();
      long lastReset = data.getBpDailyCapResetTime();
      long nowDay = now / 86400000L;
      long lastDay = lastReset / 86400000L;
      if (nowDay > lastDay) {
         data.resetBpDailyCaps();
         data.setBpDailyCapResetTime(now);
      }

   }
}
