
package net.luck.narutoaddon.OtherCode.raid.network;

import net.luck.narutoaddon.OtherCode.raid.boss.IRaidBoss;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.luck.narutoaddon.OtherCode.raid.core.RaidModInit;
import net.luck.narutoaddon.OtherCode.raid.party.RaidParty;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.List;
import java.util.UUID;

public class RaidNetworkHelper {
   public static void sendPartySync(RaidParty party) {
      if (party != null) {
         RaidPartyDataMessage message = new RaidPartyDataMessage(0, party);
         sendToParty(party, message);
      }
   }

   public static void sendMemberJoin(RaidParty party) {
      if (party != null) {
         RaidPartyDataMessage message = new RaidPartyDataMessage(1, party);
         sendToParty(party, message);
      }
   }

   public static void sendMemberLeave(RaidParty party) {
      if (party != null) {
         RaidPartyDataMessage message = new RaidPartyDataMessage(2, party);
         sendToParty(party, message);
      }
   }

   public static void sendReadyUpdate(RaidParty party) {
      if (party != null) {
         RaidPartyDataMessage message = new RaidPartyDataMessage(3, party);
         sendToParty(party, message);
      }
   }

   public static void sendPartyDisbanded(RaidParty party) {
      if (party != null) {
         RaidPartyDataMessage message = new RaidPartyDataMessage(5, (RaidParty)null);
         sendToParty(party, message);
      }
   }

   public static void sendInvite(EntityPlayerMP player, String inviterName, UUID partyId) {
      if (player != null) {
         RaidPartyDataMessage message = RaidPartyDataMessage.createInviteMessage(inviterName, partyId);
         sendToPlayer(player, message);
      }
   }

   public static void sendBossSpawn(RaidInstance raid, EntityLivingBase bossEntity) {
      if (raid != null && bossEntity != null) {
         IRaidBoss boss = (IRaidBoss)bossEntity;
         RaidBossDataMessage message = RaidBossDataMessage.createSpawnMessage(raid.getRaidId(), boss.getBossId(), boss.getBossDisplayName(), bossEntity.getEntityId(), bossEntity.getMaxHealth(), boss.getPhaseController().getTotalPhases(), boss.getDifficulty().ordinal());
         sendToRaid(raid, message);
      }
   }

   public static void sendHealthUpdate(RaidInstance raid, float currentHealth, float maxHealth) {
      if (raid != null) {
         RaidBossDataMessage message = RaidBossDataMessage.createHealthUpdate(raid.getRaidId(), currentHealth, maxHealth);
         sendToRaid(raid, message);
      }
   }

   public static void sendPhaseChange(RaidInstance raid, int currentPhase, int totalPhases, String phaseName) {
      if (raid != null) {
         RaidBossDataMessage message = RaidBossDataMessage.createPhaseChange(raid.getRaidId(), currentPhase, totalPhases, phaseName);
         sendToRaid(raid, message);
      }
   }

   public static void sendImmunityUpdate(RaidInstance raid, boolean isImmune, String reason) {
      if (raid != null) {
         RaidBossDataMessage message = RaidBossDataMessage.createImmunityUpdate(raid.getRaidId(), isImmune, reason);
         sendToRaid(raid, message);
      }
   }

   public static void sendEnrageUpdate(RaidInstance raid, int enrageLevel, long timeElapsed) {
      if (raid != null) {
         RaidBossDataMessage message = RaidBossDataMessage.createEnrageUpdate(raid.getRaidId(), enrageLevel, timeElapsed);
         sendToRaid(raid, message);
      }
   }

   public static void sendBossDeath(RaidInstance raid) {
      if (raid != null) {
         RaidBossDataMessage message = RaidBossDataMessage.createDeathMessage(raid.getRaidId());
         sendToRaid(raid, message);
      }
   }

   public static void sendBossDespawn(RaidInstance raid) {
      if (raid != null) {
         RaidBossDataMessage message = RaidBossDataMessage.createDespawnMessage(raid.getRaidId());
         sendToRaid(raid, message);
      }
   }

   public static void sendBossDespawn(RaidInstance raid, EntityPlayerMP player) {
      if (raid != null && player != null) {
         RaidBossDataMessage message = RaidBossDataMessage.createDespawnMessage(raid.getRaidId());
         sendToPlayer(player, message);
      }
   }

   public static void sendMechanicWarning(RaidInstance raid, String mechanicName, String warningMessage, int type, int warningTicks) {
      if (raid != null) {
         RaidMechanicMessage message = RaidMechanicMessage.createWarning(raid.getRaidId(), mechanicName, warningMessage, type, warningTicks);
         sendToRaid(raid, message);
      }
   }

   public static void sendMechanicStart(RaidInstance raid, String mechanicName, int type, int durationTicks) {
      if (raid != null) {
         RaidMechanicMessage message = RaidMechanicMessage.createStart(raid.getRaidId(), mechanicName, type, durationTicks);
         sendToRaid(raid, message);
      }
   }

   public static void sendMechanicEnd(RaidInstance raid, String mechanicName) {
      if (raid != null) {
         RaidMechanicMessage message = RaidMechanicMessage.createEnd(raid.getRaidId(), mechanicName);
         sendToRaid(raid, message);
      }
   }

   public static void sendSafeZones(RaidInstance raid, List<BlockPos> safePositions, double radius) {
      if (raid != null) {
         RaidMechanicMessage message = RaidMechanicMessage.createSafeZoneUpdate(raid.getRaidId(), safePositions, radius);
         sendToRaid(raid, message);
      }
   }

   public static void sendDangerZones(RaidInstance raid, List<BlockPos> dangerPositions, double radius) {
      if (raid != null) {
         RaidMechanicMessage message = RaidMechanicMessage.createDangerZoneUpdate(raid.getRaidId(), dangerPositions, radius);
         sendToRaid(raid, message);
      }
   }

   public static void sendPuzzleUpdate(RaidInstance raid, String objective, int progress, int total) {
      if (raid != null) {
         RaidMechanicMessage message = RaidMechanicMessage.createPuzzleUpdate(raid.getRaidId(), objective, progress, total);
         sendToRaid(raid, message);
      }
   }

   public static void sendClearMechanics(RaidInstance raid) {
      if (raid != null) {
         RaidMechanicMessage message = RaidMechanicMessage.createClearAll(raid.getRaidId());
         sendToRaid(raid, message);
      }
   }

   private static void sendToPlayer(EntityPlayerMP player, IMessage message) {
      if (player != null && RaidModInit.NETWORK != null) {
         RaidModInit.NETWORK.sendTo(message, player);
      }

   }

   private static void sendToParty(RaidParty party, IMessage message) {
      if (party != null && RaidModInit.NETWORK != null) {
         for(EntityPlayerMP player : party.getOnlinePlayers()) {
            RaidModInit.NETWORK.sendTo(message, player);
         }

      }
   }

   private static void sendToRaid(RaidInstance raid, IMessage message) {
      if (raid != null && RaidModInit.NETWORK != null) {
         for(EntityPlayerMP player : raid.getParticipants()) {
            RaidModInit.NETWORK.sendTo(message, player);
         }

      }
   }

   public static void sendToAllRaidMembers(RaidInstance raid, IMessage message) {
      if (raid != null && RaidModInit.NETWORK != null) {
         for(EntityPlayerMP player : raid.getAllOriginalParticipants()) {
            RaidModInit.NETWORK.sendTo(message, player);
         }

      }
   }
}
