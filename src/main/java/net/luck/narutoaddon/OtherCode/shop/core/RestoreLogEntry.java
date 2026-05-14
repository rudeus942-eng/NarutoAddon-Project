
package net.luck.narutoaddon.OtherCode.shop.core;

import net.minecraft.nbt.NBTTagCompound;

public class RestoreLogEntry {
   public long timestamp;
   public String adminName;
   public String adminUUID;
   public String itemRegistryName;
   public String itemDisplayName;
   public String targetPlayerName;

   public RestoreLogEntry() {
   }

   public RestoreLogEntry(String adminName, String adminUUID, String itemRegistryName, String itemDisplayName, String targetPlayerName) {
      this.timestamp = System.currentTimeMillis();
      this.adminName = adminName;
      this.adminUUID = adminUUID;
      this.itemRegistryName = itemRegistryName;
      this.itemDisplayName = itemDisplayName;
      this.targetPlayerName = targetPlayerName;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound tag = new NBTTagCompound();
      tag.setLong("timestamp", this.timestamp);
      tag.setString("adminName", this.adminName != null ? this.adminName : "");
      tag.setString("adminUUID", this.adminUUID != null ? this.adminUUID : "");
      tag.setString("itemRegName", this.itemRegistryName != null ? this.itemRegistryName : "");
      tag.setString("itemDisplayName", this.itemDisplayName != null ? this.itemDisplayName : "");
      tag.setString("targetPlayer", this.targetPlayerName != null ? this.targetPlayerName : "");
      return tag;
   }

   public static RestoreLogEntry readFromNBT(NBTTagCompound tag) {
      RestoreLogEntry entry = new RestoreLogEntry();
      entry.timestamp = tag.getLong("timestamp");
      entry.adminName = tag.getString("adminName");
      entry.adminUUID = tag.getString("adminUUID");
      entry.itemRegistryName = tag.getString("itemRegName");
      entry.itemDisplayName = tag.getString("itemDisplayName");
      entry.targetPlayerName = tag.getString("targetPlayer");
      return entry;
   }
}
