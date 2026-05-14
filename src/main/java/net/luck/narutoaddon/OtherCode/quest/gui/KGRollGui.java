
package net.luck.narutoaddon.OtherCode.quest.gui;

import net.luck.narutoaddon.OtherCode.quest.core.QuestModInit;
import net.luck.narutoaddon.OtherCode.quest.network.KGRollClaimMessage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

@SideOnly(Side.CLIENT)
public class KGRollGui extends GuiScreen {
   private static final int STRIP_ITEM_WIDTH = 80;
   private static final int STRIP_VISIBLE_ITEMS = 5;
   private static final int STRIP_HEIGHT = 70;
   private static final float ANIMATION_DURATION_MS = 5000.0F;
   private static final int TARGET_INDEX = 45;
   private static final int COLOR_COMMON = -11141291;
   private static final int COLOR_UNCOMMON = -11184641;
   private static final int COLOR_RARE = -5635926;
   private static final int COLOR_COMMON_BG = -15058406;
   private static final int COLOR_UNCOMMON_BG = -15066566;
   private static final int COLOR_RARE_BG = -12969414;
   private boolean animationActive = false;
   private long animationStartTime = 0L;
   private float stripOffset = 0.0F;
   private boolean animationDone = false;
   private int lastTickIndex = -1;
   private boolean claimed = false;
   private long revealTime = 0L;

   public void initGui() {
      super.initGui();
      if (KGRollClientData.pendingResult != null && !KGRollClientData.pendingResult.consumed) {
         this.animationActive = true;
         this.animationStartTime = System.currentTimeMillis();
         this.animationDone = false;
         this.lastTickIndex = -1;
         this.claimed = false;
      }

   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      drawRect(0, 0, this.width, this.height, -872415232);
      KGRollClientData.KGRollResult result = KGRollClientData.pendingResult;
      if (result != null && !result.consumed) {
         String title = "§d§lKekkei Genkai Awakening";
         int titleWidth = this.fontRenderer.getStringWidth(title);
         this.fontRenderer.drawStringWithShadow(title, (float)(this.width - titleWidth) / 2.0F, (float)this.height / 2.0F - 70.0F - 30.0F, -1);
         if (this.animationActive) {
            this.drawRollingAnimation(result);
         }

         super.drawScreen(mouseX, mouseY, partialTicks);
      } else {
         this.mc.displayGuiScreen((GuiScreen)null);
      }
   }

   private void drawRollingAnimation(KGRollClientData.KGRollResult result) {
      long elapsed = System.currentTimeMillis() - this.animationStartTime;
      float progress = Math.min(1.0F, (float)elapsed / 5000.0F);
      float eased = 1.0F - (1.0F - progress) * (1.0F - progress) * (1.0F - progress);
      float totalDistance = 3600.0F;
      this.stripOffset = eased * totalDistance;
      int centerItemIndex = (int)((this.stripOffset + 160.0F) / 80.0F);
      if (centerItemIndex != this.lastTickIndex) {
         this.lastTickIndex = centerItemIndex;
         if (this.mc.player != null) {
            this.mc.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.3F, 1.5F + progress * 0.5F);
         }
      }

      int stripTotalW = 400;
      int stripX = (this.width - stripTotalW) / 2;
      int stripY = this.height / 2 - 35;
      drawRect(stripX - 4, stripY - 4, stripX + stripTotalW + 4, stripY + 70 + 4, -5635926);
      drawRect(stripX - 2, stripY - 2, stripX + stripTotalW + 2, stripY + 70 + 2, -14540254);
      drawRect(stripX, stripY, stripX + stripTotalW, stripY + 70, -15066598);
      GL11.glEnable(3089);
      double scaleX = (double)this.mc.displayWidth / (double)this.width;
      double scaleY = (double)this.mc.displayHeight / (double)this.height;
      int scissorX = (int)((double)stripX * scaleX);
      int scissorY = (int)((double)(this.height - stripY - 70) * scaleY);
      int scissorW = (int)((double)stripTotalW * scaleX);
      int scissorH = (int)((double)70.0F * scaleY);
      GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
      List<KGRollClientData.StripEntry> strip = result.displayStrip;
      float centerOffset = 160.0F;

      for(int i = 0; i < strip.size(); ++i) {
         float itemXf = (float)(stripX + i * 80) - this.stripOffset + centerOffset;
         if (!(itemXf > (float)(stripX + stripTotalW + 80)) && !(itemXf < (float)(stripX - 80))) {
            int itemX = (int)itemXf;
            KGRollClientData.StripEntry entry = (KGRollClientData.StripEntry)strip.get(i);
            int bgColor;
            int borderColor;
            switch (entry.rarityIndex) {
               case 1:
                  bgColor = -15066566;
                  borderColor = -11184641;
                  break;
               case 2:
                  bgColor = -12969414;
                  borderColor = -5635926;
                  break;
               default:
                  bgColor = -15058406;
                  borderColor = -11141291;
            }

            drawRect(itemX + 2, stripY + 2, itemX + 80 - 2, stripY + 70 - 2, borderColor);
            drawRect(itemX + 3, stripY + 3, itemX + 80 - 3, stripY + 70 - 3, bgColor);
            ItemStack displayStack = resolveItemStack(entry.itemId);
            if (!displayStack.isEmpty()) {
               int iconX = itemX + 32;
               int iconY = stripY + 8;
               GlStateManager.enableDepth();
               RenderHelper.enableGUIStandardItemLighting();
               this.mc.getRenderItem().renderItemIntoGUI(displayStack, iconX, iconY);
               RenderHelper.disableStandardItemLighting();
               GlStateManager.disableDepth();
            }

            String name = entry.displayName;
            int nameWidth = this.fontRenderer.getStringWidth(name);
            int textX = itemX + (80 - nameWidth) / 2;
            this.fontRenderer.drawStringWithShadow(name, (float)textX, (float)(stripY + 30), -1);
            String rarityLabel = getRarityLabel(entry.rarityIndex);
            int rarityColor = getRarityColor(entry.rarityIndex);
            int rarityWidth = this.fontRenderer.getStringWidth(rarityLabel);
            this.fontRenderer.drawStringWithShadow(rarityLabel, (float)(itemX + (80 - rarityWidth) / 2), (float)(stripY + 44), rarityColor);
         }
      }

      GL11.glDisable(3089);
      int markerX = stripX + stripTotalW / 2;
      drawRect(markerX - 1, stripY - 8, markerX + 1, stripY, -22016);
      drawRect(markerX - 3, stripY - 10, markerX + 3, stripY - 8, -22016);
      drawRect(markerX - 1, stripY + 70, markerX + 1, stripY + 70 + 8, -22016);
      drawRect(markerX - 3, stripY + 70 + 8, markerX + 3, stripY + 70 + 10, -22016);
      if (progress >= 1.0F && !this.animationDone) {
         this.animationDone = true;
         this.revealTime = System.currentTimeMillis();
         if (this.mc.player != null) {
            if (result.wonRarity >= 2) {
               this.mc.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
            } else if (result.wonRarity >= 1) {
               this.mc.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            } else {
               this.mc.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.8F, 1.2F);
            }
         }
      }

      if (this.animationDone) {
         this.drawRevealPanel(result, stripX, stripY, stripTotalW);
      }

   }

   private void drawRevealPanel(KGRollClientData.KGRollResult result, int stripX, int stripY, int stripTotalW) {
      long sincReveal = System.currentTimeMillis() - this.revealTime;
      if (sincReveal < 500L) {
         int alpha = (int)(200.0F * (1.0F - (float)sincReveal / 500.0F));
         if (alpha > 0) {
            drawRect(0, 0, this.width, this.height, alpha << 24 | 16777215);
         }
      }

      int cardY = stripY + 70 + 20;
      int rarityColor = getRarityColor(result.wonRarity);
      ItemStack wonStack = resolveItemStack(result.wonItemId);
      if (!wonStack.isEmpty()) {
         GlStateManager.pushMatrix();
         int iconX = this.width / 2 - 16;
         GlStateManager.translate((float)iconX, (float)cardY, 0.0F);
         GlStateManager.scale(2.0F, 2.0F, 1.0F);
         GlStateManager.enableDepth();
         RenderHelper.enableGUIStandardItemLighting();
         this.mc.getRenderItem().renderItemIntoGUI(wonStack, 0, 0);
         RenderHelper.disableStandardItemLighting();
         GlStateManager.disableDepth();
         GlStateManager.popMatrix();
         cardY += 38;
      }

      String wonText = "§l" + result.wonDisplayName;
      int wonWidth = this.fontRenderer.getStringWidth(wonText);
      this.fontRenderer.drawStringWithShadow(wonText, (float)(this.width - wonWidth) / 2.0F, (float)cardY, rarityColor);
      String rarityLabel = "§o" + getRarityLabel(result.wonRarity);
      int rarityWidth = this.fontRenderer.getStringWidth(rarityLabel);
      this.fontRenderer.drawStringWithShadow(rarityLabel, (float)(this.width - rarityWidth) / 2.0F, (float)(cardY + 14), rarityColor);
      String awaken = "§d§lYou have awakened a new Kekkei Genkai!";
      int awakenWidth = this.fontRenderer.getStringWidth(awaken);
      this.fontRenderer.drawStringWithShadow(awaken, (float)(this.width - awakenWidth) / 2.0F, (float)(cardY + 32), -1);
      if (!this.claimed && sincReveal > 800L) {
         boolean blink = System.currentTimeMillis() / 500L % 2L == 0L;
         if (blink) {
            String clickText = "§e§l[ Click to Claim ]";
            int clickWidth = this.fontRenderer.getStringWidth(clickText);
            this.fontRenderer.drawStringWithShadow(clickText, (float)(this.width - clickWidth) / 2.0F, (float)(cardY + 52), -1);
         }
      }

   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      if (!this.animationActive || this.animationDone) {
         if (this.animationDone && !this.claimed && KGRollClientData.pendingResult != null) {
            this.claimed = true;
            KGRollClientData.pendingResult.consumed = true;
            QuestModInit.NETWORK.sendToServer(new KGRollClaimMessage());
            this.mc.displayGuiScreen((GuiScreen)null);
         }

      }
   }

   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (!this.animationActive || this.animationDone) {
         if (keyCode != 1 || !this.animationDone || this.claimed) {
            super.keyTyped(typedChar, keyCode);
         }
      }
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   private static String getRarityLabel(int rarityIndex) {
      switch (rarityIndex) {
         case 1:
            return "Uncommon";
         case 2:
            return "Rare";
         default:
            return "Common";
      }
   }

   private static int getRarityColor(int rarityIndex) {
      switch (rarityIndex) {
         case 1:
            return -11184641;
         case 2:
            return -5635926;
         default:
            return -11141291;
      }
   }

   private static ItemStack resolveItemStack(String itemId) {
      if (itemId != null && !itemId.isEmpty()) {
         Item item = Item.getByNameOrId(itemId);
         return item == null ? ItemStack.EMPTY : new ItemStack(item, 1);
      } else {
         return ItemStack.EMPTY;
      }
   }
}
