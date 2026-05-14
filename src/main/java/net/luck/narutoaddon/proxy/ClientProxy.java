package net.luck.narutoaddon.proxy;

import net.luck.narutoaddon.client.KeyHandler;
import net.luck.narutoaddon.client.renderer.LayerShadowCloak;
import net.luck.narutoaddon.handlers.RegistryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;

import java.util.Map;

public class ClientProxy implements CommonProxy {

    @Override
    public void preInit() {
        // Registrazione dei render delle entità
        RegistryHandler.registerEntityRenders();
    }

    @Override
    public void init() {
        // 1. Inizializza il KeyBinding (Crea l'istanza del tasto)
        KeyHandler.registerKeys();
    }

    @Override
    public void postInit() {
        // Registrazione dei layer per il mantello/ombra sulle skin dei giocatori
        Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();

        RenderPlayer defaultRender = skinMap.get("default");
        if (defaultRender != null) {
            defaultRender.addLayer(new LayerShadowCloak(defaultRender));
        }

        RenderPlayer slimRender = skinMap.get("slim");
        if (slimRender != null) {
            slimRender.addLayer(new LayerShadowCloak(slimRender));
        }
    }
}