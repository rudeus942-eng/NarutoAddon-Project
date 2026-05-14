
package net.luck.narutoaddon.OtherCode.raid.command;

import net.luck.narutoaddon.OtherCode.raid.arena.RaidArena;
import net.luck.narutoaddon.OtherCode.raid.arena.RaidArenaStorage;
import net.luck.narutoaddon.OtherCode.raid.boss.BossRegistry;
import net.luck.narutoaddon.OtherCode.raid.boss.IRaidBoss;
import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.core.RaidManager;
import net.luck.narutoaddon.OtherCode.raid.core.RaidQueueManager;
import net.luck.narutoaddon.OtherCode.raid.rewards.RaidRewardDistributor;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandRaidAdmin extends CommandBase {
   public String getName() {
      return "raidadmin";
   }

   public String getUsage(ICommandSender sender) {
      return "/raidadmin <arena|boss|debug>";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length == 0) {
         this.sendHelp(sender);
      } else {
         switch (args[0].toLowerCase()) {
            case "arena":
               this.handleArenaCommand(server, sender, args);
               break;
            case "boss":
               this.handleBossCommand(server, sender, args);
               break;
            case "debug":
               this.handleDebugCommand(sender);
               break;
            case "sethub":
               this.handleSetHubCommand(sender, args);
               break;
            case "test":
               this.handleTestCommand(sender, args);
               break;
            case "window":
               this.handleWindowCommand(sender, args);
               break;
            default:
               this.sendHelp(sender);
         }

      }
   }

   private void handleWindowCommand(ICommandSender sender, String[] args) {
      RaidQueueManager mgr = RaidQueueManager.getInstance();
      if (args.length < 2) {
         sender.sendMessage(new TextComponentString("§cUsage: /raidadmin window <open|close|status|reset> [durationMinutes]"));
      } else {
         String action = args[1].toLowerCase();
         long durationMs = 0L;
         if (args.length >= 3) {
            try {
               durationMs = Long.parseLong(args[2]) * 60000L;
            } catch (NumberFormatException var12) {
               sender.sendMessage(new TextComponentString("§cInvalid duration (minutes): " + args[2]));
               return;
            }
         }

         switch (action) {
            case "open":
               mgr.forceWindowOpen(durationMs);
               sender.sendMessage(new TextComponentString("§aRaid queue window FORCED OPEN" + (durationMs > 0L ? " for " + durationMs / 60000L + " minute(s)." : " until next natural transition.")));
               break;
            case "close":
               mgr.forceWindowClose(durationMs);
               sender.sendMessage(new TextComponentString("§cRaid queue window FORCED CLOSED" + (durationMs > 0L ? " for " + durationMs / 60000L + " minute(s)." : " until next natural transition.")));
               break;
            case "reset":
               mgr.clearWindowOverride();
               sender.sendMessage(new TextComponentString("§aRaid queue window override cleared — back to natural cycle."));
               break;
            case "status":
            default:
               boolean open = mgr.isQueueWindowOpen();
               long remaining = Math.max(0L, mgr.getNextWindowEventTimeMs() - System.currentTimeMillis());
               sender.sendMessage(new TextComponentString("§eRaid queue window: " + (open ? "§aOPEN" : "§cCLOSED") + " §e| next change in §f" + RaidQueueManager.formatHM(remaining) + " §e| override: §f" + mgr.getWindowOverride()));
         }

      }
   }

   private void handleArenaCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         this.sendArenaHelp(sender);
      } else {
         switch (args[1].toLowerCase()) {
            case "add":
               this.handleArenaAdd(sender, args);
               break;
            case "remove":
               this.handleArenaRemove(sender, args);
               break;
            case "setspawn":
               this.handleArenaSetSpawn(sender, args);
               break;
            case "setboss":
               this.handleArenaSetBoss(sender, args);
               break;
            case "enable":
            case "disable":
               this.handleArenaToggle(sender, args, action.equals("enable"));
               break;
            case "enableall":
            case "disableall":
               this.handleArenaToggleAll(sender, action.equals("enableall"));
               break;
            case "list":
               this.handleArenaList(sender);
               break;
            case "info":
               this.handleArenaInfo(sender, args);
               break;
            default:
               this.sendArenaHelp(sender);
         }

      }
   }

   private void handleArenaAdd(ICommandSender sender, String[] args) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         sender.sendMessage(new TextComponentString("§cThis command must be run by a player."));
      } else if (args.length < 4) {
         sender.sendMessage(new TextComponentString("§cUsage: /raidadmin arena add <bossId> <difficulty>"));
         sender.sendMessage(new TextComponentString("§7Difficulties: genin, chunin, jonin, anbu"));
      } else {
         String bossId = args[2].toLowerCase();
         String diffName = args[3].toLowerCase();
         if (!BossRegistry.bossExists(bossId)) {
            sender.sendMessage(new TextComponentString("§cUnknown boss: " + bossId));
         } else {
            RaidDifficulty difficulty = RaidDifficulty.fromString(diffName);
            if (difficulty == null) {
               sender.sendMessage(new TextComponentString("§cInvalid difficulty. Use: genin, chunin, jonin, anbu"));
            } else {
               EntityPlayerMP player = (EntityPlayerMP)sender;
               BlockPos playerPos = player.getPosition();
               int halfX = 35;
               int halfZ = 36;
               int heightBelow = 5;
               int heightAbove = 37;
               BlockPos corner1 = new BlockPos(playerPos.getX() - halfX, playerPos.getY() - heightBelow, playerPos.getZ() - halfZ);
               BlockPos corner2 = new BlockPos(playerPos.getX() + halfX, playerPos.getY() + heightAbove, playerPos.getZ() + halfZ);
               RaidArenaStorage storage = RaidArenaStorage.get(sender.getEntityWorld());
               if (storage == null) {
                  sender.sendMessage(new TextComponentString("§cError: Could not access arena storage."));
               } else {
                  if (storage.doesOverlap(corner1, corner2)) {
                     sender.sendMessage(new TextComponentString("§cWarning: This arena overlaps with an existing arena."));
                  }

                  String arenaName = bossId + "_" + difficulty.name().toLowerCase();
                  RaidArena arena = storage.createArena(arenaName, bossId, corner1, corner2);
                  arena.setDifficulty(difficulty.name().toLowerCase());
                  storage.markDirty();
                  storage.setBossSpawn(arena.getArenaId(), playerPos);
                  int spawnCount = 6;
                  int spawnRadius = 15;

                  for(int i = 0; i < spawnCount; ++i) {
                     double angle = (Math.PI * 2D) * (double)i / (double)spawnCount;
                     int spawnX = playerPos.getX() + (int)Math.round((double)spawnRadius * Math.cos(angle));
                     int spawnZ = playerPos.getZ() + (int)Math.round((double)spawnRadius * Math.sin(angle));
                     storage.setPlayerSpawn(arena.getArenaId(), i, new BlockPos(spawnX, playerPos.getY(), spawnZ));
                  }

                  sender.sendMessage(new TextComponentString("§aCreated " + difficulty.getColoredName() + "§a arena (ID: " + arena.getArenaId() + ") for " + bossId));
                  sender.sendMessage(new TextComponentString("§7Arena: 70x42x73 centered at your position"));
                  sender.sendMessage(new TextComponentString("§7Boss spawn: your position"));
                  sender.sendMessage(new TextComponentString("§7Player spawns: " + spawnCount + " positions in circle (radius " + spawnRadius + ") at Y=" + playerPos.getY()));
               }
            }
         }
      }
   }

   private void handleArenaRemove(ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /raidadmin arena remove <arenaId>"));
      } else {
         int arenaId = parseInt(args[2]);
         RaidArenaStorage storage = RaidArenaStorage.get(sender.getEntityWorld());
         if (storage == null) {
            sender.sendMessage(new TextComponentString("§cError: Could not access arena storage."));
         } else {
            if (storage.removeArena(arenaId)) {
               sender.sendMessage(new TextComponentString("§aRemoved arena " + arenaId));
            } else {
               sender.sendMessage(new TextComponentString("§cArena not found: " + arenaId));
            }

         }
      }
   }

   private void handleArenaSetSpawn(ICommandSender sender, String[] args) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         sender.sendMessage(new TextComponentString("§cThis command must be run by a player."));
      } else if (args.length < 4) {
         sender.sendMessage(new TextComponentString("§cUsage: /raidadmin arena setspawn <arenaId> <1-6>"));
      } else {
         int arenaId = parseInt(args[2]);
         int spawnIndex = parseInt(args[3]) - 1;
         if (spawnIndex >= 0 && spawnIndex <= 5) {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            BlockPos pos = player.getPosition();
            RaidArenaStorage storage = RaidArenaStorage.get(player.world);
            if (storage == null) {
               sender.sendMessage(new TextComponentString("§cError: Could not access arena storage."));
            } else {
               if (storage.setPlayerSpawn(arenaId, spawnIndex, pos)) {
                  sender.sendMessage(new TextComponentString("§aSet player spawn " + (spawnIndex + 1) + " at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
               } else {
                  sender.sendMessage(new TextComponentString("§cArena not found: " + arenaId));
               }

            }
         } else {
            sender.sendMessage(new TextComponentString("§cSpawn index must be 1-6"));
         }
      }
   }

   private void handleArenaSetBoss(ICommandSender sender, String[] args) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         sender.sendMessage(new TextComponentString("§cThis command must be run by a player."));
      } else if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /raidadmin arena setboss <arenaId>"));
      } else {
         int arenaId = parseInt(args[2]);
         EntityPlayerMP player = (EntityPlayerMP)sender;
         BlockPos pos = player.getPosition();
         RaidArenaStorage storage = RaidArenaStorage.get(player.world);
         if (storage == null) {
            sender.sendMessage(new TextComponentString("§cError: Could not access arena storage."));
         } else {
            if (storage.setBossSpawn(arenaId, pos)) {
               sender.sendMessage(new TextComponentString("§aSet boss spawn at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
            } else {
               sender.sendMessage(new TextComponentString("§cArena not found: " + arenaId));
            }

         }
      }
   }

   private void handleArenaToggle(ICommandSender sender, String[] args, boolean enable) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /raidadmin arena " + (enable ? "enable" : "disable") + " <arenaId>"));
      } else {
         int arenaId = parseInt(args[2]);
         RaidArenaStorage storage = RaidArenaStorage.get(sender.getEntityWorld());
         if (storage == null) {
            sender.sendMessage(new TextComponentString("§cError: Could not access arena storage."));
         } else {
            if (storage.setArenaEnabled(arenaId, enable)) {
               sender.sendMessage(new TextComponentString("§aArena " + arenaId + " " + (enable ? "enabled" : "disabled")));
            } else {
               sender.sendMessage(new TextComponentString("§cArena not found: " + arenaId));
            }

         }
      }
   }

   private void handleArenaToggleAll(ICommandSender sender, boolean enable) {
      RaidArenaStorage storage = RaidArenaStorage.get(sender.getEntityWorld());
      if (storage == null) {
         sender.sendMessage(new TextComponentString("§cError: Could not access arena storage."));
      } else {
         int count = 0;

         for(RaidArena arena : storage.getAllArenas()) {
            arena.setEnabled(enable);
            ++count;
         }

         storage.markDirty();
         sender.sendMessage(new TextComponentString("§a" + (enable ? "Enabled" : "Disabled") + " §e" + count + "§a arenas."));
      }
   }

   private void handleArenaList(ICommandSender sender) {
      RaidArenaStorage storage = RaidArenaStorage.get(sender.getEntityWorld());
      if (storage == null) {
         sender.sendMessage(new TextComponentString("§cError: Could not access arena storage."));
      } else {
         sender.sendMessage(new TextComponentString("§e§l=== RAID ARENAS (" + storage.getArenaCount() + ") ==="));

         for(RaidArena arena : storage.getAllArenas()) {
            String status = arena.isEnabled() ? (arena.isInUse() ? "§c[IN USE]" : "§a[AVAILABLE]") : "§7[DISABLED]";
            String diffLabel = arena.getDifficulty() != null ? " §e[" + arena.getDifficulty().toUpperCase() + "]" : " §8[legacy]";
            BlockPos center = arena.getCenter();
            String coords = "§7@ §f" + center.getX() + ", " + center.getY() + ", " + center.getZ();
            sender.sendMessage(new TextComponentString("§6#" + arena.getArenaId() + " §f" + arena.getName() + " §7(" + arena.getBossId() + ")" + diffLabel + " " + status));
            sender.sendMessage(new TextComponentString("  " + coords));
         }

      }
   }

   private void handleArenaInfo(ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /raidadmin arena info <arenaId>"));
      } else {
         int arenaId = parseInt(args[2]);
         RaidArenaStorage storage = RaidArenaStorage.get(sender.getEntityWorld());
         if (storage == null) {
            sender.sendMessage(new TextComponentString("§cError: Could not access arena storage."));
         } else {
            RaidArena arena = storage.getArena(arenaId);
            if (arena == null) {
               sender.sendMessage(new TextComponentString("§cArena not found: " + arenaId));
            } else {
               sender.sendMessage(new TextComponentString("§e§l=== ARENA " + arena.getArenaId() + " ==="));
               sender.sendMessage(new TextComponentString("§7Name: §f" + arena.getName()));
               sender.sendMessage(new TextComponentString("§7Boss: §f" + arena.getBossId()));
               String diffDisplay = arena.getDifficulty() != null ? arena.getDifficulty().toUpperCase() : "Not set (legacy)";
               sender.sendMessage(new TextComponentString("§7Difficulty: §e" + diffDisplay));
               sender.sendMessage(new TextComponentString("§7Bounds: §f" + arena.getCorner1() + " to " + arena.getCorner2()));
               sender.sendMessage(new TextComponentString("§7Boss Spawn: §f" + arena.getBossSpawn()));
               sender.sendMessage(new TextComponentString("§7Player Spawns: §f" + arena.getConfiguredSpawnCount() + "/6 configured"));
               sender.sendMessage(new TextComponentString("§7Enabled: §f" + arena.isEnabled()));
               sender.sendMessage(new TextComponentString("§7In Use: §f" + arena.isInUse()));
            }
         }
      }
   }

   private void handleBossCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         this.sendBossHelp(sender);
      } else {
         switch (args[1].toLowerCase()) {
            case "spawn":
               this.handleBossSpawn(sender, args);
               break;
            case "kill":
               this.handleBossKill(sender);
               break;
            case "phase":
               this.handleBossPhase(sender, args);
               break;
            case "list":
               this.handleBossList(sender);
               break;
            default:
               this.sendBossHelp(sender);
         }

      }
   }

   private void handleBossSpawn(ICommandSender sender, String[] args) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         sender.sendMessage(new TextComponentString("§cThis command must be run by a player."));
      } else if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /raidadmin boss spawn <bossId> [difficulty]"));
      } else {
         String bossId = args[2].toLowerCase();
         RaidDifficulty difficulty = RaidDifficulty.GENIN;
         if (args.length >= 4) {
            difficulty = RaidDifficulty.fromString(args[3]);
            if (difficulty == null) {
               sender.sendMessage(new TextComponentString("§cInvalid difficulty"));
               return;
            }
         }

         EntityPlayerMP player = (EntityPlayerMP)sender;
         EntityLivingBase bossEntity = BossRegistry.createBoss(bossId, player.world);
         if (bossEntity != null && bossEntity instanceof IRaidBoss) {
            IRaidBoss boss = (IRaidBoss)bossEntity;
            bossEntity.setPosition(player.posX, player.posY, player.posZ);
            boss.setDifficulty(difficulty);
            player.world.spawnEntity(bossEntity);
            sender.sendMessage(new TextComponentString("§aSpawned " + boss.getBossDisplayName() + " (" + difficulty.getColoredName() + "§a) for testing"));
         } else {
            sender.sendMessage(new TextComponentString("§cFailed to create boss: " + bossId));
         }
      }
   }

   private void handleBossKill(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§aBoss kill command - Not yet implemented"));
   }

   private void handleBossPhase(ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /raidadmin boss phase <phaseNumber>"));
      } else {
         int phase = parseInt(args[2]);
         sender.sendMessage(new TextComponentString("§aForce phase command - Not yet implemented"));
      }
   }

   private void handleBossList(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§e§l=== REGISTERED BOSSES ==="));

      for(BossRegistry.BossEntry entry : BossRegistry.getAllEntries()) {
         sender.sendMessage(new TextComponentString("§6" + entry.bossId + ": §f" + entry.displayName));
      }

   }

   private void handleDebugCommand(ICommandSender sender) {
      RaidManager manager = RaidManager.getInstance();
      sender.sendMessage(new TextComponentString("§e§l=== RAID DEBUG ==="));
      sender.sendMessage(new TextComponentString("§7Active Raids: §f" + manager.getActiveRaidCount()));
      sender.sendMessage(new TextComponentString("§7Queue Size: §f" + manager.getQueueSize()));
      RaidArenaStorage arenaStorage = RaidArenaStorage.get(sender.getEntityWorld());
      if (arenaStorage != null) {
         sender.sendMessage(new TextComponentString("§7Total Arenas: §f" + arenaStorage.getArenaCount()));
      }

      sender.sendMessage(new TextComponentString("§7Registered Bosses: §f" + BossRegistry.getBossCount()));
   }

   private void handleSetHubCommand(ICommandSender sender, String[] args) throws CommandException {
      if (!(sender instanceof EntityPlayerMP)) {
         sender.sendMessage(new TextComponentString("§cThis command must be run by a player."));
      } else {
         EntityPlayerMP player = (EntityPlayerMP)sender;
         BlockPos pos = player.getPosition();
         RaidArenaStorage storage = RaidArenaStorage.get(player.world);
         if (storage != null) {
            storage.setHubLocation(pos);
            sender.sendMessage(new TextComponentString("§aSet raid hub location to " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
         }

      }
   }

   private void handleTestCommand(ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         this.sendTestHelp(sender);
      } else {
         switch (args[1].toLowerCase()) {
            case "minplayers":
               this.handleTestMinPlayers(sender, args);
               break;
            case "status":
               this.handleTestStatus(sender);
               break;
            case "reset":
               this.handleTestReset(sender);
               break;
            case "mokutonchance":
               this.handleMokutonChance(sender, args);
               break;
            default:
               this.sendTestHelp(sender);
         }

      }
   }

   private void handleTestMinPlayers(ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         sender.sendMessage(new TextComponentString("§cUsage: /raidadmin test minplayers <1-6>"));
      } else {
         int minPlayers = parseInt(args[2]);
         if (minPlayers >= 1 && minPlayers <= 6) {
            RaidDifficulty.setTestingMinPartySize(minPlayers);
            sender.sendMessage(new TextComponentString("§a[TEST MODE] Minimum party size set to §f" + minPlayers));
            sender.sendMessage(new TextComponentString("§eRaids will now start with only " + minPlayers + " player(s)"));
            sender.sendMessage(new TextComponentString("§7Use §f/raidadmin test reset §7to restore default (6 players)"));
         } else {
            sender.sendMessage(new TextComponentString("§cMinimum players must be between 1 and 6"));
         }
      }
   }

   private void handleTestStatus(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§e§l=== RAID TEST STATUS ==="));
      if (RaidDifficulty.isTestingModeActive()) {
         sender.sendMessage(new TextComponentString("§aTest Mode: §fACTIVE"));
         sender.sendMessage(new TextComponentString("§7Min Party Size: §f" + RaidDifficulty.getTestingMinPartySize()));
      } else {
         sender.sendMessage(new TextComponentString("§7Test Mode: §fINACTIVE"));
         sender.sendMessage(new TextComponentString("§7Min Party Size: §f6 (default)"));
      }

      RaidManager manager = RaidManager.getInstance();
      sender.sendMessage(new TextComponentString("§7Active Raids: §f" + manager.getActiveRaidCount()));
      sender.sendMessage(new TextComponentString("§7Queue Size: §f" + manager.getQueueSize()));
   }

   private void handleTestReset(ICommandSender sender) {
      RaidDifficulty.setTestingMinPartySize(-1);
      sender.sendMessage(new TextComponentString("§a[TEST MODE] Disabled - minimum party size restored to §f6"));
   }

   private void handleMokutonChance(ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 3) {
         double current = RaidRewardDistributor.getAnbuMokutonDropChance();
         sender.sendMessage(new TextComponentString("§eCurrent ANBU Mokuton drop chance: §f" + String.format("%.1f%%", current * (double)100.0F)));
         sender.sendMessage(new TextComponentString("§7Usage: /raidadmin test mokutonchance <percent>"));
      } else {
         double percent = parseDouble(args[2], (double)0.0F, (double)100.0F);
         RaidRewardDistributor.setAnbuMokutonDropChance(percent / (double)100.0F);
         sender.sendMessage(new TextComponentString("§aANBU Hashirama Mokuton drop chance set to §f" + String.format("%.1f%%", percent)));
      }
   }

   private void sendHelp(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§e§l=== RAID ADMIN COMMANDS ==="));
      sender.sendMessage(new TextComponentString("§6/raidadmin arena §7- Arena management"));
      sender.sendMessage(new TextComponentString("§6/raidadmin boss §7- Boss management"));
      sender.sendMessage(new TextComponentString("§6/raidadmin sethub §7- Set hub location"));
      sender.sendMessage(new TextComponentString("§6/raidadmin test §7- Testing options"));
      sender.sendMessage(new TextComponentString("§6/raidadmin debug §7- Debug information"));
   }

   private void sendTestHelp(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§e§l=== TEST COMMANDS ==="));
      sender.sendMessage(new TextComponentString("§6/raidadmin test minplayers <1-6> §7- Set minimum party size"));
      sender.sendMessage(new TextComponentString("§6/raidadmin test status §7- Show current test settings"));
      sender.sendMessage(new TextComponentString("§6/raidadmin test reset §7- Reset to default settings"));
   }

   private void sendArenaHelp(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§e§l=== ARENA COMMANDS ==="));
      sender.sendMessage(new TextComponentString("§6/raidadmin arena add <name> <boss> §7- Create arena at your position"));
      sender.sendMessage(new TextComponentString("§6/raidadmin arena remove <id>"));
      sender.sendMessage(new TextComponentString("§6/raidadmin arena setspawn <id> <1-6> §7- Set player spawn at your position"));
      sender.sendMessage(new TextComponentString("§6/raidadmin arena setboss <id> §7- Set boss spawn at your position"));
      sender.sendMessage(new TextComponentString("§6/raidadmin arena enable/disable <id>"));
      sender.sendMessage(new TextComponentString("§6/raidadmin arena enableall/disableall"));
      sender.sendMessage(new TextComponentString("§6/raidadmin arena list"));
      sender.sendMessage(new TextComponentString("§6/raidadmin arena info <id>"));
   }

   private void sendBossHelp(ICommandSender sender) {
      sender.sendMessage(new TextComponentString("§e§l=== BOSS COMMANDS ==="));
      sender.sendMessage(new TextComponentString("§6/raidadmin boss spawn <id> [difficulty] §7- Spawn boss for testing"));
      sender.sendMessage(new TextComponentString("§6/raidadmin boss kill §7- Kill all raid bosses"));
      sender.sendMessage(new TextComponentString("§6/raidadmin boss phase <number> §7- Force phase change"));
      sender.sendMessage(new TextComponentString("§6/raidadmin boss list §7- List registered bosses"));
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
      if (args.length == 1) {
         return getListOfStringsMatchingLastWord(args, new String[]{"arena", "boss", "sethub", "test", "debug", "window"});
      } else {
         if (args.length == 2) {
            if (args[0].equalsIgnoreCase("arena")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"add", "remove", "setspawn", "setboss", "enable", "disable", "enableall", "disableall", "list", "info"});
            }

            if (args[0].equalsIgnoreCase("boss")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"spawn", "kill", "phase", "list"});
            }

            if (args[0].equalsIgnoreCase("test")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"minplayers", "status", "reset", "mokutonchance"});
            }

            if (args[0].equalsIgnoreCase("window")) {
               return getListOfStringsMatchingLastWord(args, new String[]{"open", "close", "status", "reset"});
            }
         }

         if (args.length == 3 && args[0].equalsIgnoreCase("test") && args[1].equalsIgnoreCase("minplayers")) {
            return getListOfStringsMatchingLastWord(args, new String[]{"1", "2", "3", "4", "5", "6"});
         } else if (args.length == 3 && args[0].equalsIgnoreCase("boss") && args[1].equalsIgnoreCase("spawn")) {
            return getListOfStringsMatchingLastWord(args, (String[])BossRegistry.getAllBossIds().toArray(new String[0]));
         } else if (args.length == 4 && args[0].equalsIgnoreCase("boss") && args[1].equalsIgnoreCase("spawn")) {
            return getListOfStringsMatchingLastWord(args, new String[]{"genin", "chunin", "jonin", "anbu"});
         } else if (args.length == 3 && args[0].equalsIgnoreCase("arena") && args[1].equalsIgnoreCase("add")) {
            return getListOfStringsMatchingLastWord(args, (String[])BossRegistry.getAllBossIds().toArray(new String[0]));
         } else {
            return args.length == 4 && args[0].equalsIgnoreCase("arena") && args[1].equalsIgnoreCase("add") ? getListOfStringsMatchingLastWord(args, new String[]{"genin", "chunin", "jonin", "anbu"}) : Collections.emptyList();
         }
      }
   }
}
