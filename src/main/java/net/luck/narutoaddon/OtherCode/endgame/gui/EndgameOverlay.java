
package net.luck.narutoaddon.OtherCode.endgame.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class EndgameOverlay extends Gui {
   private final Minecraft mc = Minecraft.getMinecraft();
   private static final int BAR_WIDTH = 200;
   private static final int BAR_HEIGHT = 12;
   private static final int BAR_SPACING = 20;
   private static final int BOSS_BAR_Y_START = 30;
   private static final long STALE_TICKS = 60L;
   private static final int COLOR_BG = Integer.MIN_VALUE;
   private static final int COLOR_HP_GREEN = -16724992;
   private static final int COLOR_HP_YELLOW = -3355648;
   private static final int COLOR_HP_RED = -3407872;
   private static final int COLOR_WHITE = -1;
   private static final int COLOR_DEFAULT_THEME = -3394765;
   private static final int COLOR_DEFAULT_ACCENT = -11193549;

   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
      if (event.getType() == ElementType.ALL) {
         if (this.mc.player != null) {
            EndgameClientData data = EndgameClientData.getInstance();
            boolean hasBars = !data.getCombatBars().isEmpty();
            boolean hasDefense = data.isDefenseActive();
            if (hasBars || hasDefense) {
               ScaledResolution sr = new ScaledResolution(this.mc);
               int screenWidth = sr.getScaledWidth();
               GlStateManager.pushMatrix();
               GlStateManager.enableBlend();
               GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
               int nextY = 30;
               if (hasBars) {
                  nextY = this.renderBossHealthBars(data, screenWidth, nextY);
               }

               if (hasDefense) {
                  this.renderDefenseStatus(data, screenWidth);
               }

               if (hasDefense) {
                  this.renderWaveCounter(data, screenWidth, nextY);
               }

               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               GlStateManager.disableBlend();
               GlStateManager.enableAlpha();
               this.mc.getTextureManager().bindTexture(Gui.ICONS);
               GlStateManager.popMatrix();
            }
         }
      }
   }

   private int renderBossHealthBars(EndgameClientData data, int screenWidth, int startY) {
      List<EndgameClientData.CombatBarData> bars = data.getCombatBars();
      long currentTick = this.mc.world != null ? this.mc.world.getTotalWorldTime() : 0L;
      List<EndgameClientData.CombatBarData> activeBars = new ArrayList();

      for(EndgameClientData.CombatBarData bar : bars) {
         if (currentTick - bar.lastUpdateTick <= 60L) {
            activeBars.add(bar);
         }
      }

      int count = activeBars.size();
      if (count == 0) {
         return startY;
      } else if (count == 1) {
         this.renderSingleBossBar((EndgameClientData.CombatBarData)activeBars.get(0), screenWidth / 2 - 100, startY);
         return startY + 20;
      } else if (count == 2) {
         int barW = 180;
         int totalWidth = barW * 2 + 20;
         int startX = screenWidth / 2 - totalWidth / 2;
         this.renderSingleBossBar((EndgameClientData.CombatBarData)activeBars.get(0), startX, startY);
         this.renderSingleBossBar((EndgameClientData.CombatBarData)activeBars.get(1), startX + barW + 20, startY);
         return startY + 20;
      } else {
         int barW = 160;
         int gapX = 15;
         int gapY = 36;
         int totalWidth = barW * 2 + gapX;
         int startX = screenWidth / 2 - totalWidth / 2;
         int maxY = startY;

         for(int i = 0; i < Math.min(count, 4); ++i) {
            int col = i % 2;
            int row = i / 2;
            int x = startX + col * (barW + gapX);
            int y = startY + row * gapY;
            this.renderSingleBossBar((EndgameClientData.CombatBarData)activeBars.get(i), x, y);
            maxY = Math.max(maxY, y + 20);
         }

         return maxY;
      }
   }

   private void renderSingleBossBar(EndgameClientData.CombatBarData bar, int x, int y) {
      int themeColor = bar.themeColor != 0 ? bar.themeColor : -3394765;
      int accentColor = bar.accentColor != 0 ? bar.accentColor : -11193549;
      String name = bar.entityName != null && !bar.entityName.isEmpty() ? bar.entityName : "Boss";
      if (bar.phase > 1) {
         name = name + " §7- Phase " + bar.phase;
      }

      int nameWidth = this.mc.fontRenderer.getStringWidth(name);
      int nameBgX = x + (200 - nameWidth) / 2 - 5;
      int nameBgY = y - 14;
      this.drawGradientRect(nameBgX, nameBgY, nameBgX + nameWidth + 10, nameBgY + 12, darkenColor(accentColor, 20) | -587202560, darkenColor(accentColor, 40) | -587202560);
      drawRect(nameBgX, nameBgY, nameBgX + nameWidth + 10, nameBgY + 1, brightenColor(accentColor, 40));
      this.mc.fontRenderer.drawStringWithShadow(name, (float)x + (float)(200 - nameWidth) / 2.0F, (float)(nameBgY + 2), themeColor);
      int outerDark = darkenColor(accentColor, 30) | -16777216;
      drawRect(x - 4, y - 4, x + 200 + 4, y + 12 + 4, outerDark);
      drawRect(x - 3, y - 3, x + 200 + 3, y + 12 + 3, accentColor);
      drawRect(x - 2, y - 2, x + 200 + 2, y + 12 + 2, -16777216);
      this.drawGradientRect(x, y, x + 200, y + 12, darkenColor(accentColor, 10), darkenColor(accentColor, 30));
      float hpPercent = bar.maxHP > 0.0F ? MathHelper.clamp(bar.currentHP / bar.maxHP, 0.0F, 1.0F) : 0.0F;
      int fillWidth = (int)(200.0F * hpPercent);
      if (fillWidth > 0) {
         int barColorDark = darkenColor(themeColor, 60);
         this.drawGradientRect(x, y, x + fillWidth, y + 12, themeColor, barColorDark);
         int shimmerOffset = (int)(System.currentTimeMillis() / 50L % 200L);
         if (shimmerOffset < fillWidth) {
            int shimmerX = x + shimmerOffset;
            drawRect(shimmerX, y, Math.min(shimmerX + 3, x + fillWidth), y + 12, 872415231);
         }

         drawRect(x, y, x + fillWidth, y + 1, brightenColor(themeColor, 40));
      }

      drawRect(x - 6, y + 6 - 1, x - 2, y + 6 + 1, brightenColor(accentColor, 30));
      drawRect(x + 200 + 2, y + 6 - 1, x + 200 + 6, y + 6 + 1, brightenColor(accentColor, 30));
      int hpInt = (int)bar.currentHP;
      int maxInt = (int)bar.maxHP;
      int percentInt = (int)(hpPercent * 100.0F);
      String hpText = hpInt + " / " + maxInt + " (" + percentInt + "%)";
      int textWidth = this.mc.fontRenderer.getStringWidth(hpText);
      int textX = x + (200 - textWidth) / 2;
      int textY = y + (12 - this.mc.fontRenderer.FONT_HEIGHT) / 2 + 1;
      this.mc.fontRenderer.drawStringWithShadow(hpText, (float)textX, (float)textY, -1);
   }

   private void renderDefenseStatus(EndgameClientData data, int screenWidth) {
      String text = "§b⛨ Village Defense: §r" + data.getDefenseVillage();
      int textWidth = this.mc.fontRenderer.getStringWidth(text);
      int x = screenWidth - textWidth - 5;
      int y = 5;
      drawRect(x - 3, y - 2, screenWidth - 2, y + 11, Integer.MIN_VALUE);
      this.mc.fontRenderer.drawStringWithShadow(text, (float)x, (float)y, -1);
   }

   private int renderWaveCounter(EndgameClientData data, int screenWidth, int startY) {
      int y = startY + 4;
      String waveText = "Wave " + data.getDefenseWave() + "/" + data.getDefenseMaxWaves();
      int waveWidth = this.mc.fontRenderer.getStringWidth(waveText);
      int waveX = screenWidth / 2 - waveWidth / 2;
      this.mc.fontRenderer.drawStringWithShadow(waveText, (float)waveX, (float)y, -1);
      y += 12;
      String hpLabel = "Village HP: " + data.getDefenseVillageHP() + "/" + data.getDefenseMaxHP();
      int labelWidth = this.mc.fontRenderer.getStringWidth(hpLabel);
      int labelX = screenWidth / 2 - labelWidth / 2;
      this.mc.fontRenderer.drawStringWithShadow(hpLabel, (float)labelX, (float)y, -1);
      y += 10;
      int villageBarWidth = 120;
      int barX = screenWidth / 2 - villageBarWidth / 2;
      drawRect(barX - 1, y - 1, barX + villageBarWidth + 1, y + 7, -16777216);
      drawRect(barX, y, barX + villageBarWidth, y + 6, Integer.MIN_VALUE);
      float villageHpPct = data.getDefenseMaxHP() > 0 ? MathHelper.clamp((float)data.getDefenseVillageHP() / (float)data.getDefenseMaxHP(), 0.0F, 1.0F) : 0.0F;
      int vFillWidth = (int)((float)villageBarWidth * villageHpPct);
      if (vFillWidth > 0) {
         drawRect(barX, y, barX + vFillWidth, y + 6, getHpColor(villageHpPct));
      }

      y += 10;
      return y;
   }

   private static int getHpColor(float percent) {
      if (percent > 0.5F) {
         return -16724992;
      } else {
         return percent > 0.25F ? -3355648 : -3407872;
      }
   }

   private static int brightenColor(int color, int amount) {
      int a = color >> 24 & 255;
      int r = Math.min(255, (color >> 16 & 255) + amount);
      int g = Math.min(255, (color >> 8 & 255) + amount);
      int b = Math.min(255, (color & 255) + amount);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static int darkenColor(int color, int amount) {
      int a = color >> 24 & 255;
      int r = Math.max(0, (color >> 16 & 255) - amount);
      int g = Math.max(0, (color >> 8 & 255) - amount);
      int b = Math.max(0, (color & 255) - amount);
      return a << 24 | r << 16 | g << 8 | b;
   }
}
