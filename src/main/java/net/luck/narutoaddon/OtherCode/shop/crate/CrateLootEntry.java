
package net.luck.narutoaddon.OtherCode.shop.crate;

import net.luck.narutoaddon.OtherCode.shop.core.ItemRarity;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrateLootEntry {
   private final String itemId;
   private final int itemMeta;
   private final int itemCount;
   private final NBTTagCompound itemNbt;
   private final double weight;
   private final ItemRarity rarity;
   private final String ftbQuestId;
   private final String displayNameOverride;
   private final List<BundleItem> bundleItems;
   private final String advancementId;
   private boolean allowDuplicates;

   public CrateLootEntry(String itemId, int itemMeta, int itemCount, NBTTagCompound itemNbt, double weight, ItemRarity rarity) {
      this(itemId, itemMeta, itemCount, itemNbt, weight, rarity, (String)null, (String)null, (List)null, (String)null);
   }

   public CrateLootEntry(String itemId, int itemMeta, int itemCount, NBTTagCompound itemNbt, double weight, ItemRarity rarity, String ftbQuestId) {
      this(itemId, itemMeta, itemCount, itemNbt, weight, rarity, ftbQuestId, (String)null, (List)null, (String)null);
   }

   public CrateLootEntry(String itemId, int itemMeta, int itemCount, NBTTagCompound itemNbt, double weight, ItemRarity rarity, String ftbQuestId, String displayNameOverride, List<BundleItem> bundleItems, String advancementId) {
      this.allowDuplicates = false;
      this.itemId = itemId;
      this.itemMeta = itemMeta;
      this.itemCount = itemCount;
      this.itemNbt = itemNbt != null ? itemNbt.copy() : null;
      this.weight = weight;
      this.rarity = rarity;
      this.ftbQuestId = ftbQuestId;
      this.displayNameOverride = displayNameOverride;
      this.bundleItems = bundleItems != null ? new ArrayList(bundleItems) : new ArrayList();
      this.advancementId = advancementId;
   }

   public String getItemId() {
      return this.itemId;
   }

   public int getItemMeta() {
      return this.itemMeta;
   }

   public int getItemCount() {
      return this.itemCount;
   }

   public NBTTagCompound getItemNbt() {
      return this.itemNbt != null ? this.itemNbt.copy() : null;
   }

   public double getWeight() {
      return this.weight;
   }

   public ItemRarity getRarity() {
      return this.rarity;
   }

   public boolean hasFtbQuest() {
      return this.ftbQuestId != null && !this.ftbQuestId.isEmpty();
   }

   public String getFtbQuestId() {
      return this.ftbQuestId;
   }

   public String getDisplayNameOverride() {
      return this.displayNameOverride;
   }

   public boolean hasDisplayNameOverride() {
      return this.displayNameOverride != null && !this.displayNameOverride.isEmpty();
   }

   public List<BundleItem> getBundleItems() {
      return Collections.unmodifiableList(this.bundleItems);
   }

   public boolean hasBundleItems() {
      return !this.bundleItems.isEmpty();
   }

   public int getBundleSize() {
      return 1 + this.bundleItems.size();
   }

   public String getAdvancementId() {
      return this.advancementId;
   }

   public boolean hasAdvancement() {
      return this.advancementId != null && !this.advancementId.isEmpty();
   }

   public boolean allowsDuplicates() {
      return this.allowDuplicates;
   }

   public CrateLootEntry withAllowDuplicates() {
      this.allowDuplicates = true;
      return this;
   }

   public ItemStack toItemStack() {
      ResourceLocation rl = new ResourceLocation(this.itemId);
      Item item = (Item)ForgeRegistries.ITEMS.getValue(rl);
      if (item == null || item == Items.AIR) {
         item = (Item)Item.REGISTRY.getObject(rl);
      }

      if (item == null || item == Items.AIR) {
         Block block = (Block)ForgeRegistries.BLOCKS.getValue(rl);
         if (block != null && block != Blocks.AIR) {
            item = Item.getItemFromBlock(block);
         }
      }

      if (item != null && item != Items.AIR) {
         ItemStack stack = new ItemStack(item, this.itemCount, this.itemMeta);
         if (this.itemNbt != null) {
            stack.setTagCompound(this.itemNbt.copy());
         }

         return stack;
      } else {
         System.out.println("[CrateLootEntry] FAILED to resolve item: '" + this.itemId + "'");
         return ItemStack.EMPTY;
      }
   }

   public String getDisplayName() {
      return this.hasDisplayNameOverride() ? this.displayNameOverride : this.toItemStack().getDisplayName();
   }

   public float getDropChance(double totalWeight) {
      return (float)(this.weight / totalWeight * (double)100.0F);
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("itemId", this.itemId);
      nbt.setInteger("itemMeta", this.itemMeta);
      nbt.setInteger("itemCount", this.itemCount);
      if (this.itemNbt != null) {
         nbt.setTag("itemNbt", this.itemNbt.copy());
      }

      nbt.setDouble("weight", this.weight);
      nbt.setByte("rarity", (byte)this.rarity.id);
      if (this.ftbQuestId != null) {
         nbt.setString("ftbQuestId", this.ftbQuestId);
      }

      if (this.displayNameOverride != null) {
         nbt.setString("displayNameOverride", this.displayNameOverride);
      }

      if (!this.bundleItems.isEmpty()) {
         NBTTagList bundleList = new NBTTagList();

         for(BundleItem bi : this.bundleItems) {
            bundleList.appendTag(bi.writeToNBT());
         }

         nbt.setTag("bundleItems", bundleList);
      }

      if (this.advancementId != null) {
         nbt.setString("advancementId", this.advancementId);
      }

      return nbt;
   }

   public static CrateLootEntry readFromNBT(NBTTagCompound nbt) {
      String itemId = nbt.getString("itemId");
      int itemMeta = nbt.getInteger("itemMeta");
      int itemCount = nbt.getInteger("itemCount");
      if (itemCount <= 0) {
         itemCount = 1;
      }

      NBTTagCompound itemNbt = nbt.hasKey("itemNbt") ? nbt.getCompoundTag("itemNbt") : null;
      double weight;
      if (nbt.hasKey("weight", 6)) {
         weight = nbt.getDouble("weight");
      } else {
         weight = (double)nbt.getInteger("weight");
      }

      ItemRarity rarity;
      if (nbt.hasKey("rarity")) {
         rarity = ItemRarity.fromId(nbt.getByte("rarity"));
      } else {
         rarity = ItemRarity.D_RANK;
      }

      String ftbQuestId = nbt.hasKey("ftbQuestId") ? nbt.getString("ftbQuestId") : null;
      String displayNameOverride = nbt.hasKey("displayNameOverride") ? nbt.getString("displayNameOverride") : null;
      List<BundleItem> bundleItems = new ArrayList();
      if (nbt.hasKey("bundleItems")) {
         NBTTagList bundleList = nbt.getTagList("bundleItems", 10);

         for(int i = 0; i < bundleList.tagCount(); ++i) {
            bundleItems.add(BundleItem.readFromNBT(bundleList.getCompoundTagAt(i)));
         }
      }

      String advancementId = nbt.hasKey("advancementId") ? nbt.getString("advancementId") : null;
      return new CrateLootEntry(itemId, itemMeta, itemCount, itemNbt, weight, rarity, ftbQuestId, displayNameOverride, bundleItems, advancementId);
   }

   public static class BundleItem {
      private final String itemId;
      private final int itemMeta;
      private final int itemCount;
      private final NBTTagCompound itemNbt;

      public BundleItem(String itemId, int itemMeta, int itemCount, NBTTagCompound itemNbt) {
         this.itemId = itemId;
         this.itemMeta = itemMeta;
         this.itemCount = itemCount;
         this.itemNbt = itemNbt != null ? itemNbt.copy() : null;
      }

      public String getItemId() {
         return this.itemId;
      }

      public int getItemMeta() {
         return this.itemMeta;
      }

      public int getItemCount() {
         return this.itemCount;
      }

      public NBTTagCompound getItemNbt() {
         return this.itemNbt != null ? this.itemNbt.copy() : null;
      }

      public ItemStack toItemStack() {
         ResourceLocation rl = new ResourceLocation(this.itemId);
         Item item = (Item)ForgeRegistries.ITEMS.getValue(rl);
         if (item == null || item == Items.AIR) {
            item = (Item)Item.REGISTRY.getObject(rl);
         }

         if (item == null || item == Items.AIR) {
            Block block = (Block)ForgeRegistries.BLOCKS.getValue(rl);
            if (block != null && block != Blocks.AIR) {
               item = Item.getItemFromBlock(block);
            }
         }

         if (item != null && item != Items.AIR) {
            ItemStack stack = new ItemStack(item, this.itemCount, this.itemMeta);
            if (this.itemNbt != null) {
               stack.setTagCompound(this.itemNbt.copy());
            }

            return stack;
         } else {
            System.out.println("[CrateLootEntry] BundleItem FAILED to resolve: '" + this.itemId + "'");
            return ItemStack.EMPTY;
         }
      }

      public NBTTagCompound writeToNBT() {
         NBTTagCompound nbt = new NBTTagCompound();
         nbt.setString("itemId", this.itemId);
         nbt.setInteger("itemMeta", this.itemMeta);
         nbt.setInteger("itemCount", this.itemCount);
         if (this.itemNbt != null) {
            nbt.setTag("itemNbt", this.itemNbt.copy());
         }

         return nbt;
      }

      public static BundleItem readFromNBT(NBTTagCompound nbt) {
         String itemId = nbt.getString("itemId");
         int itemMeta = nbt.getInteger("itemMeta");
         int itemCount = nbt.getInteger("itemCount");
         if (itemCount <= 0) {
            itemCount = 1;
         }

         NBTTagCompound itemNbt = nbt.hasKey("itemNbt") ? nbt.getCompoundTag("itemNbt") : null;
         return new BundleItem(itemId, itemMeta, itemCount, itemNbt);
      }
   }
}
