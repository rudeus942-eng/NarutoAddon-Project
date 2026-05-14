
package net.luck.narutoaddon.OtherCode.quest.npc;

import net.luck.narutoaddon.OtherCode.quest.core.QuestInstance;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.quest.core.QuestStep;
import net.luck.narutoaddon.OtherCode.quest.network.QuestNetworkHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;

import java.util.Map;

public class NpcInteractionHelper {
   public static boolean handleNpcInteraction(Entity npc, EntityPlayerMP player) {
      NBTTagCompound entityData = npc.getEntityData();
      if (!entityData.getBoolean("questEntity")) {
         return false;
      } else {
         String npcConfigId = entityData.getString("npcConfigId");
         if (npcConfigId != null && !npcConfigId.isEmpty()) {
            NpcConfig config = NpcConfigRegistry.get(npcConfigId);
            if (config != null && !config.isPassive()) {
               return false;
            } else {
               Map<String, QuestInstance> activeSlots = QuestManager.getInstance().getActiveQuests(player.getUniqueID());

               for(QuestInstance quest : activeSlots.values()) {
                  QuestStep step = quest.getCurrentStep();
                  if (step != null && step.type == QuestStep.StepType.DIALOG && step.hasNpcConfig() && npcConfigId.equals(step.npcConfigId)) {
                     double dx = player.posX - npc.posX;
                     double dz = player.posZ - npc.posZ;
                     float yaw = (float)(Math.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
                     npc.rotationYaw = yaw;
                     if (npc instanceof EntityLivingBase) {
                        ((EntityLivingBase)npc).rotationYawHead = yaw;
                        ((EntityLivingBase)npc).renderYawOffset = yaw;
                     }

                     if (step.hasMultiPhaseDialog()) {
                        QuestNetworkHelper.sendMultiPhaseDialog(player, step, quest);
                     } else {
                        QuestNetworkHelper.sendDialog(player, step, quest.getCurrentStepIndex());
                     }

                     return true;
                  }
               }

               player.sendMessage(new TextComponentString("§cThis NPC is not part of your quest."));
               return true;
            }
         } else {
            return false;
         }
      }
   }
}
