package net.luck.narutoaddon.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "narutoaddon")
public class PurpleLightningHandler {

    // Helper per leggere i dati NBT in modo sicuro (Compatibile con Forge 1.12.2)
    private static NBTTagCompound getSafeData(EntityPlayer player) {
        return player.writeToNBT(new NBTTagCompound()).getCompoundTag("ForgeData");
    }

    // Helper per salvare i dati NBT
    private static void saveSafeData(EntityPlayer player, NBTTagCompound data) {
        NBTTagCompound topTag = player.writeToNBT(new NBTTagCompound());
        topTag.setTag("ForgeData", data);
        player.readFromNBT(topTag);
    }

    // --- CHAOS LOGIC (Tick) ---
    @SubscribeEvent
    public static void onPlayerOverloadTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        EntityPlayer player = event.player;
        NBTTagCompound nbt = getSafeData(player);

        if (nbt.hasKey("PurpleLightningOverloadUntil")) {
            long until = nbt.getLong("PurpleLightningOverloadUntil");
            long currentTime = player.world.getTotalWorldTime();

            if (currentTime < until) {
                Random rand = player.world.rand;

                // Logica Movimento (Client e Server)
                if (currentTime % 2 == 0) {
                    player.motionX += (rand.nextDouble() - 0.5) * 0.2;
                    player.motionZ += (rand.nextDouble() - 0.5) * 0.2;
                    player.rotationYaw += (rand.nextFloat() * 12 - 6);
                    player.rotationPitch += (rand.nextFloat() * 8 - 4);
                }

                // Logica Inventario (Solo Server)
                if (!player.world.isRemote && currentTime % 15 == 0) {
                    player.inventory.currentItem = rand.nextInt(9);
                    player.velocityChanged = true;
                }

            } else {
                // Reset Overload
                if (!player.world.isRemote) {
                    nbt.removeTag("PurpleLightningOverloadUntil");
                    saveSafeData(player, nbt);
                    player.sendStatusMessage(new TextComponentString("§aYour nerves have stabilized."), true);
                }
            }
        }
    }

    // --- BLOCCO CLICK DESTRO ---
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        cancelIfOverloaded(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        cancelIfOverloaded(event);
    }

    // --- BLOCCO CLICK SINISTRO ---
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        cancelIfOverloaded(event);
    }

    private static void cancelIfOverloaded(PlayerInteractEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        NBTTagCompound nbt = getSafeData(player);

        if (nbt.hasKey("PurpleLightningOverloadUntil") &&
                player.world.getTotalWorldTime() < nbt.getLong("PurpleLightningOverloadUntil")) {

            event.setCanceled(true);

            if (!player.world.isRemote && player.world.getTotalWorldTime() % 10 == 0) {
                player.attackEntityFrom(DamageSource.LIGHTNING_BOLT, 1.0F);
                player.sendStatusMessage(new TextComponentString("§5§lYour muscles are paralyzed!"), true);
            }
        }
    }
}