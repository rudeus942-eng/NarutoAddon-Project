package net.luck.narutoaddon;

import net.luck.narutoaddon.Items.ShadowKg.ItemShadowRelease;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LuckTabs {

    public static final CreativeTabs LUCK_TAB = new CreativeTabs("luck_tab") {

        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack getIconItemStack() {
            net.minecraft.item.Item mioItem = ForgeRegistries.ITEMS.getValue(new net.minecraft.util.ResourceLocation("narutoaddon", "shadow_release"));
            if (mioItem != null) {
                return new ItemStack(mioItem);
            }
            return ItemStack.EMPTY;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack getTabIconItem() {
            net.minecraft.item.Item mioItem = ForgeRegistries.ITEMS.getValue(new net.minecraft.util.ResourceLocation("narutoaddon", "shadow_release"));

            if (mioItem != null) {
                return new ItemStack(mioItem);
            }
            return ItemStack.EMPTY;
        }
    };
}