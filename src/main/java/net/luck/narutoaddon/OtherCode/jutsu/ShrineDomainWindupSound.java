
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ShrineDomainWindupSound extends MovingSound {
   private final EntityShrineMalevolentShrine.EntityCustom domain;

   public ShrineDomainWindupSound(EntityShrineMalevolentShrine.EntityCustom domain, SoundEvent sound) {
      super(sound, SoundCategory.PLAYERS);
      this.domain = domain;
      this.repeat = false;
      this.volume = 4.0F;
      this.pitch = 1.0F;
      this.updatePosition();
   }

   public void update() {
      if (this.domain != null && !this.domain.isDead && !this.domain.isShrineVisible() && this.domain.ticksExisted <= 204) {
         EntityLivingBase owner = this.domain.getOwnerEntity();
         if (owner != null && owner.isEntityAlive()) {
            this.xPosF = (float)owner.posX;
            this.yPosF = (float)(owner.posY + (double)owner.height * (double)0.5F);
            this.zPosF = (float)owner.posZ;
         } else {
            this.donePlaying = true;
         }
      } else {
         this.donePlaying = true;
      }
   }

   private void updatePosition() {
      this.xPosF = (float)this.domain.posX;
      this.yPosF = (float)this.domain.posY;
      this.zPosF = (float)this.domain.posZ;
   }
}
