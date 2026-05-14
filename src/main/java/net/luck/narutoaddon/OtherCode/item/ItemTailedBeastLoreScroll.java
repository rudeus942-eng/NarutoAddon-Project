
package net.luck.narutoaddon.OtherCode.item;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.entity.EntityBijuManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class ItemTailedBeastLoreScroll extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:tb_lore_scroll_5")
   public static final Item SCROLL_5 = null;
   @ObjectHolder("inftsukaddon:tb_lore_scroll_6")
   public static final Item SCROLL_6 = null;
   @ObjectHolder("inftsukaddon:tb_lore_scroll_8")
   public static final Item SCROLL_8 = null;

   public ItemTailedBeastLoreScroll(ElementsInfTsukAddon instance) {
      super(instance, 970);
   }

   public void initElements() {
      this.elements.items.add((Supplier)() -> new ItemLoreScroll(5, "Five-Tailed Beast", "Kokuo"));
      this.elements.items.add((Supplier)() -> new ItemLoreScroll(6, "Six-Tailed Beast", "Saiken"));
      this.elements.items.add((Supplier)() -> new ItemLoreScroll(8, "Eight-Tailed Beast", "Gyuki"));
   }

   @SideOnly(Side.CLIENT)
   public void registerModels(ModelRegistryEvent event) {
      if (SCROLL_5 != null) {
         ModelLoader.setCustomModelResourceLocation(SCROLL_5, 0, new ModelResourceLocation("inftsukaddon:statbuff", "inventory"));
      }

      if (SCROLL_6 != null) {
         ModelLoader.setCustomModelResourceLocation(SCROLL_6, 0, new ModelResourceLocation("inftsukaddon:statbuff", "inventory"));
      }

      if (SCROLL_8 != null) {
         ModelLoader.setCustomModelResourceLocation(SCROLL_8, 0, new ModelResourceLocation("inftsukaddon:statbuff", "inventory"));
      }

   }

   public static class ItemLoreScroll extends Item {
      private final int beastNumber;
      private final String beastName;
      private final String beastShortName;

      public ItemLoreScroll(int beastNumber, String beastName, String beastShortName) {
         this.beastNumber = beastNumber;
         this.beastName = beastName;
         this.beastShortName = beastShortName;
         this.setMaxDamage(0);
         this.maxStackSize = 1;
         this.setTranslationKey("tb_lore_scroll_" + beastNumber);
         this.setRegistryName("tb_lore_scroll_" + beastNumber);
         this.setCreativeTab(CreativeTabs.MISC);
      }

      public String getItemStackDisplayName(ItemStack stack) {
         return "§d§lLore: " + this.beastName + " Scroll";
      }

      public boolean hasEffect(ItemStack stack) {
         return true;
      }

      @SideOnly(Side.CLIENT)
      public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
         tooltip.add("§7Contains the sealed power of §d" + this.beastShortName + " §7- the " + this.beastName);
         tooltip.add("");
         tooltip.add("§eRight-click to become the Jinchuriki of " + this.beastShortName);
         tooltip.add("§c§oThis scroll can only exist once in the world.");
         tooltip.add("§8§oConsumed on use.");
      }

      public int getItemEnchantability() {
         return 0;
      }

      public int getMaxItemUseDuration(ItemStack itemstack) {
         return 0;
      }

      public float getDestroySpeed(ItemStack par1ItemStack, IBlockState par2Block) {
         return 1.0F;
      }

      public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
         if (!world.isRemote && player instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP)player;
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               boolean assigned = EntityBijuManager.setVesselByTails(mp, this.beastNumber);
               if (!assigned) {
                  mp.sendMessage(new TextComponentString("§cThat tailed beast is already sealed in another jinchuriki."));
                  return new ActionResult(EnumActionResult.FAIL, player.getHeldItem(hand));
               }

               player.getHeldItem(hand).shrink(1);
               mp.connection.sendPacket(new SPacketTitle(Type.TITLE, new TextComponentString("§d§lJinchuriki Sealed!")));
               mp.connection.sendPacket(new SPacketTitle(Type.SUBTITLE, new TextComponentString("§eYou are now the host of " + this.beastShortName + "!")));
               mp.sendMessage(new TextComponentString("§d§l✦ " + this.beastName + " §r§ahas been sealed within you!"));

               for(EntityPlayerMP p : server.getPlayerList().getPlayers()) {
                  p.sendMessage(new TextComponentString("§d§l[LEGENDARY] §e" + mp.getName() + " §dhas become the Jinchuriki of " + this.beastShortName + "!"));
               }

               if (world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)world;
                  double px = mp.posX;
                  double py = mp.posY;
                  double pz = mp.posZ;

                  for(int h = 0; h < 50; ++h) {
                     ws.spawnParticle(EnumParticleTypes.PORTAL, px, py + (double)h * 0.4, pz, 10, (double)0.5F, 0.1, (double)0.5F, 0.1, new int[0]);
                  }

                  for(int i = 0; i < 60; ++i) {
                     double angle = (double)i / (double)60.0F * Math.PI * (double)6.0F;
                     double r = (double)2.0F + (double)i / (double)60.0F * (double)3.0F;
                     ws.spawnParticle(EnumParticleTypes.TOTEM, px + Math.cos(angle) * r, py + (double)i / (double)60.0F * (double)10.0F, pz + Math.sin(angle) * r, 3, 0.1, 0.1, 0.1, 0.05, new int[0]);
                  }

                  ws.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, px, py + (double)2.0F, pz, 3, (double)2.0F, (double)2.0F, (double)2.0F, (double)0.0F, new int[0]);
               }

               world.playSound((EntityPlayer)null, mp.posX, mp.posY, mp.posZ, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 3.0F, 0.8F);
               world.playSound((EntityPlayer)null, mp.posX, mp.posY, mp.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 1.5F, 1.2F);
               return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
            }
         }

         return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
      }
   }
}
