
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class ItemScrollBlueNormal extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:scroll_blue_normal")
   public static final Item block = null;

   public ItemScrollBlueNormal(ElementsInfTsukAddon instance) {
      super(instance, 957);
   }

   public void initElements() {
      this.elements.items.add((Supplier)() -> new ItemCustom());
   }

   @SideOnly(Side.CLIENT)
   public void registerModels(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("narutomod:scroll_fire_stream", "inventory"));
   }

   public static class ItemCustom extends Item {
      public ItemCustom() {
         this.setMaxStackSize(1);
         this.setTranslationKey("scroll_blue_normal");
         this.setRegistryName("scroll_blue_normal");
         this.setCreativeTab(CreativeTabs.MISC);
      }

      public int getItemEnchantability() {
         return 0;
      }

      public int getMaxItemUseDuration(ItemStack stack) {
         return 0;
      }

      @SideOnly(Side.CLIENT)
      public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
         super.addInformation(stack, world, tooltip, flag);
         tooltip.add("§9A-rank Limitless Release Scroll");
         tooltip.add("§7Teaches: §9Lapse: Blue");
         tooltip.add("§8Held vortex that pulls foes toward YOU.");
         tooltip.add("§8Requires: §9Limitless Release §8item");
         tooltip.add("§8Right-click to learn");
      }

      public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
         ItemStack stack = player.getHeldItem(hand);
         if (!world.isRemote && player instanceof EntityPlayerMP) {
            Item limitlessItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("inftsukaddon", "limitless_release"));
            if (limitlessItem == null) {
               player.sendMessage(new TextComponentString("§cLimitless Release not found!"));
               return new ActionResult(EnumActionResult.FAIL, stack);
            }

            boolean ok = ItemLimitlessRelease.unlockJutsu((EntityPlayerMP)player, ItemLimitlessRelease.BLUE_NORMAL.index);
            if (ok) {
               stack.shrink(1);
               player.sendMessage(new TextComponentString("§9§l★ Jutsu Learned! §r§9Lapse: Blue §ahas been unlocked!"));
               world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else {
               player.sendMessage(new TextComponentString("§cYou must have a §lLimitless Release §r§citem in your inventory!"));
            }
         }

         return new ActionResult(EnumActionResult.SUCCESS, stack);
      }
   }
}
