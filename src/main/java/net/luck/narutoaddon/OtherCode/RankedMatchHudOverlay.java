
package net.luck.narutoaddon.OtherCode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

@SideOnly(Side.CLIENT)
public class RankedMatchHudOverlay extends Gui {
   private static RankedMatchHudOverlay instance;
   private static boolean inMatch = false;
   private static String matchState = "";
   private static int timeRemainingSeconds = 0;
   private static String player1Name = "";
   private static String player2Name = "";
   private static String player1Rank = "";
   private static String player2Rank = "";
   private static String player1UUID = "";
   private static String player2UUID = "";
   private static String arenaName = "";
   private static float animationTick = 0.0F;
   private static long lastUpdateTime = 0L;
   private static final int COLOR_TIMER_NORMAL = -1;
   private static final int COLOR_TIMER_WARNING = -13312;
   private static final int COLOR_TIMER_CRITICAL = -48060;
   private static final int COLOR_VS = -39424;
   private static final int COLOR_PANEL_BG = -1442840576;
   private static final int COLOR_PANEL_BORDER = -13421773;
   private static final int COLOR_GOLD_ACCENT = -10496;
   private static final int HEAD_SIZE = 16;
   private static final int FRAME_SIZE = 22;
   private static final int FRAME_PADDING = 3;

   public static RankedMatchHudOverlay getInstance() {
      if (instance == null) {
         instance = new RankedMatchHudOverlay();
      }

      return instance;
   }

   public static void updateMatchData(String state, int timeRemaining, String p1Name, String p2Name, String p1Rank, String p2Rank, String p1UUID, String p2UUID, String arena) {
      inMatch = true;
      matchState = state;
      timeRemainingSeconds = timeRemaining;
      player1Name = p1Name;
      player2Name = p2Name;
      player1Rank = p1Rank;
      player2Rank = p2Rank;
      player1UUID = p1UUID;
      player2UUID = p2UUID;
      arenaName = arena;
      lastUpdateTime = System.currentTimeMillis();
   }

   public static void clearMatchData() {
      inMatch = false;
      matchState = "";
      timeRemainingSeconds = 0;
      player1Name = "";
      player2Name = "";
      player1Rank = "";
      player2Rank = "";
      player1UUID = "";
      player2UUID = "";
      arenaName = "";
   }

   public static boolean isInMatch() {
      return inMatch;
   }

   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
      if (event.getType() == ElementType.TEXT) {
         if (inMatch && !matchState.isEmpty()) {
            if (matchState.equals("countdown") || matchState.equals("active")) {
               Minecraft mc = Minecraft.getMinecraft();
               if (mc.player != null && !mc.gameSettings.hideGUI) {
                  if (mc.currentScreen == null || mc.currentScreen instanceof GuiChat) {
                     animationTick += 0.05F;
                     ScaledResolution scaled = new ScaledResolution(mc);
                     int screenWidth = scaled.getScaledWidth();
                     this.drawMatchHud(mc, screenWidth);
                     GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                     GlStateManager.disableBlend();
                     GlStateManager.enableAlpha();
                     mc.getTextureManager().bindTexture(Gui.ICONS);
                  }
               }
            }
         }
      }
   }

   private void drawMatchHud(Minecraft mc, int screenWidth) {
      int centerX = screenWidth / 2;
      int topY = 4;
      int panelWidth = 140;
      int panelHeight = 40;
      int panelX = centerX - panelWidth / 2;
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      this.drawPanelBackground(panelX, topY, panelWidth, panelHeight);
      this.drawPlayerVS(mc, centerX, topY + 3);
      this.drawTimer(mc, centerX, topY + 28);
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   private void drawPanelBackground(int x, int y, int width, int height) {
      drawRect(x - 1, y - 1, x + width + 1, y + height + 1, -13421773);
      this.drawGradientRect(x, y, x + width, y + height, -870704614, -871757302);
      drawRect(x, y, x + width, y + 1, -10496);
      int cornerSize = 3;
      drawRect(x, y, x + cornerSize, y + 1, -86);
      drawRect(x, y, x + 1, y + cornerSize, -86);
      drawRect(x + width - cornerSize, y, x + width, y + 1, -86);
      drawRect(x + width - 1, y, x + width, y + cornerSize, -86);
   }

   private void drawTimer(Minecraft mc, int centerX, int y) {
      int minutes = timeRemainingSeconds / 60;
      int seconds = timeRemainingSeconds % 60;
      String timeStr = String.format("%d:%02d", minutes, seconds);
      int timerColor = -1;
      boolean pulse = false;
      if (matchState.equals("active")) {
         if (timeRemainingSeconds <= 30) {
            timerColor = -48060;
            pulse = true;
         } else if (timeRemainingSeconds <= 60) {
            timerColor = -13312;
         }
      } else if (matchState.equals("countdown")) {
         timerColor = -10496;
      }

      if (pulse) {
         float pulseAmount = (float)(0.7 + 0.3 * Math.sin((double)animationTick * 0.15));
         int alpha = (int)(pulseAmount * 255.0F);
         timerColor = alpha << 24 | timerColor & 16777215;
      }

      int timerWidth = mc.fontRenderer.getStringWidth(timeStr);
      mc.fontRenderer.drawStringWithShadow(timeStr, (float)(centerX - timerWidth / 2), (float)y, timerColor);
   }

   private void drawPlayerVS(Minecraft mc, int centerX, int y) {
      int p1Tier = this.getFrameTier(player1Rank);
      int p2Tier = this.getFrameTier(player2Rank);
      String vsText = "VS";
      int vsWidth = mc.fontRenderer.getStringWidth(vsText);
      int vsSpacing = 12;
      int totalWidth = 22 + vsSpacing + vsWidth + vsSpacing + 22;
      int startX = centerX - totalWidth / 2;
      int frameY = y + 2;
      this.drawFramedPlayerHead(mc, startX, frameY, player1UUID, p1Tier);
      int vsX = startX + 22 + vsSpacing;
      int vsY = frameY + 7;
      mc.fontRenderer.drawStringWithShadow("§6§l" + vsText, (float)vsX, (float)vsY, 16777215);
      int p2FrameX = vsX + vsWidth + vsSpacing;
      this.drawFramedPlayerHead(mc, p2FrameX, frameY, player2UUID, p2Tier);
   }

   private void drawFramedPlayerHead(Minecraft mc, int x, int y, String uuidStr, int tier) {
      this.drawFrameBackground(x, y, 22, tier);
      int headX = x + 3;
      int headY = y + 3;
      ResourceLocation skinLocation = null;
      if (uuidStr != null && !uuidStr.isEmpty()) {
         try {
            UUID uuid = UUID.fromString(uuidStr);
            if (mc.getConnection() != null) {
               NetworkPlayerInfo playerInfo = mc.getConnection().getPlayerInfo(uuid);
               if (playerInfo != null) {
                  skinLocation = playerInfo.getLocationSkin();
               }
            }
         } catch (Exception var11) {
         }
      }

      if (skinLocation == null) {
         skinLocation = new ResourceLocation("textures/entity/steve.png");
      }

      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      mc.getTextureManager().bindTexture(skinLocation);
      this.drawScaledTexturedRect(headX, headY, 16, 16, 8, 8, 8, 8, 64, 64);
      this.drawScaledTexturedRect(headX, headY, 16, 16, 40, 8, 8, 8, 64, 64);
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
      this.drawFrameOverlay(x, y, 22, tier);
   }

   private int getFrameTier(String rank) {
      if (rank != null && !rank.isEmpty()) {
         if (rank.contains("Otsutsuki")) {
            return 7;
         } else if (rank.contains("Kage")) {
            return 6;
         } else if (rank.contains("ANBU")) {
            return 5;
         } else if (rank.contains("Elite Jonin")) {
            return 4;
         } else if (rank.contains("S. Jonin")) {
            return 3;
         } else if (rank.contains("Jonin")) {
            return 2;
         } else {
            return rank.contains("Chunin") ? 1 : 0;
         }
      } else {
         return 0;
      }
   }

   private void drawFrameBackground(int x, int y, int size, int tier) {
      int baseColor;
      int highlightColor;
      int shadowColor;
      int glowColor;
      switch (tier) {
         case 1:
            baseColor = -16115702;
            highlightColor = -15719920;
            shadowColor = -16446459;
            glowColor = -11154347;
            break;
         case 2:
            baseColor = -15199222;
            highlightColor = -14541808;
            shadowColor = -15988219;
            glowColor = -2241468;
            break;
         case 3:
            baseColor = -15070200;
            highlightColor = -14150640;
            shadowColor = -15923708;
            glowColor = -35038;
            break;
         case 4:
            baseColor = -16116712;
            highlightColor = -15721438;
            shadowColor = -16446964;
            glowColor = -11154194;
            break;
         case 5:
            baseColor = -15594984;
            highlightColor = -15069150;
            shadowColor = -16251636;
            glowColor = -6728244;
            break;
         case 6:
            baseColor = -15070710;
            highlightColor = -14020078;
            shadowColor = -15923963;
            glowColor = -2280653;
            break;
         case 7:
            baseColor = -15067888;
            highlightColor = -14015208;
            shadowColor = -15725304;
            glowColor = -10496;
            break;
         default:
            baseColor = -15461356;
            highlightColor = -14803426;
            shadowColor = -16119286;
            glowColor = -6710887;
      }

      if (tier >= 3) {
         float glowIntensity = (float)((double)0.25F + 0.15 * Math.sin((double)animationTick * 0.04));
         if (tier >= 5) {
            glowIntensity += 0.15F;
         }

         if (tier == 7) {
            glowIntensity += 0.1F;
         }

         int glowAlpha = (int)(glowIntensity * 255.0F);
         int glowWithAlpha = glowAlpha << 24 | glowColor & 16777215;
         int glowSize = tier >= 5 ? 3 : 2;

         for(int i = glowSize; i >= 1; --i) {
            int layerAlpha = glowAlpha / (i + 1);
            drawRect(x - i, y - i, x + size + i, y + size + i, layerAlpha << 24 | glowColor & 16777215);
         }
      }

      drawRect(x, y, x + size, y + size, baseColor);
      drawRect(x, y, x + size, y + 1, highlightColor);
      drawRect(x, y, x + 1, y + size, highlightColor);
      drawRect(x, y + size - 1, x + size, y + size, shadowColor);
      drawRect(x + size - 1, y, x + size, y + size, shadowColor);
   }

   private void drawFrameOverlay(int x, int y, int size, int tier) {
      switch (tier) {
         case 1:
            this.drawChuninFrame(x, y, size);
            return;
         case 2:
            this.drawSpecialFrame(x, y, size);
            return;
         case 3:
            this.drawJoninFrame(x, y, size);
            return;
         case 4:
            this.drawEliteFrame(x, y, size);
            return;
         case 5:
            this.drawAnbuFrame(x, y, size);
            return;
         case 6:
            this.drawKageFrame(x, y, size);
            return;
         case 7:
            this.drawOtsutsukiFrame(x, y, size);
            return;
         default:
            this.drawGeninFrame(x, y, size);
      }
   }

   private void drawGeninFrame(int x, int y, int size) {
      int highlight = -4473925;
      int base = -7829368;
      int shadow = -11184811;
      int darkShadow = -13421773;
      drawRect(x - 1, y - 1, x + size + 1, y, highlight);
      drawRect(x - 1, y - 1, x, y + size + 1, highlight);
      drawRect(x - 1, y + size, x + size + 1, y + size + 1, shadow);
      drawRect(x + size, y - 1, x + size + 1, y + size + 1, shadow);
      drawRect(x - 1, y - 1, x + 2, y + 2, highlight);
      drawRect(x + size - 2, y + size - 2, x + size + 1, y + size + 1, darkShadow);
   }

   private void drawChuninFrame(int x, int y, int size) {
      int highlight = -8921737;
      int base = -12277180;
      int shadow = -14522846;
      drawRect(x - 2, y - 2, x + size + 2, y + size + 2, 541370948);
      drawRect(x - 1, y - 1, x + size + 1, y, highlight);
      drawRect(x - 1, y - 1, x, y + size + 1, highlight);
      drawRect(x - 1, y + size, x + size + 1, y + size + 1, shadow);
      drawRect(x + size, y - 1, x + size + 1, y + size + 1, shadow);
      int cs = 4;
      drawRect(x - 2, y - 2, x + cs, y, highlight);
      drawRect(x - 2, y - 2, x, y + cs, highlight);
      drawRect(x + size - cs, y + size, x + size + 2, y + size + 2, shadow);
      drawRect(x + size, y + size - cs, x + size + 2, y + size + 2, shadow);
   }

   private void drawSpecialFrame(int x, int y, int size) {
      int parchment = -2833248;
      int highlight = -4478345;
      int base = -6715290;
      int shadow = -11189214;
      drawRect(x - 2, y - 2, x + size + 2, y + size + 2, 546932838);
      drawRect(x - 1, y - 1, x + size + 1, y, highlight);
      drawRect(x - 1, y - 1, x, y + size + 1, highlight);
      drawRect(x - 1, y + size, x + size + 1, y + size + 1, shadow);
      drawRect(x + size, y - 1, x + size + 1, y + size + 1, shadow);
      int cs = 3;
      drawRect(x - 2, y - 2, x + cs, y, parchment);
      drawRect(x - 2, y - 2, x, y + cs, parchment);
      drawRect(x + size - cs, y + size, x + size + 2, y + size + 2, shadow);
   }

   private void drawJoninFrame(int x, int y, int size) {
      int flameWhite = -4404;
      int flameYellow = -13244;
      int flameOrange = -30686;
      int ember = -5623040;
      float flicker = (float)(0.8 + 0.2 * Math.sin((double)animationTick * 0.06));
      int flickerAlpha = (int)(flicker * 40.0F);
      drawRect(x - 3, y - 3, x + size + 3, y + size + 3, flickerAlpha << 24 | 16737809);
      drawRect(x - 1, y - 2, x + size + 1, y, flameWhite);
      drawRect(x - 2, y - 1, x, y + size + 1, flameWhite);
      drawRect(x - 1, y + size, x + size + 1, y + size + 2, ember);
      drawRect(x + size, y - 1, x + size + 2, y + size + 1, ember);
      int cs = 5;
      drawRect(x - 3, y - 3, x + cs, y, flameWhite);
      drawRect(x - 3, y - 3, x, y + cs, flameWhite);
      drawRect(x + size - cs, y + size, x + size + 3, y + size + 3, ember);
   }

   private void drawEliteFrame(int x, int y, int size) {
      int electricWhite = -1114113;
      int highlight = -7803137;
      int base = -11154194;
      int shadow = -13399911;
      float pulse = (float)(0.8 + 0.2 * Math.sin((double)animationTick * 0.045));
      int pulseAlpha = (int)(pulse * 45.0F);
      drawRect(x - 3, y - 3, x + size + 3, y + size + 3, pulseAlpha << 24 | 5623022);
      int cut = 3;
      drawRect(x + cut, y - 2, x + size - cut, y, electricWhite);
      drawRect(x - 2, y + cut, x, y + size - cut, electricWhite);
      drawRect(x + cut, y + size, x + size - cut, y + size + 2, shadow);
      drawRect(x + size, y + cut, x + size + 2, y + size - cut, shadow);
      drawRect(x - 1, y + cut - 1, x + cut, y + cut + 1, electricWhite);
      drawRect(x + cut - 1, y - 1, x + cut + 1, y + cut, electricWhite);
      drawRect(x + size - cut, y + size - cut, x + size + 1, y + size + 1, shadow);
   }

   private void drawAnbuFrame(int x, int y, int size) {
      int highlight = -3372818;
      int bright = -5609780;
      int base = -7842390;
      int shadow = -12307610;
      float aura = (float)((double)0.75F + (double)0.25F * Math.sin((double)animationTick * 0.035));
      int auraAlpha = (int)(aura * 50.0F);
      drawRect(x - 2, y - 2, x + size + 2, y + size + 2, auraAlpha << 24 | 8934826);
      drawRect(x - 1, y - 2, x + size + 1, y, highlight);
      drawRect(x - 2, y - 1, x, y + size + 1, highlight);
      drawRect(x - 1, y + size, x + size + 1, y + size + 2, shadow);
      drawRect(x + size, y - 1, x + size + 2, y + size + 1, shadow);
      int cs = 5;
      drawRect(x - 3, y - 3, x + cs, y, highlight);
      drawRect(x - 3, y - 3, x, y + cs, highlight);
      drawRect(x + size - cs, y + size, x + size + 3, y + size + 3, shadow);
      int midY = y + size / 2;
      drawRect(x - 3, midY - 2, x - 1, midY + 2, base);
      drawRect(x + size + 1, midY - 2, x + size + 3, midY + 2, shadow);
   }

   private void drawKageFrame(int x, int y, int size) {
      int white = -21846;
      int highlight = -39322;
      int bright = -1162172;
      int base = -3399134;
      int shadow = -7862264;
      float power = (float)(0.7 + 0.3 * Math.sin((double)animationTick * 0.04));
      int powerAlpha = (int)(power * 60.0F);

      for(int i = 2; i >= 0; --i) {
         int layerAlpha = powerAlpha / (i + 1);
         drawRect(x - 3 - i, y - 3 - i, x + size + 3 + i, y + size + 3 + i, layerAlpha << 24 | 13378082);
      }

      drawRect(x - 2, y - 3, x + size + 2, y, white);
      drawRect(x - 3, y - 2, x, y + size + 2, white);
      drawRect(x - 2, y + size, x + size + 2, y + size + 3, shadow);
      drawRect(x + size, y - 2, x + size + 3, y + size + 2, shadow);
      int cs = 6;
      drawRect(x - 4, y - 4, x + cs, y, white);
      drawRect(x - 4, y - 4, x, y + cs, white);
      drawRect(x + size - cs, y + size, x + size + 4, y + size + 4, shadow);
   }

   private void drawOtsutsukiFrame(int x, int y, int size) {
      float pulse = (float)(0.85 + 0.15 * Math.sin((double)animationTick * 0.03));
      int divine = -1;
      int hotGold = -4472;
      int highlight = -8875;
      int bright = -1131725;
      int base = -2250206;
      int shadow = -7838208;
      float divineGlow = (float)(0.6 + 0.4 * Math.sin((double)animationTick * 0.025));

      for(int i = 4; i >= 0; --i) {
         int layerAlpha = (int)(divineGlow * 80.0F / (float)(i + 1));
         int offset = 3 + i;
         drawRect(x - offset, y - offset, x + size + offset, y + size + offset, layerAlpha << 24 | 16766720);
      }

      drawRect(x - 2, y - 4, x + size + 2, y - 2, divine);
      drawRect(x - 2, y - 2, x + size + 2, y, hotGold);
      drawRect(x - 4, y - 2, x - 2, y + size + 2, divine);
      drawRect(x - 2, y - 2, x, y + size + 2, hotGold);
      drawRect(x - 2, y + size, x + size + 2, y + size + 2, base);
      drawRect(x - 2, y + size + 2, x + size + 2, y + size + 4, shadow);
      drawRect(x + size, y - 2, x + size + 2, y + size + 2, base);
      drawRect(x + size + 2, y - 2, x + size + 4, y + size + 2, shadow);
      int cs = 8;
      drawRect(x - 5, y - 5, x + cs, y - 1, divine);
      drawRect(x - 5, y - 5, x - 1, y + cs, divine);
      drawRect(x - 3, y - 3, x + 1, y + 1, hotGold);
      drawRect(x - 2, y - 2, x, y, divine);
      drawRect(x + size - cs, y + size + 1, x + size + 5, y + size + 5, shadow);
      drawRect(x + size + 1, y + size - cs, x + size + 5, y + size + 5, shadow);
      int midX = x + size / 2;
      int midY = y + size / 2;
      drawRect(midX - 1, y - 5, midX + 1, y - 3, divine);
      drawRect(x - 5, midY - 1, x - 3, midY + 1, divine);
      this.drawOtsutsukiParticles(x, y, size);
   }

   private void drawOtsutsukiParticles(int x, int y, int size) {
      int numParticles = 6;
      float time = animationTick * 0.012F;

      for(int i = 0; i < numParticles; ++i) {
         float angle = (float)((double)time + (double)i * Math.PI * (double)2.0F / (double)numParticles);
         float radiusBase = (float)(size / 2 + 6);
         float radiusPulse = (float)(Math.sin((double)time * 0.04 + (double)i * (double)0.5F) * (double)2.0F);
         float radius = radiusBase + radiusPulse;
         int px = x + size / 2 + (int)(Math.cos((double)angle) * (double)radius);
         int py = y + size / 2 + (int)(Math.sin((double)angle) * (double)radius);
         float brightness = (float)(0.6 + 0.4 * Math.sin((double)time * 0.06 + (double)i));
         int particleAlpha = (int)(brightness * 255.0F);
         drawRect(px - 1, py - 1, px + 1, py + 1, particleAlpha << 24 | 16768341);
         drawRect(px, py, px + 1, py + 1, -1);
      }

   }

   private int brightenColor(int color, int amount) {
      int a = color >> 24 & 255;
      int r = Math.min(255, (color >> 16 & 255) + amount);
      int g = Math.min(255, (color >> 8 & 255) + amount);
      int b = Math.min(255, (color & 255) + amount);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private void drawScaledTexturedRect(int x, int y, int width, int height, int texX, int texY, int texW, int texH, int texWidth, int texHeight) {
      float u1 = (float)texX / (float)texWidth;
      float v1 = (float)texY / (float)texHeight;
      float u2 = (float)(texX + texW) / (float)texWidth;
      float v2 = (float)(texY + texH) / (float)texHeight;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
      buffer.pos((double)x, (double)(y + height), (double)0.0F).tex((double)u1, (double)v2).endVertex();
      buffer.pos((double)(x + width), (double)(y + height), (double)0.0F).tex((double)u2, (double)v2).endVertex();
      buffer.pos((double)(x + width), (double)y, (double)0.0F).tex((double)u2, (double)v1).endVertex();
      buffer.pos((double)x, (double)y, (double)0.0F).tex((double)u1, (double)v1).endVertex();
      tessellator.draw();
   }
}
