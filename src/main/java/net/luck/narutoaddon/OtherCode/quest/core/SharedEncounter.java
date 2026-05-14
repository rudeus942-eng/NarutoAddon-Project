
package net.luck.narutoaddon.OtherCode.quest.core;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.*;

public class SharedEncounter {
   private static final double CONTRIBUTION_THRESHOLD = 0.3;
   private static final double PROXIMITY_RANGE = (double)50.0F;
   private final String encounterKey;
   private final String questId;
   private final int stepIndex;
   private final Set<UUID> entityUUIDs;
   private final Map<UUID, Double> playerDamage;
   private final double totalHpPool;
   private final BlockPos location;
   private final long createdTick;

   public SharedEncounter(String encounterKey, String questId, int stepIndex, Set<UUID> entityUUIDs, double totalHpPool, BlockPos location, long createdTick) {
      this.encounterKey = encounterKey;
      this.questId = questId;
      this.stepIndex = stepIndex;
      this.entityUUIDs = new HashSet(entityUUIDs);
      this.playerDamage = new HashMap();
      this.totalHpPool = totalHpPool;
      this.location = location;
      this.createdTick = createdTick;
   }

   public void addParticipant(UUID playerUUID) {
      this.playerDamage.putIfAbsent(playerUUID, (double)0.0F);
   }

   public void recordDamage(UUID playerUUID, double amount) {
      this.playerDamage.merge(playerUUID, amount, Double::sum);
   }

   public double getContributionPercent(UUID playerUUID) {
      if (this.totalHpPool <= (double)0.0F) {
         return (double)0.0F;
      } else {
         Double dmg = (Double)this.playerDamage.get(playerUUID);
         return dmg == null ? (double)0.0F : dmg / this.totalHpPool;
      }
   }

   public boolean meetsThreshold(UUID playerUUID) {
      int displayPct = (int)Math.round(this.getContributionPercent(playerUUID) * (double)100.0F);
      return displayPct >= 30;
   }

   public Set<UUID> getQualifiedPlayers() {
      Set<UUID> qualified = new HashSet();
      int thresholdPct = 30;

      for(Map.Entry<UUID, Double> entry : this.playerDamage.entrySet()) {
         if (this.totalHpPool > (double)0.0F) {
            int displayPct = (int)Math.round((Double)entry.getValue() / this.totalHpPool * (double)100.0F);
            if (displayPct >= thresholdPct) {
               qualified.add(entry.getKey());
            }
         }
      }

      return qualified;
   }

   public Set<UUID> getAllParticipants() {
      return new HashSet(this.playerDamage.keySet());
   }

   public boolean hasLivingEntities(World world) {
      if (world instanceof WorldServer) {
         WorldServer worldServer = (WorldServer)world;

         for(UUID uuid : this.entityUUIDs) {
            Entity entity = worldServer.getEntityFromUuid(uuid);
            if (entity != null && entity.isEntityAlive()) {
               return true;
            }
         }

         return false;
      } else {
         for(Entity entity : new ArrayList(world.loadedEntityList)) {
            if (entity != null && this.entityUUIDs.contains(entity.getUniqueID()) && entity.isEntityAlive()) {
               return true;
            }
         }

         return false;
      }
   }

   public String getEncounterKey() {
      return this.encounterKey;
   }

   public String getQuestId() {
      return this.questId;
   }

   public int getStepIndex() {
      return this.stepIndex;
   }

   public Set<UUID> getEntityUUIDs() {
      return this.entityUUIDs;
   }

   public Map<UUID, Double> getPlayerDamage() {
      return this.playerDamage;
   }

   public double getTotalHpPool() {
      return this.totalHpPool;
   }

   public BlockPos getLocation() {
      return this.location;
   }

   public long getCreatedTick() {
      return this.createdTick;
   }

   public double getProximityRange() {
      return (double)50.0F;
   }

   public boolean removeParticipant(UUID playerUUID) {
      this.playerDamage.remove(playerUUID);
      return this.playerDamage.isEmpty();
   }
}
