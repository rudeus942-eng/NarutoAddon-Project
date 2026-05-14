
package net.luck.narutoaddon.OtherCode.territory.core;

import net.minecraft.nbt.NBTTagCompound;

public class PerPlayerTerritoryData {
   private boolean territoryEnabled = false;
   private long lastToggleTick = 0L;
   private int weeklyKills = 0;
   private int weeklyCaptures = 0;
   private int weeklyPoints = 0;
   private String village = "";
   private int pendingWeeklyReward = 0;
   private String pendingRewardTier = "";
   private double lastCheckX;
   private double lastCheckY;
   private double lastCheckZ;

   public boolean canToggle(long currentTick) {
      return currentTick - this.lastToggleTick >= 2400L;
   }

   public void toggle(long currentTick) {
      this.territoryEnabled = !this.territoryEnabled;
      this.lastToggleTick = currentTick;
   }

   public boolean isEnabled() {
      return this.territoryEnabled;
   }

   public void addKill() {
      ++this.weeklyKills;
   }

   public void addCapture() {
      ++this.weeklyCaptures;
   }

   public void addPoints(int amount) {
      this.weeklyPoints += amount;
   }

   public void resetWeekly() {
      this.weeklyKills = 0;
      this.weeklyCaptures = 0;
      this.weeklyPoints = 0;
   }

   public int getWeeklyKills() {
      return this.weeklyKills;
   }

   public int getWeeklyCaptures() {
      return this.weeklyCaptures;
   }

   public int getWeeklyPoints() {
      return this.weeklyPoints;
   }

   public void setWeeklyKills(int kills) {
      this.weeklyKills = kills;
   }

   public void setWeeklyCaptures(int captures) {
      this.weeklyCaptures = captures;
   }

   public void setWeeklyPoints(int points) {
      this.weeklyPoints = points;
   }

   public long getLastToggleTick() {
      return this.lastToggleTick;
   }

   public String getVillage() {
      return this.village;
   }

   public void setVillage(String village) {
      this.village = village != null ? village : "";
   }

   public int getPendingWeeklyReward() {
      return this.pendingWeeklyReward;
   }

   public String getPendingRewardTier() {
      return this.pendingRewardTier;
   }

   public boolean hasPendingReward() {
      return this.pendingWeeklyReward > 0;
   }

   public void setPendingWeeklyReward(int amount, String tierName) {
      this.pendingWeeklyReward = amount;
      this.pendingRewardTier = tierName != null ? tierName : "";
   }

   public void clearPendingReward() {
      this.pendingWeeklyReward = 0;
      this.pendingRewardTier = "";
   }

   public void updatePosition(double x, double y, double z) {
      this.lastCheckX = x;
      this.lastCheckY = y;
      this.lastCheckZ = z;
   }

   public boolean hasMovedSince(double x, double y, double z, double threshold) {
      double dx = x - this.lastCheckX;
      double dy = y - this.lastCheckY;
      double dz = z - this.lastCheckZ;
      return dx * dx + dy * dy + dz * dz > threshold * threshold;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setBoolean("territoryEnabled", this.territoryEnabled);
      nbt.setLong("lastToggleTick", this.lastToggleTick);
      nbt.setInteger("weeklyKills", this.weeklyKills);
      nbt.setInteger("weeklyCaptures", this.weeklyCaptures);
      nbt.setInteger("weeklyPoints", this.weeklyPoints);
      nbt.setString("village", this.village);
      nbt.setInteger("pendingWeeklyReward", this.pendingWeeklyReward);
      nbt.setString("pendingRewardTier", this.pendingRewardTier);
      nbt.setDouble("lastCheckX", this.lastCheckX);
      nbt.setDouble("lastCheckY", this.lastCheckY);
      nbt.setDouble("lastCheckZ", this.lastCheckZ);
      return nbt;
   }

   public static PerPlayerTerritoryData fromNBT(NBTTagCompound nbt) {
      PerPlayerTerritoryData data = new PerPlayerTerritoryData();
      data.territoryEnabled = nbt.getBoolean("territoryEnabled");
      data.lastToggleTick = nbt.getLong("lastToggleTick");
      data.weeklyKills = nbt.getInteger("weeklyKills");
      data.weeklyCaptures = nbt.getInteger("weeklyCaptures");
      data.weeklyPoints = nbt.getInteger("weeklyPoints");
      data.village = nbt.getString("village");
      data.pendingWeeklyReward = nbt.getInteger("pendingWeeklyReward");
      data.pendingRewardTier = nbt.getString("pendingRewardTier");
      data.lastCheckX = nbt.getDouble("lastCheckX");
      data.lastCheckY = nbt.getDouble("lastCheckY");
      data.lastCheckZ = nbt.getDouble("lastCheckZ");
      return data;
   }
}
