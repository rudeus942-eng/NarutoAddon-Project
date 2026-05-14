
package net.luck.narutoaddon.OtherCode.raid.core;

import net.luck.narutoaddon.OtherCode.raid.arena.RaidArena;
import net.luck.narutoaddon.OtherCode.raid.boss.IRaidBoss;
import net.luck.narutoaddon.OtherCode.raid.boss.bosses.hashirama.HashiramaMechanics;
import net.luck.narutoaddon.OtherCode.raid.boss.bosses.itachi.ItachiMechanics;
import net.luck.narutoaddon.OtherCode.raid.boss.bosses.kimimaro.KimimaroMechanics;
import net.luck.narutoaddon.OtherCode.raid.network.RaidNetworkHelper;
import net.luck.narutoaddon.OtherCode.raid.party.RaidParty;
import net.luck.narutoaddon.OtherCode.raid.rewards.RaidRewardDistributor;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class RaidInstance {
   public static final int TICK_INTERVAL = 20;
   private final int raidId;
   private final String bossId;
   private final RaidDifficulty difficulty;
   private final RaidParty party;
   private final RaidArena arena;
   private final long startTime;
   private final World world;
   private EntityLivingBase bossEntity;
   private IRaidBoss boss;
   private int bossEntityId = -1;
   private Set<UUID> alivePlayers;
   private Set<UUID> deadPlayers;
   private Map<UUID, Integer> playerDeathCounts;
   private Set<UUID> activeParticipants;
   private Set<Integer> spawnedEntityIds;
   private Set<UUID> spawnedEntityUUIDs;
   private List<BlockPos> trackedBlocks;
   private RaidState state;
   private int enrageLevel = 0;
   private long lastTickTime;
   private int tickCounter = 0;
   private static final int TIMER_SYNC_INTERVAL = 20;
   private int currentPhase = 1;
   private String currentPhaseName = "";

   public RaidInstance(int raidId, String bossId, RaidDifficulty difficulty, RaidParty party, RaidArena arena, World world) {
      this.raidId = raidId;
      this.bossId = bossId;
      this.difficulty = difficulty;
      this.party = party;
      this.arena = arena;
      this.world = world;
      this.startTime = System.currentTimeMillis();
      this.lastTickTime = this.startTime;
      this.state = RaidState.STARTING;
      this.alivePlayers = new HashSet(party.getMemberUUIDs());
      this.deadPlayers = new HashSet();
      this.playerDeathCounts = new HashMap();

      for(UUID uuid : party.getMemberUUIDs()) {
         this.playerDeathCounts.put(uuid, 0);
      }

      this.activeParticipants = new HashSet(party.getMemberUUIDs());
      this.spawnedEntityIds = new HashSet();
      this.spawnedEntityUUIDs = new HashSet();
      this.trackedBlocks = new ArrayList();
   }

   public void tick() {
      if (this.state == RaidState.ACTIVE || this.state == RaidState.STARTING) {
         ++this.tickCounter;
         long now = System.currentTimeMillis();
         this.checkEnrage(now);
         if (this.boss != null && this.bossEntity != null && !this.bossEntity.isDead) {
            this.currentPhase = this.boss.getPhaseController().getCurrentPhaseNumber();
            this.currentPhaseName = this.boss.getPhaseController().getCurrentPhaseName();
         }

         if (this.state == RaidState.ACTIVE && this.tickCounter % 20 == 0) {
            this.syncToClients();
         }

         if (this.state == RaidState.ACTIVE && this.tickCounter % 10 == 0) {
            this.checkPlayersAboveFloor();
         }

         if (this.state == RaidState.ACTIVE && this.bossEntity != null && this.bossEntity.isDead) {
            this.onVictory();
         }

         if (this.state == RaidState.ACTIVE && this.alivePlayers.isEmpty()) {
            this.onWipe();
         }

         this.lastTickTime = now;
      }
   }

   private void syncToClients() {
      RaidNetworkHelper.sendEnrageUpdate(this, this.enrageLevel, this.getElapsedTime());
      if (this.bossEntity != null && !this.bossEntity.isDead) {
         RaidNetworkHelper.sendHealthUpdate(this, this.bossEntity.getHealth(), this.bossEntity.getMaxHealth());
      }

   }

   private void checkPlayersAboveFloor() {
      if (this.arena != null) {
         BlockPos bossSpawn = this.arena.getBossSpawn();
         int platformY = bossSpawn != null ? bossSpawn.getY() : this.arena.getCorner1().getY();

         for(UUID uuid : new HashSet(this.activeParticipants)) {
            EntityPlayerMP player = this.getPlayer(uuid);
            if (player != null && !player.isDead && player.posY < (double)(platformY - 3)) {
               BlockPos playerSpawn = this.arena.getPlayerSpawn(0);
               List<UUID> members = (List<UUID>)(this.party != null ? new ArrayList(this.party.getMemberUUIDs()) : Collections.emptyList());
               int idx = members.indexOf(uuid);
               if (idx >= 0) {
                  BlockPos specific = this.arena.getPlayerSpawn(idx);
                  if (specific != null) {
                     playerSpawn = specific;
                  }
               }

               double safeX = playerSpawn != null ? (double)playerSpawn.getX() + (double)0.5F : player.posX;
               double safeZ = playerSpawn != null ? (double)playerSpawn.getZ() + (double)0.5F : player.posZ;
               player.connection.setPlayerLocation(safeX, (double)platformY, safeZ, player.rotationYaw, player.rotationPitch);
               player.motionX = (double)0.0F;
               player.motionY = (double)0.0F;
               player.motionZ = (double)0.0F;
               player.velocityChanged = true;
            }
         }

      }
   }

   private void checkEnrage(long now) {
      long elapsed = now - this.startTime;
      if (this.difficulty == RaidDifficulty.ANBU) {
         if (elapsed >= 1200000L) {
            this.triggerHardWipe();
         }
      } else if (elapsed >= 1050000L) {
         this.triggerHardWipe();
      } else if (elapsed >= 900000L && this.enrageLevel < 2) {
         this.setEnrageLevel(2);
      } else if (elapsed >= 600000L && this.enrageLevel < 1) {
         this.setEnrageLevel(1);
      }

   }

   private void setEnrageLevel(int level) {
      if (level > this.enrageLevel) {
         this.enrageLevel = level;
         if (this.boss != null) {
            this.boss.setEnrageLevel(level);
         }

         String message;
         if (level == 1) {
            message = "§c§l[ENRAGE] " + this.boss.getBossDisplayName() + " is becoming more aggressive!";
         } else {
            message = "§4§l[ENRAGE] " + this.boss.getBossDisplayName() + " is FURIOUS! Reduced mechanic warnings!";
         }

         this.broadcastMessage(message);
      }

   }

   private void triggerHardWipe() {
      for(UUID uuid : new HashSet(this.activeParticipants)) {
         EntityPlayerMP player = this.getPlayer(uuid);
         if (player != null) {
            sendResultTitle(player, "§4§lRAID FAILED", "§cTime limit exceeded!", 10, 80, 20);
         }
      }

      this.broadcastMessage("§4§l[RAID FAILED] Time limit exceeded! " + this.boss.getBossDisplayName() + " unleashes a devastating attack!");

      for(UUID uuid : new HashSet(this.alivePlayers)) {
         EntityPlayerMP player = this.getPlayer(uuid);
         if (player != null) {
            player.attackEntityFrom(DamageSource.MAGIC, 10000.0F);
         }
      }

      this.state = RaidState.TIMEOUT;
      this.cleanup();
   }

   public boolean onPlayerDeath(UUID playerUUID) {
      if (this.alivePlayers.remove(playerUUID)) {
         this.deadPlayers.add(playerUUID);
         this.playerDeathCounts.merge(playerUUID, 1, Integer::sum);
         this.activeParticipants.remove(playerUUID);
         EntityPlayerMP player = this.getPlayer(playerUUID);
         String playerName = player != null ? player.getName() : "Unknown";
         int remaining = this.alivePlayers.size();
         this.broadcastMessage("§c" + playerName + " has fallen! §7(" + remaining + " players remaining)");
         if (player != null) {
            sendResultTitle(player, "§c§lRAID FAILED", "§7You may queue again", 10, 60, 20);
            player.sendMessage(new TextComponentString(""));
            player.sendMessage(new TextComponentString("§c§lYou have failed the raid."));
            player.sendMessage(new TextComponentString("§7You can now queue for another raid."));
            player.sendMessage(new TextComponentString(""));
         }

         if (this.alivePlayers.isEmpty()) {
            this.onWipe();
            return true;
         }
      }

      return false;
   }

   public void removePlayerFromRaid(UUID playerUUID) {
      this.alivePlayers.remove(playerUUID);
      this.deadPlayers.remove(playerUUID);
      this.activeParticipants.remove(playerUUID);
      if (this.party != null) {
         this.party.removeMemberSilently(playerUUID);
      }

      System.out.println("[RaidInstance] Completely removed player " + playerUUID + " from raid #" + this.raidId);
   }

   public void onPlayerRespawn(UUID playerUUID) {
   }

   public void onPlayerDisconnect(UUID playerUUID) {
      this.onPlayerDeath(playerUUID);
   }

   private void onVictory() {
      this.state = RaidState.VICTORY;
      long clearTime = System.currentTimeMillis() - this.startTime;
      String timeFormatted = this.formatTime(clearTime);

      for(UUID uuid : new HashSet(this.activeParticipants)) {
         EntityPlayerMP player = this.getPlayer(uuid);
         if (player != null) {
            sendResultTitle(player, "§a§lRAID COMPLETE", "§eClaim your rewards!", 10, 80, 20);
         }
      }

      this.broadcastMessage("§a§l=== RAID COMPLETE ===");
      this.broadcastMessage("§a" + this.boss.getBossDisplayName() + " has been defeated!");
      this.broadcastMessage("§7Clear Time: §f" + timeFormatted);
      this.broadcastMessage("§7Survivors: §f" + this.activeParticipants.size());
      this.distributeRewards();
      this.cleanup();
   }

   private void onWipe() {
      this.state = RaidState.WIPE;

      for(UUID uuid : new HashSet(this.activeParticipants)) {
         EntityPlayerMP player = this.getPlayer(uuid);
         if (player != null) {
            sendResultTitle(player, "§c§lRAID FAILED", "§7No rewards", 10, 80, 20);
         }
      }

      this.broadcastMessage("§c§l=== RAID FAILED ===");
      this.broadcastMessage("§cAll players have been defeated.");
      this.broadcastMessage("§7No rewards have been given.");
      this.cleanup();
   }

   private void distributeRewards() {
      List<EntityPlayerMP> survivors = new ArrayList();

      for(UUID uuid : this.alivePlayers) {
         EntityPlayerMP player = this.getPlayer(uuid);
         if (player != null) {
            survivors.add(player);
         }
      }

      if (survivors.isEmpty()) {
         this.broadcastMessage("§7No survivors to receive rewards.");
      } else {
         RaidRewardDistributor.distributeRewards(this, survivors);
      }
   }

   private void cleanup() {
      if (this.bossEntity != null && !this.bossEntity.isDead) {
         this.bossEntity.setDead();
      }

      this.despawnAllSpawnedEntities();
      HashiramaMechanics.resetMechanicState(this.raidId);
      ItachiMechanics.resetMechanicState(this.raidId);
      KimimaroMechanics.resetMechanicState(this.raidId);
      this.cleanupAllTrackedBlocks();
      this.arena.setInUse(false);
      RaidRecoveryData recoveryData = RaidRecoveryData.get(this.world);
      recoveryData.clearRaid(this.raidId);
      recoveryData.markArenaFree(this.arena.getArenaId());
   }

   public void trackSpawnedEntity(Entity entity) {
      if (entity != null) {
         this.spawnedEntityIds.add(entity.getEntityId());
         this.spawnedEntityUUIDs.add(entity.getUniqueID());
         entity.getEntityData().setBoolean("isRaidEntity", true);
         entity.getEntityData().setInteger("raidId", this.raidId);
         RaidRecoveryData recoveryData = RaidRecoveryData.get(this.world);
         recoveryData.trackEntityForRaid(this.raidId, entity.getUniqueID());
      }

   }

   public void trackSpawnedEntityById(int entityId) {
      this.spawnedEntityIds.add(entityId);
      if (this.world != null) {
         Entity entity = this.world.getEntityByID(entityId);
         if (entity != null) {
            this.spawnedEntityUUIDs.add(entity.getUniqueID());
            entity.getEntityData().setBoolean("isRaidEntity", true);
            entity.getEntityData().setInteger("raidId", this.raidId);
            RaidRecoveryData recoveryData = RaidRecoveryData.get(this.world);
            recoveryData.trackEntityForRaid(this.raidId, entity.getUniqueID());
         }
      }

   }

   public void untrackSpawnedEntity(Entity entity) {
      if (entity != null) {
         this.spawnedEntityIds.remove(entity.getEntityId());
         this.spawnedEntityUUIDs.remove(entity.getUniqueID());
      }

   }

   private void despawnAllSpawnedEntities() {
      if (this.world != null) {
         int despawnedCount = 0;
         Set<UUID> alreadyDespawned = new HashSet();

         for(int entityId : this.spawnedEntityIds) {
            Entity entity = this.world.getEntityByID(entityId);
            if (entity != null && !entity.isDead) {
               this.spawnDespawnParticles(entity);
               entity.setDead();
               alreadyDespawned.add(entity.getUniqueID());
               ++despawnedCount;
            }
         }

         if (this.world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer)this.world;

            for(UUID uuid : this.spawnedEntityUUIDs) {
               if (!alreadyDespawned.contains(uuid)) {
                  Entity entity = worldServer.getEntityFromUuid(uuid);
                  if (entity != null && !entity.isDead) {
                     this.spawnDespawnParticles(entity);
                     entity.setDead();
                     ++despawnedCount;
                  }
               }
            }
         }

         if (despawnedCount > 0) {
            System.out.println("[RaidInstance] Cleaned up " + despawnedCount + " spawned entities for raid #" + this.raidId);
         }

         this.spawnedEntityIds.clear();
         this.spawnedEntityUUIDs.clear();
      }
   }

   private void spawnDespawnParticles(Entity entity) {
      if (this.world instanceof WorldServer) {
         WorldServer worldServer = (WorldServer)this.world;
         worldServer.spawnParticle(EnumParticleTypes.SMOKE_LARGE, entity.posX, entity.posY + (double)1.0F, entity.posZ, 20, (double)1.0F, (double)1.0F, (double)1.0F, 0.05, new int[0]);
         worldServer.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, entity.posX, entity.posY + (double)1.0F, entity.posZ, 30, (double)1.5F, (double)1.5F, (double)1.5F, (double)0.0F, new int[0]);
      }

   }

   public int getActiveSpawnedEntityCount() {
      int count = 0;

      for(int entityId : this.spawnedEntityIds) {
         Entity entity = this.world.getEntityByID(entityId);
         if (entity != null && !entity.isDead) {
            ++count;
         }
      }

      return count;
   }

   public boolean isRaidEntity(Entity entity) {
      if (entity == null) {
         return false;
      } else {
         return entity == this.bossEntity ? true : this.spawnedEntityIds.contains(entity.getEntityId());
      }
   }

   public void trackBlock(BlockPos pos) {
      if (pos != null) {
         if (this.arena != null && !this.arena.isInBounds(pos)) {
            if (this.world != null) {
               this.world.setBlockToAir(pos);
            }

            return;
         }

         this.trackedBlocks.add(pos);
         RaidRecoveryData recoveryData = RaidRecoveryData.get(this.world);
         recoveryData.trackBlockForRaid(this.raidId, pos);
      }

   }

   public void trackBlocks(Collection<BlockPos> positions) {
      if (positions != null && !positions.isEmpty()) {
         List<BlockPos> validPositions = new ArrayList();

         for(BlockPos pos : positions) {
            if (this.arena != null && !this.arena.isInBounds(pos)) {
               if (this.world != null) {
                  this.world.setBlockToAir(pos);
               }
            } else {
               validPositions.add(pos);
            }
         }

         if (!validPositions.isEmpty()) {
            this.trackedBlocks.addAll(validPositions);
            RaidRecoveryData recoveryData = RaidRecoveryData.get(this.world);
            recoveryData.trackBlocksForRaid(this.raidId, validPositions);
         }
      }

   }

   private void cleanupAllTrackedBlocks() {
      if (this.world != null) {
         int removed = 0;

         for(BlockPos pos : this.trackedBlocks) {
            Block block = this.world.getBlockState(pos).getBlock();
            if (block != Blocks.AIR && block != Blocks.BEDROCK && block != Blocks.BARRIER) {
               this.world.setBlockToAir(pos);
               ++removed;
            }
         }

         if (removed > 0) {
            System.out.println("[RaidInstance] Cleaned up " + removed + " tracked blocks for raid #" + this.raidId);
         }

         this.trackedBlocks.clear();
         RaidRecoveryData recoveryData = RaidRecoveryData.get(this.world);
         recoveryData.clearRaidBlocks(this.raidId);
      }
   }

   public void setBoss(EntityLivingBase entity) {
      if (!(entity instanceof IRaidBoss)) {
         throw new IllegalArgumentException("Boss entity must implement IRaidBoss");
      } else {
         this.bossEntity = entity;
         this.boss = (IRaidBoss)entity;
         this.bossEntityId = entity.getEntityId();
         this.boss.setRaidInstance(this);
         this.boss.setDifficulty(this.difficulty);
         entity.getEntityData().setBoolean("isRaidEntity", true);
         entity.getEntityData().setInteger("raidId", this.raidId);
         RaidRecoveryData recoveryData = RaidRecoveryData.get(this.world);
         recoveryData.trackEntityForRaid(this.raidId, entity.getUniqueID());
         recoveryData.markArenaInUse(this.arena.getArenaId());
      }
   }

   public void startFight() {
      this.state = RaidState.ACTIVE;
      this.broadcastMessage("§e§l=== RAID STARTED ===");
      this.broadcastMessage("§eDifficulty: " + this.difficulty.getColoredName());
      this.broadcastMessage("§7Defeat " + this.boss.getBossDisplayName() + " within 17 minutes 30 seconds!");
   }

   public static void sendResultTitle(EntityPlayerMP player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
      player.connection.sendPacket(new SPacketTitle(Type.TIMES, (ITextComponent)null, fadeIn, stay, fadeOut));
      ITextComponent titleText = new TextComponentString(title);
      player.connection.sendPacket(new SPacketTitle(Type.TITLE, titleText));
      if (subtitle != null && !subtitle.isEmpty()) {
         ITextComponent subtitleText = new TextComponentString(subtitle);
         player.connection.sendPacket(new SPacketTitle(Type.SUBTITLE, subtitleText));
      }

   }

   public void broadcastMessage(String message) {
      for(UUID uuid : this.activeParticipants) {
         EntityPlayerMP player = this.getPlayer(uuid);
         if (player != null) {
            player.sendMessage(new TextComponentString(message));
         }
      }

   }

   public void broadcastToAllOriginalMembers(String message) {
      for(UUID uuid : this.party.getMemberUUIDs()) {
         EntityPlayerMP player = this.getPlayer(uuid);
         if (player != null) {
            player.sendMessage(new TextComponentString(message));
         }
      }

   }

   private EntityPlayerMP getPlayer(UUID uuid) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      return server != null ? server.getPlayerList().getPlayerByUUID(uuid) : null;
   }

   private String formatTime(long millis) {
      long seconds = millis / 1000L;
      long minutes = seconds / 60L;
      seconds %= 60L;
      return String.format("%d:%02d", minutes, seconds);
   }

   public int getRaidId() {
      return this.raidId;
   }

   public String getBossId() {
      return this.bossId;
   }

   public RaidDifficulty getDifficulty() {
      return this.difficulty;
   }

   public RaidParty getParty() {
      return this.party;
   }

   public RaidArena getArena() {
      return this.arena;
   }

   public World getWorld() {
      return this.world;
   }

   public IRaidBoss getBoss() {
      return this.boss;
   }

   public EntityLivingBase getBossEntity() {
      return this.bossEntity;
   }

   public RaidState getState() {
      return this.state;
   }

   public int getEnrageLevel() {
      return this.enrageLevel;
   }

   public int getCurrentPhase() {
      return this.currentPhase;
   }

   public String getCurrentPhaseName() {
      return this.currentPhaseName;
   }

   public Set<UUID> getAlivePlayers() {
      return Collections.unmodifiableSet(this.alivePlayers);
   }

   public Set<UUID> getDeadPlayers() {
      return Collections.unmodifiableSet(this.deadPlayers);
   }

   public long getStartTime() {
      return this.startTime;
   }

   public long getElapsedTime() {
      return System.currentTimeMillis() - this.startTime;
   }

   public List<EntityPlayerMP> getParticipants() {
      List<EntityPlayerMP> participants = new ArrayList();

      for(UUID uuid : this.activeParticipants) {
         EntityPlayerMP player = this.getPlayer(uuid);
         if (player != null) {
            participants.add(player);
         }
      }

      return participants;
   }

   public List<EntityPlayerMP> getAllOriginalParticipants() {
      List<EntityPlayerMP> participants = new ArrayList();

      for(UUID uuid : this.party.getMemberUUIDs()) {
         EntityPlayerMP player = this.getPlayer(uuid);
         if (player != null) {
            participants.add(player);
         }
      }

      return participants;
   }

   public boolean isPlayerAlive(UUID uuid) {
      return this.alivePlayers.contains(uuid);
   }

   public boolean isActiveParticipant(UUID uuid) {
      return this.activeParticipants.contains(uuid);
   }

   public boolean isActiveParticipant(EntityPlayerMP player) {
      return player != null && this.activeParticipants.contains(player.getUniqueID());
   }

   public Set<UUID> getActiveParticipants() {
      return Collections.unmodifiableSet(this.activeParticipants);
   }

   public static enum RaidState {
      STARTING,
      ACTIVE,
      VICTORY,
      WIPE,
      TIMEOUT,
      CANCELLED;
   }
}
