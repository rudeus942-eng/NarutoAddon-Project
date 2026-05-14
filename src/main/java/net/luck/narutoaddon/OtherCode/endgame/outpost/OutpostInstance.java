
package net.luck.narutoaddon.OtherCode.endgame.outpost;

import net.luck.narutoaddon.OtherCode.endgame.EndgameModInit;
import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.endgame.PveRank;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameCombatMessage;
import net.luck.narutoaddon.OtherCode.endgame.network.EndgameNetworkHelper;
import net.luck.narutoaddon.OtherCode.quest.core.QuestManager;
import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointSpawnLogic;
import net.luck.narutoaddon.OtherCode.stat.core.StatManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class OutpostInstance {
   private static final double CONTRIBUTION_THRESHOLD = 0.05;
   private static final int COUNTDOWN_TICKS = 100;
   private static final int VICTORY_DISPLAY_TICKS = 60;
   private static final int FAILED_DISPLAY_TICKS = 40;
   private static final int SP_REWARD = 2;
   private final OutpostDefinition definition;
   private final OutpostDifficultyTier tier;
   private final OutpostRegistry.EncounterGroup encounterGroup;
   private final String instanceKey;
   private OutpostState state;
   private final Set<UUID> participants;
   private final Map<UUID, Double> playerDamage;
   private final Set<UUID> spawnedEntityUUIDs;
   private final List<UUID> bossEntityUUIDs;
   private double totalBossHpPool;
   private long stateStartTick;
   private long fightStartTime;
   private World world;
   private int countdownMessagesSent;
   private int allDeadGraceTicks;
   private static final int DEATH_GRACE_TICKS = 600;
   private static final double ABANDON_RADIUS_SQ = (double)14400.0F;

   public OutpostInstance(OutpostDefinition definition, OutpostDifficultyTier tier, OutpostRegistry.EncounterGroup encounterGroup, World world, UUID ownerUUID) {
      this.state = OutpostState.TRAVELING;
      this.participants = new HashSet();
      this.playerDamage = new HashMap();
      this.spawnedEntityUUIDs = new HashSet();
      this.bossEntityUUIDs = new ArrayList();
      this.totalBossHpPool = (double)0.0F;
      this.stateStartTick = 0L;
      this.fightStartTime = 0L;
      this.countdownMessagesSent = 0;
      this.allDeadGraceTicks = 0;
      this.definition = definition;
      this.tier = tier;
      this.encounterGroup = encounterGroup;
      this.world = world;
      this.instanceKey = definition.getOutpostId() + "_" + tier.ordinal() + "_" + ownerUUID.toString().substring(0, 8);
      this.participants.add(ownerUUID);
      this.playerDamage.put(ownerUUID, (double)0.0F);
   }

   private OutpostInstance(OutpostDefinition definition, OutpostDifficultyTier tier, OutpostRegistry.EncounterGroup encounterGroup, World world, String instanceKey) {
      this.state = OutpostState.TRAVELING;
      this.participants = new HashSet();
      this.playerDamage = new HashMap();
      this.spawnedEntityUUIDs = new HashSet();
      this.bossEntityUUIDs = new ArrayList();
      this.totalBossHpPool = (double)0.0F;
      this.stateStartTick = 0L;
      this.fightStartTime = 0L;
      this.countdownMessagesSent = 0;
      this.allDeadGraceTicks = 0;
      this.definition = definition;
      this.tier = tier;
      this.encounterGroup = encounterGroup;
      this.world = world;
      this.instanceKey = instanceKey;
   }

   public void addParticipant(EntityPlayerMP player) {
      UUID uuid = player.getUniqueID();
      this.participants.add(uuid);
      this.playerDamage.putIfAbsent(uuid, (double)0.0F);
   }

   public void removeParticipant(UUID playerUUID) {
      this.participants.remove(playerUUID);
      if (this.participants.isEmpty()) {
         if (this.state != OutpostState.ACTIVE && this.state != OutpostState.COUNTDOWN) {
            if (this.state == OutpostState.TRAVELING) {
               this.transitionTo(OutpostState.CLEANUP);
            }
         } else {
            this.transitionTo(OutpostState.FAILED);
         }
      }

   }

   public int getParticipantCount() {
      return this.participants.size();
   }

   public Set<UUID> getAllParticipantUUIDs() {
      return new HashSet(this.participants);
   }

   public OutpostState getState() {
      return this.state;
   }

   private void transitionTo(OutpostState newState) {
      this.state = newState;
      this.stateStartTick = this.world.getTotalWorldTime();
      if (newState == OutpostState.COUNTDOWN) {
         this.countdownMessagesSent = 0;
      }

   }

   public void onPlayerArrived() {
      if (this.state == OutpostState.TRAVELING) {
         this.transitionTo(OutpostState.COUNTDOWN);
      }

   }

   public void tick(World world) {
      this.world = world;
      long elapsed = world.getTotalWorldTime() - this.stateStartTick;
      switch (this.state) {
         case TRAVELING:
         case CLEANUP:
         default:
            break;
         case COUNTDOWN:
            this.tickCountdown(elapsed);
            break;
         case ACTIVE:
            this.tickActive(world);
            break;
         case VICTORY:
            if (elapsed >= 60L) {
               this.transitionTo(OutpostState.CLEANUP);
               this.cleanup();
            }
            break;
         case FAILED:
            if (elapsed >= 40L) {
               this.transitionTo(OutpostState.CLEANUP);
               this.cleanup();
            }
      }

   }

   private void tickCountdown(long elapsed) {
      int secondsElapsed = (int)(elapsed / 20L);
      if (secondsElapsed >= 0 && this.countdownMessagesSent == 0) {
         this.sendTitleToAll(TextFormatting.YELLOW + "5", this.encounterGroup.getDisplayName());
         this.countdownMessagesSent = 1;
      }

      if (secondsElapsed >= 1 && this.countdownMessagesSent == 1) {
         this.sendTitleToAll(TextFormatting.YELLOW + "4", "");
         this.countdownMessagesSent = 2;
      }

      if (secondsElapsed >= 2 && this.countdownMessagesSent == 2) {
         this.sendTitleToAll(TextFormatting.GOLD + "3", "");
         this.countdownMessagesSent = 3;
      }

      if (secondsElapsed >= 3 && this.countdownMessagesSent == 3) {
         this.sendTitleToAll(TextFormatting.RED + "2", "");
         this.countdownMessagesSent = 4;
      }

      if (secondsElapsed >= 4 && this.countdownMessagesSent == 4) {
         this.sendTitleToAll(TextFormatting.DARK_RED + "1", "");
         this.countdownMessagesSent = 5;
      }

      if (elapsed >= 100L) {
         this.sendTitleToAll(TextFormatting.RED + "" + TextFormatting.BOLD + "FIGHT!", "");
         this.spawnBosses();
         this.fightStartTime = System.currentTimeMillis();
         this.transitionTo(OutpostState.ACTIVE);
         this.syncOutpostTrackerToAll();
      }

   }

   private void tickActive(World world) {
      if (this.areAllParticipantsTooFar(world)) {
         this.transitionTo(OutpostState.FAILED);
         this.sendClearCombatBars();
         this.sendTitleToAll(TextFormatting.RED + "Mission Abandoned", TextFormatting.GRAY + "You left the outpost area.");
         this.clearOutpostTrackerForAll();
         this.clearOutpostWaypointsForAll();
         this.cleanupBosses();
      } else if (this.areBossesAllDead(world)) {
         this.transitionTo(OutpostState.VICTORY);
         this.grantRewards();
         this.sendClearCombatBars();
         this.sendTitleToAll(TextFormatting.GREEN + "" + TextFormatting.BOLD + "VICTORY!", TextFormatting.GOLD + this.encounterGroup.getDisplayName() + " defeated!");
         this.syncOutpostTrackerToAll();
         this.clearOutpostWaypointsForAll();
      } else {
         if (this.areAllParticipantsGone(world)) {
            ++this.allDeadGraceTicks;
            if (this.allDeadGraceTicks == 100) {
               this.sendTitleToAll("", TextFormatting.YELLOW + "Return within 25s or the outpost mission fails!");
            }

            if (this.allDeadGraceTicks >= 600) {
               this.transitionTo(OutpostState.FAILED);
               this.sendClearCombatBars();
               this.clearOutpostTrackerForAll();
               this.clearOutpostWaypointsForAll();
               this.cleanupBosses();
               return;
            }
         } else {
            this.allDeadGraceTicks = 0;
         }

         this.syncBossHealthBars(world);
      }
   }

   private void spawnBosses() {
      String[] configIds = this.encounterGroup.getBossConfigIds(this.tier);
      BlockPos basePos = this.definition.getLocation();

      for(int i = 0; i < configIds.length; ++i) {
         String configId = configIds[i];
         NpcConfig config = NpcConfigRegistry.get(configId);
         if (config == null) {
            System.out.println("[OutpostInstance] Unknown NPC config: " + configId);
         } else {
            Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(config.getEntityRegistryId()), this.world);
            if (entity == null) {
               System.out.println("[OutpostInstance] Failed to create entity: " + config.getEntityRegistryId());
            } else {
               double angle = (Math.PI * 2D) * (double)i / (double)configIds.length;
               double radius = configIds.length > 1 ? (double)4.0F : (double)0.0F;
               int spawnX = basePos.getX() + (int)(Math.cos(angle) * radius);
               int spawnZ = basePos.getZ() + (int)(Math.sin(angle) * radius);
               int spawnY = WaypointSpawnLogic.findGroundY(this.world, spawnX, spawnZ);
               entity.setLocationAndAngles((double)spawnX + (double)0.5F, (double)spawnY, (double)spawnZ + (double)0.5F, this.world.rand.nextFloat() * 360.0F, 0.0F);
               NBTTagCompound entityData = entity.getEntityData();
               entityData.setBoolean("endgameEntity", true);
               entityData.setString("outpostId", this.definition.getOutpostId());
               entityData.setString("encounterGroupId", this.encounterGroup.getGroupId());
               entityData.setString("instanceKey", this.instanceKey);
               if (entity instanceof INpcConfigurable) {
                  ((INpcConfigurable)entity).applyNpcConfig(config);
               }

               entity.setCustomNameTag(this.tier.getDisplayName() + " " + config.getDisplayName());
               entity.setAlwaysRenderNameTag(true);
               if (entity instanceof EntityLiving) {
                  ((EntityLiving)entity).enablePersistence();
               }

               this.world.spawnEntity(entity);
               UUID entityUUID = entity.getUniqueID();
               this.spawnedEntityUUIDs.add(entityUUID);
               this.bossEntityUUIDs.add(entityUUID);
               if (entity instanceof EntityLivingBase) {
                  this.totalBossHpPool += (double)((EntityLivingBase)entity).getMaxHealth();
               }
            }
         }
      }

      if (this.bossEntityUUIDs.isEmpty()) {
         System.out.println("[OutpostInstance] No bosses spawned for " + this.instanceKey + " — failing.");
         this.transitionTo(OutpostState.FAILED);
      }

   }

   public void onBossDamaged(UUID playerUUID, double amount) {
      if (this.state == OutpostState.ACTIVE) {
         if (this.participants.contains(playerUUID)) {
            this.playerDamage.merge(playerUUID, amount, Double::sum);
         }
      }
   }

   public boolean isBossEntity(UUID entityUUID) {
      return this.bossEntityUUIDs.contains(entityUUID);
   }

   private boolean areBossesAllDead(World world) {
      if (this.bossEntityUUIDs.isEmpty()) {
         return true;
      } else if (world instanceof WorldServer) {
         WorldServer ws = (WorldServer)world;
         int foundCount = 0;

         for(UUID uuid : this.bossEntityUUIDs) {
            Entity entity = ws.getEntityFromUuid(uuid);
            if (entity != null) {
               ++foundCount;
               if (entity.isEntityAlive()) {
                  return false;
               }
            }
         }

         return foundCount == this.bossEntityUUIDs.size();
      } else {
         for(Entity entity : world.loadedEntityList) {
            if (this.bossEntityUUIDs.contains(entity.getUniqueID()) && entity.isEntityAlive()) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean areAllParticipantsTooFar(World world) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server == null) {
         return false;
      } else {
         BlockPos loc = this.definition.getLocation();

         for(UUID pUUID : this.participants) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(pUUID);
            if (player != null && player.isEntityAlive()) {
               double dx = player.posX - (double)loc.getX();
               double dz = player.posZ - (double)loc.getZ();
               if (dx * dx + dz * dz <= (double)14400.0F) {
                  return false;
               }
            }
         }

         return !this.participants.isEmpty();
      }
   }

   private void cleanupBosses() {
      if (this.world instanceof WorldServer) {
         WorldServer ws = (WorldServer)this.world;

         for(UUID uuid : this.bossEntityUUIDs) {
            Entity entity = ws.getEntityFromUuid(uuid);
            if (entity != null) {
               entity.setDead();
            }
         }
      }

      this.bossEntityUUIDs.clear();
   }

   private boolean areAllParticipantsGone(World world) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server == null) {
         return true;
      } else {
         for(UUID pUUID : this.participants) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(pUUID);
            if (player != null && player.isEntityAlive()) {
               return false;
            }
         }

         return true;
      }
   }

   private void syncBossHealthBars(World world) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer)world;

            for(UUID bossUUID : this.bossEntityUUIDs) {
               Entity entity = ws.getEntityFromUuid(bossUUID);
               if (entity != null && entity.isEntityAlive() && entity instanceof EntityLivingBase) {
                  EntityLivingBase boss = (EntityLivingBase)entity;
                  String bossName = entity.getName();
                  int themeColor = 0;
                  int accentColor = 0;
                  if (entity instanceof INpcConfigurable) {
                     String cfgId = ((INpcConfigurable)entity).getNpcConfigId();
                     if (cfgId != null && !cfgId.isEmpty()) {
                        NpcConfig cfg = NpcConfigRegistry.get(cfgId);
                        if (cfg != null) {
                           bossName = cfg.getDisplayName();
                           if (cfg.hasHealthBarColors()) {
                              themeColor = cfg.getThemeColor();
                              accentColor = cfg.getAccentColor();
                           }
                        }
                     }
                  }

                  EndgameCombatMessage msg = new EndgameCombatMessage(entity.getEntityId(), bossName, boss.getHealth(), boss.getMaxHealth(), 0, (byte)0, themeColor, accentColor);

                  for(UUID pUUID : this.participants) {
                     EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(pUUID);
                     if (player != null) {
                        EndgameModInit.NETWORK.sendTo(msg, player);
                     }
                  }
               }
            }

         }
      }
   }

   private void sendClearCombatBars() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         EndgameCombatMessage clearMsg = EndgameCombatMessage.clearAll();

         for(UUID pUUID : this.participants) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(pUUID);
            if (player != null) {
               EndgameModInit.NETWORK.sendTo(clearMsg, player);
            }
         }

      }
   }

   private void grantRewards() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         Random rand = new Random();
         int ryoAmount = this.tier.getRandomRyo(rand);
         long clearTimeMs = System.currentTimeMillis() - this.fightStartTime;
         EndgameSavedData savedData = EndgameSavedData.get(this.world);

         for(UUID pUUID : this.participants) {
            double contribution = this.getContributionPercent(pUUID);
            if (contribution < 0.05) {
               EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(pUUID);
               if (player != null) {
                  player.sendMessage(new TextComponentString(TextFormatting.RED + "You did not contribute enough damage to earn rewards (" + String.format("%.1f%%", contribution * (double)100.0F) + " of total)."));
               }
            } else {
               EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(pUUID);
               if (player != null) {
                  RyoRewardHelper.grantRyo(player, ryoAmount);

                  try {
                     StatManager.getInstance().grantSP(player, 2);
                  } catch (Exception var18) {
                  }

                  String cooldownKey = this.definition.getOutpostId() + "_" + this.tier.ordinal();
                  long cooldownEnd = System.currentTimeMillis() + this.definition.getCooldownMs();
                  savedData.setOutpostCooldown(pUUID, cooldownKey, cooldownEnd);
                  savedData.incrementLeaderboard("outpostClears", pUUID);
                  savedData.recordFastestClear(pUUID, this.definition.getOutpostId(), clearTimeMs);
                  int pveXpAmount = PveRank.getOutpostXp(this.tier.ordinal());
                  PveRank rankBefore = savedData.getPveRank(pUUID);
                  savedData.addPveXp(pUUID, pveXpAmount);
                  PveRank rankAfter = savedData.getPveRank(pUUID);
                  player.sendMessage(new TextComponentString(TextFormatting.AQUA + "+" + pveXpAmount + " PvE XP"));
                  if (rankAfter != rankBefore) {
                     player.sendMessage(new TextComponentString(TextFormatting.GOLD + "" + TextFormatting.BOLD + "★ PvE RANK UP! " + TextFormatting.RESET + TextFormatting.YELLOW + rankBefore.getDisplayName() + " → " + rankAfter.getDisplayName()));
                  }

                  player.sendMessage(new TextComponentString(TextFormatting.GOLD + "Outpost Clear: " + TextFormatting.WHITE + this.encounterGroup.getDisplayName() + TextFormatting.GRAY + " at " + this.definition.getLocationName() + " (" + this.tier.getDisplayName() + ")" + TextFormatting.GOLD + " | Time: " + TextFormatting.WHITE + this.formatTime(clearTimeMs) + TextFormatting.GOLD + " | Contribution: " + TextFormatting.WHITE + String.format("%.1f%%", contribution * (double)100.0F)));
               }
            }
         }

      }
   }

   private double getContributionPercent(UUID playerUUID) {
      if (this.totalBossHpPool <= (double)0.0F) {
         return (double)0.0F;
      } else {
         Double dmg = (Double)this.playerDamage.get(playerUUID);
         return dmg == null ? (double)0.0F : dmg / this.totalBossHpPool;
      }
   }

   private String formatTime(long ms) {
      long seconds = ms / 1000L;
      long minutes = seconds / 60L;
      seconds %= 60L;
      return String.format("%d:%02d", minutes, seconds);
   }

   private void syncOutpostTrackerToAll() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         for(UUID pUUID : this.participants) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(pUUID);
            if (player != null) {
               EndgameNetworkHelper.sendOutpostSync(player);
            }
         }

      }
   }

   private void clearOutpostTrackerForAll() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         for(UUID pUUID : this.participants) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(pUUID);
            if (player != null) {
               EndgameNetworkHelper.sendOutpostSync(player);
            }
         }

      }
   }

   private void clearOutpostWaypointsForAll() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         for(UUID pUUID : this.participants) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(pUUID);
            if (player != null) {
               QuestManager.getInstance().getWaypointManager().clearWaypoint(player, "outpost_mission");
            }
         }

      }
   }

   private void cleanup() {
      WaypointSpawnLogic.cleanupQuestEntities(this.world, this.spawnedEntityUUIDs);
      this.spawnedEntityUUIDs.clear();
      this.bossEntityUUIDs.clear();
      if (this.world instanceof WorldServer) {
         String myKey = this.instanceKey;

         for(Entity e : new ArrayList(this.world.loadedEntityList)) {
            if (e != null && !e.isDead && e.getEntityData().getBoolean("sasoriPuppet")) {
               String eKey = e.getEntityData().getString("instanceKey");
               if (myKey != null && myKey.equals(eKey)) {
                  e.setDead();
               }
            }
         }
      }

   }

   private void sendTitleToAll(String title, String subtitle) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         for(UUID pUUID : this.participants) {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(pUUID);
            if (player != null) {
               SPacketTitle titlePacket = new SPacketTitle(Type.TITLE, new TextComponentString(title), 5, 20, 5);
               player.connection.sendPacket(titlePacket);
               if (subtitle != null && !subtitle.isEmpty()) {
                  SPacketTitle subtitlePacket = new SPacketTitle(Type.SUBTITLE, new TextComponentString(subtitle), 5, 20, 5);
                  player.connection.sendPacket(subtitlePacket);
               }
            }
         }

      }
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("instanceKey", this.instanceKey);
      nbt.setString("outpostId", this.definition.getOutpostId());
      nbt.setInteger("tierOrdinal", this.tier.ordinal());
      nbt.setString("encounterGroupId", this.encounterGroup.getGroupId());
      nbt.setString("state", this.state.name());
      nbt.setLong("stateStartTick", this.stateStartTick);
      NBTTagList participantList = new NBTTagList();

      for(UUID uuid : this.participants) {
         NBTTagCompound entry = new NBTTagCompound();
         entry.setString("uuid", uuid.toString());
         participantList.appendTag(entry);
      }

      nbt.setTag("participants", participantList);
      return nbt;
   }

   public static OutpostInstance fromNBT(NBTTagCompound nbt, World world) {
      String outpostId = nbt.getString("outpostId");
      int tierOrdinal = nbt.getInteger("tierOrdinal");
      String encounterGroupId = nbt.getString("encounterGroupId");
      String savedState = nbt.getString("state");
      String savedInstanceKey = nbt.getString("instanceKey");
      long savedStartTick = nbt.getLong("stateStartTick");
      OutpostDefinition definition = OutpostRegistry.get(outpostId);
      if (definition == null) {
         System.out.println("[OutpostInstance] Cannot restore: unknown outpost ID '" + outpostId + "'");
         return null;
      } else {
         OutpostDifficultyTier[] tiers = OutpostDifficultyTier.values();
         if (tierOrdinal >= 0 && tierOrdinal < tiers.length) {
            OutpostDifficultyTier tier = tiers[tierOrdinal];
            OutpostRegistry.EncounterGroup encounterGroup = OutpostRegistry.getEncounterGroup(encounterGroupId);
            if (encounterGroup == null) {
               System.out.println("[OutpostInstance] Cannot restore: unknown encounter group '" + encounterGroupId + "'");
               return null;
            } else {
               Set<UUID> savedParticipants = new HashSet();
               if (nbt.hasKey("participants")) {
                  NBTTagList participantList = nbt.getTagList("participants", 10);

                  for(int i = 0; i < participantList.tagCount(); ++i) {
                     NBTTagCompound entry = participantList.getCompoundTagAt(i);

                     try {
                        savedParticipants.add(UUID.fromString(entry.getString("uuid")));
                     } catch (IllegalArgumentException var19) {
                     }
                  }
               }

               if (savedParticipants.isEmpty()) {
                  System.out.println("[OutpostInstance] Cannot restore: no participants for instance '" + savedInstanceKey + "'");
                  return null;
               } else {
                  OutpostInstance instance = new OutpostInstance(definition, tier, encounterGroup, world, savedInstanceKey);

                  for(UUID uuid : savedParticipants) {
                     instance.participants.add(uuid);
                     instance.playerDamage.put(uuid, (double)0.0F);
                  }

                  OutpostState restoredState;
                  try {
                     restoredState = OutpostState.valueOf(savedState);
                  } catch (IllegalArgumentException var18) {
                     restoredState = OutpostState.FAILED;
                  }

                  if (restoredState != OutpostState.ACTIVE && restoredState != OutpostState.COUNTDOWN) {
                     if (restoredState != OutpostState.TRAVELING) {
                        System.out.println("[OutpostInstance] Not restoring '" + savedInstanceKey + "' in terminal state " + savedState + ".");
                        return null;
                     }

                     instance.state = OutpostState.TRAVELING;
                     instance.stateStartTick = savedStartTick;
                     System.out.println("[OutpostInstance] Restored '" + savedInstanceKey + "' in TRAVELING state.");
                  } else {
                     instance.state = OutpostState.FAILED;
                     instance.stateStartTick = world.getTotalWorldTime();
                     System.out.println("[OutpostInstance] Restored '" + savedInstanceKey + "' — was " + savedState + ", set to FAILED (bosses lost on restart).");
                  }

                  return instance;
               }
            }
         } else {
            System.out.println("[OutpostInstance] Cannot restore: invalid tier ordinal " + tierOrdinal);
            return null;
         }
      }
   }

   public OutpostDefinition getDefinition() {
      return this.definition;
   }

   public OutpostDifficultyTier getTier() {
      return this.tier;
   }

   public OutpostRegistry.EncounterGroup getEncounterGroup() {
      return this.encounterGroup;
   }

   public String getInstanceKey() {
      return this.instanceKey;
   }

   public Set<UUID> getSpawnedEntityUUIDs() {
      return this.spawnedEntityUUIDs;
   }

   public Map<UUID, Double> getPlayerDamage() {
      return this.playerDamage;
   }

   public double getTotalBossHpPool() {
      return this.totalBossHpPool;
   }

   public long getFightStartTime() {
      return this.fightStartTime;
   }

   public static enum OutpostState {
      TRAVELING,
      COUNTDOWN,
      ACTIVE,
      VICTORY,
      FAILED,
      CLEANUP;
   }
}
