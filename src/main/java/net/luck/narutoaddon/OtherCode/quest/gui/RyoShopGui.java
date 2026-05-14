
package net.luck.narutoaddon.OtherCode.quest.gui;

import net.luck.narutoaddon.OtherCode.gui.GuiRankedMenu;
import net.luck.narutoaddon.OtherCode.shop.building.BuildingCartData;
import net.luck.narutoaddon.OtherCode.shop.building.BuildingPurchaseMessage;
import net.luck.narutoaddon.OtherCode.shop.building.BuildingShopRegistry;
import net.luck.narutoaddon.OtherCode.shop.core.ItemRarity;
import net.luck.narutoaddon.OtherCode.shop.core.ShopCategory;
import net.luck.narutoaddon.OtherCode.shop.core.ShopModInit;
import net.luck.narutoaddon.OtherCode.shop.network.BattlePassActionMessage;
import net.luck.narutoaddon.OtherCode.shop.network.ShopActionMessage;
import net.luck.narutoaddon.OtherCode.shop.network.ShopClientData;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

@SideOnly(Side.CLIENT)
public class RyoShopGui extends GuiScreen {
   private static final int GUI_WIDTH = 400;
   private static final int GUI_HEIGHT = 300;
   private static final int GUI_WIDTH_EXPANDED = 480;
   private static final int GUI_HEIGHT_EXPANDED = 340;
   private static final int COLOR_BORDER_OUTER = -7706054;
   private static final int COLOR_BORDER_INNER = -9809872;
   private static final int COLOR_BG_TOP = -298833888;
   private static final int COLOR_BG_BOT = -300280816;
   private static final int COLOR_TITLE = -1521552;
   private static final int COLOR_PANEL_TOP = -868600792;
   private static final int COLOR_PANEL_BOT = -870442476;
   private static final int COLOR_SEPARATOR = -9807296;
   private static final int COLOR_TEXT_HEADER = -1521552;
   private static final int COLOR_TEXT_BODY = -2832216;
   private static final int COLOR_TEXT_DIM = -7700886;
   private static final int BUTTON_CLOSE = 0;
   private static final int BUTTON_DEPOSIT = 1;
   private static final int BUTTON_OPEN_CRATE = 2;
   private static final int BUTTON_HISTORY_TOGGLE = 3;
   private static final int BUTTON_MULTI_OPEN = 4;
   private static final int BUTTON_CLAIM_OVERFLOW = 5;
   private static final int BUTTON_TAB_RYO = 6;
   private static final int BUTTON_TAB_TOKEN = 7;
   private static final int BUTTON_TAB_CRYSTAL = 8;
   private static final int BUTTON_CAT_BASE = 10;
   private static final int BUTTON_CRATE_BASE = 20;
   private static final int BUTTON_TOKEN_BUY_BASE = 50;
   private static final int BUTTON_TAB_PASS = 9;
   private static final int BUTTON_PASS_BUY_KAGE = 70;
   private static final int BUTTON_PASS_BUY_OTSUTSUKI = 71;
   private static final int BUTTON_PASS_CLAIM = 72;
   private static final int BUTTON_PASS_SKIP = 73;
   private static final int BUTTON_KILL_EFFECT_BASE = 80;
   private static final int BUTTON_KILL_EFFECTS_OPEN = 90;
   private static final int BUTTON_KILL_EFFECTS_BACK = 91;
   private static final int BUTTON_TAB_REDEEM = 95;
   private static final int BUTTON_REDEEM_CLAIM = 96;
   private static final int BUTTON_TRAVEL_BASE = 60;
   private static final int BUILDING_SUB_TAB_BASE = 800;
   private static final int BUILDING_ITEM_PLUS_BASE = 810;
   private static final int BUILDING_ITEM_MINUS_BASE = 860;
   private static final int BUILDING_ITEM_PLUS64_BASE = 910;
   private static final int BUILDING_ITEM_SHULKER_BASE = 1010;
   private static final int BUILDING_PURCHASE_BTN = 960;
   private static final int BUILDING_CART_REMOVE_BASE = 961;
   private static final String[] TRAVEL_VILLAGE_IDS = new String[]{"leaf", "sand", "mist", "cloud", "stone", "rain"};
   private static final String[] TRAVEL_VILLAGE_NAMES = new String[]{"§2Leaf Village §a(Konoha)", "§eSand Village §6(Suna)", "§3Mist Village §b(Kiri)", "§7Cloud Village §8(Kumo)", "§6Stone Village §e(Iwa)", "§1Rain Village §9(Ame)"};
   private static final int[][] TRAVEL_VILLAGE_COLORS = new int[][]{{-15050214, -15910387}, {-9807344, -11912696}, {-15058342, -15916992}, {-12961206, -14540240}, {-10864102, -12966896}, {-15066534, -15724480}};
   private static final int STRIP_ITEM_WIDTH = 60;
   private static final int STRIP_VISIBLE_ITEMS = 6;
   private static final int STRIP_HEIGHT = 60;
   private static final float ANIMATION_DURATION_MS = 4000.0F;
   private static final int TARGET_INDEX = 45;
   private int guiLeft;
   private int guiTop;
   private ShopCategory selectedCategory;
   private String selectedCrateId;
   private int crateListScroll;
   private boolean historyOpen;
   private int historyScroll;
   private int lootScrollOffset;
   private boolean tokenShopMode;
   private boolean crystalShopMode;
   private boolean passMode;
   private int passScroll;
   private boolean redeemMode;
   private boolean killEffectsOpen;
   private int lastKnownOverflowCount;
   private int tokenShopScroll;
   private List<TokenShopItem> tokenShopItems;
   private int buildingSubTab;
   private int buildingGridScroll;
   private int buildingCartScroll;
   private String buildingSearchText;
   private GuiTextField buildingSearchField;
   private int buildingQtyEditIndex;
   private GuiTextField buildingQtyField;
   private boolean animationActive;
   private long animationStartTime;
   private float stripOffset;
   private boolean animationDone;
   private int lastTickIndex;
   private int cachedCrystalBalance;
   private static final NumberFormat NUMBER_FORMAT;
   private static final String[] FREE_TRACK;
   private static final String[] PREMIUM_TRACK;
   private static final String[] BUILDING_SUB_TAB_NAMES;
   private static final int[] BUILDING_SUB_TAB_COLORS;

   public RyoShopGui() {
      this.selectedCategory = ShopCategory.CONSUMABLES;
      this.selectedCrateId = null;
      this.crateListScroll = 0;
      this.historyOpen = false;
      this.historyScroll = 0;
      this.lootScrollOffset = 0;
      this.tokenShopMode = false;
      this.crystalShopMode = false;
      this.passMode = false;
      this.passScroll = 0;
      this.redeemMode = false;
      this.killEffectsOpen = false;
      this.lastKnownOverflowCount = -1;
      this.tokenShopScroll = 0;
      this.tokenShopItems = null;
      this.buildingSubTab = 0;
      this.buildingGridScroll = 0;
      this.buildingCartScroll = 0;
      this.buildingSearchText = "";
      this.buildingSearchField = null;
      this.buildingQtyEditIndex = -1;
      this.buildingQtyField = null;
      this.animationActive = false;
      this.animationStartTime = 0L;
      this.stripOffset = 0.0F;
      this.animationDone = false;
      this.lastTickIndex = -1;
      this.cachedCrystalBalance = -1;
   }

   private int getActiveWidth() {
      return !this.crystalShopMode && !this.passMode ? 400 : 480;
   }

   private int getActiveHeight() {
      return !this.crystalShopMode && !this.passMode ? 300 : 340;
   }

   private void recalcGuiPosition() {
      this.guiLeft = (this.width - this.getActiveWidth()) / 2;
      this.guiTop = (this.height - this.getActiveHeight()) / 2;
   }

   public void initGui() {
      this.recalcGuiPosition();
      this.buttonList.clear();
      this.rebuildButtons();
   }

   private void rebuildButtons() {
      this.buttonList.clear();
      this.recalcGuiPosition();
      int activeW = this.getActiveWidth();
      int activeH = this.getActiveHeight();
      int headerY = this.guiTop + 4;
      boolean ryoActive = !this.tokenShopMode && !this.crystalShopMode && !this.passMode && !this.redeemMode;
      int ryoTabTopC = ryoActive ? -9807312 : -12962776;
      int ryoTabBotC = ryoActive ? -11912678 : -14804460;
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(6, this.guiLeft + 6, headerY, 32, 14, "Ryo", ryoTabTopC, ryoTabBotC));
      int tokenTabTopC = this.tokenShopMode ? -10864022 : -12962776;
      int tokenTabBotC = this.tokenShopMode ? -12969398 : -14804460;
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(7, this.guiLeft + 40, headerY, 40, 14, "Token", tokenTabTopC, tokenTabBotC));
      boolean isDedicatedServer = !Minecraft.getMinecraft().isSingleplayer();
      if (isDedicatedServer) {
         int crystalTabTopC = this.crystalShopMode ? -9815446 : -12962776;
         int crystalTabBotC = this.crystalShopMode ? -11920822 : -14804460;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(8, this.guiLeft + 82, headerY, 42, 14, "Crystal", crystalTabTopC, crystalTabBotC));
         int passTabTopC = this.passMode ? -9803238 : -12962776;
         int passTabBotC = this.passMode ? -11908598 : -14804460;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(9, this.guiLeft + 126, headerY, 34, 14, "Pass", passTabTopC, passTabBotC));
         int redeemTabTopC = this.redeemMode ? -9815446 : -12962776;
         int redeemTabBotC = this.redeemMode ? -11920822 : -14804460;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(95, this.guiLeft + 162, headerY, 46, 14, "Redeem", redeemTabTopC, redeemTabBotC));
      }

      boolean hasOverflow = ShopClientData.overflowCount > 0;
      String bankLabel = hasOverflow ? "Bank (" + ShopClientData.overflowCount + ")" : "Bank";
      int bankTopC = hasOverflow ? -9811430 : -14014432;
      int bankBotC = hasOverflow ? -11916790 : -15067632;
      GuiRankedMenu.GuiButtonGradient bankBtn = new GuiRankedMenu.GuiButtonGradient(5, this.guiLeft + 8, this.guiTop + activeH - 22, 70, 18, bankLabel, bankTopC, bankBotC);
      bankBtn.enabled = hasOverflow;
      this.buttonList.add(bankBtn);
      this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(0, this.guiLeft + activeW - 44, headerY + 1, 34, 14, "Back", -9818064, -11920870));
      if (this.passMode) {
         this.rebuildPassButtons();
      } else if (this.crystalShopMode) {
         this.rebuildCrystalButtons();
      } else if (this.redeemMode) {
         if (ShopClientData.akamichiUnlocked) {
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(96, this.guiLeft + 200 - 60, this.guiTop + 160, 120, 20, "Claim 4 Red Chakra Pills", -9815494, -11920870));
         }

      } else {
         ShopCategory[] allCats = ShopCategory.values();
         List<ShopCategory> visibleCats = new ArrayList();

         for(ShopCategory c : allCats) {
            if (c != ShopCategory.CASH) {
               visibleCats.add(c);
            }
         }

         int tabCount = visibleCats.size();
         int totalTabArea = 388;
         int tabSpacing = 2;
         int totalSpacing = (tabCount - 1) * tabSpacing;
         int tabWidth = (totalTabArea - totalSpacing) / tabCount;
         int tabStartX = this.guiLeft + 6;
         int tabY = this.guiTop + 22;

         for(int i = 0; i < tabCount; ++i) {
            ShopCategory cat = (ShopCategory)visibleCats.get(i);
            int topC = cat == this.selectedCategory ? -9807312 : -12962776;
            int botC = cat == this.selectedCategory ? -11912678 : -14804460;
            String tabLabel = cat.displayName;
            if (cat == ShopCategory.WEAPONS && !this.tokenShopMode) {
               tabLabel = "Building";
            }

            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(10 + cat.id, tabStartX + i * (tabWidth + tabSpacing), tabY, tabWidth, 14, tabLabel, topC, botC));
         }

         if (!this.tokenShopMode && this.selectedCategory == ShopCategory.WEAPONS) {
            this.buildBuildingButtons();
         } else if (this.tokenShopMode) {
            if (!this.isSelectedCategoryLocked()) {
               if (this.tokenShopItems == null) {
                  this.tokenShopItems = this.buildTokenShopItems();
               }

               List<TokenShopItem> filtered = this.getFilteredTokenItems();
               int panelY = this.guiTop + 40;
               int panelH = 254;
               int listY = panelY + 20;
               int lineH = 20;
               int maxVisible = (panelH - 28) / lineH;
               int total = filtered.size();

               for(int i = 0; i < Math.min(maxVisible, total - this.tokenShopScroll); ++i) {
                  int idx = i + this.tokenShopScroll;
                  if (idx >= total) {
                     break;
                  }

                  TokenShopItem item = (TokenShopItem)filtered.get(idx);
                  boolean canBuy = ShopClientData.tokenBalance >= item.tokenCost;
                  int btnTopC = canBuy ? -10864022 : -12962776;
                  int btnBotC = canBuy ? -12969398 : -14804460;
                  GuiRankedMenu.GuiButtonGradient buyBtn = new GuiRankedMenu.GuiButtonGradient(50 + i, this.guiLeft + 400 - 50, listY + i * lineH, 38, 16, "Buy", btnTopC, btnBotC);
                  buyBtn.enabled = canBuy;
                  this.buttonList.add(buyBtn);
               }

            }
         } else if (this.selectedCategory == ShopCategory.TRAVEL) {
            int btnW = 150;
            int btnH = 36;
            int hGap = 10;
            int vGap = 8;
            int gridW = 2 * btnW + hGap;
            int gridH = 3 * btnH + 2 * vGap;
            int panelInnerW = 388;
            int panelInnerH = 240;
            int gridStartX = this.guiLeft + 6 + (panelInnerW - gridW) / 2;
            int gridStartY = this.guiTop + 40 + (panelInnerH - gridH) / 2;
            boolean onCooldown = System.currentTimeMillis() < ShopClientData.travelCooldownEnd;
            boolean canAffordTravel = ShopClientData.ryoBalance >= 500L;

            for(int i = 0; i < 6; ++i) {
               int col = i % 2;
               int row = i / 2;
               int bx = gridStartX + col * (btnW + hGap);
               int by = gridStartY + row * (btnH + vGap);
               String label;
               if (onCooldown) {
                  long remainMs = ShopClientData.travelCooldownEnd - System.currentTimeMillis();
                  int remainSec = Math.max(0, (int)(remainMs / 1000L));
                  label = "Cooldown: " + remainSec / 60 + ":" + String.format("%02d", remainSec % 60);
               } else {
                  label = "¥500 Ryo";
               }

               int[] vc = TRAVEL_VILLAGE_COLORS[i];
               GuiRankedMenu.GuiButtonGradient btn = new GuiRankedMenu.GuiButtonGradient(60 + i, bx, by, btnW, btnH, label, vc[0], vc[1]);
               btn.enabled = !onCooldown && canAffordTravel;
               this.buttonList.add(btn);
            }

         } else {
            List<ShopClientData.CrateClientInfo> crates = ShopClientData.getCratesByCategory(this.selectedCategory);
            int listX = this.guiLeft + 8;
            int listY = this.guiTop + 42;
            int entryH = 18;
            int maxVisible = 10;

            for(int i = 0; i < Math.min(maxVisible, crates.size() - this.crateListScroll); ++i) {
               int idx = i + this.crateListScroll;
               if (idx >= crates.size()) {
                  break;
               }

               ShopClientData.CrateClientInfo crate = (ShopClientData.CrateClientInfo)crates.get(idx);
               boolean sel = crate.crateId.equals(this.selectedCrateId);
               int topC = sel ? -10859990 : -14014432;
               int botC = sel ? -12963824 : -15067120;
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(20 + i, listX, listY + i * (entryH + 2), 172, entryH, "", topC, botC));
            }

            ShopClientData.CrateClientInfo selCrate = this.selectedCrateId != null ? ShopClientData.getCrateById(this.selectedCrateId) : null;
            if (this.historyOpen) {
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(3, this.guiLeft + 400 - 108, this.guiTop + 300 - 62, 100, 18, "Hide History", -10859984, -12965352));
            } else {
               boolean isCash = selCrate != null && selCrate.usesCrystals();
               boolean isFreeRoll = isCash && selCrate != null && ("cash_clan".equals(selCrate.crateId) && ShopClientData.freeRollAvailable || "cash_so6p".equals(selCrate.crateId) && ShopClientData.weekendPromoSO6PAvailable);
               boolean canAfford;
               if (isFreeRoll) {
                  canAfford = true;
               } else if (isCash) {
                  canAfford = selCrate != null && ShopClientData.crystalBalance >= selCrate.crystalPrice;
               } else {
                  canAfford = selCrate != null && ShopClientData.ryoBalance >= selCrate.price;
               }

               int openTopC = canAfford ? (isFreeRoll ? -9803238 : (isCash ? -9815446 : -9807312)) : -12962776;
               int openBotC = canAfford ? (isFreeRoll ? -11908598 : (isCash ? -11920822 : -11912678)) : -14804460;
               String openLabel = isFreeRoll ? "Free Roll!" : (selCrate != null && selCrate.directPurchase ? "Buy" : "Open Crate");
               GuiRankedMenu.GuiButtonGradient openBtn = new GuiRankedMenu.GuiButtonGradient(2, this.guiLeft + 400 - 108, this.guiTop + 300 - 62, 100, 18, openLabel, openTopC, openBotC);
               openBtn.enabled = canAfford;
               this.buttonList.add(openBtn);
               if (selCrate == null || !selCrate.directPurchase) {
                  boolean canAffordMulti;
                  String multiLabel;
                  if (isCash) {
                     int multiCrystalPrice = selCrate != null ? selCrate.crystalPrice * 10 : 0;
                     canAffordMulti = selCrate != null && ShopClientData.crystalBalance >= multiCrystalPrice;
                     multiLabel = selCrate != null ? "10-Roll (" + NUMBER_FORMAT.format((long)multiCrystalPrice) + " CC)" : "10-Roll";
                  } else {
                     long multiPrice = selCrate != null ? selCrate.price * 9L : 0L;
                     canAffordMulti = selCrate != null && ShopClientData.ryoBalance >= multiPrice;
                     multiLabel = selCrate != null ? "10-Roll (¥" + NUMBER_FORMAT.format(multiPrice) + ")" : "10-Roll";
                  }

                  int multiTopC = canAffordMulti ? -10851792 : -12962776;
                  int multiBotC = canAffordMulti ? -12957158 : -14804460;
                  GuiRankedMenu.GuiButtonGradient multiBtn = new GuiRankedMenu.GuiButtonGradient(4, this.guiLeft + 400 - 108, this.guiTop + 300 - 42, 100, 18, multiLabel, multiTopC, multiBotC);
                  multiBtn.enabled = canAffordMulti;
                  this.buttonList.add(multiBtn);
               }

               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(3, this.guiLeft + 8, this.guiTop + 300 - 42, 70, 18, "History", -12962776, -14804460));
            }

         }
      }
   }

   private void rebuildPassButtons() {
      for(int i = this.buttonList.size() - 1; i >= 0; --i) {
         if (((GuiButton)this.buttonList.get(i)).id == 5) {
            this.buttonList.remove(i);
            break;
         }
      }

      int activeW = this.getActiveWidth();
      int activeH = this.getActiveHeight();
      int actionBarY = this.guiTop + activeH - 28;
      int btnH = 16;
      int pad = 6;
      int contentLeft = this.guiLeft + pad;
      int contentRight = this.guiLeft + activeW - pad;
      if (ShopClientData.bpPurchasedTier == 0) {
         int buyY = this.guiTop + 46;
         int btnW = 140;
         int gap = 10;
         int startX = this.guiLeft + (activeW - btnW * 2 - gap) / 2;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(70, startX, buyY, btnW, 18, "Kage Pass - 300 CC", -9815446, -11920822));
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(71, startX + btnW + gap, buyY, btnW, 18, "Otsutsuki - 500 CC", -9803238, -11908598));
      }

      if (ShopClientData.bpPurchasedTier > 0) {
         int btnW = 86;
         boolean canClaim = false;

         for(int t = 1; t <= ShopClientData.bpCurrentTier; ++t) {
            if (!ShopClientData.bpClaimedTiers.contains(t)) {
               canClaim = true;
               break;
            }
         }

         int claimTopC = canClaim ? -11900374 : -12962776;
         int claimBotC = canClaim ? -14005744 : -14804460;
         GuiRankedMenu.GuiButtonGradient claimBtn = new GuiRankedMenu.GuiButtonGradient(72, contentLeft, actionBarY, btnW, btnH, "Claim", claimTopC, claimBotC);
         claimBtn.enabled = canClaim;
         this.buttonList.add(claimBtn);
         int skipCost = getSkipCost(ShopClientData.bpCurrentTier + 1);
         boolean canSkip = ShopClientData.bpCurrentTier < 20 && ShopClientData.crystalBalance >= skipCost;
         int skipTopC = canSkip ? -9815446 : -12962776;
         int skipBotC = canSkip ? -11920822 : -14804460;
         int skipW = 100;
         int skipX = this.guiLeft + (activeW - skipW) / 2;
         GuiRankedMenu.GuiButtonGradient skipBtn = new GuiRankedMenu.GuiButtonGradient(73, skipX, actionBarY, skipW, btnH, "Skip - " + skipCost + " CC", skipTopC, skipBotC);
         skipBtn.enabled = canSkip;
         this.buttonList.add(skipBtn);
         boolean hasOverflow = ShopClientData.overflowCount > 0;
         String bankLabel = hasOverflow ? "Bank (" + ShopClientData.overflowCount + ")" : "Bank";
         int bankTopC = hasOverflow ? -9811430 : -14014432;
         int bankBotC = hasOverflow ? -11916790 : -15067632;
         GuiRankedMenu.GuiButtonGradient bankBtn = new GuiRankedMenu.GuiButtonGradient(5, contentRight - btnW, actionBarY, btnW, btnH, bankLabel, bankTopC, bankBotC);
         bankBtn.enabled = hasOverflow;
         this.buttonList.add(bankBtn);
         int keW = 86;
         int keX = contentRight - btnW - keW - 6;
         boolean hasEffects = !ShopClientData.bpOwnedKillEffects.isEmpty();
         String keLabel = hasEffects ? "Kill Effects" : "Kill Effects";
         int keTopC = hasEffects ? -9811430 : -12962776;
         int keBotC = hasEffects ? -11916790 : -14804460;
         GuiRankedMenu.GuiButtonGradient keBtn = new GuiRankedMenu.GuiButtonGradient(90, keX, actionBarY, keW, btnH, keLabel, keTopC, keBotC);
         this.buttonList.add(keBtn);
         if (this.killEffectsOpen) {
            int panelTopY = this.guiTop + 50;
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(91, contentRight - 40, panelTopY + 2, 34, 14, "Back", -9818064, -11920870));
            if (hasEffects) {
               int effectBtnW = activeW - pad * 2 - 16;
               int effectBtnH = 16;
               int effectSpacing = 30;
               int effectY = panelTopY + 22;
               int effectIdx = 0;

               for(String effectId : ShopClientData.bpOwnedKillEffects) {
                  boolean isActive = effectId.equals(ShopClientData.bpActiveKillEffect);
                  String label = formatEffectName(effectId);
                  if (isActive) {
                     label = "§a✔ ACTIVE: " + label;
                  }

                  int topC = isActive ? -14001622 : -12963792;
                  int botC = isActive ? -15058406 : -14018022;
                  int ey = effectY + effectIdx * effectSpacing;
                  this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(80 + effectIdx, contentLeft + 8, ey, effectBtnW, effectBtnH, label, topC, botC));
                  ++effectIdx;
                  if (effectIdx >= 10) {
                     break;
                  }
               }
            }
         }
      } else {
         int btnW = 86;
         boolean hasOverflow = ShopClientData.overflowCount > 0;
         String bankLabel = hasOverflow ? "Bank (" + ShopClientData.overflowCount + ")" : "Bank";
         int bankTopC = hasOverflow ? -9811430 : -14014432;
         int bankBotC = hasOverflow ? -11916790 : -15067632;
         GuiRankedMenu.GuiButtonGradient bankBtn = new GuiRankedMenu.GuiButtonGradient(5, contentRight - btnW, actionBarY, btnW, btnH, bankLabel, bankTopC, bankBotC);
         bankBtn.enabled = hasOverflow;
         this.buttonList.add(bankBtn);
      }

   }

   private void rebuildCrystalButtons() {
      List<ShopClientData.CrateClientInfo> crates = ShopClientData.getCratesByCategory(ShopCategory.CASH);
      int activeW = this.getActiveWidth();
      int activeH = this.getActiveHeight();
      int pad = 8;
      int cardH = 26;
      int cardGap = 4;
      int listX = this.guiLeft + pad;
      int listY = this.guiTop + 42;
      int cardW = 180;
      int maxVisible = 7;

      for(int i = 0; i < Math.min(maxVisible, crates.size() - this.crateListScroll); ++i) {
         int idx = i + this.crateListScroll;
         if (idx >= crates.size()) {
            break;
         }

         ShopClientData.CrateClientInfo crate = (ShopClientData.CrateClientInfo)crates.get(idx);
         boolean sel = crate.crateId.equals(this.selectedCrateId);
         int topC = sel ? -10868134 : -14016464;
         int botC = sel ? -12969414 : -15068128;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(20 + i, listX, listY + i * (cardH + cardGap), cardW, cardH, "", topC, botC));
      }

      ShopClientData.CrateClientInfo selCrate = this.selectedCrateId != null ? ShopClientData.getCrateById(this.selectedCrateId) : null;
      int actionX = this.guiLeft + activeW - 112;
      int actionBtnW = 104;
      if (this.historyOpen) {
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(3, actionX, this.guiTop + activeH - 46, actionBtnW, 18, "Hide History", -10864038, -12969414));
      } else {
         boolean isFreeRoll = selCrate != null && selCrate.usesCrystals() && ("cash_clan".equals(selCrate.crateId) && ShopClientData.freeRollAvailable || "cash_so6p".equals(selCrate.crateId) && ShopClientData.weekendPromoSO6PAvailable);
         boolean canAfford;
         if (isFreeRoll) {
            canAfford = true;
         } else {
            canAfford = selCrate != null && ShopClientData.crystalBalance >= selCrate.crystalPrice;
         }

         String openLabel = isFreeRoll ? "Free Roll!" : (selCrate != null && selCrate.directPurchase ? "Buy" : "Open Crate");
         int openTopC = canAfford ? (isFreeRoll ? -9803238 : -9819542) : -12962776;
         int openBotC = canAfford ? (isFreeRoll ? -11908598 : -11920822) : -14804460;
         GuiRankedMenu.GuiButtonGradient openBtn = new GuiRankedMenu.GuiButtonGradient(2, actionX, this.guiTop + activeH - 46, actionBtnW, 18, openLabel, openTopC, openBotC);
         openBtn.enabled = canAfford;
         this.buttonList.add(openBtn);
         if (selCrate == null || !selCrate.directPurchase) {
            int multiCrystalPrice = selCrate != null ? selCrate.crystalPrice * 10 : 0;
            boolean canAffordMulti = selCrate != null && ShopClientData.crystalBalance >= multiCrystalPrice;
            String multiLabel = selCrate != null ? "10-Roll (" + NUMBER_FORMAT.format((long)multiCrystalPrice) + " CC)" : "10-Roll";
            int multiTopC = canAffordMulti ? -10864022 : -12962776;
            int multiBotC = canAffordMulti ? -12969398 : -14804460;
            GuiRankedMenu.GuiButtonGradient multiBtn = new GuiRankedMenu.GuiButtonGradient(4, actionX, this.guiTop + activeH - 24, actionBtnW, 18, multiLabel, multiTopC, multiBotC);
            multiBtn.enabled = canAffordMulti;
            this.buttonList.add(multiBtn);
         }

         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(3, this.guiLeft + 8, this.guiTop + activeH - 24, 70, 18, "History", -12965318, -14805984));
      }

   }

   private static int getSkipCost(int tier) {
      if (tier <= 7) {
         return 10;
      } else {
         return tier <= 14 ? 15 : 25;
      }
   }

   private static int getXPForTier(int tier) {
      if (tier <= 5) {
         return 500;
      } else if (tier <= 10) {
         return 800;
      } else {
         return tier <= 15 ? 1200 : 2500;
      }
   }

   private static String getKillEffectDescription(String effectId) {
      if (effectId == null) {
         return "";
      } else {
         String name = effectId;
         if (effectId.length() > 3 && effectId.charAt(0) == 's' && effectId.charAt(2) == '_') {
            name = effectId.substring(3);
         }

         switch (name) {
            case "fire_burst":
               return "Flames erupt around your victim on PvP kill";
            case "lightning_strike":
               return "Lightning crashes down on your victim on PvP kill";
            case "shadow_vortex":
               return "A dark vortex swirls around your victim on PvP kill";
            case "cherry_blossom":
               return "Cherry blossom petals scatter from your victim on PvP kill";
            case "ice_shatter":
               return "Ice crystals shatter outward from your victim on PvP kill";
            case "sand_burial":
               return "Sand engulfs your victim on PvP kill";
            case "wind_slash":
               return "Wind blades slash through your victim on PvP kill";
            case "water_prison":
               return "A water sphere bursts around your victim on PvP kill";
            default:
               return "Visual effect on PvP kill";
         }
      }
   }

   private static String formatEffectName(String effectId) {
      if (effectId == null) {
         return "Unknown";
      } else {
         String name = effectId;
         if (effectId.length() > 3 && effectId.charAt(0) == 's' && effectId.charAt(2) == '_') {
            name = effectId.substring(3);
         }

         StringBuilder sb = new StringBuilder();
         boolean cap = true;

         for(int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (c == '_') {
               sb.append(' ');
               cap = true;
            } else {
               sb.append(cap ? Character.toUpperCase(c) : c);
               cap = false;
            }
         }

         return sb.toString();
      }
   }

   private static String getOwnedEffectByIndex(int index) {
      int i = 0;

      for(String effectId : ShopClientData.bpOwnedKillEffects) {
         if (i == index) {
            return effectId;
         }

         ++i;
      }

      return null;
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      if (ShopClientData.overflowCount != this.lastKnownOverflowCount) {
         this.lastKnownOverflowCount = ShopClientData.overflowCount;
         this.rebuildButtons();
      }

      this.drawDefaultBackground();
      int activeW = this.getActiveWidth();
      int activeH = this.getActiveHeight();
      drawRect(this.guiLeft - 3, this.guiTop - 3, this.guiLeft + activeW + 3, this.guiTop + activeH + 3, -7706054);
      drawRect(this.guiLeft - 1, this.guiTop - 1, this.guiLeft + activeW + 1, this.guiTop + activeH + 1, -9809872);
      this.drawGradientRect(this.guiLeft, this.guiTop, this.guiLeft + activeW, this.guiTop + activeH, -298833888, -300280816);
      this.drawHollowRect(this.guiLeft + 3, this.guiTop + 3, activeW - 6, activeH - 6, -9809872);
      String balanceStr = "¥" + NUMBER_FORMAT.format(ShopClientData.ryoBalance);
      String tokenStr = NUMBER_FORMAT.format((long)ShopClientData.tokenBalance) + " tokens";
      int displayCrystalBalance = this.cachedCrystalBalance >= 0 ? this.cachedCrystalBalance : ShopClientData.crystalBalance;
      String balanceLabel;
      if (this.passMode) {
         String crystalStr = NUMBER_FORMAT.format((long)displayCrystalBalance);
         balanceLabel = "§7§dTier " + ShopClientData.bpCurrentTier + "/20 §8| §d" + crystalStr + " CC";
      } else if (!this.crystalShopMode && this.selectedCategory != ShopCategory.CASH) {
         balanceLabel = "§7§e" + balanceStr + " §8| §d" + tokenStr;
      } else {
         String crystalStr = NUMBER_FORMAT.format((long)displayCrystalBalance);
         balanceLabel = "§7§d" + crystalStr + " Crystals";
         if (ShopClientData.firstPurchaseBonusAvailable) {
            balanceLabel = balanceLabel + " §8| §e2x CC First Purchase!";
         }
      }

      if (ShopClientData.overflowCount > 0) {
         balanceLabel = balanceLabel + " §8| §c§l" + ShopClientData.overflowCount + " banked";
      }

      int balanceW = this.fontRenderer.getStringWidth(balanceLabel);
      int rightEdge = ShopClientData.overflowCount > 0 ? this.guiLeft + activeW - 102 : this.guiLeft + activeW - 46;
      int balanceX = rightEdge - balanceW - 4;
      this.fontRenderer.drawStringWithShadow(balanceLabel, (float)balanceX, (float)(this.guiTop + 7), -2832216);
      this.drawHorizontalLine(this.guiLeft + 6, this.guiLeft + activeW - 6, this.guiTop + 19, -9807296);
      if (this.redeemMode) {
         this.drawRedeemTab(mouseX, mouseY);
      } else if (this.passMode) {
         this.drawPassTab(mouseX, mouseY);
      } else if (this.tokenShopMode) {
         this.drawTokenShop(mouseX, mouseY);
      } else if (this.crystalShopMode) {
         this.drawCrystalShop(mouseX, mouseY);
      } else {
         this.drawHorizontalLine(this.guiLeft + 6, this.guiLeft + 400 - 6, this.guiTop + 38, -9807296);
         if (this.selectedCategory == ShopCategory.WEAPONS) {
            this.drawBuildingShop(mouseX, mouseY);
         } else if (this.selectedCategory == ShopCategory.TRAVEL) {
            this.drawTravelPanel(mouseX, mouseY);
         } else {
            this.drawCrateListPanel(mouseX, mouseY);
            this.drawCrateDetailPanel(mouseX, mouseY);
            if (this.historyOpen) {
               this.drawHistoryPanel(mouseX, mouseY);
            }
         }
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
      if (!this.tokenShopMode && !this.passMode && !this.crystalShopMode && !this.redeemMode && this.selectedCategory != ShopCategory.TRAVEL) {
         this.drawCrateListText();
      }

      if (this.crystalShopMode) {
         this.drawCrystalCardText();
      }

      if (!this.tokenShopMode && !this.crystalShopMode && !this.passMode && this.selectedCategory == ShopCategory.TRAVEL) {
         this.drawTravelButtonLabels();
      }

      if (ShopClientData.pendingResult != null && !ShopClientData.pendingResult.consumed) {
         if (!this.animationActive) {
            this.animationActive = true;
            this.animationStartTime = System.currentTimeMillis();
            this.animationDone = false;
            this.lastTickIndex = -1;
            this.cachedCrystalBalance = ShopClientData.crystalBalance;
         }

         this.drawRollingAnimation(mouseX, mouseY);
      } else {
         this.cachedCrystalBalance = -1;
      }

   }

   private boolean isSelectedCategoryLocked() {
      if (this.selectedCategory == ShopCategory.CLANS && !ShopClientData.clanTabUnlocked) {
         return true;
      } else {
         return this.selectedCategory == ShopCategory.KEKKEI_GENKAI && !ShopClientData.so6pTabUnlocked;
      }
   }

   private void drawTravelPanel(int mouseX, int mouseY) {
      int panelX = this.guiLeft + 6;
      int panelY = this.guiTop + 40;
      int panelW = 388;
      int panelH = 254;
      this.drawGradientRect(panelX, panelY, panelX + panelW, panelY + panelH, -868600792, -870442476);
      this.drawHollowRect(panelX, panelY, panelW, panelH, -9809872);
      String title = "§lVillage Fast Travel";
      int titleW = this.fontRenderer.getStringWidth(title);
      this.fontRenderer.drawStringWithShadow(title, (float)(panelX + (panelW - titleW) / 2), (float)(panelY + 4), -1521552);
      String subtitle = "Warp to any village for ¥500 ryo (5 min cooldown, 15s PvP immunity)";
      int subW = this.fontRenderer.getStringWidth(subtitle);
      this.fontRenderer.drawString(subtitle, panelX + (panelW - subW) / 2, panelY + panelH - 14, -7700886);
   }

   private void drawTravelButtonLabels() {
      int btnW = 150;
      int btnH = 36;
      int hGap = 10;
      int vGap = 8;
      int gridW = 2 * btnW + hGap;
      int gridH = 3 * btnH + 2 * vGap;
      int panelInnerW = 388;
      int panelInnerH = 240;
      int gridStartX = this.guiLeft + 6 + (panelInnerW - gridW) / 2;
      int gridStartY = this.guiTop + 40 + (panelInnerH - gridH) / 2;

      for(int i = 0; i < 6; ++i) {
         int col = i % 2;
         int row = i / 2;
         int bx = gridStartX + col * (btnW + hGap);
         int by = gridStartY + row * (btnH + vGap);
         String name = TRAVEL_VILLAGE_NAMES[i];
         int nameW = this.fontRenderer.getStringWidth(name);
         this.fontRenderer.drawStringWithShadow(name, (float)(bx + (btnW - nameW) / 2), (float)(by + 5), -1);
      }

   }

   private void drawCrateListPanel(int mouseX, int mouseY) {
      int panelX = this.guiLeft + 6;
      int panelY = this.guiTop + 40;
      int panelW = 178;
      int panelH = this.historyOpen ? 200 : 240;
      this.drawGradientRect(panelX, panelY, panelX + panelW, panelY + panelH, -868600792, -870442476);
      this.drawHollowRect(panelX, panelY, panelW, panelH, -9809872);
      this.fontRenderer.drawStringWithShadow("§l" + this.selectedCategory.displayName + " Crates", (float)(panelX + 4), (float)(panelY + 3), -1521552);
      if (this.isSelectedCategoryLocked()) {
         String lockMsg;
         int lockColor;
         if (this.selectedCategory == ShopCategory.KEKKEI_GENKAI) {
            lockMsg = "Complete Your Shinobi Way!";
            lockColor = -22016;
         } else {
            lockMsg = "Complete the Konoha Crush!";
            lockColor = -22016;
         }

         int msgW = this.fontRenderer.getStringWidth(lockMsg);
         int msgX = panelX + (panelW - msgW) / 2;
         int msgY = panelY + panelH / 2 - 4;
         this.fontRenderer.drawStringWithShadow(lockMsg, (float)msgX, (float)msgY, lockColor);
      } else {
         List<ShopClientData.CrateClientInfo> crates = ShopClientData.getCratesByCategory(this.selectedCategory);
         if (crates.isEmpty()) {
            this.fontRenderer.drawString("§e§lComing Soon!", panelX + 10, panelY + 20, -7700886);
         }

         if (this.selectedCrateId != null) {
            ShopClientData.CrateClientInfo selCrate = ShopClientData.getCrateById(this.selectedCrateId);
            if (selCrate != null) {
               int entryH = 18;
               int maxVisible = 10;
               int visibleCount = Math.min(maxVisible, crates.size());
               int pityY = panelY + 14 + visibleCount * (entryH + 2) + 4;
               this.drawPityDisplay(panelX + 4, pityY, panelW - 8, selCrate.crateId);
            }
         }

      }
   }

   private void drawCrateListText() {
      if (!this.isSelectedCategoryLocked()) {
         List<ShopClientData.CrateClientInfo> crates = ShopClientData.getCratesByCategory(this.selectedCategory);
         int listX = this.guiLeft + 8;
         int listY = this.guiTop + 42;
         int entryH = 18;
         int maxVisible = 10;

         for(int i = 0; i < Math.min(maxVisible, crates.size() - this.crateListScroll); ++i) {
            int idx = i + this.crateListScroll;
            if (idx >= crates.size()) {
               break;
            }

            ShopClientData.CrateClientInfo crate = (ShopClientData.CrateClientInfo)crates.get(idx);
            boolean sel = crate.crateId.equals(this.selectedCrateId);
            int textY = listY + i * (entryH + 2) + 5;
            int nameColor = sel ? -1521552 : -2832216;
            String name = this.fontRenderer.trimStringToWidth(crate.displayName, 110);
            this.fontRenderer.drawStringWithShadow(name, (float)(listX + 4), (float)textY, nameColor);
            boolean showFree = crate.usesCrystals() && ("cash_clan".equals(crate.crateId) && ShopClientData.freeRollAvailable || "cash_so6p".equals(crate.crateId) && ShopClientData.weekendPromoSO6PAvailable);
            String price;
            int priceColor;
            if (showFree) {
               price = "FREE!";
               long pulse = System.currentTimeMillis() % 1000L;
               int glow = (int)((double)155.0F + (double)100.0F * Math.sin((double)pulse * Math.PI * (double)2.0F / (double)1000.0F));
               priceColor = -16777216 | glow << 16 | glow << 8 | 0;
            } else if (crate.usesCrystals()) {
               price = NUMBER_FORMAT.format((long)crate.crystalPrice) + " CC";
               priceColor = ShopClientData.crystalBalance >= crate.crystalPrice ? -2258689 : -43691;
            } else {
               price = "¥" + NUMBER_FORMAT.format(crate.price);
               priceColor = ShopClientData.ryoBalance >= crate.price ? -11141291 : -43691;
            }

            int priceW = this.fontRenderer.getStringWidth(price);
            this.fontRenderer.drawStringWithShadow(price, (float)(listX + 168 - priceW), (float)textY, priceColor);
         }

      }
   }

   private void drawCrateDetailPanel(int mouseX, int mouseY) {
      int panelX = this.guiLeft + 188;
      int panelY = this.guiTop + 40;
      int panelW = 206;
      int panelH = this.historyOpen ? 200 : 240;
      this.drawGradientRect(panelX, panelY, panelX + panelW, panelY + panelH, -868600792, -870442476);
      this.drawHollowRect(panelX, panelY, panelW, panelH, -9809872);
      if (this.isSelectedCategoryLocked()) {
         String lockMsg = this.selectedCategory == ShopCategory.KEKKEI_GENKAI ? "Complete Your Shinobi Way!" : "Complete the Konoha Crush!";
         int lockColor = -22016;
         int msgW = this.fontRenderer.getStringWidth(lockMsg);
         this.fontRenderer.drawStringWithShadow(lockMsg, (float)(panelX + (panelW - msgW) / 2), (float)(panelY + panelH / 2 - 4), lockColor);
      } else {
         ShopClientData.CrateClientInfo selCrate = this.selectedCrateId != null ? ShopClientData.getCrateById(this.selectedCrateId) : null;
         if (selCrate == null) {
            this.fontRenderer.drawString("Select a crate", panelX + 10, panelY + 20, -7700886);
         } else {
            this.fontRenderer.drawStringWithShadow("§l" + selCrate.displayName, (float)(panelX + 4), (float)(panelY + 4), -1521552);
            int ryoDescEndY = panelY + 16;
            if (selCrate.description != null && !selCrate.description.isEmpty()) {
               List<String> descLines = this.fontRenderer.listFormattedStringToWidth(selCrate.description, panelW - 10);

               for(int dl = 0; dl < Math.min(2, descLines.size()); ++dl) {
                  this.fontRenderer.drawString((String)descLines.get(dl), panelX + 4, panelY + 16 + dl * 10, -7700886);
               }

               ryoDescEndY = panelY + 16 + Math.min(2, descLines.size()) * 10;
            }

            int ryoPriceY = ryoDescEndY + 2;
            boolean afford;
            String priceLabel;
            if (selCrate.usesCrystals()) {
               priceLabel = "Price: " + NUMBER_FORMAT.format((long)selCrate.crystalPrice) + " Crystals";
               afford = ShopClientData.crystalBalance >= selCrate.crystalPrice;
            } else {
               priceLabel = "Price: ¥" + NUMBER_FORMAT.format(selCrate.price);
               afford = ShopClientData.ryoBalance >= selCrate.price;
            }

            this.fontRenderer.drawStringWithShadow(priceLabel, (float)(panelX + 4), (float)ryoPriceY, afford ? (selCrate.usesCrystals() ? -2258689 : -11141291) : -43691);
            if (selCrate.usesCrystals() && !selCrate.directPurchase) {
               int pityCount = ShopClientData.getPityCount(selCrate.crateId);
               String pityLabel = "Rolls: " + pityCount;
               int pityW = this.fontRenderer.getStringWidth(pityLabel);
               this.fontRenderer.drawString(pityLabel, panelX + panelW - pityW - 4, ryoPriceY, -7700886);
            }

            this.drawHorizontalLine(panelX + 4, panelX + panelW - 4, ryoPriceY + 10, -9807296);
            this.fontRenderer.drawStringWithShadow("Loot Table:", (float)(panelX + 4), (float)(ryoPriceY + 14), -1521552);
            List<ShopClientData.LootEntryClientInfo> sortedEntries = new ArrayList(selCrate.lootEntries);
            Collections.sort(sortedEntries, new Comparator<ShopClientData.LootEntryClientInfo>() {
               public int compare(ShopClientData.LootEntryClientInfo a, ShopClientData.LootEntryClientInfo b) {
                  return Integer.compare(b.rarityOrdinal, a.rarityOrdinal);
               }
            });
            Minecraft mc = Minecraft.getMinecraft();
            int lootY = ryoPriceY + 26;
            int lineH = 18;
            int maxLootVisible = (panelH - 100) / lineH;
            int totalEntries = sortedEntries.size();
            int maxScrollOffset = Math.max(0, totalEntries - maxLootVisible);
            if (this.lootScrollOffset > maxScrollOffset) {
               this.lootScrollOffset = maxScrollOffset;
            }

            if (this.lootScrollOffset < 0) {
               this.lootScrollOffset = 0;
            }

            if (this.lootScrollOffset > 0) {
               String upArrow = "▲";
               int arrowW = this.fontRenderer.getStringWidth(upArrow);
               this.fontRenderer.drawString(upArrow, panelX + panelW - arrowW - 6, lootY - 10, -7700886);
            }

            for(int i = 0; i < Math.min(maxLootVisible, totalEntries - this.lootScrollOffset); ++i) {
               int idx = i + this.lootScrollOffset;
               if (idx >= totalEntries) {
                  break;
               }

               ShopClientData.LootEntryClientInfo entry = (ShopClientData.LootEntryClientInfo)sortedEntries.get(idx);
               int rarityColor = getRarityColor(entry.rarityOrdinal);
               int entryY = lootY + i * lineH;
               int iconX = panelX + 4;
               ItemStack stack = resolveItemStack(entry.itemId, entry.itemMeta, entry.itemCount, entry.itemNbt);
               if (!stack.isEmpty()) {
                  GlStateManager.enableDepth();
                  RenderHelper.enableGUIStandardItemLighting();
                  mc.getRenderItem().renderItemIntoGUI(stack, iconX, entryY);
                  RenderHelper.disableStandardItemLighting();
                  GlStateManager.disableDepth();
               }

               String itemName = this.getEntryDisplayName(entry);
               String entryText = this.fontRenderer.trimStringToWidth(itemName, panelW - 70);
               this.fontRenderer.drawString(entryText, iconX + 20, entryY + 4, rarityColor);
               String rarityName = ItemRarity.fromOrdinal(entry.rarityOrdinal).displayName;
               int nameW = this.fontRenderer.getStringWidth(rarityName);
               this.fontRenderer.drawString(rarityName, panelX + panelW - nameW - 6, entryY + 4, rarityColor);
            }

            if (this.lootScrollOffset + maxLootVisible < totalEntries) {
               String downArrow = "▼";
               int arrowW = this.fontRenderer.getStringWidth(downArrow);
               int indicatorY = lootY + maxLootVisible * lineH;
               this.fontRenderer.drawString(downArrow, panelX + panelW - arrowW - 6, indicatorY, -7700886);
            }

         }
      }
   }

   private String getEntryDisplayName(ShopClientData.LootEntryClientInfo entry) {
      String name;
      if (entry.hasDisplayNameOverride()) {
         name = entry.displayNameOverride;
         if (entry.hasBundleItems()) {
            name = name + " §7(" + entry.getBundleSize() + " items)";
         }
      } else {
         name = getItemDisplayName(entry.itemId);
         if (entry.itemNbt != null) {
            String protSuffix = getProtectionSuffix(entry.itemNbt);
            if (!protSuffix.isEmpty()) {
               name = name + " " + protSuffix;
            }
         }
      }

      return name;
   }

   private static String getProtectionSuffix(NBTTagCompound nbt) {
      if (nbt != null && nbt.hasKey("ench")) {
         NBTTagList enchList = nbt.getTagList("ench", 10);

         for(int i = 0; i < enchList.tagCount(); ++i) {
            NBTTagCompound ench = enchList.getCompoundTagAt(i);
            if (ench.getShort("id") == 0) {
               int level = ench.getShort("lvl");
               String roman;
               switch (level) {
                  case 1:
                     roman = "I";
                     break;
                  case 2:
                     roman = "II";
                     break;
                  case 3:
                     roman = "III";
                     break;
                  case 4:
                     roman = "IV";
                     break;
                  case 5:
                     roman = "V";
                     break;
                  default:
                     roman = String.valueOf(level);
               }

               return "(Prot " + roman + ")";
            }
         }

         return "";
      } else {
         return "";
      }
   }

   private void drawPityDisplay(int x, int y, int maxW, String crateId) {
      if (ShopClientData.pendingResult == null || ShopClientData.pendingResult.consumed) {
         ShopClientData.CrateClientInfo pitySelCrate = this.selectedCrateId != null ? ShopClientData.getCrateById(this.selectedCrateId) : null;
         boolean isCash = pitySelCrate != null && pitySelCrate.usesCrystals();
         int sPity = ShopClientData.getPityCount(crateId);
         if (isCash) {
            if (!pitySelCrate.directPurchase) {
               String pityLine = "§dPity: §f" + sPity;
               if (sPity >= 15) {
                  pityLine = pityLine + " §e(ACTIVE!)";
               } else if (sPity >= 10) {
                  pityLine = pityLine + " §e(close!)";
               } else if (sPity >= 6) {
                  pityLine = pityLine + " §7(building)";
               }

               this.fontRenderer.drawString(pityLine, x, y, -2258689);
               y += 9;
               this.fontRenderer.drawString("§8Pity removes C-rank, boosts A+ odds", x, y, -7700886);
               y += 10;
            }

            this.fontRenderer.drawString("§6Dupes: §aC§810 §9B§820 §5A§830 §6S§840 §cS+§850%", x, y, -7700886);
            y += 8;
            this.fontRenderer.drawString("§8Chakra Crystal refund on dupes", x, y, -7700886);
         } else {
            int sHardPity = getHardPityS(crateId);
            if (sHardPity <= 0) {
               return;
            }

            this.fontRenderer.drawString("Pity Progress:", x, y, -1521552);
            y += 10;
            String sLine = "S-Rank: " + sPity + "/" + sHardPity;
            int sColor = (double)sPity >= (double)sHardPity * 0.7 ? -22016 : -7700886;
            this.fontRenderer.drawString(sLine, x + 2, y, sColor);
         }

      }
   }

   private static int extractTier(String crateId) {
      if (crateId != null && crateId.length() >= 2) {
         char last = crateId.charAt(crateId.length() - 1);
         return last >= '1' && last <= '5' ? last - 48 : 0;
      } else {
         return 0;
      }
   }

   private static int getHardPityS(String crateId) {
      if (crateId == null) {
         return 0;
      } else {
         int tier = extractTier(crateId);
         switch (tier) {
            case 3:
               return 50;
            case 4:
               return 35;
            case 5:
               return 20;
            default:
               return 0;
         }
      }
   }

   private void drawHistoryPanel(int mouseX, int mouseY) {
      int panelX = this.guiLeft + 6;
      int panelY = this.guiTop + this.getActiveHeight() - 82;
      int panelW = this.getActiveWidth() - 12;
      int panelH = 70;
      this.drawGradientRect(panelX, panelY, panelX + panelW, panelY + panelH, -868600792, -870442476);
      this.drawHollowRect(panelX, panelY, panelW, panelH, -9809872);
      this.fontRenderer.drawStringWithShadow("§lRecent Purchases:", (float)(panelX + 4), (float)(panelY + 3), -1521552);
      List<ShopClientData.HistoryEntryInfo> hist = ShopClientData.history;
      if (hist.isEmpty()) {
         this.fontRenderer.drawString("No purchase history", panelX + 10, panelY + 16, -7700886);
      } else {
         int lineH = 10;
         int maxLines = 4;
         int startIdx = Math.max(0, hist.size() - maxLines - this.historyScroll);

         for(int i = 0; i < maxLines && startIdx + i < hist.size(); ++i) {
            ShopClientData.HistoryEntryInfo entry = (ShopClientData.HistoryEntryInfo)hist.get(startIdx + i);
            int rarityColor = getRarityColor(entry.rarityOrdinal);
            String timeAgo = formatTimeAgo(entry.timestamp);
            String line = entry.crateDisplayName + " §7→ ";
            int lineX = panelX + 6;
            int lineY2 = panelY + 15 + i * lineH;
            this.fontRenderer.drawString(line, lineX, lineY2, -7700886);
            int afterArrow = lineX + this.fontRenderer.getStringWidth(line);
            String itemPart = entry.itemDisplayName;
            this.fontRenderer.drawString(itemPart, afterArrow, lineY2, rarityColor);
            int afterItem = afterArrow + this.fontRenderer.getStringWidth(itemPart);
            this.fontRenderer.drawString(" §8- " + timeAgo, afterItem, lineY2, -7700886);
         }

      }
   }

   private void drawRollingAnimation(int mouseX, int mouseY) {
      long elapsed = System.currentTimeMillis() - this.animationStartTime;
      float progress = Math.min(1.0F, (float)elapsed / 4000.0F);
      float eased = 1.0F - (1.0F - progress) * (1.0F - progress) * (1.0F - progress);
      float totalDistance = 2700.0F;
      this.stripOffset = eased * totalDistance;
      int centerItemIndex = (int)((this.stripOffset + 180.0F) / 60.0F);
      if (centerItemIndex != this.lastTickIndex) {
         this.lastTickIndex = centerItemIndex;
         EntityPlayer player = Minecraft.getMinecraft().player;
         if (player != null) {
            ShopClientData.CrateClientInfo rollingCrate = ShopClientData.getCrateById(ShopClientData.pendingResult != null ? ShopClientData.pendingResult.crateId : null);
            boolean isCashRoll = rollingCrate != null && rollingCrate.usesCrystals();
            if (isCashRoll) {
               player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2F, 1.2F + progress * 0.8F);
            } else {
               player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.3F, 1.5F + progress * 0.5F);
            }
         }
      }

      ShopClientData.CrateResultData result = ShopClientData.pendingResult;
      if (result != null) {
         boolean isDupe = result.isDuplicate;
         drawRect(0, 0, this.width, this.height, isDupe ? -1440603614 : -1442840576);
         int stripTotalW = 360;
         int stripX = (this.width - stripTotalW) / 2;
         int stripY = this.height / 2 - 30;
         drawRect(stripX - 4, stripY - 4, stripX + stripTotalW + 4, stripY + 60 + 4, -7706054);
         drawRect(stripX - 2, stripY - 2, stripX + stripTotalW + 2, stripY + 60 + 2, -14540254);
         drawRect(stripX, stripY, stripX + stripTotalW, stripY + 60, -15066598);
         String rollTitle = "§6§lOPENING CRATE...";
         if (this.animationDone) {
            if (isDupe) {
               rollTitle = "§c§lDUPLICATE!";
            } else {
               rollTitle = "§e§lREWARD OBTAINED!";
            }
         }

         int titleW = this.fontRenderer.getStringWidth(rollTitle);
         this.fontRenderer.drawStringWithShadow(rollTitle, (float)(this.width / 2 - titleW / 2), (float)(stripY - 20), 16777215);
         if (ShopClientData.multiRollTotal > 1) {
            String rollCounter = "§7Roll " + ShopClientData.multiRollCurrent + "/" + ShopClientData.multiRollTotal;
            int counterW = this.fontRenderer.getStringWidth(rollCounter);
            this.fontRenderer.drawStringWithShadow(rollCounter, (float)(this.width / 2 - counterW / 2), (float)(stripY - 32), 16777215);
         }

         Minecraft mc = Minecraft.getMinecraft();
         GL11.glEnable(3089);
         double scaleX = (double)mc.displayWidth / (double)this.width;
         double scaleY = (double)mc.displayHeight / (double)this.height;
         int scissorX = (int)((double)stripX * scaleX);
         int scissorY = (int)((double)(this.height - stripY - 60) * scaleY);
         int scissorW = (int)((double)stripTotalW * scaleX);
         int scissorH = (int)((double)60.0F * scaleY);
         GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
         float centerOffset = 150.0F;

         for(int i = 0; i < result.displayStrip.size(); ++i) {
            float itemXf = (float)(stripX + i * 60) - this.stripOffset + centerOffset;
            if (itemXf > (float)(stripX - 60) && itemXf < (float)(stripX + stripTotalW + 60)) {
               ShopClientData.LootEntryClientInfo entry = (ShopClientData.LootEntryClientInfo)result.displayStrip.get(i);
               int rarityColor = getRarityColor(entry.rarityOrdinal);
               String rarityName = ItemRarity.fromOrdinal(entry.rarityOrdinal).displayName;
               int ix = (int)itemXf;
               int bgColor = rarityColor & 16777215 | 1073741824;
               drawRect(ix + 2, stripY + 2, ix + 60 - 2, stripY + 60 - 2, bgColor);
               this.drawHollowRect(ix + 1, stripY + 1, 58, 58, rarityColor);
               ItemStack displayStack = resolveItemStack(entry.itemId, entry.itemMeta, entry.itemCount, (NBTTagCompound)null);
               if (!displayStack.isEmpty()) {
                  GlStateManager.enableDepth();
                  RenderHelper.enableGUIStandardItemLighting();
                  mc.getRenderItem().renderItemIntoGUI(displayStack, ix + 3, stripY + 4);
                  RenderHelper.disableStandardItemLighting();
                  GlStateManager.disableDepth();
               }

               String itemName = this.getEntryDisplayName(entry);
               String truncName = this.fontRenderer.trimStringToWidth(itemName, 36);
               this.fontRenderer.drawStringWithShadow(truncName, (float)(ix + 21), (float)(stripY + 6), -1);
               String truncRarity = this.fontRenderer.trimStringToWidth(rarityName, 52);
               this.fontRenderer.drawString(truncRarity, ix + 21, stripY + 18, rarityColor);
               if (entry.itemCount > 1) {
                  String countStr = "x" + entry.itemCount;
                  int countW = this.fontRenderer.getStringWidth(countStr);
                  this.fontRenderer.drawString(countStr, ix + 60 - countW - 4, stripY + 60 - 14, -7700886);
               }
            }
         }

         GL11.glDisable(3089);
         int markerX = stripX + stripTotalW / 2;
         drawRect(markerX - 1, stripY - 8, markerX + 1, stripY, -22016);
         drawRect(markerX - 3, stripY - 10, markerX + 3, stripY - 8, -22016);
         drawRect(markerX - 1, stripY + 60, markerX + 1, stripY + 60 + 8, -22016);
         drawRect(markerX - 3, stripY + 60 + 8, markerX + 3, stripY + 60 + 10, -22016);
         if (progress >= 1.0F && !this.animationDone) {
            this.animationDone = true;
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player != null && result != null) {
               ShopClientData.CrateClientInfo revealCrate = ShopClientData.getCrateById(result.crateId);
               boolean isCashReveal = revealCrate != null && revealCrate.usesCrystals();
               if (isCashReveal) {
                  player.playSound(SoundEvents.BLOCK_NOTE_CHIME, 0.6F, 1.8F);
               } else {
                  player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.5F, 1.6F);
               }

               int rarity = result.wonRarity;
               if (isCashReveal) {
                  if (rarity >= 5) {
                     player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
                     player.playSound(SoundEvents.ENTITY_FIREWORK_LARGE_BLAST, 0.8F, 1.0F);
                  } else if (rarity >= 4) {
                     player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.2F);
                  } else if (rarity >= 3) {
                     player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                     player.playSound(SoundEvents.BLOCK_ANVIL_LAND, 0.3F, 1.5F);
                  } else if (rarity >= 2) {
                     player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.8F, 1.2F);
                  } else {
                     player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8F, 1.0F);
                  }
               } else if (rarity >= 5) {
                  player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
                  player.playSound(SoundEvents.ENTITY_ENDERDRAGON_GROWL, 0.5F, 1.5F);
               } else if (rarity >= 4) {
                  player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.2F);
               } else if (rarity >= 3) {
                  player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                  player.playSound(SoundEvents.ENTITY_FIREWORK_LARGE_BLAST, 0.6F, 1.0F);
               } else if (rarity >= 2) {
                  player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.8F, 1.2F);
               } else {
                  player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8F, 1.0F);
               }
            }
         }

         if (this.animationDone) {
            int wonColor = getRarityColor(result.wonRarity);
            String wonRarityName = ItemRarity.fromOrdinal(result.wonRarity).displayName;
            ItemStack wonStack = resolveItemStack(result.wonItemId, result.wonItemMeta, result.wonItemCount, (NBTTagCompound)null);
            int resultY = stripY + 60 + 14;
            int textBlockH = 48;
            if (result.hasBundle()) {
               textBlockH += 11 + result.wonBundleItems.size() * 10;
            }

            if (isDupe && (result.dupeCrystalsAwarded > 0 || result.duplicateTokensAwarded > 0)) {
               textBlockH += 14;
            }

            int textBgY = resultY - 4;
            int textBgH = textBlockH + 8;
            this.drawGradientRect(this.width / 2 - 140, textBgY, this.width / 2 + 140, textBgY + textBgH, -300281832, -586545144);
            if (!wonStack.isEmpty()) {
               int iconCenterX = this.width / 2 - 8;
               GlStateManager.enableDepth();
               RenderHelper.enableGUIStandardItemLighting();
               mc.getRenderItem().renderItemIntoGUI(wonStack, iconCenterX, resultY);
               mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, wonStack, iconCenterX, resultY, "");
               RenderHelper.disableStandardItemLighting();
               GlStateManager.disableDepth();
            }

            String displayName = result.wonDisplayName;
            if (result.wonBundleDisplayName != null && !result.wonBundleDisplayName.isEmpty()) {
               displayName = result.wonBundleDisplayName;
            }

            String itemText = displayName + " (" + wonRarityName + ")";
            int itemW = this.fontRenderer.getStringWidth(itemText);
            this.fontRenderer.drawStringWithShadow(itemText, (float)(this.width / 2 - itemW / 2), (float)(resultY + 20), wonColor);
            int nextY = resultY + 32;
            if (result.hasBundle()) {
               String bundleLabel = "§7Contains " + (result.wonBundleItems.size() + 1) + " items:";
               int bundleLW = this.fontRenderer.getStringWidth(bundleLabel);
               this.fontRenderer.drawString(bundleLabel, this.width / 2 - bundleLW / 2, nextY, -7700886);
               nextY += 11;

               for(ShopClientData.BundleItemClientInfo bi : result.wonBundleItems) {
                  String biName = getItemDisplayName(bi.itemId);
                  if (bi.itemCount > 1) {
                     biName = biName + " x" + bi.itemCount;
                  }

                  String biText = "§7+ " + biName;
                  int biW = this.fontRenderer.getStringWidth(biText);
                  this.fontRenderer.drawString(biText, this.width / 2 - biW / 2, nextY, -2832216);
                  nextY += 10;
               }
            }

            if (isDupe) {
               if (result.dupeCrystalsAwarded > 0) {
                  String dupeText = "§cDUPLICATE! §7+§d" + result.dupeCrystalsAwarded + " Crystals Refunded§7!";
                  int dupeW = this.fontRenderer.getStringWidth(dupeText);
                  this.fontRenderer.drawStringWithShadow(dupeText, (float)(this.width / 2 - dupeW / 2), (float)nextY, -1);
                  nextY += 14;
               } else if (result.duplicateTokensAwarded > 0) {
                  String dupeText = "§cDUPLICATE §7- Converted to §d" + result.duplicateTokensAwarded + " tokens§7!";
                  int dupeW = this.fontRenderer.getStringWidth(dupeText);
                  this.fontRenderer.drawStringWithShadow(dupeText, (float)(this.width / 2 - dupeW / 2), (float)nextY, -1);
                  nextY += 14;
               }
            }

            String contText;
            if (!ShopClientData.multiRollQueue.isEmpty()) {
               contText = "§7Click for next roll";
            } else {
               contText = "§7Click to continue";
            }

            int contW = this.fontRenderer.getStringWidth(contText);
            this.fontRenderer.drawString(contText, this.width / 2 - contW / 2, nextY + 2, 11184810);
         }

      }
   }

   protected void actionPerformed(GuiButton button) throws IOException {
      if (button.id == 0) {
         QuestLogGui.open();
      } else if (button.id == 6) {
         this.tokenShopMode = false;
         this.crystalShopMode = false;
         this.passMode = false;
         this.redeemMode = false;
         this.killEffectsOpen = false;
         this.tokenShopScroll = 0;
         this.rebuildButtons();
      } else if (button.id == 7) {
         this.tokenShopMode = true;
         this.crystalShopMode = false;
         this.passMode = false;
         this.redeemMode = false;
         this.killEffectsOpen = false;
         this.tokenShopScroll = 0;
         this.tokenShopItems = null;
         this.rebuildButtons();
      } else if (button.id == 8) {
         this.crystalShopMode = true;
         this.tokenShopMode = false;
         this.passMode = false;
         this.redeemMode = false;
         this.killEffectsOpen = false;
         this.selectedCategory = ShopCategory.CASH;
         this.selectedCrateId = null;
         this.crateListScroll = 0;
         this.lootScrollOffset = 0;
         this.rebuildButtons();
      } else if (button.id == 9) {
         this.passMode = true;
         this.tokenShopMode = false;
         this.crystalShopMode = false;
         this.redeemMode = false;
         this.killEffectsOpen = false;
         this.passScroll = 0;
         this.rebuildButtons();
      } else if (button.id == 95) {
         this.redeemMode = true;
         this.tokenShopMode = false;
         this.crystalShopMode = false;
         this.passMode = false;
         this.killEffectsOpen = false;
         this.rebuildButtons();
      } else if (button.id == 96) {
         ShopModInit.NETWORK.sendToServer(new ShopActionMessage((byte)12, ""));
      } else if (button.id == 70) {
         ShopModInit.NETWORK.sendToServer(new BattlePassActionMessage((byte)0, "1"));
         ShopClientData.bpPurchasedTier = 1;
         this.rebuildButtons();
      } else if (button.id == 71) {
         ShopModInit.NETWORK.sendToServer(new BattlePassActionMessage((byte)0, "2"));
         ShopClientData.bpPurchasedTier = 2;
         this.rebuildButtons();
      } else if (button.id == 72) {
         int tierToClaim = -1;

         for(int t = 1; t <= ShopClientData.bpCurrentTier; ++t) {
            if (!ShopClientData.bpClaimedTiers.contains(t)) {
               tierToClaim = t;
               break;
            }
         }

         if (tierToClaim > 0) {
            ShopModInit.NETWORK.sendToServer(new BattlePassActionMessage((byte)1, String.valueOf(tierToClaim)));
            ShopClientData.bpClaimedTiers.add(tierToClaim);
         }

         this.rebuildButtons();
      } else if (button.id == 73) {
         ShopModInit.NETWORK.sendToServer(new BattlePassActionMessage((byte)2));
         this.rebuildButtons();
      } else if (button.id == 90) {
         this.killEffectsOpen = !this.killEffectsOpen;
         this.rebuildButtons();
      } else if (button.id == 91) {
         this.killEffectsOpen = false;
         this.rebuildButtons();
      } else if (button.id >= 80 && button.id < 90) {
         int effectIdx = button.id - 80;
         String effectId = getOwnedEffectByIndex(effectIdx);
         if (effectId != null) {
            ShopModInit.NETWORK.sendToServer(new BattlePassActionMessage((byte)3, effectId));
            this.rebuildButtons();
         }

      } else if (button.id == 5) {
         ShopModInit.NETWORK.sendToServer(new ShopActionMessage((byte)7));
         button.enabled = false;
         this.rebuildButtons();
      } else if (button.id >= 60 && button.id < 66) {
         int villageIdx = button.id - 60;
         String villageId = TRAVEL_VILLAGE_IDS[villageIdx];
         ShopModInit.NETWORK.sendToServer(new ShopActionMessage((byte)8, villageId));
         ShopClientData.travelCooldownEnd = System.currentTimeMillis() + 300000L;
      } else if (button.id >= 50 && button.id < 100) {
         int visualIdx = button.id - 50;
         List<TokenShopItem> filtered = this.getFilteredTokenItems();
         int itemIdx = visualIdx + this.tokenShopScroll;
         if (itemIdx >= 0 && itemIdx < filtered.size()) {
            TokenShopItem item = (TokenShopItem)filtered.get(itemIdx);
            if (ShopClientData.tokenBalance >= item.tokenCost) {
               ShopModInit.NETWORK.sendToServer(new ShopActionMessage((byte)4, item.purchaseKey));
               ShopModInit.NETWORK.sendToServer(new ShopActionMessage((byte)0));
            }
         }

      } else if (button.id == 2 && this.selectedCrateId != null) {
         ShopClientData.CrateClientInfo selCrate = ShopClientData.getCrateById(this.selectedCrateId);
         if (selCrate != null) {
            boolean isFreeRoll = selCrate.usesCrystals() && ("cash_clan".equals(selCrate.crateId) && ShopClientData.freeRollAvailable || "cash_so6p".equals(selCrate.crateId) && ShopClientData.weekendPromoSO6PAvailable);
            boolean canAfford;
            if (isFreeRoll) {
               canAfford = true;
            } else if (selCrate.usesCrystals()) {
               canAfford = ShopClientData.crystalBalance >= selCrate.crystalPrice;
            } else {
               canAfford = ShopClientData.ryoBalance >= selCrate.price;
            }

            if (canAfford) {
               ShopModInit.NETWORK.sendToServer(new ShopActionMessage((byte)2, this.selectedCrateId));
            }
         }

      } else if (button.id == 4 && this.selectedCrateId != null) {
         ShopClientData.CrateClientInfo selCrate = ShopClientData.getCrateById(this.selectedCrateId);
         if (selCrate != null) {
            boolean canAfford;
            if (selCrate.usesCrystals()) {
               int multiCrystalPrice = selCrate.crystalPrice * 10;
               canAfford = ShopClientData.crystalBalance >= multiCrystalPrice;
            } else {
               long multiPrice = selCrate.price * 9L;
               canAfford = ShopClientData.ryoBalance >= multiPrice;
            }

            if (canAfford) {
               ShopModInit.NETWORK.sendToServer(new ShopActionMessage((byte)5, this.selectedCrateId));
            }
         }

      } else if (button.id == 3) {
         this.historyOpen = !this.historyOpen;
         if (this.historyOpen) {
            ShopModInit.NETWORK.sendToServer(new ShopActionMessage((byte)3));
         }

         this.rebuildButtons();
      } else {
         ShopCategory[] cats = ShopCategory.values();
         if (button.id >= 10 && button.id < 10 + cats.length) {
            int catIdx = button.id - 10;
            this.selectedCategory = ShopCategory.fromOrdinal(catIdx);
            this.selectedCrateId = null;
            this.crateListScroll = 0;
            this.lootScrollOffset = 0;
            this.tokenShopScroll = 0;
            this.buildingGridScroll = 0;
            this.crystalShopMode = false;
            this.passMode = false;
            this.redeemMode = false;
            this.killEffectsOpen = false;
            this.rebuildButtons();
         } else if (button.id >= 800 && button.id < 808) {
            this.buildingSubTab = button.id - 800;
            this.buildingGridScroll = 0;
            this.buildingSearchText = "";
            if (this.buildingSearchField != null) {
               this.buildingSearchField.setText("");
            }

            this.rebuildButtons();
         } else if (button.id >= 810 && button.id < 860) {
            int visualIdx = button.id - 810;
            this.addBuildingItemToCart(visualIdx, 1);
            this.rebuildButtons();
         } else if (button.id >= 860 && button.id < 910) {
            int visualIdx = button.id - 860;
            this.removeBuildingItemFromCart(visualIdx);
            this.rebuildButtons();
         } else if (button.id >= 910 && button.id < 960) {
            int visualIdx = button.id - 910;
            this.addBuildingItemToCart(visualIdx, 64);
            this.rebuildButtons();
         } else if (button.id >= 1010 && button.id < 1060) {
            int visualIdx = button.id - 1010;
            List<BuildingShopRegistry.BuildingItem> items = this.getFilteredBuildingItems();
            if (items != null) {
               int idx = visualIdx + this.buildingGridScroll;
               if (idx >= 0 && idx < items.size()) {
                  BuildingShopRegistry.BuildingItem item = (BuildingShopRegistry.BuildingItem)items.get(idx);
                  BuildingCartData.addShulkerBox(item.registryName, item.meta, item.displayName, item.ryoPrice);
               }
            }

            this.rebuildButtons();
         } else if (button.id != 960) {
            if (button.id >= 20 && button.id < 40) {
               int entryIdx = button.id - 20 + this.crateListScroll;
               List<ShopClientData.CrateClientInfo> crates = ShopClientData.getCratesByCategory(this.selectedCategory);
               if (entryIdx >= 0 && entryIdx < crates.size()) {
                  this.selectedCrateId = ((ShopClientData.CrateClientInfo)crates.get(entryIdx)).crateId;
                  this.lootScrollOffset = 0;
                  this.rebuildButtons();
               }

            }
         } else {
            if (!BuildingCartData.isEmpty() && ShopClientData.ryoBalance >= BuildingCartData.getTotalCost()) {
               BuildingPurchaseMessage msg = new BuildingPurchaseMessage();

               for(BuildingCartData.CartEntry entry : BuildingCartData.getEntries()) {
                  if (entry.isShulkerBox) {
                     msg.addShulkerBox(entry.registryName, entry.meta, entry.quantity);
                  } else {
                     msg.addItem(entry.registryName, entry.meta, entry.quantity);
                  }
               }

               ShopModInit.NETWORK.sendToServer(msg);
               BuildingCartData.clear();
               this.rebuildButtons();
            }

         }
      }
   }

   private void addBuildingItemToCart(int visualIdx, int quantity) {
      List<BuildingShopRegistry.BuildingItem> items = this.getFilteredBuildingItems();
      if (items != null) {
         int idx = visualIdx + this.buildingGridScroll;
         if (idx >= 0 && idx < items.size()) {
            BuildingShopRegistry.BuildingItem item = (BuildingShopRegistry.BuildingItem)items.get(idx);
            BuildingCartData.addToCart(item.registryName, item.meta, item.displayName, item.ryoPrice, quantity);
         }

      }
   }

   private static ItemStack resolveShopItemStack(String registryName, int meta) {
      if (registryName != null && registryName.startsWith("toolkit:")) {
         Item shulker = Item.getByNameOrId("minecraft:white_shulker_box");
         return shulker != null ? new ItemStack(shulker, 1, 0) : ItemStack.EMPTY;
      } else {
         ResourceLocation rl = new ResourceLocation(registryName);
         Item item = (Item)Item.REGISTRY.getObject(rl);
         if (item != null) {
            ItemStack stack = new ItemStack(item, 1, meta);
            if (!stack.isEmpty()) {
               return stack;
            }
         }

         Block block = (Block)Block.REGISTRY.getObject(rl);
         if (block != null && block != Blocks.AIR) {
            ItemStack stack = new ItemStack(block, 1, meta);
            if (!stack.isEmpty()) {
               return stack;
            }
         }

         item = Item.getByNameOrId(registryName);
         if (item != null) {
            ItemStack stack = new ItemStack(item, 1, meta);
            if (!stack.isEmpty()) {
               return stack;
            }
         }

         return ItemStack.EMPTY;
      }
   }

   private void removeBuildingItemFromCart(int visualIdx) {
      List<BuildingShopRegistry.BuildingItem> items = this.getFilteredBuildingItems();
      if (items != null) {
         int idx = visualIdx + this.buildingGridScroll;
         if (idx >= 0 && idx < items.size()) {
            BuildingShopRegistry.BuildingItem item = (BuildingShopRegistry.BuildingItem)items.get(idx);
            BuildingCartData.removeOne(item.registryName, item.meta);
         }

      }
   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      if (!this.tokenShopMode && !this.crystalShopMode && !this.passMode && this.selectedCategory == ShopCategory.WEAPONS) {
         if (this.buildingSearchField != null) {
            this.buildingSearchField.mouseClicked(mouseX, mouseY, mouseButton);
         }

         if (mouseButton == 0) {
            int gridX = this.guiLeft + 6;
            int gridY = this.guiTop + 70;
            int gridH = 188;
            int rowH = 22;
            int maxVisible = gridH / rowH;
            List<BuildingShopRegistry.BuildingItem> items = this.getFilteredBuildingItems();
            if (items != null) {
               for(int i = 0; i < Math.min(maxVisible, items.size() - this.buildingGridScroll); ++i) {
                  int idx = i + this.buildingGridScroll;
                  int rowY = gridY + i * rowH;
                  if (mouseX >= gridX + 60 && mouseX <= gridX + 115 && mouseY >= rowY + 10 && mouseY <= rowY + 24) {
                     this.buildingQtyEditIndex = idx;
                     int currentQty = BuildingCartData.getQuantity(((BuildingShopRegistry.BuildingItem)items.get(idx)).registryName, ((BuildingShopRegistry.BuildingItem)items.get(idx)).meta);
                     this.buildingQtyField = new GuiTextField(998, this.fontRenderer, gridX + 65, rowY + 11, 45, 12);
                     this.buildingQtyField.setMaxStringLength(5);
                     this.buildingQtyField.setText(String.valueOf(currentQty));
                     this.buildingQtyField.setFocused(true);
                     this.buildingQtyField.setTextColor(-1);
                     return;
                  }
               }
            }

            if (this.buildingQtyEditIndex >= 0) {
               this.commitBuildingQtyEdit();
            }
         }

         if (this.buildingQtyField != null) {
            this.buildingQtyField.mouseClicked(mouseX, mouseY, mouseButton);
         }
      }

      if (this.animationDone && ShopClientData.pendingResult != null) {
         ShopClientData.pendingResult.consumed = true;
         this.animationActive = false;
         this.animationDone = false;
         if (!ShopClientData.multiRollQueue.isEmpty()) {
            ShopClientData.CrateResultData nextResult = (ShopClientData.CrateResultData)ShopClientData.multiRollQueue.remove(0);
            ShopClientData.pendingResult = nextResult;
            ShopClientData.pendingDuplicate = nextResult.isDuplicate;
            ShopClientData.pendingDuplicateTokens = nextResult.duplicateTokensAwarded;
            ShopClientData.pendingDupeCrystals = nextResult.dupeCrystalsAwarded;
            ShopClientData.multiRollCurrent = ShopClientData.multiRollTotal - ShopClientData.multiRollQueue.size();
         } else {
            ShopClientData.multiRollTotal = 0;
            ShopClientData.multiRollCurrent = 0;
            ShopModInit.NETWORK.sendToServer(new ShopActionMessage((byte)6));
            ShopModInit.NETWORK.sendToServer(new ShopActionMessage((byte)0));
            this.rebuildButtons();
         }
      } else if (!this.animationActive || this.animationDone) {
         super.mouseClicked(mouseX, mouseY, mouseButton);
      }
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      int scroll = Mouse.getEventDWheel();
      if (scroll != 0) {
         int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
         int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
         if (this.passMode && !this.killEffectsOpen) {
            boolean isPurchased = ShopClientData.bpPurchasedTier > 0;
            int tierListStartY = isPurchased ? this.guiTop + 50 : this.guiTop + 82;
            int colHeaderH = 18;
            int listY = tierListStartY + colHeaderH;
            int listEndY = this.guiTop + this.getActiveHeight() - 40;
            int tierH = 18;
            int maxVisible = Math.max(1, Math.min(20, (listEndY - listY) / tierH));
            int maxScroll = Math.max(0, 20 - maxVisible);
            if (scroll > 0) {
               this.passScroll = Math.max(0, this.passScroll - 1);
            } else {
               this.passScroll = Math.min(maxScroll, this.passScroll + 1);
            }
         } else if (this.tokenShopMode) {
            List<TokenShopItem> filtered = this.getFilteredTokenItems();
            int lineH = 20;
            int panelH = 254;
            int maxVisible = (panelH - 28) / lineH;
            int maxScroll = Math.max(0, filtered.size() - maxVisible);
            if (scroll > 0) {
               this.tokenShopScroll = Math.max(0, this.tokenShopScroll - 1);
            } else {
               this.tokenShopScroll = Math.min(maxScroll, this.tokenShopScroll + 1);
            }

            this.rebuildButtons();
         } else if (this.crystalShopMode) {
            int cardW = 180;
            int detailPanelX = this.guiLeft + 8 + cardW + 4;
            int detailPanelRight = this.guiLeft + this.getActiveWidth() - 8;
            if (mouseX >= detailPanelX && mouseX <= detailPanelRight) {
               if (scroll > 0) {
                  this.lootScrollOffset = Math.max(0, this.lootScrollOffset - 1);
               } else {
                  ++this.lootScrollOffset;
               }
            } else {
               List<ShopClientData.CrateClientInfo> crates = ShopClientData.getCratesByCategory(ShopCategory.CASH);
               int maxScroll = Math.max(0, crates.size() - 8);
               if (scroll > 0) {
                  this.crateListScroll = Math.max(0, this.crateListScroll - 1);
               } else {
                  this.crateListScroll = Math.min(maxScroll, this.crateListScroll + 1);
               }

               this.rebuildButtons();
            }
         } else if (!this.tokenShopMode && this.selectedCategory == ShopCategory.WEAPONS) {
            int cartPanelX = this.guiLeft + 266;
            if (mouseX >= cartPanelX) {
               int cartMaxVisible = 12;
               int cartMaxScroll = Math.max(0, BuildingCartData.getEntryCount() - cartMaxVisible);
               if (scroll > 0) {
                  this.buildingCartScroll = Math.max(0, this.buildingCartScroll - 1);
               } else {
                  this.buildingCartScroll = Math.min(cartMaxScroll, this.buildingCartScroll + 1);
               }
            } else {
               List<BuildingShopRegistry.BuildingItem> items = this.getFilteredBuildingItems();
               int gridH = 188;
               int rowH = 22;
               int maxVisible = gridH / rowH;
               int total = items != null ? items.size() : 0;
               int maxScroll = Math.max(0, total - maxVisible);
               if (scroll > 0) {
                  this.buildingGridScroll = Math.max(0, this.buildingGridScroll - 1);
               } else {
                  this.buildingGridScroll = Math.min(maxScroll, this.buildingGridScroll + 1);
               }
            }

            this.rebuildButtons();
         } else {
            int detailPanelX = this.guiLeft + 188;
            int detailPanelW = 206;
            if (mouseX >= detailPanelX && mouseX <= detailPanelX + detailPanelW) {
               if (scroll > 0) {
                  this.lootScrollOffset = Math.max(0, this.lootScrollOffset - 1);
               } else {
                  ++this.lootScrollOffset;
               }
            } else {
               List<ShopClientData.CrateClientInfo> crates = ShopClientData.getCratesByCategory(this.selectedCategory);
               int maxScroll = Math.max(0, crates.size() - 10);
               if (scroll > 0) {
                  this.crateListScroll = Math.max(0, this.crateListScroll - 1);
               } else {
                  this.crateListScroll = Math.min(maxScroll, this.crateListScroll + 1);
               }

               this.rebuildButtons();
            }
         }
      }

   }

   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (this.buildingQtyField != null && this.buildingQtyField.isFocused()) {
         if (keyCode == 28) {
            this.commitBuildingQtyEdit();
         } else if (keyCode == 1) {
            this.buildingQtyEditIndex = -1;
            this.buildingQtyField = null;
         } else {
            if (Character.isDigit(typedChar) || keyCode == 14 || keyCode == 203 || keyCode == 205) {
               this.buildingQtyField.textboxKeyTyped(typedChar, keyCode);
            }

         }
      } else if (this.buildingSearchField != null && this.buildingSearchField.isFocused()) {
         this.buildingSearchField.textboxKeyTyped(typedChar, keyCode);
         this.buildingSearchText = this.buildingSearchField.getText();
         this.buildingGridScroll = 0;
         this.rebuildButtons();
         if (keyCode == 1) {
            this.buildingSearchField.setFocused(false);
         }

      } else {
         super.keyTyped(typedChar, keyCode);
      }
   }

   private void commitBuildingQtyEdit() {
      if (this.buildingQtyEditIndex >= 0 && this.buildingQtyField != null) {
         List<BuildingShopRegistry.BuildingItem> items = this.getFilteredBuildingItems();
         if (items != null && this.buildingQtyEditIndex < items.size()) {
            BuildingShopRegistry.BuildingItem item = (BuildingShopRegistry.BuildingItem)items.get(this.buildingQtyEditIndex);

            try {
               int newQty = Integer.parseInt(this.buildingQtyField.getText().trim());
               if (newQty <= 0) {
                  BuildingCartData.removeEntry(item.registryName, item.meta);
               } else {
                  int currentQty = BuildingCartData.getQuantity(item.registryName, item.meta);
                  if (currentQty == 0) {
                     BuildingCartData.addToCart(item.registryName, item.meta, item.displayName, item.ryoPrice, newQty);
                  } else {
                     BuildingCartData.setQuantity(item.registryName, item.meta, newQty);
                  }
               }
            } catch (NumberFormatException var5) {
            }
         }
      }

      this.buildingQtyEditIndex = -1;
      this.buildingQtyField = null;
      this.rebuildButtons();
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   private static int getRarityColor(int rarityOrdinal) {
      return ItemRarity.fromOrdinal(rarityOrdinal).color;
   }

   private static ItemStack resolveItemStack(String itemId, int meta, int count, NBTTagCompound nbt) {
      if (itemId != null && !itemId.isEmpty()) {
         Item item = Item.getByNameOrId(itemId);
         if (item == null) {
            return ItemStack.EMPTY;
         } else {
            ItemStack stack = new ItemStack(item, Math.max(1, count), meta);
            if (nbt != null) {
               stack.setTagCompound(nbt.copy());
            }

            return stack;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   private static String getItemDisplayName(String itemId) {
      if (itemId != null && !itemId.isEmpty()) {
         Item item = Item.getByNameOrId(itemId);
         if (item != null) {
            ItemStack stack = new ItemStack(item, 1, 0);
            String name = stack.getDisplayName();
            if (name != null && !name.isEmpty()) {
               return name;
            }
         }

         int colonIdx = itemId.indexOf(58);
         if (colonIdx >= 0 && colonIdx < itemId.length() - 1) {
            String name = itemId.substring(colonIdx + 1);
            StringBuilder sb = new StringBuilder();
            boolean capitalize = true;

            for(int i = 0; i < name.length(); ++i) {
               char c = name.charAt(i);
               if (c == '_') {
                  sb.append(' ');
                  capitalize = true;
               } else {
                  sb.append(capitalize ? Character.toUpperCase(c) : c);
                  capitalize = false;
               }
            }

            return sb.toString();
         } else {
            return itemId;
         }
      } else {
         return "Unknown";
      }
   }

   private static String formatTimeAgo(long timestamp) {
      long diff = System.currentTimeMillis() - timestamp;
      if (diff < 0L) {
         diff = 0L;
      }

      long seconds = diff / 1000L;
      if (seconds < 60L) {
         return seconds + "s ago";
      } else {
         long minutes = seconds / 60L;
         if (minutes < 60L) {
            return minutes + "m ago";
         } else {
            long hours = minutes / 60L;
            if (hours < 24L) {
               return hours + "h ago";
            } else {
               long days = hours / 24L;
               return days + "d ago";
            }
         }
      }
   }

   private void drawHollowRect(int x, int y, int w, int h, int color) {
      drawRect(x, y, x + w, y + 1, color);
      drawRect(x, y + h - 1, x + w, y + h, color);
      drawRect(x, y, x + 1, y + h, color);
      drawRect(x + w - 1, y, x + w, y + h, color);
   }

   public static void open() {
      Minecraft mc = Minecraft.getMinecraft();
      mc.displayGuiScreen(new RyoShopGui());
      ShopModInit.NETWORK.sendToServer(new ShopActionMessage((byte)0));
   }

   private List<TokenShopItem> buildTokenShopItems() {
      List<TokenShopItem> items = new ArrayList();
      Set<String> seen = new HashSet();

      for(ShopClientData.CrateClientInfo crate : ShopClientData.availableCrates) {
         if (crate.getCategory() != ShopCategory.CASH) {
            for(ShopClientData.LootEntryClientInfo entry : crate.lootEntries) {
               String key = entry.itemId + ":" + entry.itemMeta;
               if (!seen.contains(key)) {
                  seen.add(key);
                  int cost = getTokenCostClient(entry.rarityOrdinal, crate.categoryOrdinal);
                  if (cost > 0) {
                     String name = this.getEntryDisplayName(entry);
                     items.add(new TokenShopItem(entry.itemId, entry.itemMeta, entry.itemCount, entry.itemNbt, name, entry.rarityOrdinal, cost, crate.categoryOrdinal));
                  }
               }
            }
         }
      }

      Collections.sort(items, new Comparator<TokenShopItem>() {
         public int compare(TokenShopItem a, TokenShopItem b) {
            int rc = Integer.compare(b.rarityOrdinal, a.rarityOrdinal);
            return rc != 0 ? rc : a.displayName.compareTo(b.displayName);
         }
      });
      return items;
   }

   private static int getTokenCostClient(int rarityOrdinal, int categoryOrdinal) {
      if (rarityOrdinal >= 5) {
         return 0;
      } else if (rarityOrdinal != 4 || categoryOrdinal != 4 && categoryOrdinal != 5) {
         int baseCost;
         switch (rarityOrdinal) {
            case 0:
               baseCost = 10;
               break;
            case 1:
               baseCost = 30;
               break;
            case 2:
               baseCost = 80;
               break;
            case 3:
               baseCost = 200;
               break;
            case 4:
               baseCost = 500;
               break;
            default:
               return 0;
         }

         int mult10;
         switch (categoryOrdinal) {
            case 3:
               mult10 = 20;
               break;
            case 4:
               mult10 = 50;
               break;
            case 5:
               mult10 = 40;
               break;
            default:
               mult10 = 5;
         }

         return Math.max(1, baseCost * mult10 / 10);
      } else {
         return 0;
      }
   }

   private List<TokenShopItem> getFilteredTokenItems() {
      if (this.tokenShopItems == null) {
         this.tokenShopItems = this.buildTokenShopItems();
      }

      List<TokenShopItem> filtered = new ArrayList();

      for(TokenShopItem item : this.tokenShopItems) {
         if (item.categoryOrdinal == this.selectedCategory.id) {
            filtered.add(item);
         }
      }

      return filtered;
   }

   private void drawPassTab(int mouseX, int mouseY) {
      int activeW = this.getActiveWidth();
      int activeH = this.getActiveHeight();
      int pad = 6;
      int panelX = this.guiLeft + pad;
      int panelW = activeW - pad * 2;
      boolean isPurchased = ShopClientData.bpPurchasedTier > 0;
      boolean isOtsutsuki = ShopClientData.bpPurchasedTier >= 2;
      int headerY = this.guiTop + 22;
      String passName;
      if (isOtsutsuki) {
         passName = "§6§lOtsutsuki Pass";
      } else if (isPurchased) {
         passName = "§d§lKage Pass";
      } else {
         passName = "§e§lShinobi Pass";
      }

      String seasonSuffix = " §8— §7" + ShopClientData.bpSeasonName;
      this.fontRenderer.drawStringWithShadow(passName + seasonSuffix, (float)(panelX + 4), (float)(headerY + 2), -1521552);
      long remainMs = ShopClientData.bpSeasonEndTime > 0L ? ShopClientData.bpSeasonEndTime - System.currentTimeMillis() : 0L;
      if (remainMs < 0L) {
         remainMs = 0L;
      }

      int daysLeft = (int)(remainMs / 86400000L);
      int hoursLeft = (int)(remainMs / 3600000L % 24L);
      String timeStr;
      if (daysLeft > 0) {
         timeStr = "§7" + daysLeft + "d " + hoursLeft + "h left";
      } else if (hoursLeft > 0) {
         timeStr = "§e" + hoursLeft + "h left";
      } else if (ShopClientData.bpSeasonEndTime > 0L) {
         timeStr = "§c§lEnding!";
      } else {
         timeStr = "§8No season";
      }

      int timeW = this.fontRenderer.getStringWidth(timeStr);
      this.fontRenderer.drawString(timeStr, panelX + panelW - timeW - 4, headerY + 2, -7700886);
      int barX = panelX + 4;
      int barY = headerY + 10;
      int xpNeeded = ShopClientData.bpXPForNextLevel > 0 ? ShopClientData.bpXPForNextLevel : 1;
      float xpPct = Math.min(1.0F, (float)ShopClientData.bpCurrentXP / (float)xpNeeded);
      String levelLabel = "§eLv " + ShopClientData.bpCurrentTier + "/20";
      int levelLabelW = this.fontRenderer.getStringWidth(levelLabel);
      this.fontRenderer.drawStringWithShadow(levelLabel, (float)barX, (float)barY, -1521552);
      int xpBarX = barX + levelLabelW + 4;
      int xpBarW = 160;
      int xpBarH = 6;
      int xpBarMidY = barY + 2;
      drawRect(xpBarX, xpBarMidY, xpBarX + xpBarW, xpBarMidY + xpBarH, -15066598);
      this.drawHollowRect(xpBarX, xpBarMidY, xpBarW, xpBarH, -11908534);
      int fillW = (int)((float)(xpBarW - 2) * xpPct);
      if (fillW > 0) {
         int fillColor = isPurchased ? -2258689 : -9782678;
         drawRect(xpBarX + 1, xpBarMidY + 1, xpBarX + 1 + fillW, xpBarMidY + xpBarH - 1, fillColor);
      }

      String xpNums = "§7" + NUMBER_FORMAT.format((long)ShopClientData.bpCurrentXP) + "/" + NUMBER_FORMAT.format((long)xpNeeded) + " XP";
      this.fontRenderer.drawString(xpNums, xpBarX + xpBarW + 4, barY, -7700886);
      this.drawHorizontalLine(panelX + 2, panelX + panelW - 2, this.guiTop + 40, -9807296);
      int tierListStartY;
      if (!isPurchased) {
         int pitchY = this.guiTop + 66;
         String pitch = "§8Exclusive jutsu + kill effect + 13 crate rolls + bonus XP";
         int pitchW = this.fontRenderer.getStringWidth(pitch);
         this.fontRenderer.drawString(pitch, this.guiLeft + (activeW - pitchW) / 2, pitchY, -7700886);
         String bonus = "§8Otsutsuki: 1.5x XP";
         int bonusW = this.fontRenderer.getStringWidth(bonus);
         this.fontRenderer.drawString(bonus, this.guiLeft + (activeW - bonusW) / 2, pitchY + 11, -7700886);
         this.drawHorizontalLine(panelX + 2, panelX + panelW - 2, this.guiTop + 80, -9807296);
         tierListStartY = this.guiTop + 82;
      } else {
         tierListStartY = this.guiTop + 50;
      }

      if (!this.killEffectsOpen) {
         int tierNumW = 22;
         int freeColX = panelX + tierNumW + 4;
         int sepX = panelX + panelW / 2 + 4;
         int premColX = sepX + 8;
         int freeColW = sepX - freeColX - 4;
         int premColW = panelX + panelW - premColX - 4;
         this.drawGradientRect(panelX + 2, tierListStartY, sepX, tierListStartY + 14, 807570210, 354585378);
         this.drawGradientRect(sepX + 2, tierListStartY, panelX + panelW - 2, tierListStartY + 14, isPurchased ? 814228104 : 407127108, isPurchased ? 361243272 : 138691652);
         this.fontRenderer.drawStringWithShadow("§8Tier", (float)(panelX + 4), (float)(tierListStartY + 3), -7700886);
         this.fontRenderer.drawStringWithShadow("§a§lFree", (float)freeColX, (float)(tierListStartY + 3), -11141291);
         String premLabel = isPurchased ? "§d§lPremium" : "§8Premium §d(Locked)";
         this.fontRenderer.drawStringWithShadow(premLabel, (float)premColX, (float)(tierListStartY + 3), isPurchased ? -2258689 : -7700886);
         this.drawHorizontalLine(panelX + 2, panelX + panelW - 2, tierListStartY + 15, -9807296);
         int listY = tierListStartY + 18;
         int tierH = 18;
         int listEndY = this.guiTop + activeH - 40;
         int listH = listEndY - listY;
         int maxVisible = listH / tierH;
         if (maxVisible > 20) {
            maxVisible = 20;
         }

         if (maxVisible < 1) {
            maxVisible = 1;
         }

         this.drawGradientRect(panelX + 2, listY, panelX + panelW - 2, listY + maxVisible * tierH, -868600792, -870442476);
         this.drawHollowRect(panelX + 2, listY, panelW - 4, maxVisible * tierH, -9809872);
         drawRect(sepX, listY, sepX + 1, listY + maxVisible * tierH, -9807296);
         int maxScroll = Math.max(0, 20 - maxVisible);
         if (this.passScroll > maxScroll) {
            this.passScroll = maxScroll;
         }

         if (this.passScroll < 0) {
            this.passScroll = 0;
         }

         for(int i = 0; i < Math.min(maxVisible, 20 - this.passScroll); ++i) {
            int tier = i + this.passScroll + 1;
            int entryY = listY + i * tierH;
            boolean isCurrent = tier == ShopClientData.bpCurrentTier;
            boolean isCompleted = ShopClientData.bpClaimedTiers.contains(tier);
            boolean isReached = tier <= ShopClientData.bpCurrentTier;
            boolean isFuture = tier > ShopClientData.bpCurrentTier;
            if (i % 2 == 1) {
               drawRect(panelX + 3, entryY, panelX + panelW - 3, entryY + tierH, 285212671);
            }

            if (isCurrent) {
               drawRect(panelX + 3, entryY, panelX + panelW - 3, entryY + tierH, 637523712);
               drawRect(panelX + 2, entryY, panelX + 4, entryY + tierH, -10496);
            }

            if (tier > 1 && (tier - 1) % 5 == 0) {
               this.drawHorizontalLine(panelX + 3, panelX + panelW - 3, entryY, 1090508544);
            }

            int textY = entryY + (tierH - 8) / 2;
            String tierNum;
            int tierNumColor;
            if (isCompleted) {
               tierNum = "✔";
               tierNumColor = -11141291;
            } else if (isCurrent) {
               tierNum = "§l" + tier;
               tierNumColor = -10496;
            } else {
               tierNum = "" + tier;
               tierNumColor = isFuture ? -7700886 : -2832216;
            }

            this.fontRenderer.drawStringWithShadow(tierNum, (float)(panelX + 6), (float)textY, tierNumColor);
            int freeIdx = tier - 1;
            String freeReward = freeIdx < FREE_TRACK.length ? FREE_TRACK[freeIdx] : "???";
            int freeColor;
            if (isCompleted) {
               freeColor = -7700886;
            } else if (isCurrent) {
               freeColor = -7798904;
            } else if (isFuture) {
               freeColor = -8753056;
            } else {
               freeColor = -11141291;
            }

            String truncFree = this.fontRenderer.trimStringToWidth(freeReward, freeColW);
            this.fontRenderer.drawString(truncFree, freeColX, textY, freeColor);
            int premIdx = tier - 1;
            String premReward = premIdx < PREMIUM_TRACK.length ? PREMIUM_TRACK[premIdx] : "???";
            if (tier == 15) {
               String tag = "§6§l>> KILL EFFECT <<";
               if (!isPurchased) {
                  tag = "§8§l>> KILL EFFECT <<";
               }

               this.fontRenderer.drawStringWithShadow(tag, (float)premColX, (float)textY, isPurchased ? -22016 : -11184811);
            } else if (tier == 20) {
               long pulse = System.currentTimeMillis() % 2000L;
               int r = (int)((double)200.0F + (double)55.0F * Math.sin((double)pulse * Math.PI * (double)2.0F / (double)2000.0F));
               int g = (int)((double)150.0F + (double)50.0F * Math.sin((double)pulse * Math.PI * (double)2.0F / (double)2000.0F + (double)1.0F));
               int tagColor = isPurchased ? -16777216 | r << 16 | g << 8 : -11184811;
               String tag = "§l>> EXCLUSIVE JUTSU <<";
               this.fontRenderer.drawStringWithShadow(tag, (float)premColX, (float)textY, tagColor);
            } else {
               int premColor;
               if (!isPurchased) {
                  premColor = -11184811;
               } else if (isCompleted) {
                  premColor = -7700886;
               } else if (isCurrent) {
                  premColor = -1140225;
               } else if (isFuture) {
                  premColor = -8757126;
               } else {
                  premColor = -2258689;
               }

               String truncPrem = this.fontRenderer.trimStringToWidth(premReward, premColW);
               this.fontRenderer.drawString(truncPrem, premColX, textY, premColor);
            }
         }

         if (this.passScroll > 0) {
            String upArrow = "▲ Scroll up";
            int arrowW = this.fontRenderer.getStringWidth(upArrow);
            this.fontRenderer.drawString(upArrow, panelX + panelW - arrowW - 4, listY - 12, -7700886);
         }

         if (!isPurchased) {
            int overlayX = sepX + 2;
            int overlayW = panelX + panelW - 2 - overlayX;
            int overlayBotY = listY + maxVisible * tierH;
            drawRect(overlayX, listY, overlayX + overlayW, overlayBotY, -1879048192);
            int overlayMidY = (listY + overlayBotY) / 2;
            long pulse = System.currentTimeMillis() % 1500L;
            int alpha = (int)((double)180.0F + (double)75.0F * Math.sin((double)pulse * Math.PI * (double)2.0F / (double)1500.0F));
            int bannerColor = alpha << 24 | 6953578;
            int bannerH = 18;
            drawRect(overlayX + 4, overlayMidY - bannerH / 2, overlayX + overlayW - 4, overlayMidY + bannerH / 2, bannerColor);
            this.drawHollowRect(overlayX + 4, overlayMidY - bannerH / 2, overlayW - 8, bannerH, -2258689);
            String unlockText = "§d§lBuy Kage or Otsutsuki Pass";
            int unlockW = this.fontRenderer.getStringWidth(unlockText);
            this.fontRenderer.drawStringWithShadow(unlockText, (float)(overlayX + (overlayW - unlockW) / 2), (float)(overlayMidY - 4), -30465);

            for(int i = 0; i < Math.min(maxVisible, 20 - this.passScroll); ++i) {
               int tier = i + this.passScroll + 1;
               if (tier == 15 || tier == 20) {
                  int entryY = listY + i * tierH;
                  int textY = entryY + (tierH - 8) / 2;
                  if (tier == 15) {
                     String tag = "§8§l>> KILL EFFECT <<";
                     this.fontRenderer.drawStringWithShadow(tag, (float)premColX, (float)textY, -22016);
                  } else {
                     long p2 = System.currentTimeMillis() % 2000L;
                     int r2 = (int)((double)200.0F + (double)55.0F * Math.sin((double)p2 * Math.PI * (double)2.0F / (double)2000.0F));
                     int g2 = (int)((double)150.0F + (double)50.0F * Math.sin((double)p2 * Math.PI * (double)2.0F / (double)2000.0F + (double)1.0F));
                     int tagColor2 = -16777216 | r2 << 16 | g2 << 8;
                     String tag = "§l>> EXCLUSIVE JUTSU <<";
                     this.fontRenderer.drawStringWithShadow(tag, (float)premColX, (float)textY, tagColor2);
                  }
               }
            }
         }
      }

      if (this.killEffectsOpen && isPurchased) {
         int panelTopY = this.guiTop + 50;
         int panelBotY = this.guiTop + activeH - 40;
         this.drawGradientRect(panelX + 2, panelTopY, panelX + panelW - 2, panelBotY, -265279456, -266726384);
         this.drawHollowRect(panelX + 2, panelTopY, panelW - 4, panelBotY - panelTopY, -9809872);
         this.fontRenderer.drawStringWithShadow("§6§lKill Effects", (float)(panelX + 8), (float)(panelTopY + 4), -1521552);
         if (ShopClientData.bpOwnedKillEffects.isEmpty()) {
            String noEffects = "§7Complete Battle Pass Tier 15 to unlock a Kill Effect!";
            int noW = this.fontRenderer.getStringWidth(noEffects);
            int centerX = panelX + (panelW - noW) / 2;
            int centerY = panelTopY + (panelBotY - panelTopY) / 2 - 4;
            this.fontRenderer.drawStringWithShadow(noEffects, (float)centerX, (float)centerY, -2832216);
         } else {
            int effectY = panelTopY + 22;
            int effectSpacing = 30;
            int effectIdx = 0;

            for(String effectId : ShopClientData.bpOwnedKillEffects) {
               int ey = effectY + effectIdx * effectSpacing;
               String desc = getKillEffectDescription(effectId);
               this.fontRenderer.drawString("§8" + desc, panelX + 12, ey + 18, -7700886);
               ++effectIdx;
               if (effectIdx >= 10) {
                  break;
               }
            }
         }
      }

      this.drawHorizontalLine(panelX + 2, panelX + panelW - 2, this.guiTop + activeH - 36, -9807296);
   }

   private void drawCrystalShop(int mouseX, int mouseY) {
      int activeW = this.getActiveWidth();
      int activeH = this.getActiveHeight();
      int pad = 8;
      int panelX = this.guiLeft + pad;
      int panelW = activeW - pad * 2;
      int headerY = this.guiTop + 22;
      this.drawGradientRect(panelX, headerY, panelX + panelW, headerY + 18, 1080435302, 541331780);
      String crystalStr = NUMBER_FORMAT.format((long)ShopClientData.crystalBalance);
      String balLabel = "§d§l" + crystalStr + " §5Chakra Crystals";
      this.fontRenderer.drawStringWithShadow(balLabel, (float)(panelX + 4), (float)(headerY + 5), -2258689);
      if (ShopClientData.firstPurchaseBonusAvailable) {
         String badge = "§e§l2x CC First Purchase!";
         int badgeW = this.fontRenderer.getStringWidth(badge);
         long pulse = System.currentTimeMillis() % 1500L;
         int alpha = (int)((double)40.0F + (double)30.0F * Math.sin((double)pulse * Math.PI * (double)2.0F / (double)1500.0F));
         drawRect(panelX + panelW - badgeW - 10, headerY + 2, panelX + panelW - 2, headerY + 14, alpha << 24 | 16755200);
         this.fontRenderer.drawStringWithShadow(badge, (float)(panelX + panelW - badgeW - 6), (float)(headerY + 4), -8892);
      }

      this.drawHorizontalLine(panelX, panelX + panelW, this.guiTop + 40, -9811350);
      List<ShopClientData.CrateClientInfo> crates = ShopClientData.getCratesByCategory(ShopCategory.CASH);
      int cardH = 26;
      int cardGap = 4;
      int listY = this.guiTop + 42;
      int cardW = 180;
      int maxVisible = 7;
      int leftPanelH = maxVisible * (cardH + cardGap);
      this.drawGradientRect(panelX, listY, panelX + cardW, listY + leftPanelH, 809508928, 538972192);
      this.drawHollowRect(panelX, listY, cardW, leftPanelH, -10864038);
      if (crates.isEmpty()) {
         this.fontRenderer.drawString("§d§lComing Soon!", panelX + 10, listY + 20, -2258689);
      }

      int detailX = panelX + cardW + 10;
      int detailW = panelW - cardW - 10;
      int detailY = this.guiTop + 42;
      int detailH = this.historyOpen ? activeH - 102 : activeH - 50;
      this.drawGradientRect(detailX, detailY, detailX + detailW, detailY + detailH, 809508928, 538972192);
      this.drawHollowRect(detailX, detailY, detailW, detailH, -10864038);
      ShopClientData.CrateClientInfo selCrate = this.selectedCrateId != null ? ShopClientData.getCrateById(this.selectedCrateId) : null;
      if (selCrate == null) {
         String hint = "§dSelect a crate";
         int hintW = this.fontRenderer.getStringWidth(hint);
         this.fontRenderer.drawStringWithShadow(hint, (float)(detailX + (detailW - hintW) / 2), (float)(detailY + detailH / 2 - 4), -5609814);
      } else {
         this.fontRenderer.drawStringWithShadow("§d§l" + selCrate.displayName, (float)(detailX + 4), (float)(detailY + 4), -2258689);
         int descEndY = detailY + 16;
         if (selCrate.description != null && !selCrate.description.isEmpty()) {
            List<String> descLines = this.fontRenderer.listFormattedStringToWidth(selCrate.description, detailW - 10);

            for(int dl = 0; dl < Math.min(2, descLines.size()); ++dl) {
               this.fontRenderer.drawString((String)descLines.get(dl), detailX + 4, detailY + 16 + dl * 10, -7700886);
            }

            descEndY = detailY + 16 + Math.min(2, descLines.size()) * 10;
         }

         int priceY = descEndY + 2;
         String priceLabel = "Price: " + NUMBER_FORMAT.format((long)selCrate.crystalPrice) + " CC";
         boolean afford = ShopClientData.crystalBalance >= selCrate.crystalPrice;
         this.fontRenderer.drawStringWithShadow(priceLabel, (float)(detailX + 4), (float)priceY, afford ? -2258689 : -43691);
         if (!selCrate.directPurchase && (ShopClientData.pendingResult == null || ShopClientData.pendingResult.consumed)) {
            int pityCount = ShopClientData.getPityCount(selCrate.crateId);
            String pityLabel = "Pity: " + pityCount;
            int pityLW = this.fontRenderer.getStringWidth(pityLabel);
            this.fontRenderer.drawString(pityLabel, detailX + detailW - pityLW - 4, priceY, -5609763);
         }

         this.drawHorizontalLine(detailX + 4, detailX + detailW - 4, priceY + 10, -10864038);
         this.fontRenderer.drawStringWithShadow("§d§lLoot Table:", (float)(detailX + 4), (float)(priceY + 14), -2258689);
         List<ShopClientData.LootEntryClientInfo> sortedEntries = new ArrayList(selCrate.lootEntries);
         Collections.sort(sortedEntries, new Comparator<ShopClientData.LootEntryClientInfo>() {
            public int compare(ShopClientData.LootEntryClientInfo a, ShopClientData.LootEntryClientInfo b) {
               return Integer.compare(b.rarityOrdinal, a.rarityOrdinal);
            }
         });
         int lootY = priceY + 26;
         int lineH = 18;
         int maxLootVisible = (detailH - 100) / lineH;
         int totalEntries = sortedEntries.size();
         int maxScrollOffset = Math.max(0, totalEntries - maxLootVisible);
         if (this.lootScrollOffset > maxScrollOffset) {
            this.lootScrollOffset = maxScrollOffset;
         }

         if (this.lootScrollOffset < 0) {
            this.lootScrollOffset = 0;
         }

         if (this.lootScrollOffset > 0) {
            this.fontRenderer.drawString("▲", detailX + detailW - 10, lootY - 10, -5609814);
         }

         for(int i = 0; i < Math.min(maxLootVisible, totalEntries - this.lootScrollOffset); ++i) {
            int idx = i + this.lootScrollOffset;
            if (idx >= totalEntries) {
               break;
            }

            ShopClientData.LootEntryClientInfo entry = (ShopClientData.LootEntryClientInfo)sortedEntries.get(idx);
            int rarityColor = getRarityColor(entry.rarityOrdinal);
            int entryY = lootY + i * lineH;
            ItemStack stack = resolveItemStack(entry.itemId, entry.itemMeta, entry.itemCount, entry.itemNbt);
            if (!stack.isEmpty()) {
               GlStateManager.enableDepth();
               RenderHelper.enableGUIStandardItemLighting();
               this.mc.getRenderItem().renderItemIntoGUI(stack, detailX + 4, entryY);
               RenderHelper.disableStandardItemLighting();
               GlStateManager.disableDepth();
            }

            String itemName = this.getEntryDisplayName(entry);
            String entryText = this.fontRenderer.trimStringToWidth(itemName, detailW - 70);
            this.fontRenderer.drawString(entryText, detailX + 24, entryY + 4, rarityColor);
            String rarityName = ItemRarity.fromOrdinal(entry.rarityOrdinal).displayName;
            int nameW = this.fontRenderer.getStringWidth(rarityName);
            this.fontRenderer.drawString(rarityName, detailX + detailW - nameW - 6, entryY + 4, rarityColor);
         }

         if (this.lootScrollOffset + maxLootVisible < totalEntries) {
            int indicatorY = lootY + maxLootVisible * lineH;
            this.fontRenderer.drawString("▼", detailX + detailW - 10, indicatorY, -5609814);
         }
      }

      if (selCrate != null) {
         int pityY = listY + Math.min(maxVisible, crates.size()) * (cardH + cardGap) + 8;
         this.drawPityDisplay(panelX + 2, pityY, cardW - 4, selCrate.crateId);
      }

      if (this.historyOpen) {
         this.drawHistoryPanel(mouseX, mouseY);
      }

   }

   private String getCrateRarityHint(ShopClientData.CrateClientInfo crate) {
      boolean hasS5 = false;
      boolean hasS4 = false;
      boolean hasA3 = false;
      boolean hasB2 = false;

      for(ShopClientData.LootEntryClientInfo e : crate.lootEntries) {
         if (e.rarityOrdinal >= 5) {
            hasS5 = true;
         } else if (e.rarityOrdinal >= 4) {
            hasS4 = true;
         } else if (e.rarityOrdinal >= 3) {
            hasA3 = true;
         } else if (e.rarityOrdinal >= 2) {
            hasB2 = true;
         }
      }

      StringBuilder sb = new StringBuilder("§8");
      if (hasS5) {
         sb.append("§cS+ ");
      }

      if (hasS4) {
         sb.append("§6S ");
      }

      if (hasA3) {
         sb.append("§5A ");
      }

      if (hasB2) {
         sb.append("§9B ");
      }

      sb.append("§8...");
      return sb.toString();
   }

   private void drawCrystalCardText() {
      List<ShopClientData.CrateClientInfo> crates = ShopClientData.getCratesByCategory(ShopCategory.CASH);
      if (!crates.isEmpty()) {
         int pad = 8;
         int listX = this.guiLeft + pad;
         int listY = this.guiTop + 42;
         int cardH = 26;
         int cardGap = 4;
         int cardW = 180;
         int maxVisible = 7;

         for(int i = 0; i < Math.min(maxVisible, crates.size() - this.crateListScroll); ++i) {
            int idx = i + this.crateListScroll;
            if (idx >= crates.size()) {
               break;
            }

            ShopClientData.CrateClientInfo crate = (ShopClientData.CrateClientInfo)crates.get(idx);
            boolean sel = crate.crateId.equals(this.selectedCrateId);
            int cardY = listY + i * (cardH + cardGap);
            if (sel) {
               this.drawHollowRect(listX, cardY, cardW, cardH, -2258689);
            }

            int nameColor = sel ? -1135873 : -2832216;
            String name = this.fontRenderer.trimStringToWidth(crate.displayName, cardW - 70);
            this.fontRenderer.drawStringWithShadow(name, (float)(listX + 4), (float)(cardY + 8), nameColor);
            boolean isFreeRoll = "cash_clan".equals(crate.crateId) && ShopClientData.freeRollAvailable || "cash_so6p".equals(crate.crateId) && ShopClientData.weekendPromoSO6PAvailable;
            String price;
            int priceColor;
            if (isFreeRoll) {
               price = "FREE!";
               long p = System.currentTimeMillis() % 1000L;
               int glow = (int)((double)155.0F + (double)100.0F * Math.sin((double)p * Math.PI * (double)2.0F / (double)1000.0F));
               priceColor = -16777216 | glow << 16 | glow << 8;
            } else {
               price = NUMBER_FORMAT.format((long)crate.crystalPrice) + " CC";
               priceColor = ShopClientData.crystalBalance >= crate.crystalPrice ? -2258689 : -43691;
            }

            int priceW = this.fontRenderer.getStringWidth(price);
            this.fontRenderer.drawStringWithShadow(price, (float)(listX + cardW - priceW - 4), (float)(cardY + 8), priceColor);
         }

      }
   }

   private void drawTokenShop(int mouseX, int mouseY) {
      if (this.tokenShopItems == null) {
         this.tokenShopItems = this.buildTokenShopItems();
      }

      this.drawHorizontalLine(this.guiLeft + 6, this.guiLeft + 400 - 6, this.guiTop + 38, -9807296);
      int panelX = this.guiLeft + 6;
      int panelY = this.guiTop + 40;
      int panelW = 388;
      int panelH = 254;
      this.drawGradientRect(panelX, panelY, panelX + panelW, panelY + panelH, -868600792, -870442476);
      this.drawHollowRect(panelX, panelY, panelW, panelH, -9809872);
      if (this.isSelectedCategoryLocked()) {
         String lockMsg = this.selectedCategory == ShopCategory.KEKKEI_GENKAI ? "Complete Your Shinobi Way!" : "Complete the Konoha Crush!";
         int msgW = this.fontRenderer.getStringWidth(lockMsg);
         this.fontRenderer.drawStringWithShadow(lockMsg, (float)(panelX + (panelW - msgW) / 2), (float)(panelY + panelH / 2 - 4), -22016);
      } else {
         this.fontRenderer.drawStringWithShadow("§l" + this.selectedCategory.displayName + " - Token Shop", (float)(panelX + 6), (float)(panelY + 4), -1521552);
         String tokenBal = "§dTokens: " + NUMBER_FORMAT.format((long)ShopClientData.tokenBalance);
         int balW = this.fontRenderer.getStringWidth(tokenBal);
         this.fontRenderer.drawStringWithShadow(tokenBal, (float)(panelX + panelW - balW - 6), (float)(panelY + 4), -2832216);
         this.drawHorizontalLine(panelX + 4, panelX + panelW - 4, panelY + 16, -9807296);
         List<TokenShopItem> filtered = this.getFilteredTokenItems();
         int listY = panelY + 20;
         int lineH = 20;
         int maxVisible = (panelH - 28) / lineH;
         int total = filtered.size();
         int maxScroll = Math.max(0, total - maxVisible);
         if (this.tokenShopScroll > maxScroll) {
            this.tokenShopScroll = maxScroll;
         }

         if (this.tokenShopScroll < 0) {
            this.tokenShopScroll = 0;
         }

         if (total == 0) {
            this.fontRenderer.drawString("§e§lNo items in this category", panelX + 10, panelY + 24, -7700886);
         }

         Minecraft mc = Minecraft.getMinecraft();

         for(int i = 0; i < Math.min(maxVisible, total - this.tokenShopScroll); ++i) {
            int idx = i + this.tokenShopScroll;
            if (idx >= total) {
               break;
            }

            TokenShopItem item = (TokenShopItem)filtered.get(idx);
            int entryY = listY + i * lineH;
            int rarityColor = getRarityColor(item.rarityOrdinal);
            ItemStack stack = resolveItemStack(item.itemId, item.itemMeta, item.itemCount, item.itemNbt);
            if (!stack.isEmpty()) {
               GlStateManager.enableDepth();
               RenderHelper.enableGUIStandardItemLighting();
               mc.getRenderItem().renderItemIntoGUI(stack, panelX + 4, entryY);
               RenderHelper.disableStandardItemLighting();
               GlStateManager.disableDepth();
            }

            String dispName = this.fontRenderer.trimStringToWidth(item.displayName, panelW - 160);
            this.fontRenderer.drawString(dispName, panelX + 24, entryY + 4, rarityColor);
            String costStr = item.tokenCost + " tokens";
            boolean canBuy = ShopClientData.tokenBalance >= item.tokenCost;
            int costColor = canBuy ? -11141291 : -43691;
            int costW = this.fontRenderer.getStringWidth(costStr);
            this.fontRenderer.drawString(costStr, panelX + panelW - costW - 50, entryY + 4, costColor);
         }

         if (this.tokenShopScroll > 0) {
            this.fontRenderer.drawString("▲", panelX + panelW - 12, listY, -7700886);
         }

         if (this.tokenShopScroll + maxVisible < total) {
            this.fontRenderer.drawString("▼", panelX + panelW - 12, listY + maxVisible * lineH, -7700886);
         }

      }
   }

   private List<BuildingShopRegistry.BuildingItem> getFilteredBuildingItems() {
      BuildingShopRegistry.BuildingCategory cat = this.getBuildingCategoryForSubTab();
      List<BuildingShopRegistry.BuildingItem> all = BuildingShopRegistry.getItems(cat);
      if (this.buildingSearchText != null && !this.buildingSearchText.isEmpty()) {
         String search = this.buildingSearchText.toLowerCase();
         List<BuildingShopRegistry.BuildingItem> filtered = new ArrayList();

         for(BuildingShopRegistry.BuildingItem item : all) {
            if (item.displayName.toLowerCase().contains(search)) {
               filtered.add(item);
            }
         }

         return filtered;
      } else {
         return all;
      }
   }

   private BuildingShopRegistry.BuildingCategory getBuildingCategoryForSubTab() {
      switch (this.buildingSubTab) {
         case 0:
            return BuildingShopRegistry.BuildingCategory.BUILDING_BLOCKS;
         case 1:
            return BuildingShopRegistry.BuildingCategory.DECORATIONS;
         case 2:
            return BuildingShopRegistry.BuildingCategory.MISCELLANEOUS;
         case 3:
            return BuildingShopRegistry.BuildingCategory.ENTRYWAY;
         case 4:
            return BuildingShopRegistry.BuildingCategory.ADDITIONAL_LANTERNS;
         case 5:
            return BuildingShopRegistry.BuildingCategory.MACAW_FURNITURE;
         case 6:
            return BuildingShopRegistry.BuildingCategory.VARIED_COMMODITIES;
         case 7:
            return BuildingShopRegistry.BuildingCategory.TOOLS;
         default:
            return BuildingShopRegistry.BuildingCategory.BUILDING_BLOCKS;
      }
   }

   private void buildBuildingButtons() {
      int subTabY = this.guiTop + 40;
      int subTabW = 46;
      int subSpacing = 1;
      int subStartX = this.guiLeft + 6;

      for(int i = 0; i < BUILDING_SUB_TAB_NAMES.length; ++i) {
         int topC = this.buildingSubTab == i ? -9807312 : -12962776;
         int botC = this.buildingSubTab == i ? -11912678 : -14804460;
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(800 + i, subStartX + i * (subTabW + subSpacing), subTabY, subTabW, 12, BUILDING_SUB_TAB_NAMES[i], topC, botC));
      }

      if (this.buildingSearchField == null) {
         this.buildingSearchField = new GuiTextField(999, this.fontRenderer, this.guiLeft + 50, this.guiTop + 54, 130, 12);
         this.buildingSearchField.setMaxStringLength(30);
         this.buildingSearchField.setEnableBackgroundDrawing(true);
         this.buildingSearchField.setTextColor(-1);
      }

      this.buildingSearchField.x = this.guiLeft + 50;
      this.buildingSearchField.y = this.guiTop + 54;
      List<BuildingShopRegistry.BuildingItem> items = this.getFilteredBuildingItems();
      if (items != null) {
         int gridY = this.guiTop + 70;
         int gridH = 188;
         int rowH = 22;
         int maxVisible = gridH / rowH;
         int gridPanelW = 258;

         for(int i = 0; i < Math.min(maxVisible, items.size() - this.buildingGridScroll); ++i) {
            int idx = i + this.buildingGridScroll;
            if (idx >= items.size()) {
               break;
            }

            int rowY = gridY + i * rowH;
            int btnX = this.guiLeft + 170;
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(860 + i, btnX, rowY + 1, 14, 16, "-", -9818064, -11920870));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(810 + i, btnX + 16, rowY + 1, 14, 16, "+", -13604304, -15054310));
            this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(910 + i, btnX + 32, rowY + 1, 22, 16, "+64", -13608342, -15058358));
            if (this.buildingSubTab != 7) {
               this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(1010 + i, btnX + 56, rowY + 1, 22, 16, "§dBox", -10864022, -12969398));
            }
         }

         boolean canPurchase = !BuildingCartData.isEmpty() && ShopClientData.ryoBalance >= BuildingCartData.getTotalCost();
         int purchTopC = canPurchase ? -12948934 : -12962776;
         int purchBotC = canPurchase ? -15054310 : -14804460;
         GuiRankedMenu.GuiButtonGradient purchBtn = new GuiRankedMenu.GuiButtonGradient(960, this.guiLeft + 268, this.guiTop + 300 - 42, 120, 18, "Purchase", purchTopC, purchBotC);
         purchBtn.enabled = canPurchase;
         this.buttonList.add(purchBtn);
         this.buttonList.add(new GuiRankedMenu.GuiButtonGradient(3, this.guiLeft + 268, this.guiTop + 300 - 22, 56, 16, this.historyOpen ? "Hide" : "History", -11912672, -14018032));
      }
   }

   private void drawBuildingShop(int mouseX, int mouseY) {
      this.drawHorizontalLine(this.guiLeft + 6, this.guiLeft + 400 - 6, this.guiTop + 54, -9807296);
      this.fontRenderer.drawString("§7Search:", this.guiLeft + 8, this.guiTop + 57, -7700886);
      if (this.buildingSearchField != null) {
         this.buildingSearchField.drawTextBox();
      }

      int gridX = this.guiLeft + 6;
      int gridY = this.guiTop + 70;
      int gridW = 256;
      int gridH = 188;
      int rowH = 22;
      int maxVisible = gridH / rowH;
      this.drawGradientRect(gridX, gridY, gridX + gridW, gridY + gridH, -868600792, -870442476);
      this.drawHollowRect(gridX, gridY, gridW, gridH, -9809872);
      List<BuildingShopRegistry.BuildingItem> items = this.getFilteredBuildingItems();
      if (items != null && !items.isEmpty()) {
         int total = items.size();

         for(int i = 0; i < Math.min(maxVisible, total - this.buildingGridScroll); ++i) {
            int idx = i + this.buildingGridScroll;
            if (idx >= total) {
               break;
            }

            BuildingShopRegistry.BuildingItem item = (BuildingShopRegistry.BuildingItem)items.get(idx);
            int rowY = gridY + i * rowH;
            if (i % 2 == 0) {
               drawRect(gridX + 1, rowY, gridX + gridW - 1, rowY + rowH, 553648127);
            }

            ItemStack stack = resolveShopItemStack(item.registryName, item.meta);
            if (!stack.isEmpty()) {
               GlStateManager.enableDepth();
               RenderHelper.enableGUIStandardItemLighting();
               this.mc.getRenderItem().renderItemIntoGUI(stack, gridX + 3, rowY + 2);
               RenderHelper.disableStandardItemLighting();
               GlStateManager.disableDepth();
            }

            String name = this.fontRenderer.trimStringToWidth(item.displayName, 130);
            this.fontRenderer.drawString(name, gridX + 22, rowY + 3, -2832216);
            String priceStr = "§6¥" + item.ryoPrice;
            this.fontRenderer.drawString(priceStr, gridX + 22, rowY + 13, -7820732);
            int qty = BuildingCartData.getQuantity(item.registryName, item.meta);
            if (this.buildingQtyEditIndex == idx && this.buildingQtyField != null) {
               this.buildingQtyField.x = gridX + 65;
               this.buildingQtyField.y = rowY + 11;
               this.buildingQtyField.drawTextBox();
            } else if (qty > 0) {
               this.fontRenderer.drawString("§a[§f" + qty + "§a]", gridX + 65, rowY + 13, -11141291);
            } else {
               this.fontRenderer.drawString("§8[0]", gridX + 65, rowY + 13, -7700886);
            }
         }

         if (this.buildingGridScroll > 0) {
            this.fontRenderer.drawString("▲", gridX + gridW - 12, gridY + 2, -7700886);
         }

         if (this.buildingGridScroll + maxVisible < total) {
            this.fontRenderer.drawString("▼", gridX + gridW - 12, gridY + gridH - 10, -7700886);
         }
      } else {
         this.fontRenderer.drawString("§7No items available", gridX + 10, gridY + 20, -7700886);
      }

      int cartX = this.guiLeft + 266;
      int cartY = this.guiTop + 70;
      int cartW = 126;
      int cartH = 188;
      this.drawGradientRect(cartX, cartY, cartX + cartW, cartY + cartH, -869652448, -870705648);
      this.drawHollowRect(cartX, cartY, cartW, cartH, -9809872);
      String cartHeader = "§l§6Cart";
      if (!BuildingCartData.isEmpty()) {
         cartHeader = cartHeader + " §7(" + BuildingCartData.getEntryCount() + ")";
      }

      this.fontRenderer.drawString(cartHeader, cartX + 4, cartY + 3, -1521552);
      this.drawHorizontalLine(cartX + 2, cartX + cartW - 2, cartY + 13, -9807296);
      if (BuildingCartData.isEmpty()) {
         this.fontRenderer.drawString("§8Empty", cartX + 4, cartY + 18, -7700886);
      } else {
         int cartItemY = cartY + 16;
         int cartLineH = 20;
         int cartMaxVisible = (cartH - 50) / cartLineH;
         List<BuildingCartData.CartEntry> cartEntries = BuildingCartData.getEntries();

         for(int i = 0; i < Math.min(cartMaxVisible, cartEntries.size() - this.buildingCartScroll); ++i) {
            int idx = i + this.buildingCartScroll;
            if (idx >= cartEntries.size()) {
               break;
            }

            BuildingCartData.CartEntry entry = (BuildingCartData.CartEntry)cartEntries.get(idx);
            int lineY = cartItemY + i * cartLineH;
            String cartLine = this.fontRenderer.trimStringToWidth(entry.displayName, cartW - 10);
            this.fontRenderer.drawString(cartLine, cartX + 4, lineY, -2832216);
            String qtyPrice = " x" + entry.quantity + " §6¥" + NUMBER_FORMAT.format((long)entry.getTotalPrice());
            this.fontRenderer.drawString(qtyPrice, cartX + 4, lineY + 9, -7820732);
         }

         if (this.buildingCartScroll > 0) {
            this.fontRenderer.drawString("▲", cartX + cartW - 10, cartItemY, -7700886);
         }

         if (this.buildingCartScroll + cartMaxVisible < cartEntries.size()) {
            this.fontRenderer.drawString("▼", cartX + cartW - 10, cartItemY + cartMaxVisible * cartLineH, -7700886);
         }
      }

      long totalCost = BuildingCartData.getTotalCost();
      boolean canAfford = ShopClientData.ryoBalance >= totalCost;
      String totalStr = "Total: ¥" + NUMBER_FORMAT.format(totalCost);
      int totalColor = canAfford ? -11141291 : -43691;
      this.fontRenderer.drawString(totalStr, cartX + 4, cartY + cartH - 30, totalColor);
      String countStr = BuildingCartData.getTotalItemCount() + " items";
      this.fontRenderer.drawString(countStr, cartX + 4, cartY + cartH - 20, -7700886);
      if (this.historyOpen) {
         this.drawHistoryPanel(mouseX, mouseY);
      }

   }

   private void drawRedeemTab(int mouseX, int mouseY) {
      int subY = this.guiTop + 22;
      this.drawGradientRect(this.guiLeft + 6, subY, this.guiLeft + 80, subY + 14, -9807312, -11912678);
      this.fontRenderer.drawStringWithShadow("§fAkamichi", (float)(this.guiLeft + 14), (float)(subY + 3), -1521552);
      this.drawHorizontalLine(this.guiLeft + 6, this.guiLeft + 400 - 6, subY + 16, -9807296);
      boolean hasAkamichi = ShopClientData.akamichiUnlocked;
      if (!hasAkamichi) {
         String lockMsg = "Akamichi Clan Required";
         String lockMsg2 = "Unlock the Akamichi Clan to access this page.";
         int cx = this.guiLeft + 200;
         int cy = this.guiTop + 150 - 20;
         this.fontRenderer.drawString("§6§l" + lockMsg, cx - this.fontRenderer.getStringWidth(lockMsg) / 2, cy, -1521552);
         this.fontRenderer.drawString("§7" + lockMsg2, cx - this.fontRenderer.getStringWidth(lockMsg2) / 2, cy + 14, -7700886);
      } else {
         String desc = "§7Claim 4 Red Chakra Pills for your Akamichi techniques.";
         String desc2 = "§7You can reclaim as many times as needed, free of charge.";
         int cx = this.guiLeft + 200;
         this.fontRenderer.drawString(desc, cx - this.fontRenderer.getStringWidth(desc) / 2, this.guiTop + 80, -2832216);
         this.fontRenderer.drawString(desc2, cx - this.fontRenderer.getStringWidth(desc2) / 2, this.guiTop + 95, -7700886);
         ItemStack pillStack = resolveShopItemStack("kabutoaddon:red_chakra_pill", 0);
         if (!pillStack.isEmpty()) {
            GlStateManager.enableDepth();
            RenderHelper.enableGUIStandardItemLighting();
            this.mc.getRenderItem().renderItemIntoGUI(pillStack, cx - 8, this.guiTop + 120);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableDepth();
         }

         String pillLabel = "x4 Red Chakra Pill";
         this.fontRenderer.drawString("§c" + pillLabel, cx - this.fontRenderer.getStringWidth(pillLabel) / 2, this.guiTop + 140, -43691);
      }

   }

   static {
      NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);
      FREE_TRACK = new String[]{"500 Ryo", "3x Ration Pill", "1,000 Ryo", "Armor Crate I", "Jutsu XP Crate", "1,500 Ryo", "3x Gold Pill", "Jutsu XP Crate", "2,000 Ryo", "Armor Crate II", "2,500 Ryo", "2x Jutsu XP Crate", "3,000 Ryo", "Jutsu Crate I", "Armor Crate III", "3,500 Ryo", "3x Gold Pill", "2x Jutsu XP Crate", "4,000 Ryo", "5,000 Ryo + Armor III"};
      PREMIUM_TRACK = new String[]{"15 CC", "5,000 Ryo + Armor III", "2x Jutsu XP Crate", "10,000 Ryo + Jutsu II", "10 CC + Armor III", "5,000 Ryo + 1,000 NinjaXP", "Jutsu II + Armor III", "15,000 Ryo", "1,000 NinjaXP + Jutsu III", "15 CC + Armor IV", "20,000 Ryo + 2x XP Crate", "Jutsu III + Armor IV", "1,000 NinjaXP + 15,000 Ryo", "Weapon + Jutsu III", "10 CC + KILL EFFECT", "25,000 Ryo + Armor V", "2x XP Crate + Jutsu IV", "30,000 Ryo + Weapon", "30 CC + 20,000 Ryo", "EXCLUSIVE JUTSU"};
      BUILDING_SUB_TAB_NAMES = new String[]{"Blocks", "Decor", "Misc", "Entry", "Lamps", "Furn.", "VC", "Tools"};
      BUILDING_SUB_TAB_COLORS = new int[]{-5601212, -12277112, -7820732, -7846742, -5592508, -12285782, -5618552, -3381709};
   }

   private static class TokenShopItem {
      String itemId;
      int itemMeta;
      int itemCount;
      NBTTagCompound itemNbt;
      String displayName;
      int rarityOrdinal;
      int tokenCost;
      int categoryOrdinal;
      String purchaseKey;

      TokenShopItem(String itemId, int itemMeta, int itemCount, NBTTagCompound itemNbt, String displayName, int rarityOrdinal, int tokenCost, int categoryOrdinal) {
         this.itemId = itemId;
         this.itemMeta = itemMeta;
         this.itemCount = itemCount;
         this.itemNbt = itemNbt;
         this.displayName = displayName;
         this.rarityOrdinal = rarityOrdinal;
         this.tokenCost = tokenCost;
         this.categoryOrdinal = categoryOrdinal;
         this.purchaseKey = itemId + ":" + itemMeta;
      }
   }
}
