package net.luck.narutoaddon.OtherCode.quest.waypoint;

import net.luck.narutoaddon.OtherCode.quest.core.QuestStep;
import net.minecraft.util.math.BlockPos;

public class WaypointData {
   private final BlockPos position;
   private final WaypointType type;
   private final String label;

   public WaypointData(BlockPos position, WaypointType type, String label) {
      this.position = position;
      this.type = type;
      this.label = label;
   }

   public BlockPos getPosition() {
      return this.position;
   }

   public WaypointType getType() {
      return this.type;
   }

   public String getLabel() {
      return this.label;
   }

   public static WaypointType fromStepType(QuestStep.StepType stepType) {
      switch (stepType) {
         case TRAVEL:
            return WaypointType.TRAVEL;
         case COMBAT:
            return WaypointType.COMBAT;
         case DIALOG:
            return WaypointType.DIALOG;
         case INTERACT:
            return WaypointType.INTERACT;
         default:
            return WaypointType.TRAVEL;
      }
   }

   public static enum WaypointType {
      TRAVEL,
      COMBAT,
      DIALOG,
      INTERACT,
      PVP_TARGET,
      PVP_BINGO,
      PVP_MUTUAL;
   }
}
