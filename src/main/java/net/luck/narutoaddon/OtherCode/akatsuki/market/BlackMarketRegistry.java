package net.luck.narutoaddon.OtherCode.akatsuki.market;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BlackMarketRegistry {
   private static final Map<String, BlackMarketItem> ITEMS = new LinkedHashMap();

   private BlackMarketRegistry() {
   }

   private static void register(BlackMarketItem item) {
      ITEMS.put(item.getId(), item);
   }

   public static BlackMarketItem get(String id) {
      return (BlackMarketItem)ITEMS.get(id);
   }

   public static List<BlackMarketItem> getAll() {
      return new ArrayList(ITEMS.values());
   }

   public static List<BlackMarketItem> getByType(BlackMarketItem.DeployType type) {
      List<BlackMarketItem> result = new ArrayList();

      for(BlackMarketItem item : ITEMS.values()) {
         if (item.getDeployType() == type) {
            result.add(item);
         }
      }

      return result;
   }

   public static int size() {
      return ITEMS.size();
   }
}
