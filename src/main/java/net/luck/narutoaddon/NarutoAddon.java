package net.luck.narutoaddon;

import net.luck.narutoaddon.StatMenu.INinjaStats;
import net.luck.narutoaddon.StatMenu.NinjaStats;
import net.luck.narutoaddon.StatMenu.NinjaStatsStorage;
import net.luck.narutoaddon.StatMenu.StatEvents;
import net.luck.narutoaddon.network.PacketHandler;
import net.luck.narutoaddon.handlers.RegistryHandler;
import net.luck.narutoaddon.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = "narutoaddon", name = "Lucks Naruto Addon", version = "1.0", dependencies = "after:narutomod")
public class NarutoAddon {

    @Mod.Instance
    public static NarutoAddon instance;

    // Questa riga dice a Forge: "Usa ClientProxy se sei sul PC dell'utente, ServerProxy se sei su un server"
    @SidedProxy(
            clientSide = "net.luck.narutoaddon.proxy.ClientProxy",
            serverSide = "net.luck.narutoaddon.proxy.ServerProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Rimosso registerEntities perché lo facciamo già tramite gli eventi @SubscribeEvent
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Se hai commentato gli handler, non registrarli qui!
        // Forge li registra da solo se hanno @Mod.EventBusSubscriber

        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Chiamata al Proxy per i Layer (Mantello d'ombra)
        proxy.postInit();

        System.out.println("NARUTO ADDON: Caricamento completato con successo!");
    }
}