package net.luck.narutoaddon.proxy;

import net.luck.narutoaddon.client.renderer.LayerShadowCloak;
import net.luck.narutoaddon.handlers.RegistryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.common.MinecraftForge;
import java.util.Map;

public class ClientProxy implements CommonProxy {
    @Override
    public void preInit() {
        RegistryHandler.registerEntityRenders();
    }

    @Override
    public void init() {
        // Registrazione tasti
        net.luck.narutoaddon.client.KeyHandler.registerKeys();
        // Registrazione HUD Stats
        MinecraftForge.EVENT_BUS.register(new net.luck.narutoaddon.StatMenu.NinjaStatsHUD());
    }

    @Override
    public void postInit() {
        Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
        RenderPlayer defaultRender = skinMap.get("default");
        if (defaultRender != null) defaultRender.addLayer(new LayerShadowCloak(defaultRender));

        RenderPlayer slimRender = skinMap.get("slim");
        if (slimRender != null) slimRender.addLayer(new LayerShadowCloak(slimRender));
    }
}