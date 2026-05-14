
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiCheeseValidator {
   public static final int MIN_NINJA_XP_BASE = 10000;
   public static final float MIN_HEALTH_BASE = 50.0F;
   public static final int[] RANK_MIN_NINJA_XP = new int[]{10000, 15000, 25000, 40000, 60000, 80000};
   public static final float[] RANK_MIN_HEALTH = new float[]{50.0F, 60.0F, 80.0F, 100.0F, 120.0F, 150.0F};
   private static final long VICTIM_COOLDOWN_MS = 900000L;
   private static final long RECENT_DEATH_MS = 30000L;
   private static final long AFK_THRESHOLD_MS = 60000L;
   private static final double AFK_MOVE_THRESHOLD = (double)0.5F;
   private static final Map<String, Long> killCooldowns = new HashMap();
   private static final Map<UUID, Long> recentDeaths = new HashMap();
   private static final Map<UUID, PositionRecord> lastPositions = new HashMap();

   public static ValidationResult validateKill(EntityPlayerMP killer, EntityPlayerMP victim) {
      return validateKill(killer, victim, 0);
   }

   public static ValidationResult validateKill(EntityPlayerMP killer, EntityPlayerMP victim, int rankOrdinal) {
      if (isSameVillage(killer, victim)) {
         return ValidationResult.invalid(Reason.SAME_VILLAGE);
      } else {
         int minXp = getMinNinjaXp(rankOrdinal);
         if (!hasMinNinjaXp(victim, minXp)) {
            return ValidationResult.invalid(Reason.LOW_NINJA_XP);
         } else {
            float minHp = getMinHealth(rankOrdinal);
            if (!hasMinHealth(victim, minHp)) {
               return ValidationResult.invalid(Reason.LOW_HEALTH);
            } else if (isOnVictimCooldown(killer.getUniqueID(), victim.getUniqueID())) {
               return ValidationResult.invalid(Reason.VICTIM_ON_COOLDOWN);
            } else if (isRecentlyDead(victim.getUniqueID())) {
               return ValidationResult.invalid(Reason.VICTIM_RECENTLY_DEAD);
            } else {
               return isAfk(victim) ? ValidationResult.invalid(Reason.VICTIM_AFK) : ValidationResult.valid();
            }
         }
      }
   }

   public static int getMinNinjaXp(int rankOrdinal) {
      return rankOrdinal >= 0 && rankOrdinal < RANK_MIN_NINJA_XP.length ? RANK_MIN_NINJA_XP[rankOrdinal] : 10000;
   }

   public static float getMinHealth(int rankOrdinal) {
      return rankOrdinal >= 0 && rankOrdinal < RANK_MIN_HEALTH.length ? RANK_MIN_HEALTH[rankOrdinal] : 50.0F;
   }

   public static boolean isSameVillage(EntityPlayerMP killer, EntityPlayerMP victim) {
      VillageHelper.Village killerVillage = VillageHelper.getVillage(killer);
      VillageHelper.Village victimVillage = VillageHelper.getVillage(victim);
      if (killerVillage != VillageHelper.Village.UNKNOWN && victimVillage != VillageHelper.Village.UNKNOWN) {
         return killerVillage == victimVillage;
      } else {
         return false;
      }
   }

   public static boolean hasMinNinjaXp(EntityPlayer victim, int minXp) {
      try {
         double xp = victim.getEntityData().getDouble("battle_experience");
         return xp >= (double)minXp;
      } catch (Exception var4) {
         return true;
      }
   }

   public static boolean hasMinHealth(EntityPlayer victim, float minHealth) {
      return victim.getMaxHealth() >= minHealth;
   }

   public static boolean isOnVictimCooldown(UUID killerUUID, UUID victimUUID) {
      String key = killerUUID.toString() + ":" + victimUUID.toString();
      Long lastKill = (Long)killCooldowns.get(key);
      if (lastKill == null) {
         return false;
      } else {
         return System.currentTimeMillis() - lastKill < 900000L;
      }
   }

   public static boolean isRecentlyDead(UUID victimUUID) {
      Long deathTime = (Long)recentDeaths.get(victimUUID);
      if (deathTime == null) {
         return false;
      } else {
         return System.currentTimeMillis() - deathTime < 30000L;
      }
   }

   public static boolean isAfk(EntityPlayer victim) {
      PositionRecord record = (PositionRecord)lastPositions.get(victim.getUniqueID());
      if (record == null) {
         return false;
      } else {
         double dx = victim.posX - record.x;
         double dy = victim.posY - record.y;
         double dz = victim.posZ - record.z;
         double distSq = dx * dx + dy * dy + dz * dz;
         if (distSq < (double)0.25F) {
            return System.currentTimeMillis() - record.timestamp >= 60000L;
         } else {
            return false;
         }
      }
   }

   public static void onPlayerTick(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      PositionRecord record = (PositionRecord)lastPositions.get(uuid);
      if (record == null) {
         lastPositions.put(uuid, new PositionRecord(player.posX, player.posY, player.posZ, System.currentTimeMillis()));
      } else {
         double dx = player.posX - record.x;
         double dy = player.posY - record.y;
         double dz = player.posZ - record.z;
         double distSq = dx * dx + dy * dy + dz * dz;
         if (distSq >= (double)0.25F) {
            lastPositions.put(uuid, new PositionRecord(player.posX, player.posY, player.posZ, System.currentTimeMillis()));
         }

      }
   }

   public static void onPlayerDeath(EntityPlayerMP victim) {
      recentDeaths.put(victim.getUniqueID(), System.currentTimeMillis());
   }

   public static void recordKill(UUID killerUUID, UUID victimUUID) {
      String key = killerUUID.toString() + ":" + victimUUID.toString();
      killCooldowns.put(key, System.currentTimeMillis());
   }

   public static void onPlayerLogout(UUID playerUUID) {
      lastPositions.remove(playerUUID);
   }

   public static void cleanupExpired() {
      long now = System.currentTimeMillis();
      killCooldowns.entrySet().removeIf((e) -> now - (Long)e.getValue() > 900000L);
      recentDeaths.entrySet().removeIf((e) -> now - (Long)e.getValue() > 30000L);
   }

   public static enum Reason {
      VALID,
      SAME_VILLAGE,
      LOW_NINJA_XP,
      LOW_HEALTH,
      VICTIM_ON_COOLDOWN,
      VICTIM_RECENTLY_DEAD,
      VICTIM_AFK;
   }

   public static class ValidationResult {
      private final Reason reason;

      private ValidationResult(Reason reason) {
         this.reason = reason;
      }

      public boolean isValid() {
         return this.reason == Reason.VALID;
      }

      public Reason getReason() {
         return this.reason;
      }

      public static ValidationResult valid() {
         return new ValidationResult(Reason.VALID);
      }

      public static ValidationResult invalid(Reason reason) {
         return new ValidationResult(reason);
      }
   }

   private static class PositionRecord {
      final double x;
      final double y;
      final double z;
      final long timestamp;

      PositionRecord(double x, double y, double z, long timestamp) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.timestamp = timestamp;
      }
   }
}
