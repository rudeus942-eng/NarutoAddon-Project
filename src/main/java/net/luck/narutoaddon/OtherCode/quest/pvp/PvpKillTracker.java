
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.WarManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.UUID;

public class PvpKillTracker {
   private static final int BASE_PVP_XP = 5;
   private static final int MAX_PVP_XP = 15;
   private static final String PVP_XP_OBJECTIVE = "pvp_xp";
   private static final String AHZNB_NINJA_XP_KEY = "battle_experience";

   public static void onPlayerKill(EntityPlayerMP killer, EntityPlayerMP victim, DamageSource damageSource) {
      if (!killer.getUniqueID().equals(victim.getUniqueID())) {
         VillageHelper.Village killerVillage = VillageHelper.getVillage(killer);
         VillageHelper.Village victimVillage = VillageHelper.getVillage(victim);
         if (killerVillage != victimVillage || killerVillage == VillageHelper.Village.UNKNOWN) {
            boolean isWarKill = false;
            if (killerVillage != VillageHelper.Village.UNKNOWN && victimVillage != VillageHelper.Village.UNKNOWN) {
               isWarKill = WarManager.getInstance().isAtWar(killerVillage, victimVillage);
            }

            if (isWarKill || validateKill(killer, victim)) {
               int natureType = detectNature(damageSource);
               boolean victimHasDojutsu = hasDojutsuHelmet(victim);
               boolean victimHigherLevel = getNinjaXp(victim) > getNinjaXp(killer);
               KillData killData = new KillData(killer.getUniqueID(), victim.getUniqueID(), killerVillage, victimVillage, natureType, new BlockPos(killer.posX, killer.posY, killer.posZ), System.currentTimeMillis(), victimHasDojutsu, victimHigherLevel);
               routeToPvpManager(killData);
               routeToWarManager(killData, killer, victim);
               int xpAwarded = calculatePvpXp(killer, victim);
               awardPvpXp(killer, xpAwarded);
               sendKillMessage(killer, victim, killerVillage, victimVillage, xpAwarded);
            }
         }
      }
   }

   private static boolean validateKill(EntityPlayerMP killer, EntityPlayerMP victim) {
      try {
         AntiCheeseValidator.ValidationResult result = AntiCheeseValidator.validateKill(killer, victim);
         if (!result.isValid()) {
            String reason;
            switch (result.getReason()) {
               case SAME_VILLAGE:
                  reason = "same village (friendly fire)";
                  break;
               case LOW_NINJA_XP:
                  reason = "victim has too little Ninja XP";
                  break;
               case LOW_HEALTH:
                  reason = "victim has too little max health";
                  break;
               case VICTIM_ON_COOLDOWN:
                  reason = "already killed this player recently (15m cooldown)";
                  break;
               case VICTIM_RECENTLY_DEAD:
                  reason = "victim just respawned (30s protection)";
                  break;
               case VICTIM_AFK:
                  reason = "victim is AFK";
                  break;
               default:
                  reason = "invalid kill";
            }

            killer.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[PvP] " + TextFormatting.GRAY + "Kill not counted: " + TextFormatting.YELLOW + reason));
            return false;
         } else {
            return true;
         }
      } catch (Exception var4) {
         return true;
      }
   }

   private static int detectNature(DamageSource source) {
      try {
         NatureReleaseDetector.NatureType nature = NatureReleaseDetector.getNatureFromDamageSource(source);
         return NatureReleaseDetector.toInt(nature);
      } catch (Exception var2) {
         return 0;
      }
   }

   private static void routeToPvpManager(KillData killData) {
      try {
         PvpManager pvpManager = PvpManager.getInstance();
         pvpManager.onValidPvpKill(killData);
      } catch (Exception var2) {
      }

   }

   private static void routeToWarManager(KillData killData, EntityPlayerMP killer, EntityPlayerMP victim) {
      try {
         WarManager warManager = WarManager.getInstance();
         warManager.onPvpKill(killer, victim, killData);
      } catch (Exception var4) {
      }

   }

   private static int calculatePvpXp(EntityPlayerMP killer, EntityPlayerMP victim) {
      double killerNxp = getNinjaXp(killer);
      double victimNxp = getNinjaXp(victim);
      if (killerNxp <= (double)0.0F) {
         return 5;
      } else {
         float ratio = (float)(victimNxp / killerNxp);
         int xp = Math.round(5.0F * ratio);
         return Math.max(5, Math.min(xp, 15));
      }
   }

   private static void awardPvpXp(EntityPlayerMP player, int amount) {
      Scoreboard scoreboard = player.getWorldScoreboard();
      ScoreObjective objective = scoreboard.getObjective("pvp_xp");
      if (objective != null) {
         String playerName = player.getName();
         if (!scoreboard.entityHasObjective(playerName, objective)) {
            scoreboard.getOrCreateScore(playerName, objective);
         }

         int current = scoreboard.getOrCreateScore(playerName, objective).getScorePoints();
         scoreboard.getOrCreateScore(playerName, objective).setScorePoints(current + amount);
      }
   }

   private static double getNinjaXp(EntityPlayerMP player) {
      try {
         return player.getEntityData().getDouble("battle_experience");
      } catch (Exception var2) {
         return (double)0.0F;
      }
   }

   private static void sendKillMessage(EntityPlayerMP killer, EntityPlayerMP victim, VillageHelper.Village killerVillage, VillageHelper.Village victimVillage, int xpAwarded) {
      String victimLabel = victim.getName();
      if (victimVillage != VillageHelper.Village.UNKNOWN) {
         victimLabel = victimVillage.villageName + " shinobi " + victim.getName();
      }

      String msg = TextFormatting.RED + "[PvP] " + TextFormatting.GOLD + "Eliminated " + victimLabel + TextFormatting.GRAY + " (+" + xpAwarded + " PvP XP)";
      killer.sendMessage(new TextComponentString(msg));
   }

   private static boolean hasDojutsuHelmet(EntityPlayerMP player) {
      ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
      if (helmet.isEmpty()) {
         return false;
      } else if (helmet.getItem().getRegistryName() == null) {
         return false;
      } else {
         String name = helmet.getItem().getRegistryName().toString().toLowerCase();
         return name.contains("sharingan") || name.contains("byakugan") || name.contains("rinnegan") || name.contains("tenseigan") || name.contains("kekkei");
      }
   }

   public static class KillData {
      public final UUID killerId;
      public final UUID victimId;
      public final VillageHelper.Village killerVillage;
      public final VillageHelper.Village victimVillage;
      public final int natureType;
      public final BlockPos location;
      public final long timestamp;
      public final boolean victimHasDojutsu;
      public final boolean victimHigherLevel;

      public KillData(UUID killerId, UUID victimId, VillageHelper.Village killerVillage, VillageHelper.Village victimVillage, int natureType, BlockPos location, long timestamp, boolean victimHasDojutsu, boolean victimHigherLevel) {
         this.killerId = killerId;
         this.victimId = victimId;
         this.killerVillage = killerVillage;
         this.victimVillage = victimVillage;
         this.natureType = natureType;
         this.location = location;
         this.timestamp = timestamp;
         this.victimHasDojutsu = victimHasDojutsu;
         this.victimHigherLevel = victimHigherLevel;
      }
   }
}
