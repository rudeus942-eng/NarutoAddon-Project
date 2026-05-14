
package net.luck.narutoaddon.OtherCode.quest.gui;

import net.luck.narutoaddon.OtherCode.gui.GuiRankedMenu;
import net.luck.narutoaddon.OtherCode.quest.core.QuestModInit;
import net.luck.narutoaddon.OtherCode.quest.network.QuestActionMessage;
import net.luck.narutoaddon.OtherCode.quest.network.QuestClientData;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class QuestDialogGui extends GuiScreen {
   private static final int DIALOG_WIDTH = 340;
   private static final int DIALOG_HEIGHT = 220;
   private static final int COLOR_BORDER_OUTER = -7706054;
   private static final int COLOR_BORDER_INNER = -9809872;
   private static final int COLOR_BG_TOP = -298833888;
   private static final int COLOR_BG_BOT = -300280816;
   private static final int COLOR_TITLE = -1521552;
   private static final int COLOR_TEXT_BODY = -2832216;
   private static final int COLOR_SEPARATOR = -9807296;
   private static final int BUTTON_RESPONSE_BASE = 10;
   private int guiLeft;
   private int guiTop;
   private float animationTick = 0.0F;

   public void initGui() {
      this.guiLeft = (this.width - 340) / 2;
      this.guiTop = (this.height - 220) / 2;
      this.buttonList.clear();
      String[] responses = QuestClientData.getDialogResponses();
      if (responses != null) {
         int btnW = 290;
         int btnH = 22;
         int btnX = this.guiLeft + 25;
         int totalBtnHeight = responses.length * (btnH + 3);
         int btnStartY = this.guiTop + 220 - 14 - totalBtnHeight;
         int correctIdx = QuestClientData.getDialogCorrectIndex();
         boolean isQuiz = correctIdx != 0;

         for(int i = 0; i < responses.length; ++i) {
            int topCol;
            int botCol;
            if (isQuiz) {
               topCol = -12957078;
               botCol = -14009766;
            } else {
               topCol = i == correctIdx ? -12948934 : -12957078;
               botCol = i == correctIdx ? -14005718 : -14009766;
            }

            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(10 + i, btnX, btnStartY + i * (btnH + 3), btnW, btnH, responses[i], topCol, botCol));
         }
      }

   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      float glowIntensity = (float)(0.2 + 0.08 * Math.sin((double)this.animationTick));
      int glowAlpha = (int)(glowIntensity * 255.0F);
      drawRect(this.guiLeft - 4, this.guiTop - 4, this.guiLeft + 340 + 4, this.guiTop + 220 + 4, glowAlpha << 24 | 9071162);
      this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + 340, this.guiTop + 220, -298833888, -300280816);
      this.drawBorderWithShading(this.guiLeft, this.guiTop, 340, 220, -7706054, 2);
      this.drawBorderWithShading(this.guiLeft + 4, this.guiTop + 4, 332, 212, -9809872, 1);
      this.drawCornerDecorations();
      String npcName = QuestClientData.getDialogNpcName();
      if (npcName != null) {
         String displayName = "§l✦ " + npcName + " ✦";
         int nameWidth = this.fontRenderer.getStringWidth(displayName);
         this.fontRenderer.drawString(displayName, this.guiLeft + (340 - nameWidth) / 2 + 1, this.guiTop + 12 + 1, 1709072);
         this.fontRenderer.drawStringWithShadow(displayName, (float)(this.guiLeft + (340 - nameWidth) / 2), (float)(this.guiTop + 12), -1521552);
      }

      this.drawHorizontalLine(this.guiLeft + 20, this.guiLeft + 170 - 10, this.guiTop + 26, -9807296);
      this.drawHorizontalLine(this.guiLeft + 170 + 10, this.guiLeft + 340 - 20, this.guiTop + 26, -9807296);
      this.drawDiamond(this.guiLeft + 170, this.guiTop + 26, 5, -7706054);
      int textPanelX = this.guiLeft + 14;
      int textPanelY = this.guiTop + 32;
      int textPanelW = 312;
      String[] responses = QuestClientData.getDialogResponses();
      int responseArea = responses != null ? responses.length * 25 + 20 : 40;
      int textPanelH = 176 - responseArea;
      this.drawPanelWithLighting(textPanelX, textPanelY, textPanelW, textPanelH);
      String[] lines = QuestClientData.getDialogLines();
      if (lines != null) {
         int textY = textPanelY + 10;
         int textMaxWidth = textPanelW - 28;

         for(String line : lines) {
            if (line != null) {
               for(String wrappedLine : this.fontRenderer.listFormattedStringToWidth(line, textMaxWidth)) {
                  if (textY < textPanelY + textPanelH - 12) {
                     this.fontRenderer.drawStringWithShadow(wrappedLine, (float)(textPanelX + 14), (float)textY, -2832216);
                  }

                  textY += 11;
               }

               textY += 4;
            }
         }
      }

      int sepY = this.guiTop + 220 - responseArea - 8;
      this.drawHorizontalLine(this.guiLeft + 20, this.guiLeft + 340 - 20, sepY, -9807296);
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   public void updateScreen() {
      super.updateScreen();
      this.animationTick += 0.1F;
   }

   protected void actionPerformed(GuiButton button) {
      if (button.id >= 10) {
         int responseIndex = button.id - 10;
         QuestModInit.NETWORK.sendToServer(new QuestActionMessage(2, responseIndex));
         int correctIdx = QuestClientData.getDialogCorrectIndex();
         if (correctIdx < 0 || responseIndex == correctIdx) {
            QuestClientData.clearDialog();
            this.mc.displayGuiScreen((GuiScreen)null);
         }
      }

   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   private void drawPanelWithLighting(int x, int y, int w, int h) {
      this.drawGradientRect(x, y, x + w, y + h, -868600792, -870442476);
      drawRect(x + 1, y + 1, x + w - 1, y + 2, 872415231);
      drawRect(x + 1, y + 1, x + 2, y + h - 1, 587202559);
      drawRect(x + 1, y + h - 2, x + w - 1, y + h - 1, 1426063360);
      drawRect(x + w - 2, y + 1, x + w - 1, y + h - 1, 855638016);
      drawRect(x, y, x + w, y + 1, -11910088);
      drawRect(x, y + h - 1, x + w, y + h, -14015464);
      drawRect(x, y, x + 1, y + h, -11910088);
      drawRect(x + w - 1, y, x + w, y + h, -14015464);
   }

   private void drawBorderWithShading(int x, int y, int w, int h, int color, int thickness) {
      int bright = brightenColor(color, 35);
      int dark = darkenColor(color, 35);
      drawRect(x, y, x + w, y + thickness, bright);
      drawRect(x, y, x + thickness, y + h, bright);
      drawRect(x, y + h - thickness, x + w, y + h, dark);
      drawRect(x + w - thickness, y, x + w, y + h, dark);
   }

   private void drawDiamond(int cx, int cy, int size, int color) {
      drawRect(cx - 1, cy - size, cx + 1, cy - size + 2, color);
      drawRect(cx - 2, cy - size + 2, cx + 2, cy + size - 2, color);
      drawRect(cx - 1, cy + size - 2, cx + 1, cy + size, color);
   }

   private void drawCornerDecorations() {
      int size = 8;
      int offset = 8;
      int bright = brightenColor(9071162, 25) | -16777216;
      int dark = darkenColor(9071162, 25) | -16777216;
      drawRect(this.guiLeft + offset, this.guiTop + offset, this.guiLeft + offset + size, this.guiTop + offset + 2, bright);
      drawRect(this.guiLeft + offset, this.guiTop + offset, this.guiLeft + offset + 2, this.guiTop + offset + size, bright);
      drawRect(this.guiLeft + 340 - offset - size, this.guiTop + offset, this.guiLeft + 340 - offset, this.guiTop + offset + 2, bright);
      drawRect(this.guiLeft + 340 - offset - 2, this.guiTop + offset, this.guiLeft + 340 - offset, this.guiTop + offset + size, dark);
      drawRect(this.guiLeft + offset, this.guiTop + 220 - offset - 2, this.guiLeft + offset + size, this.guiTop + 220 - offset, dark);
      drawRect(this.guiLeft + offset, this.guiTop + 220 - offset - size, this.guiLeft + offset + 2, this.guiTop + 220 - offset, bright);
      drawRect(this.guiLeft + 340 - offset - size, this.guiTop + 220 - offset - 2, this.guiLeft + 340 - offset, this.guiTop + 220 - offset, dark);
      drawRect(this.guiLeft + 340 - offset - 2, this.guiTop + 220 - offset - size, this.guiLeft + 340 - offset, this.guiTop + 220 - offset, dark);
   }

   private static int brightenColor(int color, int amount) {
      int r = Math.min(255, (color >> 16 & 255) + amount);
      int g = Math.min(255, (color >> 8 & 255) + amount);
      int b = Math.min(255, (color & 255) + amount);
      return r << 16 | g << 8 | b;
   }

   private static int darkenColor(int color, int amount) {
      int r = Math.max(0, (color >> 16 & 255) - amount);
      int g = Math.max(0, (color >> 8 & 255) - amount);
      int b = Math.max(0, (color & 255) - amount);
      return r << 16 | g << 8 | b;
   }
}
