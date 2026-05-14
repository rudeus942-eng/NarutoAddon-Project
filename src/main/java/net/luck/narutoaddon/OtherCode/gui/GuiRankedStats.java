
package net.luck.narutoaddon.OtherCode.gui;

import net.luck.narutoaddon.OtherCode.InfTsukAddon;
import net.luck.narutoaddon.OtherCode.network.RankedGuiActionMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiRankedStats extends GuiScreen {
   private static final int GUI_WIDTH = 300;
   private static final int GUI_HEIGHT = 240;
   private static final int COLOR_BORDER = -12285697;
   private static final int COLOR_BG = -300279270;
   private static final int COLOR_PANEL = -869651926;
   private String playerName = "Loading...";
   private String playerRank = "";
   private int elo = 0;
   private int peakElo = 0;
   private int seasonHigh = 0;
   private int wins = 0;
   private int losses = 0;
   private int winRate = 0;
   private int currentStreak = 0;
   private int bestStreak = 0;
   private int gamesPlayed = 0;
   private int leaderboardRank = 0;
   private String placementProgress = null;
   private int guiLeft;
   private int guiTop;
   private static final int BUTTON_BACK = 0;

   public void updateData(String name, String rank, int elo, int peak, int seasonHigh, int wins, int losses, int streak, int bestStreak, int games, int lbRank, String placement) {
      this.playerName = name;
      this.playerRank = rank;
      this.elo = elo;
      this.peakElo = peak;
      this.seasonHigh = seasonHigh;
      this.wins = wins;
      this.losses = losses;
      this.currentStreak = streak;
      this.bestStreak = bestStreak;
      this.gamesPlayed = games;
      this.leaderboardRank = lbRank;
      this.placementProgress = placement;
      if (wins + losses > 0) {
         this.winRate = Math.round((float)wins / (float)(wins + losses) * 100.0F);
      }

   }

   public void initGui() {
      this.guiLeft = (this.width - 300) / 2;
      this.guiTop = (this.height - 240) / 2;
      this.buttonList.clear();
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(0, this.guiLeft + 150 - 50, this.guiTop + 240 - 28, 100, 20, "Back", -11184811, -13421773));
      InfTsukAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("fullstats"));
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      drawRect(this.guiLeft, this.guiTop, this.guiLeft + 300, this.guiTop + 240, -300279270);
      this.drawBorder(this.guiLeft, this.guiTop, 300, 240, -12285697);
      this.drawBorder(this.guiLeft + 3, this.guiTop + 3, 294, 234, -13408564);
      String title = "§b§l✦ SHINOBI STATS ✦";
      int titleWidth = this.fontRenderer.getStringWidth(title);
      this.fontRenderer.drawStringWithShadow(title, (float)(this.guiLeft + (300 - titleWidth) / 2), (float)(this.guiTop + 10), 16777215);
      String nameTitle = "§f" + this.playerName;
      int nameWidth = this.fontRenderer.getStringWidth(nameTitle);
      this.fontRenderer.drawStringWithShadow(nameTitle, (float)(this.guiLeft + (300 - nameWidth) / 2), (float)(this.guiTop + 24), 16777215);
      this.drawHorizontalLine(this.guiLeft + 15, this.guiLeft + 300 - 15, this.guiTop + 36, -12285697);
      int leftX = this.guiLeft + 15;
      int rightX = this.guiLeft + 150 + 10;
      int panelWidth = 125;
      boolean inPlacements = this.placementProgress != null && !this.placementProgress.isEmpty();
      if (inPlacements) {
         drawRect(leftX, this.guiTop + 42, this.guiLeft + 300 - 15, this.guiTop + 95, -869651926);
         this.drawBorder(leftX, this.guiTop + 42, 270, 53, -12303292);
         String placementTitle = "§d§l✦ PLACEMENT MATCHES ✦";
         int ptWidth = this.fontRenderer.getStringWidth(placementTitle);
         this.fontRenderer.drawStringWithShadow(placementTitle, (float)(this.guiLeft + (300 - ptWidth) / 2), (float)(this.guiTop + 47), 16777215);
         String progressText = "§fProgress: §e§l" + this.placementProgress + " §7matches completed";
         int progWidth = this.fontRenderer.getStringWidth(progressText);
         this.fontRenderer.drawStringWithShadow(progressText, (float)(this.guiLeft + (300 - progWidth) / 2), (float)(this.guiTop + 62), 16777215);
         String hintText = "§8Complete 5 matches to receive your rank";
         int hintWidth = this.fontRenderer.getStringWidth(hintText);
         this.fontRenderer.drawStringWithShadow(hintText, (float)(this.guiLeft + (300 - hintWidth) / 2), (float)(this.guiTop + 78), 16777215);
      } else {
         drawRect(leftX, this.guiTop + 42, leftX + panelWidth, this.guiTop + 95, -869651926);
         this.drawBorder(leftX, this.guiTop + 42, panelWidth, 53, -12303292);
         this.fontRenderer.drawStringWithShadow("§6§lCURRENT RANK", (float)(leftX + 5), (float)(this.guiTop + 47), 16777215);
         this.fontRenderer.drawStringWithShadow(GuiRankedMenu.makeBold(this.playerRank), (float)(leftX + 5), (float)(this.guiTop + 60), 16777215);
         this.fontRenderer.drawStringWithShadow("§7ELO: §e§l" + this.elo, (float)(leftX + 5), (float)(this.guiTop + 75), 16777215);
         drawRect(rightX, this.guiTop + 42, rightX + panelWidth, this.guiTop + 95, -869651926);
         this.drawBorder(rightX, this.guiTop + 42, panelWidth, 53, -12303292);
         this.fontRenderer.drawStringWithShadow("§d§lELO RECORDS", (float)(rightX + 5), (float)(this.guiTop + 47), 16777215);
         this.fontRenderer.drawStringWithShadow("§7Peak: §f" + this.peakElo, (float)(rightX + 5), (float)(this.guiTop + 60), 16777215);
         this.fontRenderer.drawStringWithShadow("§7Season High: §f" + this.seasonHigh, (float)(rightX + 5), (float)(this.guiTop + 75), 16777215);
      }

      drawRect(leftX, this.guiTop + 100, leftX + panelWidth, this.guiTop + 155, -869651926);
      this.drawBorder(leftX, this.guiTop + 100, panelWidth, 55, -12303292);
      this.fontRenderer.drawStringWithShadow("§a§lBATTLE RECORD", (float)(leftX + 5), (float)(this.guiTop + 105), 16777215);
      this.fontRenderer.drawStringWithShadow("§aWins: §f" + this.wins, (float)(leftX + 5), (float)(this.guiTop + 118), 16777215);
      this.fontRenderer.drawStringWithShadow("§cLosses: §f" + this.losses, (float)(leftX + 5), (float)(this.guiTop + 130), 16777215);
      String wrColor = this.winRate >= 50 ? "§a" : "§c";
      this.fontRenderer.drawStringWithShadow("§7Win Rate: " + wrColor + this.winRate + "%", (float)(leftX + 5), (float)(this.guiTop + 142), 16777215);
      drawRect(rightX, this.guiTop + 100, rightX + panelWidth, this.guiTop + 155, -869651926);
      this.drawBorder(rightX, this.guiTop + 100, panelWidth, 55, -12303292);
      this.fontRenderer.drawStringWithShadow("§c§lWIN STREAKS", (float)(rightX + 5), (float)(this.guiTop + 105), 16777215);
      String currentColor = this.currentStreak >= 5 ? "§c§l" : (this.currentStreak >= 3 ? "§6" : "§f");
      this.fontRenderer.drawStringWithShadow("§7Current: " + currentColor + this.currentStreak, (float)(rightX + 5), (float)(this.guiTop + 118), 16777215);
      this.fontRenderer.drawStringWithShadow("§7Best: §e" + this.bestStreak, (float)(rightX + 5), (float)(this.guiTop + 130), 16777215);
      this.fontRenderer.drawStringWithShadow("§7Games: §f" + this.gamesPlayed, (float)(rightX + 5), (float)(this.guiTop + 142), 16777215);
      drawRect(leftX, this.guiTop + 160, this.guiLeft + 300 - 15, this.guiTop + 185, -869651926);
      this.drawBorder(leftX, this.guiTop + 160, 270, 25, -12303292);
      String lbText;
      if (this.placementProgress != null && !this.placementProgress.isEmpty()) {
         lbText = "§d§lPlacements: §f" + this.placementProgress;
      } else if (this.leaderboardRank > 0) {
         String rankColor = this.leaderboardRank <= 10 ? "§6§l" : (this.leaderboardRank <= 50 ? "§e" : "§f");
         lbText = "§7Leaderboard Position: " + rankColor + "#" + this.leaderboardRank;
      } else {
         lbText = "§7Leaderboard Position: §8Not ranked";
      }

      int lbWidth = this.fontRenderer.getStringWidth(lbText);
      this.fontRenderer.drawStringWithShadow(lbText, (float)(this.guiLeft + (300 - lbWidth) / 2), (float)(this.guiTop + 168), 16777215);
      super.drawScreen(mouseX, mouseY, partialTicks);
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
      }

   }

   public boolean doesGuiPauseGame() {
      return false;
   }
}
