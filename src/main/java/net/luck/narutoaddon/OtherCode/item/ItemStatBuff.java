
package net.luck.narutoaddon.OtherCode.item;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.stat.core.PlayerStatData;
import net.luck.narutoaddon.OtherCode.stat.core.StatManager;
import net.luck.narutoaddon.OtherCode.stat.core.StatSavedData;
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
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class ItemStatBuff extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:statbuff")
   public static final Item block = null;

   public ItemStatBuff(ElementsInfTsukAddon instance) {
      super(instance, 42);
   }

   public void initElements() {
      this.elements.items.add((Supplier)() -> new ItemCustom());
   }

   @SideOnly(Side.CLIENT)
   public void registerModels(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("inftsukaddon:statbuff", "inventory"));
   }

   public static class ItemCustom extends Item {
      public ItemCustom() {
         this.setMaxDamage(0);
         this.maxStackSize = 16;
         this.setTranslationKey("statbuff");
         this.setRegistryName("statbuff");
         this.setCreativeTab(CreativeTabs.MISC);
      }

      public String getItemStackDisplayName(ItemStack stack) {
         return "§b§lChakra Enhancement Scroll";
      }

      public boolean hasEffect(ItemStack stack) {
         return true;
      }

      @SideOnly(Side.CLIENT)
      public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
         tooltip.add("§7Grants §b+5% Jutsu Damage §7permanently");
         tooltip.add("§8Right-click to consume");
         tooltip.add("");
         tooltip.add("§6Requires: §eKazekage Rescue arc complete");
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
            int cap = StatManager.getChakraEnhancementCap(mp);
            if (cap <= 0) {
               mp.sendMessage(new TextComponentString("§cYou must complete the Kazekage Rescue arc first!"));
               return new ActionResult(EnumActionResult.FAIL, player.getHeldItem(hand));
            } else {
               StatSavedData data = StatSavedData.get(world);
               PlayerStatData stats = data.getOrCreate(mp.getUniqueID());
               if (stats.addChakraEnhancementCharge(cap)) {
                  player.getHeldItem(hand).shrink(1);
                  data.markDirty();
                  StatManager.getInstance().syncToClient(mp);
                  int newLevel = stats.getChakraEnhancementCharges();
                  int dmgBonus = newLevel * 5;
                  mp.sendMessage(new TextComponentString("§b§l✦ Chakra Enhancement §r§aLevel " + newLevel + "/" + cap + "§r§7 — Jutsu damage §a+" + dmgBonus + "%"));
                  mp.connection.sendPacket(new SPacketTitle(Type.TITLE, new TextComponentString("§b§lChakra Enhanced")));
                  if (newLevel == 1) {
                     mp.connection.sendPacket(new SPacketTitle(Type.SUBTITLE, new TextComponentString("§eYour journey toward true power begins...")));
                  } else {
                     mp.connection.sendPacket(new SPacketTitle(Type.SUBTITLE, new TextComponentString("§a+" + dmgBonus + "% Jutsu Damage §7— §eGrow even stronger...")));
                  }

                  if (world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)world;
                     double px = mp.posX;
                     double py = mp.posY;
                     double pz = mp.posZ;

                     for(int i = 0; i < 40; ++i) {
                        double angle = (double)i / (double)40.0F * Math.PI * (double)4.0F;
                        double r = (double)1.5F;
                        double h = (double)i / (double)40.0F * (double)4.0F;
                        ws.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, px + Math.cos(angle) * r, py + h, pz + Math.sin(angle) * r, 1, 0.05, 0.05, 0.05, 0.01, new int[0]);
                     }

                     ws.spawnParticle(EnumParticleTypes.TOTEM, px, py + (double)1.0F, pz, 30, (double)0.5F, (double)1.0F, (double)0.5F, 0.3, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, px, py + (double)1.5F, pz, 20, (double)1.0F, (double)0.5F, (double)1.0F, (double)0.5F, new int[0]);
                  }

                  world.playSound((EntityPlayer)null, mp.posX, mp.posY, mp.posZ, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.5F, 1.2F);
                  world.playSound((EntityPlayer)null, mp.posX, mp.posY, mp.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 0.8F);
                  return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
               } else {
                  mp.sendMessage(new TextComponentString("§cChakra Enhancement is at max level (" + cap + "/" + cap + ") for your current rank!"));
                  return new ActionResult(EnumActionResult.FAIL, player.getHeldItem(hand));
               }
            }
         } else {
            return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
         }
      }
   }
}
