
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class ItemScrollRed extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:scroll_red")
   public static final Item block = null;

   public ItemScrollRed(ElementsInfTsukAddon instance) {
      super(instance, 946);
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
         this.maxStackSize = 1;
         this.setTranslationKey("scroll_red");
         this.setRegistryName("scroll_red");
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
         tooltip.add("§cS-rank Limitless Release Scroll");
         tooltip.add("§7Teaches: §cRed");
         tooltip.add("§8An orb of compressed chakra that");
         tooltip.add("§8grows in the hand and detonates on impact.");
         tooltip.add("§8Requires: §cLimitless Release §8item");
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

            boolean unlocked = ItemLimitlessRelease.unlockJutsu((EntityPlayerMP)player, ItemLimitlessRelease.RED.index);
            if (unlocked) {
               stack.shrink(1);
               player.sendMessage(new TextComponentString("§c§l★ Jutsu Learned! §r§cRed §ahas been unlocked on your Limitless Release!"));
               world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0F, 1.0F);
               if (world instanceof WorldServer) {
                  ((WorldServer)world).spawnParticle(EnumParticleTypes.FLAME, player.posX, player.posY + (double)1.5F, player.posZ, 30, (double)0.5F, (double)0.5F, (double)0.5F, 0.05, new int[0]);
               }
            } else {
               player.sendMessage(new TextComponentString("§cYou must have a §lLimitless Release §r§citem in your inventory to learn this jutsu!"));
            }
         }

         return new ActionResult(EnumActionResult.SUCCESS, stack);
      }
   }
}
