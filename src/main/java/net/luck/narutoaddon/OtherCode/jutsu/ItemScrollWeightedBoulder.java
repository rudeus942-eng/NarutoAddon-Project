
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
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
import net.narutomod.item.ItemJutsu;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class ItemScrollWeightedBoulder extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:scroll_weighted_boulder")
   public static final Item block = null;

   public ItemScrollWeightedBoulder(ElementsInfTsukAddon instance) {
      super(instance, 274);
   }

   public void initElements() {
      this.elements.items.add((Supplier)() -> new ItemCustom());
   }

   @SideOnly(Side.CLIENT)
   public void registerModels(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("narutomod:scroll_earth_wall", "inventory"));
   }

   public static class ItemCustom extends Item {
      public ItemCustom() {
         this.setMaxStackSize(1);
         this.maxStackSize = 1;
         this.setTranslationKey("scroll_weighted_boulder");
         this.setRegistryName("scroll_weighted_boulder");
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
         tooltip.add("§6B-rank Earth Release Scroll");
         tooltip.add("§7Teaches: §eWeighted Boulder Technique");
         tooltip.add("§8Right-click to learn");
      }

      public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
         ItemStack stack = player.getHeldItem(hand);
         if (!world.isRemote && player instanceof EntityPlayerMP) {
            try {
               DotonPatcher.inject();
               Item dotonItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("narutomod", "doton"));
               if (dotonItem == null) {
                  player.sendMessage(new TextComponentString("§cEarth Release not found!"));
                  return new ActionResult(EnumActionResult.FAIL, stack);
               }

               Class<?> scrollClass = Class.forName("net.narutomod.gui.GuiNinjaScroll");
               Method enableMethod = scrollClass.getMethod("enableJutsu", EntityPlayer.class, ItemJutsu.Base.class, ItemJutsu.JutsuEnum.class, Boolean.TYPE);
               Object result = enableMethod.invoke((Object)null, player, (ItemJutsu.Base)dotonItem, DotonPatcher.WEIGHTED_BOULDER, true);
               if (result != null) {
                  stack.shrink(1);
                  player.sendMessage(new TextComponentString("§6§l★ Jutsu Learned! §r§eWeighted Boulder Technique §ahas been added to your Earth Release!"));
                  world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                  if (world instanceof WorldServer) {
                     ((WorldServer)world).spawnParticle(EnumParticleTypes.BLOCK_DUST, player.posX, player.posY + (double)1.5F, player.posZ, 30, (double)0.5F, (double)0.5F, (double)0.5F, 0.05, new int[]{Block.getStateId(Blocks.DIRT.getDefaultState())});
                  }
               } else {
                  player.sendMessage(new TextComponentString("§cYou must be a ninja to learn jutsu!"));
               }
            } catch (Exception e) {
               System.err.println("[InfTsuk] ScrollWeightedBoulder: Failed to enable jutsu: " + e.getMessage());
               e.printStackTrace();
               player.sendMessage(new TextComponentString("§cFailed to learn jutsu. Contact an admin."));
            }
         }

         return new ActionResult(EnumActionResult.SUCCESS, stack);
      }
   }
}
