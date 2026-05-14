
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.luck.narutoaddon.OtherCode.quest.pvp.PvpClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class WarHudOverlay {
   private static final int COLOR_BORDER = -7706054;
   private static final int COLOR_GOLD = -1521552;
   private static final int COLOR_BEIGE = -2832216;
   private static final int COLOR_WHITE = -1;
   private static final int COLOR_GRAY = -7829368;
   private static final int COLOR_RED = -43691;
   private static final int COLOR_BG = -1442840576;
   private static final int COLOR_BG_LIGHT = 1711276032;
   private static final int COLOR_LEAF = -11141291;
   private static final int COLOR_SAND = -171;
   private static final int COLOR_STONE = -5601195;
   private static final int COLOR_CLOUD = -4486913;
   private static final int COLOR_MIST = -11167318;
   private static final int COLOR_RAIN = -14531414;
   private static final List<KillFeedEntry> killFeed = new ArrayList();
   private static final long KILL_FEED_DURATION_MS = 2000L;
   private static float captureRingProgress = 0.0F;
   private static boolean showCaptureRing = false;
   private static int captureRingColor = -1;

   public static void addKillFeedEntry(String text, int color) {
      killFeed.add(new KillFeedEntry(text, color, System.currentTimeMillis()));
   }

   public static void setCaptureRing(boolean show, float progress, int color) {
      showCaptureRing = show;
      captureRingProgress = progress;
      captureRingColor = color;
   }

   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
      if (event.getType() == ElementType.ALL) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.currentScreen == null) {
            EntityPlayer player = mc.player;
            if (player != null) {
               String playerVillage = PvpClientData.kageVillage;
               if (playerVillage != null && !playerVillage.isEmpty()) {
                  List<PvpClientData.WarClientInfo> wars = PvpClientData.getActiveWars();
                  PvpClientData.WarClientInfo activeWar = null;

                  for(PvpClientData.WarClientInfo war : wars) {
                     if (war.involvesVillage(playerVillage) && war.lobbyState == 3) {
                        activeWar = war;
                        break;
                     }
                  }

                  if (activeWar != null) {
                     ScaledResolution sr = new ScaledResolution(mc);
                     int screenWidth = sr.getScaledWidth();
                     int screenHeight = sr.getScaledHeight();
                     GlStateManager.pushMatrix();
                     GlStateManager.enableBlend();
                     WarMode mode = WarMode.fromOrdinal(activeWar.modeOrdinal);
                     switch (mode) {
                        case SKIRMISH:
                           this.renderSkirmishHud(mc, screenWidth, activeWar);
                           break;
                        case DIVISION:
                           this.renderDivisionHud(mc, screenWidth, activeWar);
                           break;
                        case DOMINATION:
                           this.renderDominationHud(mc, screenWidth, screenHeight, activeWar, player);
                           break;
                        case RUSH:
                           this.renderRushHud(mc, screenWidth, screenHeight, activeWar, player);
                           break;
                        case FOREST_OF_DEATH:
                           this.renderSkirmishHud(mc, screenWidth, activeWar);
                     }

                     if (showCaptureRing && (mode == WarMode.DOMINATION || mode == WarMode.RUSH)) {
                        this.renderCaptureRing(mc, screenWidth, screenHeight);
                     }

                     this.renderKillFeed(mc, screenWidth);
                     GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                     GlStateManager.disableBlend();
                     GlStateManager.popMatrix();
                  }
               }
            }
         }
      }
   }

   private void renderSkirmishHud(Minecraft mc, int screenWidth, PvpClientData.WarClientInfo war) {
      String timeStr = formatTime(war.timeRemainingMs);
      String text = "WAR: " + war.village1 + " " + war.score1 + " vs " + war.score2 + " " + war.village2 + " | " + timeStr;
      int textWidth = mc.fontRenderer.getStringWidth(text);
      int barWidth = textWidth + 16;
      int barHeight = 14;
      int barX = (screenWidth - barWidth) / 2;
      int barY = 2;
      drawRect(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, -7706054);
      drawRect(barX, barY, barX + barWidth, barY + barHeight, -1442840576);
      int x = barX + 8;
      int y = barY + 3;
      String warPrefix = "WAR: ";
      mc.fontRenderer.drawStringWithShadow(warPrefix, (float)x, (float)y, -1521552);
      x += mc.fontRenderer.getStringWidth(warPrefix);
      int v1Color = getVillageColor(war.village1);
      String v1Text = war.village1 + " " + war.score1;
      mc.fontRenderer.drawStringWithShadow(v1Text, (float)x, (float)y, v1Color);
      x += mc.fontRenderer.getStringWidth(v1Text);
      String vs = " vs ";
      mc.fontRenderer.drawStringWithShadow(vs, (float)x, (float)y, -2832216);
      x += mc.fontRenderer.getStringWidth(vs);
      int v2Color = getVillageColor(war.village2);
      String v2Text = war.score2 + " " + war.village2;
      mc.fontRenderer.drawStringWithShadow(v2Text, (float)x, (float)y, v2Color);
      x += mc.fontRenderer.getStringWidth(v2Text);
      String timerPart = " | " + timeStr;
      mc.fontRenderer.drawStringWithShadow(timerPart, (float)x, (float)y, -2832216);
   }

   private void renderDivisionHud(Minecraft mc, int screenWidth, PvpClientData.WarClientInfo war) {
      String roundText = "Round " + war.currentRound + "/3";
      String timeStr = formatTime(war.timeRemainingMs);
      String aliveText = war.village1 + " [" + war.score1 + " alive] vs [" + war.score2 + " alive] " + war.village2;
      String winsText = "Wins: " + war.roundsWon1 + " - " + war.roundsWon2;
      String topLine = roundText + " | " + timeStr;
      int topWidth = mc.fontRenderer.getStringWidth(topLine) + 16;
      int barWidth = Math.max(topWidth, mc.fontRenderer.getStringWidth(aliveText) + 16);
      int barHeight = 26;
      int barX = (screenWidth - barWidth) / 2;
      int barY = 2;
      drawRect(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, -7706054);
      drawRect(barX, barY, barX + barWidth, barY + barHeight, -1442840576);
      int topX = barX + (barWidth - mc.fontRenderer.getStringWidth(topLine)) / 2;
      mc.fontRenderer.drawStringWithShadow(roundText, (float)topX, (float)(barY + 3), -1521552);
      mc.fontRenderer.drawStringWithShadow(" | " + timeStr, (float)(topX + mc.fontRenderer.getStringWidth(roundText)), (float)(barY + 3), -2832216);
      int aliveWidth = mc.fontRenderer.getStringWidth(aliveText);
      int aliveX = barX + (barWidth - aliveWidth) / 2;
      int aliveY = barY + 14;
      int v1Color = getVillageColor(war.village1);
      int v2Color = getVillageColor(war.village2);
      String v1Part = war.village1 + " [" + war.score1 + " alive]";
      mc.fontRenderer.drawStringWithShadow(v1Part, (float)aliveX, (float)aliveY, v1Color);
      int ax = aliveX + mc.fontRenderer.getStringWidth(v1Part);
      mc.fontRenderer.drawStringWithShadow(" vs ", (float)ax, (float)aliveY, -2832216);
      ax += mc.fontRenderer.getStringWidth(" vs ");
      String v2Part = "[" + war.score2 + " alive] " + war.village2;
      mc.fontRenderer.drawStringWithShadow(v2Part, (float)ax, (float)aliveY, v2Color);
      int winsWidth = mc.fontRenderer.getStringWidth(winsText);
      int winsX = (screenWidth - winsWidth) / 2;
      mc.fontRenderer.drawStringWithShadow(winsText, (float)winsX, (float)(barY + barHeight + 3), -7829368);
   }

   private void renderDominationHud(Minecraft mc, int screenWidth, int screenHeight, PvpClientData.WarClientInfo war, EntityPlayer player) {
      List<PvpClientData.ZoneClientInfo> zones = PvpClientData.getWarZones();
      String timeStr = formatTime(war.timeRemainingMs);
      int zoneBoxSize = 14;
      int zoneSpacing = 4;
      int zonesWidth = zones.size() * (zoneBoxSize + zoneSpacing);
      String scoreText = war.score1 + " vs " + war.score2;
      int scoreWidth = mc.fontRenderer.getStringWidth(scoreText);
      int timeWidth = mc.fontRenderer.getStringWidth(timeStr);
      int totalInner = zonesWidth + 8 + scoreWidth + 8 + timeWidth;
      int barWidth = totalInner + 16;
      int barHeight = 18;
      int barX = (screenWidth - barWidth) / 2;
      int barY = 2;
      drawRect(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, -7706054);
      drawRect(barX, barY, barX + barWidth, barY + barHeight, -1442840576);
      int x = barX + 8;
      int y = barY + 2;

      for(int i = 0; i < zones.size(); ++i) {
         PvpClientData.ZoneClientInfo zone = (PvpClientData.ZoneClientInfo)zones.get(i);
         int boxColor = this.getZoneBoxColor(zone, war);
         drawRect(x, y, x + zoneBoxSize, y + zoneBoxSize, boxColor);
         drawRect(x + 1, y + 1, x + zoneBoxSize - 1, y + zoneBoxSize - 1, zone.contested ? this.getFlashingWhite() : boxColor);
         String letter = String.valueOf((char)(65 + i));
         int letterX = x + (zoneBoxSize - mc.fontRenderer.getStringWidth(letter)) / 2;
         mc.fontRenderer.drawStringWithShadow(letter, (float)letterX, (float)(y + 3), -1);
         x += zoneBoxSize + zoneSpacing;
      }

      x += 4;
      int v1Color = getVillageColor(war.village1);
      int v2Color = getVillageColor(war.village2);
      mc.fontRenderer.drawStringWithShadow(String.valueOf(war.score1), (float)x, (float)(y + 3), v1Color);
      x += mc.fontRenderer.getStringWidth(String.valueOf(war.score1));
      mc.fontRenderer.drawStringWithShadow(" vs ", (float)x, (float)(y + 3), -2832216);
      x += mc.fontRenderer.getStringWidth(" vs ");
      mc.fontRenderer.drawStringWithShadow(String.valueOf(war.score2), (float)x, (float)(y + 3), v2Color);
      x += mc.fontRenderer.getStringWidth(String.valueOf(war.score2));
      x += 8;
      mc.fontRenderer.drawStringWithShadow(timeStr, (float)x, (float)(y + 3), -2832216);
      this.renderZoneArrows(mc, screenWidth, barY + barHeight + 4, zones, player, war);
   }

   private void renderRushHud(Minecraft mc, int screenWidth, int screenHeight, PvpClientData.WarClientInfo war, EntityPlayer player) {
      List<PvpClientData.ZoneClientInfo> zones = PvpClientData.getWarZones();
      String timeStr = formatTime(war.timeRemainingMs);
      int barWidth = Math.min(280, screenWidth - 40);
      int barHeight = 12;
      int barX = (screenWidth - barWidth) / 2;
      int barY = 2;
      drawRect(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, -7706054);
      drawRect(barX, barY, barX + barWidth, barY + barHeight, -1442840576);
      if (!zones.isEmpty()) {
         int segmentWidth = barWidth / zones.size();

         for(int i = 0; i < zones.size(); ++i) {
            PvpClientData.ZoneClientInfo zone = (PvpClientData.ZoneClientInfo)zones.get(i);
            int segX = barX + i * segmentWidth;
            int segRight = i == zones.size() - 1 ? barX + barWidth : segX + segmentWidth;
            int fillColor = this.getZoneBoxColor(zone, war);
            if (zone.contested) {
               fillColor = this.getFlashingWhite();
            }

            if (zone.captureProgress > (double)0.0F && zone.captureProgress < (double)1.0F) {
               int fillWidth = (int)((double)(segRight - segX) * zone.captureProgress);
               int capColor = getVillageColor(zone.capturingVillage);
               drawRect(segX, barY, segX + fillWidth, barY + barHeight, withAlpha(capColor, 136));
            } else if (!zone.controllingVillage.isEmpty()) {
               drawRect(segX, barY, segRight, barY + barHeight, withAlpha(fillColor, 136));
            }

            if (i > 0) {
               drawRect(segX, barY, segX + 1, barY + barHeight, -7706054);
            }

            String num = String.valueOf(i + 1);
            int numX = segX + (segRight - segX - mc.fontRenderer.getStringWidth(num)) / 2;
            mc.fontRenderer.drawStringWithShadow(num, (float)numX, (float)(barY + 2), -1);
         }
      }

      int v1Color = getVillageColor(war.village1);
      int v2Color = getVillageColor(war.village2);
      String scoreLine = war.village1 + " " + war.score1 + " vs " + war.score2 + " " + war.village2 + " | " + timeStr;
      int scoreWidth = mc.fontRenderer.getStringWidth(scoreLine);
      int scoreX = (screenWidth - scoreWidth) / 2;
      int scoreY = barY + barHeight + 3;
      mc.fontRenderer.drawStringWithShadow(war.village1 + " " + war.score1, (float)scoreX, (float)scoreY, v1Color);
      int sx = scoreX + mc.fontRenderer.getStringWidth(war.village1 + " " + war.score1);
      mc.fontRenderer.drawStringWithShadow(" vs ", (float)sx, (float)scoreY, -2832216);
      sx += mc.fontRenderer.getStringWidth(" vs ");
      mc.fontRenderer.drawStringWithShadow(war.score2 + " " + war.village2, (float)sx, (float)scoreY, v2Color);
      sx += mc.fontRenderer.getStringWidth(war.score2 + " " + war.village2);
      mc.fontRenderer.drawStringWithShadow(" | " + timeStr, (float)sx, (float)scoreY, -2832216);
      this.renderZoneArrows(mc, screenWidth, scoreY + 12, zones, player, war);
   }

   private void renderCaptureRing(Minecraft mc, int screenWidth, int screenHeight) {
      int centerX = screenWidth / 2;
      int centerY = screenHeight / 2;
      int outerRadius = 40;
      int innerRadius = 36;
      float r = (float)(captureRingColor >> 16 & 255) / 255.0F;
      float g = (float)(captureRingColor >> 8 & 255) / 255.0F;
      float b = (float)(captureRingColor & 255) / 255.0F;
      int segments = 36;
      int filledSegments = (int)((float)segments * captureRingProgress);
      GlStateManager.disableTexture2D();

      for(int i = 0; i < segments; ++i) {
         double angle1 = (Math.PI * 2D) * (double)i / (double)segments - (Math.PI / 2D);
         double angle2 = (Math.PI * 2D) * (double)(i + 1) / (double)segments - (Math.PI / 2D);
         float alpha = i < filledSegments ? 0.7F : 0.15F;
         int color = colorFromRGBA(r, g, b, alpha);
         int x1o = centerX + (int)(Math.cos(angle1) * (double)outerRadius);
         int y1o = centerY + (int)(Math.sin(angle1) * (double)outerRadius);
         int x2o = centerX + (int)(Math.cos(angle2) * (double)outerRadius);
         int y2o = centerY + (int)(Math.sin(angle2) * (double)outerRadius);
         int x1i = centerX + (int)(Math.cos(angle1) * (double)innerRadius);
         int y1i = centerY + (int)(Math.sin(angle1) * (double)innerRadius);
         int x2i = centerX + (int)(Math.cos(angle2) * (double)innerRadius);
         int y2i = centerY + (int)(Math.sin(angle2) * (double)innerRadius);
         drawRect(Math.min(x1o, x2i), Math.min(y1o, y2i), Math.max(x2o, x1i) + 1, Math.max(y2o, y1i) + 1, color);
      }

      GlStateManager.enableTexture2D();
      String pctText = (int)(captureRingProgress * 100.0F) + "%";
      int textWidth = mc.fontRenderer.getStringWidth(pctText);
      mc.fontRenderer.drawStringWithShadow(pctText, (float)(centerX - textWidth / 2), (float)(centerY + outerRadius + 4), captureRingColor);
   }

   private void renderKillFeed(Minecraft mc, int screenWidth) {
      long now = System.currentTimeMillis();
      killFeed.removeIf((entryx) -> now - entryx.timestamp > 2000L);
      if (!killFeed.isEmpty()) {
         int y = 40;

         for(int i = 0; i < killFeed.size() && i < 5; ++i) {
            KillFeedEntry entry = (KillFeedEntry)killFeed.get(i);
            float age = (float)(now - entry.timestamp) / 2000.0F;
            float alpha = 1.0F - age;
            if (!(alpha <= 0.0F)) {
               int textWidth = mc.fontRenderer.getStringWidth(entry.text);
               int x = (screenWidth - textWidth) / 2;
               int color = withAlpha(entry.color, (int)(alpha * 255.0F));
               mc.fontRenderer.drawStringWithShadow(entry.text, (float)x, (float)y, color);
               y += 12;
            }
         }

      }
   }

   private void renderZoneArrows(Minecraft mc, int screenWidth, int startY, List<PvpClientData.ZoneClientInfo> zones, EntityPlayer player, PvpClientData.WarClientInfo war) {
      int arrowX = screenWidth / 2;
      int arrowY = startY;
      int drawn = 0;

      for(int i = 0; i < zones.size(); ++i) {
         PvpClientData.ZoneClientInfo zone = (PvpClientData.ZoneClientInfo)zones.get(i);
         double dx = (double)zone.centerX + (double)0.5F - player.posX;
         double dz = (double)zone.centerZ + (double)0.5F - player.posZ;
         double dist = Math.sqrt(dx * dx + dz * dz);
         if (!(dist > (double)50.0F)) {
            if (drawn >= 3) {
               break;
            }

            float angleToZone = (float)(Math.atan2(dz, dx) * (double)180.0F / Math.PI) - 90.0F;
            float playerYaw = player.rotationYaw % 360.0F;

            float relAngle;
            for(relAngle = angleToZone - playerYaw; relAngle > 180.0F; relAngle -= 360.0F) {
            }

            while(relAngle < -180.0F) {
               relAngle += 360.0F;
            }

            String arrow;
            if (!(relAngle > 150.0F) && !(relAngle < -150.0F)) {
               if (relAngle > 30.0F) {
                  arrow = "→";
               } else if (relAngle < -30.0F) {
                  arrow = "�?";
               } else {
                  arrow = "↑";
               }
            } else {
               arrow = "↓";
            }

            String zoneLetter = String.valueOf((char)(65 + i));
            int zoneColor = this.getZoneBoxColor(zone, war);
            String label = arrow + " Zone " + zoneLetter + " " + (int)dist + "m";
            int labelWidth = mc.fontRenderer.getStringWidth(label);
            int lx = arrowX - labelWidth / 2;
            mc.fontRenderer.drawStringWithShadow(label, (float)lx, (float)arrowY, zoneColor);
            arrowY += 11;
            ++drawn;
         }
      }

   }

   private static int getVillageColor(String villageName) {
      if (villageName == null) {
         return -7829368;
      } else {
         switch (villageName) {
            case "Leaf":
               return -11141291;
            case "Sand":
               return -171;
            case "Stone":
               return -5601195;
            case "Cloud":
               return -4486913;
            case "Mist":
               return -11167318;
            case "Rain":
               return -14531414;
            default:
               return -7829368;
         }
      }
   }

   private int getZoneBoxColor(PvpClientData.ZoneClientInfo zone, PvpClientData.WarClientInfo war) {
      if (zone.contested) {
         return this.getFlashingWhite();
      } else if (!zone.controllingVillage.isEmpty()) {
         return getVillageColor(zone.controllingVillage);
      } else if (!zone.capturingVillage.isEmpty()) {
         int base = getVillageColor(zone.capturingVillage);
         return withAlpha(base, 136);
      } else {
         return -7829368;
      }
   }

   private int getFlashingWhite() {
      long time = System.currentTimeMillis();
      float t = (float)Math.sin((double)time * 0.006) * 0.5F + 0.5F;
      int alpha = (int)(80.0F + t * 175.0F);
      return alpha << 24 | 16777215;
   }

   private static String formatTime(long timeMs) {
      if (timeMs <= 0L) {
         return "0:00";
      } else {
         long totalSeconds = timeMs / 1000L;
         long hours = totalSeconds / 3600L;
         long minutes = totalSeconds % 3600L / 60L;
         long seconds = totalSeconds % 60L;
         return hours > 0L ? String.format("%d:%02d:%02d", hours, minutes, seconds) : String.format("%d:%02d", minutes, seconds);
      }
   }

   private static int withAlpha(int color, int alpha) {
      return alpha << 24 | color & 16777215;
   }

   private static int colorFromRGBA(float r, float g, float b, float a) {
      int ai = (int)(a * 255.0F) & 255;
      int ri = (int)(r * 255.0F) & 255;
      int gi = (int)(g * 255.0F) & 255;
      int bi = (int)(b * 255.0F) & 255;
      return ai << 24 | ri << 16 | gi << 8 | bi;
   }

   private static void drawRect(int left, int top, int right, int bottom, int color) {
      Gui.drawRect(left, top, right, bottom, color);
   }

   private static class KillFeedEntry {
      final String text;
      final int color;
      final long timestamp;

      KillFeedEntry(String text, int color, long timestamp) {
         this.text = text;
         this.color = color;
         this.timestamp = timestamp;
      }
   }
}
