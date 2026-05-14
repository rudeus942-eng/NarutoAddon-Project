package net.luck.narutoaddon.client;

import net.luck.narutoaddon.client.gui.GuiStatMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = "narutoaddon", value = Side.CLIENT)
public class KeyHandler {

    // Non inizializzarlo qui con = new KeyBinding(...)!
    public static KeyBinding OPEN_MENU;

    public static void registerKeys() {
        // Inizializzalo solo quando viene chiamato esplicitamente nel ClientProxy o nel Main
        OPEN_MENU = new KeyBinding("key.narutoaddon.open_menu", Keyboard.KEY_N, "key.categories.narutoaddon");
        ClientRegistry.registerKeyBinding(OPEN_MENU);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        // Aggiungiamo un controllo di sicurezza per evitare NullPointerException
        if (OPEN_MENU != null && OPEN_MENU.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();

            // Apriamo la GUI solo se non ce n'è già una aperta
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new GuiStatMenu());
            }
        }
    }
}