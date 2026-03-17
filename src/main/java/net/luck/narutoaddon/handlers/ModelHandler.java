package net.luck.narutoaddon.handlers;

import net.luck.narutoaddon.init.ModItems;
import net.luck.narutoaddon.entity.EntityShadowSpike;
import net.luck.narutoaddon.client.renderer.RenderShadowSpike;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = "narutoaddon", value = Side.CLIENT)
public class ModelHandler {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        // Usa ModItems invece di RegistryHandler
        registerModel(ModItems.SHADOW_RELEASE);
    }

    private static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}