
package net.luck.narutoaddon.OtherCode.jutsu.domain;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.UUID;

public class InfiniteVoidEffects {
   public static final int DURATION_TICKS = 1200;
   public static final int DAMAGE_INTERVAL = 40;
   public static final float DAMAGE_PER_TICK = 2.0F;

   public static void tickEffects(WorldServer world, DomainInstance domain, long currentTime) {
      Vec3d center = domain.getCenterVec();
      int radius = domain.getRadius();
      AxisAlignedBB box = new AxisAlignedBB(center.x - (double)radius, center.y - (double)radius, center.z - (double)radius, center.x + (double)radius, center.y + (double)radius, center.z + (double)radius);
      List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
      UUID ownerId = domain.getOwnerUUID();
      boolean damageTick = (currentTime - domain.getStartTime()) % 40L == 0L;

      for(EntityLivingBase e : entities) {
         if (!e.getUniqueID().equals(ownerId) && domain.isInsideDomain(e.getPositionVector())) {
            e.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 3, false, true));
            e.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 40, 0, false, true));
            e.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 40, 0, false, true));
            e.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 40, 2, false, true));
            e.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 40, 2, false, true));
            if (damageTick) {
               e.hurtResistantTime = 0;
               e.hurtTime = 0;
               e.attackEntityFrom(DamageSource.OUT_OF_WORLD, 2.0F);
            }
         }
      }

   }
}
