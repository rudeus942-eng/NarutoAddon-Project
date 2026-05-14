
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.*;

public class OperationTracker {
   private String orderId;
   private String templateId;
   private String villageName;
   private final Map<Integer, Set<UUID>> tierCompletions = new HashMap();
   private final Set<UUID> participants = new HashSet();
   private boolean bonusPaid;

   public OperationTracker(String orderId, String templateId, String villageName) {
      this.orderId = orderId;
      this.templateId = templateId;
      this.villageName = villageName;
      this.bonusPaid = false;
   }

   private OperationTracker() {
      this.bonusPaid = false;
   }

   public String getOrderId() {
      return this.orderId;
   }

   public String getTemplateId() {
      return this.templateId;
   }

   public String getVillageName() {
      return this.villageName;
   }

   public void recordTierCompletion(UUID playerId, QuestDefinition.QuestRank rank) {
      ((Set)this.tierCompletions.computeIfAbsent(rank.ordinal(), (k) -> new HashSet())).add(playerId);
   }

   public boolean hasTierCompletion(QuestDefinition.QuestRank rank) {
      Set<UUID> completers = (Set)this.tierCompletions.get(rank.ordinal());
      return completers != null && !completers.isEmpty();
   }

   public int getCompletedTierCount() {
      int count = 0;

      for(Map.Entry<Integer, Set<UUID>> entry : this.tierCompletions.entrySet()) {
         if (entry.getValue() != null && !((Set)entry.getValue()).isEmpty()) {
            ++count;
         }
      }

      return count;
   }

   public float getBonusMultiplier() {
      int completed = this.getCompletedTierCount();
      return completed <= 1 ? 0.0F : (float)(completed - 1) * 0.2F;
   }

   public boolean isFullCompletion() {
      return this.getCompletedTierCount() >= 6;
   }

   public void addParticipant(UUID playerId) {
      this.participants.add(playerId);
   }

   public Set<UUID> getParticipants() {
      return Collections.unmodifiableSet(this.participants);
   }

   public boolean isBonusPaid() {
      return this.bonusPaid;
   }

   public void markBonusPaid() {
      this.bonusPaid = true;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("orderId", this.orderId);
      nbt.setString("templateId", this.templateId);
      nbt.setString("villageName", this.villageName);
      nbt.setBoolean("bonusPaid", this.bonusPaid);
      NBTTagCompound tierTag = new NBTTagCompound();

      for(Map.Entry<Integer, Set<UUID>> entry : this.tierCompletions.entrySet()) {
         NBTTagList uuidList = new NBTTagList();

         for(UUID uuid : (Set)entry.getValue()) {
            uuidList.appendTag(new NBTTagString(uuid.toString()));
         }

         tierTag.setTag(String.valueOf(entry.getKey()), uuidList);
      }

      nbt.setTag("tierCompletions", tierTag);
      NBTTagList partList = new NBTTagList();

      for(UUID uuid : this.participants) {
         partList.appendTag(new NBTTagString(uuid.toString()));
      }

      nbt.setTag("participants", partList);
      return nbt;
   }

   public static OperationTracker readFromNBT(NBTTagCompound nbt) {
      OperationTracker tracker = new OperationTracker();
      tracker.orderId = nbt.getString("orderId");
      tracker.templateId = nbt.getString("templateId");
      tracker.villageName = nbt.getString("villageName");
      tracker.bonusPaid = nbt.getBoolean("bonusPaid");
      if (nbt.hasKey("tierCompletions")) {
         NBTTagCompound tierTag = nbt.getCompoundTag("tierCompletions");

         for(String key : tierTag.getKeySet()) {
            try {
               int rankOrdinal = Integer.parseInt(key);
               NBTTagList uuidList = tierTag.getTagList(key, 8);
               Set<UUID> uuids = new HashSet();

               for(int i = 0; i < uuidList.tagCount(); ++i) {
                  try {
                     uuids.add(UUID.fromString(uuidList.getStringTagAt(i)));
                  } catch (IllegalArgumentException var11) {
                  }
               }

               if (!uuids.isEmpty()) {
                  tracker.tierCompletions.put(rankOrdinal, uuids);
               }
            } catch (NumberFormatException var12) {
            }
         }
      }

      if (nbt.hasKey("participants")) {
         NBTTagList partList = nbt.getTagList("participants", 8);

         for(int i = 0; i < partList.tagCount(); ++i) {
            try {
               tracker.participants.add(UUID.fromString(partList.getStringTagAt(i)));
            } catch (IllegalArgumentException var10) {
            }
         }
      }

      return tracker;
   }
}
