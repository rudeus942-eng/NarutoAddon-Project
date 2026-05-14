
package net.luck.narutoaddon.OtherCode.endgame.contract;

import net.luck.narutoaddon.OtherCode.endgame.EndgameModInit;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameCombatMessage;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostDifficultyTier;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostRegistry;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.quest.core.TerrainCache;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointData;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointSpawnLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ContractManager {
   public static final int POOL_SIZE = 20;
   public static final int MIN_POOL_CHECK_INTERVAL_TICKS = 40;
   public static final int[] RANK_DAILY_CAP = new int[]{0, 0, 5, 2, 1};
   private static final double[] RANK_DMG_FLOOR = new double[]{(double)10.0F, (double)15.0F, (double)20.0F, (double)30.0F, (double)40.0F};
   private static final Set<String> BLACKLIST_CONFIG_IDS = new HashSet(Arrays.asList("arc10_kigiri", "arc10_kigiri_cursed"));
   private static final float MIN_TRUE_DAMAGE_SPLIT = 0.4F;
   private static final ContractManager INSTANCE = new ContractManager();
   private static final Random RANDOM = new Random();
   private static final String[] WORLD_BOSS_IDS = new String[]{"wb_hidan", "wb_zabuza", "world_boss_sasori", "world_boss_deidara", "wb_kokuo", "wb_saiken", "wb_gyuki"};
   private static final String[] STORY_BOSS_IDS = new String[]{"zabuza_bridge", "orochimaru_disguised", "orochimaru_kazekage", "tobirama_edo", "tobirama_edo_duo", "orochimaru_final", "itachi_hostile", "kisame_hostile", "sasori_kazekage", "sasori_hundred", "hidan_first_encounter", "hidan_ritual_master", "hidan_final", "kakuzu_five_hearts", "kakuzu_weakened", "orochimaru_hostile", "kabuto_hostile", "arc11_konan", "arc11_deidara_boss", "arc11_pain_animal", "arc11_pain_human", "arc11_pain_preta", "arc11_kisame", "arc11_itachi_phantom"};
   private final AtomicInteger tickCounter = new AtomicInteger();
   private final Set<String> hardTargetIds = new LinkedHashSet();
   private boolean seeded = false;
   private static final double CONTRACT_BAR_RANGE = (double)64.0F;
   private static final double CONTRACT_BAR_RANGE_SQ = (double)4096.0F;

   private ContractManager() {
   }

   public static ContractManager getInstance() {
      return INSTANCE;
   }

   public static void reset() {
      INSTANCE.hardTargetIds.clear();
      INSTANCE.seeded = false;
      INSTANCE.tickCounter.set(0);
   }

   private void seedHardTargets() {
      if (!this.seeded) {
         this.seeded = true;
         this.hardTargetIds.clear();
         int rejPassive = 0;
         int rejHpFloor = 0;
         int rejDmgFloor = 0;
         int rejTrueDmg = 0;
         int rejBlacklist = 0;
         int outpostCount = 0;

         for(OutpostRegistry.EncounterGroup group : OutpostRegistry.getAllEncounterGroups()) {
            for(OutpostDifficultyTier tier : OutpostDifficultyTier.values()) {
               for(String configId : group.getBossConfigIds(tier)) {
                  NpcConfig cfg = NpcConfigRegistry.get(configId);
                  if (cfg != null) {
                     int rr = this.rejectionReason(cfg);
                     if (rr == 1) {
                        ++rejPassive;
                     } else if (rr == 2) {
                        ++rejHpFloor;
                     } else if (rr == 3) {
                        ++rejDmgFloor;
                     } else if (rr == 4) {
                        ++rejTrueDmg;
                     } else if (rr == 5) {
                        ++rejBlacklist;
                     } else {
                        this.hardTargetIds.add(configId);
                        ++outpostCount;
                     }
                  }
               }
            }
         }

         int worldBossCount = 0;

         for(String id : WORLD_BOSS_IDS) {
            NpcConfig cfg = NpcConfigRegistry.get(id);
            if (cfg != null) {
               int rr = this.rejectionReason(cfg);
               if (rr == 1) {
                  ++rejPassive;
               } else if (rr == 2) {
                  ++rejHpFloor;
               } else if (rr == 3) {
                  ++rejDmgFloor;
               } else if (rr == 4) {
                  ++rejTrueDmg;
               } else if (rr == 5) {
                  ++rejBlacklist;
               } else {
                  this.hardTargetIds.add(id);
                  ++worldBossCount;
               }
            }
         }

         int storyCount = 0;

         for(String id : STORY_BOSS_IDS) {
            NpcConfig cfg = NpcConfigRegistry.get(id);
            if (cfg != null) {
               int rr = this.rejectionReason(cfg);
               if (rr == 1) {
                  ++rejPassive;
               } else if (rr == 2) {
                  ++rejHpFloor;
               } else if (rr == 3) {
                  ++rejDmgFloor;
               } else if (rr == 4) {
                  ++rejTrueDmg;
               } else if (rr == 5) {
                  ++rejBlacklist;
               } else {
                  this.hardTargetIds.add(id);
                  ++storyCount;
               }
            }
         }

         EnumMap<ContractDefinition.Rank, Integer> rankCounts = new EnumMap(ContractDefinition.Rank.class);

         for(ContractDefinition.Rank r : ContractDefinition.Rank.values()) {
            rankCounts.put(r, 0);
         }

         for(String id : this.hardTargetIds) {
            NpcConfig cfg = NpcConfigRegistry.get(id);
            if (cfg != null) {
               rankCounts.merge(this.rankForConfig(cfg), 1, Integer::sum);
            }
         }

         System.out.println("[ContractBoard] IR-198 hard-target pool: " + this.hardTargetIds.size() + " total");
         System.out.println("  outpost: " + outpostCount + " | world boss: " + worldBossCount + " | story: " + storyCount);
         System.out.println("  rejected: " + rejPassive + " passive/dialogue, " + rejHpFloor + " below HP floor, " + rejDmgFloor + " below rank damage floor, " + rejTrueDmg + " below true-damage floor (" + 0.4F + "), " + rejBlacklist + " blacklisted");
         System.out.println("  rank counts: D=" + rankCounts.get(ContractDefinition.Rank.D) + " C=" + rankCounts.get(ContractDefinition.Rank.C) + " B=" + rankCounts.get(ContractDefinition.Rank.B) + " A=" + rankCounts.get(ContractDefinition.Rank.A) + " S=" + rankCounts.get(ContractDefinition.Rank.S));
         this.warnOnStarvedRanks();
      }
   }

   private boolean isViableContractTarget(NpcConfig cfg) {
      return this.rejectionReason(cfg) == 0;
   }

   private int rejectionReason(NpcConfig cfg) {
      if (BLACKLIST_CONFIG_IDS.contains(cfg.getConfigId())) {
         return 5;
      } else if (cfg.getBehavior() != NpcConfig.Behavior.HOSTILE) {
         return 1;
      } else {
         double hp = cfg.getMaxHealth();
         double dmg = cfg.getAttackDamage();
         if (!(hp < (double)20000.0F) && !(dmg < (double)20.0F)) {
            ContractDefinition.Rank r;
            if (hp >= (double)100000.0F) {
               r = ContractDefinition.Rank.S;
            } else if (hp >= (double)50000.0F) {
               r = ContractDefinition.Rank.A;
            } else {
               r = ContractDefinition.Rank.B;
            }

            if (dmg < RANK_DMG_FLOOR[r.ordinal()]) {
               return 3;
            } else {
               return cfg.getTrueDamageSplit() < 0.4F ? 4 : 0;
            }
         } else {
            return 2;
         }
      }
   }

   private void warnOnStarvedRanks() {
      EnumMap<ContractDefinition.Rank, Integer> counts = new EnumMap(ContractDefinition.Rank.class);

      for(ContractDefinition.Rank r : ContractDefinition.Rank.values()) {
         counts.put(r, 0);
      }

      for(String id : this.hardTargetIds) {
         NpcConfig cfg = NpcConfigRegistry.get(id);
         if (cfg != null) {
            counts.merge(this.rankForConfig(cfg), 1, Integer::sum);
         }
      }

      for(Map.Entry<ContractDefinition.Rank, Integer> e : counts.entrySet()) {
         ContractDefinition.Rank r = (ContractDefinition.Rank)e.getKey();
         if (r != ContractDefinition.Rank.D && r != ContractDefinition.Rank.C && (Integer)e.getValue() == 0) {
            System.out.println("[ContractBoard] WARNING: rank " + r.name() + " has no candidates — slots will fall back to the next-lower rank.");
         }
      }

   }

   private ContractDefinition.Rank rankForConfig(NpcConfig cfg) {
      double hp = cfg.getMaxHealth();
      if (hp >= (double)100000.0F) {
         return ContractDefinition.Rank.S;
      } else {
         return hp >= (double)50000.0F ? ContractDefinition.Rank.A : ContractDefinition.Rank.B;
      }
   }

   private ContractDefinition generatePveContract(World world, ContractSavedData data, Set<String> usedConfigs) {
      this.seedHardTargets();
      if (this.hardTargetIds.isEmpty()) {
         return null;
      } else {
         List<String> pool = new ArrayList(this.hardTargetIds);
         pool.removeAll(usedConfigs);
         if (pool.isEmpty()) {
            pool = new ArrayList(this.hardTargetIds);
         }

         String configId = (String)pool.get(RANDOM.nextInt(pool.size()));
         NpcConfig cfg = NpcConfigRegistry.get(configId);
         if (cfg == null) {
            return null;
         } else {
            ContractDefinition.Rank rank = this.rankForConfig(cfg);
            BlockPos safe = this.pickSafeContractLocation(world);
            int hintX = safe.getX();
            int hintZ = safe.getZ();
            String locationHint = this.locationHintFor(hintX, hintZ);
            String id = data.nextContractId();
            long now = System.currentTimeMillis();
            return ContractDefinition.pveMark(id, rank, cfg.getDisplayName(), configId, hintX, hintZ, locationHint, now);
         }
      }
   }

   private BlockPos pickSafeContractLocation(World world) {
      TerrainCache terrain = world != null ? TerrainCache.get(world) : null;
      int min = -6000;
      int range = 12001;

      for(int attempt = 0; attempt < 20; ++attempt) {
         int x = -6000 + RANDOM.nextInt(12001);
         int z = -6000 + RANDOM.nextInt(12001);
         if (terrain == null || !terrain.isWater(world, x, z)) {
            BlockPos p = WaypointSpawnLogic.findLandPosition(world, new BlockPos(x, 0, z));
            int y = WaypointSpawnLogic.findGroundY(world, p.getX(), p.getZ());
            if (y > 0) {
               return new BlockPos(p.getX(), y, p.getZ());
            }
         }
      }

      int x = -6000 + RANDOM.nextInt(12001);
      int z = -6000 + RANDOM.nextInt(12001);
      BlockPos p = WaypointSpawnLogic.findLandPosition(world, new BlockPos(x, 0, z));
      int y = WaypointSpawnLogic.findGroundY(world, p.getX(), p.getZ());
      return new BlockPos(p.getX(), Math.max(y, 64), p.getZ());
   }

   private String locationHintFor(int x, int z) {
      String nsEw = (z < -2000 ? "north" : (z > 2000 ? "south" : "central")) + "-" + (x < -2000 ? "west" : (x > 2000 ? "east" : "central"));
      return nsEw + " region (" + x + ", " + z + ")";
   }

   public void maintainPool(World world) {
      if (world != null && !world.isRemote) {
         ContractSavedData data = ContractSavedData.get(world);

         for(ContractDefinition c : data.getActiveContracts()) {
            ContractDefinition.Rank r = c.getRank();
            if (r == ContractDefinition.Rank.D || r == ContractDefinition.Rank.C) {
               data.removeContract(c.getId());
            }
         }

         List<ContractDefinition> var8 = data.getActiveContracts();
         if (var8.size() < 20) {
            Set<String> usedConfigs = new HashSet();

            for(ContractDefinition c : var8) {
               if (c.getTargetConfigId() != null) {
                  usedConfigs.add(c.getTargetConfigId());
               }
            }

            int needed = 20 - var8.size();

            for(int i = 0; i < needed; ++i) {
               ContractDefinition pve = this.generatePveContract(world, data, usedConfigs);
               if (pve == null) {
                  break;
               }

               if (pve.getTargetConfigId() != null) {
                  usedConfigs.add(pve.getTargetConfigId());
               }

               data.addContract(pve);
            }

         }
      }
   }

   public void onServerTick(World world) {
      if (world != null && !world.isRemote) {
         this.syncContractCombatBars(world);
         int t = this.tickCounter.incrementAndGet();
         if (t % 40 == 0) {
            this.maintainPool(world);
         }
      }
   }

   private void syncContractCombatBars(World world) {
      if (world instanceof WorldServer) {
         WorldServer ws = (WorldServer)world;
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            ContractSavedData data = ContractSavedData.get(world);

            for(UUID playerId : new ArrayList(this.acceptingPlayerIds(data))) {
               EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
               if (player != null) {
                  UUID entityUuid = data.getSpawnedTargetEntity(playerId);
                  if (entityUuid != null) {
                     Entity entity = ws.getEntityFromUuid(entityUuid);
                     if (entity instanceof EntityLivingBase && entity.isEntityAlive() && player.world == entity.world && !(player.getDistanceSq(entity) > (double)4096.0F)) {
                        EntityLivingBase boss = (EntityLivingBase)entity;
                        String acceptedContractId = data.getAcceptedContractId(playerId);
                        ContractDefinition activeContract = acceptedContractId != null ? data.findContract(acceptedContractId) : null;
                        String name = entity.getName();
                        int themeColor = 0;
                        int accentColor = 0;
                        if (activeContract != null) {
                           NpcConfig cfg = NpcConfigRegistry.get(activeContract.getTargetConfigId());
                           if (cfg != null) {
                              name = cfg.getDisplayName();
                              if (cfg.hasHealthBarColors()) {
                                 themeColor = cfg.getThemeColor();
                                 accentColor = cfg.getAccentColor();
                              }
                           }
                        }

                        EndgameCombatMessage msg = new EndgameCombatMessage(entity.getEntityId(), name, boss.getHealth(), boss.getMaxHealth(), 0, (byte)3, themeColor, accentColor);
                        EndgameModInit.NETWORK.sendTo(msg, player);
                     }
                  }
               }
            }

         }
      }
   }

   private Set<UUID> acceptingPlayerIds(ContractSavedData data) {
      Set<UUID> out = new HashSet();

      for(ContractDefinition c : data.getActiveContracts()) {
         UUID acc = data.findAcceptor(c.getId());
         if (acc != null) {
            out.add(acc);
         }
      }

      return out;
   }

   public boolean acceptContract(EntityPlayerMP player, String contractId) {
      ContractSavedData data = ContractSavedData.get(player.world);
      if (data.getAcceptedContractId(player.getUniqueID()) != null) {
         player.sendMessage(new TextComponentString("§cYou already have an active contract. Complete or abandon it first."));
         return false;
      } else {
         ContractDefinition c = data.findContract(contractId);
         if (c == null) {
            player.sendMessage(new TextComponentString("§cThat contract is no longer available."));
            return false;
         } else {
            UUID existingAcceptor = data.findAcceptor(contractId);
            if (existingAcceptor != null && !existingAcceptor.equals(player.getUniqueID())) {
               player.sendMessage(new TextComponentString("§cThat contract is already claimed by another shinobi. Pick a different one."));
               return false;
            } else {
               data.acceptContract(player.getUniqueID(), contractId, System.currentTimeMillis());
               Entity spawned = this.spawnContractTarget(player.world, c);
               if (spawned != null) {
                  data.setSpawnedTargetEntity(player.getUniqueID(), spawned.getUniqueID());
               } else {
                  player.sendMessage(new TextComponentString("§eWarning: failed to pre-spawn contract target — report if this keeps happening."));
               }

               int wpX = c.getHintX();
               int wpZ = c.getHintZ();
               int wpY = spawned != null ? (int)Math.round(spawned.posY) : WaypointSpawnLogic.findGroundY(player.world, wpX, wpZ);
               if (wpY <= 0) {
                  wpY = 64;
               }

               QuestManager.getInstance().getWaypointManager().setWaypoint(player, "contract_mission", new WaypointData(new BlockPos(wpX, wpY, wpZ), WaypointData.WaypointType.COMBAT, c.getTargetName()));
               player.sendMessage(new TextComponentString("§aAccepted contract: §f" + c.getTargetName() + " §7[" + c.getRank().name() + "]"));
               player.sendMessage(new TextComponentString("§7Location: §f" + c.getLocationHint()));
               return true;
            }
         }
      }
   }

   public void onPlayerDeath(EntityPlayerMP player) {
      if (player != null) {
         ContractSavedData data = ContractSavedData.get(player.world);
         String id = data.getAcceptedContractId(player.getUniqueID());
         if (id != null) {
            this.despawnTargetEntity(player.world, data.getSpawnedTargetEntity(player.getUniqueID()));
            this.clearCombatBarFor(player);
            QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "contract_mission");
            data.clearAcceptance(player.getUniqueID());
            player.sendMessage(new TextComponentString("§7[Contract] You died — target vanished. Accept a new one from the board."));

            try {
               ContractNetworkMessage.sendSync(player);
            } catch (Throwable var5) {
            }

         }
      }
   }

   public void onPlayerLogout(EntityPlayerMP player) {
      if (player != null) {
         ContractSavedData data = ContractSavedData.get(player.world);
         String id = data.getAcceptedContractId(player.getUniqueID());
         if (id != null) {
            this.despawnTargetEntity(player.world, data.getSpawnedTargetEntity(player.getUniqueID()));
            data.clearAcceptance(player.getUniqueID());
         }
      }
   }

   public void abandonContract(EntityPlayerMP player) {
      ContractSavedData data = ContractSavedData.get(player.world);
      String id = data.getAcceptedContractId(player.getUniqueID());
      if (id == null) {
         player.sendMessage(new TextComponentString("§cYou have no active contract."));
      } else {
         this.despawnTargetEntity(player.world, data.getSpawnedTargetEntity(player.getUniqueID()));
         this.clearCombatBarFor(player);
         QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "contract_mission");
         data.clearAcceptance(player.getUniqueID());
         player.sendMessage(new TextComponentString("§7Contract abandoned."));
      }
   }

   private Entity spawnContractTarget(World world, ContractDefinition c) {
      if (world != null && !world.isRemote) {
         String configId = c.getTargetConfigId();
         if (configId == null) {
            return null;
         } else {
            NpcConfig cfg = NpcConfigRegistry.get(configId);
            if (cfg == null) {
               System.out.println("[Contract] Unknown NPC config for spawn: " + configId);
               return null;
            } else {
               int sx = c.getHintX();
               int sz = c.getHintZ();
               int sy = WaypointSpawnLogic.findGroundY(world, sx, sz);
               if (sy <= 0) {
                  sy = 64;
               }

               for(Entity existing : world.loadedEntityList) {
                  if (existing.isEntityAlive() && c.getId().equals(existing.getEntityData().getString("inftsuk_contract_id"))) {
                     System.out.println("[Contract] Re-using existing spawn for contract " + c.getId() + " (entity " + existing.getEntityId() + ") instead of creating duplicate");
                     return existing;
                  }
               }

               Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(cfg.getEntityRegistryId()), world);
               if (entity == null) {
                  System.out.println("[Contract] Failed to create entity: " + cfg.getEntityRegistryId());
                  return null;
               } else {
                  float yaw = world.rand.nextFloat() * 360.0F;
                  entity.setLocationAndAngles((double)sx + (double)0.5F, (double)sy, (double)sz + (double)0.5F, yaw, 0.0F);
                  if (entity instanceof EntityLivingBase) {
                     ((EntityLivingBase)entity).rotationYawHead = yaw;
                     ((EntityLivingBase)entity).renderYawOffset = yaw;
                  }

                  entity.getEntityData().setString("npcConfigId", configId);
                  entity.getEntityData().setBoolean("inftsuk_contract_spawn", true);
                  entity.getEntityData().setString("inftsuk_contract_id", c.getId());
                  if (entity instanceof INpcConfigurable) {
                     ((INpcConfigurable)entity).applyNpcConfig(cfg);
                  }

                  if (entity instanceof EntityLiving) {
                     ((EntityLiving)entity).enablePersistence();
                  }

                  world.spawnEntity(entity);
                  System.out.println("[Contract] Spawned " + configId + " at " + sx + "," + sy + "," + sz + " for contract " + c.getId());
                  return entity;
               }
            }
         }
      } else {
         return null;
      }
   }

   private void clearCombatBarFor(EntityPlayerMP player) {
      if (player != null) {
         EndgameModInit.NETWORK.sendTo(EndgameCombatMessage.clearAll(), player);
      }
   }

   private void despawnTargetEntity(World world, UUID entityUuid) {
      if (world != null && !world.isRemote && entityUuid != null) {
         for(Entity e : world.loadedEntityList) {
            if (entityUuid.equals(e.getUniqueID())) {
               e.getEntityData().setBoolean("inftsuk_contract_vanished", true);
               e.setDead();
               return;
            }
         }

      }
   }

   public boolean completeContract(EntityPlayerMP player, String contractId) {
      ContractSavedData data = ContractSavedData.get(player.world);
      String accepted = data.getAcceptedContractId(player.getUniqueID());
      if (accepted != null && accepted.equals(contractId)) {
         ContractDefinition c = data.findContract(contractId);
         if (c == null) {
            data.clearAcceptance(player.getUniqueID());
            return false;
         } else {
            this.clearCombatBarFor(player);
            QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "contract_mission");
            this.grantRewards(player, c);
            data.removeContract(contractId);
            data.clearAcceptance(player.getUniqueID());
            this.maintainPool(player.world);
            return true;
         }
      } else {
         return false;
      }
   }

   private void grantRewards(EntityPlayerMP player, ContractDefinition c) {
      ContractDefinition.Rank rank = c.getRank();
      RyoRewardHelper.grantRyo(player, rank.ryo);
      player.sendMessage(new TextComponentString("§aContract complete: §f" + c.getTargetName()));
   }

   public void onNpcKilled(EntityPlayerMP killer, String configId) {
      if (killer != null && configId != null) {
         ContractSavedData data = ContractSavedData.get(killer.world);
         String contractId = data.getAcceptedContractId(killer.getUniqueID());
         if (contractId != null) {
            ContractDefinition c = data.findContract(contractId);
            if (c != null) {
               if (configId.equals(c.getTargetConfigId())) {
                  if (!this.canClaimRank(killer.getUniqueID(), c.getRank(), data)) {
                     long resetMs = data.msUntilReset(killer.getUniqueID(), c.getRank().ordinal());
                     long hours = resetMs / 3600000L;
                     long minutes = resetMs / 60000L % 60L;
                     killer.sendMessage(new TextComponentString("§7[Contract] Rank cap reached for today — kill unrewarded. Resets in " + hours + "h " + minutes + "m."));
                  } else {
                     data.recordCompletion(killer.getUniqueID(), c.getRank().ordinal());
                     this.completeContract(killer, contractId);

                     try {
                        ContractNetworkMessage.sendSync(killer);
                     } catch (Throwable var12) {
                     }

                  }
               }
            }
         }
      }
   }

   public boolean canClaimRank(UUID player, ContractDefinition.Rank rank, ContractSavedData data) {
      int cap = RANK_DAILY_CAP[rank.ordinal()];
      if (cap < 0) {
         return true;
      } else {
         int[] used = data.getUsedToday(player);
         return used[rank.ordinal()] < cap;
      }
   }
}
