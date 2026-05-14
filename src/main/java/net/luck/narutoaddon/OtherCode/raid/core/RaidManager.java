
package net.luck.narutoaddon.OtherCode.raid.core;

import net.luck.narutoaddon.OtherCode.raid.arena.RaidArena;
import net.luck.narutoaddon.OtherCode.raid.arena.RaidArenaStorage;
import net.luck.narutoaddon.OtherCode.raid.boss.BossRegistry;
import net.luck.narutoaddon.OtherCode.raid.boss.EntityRaidBoss;
import net.luck.narutoaddon.OtherCode.raid.boss.IRaidBoss;
import net.luck.narutoaddon.OtherCode.raid.network.RaidNetworkHelper;
import net.luck.narutoaddon.OtherCode.raid.party.RaidParty;
import net.luck.narutoaddon.OtherCode.raid.party.RaidPartyStorage;
import net.luck.narutoaddon.OtherCode.raid.party.RaidQueue;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextComponent.Serializer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class RaidManager {
   private static RaidManager instance;
   private Map<Integer, RaidInstance> activeRaids = new HashMap();
   private Map<UUID, Integer> playerToRaid = new HashMap();
   private int raidIdCounter = 0;
   private RaidQueue queue = new RaidQueue();
   private int tickCounter = 0;
   private Map<Integer, Long> countdownRaids = new HashMap();
   public static final long COUNTDOWN_DURATION = 3000L;
   private Map<UUID, BlockPos> preRaidPositions = new HashMap();
   private Set<UUID> disconnectPenaltyPlayers = new HashSet();
   private Map<UUID, Long> raidCooldowns = new HashMap();
   private static final long RAID_COOLDOWN_MS = 300000L;
   private World cachedWorld = null;

   public static RaidManager getInstance() {
      if (instance == null) {
         instance = new RaidManager();
      }

      return instance;
   }

   public static void reset() {
      instance = null;
   }

   public void onServerTick(World world) {
      ++this.tickCounter;
      if (this.tickCounter % 20 == 0) {
         this.processQueue(world);
      }

      this.processCountdowns(world);
      if (this.tickCounter % 1200 == 0) {
         long now = System.currentTimeMillis();
         this.raidCooldowns.entrySet().removeIf((e) -> now >= (Long)e.getValue());
      }

      for(RaidInstance raid : new ArrayList(this.activeRaids.values())) {
         raid.tick();
         if (raid.getState() == RaidInstance.RaidState.VICTORY || raid.getState() == RaidInstance.RaidState.WIPE || raid.getState() == RaidInstance.RaidState.TIMEOUT || raid.getState() == RaidInstance.RaidState.CANCELLED) {
            this.cleanupRaid(raid);
         }
      }

   }

   private void processCountdowns(World world) {
      long now = System.currentTimeMillis();
      List<Integer> finishedCountdowns = new ArrayList();

      for(Map.Entry<Integer, Long> entry : this.countdownRaids.entrySet()) {
         int raidId = (Integer)entry.getKey();
         long startTime = (Long)entry.getValue();
         RaidInstance raid = (RaidInstance)this.activeRaids.get(raidId);
         if (raid != null && raid.getState() == RaidInstance.RaidState.STARTING) {
            long elapsed = now - startTime;
            int secondsRemaining = (int)((3000L - elapsed) / 1000L) + 1;
            long prevElapsed = elapsed - 50L;
            int prevSecondsRemaining = (int)((3000L - prevElapsed) / 1000L) + 1;
            if (secondsRemaining != prevSecondsRemaining && secondsRemaining > 0 && secondsRemaining <= 3) {
               String color = secondsRemaining == 1 ? "red" : (secondsRemaining == 2 ? "gold" : "yellow");

               for(EntityPlayerMP player : raid.getParticipants()) {
                  this.sendCountdownTitle(player, secondsRemaining, color);
               }
            }

            if (elapsed >= 3000L) {
               finishedCountdowns.add(raidId);

               for(EntityPlayerMP player : raid.getParticipants()) {
                  this.sendFightTitle(player);
               }

               this.spawnBoss(raid, world);
            }
         } else {
            finishedCountdowns.add(raidId);
         }
      }

      for(int raidId : finishedCountdowns) {
         this.countdownRaids.remove(raidId);
      }

   }

   private void sendCountdownTitle(EntityPlayerMP player, int number, String color) {
      if (player.connection != null) {
         player.connection.sendPacket(new SPacketTitle(Type.TIMES, (ITextComponent)null, 0, 20, 10));
         String titleJson = "{\"text\":\"" + number + "\",\"color\":\"" + color + "\",\"bold\":true}";
         ITextComponent titleText = Serializer.jsonToComponent(titleJson);
         player.connection.sendPacket(new SPacketTitle(Type.TITLE, titleText));
      }
   }

   private void sendFightTitle(EntityPlayerMP player) {
      if (player.connection != null) {
         player.connection.sendPacket(new SPacketTitle(Type.TIMES, (ITextComponent)null, 0, 30, 10));
         String titleJson = "{\"text\":\"⚔ RAID START! ⚔\",\"color\":\"green\",\"bold\":true}";
         ITextComponent titleText = Serializer.jsonToComponent(titleJson);
         player.connection.sendPacket(new SPacketTitle(Type.TITLE, titleText));
      }
   }

   private void processQueue(World world) {
      RaidArenaStorage arenaStorage = RaidArenaStorage.get(world);
      if (arenaStorage != null) {
         Set<String> processedCombos = new HashSet();

         for(RaidQueue.QueueEntry entry : this.queue.getAllEntries()) {
            String combo = entry.bossId + ":" + entry.difficulty.name();
            if (!processedCombos.contains(combo)) {
               processedCombos.add(combo);
               RaidArena arena = arenaStorage.findAvailableArena(entry.bossId);
               if (arena != null) {
                  int requiredPlayers = entry.difficulty.getMinPartySize();
                  RaidQueue.MatchedGroup match = this.queue.tryMatchGroup(entry.bossId, entry.difficulty, requiredPlayers);
                  if (match != null) {
                     System.out.println("[RaidManager] Matched " + match.getTotalPlayers() + " players for " + entry.bossId + " " + entry.difficulty.name());
                     this.startRaid(world, match.mergedParty, match.bossId, match.difficulty, arena);
                  }
               }
            }
         }

      }
   }

   public RaidInstance startRaid(String bossId, RaidDifficulty difficulty, List<EntityPlayerMP> players, World world, BlockPos spawnPos) {
      if (players != null && !players.isEmpty()) {
         EntityPlayerMP leader = (EntityPlayerMP)players.get(0);
         RaidParty tempParty = new RaidParty(leader);

         for(int i = 1; i < players.size(); ++i) {
            tempParty.addMember((EntityPlayerMP)players.get(i));
         }

         tempParty.setState(RaidParty.PartyState.READY);
         int tempArenaId = (int)(System.currentTimeMillis() % 2147483647L);
         RaidArena tempArena = new RaidArena(tempArenaId, "temp_" + tempArenaId, bossId, spawnPos.add(-30, -5, -30), spawnPos.add(30, 20, 30));
         tempArena.setBossSpawn(spawnPos);

         for(int i = 0; i < 6; ++i) {
            double angle = (Math.PI * 2D) * (double)i / (double)6.0F;
            int x = (int)((double)spawnPos.getX() + (double)10.0F * Math.cos(angle));
            int z = (int)((double)spawnPos.getZ() + (double)10.0F * Math.sin(angle));
            tempArena.setPlayerSpawn(i, new BlockPos(x, spawnPos.getY(), z));
         }

         return this.startRaid(world, tempParty, bossId, difficulty, tempArena);
      } else {
         return null;
      }
   }

   public RaidInstance startRaid(World world, RaidParty party, String bossId, RaidDifficulty difficulty, RaidArena arena) {
      int raidId = ++this.raidIdCounter;
      RaidInstance raid = new RaidInstance(raidId, bossId, difficulty, party, arena, world);
      arena.setInUse(true);
      this.activeRaids.put(raidId, raid);

      for(UUID uuid : party.getMemberUUIDs()) {
         this.playerToRaid.put(uuid, raidId);
      }

      party.setState(RaidParty.PartyState.IN_RAID);
      party.setCurrentRaidId(raidId);
      this.teleportPartyToArena(party, arena, world);
      this.startRaidCountdown(raid, world);
      return raid;
   }

   private void teleportPartyToArena(RaidParty party, RaidArena arena, World world) {
      List<UUID> members = new ArrayList(party.getMemberUUIDs());
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      BlockPos bossSpawn = arena.getBossSpawn();
      System.out.println("[RaidManager] Teleporting " + members.size() + " players to arena " + arena.getName());
      System.out.println("[RaidManager] Boss spawn: " + bossSpawn);

      for(int i = 0; i < members.size() && i < 6; ++i) {
         UUID uuid = (UUID)members.get(i);
         EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
         if (player != null) {
            BlockPos originalPos = new BlockPos(player.posX, player.posY, player.posZ);
            this.preRaidPositions.put(uuid, originalPos);
            RaidRecoveryData.get(world).savePlayerPreRaidPosition(uuid, originalPos);
            System.out.println("[RaidManager] Saved original position for " + player.getName() + ": " + originalPos);
            BlockPos spawn = arena.getPlayerSpawn(i);
            System.out.println("[RaidManager] Player spawn " + i + ": " + spawn);
            if (spawn != null) {
               double x = (double)spawn.getX() + (double)0.5F;
               double z = (double)spawn.getZ() + (double)0.5F;
               double y = this.findSafeY(world, spawn.getX(), spawn.getY(), spawn.getZ());
               float yaw = player.rotationYaw;
               if (bossSpawn != null) {
                  double dx = (double)(bossSpawn.getX() - spawn.getX());
                  double dz = (double)(bossSpawn.getZ() - spawn.getZ());
                  yaw = (float)(Math.atan2(dz, dx) * (double)180.0F / Math.PI) - 90.0F;
               }

               player.connection.setPlayerLocation(x, y, z, yaw, player.rotationPitch);
               player.setPositionAndUpdate(x, y, z);
               player.rotationYaw = yaw;
               System.out.println("[RaidManager] Teleported " + player.getName() + " to " + x + ", " + y + ", " + z);
            } else {
               System.out.println("[RaidManager] WARNING: No spawn point configured for index " + i);
            }
         } else {
            System.out.println("[RaidManager] WARNING: Player not found for UUID " + uuid);
         }
      }

      for(UUID uuid : members) {
         this.applyRaidCooldown(uuid);
      }

   }

   private double findSafeY(World world, int x, int targetY, int z) {
      for(int y = targetY; y < targetY + 10 && y < 255; ++y) {
         Material feetMat = world.getBlockState(new BlockPos(x, y, z)).getMaterial();
         Material headMat = world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial();
         Material belowMat = world.getBlockState(new BlockPos(x, y - 1, z)).getMaterial();
         boolean feetClear = !feetMat.isSolid();
         boolean headClear = !headMat.isSolid();
         boolean hasFloor = belowMat.isSolid();
         if (feetClear && headClear && hasFloor) {
            return (double)y;
         }
      }

      return (double)targetY;
   }

   private void startRaidCountdown(RaidInstance raid, World world) {
      raid.broadcastMessage("§e§l=== RAID STARTING ===");
      raid.broadcastMessage("§7Boss: §f" + BossRegistry.getBossDisplayName(raid.getBossId()));
      raid.broadcastMessage("§7Difficulty: " + raid.getDifficulty().getColoredName());
      raid.broadcastMessage("§7Get ready...");
      this.countdownRaids.put(raid.getRaidId(), System.currentTimeMillis());
   }

   private void spawnBoss(RaidInstance raid, World world) {
      RaidArena arena = raid.getArena();
      BlockPos bossSpawn = arena.getBossSpawn();
      if (bossSpawn == null) {
         raid.broadcastMessage("§cError: Boss spawn point not configured for arena!");
         this.cancelRaid(raid.getRaidId());
      } else {
         EntityLivingBase bossEntity = BossRegistry.createBoss(raid.getBossId(), world);
         if (bossEntity != null && bossEntity instanceof IRaidBoss) {
            IRaidBoss boss = (IRaidBoss)bossEntity;
            double x = (double)bossSpawn.getX() + (double)0.5F;
            double y = this.findSafeY(world, bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ());
            double z = (double)bossSpawn.getZ() + (double)0.5F;
            bossEntity.setLocationAndAngles(x, y, z, 0.0F, 0.0F);
            world.spawnEntity(bossEntity);
            bossEntity.setPosition(x, y, z);
            raid.setBoss(bossEntity);
            raid.startFight();
            RaidNetworkHelper.sendBossSpawn(raid, bossEntity);
            raid.broadcastMessage("§c§l" + boss.getBossDisplayName() + " has appeared!");
            System.out.println("[RaidManager] Spawned boss " + raid.getBossId() + " at " + x + ", " + y + ", " + z);
         } else {
            raid.broadcastMessage("§cError: Failed to spawn boss! Boss type not registered: " + raid.getBossId());
            this.cancelRaid(raid.getRaidId());
         }

      }
   }

   public void cancelRaid(int raidId) {
      RaidInstance raid = (RaidInstance)this.activeRaids.get(raidId);
      if (raid != null) {
         raid.broadcastMessage("§c§lRaid has been cancelled.");
         this.cleanupRaid(raid);
      }

   }

   private void cleanupRaid(RaidInstance raid) {
      if (raid.getState() == RaidInstance.RaidState.VICTORY) {
         RaidNetworkHelper.sendBossDeath(raid);
      } else {
         RaidNetworkHelper.sendBossDespawn(raid);
      }

      this.activeRaids.remove(raid.getRaidId());

      for(UUID uuid : raid.getParty().getMemberUUIDs()) {
         this.playerToRaid.remove(uuid);
      }

      raid.getArena().setInUse(false);
      this.teleportPartyToHub(raid.getParty(), raid.getWorld());
      this.disbandPartyAfterRaid(raid.getParty(), raid.getWorld());
   }

   private void disbandPartyAfterRaid(RaidParty party, World world) {
      RaidPartyStorage storage = RaidPartyStorage.get(world);
      if (storage != null) {
         UUID partyId = party.getPartyId();
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

         for(UUID uuid : party.getMemberUUIDs()) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
            if (player != null) {
               player.sendMessage(new TextComponentString("§7Your raid party has been disbanded."));
            }
         }

         storage.removeParty(partyId);
         System.out.println("[RaidManager] Disbanded party " + partyId + " after raid completion");
      }
   }

   private void teleportPartyToHub(RaidParty party, World world) {
      RaidArenaStorage storage = RaidArenaStorage.get(world);
      BlockPos hub = null;
      if (storage != null) {
         hub = storage.getHubLocation();
      }

      if (hub == null) {
         hub = new BlockPos(0, 65, 0);
      }

      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

      for(UUID uuid : party.getMemberUUIDs()) {
         EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
         if (player != null) {
            BlockPos originalPos = (BlockPos)this.preRaidPositions.remove(uuid);
            RaidRecoveryData.get(player.world).clearPlayerPreRaidPosition(uuid);
            if (originalPos != null) {
               double x = (double)originalPos.getX() + (double)0.5F;
               double y = (double)originalPos.getY();
               double z = (double)originalPos.getZ() + (double)0.5F;
               player.connection.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
               player.setPositionAndUpdate(x, y, z);
               System.out.println("[RaidManager] Returned " + player.getName() + " to original position: " + originalPos);
            } else {
               player.setPositionAndUpdate((double)hub.getX() + (double)0.5F, (double)hub.getY(), (double)hub.getZ() + (double)0.5F);
               System.out.println("[RaidManager] No original position found for " + player.getName() + ", teleported to hub");
            }
         }
      }

   }

   public void clearPreRaidPosition(UUID playerUUID) {
      this.preRaidPositions.remove(playerUUID);
   }

   public BlockPos getPreRaidPosition(UUID playerUUID) {
      return (BlockPos)this.preRaidPositions.get(playerUUID);
   }

   public QueueResult queueSoloPlayer(EntityPlayerMP player, String bossId, RaidDifficulty difficulty) {
      UUID uuid = player.getUniqueID();
      if (this.isOnRaidCooldown(uuid)) {
         long remaining = this.getRaidCooldownRemaining(uuid);
         int minutes = (int)(remaining / 60000L) + 1;
         return new QueueResult(false, "§cYou must wait " + minutes + " minutes before joining another raid.");
      } else if (!BossRegistry.bossExists(bossId)) {
         return new QueueResult(false, "Unknown boss: " + bossId);
      } else if (this.playerToRaid.containsKey(uuid)) {
         return new QueueResult(false, "You are already in a raid.");
      } else if (this.queue.isPlayerInQueue(uuid)) {
         return new QueueResult(false, "You are already in the queue.");
      } else {
         RaidQueue.QueueEntry entry = this.queue.addSoloPlayer(player, bossId, difficulty);
         if (entry == null) {
            return new QueueResult(false, "Failed to join queue.");
         } else {
            int playersInQueue = this.queue.getPlayerCountForBossDifficulty(bossId, difficulty);
            int needed = difficulty.getMinPartySize();
            return new QueueResult(true, "§aQueued for " + BossRegistry.getBossDisplayName(bossId) + " (" + difficulty.getColoredName() + "§a) - " + playersInQueue + "/" + needed + " players");
         }
      }
   }

   public QueueResult queueParty(RaidParty party, String bossId, RaidDifficulty difficulty) {
      if (party.getState() != RaidParty.PartyState.READY) {
         return new QueueResult(false, "Party is not ready. All members must mark themselves ready.");
      } else if (!BossRegistry.bossExists(bossId)) {
         return new QueueResult(false, "Unknown boss: " + bossId);
      } else {
         for(UUID uuid : party.getMemberUUIDs()) {
            if (this.isOnRaidCooldown(uuid)) {
               long remaining = this.getRaidCooldownRemaining(uuid);
               int minutes = (int)(remaining / 60000L) + 1;
               return new QueueResult(false, "§cA party member must wait " + minutes + " minutes before joining another raid.");
            }

            if (this.playerToRaid.containsKey(uuid)) {
               return new QueueResult(false, "A party member is already in a raid.");
            }

            if (this.queue.isPlayerInQueue(uuid)) {
               return new QueueResult(false, "A party member is already in the queue.");
            }
         }

         RaidQueue.QueueEntry entry = this.queue.addParty(party, bossId, difficulty);
         if (entry == null) {
            return new QueueResult(false, "Failed to join queue.");
         } else {
            party.setState(RaidParty.PartyState.QUEUED);
            int playersInQueue = this.queue.getPlayerCountForBossDifficulty(bossId, difficulty);
            int needed = difficulty.getMinPartySize();
            return new QueueResult(true, "§aParty queued for " + BossRegistry.getBossDisplayName(bossId) + " (" + difficulty.getColoredName() + "§a) - " + playersInQueue + "/" + needed + " players");
         }
      }
   }

   public QueueResult leaveQueue(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      if (!this.queue.isPlayerInQueue(uuid)) {
         return new QueueResult(false, "You are not in the queue.");
      } else {
         this.queue.removePlayer(uuid);
         return new QueueResult(true, "§eYou have left the queue.");
      }
   }

   public QueueResult leaveQueue(RaidParty party) {
      if (party.getState() != RaidParty.PartyState.QUEUED) {
         return new QueueResult(false, "Party is not in queue.");
      } else {
         this.queue.removeParty(party.getPartyId());
         party.setState(RaidParty.PartyState.READY);
         return new QueueResult(true, "§eParty has left the queue.");
      }
   }

   public void onPlayerDeath(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      Integer raidId = (Integer)this.playerToRaid.get(uuid);
      if (raidId != null) {
         RaidInstance raid = (RaidInstance)this.activeRaids.get(raidId);
         if (raid != null && raid.getState() == RaidInstance.RaidState.ACTIVE) {
            boolean wasWipe = raid.onPlayerDeath(uuid);
            this.releasePlayerFromRaid(player, raid, !wasWipe);
         }
      }

   }

   public void onPlayerDisconnect(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      Integer raidId = (Integer)this.playerToRaid.get(uuid);
      if (raidId != null) {
         RaidInstance raid = (RaidInstance)this.activeRaids.get(raidId);
         if (raid != null && raid.getState() == RaidInstance.RaidState.ACTIVE) {
            this.disconnectPenaltyPlayers.add(uuid);
            RaidRecoveryData.get(player.world).addDisconnectPenalty(uuid);
            System.out.println("[RaidManager] Player " + player.getName() + " disconnected during raid - added to penalty list");
            boolean wasWipe = raid.onPlayerDeath(uuid);
            this.releasePlayerFromRaid(player, raid, !wasWipe);
         }
      }

   }

   private void releasePlayerFromRaid(EntityPlayerMP player, RaidInstance raid, boolean teleportBack) {
      UUID uuid = player.getUniqueID();
      this.playerToRaid.remove(uuid);
      raid.removePlayerFromRaid(uuid);
      EntityLivingBase bossEntity = raid.getBossEntity();
      if (bossEntity instanceof EntityRaidBoss) {
         ((EntityRaidBoss)bossEntity).onPlayerRemovedFromRaid(player);
      }

      RaidNetworkHelper.sendBossDespawn(raid, player);
      if (teleportBack && player.connection != null) {
         BlockPos originalPos = (BlockPos)this.preRaidPositions.remove(uuid);
         RaidRecoveryData.get(player.world).clearPlayerPreRaidPosition(uuid);
         if (originalPos != null) {
            double x = (double)originalPos.getX() + (double)0.5F;
            double y = (double)originalPos.getY();
            double z = (double)originalPos.getZ() + (double)0.5F;
            player.connection.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
            player.setPositionAndUpdate(x, y, z);
            System.out.println("[RaidManager] Returned eliminated player " + player.getName() + " to original position: " + originalPos);
         }
      }

      System.out.println("[RaidManager] Released player " + player.getName() + " from raid #" + raid.getRaidId() + " - they can now queue again");
   }

   public void onPlayerLogin(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      if (this.disconnectPenaltyPlayers.remove(uuid)) {
         System.out.println("[RaidManager] Player " + player.getName() + " had disconnect penalty - applying death");
         player.sendMessage(new TextComponentString(""));
         player.sendMessage(new TextComponentString("§c§l[RAID PENALTY]"));
         player.sendMessage(new TextComponentString("§cYou disconnected during a raid."));
         player.sendMessage(new TextComponentString("§cDisconnecting during raids is not allowed."));
         player.sendMessage(new TextComponentString(""));
         BlockPos originalPos = (BlockPos)this.preRaidPositions.remove(uuid);
         RaidRecoveryData.get(player.world).clearPlayerPreRaidPosition(uuid);
         if (originalPos != null) {
            player.setPositionAndUpdate((double)originalPos.getX() + (double)0.5F, (double)originalPos.getY(), (double)originalPos.getZ() + (double)0.5F);
         }

         player.attackEntityFrom(DamageSource.OUT_OF_WORLD, 10000.0F);
      }

   }

   public boolean hasDisconnectPenalty(UUID uuid) {
      return this.disconnectPenaltyPlayers.contains(uuid);
   }

   public void clearDisconnectPenalty(UUID uuid) {
      this.disconnectPenaltyPlayers.remove(uuid);
   }

   public void applyRaidCooldown(UUID playerId) {
      this.raidCooldowns.put(playerId, System.currentTimeMillis() + 300000L);
   }

   public boolean isOnRaidCooldown(UUID playerId) {
      Long end = (Long)this.raidCooldowns.get(playerId);
      return end != null && System.currentTimeMillis() < end;
   }

   public long getRaidCooldownRemaining(UUID playerId) {
      Long end = (Long)this.raidCooldowns.get(playerId);
      if (end == null) {
         return 0L;
      } else {
         long remaining = end - System.currentTimeMillis();
         return remaining > 0L ? remaining : 0L;
      }
   }

   public void clearRaidCooldown(UUID playerId) {
      this.raidCooldowns.remove(playerId);
      if (this.cachedWorld != null) {
         RaidRecoveryData.get(this.cachedWorld).clearRaidCooldown(playerId);
      }

   }

   public void loadCooldownsFromSavedData(World world) {
      this.cachedWorld = world;
      RaidRecoveryData data = RaidRecoveryData.get(world);
      Map<String, Long> persisted = data.getAllRaidCooldowns();
      long now = System.currentTimeMillis();
      int loaded = 0;

      for(Map.Entry<String, Long> entry : persisted.entrySet()) {
         if ((Long)entry.getValue() > now) {
            try {
               UUID uuid = UUID.fromString((String)entry.getKey());
               this.raidCooldowns.put(uuid, entry.getValue());
               ++loaded;
            } catch (IllegalArgumentException var10) {
            }
         }
      }

      data.cleanExpiredCooldowns();
      if (loaded > 0) {
         System.out.println("[RaidManager] Loaded " + loaded + " persisted raid cooldown(s).");
      }

   }

   public RaidInstance getRaid(int raidId) {
      return (RaidInstance)this.activeRaids.get(raidId);
   }

   public RaidInstance getPlayerRaid(UUID playerUUID) {
      Integer raidId = (Integer)this.playerToRaid.get(playerUUID);
      return raidId != null ? (RaidInstance)this.activeRaids.get(raidId) : null;
   }

   public boolean isPlayerInRaid(UUID playerUUID) {
      return this.playerToRaid.containsKey(playerUUID);
   }

   public Map<UUID, RaidInstance> getPlayerRaidMap() {
      Map<UUID, RaidInstance> result = new HashMap();

      for(Map.Entry<UUID, Integer> entry : this.playerToRaid.entrySet()) {
         RaidInstance raid = (RaidInstance)this.activeRaids.get(entry.getValue());
         if (raid != null) {
            result.put(entry.getKey(), raid);
         }
      }

      return result;
   }

   public int getActiveRaidCount() {
      return this.activeRaids.size();
   }

   public int getQueueSize() {
      return this.queue.getQueuedParties().size();
   }

   public RaidQueue getQueue() {
      return this.queue;
   }

   public static class QueueResult {
      public final boolean success;
      public final String message;

      public QueueResult(boolean success, String message) {
         this.success = success;
         this.message = message;
      }
   }
}
