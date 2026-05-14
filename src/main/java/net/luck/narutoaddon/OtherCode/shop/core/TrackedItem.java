
package net.luck.narutoaddon.OtherCode.shop.core;

import net.minecraft.nbt.NBTTagCompound;

public class TrackedItem {
   public String itemRegistryName;
   public int meta;
   public String displayName;
   public NBTTagCompound nbtSnapshot;
   public long firstSeenTimestamp;
   public long lastSeenTimestamp;
   public boolean currentlyOwned;
   public String category;
   public int[] jutsuXp;
   public boolean hasOwner;
   public boolean isAffinity;

   public TrackedItem() {
   }

   public TrackedItem(String registryName, int meta, String displayName, NBTTagCompound nbtSnapshot, String category) {
      this.itemRegistryName = registryName;
      this.meta = meta;
      this.displayName = displayName;
      this.nbtSnapshot = nbtSnapshot != null ? nbtSnapshot.copy() : new NBTTagCompound();
      this.firstSeenTimestamp = System.currentTimeMillis();
      this.lastSeenTimestamp = this.firstSeenTimestamp;
      this.currentlyOwned = true;
      this.category = category;
      this.extractNbtFields();
   }

   public void extractNbtFields() {
      if (this.nbtSnapshot == null) {
         this.jutsuXp = null;
         this.hasOwner = false;
         this.isAffinity = false;
      } else {
         NBTTagCompound tag = this.nbtSnapshot;
         if (tag.hasKey("JutsuExperienceMap")) {
            this.jutsuXp = tag.getIntArray("JutsuExperienceMap");
         } else if (tag.hasKey("jutsuXp")) {
            this.jutsuXp = tag.getIntArray("jutsuXp");
         } else {
            this.jutsuXp = null;
         }

         this.hasOwner = tag.hasKey("OwnerUUID") || tag.hasKey("ownerUUIDMost") || tag.hasKey("OwnerIdMost");
         this.isAffinity = tag.hasKey("IsNatureAffinity") && tag.getBoolean("IsNatureAffinity");
      }
   }

   public String getKey() {
      return this.itemRegistryName + ":" + this.meta + "|" + (this.displayName != null ? this.displayName : "");
   }

   public int getTotalXp() {
      if (this.jutsuXp != null && this.jutsuXp.length != 0) {
         int total = 0;

         for(int xp : this.jutsuXp) {
            total += xp;
         }

         return total;
      } else {
         return 0;
      }
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound tag = new NBTTagCompound();
      tag.setString("regName", this.itemRegistryName != null ? this.itemRegistryName : "");
      tag.setInteger("meta", this.meta);
      tag.setString("displayName", this.displayName != null ? this.displayName : "");
      if (this.nbtSnapshot != null) {
         tag.setTag("nbtSnapshot", this.nbtSnapshot.copy());
      }

      tag.setLong("firstSeen", this.firstSeenTimestamp);
      tag.setLong("lastSeen", this.lastSeenTimestamp);
      tag.setBoolean("owned", this.currentlyOwned);
      tag.setString("category", this.category != null ? this.category : "");
      if (this.jutsuXp != null) {
         tag.setIntArray("jutsuXp", this.jutsuXp);
      }

      tag.setBoolean("hasOwner", this.hasOwner);
      tag.setBoolean("isAffinity", this.isAffinity);
      return tag;
   }

   public static TrackedItem readFromNBT(NBTTagCompound tag) {
      TrackedItem item = new TrackedItem();
      item.itemRegistryName = tag.getString("regName");
      item.meta = tag.getInteger("meta");
      item.displayName = tag.getString("displayName");
      if (tag.hasKey("nbtSnapshot")) {
         item.nbtSnapshot = tag.getCompoundTag("nbtSnapshot");
      }

      item.firstSeenTimestamp = tag.getLong("firstSeen");
      item.lastSeenTimestamp = tag.getLong("lastSeen");
      item.currentlyOwned = tag.getBoolean("owned");
      item.category = tag.getString("category");
      if (tag.hasKey("jutsuXp")) {
         item.jutsuXp = tag.getIntArray("jutsuXp");
      }

      item.hasOwner = tag.getBoolean("hasOwner");
      item.isAffinity = tag.getBoolean("isAffinity");
      return item;
   }
}
