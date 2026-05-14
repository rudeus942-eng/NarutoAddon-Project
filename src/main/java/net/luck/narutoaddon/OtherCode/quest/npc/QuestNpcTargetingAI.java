
package net.luck.narutoaddon.OtherCode.quest.npc;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;

import java.util.List;

public class QuestNpcTargetingAI<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {
   public QuestNpcTargetingAI(EntityCreature creature, Class<T> classTarget, boolean checkSight) {
      super(creature, classTarget, checkSight);
   }

   private boolean isFriendlyToDefender(EntityPlayer player) {
      if (!this.taskOwner.getEntityData().getBoolean("territoryDefender")) {
         return false;
      } else {
         String defenderVillage = this.taskOwner.getEntityData().getString("defenderVillage");
         if (defenderVillage != null && !defenderVillage.isEmpty()) {
            Team team = player.getTeam();
            return team == null ? false : team.getName().toLowerCase().contains(defenderVillage.toLowerCase());
         } else {
            return false;
         }
      }
   }

   public boolean shouldExecute() {
      if (super.shouldExecute()) {
         if (!(this.targetEntity instanceof EntityPlayer) || !this.isFriendlyToDefender((EntityPlayer)this.targetEntity)) {
            return true;
         }

         this.targetEntity = null;
      }

      double range = this.getTargetDistance();
      List<T> list = this.taskOwner.world.getEntitiesWithinAABB(this.targetClass, this.taskOwner.getEntityBoundingBox().grow(range, (double)4.0F, range), (entity) -> {
         if (entity != null && entity.isEntityAlive()) {
            if (entity instanceof EntityPlayer) {
               EntityPlayer p = (EntityPlayer)entity;
               if (p.isSpectator() || p.isCreative()) {
                  return false;
               }

               if (this.isFriendlyToDefender(p)) {
                  return false;
               }
            }

            if (this.shouldCheckSight && !this.taskOwner.getEntitySenses().canSee(entity)) {
               return false;
            } else {
               return (double)this.taskOwner.getDistance(entity) <= range;
            }
         } else {
            return false;
         }
      });
      if (list.isEmpty()) {
         return false;
      } else {
         list.sort((a, b) -> Double.compare(a.getDistanceSq(this.taskOwner), b.getDistanceSq(this.taskOwner)));
         this.targetEntity = (EntityLivingBase)list.get(0);
         return true;
      }
   }

   public boolean shouldContinueExecuting() {
      if (this.targetEntity instanceof EntityPlayer && this.isFriendlyToDefender((EntityPlayer)this.targetEntity)) {
         this.targetEntity = null;
         return false;
      } else {
         return super.shouldContinueExecuting();
      }
   }
}
