
package net.luck.narutoaddon.OtherCode.endgame.contract;

import net.minecraft.nbt.NBTTagCompound;

public class ContractDefinition {
   private final String id;
   private final Rank rank;
   private final String targetName;
   private final String targetConfigId;
   private final int hintX;
   private final int hintZ;
   private final String locationHint;
   private final long postedAt;

   private ContractDefinition(String id, Rank rank, String targetName, String targetConfigId, int hintX, int hintZ, String locationHint, long postedAt) {
      this.id = id;
      this.rank = rank;
      this.targetName = targetName;
      this.targetConfigId = targetConfigId;
      this.hintX = hintX;
      this.hintZ = hintZ;
      this.locationHint = locationHint == null ? "" : locationHint;
      this.postedAt = postedAt;
   }

   public static ContractDefinition pveMark(String id, Rank rank, String targetName, String targetConfigId, int hintX, int hintZ, String locationHint, long postedAt) {
      return new ContractDefinition(id, rank, targetName, targetConfigId, hintX, hintZ, locationHint, postedAt);
   }

   public String getId() {
      return this.id;
   }

   public Rank getRank() {
      return this.rank;
   }

   public String getTargetName() {
      return this.targetName;
   }

   public String getTargetConfigId() {
      return this.targetConfigId;
   }

   public int getHintX() {
      return this.hintX;
   }

   public int getHintZ() {
      return this.hintZ;
   }

   public String getLocationHint() {
      return this.locationHint;
   }

   public long getPostedAt() {
      return this.postedAt;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("id", this.id);
      nbt.setString("rank", this.rank.name());
      nbt.setString("targetName", this.targetName == null ? "" : this.targetName);
      nbt.setString("targetConfigId", this.targetConfigId == null ? "" : this.targetConfigId);
      nbt.setInteger("hintX", this.hintX);
      nbt.setInteger("hintZ", this.hintZ);
      nbt.setString("locationHint", this.locationHint);
      nbt.setLong("postedAt", this.postedAt);
      return nbt;
   }

   public static ContractDefinition readFromNBT(NBTTagCompound nbt) {
      if (nbt.hasKey("type")) {
         String t = nbt.getString("type");
         if (t != null && t.equalsIgnoreCase("PVP_MARK")) {
            return null;
         }
      }

      String id = nbt.getString("id");
      Rank rank = Rank.fromName(nbt.getString("rank"));
      String targetName = nbt.getString("targetName");
      String targetConfigId = nbt.getString("targetConfigId");
      int hintX = nbt.getInteger("hintX");
      int hintZ = nbt.getInteger("hintZ");
      String locationHint = nbt.getString("locationHint");
      long postedAt = nbt.getLong("postedAt");
      return new ContractDefinition(id, rank, targetName, targetConfigId.isEmpty() ? null : targetConfigId, hintX, hintZ, locationHint, postedAt);
   }

   public static enum Rank {
      D(0, 0, 0, 0.0F),
      C(0, 0, 0, 0.0F),
      B(10000, 0, 0, 0.0F),
      A(20000, 0, 0, 0.0F),
      S(40000, 0, 0, 0.0F);

      public final int ryo;
      public final int sp;
      public final int tokens;
      public final float ringFragmentChance;

      private Rank(int ryo, int sp, int tokens, float ringFragmentChance) {
         this.ryo = ryo;
         this.sp = sp;
         this.tokens = tokens;
         this.ringFragmentChance = ringFragmentChance;
      }

      public static Rank fromName(String name) {
         if (name == null) {
            return C;
         } else {
            try {
               return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException var2) {
               return C;
            }
         }
      }
   }
}
