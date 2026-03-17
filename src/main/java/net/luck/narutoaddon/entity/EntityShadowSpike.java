package net.luck.narutoaddon.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public class EntityShadowSpike extends Entity {

    private int lifeTime = 0;
    // --- MODIFICA: Default a 200 tick (10 secondi) ---
    private int maxLife = 200;

    private static final DataParameter<Float> ROTATION_PITCH = EntityDataManager.createKey(EntityShadowSpike.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> SCALE = EntityDataManager.createKey(EntityShadowSpike.class, DataSerializers.FLOAT);

    public EntityShadowSpike(World worldIn) {
        super(worldIn);
        this.setSize(0.8F, 2.5F);
        this.preventEntitySpawning = true;
        this.ignoreFrustumCheck = true;

        if (!worldIn.isRemote) {
            this.rotationYaw = worldIn.rand.nextFloat() * 360.0F;
            this.setSpikePitch((worldIn.rand.nextFloat() - 0.5F) * 70.0F);
            this.setSpikeScale(0.5F + worldIn.rand.nextFloat() * 1.0F);
        }
    }

    public void setMaxLife(int ticks) {
        this.maxLife = ticks;
    }

    public EntityShadowSpike(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setPosition(x, y, z);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(ROTATION_PITCH, 0.0F);
        this.dataManager.register(SCALE, 1.0F);
    }

    public void setSpikePitch(float pitch) { this.dataManager.set(ROTATION_PITCH, pitch); }
    public float getSpikePitch() { return this.dataManager.get(ROTATION_PITCH); }

    public void setSpikeScale(float scale) { this.dataManager.set(SCALE, scale); }
    public float getSpikeScale() { return this.dataManager.get(SCALE); }

    @Override
    public void onUpdate() {
        super.onUpdate();

        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;

        if (!this.world.isRemote) {
            this.lifeTime++;
            if (this.lifeTime > this.maxLife) {
                this.setDead();
            }
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return distance < 1024.0D;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.lifeTime = compound.getInteger("LifeTime");
        this.maxLife = compound.getInteger("MaxLife");
        // Se carichiamo un vecchio spike senza NBT, mettiamo il default di 10s
        if (this.maxLife <= 0) this.maxLife = 200;

        this.setSpikePitch(compound.getFloat("SpikePitch"));
        this.setSpikeScale(compound.getFloat("SpikeScale"));
        this.rotationYaw = compound.getFloat("SpikeYaw");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("LifeTime", this.lifeTime);
        compound.setInteger("MaxLife", this.maxLife);
        compound.setFloat("SpikePitch", this.getSpikePitch());
        compound.setFloat("SpikeScale", this.getSpikeScale());
        compound.setFloat("SpikeYaw", this.rotationYaw);
    }
}