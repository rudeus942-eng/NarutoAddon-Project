
package net.luck.narutoaddon.OtherCode.jutsu.domain;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

import java.util.UUID;

public class DomainInstance {
   public static final int DEFAULT_WALL_HP = 5000;
   private final UUID id;
   private final UUID ownerUUID;
   private final DomainType type;
   private final BlockPos center;
   private final int radius;
   private final long startTime;
   private long expirationTime;
   private DomainPhase phase;
   private int cutsceneTick;
   private int wallHP;
   private final int maxWallHP;

   public DomainInstance(UUID ownerUUID, DomainType type, BlockPos center, int radius, long startTime, int durationTicks) {
      this.id = UUID.randomUUID();
      this.ownerUUID = ownerUUID;
      this.type = type;
      this.center = center;
      this.radius = radius;
      this.startTime = startTime;
      this.expirationTime = startTime + (long)durationTicks;
      this.phase = DomainPhase.CUTSCENE;
      this.cutsceneTick = 0;
      this.maxWallHP = 5000;
      this.wallHP = 5000;
   }

   private DomainInstance(UUID id, UUID ownerUUID, DomainType type, BlockPos center, int radius, long startTime, long expirationTime, DomainPhase phase, int wallHP, int maxWallHP) {
      this.id = id;
      this.ownerUUID = ownerUUID;
      this.type = type;
      this.center = center;
      this.radius = radius;
      this.startTime = startTime;
      this.expirationTime = expirationTime;
      this.phase = phase;
      this.cutsceneTick = 0;
      this.maxWallHP = maxWallHP;
      this.wallHP = wallHP;
   }

   public UUID getId() {
      return this.id;
   }

   public UUID getOwnerUUID() {
      return this.ownerUUID;
   }

   public DomainType getType() {
      return this.type;
   }

   public BlockPos getCenter() {
      return this.center;
   }

   public int getRadius() {
      return this.radius;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public long getExpirationTime() {
      return this.expirationTime;
   }

   public DomainPhase getPhase() {
      return this.phase;
   }

   public int getCutsceneTick() {
      return this.cutsceneTick;
   }

   public Vec3d getCenterVec() {
      return new Vec3d((double)this.center.getX() + (double)0.5F, (double)this.center.getY(), (double)this.center.getZ() + (double)0.5F);
   }

   public int getRemainingTicks(long currentTime) {
      return (int)Math.max(0L, this.expirationTime - currentTime);
   }

   public boolean isExpired(long currentTime) {
      return currentTime >= this.expirationTime;
   }

   public void setPhase(DomainPhase phase) {
      this.phase = phase;
   }

   public void incrementCutsceneTick() {
      ++this.cutsceneTick;
   }

   public int getWallHP() {
      return this.wallHP;
   }

   public int getMaxWallHP() {
      return this.maxWallHP;
   }

   public void damageWall(int amount) {
      this.wallHP = Math.max(0, Math.min(this.maxWallHP, this.wallHP - amount));
   }

   public void forceCollapse(long currentTime) {
      this.expirationTime = Math.min(this.expirationTime, currentTime);
   }

   public void activate(WorldServer world) {
      DomainSavedData.get(world).addDomain(this);
      DomainBlockManager.buildDomain(world, this);
   }

   public void cleanup(WorldServer world) {
      DomainBlockManager.restoreDomain(world, this);
      DomainSavedData.get(world).removeDomain(this.id);
   }

   public boolean isInsideDomain(Vec3d position) {
      double dx = position.x - ((double)this.center.getX() + (double)0.5F);
      double dy = position.y - (double)this.center.getY();
      double dz = position.z - ((double)this.center.getZ() + (double)0.5F);
      return dx * dx + dy * dy + dz * dz <= (double)(this.radius * this.radius);
   }

   public boolean isInsideDomain(BlockPos pos) {
      return this.isInsideDomain(new Vec3d((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F));
   }

   public double getDistanceFromCenter(Vec3d position) {
      double dx = position.x - ((double)this.center.getX() + (double)0.5F);
      double dy = position.y - (double)this.center.getY();
      double dz = position.z - ((double)this.center.getZ() + (double)0.5F);
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   public NBTTagCompound serializeNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("id", this.id.toString());
      nbt.setString("owner", this.ownerUUID.toString());
      nbt.setString("type", this.type.getId());
      nbt.setInteger("cx", this.center.getX());
      nbt.setInteger("cy", this.center.getY());
      nbt.setInteger("cz", this.center.getZ());
      nbt.setInteger("radius", this.radius);
      nbt.setLong("startTime", this.startTime);
      nbt.setLong("expirationTime", this.expirationTime);
      nbt.setString("phase", this.phase.name());
      nbt.setInteger("wallHP", this.wallHP);
      nbt.setInteger("maxWallHP", this.maxWallHP);
      return nbt;
   }

   public static DomainInstance deserializeNBT(NBTTagCompound nbt) {
      UUID id = UUID.fromString(nbt.getString("id"));
      UUID owner = UUID.fromString(nbt.getString("owner"));
      DomainType type = DomainType.fromId(nbt.getString("type"));
      BlockPos center = new BlockPos(nbt.getInteger("cx"), nbt.getInteger("cy"), nbt.getInteger("cz"));
      int radius = nbt.getInteger("radius");
      long startTime = nbt.getLong("startTime");
      long expirationTime = nbt.getLong("expirationTime");

      DomainPhase phase;
      try {
         phase = DomainPhase.valueOf(nbt.getString("phase"));
      } catch (IllegalArgumentException var13) {
         phase = DomainPhase.ACTIVE;
      }

      int maxWallHP = nbt.hasKey("maxWallHP") ? nbt.getInteger("maxWallHP") : 5000;
      int wallHP = nbt.hasKey("wallHP") ? nbt.getInteger("wallHP") : maxWallHP;
      return new DomainInstance(id, owner, type, center, radius, startTime, expirationTime, phase, wallHP, maxWallHP);
   }

   public static enum DomainPhase {
      CUTSCENE,
      ACTIVE,
      CRUMBLING,
      ENDED;
   }
}
