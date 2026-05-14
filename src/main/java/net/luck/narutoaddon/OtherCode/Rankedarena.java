
package net.luck.narutoaddon.OtherCode;

import net.luck.narutoaddon.OtherCode.network.RankedMatchHudMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Rankedarena extends WorldSavedData {
   private static final String DATA_NAME = "RankedArenaData";
   public static final String STATE_COUNTDOWN = "countdown";
   public static final String STATE_ACTIVE = "active";
   public static final String STATE_FINISHED = "finished";
   public static final String STATE_PAUSED = "paused";
   public static final int COUNTDOWN_DURATION = 3000;
   public static final int INVULNERABILITY_DURATION = 3000;
   public static final int MATCH_TIME_LIMIT = 300000;
   public static final int DISCONNECT_GRACE_PERIOD = 30000;
   public static final int RESULT_DISPLAY_DURATION = 5000;
   private BlockPos hubLocation = new BlockPos(0, 65, 0);
   private static final Map<String, Long> pendingTeleports = new ConcurrentHashMap();
   private Map<Integer, Arena> arenas = new HashMap();
   private int nextArenaId = 1;
   private static Map<String, BlockPos> originalPositions = new HashMap();

   public static void scheduleDelayedTeleport(String uuid, long delayMs) {
      pendingTeleports.put(uuid, System.currentTimeMillis() + delayMs);
   }

   public static void processPendingTeleports(World world) {
      if (!pendingTeleports.isEmpty()) {
         long now = System.currentTimeMillis();
         Iterator<Map.Entry<String, Long>> iterator = pendingTeleports.entrySet().iterator();

         while(iterator.hasNext()) {
            Map.Entry<String, Long> entry = (Map.Entry)iterator.next();
            if (now >= (Long)entry.getValue()) {
               String uuid = (String)entry.getKey();
               iterator.remove();
               EntityPlayerMP player = getPlayer(world, uuid);
               if (player != null) {
                  returnToOriginalPosition(player);
                  luckAddonAddon.PACKET_HANDLER.sendTo(new RankedMatchHudMessage(false), player);
               }
            }
         }

      }
   }

   public static void cancelPendingTeleport(String uuid) {
      pendingTeleports.remove(uuid);
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

   public Rankedarena() {
      super("RankedArenaData");
      this.initializeDefaultArenas();
   }

   public Rankedarena(String name) {
      super(name);
      this.initializeDefaultArenas();
   }

   private void initializeDefaultArenas() {
      if (this.arenas.isEmpty()) {
         this.arenas.put(1, new Arena(1, "Valley of the End", "The legendary battleground where Hashirama and Madara clashed", "§b", new BlockPos(1000, 70, 1000), new BlockPos(1030, 70, 1000), new BlockPos(1015, 68, 1000), new BlockPos(980, 50, 980), new BlockPos(1050, 120, 1020), true, 45));
         this.arenas.put(2, new Arena(2, "Chunin Exam Arena", "The formal arena where ninja prove their worth", "§e", new BlockPos(2000, 65, 2000), new BlockPos(2030, 65, 2000), new BlockPos(2015, 65, 2000), new BlockPos(1980, 55, 1980), new BlockPos(2050, 100, 2020), false, 0));
         this.arenas.put(3, new Arena(3, "Forest of Death", "A dangerous training ground filled with peril", "§2", new BlockPos(3000, 70, 3000), new BlockPos(3040, 70, 3000), new BlockPos(3020, 70, 3000), new BlockPos(2970, 50, 2970), new BlockPos(3070, 120, 3030), true, 40));
         this.nextArenaId = 4;
      }

   }

   public void readFromNBT(NBTTagCompound nbt) {
      this.arenas.clear();
      this.hubLocation = new BlockPos(nbt.getInteger("hubX"), nbt.hasKey("hubY") ? nbt.getInteger("hubY") : 65, nbt.getInteger("hubZ"));
      this.nextArenaId = nbt.getInteger("nextArenaId");
      if (this.nextArenaId == 0) {
         this.nextArenaId = 1;
      }

      NBTTagList arenaList = nbt.getTagList("arenas", 10);

      for(int i = 0; i < arenaList.tagCount(); ++i) {
         Arena arena = Arena.readFromNBT(arenaList.getCompoundTagAt(i));
         this.arenas.put(arena.id, arena);
         if (arena.id >= this.nextArenaId) {
            this.nextArenaId = arena.id + 1;
         }
      }

      if (this.arenas.isEmpty()) {
         this.initializeDefaultArenas();
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      nbt.setInteger("hubX", this.hubLocation.getX());
      nbt.setInteger("hubY", this.hubLocation.getY());
      nbt.setInteger("hubZ", this.hubLocation.getZ());
      nbt.setInteger("nextArenaId", this.nextArenaId);
      NBTTagList arenaList = new NBTTagList();

      for(Arena arena : this.arenas.values()) {
         arenaList.appendTag(arena.writeToNBT());
      }

      nbt.setTag("arenas", arenaList);
      return nbt;
   }

   public static Rankedarena get(World world) {
      MapStorage storage = world.getMapStorage();
      Rankedarena instance = (Rankedarena)storage.getOrLoadData(Rankedarena.class, "RankedArenaData");
      if (instance == null) {
         instance = new Rankedarena();
         storage.setData("RankedArenaData", instance);
      }

      return instance;
   }

   public static Arena getArena(World world, int arenaId) {
      Rankedarena data = get(world);
      return (Arena)data.arenas.get(arenaId);
   }

   /** @deprecated */
   @Deprecated
   public static Arena getArena(int arenaId) {
      return null;
   }

   public static Map<Integer, Arena> getAllArenas(World world) {
      Rankedarena data = get(world);
      return new HashMap(data.arenas);
   }

   public static List<Arena> getEnabledArenas(World world) {
      Rankedarena data = get(world);
      List<Arena> enabled = new ArrayList();

      for(Arena arena : data.arenas.values()) {
         if (arena.enabled) {
            enabled.add(arena);
         }
      }

      return enabled;
   }

   public static Arena getRandomArena(World world) {
      List<Arena> enabled = getEnabledArenas(world);
      if (enabled.isEmpty()) {
         return null;
      } else {
         int index = (int)(Math.random() * (double)enabled.size());
         return (Arena)enabled.get(index);
      }
   }

   public static void teleportToArena(EntityPlayerMP player, int arenaId, boolean isPlayer1) {
      Arena arena = getArena(player.world, arenaId);
      if (arena == null) {
         System.out.println("[Ranked] ERROR: Cannot teleport - arena " + arenaId + " not found!");
      } else {
         originalPositions.put(player.getUniqueID().toString(), new BlockPos(player.posX, player.posY, player.posZ));
         BlockPos spawn = isPlayer1 ? arena.player1Spawn : arena.player2Spawn;
         System.out.println("[Ranked] Teleporting " + player.getName() + " to arena " + arenaId + " (" + arena.name + ") at " + spawn.getX() + ", " + spawn.getY() + ", " + spawn.getZ());
         player.connection.setPlayerLocation((double)spawn.getX() + (double)0.5F, (double)spawn.getY(), (double)spawn.getZ() + (double)0.5F, player.rotationYaw, player.rotationPitch);
         double dx = (double)(arena.centerPoint.getX() - spawn.getX());
         double dz = (double)(arena.centerPoint.getZ() - spawn.getZ());
         float yaw = (float)(Math.atan2(dz, dx) * (double)180.0F / Math.PI) - 90.0F;
         player.rotationYaw = yaw;
         player.setPositionAndUpdate((double)spawn.getX() + (double)0.5F, (double)spawn.getY(), (double)spawn.getZ() + (double)0.5F);
      }
   }

   public static void returnToOriginalPosition(EntityPlayerMP player) {
      String uuid = player.getUniqueID().toString();
      BlockPos original = (BlockPos)originalPositions.get(uuid);
      Rankedarena data = get(player.world);
      if (original != null) {
         player.setPositionAndUpdate((double)original.getX() + (double)0.5F, (double)original.getY(), (double)original.getZ() + (double)0.5F);
         originalPositions.remove(uuid);
      } else {
         player.setPositionAndUpdate((double)data.hubLocation.getX() + (double)0.5F, (double)data.hubLocation.getY(), (double)data.hubLocation.getZ() + (double)0.5F);
      }

   }

   public static void returnToHub(EntityPlayerMP player) {
      Rankedarena data = get(player.world);
      originalPositions.remove(player.getUniqueID().toString());
      player.setPositionAndUpdate((double)data.hubLocation.getX() + (double)0.5F, (double)data.hubLocation.getY(), (double)data.hubLocation.getZ() + (double)0.5F);
   }

   public static BlockPos getHubLocation(World world) {
      Rankedarena data = get(world);
      return data.hubLocation;
   }

   public static void setHubLocation(World world, BlockPos pos) {
      Rankedarena data = get(world);
      data.hubLocation = pos;
      data.markDirty();
   }

   public static int addArena(World world, String name, BlockPos p1Spawn, BlockPos p2Spawn) {
      Rankedarena data = get(world);
      int id = data.nextArenaId++;
      BlockPos center = new BlockPos((p1Spawn.getX() + p2Spawn.getX()) / 2, (p1Spawn.getY() + p2Spawn.getY()) / 2, (p1Spawn.getZ() + p2Spawn.getZ()) / 2);
      int minX = Math.min(p1Spawn.getX(), p2Spawn.getX()) - 50;
      int minY = Math.min(p1Spawn.getY(), p2Spawn.getY()) - 20;
      int minZ = Math.min(p1Spawn.getZ(), p2Spawn.getZ()) - 50;
      int maxX = Math.max(p1Spawn.getX(), p2Spawn.getX()) + 50;
      int maxY = Math.max(p1Spawn.getY(), p2Spawn.getY()) + 50;
      int maxZ = Math.max(p1Spawn.getZ(), p2Spawn.getZ()) + 50;
      Arena arena = new Arena(id, name, "Custom arena", "§f", p1Spawn, p2Spawn, center, new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ), false, 0);
      data.arenas.put(id, arena);
      data.markDirty();
      return id;
   }

   public static boolean removeArena(World world, int arenaId) {
      Rankedarena data = get(world);
      if (data.arenas.containsKey(arenaId)) {
         data.arenas.remove(arenaId);
         data.markDirty();
         return true;
      } else {
         return false;
      }
   }

   public static boolean setArenaSpawn1(World world, int arenaId, BlockPos pos) {
      Rankedarena data = get(world);
      Arena arena = (Arena)data.arenas.get(arenaId);
      if (arena == null) {
         return false;
      } else {
         arena.player1Spawn = pos;
         updateArenaCenter(arena);
         data.markDirty();
         return true;
      }
   }

   public static boolean setArenaSpawn2(World world, int arenaId, BlockPos pos) {
      Rankedarena data = get(world);
      Arena arena = (Arena)data.arenas.get(arenaId);
      if (arena == null) {
         return false;
      } else {
         arena.player2Spawn = pos;
         updateArenaCenter(arena);
         data.markDirty();
         return true;
      }
   }

   public static boolean setArenaName(World world, int arenaId, String name) {
      Rankedarena data = get(world);
      Arena arena = (Arena)data.arenas.get(arenaId);
      if (arena == null) {
         return false;
      } else {
         arena.name = name;
         data.markDirty();
         return true;
      }
   }

   public static boolean setArenaEnabled(World world, int arenaId, boolean enabled) {
      Rankedarena data = get(world);
      Arena arena = (Arena)data.arenas.get(arenaId);
      if (arena == null) {
         return false;
      } else {
         arena.enabled = enabled;
         data.markDirty();
         return true;
      }
   }

   public static boolean setArenaRingOut(World world, int arenaId, boolean hasRingOut, int ringOutY) {
      Rankedarena data = get(world);
      Arena arena = (Arena)data.arenas.get(arenaId);
      if (arena == null) {
         return false;
      } else {
         arena.hasRingOut = hasRingOut;
         arena.ringOutY = ringOutY;
         data.markDirty();
         return true;
      }
   }

   private static void updateArenaCenter(Arena arena) {
      arena.centerPoint = new BlockPos((arena.player1Spawn.getX() + arena.player2Spawn.getX()) / 2, (arena.player1Spawn.getY() + arena.player2Spawn.getY()) / 2, (arena.player1Spawn.getZ() + arena.player2Spawn.getZ()) / 2);
   }

   public static void startCountdown(World world, Rankedqueue.Match match) {
      System.out.println("[Ranked] startCountdown called for match " + match.matchId + ", arena " + match.arenaId);
      match.state = "countdown";
      EntityPlayerMP p1 = getPlayer(world, match.player1UUID);
      EntityPlayerMP p2 = getPlayer(world, match.player2UUID);
      System.out.println("[Ranked] Player 1: " + (p1 != null ? p1.getName() : "NULL"));
      System.out.println("[Ranked] Player 2: " + (p2 != null ? p2.getName() : "NULL"));
      Arena arena = getArena(world, match.arenaId);
      if (arena != null) {
         System.out.println("[Ranked] Arena found: " + arena.name);
         System.out.println("[Ranked] P1 spawn: " + arena.player1Spawn);
         System.out.println("[Ranked] P2 spawn: " + arena.player2Spawn);
         if (p1 != null) {
            teleportToArena(p1, match.arenaId, true);
         }

         if (p2 != null) {
            teleportToArena(p2, match.arenaId, false);
         }

         String arenaMsg = "§6Arena: " + arena.color + arena.name;
         sendMessage(p1, arenaMsg);
         sendMessage(p2, arenaMsg);
      } else {
         System.out.println("[Ranked] ERROR: Arena " + match.arenaId + " is null! Cannot start match.");
         Map<Integer, Arena> allArenas = getAllArenas(world);
         System.out.println("[Ranked] Available arenas: " + allArenas.size());

         for(Arena a : allArenas.values()) {
            System.out.println("[Ranked]   - Arena " + a.id + ": " + a.name + " (enabled=" + a.enabled + ")");
         }

      }
   }

   public static void startMatch(Rankedqueue.Match match) {
      match.state = "active";
      match.startTime = System.currentTimeMillis();
   }

   public static void endMatch(World world, int matchId, String winnerUUID, String loserUUID, String reason) {
      Rankedqueue.Match match = Rankedqueue.getMatch(matchId);
      if (match != null) {
         match.state = "finished";
         match.winnerUUID = winnerUUID;
         match.loserUUID = loserUUID;
         EntityPlayerMP winner = getPlayer(world, winnerUUID);
         EntityPlayerMP loser = getPlayer(world, loserUUID);
         Rankeddatastorage storage = Rankeddatastorage.get(world);
         if (storage != null) {
            Rankeddatastorage.PlayerRankedData winnerData = storage.getPlayerData(winnerUUID);
            Rankeddatastorage.PlayerRankedData loserData = storage.getPlayerData(loserUUID);
            String oldWinnerTier = Rankedelocore.getRankTier(winnerData.currentElo).name;
            int winnerK = Rankedelocore.getKFactor(winnerData.gamesPlayed, winnerData.currentElo);
            int loserK = Rankedelocore.getKFactor(loserData.gamesPlayed, loserData.currentElo);
            double winnerExpected = Rankedelocore.calculateExpectedScore(winnerData.currentElo, loserData.currentElo);
            double loserExpected = Rankedelocore.calculateExpectedScore(loserData.currentElo, winnerData.currentElo);
            int winnerChange = Rankedelocore.clampElo(Rankedelocore.calculateNewRating(winnerData.currentElo, winnerK, 1, winnerExpected)) - winnerData.currentElo;
            int loserChange = Rankedelocore.clampElo(Rankedelocore.calculateNewRating(loserData.currentElo, loserK, 0, loserExpected)) - loserData.currentElo;
            double streakMult = Rankedelocore.getStreakMultiplier(winnerData.currentWinStreak + 1);
            winnerChange = (int)Math.round((double)winnerChange * streakMult);
            int winnerNewElo = winnerData.currentElo + winnerChange;
            int loserNewElo = loserData.currentElo + loserChange;
            String newWinnerTier = Rankedelocore.getRankTier(winnerNewElo).name;
            Rankeddatastorage.updatePlayerAfterMatch(world, winnerUUID, true, winnerChange, winnerNewElo);
            Rankeddatastorage.updatePlayerAfterMatch(world, loserUUID, false, loserChange, loserNewElo);
            if (winner != null) {
               sendResultTitle(winner, "§a§lVICTORY", "§a+" + winnerChange + " ELO", 10, 80, 20);
               sendMessage(winner, "§a§lVICTORY!");
               sendMessage(winner, "§7ELO: " + winnerData.currentElo + " §a+" + winnerChange + " §7-> §f" + winnerNewElo);
               Rankeddatastorage.PlayerRankedData updatedWinnerData = storage.getPlayerData(winnerUUID);
               Rankedrewards.processWinReward(winner, updatedWinnerData.currentWinStreak, updatedWinnerData.gamesPlayed, oldWinnerTier, newWinnerTier);
            }

            if (loser != null) {
               sendResultTitle(loser, "§c§lDEFEAT", "§c" + loserChange + " ELO", 10, 80, 20);
               sendMessage(loser, "§c§lDEFEAT");
               sendMessage(loser, "§7ELO: " + loserData.currentElo + " §c" + loserChange + " §7-> §f" + loserNewElo);
               Rankedrewards.processLossReward(loser);
            }
         }

         Rankedqueue.endMatch(matchId, winnerUUID, loserUUID);
         if (winner != null) {
            scheduleDelayedTeleport(winnerUUID, 5000L);
         }

         if (loser != null) {
            luckAddonAddon.PACKET_HANDLER.sendTo(new RankedMatchHudMessage(false), loser);
         }

      }
   }

   public static void endMatchDraw(World world, int matchId, String reason) {
      Rankedqueue.Match match = Rankedqueue.getMatch(matchId);
      if (match != null) {
         match.state = "finished";
         match.winnerUUID = null;
         match.loserUUID = null;
         EntityPlayerMP p1 = getPlayer(world, match.player1UUID);
         EntityPlayerMP p2 = getPlayer(world, match.player2UUID);
         String reasonMessage = "§7" + reason;
         if (p1 != null) {
            sendResultTitle(p1, "§e§lDRAW", "No ELO changes", 10, 80, 20);
            sendMessage(p1, "§e§lDRAW!");
            sendMessage(p1, reasonMessage);
            sendMessage(p1, "§7No ELO changes.");
         }

         if (p2 != null) {
            sendResultTitle(p2, "§e§lDRAW", "No ELO changes", 10, 80, 20);
            sendMessage(p2, "§e§lDRAW!");
            sendMessage(p2, reasonMessage);
            sendMessage(p2, "§7No ELO changes.");
         }

         Rankeddatastorage storage = Rankeddatastorage.get(world);
         if (storage != null) {
            Rankeddatastorage.PlayerRankedData p1Data = storage.getPlayerData(match.player1UUID);
            Rankeddatastorage.PlayerRankedData p2Data = storage.getPlayerData(match.player2UUID);
            ++p1Data.gamesPlayed;
            p1Data.currentWinStreak = 0;
            ++p2Data.gamesPlayed;
            p2Data.currentWinStreak = 0;
            if (!p1Data.isPlaced) {
               ++p1Data.placementGamesCompleted;
            }

            if (!p2Data.isPlaced) {
               ++p2Data.placementGamesCompleted;
            }

            storage.savePlayerData(match.player1UUID, p1Data);
            storage.savePlayerData(match.player2UUID, p2Data);
         }

         Rankedqueue.endMatch(matchId, (String)null, (String)null);
         if (p1 != null) {
            scheduleDelayedTeleport(match.player1UUID, 5000L);
         }

         if (p2 != null) {
            scheduleDelayedTeleport(match.player2UUID, 5000L);
         }

      }
   }

   public static void handleMatchDeath(World world, EntityPlayer deadPlayer) {
      String uuid = deadPlayer.getUniqueID().toString();
      Rankedqueue.Match match = Rankedqueue.getPlayerMatch(uuid);
      if (match != null && match.state.equals("active")) {
         String winnerUUID = match.player1UUID.equals(uuid) ? match.player2UUID : match.player1UUID;
         endMatch(world, match.matchId, winnerUUID, uuid, "death");
      }
   }

   public static void handleMatchDisconnect(World world, String uuid) {
      Rankedqueue.Match match = Rankedqueue.getPlayerMatch(uuid);
      if (match != null) {
         if (match.state.equals("countdown")) {
            Rankedqueue.cancelMatch(match.matchId, "Player disconnected during countdown");
         } else {
            if (match.state.equals("active")) {
               String winnerUUID = match.player1UUID.equals(uuid) ? match.player2UUID : match.player1UUID;
               endMatch(world, match.matchId, winnerUUID, uuid, "disconnect");
            }

         }
      }
   }

   private static EntityPlayerMP getPlayer(World world, String uuid) {
      return world.getMinecraftServer() == null ? null : world.getMinecraftServer().getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
   }

   private static void sendMessage(EntityPlayerMP player, String message) {
      if (player != null) {
         player.sendMessage(new TextComponentString(message));
      }

   }

   public static boolean isPlayerInBounds(EntityPlayer player, int arenaId) {
      Arena arena = getArena(player.world, arenaId);
      return arena == null ? true : arena.isInBounds(player.getPosition());
   }

   public static boolean checkRingOut(EntityPlayer player, int arenaId) {
      Arena arena = getArena(player.world, arenaId);
      return arena == null ? false : arena.isRingOut(player.getPosition());
   }

   public static class Arena {
      public int id;
      public String name;
      public String description;
      public String color;
      public BlockPos player1Spawn;
      public BlockPos player2Spawn;
      public BlockPos centerPoint;
      public BlockPos boundaryMin;
      public BlockPos boundaryMax;
      public boolean hasRingOut;
      public int ringOutY;
      public boolean enabled = true;

      public Arena(int id, String name, String description, String color, BlockPos p1Spawn, BlockPos p2Spawn, BlockPos center, BlockPos boundMin, BlockPos boundMax, boolean hasRingOut, int ringOutY) {
         this.id = id;
         this.name = name;
         this.description = description;
         this.color = color;
         this.player1Spawn = p1Spawn;
         this.player2Spawn = p2Spawn;
         this.centerPoint = center;
         this.boundaryMin = boundMin;
         this.boundaryMax = boundMax;
         this.hasRingOut = hasRingOut;
         this.ringOutY = ringOutY;
      }

      public boolean isInBounds(BlockPos pos) {
         return pos.getX() >= this.boundaryMin.getX() && pos.getX() <= this.boundaryMax.getX() && pos.getY() >= this.boundaryMin.getY() && pos.getY() <= this.boundaryMax.getY() && pos.getZ() >= this.boundaryMin.getZ() && pos.getZ() <= this.boundaryMax.getZ();
      }

      public boolean isRingOut(BlockPos pos) {
         return this.hasRingOut && pos.getY() < this.ringOutY;
      }

      public NBTTagCompound writeToNBT() {
         NBTTagCompound nbt = new NBTTagCompound();
         nbt.setInteger("id", this.id);
         nbt.setString("name", this.name);
         nbt.setString("description", this.description != null ? this.description : "");
         nbt.setString("color", this.color != null ? this.color : "§f");
         nbt.setInteger("p1X", this.player1Spawn.getX());
         nbt.setInteger("p1Y", this.player1Spawn.getY());
         nbt.setInteger("p1Z", this.player1Spawn.getZ());
         nbt.setInteger("p2X", this.player2Spawn.getX());
         nbt.setInteger("p2Y", this.player2Spawn.getY());
         nbt.setInteger("p2Z", this.player2Spawn.getZ());
         nbt.setInteger("cX", this.centerPoint.getX());
         nbt.setInteger("cY", this.centerPoint.getY());
         nbt.setInteger("cZ", this.centerPoint.getZ());
         nbt.setInteger("bMinX", this.boundaryMin.getX());
         nbt.setInteger("bMinY", this.boundaryMin.getY());
         nbt.setInteger("bMinZ", this.boundaryMin.getZ());
         nbt.setInteger("bMaxX", this.boundaryMax.getX());
         nbt.setInteger("bMaxY", this.boundaryMax.getY());
         nbt.setInteger("bMaxZ", this.boundaryMax.getZ());
         nbt.setBoolean("hasRingOut", this.hasRingOut);
         nbt.setInteger("ringOutY", this.ringOutY);
         nbt.setBoolean("enabled", this.enabled);
         return nbt;
      }

      public static Arena readFromNBT(NBTTagCompound nbt) {
         Arena arena = new Arena(nbt.getInteger("id"), nbt.getString("name"), nbt.getString("description"), nbt.getString("color"), new BlockPos(nbt.getInteger("p1X"), nbt.getInteger("p1Y"), nbt.getInteger("p1Z")), new BlockPos(nbt.getInteger("p2X"), nbt.getInteger("p2Y"), nbt.getInteger("p2Z")), new BlockPos(nbt.getInteger("cX"), nbt.getInteger("cY"), nbt.getInteger("cZ")), new BlockPos(nbt.getInteger("bMinX"), nbt.getInteger("bMinY"), nbt.getInteger("bMinZ")), new BlockPos(nbt.getInteger("bMaxX"), nbt.getInteger("bMaxY"), nbt.getInteger("bMaxZ")), nbt.getBoolean("hasRingOut"), nbt.getInteger("ringOutY"));
         arena.enabled = !nbt.hasKey("enabled") || nbt.getBoolean("enabled");
         return arena;
      }
   }
}
