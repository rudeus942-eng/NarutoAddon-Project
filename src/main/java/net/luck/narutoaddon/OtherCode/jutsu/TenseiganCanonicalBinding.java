package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.lang.reflect.Field;

@EventBusSubscriber(
   modid = "inftsukaddon"
)
public class TenseiganCanonicalBinding {
   private static final String AHZNB_TENSEIGAN_CLASS = "net.narutomod.item.ItemTenseigan";
   private static final String HELMET_FIELD_NAME = "helmet";
   private static Field cachedHelmetField;
   private static boolean loggedSuccessOnce = false;

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         ensureBound();
      }
   }

   private static Field getHelmetField() {
      if (cachedHelmetField != null) {
         return cachedHelmetField;
      } else {
         try {
            Class<?> ahznbTenseigan = Class.forName("net.narutomod.item.ItemTenseigan");
            Field f = ahznbTenseigan.getField("helmet");
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(f, f.getModifiers() & -17);
            cachedHelmetField = f;
            return f;
         } catch (Throwable var3) {
            return null;
         }
      }
   }

   private static void ensureBound() {
      Field helmetField = getHelmetField();
      if (helmetField != null) {
         Item ourHelmet = ItemluckAddonTenseigan.helmet;
         if (ourHelmet != null) {
            try {
               Object current = helmetField.get((Object)null);
               if (current == ourHelmet) {
                  return;
               }

               helmetField.set((Object)null, ourHelmet);
               if (!loggedSuccessOnce) {
                  System.out.println("[InfTsukAddon] Tenseigan rebind complete. ItemTenseigan.helmet now → " + ourHelmet + ". TCM + Byakugan upgrade + inventory scans will target our Tenseigan.");
                  loggedSuccessOnce = true;
               } else {
                  System.out.println("[InfTsukAddon] Tenseigan rebind was reset by another mod; re-applied.");
               }
            } catch (Throwable t) {
               System.err.println("[InfTsukAddon] Tenseigan rebind write failed: " + t);
               t.printStackTrace();
            }

         }
      }
   }
}
