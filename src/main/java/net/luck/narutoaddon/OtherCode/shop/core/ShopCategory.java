package net.luck.narutoaddon.OtherCode.shop.core;

public enum ShopCategory {
   CONSUMABLES(0, "Supply", -12277044),
   WEAPONS(1, "Weapons", -3390396),
   ARMOR(2, "Armor", -5601246),
   JUTSU(3, "Jutsu", -12268476),
   CLANS(4, "Clans", -7846708),
   KEKKEI_GENKAI(5, "SO6P", -39424),
   TRAVEL(6, "Travel", -12277112),
   CASH(7, "Cash", -2276097);

   public final int id;
   public final String displayName;
   public final int color;

   private ShopCategory(int id, String displayName, int color) {
      this.id = id;
      this.displayName = displayName;
      this.color = color;
   }

   public static ShopCategory fromId(int id) {
      ShopCategory[] values = values();

      for(ShopCategory c : values) {
         if (c.id == id) {
            return c;
         }
      }

      return CONSUMABLES;
   }

   public static ShopCategory fromOrdinal(int ordinal) {
      return fromId(ordinal);
   }
}
