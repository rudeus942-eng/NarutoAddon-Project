package net.luck.narutoaddon.OtherCode.quest.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class KGRollClientData {
   public static KGRollResult pendingResult = null;

   public static class StripEntry {
      public final String itemId;
      public final String displayName;
      public final int rarityIndex;

      public StripEntry(String itemId, String displayName, int rarityIndex) {
         this.itemId = itemId;
         this.displayName = displayName;
         this.rarityIndex = rarityIndex;
      }
   }

   public static class KGRollResult {
      public final String wonItemId;
      public final String wonDisplayName;
      public final int wonRarity;
      public final List<StripEntry> displayStrip;
      public final int targetIndex;
      public boolean consumed = false;

      public KGRollResult(String wonItemId, String wonDisplayName, int wonRarity, List<StripEntry> displayStrip, int targetIndex) {
         this.wonItemId = wonItemId;
         this.wonDisplayName = wonDisplayName;
         this.wonRarity = wonRarity;
         this.displayStrip = displayStrip;
         this.targetIndex = targetIndex;
      }
   }
}
