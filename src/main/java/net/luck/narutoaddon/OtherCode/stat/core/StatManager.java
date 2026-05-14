
package net.luck.narutoaddon.OtherCode.stat.core;

import net.luck.narutoaddon.OtherCode.raid.boss.IRaidBoss;
import net.luck.narutoaddon.OtherCode.stat.network.StatSyncMessage;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatManager {
   private static StatManager instance;
   private static final ResourceLocation JONIN_ADVANCEMENT = new ResourceLocation("inftsukaddon", "arc3_complete");
   private final Map<UUID, StatElement> lastJutsuElement = new ConcurrentHashMap();

   private StatManager() {
   }

   public static StatManager getInstance() {
      if (instance == null) {
         instance = new StatManager();
      }

      return instance;
   }

   public boolean isJonin(EntityPlayerMP player) {
      MinecraftServer server = player.getServer();
      if (server == null) {
         return false;
      } else {
         Advancement adv = server.getAdvancementManager().getAdvancement(JONIN_ADVANCEMENT);
         return adv == null ? false : player.getAdvancements().getProgress(adv).isDone();
      }
   }

   public PlayerStatData getPlayerData(World world, UUID playerId) {
      StatSavedData saved = StatSavedData.get(world);
      return saved.getOrCreate(playerId);
   }

   public boolean grantSP(EntityPlayerMP player, int amount) {
      if (amount <= 0) {
         return false;
      } else if (!this.isJonin(player)) {
         return false;
      } else {
         StatSavedData saved = StatSavedData.get((World)player.getServerWorld());
         PlayerStatData data = saved.getOrCreate(player.getUniqueID());
         data.grantSP(amount);
         saved.markDirty();
         this.syncToClient(player);
         player.sendMessage(new TextComponentString(TextFormatting.GREEN + "+" + amount + " Stat Points earned! " + TextFormatting.GRAY + "(" + data.getAvailableSP() + " available)"));
         return true;
      }
   }

   public void adminGrantSP(World world, UUID playerId, int amount) {
      StatSavedData saved = StatSavedData.get(world);
      PlayerStatData data = saved.getOrCreate(playerId);
      data.grantSP(amount);
      saved.markDirty();
   }

   public void adminSetSP(World world, UUID playerId, int amount) {
      StatSavedData saved = StatSavedData.get(world);
      PlayerStatData data = saved.getOrCreate(playerId);
      data.setSPEarned(amount);
      saved.markDirty();
   }

   public boolean allocate(EntityPlayerMP player, StatCategory category, StatElement element) {
      if (!this.isJonin(player)) {
         return false;
      } else {
         StatSavedData saved = StatSavedData.get((World)player.getServerWorld());
         PlayerStatData data = saved.getOrCreate(player.getUniqueID());
         if (data.allocate(category, element)) {
            saved.markDirty();
            this.syncToClient(player);
            return true;
         } else {
            return false;
         }
      }
   }

   public String respec(EntityPlayerMP player) {
      if (!this.isJonin(player)) {
         return "You must be Jonin rank to use stats.";
      } else {
         StatSavedData saved = StatSavedData.get((World)player.getServerWorld());
         PlayerStatData data = saved.getOrCreate(player.getUniqueID());
         long now = System.currentTimeMillis();
         long elapsed = now - data.getLastRespecTime();
         if (elapsed < 604800000L) {
            long remaining = 604800000L - elapsed;
            long days = remaining / 86400000L;
            long hours = remaining % 86400000L / 3600000L;
            return "Respec on cooldown: " + days + "d " + hours + "h remaining.";
         } else if (data.getRespecTokens() <= 0) {
            return "You need a Respec Token.";
         } else {
            int ryo = this.getRyo(player);
            if (ryo < 5000) {
               return "You need 5000 Ryo (you have " + ryo + ").";
            } else {
               this.deductRyo(player, 5000);
               data.consumeRespecToken();
               data.respecAll();
               saved.markDirty();
               this.syncToClient(player);
               player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Stats reset! " + data.getAvailableSP() + " SP available."));
               return null;
            }
         }
      }
   }

   public void adminRespec(World world, UUID playerId) {
      StatSavedData saved = StatSavedData.get(world);
      PlayerStatData data = saved.getOrCreate(playerId);
      data.respecAll();
      saved.markDirty();
   }

   private int getRyo(EntityPlayerMP player) {
      try {
         Scoreboard sb = player.getWorldScoreboard();
         ScoreObjective obj = sb.getObjective("Ryo");
         return obj == null ? 0 : sb.getOrCreateScore(player.getName(), obj).getScorePoints();
      } catch (Exception var4) {
         return 0;
      }
   }

   private void deductRyo(EntityPlayerMP player, int amount) {
      try {
         Scoreboard sb = player.getWorldScoreboard();
         ScoreObjective obj = sb.getObjective("Ryo");
         if (obj == null) {
            return;
         }

         Score score = sb.getOrCreateScore(player.getName(), obj);
         score.setScorePoints(Math.max(0, score.getScorePoints() - amount));
      } catch (Exception var6) {
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public void onLivingAttackCacheElement(LivingAttackEvent event) {
      if (!event.isCanceled()) {
         DamageSource source = event.getSource();
         if (source != null) {
            String damageType = source.getDamageType();
            if ("ninjutsu".equals(damageType)) {
               Entity immediate = source.getImmediateSource();
               Entity trueSource = source.getTrueSource();
               if (immediate != null && trueSource != null) {
                  if (trueSource instanceof EntityPlayerMP) {
                     if (immediate != trueSource) {
                        StatElement element = JutsuElementMapper.resolveFromEntity(immediate);
                        if (element != null && element != StatElement.GENERIC_NINJUTSU) {
                           this.lastJutsuElement.put(trueSource.getUniqueID(), element);
                        }

                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.NORMAL
   )
   public void onLivingHurt(LivingHurtEvent event) {
      if (!event.isCanceled()) {
         World world = event.getEntityLiving().world;
         if (!world.isRemote) {
            DamageSource source = event.getSource();
            if (JutsuElementMapper.isJutsuDamage(source)) {
               StatElement element = JutsuElementMapper.resolve(source);
               Entity trueSourceForCache = source.getTrueSource();
               String damageType = source.getDamageType();
               if ("senjutsu".equals(damageType) && (element == null || element == StatElement.GENERIC_NINJUTSU) && trueSourceForCache instanceof EntityPlayerMP) {
                  StatElement cached = (StatElement)this.lastJutsuElement.get(trueSourceForCache.getUniqueID());
                  if (cached != null && cached != StatElement.GENERIC_NINJUTSU) {
                     element = cached;
                     System.out.println("[StatManager] Senjutsu: using cached element " + cached + " for " + ((EntityPlayerMP)trueSourceForCache).getName());
                  }
               }

               if (element != null && element != StatElement.GENERIC_NINJUTSU) {
                  float originalDamage = event.getAmount();
                  double offenseBonus = (double)0.0F;
                  double elementalCycleBonus = (double)0.0F;
                  double defenseReduction = (double)0.0F;
                  Entity trueSource = source.getTrueSource();
                  PlayerStatData attackerStats = null;
                  if (trueSource instanceof EntityPlayerMP) {
                     EntityPlayerMP attacker = (EntityPlayerMP)trueSource;
                     attackerStats = this.getPlayerData(world, attacker.getUniqueID());
                     offenseBonus = attackerStats.getOffenseBonus(element);
                  }

                  EntityLivingBase defender = event.getEntityLiving();
                  if (defender instanceof EntityPlayerMP) {
                     EntityPlayerMP defPlayer = (EntityPlayerMP)defender;
                     PlayerStatData defenderStats = this.getPlayerData(world, defPlayer.getUniqueID());
                     defenseReduction = defenderStats.getDefenseReduction(element);
                     if (this.isRaidBoss(trueSource)) {
                        defenseReduction *= (double)0.5F;
                     }
                  }

                  if (element.isBaseNature() && defender instanceof EntityLivingBase && defender instanceof EntityPlayerMP) {
                     EntityPlayerMP defPlayer = (EntityPlayerMP)defender;
                     PlayerStatData defStats = this.getPlayerData(world, defPlayer.getUniqueID());
                     StatElement defHighest = this.getHighestDefenseNature(defStats);
                     if (defHighest != null && element.hasAdvantageOver(defHighest)) {
                        elementalCycleBonus = 0.08;
                     } else if (defHighest != null && element.hasDisadvantageAgainst(defHighest)) {
                        elementalCycleBonus = -0.05;
                     }
                  }

                  double modified = (double)originalDamage * ((double)1.0F + offenseBonus) * ((double)1.0F + elementalCycleBonus) * ((double)1.0F - defenseReduction);
                  if (attackerStats != null) {
                     double ceBonus = attackerStats.getChakraEnhancementBonus();
                     if (ceBonus > (double)0.0F) {
                        modified *= (double)1.0F + ceBonus;
                     }
                  }

                  float finalDamage = Math.max(1.0F, (float)modified);
                  event.setAmount(finalDamage);
               } else {
                  if ("senjutsu".equals(source.getDamageType())) {
                     EntityLivingBase defender = event.getEntityLiving();
                     if (defender instanceof EntityPlayerMP) {
                        EntityPlayerMP defPlayer = (EntityPlayerMP)defender;
                        PlayerStatData defStats = this.getPlayerData(world, defPlayer.getUniqueID());
                        double avgDefense = defStats.getAverageDefenseReduction();
                        if (avgDefense > (double)0.0F) {
                           float scaled = event.getAmount() * (float)((double)1.0F - avgDefense);
                           event.setAmount(Math.max(1.0F, scaled));
                           System.out.println("[StatManager] Senjutsu fallback defense applied: " + event.getAmount() + " (avgDef=" + avgDefense + ")");
                        }
                     }
                  }

               }
            }
         }
      }
   }

   private StatElement getHighestDefenseNature(PlayerStatData data) {
      StatElement highest = null;
      int highestLevel = 0;

      for(StatElement el : StatElement.getElementsForCategory(StatCategory.NATURE_DEFENSE)) {
         int level = data.getLevel(StatCategory.NATURE_DEFENSE, el);
         if (level > highestLevel) {
            highestLevel = level;
            highest = el;
         }
      }

      return highest;
   }

   private boolean isRaidBoss(Entity entity) {
      return entity == null ? false : entity instanceof IRaidBoss;
   }

   public void syncToClient(EntityPlayerMP player) {
      PlayerStatData data = this.getPlayerData(player.getServerWorld(), player.getUniqueID());
      boolean unlocked = this.isJonin(player);
      int ceCap = getChakraEnhancementCap(player);
      StatSyncMessage msg = StatSyncMessage.fromPlayerData(data, unlocked, ceCap);
      StatModInit.NETWORK.sendTo(msg, player);
   }

   public void onPlayerLogin(EntityPlayerMP player) {
      this.syncToClient(player);
   }

   public static int getChakraEnhancementCap(EntityPlayerMP player) {
      MinecraftServer server = player.getServer();
      if (server == null) {
         return 0;
      } else {
         int cap = 0;
         Advancement arc7 = server.getAdvancementManager().getAdvancement(new ResourceLocation("inftsukaddon", "arc7_complete"));
         if (arc7 != null && player.getAdvancements().getProgress(arc7).isDone()) {
            ++cap;
         }

         Advancement arc8 = server.getAdvancementManager().getAdvancement(new ResourceLocation("inftsukaddon", "arc8_complete"));
         if (arc8 != null && player.getAdvancements().getProgress(arc8).isDone()) {
            ++cap;
         }

         Advancement arc9 = server.getAdvancementManager().getAdvancement(new ResourceLocation("inftsukaddon", "arc9_complete"));
         if (arc9 != null && player.getAdvancements().getProgress(arc9).isDone()) {
            ++cap;
         }

         return cap;
      }
   }

   public int getSPForRank(int rankOrdinal) {
      switch (rankOrdinal) {
         case 2:
            return 2;
         case 3:
            return 4;
         case 4:
            return 7;
         case 5:
            return 10;
         default:
            return 0;
      }
   }
}
