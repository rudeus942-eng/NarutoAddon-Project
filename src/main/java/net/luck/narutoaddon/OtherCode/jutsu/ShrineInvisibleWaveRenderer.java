package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ShrineInvisibleWaveRenderer extends Render<EntityShrineDismantle.EntityCustom> {
   public ShrineInvisibleWaveRenderer(RenderManager renderManager) {
      super(renderManager);
   }

   public void doRender(EntityShrineDismantle.EntityCustom entity, double x, double y, double z, float entityYaw, float partialTicks) {
   }

   protected ResourceLocation getEntityTexture(EntityShrineDismantle.EntityCustom entity) {
      return null;
   }
}
