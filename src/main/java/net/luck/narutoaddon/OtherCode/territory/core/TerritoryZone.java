
package net.luck.narutoaddon.OtherCode.territory.core;

import net.minecraft.nbt.NBTTagCompound;

public class TerritoryZone {
   private final String zoneId;
   private final String displayName;
   private int centerX;
   private int centerZ;
   private final int radius;
   private final String homeVillage;
   private final boolean isStrategic;
   private final double strategicMultiplier;
   private String ownerVillage;
   private float captureProgress;
   private String captureAttackerVillage;
   private boolean contested;
   private int wallLevel;
   private int garrisonLevel;
   private int trainingLevel;
   private int watchtowerLevel;
   private int specialistLevel;
   private boolean defendersSpawned;

   public TerritoryZone(String zoneId, String displayName, int centerX, int centerZ, int radius, String homeVillage, boolean isStrategic, double strategicMultiplier) {
      this.zoneId = zoneId;
      this.displayName = displayName;
      this.centerX = centerX;
      this.centerZ = centerZ;
      this.radius = radius;
      this.homeVillage = homeVillage;
      this.isStrategic = isStrategic;
      this.strategicMultiplier = strategicMultiplier;
      this.ownerVillage = homeVillage;
      this.captureProgress = 0.0F;
      this.captureAttackerVillage = "";
   }

   public void resetCapture() {
   }

   public boolean isHomeZone() {
      return false;
   }

   public boolean isCaptured() {
      return false;
   }

   public String getCurrentOwner() {
      return this.ownerVillage;
   }

   public void setOwnerVillage(String village) {
   }

   public float getCaptureProgress() {
      return this.captureProgress;
   }

   public void setCaptureProgress(float progress) {
   }

   public String getCaptureAttackerVillage() {
      return this.captureAttackerVillage != null ? this.captureAttackerVillage : "";
   }

   public void setCaptureAttackerVillage(String village) {
   }

   public String getZoneId() {
      return this.zoneId;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public int getCenterX() {
      return this.centerX;
   }

   public int getCenterZ() {
      return this.centerZ;
   }

   public void updatePosition(int x, int z) {
   }

   public int getRadius() {
      return this.radius;
   }

   public String getHomeVillage() {
      return this.homeVillage;
   }

   public boolean isStrategic() {
      return this.isStrategic;
   }

   public double getStrategicMultiplier() {
      return this.strategicMultiplier;
   }

   public String getOwnerVillage() {
      return this.ownerVillage;
   }

   public int getStrategicValue() {
      return 0;
   }

   public boolean isContested() {
      return false;
   }

   public void setContested(boolean contested) {
   }

   public int getWallLevel() {
      return this.wallLevel;
   }

   public void setWallLevel(int level) {
   }

   public int getGarrisonLevel() {
      return this.garrisonLevel;
   }

   public void setGarrisonLevel(int level) {
   }

   public int getTrainingLevel() {
      return this.trainingLevel;
   }

   public void setTrainingLevel(int level) {
   }

   public int getWatchtowerLevel() {
      return this.watchtowerLevel;
   }

   public void setWatchtowerLevel(int level) {
   }

   public int getSpecialistLevel() {
      return this.specialistLevel;
   }

   public void setSpecialistLevel(int level) {
   }

   public boolean isDefendersSpawned() {
      return this.defendersSpawned;
   }

   public void setDefendersSpawned(boolean spawned) {
   }

   public int getFortifyLevel() {
      return this.wallLevel;
   }

   public void setFortifyLevel(int level) {
   }

   public int getFortifyBonus() {
      return 0;
   }

   public int getGarrisonCount() {
      return 0;
   }

   public int getDefenderTier() {
      return 1;
   }

   public int getDefenderWaveLevel() {
      return 1;
   }

   public long getDefenderCooldownTicks() {
      return 0L;
   }

   public int getTotalFortifyInvestment() {
      return 0;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("zoneId", this.zoneId != null ? this.zoneId : "");
      return nbt;
   }

   public void readMutableFromNBT(NBTTagCompound nbt) {
   }

   public static TerritoryZone fromNBT(NBTTagCompound nbt) {
      String zoneId = nbt.getString("zoneId");
      String displayName = nbt.getString("displayName");
      return new TerritoryZone(zoneId != null ? zoneId : "", displayName != null ? displayName : "", 0, 0, 1, "", false, (double)1.0F);
   }

   public void resetToHome() {
   }
}
