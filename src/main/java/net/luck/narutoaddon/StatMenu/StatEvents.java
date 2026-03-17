package net.luck.narutoaddon.StatMenu;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class StatEvents {
    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            // Questo "incolla" le stats al giocatore quando entra nel mondo
            event.addCapability(new ResourceLocation("narutoaddon", "ninja_stats"), new NinjaStatsProvider());
        }
    }
}