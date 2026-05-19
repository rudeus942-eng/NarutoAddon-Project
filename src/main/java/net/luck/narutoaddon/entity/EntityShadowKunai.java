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
        if (owner != null) {
            this.ownerUUID = owner.getUniqueID();
        }
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
            if (this.ticksExisted > MAX_LIFETIME) {
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

                    // FIX ANTI-CRASH: Controlliamo se il caster esiste ed è online
                    if (caster != null) {
                        // Se il caster c'è, applichiamo il congelamento normale attribuito a lui
                        ItemShadowRelease.applyHardFreeze(caster, target, 140);
                    } else {
                        // Se il caster è null (es. offline o rilocato), evitiamo il crash!
                        // Opzione A: Se applyHardFreeze crasha senza caster, simuliamo il congelamento usando il target stesso come finto caster
                        if (target instanceof EntityPlayer) {
                            ItemShadowRelease.applyHardFreeze((EntityPlayer) target, target, 140);
                        } else {
                            // Se il target è un mob (es. uno Zombie) e applyHardFreeze vuole SOLO un EntityPlayer,
                            // fermiamo l'esecuzione per evitare il crash, oppure esegui un congelamento vanilla alternativo.
                            // In questo caso, se non c'è un player a cui assegnare lo Stun, saltiamo la riga distruttiva.
                            System.out.println("[ShadowKunai] Caster offline, skipping freeze to prevent crash.");
                        }
                    }

                    // Il Kunai scompare in ogni caso dopo aver provato ad attivare la trappola
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