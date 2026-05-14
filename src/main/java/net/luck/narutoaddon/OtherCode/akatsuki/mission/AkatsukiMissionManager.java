package net.luck.narutoaddon.OtherCode.akatsuki.mission;

import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiModInit;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiSavedData;
import net.luck.narutoaddon.OtherCode.endgame.EndgameModInit;
import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.endgame.PveRank;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameCombatMessage;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.quest.core.TerrainCache;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointData;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointSpawnLogic;
import net.luck.narutoaddon.OtherCode.stat.core.StatManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AkatsukiMissionManager {
   private static AkatsukiMissionManager INSTANCE = new AkatsukiMissionManager();
   private static final int MAX_DAILY = 3;
   private static final int MAX_WEEKLY = 2;
   private static final int MAX_SPECIAL = 1;
   private static final int MAP_MIN = -5500;
   private static final int MAP_MAX = 5500;
   private static final int MIN_DISTANCE = 1000;
   private static final int MAX_DISTANCE = 2000;
   private static final int ACTIVATION_RADIUS = 50;
   private static final int ABANDON_RADIUS = 120;
   private static final int COUNTDOWN_TICKS = 100;
   private static final long OFFER_COOLDOWN_MS = 86400000L;
   private final Map<UUID, List<AkatsukiMission>> activeMissions = new ConcurrentHashMap();
   private final Map<UUID, List<AkatsukiMission>> offers = new ConcurrentHashMap();
   private final Map<UUID, Long> lastOfferGenTime = new ConcurrentHashMap();
   private static final int[][] LAND_REGIONS = new int[][]{{-1600, 800, -1500, 800}, {-3600, -1800, 0, 2000}, {-2700, -1600, -1300, -200}, {-3200, -1600, -3500, -1800}, {800, 3000, -4000, -2200}, {3000, 4500, -2800, -1400}, {-2000, -800, -1800, -1300}, {400, 1200, -1400, -400}, {1000, 2000, 200, 1200}, {-1200, 0, -3500, -2800}};

   private AkatsukiMissionManager() {
   }

   public static AkatsukiMissionManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new AkatsukiMissionManager();
      }

      return INSTANCE;
   }

   public static void reset() {
      INSTANCE = null;
   }

   public void generateOffers(UUID playerId) {
      long now = System.currentTimeMillis();
      Long lastGen = (Long)this.lastOfferGenTime.get(playerId);
      if (lastGen == null || now - lastGen >= 86400000L) {
         List<AkatsukiMission> playerOffers = new ArrayList();
         Random rand = new Random();

         for(int i = 0; i < 3; ++i) {
            AkatsukiMissionTemplate.Template template = AkatsukiMissionTemplate.getRandomDaily(rand);
            playerOffers.add(this.createMissionFromTemplate(template, playerId, rand));
         }

         for(int i = 0; i < 2; ++i) {
            AkatsukiMissionTemplate.Template weeklyTemplate = AkatsukiMissionTemplate.getRandomWeekly(rand);
            playerOffers.add(this.createMissionFromTemplate(weeklyTemplate, playerId, rand));
         }

         this.offers.put(playerId, playerOffers);
         this.lastOfferGenTime.put(playerId, now);
      }
   }

   public List<AkatsukiMission> getOffers(UUID playerId) {
      if (!this.offers.containsKey(playerId)) {
         this.generateOffers(playerId);
      }

      return (List)this.offers.getOrDefault(playerId, Collections.emptyList());
   }

   public boolean acceptOffer(UUID playerId, int offerIndex) {
      List<AkatsukiMission> playerOffers = (List)this.offers.get(playerId);
      if (playerOffers != null && offerIndex >= 0 && offerIndex < playerOffers.size()) {
         AkatsukiMission mission = (AkatsukiMission)playerOffers.get(offerIndex);
         if (!this.acceptMission(playerId, mission)) {
            return false;
         } else {
            playerOffers.remove(offerIndex);
            if (playerOffers.isEmpty()) {
               this.offers.remove(playerId);
            }

            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
               if (player != null) {
                  this.setMissionWaypoint(player, mission);
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private AkatsukiMission createMissionFromTemplate(AkatsukiMissionTemplate.Template template, UUID playerId, Random rand) {
      BlockPos basePos = this.generateTargetPosition(playerId);
      AkatsukiMission mission = new AkatsukiMission(template.id, template.name, template.description, playerId, basePos, template.category);
      if (template.steps != null && template.steps.length > 0) {
         BlockPos currentPos = basePos;

         for(AkatsukiMissionTemplate.MissionStep stepDef : template.steps) {
            BlockPos stepPos;
            if (stepDef.posOffsetMin == 0 && stepDef.posOffsetMax == 0) {
               stepPos = currentPos;
            } else {
               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               World stepWorld = server != null ? server.getWorld(0) : null;
               TerrainCache stepTerrain = stepWorld != null ? TerrainCache.get(stepWorld) : null;
               BlockPos candidate = null;

               for(int attempt = 0; attempt < 10; ++attempt) {
                  double angle = rand.nextDouble() * (double)2.0F * Math.PI;
                  int dist = stepDef.posOffsetMin + rand.nextInt(Math.max(1, stepDef.posOffsetMax - stepDef.posOffsetMin + 1));
                  int cx = currentPos.getX() + (int)(Math.cos(angle) * (double)dist);
                  int cz = currentPos.getZ() + (int)(Math.sin(angle) * (double)dist);
                  cx = Math.max(-5500, Math.min(5500, cx));
                  cz = Math.max(-5500, Math.min(5500, cz));
                  if (stepWorld == null || stepTerrain == null || !stepTerrain.isWater(stepWorld, cx, cz)) {
                     int cy = 64;
                     if (stepWorld != null) {
                        cy = WaypointSpawnLogic.findGroundY(stepWorld, cx, cz);
                        if (cy < 1) {
                           cy = 64;
                        }
                     }

                     candidate = new BlockPos(cx, cy, cz);
                     break;
                  }
               }

               if (candidate == null) {
                  double angle = rand.nextDouble() * (double)2.0F * Math.PI;
                  int dist = stepDef.posOffsetMin + rand.nextInt(Math.max(1, stepDef.posOffsetMax - stepDef.posOffsetMin + 1));
                  int fx = currentPos.getX() + (int)(Math.cos(angle) * (double)dist);
                  int fz = currentPos.getZ() + (int)(Math.sin(angle) * (double)dist);
                  fx = Math.max(-5500, Math.min(5500, fx));
                  fz = Math.max(-5500, Math.min(5500, fz));
                  candidate = new BlockPos(fx, 64, fz);
               }

               if (stepWorld != null) {
                  candidate = WaypointSpawnLogic.findLandPosition(stepWorld, candidate);
                  int safeY = WaypointSpawnLogic.findGroundY(stepWorld, candidate.getX(), candidate.getZ());
                  if (safeY > 0) {
                     candidate = new BlockPos(candidate.getX(), safeY, candidate.getZ());
                  }
               }

               stepPos = candidate;
            }

            mission.addStep(new AkatsukiMission.StepInstance(stepPos, stepDef.type, stepDef.objective, stepDef.npcConfigIds, stepDef.scoutDurationTicks));
            currentPos = stepPos;
         }
      } else {
         AkatsukiMission.StepInstance step = new AkatsukiMission.StepInstance(basePos, AkatsukiMissionTemplate.MissionStep.StepType.TRAVEL, template.objectiveText, (String[])null, 0);
         mission.addStep(step);
      }

      mission.setRyoReward((int)((double)template.getRandomRyo(rand) * (double)1.5F));
      mission.setPveXpReward(template.pveXp);
      return mission;
   }

   public boolean acceptMission(UUID playerId, AkatsukiMission mission) {
      List<AkatsukiMission> missions = (List)this.activeMissions.computeIfAbsent(playerId, (k) -> new ArrayList());
      long dailyCount = 0L;
      long weeklyCount = 0L;
      long specialCount = 0L;

      for(AkatsukiMission m : missions) {
         if (!m.isCompleted()) {
            switch (m.getCategory()) {
               case DAILY:
                  ++dailyCount;
                  break;
               case WEEKLY:
                  ++weeklyCount;
                  break;
               case SPECIAL:
                  ++specialCount;
            }
         }
      }

      switch (mission.getCategory()) {
         case DAILY:
            if (dailyCount >= 3L) {
               return false;
            }
            break;
         case WEEKLY:
            if (weeklyCount >= 2L) {
               return false;
            }
            break;
         case SPECIAL:
            if (specialCount >= 1L) {
               return false;
            }
      }

      missions.add(mission);
      this.saveMissions();
      return true;
   }

   public boolean abandonMission(UUID playerId, String templateId) {
      List<AkatsukiMission> missions = (List)this.activeMissions.get(playerId);
      if (missions == null) {
         return false;
      } else {
         Iterator<AkatsukiMission> it = missions.iterator();

         while(it.hasNext()) {
            AkatsukiMission m = (AkatsukiMission)it.next();
            if (m.getTemplateId().equals(templateId) && !m.isCompleted()) {
               this.cleanupMissionEntities(m);
               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               if (server != null) {
                  EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
                  if (player != null) {
                     QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "akatsuki_mission_" + templateId);
                  }
               }

               it.remove();
               m.setMissionState(AkatsukiMission.MissionState.TRAVELING);
               m.getSpawnedEntityUUIDs().clear();
               List<AkatsukiMission> playerOffers = (List)this.offers.computeIfAbsent(playerId, (k) -> new ArrayList());
               playerOffers.add(m);
               this.saveMissions();
               return true;
            }
         }

         return false;
      }
   }

   public List<AkatsukiMission> getActiveMissions(UUID playerId) {
      return (List)this.activeMissions.getOrDefault(playerId, Collections.emptyList());
   }

   public void tickPlayer(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      List<AkatsukiMission> missions = (List)this.activeMissions.get(playerId);
      if (missions != null && !missions.isEmpty()) {
         boolean changed = false;

         for(AkatsukiMission mission : new ArrayList(missions)) {
            if (!mission.isCompleted()) {
               BlockPos targetPos = mission.getTargetPos();
               double dx = player.posX - (double)targetPos.getX();
               double dz = player.posZ - (double)targetPos.getZ();
               double distSq = dx * dx + dz * dz;
               AkatsukiMission.StepInstance currentStep = mission.getCurrentStep();
               switch (mission.getMissionState()) {
                  case TRAVELING:
                     if (distSq <= (double)2500.0F) {
                        if (currentStep != null) {
                           changed |= this.handleStepArrival(player, mission, currentStep);
                        } else {
                           mission.setMissionState(AkatsukiMission.MissionState.COUNTDOWN);
                           this.sendTitle(player, TextFormatting.YELLOW + "5", mission.getTemplateName());
                           changed = true;
                        }
                     }
                     break;
                  case COUNTDOWN:
                     changed |= this.tickCountdown(player, mission);
                     break;
                  case ACTIVE:
                     changed |= this.tickActive(player, mission, distSq);
                  case VICTORY:
                  default:
                     break;
                  case FAILED:
                     this.cleanupMissionEntities(mission);
                     missions.remove(mission);
                     QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "akatsuki_mission_" + mission.getTemplateId());
                     changed = true;
               }
            }
         }

         if (changed) {
            this.saveMissions();
            this.syncToClient(player);
         }

      }
   }

   private boolean handleStepArrival(EntityPlayerMP player, AkatsukiMission mission, AkatsukiMission.StepInstance step) {
      switch (step.getType()) {
         case TRAVEL:
            return this.handleStepComplete(player, mission, "Arrived at destination");
         case SCOUT:
            mission.setMissionState(AkatsukiMission.MissionState.ACTIVE);
            String scoutObj = step.getObjective().isEmpty() ? "Scout the area" : step.getObjective();
            this.sendTitle(player, TextFormatting.YELLOW + "Scouting...", TextFormatting.GRAY + scoutObj);
            return true;
         case COMBAT:
            mission.setMissionState(AkatsukiMission.MissionState.COUNTDOWN);
            this.sendTitle(player, TextFormatting.YELLOW + "5", mission.getTemplateName());
            return true;
         default:
            return false;
      }
   }

   private boolean handleStepComplete(EntityPlayerMP player, AkatsukiMission mission, String message) {
      int stepNum = mission.getCurrentStepIndex() + 1;
      int totalSteps = mission.getTotalSteps();
      if (mission.isOnFinalStep()) {
         this.completeMissionWithCombat(player, mission);
         return true;
      } else {
         mission.advanceStep();
         player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GREEN + "Step " + stepNum + "/" + totalSteps + " complete" + TextFormatting.GRAY + " — " + message));
         this.setMissionWaypoint(player, mission);
         AkatsukiMission.StepInstance nextStep = mission.getCurrentStep();
         if (nextStep != null && !nextStep.getObjective().isEmpty()) {
            player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.YELLOW + "Next: " + TextFormatting.WHITE + nextStep.getObjective()));
         }

         return true;
      }
   }

   private boolean tickCountdown(EntityPlayerMP player, AkatsukiMission mission) {
      mission.incrementCountdownTicks();
      int ticks = mission.getCountdownTicks();
      switch (ticks) {
         case 1:
            this.sendTitle(player, TextFormatting.YELLOW + "4", "");
            break;
         case 2:
            this.sendTitle(player, TextFormatting.GOLD + "3", "");
            break;
         case 3:
            this.sendTitle(player, TextFormatting.RED + "2", "");
            break;
         case 4:
            this.sendTitle(player, TextFormatting.DARK_RED + "1", "");
      }

      if (ticks >= 5) {
         this.sendTitle(player, TextFormatting.RED + "" + TextFormatting.BOLD + "FIGHT!", "");
         this.spawnMissionNPCs(player, mission);
         mission.setMissionState(AkatsukiMission.MissionState.ACTIVE);
         return true;
      } else {
         return false;
      }
   }

   private boolean tickActive(EntityPlayerMP player, AkatsukiMission mission, double distSq) {
      AkatsukiMission.StepInstance currentStep = mission.getCurrentStep();
      if (currentStep != null && currentStep.getType() == AkatsukiMissionTemplate.MissionStep.StepType.SCOUT) {
         return this.tickActiveScout(player, mission, currentStep, distSq);
      } else if (distSq > (double)14400.0F) {
         mission.setMissionState(AkatsukiMission.MissionState.FAILED);
         this.sendTitle(player, TextFormatting.RED + "Mission Failed", TextFormatting.GRAY + "You left the combat area.");
         this.clearCombatBars(player);
         return true;
      } else if (mission.allEnemiesDefeated()) {
         return this.handleStepComplete(player, mission, "All targets eliminated");
      } else {
         if (!mission.getSpawnedEntityUUIDs().isEmpty()) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               World world = server.getWorld(0);
               boolean anyAlive = false;

               for(UUID entityUUID : mission.getSpawnedEntityUUIDs()) {
                  Entity entity = ((WorldServer)world).getEntityFromUuid(entityUUID);
                  if (entity != null && entity.isEntityAlive()) {
                     anyAlive = true;
                     break;
                  }
               }

               if (!anyAlive && !mission.allEnemiesDefeated()) {
                  mission.getSpawnedEntityUUIDs().clear();
                  mission.setMissionState(AkatsukiMission.MissionState.TRAVELING);
                  this.clearCombatBars(player);
                  player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.YELLOW + "Targets lost. Return to the mission area to re-engage."));
                  this.setMissionWaypoint(player, mission);
                  return true;
               }
            }
         }

         this.syncMissionHealthBars(player, mission);
         return false;
      }
   }

   private boolean tickActiveScout(EntityPlayerMP player, AkatsukiMission mission, AkatsukiMission.StepInstance step, double distSq) {
      if (distSq > (double)2500.0F) {
         if (mission.getScoutProgress() > 0) {
            mission.setScoutProgress(0);
            player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.YELLOW + "You left the scout area. Progress reset."));
         }

         return false;
      } else {
         mission.incrementScoutProgress();
         int progress = mission.getScoutProgress();
         int required = step.getScoutDurationTicks();
         if (progress % 5 == 0 && progress < required) {
            player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "Scouting... " + TextFormatting.WHITE + progress + "/" + required + "s"));
         }

         return progress >= required ? this.handleStepComplete(player, mission, "Area scouted") : false;
      }
   }

   private void spawnMissionNPCs(EntityPlayerMP player, AkatsukiMission mission) {
      String[] configIds = mission.getNpcConfigIds();
      if (configIds != null && configIds.length != 0) {
         mission.setKillsRequired(configIds.length);
         BlockPos basePos = mission.getTargetPos();
         World world = player.world;

         for(int i = 0; i < configIds.length; ++i) {
            String configId = configIds[i];
            NpcConfig config = NpcConfigRegistry.get(configId);
            if (config == null) {
               System.out.println("[AkatsukiMission] Unknown NPC config: " + configId);
               mission.incrementKills();
               player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "Target spawn failed — skipping."));
            } else {
               Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(config.getEntityRegistryId()), world);
               if (entity == null) {
                  System.out.println("[AkatsukiMission] Failed to create entity: " + config.getEntityRegistryId());
                  mission.incrementKills();
                  player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "Target spawn failed — skipping."));
               } else {
                  double angle = (Math.PI * 2D) * (double)i / (double)configIds.length;
                  double radius = configIds.length > 1 ? (double)4.0F : (double)0.0F;
                  int spawnX = basePos.getX() + (int)(Math.cos(angle) * radius);
                  int spawnZ = basePos.getZ() + (int)(Math.sin(angle) * radius);
                  BlockPos npcPos = WaypointSpawnLogic.findLandPosition(world, new BlockPos(spawnX, 64, spawnZ));
                  spawnX = npcPos.getX();
                  spawnZ = npcPos.getZ();
                  int spawnY = WaypointSpawnLogic.findGroundY(world, spawnX, spawnZ);
                  entity.setLocationAndAngles((double)spawnX + (double)0.5F, (double)spawnY, (double)spawnZ + (double)0.5F, world.rand.nextFloat() * 360.0F, 0.0F);
                  NBTTagCompound entityData = entity.getEntityData();
                  entityData.setBoolean("akatsukiMissionEntity", true);
                  entityData.setString("missionOwnerId", mission.getOwnerId().toString());
                  entityData.setString("missionTemplateId", mission.getTemplateId());
                  if (entity instanceof INpcConfigurable) {
                     ((INpcConfigurable)entity).applyNpcConfig(config);
                  }

                  entity.setCustomNameTag(TextFormatting.RED + config.getDisplayName());
                  entity.setAlwaysRenderNameTag(true);
                  if (entity instanceof EntityLiving) {
                     ((EntityLiving)entity).enablePersistence();
                  }

                  world.spawnEntity(entity);
                  mission.addSpawnedEntity(entity.getUniqueID());
               }
            }
         }

      }
   }

   public void onEntityDeath(Entity entity, EntityPlayerMP killer) {
      NBTTagCompound data = entity.getEntityData();
      if (data.getBoolean("akatsukiMissionEntity")) {
         String ownerIdStr = data.getString("missionOwnerId");
         String templateId = data.getString("missionTemplateId");
         if (!ownerIdStr.isEmpty() && !templateId.isEmpty()) {
            UUID ownerId;
            try {
               ownerId = UUID.fromString(ownerIdStr);
            } catch (IllegalArgumentException var12) {
               return;
            }

            List<AkatsukiMission> missions = (List)this.activeMissions.get(ownerId);
            if (missions != null) {
               for(AkatsukiMission mission : missions) {
                  if (mission.getTemplateId().equals(templateId) && mission.getMissionState() == AkatsukiMission.MissionState.ACTIVE && !mission.isCompleted()) {
                     if (mission.getSpawnedEntityUUIDs().contains(entity.getUniqueID())) {
                        mission.incrementKills();
                        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
                        if (server != null) {
                           EntityPlayerMP owner = server.getPlayerList().getPlayerByUUID(ownerId);
                           if (owner != null) {
                              owner.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "Target eliminated " + TextFormatting.WHITE + mission.getKillsAchieved() + "/" + mission.getKillsRequired()));
                              if (mission.allEnemiesDefeated()) {
                                 this.handleStepComplete(owner, mission, "All targets eliminated");
                                 this.saveMissions();
                                 this.syncToClient(owner);
                              }
                           }
                        }
                     }
                     break;
                  }
               }

            }
         }
      }
   }

   private void completeMissionWithCombat(EntityPlayerMP player, AkatsukiMission mission) {
      UUID playerId = player.getUniqueID();
      mission.setCompleted(true);
      mission.setMissionState(AkatsukiMission.MissionState.VICTORY);
      AkatsukiMission.StepInstance finalStep = mission.getCurrentStep();
      if (finalStep != null) {
         finalStep.setCompleted(true);
      }

      this.clearCombatBars(player);
      this.sendTitle(player, TextFormatting.GREEN + "" + TextFormatting.BOLD + "VICTORY!", TextFormatting.GOLD + mission.getTemplateName() + " complete!");
      AkatsukiMissionTemplate.Template template = AkatsukiMissionTemplate.getById(mission.getTemplateId());
      int tokenReward = template != null ? template.tokenReward : 15;
      int repReward = template != null ? template.repReward : 25;
      int xpReward = template != null ? template.xpReward : 200;
      AkatsukiManager mgr = AkatsukiManager.getInstance();
      mgr.addBountyTokens(playerId, tokenReward);
      mgr.addReputation(playerId, repReward, "Mission: " + mission.getTemplateName());
      player.addExperience(xpReward);
      int ryoAmount = mission.getRyoReward();
      if (ryoAmount > 0) {
         RyoRewardHelper.grantRyo(player, ryoAmount);
      }

      int pveXp = mission.getPveXpReward();
      if (pveXp > 0) {
         try {
            EndgameSavedData savedData = EndgameSavedData.get(player.world);
            PveRank rankBefore = savedData.getPveRank(playerId);
            savedData.addPveXp(playerId, pveXp);
            PveRank rankAfter = savedData.getPveRank(playerId);
            player.sendMessage(new TextComponentString(TextFormatting.AQUA + "+" + pveXp + " PvE XP"));
            if (rankAfter != rankBefore) {
               player.sendMessage(new TextComponentString(TextFormatting.GOLD + "" + TextFormatting.BOLD + "★ PvE RANK UP! " + TextFormatting.RESET + TextFormatting.YELLOW + rankBefore.getDisplayName() + " → " + rankAfter.getDisplayName()));
            }
         } catch (Exception var16) {
         }
      }

      int spReward = mission.getCategory() == AkatsukiMissionTemplate.Category.WEEKLY ? 3 : 1;

      try {
         StatManager.getInstance().grantSP(player, spReward);
         player.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "+" + spReward + " SP"));
      } catch (Exception var15) {
      }

      player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GREEN + "Mission complete: " + mission.getTemplateName() + TextFormatting.GRAY + " (+" + tokenReward + " tokens, +" + repReward + " rep, +" + xpReward + " XP, +" + ryoAmount + " Ryo, +" + pveXp + " PvE XP)"));
      QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "akatsuki_mission_" + mission.getTemplateId());
      this.cleanupMissionEntities(mission);
      List<AkatsukiMission> missions = (List)this.activeMissions.get(playerId);
      if (missions != null) {
         missions.remove(mission);
      }

      this.saveMissions();
      this.syncToClient(player);
      mgr.syncToClient(player);
   }

   private void syncMissionHealthBars(EntityPlayerMP player, AkatsukiMission mission) {
      World world = player.world;
      if (world instanceof WorldServer) {
         WorldServer ws = (WorldServer)world;

         for(UUID entityUUID : mission.getSpawnedEntityUUIDs()) {
            Entity entity = ws.getEntityFromUuid(entityUUID);
            if (entity != null && entity.isEntityAlive() && entity instanceof EntityLivingBase) {
               EntityLivingBase boss = (EntityLivingBase)entity;
               String bossName = entity.getName();
               int themeColor = -3407872;
               int accentColor = -10092544;
               if (entity instanceof INpcConfigurable) {
                  String cfgId = ((INpcConfigurable)entity).getNpcConfigId();
                  if (cfgId != null && !cfgId.isEmpty()) {
                     NpcConfig cfg = NpcConfigRegistry.get(cfgId);
                     if (cfg != null) {
                        bossName = cfg.getDisplayName();
                        if (cfg.hasHealthBarColors()) {
                           themeColor = cfg.getThemeColor();
                           accentColor = cfg.getAccentColor();
                        }
                     }
                  }
               }

               EndgameCombatMessage msg = new EndgameCombatMessage(entity.getEntityId(), bossName, boss.getHealth(), boss.getMaxHealth(), 0, (byte)0, themeColor, accentColor);
               EndgameModInit.NETWORK.sendTo(msg, player);
            }
         }

      }
   }

   private void clearCombatBars(EntityPlayerMP player) {
      try {
         EndgameCombatMessage clearMsg = EndgameCombatMessage.clearAll();
         EndgameModInit.NETWORK.sendTo(clearMsg, player);
      } catch (Exception var3) {
      }

   }

   private void cleanupMissionEntities(AkatsukiMission mission) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         World world = server.getWorld(0);
         if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer)world;

            for(UUID entityUUID : mission.getSpawnedEntityUUIDs()) {
               Entity entity = ws.getEntityFromUuid(entityUUID);
               if (entity != null) {
                  entity.setDead();
               }
            }

            mission.getSpawnedEntityUUIDs().clear();
         }
      }
   }

   public void setMissionWaypoint(EntityPlayerMP player, AkatsukiMission mission) {
      QuestManager qm = QuestManager.getInstance();
      WaypointData.WaypointType type = WaypointData.WaypointType.TRAVEL;
      AkatsukiMission.StepInstance currentStep = mission.getCurrentStep();
      if (currentStep != null) {
         switch (currentStep.getType()) {
            case TRAVEL:
            case SCOUT:
            default:
               type = WaypointData.WaypointType.TRAVEL;
               break;
            case COMBAT:
               type = WaypointData.WaypointType.COMBAT;
         }
      } else {
         String[] npcIds = mission.getNpcConfigIds();
         type = npcIds != null && npcIds.length > 0 ? WaypointData.WaypointType.COMBAT : WaypointData.WaypointType.TRAVEL;
      }

      String label = mission.getTemplateName();
      if (mission.getTotalSteps() > 1) {
         label = label + " (" + (mission.getCurrentStepIndex() + 1) + "/" + mission.getTotalSteps() + ")";
      }

      qm.getWaypointManager().setWaypoint(player, "akatsuki_mission_" + mission.getTemplateId(), new WaypointData(mission.getTargetPos(), type, label));
   }

   private void sendTitle(EntityPlayerMP player, String title, String subtitle) {
      SPacketTitle titlePacket = new SPacketTitle(Type.TITLE, new TextComponentString(title), 5, 20, 5);
      player.connection.sendPacket(titlePacket);
      if (subtitle != null && !subtitle.isEmpty()) {
         SPacketTitle subtitlePacket = new SPacketTitle(Type.SUBTITLE, new TextComponentString(subtitle), 5, 20, 5);
         player.connection.sendPacket(subtitlePacket);
      }

   }

   public void syncToClient(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      List<AkatsukiMission> active = this.getActiveMissions(playerId);
      List<AkatsukiMission> playerOffers = this.getOffers(playerId);
      AkatsukiMissionSyncMessage msg = new AkatsukiMissionSyncMessage(active, playerOffers);
      AkatsukiModInit.NETWORK.sendTo(msg, player);
   }

   private static boolean isInKnownLand(int x, int z) {
      for(int[] region : LAND_REGIONS) {
         if (x >= region[0] && x <= region[1] && z >= region[2] && z <= region[3]) {
            return true;
         }
      }

      return false;
   }

   private BlockPos generateTargetPosition(UUID playerId) {
      Random rand = new Random();
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      int baseX = 0;
      int baseZ = 0;
      if (server != null) {
         EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
         if (player != null) {
            baseX = (int)player.posX;
            baseZ = (int)player.posZ;
         }
      }

      World world = server != null ? server.getWorld(0) : null;
      TerrainCache terrain = world != null ? TerrainCache.get(world) : null;
      BlockPos result = null;

      for(int attempt = 0; attempt < 15; ++attempt) {
         double angle = rand.nextDouble() * (double)2.0F * Math.PI;
         int distance = 1000 + rand.nextInt(1001);
         int x = baseX + (int)(Math.cos(angle) * (double)distance);
         int z = baseZ + (int)(Math.sin(angle) * (double)distance);
         x = Math.max(-5500, Math.min(5500, x));
         z = Math.max(-5500, Math.min(5500, z));
         if (isInKnownLand(x, z) && (world == null || terrain == null || !terrain.isWater(world, x, z))) {
            int y = 64;
            if (world != null) {
               y = WaypointSpawnLogic.findGroundY(world, x, z);
               if (y < 1) {
                  y = 64;
               }
            }

            result = new BlockPos(x, y, z);
            break;
         }
      }

      if (result == null) {
         double angle = rand.nextDouble() * (double)2.0F * Math.PI;
         int distance = 1000 + rand.nextInt(1001);
         int x = baseX + (int)(Math.cos(angle) * (double)distance);
         int z = baseZ + (int)(Math.sin(angle) * (double)distance);
         x = Math.max(-5500, Math.min(5500, x));
         z = Math.max(-5500, Math.min(5500, z));
         result = new BlockPos(x, 64, z);
      }

      if (world != null) {
         result = WaypointSpawnLogic.findLandPosition(world, result);
         int safeY = WaypointSpawnLogic.findGroundY(world, result.getX(), result.getZ());
         if (safeY > 0) {
            result = new BlockPos(result.getX(), safeY, result.getZ());
         }
      }

      return result;
   }

   public void onPlayerLogout(UUID playerId) {
      List<AkatsukiMission> missions = (List)this.activeMissions.get(playerId);
      if (missions != null) {
         for(AkatsukiMission mission : missions) {
            if (mission.getOwnerId().equals(playerId) && mission.isCombatActive()) {
               this.cleanupMissionEntities(mission);
               mission.setMissionState(AkatsukiMission.MissionState.TRAVELING);
               mission.getSpawnedEntityUUIDs().clear();
               mission.setScoutProgress(0);
            }
         }

         this.saveMissions();
      }
   }

   public void loadMissions(AkatsukiSavedData data) {
      this.activeMissions.clear();
      NBTTagCompound root = data.getMissionsNBT();
      if (root != null) {
         NBTTagList playerList = root.getTagList("players", 10);

         for(int i = 0; i < playerList.tagCount(); ++i) {
            NBTTagCompound playerTag = playerList.getCompoundTagAt(i);

            UUID playerId;
            try {
               playerId = UUID.fromString(playerTag.getString("uuid"));
            } catch (IllegalArgumentException var11) {
               continue;
            }

            NBTTagList missionList = playerTag.getTagList("missions", 10);
            List<AkatsukiMission> missions = new ArrayList();

            for(int j = 0; j < missionList.tagCount(); ++j) {
               AkatsukiMission mission = new AkatsukiMission();
               mission.readFromNBT(missionList.getCompoundTagAt(j));
               missions.add(mission);
            }

            this.activeMissions.put(playerId, missions);
         }

      }
   }

   private void saveMissions() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         AkatsukiSavedData data = AkatsukiSavedData.get(server.getWorld(0));
         NBTTagCompound root = new NBTTagCompound();
         NBTTagList playerList = new NBTTagList();

         for(Map.Entry<UUID, List<AkatsukiMission>> entry : this.activeMissions.entrySet()) {
            NBTTagCompound playerTag = new NBTTagCompound();
            playerTag.setString("uuid", ((UUID)entry.getKey()).toString());
            NBTTagList missionList = new NBTTagList();

            for(AkatsukiMission mission : (List)entry.getValue()) {
               missionList.appendTag(mission.writeToNBT());
            }

            playerTag.setTag("missions", missionList);
            playerList.appendTag(playerTag);
         }

         root.setTag("players", playerList);
         data.setMissionsNBT(root);
         data.markDirty();
      }
   }
}
