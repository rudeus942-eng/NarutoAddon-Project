
package net.luck.narutoaddon.OtherCode.raid.integration;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.narutomod.entity.EntityWoodGolem;

import java.lang.reflect.Field;

public class BossWoodGolem {
   private static Boolean narutoModLoaded = null;

   public static Entity create(World world, double x, double y, double z, EntityLivingBase owner) {
      if (!isNarutoModLoaded()) {
         return null;
      } else {
         try {
            EntityWoodGolem.EC golem = new EntityWoodGolem.EC(owner, (double)1.0F);
            golem.setPosition(x, y, z);
            golem.setSummoner(owner);
            golem.setEntityInvulnerable(true);
            golem.setAir(Integer.MAX_VALUE);
            world.spawnEntity(golem);
            System.out.println("[RaidBoss] Spawned persistent Wood Golem at " + x + ", " + y + ", " + z);
            return golem;
         } catch (Exception e) {
            System.err.println("[RaidBoss] Failed to create BossWoodGolem: " + e.getMessage());
            e.printStackTrace();
            return null;
         }
      }
   }

   public static void maintainAlive(Entity golem) {
      if (golem != null && !golem.isDead) {
         if (golem.isEntityAlive()) {
            try {
               Field deadField = Entity.class.getDeclaredField("dead");
               deadField.setAccessible(true);
               deadField.setBoolean(golem, false);
            } catch (Exception var2) {
            }
         }

      }
   }

   private static boolean isNarutoModLoaded() {
      if (narutoModLoaded == null) {
         narutoModLoaded = Loader.isModLoaded("narutomod");
      }

      return narutoModLoaded;
   }
}
