
package net.luck.narutoaddon.OtherCode.shop.gui;

import net.luck.narutoaddon.OtherCode.shop.core.ItemCategory;
import net.luck.narutoaddon.OtherCode.shop.core.ItemRarity;
import net.luck.narutoaddon.OtherCode.shop.core.ShopModInit;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateDefinition;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateRegistry;
import net.luck.narutoaddon.OtherCode.shop.network.ShopAdminClientData;
import net.luck.narutoaddon.OtherCode.shop.network.ShopAdminRestoreMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ShopAdminGui extends GuiScreen {
   private static final int GUI_WIDTH = 420;
   private static final int GUI_HEIGHT = 300;
   private static final int BG_COLOR = -14016748;
   private static final int BORDER_COLOR = -3626930;
   private static final int HEADER_COLOR = -10496;
   private static final int LABEL_COLOR = -3626930;
   private static final int VALUE_COLOR = -1;
   private static final int SEPARATOR_COLOR = 1154000974;
   private static final int TAB_ACTIVE_BG = -12964070;
   private static final int TAB_INACTIVE_BG = -15067890;
   private int guiLeft;
   private int guiTop;
   private int currentTab = 0;
   private int historyScroll = 0;
   private int ownedScroll = 0;
   private int trackedScroll = 0;
   private int restoreLogScroll = 0;
   private static final int HISTORY_VISIBLE_ROWS = 16;
   private static final int OWNED_VISIBLE_ROWS = 20;
   private static final int TRACKED_VISIBLE_ROWS = 12;
   private static final int RESTORE_LOG_VISIBLE_ROWS = 16;
   private static final int TAB_OVERVIEW = 0;
   private static final int TAB_HISTORY = 1;
   private static final int TAB_OWNED = 2;
   private static final int TAB_TRACKED = 3;
   private static final int TAB_RESTORE_LOG = 4;
   private static final int BTN_TAB_OVERVIEW = 100;
   private static final int BTN_TAB_HISTORY = 101;
   private static final int BTN_TAB_OWNED = 102;
   private static final int BTN_TAB_TRACKED = 103;
   private static final int BTN_TAB_RESTORE_LOG = 104;
   private static final int BTN_CLOSE = 105;
   private static final int BTN_CAT_FILTER = 200;
   private static final int BTN_STATUS_FILTER = 201;
   private static final int BTN_RESTORE_BASE = 300;
   private String searchText = "";
   private int categoryFilter = 0;
   private int statusFilter = 0;
   private GuiTextField searchField;
   private int confirmingRestoreIndex = -1;
   private long confirmExpireTime = 0L;
   private List<ShopAdminClientData.AdminTrackedItem> filteredTrackedItems = new ArrayList();
   private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");
   private static final String[] CATEGORY_LABELS = new String[]{"ALL", "DOJUTSU", "NATURE", "SCROLL", "SPECIAL", "ARMOR"};
   private static final String[] STATUS_LABELS = new String[]{"ALL", "OWNED", "MISSING"};

   public void initGui() {
      this.guiLeft = (this.width - 420) / 2;
      this.guiTop = (this.height - 300) / 2;
      this.buttonList.clear();
      int tabY = this.guiTop + 4;
      int tabW = 70;
      int tabH = 16;
      int tabX = this.guiLeft + 6;
      int tabGap = 3;
      this.buttonList.add(new TabButton(100, tabX, tabY, tabW, tabH, "Overview", this.currentTab == 0));
      tabX += tabW + tabGap;
      this.buttonList.add(new TabButton(101, tabX, tabY, tabW, tabH, "History", this.currentTab == 1));
      tabX += tabW + tabGap;
      this.buttonList.add(new TabButton(102, tabX, tabY, tabW, tabH, "Owned", this.currentTab == 2));
      tabX += tabW + tabGap;
      this.buttonList.add(new TabButton(103, tabX, tabY, tabW, tabH, "Jutsu/KG", this.currentTab == 3));
      tabX += tabW + tabGap;
      this.buttonList.add(new TabButton(104, tabX, tabY, tabW, tabH, "Restores", this.currentTab == 4));
      this.buttonList.add(new TabButton(105, this.guiLeft + 420 - 50, tabY, 40, tabH, "§cClose", false));
      if (this.currentTab == 3) {
         this.searchField = new GuiTextField(0, this.fontRenderer, this.guiLeft + 10, this.guiTop + 55, 180, 14);
         this.searchField.setMaxStringLength(30);
         this.searchField.setText(this.searchText);
         this.searchField.setFocused(true);
         this.buttonList.add(new TabButton(200, this.guiLeft + 200, this.guiTop + 54, 70, 16, "Cat: " + CATEGORY_LABELS[this.categoryFilter], false));
         this.buttonList.add(new TabButton(201, this.guiLeft + 276, this.guiTop + 54, 70, 16, "St: " + STATUS_LABELS[this.statusFilter], false));
         this.rebuildFilteredList();
         int startIdx = this.trackedScroll;
         int endIdx = Math.min(startIdx + 12, this.filteredTrackedItems.size());
         int btnIdx = 0;

         for(int i = startIdx; i < endIdx; ++i) {
            ShopAdminClientData.AdminTrackedItem item = (ShopAdminClientData.AdminTrackedItem)this.filteredTrackedItems.get(i);
            if (!item.owned) {
               int rowY = this.guiTop + 88 + (i - startIdx) * 14;
               boolean isConfirming = this.confirmingRestoreIndex == i;
               String btnLabel = isConfirming ? "§eCONFIRM?" : "RESTORE";
               this.buttonList.add(new TabButton(300 + btnIdx, this.guiLeft + 420 - 60, rowY - 1, 50, 12, btnLabel, isConfirming));
               ++btnIdx;
            }
         }
      } else {
         this.searchField = null;
      }

   }

   private void rebuildFilteredList() {
      this.filteredTrackedItems.clear();

      for(ShopAdminClientData.AdminTrackedItem item : ShopAdminClientData.trackedItems) {
         if (this.searchText.isEmpty() || item.displayName.toLowerCase().contains(this.searchText.toLowerCase())) {
            if (this.categoryFilter > 0) {
               String catName = CATEGORY_LABELS[this.categoryFilter];
               if (!catName.equalsIgnoreCase(item.category)) {
                  continue;
               }
            }

            if ((this.statusFilter != 1 || item.owned) && (this.statusFilter != 2 || !item.owned)) {
               this.filteredTrackedItems.add(item);
            }
         }
      }

   }

   protected void actionPerformed(GuiButton button) {
      switch (button.id) {
         case 100:
            this.currentTab = 0;
            this.initGui();
            break;
         case 101:
            this.currentTab = 1;
            this.historyScroll = 0;
            this.initGui();
            break;
         case 102:
            this.currentTab = 2;
            this.ownedScroll = 0;
            this.initGui();
            break;
         case 103:
            this.currentTab = 3;
            this.trackedScroll = 0;
            this.confirmingRestoreIndex = -1;
            this.initGui();
            break;
         case 104:
            this.currentTab = 4;
            this.restoreLogScroll = 0;
            this.initGui();
            break;
         case 105:
            this.mc.displayGuiScreen((GuiScreen)null);
            break;
         case 200:
            this.categoryFilter = (this.categoryFilter + 1) % CATEGORY_LABELS.length;
            this.trackedScroll = 0;
            this.confirmingRestoreIndex = -1;
            this.initGui();
            break;
         case 201:
            this.statusFilter = (this.statusFilter + 1) % STATUS_LABELS.length;
            this.trackedScroll = 0;
            this.confirmingRestoreIndex = -1;
            this.initGui();
            break;
         default:
            if (button.id >= 300 && button.id < 312) {
               int visibleIdx = button.id - 300;
               int actualIdx = this.mapVisibleToFilteredIndex(visibleIdx);
               if (actualIdx >= 0 && actualIdx < this.filteredTrackedItems.size()) {
                  ShopAdminClientData.AdminTrackedItem item = (ShopAdminClientData.AdminTrackedItem)this.filteredTrackedItems.get(actualIdx);
                  if (!item.owned) {
                     if (this.confirmingRestoreIndex == actualIdx && System.currentTimeMillis() < this.confirmExpireTime) {
                        ShopModInit.NETWORK.sendToServer(new ShopAdminRestoreMessage(ShopAdminClientData.targetUUID, item.regName, item.meta, item.displayName));
                        this.confirmingRestoreIndex = -1;
                        this.initGui();
                     } else {
                        this.confirmingRestoreIndex = actualIdx;
                        this.confirmExpireTime = System.currentTimeMillis() + 3000L;
                        this.initGui();
                     }
                  }
               }
            }
      }

   }

   private int mapVisibleToFilteredIndex(int visibleIdx) {
      int startIdx = this.trackedScroll;
      int endIdx = Math.min(startIdx + 12, this.filteredTrackedItems.size());
      int missingBtnCount = 0;

      for(int i = startIdx; i < endIdx; ++i) {
         ShopAdminClientData.AdminTrackedItem item = (ShopAdminClientData.AdminTrackedItem)this.filteredTrackedItems.get(i);
         if (!item.owned) {
            if (missingBtnCount == visibleIdx) {
               return i;
            }

            ++missingBtnCount;
         }
      }

      return -1;
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      drawRect(this.guiLeft, this.guiTop, this.guiLeft + 420, this.guiTop + 300, -14016748);
      this.drawHorizontalLine(this.guiLeft, this.guiLeft + 420 - 1, this.guiTop, -3626930);
      this.drawHorizontalLine(this.guiLeft, this.guiLeft + 420 - 1, this.guiTop + 300 - 1, -3626930);
      this.drawVerticalLine(this.guiLeft, this.guiTop, this.guiTop + 300 - 1, -3626930);
      this.drawVerticalLine(this.guiLeft + 420 - 1, this.guiTop, this.guiTop + 300 - 1, -3626930);
      this.drawHorizontalLine(this.guiLeft + 1, this.guiLeft + 420 - 2, this.guiTop + 1, 1154000974);
      this.drawHorizontalLine(this.guiLeft + 1, this.guiLeft + 420 - 2, this.guiTop + 300 - 2, 1154000974);
      this.drawHorizontalLine(this.guiLeft + 4, this.guiLeft + 420 - 5, this.guiTop + 24, -3626930);
      String title = "§6§lShop Admin: §e" + ShopAdminClientData.targetName;
      this.fontRenderer.drawStringWithShadow(title, (float)(this.guiLeft + 210 - this.fontRenderer.getStringWidth("Shop Admin: " + ShopAdminClientData.targetName) / 2), (float)(this.guiTop + 28), -10496);
      String uuid = ShopAdminClientData.targetUUID;
      if (uuid.length() > 36) {
         uuid = uuid.substring(0, 36);
      }

      this.fontRenderer.drawStringWithShadow("§8UUID: " + uuid, (float)(this.guiLeft + 10), (float)(this.guiTop + 40), -7829368);
      int contentY = this.guiTop + 54;
      switch (this.currentTab) {
         case 0:
            this.drawOverviewTab(contentY);
            break;
         case 1:
            this.drawHistoryTab(contentY, mouseX, mouseY);
            break;
         case 2:
            this.drawOwnedTab(contentY);
            break;
         case 3:
            this.drawTrackedTab(contentY);
            break;
         case 4:
            this.drawRestoreLogTab(contentY);
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
      if (this.searchField != null && this.currentTab == 3) {
         this.searchField.drawTextBox();
      }

   }

   private void drawOverviewTab(int startY) {
      int x = this.guiLeft + 15;
      int lineH = 13;
      this.drawSectionHeader(x, startY, "Economy");
      int y = startY + lineH + 2;
      this.drawLabelValue(x, y, "Ryo Balance:", this.formatNumber(ShopAdminClientData.ryoBalance));
      y += lineH;
      this.drawLabelValue(x, y, "Token Balance:", String.valueOf(ShopAdminClientData.tokenBalance));
      y += lineH;
      this.drawLabelValue(x, y, "Login Streak:", ShopAdminClientData.loginStreak + " day(s)");
      y += lineH;
      this.drawLabelValue(x, y, "Overflow Items:", String.valueOf(ShopAdminClientData.overflowCount));
      y += lineH + 4;
      this.drawSectionHeader(x, y, "Lifetime Stats");
      y += lineH + 2;
      this.drawLabelValue(x, y, "Lifetime Ryo Spent:", this.formatNumber(ShopAdminClientData.lifetimeSpent));
      y += lineH;
      this.drawLabelValue(x, y, "Lifetime Crates Opened:", String.valueOf(ShopAdminClientData.lifetimeCratesOpened));
      y += lineH;
      long recentSpent = 0L;

      for(ShopAdminClientData.AdminHistoryEntry entry : ShopAdminClientData.history) {
         CrateDefinition crate = CrateRegistry.getById(entry.crateId);
         if (crate != null) {
            recentSpent += crate.getPrice();
         }
      }

      this.drawLabelValue(x, y, "Recent Spent (last 50):", this.formatNumber(recentSpent));
      y += lineH + 4;
      if (!ShopAdminClientData.pityCounters.isEmpty()) {
         this.drawSectionHeader(x, y, "Pity Counters");
         y += lineH + 2;

         for(Map.Entry<String, Integer> entry : ShopAdminClientData.pityCounters.entrySet()) {
            if ((Integer)entry.getValue() > 0) {
               this.drawLabelValue(x, y, (String)entry.getKey() + ":", entry.getValue() + " rolls since S");
               y += lineH;
               if (y > this.guiTop + 300 - 20) {
                  break;
               }
            }
         }
      }

      y += 4;
      this.drawLabelValue(x, y, "History Entries:", String.valueOf(ShopAdminClientData.history.size()));
      y += lineH;
      this.drawLabelValue(x, y, "Unique Owned Items:", String.valueOf(ShopAdminClientData.ownedItems.size()));
   }

   private void drawHistoryTab(int startY, int mouseX, int mouseY) {
      int x = this.guiLeft + 10;
      List<ShopAdminClientData.AdminHistoryEntry> history = ShopAdminClientData.history;
      this.fontRenderer.drawStringWithShadow("§nDate", (float)x, (float)startY, -3626930);
      this.fontRenderer.drawStringWithShadow("§nCrate", (float)(x + 75), (float)startY, -3626930);
      this.fontRenderer.drawStringWithShadow("§nItem Won", (float)(x + 170), (float)startY, -3626930);
      this.fontRenderer.drawStringWithShadow("§nRarity", (float)(x + 310), (float)startY, -3626930);
      this.fontRenderer.drawStringWithShadow("§nTokens", (float)(x + 370), (float)startY, -3626930);
      int y = startY + 12;
      this.drawHorizontalLine(x, this.guiLeft + 420 - 10, y, 1154000974);
      y += 3;
      if (history.isEmpty()) {
         this.fontRenderer.drawStringWithShadow("§7No history entries.", (float)x, (float)y, -7829368);
      } else {
         int maxScroll = Math.max(0, history.size() - 16);
         if (this.historyScroll > maxScroll) {
            this.historyScroll = maxScroll;
         }

         for(int i = this.historyScroll; i < Math.min(this.historyScroll + 16, history.size()); ++i) {
            ShopAdminClientData.AdminHistoryEntry entry = (ShopAdminClientData.AdminHistoryEntry)history.get(i);
            String date = this.dateFormat.format(new Date(entry.timestamp));
            this.fontRenderer.drawStringWithShadow(date, (float)x, (float)y, -3355444);
            String crateDisplay = entry.crateId;
            if (crateDisplay.length() > 14) {
               crateDisplay = crateDisplay.substring(0, 14);
            }

            this.fontRenderer.drawStringWithShadow(crateDisplay, (float)(x + 75), (float)y, -3355444);
            String itemDisplay = entry.displayName;
            if (itemDisplay.length() > 20) {
               itemDisplay = itemDisplay.substring(0, 20);
            }

            this.fontRenderer.drawStringWithShadow(itemDisplay, (float)(x + 170), (float)y, -3355444);
            ItemRarity rarity = ItemRarity.fromOrdinal(entry.rarityOrdinal);
            this.fontRenderer.drawStringWithShadow(rarity.displayName, (float)(x + 310), (float)y, rarity.color);
            if (entry.tokenAmount > 0) {
               this.fontRenderer.drawStringWithShadow("+" + entry.tokenAmount, (float)(x + 370), (float)y, -22016);
            } else {
               this.fontRenderer.drawStringWithShadow("-", (float)(x + 370), (float)y, -10066330);
            }

            y += 13;
         }

         if (history.size() > 16) {
            String scrollInfo = this.historyScroll + 1 + "-" + Math.min(this.historyScroll + 16, history.size()) + " of " + history.size();
            this.fontRenderer.drawStringWithShadow("§7" + scrollInfo, (float)(this.guiLeft + 420 - 10 - this.fontRenderer.getStringWidth(scrollInfo)), (float)(this.guiTop + 300 - 14), -7829368);
         }

      }
   }

   private void drawOwnedTab(int startY) {
      int x = this.guiLeft + 15;
      List<String> owned = ShopAdminClientData.ownedItems;
      this.fontRenderer.drawStringWithShadow("§eOwned Items (" + owned.size() + " unique)", (float)x, (float)startY, -10496);
      int y = startY + 14;
      this.drawHorizontalLine(x, this.guiLeft + 420 - 15, y, 1154000974);
      y += 4;
      if (owned.isEmpty()) {
         this.fontRenderer.drawStringWithShadow("§7No owned items.", (float)x, (float)y, -7829368);
      } else {
         int maxScroll = Math.max(0, owned.size() - 20);
         if (this.ownedScroll > maxScroll) {
            this.ownedScroll = maxScroll;
         }

         for(int i = this.ownedScroll; i < Math.min(this.ownedScroll + 20, owned.size()); ++i) {
            String itemKey = (String)owned.get(i);
            this.fontRenderer.drawStringWithShadow("§7" + itemKey, (float)x, (float)y, -3355444);
            y += 11;
         }

         if (owned.size() > 20) {
            String scrollInfo = this.ownedScroll + 1 + "-" + Math.min(this.ownedScroll + 20, owned.size()) + " of " + owned.size();
            this.fontRenderer.drawStringWithShadow("§7" + scrollInfo, (float)(this.guiLeft + 420 - 15 - this.fontRenderer.getStringWidth(scrollInfo)), (float)(this.guiTop + 300 - 14), -7829368);
         }

      }
   }

   private void drawTrackedTab(int startY) {
      int x = this.guiLeft + 10;
      int y = startY + 20;
      this.fontRenderer.drawStringWithShadow("§nItem", (float)x, (float)y, -3626930);
      this.fontRenderer.drawStringWithShadow("§nXP", (float)(x + 190), (float)y, -3626930);
      this.fontRenderer.drawStringWithShadow("§nOwner", (float)(x + 240), (float)y, -3626930);
      this.fontRenderer.drawStringWithShadow("§nStatus", (float)(x + 290), (float)y, -3626930);
      y += 12;
      this.drawHorizontalLine(x, this.guiLeft + 420 - 10, y, 1154000974);
      y += 2;
      if (this.filteredTrackedItems.isEmpty()) {
         this.fontRenderer.drawStringWithShadow("§7No tracked items match filters.", (float)x, (float)(y + 4), -7829368);
         this.drawTrackedFooter();
      } else {
         int maxScroll = Math.max(0, this.filteredTrackedItems.size() - 12);
         if (this.trackedScroll > maxScroll) {
            this.trackedScroll = maxScroll;
         }

         int startIdx = this.trackedScroll;
         int endIdx = Math.min(startIdx + 12, this.filteredTrackedItems.size());

         for(int i = startIdx; i < endIdx; ++i) {
            ShopAdminClientData.AdminTrackedItem item = (ShopAdminClientData.AdminTrackedItem)this.filteredTrackedItems.get(i);
            if (!item.owned) {
               drawRect(x - 2, y, this.guiLeft + 420 - 8, y + 13, 1090453504);
            }

            ItemCategory cat = ItemCategory.fromName(item.category);
            int nameColor = cat != null ? cat.color : -3355444;
            String displayName = item.displayName;
            if (displayName.length() > 22) {
               displayName = displayName.substring(0, 22);
            }

            this.fontRenderer.drawStringWithShadow(displayName, (float)x, (float)(y + 2), nameColor);
            if (item.jutsuXp != null && item.jutsuXp.length > 0) {
               int totalXp = 0;

               for(int xp : item.jutsuXp) {
                  totalXp += xp;
               }

               this.fontRenderer.drawStringWithShadow(String.valueOf(totalXp), (float)(x + 190), (float)(y + 2), -3355444);
            } else {
               this.fontRenderer.drawStringWithShadow("-", (float)(x + 190), (float)(y + 2), -10066330);
            }

            this.fontRenderer.drawStringWithShadow(item.hasOwner ? "Y" : "N", (float)(x + 248), (float)(y + 2), item.hasOwner ? -11141291 : -7829368);
            if (item.owned) {
               this.fontRenderer.drawStringWithShadow("OWNED", (float)(x + 290), (float)(y + 2), -11141291);
            } else {
               this.fontRenderer.drawStringWithShadow("MISSING", (float)(x + 290), (float)(y + 2), -43691);
            }

            y += 14;
         }

         this.drawTrackedFooter();
         if (this.filteredTrackedItems.size() > 12) {
            String scrollInfo = this.trackedScroll + 1 + "-" + Math.min(this.trackedScroll + 12, this.filteredTrackedItems.size()) + " of " + this.filteredTrackedItems.size();
            this.fontRenderer.drawStringWithShadow("§7" + scrollInfo, (float)(this.guiLeft + 420 - 10 - this.fontRenderer.getStringWidth(scrollInfo)), (float)(this.guiTop + 300 - 14), -7829368);
         }

      }
   }

   private void drawTrackedFooter() {
      int missingCount = 0;

      for(ShopAdminClientData.AdminTrackedItem item : this.filteredTrackedItems) {
         if (!item.owned) {
            ++missingCount;
         }
      }

      String lastScan = "N/A";
      if (!ShopAdminClientData.trackedItems.isEmpty()) {
         lastScan = this.dateFormat.format(new Date(((ShopAdminClientData.AdminTrackedItem)ShopAdminClientData.trackedItems.get(0)).lastSeen));
      }

      String footer = this.filteredTrackedItems.size() + " items (" + missingCount + " MISSING) | Last scan: " + lastScan;
      this.fontRenderer.drawStringWithShadow("§7" + footer, (float)(this.guiLeft + 10), (float)(this.guiTop + 300 - 26), -7829368);
   }

   private void drawRestoreLogTab(int startY) {
      int x = this.guiLeft + 10;
      this.fontRenderer.drawStringWithShadow("§eRestore History (" + ShopAdminClientData.restoreLog.size() + " entries)", (float)x, (float)startY, -10496);
      int y = startY + 14;
      this.fontRenderer.drawStringWithShadow("§nDate", (float)x, (float)y, -3626930);
      this.fontRenderer.drawStringWithShadow("§nAdmin", (float)(x + 85), (float)y, -3626930);
      this.fontRenderer.drawStringWithShadow("§nItem", (float)(x + 175), (float)y, -3626930);
      this.fontRenderer.drawStringWithShadow("§nPlayer", (float)(x + 320), (float)y, -3626930);
      y += 12;
      this.drawHorizontalLine(x, this.guiLeft + 420 - 10, y, 1154000974);
      y += 3;
      List<ShopAdminClientData.AdminRestoreLog> logs = ShopAdminClientData.restoreLog;
      if (logs.isEmpty()) {
         this.fontRenderer.drawStringWithShadow("§7No restore history.", (float)x, (float)y, -7829368);
      } else {
         int maxScroll = Math.max(0, logs.size() - 16);
         if (this.restoreLogScroll > maxScroll) {
            this.restoreLogScroll = maxScroll;
         }

         for(int i = this.restoreLogScroll; i < Math.min(this.restoreLogScroll + 16, logs.size()); ++i) {
            ShopAdminClientData.AdminRestoreLog entry = (ShopAdminClientData.AdminRestoreLog)logs.get(i);
            String date = this.dateFormat.format(new Date(entry.timestamp));
            this.fontRenderer.drawStringWithShadow(date, (float)x, (float)y, -3355444);
            String admin = entry.adminName;
            if (admin.length() > 12) {
               admin = admin.substring(0, 12);
            }

            this.fontRenderer.drawStringWithShadow(admin, (float)(x + 85), (float)y, -3355444);
            String itemName = entry.itemName;
            if (itemName.length() > 20) {
               itemName = itemName.substring(0, 20);
            }

            this.fontRenderer.drawStringWithShadow(itemName, (float)(x + 175), (float)y, -22016);
            String playerName = entry.targetPlayerName;
            if (playerName.length() > 14) {
               playerName = playerName.substring(0, 14);
            }

            this.fontRenderer.drawStringWithShadow(playerName, (float)(x + 320), (float)y, -3355444);
            y += 13;
         }

         if (logs.size() > 16) {
            String scrollInfo = this.restoreLogScroll + 1 + "-" + Math.min(this.restoreLogScroll + 16, logs.size()) + " of " + logs.size();
            this.fontRenderer.drawStringWithShadow("§7" + scrollInfo, (float)(this.guiLeft + 420 - 10 - this.fontRenderer.getStringWidth(scrollInfo)), (float)(this.guiTop + 300 - 14), -7829368);
         }

      }
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      int scroll = Mouse.getEventDWheel();
      if (scroll != 0) {
         int dir = scroll > 0 ? -1 : 1;
         if (this.currentTab == 1) {
            this.historyScroll = Math.max(0, Math.min(this.historyScroll + dir * 3, Math.max(0, ShopAdminClientData.history.size() - 16)));
         } else if (this.currentTab == 2) {
            this.ownedScroll = Math.max(0, Math.min(this.ownedScroll + dir * 3, Math.max(0, ShopAdminClientData.ownedItems.size() - 20)));
         } else if (this.currentTab == 3) {
            this.trackedScroll = Math.max(0, Math.min(this.trackedScroll + dir * 3, Math.max(0, this.filteredTrackedItems.size() - 12)));
            this.initGui();
         } else if (this.currentTab == 4) {
            this.restoreLogScroll = Math.max(0, Math.min(this.restoreLogScroll + dir * 3, Math.max(0, ShopAdminClientData.restoreLog.size() - 16)));
         }
      }

   }

   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (this.searchField != null && this.searchField.textboxKeyTyped(typedChar, keyCode)) {
         this.searchText = this.searchField.getText();
         this.trackedScroll = 0;
         this.confirmingRestoreIndex = -1;
         this.initGui();
      } else {
         super.keyTyped(typedChar, keyCode);
      }
   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      if (this.searchField != null) {
         this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
      }

      super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   public void updateScreen() {
      super.updateScreen();
      if (this.searchField != null) {
         this.searchField.updateCursorCounter();
      }

      if (this.confirmingRestoreIndex >= 0 && System.currentTimeMillis() > this.confirmExpireTime) {
         this.confirmingRestoreIndex = -1;
         this.initGui();
      }

   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   private void drawSectionHeader(int x, int y, String text) {
      this.fontRenderer.drawStringWithShadow("§6§l" + text, (float)x, (float)y, -10496);
      this.drawHorizontalLine(x, x + this.fontRenderer.getStringWidth(text) + 4, y + 10, 1154000974);
   }

   private void drawLabelValue(int x, int y, String label, String value) {
      this.fontRenderer.drawStringWithShadow("§e" + label, (float)(x + 4), (float)y, -3626930);
      this.fontRenderer.drawStringWithShadow("§f" + value, (float)(x + 4 + this.fontRenderer.getStringWidth(label) + 6), (float)y, -1);
   }

   private String formatNumber(long value) {
      if (value >= 1000000L) {
         return String.format("%,.1fM", (double)value / (double)1000000.0F);
      } else {
         return value >= 1000L ? String.format("%,d", value) : String.valueOf(value);
      }
   }

   private static class TabButton extends GuiButton {
      private final boolean active;

      public TabButton(int id, int x, int y, int width, int height, String text, boolean active) {
         super(id, x, y, width, height, text);
         this.active = active;
      }

      public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
         if (this.visible) {
            boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int bg = this.active ? -12964070 : (hovered ? -14015208 : -15067890);
            int border = this.active ? -3626930 : -10070736;
            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, bg);
            this.drawHorizontalLine(this.x, this.x + this.width - 1, this.y, border);
            this.drawHorizontalLine(this.x, this.x + this.width - 1, this.y + this.height - 1, this.active ? bg : border);
            this.drawVerticalLine(this.x, this.y, this.y + this.height - 1, border);
            this.drawVerticalLine(this.x + this.width - 1, this.y, this.y + this.height - 1, border);
            int textColor = this.active ? -10496 : (hovered ? -2241400 : -6715290);
            mc.fontRenderer.drawStringWithShadow(this.displayString, (float)(this.x + this.width / 2 - mc.fontRenderer.getStringWidth(this.displayString) / 2), (float)(this.y + (this.height - 8) / 2), textColor);
         }
      }
   }
}
