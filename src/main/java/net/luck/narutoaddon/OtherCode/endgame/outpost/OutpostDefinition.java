package net.luck.narutoaddon.OtherCode.endgame.outpost;

import net.minecraft.util.math.BlockPos;

public class OutpostDefinition {
   private final String outpostId;
   private final String locationName;
   private final BlockPos location;
   private final String loreDescription;
   private final int activationRadius;
   private final long cooldownMs;
   private final int maxParticipants;

   public OutpostDefinition(String outpostId, String locationName, BlockPos location, String loreDescription, int activationRadius, long cooldownMs, int maxParticipants) {
      this.outpostId = outpostId;
      this.locationName = locationName;
      this.location = location;
      this.loreDescription = loreDescription;
      this.activationRadius = activationRadius;
      this.cooldownMs = cooldownMs;
      this.maxParticipants = maxParticipants;
   }

   public String getOutpostId() {
      return this.outpostId;
   }

   public String getLocationName() {
      return this.locationName;
   }

   /** @deprecated */
   @Deprecated
   public String getDisplayName() {
      return this.locationName;
   }

   public BlockPos getLocation() {
      return this.location;
   }

   public String getLoreDescription() {
      return this.loreDescription;
   }

   public int getActivationRadius() {
      return this.activationRadius;
   }

   public long getCooldownMs() {
      return this.cooldownMs;
   }

   public int getMaxParticipants() {
      return this.maxParticipants;
   }
}
