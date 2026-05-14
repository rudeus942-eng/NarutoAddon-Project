
package net.luck.narutoaddon.OtherCode.endgame.util;

import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class DefenderWaveGenerator {
   private static final String[] LEAF_WAVE_1 = new String[]{"d_rogue_genin_1", "d_rogue_genin_leaf_f", "d_bandit_1", "d_bandit_4", "d_bandit_5"};
   private static final String[] LEAF_WAVE_2 = new String[]{"d_rogue_genin_1", "d_rogue_genin_leaf_f", "d_mercenary_1", "d_bandit_6", "d_bandit_7"};
   private static final String[] LEAF_WAVE_3 = new String[]{"c_rogue_genin_fire", "c_tracker_nin"};
   private static final String LEAF_BOSS = "c_chunin_captain_fire";
   private static final String[] SAND_WAVE_1 = new String[]{"d_sand_bandit", "d_rogue_genin_sand", "d_bandit_2", "d_bandit_7", "d_bandit_8"};
   private static final String[] SAND_WAVE_2 = new String[]{"d_mercenary_2", "d_mercenary_sand", "d_rogue_genin_sand", "d_bandit_6"};
   private static final String[] SAND_WAVE_3 = new String[]{"c_rogue_genin_wind", "c_tracker_nin"};
   private static final String SAND_BOSS = "c_chunin_captain_wind";
   private static final String[] MIST_WAVE_1 = new String[]{"d_mercenary_water", "d_rogue_genin_3", "d_bandit_1", "d_bandit_4", "d_bandit_5"};
   private static final String[] MIST_WAVE_2 = new String[]{"d_mercenary_water", "d_rogue_genin_3", "d_mercenary_1", "d_bandit_2"};
   private static final String[] MIST_WAVE_3 = new String[]{"c_rogue_genin_water", "c_tracker_nin"};
   private static final String MIST_BOSS = "c_chunin_captain_water";
   private static final String[] STONE_WAVE_1 = new String[]{"d_rogue_genin_stone", "d_bandit_1", "d_bandit_3", "d_bandit_6", "d_bandit_8"};
   private static final String[] STONE_WAVE_2 = new String[]{"d_rogue_genin_stone", "d_mercenary_3", "d_mercenary_1", "d_bandit_2", "d_bandit_7"};
   private static final String[] STONE_WAVE_3 = new String[]{"c_rogue_genin_fire", "c_tracker_nin"};
   private static final String STONE_BOSS = "c_chunin_captain_fire";
   private static final String[] CLOUD_WAVE_1 = new String[]{"d_rogue_genin_lightning", "d_bandit_1", "d_bandit_4", "d_bandit_7"};
   private static final String[] CLOUD_WAVE_2 = new String[]{"d_rogue_genin_lightning", "d_mercenary_lightning", "d_bandit_2", "d_bandit_8"};
   private static final String[] CLOUD_WAVE_3 = new String[]{"c_rogue_genin_lightning", "c_tracker_nin"};
   private static final String CLOUD_BOSS = "c_chunin_captain_lightning";
   private static final String[] RAIN_WAVE_1 = new String[]{"d_assassin_1", "d_assassin_2", "d_bandit_1", "d_bandit_4"};
   private static final String[] RAIN_WAVE_2 = new String[]{"d_assassin_1", "d_assassin_2", "d_bandit_3", "d_bandit_8"};
   private static final String[] RAIN_WAVE_3 = new String[]{"c_assassin_fast", "c_tracker_nin"};
   private static final String RAIN_BOSS = "c_assassin_fast";
   private static final String[] AKATSUKI_WAVE_1 = new String[]{"ak_def_grunt_fire", "ak_def_grunt_lightning", "ak_def_grunt_fire"};
   private static final String[] AKATSUKI_WAVE_2 = new String[]{"ak_def_enforcer_fire", "ak_def_enforcer_water", "ak_def_elite"};
   private static final String[] AKATSUKI_WAVE_3 = new String[]{"ak_def_elite", "ak_def_captain"};
   private static final String AKATSUKI_BOSS = "ak_def_boss";
   private static final String[] FALLBACK_WAVE_1 = new String[]{"d_bandit_1", "d_bandit_2", "d_bandit_4", "d_bandit_5", "d_rogue_genin_1"};
   private static final String[] FALLBACK_WAVE_2 = new String[]{"d_rogue_genin_2", "d_mercenary_1", "d_mercenary_water", "d_bandit_6", "d_bandit_7"};
   private static final String[] FALLBACK_WAVE_3 = new String[]{"c_rogue_genin_fire", "c_chunin_captain_fire", "c_tracker_nin"};
   private static final String FALLBACK_BOSS = "c_chunin_captain_fire";
   private static final String[] LEAF_TEXTURES = new String[]{"inftsukaddon:textures/leaf1.png", "inftsukaddon:textures/leaf2.png", "inftsukaddon:textures/leaf3.png", "inftsukaddon:textures/femaleleaf1.png", "inftsukaddon:textures/femaleleaf2.png", "inftsukaddon:textures/femaleleaf3.png"};
   private static final String[] SAND_TEXTURES = new String[]{"inftsukaddon:textures/sand1.png", "inftsukaddon:textures/sand2.png", "inftsukaddon:textures/sand3.png", "inftsukaddon:textures/sand4.png", "inftsukaddon:textures/femalesand1.png", "inftsukaddon:textures/sand_shinobi.png"};
   private static final String[] MIST_TEXTURES = new String[]{"inftsukaddon:textures/mist1.png"};
   private static final String[] STONE_TEXTURES = new String[]{"inftsukaddon:textures/stone1.png", "inftsukaddon:textures/stone2.png", "inftsukaddon:textures/stone3.png", "inftsukaddon:textures/femalestone1.png"};
   private static final String[] CLOUD_TEXTURES = new String[]{"inftsukaddon:textures/cloud1.png", "inftsukaddon:textures/cloud2.png", "inftsukaddon:textures/cloud3.png"};
   private static final String[] RAIN_TEXTURES = new String[]{"inftsukaddon:textures/rain_clan_leader.png"};
   private static final String[] AKATSUKI_TEXTURES = new String[]{"inftsukaddon:textures/akatsukiguard.png", "inftsukaddon:textures/itachi1.png", "inftsukaddon:textures/kisame1.png", "inftsukaddon:textures/deidara.png", "inftsukaddon:textures/hidan.png", "inftsukaddon:textures/kakuzu.png", "inftsukaddon:textures/sasori.png"};
   private static final int WAVE_1_COUNT = 3;
   private static final int WAVE_2_COUNT = 4;
   private static final int WAVE_3_COUNT = 5;
   private static final float WAVE_2_THRESHOLD = 35.0F;
   private static final float WAVE_3_THRESHOLD = 70.0F;
   private static final double MIN_SPAWN_OFFSET = (double)10.0F;
   private static final double MAX_SPAWN_OFFSET = (double)30.0F;
   private static final long WAVE_RESPAWN_COOLDOWN_TICKS = 1200L;
   private static final double TIER_1_HP = (double)325.0F;
   private static final double TIER_1_DMG = (double)7.0F;
   private static final double TIER_1_SPEED = 0.34;
   private static final int TIER_1_RYO_MIN = 20;
   private static final int TIER_1_RYO_MAX = 30;
   private static final double TIER_2_HP = (double)650.0F;
   private static final double TIER_2_DMG = (double)11.0F;
   private static final double TIER_2_SPEED = 0.38;
   private static final int TIER_2_RYO_MIN = 30;
   private static final int TIER_2_RYO_MAX = 50;
   private static final double TIER_3_HP = (double)1300.0F;
   private static final double TIER_3_DMG = (double)16.0F;
   private static final double TIER_3_SPEED = 0.42;
   private static final int TIER_3_RYO_MIN = 50;
   private static final int TIER_3_RYO_MAX = 80;
   private static final double TIER_4_HP = (double)1590.0F;
   private static final double TIER_4_DMG = (double)22.0F;
   private static final double TIER_4_SPEED = 0.46;
   private static final int TIER_4_RYO_MIN = 80;
   private static final int TIER_4_RYO_MAX = 120;
   private static final double SPECIALIST_HP = (double)3410.0F;
   private static final double SPECIALIST_DMG = (double)30.0F;
   private static final double SPECIALIST_SPEED = 0.48;
   private static final int SPECIALIST_RYO_MIN = 100;
   private static final int SPECIALIST_RYO_MAX = 150;
   private static final int BOSS_SPAWN_DELAY_TICKS = 60;
   private static final Random rand = new Random();
   private static final Map<String, Map<Integer, Long>> waveDefeatTimestamps = new HashMap();

   public static int getWaveForProgress(float captureProgress) {
      if (captureProgress >= 70.0F) {
         return 3;
      } else {
         return captureProgress >= 35.0F ? 2 : 1;
      }
   }

   public static void recordWaveDefeated(String zoneId, int waveNumber, long worldTick) {
      ((Map)waveDefeatTimestamps.computeIfAbsent(zoneId, (k) -> new HashMap())).put(waveNumber, worldTick);
   }

   public static boolean isWaveOnCooldown(String zoneId, int waveNumber, long currentTick) {
      Map<Integer, Long> zoneTimestamps = (Map)waveDefeatTimestamps.get(zoneId);
      if (zoneTimestamps == null) {
         return false;
      } else {
         Long defeatTick = (Long)zoneTimestamps.get(waveNumber);
         if (defeatTick == null) {
            return false;
         } else {
            return currentTick - defeatTick < 1200L;
         }
      }
   }

   public static void clearCooldowns(String zoneId) {
      waveDefeatTimestamps.remove(zoneId);
   }

   public static List<UUID> spawnWave(World world, WaveContext ctx, int waveNumber, String defenderVillage) {
      return spawnWave(world, ctx, waveNumber, defenderVillage, (double)ctx.centerX, (double)ctx.centerZ, -1);
   }

   public static List<UUID> spawnWave(World world, WaveContext ctx, int waveNumber, String defenderVillage, double spawnCenterX, double spawnCenterZ) {
      return spawnWave(world, ctx, waveNumber, defenderVillage, spawnCenterX, spawnCenterZ, -1);
   }

   public static List<UUID> spawnWave(World world, WaveContext ctx, int waveNumber, String defenderVillage, double spawnCenterX, double spawnCenterZ, int countOverride) {
      if (!world.isRemote && waveNumber >= 1 && waveNumber <= 3) {
         if (isWaveOnCooldown(ctx.zoneId, waveNumber, world.getTotalWorldTime())) {
            return Collections.emptyList();
         } else {
            String[] configPool = getConfigsForWave(waveNumber, defenderVillage);
            int count = countOverride > 0 ? countOverride : getCountForWave(waveNumber);
            int defenderTier = ctx.defenderTier;
            List<UUID> spawnedUUIDs = new ArrayList();
            int regularCount = waveNumber == 3 ? count - 1 : count;

            for(int i = 0; i < regularCount; ++i) {
               String configId = configPool[rand.nextInt(configPool.length)];
               int ryoDrop = getRyoDropForTier(defenderTier);
               Entity entity = spawnDefenderNpc(world, ctx, configId, defenderVillage, waveNumber, ryoDrop, 0, spawnCenterX, spawnCenterZ, defenderTier);
               if (entity != null) {
                  spawnedUUIDs.add(entity.getUniqueID());
               }
            }

            if (waveNumber == 3) {
               String bossConfigId = getBossConfigForVillage(defenderVillage);
               int bossTier = Math.max(defenderTier, 4);
               int bossRyo = getRyoDropForTier(bossTier);
               Entity boss = spawnDefenderNpc(world, ctx, bossConfigId, defenderVillage, waveNumber, bossRyo, 60, spawnCenterX, spawnCenterZ, bossTier);
               if (boss != null) {
                  spawnedUUIDs.add(boss.getUniqueID());
               }
            }

            return spawnedUUIDs;
         }
      } else {
         return Collections.emptyList();
      }
   }

   public static List<UUID> spawnSpecialists(World world, WaveContext ctx, String defenderVillage, double spawnCenterX, double spawnCenterZ, int count) {
      if (!world.isRemote && count > 0) {
         String bossConfig = getBossConfigForVillage(defenderVillage);
         List<UUID> spawnedUUIDs = new ArrayList();

         for(int i = 0; i < count; ++i) {
            int ryoDrop = 100 + rand.nextInt(51);
            Entity entity = spawnDefenderNpc(world, ctx, bossConfig, defenderVillage, 3, ryoDrop, 0, spawnCenterX, spawnCenterZ, 5);
            if (entity != null) {
               spawnedUUIDs.add(entity.getUniqueID());
            }
         }

         return spawnedUUIDs;
      } else {
         return Collections.emptyList();
      }
   }

   public static void despawnAll(World world, List<UUID> entityUUIDs) {
      if (!world.isRemote && entityUUIDs != null && !entityUUIDs.isEmpty()) {
         Set<UUID> uuidsToRemove = new HashSet(entityUUIDs);

         for(Entity entity : world.loadedEntityList) {
            if (!entity.isDead && uuidsToRemove.contains(entity.getUniqueID())) {
               entity.setDead();
               uuidsToRemove.remove(entity.getUniqueID());
               if (uuidsToRemove.isEmpty()) {
                  break;
               }
            }
         }

      }
   }

   public static String[] getConfigsForWave(int waveNumber, String village) {
      switch (village != null ? village.toLowerCase() : "") {
         case "leaf":
         case "konoha":
            return waveNumber == 1 ? LEAF_WAVE_1 : (waveNumber == 2 ? LEAF_WAVE_2 : LEAF_WAVE_3);
         case "sand":
         case "sunagakure":
         case "suna":
            return waveNumber == 1 ? SAND_WAVE_1 : (waveNumber == 2 ? SAND_WAVE_2 : SAND_WAVE_3);
         case "mist":
         case "kirigakure":
         case "kiri":
            return waveNumber == 1 ? MIST_WAVE_1 : (waveNumber == 2 ? MIST_WAVE_2 : MIST_WAVE_3);
         case "stone":
         case "iwagakure":
         case "iwa":
            return waveNumber == 1 ? STONE_WAVE_1 : (waveNumber == 2 ? STONE_WAVE_2 : STONE_WAVE_3);
         case "cloud":
         case "kumogakure":
         case "kumo":
            return waveNumber == 1 ? CLOUD_WAVE_1 : (waveNumber == 2 ? CLOUD_WAVE_2 : CLOUD_WAVE_3);
         case "rain":
         case "amegakure":
         case "ame":
            return waveNumber == 1 ? RAIN_WAVE_1 : (waveNumber == 2 ? RAIN_WAVE_2 : RAIN_WAVE_3);
         case "akatsuki":
            return waveNumber == 1 ? AKATSUKI_WAVE_1 : (waveNumber == 2 ? AKATSUKI_WAVE_2 : AKATSUKI_WAVE_3);
         default:
            return waveNumber == 1 ? FALLBACK_WAVE_1 : (waveNumber == 2 ? FALLBACK_WAVE_2 : FALLBACK_WAVE_3);
      }
   }

   private static String getBossConfigForVillage(String village) {
      switch (village != null ? village.toLowerCase() : "") {
         case "leaf":
         case "konoha":
            return "c_chunin_captain_fire";
         case "sand":
         case "sunagakure":
         case "suna":
            return "c_chunin_captain_wind";
         case "mist":
         case "kirigakure":
         case "kiri":
            return "c_chunin_captain_water";
         case "stone":
         case "iwagakure":
         case "iwa":
            return "c_chunin_captain_fire";
         case "cloud":
         case "kumogakure":
         case "kumo":
            return "c_chunin_captain_lightning";
         case "rain":
         case "amegakure":
         case "ame":
            return "c_assassin_fast";
         case "akatsuki":
            return "ak_def_boss";
         default:
            return "c_chunin_captain_fire";
      }
   }

   private static int getCountForWave(int waveNumber) {
      switch (waveNumber) {
         case 1:
            return 3;
         case 2:
            return 4;
         case 3:
            return 5;
         default:
            return 3;
      }
   }

   private static int getRyoDropForTier(int defenderTier) {
      switch (defenderTier) {
         case 1:
            return 20 + rand.nextInt(11);
         case 2:
            return 30 + rand.nextInt(21);
         case 3:
            return 50 + rand.nextInt(31);
         case 4:
            return 80 + rand.nextInt(41);
         default:
            return 20;
      }
   }

   private static double getHpForTier(int tier) {
      switch (tier) {
         case 1:
            return (double)325.0F;
         case 2:
            return (double)650.0F;
         case 3:
            return (double)1300.0F;
         case 4:
            return (double)1590.0F;
         case 5:
            return (double)3410.0F;
         default:
            return (double)325.0F;
      }
   }

   private static double getDmgForTier(int tier) {
      switch (tier) {
         case 1:
            return (double)7.0F;
         case 2:
            return (double)11.0F;
         case 3:
            return (double)16.0F;
         case 4:
            return (double)22.0F;
         case 5:
            return (double)30.0F;
         default:
            return (double)7.0F;
      }
   }

   private static float getTrueDmgMultForTier(int tier) {
      switch (tier) {
         case 1:
            return 1.0F;
         case 2:
            return 1.3F;
         case 3:
            return 1.6F;
         case 4:
            return 2.0F;
         case 5:
            return 2.5F;
         default:
            return 1.0F;
      }
   }

   private static double getSpeedForTier(int tier) {
      switch (tier) {
         case 1:
            return 0.34;
         case 2:
            return 0.38;
         case 3:
            return 0.42;
         case 4:
            return 0.46;
         case 5:
            return 0.48;
         default:
            return 0.34;
      }
   }

   private static Entity spawnDefenderNpc(World world, WaveContext ctx, String configId, String defenderVillage, int waveNumber, int ryoDrop, int spawnDelayTicks, double spawnCenterX, double spawnCenterZ, int statTierOverride) {
      NpcConfig config = NpcConfigRegistry.get(configId);
      if (config == null) {
         System.out.println("[DefenderWave] Unknown NPC config: " + configId);
         return null;
      } else {
         Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(config.getEntityRegistryId()), world);
         if (entity == null) {
            System.out.println("[DefenderWave] Failed to create entity for config: " + configId);
            return null;
         } else {
            double angle = rand.nextDouble() * Math.PI * (double)2.0F;
            double offset = (double)10.0F + rand.nextDouble() * (double)20.0F;
            double spawnX = spawnCenterX + (double)0.5F + Math.cos(angle) * offset;
            double spawnZ = spawnCenterZ + (double)0.5F + Math.sin(angle) * offset;
            int safeY = findSafeSpawnY(world, (int)spawnX, (int)spawnZ);
            if (safeY <= 0) {
               System.out.println("[DefenderWave] No safe spawn Y at " + (int)spawnX + ", " + (int)spawnZ + " for config: " + configId);
               return null;
            } else {
               float yaw = (float)(rand.nextDouble() * (double)360.0F);
               entity.setLocationAndAngles(spawnX, (double)safeY, spawnZ, yaw, 0.0F);
               if (entity instanceof EntityLivingBase) {
                  ((EntityLivingBase)entity).rotationYawHead = yaw;
                  ((EntityLivingBase)entity).renderYawOffset = yaw;
               }

               NBTTagCompound entityData = entity.getEntityData();
               entityData.setBoolean("territoryEntity", true);
               entityData.setBoolean("territoryDefender", true);
               entityData.setString("territoryZoneId", ctx.zoneId);
               entityData.setString("defenderVillage", defenderVillage != null ? defenderVillage : "");
               entityData.setString("npcConfigId", configId);
               entityData.setInteger("territoryWave", waveNumber);
               entityData.setInteger("territoryRyoDrop", ryoDrop);
               entityData.setBoolean("PersistenceRequired", true);
               if (entity instanceof INpcConfigurable) {
                  ((INpcConfigurable)entity).applyNpcConfig(config);
               }

               if (statTierOverride > 0 && entity instanceof EntityLivingBase) {
                  EntityLivingBase living = (EntityLivingBase)entity;
                  double hp = getHpForTier(statTierOverride);
                  double dmg = getDmgForTier(statTierOverride);
                  double spd = getSpeedForTier(statTierOverride);
                  living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(hp);
                  living.setHealth((float)hp);
                  living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(dmg);
                  living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(spd);
               }

               if (statTierOverride > 0 && entity instanceof QuestNpcBase) {
                  QuestNpcBase npc = (QuestNpcBase)entity;
                  int ct = Math.min(statTierOverride, 4);
                  npc.setCombatTier(ct);
                  npc.setTrueDamageMultiplier(getTrueDmgMultForTier(statTierOverride));
               }

               if ("akatsuki".equalsIgnoreCase(defenderVillage) && entity instanceof EntityLivingBase) {
                  EntityLivingBase akLiving = (EntityLivingBase)entity;
                  double currentDmg = akLiving.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                  akLiving.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(currentDmg * (double)1.25F);
                  if (entity instanceof QuestNpcBase) {
                     QuestNpcBase akNpc = (QuestNpcBase)entity;
                     float currentTrueMult = getTrueDmgMultForTier(statTierOverride > 0 ? statTierOverride : 1);
                     akNpc.setTrueDamageMultiplier(currentTrueMult * 1.3F);
                  }
               }

               if (entity instanceof QuestNpcBase) {
                  String villageSkin = getRandomVillageTexture(defenderVillage);
                  if (villageSkin != null) {
                     ((QuestNpcBase)entity).setTextureOverride(villageSkin);
                  }
               }

               if (entity instanceof EntityCreature) {
                  ((EntityCreature)entity).getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((double)100.0F);
               }

               if (spawnDelayTicks > 0) {
                  entityData.setInteger("spawnDelayTicks", spawnDelayTicks);
               }

               if (entity instanceof EntityLiving) {
                  ((EntityLiving)entity).enablePersistence();
               }

               world.spawnEntity(entity);
               return entity;
            }
         }
      }
   }

   public static int findSafeSpawnY(World world, int x, int z) {
      int topY = world.getHeight(x, z);
      if (topY <= 1) {
         topY = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
         if (topY <= 1) {
            return 64;
         }
      }

      for(int y = topY - 1; y > 0; --y) {
         IBlockState state = world.getBlockState(new BlockPos(x, y, z));
         Material mat = state.getMaterial();
         if (mat != Material.AIR && mat != Material.PLANTS && mat != Material.VINE && mat != Material.LEAVES && mat != Material.WOOD && mat != Material.SNOW && mat != Material.CARPET && mat != Material.WEB && mat != Material.WATER) {
            int spawnY = y + 1;
            Material feetMat = world.getBlockState(new BlockPos(x, spawnY, z)).getMaterial();
            Material headMat = world.getBlockState(new BlockPos(x, spawnY + 1, z)).getMaterial();
            boolean feetClear = feetMat == Material.AIR || feetMat == Material.PLANTS || feetMat == Material.VINE || feetMat == Material.SNOW || feetMat == Material.LEAVES || feetMat == Material.WEB;
            boolean headClear = headMat == Material.AIR || headMat == Material.PLANTS || headMat == Material.VINE || headMat == Material.SNOW || headMat == Material.LEAVES || headMat == Material.WEB;
            if (feetClear && headClear) {
               return spawnY;
            }
         }
      }

      return 64;
   }

   private static String getRandomVillageTexture(String village) {
      String[] pool = getTexturePoolForVillage(village);
      return pool != null && pool.length != 0 ? pool[rand.nextInt(pool.length)] : null;
   }

   private static String[] getTexturePoolForVillage(String village) {
      if (village == null) {
         return null;
      } else {
         switch (village.toLowerCase()) {
            case "leaf":
            case "konoha":
               return LEAF_TEXTURES;
            case "sand":
            case "sunagakure":
            case "suna":
               return SAND_TEXTURES;
            case "mist":
            case "kirigakure":
            case "kiri":
               return MIST_TEXTURES;
            case "stone":
            case "iwagakure":
            case "iwa":
               return STONE_TEXTURES;
            case "cloud":
            case "kumogakure":
            case "kumo":
               return CLOUD_TEXTURES;
            case "rain":
            case "amegakure":
            case "ame":
               return RAIN_TEXTURES;
            case "akatsuki":
               return AKATSUKI_TEXTURES;
            default:
               return null;
         }
      }
   }

   public static final class WaveContext {
      public final String zoneId;
      public final int centerX;
      public final int centerZ;
      public final int defenderTier;

      public WaveContext(String zoneId, int centerX, int centerZ, int defenderTier) {
         this.zoneId = zoneId != null ? zoneId : "wave";
         this.centerX = centerX;
         this.centerZ = centerZ;
         this.defenderTier = Math.max(1, Math.min(5, defenderTier));
      }
   }
}
