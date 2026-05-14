
package net.luck.narutoaddon.OtherCode.jutsu.domain;

import net.luck.narutoaddon.OtherCode.jutsu.EntityDomainInfiniteVoid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

@EventBusSubscriber(
   modid = "inftsukaddon",
   value = {Side.CLIENT}
)
public class DomainWorldRenderer {
   private static final ResourceLocation VOID_TEX = new ResourceLocation("inftsukaddon", "textures/blocks/domain_sky.png");
   private static final int SKYBOX_SUBDIVISIONS = 20;
   private static final int DOME_SUBDIVISIONS = 24;
   private static final float UV_MARGIN = 0.002F;
   private static Framebuffer skyboxFbo;
   private static int fboWidth;
   private static int fboHeight;
   private static final FloatBuffer MAT_BUF = BufferUtils.createFloatBuffer(16);
   private static final float[] PROJ = new float[16];
   private static final float[] MV = new float[16];
   private static final float[] MVP = new float[16];

   @SubscribeEvent
   public static void onRenderWorldLast(RenderWorldLastEvent event) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.world != null && mc.player != null) {
         double pX = mc.player.posX;
         double pY = mc.player.posY + (double)mc.player.getEyeHeight();
         double pZ = mc.player.posZ;

         for(Entity e : mc.world.loadedEntityList) {
            if (e instanceof EntityDomainInfiniteVoid.EntityCustom && !e.isDead) {
               double dx = e.posX - pX;
               double dy = e.posY - pY;
               double dz = e.posZ - pZ;
               double distSq = dx * dx + dy * dy + dz * dz;
               double innerR = (double)20.0F;
               if (distSq <= innerR * innerR) {
                  renderSkyboxToFbo(mc);
                  renderDomeWithFbo(mc, e, event.getPartialTicks());
                  reRenderParticles(mc, event.getPartialTicks());
                  return;
               }

               if (distSq <= (double)6400.0F) {
                  renderWhiteShell(mc, e, event.getPartialTicks());
               }
            }
         }

      }
   }

   private static void renderSkyboxToFbo(Minecraft mc) {
      int w = mc.displayWidth;
      int h = mc.displayHeight;
      if (w > 0 && h > 0) {
         if (skyboxFbo == null || fboWidth != w || fboHeight != h) {
            if (skyboxFbo != null) {
               skyboxFbo.deleteFramebuffer();
            }

            skyboxFbo = new Framebuffer(w, h, true);
            skyboxFbo.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
            fboWidth = w;
            fboHeight = h;
         }

         int prevFbo = GL11.glGetInteger(36006);
         skyboxFbo.framebufferClear();
         skyboxFbo.bindFramebuffer(true);
         GlStateManager.matrixMode(5889);
         GlStateManager.pushMatrix();
         GlStateManager.loadIdentity();
         float aspect = (float)w / (float)h;
         double fov = (double)mc.gameSettings.fovSetting;
         double fovRad = Math.toRadians(fov);
         double top = 0.05 * Math.tan(fovRad / (double)2.0F);
         double right = top * (double)aspect;
         GL11.glFrustum(-right, right, -top, top, 0.05, (double)300.0F);
         GlStateManager.matrixMode(5888);
         GlStateManager.pushMatrix();
         GlStateManager.loadIdentity();
         GlStateManager.rotate(mc.player.rotationPitch, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(mc.player.rotationYaw + 180.0F, 0.0F, 1.0F, 0.0F);
         mc.getTextureManager().bindTexture(VOID_TEX);
         GlStateManager.glTexParameteri(3553, 10241, 9729);
         GlStateManager.glTexParameteri(3553, 10240, 9729);
         GlStateManager.glTexParameteri(3553, 10242, 33071);
         GlStateManager.glTexParameteri(3553, 10243, 33071);
         GlStateManager.enableTexture2D();
         GlStateManager.disableLighting();
         GlStateManager.disableCull();
         GlStateManager.disableBlend();
         GlStateManager.depthMask(true);
         GlStateManager.enableDepth();
         GlStateManager.depthFunc(515);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         drawSpherifiedCubeAtlas(100.0F, 20);
         GlStateManager.matrixMode(5889);
         GlStateManager.popMatrix();
         GlStateManager.matrixMode(5888);
         GlStateManager.popMatrix();
         OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, prevFbo);
         GL11.glViewport(0, 0, w, h);
      }
   }

   private static void drawSpherifiedCubeAtlas(float r, int subdivisions) {
      Tessellator tess = Tessellator.getInstance();
      BufferBuilder buf = tess.getBuffer();
      buf.begin(7, DefaultVertexFormats.POSITION_TEX);
      float cw = 0.33333334F;
      float m = 0.002F;
      subdividedFaceWithAtlasUV(buf, r, subdivisions, -r, -r, -r, r, -r, -r, r, -r, r, -r, -r, r, 0.002F, 0.002F, 0.33133334F, 0.498F);
      subdividedFaceWithAtlasUV(buf, r, subdivisions, -r, r, r, r, r, r, r, r, -r, -r, r, -r, 0.33533335F, 0.002F, 0.6646667F, 0.498F);
      subdividedFaceWithAtlasUV(buf, r, subdivisions, -r, r, r, -r, r, -r, -r, -r, -r, -r, -r, r, 0.66866666F, 0.002F, 0.998F, 0.498F);
      subdividedFaceWithAtlasUV(buf, r, subdivisions, r, r, -r, r, r, r, r, -r, r, r, -r, -r, 0.002F, 0.502F, 0.33133334F, 0.998F);
      subdividedFaceWithAtlasUV(buf, r, subdivisions, -r, r, -r, r, r, -r, r, -r, -r, -r, -r, -r, 0.33533335F, 0.502F, 0.6646667F, 0.998F);
      subdividedFaceWithAtlasUV(buf, r, subdivisions, r, r, r, -r, r, r, -r, -r, r, r, -r, r, 0.66866666F, 0.502F, 0.998F, 0.998F);
      tess.draw();
   }

   private static void subdividedFaceWithAtlasUV(BufferBuilder buf, float radius, int N, float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11, float x01, float y01, float z01, float uMin, float vMin, float uMax, float vMax) {
      for(int i = 0; i < N; ++i) {
         float s0 = (float)i / (float)N;
         float s1 = (float)(i + 1) / (float)N;

         for(int j = 0; j < N; ++j) {
            float t0 = (float)j / (float)N;
            float t1 = (float)(j + 1) / (float)N;
            emitAtlasVertex(buf, radius, s0, t0, x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01, uMin, vMin, uMax, vMax);
            emitAtlasVertex(buf, radius, s0, t1, x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01, uMin, vMin, uMax, vMax);
            emitAtlasVertex(buf, radius, s1, t1, x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01, uMin, vMin, uMax, vMax);
            emitAtlasVertex(buf, radius, s1, t0, x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01, uMin, vMin, uMax, vMax);
         }
      }

   }

   private static void emitAtlasVertex(BufferBuilder buf, float radius, float s, float t, float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11, float x01, float y01, float z01, float uMin, float vMin, float uMax, float vMax) {
      float w00 = (1.0F - s) * (1.0F - t);
      float w10 = s * (1.0F - t);
      float w11 = s * t;
      float w01 = (1.0F - s) * t;
      float cx = x00 * w00 + x10 * w10 + x11 * w11 + x01 * w01;
      float cy = y00 * w00 + y10 * w10 + y11 * w11 + y01 * w01;
      float cz = z00 * w00 + z10 * w10 + z11 * w11 + z01 * w01;
      float len = (float)Math.sqrt((double)(cx * cx + cy * cy + cz * cz));
      float x = cx / len * radius;
      float y = cy / len * radius;
      float z = cz / len * radius;
      float u = uMin + s * (uMax - uMin);
      float v = vMin + t * (vMax - vMin);
      buf.pos((double)x, (double)y, (double)z).tex((double)u, (double)v).endVertex();
   }

   private static void renderDomeWithFbo(Minecraft mc, Entity domainEntity, float partialTicks) {
      if (skyboxFbo != null) {
         double entX = domainEntity.lastTickPosX + (domainEntity.posX - domainEntity.lastTickPosX) * (double)partialTicks;
         double entY = domainEntity.lastTickPosY + (domainEntity.posY - domainEntity.lastTickPosY) * (double)partialTicks;
         double entZ = domainEntity.lastTickPosZ + (domainEntity.posZ - domainEntity.lastTickPosZ) * (double)partialTicks;
         double camX = TileEntityRendererDispatcher.staticPlayerX;
         double camY = TileEntityRendererDispatcher.staticPlayerY;
         double camZ = TileEntityRendererDispatcher.staticPlayerZ;
         float r = 19.5F;
         GL11.glBindTexture(3553, skyboxFbo.framebufferTexture);
         GlStateManager.glTexParameteri(3553, 10241, 9729);
         GlStateManager.glTexParameteri(3553, 10240, 9729);
         GlStateManager.glTexParameteri(3553, 10242, 33071);
         GlStateManager.glTexParameteri(3553, 10243, 33071);
         GlStateManager.pushMatrix();
         GlStateManager.translate(entX - camX, entY - camY, entZ - camZ);
         GlStateManager.enableTexture2D();
         GlStateManager.disableLighting();
         GlStateManager.disableCull();
         GlStateManager.disableBlend();
         GlStateManager.enableDepth();
         GlStateManager.depthMask(false);
         GlStateManager.depthFunc(515);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         captureMatrix(2983, PROJ);
         captureMatrix(2982, MV);
         multMatrix(PROJ, MV, MVP);
         Tessellator tess = Tessellator.getInstance();
         BufferBuilder buf = tess.getBuffer();
         buf.begin(7, DefaultVertexFormats.POSITION_TEX);
         drawSpherifiedCubeScreenSpace(buf, r, 24);
         tess.draw();
         GlStateManager.depthMask(true);
         GlStateManager.enableCull();
         GlStateManager.enableLighting();
         GlStateManager.popMatrix();
         mc.getTextureManager().bindTexture(VOID_TEX);
      }
   }

   private static void reRenderParticles(Minecraft mc, float partialTicks) {
      Entity viewEntity = mc.getRenderViewEntity();
      if (viewEntity != null) {
         try {
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableTexture2D();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0.0F, 0.0F);
            GL11.glTexEnvi(8960, 8704, 8448);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, 0.003921569F);
            GlStateManager.enableDepth();
            GlStateManager.depthFunc(515);
            GlStateManager.depthMask(false);
            GlStateManager.disableLighting();
            mc.effectRenderer.renderParticles(viewEntity, partialTicks);
            GlStateManager.depthMask(true);
         } catch (Throwable t) {
            t.printStackTrace();
         }

      }
   }

   private static void renderWhiteShell(Minecraft mc, Entity domainEntity, float partialTicks) {
      double entX = domainEntity.lastTickPosX + (domainEntity.posX - domainEntity.lastTickPosX) * (double)partialTicks;
      double entY = domainEntity.lastTickPosY + (domainEntity.posY - domainEntity.lastTickPosY) * (double)partialTicks;
      double entZ = domainEntity.lastTickPosZ + (domainEntity.posZ - domainEntity.lastTickPosZ) * (double)partialTicks;
      double camX = TileEntityRendererDispatcher.staticPlayerX;
      double camY = TileEntityRendererDispatcher.staticPlayerY;
      double camZ = TileEntityRendererDispatcher.staticPlayerZ;
      float r = 21.5F;
      GlStateManager.disableTexture2D();
      GlStateManager.pushMatrix();
      GlStateManager.translate(entX - camX, entY - camY, entZ - camZ);
      GlStateManager.disableLighting();
      GlStateManager.enableCull();
      GlStateManager.cullFace(CullFace.FRONT);
      GlStateManager.disableBlend();
      GlStateManager.enableDepth();
      GlStateManager.depthMask(true);
      GlStateManager.depthFunc(515);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      Tessellator tess = Tessellator.getInstance();
      BufferBuilder buf = tess.getBuffer();
      buf.begin(7, DefaultVertexFormats.POSITION);
      drawSpherifiedCubeSolid(buf, r, 24);
      tess.draw();
      GlStateManager.cullFace(CullFace.BACK);
      GlStateManager.enableTexture2D();
      GlStateManager.enableLighting();
      GlStateManager.popMatrix();
   }

   private static void drawSpherifiedCubeSolid(BufferBuilder buf, float r, int N) {
      subdivideFaceSolid(buf, r, N, -r, -r, -r, r, -r, -r, r, -r, r, -r, -r, r);
      subdivideFaceSolid(buf, r, N, -r, r, r, r, r, r, r, r, -r, -r, r, -r);
      subdivideFaceSolid(buf, r, N, -r, r, r, -r, r, -r, -r, -r, -r, -r, -r, r);
      subdivideFaceSolid(buf, r, N, r, r, -r, r, r, r, r, -r, r, r, -r, -r);
      subdivideFaceSolid(buf, r, N, -r, r, -r, r, r, -r, r, -r, -r, -r, -r, -r);
      subdivideFaceSolid(buf, r, N, r, r, r, -r, r, r, -r, -r, r, r, -r, r);
   }

   private static void subdivideFaceSolid(BufferBuilder buf, float radius, int N, float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11, float x01, float y01, float z01) {
      for(int i = 0; i < N; ++i) {
         float s0 = (float)i / (float)N;
         float s1 = (float)(i + 1) / (float)N;

         for(int j = 0; j < N; ++j) {
            float t0 = (float)j / (float)N;
            float t1 = (float)(j + 1) / (float)N;
            emitSolidVertex(buf, radius, s0, t0, x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01);
            emitSolidVertex(buf, radius, s0, t1, x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01);
            emitSolidVertex(buf, radius, s1, t1, x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01);
            emitSolidVertex(buf, radius, s1, t0, x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01);
         }
      }

   }

   private static void emitSolidVertex(BufferBuilder buf, float radius, float s, float t, float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11, float x01, float y01, float z01) {
      float w00 = (1.0F - s) * (1.0F - t);
      float w10 = s * (1.0F - t);
      float w11 = s * t;
      float w01 = (1.0F - s) * t;
      float cx = x00 * w00 + x10 * w10 + x11 * w11 + x01 * w01;
      float cy = y00 * w00 + y10 * w10 + y11 * w11 + y01 * w01;
      float cz = z00 * w00 + z10 * w10 + z11 * w11 + z01 * w01;
      float len = (float)Math.sqrt((double)(cx * cx + cy * cy + cz * cz));
      float x = cx / len * radius;
      float y = cy / len * radius;
      float z = cz / len * radius;
      buf.pos((double)x, (double)y, (double)z).endVertex();
   }

   private static void drawSpherifiedCubeScreenSpace(BufferBuilder buf, float r, int N) {
      subdivideFaceScreenSpace(buf, r, N, -r, -r, -r, r, -r, -r, r, -r, r, -r, -r, r);
      subdivideFaceScreenSpace(buf, r, N, -r, r, r, r, r, r, r, r, -r, -r, r, -r);
      subdivideFaceScreenSpace(buf, r, N, -r, r, r, -r, r, -r, -r, -r, -r, -r, -r, r);
      subdivideFaceScreenSpace(buf, r, N, r, r, -r, r, r, r, r, -r, r, r, -r, -r);
      subdivideFaceScreenSpace(buf, r, N, -r, r, -r, r, r, -r, r, -r, -r, -r, -r, -r);
      subdivideFaceScreenSpace(buf, r, N, r, r, r, -r, r, r, -r, -r, r, r, -r, r);
   }

   private static void subdivideFaceScreenSpace(BufferBuilder buf, float radius, int N, float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11, float x01, float y01, float z01) {
      for(int i = 0; i < N; ++i) {
         float s0 = (float)i / (float)N;
         float s1 = (float)(i + 1) / (float)N;

         for(int j = 0; j < N; ++j) {
            float t0 = (float)j / (float)N;
            float t1 = (float)(j + 1) / (float)N;
            emitScreenSpaceVertex(buf, radius, s0, t0, x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01);
            emitScreenSpaceVertex(buf, radius, s0, t1, x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01);
            emitScreenSpaceVertex(buf, radius, s1, t1, x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01);
            emitScreenSpaceVertex(buf, radius, s1, t0, x00, y00, z00, x10, y10, z10, x11, y11, z11, x01, y01, z01);
         }
      }

   }

   private static void emitScreenSpaceVertex(BufferBuilder buf, float radius, float s, float t, float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11, float x01, float y01, float z01) {
      float w00 = (1.0F - s) * (1.0F - t);
      float w10 = s * (1.0F - t);
      float w11 = s * t;
      float w01 = (1.0F - s) * t;
      float cx = x00 * w00 + x10 * w10 + x11 * w11 + x01 * w01;
      float cy = y00 * w00 + y10 * w10 + y11 * w11 + y01 * w01;
      float cz = z00 * w00 + z10 * w10 + z11 * w11 + z01 * w01;
      float len = (float)Math.sqrt((double)(cx * cx + cy * cy + cz * cz));
      float x = cx / len * radius;
      float y = cy / len * radius;
      float z = cz / len * radius;
      float clipX = MVP[0] * x + MVP[4] * y + MVP[8] * z + MVP[12];
      float clipY = MVP[1] * x + MVP[5] * y + MVP[9] * z + MVP[13];
      float clipW = MVP[3] * x + MVP[7] * y + MVP[11] * z + MVP[15];
      float u;
      float v;
      if (Math.abs(clipW) < 1.0E-4F) {
         u = 0.5F;
         v = 0.5F;
      } else {
         float invW = 1.0F / clipW;
         u = clipX * invW * 0.5F + 0.5F;
         v = clipY * invW * 0.5F + 0.5F;
      }

      buf.pos((double)x, (double)y, (double)z).tex((double)u, (double)v).endVertex();
   }

   private static void captureMatrix(int mode, float[] out) {
      MAT_BUF.clear();
      GL11.glGetFloat(mode, MAT_BUF);
      MAT_BUF.rewind();
      MAT_BUF.get(out);
   }

   private static void multMatrix(float[] a, float[] b, float[] out) {
      for(int row = 0; row < 4; ++row) {
         for(int col = 0; col < 4; ++col) {
            float sum = 0.0F;

            for(int k = 0; k < 4; ++k) {
               sum += a[row + k * 4] * b[k + col * 4];
            }

            out[row + col * 4] = sum;
         }
      }

   }
}
