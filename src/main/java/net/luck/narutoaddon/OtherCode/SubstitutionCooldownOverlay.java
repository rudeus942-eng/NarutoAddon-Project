
package net.luck.narutoaddon.OtherCode;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ElementsInfTsukAddon.ModElement.Tag
public class SubstitutionCooldownOverlay extends ElementsInfTsukAddon.ModElement {
   public static long clientCooldownEnd = 0L;
   private static final int COOLDOWN_DURATION = 600;

   public SubstitutionCooldownOverlay(ElementsInfTsukAddon instance) {
      super(instance, 10);
   }

   public void preInit(FMLPreInitializationEvent event) {
      this.elements.addNetworkMessage(CooldownSyncMessageHandler.class, CooldownSyncMessage.class, Side.CLIENT);
   }

   @SideOnly(Side.CLIENT)
   public void init(FMLInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new CooldownRenderer());
   }

   public static class CooldownSyncMessage implements IMessage {
      public long cooldownEndTime;

      public CooldownSyncMessage() {
      }

      public CooldownSyncMessage(long cooldownEndTime) {
         this.cooldownEndTime = cooldownEndTime;
      }

      public void toBytes(ByteBuf buf) {
         buf.writeLong(this.cooldownEndTime);
      }

      public void fromBytes(ByteBuf buf) {
         this.cooldownEndTime = buf.readLong();
      }
   }

   public static class CooldownSyncMessageHandler implements IMessageHandler<CooldownSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(CooldownSyncMessage message, MessageContext context) {
         Minecraft.getMinecraft().addScheduledTask(() -> SubstitutionCooldownOverlay.clientCooldownEnd = message.cooldownEndTime);
         return null;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class CooldownRenderer extends Gui {
      @SubscribeEvent
      public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
         if (event.getType() == ElementType.HOTBAR) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.player;
            if (player != null && mc.world != null) {
               long currentTime = mc.world.getTotalWorldTime();
               long ticksRemaining = SubstitutionCooldownOverlay.clientCooldownEnd - currentTime;
               if (ticksRemaining > 0L) {
                  float progress = 1.0F - (float)ticksRemaining / 600.0F;
                  ScaledResolution resolution = new ScaledResolution(mc);
                  int screenWidth = resolution.getScaledWidth();
                  int screenHeight = resolution.getScaledHeight();
                  int barWidth = 81;
                  int barHeight = 5;
                  int barX = screenWidth / 2 + 10;
                  int barY = screenHeight - 39 - 12;
                  GlStateManager.pushMatrix();
                  GlStateManager.disableTexture2D();
                  GlStateManager.enableBlend();
                  drawRect(barX - 1, barY + barHeight, barX + barWidth + 1, barY + barHeight + 1, -11513776);
                  drawRect(barX + barWidth, barY - 1, barX + barWidth + 1, barY + barHeight + 1, -11513776);
                  drawRect(barX - 1, barY - 1, barX + barWidth, barY, -14671840);
                  drawRect(barX - 1, barY - 1, barX, barY + barHeight + 1, -14671840);
                  drawRect(barX, barY, barX + barWidth, barY + 1, -15724528);
                  drawRect(barX, barY, barX + 1, barY + barHeight, -15724528);
                  drawRect(barX, barY + barHeight - 1, barX + barWidth, barY + barHeight, -13092808);
                  drawRect(barX + barWidth - 1, barY, barX + barWidth, barY + barHeight, -13092808);
                  drawRect(barX + 1, barY + 1, barX + barWidth - 1, barY + barHeight - 1, -16119286);
                  int filledWidth = (int)((float)(barWidth - 2) * progress);
                  int barColor;
                  int highlightColor;
                  int shadowColor;
                  if (progress < 0.5F) {
                     int red = 255;
                     int green = (int)(255.0F * progress * 2.0F);
                     barColor = -16777216 | red << 16 | green << 8;
                     highlightColor = -16777216 | Math.min(255, red + 60) << 16 | Math.min(255, green + 60) << 8;
                     shadowColor = -16777216 | Math.max(0, red - 80) << 16 | Math.max(0, green - 80) << 8;
                  } else {
                     int red = (int)(255.0F * (1.0F - (progress - 0.5F) * 2.0F));
                     int green = 255;
                     barColor = -16777216 | red << 16 | green << 8;
                     highlightColor = -16777216 | Math.min(255, red + 60) << 16 | Math.min(255, green + 60) << 8;
                     shadowColor = -16777216 | Math.max(0, red - 80) << 16 | Math.max(0, green - 80) << 8;
                  }

                  if (filledWidth > 0) {
                     int fillX = barX + 1;
                     int fillY = barY + 1;
                     int fillHeight = barHeight - 2;
                     drawRect(fillX, fillY, fillX + filledWidth, fillY + fillHeight, barColor);
                     drawRect(fillX, fillY, fillX + filledWidth, fillY + 1, highlightColor);
                     drawRect(fillX, fillY + fillHeight - 1, fillX + filledWidth, fillY + fillHeight, shadowColor);
                  }

                  GlStateManager.enableTexture2D();
                  GlStateManager.disableBlend();
                  GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                  mc.getTextureManager().bindTexture(Gui.ICONS);
                  GlStateManager.popMatrix();
               }
            }
         }
      }
   }
}
