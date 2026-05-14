
package net.luck.narutoaddon.OtherCode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RankedHudRenderer extends Gui {
   private static RankedHudRenderer instance;
   private static String playerRank = "Genin";
   private static int playerElo = 1000;
   private static boolean isPlaced = false;
   private float animationTick = 0.0F;

   public static RankedHudRenderer getInstance() {
      if (instance == null) {
         instance = new RankedHudRenderer();
      }

      return instance;
   }

   public static void updatePlayerData(String rank, int elo) {
      playerRank = rank != null ? rank : "Genin";
      playerElo = elo;
   }

   public static void updatePlayerData(String rank, int elo, String unused, boolean placed) {
      playerRank = rank != null ? rank : "Genin";
      playerElo = elo;
      isPlaced = placed;
   }

   public static String getPlayerRank() {
      return playerRank;
   }

   public static int getPlayerElo() {
      return playerElo;
   }

   public static boolean isPlayerPlaced() {
      return isPlaced;
   }

   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
      if (event.getType() == ElementType.HOTBAR) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.player != null && !mc.gameSettings.hideGUI) {
            if (mc.currentScreen == null || mc.currentScreen instanceof GuiChat) {
               this.animationTick += 0.05F;
               ScaledResolution scaled = new ScaledResolution(mc);
               int screenWidth = scaled.getScaledWidth();
               int screenHeight = scaled.getScaledHeight();
               int centerX = screenWidth / 2;
               int frameSize = 28;
               int frameX = centerX + 100;
               int frameY = screenHeight - 22 - frameSize / 2;
               this.drawPlayerHeadWithFrame(mc, frameX, frameY, frameSize);
            }
         }
      }
   }

   private void drawPlayerHeadWithFrame(Minecraft mc, int x, int y, int size) {
      NetworkPlayerInfo playerInfo = mc.getConnection() != null ? mc.getConnection().getPlayerInfo(mc.player.getUniqueID()) : null;
      ResourceLocation skinLocation;
      if (playerInfo != null) {
         skinLocation = playerInfo.getLocationSkin();
      } else {
         skinLocation = new ResourceLocation("textures/entity/steve.png");
      }

      int frameTier = this.getFrameTier();
      this.drawFrameBackground(x, y, size, frameTier);
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      mc.getTextureManager().bindTexture(skinLocation);
      int headSize = 18;
      int headX = x + (size - headSize) / 2;
      int headY = y + (size - headSize) / 2;
      this.drawScaledTexturedRect(headX, headY, headSize, headSize, 8, 8, 8, 8, 64, 64);
      this.drawScaledTexturedRect(headX, headY, headSize, headSize, 40, 8, 8, 8, 64, 64);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableBlend();
      mc.getTextureManager().bindTexture(Gui.ICONS);
      GlStateManager.popMatrix();
      this.drawFrameOverlay(x, y, size, frameTier);
   }

   private void drawScaledTexturedRect(int x, int y, int width, int height, int texX, int texY, int texW, int texH, int texWidth, int texHeight) {
      float u1 = (float)texX / (float)texWidth;
      float v1 = (float)texY / (float)texHeight;
      float u2 = (float)(texX + texW) / (float)texWidth;
      float v2 = (float)(texY + texH) / (float)texHeight;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
      buffer.pos((double)x, (double)(y + height), (double)0.0F).tex((double)u1, (double)v2).endVertex();
      buffer.pos((double)(x + width), (double)(y + height), (double)0.0F).tex((double)u2, (double)v2).endVertex();
      buffer.pos((double)(x + width), (double)y, (double)0.0F).tex((double)u2, (double)v1).endVertex();
      buffer.pos((double)x, (double)y, (double)0.0F).tex((double)u1, (double)v1).endVertex();
      tessellator.draw();
   }

   private int getFrameTier() {
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

   private void drawFrameBackground(int x, int y, int size, int tier) {
      int baseColor;
      int highlightColor;
      int shadowColor;
      int glowColor;
      switch (tier) {
         case -1:
            baseColor = -16119286;
            highlightColor = -15066598;
            shadowColor = -16448251;
            glowColor = -13421773;
            break;
         case 0:
         default:
            baseColor = -15461356;
            highlightColor = -14803426;
            shadowColor = -16119286;
            glowColor = -6710887;
            break;
         case 1:
            baseColor = -16115702;
            highlightColor = -15719920;
            shadowColor = -16446459;
            glowColor = -11154347;
            break;
         case 2:
            baseColor = -15199222;
            highlightColor = -14541808;
            shadowColor = -15988219;
            glowColor = -2241468;
            break;
         case 3:
            baseColor = -15070200;
            highlightColor = -14150640;
            shadowColor = -15923708;
            glowColor = -35038;
            break;
         case 4:
            baseColor = -16116712;
            highlightColor = -15721438;
            shadowColor = -16446964;
            glowColor = -11154194;
            break;
         case 5:
            baseColor = -15594984;
            highlightColor = -15069150;
            shadowColor = -16251636;
            glowColor = -6728244;
            break;
         case 6:
            baseColor = -15070710;
            highlightColor = -14020078;
            shadowColor = -15923963;
            glowColor = -2280653;
            break;
         case 7:
            baseColor = -15067888;
            highlightColor = -14015208;
            shadowColor = -15725304;
            glowColor = -10496;
      }

      if (tier >= 3) {
         float glowIntensity = (float)((double)0.25F + 0.15 * Math.sin((double)this.animationTick * 0.04));
         if (tier >= 5) {
            glowIntensity += 0.15F;
         }

         if (tier == 7) {
            glowIntensity += 0.1F;
         }

         int glowAlpha = (int)(glowIntensity * 255.0F);
         int glowWithAlpha = glowAlpha << 24 | glowColor & 16777215;
         int glowSize = tier >= 5 ? 4 : 3;

         for(int i = glowSize; i >= 1; --i) {
            int layerAlpha = glowAlpha / (i + 1);
            drawRect(x - i, y - i, x + size + i, y + size + i, layerAlpha << 24 | glowColor & 16777215);
         }
      }

      drawRect(x, y, x + size, y + size, baseColor);
      drawRect(x, y, x + size, y + 1, highlightColor);
      drawRect(x, y, x + 1, y + size, highlightColor);
      drawRect(x + 1, y + 1, x + size - 1, y + 2, this.blendColors(highlightColor, baseColor, 0.5F));
      drawRect(x + 1, y + 1, x + 2, y + size - 1, this.blendColors(highlightColor, baseColor, 0.5F));
      drawRect(x, y + size - 1, x + size, y + size, shadowColor);
      drawRect(x + size - 1, y, x + size, y + size, shadowColor);
      drawRect(x + 1, y + size - 2, x + size - 1, y + size - 1, this.blendColors(shadowColor, baseColor, 0.5F));
      drawRect(x + size - 2, y + 1, x + size - 1, y + size - 1, this.blendColors(shadowColor, baseColor, 0.5F));
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

   private void drawFrameOverlay(int x, int y, int size, int tier) {
      switch (tier) {
         case 0:
            this.drawGeninFrame(x, y, size);
            return;
         case 1:
            this.drawChuninFrame(x, y, size);
            return;
         case 2:
            this.drawSpecialFrame(x, y, size);
            return;
         case 3:
            this.drawJoninFrame(x, y, size);
            return;
         case 4:
            this.drawEliteFrame(x, y, size);
            return;
         case 5:
            this.drawAnbuFrame(x, y, size);
            return;
         case 6:
            this.drawKageFrame(x, y, size);
            return;
         case 7:
            this.drawOtsutsukiFrame(x, y, size);
            return;
         default:
            this.drawUnrankedFrame(x, y, size);
      }
   }

   private void drawUnrankedFrame(int x, int y, int size) {
      int dark = -15066598;
      int mid = -13421773;
      int light = -11908534;
      float pulse = (float)(0.7 + 0.3 * Math.sin((double)this.animationTick * 0.03));
      int pulseAlpha = (int)(pulse * 60.0F);
      drawRect(x - 2, y - 2, x + size + 2, y + size + 2, pulseAlpha << 24 | 4473924);
      drawRect(x, y - 1, x + 6, y + 1, mid);
      drawRect(x + 10, y - 1, x + size - 10, y + 1, mid);
      drawRect(x + size - 6, y - 1, x + size, y + 1, mid);
      drawRect(x, y + size - 1, x + 6, y + size + 1, dark);
      drawRect(x + 10, y + size - 1, x + size - 10, y + size + 1, dark);
      drawRect(x + size - 6, y + size - 1, x + size, y + size + 1, dark);
      drawRect(x - 1, y, x + 1, y + 6, mid);
      drawRect(x - 1, y + 10, x + 1, y + size - 10, mid);
      drawRect(x - 1, y + size - 6, x + 1, y + size, mid);
      drawRect(x + size - 1, y, x + size + 1, y + 6, dark);
      drawRect(x + size - 1, y + 10, x + size + 1, y + size - 10, dark);
      drawRect(x + size - 1, y + size - 6, x + size + 1, y + size, dark);
      int cs = 5;
      drawRect(x - 2, y - 2, x + cs, y, light);
      drawRect(x - 2, y - 2, x, y + cs, light);
      drawRect(x + size - cs, y - 2, x + size + 2, y, mid);
      drawRect(x + size, y - 2, x + size + 2, y + cs, dark);
      drawRect(x - 2, y + size, x + cs, y + size + 2, mid);
      drawRect(x - 2, y + size - cs, x, y + size + 2, dark);
      drawRect(x + size - cs, y + size, x + size + 2, y + size + 2, dark);
      drawRect(x + size, y + size - cs, x + size + 2, y + size + 2, dark);
   }

   private void drawGeninFrame(int x, int y, int size) {
      int highlight = -4473925;
      int base = -7829368;
      int shadow = -11184811;
      int darkShadow = -13421773;
      drawRect(x - 1, y - 2, x + size + 1, y, highlight);
      drawRect(x - 1, y, x + size + 1, y + 1, base);
      drawRect(x - 2, y - 1, x, y + size + 1, highlight);
      drawRect(x, y - 1, x + 1, y + size + 1, base);
      drawRect(x - 1, y + size - 1, x + size + 1, y + size, base);
      drawRect(x - 1, y + size, x + size + 1, y + size + 2, shadow);
      drawRect(x + size - 1, y - 1, x + size, y + size + 1, base);
      drawRect(x + size, y - 1, x + size + 2, y + size + 1, shadow);
      drawRect(x - 2, y - 2, x + 2, y + 2, highlight);
      drawRect(x + size - 2, y - 2, x + size + 2, y + 2, base);
      drawRect(x - 2, y + size - 2, x + 2, y + size + 2, base);
      drawRect(x + size - 2, y + size - 2, x + size + 2, y + size + 2, darkShadow);
      drawRect(x + 1, y + 1, x + 3, y + 3, 822083583);
   }

   private void drawChuninFrame(int x, int y, int size) {
      int highlight = -8921737;
      int base = -12277180;
      int mid = -13400013;
      int shadow = -14522846;
      int darkShadow = -15645679;
      drawRect(x - 3, y - 3, x + size + 3, y + size + 3, 541370948);
      drawRect(x - 1, y - 2, x + size + 1, y - 1, highlight);
      drawRect(x - 1, y - 1, x + size + 1, y + 1, base);
      drawRect(x - 2, y - 1, x - 1, y + size + 1, highlight);
      drawRect(x - 1, y - 1, x + 1, y + size + 1, base);
      drawRect(x - 1, y + size - 1, x + size + 1, y + size + 1, mid);
      drawRect(x - 1, y + size + 1, x + size + 1, y + size + 2, shadow);
      drawRect(x + size - 1, y - 1, x + size + 1, y + size + 1, mid);
      drawRect(x + size + 1, y - 1, x + size + 2, y + size + 1, shadow);
      int cs = 6;
      drawRect(x - 2, y - 3, x + cs, y, highlight);
      drawRect(x - 3, y - 2, x, y + cs, highlight);
      drawRect(x - 1, y - 2, x + cs - 1, y - 1, -5570646);
      drawRect(x + size - cs, y - 3, x + size + 2, y, base);
      drawRect(x + size, y - 2, x + size + 3, y + cs, mid);
      drawRect(x - 2, y + size, x + cs, y + size + 3, mid);
      drawRect(x - 3, y + size - cs, x, y + size + 2, shadow);
      drawRect(x + size - cs, y + size, x + size + 2, y + size + 3, shadow);
      drawRect(x + size, y + size - cs, x + size + 3, y + size + 2, darkShadow);
      drawRect(x + 2, y + 2, x + 4, y + 4, 1090519039);
   }

   private void drawSpecialFrame(int x, int y, int size) {
      int parchment = -2833248;
      int highlight = -4478345;
      int base = -6715290;
      int mid = -8952252;
      int shadow = -11189214;
      int accent = -7638187;
      drawRect(x - 3, y - 3, x + size + 3, y + size + 3, 546932838);
      drawRect(x - 2, y - 2, x + size + 2, y - 1, highlight);
      drawRect(x - 2, y - 2, x - 1, y + size + 2, highlight);
      drawRect(x - 2, y + size + 1, x + size + 2, y + size + 2, shadow);
      drawRect(x + size + 1, y - 2, x + size + 2, y + size + 2, shadow);
      drawRect(x - 1, y - 1, x + size + 1, y + 1, base);
      drawRect(x - 1, y - 1, x + 1, y + size + 1, base);
      drawRect(x - 1, y + size - 1, x + size + 1, y + size + 1, mid);
      drawRect(x + size - 1, y - 1, x + size + 1, y + size + 1, mid);
      int cs = 5;
      drawRect(x - 3, y - 3, x + cs, y - 1, parchment);
      drawRect(x - 3, y - 3, x - 1, y + cs, parchment);
      drawRect(x - 2, y - 2, x + 3, y, highlight);
      drawRect(x - 2, y - 2, x, y + 3, highlight);
      drawRect(x + size - cs, y - 3, x + size + 3, y - 1, highlight);
      drawRect(x + size + 1, y - 3, x + size + 3, y + cs, base);
      drawRect(x - 3, y + size + 1, x + cs, y + size + 3, base);
      drawRect(x - 3, y + size - cs, x - 1, y + size + 3, mid);
      drawRect(x + size - cs, y + size + 1, x + size + 3, y + size + 3, shadow);
      drawRect(x + size + 1, y + size - cs, x + size + 3, y + size + 3, shadow);
      int midPoint = size / 2;
      drawRect(x + midPoint - 2, y - 3, x + midPoint + 2, y - 1, accent);
      drawRect(x + midPoint - 1, y - 2, x + midPoint + 1, y - 1, parchment);
      drawRect(x - 3, y + midPoint - 2, x - 1, y + midPoint + 2, accent);
      drawRect(x - 2, y + midPoint - 1, x - 1, y + midPoint + 1, parchment);
      drawRect(x + 2, y + 2, x + 4, y + 4, 637534207);
   }

   private void drawJoninFrame(int x, int y, int size) {
      int flameWhite = -4404;
      int flameYellow = -13244;
      int flameOrange = -30686;
      int flameRed = -2271983;
      int ember = -5623040;
      int ash = -10083840;
      float flicker = (float)(0.8 + 0.2 * Math.sin((double)this.animationTick * 0.06));
      int flickerAlpha = (int)(flicker * 55.0F);
      drawRect(x - 4, y - 4, x + size + 4, y + size + 4, flickerAlpha << 24 | 16737809);
      drawRect(x - 2, y - 3, x + size + 2, y - 1, flameWhite);
      drawRect(x - 2, y - 1, x + size + 2, y + 1, flameYellow);
      drawRect(x - 3, y - 2, x - 1, y + size + 2, flameWhite);
      drawRect(x - 1, y - 2, x + 1, y + size + 2, flameYellow);
      drawRect(x - 2, y + size - 1, x + size + 2, y + size + 1, flameRed);
      drawRect(x - 2, y + size + 1, x + size + 2, y + size + 3, ember);
      drawRect(x + size - 1, y - 2, x + size + 1, y + size + 2, flameRed);
      drawRect(x + size + 1, y - 2, x + size + 3, y + size + 2, ember);
      int cs = 7;
      drawRect(x - 4, y - 5, x + cs, y - 1, flameWhite);
      drawRect(x - 5, y - 4, x - 1, y + cs, flameWhite);
      drawRect(x - 3, y - 4, x + 5, y - 2, -35);
      drawRect(x - 4, y - 3, x - 2, y + 5, -35);
      drawRect(x + size - cs, y - 5, x + size + 4, y - 1, flameYellow);
      drawRect(x + size + 1, y - 4, x + size + 5, y + cs, flameOrange);
      drawRect(x - 4, y + size + 1, x + cs, y + size + 5, flameOrange);
      drawRect(x - 5, y + size - cs, x - 1, y + size + 4, flameRed);
      drawRect(x + size - cs, y + size + 1, x + size + 4, y + size + 5, ember);
      drawRect(x + size + 1, y + size - cs, x + size + 5, y + size + 4, ash);
      float spark = (float)((double)0.5F + (double)0.5F * Math.sin((double)this.animationTick * 0.1));
      int sparkAlpha = (int)(spark * 180.0F);
      drawRect(x + size / 2, y - 4, x + size / 2 + 1, y - 3, sparkAlpha << 24 | 16777130);
      drawRect(x + 2, y + 2, x + 5, y + 5, 1174391876);
   }

   private void drawEliteFrame(int x, int y, int size) {
      int electricWhite = -1114113;
      int highlight = -7803137;
      int base = -11154194;
      int mid = -12277061;
      int shadow = -13399911;
      int darkShadow = -14522761;
      float pulse = (float)(0.8 + 0.2 * Math.sin((double)this.animationTick * 0.045));
      int pulseAlpha = (int)(pulse * 60.0F);
      drawRect(x - 4, y - 4, x + size + 4, y + size + 4, pulseAlpha << 24 | 5623022);
      int cut = 5;
      drawRect(x + cut, y - 3, x + size - cut, y - 1, electricWhite);
      drawRect(x + cut, y - 1, x + size - cut, y + 1, highlight);
      drawRect(x + cut, y + size - 1, x + size - cut, y + size + 1, mid);
      drawRect(x + cut, y + size + 1, x + size - cut, y + size + 3, shadow);
      drawRect(x - 3, y + cut, x - 1, y + size - cut, electricWhite);
      drawRect(x - 1, y + cut, x + 1, y + size - cut, highlight);
      drawRect(x + size - 1, y + cut, x + size + 1, y + size - cut, mid);
      drawRect(x + size + 1, y + cut, x + size + 3, y + size - cut, shadow);
      drawRect(x - 2, y + cut - 2, x + cut, y + cut, electricWhite);
      drawRect(x + cut - 2, y - 2, x + cut, y + cut, electricWhite);
      drawRect(x - 1, y + cut - 1, x + cut - 1, y + cut + 1, highlight);
      drawRect(x + cut - 1, y - 1, x + cut + 1, y + cut - 1, highlight);
      drawRect(x + size - cut, y - 2, x + size + 2, y + cut, highlight);
      drawRect(x + size - cut, y + cut - 2, x + size + 2, y + cut, base);
      drawRect(x - 2, y + size - cut, x + cut, y + size + 2, base);
      drawRect(x + cut - 2, y + size - cut, x + cut, y + size + 2, mid);
      drawRect(x + size - cut, y + size - cut, x + size + 2, y + size + 2, shadow);
      int midY = y + size / 2;
      drawRect(x - 4, midY - 2, x - 2, midY + 2, highlight);
      drawRect(x + size + 2, midY - 2, x + size + 4, midY + 2, shadow);
      drawRect(x + 3, y + 3, x + 6, y + 4, 1627389951);
   }

   private void drawAnbuFrame(int x, int y, int size) {
      int highlight = -3372818;
      int bright = -5609780;
      int base = -7842390;
      int mid = -10075000;
      int shadow = -12307610;
      int darkShadow = -14544572;
      float aura = (float)((double)0.75F + (double)0.25F * Math.sin((double)this.animationTick * 0.035));
      int auraAlpha = (int)(aura * 70.0F);

      for(int i = 2; i >= 0; --i) {
         int layerAlpha = auraAlpha / (i + 1);
         drawRect(x - 3 - i, y - 3 - i, x + size + 3 + i, y + size + 3 + i, layerAlpha << 24 | 8934826);
      }

      drawRect(x - 2, y - 3, x + size + 2, y - 1, highlight);
      drawRect(x - 2, y - 1, x + size + 2, y + 1, bright);
      drawRect(x - 3, y - 2, x - 1, y + size + 2, highlight);
      drawRect(x - 1, y - 2, x + 1, y + size + 2, bright);
      drawRect(x - 2, y + size - 1, x + size + 2, y + size + 1, mid);
      drawRect(x - 2, y + size + 1, x + size + 2, y + size + 3, shadow);
      drawRect(x + size - 1, y - 2, x + size + 1, y + size + 2, mid);
      drawRect(x + size + 1, y - 2, x + size + 3, y + size + 2, shadow);
      int cs = 8;
      drawRect(x - 4, y - 4, x + cs, y, highlight);
      drawRect(x - 4, y - 4, x, y + cs, highlight);
      drawRect(x - 3, y - 3, x + 3, y - 1, -2249985);
      drawRect(x + size - cs, y - 4, x + size + 4, y, bright);
      drawRect(x + size, y - 4, x + size + 4, y + cs, base);
      drawRect(x - 4, y + size, x + cs, y + size + 4, base);
      drawRect(x - 4, y + size - cs, x, y + size + 4, shadow);
      drawRect(x + size - cs, y + size, x + size + 4, y + size + 4, shadow);
      drawRect(x + size, y + size - cs, x + size + 4, y + size + 4, darkShadow);
      int midY = y + size / 2;
      drawRect(x - 5, midY - 3, x - 2, midY + 3, mid);
      drawRect(x - 5, midY - 2, x - 3, midY - 1, highlight);
      drawRect(x + size + 2, midY - 3, x + size + 5, midY + 3, darkShadow);
      drawRect(x + size + 3, midY + 1, x + size + 5, midY + 2, shadow);
      drawRect(x + 2, y + 2, x + 4, y + 4, 818710766);
   }

   private void drawKageFrame(int x, int y, int size) {
      int white = -21846;
      int highlight = -39322;
      int bright = -1162172;
      int base = -3399134;
      int mid = -5631727;
      int shadow = -7862264;
      int darkShadow = -12320768;
      float power = (float)(0.7 + 0.3 * Math.sin((double)this.animationTick * 0.04));
      int powerAlpha = (int)(power * 80.0F);

      for(int i = 3; i >= 0; --i) {
         int layerAlpha = powerAlpha / (i + 1);
         drawRect(x - 4 - i, y - 4 - i, x + size + 4 + i, y + size + 4 + i, layerAlpha << 24 | 13378082);
      }

      drawRect(x - 3, y - 4, x + size + 3, y - 2, white);
      drawRect(x - 3, y - 2, x + size + 3, y, highlight);
      drawRect(x - 3, y, x + size + 3, y + 2, bright);
      drawRect(x - 4, y - 3, x - 2, y + size + 3, white);
      drawRect(x - 2, y - 3, x, y + size + 3, highlight);
      drawRect(x, y - 3, x + 2, y + size + 3, bright);
      drawRect(x - 3, y + size - 2, x + size + 3, y + size, base);
      drawRect(x - 3, y + size, x + size + 3, y + size + 2, mid);
      drawRect(x - 3, y + size + 2, x + size + 3, y + size + 4, shadow);
      drawRect(x + size - 2, y - 3, x + size, y + size + 3, base);
      drawRect(x + size, y - 3, x + size + 2, y + size + 3, mid);
      drawRect(x + size + 2, y - 3, x + size + 4, y + size + 3, shadow);
      int cs = 10;
      drawRect(x - 5, y - 6, x + cs, y - 1, white);
      drawRect(x - 6, y - 5, x - 1, y + cs, white);
      drawRect(x - 4, y - 5, x + cs - 2, y - 3, -13108);
      drawRect(x + size - cs, y - 6, x + size + 5, y - 1, highlight);
      drawRect(x + size + 1, y - 5, x + size + 6, y + cs, bright);
      drawRect(x - 5, y + size + 1, x + cs, y + size + 6, bright);
      drawRect(x - 6, y + size - cs, x - 1, y + size + 5, mid);
      drawRect(x + size - cs, y + size + 1, x + size + 5, y + size + 6, shadow);
      drawRect(x + size + 1, y + size - cs, x + size + 6, y + size + 5, darkShadow);
      drawRect(x + size / 2 - 1, y - 6, x + size / 2 + 1, y - 4, white);
      drawRect(x - 6, y + size / 2 - 1, x - 4, y + size / 2 + 1, white);
      drawRect(x + 3, y + 3, x + 6, y + 6, 1358932650);
   }

   private void drawOtsutsukiFrame(int x, int y, int size) {
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
         int layerAlpha = (int)(divineGlow * 100.0F / (float)(i + 1));
         int offset = 4 + i * 2;
         drawRect(x - offset, y - offset, x + size + offset, y + size + offset, layerAlpha << 24 | 16766720);
      }

      drawRect(x - 3, y - 5, x + size + 3, y - 3, divine);
      drawRect(x - 3, y - 3, x + size + 3, y - 1, hotGold);
      drawRect(x - 3, y - 1, x + size + 3, y + 1, highlight);
      drawRect(x - 5, y - 3, x - 3, y + size + 3, divine);
      drawRect(x - 3, y - 3, x - 1, y + size + 3, hotGold);
      drawRect(x - 1, y - 3, x + 1, y + size + 3, highlight);
      drawRect(x - 3, y + size - 1, x + size + 3, y + size + 1, base);
      drawRect(x - 3, y + size + 1, x + size + 3, y + size + 3, mid);
      drawRect(x - 3, y + size + 3, x + size + 3, y + size + 5, shadow);
      drawRect(x + size - 1, y - 3, x + size + 1, y + size + 3, base);
      drawRect(x + size + 1, y - 3, x + size + 3, y + size + 3, mid);
      drawRect(x + size + 3, y - 3, x + size + 5, y + size + 3, shadow);
      int cs = 12;
      drawRect(x - 6, y - 7, x + cs, y - 2, divine);
      drawRect(x - 7, y - 6, x - 2, y + cs, divine);
      drawRect(x - 5, y - 6, x + cs - 3, y - 4, -52);
      drawRect(x - 4, y - 4, x + 2, y + 2, hotGold);
      drawRect(x - 3, y - 3, x + 1, y + 1, divine);
      drawRect(x + size - cs, y - 7, x + size + 6, y - 2, hotGold);
      drawRect(x + size + 2, y - 6, x + size + 7, y + cs, highlight);
      drawRect(x + size - 2, y - 4, x + size + 4, y + 2, bright);
      drawRect(x - 6, y + size + 2, x + cs, y + size + 7, highlight);
      drawRect(x - 7, y + size - cs, x - 2, y + size + 6, base);
      drawRect(x - 4, y + size - 2, x + 2, y + size + 4, base);
      drawRect(x + size - cs, y + size + 2, x + size + 6, y + size + 7, mid);
      drawRect(x + size + 2, y + size - cs, x + size + 7, y + size + 6, shadow);
      drawRect(x + size - 2, y + size - 2, x + size + 4, y + size + 4, shadow);
      int midX = x + size / 2;
      int midY = y + size / 2;
      drawRect(midX - 1, y - 7, midX + 1, y - 5, divine);
      drawRect(x - 7, midY - 1, x - 5, midY + 1, divine);
      drawRect(x + 3, y + 3, x + 7, y + 7, 1627389900);
      this.drawOtsutsukiParticles(x, y, size);
   }

   private void drawOtsutsukiParticles(int x, int y, int size) {
      int numParticles = 8;
      float time = this.animationTick * 0.012F;

      for(int i = 0; i < numParticles; ++i) {
         float angle = (float)((double)time + (double)i * Math.PI * (double)2.0F / (double)numParticles);
         float radiusBase = (float)(size / 2 + 8);
         float radiusPulse = (float)(Math.sin((double)time * 0.04 + (double)i * (double)0.5F) * (double)3.0F);
         float radius = radiusBase + radiusPulse;
         int px = x + size / 2 + (int)(Math.cos((double)angle) * (double)radius);
         int py = y + size / 2 + (int)(Math.sin((double)angle) * (double)radius);
         float brightness = (float)(0.6 + 0.4 * Math.sin((double)time * 0.06 + (double)i));
         int particleAlpha = (int)(brightness * 255.0F);
         drawRect(px - 1, py - 1, px + 2, py + 2, particleAlpha << 24 | 16768341);
         drawRect(px, py, px + 1, py + 1, -1);
      }

      float sparkle = (float)((double)0.5F + (double)0.5F * Math.sin((double)this.animationTick * 0.08));
      int sparkleAlpha = (int)(sparkle * 200.0F);
      drawRect(x - 5, y - 5, x - 4, y - 4, sparkleAlpha << 24 | 16777130);
      drawRect(x + size + 4, y - 5, x + size + 5, y - 4, sparkleAlpha << 24 | 16777130);
   }

   private int brightenColor(int color, int amount) {
      int a = color >> 24 & 255;
      int r = Math.min(255, (color >> 16 & 255) + amount);
      int g = Math.min(255, (color >> 8 & 255) + amount);
      int b = Math.min(255, (color & 255) + amount);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private int darkenColor(int color, int amount) {
      int a = color >> 24 & 255;
      int r = Math.max(0, (color >> 16 & 255) - amount);
      int g = Math.max(0, (color >> 8 & 255) - amount);
      int b = Math.max(0, (color & 255) - amount);
      return a << 24 | r << 16 | g << 8 | b;
   }
}
