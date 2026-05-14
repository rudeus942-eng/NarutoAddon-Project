
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KageManager {
   private static KageManager instance;
   private final Map<String, UUID> villageKages = new HashMap();

   public static KageManager getInstance() {
      if (instance == null) {
         instance = new KageManager();
      }

      return instance;
   }

   public static void reset() {
      instance = null;
   }

   public boolean setKage(VillageHelper.Village village, EntityPlayerMP player) {
      if (village == VillageHelper.Village.UNKNOWN) {
         return false;
      } else {
         VillageHelper.Village playerVillage = VillageHelper.getVillage(player);
         if (playerVillage != village) {
            return false;
         } else {
            UUID playerId = player.getUniqueID();
            this.villageKages.values().removeIf((uuid) -> uuid.equals(playerId));
            this.villageKages.put(village.teamName, playerId);
            return true;
         }
      }
   }

   public void setKageDirect(VillageHelper.Village village, UUID playerId) {
      if (village != VillageHelper.Village.UNKNOWN) {
         this.villageKages.values().removeIf((uuid) -> uuid.equals(playerId));
         this.villageKages.put(village.teamName, playerId);
      }
   }

   public void removeKage(VillageHelper.Village village) {
      if (village != VillageHelper.Village.UNKNOWN) {
         this.villageKages.remove(village.teamName);
      }
   }

   @Nullable
   public UUID getKage(VillageHelper.Village village) {
      return village == VillageHelper.Village.UNKNOWN ? null : (UUID)this.villageKages.get(village.teamName);
   }

   public boolean isKage(UUID playerId) {
      return this.villageKages.containsValue(playerId);
   }

   @Nullable
   public VillageHelper.Village getKageVillage(UUID playerId) {
      for(Map.Entry<String, UUID> entry : this.villageKages.entrySet()) {
         if (((UUID)entry.getValue()).equals(playerId)) {
            return this.teamNameToVillage((String)entry.getKey());
         }
      }

      return null;
   }

   public boolean isKageOnline(VillageHelper.Village village) {
      return this.getKagePlayer(village) != null;
   }

   @Nullable
   public EntityPlayerMP getKagePlayer(VillageHelper.Village village) {
      UUID kageId = this.getKage(village);
      if (kageId == null) {
         return null;
      } else {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         return server == null ? null : server.getPlayerList().getPlayerByUUID(kageId);
      }
   }

   public Map<String, UUID> getAllKages() {
      return new HashMap(this.villageKages);
   }

   public void validateAllKages() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         Map<String, UUID> toRemove = new HashMap();

         for(Map.Entry<String, UUID> entry : this.villageKages.entrySet()) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID((UUID)entry.getValue());
            if (player != null) {
               VillageHelper.Village village = this.teamNameToVillage((String)entry.getKey());
               if (village == null || VillageHelper.getVillage(player) != village) {
                  toRemove.put(entry.getKey(), entry.getValue());
               }
            }
         }

         for(String key : toRemove.keySet()) {
            this.villageKages.remove(key);
         }

      }
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();

      for(Map.Entry<String, UUID> entry : this.villageKages.entrySet()) {
         nbt.setString((String)entry.getKey(), ((UUID)entry.getValue()).toString());
      }

      return nbt;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.villageKages.clear();

      for(VillageHelper.Village v : VillageHelper.Village.values()) {
         if (v != VillageHelper.Village.UNKNOWN && nbt.hasKey(v.teamName)) {
            try {
               UUID id = UUID.fromString(nbt.getString(v.teamName));
               this.villageKages.put(v.teamName, id);
            } catch (IllegalArgumentException var7) {
            }
         }
      }

   }

   @Nullable
   private VillageHelper.Village teamNameToVillage(String teamName) {
      for(VillageHelper.Village v : VillageHelper.Village.values()) {
         if (v != VillageHelper.Village.UNKNOWN && v.teamName.equals(teamName)) {
            return v;
         }
      }

      return null;
   }
}
