package net.luck.narutoaddon.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.narutomod.item.ItemJutsu;

public class PacketSyncShadowJutsu implements IMessage {

    // Costruttore obbligatorio per Forge
    public PacketSyncShadowJutsu() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketSyncShadowJutsu, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncShadowJutsu message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (player == null) return null;

            // Eseguiamo sul thread principale del server
            player.getServerWorld().addScheduledTask(() -> {
                // Controlliamo prima la mano principale, poi la secondaria
                ItemStack stack = player.getHeldItemMainhand();
                if (!(stack.getItem() instanceof ItemJutsu.Base)) {
                    stack = player.getHeldItemOffhand();
                }

                if (stack.getItem() instanceof ItemJutsu.Base) {
                    // 1. Rotazione logica NBT
                    rotateJutsu(stack);

                    // 2. Feedback sonoro (Sfoglio di pergamena)
                    player.world.playSound(null, player.posX, player.posY, player.posZ,
                            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 0.6f, 1.2f);

                    // 3. Sincronizzazione totale inventario
                    player.inventoryContainer.detectAndSendChanges();
                }
            });
            return null;
        }

        private static void rotateJutsu(ItemStack stack) {
            NBTTagCompound compound = stack.getTagCompound();
            if (compound == null) {
                compound = new NBTTagCompound();
            }

            // Cicla tra 0 e 4 (5 Jutsu totali)
            int current = compound.getInteger("SelectedJutsu");
            int next = (current + 1) % 5;

            compound.setInteger("SelectedJutsu", next);
            stack.setTagCompound(compound);

            // Forza il sistema interno della Naruto Mod ad allinearsi
            // Molte versioni usano l'indice 'index' per decidere quale Jutsu lanciare
            compound.setInteger("index", next);
        }
    }
}