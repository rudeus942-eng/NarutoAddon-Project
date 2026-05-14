
package net.luck.narutoaddon.OtherCode.quest.gui;

import net.luck.narutoaddon.OtherCode.quest.network.QuestCombatClientData;
import net.luck.narutoaddon.OtherCode.quest.network.QuestCombatHealthMessage;
import net.luck.narutoaddon.OtherCode.raid.network.RaidClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class QuestCombatOverlay extends Gui {
   private final Minecraft mc = Minecraft.getMinecraft();
   private static final int COLOR_ITACHI_THEME = -3407872;
   private static final int COLOR_ITACHI_ACCENT = -13434880;
   private static final int COLOR_KISAME_THEME = -16750900;
   private static final int COLOR_KISAME_ACCENT = -16764058;
   private static final int COLOR_DEFAULT_THEME = -7829368;
   private static final int COLOR_DEFAULT_ACCENT = -12303292;
   private static final int BAR_HEIGHT = 12;
   private static final int HEALTH_BAR_Y_OFFSET = 30;

   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
      if (event.getType() == ElementType.ALL) {
         if (!RaidClientData.isInRaid()) {
            QuestCombatHealthMessage.EnemyHealthEntry[] enemies = QuestCombatClientData.getEnemies();
            if (enemies != null && enemies.length != 0) {
               ScaledResolution sr = new ScaledResolution(this.mc);
               int screenWidth = sr.getScaledWidth();
               GlStateManager.pushMatrix();
               GlStateManager.enableBlend();
               GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
               int count = enemies.length;
               if (count == 1) {
                  this.renderBar(enemies[0], screenWidth / 2 - 100, 30, 200);
               } else if (count == 2) {
                  int totalWidth = 380;
                  int startX = screenWidth / 2 - totalWidth / 2;
                  this.renderBar(enemies[0], startX, 30, 180);
                  this.renderBar(enemies[1], startX + 200, 30, 180);
               } else {
                  int barWidth = 160;
                  int gapX = 15;
                  int gapY = 36;
                  int totalWidth = barWidth * 2 + gapX;
                  int startX = screenWidth / 2 - totalWidth / 2;

                  for(int i = 0; i < count && i < 4; ++i) {
                     int col = i % 2;
                     int row = i / 2;
                     int x = startX + col * (barWidth + gapX);
                     int y = 30 + row * gapY;
                     this.renderBar(enemies[i], x, y, barWidth);
                  }
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

   private void renderBar(QuestCombatHealthMessage.EnemyHealthEntry entry, int x, int y, int barWidth) {
      int themeColor = entry.themeColor != 0 ? entry.themeColor : -7829368;
      int accentColor = entry.accentColor != 0 ? entry.accentColor : -12303292;
      String name = entry.entityName != null ? entry.entityName : "Enemy";
      int nameWidth = this.mc.fontRenderer.getStringWidth(name);
      int nameBgX = x + (barWidth - nameWidth) / 2 - 5;
      int nameBgY = y - 14;
      this.drawGradientRect(nameBgX, nameBgY, nameBgX + nameWidth + 10, nameBgY + 12, darkenColor(accentColor, 20) | -587202560, darkenColor(accentColor, 40) | -587202560);
      drawRect(nameBgX, nameBgY, nameBgX + nameWidth + 10, nameBgY + 1, brightenColor(accentColor, 40));
      this.mc.fontRenderer.drawStringWithShadow(name, (float)x + (float)(barWidth - nameWidth) / 2.0F, (float)(nameBgY + 2), themeColor);
      int outerDark = darkenColor(accentColor, 30) | -16777216;
      drawRect(x - 4, y - 4, x + barWidth + 4, y + 12 + 4, outerDark);
      drawRect(x - 3, y - 3, x + barWidth + 3, y + 12 + 3, accentColor);
      drawRect(x - 2, y - 2, x + barWidth + 2, y + 12 + 2, -16777216);
      this.drawGradientRect(x, y, x + barWidth, y + 12, darkenColor(accentColor, 10), darkenColor(accentColor, 30));
      float healthPercent = entry.maxHP > 0.0F ? Math.min(1.0F, entry.currentHP / entry.maxHP) : 0.0F;
      int fillWidth = (int)((float)barWidth * healthPercent);
      if (fillWidth > 0) {
         int barColorDark = darkenColor(themeColor, 60);
         this.drawGradientRect(x, y, x + fillWidth, y + 12, themeColor, barColorDark);
         int shimmerOffset = (int)(System.currentTimeMillis() / 50L % (long)barWidth);
         if (shimmerOffset < fillWidth) {
            int shimmerX = x + shimmerOffset;
            drawRect(shimmerX, y, Math.min(shimmerX + 3, x + fillWidth), y + 12, 872415231);
         }

         drawRect(x, y, x + fillWidth, y + 1, brightenColor(themeColor, 40));
      }

      drawRect(x - 6, y + 6 - 1, x - 2, y + 6 + 1, brightenColor(accentColor, 30));
      drawRect(x + barWidth + 2, y + 6 - 1, x + barWidth + 6, y + 6 + 1, brightenColor(accentColor, 30));
      int currentHpInt = (int)entry.currentHP;
      int maxHpInt = (int)entry.maxHP;
      int percentInt = (int)(healthPercent * 100.0F);
      String hpText = currentHpInt + " / " + maxHpInt + " (" + percentInt + "%)";
      int textWidth = this.mc.fontRenderer.getStringWidth(hpText);
      int textX = x + (barWidth - textWidth) / 2;
      int textY = y + (12 - this.mc.fontRenderer.FONT_HEIGHT) / 2 + 1;
      this.mc.fontRenderer.drawStringWithShadow(hpText, (float)textX, (float)textY, -1);
   }

   private static int darkenColor(int color, int amount) {
      int a = color >> 24 & 255;
      int r = Math.max(0, (color >> 16 & 255) - amount);
      int g = Math.max(0, (color >> 8 & 255) - amount);
      int b = Math.max(0, (color & 255) - amount);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static int brightenColor(int color, int amount) {
      int a = color >> 24 & 255;
      int r = Math.min(255, (color >> 16 & 255) + amount);
      int g = Math.min(255, (color >> 8 & 255) + amount);
      int b = Math.min(255, (color & 255) + amount);
      return a << 24 | r << 16 | g << 8 | b;
   }
}
