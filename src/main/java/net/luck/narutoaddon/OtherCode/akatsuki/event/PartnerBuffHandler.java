package net.luck.narutoaddon.OtherCode.akatsuki.event;

import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiMember;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PartnerBuffHandler {
   private static final double PARTNER_RANGE = (double)25.0F;
   private static final float DAMAGE_BOOST = 1.15F;
   private static final float DEFENSE_BOOST = 0.9F;
   private static final ConcurrentHashMap<UUID, Boolean> partnerNearbyCache = new ConcurrentHashMap();
   private static int cacheTick = 0;

   @SubscribeEvent
   public void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         ++cacheTick;
         if (cacheTick % 40 == 0) {
            partnerNearbyCache.clear();
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               AkatsukiManager mgr = AkatsukiManager.getInstance();
               if (mgr != null) {
                  for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                     UUID playerId = player.getUniqueID();
                     if (mgr.isAkatsuki(playerId)) {
                        AkatsukiMember member = mgr.getMember(playerId);
                        if (member != null && member.getPartnerId() != null) {
                           UUID partnerId = member.getPartnerId();
                           EntityPlayerMP partner = server.getPlayerList().getPlayerByUUID(partnerId);
                           if (partner != null && partner.dimension == player.dimension && (double)partner.getDistance(player) <= (double)25.0F) {
                              partnerNearbyCache.put(playerId, Boolean.TRUE);
                           }
                        }
                     }
                  }

               }
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public void onLivingHurt(LivingHurtEvent event) {
      if (!event.isCanceled()) {
         if (event.getSource().getTrueSource() instanceof EntityPlayerMP) {
            EntityPlayerMP attacker = (EntityPlayerMP)event.getSource().getTrueSource();
            if (this.isPartnerNearbyCached(attacker)) {
               event.setAmount(event.getAmount() * 1.15F);
            }
         }

         if (event.getEntityLiving() instanceof EntityPlayerMP) {
            EntityPlayerMP defender = (EntityPlayerMP)event.getEntityLiving();
            if (this.isPartnerNearbyCached(defender)) {
               event.setAmount(Math.max(1.0F, event.getAmount() * 0.9F));
            }
         }

      }
   }

   @SubscribeEvent
   public void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            if (player.ticksExisted % 40 == 0) {
               if (this.isPartnerNearbyCached(player)) {
                  player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60, 0, false, false));
               }

            }
         }
      }
   }

   private boolean isPartnerNearbyCached(EntityPlayerMP player) {
      return partnerNearbyCache.containsKey(player.getUniqueID());
   }
}
