package net.luck.narutoaddon.OtherCode.akatsuki.market;

public class BlackMarketItem {
   private final String id;
   private final String displayName;
   private final String description;
   private final int tokenCost;
   private final DeployType deployType;
   private final int durationTicks;
   private final int cooldownTicks;
   private final String[] npcConfigIds;
   private final int npcCount;
   private final int zoneCount;
   private final float capSpeedMultiplier;
   private final boolean roaming;

   public BlackMarketItem(String id, String displayName, String description, int tokenCost, DeployType deployType, int durationTicks, int cooldownTicks, String[] npcConfigIds, int npcCount, int zoneCount, float capSpeedMultiplier, boolean roaming) {
      this.id = id;
      this.displayName = displayName;
      this.description = description;
      this.tokenCost = tokenCost;
      this.deployType = deployType;
      this.durationTicks = durationTicks;
      this.cooldownTicks = cooldownTicks;
      this.npcConfigIds = npcConfigIds;
      this.npcCount = npcCount;
      this.zoneCount = zoneCount;
      this.capSpeedMultiplier = capSpeedMultiplier;
      this.roaming = roaming;
   }

   public String getId() {
      return this.id;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getDescription() {
      return this.description;
   }

   public int getTokenCost() {
      return this.tokenCost;
   }

   public DeployType getDeployType() {
      return this.deployType;
   }

   public int getDurationTicks() {
      return this.durationTicks;
   }

   public int getCooldownTicks() {
      return this.cooldownTicks;
   }

   public String[] getNpcConfigIds() {
      return this.npcConfigIds;
   }

   public int getNpcCount() {
      return this.npcCount;
   }

   public int getZoneCount() {
      return this.zoneCount;
   }

   public float getCapSpeedMultiplier() {
      return this.capSpeedMultiplier;
   }

   public boolean isInstant() {
      return this.durationTicks <= 0;
   }

   public boolean isOffensive() {
      return this.deployType == DeployType.OFFENSIVE;
   }

   public boolean isDefensive() {
      return this.deployType == DeployType.DEFENSIVE;
   }

   public boolean isBuff() {
      return this.deployType == DeployType.BUFF;
   }

   public boolean isMassDeploy() {
      return this.deployType == DeployType.MASS_DEPLOY;
   }

   public boolean isRoaming() {
      return this.roaming;
   }

   public String getDurationDisplay() {
      if (this.durationTicks <= 0) {
         return "Instant";
      } else {
         int seconds = this.durationTicks / 20;
         if (seconds >= 3600) {
            return seconds / 3600 + "h";
         } else {
            return seconds >= 60 ? seconds / 60 + "m" : seconds + "s";
         }
      }
   }

   public static enum DeployType {
      OFFENSIVE,
      DEFENSIVE,
      SABOTAGE,
      BUFF,
      MASS_DEPLOY;
   }
}
