
package net.luck.narutoaddon.OtherCode.quest.pvp.tournament;

import net.luck.narutoaddon.OtherCode.quest.gui.QuestLogGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TournamentRewardGui extends GuiContainer {
   private final int place;
   private final int borderColor;
   private final int accentColor;
   private final int titleColor;
   private final int slotBgColor;
   private final String titleText;
   private static final int COLOR_BG_TOP = -298833888;
   private static final int COLOR_BG_BOT = -300280816;
   private static final int COLOR_LABEL = -4675464;
   private static final int COLOR_SEPARATOR = 1728053247;

   public TournamentRewardGui(TournamentRewardContainer container, int place) {
      super(container);
      this.place = place;
      this.xSize = 176;
      this.ySize = 133;
      switch (place) {
         case 1:
            this.borderColor = -2840064;
            this.accentColor = -10496;
            this.titleColor = -10496;
            this.slotBgColor = 1087678976;
            this.titleText = "§6* 1st Place Rewards *";
            break;
         case 2:
            this.borderColor = -4144960;
            this.accentColor = -2039584;
            this.titleColor = -2039584;
            this.slotBgColor = 1086374080;
            this.titleText = "§7- 2nd Place Rewards -";
            break;
         case 3:
            this.borderColor = -3309774;
            this.accentColor = -4684277;
            this.titleColor = -4684277;
            this.slotBgColor = 1087209266;
            this.titleText = "§c- 3rd Place Rewards -";
            break;
         default:
            this.borderColor = -7706054;
            this.accentColor = -1521552;
            this.titleColor = -1521552;
            this.slotBgColor = 1079334229;
            this.titleText = "Set Rewards";
      }

   }

   public void initGui() {
      super.initGui();
      this.buttonList.add(new GuiButton(0, this.guiLeft + this.xSize / 2 - 30, this.guiTop + this.ySize + 4, 60, 16, "§7◀ Back"));
   }

   protected void actionPerformed(GuiButton button) {
      if (button.id == 0) {
         this.mc.player.closeScreen();
         QuestLogGui.openToTournament();
      }

   }

   protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
      int x = this.guiLeft;
      int y = this.guiTop;
      Gui.drawRect(x, y, x + this.xSize, y + this.ySize, this.borderColor);
      this.drawGradientRect(x + 1, y + 1, x + this.xSize - 1, y + this.ySize - 1, -298833888, -300280816);
      this.drawHollowRect(x + 2, y + 2, this.xSize - 4, this.ySize - 4, 1157627903);
      Gui.drawRect(x + 4, y + 15, x + this.xSize - 4, y + 16, this.accentColor);

      for(int i = 0; i < 9; ++i) {
         int sx = x + 7 + i * 18;
         int sy = y + 19;
         Gui.drawRect(sx, sy, sx + 18, sy + 18, this.slotBgColor);
         this.drawHollowRect(sx, sy, 18, 18, 1140850688);
      }

      Gui.drawRect(x + 4, y + 40, x + this.xSize - 4, y + 41, 1728053247);

      for(int row = 0; row < 3; ++row) {
         for(int col = 0; col < 9; ++col) {
            int sx = x + 7 + col * 18;
            int sy = y + 50 + row * 18;
            Gui.drawRect(sx, sy, sx + 18, sy + 18, 822083583);
            this.drawHollowRect(sx, sy, 18, 18, 1140850688);
         }
      }

      Gui.drawRect(x + 4, y + 105, x + this.xSize - 4, y + 106, 872415231);

      for(int i = 0; i < 9; ++i) {
         int sx = x + 7 + i * 18;
         int sy = y + 108;
         Gui.drawRect(sx, sy, sx + 18, sy + 18, 822083583);
         this.drawHollowRect(sx, sy, 18, 18, 1140850688);
      }

   }

   protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
      this.fontRenderer.drawString(this.titleText, this.xSize / 2 - this.fontRenderer.getStringWidth(this.titleText) / 2, 5, this.titleColor);
      this.fontRenderer.drawString("Your Inventory", 7, 42, -4675464);
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      super.drawScreen(mouseX, mouseY, partialTicks);
      this.renderHoveredToolTip(mouseX, mouseY);
   }

   private void drawHollowRect(int x, int y, int w, int h, int color) {
      Gui.drawRect(x, y, x + w, y + 1, color);
      Gui.drawRect(x, y + h - 1, x + w, y + h, color);
      Gui.drawRect(x, y + 1, x + 1, y + h - 1, color);
      Gui.drawRect(x + w - 1, y + 1, x + w, y + h - 1, color);
   }
}
