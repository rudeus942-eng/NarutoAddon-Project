package net.luck.narutoaddon.client.renderer;

import net.luck.narutoaddon.client.model.ModelShadowKunai;
import net.luck.narutoaddon.entity.EntityShadowKunai;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.model.ModelBase;

public class RenderShadowKunai extends Render<EntityShadowKunai> {

    // Cambia qui il percorso della texture se vuoi usare quella originale della mod
    private static final ResourceLocation TEXTURE = new ResourceLocation("narutomod", "textures/entity/kunai.png");

    protected ModelBase mainModel = new ModelShadowKunai();

    public RenderShadowKunai(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityShadowKunai entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();

        // 1. Posizionamento (Alzato leggermente per far vedere bene la lama)
        GlStateManager.translate(x, y + 0.37D, z);

        // 2. Colore Nero Ombra
        GlStateManager.color(0.0F, 0.0F, 0.0F, 1.0F);

        // 3. ROTAZIONE
        // Mantiene la rotazione del giocatore/entità sul suo asse
        GlStateManager.rotate(entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        // Inclinazione di 65 gradi (Invece di -90 che lo rendeva perfettamente verticale)
        // Usiamo un valore negativo o positivo a seconda di come è costruito il tuo modello
        GlStateManager.rotate(-45.0F, 1.0F, 0.0F, 0.0F);

        // 4. Scala
        GlStateManager.scale(1.0F, 1.0F, 1.0F);

        this.bindEntityTexture(entity);

        // 5. Rendering del modello
        this.mainModel.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);

        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityShadowKunai entity) {
        return TEXTURE;
    }
}