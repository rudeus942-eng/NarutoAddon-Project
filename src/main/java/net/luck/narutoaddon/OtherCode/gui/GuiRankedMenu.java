
package net.luck.narutoaddon.OtherCode.gui;

import net.luck.narutoaddon.OtherCode.luckAddonAddon;
import net.luck.narutoaddon.OtherCode.network.RankedGuiActionMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiRankedMenu extends GuiScreen {
   private static final int GUI_WIDTH = 300;
   private static final int GUI_HEIGHT = 240;
   private static final int COLOR_BORDER_OUTER = -39424;
   private static final int COLOR_BORDER_INNER = -3386112;
   private static final int COLOR_BG_DARK = -300279270;
   private static final int COLOR_BG_PANEL = -869651926;
   private static final int COLOR_ACCENT = -48060;
   private static final int COLOR_GOLD = -10496;
   private static final int COLOR_CHAKRA_BLUE = -12285697;
   private String playerRank = "Loading...";
   private int playerElo = 0;
   private int wins = 0;
   private int losses = 0;
   private int winRate = 0;
   private int winStreak = 0;
   private boolean isQueued = false;
   private boolean isInMatch = false;
   private int queueTime = 0;
   private int queueSize = 0;
   private String placementProgress = null;
   private boolean isInRaid = false;
   private String currentRaidBoss = "";
   private int dailyWinsToday = 0;
   private int dailyWinsRequired = 3;
   private boolean dailyTaskCompleted = false;
   private long lastUpdateTime = 0L;
   private long queueStartTime = 0L;
   private static final long UPDATE_INTERVAL = 1000L;
   private static final int BUTTON_QUEUE = 0;
   private static final int BUTTON_LEAVE_QUEUE = 1;
   private static final int BUTTON_STATS = 2;
   private static final int BUTTON_LEADERBOARD = 3;
   private static final int BUTTON_CLOSE = 4;
   private static final int BUTTON_SEASON = 5;
   private static final int BUTTON_TITLES = 6;
   private static final int BUTTON_CLAIM_REWARDS = 7;
   private static final int BUTTON_RAID = 8;
   private boolean hasPendingRewards = false;
   private int guiLeft;
   private int guiTop;
   private float animationTick = 0.0F;

   public GuiRankedMenu() {
      this.queueStartTime = System.currentTimeMillis();
   }

   public void updateData(String rank, int elo, int w, int l, int streak, boolean queued, boolean inMatch, int qTime, int qSize, String placement) {
      this.playerRank = rank;
      this.playerElo = elo;
      this.wins = w;
      this.losses = l;
      this.winStreak = streak;
      boolean wasQueued = this.isQueued;
      boolean wasInMatch = this.isInMatch;
      this.isQueued = queued;
      this.isInMatch = inMatch;
      this.queueTime = qTime;
      this.queueSize = qSize;
      this.placementProgress = placement;
      if (queued && !wasQueued) {
         this.queueStartTime = System.currentTimeMillis() - (long)qTime * 1000L;
      }

      if (w + l > 0) {
         this.winRate = Math.round((float)w / (float)(w + l) * 100.0F);
      }

      if (this.buttonList != null && (wasQueued != queued || wasInMatch != inMatch)) {
         this.buttonList.clear();
         this.initGui();
      }

   }

   public void updateDailyTask(int winsToday, int winsRequired, boolean completed) {
      this.dailyWinsToday = winsToday;
      this.dailyWinsRequired = winsRequired;
      this.dailyTaskCompleted = completed;
   }

   public void updatePendingRewards(boolean hasPending) {
      boolean wasHasPending = this.hasPendingRewards;
      this.hasPendingRewards = hasPending;
      if (this.buttonList != null && wasHasPending != hasPending) {
         this.buttonList.clear();
         this.initGui();
      }

   }

   public void updateRaidState(boolean inRaid, String raidBoss) {
      boolean wasInRaid = this.isInRaid;
      this.isInRaid = inRaid;
      this.currentRaidBoss = raidBoss != null ? raidBoss : "";
      if (this.buttonList != null && wasInRaid != inRaid) {
         this.buttonList.clear();
         this.initGui();
      }

   }

   public void initGui() {
      this.guiLeft = (this.width - 300) / 2;
      this.guiTop = (this.height - 240) / 2;
      this.buttonList.clear();
      int centerX = this.guiLeft + 150;
      if (!this.isInMatch && !this.isInRaid) {
         if (this.isQueued) {
            this.buttonList.add(new GuiButtonGradient(1, centerX - 65, this.guiTop + 155, 130, 24, "Leave Queue", -5627358, -10088175));
         } else {
            this.buttonList.add(new GuiButtonGradient(0, centerX - 65, this.guiTop + 125, 130, 24, "⚔ Join Battle!", -14505438, -15636975));
         }

         int tabY = this.guiTop + 190;
         int btnWidth = 65;
         int spacing = 4;
         int totalButtonsWidth = btnWidth * 4 + spacing * 3;
         int startX = this.guiLeft + (300 - totalButtonsWidth) / 2;
         this.buttonList.add(new GuiButtonGradient(2, startX, tabY, btnWidth, 20, "Stats", -13408598, -14531448));
         this.buttonList.add(new GuiButtonGradient(3, startX + btnWidth + spacing, tabY, btnWidth, 20, "Rankings", -5601246, -7838191));
         this.buttonList.add(new GuiButtonGradient(5, startX + (btnWidth + spacing) * 2, tabY, btnWidth, 20, "Season", -8965206, -11197816));
         this.buttonList.add(new GuiButtonGradient(8, startX + (btnWidth + spacing) * 3, tabY, btnWidth, 20, "Raid", -5622989, -7855582));
         this.buttonList.add(new GuiButtonGradient(4, centerX - 45, this.guiTop + 218, 90, 18, "Close", -11184811, -13421773));
      } else {
         this.buttonList.add(new GuiButtonGradient(4, centerX - 60, this.guiTop + 240 - 35, 120, 24, "Close", -11184811, -13421773));
      }
   }

   public void updateScreen() {
      super.updateScreen();
      this.animationTick += 0.1F;
      if (this.isQueued) {
         long now = System.currentTimeMillis();
         if (now - this.lastUpdateTime >= 1000L) {
            this.lastUpdateTime = now;
            if (this.queueTime % 3 == 0) {
               luckAddonAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("open"));
            }
         }
      }

   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      if (this.isInMatch) {
         this.drawInBattleScreen(mouseX, mouseY, partialTicks);
      } else if (this.isInRaid) {
         this.drawInRaidScreen(mouseX, mouseY, partialTicks);
      } else {
         float glowIntensity = (float)(0.3 + 0.15 * Math.sin((double)this.animationTick));
         int glowAlpha = (int)(glowIntensity * 255.0F);
         drawRect(this.guiLeft - 4, this.guiTop - 4, this.guiLeft + 300 + 4, this.guiTop + 240 + 4, glowAlpha << 24 | 16737792);
         this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + 300, this.guiTop + 240, -299226582, -301331958);
         this.drawBorderWithShading(this.guiLeft, this.guiTop, 300, 240, -39424, 2);
         this.drawBorderWithShading(this.guiLeft + 4, this.guiTop + 4, 292, 232, -3386112, 1);
         this.drawCornerDecorations();
         String title = "§6§l✦ SHINOBI ARENA ✦";
         int titleWidth = this.fontRenderer.getStringWidth(title);
         this.fontRenderer.drawString(title, this.guiLeft + (300 - titleWidth) / 2 + 1, this.guiTop + 14 + 1, 0);
         this.fontRenderer.drawStringWithShadow(title, (float)(this.guiLeft + (300 - titleWidth) / 2), (float)(this.guiTop + 14), -10496);
         String subtitle = "§7Ranked 1v1 Combat";
         int subWidth = this.fontRenderer.getStringWidth(subtitle);
         this.fontRenderer.drawString(subtitle, this.guiLeft + (300 - subWidth) / 2, this.guiTop + 26, 10066329);
         String copyrightLeft = "§bOwned by IceeNetwork";
         String copyrightRight = "§bDiscord.gg/iceeramen";
         this.fontRenderer.drawString(copyrightLeft, this.guiLeft + 28, this.guiTop + 36, 5636095);
         int rightWidth = this.fontRenderer.getStringWidth(copyrightRight);
         this.fontRenderer.drawString(copyrightRight, this.guiLeft + 300 - rightWidth - 28, this.guiTop + 36, 5636095);
         this.drawHorizontalLine(this.guiLeft + 25, this.guiLeft + 150 - 10, this.guiTop + 46, -3386112);
         this.drawHorizontalLine(this.guiLeft + 150 + 10, this.guiLeft + 300 - 25, this.guiTop + 46, -3386112);
         this.drawDiamond(this.guiLeft + 150, this.guiTop + 46, 5, -39424);
         this.drawPanelWithLighting(this.guiLeft + 15, this.guiTop + 52, 270, 63);
         boolean inPlacements = this.placementProgress != null && !this.placementProgress.isEmpty();
         if (inPlacements) {
            String placementTitle = "§d§l✦ PLACEMENT MATCHES ✦";
            int titleW = this.fontRenderer.getStringWidth(placementTitle);
            this.fontRenderer.drawStringWithShadow(placementTitle, (float)(this.guiLeft + (300 - titleW) / 2), (float)(this.guiTop + 57), 16777215);
            String progressText = "§fProgress: §e§l" + this.placementProgress + " §7matches completed";
            int progW = this.fontRenderer.getStringWidth(progressText);
            this.fontRenderer.drawStringWithShadow(progressText, (float)(this.guiLeft + (300 - progW) / 2), (float)(this.guiTop + 70), 16777215);
            String hintText = "§8Complete 5 matches to receive your rank!";
            int hintW = this.fontRenderer.getStringWidth(hintText);
            this.fontRenderer.drawString(hintText, this.guiLeft + (300 - hintW) / 2, this.guiTop + 83, 8947848);
            if (this.wins > 0 || this.losses > 0) {
               String recordText = "§a" + this.wins + "W §7- §c" + this.losses + "L";
               int recW = this.fontRenderer.getStringWidth(recordText);
               this.fontRenderer.drawStringWithShadow(recordText, (float)(this.guiLeft + (300 - recW) / 2), (float)(this.guiTop + 96), 16777215);
            }
         } else {
            String rankIcon = this.getRankIcon();
            String rankLabel = rankIcon + " " + makeBold(this.playerRank);
            this.fontRenderer.drawStringWithShadow(rankLabel, (float)(this.guiLeft + 24), (float)(this.guiTop + 57), 16777215);
            String eloColor = this.playerElo >= 2000 ? "§c" : (this.playerElo >= 1500 ? "§6" : "§e");
            String eloLabel = "§7ELO: " + eloColor + "§l" + this.playerElo;
            this.fontRenderer.drawStringWithShadow(eloLabel, (float)(this.guiLeft + 24), (float)(this.guiTop + 70), 16777215);
            String winsText = "§a§l" + this.wins + "W";
            String lossesText = "§c§l" + this.losses + "L";
            String wrColor = this.winRate >= 60 ? "§a" : (this.winRate >= 50 ? "§e" : "§c");
            String recordLabel = winsText + " §7/ " + lossesText + " §8(" + wrColor + this.winRate + "%§8)";
            this.fontRenderer.drawStringWithShadow(recordLabel, (float)(this.guiLeft + 24), (float)(this.guiTop + 83), 16777215);
            int nextY = this.guiTop + 96;
            if (this.winStreak > 0) {
               String streakColor = this.winStreak >= 5 ? "§c§l" : (this.winStreak >= 3 ? "§6" : "§a");
               String fireIcon = this.winStreak >= 5 ? "★ " : (this.winStreak >= 3 ? "★ " : "• ");
               String streakLabel = fireIcon + streakColor + this.winStreak + " Win Streak!";
               this.fontRenderer.drawStringWithShadow(streakLabel, (float)(this.guiLeft + 24), (float)nextY, 16777215);
               nextY += 10;
            }
         }

         if (this.isInMatch) {
            int alertX = this.guiLeft + 30;
            int alertY = this.guiTop + 120;
            int alertW = 240;
            int alertH = 30;
            this.drawPanelWithLighting(alertX, alertY, alertW, alertH);
            this.drawGradientRect(alertX + 1, alertY + 1, alertX + alertW - 1, alertY + alertH - 1, 1720188928, 1145307136);
            String matchLabel = "§c§l⚠ IN BATTLE ⚠";
            int matchWidth = this.fontRenderer.getStringWidth(matchLabel);
            this.fontRenderer.drawStringWithShadow(matchLabel, (float)(this.guiLeft + (300 - matchWidth) / 2), (float)(alertY + 11), 16777215);
         } else if (this.isQueued) {
            int liveQueueTime = (int)((System.currentTimeMillis() - this.queueStartTime) / 1000L);
            int searchX = this.guiLeft + 25;
            int searchY = this.guiTop + 120;
            int searchW = 250;
            int searchH = 32;
            float pulse = (float)((double)0.5F + 0.2 * Math.sin((double)(this.animationTick * 2.0F)));
            this.drawPanelWithLighting(searchX, searchY, searchW, searchH);
            int pulseAlpha = (int)(pulse * 150.0F);
            this.drawGradientRect(searchX + 1, searchY + 1, searchX + searchW - 1, searchY + searchH - 1, pulseAlpha << 24 | 13395456, pulseAlpha << 24 | 8930304);
            int dots = (int)(this.animationTick * 2.0F) % 4;
            String dotsStr = "";

            for(int i = 0; i < dots; ++i) {
               dotsStr = dotsStr + ".";
            }

            String searchLabel = "§e§lSearching" + dotsStr;
            int searchLabelWidth = this.fontRenderer.getStringWidth(searchLabel);
            this.fontRenderer.drawStringWithShadow(searchLabel, (float)(this.guiLeft + (300 - searchLabelWidth) / 2), (float)(searchY + 5), 16777215);
            String timeLabel = "§f" + this.formatTime(liveQueueTime) + " §8| §f" + this.queueSize + " in queue";
            int timeWidth = this.fontRenderer.getStringWidth(timeLabel);
            this.fontRenderer.drawStringWithShadow(timeLabel, (float)(this.guiLeft + (300 - timeWidth) / 2), (float)(searchY + 18), 16777215);
         }

         int dailyX = this.guiLeft + 15;
         int dailyY = this.guiTop + 158;
         int dailyW = 270;
         int dailyH = 22;
         if (!this.isQueued && !this.isInMatch) {
            this.drawPanelWithLighting(dailyX, dailyY, dailyW, dailyH);
            String taskIcon = this.dailyTaskCompleted ? "§a✓" : "§e✦";
            String taskLabel = taskIcon + " §7Daily: ";
            int barX = dailyX + 70;
            int barY = dailyY + 7;
            int barW = 120;
            int barH = 8;
            drawRect(barX, barY, barX + barW, barY + barH, -14540254);
            int fillW = (int)((float)this.dailyWinsToday / (float)this.dailyWinsRequired * (float)barW);
            fillW = Math.min(fillW, barW);
            int fillColor = this.dailyTaskCompleted ? -14505438 : -30720;
            drawRect(barX + 1, barY + 1, barX + 1 + fillW, barY + barH - 1, fillColor);
            drawRect(barX, barY, barX + barW, barY + 1, -12303292);
            drawRect(barX, barY + barH - 1, barX + barW, barY + barH, -14540254);
            drawRect(barX, barY, barX + 1, barY + barH, -12303292);
            drawRect(barX + barW - 1, barY, barX + barW, barY + barH, -14540254);
            this.fontRenderer.drawStringWithShadow(taskLabel, (float)(dailyX + 8), (float)(dailyY + 7), 16777215);
            String progressText;
            if (this.dailyTaskCompleted) {
               progressText = "§a§lComplete!";
            } else {
               progressText = "§f" + this.dailyWinsToday + "/" + this.dailyWinsRequired + " wins";
            }

            int textX = barX + barW + 8;
            this.fontRenderer.drawStringWithShadow(progressText, (float)textX, (float)(dailyY + 7), 16777215);
         }

         this.drawHorizontalLine(this.guiLeft + 25, this.guiLeft + 300 - 25, this.guiTop + 182, -3386112);
         super.drawScreen(mouseX, mouseY, partialTicks);
      }
   }

   private void drawInBattleScreen(int mouseX, int mouseY, float partialTicks) {
      float glowIntensity = (float)(0.4 + 0.2 * Math.sin((double)this.animationTick * (double)1.5F));
      int glowAlpha = (int)(glowIntensity * 255.0F);
      drawRect(this.guiLeft - 6, this.guiTop - 6, this.guiLeft + 300 + 6, this.guiTop + 240 + 6, glowAlpha << 24 | 11141120);
      this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + 300, this.guiTop + 240, -299231979, -301333243);
      this.drawBorderWithShading(this.guiLeft, this.guiTop, 300, 240, -3399134, 2);
      this.drawBorderWithShading(this.guiLeft + 4, this.guiTop + 4, 292, 232, -7859951, 1);
      this.drawCornerDecorations();
      String title = "§c§l☠ IN BATTLE ☠";
      int titleWidth = this.fontRenderer.getStringWidth(title);
      this.fontRenderer.drawString(title, this.guiLeft + (300 - titleWidth) / 2 + 1, this.guiTop + 20 + 1, 0);
      this.fontRenderer.drawStringWithShadow(title, (float)(this.guiLeft + (300 - titleWidth) / 2), (float)(this.guiTop + 20), -48060);
      String subtitle = "§7Match in Progress";
      int subWidth = this.fontRenderer.getStringWidth(subtitle);
      this.fontRenderer.drawString(subtitle, this.guiLeft + (300 - subWidth) / 2, this.guiTop + 35, 11184810);
      this.drawHorizontalLine(this.guiLeft + 25, this.guiLeft + 300 - 25, this.guiTop + 48, -7859951);
      int panelX = this.guiLeft + 20;
      int panelY = this.guiTop + 60;
      int panelW = 260;
      int panelH = 100;
      this.drawPanelWithLighting(panelX, panelY, panelW, panelH);
      this.drawGradientRect(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, 866779136, 575995904);
      String rankLabel = "§7Your Rank: " + this.playerRank;
      this.fontRenderer.drawStringWithShadow(rankLabel, (float)(panelX + 12), (float)(panelY + 12), 16777215);
      String eloColor = this.playerElo >= 2000 ? "§c" : (this.playerElo >= 1500 ? "§6" : "§e");
      String eloLabel = "§7ELO: " + eloColor + this.playerElo;
      this.fontRenderer.drawStringWithShadow(eloLabel, (float)(panelX + 12), (float)(panelY + 26), 16777215);
      int tipY = panelY + 48;
      String tip1 = "§8• §7Focus on your opponent";
      String tip2 = "§8• §7Use your abilities wisely";
      String tip3 = "§8• §7Watch the arena boundaries";
      this.fontRenderer.drawString(tip1, panelX + 12, tipY, 13421772);
      this.fontRenderer.drawString(tip2, panelX + 12, tipY + 14, 13421772);
      this.fontRenderer.drawString(tip3, panelX + 12, tipY + 28, 13421772);
      float pulse = (float)(0.6 + 0.4 * Math.sin((double)(this.animationTick * 2.0F)));
      int indicatorAlpha = (int)(pulse * 255.0F);
      int indicatorX = this.guiLeft + 150;
      int indicatorY = this.guiTop + 175;
      String indicator = "§c§l⚔ FIGHT! ⚔";
      int indicatorWidth = this.fontRenderer.getStringWidth(indicator);
      this.fontRenderer.drawStringWithShadow(indicator, (float)(indicatorX - indicatorWidth / 2), (float)indicatorY, indicatorAlpha << 24 | 16729156);
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private void drawInRaidScreen(int mouseX, int mouseY, float partialTicks) {
      float glowIntensity = (float)(0.4 + 0.2 * Math.sin((double)this.animationTick * (double)1.5F));
      int glowAlpha = (int)(glowIntensity * 255.0F);
      drawRect(this.guiLeft - 6, this.guiTop - 6, this.guiLeft + 300 + 6, this.guiTop + 240 + 6, glowAlpha << 24 | 8912896);
      this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + 300, this.guiTop + 240, -300283382, -301662208);
      this.drawBorderWithShading(this.guiLeft, this.guiTop, 300, 240, -5631727, 2);
      this.drawBorderWithShading(this.guiLeft + 4, this.guiTop + 4, 292, 232, -10090488, 1);
      this.drawCornerDecorations();
      String title = "§4§l☠ BOSS RAID ☠";
      int titleWidth = this.fontRenderer.getStringWidth(title);
      this.fontRenderer.drawString(title, this.guiLeft + (300 - titleWidth) / 2 + 1, this.guiTop + 20 + 1, 0);
      this.fontRenderer.drawStringWithShadow(title, (float)(this.guiLeft + (300 - titleWidth) / 2), (float)(this.guiTop + 20), -3399134);
      String subtitle = "§7Fighting: §6" + this.currentRaidBoss;
      int subWidth = this.fontRenderer.getStringWidth(subtitle);
      this.fontRenderer.drawString(subtitle, this.guiLeft + (300 - subWidth) / 2, this.guiTop + 35, 11184810);
      this.drawHorizontalLine(this.guiLeft + 25, this.guiLeft + 300 - 25, this.guiTop + 48, -10090488);
      int panelX = this.guiLeft + 20;
      int panelY = this.guiTop + 60;
      int panelW = 260;
      int panelH = 100;
      this.drawPanelWithLighting(panelX, panelY, panelW, panelH);
      this.drawGradientRect(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, 862322688, 573767680);
      int tipY = panelY + 12;
      String tip1 = "§8• §7Watch for boss mechanics!";
      String tip2 = "§8• §7Stay in safe zones during attacks";
      String tip3 = "§8• §7Work with your party";
      String tip4 = "§8• §7Custom boss bar is at the top";
      this.fontRenderer.drawString(tip1, panelX + 12, tipY, 13421772);
      this.fontRenderer.drawString(tip2, panelX + 12, tipY + 14, 13421772);
      this.fontRenderer.drawString(tip3, panelX + 12, tipY + 28, 13421772);
      this.fontRenderer.drawString(tip4, panelX + 12, tipY + 42, 13421772);
      String notice = "§ePress R to open Raid Menu";
      int noticeW = this.fontRenderer.getStringWidth(notice);
      this.fontRenderer.drawString(notice, panelX + (panelW - noticeW) / 2, tipY + 65, 16755200);
      float pulse = (float)(0.6 + 0.4 * Math.sin((double)(this.animationTick * 2.0F)));
      int indicatorAlpha = (int)(pulse * 255.0F);
      int indicatorX = this.guiLeft + 150;
      int indicatorY = this.guiTop + 175;
      String indicator = "§4§l⚔ BOSS FIGHT! ⚔";
      int indicatorWidth = this.fontRenderer.getStringWidth(indicator);
      this.fontRenderer.drawStringWithShadow(indicator, (float)(indicatorX - indicatorWidth / 2), (float)indicatorY, indicatorAlpha << 24 | 13378082);
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

   private void drawBorderWithShading(int x, int y, int w, int h, int color, int thickness) {
      int brightColor = brightenColor(color, 40);
      int darkColor = darkenColor(color, 40);
      drawRect(x, y, x + w, y + thickness, brightColor);
      drawRect(x, y, x + thickness, y + h, brightColor);
      drawRect(x, y + h - thickness, x + w, y + h, darkColor);
      drawRect(x + w - thickness, y, x + w, y + h, darkColor);
   }

   private void drawDiamond(int centerX, int centerY, int size, int color) {
      drawRect(centerX - 1, centerY - size, centerX + 1, centerY - size + 2, color);
      drawRect(centerX - 2, centerY - size + 2, centerX + 2, centerY + size - 2, color);
      drawRect(centerX - 1, centerY + size - 2, centerX + 1, centerY + size, color);
   }

   private void drawCornerDecorations() {
      int size = 8;
      int offset = 8;
      int brightOrange = brightenColor(16737792, 30) | -16777216;
      int darkOrange = darkenColor(16737792, 30) | -16777216;
      drawRect(this.guiLeft + offset, this.guiTop + offset, this.guiLeft + offset + size, this.guiTop + offset + 2, brightOrange);
      drawRect(this.guiLeft + offset, this.guiTop + offset, this.guiLeft + offset + 2, this.guiTop + offset + size, brightOrange);
      drawRect(this.guiLeft + 300 - offset - size, this.guiTop + offset, this.guiLeft + 300 - offset, this.guiTop + offset + 2, brightOrange);
      drawRect(this.guiLeft + 300 - offset - 2, this.guiTop + offset, this.guiLeft + 300 - offset, this.guiTop + offset + size, darkOrange);
      drawRect(this.guiLeft + offset, this.guiTop + 240 - offset - 2, this.guiLeft + offset + size, this.guiTop + 240 - offset, darkOrange);
      drawRect(this.guiLeft + offset, this.guiTop + 240 - offset - size, this.guiLeft + offset + 2, this.guiTop + 240 - offset, brightOrange);
      drawRect(this.guiLeft + 300 - offset - size, this.guiTop + 240 - offset - 2, this.guiLeft + 300 - offset, this.guiTop + 240 - offset, darkOrange);
      drawRect(this.guiLeft + 300 - offset - 2, this.guiTop + 240 - offset - size, this.guiLeft + 300 - offset, this.guiTop + 240 - offset, darkOrange);
   }

   private String getRankIcon() {
      if (this.playerRank.contains("Otsutsuki")) {
         return "§4★";
      } else if (this.playerRank.contains("Kage")) {
         return "§c★";
      } else if (this.playerRank.contains("ANBU")) {
         return "§5☆";
      } else if (this.playerRank.contains("Elite")) {
         return "§b☆";
      } else if (this.playerRank.contains("Jonin")) {
         return "§6◆";
      } else if (this.playerRank.contains("Special")) {
         return "§e◆";
      } else if (this.playerRank.contains("Chunin")) {
         return "§a◇";
      } else {
         return this.playerRank.contains("Genin") ? "§7○" : "§f○";
      }
   }

   private String getRankShortName() {
      if (this.playerRank.contains("Otsutsuki")) {
         return "§4Ots";
      } else if (this.playerRank.contains("Kage")) {
         return "§cKag";
      } else if (this.playerRank.contains("ANBU")) {
         return "§5ANB";
      } else if (this.playerRank.contains("Elite")) {
         return "§bElt";
      } else if (this.playerRank.contains("Jonin")) {
         return "§6Jon";
      } else if (this.playerRank.contains("Special")) {
         return "§eSpc";
      } else if (this.playerRank.contains("Chunin")) {
         return "§aChu";
      } else {
         return this.playerRank.contains("Genin") ? "§7Gen" : "";
      }
   }

   protected void actionPerformed(GuiButton button) {
      switch (button.id) {
         case 0:
            luckAddonAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("queue"));
            break;
         case 1:
            luckAddonAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("leave"));
            break;
         case 2:
            this.mc.displayGuiScreen(new GuiRankedStats());
            break;
         case 3:
            this.mc.displayGuiScreen(new GuiRankedLeaderboard());
            break;
         case 4:
            this.mc.displayGuiScreen((GuiScreen)null);
            break;
         case 5:
            this.mc.displayGuiScreen(new GuiRankedSeason());
         case 6:
         default:
            break;
         case 7:
            luckAddonAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("claimrewards"));
            luckAddonAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("open"));
            break;
         case 8:
            GuiRaidMenu.open();
      }

   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   private String formatTime(int seconds) {
      int mins = seconds / 60;
      int secs = seconds % 60;
      return String.format("%d:%02d", mins, secs);
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

   public static String makeBold(String text) {
      if (text != null && !text.isEmpty()) {
         StringBuilder result = new StringBuilder();

         for(int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            result.append(c);
            if (c == 167 && i + 1 < text.length()) {
               char next = text.charAt(i + 1);
               result.append(next);
               ++i;
               if (next >= '0' && next <= '9' || next >= 'a' && next <= 'f' || next >= 'A' && next <= 'F') {
                  result.append("§l");
               }
            }
         }

         if (!text.contains("§")) {
            return "§l" + text;
         } else {
            return result.toString();
         }
      } else {
         return text;
      }
   }

   public static void open() {
      Minecraft mc = Minecraft.getMinecraft();
      GuiRankedMenu gui = new GuiRankedMenu();
      mc.displayGuiScreen(gui);
      luckAddonAddon.PACKET_HANDLER.sendToServer(new RankedGuiActionMessage("open"));
   }

   public static class GuiButtonGradient extends GuiButton {
      public float textScale = 1.0F;
      private int colorTop;
      private int colorBottom;

      public GuiButtonGradient(int id, int x, int y, int width, int height, String text, int colorTop, int colorBottom) {
         super(id, x, y, width, height, text);
         this.colorTop = colorTop;
         this.colorBottom = colorBottom;
      }

      public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
         if (this.visible) {
            boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int topColor = hovered ? brightenColor(this.colorTop, 50) : this.colorTop;
            int bottomColor = hovered ? brightenColor(this.colorBottom, 50) : this.colorBottom;
            topColor = -16777216 | topColor;
            bottomColor = -16777216 | bottomColor;
            this.drawGradientRect(this.x, this.y, this.x + this.width, this.y + this.height, topColor, bottomColor);
            int highlightAlpha = hovered ? 102 : 68;
            drawRect(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + 3, highlightAlpha << 24 | 16777215);
            drawRect(this.x + 1, this.y + 1, this.x + 2, this.y + this.height - 1, highlightAlpha << 24 | 16777215);
            drawRect(this.x + 1, this.y + this.height - 3, this.x + this.width - 1, this.y + this.height - 1, 1711276032);
            drawRect(this.x + this.width - 2, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, 1140850688);
            int borderBright = hovered ? -16777216 | brightenColor(this.colorTop, 100) : -16777216 | brightenColor(this.colorTop, 60);
            int borderDark = hovered ? -16777216 | brightenColor(this.colorBottom, 30) : -11184811;
            drawRect(this.x, this.y, this.x + this.width, this.y + 1, borderBright);
            drawRect(this.x, this.y, this.x + 1, this.y + this.height, borderBright);
            drawRect(this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, borderDark);
            drawRect(this.x + this.width - 1, this.y, this.x + this.width, this.y + this.height, borderDark);
            if (hovered) {
               drawRect(this.x + 2, this.y + 3, this.x + this.width - 2, this.y + 4, 587202559);
            }

            int textColor = hovered ? 16777215 : 14737632;
            String displayText = this.displayString;
            if (this.textScale != 1.0F) {
               GlStateManager.pushMatrix();
               int scaledTextW = (int)((float)mc.fontRenderer.getStringWidth(displayText) * this.textScale);
               float textX = (float)this.x + (float)(this.width - scaledTextW) / 2.0F;
               float textY = (float)this.y + (float)(this.height - (int)(8.0F * this.textScale)) / 2.0F;
               GlStateManager.translate(textX, textY, 0.0F);
               GlStateManager.scale(this.textScale, this.textScale, 1.0F);
               mc.fontRenderer.drawStringWithShadow(displayText, 0.0F, 0.0F, textColor);
               GlStateManager.popMatrix();
            } else {
               int textX = this.x + (this.width - mc.fontRenderer.getStringWidth(displayText)) / 2;
               int textY = this.y + (this.height - 8) / 2;
               mc.fontRenderer.drawStringWithShadow(displayText, (float)textX, (float)textY, textColor);
            }

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
