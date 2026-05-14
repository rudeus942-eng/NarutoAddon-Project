
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
public class ShrineCleaveRenderer extends Render<EntityShrineCleave.VisualSlashEntity> {
   private static final ResourceLocation[] HORIZONTAL_FRAMES = new ResourceLocation[]{new ResourceLocation("inftsukaddon:textures/effects/shrine_cleave/horizontal_0.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_cleave/horizontal_1.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_cleave/horizontal_2.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_cleave/horizontal_3.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_cleave/horizontal_4.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_cleave/horizontal_5.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_cleave/horizontal_6.png"), new ResourceLocation("inftsukaddon:textures/effects/shrine_cleave/horizontal_7.png")};

   public ShrineCleaveRenderer(RenderManager renderManager) {
      super(renderManager);
   }

   public void doRender(EntityShrineCleave.VisualSlashEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
      if (entity.ticksExisted >= 1) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(x, y, z);
         float age = ((float)entity.ticksExisted + partialTicks) / (float)Math.max(1, entity.getMaxAge());
         float life = MathHelper.clamp(age, 0.0F, 1.0F);
         float popIn = Math.min(1.0F, life / 0.15F);
         float fadeOut = life < 0.68F ? 1.0F : (1.0F - life) / 0.32F;
         fadeOut = MathHelper.clamp(fadeOut, 0.0F, 1.0F);
         float visibility = popIn * fadeOut;
         float width = entity.getHalfWidth() * (0.82F + visibility * 0.25F);
         float height = entity.getHalfHeight() * (0.88F + visibility * 0.12F);
         GlStateManager.enableBlend();
         GlStateManager.disableLighting();
         GlStateManager.disableFog();
         GlStateManager.disableCull();
         GlStateManager.depthMask(false);
         GlStateManager.alphaFunc(516, 0.01F);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         ResourceLocation texture = HORIZONTAL_FRAMES[Math.floorMod(entity.getFrame(), HORIZONTAL_FRAMES.length)];
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         this.renderSlashLayer(texture, entity.getRoll(), width, height, visibility * 0.96F);
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         this.renderSlashLayer(texture, entity.getRoll(), width * 0.92F, height * 0.92F, visibility * 0.16F);
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

   private void renderSlashLayer(ResourceLocation texture, float roll, float width, float height, float alpha) {
      this.bindTexture(texture);
      GlStateManager.pushMatrix();
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

   protected ResourceLocation getEntityTexture(EntityShrineCleave.VisualSlashEntity entity) {
      return HORIZONTAL_FRAMES[0];
   }
}
