package net.luck.narutoaddon.OtherCode.shop.network;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@SideOnly(Side.CLIENT)
public class ShopAdminClientData {
   public static String targetName = "";
   public static String targetUUID = "";
   public static long ryoBalance = 0L;
   public static int tokenBalance = 0;
   public static int loginStreak = 0;
   public static int overflowCount = 0;
   public static long lifetimeSpent = 0L;
   public static int lifetimeCratesOpened = 0;
   public static List<AdminHistoryEntry> history = new CopyOnWriteArrayList();
   public static List<String> ownedItems = new CopyOnWriteArrayList();
   public static Map<String, Integer> pityCounters = new ConcurrentHashMap();
   public static List<AdminTrackedItem> trackedItems = new CopyOnWriteArrayList();
   public static List<AdminRestoreLog> restoreLog = new CopyOnWriteArrayList();

   public static void clear() {
      targetName = "";
      targetUUID = "";
      ryoBalance = 0L;
      tokenBalance = 0;
      loginStreak = 0;
      overflowCount = 0;
      lifetimeSpent = 0L;
      lifetimeCratesOpened = 0;
      history = new CopyOnWriteArrayList();
      ownedItems = new CopyOnWriteArrayList();
      pityCounters = new ConcurrentHashMap();
      trackedItems = new CopyOnWriteArrayList();
      restoreLog = new CopyOnWriteArrayList();
   }

   public static class AdminHistoryEntry {
      public final String crateId;
      public final String displayName;
      public final int rarityOrdinal;
      public final long timestamp;
      public final int tokenAmount;

      public AdminHistoryEntry(String crateId, String displayName, int rarityOrdinal, long timestamp, int tokenAmount) {
         this.crateId = crateId;
         this.displayName = displayName;
         this.rarityOrdinal = rarityOrdinal;
         this.timestamp = timestamp;
         this.tokenAmount = tokenAmount;
      }
   }

   public static class AdminTrackedItem {
      public final String regName;
      public final int meta;
      public final String displayName;
      public final String category;
      public final boolean owned;
      public final long firstSeen;
      public final long lastSeen;
      public final int[] jutsuXp;
      public final boolean hasOwner;
      public final boolean isAffinity;

      public AdminTrackedItem(String regName, int meta, String displayName, String category, boolean owned, long firstSeen, long lastSeen, int[] jutsuXp, boolean hasOwner, boolean isAffinity) {
         this.regName = regName;
         this.meta = meta;
         this.displayName = displayName;
         this.category = category;
         this.owned = owned;
         this.firstSeen = firstSeen;
         this.lastSeen = lastSeen;
         this.jutsuXp = jutsuXp;
         this.hasOwner = hasOwner;
         this.isAffinity = isAffinity;
      }
   }

   public static class AdminRestoreLog {
      public final long timestamp;
      public final String adminName;
      public final String itemName;
      public final String targetPlayerName;

      public AdminRestoreLog(long timestamp, String adminName, String itemName, String targetPlayerName) {
         this.timestamp = timestamp;
         this.adminName = adminName;
         this.itemName = itemName;
         this.targetPlayerName = targetPlayerName;
      }
   }
}
