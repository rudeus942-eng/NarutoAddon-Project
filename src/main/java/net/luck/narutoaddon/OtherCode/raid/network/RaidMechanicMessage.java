
package net.luck.narutoaddon.OtherCode.raid.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class RaidMechanicMessage implements IMessage {
   public static final int ACTION_MECHANIC_WARNING = 0;
   public static final int ACTION_MECHANIC_START = 1;
   public static final int ACTION_MECHANIC_END = 2;
   public static final int ACTION_SAFE_ZONE_UPDATE = 3;
   public static final int ACTION_DANGER_ZONE_UPDATE = 4;
   public static final int ACTION_PUZZLE_UPDATE = 5;
   public static final int ACTION_CLEAR_ALL = 6;
   public static final int MECH_TYPE_AOE = 0;
   public static final int MECH_TYPE_SAFE_ZONE = 1;
   public static final int MECH_TYPE_SUMMON = 2;
   public static final int MECH_TYPE_PUZZLE = 3;
   public static final int MECH_TYPE_ULTIMATE = 4;
   public static final int MECH_TYPE_SHIELD = 5;
   private int action;
   private int raidId;
   private String mechanicName;
   private String warningMessage;
   private int mechanicType;
   private int warningTicks;
   private int durationTicks;
   private List<ZoneData> safeZones = new ArrayList();
   private List<ZoneData> dangerZones = new ArrayList();
   private String puzzleObjective;
   private int puzzleProgress;
   private int puzzleTotal;

   public static RaidMechanicMessage createWarning(int raidId, String mechanicName, String warningMessage, int mechanicType, int warningTicks) {
      RaidMechanicMessage msg = new RaidMechanicMessage();
      msg.action = 0;
      msg.raidId = raidId;
      msg.mechanicName = mechanicName;
      msg.warningMessage = warningMessage;
      msg.mechanicType = mechanicType;
      msg.warningTicks = warningTicks;
      return msg;
   }

   public static RaidMechanicMessage createStart(int raidId, String mechanicName, int mechanicType, int durationTicks) {
      RaidMechanicMessage msg = new RaidMechanicMessage();
      msg.action = 1;
      msg.raidId = raidId;
      msg.mechanicName = mechanicName;
      msg.mechanicType = mechanicType;
      msg.durationTicks = durationTicks;
      return msg;
   }

   public static RaidMechanicMessage createEnd(int raidId, String mechanicName) {
      RaidMechanicMessage msg = new RaidMechanicMessage();
      msg.action = 2;
      msg.raidId = raidId;
      msg.mechanicName = mechanicName;
      return msg;
   }

   public static RaidMechanicMessage createSafeZoneUpdate(int raidId, List<BlockPos> safePositions, double radius) {
      RaidMechanicMessage msg = new RaidMechanicMessage();
      msg.action = 3;
      msg.raidId = raidId;
      msg.safeZones = new ArrayList();

      for(BlockPos pos : safePositions) {
         msg.safeZones.add(new ZoneData(pos, radius));
      }

      return msg;
   }

   public static RaidMechanicMessage createDangerZoneUpdate(int raidId, List<BlockPos> dangerPositions, double radius) {
      RaidMechanicMessage msg = new RaidMechanicMessage();
      msg.action = 4;
      msg.raidId = raidId;
      msg.dangerZones = new ArrayList();

      for(BlockPos pos : dangerPositions) {
         msg.dangerZones.add(new ZoneData(pos, radius));
      }

      return msg;
   }

   public static RaidMechanicMessage createPuzzleUpdate(int raidId, String objective, int progress, int total) {
      RaidMechanicMessage msg = new RaidMechanicMessage();
      msg.action = 5;
      msg.raidId = raidId;
      msg.puzzleObjective = objective;
      msg.puzzleProgress = progress;
      msg.puzzleTotal = total;
      return msg;
   }

   public static RaidMechanicMessage createClearAll(int raidId) {
      RaidMechanicMessage msg = new RaidMechanicMessage();
      msg.action = 6;
      msg.raidId = raidId;
      return msg;
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readInt();
      this.raidId = buf.readInt();
      switch (this.action) {
         case 0:
            this.mechanicName = ByteBufUtils.readUTF8String(buf);
            this.warningMessage = ByteBufUtils.readUTF8String(buf);
            this.mechanicType = buf.readInt();
            this.warningTicks = buf.readInt();
            break;
         case 1:
            this.mechanicName = ByteBufUtils.readUTF8String(buf);
            this.mechanicType = buf.readInt();
            this.durationTicks = buf.readInt();
            break;
         case 2:
            this.mechanicName = ByteBufUtils.readUTF8String(buf);
            break;
         case 3:
            int safeCount = buf.readInt();
            this.safeZones = new ArrayList();

            for(int i = 0; i < safeCount; ++i) {
               BlockPos pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
               double radius = buf.readDouble();
               this.safeZones.add(new ZoneData(pos, radius));
            }
            break;
         case 4:
            int dangerCount = buf.readInt();
            this.dangerZones = new ArrayList();

            for(int i = 0; i < dangerCount; ++i) {
               BlockPos pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
               double radius = buf.readDouble();
               this.dangerZones.add(new ZoneData(pos, radius));
            }
            break;
         case 5:
            this.puzzleObjective = ByteBufUtils.readUTF8String(buf);
            this.puzzleProgress = buf.readInt();
            this.puzzleTotal = buf.readInt();
         case 6:
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.action);
      buf.writeInt(this.raidId);
      switch (this.action) {
         case 0:
            ByteBufUtils.writeUTF8String(buf, this.mechanicName != null ? this.mechanicName : "");
            ByteBufUtils.writeUTF8String(buf, this.warningMessage != null ? this.warningMessage : "");
            buf.writeInt(this.mechanicType);
            buf.writeInt(this.warningTicks);
            break;
         case 1:
            ByteBufUtils.writeUTF8String(buf, this.mechanicName != null ? this.mechanicName : "");
            buf.writeInt(this.mechanicType);
            buf.writeInt(this.durationTicks);
            break;
         case 2:
            ByteBufUtils.writeUTF8String(buf, this.mechanicName != null ? this.mechanicName : "");
            break;
         case 3:
            buf.writeInt(this.safeZones != null ? this.safeZones.size() : 0);
            if (this.safeZones != null) {
               for(ZoneData zone : this.safeZones) {
                  buf.writeInt(zone.pos.getX());
                  buf.writeInt(zone.pos.getY());
                  buf.writeInt(zone.pos.getZ());
                  buf.writeDouble(zone.radius);
               }
            }
            break;
         case 4:
            buf.writeInt(this.dangerZones != null ? this.dangerZones.size() : 0);
            if (this.dangerZones != null) {
               for(ZoneData zone : this.dangerZones) {
                  buf.writeInt(zone.pos.getX());
                  buf.writeInt(zone.pos.getY());
                  buf.writeInt(zone.pos.getZ());
                  buf.writeDouble(zone.radius);
               }
            }
            break;
         case 5:
            ByteBufUtils.writeUTF8String(buf, this.puzzleObjective != null ? this.puzzleObjective : "");
            buf.writeInt(this.puzzleProgress);
            buf.writeInt(this.puzzleTotal);
         case 6:
      }

   }

   public int getAction() {
      return this.action;
   }

   public int getRaidId() {
      return this.raidId;
   }

   public String getMechanicName() {
      return this.mechanicName;
   }

   public String getWarningMessage() {
      return this.warningMessage;
   }

   public int getMechanicType() {
      return this.mechanicType;
   }

   public int getWarningTicks() {
      return this.warningTicks;
   }

   public int getDurationTicks() {
      return this.durationTicks;
   }

   public List<ZoneData> getSafeZones() {
      return this.safeZones;
   }

   public List<ZoneData> getDangerZones() {
      return this.dangerZones;
   }

   public String getPuzzleObjective() {
      return this.puzzleObjective;
   }

   public int getPuzzleProgress() {
      return this.puzzleProgress;
   }

   public int getPuzzleTotal() {
      return this.puzzleTotal;
   }

   public static class ZoneData {
      public final BlockPos pos;
      public final double radius;

      public ZoneData(BlockPos pos, double radius) {
         this.pos = pos;
         this.radius = radius;
      }
   }

   public static class Handler implements IMessageHandler<RaidMechanicMessage, IMessage> {
      public IMessage onMessage(RaidMechanicMessage message, MessageContext ctx) {
         if (ctx.side == Side.CLIENT) {
            Minecraft.getMinecraft().addScheduledTask(() -> this.handleClient(message));
         }

         return null;
      }

      @SideOnly(Side.CLIENT)
      private void handleClient(RaidMechanicMessage message) {
         RaidClientData.handleMechanicUpdate(message);
      }
   }
}
