package net.luck.narutoaddon.OtherCode.raid.boss;

import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public interface BossMechanic {
   String getName();

   String getDisplayName();

   String getWarningMessage();

   int getWarningTicks();

   int getDurationTicks();

   void onStart(EntityLivingBase var1, RaidInstance var2);

   void onTick(EntityLivingBase var1, RaidInstance var2, int var3);

   void onEnd(EntityLivingBase var1, RaidInstance var2);

   default List<BlockPos> getSafeZones() {
      return Collections.emptyList();
   }

   default int getSafeZoneRadius() {
      return 5;
   }

   default boolean allowsDamage() {
      return true;
   }

   default boolean isPuzzleMechanic() {
      return false;
   }

   default boolean isPuzzleComplete(EntityLivingBase boss, RaidInstance raid) {
      return true;
   }

   default void onInterrupt(EntityLivingBase boss, RaidInstance raid) {
      this.onEnd(boss, raid);
   }

   default float getEnrageDamageMultiplier(int enrageLevel) {
      switch (enrageLevel) {
         case 1:
            return 1.25F;
         case 2:
            return 1.5F;
         default:
            return 1.0F;
      }
   }

   default int getEnrageWarningReduction(int enrageLevel) {
      switch (enrageLevel) {
         case 1:
            return 0;
         case 2:
            return 20;
         default:
            return 0;
      }
   }

   default int getSelectionWeight() {
      return 10;
   }

   default boolean isHeavyMechanic() {
      return false;
   }
}
