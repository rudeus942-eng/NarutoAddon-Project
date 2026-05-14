
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.List;
import java.util.UUID;

public class WarMomentumSystem {
   private static final int CAPTURE_BUFF_TICKS = 600;
   private static final int LOSS_BUFF_TICKS = 300;

   public static void onCheckpointCaptured(VillageHelper.Village capturingVillage, WarInstance war, World world) {
      List<UUID> roster = getRoster(capturingVillage, war);
      if (roster != null) {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            for(UUID uuid : roster) {
               EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
               if (player != null) {
                  player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 600, 0, false, true));
                  player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 600, 0, false, true));
               }
            }

         }
      }
   }

   public static void onCheckpointLost(VillageHelper.Village losingVillage, WarInstance war, World world) {
      List<UUID> roster = getRoster(losingVillage, war);
      if (roster != null) {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            for(UUID uuid : roster) {
               EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
               if (player != null) {
                  player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 300, 0, false, true));
               }
            }

         }
      }
   }

   public static void tickMomentum(WarInstance war, World world) {
      if (war.getZones() != null) {
         int capturedByV1 = 0;
         int capturedByV2 = 0;

         for(WarZone zone : war.getZones()) {
            if (zone.getControllingVillage() == war.getVillage1()) {
               ++capturedByV1;
            } else if (zone.getControllingVillage() == war.getVillage2()) {
               ++capturedByV2;
            }
         }

         if (capturedByV1 > capturedByV2 && capturedByV1 >= 3) {
            applyPassiveBuff(war.getVillage1(), war, world);
         } else if (capturedByV2 > capturedByV1 && capturedByV2 >= 3) {
            applyPassiveBuff(war.getVillage2(), war, world);
         }

      }
   }

   private static void applyPassiveBuff(VillageHelper.Village village, WarInstance war, World world) {
      List<UUID> roster = getRoster(village, war);
      if (roster != null) {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            for(UUID uuid : roster) {
               EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
               if (player != null && !player.isPotionActive(MobEffects.SPEED)) {
                  player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 40, 0, false, false));
               }
            }

         }
      }
   }

   private static List<UUID> getRoster(VillageHelper.Village village, WarInstance war) {
      WarLobby lobby = war.getLobby();
      if (lobby == null) {
         return null;
      } else if (village == war.getVillage1()) {
         return lobby.getRoster1();
      } else {
         return village == war.getVillage2() ? lobby.getRoster2() : null;
      }
   }
}
