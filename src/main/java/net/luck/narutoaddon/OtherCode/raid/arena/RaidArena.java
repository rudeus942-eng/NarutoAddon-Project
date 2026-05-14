
package net.luck.narutoaddon.OtherCode.raid.arena;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class RaidArena {
   private int arenaId;
   private String name;
   private String bossId;
   private String difficulty;
   private BlockPos corner1;
   private BlockPos corner2;
   private AxisAlignedBB bounds;
   private List<BlockPos> playerSpawns = new ArrayList();
   private BlockPos bossSpawn;
   private boolean enabled = true;
   private boolean inUse = false;

   public RaidArena(int arenaId, String name, String bossId, BlockPos corner1, BlockPos corner2) {
      this.arenaId = arenaId;
      this.name = name;
      this.bossId = bossId;
      this.setCorners(corner1, corner2);
   }

   private RaidArena() {
   }

   public void setCorners(BlockPos c1, BlockPos c2) {
      int minX = Math.min(c1.getX(), c2.getX());
      int minY = Math.min(c1.getY(), c2.getY());
      int minZ = Math.min(c1.getZ(), c2.getZ());
      int maxX = Math.max(c1.getX(), c2.getX());
      int maxY = Math.max(c1.getY(), c2.getY());
      int maxZ = Math.max(c1.getZ(), c2.getZ());
      this.corner1 = new BlockPos(minX, minY, minZ);
      this.corner2 = new BlockPos(maxX, maxY, maxZ);
      this.bounds = new AxisAlignedBB((double)minX, (double)minY, (double)minZ, (double)(maxX + 1), (double)(maxY + 1), (double)(maxZ + 1));
   }

   public boolean isInBounds(BlockPos pos) {
      return this.bounds.contains(new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F));
   }

   public boolean isInBounds(double x, double y, double z) {
      return this.bounds.contains(new Vec3d(x, y, z));
   }

   public void setPlayerSpawn(int index, BlockPos pos) {
      while(this.playerSpawns.size() <= index) {
         this.playerSpawns.add((Object)null);
      }

      this.playerSpawns.set(index, pos);
   }

   public BlockPos getPlayerSpawn(int index) {
      if (index >= 0 && index < this.playerSpawns.size()) {
         return (BlockPos)this.playerSpawns.get(index);
      } else {
         return !this.playerSpawns.isEmpty() && this.playerSpawns.get(0) != null ? (BlockPos)this.playerSpawns.get(0) : this.getCenter();
      }
   }

   public List<BlockPos> getPlayerSpawns() {
      return new ArrayList(this.playerSpawns);
   }

   public int getConfiguredSpawnCount() {
      int count = 0;

      for(BlockPos spawn : this.playerSpawns) {
         if (spawn != null) {
            ++count;
         }
      }

      return count;
   }

   public void setBossSpawn(BlockPos pos) {
      this.bossSpawn = pos;
   }

   public BlockPos getBossSpawn() {
      return this.bossSpawn != null ? this.bossSpawn : this.getCenter();
   }

   public BlockPos getCenter() {
      int x = (this.corner1.getX() + this.corner2.getX()) / 2;
      int y = this.corner1.getY();
      int z = (this.corner1.getZ() + this.corner2.getZ()) / 2;
      return new BlockPos(x, y, z);
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isInUse() {
      return this.inUse;
   }

   public void setInUse(boolean inUse) {
      this.inUse = inUse;
   }

   public boolean isAvailable() {
      return this.enabled && !this.inUse;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound tag = new NBTTagCompound();
      tag.setInteger("arenaId", this.arenaId);
      tag.setString("name", this.name);
      tag.setString("bossId", this.bossId);
      tag.setBoolean("enabled", this.enabled);
      if (this.difficulty != null) {
         tag.setString("difficulty", this.difficulty);
      }

      tag.setInteger("c1x", this.corner1.getX());
      tag.setInteger("c1y", this.corner1.getY());
      tag.setInteger("c1z", this.corner1.getZ());
      tag.setInteger("c2x", this.corner2.getX());
      tag.setInteger("c2y", this.corner2.getY());
      tag.setInteger("c2z", this.corner2.getZ());
      NBTTagList spawnList = new NBTTagList();

      for(BlockPos spawn : this.playerSpawns) {
         NBTTagCompound spawnTag = new NBTTagCompound();
         if (spawn != null) {
            spawnTag.setBoolean("set", true);
            spawnTag.setInteger("x", spawn.getX());
            spawnTag.setInteger("y", spawn.getY());
            spawnTag.setInteger("z", spawn.getZ());
         } else {
            spawnTag.setBoolean("set", false);
         }

         spawnList.appendTag(spawnTag);
      }

      tag.setTag("playerSpawns", spawnList);
      if (this.bossSpawn != null) {
         tag.setBoolean("bossSpawnSet", true);
         tag.setInteger("bsx", this.bossSpawn.getX());
         tag.setInteger("bsy", this.bossSpawn.getY());
         tag.setInteger("bsz", this.bossSpawn.getZ());
      } else {
         tag.setBoolean("bossSpawnSet", false);
      }

      return tag;
   }

   public static RaidArena readFromNBT(NBTTagCompound tag) {
      RaidArena arena = new RaidArena();
      arena.arenaId = tag.getInteger("arenaId");
      arena.name = tag.getString("name");
      arena.bossId = tag.getString("bossId");
      arena.enabled = tag.getBoolean("enabled");
      if (tag.hasKey("difficulty")) {
         arena.difficulty = tag.getString("difficulty");
      }

      BlockPos c1 = new BlockPos(tag.getInteger("c1x"), tag.getInteger("c1y"), tag.getInteger("c1z"));
      BlockPos c2 = new BlockPos(tag.getInteger("c2x"), tag.getInteger("c2y"), tag.getInteger("c2z"));
      arena.setCorners(c1, c2);
      NBTTagList spawnList = tag.getTagList("playerSpawns", 10);

      for(int i = 0; i < spawnList.tagCount(); ++i) {
         NBTTagCompound spawnTag = spawnList.getCompoundTagAt(i);
         if (spawnTag.getBoolean("set")) {
            arena.playerSpawns.add(new BlockPos(spawnTag.getInteger("x"), spawnTag.getInteger("y"), spawnTag.getInteger("z")));
         } else {
            arena.playerSpawns.add((Object)null);
         }
      }

      if (tag.getBoolean("bossSpawnSet")) {
         arena.bossSpawn = new BlockPos(tag.getInteger("bsx"), tag.getInteger("bsy"), tag.getInteger("bsz"));
      }

      return arena;
   }

   public int getArenaId() {
      return this.arenaId;
   }

   public String getName() {
      return this.name;
   }

   public String getBossId() {
      return this.bossId;
   }

   public BlockPos getCorner1() {
      return this.corner1;
   }

   public BlockPos getCorner2() {
      return this.corner2;
   }

   public AxisAlignedBB getBounds() {
      return this.bounds;
   }

   public String getDifficulty() {
      return this.difficulty;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setBossId(String bossId) {
      this.bossId = bossId;
   }

   public void setDifficulty(String difficulty) {
      this.difficulty = difficulty;
   }
}
