
package net.luck.narutoaddon.OtherCode.jutsu;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.narutomod.item.ItemJutsu;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FutonPatcher {
   public static final ItemJutsu.JutsuEnum WIND_CUTTER = new ItemJutsu.JutsuEnum(6, "entity.wind_cutter.name", 'B', (double)35.0F, new EntityWindCutter.Jutsu());
   public static final ItemJutsu.JutsuEnum PRESSURE_DAMAGE = new ItemJutsu.JutsuEnum(7, "entity.futon_pressure_damage.name", 'B', (double)65.0F, new EntityFutonPressureDamage.Jutsu());
   public static final ItemJutsu.JutsuEnum TORNADO = new ItemJutsu.JutsuEnum(8, "entity.futon_tornado.name", 'B', (double)75.0F, new EntityFutonTornado.Jutsu());
   private static boolean patched = false;

   public static void inject() {
      if (!patched) {
         patched = true;

         try {
            Item futonItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("narutomod", "futon"));
            if (futonItem == null) {
               System.err.println("[InfTsuk] FutonPatcher: narutomod:futon item not found");
               return;
            }

            Class<?> baseClass;
            for(baseClass = futonItem.getClass().getSuperclass(); baseClass != null && !baseClass.getName().contains("ItemJutsu$Base"); baseClass = baseClass.getSuperclass()) {
            }

            if (baseClass == null) {
               System.err.println("[InfTsuk] FutonPatcher: Could not find ItemJutsu.Base in hierarchy");
               return;
            }

            Field jutsuListField = null;

            for(Field f : baseClass.getDeclaredFields()) {
               f.setAccessible(true);
               if (ImmutableList.class.isAssignableFrom(f.getType())) {
                  Object val = f.get(futonItem);
                  if (val instanceof ImmutableList) {
                     ImmutableList<?> list = (ImmutableList)val;
                     if (!list.isEmpty() && list.get(0) instanceof ItemJutsu.JutsuEnum) {
                        jutsuListField = f;
                        break;
                     }
                  }
               }
            }

            if (jutsuListField == null) {
               System.err.println("[InfTsuk] FutonPatcher: Could not find jutsuList field");
               return;
            }

            jutsuListField.setAccessible(true);
            ImmutableList<?> currentList = (ImmutableList)jutsuListField.get(futonItem);
            List<Object> newList = new ArrayList();
            UnmodifiableIterator var17 = currentList.iterator();

            while(var17.hasNext()) {
               Object obj = var17.next();
               newList.add(obj);
            }

            newList.add(WIND_CUTTER);
            newList.add(PRESSURE_DAMAGE);
            newList.add(TORNADO);
            ImmutableList<Object> newImmutableList = ImmutableList.copyOf(newList);
            setFinalField(jutsuListField, futonItem, newImmutableList);
            int newSize = newList.size();

            for(Field f : baseClass.getDeclaredFields()) {
               f.setAccessible(true);
               Object val = f.get(futonItem);
               if (val instanceof long[]) {
                  long[] oldArr = (long[])val;
                  if (oldArr.length < newSize) {
                     long[] newArr = new long[newSize];
                     Arrays.fill(newArr, -1L);
                     System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
                     setFinalField(f, futonItem, newArr);
                     System.out.println("[InfTsuk] FutonPatcher: Extended cooldown map from " + oldArr.length + " to " + newSize);
                  }
               }

               if (val instanceof int[]) {
                  int[] oldArr = (int[])val;
                  if (oldArr.length < newSize) {
                     int[] newArr = new int[newSize];
                     System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
                     setFinalField(f, futonItem, newArr);
                     System.out.println("[InfTsuk] FutonPatcher: Extended XP map from " + oldArr.length + " to " + newSize);
                  }
               }
            }

            System.out.println("[InfTsuk] FutonPatcher: Successfully injected Wind Cutter, Pressure Damage, and Tornado into Wind Release!");
         } catch (Exception e) {
            System.err.println("[InfTsuk] FutonPatcher: Injection failed: " + e.getMessage());
            e.printStackTrace();
         }

      }
   }

   private static void setFinalField(Field field, Object target, Object value) throws Exception {
      field.setAccessible(true);
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(field, field.getModifiers() & -17);
      field.set(target, value);
   }
}
