package net.luck.narutoaddon.OtherCode.shop.core;

public enum ItemRarity {
   D_RANK(0, "D-Rank", -5197648),
   C_RANK(1, "C-Rank", -11141291),
   B_RANK(2, "B-Rank", -11167233),
   A_RANK(3, "A-Rank", -5635841),
   S_RANK(4, "S-Rank", -22016),
   S_PLUS_RANK(5, "S+-Rank", -43691),
   OTSUTSUKI_RANK(6, "Otsutsuki", -2051841);

   public final int id;
   public final String displayName;
   public final int color;

   private ItemRarity(int id, String displayName, int color) {
      this.id = id;
      this.displayName = displayName;
      this.color = color;
   }

   public static ItemRarity fromId(int id) {
      ItemRarity[] values = values();

      for(ItemRarity r : values) {
         if (r.id == id) {
            return r;
         }
      }

      return D_RANK;
   }

   public static ItemRarity fromLegacyOrdinal(int oldOrdinal) {
      switch (oldOrdinal) {
         case 0:
            return D_RANK;
         case 1:
            return C_RANK;
         case 2:
            return B_RANK;
         case 3:
            return A_RANK;
         case 4:
            return S_RANK;
         default:
            return D_RANK;
      }
   }

   public static ItemRarity fromOrdinal(int ordinal) {
      return fromId(ordinal);
   }
}
