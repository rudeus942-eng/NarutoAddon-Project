package net.luck.narutoaddon.handlers;

import net.luck.narutoaddon.Items.ItemPurpleLightning;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = "narutoaddon")
public class PurpleLightningHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        EntityPlayer player = event.player;
        NBTTagCompound nbt = player.getEntityData();

        long until = nbt.getLong("PurpleLightningOverloadUntil");
        long currentTime = player.world.getTotalWorldTime();

        // Se il tempo è scaduto, resettiamo SEMPRE a 0
        if (until > 0 && currentTime >= until) {
            nbt.setLong("PurpleLightningOverloadUntil", 0L);
            if (!player.world.isRemote) {
                player.sendStatusMessage(new TextComponentString("§aNervi stabilizzati!"), true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        NBTTagCompound nbt = player.getEntityData();
        long until = nbt.getLong("PurpleLightningOverloadUntil");
        long currentTime = player.world.getTotalWorldTime();

        // Se non hai il fulmine in mano, non fare nulla e lascia passare l'evento
        if (!(player.getHeldItemMainhand().getItem() instanceof ItemPurpleLightning)) {
            return;
        }

        // Se hai il fulmine ed è in overload, blocca il click
        if (until > 0 && currentTime < until) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            }
        }
    }
}