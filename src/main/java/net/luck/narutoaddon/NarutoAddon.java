package net.luck.narutoaddon;

//import net.luck.narutoaddon.OtherCode.luckAddonAddon;
import net.luck.narutoaddon.StatMenu.INinjaStats;
import net.luck.narutoaddon.StatMenu.NinjaStats;
import net.luck.narutoaddon.StatMenu.NinjaStatsStorage;
import net.luck.narutoaddon.network.PacketHandler;
import net.luck.narutoaddon.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "narutoaddon", name = "Lucks Naruto Addon", version = "1.0"/*, dependencies = "required-after:narutomod"*/)
public class NarutoAddon {
    public static final String MODID = "narutoaddon";

    @Mod.Instance("narutoaddon")
    public static NarutoAddon instance;

    @SidedProxy(
            clientSide = "net.luck.narutoaddon.proxy.ClientProxy",
            serverSide = "net.luck.narutoaddon.proxy.ServerProxy"
    )
    public static CommonProxy proxy;
    //public static luckAddonAddon luckElements;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        //luckElements = new luckAddonAddon();
        PacketHandler.init();
        CapabilityManager.INSTANCE.register(
                INinjaStats.class,
                new NinjaStatsStorage(),
                NinjaStats.class
        );

        //MinecraftForge.EVENT_BUS.register(luckElements);

        //luckElements.preInit(event);

        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // --- 2. REGISTRAZIONE EVENTI STATISTICHE ---
        MinecraftForge.EVENT_BUS.register(new net.luck.narutoaddon.StatMenu.StatEvents());
        //luckElements.init(event);

        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
        System.out.println("NARUTO ADDON: Caricamento completato con successo!");
    }
}