
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpSavedData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;

public class LuckPermsSync {
   private static final Map<String, VillageHelper.Village> KAGE_GROUPS = new HashMap();
   private static final Map<String, VillageHelper.Village> ADVISOR_GROUPS = new HashMap();
   private static String lastError;

   public static String syncAllOnlinePlayers(MinecraftServer server) {
      if (!isLuckPermsAvailable()) {
         return TextFormatting.RED + "LuckPerms not detected. " + (lastError != null ? lastError : "No API class found.");
      } else {
         int kagesSet = 0;
         int advisorsSet = 0;
         int kagesRemoved = 0;
         int advisorsRemoved = 0;
         StringBuilder details = new StringBuilder();
         KageManager kageManager = KageManager.getInstance();
         AdvisorManager advisorManager = AdvisorManager.getInstance();

         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            SyncResult result = syncPlayer(player);
            if (result != null) {
               if (result.kageVillage != null) {
                  VillageHelper.Village existing = kageManager.getKageVillage(player.getUniqueID());
                  if (existing != result.kageVillage) {
                     kageManager.setKageDirect(result.kageVillage, player.getUniqueID());
                     ++kagesSet;
                     details.append("\n  ").append(TextFormatting.YELLOW).append(player.getName()).append(TextFormatting.RESET).append(" -> Kage of ").append(result.kageVillage.villageName);
                  }
               }

               if (result.advisorVillage != null) {
                  VillageHelper.Village existing = advisorManager.getAdvisorVillage(player.getUniqueID());
                  if (existing != result.advisorVillage) {
                     advisorManager.addAdvisorDirect(result.advisorVillage, player.getUniqueID());
                     ++advisorsSet;
                     details.append("\n  ").append(TextFormatting.YELLOW).append(player.getName()).append(TextFormatting.RESET).append(" -> Advisor of ").append(result.advisorVillage.villageName);
                  }
               }

               if (result.kageVillage == null && kageManager.isKage(player.getUniqueID())) {
                  VillageHelper.Village oldVillage = kageManager.getKageVillage(player.getUniqueID());
                  kageManager.removeKage(oldVillage);
                  ++kagesRemoved;
                  details.append("\n  ").append(TextFormatting.YELLOW).append(player.getName()).append(TextFormatting.RESET).append(" -> Kage removed (no LP group)");
               }

               if (result.advisorVillage == null && advisorManager.isAdvisor(player.getUniqueID())) {
                  VillageHelper.Village oldVillage = advisorManager.getAdvisorVillage(player.getUniqueID());
                  if (oldVillage != null) {
                     advisorManager.removeAdvisor(oldVillage, player.getUniqueID());
                     ++advisorsRemoved;
                     details.append("\n  ").append(TextFormatting.YELLOW).append(player.getName()).append(TextFormatting.RESET).append(" -> Advisor removed (no LP group)");
                  }
               }
            }
         }

         if (kagesSet + advisorsSet + kagesRemoved + advisorsRemoved > 0) {
            PvpSavedData.get(server.getWorld(0)).markDirty();
         }

         String summary = TextFormatting.GREEN + "LP Sync complete: " + TextFormatting.WHITE + kagesSet + " kages set, " + advisorsSet + " advisors set, " + kagesRemoved + " kages removed, " + advisorsRemoved + " advisors removed.";
         return summary + details.toString();
      }
   }

   public static void syncOnLogin(EntityPlayerMP player) {
      if (isLuckPermsAvailable()) {
         SyncResult result = syncPlayer(player);
         if (result != null) {
            KageManager kageManager = KageManager.getInstance();
            AdvisorManager advisorManager = AdvisorManager.getInstance();
            boolean changed = false;
            if (result.kageVillage != null) {
               VillageHelper.Village existing = kageManager.getKageVillage(player.getUniqueID());
               if (existing != result.kageVillage) {
                  kageManager.setKageDirect(result.kageVillage, player.getUniqueID());
                  changed = true;
               }
            } else if (kageManager.isKage(player.getUniqueID())) {
               VillageHelper.Village oldVillage = kageManager.getKageVillage(player.getUniqueID());
               kageManager.removeKage(oldVillage);
               changed = true;
            }

            if (result.advisorVillage != null) {
               VillageHelper.Village existing = advisorManager.getAdvisorVillage(player.getUniqueID());
               if (existing != result.advisorVillage) {
                  advisorManager.addAdvisorDirect(result.advisorVillage, player.getUniqueID());
                  changed = true;
               }
            } else if (advisorManager.isAdvisor(player.getUniqueID())) {
               VillageHelper.Village oldVillage = advisorManager.getAdvisorVillage(player.getUniqueID());
               if (oldVillage != null) {
                  advisorManager.removeAdvisor(oldVillage, player.getUniqueID());
                  changed = true;
               }
            }

            if (changed) {
               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               if (server != null) {
                  PvpSavedData.get(server.getWorld(0)).markDirty();
               }
            }

         }
      }
   }

   @Nullable
   private static SyncResult syncPlayer(EntityPlayerMP player) {
      try {
         Collection<String> groupNames = getPlayerGroupNames(player.getUniqueID());
         if (groupNames != null && !groupNames.isEmpty()) {
            VillageHelper.Village kageVillage = null;
            VillageHelper.Village advisorVillage = null;

            for(String groupName : groupNames) {
               String lower = groupName.toLowerCase();
               VillageHelper.Village kage = (VillageHelper.Village)KAGE_GROUPS.get(lower);
               if (kage != null) {
                  kageVillage = kage;
               } else {
                  VillageHelper.Village advisor = (VillageHelper.Village)ADVISOR_GROUPS.get(lower);
                  if (advisor != null) {
                     advisorVillage = advisor;
                  }
               }
            }

            return new SyncResult(kageVillage, advisorVillage);
         } else {
            return new SyncResult((VillageHelper.Village)null, (VillageHelper.Village)null);
         }
      } catch (Exception var9) {
         return null;
      }
   }

   @Nullable
   private static Class<?> tryLoadClass(String className) {
      try {
         return Class.forName(className);
      } catch (ClassNotFoundException var4) {
         try {
            ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            if (tcl != null) {
               return Class.forName(className, true, tcl);
            }
         } catch (ClassNotFoundException var3) {
         }

         try {
            ClassLoader scl = ClassLoader.getSystemClassLoader();
            return Class.forName(className, true, scl);
         } catch (ClassNotFoundException var2) {
            return null;
         }
      }
   }

   private static boolean isLuckPermsAvailable() {
      String[] classNames = new String[]{"net.luckperms.api.LuckPermsProvider", "me.lucko.luckperms.api.LuckPermsProvider", "me.lucko.luckperms.LuckPerms", "me.lucko.luckperms.api.LuckPerms"};

      for(String className : classNames) {
         Class<?> c = tryLoadClass(className);
         if (c != null) {
            lastError = null;
            return true;
         }
      }

      lastError = "Tried " + classNames.length + " LP API classes across 3 classloaders — none found.";
      return false;
   }

   @Nullable
   private static Collection<String> getPlayerGroupNames(UUID playerId) {
      try {
         Class<?> providerClass = tryLoadClass("net.luckperms.api.LuckPermsProvider");
         if (providerClass != null) {
            Method getMethod = providerClass.getMethod("get");
            Object luckPerms = getMethod.invoke((Object)null);
            Object userManager = callMethod(luckPerms, "getUserManager");
            if (userManager == null) {
               lastError = "LP getUserManager() returned null";
               return null;
            }

            Object user = callMethod(userManager, "getUser", new Class[]{UUID.class}, playerId);
            if (user == null) {
               Object future = callMethod(userManager, "loadUser", new Class[]{UUID.class}, playerId);
               if (future != null) {
                  user = callMethod(future, "join");
               }
            }

            if (user == null) {
               return null;
            }

            Object nodesObj = callMethod(user, "getNodes");
            if (!(nodesObj instanceof Collection)) {
               return null;
            }

            Class<?> inheritanceNodeClass = tryLoadClass("net.luckperms.api.node.types.InheritanceNode");
            if (inheritanceNodeClass == null) {
               Object primaryGroup = callMethod(user, "getPrimaryGroup");
               if (primaryGroup instanceof String) {
                  List<String> result = new ArrayList();
                  result.add((String)primaryGroup);
                  return result;
               }

               return null;
            }

            Collection<?> nodes = (Collection)nodesObj;
            List<String> groupNames = new ArrayList();

            for(Object node : nodes) {
               if (inheritanceNodeClass.isInstance(node)) {
                  Object gName = callMethod(node, "getGroupName");
                  if (gName instanceof String) {
                     groupNames.add((String)gName);
                  }
               }
            }

            Object primaryGroup = callMethod(user, "getPrimaryGroup");
            if (primaryGroup instanceof String && !groupNames.contains(primaryGroup)) {
               groupNames.add((String)primaryGroup);
            }

            return groupNames;
         }
      } catch (Exception e) {
         lastError = "Modern LP API error: " + e.getClass().getSimpleName() + " - " + e.getMessage();
      }

      try {
         Class<?> providerClass = tryLoadClass("me.lucko.luckperms.LuckPerms");
         if (providerClass != null) {
            Method getApi = providerClass.getMethod("getApi");
            Object api = getApi.invoke((Object)null);
            Object user = callMethod(api, "getUser", new Class[]{UUID.class}, playerId);
            if (user == null) {
               return null;
            }

            Object result = callMethod(user, "getGroupNames");
            if (result instanceof Collection) {
               return (Collection)result;
            }
         }
      } catch (Exception e) {
         lastError = "Legacy LP API error: " + e.getClass().getSimpleName() + " - " + e.getMessage();
      }

      return null;
   }

   @Nullable
   private static Object callMethod(Object target, String methodName) {
      return callMethod(target, methodName, new Class[0]);
   }

   @Nullable
   private static Object callMethod(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
      try {
         Class<?> c;
         try {
            Method m = target.getClass().getMethod(methodName, paramTypes);
            m.setAccessible(true);
            return m.invoke(target, args);
         } catch (NoSuchMethodException var8) {
            c = target.getClass();
         }

         while(c != null) {
            try {
               Method m = c.getDeclaredMethod(methodName, paramTypes);
               m.setAccessible(true);
               return m.invoke(target, args);
            } catch (NoSuchMethodException var9) {
               c = c.getSuperclass();
            }
         }

         for(Class<?> iface : getAllInterfaces(target.getClass())) {
            try {
               Method m = iface.getMethod(methodName, paramTypes);
               m.setAccessible(true);
               return m.invoke(target, args);
            } catch (NoSuchMethodException var10) {
            }
         }
      } catch (Exception var11) {
      }

      return null;
   }

   private static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
      Set<Class<?>> interfaces = new LinkedHashSet();

      for(Class<?> c = clazz; c != null; c = c.getSuperclass()) {
         for(Class<?> iface : c.getInterfaces()) {
            interfaces.add(iface);

            for(Class<?> superIface : iface.getInterfaces()) {
               interfaces.add(superIface);
            }
         }
      }

      return interfaces;
   }

   static {
      KAGE_GROUPS.put("hokage", VillageHelper.Village.LEAF);
      KAGE_GROUPS.put("kazekage", VillageHelper.Village.SAND);
      KAGE_GROUPS.put("mizukage", VillageHelper.Village.MIST);
      KAGE_GROUPS.put("tsuchikage", VillageHelper.Village.STONE);
      KAGE_GROUPS.put("raikage", VillageHelper.Village.CLOUD);
      KAGE_GROUPS.put("amekage", VillageHelper.Village.RAIN);
      ADVISOR_GROUPS.put("leafadvisor", VillageHelper.Village.LEAF);
      ADVISOR_GROUPS.put("sandadvisor", VillageHelper.Village.SAND);
      ADVISOR_GROUPS.put("mistadvisor", VillageHelper.Village.MIST);
      ADVISOR_GROUPS.put("stoneadvisor", VillageHelper.Village.STONE);
      ADVISOR_GROUPS.put("cloudadvisor", VillageHelper.Village.CLOUD);
      ADVISOR_GROUPS.put("rainadvisor", VillageHelper.Village.RAIN);
      lastError = null;
   }

   private static class SyncResult {
      final VillageHelper.Village kageVillage;
      final VillageHelper.Village advisorVillage;

      SyncResult(VillageHelper.Village kageVillage, VillageHelper.Village advisorVillage) {
         this.kageVillage = kageVillage;
         this.advisorVillage = advisorVillage;
      }
   }
}
