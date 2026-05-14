package net.luck.narutoaddon.entity;

import net.luck.narutoaddon.Items.ShadowKg.ItemShadowRelease;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class EntityShadowKunai extends Entity {

    private UUID ownerUUID;
    // Definiamo la durata massima: 10 secondi = 200 ticks
    private static final int MAX_LIFETIME = 200;

    public EntityShadowKunai(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.5F);
        this.noClip = true;
    }

    public EntityShadowKunai(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setPosition(x, y, z);
    }

    public void setOwner(EntityPlayer owner) {
        this.ownerUUID = owner.getUniqueID();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        // Il Kunai rimane fisso a terra dove viene spawnato
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;

        if (!this.world.isRemote) {
            // 1. SICUREZZA DESPAWN: 10 secondi (200 ticks)
            if (this.ticksExisted > 200) {
                this.setDead();
                return;
            }

            // 2. LOGICA DI ATTIVAZIONE
            // Raggio di 1.5 blocchi per rilevare il passaggio di nemici
            AxisAlignedBB triggerArea = this.getEntityBoundingBox().grow(1.5D);
            List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, triggerArea);

            for (EntityLivingBase target : targets) {
                // Filtri: No proprietario, no player in Creative
                boolean isOwner = ownerUUID != null && target.getUniqueID().equals(ownerUUID);
                boolean isCreative = (target instanceof EntityPlayer && ((EntityPlayer)target).isCreative());

                if (!isOwner && !isCreative && target.isEntityAlive()) {
                    // Recuperiamo il caster tramite l'UUID salvato nel Kunai
                    EntityPlayer caster = (ownerUUID != null) ? this.world.getPlayerEntityByUUID(ownerUUID) : null;

                    // CHIAMATA RIPRISTINATA: 4 Argomenti (caster, target, durata, isTendrils)
                    // Usiamo il caster (se trovato) o il target stesso come fallback per evitare null
                    ItemShadowRelease.applyHardFreeze(caster, target, 140);
                    // Il Kunai scompare dopo aver attivato la trappola
                    this.setDead();
                    break;
                }
            }
        }
    }

    @Override
    protected void entityInit() {}

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasUniqueId("OwnerUUID")) {
            this.ownerUUID = compound.getUniqueId("OwnerUUID");
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (this.ownerUUID != null) {
            compound.setUniqueId("OwnerUUID", this.ownerUUID);
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
}