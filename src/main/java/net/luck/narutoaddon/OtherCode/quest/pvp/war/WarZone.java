
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class WarZone {
   private BlockPos center;
   private int radius;
   private VillageHelper.Village controllingVillage;
   private double captureProgress;
   private VillageHelper.Village capturingVillage;
   private boolean contested;
   private int zoneIndex;
   private static final double CAPTURE_RATE_PER_TICK = 0.005;
   private static final double DECAY_RATE_PER_TICK = 0.002;

   public WarZone(BlockPos center, int radius, int zoneIndex) {
      this.center = center;
      this.radius = radius;
      this.zoneIndex = zoneIndex;
      this.controllingVillage = null;
      this.captureProgress = (double)0.0F;
      this.capturingVillage = null;
      this.contested = false;
   }

   private WarZone() {
   }

   public BlockPos getCenter() {
      return this.center;
   }

   public int getRadius() {
      return this.radius;
   }

   public VillageHelper.Village getControllingVillage() {
      return this.controllingVillage;
   }

   public double getCaptureProgress() {
      return this.captureProgress;
   }

   public VillageHelper.Village getCapturingVillage() {
      return this.capturingVillage;
   }

   public boolean isContested() {
      return this.contested;
   }

   public int getZoneIndex() {
      return this.zoneIndex;
   }

   public boolean isPlayerInZone(EntityPlayerMP player) {
      double dx = player.posX - (double)this.center.getX();
      double dz = player.posZ - (double)this.center.getZ();
      return dx * dx + dz * dz <= (double)(this.radius * this.radius);
   }

   public void tickZone(VillageHelper.Village village1, VillageHelper.Village village2, List<EntityPlayerMP> playersInZone) {
      int count1 = 0;
      int count2 = 0;

      for(EntityPlayerMP player : playersInZone) {
         VillageHelper.Village pv = VillageHelper.getVillage(player);
         if (pv == village1) {
            ++count1;
         } else if (pv == village2) {
            ++count2;
         }
      }

      if (count1 > 0 && count2 > 0) {
         this.contested = true;
      } else {
         this.contested = false;
         if (count1 > 0) {
            this.handleCapture(village1, count1);
         } else if (count2 > 0) {
            this.handleCapture(village2, count2);
         } else if (this.captureProgress > (double)0.0F && this.controllingVillage == null) {
            this.captureProgress = Math.max((double)0.0F, this.captureProgress - 0.002);
            if (this.captureProgress <= (double)0.0F) {
               this.capturingVillage = null;
            }
         }

      }
   }

   private void handleCapture(VillageHelper.Village village, int playerCount) {
      double rate = 0.005 * (double)Math.min(playerCount, 3);
      if (this.controllingVillage != village) {
         if (this.controllingVillage != null && this.controllingVillage != village) {
            this.captureProgress = Math.max((double)0.0F, this.captureProgress - rate);
            if (this.captureProgress <= (double)0.0F) {
               this.controllingVillage = null;
               this.capturingVillage = village;
            }

         } else {
            if (this.capturingVillage != null && this.capturingVillage != village) {
               this.captureProgress = Math.max((double)0.0F, this.captureProgress - rate);
               if (this.captureProgress <= (double)0.0F) {
                  this.capturingVillage = village;
               }
            } else {
               this.capturingVillage = village;
               this.captureProgress = Math.min((double)1.0F, this.captureProgress + rate);
               if (this.captureProgress >= (double)1.0F) {
                  this.controllingVillage = village;
                  this.captureProgress = (double)1.0F;
               }
            }

         }
      }
   }

   public int getBeaconColor() {
      if (this.contested) {
         return 3;
      } else if (this.controllingVillage == null && this.capturingVillage == null) {
         return 0;
      } else {
         return this.controllingVillage != null ? this.controllingVillage.ordinal() + 1 : 0;
      }
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setInteger("centerX", this.center.getX());
      nbt.setInteger("centerY", this.center.getY());
      nbt.setInteger("centerZ", this.center.getZ());
      nbt.setInteger("radius", this.radius);
      nbt.setInteger("zoneIndex", this.zoneIndex);
      nbt.setDouble("captureProgress", this.captureProgress);
      nbt.setBoolean("contested", this.contested);
      if (this.controllingVillage != null) {
         nbt.setInteger("controllingVillage", this.controllingVillage.ordinal());
      }

      if (this.capturingVillage != null) {
         nbt.setInteger("capturingVillage", this.capturingVillage.ordinal());
      }

      return nbt;
   }

   public static WarZone readFromNBT(NBTTagCompound nbt) {
      WarZone zone = new WarZone();
      zone.center = new BlockPos(nbt.getInteger("centerX"), nbt.getInteger("centerY"), nbt.getInteger("centerZ"));
      zone.radius = nbt.getInteger("radius");
      zone.zoneIndex = nbt.getInteger("zoneIndex");
      zone.captureProgress = nbt.getDouble("captureProgress");
      zone.contested = nbt.getBoolean("contested");
      VillageHelper.Village[] villages = VillageHelper.Village.values();
      if (nbt.hasKey("controllingVillage")) {
         int ord = nbt.getInteger("controllingVillage");
         zone.controllingVillage = ord >= 0 && ord < villages.length ? villages[ord] : null;
      }

      if (nbt.hasKey("capturingVillage")) {
         int ord = nbt.getInteger("capturingVillage");
         zone.capturingVillage = ord >= 0 && ord < villages.length ? villages[ord] : null;
      }

      return zone;
   }
}
