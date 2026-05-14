
package net.luck.narutoaddon.OtherCode.quest.core;

import net.luck.narutoaddon.OtherCode.entity.EntityPainAnimalNPC;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class NpcBalanceHandler {
   private static boolean bypassingHydrification = false;

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public void onLivingHurtFuryResist(LivingHurtEvent event) {
      if (!event.isCanceled()) {
         EntityLivingBase target = event.getEntityLiving();
         if (target != null && !target.world.isRemote) {
            if (isOurNpc(target)) {
               DamageSource source = event.getSource();
               if (source != null) {
                  Entity immediate = source.getImmediateSource();
                  if (immediate != null && immediate.getClass().getName().contains("EntityFury")) {
                     event.setAmount(event.getAmount() * 0.07F);
                  }

               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            long worldTime = server.getWorld(0).getTotalWorldTime();
            if (worldTime % 10L == 0L) {
               for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                  if (hasHydrification(player)) {
                     float totalDmg = 0.0F;

                     for(Entity entity : player.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().grow((double)30.0F))) {
                        if (isOurNpc(entity) && entity instanceof EntityCreature) {
                           EntityCreature npc = (EntityCreature)entity;
                           EntityLivingBase target = npc.getAttackTarget();
                           if (target == player) {
                              double dist = (double)npc.getDistance(player);
                              float damage = (float)npc.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                              if (dist <= (double)4.0F) {
                                 totalDmg += damage * 0.3F;
                                 break;
                              }

                              if (dist <= (double)25.0F) {
                                 totalDmg += damage * 0.15F;
                                 break;
                              }
                           }
                        }
                     }

                     if (totalDmg > 0.0F) {
                        player.setHealth(player.getHealth() - totalDmg);
                        player.hurtResistantTime = 10;
                     }
                  }
               }

            }
         }
      }
   }

   private static boolean hasHydrification(EntityLivingBase target) {
      if (!target.getEntityData().hasKey("HydrificationEntityIdKey")) {
         return false;
      } else {
         int entityId = target.getEntityData().getInteger("HydrificationEntityIdKey");
         Entity hydro = target.world.getEntityByID(entityId);
         return hydro != null && hydro.isEntityAlive();
      }
   }

   private static boolean isOurNpc(Entity entity) {
      if (entity instanceof QuestNpcBase) {
         return true;
      } else if (entity instanceof EntityPainAnimalNPC.EntitySummonedBeast) {
         return true;
      } else if (entity instanceof EntityPainAnimalNPC.EntitySummonedBird) {
         return true;
      } else if (entity instanceof EntityPainAnimalNPC.EntitySummonedChameleon) {
         return true;
      } else {
         String className = entity.getClass().getName();
         return className.contains("EntityPuppetKazekage") || className.contains("EntityPuppetHundred") || className.contains("EntityQuestPuppet");
      }
   }
}
