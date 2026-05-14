
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ShrineMalevolentShrineRenderer extends Render<EntityShrineMalevolentShrine.EntityCustom> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("inftsukaddon:textures/entity/shrine_malevolentshrine.png");
   private final ModelMalevolentShrine model = new ModelMalevolentShrine();

   public ShrineMalevolentShrineRenderer(RenderManager renderManager) {
      super(renderManager);
      this.shadowSize = 0.0F;
   }

   public void doRender(EntityShrineMalevolentShrine.EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
      if (entity.isShrineVisible()) {
         float activeAge = (float)entity.getActiveAge() + partialTicks;
         float spawn = MathHelper.clamp(activeAge / 25.0F, 0.0F, 1.0F);
         float smoothSpawn = spawn * spawn * (3.0F - 2.0F * spawn);
         float tilt = -55.0F * (1.0F - smoothSpawn);
         float scale = 0.82F + smoothSpawn * 0.38F;
         GlStateManager.pushMatrix();
         GlStateManager.translate(x, y + 0.15 + (double)((1.0F - smoothSpawn) * 1.5F), z);
         GlStateManager.rotate(180.0F - entity.getCastYaw(), 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(tilt, 1.0F, 0.0F, 0.0F);
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.scale(-1.0F, -1.0F, 1.0F);
         GlStateManager.translate(0.0F, -1.5F, 0.0F);
         GlStateManager.disableCull();
         GlStateManager.enableRescaleNormal();
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         this.bindTexture(TEXTURE);
         this.model.render(entity, 0.0F, 0.0F, activeAge, 0.0F, 0.0F, 0.0625F);
         GlStateManager.disableRescaleNormal();
         GlStateManager.enableCull();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();
      }
   }

   protected ResourceLocation getEntityTexture(EntityShrineMalevolentShrine.EntityCustom entity) {
      return TEXTURE;
   }
}
