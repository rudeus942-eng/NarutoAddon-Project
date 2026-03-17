package net.luck.narutoaddon.StatMenu;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class NinjaStatsProvider implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(INinjaStats.class)
    public static final Capability<INinjaStats> NINJA_STATS = null;

    private INinjaStats instance = NINJA_STATS.getDefaultInstance();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == NINJA_STATS;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == NINJA_STATS ? NINJA_STATS.<T>cast(this.instance) : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return NINJA_STATS.getStorage().writeNBT(NINJA_STATS, this.instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        NINJA_STATS.getStorage().readNBT(NINJA_STATS, this.instance, null, nbt);
    }
}