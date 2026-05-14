
package net.luck.narutoaddon.OtherCode;

import net.luck.narutoaddon.OtherCode.item.ItemScrollofMoltenResistance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ElementsInfTsukAddon.ModElement.Tag
public class ScrollofMoltenResistanceHandler extends ElementsInfTsukAddon.ModElement {
   private static final boolean DEBUG_MODE = false;

   public ScrollofMoltenResistanceHandler(ElementsInfTsukAddon instance) {
      super(instance, 53);
   }

   public void init(FMLInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new ScrollEventHandler());
   }

   public static class ScrollEventHandler {
      @SubscribeEvent(
         priority = EventPriority.HIGH
      )
      public void onLivingHurt(LivingHurtEvent event) {
         if (event.getEntityLiving() instanceof EntityPlayer) {
            if (!event.isCanceled()) {
               EntityPlayer player = (EntityPlayer)event.getEntityLiving();
               ItemStack offhand = player.getHeldItemOffhand();
               if (!offhand.isEmpty()) {
                  if (offhand.getItem() instanceof ItemScrollofMoltenResistance.ItemCustom) {
                     String damageType = event.getSource().getDamageType();
                     if (this.isLavaDamage(event)) {
                        float originalDamage = event.getAmount();
                        float multiplier = ItemScrollofMoltenResistance.ItemCustom.getDamageMultiplier(offhand);
                        float newDamage = originalDamage * multiplier;
                        int tier = offhand.getMetadata() + 1;
                        int reductionPercent = tier * 10;
                        event.setAmount(newDamage);
                     }

                  }
               }
            }
         }
      }

      private boolean isLavaDamage(LivingHurtEvent event) {
         String damageType = event.getSource().getDamageType();
         if (damageType != null) {
            String lower = damageType.toLowerCase();
            if (lower.contains("melt") || lower.contains("magma") || lower.contains("quicklime") || lower.contains("lava") || lower.contains("yoton") || lower.contains("yooton")) {
               return true;
            }
         }

         if (event.getSource().getImmediateSource() != null) {
            try {
               String fullClassName = event.getSource().getImmediateSource().getClass().getName();
               if (fullClassName != null && this.matchesLavaEntity(fullClassName)) {
                  return true;
               }

               int lastDot = fullClassName != null ? fullClassName.lastIndexOf(46) : -1;
               String simpleClassName = lastDot >= 0 ? fullClassName.substring(lastDot + 1) : fullClassName;
               if (simpleClassName != null && this.matchesLavaEntity(simpleClassName)) {
                  return true;
               }
            } catch (Exception var6) {
            }
         }

         return false;
      }

      private boolean matchesLavaEntity(String name) {
         if (name == null) {
            return false;
         } else {
            String lower = name.toLowerCase();
            if (!lower.contains("magmaball") && !lower.contains("meltingjutsu") && !lower.contains("lavachakramode") && !lower.contains("quicklime") && !lower.contains("yooton") && !lower.contains("yoton")) {
               if (!lower.contains("melt") && !lower.contains("magma") && !lower.contains("lava") && !lower.contains("molten") && !lower.contains("volcanic")) {
                  return lower.contains("narutomod") && (lower.contains("magma") || lower.contains("melt") || lower.contains("lava") || lower.contains("quicklime"));
               } else {
                  return true;
               }
            } else {
               return true;
            }
         }
      }
   }
}
