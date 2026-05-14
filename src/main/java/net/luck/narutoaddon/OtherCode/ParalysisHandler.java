
package net.luck.narutoaddon.OtherCode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.util.EnumSet;

@ElementsInfTsukAddon.ModElement.Tag
public class ParalysisHandler extends ElementsInfTsukAddon.ModElement {
   public ParalysisHandler(ElementsInfTsukAddon instance) {
      super(instance, 101);
   }

   public void init(FMLInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new ParalysisEventHandler());
   }

   public static class ParalysisEventHandler {
      @SubscribeEvent
      public void onPlayerTick(TickEvent.PlayerTickEvent event) {
         if (event.phase == Phase.END) {
            EntityPlayer player = event.player;
            if (player.getEntityData().hasKey("ParalysisEndTime")) {
               long endTime = player.getEntityData().getLong("ParalysisEndTime");
               long currentTime = player.world.getTotalWorldTime();
               if (currentTime < endTime) {
                  float lockedYaw = player.getEntityData().getFloat("ParalysisYaw");
                  float lockedPitch = player.getEntityData().getFloat("ParalysisPitch");
                  player.rotationYaw = lockedYaw;
                  player.rotationPitch = lockedPitch;
                  player.rotationYawHead = lockedYaw;
                  player.prevRotationYaw = lockedYaw;
                  player.prevRotationPitch = lockedPitch;
                  player.prevRotationYawHead = lockedYaw;
                  if (!player.world.isRemote && player instanceof EntityPlayerMP) {
                     EntityPlayerMP playerMP = (EntityPlayerMP)player;
                     playerMP.connection.setPlayerLocation(player.posX, player.posY, player.posZ, lockedYaw, lockedPitch, EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class));
                  }
               } else {
                  player.getEntityData().removeTag("ParalysisEndTime");
                  player.getEntityData().removeTag("ParalysisYaw");
                  player.getEntityData().removeTag("ParalysisPitch");
               }

            }
         }
      }
   }
}
