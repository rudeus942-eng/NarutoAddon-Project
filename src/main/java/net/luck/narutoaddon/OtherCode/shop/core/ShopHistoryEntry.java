
package net.luck.narutoaddon.OtherCode.shop.core;

import net.minecraft.nbt.NBTTagCompound;

public class ShopHistoryEntry {
   private final String crateId;
   private final String itemId;
   private final int itemMeta;
   private final int itemCount;
   private final String displayName;
   private final int rarityOrdinal;
   private final long timestamp;
   private final int tokenAmount;

   public ShopHistoryEntry(String crateId, String itemId, int itemMeta, int itemCount, String displayName, int rarityOrdinal, long timestamp) {
      this(crateId, itemId, itemMeta, itemCount, displayName, rarityOrdinal, timestamp, 0);
   }

   public ShopHistoryEntry(String crateId, String itemId, int itemMeta, int itemCount, String displayName, int rarityOrdinal, long timestamp, int tokenAmount) {
      this.crateId = crateId;
      this.itemId = itemId;
      this.itemMeta = itemMeta;
      this.itemCount = itemCount;
      this.displayName = displayName;
      this.rarityOrdinal = rarityOrdinal;
      this.timestamp = timestamp;
      this.tokenAmount = tokenAmount;
   }

   public String getCrateId() {
      return this.crateId;
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

   public String getDisplayName() {
      return this.displayName;
   }

   public int getRarityOrdinal() {
      return this.rarityOrdinal;
   }

   public long getTimestamp() {
      return this.timestamp;
   }

   public int getTokenAmount() {
      return this.tokenAmount;
   }

   public boolean isDuplicate() {
      return this.tokenAmount > 0;
   }

   public ItemRarity getRarity() {
      return ItemRarity.fromOrdinal(this.rarityOrdinal);
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("crateId", this.crateId);
      nbt.setString("itemId", this.itemId);
      nbt.setInteger("itemMeta", this.itemMeta);
      nbt.setInteger("itemCount", this.itemCount);
      nbt.setString("displayName", this.displayName);
      nbt.setByte("rarity", (byte)this.rarityOrdinal);
      nbt.setLong("timestamp", this.timestamp);
      if (this.tokenAmount > 0) {
         nbt.setInteger("tokenAmount", this.tokenAmount);
      }

      nbt.setByte("version", (byte)2);
      return nbt;
   }

   public static ShopHistoryEntry readFromNBT(NBTTagCompound nbt) {
      int version = nbt.hasKey("version") ? nbt.getByte("version") : 1;
      int rarityOrd = nbt.getByte("rarity");
      if (version < 2 && rarityOrd >= 0 && rarityOrd <= 4) {
         rarityOrd = ItemRarity.fromLegacyOrdinal(rarityOrd).id;
      }

      int tokenAmount = nbt.hasKey("tokenAmount") ? nbt.getInteger("tokenAmount") : 0;
      return new ShopHistoryEntry(nbt.getString("crateId"), nbt.getString("itemId"), nbt.getInteger("itemMeta"), nbt.getInteger("itemCount"), nbt.getString("displayName"), rarityOrd, nbt.getLong("timestamp"), tokenAmount);
   }
}
