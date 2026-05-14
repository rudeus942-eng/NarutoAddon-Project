
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class PvpTrackerOverlay {
   private static final int MARGIN_LEFT = 5;
   private static final int MARGIN_TOP = 5;
   private static final int MAX_TARGETS = 3;
   private static final int COLOR_RED = -48060;
   private static final int COLOR_ORANGE = -30652;
   private static final int COLOR_PURPLE = -5618433;
   private static final int COLOR_YELLOW = -171;
   private static final int COLOR_WHITE = -1;
   private static final int BG_COLOR = -2013265920;
   private static final int BORDER_COLOR = -12316399;
   private static final int BOX_WIDTH = 120;
   private static final int PADDING = 4;
   private static final int BOX_SPACING = 3;
   private static final String[] PVP_SLOT_PRIORITY = new String[]{"pvp_daily_0", "pvp_daily_1", "pvp_daily_2", "pvp_weekly_0", "pvp_weekly_1", "pvp_weekly_2", "pvp_random"};

   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
      if (event.getType() == ElementType.ALL) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.currentScreen == null) {
            EntityPlayer player = mc.player;
            if (player != null) {
               List<PvpClientData.TargetPosInfo> targets = PvpClientData.getTargetPositions();
               Map<String, PvpClientData.ActivePvpMissionInfo> missions = PvpClientData.getAllActiveMissions();
               if (!targets.isEmpty() || !missions.isEmpty()) {
                  GlStateManager.pushMatrix();
                  GlStateManager.enableBlend();
                  int currentY = 5;
                  int drawn = 0;

                  for(PvpClientData.TargetPosInfo target : targets) {
                     if (drawn >= 3) {
                        break;
                     }

                     double dx = (double)target.x + (double)0.5F - player.posX;
                     double dz = (double)target.z + (double)0.5F - player.posZ;
                     int distance = (int)Math.sqrt(dx * dx + dz * dz);
                     int fuzzyDist = (distance + 25) / 50 * 50;
                     float angleToTarget = (float)(Math.atan2(dz, dx) * (double)180.0F / Math.PI) - 90.0F;
                     float playerYaw = player.rotationYaw % 360.0F;

                     float relativeAngle;
                     for(relativeAngle = angleToTarget - playerYaw; relativeAngle > 180.0F; relativeAngle -= 360.0F) {
                     }

                     while(relativeAngle < -180.0F) {
                        relativeAngle += 360.0F;
                     }

                     String arrow;
                     if (!(relativeAngle > 150.0F) && !(relativeAngle < -150.0F)) {
                        if (relativeAngle > 30.0F) {
                           arrow = "→";
                        } else if (relativeAngle < -30.0F) {
                           arrow = "�?";
                        } else {
                           arrow = "↑";
                        }
                     } else {
                        arrow = "↓";
                     }

                     int color = getBeaconColor(target.beaconType);
                     String label = getBeaconLabel(target.beaconType);
                     int lineHeight = 10;
                     int boxHeight = lineHeight * 2 + 8;
                     drawRect(4, currentY - 1, 126, currentY + boxHeight + 1, -12316399);
                     drawRect(5, currentY, 125, currentY + boxHeight, -2013265920);
                     drawRect(5, currentY, 7, currentY + boxHeight, color);
                     int textX = 11;
                     int textY = currentY + 4;
                     float scale = 2.0F;
                     GlStateManager.pushMatrix();
                     GlStateManager.translate((float)textX, (float)textY, 0.0F);
                     GlStateManager.scale(scale, scale, 1.0F);
                     mc.fontRenderer.drawStringWithShadow(arrow, 0.0F, 0.0F, color);
                     GlStateManager.popMatrix();
                     int arrowOffset = (int)((float)mc.fontRenderer.getStringWidth(arrow) * scale) + 4;
                     String distText = label + " ~" + fuzzyDist + "m";
                     mc.fontRenderer.drawStringWithShadow(distText, (float)(textX + arrowOffset), (float)(textY + 4), color);
                     currentY += boxHeight + 3;
                     ++drawn;
                  }

                  for(String slot : PVP_SLOT_PRIORITY) {
                     PvpClientData.ActivePvpMissionInfo mission = (PvpClientData.ActivePvpMissionInfo)missions.get(slot);
                     if (mission != null && mission.isTimeLimited()) {
                        long elapsed = System.currentTimeMillis() - mission.startTimeMs;
                        long remaining = mission.timeLimitMs - elapsed;
                        if (remaining > 0L) {
                           int totalSeconds = (int)(remaining / 1000L);
                           int minutes = totalSeconds / 60;
                           int seconds = totalSeconds % 60;
                           String timerText = "SURVIVE: " + minutes + ":" + String.format("%02d", seconds);
                           int lineHeight = 10;
                           int boxHeight = lineHeight + 8;
                           drawRect(4, currentY - 1, 126, currentY + boxHeight + 1, -12316399);
                           drawRect(5, currentY, 125, currentY + boxHeight, -2013265920);
                           drawRect(5, currentY, 7, currentY + boxHeight, -171);
                           mc.fontRenderer.drawStringWithShadow(timerText, 11.0F, (float)(currentY + 4), -171);
                           int var10000 = currentY + boxHeight + 3;
                           break;
                        }
                     }
                  }

                  GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                  GlStateManager.disableBlend();
                  GlStateManager.popMatrix();
               }
            }
         }
      }
   }

   private static int getBeaconColor(int beaconType) {
      switch (beaconType) {
         case 0:
            return -48060;
         case 1:
            return -30652;
         case 2:
            return -5618433;
         default:
            return -48060;
      }
   }

   private static String getBeaconLabel(int beaconType) {
      switch (beaconType) {
         case 0:
            return "TARGET";
         case 1:
            return "BINGO";
         case 2:
            return "MUTUAL";
         default:
            return "TARGET";
      }
   }

   private static void drawRect(int left, int top, int right, int bottom, int color) {
      Gui.drawRect(left, top, right, bottom, color);
   }
}
