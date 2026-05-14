
package net.luck.narutoaddon.OtherCode.gui;

import net.luck.narutoaddon.OtherCode.luckAddonAddon;
import net.luck.narutoaddon.OtherCode.network.RankedGuiActionMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiRankedLeaderboard extends GuiScreen {
   private static final int GUI_WIDTH = 320;
   private static final int GUI_HEIGHT = 260;
   private static final int COLOR_BORDER = -10496;
   private static final int COLOR_BG = -300279270;
   private static final int COLOR_PANEL = -869651926;
   private static final int COLOR_ROW_ODD = 1144206131;
   private static final int COLOR_ROW_EVEN = 1143087650;
   private static final int COLOR_ROW_SELF = 1157588480;
   private List<LeaderboardEntry> entries = new ArrayList();
   private int currentPage = 1;
   private int totalPages = 1;
   private int playerRank = 0;
   private String playerUUID = "";
   private int guiLeft;
   private int guiTop;
   private static final int BUTTON_BACK = 0;
   private static final int BUTTON_PREV = 1;
   private static final int BUTTON_NEXT = 2;

   public void updateData(List<LeaderboardEntry> entries, int page, int totalPages, int playerRank, String playerUUID) {
      this.entries = entries;
      this.currentPage = page;
      this.totalPages = totalPages;
      this.playerRank = playerRank;
      this.playerUUID = playerUUID;
      this.updateButtonStates();
   }

   private void updateButtonStates() {
      if (this.buttonList != null && !this.buttonList.isEmpty()) {
         for(GuiButton button : this.buttonList) {
            if (button.id == 1) {
               button.enabled = this.currentPage > 1;
            } else if (button.id == 2) {
               button.enabled = this.currentPage < this.totalPages;
            }
         }

      }
   }

   public void initGui() {
      this.guiLeft = (this.width - 320) / 2;
      this.guiTop = (this.height - 260) / 2;
      this.buttonList.clear();
      int centerX = this.guiLeft + 160;
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(1, this.guiLeft + 15, this.guiTop + 260 - 28, 60, 20, "< Prev", -12303292, -14540254));
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(0, centerX - 40, this.guiTop + 260 - 28, 80, 20, "Back", -11184811, -13421773));
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(2, this.guiLeft + 320 - 75, this.guiTop + 260 - 28, 60, 20, "Next >", -12303292, -14540254));
      if (this.currentPage <= 1) {
         ((GuiButton)this.buttonList.get(0)).enabled = false;
      }

      if (this.currentPage >= this.totalPages) {
         ((GuiButton)this.buttonList.get(2)).enabled = false;
      }

      luckAddonAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("leaderboard:" + this.currentPage));
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      drawRect(this.guiLeft, this.guiTop, this.guiLeft + 320, this.guiTop + 260, -300279270);
      this.drawBorder(this.guiLeft, this.guiTop, 320, 260, -10496);
      this.drawBorder(this.guiLeft + 3, this.guiTop + 3, 314, 254, -3364352);
      String title = "§6§l✦ SHINOBI RANKINGS ✦";
      int titleWidth = this.fontRenderer.getStringWidth(title);
      this.fontRenderer.drawStringWithShadow(title, (float)(this.guiLeft + (320 - titleWidth) / 2), (float)(this.guiTop + 10), 16777215);
      String pageInfo = "§7Page " + this.currentPage + " of " + this.totalPages;
      int pageWidth = this.fontRenderer.getStringWidth(pageInfo);
      this.fontRenderer.drawStringWithShadow(pageInfo, (float)(this.guiLeft + (320 - pageWidth) / 2), (float)(this.guiTop + 23), 16777215);
      this.drawHorizontalLine(this.guiLeft + 15, this.guiLeft + 320 - 15, this.guiTop + 34, -10496);
      int headerY = this.guiTop + 40;
      this.fontRenderer.drawStringWithShadow("§e#", (float)(this.guiLeft + 20), (float)headerY, 16777215);
      this.fontRenderer.drawStringWithShadow("§eName", (float)(this.guiLeft + 45), (float)headerY, 16777215);
      this.fontRenderer.drawStringWithShadow("§eRank", (float)(this.guiLeft + 130), (float)headerY, 16777215);
      this.fontRenderer.drawStringWithShadow("§eELO", (float)(this.guiLeft + 210), (float)headerY, 16777215);
      this.fontRenderer.drawStringWithShadow("§eW/L", (float)(this.guiLeft + 260), (float)headerY, 16777215);
      int startY = this.guiTop + 54;
      int rowHeight = 16;
      if (this.entries.isEmpty()) {
         String loading = "§7Loading...";
         int loadWidth = this.fontRenderer.getStringWidth(loading);
         this.fontRenderer.drawStringWithShadow(loading, (float)(this.guiLeft + (320 - loadWidth) / 2), (float)(startY + 40), 16777215);
      } else {
         for(int i = 0; i < this.entries.size() && i < 10; ++i) {
            LeaderboardEntry entry = (LeaderboardEntry)this.entries.get(i);
            int rowY = startY + i * rowHeight;
            boolean isPlayer = entry.uuid.equals(this.playerUUID);
            int rowColor = isPlayer ? 1157588480 : (i % 2 == 0 ? 1143087650 : 1144206131);
            drawRect(this.guiLeft + 15, rowY - 2, this.guiLeft + 320 - 15, rowY + rowHeight - 2, rowColor);
            String rankColor = "§f";
            if (entry.rank == 1) {
               rankColor = "§6§l";
            } else if (entry.rank == 2) {
               rankColor = "§7§l";
            } else if (entry.rank == 3) {
               rankColor = "§c§l";
            } else if (entry.rank <= 10) {
               rankColor = "§e";
            }

            this.fontRenderer.drawStringWithShadow(rankColor + entry.rank, (float)(this.guiLeft + 20), (float)rowY, 16777215);
            String name = entry.name;
            if (name.length() > 12) {
               name = name.substring(0, 10) + "..";
            }

            String nameColor = isPlayer ? "§6" : "§f";
            this.fontRenderer.drawStringWithShadow(nameColor + name, (float)(this.guiLeft + 45), (float)rowY, 16777215);
            this.fontRenderer.drawStringWithShadow(GuiRankedMenu.makeBold(entry.rankTier), (float)(this.guiLeft + 130), (float)rowY, 16777215);
            this.fontRenderer.drawStringWithShadow("§e" + entry.elo, (float)(this.guiLeft + 210), (float)rowY, 16777215);
            this.fontRenderer.drawStringWithShadow("§a" + entry.wins + "§7/§c" + entry.losses, (float)(this.guiLeft + 260), (float)rowY, 16777215);
         }
      }

      drawRect(this.guiLeft + 15, this.guiTop + 260 - 50, this.guiLeft + 320 - 15, this.guiTop + 260 - 35, -869651926);
      String yourRank;
      if (this.playerRank > 0) {
         yourRank = "§7Your Rank: §6§l#" + this.playerRank;
      } else {
         yourRank = "§8Complete placements to appear on leaderboard";
      }

      int yourWidth = this.fontRenderer.getStringWidth(yourRank);
      this.fontRenderer.drawStringWithShadow(yourRank, (float)(this.guiLeft + (320 - yourWidth) / 2), (float)(this.guiTop + 260 - 46), 16777215);
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private void drawBorder(int x, int y, int w, int h, int color) {
      this.drawHorizontalLine(x, x + w - 1, y, color);
      this.drawHorizontalLine(x, x + w - 1, y + h - 1, color);
      this.drawVerticalLine(x, y, y + h - 1, color);
      this.drawVerticalLine(x + w - 1, y, y + h - 1, color);
   }

   protected void actionPerformed(GuiButton button) {
      switch (button.id) {
         case 0:
            GuiRankedMenu.open();
            break;
         case 1:
            if (this.currentPage > 1) {
               --this.currentPage;
               luckAddonAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("leaderboard:" + this.currentPage));
            }
            break;
         case 2:
            if (this.currentPage < this.totalPages) {
               ++this.currentPage;
               luckAddonAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("leaderboard:" + this.currentPage));
            }
      }

   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   public static class LeaderboardEntry {
      public int rank;
      public String name;
      public String rankTier;
      public int elo;
      public int wins;
      public int losses;
      public String uuid;

      public LeaderboardEntry(int rank, String name, String rankTier, int elo, int wins, int losses, String uuid) {
         this.rank = rank;
         this.name = name;
         this.rankTier = rankTier;
         this.elo = elo;
         this.wins = wins;
         this.losses = losses;
         this.uuid = uuid;
      }
   }
}
