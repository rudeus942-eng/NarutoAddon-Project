package net.luck.narutoaddon.OtherCode.akatsuki.bounty;

import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class BountyEntry {
   private static final long EXPIRY_MS = 604800000L;
   private UUID targetId;
   private String targetName;
   private int bountyAmount;
   private long placedTimestamp;
   private UUID placedBy;

   public BountyEntry(UUID targetId, String targetName, int bountyAmount, UUID placedBy) {
      this.targetId = targetId;
      this.targetName = targetName;
      this.bountyAmount = bountyAmount;
      this.placedTimestamp = System.currentTimeMillis();
      this.placedBy = placedBy;
   }

   public BountyEntry() {
   }

   public UUID getTargetId() {
      return this.targetId;
   }

   public String getTargetName() {
      return this.targetName;
   }

   public int getBountyAmount() {
      return this.bountyAmount;
   }

   public long getPlacedTimestamp() {
      return this.placedTimestamp;
   }

   public UUID getPlacedBy() {
      return this.placedBy;
   }

   public void addAmount(int amount) {
      this.bountyAmount += amount;
   }

   public boolean isExpired() {
      return System.currentTimeMillis() - this.placedTimestamp > 604800000L;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("targetId", this.targetId.toString());
      nbt.setString("targetName", this.targetName);
      nbt.setInteger("bountyAmount", this.bountyAmount);
      nbt.setLong("placedTimestamp", this.placedTimestamp);
      if (this.placedBy != null) {
         nbt.setString("placedBy", this.placedBy.toString());
      }

      return nbt;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      try {
         this.targetId = UUID.fromString(nbt.getString("targetId"));
      } catch (IllegalArgumentException var4) {
         this.targetId = UUID.randomUUID();
      }

      this.targetName = nbt.getString("targetName");
      this.bountyAmount = nbt.getInteger("bountyAmount");
      this.placedTimestamp = nbt.getLong("placedTimestamp");
      if (nbt.hasKey("placedBy")) {
         try {
            this.placedBy = UUID.fromString(nbt.getString("placedBy"));
         } catch (IllegalArgumentException var3) {
            this.placedBy = null;
         }
      } else {
         this.placedBy = null;
      }

   }
}
