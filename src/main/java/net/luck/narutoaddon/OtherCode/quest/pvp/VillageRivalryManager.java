package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.WarManager;

import java.util.Random;

public class VillageRivalryManager {
   private static final int NUM_VILLAGES = 6;
   private static final float[][] RIVALRY_MATRIX = new float[6][6];

   public static float getRivalryWeight(VillageHelper.Village v1, VillageHelper.Village v2) {
      if (v1 != VillageHelper.Village.UNKNOWN && v2 != VillageHelper.Village.UNKNOWN) {
         if (v1 == v2) {
            return 0.0F;
         } else {
            return v1.ordinal() < 6 && v2.ordinal() < 6 ? RIVALRY_MATRIX[v1.ordinal()][v2.ordinal()] : 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   public static float getEffectiveWeight(VillageHelper.Village v1, VillageHelper.Village v2) {
      float base = getRivalryWeight(v1, v2);
      if (v1 != v2 && v1 != VillageHelper.Village.UNKNOWN && v2 != VillageHelper.Village.UNKNOWN) {
         try {
            WarManager warManager = WarManager.getInstance();
            Object war1 = warManager.getActiveWar(v1);
            Object war2 = warManager.getActiveWar(v2);
            if (war1 != null || war2 != null) {
               return Math.max(base, 1.0F);
            }
         } catch (Exception var6) {
         }

         return base;
      } else {
         return base;
      }
   }

   public static VillageHelper.Village getTargetVillage(VillageHelper.Village playerVillage, Random random) {
      if (playerVillage == VillageHelper.Village.UNKNOWN) {
         return null;
      } else if (playerVillage.ordinal() >= 6) {
         return null;
      } else {
         float totalWeight = 0.0F;
         float[] weights = new float[6];

         for(int i = 0; i < 6; ++i) {
            if (i == playerVillage.ordinal()) {
               weights[i] = 0.0F;
            } else {
               weights[i] = getEffectiveWeight(playerVillage, VillageHelper.Village.values()[i]);
               totalWeight += weights[i];
            }
         }

         if (totalWeight <= 0.0F) {
            return null;
         } else {
            float roll = random.nextFloat() * totalWeight;
            float cumulative = 0.0F;

            for(int i = 0; i < 6; ++i) {
               cumulative += weights[i];
               if (roll < cumulative) {
                  return VillageHelper.Village.values()[i];
               }
            }

            return getRivalVillage(playerVillage);
         }
      }
   }

   public static VillageHelper.Village getRivalVillage(VillageHelper.Village playerVillage) {
      if (playerVillage == VillageHelper.Village.UNKNOWN) {
         return null;
      } else if (playerVillage.ordinal() >= 6) {
         return null;
      } else {
         float maxWeight = -1.0F;
         VillageHelper.Village rival = null;

         for(int i = 0; i < 6; ++i) {
            if (i != playerVillage.ordinal()) {
               float w = getEffectiveWeight(playerVillage, VillageHelper.Village.values()[i]);
               if (w > maxWeight) {
                  maxWeight = w;
                  rival = VillageHelper.Village.values()[i];
               }
            }
         }

         return rival;
      }
   }

   public static String getRivalryDescription(VillageHelper.Village v1, VillageHelper.Village v2) {
      float weight = getEffectiveWeight(v1, v2);
      return weight > 1.0F ? "at war" : "enemy shinobi";
   }

   static {
      for(int i = 0; i < 6; ++i) {
         for(int j = 0; j < 6; ++j) {
            if (i != j) {
               RIVALRY_MATRIX[i][j] = 1.0F;
            }
         }
      }

   }
}
