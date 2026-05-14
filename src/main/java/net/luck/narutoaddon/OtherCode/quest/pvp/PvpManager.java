
package net.luck.narutoaddon.OtherCode.quest.pvp;

import net.luck.narutoaddon.OtherCode.quest.core.QuestDefinition;
import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.network.PvpNetworkHelper;
import net.luck.narutoaddon.OtherCode.quest.pvp.war.WarManager;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassManager;
import net.luck.narutoaddon.OtherCode.stat.core.StatManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.narutomod.PlayerTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PvpManager {
   private static PvpManager instance;
   private static final String[] PVP_DAILY_SUB_SLOTS = new String[]{"pvp_daily_0", "pvp_daily_1", "pvp_daily_2"};
   private static final String[] PVP_WEEKLY_SUB_SLOTS = new String[]{"pvp_weekly_0", "pvp_weekly_1", "pvp_weekly_2"};
   private static final String PVP_RANDOM_SLOT = "pvp_random";
   private static final String PVP_LEADERSHIP_SLOT = "pvp_leadership";
   private static final String PVP_ASSIGNED_SLOT = "pvp_assigned";
   private static final String[] ALL_PVP_SUB_SLOTS = new String[]{"pvp_daily_0", "pvp_daily_1", "pvp_daily_2", "pvp_weekly_0", "pvp_weekly_1", "pvp_weekly_2", "pvp_random"};
   private final Map<UUID, Map<String, PvpMissionOffer>> currentOffers = new HashMap();
   private int tickCounter = 0;
   private final Map<String, Long> offlineTargetTimestamps = new HashMap();
   private static final long OFFLINE_TARGET_REROLL_MS = 600000L;
   private static final Random RYO_RAND = new Random();

   public static PvpManager getInstance() {
      if (instance == null) {
         instance = new PvpManager();
      }

      return instance;
   }

   public static void reset() {
      instance = null;
   }

   public void onServerTick(World world) {
      ++this.tickCounter;
      this.tickMissionTimers(world);

      try {
         WarManager.getInstance().tickWars(world);
      } catch (Exception e) {
         System.err.println("[WAR] Error in tickWars: " + e.getMessage());
         e.printStackTrace();
      }

      if (this.tickCounter % 20 == 0) {
         this.tickAntiCheese(world);
      }

      if (this.tickCounter % 600 == 0) {
         this.tickOfflineTargetCheck(world);
      }

      if (this.tickCounter % 1200 == 0) {
         AntiCheeseValidator.cleanupExpired();
         VillageOrderManager.getInstance().tickOrders(world);
      }

      if (this.tickCounter % 432000 == 0 || this.tickCounter == 1) {
         this.tickBingoRefresh(world);
      }

   }

   private void tickBingoRefresh(World world) {
      try {
         PvpSavedData data = PvpSavedData.get(world);
         long lastRefresh = data.getLastBingoRotation();
         long elapsed = System.currentTimeMillis() - lastRefresh;
         if (lastRefresh == 0L || elapsed >= 21600000L) {
            BingoBook.getInstance().refresh(world);
         }
      } catch (Exception var7) {
      }

   }

   private void tickMissionTimers(World world) {
      PvpSavedData data = PvpSavedData.get(world);
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            UUID playerId = player.getUniqueID();
            Map<String, PvpMissionInstance> slots = data.getActiveInstances(playerId);

            for(Map.Entry<String, PvpMissionInstance> entry : slots.entrySet()) {
               PvpMissionInstance inst = (PvpMissionInstance)entry.getValue();
               if (!inst.isCompleted() && !inst.isFailed()) {
                  inst.tickSurvival();
                  if (inst.isCompleted()) {
                     this.completeMission(player, (String)entry.getKey(), world);
                  }
               }
            }
         }

      }
   }

   private void tickAntiCheese(World world) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            AntiCheeseValidator.onPlayerTick(player);
         }

      }
   }

   private void tickOfflineTargetCheck(World world) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         PvpSavedData data = PvpSavedData.get(world);
         long now = System.currentTimeMillis();

         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            UUID playerId = player.getUniqueID();
            Map<String, PvpMissionInstance> slots = data.getActiveInstances(playerId);

            for(Map.Entry<String, PvpMissionInstance> entry : slots.entrySet()) {
               String subSlot = (String)entry.getKey();
               PvpMissionInstance inst = (PvpMissionInstance)entry.getValue();
               if (!inst.isCompleted() && !inst.isFailed()) {
                  PvpObjective.ObjectiveType type = inst.getObjective().getType();
                  if (type == PvpObjective.ObjectiveType.KILL_SPECIFIC_PLAYER || type == PvpObjective.ObjectiveType.MUTUAL_HUNT) {
                     UUID targetId = inst.getObjective().getTargetPlayerId();
                     if (targetId != null) {
                        EntityPlayerMP targetPlayer = server.getPlayerList().getPlayerByUUID(targetId);
                        String trackingKey = playerId.toString() + ":" + subSlot;
                        if (targetPlayer == null) {
                           if (!this.offlineTargetTimestamps.containsKey(trackingKey)) {
                              this.offlineTargetTimestamps.put(trackingKey, now);
                           } else {
                              long offlineSince = (Long)this.offlineTargetTimestamps.get(trackingKey);
                              if (now - offlineSince >= 600000L) {
                                 data.removeActiveInstance(playerId, subSlot);
                                 this.offlineTargetTimestamps.remove(trackingKey);
                                 PvpMissionOffer newOffer = PvpMissionGenerator.generateOffer(playerId, subSlot, world);
                                 if (newOffer != null) {
                                    data.saveOffer(playerId, subSlot, newOffer);
                                 }

                                 player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "[PvP] " + TextFormatting.GRAY + "Your target went offline. Mission auto-rerolled."));
                                 this.syncToClient(player);
                              }
                           }
                        } else {
                           this.offlineTargetTimestamps.remove(trackingKey);
                        }
                     }
                  }
               }
            }
         }

      }
   }

   public void onPlayerLogin(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      World world = player.world;
      PvpSavedData data = PvpSavedData.get(world);
      Map<String, PvpMissionInstance> savedInstances = data.getActiveInstances(playerId);

      for(String subSlot : ALL_PVP_SUB_SLOTS) {
         if ((savedInstances == null || !savedInstances.containsKey(subSlot)) && !data.isSlotOnCooldown(playerId, subSlot)) {
            this.getCurrentOffer(playerId, subSlot, world);
         }
      }

      try {
         NBTTagCompound bingoNBT = data.getBingoBookNBT();
         if (BingoBook.getInstance().getEntryCount() == 0 && bingoNBT != null && bingoNBT.getSize() > 0) {
            BingoBook.getInstance().readFromNBT(bingoNBT);
         }
      } catch (Exception var10) {
      }

      this.syncToClient(player);
   }

   public void onPlayerLogout(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      this.currentOffers.remove(playerId);
      AntiCheeseValidator.onPlayerLogout(playerId);
   }

   public void onPlayerDeath(EntityPlayerMP player) {
      World world = player.world;
      PvpSavedData data = PvpSavedData.get(world);
      UUID playerId = player.getUniqueID();
      Map<String, PvpMissionInstance> slots = data.getActiveInstances(playerId);

      for(Map.Entry<String, PvpMissionInstance> entry : slots.entrySet()) {
         PvpMissionInstance inst = (PvpMissionInstance)entry.getValue();
         if (!inst.isCompleted() && !inst.isFailed()) {
            inst.onDeath();
            if (inst.isFailed()) {
               this.failMission(player, (String)entry.getKey(), world);
            }
         }
      }

      AntiCheeseValidator.onPlayerDeath(player);
   }

   public void onValidPvpKill(PvpKillTracker.KillData killData) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         EntityPlayerMP killer = server.getPlayerList().getPlayerByUUID(killData.killerId);
         if (killer != null) {
            World world = killer.world;
            PvpSavedData data = PvpSavedData.get(world);
            Map<String, PvpMissionInstance> slots = data.getActiveInstances(killData.killerId);
            boolean anyCompleted = false;

            for(Map.Entry<String, PvpMissionInstance> entry : slots.entrySet()) {
               PvpMissionInstance inst = (PvpMissionInstance)entry.getValue();
               if (!inst.isCompleted() && !inst.isFailed()) {
                  boolean usedNature = killData.natureType > 0;
                  boolean usedDojutsu = false;
                  boolean victimHigherLevel = killData.victimHigherLevel;
                  boolean victimHasDojutsu = killData.victimHasDojutsu;
                  String rejection = inst.onKill(killData.victimId, killData.victimVillage != null ? killData.victimVillage.teamName : null, usedNature, usedDojutsu, victimHigherLevel, victimHasDojutsu, killData.natureType);
                  if (rejection != null) {
                     killer.sendMessage(new TextComponentString(TextFormatting.DARK_GRAY + "[" + inst.getName() + "] " + TextFormatting.GRAY + "Kill not counted: " + TextFormatting.YELLOW + rejection));
                  }

                  if (inst.isCompleted()) {
                     anyCompleted = true;
                     this.completeMission(killer, (String)entry.getKey(), world);
                  }
               }
            }

            PvpMissionInstance leadershipMission = data.getLeadershipInstance(killData.killerId);
            if (leadershipMission != null && !leadershipMission.isCompleted() && !leadershipMission.isFailed()) {
               boolean usedNatureLdr = killData.natureType > 0;
               String ldrRejection = leadershipMission.onKill(killData.victimId, killData.victimVillage != null ? killData.victimVillage.teamName : null, usedNatureLdr, false, killData.victimHigherLevel, killData.victimHasDojutsu, killData.natureType);
               if (ldrRejection != null) {
                  killer.sendMessage(new TextComponentString(TextFormatting.DARK_GRAY + "[" + leadershipMission.getName() + "] " + TextFormatting.GRAY + "Kill not counted: " + TextFormatting.YELLOW + ldrRejection));
               }

               if (leadershipMission.isComplete()) {
                  this.completeLeadershipMission(killer, world);
               }
            }

            data.addPvpXp(killData.killerId, this.calculatePvpXp(killData));
            AntiCheeseValidator.recordKill(killData.killerId, killData.victimId);
            VillageHelper.Village killerVillage = killData.killerVillage;
            if (killerVillage != null && killerVillage != VillageHelper.Village.UNKNOWN) {
               VillageOrder matchingOrder = VillageOrderManager.getInstance().getMatchingOrder(killerVillage.teamName, killData.victimId, killData.victimVillage != null ? killData.victimVillage.teamName : null, world);
               if (matchingOrder != null) {
                  data.addPvpXp(killData.killerId, (long)matchingOrder.getBonusPvpXp());
                  addNinjaXp(killer, matchingOrder.getBonusNinjaXp());
                  killer.sendMessage(new TextComponentString("§6[ORDER] §aVillage Order bonus! +" + matchingOrder.getBonusPvpXp() + " PvP XP, +" + matchingOrder.getBonusNinjaXp() + " Ninja XP"));
               }
            }

            try {
               BingoBook.BingoEntry bingoEntry = BingoBook.getInstance().onTargetKilled(killData.killerId, killData.victimId);
               if (bingoEntry != null) {
                  addNinjaXp(killer, bingoEntry.ninjaXpReward);
                  data.addPvpXp(killData.killerId, (long)bingoEntry.pvpXpReward);
                  if (bingoEntry.ryoReward > 0) {
                     grantRyo(killer, bingoEntry.ryoReward);
                  }

                  String ryoText = bingoEntry.ryoReward > 0 ? ", +" + bingoEntry.ryoReward + " Ryo" : "";
                  killer.sendMessage(new TextComponentString(TextFormatting.GOLD + "[BINGO BOOK] " + TextFormatting.GREEN + "Target eliminated: " + bingoEntry.targetName + "! +" + bingoEntry.ninjaXpReward + " Ninja XP, +" + bingoEntry.pvpXpReward + " PvP XP" + ryoText));

                  for(EntityPlayerMP p : server.getPlayerList().getPlayers()) {
                     this.syncToClient(p);
                  }
               }
            } catch (Exception var16) {
            }

            this.syncToClient(killer);
         }
      }
   }

   private long calculatePvpXp(PvpKillTracker.KillData killData) {
      return 5L;
   }

   public PvpMissionOffer getCurrentOffer(UUID playerId, String subSlot, World world) {
      Map<String, PvpMissionOffer> playerOffers = (Map)this.currentOffers.get(playerId);
      if (playerOffers != null && playerOffers.containsKey(subSlot)) {
         PvpMissionOffer cached = (PvpMissionOffer)playerOffers.get(subSlot);
         if (!PvpSavedData.isOfferStale(subSlot, cached.getCreatedAt())) {
            return cached;
         }
      }

      PvpSavedData data = PvpSavedData.get(world);
      PvpMissionOffer saved = data.getSavedOffer(playerId, subSlot);
      if (saved != null && !PvpSavedData.isOfferStale(subSlot, saved.getCreatedAt())) {
         ((Map)this.currentOffers.computeIfAbsent(playerId, (k) -> new HashMap())).put(subSlot, saved);
         return saved;
      } else {
         return this.generateOffer(playerId, subSlot, world);
      }
   }

   public PvpMissionOffer generateOffer(UUID playerId, String subSlot, World world) {
      PvpMissionOffer offer = PvpMissionGenerator.generateOffer(playerId, subSlot, world);
      if (offer == null) {
         return null;
      } else {
         ((Map)this.currentOffers.computeIfAbsent(playerId, (k) -> new HashMap())).put(subSlot, offer);
         PvpSavedData data = PvpSavedData.get(world);
         data.saveOffer(playerId, subSlot, offer);
         return offer;
      }
   }

   public PvpMissionOffer rerollOffer(UUID playerId, String subSlot, World world) {
      PvpSavedData data = PvpSavedData.get(world);
      String baseCategory = getBaseCategory(subSlot);
      return !data.useReroll(playerId, baseCategory) ? null : this.generateOffer(playerId, subSlot, world);
   }

   public boolean acceptOffer(UUID playerId, String subSlot, World world) {
      PvpSavedData data = PvpSavedData.get(world);
      if (data.getActiveInstance(playerId, subSlot) != null) {
         return false;
      } else if (data.isSlotOnCooldown(playerId, subSlot)) {
         return false;
      } else {
         PvpMissionOffer offer = this.getCurrentOffer(playerId, subSlot, world);
         if (offer == null) {
            return false;
         } else {
            PvpMissionInstance instance = PvpMissionInstance.fromOffer(offer);
            data.setActiveInstance(playerId, subSlot, instance);
            data.removeOffer(playerId, subSlot);
            Map<String, PvpMissionOffer> cached = (Map)this.currentOffers.get(playerId);
            if (cached != null) {
               cached.remove(subSlot);
            }

            return true;
         }
      }
   }

   public void abandonMission(UUID playerId, String subSlot, World world) {
      PvpSavedData data = PvpSavedData.get(world);
      PvpMissionInstance inst = data.getActiveInstance(playerId, subSlot);
      if (inst != null) {
         data.removeActiveInstance(playerId, subSlot);
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         EntityPlayerMP player = server != null ? server.getPlayerList().getPlayerByUUID(playerId) : null;
         if (player != null) {
            player.sendMessage(new TextComponentString(TextFormatting.RED + "[PvP] " + TextFormatting.GRAY + "Mission abandoned."));
         }

      }
   }

   private void completeMission(EntityPlayerMP player, String subSlot, World world) {
      PvpSavedData data = PvpSavedData.get(world);
      UUID playerId = player.getUniqueID();
      PvpMissionInstance inst = data.getActiveInstance(playerId, subSlot);
      if (inst != null) {
         inst.markCompleted();
         int ninjaXp = inst.getNinjaXpReward();
         if (ninjaXp > 0) {
            addNinjaXp(player, ninjaXp);
         }

         int pvpXp = inst.getPvpXpReward();
         boolean rankedUp = data.addPvpXp(playerId, (long)pvpXp);
         int ryoReward = inst.getRyoReward();
         if (ryoReward <= 0) {
            ryoReward = this.calculateMissionRyo(subSlot, inst.getRank());
         }

         if (ryoReward > 0) {
            grantRyo(player, ryoReward);
         }

         StatManager statMgr = StatManager.getInstance();
         int spReward = statMgr.getSPForRank(inst.getRank().ordinal());
         if (spReward > 0) {
            statMgr.grantSP(player, spReward);
         }

         try {
            BattlePassManager.getInstance().awardXP(playerId, 75, "ranked_pvp", player.world);
         } catch (Exception var17) {
         }

         this.checkFirstPvpWinBonus(player);
         String cmdReward = inst.getCommandReward();
         if (cmdReward != null && !cmdReward.isEmpty()) {
            String resolved = cmdReward.replace("{player}", player.getName());
            player.getServer().getCommandManager().executeCommand(player.getServer(), resolved);
         }

         data.recordSlotCompletion(playerId, subSlot);
         data.removeActiveInstance(playerId, subSlot);
         if ("pvp_assigned".equals(subSlot) && inst.getOperationOrderId() != null) {
            OperationTracker tracker = data.getOperationTracker(inst.getOperationOrderId());
            if (tracker != null && !tracker.isBonusPaid()) {
               tracker.recordTierCompletion(playerId, inst.getRank());
               data.setOperationTracker(inst.getOperationOrderId(), tracker);
               int completedTiers = tracker.getCompletedTierCount();
               this.broadcastOperationProgress(tracker, completedTiers, world);
               if (tracker.isFullCompletion()) {
                  this.payOperationBonus(tracker, world);
               }
            }
         }

         String ryoText = ryoReward > 0 ? ", +" + ryoReward + " Ryo" : "";
         String spText = spReward > 0 ? ", +" + spReward + " SP" : "";
         player.sendMessage(new TextComponentString(TextFormatting.GREEN + "[PvP] " + TextFormatting.GOLD + "Mission Complete: " + TextFormatting.WHITE + inst.getName() + TextFormatting.GRAY + " (+" + ninjaXp + " Ninja XP, +" + pvpXp + " PvP XP" + ryoText + spText + ")"));
         if (rankedUp) {
            QuestDefinition.QuestRank newRank = data.getPvpRank(playerId);
            player.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "[PvP] " + TextFormatting.GOLD + "PvP Rank Up! You are now " + newRank.displayName + " rank!"));
         }

         this.syncToClient(player);
      }
   }

   private void failMission(EntityPlayerMP player, String subSlot, World world) {
      PvpSavedData data = PvpSavedData.get(world);
      UUID playerId = player.getUniqueID();
      PvpMissionInstance inst = data.getActiveInstance(playerId, subSlot);
      if (inst != null) {
         inst.markFailed();
         data.removeActiveInstance(playerId, subSlot);

         try {
            BattlePassManager.getInstance().awardXP(playerId, 20, "ranked_pvp", player.world);
         } catch (Exception var8) {
         }

         player.sendMessage(new TextComponentString(TextFormatting.RED + "[PvP] " + TextFormatting.GRAY + "Mission Failed: " + TextFormatting.WHITE + inst.getName()));
         this.syncToClient(player);
      }
   }

   public Map<String, PvpMissionInstance> getActiveInstances(UUID playerId, World world) {
      return PvpSavedData.get(world).getActiveInstances(playerId);
   }

   public int getRerollsRemaining(UUID playerId, String subSlot, World world) {
      return PvpSavedData.get(world).getRerollsRemaining(playerId, getBaseCategory(subSlot));
   }

   private void syncToClient(EntityPlayerMP player) {
      try {
         PvpNetworkHelper.sendPvpSync(player);
      } catch (Exception var3) {
      }

   }

   public PvpMissionOffer getLeadershipOffer(UUID playerId, World world) {
      PvpSavedData data = PvpSavedData.get(world);
      PvpMissionOffer existing = data.getLeadershipOffer(playerId);
      if (existing != null) {
         return existing;
      } else {
         PvpMissionOffer offer = PvpMissionGenerator.generateLeadershipOffer(playerId, world);
         if (offer != null) {
            data.saveLeadershipOffer(playerId, offer);
         }

         return offer;
      }
   }

   public boolean acceptLeadershipMission(UUID playerId, World world) {
      PvpSavedData data = PvpSavedData.get(world);
      if (data.getLeadershipInstance(playerId) != null) {
         return false;
      } else {
         PvpMissionOffer offer = data.getLeadershipOffer(playerId);
         if (offer == null) {
            return false;
         } else {
            PvpMissionInstance instance = PvpMissionInstance.fromOffer(offer);
            data.setLeadershipInstance(playerId, instance);
            data.removeLeadershipOffer(playerId);
            return true;
         }
      }
   }

   public PvpMissionOffer rerollLeadershipOffer(UUID playerId, World world) {
      PvpMissionOffer offer = PvpMissionGenerator.generateLeadershipOffer(playerId, world);
      if (offer != null) {
         PvpSavedData data = PvpSavedData.get(world);
         data.saveLeadershipOffer(playerId, offer);
      }

      return offer;
   }

   public void abandonLeadershipMission(UUID playerId, World world) {
      PvpSavedData data = PvpSavedData.get(world);
      data.removeLeadershipInstance(playerId);
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      EntityPlayerMP player = server != null ? server.getPlayerList().getPlayerByUUID(playerId) : null;
      if (player != null) {
         player.sendMessage(new TextComponentString(TextFormatting.RED + "[PvP] " + TextFormatting.GRAY + "Leadership mission abandoned."));
      }

   }

   public boolean acceptAssignedMission(UUID playerId, String orderId, World world) {
      PvpSavedData data = PvpSavedData.get(world);
      if (data.getActiveInstance(playerId, "pvp_assigned") != null) {
         this.sendPlayerMessage(playerId, TextFormatting.RED + "[PvP] " + TextFormatting.GRAY + "You already have an assigned mission active.");
         return false;
      } else {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server == null) {
            return false;
         } else {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
            if (player == null) {
               return false;
            } else {
               VillageHelper.Village village = VillageHelper.getVillage(player);
               if (village == VillageHelper.Village.UNKNOWN) {
                  return false;
               } else {
                  VillageOrder order = VillageOrderManager.getInstance().getAssignedMissionOrder(village.teamName, orderId, world);
                  if (order != null && !order.isExpired()) {
                     PvpMissionTemplate template = PvpMissionTemplate.getLeadershipById(order.getAssignedTemplateId());
                     if (template == null) {
                        return false;
                     } else {
                        PvpMissionOffer offer = PvpMissionGenerator.generateOfferFromTemplate(template, playerId, world, order.getOrderId());
                        if (offer == null) {
                           return false;
                        } else {
                           PvpMissionInstance instance = PvpMissionInstance.fromOffer(offer);
                           data.setActiveInstance(playerId, "pvp_assigned", instance);
                           OperationTracker tracker = data.getOperationTracker(order.getOrderId());
                           if (tracker == null) {
                              tracker = new OperationTracker(order.getOrderId(), template.getId(), village.teamName);
                           }

                           tracker.addParticipant(playerId);
                           data.setOperationTracker(order.getOrderId(), tracker);
                           player.sendMessage(new TextComponentString(TextFormatting.GREEN + "[PvP] " + TextFormatting.GOLD + "Assigned Mission Accepted: " + TextFormatting.WHITE + offer.getName()));
                           this.syncToClient(player);
                           return true;
                        }
                     }
                  } else {
                     this.sendPlayerMessage(playerId, TextFormatting.RED + "[PvP] " + TextFormatting.GRAY + "That assigned mission is no longer available.");
                     return false;
                  }
               }
            }
         }
      }
   }

   private void sendPlayerMessage(UUID playerId, String message) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         EntityPlayerMP p = server.getPlayerList().getPlayerByUUID(playerId);
         if (p != null) {
            p.sendMessage(new TextComponentString(message));
         }
      }

   }

   private void completeLeadershipMission(EntityPlayerMP player, World world) {
      UUID playerId = player.getUniqueID();
      PvpSavedData data = PvpSavedData.get(world);
      PvpMissionInstance inst = data.getLeadershipInstance(playerId);
      if (inst != null) {
         inst.markCompleted();
         int ninjaXp = inst.getNinjaXpReward();
         if (ninjaXp > 0) {
            addNinjaXp(player, ninjaXp);
         }

         int pvpXp = inst.getPvpXpReward();
         boolean rankedUp = data.addPvpXp(playerId, (long)pvpXp);
         int leadershipRyo = inst.getRyoReward();
         if (leadershipRyo <= 0) {
            leadershipRyo = 600 + RYO_RAND.nextInt(1201);
         }

         if (leadershipRyo > 0) {
            grantRyo(player, leadershipRyo);
         }

         StatManager leaderStatMgr = StatManager.getInstance();
         int leaderSpReward = leaderStatMgr.getSPForRank(inst.getRank().ordinal());
         if (leaderSpReward > 0) {
            leaderStatMgr.grantSP(player, leaderSpReward);
         }

         this.checkFirstPvpWinBonus(player);
         String cmdReward = inst.getCommandReward();
         if (cmdReward != null && !cmdReward.isEmpty()) {
            String resolved = cmdReward.replace("{player}", player.getName());
            player.getServer().getCommandManager().executeCommand(player.getServer(), resolved);
         }

         data.removeLeadershipInstance(playerId);
         String ryoText = leadershipRyo > 0 ? ", +" + leadershipRyo + " Ryo" : "";
         player.sendMessage(new TextComponentString(TextFormatting.GREEN + "[PvP] " + TextFormatting.GOLD + "Leadership Mission Complete: " + TextFormatting.WHITE + inst.getName() + TextFormatting.GRAY + " (+" + ninjaXp + " Ninja XP, +" + pvpXp + " PvP XP" + ryoText + ")"));
         if (rankedUp) {
            QuestDefinition.QuestRank newRank = data.getPvpRank(playerId);
            player.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "[PvP] " + TextFormatting.GOLD + "PvP Rank Up! You are now " + newRank.displayName + " rank!"));
         }

         this.syncToClient(player);
      }
   }

   private void payOperationBonus(OperationTracker tracker, World world) {
      if (!tracker.isBonusPaid()) {
         float multiplier = tracker.getBonusMultiplier();
         if (!(multiplier <= 0.0F)) {
            int bonusNinjaXp = Math.round(200.0F * multiplier);
            int bonusPvpXp = Math.round(100.0F * multiplier);
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               PvpSavedData data = PvpSavedData.get(world);

               for(UUID participantId : tracker.getParticipants()) {
                  data.addPvpXp(participantId, (long)bonusPvpXp);
                  EntityPlayerMP participant = server.getPlayerList().getPlayerByUUID(participantId);
                  if (participant != null) {
                     addNinjaXp(participant, bonusNinjaXp);
                     int tierCount = tracker.getCompletedTierCount();
                     participant.sendMessage(new TextComponentString(TextFormatting.GOLD + "[Operation] " + TextFormatting.GREEN + "Village operation complete! " + tierCount + "/6 tiers finished. " + TextFormatting.YELLOW + "Bonus: +" + bonusNinjaXp + " Ninja XP, +" + bonusPvpXp + " PvP XP"));
                  }
               }

               tracker.markBonusPaid();
               data.setOperationTracker(tracker.getOrderId(), tracker);
            }
         }
      }
   }

   private void broadcastOperationProgress(OperationTracker tracker, int completedTiers, World world) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         String villageName = tracker.getVillageName();

         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            VillageHelper.Village pVillage = VillageHelper.getVillage(player);
            if (pVillage.teamName.equals(villageName)) {
               player.sendMessage(new TextComponentString(TextFormatting.GOLD + "[Operation] " + TextFormatting.AQUA + "Village operation progress: " + completedTiers + "/6 tiers complete!"));
            }
         }

      }
   }

   private int calculateMissionRyo(String subSlot, QuestDefinition.QuestRank rank) {
      if (subSlot.equals("pvp_assigned")) {
         return 300 + RYO_RAND.nextInt(601);
      } else {
         String baseType;
         if (subSlot.startsWith("pvp_daily")) {
            baseType = "daily";
         } else if (subSlot.startsWith("pvp_weekly")) {
            baseType = "weekly";
         } else {
            if (!subSlot.equals("pvp_random")) {
               return 0;
            }

            baseType = "random";
         }

         int min;
         int max;
         switch (rank) {
            case C:
               min = "daily".equals(baseType) ? 180 : ("weekly".equals(baseType) ? 800 : 70);
               max = "daily".equals(baseType) ? 400 : ("weekly".equals(baseType) ? 1400 : 140);
               break;
            case B:
               min = "daily".equals(baseType) ? 350 : ("weekly".equals(baseType) ? 1400 : 140);
               max = "daily".equals(baseType) ? 700 : ("weekly".equals(baseType) ? 2400 : 300);
               break;
            case A:
               min = "daily".equals(baseType) ? 600 : ("weekly".equals(baseType) ? 2400 : 250);
               max = "daily".equals(baseType) ? 1100 : ("weekly".equals(baseType) ? 4200 : 500);
               break;
            case S:
            case S_PLUS:
               min = "daily".equals(baseType) ? 900 : ("weekly".equals(baseType) ? 4200 : 400);
               max = "daily".equals(baseType) ? 1800 : ("weekly".equals(baseType) ? 7200 : 750);
               break;
            default:
               min = "daily".equals(baseType) ? 100 : ("weekly".equals(baseType) ? 400 : 35);
               max = "daily".equals(baseType) ? 220 : ("weekly".equals(baseType) ? 700 : 70);
         }

         return min + RYO_RAND.nextInt(max - min + 1);
      }
   }

   private static void grantRyo(EntityPlayerMP player, int amount) {
      if (amount > 0) {
         RyoRewardHelper.grantRyoSilent(player, amount);
      }
   }

   private void checkFirstPvpWinBonus(EntityPlayerMP player) {
      try {
         World world = player.world;
         if (world.isRemote) {
            return;
         }

         ShopSavedData shopData = ShopSavedData.get(world);
         if (shopData.canClaimFirstPvpWin(player.getUniqueID())) {
            shopData.claimFirstPvpWin(player.getUniqueID());
            grantRyo(player, 300);
            player.sendMessage(new TextComponentString(TextFormatting.GOLD + "[PvP] " + TextFormatting.GREEN + "First PvP win of the day! +" + 300 + " Ryo bonus!"));
         }
      } catch (Exception var4) {
      }

   }

   private static String getBaseCategory(String subSlot) {
      if (subSlot.startsWith("pvp_daily")) {
         return "pvp_daily";
      } else {
         return subSlot.startsWith("pvp_weekly") ? "pvp_weekly" : "pvp_random";
      }
   }

   public static String[] getAllSubSlots() {
      return ALL_PVP_SUB_SLOTS;
   }

   public static String[] getDailySubSlots() {
      return PVP_DAILY_SUB_SLOTS;
   }

   public static String[] getWeeklySubSlots() {
      return PVP_WEEKLY_SUB_SLOTS;
   }

   public static String getRandomSlot() {
      return "pvp_random";
   }

   private static void addNinjaXp(EntityPlayerMP player, int amount) {
      try {
         PlayerTracker.addBattleXp(player, (double)amount);
      } catch (Exception var6) {
         try {
            double current = player.getEntityData().getDouble("battle_experience");
            player.getEntityData().setDouble("battle_experience", Math.min(current + (double)amount, (double)100000.0F));
         } catch (Exception var5) {
         }
      }

   }
}
