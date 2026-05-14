
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
public class ItemScrollInfiniteVoid extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:scroll_infinite_void")
   public static final Item block = null;

   public ItemScrollInfiniteVoid(ElementsInfTsukAddon instance) {
      super(instance, 954);
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
         this.setTranslationKey("scroll_infinite_void");
         this.setRegistryName("scroll_infinite_void");
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
         tooltip.add("§5§l§oDomain Expansion Scroll");
         tooltip.add("§7Teaches: §dUnlimited Void");
         tooltip.add("§8A 20-block sphere of sealed space");
         tooltip.add("§8that overwhelms all within for 2 minutes.");
         tooltip.add("§8Requires: §5Limitless Release §8item");
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

            boolean unlocked = ItemLimitlessRelease.unlockJutsu((EntityPlayerMP)player, ItemLimitlessRelease.INFINITE_VOID.index);
            if (unlocked) {
               stack.shrink(1);
               player.sendMessage(new TextComponentString("§5§l★ Domain Expansion Learned! §r§dUnlimited Void §ahas been unlocked on your Limitless Release!"));
               world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0F, 0.7F);
               if (world instanceof WorldServer) {
                  ((WorldServer)world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, player.posX, player.posY + (double)1.5F, player.posZ, 60, (double)1.0F, (double)1.0F, (double)1.0F, 0.15, new int[0]);
               }
            } else {
               player.sendMessage(new TextComponentString("§cYou must have a §lLimitless Release §r§citem in your inventory to learn this!"));
            }
         }

         return new ActionResult(EnumActionResult.SUCCESS, stack);
      }
   }
}
