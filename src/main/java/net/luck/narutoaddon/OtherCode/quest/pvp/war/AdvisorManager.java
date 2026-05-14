
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.*;

public class AdvisorManager {
   private static AdvisorManager instance;
   private static final int MAX_ADVISORS_PER_VILLAGE = 3;
   private final Map<String, List<UUID>> villageAdvisors = new HashMap();

   public static AdvisorManager getInstance() {
      if (instance == null) {
         instance = new AdvisorManager();
      }

      return instance;
   }

   public static void reset() {
      instance = null;
   }

   public boolean addAdvisor(VillageHelper.Village village, EntityPlayerMP player) {
      if (village == VillageHelper.Village.UNKNOWN) {
         return false;
      } else {
         VillageHelper.Village playerVillage = VillageHelper.getVillage(player);
         if (playerVillage != village) {
            return false;
         } else {
            UUID playerId = player.getUniqueID();

            for(List<UUID> advisors : this.villageAdvisors.values()) {
               advisors.remove(playerId);
            }

            List<UUID> advisors = (List)this.villageAdvisors.computeIfAbsent(village.teamName, (k) -> new ArrayList());
            if (advisors.size() >= 3) {
               return false;
            } else if (advisors.contains(playerId)) {
               return true;
            } else {
               advisors.add(playerId);
               return true;
            }
         }
      }
   }

   public void addAdvisorDirect(VillageHelper.Village village, UUID playerId) {
      if (village != VillageHelper.Village.UNKNOWN) {
         for(List<UUID> advisors : this.villageAdvisors.values()) {
            advisors.remove(playerId);
         }

         List<UUID> advisors = (List)this.villageAdvisors.computeIfAbsent(village.teamName, (k) -> new ArrayList());
         if (!advisors.contains(playerId) && advisors.size() < 3) {
            advisors.add(playerId);
         }

      }
   }

   public boolean removeAdvisor(VillageHelper.Village village, UUID playerId) {
      if (village == VillageHelper.Village.UNKNOWN) {
         return false;
      } else {
         List<UUID> advisors = (List)this.villageAdvisors.get(village.teamName);
         if (advisors == null) {
            return false;
         } else {
            boolean removed = advisors.remove(playerId);
            if (advisors.isEmpty()) {
               this.villageAdvisors.remove(village.teamName);
            }

            return removed;
         }
      }
   }

   public void removeAllAdvisors(VillageHelper.Village village) {
      if (village != VillageHelper.Village.UNKNOWN) {
         this.villageAdvisors.remove(village.teamName);
      }
   }

   public List<UUID> getAdvisors(VillageHelper.Village village) {
      if (village == VillageHelper.Village.UNKNOWN) {
         return new ArrayList();
      } else {
         List<UUID> advisors = (List)this.villageAdvisors.get(village.teamName);
         return advisors != null ? new ArrayList(advisors) : new ArrayList();
      }
   }

   public boolean isAdvisor(UUID playerId) {
      for(List<UUID> advisors : this.villageAdvisors.values()) {
         if (advisors.contains(playerId)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public VillageHelper.Village getAdvisorVillage(UUID playerId) {
      for(Map.Entry<String, List<UUID>> entry : this.villageAdvisors.entrySet()) {
         if (((List)entry.getValue()).contains(playerId)) {
            return this.teamNameToVillage((String)entry.getKey());
         }
      }

      return null;
   }

   public boolean isLeadership(UUID playerId) {
      return KageManager.getInstance().isKage(playerId) || this.isAdvisor(playerId);
   }

   public boolean hasLeadershipAuthority(UUID playerId, VillageHelper.Village village) {
      if (village == VillageHelper.Village.UNKNOWN) {
         return false;
      } else {
         UUID kageId = KageManager.getInstance().getKage(village);
         if (kageId != null && kageId.equals(playerId)) {
            return true;
         } else {
            VillageHelper.Village advisorVillage = this.getAdvisorVillage(playerId);
            return advisorVillage == village && !KageManager.getInstance().isKageOnline(village);
         }
      }
   }

   public void validateAllAdvisors() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         for(Map.Entry<String, List<UUID>> entry : (new HashMap(this.villageAdvisors)).entrySet()) {
            VillageHelper.Village village = this.teamNameToVillage((String)entry.getKey());
            if (village == null) {
               this.villageAdvisors.remove(entry.getKey());
            } else {
               List<UUID> toRemove = new ArrayList();

               for(UUID advisorId : (List)entry.getValue()) {
                  EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(advisorId);
                  if (player != null && VillageHelper.getVillage(player) != village) {
                     toRemove.add(advisorId);
                  }
               }

               ((List)entry.getValue()).removeAll(toRemove);
               if (((List)entry.getValue()).isEmpty()) {
                  this.villageAdvisors.remove(entry.getKey());
               }
            }
         }

      }
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();

      for(Map.Entry<String, List<UUID>> entry : this.villageAdvisors.entrySet()) {
         NBTTagList list = new NBTTagList();

         for(UUID uuid : (List)entry.getValue()) {
            list.appendTag(new NBTTagString(uuid.toString()));
         }

         nbt.setTag((String)entry.getKey(), list);
      }

      return nbt;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.villageAdvisors.clear();

      for(VillageHelper.Village v : VillageHelper.Village.values()) {
         if (v != VillageHelper.Village.UNKNOWN && nbt.hasKey(v.teamName)) {
            NBTTagList list = nbt.getTagList(v.teamName, 8);
            List<UUID> advisors = new ArrayList();

            for(int i = 0; i < list.tagCount(); ++i) {
               try {
                  advisors.add(UUID.fromString(list.getStringTagAt(i)));
               } catch (IllegalArgumentException var10) {
               }
            }

            if (!advisors.isEmpty()) {
               this.villageAdvisors.put(v.teamName, advisors);
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
