package net.luck.narutoaddon.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * ShadowSpike - Tradotto dal JSON originale di AHZNB
 * Altezza totale: 31.5 pixel (circa 2 blocchi)
 */
public class ModelShadowSpike extends ModelBase {
    public ModelRenderer[] parts = new ModelRenderer[9];

    public ModelShadowSpike() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        // Elemento 0: Base (6, 0, 6) a (10, 6, 10)
        parts[0] = new ModelRenderer(this, 20, 51);
        parts[0].addBox(-2.0F, -6.0F, -2.0F, 4, 6, 4);
        parts[0].setRotationPoint(0.0F, 24.0F, 0.0F);

        // Elemento 1: (6.25, 6, 6.25) a (9.75, 11.5, 9.75)
        parts[1] = new ModelRenderer(this, 20, 46);
        parts[1].addBox(-1.75F, -11.5F, -1.75F, 3, 5, 3); // Altezza 5.5
        parts[1].setRotationPoint(0.0F, 24.0F, 0.0F);

        // Elemento 2: (6.5, 11.5, 6.5) a (9.5, 16.5, 9.5)
        parts[2] = new ModelRenderer(this, 21, 46);
        parts[2].addBox(-1.5F, -16.5F, -1.5F, 3, 5, 3);
        parts[2].setRotationPoint(0.0F, 24.0F, 0.0F);

        // Elemento 3: (6.75, 16.5, 6.75) a (9.25, 21, 9.25)
        parts[3] = new ModelRenderer(this, 22, 31);
        parts[3].addBox(-1.25F, -21.0F, -1.25F, 2, 4, 2);
        parts[3].setRotationPoint(0.0F, 24.0F, 0.0F);

        // Elemento 4: (7, 21, 7) a (9, 25, 9)
        parts[4] = new ModelRenderer(this, 22, 22);
        parts[4].addBox(-1.0F, -25.0F, -1.0F, 2, 4, 2);
        parts[4].setRotationPoint(0.0F, 24.0F, 0.0F);

        // Elemento 5: (7.25, 25, 7.25) a (8.75, 28, 8.75)
        parts[5] = new ModelRenderer(this, 23, 21);
        parts[5].addBox(-0.75F, -28.0F, -0.75F, 1, 3, 1);
        parts[5].setRotationPoint(0.0F, 24.0F, 0.0F);

        // Elemento 6: (7.5, 28, 7.5) a (8.5, 30, 8.5)
        parts[6] = new ModelRenderer(this, 23, 13);
        parts[6].addBox(-0.5F, -30.0F, -0.5F, 1, 2, 1);
        parts[6].setRotationPoint(0.0F, 24.0F, 0.0F);

        // Elemento 7: (7.75, 30, 7.75) a (8.25, 31, 8.25)
        parts[7] = new ModelRenderer(this, 24, 9);
        parts[7].addBox(-0.25F, -31.0F, -0.25F, 1, 1, 1);
        parts[7].setRotationPoint(0.0F, 24.0F, 0.0F);

        // Elemento 8: Punta finale (7.85, 31, 7.85) a (8.15, 31.5, 8.15)
        parts[8] = new ModelRenderer(this, 24, 7);
        parts[8].addBox(-0.15F, -31.5F, -0.15F, 0, 1, 0); // Quasi invisibile
        parts[8].setRotationPoint(0.0F, 24.0F, 0.0F);
    }

    @Override
    public void render(Entity entityIn, float f, float f1, float f2, float f3, float f4, float scale) {
        // Importiamo l'entità per leggere la scala personalizzata
        if (entityIn instanceof net.luck.narutoaddon.entity.EntityShadowSpike) {
            float entityScale = ((net.luck.narutoaddon.entity.EntityShadowSpike) entityIn).getSpikeScale();

            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            // Applichiamo la scala dell'entità prima di renderizzare le parti
            net.minecraft.client.renderer.GlStateManager.scale(entityScale, entityScale, entityScale);

            for (ModelRenderer part : parts) {
                if (part != null) part.render(scale);
            }

            net.minecraft.client.renderer.GlStateManager.popMatrix();
        } else {
            // Render di fallback se usato per altro (es. GUI o particelle)
            for (ModelRenderer part : parts) {
                if (part != null) part.render(scale);
            }
        }
    }
}