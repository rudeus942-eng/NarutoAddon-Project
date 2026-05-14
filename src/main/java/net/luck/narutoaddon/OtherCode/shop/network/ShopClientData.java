package net.luck.narutoaddon.OtherCode.shop.network;

import net.luck.narutoaddon.OtherCode.shop.core.ShopCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

@SideOnly(Side.CLIENT)
public class ShopClientData {
   public static long ryoBalance = 0L;
   public static List<CrateClientInfo> availableCrates = new ArrayList();
   public static List<HistoryEntryInfo> history = new ArrayList();
   public static CrateResultData pendingResult = null;
   public static List<CrateResultData> multiRollQueue = new ArrayList();
   public static int multiRollTotal = 0;
   public static int multiRollCurrent = 0;
   public static Map<String, Integer> pityCounters = new HashMap();
   public static int tokenBalance = 0;
   public static int crystalBalance = 0;
   public static int loginStreak = 0;
   public static int overflowCount = 0;
   public static boolean clanTabUnlocked = false;
   public static boolean so6pTabUnlocked = false;
   public static boolean akamichiUnlocked = false;
   public static boolean freeRollAvailable = false;
   public static boolean firstPurchaseBonusAvailable = false;
   public static boolean weekendPromoSO6PAvailable = false;
   public static boolean pendingDuplicate = false;
   public static int pendingDuplicateTokens = 0;
   public static int pendingDupeCrystals = 0;
   public static long travelCooldownEnd = 0L;
   public static int bpSeasonNumber = 1;
   public static String bpSeasonName = "Season 1: Dawn of the Shinobi";
   public static long bpSeasonEndTime = 0L;
   public static int bpCurrentTier = 0;
   public static int bpCurrentXP = 0;
   public static int bpXPForNextLevel = 500;
   public static int bpPurchasedTier = 0;
   public static Set<Integer> bpClaimedTiers = new HashSet();
   public static String bpActiveKillEffect = null;
   public static Set<String> bpOwnedKillEffects = new HashSet();
   public static int bpOtsutsukiSkipsRemaining = 0;

   public static List<CrateClientInfo> getCratesByCategory(ShopCategory cat) {
      List<CrateClientInfo> result = new ArrayList();
      byte catOrd = (byte)cat.id;

      for(CrateClientInfo c : availableCrates) {
         if (c.categoryOrdinal == catOrd) {
            result.add(c);
         }
      }

      return result;
   }

   public static CrateClientInfo getCrateById(String crateId) {
      if (crateId == null) {
         return null;
      } else {
         for(CrateClientInfo c : availableCrates) {
            if (crateId.equals(c.crateId)) {
               return c;
            }
         }

         return null;
      }
   }

   public static int getPityCount(String crateId) {
      return (Integer)pityCounters.getOrDefault(crateId, 0);
   }

   public static void clear() {
      ryoBalance = 0L;
      availableCrates = new ArrayList();
      history = new ArrayList();
      pendingResult = null;
      multiRollQueue = new ArrayList();
      multiRollTotal = 0;
      multiRollCurrent = 0;
      pityCounters = new HashMap();
      tokenBalance = 0;
      crystalBalance = 0;
      loginStreak = 0;
      overflowCount = 0;
      clanTabUnlocked = false;
      so6pTabUnlocked = false;
      akamichiUnlocked = false;
      freeRollAvailable = false;
      weekendPromoSO6PAvailable = false;
      firstPurchaseBonusAvailable = false;
      pendingDuplicate = false;
      pendingDuplicateTokens = 0;
      pendingDupeCrystals = 0;
      travelCooldownEnd = 0L;
      bpSeasonNumber = 1;
      bpSeasonName = "Season 1: Dawn of the Shinobi";
      bpSeasonEndTime = 0L;
      bpCurrentTier = 0;
      bpCurrentXP = 0;
      bpXPForNextLevel = 500;
      bpPurchasedTier = 0;
      bpClaimedTiers = new HashSet();
      bpActiveKillEffect = null;
      bpOwnedKillEffects = new HashSet();
      bpOtsutsukiSkipsRemaining = 0;
   }

   public static void clearResult() {
      pendingResult = null;
      multiRollQueue = new ArrayList();
      multiRollTotal = 0;
      multiRollCurrent = 0;
      pendingDuplicate = false;
      pendingDuplicateTokens = 0;
      pendingDupeCrystals = 0;
   }

   public static class CrateClientInfo {
      public final String crateId;
      public final String displayName;
      public final String description;
      public final byte categoryOrdinal;
      public final long price;
      public final int crystalPrice;
      public final boolean directPurchase;
      public final String iconItemId;
      public final int iconMeta;
      public final List<LootEntryClientInfo> lootEntries;

      public CrateClientInfo(String crateId, String displayName, String description, byte categoryOrdinal, long price, int crystalPrice, boolean directPurchase, String iconItemId, int iconMeta, List<LootEntryClientInfo> lootEntries) {
         this.crateId = crateId;
         this.displayName = displayName;
         this.description = description;
         this.categoryOrdinal = categoryOrdinal;
         this.price = price;
         this.crystalPrice = crystalPrice;
         this.directPurchase = directPurchase;
         this.iconItemId = iconItemId;
         this.iconMeta = iconMeta;
         this.lootEntries = (List<LootEntryClientInfo>)(lootEntries != null ? lootEntries : new ArrayList());
      }

      public ShopCategory getCategory() {
         return ShopCategory.fromOrdinal(this.categoryOrdinal);
      }

      public boolean usesCrystals() {
         return this.crystalPrice > 0;
      }
   }

   public static class LootEntryClientInfo {
      public final String itemId;
      public final int itemMeta;
      public final int itemCount;
      public final byte rarityOrdinal;
      public final double weight;
      public final float dropChancePct;
      public final String displayNameOverride;
      public final NBTTagCompound itemNbt;
      public final List<BundleItemClientInfo> bundleItems;

      public LootEntryClientInfo(String itemId, int itemMeta, int itemCount, byte rarityOrdinal, double weight, float dropChancePct) {
         this(itemId, itemMeta, itemCount, rarityOrdinal, weight, dropChancePct, (String)null, (NBTTagCompound)null, new ArrayList());
      }

      public LootEntryClientInfo(String itemId, int itemMeta, int itemCount, byte rarityOrdinal, double weight, float dropChancePct, String displayNameOverride, NBTTagCompound itemNbt, List<BundleItemClientInfo> bundleItems) {
         this.itemId = itemId;
         this.itemMeta = itemMeta;
         this.itemCount = itemCount;
         this.rarityOrdinal = rarityOrdinal;
         this.weight = weight;
         this.dropChancePct = dropChancePct;
         this.displayNameOverride = displayNameOverride;
         this.itemNbt = itemNbt;
         this.bundleItems = (List<BundleItemClientInfo>)(bundleItems != null ? bundleItems : new ArrayList());
      }

      public boolean hasDisplayNameOverride() {
         return this.displayNameOverride != null && !this.displayNameOverride.isEmpty();
      }

      public boolean hasBundleItems() {
         return !this.bundleItems.isEmpty();
      }

      public int getBundleSize() {
         return 1 + this.bundleItems.size();
      }
   }

   public static class BundleItemClientInfo {
      public final String itemId;
      public final int itemMeta;
      public final int itemCount;
      public final NBTTagCompound itemNbt;

      public BundleItemClientInfo(String itemId, int itemMeta, int itemCount, NBTTagCompound itemNbt) {
         this.itemId = itemId;
         this.itemMeta = itemMeta;
         this.itemCount = itemCount;
         this.itemNbt = itemNbt;
      }
   }

   public static class HistoryEntryInfo {
      public final String crateDisplayName;
      public final String itemDisplayName;
      public final byte rarityOrdinal;
      public final long timestamp;

      public HistoryEntryInfo(String crateDisplayName, String itemDisplayName, byte rarityOrdinal, long timestamp) {
         this.crateDisplayName = crateDisplayName;
         this.itemDisplayName = itemDisplayName;
         this.rarityOrdinal = rarityOrdinal;
         this.timestamp = timestamp;
      }
   }

   public static class CrateResultData {
      public final String crateId;
      public final long newBalance;
      public final String wonItemId;
      public final int wonItemMeta;
      public final int wonItemCount;
      public final byte wonRarity;
      public final String wonDisplayName;
      public final List<LootEntryClientInfo> displayStrip;
      public final int targetIndex;
      public final boolean isDuplicate;
      public final int duplicateTokensAwarded;
      public final int dupeCrystalsAwarded;
      public final String wonBundleDisplayName;
      public final List<BundleItemClientInfo> wonBundleItems;
      public boolean consumed;

      public CrateResultData(String crateId, long newBalance, String wonItemId, int wonItemMeta, int wonItemCount, byte wonRarity, String wonDisplayName, List<LootEntryClientInfo> displayStrip, int targetIndex) {
         this(crateId, newBalance, wonItemId, wonItemMeta, wonItemCount, wonRarity, wonDisplayName, displayStrip, targetIndex, false, 0, 0, (String)null, (List)null);
      }

      public CrateResultData(String crateId, long newBalance, String wonItemId, int wonItemMeta, int wonItemCount, byte wonRarity, String wonDisplayName, List<LootEntryClientInfo> displayStrip, int targetIndex, boolean isDuplicate, int duplicateTokensAwarded) {
         this(crateId, newBalance, wonItemId, wonItemMeta, wonItemCount, wonRarity, wonDisplayName, displayStrip, targetIndex, isDuplicate, duplicateTokensAwarded, 0, (String)null, (List)null);
      }

      public CrateResultData(String crateId, long newBalance, String wonItemId, int wonItemMeta, int wonItemCount, byte wonRarity, String wonDisplayName, List<LootEntryClientInfo> displayStrip, int targetIndex, boolean isDuplicate, int duplicateTokensAwarded, int dupeCrystalsAwarded, String wonBundleDisplayName, List<BundleItemClientInfo> wonBundleItems) {
         this.consumed = false;
         this.crateId = crateId;
         this.newBalance = newBalance;
         this.wonItemId = wonItemId;
         this.wonItemMeta = wonItemMeta;
         this.wonItemCount = wonItemCount;
         this.wonRarity = wonRarity;
         this.wonDisplayName = wonDisplayName;
         this.displayStrip = (List<LootEntryClientInfo>)(displayStrip != null ? displayStrip : new ArrayList());
         this.targetIndex = targetIndex;
         this.isDuplicate = isDuplicate;
         this.duplicateTokensAwarded = duplicateTokensAwarded;
         this.dupeCrystalsAwarded = dupeCrystalsAwarded;
         this.wonBundleDisplayName = wonBundleDisplayName;
         this.wonBundleItems = (List<BundleItemClientInfo>)(wonBundleItems != null ? wonBundleItems : new ArrayList());
      }

      public CrateResultData(String crateId, long newBalance, String wonItemId, int wonItemMeta, int wonItemCount, byte wonRarity, String wonDisplayName, List<LootEntryClientInfo> displayStrip, int targetIndex, boolean isDuplicate, int duplicateTokensAwarded, String wonBundleDisplayName, List<BundleItemClientInfo> wonBundleItems) {
         this(crateId, newBalance, wonItemId, wonItemMeta, wonItemCount, wonRarity, wonDisplayName, displayStrip, targetIndex, isDuplicate, duplicateTokensAwarded, 0, wonBundleDisplayName, wonBundleItems);
      }

      public boolean hasBundle() {
         return !this.wonBundleItems.isEmpty();
      }
   }
}
