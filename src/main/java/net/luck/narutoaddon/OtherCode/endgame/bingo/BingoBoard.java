
package net.luck.narutoaddon.OtherCode.endgame.bingo;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BingoBoard {
   private final List<BingoSlot> slots;
   private final long generatedTime;
   private final String tierName;
   private final int tierOrdinal;

   public BingoBoard(List<BingoSlot> slots, long generatedTime, String tierName) {
      this(slots, generatedTime, tierName, 0);
   }

   public BingoBoard(List<BingoSlot> slots, long generatedTime, String tierName, int tierOrdinal) {
      this.slots = slots;
      this.generatedTime = generatedTime;
      this.tierName = tierName;
      this.tierOrdinal = tierOrdinal;
   }

   public List<BingoSlot> getSlots() {
      return Collections.unmodifiableList(this.slots);
   }

   public BingoSlot getSlot(int index) {
      return index >= 0 && index < this.slots.size() ? (BingoSlot)this.slots.get(index) : null;
   }

   public long getGeneratedTime() {
      return this.generatedTime;
   }

   public String getTierName() {
      return this.tierName;
   }

   public int getTierOrdinal() {
      return this.tierOrdinal;
   }

   public int getSlotCount() {
      return this.slots.size();
   }

   public boolean isAllComplete() {
      for(BingoSlot slot : this.slots) {
         if (!slot.isCompleted()) {
            return false;
         }
      }

      return true;
   }

   public int getCompletedCount() {
      int count = 0;

      for(BingoSlot slot : this.slots) {
         if (slot.isCompleted()) {
            ++count;
         }
      }

      return count;
   }

   public int getTotalBaseRyo() {
      int total = 0;

      for(BingoSlot slot : this.slots) {
         total += slot.getRyoReward();
      }

      return total;
   }

   public int getJackpotRyo() {
      return this.getTotalBaseRyo() * 2;
   }

   public NBTTagCompound toNBT() {
      NBTTagCompound tag = new NBTTagCompound();
      tag.setLong("generatedTime", this.generatedTime);
      tag.setString("tierName", this.tierName);
      tag.setInteger("tierOrdinal", this.tierOrdinal);
      NBTTagList slotList = new NBTTagList();

      for(BingoSlot slot : this.slots) {
         slotList.appendTag(slot.toNBT());
      }

      tag.setTag("slots", slotList);
      return tag;
   }

   public static BingoBoard fromNBT(NBTTagCompound tag) {
      long genTime = tag.getLong("generatedTime");
      String tier = tag.getString("tierName");
      int tierOrd = tag.hasKey("tierOrdinal") ? tag.getInteger("tierOrdinal") : 0;
      NBTTagList slotList = tag.getTagList("slots", 10);
      List<BingoSlot> slots = new ArrayList();

      for(int i = 0; i < slotList.tagCount(); ++i) {
         slots.add(BingoSlot.fromNBT(slotList.getCompoundTagAt(i)));
      }

      return new BingoBoard(slots, genTime, tier, tierOrd);
   }

   public static class BingoSlot {
      private final String targetId;
      private final int spawnX;
      private final int spawnZ;
      private final int ryoReward;
      private boolean completed;
      private boolean active;

      public BingoSlot(String targetId, int spawnX, int spawnZ, int ryoReward) {
         this.targetId = targetId;
         this.spawnX = spawnX;
         this.spawnZ = spawnZ;
         this.ryoReward = ryoReward;
         this.completed = false;
         this.active = false;
      }

      public String getTargetId() {
         return this.targetId;
      }

      public int getSpawnX() {
         return this.spawnX;
      }

      public int getSpawnZ() {
         return this.spawnZ;
      }

      public int getRyoReward() {
         return this.ryoReward;
      }

      public boolean isCompleted() {
         return this.completed;
      }

      public void setCompleted(boolean completed) {
         this.completed = completed;
      }

      public boolean isActive() {
         return this.active;
      }

      public void setActive(boolean active) {
         this.active = active;
      }

      public NBTTagCompound toNBT() {
         NBTTagCompound tag = new NBTTagCompound();
         tag.setString("targetId", this.targetId);
         tag.setInteger("spawnX", this.spawnX);
         tag.setInteger("spawnZ", this.spawnZ);
         tag.setInteger("ryoReward", this.ryoReward);
         tag.setBoolean("completed", this.completed);
         tag.setBoolean("active", this.active);
         return tag;
      }

      public static BingoSlot fromNBT(NBTTagCompound tag) {
         BingoSlot slot = new BingoSlot(tag.getString("targetId"), tag.getInteger("spawnX"), tag.getInteger("spawnZ"), tag.getInteger("ryoReward"));
         slot.completed = tag.getBoolean("completed");
         slot.active = tag.getBoolean("active");
         return slot;
      }
   }
}
