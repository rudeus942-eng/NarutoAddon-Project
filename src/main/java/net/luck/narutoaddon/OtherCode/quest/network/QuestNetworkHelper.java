
package net.luck.narutoaddon.OtherCode.quest.network;

import net.luck.narutoaddon.OtherCode.quest.core.*;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointData;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class QuestNetworkHelper {
   public static void sendQuestSync(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      QuestManager manager = QuestManager.getInstance();
      Map<String, QuestInstance> activeSlots = manager.getActiveQuests(playerId);
      List<QuestSyncMessage.ActiveQuestInfo> activeInfos = new ArrayList();
      Set<String> activeQuestIds = new HashSet();

      for(Map.Entry<String, QuestInstance> slotEntry : activeSlots.entrySet()) {
         QuestInstance active = (QuestInstance)slotEntry.getValue();
         if (active.getState() == QuestInstance.QuestState.ACTIVE) {
            activeQuestIds.add(active.getQuestId());
            QuestSyncMessage.ActiveQuestInfo info = new QuestSyncMessage.ActiveQuestInfo();
            info.questId = active.getQuestId();
            info.stepIndex = active.getCurrentStepIndex();
            info.state = (byte)active.getState().ordinal();
            info.killProgress = active.getKillProgress();
            info.category = slotToCategoryByte((String)slotEntry.getKey());
            if (QuestManager.isStorySlot((String)slotEntry.getKey())) {
               info.slotKey = (String)slotEntry.getKey();
            }

            if (active.isGenerated()) {
               info.questName = active.getGeneratedName() != null ? active.getGeneratedName() : active.getQuestId();
               info.totalSteps = active.getGeneratedStepCount();
               QuestStep step = active.getCurrentStep();
               info.stepDesc = step != null ? step.description : (active.getGeneratedDescription() != null ? active.getGeneratedDescription() : "");
               info.killsRequired = step != null && step.isKillCount() ? step.killsRequired : 0;
               int genXp = active.getGeneratedXpReward();
               info.rewardSummary = genXp > 0 ? "§e" + genXp + " Ninja XP" : "";
            } else {
               QuestDefinition def = QuestRegistry.getById(active.getQuestId());
               info.questName = def != null ? def.getName() : active.getQuestId();
               info.totalSteps = def != null ? def.getStepCount() : 0;
               QuestStep step = active.getCurrentStep();
               info.stepDesc = step != null ? step.description : "";
               info.killsRequired = step != null && step.isKillCount() ? step.killsRequired : 0;
               info.rewardSummary = def != null ? buildRewardSummary(def) : "";
            }

            activeInfos.add(info);
         }
      }

      QuestSavedData savedData = QuestSavedData.get(player.world);
      Set<String> completed = savedData.getCompletedQuests(playerId);
      int highestCompletedArc = 0;

      for(String cqId : completed) {
         QuestDefinition cqDef = QuestRegistry.getById(cqId);
         if (cqDef != null && cqDef.getArcNumber() != 99) {
            highestCompletedArc = Math.max(highestCompletedArc, cqDef.getArcNumber());
         }
      }

      List<QuestSyncMessage.QuestEntry> entries = new ArrayList();

      for(QuestDefinition def : QuestRegistry.getAll()) {
         if (!activeQuestIds.contains(def.getId())) {
            byte category = getCategoryByte(def);
            byte rank = def.getRank() != null ? (byte)def.getRank().ordinal() : 0;
            if (def.isRepeatable()) {
               boolean onCooldown = savedData.isRepeatableOnCooldown(playerId, def.getId());
               long cooldownRemaining = onCooldown ? savedData.getRepeatableCooldownRemaining(playerId, def.getId()) : 0L;
               entries.add(new QuestSyncMessage.QuestEntry(def.getId(), def.getName(), def.getDescription(), def.getArcNumber(), !onCooldown, category, cooldownRemaining, rank, def.getStoryline(), buildRewardSummary(def)));
            } else {
               boolean isCompleted = completed.contains(def.getId());
               int questArc = def.getArcNumber();
               if (isCompleted || questArc <= 0 || questArc == 99 || questArc >= highestCompletedArc) {
                  boolean available = !isCompleted;
                  if (available && def.hasRequiredAdvancement()) {
                     boolean advDone = false;

                     try {
                        Advancement adv = player.getServer().getAdvancementManager().getAdvancement(new ResourceLocation(def.getRequiredAdvancement()));
                        advDone = adv != null && player.getAdvancements().getProgress(adv).isDone();
                     } catch (Exception var24) {
                     }

                     boolean questDone = QuestManager.getInstance().hasCompletedQuestForAdvancement(player.getUniqueID(), def.getRequiredAdvancement());
                     if (!advDone && !questDone) {
                        available = false;
                     }
                  }

                  for(String prereq : def.getPrerequisites()) {
                     if (!completed.contains(prereq)) {
                        available = false;
                        break;
                     }
                  }

                  entries.add(new QuestSyncMessage.QuestEntry(def.getId(), def.getName(), def.getDescription(), def.getArcNumber(), available, category, 0L, rank, def.getStoryline(), buildRewardSummary(def)));
               }
            }
         }
      }

      List<String> completedIds = new ArrayList(completed);
      List<QuestSyncMessage.OfferInfo> offerInfos = new ArrayList();
      String[] allSubSlots = new String[]{"daily_0", "daily_1", "daily_2", "weekly_0", "weekly_1", "weekly_2", "random"};

      for(String subSlot : allSubSlots) {
         byte slotByte = QuestSyncMessage.subSlotToByte(subSlot);
         if (!activeSlots.containsKey(subSlot)) {
            String baseCategory = QuestManager.getBaseCategory(subSlot);
            int rerolls = savedData.getRerollsRemaining(playerId, baseCategory);
            long cooldownRemaining = savedData.getSlotCooldownRemaining(playerId, subSlot);
            if (cooldownRemaining > 0L) {
               QuestSyncMessage.OfferInfo oi = new QuestSyncMessage.OfferInfo();
               oi.slotType = slotByte;
               oi.name = "";
               oi.description = "";
               oi.rank = 0;
               oi.killCount = 0;
               oi.xpReward = 0;
               oi.ryoReward = 0;
               oi.rerollsRemaining = rerolls;
               oi.cooldownRemaining = cooldownRemaining;
               offerInfos.add(oi);
            } else {
               RepeatableQuestGenerator.QuestOffer offer = manager.getCurrentOffer(playerId, subSlot);
               if (offer != null) {
                  QuestSyncMessage.OfferInfo oi = new QuestSyncMessage.OfferInfo();
                  oi.slotType = slotByte;
                  oi.name = offer.name;
                  oi.description = offer.description;
                  oi.rank = (byte)offer.rank.ordinal();
                  oi.killCount = offer.killCount;
                  oi.xpReward = offer.xpReward;
                  oi.ryoReward = offer.ryoReward;
                  oi.rerollsRemaining = rerolls;
                  oi.cooldownRemaining = 0L;
                  offerInfos.add(oi);
               }
            }
         }
      }

      boolean beginnerComplete = savedData.hasFlag(playerId, "beginner_v2_complete");
      QuestSyncMessage msg = new QuestSyncMessage(activeInfos, completed.size(), entries, completedIds, offerInfos, beginnerComplete, false);
      QuestModInit.NETWORK.sendTo(msg, player);
   }

   private static byte slotToCategoryByte(String slot) {
      return QuestSyncMessage.subSlotToByte(slot);
   }

   private static String buildRewardSummary(QuestDefinition def) {
      if (def.getRewards() != null && !def.getRewards().isEmpty()) {
         int totalXp = 0;

         for(QuestReward r : def.getRewards()) {
            if (r.type == QuestReward.RewardType.XP) {
               totalXp += r.xpAmount;
            }
         }

         StringBuilder sb = new StringBuilder();
         if (totalXp > 0) {
            sb.append("§e").append(totalXp).append(" Ninja XP");
         }

         int ryo = RyoRewardHelper.calculateStoryQuestRyo(def);
         if (ryo > 0) {
            if (sb.length() > 0) {
               sb.append("§7, ");
            }

            sb.append("§6").append(ryo).append(" Ryo");
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   private static byte getCategoryByte(QuestDefinition def) {
      switch (def.getRepeatType()) {
         case DAILY:
            return 1;
         case WEEKLY:
            return 2;
         case RANDOM:
            return 3;
         default:
            return 0;
      }
   }

   public static void sendWaypoint(EntityPlayerMP player, String questId, WaypointData waypoint) {
      QuestModInit.NETWORK.sendTo(QuestWaypointMessage.from(questId, waypoint), player);
   }

   public static void sendWaypoint(EntityPlayerMP player, WaypointData waypoint) {
      QuestModInit.NETWORK.sendTo(QuestWaypointMessage.from(waypoint), player);
   }

   public static void clearWaypoint(EntityPlayerMP player, String questId) {
      QuestModInit.NETWORK.sendTo(QuestWaypointMessage.clear(questId), player);
   }

   public static void clearWaypoint(EntityPlayerMP player) {
      QuestModInit.NETWORK.sendTo(QuestWaypointMessage.clear(), player);
   }

   public static void sendDialog(EntityPlayerMP player, QuestStep step, int stepIndex) {
      if (step != null && step.type == QuestStep.StepType.DIALOG) {
         QuestDialogMessage msg = new QuestDialogMessage(step.npcName, step.dialogLines, step.dialogResponses, stepIndex, step.correctResponseIndex);
         QuestModInit.NETWORK.sendTo(msg, player);
      }
   }

   public static void sendDialogWithLines(EntityPlayerMP player, QuestStep step, String[] lines, int stepIndex) {
      if (step != null && step.type == QuestStep.StepType.DIALOG) {
         String[] advanceOnly = step.dialogResponses != null && step.dialogResponses.length > 0 ? new String[]{step.dialogResponses[step.correctResponseIndex]} : step.dialogResponses;
         QuestDialogMessage msg = new QuestDialogMessage(step.npcName, lines, advanceOnly, stepIndex, 0);
         QuestModInit.NETWORK.sendTo(msg, player);
      }
   }

   public static void sendMultiPhaseDialog(EntityPlayerMP player, QuestStep step, QuestInstance quest) {
      if (step != null && step.hasMultiPhaseDialog()) {
         int phaseIdx = quest.getDialogPhaseIndex();
         QuestStep.DialogPhase[] phases = step.dialogPhases;
         if (phaseIdx >= 0 && phaseIdx < phases.length) {
            QuestStep.DialogPhase phase = phases[phaseIdx];
            String[] lines;
            if (phase.resultVariants != null) {
               int correct = quest.getQuizCorrectCount();
               int total = phase.quizQuestionCount;
               if (correct >= total) {
                  lines = phase.resultVariants[0];
               } else if (correct > 0) {
                  lines = phase.resultVariants[1];
               } else {
                  lines = phase.resultVariants[2];
               }
            } else {
               lines = phase.lines;
            }

            int correctIdx = phase.isQuiz ? -1 : phase.correctResponseIndex;
            QuestDialogMessage msg = new QuestDialogMessage(step.npcName, lines, phase.responses, quest.getCurrentStepIndex(), correctIdx);
            QuestModInit.NETWORK.sendTo(msg, player);
         }
      }
   }
}
