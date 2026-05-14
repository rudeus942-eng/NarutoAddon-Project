
package net.luck.narutoaddon.OtherCode.quest.core;

import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.endgame.PveRank;
import net.luck.narutoaddon.OtherCode.entity.*;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.quest.network.KGRollResultMessage;
import net.luck.narutoaddon.OtherCode.quest.network.QuestCombatHealthMessage;
import net.luck.narutoaddon.OtherCode.quest.network.QuestNetworkHelper;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointData;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointManager;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointSpawnLogic;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassManager;
import net.luck.narutoaddon.OtherCode.stat.core.StatManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {
   private static QuestManager instance;
   private final Map<UUID, Map<String, QuestInstance>> activeQuests = new ConcurrentHashMap();
   private final Map<UUID, Set<String>> completedQuests = new ConcurrentHashMap();
   private final WaypointManager waypointManager = new WaypointManager();
   private int tickCounter = 0;
   private final Map<UUID, Integer> cleanupTimers = new ConcurrentHashMap();
   private static final int CLEANUP_DELAY_TICKS = 1200;
   private final Map<String, SharedEncounter> sharedEncounters = new ConcurrentHashMap();
   private final Map<UUID, Integer> areaStayProgress = new ConcurrentHashMap();
   private final Map<String, UUID> sharedDialogNpcs = new ConcurrentHashMap();
   private final Map<UUID, Set<UUID>> entityToPlayers = new ConcurrentHashMap();
   private final Map<UUID, String[]> pendingKGRolls = new ConcurrentHashMap();
   private static final int SCRIPTED_RETREAT_TICKS = 1200;
   private static final String[] DAILY_SUB_SLOTS = new String[]{"daily_0", "daily_1", "daily_2"};
   private static final String[] WEEKLY_SUB_SLOTS = new String[]{"weekly_0", "weekly_1", "weekly_2"};
   private static final String[] ALL_REPEATABLE_SUB_SLOTS = new String[]{"daily_0", "daily_1", "daily_2", "weekly_0", "weekly_1", "weekly_2", "random"};
   private final Map<UUID, Map<String, RepeatableQuestGenerator.QuestOffer>> currentOffers = new ConcurrentHashMap();
   private final Map<UUID, Map<Integer, Float>> lastSentHealth = new ConcurrentHashMap();
   private final Random random = new Random();
   private static final String[][] KG_TABLE = new String[][]{{"narutomod:yooton", "Lava Release", "30", "0"}, {"narutomod:hyoton", "Ice Release", "30", "0"}, {"narutomod:futton", "Boil Release", "20", "1"}, {"narutomod:ranton", "Storm Release", "12", "2"}, {"narutomod:bakuton", "Explosion Release", "8", "2"}};
   private static final int KG_TOTAL_WEIGHT = 100;
   private static final int KG_STRIP_SIZE = 60;
   private static final int KG_TARGET_INDEX = 45;
   private static final String[][] RANK_PROGRESSION = new String[][]{{"academy_graduation", "StoryGenin"}, {"land_of_waves_end", "StoryGenin2"}, {"preliminary_neji", "StoryChunin"}, {"konoha_crush_commence", "StoryChuninLeader"}, {"shikamaru_ambush", "StoryTokujo"}, {"hiruzen_death", "StoryJonin"}, {"tsunade_search_4", "StorySJonin4"}, {"resolve", "StorySJonin3"}, {"kaze_resolution", "StorySJonin2"}, {"tenchi_resolution", "StorySJonin1"}, {"immortal_duo_conclusion", "StoryEJonin4"}, {"hunt_itachi_truth", "StoryEJonin3"}};

   public static synchronized QuestManager getInstance() {
      if (instance == null) {
         instance = new QuestManager();
      }

      return instance;
   }

   public static synchronized void reset() {
      instance = null;
   }

   public WaypointManager getWaypointManager() {
      return this.waypointManager;
   }

   public static QuestDefinition.QuestRank getEffectiveMissionRank(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      EndgameSavedData endgameData = EndgameSavedData.get(player.world);
      PveRank pveRank = endgameData.getPveRank(playerId);
      QuestDefinition.QuestRank pveBasedRank = pveRankToQuestRank(pveRank);
      QuestSavedData questData = QuestSavedData.get(player.world);
      QuestDefinition.QuestRank adminRank = questData.getPlayerMissionRank(playerId);
      return pveBasedRank.ordinal() >= adminRank.ordinal() ? pveBasedRank : adminRank;
   }

   private static QuestDefinition.QuestRank pveRankToQuestRank(PveRank pveRank) {
      switch (pveRank) {
         case GENIN:
            return QuestDefinition.QuestRank.D;
         case CHUNIN:
            return QuestDefinition.QuestRank.C;
         case JONIN:
            return QuestDefinition.QuestRank.B;
         case ANBU:
            return QuestDefinition.QuestRank.A;
         case KAGE:
            return QuestDefinition.QuestRank.S;
         default:
            return QuestDefinition.QuestRank.D;
      }
   }

   public static boolean isStorySlot(String slot) {
      return slot != null && slot.startsWith("story:");
   }

   public static String getStorylineFromSlot(String slot) {
      return slot != null && slot.startsWith("story:") ? slot.substring(6) : "leaf_story";
   }

   public static String storySlotFor(String storyline) {
      return "story:" + storyline;
   }

   public static String getSlotForQuest(QuestDefinition def) {
      if (def == null) {
         return storySlotFor("leaf_story");
      } else {
         switch (def.getRepeatType()) {
            case DAILY:
               return "daily";
            case WEEKLY:
               return "weekly";
            case RANDOM:
               return "random";
            default:
               return storySlotFor(def.getStoryline());
         }
      }
   }

   public static String getSlotForInstance(QuestInstance quest) {
      if (quest.isGenerated()) {
         String qid = quest.getQuestId();
         if (qid.startsWith("gen_daily_")) {
            char idx = qid.charAt(10);
            return idx >= '0' && idx <= '2' ? "daily_" + idx : "daily_0";
         } else if (qid.startsWith("gen_weekly_")) {
            char idx = qid.charAt(11);
            return idx >= '0' && idx <= '2' ? "weekly_" + idx : "weekly_0";
         } else {
            return qid.startsWith("gen_random_") ? "random" : "random";
         }
      } else {
         QuestDefinition def = QuestRegistry.getById(quest.getQuestId());
         return getSlotForQuest(def);
      }
   }

   public static String getBaseCategory(String subSlot) {
      if (subSlot.startsWith("daily")) {
         return "daily";
      } else if (subSlot.startsWith("weekly")) {
         return "weekly";
      } else {
         return isStorySlot(subSlot) ? "story" : subSlot;
      }
   }

   public static String[] getSubSlots(String baseCategory) {
      switch (baseCategory) {
         case "daily":
            return DAILY_SUB_SLOTS;
         case "weekly":
            return WEEKLY_SUB_SLOTS;
         default:
            return new String[]{baseCategory};
      }
   }

   public static boolean isRepeatableSubSlot(String slot) {
      return slot.startsWith("daily_") || slot.startsWith("weekly_") || slot.equals("random");
   }

   private static String getCategoryDisplayName(String slot) {
      if (isStorySlot(slot)) {
         return "story";
      } else {
         switch (getBaseCategory(slot)) {
            case "daily":
               return "daily";
            case "weekly":
               return "weekly";
            case "random":
               return "random";
            default:
               return "story";
         }
      }
   }

   public void generateOffer(EntityPlayerMP player, String slotType) {
      UUID playerId = player.getUniqueID();
      BlockPos playerPos = player.getPosition();
      QuestSavedData savedData = QuestSavedData.get(player.world);
      QuestDefinition.QuestRank playerRank = getEffectiveMissionRank(player);
      RepeatableQuestGenerator.QuestOffer offer = RepeatableQuestGenerator.generateOffer(slotType, playerPos, this.random, player, playerRank);
      ((Map)this.currentOffers.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap())).put(slotType, offer);
      savedData.saveOffer(playerId, slotType, offer);
   }

   public boolean rerollOffer(EntityPlayerMP player, String subSlot) {
      UUID playerId = player.getUniqueID();
      QuestSavedData savedData = QuestSavedData.get(player.world);
      String baseCategory = getBaseCategory(subSlot);
      if (!"random".equals(baseCategory)) {
         int remaining = savedData.getRerollsRemaining(playerId, baseCategory);
         if (remaining <= 0) {
            player.sendMessage(new TextComponentString("§cNo rerolls remaining for " + getCategoryDisplayName(subSlot) + " quests."));
            return false;
         }

         if (!savedData.useReroll(playerId, baseCategory)) {
            player.sendMessage(new TextComponentString("§cNo rerolls remaining for " + getCategoryDisplayName(subSlot) + " quests."));
            return false;
         }
      }

      this.generateOffer(player, subSlot);
      QuestNetworkHelper.sendQuestSync(player);
      return true;
   }

   public void acceptOffer(EntityPlayerMP player, String slotType) {
      UUID playerId = player.getUniqueID();
      Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
      if (playerSlots != null && playerSlots.containsKey(slotType)) {
         player.sendMessage(new TextComponentString("§cYou already have an active " + getCategoryDisplayName(slotType) + " quest. Abandon it first."));
      } else {
         QuestSavedData savedData = QuestSavedData.get(player.world);
         if (savedData.isSlotOnCooldown(playerId, slotType)) {
            long remaining = savedData.getSlotCooldownRemaining(playerId, slotType);
            String timeStr = formatCooldown(remaining);
            player.sendMessage(new TextComponentString("§cThis slot is on cooldown. Available in §e" + timeStr + "§c."));
         } else {
            Map<String, RepeatableQuestGenerator.QuestOffer> playerOffers = (Map)this.currentOffers.get(playerId);
            if (playerOffers != null && playerOffers.containsKey(slotType)) {
               RepeatableQuestGenerator.QuestOffer offer = (RepeatableQuestGenerator.QuestOffer)playerOffers.get(slotType);
               QuestInstance quest = QuestInstance.fromOffer(offer);
               ((Map)this.activeQuests.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap())).put(slotType, quest);
               savedData.saveQuest(playerId, quest);
               playerOffers.remove(slotType);
               savedData.removeOffer(playerId, slotType);
               player.sendMessage(new TextComponentString("§aQuest accepted: §6" + offer.name));
               this.setupCurrentStep(player, quest);
               QuestNetworkHelper.sendQuestSync(player);
            } else {
               player.sendMessage(new TextComponentString("§cNo offer available. Try opening the Quest Log."));
            }
         }
      }
   }

   public RepeatableQuestGenerator.QuestOffer getCurrentOffer(UUID playerId, String slotType) {
      Map<String, RepeatableQuestGenerator.QuestOffer> playerOffers = (Map)this.currentOffers.get(playerId);
      return playerOffers == null ? null : (RepeatableQuestGenerator.QuestOffer)playerOffers.get(slotType);
   }

   public void acceptQuest(EntityPlayerMP player, String questId) {
      UUID playerId = player.getUniqueID();
      QuestDefinition def = QuestRegistry.getById(questId);
      if (def == null) {
         player.sendMessage(new TextComponentString("§cQuest not found: " + questId));
      } else if (def.isRepeatable()) {
         player.sendMessage(new TextComponentString("§cRepeatable quests must be accepted through the Quest Log GUI."));
      } else {
         String slot = getSlotForQuest(def);
         Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
         if (playerSlots != null && playerSlots.containsKey(slot)) {
            player.sendMessage(new TextComponentString("§cYou already have an active " + getCategoryDisplayName(slot) + " quest. Abandon it first."));
         } else if (!"shinobi_way".equals(def.getStoryline()) || !this.hasAdvancement(player, "inftsukaddon:shinobi_way") && !this.hasCompletedQuestForAdvancement(playerId, "inftsukaddon:shinobi_way")) {
            if (def.hasRequiredAdvancement()) {
               boolean advDone = this.hasAdvancement(player, def.getRequiredAdvancement());
               boolean questDone = this.hasCompletedQuestForAdvancement(playerId, def.getRequiredAdvancement());
               if (!advDone && !questDone) {
                  player.sendMessage(new TextComponentString("§cYou must complete the previous story arc first."));
                  return;
               }
            }

            Set<String> playerCompleted = (Set)this.completedQuests.getOrDefault(playerId, new HashSet());

            for(String prereq : def.getPrerequisites()) {
               if (!playerCompleted.contains(prereq)) {
                  QuestDefinition prereqDef = QuestRegistry.getById(prereq);
                  String prereqName = prereqDef != null ? prereqDef.getName() : prereq;
                  player.sendMessage(new TextComponentString("§cYou must complete \"§e" + prereqName + "§c\" first."));
                  return;
               }
            }

            if (playerCompleted.contains(questId)) {
               player.sendMessage(new TextComponentString("§cYou have already completed this quest."));
            } else {
               int questArc = def.getArcNumber();
               if (questArc > 0 && questArc != 99) {
                  int highestCompletedArc = 0;

                  for(String cqId : playerCompleted) {
                     QuestDefinition cqDef = QuestRegistry.getById(cqId);
                     if (cqDef != null && cqDef.getArcNumber() != 99) {
                        highestCompletedArc = Math.max(highestCompletedArc, cqDef.getArcNumber());
                     }
                  }

                  if (questArc < highestCompletedArc) {
                     player.sendMessage(new TextComponentString("§cYou have already progressed past this arc."));
                     return;
                  }
               }

               QuestInstance quest = new QuestInstance(questId);
               ((Map)this.activeQuests.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap())).put(slot, quest);
               QuestSavedData savedData = QuestSavedData.get(player.world);
               savedData.saveQuest(playerId, quest);
               player.sendMessage(new TextComponentString("§aQuest accepted: §6" + def.getName()));
               this.setupCurrentStep(player, quest);
               QuestNetworkHelper.sendQuestSync(player);
            }
         } else {
            player.sendMessage(new TextComponentString("§cYou have already completed Your Shinobi Way."));
         }
      }
   }

   public void abandonQuest(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      Map<String, QuestInstance> slots = (Map)this.activeQuests.get(playerId);
      if (slots != null) {
         for(String slot : slots.keySet()) {
            if (isStorySlot(slot)) {
               this.abandonQuestBySlot(player, slot);
               return;
            }
         }
      }

      player.sendMessage(new TextComponentString("§cYou don't have an active story quest."));
   }

   public void abandonQuestBySlot(EntityPlayerMP player, String slot) {
      if ("story".equals(slot)) {
         this.abandonQuest(player);
      } else {
         UUID playerId = player.getUniqueID();
         Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
         if (playerSlots != null && playerSlots.containsKey(slot)) {
            QuestInstance quest = (QuestInstance)playerSlots.remove(slot);
            if (playerSlots.isEmpty()) {
               this.activeQuests.remove(playerId);
            }

            this.areaStayProgress.remove(playerId);
            QuestStep abandonStep = quest.getCurrentStep();
            if (abandonStep != null && abandonStep.type == QuestStep.StepType.COMBAT && !quest.getSpawnedEntities().isEmpty()) {
               String encKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
               SharedEncounter enc = (SharedEncounter)this.sharedEncounters.get(encKey);
               if (enc != null && this.hasOtherLivingParticipants(player.world, encKey, playerId)) {
                  this.cleanEntityCacheForPlayer(quest, playerId);
                  quest.getSpawnedEntities().clear();
                  enc.removeParticipant(playerId);
               } else {
                  this.cleanEntityCacheFull(quest);
                  WaypointSpawnLogic.cleanupQuestEntities(player.world, quest.getSpawnedEntities());
                  if (enc != null) {
                     enc.removeParticipant(playerId);
                     if (enc.getAllParticipants().isEmpty()) {
                        this.sharedEncounters.remove(encKey);
                     }
                  }
               }

               if (!quest.getSceneEntities().isEmpty() && abandonStep.sceneGroup != null) {
                  this.cleanupSceneEntities(player, quest, abandonStep.sceneGroup);
               }
            } else if (abandonStep != null && abandonStep.type == QuestStep.StepType.DIALOG && abandonStep.hasNpcConfig()) {
               if (abandonStep.sceneGroup != null && !quest.getSceneEntities().isEmpty()) {
                  this.cleanupSceneEntities(player, quest, abandonStep.sceneGroup);
               }

               if (!quest.getSpawnedEntities().isEmpty()) {
                  if (this.otherPlayersNeedDialogNpc(abandonStep.npcConfigId, playerId)) {
                     this.cleanEntityCacheForPlayer(quest, playerId);
                     quest.getSpawnedEntities().clear();
                  } else {
                     this.cleanEntityCacheFull(quest);
                     WaypointSpawnLogic.cleanupQuestEntities(player.world, quest.getSpawnedEntities());
                     BlockPos npcPos = this.getStepPos(player.world, quest);
                     if (npcPos != null) {
                        this.clearSharedDialogNpc(abandonStep.npcConfigId, npcPos);
                     }
                  }
               }
            } else {
               this.cleanEntityCacheFull(quest);
               WaypointSpawnLogic.cleanupQuestEntities(player.world, quest.getSpawnedEntities());
               if (!quest.getSceneEntities().isEmpty() && abandonStep != null && abandonStep.sceneGroup != null) {
                  this.cleanupSceneEntities(player, quest, abandonStep.sceneGroup);
               }
            }

            this.waypointManager.clearWaypoint(player, quest.getQuestId());
            QuestSavedData savedData = QuestSavedData.get(player.world);
            savedData.removeActiveQuest(playerId, quest.getQuestId());
            String questName;
            if (quest.isGenerated()) {
               questName = quest.getGeneratedName() != null ? quest.getGeneratedName() : quest.getQuestId();
            } else {
               QuestDefinition def = QuestRegistry.getById(quest.getQuestId());
               questName = def != null ? def.getName() : quest.getQuestId();
            }

            player.sendMessage(new TextComponentString("§eQuest abandoned: §6" + questName));
            if (isRepeatableSubSlot(slot)) {
               String baseCategory = getBaseCategory(slot);
               if ("random".equals(baseCategory)) {
                  this.generateOffer(player, slot);
               } else {
                  int remaining = savedData.getRerollsRemaining(playerId, baseCategory);
                  if (remaining > 0) {
                     savedData.useReroll(playerId, baseCategory);
                     int left = savedData.getRerollsRemaining(playerId, baseCategory);
                     player.sendMessage(new TextComponentString("§e(" + left + " " + getCategoryDisplayName(slot) + " rerolls remaining)"));
                     this.generateOffer(player, slot);
                  } else {
                     player.sendMessage(new TextComponentString("§cNo rerolls remaining. New " + getCategoryDisplayName(slot) + " missions available at next reset."));
                  }
               }
            }

            QuestNetworkHelper.sendQuestSync(player);
         } else {
            player.sendMessage(new TextComponentString("§cYou don't have an active " + getCategoryDisplayName(slot) + " quest."));
         }
      }
   }

   public void advanceQuest(EntityPlayerMP player, String questId) {
      UUID playerId = player.getUniqueID();
      QuestInstance quest = this.findActiveQuestById(playerId, questId);
      if (quest != null) {
         QuestStep prevStep = quest.getCurrentStep();
         if (prevStep != null && prevStep.type == QuestStep.StepType.COMBAT) {
            this.sendCombatHealthClear(player);
         }

         if (quest.isOnFinalStep()) {
            this.completeQuest(player, quest);
         } else {
            if (!quest.getSpawnedEntities().isEmpty()) {
               String encKey = this.getEncounterKey(questId, quest.getCurrentStepIndex());
               SharedEncounter enc = (SharedEncounter)this.sharedEncounters.get(encKey);
               if (enc != null) {
                  enc.removeParticipant(playerId);
                  if (enc.getAllParticipants().isEmpty()) {
                     this.sharedEncounters.remove(encKey);
                     this.cleanEntityCacheFull(quest);
                     WaypointSpawnLogic.cleanupQuestEntities(player.world, quest.getSpawnedEntities());
                  } else {
                     this.cleanEntityCacheForPlayer(quest, playerId);
                  }
               } else {
                  this.cleanEntityCacheFull(quest);
                  WaypointSpawnLogic.cleanupQuestEntities(player.world, quest.getSpawnedEntities());
               }

               quest.getSpawnedEntities().clear();
            }

            String prevSceneGroup = prevStep != null ? prevStep.sceneGroup : null;
            QuestDefinition def = QuestRegistry.getById(questId);
            QuestStep nextStep = def != null ? def.getStep(quest.getCurrentStepIndex() + 1) : null;
            String nextSceneGroup = nextStep != null ? nextStep.sceneGroup : null;
            boolean sceneGroupChanging = prevSceneGroup != null && !prevSceneGroup.equals(nextSceneGroup) || prevSceneGroup != null && nextSceneGroup == null;
            if (sceneGroupChanging && !quest.getSceneEntities().isEmpty()) {
               this.cleanupSceneEntities(player, quest, prevSceneGroup);
            }

            if (prevStep != null && prevStep.onCompleteCommand != null) {
               this.executeOnCompleteCommand(player, prevStep.onCompleteCommand);
            }

            quest.advanceStep();
            if (sceneGroupChanging) {
               quest.clearSceneEntities();
            }

            if (!quest.isGenerated()) {
               try {
                  BattlePassManager.getInstance().awardXP(playerId, 125, "story_quest", player.world);
               } catch (Exception var12) {
               }
            }

            QuestSavedData savedData = QuestSavedData.get(player.world);
            savedData.saveQuest(playerId, quest);
            this.setupCurrentStep(player, quest);
            QuestNetworkHelper.sendQuestSync(player);
         }

      }
   }

   public void advanceQuest(EntityPlayerMP player, QuestInstance quest) {
      this.advanceQuest(player, quest.getQuestId());
   }

   private void executeOnCompleteCommand(EntityPlayerMP player, String rawCommand) {
      if (player != null && rawCommand != null && !rawCommand.isEmpty()) {
         String cmd = rawCommand.replace("{player}", player.getName());
         if (cmd.startsWith("tp ")) {
            String[] parts = cmd.split("\\s+");
            if (parts.length >= 5) {
               try {
                  double x = Double.parseDouble(parts[2]);
                  double y = Double.parseDouble(parts[3]);
                  double z = Double.parseDouble(parts[4]);
                  float yaw = player.rotationYaw;
                  float pitch = player.rotationPitch;
                  if (parts.length >= 7) {
                     yaw = Float.parseFloat(parts[5]);
                     pitch = Float.parseFloat(parts[6]);
                  }

                  player.dismountRidingEntity();
                  player.connection.setPlayerLocation(x + (double)0.5F, y, z + (double)0.5F, yaw, pitch);
                  return;
               } catch (NumberFormatException var13) {
               }
            }
         }

         MinecraftServer srv = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (srv != null) {
            srv.getCommandManager().executeCommand(srv, cmd);
         }

      }
   }

   private void completeQuest(EntityPlayerMP player, QuestInstance quest) {
      UUID playerId = player.getUniqueID();
      String questId = quest.getQuestId();
      String slot;
      if (quest.isGenerated()) {
         slot = getSlotForInstance(quest);
      } else {
         QuestDefinition def = QuestRegistry.getById(questId);
         slot = def != null ? getSlotForQuest(def) : storySlotFor("leaf_story");
      }

      QuestStep finalStep = quest.getCurrentStep();
      if (finalStep != null && finalStep.onCompleteCommand != null) {
         this.executeOnCompleteCommand(player, finalStep.onCompleteCommand);
      }

      quest.complete();
      Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
      if (playerSlots != null) {
         playerSlots.remove(slot);
         if (playerSlots.isEmpty()) {
            this.activeQuests.remove(playerId);
         }
      }

      String encKey = this.getEncounterKey(questId, quest.getCurrentStepIndex());
      SharedEncounter enc = (SharedEncounter)this.sharedEncounters.get(encKey);
      if (enc != null) {
         enc.removeParticipant(playerId);
         if (enc.getAllParticipants().isEmpty()) {
            this.sharedEncounters.remove(encKey);
         }
      }

      this.waypointManager.clearWaypoint(player, questId);
      if (!quest.getSceneEntities().isEmpty()) {
         QuestStep lastStep = quest.getCurrentStep();
         String sceneGroup = lastStep != null ? lastStep.sceneGroup : null;
         if (sceneGroup != null) {
            this.cleanupSceneEntities(player, quest, sceneGroup);
         } else {
            for(UUID entityUUID : quest.getSceneEntities().values()) {
               Entity entity = this.findEntityByUUID(player.world, entityUUID);
               if (entity != null) {
                  entity.setDead();
               }

               Set<UUID> players = (Set)this.entityToPlayers.get(entityUUID);
               if (players != null) {
                  players.remove(playerId);
                  if (players.isEmpty()) {
                     this.entityToPlayers.remove(entityUUID);
                  }
               }
            }

            quest.clearSceneEntities();
         }
      }

      if (!quest.getSpawnedEntities().isEmpty()) {
         this.cleanEntityCacheFull(quest);
         WaypointSpawnLogic.cleanupQuestEntities(player.world, quest.getSpawnedEntities());
         quest.getSpawnedEntities().clear();
      }

      QuestSavedData savedData = QuestSavedData.get(player.world);
      if (quest.isGenerated()) {
         savedData.recordSlotCompletion(playerId, slot);
         savedData.removeActiveQuest(playerId, questId);
         String questName = quest.getGeneratedName() != null ? quest.getGeneratedName() : questId;
         StatManager statMgr = StatManager.getInstance();
         int spReward = statMgr.getSPForRank(quest.getGeneratedRankOrdinal());
         boolean spGranted = spReward > 0 && statMgr.grantSP(player, spReward);
         String completeLine = "§6§l Quest Complete: §a" + questName;
         if (spGranted) {
            completeLine = completeLine + " §r§d| +" + spReward + " SP";
         }

         player.sendMessage(new TextComponentString(completeLine));
         int xp = quest.getGeneratedXpReward();
         if (xp > 0) {
            QuestReward.xp(xp).grant(player);
         }

         RyoRewardHelper.grantGeneratedQuestReward(player, quest, slot);
         EndgameSavedData endgameData = EndgameSavedData.get(player.world);
         String rankLetter = quest.getGeneratedRankOrdinal() < QuestDefinition.QuestRank.values().length ? QuestDefinition.QuestRank.values()[quest.getGeneratedRankOrdinal()].displayName : "D";
         int pveXpAmount = PveRank.getMissionXp(rankLetter);
         PveRank pveRankBefore = endgameData.getPveRank(playerId);
         endgameData.addPveXp(playerId, pveXpAmount);
         PveRank pveRankAfter = endgameData.getPveRank(playerId);
         player.sendMessage(new TextComponentString("§b+" + pveXpAmount + " PvE XP"));
         if (pveRankAfter != pveRankBefore) {
            player.sendMessage(new TextComponentString("§6§l★ PvE RANK UP! §r§e" + pveRankBefore.getDisplayName() + " → " + pveRankAfter.getDisplayName()));
         }

         try {
            if (slot.startsWith("daily_")) {
               BattlePassManager.getInstance().awardXP(playerId, 125, "daily_mission", player.world);
            } else if (slot.startsWith("weekly_")) {
               BattlePassManager.getInstance().awardXP(playerId, 350, "weekly_mission", player.world);
            }
         } catch (Exception var24) {
         }

         this.generateOffer(player, slot);
      } else {
         QuestDefinition def = QuestRegistry.getById(questId);
         if (def != null && def.isRepeatable()) {
            savedData.recordRepeatableCompletion(playerId, questId);
            savedData.removeActiveQuest(playerId, questId);
         } else {
            ((Set)this.completedQuests.computeIfAbsent(playerId, (k) -> new HashSet())).add(questId);
            savedData.completeQuest(playerId, questId);
         }

         if (def != null) {
            player.sendMessage(new TextComponentString("§6§l Quest Complete: §a" + def.getName()));

            for(QuestReward reward : def.getRewards()) {
               reward.grant(player);
            }

            if (!def.isRepeatable()) {
               RyoRewardHelper.grantStoryQuestReward(player, def);
            }

            if (!def.isRepeatable()) {
               try {
                  BattlePassManager.getInstance().awardXP(playerId, 125, "story_quest", player.world);
               } catch (Exception var23) {
               }
            }
         }
      }

      QuestNetworkHelper.sendQuestSync(player);
      if (!quest.isGenerated()) {
         this.ensureHighestRank(player, playerId);
      }

      if ("bell_test".equals(questId)) {
         this.grantAdvancement(player, "inftsukaddon:becomegenin");
      }

      if ("land_of_waves_end".equals(questId)) {
         this.grantAdvancement(player, "inftsukaddon:landofwaves");
      }

      if ("surge_of_rebirth".equals(questId)) {
         this.grantAdvancement(player, "inftsukaddon:shinobi_way");
      }

      if ("preliminary_victory".equals(questId)) {
         this.grantAdvancement(player, "inftsukaddon:chuninexams");
      }

      if ("hiruzen_death".equals(questId)) {
         this.grantAdvancement(player, "inftsukaddon:arc3_complete");
      }

      if ("surge_of_rebirth".equals(questId)) {
         this.triggerKGRoll(player);
      }

      if (!quest.isGenerated()) {
         QuestDefinition completedDef = QuestRegistry.getById(questId);
         if (completedDef != null && !completedDef.isRepeatable()) {
            String completedStoryline = completedDef.getStoryline();

            for(QuestDefinition candidate : QuestRegistry.getAll()) {
               if (!candidate.isRepeatable() && candidate.getStoryline().equals(completedStoryline)) {
                  List<String> prereqs = candidate.getPrerequisites();
                  if (prereqs.size() == 1 && prereqs.contains(questId)) {
                     this.acceptQuest(player, candidate.getId());
                     break;
                  }
               }
            }
         }
      }

   }

   private void triggerKGRoll(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      QuestSavedData savedData = QuestSavedData.get(player.world);
      if (savedData.hasFlag(playerId, "kg_received")) {
         player.sendMessage(new TextComponentString("§c You have already received a Kekkei Genkai from this quest."));
      } else {
         Random rand = new Random();
         int roll = rand.nextInt(100);
         int wonIndex = 0;
         int cumulative = 0;

         for(int i = 0; i < KG_TABLE.length; ++i) {
            cumulative += Integer.parseInt(KG_TABLE[i][2]);
            if (roll < cumulative) {
               wonIndex = i;
               break;
            }
         }

         String wonItemId = KG_TABLE[wonIndex][0];
         String wonDisplayName = KG_TABLE[wonIndex][1];
         int wonRarity = Integer.parseInt(KG_TABLE[wonIndex][3]);
         List<KGRollResultMessage.StripEntryData> strip = new ArrayList(60);

         for(int i = 0; i < 60; ++i) {
            if (i == 45) {
               strip.add(new KGRollResultMessage.StripEntryData(wonItemId, wonDisplayName, wonRarity));
            } else if (i > 40 && i < 45) {
               if (rand.nextInt(3) == 0) {
                  strip.add(new KGRollResultMessage.StripEntryData(wonItemId, wonDisplayName, wonRarity));
               } else {
                  int ri = rand.nextInt(KG_TABLE.length);
                  strip.add(new KGRollResultMessage.StripEntryData(KG_TABLE[ri][0], KG_TABLE[ri][1], Integer.parseInt(KG_TABLE[ri][3])));
               }
            } else {
               int ri = rand.nextInt(KG_TABLE.length);
               strip.add(new KGRollResultMessage.StripEntryData(KG_TABLE[ri][0], KG_TABLE[ri][1], Integer.parseInt(KG_TABLE[ri][3])));
            }
         }

         this.pendingKGRolls.put(playerId, new String[]{wonItemId, wonDisplayName});
         KGRollResultMessage msg = new KGRollResultMessage(wonItemId, wonDisplayName, wonRarity, 45, strip);
         QuestModInit.NETWORK.sendTo(msg, player);
      }
   }

   public void claimKGRoll(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      String[] pending = (String[])this.pendingKGRolls.remove(playerId);
      if (pending != null) {
         QuestSavedData savedData = QuestSavedData.get(player.world);
         if (!savedData.hasFlag(playerId, "kg_received")) {
            savedData.setFlag(playerId, "kg_received");
            String itemId = pending[0];
            String displayName = pending[1];

            try {
               Item item = (Item)Item.REGISTRY.getObject(new ResourceLocation(itemId));
               if (item != null) {
                  ItemStack stack = new ItemStack(item, 1);
                  if (!player.inventory.addItemStackToInventory(stack)) {
                     EntityItem drop = new EntityItem(player.world, player.posX, player.posY + (double)0.5F, player.posZ, stack);
                     drop.setNoPickupDelay();
                     player.world.spawnEntity(drop);
                  }
               } else {
                  System.err.println("[QuestSystem] KG item not found in registry: " + itemId);
               }
            } catch (Exception e) {
               System.err.println("[QuestSystem] Failed to give KG item: " + e.getMessage());
            }

            player.sendMessage(new TextComponentString("§d§lYou have awakened §e§l" + displayName + "§d§l!"));
         }
      }
   }

   private BlockPos getStepPos(World world, QuestInstance quest) {
      return this.getStepPos(world, quest, false);
   }

   private BlockPos getStepPos(World world, QuestInstance quest, boolean skipGroundY) {
      BlockPos pos;
      if (quest.isGenerated()) {
         pos = quest.getCurrentStepPosition();
         if (!skipGroundY) {
            int safeY = WaypointSpawnLogic.findGroundY(world, pos.getX(), pos.getZ());
            pos = new BlockPos(pos.getX(), safeY, pos.getZ());
         }
      } else {
         QuestSavedData savedData = QuestSavedData.get(world);
         pos = savedData.getEffectivePosition(quest.getQuestId(), quest.getCurrentStepIndex());
         if (pos == null) {
            System.out.println("[QuestManager] WARNING: No position found for quest " + quest.getQuestId() + " step " + quest.getCurrentStepIndex() + " — skipping spawn");
            return null;
         }

         if (pos.getX() == 0 && pos.getZ() == 0 && pos.getY() <= 1) {
            System.out.println("[QuestManager] WARNING: Position is world origin (0,0,0) for quest " + quest.getQuestId() + " step " + quest.getCurrentStepIndex() + " — skipping spawn");
            return null;
         }

         QuestDefinition def = QuestRegistry.getById(quest.getQuestId());
         if (!skipGroundY && pos.getY() <= 1 && def != null && def.getArcNumber() >= 5) {
            int safeY = WaypointSpawnLogic.findGroundY(world, pos.getX(), pos.getZ());
            pos = new BlockPos(pos.getX(), safeY, pos.getZ());
         }
      }

      return pos;
   }

   private void setupCurrentStep(EntityPlayerMP player, QuestInstance quest) {
      this.setupCurrentStep(player, quest, false);
   }

   private void setupCurrentStep(EntityPlayerMP player, QuestInstance quest, boolean skipGroundY) {
      QuestStep step = quest.getCurrentStep();
      if (step != null) {
         BlockPos pos = this.getStepPos(player.world, quest, skipGroundY);
         if (pos == null) {
            player.sendMessage(new TextComponentString("§cQuest position data is missing — contact an admin."));
         } else {
            pos = WaypointManager.validatePosition(player.world, pos);
            WaypointData.WaypointType wpType = WaypointData.fromStepType(step.type);
            WaypointData waypoint = new WaypointData(pos, wpType, step.description);
            this.waypointManager.setWaypoint(player, quest.getQuestId(), waypoint);
            if (step.type == QuestStep.StepType.DIALOG && !step.hasNpcConfig()) {
               BlockPos playerPos = player.getPosition();
               double dx = (double)(playerPos.getX() - pos.getX());
               double dz = (double)(playerPos.getZ() - pos.getZ());
               int dy = Math.abs(playerPos.getY() - pos.getY());
               if (dx * dx + dz * dz <= (double)100.0F && dy <= 4) {
                  this.waypointManager.clearWaypoint(player, quest.getQuestId());
                  QuestNetworkHelper.sendDialog(player, step, quest.getCurrentStepIndex());
                  return;
               }
            }

            player.sendMessage(new TextComponentString("§eObjective: §f" + step.description));
         }
      }
   }

   public void onWaypointReached(EntityPlayerMP player, String questId) {
      UUID playerId = player.getUniqueID();
      QuestInstance quest = this.findActiveQuestById(playerId, questId);
      if (quest != null) {
         QuestStep step = quest.getCurrentStep();
         if (step != null) {
            this.waypointManager.clearWaypoint(player, questId);
            switch (step.type) {
               case TRAVEL:
                  player.sendMessage(new TextComponentString("§aYou've reached the destination!"));
                  this.advanceQuest(player, quest);
                  break;
               case COMBAT:
                  this.startCombatStep(player, quest, step);
                  break;
               case DIALOG:
                  if (step.hasNpcConfig()) {
                     this.startDialogNpcStep(player, quest, step);
                  } else {
                     QuestNetworkHelper.sendDialog(player, step, quest.getCurrentStepIndex());
                  }
                  break;
               case INTERACT:
                  player.sendMessage(new TextComponentString("§aInteraction complete!"));
                  this.advanceQuest(player, quest);
                  break;
               case AREA_STAY:
                  int totalSeconds = step.stayDurationTicks / 20;
                  player.sendMessage(new TextComponentString("§b✦ Stay in this area for " + totalSeconds + " seconds. Don't leave the " + (int)step.stayRadius + "-block radius."));
                  this.areaStayProgress.put(player.getUniqueID(), 0);
            }

         }
      }
   }

   public void onWaypointReached(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      Map<String, QuestInstance> slots = (Map)this.activeQuests.get(playerId);
      if (slots != null) {
         for(Map.Entry<String, QuestInstance> entry : slots.entrySet()) {
            if (isStorySlot((String)entry.getKey())) {
               this.onWaypointReached(player, ((QuestInstance)entry.getValue()).getQuestId());
               return;
            }
         }
      }

   }

   private void startDialogNpcStep(EntityPlayerMP player, QuestInstance quest, QuestStep step) {
      if (step.sceneGroup != null) {
         this.startSceneDialogStep(player, quest, step);
      } else {
         BlockPos pos = this.getStepPos(player.world, quest);
         if (pos == null) {
            player.sendMessage(new TextComponentString("§cCannot spawn NPC — quest position data is missing."));
         } else {
            String sharingKey = step.npcConfigId + "::" + pos.getX() + "::" + pos.getY() + "::" + pos.getZ();
            UUID existingNpcUUID = (UUID)this.sharedDialogNpcs.get(sharingKey);
            if (existingNpcUUID != null) {
               Entity existing = this.findEntityByUUID(player.world, existingNpcUUID);
               if (existing != null && existing.isEntityAlive()) {
                  quest.trackEntity(existingNpcUUID);
                  ((Set)this.entityToPlayers.computeIfAbsent(existingNpcUUID, (k) -> new HashSet())).add(player.getUniqueID());
                  QuestSavedData.get(player.world).saveQuest(player.getUniqueID(), quest);
                  String displayName = step.npcName != null ? step.npcName : "NPC";
                  player.sendMessage(new TextComponentString("§a" + displayName + " is here! Right-click to speak."));
                  return;
               }

               this.sharedDialogNpcs.remove(sharingKey, existingNpcUUID);
            }

            Entity worldScanHit = this.findExistingNpcByConfigId(player.world, step.npcConfigId, pos);
            if (worldScanHit != null) {
               UUID hitUUID = worldScanHit.getUniqueID();
               quest.trackEntity(hitUUID);
               ((Set)this.entityToPlayers.computeIfAbsent(hitUUID, (k) -> new HashSet())).add(player.getUniqueID());
               this.sharedDialogNpcs.put(sharingKey, hitUUID);
               QuestSavedData.get(player.world).saveQuest(player.getUniqueID(), quest);
               String displayName = step.npcName != null ? step.npcName : "NPC";
               player.sendMessage(new TextComponentString("§a" + displayName + " is here! Right-click to speak."));
            } else {
               Entity npc = WaypointSpawnLogic.spawnQuestNpc(player.world, player, step.npcConfigId, pos, quest.getQuestId(), quest.isGenerated());
               if (npc != null) {
                  quest.trackEntity(npc.getUniqueID());
                  ((Set)this.entityToPlayers.computeIfAbsent(npc.getUniqueID(), (k) -> new HashSet())).add(player.getUniqueID());
                  QuestSavedData.get(player.world).saveQuest(player.getUniqueID(), quest);
                  UUID racedUUID = (UUID)this.sharedDialogNpcs.putIfAbsent(sharingKey, npc.getUniqueID());
                  if (racedUUID != null && !racedUUID.equals(npc.getUniqueID())) {
                     Entity racedEntity = this.findEntityByUUID(player.world, racedUUID);
                     if (racedEntity != null && racedEntity.isEntityAlive()) {
                        npc.setDead();
                        quest.removeSpawnedEntity(npc.getUniqueID());
                        quest.trackEntity(racedUUID);
                        ((Set)this.entityToPlayers.computeIfAbsent(racedUUID, (k) -> new HashSet())).add(player.getUniqueID());
                     } else {
                        this.sharedDialogNpcs.put(sharingKey, npc.getUniqueID());
                     }
                  }

                  String displayName = step.npcName != null ? step.npcName : "NPC";
                  player.sendMessage(new TextComponentString("§a" + displayName + " has appeared! Right-click to speak."));
               } else {
                  System.out.println("[QuestManager] NPC spawn failed for config: " + step.npcConfigId + ", falling back to auto-dialog");
                  QuestNetworkHelper.sendDialog(player, step, quest.getCurrentStepIndex());
               }

            }
         }
      }
   }

   private void startSceneDialogStep(EntityPlayerMP player, QuestInstance quest, QuestStep step) {
      if (quest.getSceneEntities().isEmpty()) {
         this.spawnSceneNpcs(player, quest, step);
      }

      UUID npcUUID = quest.getSceneEntity(step.npcConfigId);
      if (npcUUID != null) {
         Entity npc = this.findEntityByUUID(player.world, npcUUID);
         if (npc != null && npc.isEntityAlive()) {
            quest.trackEntity(npcUUID);
            ((Set)this.entityToPlayers.computeIfAbsent(npcUUID, (k) -> new HashSet())).add(player.getUniqueID());
            QuestSavedData.get(player.world).saveQuest(player.getUniqueID(), quest);
            String displayName = step.npcName != null ? step.npcName : "NPC";
            player.sendMessage(new TextComponentString("§a" + displayName + " is waiting! Right-click to speak."));
            return;
         }
      }

      BlockPos pos = this.getStepPos(player.world, quest);
      if (pos == null) {
         player.sendMessage(new TextComponentString("§cCannot spawn scene NPC — quest position data is missing."));
      } else {
         pos = WaypointManager.validatePosition(player.world, pos);
         Entity worldScanHit = this.findExistingNpcByConfigId(player.world, step.npcConfigId, pos);
         if (worldScanHit != null) {
            UUID hitUUID = worldScanHit.getUniqueID();
            quest.trackSceneEntity(step.npcConfigId, hitUUID);
            quest.trackEntity(hitUUID);
            ((Set)this.entityToPlayers.computeIfAbsent(hitUUID, (k) -> new HashSet())).add(player.getUniqueID());
            QuestSavedData.get(player.world).saveQuest(player.getUniqueID(), quest);
            String sharingKey = step.npcConfigId + "::" + pos.getX() + "::" + pos.getY() + "::" + pos.getZ();
            this.sharedDialogNpcs.put(sharingKey, hitUUID);
            String displayName = step.npcName != null ? step.npcName : "NPC";
            player.sendMessage(new TextComponentString("§a" + displayName + " is waiting! Right-click to speak."));
         } else {
            Entity newNpc = WaypointSpawnLogic.spawnQuestNpc(player.world, player, step.npcConfigId, pos, quest.getQuestId(), quest.isGenerated());
            if (newNpc != null) {
               quest.trackSceneEntity(step.npcConfigId, newNpc.getUniqueID());
               quest.trackEntity(newNpc.getUniqueID());
               ((Set)this.entityToPlayers.computeIfAbsent(newNpc.getUniqueID(), (k) -> new HashSet())).add(player.getUniqueID());
               QuestSavedData.get(player.world).saveQuest(player.getUniqueID(), quest);
               String sharingKey = step.npcConfigId + "::" + pos.getX() + "::" + pos.getY() + "::" + pos.getZ();
               this.sharedDialogNpcs.put(sharingKey, newNpc.getUniqueID());
               String displayName = step.npcName != null ? step.npcName : "NPC";
               player.sendMessage(new TextComponentString("§a" + displayName + " is waiting! Right-click to speak."));
            } else {
               System.out.println("[QuestManager] Scene NPC spawn failed for config: " + step.npcConfigId + ", falling back to auto-dialog");
               QuestNetworkHelper.sendDialog(player, step, quest.getCurrentStepIndex());
            }

         }
      }
   }

   private void spawnSceneNpcs(EntityPlayerMP player, QuestInstance quest, QuestStep currentStep) {
      QuestDefinition def = QuestRegistry.getById(quest.getQuestId());
      if (def != null) {
         QuestSavedData savedData = QuestSavedData.get(player.world);

         for(int i = 0; i < def.getStepCount(); ++i) {
            QuestStep s = def.getStep(i);
            if (s.sceneGroup != null && s.sceneGroup.equals(currentStep.sceneGroup) && s.type == QuestStep.StepType.DIALOG && s.hasNpcConfig() && quest.getSceneEntity(s.npcConfigId) == null) {
               BlockPos pos = savedData.getEffectivePosition(quest.getQuestId(), i);
               pos = WaypointManager.validatePosition(player.world, pos);
               String sharingKey = s.npcConfigId + "::" + pos.getX() + "::" + pos.getY() + "::" + pos.getZ();
               UUID existingNpcUUID = (UUID)this.sharedDialogNpcs.get(sharingKey);
               if (existingNpcUUID != null) {
                  Entity existing = this.findEntityByUUID(player.world, existingNpcUUID);
                  if (existing != null && existing.isEntityAlive()) {
                     quest.trackSceneEntity(s.npcConfigId, existingNpcUUID);
                     ((Set)this.entityToPlayers.computeIfAbsent(existingNpcUUID, (k) -> new HashSet())).add(player.getUniqueID());
                     continue;
                  }

                  this.sharedDialogNpcs.remove(sharingKey);
               }

               Entity worldScanHit = this.findExistingNpcByConfigId(player.world, s.npcConfigId, pos);
               if (worldScanHit != null) {
                  UUID hitUUID = worldScanHit.getUniqueID();
                  quest.trackSceneEntity(s.npcConfigId, hitUUID);
                  ((Set)this.entityToPlayers.computeIfAbsent(hitUUID, (k) -> new HashSet())).add(player.getUniqueID());
                  this.sharedDialogNpcs.put(sharingKey, hitUUID);
               } else {
                  Entity npc = WaypointSpawnLogic.spawnQuestNpc(player.world, player, s.npcConfigId, pos, quest.getQuestId(), quest.isGenerated());
                  if (npc != null) {
                     quest.trackSceneEntity(s.npcConfigId, npc.getUniqueID());
                     quest.trackEntity(npc.getUniqueID());
                     ((Set)this.entityToPlayers.computeIfAbsent(npc.getUniqueID(), (k) -> new HashSet())).add(player.getUniqueID());
                     this.sharedDialogNpcs.put(sharingKey, npc.getUniqueID());
                  }
               }
            }
         }

         for(QuestDefinition.SceneNpc sceneNpc : def.getSceneNpcsForGroup(currentStep.sceneGroup)) {
            if (quest.getSceneEntity(sceneNpc.npcConfigId) == null) {
               BlockPos pos = WaypointManager.validatePosition(player.world, sceneNpc.position);
               String sharingKey = sceneNpc.npcConfigId + "::" + pos.getX() + "::" + pos.getY() + "::" + pos.getZ();
               UUID existingNpcUUID = (UUID)this.sharedDialogNpcs.get(sharingKey);
               if (existingNpcUUID != null) {
                  Entity existing = this.findEntityByUUID(player.world, existingNpcUUID);
                  if (existing != null && existing.isEntityAlive()) {
                     quest.trackSceneEntity(sceneNpc.npcConfigId, existingNpcUUID);
                     ((Set)this.entityToPlayers.computeIfAbsent(existingNpcUUID, (k) -> new HashSet())).add(player.getUniqueID());
                     continue;
                  }

                  this.sharedDialogNpcs.remove(sharingKey);
               }

               Entity worldScanHit = this.findExistingNpcByConfigId(player.world, sceneNpc.npcConfigId, pos);
               if (worldScanHit != null) {
                  UUID hitUUID = worldScanHit.getUniqueID();
                  quest.trackSceneEntity(sceneNpc.npcConfigId, hitUUID);
                  ((Set)this.entityToPlayers.computeIfAbsent(hitUUID, (k) -> new HashSet())).add(player.getUniqueID());
                  this.sharedDialogNpcs.put(sharingKey, hitUUID);
               } else {
                  Entity npc = WaypointSpawnLogic.spawnQuestNpc(player.world, player, sceneNpc.npcConfigId, pos, quest.getQuestId(), quest.isGenerated());
                  if (npc != null) {
                     quest.trackSceneEntity(sceneNpc.npcConfigId, npc.getUniqueID());
                     ((Set)this.entityToPlayers.computeIfAbsent(npc.getUniqueID(), (k) -> new HashSet())).add(player.getUniqueID());
                     this.sharedDialogNpcs.put(sharingKey, npc.getUniqueID());
                  }
               }
            }
         }

         savedData.saveQuest(player.getUniqueID(), quest);
      }
   }

   private void cleanupSceneEntities(EntityPlayerMP player, QuestInstance quest, String sceneGroup) {
      if (!quest.getSceneEntities().isEmpty()) {
         boolean othersInScene = this.otherPlayersInScene(quest.getQuestId(), sceneGroup, player.getUniqueID());

         for(Map.Entry<String, UUID> entry : new ArrayList(quest.getSceneEntities().entrySet())) {
            UUID entityUUID = (UUID)entry.getValue();
            Set<UUID> players = (Set)this.entityToPlayers.get(entityUUID);
            if (players != null) {
               players.remove(player.getUniqueID());
               if (players.isEmpty()) {
                  this.entityToPlayers.remove(entityUUID);
               }
            }

            if (!othersInScene) {
               Entity entity = this.findEntityByUUID(player.world, entityUUID);
               if (entity != null) {
                  entity.setDead();
               }
            }
         }

         if (!othersInScene) {
            QuestDefinition def = QuestRegistry.getById(quest.getQuestId());
            if (def != null) {
               QuestSavedData savedData = QuestSavedData.get(player.world);

               for(int i = 0; i < def.getStepCount(); ++i) {
                  QuestStep s = def.getStep(i);
                  if (s.sceneGroup != null && s.sceneGroup.equals(sceneGroup) && s.hasNpcConfig()) {
                     BlockPos pos = savedData.getEffectivePosition(quest.getQuestId(), i);
                     pos = WaypointManager.validatePosition(player.world, pos);
                     this.clearSharedDialogNpc(s.npcConfigId, pos);
                  }
               }

               for(QuestDefinition.SceneNpc sceneNpc : def.getSceneNpcsForGroup(sceneGroup)) {
                  BlockPos pos = WaypointManager.validatePosition(player.world, sceneNpc.position);
                  String sharingKey = sceneNpc.npcConfigId + "::" + pos.getX() + "::" + pos.getY() + "::" + pos.getZ();
                  this.sharedDialogNpcs.remove(sharingKey);
               }
            }
         }

         quest.clearSceneEntities();
      }
   }

   private boolean otherPlayersInScene(String questId, String sceneGroup, UUID excludePlayer) {
      for(Map.Entry<UUID, Map<String, QuestInstance>> playerEntry : new ArrayList(this.activeQuests.entrySet())) {
         if (!((UUID)playerEntry.getKey()).equals(excludePlayer)) {
            for(QuestInstance q : ((Map)playerEntry.getValue()).values()) {
               QuestStep s = q.getCurrentStep();
               if (s != null && sceneGroup.equals(s.sceneGroup)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean otherPlayersNeedDialogNpc(String npcConfigId, UUID excludePlayer) {
      for(Map.Entry<UUID, Map<String, QuestInstance>> playerEntry : new ArrayList(this.activeQuests.entrySet())) {
         if (!((UUID)playerEntry.getKey()).equals(excludePlayer)) {
            for(QuestInstance q : ((Map)playerEntry.getValue()).values()) {
               QuestStep s = q.getCurrentStep();
               if (s != null && s.type == QuestStep.StepType.DIALOG && s.hasNpcConfig() && npcConfigId.equals(s.npcConfigId)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private void clearSharedDialogNpc(String npcConfigId, BlockPos pos) {
      String sharingKey = npcConfigId + "::" + pos.getX() + "::" + pos.getY() + "::" + pos.getZ();
      UUID removedUUID = (UUID)this.sharedDialogNpcs.remove(sharingKey);
      Iterator<Map.Entry<String, UUID>> iter = this.sharedDialogNpcs.entrySet().iterator();

      while(iter.hasNext()) {
         Map.Entry<String, UUID> entry = (Map.Entry)iter.next();
         if (((String)entry.getKey()).startsWith(npcConfigId + "::")) {
            iter.remove();
         }
      }

   }

   private void killNpcsByConfigNearPos(World world, String npcConfigId, BlockPos pos, double radius) {
      if (npcConfigId != null && pos != null) {
         AxisAlignedBB searchBox = new AxisAlignedBB((double)pos.getX() - radius, (double)(pos.getY() - 5), (double)pos.getZ() - radius, (double)pos.getX() + radius, (double)(pos.getY() + 5), (double)pos.getZ() + radius);

         for(Entity e : world.getEntitiesWithinAABB(Entity.class, searchBox)) {
            if (!e.isDead && e instanceof INpcConfigurable) {
               INpcConfigurable configurable = (INpcConfigurable)e;
               if (npcConfigId.equals(configurable.getNpcConfigId())) {
                  e.setDead();
               }
            }
         }

      }
   }

   private void startCombatStep(EntityPlayerMP player, QuestInstance quest, QuestStep step) {
      String debugPrefix = "[QuestCombat:" + quest.getQuestId() + ":" + quest.getCurrentStepIndex() + " for " + player.getName() + "] ";
      if (!quest.getSpawnedEntities().isEmpty()) {
         boolean anyAlive = false;

         for(UUID uuid : quest.getSpawnedEntities()) {
            Entity existing = this.findEntityByUUID(player.world, uuid);
            if (existing != null && existing.isEntityAlive()) {
               anyAlive = true;
               break;
            }
         }

         if (anyAlive) {
            System.out.println(debugPrefix + "PATH=OWN_ENTITIES_ALIVE — skipping spawn, combat in progress");
            this.waypointManager.clearWaypoint(player, quest.getQuestId());
            QuestNetworkHelper.sendQuestSync(player);
            return;
         }

         System.out.println(debugPrefix + "Clearing " + quest.getSpawnedEntities().size() + " stale entity UUIDs");
         this.cleanEntityCacheForPlayer(quest, player.getUniqueID());
         quest.getSpawnedEntities().clear();
      }

      if (step.spawnEnemies) {
         String encKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
         SharedEncounter existingEnc = (SharedEncounter)this.sharedEncounters.get(encKey);
         if (existingEnc != null) {
            boolean hasLiving = existingEnc.hasLivingEntities(player.world);
            if (hasLiving) {
               System.out.println(debugPrefix + "PATH=SHARED_ENCOUNTER_JOIN — joining encounter " + encKey + " with " + existingEnc.getEntityUUIDs().size() + " entities, " + existingEnc.getAllParticipants().size() + " participants");
               existingEnc.addParticipant(player.getUniqueID());

               for(UUID entityUUID : existingEnc.getEntityUUIDs()) {
                  quest.trackEntity(entityUUID);
                  ((Set)this.entityToPlayers.computeIfAbsent(entityUUID, (k) -> new HashSet())).add(player.getUniqueID());
               }

               this.waypointManager.clearWaypoint(player, quest.getQuestId());
               QuestSavedData.get(player.world).saveQuest(player.getUniqueID(), quest);
               player.sendMessage(new TextComponentString("§aYou've joined an ongoing battle!"));
               QuestNetworkHelper.sendQuestSync(player);
               return;
            }

            System.out.println(debugPrefix + "SharedEncounter " + encKey + " exists but hasLivingEntities=false (entities in unloaded chunks or dead), removing stale encounter");
            this.sharedEncounters.remove(encKey);
         } else {
            System.out.println(debugPrefix + "No SharedEncounter found for key " + encKey);
         }
      }

      if (!quest.isGenerated() && step.npcConfigIds != null) {
         BlockPos spawnPos = this.getStepPos(player.world, quest);
         if (spawnPos == null) {
            return;
         }

         List<Entity> existingNpcs = new ArrayList();
         List<Entity> staleNpcs = new ArrayList();

         for(Entity e : new ArrayList(player.world.loadedEntityList)) {
            if (e != null && !e.isDead) {
               NBTTagCompound data = e.getEntityData();
               if (data.getBoolean("questEntity")) {
                  String configId = data.getString("npcConfigId");
                  if (!configId.isEmpty()) {
                     for(String stepConfig : step.npcConfigIds) {
                        if (stepConfig.equals(configId)) {
                           double dx = e.posX - (double)spawnPos.getX();
                           double dz = e.posZ - (double)spawnPos.getZ();
                           if (dx * dx + dz * dz <= (double)10000.0F) {
                              existingNpcs.add(e);
                           } else {
                              staleNpcs.add(e);
                           }
                           break;
                        }
                     }
                  }
               }
            }
         }

         for(Entity stale : staleNpcs) {
            System.out.println(debugPrefix + "Killing stale NPC " + stale.getCustomNameTag() + " (too far from spawn: " + String.format("%.0f", stale.getDistance((double)spawnPos.getX(), (double)spawnPos.getY(), (double)spawnPos.getZ())) + " blocks)");
            stale.setDead();
         }

         int expectedTotal = 0;

         for(int c : step.spawnCounts) {
            expectedTotal += c;
         }

         if (!existingNpcs.isEmpty() && existingNpcs.size() >= expectedTotal) {
            System.out.println(debugPrefix + "PATH=WORLD_SCAN_ADOPT — found " + existingNpcs.size() + "/" + expectedTotal + " matching live NPCs near spawn");

            for(Entity e : existingNpcs) {
               quest.trackEntity(e.getUniqueID());
               ((Set)this.entityToPlayers.computeIfAbsent(e.getUniqueID(), (k) -> new HashSet())).add(player.getUniqueID());
            }

            String encKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
            SharedEncounter existingEnc = (SharedEncounter)this.sharedEncounters.get(encKey);
            if (existingEnc != null) {
               existingEnc.addParticipant(player.getUniqueID());
            } else {
               double totalHp = (double)0.0F;

               for(Entity e : existingNpcs) {
                  if (e instanceof EntityLivingBase) {
                     totalHp += (double)((EntityLivingBase)e).getMaxHealth();
                  }
               }

               SharedEncounter newEnc = new SharedEncounter(encKey, quest.getQuestId(), quest.getCurrentStepIndex(), quest.getSpawnedEntities(), totalHp, spawnPos, player.world.getTotalWorldTime());
               newEnc.addParticipant(player.getUniqueID());
               this.sharedEncounters.put(encKey, newEnc);
               System.out.println(debugPrefix + "Created new SharedEncounter from world-scan adopt");
            }

            this.waypointManager.clearWaypoint(player, quest.getQuestId());
            QuestSavedData.get(player.world).saveQuest(player.getUniqueID(), quest);
            player.sendMessage(new TextComponentString("§aYou've joined an ongoing battle!"));
            QuestNetworkHelper.sendQuestSync(player);
            return;
         }

         if (!existingNpcs.isEmpty()) {
            System.out.println(debugPrefix + "Partial match: " + existingNpcs.size() + "/" + expectedTotal + " — killing remnants");

            for(Entity e : existingNpcs) {
               e.setDead();
            }
         }
      }

      if (!quest.isGenerated() && step.spawnEnemies && step.npcConfigIds == null && step.spawnEntityIds != null) {
         BlockPos spawnPos = this.getStepPos(player.world, quest);
         if (spawnPos == null) {
            return;
         }

         List<Entity> existingNpcs = new ArrayList();

         for(Entity e : new ArrayList(player.world.loadedEntityList)) {
            if (e != null && !e.isDead) {
               NBTTagCompound data = e.getEntityData();
               if (data.getBoolean("questEntity")) {
                  ResourceLocation regName = EntityList.getKey(e);
                  if (regName != null) {
                     String regStr = regName.toString();

                     for(String stepEntityId : step.spawnEntityIds) {
                        if (stepEntityId.equals(regStr)) {
                           double dx = e.posX - (double)spawnPos.getX();
                           double dz = e.posZ - (double)spawnPos.getZ();
                           if (dx * dx + dz * dz <= (double)10000.0F) {
                              existingNpcs.add(e);
                           }
                           break;
                        }
                     }
                  }
               }
            }
         }

         if (!existingNpcs.isEmpty()) {
            System.out.println(debugPrefix + "PATH=OLD_STYLE_WORLD_SCAN_ADOPT — found " + existingNpcs.size() + " matching old-style NPCs near spawn");

            for(Entity e : existingNpcs) {
               quest.trackEntity(e.getUniqueID());
               ((Set)this.entityToPlayers.computeIfAbsent(e.getUniqueID(), (k) -> new HashSet())).add(player.getUniqueID());
            }

            String encKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
            SharedEncounter existingEnc = (SharedEncounter)this.sharedEncounters.get(encKey);
            if (existingEnc != null) {
               existingEnc.addParticipant(player.getUniqueID());
            } else {
               double totalHp = (double)0.0F;

               for(Entity e : existingNpcs) {
                  if (e instanceof EntityLivingBase) {
                     totalHp += (double)((EntityLivingBase)e).getMaxHealth();
                  }
               }

               SharedEncounter newEnc = new SharedEncounter(encKey, quest.getQuestId(), quest.getCurrentStepIndex(), quest.getSpawnedEntities(), totalHp, spawnPos, player.world.getTotalWorldTime());
               newEnc.addParticipant(player.getUniqueID());
               this.sharedEncounters.put(encKey, newEnc);
            }

            this.waypointManager.clearWaypoint(player, quest.getQuestId());
            QuestSavedData.get(player.world).saveQuest(player.getUniqueID(), quest);
            player.sendMessage(new TextComponentString("§aYou've joined an ongoing battle!"));
            QuestNetworkHelper.sendQuestSync(player);
            return;
         }
      }

      if (step.isHealthRetreat()) {
         quest.setRetreatPhase(0);
         quest.setRetreatTimer(0);
         player.sendMessage(new TextComponentString("§c§lA powerful enemy has appeared!"));
      } else if (step.isScriptedRetreat) {
         quest.setCombatTimer(1200);
         player.sendMessage(new TextComponentString("§c§lDangerous enemies have appeared! Survive for 60 seconds!"));
      } else if (step.isKillCount()) {
         player.sendMessage(new TextComponentString("§eDefeat " + step.killsRequired + " enemies to proceed."));
      } else if (step.isDropChance()) {
         player.sendMessage(new TextComponentString("§eDefeat enemies to obtain: §6" + step.dropItemName));
      } else if (step.isKillAllSpawned()) {
         player.sendMessage(new TextComponentString("§c§lEnemies have appeared! Defeat them all!"));
      }

      if (step.spawnEnemies) {
         System.out.println(debugPrefix + "PATH=FRESH_SPAWN — spawning new enemies" + (step.npcConfigIds != null ? " (npcConfig)" : " (old-style)"));
         BlockPos pos = this.getStepPos(player.world, quest);
         if (pos == null) {
            System.out.println(debugPrefix + "ABORT — no valid spawn position");
            return;
         }

         String encKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
         List<Entity> spawned;
         if (step.npcConfigIds != null) {
            spawned = WaypointSpawnLogic.spawnQuestNpcs(player.world, player, step, pos, quest.getQuestId(), quest.isGenerated());
         } else {
            spawned = WaypointSpawnLogic.spawnQuestEnemies(player.world, player, step, pos, quest.isGenerated());
         }

         for(Entity entity : spawned) {
            quest.trackEntity(entity.getUniqueID());
            ((Set)this.entityToPlayers.computeIfAbsent(entity.getUniqueID(), (k) -> new HashSet())).add(player.getUniqueID());
         }

         double totalHp = (double)0.0F;

         for(Entity entity : spawned) {
            if (entity instanceof EntityLivingBase) {
               totalHp += (double)((EntityLivingBase)entity).getMaxHealth();
            }
         }

         SharedEncounter newEnc = new SharedEncounter(encKey, quest.getQuestId(), quest.getCurrentStepIndex(), quest.getSpawnedEntities(), totalHp, pos, player.world.getTotalWorldTime());
         newEnc.addParticipant(player.getUniqueID());
         this.sharedEncounters.put(encKey, newEnc);
         System.out.println(debugPrefix + "Created SharedEncounter " + encKey + " with " + spawned.size() + " entities");
         QuestSavedData.get(player.world).saveQuest(player.getUniqueID(), quest);
      }

      if (step.sceneGroup != null && quest.getSceneEntities().isEmpty()) {
         this.spawnSceneNpcs(player, quest, step);
      }

      this.waypointManager.clearWaypoint(player, quest.getQuestId());
      QuestNetworkHelper.sendQuestSync(player);
   }

   public void onQuestEntityDeath(Entity deadEntity, EntityPlayerMP killer) {
      UUID deadUUID = deadEntity.getUniqueID();
      Set<UUID> candidatePlayerIds = new LinkedHashSet();
      String ownerStr = deadEntity.getEntityData().getString("ownerUUID");
      if (ownerStr != null && !ownerStr.isEmpty()) {
         try {
            UUID ownerUUID = UUID.fromString(ownerStr);
            candidatePlayerIds.add(ownerUUID);
         } catch (IllegalArgumentException var10) {
         }
      }

      if (killer != null) {
         candidatePlayerIds.add(killer.getUniqueID());
      }

      Set<UUID> cachedOwners = (Set)this.entityToPlayers.get(deadUUID);
      if (cachedOwners != null) {
         candidatePlayerIds.addAll(cachedOwners);
      }

      for(SharedEncounter enc : this.sharedEncounters.values()) {
         if (enc.getEntityUUIDs().contains(deadUUID)) {
            candidatePlayerIds.addAll(enc.getAllParticipants());
            break;
         }
      }

      this.entityToPlayers.remove(deadUUID);

      for(UUID playerId : candidatePlayerIds) {
         Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
         if (playerSlots != null) {
            this.processQuestEntityDeathForPlayer(deadEntity, deadUUID, playerId, playerSlots);
         }
      }

   }

   private void processQuestEntityDeathForPlayer(Entity deadEntity, UUID deadUUID, UUID playerId, Map<String, QuestInstance> playerSlots) {
      for(QuestInstance quest : new ArrayList(playerSlots.values())) {
         QuestStep step = quest.getCurrentStep();
         if (step != null && step.type == QuestStep.StepType.COMBAT) {
            if (step.isHealthRetreat() && quest.getSpawnedEntities().contains(deadUUID)) {
               if (quest.getRetreatPhase() < 1) {
                  quest.removeSpawnedEntity(deadUUID);
                  if (quest.getSpawnedEntities().isEmpty()) {
                     quest.setRetreatPhase(3);
                     String encKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
                     if (this.sharedEncounters.get(encKey) != null) {
                        this.advanceSharedEncounterParticipants(deadEntity.world, quest.getQuestId(), quest.getCurrentStepIndex(), playerId);
                     } else {
                        EntityPlayerMP player = this.getPlayerByUUID(deadEntity.world, playerId);
                        if (player != null) {
                           this.sendCombatHealthClear(player);
                           this.advanceQuest(player, quest);
                           player.sendMessage(new TextComponentString("§aQuest objective completed!"));
                        }
                     }
                  }
               }
               break;
            }

            if (step.isKillAllSpawned() && quest.getSpawnedEntities().contains(deadUUID)) {
               quest.removeSpawnedEntity(deadUUID);
               if (quest.getSpawnedEntities().isEmpty()) {
                  String encKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
                  if (this.sharedEncounters.get(encKey) != null) {
                     this.advanceSharedEncounterParticipants(deadEntity.world, quest.getQuestId(), quest.getCurrentStepIndex(), playerId);
                  } else {
                     EntityPlayerMP player = this.getPlayerByUUID(deadEntity.world, playerId);
                     if (player != null) {
                        this.sendCombatHealthClear(player);
                        this.advanceQuest(player, quest);
                        player.sendMessage(new TextComponentString("§aQuest objective completed!"));
                     }
                  }
               }
               break;
            }
         }
      }

   }

   public void onWorldEntityDeath(Entity deadEntity, EntityPlayerMP killer) {
      if (killer != null) {
         UUID playerId = killer.getUniqueID();
         Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
         if (playerSlots != null) {
            String entityType = EntityList.getKey(deadEntity) != null ? EntityList.getKey(deadEntity).toString() : "";

            for(QuestInstance quest : new ArrayList(playerSlots.values())) {
               QuestStep step = quest.getCurrentStep();
               if (step != null && step.type == QuestStep.StepType.COMBAT && step.targetEntityId != null && entityType.equals(step.targetEntityId)) {
                  if (step.isKillCount()) {
                     int progress = quest.incrementKills();
                     killer.sendMessage(new TextComponentString("§eDefeated " + progress + "/" + step.killsRequired));
                     if (progress >= step.killsRequired) {
                        killer.sendMessage(new TextComponentString("§aKill objective complete!"));
                        this.advanceQuest(killer, quest);
                     } else {
                        QuestNetworkHelper.sendQuestSync(killer);
                     }
                  } else if (step.isDropChance()) {
                     if (Math.random() < step.dropChance) {
                        quest.setDropObtained();
                        killer.sendMessage(new TextComponentString("§6§lYou obtained: " + step.dropItemName + "!"));
                        this.advanceQuest(killer, quest);
                     } else {
                        killer.sendMessage(new TextComponentString("§7No drop this time..."));
                     }
                  }
               }
            }

         }
      }
   }

   public void onDialogResponse(EntityPlayerMP player, int responseIndex) {
      UUID playerId = player.getUniqueID();
      Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
      if (playerSlots != null) {
         QuestInstance quest = null;
         QuestStep step = null;

         for(QuestInstance candidate : playerSlots.values()) {
            QuestStep candidateStep = candidate.getCurrentStep();
            if (candidateStep != null && candidateStep.type == QuestStep.StepType.DIALOG) {
               quest = candidate;
               step = candidateStep;
               break;
            }
         }

         if (quest != null && step != null) {
            if (step.hasMultiPhaseDialog()) {
               QuestStep.DialogPhase[] phases = step.dialogPhases;
               int phaseIdx = quest.getDialogPhaseIndex();
               if (phaseIdx >= 0 && phaseIdx < phases.length) {
                  QuestStep.DialogPhase phase = phases[phaseIdx];
                  if (phase.responses != null && responseIndex >= 0 && responseIndex < phase.responses.length) {
                     player.sendMessage(new TextComponentString("§7You: " + phase.responses[responseIndex]));
                     if (phase.isQuiz) {
                        if (responseIndex == phase.correctResponseIndex) {
                           quest.incrementQuizCorrectCount();
                        }

                        this.advanceDialogPhase(player, quest, step);
                     } else if (phase.resultVariants != null) {
                        this.advanceDialogPhase(player, quest, step);
                     } else if (responseIndex == phase.correctResponseIndex) {
                        this.advanceDialogPhase(player, quest, step);
                     } else if (phase.moreInfoLines != null) {
                        String npcName = step.npcName != null ? step.npcName : "NPC";

                        for(String line : phase.moreInfoLines) {
                           player.sendMessage(new TextComponentString("§e" + npcName + ": §f" + line));
                        }

                        QuestNetworkHelper.sendMultiPhaseDialog(player, step, quest);
                     }

                  }
               }
            } else if (step.dialogResponses != null && responseIndex >= 0 && responseIndex < step.dialogResponses.length) {
               player.sendMessage(new TextComponentString("§7You: " + step.dialogResponses[responseIndex]));
               if (responseIndex == step.correctResponseIndex) {
                  if (step.sceneGroup != null && step.hasNpcConfig()) {
                     boolean sceneContinues = false;
                     if (!quest.isOnFinalStep()) {
                        QuestDefinition def = QuestRegistry.getById(quest.getQuestId());
                        if (def != null) {
                           QuestStep nextStep = def.getStep(quest.getCurrentStepIndex() + 1);
                           sceneContinues = nextStep != null && step.sceneGroup.equals(nextStep.sceneGroup);
                        }
                     }

                     if (sceneContinues) {
                        QuestDefinition defCheck = QuestRegistry.getById(quest.getQuestId());
                        QuestStep nextStepCheck = defCheck != null ? defCheck.getStep(quest.getCurrentStepIndex() + 1) : null;
                        if (nextStepCheck != null && nextStepCheck.type == QuestStep.StepType.COMBAT) {
                           WaypointSpawnLogic.cleanupQuestEntities(player.world, quest.getSpawnedEntities());
                           quest.getSceneEntities().remove(step.npcConfigId);
                           BlockPos npcPos = QuestSavedData.get(player.world).getEffectivePosition(quest.getQuestId(), quest.getCurrentStepIndex());
                           this.clearSharedDialogNpc(step.npcConfigId, npcPos);
                        }

                        this.cleanEntityCacheForPlayer(quest, player.getUniqueID());
                        quest.getSpawnedEntities().clear();
                     } else {
                        this.cleanupSceneEntities(player, quest, step.sceneGroup);
                        this.cleanEntityCacheForPlayer(quest, player.getUniqueID());
                        quest.getSpawnedEntities().clear();
                     }
                  } else if (step.hasNpcConfig()) {
                     boolean nextStepReusesSameNpc = false;
                     if (!quest.isOnFinalStep()) {
                        QuestDefinition def = QuestRegistry.getById(quest.getQuestId());
                        if (def != null) {
                           QuestStep nextStep = def.getStep(quest.getCurrentStepIndex() + 1);
                           if (nextStep != null && nextStep.type == QuestStep.StepType.DIALOG && nextStep.hasNpcConfig() && step.npcConfigId.equals(nextStep.npcConfigId)) {
                              nextStepReusesSameNpc = true;
                           }
                        }
                     }

                     if (nextStepReusesSameNpc) {
                        this.cleanEntityCacheForPlayer(quest, player.getUniqueID());
                        quest.getSpawnedEntities().clear();
                     } else if (this.otherPlayersNeedDialogNpc(step.npcConfigId, player.getUniqueID())) {
                        this.cleanEntityCacheForPlayer(quest, player.getUniqueID());
                        quest.getSpawnedEntities().clear();
                     } else {
                        this.cleanEntityCacheFull(quest);
                        WaypointSpawnLogic.cleanupQuestEntities(player.world, quest.getSpawnedEntities());
                        quest.getSpawnedEntities().clear();
                        BlockPos npcPos = this.getStepPos(player.world, quest);
                        if (npcPos != null) {
                           this.clearSharedDialogNpc(step.npcConfigId, npcPos);
                        }

                        this.killNpcsByConfigNearPos(player.world, step.npcConfigId, npcPos, (double)15.0F);
                     }
                  }

                  this.advanceQuest(player, quest);
               } else {
                  if (step.correctResponseIndex > 0) {
                     String npcName = step.npcName != null ? step.npcName : "NPC";
                     player.sendMessage(new TextComponentString("§e" + npcName + ": §7That's not right. Think carefully."));
                  }

                  if (step.moreInfoLines != null && step.moreInfoLines.length > 0 && step.correctResponseIndex == 0) {
                     QuestNetworkHelper.sendDialogWithLines(player, step, step.moreInfoLines, quest.getCurrentStepIndex());
                  } else {
                     QuestNetworkHelper.sendDialog(player, step, quest.getCurrentStepIndex());
                  }
               }

            }
         }
      }
   }

   private void advanceDialogPhase(EntityPlayerMP player, QuestInstance quest, QuestStep step) {
      int nextPhase = quest.getDialogPhaseIndex() + 1;
      QuestStep.DialogPhase[] phases = step.dialogPhases;
      if (nextPhase < phases.length) {
         quest.setDialogPhaseIndex(nextPhase);
         QuestSavedData.get(player.world).saveQuest(player.getUniqueID(), quest);
         player.getServer().addScheduledTask(() -> QuestNetworkHelper.sendMultiPhaseDialog(player, step, quest));
      } else {
         quest.resetQuizState();
         if (step.hasNpcConfig()) {
            if (this.otherPlayersNeedDialogNpc(step.npcConfigId, player.getUniqueID())) {
               this.cleanEntityCacheForPlayer(quest, player.getUniqueID());
               quest.getSpawnedEntities().clear();
            } else {
               this.cleanEntityCacheFull(quest);
               WaypointSpawnLogic.cleanupQuestEntities(player.world, quest.getSpawnedEntities());
               quest.getSpawnedEntities().clear();
               BlockPos npcPos2 = this.getStepPos(player.world, quest);
               if (npcPos2 != null) {
                  this.clearSharedDialogNpc(step.npcConfigId, npcPos2);
                  this.killNpcsByConfigNearPos(player.world, step.npcConfigId, npcPos2, (double)15.0F);
               }
            }
         }

         this.advanceQuest(player, quest);
      }

   }

   public void onPlayerDeath(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
      if (playerSlots != null && !playerSlots.isEmpty()) {
         boolean hadCombat = false;
         boolean hadNpcDialog = false;
         boolean hadRevertToDialog = false;

         for(QuestInstance quest : playerSlots.values()) {
            QuestStep step = quest.getCurrentStep();
            if (step != null) {
               if (step.type == QuestStep.StepType.COMBAT) {
                  String encKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
                  this.hasOtherLivingParticipants(player.world, encKey, playerId);
                  String deathEncKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
                  boolean othersStillAlive = this.hasOtherLivingParticipants(player.world, deathEncKey, playerId);
                  if (!othersStillAlive) {
                     WaypointSpawnLogic.cleanupQuestEntities(player.world, quest.getSpawnedEntities());
                     SharedEncounter enc = (SharedEncounter)this.sharedEncounters.get(deathEncKey);
                     if (enc != null) {
                        WaypointSpawnLogic.cleanupQuestEntities(player.world, enc.getEntityUUIDs());
                        this.sharedEncounters.remove(deathEncKey);
                     }
                  }

                  this.cleanEntityCacheForPlayer(quest, playerId);
                  quest.getSpawnedEntities().clear();
                  quest.setCombatTimer(0);
                  quest.resetKillProgress();
                  if (quest.getJiraiyaRescueUUID() != null) {
                     Set<UUID> jiraiyaSet = new HashSet();
                     jiraiyaSet.add(quest.getJiraiyaRescueUUID());
                     WaypointSpawnLogic.cleanupQuestEntities(player.world, jiraiyaSet);
                     quest.setJiraiyaRescueUUID((UUID)null);
                  }

                  quest.setRetreatPhase(0);
                  quest.setRetreatTimer(0);
                  int curIdx = quest.getCurrentStepIndex();
                  boolean revertedToDialog = false;
                  if (curIdx > 0 && !quest.isGenerated()) {
                     QuestDefinition def = QuestRegistry.getById(quest.getQuestId());
                     if (def != null) {
                        QuestStep prevStep = def.getStep(curIdx - 1);
                        if (prevStep != null && prevStep.type == QuestStep.StepType.DIALOG && prevStep.onCompleteCommand != null && prevStep.onCompleteCommand.contains("tp ")) {
                           quest.revertToStep(curIdx - 1);
                           revertedToDialog = true;
                        }
                     }
                  }

                  this.setupCurrentStep(player, quest);
                  QuestSavedData.get(player.world).saveQuest(playerId, quest);
                  hadCombat = true;
                  if (revertedToDialog) {
                     hadRevertToDialog = true;
                  }
               } else if (step.type == QuestStep.StepType.DIALOG && step.hasNpcConfig()) {
                  if (step.sceneGroup != null) {
                     this.cleanEntityCacheForPlayer(quest, playerId);
                     quest.getSpawnedEntities().clear();
                  } else if (this.otherPlayersNeedDialogNpc(step.npcConfigId, playerId)) {
                     this.cleanEntityCacheForPlayer(quest, playerId);
                     quest.getSpawnedEntities().clear();
                  } else {
                     this.cleanEntityCacheFull(quest);
                     WaypointSpawnLogic.cleanupQuestEntities(player.world, quest.getSpawnedEntities());
                     quest.getSpawnedEntities().clear();
                     BlockPos npcPos3 = this.getStepPos(player.world, quest);
                     if (npcPos3 != null) {
                        this.clearSharedDialogNpc(step.npcConfigId, npcPos3);
                     }
                  }

                  this.setupCurrentStep(player, quest);
                  QuestSavedData.get(player.world).saveQuest(playerId, quest);
                  hadNpcDialog = true;
               }
            }
         }

         if (hadCombat) {
            this.sendCombatHealthClear(player);
            if (hadRevertToDialog) {
               player.sendMessage(new TextComponentString("§cYou died during combat. Speak to the NPC again to re-enter the arena."));
            } else {
               player.sendMessage(new TextComponentString("§cYou died during combat. Return to the waypoint to retry."));
            }
         }

         if (hadNpcDialog) {
            player.sendMessage(new TextComponentString("§cReturn to the waypoint to speak with the NPC again."));
         }

         if (hadCombat || hadNpcDialog) {
            QuestNetworkHelper.sendQuestSync(player);
         }

      }
   }

   public void onPlayerDisconnect(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
      if (playerSlots != null && !playerSlots.isEmpty()) {
         boolean hasEntitiesNeedingCleanup = false;

         for(QuestInstance quest : playerSlots.values()) {
            if (!quest.getSceneEntities().isEmpty()) {
               QuestStep step = quest.getCurrentStep();
               String sceneGroup = step != null ? step.sceneGroup : null;
               if (sceneGroup != null) {
                  this.cleanupSceneEntities(player, quest, sceneGroup);
               } else {
                  for(UUID entityUUID : quest.getSceneEntities().values()) {
                     Entity sceneEntity = this.findEntityByUUID(player.world, entityUUID);
                     if (sceneEntity != null) {
                        sceneEntity.setDead();
                     }

                     Set<UUID> players = (Set)this.entityToPlayers.get(entityUUID);
                     if (players != null) {
                        players.remove(playerId);
                        if (players.isEmpty()) {
                           this.entityToPlayers.remove(entityUUID);
                        }
                     }
                  }

                  quest.clearSceneEntities();
               }
            }

            if (!quest.getSpawnedEntities().isEmpty()) {
               QuestStep step = quest.getCurrentStep();
               if (step != null && step.type == QuestStep.StepType.COMBAT) {
                  String encKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
                  if (this.hasOtherLivingParticipants(player.world, encKey, playerId)) {
                     this.cleanEntityCacheForPlayer(quest, playerId);
                     quest.getSpawnedEntities().clear();
                     SharedEncounter enc = (SharedEncounter)this.sharedEncounters.get(encKey);
                     if (enc != null) {
                        enc.removeParticipant(playerId);
                     }
                     continue;
                  }
               } else if (step != null && step.type == QuestStep.StepType.DIALOG && step.hasNpcConfig() && this.otherPlayersNeedDialogNpc(step.npcConfigId, playerId)) {
                  this.cleanEntityCacheForPlayer(quest, playerId);
                  quest.getSpawnedEntities().clear();
                  continue;
               }

               hasEntitiesNeedingCleanup = true;
            }
         }

         if (hasEntitiesNeedingCleanup) {
            this.cleanupTimers.put(playerId, 1200);
         }

         this.waypointManager.clearAllWaypoints(playerId);
         this.currentOffers.remove(playerId);
         this.lastSentHealth.remove(playerId);
      }
   }

   public void onPlayerLogin(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      this.cleanupTimers.remove(playerId);
      QuestSavedData savedData = QuestSavedData.get(player.world);
      Set<String> completed = savedData.getCompletedQuests(playerId);
      if (!completed.isEmpty()) {
         this.completedQuests.put(playerId, new HashSet(completed));
      }

      this.handleAdvancementSkips(player);
      List<QuestInstance> savedQuests = savedData.getActiveQuests(playerId);
      Iterator var6 = savedQuests.iterator();

      while(true) {
         QuestInstance saved;
         String slot;
         String questName;
         while(true) {
            if (!var6.hasNext()) {
               Map<String, RepeatableQuestGenerator.QuestOffer> persistedOffers = savedData.getSavedOffers(playerId);
               Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);

               for(String subSlot : ALL_REPEATABLE_SUB_SLOTS) {
                  if ((playerSlots == null || !playerSlots.containsKey(subSlot)) && !savedData.isSlotOnCooldown(playerId, subSlot)) {
                     RepeatableQuestGenerator.QuestOffer saved = (RepeatableQuestGenerator.QuestOffer)persistedOffers.get(subSlot);
                     if (saved != null && !QuestSavedData.isOfferStale(subSlot, saved.createdAt)) {
                        ((Map)this.currentOffers.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap())).put(subSlot, saved);
                     } else {
                        this.generateOffer(player, subSlot);
                     }
                  }
               }

               if (savedData.hasFlag(playerId, "kg_owed")) {
                  savedData.removeFlag(playerId, "kg_owed");
                  if (!savedData.hasFlag(playerId, "kg_received")) {
                     player.getServer().addScheduledTask(() -> {
                        player.sendMessage(new TextComponentString("§d§lYou have a pending Kekkei Genkai roll from the Shinobi Way questline!"));
                        this.triggerKGRoll(player);
                     });
                  }
               }

               QuestNetworkHelper.sendQuestSync(player);
               return;
            }

            saved = (QuestInstance)var6.next();
            if (saved.getState() == QuestInstance.QuestState.ACTIVE) {
               if (saved.isGenerated()) {
                  slot = getSlotForInstance(saved);
                  questName = saved.getGeneratedName() != null ? saved.getGeneratedName() : saved.getQuestId();
                  break;
               }

               QuestDefinition def = QuestRegistry.getById(saved.getQuestId());
               if (def != null) {
                  slot = getSlotForQuest(def);
                  questName = def.getName();
                  break;
               }
            }
         }

         ((Map)this.activeQuests.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap())).put(slot, saved);
         QuestStep loginStep = saved.getCurrentStep();
         if (loginStep != null && loginStep.type == QuestStep.StepType.COMBAT && !saved.getSpawnedEntities().isEmpty()) {
            boolean anyAlive = false;

            for(UUID entityUUID : saved.getSpawnedEntities()) {
               Entity entity = this.findEntityByUUID(player.world, entityUUID);
               if (entity != null && entity.isEntityAlive()) {
                  anyAlive = true;
                  break;
               }
            }

            if (anyAlive) {
               player.sendMessage(new TextComponentString("§eResuming quest: §6" + questName + " §c(Combat in progress)"));
               continue;
            }

            this.cleanEntityCacheForPlayer(saved, playerId);
            saved.getSpawnedEntities().clear();
            QuestSavedData.get(player.world).saveQuest(playerId, saved);
            player.sendMessage(new TextComponentString("§eResuming quest: §6" + questName + " §7(Restarting combat step)"));
         }

         if (!saved.getSceneEntities().isEmpty()) {
            boolean anySceneAlive = false;

            for(UUID sceneUUID : saved.getSceneEntities().values()) {
               Entity entity = this.findEntityByUUID(player.world, sceneUUID);
               if (entity != null && entity.isEntityAlive()) {
                  anySceneAlive = true;
                  break;
               }
            }

            if (!anySceneAlive) {
               saved.clearSceneEntities();
               QuestSavedData.get(player.world).saveQuest(playerId, saved);
            }
         }

         this.setupCurrentStep(player, saved, true);
         player.sendMessage(new TextComponentString("§eResuming quest: §6" + questName));
      }
   }

   public void onServerTick(World world) {
      if (!this.activeQuests.isEmpty() || !this.cleanupTimers.isEmpty()) {
         ++this.tickCounter;
         if (!this.cleanupTimers.isEmpty()) {
            List<UUID> expired = new ArrayList();

            for(Map.Entry<UUID, Integer> entry : this.cleanupTimers.entrySet()) {
               int remaining = (Integer)entry.getValue() - 1;
               if (remaining <= 0) {
                  expired.add(entry.getKey());
               } else {
                  entry.setValue(remaining);
               }
            }

            for(UUID playerId : expired) {
               this.cleanupTimers.remove(playerId);
               Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
               if (playerSlots != null) {
                  for(QuestInstance quest : playerSlots.values()) {
                     this.cleanEntityCacheFull(quest);
                     WaypointSpawnLogic.cleanupQuestEntities(world, quest.getSpawnedEntities());
                     quest.getSpawnedEntities().clear();
                     String encKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
                     SharedEncounter enc = (SharedEncounter)this.sharedEncounters.get(encKey);
                     if (enc != null) {
                        enc.removeParticipant(playerId);
                        if (enc.getAllParticipants().isEmpty()) {
                           this.sharedEncounters.remove(encKey);
                        }
                     }

                     if (!quest.getSceneEntities().isEmpty()) {
                        QuestStep step = quest.getCurrentStep();
                        String sceneGroup = step != null ? step.sceneGroup : null;
                        if (sceneGroup != null) {
                           QuestDefinition def = QuestRegistry.getById(quest.getQuestId());
                           if (def != null) {
                              for(QuestDefinition.SceneNpc sceneNpc : def.getSceneNpcsForGroup(sceneGroup)) {
                                 BlockPos pos = WaypointManager.validatePosition(world, sceneNpc.position);
                                 String sharingKey = sceneNpc.npcConfigId + "::" + pos.getX() + "::" + pos.getY() + "::" + pos.getZ();
                                 this.sharedDialogNpcs.remove(sharingKey);
                              }
                           }
                        }

                        for(UUID entityUUID : quest.getSceneEntities().values()) {
                           Entity sceneEntity = this.findEntityByUUID(world, entityUUID);
                           if (sceneEntity != null) {
                              sceneEntity.setDead();
                           }

                           this.entityToPlayers.remove(entityUUID);
                        }

                        quest.clearSceneEntities();
                     }
                  }
               }
            }
         }

         if (this.tickCounter % 1200 == 0 && !this.sharedEncounters.isEmpty()) {
            long now = world.getTotalWorldTime();
            this.sharedEncounters.entrySet().removeIf((e) -> now - ((SharedEncounter)e.getValue()).getCreatedTick() > 12000L || !((SharedEncounter)e.getValue()).hasLivingEntities(world));
         }

         if (this.tickCounter % 6000 == 0 && !this.currentOffers.isEmpty()) {
            this.currentOffers.entrySet().removeIf((e) -> !this.activeQuests.containsKey(e.getKey()) && this.getPlayerByUUID(world, (UUID)e.getKey()) == null);
         }

         if (this.tickCounter % 1200 == 0 && !this.entityToPlayers.isEmpty() && world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer)world;
            this.entityToPlayers.entrySet().removeIf((e) -> {
               Entity entity = worldServer.getEntityFromUuid((UUID)e.getKey());
               return entity == null || entity.isDead;
            });
         }

         if (!this.activeQuests.isEmpty()) {
            if (this.tickCounter % 10 == 0) {
               for(UUID playerId : new ArrayList(this.activeQuests.keySet())) {
                  EntityPlayerMP player = this.getPlayerByUUID(world, playerId);
                  if (player != null) {
                     this.waypointManager.checkProximity(player, world);
                  }
               }
            }

            List<Map.Entry<UUID, String>> retreatComplete = null;

            for(Map.Entry<UUID, Map<String, QuestInstance>> playerEntry : new ArrayList(this.activeQuests.entrySet())) {
               for(Map.Entry<String, QuestInstance> slotEntry : new ArrayList(((Map)playerEntry.getValue()).entrySet())) {
                  QuestInstance quest = (QuestInstance)slotEntry.getValue();
                  if (quest.getCombatTimer() > 0) {
                     quest.decrementCombatTimer();
                     if (quest.getCombatTimer() <= 0) {
                        if (retreatComplete == null) {
                           retreatComplete = new ArrayList();
                        }

                        retreatComplete.add(new AbstractMap.SimpleEntry(playerEntry.getKey(), slotEntry.getKey()));
                     }
                  }
               }
            }

            if (retreatComplete != null) {
               for(Map.Entry<UUID, String> entry : retreatComplete) {
                  UUID playerId = (UUID)entry.getKey();
                  String slot = (String)entry.getValue();
                  Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
                  if (playerSlots != null) {
                     QuestInstance quest = (QuestInstance)playerSlots.get(slot);
                     if (quest != null) {
                        EntityPlayerMP player = this.getPlayerByUUID(world, playerId);
                        if (player != null) {
                           this.cleanEntityCacheFull(quest);
                           WaypointSpawnLogic.cleanupQuestEntities(world, quest.getSpawnedEntities());
                           quest.getSpawnedEntities().clear();
                           player.sendMessage(new TextComponentString("§aThe enemies have retreated!"));
                           String encKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
                           if (this.sharedEncounters.get(encKey) != null) {
                              this.advanceSharedEncounterParticipants(world, quest.getQuestId(), quest.getCurrentStepIndex(), playerId);
                           } else {
                              this.sendCombatHealthClear(player);
                              this.advanceQuest(player, quest);
                           }
                        }
                     }
                  }
               }
            }

            if (this.tickCounter % 40 == 0) {
               this.tickDialogNpcLivenessCheck(world);
            }

            if (this.tickCounter % 40 == 0) {
               this.tickCombatEntityLivenessCheck(world);
            }

            if (this.tickCounter % 10 == 0) {
               this.tickHealthRetreatQuests(world);
            }

            this.tickAreaStayQuests(world);
            if (this.tickCounter % 20 == 0) {
               this.sendCombatHealthUpdates(world);
            }

         }
      }
   }

   private void tickAreaStayQuests(World world) {
      List<Map.Entry<UUID, String>> completed = null;

      for(Map.Entry<UUID, Map<String, QuestInstance>> playerEntry : new ArrayList(this.activeQuests.entrySet())) {
         UUID playerId = (UUID)playerEntry.getKey();
         EntityPlayerMP player = this.getPlayerByUUID(world, playerId);
         if (player != null) {
            for(Map.Entry<String, QuestInstance> slotEntry : new ArrayList(((Map)playerEntry.getValue()).entrySet())) {
               QuestInstance quest = (QuestInstance)slotEntry.getValue();
               QuestStep step = quest.getCurrentStep();
               if (step != null && step.type == QuestStep.StepType.AREA_STAY) {
                  BlockPos stepPos = this.getStepPos(world, quest);
                  if (stepPos == null || stepPos.equals(BlockPos.ORIGIN)) {
                     stepPos = step.defaultPosition;
                  }

                  if (stepPos != null) {
                     double dx = player.posX - (double)stepPos.getX() - (double)0.5F;
                     double dz = player.posZ - (double)stepPos.getZ() - (double)0.5F;
                     double horizDist = Math.sqrt(dx * dx + dz * dz);
                     double yDist = Math.abs(player.posY - (double)stepPos.getY());
                     if (horizDist <= step.stayRadius && yDist <= (double)10.0F) {
                        int progress = (Integer)this.areaStayProgress.getOrDefault(playerId, 0) + 1;
                        this.areaStayProgress.put(playerId, progress);
                        if (progress % 20 == 0) {
                           int secondsDone = progress / 20;
                           int secondsTotal = step.stayDurationTicks / 20;
                           player.sendMessage(new TextComponentString("§b✦ " + step.description + "... §f" + secondsDone + "/" + secondsTotal + "s"));
                        }

                        if (progress >= step.stayDurationTicks) {
                           this.areaStayProgress.remove(playerId);
                           if (completed == null) {
                              completed = new ArrayList();
                           }

                           completed.add(new AbstractMap.SimpleEntry(playerId, slotEntry.getKey()));
                        }
                     } else if (this.areaStayProgress.containsKey(playerId)) {
                        this.areaStayProgress.remove(playerId);
                        player.sendMessage(new TextComponentString("§c✖ Left the area. Stay within " + (int)step.stayRadius + " blocks to continue."));
                     }
                  }
               }
            }
         }
      }

      if (completed != null) {
         for(Map.Entry<UUID, String> entry : completed) {
            UUID playerId = (UUID)entry.getKey();
            String slot = (String)entry.getValue();
            Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
            if (playerSlots != null) {
               QuestInstance quest = (QuestInstance)playerSlots.get(slot);
               if (quest != null) {
                  EntityPlayerMP player = this.getPlayerByUUID(world, playerId);
                  if (player != null) {
                     player.sendMessage(new TextComponentString("§a✔ " + quest.getCurrentStep().description + " complete!"));
                     this.advanceQuest(player, quest);
                  }
               }
            }
         }
      }

   }

   private void tickDialogNpcLivenessCheck(World world) {
      for(Map.Entry<UUID, Map<String, QuestInstance>> playerEntry : new ArrayList(this.activeQuests.entrySet())) {
         UUID playerId = (UUID)playerEntry.getKey();

         for(Map.Entry<String, QuestInstance> slotEntry : new ArrayList(((Map)playerEntry.getValue()).entrySet())) {
            QuestInstance quest = (QuestInstance)slotEntry.getValue();
            QuestStep step = quest.getCurrentStep();
            if (step != null && step.type == QuestStep.StepType.DIALOG && step.hasNpcConfig() && !quest.getSpawnedEntities().isEmpty()) {
               boolean allDead = true;

               for(UUID entityUUID : quest.getSpawnedEntities()) {
                  Entity entity = this.findEntityByUUID(world, entityUUID);
                  if (entity != null && entity.isEntityAlive()) {
                     allDead = false;
                     break;
                  }
               }

               if (allDead) {
                  this.cleanEntityCacheForPlayer(quest, playerId);
                  quest.getSpawnedEntities().clear();
                  if (step.sceneGroup != null) {
                     quest.clearSceneEntities();
                  }

                  EntityPlayerMP player = this.getPlayerByUUID(world, playerId);
                  if (player != null) {
                     player.sendMessage(new TextComponentString("§eThe NPC has disappeared. Relocating..."));
                     this.setupCurrentStep(player, quest);
                  }
               }
            }
         }
      }

      Set<UUID> allSceneEntityUUIDs = new HashSet();

      for(Map.Entry<UUID, Map<String, QuestInstance>> playerEntry : new ArrayList(this.activeQuests.entrySet())) {
         for(QuestInstance quest : ((Map)playerEntry.getValue()).values()) {
            allSceneEntityUUIDs.addAll(quest.getSceneEntities().values());
         }
      }

      Iterator<Map.Entry<String, UUID>> it = this.sharedDialogNpcs.entrySet().iterator();

      while(true) {
         UUID npcUUID;
         Entity npcEntity;
         String keyConfigId;
         while(true) {
            if (!it.hasNext()) {
               return;
            }

            Map.Entry<String, UUID> entry = (Map.Entry)it.next();
            npcUUID = (UUID)entry.getValue();
            npcEntity = this.findEntityByUUID(world, npcUUID);
            if (npcEntity != null && npcEntity.isEntityAlive()) {
               if (!allSceneEntityUUIDs.contains(npcUUID)) {
                  String sharingKey = (String)entry.getKey();
                  keyConfigId = null;

                  try {
                     String[] parts = sharingKey.split("::");
                     if (parts.length == 4) {
                        keyConfigId = parts[0];
                        break;
                     }
                  } catch (Exception var16) {
                  }
               }
            } else {
               it.remove();
            }
         }

         boolean anyNearby = false;

         for(Map.Entry<UUID, Map<String, QuestInstance>> playerEntry : this.activeQuests.entrySet()) {
            EntityPlayerMP player = this.getPlayerByUUID(world, (UUID)playerEntry.getKey());
            if (player != null) {
               for(QuestInstance quest : ((Map)playerEntry.getValue()).values()) {
                  QuestStep step = quest.getCurrentStep();
                  if (step != null && step.type == QuestStep.StepType.DIALOG && step.hasNpcConfig() && keyConfigId.equals(step.npcConfigId) && player.getDistanceSq(npcEntity) <= (double)10000.0F) {
                     anyNearby = true;
                     break;
                  }
               }

               if (anyNearby) {
                  break;
               }
            }
         }

         if (!anyNearby) {
            npcEntity.setDead();
            it.remove();

            for(Map.Entry<UUID, Map<String, QuestInstance>> playerEntry : this.activeQuests.entrySet()) {
               for(QuestInstance quest : ((Map)playerEntry.getValue()).values()) {
                  quest.getSpawnedEntities().remove(npcUUID);
               }
            }
         }
      }
   }

   private void tickCombatEntityLivenessCheck(World world) {
      List<Map.Entry<UUID, String>> toAdvance = null;

      for(Map.Entry<UUID, Map<String, QuestInstance>> playerEntry : new ArrayList(this.activeQuests.entrySet())) {
         UUID playerId = (UUID)playerEntry.getKey();

         for(Map.Entry<String, QuestInstance> slotEntry : new ArrayList(((Map)playerEntry.getValue()).entrySet())) {
            QuestInstance quest = (QuestInstance)slotEntry.getValue();
            QuestStep step = quest.getCurrentStep();
            if (step != null && step.type == QuestStep.StepType.COMBAT && !quest.getSpawnedEntities().isEmpty() && quest.getRetreatPhase() < 1) {
               boolean anyAlive = false;
               List<UUID> deadEntities = null;

               for(UUID entityUUID : new ArrayList(quest.getSpawnedEntities())) {
                  Entity entity = this.findEntityByUUID(world, entityUUID);
                  if (entity != null && entity.isEntityAlive()) {
                     anyAlive = true;
                  } else {
                     if (deadEntities == null) {
                        deadEntities = new ArrayList();
                     }

                     deadEntities.add(entityUUID);
                  }
               }

               if (deadEntities != null) {
                  for(UUID deadUUID : deadEntities) {
                     quest.removeSpawnedEntity(deadUUID);
                     this.entityToPlayers.remove(deadUUID);
                  }
               }

               if (!anyAlive && !quest.getSpawnedEntities().isEmpty()) {
                  quest.getSpawnedEntities().clear();
               }

               if (!anyAlive) {
                  if (toAdvance == null) {
                     toAdvance = new ArrayList();
                  }

                  toAdvance.add(new AbstractMap.SimpleEntry(playerId, slotEntry.getKey()));
               }
            }
         }
      }

      if (toAdvance != null) {
         for(Map.Entry<UUID, String> entry : toAdvance) {
            UUID playerId = (UUID)entry.getKey();
            String slot = (String)entry.getValue();
            Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
            if (playerSlots != null) {
               QuestInstance quest = (QuestInstance)playerSlots.get(slot);
               if (quest != null) {
                  QuestStep step = quest.getCurrentStep();
                  if (step != null && step.type == QuestStep.StepType.COMBAT) {
                     EntityPlayerMP player = this.getPlayerByUUID(world, playerId);
                     if (player != null && (step.isKillAllSpawned() || step.isHealthRetreat())) {
                        String encKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
                        if (this.sharedEncounters.get(encKey) != null) {
                           this.advanceSharedEncounterParticipants(world, quest.getQuestId(), quest.getCurrentStepIndex(), playerId);
                        } else {
                           this.sendCombatHealthClear(player);
                           quest.getSpawnedEntities().clear();
                           this.setupCurrentStep(player, quest);
                           QuestSavedData.get(world).saveQuest(playerId, quest);
                        }
                     }
                  }
               }
            }
         }
      }

   }

   private void tickHealthRetreatQuests(World world) {
      List<Map.Entry<UUID, String>> advanceList = null;

      for(Map.Entry<UUID, Map<String, QuestInstance>> playerEntry : new ArrayList(this.activeQuests.entrySet())) {
         for(Map.Entry<String, QuestInstance> slotEntry : new ArrayList(((Map)playerEntry.getValue()).entrySet())) {
            QuestInstance quest = (QuestInstance)slotEntry.getValue();
            QuestStep step = quest.getCurrentStep();
            if (step != null && step.isHealthRetreat()) {
               UUID playerId = (UUID)playerEntry.getKey();
               EntityPlayerMP player = this.getPlayerByUUID(world, playerId);
               if (player != null) {
                  int phase = quest.getRetreatPhase();
                  boolean playerDead = player.isDead || player.getHealth() <= 0.0F;
                  if (phase == 0) {
                     if (!playerDead && !quest.getSpawnedEntities().isEmpty()) {
                        boolean allAtThreshold = true;
                        int foundCount = 0;

                        for(UUID entityUUID : quest.getSpawnedEntities()) {
                           Entity entity = this.findEntityByUUID(world, entityUUID);
                           if (entity instanceof EntityLivingBase) {
                              ++foundCount;
                              EntityLivingBase living = (EntityLivingBase)entity;
                              float healthPercent = living.getHealth() / living.getMaxHealth();
                              if ((double)healthPercent > step.retreatHealthPercent) {
                                 allAtThreshold = false;
                              }
                           }
                        }

                        if (allAtThreshold && foundCount > 0) {
                           quest.setRetreatPhase(1);
                           quest.setRetreatTimer(0);

                           for(UUID entityUUID : quest.getSpawnedEntities()) {
                              Entity entity = this.findEntityByUUID(world, entityUUID);
                              if (entity instanceof EntityLivingBase) {
                                 if (entity instanceof EntityItachi.EntityCustom) {
                                    ((EntityItachi.EntityCustom)entity).setRetreating(true);
                                 } else if (entity instanceof EntityKisame.EntityCustom) {
                                    ((EntityKisame.EntityCustom)entity).setRetreating(true);
                                 } else if (entity instanceof QuestNpcBase) {
                                    ((QuestNpcBase)entity).setRetreating(true);
                                 }

                                 double dx = entity.posX - player.posX;
                                 double dz = entity.posZ - player.posZ;
                                 double dist = Math.sqrt(dx * dx + dz * dz);
                                 if (dist > (double)0.0F) {
                                    entity.motionX = dx / dist * (double)1.5F;
                                    entity.motionY = (double)0.5F;
                                    entity.motionZ = dz / dist * (double)1.5F;
                                    entity.velocityChanged = true;
                                 }
                              }
                           }

                           player.sendMessage(new TextComponentString("§c§lThe enemies are retreating!"));
                           QuestSavedData.get(world).saveQuest(playerId, quest);
                        }
                     }
                  } else if (phase == 1) {
                     quest.incrementRetreatTimer();
                     if (quest.getRetreatTimer() >= 2) {
                        quest.setRetreatPhase(2);
                        quest.setRetreatTimer(0);
                        String questId = quest.getQuestId();
                        boolean hasJiraiyaRescue = "itachi_encounter".equals(questId);
                        if (hasJiraiyaRescue && !playerDead) {
                           BlockPos combatPos = step.defaultPosition;
                           double distToFight = player.getDistance((double)combatPos.getX() + (double)0.5F, (double)combatPos.getY(), (double)combatPos.getZ() + (double)0.5F);
                           if (distToFight <= (double)50.0F && quest.getJiraiyaRescueUUID() == null) {
                              BlockPos spawnPos = player.getPosition().add(3, 0, 3);
                              Entity jiraiya = WaypointSpawnLogic.spawnQuestNpc(world, player, "jiraiya_rescue", spawnPos);
                              if (jiraiya != null) {
                                 quest.setJiraiyaRescueUUID(jiraiya.getUniqueID());
                                 if (jiraiya instanceof EntityJiraiya.EntityCustom) {
                                    ((EntityJiraiya.EntityCustom)jiraiya).setMaxLifetime(600);
                                 }
                              }
                           }
                        }

                        QuestSavedData.get(world).saveQuest(playerId, quest);
                     }
                  } else if (phase == 2) {
                     quest.incrementRetreatTimer();
                     int t = quest.getRetreatTimer();
                     String questId = quest.getQuestId();
                     boolean hasJiraiyaRescue = "itachi_encounter".equals(questId);
                     if (!playerDead && hasJiraiyaRescue) {
                        if (t == 1) {
                           player.sendMessage(new TextComponentString("§a§lJiraiya: §fGet away from them!"));
                        } else if (t == 4) {
                           player.sendMessage(new TextComponentString("§a§lJiraiya: §fI'll handle this. Go!"));
                        } else if (t == 8) {
                           player.sendMessage(new TextComponentString("§c§lItachi: §f...Let's go, Kisame. We got what we came for."));
                        }
                     } else if (!playerDead && "defeat_orochimaru_forest".equals(questId)) {
                        if (t == 1) {
                           player.sendMessage(new TextComponentString("§5§lOrochimaru: §fKuku... interesting. You're stronger than I expected."));
                        } else if (t == 5) {
                           player.sendMessage(new TextComponentString("§5§lOrochimaru: §fWe'll meet again... Sasuke-kun will come to me eventually."));
                        }
                     }

                     if (t != 10) {
                        if (t >= 18) {
                           quest.setRetreatPhase(3);
                           if (quest.getJiraiyaRescueUUID() != null) {
                              Entity jiraiyaEntity = this.findEntityByUUID(world, quest.getJiraiyaRescueUUID());
                              if (jiraiyaEntity != null && !jiraiyaEntity.isDead) {
                                 WaypointSpawnLogic.spawnSmokeParticles(world, jiraiyaEntity.posX, jiraiyaEntity.posY, jiraiyaEntity.posZ);
                              }

                              HashSet<UUID> jSet = new HashSet();
                              jSet.add(quest.getJiraiyaRescueUUID());
                              WaypointSpawnLogic.cleanupQuestEntities(world, jSet);
                              quest.setJiraiyaRescueUUID((UUID)null);
                           }

                           if (advanceList == null) {
                              advanceList = new ArrayList();
                           }

                           advanceList.add(new AbstractMap.SimpleEntry(playerId, slotEntry.getKey()));
                        }
                     } else {
                        for(UUID entityUUID : quest.getSpawnedEntities()) {
                           Entity entity = this.findEntityByUUID(world, entityUUID);
                           if (entity != null && !entity.isDead) {
                              if (entity instanceof QuestNpcBase) {
                                 ((QuestNpcBase)entity).setRetreating(false);
                              }

                              WaypointSpawnLogic.spawnSmokeParticles(world, entity.posX, entity.posY, entity.posZ);
                           }
                        }

                        this.cleanEntityCacheFull(quest);
                        WaypointSpawnLogic.cleanupQuestEntities(world, quest.getSpawnedEntities());
                        quest.getSpawnedEntities().clear();
                     }
                  }
               }
            }
         }
      }

      if (advanceList != null) {
         for(Map.Entry<UUID, String> entry : advanceList) {
            UUID playerId = (UUID)entry.getKey();
            Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
            if (playerSlots != null) {
               QuestInstance quest = (QuestInstance)playerSlots.get(entry.getValue());
               if (quest != null) {
                  EntityPlayerMP player = this.getPlayerByUUID(world, playerId);
                  if (player != null) {
                     player.sendMessage(new TextComponentString("§aJiraiya has driven off the attackers."));
                     String encKey = this.getEncounterKey(quest.getQuestId(), quest.getCurrentStepIndex());
                     if (this.sharedEncounters.get(encKey) != null) {
                        this.advanceSharedEncounterParticipants(world, quest.getQuestId(), quest.getCurrentStepIndex(), playerId);
                     } else {
                        this.sendCombatHealthClear(player);
                        this.advanceQuest(player, quest);
                     }
                  }
               }
            }
         }
      }

   }

   private void sendCombatHealthUpdates(World world) {
      for(Map.Entry<UUID, Map<String, QuestInstance>> playerEntry : new ArrayList(this.activeQuests.entrySet())) {
         UUID playerId = (UUID)playerEntry.getKey();
         EntityPlayerMP player = this.getPlayerByUUID(world, playerId);
         if (player != null) {
            List<QuestCombatHealthMessage.EnemyHealthEntry> entries = null;
            Map<Integer, Float> playerLastSent = (Map)this.lastSentHealth.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap());

            for(QuestInstance quest : ((Map)playerEntry.getValue()).values()) {
               QuestStep step = quest.getCurrentStep();
               if (step != null && step.type == QuestStep.StepType.COMBAT && !quest.getSpawnedEntities().isEmpty()) {
                  for(UUID entityUUID : quest.getSpawnedEntities()) {
                     Entity entity = this.findEntityByUUID(world, entityUUID);
                     if (entity instanceof EntityLivingBase && entity.isEntityAlive()) {
                        EntityLivingBase living = (EntityLivingBase)entity;
                        float currentHealth = living.getHealth();
                        float maxHealth = living.getMaxHealth();
                        int entityId = entity.getEntityId();
                        Float lastHealth = (Float)playerLastSent.get(entityId);
                        playerLastSent.put(entityId, currentHealth);
                        if (entries == null) {
                           entries = new ArrayList();
                        }

                        int themeColor;
                        int accentColor;
                        String name;
                        if (entity instanceof EntityItachi.EntityCustom) {
                           themeColor = -3407872;
                           accentColor = -13434880;
                           name = "Itachi Uchiha";
                        } else if (entity instanceof EntityKisame.EntityCustom) {
                           themeColor = -16750900;
                           accentColor = -16764058;
                           name = "Kisame Hoshigaki";
                        } else if (entity instanceof EntityTsunade.EntityCustom) {
                           themeColor = -3364352;
                           accentColor = -10070784;
                           name = "Tsunade";
                        } else if (entity instanceof EntityKabuto.EntityCustom) {
                           themeColor = -10083670;
                           accentColor = -13430443;
                           name = "Kabuto Yakushi";
                        } else if (entity instanceof EntityOrochimaru.EntityCustom) {
                           themeColor = -7846708;
                           accentColor = -12311962;
                           name = "Orochimaru";
                        } else if (entity instanceof EntityOrochimaruSnake.EntityCustom) {
                           themeColor = -8965189;
                           accentColor = -12969378;
                           name = "Manda";
                        } else if (entity instanceof QuestNpcBase) {
                           QuestNpcBase questNpcBase = (QuestNpcBase)entity;
                           String cfgId = questNpcBase.getNpcConfigId();
                           NpcConfig cfg = cfgId != null && !cfgId.isEmpty() ? NpcConfigRegistry.get(cfgId) : null;
                           if (cfg != null && cfg.hasHealthBarColors()) {
                              themeColor = cfg.getThemeColor();
                              accentColor = cfg.getAccentColor();
                              name = cfg.getDisplayName();
                           } else {
                              themeColor = -7829368;
                              accentColor = -12303292;
                              name = entity.hasCustomName() ? entity.getCustomNameTag() : entity.getName();
                           }
                        } else {
                           themeColor = -7829368;
                           accentColor = -12303292;
                           name = entity.hasCustomName() ? entity.getCustomNameTag() : entity.getName();
                        }

                        entries.add(new QuestCombatHealthMessage.EnemyHealthEntry(name, entity.getEntityId(), living.getHealth(), living.getMaxHealth(), themeColor, accentColor));
                        if (entity instanceof EntityOrochimaru.EntityCustom) {
                           EntityOrochimaru.EntityCustom oro = (EntityOrochimaru.EntityCustom)entity;
                           UUID mandaId = oro.getMandaUUID();
                           if (mandaId != null) {
                              Entity mandaEntity = this.findEntityByUUID(world, mandaId);
                              if (mandaEntity instanceof EntityLivingBase && mandaEntity.isEntityAlive()) {
                                 EntityLivingBase mandaLiving = (EntityLivingBase)mandaEntity;
                                 entries.add(new QuestCombatHealthMessage.EnemyHealthEntry("Manda", mandaEntity.getEntityId(), mandaLiving.getHealth(), mandaLiving.getMaxHealth(), -8965189, -12969378));
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (entries != null && !entries.isEmpty()) {
               QuestCombatHealthMessage.EnemyHealthEntry[] arr = (QuestCombatHealthMessage.EnemyHealthEntry[])entries.toArray(new QuestCombatHealthMessage.EnemyHealthEntry[0]);
               QuestModInit.NETWORK.sendTo(QuestCombatHealthMessage.update(arr), player);
            }
         }
      }

   }

   public void sendCombatHealthClear(EntityPlayerMP player) {
      if (player != null) {
         QuestModInit.NETWORK.sendTo(QuestCombatHealthMessage.clear(), player);
         this.lastSentHealth.remove(player.getUniqueID());
      }

   }

   private Entity findEntityByUUID(World world, UUID uuid) {
      if (world instanceof WorldServer) {
         WorldServer worldServer = (WorldServer)world;
         if (worldServer.getMinecraftServer() != null) {
            Entity entity = worldServer.getEntityFromUuid(uuid);
            if (entity != null && !entity.isDead) {
               return entity;
            }

            return null;
         }
      }

      for(Entity entity : new ArrayList(world.loadedEntityList)) {
         if (entity != null && !entity.isDead && entity.getUniqueID().equals(uuid)) {
            return entity;
         }
      }

      return null;
   }

   private Entity findExistingNpcByConfigId(World world, String npcConfigId, BlockPos expectedPos) {
      Iterator var4 = (new ArrayList(world.loadedEntityList)).iterator();

      Entity e;
      while(true) {
         if (!var4.hasNext()) {
            return null;
         }

         e = (Entity)var4.next();
         if (e != null && !e.isDead) {
            NBTTagCompound data = e.getEntityData();
            if (data.getBoolean("questEntity")) {
               String configId = data.getString("npcConfigId");
               if (npcConfigId.equals(configId)) {
                  if (expectedPos == null) {
                     break;
                  }

                  double dx = e.posX - (double)expectedPos.getX();
                  double dz = e.posZ - (double)expectedPos.getZ();
                  if (!(dx * dx + dz * dz > (double)2500.0F)) {
                     break;
                  }
               }
            }
         }
      }

      return e;
   }

   private void cleanEntityCacheForPlayer(QuestInstance quest, UUID playerId) {
      for(UUID entityUUID : quest.getSpawnedEntities()) {
         Set<UUID> owners = (Set)this.entityToPlayers.get(entityUUID);
         if (owners != null) {
            owners.remove(playerId);
            if (owners.isEmpty()) {
               this.entityToPlayers.remove(entityUUID);
            }
         }
      }

   }

   private void cleanEntityCacheFull(QuestInstance quest) {
      for(UUID entityUUID : quest.getSpawnedEntities()) {
         this.entityToPlayers.remove(entityUUID);
      }

   }

   public Map<String, QuestInstance> getActiveQuests(UUID playerId) {
      Map<String, QuestInstance> slots = (Map)this.activeQuests.get(playerId);
      return slots != null ? Collections.unmodifiableMap(slots) : Collections.emptyMap();
   }

   public QuestInstance getActiveQuest(UUID playerId) {
      Map<String, QuestInstance> slots = (Map)this.activeQuests.get(playerId);
      if (slots != null && !slots.isEmpty()) {
         for(Map.Entry<String, QuestInstance> entry : slots.entrySet()) {
            if (isStorySlot((String)entry.getKey())) {
               return (QuestInstance)entry.getValue();
            }
         }

         return (QuestInstance)slots.values().iterator().next();
      } else {
         return null;
      }
   }

   public QuestInstance getActiveStoryQuest(UUID playerId, String storyline) {
      return this.getActiveQuestInSlot(playerId, storySlotFor(storyline));
   }

   public List<String> getActiveStorylines(UUID playerId) {
      Map<String, QuestInstance> slots = (Map)this.activeQuests.get(playerId);
      if (slots == null) {
         return Collections.emptyList();
      } else {
         List<String> result = new ArrayList();

         for(String slot : slots.keySet()) {
            if (isStorySlot(slot)) {
               result.add(getStorylineFromSlot(slot));
            }
         }

         return result;
      }
   }

   public QuestInstance getActiveQuestInSlot(UUID playerId, String slot) {
      Map<String, QuestInstance> slots = (Map)this.activeQuests.get(playerId);
      return slots == null ? null : (QuestInstance)slots.get(slot);
   }

   private QuestInstance findActiveQuestById(UUID playerId, String questId) {
      Map<String, QuestInstance> slots = (Map)this.activeQuests.get(playerId);
      if (slots == null) {
         return null;
      } else {
         for(QuestInstance quest : slots.values()) {
            if (quest.getQuestId().equals(questId)) {
               return quest;
            }
         }

         return null;
      }
   }

   public boolean hasAnyActiveQuest(UUID playerId) {
      Map<String, QuestInstance> slots = (Map)this.activeQuests.get(playerId);
      return slots != null && !slots.isEmpty();
   }

   public boolean isInActiveCombat(UUID playerId) {
      Map<String, QuestInstance> slots = (Map)this.activeQuests.get(playerId);
      if (slots == null) {
         return false;
      } else {
         for(QuestInstance quest : slots.values()) {
            if (!quest.getSpawnedEntities().isEmpty()) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean isPlayerInQuestCombat(EntityPlayerMP player) {
      return getInstance().isInActiveCombat(player.getUniqueID());
   }

   public boolean hasCompletedQuest(UUID playerId, String questId) {
      Set<String> completed = (Set)this.completedQuests.get(playerId);
      return completed != null && completed.contains(questId);
   }

   public Set<String> getCompletedQuests(UUID playerId) {
      return (Set)this.completedQuests.getOrDefault(playerId, new HashSet());
   }

   public void sendQuestTrackInfo(EntityPlayerMP player) {
      Map<String, QuestInstance> slots = this.getActiveQuests(player.getUniqueID());
      if (slots.isEmpty()) {
         player.sendMessage(new TextComponentString("§7No active quests."));
      } else {
         player.sendMessage(new TextComponentString("§6--- Active Quests ---"));

         for(Map.Entry<String, QuestInstance> entry : slots.entrySet()) {
            String slot = (String)entry.getKey();
            QuestInstance quest = (QuestInstance)entry.getValue();
            String questName;
            int stepCount;
            if (quest.isGenerated()) {
               questName = quest.getGeneratedName() != null ? quest.getGeneratedName() : quest.getQuestId();
               stepCount = quest.getGeneratedStepCount();
            } else {
               QuestDefinition def = QuestRegistry.getById(quest.getQuestId());
               if (def == null) {
                  continue;
               }

               questName = def.getName();
               stepCount = def.getStepCount();
            }

            QuestStep step = quest.getCurrentStep();
            player.sendMessage(new TextComponentString("§e[" + slot.toUpperCase() + "] §6" + questName));
            player.sendMessage(new TextComponentString("  §eStep " + (quest.getCurrentStepIndex() + 1) + "/" + stepCount));
            if (step != null) {
               player.sendMessage(new TextComponentString("  §fObjective: " + step.description));
            }

            WaypointData wp = this.waypointManager.getWaypoint(player.getUniqueID(), quest.getQuestId());
            if (wp != null) {
               BlockPos pos = wp.getPosition();
               player.sendMessage(new TextComponentString("  §7Waypoint: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
            }
         }

      }
   }

   public void forceGiveQuest(EntityPlayerMP player, String questId) {
      UUID playerId = player.getUniqueID();
      QuestDefinition def = QuestRegistry.getById(questId);
      if (def != null) {
         String slot = getSlotForQuest(def);
         Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
         if (playerSlots != null && playerSlots.containsKey(slot)) {
            QuestInstance existing = (QuestInstance)playerSlots.remove(slot);
            this.cleanEntityCacheFull(existing);
            WaypointSpawnLogic.cleanupQuestEntities(player.world, existing.getSpawnedEntities());
            this.waypointManager.clearWaypoint(player, existing.getQuestId());
         }

         QuestInstance quest = new QuestInstance(questId);
         ((Map)this.activeQuests.computeIfAbsent(playerId, (k) -> new ConcurrentHashMap())).put(slot, quest);
         QuestSavedData savedData = QuestSavedData.get(player.world);
         savedData.saveQuest(playerId, quest);
         player.sendMessage(new TextComponentString("§aQuest assigned: §6" + def.getName()));
         this.setupCurrentStep(player, quest);
         QuestNetworkHelper.sendQuestSync(player);
      }
   }

   public void forceCompleteQuest(EntityPlayerMP player, String slot) {
      UUID playerId = player.getUniqueID();
      QuestInstance quest = this.getActiveQuestInSlot(playerId, slot);
      if (quest == null) {
         player.sendMessage(new TextComponentString("§cNo active " + getCategoryDisplayName(slot) + " quest to complete."));
      } else {
         this.cleanEntityCacheFull(quest);
         WaypointSpawnLogic.cleanupQuestEntities(player.world, quest.getSpawnedEntities());
         this.completeQuest(player, quest);
      }
   }

   public void forceCompleteQuest(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      Map<String, QuestInstance> slots = (Map)this.activeQuests.get(playerId);
      if (slots != null && !slots.isEmpty()) {
         String slot = null;

         for(String s : slots.keySet()) {
            if (isStorySlot(s)) {
               slot = s;
               break;
            }
         }

         if (slot == null) {
            slot = (String)slots.keySet().iterator().next();
         }

         this.forceCompleteQuest(player, slot);
      } else {
         player.sendMessage(new TextComponentString("§cNo active quest to complete."));
      }
   }

   public void resetQuest(EntityPlayerMP player, String questId) {
      UUID playerId = player.getUniqueID();
      Map<String, QuestInstance> playerSlots = (Map)this.activeQuests.get(playerId);
      if (playerSlots != null) {
         String slotToRemove = null;

         for(Map.Entry<String, QuestInstance> entry : playerSlots.entrySet()) {
            QuestInstance q = (QuestInstance)entry.getValue();
            if (questId == null || q.getQuestId().equals(questId)) {
               this.cleanEntityCacheFull(q);
               WaypointSpawnLogic.cleanupQuestEntities(player.world, q.getSpawnedEntities());
               this.waypointManager.clearWaypoint(player, q.getQuestId());

               for(int stepIdx = 0; stepIdx <= q.getCurrentStepIndex(); ++stepIdx) {
                  String encKey = this.getEncounterKey(q.getQuestId(), stepIdx);
                  this.sharedEncounters.remove(encKey);
               }

               if (questId != null) {
                  slotToRemove = (String)entry.getKey();
                  break;
               }
            }
         }

         if (questId != null && slotToRemove != null) {
            playerSlots.remove(slotToRemove);
            if (playerSlots.isEmpty()) {
               this.activeQuests.remove(playerId);
            }
         } else if (questId == null) {
            this.waypointManager.clearAllWaypoints(playerId);
            this.activeQuests.remove(playerId);
         }
      }

      QuestSavedData savedData = QuestSavedData.get(player.world);
      if (questId != null) {
         savedData.resetQuest(playerId, questId);
         Set<String> completed = (Set)this.completedQuests.get(playerId);
         if (completed != null) {
            completed.remove(questId);
         }
      } else {
         savedData.resetAllQuests(playerId);
         this.completedQuests.remove(playerId);
         this.activeQuests.remove(playerId);
      }

      player.sendMessage(new TextComponentString("§eQuest progress reset" + (questId != null ? " for " + questId : " (all quests)") + "."));
      QuestNetworkHelper.sendQuestSync(player);
   }

   public void acceptRandomQuest(EntityPlayerMP player) {
      this.acceptOffer(player, "random");
   }

   private static String formatCooldown(long ms) {
      long totalSeconds = ms / 1000L;
      long hours = totalSeconds / 3600L;
      long minutes = totalSeconds % 3600L / 60L;
      if (hours > 0L) {
         return hours + "h " + minutes + "m";
      } else {
         return minutes > 0L ? minutes + "m" : "< 1m";
      }
   }

   private EntityPlayerMP getPlayerByUUID(World world, UUID playerId) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      return server == null ? null : server.getPlayerList().getPlayerByUUID(playerId);
   }

   private String getEncounterKey(String questId, int stepIndex) {
      return questId + "_" + stepIndex;
   }

   public void removeSharedEncounter(String encKey) {
      if (encKey != null) {
         this.sharedEncounters.remove(encKey);
      }

   }

   public void onLivingDamage(LivingDamageEvent event) {
      Entity target = event.getEntity();
      if (target != null && !target.world.isRemote) {
         if (target.getEntityData().getBoolean("questEntity")) {
            if (target instanceof EntityLivingBase) {
               EntityLivingBase living = (EntityLivingBase)target;
               UUID targetUUID = target.getUniqueID();
               Set<UUID> cachedOwners = (Set)this.entityToPlayers.get(targetUUID);
               if (cachedOwners != null) {
                  boolean handled = false;

                  for(UUID ownerId : cachedOwners) {
                     if (handled) {
                        break;
                     }

                     Map var9 = (Map)this.activeQuests.get(ownerId);
                  }
               }
            }

            EntityPlayerMP player = this.resolvePlayerFromDamage(event.getSource());
            if (player != null) {
               UUID targetUUID = target.getUniqueID();
               UUID playerUUID = player.getUniqueID();
               float amount = event.getAmount();

               for(SharedEncounter enc : this.sharedEncounters.values()) {
                  if (enc.getEntityUUIDs().contains(targetUUID)) {
                     enc.addParticipant(playerUUID);
                     enc.recordDamage(playerUUID, (double)amount);
                     break;
                  }
               }

            }
         }
      }
   }

   private EntityPlayerMP resolvePlayerFromDamage(DamageSource source) {
      Entity trueSource = source.getTrueSource();
      if (trueSource instanceof EntityPlayerMP) {
         return (EntityPlayerMP)trueSource;
      } else {
         Entity immediate = source.getImmediateSource();
         if (immediate instanceof EntityPlayerMP) {
            return (EntityPlayerMP)immediate;
         } else {
            if (immediate != null && immediate.getEntityData().hasKey("OwnerUUID")) {
               try {
                  UUID ownerUUID = UUID.fromString(immediate.getEntityData().getString("OwnerUUID"));
                  Entity owner = immediate.world.getPlayerEntityByUUID(ownerUUID);
                  if (owner instanceof EntityPlayerMP) {
                     return (EntityPlayerMP)owner;
                  }
               } catch (IllegalArgumentException var11) {
               }
            }

            if (trueSource != null && !(trueSource instanceof EntityPlayerMP)) {
               for(Entity passenger : trueSource.getPassengers()) {
                  if (passenger instanceof EntityPlayerMP) {
                     return (EntityPlayerMP)passenger;
                  }
               }

               Entity riding = trueSource.getRidingEntity();
               if (riding instanceof EntityPlayerMP) {
                  return (EntityPlayerMP)riding;
               }

               if (trueSource.getEntityData().hasKey("OwnerUUID")) {
                  try {
                     UUID ownerUUID = UUID.fromString(trueSource.getEntityData().getString("OwnerUUID"));
                     Entity owner = trueSource.world.getPlayerEntityByUUID(ownerUUID);
                     if (owner instanceof EntityPlayerMP) {
                        return (EntityPlayerMP)owner;
                     }
                  } catch (IllegalArgumentException var10) {
                  }
               }
            }

            if (trueSource instanceof EntityLivingBase && trueSource.getEntityData().hasKey("SummonerID")) {
               try {
                  UUID summonerId = UUID.fromString(trueSource.getEntityData().getString("SummonerID"));
                  Entity summoner = trueSource.world.getPlayerEntityByUUID(summonerId);
                  if (summoner instanceof EntityPlayerMP) {
                     return (EntityPlayerMP)summoner;
                  }
               } catch (IllegalArgumentException var9) {
               }
            }

            for(Entity src : new Entity[]{trueSource, immediate}) {
               if (src != null) {
                  if (src instanceof EntityThrowable) {
                     Entity thrower = ((EntityThrowable)src).getThrower();
                     if (thrower instanceof EntityPlayerMP) {
                        return (EntityPlayerMP)thrower;
                     }
                  }

                  if (src instanceof EntityArrow) {
                     Entity shooter = ((EntityArrow)src).shootingEntity;
                     if (shooter instanceof EntityPlayerMP) {
                        return (EntityPlayerMP)shooter;
                     }
                  }
               }
            }

            return null;
         }
      }
   }

   public void advanceSharedEncounterParticipants(World world, String questId, int stepIndex, UUID triggeringPlayerId) {
      String encKey = this.getEncounterKey(questId, stepIndex);
      SharedEncounter enc = (SharedEncounter)this.sharedEncounters.get(encKey);
      if (enc == null) {
         System.out.println("[QuestSystem] WARN: advanceSharedEncounter called but enc is null for " + encKey + ", triggering player " + triggeringPlayerId + " -- already processed or lost, skipping.");
         EntityPlayerMP p = this.getPlayerByUUID(world, triggeringPlayerId);
         if (p != null) {
            this.sendCombatHealthClear(p);
         }

      } else {
         Set<UUID> advancedPlayers = new HashSet();

         for(UUID pid : enc.getAllParticipants()) {
            QuestInstance pQuest = this.findActiveQuestById(pid, enc.getQuestId());
            if (pQuest != null && pQuest.getCurrentStepIndex() == enc.getStepIndex() && enc.meetsThreshold(pid)) {
               int pct = (int)Math.round(enc.getContributionPercent(pid) * (double)100.0F);
               advancedPlayers.add(pid);
               EntityPlayerMP p = this.getPlayerByUUID(world, pid);
               if (p != null) {
                  this.sendCombatHealthClear(p);
                  this.advanceQuest(p, pQuest);
                  p.sendMessage(new TextComponentString("§aQuest objective completed! §7(Contribution: " + pct + "%)"));
               } else if (!pQuest.isOnFinalStep()) {
                  pQuest.advanceStep();
                  QuestSavedData.get(world).saveQuest(pid, pQuest);
               } else {
                  pQuest.complete();
                  QuestSavedData savedData = QuestSavedData.get(world);
                  String questSlot = getSlotForInstance(pQuest);
                  if (pQuest.isGenerated()) {
                     savedData.recordSlotCompletion(pid, questSlot);
                     savedData.removeActiveQuest(pid, pQuest.getQuestId());
                  } else {
                     QuestDefinition pDef = QuestRegistry.getById(pQuest.getQuestId());
                     if (pDef != null && pDef.isRepeatable()) {
                        savedData.recordRepeatableCompletion(pid, pQuest.getQuestId());
                        savedData.removeActiveQuest(pid, pQuest.getQuestId());
                     } else {
                        ((Set)this.completedQuests.computeIfAbsent(pid, (k) -> new HashSet())).add(pQuest.getQuestId());
                        savedData.completeQuest(pid, pQuest.getQuestId());
                     }
                  }

                  this.waypointManager.removeWaypointByUUID(pid, pQuest.getQuestId());
                  Map<String, QuestInstance> pSlots = (Map)this.activeQuests.get(pid);
                  if (pSlots != null) {
                     pSlots.remove(questSlot);
                     if (pSlots.isEmpty()) {
                        this.activeQuests.remove(pid);
                     }
                  }
               }
            }
         }

         for(UUID pid : enc.getAllParticipants()) {
            if (!advancedPlayers.contains(pid)) {
               QuestInstance pQuest = this.findActiveQuestById(pid, enc.getQuestId());
               if (pQuest != null && pQuest.getCurrentStepIndex() == enc.getStepIndex()) {
                  EntityPlayerMP p = this.getPlayerByUUID(world, pid);
                  if (p != null) {
                     this.sendCombatHealthClear(p);
                     int pct = (int)Math.round(enc.getContributionPercent(pid) * (double)100.0F);
                     p.sendMessage(new TextComponentString("§cInsufficient contribution (" + pct + "%). Need 30% to get quest credit."));
                     this.cleanEntityCacheForPlayer(pQuest, pid);
                     pQuest.getSpawnedEntities().clear();
                     pQuest.setRetreatPhase(0);
                     pQuest.setRetreatTimer(0);
                     this.setupCurrentStep(p, pQuest);
                     QuestSavedData.get(world).saveQuest(pid, pQuest);
                  }
               }
            }
         }

         this.sharedEncounters.remove(encKey);
      }
   }

   private boolean hasOtherLivingParticipants(World world, String encKey, UUID excludePlayer) {
      SharedEncounter enc = (SharedEncounter)this.sharedEncounters.get(encKey);
      if (enc == null) {
         return false;
      } else {
         for(UUID pid : enc.getAllParticipants()) {
            if (!pid.equals(excludePlayer)) {
               EntityPlayerMP p = this.getPlayerByUUID(world, pid);
               if (p != null && p.isEntityAlive()) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean hasAdvancement(EntityPlayerMP player, String advancementId) {
      try {
         ResourceLocation loc = new ResourceLocation(advancementId);
         Advancement adv = player.getServer().getAdvancementManager().getAdvancement(loc);
         return adv != null && player.getAdvancements().getProgress(adv).isDone();
      } catch (Exception var5) {
         return false;
      }
   }

   private void ensureHighestRank(EntityPlayerMP player, UUID playerId) {
      Set<String> playerCompleted = (Set)this.completedQuests.getOrDefault(playerId, new HashSet());
      String highestRank = null;

      for(String[] entry : RANK_PROGRESSION) {
         if (playerCompleted.contains(entry[0])) {
            highestRank = entry[1];
         }
      }

      if (highestRank != null) {
         player.getServer().getCommandManager().executeCommand(player.getServer(), "rank add " + player.getName() + " " + highestRank);
      }

   }

   private void grantAdvancement(EntityPlayerMP player, String advancementId) {
      try {
         ResourceLocation loc = new ResourceLocation(advancementId);
         Advancement adv = player.getServer().getAdvancementManager().getAdvancement(loc);
         if (adv != null) {
            AdvancementProgress progress = player.getAdvancements().getProgress(adv);
            if (progress.isDone()) {
               System.out.println("[InfTsuk] Advancement '" + advancementId + "' already granted to " + player.getName());
               return;
            }

            for(String criterion : progress.getRemaningCriteria()) {
               player.getAdvancements().grantCriterion(adv, criterion);
            }

            System.out.println("[InfTsuk] Granted advancement '" + advancementId + "' to " + player.getName());
         } else {
            System.err.println("[InfTsuk] WARNING: Advancement '" + advancementId + "' not found in AdvancementManager for " + player.getName() + "! Check assets/inftsukaddon/advancements/ JSON files.");
         }
      } catch (Exception e) {
         System.err.println("[InfTsuk] Failed to grant advancement '" + advancementId + "': " + e.getMessage());
      }

   }

   public boolean hasCompletedQuestForAdvancement(UUID playerId, String advancementId) {
      Set<String> completed = (Set)this.completedQuests.getOrDefault(playerId, Collections.emptySet());
      switch (advancementId) {
         case "inftsukaddon:becomegenin":
            return completed.contains("bell_test");
         case "inftsukaddon:landofwaves":
            return completed.contains("land_of_waves_end");
         case "inftsukaddon:chuninexams":
            return completed.contains("preliminary_victory");
         case "inftsukaddon:arc3_complete":
            return completed.contains("hiruzen_death");
         case "inftsukaddon:shinobi_way":
            return completed.contains("surge_of_rebirth");
         case "inftsukaddon:arc6_complete":
            return completed.contains("resolve");
         case "inftsukaddon:arc7_complete":
            return completed.contains("kaze_resolution");
         case "inftsukaddon:arc8_complete":
            return completed.contains("tenchi_resolution");
         case "inftsukaddon:arc9_complete":
            return completed.contains("immortal_duo_conclusion");
         default:
            return false;
      }
   }

   public void handleAdvancementSkips(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      QuestSavedData savedData = QuestSavedData.get(player.world);
      Set<String> alreadyCompleted = (Set)this.completedQuests.getOrDefault(playerId, new HashSet());
      if (alreadyCompleted.contains("hiruzen_death") && !this.hasAdvancement(player, "inftsukaddon:arc3_complete")) {
         this.grantAdvancement(player, "inftsukaddon:arc3_complete");
         player.sendMessage(new TextComponentString("§a[Quest] Konoha Crush advancement restored! Clan tab is now unlocked."));
      }

      int skipUpToArc = 0;
      if (this.hasAdvancement(player, "inftsukaddon:arc3_complete")) {
         skipUpToArc = 4;
      } else if (this.hasAdvancement(player, "inftsukaddon:chuninexams")) {
         skipUpToArc = 3;
      } else if (this.hasAdvancement(player, "inftsukaddon:landofwaves")) {
         skipUpToArc = 2;
      } else if (this.hasAdvancement(player, "inftsukaddon:becomegenin")) {
         skipUpToArc = 1;
      }

      if (skipUpToArc > 0) {
         boolean anySkipped = false;

         for(QuestDefinition def : QuestRegistry.getAll()) {
            if (!def.isRepeatable() && "leaf_story".equals(def.getStoryline()) && def.getArcNumber() > 0 && def.getArcNumber() <= skipUpToArc && !alreadyCompleted.contains(def.getId())) {
               ((Set)this.completedQuests.computeIfAbsent(playerId, (k) -> new HashSet())).add(def.getId());
               savedData.completeQuest(playerId, def.getId());
               anySkipped = true;
            }
         }

         if (anySkipped) {
            player.sendMessage(new TextComponentString("§a[Quest] Previous story quests auto-completed based on your advancement progress."));
         }
      }

      if (this.hasAdvancement(player, "inftsukaddon:shinobi_way")) {
         boolean anySkipped = false;

         for(QuestDefinition def : QuestRegistry.getAll()) {
            if (!def.isRepeatable() && "shinobi_way".equals(def.getStoryline()) && !alreadyCompleted.contains(def.getId())) {
               ((Set)this.completedQuests.computeIfAbsent(playerId, (k) -> new HashSet())).add(def.getId());
               savedData.completeQuest(playerId, def.getId());
               anySkipped = true;
            }
         }

         if (anySkipped) {
            player.sendMessage(new TextComponentString("§a[Quest] Your Shinobi Way quests auto-completed based on your advancement."));
         }
      }

   }
}
