
package net.luck.narutoaddon.OtherCode.raid.rewards;

import net.luck.narutoaddon.OtherCode.item.ItemRaidBossClaimScroll;
import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.luck.narutoaddon.OtherCode.raid.core.RaidQueueManager;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class RaidRewardDistributor extends WorldSavedData {
   private static final String DATA_NAME = "RaidRewardDistributor";
   private static final String BACKUP_FILE_NAME = "raid_pending_rewards_backup.dat";
   private Map<UUID, List<PendingReward>> pendingRewards = new HashMap();
   private static double anbuMokutonDropChance = 0.01;

   public RaidRewardDistributor() {
      super("RaidRewardDistributor");
   }

   public RaidRewardDistributor(String name) {
      super(name);
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.loadFromNBTInternal(nbt);
      if (this.pendingRewards.isEmpty()) {
         this.restoreFromBackup();
      }

   }

   private void loadFromNBTInternal(NBTTagCompound nbt) {
      this.pendingRewards.clear();
      NBTTagCompound rewards = nbt.getCompoundTag("pendingRewards");

      for(String uuidStr : rewards.getKeySet()) {
         try {
            UUID uuid = UUID.fromString(uuidStr);
            NBTTagList list = rewards.getTagList(uuidStr, 10);
            List<PendingReward> playerRewards = new ArrayList();

            for(int i = 0; i < list.tagCount(); ++i) {
               PendingReward reward = PendingReward.fromNBT(list.getCompoundTagAt(i));
               if (!reward.claimed) {
                  playerRewards.add(reward);
               }
            }

            if (!playerRewards.isEmpty()) {
               this.pendingRewards.put(uuid, playerRewards);
            }
         } catch (Exception var10) {
            System.err.println("[RaidRewardDistributor] Failed to load pending rewards for " + uuidStr);
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagCompound rewards = new NBTTagCompound();

      for(Map.Entry<UUID, List<PendingReward>> entry : this.pendingRewards.entrySet()) {
         NBTTagList list = new NBTTagList();

         for(PendingReward reward : (List)entry.getValue()) {
            if (!reward.claimed) {
               list.appendTag(reward.toNBT());
            }
         }

         if (list.tagCount() > 0) {
            rewards.setTag(((UUID)entry.getKey()).toString(), list);
         }
      }

      nbt.setTag("pendingRewards", rewards);
      return nbt;
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

         File backupFile = new File(dataDir, "raid_pending_rewards_backup.dat");
         NBTTagCompound nbt = new NBTTagCompound();
         this.writeToNBT(nbt);
         CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(backupFile));
      } catch (Exception e) {
         System.err.println("[RaidRewardDistributor] Failed to save backup: " + e.getMessage());
      }

   }

   private void restoreFromBackup() {
      try {
         File worldDir = DimensionManager.getCurrentSaveRootDirectory();
         if (worldDir == null) {
            return;
         }

         File dataDir = new File(worldDir, "data");
         File backupFile = new File(dataDir, "raid_pending_rewards_backup.dat");
         if (backupFile.exists()) {
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(new FileInputStream(backupFile));
            this.loadFromNBTInternal(nbt);
            System.out.println("[RaidRewardDistributor] Restored from backup!");
         }
      } catch (Exception e) {
         System.err.println("[RaidRewardDistributor] Failed to restore from backup: " + e.getMessage());
      }

   }

   public static void setAnbuMokutonDropChance(double chance) {
      anbuMokutonDropChance = Math.max((double)0.0F, Math.min((double)1.0F, chance));
   }

   public static double getAnbuMokutonDropChance() {
      return anbuMokutonDropChance;
   }

   public static void distributeRewards(RaidInstance raid, List<EntityPlayerMP> survivors) {
      if (survivors.isEmpty()) {
         System.out.println("[RaidRewardDistributor] No survivors to reward.");
      } else {
         World world = raid.getWorld();
         RaidRewardConfig rewardConfig = RaidRewardConfig.get(world);
         RaidRewardDistributor distributor = get(world);
         if (rewardConfig != null && distributor != null) {
            String bossId = raid.getBossId();
            RaidDifficulty difficulty = raid.getDifficulty();
            String bossCategory = "boss:" + bossId + ":" + difficulty.name().toLowerCase();
            List<RaidRewardConfig.ConfiguredReward> bossRewards = rewardConfig.getRewards(bossCategory);
            int baseRyo = difficulty.getBaseRyoReward();
            System.out.println("[RaidRewardDistributor] Storing pending rewards for " + survivors.size() + " survivors");
            System.out.println("[RaidRewardDistributor] Base ryo: " + baseRyo + ", Boss rewards configured: " + bossRewards.size());
            Random rng = new Random();

            for(EntityPlayerMP player : survivors) {
               PendingReward pending = new PendingReward(bossId, difficulty.getDisplayName(), baseRyo, bossRewards);
               distributor.addPendingReward(player.getUniqueID(), pending);
               player.sendMessage(new TextComponentString(""));
               player.sendMessage(new TextComponentString("§e§lYou have unclaimed raid rewards!"));
               player.sendMessage(new TextComponentString("§6+" + baseRyo + " Ryo"));
               if ("itachi".equals(bossId) && difficulty == RaidDifficulty.ANBU && rng.nextDouble() < 0.05) {
                  Item tabletItem = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation("clansaddon", "uchiha_stone_tablet"));
                  if (tabletItem != null) {
                     ItemStack tabletStack = new ItemStack(tabletItem);
                     if (!player.inventory.addItemStackToInventory(tabletStack)) {
                        ShopSavedData shopData = ShopSavedData.get(world);
                        if (shopData != null) {
                           NBTTagCompound ov = new NBTTagCompound();
                           ov.setString("itemId", "clansaddon:uchiha_stone_tablet");
                           ov.setInteger("meta", 0);
                           ov.setInteger("count", 1);
                           shopData.addOverflowItem(player.getUniqueID(), ov);
                           player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Inventory full - Uchiha Stone Tablet sent to your bank!"));
                        }
                     }

                     player.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "" + TextFormatting.BOLD + "★ RARE DROP: " + TextFormatting.RED + "Uchiha Stone Tablet" + TextFormatting.LIGHT_PURPLE + "" + TextFormatting.BOLD + " ★"));
                     System.out.println("[RaidRewardDistributor] Uchiha Stone Tablet drop for " + player.getName() + "!");
                  }
               }

               if (difficulty == RaidDifficulty.ANBU && rng.nextDouble() < anbuMokutonDropChance) {
                  Item scrollItem = ItemRaidBossClaimScroll.getScrollForBoss(bossId);
                  if (scrollItem != null) {
                     ItemStack scrollStack = new ItemStack(scrollItem);
                     if (!player.inventory.addItemStackToInventory(scrollStack)) {
                        ShopSavedData shopData = ShopSavedData.get(world);
                        if (shopData != null) {
                           NBTTagCompound ov = new NBTTagCompound();
                           scrollStack.writeToNBT(ov);
                           shopData.addOverflowItem(player.getUniqueID(), ov);
                           player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Inventory full - Claim scroll sent to your bank!"));
                        }
                     }

                     player.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "" + TextFormatting.BOLD + "★ RARE DROP: " + TextFormatting.GOLD + scrollStack.getDisplayName() + TextFormatting.LIGHT_PURPLE + "" + TextFormatting.BOLD + " ★"));
                     System.out.println("[RaidRewardDistributor] ANBU claim scroll drop (" + bossId + ") for " + player.getName() + "!");
                  }
               }

               player.sendMessage(new TextComponentString("§7Open the §fRaid Menu §7to claim them."));
               player.sendMessage(new TextComponentString(""));
               RaidQueueManager.getInstance().sendQueueStatus(player);
            }

            distributor.markDirty();
            distributor.saveBackup();
         } else {
            System.err.println("[RaidRewardDistributor] Could not access reward config or distributor!");
         }
      }
   }

   public void addPendingReward(UUID playerUUID, PendingReward reward) {
      ((List)this.pendingRewards.computeIfAbsent(playerUUID, (k) -> new ArrayList())).add(reward);
   }

   public List<PendingReward> getPendingRewards(UUID playerUUID) {
      List<PendingReward> rewards = (List)this.pendingRewards.get(playerUUID);
      if (rewards == null) {
         return Collections.emptyList();
      } else {
         List<PendingReward> unclaimed = new ArrayList();

         for(PendingReward reward : rewards) {
            if (!reward.claimed) {
               unclaimed.add(reward);
            }
         }

         return unclaimed;
      }
   }

   public boolean hasPendingRewards(UUID playerUUID) {
      return !this.getPendingRewards(playerUUID).isEmpty();
   }

   public boolean claimRewards(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      List<PendingReward> rewards = this.getPendingRewards(uuid);
      if (rewards.isEmpty()) {
         player.sendMessage(new TextComponentString("§cYou have no pending raid rewards."));
         return false;
      } else {
         int totalRyo = 0;
         int raidCount = rewards.size();
         player.sendMessage(new TextComponentString("§6§l===== RAID REWARDS ====="));
         if (raidCount > 1) {
            player.sendMessage(new TextComponentString("§7Claiming rewards from §f" + raidCount + " raids§7:"));
         }

         for(PendingReward reward : rewards) {
            totalRyo += reward.ryo;
            player.sendMessage(new TextComponentString("§e" + reward.bossId + " (" + reward.difficultyName + "): §a+" + reward.ryo + " Ryo"));
            reward.claimed = true;
         }

         giveRyo(player, totalRyo);
         player.sendMessage(new TextComponentString(""));
         player.sendMessage(new TextComponentString("§a§lTotal Claimed: §f" + totalRyo + " Ryo"));
         player.sendMessage(new TextComponentString("§6§l========================"));
         this.cleanupClaimedRewards(uuid);
         this.markDirty();
         this.saveBackup();
         RaidQueueManager.getInstance().sendQueueStatus(player);
         return true;
      }
   }

   private void cleanupClaimedRewards(UUID playerUUID) {
      List<PendingReward> rewards = (List)this.pendingRewards.get(playerUUID);
      if (rewards != null) {
         rewards.removeIf((r) -> r.claimed);
         if (rewards.isEmpty()) {
            this.pendingRewards.remove(playerUUID);
         }
      }

   }

   public static void giveRyo(EntityPlayerMP player, int amount) {
      if (amount > 0) {
         RyoRewardHelper.grantRyoSilent(player, amount);
      }
   }

   public static RaidRewardDistributor get(World world) {
      if (world != null && world.getMapStorage() != null) {
         MapStorage storage = world.getMapStorage();
         RaidRewardDistributor instance = (RaidRewardDistributor)storage.getOrLoadData(RaidRewardDistributor.class, "RaidRewardDistributor");
         if (instance == null) {
            instance = new RaidRewardDistributor();
            storage.setData("RaidRewardDistributor", instance);
         }

         return instance;
      } else {
         return null;
      }
   }

   public static class PendingReward {
      public String bossId;
      public String difficultyName;
      public int ryo;
      public List<RaidRewardConfig.ConfiguredReward> items = new ArrayList();
      public long timestamp;
      public boolean claimed = false;

      public PendingReward() {
      }

      public PendingReward(String bossId, String difficultyName, int ryo, List<RaidRewardConfig.ConfiguredReward> items) {
         this.bossId = bossId;
         this.difficultyName = difficultyName;
         this.ryo = ryo;
         this.items = new ArrayList(items);
         this.timestamp = System.currentTimeMillis();
      }

      public NBTTagCompound toNBT() {
         NBTTagCompound nbt = new NBTTagCompound();
         nbt.setString("bossId", this.bossId);
         nbt.setString("difficulty", this.difficultyName);
         nbt.setInteger("ryo", this.ryo);
         nbt.setLong("timestamp", this.timestamp);
         nbt.setBoolean("claimed", this.claimed);
         NBTTagList itemList = new NBTTagList();

         for(RaidRewardConfig.ConfiguredReward reward : this.items) {
            itemList.appendTag(reward.writeToNBT());
         }

         nbt.setTag("items", itemList);
         return nbt;
      }

      public static PendingReward fromNBT(NBTTagCompound nbt) {
         PendingReward reward = new PendingReward();
         reward.bossId = nbt.getString("bossId");
         reward.difficultyName = nbt.getString("difficulty");
         reward.ryo = nbt.getInteger("ryo");
         reward.timestamp = nbt.getLong("timestamp");
         reward.claimed = nbt.getBoolean("claimed");
         NBTTagList itemList = nbt.getTagList("items", 10);

         for(int i = 0; i < itemList.tagCount(); ++i) {
            reward.items.add(RaidRewardConfig.ConfiguredReward.readFromNBT(itemList.getCompoundTagAt(i)));
         }

         return reward;
      }
   }
}
