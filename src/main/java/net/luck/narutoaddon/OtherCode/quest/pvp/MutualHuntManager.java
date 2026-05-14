
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class MutualHuntManager {
   private static MutualHuntManager INSTANCE;
   private final Map<UUID, UUID> activeMutualHunts = new HashMap();
   private final Random random = new Random();
   private static final int MIN_TARGET_BATTLE_XP = 100;

   private MutualHuntManager() {
   }

   public static MutualHuntManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new MutualHuntManager();
      }

      return INSTANCE;
   }

   @Nullable
   public UUID createMutualHunt(UUID player1UUID, VillageHelper.Village player1Village, World world) {
      if (this.activeMutualHunts.containsKey(player1UUID)) {
         return null;
      } else if (player1Village == VillageHelper.Village.UNKNOWN) {
         return null;
      } else {
         UUID opponentUUID = this.findSuitableOpponent(player1Village, player1UUID, world);
         if (opponentUUID == null) {
            return null;
         } else {
            this.activeMutualHunts.put(player1UUID, opponentUUID);
            this.activeMutualHunts.put(opponentUUID, player1UUID);
            PvpSavedData data = PvpSavedData.get(world);
            data.setMutualHunt(player1UUID, opponentUUID);
            return opponentUUID;
         }
      }
   }

   public boolean onMutualHuntKill(UUID killerUUID, UUID victimUUID, World world) {
      UUID target = (UUID)this.activeMutualHunts.get(killerUUID);
      if (target != null && target.equals(victimUUID)) {
         this.cancelMutualHunt(killerUUID, world);
         return true;
      } else {
         return false;
      }
   }

   public void cancelMutualHunt(UUID playerUUID, World world) {
      UUID other = (UUID)this.activeMutualHunts.remove(playerUUID);
      if (other != null) {
         this.activeMutualHunts.remove(other);
      }

      PvpSavedData data = PvpSavedData.get(world);
      data.removeMutualHunt(playerUUID);
   }

   @Nullable
   public UUID getMutualTarget(UUID playerUUID) {
      return (UUID)this.activeMutualHunts.get(playerUUID);
   }

   public boolean isInMutualHunt(UUID playerUUID) {
      return this.activeMutualHunts.containsKey(playerUUID);
   }

   @Nullable
   private UUID findSuitableOpponent(VillageHelper.Village playerVillage, UUID excludeUUID, World world) {
      MinecraftServer server = world.getMinecraftServer();
      if (server == null) {
         return null;
      } else {
         List<EntityPlayerMP> candidates = new ArrayList();
         List<Float> weights = new ArrayList();
         float totalWeight = 0.0F;

         for(EntityPlayerMP other : server.getPlayerList().getPlayers()) {
            if (!other.getUniqueID().equals(excludeUUID) && !this.activeMutualHunts.containsKey(other.getUniqueID())) {
               VillageHelper.Village otherVillage = VillageHelper.getVillage(other);
               if (otherVillage != playerVillage && otherVillage != VillageHelper.Village.UNKNOWN) {
                  int battleXp = getBattleXp(other);
                  if (battleXp >= 100) {
                     float rivalryWeight = VillageRivalryManager.getEffectiveWeight(playerVillage, otherVillage);
                     if (rivalryWeight <= 0.0F) {
                        rivalryWeight = 0.1F;
                     }

                     candidates.add(other);
                     weights.add(rivalryWeight);
                     totalWeight += rivalryWeight;
                  }
               }
            }
         }

         if (candidates.isEmpty()) {
            return null;
         } else {
            float roll = this.random.nextFloat() * totalWeight;
            float cumulative = 0.0F;

            for(int i = 0; i < candidates.size(); ++i) {
               cumulative += (Float)weights.get(i);
               if (roll < cumulative) {
                  return ((EntityPlayerMP)candidates.get(i)).getUniqueID();
               }
            }

            return ((EntityPlayerMP)candidates.get(candidates.size() - 1)).getUniqueID();
         }
      }
   }

   private static int getBattleXp(EntityPlayerMP player) {
      try {
         return (int)player.getEntityData().getDouble("battle_experience");
      } catch (Exception var2) {
         return 0;
      }
   }

   public void loadFromSavedData(PvpSavedData data) {
      this.activeMutualHunts.clear();
   }

   public void syncFromSavedData(World world) {
      this.activeMutualHunts.clear();
      PvpSavedData data = PvpSavedData.get(world);
      MinecraftServer server = world.getMinecraftServer();
      if (server != null) {
         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            UUID target = data.getMutualHuntTarget(player.getUniqueID());
            if (target != null) {
               this.activeMutualHunts.put(player.getUniqueID(), target);
            }
         }

      }
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();

      for(Map.Entry<UUID, UUID> entry : this.activeMutualHunts.entrySet()) {
         nbt.setString(((UUID)entry.getKey()).toString(), ((UUID)entry.getValue()).toString());
      }

      return nbt;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.activeMutualHunts.clear();

      for(String uuidStr : nbt.getKeySet()) {
         try {
            UUID player = UUID.fromString(uuidStr);
            UUID target = UUID.fromString(nbt.getString(uuidStr));
            this.activeMutualHunts.put(player, target);
         } catch (IllegalArgumentException var6) {
         }
      }

   }
}
