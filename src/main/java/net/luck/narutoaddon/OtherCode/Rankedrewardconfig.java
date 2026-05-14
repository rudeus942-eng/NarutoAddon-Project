
package net.luck.narutoaddon.OtherCode;

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
import java.util.*;

public class Rankedrewardconfig extends WorldSavedData {
   private static final String DATA_NAME = "RankedRewardConfig";
   private static final String BACKUP_FILE_NAME = "ranked_rewards_backup.dat";
   private static final String BACKUP_FILE_NAME_2 = "ranked_rewards_backup2.dat";
   public static final String CATEGORY_RANKUP = "rankup";
   public static final String CATEGORY_STREAK = "streak";
   public static final String CATEGORY_POSITION = "position";
   public static final String CATEGORY_SEASON = "season";
   private static World cachedWorld = null;
   private Map<String, List<ConfiguredReward>> rewards = new HashMap();
   public static final String[] VALID_TIERS = new String[]{"Genin", "Chunin", "Jonin", "S. Jonin", "Elite Jonin", "ANBU", "Kage", "Otsutsuki"};

   public Rankedrewardconfig() {
      super("RankedRewardConfig");
   }

   public Rankedrewardconfig(String name) {
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

      System.out.println("[RankedRewards] Loaded " + this.rewards.size() + " reward categories from NBT");
      if (this.rewards.isEmpty()) {
         System.out.println("[RankedRewards] WARNING: Main data empty, attempting backup recovery...");
         this.tryRecoverFromBackup();
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
      nbt.setLong("lastSaved", System.currentTimeMillis());
      System.out.println("[RankedRewards] Writing " + this.rewards.size() + " reward categories to NBT");
      return nbt;
   }

   public static Rankedrewardconfig get(World world) {
      if (world != null && !world.isRemote) {
         cachedWorld = world;
         MapStorage storage = world.getMapStorage();
         if (storage == null) {
            System.out.println("[RankedRewards] ERROR: MapStorage is null!");
            return null;
         } else {
            Rankedrewardconfig instance = (Rankedrewardconfig)storage.getOrLoadData(Rankedrewardconfig.class, "RankedRewardConfig");
            if (instance == null) {
               System.out.println("[RankedRewards] Creating new instance, checking for backup...");
               instance = new Rankedrewardconfig();
               instance.tryRecoverFromBackup();
               storage.setData("RankedRewardConfig", instance);
               instance.markDirty();
            }

            return instance;
         }
      } else {
         return null;
      }
   }

   private static File getBackupFile(int index) {
      File worldDir = DimensionManager.getCurrentSaveRootDirectory();
      if (worldDir == null) {
         worldDir = new File(".");
      }

      String fileName = index == 1 ? "ranked_rewards_backup.dat" : "ranked_rewards_backup2.dat";
      return new File(worldDir, fileName);
   }

   private void saveToBackup() {
      if (this.rewards.isEmpty()) {
         System.out.println("[RankedRewards] Skipping backup - no rewards to save");
      } else {
         NBTTagCompound nbt = new NBTTagCompound();
         this.writeToNBT(nbt);
         nbt.setLong("backupTime", System.currentTimeMillis());
         nbt.setInteger("rewardCount", this.countTotalRewards());
         File backup1 = getBackupFile(1);
         File backup2 = getBackupFile(2);

         try {
            if (backup1.exists()) {
               NBTTagCompound existing = CompressedStreamTools.readCompressed(new FileInputStream(backup1));
               CompressedStreamTools.writeCompressed(existing, new FileOutputStream(backup2));
            }

            CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(backup1));
            System.out.println("[RankedRewards] Backup saved: " + this.countTotalRewards() + " rewards to " + backup1.getAbsolutePath());
         } catch (Exception e) {
            System.out.println("[RankedRewards] ERROR saving backup: " + e.getMessage());
            e.printStackTrace();

            try {
               CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(backup2));
               System.out.println("[RankedRewards] Fallback backup saved to " + backup2.getAbsolutePath());
            } catch (Exception var6) {
               System.out.println("[RankedRewards] CRITICAL: Could not save any backup!");
            }
         }

      }
   }

   private void tryRecoverFromBackup() {
      File backup1 = getBackupFile(1);
      File backup2 = getBackupFile(2);
      if (this.tryLoadBackup(backup1)) {
         System.out.println("[RankedRewards] Successfully recovered from primary backup");
      } else if (this.tryLoadBackup(backup2)) {
         System.out.println("[RankedRewards] Successfully recovered from secondary backup");
      } else {
         System.out.println("[RankedRewards] No backup files found or backups are empty");
      }
   }

   private boolean tryLoadBackup(File backupFile) {
      if (!backupFile.exists()) {
         return false;
      } else {
         try {
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(new FileInputStream(backupFile));
            if (!nbt.hasKey("rewards")) {
               System.out.println("[RankedRewards] Backup file invalid (no rewards tag): " + backupFile.getName());
               return false;
            } else {
               int rewardCount = nbt.getInteger("rewardCount");
               if (rewardCount == 0) {
                  System.out.println("[RankedRewards] Backup file empty: " + backupFile.getName());
                  return false;
               } else {
                  this.readFromNBT(nbt);
                  long backupTime = nbt.getLong("backupTime");
                  System.out.println("[RankedRewards] Recovered " + this.countTotalRewards() + " rewards from backup (saved at " + new Date(backupTime) + ")");
                  return !this.rewards.isEmpty();
               }
            }
         } catch (Exception e) {
            System.out.println("[RankedRewards] Error reading backup " + backupFile.getName() + ": " + e.getMessage());
            return false;
         }
      }
   }

   private int countTotalRewards() {
      int count = 0;

      for(List<ConfiguredReward> list : this.rewards.values()) {
         count += list.size();
      }

      return count;
   }

   public void forceSave() {
      this.markDirty();
      this.saveToBackup();
      if (cachedWorld != null && cachedWorld.getMapStorage() != null) {
         try {
            cachedWorld.getMapStorage().saveAllData();
            System.out.println("[RankedRewards] Forced save complete");
         } catch (Exception e) {
            System.out.println("[RankedRewards] Warning: Could not force MapStorage save: " + e.getMessage());
         }
      }

   }

   public void addReward(String category, String key, ConfiguredReward reward) {
      String fullKey = category + ":" + key;
      ((List)this.rewards.computeIfAbsent(fullKey, (k) -> new ArrayList())).add(reward);
      System.out.println("[RankedRewards] Added reward: " + reward.getDisplayName() + " to " + fullKey);
      this.forceSave();
   }

   public boolean removeReward(String category, String key, int index) {
      String fullKey = category + ":" + key;
      List<ConfiguredReward> list = (List)this.rewards.get(fullKey);
      if (list != null && index >= 0 && index < list.size()) {
         ConfiguredReward removed = (ConfiguredReward)list.remove(index);
         if (list.isEmpty()) {
            this.rewards.remove(fullKey);
         }

         System.out.println("[RankedRewards] Removed reward: " + removed.getDisplayName() + " from " + fullKey);
         this.forceSave();
         return true;
      } else {
         return false;
      }
   }

   public void clearRewards(String category, String key) {
      String fullKey = category + ":" + key;
      this.rewards.remove(fullKey);
      System.out.println("[RankedRewards] Cleared all rewards for " + fullKey);
      this.forceSave();
   }

   public List<ConfiguredReward> getRewards(String category, String key) {
      String fullKey = category + ":" + key;
      return (List)this.rewards.getOrDefault(fullKey, new ArrayList());
   }

   public List<ItemStack> getAllRewardItems(String category, String key) {
      List<ItemStack> items = new ArrayList();

      for(ConfiguredReward reward : this.getRewards(category, key)) {
         ItemStack stack = reward.toItemStack();
         if (!stack.isEmpty()) {
            items.add(stack);
         }
      }

      return items;
   }

   public List<ConfiguredReward> getAllRewardConfigs(String category, String key) {
      return new ArrayList(this.getRewards(category, key));
   }

   public boolean hasRewards(String category, String key) {
      String fullKey = category + ":" + key;
      List<ConfiguredReward> list = (List)this.rewards.get(fullKey);
      return list != null && !list.isEmpty();
   }

   public List<String> getKeysForCategory(String category) {
      List<String> keys = new ArrayList();
      String prefix = category + ":";

      for(String fullKey : this.rewards.keySet()) {
         if (fullKey.startsWith(prefix)) {
            keys.add(fullKey.substring(prefix.length()));
         }
      }

      return keys;
   }

   public int getTotalRewardCount() {
      return this.countTotalRewards();
   }

   public void debugPrintAllRewards() {
      System.out.println("[RankedRewards] === All Configured Rewards ===");

      for(Map.Entry<String, List<ConfiguredReward>> entry : this.rewards.entrySet()) {
         System.out.println("[RankedRewards] " + (String)entry.getKey() + ":");

         for(ConfiguredReward reward : (List)entry.getValue()) {
            System.out.println("[RankedRewards]   - " + reward.getDisplayName());
         }
      }

      System.out.println("[RankedRewards] Total: " + this.countTotalRewards() + " rewards");
   }

   public void addRankUpReward(String tierName, ConfiguredReward reward) {
      this.addReward("rankup", tierName, reward);
   }

   public List<ConfiguredReward> getRankUpRewards(String tierName) {
      return this.getRewards("rankup", tierName);
   }

   public void addStreakReward(int streak, ConfiguredReward reward) {
      this.addReward("streak", String.valueOf(streak), reward);
   }

   public List<ConfiguredReward> getStreakRewards(int streak) {
      return this.getRewards("streak", String.valueOf(streak));
   }

   /** @deprecated */
   @Deprecated
   public void addPositionReward(int position, ConfiguredReward reward) {
      this.addPositionReward(0, position, reward);
   }

   /** @deprecated */
   @Deprecated
   public List<ConfiguredReward> getPositionRewards(int position) {
      return this.getPositionRewards(0, position);
   }

   public void addPositionReward(int seasonNumber, int position, ConfiguredReward reward) {
      String key = seasonNumber + ":" + position;
      this.addReward("position", key, reward);
   }

   public List<ConfiguredReward> getPositionRewards(int seasonNumber, int position) {
      String key = seasonNumber + ":" + position;
      return this.getRewards("position", key);
   }

   public boolean removePositionReward(int seasonNumber, int position, int index) {
      String key = seasonNumber + ":" + position;
      return this.removeReward("position", key, index);
   }

   public void clearPositionRewards(int seasonNumber, int position) {
      String key = seasonNumber + ":" + position;
      this.clearRewards("position", key);
   }

   public boolean hasPositionRewards(int seasonNumber, int position) {
      String key = seasonNumber + ":" + position;
      return this.hasRewards("position", key);
   }

   public List<Integer> getConfiguredPositionsForSeason(int seasonNumber) {
      List<Integer> positions = new ArrayList();
      String prefix = seasonNumber + ":";

      for(String key : this.getKeysForCategory("position")) {
         if (key.startsWith(prefix)) {
            try {
               int pos = Integer.parseInt(key.substring(prefix.length()));
               positions.add(pos);
            } catch (NumberFormatException var7) {
            }
         }
      }

      Collections.sort(positions);
      return positions;
   }

   public void addSeasonReward(String tierName, ConfiguredReward reward) {
      this.addReward("season", tierName, reward);
   }

   public List<ConfiguredReward> getSeasonRewards(String tierName) {
      return this.getRewards("season", tierName);
   }

   public boolean removeSeasonReward(String tierName, int index) {
      return this.removeReward("season", tierName, index);
   }

   public void clearSeasonRewards(String tierName) {
      this.clearRewards("season", tierName);
   }

   public boolean hasSeasonRewards(String tierName) {
      return this.hasRewards("season", tierName);
   }

   public int clearOldSeasonFormat() {
      List<String> keysToRemove = new ArrayList();
      String prefix = "season:";

      for(String fullKey : this.rewards.keySet()) {
         if (fullKey.startsWith(prefix)) {
            String key = fullKey.substring(prefix.length());
            if (key.contains(":")) {
               keysToRemove.add(fullKey);
            }
         }
      }

      for(String key : keysToRemove) {
         this.rewards.remove(key);
         System.out.println("[RankedRewards] Cleared old format key: " + key);
      }

      if (!keysToRemove.isEmpty()) {
         this.forceSave();
      }

      return keysToRemove.size();
   }

   public static boolean isValidTier(String tier) {
      for(String valid : VALID_TIERS) {
         if (valid.equalsIgnoreCase(tier)) {
            return true;
         }
      }

      return false;
   }

   public static String normalizeTierName(String tier) {
      for(String valid : VALID_TIERS) {
         if (valid.equalsIgnoreCase(tier)) {
            return valid;
         }
      }

      String lower = tier.toLowerCase().replace(" ", "").replace("_", "").replace(".", "");
      if (!lower.equals("sjonin") && !lower.equals("specialjonin")) {
         if (lower.equals("elitejonin")) {
            return "Elite Jonin";
         } else {
            return tier;
         }
      } else {
         return "S. Jonin";
      }
   }

   public static class ConfiguredReward {
      public String itemId;
      public int amount;
      public int metadata;
      public NBTTagCompound nbt;
      public int weight;

      public ConfiguredReward() {
         this.weight = 1;
      }

      public ConfiguredReward(ItemStack stack) {
         this.itemId = ((ResourceLocation)Item.REGISTRY.getNameForObject(stack.getItem())).toString();
         this.amount = stack.getCount();
         this.metadata = stack.getMetadata();
         this.nbt = stack.hasTagCompound() ? stack.getTagCompound().copy() : null;
         this.weight = 1;
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
         tag.setInteger("weight", this.weight);
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
         reward.weight = tag.hasKey("weight") ? tag.getInteger("weight") : 1;
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
