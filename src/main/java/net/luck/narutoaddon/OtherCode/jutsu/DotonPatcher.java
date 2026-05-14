
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

public class DotonPatcher {
   public static final ItemJutsu.JutsuEnum WEIGHTED_BOULDER = new ItemJutsu.JutsuEnum(7, "entity.weighted_boulder.name", 'B', (double)45.0F, new EntityWeightedBoulder.Jutsu());
   public static final ItemJutsu.JutsuEnum EARTH_SPEAR = new ItemJutsu.JutsuEnum(8, "entity.doton_earth_spear.name", 'B', (double)50.0F, new EntityDotonEarthSpear.Jutsu());
   public static final ItemJutsu.JutsuEnum MOLE_HIDE = new ItemJutsu.JutsuEnum(9, "entity.doton_mole_hide.name", 'C', (double)40.0F, new EntityDotonMoleHide.Jutsu());
   private static boolean patched = false;

   public static void inject() {
      if (!patched) {
         patched = true;

         try {
            Item dotonItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("narutomod", "doton"));
            if (dotonItem == null) {
               System.err.println("[InfTsuk] DotonPatcher: narutomod:doton item not found");
               return;
            }

            Class<?> baseClass;
            for(baseClass = dotonItem.getClass().getSuperclass(); baseClass != null && !baseClass.getName().contains("ItemJutsu$Base"); baseClass = baseClass.getSuperclass()) {
            }

            if (baseClass == null) {
               System.err.println("[InfTsuk] DotonPatcher: Could not find ItemJutsu.Base in hierarchy");
               return;
            }

            Field jutsuListField = null;

            for(Field f : baseClass.getDeclaredFields()) {
               f.setAccessible(true);
               if (ImmutableList.class.isAssignableFrom(f.getType())) {
                  Object val = f.get(dotonItem);
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
               System.err.println("[InfTsuk] DotonPatcher: Could not find jutsuList field");
               return;
            }

            jutsuListField.setAccessible(true);
            ImmutableList<?> currentList = (ImmutableList)jutsuListField.get(dotonItem);
            List<Object> newList = new ArrayList();
            UnmodifiableIterator var17 = currentList.iterator();

            while(var17.hasNext()) {
               Object obj = var17.next();
               newList.add(obj);
            }

            newList.add(WEIGHTED_BOULDER);
            newList.add(EARTH_SPEAR);
            newList.add(MOLE_HIDE);
            ImmutableList<Object> newImmutableList = ImmutableList.copyOf(newList);
            setFinalField(jutsuListField, dotonItem, newImmutableList);
            int newSize = newList.size();

            for(Field f : baseClass.getDeclaredFields()) {
               f.setAccessible(true);
               Object val = f.get(dotonItem);
               if (val instanceof long[]) {
                  long[] oldArr = (long[])val;
                  if (oldArr.length < newSize) {
                     long[] newArr = new long[newSize];
                     Arrays.fill(newArr, -1L);
                     System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
                     setFinalField(f, dotonItem, newArr);
                     System.out.println("[InfTsuk] DotonPatcher: Extended cooldown map from " + oldArr.length + " to " + newSize);
                  }
               }

               if (val instanceof int[]) {
                  int[] oldArr = (int[])val;
                  if (oldArr.length < newSize) {
                     int[] newArr = new int[newSize];
                     System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
                     setFinalField(f, dotonItem, newArr);
                     System.out.println("[InfTsuk] DotonPatcher: Extended XP map from " + oldArr.length + " to " + newSize);
                  }
               }
            }

            System.out.println("[InfTsuk] DotonPatcher: Successfully injected Weighted Boulder, Earth Spear, and Mole Hide into Earth Release!");
         } catch (Exception e) {
            System.err.println("[InfTsuk] DotonPatcher: Injection failed: " + e.getMessage());
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
