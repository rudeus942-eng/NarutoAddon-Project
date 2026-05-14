package net.luck.narutoaddon.OtherCode.shop.core;

public enum ItemCategory {
   DOJUTSU("Dojutsu/KG", -43691),
   NATURE("Nature Release", -11141121),
   SCROLL("Jutsu Scroll", -11141291),
   SPECIAL("Special", -22016),
   ARMOR("Armor", -5592406);

   public final String displayName;
   public final int color;

   private ItemCategory(String displayName, int color) {
      this.displayName = displayName;
      this.color = color;
   }

   public static ItemCategory fromName(String name) {
      if (name == null) {
         return null;
      } else {
         for(ItemCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(name)) {
               return cat;
            }
         }

         return null;
      }
   }
}
