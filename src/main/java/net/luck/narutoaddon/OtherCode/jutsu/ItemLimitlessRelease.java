
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
public class ItemLimitlessRelease extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:limitless_release")
   public static final Item block = null;
   public static final ItemJutsu.JutsuEnum RED = new ItemJutsu.JutsuEnum(0, "entity.limitless_red.name", 'S', (double)70.0F, new EntityLimitlessRed.Jutsu());
   public static final ItemJutsu.JutsuEnum BLUE = new ItemJutsu.JutsuEnum(1, "entity.limitless_blue.name", 'S', (double)85.0F, new EntityLimitlessBlue.Jutsu());
   public static final ItemJutsu.JutsuEnum PURPLE = new ItemJutsu.JutsuEnum(2, "entity.limitless_purple.name", 'S', (double)110.0F, new EntityLimitlessPurple.Jutsu());
   public static final ItemJutsu.JutsuEnum INFINITE_VOID = new ItemJutsu.JutsuEnum(3, "entity.domain_infinite_void.name", 'S', (double)200.0F, new EntityDomainInfiniteVoid.Jutsu());
   public static final ItemJutsu.JutsuEnum BLUE_NORMAL = new ItemJutsu.JutsuEnum(4, "entity.limitless_blue_normal.name", 'A', (double)45.0F, new EntityLimitlessBlueNormal.Jutsu());
   public static final ItemJutsu.JutsuEnum BLUE_AMPLIFIED_PALM = new ItemJutsu.JutsuEnum(5, "jutsu.blue_amplified_palm.name", 'A', (double)40.0F, new TwinLionModeHandler.Jutsu());

   public ItemLimitlessRelease(ElementsInfTsukAddon instance) {
      super(instance, 947);
   }

   public void initElements() {
      this.elements.items.add((Supplier)() -> new RangedItem());
   }

   @SideOnly(Side.CLIENT)
   public void registerModels(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("inftsukaddon:limitless_release", "inventory"));
   }

   public static boolean unlockJutsu(EntityPlayerMP player, int jutsuIndex) {
      Item limitlessItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("inftsukaddon", "limitless_release"));
      if (limitlessItem == null) {
         return false;
      } else {
         boolean found = false;

         for(int i = 0; i < player.inventory.getSizeInventory(); ++i) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == limitlessItem) {
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
         super(Type.SHAKUTON, new ItemJutsu.JutsuEnum[]{ItemLimitlessRelease.RED, ItemLimitlessRelease.BLUE, ItemLimitlessRelease.PURPLE, ItemLimitlessRelease.INFINITE_VOID, ItemLimitlessRelease.BLUE_NORMAL, ItemLimitlessRelease.BLUE_AMPLIFIED_PALM});
         this.setTranslationKey("limitless_release");
         this.setRegistryName("limitless_release");
         this.setCreativeTab(CreativeTabs.COMBAT);
         this.maxStackSize = 1;
         this.defaultCooldownMap[ItemLimitlessRelease.RED.index] = -1L;
         this.defaultCooldownMap[ItemLimitlessRelease.BLUE.index] = -1L;
         this.defaultCooldownMap[ItemLimitlessRelease.PURPLE.index] = -1L;
         this.defaultCooldownMap[ItemLimitlessRelease.INFINITE_VOID.index] = -1L;
         this.defaultCooldownMap[ItemLimitlessRelease.BLUE_NORMAL.index] = -1L;
         this.defaultCooldownMap[ItemLimitlessRelease.BLUE_AMPLIFIED_PALM.index] = -1L;
      }

      @SideOnly(Side.CLIENT)
      public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
         super.addInformation(stack, world, tooltip, flag);
         tooltip.add("§5§lLimitless Release");
         tooltip.add("");
         tooltip.add("§c★ Red");
         tooltip.add("§8Compresses chakra into a crimson orb");
         tooltip.add("§8that detonates on impact. Bypasses armor.");
         tooltip.add("");
         tooltip.add("§9★ Max Lapse: Blue");
         tooltip.add("§8Anchors an attracting orb that pulses,");
         tooltip.add("§8pulling nearby foes toward it. Inverse of Red.");
         tooltip.add("");
         tooltip.add("§5★ Hollow Purple");
         tooltip.add("§8Red + Blue fused. Fires a trailing orb");
         tooltip.add("§8that creates a 5-second detonation zone.");
         tooltip.add("");
         tooltip.add("§d★ §oDomain Expansion: §dUnlimited Void");
         tooltip.add("§8Sealed sphere of infinite information —");
         tooltip.add("§8all inside overwhelmed for 2 minutes.");
      }
   }
}
