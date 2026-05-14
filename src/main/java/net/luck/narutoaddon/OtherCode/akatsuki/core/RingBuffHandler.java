package net.luck.narutoaddon.OtherCode.akatsuki.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.narutomod.Chakra;

import java.util.UUID;

public class RingBuffHandler {
   private static final UUID RING_SPEED_MODIFIER_UUID = UUID.fromString("a3f5c8e1-7b2d-4f9a-8e6c-1d2b3a4c5e6f");
   private static final String RING_SPEED_MODIFIER_NAME = "akatsuki_ring_speed";

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public void onLivingHurt(LivingHurtEvent event) {
      if (!event.isCanceled()) {
         float damage = event.getAmount();
         AkatsukiManager manager = AkatsukiManager.getInstance();
         if (event.getSource() != null && event.getSource().getTrueSource() != null) {
            Entity source = event.getSource().getTrueSource();
            if (source instanceof EntityPlayerMP) {
               EntityPlayerMP attacker = (EntityPlayerMP)source;
               if (manager.isAkatsuki(attacker.getUniqueID())) {
                  AkatsukiMember member = manager.getMember(attacker.getUniqueID());
                  if (member != null) {
                     int[] levels = member.getRingUpgradeLevels();
                     if (levels[0] > 0) {
                        damage *= (float)((double)1.0F + (double)levels[0] * 0.03);
                     }

                     if (levels[4] > 0) {
                        damage *= (float)((double)1.0F + (double)levels[4] * 0.02);
                     }
                  }
               }
            }
         }

         if (event.getEntityLiving() instanceof EntityPlayerMP) {
            EntityPlayerMP defender = (EntityPlayerMP)event.getEntityLiving();
            if (manager.isAkatsuki(defender.getUniqueID())) {
               AkatsukiMember member = manager.getMember(defender.getUniqueID());
               if (member != null) {
                  int[] levels = member.getRingUpgradeLevels();
                  if (levels[1] > 0) {
                     damage *= (float)((double)1.0F - (double)levels[1] * 0.02);
                  }

                  if (levels[4] > 0) {
                     damage *= (float)((double)1.0F - (double)levels[4] * 0.02);
                  }

                  damage = Math.max(1.0F, damage);
               }
            }
         }

         event.setAmount(damage);
      }
   }

   @SubscribeEvent
   public void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            if (player.ticksExisted % 20 == 0) {
               AkatsukiManager manager = AkatsukiManager.getInstance();
               if (!manager.isAkatsuki(player.getUniqueID())) {
                  this.removeSpeedModifier(player);
               } else {
                  AkatsukiMember member = manager.getMember(player.getUniqueID());
                  if (member == null) {
                     this.removeSpeedModifier(player);
                  } else {
                     int[] levels = member.getRingUpgradeLevels();
                     int speedLevel = levels[2];
                     int versatilityLevel = levels[4];
                     double speedBonus = (double)speedLevel * 0.02 + (double)versatilityLevel * 0.02;
                     this.applySpeedModifier(player, speedBonus);
                     int chakraLevel = levels[3];
                     if (chakraLevel > 0 || versatilityLevel > 0) {
                        double regenBonus = (double)chakraLevel * 0.03 + (double)versatilityLevel * 0.02;
                        double bonusChakra = 0.6 * regenBonus * (double)20.0F;
                        if (bonusChakra > (double)0.0F) {
                           try {
                              Chakra.Pathway pathway = Chakra.pathway(player);
                              if (pathway != null && pathway.getAmount() < pathway.getMax()) {
                                 pathway.consume(-bonusChakra);
                              }
                           } catch (Exception var16) {
                           }
                        }
                     }

                  }
               }
            }
         }
      }
   }

   private void applySpeedModifier(EntityPlayerMP player, double speedBonus) {
      IAttributeInstance speedAttr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      if (speedAttr != null) {
         AttributeModifier existing = speedAttr.getModifier(RING_SPEED_MODIFIER_UUID);
         if (existing != null) {
            if (Math.abs(existing.getAmount() - speedBonus) < 1.0E-4) {
               return;
            }

            speedAttr.removeModifier(existing);
         }

         if (speedBonus > (double)0.0F) {
            AttributeModifier modifier = new AttributeModifier(RING_SPEED_MODIFIER_UUID, "akatsuki_ring_speed", speedBonus, 2);
            speedAttr.applyModifier(modifier);
         }

      }
   }

   private void removeSpeedModifier(EntityPlayerMP player) {
      IAttributeInstance speedAttr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      if (speedAttr != null) {
         AttributeModifier existing = speedAttr.getModifier(RING_SPEED_MODIFIER_UUID);
         if (existing != null) {
            speedAttr.removeModifier(existing);
         }

      }
   }
}
