package net.luck.narutoaddon.handlers;

import net.luck.narutoaddon.Items.ItemPurpleLightning;
import net.luck.narutoaddon.Items.ShadowKg.ItemShadowRelease;
import net.luck.narutoaddon.entity.EntityShadowKunai;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class RegistryHandler {

    public static final List<Item> ITEMS = new ArrayList<>();
    public static final Item PURPLE_LIGHTNING = new ItemPurpleLightning();
    public static final Item SHADOW_RELEASE = new ItemShadowRelease();

    public static void init() {
        if (ITEMS.isEmpty()) {
            PURPLE_LIGHTNING.setRegistryName(new ResourceLocation("narutoaddon", "purple_lightning"));
            PURPLE_LIGHTNING.setUnlocalizedName("purple_lightning");

            SHADOW_RELEASE.setRegistryName(new ResourceLocation("narutoaddon", "shadow_release"));
            SHADOW_RELEASE.setUnlocalizedName("shadow_release");

            ITEMS.add(PURPLE_LIGHTNING);
            ITEMS.add(SHADOW_RELEASE);
        }
    }

    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
        init();
        event.getRegistry().registerAll(ITEMS.toArray(new Item[0]));
    }

    private static int entityId = 500;

    @SubscribeEvent
    public static void onEntityRegister(RegistryEvent.Register<EntityEntry> event) {

        EntityEntry shadowKunai = EntityEntryBuilder.create()
                .entity(EntityShadowKunai.class)
                .name("shadow_kunai")
                .id(new ResourceLocation("narutoaddon", "shadow_kunai"), entityId++)
                .tracker(64, 20, true)
                .build();

        event.getRegistry().registerAll( shadowKunai);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegister(net.minecraftforge.client.event.ModelRegistryEvent event) {
        for (Item item : ITEMS) {
            // Usiamo i percorsi completi per evitare import in cima al file
            net.minecraft.client.renderer.block.model.ModelResourceLocation loc =
                    new net.minecraft.client.renderer.block.model.ModelResourceLocation(item.getRegistryName(), "inventory");
            net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(item, 0, loc);
        }
    }

    public static void registerEntities() {}

    @SideOnly(Side.CLIENT)
    public static void registerEntityRenders() {
        // Registrazione sicura per il Kunai
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                net.luck.narutoaddon.entity.EntityShadowKunai.class,
                manager -> new net.luck.narutoaddon.client.renderer.RenderShadowKunai(manager)
        );

    }
}