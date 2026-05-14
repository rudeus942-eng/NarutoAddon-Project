package net.luck.narutoaddon.OtherCode.akatsuki.contract;

import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class Contract {
   public static final long EXPIRY_MS = 172800000L;
   private UUID contractId;
   private UUID posterId;
   private String posterName;
   private ContractType type;
   private String targetName;
   private int ryoAmount;
   private String targetZoneId = "";
   private String posterVillage = "";
   private UUID[] assignees = new UUID[2];
   private String[] assigneeNames = new String[]{"", ""};
   private ContractStatus status;
   private long createdTimestamp;
   private long acceptedTimestamp;

   public Contract(UUID contractId, UUID posterId, String posterName, ContractType type, String targetName, int ryoAmount) {
      this.contractId = contractId;
      this.posterId = posterId;
      this.posterName = posterName;
      this.type = type;
      this.targetName = targetName;
      this.ryoAmount = ryoAmount;
      this.status = ContractStatus.OPEN;
      this.createdTimestamp = System.currentTimeMillis();
      this.acceptedTimestamp = 0L;
   }

   public Contract() {
      this.contractId = UUID.randomUUID();
      this.status = ContractStatus.OPEN;
   }

   public UUID getContractId() {
      return this.contractId;
   }

   public UUID getPosterId() {
      return this.posterId;
   }

   public String getPosterName() {
      return this.posterName;
   }

   public ContractType getType() {
      return this.type;
   }

   public String getTargetName() {
      return this.targetName;
   }

   public int getRyoAmount() {
      return this.ryoAmount;
   }

   public String getTargetZoneId() {
      return this.targetZoneId;
   }

   public void setTargetZoneId(String targetZoneId) {
      this.targetZoneId = targetZoneId != null ? targetZoneId : "";
   }

   public String getPosterVillage() {
      return this.posterVillage;
   }

   public void setPosterVillage(String village) {
      this.posterVillage = village != null ? village : "";
   }

   public UUID[] getAssignees() {
      return this.assignees;
   }

   public String[] getAssigneeNames() {
      return this.assigneeNames;
   }

   public ContractStatus getStatus() {
      return this.status;
   }

   public long getCreatedTimestamp() {
      return this.createdTimestamp;
   }

   public long getAcceptedTimestamp() {
      return this.acceptedTimestamp;
   }

   public void setStatus(ContractStatus status) {
      this.status = status;
   }

   public void setAcceptedTimestamp(long ts) {
      this.acceptedTimestamp = ts;
   }

   public boolean isExpired() {
      return this.status == ContractStatus.OPEN && System.currentTimeMillis() - this.createdTimestamp > 172800000L;
   }

   public int getAssigneeCount() {
      int count = 0;
      if (this.assignees[0] != null) {
         ++count;
      }

      if (this.assignees[1] != null) {
         ++count;
      }

      return count;
   }

   public boolean addAssignee(UUID playerId, String playerName) {
      for(int i = 0; i < 2; ++i) {
         if (this.assignees[i] == null) {
            this.assignees[i] = playerId;
            this.assigneeNames[i] = playerName;
            return true;
         }
      }

      return false;
   }

   public boolean isAssignee(UUID playerId) {
      return playerId.equals(this.assignees[0]) || playerId.equals(this.assignees[1]);
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("contractId", this.contractId.toString());
      nbt.setString("posterId", this.posterId.toString());
      nbt.setString("posterName", this.posterName != null ? this.posterName : "");
      nbt.setInteger("type", this.type.ordinal());
      nbt.setString("targetName", this.targetName != null ? this.targetName : "");
      nbt.setString("targetZoneId", this.targetZoneId != null ? this.targetZoneId : "");
      nbt.setString("posterVillage", this.posterVillage != null ? this.posterVillage : "");
      nbt.setInteger("ryoAmount", this.ryoAmount);
      nbt.setInteger("status", this.status.ordinal());
      nbt.setLong("createdTimestamp", this.createdTimestamp);
      nbt.setLong("acceptedTimestamp", this.acceptedTimestamp);

      for(int i = 0; i < 2; ++i) {
         nbt.setString("assignee" + i, this.assignees[i] != null ? this.assignees[i].toString() : "");
         nbt.setString("assigneeName" + i, this.assigneeNames[i] != null ? this.assigneeNames[i] : "");
      }

      return nbt;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      try {
         this.contractId = UUID.fromString(nbt.getString("contractId"));
      } catch (IllegalArgumentException var9) {
         this.contractId = UUID.randomUUID();
      }

      try {
         this.posterId = UUID.fromString(nbt.getString("posterId"));
      } catch (IllegalArgumentException var8) {
         this.posterId = UUID.randomUUID();
      }

      this.posterName = nbt.getString("posterName");
      int typeOrd = nbt.getInteger("type");
      this.type = typeOrd >= 0 && typeOrd < ContractType.values().length ? ContractType.values()[typeOrd] : ContractType.ASSASSINATION;
      this.targetName = nbt.getString("targetName");
      this.targetZoneId = nbt.getString("targetZoneId");
      this.posterVillage = nbt.getString("posterVillage");
      this.ryoAmount = nbt.getInteger("ryoAmount");
      int statusOrd = nbt.getInteger("status");
      this.status = statusOrd >= 0 && statusOrd < ContractStatus.values().length ? ContractStatus.values()[statusOrd] : ContractStatus.OPEN;
      this.createdTimestamp = nbt.getLong("createdTimestamp");
      this.acceptedTimestamp = nbt.getLong("acceptedTimestamp");

      for(int i = 0; i < 2; ++i) {
         String aStr = nbt.getString("assignee" + i);
         if (!aStr.isEmpty()) {
            try {
               this.assignees[i] = UUID.fromString(aStr);
            } catch (IllegalArgumentException var7) {
               this.assignees[i] = null;
            }
         }

         this.assigneeNames[i] = nbt.getString("assigneeName" + i);
      }

   }

   public static enum ContractType {
      ASSASSINATION,
      TERRITORY;

      public String displayName() {
         switch (this) {
            case ASSASSINATION:
               return "Assassination";
            case TERRITORY:
               return "Territory Hire";
            default:
               return this.name();
         }
      }
   }

   public static enum ContractStatus {
      OPEN,
      ACCEPTED,
      COMPLETED,
      FAILED,
      EXPIRED,
      CANCELLED;
   }
}
