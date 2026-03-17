package net.luck.narutoaddon.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("shadowchannel");
    private static int nextId = 0;

    public static void registerMessages() {
        // Registriamo il nostro pacchetto di switch
        INSTANCE.registerMessage(PacketSyncShadowJutsu.Handler.class, PacketSyncShadowJutsu.class, nextId++, Side.SERVER);
    }
}