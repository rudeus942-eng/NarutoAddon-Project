
package net.luck.narutoaddon.OtherCode;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

@ElementsInfTsukAddon.ModElement.Tag
public class ProtectionNerfHandler extends ElementsInfTsukAddon.ModElement {
   private static int EPF_CAP = 16;
   private static boolean ENABLED = false;
   private static boolean DEBUG_MODE = false;
   private static Configuration config;

   public ProtectionNerfHandler(ElementsInfTsukAddon instance) {
      super(instance, 102);
   }

   public void preInit(FMLPreInitializationEvent event) {
      File configFile = new File(event.getModConfigurationDirectory(), "inftsukaddon_ranked_protection.cfg");
      config = new Configuration(configFile);
      this.loadConfig();
      MinecraftForge.EVENT_BUS.register(new ProtectionNerfEventHandler());
   }

   private void loadConfig() {
      try {
         config.load();
         ENABLED = config.getBoolean("enabled", "general", false, "Enable the Ranked Protection EPF cap system (disabled by default)");
         EPF_CAP = config.getInt("epfCap", "general", 16, 0, 20, "Maximum Protection EPF allowed in ranked matches.\n0 = Protection does nothing in ranked\n16 = Cap at 64% damage reduction (default)\n20 = No cap (vanilla behavior)");
         DEBUG_MODE = config.getBoolean("debugMode", "general", false, "Enable debug messages in server console showing protection calculations");
      } catch (Exception e) {
         System.err.println("[InfTsukAddon] Error loading Ranked Protection config: " + e.getMessage());
      } finally {
         if (config.hasChanged()) {
            config.save();
         }

      }

   }

   private static int calculateProtectionEPF(EntityPlayer player) {
      int totalEPF = 0;
      Enchantment protectionEnchant = Enchantment.getEnchantmentByID(0);
      if (protectionEnchant == null) {
         return 0;
      } else {
         for(EntityEquipmentSlot slot : new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET}) {
            ItemStack armor = player.getItemStackFromSlot(slot);
            if (!armor.isEmpty()) {
               int level = EnchantmentHelper.getEnchantmentLevel(protectionEnchant, armor);
               totalEPF += level;
            }
         }

         return Math.min(totalEPF, 20);
      }
   }

   private static boolean isInActiveRankedMatch(EntityPlayer player) {
      String uuid = player.getUniqueID().toString();
      if (!Rankedqueue.isInMatch(uuid)) {
         return false;
      } else {
         Rankedqueue.Match match = Rankedqueue.getPlayerMatch(uuid);
         return match == null ? false : "active".equals(match.state);
      }
   }

   public static int getEPFCap() {
      return EPF_CAP;
   }

   public static boolean isEnabled() {
      return ENABLED;
   }

   public static void setEPFCap(int value) {
      EPF_CAP = Math.max(0, Math.min(20, value));
      if (config != null) {
         config.get("general", "epfCap", 16).set(EPF_CAP);
         config.save();
      }

   }

   public static void setEnabled(boolean enabled) {
      ENABLED = enabled;
      if (config != null) {
         config.get("general", "enabled", true).set(ENABLED);
         config.save();
      }

   }

   public static float getReductionPercent(int epf) {
      return (float)Math.min(epf, 20) / 25.0F * 100.0F;
   }

   // $FF: synthetic method
   static boolean access$000() {
      return ENABLED;
   }

   // $FF: synthetic method
   static boolean access$100(EntityPlayer x0) {
      return isInActiveRankedMatch(x0);
   }

   // $FF: synthetic method
   static int access$200(EntityPlayer x0) {
      return calculateProtectionEPF(x0);
   }

   // $FF: synthetic method
   static int access$300() {
      return EPF_CAP;
   }

   // $FF: synthetic method
   static boolean access$400() {
      return DEBUG_MODE;
   }

   public static class ProtectionNerfEventHandler {
      @SubscribeEvent(
         priority = EventPriority.LOWEST
      )
      public void onLivingDamage(LivingDamageEvent event) {
      }
   }
}
