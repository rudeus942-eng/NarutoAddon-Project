
package net.luck.narutoaddon.OtherCode.raid.command;

import net.luck.narutoaddon.OtherCode.raid.boss.BossRegistry;
import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.core.RaidManager;
import net.luck.narutoaddon.OtherCode.raid.core.RaidModInit;
import net.luck.narutoaddon.OtherCode.raid.network.RaidNetworkHelper;
import net.luck.narutoaddon.OtherCode.raid.network.RaidPartyDataMessage;
import net.luck.narutoaddon.OtherCode.raid.party.RaidParty;
import net.luck.narutoaddon.OtherCode.raid.party.RaidPartyStorage;
import net.luck.narutoaddon.OtherCode.raid.rewards.RaidRewardDistributor;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CommandRaid extends CommandBase {
   public String getName() {
      return "raid";
   }

   public String getUsage(ICommandSender sender) {
      return "/raid <party|queue|leavequeue|ready|rewards|stats|bosses|help>";
   }

   public int getRequiredPermissionLevel() {
      return 0;
   }

   public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
      return sender instanceof EntityPlayerMP;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         throw new CommandException("This command can only be used by players.", new Object[0]);
      } else {
         EntityPlayerMP player = (EntityPlayerMP)sender;
         if (args.length == 0) {
            this.sendHelp(player);
         } else {
            switch (args[0].toLowerCase()) {
               case "party":
                  this.handlePartyCommand(player, args);
                  break;
               case "queue":
               case "q":
                  this.handleQueueCommand(player, args);
                  break;
               case "leavequeue":
               case "lq":
                  this.handleLeaveQueueCommand(player);
                  break;
               case "rewards":
               case "claim":
                  this.handleRewardsCommand(player);
                  break;
               case "bosses":
                  this.handleBossesCommand(player);
                  break;
               case "help":
               default:
                  this.sendHelp(player);
            }

         }
      }
   }

   private void handlePartyCommand(EntityPlayerMP player, String[] args) {
      if (args.length < 2) {
         this.sendPartyHelp(player);
      } else {
         String action = args[1].toLowerCase();
         RaidPartyStorage storage = RaidPartyStorage.get(player.world);
         if (storage == null) {
            player.sendMessage(new TextComponentString("§cError: Could not access party storage."));
         } else {
            switch (action) {
               case "create":
                  this.createParty(player, storage);
                  break;
               case "invite":
                  if (args.length < 3) {
                     player.sendMessage(new TextComponentString("§cUsage: /raid party invite <player>"));
                     return;
                  }

                  this.invitePlayer(player, storage, args[2]);
                  break;
               case "accept":
                  this.acceptInvite(player, storage);
                  break;
               case "decline":
                  this.declineInvite(player, storage);
                  break;
               case "kick":
                  if (args.length < 3) {
                     player.sendMessage(new TextComponentString("§cUsage: /raid party kick <player>"));
                     return;
                  }

                  this.kickPlayer(player, storage, args[2]);
                  break;
               case "leave":
                  this.leaveParty(player, storage);
                  break;
               case "promote":
                  if (args.length < 3) {
                     player.sendMessage(new TextComponentString("§cUsage: /raid party promote <player>"));
                     return;
                  }

                  this.promotePlayer(player, storage, args[2]);
                  break;
               case "info":
               case "list":
                  this.showPartyInfo(player, storage);
                  break;
               case "disband":
                  this.disbandParty(player, storage);
                  break;
               default:
                  this.sendPartyHelp(player);
            }

         }
      }
   }

   private void createParty(EntityPlayerMP player, RaidPartyStorage storage) {
      if (storage.isInParty(player.getUniqueID())) {
         player.sendMessage(new TextComponentString("§cYou are already in a party. Leave first with /raid party leave"));
      } else {
         RaidParty party = new RaidParty(player);
         storage.registerParty(party);
         storage.updatePlayerMapping(player.getUniqueID(), party.getPartyId());
         player.sendMessage(new TextComponentString("§a§l[RAID PARTY] §fParty created!"));
         player.sendMessage(new TextComponentString("§7Invite players with §f/raid party invite <player>"));
         RaidNetworkHelper.sendPartySync(party);
      }
   }

   private void invitePlayer(EntityPlayerMP player, RaidPartyStorage storage, String targetName) {
      RaidParty party = storage.getPlayerParty(player.getUniqueID());
      if (party == null) {
         player.sendMessage(new TextComponentString("§cYou are not in a party. Create one with /raid party create"));
      } else {
         EntityPlayerMP target = player.getServer().getPlayerList().getPlayerByUsername(targetName);
         if (target == null) {
            player.sendMessage(new TextComponentString("§cPlayer not found: " + targetName));
         } else if (storage.isInParty(target.getUniqueID())) {
            player.sendMessage(new TextComponentString("§c" + targetName + " is already in a party."));
         } else {
            RaidParty.InviteResult result = party.invite(player, target);
            player.sendMessage(new TextComponentString(result.success ? "§a" + result.message : "§c" + result.message));
            if (result.success) {
               storage.markDirty();
               RaidPartyDataMessage inviteMsg = RaidPartyDataMessage.createInviteMessage(player.getName(), party.getPartyId());
               RaidModInit.NETWORK.sendTo(inviteMsg, target);
               RaidNetworkHelper.sendPartySync(party);
            }

         }
      }
   }

   private void acceptInvite(EntityPlayerMP player, RaidPartyStorage storage) {
      for(RaidParty party : storage.getAllParties()) {
         RaidParty.InviteResult result = party.acceptInvite(player);
         if (result.success) {
            storage.updatePlayerMapping(player.getUniqueID(), party.getPartyId());
            player.sendMessage(new TextComponentString("§a" + result.message));
            RaidNetworkHelper.sendPartySync(party);
            return;
         }
      }

      player.sendMessage(new TextComponentString("§cYou don't have any pending party invites."));
   }

   private void declineInvite(EntityPlayerMP player, RaidPartyStorage storage) {
      for(RaidParty party : storage.getAllParties()) {
         RaidParty.InviteResult result = party.declineInvite(player);
         if (result.success) {
            player.sendMessage(new TextComponentString("§e" + result.message));
            storage.markDirty();
            RaidNetworkHelper.sendPartySync(party);
            return;
         }
      }

      player.sendMessage(new TextComponentString("§cYou don't have any pending party invites."));
   }

   private void kickPlayer(EntityPlayerMP player, RaidPartyStorage storage, String targetName) {
      RaidParty party = storage.getPlayerParty(player.getUniqueID());
      if (party == null) {
         player.sendMessage(new TextComponentString("§cYou are not in a party."));
      } else {
         UUID targetUUID = null;

         for(RaidParty.PartyMember member : party.getMembers()) {
            if (member.name.equalsIgnoreCase(targetName)) {
               targetUUID = member.uuid;
               break;
            }
         }

         if (targetUUID == null) {
            player.sendMessage(new TextComponentString("§c" + targetName + " is not in your party."));
         } else {
            RaidParty.InviteResult result = party.kick(player, targetUUID);
            if (result.success) {
               storage.updatePlayerMapping(targetUUID, (UUID)null);
               EntityPlayerMP kicked = player.getServer().getPlayerList().getPlayerByUUID(targetUUID);
               if (kicked != null) {
                  RaidPartyDataMessage disbandMsg = new RaidPartyDataMessage(5, (RaidParty)null);
                  RaidModInit.NETWORK.sendTo(disbandMsg, kicked);
               }

               RaidNetworkHelper.sendPartySync(party);
            }

            player.sendMessage(new TextComponentString(result.success ? "§a" + result.message : "§c" + result.message));
         }
      }
   }

   private void leaveParty(EntityPlayerMP player, RaidPartyStorage storage) {
      RaidParty party = storage.getPlayerParty(player.getUniqueID());
      if (party == null) {
         player.sendMessage(new TextComponentString("§cYou are not in a party."));
      } else {
         RaidParty.InviteResult result = party.leave(player);
         storage.updatePlayerMapping(player.getUniqueID(), (UUID)null);
         RaidPartyDataMessage disbandMsg = new RaidPartyDataMessage(5, (RaidParty)null);
         RaidModInit.NETWORK.sendTo(disbandMsg, player);
         if (party.getSize() == 0) {
            storage.removeParty(party.getPartyId());
         } else {
            RaidNetworkHelper.sendPartySync(party);
         }

         player.sendMessage(new TextComponentString("§e" + result.message));
      }
   }

   private void promotePlayer(EntityPlayerMP player, RaidPartyStorage storage, String targetName) {
      RaidParty party = storage.getPlayerParty(player.getUniqueID());
      if (party == null) {
         player.sendMessage(new TextComponentString("§cYou are not in a party."));
      } else {
         UUID targetUUID = null;

         for(RaidParty.PartyMember member : party.getMembers()) {
            if (member.name.equalsIgnoreCase(targetName)) {
               targetUUID = member.uuid;
               break;
            }
         }

         if (targetUUID == null) {
            player.sendMessage(new TextComponentString("§c" + targetName + " is not in your party."));
         } else {
            RaidParty.InviteResult result = party.promote(player, targetUUID);
            player.sendMessage(new TextComponentString(result.success ? "§a" + result.message : "§c" + result.message));
            if (result.success) {
               storage.markDirty();
               RaidNetworkHelper.sendPartySync(party);
            }

         }
      }
   }

   private void showPartyInfo(EntityPlayerMP player, RaidPartyStorage storage) {
      RaidParty party = storage.getPlayerParty(player.getUniqueID());
      if (party == null) {
         player.sendMessage(new TextComponentString("§cYou are not in a party."));
      } else {
         player.sendMessage(new TextComponentString("§e§l=== RAID PARTY ==="));
         player.sendMessage(new TextComponentString("§7State: §f" + party.getState().name()));
         player.sendMessage(new TextComponentString("§7Members (" + party.getSize() + "/" + 6 + "):"));

         for(RaidParty.PartyMember member : party.getMembers()) {
            String status = member.ready ? "§a[READY]" : "§c[NOT READY]";
            String leader = member.uuid.equals(party.getLeaderId()) ? " §6[Leader]" : "";
            player.sendMessage(new TextComponentString("  §f" + member.name + " " + status + leader));
         }

      }
   }

   private void disbandParty(EntityPlayerMP player, RaidPartyStorage storage) {
      RaidParty party = storage.getPlayerParty(player.getUniqueID());
      if (party == null) {
         player.sendMessage(new TextComponentString("§cYou are not in a party."));
      } else if (!party.isLeader(player.getUniqueID())) {
         player.sendMessage(new TextComponentString("§cOnly the party leader can disband the party."));
      } else {
         RaidPartyDataMessage disbandMsg = new RaidPartyDataMessage(5, (RaidParty)null);

         for(UUID uuid : party.getMemberUUIDs()) {
            storage.updatePlayerMapping(uuid, (UUID)null);
            EntityPlayerMP member = player.getServer().getPlayerList().getPlayerByUUID(uuid);
            if (member != null) {
               RaidModInit.NETWORK.sendTo(disbandMsg, member);
            }
         }

         storage.removeParty(party.getPartyId());
         party.broadcastMessage("§cThe party has been disbanded.");
      }
   }

   private void handleQueueCommand(EntityPlayerMP player, String[] args) {
      if (args.length < 2) {
         player.sendMessage(new TextComponentString("§cUsage: /raid queue <boss> [difficulty]"));
         player.sendMessage(new TextComponentString("§7Available bosses: " + String.join(", ", BossRegistry.getAllBossIds())));
      } else {
         String bossId = args[1].toLowerCase();
         RaidDifficulty difficulty = RaidDifficulty.GENIN;
         if (args.length >= 3) {
            difficulty = RaidDifficulty.fromString(args[2]);
            if (difficulty == null) {
               player.sendMessage(new TextComponentString("§cInvalid difficulty. Use: genin, chunin, or jonin"));
               return;
            }
         }

         RaidPartyStorage storage = RaidPartyStorage.get(player.world);
         RaidParty party = storage.getPlayerParty(player.getUniqueID());
         RaidManager.QueueResult result;
         if (party != null) {
            if (!party.isLeader(player.getUniqueID())) {
               player.sendMessage(new TextComponentString("§cOnly the party leader can queue for raids."));
               return;
            }

            for(UUID memberUUID : party.getMemberUUIDs()) {
               party.setReady(memberUUID, true);
            }

            party.setState(RaidParty.PartyState.READY);
            result = RaidManager.getInstance().queueParty(party, bossId, difficulty);
         } else {
            result = RaidManager.getInstance().queueSoloPlayer(player, bossId, difficulty);
         }

         player.sendMessage(new TextComponentString(result.success ? result.message : "§c" + result.message));
         if (result.success) {
            int playersInQueue = RaidManager.getInstance().getQueue().getPlayerCountForBossDifficulty(bossId, difficulty);
            int needed = difficulty.getMinPartySize();
            player.sendMessage(new TextComponentString("§7Waiting for players: §f" + playersInQueue + "/" + needed));
         }

      }
   }

   private void handleLeaveQueueCommand(EntityPlayerMP player) {
      RaidManager.QueueResult result = RaidManager.getInstance().leaveQueue(player);
      if (!result.success) {
         RaidPartyStorage storage = RaidPartyStorage.get(player.world);
         RaidParty party = storage.getPlayerParty(player.getUniqueID());
         if (party != null && party.isLeader(player.getUniqueID())) {
            result = RaidManager.getInstance().leaveQueue(party);
         }
      }

      player.sendMessage(new TextComponentString(result.success ? result.message : "§c" + result.message));
   }

   private void handleRewardsCommand(EntityPlayerMP player) {
      RaidRewardDistributor distributor = RaidRewardDistributor.get(player.world);
      if (distributor == null) {
         player.sendMessage(new TextComponentString("§cError: Could not access reward data."));
      } else if (!distributor.hasPendingRewards(player.getUniqueID())) {
         player.sendMessage(new TextComponentString("§7You have no pending raid rewards."));
      } else {
         distributor.claimRewards(player);
      }
   }

   private void handleBossesCommand(EntityPlayerMP player) {
      player.sendMessage(new TextComponentString("§e§l=== AVAILABLE BOSSES ==="));

      for(BossRegistry.BossEntry entry : BossRegistry.getAllEntries()) {
         player.sendMessage(new TextComponentString("§6" + entry.displayName + " §7(" + entry.bossId + ")"));
         player.sendMessage(new TextComponentString("  §8" + entry.description));
      }

   }

   private void sendHelp(EntityPlayerMP player) {
      player.sendMessage(new TextComponentString("§e§l=== RAID COMMANDS ==="));
      player.sendMessage(new TextComponentString("§6/raid queue <boss> [difficulty] §7- Queue for raid (solo or party)"));
      player.sendMessage(new TextComponentString("§6/raid leavequeue §7- Leave queue"));
      player.sendMessage(new TextComponentString("§6/raid party §7- Party management (optional)"));
      player.sendMessage(new TextComponentString("§6/raid rewards §7- Claim pending raid rewards"));
      player.sendMessage(new TextComponentString("§6/raid bosses §7- List available bosses"));
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§7You can queue solo or with a party. Solo players will"));
      player.sendMessage(new TextComponentString("§7be matched with others queueing for the same boss."));
   }

   private void sendPartyHelp(EntityPlayerMP player) {
      player.sendMessage(new TextComponentString("§e§l=== PARTY COMMANDS ==="));
      player.sendMessage(new TextComponentString("§7Parties are optional - you can queue solo!"));
      player.sendMessage(new TextComponentString(""));
      player.sendMessage(new TextComponentString("§6/raid party create §7- Create a new party"));
      player.sendMessage(new TextComponentString("§6/raid party invite <player> §7- Invite a player"));
      player.sendMessage(new TextComponentString("§6/raid party accept §7- Accept an invite"));
      player.sendMessage(new TextComponentString("§6/raid party decline §7- Decline an invite"));
      player.sendMessage(new TextComponentString("§6/raid party kick <player> §7- Kick a player"));
      player.sendMessage(new TextComponentString("§6/raid party leave §7- Leave the party"));
      player.sendMessage(new TextComponentString("§6/raid party promote <player> §7- Transfer leadership"));
      player.sendMessage(new TextComponentString("§6/raid party info §7- Show party info"));
      player.sendMessage(new TextComponentString("§6/raid party disband §7- Disband the party"));
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"queue", "leavequeue", "party", "rewards", "bosses", "help"});
      } else if (args.length == 2 && args[0].equalsIgnoreCase("party")) {
         return getListOfStringsMatchingLastWord(args, new String[]{"create", "invite", "accept", "decline", "kick", "leave", "promote", "info", "disband"});
      } else if (args.length != 2 || !args[0].equalsIgnoreCase("queue") && !args[0].equalsIgnoreCase("q")) {
         if (args.length != 3 || !args[0].equalsIgnoreCase("queue") && !args[0].equalsIgnoreCase("q")) {
            return args.length != 3 || !args[0].equalsIgnoreCase("party") || !args[1].equalsIgnoreCase("invite") && !args[1].equalsIgnoreCase("kick") && !args[1].equalsIgnoreCase("promote") ? Collections.emptyList() : getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
         } else {
            return getListOfStringsMatchingLastWord(args, new String[]{"genin", "chunin", "jonin", "anbu"});
         }
      } else {
         return getListOfStringsMatchingLastWord(args, (String[])BossRegistry.getAllBossIds().toArray(new String[0]));
      }
   }
}
