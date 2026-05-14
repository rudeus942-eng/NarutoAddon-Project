package net.luck.narutoaddon.OtherCode.endgame.defense;

import net.minecraft.util.math.BlockPos;

import java.util.*;

public class DefenseDefinition {
   private static final Map<String, DefenseDefinition> REGISTRY = new HashMap();
   private final String villageName;
   private final BlockPos centerPos;
   private final int centerRadius;
   private final List<DefenseSpawnPoint> spawnPoints;
   private final int villageMaxHP;

   public DefenseDefinition(String villageName, BlockPos centerPos, int centerRadius, List<DefenseSpawnPoint> spawnPoints, int villageMaxHP) {
      this.villageName = villageName;
      this.centerPos = centerPos;
      this.centerRadius = centerRadius;
      this.spawnPoints = Collections.unmodifiableList(new ArrayList(spawnPoints));
      this.villageMaxHP = villageMaxHP;
   }

   public String getVillageName() {
      return this.villageName;
   }

   public BlockPos getCenterPos() {
      return this.centerPos;
   }

   public int getCenterRadius() {
      return this.centerRadius;
   }

   public List<DefenseSpawnPoint> getSpawnPoints() {
      return this.spawnPoints;
   }

   public int getVillageMaxHP() {
      return this.villageMaxHP;
   }

   public static void init() {
      REGISTRY.clear();
      register(new DefenseDefinition("LEAF", new BlockPos(-947, 65, -843), 30, Arrays.asList(new DefenseSpawnPoint("Main Gate", new BlockPos(-1100, 65, -843), calcYaw(-1100, -843, -947, -843)), new DefenseSpawnPoint("Training Grounds", new BlockPos(-947, 65, -1100), calcYaw(-947, -1100, -947, -843)), new DefenseSpawnPoint("Forest Path", new BlockPos(-750, 65, -700), calcYaw(-750, -700, -947, -843))), 100));
      register(new DefenseDefinition("SAND", new BlockPos(-2734, 65, 520), 30, Arrays.asList(new DefenseSpawnPoint("Desert Gate", new BlockPos(-2900, 65, 520), calcYaw(-2900, 520, -2734, 520)), new DefenseSpawnPoint("Canyon Wall", new BlockPos(-2734, 65, 350), calcYaw(-2734, 350, -2734, 520)), new DefenseSpawnPoint("Oasis Path", new BlockPos(-2550, 65, 650), calcYaw(-2550, 650, -2734, 520))), 100));
      register(new DefenseDefinition("MIST", new BlockPos(3861, 65, -2155), 30, Arrays.asList(new DefenseSpawnPoint("Harbor Gate", new BlockPos(3700, 65, -2155), calcYaw(3700, -2155, 3861, -2155)), new DefenseSpawnPoint("Cliff Path", new BlockPos(3861, 65, -2350), calcYaw(3861, -2350, 3861, -2155)), new DefenseSpawnPoint("Beach Approach", new BlockPos(4050, 65, -2000), calcYaw(4050, -2000, 3861, -2155))), 100));
      register(new DefenseDefinition("STONE", new BlockPos(-2500, 65, -2655), 30, Arrays.asList(new DefenseSpawnPoint("Mountain Gate", new BlockPos(-2700, 65, -2655), calcYaw(-2700, -2655, -2500, -2655)), new DefenseSpawnPoint("Cliff Side", new BlockPos(-2500, 65, -2850), calcYaw(-2500, -2850, -2500, -2655)), new DefenseSpawnPoint("Valley Path", new BlockPos(-2300, 65, -2500), calcYaw(-2300, -2500, -2500, -2655))), 100));
      register(new DefenseDefinition("CLOUD", new BlockPos(1912, 65, -3066), 30, Arrays.asList(new DefenseSpawnPoint("Highland Gate", new BlockPos(1700, 65, -3066), calcYaw(1700, -3066, 1912, -3066)), new DefenseSpawnPoint("Ridge Path", new BlockPos(1912, 65, -3250), calcYaw(1912, -3250, 1912, -3066)), new DefenseSpawnPoint("Eastern Trail", new BlockPos(2100, 65, -2900), calcYaw(2100, -2900, 1912, -3066))), 100));
      register(new DefenseDefinition("RAIN", new BlockPos(-2249, 65, -828), 30, Arrays.asList(new DefenseSpawnPoint("Industrial Gate", new BlockPos(-2400, 65, -828), calcYaw(-2400, -828, -2249, -828)), new DefenseSpawnPoint("Pipe Network", new BlockPos(-2249, 65, -1000), calcYaw(-2249, -1000, -2249, -828)), new DefenseSpawnPoint("Canal Approach", new BlockPos(-2050, 65, -650), calcYaw(-2050, -650, -2249, -828))), 100));
   }

   private static void register(DefenseDefinition def) {
      REGISTRY.put(def.villageName, def);
   }

   public static DefenseDefinition get(String villageName) {
      return (DefenseDefinition)REGISTRY.get(villageName);
   }

   public static Collection<DefenseDefinition> getAll() {
      return REGISTRY.values();
   }

   private static float calcYaw(int fromX, int fromZ, int toX, int toZ) {
      double dx = (double)(toX - fromX);
      double dz = (double)(toZ - fromZ);
      return (float)(Math.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
   }
}
