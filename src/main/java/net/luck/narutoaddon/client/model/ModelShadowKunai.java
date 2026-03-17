package net.luck.narutoaddon.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelShadowKunai extends ModelBase {
    public ModelRenderer handle;
    public ModelRenderer blade;
    public ModelRenderer ring;

    public ModelShadowKunai() {
        this.textureWidth = 32;
        this.textureHeight = 32;

        // Impugnatura (Handle)
        this.handle = new ModelRenderer(this, 0, 0);
        this.handle.addBox(-0.5F, -4.0F, -0.5F, 1, 4, 1);
        this.handle.setRotationPoint(0.0F, 0.0F, 0.0F);

        // Lama (Blade)
        this.blade = new ModelRenderer(this, 4, 0);
        this.blade.addBox(-1.5F, -10.0F, -0.5F, 3, 6, 1);
        this.blade.setRotationPoint(0.0F, 0.0F, 0.0F);

        // Anello (Ring)
        this.ring = new ModelRenderer(this, 12, 0);
        this.ring.addBox(-1.0F, 0.0F, -0.5F, 2, 2, 1);
        this.ring.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.handle.render(f5);
        this.blade.render(f5);
        this.ring.render(f5);
    }
}