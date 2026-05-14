
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.UUID;

public class VillageOrder {
   private final String orderId;
   private final String issuerVillage;
   private final UUID issuerId;
   private final String issuerName;
   private final OrderType orderType;
   @Nullable
   private final String targetVillage;
   @Nullable
   private final UUID targetPlayerId;
   @Nullable
   private final String targetPlayerName;
   private final long issuedAt;
   private final long expiresAt;
   private final int bonusPvpXp;
   private final int bonusNinjaXp;
   @Nullable
   private final String assignedTemplateId;
   @Nullable
   private final String assignedMissionName;

   public VillageOrder(String orderId, String issuerVillage, UUID issuerId, String issuerName, OrderType orderType, @Nullable String targetVillage, @Nullable UUID targetPlayerId, @Nullable String targetPlayerName, long issuedAt, long expiresAt, int bonusPvpXp, int bonusNinjaXp) {
      this(orderId, issuerVillage, issuerId, issuerName, orderType, targetVillage, targetPlayerId, targetPlayerName, issuedAt, expiresAt, bonusPvpXp, bonusNinjaXp, (String)null, (String)null);
   }

   public VillageOrder(String orderId, String issuerVillage, UUID issuerId, String issuerName, OrderType orderType, @Nullable String targetVillage, @Nullable UUID targetPlayerId, @Nullable String targetPlayerName, long issuedAt, long expiresAt, int bonusPvpXp, int bonusNinjaXp, @Nullable String assignedTemplateId, @Nullable String assignedMissionName) {
      this.orderId = orderId;
      this.issuerVillage = issuerVillage;
      this.issuerId = issuerId;
      this.issuerName = issuerName;
      this.orderType = orderType;
      this.targetVillage = targetVillage;
      this.targetPlayerId = targetPlayerId;
      this.targetPlayerName = targetPlayerName;
      this.issuedAt = issuedAt;
      this.expiresAt = expiresAt;
      this.bonusPvpXp = bonusPvpXp;
      this.bonusNinjaXp = bonusNinjaXp;
      this.assignedTemplateId = assignedTemplateId;
      this.assignedMissionName = assignedMissionName;
   }

   public boolean isExpired() {
      return System.currentTimeMillis() >= this.expiresAt;
   }

   public boolean matchesKill(UUID victimId, String victimVillage) {
      switch (this.orderType) {
         case TARGET_PLAYER:
            return this.targetPlayerId != null && this.targetPlayerId.equals(victimId);
         case TARGET_VILLAGE:
            return this.targetVillage != null && this.targetVillage.equals(victimVillage);
         default:
            return false;
      }
   }

   public String getOrderId() {
      return this.orderId;
   }

   public String getIssuerVillage() {
      return this.issuerVillage;
   }

   public UUID getIssuerId() {
      return this.issuerId;
   }

   public String getIssuerName() {
      return this.issuerName;
   }

   public OrderType getOrderType() {
      return this.orderType;
   }

   @Nullable
   public String getTargetVillage() {
      return this.targetVillage;
   }

   @Nullable
   public UUID getTargetPlayerId() {
      return this.targetPlayerId;
   }

   @Nullable
   public String getTargetPlayerName() {
      return this.targetPlayerName;
   }

   public long getIssuedAt() {
      return this.issuedAt;
   }

   public long getExpiresAt() {
      return this.expiresAt;
   }

   public int getBonusPvpXp() {
      return this.bonusPvpXp;
   }

   public int getBonusNinjaXp() {
      return this.bonusNinjaXp;
   }

   @Nullable
   public String getAssignedTemplateId() {
      return this.assignedTemplateId;
   }

   @Nullable
   public String getAssignedMissionName() {
      return this.assignedMissionName;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("orderId", this.orderId);
      nbt.setString("issuerVillage", this.issuerVillage);
      nbt.setLong("issuerIdMost", this.issuerId.getMostSignificantBits());
      nbt.setLong("issuerIdLeast", this.issuerId.getLeastSignificantBits());
      nbt.setString("issuerName", this.issuerName);
      nbt.setString("orderType", this.orderType.name());
      if (this.targetVillage != null) {
         nbt.setString("targetVillage", this.targetVillage);
      }

      if (this.targetPlayerId != null) {
         nbt.setLong("targetPlayerIdMost", this.targetPlayerId.getMostSignificantBits());
         nbt.setLong("targetPlayerIdLeast", this.targetPlayerId.getLeastSignificantBits());
      }

      if (this.targetPlayerName != null) {
         nbt.setString("targetPlayerName", this.targetPlayerName);
      }

      nbt.setLong("issuedAt", this.issuedAt);
      nbt.setLong("expiresAt", this.expiresAt);
      nbt.setInteger("bonusPvpXp", this.bonusPvpXp);
      nbt.setInteger("bonusNinjaXp", this.bonusNinjaXp);
      if (this.assignedTemplateId != null) {
         nbt.setString("assignedTemplateId", this.assignedTemplateId);
      }

      if (this.assignedMissionName != null) {
         nbt.setString("assignedMissionName", this.assignedMissionName);
      }

      return nbt;
   }

   public static VillageOrder readFromNBT(NBTTagCompound nbt) {
      String orderId = nbt.getString("orderId");
      String issuerVillage = nbt.getString("issuerVillage");
      UUID issuerId = new UUID(nbt.getLong("issuerIdMost"), nbt.getLong("issuerIdLeast"));
      String issuerName = nbt.getString("issuerName");
      OrderType orderType = OrderType.valueOf(nbt.getString("orderType"));
      String targetVillage = nbt.hasKey("targetVillage") ? nbt.getString("targetVillage") : null;
      UUID targetPlayerId = null;
      if (nbt.hasKey("targetPlayerIdMost")) {
         targetPlayerId = new UUID(nbt.getLong("targetPlayerIdMost"), nbt.getLong("targetPlayerIdLeast"));
      }

      String targetPlayerName = nbt.hasKey("targetPlayerName") ? nbt.getString("targetPlayerName") : null;
      long issuedAt = nbt.getLong("issuedAt");
      long expiresAt = nbt.getLong("expiresAt");
      int bonusPvpXp = nbt.getInteger("bonusPvpXp");
      int bonusNinjaXp = nbt.getInteger("bonusNinjaXp");
      String assignedTemplateId = nbt.hasKey("assignedTemplateId") ? nbt.getString("assignedTemplateId") : null;
      String assignedMissionName = nbt.hasKey("assignedMissionName") ? nbt.getString("assignedMissionName") : null;
      return new VillageOrder(orderId, issuerVillage, issuerId, issuerName, orderType, targetVillage, targetPlayerId, targetPlayerName, issuedAt, expiresAt, bonusPvpXp, bonusNinjaXp, assignedTemplateId, assignedMissionName);
   }

   public static enum OrderType {
      TARGET_PLAYER,
      TARGET_VILLAGE,
      ASSIGN_MISSION;
   }
}
