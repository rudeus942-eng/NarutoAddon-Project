package net.luck.narutoaddon.OtherCode.akatsuki.market;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlackMarketDeployment {
   private final String deploymentId;
   private final String itemId;
   private final UUID purchaserId;
   private String currentZoneId;
   private final long startTimeMs;
   private final long endTimeMs;
   private final boolean roaming;
   private final List<UUID> spawnedNpcUUIDs;
   private boolean completed;
   private boolean npcsSpawned;
   private int zonesCapped;

   public BlackMarketDeployment(String deploymentId, String itemId, UUID purchaserId, String zoneId, long durationMs, boolean roaming) {
      this.deploymentId = deploymentId;
      this.itemId = itemId;
      this.purchaserId = purchaserId;
      this.currentZoneId = zoneId;
      this.startTimeMs = System.currentTimeMillis();
      this.endTimeMs = this.startTimeMs + durationMs;
      this.roaming = roaming;
      this.spawnedNpcUUIDs = new ArrayList();
      this.completed = false;
      this.npcsSpawned = false;
      this.zonesCapped = 0;
   }

   public String getDeploymentId() {
      return this.deploymentId;
   }

   public String getItemId() {
      return this.itemId;
   }

   public UUID getPurchaserId() {
      return this.purchaserId;
   }

   public String getCurrentZoneId() {
      return this.currentZoneId;
   }

   public boolean isRoaming() {
      return this.roaming;
   }

   public List<UUID> getSpawnedNpcUUIDs() {
      return this.spawnedNpcUUIDs;
   }

   public boolean isCompleted() {
      return this.completed;
   }

   public boolean isNpcsSpawned() {
      return this.npcsSpawned;
   }

   public int getZonesCapped() {
      return this.zonesCapped;
   }

   public void setCurrentZoneId(String zoneId) {
      this.currentZoneId = zoneId;
   }

   public void setCompleted(boolean completed) {
      this.completed = completed;
   }

   public void setNpcsSpawned(boolean spawned) {
      this.npcsSpawned = spawned;
   }

   public void incrementZonesCapped() {
      ++this.zonesCapped;
   }

   public void addNpcUUID(UUID uuid) {
      this.spawnedNpcUUIDs.add(uuid);
   }

   public boolean isExpired() {
      return System.currentTimeMillis() >= this.endTimeMs;
   }

   public long getRemainingMs() {
      return Math.max(0L, this.endTimeMs - System.currentTimeMillis());
   }

   public String getFormattedTimeRemaining() {
      long remaining = this.getRemainingMs();
      long seconds = remaining / 1000L % 60L;
      long minutes = remaining / 60000L % 60L;
      return String.format("%dm %02ds", minutes, seconds);
   }

   public boolean areAllNpcsDead(World world) {
      for(UUID npcUUID : this.spawnedNpcUUIDs) {
         Entity entity = findEntityByUUID(world, npcUUID);
         if (entity != null && entity.isEntityAlive()) {
            return false;
         }
      }

      return !this.spawnedNpcUUIDs.isEmpty();
   }

   private static Entity findEntityByUUID(World world, UUID uuid) {
      for(Entity entity : world.loadedEntityList) {
         if (entity.getUniqueID().equals(uuid)) {
            return entity;
         }
      }

      return null;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("deploymentId", this.deploymentId);
      nbt.setString("itemId", this.itemId);
      nbt.setString("purchaserId", this.purchaserId.toString());
      nbt.setString("currentZoneId", this.currentZoneId);
      nbt.setLong("startTimeMs", this.startTimeMs);
      nbt.setLong("endTimeMs", this.endTimeMs);
      nbt.setBoolean("roaming", this.roaming);
      nbt.setBoolean("completed", this.completed);
      nbt.setInteger("zonesCapped", this.zonesCapped);
      NBTTagList npcList = new NBTTagList();

      for(UUID uuid : this.spawnedNpcUUIDs) {
         NBTTagCompound entry = new NBTTagCompound();
         entry.setString("uuid", uuid.toString());
         npcList.appendTag(entry);
      }

      nbt.setTag("npcs", npcList);
      return nbt;
   }

   public static BlackMarketDeployment readFromNBT(NBTTagCompound nbt) {
      BlackMarketDeployment dep = new BlackMarketDeployment(nbt.getString("deploymentId"), nbt.getString("itemId"), UUID.fromString(nbt.getString("purchaserId")), nbt.getString("currentZoneId"), 0L, nbt.getBoolean("roaming"));

      try {
         Field endField = BlackMarketDeployment.class.getDeclaredField("endTimeMs");
         endField.setAccessible(true);
         endField.setLong(dep, nbt.getLong("endTimeMs"));
         Field startField = BlackMarketDeployment.class.getDeclaredField("startTimeMs");
         startField.setAccessible(true);
         startField.setLong(dep, nbt.getLong("startTimeMs"));
      } catch (Exception var6) {
      }

      dep.completed = nbt.getBoolean("completed");
      dep.zonesCapped = nbt.getInteger("zonesCapped");
      NBTTagList npcList = nbt.getTagList("npcs", 10);

      for(int i = 0; i < npcList.tagCount(); ++i) {
         try {
            dep.spawnedNpcUUIDs.add(UUID.fromString(npcList.getCompoundTagAt(i).getString("uuid")));
         } catch (IllegalArgumentException var5) {
         }
      }

      return dep;
   }
}
