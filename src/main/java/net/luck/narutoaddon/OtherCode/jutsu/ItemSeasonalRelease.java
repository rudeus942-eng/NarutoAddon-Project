
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemJutsu.JutsuEnum.Type;

import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class ItemSeasonalRelease extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:seasonal_release")
   public static final Item block = null;
   public static final ItemJutsu.JutsuEnum BLAZING_DECIMATION = new ItemJutsu.JutsuEnum(0, "entity.scorch_blazing_decimation.name", 'S', (double)65.0F, new EntityScorchBlazingDecimation.Jutsu());
   public static final ItemJutsu.JutsuEnum KIRIN = new ItemJutsu.JutsuEnum(1, "entity.kirin.name", 'S', (double)80.0F, new EntityKirin.Jutsu());
   public static final ItemJutsu.JutsuEnum SHADOW_REND = new ItemJutsu.JutsuEnum(2, "entity.shadow_rend.name", 'S', (double)80.0F, new EntityShadowRend.Jutsu());

   public ItemSeasonalRelease(ElementsInfTsukAddon instance) {
      super(instance, 942);
   }

   public void initElements() {
      this.elements.items.add((Supplier)() -> new RangedItem());
   }

   @SideOnly(Side.CLIENT)
   public void registerModels(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("narutomod:ninjutsu", "inventory"));
   }

   public static boolean unlockJutsu(EntityPlayerMP player, int jutsuIndex) {
      Item seasonalItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("inftsukaddon", "seasonal_release"));
      if (seasonalItem == null) {
         return false;
      } else {
         boolean found = false;

         for(int i = 0; i < player.inventory.getSizeInventory(); ++i) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == seasonalItem) {
               NBTTagCompound nbt = stack.getTagCompound();
               if (nbt == null) {
                  nbt = new NBTTagCompound();
                  stack.setTagCompound(nbt);
               }

               nbt.setLong("JutsuCDMapKey" + jutsuIndex, 0L);
               found = true;
            }
         }

         if (found) {
            player.inventoryContainer.detectAndSendChanges();
         }

         return found;
      }
   }

   public static class RangedItem extends ItemJutsu.Base {
      public RangedItem() {
         super(Type.SHAKUTON, new ItemJutsu.JutsuEnum[]{ItemSeasonalRelease.BLAZING_DECIMATION, ItemSeasonalRelease.KIRIN, ItemSeasonalRelease.SHADOW_REND});
         this.setTranslationKey("seasonal_release");
         this.setRegistryName("seasonal_release");
         this.setCreativeTab(CreativeTabs.COMBAT);
         this.maxStackSize = 1;
         this.defaultCooldownMap[ItemSeasonalRelease.BLAZING_DECIMATION.index] = 0L;
         this.defaultCooldownMap[ItemSeasonalRelease.KIRIN.index] = -1L;
         this.defaultCooldownMap[ItemSeasonalRelease.SHADOW_REND.index] = -1L;
      }

      @SideOnly(Side.CLIENT)
      public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
         super.addInformation(stack, world, tooltip, flag);
         tooltip.add("§6§lSeasonal Battle Pass Release");
         tooltip.add("");
         tooltip.add("§6Season 1:");
         tooltip.add("§c★ Scorch Release: Blazing Decimation");
         tooltip.add("§8Summons scorching orbs that orbit, then");
         tooltip.add("§8launch at your target, incinerating all.");
         tooltip.add("");
         tooltip.add("§6Season 2:");
         tooltip.add("§b★ Lightning Release: Kirin");
         tooltip.add("§8Summons a lightning dragon from the storm");
         tooltip.add("§8clouds to obliterate your target.");
         tooltip.add("");
         tooltip.add("§6Season 3:");
         tooltip.add("§8★ Shadow Release: Shadow Rend");
         tooltip.add("§8A cross-shaped cone of shadow pressure");
         tooltip.add("§8radiates from your palm, tearing through");
         tooltip.add("§8everything in its path. 48-block range, 22° spread.");
      }
   }
}
