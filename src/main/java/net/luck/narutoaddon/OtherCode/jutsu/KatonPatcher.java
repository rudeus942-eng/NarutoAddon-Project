
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

public class KatonPatcher {
   public static final ItemJutsu.JutsuEnum PHOENIX_SAGE_FIRE = new ItemJutsu.JutsuEnum(6, "entity.phoenix_sage_fire.name", 'B', (double)40.0F, new EntityPhoenixSageFire.Jutsu());
   public static final ItemJutsu.JutsuEnum FIRE_DRAGON = new ItemJutsu.JutsuEnum(7, "entity.katon_fire_dragon.name", 'B', (double)75.0F, new EntityKatonFireDragon.Jutsu());
   public static final ItemJutsu.JutsuEnum ASH_PILE_BURNING = new ItemJutsu.JutsuEnum(8, "entity.katon_ash_cloud.name", 'B', (double)60.0F, new EntityKatonAshCloud.Jutsu());
   private static boolean patched = false;

   public static void inject() {
      if (!patched) {
         patched = true;

         try {
            Item katonItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("narutomod", "katon"));
            if (katonItem == null) {
               System.err.println("[InfTsuk] KatonPatcher: narutomod:katon item not found");
               return;
            }

            Class<?> baseClass;
            for(baseClass = katonItem.getClass().getSuperclass(); baseClass != null && !baseClass.getName().contains("ItemJutsu$Base"); baseClass = baseClass.getSuperclass()) {
            }

            if (baseClass == null) {
               System.err.println("[InfTsuk] KatonPatcher: Could not find ItemJutsu.Base in hierarchy");
               return;
            }

            Field jutsuListField = null;

            for(Field f : baseClass.getDeclaredFields()) {
               f.setAccessible(true);
               if (ImmutableList.class.isAssignableFrom(f.getType())) {
                  Object val = f.get(katonItem);
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
               System.err.println("[InfTsuk] KatonPatcher: Could not find jutsuList field");
               return;
            }

            jutsuListField.setAccessible(true);
            ImmutableList<?> currentList = (ImmutableList)jutsuListField.get(katonItem);
            List<Object> newList = new ArrayList();
            UnmodifiableIterator var17 = currentList.iterator();

            while(var17.hasNext()) {
               Object obj = var17.next();
               newList.add(obj);
            }

            newList.add(PHOENIX_SAGE_FIRE);
            newList.add(FIRE_DRAGON);
            newList.add(ASH_PILE_BURNING);
            ImmutableList<Object> newImmutableList = ImmutableList.copyOf(newList);
            setFinalField(jutsuListField, katonItem, newImmutableList);
            int newSize = newList.size();

            for(Field f : baseClass.getDeclaredFields()) {
               f.setAccessible(true);
               Object val = f.get(katonItem);
               if (val instanceof long[]) {
                  long[] oldArr = (long[])val;
                  if (oldArr.length < newSize) {
                     long[] newArr = new long[newSize];
                     Arrays.fill(newArr, -1L);
                     System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
                     setFinalField(f, katonItem, newArr);
                     System.out.println("[InfTsuk] KatonPatcher: Extended cooldown map from " + oldArr.length + " to " + newSize);
                  }
               }

               if (val instanceof int[]) {
                  int[] oldArr = (int[])val;
                  if (oldArr.length < newSize) {
                     int[] newArr = new int[newSize];
                     System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
                     setFinalField(f, katonItem, newArr);
                     System.out.println("[InfTsuk] KatonPatcher: Extended XP map from " + oldArr.length + " to " + newSize);
                  }
               }
            }

            System.out.println("[InfTsuk] KatonPatcher: Successfully injected Phoenix Sage Fire, Fire Dragon, and Ash Pile Burning into Fire Release!");
         } catch (Exception e) {
            System.err.println("[InfTsuk] KatonPatcher: Injection failed: " + e.getMessage());
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
