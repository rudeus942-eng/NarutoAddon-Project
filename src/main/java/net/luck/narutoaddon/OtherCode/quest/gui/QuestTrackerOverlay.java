
package net.luck.narutoaddon.OtherCode.quest.gui;

import net.luck.narutoaddon.OtherCode.akatsuki.network.AkatsukiClientData;
import net.luck.narutoaddon.OtherCode.quest.network.QuestClientData;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class QuestTrackerOverlay {
   private static final int BG_COLOR = -2013265920;
   private static final int COLOR_GOLD = -22016;
   private static final int COLOR_WHITE = -1;
   private static final int COLOR_GRAY = -7829368;
   private static final int COLOR_YELLOW = -171;
   private static final int BORDER_COLOR = -11193600;
   private static final int PADDING = 4;
   private static final int MARGIN_RIGHT = 5;
   private static final int MARGIN_TOP = 5;
   private static final int MAX_BOX_WIDTH = 140;
   private static final int BOX_SPACING = 3;
   private static final int COMPACT_LINE_HEIGHT = 14;
   private static final int COMPACT_THRESHOLD = 3;
   public static int lastOverlayHeight = 0;
   private static final String[] SLOT_PRIORITY = new String[]{"daily_0", "daily_1", "daily_2", "weekly_0", "weekly_1", "weekly_2", "random", "outpost_mission", "bingo_hunt", "incursion_event", "contract_mission"};
   private static final String[] PVP_SLOT_PRIORITY = new String[]{"pvp_daily_0", "pvp_daily_1", "pvp_daily_2", "pvp_weekly_0", "pvp_weekly_1", "pvp_weekly_2", "pvp_random"};
   private static final int COLOR_AK_CRIMSON = -3399134;
   private static final int COLOR_AK_BORDER = -11202287;

   private static int getSlotColor(String slot) {
      if (QuestClientData.isStorySlot(slot)) {
         return -22016;
      } else if (QuestClientData.isOutpostSlot(slot)) {
         return -43691;
      } else if (QuestClientData.isBingoSlot(slot)) {
         return -30669;
      } else if (QuestClientData.isIncursionSlot(slot)) {
         return -11158563;
      } else if (QuestClientData.isContractSlot(slot)) {
         return -8875;
      } else if (slot.startsWith("daily")) {
         return -11162881;
      } else if (slot.startsWith("weekly")) {
         return -5614081;
      } else {
         return slot.equals("random") ? -21931 : -22016;
      }
   }

   private static int getPvpSlotColor(String slot) {
      if (slot.startsWith("pvp_daily")) {
         return -48060;
      } else if (slot.startsWith("pvp_weekly")) {
         return -3399134;
      } else {
         return slot.equals("pvp_random") ? -39356 : -48060;
      }
   }

   private static String getSlotTag(String slot) {
      if (QuestClientData.isStorySlot(slot)) {
         return "§6S";
      } else if (QuestClientData.isOutpostSlot(slot)) {
         return "§c⚔";
      } else if (QuestClientData.isBingoSlot(slot)) {
         return "§6B";
      } else if (QuestClientData.isContractSlot(slot)) {
         return "§e☠";
      } else if (slot.startsWith("daily")) {
         return "§bD";
      } else if (slot.startsWith("weekly")) {
         return "§dW";
      } else {
         return slot.equals("random") ? "§6R" : "?";
      }
   }

   private static String getPvpSlotTag(String slot) {
      if (slot.startsWith("pvp_daily")) {
         return "§cD";
      } else if (slot.startsWith("pvp_weekly")) {
         return "§4W";
      } else {
         return slot.equals("pvp_random") ? "§6R" : "?";
      }
   }

   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
      if (event.getType() == ElementType.ALL) {
         Map<String, QuestClientData.ActiveQuestState> allPve = QuestClientData.getAllActiveQuests();
         Map<String, PvpClientData.ActivePvpMissionInfo> allPvp = PvpClientData.getAllActiveMissions();
         List<AkatsukiClientData.MissionClientEntry> akMissions = AkatsukiClientData.activeMissions;
         boolean hasPve = !allPve.isEmpty();
         boolean hasPvp = !allPvp.isEmpty();
         boolean hasAk = akMissions != null && !akMissions.isEmpty();
         if (!hasPve && !hasPvp && !hasAk) {
            lastOverlayHeight = 0;
         } else {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen == null) {
               ScaledResolution sr = new ScaledResolution(mc);
               int screenWidth = sr.getScaledWidth();
               int akCount = 0;
               if (hasAk) {
                  for(AkatsukiClientData.MissionClientEntry m : akMissions) {
                     if (!m.completed) {
                        ++akCount;
                     }
                  }
               }

               int totalActive = allPve.size() + allPvp.size() + akCount;
               boolean compact = totalActive >= 3;
               GlStateManager.pushMatrix();
               GlStateManager.enableBlend();
               int boxX = screenWidth - 140 - 5;
               int currentY = 5;

               for(Map.Entry<String, QuestClientData.ActiveQuestState> entry : allPve.entrySet()) {
                  if (QuestClientData.isStorySlot((String)entry.getKey())) {
                     QuestClientData.ActiveQuestState aq = (QuestClientData.ActiveQuestState)entry.getValue();
                     currentY = this.drawQuestBox(mc, boxX, currentY, aq, "story", getSlotColor((String)entry.getKey()));
                     currentY += 3;
                  }
               }

               for(String slot : SLOT_PRIORITY) {
                  QuestClientData.ActiveQuestState aq = (QuestClientData.ActiveQuestState)allPve.get(slot);
                  if (aq != null) {
                     if (!compact) {
                        currentY = this.drawQuestBox(mc, boxX, currentY, aq, QuestClientData.getBaseCategory(slot), getSlotColor(slot));
                     } else {
                        currentY = this.drawCompactPveLine(mc, boxX, currentY, aq, slot, getSlotColor(slot));
                     }

                     currentY += 3;
                  }
               }

               for(String pvpSlot : PVP_SLOT_PRIORITY) {
                  PvpClientData.ActivePvpMissionInfo pvpMission = (PvpClientData.ActivePvpMissionInfo)allPvp.get(pvpSlot);
                  if (pvpMission != null) {
                     if (!compact) {
                        currentY = this.drawPvpBox(mc, boxX, currentY, pvpMission, pvpSlot, getPvpSlotColor(pvpSlot));
                     } else {
                        currentY = this.drawCompactPvpLine(mc, boxX, currentY, pvpMission, pvpSlot, getPvpSlotColor(pvpSlot));
                     }

                     currentY += 3;
                  }
               }

               if (hasAk) {
                  for(AkatsukiClientData.MissionClientEntry m : akMissions) {
                     if (!m.completed) {
                        if (!compact) {
                           currentY = this.drawAkatsukiBox(mc, boxX, currentY, m);
                        } else {
                           currentY = this.drawCompactAkatsukiLine(mc, boxX, currentY, m);
                        }

                        currentY += 3;
                     }
                  }
               }

               lastOverlayHeight = currentY - 5;
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               GlStateManager.disableBlend();
               GlStateManager.popMatrix();
            }
         }
      }
   }

   private int drawQuestBox(Minecraft mc, int boxX, int boxY, QuestClientData.ActiveQuestState aq, String slot, int slotColor) {
      String questName = aq.questName;
      String stepDesc = aq.stepDesc;
      int textAreaWidth = 132;
      if (questName != null && mc.fontRenderer.getStringWidth(questName) > textAreaWidth) {
         while(questName.length() > 3 && mc.fontRenderer.getStringWidth(questName + "..") > textAreaWidth) {
            questName = questName.substring(0, questName.length() - 1);
         }

         questName = questName + "..";
      }

      List<String> descLines = null;
      if (stepDesc != null) {
         descLines = mc.fontRenderer.listFormattedStringToWidth(stepDesc, textAreaWidth);
         if (descLines.size() > 2) {
            descLines = descLines.subList(0, 2);
            String lastLine = (String)descLines.get(1);
            if (lastLine.length() > 3) {
               descLines.set(1, lastLine.substring(0, lastLine.length() - 2) + "..");
            }
         }
      }

      int lineHeight = 10;
      int lines = 1;
      if (descLines != null) {
         lines += descLines.size();
      }

      boolean showKills = aq.killsRequired > 0;
      if (showKills) {
         ++lines;
      }

      boolean isStory = "story".equals(slot);
      if (isStory) {
         ++lines;
      }

      int boxHeight = lines * lineHeight + 8;
      int boxWidth = 140;
      drawRect(boxX - 1, boxY - 1, boxX + boxWidth + 1, boxY + boxHeight + 1, -11193600);
      drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, -2013265920);
      drawRect(boxX, boxY, boxX + boxWidth, boxY + 1, slotColor);
      int textX = boxX + 4;
      int textY = boxY + 4;
      mc.fontRenderer.drawStringWithShadow(questName != null ? questName : "", (float)textX, (float)textY, -22016);
      textY += lineHeight;
      if (descLines != null) {
         for(String line : descLines) {
            mc.fontRenderer.drawStringWithShadow(line, (float)textX, (float)textY, -1);
            textY += lineHeight;
         }
      }

      if (isStory) {
         String stepProgress = "Step " + (aq.stepIndex + 1) + "/" + aq.totalSteps;
         mc.fontRenderer.drawStringWithShadow(stepProgress, (float)textX, (float)textY, -7829368);
         textY += lineHeight;
      }

      if (showKills) {
         String killText = "Defeated " + aq.killProgress + "/" + aq.killsRequired;
         mc.fontRenderer.drawStringWithShadow(killText, (float)textX, (float)textY, -171);
      }

      return boxY + boxHeight;
   }

   private int drawPvpBox(Minecraft mc, int boxX, int boxY, PvpClientData.ActivePvpMissionInfo mission, String slot, int slotColor) {
      String missionName = mission.name;
      int textAreaWidth = 132;
      if (missionName != null && mc.fontRenderer.getStringWidth(missionName) > textAreaWidth) {
         while(missionName.length() > 3 && mc.fontRenderer.getStringWidth(missionName + "..") > textAreaWidth) {
            missionName = missionName.substring(0, missionName.length() - 1);
         }

         missionName = missionName + "..";
      }

      List<String> descLines = null;
      if (mission.description != null) {
         descLines = mc.fontRenderer.listFormattedStringToWidth(mission.description, textAreaWidth);
         if (descLines.size() > 2) {
            descLines = descLines.subList(0, 2);
            String lastLine = (String)descLines.get(1);
            if (lastLine.length() > 3) {
               descLines.set(1, lastLine.substring(0, lastLine.length() - 2) + "..");
            }
         }
      }

      int lineHeight = 10;
      int lines = 1;
      if (descLines != null) {
         lines += descLines.size();
      }

      boolean showKills = mission.killsRequired > 0;
      if (showKills) {
         ++lines;
      }

      boolean showTimer = mission.isTimeLimited();
      if (showTimer) {
         ++lines;
      }

      int boxHeight = lines * lineHeight + 8;
      int boxWidth = 140;
      drawRect(boxX - 1, boxY - 1, boxX + boxWidth + 1, boxY + boxHeight + 1, -13430511);
      drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, -2013265920);
      drawRect(boxX, boxY, boxX + boxWidth, boxY + 2, slotColor);
      int textX = boxX + 4;
      int textY = boxY + 4;
      mc.fontRenderer.drawStringWithShadow(missionName != null ? missionName : "", (float)textX, (float)textY, -39322);
      textY += lineHeight;
      if (descLines != null) {
         for(String line : descLines) {
            mc.fontRenderer.drawStringWithShadow(line, (float)textX, (float)textY, -1);
            textY += lineHeight;
         }
      }

      if (showKills) {
         String killText = "Kills " + mission.progress + "/" + mission.killsRequired;
         mc.fontRenderer.drawStringWithShadow(killText, (float)textX, (float)textY, -171);
         textY += lineHeight;
      }

      if (showTimer) {
         long elapsed = System.currentTimeMillis() - mission.startTimeMs;
         long remaining = mission.timeLimitMs - elapsed;
         if (remaining < 0L) {
            remaining = 0L;
         }

         int secs = (int)(remaining / 1000L);
         int mins = secs / 60;
         secs %= 60;
         String timerText = String.format("Time: %d:%02d", mins, secs);
         mc.fontRenderer.drawStringWithShadow(timerText, (float)textX, (float)textY, -30584);
      }

      return boxY + boxHeight;
   }

   private int drawCompactPveLine(Minecraft mc, int boxX, int boxY, QuestClientData.ActiveQuestState aq, String slot, int slotColor) {
      int boxWidth = 140;
      int boxHeight = 14;
      drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, -2013265920);
      drawRect(boxX, boxY, boxX + 2, boxY + boxHeight, slotColor);
      int textX = boxX + 5;
      int textY = boxY + 3;
      String tag = getSlotTag(slot);
      String progress = "";
      if (aq.killsRequired > 0) {
         progress = " §e" + aq.killProgress + "/" + aq.killsRequired;
      } else if ("story".equals(QuestClientData.getBaseCategory(slot))) {
         progress = " §7" + (aq.stepIndex + 1) + "/" + aq.totalSteps;
      }

      String nameAndProgress = tag + " §r";
      int availableWidth = boxWidth - 8 - mc.fontRenderer.getStringWidth(tag + " ") - mc.fontRenderer.getStringWidth(progress);
      String name = aq.questName != null ? aq.questName : "";
      if (mc.fontRenderer.getStringWidth(name) > availableWidth) {
         while(name.length() > 2 && mc.fontRenderer.getStringWidth(name + "..") > availableWidth) {
            name = name.substring(0, name.length() - 1);
         }

         name = name + "..";
      }

      mc.fontRenderer.drawStringWithShadow(nameAndProgress + name + progress, (float)textX, (float)textY, -1);
      return boxY + boxHeight;
   }

   private int drawCompactPvpLine(Minecraft mc, int boxX, int boxY, PvpClientData.ActivePvpMissionInfo mission, String slot, int slotColor) {
      int boxWidth = 140;
      int boxHeight = 14;
      drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, -2013265920);
      drawRect(boxX, boxY, boxX + 2, boxY + boxHeight, slotColor);
      int textX = boxX + 5;
      int textY = boxY + 3;
      String tag = getPvpSlotTag(slot);
      String suffix = "";
      if (mission.killsRequired > 0) {
         suffix = " §e" + mission.progress + "/" + mission.killsRequired;
      }

      if (mission.isTimeLimited()) {
         long elapsed = System.currentTimeMillis() - mission.startTimeMs;
         long remaining = mission.timeLimitMs - elapsed;
         if (remaining < 0L) {
            remaining = 0L;
         }

         int secs = (int)(remaining / 1000L);
         int mins = secs / 60;
         secs %= 60;
         suffix = suffix + " §c" + String.format("%d:%02d", mins, secs);
      }

      String nameAndTag = tag + " §r";
      int availableWidth = boxWidth - 8 - mc.fontRenderer.getStringWidth(tag + " ") - mc.fontRenderer.getStringWidth(suffix);
      String name = mission.name != null ? mission.name : "";
      if (mc.fontRenderer.getStringWidth(name) > availableWidth) {
         while(name.length() > 2 && mc.fontRenderer.getStringWidth(name + "..") > availableWidth) {
            name = name.substring(0, name.length() - 1);
         }

         name = name + "..";
      }

      mc.fontRenderer.drawStringWithShadow(nameAndTag + name + suffix, (float)textX, (float)textY, -1);
      return boxY + boxHeight;
   }

   private int drawAkatsukiBox(Minecraft mc, int boxX, int boxY, AkatsukiClientData.MissionClientEntry m) {
      String missionName = m.templateName;
      int textAreaWidth = 132;
      if (missionName != null && mc.fontRenderer.getStringWidth(missionName) > textAreaWidth) {
         while(missionName.length() > 3 && mc.fontRenderer.getStringWidth(missionName + "..") > textAreaWidth) {
            missionName = missionName.substring(0, missionName.length() - 1);
         }

         missionName = missionName + "..";
      }

      int lineHeight = 10;
      int lines = 1;
      if (m.objectiveText != null && !m.objectiveText.isEmpty()) {
         ++lines;
      }

      if (m.killsRequired > 0) {
         ++lines;
      }

      int boxHeight = lines * lineHeight + 8;
      int boxWidth = 140;
      drawRect(boxX - 1, boxY - 1, boxX + boxWidth + 1, boxY + boxHeight + 1, -11202287);
      drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, -2013265920);
      drawRect(boxX, boxY, boxX + boxWidth, boxY + 2, -3399134);
      int textX = boxX + 4;
      int textY = boxY + 4;
      mc.fontRenderer.drawStringWithShadow("§cAK §r" + (missionName != null ? missionName : ""), (float)textX, (float)textY, -3399134);
      textY += lineHeight;
      if (m.objectiveText != null && !m.objectiveText.isEmpty()) {
         String obj = m.objectiveText;
         if (mc.fontRenderer.getStringWidth(obj) > textAreaWidth) {
            while(obj.length() > 3 && mc.fontRenderer.getStringWidth(obj + "..") > textAreaWidth) {
               obj = obj.substring(0, obj.length() - 1);
            }

            obj = obj + "..";
         }

         mc.fontRenderer.drawStringWithShadow(obj, (float)textX, (float)textY, -1);
         textY += lineHeight;
      }

      if (m.killsRequired > 0) {
         String killText = "Targets " + m.killsAchieved + "/" + m.killsRequired;
         mc.fontRenderer.drawStringWithShadow(killText, (float)textX, (float)textY, -171);
      }

      return boxY + boxHeight;
   }

   private int drawCompactAkatsukiLine(Minecraft mc, int boxX, int boxY, AkatsukiClientData.MissionClientEntry m) {
      int boxWidth = 140;
      int boxHeight = 14;
      drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, -2013265920);
      drawRect(boxX, boxY, boxX + 2, boxY + boxHeight, -3399134);
      int textX = boxX + 5;
      int textY = boxY + 3;
      String tag = "§cAK";
      String suffix = "";
      if (m.killsRequired > 0) {
         suffix = " §e" + m.killsAchieved + "/" + m.killsRequired;
      }

      String nameAndTag = tag + " §r";
      int availableWidth = boxWidth - 8 - mc.fontRenderer.getStringWidth(tag + " ") - mc.fontRenderer.getStringWidth(suffix);
      String name = m.templateName != null ? m.templateName : "";
      if (mc.fontRenderer.getStringWidth(name) > availableWidth) {
         while(name.length() > 2 && mc.fontRenderer.getStringWidth(name + "..") > availableWidth) {
            name = name.substring(0, name.length() - 1);
         }

         name = name + "..";
      }

      mc.fontRenderer.drawStringWithShadow(nameAndTag + name + suffix, (float)textX, (float)textY, -1);
      return boxY + boxHeight;
   }

   private static void drawRect(int left, int top, int right, int bottom, int color) {
      Gui.drawRect(left, top, right, bottom, color);
   }
}
