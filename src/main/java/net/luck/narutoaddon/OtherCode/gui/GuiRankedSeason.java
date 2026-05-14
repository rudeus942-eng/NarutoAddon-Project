
package net.luck.narutoaddon.OtherCode.gui;

import net.luck.narutoaddon.OtherCode.luckAddonAddon;
import net.luck.narutoaddon.OtherCode.network.RankedGuiActionMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiRankedSeason extends GuiScreen {
   private static final int GUI_WIDTH = 300;
   private static final int GUI_HEIGHT = 290;
   private static final int COLOR_BORDER = -6736948;
   private static final int COLOR_BG = -300279270;
   private static final int COLOR_PANEL = -869651926;
   private static final int COLOR_TAB_ACTIVE = -868599238;
   private static final int COLOR_TAB_INACTIVE = -1441129958;
   private int seasonNumber = 0;
   private String seasonId = "Beta";
   private String seasonName = "Loading...";
   private int daysRemaining = 0;
   private int hoursRemaining = 0;
   private int minutesRemaining = 0;
   private int totalMatches = 0;
   private int activePlayers = 0;
   private boolean hasPendingRewards = false;
   private String rewardTier = "";
   private int currentTab = 0;
   private static final int TAB_INFO = 0;
   private static final int TAB_REWARDS = 1;
   private int guiLeft;
   private int guiTop;
   private static final int BUTTON_BACK = 0;
   private static final int BUTTON_CLAIM = 1;
   private static final int BUTTON_TAB_INFO = 2;
   private static final int BUTTON_TAB_REWARDS = 3;
   private static final String[][] RANK_REWARDS = new String[][]{{"Genin", "800 Ryo", "§7"}, {"Chunin", "2,000 Ryo", "§a"}, {"Jonin", "4,000 Ryo", "§6"}, {"S. Jonin", "6,000 Ryo", "§e"}, {"Elite Jonin", "10,000 Ryo", "§b"}, {"ANBU", "16,000 Ryo", "§5"}, {"Kage", "28,000 Ryo", "§c"}, {"Otsutsuki", "48,000 Ryo", "§4"}};
   private static final String[][] POSITION_REWARDS = new String[][]{{"#1 Champion", "+200,000 Ryo", "§6"}, {"#2 Runner-Up", "+100,000 Ryo", "§5"}, {"#3 Third Place", "+60,000 Ryo", "§c"}, {"Top 10", "+20,000 Ryo", "§e"}};
   private static final String[][] RANKUP_REWARDS = new String[][]{{"Chunin", "3,000 Ryo", "§a"}, {"Jonin", "6,000 Ryo", "§6"}, {"S. Jonin", "10,000 Ryo", "§e"}, {"Elite Jonin", "16,000 Ryo", "§b"}, {"ANBU", "24,000 Ryo", "§5"}, {"Kage", "36,000 Ryo", "§c"}, {"Otsutsuki", "50,000 Ryo", "§4"}};

   public void updateData(int number, String id, String name, int days, int hours, int minutes, int matches, int players, boolean pendingRewards, String reward) {
      this.seasonNumber = number;
      this.seasonId = id != null && !id.isEmpty() ? id : "S" + number;
      this.seasonName = name;
      this.daysRemaining = days;
      this.hoursRemaining = hours;
      this.minutesRemaining = minutes;
      this.totalMatches = matches;
      this.activePlayers = players;
      this.hasPendingRewards = pendingRewards;
      this.rewardTier = reward;
      if (this.buttonList != null) {
         this.buttonList.clear();
         this.initGui();
      }

   }

   public void initGui() {
      this.guiLeft = (this.width - 300) / 2;
      this.guiTop = (this.height - 290) / 2;
      this.buttonList.clear();
      int centerX = this.guiLeft + 150;
      int tabWidth = 90;
      int tabY = this.guiTop + 28;
      this.buttonList.add(new GuiButtonTab(2, this.guiLeft + 30, tabY, tabWidth, 18, "Info", this.currentTab == 0));
      this.buttonList.add(new GuiButtonTab(3, this.guiLeft + 300 - 30 - tabWidth, tabY, tabWidth, 18, "Rewards", this.currentTab == 1));
      if (this.currentTab == 0 && this.hasPendingRewards) {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(1, centerX - 60, this.guiTop + 185, 120, 22, "Claim Rewards!", -14505438, -15636975));
      }

      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(0, centerX - 40, this.guiTop + 290 - 28, 80, 20, "Back", -11184811, -13421773));
      luckAddonAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("season"));
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      drawRect(this.guiLeft, this.guiTop, this.guiLeft + 300, this.guiTop + 290, -300279270);
      this.drawBorder(this.guiLeft, this.guiTop, 300, 290, -6736948);
      this.drawBorder(this.guiLeft + 3, this.guiTop + 3, 294, 284, -8969558);
      String title = "§d§l✦ " + this.seasonId.toUpperCase() + " ✦";
      int titleWidth = this.fontRenderer.getStringWidth(title);
      this.fontRenderer.drawStringWithShadow(title, (float)(this.guiLeft + (300 - titleWidth) / 2), (float)(this.guiTop + 10), 16777215);
      drawRect(this.guiLeft + 10, this.guiTop + 48, this.guiLeft + 300 - 10, this.guiTop + 290 - 40, -870704614);
      this.drawBorder(this.guiLeft + 10, this.guiTop + 48, 280, 202, -12303292);
      if (this.currentTab == 0) {
         this.drawInfoTab();
      } else {
         this.drawRewardsTab();
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private void drawInfoTab() {
      int contentX = this.guiLeft + 20;
      int contentY = this.guiTop + 58;
      String seasonTitle = "§fSeason Name: §d§l" + this.seasonName;
      this.fontRenderer.drawStringWithShadow(seasonTitle, (float)contentX, (float)contentY, 16777215);
      drawRect(this.guiLeft + 15, this.guiTop + 75, this.guiLeft + 300 - 15, this.guiTop + 100, -869651926);
      this.drawBorder(this.guiLeft + 15, this.guiTop + 75, 270, 25, -12303292);
      String timeLabel;
      String timeColor;
      if (this.daysRemaining > 3) {
         timeLabel = this.daysRemaining + "d " + this.hoursRemaining + "h Remaining";
         timeColor = "§a";
      } else if (this.daysRemaining >= 1) {
         timeLabel = this.daysRemaining + "d " + this.hoursRemaining + "h Remaining";
         timeColor = this.daysRemaining > 1 ? "§e" : "§c";
      } else if (this.hoursRemaining > 0) {
         timeLabel = this.hoursRemaining + "h " + this.minutesRemaining + "m - Ending Soon!";
         timeColor = "§c§l";
      } else if (this.minutesRemaining > 0) {
         timeLabel = this.minutesRemaining + " Minutes Left!";
         timeColor = "§c§l";
      } else {
         timeLabel = "Season Ending!";
         timeColor = "§c§l";
      }

      String timeText = timeColor + timeLabel;
      int timeWidth = this.fontRenderer.getStringWidth(timeText);
      this.fontRenderer.drawStringWithShadow(timeText, (float)(this.guiLeft + (300 - timeWidth) / 2), (float)(this.guiTop + 83), 16777215);
      drawRect(this.guiLeft + 15, this.guiTop + 108, this.guiLeft + 300 - 15, this.guiTop + 148, -869651926);
      this.drawBorder(this.guiLeft + 15, this.guiTop + 108, 270, 40, -12303292);
      this.fontRenderer.drawStringWithShadow("§7Total Matches This Season: §f" + this.totalMatches, (float)contentX, (float)(this.guiTop + 116), 16777215);
      this.fontRenderer.drawStringWithShadow("§7Active Shinobi: §f" + this.activePlayers, (float)contentX, (float)(this.guiTop + 130), 16777215);
      if (this.hasPendingRewards) {
         drawRect(this.guiLeft + 15, this.guiTop + 155, this.guiLeft + 300 - 15, this.guiTop + 178, 1144236595);
         this.drawBorder(this.guiLeft + 15, this.guiTop + 155, 270, 23, -13391309);
         String rewardText = "§a§lUnclaimed Rewards: " + this.rewardTier;
         int rewardWidth = this.fontRenderer.getStringWidth(rewardText);
         this.fontRenderer.drawStringWithShadow(rewardText, (float)(this.guiLeft + (300 - rewardWidth) / 2), (float)(this.guiTop + 162), 16777215);
      }

   }

   private void drawRewardsTab() {
      int contentX = this.guiLeft + 20;
      int contentY = this.guiTop + 54;
      String sectionTitle = "§6§lSeason End Tier Rewards";
      this.fontRenderer.drawStringWithShadow(sectionTitle, (float)contentX, (float)contentY, 16777215);
      contentY += 11;

      for(int i = 0; i < RANK_REWARDS.length; ++i) {
         String[] reward = RANK_REWARDS[i];
         String rankName = reward[0];
         String ryoAmount = reward[1];
         String color = reward[2];
         int rowY = contentY + i * 10;
         if (i % 2 == 0) {
            drawRect(contentX - 5, rowY - 1, this.guiLeft + 300 - 20, rowY + 9, 369098751);
         }

         String icon = this.getRankIcon(rankName);
         String line = icon + " " + color + "§l" + rankName + "§7: §e" + ryoAmount;
         this.fontRenderer.drawString(line, contentX, rowY, 13421772);
      }

      int posY = contentY + RANK_REWARDS.length * 10 + 6;
      this.drawHorizontalLine(contentX, this.guiLeft + 300 - 20, posY - 3, -12303292);
      String posTitle = "§d§lPosition Bonuses";
      this.fontRenderer.drawStringWithShadow(posTitle, (float)contentX, (float)posY, 16777215);
      posY += 11;

      for(int i = 0; i < POSITION_REWARDS.length; ++i) {
         String[] reward = POSITION_REWARDS[i];
         String pos = reward[0];
         String ryoAmount = reward[1];
         String color = reward[2];
         String line = color + "§l" + pos + "§7: §e" + ryoAmount;
         this.fontRenderer.drawString(line, contentX + 5, posY + i * 10, 13421772);
      }

      int rankupY = posY + POSITION_REWARDS.length * 10 + 6;
      this.drawHorizontalLine(contentX, this.guiLeft + 300 - 20, rankupY - 3, -12303292);
      String rankupTitle = "§b§lRank-Up Bonuses §8(Per Season)";
      this.fontRenderer.drawStringWithShadow(rankupTitle, (float)contentX, (float)rankupY, 16777215);
      rankupY += 11;
      String summaryLine = "§a§lChunin§7-§4§lOtsutsuki§7: §e500-12,000 Ryo";
      this.fontRenderer.drawString(summaryLine, contentX + 5, rankupY, 13421772);
   }

   private String getRankIcon(String rank) {
      switch (rank) {
         case "Otsutsuki":
            return "§4★";
         case "Kage":
            return "§c★";
         case "ANBU":
            return "§5☆";
         case "Elite Jonin":
            return "§b☆";
         case "S. Jonin":
            return "§e◆";
         case "Jonin":
            return "§6◆";
         case "Chunin":
            return "§a◇";
         case "Genin":
            return "§7○";
         default:
            return "§f○";
      }
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
            luckAddonAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("claimrewards"));
            break;
         case 2:
            this.currentTab = 0;
            this.buttonList.clear();
            this.initGui();
            break;
         case 3:
            this.currentTab = 1;
            this.buttonList.clear();
            this.initGui();
      }

   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   public static class GuiButtonTab extends GuiButton {
      private boolean active;

      public GuiButtonTab(int id, int x, int y, int width, int height, String text, boolean active) {
         super(id, x, y, width, height, text);
         this.active = active;
      }

      public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
         if (this.visible) {
            boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int bgColor = this.active ? -868599238 : (hovered ? -869651926 : -1441129958);
            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, bgColor);
            int borderColor = this.active ? -6736948 : -12303292;
            drawRect(this.x, this.y, this.x + this.width, this.y + 1, borderColor);
            drawRect(this.x, this.y, this.x + 1, this.y + this.height, borderColor);
            drawRect(this.x + this.width - 1, this.y, this.x + this.width, this.y + this.height, borderColor);
            if (!this.active) {
               drawRect(this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, borderColor);
            }

            int textColor = this.active ? 16777215 : (hovered ? 13421772 : 8947848);
            String displayText = this.displayString;
            int textX = this.x + (this.width - mc.fontRenderer.getStringWidth(displayText)) / 2;
            int textY = this.y + (this.height - 8) / 2;
            if (this.active) {
               mc.fontRenderer.drawStringWithShadow(displayText, (float)textX, (float)textY, textColor);
            } else {
               mc.fontRenderer.drawString(displayText, textX, textY, textColor);
            }

         }
      }
   }
}
