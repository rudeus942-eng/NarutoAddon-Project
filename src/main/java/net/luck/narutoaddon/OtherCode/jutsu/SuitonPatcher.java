
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

public class SuitonPatcher {
   public static final ItemJutsu.JutsuEnum WATER_FANG_BULLET = new ItemJutsu.JutsuEnum(10, "entity.water_fang_bullet.name", 'B', (double)45.0F, new EntityWaterFangBullet.Jutsu());
   public static final ItemJutsu.JutsuEnum WATER_FORMATION_WALL = new ItemJutsu.JutsuEnum(11, "entity.water_formation_wall.name", 'B', (double)50.0F, new EntityWaterFormationWall.Jutsu());
   public static final ItemJutsu.JutsuEnum WATER_NEEDLES = new ItemJutsu.JutsuEnum(12, "entity.water_needles.name", 'C', (double)30.0F, new EntityWaterNeedles.Jutsu());
   private static boolean patched = false;

   public static void inject() {
      if (!patched) {
         patched = true;

         try {
            Item suitonItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("narutomod", "suiton"));
            if (suitonItem == null) {
               System.err.println("[InfTsuk] SuitonPatcher: narutomod:suiton item not found");
               return;
            }

            Class<?> baseClass;
            for(baseClass = suitonItem.getClass().getSuperclass(); baseClass != null && !baseClass.getName().contains("ItemJutsu$Base"); baseClass = baseClass.getSuperclass()) {
            }

            if (baseClass == null) {
               System.err.println("[InfTsuk] SuitonPatcher: Could not find ItemJutsu.Base in hierarchy");
               return;
            }

            Field jutsuListField = null;

            for(Field f : baseClass.getDeclaredFields()) {
               f.setAccessible(true);
               if (ImmutableList.class.isAssignableFrom(f.getType())) {
                  Object val = f.get(suitonItem);
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
               System.err.println("[InfTsuk] SuitonPatcher: Could not find jutsuList field");
               return;
            }

            jutsuListField.setAccessible(true);
            ImmutableList<?> currentList = (ImmutableList)jutsuListField.get(suitonItem);
            List<Object> newList = new ArrayList();
            UnmodifiableIterator var17 = currentList.iterator();

            while(var17.hasNext()) {
               Object obj = var17.next();
               newList.add(obj);
            }

            newList.add(WATER_FANG_BULLET);
            newList.add(WATER_FORMATION_WALL);
            newList.add(WATER_NEEDLES);
            ImmutableList<Object> newImmutableList = ImmutableList.copyOf(newList);
            setFinalField(jutsuListField, suitonItem, newImmutableList);
            int newSize = newList.size();

            for(Field f : baseClass.getDeclaredFields()) {
               f.setAccessible(true);
               Object val = f.get(suitonItem);
               if (val instanceof long[]) {
                  long[] oldArr = (long[])val;
                  if (oldArr.length < newSize) {
                     long[] newArr = new long[newSize];
                     Arrays.fill(newArr, -1L);
                     System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
                     setFinalField(f, suitonItem, newArr);
                     System.out.println("[InfTsuk] SuitonPatcher: Extended cooldown map from " + oldArr.length + " to " + newSize);
                  }
               }

               if (val instanceof int[]) {
                  int[] oldArr = (int[])val;
                  if (oldArr.length < newSize) {
                     int[] newArr = new int[newSize];
                     System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
                     setFinalField(f, suitonItem, newArr);
                     System.out.println("[InfTsuk] SuitonPatcher: Extended XP map from " + oldArr.length + " to " + newSize);
                  }
               }
            }

            System.out.println("[InfTsuk] SuitonPatcher: Successfully injected Water Fang Bullet, Water Formation Wall, and Water Needles into Water Release!");
         } catch (Exception e) {
            System.err.println("[InfTsuk] SuitonPatcher: Injection failed: " + e.getMessage());
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
