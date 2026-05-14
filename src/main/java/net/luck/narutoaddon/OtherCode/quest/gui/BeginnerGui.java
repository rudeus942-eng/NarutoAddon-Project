
package net.luck.narutoaddon.OtherCode.quest.gui;

import net.luck.narutoaddon.OtherCode.quest.core.QuestModInit;
import net.luck.narutoaddon.OtherCode.quest.network.BeginnerActionMessage;
import net.luck.narutoaddon.OtherCode.quest.network.QuestClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class BeginnerGui extends GuiScreen {
   private static final int GUI_WIDTH = 360;
   private static final int GUI_HEIGHT = 280;
   private static final int COLOR_BG_TOP = -298833888;
   private static final int COLOR_BG_BOT = -300280816;
   private static final int COLOR_BORDER_OUTER = -7706054;
   private static final int COLOR_BORDER_INNER = -9809872;
   private static final int COLOR_PANEL_TOP = -868600792;
   private static final int COLOR_PANEL_BOT = -870442476;
   private static final int COLOR_TITLE = -1521552;
   private static final int COLOR_TEXT_BODY = -2832216;
   private static final int COLOR_TEXT_DIM = -7700886;
   private static final int COLOR_SEPARATOR = -9807296;
   private static final int BUTTON_BECOME_SHINOBI = 0;
   private static final int BUTTON_VILLAGE_LEAF = 1;
   private static final int BUTTON_VILLAGE_SAND = 2;
   private static final int BUTTON_VILLAGE_MIST = 3;
   private static final int BUTTON_VILLAGE_CLOUD = 4;
   private static final int BUTTON_VILLAGE_STONE = 5;
   private static final int BUTTON_VILLAGE_RAIN = 6;
   private int stage = 0;
   private boolean actionSent = false;
   private int guiLeft;
   private int guiTop;
   private float animationTick = 0.0F;

   public static void open() {
      Minecraft mc = Minecraft.getMinecraft();
      mc.displayGuiScreen(new BeginnerGui());
   }

   public void updateScreen() {
      this.animationTick += 0.1F;
   }

   public void initGui() {
      this.guiLeft = (this.width - 360) / 2;
      this.guiTop = (this.height - 280) / 2;
      this.buttonList.clear();
      this.actionSent = false;
      if (this.stage == 0) {
         this.buttonList.add(new GradientButton(0, this.guiLeft + 180 - 80, this.guiTop + 280 - 60, 160, 26, "§lBecome a Shinobi", -7706086, -9811440));
      } else if (this.stage == 1) {
         int btnW = 130;
         int btnH = 36;
         int gapX = 16;
         int gapY = 12;
         int gridLeft = this.guiLeft + (360 - (2 * btnW + gapX)) / 2;
         int gridTop = this.guiTop + 90;
         String[] names = new String[]{"§2Leaf Village §a(Konoha)", "§eSand Village §6(Suna)", "§3Mist Village §b(Kiri)", "§7Cloud Village §8(Kumo)", "§6Stone Village §e(Iwa)", "§1Rain Village §9(Ame)"};
         int[][] colors = new int[][]{{-15050214, -15910387}, {-9807344, -11912696}, {-15058342, -15916992}, {-12961206, -14540240}, {-10864102, -12966896}, {-15066534, -15724480}};
         int[] ids = new int[]{1, 2, 3, 4, 5, 6};
         int idx = 0;

         for(int row = 0; row < 3; ++row) {
            for(int col = 0; col < 2; ++col) {
               int x = gridLeft + col * (btnW + gapX);
               int y = gridTop + row * (btnH + gapY);
               this.buttonList.add(new GradientButton(ids[idx], x, y, btnW, btnH, names[idx], colors[idx][0], colors[idx][1]));
               ++idx;
            }
         }
      }

   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      float glowIntensity = (float)(0.15 + 0.06 * Math.sin((double)this.animationTick));
      int glowAlpha = (int)(glowIntensity * 255.0F);
      drawRect(this.guiLeft - 5, this.guiTop - 5, this.guiLeft + 360 + 5, this.guiTop + 280 + 5, glowAlpha << 24 | 9071162);
      int bright = brightenColor(9071162, 35) | -16777216;
      int dark = darkenColor(9071162, 35) | -16777216;
      drawRect(this.guiLeft - 3, this.guiTop - 3, this.guiLeft + 360 + 3, this.guiTop - 2, bright);
      drawRect(this.guiLeft - 3, this.guiTop - 3, this.guiLeft - 2, this.guiTop + 280 + 3, bright);
      drawRect(this.guiLeft - 3, this.guiTop + 280 + 2, this.guiLeft + 360 + 3, this.guiTop + 280 + 3, dark);
      drawRect(this.guiLeft + 360 + 2, this.guiTop - 3, this.guiLeft + 360 + 3, this.guiTop + 280 + 3, dark);
      drawRect(this.guiLeft - 2, this.guiTop - 2, this.guiLeft + 360 + 2, this.guiTop + 280 + 2, -9809872);
      this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + 360, this.guiTop + 280, -298833888, -300280816);
      drawRect(this.guiLeft + 1, this.guiTop + 1, this.guiLeft + 360 - 1, this.guiTop + 3, 587202559);
      drawRect(this.guiLeft + 1, this.guiTop + 1, this.guiLeft + 2, this.guiTop + 280 - 1, 301989887);
      drawRect(this.guiLeft + 1, this.guiTop + 280 - 3, this.guiLeft + 360 - 1, this.guiTop + 280 - 1, 855638016);
      if (this.stage == 0) {
         this.drawWelcomeStage(mouseX, mouseY);
      } else if (this.stage == 1) {
         this.drawVillageSelectStage(mouseX, mouseY);
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private void drawWelcomeStage(int mouseX, int mouseY) {
      int centerX = this.guiLeft + 180;
      int panelX = this.guiLeft + 20;
      int panelY = this.guiTop + 18;
      int panelW = 320;
      int panelH = 50;
      this.drawGradientRect(panelX, panelY, panelX + panelW, panelY + panelH, -868600792, -870442476);
      drawRect(panelX, panelY, panelX + panelW, panelY + 1, -11910088);
      drawRect(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, -14015464);
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)centerX, (float)(panelY + 8), 0.0F);
      GlStateManager.scale(1.5F, 1.5F, 1.0F);
      this.drawCenteredString(this.fontRenderer, "Welcome to IceeRamen:", 0, 0, -1521552);
      this.drawCenteredString(this.fontRenderer, "Infinite Tsukuyomi Chapter 2!", 0, 12, -1521552);
      GlStateManager.popMatrix();
      int sepY = this.guiTop + 78;
      drawRect(this.guiLeft + 30, sepY, this.guiLeft + 360 - 30, sepY + 1, -9807296);
      int bodyPanelY = this.guiTop + 88;
      int bodyPanelH = 100;
      this.drawGradientRect(panelX, bodyPanelY, panelX + panelW, bodyPanelY + bodyPanelH, 1144006688, 1142952976);
      int bodyY = bodyPanelY + 12;
      this.drawCenteredString(this.fontRenderer, "Your journey as a shinobi begins here.", centerX, bodyY, -2832216);
      this.drawCenteredString(this.fontRenderer, "", centerX, bodyY + 16, -2832216);
      this.drawCenteredString(this.fontRenderer, "You will receive starter equipment,", centerX, bodyY + 30, -2832216);
      this.drawCenteredString(this.fontRenderer, "ninja training, and choose your village.", centerX, bodyY + 44, -2832216);
      this.drawCenteredString(this.fontRenderer, "", centerX, bodyY + 58, -2832216);
      this.drawCenteredString(this.fontRenderer, "§7Press the button below to begin.", centerX, bodyY + 72, -7700886);
   }

   private void drawVillageSelectStage(int mouseX, int mouseY) {
      int centerX = this.guiLeft + 180;
      int panelX = this.guiLeft + 20;
      int panelY = this.guiTop + 15;
      int panelW = 320;
      int panelH = 35;
      this.drawGradientRect(panelX, panelY, panelX + panelW, panelY + panelH, -868600792, -870442476);
      drawRect(panelX, panelY, panelX + panelW, panelY + 1, -11910088);
      drawRect(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, -14015464);
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)centerX, (float)(panelY + 8), 0.0F);
      GlStateManager.scale(1.5F, 1.5F, 1.0F);
      this.drawCenteredString(this.fontRenderer, "Choose Your Village", 0, 0, -1521552);
      GlStateManager.popMatrix();
      int sepY = this.guiTop + 58;
      drawRect(this.guiLeft + 30, sepY, this.guiLeft + 360 - 30, sepY + 1, -9807296);
      this.drawCenteredString(this.fontRenderer, "This choice determines your headband, village, and allies.", centerX, this.guiTop + 66, -7700886);
      this.drawCenteredString(this.fontRenderer, "Choose wisely — this cannot be changed easily!", centerX, this.guiTop + 78, -5608902);
      this.drawCenteredString(this.fontRenderer, "§8Your village awaits, young shinobi.", centerX, this.guiTop + 280 - 30, -10858432);
   }

   protected void actionPerformed(GuiButton button) throws IOException {
      if (!this.actionSent) {
         if (button.id == 0 && this.stage == 0) {
            this.actionSent = true;
            QuestModInit.NETWORK.sendToServer(new BeginnerActionMessage((byte)0));
            this.stage = 1;
            this.actionSent = false;
            this.initGui();
         } else if (button.id >= 1 && button.id <= 6 && this.stage == 1) {
            this.actionSent = true;
            byte action = (byte)button.id;
            QuestModInit.NETWORK.sendToServer(new BeginnerActionMessage(action));
            QuestClientData.setBeginnerComplete(true);
            this.mc.displayGuiScreen((GuiScreen)null);
         }

      }
   }

   public boolean doesGuiPauseGame() {
      return false;
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

   @SideOnly(Side.CLIENT)
   private static class GradientButton extends GuiButton {
      private final int colorTop;
      private final int colorBottom;

      public GradientButton(int id, int x, int y, int w, int h, String text, int colorTop, int colorBottom) {
         super(id, x, y, w, h, text);
         this.colorTop = colorTop;
         this.colorBottom = colorBottom;
      }

      public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
         if (this.visible) {
            boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int topColor = hovered ? brightenColor(this.colorTop, 40) : this.colorTop;
            int bottomColor = hovered ? brightenColor(this.colorBottom, 40) : this.colorBottom;
            int borderBright = brightenColor(this.colorTop, hovered ? 100 : 60) | -16777216;
            int borderDark = (hovered ? brightenColor(this.colorBottom, 30) : -13421773) | -16777216;
            drawRect(this.x, this.y, this.x + this.width, this.y + 1, borderBright);
            drawRect(this.x, this.y, this.x + 1, this.y + this.height, borderBright);
            drawRect(this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, borderDark);
            drawRect(this.x + this.width - 1, this.y, this.x + this.width, this.y + this.height, borderDark);
            this.drawGradientRect(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, topColor | -587202560, bottomColor | -587202560);
            int highlightAlpha = hovered ? 85 : 51;
            drawRect(this.x + 2, this.y + 2, this.x + this.width - 2, this.y + 4, highlightAlpha << 24 | 16777215);
            drawRect(this.x + 2, this.y + 2, this.x + 3, this.y + this.height - 2, highlightAlpha / 2 << 24 | 16777215);
            drawRect(this.x + 2, this.y + this.height - 4, this.x + this.width - 2, this.y + this.height - 2, 1140850688);
            if (hovered) {
               drawRect(this.x + 3, this.y + 4, this.x + this.width - 3, this.y + 5, 587202559);
            }

            String text = this.displayString;
            mc.fontRenderer.drawStringWithShadow(text, (float)this.x + (float)(this.width - mc.fontRenderer.getStringWidth(text)) / 2.0F, (float)this.y + (float)(this.height - 8) / 2.0F, -1);
         }
      }

      private static int brightenColor(int color, int amount) {
         int r = Math.min(255, (color >> 16 & 255) + amount);
         int g = Math.min(255, (color >> 8 & 255) + amount);
         int b = Math.min(255, (color & 255) + amount);
         return r << 16 | g << 8 | b;
      }
   }
}
