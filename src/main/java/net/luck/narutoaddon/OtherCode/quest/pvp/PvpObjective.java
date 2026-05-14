
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.UUID;

public class PvpObjective {
   private final ObjectiveType type;
   @Nullable
   private final String targetVillage;
   @Nullable
   private final UUID targetPlayerId;
   private final int killsRequired;
   private final int natureType;
   private final int timeLimit;

   private PvpObjective(ObjectiveType type, @Nullable String targetVillage, @Nullable UUID targetPlayerId, int killsRequired, int natureType, int timeLimit) {
      this.type = type;
      this.targetVillage = targetVillage;
      this.targetPlayerId = targetPlayerId;
      this.killsRequired = killsRequired;
      this.natureType = natureType;
      this.timeLimit = timeLimit;
   }

   public static PvpObjective killVillage(String village, int kills) {
      return new PvpObjective(ObjectiveType.KILL_VILLAGE, village, (UUID)null, kills, 0, 0);
   }

   public static PvpObjective killAnyEnemy(int kills) {
      return new PvpObjective(ObjectiveType.KILL_ANY_ENEMY, (String)null, (UUID)null, kills, 0, 0);
   }

   public static PvpObjective killMultiVillage(int kills) {
      return new PvpObjective(ObjectiveType.KILL_MULTI_VILLAGE, (String)null, (UUID)null, kills, 0, 0);
   }

   public static PvpObjective killWithNature(int kills, int natureType) {
      return new PvpObjective(ObjectiveType.KILL_WITH_NATURE, (String)null, (UUID)null, kills, natureType, 0);
   }

   public static PvpObjective killWithDojutsu(int kills) {
      return new PvpObjective(ObjectiveType.KILL_WITH_DOJUTSU, (String)null, (UUID)null, kills, 0, 0);
   }

   public static PvpObjective killHigherLevel(int kills) {
      return new PvpObjective(ObjectiveType.KILL_HIGHER_LEVEL, (String)null, (UUID)null, kills, 0, 0);
   }

   public static PvpObjective killInRegion(String region, int kills) {
      return new PvpObjective(ObjectiveType.KILL_IN_REGION, region, (UUID)null, kills, 0, 0);
   }

   public static PvpObjective killSpecificPlayer(UUID targetId) {
      return new PvpObjective(ObjectiveType.KILL_SPECIFIC_PLAYER, (String)null, targetId, 1, 0, 0);
   }

   public static PvpObjective mutualHunt(UUID targetId) {
      return new PvpObjective(ObjectiveType.MUTUAL_HUNT, (String)null, targetId, 1, 0, 0);
   }

   public static PvpObjective surviveDuration(int ticks) {
      return new PvpObjective(ObjectiveType.SURVIVE_DURATION, (String)null, (UUID)null, 0, 0, ticks);
   }

   public static PvpObjective killStreak(int kills) {
      return new PvpObjective(ObjectiveType.KILL_STREAK, (String)null, (UUID)null, kills, 0, 0);
   }

   public static PvpObjective killDojutsuUser(int kills) {
      return new PvpObjective(ObjectiveType.KILL_DOJUTSU_USER, (String)null, (UUID)null, kills, 0, 0);
   }

   public ObjectiveType getType() {
      return this.type;
   }

   @Nullable
   public String getTargetVillage() {
      return this.targetVillage;
   }

   @Nullable
   public UUID getTargetPlayerId() {
      return this.targetPlayerId;
   }

   public int getKillsRequired() {
      return this.killsRequired;
   }

   public int getNatureType() {
      return this.natureType;
   }

   public int getTimeLimit() {
      return this.timeLimit;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setInteger("type", this.type.ordinal());
      if (this.targetVillage != null) {
         nbt.setString("targetVillage", this.targetVillage);
      }

      if (this.targetPlayerId != null) {
         nbt.setString("targetPlayerId", this.targetPlayerId.toString());
      }

      nbt.setInteger("killsRequired", this.killsRequired);
      nbt.setInteger("natureType", this.natureType);
      nbt.setInteger("timeLimit", this.timeLimit);
      return nbt;
   }

   public static PvpObjective readFromNBT(NBTTagCompound nbt) {
      int typeOrd = nbt.getInteger("type");
      ObjectiveType type = typeOrd < ObjectiveType.values().length ? ObjectiveType.values()[typeOrd] : ObjectiveType.KILL_ANY_ENEMY;
      String targetVillage = nbt.hasKey("targetVillage") ? nbt.getString("targetVillage") : null;
      UUID targetPlayerId = null;
      if (nbt.hasKey("targetPlayerId")) {
         try {
            targetPlayerId = UUID.fromString(nbt.getString("targetPlayerId"));
         } catch (IllegalArgumentException var8) {
         }
      }

      int killsRequired = nbt.getInteger("killsRequired");
      int natureType = nbt.getInteger("natureType");
      int timeLimit = nbt.getInteger("timeLimit");
      return new PvpObjective(type, targetVillage, targetPlayerId, killsRequired, natureType, timeLimit);
   }

   public static enum ObjectiveType {
      KILL_VILLAGE,
      KILL_ANY_ENEMY,
      KILL_MULTI_VILLAGE,
      KILL_WITH_NATURE,
      KILL_WITH_DOJUTSU,
      KILL_HIGHER_LEVEL,
      KILL_IN_REGION,
      KILL_SPECIFIC_PLAYER,
      MUTUAL_HUNT,
      SURVIVE_DURATION,
      KILL_STREAK,
      KILL_DOJUTSU_USER;
   }
}
