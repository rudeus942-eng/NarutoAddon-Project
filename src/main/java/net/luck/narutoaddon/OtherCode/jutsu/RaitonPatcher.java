
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

public class RaitonPatcher {
   public static final ItemJutsu.JutsuEnum LIGHTNING_BEAST_FANG = new ItemJutsu.JutsuEnum(7, "entity.lightning_beast_fang.name", 'B', (double)50.0F, new EntityLightningBeastFang.Jutsu());
   public static final ItemJutsu.JutsuEnum ELECTROMAGNETIC_MURDER = new ItemJutsu.JutsuEnum(8, "entity.raiton_electromagnetic_murder.name", 'B', (double)35.0F, new EntityRaitonElectromagneticMurder.Jutsu());
   public static final ItemJutsu.JutsuEnum LIGHTNING_CLONE = new ItemJutsu.JutsuEnum(9, "entity.raiton_lightning_clone.name", 'A', (double)60.0F, new EntityRaitonLightningClone.Jutsu());
   private static boolean patched = false;

   public static void inject() {
      if (!patched) {
         patched = true;

         try {
            Item raitonItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("narutomod", "raiton"));
            if (raitonItem == null) {
               System.err.println("[InfTsuk] RaitonPatcher: narutomod:raiton item not found");
               return;
            }

            Class<?> baseClass;
            for(baseClass = raitonItem.getClass().getSuperclass(); baseClass != null && !baseClass.getName().contains("ItemJutsu$Base"); baseClass = baseClass.getSuperclass()) {
            }

            if (baseClass == null) {
               System.err.println("[InfTsuk] RaitonPatcher: Could not find ItemJutsu.Base in hierarchy");
               return;
            }

            Field jutsuListField = null;

            for(Field f : baseClass.getDeclaredFields()) {
               f.setAccessible(true);
               if (ImmutableList.class.isAssignableFrom(f.getType())) {
                  Object val = f.get(raitonItem);
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
               System.err.println("[InfTsuk] RaitonPatcher: Could not find jutsuList field");
               return;
            }

            jutsuListField.setAccessible(true);
            ImmutableList<?> currentList = (ImmutableList)jutsuListField.get(raitonItem);
            List<Object> newList = new ArrayList();
            UnmodifiableIterator var17 = currentList.iterator();

            while(var17.hasNext()) {
               Object obj = var17.next();
               newList.add(obj);
            }

            newList.add(LIGHTNING_BEAST_FANG);
            newList.add(ELECTROMAGNETIC_MURDER);
            newList.add(LIGHTNING_CLONE);
            ImmutableList<Object> newImmutableList = ImmutableList.copyOf(newList);
            setFinalField(jutsuListField, raitonItem, newImmutableList);
            int newSize = newList.size();

            for(Field f : baseClass.getDeclaredFields()) {
               f.setAccessible(true);
               Object val = f.get(raitonItem);
               if (val instanceof long[]) {
                  long[] oldArr = (long[])val;
                  if (oldArr.length < newSize) {
                     long[] newArr = new long[newSize];
                     Arrays.fill(newArr, -1L);
                     System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
                     setFinalField(f, raitonItem, newArr);
                     System.out.println("[InfTsuk] RaitonPatcher: Extended cooldown map from " + oldArr.length + " to " + newSize);
                  }
               }

               if (val instanceof int[]) {
                  int[] oldArr = (int[])val;
                  if (oldArr.length < newSize) {
                     int[] newArr = new int[newSize];
                     System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
                     setFinalField(f, raitonItem, newArr);
                     System.out.println("[InfTsuk] RaitonPatcher: Extended XP map from " + oldArr.length + " to " + newSize);
                  }
               }
            }

            System.out.println("[InfTsuk] RaitonPatcher: Successfully injected Lightning Beast Tracking Fang, Electromagnetic Murder, Lightning Shadow Clone into Lightning Release!");
         } catch (Exception e) {
            System.err.println("[InfTsuk] RaitonPatcher: Injection failed: " + e.getMessage());
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
