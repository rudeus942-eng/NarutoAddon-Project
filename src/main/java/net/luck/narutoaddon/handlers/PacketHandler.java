package net.luck.narutoaddon.handlers;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    // Commenta l'istanza finché non avrai un nuovo pacchetto pronto
    // public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("narutoaddon_chan");

    private static int packetId = 0;

    public static void registerMessages() {
        // Vuoto
    }
}
