
package net.luck.narutoaddon.OtherCode.quest.network;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class QuestCombatClientData {
   private static QuestCombatHealthMessage.EnemyHealthEntry[] enemies = null;
   private static long lastUpdateTick = 0L;
   private static final long STALE_THRESHOLD = 100L;

   public static QuestCombatHealthMessage.EnemyHealthEntry[] getEnemies() {
      QuestCombatHealthMessage.EnemyHealthEntry[] current = enemies;
      if (current == null) {
         return null;
      } else {
         long currentTick = Minecraft.getMinecraft().world != null ? Minecraft.getMinecraft().world.getTotalWorldTime() : 0L;
         if (currentTick - lastUpdateTick > 100L) {
            enemies = null;
            return null;
         } else {
            return current;
         }
      }
   }

   public static void update(QuestCombatHealthMessage.EnemyHealthEntry[] newEntries) {
      enemies = newEntries;
      if (Minecraft.getMinecraft().world != null) {
         lastUpdateTick = Minecraft.getMinecraft().world.getTotalWorldTime();
      }

   }

   public static void clear() {
      enemies = null;
      lastUpdateTick = 0L;
   }
}
