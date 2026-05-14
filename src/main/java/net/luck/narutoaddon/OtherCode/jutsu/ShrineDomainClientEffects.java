
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;

@SideOnly(Side.CLIENT)
public class ShrineDomainClientEffects extends Gui {
   private static final Set<EntityShrineMalevolentShrine.EntityCustom> WINDUP_SOUND_STARTED = Collections.newSetFromMap(new WeakHashMap());
   private static final Set<EntityShrineMalevolentShrine.EntityCustom> MUSIC_SOUND_STARTED = Collections.newSetFromMap(new WeakHashMap());

   @SubscribeEvent
   public void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.world == null) {
            WINDUP_SOUND_STARTED.clear();
            MUSIC_SOUND_STARTED.clear();
         } else {
            for(Entity entity : mc.world.loadedEntityList) {
               if (entity instanceof EntityShrineMalevolentShrine.EntityCustom) {
                  this.updateDomainSounds(mc, (EntityShrineMalevolentShrine.EntityCustom)entity);
               }
            }

         }
      }
   }

   private void updateDomainSounds(Minecraft mc, EntityShrineMalevolentShrine.EntityCustom domain) {
      if (!domain.isDead) {
         if (!domain.isShrineVisible()) {
            if (domain.ticksExisted <= 204 && WINDUP_SOUND_STARTED.add(domain)) {
               SoundEvent sound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("inftsukaddon:shrine_domain_expansion"));
               if (sound != null) {
                  mc.getSoundHandler().playSound(new ShrineDomainWindupSound(domain, sound));
               }
            }

         } else {
            if (domain.getActiveAge() <= 1200 && MUSIC_SOUND_STARTED.add(domain)) {
               SoundEvent sound = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("inftsukaddon:shrine_domainmusic"));
               if (sound != null) {
                  mc.getSoundHandler().playSound(new ShrineDomainMusicSound(domain, sound));
               }
            }

         }
      }
   }

   @SubscribeEvent
   public void onFogColors(EntityViewRenderEvent.FogColors event) {
      float intensity = this.getDomainTintIntensity();
      if (!(intensity <= 0.0F)) {
         event.setRed(MathHelper.clamp(event.getRed() * (1.0F - intensity) + 0.95F * intensity, 0.0F, 1.0F));
         event.setGreen(MathHelper.clamp(event.getGreen() * (1.0F - intensity) + 0.015F * intensity, 0.0F, 1.0F));
         event.setBlue(MathHelper.clamp(event.getBlue() * (1.0F - intensity) + 0.01F * intensity, 0.0F, 1.0F));
      }
   }

   @SubscribeEvent
   public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
      if (event.getType() == ElementType.ALL) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.player != null && mc.world != null && !mc.gameSettings.hideGUI) {
            float intensity = this.getDomainTintIntensity();
            if (!(intensity <= 0.0F)) {
               ScaledResolution sr = new ScaledResolution(mc);
               GlStateManager.pushMatrix();
               GlStateManager.disableTexture2D();
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(770, 771);
               int alpha = MathHelper.clamp((int)(112.0F * intensity), 0, 112);
               drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), alpha << 24 | 13370624);
               GlStateManager.disableBlend();
               GlStateManager.enableTexture2D();
               GlStateManager.popMatrix();
               this.renderShrineHealthBar(sr);
            }
         }
      }
   }

   private void renderShrineHealthBar(ScaledResolution sr) {
      EntityShrineMalevolentShrine.EntityCustom shrine = this.getVisibleShrineForPlayer();
      if (shrine != null) {
         int width = 220;
         int height = 12;
         int x = (sr.getScaledWidth() - width) / 2;
         int y = 15;
         float health = shrine.getShrineHealth();
         float maxHealth = shrine.getShrineMaxHealth();
         float percent = MathHelper.clamp(maxHealth > 0.0F ? health / maxHealth : 0.0F, 0.0F, 1.0F);
         int fill = (int)((float)width * percent);
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         drawRect(x - 5, y - 11, x + width + 5, y + height + 6, -654311424);
         drawRect(x - 4, y - 10, x + width + 4, y + height + 5, -1437990912);
         drawRect(x - 2, y - 2, x + width + 2, y + height + 2, -16449536);
         this.drawGradientRect(x, y, x + width, y + height, -14155776, -15859712);
         if (fill > 0) {
            this.drawGradientRect(x, y, x + fill, y + height, -2091498, -9829115);
            drawRect(x, y, x + fill, y + 1, -34953);
         }

         Minecraft mc = Minecraft.getMinecraft();
         String name = "§4§lMalevolent Shrine";
         int nameWidth = mc.fontRenderer.getStringWidth(name);
         mc.fontRenderer.drawStringWithShadow(name, (float)(sr.getScaledWidth() - nameWidth) / 2.0F, (float)(y - 10), 16777215);
         String text = String.format(Locale.ROOT, "%.0f / %.0f", health, maxHealth);
         int textWidth = mc.fontRenderer.getStringWidth(text);
         mc.fontRenderer.drawStringWithShadow(text, (float)(sr.getScaledWidth() - textWidth) / 2.0F, (float)(y + 2), 16777215);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }
   }

   private float getDomainTintIntensity() {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.player != null && mc.world != null) {
         float strongest = 0.0F;

         for(Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityShrineMalevolentShrine.EntityCustom) {
               EntityShrineMalevolentShrine.EntityCustom shrine = (EntityShrineMalevolentShrine.EntityCustom)entity;
               if (shrine.isShrineVisible() && shrine.getActiveAge() <= 1200) {
                  double distanceSq = mc.player.getDistanceSq(shrine.posX, shrine.posY, shrine.posZ);
                  float radius = shrine.getDomainRadius();
                  if (!(distanceSq > (double)(radius * radius))) {
                     float fadeIn = MathHelper.clamp((float)shrine.getActiveAge() / 20.0F, 0.0F, 1.0F);
                     float edge = MathHelper.clamp((radius - (float)Math.sqrt(distanceSq)) / 8.0F, 0.0F, 1.0F);
                     strongest = Math.max(strongest, 0.82F * fadeIn * edge);
                  }
               }
            }
         }

         return strongest;
      } else {
         return 0.0F;
      }
   }

   private EntityShrineMalevolentShrine.EntityCustom getVisibleShrineForPlayer() {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.player != null && mc.world != null) {
         EntityShrineMalevolentShrine.EntityCustom closest = null;
         double closestDistance = Double.MAX_VALUE;

         for(Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityShrineMalevolentShrine.EntityCustom) {
               EntityShrineMalevolentShrine.EntityCustom shrine = (EntityShrineMalevolentShrine.EntityCustom)entity;
               if (shrine.isShrineVisible() && shrine.getActiveAge() <= 1200) {
                  double distanceSq = mc.player.getDistanceSq(shrine.posX, shrine.posY, shrine.posZ);
                  float radius = shrine.getDomainRadius();
                  if (!(distanceSq > (double)(radius * radius)) && !(distanceSq >= closestDistance)) {
                     closestDistance = distanceSq;
                     closest = shrine;
                  }
               }
            }
         }

         return closest;
      } else {
         return null;
      }
   }
}
