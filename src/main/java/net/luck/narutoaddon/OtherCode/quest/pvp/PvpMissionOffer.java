
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

public class PvpMissionOffer {
   private final String offerId;
   private final String templateId;
   private final String name;
   private final String description;
   private final PvpObjective objective;
   private final int ninjaXpReward;
   private final int pvpXpReward;
   private final int ryoReward;
   private final String commandReward;
   private final QuestDefinition.QuestRank rank;
   private final long createdAt;
   @Nullable
   private final String operationOrderId;

   public PvpMissionOffer(String offerId, String templateId, String name, String description, PvpObjective objective, int ninjaXpReward, int pvpXpReward, String commandReward, QuestDefinition.QuestRank rank, long createdAt) {
      this(offerId, templateId, name, description, objective, ninjaXpReward, pvpXpReward, 0, commandReward, rank, createdAt, (String)null);
   }

   public PvpMissionOffer(String offerId, String templateId, String name, String description, PvpObjective objective, int ninjaXpReward, int pvpXpReward, String commandReward, QuestDefinition.QuestRank rank, long createdAt, @Nullable String operationOrderId) {
      this(offerId, templateId, name, description, objective, ninjaXpReward, pvpXpReward, 0, commandReward, rank, createdAt, operationOrderId);
   }

   public PvpMissionOffer(String offerId, String templateId, String name, String description, PvpObjective objective, int ninjaXpReward, int pvpXpReward, int ryoReward, String commandReward, QuestDefinition.QuestRank rank, long createdAt, @Nullable String operationOrderId) {
      this.offerId = offerId;
      this.templateId = templateId;
      this.name = name;
      this.description = description;
      this.objective = objective;
      this.ninjaXpReward = ninjaXpReward;
      this.pvpXpReward = pvpXpReward;
      this.ryoReward = ryoReward;
      this.commandReward = commandReward;
      this.rank = rank;
      this.createdAt = createdAt;
      this.operationOrderId = operationOrderId;
   }

   public String getOfferId() {
      return this.offerId;
   }

   public String getTemplateId() {
      return this.templateId;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public PvpObjective getObjective() {
      return this.objective;
   }

   public int getNinjaXpReward() {
      return this.ninjaXpReward;
   }

   public int getPvpXpReward() {
      return this.pvpXpReward;
   }

   public int getRyoReward() {
      return this.ryoReward;
   }

   public String getCommandReward() {
      return this.commandReward;
   }

   public QuestDefinition.QuestRank getRank() {
      return this.rank;
   }

   public long getCreatedAt() {
      return this.createdAt;
   }

   @Nullable
   public String getOperationOrderId() {
      return this.operationOrderId;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("offerId", this.offerId);
      nbt.setString("templateId", this.templateId);
      nbt.setString("name", this.name);
      nbt.setString("description", this.description);
      nbt.setTag("objective", this.objective.writeToNBT());
      nbt.setInteger("ninjaXpReward", this.ninjaXpReward);
      nbt.setInteger("pvpXpReward", this.pvpXpReward);
      nbt.setInteger("ryoReward", this.ryoReward);
      if (this.commandReward != null) {
         nbt.setString("commandReward", this.commandReward);
      }

      nbt.setByte("rank", (byte)this.rank.ordinal());
      nbt.setLong("createdAt", this.createdAt);
      if (this.operationOrderId != null) {
         nbt.setString("operationOrderId", this.operationOrderId);
      }

      return nbt;
   }

   public static PvpMissionOffer readFromNBT(NBTTagCompound nbt) {
      String offerId = nbt.getString("offerId");
      String templateId = nbt.getString("templateId");
      String name = nbt.getString("name");
      String description = nbt.getString("description");
      PvpObjective objective = PvpObjective.readFromNBT(nbt.getCompoundTag("objective"));
      int ninjaXpReward = nbt.getInteger("ninjaXpReward");
      int pvpXpReward = nbt.getInteger("pvpXpReward");
      int ryoReward = nbt.getInteger("ryoReward");
      String commandReward = nbt.hasKey("commandReward") ? nbt.getString("commandReward") : null;
      byte rankOrd = nbt.getByte("rank");
      QuestDefinition.QuestRank rank = rankOrd < QuestDefinition.QuestRank.values().length ? QuestDefinition.QuestRank.values()[rankOrd] : QuestDefinition.QuestRank.D;
      long createdAt = nbt.getLong("createdAt");
      String operationOrderId = nbt.hasKey("operationOrderId") ? nbt.getString("operationOrderId") : null;
      return new PvpMissionOffer(offerId, templateId, name, description, objective, ninjaXpReward, pvpXpReward, ryoReward, commandReward, rank, createdAt, operationOrderId);
   }
}
