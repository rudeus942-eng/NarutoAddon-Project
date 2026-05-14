
package net.luck.narutoaddon.OtherCode.endgame.incursion;

import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.endgame.PveRank;
import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointSpawnLogic;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class IncursionInstance {
   private static final int ANNOUNCE_WAIT_TICKS = 2400;
   private static final double CONTRIBUTION_THRESHOLD = 0.05;
   private static final int WAVE_REWARD_RYO = 230;
   private static final int COMPLETION_BONUS_RYO = 600;
   private static final int WAVE_STALE_TIMEOUT_TICKS = 6000;
   private final IncursionDefinition definition;
   private final IncursionManager.HotspotRegion hotspot;
   private final BlockPos spawnLocation;
   private IncursionState state;
   private int currentWaveIndex;
   private final Set<UUID> participants;
   private final Map<UUID, Double> waveDamageContribution;
   private final Set<UUID> spawnedEntities;
   private double totalWaveHpPool;
   private long stateStartTime;
   private int maxPlayers;
   private int stateTicks;

   public IncursionInstance(IncursionDefinition definition, IncursionManager.HotspotRegion hotspot, World world) {
      this.definition = definition;
      this.hotspot = hotspot;
      this.spawnLocation = this.findSpawnLocation(hotspot, world);
      this.state = IncursionState.ANNOUNCED;
      this.currentWaveIndex = 0;
      this.participants = new HashSet();
      this.waveDamageContribution = new HashMap();
      this.spawnedEntities = new HashSet();
      this.totalWaveHpPool = (double)0.0F;
      this.stateStartTime = System.currentTimeMillis();
      this.maxPlayers = 10;
      this.stateTicks = 0;
   }

   private BlockPos findSpawnLocation(IncursionManager.HotspotRegion region, World world) {
      for(int attempt = 0; attempt < 20; ++attempt) {
         int x = region.getRandomX(world.rand);
         int z = region.getRandomZ(world.rand);
         BlockPos candidate = WaypointSpawnLogic.findLandPosition(world, new BlockPos(x, 64, z));
         int y = WaypointSpawnLogic.findGroundY(world, candidate.getX(), candidate.getZ());
         BlockPos groundPos = new BlockPos(candidate.getX(), y - 1, candidate.getZ());
         IBlockState groundState = world.getBlockState(groundPos);
         Material mat = groundState.getMaterial();
         if (mat != Material.WATER && mat != Material.ICE && mat != Material.PACKED_ICE) {
            return new BlockPos(candidate.getX(), y, candidate.getZ());
         }
      }

      int cx = (region.getMinX() + region.getMaxX()) / 2;
      int cz = (region.getMinZ() + region.getMaxZ()) / 2;
      BlockPos fallback = WaypointSpawnLogic.findLandPosition(world, new BlockPos(cx, 64, cz));
      int cy = WaypointSpawnLogic.findGroundY(world, fallback.getX(), fallback.getZ());
      System.out.println("[Incursion] WARNING: Could not find dry land in 20 attempts for " + region.getName() + ", falling back to center (" + fallback.getX() + ", " + cy + ", " + fallback.getZ() + ")");
      return new BlockPos(fallback.getX(), cy, fallback.getZ());
   }

   public void tick(World world) {
      this.stateTicks += 20;
      switch (this.state) {
         case ANNOUNCED:
            if (this.stateTicks >= 2400) {
               if (this.participants.isEmpty()) {
                  this.state = IncursionState.FAILED;
                  return;
               }

               if (this.isAnyParticipantNearby(world, (double)100.0F)) {
                  this.transitionTo(IncursionState.WAVE_ACTIVE);
                  this.spawnWave(0, world);
               } else {
                  this.transitionTo(IncursionState.WAITING);
                  this.announceToNearby(world, "§e[INCURSION] §7Waiting for participants to arrive at the location...");
               }
            }
            break;
         case WAITING:
            if (this.isAnyParticipantNearby(world, (double)100.0F)) {
               this.transitionTo(IncursionState.WAVE_ACTIVE);
               this.spawnWave(this.currentWaveIndex, world);
               this.announceToNearby(world, "§c[INCURSION] §eThe battle begins!");
            } else if (this.stateTicks >= 6000) {
               this.announceToNearby(world, "§c[INCURSION] §7No one arrived in time. The incursion fades.");
               this.transitionTo(IncursionState.FAILED);
            }
            break;
         case WAVE_ACTIVE:
            if (this.stateTicks >= 6000) {
               this.announceToNearby(world, "§c[INCURSION] §7The incursion has timed out.");
               this.transitionTo(IncursionState.FAILED);
               return;
            }

            if (this.areAllEnemiesDead(world)) {
               Set<UUID> qualified = this.getQualifiedPlayers();
               this.grantWaveReward(qualified, world, this.currentWaveIndex + 1);
               ++this.currentWaveIndex;
               if (this.currentWaveIndex >= this.definition.getWaves().size()) {
                  this.transitionTo(IncursionState.COMPLETE);
                  this.grantCompletionBonus(world);
                  this.announceToNearby(world, "§a§l[INCURSION] §eVictory! The " + this.definition.getDisplayName() + " has been repelled!");
               } else {
                  this.transitionTo(IncursionState.WAVE_BREAK);
                  IncursionWave nextWave = (IncursionWave)this.definition.getWaves().get(this.currentWaveIndex);
                  this.announceToNearby(world, "§e[INCURSION] §7Wave " + this.currentWaveIndex + " cleared! Next wave in " + nextWave.getDelayBeforeWaveTicks() / 20 + "s...");
               }

               this.waveDamageContribution.clear();
               this.totalWaveHpPool = (double)0.0F;
            }
            break;
         case WAVE_BREAK:
            IncursionWave nextWave = (IncursionWave)this.definition.getWaves().get(this.currentWaveIndex);
            if (this.stateTicks >= nextWave.getDelayBeforeWaveTicks()) {
               this.transitionTo(IncursionState.WAVE_ACTIVE);
               this.spawnWave(this.currentWaveIndex, world);
               this.announceToNearby(world, "§c[INCURSION] §eWave " + (this.currentWaveIndex + 1) + " incoming!");
            }
         case COMPLETE:
         case FAILED:
      }

   }

   private void transitionTo(IncursionState newState) {
      this.state = newState;
      this.stateTicks = 0;
      this.stateStartTime = System.currentTimeMillis();
   }

   private boolean isAnyParticipantNearby(World world, double radius) {
      MinecraftServer server = world.getMinecraftServer();
      if (server == null) {
         return false;
      } else {
         double radiusSq = radius * radius;

         for(UUID playerId : this.participants) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
            if (player != null) {
               double dx = player.posX - (double)this.spawnLocation.getX();
               double dz = player.posZ - (double)this.spawnLocation.getZ();
               if (dx * dx + dz * dz <= radiusSq) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public void spawnWave(int index, World world) {
      if (index >= 0 && index < this.definition.getWaves().size()) {
         WaypointSpawnLogic.cleanupQuestEntities(world, this.spawnedEntities);
         this.spawnedEntities.clear();
         this.totalWaveHpPool = (double)0.0F;
         IncursionWave wave = (IncursionWave)this.definition.getWaves().get(index);
         String[] configIds = wave.getEnemyConfigIds();
         int[] counts = wave.getEnemyCounts();

         for(int i = 0; i < configIds.length; ++i) {
            int count = i < counts.length ? counts[i] : 1;

            for(int j = 0; j < count; ++j) {
               int offsetX = world.rand.nextInt(21) - 10;
               int offsetZ = world.rand.nextInt(21) - 10;
               int x = this.spawnLocation.getX() + offsetX;
               int z = this.spawnLocation.getZ() + offsetZ;
               BlockPos raw = WaypointSpawnLogic.findLandPosition(world, new BlockPos(x, 64, z));
               int y = WaypointSpawnLogic.findGroundY(world, raw.getX(), raw.getZ());
               BlockPos pos = new BlockPos(raw.getX(), y, raw.getZ());
               Entity entity = this.spawnNpcFromConfig(world, configIds[i], pos);
               if (entity != null) {
                  this.spawnedEntities.add(entity.getUniqueID());
                  if (entity instanceof EntityLivingBase) {
                     this.totalWaveHpPool += (double)((EntityLivingBase)entity).getMaxHealth();
                  }
               }
            }
         }

         System.out.println("[Incursion] Wave " + (index + 1) + " of " + this.definition.getDisplayName() + " spawned " + this.spawnedEntities.size() + " entities at (" + this.spawnLocation.getX() + ", " + this.spawnLocation.getY() + ", " + this.spawnLocation.getZ() + ") | participants=" + this.participants.size());
         if (this.spawnedEntities.isEmpty()) {
            System.out.println("[Incursion] WARNING: Wave " + (index + 1) + " spawned 0 entities! Check config IDs.");
         }

      }
   }

   private Entity spawnNpcFromConfig(World world, String npcConfigId, BlockPos pos) {
      NpcConfig config = NpcConfigRegistry.get(npcConfigId);
      if (config == null) {
         System.out.println("[Incursion] Unknown NPC config: " + npcConfigId);
         return null;
      } else {
         Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(config.getEntityRegistryId()), world);
         if (entity == null) {
            System.out.println("[Incursion] Failed to create entity: " + config.getEntityRegistryId());
            return null;
         } else {
            double spawnX = (double)pos.getX() + (double)0.5F;
            double spawnZ = (double)pos.getZ() + (double)0.5F;
            double spawnY = (double)pos.getY();
            float yaw = world.rand.nextFloat() * 360.0F;
            entity.setLocationAndAngles(spawnX, spawnY, spawnZ, yaw, 0.0F);
            if (entity instanceof EntityLivingBase) {
               ((EntityLivingBase)entity).rotationYawHead = yaw;
               ((EntityLivingBase)entity).renderYawOffset = yaw;
            }

            NBTTagCompound entityData = entity.getEntityData();
            entityData.setBoolean("questEntity", true);
            entityData.setBoolean("incursionEntity", true);
            entityData.setString("npcConfigId", npcConfigId);
            if (entity instanceof INpcConfigurable) {
               ((INpcConfigurable)entity).applyNpcConfig(config);
            }

            if (entity instanceof EntityLiving) {
               ((EntityLiving)entity).enablePersistence();
            }

            entityData.setBoolean("PersistenceRequired", true);
            world.spawnEntity(entity);
            return entity;
         }
      }
   }

   private boolean areAllEnemiesDead(World world) {
      return this.spawnedEntities.isEmpty() ? true : WaypointSpawnLogic.areAllEnemiesDead(world, this.spawnedEntities);
   }

   public void onEnemyDamaged(UUID playerUUID, double amount) {
      this.waveDamageContribution.merge(playerUUID, amount, Double::sum);
   }

   public void onEnemyKilled(UUID entityUUID) {
      this.spawnedEntities.remove(entityUUID);
   }

   public boolean addParticipant(UUID player) {
      if (this.participants.size() >= this.maxPlayers) {
         return false;
      } else {
         this.participants.add(player);
         return true;
      }
   }

   public void removeParticipant(UUID player) {
      this.participants.remove(player);
   }

   public boolean isParticipant(UUID player) {
      return this.participants.contains(player);
   }

   public Set<UUID> getQualifiedPlayers() {
      Set<UUID> qualified = new HashSet();
      if (this.totalWaveHpPool <= (double)0.0F) {
         return new HashSet(this.participants);
      } else {
         for(Map.Entry<UUID, Double> entry : this.waveDamageContribution.entrySet()) {
            if ((Double)entry.getValue() / this.totalWaveHpPool >= 0.05) {
               qualified.add(entry.getKey());
            }
         }

         return qualified;
      }
   }

   public void cleanup() {
      FMLCommonHandler handler = FMLCommonHandler.instance();
      if (handler.getMinecraftServerInstance() != null) {
         World world = handler.getMinecraftServerInstance().getWorld(0);
         if (world != null) {
            WaypointSpawnLogic.cleanupQuestEntities(world, this.spawnedEntities);
         }
      }

      this.spawnedEntities.clear();
      this.waveDamageContribution.clear();
   }

   public void grantWaveReward(Set<UUID> qualified, World world, int waveNum) {
      MinecraftServer server = world.getMinecraftServer();
      if (server != null) {
         EndgameSavedData data = EndgameSavedData.get(world);

         for(UUID playerId : qualified) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
            if (player != null) {
               int actualRyo = RyoRewardHelper.grantRyoSilent(player, 230);
               PveRank rankBefore = data.getPveRank(playerId);
               data.addPveXp(playerId, 15);
               PveRank rankAfter = data.getPveRank(playerId);
               String ryoText = "§a+" + actualRyo + " Ryo";
               if (actualRyo > 230) {
                  ryoText = ryoText + " §6(+" + (actualRyo - 230) + " territory)";
               }

               player.sendMessage(new TextComponentString(ryoText + " §b+" + 15 + " PvE XP §7(Incursion Wave " + waveNum + ")"));
               if (rankAfter != rankBefore) {
                  player.sendMessage(new TextComponentString("§6§l★ PvE RANK UP! §r§e" + rankBefore.getDisplayName() + " → " + rankAfter.getDisplayName()));
               }

               data.incrementLeaderboard("incursionWaves", playerId);
            }
         }

      }
   }

   public void grantCompletionBonus(World world) {
      MinecraftServer server = world.getMinecraftServer();
      if (server != null) {
         EndgameSavedData data = EndgameSavedData.get(world);

         for(UUID playerId : this.participants) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
            if (player != null) {
               int actualRyo = RyoRewardHelper.grantRyoSilent(player, 600);
               PveRank rankBefore = data.getPveRank(playerId);
               data.addPveXp(playerId, 50);
               PveRank rankAfter = data.getPveRank(playerId);
               String ryoText = "§6§l+" + actualRyo + " Ryo";
               if (actualRyo > 600) {
                  ryoText = ryoText + " §6(+" + (actualRyo - 600) + " territory)";
               }

               player.sendMessage(new TextComponentString(ryoText + " §b+" + 50 + " PvE XP §e(Incursion Complete!)"));
               if (rankAfter != rankBefore) {
                  player.sendMessage(new TextComponentString("§6§l★ PvE RANK UP! §r§e" + rankBefore.getDisplayName() + " → " + rankAfter.getDisplayName()));
               }

               data.incrementLeaderboard("incursionWaves", playerId);
            }
         }

      }
   }

   private void announceToNearby(World world, String msg) {
      MinecraftServer server = world.getMinecraftServer();
      if (server != null) {
         TextComponentString message = new TextComponentString(msg);

         for(UUID playerId : this.participants) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
            if (player != null) {
               player.sendMessage(message);
            }
         }

      }
   }

   public IncursionDefinition getDefinition() {
      return this.definition;
   }

   public IncursionManager.HotspotRegion getHotspot() {
      return this.hotspot;
   }

   public BlockPos getSpawnLocation() {
      return this.spawnLocation;
   }

   public IncursionState getState() {
      return this.state;
   }

   public int getCountdownSeconds() {
      switch (this.state) {
         case ANNOUNCED:
            int remaining = 2400 - this.stateTicks;
            return Math.max(0, remaining / 20);
         case WAVE_BREAK:
            if (this.currentWaveIndex < this.definition.getWaves().size()) {
               int delay = ((IncursionWave)this.definition.getWaves().get(this.currentWaveIndex)).getDelayBeforeWaveTicks();
               int remaining = delay - this.stateTicks;
               return Math.max(0, remaining / 20);
            }

            return -1;
         default:
            return -1;
      }
   }

   public int getCurrentWaveIndex() {
      return this.currentWaveIndex;
   }

   public Set<UUID> getParticipants() {
      return this.participants;
   }

   public Set<UUID> getSpawnedEntities() {
      return this.spawnedEntities;
   }

   public int getMaxPlayers() {
      return this.maxPlayers;
   }

   public int getTotalWaves() {
      return this.definition.getWaves().size();
   }

   public static enum IncursionState {
      ANNOUNCED,
      WAITING,
      WAVE_ACTIVE,
      WAVE_BREAK,
      COMPLETE,
      FAILED;
   }
}
