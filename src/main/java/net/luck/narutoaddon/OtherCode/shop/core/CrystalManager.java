package net.luck.narutoaddon.OtherCode.shop.core;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CrystalManager {
   private static CrystalManager INSTANCE;
   private final Map<UUID, LinkedList<Long>> grantTimestamps = new ConcurrentHashMap();
   private static final int RATE_LIMIT_COUNT = 10;
   private static final long RATE_LIMIT_WINDOW_MS = 60000L;
   private static final SimpleDateFormat AUDIT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

   private CrystalManager() {
   }

   public static CrystalManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new CrystalManager();
      }

      return INSTANCE;
   }

   public int getBalance(UUID playerUUID, World world) {
      ShopSavedData data = ShopSavedData.get(world);
      return data.getCrystals(playerUUID);
   }

   public int addCrystals(UUID playerUUID, int amount, World world, String executor) {
      if (amount <= 0) {
         return this.getBalance(playerUUID, world);
      } else if (this.isRateLimited(playerUUID)) {
         return -1;
      } else {
         this.recordGrant(playerUUID);
         ShopSavedData data = ShopSavedData.get(world);
         int finalAmount = amount;
         boolean bonusApplied = false;
         if (!data.hasUsedFirstPurchaseBonus(playerUUID)) {
            finalAmount = amount * 2;
            data.markFirstPurchaseBonusUsed(playerUUID);
            bonusApplied = true;
         }

         data.addCrystals(playerUUID, finalAmount);
         data.recordCrystalPurchase(playerUUID, finalAmount);
         int newBalance = data.getCrystals(playerUUID);
         String playerName = this.resolvePlayerName(playerUUID);
         if (bonusApplied) {
            this.writeAuditLog(executor, playerName + " [2x FIRST PURCHASE BONUS: " + amount + " -> " + finalAmount + "]", finalAmount, newBalance);
         } else {
            this.writeAuditLog(executor, playerName, finalAmount, newBalance);
         }

         return newBalance;
      }
   }

   public boolean removeCrystals(UUID playerUUID, int amount, World world) {
      if (amount <= 0) {
         return false;
      } else {
         ShopSavedData data = ShopSavedData.get(world);
         return data.removeCrystals(playerUUID, amount);
      }
   }

   public boolean hasCrystals(UUID playerUUID, int amount, World world) {
      ShopSavedData data = ShopSavedData.get(world);
      return data.hasCrystals(playerUUID, amount);
   }

   private boolean isRateLimited(UUID playerUUID) {
      LinkedList<Long> timestamps = (LinkedList)this.grantTimestamps.get(playerUUID);
      if (timestamps == null) {
         return false;
      } else {
         long cutoff = System.currentTimeMillis() - 60000L;

         while(!timestamps.isEmpty() && (Long)timestamps.getFirst() < cutoff) {
            timestamps.removeFirst();
         }

         return timestamps.size() >= 10;
      }
   }

   private void recordGrant(UUID playerUUID) {
      LinkedList<Long> timestamps = (LinkedList)this.grantTimestamps.computeIfAbsent(playerUUID, (k) -> new LinkedList());
      timestamps.addLast(System.currentTimeMillis());
      long cutoff = System.currentTimeMillis() - 60000L;

      while(!timestamps.isEmpty() && (Long)timestamps.getFirst() < cutoff) {
         timestamps.removeFirst();
      }

   }

   private void writeAuditLog(String executor, String playerName, int amount, int newBalance) {
      try {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server == null) {
            return;
         }

         File logsDir = new File(server.getDataDirectory(), "logs");
         if (!logsDir.exists()) {
            logsDir.mkdirs();
         }

         File auditFile = new File(logsDir, "crystal-audit.log");
         String timestamp = AUDIT_DATE_FORMAT.format(new Date());
         String line = String.format("[%s] CRYSTAL_GRANT executor=%s target=%s amount=%d newBalance=%d%n", timestamp, executor, playerName, amount, newBalance);
         FileWriter writer = new FileWriter(auditFile, true);

         try {
            writer.write(line);
         } finally {
            writer.close();
         }
      } catch (IOException e) {
         System.err.println("[CrystalManager] Failed to write audit log: " + e.getMessage());
      }

   }

   private String resolvePlayerName(UUID playerUUID) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerUUID);
         if (player != null) {
            return player.getName();
         }
      }

      return playerUUID.toString();
   }
}
