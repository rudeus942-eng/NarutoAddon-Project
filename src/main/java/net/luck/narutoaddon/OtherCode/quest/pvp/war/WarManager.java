
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.PvpKillTracker;
import net.luck.narutoaddon.OtherCode.quest.pvp.network.PvpNetworkHelper;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.*;

public class WarManager {
   private static WarManager instance;
   private final Map<String, WarInstance> activeWars = new HashMap();
   private final Map<String, Long> warCooldowns = new HashMap();
   private static final long COOLDOWN_MS = 14400000L;
   private static final int SCORE_NORMAL_KILL = 1;
   private static final int SCORE_HIGHER_LEVEL_KILL = 2;
   private static final int SCORE_KAGE_KILL = 5;
   private static final int ZONE_KILL_BONUS = 1;
   private static final int DIVISION_BEST_OF = 3;
   private static final int WINNER_BASE_XP = 100;
   private static final int LOSER_PARTICIPATION_XP = 25;
   private static final int[] TOP_KILLER_BONUS = new int[]{150, 100, 50};
   private int tickCounter = 0;
   private static final int[] TOP_KILLER_RYO = new int[]{60000, 35000, 20000};

   public static WarManager getInstance() {
      if (instance == null) {
         instance = new WarManager();
      }

      return instance;
   }

   public static void reset() {
      if (instance != null) {
         System.out.println("[WAR DEBUG] WarManager.reset() called. Clearing " + instance.activeWars.size() + " active wars and " + instance.warCooldowns.size() + " cooldowns.");
      }

      instance = null;
   }

   @Nullable
   public WarInstance declareWar(UUID kageId, VillageHelper.Village attackerVillage, VillageHelper.Village defenderVillage, WarMode mode, World world) {
      if (attackerVillage != VillageHelper.Village.UNKNOWN && defenderVillage != VillageHelper.Village.UNKNOWN) {
         if (attackerVillage == defenderVillage) {
            System.out.println("[WAR DEBUG] Rejected: attacker=defender=" + attackerVillage);
            return null;
         } else if (defenderVillage == VillageHelper.Village.AKATSUKI && attackerVillage != VillageHelper.Village.AKATSUKI) {
            System.out.println("[WAR DEBUG] Rejected: villages cannot declare war on Akatsuki. attacker=" + attackerVillage);
            return null;
         } else if (!this.canDeclareWar(attackerVillage)) {
            System.out.println("[WAR DEBUG] Attacker " + attackerVillage.villageName + " cannot declare war. Active war: " + this.getActiveWar(attackerVillage) + ", cooldown remaining: " + this.getRemainingCooldown(attackerVillage) + "ms, activeWars size: " + this.activeWars.size());
            return null;
         } else if (!this.canDeclareWar(defenderVillage)) {
            System.out.println("[WAR DEBUG] Defender " + defenderVillage.villageName + " cannot declare war. Active war: " + this.getActiveWar(defenderVillage) + ", cooldown remaining: " + this.getRemainingCooldown(defenderVillage) + "ms, activeWars size: " + this.activeWars.size());
            return null;
         } else if (mode.isAkatsukiOnly() && attackerVillage != VillageHelper.Village.AKATSUKI) {
            System.out.println("[WAR DEBUG] Rejected: " + mode.displayName + " is Akatsuki-only. attacker=" + attackerVillage);
            return null;
         } else {
            KageManager kageManager = KageManager.getInstance();
            UUID attackerKage = kageManager.getKage(attackerVillage);
            if (attackerKage != null && attackerKage.equals(kageId)) {
               String warId = "war_" + attackerVillage.teamName + "_" + defenderVillage.teamName + "_" + System.currentTimeMillis();
               WarInstance war = new WarInstance(warId, mode, attackerVillage, defenderVillage, kageId);
               if (mode == WarMode.SKIRMISH) {
                  this.activeWars.put(warId, war);
                  this.broadcastServerMessage(TextFormatting.RED + "[WAR] " + TextFormatting.GOLD + attackerVillage.villageName + TextFormatting.YELLOW + " has declared " + mode.displayName + " against " + TextFormatting.GOLD + defenderVillage.villageName + TextFormatting.YELLOW + "! Duration: " + war.getFormattedTimeRemaining());
               } else {
                  if (mode == WarMode.DIVISION && WarBattlefield.isArenaInUse()) {
                     MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
                     if (server != null) {
                        EntityPlayerMP kagePlayer = server.getPlayerList().getPlayerByUUID(kageId);
                        if (kagePlayer != null) {
                           kagePlayer.sendMessage(new TextComponentString(TextFormatting.RED + "[WAR] The battlefield arena is currently in use by another 10v10 battle. Wait for it to finish before declaring a 10v10 war."));
                        }
                     }

                     return null;
                  }

                  UUID defenderKage = kageManager.getKage(defenderVillage);
                  if (defenderKage == null) {
                     return null;
                  }

                  WarLobby lobby = new WarLobby(attackerVillage, defenderVillage, kageId, defenderKage);
                  war.setLobby(lobby);
                  this.activeWars.put(warId, war);
                  EntityPlayerMP defKagePlayer = kageManager.getKagePlayer(defenderVillage);
                  if (defKagePlayer != null) {
                     defKagePlayer.sendMessage(new TextComponentString(TextFormatting.RED + "[WAR] " + TextFormatting.GOLD + attackerVillage.villageName + TextFormatting.YELLOW + " has challenged your village to " + mode.displayName + "! Open the War tab to respond."));
                  }

                  this.broadcastServerMessage(TextFormatting.YELLOW + "[WAR] " + TextFormatting.GOLD + attackerVillage.villageName + TextFormatting.YELLOW + " has challenged " + TextFormatting.GOLD + defenderVillage.villageName + TextFormatting.YELLOW + " to " + mode.displayName + "!");
               }

               return war;
            } else {
               System.out.println("[WAR DEBUG] Kage validation failed. kageId=" + kageId + " attackerKage=" + attackerKage + " village=" + attackerVillage.villageName);
               return null;
            }
         }
      } else {
         System.out.println("[WAR DEBUG] Rejected: attacker=" + attackerVillage + " defender=" + defenderVillage + " (UNKNOWN village)");
         return null;
      }
   }

   public boolean surrenderWar(UUID kageId) {
      VillageHelper.Village kageVillage = KageManager.getInstance().getKageVillage(kageId);
      if (kageVillage == null) {
         return false;
      } else {
         WarInstance war = this.getActiveWar(kageVillage);
         if (war == null) {
            return false;
         } else {
            war.surrender(kageVillage);
            this.endWar(war, false);
            return true;
         }
      }
   }

   public void onPvpKill(EntityPlayerMP killer, EntityPlayerMP victim, PvpKillTracker.KillData killData) {
      VillageHelper.Village killerVillage = killData.killerVillage;
      VillageHelper.Village victimVillage = killData.victimVillage;
      WarInstance war = this.findWarBetween(killerVillage, victimVillage);
      if (war != null) {
         int side = war.getPlayerSide(killerVillage);
         if (side != 0) {
            int score;
            if (war.getMode() == WarMode.INFILTRATION) {
               boolean killerIsAkatsuki = side == 1;
               score = killerIsAkatsuki ? 1 : 3;
            } else {
               score = 1;
               if (victim.experienceLevel > killer.experienceLevel) {
                  score = 2;
               }

               KageManager kageManager = KageManager.getInstance();
               if (kageManager.isKage(victim.getUniqueID())) {
                  score = 5;
               }

               if ((war.getMode() == WarMode.DOMINATION || war.getMode() == WarMode.RUSH) && war.getZones() != null) {
                  for(WarZone zone : war.getZones()) {
                     if (zone.isPlayerInZone(killer)) {
                        ++score;
                        break;
                     }
                  }
               }
            }

            if (side == 1) {
               war.addScore1(score);
            } else {
               war.addScore2(score);
            }

            war.recordKill(killerVillage, killer.getUniqueID());
         }
      }
   }

   public void tickWars(World world) {
      ++this.tickCounter;
      if (WarBattlefield.isCountdownActive()) {
         String warId = WarBattlefield.getCountdownWarId();
         WarInstance war = warId != null ? (WarInstance)this.activeWars.get(warId) : null;
         if (war != null && war.getLobby() != null) {
            List<UUID> allPlayers = new ArrayList();
            allPlayers.addAll(war.getLobby().getRoster1());
            allPlayers.addAll(war.getLobby().getRoster2());
            boolean fightStarted = WarBattlefield.tickCountdown(allPlayers);
            if (fightStarted) {
               war.resetRoundTimer();
               PvpNetworkHelper.sendWarSyncToAll();
            }
         }
      }

      if (this.tickCounter % 20 == 0) {
         boolean periodicSync = this.tickCounter % 100 == 0 && !this.activeWars.isEmpty();
         List<WarInstance> toEnd = new ArrayList();

         for(WarInstance war : this.activeWars.values()) {
            WarLobby lobby = war.getLobby();
            if (lobby != null && lobby.getState() == WarLobby.LobbyState.CHALLENGE_SENT && lobby.isExpired()) {
               lobby.setState(WarLobby.LobbyState.EXPIRED);
               toEnd.add(war);
            } else {
               if (lobby != null && war.getMode() != WarMode.SKIRMISH) {
                  if (lobby.getState() == WarLobby.LobbyState.LOCKED_WAITING) {
                     if (lobby.isReady()) {
                        lobby.setState(WarLobby.LobbyState.READY);
                        this.startWarFromLobby(war, world);
                     }
                     continue;
                  }

                  if (lobby.getState() == WarLobby.LobbyState.READY && war.getMode() == WarMode.DIVISION && !WarBattlefield.isArenaInUse()) {
                     this.startWarFromLobby(war, world);
                     continue;
                  }

                  if (lobby.getState() != WarLobby.LobbyState.READY) {
                     continue;
                  }
               }

               if (war.isTimeUp()) {
                  if (war.getMode() == WarMode.DIVISION) {
                     if (!WarBattlefield.isTeleportInProgress() && !WarBattlefield.isCountdownActive()) {
                        this.endDivisionRound(war, world);
                        if (this.isDivisionComplete(war)) {
                           toEnd.add(war);
                        }
                     }
                  } else {
                     toEnd.add(war);
                  }
               } else {
                  if (war.getZones() != null) {
                     this.tickZones(war, world);
                  }

                  if (war.getMode() == WarMode.RUSH) {
                     WarMomentumSystem.tickMomentum(war, world);
                  }
               }
            }
         }

         for(WarInstance war : toEnd) {
            this.endWar(war, false);
         }

         if (periodicSync && !this.activeWars.isEmpty()) {
            PvpNetworkHelper.sendWarSyncToAll();
         }

      }
   }

   private void startWarFromLobby(WarInstance war, World world) {
      if (war.getMode() == WarMode.DOMINATION) {
         List<WarZone> zones = WarZoneGenerator.generateDominationZones(war.getVillage1(), war.getVillage2(), world);
         war.setZones(zones);
      } else if (war.getMode() == WarMode.RUSH) {
         List<WarZone> zones = WarZoneGenerator.generateRushCheckpoints(war.getVillage1(), war.getVillage2(), world);
         war.setZones(zones);
      }

      if (war.getMode() == WarMode.DIVISION) {
         if (!WarBattlefield.reserveArena(war.getWarId())) {
            this.broadcastServerMessage(TextFormatting.RED + "[WAR] " + TextFormatting.YELLOW + "10v10 battle cancelled — arena is already in use.");
            this.activeWars.remove(war.getWarId());
            return;
         }

         this.startDivisionRound(war, world);
      }

      this.broadcastServerMessage(TextFormatting.RED + "[WAR] " + TextFormatting.GOLD + war.getVillage1().villageName + TextFormatting.YELLOW + " vs " + TextFormatting.GOLD + war.getVillage2().villageName + TextFormatting.YELLOW + " " + war.getMode().displayName + " has begun!");
      PvpNetworkHelper.sendWarSyncToAll();
   }

   private void tickZones(WarInstance war, World world) {
      if (war.getZones() != null) {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            for(WarZone zone : war.getZones()) {
               List<EntityPlayerMP> playersInZone = new ArrayList();

               for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                  if (zone.isPlayerInZone(player)) {
                     VillageHelper.Village pv = VillageHelper.getVillage(player);
                     if (pv == war.getVillage1() || pv == war.getVillage2()) {
                        playersInZone.add(player);
                     }
                  }
               }

               VillageHelper.Village prevController = zone.getControllingVillage();
               zone.tickZone(war.getVillage1(), war.getVillage2(), playersInZone);
               VillageHelper.Village newController = zone.getControllingVillage();
               if (newController != null && newController != prevController) {
                  this.onZoneCaptured(war, zone, newController, world);
               } else if (prevController != null && newController == null) {
                  this.onZoneLost(war, zone, prevController, world);
               }
            }

         }
      }
   }

   private void onZoneCaptured(WarInstance war, WarZone zone, VillageHelper.Village capturingVillage, World world) {
      this.broadcastServerMessage(TextFormatting.YELLOW + "[WAR] " + TextFormatting.GOLD + capturingVillage.villageName + TextFormatting.YELLOW + " has captured Zone " + (zone.getZoneIndex() + 1) + "!");
      if (war.getMode() == WarMode.RUSH) {
         WarMomentumSystem.onCheckpointCaptured(capturingVillage, war, world);
      }

      int side = war.getPlayerSide(capturingVillage);
      if (side == 1) {
         war.addScore1(3);
      } else if (side == 2) {
         war.addScore2(3);
      }

   }

   private void onZoneLost(WarInstance war, WarZone zone, VillageHelper.Village losingVillage, World world) {
      if (war.getMode() == WarMode.RUSH) {
         WarMomentumSystem.onCheckpointLost(losingVillage, war, world);
      }

   }

   public void startDivisionRound(WarInstance war, World world) {
      this.broadcastServerMessage(TextFormatting.YELLOW + "[WAR] 10v10 Round " + war.getCurrentRound() + " — Teleporting players...");
      WarLobby lobby = war.getLobby();
      if (lobby == null) {
         System.out.println("[WAR] startDivisionRound: lobby is null! Cannot teleport.");
      } else {
         List<UUID> roster1 = lobby.getRoster1();
         List<UUID> roster2 = lobby.getRoster2();
         System.out.println("[WAR] startDivisionRound: roster1=" + roster1.size() + " players, roster2=" + roster2.size() + " players");
         System.out.println("[WAR] Team1Spawn=" + WarBattlefield.getTeam1Spawn() + " Team2Spawn=" + WarBattlefield.getTeam2Spawn());
         if (roster1.isEmpty() && roster2.isEmpty()) {
            System.out.println("[WAR] ERROR: Both rosters are empty! Teleport will do nothing.");
            this.broadcastServerMessage(TextFormatting.RED + "[WAR] Error: No players in rosters. Teleport cancelled.");
         } else {
            WarBattlefield.teleportTeams(roster1, roster2, world);
            WarBattlefield.startCountdown(war.getWarId());
         }
      }
   }

   public void endDivisionRound(WarInstance war, World world) {
      VillageHelper.Village roundWinner = war.getWinningVillage();
      if (roundWinner == war.getVillage1()) {
         war.addRoundWin1();
      } else if (roundWinner == war.getVillage2()) {
         war.addRoundWin2();
      }

      this.broadcastServerMessage(TextFormatting.YELLOW + "[WAR] Round " + war.getCurrentRound() + " complete! Score: " + war.getVillage1().villageName + " " + war.getRoundsWon1() + " - " + war.getRoundsWon2() + " " + war.getVillage2().villageName);
      WarLobby lobby = war.getLobby();
      if (lobby != null) {
         List<UUID> allPlayers = new ArrayList();
         allPlayers.addAll(lobby.getRoster1());
         allPlayers.addAll(lobby.getRoster2());
         WarBattlefield.returnPlayers(allPlayers, world);
      }

      if (!this.isDivisionComplete(war)) {
         war.advanceRound();
      }

   }

   public void onDivisionPlayerDeath(EntityPlayerMP player, WarInstance war, World world) {
      WarBattlefield.enterDeadState(player);
   }

   private boolean isDivisionComplete(WarInstance war) {
      int needed = 2;
      return war.getRoundsWon1() >= needed || war.getRoundsWon2() >= needed;
   }

   public void endWar(WarInstance war, boolean forcedByAdmin) {
      VillageHelper.Village winner = war.getWinningVillage();
      if (forcedByAdmin) {
         this.broadcastServerMessage(TextFormatting.RED + "[WAR] " + TextFormatting.YELLOW + "The war between " + war.getVillage1().villageName + " and " + war.getVillage2().villageName + " has been ended by an admin.");
      } else if (war.isSurrendered()) {
         this.broadcastServerMessage(TextFormatting.RED + "[WAR] " + TextFormatting.GOLD + war.getSurrenderedBy().villageName + TextFormatting.YELLOW + " has surrendered! " + TextFormatting.GOLD + (winner != null ? winner.villageName : "No one") + TextFormatting.YELLOW + " wins!");
      } else if (winner != null) {
         this.broadcastServerMessage(TextFormatting.RED + "[WAR] " + TextFormatting.GOLD + winner.villageName + TextFormatting.YELLOW + " wins the " + war.getMode().displayName + " against " + TextFormatting.GOLD + (winner == war.getVillage1() ? war.getVillage2().villageName : war.getVillage1().villageName) + TextFormatting.YELLOW + "! Final score: " + war.getScore1() + " - " + war.getScore2());
      } else {
         this.broadcastServerMessage(TextFormatting.RED + "[WAR] " + TextFormatting.YELLOW + "The " + war.getMode().displayName + " between " + war.getVillage1().villageName + " and " + war.getVillage2().villageName + " has ended in a draw! Score: " + war.getScore1() + " - " + war.getScore2());
      }

      this.distributeRewards(war, winner);
      this.warCooldowns.put(war.getVillage1().teamName, System.currentTimeMillis() + 14400000L);
      this.warCooldowns.put(war.getVillage2().teamName, System.currentTimeMillis() + 14400000L);
      if (war.getMode() == WarMode.DIVISION) {
         WarBattlefield.cancelCountdown();
         WarBattlefield.releaseArena();
      }

      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         if (war.getMode() == WarMode.DIVISION && war.getLobby() != null) {
            List<UUID> divisionPlayers = new ArrayList();
            divisionPlayers.addAll(war.getLobby().getRoster1());
            divisionPlayers.addAll(war.getLobby().getRoster2());
            WarBattlefield.returnPlayers(divisionPlayers, server.worlds[0]);
         }

         Set<UUID> allParticipants = new HashSet();
         allParticipants.addAll(war.getPlayerKills1().keySet());
         allParticipants.addAll(war.getPlayerKills2().keySet());
         if (war.getLobby() != null) {
            allParticipants.addAll(war.getLobby().getRoster1());
            allParticipants.addAll(war.getLobby().getRoster2());
         }

         for(UUID uuid : allParticipants) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
            if (player != null) {
               WarBattlefield.forceCleanup(player);
               server.getCommandManager().executeCommand(server, "clearcooldowns " + player.getName());
            }
         }
      }

      this.activeWars.remove(war.getWarId());
      PvpNetworkHelper.sendWarSyncToAll();
   }

   private void distributeRewards(WarInstance war, @Nullable VillageHelper.Village winner) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         this.distributeTeamRewards(server, war.getPlayerKills1(), winner == war.getVillage1(), war);
         this.distributeTeamRewards(server, war.getPlayerKills2(), winner == war.getVillage2(), war);
      }
   }

   private void distributeTeamRewards(MinecraftServer server, Map<UUID, Integer> playerKills, boolean isWinningTeam, WarInstance war) {
      if (!playerKills.isEmpty()) {
         List<Map.Entry<UUID, Integer>> sorted = new ArrayList(playerKills.entrySet());
         sorted.sort((a, b) -> ((Integer)b.getValue()).compareTo((Integer)a.getValue()));

         for(int i = 0; i < sorted.size(); ++i) {
            Map.Entry<UUID, Integer> entry = (Map.Entry)sorted.get(i);
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID((UUID)entry.getKey());
            if (player != null) {
               if ((Integer)entry.getValue() <= 0) {
                  int ryo = 5000;
                  int xp = 12;
                  grantRyo(player, ryo);
                  this.awardNinjaXp(player, xp);

                  try {
                     BattlePassManager.getInstance().awardXP((UUID)entry.getKey(), 50, "war", player.world);
                  } catch (Exception var12) {
                  }

                  player.sendMessage(new TextComponentString(TextFormatting.GOLD + "[WAR] " + TextFormatting.GREEN + "You earned " + xp + " Ninja XP + " + ryo + " Ryo!" + TextFormatting.GRAY + " (participation)"));
               } else {
                  int xp;
                  int ryo;
                  if (isWinningTeam) {
                     xp = 100;
                     ryo = 50000;
                     if (i < TOP_KILLER_BONUS.length) {
                        xp += TOP_KILLER_BONUS[i];
                     }

                     if (i < TOP_KILLER_RYO.length) {
                        ryo += TOP_KILLER_RYO[i];
                     }
                  } else {
                     xp = 25;
                     ryo = 15000;
                     if (i < TOP_KILLER_RYO.length) {
                        ryo += TOP_KILLER_RYO[i];
                     }
                  }

                  this.awardNinjaXp(player, xp);
                  grantRyo(player, ryo);

                  try {
                     BattlePassManager.getInstance().awardXP((UUID)entry.getKey(), 150, "war", player.world);
                     if (isWinningTeam) {
                        BattlePassManager.getInstance().awardXP((UUID)entry.getKey(), 100, "war", player.world);
                     }
                  } catch (Exception var13) {
                  }

                  String bonusText = "";
                  if (i < TOP_KILLER_BONUS.length) {
                     bonusText = " (Top " + (i + 1) + " killer bonus!)";
                  }

                  player.sendMessage(new TextComponentString(TextFormatting.GOLD + "[WAR] " + TextFormatting.GREEN + "You earned " + xp + " Ninja XP + " + ryo + " Ryo!" + bonusText + TextFormatting.GRAY + " (" + entry.getValue() + " kills)"));
               }
            }
         }

      }
   }

   private static void grantRyo(EntityPlayerMP player, int amount) {
      if (amount > 0) {
         RyoRewardHelper.grantRyoSilent(player, amount);
      }
   }

   private void awardNinjaXp(EntityPlayerMP player, int amount) {
      Scoreboard scoreboard = player.getWorldScoreboard();
      ScoreObjective objective = scoreboard.getObjective("ninja_xp");
      if (objective != null) {
         String playerName = player.getName();
         if (!scoreboard.entityHasObjective(playerName, objective)) {
            scoreboard.getOrCreateScore(playerName, objective);
         }

         int current = scoreboard.getOrCreateScore(playerName, objective).getScorePoints();
         scoreboard.getOrCreateScore(playerName, objective).setScorePoints(current + amount);
      }
   }

   @Nullable
   public WarInstance getActiveWar(VillageHelper.Village village) {
      for(WarInstance war : this.activeWars.values()) {
         if (war.getVillage1() == village || war.getVillage2() == village) {
            return war;
         }
      }

      return null;
   }

   public List<WarInstance> getAllActiveWars() {
      return new ArrayList(this.activeWars.values());
   }

   public boolean canDeclareWar(VillageHelper.Village village) {
      if (village == VillageHelper.Village.UNKNOWN) {
         return false;
      } else if (this.getActiveWar(village) != null) {
         return false;
      } else {
         Long cooldownExpiry = (Long)this.warCooldowns.get(village.teamName);
         return cooldownExpiry == null || System.currentTimeMillis() >= cooldownExpiry;
      }
   }

   public boolean isAtWar(VillageHelper.Village v1, VillageHelper.Village v2) {
      WarInstance war = this.findWarBetween(v1, v2);
      return war != null;
   }

   @Nullable
   public WarInstance findWarBetween(VillageHelper.Village v1, VillageHelper.Village v2) {
      for(WarInstance war : this.activeWars.values()) {
         if (war.getVillage1() == v1 && war.getVillage2() == v2 || war.getVillage1() == v2 && war.getVillage2() == v1) {
            return war;
         }
      }

      return null;
   }

   public float getWarBonus(VillageHelper.Village killerVillage, VillageHelper.Village victimVillage) {
      return this.isAtWar(killerVillage, victimVillage) ? 1.5F : 1.0F;
   }

   public boolean adminEndWar(String warId) {
      WarInstance war = (WarInstance)this.activeWars.get(warId);
      if (war == null) {
         return false;
      } else {
         this.endWar(war, true);
         return true;
      }
   }

   public void setCooldown(VillageHelper.Village village, long cooldownEndMs) {
      this.warCooldowns.put(village.teamName, cooldownEndMs);
   }

   public void clearCooldown(VillageHelper.Village village) {
      this.warCooldowns.remove(village.teamName);
   }

   public long getRemainingCooldown(VillageHelper.Village village) {
      Long expiry = (Long)this.warCooldowns.get(village.teamName);
      if (expiry == null) {
         return 0L;
      } else {
         long remaining = expiry - System.currentTimeMillis();
         return Math.max(0L, remaining);
      }
   }

   private void broadcastServerMessage(String message) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         server.getPlayerList().sendMessage(new TextComponentString(message));
      }

   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      NBTTagList warList = new NBTTagList();

      for(WarInstance war : this.activeWars.values()) {
         warList.appendTag(war.writeToNBT());
      }

      nbt.setTag("activeWars", warList);
      NBTTagCompound cooldownsNbt = new NBTTagCompound();

      for(Map.Entry<String, Long> entry : this.warCooldowns.entrySet()) {
         cooldownsNbt.setLong((String)entry.getKey(), (Long)entry.getValue());
      }

      nbt.setTag("cooldowns", cooldownsNbt);
      return nbt;
   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.activeWars.clear();
      this.warCooldowns.clear();
      if (nbt.hasKey("activeWars")) {
         NBTTagList warList = nbt.getTagList("activeWars", 10);

         for(int i = 0; i < warList.tagCount(); ++i) {
            WarInstance war = WarInstance.readFromNBT(warList.getCompoundTagAt(i));
            this.activeWars.put(war.getWarId(), war);
         }
      }

      if (nbt.hasKey("cooldowns")) {
         NBTTagCompound cooldownsNbt = nbt.getCompoundTag("cooldowns");

         for(String key : cooldownsNbt.getKeySet()) {
            this.warCooldowns.put(key, cooldownsNbt.getLong(key));
         }
      }

      System.out.println("[WAR DEBUG] readFromNBT: loaded " + this.activeWars.size() + " active wars, " + this.warCooldowns.size() + " cooldowns.");

      for(WarInstance war : this.activeWars.values()) {
         System.out.println("[WAR DEBUG]   War: " + war.getWarId() + " v1=" + war.getVillage1() + " v2=" + war.getVillage2() + " mode=" + war.getMode());
      }

      for(Map.Entry<String, Long> cd : this.warCooldowns.entrySet()) {
         long remaining = (Long)cd.getValue() - System.currentTimeMillis();
         System.out.println("[WAR DEBUG]   Cooldown: " + (String)cd.getKey() + " remaining=" + remaining + "ms" + (remaining <= 0L ? " (EXPIRED)" : ""));
      }

   }
}
