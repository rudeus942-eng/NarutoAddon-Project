package net.luck.narutoaddon.OtherCode.akatsuki.core;

public enum AkatsukiRing {
   REI("零", "Rei", "Zero", "Pain", "Right Thumb"),
   AO("青", "Ao", "Blue", "Deidara", "Right Index"),
   BYAKU("白", "Byaku", "White", "Konan", "Right Middle"),
   SHU("朱", "Shu", "Scarlet", "Itachi", "Right Ring"),
   GAI("亥", "Gai", "Boar", "Zetsu", "Right Pinkie"),
   KU("空", "Ku", "Void", "Orochimaru", "Left Pinkie"),
   NAN("南", "Nan", "South", "Kisame", "Left Ring"),
   HOKU("北", "Hoku", "North", "Kakuzu", "Left Middle"),
   SAN("三", "San", "Three", "Hidan", "Left Index"),
   GYOKU("玉", "Gyoku", "Jewel", "Sasori", "Left Thumb");

   public final String kanji;
   public final String romajiName;
   public final String englishName;
   public final String canonHolder;
   public final String fingerPosition;

   private AkatsukiRing(String kanji, String romajiName, String englishName, String canonHolder, String fingerPosition) {
      this.kanji = kanji;
      this.romajiName = romajiName;
      this.englishName = englishName;
      this.canonHolder = canonHolder;
      this.fingerPosition = fingerPosition;
   }

   public static AkatsukiRing getByIndex(int index) {
      return index >= 0 && index < values().length ? values()[index] : REI;
   }

   public static AkatsukiRing fromName(String name) {
      for(AkatsukiRing ring : values()) {
         if (ring.name().equalsIgnoreCase(name) || ring.romajiName.equalsIgnoreCase(name)) {
            return ring;
         }
      }

      return REI;
   }
}
