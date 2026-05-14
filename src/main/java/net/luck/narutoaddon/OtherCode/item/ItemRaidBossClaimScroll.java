
package net.luck.narutoaddon.OtherCode.item;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class ItemRaidBossClaimScroll extends ElementsInfTsukAddon.ModElement {
   private static final BossScrollDef[] BOSS_SCROLLS = new BossScrollDef[]{new BossScrollDef("hashirama", "Hashirama Senju", "Mokuton (Wood Release)", "clansaddon:clans/wood_unlocked", new String[0], new ScrollItem("narutomod:mokuton", 0), new ScrollItem[]{new ScrollItem("clansaddon:scroll_gracious_deity_gates", 0)}), new BossScrollDef("itachi", "Itachi Uchiha", "Uchiha Clan Package", "clansaddon:clans/uchiha_unlocked", new String[]{"clansaddon:uchiha/root"}, new ScrollItem("narutomod:sharinganhelmet", 0), new ScrollItem[]{new ScrollItem("narutomod:scroll_genjutsu", 0), new ScrollItem("narutomod:scroll_flame_formation", 0), new ScrollItem("clansaddon:uchiha_stone_tablet", 0)}), new BossScrollDef("kimimaro", "Kimimaro Kaguya", "Kaguya Clan Package", "clansaddon:clans/kaguya_unlocked", new String[0], new ScrollItem("narutomod:shikotsumyaku", 0), new ScrollItem[]{new ScrollItem("clansaddon:scroll_larch_and_willow", 0)})};
   @ObjectHolder("inftsukaddon:raid_claim_scroll_hashirama")
   public static final Item SCROLL_HASHIRAMA = null;
   @ObjectHolder("inftsukaddon:raid_claim_scroll_itachi")
   public static final Item SCROLL_ITACHI = null;
   @ObjectHolder("inftsukaddon:raid_claim_scroll_kimimaro")
   public static final Item SCROLL_KIMIMARO = null;

   public ItemRaidBossClaimScroll(ElementsInfTsukAddon instance) {
      super(instance, 985);
   }

   public void initElements() {
      for(BossScrollDef def : BOSS_SCROLLS) {
         this.elements.items.add((Supplier)() -> new ItemClaimScroll(def));
      }

   }

   @SideOnly(Side.CLIENT)
   public void registerModels(ModelRegistryEvent event) {
      if (SCROLL_HASHIRAMA != null) {
         ModelLoader.setCustomModelResourceLocation(SCROLL_HASHIRAMA, 0, new ModelResourceLocation("inftsukaddon:statbuff", "inventory"));
      }

      if (SCROLL_ITACHI != null) {
         ModelLoader.setCustomModelResourceLocation(SCROLL_ITACHI, 0, new ModelResourceLocation("inftsukaddon:statbuff", "inventory"));
      }

      if (SCROLL_KIMIMARO != null) {
         ModelLoader.setCustomModelResourceLocation(SCROLL_KIMIMARO, 0, new ModelResourceLocation("inftsukaddon:statbuff", "inventory"));
      }

   }

   public static Item getScrollForBoss(String bossId) {
      if ("hashirama".equals(bossId)) {
         return SCROLL_HASHIRAMA;
      } else if ("itachi".equals(bossId)) {
         return SCROLL_ITACHI;
      } else {
         return "kimimaro".equals(bossId) ? SCROLL_KIMIMARO : null;
      }
   }

   private static class BossScrollDef {
      final String bossId;
      final String bossDisplayName;
      final String rewardName;
      final String mainAdvancement;
      final String[] extraAdvancements;
      final ScrollItem mainItem;
      final ScrollItem[] bonusItems;

      BossScrollDef(String bossId, String bossDisplayName, String rewardName, String mainAdvancement, String[] extraAdvancements, ScrollItem mainItem, ScrollItem... bonusItems) {
         this.bossId = bossId;
         this.bossDisplayName = bossDisplayName;
         this.rewardName = rewardName;
         this.mainAdvancement = mainAdvancement;
         this.extraAdvancements = extraAdvancements;
         this.mainItem = mainItem;
         this.bonusItems = bonusItems;
      }

      String registryName() {
         return "raid_claim_scroll_" + this.bossId;
      }
   }

   private static class ScrollItem {
      final String itemId;
      final int meta;

      ScrollItem(String itemId, int meta) {
         this.itemId = itemId;
         this.meta = meta;
      }
   }

   public static class ItemClaimScroll extends Item {
      private final BossScrollDef def;

      public ItemClaimScroll(BossScrollDef def) {
         this.def = def;
         this.setMaxDamage(0);
         this.maxStackSize = 1;
         this.setTranslationKey(def.registryName());
         this.setRegistryName(def.registryName());
         this.setCreativeTab(CreativeTabs.MISC);
      }

      public String getItemStackDisplayName(ItemStack stack) {
         return "§6§l" + this.def.bossDisplayName + " Claim Scroll";
      }

      public boolean hasEffect(ItemStack stack) {
         return true;
      }

      @SideOnly(Side.CLIENT)
      public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
         tooltip.add("§7A rare scroll obtained from defeating");
         tooltip.add("§6" + this.def.bossDisplayName + " §7in an ANBU raid.");
         tooltip.add("");
         tooltip.add("§eRight-click to claim: §a" + this.def.rewardName);
         tooltip.add("");
         tooltip.add("§c§oConsumed on use.");
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
            if (this.def.mainAdvancement != null) {
               grantAdvancement(mp, this.def.mainAdvancement);
            }

            if (this.def.extraAdvancements != null) {
               for(String adv : this.def.extraAdvancements) {
                  grantAdvancement(mp, adv);
               }
            }

            this.giveItem(mp, this.def.mainItem);

            for(ScrollItem bonus : this.def.bonusItems) {
               this.giveItem(mp, bonus);
            }

            player.getHeldItem(hand).shrink(1);
            mp.connection.sendPacket(new SPacketTitle(Type.TITLE, new TextComponentString("§6§l" + this.def.rewardName + " Unlocked!")));
            mp.connection.sendPacket(new SPacketTitle(Type.SUBTITLE, new TextComponentString("§eGranted by " + this.def.bossDisplayName)));
            mp.sendMessage(new TextComponentString("§6§l✦ " + this.def.rewardName + " §r§ahas been unlocked!"));
            mp.sendMessage(new TextComponentString("§7Reward from defeating §6" + this.def.bossDisplayName + " §7(ANBU Raid)"));
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               for(EntityPlayerMP p : server.getPlayerList().getPlayers()) {
                  if (p != mp) {
                     p.sendMessage(new TextComponentString("§6§l[RAID] §e" + mp.getName() + " §6has unlocked §a" + this.def.rewardName + " §6from the " + this.def.bossDisplayName + " raid!"));
                  }
               }
            }

            if (world instanceof WorldServer) {
               WorldServer ws = (WorldServer)world;
               double px = mp.posX;
               double py = mp.posY;
               double pz = mp.posZ;

               for(int h = 0; h < 30; ++h) {
                  ws.spawnParticle(EnumParticleTypes.TOTEM, px, py + (double)h * (double)0.5F, pz, 8, (double)0.5F, 0.1, (double)0.5F, 0.1, new int[0]);
               }

               for(int i = 0; i < 40; ++i) {
                  double angle = (double)i / (double)40.0F * Math.PI * (double)4.0F;
                  double r = (double)1.5F + (double)i / (double)40.0F * (double)2.5F;
                  ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, px + Math.cos(angle) * r, py + (double)i / (double)40.0F * (double)8.0F, pz + Math.sin(angle) * r, 2, 0.1, 0.1, 0.1, (double)0.0F, new int[0]);
               }
            }

            world.playSound((EntityPlayer)null, mp.posX, mp.posY, mp.posZ, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 3.0F, 1.0F);
            world.playSound((EntityPlayer)null, mp.posX, mp.posY, mp.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 2.0F, 0.8F);
            return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
         } else {
            return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
         }
      }

      private void giveItem(EntityPlayerMP player, ScrollItem scrollItem) {
         Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(scrollItem.itemId));
         if (item == null) {
            System.out.println("[RaidClaimScroll] WARNING: Item not found: " + scrollItem.itemId);
         } else {
            ItemStack stack = new ItemStack(item, 1, scrollItem.meta);
            if (!player.inventory.addItemStackToInventory(stack)) {
               ShopSavedData shopData = ShopSavedData.get(player.getServerWorld());
               if (shopData != null) {
                  NBTTagCompound ov = new NBTTagCompound();
                  ov.setString("itemId", scrollItem.itemId);
                  ov.setInteger("meta", scrollItem.meta);
                  ov.setInteger("count", 1);
                  shopData.addOverflowItem(player.getUniqueID(), ov);
                  player.sendMessage(new TextComponentString("§eInventory full - item sent to your bank!"));
               }
            }

            player.inventoryContainer.detectAndSendChanges();
         }
      }

      private static void grantAdvancement(EntityPlayerMP player, String advancementId) {
         if (advancementId != null && !advancementId.isEmpty()) {
            try {
               ResourceLocation loc = new ResourceLocation(advancementId);
               Advancement advancement = player.getServer().getAdvancementManager().getAdvancement(loc);
               if (advancement != null) {
                  AdvancementProgress progress = player.getAdvancements().getProgress(advancement);

                  for(String criterion : progress.getRemaningCriteria()) {
                     player.getAdvancements().grantCriterion(advancement, criterion);
                  }
               }
            } catch (Exception var7) {
               System.out.println("[RaidClaimScroll] Failed to grant advancement: " + advancementId);
            }

         }
      }
   }
}
