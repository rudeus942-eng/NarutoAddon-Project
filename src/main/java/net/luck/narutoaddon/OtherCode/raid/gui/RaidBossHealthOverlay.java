
package net.luck.narutoaddon.OtherCode.raid.gui;

import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.network.RaidClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RaidBossHealthOverlay extends Gui {
   private static final ResourceLocation RAID_HUD_TEXTURE = new ResourceLocation("inftsukaddon", "textures/gui/raid_hud.png");
   private static final int COLOR_HEALTH_BAR = -3407872;
   private static final int COLOR_HEALTH_BG = -13434880;
   private static final int COLOR_IMMUNE_BAR = -7864065;
   private static final int COLOR_ENRAGE_1 = -30720;
   private static final int COLOR_ENRAGE_2 = -65536;
   private static final int COLOR_SAFE_ZONE = -16711936;
   private static final int COLOR_DANGER_ZONE = -65536;
   private static final int COLOR_WARNING = -256;
   private static final int COLOR_WHITE = -1;
   private static final int COLOR_GRAY = -5592406;
   private static final int COLOR_GOLD = -22016;
   private static final int COLOR_GENIN = -7829368;
   private static final int COLOR_CHUNIN = -11141291;
   private static final int COLOR_JONIN = -22016;
   private static final int COLOR_ANBU = -43691;
   private static final int HEALTH_BAR_WIDTH = 200;
   private static final int HEALTH_BAR_HEIGHT = 12;
   private static final int HEALTH_BAR_Y_OFFSET = 30;
   private final Minecraft mc = Minecraft.getMinecraft();

   @SubscribeEvent
   public void onRenderBossBarPre(RenderGameOverlayEvent.Pre event) {
      if (event.getType() == ElementType.BOSSHEALTH && RaidClientData.isInRaid()) {
         event.setCanceled(true);
      }

      if ((event.getType() == ElementType.HEALTH || event.getType() == ElementType.ARMOR) && RaidClientData.isInRaid()) {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableAlpha();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         this.mc.getTextureManager().bindTexture(Gui.ICONS);
      }

   }

   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
      if (event.getType() == ElementType.ALL) {
         if (RaidClientData.isInRaid()) {
            ScaledResolution sr = new ScaledResolution(this.mc);
            int screenWidth = sr.getScaledWidth();
            int screenHeight = sr.getScaledHeight();
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            this.renderBossHealthBar(screenWidth, screenHeight);
            this.renderPhaseIndicator(screenWidth, screenHeight);
            this.renderEnrageTimer(screenWidth, screenHeight);
            this.renderMechanicWarning(screenWidth, screenHeight);
            this.renderPuzzleProgress(screenWidth, screenHeight);
            this.renderImmunityStatus(screenWidth, screenHeight);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            this.mc.getTextureManager().bindTexture(Gui.ICONS);
            GlStateManager.popMatrix();
         }
      }
   }

   private void renderBossHealthBar(int screenWidth, int screenHeight) {
      int barX = (screenWidth - 200) / 2;
      int barY = 30;
      drawRect(barX - 4, barY - 4, barX + 200 + 4, barY + 12 + 4, -15073280);
      drawRect(barX - 3, barY - 3, barX + 200 + 3, barY + 12 + 3, -12320768);
      drawRect(barX - 2, barY - 2, barX + 200 + 2, barY + 12 + 2, -16777216);
      this.drawGradientRect(barX, barY, barX + 200, barY + 12, -13434880, -15073280);
      float healthPercent = RaidClientData.getBossHealthPercent();
      int healthWidth = (int)(200.0F * healthPercent);
      int barColor = RaidClientData.isBossImmune() ? -7864065 : this.getHealthBarColor();
      int barColorDark = this.darkenColor(barColor, 60);
      this.drawGradientRect(barX, barY, barX + healthWidth, barY + 12, barColor, barColorDark);
      int shimmerOffset = (int)(System.currentTimeMillis() / 50L % 200L);
      if (shimmerOffset < healthWidth) {
         int shimmerX = barX + shimmerOffset;
         drawRect(shimmerX, barY, Math.min(shimmerX + 3, barX + healthWidth), barY + 12, 872415231);
      }

      drawRect(barX, barY, barX + healthWidth, barY + 1, this.brightenColor(barColor, 40));
      drawRect(barX - 6, barY + 6 - 1, barX - 2, barY + 6 + 1, -5636096);
      drawRect(barX + 200 + 2, barY + 6 - 1, barX + 200 + 6, barY + 6 + 1, -5636096);
      String bossName = RaidClientData.getBossDisplayName();
      int difficultyOrdinal = RaidClientData.getBossDifficulty();
      RaidDifficulty diff = RaidDifficulty.values()[Math.min(difficultyOrdinal, RaidDifficulty.values().length - 1)];
      String diffTag = "§l[" + diff.getDisplayName().toUpperCase() + "]";
      int nameColor = this.getDifficultyColor(diff);
      String fullName = "☠ " + diffTag + " " + bossName + " ☠";
      int nameWidth = this.mc.fontRenderer.getStringWidth(fullName);
      int nameBgX = (screenWidth - nameWidth) / 2 - 5;
      int nameBgY = barY - 16;
      this.drawGradientRect(nameBgX, nameBgY, nameBgX + nameWidth + 10, nameBgY + 12, -585498624, -586547200);
      drawRect(nameBgX, nameBgY, nameBgX + nameWidth + 10, nameBgY + 1, -10092544);
      this.mc.fontRenderer.drawStringWithShadow(fullName, (float)(screenWidth - nameWidth) / 2.0F, (float)(barY - 14), nameColor);
      int healthPercentInt = (int)(healthPercent * 100.0F);
      String healthText = String.format("%.0f / %.0f (%d%%)", RaidClientData.getBossCurrentHealth(), RaidClientData.getBossMaxHealth(), healthPercentInt);
      int healthTextWidth = this.mc.fontRenderer.getStringWidth(healthText);
      this.mc.fontRenderer.drawStringWithShadow(healthText, (float)(screenWidth - healthTextWidth) / 2.0F, (float)(barY + 2), -1);
   }

   private int darkenColor(int color, int amount) {
      int r = Math.max(0, (color >> 16 & 255) - amount);
      int g = Math.max(0, (color >> 8 & 255) - amount);
      int b = Math.max(0, (color & 255) - amount);
      return -16777216 | r << 16 | g << 8 | b;
   }

   private int brightenColor(int color, int amount) {
      int r = Math.min(255, (color >> 16 & 255) + amount);
      int g = Math.min(255, (color >> 8 & 255) + amount);
      int b = Math.min(255, (color & 255) + amount);
      return -16777216 | r << 16 | g << 8 | b;
   }

   private void renderPhaseIndicator(int screenWidth, int screenHeight) {
      int currentPhase = RaidClientData.getCurrentPhase();
      int totalPhases = RaidClientData.getTotalPhases();
      int barX = (screenWidth - 200) / 2;
      int barY = 46;
      int dotSpacing = 200 / (totalPhases + 1);
      int dotSize = 6;

      for(int i = 1; i <= totalPhases; ++i) {
         int dotX = barX + dotSpacing * i - dotSize / 2;
         int dotColor;
         if (i < currentPhase) {
            dotColor = -16733696;
         } else if (i == currentPhase) {
            dotColor = -22016;
         } else {
            dotColor = -5592406;
         }

         drawRect(dotX, barY, dotX + dotSize, barY + dotSize, dotColor);
      }

      String phaseName = RaidClientData.getPhaseName();
      if (phaseName != null && !phaseName.isEmpty()) {
         int phaseTextWidth = this.mc.fontRenderer.getStringWidth(phaseName);
         this.mc.fontRenderer.drawStringWithShadow(phaseName, (float)(screenWidth - phaseTextWidth) / 2.0F, (float)(barY + dotSize + 2), -5592406);
      }

   }

   private void renderEnrageTimer(int screenWidth, int screenHeight) {
      long elapsed = RaidClientData.getRaidTimeElapsed();
      int enrageLevel = RaidClientData.getEnrageLevel();
      long ENRAGE_1_TIME = 600000L;
      long ENRAGE_2_TIME = 900000L;
      long WIPE_TIME = 1050000L;
      int timerX = (screenWidth + 200) / 2 + 15;
      int timerY = 26;
      int timerWidth = 75;
      int timerHeight = 38;
      int frameColor;
      int barColor;
      int textColor;
      String statusLabel;
      long nextMilestone;
      long previousMilestone;
      if (enrageLevel >= 2) {
         frameColor = -5636096;
         barColor = -65536;
         textColor = -65536;
         statusLabel = "⚠ ENRAGED";
         nextMilestone = 1050000L;
         previousMilestone = 900000L;
      } else if (enrageLevel == 1) {
         frameColor = -7842560;
         barColor = -30720;
         textColor = -30720;
         statusLabel = "ENRAGE";
         nextMilestone = 900000L;
         previousMilestone = 600000L;
      } else {
         frameColor = -12303292;
         barColor = -16733696;
         textColor = -1;
         statusLabel = "TIME";
         nextMilestone = 600000L;
         previousMilestone = 0L;
      }

      long timeToNext = nextMilestone - elapsed;
      boolean shouldFlash = timeToNext > 0L && timeToNext < 30000L && System.currentTimeMillis() / 300L % 2L == 0L;
      if (shouldFlash && enrageLevel < 2) {
         frameColor = this.brightenColor(frameColor, 60);
      }

      drawRect(timerX - 2, timerY - 2, timerX + timerWidth + 2, timerY + timerHeight + 2, -16777216);
      drawRect(timerX - 1, timerY - 1, timerX + timerWidth + 1, timerY + timerHeight + 1, frameColor);
      this.drawGradientRect(timerX, timerY, timerX + timerWidth, timerY + timerHeight, -300279270, -301331958);
      int labelWidth = this.mc.fontRenderer.getStringWidth(statusLabel);
      int labelX = timerX + (timerWidth - labelWidth) / 2;
      this.mc.fontRenderer.drawStringWithShadow(statusLabel, (float)labelX, (float)(timerY + 2), textColor);
      long seconds = elapsed / 1000L;
      long minutes = seconds / 60L;
      seconds %= 60L;
      String timeText = String.format("%02d:%02d", minutes, seconds);
      int timeWidth = this.mc.fontRenderer.getStringWidth(timeText);
      int timeX = timerX + (timerWidth - timeWidth) / 2;
      this.mc.fontRenderer.drawStringWithShadow(timeText, (float)timeX, (float)(timerY + 13), textColor);
      int progressBarX = timerX + 4;
      int progressBarY = timerY + 25;
      int progressBarWidth = timerWidth - 8;
      int progressBarHeight = 4;
      drawRect(progressBarX, progressBarY, progressBarX + progressBarWidth, progressBarY + progressBarHeight, -14540254);
      float progress;
      if (elapsed >= nextMilestone) {
         progress = 1.0F;
      } else {
         long windowDuration = nextMilestone - previousMilestone;
         long elapsed_in_window = elapsed - previousMilestone;
         progress = Math.max(0.0F, Math.min(1.0F, (float)elapsed_in_window / (float)windowDuration));
      }

      int fillWidth = (int)((float)progressBarWidth * progress);
      if (fillWidth > 0) {
         int barColorDark = this.darkenColor(barColor, 40);
         this.drawGradientRect(progressBarX, progressBarY, progressBarX + fillWidth, progressBarY + progressBarHeight, barColor, barColorDark);
         drawRect(progressBarX, progressBarY, progressBarX + fillWidth, progressBarY + 1, this.brightenColor(barColor, 30));
      }

      if (timeToNext > 0L) {
         long nextSecs = timeToNext / 1000L;
         long nextMins = nextSecs / 60L;
         nextSecs %= 60L;
         String nextLabel;
         if (enrageLevel >= 2) {
            nextLabel = String.format("WIPE: %d:%02d", nextMins, nextSecs);
         } else if (enrageLevel == 1) {
            nextLabel = String.format("E2: %d:%02d", nextMins, nextSecs);
         } else {
            nextLabel = String.format("E1: %d:%02d", nextMins, nextSecs);
         }

         int nextWidth = this.mc.fontRenderer.getStringWidth(nextLabel);
         int nextX = timerX + (timerWidth - nextWidth) / 2;
         int nextColor = timeToNext < 30000L ? (shouldFlash ? -256 : textColor) : -5592406;
         this.mc.fontRenderer.drawStringWithShadow(nextLabel, (float)nextX, (float)(timerY + 31), nextColor);
      } else if (enrageLevel >= 2) {
         String wipeLabel = "☠ WIPE ☠";
         int wipeWidth = this.mc.fontRenderer.getStringWidth(wipeLabel);
         int wipeX = timerX + (timerWidth - wipeWidth) / 2;
         boolean wipeFlash = System.currentTimeMillis() / 200L % 2L == 0L;
         this.mc.fontRenderer.drawStringWithShadow(wipeLabel, (float)wipeX, (float)(timerY + 31), wipeFlash ? -65536 : -256);
      }

   }

   private void renderMechanicWarning(int screenWidth, int screenHeight) {
      if (RaidClientData.hasWarning()) {
         String warningMessage = RaidClientData.getCurrentWarningMessage();
         int warningTicks = RaidClientData.getWarningTicksRemaining();
         boolean flash = System.currentTimeMillis() / 250L % 2L == 0L;
         if (warningTicks >= 40 || flash) {
            int warningY = screenHeight / 3;
            int msgWidth = this.mc.fontRenderer.getStringWidth(warningMessage);
            int bgX = (screenWidth - msgWidth) / 2 - 10;
            int bgY = warningY - 5;
            drawRect(bgX, bgY, bgX + msgWidth + 20, bgY + 20, -1442840576);
            this.mc.fontRenderer.drawStringWithShadow(warningMessage, (float)(screenWidth - msgWidth) / 2.0F, (float)warningY, -256);
            float secondsLeft = (float)warningTicks / 20.0F;
            String countdown = String.format("%.1fs", secondsLeft);
            int countdownWidth = this.mc.fontRenderer.getStringWidth(countdown);
            this.mc.fontRenderer.drawStringWithShadow(countdown, (float)(screenWidth - countdownWidth) / 2.0F, (float)(warningY + 12), -256);
            RaidClientData.tickWarning();
         }
      }
   }

   private void renderPuzzleProgress(int screenWidth, int screenHeight) {
      if (RaidClientData.isPuzzleActive()) {
         String objective = RaidClientData.getPuzzleObjective();
         int progress = RaidClientData.getPuzzleProgress();
         int total = RaidClientData.getPuzzleTotal();
         int puzzleY = screenHeight / 2 - 30;
         int objWidth = this.mc.fontRenderer.getStringWidth(objective);
         int bgX = (screenWidth - objWidth) / 2 - 10;
         int bgY = puzzleY - 5;
         drawRect(bgX, bgY, bgX + objWidth + 20, bgY + 35, -1442840576);
         this.mc.fontRenderer.drawStringWithShadow(objective, (float)(screenWidth - objWidth) / 2.0F, (float)puzzleY, -22016);
         int progressBarWidth = 100;
         int progressBarX = (screenWidth - progressBarWidth) / 2;
         int progressBarY = puzzleY + 15;
         drawRect(progressBarX, progressBarY, progressBarX + progressBarWidth, progressBarY + 8, -13421773);
         int fillWidth = (int)((float)progress / (float)total * (float)progressBarWidth);
         drawRect(progressBarX, progressBarY, progressBarX + fillWidth, progressBarY + 8, -16711936);
         String progressText = progress + " / " + total;
         int progressTextWidth = this.mc.fontRenderer.getStringWidth(progressText);
         this.mc.fontRenderer.drawStringWithShadow(progressText, (float)(screenWidth - progressTextWidth) / 2.0F, (float)(progressBarY + 10), -1);
      }
   }

   private void renderImmunityStatus(int screenWidth, int screenHeight) {
      if (RaidClientData.isBossImmune()) {
         String reason = RaidClientData.getImmunityReason();
         if (reason == null || reason.isEmpty()) {
            reason = "IMMUNE";
         }

         int barX = (screenWidth - 200) / 2;
         int immuneY = 67;
         int reasonWidth = this.mc.fontRenderer.getStringWidth(reason);
         this.mc.fontRenderer.drawStringWithShadow(reason, (float)(screenWidth - reasonWidth) / 2.0F, (float)immuneY, -7864065);
      }
   }

   private int getHealthBarColor() {
      int enrageLevel = RaidClientData.getEnrageLevel();
      if (enrageLevel >= 2) {
         return -65536;
      } else {
         return enrageLevel == 1 ? -30720 : -3407872;
      }
   }

   private int getDifficultyColor(RaidDifficulty diff) {
      switch (diff) {
         case GENIN:
            return -7829368;
         case CHUNIN:
            return -11141291;
         case JONIN:
            return -22016;
         case ANBU:
            return -43691;
         default:
            return -1;
      }
   }
}
