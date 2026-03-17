package net.luck.narutoaddon.init;

import net.luck.narutoaddon.Items.ShadowKg.ItemShadowRelease;
import net.luck.narutoaddon.Items.ItemPurpleLightning;
import net.minecraft.item.Item;
import java.util.ArrayList;
import java.util.List;

public class ModItems {

    // Questa lista serve al RegistryHandler per registrare tutto in un colpo solo
    public static final List<Item> ITEMS = new ArrayList<Item>();

    // Qui dichiariamo i tuoi due Jutsu principali
    public static final Item SHADOW_RELEASE = new ItemShadowRelease();
    public static final Item PURPLE_LIGHT = new ItemPurpleLightning();

    static {
        // Li aggiungiamo alla lista
        ITEMS.add(SHADOW_RELEASE);
        ITEMS.add(PURPLE_LIGHT);
    }
}