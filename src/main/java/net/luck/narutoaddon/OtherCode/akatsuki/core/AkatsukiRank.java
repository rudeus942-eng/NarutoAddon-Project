package net.luck.narutoaddon.OtherCode.akatsuki.core;

public enum AkatsukiRank {
   INITIATE(0, "Initiate"),
   OPERATIVE(1000, "Operative"),
   LIEUTENANT(3000, "Lieutenant"),
   CAPTAIN(6000, "Captain"),
   INNER_CIRCLE(10000, "Inner Circle");

   public final int repRequired;
   public final String displayName;

   private AkatsukiRank(int repRequired, String displayName) {
      this.repRequired = repRequired;
      this.displayName = displayName;
   }

   public static AkatsukiRank fromReputation(int reputation) {
      AkatsukiRank result = INITIATE;

      for(AkatsukiRank rank : values()) {
         if (reputation >= rank.repRequired) {
            result = rank;
         }
      }

      return result;
   }

   public AkatsukiRank next() {
      int idx = this.ordinal() + 1;
      return idx < values().length ? values()[idx] : this;
   }

   public int repToNext() {
      AkatsukiRank next = this.next();
      return next == this ? 0 : next.repRequired - this.repRequired;
   }
}
