
package net.luck.narutoaddon.OtherCode.gui;

import net.luck.narutoaddon.OtherCode.InfTsukAddon;
import net.luck.narutoaddon.OtherCode.Rankedseason;
import net.luck.narutoaddon.OtherCode.network.RankedGuiActionMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiRankedTitles extends GuiScreen {
   private static final int GUI_WIDTH = 280;
   private static final int GUI_HEIGHT = 220;
   private static final int COLOR_BORDER_OUTER = -39424;
   private static final int COLOR_BORDER_INNER = -3386112;
   private static final int COLOR_BG_DARK = -300279270;
   private static final int COLOR_PANEL = -869651926;
   private static final int COLOR_GOLD = -10496;
   private static final int COLOR_SELECTED = -14505438;
   private List<String> collectedTitles = new ArrayList();
   private String activeTitle = "";
   private int currentPage = 0;
   private static final int TITLES_PER_PAGE = 5;
   private int guiLeft;
   private int guiTop;
   private float animationTick = 0.0F;
   private static final int BUTTON_BACK = 0;
   private static final int BUTTON_CLEAR_TITLE = 1;
   private static final int BUTTON_PREV_PAGE = 2;
   private static final int BUTTON_NEXT_PAGE = 3;
   private static final int BUTTON_TITLE_BASE = 100;

   public GuiRankedTitles() {
   }

   public GuiRankedTitles(List<String> titles, String active) {
      this.collectedTitles = titles != null ? new ArrayList(titles) : new ArrayList();
      this.activeTitle = active != null ? active : "";
      this.collectedTitles.sort((a, b) -> {
         int seasonA = this.extractSeasonNumber(a);
         int seasonB = this.extractSeasonNumber(b);
         return Integer.compare(seasonB, seasonA);
      });
   }

   public void updateData(List<String> titles, String active) {
      this.collectedTitles = titles != null ? new ArrayList(titles) : new ArrayList();
      this.activeTitle = active != null ? active : "";
      this.collectedTitles.sort((a, b) -> {
         int seasonA = this.extractSeasonNumber(a);
         int seasonB = this.extractSeasonNumber(b);
         return Integer.compare(seasonB, seasonA);
      });
      if (this.buttonList != null) {
         this.buttonList.clear();
         this.initGui();
      }

   }

   private int extractSeasonNumber(String title) {
      if (title.startsWith("S") && title.length() > 1) {
         int spaceIndex = title.indexOf(32);
         if (spaceIndex > 1) {
            try {
               return Integer.parseInt(title.substring(1, spaceIndex));
            } catch (NumberFormatException var4) {
               return 0;
            }
         }
      }

      return 0;
   }

   public void initGui() {
      this.guiLeft = (this.width - 280) / 2;
      this.guiTop = (this.height - 220) / 2;
      this.buttonList.clear();
      int centerX = this.guiLeft + 140;
      int startIndex = this.currentPage * 5;
      int endIndex = Math.min(startIndex + 5, this.collectedTitles.size());
      int buttonY = this.guiTop + 65;

      for(int i = startIndex; i < endIndex; ++i) {
         String title = (String)this.collectedTitles.get(i);
         boolean isSelected = title.equals(this.activeTitle);
         int btnColor1 = isSelected ? -14505438 : -13408598;
         int btnColor2 = isSelected ? -15636975 : -14531448;
         String displayText = (isSelected ? "✓ " : "  ") + title;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(100 + i, this.guiLeft + 20, buttonY, 240, 22, displayText, btnColor1, btnColor2));
         buttonY += 26;
      }

      int totalPages = Math.max(1, (this.collectedTitles.size() + 5 - 1) / 5);
      if (totalPages > 1) {
         if (this.currentPage > 0) {
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(2, this.guiLeft + 20, this.guiTop + 220 - 60, 50, 18, "<< Prev", -11184811, -13421773));
         }

         if (this.currentPage < totalPages - 1) {
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(3, this.guiLeft + 280 - 70, this.guiTop + 220 - 60, 50, 18, "Next >>", -11184811, -13421773));
         }
      }

      if (this.activeTitle != null && !this.activeTitle.isEmpty()) {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(1, centerX - 50, this.guiTop + 220 - 60, 100, 18, "Clear Title", -5627358, -10088175));
      }

      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(0, centerX - 40, this.guiTop + 220 - 32, 80, 20, "Back", -11184811, -13421773));
   }

   public void updateScreen() {
      super.updateScreen();
      this.animationTick += 0.1F;
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      float glowIntensity = (float)(0.3 + 0.15 * Math.sin((double)this.animationTick));
      int glowAlpha = (int)(glowIntensity * 255.0F);
      drawRect(this.guiLeft - 4, this.guiTop - 4, this.guiLeft + 280 + 4, this.guiTop + 220 + 4, glowAlpha << 24 | 16737792);
      this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + 280, this.guiTop + 220, -299226582, -301331958);
      this.drawBorder(this.guiLeft, this.guiTop, 280, 220, -39424);
      this.drawBorder(this.guiLeft + 3, this.guiTop + 3, 274, 214, -3386112);
      String title = "§6§l✦ MY TITLES ✦";
      int titleWidth = this.fontRenderer.getStringWidth(title);
      this.fontRenderer.drawStringWithShadow(title, (float)(this.guiLeft + (280 - titleWidth) / 2), (float)(this.guiTop + 12), -10496);
      String subtitle = "§7Collected: §e" + this.collectedTitles.size() + " titles";
      int subWidth = this.fontRenderer.getStringWidth(subtitle);
      this.fontRenderer.drawString(subtitle, this.guiLeft + (280 - subWidth) / 2, this.guiTop + 26, 10066329);
      this.drawPanelWithLighting(this.guiLeft + 15, this.guiTop + 40, 250, 20);
      String activeDisplay;
      if (this.activeTitle != null && !this.activeTitle.isEmpty()) {
         String coloredTitle = Rankedseason.getFormattedTitle(this.activeTitle);
         activeDisplay = "§7Active: " + coloredTitle;
      } else {
         activeDisplay = "§8No title selected";
      }

      int activeWidth = this.fontRenderer.getStringWidth(activeDisplay);
      this.fontRenderer.drawStringWithShadow(activeDisplay, (float)(this.guiLeft + (280 - activeWidth) / 2), (float)(this.guiTop + 46), 16777215);
      if (this.collectedTitles.isEmpty()) {
         this.drawPanelWithLighting(this.guiLeft + 20, this.guiTop + 70, 240, 60);
         String noTitles1 = "§7You haven't earned any titles yet!";
         int nt1Width = this.fontRenderer.getStringWidth(noTitles1);
         this.fontRenderer.drawString(noTitles1, this.guiLeft + (280 - nt1Width) / 2, this.guiTop + 82, 11184810);
         String noTitles2 = "§8Place in the §eTop 3§8 at season end";
         int nt2Width = this.fontRenderer.getStringWidth(noTitles2);
         this.fontRenderer.drawString(noTitles2, this.guiLeft + (280 - nt2Width) / 2, this.guiTop + 95, 8947848);
         String noTitles3 = "§8to earn exclusive titles!";
         int nt3Width = this.fontRenderer.getStringWidth(noTitles3);
         this.fontRenderer.drawString(noTitles3, this.guiLeft + (280 - nt3Width) / 2, this.guiTop + 108, 8947848);
      }

      if (this.collectedTitles.size() > 5) {
         int totalPages = (this.collectedTitles.size() + 5 - 1) / 5;
         String pageText = "§7Page " + (this.currentPage + 1) + "/" + totalPages;
         int pageWidth = this.fontRenderer.getStringWidth(pageText);
         this.fontRenderer.drawString(pageText, this.guiLeft + (280 - pageWidth) / 2, this.guiTop + 220 - 75, 11184810);
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private void drawPanelWithLighting(int x, int y, int w, int h) {
      this.drawGradientRect(x, y, x + w, y + h, -868599238, -870704614);
      drawRect(x + 1, y + 1, x + w - 1, y + 2, 1157627903);
      drawRect(x + 1, y + 1, x + 2, y + h - 1, 872415231);
      drawRect(x + 1, y + h - 2, x + w - 1, y + h - 1, 1711276032);
      drawRect(x + w - 2, y + 1, x + w - 1, y + h - 1, 1140850688);
      drawRect(x, y, x + w, y + 1, -11184811);
      drawRect(x, y + h - 1, x + w, y + h, -13421773);
      drawRect(x, y, x + 1, y + h, -11184811);
      drawRect(x + w - 1, y, x + w, y + h, -13421773);
   }

   private void drawBorder(int x, int y, int w, int h, int color) {
      this.drawHorizontalLine(x, x + w - 1, y, color);
      this.drawHorizontalLine(x, x + w - 1, y + h - 1, color);
      this.drawVerticalLine(x, y, y + h - 1, color);
      this.drawVerticalLine(x + w - 1, y, y + h - 1, color);
   }

   protected void actionPerformed(GuiButton button) {
      if (button.id == 0) {
         GuiRankedMenu.open();
      } else if (button.id == 1) {
         InfTsukAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("cleartitle"));
         this.activeTitle = "";
         this.buttonList.clear();
         this.initGui();
      } else if (button.id == 2) {
         if (this.currentPage > 0) {
            --this.currentPage;
            this.buttonList.clear();
            this.initGui();
         }

      } else if (button.id == 3) {
         int totalPages = (this.collectedTitles.size() + 5 - 1) / 5;
         if (this.currentPage < totalPages - 1) {
            ++this.currentPage;
            this.buttonList.clear();
            this.initGui();
         }

      } else {
         if (button.id >= 100) {
            int index = button.id - 100;
            if (index >= 0 && index < this.collectedTitles.size()) {
               String selectedTitle = (String)this.collectedTitles.get(index);
               if (selectedTitle.equals(this.activeTitle)) {
                  InfTsukAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("cleartitle"));
                  this.activeTitle = "";
               } else {
                  InfTsukAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("selecttitle:" + selectedTitle));
                  this.activeTitle = selectedTitle;
               }

               this.buttonList.clear();
               this.initGui();
            }
         }

      }
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   public static void open() {
      Minecraft mc = Minecraft.getMinecraft();
      mc.displayGuiScreen(new GuiRankedTitles());
      InfTsukAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("titles"));
   }
}
