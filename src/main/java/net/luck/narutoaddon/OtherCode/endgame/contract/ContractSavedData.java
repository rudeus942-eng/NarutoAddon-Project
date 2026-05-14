
package net.luck.narutoaddon.OtherCode.endgame.contract;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;

public class ContractSavedData extends WorldSavedData {
   private static final String DATA_NAME = "InfTsukContractData";
   private final List<ContractDefinition> activeContracts = new ArrayList();
   private final Map<UUID, String> acceptedBy = new HashMap();
   private final Map<String, Long> acceptedAt = new HashMap();
   private final Map<UUID, long[]> contractFirstClaimMs = new HashMap();
   private final Map<UUID, int[]> contractCompletionsThisWindow = new HashMap();
   private final Map<UUID, UUID> spawnedTargetEntity = new HashMap();
   private long nextContractSeq = 1L;
   private static final long DAY_MS = 86400000L;

   public ContractSavedData() {
      super("InfTsukContractData");
   }

   public ContractSavedData(String name) {
      super(name);
   }

   public static ContractSavedData get(World world) {
      MapStorage storage = world.getMapStorage();
      if (storage == null) {
         return new ContractSavedData();
      } else {
         ContractSavedData data = (ContractSavedData)storage.getOrLoadData(ContractSavedData.class, "InfTsukContractData");
         if (data == null) {
            data = new ContractSavedData();
            storage.setData("InfTsukContractData", data);
         }

         return data;
      }
   }

   public List<ContractDefinition> getActiveContracts() {
      return new ArrayList(this.activeContracts);
   }

   public void replaceActiveContracts(List<ContractDefinition> list) {
      this.activeContracts.clear();
      this.activeContracts.addAll(list);
      this.markDirty();
   }

   public void addContract(ContractDefinition c) {
      this.activeContracts.add(c);
      this.markDirty();
   }

   public boolean removeContract(String id) {
      boolean removed = this.activeContracts.removeIf((c) -> c.getId().equals(id));
      if (removed) {
         this.acceptedAt.remove(id);
         this.acceptedBy.values().removeIf(id::equals);
         this.markDirty();
      }

      return removed;
   }

   public ContractDefinition findContract(String id) {
      for(ContractDefinition c : this.activeContracts) {
         if (c.getId().equals(id)) {
            return c;
         }
      }

      return null;
   }

   public String getAcceptedContractId(UUID player) {
      return (String)this.acceptedBy.get(player);
   }

   public void acceptContract(UUID player, String contractId, long now) {
      this.acceptedBy.put(player, contractId);
      this.acceptedAt.put(contractId, now);
      this.markDirty();
   }

   public void clearAcceptance(UUID player) {
      String id = (String)this.acceptedBy.remove(player);
      if (id != null) {
         this.acceptedAt.remove(id);
         this.markDirty();
      }

      this.spawnedTargetEntity.remove(player);
   }

   public UUID getSpawnedTargetEntity(UUID player) {
      return (UUID)this.spawnedTargetEntity.get(player);
   }

   public void setSpawnedTargetEntity(UUID player, UUID entity) {
      if (entity == null) {
         this.spawnedTargetEntity.remove(player);
      } else {
         this.spawnedTargetEntity.put(player, entity);
      }

      this.markDirty();
   }

   public void clearSpawnedTargetEntity(UUID player) {
      this.spawnedTargetEntity.remove(player);
      this.markDirty();
   }

   public long getAcceptedAt(String contractId) {
      return (Long)this.acceptedAt.getOrDefault(contractId, 0L);
   }

   public UUID findAcceptor(String contractId) {
      for(Map.Entry<UUID, String> e : this.acceptedBy.entrySet()) {
         if (contractId.equals(e.getValue())) {
            return (UUID)e.getKey();
         }
      }

      return null;
   }

   public String nextContractId() {
      String id = "c" + this.nextContractSeq++;
      this.markDirty();
      return id;
   }

   public int[] getUsedToday(UUID player) {
      int[] arr = (int[])this.contractCompletionsThisWindow.get(player);
      long[] ts = (long[])this.contractFirstClaimMs.get(player);
      if (arr != null && ts != null) {
         long now = System.currentTimeMillis();
         int[] out = new int[5];

         for(int i = 0; i < 5; ++i) {
            if (ts[i] > 0L && now - ts[i] <= 86400000L) {
               out[i] = arr[i];
            }
         }

         return out;
      } else {
         return new int[5];
      }
   }

   public int recordCompletion(UUID player, int rankOrdinal) {
      if (rankOrdinal >= 0 && rankOrdinal <= 4) {
         long[] ts = (long[])this.contractFirstClaimMs.computeIfAbsent(player, (k) -> new long[5]);
         int[] arr = (int[])this.contractCompletionsThisWindow.computeIfAbsent(player, (k) -> new int[5]);
         long now = System.currentTimeMillis();
         if (ts[rankOrdinal] == 0L || now - ts[rankOrdinal] > 86400000L) {
            ts[rankOrdinal] = now;
            arr[rankOrdinal] = 0;
         }

         int var10002 = arr[rankOrdinal]++;
         this.markDirty();
         return arr[rankOrdinal];
      } else {
         return 0;
      }
   }

   public long msUntilReset(UUID player, int rankOrdinal) {
      if (rankOrdinal >= 0 && rankOrdinal <= 4) {
         long[] ts = (long[])this.contractFirstClaimMs.get(player);
         if (ts != null && ts[rankOrdinal] != 0L) {
            long elapsed = System.currentTimeMillis() - ts[rankOrdinal];
            return Math.max(0L, 86400000L - elapsed);
         } else {
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.activeContracts.clear();
      this.acceptedBy.clear();
      this.acceptedAt.clear();
      this.contractFirstClaimMs.clear();
      this.contractCompletionsThisWindow.clear();
      this.nextContractSeq = Math.max(1L, nbt.getLong("nextSeq"));
      if (nbt.hasKey("contracts")) {
         NBTTagList list = nbt.getTagList("contracts", 10);

         for(int i = 0; i < list.tagCount(); ++i) {
            ContractDefinition c = ContractDefinition.readFromNBT(list.getCompoundTagAt(i));
            if (c != null) {
               this.activeContracts.add(c);
            }
         }
      }

      if (nbt.hasKey("acceptedBy")) {
         NBTTagCompound tag = nbt.getCompoundTag("acceptedBy");

         for(String uuidStr : tag.getKeySet()) {
            try {
               this.acceptedBy.put(UUID.fromString(uuidStr), tag.getString(uuidStr));
            } catch (IllegalArgumentException var10) {
            }
         }
      }

      if (nbt.hasKey("acceptedAt")) {
         NBTTagCompound tag = nbt.getCompoundTag("acceptedAt");

         for(String id : tag.getKeySet()) {
            this.acceptedAt.put(id, tag.getLong(id));
         }
      }

      if (nbt.hasKey("capFirstClaim")) {
         NBTTagCompound tag = nbt.getCompoundTag("capFirstClaim");

         for(String uuidStr : tag.getKeySet()) {
            try {
               NBTTagCompound sub = tag.getCompoundTag(uuidStr);
               long[] arr = new long[5];

               for(int i = 0; i < 5; ++i) {
                  arr[i] = sub.getLong("r" + i);
               }

               this.contractFirstClaimMs.put(UUID.fromString(uuidStr), arr);
            } catch (IllegalArgumentException var11) {
            }
         }
      }

      if (nbt.hasKey("capCounts")) {
         NBTTagCompound tag = nbt.getCompoundTag("capCounts");

         for(String uuidStr : tag.getKeySet()) {
            try {
               int[] arr = tag.getIntArray(uuidStr);
               if (arr.length == 5) {
                  this.contractCompletionsThisWindow.put(UUID.fromString(uuidStr), arr);
               }
            } catch (IllegalArgumentException var9) {
            }
         }
      }

      if (nbt.hasKey("spawnedTargets")) {
         NBTTagCompound tag = nbt.getCompoundTag("spawnedTargets");

         for(String uuidStr : tag.getKeySet()) {
            try {
               this.spawnedTargetEntity.put(UUID.fromString(uuidStr), UUID.fromString(tag.getString(uuidStr)));
            } catch (IllegalArgumentException var8) {
            }
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      nbt.setLong("nextSeq", this.nextContractSeq);
      NBTTagList list = new NBTTagList();

      for(ContractDefinition c : this.activeContracts) {
         list.appendTag(c.writeToNBT());
      }

      nbt.setTag("contracts", list);
      NBTTagCompound accTag = new NBTTagCompound();

      for(Map.Entry<UUID, String> e : this.acceptedBy.entrySet()) {
         accTag.setString(((UUID)e.getKey()).toString(), (String)e.getValue());
      }

      nbt.setTag("acceptedBy", accTag);
      NBTTagCompound accAtTag = new NBTTagCompound();

      for(Map.Entry<String, Long> e : this.acceptedAt.entrySet()) {
         accAtTag.setLong((String)e.getKey(), (Long)e.getValue());
      }

      nbt.setTag("acceptedAt", accAtTag);
      NBTTagCompound firstClaimTag = new NBTTagCompound();

      for(Map.Entry<UUID, long[]> e : this.contractFirstClaimMs.entrySet()) {
         long[] arr = (long[])e.getValue();
         NBTTagCompound sub = new NBTTagCompound();

         for(int i = 0; i < 5 && i < arr.length; ++i) {
            sub.setLong("r" + i, arr[i]);
         }

         firstClaimTag.setTag(((UUID)e.getKey()).toString(), sub);
      }

      nbt.setTag("capFirstClaim", firstClaimTag);
      NBTTagCompound countsTag = new NBTTagCompound();

      for(Map.Entry<UUID, int[]> e : this.contractCompletionsThisWindow.entrySet()) {
         countsTag.setIntArray(((UUID)e.getKey()).toString(), (int[])e.getValue());
      }

      nbt.setTag("capCounts", countsTag);
      NBTTagCompound spawnedTag = new NBTTagCompound();

      for(Map.Entry<UUID, UUID> e : this.spawnedTargetEntity.entrySet()) {
         spawnedTag.setString(((UUID)e.getKey()).toString(), ((UUID)e.getValue()).toString());
      }

      nbt.setTag("spawnedTargets", spawnedTag);
      return nbt;
   }
}
