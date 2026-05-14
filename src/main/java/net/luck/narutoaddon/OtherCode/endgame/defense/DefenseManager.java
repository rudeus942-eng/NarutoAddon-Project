
package net.luck.narutoaddon.OtherCode.endgame.defense;

import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameNetworkHelper;
import net.luck.narutoaddon.OtherCode.quest.core.VillageHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefenseManager {
   private static DefenseManager INSTANCE = new DefenseManager();
   private final Map<String, DefenseInstance> activeDefenses = new ConcurrentHashMap();
   private int tickCounter;
   private final Random random = new Random();
   private long lastPeakUpdate;

   private DefenseManager() {
   }

   public static DefenseManager getInstance() {
      return INSTANCE;
   }

   public static void reset() {
      INSTANCE.activeDefenses.values().forEach(DefenseInstance::cleanup);
      INSTANCE = new DefenseManager();
   }

   public void onServerTick(World world) {
      ++this.tickCounter;
      if (this.tickCounter % 6000 == 0) {
         this.updatePeakHours(world);
      }

      if (this.tickCounter % 72000 == 0) {
         for(VillageHelper.Village village : VillageHelper.Village.values()) {
            if (village != VillageHelper.Village.UNKNOWN) {
               String name = village.name();
               if (this.shouldStartDefense(world, name)) {
                  DefenseDifficulty difficulty = this.determineAutoDifficulty(world, name);
                  this.startDefense(world, name, difficulty);
               }
            }
         }
      }

      boolean hadActive = !this.activeDefenses.isEmpty();
      Iterator<Map.Entry<String, DefenseInstance>> it = this.activeDefenses.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<String, DefenseInstance> entry = (Map.Entry)it.next();
         DefenseInstance instance = (DefenseInstance)entry.getValue();
         instance.tick(world);
         if (instance.getState() == DefenseInstance.DefenseState.VICTORY || instance.getState() == DefenseInstance.DefenseState.FAILED) {
            instance.cleanup();
            it.remove();
         }
      }

      if ((hadActive || !this.activeDefenses.isEmpty()) && this.tickCounter % 40 == 0) {
         for(EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            EndgameNetworkHelper.sendDefenseSync(player);
         }
      }

   }

   public void updatePeakHours(World world) {
      EndgameSavedData data = EndgameSavedData.get(world);
      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      int currentHour = cal.get(11);
      Map<String, Integer> villageCounts = new HashMap();

      for(EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
         VillageHelper.Village village = VillageHelper.getVillage(player);
         if (village != VillageHelper.Village.UNKNOWN) {
            villageCounts.merge(village.name(), 1, Integer::sum);
         }
      }

      for(Map.Entry<String, Integer> entry : villageCounts.entrySet()) {
         data.updatePeakHours((String)entry.getKey(), currentHour, (Integer)entry.getValue());
      }

   }

   public boolean shouldStartDefense(World world, String village) {
      if (this.activeDefenses.containsKey(village)) {
         return false;
      } else {
         DefenseDefinition def = DefenseDefinition.get(village);
         if (def == null) {
            return false;
         } else {
            EndgameSavedData data = EndgameSavedData.get(world);
            Integer override = data.getDefenseScheduleOverride(village);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            int currentHour = cal.get(11);
            int currentMinute = cal.get(12);
            int targetHour;
            if (override != null) {
               targetHour = override;
            } else {
               targetHour = data.getPeakHour(village);
            }

            int currentTotalMinutes = currentHour * 60 + currentMinute;
            int targetTotalMinutes = targetHour * 60;
            int diff = Math.abs(currentTotalMinutes - targetTotalMinutes);
            if (diff > 720) {
               diff = 1440 - diff;
            }

            if (diff > 30) {
               return false;
            } else {
               boolean hasPlayer = false;

               for(EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
                  VillageHelper.Village v = VillageHelper.getVillage(player);
                  if (v.name().equals(village)) {
                     hasPlayer = true;
                     break;
                  }
               }

               return hasPlayer;
            }
         }
      }
   }

   public void startDefense(World world, String village, DefenseDifficulty difficulty) {
      DefenseDefinition def = DefenseDefinition.get(village);
      if (def != null) {
         if (!this.activeDefenses.containsKey(village)) {
            DefenseInstance instance = new DefenseInstance(def, difficulty, world);
            this.activeDefenses.put(village, instance);
            String diffName = difficulty.name();

            for(EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
               VillageHelper.Village v = VillageHelper.getVillage(player);
               if (v.name().equals(village)) {
                  player.sendMessage(new TextComponentString("§6§l[Village Defense] §eYour village is under attack! §7Difficulty: §f" + diffName + " §7| Use §b/endgame defense join §7to participate!"));
                  instance.addParticipant(player.getUniqueID());
               }
            }

         }
      }
   }

   public void tryJoin(EntityPlayerMP player) {
      if (player.getServer() != null) {
         Advancement adv = player.getServer().getAdvancementManager().getAdvancement(new ResourceLocation("inftsukaddon", "arc3_complete"));
         if (adv == null || !player.getAdvancements().getProgress(adv).isDone()) {
            player.sendMessage(new TextComponentString("§cYou must be Jonin rank or higher to participate."));
            return;
         }
      }

      VillageHelper.Village village = VillageHelper.getVillage(player);
      if (village == VillageHelper.Village.UNKNOWN) {
         player.sendMessage(new TextComponentString("§cYou must belong to a village to join its defense."));
      } else {
         DefenseInstance instance = (DefenseInstance)this.activeDefenses.get(village.name());
         if (instance == null) {
            player.sendMessage(new TextComponentString("§cNo active defense event for your village."));
         } else if (instance.getParticipants().contains(player.getUniqueID())) {
            player.sendMessage(new TextComponentString("§eYou are already participating in this defense."));
         } else {
            instance.addParticipant(player.getUniqueID());
            player.sendMessage(new TextComponentString("§aYou have joined the village defense! Head to the battle zones."));
         }
      }
   }

   public void onPlayerDisconnect(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();

      for(DefenseInstance instance : this.activeDefenses.values()) {
         instance.removeParticipant(uuid);
      }

   }

   public DefenseInstance getActiveDefense(String village) {
      return (DefenseInstance)this.activeDefenses.get(village);
   }

   public void removeDefense(String village) {
      this.activeDefenses.remove(village);
   }

   public void stopAll() {
      for(DefenseInstance inst : this.activeDefenses.values()) {
         inst.cleanup();
      }

      this.activeDefenses.clear();
   }

   public Map<String, DefenseInstance> getActiveDefenses() {
      return Collections.unmodifiableMap(this.activeDefenses);
   }

   public void scheduleDefense(World world, String village, int hour) {
      EndgameSavedData data = EndgameSavedData.get(world);
      data.setDefenseScheduleOverride(village, hour);
   }

   private DefenseDifficulty determineAutoDifficulty(World world, String village) {
      List<EntityPlayerMP> villagePlayers = new ArrayList();

      for(EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
         VillageHelper.Village v = VillageHelper.getVillage(player);
         if (v.name().equals(village)) {
            villagePlayers.add(player);
         }
      }

      if (villagePlayers.isEmpty()) {
         return DefenseDifficulty.NORMAL;
      } else {
         int joninCount = 0;
         int highSpCount = 0;

         for(EntityPlayerMP player : villagePlayers) {
            if (player.getServer() != null) {
               Advancement adv = player.getServer().getAdvancementManager().getAdvancement(new ResourceLocation("inftsukaddon", "arc3_complete"));
               if (adv != null && player.getAdvancements().getProgress(adv).isDone()) {
                  ++joninCount;
               }
            }
         }

         if (joninCount == 0) {
            return DefenseDifficulty.NORMAL;
         } else {
            double joninRatio = (double)joninCount / (double)villagePlayers.size();
            if (joninRatio >= (double)0.5F) {
               return DefenseDifficulty.HARD;
            } else {
               return DefenseDifficulty.NORMAL;
            }
         }
      }
   }

   public static enum DefenseDifficulty {
      NORMAL((double)1.0F, (double)1.0F, 1500, 0),
      HARD((double)1.5F, 1.3, 3000, 1),
      NIGHTMARE((double)2.5F, (double)2.0F, 5000, 2);

      public final double hpMult;
      public final double dmgMult;
      public final int ryoReward;
      public final int spReward;

      private DefenseDifficulty(double hpMult, double dmgMult, int ryoReward, int spReward) {
         this.hpMult = hpMult;
         this.dmgMult = dmgMult;
         this.ryoReward = ryoReward;
         this.spReward = spReward;
      }
   }
}
