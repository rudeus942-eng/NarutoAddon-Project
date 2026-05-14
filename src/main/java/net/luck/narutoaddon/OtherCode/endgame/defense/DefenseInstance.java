
package net.luck.narutoaddon.OtherCode.endgame.defense;

import net.luck.narutoaddon.OtherCode.endgame.EndgameSavedData;
import net.luck.narutoaddon.OtherCode.endgame.PveRank;
import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointSpawnLogic;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class DefenseInstance {
   private final DefenseDefinition definition;
   private final DefenseManager.DefenseDifficulty difficulty;
   private DefenseState state;
   private int currentWaveIndex;
   private int villageHP;
   private final Set<UUID> participants = new HashSet();
   private final Map<UUID, Double> waveDamageContribution = new HashMap();
   private final Set<UUID> spawnedEntities = new HashSet();
   private final Set<UUID> breachedEntities = new HashSet();
   private double totalWaveHpPool;
   private long stateStartTick;
   private final List<DefenseWave> waves;
   private static final int COUNTDOWN_TICKS = 200;
   private static final int WAVE_BREAK_TICKS = 900;
   private static final int WAVE_REWARD_RYO = 250;
   private static final int WAVE_STALE_TIMEOUT_TICKS = 6000;

   private static String grunt(int wave) {
      int tier = Math.min(1 + (wave - 1) / 2, 5);
      return "wave_grunt_" + tier;
   }

   private static String captain(int wave) {
      int tier = Math.min(1 + (wave - 1) / 2, 5);
      return "wave_captain_" + tier;
   }

   private static String elite(int wave) {
      int tier = Math.min(1 + (wave - 1) / 2, 5);
      return "wave_elite_" + tier;
   }

   public DefenseInstance(DefenseDefinition definition, DefenseManager.DefenseDifficulty difficulty, World world) {
      this.definition = definition;
      this.difficulty = difficulty;
      this.state = DefenseState.COUNTDOWN;
      this.currentWaveIndex = 0;
      this.villageHP = definition.getVillageMaxHP();
      this.stateStartTick = world.getTotalWorldTime();
      this.waves = this.generateWaves();
   }

   private List<DefenseWave> generateWaves() {
      List<DefenseWave> result = new ArrayList();
      double mult = this.difficulty.hpMult;
      result.add(this.buildWave(1, 1200, new int[]{0}, new String[][]{{grunt(1)}}, new int[][]{{scale(3, mult)}}));
      result.add(this.buildWave(2, 1200, new int[]{0, 1}, new String[][]{{grunt(2)}, {grunt(2)}}, new int[][]{{scale(2, mult)}, {scale(2, mult)}}));
      result.add(this.buildWave(3, 1200, new int[]{0, 1}, new String[][]{{captain(3), grunt(3)}, {captain(3), grunt(3)}}, new int[][]{{scale(1, mult), scale(2, mult)}, {scale(1, mult), scale(1, mult)}}));
      result.add(this.buildWave(4, 1200, new int[]{0, 1, 2}, new String[][]{{captain(4), grunt(4)}, {captain(4), grunt(4)}, {captain(4), grunt(4)}}, new int[][]{{scale(1, mult), scale(2, mult)}, {scale(1, mult), scale(1, mult)}, {scale(1, mult), scale(1, mult)}}));
      result.add(this.buildWave(5, 1200, new int[]{0, 1, 2}, new String[][]{{elite(5), captain(5), grunt(5)}, {captain(5), grunt(5)}, {grunt(5)}}, new int[][]{{scale(1, mult), scale(1, mult), scale(1, mult)}, {scale(1, mult), scale(1, mult)}, {scale(1, mult)}}));
      result.add(this.buildWave(6, 1200, new int[]{0, 1, 2}, new String[][]{{elite(6), captain(6), grunt(6)}, {elite(6), captain(6), grunt(6)}, {captain(6), grunt(6)}}, new int[][]{{scale(1, mult), scale(1, mult), scale(2, mult)}, {scale(1, mult), scale(1, mult), scale(2, mult)}, {scale(1, mult), scale(1, mult)}}));
      return result;
   }

   private static int scale(int base, double mult) {
      int result = (int)Math.round((double)base * mult);
      return Math.max(1, Math.min(result, 20));
   }

   private DefenseWave buildWave(int waveNum, int delay, int[] spawnPointIndices, String[][] enemies, int[][] counts) {
      Map<Integer, String[]> enemiesBySpawn = new HashMap();
      Map<Integer, int[]> countsBySpawn = new HashMap();

      for(int i = 0; i < spawnPointIndices.length; ++i) {
         enemiesBySpawn.put(spawnPointIndices[i], enemies[i]);
         countsBySpawn.put(spawnPointIndices[i], counts[i]);
      }

      return new DefenseWave(waveNum, enemiesBySpawn, countsBySpawn, delay);
   }

   public void tick(World world) {
      long currentTick = world.getTotalWorldTime();
      long elapsed = currentTick - this.stateStartTick;
      switch (this.state) {
         case COUNTDOWN:
            if (elapsed >= 200L) {
               this.state = DefenseState.WAVE_ACTIVE;
               this.stateStartTick = currentTick;
               this.spawnWaveEnemies(world, this.currentWaveIndex);
               this.broadcastToParticipants(world, "§6§l[Defense] §eWave " + (this.currentWaveIndex + 1) + "/" + this.waves.size() + " incoming!");
            }
            break;
         case WAVE_ACTIVE:
            if (elapsed >= 6000L) {
               this.broadcastToParticipants(world, "§c[Defense] §7The defense has timed out.");
               this.state = DefenseState.FAILED;
               this.stateStartTick = currentTick;
               return;
            }

            this.checkEnemyBreach(world);
            if (this.villageHP <= 0) {
               this.state = DefenseState.FAILED;
               this.stateStartTick = currentTick;
               this.broadcastToParticipants(world, "§c§l[Defense] §4Village defenses have fallen! The enemy has overrun the village.");
               return;
            }

            if (this.areAllSpawnedDead(world)) {
               Set<UUID> qualified = new HashSet(this.participants);
               this.grantWaveReward(world, qualified);
               ++this.currentWaveIndex;
               this.waveDamageContribution.clear();
               if (this.currentWaveIndex >= this.waves.size()) {
                  this.state = DefenseState.VICTORY;
                  this.stateStartTick = currentTick;
                  this.grantVictoryReward(world);
                  this.broadcastToParticipants(world, "§a§l[Defense] §2Victory! The village is safe! §7HP remaining: §a" + this.villageHP + "%");
               } else {
                  this.state = DefenseState.WAVE_BREAK;
                  this.stateStartTick = currentTick;
                  this.broadcastToParticipants(world, "§a§l[Defense] §eWave " + this.currentWaveIndex + " cleared! §7Next wave in 45 seconds. §7Village HP: §a" + this.villageHP + "%");
               }
            }
            break;
         case WAVE_BREAK:
            if (elapsed >= 900L) {
               this.state = DefenseState.WAVE_ACTIVE;
               this.stateStartTick = currentTick;
               this.spawnWaveEnemies(world, this.currentWaveIndex);
               this.broadcastToParticipants(world, "§6§l[Defense] §eWave " + (this.currentWaveIndex + 1) + "/" + this.waves.size() + " incoming!");
            }
         case VICTORY:
         case FAILED:
      }

   }

   public void spawnWaveEnemies(World world, int waveIndex) {
      if (waveIndex >= 0 && waveIndex < this.waves.size()) {
         DefenseWave wave = (DefenseWave)this.waves.get(waveIndex);
         this.spawnedEntities.clear();
         this.breachedEntities.clear();
         this.totalWaveHpPool = (double)0.0F;
         List<DefenseSpawnPoint> spawnPoints = this.definition.getSpawnPoints();

         for(Map.Entry<Integer, String[]> entry : wave.getEnemiesBySpawnPoint().entrySet()) {
            int spIdx = (Integer)entry.getKey();
            if (spIdx >= 0 && spIdx < spawnPoints.size()) {
               DefenseSpawnPoint sp = (DefenseSpawnPoint)spawnPoints.get(spIdx);
               String[] configIds = (String[])entry.getValue();
               int[] counts = (int[])wave.getCountsBySpawnPoint().get(spIdx);
               if (counts != null) {
                  for(int i = 0; i < configIds.length; ++i) {
                     int count = i < counts.length ? counts[i] : 1;

                     for(int j = 0; j < count; ++j) {
                        Entity entity = this.spawnDefenseEnemy(world, configIds[i], sp);
                        if (entity != null) {
                           this.spawnedEntities.add(entity.getUniqueID());
                           if (entity instanceof EntityLivingBase) {
                              this.totalWaveHpPool += (double)((EntityLivingBase)entity).getMaxHealth();
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private Entity spawnDefenseEnemy(World world, String configId, DefenseSpawnPoint spawnPoint) {
      NpcConfig config = NpcConfigRegistry.get(configId);
      if (config == null) {
         System.out.println("[Defense] Unknown NPC config: " + configId);
         return null;
      } else {
         Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(config.getEntityRegistryId()), world);
         if (entity == null) {
            System.out.println("[Defense] Failed to create entity: " + config.getEntityRegistryId());
            return null;
         } else {
            BlockPos pos = spawnPoint.getPosition();
            BlockPos landPos = WaypointSpawnLogic.findLandPosition(world, pos);
            int groundY = WaypointSpawnLogic.findGroundY(world, landPos.getX(), landPos.getZ());
            double offsetX = (world.rand.nextDouble() - (double)0.5F) * (double)8.0F;
            double offsetZ = (world.rand.nextDouble() - (double)0.5F) * (double)8.0F;
            double spawnX = (double)landPos.getX() + (double)0.5F + offsetX;
            double spawnZ = (double)landPos.getZ() + (double)0.5F + offsetZ;
            double spawnY = (double)groundY;
            float yaw = spawnPoint.getDirectionTowardCenter();
            entity.setLocationAndAngles(spawnX, spawnY, spawnZ, yaw, 0.0F);
            if (entity instanceof EntityLivingBase) {
               ((EntityLivingBase)entity).rotationYawHead = yaw;
               ((EntityLivingBase)entity).renderYawOffset = yaw;
            }

            if (entity instanceof INpcConfigurable) {
               ((INpcConfigurable)entity).applyNpcConfig(config);
            }

            if (entity instanceof EntityLivingBase) {
               EntityLivingBase living = (EntityLivingBase)entity;
               if (this.difficulty.hpMult != (double)1.0F) {
                  double scaledHp = living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() * this.difficulty.hpMult;
                  living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(scaledHp);
                  living.setHealth((float)scaledHp);
               }

               if (this.difficulty.dmgMult != (double)1.0F && living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
                  double scaledDmg = living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue() * this.difficulty.dmgMult;
                  living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(scaledDmg);
               }
            }

            NBTTagCompound entityData = entity.getEntityData();
            entityData.setBoolean("questEntity", true);
            entityData.setBoolean("defenseEntity", true);
            entityData.setString("defenseVillage", this.definition.getVillageName());
            entityData.setString("npcConfigId", configId);
            entityData.setBoolean("PersistenceRequired", true);
            if (entity instanceof EntityLiving) {
               ((EntityLiving)entity).enablePersistence();
            }

            world.spawnEntity(entity);
            return entity;
         }
      }
   }

   public void checkEnemyBreach(World world) {
      BlockPos center = this.definition.getCenterPos();
      double radiusSq = (double)(this.definition.getCenterRadius() * this.definition.getCenterRadius());

      for(Entity entity : new ArrayList(world.loadedEntityList)) {
         if (entity != null && !entity.isDead && this.spawnedEntities.contains(entity.getUniqueID()) && !this.breachedEntities.contains(entity.getUniqueID())) {
            double dx = entity.posX - (double)center.getX();
            double dz = entity.posZ - (double)center.getZ();
            double distSq = dx * dx + dz * dz;
            if (distSq <= radiusSq) {
               --this.villageHP;
               this.breachedEntities.add(entity.getUniqueID());
               if (this.villageHP <= 0) {
                  this.villageHP = 0;
                  return;
               }
            }
         }
      }

   }

   public void onEnemyDamaged(UUID playerUUID, double amount) {
      this.waveDamageContribution.merge(playerUUID, amount, Double::sum);
   }

   public void grantWaveReward(World world, Set<UUID> qualified) {
      int ryo = (int)((double)250.0F * this.difficulty.dmgMult);
      EndgameSavedData data = EndgameSavedData.get(world);

      for(UUID uuid : qualified) {
         EntityPlayerMP player = this.getOnlinePlayer(uuid);
         if (player != null) {
            int actualRyo = RyoRewardHelper.grantRyoSilent(player, ryo);
            PveRank rankBefore = data.getPveRank(uuid);
            data.addPveXp(uuid, 15);
            PveRank rankAfter = data.getPveRank(uuid);
            String ryoText = "§a+" + actualRyo + " Ryo";
            if (actualRyo > ryo) {
               ryoText = ryoText + " §6(+" + (actualRyo - ryo) + " territory)";
            }

            player.sendMessage(new TextComponentString(ryoText + " §b+" + 15 + " PvE XP §7(wave clear)"));
            if (rankAfter != rankBefore) {
               player.sendMessage(new TextComponentString("§6§l★ PvE RANK UP! §r§e" + rankBefore.getDisplayName() + " → " + rankAfter.getDisplayName()));
            }

            data.incrementLeaderboard("defenseWaves", uuid);
         }
      }

   }

   public void grantVictoryReward(World world) {
      int ryo = this.difficulty.ryoReward;
      int sp = this.difficulty.spReward;
      int pveXpAmount = PveRank.getDefenseVictoryXp(this.difficulty.ordinal());
      EndgameSavedData data = EndgameSavedData.get(world);

      for(UUID uuid : this.participants) {
         EntityPlayerMP player = this.getOnlinePlayer(uuid);
         if (player != null) {
            int actualRyo = RyoRewardHelper.grantRyoSilent(player, ryo);
            if (sp > 0) {
               player.getServer().getCommandManager().executeCommand(player.getServer(), "statadmin addsp " + player.getName() + " " + sp);
            }

            PveRank rankBefore = data.getPveRank(uuid);
            data.addPveXp(uuid, pveXpAmount);
            PveRank rankAfter = data.getPveRank(uuid);
            String victRyoText = "§a+" + actualRyo + " Ryo";
            if (actualRyo > ryo) {
               victRyoText = victRyoText + " §6(+" + (actualRyo - ryo) + " territory)";
            }

            player.sendMessage(new TextComponentString("§6§l[Defense Victory] " + victRyoText + (sp > 0 ? " §b+" + sp + " SP" : "") + " §b+" + pveXpAmount + " PvE XP"));
            if (rankAfter != rankBefore) {
               player.sendMessage(new TextComponentString("§6§l★ PvE RANK UP! §r§e" + rankBefore.getDisplayName() + " → " + rankAfter.getDisplayName()));
            }

            data.setDefenseCompletionTime(uuid, this.definition.getVillageName(), System.currentTimeMillis());
         }
      }

   }

   public void cleanup() {
      if (!this.spawnedEntities.isEmpty()) {
         WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
         if (world != null) {
            WaypointSpawnLogic.cleanupQuestEntities(world, this.spawnedEntities);
         }

         this.spawnedEntities.clear();
         this.breachedEntities.clear();
      }
   }

   private boolean areAllSpawnedDead(World world) {
      return WaypointSpawnLogic.areAllEnemiesDead(world, this.spawnedEntities);
   }

   private EntityPlayerMP getOnlinePlayer(UUID uuid) {
      return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uuid);
   }

   private void broadcastToParticipants(World world, String message) {
      for(UUID uuid : this.participants) {
         EntityPlayerMP player = this.getOnlinePlayer(uuid);
         if (player != null) {
            player.sendMessage(new TextComponentString(message));
         }
      }

   }

   public DefenseDefinition getDefinition() {
      return this.definition;
   }

   public DefenseManager.DefenseDifficulty getDifficulty() {
      return this.difficulty;
   }

   public DefenseState getState() {
      return this.state;
   }

   public int getCurrentWaveIndex() {
      return this.currentWaveIndex;
   }

   public int getTotalWaves() {
      return this.waves.size();
   }

   public int getVillageHP() {
      return this.villageHP;
   }

   public Set<UUID> getParticipants() {
      return Collections.unmodifiableSet(this.participants);
   }

   public void addParticipant(UUID uuid) {
      this.participants.add(uuid);
   }

   public void removeParticipant(UUID uuid) {
      this.participants.remove(uuid);
   }

   public Set<UUID> getSpawnedEntities() {
      return Collections.unmodifiableSet(this.spawnedEntities);
   }

   public double getTotalWaveHpPool() {
      return this.totalWaveHpPool;
   }

   public static enum DefenseState {
      COUNTDOWN,
      WAVE_ACTIVE,
      WAVE_BREAK,
      VICTORY,
      FAILED;
   }
}
