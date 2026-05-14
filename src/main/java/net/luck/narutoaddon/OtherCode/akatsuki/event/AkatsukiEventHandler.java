package net.luck.narutoaddon.OtherCode.akatsuki.event;

import net.luck.narutoaddon.OtherCode.akatsuki.bounty.BountyEntry;
import net.luck.narutoaddon.OtherCode.akatsuki.bounty.BountyManager;
import net.luck.narutoaddon.OtherCode.akatsuki.bounty.HeatTracker;
import net.luck.narutoaddon.OtherCode.akatsuki.contract.ContractManager;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiMember;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiSavedData;
import net.luck.narutoaddon.OtherCode.akatsuki.market.BlackMarketManager;
import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.endgame.PveRank;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AkatsukiEventHandler {
   private static final String LOG_PREFIX = "[AkatsukiBounty] ";
   private int tickCounter = 0;
   private static final Map<UUID, Long> recentBountyClaims = new ConcurrentHashMap();
   private static final long BOUNTY_CLAIM_COOLDOWN_MS = 1800000L;
   private static final Map<String, Long> recentBountyKills = new ConcurrentHashMap();
   private static final long BOUNTY_KILL_COOLDOWN_MS = 1800000L;
   private static final Map<UUID, Long> lastCombatActivity = new ConcurrentHashMap();

   private static boolean isAkatsuki(EntityPlayerMP player) {
      Team team = player.getTeam();
      return team != null && team.getName().equalsIgnoreCase("akatsuki");
   }

   public static void markCombatActivity(UUID playerId) {
      lastCombatActivity.put(playerId, System.currentTimeMillis());
   }

   private static boolean wasInCombatRecently(UUID playerId) {
      Long lastTime = (Long)lastCombatActivity.get(playerId);
      return lastTime != null && System.currentTimeMillis() - lastTime < 60000L;
   }

   @SubscribeEvent
   public void onLivingHurt(LivingHurtEvent event) {
      if (event.getSource() != null && event.getSource().getTrueSource() instanceof EntityPlayerMP && event.getEntityLiving() instanceof EntityPlayerMP) {
         markCombatActivity(event.getSource().getTrueSource().getUniqueID());
         markCombatActivity(event.getEntityLiving().getUniqueID());
      }

   }

   @SubscribeEvent
   public void onLivingDeath(LivingDeathEvent event) {
      if (event.getEntityLiving() instanceof EntityPlayerMP) {
         if (event.getSource().getTrueSource() != null) {
            if (event.getSource().getTrueSource() instanceof EntityPlayerMP) {
               EntityPlayerMP victim = (EntityPlayerMP)event.getEntityLiving();
               EntityPlayerMP killer = (EntityPlayerMP)event.getSource().getTrueSource();
               World world = victim.getServerWorld();
               AkatsukiSavedData data = AkatsukiSavedData.get(world);
               if (data != null) {
                  markCombatActivity(killer.getUniqueID());
                  markCombatActivity(victim.getUniqueID());
                  boolean killerIsAkatsuki = isAkatsuki(killer);
                  boolean victimIsAkatsuki = isAkatsuki(victim);
                  System.out.println("[AkatsukiBounty] PvP kill: " + killer.getName() + " (akatsuki=" + killerIsAkatsuki + ") killed " + victim.getName() + " (akatsuki=" + victimIsAkatsuki + ")");
                  boolean bountyClaimHandled = false;
                  if (killerIsAkatsuki && !victimIsAkatsuki) {
                     AkatsukiMember member = data.getMember(killer.getUniqueID());
                     if (member == null) {
                        System.out.println("[AkatsukiBounty] WARNING: " + killer.getName() + " is on akatsuki scoreboard team but has no AkatsukiMember saved data — skipping Akatsuki-specific rewards, will still try bounty claim");
                     }

                     if (member != null) {
                        VillageHelper.Village victimVillage = VillageHelper.getVillage(victim);
                        String villageName = victimVillage.teamName;
                        String killKey = killer.getUniqueID().toString() + ":" + victim.getUniqueID().toString();
                        Long lastKillTime = (Long)recentBountyKills.get(killKey);
                        boolean killOnCooldown = lastKillTime != null && System.currentTimeMillis() - lastKillTime < 1800000L;
                        if (!killOnCooldown) {
                           recentBountyKills.put(killKey, System.currentTimeMillis());
                           EndgameSavedData endgameData = EndgameSavedData.get(world);
                           PveRank victimRank = endgameData.getPveRank(victim.getUniqueID());
                           Random rand = new Random();
                           int bountyIncrease;
                           switch (victimRank) {
                              case KAGE:
                                 bountyIncrease = 800 + rand.nextInt(801);
                                 break;
                              case ANBU:
                                 bountyIncrease = 400 + rand.nextInt(401);
                                 break;
                              case JONIN:
                                 bountyIncrease = 200 + rand.nextInt(201);
                                 break;
                              case CHUNIN:
                                 bountyIncrease = 100 + rand.nextInt(101);
                                 break;
                              default:
                                 bountyIncrease = 50 + rand.nextInt(51);
                           }

                           BountyManager.autoIncreaseBounty(data, killer.getUniqueID(), killer.getName(), bountyIncrease);
                           killer.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "☠ Bounty +" + bountyIncrease + " Ryo " + TextFormatting.GRAY + "on your head for killing " + TextFormatting.WHITE + victim.getName()));
                        } else {
                           long remaining = 1800000L - (System.currentTimeMillis() - lastKillTime);
                           int minsLeft = (int)(remaining / 60000L) + 1;
                           killer.sendMessage(new TextComponentString(TextFormatting.GRAY + "No bounty increase — you recently killed " + TextFormatting.WHITE + victim.getName() + TextFormatting.GRAY + " (" + minsLeft + " min cooldown)"));
                        }

                        if (!villageName.equals("Unknown")) {
                           HeatTracker.addHeat(member, villageName, 15);
                        }

                        member.addReputation(5);
                        data.markDirty();
                        String ringKanji = member.getRing().kanji;
                        String deathMsg = TextFormatting.RED + victim.getName() + TextFormatting.GRAY + " was eliminated by " + TextFormatting.DARK_RED + "[" + ringKanji + "] " + TextFormatting.RED + killer.getName() + TextFormatting.DARK_RED + " of the Akatsuki";

                        for(EntityPlayerMP p : world.getMinecraftServer().getPlayerList().getPlayers()) {
                           p.sendMessage(new TextComponentString(deathMsg));
                        }
                     }

                     bountyClaimHandled = this.tryClaimBounty(data, world, killer, victim);

                     try {
                        ContractManager contractManager = ContractManager.getInstance();
                        if (contractManager != null) {
                           contractManager.onPlayerKill(victim.getName(), killer.getUniqueID());
                        }
                     } catch (Exception var19) {
                     }
                  }

                  if (!killerIsAkatsuki && victimIsAkatsuki) {
                     bountyClaimHandled = this.tryClaimBounty(data, world, killer, victim);
                     AkatsukiMember deadMember = data.getMember(victim.getUniqueID());
                     if (deadMember != null) {
                        deadMember.addReputation(-10);
                        data.markDirty();
                     }
                  }

                  if (!bountyClaimHandled) {
                     this.tryClaimBounty(data, world, killer, victim);
                  }

               }
            }
         }
      }
   }

   private boolean tryClaimBounty(AkatsukiSavedData data, World world, EntityPlayerMP killer, EntityPlayerMP victim) {
      BountyEntry bountyEntry = BountyManager.getBountyOn(data, victim.getUniqueID());
      if (bountyEntry == null) {
         System.out.println("[AkatsukiBounty] No bounty exists on " + victim.getName());
         return false;
      } else {
         int bountyAmount = bountyEntry.getBountyAmount();
         System.out.println("[AkatsukiBounty] Found bounty on " + victim.getName() + ": " + bountyAmount + " Ryo");
         if (bountyAmount < 100) {
            System.out.println("[AkatsukiBounty] Bounty too small to claim: " + bountyAmount + " < 100");
            return true;
         } else {
            Long lastClaim = (Long)recentBountyClaims.get(victim.getUniqueID());
            boolean onCooldown = lastClaim != null && System.currentTimeMillis() - lastClaim < 1800000L;
            if (onCooldown) {
               long remaining = 1800000L - (System.currentTimeMillis() - lastClaim);
               int minsLeft = (int)(remaining / 60000L) + 1;
               killer.sendMessage(new TextComponentString(TextFormatting.RED + "This target's bounty was recently claimed. " + TextFormatting.GRAY + "(" + minsLeft + " min cooldown)"));
               System.out.println("[AkatsukiBounty] Bounty claim blocked: cooldown (" + minsLeft + " min left)");
               return true;
            } else {
               boolean victimWasFighting = wasInCombatRecently(victim.getUniqueID());
               if (!victimWasFighting) {
                  System.out.println("[AkatsukiBounty] WARNING: victimWasFighting=false for " + victim.getName() + " — this shouldn't happen in a real PvP kill. Allowing claim anyway.");
               }

               int totalBounty = bountyEntry.getBountyAmount();
               int reward = (int)((double)totalBounty * 0.6);
               int remaining = totalBounty - reward;
               if (remaining >= 50) {
                  bountyEntry.addAmount(-reward);
               } else {
                  data.removeBounty(victim.getUniqueID());
               }

               data.markDirty();
               recentBountyClaims.put(victim.getUniqueID(), System.currentTimeMillis());
               ShopSavedData shopData = ShopSavedData.get(world);
               shopData.addBalance(killer.getUniqueID(), (long)reward);
               System.out.println("[AkatsukiBounty] Bounty CLAIMED: " + killer.getName() + " earned " + reward + " Ryo for killing " + victim.getName() + " (remaining bounty: " + (remaining >= 50 ? remaining : 0) + ")");
               String claimMsg = TextFormatting.GOLD + "★ Bounty Claimed! " + TextFormatting.WHITE + "You earned " + TextFormatting.GOLD + reward + " Ryo" + TextFormatting.WHITE + " for eliminating " + TextFormatting.RED + victim.getName() + (remaining >= 50 ? TextFormatting.GRAY + " (" + remaining + " Ryo bounty remains)" : "");
               killer.sendMessage(new TextComponentString(claimMsg));
               String broadcastMsg = TextFormatting.GOLD + "★ " + killer.getName() + TextFormatting.GRAY + " has claimed " + TextFormatting.GOLD + reward + " Ryo" + TextFormatting.GRAY + " bounty on " + TextFormatting.RED + victim.getName();

               for(EntityPlayerMP p : world.getMinecraftServer().getPlayerList().getPlayers()) {
                  p.sendMessage(new TextComponentString(broadcastMsg));
               }

               return true;
            }
         }
      }
   }

   @SubscribeEvent
   public void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         ++this.tickCounter;
         if (this.tickCounter % 200 == 0) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
               World world = server.getWorld(0);
               if (world != null) {
                  AkatsukiSavedData data = AkatsukiSavedData.get(world);
                  if (data != null) {
                     boolean anyDecayed = false;

                     for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                        if (isAkatsuki(player)) {
                           AkatsukiMember member = data.getMember(player.getUniqueID());
                           if (member != null && !member.getHeatMap().isEmpty()) {
                              HeatTracker.decayAllHeat(member, 1);
                              anyDecayed = true;
                           }
                        }
                     }

                     if (anyDecayed) {
                        data.markDirty();
                     }
                  }
               }
            }
         }

         if (this.tickCounter % 12000 == 0) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server == null) {
               return;
            }

            World world = server.getWorld(0);
            if (world != null) {
               AkatsukiSavedData data = AkatsukiSavedData.get(world);
               if (data != null) {
                  BountyManager.cleanExpired(data);
               }
            }
         }

         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            World world = server.getWorld(0);
            if (world != null) {
               BlackMarketManager.getInstance().tick(world);
            }
         }

         if (this.tickCounter >= 24000) {
            this.tickCounter = 0;
         }

      }
   }
}
