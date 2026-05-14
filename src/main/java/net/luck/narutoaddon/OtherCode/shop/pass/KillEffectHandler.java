
package net.luck.narutoaddon.OtherCode.shop.pass;

import net.luck.narutoaddon.OtherCode.jutsu.EntityKillEffectInferno;
import net.luck.narutoaddon.OtherCode.jutsu.EntityKillEffectLightning;
import net.luck.narutoaddon.OtherCode.jutsu.EntityKillEffectShadow;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class KillEffectHandler {
   @SubscribeEvent
   public void onPlayerDeath(LivingDeathEvent event) {
      if (event.getEntityLiving() instanceof EntityPlayerMP) {
         if (event.getSource().getTrueSource() instanceof EntityPlayerMP) {
            EntityPlayerMP victim = (EntityPlayerMP)event.getEntityLiving();
            EntityPlayerMP killer = (EntityPlayerMP)event.getSource().getTrueSource();
            if (!killer.getUniqueID().equals(victim.getUniqueID())) {
               String activeEffectId;
               try {
                  ShopSavedData data = ShopSavedData.get(killer.world);
                  activeEffectId = data.getActiveKillEffect(killer.getUniqueID());
               } catch (Exception var15) {
                  return;
               }

               if (activeEffectId != null && !activeEffectId.isEmpty()) {
                  KillEffectRegistry.KillEffect effect = KillEffectRegistry.getEffect(activeEffectId);
                  if (effect != null) {
                     double x = victim.posX;
                     double y = victim.posY;
                     double z = victim.posZ;
                     if ("s1_fire_burst".equals(activeEffectId)) {
                        EntityKillEffectInferno.EntityCustom killEffect = new EntityKillEffectInferno.EntityCustom(victim.world);
                        killEffect.setPosition(x, y, z);
                        victim.world.spawnEntity(killEffect);
                     } else if ("s2_lightning_strike".equals(activeEffectId)) {
                        EntityKillEffectLightning.EntityCustom killEffect = new EntityKillEffectLightning.EntityCustom(victim.world);
                        killEffect.setPosition(x, y, z);
                        victim.world.spawnEntity(killEffect);
                     } else if ("s3_shadow_dissolve".equals(activeEffectId)) {
                        EntityKillEffectShadow.EntityCustom killEffect = new EntityKillEffectShadow.EntityCustom(victim.world);
                        killEffect.setPosition(x, y, z);
                        victim.world.spawnEntity(killEffect);
                     } else {
                        if (victim.world instanceof WorldServer) {
                           WorldServer worldServer = (WorldServer)victim.world;
                           double spread = effect.getParticleSpread();
                           worldServer.spawnParticle(effect.getParticleType(), x, y + (double)1.0F, z, effect.getParticleCount(), spread, spread * (double)0.5F, spread, 0.05, new int[0]);
                        }

                        victim.world.playSound((EntityPlayer)null, victim.posX, victim.posY, victim.posZ, effect.getSoundEvent(), SoundCategory.PLAYERS, effect.getSoundVolume(), effect.getSoundPitch());
                     }
                  }
               }
            }
         }
      }
   }
}
