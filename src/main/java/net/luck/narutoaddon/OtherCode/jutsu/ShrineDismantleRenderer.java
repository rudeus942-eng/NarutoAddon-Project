
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ShrineDismantleRenderer extends Render<EntityShrineDismantle.VisualSlashEntity> {
   private static final ResourceLocation[] HORIZONTAL_FRAMES = new ResourceLocation[]{new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/horizontal_0.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/horizontal_1.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/horizontal_2.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/horizontal_3.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/horizontal_4.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/horizontal_5.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/horizontal_6.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/horizontal_7.png")};
   private static final ResourceLocation[] STAR_FRAMES = new ResourceLocation[]{new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/star_0.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/star_1.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/star_2.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/star_3.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/star_4.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/star_5.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/star_6.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_dismantle/star_7.png")};

   public ShrineDismantleRenderer(RenderManager renderManager) {
      super(renderManager);
   }

   public void doRender(EntityShrineDismantle.VisualSlashEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
      if (entity.ticksExisted >= 1) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(x, y, z);
         float age = ((float)entity.ticksExisted + partialTicks) / (float)Math.max(1, entity.getMaxAge());
         float life = MathHelper.clamp(age, 0.0F, 1.0F);
         float popIn = Math.min(1.0F, life / 0.18F);
         float fadeOut = life < 0.62F ? 1.0F : (1.0F - life) / 0.38F;
         fadeOut = MathHelper.clamp(fadeOut, 0.0F, 1.0F);
         float visibility = popIn * fadeOut;
         ResourceLocation texture = entity.getSlashStyle() == 1 ? STAR_FRAMES[Math.floorMod(entity.getFrame(), STAR_FRAMES.length)] : HORIZONTAL_FRAMES[Math.floorMod(entity.getFrame(), HORIZONTAL_FRAMES.length)];
         GlStateManager.enableBlend();
         GlStateManager.disableLighting();
         GlStateManager.disableFog();
         GlStateManager.disableCull();
         GlStateManager.depthMask(false);
         GlStateManager.alphaFunc(516, 0.01F);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         float width = entity.getHalfWidth() * (0.75F + visibility * 0.35F);
         float height = entity.getHalfHeight() * (0.82F + visibility * 0.18F);
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         this.renderSlashLayer(texture, entity.getRoll(), 0.0F, 0.0F, 0.0F, width, height, visibility * 0.95F);
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         this.renderSlashLayer(texture, entity.getRoll(), 0.0F, 0.0F, 0.0F, width * 0.92F, height * 0.92F, visibility * 0.16F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.depthMask(true);
         GlStateManager.enableFog();
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.disableBlend();
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.popMatrix();
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }
   }

   private void renderSlashLayer(ResourceLocation texture, float roll, float offsetX, float offsetY, float offsetZ, float width, float height, float alpha) {
      this.bindTexture(texture);
      GlStateManager.pushMatrix();
      GlStateManager.translate(offsetX, offsetY, offsetZ);
      GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate((this.renderManager.options.thirdPersonView == 2 ? -1.0F : 1.0F) * this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(roll, 0.0F, 0.0F, 1.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
      buffer.pos((double)(-width), (double)(-height), (double)0.0F).tex((double)0.0F, (double)1.0F).endVertex();
      buffer.pos((double)width, (double)(-height), (double)0.0F).tex((double)1.0F, (double)1.0F).endVertex();
      buffer.pos((double)width, (double)height, (double)0.0F).tex((double)1.0F, (double)0.0F).endVertex();
      buffer.pos((double)(-width), (double)height, (double)0.0F).tex((double)0.0F, (double)0.0F).endVertex();
      tessellator.draw();
      GlStateManager.popMatrix();
   }

   protected ResourceLocation getEntityTexture(EntityShrineDismantle.VisualSlashEntity entity) {
      return HORIZONTAL_FRAMES[0];
   }
}
