
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
import net.narutomod.Particles;
import net.narutomod.Particles.Types;
import net.narutomod.item.ItemJutsu;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class ItemScrollWaterNeedles extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:scroll_water_needles")
   public static final Item block = null;

   public ItemScrollWaterNeedles(ElementsInfTsukAddon instance) {
      super(instance, 906);
   }

   public void initElements() {
      this.elements.items.add((Supplier)() -> new ItemCustom());
   }

   @SideOnly(Side.CLIENT)
   public void registerModels(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("narutomod:scroll_water_dragon", "inventory"));
   }

   public static class ItemCustom extends Item {
      public ItemCustom() {
         this.setMaxStackSize(1);
         this.maxStackSize = 1;
         this.setTranslationKey("scroll_water_needles");
         this.setRegistryName("scroll_water_needles");
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
         tooltip.add("§bB-rank Water Release Scroll");
         tooltip.add("§7Teaches: §bThousand Flying Water Needles");
         tooltip.add("§8Right-click to learn");
      }

      public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
         ItemStack stack = player.getHeldItem(hand);
         if (!world.isRemote && player instanceof EntityPlayerMP) {
            try {
               SuitonPatcher.inject();
               Item suitonItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("narutomod", "suiton"));
               if (suitonItem == null) {
                  player.sendMessage(new TextComponentString("§cWater Release not found!"));
                  return new ActionResult(EnumActionResult.FAIL, stack);
               }

               Class<?> scrollClass = Class.forName("net.narutomod.gui.GuiNinjaScroll");
               Method enableMethod = scrollClass.getMethod("enableJutsu", EntityPlayer.class, ItemJutsu.Base.class, ItemJutsu.JutsuEnum.class, Boolean.TYPE);
               Object result = enableMethod.invoke((Object)null, player, (ItemJutsu.Base)suitonItem, SuitonPatcher.WATER_NEEDLES, true);
               if (result != null) {
                  stack.shrink(1);
                  player.sendMessage(new TextComponentString("§6§l★ Jutsu Learned! §r§bThousand Flying Water Needles §ahas been added to your Water Release!"));
                  world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                  if (!player.world.isRemote) {
                     Particles.spawnParticle(world, Types.SMOKE, player.posX, player.posY + (double)1.5F, player.posZ, 20, 0.6, 0.6, 0.6, (double)0.0F, 0.1, (double)0.0F, new int[]{-2006533155, 20});
                  }
               } else {
                  player.sendMessage(new TextComponentString("§cYou must be a ninja to learn jutsu!"));
               }
            } catch (Exception e) {
               System.err.println("[InfTsuk] ScrollWaterNeedles: Failed to enable jutsu: " + e.getMessage());
               e.printStackTrace();
               player.sendMessage(new TextComponentString("§cFailed to learn jutsu. Contact an admin."));
            }
         }

         return new ActionResult(EnumActionResult.SUCCESS, stack);
      }
   }
}
