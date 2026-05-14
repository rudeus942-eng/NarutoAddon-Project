package net.luck.narutoaddon;

import net.luck.narutoaddon.StatMenu.INinjaStats;
import net.luck.narutoaddon.StatMenu.NinjaStats;
import net.luck.narutoaddon.StatMenu.NinjaStatsStorage;
import net.luck.narutoaddon.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "narutoaddon", name = "Lucks Naruto Addon", version = "1.0", dependencies = "required-after:narutomod")
public class NarutoAddon {

    @Mod.Instance
    public static NarutoAddon instance;

    @SidedProxy(
            clientSide = "net.luck.narutoaddon.proxy.ClientProxy",
            serverSide = "net.luck.narutoaddon.proxy.ServerProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // --- 1. REGISTRAZIONE CAPABILITY (Fondamentale per evitare il NullPointerException) ---
        CapabilityManager.INSTANCE.register(INinjaStats.class, new NinjaStatsStorage(), NinjaStats.class);

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // --- 2. REGISTRAZIONE EVENTI STATISTICHE ---
        MinecraftForge.EVENT_BUS.register(new net.luck.narutoaddon.StatMenu.StatEvents());

        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
        System.out.println("NARUTO ADDON: Caricamento completato con successo!");
    }
}