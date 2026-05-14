package net.luck.narutoaddon.OtherCode;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Sostituisci "ElementsInfTsukAddon" con il nome della tua classe ModElement principale
public class NewPlayerPvpProtection {
   public static final double PVP_XP_THRESHOLD = 10000.0;
   private static final String BATTLE_XP_KEY = "battle_experience";
   private static final long MESSAGE_COOLDOWN_MS = 5000L;

   public static class Handler {
      private final Map<UUID, Long> lastMessageTime = new HashMap<>();
      private final Map<UUID, Long> recentlyBlockedTargets = new HashMap<>();
      private final Map<Class<?>, String> reflectionFieldCache = new HashMap<>();
      private final Map<Class<?>, String> reflectionMethodCache = new HashMap<>();
      private static final String NO_FIELD = "__NONE__";
      private static final String NO_METHOD = "__NONE__";

      @SubscribeEvent(priority = EventPriority.HIGHEST)
      public void onLivingAttack(LivingAttackEvent event) {
         if (event.isCanceled()) return;
         if (!(event.getEntity() instanceof EntityPlayer)) return;

         EntityPlayer target = (EntityPlayer) event.getEntity();
         if (target.world.isRemote) return;

         Entity trueSource = event.getSource().getTrueSource();
         EntityPlayer attacker = (trueSource instanceof EntityPlayer) ? (EntityPlayer) trueSource : null;

         if (attacker == null) {
            attacker = this.traceOwnerFromImmediate(event.getSource().getImmediateSource());
         }

         if (attacker == null) {
            if (this.isProtectedPlayer(target) && this.isRecentlyBlocked(target)) {
               event.setCanceled(true);
            }
         } else if (!attacker.getUniqueID().equals(target.getUniqueID())) {
            long worldTime = target.world.getTotalWorldTime();
            long targetProtect = target.getEntityData().getLong("travel_pvp_protect");

            if (worldTime < targetProtect) {
               event.setCanceled(true);
               this.markRecentlyBlocked(target);
               this.sendRateLimitedMessage(attacker, "§c[PvP] Quel giocatore ha l'immunità PvP temporanea!");
            } else {
               double targetXp = target.getEntityData().getDouble(BATTLE_XP_KEY);
               double attackerXp = attacker.getEntityData().getDouble(BATTLE_XP_KEY);

               if (targetXp < PVP_XP_THRESHOLD) {
                  event.setCanceled(true);
                  this.markRecentlyBlocked(target);
                  this.sendRateLimitedMessage(attacker, "§c[PvP] Questo giocatore è un nuovo shinobi (sotto 10k XP) ed è protetto.");
               }
            }
         }
      }

      private EntityPlayer traceOwnerFromImmediate(Entity immediate) {
         if (immediate == null) return null;
         if (immediate instanceof EntityThrowable) {
            EntityLivingBase thrower = ((EntityThrowable) immediate).getThrower();
            if (thrower instanceof EntityPlayer) return (EntityPlayer) thrower;
         }
         return this.traceOwnerCached(immediate);
      }

      private EntityPlayer traceOwnerCached(Entity entity) {
         Class<?> entityClass = entity.getClass();
         String cachedField = reflectionFieldCache.getOrDefault(entityClass, findOwnerFieldName(entityClass));
         reflectionFieldCache.putIfAbsent(entityClass, cachedField);

         if (!NO_FIELD.equals(cachedField)) {
            EntityPlayer result = this.readPlayerField(entity, cachedField);
            if (result != null) return result;
         }

         String cachedMethod = reflectionMethodCache.getOrDefault(entityClass, findOwnerMethodName(entityClass));
         reflectionMethodCache.putIfAbsent(entityClass, cachedMethod);

         return !NO_METHOD.equals(cachedMethod) ? this.callPlayerMethod(entity, cachedMethod) : null;
      }

      private String findOwnerFieldName(Class<?> entityClass) {
         String[] names = {"thrower", "shootingEntity", "owner"};
         for (String name : names) {
            try {
               entityClass.getDeclaredField(name);
               return name;
            } catch (NoSuchFieldException ignored) {}
         }
         return NO_FIELD;
      }

      private String findOwnerMethodName(Class<?> entityClass) {
         String[] names = {"getShooter", "getThrower"};
         for (String name : names) {
            try {
               entityClass.getDeclaredMethod(name);
               return name;
            } catch (NoSuchMethodException ignored) {}
         }
         return NO_METHOD;
      }

      private EntityPlayer readPlayerField(Entity entity, String fieldName) {
         try {
            Field f = entity.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object val = f.get(entity);
            return (val instanceof EntityPlayer) ? (EntityPlayer) val : null;
         } catch (Exception e) { return null; }
      }

      private EntityPlayer callPlayerMethod(Entity entity, String methodName) {
         try {
            Method m = entity.getClass().getDeclaredMethod(methodName);
            m.setAccessible(true);
            Object val = m.invoke(entity);
            return (val instanceof EntityPlayer) ? (EntityPlayer) val : null;
         } catch (Exception e) { return null; }
      }

      private boolean isProtectedPlayer(EntityPlayer player) {
         return player.getEntityData().getDouble(BATTLE_XP_KEY) < PVP_XP_THRESHOLD;
      }

      private void markRecentlyBlocked(EntityPlayer target) {
         this.recentlyBlockedTargets.put(target.getUniqueID(), target.world.getTotalWorldTime());
      }

      private boolean isRecentlyBlocked(EntityPlayer target) {
         Long blockedTick = recentlyBlockedTargets.get(target.getUniqueID());
         return blockedTick != null && blockedTick == target.world.getTotalWorldTime();
      }

      private void sendRateLimitedMessage(EntityPlayer player, String message) {
         long now = System.currentTimeMillis();
         Long last = lastMessageTime.get(player.getUniqueID());
         if (last == null || now - last >= MESSAGE_COOLDOWN_MS) {
            lastMessageTime.put(player.getUniqueID(), now);
            player.sendMessage(new TextComponentString(message));
         }
      }
   }
}