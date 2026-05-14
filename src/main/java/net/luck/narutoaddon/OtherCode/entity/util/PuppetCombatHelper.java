
package net.luck.narutoaddon.OtherCode.entity.util;

import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.minecraft.entity.Entity;

public class PuppetCombatHelper {
   public static boolean shouldBlockFriendlyFire(Entity attacker) {
      if (attacker == null) {
         return false;
      } else if (attacker instanceof QuestNpcBase) {
         return true;
      } else if (attacker.getEntityData().getBoolean("sasoriPuppet")) {
         return true;
      } else {
         String cls = attacker.getClass().getName();
         return cls.contains("EntityPuppetKazekage") || cls.contains("EntityPuppetHundred") || cls.contains("EntityQuestPuppet");
      }
   }
}
