package net.luck.narutoaddon.StatMenu;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class NinjaStatsStorage implements IStorage<INinjaStats> {

    @Override
    public NBTBase writeNBT(Capability<INinjaStats> capability, INinjaStats instance, EnumFacing side) {
        NBTTagCompound compound = new NBTTagCompound();

        // Saving numeric stats
        compound.setFloat("health", instance.getHealth());
        compound.setInteger("strength", instance.getStrength());
        compound.setInteger("agility", instance.getAgility());

        // Saving strings (Releases, Kekkei Genkai, Dojutsu)
        compound.setString("releases", instance.getReleases());
        compound.setString("kekkeiGenkai", instance.getKekkeiGenkai());
        compound.setString("dojutsu", instance.getDojutsu());

        return compound;
    }

    @Override
    public void readNBT(Capability<INinjaStats> capability, INinjaStats instance, EnumFacing side, NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) return;
        NBTTagCompound compound = (NBTTagCompound) nbt;

        // Loading numeric stats
        instance.setHealth(compound.getFloat("health"));
        instance.setStrength(compound.getInteger("strength"));
        instance.setAgility(compound.getInteger("agility"));

        // Loading strings
        instance.setReleases(compound.getString("releases"));
        instance.setKekkeiGenkai(compound.getString("kekkeiGenkai"));
        instance.setDojutsu(compound.getString("dojutsu"));
    }
}