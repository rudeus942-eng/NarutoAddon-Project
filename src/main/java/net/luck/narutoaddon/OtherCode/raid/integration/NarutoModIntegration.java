
package net.luck.narutoaddon.OtherCode.raid.integration;

import com.mojang.authlib.GameProfile;
import net.luck.narutoaddon.OtherCode.entity.EntityBossWoodGolem;
import net.luck.narutoaddon.OtherCode.entity.EntityHashiramaClone;
import net.luck.narutoaddon.OtherCode.entity.EntityWoodDragon;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.Loader;
import net.narutomod.Chakra;
import net.narutomod.entity.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class NarutoModIntegration {
   private static Boolean narutoModLoaded = null;
   private static final WeakHashMap<Entity, FakePlayer> fakePlayerRiders = new WeakHashMap();
   private static final Set<Entity> managedShieldEntities = new HashSet();
   private static final GameProfile HASHIRAMA_PROFILE = new GameProfile(UUID.fromString("12345678-1234-1234-1234-123456789abc"), "[Boss]Hashirama");

   public static boolean isNarutoModLoaded() {
      if (narutoModLoaded == null) {
         narutoModLoaded = Loader.isModLoaded("narutomod");
         if (narutoModLoaded) {
            System.out.println("[RaidBoss] NarutoMod detected! Integration enabled.");
         } else {
            System.out.println("[RaidBoss] NarutoMod not found. Wood Release abilities will use fallback entities.");
         }
      }

      return narutoModLoaded;
   }

   public static Entity spawnWoodGolem(World world, double x, double y, double z, EntityLivingBase owner) {
      try {
         EntityBossWoodGolem.EntityCustom golem = new EntityBossWoodGolem.EntityCustom(world);
         golem.setPosition(x, y, z);
         golem.setOwner(owner);
         golem.setGolemScale(3.0F);
         world.spawnEntity(golem);
         System.out.println("[RaidBoss] Spawned Boss Wood Golem at " + x + ", " + y + ", " + z);
         return golem;
      } catch (Exception e) {
         System.err.println("[RaidBoss] Failed to spawn Boss Wood Golem: " + e.getMessage());
         e.printStackTrace();
         return null;
      }
   }

   public static Entity spawnWoodGolem(World world, double x, double y, double z, EntityLivingBase owner, float scale) {
      try {
         EntityBossWoodGolem.EntityCustom golem = new EntityBossWoodGolem.EntityCustom(world);
         golem.setPosition(x, y, z);
         golem.setOwner(owner);
         golem.setGolemScale(scale);
         world.spawnEntity(golem);
         System.out.println("[RaidBoss] Spawned Boss Wood Golem (scale=" + scale + ") at " + x + ", " + y + ", " + z);
         return golem;
      } catch (Exception e) {
         System.err.println("[RaidBoss] Failed to spawn Boss Wood Golem: " + e.getMessage());
         e.printStackTrace();
         return null;
      }
   }

   public static Entity spawnWoodGolem(World world, double x, double y, double z, EntityLivingBase owner, float scale, int maxLifetimeTicks) {
      try {
         EntityBossWoodGolem.EntityCustom golem = new EntityBossWoodGolem.EntityCustom(world);
         golem.setPosition(x, y, z);
         golem.setOwner(owner);
         golem.setGolemScale(scale);
         golem.setMaxLifetime(maxLifetimeTicks);
         world.spawnEntity(golem);
         System.out.println("[RaidBoss] Spawned Boss Wood Golem (scale=" + scale + ", lifetime=" + maxLifetimeTicks + ") at " + x + ", " + y + ", " + z);
         return golem;
      } catch (Exception e) {
         System.err.println("[RaidBoss] Failed to spawn Boss Wood Golem: " + e.getMessage());
         e.printStackTrace();
         return null;
      }
   }

   public static Entity spawnWoodGolemForRaid(World world, double x, double y, double z, EntityLivingBase owner, RaidInstance raid) {
      Entity golem = spawnWoodGolem(world, x, y, z, owner);
      if (golem != null && raid != null) {
         raid.trackSpawnedEntity(golem);
      }

      return golem;
   }

   public static Entity spawnWoodGolemForRaid(World world, double x, double y, double z, EntityLivingBase owner, float scale, RaidInstance raid) {
      Entity golem = spawnWoodGolem(world, x, y, z, owner, scale);
      if (golem != null && raid != null) {
         raid.trackSpawnedEntity(golem);
      }

      return golem;
   }

   public static Entity spawnWoodGolemForRaid(World world, double x, double y, double z, EntityLivingBase owner, float scale, int maxLifetimeTicks, RaidInstance raid) {
      Entity golem = spawnWoodGolem(world, x, y, z, owner, scale, maxLifetimeTicks);
      if (golem != null && raid != null) {
         raid.trackSpawnedEntity(golem);
      }

      return golem;
   }

   public static Entity spawnNarutoWoodGolem(World world, double x, double y, double z, EntityLivingBase owner) {
      if (!isNarutoModLoaded()) {
         return null;
      } else {
         try {
            FakePlayer fakeRider = getOrCreateFakePlayer(world);
            if (fakeRider == null) {
               System.err.println("[RaidBoss] Failed to create FakePlayer for Wood Golem rider");
               return null;
            } else {
               fakeRider.setPositionAndUpdate(x, y, z);
               fillFakePlayerChakra(fakeRider);
               EntityWoodGolem.EC golem = new EntityWoodGolem.EC(fakeRider, (double)1.0F);
               golem.setPosition(x, y, z);
               golem.setSummoner(fakeRider);
               golem.setEntityInvulnerable(true);
               world.spawnEntity(golem);
               fakeRider.startRiding(golem, true);
               if (golem.getControllingPassenger() != fakeRider) {
                  try {
                     Method addPassenger = Entity.class.getDeclaredMethod("addPassenger", Entity.class);
                     addPassenger.setAccessible(true);
                     addPassenger.invoke(golem, fakeRider);
                  } catch (Exception e) {
                     System.err.println("[RaidBoss] Could not force add passenger: " + e.getMessage());
                  }
               }

               applyShieldEntityPersistence(golem, fakeRider);
               fakePlayerRiders.put(golem, fakeRider);
               managedShieldEntities.add(golem);
               Entity passenger = golem.getControllingPassenger();
               System.out.println("[RaidBoss] Spawned NarutoMod Wood Golem at " + x + ", " + y + ", " + z);
               System.out.println("[RaidBoss] Golem controlling passenger: " + (passenger != null ? passenger.getName() : "null"));
               System.out.println("[RaidBoss] Golem passengers count: " + golem.getPassengers().size());
               System.out.println("[RaidBoss] FakePlayer is riding: " + fakeRider.isRiding());
               return golem;
            }
         } catch (Exception e) {
            System.err.println("[RaidBoss] Failed to spawn NarutoMod Wood Golem: " + e.getMessage());
            e.printStackTrace();
            return null;
         }
      }
   }

   private static void applyShieldEntityPersistence(Entity entity, FakePlayer rider) {
      if (entity != null) {
         try {
            for(Class<?> clazz = entity.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
               for(Field field : clazz.getDeclaredFields()) {
                  field.setAccessible(true);
                  String fieldName = field.getName().toLowerCase();
                  Class<?> fieldType = field.getType();
                  if ((fieldType == Integer.TYPE || fieldType == Integer.class) && (fieldName.contains("tick") || fieldName.contains("time") || fieldName.contains("life") || fieldName.contains("age") || fieldName.contains("death") || fieldName.contains("despawn") || fieldName.contains("duration") || fieldName.contains("timer"))) {
                     if (!fieldName.contains("max") && !fieldName.contains("life") && !fieldName.contains("duration")) {
                        field.setInt(entity, 0);
                     } else {
                        field.setInt(entity, Integer.MAX_VALUE);
                     }

                     System.out.println("[RaidBoss] Reset field " + fieldName + " on " + entity.getClass().getSimpleName());
                  }

                  if (fieldType == Boolean.TYPE || fieldType == Boolean.class) {
                     if (fieldName.contains("dead") || fieldName.contains("remove") || fieldName.contains("despawn") || fieldName.contains("die") || fieldName.contains("kill") || fieldName.contains("destroy")) {
                        field.setBoolean(entity, false);
                        System.out.println("[RaidBoss] Set " + fieldName + " = false on " + entity.getClass().getSimpleName());
                     }

                     if (fieldName.contains("persist") || fieldName.contains("immortal") || fieldName.contains("invul") || fieldName.contains("nodespawn")) {
                        field.setBoolean(entity, true);
                        System.out.println("[RaidBoss] Set " + fieldName + " = true on " + entity.getClass().getSimpleName());
                     }
                  }

                  if (EntityLivingBase.class.isAssignableFrom(fieldType) && (fieldName.contains("summon") || fieldName.contains("owner") || fieldName.contains("caster") || fieldName.contains("user"))) {
                     Object current = field.get(entity);
                     if (current == null && rider != null) {
                        field.set(entity, rider);
                        System.out.println("[RaidBoss] Set " + fieldName + " to FakePlayer on " + entity.getClass().getSimpleName());
                     }
                  }
               }
            }
         } catch (Exception e) {
            System.err.println("[RaidBoss] Error applying persistence: " + e.getMessage());
         }

      }
   }

   private static FakePlayer getOrCreateFakePlayer(World world) {
      if (!(world instanceof WorldServer)) {
         return null;
      } else {
         WorldServer worldServer = (WorldServer)world;
         FakePlayer fakePlayer = FakePlayerFactory.get(worldServer, HASHIRAMA_PROFILE);
         if (fakePlayer != null) {
            fillFakePlayerChakra(fakePlayer);
         }

         return fakePlayer;
      }
   }

   public static void fillFakePlayerChakra(EntityPlayer player) {
      if (isNarutoModLoaded() && player != null) {
         try {
            Chakra.Pathway pathway = Chakra.pathway(player);
            if (pathway != null) {
               try {
                  Field amountField = pathway.getClass().getDeclaredField("amount");
                  amountField.setAccessible(true);
                  amountField.setDouble(pathway, (double)100000.0F);
                  Field maxField = pathway.getClass().getDeclaredField("max");
                  maxField.setAccessible(true);
                  maxField.setDouble(pathway, (double)100000.0F);
                  System.out.println("[RaidBoss] Set FakePlayer chakra to 100000");
               } catch (IllegalAccessException | NoSuchFieldException var8) {
                  try {
                     for(Field field : pathway.getClass().getDeclaredFields()) {
                        if (field.getType() == Double.TYPE || field.getType() == Float.TYPE) {
                           field.setAccessible(true);
                           field.setDouble(pathway, (double)100000.0F);
                        }
                     }
                  } catch (Exception e2) {
                     System.err.println("[RaidBoss] Could not set chakra via reflection: " + e2.getMessage());
                  }
               }
            }
         } catch (Exception e) {
            System.err.println("[RaidBoss] Could not fill FakePlayer chakra: " + e.getMessage());
         }

      }
   }

   public static void tickFakePlayerChakra() {
      for(FakePlayer rider : fakePlayerRiders.values()) {
         if (rider != null) {
            fillFakePlayerChakra(rider);
         }
      }

      Iterator<Entity> iter = managedShieldEntities.iterator();

      while(iter.hasNext()) {
         Entity entity = (Entity)iter.next();
         if (entity != null && !entity.isDead && entity.isEntityAlive()) {
            FakePlayer rider = (FakePlayer)fakePlayerRiders.get(entity);
            if (rider != null) {
               fillFakePlayerChakra(rider);
               if (!rider.isRiding() || rider.getRidingEntity() != entity) {
                  rider.startRiding(entity, true);
               }

               if (entity.getControllingPassenger() != rider) {
                  try {
                     Method addPassenger = Entity.class.getDeclaredMethod("addPassenger", Entity.class);
                     addPassenger.setAccessible(true);
                     addPassenger.invoke(entity, rider);
                  } catch (Exception var4) {
                     rider.startRiding(entity, true);
                  }
               }

               maintainShieldEntity(entity, rider);
            }
         } else {
            iter.remove();
            fakePlayerRiders.remove(entity);
         }
      }

   }

   private static void maintainShieldEntity(Entity entity, FakePlayer rider) {
      if (entity != null) {
         try {
            if (entity.ticksExisted > 1000) {
               entity.ticksExisted = 100;
            }

            for(Class<?> clazz = entity.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
               for(Field field : clazz.getDeclaredFields()) {
                  field.setAccessible(true);
                  String fieldName = field.getName().toLowerCase();
                  Class<?> fieldType = field.getType();
                  if (fieldType == Integer.TYPE) {
                     if (fieldName.contains("tickslived") || fieldName.contains("ageticks") || fieldName.contains("liveticks") || fieldName.contains("activeticks") || fieldName.contains("duration") || fieldName.contains("lifetime") || fieldName.contains("maxlife")) {
                        int current = field.getInt(entity);
                        if (current > 100) {
                           field.setInt(entity, 100);
                        }
                     }

                     if (fieldName.contains("deathtick") || fieldName.contains("killtick") || fieldName.contains("removetick") || fieldName.contains("despawntick")) {
                        field.setInt(entity, 0);
                     }

                     if (fieldName.contains("maxlifetime") || fieldName.contains("maxduration") || fieldName.contains("maxtime") || fieldName.contains("maxticks")) {
                        field.setInt(entity, 1073741823);
                     }
                  }

                  if (fieldType == Boolean.TYPE) {
                     if (fieldName.equals("dead") || fieldName.equals("isdead") || fieldName.contains("shoulddie") || fieldName.contains("markedfordeath")) {
                        field.setBoolean(entity, false);
                     }

                     if (fieldName.contains("persist") || fieldName.contains("immortal") || fieldName.contains("permanent")) {
                        field.setBoolean(entity, true);
                     }
                  }
               }
            }

            try {
               Method setSummoner = entity.getClass().getMethod("setSummoner", EntityLivingBase.class);
               setSummoner.invoke(entity, rider);
            } catch (NoSuchMethodException var11) {
               try {
                  Method setSummoner = entity.getClass().getSuperclass().getMethod("setSummoner", EntityLivingBase.class);
                  setSummoner.invoke(entity, rider);
               } catch (Exception var10) {
               }
            }
         } catch (Exception var12) {
         }

      }
   }

   public static void cleanupEntityRider(Entity entity) {
      FakePlayer rider = (FakePlayer)fakePlayerRiders.remove(entity);
      if (rider != null) {
         rider.dismountRidingEntity();
      }

      managedShieldEntities.remove(entity);
   }

   public static void cleanupShieldEntity(Entity entity) {
      cleanupEntityRider(entity);
   }

   public static void despawnManagedEntity(Entity entity) {
      if (entity != null) {
         cleanupEntityRider(entity);
         entity.setDead();
      }
   }

   public static int getManagedEntityCount() {
      return managedShieldEntities.size();
   }

   public static Entity spawnWoodGolemNoRider(World world, double x, double y, double z, EntityLivingBase owner) {
      if (!isNarutoModLoaded()) {
         return null;
      } else {
         try {
            EntityWoodGolem.EC golem = new EntityWoodGolem.EC(owner, (double)1.0F);
            golem.setPosition(x, y, z);
            golem.setSummoner(owner);
            world.spawnEntity(golem);
            System.out.println("[RaidBoss] Spawned Wood Golem (no rider) at " + x + ", " + y + ", " + z);
            return golem;
         } catch (Exception e) {
            System.err.println("[RaidBoss] Failed to spawn Wood Golem: " + e.getMessage());
            return null;
         }
      }
   }

   public static Entity spawnWoodPrison(World world, double x, double y, double z, EntityLivingBase owner) {
      return spawnWoodPrison(world, x, y, z, owner, 2, 3);
   }

   public static Entity spawnWoodPrison(World world, double x, double y, double z, EntityLivingBase owner, int radius, int height) {
      if (!isNarutoModLoaded()) {
         return null;
      } else {
         try {
            Vec3d pos = new Vec3d(x, y, z);
            float yaw = owner != null ? owner.rotationYaw : 0.0F;
            EntityWoodPrison.EC prison = new EntityWoodPrison.EC(world, pos, yaw);

            try {
               Field radiusField = EntityWoodPrison.EC.class.getDeclaredField("radius");
               radiusField.setAccessible(true);
               radiusField.setInt(prison, radius);
               Field tHeightField = EntityWoodPrison.EC.class.getDeclaredField("tHeight");
               tHeightField.setAccessible(true);
               tHeightField.setInt(prison, height);
               System.out.println("[RaidBoss] Set Wood Prison size: radius=" + radius + ", height=" + height);
            } catch (IllegalAccessException | NoSuchFieldException e) {
               System.err.println("[RaidBoss] Could not set Wood Prison size: " + ((ReflectiveOperationException)e).getMessage());
            }

            world.spawnEntity(prison);
            System.out.println("[RaidBoss] Spawned Wood Prison at " + x + ", " + y + ", " + z);
            return prison;
         } catch (Exception e) {
            System.err.println("[RaidBoss] Failed to spawn Wood Prison: " + e.getMessage());
            return null;
         }
      }
   }

   public static Entity spawnWoodPrisonForRaid(World world, double x, double y, double z, EntityLivingBase owner, RaidInstance raid) {
      Entity prison = spawnWoodPrison(world, x, y, z, owner);
      if (prison != null && raid != null) {
         raid.trackSpawnedEntity(prison);
      }

      return prison;
   }

   public static Entity spawnWoodPrisonForRaid(World world, double x, double y, double z, EntityLivingBase owner, int radius, int height, RaidInstance raid) {
      Entity prison = spawnWoodPrison(world, x, y, z, owner, radius, height);
      if (prison != null && raid != null) {
         raid.trackSpawnedEntity(prison);
      }

      return prison;
   }

   public static Entity spawnWoodForest(World world, double x, double y, double z, EntityLivingBase owner) {
      if (!isNarutoModLoaded()) {
         return null;
      } else {
         try {
            BlockPos pos = new BlockPos(x, y, z);
            EntityWoodForest.EC forest = new EntityWoodForest.EC(owner, pos, 1.0F);
            world.spawnEntity(forest);
            System.out.println("[RaidBoss] Spawned Wood Forest at " + x + ", " + y + ", " + z);
            return forest;
         } catch (Exception e) {
            System.err.println("[RaidBoss] Failed to spawn Wood Forest: " + e.getMessage());
            return null;
         }
      }
   }

   public static Entity spawnWoodArm(World world, double x, double y, double z, EntityLivingBase owner) {
      if (!isNarutoModLoaded()) {
         return null;
      } else {
         try {
            EntityWoodArm.EC arm = new EntityWoodArm.EC(owner, owner);
            arm.setPosition(x, y, z);
            world.spawnEntity(arm);
            System.out.println("[RaidBoss] Spawned Wood Arm at " + x + ", " + y + ", " + z);
            return arm;
         } catch (Exception e) {
            System.err.println("[RaidBoss] Failed to spawn Wood Arm: " + e.getMessage());
            return null;
         }
      }
   }

   public static Entity spawnWoodArm(World world, double x, double y, double z, EntityLivingBase owner, Entity target) {
      if (!isNarutoModLoaded()) {
         return null;
      } else {
         try {
            EntityWoodArm.EC arm = new EntityWoodArm.EC(owner, target);
            arm.setPosition(x, y, z);
            world.spawnEntity(arm);
            System.out.println("[RaidBoss] Spawned Wood Arm targeting " + target.getName());
            return arm;
         } catch (Exception e) {
            System.err.println("[RaidBoss] Failed to spawn Wood Arm: " + e.getMessage());
            return null;
         }
      }
   }

   public static Entity spawnWoodBurial(World world, double x, double y, double z, EntityLivingBase owner) {
      if (!isNarutoModLoaded()) {
         return null;
      } else {
         try {
            EntityWoodBurial.EC burial = new EntityWoodBurial.EC(owner);
            burial.setPosition(x, y, z);
            world.spawnEntity(burial);
            System.out.println("[RaidBoss] Spawned Wood Burial at " + x + ", " + y + ", " + z);
            return burial;
         } catch (Exception e) {
            System.err.println("[RaidBoss] Failed to spawn Wood Burial: " + e.getMessage());
            return null;
         }
      }
   }

   public static Entity spawnWoodCutting(World world, double x, double y, double z, EntityLivingBase owner) {
      if (!isNarutoModLoaded()) {
         return null;
      } else {
         try {
            EntityWoodCutting.EC cutting = new EntityWoodCutting.EC(owner);
            cutting.setPosition(x, y, z);
            world.spawnEntity(cutting);
            System.out.println("[RaidBoss] Spawned Wood Cutting at " + x + ", " + y + ", " + z);
            return cutting;
         } catch (Exception e) {
            System.err.println("[RaidBoss] Failed to spawn Wood Cutting: " + e.getMessage());
            return null;
         }
      }
   }

   public static Entity spawnBuddha1000(World world, double x, double y, double z, EntityLivingBase owner) {
      if (!isNarutoModLoaded()) {
         return null;
      } else {
         try {
            FakePlayer fakeRider = getOrCreateFakePlayer(world);
            if (fakeRider == null) {
               System.err.println("[RaidBoss] Failed to create FakePlayer for Buddha1000 rider");
               return null;
            } else {
               fakeRider.setPositionAndUpdate(x, y, z);
               fillFakePlayerChakra(fakeRider);
               EntityBuddha1000.EC buddha = new EntityBuddha1000.EC(fakeRider, (double)0.0F);
               buddha.setPosition(x, y, z);
               buddha.setSummoner(fakeRider);
               applyShieldEntityPersistence(buddha, fakeRider);
               world.spawnEntity(buddha);
               fakeRider.startRiding(buddha, true);
               Entity controller = buddha.getControllingPassenger();
               if (controller != fakeRider) {
                  System.err.println("[RaidBoss] WARNING: FakePlayer is not the controlling passenger!");
                  System.err.println("[RaidBoss] Controlling passenger: " + (controller != null ? controller.getName() : "null"));

                  try {
                     Method addPassenger = Entity.class.getDeclaredMethod("addPassenger", Entity.class);
                     addPassenger.setAccessible(true);
                     addPassenger.invoke(buddha, fakeRider);
                  } catch (Exception e) {
                     System.err.println("[RaidBoss] Could not force add FakePlayer as passenger: " + e.getMessage());
                  }
               }

               fakePlayerRiders.put(buddha, fakeRider);
               managedShieldEntities.add(buddha);
               System.out.println("[RaidBoss] Spawned Buddha1000 at " + x + ", " + y + ", " + z + " with FakePlayer rider");
               System.out.println("[RaidBoss] Buddha controlling passenger: " + (buddha.getControllingPassenger() != null ? buddha.getControllingPassenger().getName() : "null"));
               return buddha;
            }
         } catch (Exception e) {
            System.err.println("[RaidBoss] Failed to spawn Buddha1000: " + e.getMessage());
            e.printStackTrace();
            return null;
         }
      }
   }

   public static float getChakra(EntityPlayer player) {
      if (isNarutoModLoaded() && player != null) {
         try {
            Chakra.Pathway pathway = Chakra.pathway(player);
            if (pathway != null) {
               return (float)pathway.getAmount();
            }
         } catch (Exception var2) {
         }

         return 0.0F;
      } else {
         return 0.0F;
      }
   }

   public static boolean drainChakra(EntityPlayer player, float amount) {
      if (isNarutoModLoaded() && player != null) {
         try {
            Chakra.Pathway pathway = Chakra.pathway(player);
            if (pathway != null) {
               pathway.consume(amount);
               return true;
            }
         } catch (Exception e) {
            System.err.println("[RaidBoss] Failed to drain chakra: " + e.getMessage());
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean addChakra(EntityPlayer player, float amount) {
      return isNarutoModLoaded() && player != null ? false : false;
   }

   public static float getMaxChakra(EntityPlayer player) {
      if (isNarutoModLoaded() && player != null) {
         try {
            Chakra.Pathway pathway = Chakra.pathway(player);
            if (pathway != null) {
               return (float)pathway.getMax();
            }
         } catch (Exception var2) {
         }

         return 0.0F;
      } else {
         return 0.0F;
      }
   }

   public static boolean isWoodReleaseEntity(Entity entity) {
      if (entity != null && isNarutoModLoaded()) {
         return entity instanceof EntityWoodGolem.EC || entity instanceof EntityWoodPrison.EC || entity instanceof EntityWoodForest.EC || entity instanceof EntityWoodArm.EC || entity instanceof EntityWoodBurial.EC || entity instanceof EntityWoodCutting.EC || entity instanceof EntityBuddha1000.EC;
      } else {
         return false;
      }
   }

   public static boolean isNarutoModEntity(Entity entity) {
      if (entity == null) {
         return false;
      } else {
         String className = entity.getClass().getName();
         return className.startsWith("net.narutomod.");
      }
   }

   public static Entity spawnWoodDragon(World world, double x, double y, double z, EntityLivingBase owner, EntityLivingBase target) {
      try {
         EntityWoodDragon.EntityCustom dragon = new EntityWoodDragon.EntityCustom(world, owner, target);
         dragon.setPosition(x, y, z);
         dragon.setDragonScale(1.5F);
         dragon.setMaxLifetime(400);
         world.spawnEntity(dragon);
         System.out.println("[RaidBoss] Spawned Wood Dragon at " + x + ", " + y + ", " + z + " targeting " + (target != null ? target.getName() : "null"));
         return dragon;
      } catch (Exception e) {
         System.err.println("[RaidBoss] Failed to spawn Wood Dragon: " + e.getMessage());
         e.printStackTrace();
         return null;
      }
   }

   public static Entity spawnWoodDragon(World world, double x, double y, double z, EntityLivingBase owner, EntityLivingBase target, float scale, float damage, int lifetimeTicks) {
      try {
         EntityWoodDragon.EntityCustom dragon = new EntityWoodDragon.EntityCustom(world, owner, target);
         dragon.setPosition(x, y, z);
         dragon.setDragonScale(scale);
         dragon.setDamage(damage);
         dragon.setMaxLifetime(lifetimeTicks);
         world.spawnEntity(dragon);
         System.out.println("[RaidBoss] Spawned Wood Dragon (scale=" + scale + ", damage=" + damage + ") at " + x + ", " + y + ", " + z);
         return dragon;
      } catch (Exception e) {
         System.err.println("[RaidBoss] Failed to spawn Wood Dragon: " + e.getMessage());
         e.printStackTrace();
         return null;
      }
   }

   public static Entity spawnWoodDragonForRaid(World world, double x, double y, double z, EntityLivingBase owner, EntityLivingBase target, RaidInstance raid) {
      Entity dragon = spawnWoodDragon(world, x, y, z, owner, target);
      if (dragon != null && raid != null) {
         raid.trackSpawnedEntity(dragon);
      }

      return dragon;
   }

   public static Entity spawnWoodDragonForRaid(World world, double x, double y, double z, EntityLivingBase owner, EntityLivingBase target, float scale, float damage, int lifetimeTicks, RaidInstance raid) {
      Entity dragon = spawnWoodDragon(world, x, y, z, owner, target, scale, damage, lifetimeTicks);
      if (dragon != null && raid != null) {
         raid.trackSpawnedEntity(dragon);
      }

      return dragon;
   }

   public static Entity spawnWoodClone(World world, double x, double y, double z, EntityLivingBase owner) {
      try {
         EntityHashiramaClone.EntityCustom clone = new EntityHashiramaClone.EntityCustom(world, owner);
         clone.setPosition(x, y, z);
         clone.setCloneHealth(50.0F);
         clone.setCloneDamage(10.0F);
         clone.setMaxLifetime(600);
         world.spawnEntity(clone);
         System.out.println("[RaidBoss] Spawned Hashirama Clone at " + x + ", " + y + ", " + z);
         return clone;
      } catch (Exception e) {
         System.err.println("[RaidBoss] Failed to spawn Hashirama Clone: " + e.getMessage());
         e.printStackTrace();
         return null;
      }
   }

   public static Entity spawnWoodClone(World world, double x, double y, double z, EntityLivingBase owner, float health, float damage, int lifetimeTicks) {
      try {
         EntityHashiramaClone.EntityCustom clone = new EntityHashiramaClone.EntityCustom(world, owner);
         clone.setPosition(x, y, z);
         clone.setCloneHealth(health);
         clone.setCloneDamage(damage);
         clone.setMaxLifetime(lifetimeTicks);
         world.spawnEntity(clone);
         System.out.println("[RaidBoss] Spawned Hashirama Clone (health=" + health + ", damage=" + damage + ") at " + x + ", " + y + ", " + z);
         return clone;
      } catch (Exception e) {
         System.err.println("[RaidBoss] Failed to spawn Hashirama Clone: " + e.getMessage());
         e.printStackTrace();
         return null;
      }
   }

   public static Entity spawnWoodCloneForRaid(World world, double x, double y, double z, EntityLivingBase owner, RaidInstance raid) {
      Entity clone = spawnWoodClone(world, x, y, z, owner);
      if (clone != null && raid != null) {
         raid.trackSpawnedEntity(clone);
      }

      return clone;
   }

   public static Entity spawnWoodCloneForRaid(World world, double x, double y, double z, EntityLivingBase owner, float health, float damage, int lifetimeTicks, RaidInstance raid) {
      Entity clone = spawnWoodClone(world, x, y, z, owner, health, damage, lifetimeTicks);
      if (clone != null && raid != null) {
         raid.trackSpawnedEntity(clone);
      }

      return clone;
   }
}
