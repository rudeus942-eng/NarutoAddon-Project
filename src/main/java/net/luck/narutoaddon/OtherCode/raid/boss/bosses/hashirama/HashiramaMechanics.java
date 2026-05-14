
package net.luck.narutoaddon.OtherCode.raid.boss.bosses.hashirama;

import net.luck.narutoaddon.OtherCode.entity.*;
import net.luck.narutoaddon.OtherCode.raid.boss.BossMechanic;
import net.luck.narutoaddon.OtherCode.raid.boss.mechanics.SummonMechanic;
import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.core.RaidInstance;
import net.luck.narutoaddon.OtherCode.raid.integration.NarutoModIntegration;
import net.luck.narutoaddon.OtherCode.raid.util.KnockbackHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.*;

public class HashiramaMechanics {
   private static final float TRUE_DAMAGE_PERCENT = 0.3F;
   private static final DamageSource TRUE_DAMAGE_SOURCE = (new DamageSource("hashirama_true")).setDamageBypassesArmor().setMagicDamage();
   private static Entity activeWoodGolem = null;
   private static int activeWoodGolemWorldId = Integer.MIN_VALUE;
   private static Map<Integer, FloweringTreesMechanic> activeFloweringTreesByRaid = new HashMap();
   private static Map<Integer, WoodPrisonMechanic> activeWoodPrisonByRaid = new HashMap();
   private static Map<Integer, World> activeWorldByRaid = new HashMap();
   private static FloweringTreesMechanic activeFloweringTrees = null;
   private static WoodPrisonMechanic activeWoodPrison = null;
   private static World activeWorld = null;

   public static void applyDamageWithTrueComponent(EntityLivingBase target, float totalDamage, DamageSource normalSource) {
      float trueDamage = totalDamage * 0.3F;
      float normalDamage = totalDamage * 0.7F;
      target.hurtResistantTime = 0;
      target.attackEntityFrom(normalSource, normalDamage);
      target.hurtResistantTime = 0;
      target.attackEntityFrom(TRUE_DAMAGE_SOURCE, trueDamage);
   }

   public static void setActiveFloweringTrees(FloweringTreesMechanic mechanic, World world, int raidId) {
      activeFloweringTrees = mechanic;
      activeWorld = world;
      activeFloweringTreesByRaid.put(raidId, mechanic);
      activeWorldByRaid.put(raidId, world);
   }

   /** @deprecated */
   public static void setActiveFloweringTrees(FloweringTreesMechanic mechanic, World world) {
      activeFloweringTrees = mechanic;
      activeWorld = world;
   }

   public static void clearActiveFloweringTrees(int raidId) {
      activeFloweringTreesByRaid.remove(raidId);
      activeFloweringTrees = null;
   }

   public static void clearActiveFloweringTrees() {
      activeFloweringTrees = null;
   }

   public static void setActiveWoodPrison(WoodPrisonMechanic mechanic, World world, int raidId) {
      activeWoodPrison = mechanic;
      activeWorld = world;
      activeWoodPrisonByRaid.put(raidId, mechanic);
      activeWorldByRaid.put(raidId, world);
   }

   /** @deprecated */
   public static void setActiveWoodPrison(WoodPrisonMechanic mechanic, World world) {
      activeWoodPrison = mechanic;
      activeWorld = world;
   }

   public static void clearActiveWoodPrison(int raidId) {
      activeWoodPrisonByRaid.remove(raidId);
      activeWoodPrison = null;
   }

   public static void clearActiveWoodPrison() {
      activeWoodPrison = null;
   }

   public static boolean hasActiveWoodGolem(World world) {
      if (activeWoodGolem != null && !activeWoodGolem.isDead) {
         if (world != null && world.provider.getDimension() != activeWoodGolemWorldId) {
            System.out.println("[HashiramaMechanics] hasActiveWoodGolem: golem exists but in different world (golem world=" + activeWoodGolemWorldId + ", check world=" + world.provider.getDimension() + ")");
            return false;
         } else if (activeWoodGolem.world != null && activeWoodGolem.world.loadedEntityList.contains(activeWoodGolem)) {
            System.out.println("[HashiramaMechanics] hasActiveWoodGolem: true (golem=" + activeWoodGolem.getEntityId() + ", world=" + activeWoodGolemWorldId + ")");
            return true;
         } else {
            System.out.println("[HashiramaMechanics] hasActiveWoodGolem: golem reference stale (not in world entity list)");
            activeWoodGolem = null;
            activeWoodGolemWorldId = Integer.MIN_VALUE;
            return false;
         }
      } else {
         activeWoodGolem = null;
         activeWoodGolemWorldId = Integer.MIN_VALUE;
         return false;
      }
   }

   public static boolean hasActiveWoodGolem() {
      return hasActiveWoodGolem((World)null);
   }

   public static void setActiveWoodGolem(Entity golem) {
      activeWoodGolem = golem;
      if (golem != null && golem.world != null) {
         activeWoodGolemWorldId = golem.world.provider.getDimension();
         System.out.println("[HashiramaMechanics] setActiveWoodGolem: " + golem.getEntityId() + " in world " + activeWoodGolemWorldId);
      } else {
         activeWoodGolemWorldId = Integer.MIN_VALUE;
      }

   }

   public static void clearActiveWoodGolem() {
      System.out.println("[HashiramaMechanics] clearActiveWoodGolem called");
      activeWoodGolem = null;
      activeWoodGolemWorldId = Integer.MIN_VALUE;
   }

   public static Entity getActiveWoodGolem() {
      if (activeWoodGolem != null && activeWoodGolem.isDead) {
         activeWoodGolem = null;
         activeWoodGolemWorldId = Integer.MIN_VALUE;
      }

      return activeWoodGolem;
   }

   public static void resetMechanicState(int raidId) {
      activeWoodGolem = null;
      activeWoodGolemWorldId = Integer.MIN_VALUE;
      FloweringTreesMechanic floweringTrees = (FloweringTreesMechanic)activeFloweringTreesByRaid.remove(raidId);
      World raidWorld = (World)activeWorldByRaid.remove(raidId);
      if (floweringTrees != null) {
         System.out.println("[HashiramaMechanics] Cleaning up FloweringTrees blocks for raid #" + raidId);
         floweringTrees.forceCleanup(raidWorld);
      }

      if (activeFloweringTrees == floweringTrees) {
         activeFloweringTrees = null;
      }

      WoodPrisonMechanic woodPrison = (WoodPrisonMechanic)activeWoodPrisonByRaid.remove(raidId);
      if (woodPrison != null) {
         System.out.println("[HashiramaMechanics] Cleaning up WoodPrison blocks for raid #" + raidId);
         woodPrison.forceCleanup(raidWorld != null ? raidWorld : activeWorld);
      }

      if (activeWoodPrison == woodPrison) {
         activeWoodPrison = null;
      }

      if (activeFloweringTreesByRaid.isEmpty() && activeWoodPrisonByRaid.isEmpty()) {
         activeWorld = null;
      }

   }

   public static void resetMechanicState() {
      activeWoodGolem = null;
      activeWoodGolemWorldId = Integer.MIN_VALUE;

      for(Map.Entry<Integer, FloweringTreesMechanic> entry : activeFloweringTreesByRaid.entrySet()) {
         World world = (World)activeWorldByRaid.get(entry.getKey());
         if (entry.getValue() != null) {
            ((FloweringTreesMechanic)entry.getValue()).forceCleanup(world);
         }
      }

      activeFloweringTreesByRaid.clear();

      for(Map.Entry<Integer, WoodPrisonMechanic> entry : activeWoodPrisonByRaid.entrySet()) {
         World world = (World)activeWorldByRaid.get(entry.getKey());
         if (entry.getValue() != null) {
            ((WoodPrisonMechanic)entry.getValue()).forceCleanup(world);
         }
      }

      activeWoodPrisonByRaid.clear();
      activeWorldByRaid.clear();
      if (activeFloweringTrees != null) {
         activeFloweringTrees.forceCleanup(activeWorld);
         activeFloweringTrees = null;
      }

      if (activeWoodPrison != null) {
         activeWoodPrison.forceCleanup(activeWorld);
         activeWoodPrison = null;
      }

      activeWorld = null;
   }

   public static BossMechanic createWoodDragon(RaidDifficulty difficulty) {
      float damage = 8.0F * difficulty.getDamageMultiplier();
      int duration = 100 + difficulty.ordinal() * 20;
      return new WoodDragonMechanic(damage, duration, difficulty);
   }

   public static BossMechanic createHoteiTechnique(RaidDifficulty difficulty) {
      float damage = 14.4F * difficulty.getDamageMultiplier();
      int handCount = 2 + difficulty.ordinal();
      return new HoteiTechniqueMechanic(damage, handCount, difficulty);
   }

   public static BossMechanic createFloweringTrees(RaidDifficulty difficulty) {
      float damage = 5.6F * difficulty.getDamageMultiplier();
      int safeZones = 3 - difficulty.ordinal();
      if (safeZones < 1) {
         safeZones = 1;
      }

      return new FloweringTreesMechanic(damage, difficulty, Math.max(1, safeZones));
   }

   public static BossMechanic createWoodClones(RaidDifficulty difficulty) {
      int cloneCount = difficulty.getCloneCount();
      float cloneHealth = 350.0F * difficulty.getHealthMultiplier();
      float cloneDamage = 5.6F * difficulty.getDamageMultiplier();
      return (new SummonMechanic.Builder()).name("wood_clones").displayName("Wood Clones").warningTicks(60 - difficulty.getWarningTimeReduction()).durationTicks(720).summonCount(cloneCount).puzzle(true).damageWindowTicks(200).entityFactory((world, boss) -> {
         EntityHashiramaClone.EntityCustom clone = new EntityHashiramaClone.EntityCustom(world, boss);
         clone.setCloneHealth(cloneHealth);
         clone.setCloneDamage(cloneDamage);
         clone.setMaxLifetime(720);
         return clone;
      }).build();
   }

   public static BossMechanic createTrueSeveralThousandHands(RaidDifficulty difficulty) {
      float damage = 56.0F * difficulty.getDamageMultiplier();
      return new TrueSeveralThousandHandsMechanic(damage, difficulty);
   }

   public static BossMechanic createWoodHuman(RaidDifficulty difficulty) {
      return new WoodHumanMechanic(difficulty);
   }

   public static BossMechanic createWoodPrison(RaidDifficulty difficulty) {
      return new WoodPrisonMechanic(difficulty);
   }

   public static BossMechanic createWoodSpearBarrage(RaidDifficulty difficulty) {
      float damage = 6.4F * difficulty.getDamageMultiplier();
      int spearCount = 5 + difficulty.ordinal();
      float spreadAngle = 25.0F - (float)difficulty.ordinal() * 3.0F;
      int cooldown = 60 - difficulty.ordinal() * 10;
      return new WoodSpearBarrageMechanic(damage, spearCount, spreadAngle, cooldown, difficulty);
   }

   private static class WoodDragonMechanic implements BossMechanic {
      private final float damage;
      private final int duration;
      private final RaidDifficulty difficulty;
      private EntityPlayer target;
      private EntityWoodDragon.EntityCustom spawnedDragon = null;

      public WoodDragonMechanic(float damage, int duration, RaidDifficulty difficulty) {
         this.damage = damage;
         this.duration = duration;
         this.difficulty = difficulty;
      }

      public String getName() {
         return "wood_dragon";
      }

      public String getDisplayName() {
         return "Wood Dragon";
      }

      public String getWarningMessage() {
         return "Hashirama summons a Wood Dragon!";
      }

      public int getWarningTicks() {
         return 40 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return this.duration;
      }

      public int getSelectionWeight() {
         return 10;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         List<EntityPlayerMP> players = (List<EntityPlayerMP>)(raid != null ? raid.getParticipants() : new ArrayList());
         if (!players.isEmpty()) {
            this.target = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
            if (raid != null) {
               raid.broadcastMessage("§cWood Dragon is targeting " + this.target.getName() + "!");
            }

            float scale = 1.5F + (float)this.difficulty.ordinal() * 0.75F;
            float dragonDamage = this.damage;
            float chakraDrain = 20.0F + (float)this.difficulty.ordinal() * 14.0F;
            float moveSpeed = 0.95F + (float)this.difficulty.ordinal() * 0.19F;
            this.spawnedDragon = new EntityWoodDragon.EntityCustom(boss.world, boss, this.target);
            this.spawnedDragon.setPosition(boss.posX, boss.posY + (double)2.0F, boss.posZ);
            this.spawnedDragon.setDragonScale(scale);
            this.spawnedDragon.setDamage(dragonDamage);
            this.spawnedDragon.setChakraDrain(chakraDrain);
            this.spawnedDragon.setMoveSpeed(moveSpeed);
            this.spawnedDragon.setMaxLifetime(this.duration);
            boss.world.spawnEntity(this.spawnedDragon);
            if (raid != null) {
               raid.trackSpawnedEntity(this.spawnedDragon);
            }

            System.out.println("[HashiramaMechanics] Spawned Wood Dragon - scale=" + scale + ", damage=" + dragonDamage + ", chakraDrain=" + chakraDrain + ", speed=" + moveSpeed);
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_ENDERDRAGON_GROWL, SoundCategory.HOSTILE, 2.0F, 0.7F);
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (this.spawnedDragon != null && this.spawnedDragon.isDead) {
            this.spawnedDragon = null;
         }

         if (this.spawnedDragon != null && (this.target == null || this.target.isDead)) {
            List<EntityPlayerMP> players = (List<EntityPlayerMP>)(raid != null ? raid.getParticipants() : new ArrayList());
            if (!players.isEmpty()) {
               this.target = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
               this.spawnedDragon.setTarget(this.target);
               if (raid != null) {
                  raid.broadcastMessage("§6Wood Dragon is now targeting " + this.target.getName() + "!");
               }
            }
         }

      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         if (this.spawnedDragon != null && !this.spawnedDragon.isDead) {
            this.spawnedDragon.setDead();
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0F, 1.2F);
      }
   }

   private static class HoteiTechniqueMechanic implements BossMechanic {
      private final float damage;
      private final int handCount;
      private final RaidDifficulty difficulty;
      private List<BlockPos> slamLocations = new ArrayList();
      private List<Entity> spawnedHands = new ArrayList();
      private boolean handsSlammed = false;
      private final int slamTick;

      public HoteiTechniqueMechanic(float damage, int handCount, RaidDifficulty difficulty) {
         this.damage = damage;
         this.handCount = handCount;
         this.difficulty = difficulty;
         this.slamTick = 50 - difficulty.ordinal() * 15;
      }

      public String getName() {
         return "hotei_technique";
      }

      public String getDisplayName() {
         return "Hotei Technique";
      }

      public String getWarningMessage() {
         return "Hashirama summons giant wooden hands!";
      }

      public int getWarningTicks() {
         return 80 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return this.slamTick + 20;
      }

      public int getSelectionWeight() {
         return 10;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.slamLocations.clear();
         this.spawnedHands.clear();
         this.handsSlammed = false;
         if (raid != null) {
            List<EntityPlayerMP> players = raid.getParticipants();

            for(int i = 0; i < Math.min(this.handCount, players.size()); ++i) {
               EntityPlayerMP target = (EntityPlayerMP)players.get(i);
               this.slamLocations.add(target.getPosition());
            }

            while(this.slamLocations.size() < this.handCount && !players.isEmpty()) {
               EntityPlayerMP target = (EntityPlayerMP)players.get(boss.world.rand.nextInt(players.size()));
               int offsetX = boss.world.rand.nextInt(10) - 5;
               int offsetZ = boss.world.rand.nextInt(10) - 5;
               this.slamLocations.add(target.getPosition().add(offsetX, 0, offsetZ));
            }

            raid.broadcastMessage("§c§l" + this.handCount + " giant hands are descending!");
            raid.broadcastMessage("§eMove away from the marked areas!");
            boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.HOSTILE, 2.0F, 0.6F);
         }
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            if (!this.handsSlammed && ticksElapsed % 3 == 0 && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;
               int radius = 5 + this.difficulty.ordinal();

               for(BlockPos slamPos : this.slamLocations) {
                  for(int angle = 0; angle < 360; angle += 15) {
                     double rad = Math.toRadians((double)angle);
                     double x = (double)slamPos.getX() + (double)0.5F + (double)radius * Math.cos(rad);
                     double z = (double)slamPos.getZ() + (double)0.5F + (double)radius * Math.sin(rad);
                     world.spawnParticle(EnumParticleTypes.FLAME, x, (double)slamPos.getY() + (double)0.5F, z, 2, (double)0.0F, 0.1, (double)0.0F, (double)0.0F, new int[0]);
                  }

                  double heightProgress = (double)1.0F - (double)ticksElapsed / (double)this.slamTick;
                  double handY = (double)slamPos.getY() + (double)20.0F * Math.max((double)0.0F, heightProgress);
                  world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)slamPos.getX() + (double)0.5F, handY, (double)slamPos.getZ() + (double)0.5F, 10, (double)2.0F, (double)1.0F, (double)2.0F, (double)0.0F, new int[0]);
               }
            }

            if (ticksElapsed == this.slamTick && !this.handsSlammed) {
               this.handsSlammed = true;
               int radius = 5 + this.difficulty.ordinal();

               for(BlockPos slamPos : this.slamLocations) {
                  if (NarutoModIntegration.isNarutoModLoaded()) {
                     Entity arm = NarutoModIntegration.spawnWoodArm(boss.world, (double)slamPos.getX(), (double)slamPos.getY(), (double)slamPos.getZ(), boss);
                     if (arm != null) {
                        this.spawnedHands.add(arm);
                        if (raid != null) {
                           raid.trackSpawnedEntity(arm);
                        }
                     }
                  }

                  AxisAlignedBB hitbox = new AxisAlignedBB((double)(slamPos.getX() - radius), (double)(slamPos.getY() - 2), (double)(slamPos.getZ() - radius), (double)(slamPos.getX() + radius), (double)(slamPos.getY() + 5), (double)(slamPos.getZ() + radius));

                  for(EntityPlayer player : boss.world.getEntitiesWithinAABB(EntityPlayer.class, hitbox)) {
                     HashiramaMechanics.applyDamageWithTrueComponent(player, this.damage, DamageSource.MAGIC);
                     double dx = player.posX - (double)slamPos.getX();
                     double dz = player.posZ - (double)slamPos.getZ();
                     double dist = Math.sqrt(dx * dx + dz * dz);
                     if (dist > (double)0.0F) {
                        KnockbackHelper.applyWallSafeKnockback(player, dx / dist * (double)1.0F, (double)0.5F, dz / dist * (double)1.0F);
                     }
                  }

                  if (boss.world instanceof WorldServer) {
                     WorldServer world = (WorldServer)boss.world;
                     world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, (double)slamPos.getX() + (double)0.5F, (double)(slamPos.getY() + 1), (double)slamPos.getZ() + (double)0.5F, 15, (double)radius / (double)2.0F, (double)1.0F, (double)radius / (double)2.0F, (double)0.0F, new int[0]);
                  }
               }

               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.5F, 0.7F);
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         this.slamLocations.clear();

         for(Entity hand : this.spawnedHands) {
            if (hand != null && !hand.isDead) {
               hand.setDead();
            }
         }

         this.spawnedHands.clear();
      }

      public void onInterrupt(EntityLivingBase boss, RaidInstance raid) {
         for(Entity hand : this.spawnedHands) {
            if (hand != null && !hand.isDead) {
               hand.setDead();
            }
         }

         this.spawnedHands.clear();
         this.slamLocations.clear();
      }
   }

   private static class FloweringTreesMechanic implements BossMechanic {
      private final float damage;
      private final RaidDifficulty difficulty;
      private final int safeZoneCount;
      private List<BlockPos> safeZones = new ArrayList();
      private List<BlockPos> placedBlocks = new ArrayList();
      private List<TreeData> trees = new ArrayList();
      private int treeCount;
      private boolean treesFullyGrown = false;
      private static final int GROWTH_TICKS = 60;
      private static final int MAX_TREE_HEIGHT = 12;
      private static final int MIN_TREE_HEIGHT = 6;

      public FloweringTreesMechanic(float damage, RaidDifficulty difficulty, int safeZoneCount) {
         this.damage = damage;
         this.difficulty = difficulty;
         this.safeZoneCount = safeZoneCount;
         this.treeCount = 15 + difficulty.ordinal() * 5;
      }

      public String getName() {
         return "flowering_trees";
      }

      public String getDisplayName() {
         return "Advent of Flowering Trees";
      }

      public String getWarningMessage() {
         return "Hashirama creates a forest of flowering trees!";
      }

      public int getWarningTicks() {
         return 100 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 200;
      }

      public int getSelectionWeight() {
         return 8;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public List<BlockPos> getSafeZones() {
         return this.safeZones;
      }

      public int getSafeZoneRadius() {
         return 6;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.safeZones.clear();
         this.placedBlocks.clear();
         this.trees.clear();
         this.treesFullyGrown = false;
         World world = boss.world;
         Random rand = world.rand;
         if (raid != null) {
            HashiramaMechanics.setActiveFloweringTrees(this, world, raid.getRaidId());
         } else {
            HashiramaMechanics.setActiveFloweringTrees(this, world);
         }

         for(int i = 0; i < this.safeZoneCount; ++i) {
            double angle = Math.toRadians((double)360.0F / (double)this.safeZoneCount * (double)i + (double)rand.nextInt(30));
            int distance = 15 + rand.nextInt(10);
            int x = (int)(boss.posX + (double)distance * Math.cos(angle));
            int z = (int)(boss.posZ + (double)distance * Math.sin(angle));
            this.safeZones.add(new BlockPos(x, (int)boss.posY, z));
         }

         for(int i = 0; i < this.treeCount; ++i) {
            double angle = Math.toRadians((double)360.0F / (double)this.treeCount * (double)i + (double)rand.nextInt(15));
            int distance = 8 + rand.nextInt(20);
            int x = (int)(boss.posX + (double)distance * Math.cos(angle));
            int z = (int)(boss.posZ + (double)distance * Math.sin(angle));
            BlockPos treeRoot = new BlockPos(x, (int)boss.posY, z);
            if (!this.isInSafeZone(treeRoot)) {
               treeRoot = this.findGround(world, treeRoot);
               int height = 6 + rand.nextInt(7);
               float rotation = rand.nextFloat() * 360.0F;
               int branches = 2 + rand.nextInt(3);
               this.trees.add(new TreeData(treeRoot, height, rotation, branches));
            }
         }

         System.out.println("[FloweringTrees] onStart - planned " + this.trees.size() + " trees");
         if (raid != null) {
            raid.broadcastMessage("§d§lFind the gaps in the pollen! " + this.safeZoneCount + " safe zones!");
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.HOSTILE, 3.0F, 0.5F);
      }

      private BlockPos findGround(World world, BlockPos pos) {
         for(int y = pos.getY(); y > pos.getY() - 10; --y) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (world.isAirBlock(checkPos) && !world.isAirBlock(checkPos.down())) {
               return checkPos;
            }

            if (!world.isAirBlock(checkPos)) {
               return checkPos.up();
            }
         }

         return pos;
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            World world = boss.world;
            if (ticksElapsed <= 60 && !this.treesFullyGrown) {
               float growthProgress = (float)ticksElapsed / 60.0F;
               this.growTrees(world, growthProgress);
               if (ticksElapsed % 10 == 0) {
                  world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.HOSTILE, 1.0F, 0.8F + world.rand.nextFloat() * 0.4F);
               }

               if (ticksElapsed == 60) {
                  this.treesFullyGrown = true;
                  System.out.println("[FloweringTrees] Trees fully grown, placed " + this.placedBlocks.size() + " blocks");
                  if (raid != null) {
                     raid.trackBlocks(this.placedBlocks);
                  }
               }
            }

            if (ticksElapsed % 3 == 0 && world instanceof WorldServer) {
               WorldServer ws = (WorldServer)world;

               for(int i = 0; i < 50; ++i) {
                  double x = boss.posX + (world.rand.nextDouble() - (double)0.5F) * (double)60.0F;
                  double z = boss.posZ + (world.rand.nextDouble() - (double)0.5F) * (double)60.0F;
                  BlockPos pos = new BlockPos(x, boss.posY, z);
                  if (!this.isInSafeZone(pos)) {
                     double y = boss.posY + (double)1.5F + world.rand.nextDouble() * (double)8.0F;
                     ws.spawnParticle(EnumParticleTypes.DRAGON_BREATH, x, y, z, 2, 0.3, 0.2, 0.3, 0.01, new int[0]);
                  }
               }

               for(BlockPos safeZone : this.safeZones) {
                  int safeRadius = this.getSafeZoneRadius();

                  for(int angle = 0; angle < 360; angle += 10) {
                     double rad = Math.toRadians((double)angle);
                     double px = (double)safeZone.getX() + (double)0.5F + (double)safeRadius * Math.cos(rad);
                     double pz = (double)safeZone.getZ() + (double)0.5F + (double)safeRadius * Math.sin(rad);
                     ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, px, (double)safeZone.getY() + (double)0.5F, pz, 3, 0.1, 0.3, 0.1, (double)0.0F, new int[0]);
                     ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, px, (double)safeZone.getY() + (double)1.5F, pz, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  }

                  for(int y = 0; y < 15; ++y) {
                     ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)safeZone.getX() + (double)0.5F, (double)(safeZone.getY() + y), (double)safeZone.getZ() + (double)0.5F, 2, 0.3, 0.1, 0.3, (double)0.0F, new int[0]);
                  }

                  for(int i = 0; i < 10; ++i) {
                     double offsetX = (world.rand.nextDouble() - (double)0.5F) * (double)safeRadius * (double)1.5F;
                     double offsetZ = (world.rand.nextDouble() - (double)0.5F) * (double)safeRadius * (double)1.5F;
                     double offsetY = world.rand.nextDouble() * (double)3.0F;
                     ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)safeZone.getX() + (double)0.5F + offsetX, (double)safeZone.getY() + (double)0.5F + offsetY, (double)safeZone.getZ() + (double)0.5F + offsetZ, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  }

                  double spiralAngle = Math.toRadians((double)(ticksElapsed * 10 % 360));
                  double spiralX = (double)safeZone.getX() + (double)0.5F + (double)2.0F * Math.cos(spiralAngle);
                  double spiralZ = (double)safeZone.getZ() + (double)0.5F + (double)2.0F * Math.sin(spiralAngle);
                  double spiralY = (double)safeZone.getY() + (double)(ticksElapsed % 40) / (double)4.0F;
                  ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, spiralX, spiralY, spiralZ, 3, 0.1, 0.1, 0.1, (double)0.0F, new int[0]);
               }

               if (this.treesFullyGrown) {
                  for(TreeData tree : this.trees) {
                     if (tree.grown) {
                        double spreadRadius = (double)8.0F;
                        double px = (double)tree.rootPos.getX() + (double)0.5F + (world.rand.nextDouble() - (double)0.5F) * spreadRadius * (double)2.0F;
                        double py = (double)tree.rootPos.getY() + (double)tree.targetHeight * (double)0.5F + world.rand.nextDouble() * (double)3.0F;
                        double pz = (double)tree.rootPos.getZ() + (double)0.5F + (world.rand.nextDouble() - (double)0.5F) * spreadRadius * (double)2.0F;
                        ws.spawnParticle(EnumParticleTypes.SPELL_MOB, px, py, pz, 0, (double)1.0F, (double)0.5F, 0.7, (double)1.0F, new int[0]);
                     }
                  }
               }
            }

            if (ticksElapsed % 20 == 0) {
               for(EntityPlayer player : raid.getParticipants()) {
                  if (!this.isInSafeZone(player.getPosition())) {
                     HashiramaMechanics.applyDamageWithTrueComponent(player, this.damage, DamageSource.MAGIC);
                     player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1));
                  }
               }
            }

         }
      }

      private void growTrees(World world, float progress) {
         for(TreeData tree : this.trees) {
            if (!tree.grown) {
               int currentHeight = (int)((float)tree.targetHeight * progress);
               this.growSingleTree(world, tree, currentHeight, progress >= 1.0F);
            }
         }

      }

      private void growSingleTree(World world, TreeData tree, int height, boolean addLeaves) {
         BlockPos root = tree.rootPos;

         for(int y = 0; y < height; ++y) {
            BlockPos trunkPos = root.up(y);
            if (world.isAirBlock(trunkPos) && !this.placedBlocks.contains(trunkPos)) {
               world.setBlockState(trunkPos, Blocks.LOG.getDefaultState());
               this.placedBlocks.add(trunkPos);
            }
         }

         if (height > 4) {
            int branch1Height = (int)((double)height * 0.6);
            int branch2Height = (int)((double)height * 0.8);

            for(int b = 0; b < tree.branchCount; ++b) {
               float branchAngle = tree.rotationAngle + 360.0F / (float)tree.branchCount * (float)b;
               double rad = Math.toRadians((double)branchAngle);
               if (height >= branch1Height + 2) {
                  this.growBranch(world, root.up(branch1Height), rad, 3, addLeaves);
               }

               if (height >= branch2Height + 2) {
                  this.growBranch(world, root.up(branch2Height), rad + Math.PI / (double)tree.branchCount, 2, addLeaves);
               }
            }

            if (addLeaves && height >= tree.targetHeight) {
               BlockPos topPos = root.up(height);
               this.placeLeafCluster(world, topPos, 2);
               tree.grown = true;
            }
         }

      }

      private void growBranch(World world, BlockPos start, double angleRad, int length, boolean addLeaves) {
         BlockPos current = start;

         for(int i = 1; i <= length; ++i) {
            int dx = (int)Math.round(Math.cos(angleRad) * (double)i);
            int dz = (int)Math.round(Math.sin(angleRad) * (double)i);
            int dy = (i + 1) / 2;
            BlockPos branchPos = start.add(dx, dy, dz);
            if (world.isAirBlock(branchPos) && !this.placedBlocks.contains(branchPos)) {
               world.setBlockState(branchPos, Blocks.LOG.getDefaultState());
               this.placedBlocks.add(branchPos);
               current = branchPos;
            }
         }

         if (addLeaves) {
            this.placeLeafCluster(world, current.up(), 1);
         }

      }

      private void placeLeafCluster(World world, BlockPos center, int radius) {
         IBlockState pinkLeaves = Blocks.STAINED_GLASS.getStateFromMeta(6);
         IBlockState normalLeaves = Blocks.LEAVES.getDefaultState();

         for(int dx = -radius; dx <= radius; ++dx) {
            for(int dy = -1; dy <= 1; ++dy) {
               for(int dz = -radius; dz <= radius; ++dz) {
                  if (dx * dx + dy * dy + dz * dz <= radius * radius + 1) {
                     BlockPos leafPos = center.add(dx, dy, dz);
                     if (world.isAirBlock(leafPos) && !this.placedBlocks.contains(leafPos)) {
                        boolean usePink = (dx + dy + dz) % 2 == 0 || world.rand.nextFloat() < 0.6F;
                        world.setBlockState(leafPos, usePink ? pinkLeaves : normalLeaves);
                        this.placedBlocks.add(leafPos);
                     }
                  }
               }
            }
         }

      }

      private boolean isInSafeZone(BlockPos pos) {
         for(BlockPos safeZone : this.safeZones) {
            double distSq = safeZone.distanceSq((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            if (distSq <= (double)(this.getSafeZoneRadius() * this.getSafeZoneRadius())) {
               return true;
            }
         }

         return false;
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         if (raid != null) {
            HashiramaMechanics.clearActiveFloweringTrees(raid.getRaidId());
         } else {
            HashiramaMechanics.clearActiveFloweringTrees();
         }

         this.cleanupBlocks(boss.world);
         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.HOSTILE, 2.0F, 0.5F);
         if (raid != null) {
            raid.broadcastMessage("§aThe flowering trees wither away!");
         }

      }

      public void forceCleanup(World world) {
         if (world != null) {
            this.cleanupBlocks(world);
         }

      }

      private void cleanupBlocks(World world) {
         if (world != null) {
            for(BlockPos pos : this.placedBlocks) {
               Block block = world.getBlockState(pos).getBlock();
               if (block == Blocks.LOG || block == Blocks.LEAVES || block == Blocks.STAINED_GLASS) {
                  world.setBlockToAir(pos);
                  if (world instanceof WorldServer) {
                     ((WorldServer)world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, 5, 0.2, 0.2, 0.2, 0.05, new int[]{Block.getStateId(Blocks.LOG.getDefaultState())});
                  }
               }
            }

            this.placedBlocks.clear();
            this.safeZones.clear();
            this.trees.clear();
            this.treesFullyGrown = false;
         }
      }

      private static class TreeData {
         BlockPos rootPos;
         int targetHeight;
         float rotationAngle;
         int branchCount;
         boolean grown;

         TreeData(BlockPos root, int height, float angle, int branches) {
            this.rootPos = root;
            this.targetHeight = height;
            this.rotationAngle = angle;
            this.branchCount = branches;
            this.grown = false;
         }
      }
   }

   private static class TrueSeveralThousandHandsMechanic implements BossMechanic {
      private final float damage;
      private final RaidDifficulty difficulty;
      private List<BlockPos> safeZones = new ArrayList();
      private Entity buddha1000 = null;
      private BlockPos bossStartPos;
      private int attackTimer = 0;
      private static final int ATTACK_INTERVAL = 60;
      private float lockedYaw = 0.0F;
      private boolean facingLocked = false;

      public TrueSeveralThousandHandsMechanic(float damage, RaidDifficulty difficulty) {
         this.damage = damage;
         this.difficulty = difficulty;
      }

      public String getName() {
         return "true_several_thousand_hands";
      }

      public String getDisplayName() {
         return "True Several Thousand Hands";
      }

      public String getWarningMessage() {
         return "Hashirama summons the True Several Thousand Hands!";
      }

      public int getWarningTicks() {
         return 120 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 300;
      }

      public boolean allowsDamage() {
         return false;
      }

      public int getSelectionWeight() {
         return 5;
      }

      public boolean isHeavyMechanic() {
         return true;
      }

      public List<BlockPos> getSafeZones() {
         return this.safeZones;
      }

      public int getSafeZoneRadius() {
         return 12;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.safeZones.clear();
         this.bossStartPos = boss.getPosition();
         this.attackTimer = 0;
         this.lockedYaw = boss.rotationYaw;
         this.facingLocked = true;
         if (boss instanceof EntityHashirama.EntityCustom) {
            ((EntityHashirama.EntityCustom)boss).setFacingLocked(true, this.lockedYaw);
         }

         boss.rotationYaw = this.lockedYaw;
         boss.prevRotationYaw = this.lockedYaw;
         boss.renderYawOffset = this.lockedYaw;
         boss.rotationYawHead = this.lockedYaw;
         boss.prevRotationYawHead = this.lockedYaw;
         boss.prevRenderYawOffset = this.lockedYaw;
         EntityBossBuddha1000.EntityCustom customBuddha = new EntityBossBuddha1000.EntityCustom(boss.world, boss);
         customBuddha.setPosition(boss.posX, boss.posY, boss.posZ);
         customBuddha.setBuddhaScale(1.0F);
         customBuddha.setMaxLifetime(400);
         customBuddha.rotationYaw = this.lockedYaw;
         customBuddha.prevRotationYaw = this.lockedYaw;
         customBuddha.renderYawOffset = this.lockedYaw;
         customBuddha.rotationYawHead = this.lockedYaw;
         customBuddha.setFacingLocked(true, this.lockedYaw);
         boolean spawnSuccess = boss.world.spawnEntity(customBuddha);
         if ((!spawnSuccess || customBuddha.isDead) && !boss.world.loadedEntityList.contains(customBuddha)) {
            boss.world.loadedEntityList.add(customBuddha);
            boss.world.onEntityAdded(customBuddha);
         }

         customBuddha.mountRider(boss);
         this.buddha1000 = customBuddha;
         if (raid != null) {
            raid.trackSpawnedEntity(customBuddha);
         }

         double yaw = Math.toRadians((double)(this.lockedYaw + 180.0F));
         int safeX = (int)(boss.posX + (double)25.0F * Math.sin(yaw));
         int safeZ = (int)(boss.posZ - (double)25.0F * Math.cos(yaw));
         this.safeZones.add(new BlockPos(safeX, (int)boss.posY, safeZ));
         if (raid != null) {
            raid.broadcastMessage("§6\"Wood Release: True Several Thousand Hands!\"");
            raid.broadcastMessage("§c§lDODGE! Get behind the Buddha!");
            raid.broadcastMessage("§eOnly 1 safe zone - directly behind the giant statue!");
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 3.0F, 0.5F);
         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.HOSTILE, 3.0F, 0.3F);
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (raid != null) {
            ++this.attackTimer;
            if (this.facingLocked) {
               boss.rotationYaw = this.lockedYaw;
               boss.prevRotationYaw = this.lockedYaw;
               boss.renderYawOffset = this.lockedYaw;
               boss.rotationYawHead = this.lockedYaw;
               boss.prevRotationYawHead = this.lockedYaw;
               boss.prevRenderYawOffset = this.lockedYaw;
               if (this.buddha1000 != null && !this.buddha1000.isDead) {
                  this.buddha1000.rotationYaw = this.lockedYaw;
                  this.buddha1000.prevRotationYaw = this.lockedYaw;
                  if (this.buddha1000 instanceof EntityLivingBase) {
                     ((EntityLivingBase)this.buddha1000).renderYawOffset = this.lockedYaw;
                     ((EntityLivingBase)this.buddha1000).rotationYawHead = this.lockedYaw;
                     ((EntityLivingBase)this.buddha1000).prevRotationYawHead = this.lockedYaw;
                     ((EntityLivingBase)this.buddha1000).prevRenderYawOffset = this.lockedYaw;
                  }
               }
            }

            if (this.attackTimer >= 60) {
               if (this.buddha1000 != null && !this.buddha1000.isDead && this.buddha1000 instanceof EntityBossBuddha1000.EntityCustom) {
                  EntityBossBuddha1000.EntityCustom customBuddha = (EntityBossBuddha1000.EntityCustom)this.buddha1000;
                  int armCount = 10 + this.difficulty.ordinal() * 5;
                  float armDamage = this.damage * 1.2F;
                  customBuddha.shootArmsSpread(armCount, armDamage);
                  if (raid != null) {
                     raid.broadcastMessage("§c[ATTACK] The Buddha unleashes its thousand arms!");
                  }
               }

               this.attackTimer = 0;
            }

            if (ticksElapsed % 5 == 0 && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;
               double buddhaX = this.buddha1000 != null ? this.buddha1000.posX : boss.posX;
               double buddhaY = this.buddha1000 != null ? this.buddha1000.posY : boss.posY;
               double buddhaZ = this.buddha1000 != null ? this.buddha1000.posZ : boss.posZ;

               for(int i = 0; i < 20; ++i) {
                  double angle = Math.toRadians((double)this.lockedYaw + (world.rand.nextDouble() - (double)0.5F) * (double)180.0F);
                  double distance = (double)10.0F + world.rand.nextDouble() * (double)40.0F;
                  double x = buddhaX + distance * Math.sin(angle);
                  double z = buddhaZ - distance * Math.cos(angle);
                  double y = buddhaY + world.rand.nextDouble() * (double)25.0F;
                  world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 1, (double)0.5F, (double)0.5F, (double)0.5F, 0.01, new int[0]);
               }
            }

            if (ticksElapsed % 40 == 0) {
               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 0.8F);
            }

         }
      }

      private boolean isInSafeZone(BlockPos pos) {
         for(BlockPos safeZone : this.safeZones) {
            double distSq = safeZone.distanceSq((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            if (distSq <= (double)(this.getSafeZoneRadius() * this.getSafeZoneRadius())) {
               return true;
            }
         }

         return false;
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         this.safeZones.clear();
         this.facingLocked = false;
         if (boss instanceof EntityHashirama.EntityCustom) {
            ((EntityHashirama.EntityCustom)boss).setFacingLocked(false, 0.0F);
         }

         if (boss.isRiding()) {
            boss.dismountRidingEntity();
         }

         if (this.buddha1000 != null && !this.buddha1000.isDead) {
            if (this.buddha1000 instanceof EntityBossBuddha1000.EntityCustom) {
               ((EntityBossBuddha1000.EntityCustom)this.buddha1000).despawnBuddha();
            } else {
               this.buddha1000.setDead();
            }
         }

         this.buddha1000 = null;
         if (raid != null) {
            raid.broadcastMessage("§aThe technique has ended! Damage window open!");
         }

         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 1.2F);
      }

      public void onInterrupt(EntityLivingBase boss, RaidInstance raid) {
         this.facingLocked = false;
         if (boss instanceof EntityHashirama.EntityCustom) {
            ((EntityHashirama.EntityCustom)boss).setFacingLocked(false, 0.0F);
         }

         if (boss.isRiding()) {
            boss.dismountRidingEntity();
         }

         if (this.buddha1000 != null && !this.buddha1000.isDead) {
            if (this.buddha1000 instanceof EntityBossBuddha1000.EntityCustom) {
               ((EntityBossBuddha1000.EntityCustom)this.buddha1000).despawnBuddha();
            } else {
               this.buddha1000.setDead();
            }
         }

         this.buddha1000 = null;
         this.safeZones.clear();
      }
   }

   private static class WoodHumanMechanic implements BossMechanic {
      private final RaidDifficulty difficulty;
      private EntityBossWoodGolem.EntityCustom woodGolem;
      private boolean puzzleComplete = false;
      private int damageWindowTicksRemaining = 0;
      private static final int DAMAGE_WINDOW_TICKS = 200;
      private static final float GOLEM_SCALE = 6.0F;

      public WoodHumanMechanic(RaidDifficulty difficulty) {
         this.difficulty = difficulty;
      }

      public String getName() {
         return "wood_human";
      }

      public String getDisplayName() {
         return "Wood Human";
      }

      public String getWarningMessage() {
         return "Hashirama summons the Wood Human!";
      }

      public int getWarningTicks() {
         return 80 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 800;
      }

      public boolean isPuzzleMechanic() {
         return true;
      }

      public boolean allowsDamage() {
         return this.puzzleComplete;
      }

      public int getSelectionWeight() {
         return 1;
      }

      public boolean isHeavyMechanic() {
         return true;
      }

      public boolean isPuzzleComplete(EntityLivingBase boss, RaidInstance raid) {
         return this.puzzleComplete;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.puzzleComplete = false;
         this.damageWindowTicksRemaining = 0;
         if (HashiramaMechanics.hasActiveWoodGolem(boss.world)) {
            this.puzzleComplete = true;
         } else {
            this.woodGolem = new EntityBossWoodGolem.EntityCustom(boss.world);
            double spawnX = boss.posX + (double)3.0F;
            double spawnY = boss.posY;

            double spawnZ;
            for(spawnZ = boss.posZ + (double)3.0F; boss.world.getBlockState(new BlockPos(spawnX, spawnY, spawnZ)).getMaterial().isSolid() && spawnY < boss.posY + (double)10.0F; ++spawnY) {
            }

            this.woodGolem.setPosition(spawnX, spawnY, spawnZ);
            this.woodGolem.setOwner(boss);
            this.woodGolem.setGolemScale(6.0F);
            this.woodGolem.setSpawnCenter(boss.posX, boss.posY, boss.posZ);
            this.woodGolem.rotationYaw = boss.rotationYaw;
            this.woodGolem.prevRotationYaw = boss.rotationYaw;
            this.woodGolem.renderYawOffset = boss.rotationYaw;
            this.woodGolem.setFacingLocked(false, boss.rotationYaw);
            float healthMult = this.difficulty.getHealthMultiplier();
            double maxHealth = (double)1050.0F * (double)healthMult;
            this.woodGolem.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);
            this.woodGolem.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue((double)(21.0F * this.difficulty.getDamageMultiplier()));
            this.woodGolem.setHealth((float)maxHealth);
            boolean spawnSuccess = boss.world.spawnEntity(this.woodGolem);
            if (!spawnSuccess || this.woodGolem.isDead) {
               try {
                  this.woodGolem.isDead = false;
                  if (!boss.world.loadedEntityList.contains(this.woodGolem)) {
                     if (boss.world instanceof WorldServer) {
                        WorldServer worldServer = (WorldServer)boss.world;
                        if (!worldServer.spawnEntity(this.woodGolem)) {
                           boss.world.loadedEntityList.add(this.woodGolem);
                           boss.world.onEntityAdded(this.woodGolem);
                        }
                     } else {
                        boss.world.loadedEntityList.add(this.woodGolem);
                        boss.world.onEntityAdded(this.woodGolem);
                     }
                  }

                  if (!boss.world.loadedEntityList.contains(this.woodGolem)) {
                     throw new RuntimeException("Entity still not in world after force-add");
                  }
               } catch (Exception var14) {
                  this.puzzleComplete = true;
                  return;
               }
            }

            if (this.woodGolem.isDead) {
               this.puzzleComplete = true;
            } else {
               HashiramaMechanics.setActiveWoodGolem(this.woodGolem);
               if (raid != null) {
                  raid.trackSpawnedEntity(this.woodGolem);
               }

               this.woodGolem.mountRider(boss);
               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 2.0F, 0.5F);
               if (boss.world instanceof WorldServer) {
                  ((WorldServer)boss.world).spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, boss.posX, boss.posY + (double)2.0F, boss.posZ, 3, (double)2.0F, (double)2.0F, (double)2.0F, (double)0.0F, new int[0]);
               }

               if (raid != null) {
                  raid.broadcastMessage("§c§lDestroy the Wood Human to damage Hashirama!");
               }

            }
         }
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (ticksElapsed >= 20) {
            if (this.woodGolem != null && this.woodGolem.isDead && !this.puzzleComplete) {
               this.puzzleComplete = true;
               this.damageWindowTicksRemaining = 200;
               HashiramaMechanics.clearActiveWoodGolem();
               if (boss.isRiding()) {
                  boss.dismountRidingEntity();
               }

               boss.motionY = (double)0.0F;
               boss.fallDistance = 0.0F;
               if (raid != null) {
                  raid.broadcastMessage("§a§lWood Human destroyed! Damage window open!");
               }

               boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.HOSTILE, 2.0F, 1.2F);
            }

            if (this.puzzleComplete && this.damageWindowTicksRemaining > 0) {
               --this.damageWindowTicksRemaining;
               if (this.damageWindowTicksRemaining == 60 && raid != null) {
                  raid.broadcastMessage("§e§lDamage window closing in 3 seconds!");
               }
            }

            if (this.woodGolem != null && !this.woodGolem.isDead && ticksElapsed % 10 == 0 && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;
               world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.woodGolem.posX, this.woodGolem.posY + (double)12.0F, this.woodGolem.posZ, 5, (double)6.0F, (double)12.0F, (double)6.0F, (double)0.0F, new int[0]);
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         if (this.woodGolem != null && !this.woodGolem.isDead) {
            this.woodGolem.setDead();
            if (raid != null) {
               raid.untrackSpawnedEntity(this.woodGolem);
            }
         }

         if (boss.isRiding()) {
            boss.dismountRidingEntity();
         }

         HashiramaMechanics.clearActiveWoodGolem();
         this.woodGolem = null;
      }

      public void onInterrupt(EntityLivingBase boss, RaidInstance raid) {
         this.onEnd(boss, raid);
      }
   }

   private static class WoodPrisonMechanic implements BossMechanic {
      private final RaidDifficulty difficulty;
      private EntityPlayer trappedPlayer;
      private BlockPos prisonLocation;
      private List<BlockPos> placedBlocks = new ArrayList();
      private int rescueProgress = 0;
      private static final int RESCUE_THRESHOLD = 100;
      private static final int PRISON_RADIUS = 2;
      private static final int PRISON_HEIGHT = 4;
      private static final int RESCUE_RADIUS = 3;
      private boolean playerFreed = false;

      public WoodPrisonMechanic(RaidDifficulty difficulty) {
         this.difficulty = difficulty;
      }

      public String getName() {
         return "wood_prison";
      }

      public String getDisplayName() {
         return "Four-Pillar Prison";
      }

      public String getWarningMessage() {
         return "Hashirama prepares Four-Pillar Prison!";
      }

      public int getWarningTicks() {
         return 60 - this.difficulty.getWarningTimeReduction();
      }

      public int getDurationTicks() {
         return 200;
      }

      public int getSelectionWeight() {
         return 8;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         if (raid != null) {
            this.placedBlocks.clear();
            this.rescueProgress = 0;
            this.playerFreed = false;
            HashiramaMechanics.setActiveWoodPrison(this, boss.world, raid.getRaidId());
            List<EntityPlayerMP> players = raid.getParticipants();
            if (!players.isEmpty()) {
               this.trappedPlayer = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
               this.prisonLocation = this.findSafePrisonLocation(boss.world, this.trappedPlayer.getPosition());
               this.buildPrison(boss.world);
               raid.trackBlocks(this.placedBlocks);
               raid.broadcastMessage("§c" + this.trappedPlayer.getName() + " has been trapped in a wood prison!");
               raid.broadcastMessage("§e§lTeammates: Attack the prison walls to free them!");
            }

            boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.HOSTILE, 2.0F, 0.5F);
         }
      }

      private void buildPrison(World world) {
         if (this.prisonLocation != null) {
            int[][] corners = new int[][]{{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};

            for(int[] corner : corners) {
               int groundY = this.findGroundY(world, this.prisonLocation.add(corner[0], 0, corner[1]));

               for(int y = groundY; y < this.prisonLocation.getY() + 4; ++y) {
                  BlockPos pos = new BlockPos(this.prisonLocation.getX() + corner[0], y, this.prisonLocation.getZ() + corner[1]);
                  if (world.isAirBlock(pos)) {
                     world.setBlockState(pos, Blocks.LOG.getDefaultState());
                     this.placedBlocks.add(pos);
                  }
               }
            }

            for(int x = -1; x < 2; ++x) {
               int groundY = this.findGroundY(world, this.prisonLocation.add(x, 0, -2));

               for(int y = groundY; y < this.prisonLocation.getY() + 4; ++y) {
                  BlockPos pos = new BlockPos(this.prisonLocation.getX() + x, y, this.prisonLocation.getZ() - 2);
                  if (world.isAirBlock(pos)) {
                     world.setBlockState(pos, Blocks.OAK_FENCE.getDefaultState());
                     this.placedBlocks.add(pos);
                  }
               }
            }

            for(int x = -1; x < 2; ++x) {
               int groundY = this.findGroundY(world, this.prisonLocation.add(x, 0, 2));

               for(int y = groundY; y < this.prisonLocation.getY() + 4; ++y) {
                  BlockPos pos = new BlockPos(this.prisonLocation.getX() + x, y, this.prisonLocation.getZ() + 2);
                  if (world.isAirBlock(pos)) {
                     world.setBlockState(pos, Blocks.OAK_FENCE.getDefaultState());
                     this.placedBlocks.add(pos);
                  }
               }
            }

            for(int z = -1; z < 2; ++z) {
               int groundY = this.findGroundY(world, this.prisonLocation.add(-2, 0, z));

               for(int y = groundY; y < this.prisonLocation.getY() + 4; ++y) {
                  BlockPos pos = new BlockPos(this.prisonLocation.getX() - 2, y, this.prisonLocation.getZ() + z);
                  if (world.isAirBlock(pos)) {
                     world.setBlockState(pos, Blocks.OAK_FENCE.getDefaultState());
                     this.placedBlocks.add(pos);
                  }
               }
            }

            for(int z = -1; z < 2; ++z) {
               int groundY = this.findGroundY(world, this.prisonLocation.add(2, 0, z));

               for(int y = groundY; y < this.prisonLocation.getY() + 4; ++y) {
                  BlockPos pos = new BlockPos(this.prisonLocation.getX() + 2, y, this.prisonLocation.getZ() + z);
                  if (world.isAirBlock(pos)) {
                     world.setBlockState(pos, Blocks.OAK_FENCE.getDefaultState());
                     this.placedBlocks.add(pos);
                  }
               }
            }

            for(int x = -2; x <= 2; ++x) {
               for(int z = -2; z <= 2; ++z) {
                  BlockPos pos = this.prisonLocation.add(x, 3, z);
                  if (world.isAirBlock(pos)) {
                     world.setBlockState(pos, Blocks.OAK_FENCE.getDefaultState());
                     this.placedBlocks.add(pos);
                  }

                  BlockPos pos2 = this.prisonLocation.add(x, 4, z);
                  if (world.isAirBlock(pos2)) {
                     world.setBlockState(pos2, Blocks.OAK_FENCE.getDefaultState());
                     this.placedBlocks.add(pos2);
                  }
               }
            }

         }
      }

      private int findGroundY(World world, BlockPos pos) {
         for(int y = pos.getY(); y > pos.getY() - 10; --y) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            BlockPos belowPos = checkPos.down();
            if (world.isAirBlock(checkPos) && !world.isAirBlock(belowPos)) {
               return y;
            }

            if (!world.isAirBlock(checkPos)) {
               return y + 1;
            }
         }

         return pos.getY();
      }

      private BlockPos findSafePrisonLocation(World world, BlockPos pos) {
         int x = pos.getX();
         int z = pos.getZ();

         for(int y = pos.getY(); y < pos.getY() + 10 && y < 255; ++y) {
            boolean feetClear = !world.getBlockState(new BlockPos(x, y, z)).getMaterial().isSolid();
            boolean headClear = !world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial().isSolid();
            boolean hasFloor = world.getBlockState(new BlockPos(x, y - 1, z)).getMaterial().isSolid();
            if (feetClear && headClear && hasFloor) {
               return new BlockPos(x, y, z);
            }
         }

         return pos;
      }

      private void removePrison(World world) {
         for(BlockPos pos : this.placedBlocks) {
            if (world.getBlockState(pos).getBlock() == Blocks.LOG || world.getBlockState(pos).getBlock() == Blocks.OAK_FENCE) {
               world.setBlockToAir(pos);
               if (world instanceof WorldServer) {
                  ((WorldServer)world).spawnParticle(EnumParticleTypes.CLOUD, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, 3, 0.2, 0.2, 0.2, 0.02, new int[0]);
               }
            }
         }

         this.placedBlocks.clear();
      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (this.trappedPlayer != null && !this.trappedPlayer.isDead && !this.playerFreed) {
            if (this.trappedPlayer instanceof EntityPlayerMP) {
               ((EntityPlayerMP)this.trappedPlayer).connection.setPlayerLocation((double)this.prisonLocation.getX() + (double)0.5F, (double)this.prisonLocation.getY(), (double)this.prisonLocation.getZ() + (double)0.5F, this.trappedPlayer.rotationYaw, this.trappedPlayer.rotationPitch);
            } else {
               this.trappedPlayer.setPositionAndUpdate((double)this.prisonLocation.getX() + (double)0.5F, (double)this.prisonLocation.getY(), (double)this.prisonLocation.getZ() + (double)0.5F);
            }

            this.trappedPlayer.motionX = (double)0.0F;
            this.trappedPlayer.motionY = (double)0.0F;
            this.trappedPlayer.motionZ = (double)0.0F;
            this.trappedPlayer.velocityChanged = true;
            if (raid != null) {
               for(EntityPlayerMP player : raid.getParticipants()) {
                  if (player != this.trappedPlayer && !player.isDead) {
                     double dist = player.getDistance((double)this.prisonLocation.getX(), (double)this.prisonLocation.getY(), (double)this.prisonLocation.getZ());
                     if (dist <= (double)5.0F && player.isSwingInProgress) {
                        this.rescueProgress += 5;
                        if (boss.world instanceof WorldServer) {
                           WorldServer ws = (WorldServer)boss.world;
                           ws.spawnParticle(EnumParticleTypes.CRIT, (double)this.prisonLocation.getX() + (double)0.5F, (double)this.prisonLocation.getY() + (double)1.5F, (double)this.prisonLocation.getZ() + (double)0.5F, 5, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
                        }

                        boss.world.playSound((EntityPlayer)null, this.prisonLocation, SoundEvents.BLOCK_WOOD_HIT, SoundCategory.BLOCKS, 0.8F, 1.0F + boss.world.rand.nextFloat() * 0.2F);
                        int progressPercent = this.rescueProgress * 100 / 100;
                        if (this.rescueProgress == 25 || this.rescueProgress == 50 || this.rescueProgress == 75) {
                           raid.broadcastMessage("§e[RESCUE] Prison integrity: " + (100 - progressPercent) + "%");
                        }
                     }
                  }
               }

               if (this.rescueProgress >= 100) {
                  this.playerFreed = true;
                  raid.broadcastMessage("§a§l" + this.trappedPlayer.getName() + " has been rescued by teammates!");
                  boss.world.playSound((EntityPlayer)null, this.prisonLocation, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 2.0F, 0.8F);
                  this.removePrison(boss.world);
                  if (boss.world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)boss.world;
                     ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)this.prisonLocation.getX() + (double)0.5F, (double)(this.prisonLocation.getY() + 1), (double)this.prisonLocation.getZ() + (double)0.5F, 30, (double)1.0F, (double)1.0F, (double)1.0F, 0.1, new int[0]);
                  }

                  return;
               }
            }

            if (ticksElapsed % 5 == 0 && boss.world instanceof WorldServer) {
               WorldServer world = (WorldServer)boss.world;
               EnumParticleTypes particleType = EnumParticleTypes.VILLAGER_HAPPY;
               if (this.rescueProgress > 75) {
                  particleType = EnumParticleTypes.FLAME;
               } else if (this.rescueProgress > 50) {
                  particleType = EnumParticleTypes.CRIT;
               }

               for(int angle = 0; angle < 360; angle += 90) {
                  double rad = Math.toRadians((double)angle);
                  double x = (double)this.prisonLocation.getX() + (double)0.5F + (double)2.0F * Math.cos(rad);
                  double z = (double)this.prisonLocation.getZ() + (double)0.5F + (double)2.0F * Math.sin(rad);

                  for(int yOffset = 0; yOffset < 4; ++yOffset) {
                     world.spawnParticle(particleType, x, (double)(this.prisonLocation.getY() + yOffset), z, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
                  }
               }
            }

            if (ticksElapsed % 40 == 0) {
               float damage = 8.0F * this.difficulty.getDamageMultiplier();
               HashiramaMechanics.applyDamageWithTrueComponent(this.trappedPlayer, damage, DamageSource.MAGIC);
            }

         }
      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         if (raid != null) {
            HashiramaMechanics.clearActiveWoodPrison(raid.getRaidId());
         } else {
            HashiramaMechanics.clearActiveWoodPrison();
         }

         if (this.trappedPlayer != null && raid != null && !this.playerFreed) {
            raid.broadcastMessage("§a" + this.trappedPlayer.getName() + " has been freed from the wood prison!");
         }

         if (!this.playerFreed) {
            this.removePrison(boss.world);
            if (this.prisonLocation != null) {
               boss.world.playSound((EntityPlayer)null, this.prisonLocation, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1.5F, 0.8F);
            }
         }

         this.trappedPlayer = null;
         this.prisonLocation = null;
         this.playerFreed = false;
         this.rescueProgress = 0;
      }

      public void onInterrupt(EntityLivingBase boss, RaidInstance raid) {
         if (raid != null) {
            HashiramaMechanics.clearActiveWoodPrison(raid.getRaidId());
         } else {
            HashiramaMechanics.clearActiveWoodPrison();
         }

         if (boss.world != null) {
            this.removePrison(boss.world);
         }

         this.trappedPlayer = null;
         this.prisonLocation = null;
      }

      public void forceCleanup(World world) {
         if (world != null) {
            this.removePrison(world);
         }

         this.trappedPlayer = null;
         this.prisonLocation = null;
      }
   }

   private static class WoodSpearBarrageMechanic implements BossMechanic {
      private final float damage;
      private final int spearCount;
      private final float spreadAngle;
      private final int cooldown;
      private final RaidDifficulty difficulty;
      private int ticksSinceLastBarrage = 0;
      private int chargeUpTicks = 0;
      private static final int CHARGE_UP_TIME = 20;
      private boolean isCharging = false;
      private EntityPlayer targetPlayer = null;

      public WoodSpearBarrageMechanic(float damage, int spearCount, float spreadAngle, int cooldown, RaidDifficulty difficulty) {
         this.damage = damage;
         this.spearCount = spearCount;
         this.spreadAngle = spreadAngle;
         this.cooldown = cooldown;
         this.difficulty = difficulty;
      }

      public String getName() {
         return "wood_spear_barrage";
      }

      public String getDisplayName() {
         return "Wood Spear Barrage";
      }

      public String getWarningMessage() {
         return "§6Hashirama prepares Wood Release: Thrusting Spears!";
      }

      public int getWarningTicks() {
         return 20;
      }

      public int getDurationTicks() {
         return 30;
      }

      public int getSelectionWeight() {
         return 12;
      }

      public boolean isHeavyMechanic() {
         return false;
      }

      public void onStart(EntityLivingBase boss, RaidInstance raid) {
         this.isCharging = true;
         this.chargeUpTicks = 0;
         this.targetPlayer = null;
         if (raid != null) {
            List<EntityPlayerMP> players = raid.getParticipants();
            if (!players.isEmpty()) {
               this.targetPlayer = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
            }
         }

      }

      public void onTick(EntityLivingBase boss, RaidInstance raid, int ticksElapsed) {
         if (!boss.world.isRemote && raid != null) {
            if (this.isCharging) {
               ++this.chargeUpTicks;
               if (this.chargeUpTicks >= 20) {
                  this.fireSpearBarrage(boss, raid);
                  this.isCharging = false;
               }
            }

         }
      }

      private void fireSpearBarrage(EntityLivingBase boss, RaidInstance raid) {
         if (this.targetPlayer == null || this.targetPlayer.isDead) {
            List<EntityPlayerMP> players = raid.getParticipants();
            if (players.isEmpty()) {
               return;
            }

            this.targetPlayer = (EntityPlayer)players.get(boss.world.rand.nextInt(players.size()));
         }

         Vec3d bossPos = new Vec3d(boss.posX, boss.posY + (double)boss.getEyeHeight(), boss.posZ);
         Vec3d targetPos = this.targetPlayer.getPositionVector().add((double)0.0F, (double)(this.targetPlayer.height / 2.0F), (double)0.0F);
         EntityWoodSpear.EntityCustom.spawnShotgunSpread(boss.world, boss, bossPos, targetPos, this.spearCount, this.spreadAngle, 2.0F, this.damage, 0.8F + boss.world.rand.nextFloat() * 0.4F);
         raid.broadcastMessage("§6Wood Release: Cutting Spears!");
         boss.world.playSound((EntityPlayer)null, boss.getPosition(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.HOSTILE, 2.0F, 0.5F);
         if (boss.world instanceof WorldServer) {
            WorldServer ws = (WorldServer)boss.world;
            ws.spawnParticle(EnumParticleTypes.CLOUD, boss.posX, boss.posY + (double)1.5F, boss.posZ, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
         }

      }

      public void onEnd(EntityLivingBase boss, RaidInstance raid) {
         this.isCharging = false;
         this.chargeUpTicks = 0;
         this.targetPlayer = null;
      }

      public void onInterrupt(EntityLivingBase boss, RaidInstance raid) {
         this.onEnd(boss, raid);
      }

      public List<BlockPos> getSafeZones() {
         return new ArrayList();
      }

      public boolean allowsDamage() {
         return true;
      }
   }
}
