
package net.luck.narutoaddon.OtherCode.quest.waypoint;

import net.luck.narutoaddon.OtherCode.quest.gui.QuestTrackerOverlay;
import net.luck.narutoaddon.OtherCode.quest.network.QuestClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class WaypointRenderer {
   private static final int RENDER_DISTANCE = 1024;
   private static final float BEAM_WIDTH = 0.4F;
   private static final int BEAM_HEIGHT = 256;
   private static final String[] ARROW_SLOT_PRIORITY_NON_STORY = new String[]{"daily_0", "daily_1", "daily_2", "weekly_0", "weekly_1", "weekly_2", "random", "outpost_mission", "bingo_hunt", "incursion_event", "contract_mission"};
   private static final int MAX_ARROWS = 5;

   @SubscribeEvent
   public void onRenderWorldLast(RenderWorldLastEvent event) {
      if (QuestClientData.hasWaypoint()) {
         Minecraft mc = Minecraft.getMinecraft();
         EntityPlayer player = mc.player;
         if (player != null) {
            float partialTicks = event.getPartialTicks();
            double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
            double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
            double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;

            for(Map.Entry<String, QuestClientData.WaypointInfo> entry : QuestClientData.getAllWaypoints().entrySet()) {
               QuestClientData.WaypointInfo wp = (QuestClientData.WaypointInfo)entry.getValue();
               BlockPos waypointPos = wp.pos;
               double dx = (double)waypointPos.getX() + (double)0.5F - player.posX;
               double dz = (double)waypointPos.getZ() + (double)0.5F - player.posZ;
               double distSq = dx * dx + dz * dz;
               if (!(distSq > (double)1048576.0F)) {
                  double renderX = (double)waypointPos.getX() + (double)0.5F - playerX;
                  double renderY = (double)waypointPos.getY() - playerY;
                  double renderZ = (double)waypointPos.getZ() + (double)0.5F - playerZ;
                  float[] color = this.getBeamColor(wp.type);
                  this.renderBeam(renderX, renderY, renderZ, color);
               }
            }

         }
      }
   }

   private void renderBeam(double x, double y, double z, float[] color) {
      GlStateManager.pushMatrix();
      GlStateManager.disableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.disableDepth();
      GlStateManager.depthMask(false);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      float alpha = 0.35F;
      float hw = 0.2F;
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(x - (double)hw, y, z - (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x - (double)hw, y + (double)256.0F, z - (double)hw).color(color[0], color[1], color[2], alpha * 0.3F).endVertex();
      buffer.pos(x + (double)hw, y + (double)256.0F, z - (double)hw).color(color[0], color[1], color[2], alpha * 0.3F).endVertex();
      buffer.pos(x + (double)hw, y, z - (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x + (double)hw, y, z + (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x + (double)hw, y + (double)256.0F, z + (double)hw).color(color[0], color[1], color[2], alpha * 0.3F).endVertex();
      buffer.pos(x - (double)hw, y + (double)256.0F, z + (double)hw).color(color[0], color[1], color[2], alpha * 0.3F).endVertex();
      buffer.pos(x - (double)hw, y, z + (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x + (double)hw, y, z - (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x + (double)hw, y + (double)256.0F, z - (double)hw).color(color[0], color[1], color[2], alpha * 0.3F).endVertex();
      buffer.pos(x + (double)hw, y + (double)256.0F, z + (double)hw).color(color[0], color[1], color[2], alpha * 0.3F).endVertex();
      buffer.pos(x + (double)hw, y, z + (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x - (double)hw, y, z + (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      buffer.pos(x - (double)hw, y + (double)256.0F, z + (double)hw).color(color[0], color[1], color[2], alpha * 0.3F).endVertex();
      buffer.pos(x - (double)hw, y + (double)256.0F, z - (double)hw).color(color[0], color[1], color[2], alpha * 0.3F).endVertex();
      buffer.pos(x - (double)hw, y, z - (double)hw).color(color[0], color[1], color[2], alpha).endVertex();
      tessellator.draw();
      float owh = 0.6F;
      float outerAlpha = 0.12F;
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

   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
      if (event.getType() == ElementType.ALL) {
         if (QuestClientData.hasWaypoint()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen == null) {
               EntityPlayer player = mc.player;
               if (player != null) {
                  ScaledResolution sr = new ScaledResolution(mc);
                  int screenWidth = sr.getScaledWidth();
                  int hudX = screenWidth - 75;
                  int hudY = 8;
                  if (QuestTrackerOverlay.lastOverlayHeight > 0) {
                     hudY = 5 + QuestTrackerOverlay.lastOverlayHeight + 8;
                  }

                  GlStateManager.pushMatrix();
                  GlStateManager.enableBlend();
                  List<String> slotOrder = new ArrayList();

                  for(String key : QuestClientData.getAllActiveQuests().keySet()) {
                     if (key.startsWith("story:")) {
                        slotOrder.add(key);
                     }
                  }

                  for(String s : ARROW_SLOT_PRIORITY_NON_STORY) {
                     slotOrder.add(s);
                  }

                  int drawn = 0;

                  for(String slot : slotOrder) {
                     if (drawn >= 5) {
                        break;
                     }

                     QuestClientData.ActiveQuestState aq = (QuestClientData.ActiveQuestState)QuestClientData.getAllActiveQuests().get(slot);
                     if (aq != null) {
                        QuestClientData.WaypointInfo wp = QuestClientData.getWaypointForQuest(aq.questId);
                        if (wp != null) {
                           hudY = this.drawArrowEntry(mc, player, wp, getSlotColor(slot), getSlotLabel(slot), hudX, hudY);
                           ++drawn;
                        }
                     }
                  }

                  for(Map.Entry<String, QuestClientData.WaypointInfo> wpEntry : QuestClientData.getAllWaypoints().entrySet()) {
                     if (drawn >= 5) {
                        break;
                     }

                     String wpKey = (String)wpEntry.getKey();
                     if (wpKey.startsWith("akatsuki_mission_") || wpKey.equals("leader_mission")) {
                        String label = wpKey.equals("leader_mission") ? "ORDER" : "AK";
                        hudY = this.drawArrowEntry(mc, player, (QuestClientData.WaypointInfo)wpEntry.getValue(), -3399134, label, hudX, hudY);
                        ++drawn;
                     }
                  }

                  if (drawn < 5) {
                     QuestClientData.WaypointInfo territoryWp = QuestClientData.getWaypointForQuest("territory_zone");
                     if (territoryWp != null) {
                        this.drawArrowEntry(mc, player, territoryWp, -11149910, "ZONE", hudX, hudY);
                        ++drawn;
                     }
                  }

                  GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                  GlStateManager.disableBlend();
                  GlStateManager.popMatrix();
               }
            }
         }
      }
   }

   private int drawArrowEntry(Minecraft mc, EntityPlayer player, QuestClientData.WaypointInfo wp, int color, String slotLabel, int hudX, int hudY) {
      double dx = (double)wp.pos.getX() + (double)0.5F - player.posX;
      double dz = (double)wp.pos.getZ() + (double)0.5F - player.posZ;
      int distance = (int)Math.sqrt(dx * dx + dz * dz);
      float angleToWaypoint = (float)(Math.atan2(dz, dx) * (double)180.0F / Math.PI) - 90.0F;
      float playerYaw = player.rotationYaw % 360.0F;

      float relativeAngle;
      for(relativeAngle = angleToWaypoint - playerYaw; relativeAngle > 180.0F; relativeAngle -= 360.0F) {
      }

      while(relativeAngle < -180.0F) {
         relativeAngle += 360.0F;
      }

      String arrow;
      if (!(relativeAngle > 150.0F) && !(relativeAngle < -150.0F)) {
         if (relativeAngle > 30.0F) {
            arrow = "→";
         } else if (relativeAngle < -30.0F) {
            arrow = "�?";
         } else {
            arrow = "↑";
         }
      } else {
         arrow = "↓";
      }

      float scale = 2.0F;
      int arrowWidth = mc.fontRenderer.getStringWidth(arrow);
      float arrowDrawX = (float)hudX - (float)arrowWidth * scale / 2.0F;
      GlStateManager.pushMatrix();
      GlStateManager.translate(arrowDrawX, (float)hudY, 0.0F);
      GlStateManager.scale(scale, scale, 1.0F);
      mc.fontRenderer.drawStringWithShadow(arrow, 0.0F, 0.0F, color);
      GlStateManager.popMatrix();
      int textY = hudY + (int)(10.0F * scale);
      String text = slotLabel + " " + distance + "m";
      int textWidth = mc.fontRenderer.getStringWidth(text);
      mc.fontRenderer.drawStringWithShadow(text, (float)(hudX - textWidth / 2), (float)textY, color);
      return hudY + (int)(10.0F * scale) + 14;
   }

   private static int getSlotColor(String slot) {
      if (slot.startsWith("story")) {
         return -22016;
      } else if ("outpost_mission".equals(slot)) {
         return -43691;
      } else if ("bingo_hunt".equals(slot)) {
         return -30669;
      } else if ("incursion_event".equals(slot)) {
         return -11158563;
      } else if ("contract_mission".equals(slot)) {
         return -8875;
      } else if (slot.startsWith("daily")) {
         return -11162881;
      } else if (slot.startsWith("weekly")) {
         return -5614081;
      } else {
         return "random".equals(slot) ? -11141291 : -22016;
      }
   }

   private static String getSlotLabel(String slot) {
      if (slot.startsWith("story")) {
         return "STORY";
      } else if ("outpost_mission".equals(slot)) {
         return "OUTPOST";
      } else if ("bingo_hunt".equals(slot)) {
         return "BINGO";
      } else if ("incursion_event".equals(slot)) {
         return "INCURSION";
      } else if ("contract_mission".equals(slot)) {
         return "CONTRACT";
      } else if (slot.startsWith("daily")) {
         return "DAILY";
      } else if (slot.startsWith("weekly")) {
         return "WEEKLY";
      } else {
         return "random".equals(slot) ? "RANDOM" : "QUEST";
      }
   }

   private float[] getBeamColor(WaypointData.WaypointType type) {
      if (type == null) {
         return new float[]{0.27F, 0.27F, 1.0F};
      } else {
         switch (type) {
            case TRAVEL:
               return new float[]{0.27F, 0.27F, 1.0F};
            case COMBAT:
               return new float[]{1.0F, 0.27F, 0.27F};
            case DIALOG:
               return new float[]{0.27F, 1.0F, 0.27F};
            case INTERACT:
               return new float[]{1.0F, 1.0F, 0.27F};
            case PVP_TARGET:
               return new float[]{1.0F, 0.27F, 0.27F};
            case PVP_BINGO:
               return new float[]{1.0F, 0.53F, 0.27F};
            case PVP_MUTUAL:
               return new float[]{0.67F, 0.27F, 1.0F};
            default:
               return new float[]{0.27F, 0.27F, 1.0F};
         }
      }
   }
}
