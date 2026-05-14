
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(
   modid = "inftsukaddon",
   value = {Side.CLIENT}
)
public class KetsuryuganGenjutsuClient extends Gui {
   private static final float PEAK_INTENSITY = 0.85F;
   private static final int OVERLAY_ALPHA = 130;
   private static final int OVERLAY_COLOR = 13370624;

   @SubscribeEvent
   public static void onFogColors(EntityViewRenderEvent.FogColors event) {
      float intensity = getGenjutsuIntensity();
      if (!(intensity <= 0.0F)) {
         event.setRed(MathHelper.clamp(event.getRed() * (1.0F - intensity) + 0.95F * intensity, 0.0F, 1.0F));
         event.setGreen(MathHelper.clamp(event.getGreen() * (1.0F - intensity) + 0.015F * intensity, 0.0F, 1.0F));
         event.setBlue(MathHelper.clamp(event.getBlue() * (1.0F - intensity) + 0.01F * intensity, 0.0F, 1.0F));
      }
   }

   @SubscribeEvent
   public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
      if (event.getType() == ElementType.ALL) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.player != null && mc.world != null && !mc.gameSettings.hideGUI) {
            float intensity = getGenjutsuIntensity();
            if (!(intensity <= 0.0F)) {
               ScaledResolution sr = new ScaledResolution(mc);
               GlStateManager.pushMatrix();
               GlStateManager.disableTexture2D();
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(770, 771);
               int alpha = MathHelper.clamp((int)(130.0F * intensity), 0, 130);
               Gui.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), alpha << 24 | 13370624);
               GlStateManager.disableBlend();
               GlStateManager.enableTexture2D();
               GlStateManager.popMatrix();
            }
         }
      }
   }

   private static float getGenjutsuIntensity() {
      Minecraft mc = Minecraft.getMinecraft();
      EntityPlayer player = mc.player;
      if (player != null && mc.world != null) {
         if (!hasAmp9(player, MobEffects.SLOWNESS)) {
            return 0.0F;
         } else if (!hasAmp9(player, MobEffects.MINING_FATIGUE)) {
            return 0.0F;
         } else {
            return !hasAmp9(player, MobEffects.WEAKNESS) ? 0.0F : 0.85F;
         }
      } else {
         return 0.0F;
      }
   }

   private static boolean hasAmp9(EntityPlayer player, Potion potion) {
      PotionEffect eff = player.getActivePotionEffect(potion);
      return eff != null && eff.getAmplifier() >= 9;
   }
}
