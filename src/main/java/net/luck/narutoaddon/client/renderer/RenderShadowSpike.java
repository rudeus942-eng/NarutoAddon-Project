package net.luck.narutoaddon.client.renderer;

import net.luck.narutoaddon.client.model.ModelShadowSpike;
import net.luck.narutoaddon.entity.EntityShadowSpike;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderShadowSpike extends Render<EntityShadowSpike> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("narutoaddon", "textures/entity/shadow_spike.png");
    protected ModelShadowSpike model = new ModelShadowSpike();

    public RenderShadowSpike(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityShadowSpike entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();

        // 1. Posizionamento nel mondo
        GlStateManager.translate((float)x, (float)y, (float)z);

        // 2. Applichiamo la rotazione Yaw (Y) salvata nell'entità
        // Usiamo entity.rotationYaw invece di entityYaw per coerenza con lo spawn
        GlStateManager.rotate(180.0F - entity.rotationYaw, 0.0F, 1.0F, 0.0F);

        // 3. Applichiamo l'inclinazione (Pitch) recuperata dal DataManager
        float pitch = entity.getSpikePitch();
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);

        // 4. Applichiamo la scala dinamica (casuale per ogni spillo)
        // Moltiplichiamo per i tuoi valori base (-1.5, -1.0, 2.5) per mantenere le proporzioni del modello
        float s = entity.getSpikeScale();
        GlStateManager.scale(-1.5F * s, -1.0F * s, 2.5F * s);

        // 5. Centramento del modello (offset per farlo uscire correttamente dal terreno)
        GlStateManager.translate(0.0F, -1.5F, 0.0F);

        this.bindEntityTexture(entity);
        GlStateManager.disableCull();

        // Render del modello
        this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityShadowSpike entity) {
        return TEXTURE;
    }
}