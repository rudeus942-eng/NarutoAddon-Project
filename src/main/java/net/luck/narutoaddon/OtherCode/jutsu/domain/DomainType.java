package net.luck.narutoaddon.OtherCode.jutsu.domain;

public enum DomainType {
   INFINITE_VOID("infinite_void", "Unlimited Void");

   private final String id;
   private final String displayName;

   private DomainType(String id, String displayName) {
      this.id = id;
      this.displayName = displayName;
   }

   public String getId() {
      return this.id;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public static DomainType fromId(String id) {
      for(DomainType t : values()) {
         if (t.id.equals(id)) {
            return t;
         }
      }

      return INFINITE_VOID;
   }
}
