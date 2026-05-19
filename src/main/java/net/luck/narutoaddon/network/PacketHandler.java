package net.luck.narutoaddon.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static SimpleNetworkWrapper INSTANCE;

    private static int nextId = 0;

    public static void init() {

        if (INSTANCE != null) return;

        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("narutoaddon");

        INSTANCE.registerMessage(
                PacketSyncShadowJutsu.Handler.class,
                PacketSyncShadowJutsu.class,
                nextId++,
                Side.SERVER
        );
    }
}