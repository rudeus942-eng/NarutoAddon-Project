
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.narutomod.item.ItemJutsu;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(
   modid = "inftsukaddon"
)
public class KetsuryuganGenjutsuOverride {
   public static final int GENJUTSU_DURATION_TICKS = 100;
   public static final double TARGET_RANGE = (double)32.0F;
   private static boolean callbackSwapped = false;
   private static final Map<UUID, Long> activeTargets = new HashMap();

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         if (!callbackSwapped) {
            callbackSwapped = true;
            swapGenjutsuCallback();
         }

         if (!activeTargets.isEmpty()) {
            Iterator<Map.Entry<UUID, Long>> it = activeTargets.entrySet().iterator();

            while(it.hasNext()) {
               Map.Entry<UUID, Long> entry = (Map.Entry)it.next();
               EntityLivingBase target = findEntityByUUID((UUID)entry.getKey());
               if (target != null && target.isEntityAlive()) {
                  long now = target.world.getTotalWorldTime();
                  if (now >= (Long)entry.getValue()) {
                     it.remove();
                  } else {
                     pinEffects(target);
                  }
               } else {
                  it.remove();
               }
            }

         }
      }
   }

   private static void swapGenjutsuCallback() {
      try {
         Class<?> kets = Class.forName("net.narutomod.item.ItemKetsuryugan");
         Field genjutsuEnumField = kets.getField("GENJUTSU");
         Object jutsuEnum = genjutsuEnumField.get((Object)null);
         if (jutsuEnum == null) {
            System.err.println("[InfTsukAddon] ItemKetsuryugan.GENJUTSU is null — callback swap aborted.");
            return;
         }

         Field jutsuField = jutsuEnum.getClass().getField("jutsu");
         Field modifiers = Field.class.getDeclaredField("modifiers");
         modifiers.setAccessible(true);
         modifiers.setInt(jutsuField, jutsuField.getModifiers() & -17);
         jutsuField.set(jutsuEnum, new RedGenjutsuCallback());
         System.out.println("[InfTsukAddon] Replaced ItemKetsuryugan.GENJUTSU.jutsu with RedGenjutsuCallback.");
      } catch (Throwable t) {
         System.err.println("[InfTsukAddon] Failed to swap Ketsuryugan genjutsu callback: " + t);
         t.printStackTrace();
      }

   }

   private static EntityLivingBase findCrosshairTarget(EntityLivingBase caster, double range) {
      World world = caster.world;
      Vec3d eye = caster.getPositionEyes(1.0F);
      Vec3d look = caster.getLookVec();
      Vec3d far = eye.add(look.x * range, look.y * range, look.z * range);
      double maxDist = range;
      RayTraceResult blockRay = world.rayTraceBlocks(eye, far, false, true, false);
      if (blockRay != null && blockRay.typeOfHit == Type.BLOCK) {
         maxDist = blockRay.hitVec.distanceTo(eye);
      }

      AxisAlignedBB scan = caster.getEntityBoundingBox().expand(look.x * range, look.y * range, look.z * range).grow((double)1.0F);
      EntityLivingBase best = null;
      double bestDist = Double.MAX_VALUE;

      for(Entity e : world.getEntitiesWithinAABB(EntityLivingBase.class, scan)) {
         if (e != caster && e.isEntityAlive()) {
            AxisAlignedBB box = e.getEntityBoundingBox().grow(0.3);
            RayTraceResult hit = box.calculateIntercept(eye, far);
            if (hit != null) {
               double d = hit.hitVec.distanceTo(eye);
               if (!(d > maxDist) && d < bestDist) {
                  bestDist = d;
                  best = (EntityLivingBase)e;
               }
            }
         }
      }

      return best;
   }

   private static void applyGenjutsu(WorldServer world, EntityLivingBase caster, EntityLivingBase target) {
      activeTargets.put(target.getUniqueID(), world.getTotalWorldTime() + 100L);
      pinEffects(target);
      world.playSound((EntityPlayer)null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_ENDERMEN_STARE, SoundCategory.HOSTILE, 1.2F, 0.5F);
      if (caster instanceof EntityPlayer) {
         ((EntityPlayer)caster).sendStatusMessage(new TextComponentString("§4§l血 §c§oKetsuryugan Genjutsu cast on " + target.getName()), true);
      }

      if (target instanceof EntityPlayer) {
         ((EntityPlayer)target).sendStatusMessage(new TextComponentString("§4§l血 §c§oYou are caught in a Ketsuryugan Genjutsu..."), true);
      }

   }

   private static void pinEffects(EntityLivingBase target) {
      target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 4, 9, true, false));
      target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 4, 9, true, false));
      target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 4, 9, true, false));
   }

   private static EntityLivingBase findEntityByUUID(UUID id) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server == null) {
         return null;
      } else {
         for(WorldServer ws : server.worlds) {
            Entity e = ws.getEntityFromUuid(id);
            if (e instanceof EntityLivingBase) {
               return (EntityLivingBase)e;
            }
         }

         return null;
      }
   }

   public static class RedGenjutsuCallback implements ItemJutsu.IJutsuCallback {
      public boolean createJutsu(ItemStack stack, EntityLivingBase caster, float power) {
         if (caster == null) {
            return false;
         } else {
            World world = caster.world;
            if (world.isRemote) {
               return false;
            } else if (!(world instanceof WorldServer)) {
               return false;
            } else {
               EntityLivingBase target = KetsuryuganGenjutsuOverride.findCrosshairTarget(caster, (double)32.0F);
               if (target == null) {
                  if (caster instanceof EntityPlayer) {
                     ((EntityPlayer)caster).sendStatusMessage(new TextComponentString("§c§oNo target in sight."), true);
                  }

                  return false;
               } else {
                  KetsuryuganGenjutsuOverride.applyGenjutsu((WorldServer)world, caster, target);
                  return true;
               }
            }
         }
      }

      public float getBasePower() {
         return 1.0F;
      }

      public float getPowerupDelay() {
         return 0.0F;
      }

      public float getMaxPower() {
         return 1.0F;
      }
   }
}
