
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ShrineChargingCastSound extends MovingSound {
   private final EntityShrineDivineFlames.EntityCustom arrow;

   public ShrineChargingCastSound(EntityShrineDivineFlames.EntityCustom arrow, SoundEvent sound) {
      super(sound, SoundCategory.PLAYERS);
      this.arrow = arrow;
      this.repeat = false;
      this.volume = 1.0F;
      this.pitch = 1.0F;
      this.updatePosition();
   }

   public void update() {
      if (this.arrow != null && !this.arrow.isDead && this.arrow.isCharging()) {
         EntityLivingBase owner = this.arrow.getOwnerEntity();
         if (owner != null && owner.isEntityAlive()) {
            this.updatePosition();
         } else {
            this.donePlaying = true;
         }
      } else {
         this.donePlaying = true;
      }
   }

   private void updatePosition() {
      this.xPosF = (float)this.arrow.posX;
      this.yPosF = (float)this.arrow.posY;
      this.zPosF = (float)this.arrow.posZ;
   }
}
