
package net.luck.narutoaddon.OtherCode.shop.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class PlayerItemTracker extends WorldSavedData {
   private static final String DATA_NAME = "InfTsukItemTracker";
   private final Map<UUID, List<TrackedItem>> trackedItems = new HashMap();
   private final Map<UUID, List<RestoreLogEntry>> restoreLog = new HashMap();
   private static final int MAX_RESTORE_LOG = 100;

   public PlayerItemTracker() {
      super("InfTsukItemTracker");
   }

   public PlayerItemTracker(String name) {
      super(name);
   }

   public static PlayerItemTracker get(World world) {
      MapStorage storage = world.getMapStorage();
      PlayerItemTracker data = (PlayerItemTracker)storage.getOrLoadData(PlayerItemTracker.class, "InfTsukItemTracker");
      if (data == null) {
         data = new PlayerItemTracker();
         storage.setData("InfTsukItemTracker", data);
      }

      return data;
   }

   public List<TrackedItem> getTrackedItems(UUID playerId) {
      List<TrackedItem> items = (List)this.trackedItems.get(playerId);
      return items != null ? new ArrayList(items) : new ArrayList();
   }

   public TrackedItem findTrackedItem(UUID playerId, String registryName, int meta) {
      List<TrackedItem> items = (List)this.trackedItems.get(playerId);
      if (items == null) {
         return null;
      } else {
         for(TrackedItem item : items) {
            if (item.itemRegistryName.equals(registryName) && item.meta == meta) {
               return item;
            }
         }

         return null;
      }
   }

   public TrackedItem findTrackedItem(UUID playerId, String registryName, int meta, String displayName) {
      List<TrackedItem> items = (List)this.trackedItems.get(playerId);
      if (items == null) {
         return null;
      } else {
         for(TrackedItem item : items) {
            if (item.itemRegistryName.equals(registryName) && item.meta == meta && Objects.equals(item.displayName, displayName)) {
               return item;
            }
         }

         return null;
      }
   }

   public void addOrUpdateItem(UUID playerId, TrackedItem newItem) {
      List<TrackedItem> items = (List)this.trackedItems.computeIfAbsent(playerId, (k) -> new ArrayList());
      String key = newItem.getKey();

      for(int i = 0; i < items.size(); ++i) {
         if (((TrackedItem)items.get(i)).getKey().equals(key)) {
            items.set(i, newItem);
            this.markDirty();
            return;
         }
      }

      items.add(newItem);
      this.markDirty();
   }

   public void reconcileOwnership(UUID playerId, Set<String> foundKeys) {
      List<TrackedItem> items = (List)this.trackedItems.get(playerId);
      if (items != null) {
         boolean changed = false;

         for(TrackedItem item : items) {
            boolean shouldOwn = foundKeys.contains(item.getKey());
            if (item.currentlyOwned != shouldOwn) {
               item.currentlyOwned = shouldOwn;
               changed = true;
            }
         }

         if (changed) {
            this.markDirty();
         }

      }
   }

   public List<RestoreLogEntry> getRestoreLog(UUID playerId) {
      List<RestoreLogEntry> log = (List)this.restoreLog.get(playerId);
      return log != null ? new ArrayList(log) : new ArrayList();
   }

   public void addRestoreLogEntry(UUID playerId, RestoreLogEntry entry) {
      List<RestoreLogEntry> log = (List)this.restoreLog.computeIfAbsent(playerId, (k) -> new ArrayList());
      log.add(0, entry);

      while(log.size() > 100) {
         log.remove(log.size() - 1);
      }

      this.markDirty();
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.trackedItems.clear();
      if (nbt.hasKey("trackedItems")) {
         NBTTagCompound itemsTag = nbt.getCompoundTag("trackedItems");

         for(String key : itemsTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(key);
               NBTTagList itemList = itemsTag.getTagList(key, 10);
               List<TrackedItem> items = new ArrayList();

               for(int i = 0; i < itemList.tagCount(); ++i) {
                  items.add(TrackedItem.readFromNBT(itemList.getCompoundTagAt(i)));
               }

               this.trackedItems.put(playerId, items);
            } catch (IllegalArgumentException var10) {
            }
         }
      }

      this.restoreLog.clear();
      if (nbt.hasKey("restoreLog")) {
         NBTTagCompound logTag = nbt.getCompoundTag("restoreLog");

         for(String key : logTag.getKeySet()) {
            try {
               UUID playerId = UUID.fromString(key);
               NBTTagList logList = logTag.getTagList(key, 10);
               List<RestoreLogEntry> entries = new ArrayList();

               for(int i = 0; i < logList.tagCount(); ++i) {
                  entries.add(RestoreLogEntry.readFromNBT(logList.getCompoundTagAt(i)));
               }

               this.restoreLog.put(playerId, entries);
            } catch (IllegalArgumentException var9) {
            }
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagCompound itemsTag = new NBTTagCompound();

      for(Map.Entry<UUID, List<TrackedItem>> entry : this.trackedItems.entrySet()) {
         NBTTagList itemList = new NBTTagList();

         for(TrackedItem item : (List)entry.getValue()) {
            itemList.appendTag(item.writeToNBT());
         }

         itemsTag.setTag(((UUID)entry.getKey()).toString(), itemList);
      }

      nbt.setTag("trackedItems", itemsTag);
      NBTTagCompound logTag = new NBTTagCompound();

      for(Map.Entry<UUID, List<RestoreLogEntry>> entry : this.restoreLog.entrySet()) {
         NBTTagList logList = new NBTTagList();

         for(RestoreLogEntry logEntry : (List)entry.getValue()) {
            logList.appendTag(logEntry.writeToNBT());
         }

         logTag.setTag(((UUID)entry.getKey()).toString(), logList);
      }

      nbt.setTag("restoreLog", logTag);
      return nbt;
   }
}
