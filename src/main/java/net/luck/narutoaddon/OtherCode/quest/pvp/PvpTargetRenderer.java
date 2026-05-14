
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class PvpTargetRenderer {
   private static final int RENDER_DISTANCE = 512;
   private static final float BEAM_WIDTH = 0.3F;
   private static final int BEAM_HEIGHT = 256;
   private static final float[][] BEAM_COLORS = new float[][]{{1.0F, 0.27F, 0.27F}, {1.0F, 0.53F, 0.27F}, {0.67F, 0.27F, 1.0F}};

   @SubscribeEvent
   public void onRenderWorldLast(RenderWorldLastEvent event) {
      List<PvpClientData.TargetPosInfo> targets = PvpClientData.getTargetPositions();
      if (!targets.isEmpty()) {
         Minecraft mc = Minecraft.getMinecraft();
         EntityPlayer player = mc.player;
         if (player != null) {
            float partialTicks = event.getPartialTicks();
            double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
            double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
            double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
            long ticks = mc.world.getTotalWorldTime();
            float pulseAlpha = 0.15F + 0.1F * (float)Math.sin((double)ticks * 0.1 + (double)partialTicks * 0.1);

            for(PvpClientData.TargetPosInfo target : targets) {
               double dx = (double)target.x + (double)0.5F - player.posX;
               double dz = (double)target.z + (double)0.5F - player.posZ;
               double distSq = dx * dx + dz * dz;
               if (!(distSq > (double)262144.0F)) {
                  double renderX = (double)target.x + (double)0.5F - playerX;
                  double renderY = (double)0.0F - playerY;
                  double renderZ = (double)target.z + (double)0.5F - playerZ;
                  float[] color = getColor(target.beaconType);
                  this.renderBeam(renderX, renderY, renderZ, color, pulseAlpha);
               }
            }

         }
      }
   }

   private void renderBeam(double x, double y, double z, float[] color, float alpha) {
      GlStateManager.pushMatrix();
      GlStateManager.disableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.disableDepth();
      GlStateManager.depthMask(false);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      float hw = 0.15F;
      float topAlpha = alpha * 0.3F;
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(x - (double)hw, y, z - (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x - (double)hw, y + (double)256.0F, z - (double)hw).color(color[0], color[1], color[2], topAlpha).endVertex();
      buffer.pos(x + (double)hw, y + (double)256.0F, z - (double)hw).color(color[0], color[1], color[2], topAlpha).endVertex();
      buffer.pos(x + (double)hw, y, z - (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x + (double)hw, y, z + (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x + (double)hw, y + (double)256.0F, z + (double)hw).color(color[0], color[1], color[2], topAlpha).endVertex();
      buffer.pos(x - (double)hw, y + (double)256.0F, z + (double)hw).color(color[0], color[1], color[2], topAlpha).endVertex();
      buffer.pos(x - (double)hw, y, z + (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x + (double)hw, y, z - (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x + (double)hw, y + (double)256.0F, z - (double)hw).color(color[0], color[1], color[2], topAlpha).endVertex();
      buffer.pos(x + (double)hw, y + (double)256.0F, z + (double)hw).color(color[0], color[1], color[2], topAlpha).endVertex();
      buffer.pos(x + (double)hw, y, z + (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x - (double)hw, y, z + (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x - (double)hw, y + (double)256.0F, z + (double)hw).color(color[0], color[1], color[2], topAlpha).endVertex();
      buffer.pos(x - (double)hw, y + (double)256.0F, z - (double)hw).color(color[0], color[1], color[2], topAlpha).endVertex();
      buffer.pos(x - (double)hw, y, z - (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      tessellator.draw();
      float owh = 0.45000002F;
      float outerAlpha = alpha * 0.35F;
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(x - (double)owh, y, z - (double)owh).color(color[0], color[1], color[2], outerAlpha).endVertex();
      buffer.pos(x - (double)owh, y + (double)256.0F, z - (double)owh).color(color[0], color[1], color[2], 0.0F).endVertex();
      buffer.pos(x + (double)owh, y + (double)256.0F, z - (double)owh).color(color[0], color[1], color[2], 0.0F).endVertex();
      buffer.pos(x + (double)owh, y, z - (double)owh).color(color[0], color[1], color[2], outerAlpha).endVertex();
      buffer.pos(x + (double)owh, y, z + (double)owh).color(color[0], color[1], color[2], outerAlpha).endVertex();
      buffer.pos(x + (double)owh, y + (double)256.0F, z + (double)owh).color(color[0], color[1], color[2], 0.0F).endVertex();
      buffer.pos(x - (double)owh, y + (double)256.0F, z + (double)owh).color(color[0], color[1], color[2], 0.0F).endVertex();
      buffer.pos(x - (double)owh, y, z + (double)owh).color(color[0], color[1], color[2], outerAlpha).endVertex();
      buffer.pos(x + (double)owh, y, z - (double)owh).color(color[0], color[1], color[2], outerAlpha).endVertex();
      buffer.pos(x + (double)owh, y + (double)256.0F, z - (double)owh).color(color[0], color[1], color[2], 0.0F).endVertex();
      buffer.pos(x + (double)owh, y + (double)256.0F, z + (double)owh).color(color[0], color[1], color[2], 0.0F).endVertex();
      buffer.pos(x + (double)owh, y, z + (double)owh).color(color[0], color[1], color[2], outerAlpha).endVertex();
      buffer.pos(x - (double)owh, y, z + (double)owh).color(color[0], color[1], color[2], outerAlpha).endVertex();
      buffer.pos(x - (double)owh, y + (double)256.0F, z + (double)owh).color(color[0], color[1], color[2], 0.0F).endVertex();
      buffer.pos(x - (double)owh, y + (double)256.0F, z - (double)owh).color(color[0], color[1], color[2], 0.0F).endVertex();
      buffer.pos(x - (double)owh, y, z - (double)owh).color(color[0], color[1], color[2], outerAlpha).endVertex();
      tessellator.draw();
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.disableBlend();
      GlStateManager.enableLighting();
      GlStateManager.enableTexture2D();
      GlStateManager.popMatrix();
   }

   private static float[] getColor(int beaconType) {
      return beaconType >= 0 && beaconType < BEAM_COLORS.length ? BEAM_COLORS[beaconType] : BEAM_COLORS[0];
   }
}
