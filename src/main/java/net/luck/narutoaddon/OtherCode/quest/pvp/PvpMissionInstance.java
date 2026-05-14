
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import javax.annotation.Nullable;
import java.util.*;

public class PvpMissionInstance {
   private final String instanceId;
   private final String offerId;
   private final String templateId;
   private final String name;
   private final PvpObjective objective;
   private final QuestDefinition.QuestRank rank;
   private final int ninjaXpReward;
   private final int pvpXpReward;
   private final int ryoReward;
   @Nullable
   private final String commandReward;
   @Nullable
   private final String operationOrderId;
   private final Set<UUID> uniqueVictimsKilled;
   private final Map<UUID, Long> victimCooldowns;
   private final Set<String> villagesKilledFrom;
   private int natureReleaseKills;
   private int survivalTicksRemaining;
   @Nullable
   private UUID mutualTarget;
   private int currentStreak;
   private final long startTime;
   private boolean completed;
   private boolean failed;

   public PvpMissionInstance(String instanceId, String offerId, String templateId, String name, PvpObjective objective, QuestDefinition.QuestRank rank, int ninjaXpReward, int pvpXpReward, int ryoReward, @Nullable String commandReward, @Nullable String operationOrderId) {
      this.instanceId = instanceId;
      this.offerId = offerId;
      this.templateId = templateId;
      this.name = name;
      this.objective = objective;
      this.rank = rank;
      this.ninjaXpReward = ninjaXpReward;
      this.pvpXpReward = pvpXpReward;
      this.ryoReward = ryoReward;
      this.commandReward = commandReward;
      this.operationOrderId = operationOrderId;
      this.uniqueVictimsKilled = new HashSet();
      this.victimCooldowns = new HashMap();
      this.villagesKilledFrom = new HashSet();
      this.natureReleaseKills = 0;
      this.survivalTicksRemaining = objective.getTimeLimit();
      this.mutualTarget = objective.getTargetPlayerId();
      this.currentStreak = 0;
      this.startTime = System.currentTimeMillis();
      this.completed = false;
      this.failed = false;
   }

   private PvpMissionInstance(String instanceId, String offerId, String templateId, String name, PvpObjective objective, QuestDefinition.QuestRank rank, int ninjaXpReward, int pvpXpReward, int ryoReward, @Nullable String commandReward, @Nullable String operationOrderId, Set<UUID> uniqueVictimsKilled, Map<UUID, Long> victimCooldowns, Set<String> villagesKilledFrom, int natureReleaseKills, int survivalTicksRemaining, @Nullable UUID mutualTarget, int currentStreak, long startTime, boolean completed, boolean failed) {
      this.instanceId = instanceId;
      this.offerId = offerId;
      this.templateId = templateId;
      this.name = name;
      this.objective = objective;
      this.rank = rank;
      this.ninjaXpReward = ninjaXpReward;
      this.pvpXpReward = pvpXpReward;
      this.ryoReward = ryoReward;
      this.commandReward = commandReward;
      this.operationOrderId = operationOrderId;
      this.uniqueVictimsKilled = uniqueVictimsKilled;
      this.victimCooldowns = victimCooldowns;
      this.villagesKilledFrom = villagesKilledFrom;
      this.natureReleaseKills = natureReleaseKills;
      this.survivalTicksRemaining = survivalTicksRemaining;
      this.mutualTarget = mutualTarget;
      this.currentStreak = currentStreak;
      this.startTime = startTime;
      this.completed = completed;
      this.failed = failed;
   }

   public static PvpMissionInstance fromOffer(PvpMissionOffer offer) {
      String instanceId = "pvp_" + System.currentTimeMillis() + "_" + (int)(Math.random() * (double)10000.0F);
      return new PvpMissionInstance(instanceId, offer.getOfferId(), offer.getTemplateId(), offer.getName(), offer.getObjective(), offer.getRank(), offer.getNinjaXpReward(), offer.getPvpXpReward(), offer.getRyoReward(), offer.getCommandReward(), offer.getOperationOrderId());
   }

   public String getInstanceId() {
      return this.instanceId;
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

   public PvpObjective getObjective() {
      return this.objective;
   }

   public QuestDefinition.QuestRank getRank() {
      return this.rank;
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

   @Nullable
   public String getCommandReward() {
      return this.commandReward;
   }

   @Nullable
   public String getOperationOrderId() {
      return this.operationOrderId;
   }

   public Set<UUID> getUniqueVictimsKilled() {
      return this.uniqueVictimsKilled;
   }

   public Map<UUID, Long> getVictimCooldowns() {
      return this.victimCooldowns;
   }

   public Set<String> getVillagesKilledFrom() {
      return this.villagesKilledFrom;
   }

   public int getNatureReleaseKills() {
      return this.natureReleaseKills;
   }

   public int getSurvivalTicksRemaining() {
      return this.survivalTicksRemaining;
   }

   @Nullable
   public UUID getMutualTarget() {
      return this.mutualTarget;
   }

   public int getCurrentStreak() {
      return this.currentStreak;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public boolean isCompleted() {
      return this.completed;
   }

   public boolean isFailed() {
      return this.failed;
   }

   @Nullable
   public String onKill(UUID victimId, @Nullable String victimVillage, boolean usedNatureRelease, boolean usedDojutsu, boolean victimIsHigherLevel, boolean victimHasDojutsu, int natureType) {
      if (!this.completed && !this.failed) {
         long now = System.currentTimeMillis();
         if (this.victimCooldowns.containsKey(victimId) && now < (Long)this.victimCooldowns.get(victimId)) {
            long remainMs = (Long)this.victimCooldowns.get(victimId) - now;
            int remainMin = (int)(remainMs / 60000L) + 1;
            return "already killed this player recently (" + remainMin + "m cooldown)";
         } else {
            this.victimCooldowns.put(victimId, now + 300000L);
            if (victimVillage != null && !victimVillage.isEmpty()) {
               this.villagesKilledFrom.add(victimVillage);
            }

            PvpObjective.ObjectiveType type = this.objective.getType();
            boolean countsForObjective = false;
            String rejectionReason = null;
            switch (type) {
               case KILL_ANY_ENEMY:
               case KILL_MULTI_VILLAGE:
               case KILL_STREAK:
                  countsForObjective = true;
                  break;
               case KILL_VILLAGE:
                  countsForObjective = victimVillage != null && victimVillage.equals(this.objective.getTargetVillage());
                  if (!countsForObjective) {
                     rejectionReason = "need kills on " + this.objective.getTargetVillage() + " shinobi";
                  }
                  break;
               case KILL_IN_REGION:
                  countsForObjective = victimVillage != null && victimVillage.equals(this.objective.getTargetVillage());
                  if (!countsForObjective) {
                     rejectionReason = "need kills inside your village territory";
                  }
                  break;
               case KILL_HIGHER_LEVEL:
                  countsForObjective = victimIsHigherLevel;
                  if (!victimIsHigherLevel) {
                     rejectionReason = "victim needs MORE Ninja XP than you";
                  }
                  break;
               case KILL_DOJUTSU_USER:
                  countsForObjective = victimHasDojutsu;
                  if (!victimHasDojutsu) {
                     rejectionReason = "victim must be wearing a dojutsu helmet";
                  }
                  break;
               case KILL_WITH_NATURE:
                  if (!usedNatureRelease) {
                     rejectionReason = "must kill with a ninjutsu (nature release jutsu)";
                  } else {
                     int requiredNature = this.objective.getNatureType();
                     if (requiredNature == 0) {
                        countsForObjective = true;
                     } else if (natureType == requiredNature) {
                        countsForObjective = true;
                     } else {
                        String[] natureNames = new String[]{"None", "Fire", "Wind", "Water", "Lightning", "Earth"};
                        String needed = requiredNature > 0 && requiredNature < natureNames.length ? natureNames[requiredNature] : "correct";
                        rejectionReason = "must kill with " + needed + " Release jutsu";
                     }
                  }
                  break;
               case KILL_WITH_DOJUTSU:
                  countsForObjective = usedDojutsu;
                  if (!usedDojutsu) {
                     rejectionReason = "must kill using a dojutsu ability";
                  }
                  break;
               case KILL_SPECIFIC_PLAYER:
               case MUTUAL_HUNT:
                  countsForObjective = true;
               case SURVIVE_DURATION:
            }

            if (countsForObjective) {
               this.uniqueVictimsKilled.add(victimId);
               if (usedNatureRelease && type == PvpObjective.ObjectiveType.KILL_WITH_NATURE) {
                  int requiredNature = this.objective.getNatureType();
                  if (requiredNature == 0 || natureType == requiredNature) {
                     ++this.natureReleaseKills;
                  }
               }

               ++this.currentStreak;
            }

            if (this.isComplete()) {
               this.completed = true;
            }

            return rejectionReason;
         }
      } else {
         return null;
      }
   }

   public void onDeath() {
      this.currentStreak = 0;
      if (this.objective.getType() == PvpObjective.ObjectiveType.SURVIVE_DURATION) {
         this.failed = true;
      }

   }

   public void tickSurvival() {
      if (!this.completed && !this.failed) {
         if (this.objective.getType() == PvpObjective.ObjectiveType.SURVIVE_DURATION && this.survivalTicksRemaining > 0) {
            --this.survivalTicksRemaining;
            if (this.survivalTicksRemaining <= 0) {
               this.completed = true;
            }
         }

      }
   }

   public boolean isComplete() {
      PvpObjective.ObjectiveType type = this.objective.getType();
      int required = this.objective.getKillsRequired();
      int kills = this.uniqueVictimsKilled.size();
      switch (type) {
         case KILL_ANY_ENEMY:
         case KILL_VILLAGE:
         case KILL_IN_REGION:
         case KILL_HIGHER_LEVEL:
         case KILL_DOJUTSU_USER:
            return kills >= required;
         case KILL_MULTI_VILLAGE:
            return kills >= required && this.villagesKilledFrom.size() >= 2;
         case KILL_STREAK:
            return this.currentStreak >= required;
         case KILL_WITH_NATURE:
            return this.natureReleaseKills >= required;
         case KILL_WITH_DOJUTSU:
            return kills >= required;
         case KILL_SPECIFIC_PLAYER:
         case MUTUAL_HUNT:
            return this.uniqueVictimsKilled.contains(this.objective.getTargetPlayerId());
         case SURVIVE_DURATION:
            return this.survivalTicksRemaining <= 0;
         default:
            return false;
      }
   }

   public float getProgress() {
      if (this.completed) {
         return 1.0F;
      } else {
         PvpObjective.ObjectiveType type = this.objective.getType();
         int required = this.objective.getKillsRequired();
         switch (type) {
            case KILL_ANY_ENEMY:
            case KILL_MULTI_VILLAGE:
            case KILL_VILLAGE:
            case KILL_IN_REGION:
            case KILL_HIGHER_LEVEL:
            case KILL_DOJUTSU_USER:
            case KILL_WITH_DOJUTSU:
               return required > 0 ? Math.min(1.0F, (float)this.uniqueVictimsKilled.size() / (float)required) : 0.0F;
            case KILL_STREAK:
               return required > 0 ? Math.min(1.0F, (float)this.currentStreak / (float)required) : 0.0F;
            case KILL_WITH_NATURE:
               return required > 0 ? Math.min(1.0F, (float)this.natureReleaseKills / (float)required) : 0.0F;
            case KILL_SPECIFIC_PLAYER:
            case MUTUAL_HUNT:
               return this.uniqueVictimsKilled.contains(this.objective.getTargetPlayerId()) ? 1.0F : 0.0F;
            case SURVIVE_DURATION:
               int total = this.objective.getTimeLimit();
               return total > 0 ? Math.min(1.0F, 1.0F - (float)this.survivalTicksRemaining / (float)total) : 0.0F;
            default:
               return 0.0F;
         }
      }
   }

   public String getProgressText() {
      PvpObjective.ObjectiveType type = this.objective.getType();
      int required = this.objective.getKillsRequired();
      int kills = this.uniqueVictimsKilled.size();
      switch (type) {
         case KILL_ANY_ENEMY:
            return "Enemy Kills: " + kills + "/" + required;
         case KILL_MULTI_VILLAGE:
            return "Kills: " + kills + "/" + required + " (Villages: " + this.villagesKilledFrom.size() + "/2)";
         case KILL_STREAK:
            return "Streak: " + this.currentStreak + "/" + required;
         case KILL_VILLAGE:
            return "Kills (" + this.objective.getTargetVillage() + "): " + kills + "/" + required;
         case KILL_IN_REGION:
            return "Region Kills (" + this.objective.getTargetVillage() + "): " + kills + "/" + required;
         case KILL_HIGHER_LEVEL:
            return "Elite Kills (Higher Ninja XP): " + kills + "/" + required;
         case KILL_DOJUTSU_USER:
            return "Dojutsu User Kills: " + kills + "/" + required;
         case KILL_WITH_NATURE:
            return "Nature Kills: " + this.natureReleaseKills + "/" + required;
         case KILL_WITH_DOJUTSU:
            return "Dojutsu Kills: " + kills + "/" + required;
         case KILL_SPECIFIC_PLAYER:
            return "Target: " + (kills >= 1 ? "Eliminated" : "Alive");
         case MUTUAL_HUNT:
            return "Mutual Hunt: " + (kills >= 1 ? "Complete" : "In Progress");
         case SURVIVE_DURATION:
            int secs = this.survivalTicksRemaining / 20;
            return "Survive: " + secs + "s remaining";
         default:
            return "???";
      }
   }

   public void markCompleted() {
      this.completed = true;
   }

   public void markFailed() {
      this.failed = true;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("instanceId", this.instanceId);
      nbt.setString("offerId", this.offerId);
      nbt.setString("templateId", this.templateId);
      nbt.setString("name", this.name);
      nbt.setTag("objective", this.objective.writeToNBT());
      nbt.setByte("rank", (byte)this.rank.ordinal());
      nbt.setInteger("ninjaXpReward", this.ninjaXpReward);
      nbt.setInteger("pvpXpReward", this.pvpXpReward);
      nbt.setInteger("ryoReward", this.ryoReward);
      if (this.commandReward != null) {
         nbt.setString("commandReward", this.commandReward);
      }

      if (this.operationOrderId != null) {
         nbt.setString("operationOrderId", this.operationOrderId);
      }

      NBTTagList victimList = new NBTTagList();

      for(UUID uuid : this.uniqueVictimsKilled) {
         victimList.appendTag(new NBTTagString(uuid.toString()));
      }

      nbt.setTag("uniqueVictims", victimList);
      NBTTagList cooldownList = new NBTTagList();

      for(Map.Entry<UUID, Long> entry : this.victimCooldowns.entrySet()) {
         NBTTagCompound cdTag = new NBTTagCompound();
         cdTag.setString("uuid", ((UUID)entry.getKey()).toString());
         cdTag.setLong("expires", (Long)entry.getValue());
         cooldownList.appendTag(cdTag);
      }

      nbt.setTag("victimCooldowns", cooldownList);
      NBTTagList villageList = new NBTTagList();

      for(String village : this.villagesKilledFrom) {
         villageList.appendTag(new NBTTagString(village));
      }

      nbt.setTag("villagesKilledFrom", villageList);
      nbt.setInteger("natureReleaseKills", this.natureReleaseKills);
      nbt.setInteger("survivalTicksRemaining", this.survivalTicksRemaining);
      if (this.mutualTarget != null) {
         nbt.setString("mutualTarget", this.mutualTarget.toString());
      }

      nbt.setInteger("currentStreak", this.currentStreak);
      nbt.setLong("startTime", this.startTime);
      nbt.setBoolean("completed", this.completed);
      nbt.setBoolean("failed", this.failed);
      return nbt;
   }

   public static PvpMissionInstance readFromNBT(NBTTagCompound nbt) {
      String instanceId = nbt.getString("instanceId");
      String offerId = nbt.getString("offerId");
      String templateId = nbt.getString("templateId");
      String name = nbt.getString("name");
      PvpObjective objective = PvpObjective.readFromNBT(nbt.getCompoundTag("objective"));
      byte rankOrd = nbt.getByte("rank");
      QuestDefinition.QuestRank rank = rankOrd < QuestDefinition.QuestRank.values().length ? QuestDefinition.QuestRank.values()[rankOrd] : QuestDefinition.QuestRank.D;
      int ninjaXpReward = nbt.getInteger("ninjaXpReward");
      int pvpXpReward = nbt.getInteger("pvpXpReward");
      int ryoReward = nbt.getInteger("ryoReward");
      String commandReward = nbt.hasKey("commandReward") ? nbt.getString("commandReward") : null;
      String operationOrderId = nbt.hasKey("operationOrderId") ? nbt.getString("operationOrderId") : null;
      Set<UUID> uniqueVictims = new HashSet();
      NBTTagList victimList = nbt.getTagList("uniqueVictims", 8);

      for(int i = 0; i < victimList.tagCount(); ++i) {
         try {
            uniqueVictims.add(UUID.fromString(victimList.getStringTagAt(i)));
         } catch (IllegalArgumentException var29) {
         }
      }

      Map<UUID, Long> victimCooldowns = new HashMap();
      NBTTagList cooldownList = nbt.getTagList("victimCooldowns", 10);

      for(int i = 0; i < cooldownList.tagCount(); ++i) {
         NBTTagCompound cdTag = cooldownList.getCompoundTagAt(i);

         try {
            UUID uuid = UUID.fromString(cdTag.getString("uuid"));
            long expires = cdTag.getLong("expires");
            victimCooldowns.put(uuid, expires);
         } catch (IllegalArgumentException var28) {
         }
      }

      Set<String> villagesKilledFrom = new HashSet();
      NBTTagList villageList = nbt.getTagList("villagesKilledFrom", 8);

      for(int i = 0; i < villageList.tagCount(); ++i) {
         villagesKilledFrom.add(villageList.getStringTagAt(i));
      }

      int natureReleaseKills = nbt.getInteger("natureReleaseKills");
      int survivalTicksRemaining = nbt.getInteger("survivalTicksRemaining");
      UUID mutualTarget = null;
      if (nbt.hasKey("mutualTarget")) {
         try {
            mutualTarget = UUID.fromString(nbt.getString("mutualTarget"));
         } catch (IllegalArgumentException var27) {
         }
      }

      int currentStreak = nbt.getInteger("currentStreak");
      long startTime = nbt.getLong("startTime");
      boolean completed = nbt.getBoolean("completed");
      boolean failed = nbt.getBoolean("failed");
      return new PvpMissionInstance(instanceId, offerId, templateId, name, objective, rank, ninjaXpReward, pvpXpReward, ryoReward, commandReward, operationOrderId, uniqueVictims, victimCooldowns, villagesKilledFrom, natureReleaseKills, survivalTicksRemaining, mutualTarget, currentStreak, startTime, completed, failed);
   }
}
