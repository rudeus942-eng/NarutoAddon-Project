
package net.luck.narutoaddon.OtherCode.item;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class ItemScrollofMoltenResistance extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:scrollofmoltenresistance")
   public static final Item block = null;

   public ItemScrollofMoltenResistance(ElementsInfTsukAddon instance) {
      super(instance, 6);
   }

   public void initElements() {
      this.elements.items.add((Supplier)() -> new ItemCustom());
   }

   public void preInit(FMLPreInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new RecipeRegistrar());
   }

   @SideOnly(Side.CLIENT)
   public void registerModels(ModelRegistryEvent event) {
      ModelResourceLocation model = new ModelResourceLocation("inftsukaddon:scrollofmoltenresistance", "inventory");
      ModelLoader.setCustomModelResourceLocation(block, 0, model);
      ModelLoader.setCustomModelResourceLocation(block, 1, model);
      ModelLoader.setCustomModelResourceLocation(block, 2, model);
   }

   public static class RecipeRegistrar {
      @SubscribeEvent
      public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
         event.getRegistry().register(new UpgradeRecipe(new ResourceLocation("inftsukaddon", "scroll_upgrade_tier2"), new ItemStack(ItemScrollofMoltenResistance.block, 1, 1), 0, 0));
         event.getRegistry().register(new UpgradeRecipe(new ResourceLocation("inftsukaddon", "scroll_upgrade_tier3"), new ItemStack(ItemScrollofMoltenResistance.block, 1, 2), 1, 1));
      }
   }

   public static class ItemCustom extends Item {
      public ItemCustom() {
         this.setTranslationKey("scrollofmoltenresistance");
         this.setRegistryName("scrollofmoltenresistance");
         this.setCreativeTab(CreativeTabs.COMBAT);
         this.setMaxStackSize(1);
         this.setHasSubtypes(true);
         this.setMaxDamage(0);
      }

      public String getTranslationKey(ItemStack stack) {
         int tier = stack.getMetadata() + 1;
         return super.getTranslationKey() + "_tier" + tier;
      }

      public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
         if (this.isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0));
            items.add(new ItemStack(this, 1, 1));
            items.add(new ItemStack(this, 1, 2));
         }

      }

      @SideOnly(Side.CLIENT)
      public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
         int tier = stack.getMetadata() + 1;
         int reduction = tier * 10;
         tooltip.add("§a" + reduction + "% Lava Release Resistance");
         if (tier < 3) {
            tooltip.add("§8Combine two of the same tier to upgrade");
         } else {
            tooltip.add("§6Maximum tier reached");
         }

      }

      public boolean hasEffect(ItemStack stack) {
         return false;
      }

      public static float getDamageMultiplier(ItemStack stack) {
         int tier = stack.getMetadata() + 1;
         return 1.0F - (float)tier * 0.1F;
      }
   }

   public static class UpgradeRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
      private final ItemStack output;
      private final int inputMeta1;
      private final int inputMeta2;

      public UpgradeRecipe(ResourceLocation name, ItemStack output, int inputMeta1, int inputMeta2) {
         this.setRegistryName(name);
         this.output = output;
         this.inputMeta1 = inputMeta1;
         this.inputMeta2 = inputMeta2;
      }

      public boolean matches(InventoryCrafting inv, World world) {
         boolean found1 = false;
         boolean found2 = false;
         int count = 0;

         for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
               ++count;
               if (stack.getItem() == ItemScrollofMoltenResistance.block && stack.getMetadata() == this.inputMeta1 && !found1) {
                  found1 = true;
               } else {
                  if (stack.getItem() != ItemScrollofMoltenResistance.block || stack.getMetadata() != this.inputMeta2 || found2) {
                     return false;
                  }

                  found2 = true;
               }
            }
         }

         return found1 && found2 && count == 2;
      }

      public ItemStack getCraftingResult(InventoryCrafting inv) {
         return this.output.copy();
      }

      public boolean canFit(int width, int height) {
         return width * height >= 2;
      }

      public ItemStack getRecipeOutput() {
         return this.output;
      }
   }
}
