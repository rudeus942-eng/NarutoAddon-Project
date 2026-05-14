
package net.luck.narutoaddon.OtherCode.gui;

import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.core.RaidModInit;
import net.luck.narutoaddon.OtherCode.raid.core.RaidQueueManager;
import net.luck.narutoaddon.OtherCode.raid.network.RaidClientData;
import net.luck.narutoaddon.OtherCode.raid.network.RaidQueueMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiRaidMenu extends GuiScreen {
   private static final int GUI_WIDTH = 300;
   private static final int GUI_HEIGHT = 240;
   private static final int COLOR_BORDER_OUTER = -3394765;
   private static final int COLOR_BORDER_INNER = -6741470;
   private static final int COLOR_BG_DARK = -300279270;
   private static final int COLOR_GOLD = -10496;
   private static final int BUTTON_BACK = 0;
   private static final int BUTTON_QUEUE = 1;
   private static final int BUTTON_LEAVE_QUEUE = 2;
   private static final int BUTTON_CLAIM_REWARDS = 3;
   private static final int BUTTON_DIFF_GENIN = 10;
   private static final int BUTTON_DIFF_CHUNIN = 11;
   private static final int BUTTON_DIFF_JONIN = 12;
   private static final int BUTTON_DIFF_ANBU = 13;
   private static final int BUTTON_BOSS_PREV = 20;
   private static final int BUTTON_BOSS_NEXT = 21;
   private static final String[] BOSS_IDS = new String[]{"hashirama", "itachi", "kimimaro"};
   private static final String[] BOSS_NAMES = new String[]{"Hashirama Senju", "Itachi Uchiha", "Kimimaro Kaguya"};
   private static final String[] BOSS_DESCRIPTIONS = new String[]{"The God of Shinobi. Master of Wood Release.", "Prodigy of the Uchiha. Wielder of Susanoo.", "Last of the Kaguya. Master of Shikotsumyaku."};
   private int selectedBossIndex = 0;
   private RaidDifficulty selectedDifficulty;
   private boolean isQueued;
   private int queueTime;
   private int queueSize;
   private String queuedBossId;
   private RaidDifficulty queuedDifficulty;
   private boolean isInRaid;
   private String currentRaidBoss;
   private boolean hasPendingRewards;
   private int guiLeft;
   private int guiTop;
   private float animationTick;
   private long queueStartTime;
   private boolean statusRequested;

   public GuiRaidMenu() {
      this.selectedDifficulty = RaidDifficulty.GENIN;
      this.isQueued = false;
      this.queueTime = 0;
      this.queueSize = 0;
      this.queuedBossId = "";
      this.queuedDifficulty = null;
      this.isInRaid = false;
      this.currentRaidBoss = "";
      this.hasPendingRewards = false;
      this.animationTick = 0.0F;
      this.queueStartTime = 0L;
      this.statusRequested = false;
      this.queueStartTime = System.currentTimeMillis();
   }

   public void updateQueueData(boolean queued, int time, int size, String bossId, RaidDifficulty diff) {
      boolean wasQueued = this.isQueued;
      this.isQueued = queued;
      this.queueTime = time;
      this.queueSize = size;
      this.queuedBossId = bossId;
      this.queuedDifficulty = diff;
      if (queued && !wasQueued) {
         this.queueStartTime = System.currentTimeMillis() - (long)time * 1000L;
      }

      if (this.buttonList != null && wasQueued != queued) {
         this.buttonList.clear();
         this.initGui();
      }

   }

   public void updateRaidState(boolean inRaid, String bossName) {
      boolean wasInRaid = this.isInRaid;
      this.isInRaid = inRaid;
      this.currentRaidBoss = bossName;
      if (this.buttonList != null && wasInRaid != inRaid) {
         this.buttonList.clear();
         this.initGui();
      }

   }

   public void updatePendingRewards(boolean hasRewards) {
      boolean hadRewards = this.hasPendingRewards;
      this.hasPendingRewards = hasRewards;
      if (this.buttonList != null && hadRewards != hasRewards) {
         this.buttonList.clear();
         this.initGui();
      }

   }

   public void initGui() {
      this.guiLeft = (this.width - 300) / 2;
      this.guiTop = (this.height - 240) / 2;
      if (!this.statusRequested) {
         this.statusRequested = true;
         RaidModInit.NETWORK.sendToServer(new RaidQueueMessage("status", "", 0));
      }

      this.buttonList.clear();
      int centerX = this.guiLeft + 150;
      if (this.isInRaid) {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(0, centerX - 60, this.guiTop + 240 - 35, 120, 24, "Close", -11184811, -13421773));
      } else {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(20, this.guiLeft + 20, this.guiTop + 70, 20, 20, "<", -11184811, -13421773));
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(21, this.guiLeft + 300 - 40, this.guiTop + 70, 20, 20, ">", -11184811, -13421773));
         int diffY = this.guiTop + 130;
         int diffBtnWidth = 55;
         int diffSpacing = 6;
         int totalDiffWidth = diffBtnWidth * 4 + diffSpacing * 3;
         int diffStartX = this.guiLeft + (300 - totalDiffWidth) / 2;
         this.buttonList.add(new DifficultyButton(10, diffStartX, diffY, diffBtnWidth, 22, "Genin", RaidDifficulty.GENIN, -14514142, -15641327));
         this.buttonList.add(new DifficultyButton(11, diffStartX + diffBtnWidth + diffSpacing, diffY, diffBtnWidth, 22, "Chunin", RaidDifficulty.CHUNIN, -14527062, -15649946));
         this.buttonList.add(new DifficultyButton(12, diffStartX + (diffBtnWidth + diffSpacing) * 2, diffY, diffBtnWidth, 22, "Jonin", RaidDifficulty.JONIN, -5609950, -8961007));
         this.buttonList.add(new DifficultyButton(13, diffStartX + (diffBtnWidth + diffSpacing) * 3, diffY, diffBtnWidth, 22, "Anbu", RaidDifficulty.ANBU, -5627358, -10088175));
         if (this.isQueued) {
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(2, centerX - 70, this.guiTop + 195, 140, 26, "Leave Queue", -5627358, -10088175));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(0, centerX - 45, this.guiTop + 225, 90, 20, "Back", -11184811, -13421773));
         } else if (this.hasPendingRewards) {
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(3, centerX - 70, this.guiTop + 165, 140, 26, "✦ Claim Rewards ✦", -3368670, -6724079));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(1, centerX - 70, this.guiTop + 195, 140, 26, "⚔ Queue for Raid", -14505438, -15636975));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(0, centerX - 45, this.guiTop + 225, 90, 20, "Back", -11184811, -13421773));
         } else {
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(1, centerX - 70, this.guiTop + 175, 140, 26, "⚔ Queue for Raid", -14505438, -15636975));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(0, centerX - 45, this.guiTop + 210, 90, 20, "Back", -11184811, -13421773));
         }

      }
   }

   public void updateScreen() {
      super.updateScreen();
      this.animationTick += 0.1F;
      boolean locked = !this.isInRaid && !RaidClientData.isQueueWindowOpen();

      for(GuiButton btn : this.buttonList) {
         if (btn.id != 0) {
            btn.enabled = !locked;
         }
      }

   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      if (this.isInRaid) {
         this.drawInRaidScreen(mouseX, mouseY, partialTicks);
      } else {
         float glowIntensity = (float)(0.3 + 0.15 * Math.sin((double)this.animationTick));
         int glowAlpha = (int)(glowIntensity * 255.0F);
         drawRect(this.guiLeft - 4, this.guiTop - 4, this.guiLeft + 300 + 4, this.guiTop + 240 + 4, glowAlpha << 24 | 13382451);
         this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + 300, this.guiTop + 240, -299226582, -301331958);
         this.drawBorder(this.guiLeft, this.guiTop, 300, 240, -3394765, 2);
         this.drawBorder(this.guiLeft + 4, this.guiTop + 4, 292, 232, -6741470, 1);
         String title = "§c§l☠ RAID BOSS QUEUE ☠";
         int titleWidth = this.fontRenderer.getStringWidth(title);
         this.fontRenderer.drawString(title, this.guiLeft + (300 - titleWidth) / 2 + 1, this.guiTop + 14 + 1, 0);
         this.fontRenderer.drawStringWithShadow(title, (float)(this.guiLeft + (300 - titleWidth) / 2), (float)(this.guiTop + 14), -10496);
         String subtitle = "§7Challenge powerful bosses with your party";
         if (RaidClientData.isQueueWindowOpen()) {
            long _nextMs = RaidClientData.getNextQueueEventTimeMs();
            long _remaining = _nextMs > 0L ? Math.max(0L, _nextMs - System.currentTimeMillis()) : 0L;
            subtitle = "§a§l⚫ Queue OPEN §r§7- closes in §e" + RaidQueueManager.formatHM(_remaining);
         }

         int subWidth = this.fontRenderer.getStringWidth(subtitle);
         this.fontRenderer.drawString(subtitle, this.guiLeft + (300 - subWidth) / 2, this.guiTop + 28, 10066329);
         this.drawHorizontalLine(this.guiLeft + 25, this.guiLeft + 300 - 25, this.guiTop + 40, -6741470);
         this.drawPanel(this.guiLeft + 15, this.guiTop + 50, 270, 55);
         String bossName = BOSS_NAMES[this.selectedBossIndex];
         String bossLabel = "§6§l" + bossName;
         int bossWidth = this.fontRenderer.getStringWidth(bossLabel);
         this.fontRenderer.drawStringWithShadow(bossLabel, (float)(this.guiLeft + (300 - bossWidth) / 2), (float)(this.guiTop + 58), 16777215);
         String bossDesc = "§7" + BOSS_DESCRIPTIONS[this.selectedBossIndex];
         int descWidth = this.fontRenderer.getStringWidth(bossDesc);
         this.fontRenderer.drawString(bossDesc, this.guiLeft + (300 - descWidth) / 2, this.guiTop + 75, 11184810);
         String countLabel = "§8(" + (this.selectedBossIndex + 1) + "/" + BOSS_IDS.length + ")";
         int countWidth = this.fontRenderer.getStringWidth(countLabel);
         this.fontRenderer.drawString(countLabel, this.guiLeft + (300 - countWidth) / 2, this.guiTop + 90, 6710886);
         String diffLabel = "§fSelect Difficulty:";
         int diffLabelWidth = this.fontRenderer.getStringWidth(diffLabel);
         this.fontRenderer.drawStringWithShadow(diffLabel, (float)(this.guiLeft + (300 - diffLabelWidth) / 2), (float)(this.guiTop + 115), 16777215);
         if (this.isQueued) {
            int liveQueueTime = (int)((System.currentTimeMillis() - this.queueStartTime) / 1000L);
            int searchX = this.guiLeft + 25;
            int searchY = this.guiTop + 155;
            int searchW = 250;
            int searchH = 32;
            float pulse = (float)((double)0.5F + 0.2 * Math.sin((double)(this.animationTick * 2.0F)));
            this.drawPanelWithLighting(searchX, searchY, searchW, searchH);
            int pulseAlpha = (int)(pulse * 150.0F);
            this.drawGradientRect(searchX + 1, searchY + 1, searchX + searchW - 1, searchY + searchH - 1, pulseAlpha << 24 | 13382400, pulseAlpha << 24 | 8921600);
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
         } else {
            String diffInfo = this.getDifficultyInfo(this.selectedDifficulty);
            int diffInfoWidth = this.fontRenderer.getStringWidth(diffInfo);
            this.fontRenderer.drawString(diffInfo, this.guiLeft + (300 - diffInfoWidth) / 2, this.guiTop + 155, 11184810);
         }

         super.drawScreen(mouseX, mouseY, partialTicks);
         if (!RaidClientData.isQueueWindowOpen()) {
            long nextMs = RaidClientData.getNextQueueEventTimeMs();
            long remaining = nextMs > 0L ? Math.max(0L, nextMs - System.currentTimeMillis()) : 0L;
            int overlayX1 = this.guiLeft + 8;
            int overlayY1 = this.guiTop + 8;
            int overlayX2 = this.guiLeft + 300 - 8;
            int overlayY2 = this.guiTop + 207;
            drawRect(overlayX1, overlayY1, overlayX2, overlayY2, -436207616);
            this.drawBorder(overlayX1, overlayY1, overlayX2 - overlayX1, overlayY2 - overlayY1, -3399134, 2);
            this.drawBorder(overlayX1 + 3, overlayY1 + 3, overlayX2 - overlayX1 - 6, overlayY2 - overlayY1 - 6, -10088175, 1);
            float pulse = (float)(0.7 + 0.3 * Math.sin((double)(this.animationTick * 2.0F)));
            int pulseAlpha = (int)(pulse * 255.0F);
            int pulseColor = pulseAlpha << 24 | 16729156;
            String lockIcons = "§c§l\ud83d\udd12  LOCKED  \ud83d\udd12";
            int lockIconsW = this.fontRenderer.getStringWidth(lockIcons);
            this.fontRenderer.drawStringWithShadow(lockIcons, (float)(this.guiLeft + (300 - lockIconsW) / 2), (float)(this.guiTop + 70), pulseColor);
            String bannerTitle = "§f§lRAID QUEUE CLOSED";
            int bannerW = this.fontRenderer.getStringWidth(bannerTitle);
            this.fontRenderer.drawStringWithShadow(bannerTitle, (float)(this.guiLeft + (300 - bannerW) / 2), (float)(this.guiTop + 95), -1);
            String countdown = "§7Opens in §e§l" + RaidQueueManager.formatHM(remaining);
            int cdW = this.fontRenderer.getStringWidth(countdown);
            this.fontRenderer.drawStringWithShadow(countdown, (float)(this.guiLeft + (300 - cdW) / 2), (float)(this.guiTop + 125), 16777215);
            String hint = "§8The queue rotates on a 30 min open / 2 hr cycle.";
            int hintW = this.fontRenderer.getStringWidth(hint);
            this.fontRenderer.drawString(hint, this.guiLeft + (300 - hintW) / 2, this.guiTop + 150, 10066329);
            String hint2 = "§8Admins can override with §7/raidadmin window";
            int hint2W = this.fontRenderer.getStringWidth(hint2);
            this.fontRenderer.drawString(hint2, this.guiLeft + (300 - hint2W) / 2, this.guiTop + 165, 10066329);
         }

      }
   }

   private void drawInRaidScreen(int mouseX, int mouseY, float partialTicks) {
      float glowIntensity = (float)(0.4 + 0.2 * Math.sin((double)this.animationTick * (double)1.5F));
      int glowAlpha = (int)(glowIntensity * 255.0F);
      drawRect(this.guiLeft - 6, this.guiTop - 6, this.guiLeft + 300 + 6, this.guiTop + 240 + 6, glowAlpha << 24 | 11141120);
      this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + 300, this.guiTop + 240, -299231979, -301333243);
      this.drawBorder(this.guiLeft, this.guiTop, 300, 240, -3399134, 2);
      this.drawBorder(this.guiLeft + 4, this.guiTop + 4, 292, 232, -7859951, 1);
      String title = "§c§l☠ RAID IN PROGRESS ☠";
      int titleWidth = this.fontRenderer.getStringWidth(title);
      this.fontRenderer.drawString(title, this.guiLeft + (300 - titleWidth) / 2 + 1, this.guiTop + 20 + 1, 0);
      this.fontRenderer.drawStringWithShadow(title, (float)(this.guiLeft + (300 - titleWidth) / 2), (float)(this.guiTop + 20), -48060);
      String subtitle = "§7Fighting: §6" + this.currentRaidBoss;
      int subWidth = this.fontRenderer.getStringWidth(subtitle);
      this.fontRenderer.drawString(subtitle, this.guiLeft + (300 - subWidth) / 2, this.guiTop + 35, 11184810);
      this.drawHorizontalLine(this.guiLeft + 25, this.guiLeft + 300 - 25, this.guiTop + 48, -7859951);
      int panelX = this.guiLeft + 20;
      int panelY = this.guiTop + 60;
      int panelW = 260;
      int panelH = 100;
      this.drawPanelWithLighting(panelX, panelY, panelW, panelH);
      this.drawGradientRect(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, 866779136, 575995904);
      int tipY = panelY + 12;
      String tip1 = "§8• §7Watch for boss mechanics!";
      String tip2 = "§8• §7Stay in safe zones during attacks";
      String tip3 = "§8• §7Work with your party";
      String tip4 = "§8• §7Boss health bar is at the top";
      this.fontRenderer.drawString(tip1, panelX + 12, tipY, 13421772);
      this.fontRenderer.drawString(tip2, panelX + 12, tipY + 14, 13421772);
      this.fontRenderer.drawString(tip3, panelX + 12, tipY + 28, 13421772);
      this.fontRenderer.drawString(tip4, panelX + 12, tipY + 42, 13421772);
      String barNote = "§eCustom boss bar displayed on screen";
      int barNoteW = this.fontRenderer.getStringWidth(barNote);
      this.fontRenderer.drawString(barNote, panelX + (panelW - barNoteW) / 2, tipY + 65, 16755200);
      float pulse = (float)(0.6 + 0.4 * Math.sin((double)(this.animationTick * 2.0F)));
      int indicatorAlpha = (int)(pulse * 255.0F);
      int indicatorX = this.guiLeft + 150;
      int indicatorY = this.guiTop + 175;
      String indicator = "§c§l⚔ FIGHT! ⚔";
      int indicatorWidth = this.fontRenderer.getStringWidth(indicator);
      this.fontRenderer.drawStringWithShadow(indicator, (float)(indicatorX - indicatorWidth / 2), (float)indicatorY, indicatorAlpha << 24 | 16729156);
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private String getDifficultyInfo(RaidDifficulty diff) {
      switch (diff) {
         case GENIN:
            return "§aEasy - " + diff.getMinPartySize() + " players | " + diff.getBaseRyoReward() + " ryo";
         case CHUNIN:
            return "§bNormal - " + diff.getMinPartySize() + " players | " + diff.getBaseRyoReward() + " ryo";
         case JONIN:
            return "§6Hard - " + diff.getMinPartySize() + " players | " + diff.getBaseRyoReward() + " ryo";
         case ANBU:
            return "§cSolo - Entry: " + diff.getEntryCost() + " ryo | Reward: " + diff.getBaseRyoReward() + " ryo";
         default:
            return "";
      }
   }

   private void drawPanel(int x, int y, int w, int h) {
      this.drawGradientRect(x, y, x + w, y + h, -868599238, -870704614);
      drawRect(x, y, x + w, y + 1, -11184811);
      drawRect(x, y + h - 1, x + w, y + h, -13421773);
      drawRect(x, y, x + 1, y + h, -11184811);
      drawRect(x + w - 1, y, x + w, y + h, -13421773);
   }

   private void drawPanelWithLighting(int x, int y, int w, int h) {
      this.drawGradientRect(x, y, x + w, y + h, -584443366, -585496054);
      drawRect(x, y, x + w, y + 1, -10075068);
      drawRect(x, y, x + 1, y + h, -10075068);
      drawRect(x + 1, y + h - 1, x + w, y + h, -14544623);
      drawRect(x + w - 1, y + 1, x + w, y + h - 1, -14544623);
      drawRect(x + 1, y + 1, x + w - 1, y + 2, 1157627903);
   }

   private void drawBorder(int x, int y, int w, int h, int color, int thickness) {
      drawRect(x, y, x + w, y + thickness, color);
      drawRect(x, y + h - thickness, x + w, y + h, color);
      drawRect(x, y, x + thickness, y + h, color);
      drawRect(x + w - thickness, y, x + w, y + h, color);
   }

   protected void actionPerformed(GuiButton button) {
      switch (button.id) {
         case 0:
            GuiRankedMenu.open();
            break;
         case 1:
            if (!RaidClientData.isQueueWindowOpen()) {
               long remaining = RaidClientData.getNextQueueEventTimeMs() - System.currentTimeMillis();
               if (remaining < 0L) {
                  remaining = 0L;
               }

               String msg = "§cRaid queue is currently §4LOCKED§c. Opens in §e" + RaidQueueManager.formatHM(remaining) + "§c.";
               if (Minecraft.getMinecraft().player != null) {
                  Minecraft.getMinecraft().player.sendMessage(new TextComponentString(msg));
               }
            } else {
               String bossId = BOSS_IDS[this.selectedBossIndex];
               RaidModInit.NETWORK.sendToServer(new RaidQueueMessage("queue", bossId, this.selectedDifficulty.ordinal()));
            }
            break;
         case 2:
            RaidModInit.NETWORK.sendToServer(new RaidQueueMessage("leave", "", 0));
            break;
         case 3:
            RaidModInit.NETWORK.sendToServer(new RaidQueueMessage("claimrewards", "", 0));
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         default:
            break;
         case 10:
            this.selectedDifficulty = RaidDifficulty.GENIN;
            break;
         case 11:
            this.selectedDifficulty = RaidDifficulty.CHUNIN;
            break;
         case 12:
            this.selectedDifficulty = RaidDifficulty.JONIN;
            break;
         case 13:
            this.selectedDifficulty = RaidDifficulty.ANBU;
            break;
         case 20:
            --this.selectedBossIndex;
            if (this.selectedBossIndex < 0) {
               this.selectedBossIndex = BOSS_IDS.length - 1;
            }
            break;
         case 21:
            ++this.selectedBossIndex;
            if (this.selectedBossIndex >= BOSS_IDS.length) {
               this.selectedBossIndex = 0;
            }
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

   public static void open() {
      Minecraft mc = Minecraft.getMinecraft();
      GuiRaidMenu gui = new GuiRaidMenu();
      mc.displayGuiScreen(gui);
   }

   private class DifficultyButton extends GuiRankedMenu.GuiButtonGradient {
      private final RaidDifficulty difficulty;

      public DifficultyButton(int id, int x, int y, int width, int height, String text, RaidDifficulty diff, int colorTop, int colorBottom) {
         super(id, x, y, width, height, text, colorTop, colorBottom);
         this.difficulty = diff;
      }

      public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
         if (this.visible) {
            if (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height) {
               boolean var7 = true;
            } else {
               boolean var10000 = false;
            }

            boolean selected = GuiRaidMenu.this.selectedDifficulty == this.difficulty;
            if (selected) {
               drawRect(this.x - 2, this.y - 2, this.x + this.width + 2, this.y + this.height + 2, -1);
            }

            super.drawButton(mc, mouseX, mouseY, partialTicks);
            if (selected) {
               mc.fontRenderer.drawString("✓", this.x + this.width - 10, this.y + 2, 16777215);
            }

         }
      }
   }
}
