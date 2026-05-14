
package net.luck.narutoaddon.OtherCode.quest.pvp.war;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.UUID;

public class InfiltrationObjective {
   private final ObjectiveType type;
   private final BlockPos location;
   private ObjectiveStatus status;
   private UUID channelingPlayer;
   private int channelProgress;
   private UUID assassinationTarget;

   public InfiltrationObjective(ObjectiveType type, BlockPos location) {
      this.type = type;
      this.location = location;
      this.status = ObjectiveStatus.PENDING;
      this.channelingPlayer = null;
      this.channelProgress = 0;
      this.assassinationTarget = null;
   }

   public ObjectiveType getType() {
      return this.type;
   }

   public BlockPos getLocation() {
      return this.location;
   }

   public ObjectiveStatus getStatus() {
      return this.status;
   }

   public UUID getChannelingPlayer() {
      return this.channelingPlayer;
   }

   public int getChannelProgress() {
      return this.channelProgress;
   }

   public UUID getAssassinationTarget() {
      return this.assassinationTarget;
   }

   public void setAssassinationTarget(UUID targetUUID) {
      this.assassinationTarget = targetUUID;
   }

   public boolean isComplete() {
      return this.status == ObjectiveStatus.COMPLETED;
   }

   public boolean isPending() {
      return this.status == ObjectiveStatus.PENDING || this.status == ObjectiveStatus.CHANNELING;
   }

   public boolean startChannel(EntityPlayerMP player) {
      if (this.status != ObjectiveStatus.PENDING) {
         return false;
      } else if (this.type == ObjectiveType.ASSASSINATE) {
         return false;
      } else {
         double dist = player.getDistance((double)this.location.getX() + (double)0.5F, (double)this.location.getY(), (double)this.location.getZ() + (double)0.5F);
         if (dist > (double)3.5F) {
            return false;
         } else {
            this.channelingPlayer = player.getUniqueID();
            this.channelProgress = 0;
            this.status = ObjectiveStatus.CHANNELING;
            return true;
         }
      }
   }

   public boolean tickChannel(World world, EntityPlayerMP player) {
      if (this.status != ObjectiveStatus.CHANNELING) {
         return false;
      } else if (this.channelingPlayer != null && this.channelingPlayer.equals(player.getUniqueID())) {
         if (!player.isDead && !(player.getHealth() <= 0.0F)) {
            double dist = player.getDistance((double)this.location.getX() + (double)0.5F, (double)this.location.getY(), (double)this.location.getZ() + (double)0.5F);
            if (dist > (double)5.0F) {
               this.interruptChannel();
               return false;
            } else {
               ++this.channelProgress;
               if (this.channelProgress % 10 == 0 && world instanceof WorldServer) {
                  WorldServer ws = (WorldServer)world;
                  EnumParticleTypes particle = this.type == ObjectiveType.STEAL_INTEL ? EnumParticleTypes.ENCHANTMENT_TABLE : EnumParticleTypes.FLAME;
                  ws.spawnParticle(particle, (double)this.location.getX() + (double)0.5F, (double)this.location.getY() + (double)1.0F, (double)this.location.getZ() + (double)0.5F, 5, 0.3, 0.3, 0.3, 0.02, new int[0]);
               }

               if (this.channelProgress >= this.type.channelTicks) {
                  this.status = ObjectiveStatus.COMPLETED;
                  this.channelingPlayer = null;
                  if (world instanceof WorldServer) {
                     WorldServer ws = (WorldServer)world;
                     ws.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, (double)this.location.getX() + (double)0.5F, (double)this.location.getY() + (double)1.5F, (double)this.location.getZ() + (double)0.5F, 20, (double)0.5F, (double)0.5F, (double)0.5F, 0.1, new int[0]);
                  }

                  return true;
               } else {
                  return false;
               }
            }
         } else {
            this.interruptChannel();
            return false;
         }
      } else {
         return false;
      }
   }

   public void interruptChannel() {
      if (this.status == ObjectiveStatus.CHANNELING) {
         this.status = ObjectiveStatus.PENDING;
         this.channelingPlayer = null;
         this.channelProgress = 0;
      }

   }

   public void markAssassinationComplete() {
      if (this.type == ObjectiveType.ASSASSINATE && this.status != ObjectiveStatus.COMPLETED) {
         this.status = ObjectiveStatus.COMPLETED;
      }

   }

   public float getChannelPercent() {
      return this.type.channelTicks <= 0 ? 0.0F : (float)this.channelProgress / (float)this.type.channelTicks;
   }

   public NBTTagCompound writeToNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setInteger("type", this.type.ordinal());
      nbt.setInteger("x", this.location.getX());
      nbt.setInteger("y", this.location.getY());
      nbt.setInteger("z", this.location.getZ());
      nbt.setInteger("status", this.status.ordinal());
      nbt.setInteger("channelProgress", this.channelProgress);
      if (this.channelingPlayer != null) {
         nbt.setString("channelingPlayer", this.channelingPlayer.toString());
      }

      if (this.assassinationTarget != null) {
         nbt.setString("assassinationTarget", this.assassinationTarget.toString());
      }

      return nbt;
   }

   public static InfiltrationObjective readFromNBT(NBTTagCompound nbt) {
      ObjectiveType type = ObjectiveType.values()[nbt.getInteger("type")];
      BlockPos pos = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
      InfiltrationObjective obj = new InfiltrationObjective(type, pos);
      obj.status = ObjectiveStatus.values()[nbt.getInteger("status")];
      obj.channelProgress = nbt.getInteger("channelProgress");
      if (nbt.hasKey("channelingPlayer")) {
         try {
            obj.channelingPlayer = UUID.fromString(nbt.getString("channelingPlayer"));
         } catch (IllegalArgumentException var6) {
         }
      }

      if (nbt.hasKey("assassinationTarget")) {
         try {
            obj.assassinationTarget = UUID.fromString(nbt.getString("assassinationTarget"));
         } catch (IllegalArgumentException var5) {
         }
      }

      return obj;
   }

   public static enum ObjectiveType {
      STEAL_INTEL("Steal Intel Scroll", 200, 10),
      PLANT_EXPLOSIVE("Plant Explosive Tag", 160, 10),
      ASSASSINATE("Assassinate Target", 0, 10);

      public final String displayName;
      public final int channelTicks;
      public final int points;

      private ObjectiveType(String displayName, int channelTicks, int points) {
         this.displayName = displayName;
         this.channelTicks = channelTicks;
         this.points = points;
      }
   }

   public static enum ObjectiveStatus {
      PENDING,
      CHANNELING,
      COMPLETED,
      FAILED;
   }
}
