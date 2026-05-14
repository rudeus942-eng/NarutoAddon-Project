
package net.luck.narutoaddon.OtherCode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RankedInventoryOverlay extends Gui {
   private static RankedInventoryOverlay instance;
   private float animationTick = 0.0F;

   public static RankedInventoryOverlay getInstance() {
      if (instance == null) {
         instance = new RankedInventoryOverlay();
      }

      return instance;
   }

   public static void register() {
      MinecraftForge.EVENT_BUS.register(getInstance());
   }

   @SubscribeEvent
   public void onGuiDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
      if (event.getGui() instanceof GuiInventory) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.player != null) {
            this.animationTick += 0.05F;
            GuiContainer gui = (GuiContainer)event.getGui();
            int guiWidth = 176;
            int guiHeight = 166;
            int guiLeft = (gui.width - guiWidth) / 2;
            int guiTop = (gui.height - guiHeight) / 2;
            int frameTier = this.getFrameTier();
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            this.drawInventoryFrame(guiLeft, guiTop, guiWidth, guiHeight, frameTier);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
         }
      }
   }

   private int getFrameTier() {
      String playerRank = RankedHudRenderer.getPlayerRank();
      boolean isPlaced = RankedHudRenderer.isPlayerPlaced();
      if (!isPlaced) {
         return -1;
      } else if (playerRank.contains("Otsutsuki")) {
         return 7;
      } else if (playerRank.contains("Kage")) {
         return 6;
      } else if (playerRank.contains("ANBU")) {
         return 5;
      } else if (playerRank.contains("Elite Jonin")) {
         return 4;
      } else if (playerRank.contains("S. Jonin")) {
         return 3;
      } else if (playerRank.contains("Jonin")) {
         return 2;
      } else {
         return playerRank.contains("Chunin") ? 1 : 0;
      }
   }

   private void drawInventoryFrame(int x, int y, int width, int height, int tier) {
      int offset = 4;
      int frameX = x - offset;
      int frameY = y - offset;
      int frameW = width + offset * 2;
      int frameH = height + offset * 2;
      switch (tier) {
         case 0:
            this.drawGeninInventoryFrame(frameX, frameY, frameW, frameH);
            break;
         case 1:
            this.drawChuninInventoryFrame(frameX, frameY, frameW, frameH);
            break;
         case 2:
            this.drawSpecialInventoryFrame(frameX, frameY, frameW, frameH);
            break;
         case 3:
            this.drawJoninInventoryFrame(frameX, frameY, frameW, frameH);
            break;
         case 4:
            this.drawEliteInventoryFrame(frameX, frameY, frameW, frameH);
            break;
         case 5:
            this.drawAnbuInventoryFrame(frameX, frameY, frameW, frameH);
            break;
         case 6:
            this.drawKageInventoryFrame(frameX, frameY, frameW, frameH);
            break;
         case 7:
            this.drawOtsutsukiInventoryFrame(frameX, frameY, frameW, frameH);
            break;
         default:
            this.drawUnrankedInventoryFrame(frameX, frameY, frameW, frameH);
      }

   }

   private void drawUnrankedInventoryFrame(int x, int y, int w, int h) {
      int dark = -15066598;
      int mid = -13421773;
      int light = -11908534;
      float pulse = (float)(0.7 + 0.3 * Math.sin((double)this.animationTick * 0.03));
      int pulseAlpha = (int)(pulse * 40.0F);
      drawRect(x - 3, y - 3, x + w + 3, y + h + 3, pulseAlpha << 24 | 3355443);

      for(int i = 0; i < w; i += 12) {
         int segW = Math.min(8, w - i);
         drawRect(x + i, y - 2, x + i + segW, y, mid);
      }

      for(int i = 0; i < w; i += 12) {
         int segW = Math.min(8, w - i);
         drawRect(x + i, y + h, x + i + segW, y + h + 2, dark);
      }

      for(int i = 0; i < h; i += 12) {
         int segH = Math.min(8, h - i);
         drawRect(x - 2, y + i, x, y + i + segH, mid);
      }

      for(int i = 0; i < h; i += 12) {
         int segH = Math.min(8, h - i);
         drawRect(x + w, y + i, x + w + 2, y + i + segH, dark);
      }

      int cs = 10;
      drawRect(x - 3, y - 3, x + cs, y, light);
      drawRect(x - 3, y - 3, x, y + cs, light);
      drawRect(x + w - cs, y - 3, x + w + 3, y, mid);
      drawRect(x + w, y - 3, x + w + 3, y + cs, dark);
      drawRect(x - 3, y + h, x + cs, y + h + 3, mid);
      drawRect(x - 3, y + h - cs, x, y + h + 3, dark);
      drawRect(x + w - cs, y + h, x + w + 3, y + h + 3, dark);
      drawRect(x + w, y + h - cs, x + w + 3, y + h + 3, dark);
   }

   private void drawGeninInventoryFrame(int x, int y, int w, int h) {
      int highlight = -4473925;
      int base = -7829368;
      int shadow = -11184811;
      int darkShadow = -13421773;
      drawRect(x - 2, y - 3, x + w + 2, y - 1, highlight);
      drawRect(x - 2, y - 1, x + w + 2, y, base);
      drawRect(x - 3, y - 2, x - 1, y + h + 2, highlight);
      drawRect(x - 1, y - 2, x, y + h + 2, base);
      drawRect(x - 2, y + h, x + w + 2, y + h + 1, base);
      drawRect(x - 2, y + h + 1, x + w + 2, y + h + 3, shadow);
      drawRect(x + w, y - 2, x + w + 1, y + h + 2, base);
      drawRect(x + w + 1, y - 2, x + w + 3, y + h + 2, shadow);
      int cs = 8;
      drawRect(x - 3, y - 3, x + cs, y + cs, highlight);
      drawRect(x + w - cs, y - 3, x + w + 3, y + cs, base);
      drawRect(x - 3, y + h - cs, x + cs, y + h + 3, base);
      drawRect(x + w - cs, y + h - cs, x + w + 3, y + h + 3, darkShadow);
   }

   private void drawChuninInventoryFrame(int x, int y, int w, int h) {
      int highlight = -8921737;
      int base = -12277180;
      int mid = -13400013;
      int shadow = -14522846;
      int darkShadow = -15645679;
      drawRect(x - 4, y - 4, x + w + 4, y + h + 4, 541370948);
      drawRect(x - 2, y - 3, x + w + 2, y - 1, highlight);
      drawRect(x - 2, y - 1, x + w + 2, y + 1, base);
      drawRect(x - 3, y - 2, x - 1, y + h + 2, highlight);
      drawRect(x - 1, y - 2, x + 1, y + h + 2, base);
      drawRect(x - 2, y + h - 1, x + w + 2, y + h + 1, mid);
      drawRect(x - 2, y + h + 1, x + w + 2, y + h + 3, shadow);
      drawRect(x + w - 1, y - 2, x + w + 1, y + h + 2, mid);
      drawRect(x + w + 1, y - 2, x + w + 3, y + h + 2, shadow);
      int cs = 12;
      drawRect(x - 4, y - 4, x + cs, y, highlight);
      drawRect(x - 4, y - 4, x, y + cs, highlight);
      drawRect(x - 2, y - 3, x + cs - 2, y - 1, -5570646);
      drawRect(x + w - cs, y - 4, x + w + 4, y, base);
      drawRect(x + w, y - 4, x + w + 4, y + cs, mid);
      drawRect(x - 4, y + h, x + cs, y + h + 4, mid);
      drawRect(x - 4, y + h - cs, x, y + h + 4, shadow);
      drawRect(x + w - cs, y + h, x + w + 4, y + h + 4, shadow);
      drawRect(x + w, y + h - cs, x + w + 4, y + h + 4, darkShadow);
   }

   private void drawJoninInventoryFrame(int x, int y, int w, int h) {
      int flameWhite = -4404;
      int flameYellow = -13244;
      int flameOrange = -30686;
      int flameRed = -2271983;
      int ember = -5623040;
      int ash = -10083840;
      float flicker = (float)(0.8 + 0.2 * Math.sin((double)this.animationTick * 0.06));
      int flickerAlpha = (int)(flicker * 45.0F);
      drawRect(x - 5, y - 5, x + w + 5, y + h + 5, flickerAlpha << 24 | 16737809);
      drawRect(x - 3, y - 4, x + w + 3, y - 2, flameWhite);
      drawRect(x - 3, y - 2, x + w + 3, y, flameYellow);
      drawRect(x - 4, y - 3, x - 2, y + h + 3, flameWhite);
      drawRect(x - 2, y - 3, x, y + h + 3, flameYellow);
      drawRect(x - 3, y + h, x + w + 3, y + h + 2, flameRed);
      drawRect(x - 3, y + h + 2, x + w + 3, y + h + 4, ember);
      drawRect(x + w, y - 3, x + w + 2, y + h + 3, flameRed);
      drawRect(x + w + 2, y - 3, x + w + 4, y + h + 3, ember);
      int cs = 14;
      drawRect(x - 5, y - 6, x + cs, y - 1, flameWhite);
      drawRect(x - 6, y - 5, x - 1, y + cs, flameWhite);
      drawRect(x - 4, y - 5, x + 8, y - 3, -35);
      drawRect(x + w - cs, y - 6, x + w + 5, y - 1, flameYellow);
      drawRect(x + w + 1, y - 5, x + w + 6, y + cs, flameOrange);
      drawRect(x - 5, y + h + 1, x + cs, y + h + 6, flameOrange);
      drawRect(x - 6, y + h - cs, x - 1, y + h + 5, flameRed);
      drawRect(x + w - cs, y + h + 1, x + w + 5, y + h + 6, ember);
      drawRect(x + w + 1, y + h - cs, x + w + 6, y + h + 5, ash);
      float spark = (float)((double)0.5F + (double)0.5F * Math.sin((double)this.animationTick * 0.1));
      int sparkAlpha = (int)(spark * 180.0F);
      drawRect(x + w / 2, y - 5, x + w / 2 + 2, y - 3, sparkAlpha << 24 | 16777130);
   }

   private void drawSpecialInventoryFrame(int x, int y, int w, int h) {
      int parchment = -2833248;
      int highlight = -4478345;
      int base = -6715290;
      int mid = -8952252;
      int shadow = -11189214;
      int accent = -7638187;
      drawRect(x - 4, y - 4, x + w + 4, y + h + 4, 412715110);
      drawRect(x - 3, y - 3, x + w + 3, y - 2, highlight);
      drawRect(x - 3, y - 3, x - 2, y + h + 3, highlight);
      drawRect(x - 3, y + h + 2, x + w + 3, y + h + 3, shadow);
      drawRect(x + w + 2, y - 3, x + w + 3, y + h + 3, shadow);
      drawRect(x - 2, y - 2, x + w + 2, y, base);
      drawRect(x - 2, y - 2, x, y + h + 2, base);
      drawRect(x - 2, y + h, x + w + 2, y + h + 2, mid);
      drawRect(x + w, y - 2, x + w + 2, y + h + 2, mid);
      int cs = 10;
      drawRect(x - 4, y - 4, x + cs, y - 1, parchment);
      drawRect(x - 4, y - 4, x - 1, y + cs, parchment);
      drawRect(x - 3, y - 3, x + 5, y - 1, highlight);
      drawRect(x + w - cs, y - 4, x + w + 4, y - 1, highlight);
      drawRect(x + w + 1, y - 4, x + w + 4, y + cs, base);
      drawRect(x - 4, y + h + 1, x + cs, y + h + 4, base);
      drawRect(x - 4, y + h - cs, x - 1, y + h + 4, mid);
      drawRect(x + w - cs, y + h + 1, x + w + 4, y + h + 4, shadow);
      drawRect(x + w + 1, y + h - cs, x + w + 4, y + h + 4, shadow);
      int midX = x + w / 2;
      int midY = y + h / 2;
      drawRect(midX - 3, y - 4, midX + 3, y - 2, accent);
      drawRect(midX - 2, y - 3, midX + 2, y - 2, parchment);
      drawRect(x - 4, midY - 3, x - 2, midY + 3, accent);
      drawRect(x - 3, midY - 2, x - 2, midY + 2, parchment);
   }

   private void drawEliteInventoryFrame(int x, int y, int w, int h) {
      int electricWhite = -1114113;
      int highlight = -7803137;
      int base = -11154194;
      int mid = -12277061;
      int shadow = -13399911;
      int darkShadow = -14522761;
      float pulse = (float)(0.8 + 0.2 * Math.sin((double)this.animationTick * 0.045));
      int pulseAlpha = (int)(pulse * 50.0F);
      drawRect(x - 5, y - 5, x + w + 5, y + h + 5, pulseAlpha << 24 | 5623022);
      int cut = 10;
      drawRect(x + cut, y - 4, x + w - cut, y - 2, electricWhite);
      drawRect(x + cut, y - 2, x + w - cut, y, highlight);
      drawRect(x + cut, y + h, x + w - cut, y + h + 2, mid);
      drawRect(x + cut, y + h + 2, x + w - cut, y + h + 4, shadow);
      drawRect(x - 4, y + cut, x - 2, y + h - cut, electricWhite);
      drawRect(x - 2, y + cut, x, y + h - cut, highlight);
      drawRect(x + w, y + cut, x + w + 2, y + h - cut, mid);
      drawRect(x + w + 2, y + cut, x + w + 4, y + h - cut, shadow);
      drawRect(x - 3, y + cut - 3, x + cut, y + cut, electricWhite);
      drawRect(x + cut - 3, y - 3, x + cut, y + cut, electricWhite);
      drawRect(x + w - cut, y - 3, x + w + 3, y + cut, highlight);
      drawRect(x + w - cut, y + cut - 3, x + w + 3, y + cut, base);
      drawRect(x - 3, y + h - cut, x + cut, y + h + 3, base);
      drawRect(x + cut - 3, y + h - cut, x + cut, y + h + 3, mid);
      drawRect(x + w - cut, y + h - cut, x + w + 3, y + h + 3, shadow);
      int midY = y + h / 2;
      drawRect(x - 5, midY - 4, x - 3, midY + 4, highlight);
      drawRect(x + w + 3, midY - 4, x + w + 5, midY + 4, shadow);
      int midX = x + w / 2;
      drawRect(midX - 4, y - 5, midX + 4, y - 3, electricWhite);
      drawRect(midX - 4, y + h + 3, midX + 4, y + h + 5, shadow);
   }

   private void drawAnbuInventoryFrame(int x, int y, int w, int h) {
      int highlight = -3372818;
      int bright = -5609780;
      int base = -7842390;
      int mid = -10075000;
      int shadow = -12307610;
      int darkShadow = -14544572;
      float aura = (float)((double)0.75F + (double)0.25F * Math.sin((double)this.animationTick * 0.035));
      int auraAlpha = (int)(aura * 55.0F);

      for(int i = 2; i >= 0; --i) {
         int layerAlpha = auraAlpha / (i + 1);
         drawRect(x - 4 - i, y - 4 - i, x + w + 4 + i, y + h + 4 + i, layerAlpha << 24 | 8934826);
      }

      drawRect(x - 3, y - 4, x + w + 3, y - 2, highlight);
      drawRect(x - 3, y - 2, x + w + 3, y, bright);
      drawRect(x - 4, y - 3, x - 2, y + h + 3, highlight);
      drawRect(x - 2, y - 3, x, y + h + 3, bright);
      drawRect(x - 3, y + h, x + w + 3, y + h + 2, mid);
      drawRect(x - 3, y + h + 2, x + w + 3, y + h + 4, shadow);
      drawRect(x + w, y - 3, x + w + 2, y + h + 3, mid);
      drawRect(x + w + 2, y - 3, x + w + 4, y + h + 3, shadow);
      int cs = 15;
      drawRect(x - 5, y - 5, x + cs, y, highlight);
      drawRect(x - 5, y - 5, x, y + cs, highlight);
      drawRect(x - 4, y - 4, x + 5, y - 1, -2249985);
      drawRect(x + w - cs, y - 5, x + w + 5, y, bright);
      drawRect(x + w, y - 5, x + w + 5, y + cs, base);
      drawRect(x - 5, y + h, x + cs, y + h + 5, base);
      drawRect(x - 5, y + h - cs, x, y + h + 5, shadow);
      drawRect(x + w - cs, y + h, x + w + 5, y + h + 5, shadow);
      drawRect(x + w, y + h - cs, x + w + 5, y + h + 5, darkShadow);
      int midY = y + h / 2;
      drawRect(x - 6, midY - 5, x - 3, midY + 5, mid);
      drawRect(x - 6, midY - 3, x - 4, midY - 1, highlight);
      drawRect(x + w + 3, midY - 5, x + w + 6, midY + 5, darkShadow);
   }

   private void drawKageInventoryFrame(int x, int y, int w, int h) {
      int white = -21846;
      int highlight = -39322;
      int bright = -1162172;
      int base = -3399134;
      int mid = -5631727;
      int shadow = -7862264;
      int darkShadow = -12320768;
      float power = (float)(0.7 + 0.3 * Math.sin((double)this.animationTick * 0.04));
      int powerAlpha = (int)(power * 65.0F);

      for(int i = 3; i >= 0; --i) {
         int layerAlpha = powerAlpha / (i + 1);
         drawRect(x - 5 - i, y - 5 - i, x + w + 5 + i, y + h + 5 + i, layerAlpha << 24 | 13378082);
      }

      drawRect(x - 4, y - 5, x + w + 4, y - 3, white);
      drawRect(x - 4, y - 3, x + w + 4, y - 1, highlight);
      drawRect(x - 4, y - 1, x + w + 4, y + 1, bright);
      drawRect(x - 5, y - 4, x - 3, y + h + 4, white);
      drawRect(x - 3, y - 4, x - 1, y + h + 4, highlight);
      drawRect(x - 1, y - 4, x + 1, y + h + 4, bright);
      drawRect(x - 4, y + h - 1, x + w + 4, y + h + 1, base);
      drawRect(x - 4, y + h + 1, x + w + 4, y + h + 3, mid);
      drawRect(x - 4, y + h + 3, x + w + 4, y + h + 5, shadow);
      drawRect(x + w - 1, y - 4, x + w + 1, y + h + 4, base);
      drawRect(x + w + 1, y - 4, x + w + 3, y + h + 4, mid);
      drawRect(x + w + 3, y - 4, x + w + 5, y + h + 4, shadow);
      int cs = 18;
      drawRect(x - 6, y - 7, x + cs, y - 1, white);
      drawRect(x - 7, y - 6, x - 1, y + cs, white);
      drawRect(x - 5, y - 6, x + cs - 3, y - 4, -13108);
      drawRect(x + w - cs, y - 7, x + w + 6, y - 1, highlight);
      drawRect(x + w + 1, y - 6, x + w + 7, y + cs, bright);
      drawRect(x - 6, y + h + 1, x + cs, y + h + 7, bright);
      drawRect(x - 7, y + h - cs, x - 1, y + h + 6, mid);
      drawRect(x + w - cs, y + h + 1, x + w + 6, y + h + 7, shadow);
      drawRect(x + w + 1, y + h - cs, x + w + 7, y + h + 6, darkShadow);
      int midX = x + w / 2;
      int midY = y + h / 2;
      drawRect(midX - 2, y - 7, midX + 2, y - 5, white);
      drawRect(x - 7, midY - 2, x - 5, midY + 2, white);
   }

   private void drawOtsutsukiInventoryFrame(int x, int y, int w, int h) {
      float pulse = (float)(0.85 + 0.15 * Math.sin((double)this.animationTick * 0.03));
      int divine = -1;
      int hotGold = -4472;
      int highlight = this.blendColors(-8875, -13261, pulse);
      int bright = -1131725;
      int base = -2250206;
      int mid = -4487151;
      int shadow = -7838208;
      float divineGlow = (float)(0.6 + 0.4 * Math.sin((double)this.animationTick * 0.025));

      for(int i = 5; i >= 0; --i) {
         int layerAlpha = (int)(divineGlow * 80.0F / (float)(i + 1));
         int offset = 5 + i * 2;
         drawRect(x - offset, y - offset, x + w + offset, y + h + offset, layerAlpha << 24 | 16766720);
      }

      drawRect(x - 4, y - 6, x + w + 4, y - 4, divine);
      drawRect(x - 4, y - 4, x + w + 4, y - 2, hotGold);
      drawRect(x - 4, y - 2, x + w + 4, y, highlight);
      drawRect(x - 6, y - 4, x - 4, y + h + 4, divine);
      drawRect(x - 4, y - 4, x - 2, y + h + 4, hotGold);
      drawRect(x - 2, y - 4, x, y + h + 4, highlight);
      drawRect(x - 4, y + h, x + w + 4, y + h + 2, base);
      drawRect(x - 4, y + h + 2, x + w + 4, y + h + 4, mid);
      drawRect(x - 4, y + h + 4, x + w + 4, y + h + 6, shadow);
      drawRect(x + w, y - 4, x + w + 2, y + h + 4, base);
      drawRect(x + w + 2, y - 4, x + w + 4, y + h + 4, mid);
      drawRect(x + w + 4, y - 4, x + w + 6, y + h + 4, shadow);
      int cs = 20;
      drawRect(x - 7, y - 8, x + cs, y - 2, divine);
      drawRect(x - 8, y - 7, x - 2, y + cs, divine);
      drawRect(x - 6, y - 7, x + cs - 4, y - 5, -52);
      drawRect(x - 5, y - 5, x + 3, y + 3, hotGold);
      drawRect(x - 4, y - 4, x + 2, y + 2, divine);
      drawRect(x + w - cs, y - 8, x + w + 7, y - 2, hotGold);
      drawRect(x + w + 2, y - 7, x + w + 8, y + cs, highlight);
      drawRect(x + w - 3, y - 5, x + w + 5, y + 3, bright);
      drawRect(x - 7, y + h + 2, x + cs, y + h + 8, highlight);
      drawRect(x - 8, y + h - cs, x - 2, y + h + 7, base);
      drawRect(x - 5, y + h - 3, x + 3, y + h + 5, base);
      drawRect(x + w - cs, y + h + 2, x + w + 7, y + h + 8, mid);
      drawRect(x + w + 2, y + h - cs, x + w + 8, y + h + 7, shadow);
      drawRect(x + w - 3, y + h - 3, x + w + 5, y + h + 5, shadow);
      int midX = x + w / 2;
      int midY = y + h / 2;
      drawRect(midX - 2, y - 8, midX + 2, y - 6, divine);
      drawRect(x - 8, midY - 2, x - 6, midY + 2, divine);
      drawRect(midX - 2, y + h + 6, midX + 2, y + h + 8, mid);
      drawRect(x + w + 6, midY - 2, x + w + 8, midY + 2, shadow);
      this.drawOtsutsukiParticles(x, y, w, h);
   }

   private void drawOtsutsukiParticles(int x, int y, int w, int h) {
      int numParticles = 12;
      float time = this.animationTick * 0.01F;
      int centerX = x + w / 2;
      int centerY = y + h / 2;

      for(int i = 0; i < numParticles; ++i) {
         float angle = (float)((double)time + (double)i * Math.PI * (double)2.0F / (double)numParticles);
         float radiusX = (float)(w / 2 + 12);
         float radiusY = (float)(h / 2 + 12);
         float radiusPulse = (float)(Math.sin((double)time * 0.03 + (double)i * (double)0.5F) * (double)4.0F);
         int px = centerX + (int)(Math.cos((double)angle) * (double)(radiusX + radiusPulse));
         int py = centerY + (int)(Math.sin((double)angle) * (double)(radiusY + radiusPulse));
         float brightness = (float)(0.6 + 0.4 * Math.sin((double)time * 0.05 + (double)i));
         int particleAlpha = (int)(brightness * 255.0F);
         drawRect(px - 1, py - 1, px + 2, py + 2, particleAlpha << 24 | 16768341);
         drawRect(px, py, px + 1, py + 1, -1);
      }

      float sparkle = (float)((double)0.5F + (double)0.5F * Math.sin((double)this.animationTick * 0.08));
      int sparkleAlpha = (int)(sparkle * 200.0F);
      drawRect(x - 6, y - 6, x - 4, y - 4, sparkleAlpha << 24 | 16777130);
      drawRect(x + w + 4, y - 6, x + w + 6, y - 4, sparkleAlpha << 24 | 16777130);
      drawRect(x - 6, y + h + 4, x - 4, y + h + 6, sparkleAlpha << 24 | 16777130);
      drawRect(x + w + 4, y + h + 4, x + w + 6, y + h + 6, sparkleAlpha << 24 | 16777130);
   }

   private int blendColors(int color1, int color2, float ratio) {
      int a1 = color1 >> 24 & 255;
      int r1 = color1 >> 16 & 255;
      int g1 = color1 >> 8 & 255;
      int b1 = color1 & 255;
      int a2 = color2 >> 24 & 255;
      int r2 = color2 >> 16 & 255;
      int g2 = color2 >> 8 & 255;
      int b2 = color2 & 255;
      int a = (int)((float)a1 * ratio + (float)a2 * (1.0F - ratio));
      int r = (int)((float)r1 * ratio + (float)r2 * (1.0F - ratio));
      int g = (int)((float)g1 * ratio + (float)g2 * (1.0F - ratio));
      int b = (int)((float)b1 * ratio + (float)b2 * (1.0F - ratio));
      return a << 24 | r << 16 | g << 8 | b;
   }
}
