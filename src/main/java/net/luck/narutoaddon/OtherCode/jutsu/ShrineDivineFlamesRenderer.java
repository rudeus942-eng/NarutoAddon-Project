
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@SideOnly(Side.CLIENT)
public class ShrineDivineFlamesRenderer extends Render<EntityShrineDivineFlames.EntityCustom> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("inftsukaddon:textures/effects/shrine_divineflames.png");
   private static final Set<EntityShrineDivineFlames.EntityCustom> CHARGE_SOUND_STARTED = Collections.newSetFromMap(new WeakHashMap());
   private final ShrineModelDivineFlames model = new ShrineModelDivineFlames();

   public ShrineDivineFlamesRenderer(RenderManager renderManager) {
      super(renderManager);
      this.shadowSize = 0.0F;
   }

   public void doRender(EntityShrineDivineFlames.EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
      this.playChargeSoundOnce(entity);
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y + 0.18, z);
      GlStateManager.rotate(-this.interpolateAngle(entity.prevRotationYaw, entity.rotationYaw, partialTicks), 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(this.interpolateAngle(entity.prevRotationPitch, entity.rotationPitch, partialTicks), 1.0F, 0.0F, 0.0F);
      float scale = entity.getRenderScale();
      float chargeAlpha = entity.isCharging() ? 0.85F : 1.0F;
      float chargeSize = entity.isCharging() ? 0.8F + entity.getChargePower() / 10.0F * 0.35F : 1.15F;
      float lengthScale = this.getLengthScale(entity, partialTicks);
      this.bindEntityTexture(entity);
      GlStateManager.enableBlend();
      GlStateManager.disableLighting();
      GlStateManager.disableCull();
      GlStateManager.depthMask(false);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
      GlStateManager.scale(scale * chargeSize, scale * chargeSize, scale * chargeSize * lengthScale);
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      GlStateManager.color(1.0F, 1.0F, 1.0F, chargeAlpha);
      this.model.render(entity, 0.0F, 0.0F, (float)entity.ticksExisted + partialTicks, 0.0F, 0.0F, 0.0625F);
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
      GlStateManager.scale(1.05F, 1.05F, 1.08F);
      GlStateManager.color(1.0F, 0.65F, 0.22F, entity.isCharging() ? 0.3F : 0.36F);
      this.model.render(entity, 0.0F, 0.0F, (float)entity.ticksExisted + partialTicks, 0.0F, 0.0F, 0.0625F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.depthMask(true);
      GlStateManager.enableLighting();
      GlStateManager.enableCull();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   private void playChargeSoundOnce(EntityShrineDivineFlames.EntityCustom entity) {
      if (entity.isCharging() && !CHARGE_SOUND_STARTED.contains(entity)) {
         CHARGE_SOUND_STARTED.add(entity);
         SoundEvent start = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("inftsukaddon:shrine_divineflames_cast"));
         if (start != null) {
            Minecraft.getMinecraft().getSoundHandler().playSound(new ShrineChargingCastSound(entity, start));
         }

      }
   }

   private float getLengthScale(EntityShrineDivineFlames.EntityCustom entity, float partialTicks) {
      if (!entity.isCharging()) {
         return 1.0F;
      } else {
         float progressTicks = Math.min(58.0F, (float)entity.getChargeTicks() + partialTicks);
         float seconds = progressTicks / 20.0F;
         if (seconds <= 0.4167F) {
            return this.lerp(0.02F, 0.5342F, seconds / 0.4167F);
         } else if (seconds <= 1.0417F) {
            return this.lerp(0.5342F, 0.525F, (seconds - 0.4167F) / 0.625F);
         } else {
            return seconds <= 2.9167F ? this.lerp(0.525F, 1.0F, (seconds - 1.0417F) / 1.8749999F) : 1.0F;
         }
      }
   }

   private float lerp(float a, float b, float t) {
      return a + (b - a) * MathHelper.clamp(t, 0.0F, 1.0F);
   }

   private float interpolateAngle(float prev, float current, float partialTicks) {
      float delta;
      for(delta = current - prev; delta < -180.0F; delta += 360.0F) {
      }

      while(delta >= 180.0F) {
         delta -= 360.0F;
      }

      return prev + partialTicks * delta;
   }

   protected ResourceLocation getEntityTexture(EntityShrineDivineFlames.EntityCustom entity) {
      return TEXTURE;
   }
}
