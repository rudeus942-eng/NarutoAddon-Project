package net.luck.narutoaddon.OtherCode.quest.npc;

public enum NpcPose {
   STANDING(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
   SITTING(-0.15F, 0.0F, 0.0F, -0.15F, 0.0F, 0.0F, -1.4137167F, 0.31416F, 0.07854F, -1.4137167F, -0.31416F, -0.07854F, 0.0F, -0.65F),
   SITTING_CROSS_LEGGED(-0.3F, 0.0F, 0.0F, -0.3F, 0.0F, 0.0F, -1.6F, 0.5F, 0.5F, -1.6F, -0.5F, -0.5F, 0.0F, -0.7F),
   ARMS_CROSSED(-0.85F, 0.35F, 0.0F, -0.85F, -0.35F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

   public final float rightArmX;
   public final float rightArmY;
   public final float rightArmZ;
   public final float leftArmX;
   public final float leftArmY;
   public final float leftArmZ;
   public final float rightLegX;
   public final float rightLegY;
   public final float rightLegZ;
   public final float leftLegX;
   public final float leftLegY;
   public final float leftLegZ;
   public final float bodyX;
   public final float yOffset;

   private NpcPose(float rax, float ray, float raz, float lax, float lay, float laz, float rlx, float rly, float rlz, float llx, float lly, float llz, float bodyX, float yOffset) {
      this.rightArmX = rax;
      this.rightArmY = ray;
      this.rightArmZ = raz;
      this.leftArmX = lax;
      this.leftArmY = lay;
      this.leftArmZ = laz;
      this.rightLegX = rlx;
      this.rightLegY = rly;
      this.rightLegZ = rlz;
      this.leftLegX = llx;
      this.leftLegY = lly;
      this.leftLegZ = llz;
      this.bodyX = bodyX;
      this.yOffset = yOffset;
   }

   public boolean isCustom() {
      return this != STANDING;
   }

   public static NpcPose fromName(String name) {
      if (name != null && !name.isEmpty()) {
         try {
            return valueOf(name.toUpperCase());
         } catch (IllegalArgumentException var2) {
            return STANDING;
         }
      } else {
         return STANDING;
      }
   }
}
