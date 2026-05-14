
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.KageManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class PvpMissionGenerator {
   private static final Random RANDOM = new Random();
   private static final int DAILY_NINJA_XP_MIN = 50;
   private static final int DAILY_NINJA_XP_MAX = 150;
   private static final int WEEKLY_NINJA_XP_MIN = 200;
   private static final int WEEKLY_NINJA_XP_MAX = 500;
   private static final int RANDOM_NINJA_XP_MIN = 10;
   private static final int RANDOM_NINJA_XP_MAX = 30;
   private static final int DAILY_PVP_XP_MIN = 30;
   private static final int DAILY_PVP_XP_MAX = 80;
   private static final int WEEKLY_PVP_XP_MIN = 100;
   private static final int WEEKLY_PVP_XP_MAX = 250;
   private static final int RANDOM_PVP_XP_MIN = 10;
   private static final int RANDOM_PVP_XP_MAX = 25;
   private static final float[] RANK_KILL_MULTIPLIERS = new float[]{1.0F, 1.2F, 1.5F, 1.8F, 2.2F, 2.8F};
   private static final int SURVIVE_TIMER_MIN = 12000;
   private static final int SURVIVE_TIMER_MAX = 18000;
   private static final int MIN_TARGET_BATTLE_XP = 100;

   @Nullable
   public static PvpMissionOffer generateOffer(UUID playerUUID, String subSlot, World world) {
      return generateOfferInternal(playerUUID, subSlot, world, (String)null);
   }

   @Nullable
   public static PvpMissionOffer rerollOffer(UUID playerUUID, String subSlot, World world) {
      PvpSavedData data = PvpSavedData.get(world);
      PvpMissionOffer currentOffer = data.getSavedOffer(playerUUID, subSlot);
      String excludeTemplateId = currentOffer != null ? currentOffer.getTemplateId() : null;
      return generateOfferInternal(playerUUID, subSlot, world, excludeTemplateId);
   }

   @Nullable
   public static PvpMissionOffer generateLeadershipOffer(UUID playerUUID, World world) {
      MinecraftServer server = world.getMinecraftServer();
      if (server == null) {
         return null;
      } else {
         EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerUUID);
         if (player == null) {
            return null;
         } else {
            VillageHelper.Village playerVillage = VillageHelper.getVillage(player);
            if (playerVillage == VillageHelper.Village.UNKNOWN) {
               return null;
            } else {
               PvpSavedData data = PvpSavedData.get(world);
               QuestDefinition.QuestRank playerRank = data.getPvpRank(playerUUID);
               List<PvpMissionTemplate> pool = new ArrayList();

               for(PvpMissionTemplate t : PvpMissionTemplate.getLeadership()) {
                  if (t.getMinRank().ordinal() <= playerRank.ordinal()) {
                     pool.add(t);
                  }
               }

               if (pool.isEmpty()) {
                  return null;
               } else {
                  VillageHelper.Village targetVillage = VillageRivalryManager.getTargetVillage(playerVillage, RANDOM);
                  if (targetVillage == null) {
                     targetVillage = VillageHelper.Village.SAND;
                  }

                  List<PvpMissionTemplate> filteredPool = new ArrayList(pool);

                  for(int i = filteredPool.size() - 1; i >= 0; --i) {
                     PvpMissionTemplate t = (PvpMissionTemplate)filteredPool.get(i);
                     if (t.getId().equals("kage_assassination")) {
                        UUID enemyKageId = findOnlineEnemyKage(playerVillage, targetVillage, server);
                        if (enemyKageId == null) {
                           filteredPool.remove(i);
                        }
                     }
                  }

                  if (filteredPool.isEmpty()) {
                     return null;
                  } else {
                     PvpMissionTemplate template = (PvpMissionTemplate)filteredPool.get(RANDOM.nextInt(filteredPool.size()));
                     Set<NatureReleaseDetector.NatureType> playerNatures = NatureReleaseDetector.getPlayerNatureReleases(player);
                     NatureReleaseDetector.NatureType selectedNature = null;
                     if (!playerNatures.isEmpty()) {
                        List<NatureReleaseDetector.NatureType> natureList = new ArrayList(playerNatures);
                        selectedNature = (NatureReleaseDetector.NatureType)natureList.get(RANDOM.nextInt(natureList.size()));
                     }

                     int baseKills;
                     if (template.getMinKills() == template.getMaxKills()) {
                        baseKills = template.getMinKills();
                     } else {
                        baseKills = template.getMinKills() + RANDOM.nextInt(template.getMaxKills() - template.getMinKills() + 1);
                     }

                     int scaledKills = Math.max(1, Math.round((float)baseKills * getRankKillMultiplier(playerRank)));
                     PvpObjective objective;
                     if (template.getId().equals("kage_assassination")) {
                        UUID enemyKageId = findOnlineEnemyKage(playerVillage, targetVillage, server);
                        if (enemyKageId == null) {
                           return null;
                        }

                        int timeLimitTicks = template.getTimeLimitTicks();
                        if (timeLimitTicks > 0) {
                           objective = (new PvpObjectiveBuilder(PvpObjective.ObjectiveType.KILL_SPECIFIC_PLAYER)).targetPlayerId(enemyKageId).killsRequired(1).timeLimit(timeLimitTicks).build();
                        } else {
                           objective = PvpObjective.killSpecificPlayer(enemyKageId);
                        }
                     } else {
                        objective = buildObjective(template, scaledKills, targetVillage, selectedNature, player, world, playerUUID);
                     }

                     if (objective == null) {
                        return null;
                     } else {
                        int ninjaXp = calculateReward(template.getBaseNinjaXp(), PvpMissionTemplate.Category.WEEKLY, 50, 150, 200, 500, 10, 30, playerRank);
                        int pvpXp = calculateReward(template.getBasePvpXp(), PvpMissionTemplate.Category.WEEKLY, 30, 80, 100, 250, 10, 25, playerRank);
                        int ryoReward = 600 + RANDOM.nextInt(1201);
                        String name = fillPlaceholders(template.getNamePattern(), playerVillage, targetVillage, selectedNature, scaledKills, (String)null, (String)null);
                        String loreText = template.getLore(playerVillage.teamName, targetVillage.teamName);
                        String description = fillPlaceholders(loreText, playerVillage, targetVillage, selectedNature, scaledKills, (String)null, (String)null);
                        String offerId = "pvp_ldr_" + System.currentTimeMillis() + "_" + RANDOM.nextInt(10000);
                        return new PvpMissionOffer(offerId, template.getId(), name, description, objective, ninjaXp, pvpXp, ryoReward, (String)null, playerRank, System.currentTimeMillis(), (String)null);
                     }
                  }
               }
            }
         }
      }
   }

   @Nullable
   public static PvpMissionOffer generateOfferFromTemplate(PvpMissionTemplate template, UUID playerUUID, World world, @Nullable String operationOrderId) {
      MinecraftServer server = world.getMinecraftServer();
      if (server == null) {
         return null;
      } else {
         EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerUUID);
         if (player == null) {
            return null;
         } else {
            VillageHelper.Village playerVillage = VillageHelper.getVillage(player);
            if (playerVillage == VillageHelper.Village.UNKNOWN) {
               return null;
            } else {
               PvpSavedData data = PvpSavedData.get(world);
               QuestDefinition.QuestRank playerRank = data.getPvpRank(playerUUID);
               VillageHelper.Village targetVillage = VillageRivalryManager.getTargetVillage(playerVillage, RANDOM);
               if (targetVillage == null) {
                  targetVillage = VillageHelper.Village.SAND;
               }

               if (template.getId().equals("kage_assassination")) {
                  UUID enemyKageId = findOnlineEnemyKage(playerVillage, targetVillage, server);
                  if (enemyKageId == null) {
                     return null;
                  }
               }

               Set<NatureReleaseDetector.NatureType> playerNatures = NatureReleaseDetector.getPlayerNatureReleases(player);
               NatureReleaseDetector.NatureType selectedNature = null;
               if (!playerNatures.isEmpty()) {
                  List<NatureReleaseDetector.NatureType> natureList = new ArrayList(playerNatures);
                  selectedNature = (NatureReleaseDetector.NatureType)natureList.get(RANDOM.nextInt(natureList.size()));
               }

               String roleTitle = null;
               String roleDesc = null;
               int scaledKills;
               if (template.isOperationTiered()) {
                  OperationTierTable tierTable = OperationTierTable.get(template.getId());
                  if (tierTable != null) {
                     OperationTierTable.TierEntry tierEntry = tierTable.getTier(playerRank);
                     scaledKills = tierEntry.killCount;
                     roleTitle = tierEntry.roleTitle;
                     roleDesc = tierEntry.roleDescription;
                  } else {
                     scaledKills = template.getMinKills();
                  }
               } else {
                  int baseKills;
                  if (template.getMinKills() == template.getMaxKills()) {
                     baseKills = template.getMinKills();
                  } else {
                     baseKills = template.getMinKills() + RANDOM.nextInt(template.getMaxKills() - template.getMinKills() + 1);
                  }

                  scaledKills = Math.max(1, Math.round((float)baseKills * getRankKillMultiplier(playerRank)));
               }

               PvpObjective objective;
               if (template.getId().equals("kage_assassination")) {
                  UUID enemyKageId = findOnlineEnemyKage(playerVillage, targetVillage, server);
                  if (enemyKageId == null) {
                     return null;
                  }

                  int timeLimitTicks = template.getTimeLimitTicks();
                  if (timeLimitTicks > 0) {
                     objective = (new PvpObjectiveBuilder(PvpObjective.ObjectiveType.KILL_SPECIFIC_PLAYER)).targetPlayerId(enemyKageId).killsRequired(1).timeLimit(timeLimitTicks).build();
                  } else {
                     objective = PvpObjective.killSpecificPlayer(enemyKageId);
                  }
               } else {
                  objective = buildObjective(template, scaledKills, targetVillage, selectedNature, player, world, playerUUID);
               }

               if (objective == null) {
                  return null;
               } else {
                  int ninjaXp = calculateReward(template.getBaseNinjaXp(), PvpMissionTemplate.Category.DAILY, 50, 150, 200, 500, 10, 30, playerRank);
                  int pvpXp = calculateReward(template.getBasePvpXp(), PvpMissionTemplate.Category.DAILY, 30, 80, 100, 250, 10, 25, playerRank);
                  int ryoReward = 300 + RANDOM.nextInt(601);
                  String name = fillPlaceholders(template.getNamePattern(), playerVillage, targetVillage, selectedNature, scaledKills, roleTitle, roleDesc);
                  String loreText = template.getLore(playerVillage.teamName, targetVillage.teamName);
                  String description = fillPlaceholders(loreText, playerVillage, targetVillage, selectedNature, scaledKills, roleTitle, roleDesc);
                  String offerId = "pvp_assigned_" + System.currentTimeMillis() + "_" + RANDOM.nextInt(10000);
                  return new PvpMissionOffer(offerId, template.getId(), name, description, objective, ninjaXp, pvpXp, ryoReward, (String)null, playerRank, System.currentTimeMillis(), operationOrderId);
               }
            }
         }
      }
   }

   @Nullable
   private static UUID findOnlineEnemyKage(VillageHelper.Village playerVillage, VillageHelper.Village preferredTarget, MinecraftServer server) {
      KageManager kageManager = KageManager.getInstance();
      Map<String, UUID> allKages = kageManager.getAllKages();
      UUID preferredKageId = (UUID)allKages.get(preferredTarget.teamName);
      if (preferredKageId != null && server.getPlayerList().getPlayerByUUID(preferredKageId) != null) {
         return preferredKageId;
      } else {
         for(Map.Entry<String, UUID> entry : allKages.entrySet()) {
            if (!((String)entry.getKey()).equals(playerVillage.teamName) && server.getPlayerList().getPlayerByUUID((UUID)entry.getValue()) != null) {
               return (UUID)entry.getValue();
            }
         }

         return null;
      }
   }

   @Nullable
   private static PvpMissionOffer generateOfferInternal(UUID playerUUID, String subSlot, World world, @Nullable String excludeTemplateId) {
      MinecraftServer server = world.getMinecraftServer();
      if (server == null) {
         return null;
      } else {
         EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerUUID);
         if (player == null) {
            return null;
         } else {
            VillageHelper.Village playerVillage = VillageHelper.getVillage(player);
            if (playerVillage == VillageHelper.Village.UNKNOWN) {
               return null;
            } else {
               PvpSavedData data = PvpSavedData.get(world);
               QuestDefinition.QuestRank playerRank = data.getPvpRank(playerUUID);
               PvpMissionTemplate.Category category = getSubSlotCategory(subSlot);
               if (category == null) {
                  return null;
               } else {
                  List<PvpMissionTemplate> pool = getTemplatePool(category);
                  List<PvpMissionTemplate> eligible = new ArrayList();

                  for(PvpMissionTemplate tmpl : pool) {
                     if (tmpl.getMinRank().ordinal() <= playerRank.ordinal() && (excludeTemplateId == null || !tmpl.getId().equals(excludeTemplateId))) {
                        eligible.add(tmpl);
                     }
                  }

                  if (eligible.isEmpty()) {
                     return null;
                  } else {
                     PvpMissionTemplate template = (PvpMissionTemplate)eligible.get(RANDOM.nextInt(eligible.size()));
                     VillageHelper.Village targetVillage = VillageRivalryManager.getTargetVillage(playerVillage, RANDOM);
                     if (targetVillage == null) {
                        targetVillage = VillageHelper.Village.SAND;
                     }

                     Set<NatureReleaseDetector.NatureType> playerNatures = NatureReleaseDetector.getPlayerNatureReleases(player);
                     NatureReleaseDetector.NatureType selectedNature = null;
                     if (!playerNatures.isEmpty()) {
                        List<NatureReleaseDetector.NatureType> natureList = new ArrayList(playerNatures);
                        selectedNature = (NatureReleaseDetector.NatureType)natureList.get(RANDOM.nextInt(natureList.size()));
                     }

                     int baseKills;
                     if (template.getMinKills() == template.getMaxKills()) {
                        baseKills = template.getMinKills();
                     } else {
                        baseKills = template.getMinKills() + RANDOM.nextInt(template.getMaxKills() - template.getMinKills() + 1);
                     }

                     int scaledKills = Math.max(1, Math.round((float)baseKills * getRankKillMultiplier(playerRank)));
                     PvpObjective objective = buildObjective(template, scaledKills, targetVillage, selectedNature, player, world, playerUUID);
                     if (objective == null) {
                        return null;
                     } else {
                        int ninjaXp = calculateReward(template.getBaseNinjaXp(), category, 50, 150, 200, 500, 10, 30, playerRank);
                        int pvpXp = calculateReward(template.getBasePvpXp(), category, 30, 80, 100, 250, 10, 25, playerRank);
                        int ryoReward = calculatePvpRyo(category, playerRank);
                        String name = fillPlaceholders(template.getNamePattern(), playerVillage, targetVillage, selectedNature, scaledKills, (String)null, (String)null);
                        String loreText = template.getLore(playerVillage.teamName, targetVillage.teamName);
                        String description = fillPlaceholders(loreText, playerVillage, targetVillage, selectedNature, scaledKills, (String)null, (String)null);
                        String offerId = "pvp_offer_" + System.currentTimeMillis() + "_" + RANDOM.nextInt(10000);
                        return new PvpMissionOffer(offerId, template.getId(), name, description, objective, ninjaXp, pvpXp, ryoReward, (String)null, playerRank, System.currentTimeMillis(), (String)null);
                     }
                  }
               }
            }
         }
      }
   }

   @Nullable
   private static PvpObjective buildObjective(PvpMissionTemplate template, int kills, VillageHelper.Village targetVillage, @Nullable NatureReleaseDetector.NatureType nature, EntityPlayerMP player, World world, UUID playerUUID) {
      PvpObjective.ObjectiveType type = template.getObjectiveType();
      int timeLimitTicks = template.getTimeLimitTicks();
      switch (type) {
         case KILL_VILLAGE:
            return PvpObjective.killVillage(targetVillage.teamName, kills);
         case KILL_ANY_ENEMY:
            if (timeLimitTicks > 0) {
               return (new PvpObjectiveBuilder(type)).killsRequired(kills).timeLimit(timeLimitTicks).build();
            }

            return PvpObjective.killAnyEnemy(kills);
         case KILL_MULTI_VILLAGE:
            return PvpObjective.killMultiVillage(kills);
         case KILL_WITH_NATURE:
            int natureInt = nature != null ? NatureReleaseDetector.toInt(nature) : 1;
            return PvpObjective.killWithNature(kills, natureInt);
         case KILL_WITH_DOJUTSU:
            return PvpObjective.killWithDojutsu(kills);
         case KILL_HIGHER_LEVEL:
            return PvpObjective.killHigherLevel(kills);
         case KILL_IN_REGION:
            return PvpObjective.killInRegion(targetVillage.teamName, kills);
         case KILL_SPECIFIC_PLAYER:
            UUID targetId = findSuitableTarget(player, targetVillage, world, playerUUID);
            if (targetId == null) {
               return null;
            } else {
               if (timeLimitTicks > 0) {
                  return (new PvpObjectiveBuilder(type)).targetPlayerId(targetId).killsRequired(1).timeLimit(timeLimitTicks).build();
               }

               return PvpObjective.killSpecificPlayer(targetId);
            }
         case MUTUAL_HUNT:
            UUID targetId = findSuitableTarget(player, targetVillage, world, playerUUID);
            if (targetId == null) {
               return null;
            }

            return PvpObjective.mutualHunt(targetId);
         case SURVIVE_DURATION:
            int surviveTime = 12000 + RANDOM.nextInt(6001);
            return PvpObjective.surviveDuration(surviveTime);
         case KILL_STREAK:
            return PvpObjective.killStreak(kills);
         case KILL_DOJUTSU_USER:
            return PvpObjective.killDojutsuUser(kills);
         default:
            return PvpObjective.killAnyEnemy(kills);
      }
   }

   @Nullable
   private static UUID findSuitableTarget(EntityPlayerMP player, VillageHelper.Village targetVillage, World world, UUID excludeUUID) {
      MinecraftServer server = world.getMinecraftServer();
      if (server == null) {
         return null;
      } else {
         List<EntityPlayerMP> candidates = new ArrayList();

         for(EntityPlayerMP other : server.getPlayerList().getPlayers()) {
            if (!other.getUniqueID().equals(excludeUUID)) {
               VillageHelper.Village otherVillage = VillageHelper.getVillage(other);
               if (otherVillage == targetVillage) {
                  int battleXp = getBattleXp(other);
                  if (battleXp >= 100) {
                     candidates.add(other);
                  }
               }
            }
         }

         if (candidates.isEmpty()) {
            return null;
         } else {
            return ((EntityPlayerMP)candidates.get(RANDOM.nextInt(candidates.size()))).getUniqueID();
         }
      }
   }

   private static int getBattleXp(EntityPlayerMP player) {
      try {
         return (int)player.getEntityData().getDouble("battle_experience");
      } catch (Exception var2) {
         return 0;
      }
   }

   private static int calculateReward(int baseReward, PvpMissionTemplate.Category category, int dailyMin, int dailyMax, int weeklyMin, int weeklyMax, int randomMin, int randomMax, QuestDefinition.QuestRank rank) {
      int scaled = (int)((double)baseReward * rank.xpMultiplier);
      int min;
      int max;
      switch (category) {
         case DAILY:
            min = dailyMin;
            max = dailyMax;
            break;
         case WEEKLY:
            min = weeklyMin;
            max = weeklyMax;
            break;
         default:
            min = randomMin;
            max = randomMax;
      }

      return Math.max(min, Math.min(max, scaled));
   }

   private static int calculatePvpRyo(PvpMissionTemplate.Category category, QuestDefinition.QuestRank rank) {
      int min;
      int max;
      switch (rank) {
         case C:
            switch (category) {
               case DAILY:
                  min = 180;
                  max = 400;
                  return min + RANDOM.nextInt(max - min + 1);
               case WEEKLY:
                  min = 800;
                  max = 1400;
                  return min + RANDOM.nextInt(max - min + 1);
               default:
                  min = 70;
                  max = 140;
                  return min + RANDOM.nextInt(max - min + 1);
            }
         case B:
            switch (category) {
               case DAILY:
                  min = 350;
                  max = 700;
                  return min + RANDOM.nextInt(max - min + 1);
               case WEEKLY:
                  min = 1400;
                  max = 2400;
                  return min + RANDOM.nextInt(max - min + 1);
               default:
                  min = 140;
                  max = 300;
                  return min + RANDOM.nextInt(max - min + 1);
            }
         case A:
            switch (category) {
               case DAILY:
                  min = 600;
                  max = 1100;
                  return min + RANDOM.nextInt(max - min + 1);
               case WEEKLY:
                  min = 2400;
                  max = 4200;
                  return min + RANDOM.nextInt(max - min + 1);
               default:
                  min = 250;
                  max = 500;
                  return min + RANDOM.nextInt(max - min + 1);
            }
         case S:
         case S_PLUS:
            switch (category) {
               case DAILY:
                  min = 900;
                  max = 1800;
                  return min + RANDOM.nextInt(max - min + 1);
               case WEEKLY:
                  min = 4200;
                  max = 7200;
                  return min + RANDOM.nextInt(max - min + 1);
               default:
                  min = 400;
                  max = 750;
                  return min + RANDOM.nextInt(max - min + 1);
            }
         default:
            switch (category) {
               case DAILY:
                  min = 100;
                  max = 220;
                  break;
               case WEEKLY:
                  min = 400;
                  max = 700;
                  break;
               default:
                  min = 35;
                  max = 70;
            }
      }

      return min + RANDOM.nextInt(max - min + 1);
   }

   private static float getRankKillMultiplier(QuestDefinition.QuestRank rank) {
      int ord = rank.ordinal();
      return ord < RANK_KILL_MULTIPLIERS.length ? RANK_KILL_MULTIPLIERS[ord] : 1.0F;
   }

   private static String fillPlaceholders(String text, VillageHelper.Village playerVillage, VillageHelper.Village targetVillage, @Nullable NatureReleaseDetector.NatureType nature, int kills, @Nullable String roleTitle, @Nullable String roleDesc) {
      if (text == null) {
         return "";
      } else {
         text = text.replace("{village}", playerVillage.villageName);
         text = text.replace("{targetVillage}", targetVillage.villageName);
         text = text.replace("{landName}", playerVillage.landName);
         text = text.replace("{terrainName}", playerVillage.terrainName);
         text = text.replace("{kills}", String.valueOf(kills));
         text = text.replace("{nature}", nature != null ? getNatureDisplayName(nature) : "Chakra");
         if (roleTitle != null) {
            text = text.replace("{roleTitle}", roleTitle);
         }

         if (roleDesc != null) {
            text = text.replace("{roleDesc}", roleDesc);
         }

         return text;
      }
   }

   private static String getNatureDisplayName(NatureReleaseDetector.NatureType nature) {
      switch (nature) {
         case KATON:
            return "Fire";
         case SUITON:
            return "Water";
         case FUTON:
            return "Wind";
         case RAITON:
            return "Lightning";
         case DOTON:
            return "Earth";
         default:
            return "Chakra";
      }
   }

   @Nullable
   public static PvpMissionTemplate.Category getSubSlotCategory(String subSlot) {
      if (subSlot == null) {
         return null;
      } else if (subSlot.startsWith("pvp_daily")) {
         return PvpMissionTemplate.Category.DAILY;
      } else if (subSlot.startsWith("pvp_weekly")) {
         return PvpMissionTemplate.Category.WEEKLY;
      } else if (subSlot.startsWith("pvp_random")) {
         return PvpMissionTemplate.Category.RANDOM;
      } else {
         return subSlot.equals("pvp_leadership") ? PvpMissionTemplate.Category.LEADERSHIP : null;
      }
   }

   public static String getBaseCategory(String subSlot) {
      if (subSlot == null) {
         return "pvp_random";
      } else if (subSlot.startsWith("pvp_daily")) {
         return "pvp_daily";
      } else if (subSlot.startsWith("pvp_weekly")) {
         return "pvp_weekly";
      } else {
         return subSlot.equals("pvp_leadership") ? "pvp_leadership" : "pvp_random";
      }
   }

   private static List<PvpMissionTemplate> getTemplatePool(PvpMissionTemplate.Category category) {
      switch (category) {
         case DAILY:
            return PvpMissionTemplate.getDaily();
         case WEEKLY:
            return PvpMissionTemplate.getWeekly();
         case RANDOM:
            return PvpMissionTemplate.getRandom();
         case LEADERSHIP:
            return PvpMissionTemplate.getLeadership();
         default:
            return PvpMissionTemplate.getDaily();
      }
   }

   private static int getVillageNature(VillageHelper.Village village) {
      switch (village) {
         case LEAF:
            return 1;
         case SAND:
            return 2;
         case MIST:
            return 3;
         case CLOUD:
            return 4;
         case STONE:
            return 5;
         case RAIN:
            return 3;
         default:
            return 1;
      }
   }

   private static class PvpObjectiveBuilder {
      private final PvpObjective.ObjectiveType type;
      private String targetVillage;
      private UUID targetPlayerId;
      private int killsRequired;
      private int natureType;
      private int timeLimit;

      PvpObjectiveBuilder(PvpObjective.ObjectiveType type) {
         this.type = type;
      }

      PvpObjectiveBuilder targetVillage(String v) {
         this.targetVillage = v;
         return this;
      }

      PvpObjectiveBuilder targetPlayerId(UUID id) {
         this.targetPlayerId = id;
         return this;
      }

      PvpObjectiveBuilder killsRequired(int k) {
         this.killsRequired = k;
         return this;
      }

      PvpObjectiveBuilder natureType(int n) {
         this.natureType = n;
         return this;
      }

      PvpObjectiveBuilder timeLimit(int t) {
         this.timeLimit = t;
         return this;
      }

      PvpObjective build() {
         NBTTagCompound nbt = new NBTTagCompound();
         nbt.setInteger("type", this.type.ordinal());
         if (this.targetVillage != null) {
            nbt.setString("targetVillage", this.targetVillage);
         }

         if (this.targetPlayerId != null) {
            nbt.setString("targetPlayerId", this.targetPlayerId.toString());
         }

         nbt.setInteger("killsRequired", this.killsRequired);
         nbt.setInteger("natureType", this.natureType);
         nbt.setInteger("timeLimit", this.timeLimit);
         return PvpObjective.readFromNBT(nbt);
      }
   }
}
