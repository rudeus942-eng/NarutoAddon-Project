package net.luck.narutoaddon.OtherCode.stat.core;

public enum StatCategory {
   NATURE_OFFENSE(0, "Nature Mastery"),
   KG_OFFENSE(1, "KG Mastery"),
   NATURE_DEFENSE(2, "Nature Defense"),
   KG_DEFENSE(3, "KG Defense");

   private final int id;
   private final String displayName;
   private static final StatCategory[] VALUES = values();

   private StatCategory(int id, String displayName) {
      this.id = id;
      this.displayName = displayName;
   }

   public int getId() {
      return this.id;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public boolean isOffense() {
      return this == NATURE_OFFENSE || this == KG_OFFENSE;
   }

   public boolean isDefense() {
      return this == NATURE_DEFENSE || this == KG_DEFENSE;
   }

   public boolean isKG() {
      return this == KG_OFFENSE || this == KG_DEFENSE;
   }

   public static StatCategory fromId(int id) {
      for(StatCategory cat : VALUES) {
         if (cat.id == id) {
            return cat;
         }
      }

      return null;
   }
}
