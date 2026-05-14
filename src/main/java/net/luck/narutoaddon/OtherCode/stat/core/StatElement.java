package net.luck.narutoaddon.OtherCode.stat.core;

import javax.annotation.Nullable;

public enum StatElement {
   KATON(0, "Fire", StatCategory.NATURE_OFFENSE, StatCategory.NATURE_DEFENSE, true),
   FUTON(1, "Wind", StatCategory.NATURE_OFFENSE, StatCategory.NATURE_DEFENSE, true),
   SUITON(2, "Water", StatCategory.NATURE_OFFENSE, StatCategory.NATURE_DEFENSE, true),
   RAITON(3, "Lightning", StatCategory.NATURE_OFFENSE, StatCategory.NATURE_DEFENSE, true),
   DOTON(4, "Earth", StatCategory.NATURE_OFFENSE, StatCategory.NATURE_DEFENSE, true),
   MOKUTON(10, "Wood", StatCategory.KG_OFFENSE, StatCategory.KG_DEFENSE, false),
   HYOTON(11, "Ice", StatCategory.KG_OFFENSE, StatCategory.KG_DEFENSE, false),
   RANTON(13, "Storm", StatCategory.KG_OFFENSE, StatCategory.KG_DEFENSE, false),
   YOTON(15, "Lava", StatCategory.KG_OFFENSE, StatCategory.KG_DEFENSE, false),
   FUTTON(16, "Boil", StatCategory.KG_OFFENSE, StatCategory.KG_DEFENSE, false),
   SHIKOTSUMYAKU(18, "Bone", StatCategory.KG_OFFENSE, StatCategory.KG_DEFENSE, false),
   BAKUTON(19, "Explosion", StatCategory.KG_OFFENSE, StatCategory.KG_DEFENSE, false),
   SHARINGAN(20, "Sharingan", StatCategory.KG_OFFENSE, StatCategory.KG_DEFENSE, false),
   BYAKUGAN(21, "Byakugan", StatCategory.KG_OFFENSE, StatCategory.KG_DEFENSE, false),
   TAIJUTSU(40, "Taijutsu", (StatCategory)null, (StatCategory)null, false),
   GENERIC_NINJUTSU(99, "Ninjutsu", (StatCategory)null, (StatCategory)null, false);

   private final int id;
   private final String displayName;
   private final StatCategory offenseCategory;
   private final StatCategory defenseCategory;
   private final boolean baseNature;
   private static final StatElement[] VALUES = values();

   private StatElement(int id, String displayName, @Nullable StatCategory offenseCategory, @Nullable StatCategory defenseCategory, boolean baseNature) {
      this.id = id;
      this.displayName = displayName;
      this.offenseCategory = offenseCategory;
      this.defenseCategory = defenseCategory;
      this.baseNature = baseNature;
   }

   public int getId() {
      return this.id;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public StatCategory getOffenseCategory() {
      return this.offenseCategory;
   }

   public StatCategory getDefenseCategory() {
      return this.defenseCategory;
   }

   public boolean isBaseNature() {
      return this.baseNature;
   }

   public boolean isKG() {
      return this.id >= 10 && this.id <= 23;
   }

   public boolean isDojutsu() {
      return this.id >= 20 && this.id <= 23;
   }

   public boolean hasAdvantageOver(StatElement other) {
      if (this.baseNature && other.baseNature) {
         switch (this) {
            case KATON:
               return other == FUTON;
            case FUTON:
               return other == RAITON;
            case RAITON:
               return other == DOTON;
            case DOTON:
               return other == SUITON;
            case SUITON:
               return other == KATON;
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public boolean hasDisadvantageAgainst(StatElement other) {
      return other.hasAdvantageOver(this);
   }

   public static StatElement fromId(int id) {
      for(StatElement el : VALUES) {
         if (el.id == id) {
            return el;
         }
      }

      return null;
   }

   public static StatElement[] getElementsForCategory(StatCategory category) {
      switch (category) {
         case NATURE_OFFENSE:
         case NATURE_DEFENSE:
            return new StatElement[]{KATON, FUTON, SUITON, RAITON, DOTON};
         case KG_OFFENSE:
         case KG_DEFENSE:
            return new StatElement[]{MOKUTON, HYOTON, RANTON, YOTON, FUTTON, SHIKOTSUMYAKU, BAKUTON, SHARINGAN, BYAKUGAN};
         default:
            return new StatElement[0];
      }
   }
}
