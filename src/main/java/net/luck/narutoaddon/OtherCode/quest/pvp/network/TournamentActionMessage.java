
package net.luck.narutoaddon.OtherCode.quest.pvp.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.InfTsukAddon;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.tournament.TournamentInstance;
import net.luck.narutoaddon.OtherCode.quest.pvp.tournament.TournamentManager;
import net.luck.narutoaddon.OtherCode.quest.pvp.tournament.TournamentState;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.AdvisorManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Iterator;
import java.util.UUID;

public class TournamentActionMessage implements IMessage {
   public static final byte ACTION_CREATE_TOURNAMENT = 0;
   public static final byte ACTION_SET_REWARDS = 1;
   public static final byte ACTION_FINALIZE = 2;
   public static final byte ACTION_SIGNUP = 3;
   public static final byte ACTION_WITHDRAW = 4;
   public static final byte ACTION_CANCEL = 5;
   public static final byte ACTION_OPEN_REWARD_GUI = 6;
   public static final byte ACTION_TOGGLE_VILLAGE_ONLY = 7;
   private byte action;
   private byte place;
   private int signupDurationMinutes;
   private int matchTimeoutSec;
   private byte minRank;
   private byte maxRank;

   public static TournamentActionMessage createTournament(int signupDurationMinutes, int matchTimeoutSec, byte minRank, byte maxRank) {
      TournamentActionMessage msg = new TournamentActionMessage();
      msg.action = 0;
      msg.signupDurationMinutes = signupDurationMinutes;
      msg.matchTimeoutSec = matchTimeoutSec;
      msg.minRank = minRank;
      msg.maxRank = maxRank;
      return msg;
   }

   public static TournamentActionMessage openRewardGui(byte place) {
      TournamentActionMessage msg = new TournamentActionMessage();
      msg.action = 6;
      msg.place = place;
      return msg;
   }

   public static TournamentActionMessage finalize_() {
      return simple((byte)2);
   }

   public static TournamentActionMessage signup() {
      return simple((byte)3);
   }

   public static TournamentActionMessage withdraw() {
      return simple((byte)4);
   }

   public static TournamentActionMessage cancel() {
      return simple((byte)5);
   }

   public static TournamentActionMessage toggleVillageOnly() {
      return simple((byte)7);
   }

   private static TournamentActionMessage simple(byte action) {
      TournamentActionMessage msg = new TournamentActionMessage();
      msg.action = action;
      return msg;
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readByte();
      switch (this.action) {
         case 0:
            this.signupDurationMinutes = buf.readInt();
            this.matchTimeoutSec = buf.readInt();
            this.minRank = buf.readByte();
            this.maxRank = buf.readByte();
            break;
         case 6:
            this.place = buf.readByte();
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.action);
      switch (this.action) {
         case 0:
            buf.writeInt(this.signupDurationMinutes);
            buf.writeInt(this.matchTimeoutSec);
            buf.writeByte(this.minRank);
            buf.writeByte(this.maxRank);
            break;
         case 6:
            buf.writeByte(this.place);
      }

   }

   public static class Handler implements IMessageHandler<TournamentActionMessage, IMessage> {
      public IMessage onMessage(TournamentActionMessage msg, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         if (player == null) {
            return null;
         } else {
            player.getServerWorld().addScheduledTask(() -> {
               UUID playerId = player.getUniqueID();
               VillageHelper.Village village = VillageHelper.getVillage(player);
               String villageName = village.teamName;
               TournamentManager tm = TournamentManager.getInstance();
               boolean isOp = player.canUseCommand(2, "");
               switch (msg.action) {
                  case 0:
                     if (!isOp && !AdvisorManager.getInstance().hasLeadershipAuthority(playerId, village)) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have leadership authority to create a tournament."));
                        return;
                     }

                     long signupDurationMs = (long)msg.signupDurationMinutes * 60L * 1000L;
                     String tourneyVillage = isOp ? "global" : villageName;
                     TournamentInstance inst = tm.createTournament(playerId, player.getName(), tourneyVillage, msg.minRank, msg.maxRank, signupDurationMs, msg.matchTimeoutSec);
                     if (inst != null && isOp) {
                        inst.setVillageOnly(false);
                     }

                     if (inst == null) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "A tournament is already active."));
                     }
                  case 1:
                  default:
                     break;
                  case 2:
                     if (!isOp && !AdvisorManager.getInstance().hasLeadershipAuthority(playerId, village)) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have leadership authority."));
                        return;
                     }

                     String finalizeVillage = villageName;
                     if (!tm.hasTournament(villageName) && isOp) {
                        if (tm.hasTournament("global")) {
                           finalizeVillage = "global";
                        } else {
                           Iterator var22 = tm.getActiveVillages().iterator();
                           if (var22.hasNext()) {
                              String v = (String)var22.next();
                              finalizeVillage = v;
                           }
                        }
                     }

                     if (!tm.finalizeTournament(finalizeVillage, player.world)) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot finalize tournament (not in SETUP state or none exists)."));
                     }
                     break;
                  case 3:
                     String error = tm.signUp(playerId, player.getName(), villageName, player.world);
                     if (error != null) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + error));
                     } else {
                        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "You have signed up for the tournament!"));
                     }
                     break;
                  case 4:
                     boolean withdrawn = tm.withdrawSignup(playerId, villageName, player.world);
                     if (!withdrawn) {
                        for(TournamentInstance t : tm.getAllTournaments().values()) {
                           if (t.hasParticipant(playerId) && t.getState() == TournamentState.SIGNUP) {
                              withdrawn = tm.withdrawSignup(playerId, t.getVillageName(), player.world);
                              if (withdrawn) {
                                 break;
                              }
                           }
                        }
                     }

                     if (withdrawn) {
                        player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "You have withdrawn from the tournament."));
                     } else {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot withdraw (no active signup or tournament not in signup phase)."));
                     }
                     break;
                  case 5:
                     if (!isOp && !AdvisorManager.getInstance().hasLeadershipAuthority(playerId, village)) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have leadership authority."));
                        return;
                     }

                     if (tm.hasTournament(villageName)) {
                        tm.cancelTournament(villageName, player.world);
                     } else if (isOp) {
                        Iterator var12 = tm.getActiveVillages().iterator();
                        if (var12.hasNext()) {
                           String v = (String)var12.next();
                           tm.cancelTournament(v, player.world);
                           player.sendMessage(new TextComponentString(TextFormatting.GOLD + "Cancelled tournament for " + v));
                        }
                     }
                     break;
                  case 6:
                     if (!isOp && !AdvisorManager.getInstance().hasLeadershipAuthority(playerId, village)) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have leadership authority."));
                        return;
                     }

                     String rewardVillage = villageName;
                     if (!tm.hasTournament(villageName) && isOp) {
                        if (tm.hasTournament("global")) {
                           rewardVillage = "global";
                        } else {
                           Iterator instx = tm.getActiveVillages().iterator();
                           if (instx.hasNext()) {
                              String v = (String)instx.next();
                              rewardVillage = v;
                           }
                        }
                     }

                     TournamentInstance inst = tm.getTournamentForVillage(rewardVillage);
                     if (inst == null) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "No tournament exists."));
                        return;
                     }

                     int guiId = 100 + msg.place - 1;
                     player.openGui(InfTsukAddon.instance, guiId, player.world, (int)player.posX, (int)player.posY, (int)player.posZ);
                     break;
                  case 7:
                     if (!isOp && !AdvisorManager.getInstance().hasLeadershipAuthority(playerId, village)) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have leadership authority."));
                        return;
                     }

                     String toggleVillage = villageName;
                     if (!tm.hasTournament(villageName) && isOp) {
                        if (tm.hasTournament("global")) {
                           toggleVillage = "global";
                        } else {
                           Iterator var8 = tm.getActiveVillages().iterator();
                           if (var8.hasNext()) {
                              String v = (String)var8.next();
                              toggleVillage = v;
                           }
                        }
                     }

                     TournamentInstance inst = tm.getTournamentForVillage(toggleVillage);
                     if (inst == null) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "No tournament exists."));
                        return;
                     }

                     inst.setVillageOnly(!inst.isVillageOnly());
                     tm.saveTournament(toggleVillage, player.world);
                     player.sendMessage(new TextComponentString(TextFormatting.GOLD + "Village Only: " + (inst.isVillageOnly() ? "ON" : "OFF")));
               }

               PvpNetworkHelper.sendTournamentSync(player);
            });
            return null;
         }
      }
   }
}
