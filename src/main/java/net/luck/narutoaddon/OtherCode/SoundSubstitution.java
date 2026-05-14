package net.luck.narutoaddon.OtherCode;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

public class SoundSubstitution {
   @ObjectHolder("inftsukaddon:substitution")
   public static final SoundEvent SUBSTITUTION_SOUND = null;

   public static SoundEvent getSubstitutionSound() {
      return (SoundEvent)ElementsInfTsukAddon.sounds.get(new ResourceLocation("inftsukaddon", "substitution"));
   }
}
