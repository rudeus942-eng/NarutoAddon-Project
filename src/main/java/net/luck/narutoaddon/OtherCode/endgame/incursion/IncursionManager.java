
package net.luck.narutoaddon.OtherCode.endgame.incursion;

import net.luck.narutoaddon.OtherCode.endgame.network.EndgameNetworkHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class IncursionManager {
   private static IncursionManager INSTANCE = new IncursionManager();
   private IncursionInstance activeIncursion = null;
   private long nextIncursionTime = System.currentTimeMillis();
   private int tickCounter = 0;
   private final List<HotspotRegion> hotspots = new ArrayList();
   private final Random random = null;
   private static final double AUTO_JOIN_RADIUS = (double)64.0F;

   private IncursionManager() {
      this.initHotspots();
   }

   public static IncursionManager getInstance() {
      return INSTANCE;
   }

   public static void reset() {
      INSTANCE = new IncursionManager();
   }

   private void initHotspots() {
      this.hotspots.clear();
      this.hotspots.add(new HotspotRegion("Northern Forest", 200, 600, -2000, -1600));
      this.hotspots.add(new HotspotRegion("Eastern Cliffs", 2000, 2400, -1000, -600));
      this.hotspots.add(new HotspotRegion("Southern Desert Edge", -2000, -1600, 600, 1000));
      this.hotspots.add(new HotspotRegion("Western Marshland", -3000, -2600, -400, 0));
      this.hotspots.add(new HotspotRegion("River Crossing", -800, -400, 200, 600));
      this.hotspots.add(new HotspotRegion("Mountain Pass", -2200, -1800, -2000, -1600));
      this.hotspots.add(new HotspotRegion("Coastal Road", 3000, 3400, -1400, -1000));
      this.hotspots.add(new HotspotRegion("Forest of Death Outskirts", -400, 0, -1800, -1400));
      this.hotspots.add(new HotspotRegion("Wind Country Border", -1800, -1400, 0, 400));
      this.hotspots.add(new HotspotRegion("Lightning Plateau", 1600, 2000, -2800, -2400));
      this.hotspots.add(new HotspotRegion("Sound Country Ruins", -1000, -500, -2100, -1700));
      this.hotspots.add(new HotspotRegion("Rain Country Border", -1600, -1200, -800, -400));
      this.hotspots.add(new HotspotRegion("Earth Country Foothills", -2600, -2200, -2600, -2200));
      this.hotspots.add(new HotspotRegion("Fire Country Crossroads", -600, -200, -600, -200));
      this.hotspots.add(new HotspotRegion("Mist Country Shore", 3200, 3600, -2200, -1800));
   }

   public void onServerTick(World world) {
      if (!world.isRemote) {
         ++this.tickCounter;
         if (this.tickCounter % 20 == 0) {
            if (this.activeIncursion != null) {
               this.autoJoinNearbyPlayers(world);
               this.activeIncursion.tick(world);
               if (this.activeIncursion.getState() != IncursionInstance.IncursionState.COMPLETE && this.activeIncursion.getState() != IncursionInstance.IncursionState.FAILED) {
                  this.sendIncursionSyncToAll(world);
               } else {
                  this.sendIncursionSyncToAll(world);
                  this.activeIncursion.cleanup();
                  this.activeIncursion = null;
                  this.scheduleNext();
               }
            } else if (System.currentTimeMillis() >= this.nextIncursionTime) {
               this.startIncursion(world);
               this.sendIncursionSyncToAll(world);
            }

         }
      }
   }

   private void autoJoinNearbyPlayers(World world) {
      if (this.activeIncursion != null) {
         IncursionInstance.IncursionState state = this.activeIncursion.getState();
         if (state != IncursionInstance.IncursionState.COMPLETE && state != IncursionInstance.IncursionState.FAILED) {
            MinecraftServer server = world.getMinecraftServer();
            if (server != null) {
               BlockPos loc = this.activeIncursion.getSpawnLocation();
               double radiusSq = (double)4096.0F;

               for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                  if (!this.activeIncursion.getParticipants().contains(player.getUniqueID())) {
                     double dx = player.posX - (double)loc.getX();
                     double dz = player.posZ - (double)loc.getZ();
                     if (dx * dx + dz * dz <= radiusSq) {
                        Advancement adv = server.getAdvancementManager().getAdvancement(new ResourceLocation("inftsukaddon", "arc3_complete"));
                        if (adv != null && player.getAdvancements().getProgress(adv).isDone() && this.activeIncursion.addParticipant(player.getUniqueID())) {
                           player.sendMessage(new TextComponentString("§a[INCURSION] §eYou have entered the incursion zone: §f" + this.activeIncursion.getDefinition().getDisplayName()));
                        }
                     }
                  }
               }

            }
         }
      }
   }

   private void sendIncursionSyncToAll(World world) {
      MinecraftServer server = world.getMinecraftServer();
      if (server != null) {
         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            EndgameNetworkHelper.sendIncursionSync(player);
         }

      }
   }

   public void scheduleNext() {
      long delayMs = 1800000L + (long)(ThreadLocalRandom.current().nextDouble() * (double)30.0F * (double)60.0F * (double)1000.0F);
      this.nextIncursionTime = System.currentTimeMillis() + delayMs;
   }

   public void startIncursion(World world) {
      if (this.activeIncursion == null) {
         HotspotRegion hotspot = (HotspotRegion)this.hotspots.get(world.rand.nextInt(this.hotspots.size()));
         List<IncursionDefinition> allTemplates = new ArrayList(IncursionDefinition.getAll());
         if (!allTemplates.isEmpty()) {
            IncursionDefinition template = (IncursionDefinition)allTemplates.get(world.rand.nextInt(allTemplates.size()));
            this.activeIncursion = new IncursionInstance(template, hotspot, world);
            this.announceToServer(world, template.getAnnouncement());
            this.announceToServer(world, "§e  Location: §f" + hotspot.getName() + " §7(" + this.activeIncursion.getSpawnLocation().getX() + ", " + this.activeIncursion.getSpawnLocation().getZ() + ")");
         }
      }
   }

   public boolean tryJoin(EntityPlayerMP player) {
      if (player.getServer() != null) {
         Advancement adv = player.getServer().getAdvancementManager().getAdvancement(new ResourceLocation("inftsukaddon", "arc3_complete"));
         if (adv == null || !player.getAdvancements().getProgress(adv).isDone()) {
            player.sendMessage(new TextComponentString("§cYou must be Jonin rank or higher to participate."));
            return false;
         }
      }

      if (this.activeIncursion == null) {
         player.sendMessage(new TextComponentString("§cNo incursion is currently active."));
         return false;
      } else if (this.activeIncursion.getState() != IncursionInstance.IncursionState.COMPLETE && this.activeIncursion.getState() != IncursionInstance.IncursionState.FAILED) {
         if (!this.activeIncursion.addParticipant(player.getUniqueID())) {
            player.sendMessage(new TextComponentString("§cThe incursion is full!"));
            return false;
         } else {
            player.sendMessage(new TextComponentString("§aYou have joined the incursion: §e" + this.activeIncursion.getDefinition().getDisplayName()));
            return true;
         }
      } else {
         player.sendMessage(new TextComponentString("§cThe incursion has already ended."));
         return false;
      }
   }

   public boolean tryLeave(EntityPlayerMP player) {
      if (this.activeIncursion == null) {
         player.sendMessage(new TextComponentString("§cNo incursion is currently active."));
         return false;
      } else if (!this.activeIncursion.isParticipant(player.getUniqueID())) {
         player.sendMessage(new TextComponentString("§cYou are not in this incursion."));
         return false;
      } else {
         this.activeIncursion.removeParticipant(player.getUniqueID());
         player.sendMessage(new TextComponentString("§eYou have left the incursion."));
         return true;
      }
   }

   public boolean isPlayerInIncursion(EntityPlayerMP player) {
      return this.activeIncursion != null && this.activeIncursion.isParticipant(player.getUniqueID());
   }

   public void onPlayerDisconnect(EntityPlayerMP player) {
      if (this.activeIncursion != null) {
         this.activeIncursion.removeParticipant(player.getUniqueID());
      }

   }

   public IncursionInstance getActiveIncursion() {
      return this.activeIncursion;
   }

   public long getNextIncursionTime() {
      return this.nextIncursionTime;
   }

   public List<HotspotRegion> getHotspots() {
      return this.hotspots;
   }

   public void announceToServer(World world, String msg) {
      MinecraftServer server = world.getMinecraftServer();
      if (server != null) {
         TextComponentString message = new TextComponentString(msg);

         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            player.sendMessage(message);
         }

      }
   }

   public static class HotspotRegion {
      private final String name;
      private final int minX;
      private final int maxX;
      private final int minZ;
      private final int maxZ;

      public HotspotRegion(String name, int minX, int maxX, int minZ, int maxZ) {
         this.name = name;
         this.minX = minX;
         this.maxX = maxX;
         this.minZ = minZ;
         this.maxZ = maxZ;
      }

      public String getName() {
         return this.name;
      }

      public int getMinX() {
         return this.minX;
      }

      public int getMaxX() {
         return this.maxX;
      }

      public int getMinZ() {
         return this.minZ;
      }

      public int getMaxZ() {
         return this.maxZ;
      }

      public int getRandomX(Random rand) {
         return this.minX + rand.nextInt(this.maxX - this.minX + 1);
      }

      public int getRandomZ(Random rand) {
         return this.minZ + rand.nextInt(this.maxZ - this.minZ + 1);
      }
   }
}
