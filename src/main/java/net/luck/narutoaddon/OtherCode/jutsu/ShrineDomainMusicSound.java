
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ShrineDomainMusicSound extends MovingSound {
   private final EntityShrineMalevolentShrine.EntityCustom domain;

   public ShrineDomainMusicSound(EntityShrineMalevolentShrine.EntityCustom domain, SoundEvent sound) {
      super(sound, SoundCategory.AMBIENT);
      this.domain = domain;
      this.repeat = false;
      this.volume = 4.0F;
      this.pitch = 1.0F;
      this.updatePosition();
   }

   public void update() {
      if (this.domain != null && !this.domain.isDead && this.domain.isShrineVisible() && this.domain.getActiveAge() <= 1200) {
         EntityLivingBase owner = this.domain.getOwnerEntity();
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
      this.xPosF = (float)this.domain.posX;
      this.yPosF = (float)(this.domain.posY + (double)2.0F);
      this.zPosF = (float)this.domain.posZ;
   }
}
