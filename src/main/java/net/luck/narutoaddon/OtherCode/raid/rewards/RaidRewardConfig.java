
package net.luck.narutoaddon.OtherCode.raid.rewards;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RaidRewardConfig extends WorldSavedData {
   private static final String DATA_NAME = "RaidRewardConfig";
   private static final String BACKUP_FILE_NAME = "raid_rewards_backup.dat";
   private static final String BACKUP_FILE_NAME_2 = "raid_rewards_backup2.dat";
   public static final String CATEGORY_BOSS = "boss";
   public static final String CATEGORY_FIRST_CLEAR = "firstclear";
   private static World cachedWorld = null;
   private Map<String, List<ConfiguredReward>> rewards = new HashMap();

   public RaidRewardConfig() {
      super("RaidRewardConfig");
   }

   public RaidRewardConfig(String name) {
      super(name);
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.rewards.clear();
      NBTTagCompound rewardsNBT = nbt.getCompoundTag("rewards");

      for(String key : rewardsNBT.getKeySet()) {
         NBTTagList list = rewardsNBT.getTagList(key, 10);
         List<ConfiguredReward> rewardList = new ArrayList();

         for(int i = 0; i < list.tagCount(); ++i) {
            rewardList.add(ConfiguredReward.readFromNBT(list.getCompoundTagAt(i)));
         }

         this.rewards.put(key, rewardList);
      }

      if (this.rewards.isEmpty()) {
         this.restoreFromBackup();
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagCompound rewardsNBT = new NBTTagCompound();

      for(Map.Entry<String, List<ConfiguredReward>> entry : this.rewards.entrySet()) {
         NBTTagList list = new NBTTagList();

         for(ConfiguredReward reward : (List)entry.getValue()) {
            list.appendTag(reward.writeToNBT());
         }

         rewardsNBT.setTag((String)entry.getKey(), list);
      }

      nbt.setTag("rewards", rewardsNBT);
      return nbt;
   }

   public void addReward(String category, ConfiguredReward reward) {
      ((List)this.rewards.computeIfAbsent(category, (k) -> new ArrayList())).add(reward);
      this.markDirty();
      this.saveBackup();
   }

   public void removeReward(String category, int index) {
      List<ConfiguredReward> list = (List)this.rewards.get(category);
      if (list != null && index >= 0 && index < list.size()) {
         list.remove(index);
         this.markDirty();
         this.saveBackup();
      }

   }

   public void clearRewards(String category) {
      this.rewards.remove(category);
      this.markDirty();
      this.saveBackup();
   }

   public List<ConfiguredReward> getRewards(String category) {
      return (List)this.rewards.getOrDefault(category, new ArrayList());
   }

   private void saveBackup() {
      try {
         File worldDir = DimensionManager.getCurrentSaveRootDirectory();
         if (worldDir == null) {
            return;
         }

         File dataDir = new File(worldDir, "data");
         if (!dataDir.exists()) {
            dataDir.mkdirs();
         }

         File backupFile = new File(dataDir, "raid_rewards_backup.dat");
         NBTTagCompound nbt = new NBTTagCompound();
         this.writeToNBT(nbt);
         CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(backupFile));
         File backupFile2 = new File(dataDir, "raid_rewards_backup2.dat");
         CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(backupFile2));
      } catch (Exception e) {
         System.err.println("[RaidRewardConfig] Failed to save backup: " + e.getMessage());
      }

   }

   private void restoreFromBackup() {
      try {
         File worldDir = DimensionManager.getCurrentSaveRootDirectory();
         if (worldDir == null) {
            return;
         }

         File dataDir = new File(worldDir, "data");
         File backupFile = new File(dataDir, "raid_rewards_backup.dat");
         if (!backupFile.exists()) {
            backupFile = new File(dataDir, "raid_rewards_backup2.dat");
         }

         if (backupFile.exists()) {
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(new FileInputStream(backupFile));
            this.readFromNBT(nbt);
            System.out.println("[RaidRewardConfig] Restored from backup!");
         }
      } catch (Exception e) {
         System.err.println("[RaidRewardConfig] Failed to restore from backup: " + e.getMessage());
      }

   }

   public void forceSave() {
      this.markDirty();
      this.saveBackup();
   }

   public static RaidRewardConfig get(World world) {
      if (world != null && world.getMapStorage() != null) {
         cachedWorld = world;
         MapStorage storage = world.getMapStorage();
         RaidRewardConfig instance = (RaidRewardConfig)storage.getOrLoadData(RaidRewardConfig.class, "RaidRewardConfig");
         if (instance == null) {
            instance = new RaidRewardConfig();
            storage.setData("RaidRewardConfig", instance);
         }

         return instance;
      } else {
         return null;
      }
   }

   public static class ConfiguredReward {
      public String itemId;
      public int amount;
      public int metadata;
      public NBTTagCompound nbt;

      public ConfiguredReward() {
      }

      public ConfiguredReward(ItemStack stack) {
         this.itemId = ((ResourceLocation)Item.REGISTRY.getNameForObject(stack.getItem())).toString();
         this.amount = stack.getCount();
         this.metadata = stack.getMetadata();
         this.nbt = stack.hasTagCompound() ? stack.getTagCompound().copy() : null;
      }

      public ItemStack toItemStack() {
         Item item = Item.getByNameOrId(this.itemId);
         if (item == null) {
            return ItemStack.EMPTY;
         } else {
            ItemStack stack = new ItemStack(item, this.amount, this.metadata);
            if (this.nbt != null) {
               stack.setTagCompound(this.nbt.copy());
            }

            return stack;
         }
      }

      public NBTTagCompound writeToNBT() {
         NBTTagCompound tag = new NBTTagCompound();
         tag.setString("itemId", this.itemId);
         tag.setInteger("amount", this.amount);
         tag.setInteger("metadata", this.metadata);
         if (this.nbt != null) {
            tag.setTag("nbt", this.nbt.copy());
         }

         return tag;
      }

      public static ConfiguredReward readFromNBT(NBTTagCompound tag) {
         ConfiguredReward reward = new ConfiguredReward();
         reward.itemId = tag.getString("itemId");
         reward.amount = tag.getInteger("amount");
         reward.metadata = tag.getInteger("metadata");
         if (tag.hasKey("nbt")) {
            reward.nbt = tag.getCompoundTag("nbt").copy();
         }

         return reward;
      }

      public String getDisplayName() {
         ItemStack stack = this.toItemStack();
         if (stack.isEmpty()) {
            return this.itemId + " (invalid)";
         } else {
            String name = stack.getDisplayName() + " x" + this.amount;
            if (this.nbt != null && !this.nbt.isEmpty()) {
               name = name + " [+NBT]";
            }

            return name;
         }
      }
   }
}
