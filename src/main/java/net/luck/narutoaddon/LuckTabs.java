package net.luck.narutoaddon;

import net.luck.narutoaddon.init.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LuckTabs {

    public static final CreativeTabs LUCK_TAB = new CreativeTabs("luck_tab") {
        @SideOnly(Side.CLIENT)
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModItems.SHADOW_RELEASE);
        }

        @Override
        public String getTranslationKey() {
            return "Luck Tab";
        }
    };
}