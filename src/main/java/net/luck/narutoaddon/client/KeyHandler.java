package net.luck.narutoaddon.client;

import net.luck.narutoaddon.StatMenu.NinjaStatsHUD;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = "narutoaddon", value = Side.CLIENT)
public class KeyHandler {

    public static final KeyBinding OPEN_STATS = new KeyBinding("key.narutoaddon.stats", org.lwjgl.input.Keyboard.KEY_N, "key.categories.narutoaddon");

    public static void registerKeys() {
        ClientRegistry.registerKeyBinding(OPEN_STATS);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (OPEN_STATS.isPressed()) {
            NinjaStatsHUD.isVisible = !NinjaStatsHUD.isVisible;
        }
    }
}