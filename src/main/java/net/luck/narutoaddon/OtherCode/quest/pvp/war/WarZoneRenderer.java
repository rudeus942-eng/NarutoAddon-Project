
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.luck.narutoaddon.OtherCode.quest.pvp.PvpClientData;
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
public class WarZoneRenderer {
   private static final int RENDER_DISTANCE = 256;
   private static final int RING_RENDER_DISTANCE = 80;
   private static final float BEAM_WIDTH = 0.6F;
   private static final int BEAM_HEIGHT = 256;
   private static final int RING_SEGMENTS = 48;

   @SubscribeEvent
   public void onRenderWorldLast(RenderWorldLastEvent event) {
      Minecraft mc = Minecraft.getMinecraft();
      EntityPlayer player = mc.player;
      if (player != null) {
         String playerVillage = PvpClientData.kageVillage;
         if (playerVillage != null && !playerVillage.isEmpty()) {
            List<PvpClientData.WarClientInfo> wars = PvpClientData.getActiveWars();
            PvpClientData.WarClientInfo activeWar = null;

            for(PvpClientData.WarClientInfo war : wars) {
               if (war.involvesVillage(playerVillage) && war.lobbyState == 3) {
                  activeWar = war;
                  break;
               }
            }

            if (activeWar != null) {
               List<PvpClientData.ZoneClientInfo> zones = PvpClientData.getWarZones();
               if (!zones.isEmpty()) {
                  boolean anyInRange = false;

                  for(PvpClientData.ZoneClientInfo zone : zones) {
                     double dx = (double)zone.centerX + (double)0.5F - player.posX;
                     double dz = (double)zone.centerZ + (double)0.5F - player.posZ;
                     if (dx * dx + dz * dz <= (double)65536.0F) {
                        anyInRange = true;
                        break;
                     }
                  }

                  if (anyInRange) {
                     float partialTicks = event.getPartialTicks();
                     double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
                     double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
                     double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;

                     for(PvpClientData.ZoneClientInfo zone : zones) {
                        double dx = (double)zone.centerX + (double)0.5F - player.posX;
                        double dz = (double)zone.centerZ + (double)0.5F - player.posZ;
                        double distSq = dx * dx + dz * dz;
                        if (!(distSq > (double)65536.0F)) {
                           double renderX = (double)zone.centerX + (double)0.5F - playerX;
                           double renderY = (double)0.0F - playerY;
                           double renderZ = (double)zone.centerZ + (double)0.5F - playerZ;
                           float[] beamColor = this.getZoneBeamColor(zone, activeWar);
                           float beamAlpha = this.getZoneBeamAlpha(zone);
                           this.renderBeam(renderX, renderY, renderZ, beamColor, beamAlpha);
                           if (distSq <= (double)6400.0F) {
                              this.renderBoundaryRing(renderX, renderY, renderZ, zone.radius, beamColor, zone.contested, player);
                           }
                        }
                     }

                  }
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
      float hw = 0.3F;
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
      float owh = 0.90000004F;
      float outerAlpha = alpha * 0.3F;
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

   private void renderBoundaryRing(double centerX, double centerY, double centerZ, int radius, float[] color, boolean contested, EntityPlayer player) {
      GlStateManager.pushMatrix();
      GlStateManager.disableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.disableDepth();
      GlStateManager.depthMask(false);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      float alpha = contested ? this.getContestedAlpha() : 0.4F;
      float quadSize = 0.3F;
      double ringY = centerY + player.posY + 0.1;
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);

      for(int i = 0; i < 48; ++i) {
         double angle = (Math.PI * 2D) * (double)i / (double)48.0F;
         double px = centerX + Math.cos(angle) * (double)radius;
         double pz = centerZ + Math.sin(angle) * (double)radius;
         buffer.pos(px - (double)quadSize, ringY, pz - (double)quadSize).color(color[0], color[1], color[2], alpha).endVertex();
         buffer.pos(px - (double)quadSize, ringY, pz + (double)quadSize).color(color[0], color[1], color[2], alpha).endVertex();
         buffer.pos(px + (double)quadSize, ringY, pz + (double)quadSize).color(color[0], color[1], color[2], alpha).endVertex();
         buffer.pos(px + (double)quadSize, ringY, pz - (double)quadSize).color(color[0], color[1], color[2], alpha).endVertex();
      }

      tessellator.draw();
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.disableBlend();
      GlStateManager.enableLighting();
      GlStateManager.enableTexture2D();
      GlStateManager.popMatrix();
   }

   private float[] getZoneBeamColor(PvpClientData.ZoneClientInfo zone, PvpClientData.WarClientInfo war) {
      if (zone.contested) {
         return new float[]{1.0F, 1.0F, 1.0F};
      } else if (!zone.controllingVillage.isEmpty()) {
         return this.villageNameToColor(zone.controllingVillage);
      } else {
         return !zone.capturingVillage.isEmpty() ? this.villageNameToColor(zone.capturingVillage) : new float[]{0.53F, 0.53F, 0.53F};
      }
   }

   private float getZoneBeamAlpha(PvpClientData.ZoneClientInfo zone) {
      return zone.contested ? this.getContestedAlpha() : 0.4F;
   }

   private float getContestedAlpha() {
      long time = System.currentTimeMillis();
      float t = (float)Math.sin((double)time * 0.006) * 0.5F + 0.5F;
      return 0.2F + t * 0.5F;
   }

   private float[] villageNameToColor(String villageName) {
      if (villageName == null) {
         return new float[]{0.53F, 0.53F, 0.53F};
      } else {
         switch (villageName) {
            case "Leaf":
               return new float[]{0.33F, 1.0F, 0.33F};
            case "Sand":
               return new float[]{1.0F, 1.0F, 0.33F};
            case "Stone":
               return new float[]{0.67F, 0.53F, 0.33F};
            case "Cloud":
               return new float[]{0.33F, 0.33F, 1.0F};
            case "Mist":
               return new float[]{0.33F, 1.0F, 1.0F};
            case "Rain":
               return new float[]{0.53F, 0.53F, 0.53F};
            default:
               return new float[]{0.53F, 0.53F, 0.53F};
         }
      }
   }
}
