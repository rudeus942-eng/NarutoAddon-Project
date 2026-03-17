package net.luck.narutoaddon.client.renderer;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerShadowCloak implements LayerRenderer<EntityPlayer> {
    private final RenderPlayer renderPlayer;

    public LayerShadowCloak(RenderPlayer renderPlayer) {
        this.renderPlayer = renderPlayer;
    }

    @Override
    public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        if (player == null || player.isDead || player.isInvisible()) return;

        // Attivazione basata sugli effetti del jutsu (Segnale per il mantello d'ombra)
        if (!player.isPotionActive(MobEffects.SPEED) || !player.isPotionActive(MobEffects.RESISTANCE)) return;

        ModelPlayer model = this.renderPlayer.getMainModel();
        if (model == null) return;

        try {
            GlStateManager.pushMatrix();

            // Setup Rendering Ombra (Trasparente)
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();

            // Nero al 70% di opacità
            GlStateManager.color(0.0F, 0.0F, 0.0F, 0.7F);

            // Prevenzione Z-Fighting (Sfarfallio tra la pelle e l'ombra)
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(-1.0F, -10.0F);

            // Scala leggermente maggiorata per "avvolgere" il corpo del player
            float s = 1.04F;
            GlStateManager.scale(s, s, s);

            // --- CONFIGURAZIONE MANUALE DEL MODELLO ---
            model.swingProgress = player.getSwingProgress(partialTicks);
            model.isRiding = player.isRiding();
            model.isChild = player.isChild();

            // Applichiamo le rotazioni standard per far muovere il mantello con il giocatore
            model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player);

            // --- RENDERING SICURO (NULL-CHECK) ---
            // Renderizziamo ogni parte solo se esiste per evitare NullPointerException
            if (model.bipedHead != null) model.bipedHead.render(scale);
            if (model.bipedBody != null) model.bipedBody.render(scale);
            if (model.bipedRightArm != null) model.bipedRightArm.render(scale);
            if (model.bipedLeftArm != null) model.bipedLeftArm.render(scale);
            if (model.bipedRightLeg != null) model.bipedRightLeg.render(scale);
            if (model.bipedLeftLeg != null) model.bipedLeftLeg.render(scale);
            if (model.bipedHeadwear != null) model.bipedHeadwear.render(scale);

            // Pulizia degli stati grafici per non rompere il resto del gioco
            GlStateManager.disablePolygonOffset();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            GlStateManager.popMatrix();

        } catch (Exception e) {
            // Se qualcosa fallisce nonostante i controlli, chiudiamo la matrice per evitare errori a cascata
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}