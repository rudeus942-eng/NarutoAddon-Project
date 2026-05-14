
package net.luck.narutoaddon.OtherCode.stat.core;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class JutsuElementMapper {
   private static final Map<Class<?>, StatElement> CLASS_CACHE = new HashMap();
   private static final Map<String, StatElement> NAME_MAP = new HashMap();
   private static final Map<String, StatElement> JUTSU_TYPE_MAP = new HashMap();

   private JutsuElementMapper() {
   }

   @Nullable
   public static StatElement resolve(DamageSource source) {
      if (source == null) {
         return null;
      } else {
         String damageType = source.getDamageType();
         if (!"ninjutsu".equals(damageType) && !"senjutsu".equals(damageType)) {
            return null;
         } else {
            Entity immediate = source.getImmediateSource();
            if (immediate != null) {
               StatElement resolved = resolveFromEntity(immediate);
               if (resolved != null) {
                  return resolved;
               }
            }

            Entity trueSource = source.getTrueSource();
            if (trueSource != null && trueSource != immediate) {
               StatElement resolved = resolveFromEntity(trueSource);
               if (resolved != null) {
                  return resolved;
               }
            }

            return StatElement.GENERIC_NINJUTSU;
         }
      }
   }

   @Nullable
   static StatElement resolveFromEntity(Entity entity) {
      Class<?> clazz = entity.getClass();
      if (CLASS_CACHE.containsKey(clazz)) {
         return (StatElement)CLASS_CACHE.get(clazz);
      } else {
         StatElement fromIJutsu = tryIJutsu(entity);
         if (fromIJutsu != null) {
            CLASS_CACHE.put(clazz, fromIJutsu);
            return fromIJutsu;
         } else {
            StatElement fromName = tryNameMatch(clazz);
            if (fromName != null) {
               CLASS_CACHE.put(clazz, fromName);
               return fromName;
            } else {
               CLASS_CACHE.put(clazz, StatElement.GENERIC_NINJUTSU);
               return StatElement.GENERIC_NINJUTSU;
            }
         }
      }
   }

   @Nullable
   private static StatElement tryIJutsu(Entity entity) {
      try {
         Class<?> ijutsuClass = Class.forName("net.narutomod.item.ItemJutsu$IJutsu");
         if (!ijutsuClass.isInstance(entity)) {
            return null;
         } else {
            Object jutsuType = ijutsuClass.getMethod("getJutsuType").invoke(entity);
            if (jutsuType == null) {
               return null;
            } else {
               String typeName = jutsuType.toString();
               return (StatElement)JUTSU_TYPE_MAP.get(typeName);
            }
         }
      } catch (Exception var4) {
         return null;
      }
   }

   @Nullable
   private static StatElement tryNameMatch(Class<?> clazz) {
      String name = safeSimpleName(clazz);

      for(Map.Entry<String, StatElement> entry : NAME_MAP.entrySet()) {
         if (name.contains((CharSequence)entry.getKey())) {
            return (StatElement)entry.getValue();
         }
      }

      return null;
   }

   private static String safeSimpleName(Class<?> clazz) {
      String name = clazz.getName();
      int lastDot = name.lastIndexOf(46);
      return lastDot >= 0 ? name.substring(lastDot + 1) : name;
   }

   public static boolean isJutsuDamage(DamageSource source) {
      if (source == null) {
         return false;
      } else {
         String type = source.getDamageType();
         return "ninjutsu".equals(type) || "senjutsu".equals(type);
      }
   }

   static {
      NAME_MAP.put("Katon", StatElement.KATON);
      NAME_MAP.put("Fireball", StatElement.KATON);
      NAME_MAP.put("Fire", StatElement.KATON);
      NAME_MAP.put("Futon", StatElement.FUTON);
      NAME_MAP.put("Wind", StatElement.FUTON);
      NAME_MAP.put("Vacuum", StatElement.FUTON);
      NAME_MAP.put("Suiton", StatElement.SUITON);
      NAME_MAP.put("Water", StatElement.SUITON);
      NAME_MAP.put("Raiton", StatElement.RAITON);
      NAME_MAP.put("Lightning", StatElement.RAITON);
      NAME_MAP.put("Chidori", StatElement.RAITON);
      NAME_MAP.put("Doton", StatElement.DOTON);
      NAME_MAP.put("Earth", StatElement.DOTON);
      NAME_MAP.put("Mokuton", StatElement.MOKUTON);
      NAME_MAP.put("Wood", StatElement.MOKUTON);
      NAME_MAP.put("Hyoton", StatElement.HYOTON);
      NAME_MAP.put("Ice", StatElement.HYOTON);
      NAME_MAP.put("Ranton", StatElement.RANTON);
      NAME_MAP.put("Storm", StatElement.RANTON);
      NAME_MAP.put("Yoton", StatElement.YOTON);
      NAME_MAP.put("Lava", StatElement.YOTON);
      NAME_MAP.put("Futton", StatElement.FUTTON);
      NAME_MAP.put("Boil", StatElement.FUTTON);
      NAME_MAP.put("Bakuton", StatElement.BAKUTON);
      NAME_MAP.put("Explosion", StatElement.BAKUTON);
      NAME_MAP.put("Sharingan", StatElement.SHARINGAN);
      NAME_MAP.put("Amaterasu", StatElement.SHARINGAN);
      NAME_MAP.put("Susanoo", StatElement.SHARINGAN);
      NAME_MAP.put("Tsukuyomi", StatElement.SHARINGAN);
      NAME_MAP.put("Byakugan", StatElement.BYAKUGAN);
      NAME_MAP.put("Bone", StatElement.SHIKOTSUMYAKU);
      JUTSU_TYPE_MAP.put("KATON", StatElement.KATON);
      JUTSU_TYPE_MAP.put("FUTON", StatElement.FUTON);
      JUTSU_TYPE_MAP.put("SUITON", StatElement.SUITON);
      JUTSU_TYPE_MAP.put("RAITON", StatElement.RAITON);
      JUTSU_TYPE_MAP.put("DOTON", StatElement.DOTON);
      JUTSU_TYPE_MAP.put("MOKUTON", StatElement.MOKUTON);
      JUTSU_TYPE_MAP.put("HYOTON", StatElement.HYOTON);
      JUTSU_TYPE_MAP.put("RANTON", StatElement.RANTON);
      JUTSU_TYPE_MAP.put("YOTON", StatElement.YOTON);
      JUTSU_TYPE_MAP.put("FUTTON", StatElement.FUTTON);
      JUTSU_TYPE_MAP.put("BAKUTON", StatElement.BAKUTON);
      JUTSU_TYPE_MAP.put("SHARINGAN", StatElement.SHARINGAN);
      JUTSU_TYPE_MAP.put("BYAKUGAN", StatElement.BYAKUGAN);
      JUTSU_TYPE_MAP.put("SHIKOTSUMYAKU", StatElement.SHIKOTSUMYAKU);
      JUTSU_TYPE_MAP.put("NINJUTSU", StatElement.GENERIC_NINJUTSU);
      JUTSU_TYPE_MAP.put("INTON", StatElement.GENERIC_NINJUTSU);
      JUTSU_TYPE_MAP.put("IRYO", StatElement.GENERIC_NINJUTSU);
      JUTSU_TYPE_MAP.put("KUCHIYOSE", StatElement.GENERIC_NINJUTSU);
      JUTSU_TYPE_MAP.put("OTHER", StatElement.GENERIC_NINJUTSU);
   }
}
