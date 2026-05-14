package net.luck.narutoaddon.handlers;

import net.luck.narutoaddon.entity.EntityShadowKunai;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = "narutoaddon")
public class ShadowEventHandler {

    private static final double R = 0.001D;
    private static final double G = 0.0D;
    private static final double B = 0.001D;

    // Metodo Helper per ottenere i dati NBT in modo sicuro su Forge 1.12.2
    private static NBTTagCompound getSafeData(EntityLivingBase entity) {
        return entity.writeToNBT(new NBTTagCompound()).getCompoundTag("ForgeData");
    }

    // Metodo Helper per salvare i dati NBT modificati
    private static void saveSafeData(EntityLivingBase entity, NBTTagCompound data) {
        NBTTagCompound topTag = entity.writeToNBT(new NBTTagCompound());
        topTag.setTag("ForgeData", data);
        entity.readFromNBT(topTag);
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity == null || entity.world == null) return;

        World world = entity.world;
        NBTTagCompound data = getSafeData(entity);
        long currentTime = world.getTotalWorldTime();

        // 1. PARTICELLE CLOAK (Effetto Visivo Armatura d'Ombra)
        if (data.getBoolean("ShadowCloakActive")) {
            if (world instanceof WorldServer && currentTime % 2 == 0) {
                spawnBlackParticle(world, entity.posX + (world.rand.nextDouble()-0.5)*0.7, entity.posY + world.rand.nextDouble()*2, entity.posZ + (world.rand.nextDouble()-0.5)*0.7);
            }
        }

        // 2. LOGICA FREEZE TOTALE (Immobilizzazione Jutsu)
        if (data.hasKey("ShadowFrozenUntil") && data.getLong("ShadowFrozenUntil") > currentTime) {
            entity.setPositionAndUpdate(data.getDouble("FreezeX"), data.getDouble("FreezeY"), data.getDouble("FreezeZ"));
            entity.rotationYaw = data.getFloat("FreezeYaw");
            entity.rotationPitch = data.getFloat("FreezePitch");
            entity.rotationYawHead = data.getFloat("FreezeYaw");
            entity.motionX = 0; entity.motionY = 0; entity.motionZ = 0;
            entity.velocityChanged = true;

            if (world instanceof WorldServer) renderHardFreezeVisuals((WorldServer) world, entity);
        }

        // 3. GATHERING LOGIC (Attrazione verso un punto)
        if (data.getBoolean("ShadowGatheringActive")) {
            handleGatheringLogic(world, entity, data, currentTime);
        }

        // 4. TRAP LOGIC (Attivazione Trappola Kunai)
        if (data.hasKey("TrapPosX")) {
            handleTrapLogic(world, entity, data);
        }

        // Salviamo i dati alla fine dell'update
        saveSafeData(entity, data);
    }

    private static void renderHardFreezeVisuals(WorldServer ws, EntityLivingBase entity) {
        double[] heights = {0.2D, 1.2D};
        for (double h : heights) {
            for (int i = 0; i < 10; i++) {
                double angle = i * (Math.PI * 2 / 10);
                spawnBlackParticle(ws, entity.posX + Math.cos(angle) * 0.9, entity.posY + h, entity.posZ + Math.sin(angle) * 0.9);
            }
        }
    }

    private static void handleGatheringLogic(World world, EntityLivingBase caster, NBTTagCompound data, long time) {
        double tx = data.getDouble("GatherPosX"), ty = data.getDouble("GatherPosY"), tz = data.getDouble("GatherPosZ");
        double range = 10.0D;

        if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer) world;
            double rot = time * 0.5D;
            for (int line = 0; line < 3; line++) {
                double baseAngle = (Math.PI * 2 / 3) * line + rot;
                for (int i = 0; i < 15; i++) {
                    double dist = (i / 15.0D) * range;
                    spawnBlackParticle(ws, tx + Math.cos(baseAngle + (i*0.15)) * dist, ty + 0.1, tz + Math.sin(baseAngle + (i*0.15)) * dist);
                }
            }
        }

        if (time % 2 == 0) {
            for (EntityLivingBase t : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(tx, ty, tz, tx, ty, tz).grow(range))) {
                if (t != caster && !t.isDead) {
                    double dx = tx - t.posX, dy = (ty + 0.5) - t.posY, dz = tz - t.posZ;
                    double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
                    if (dist > 0.4) {
                        t.motionX = (dx/dist)*0.5; t.motionY = (dy/dist)*0.5; t.motionZ = (dz/dist)*0.5;
                        t.velocityChanged = true;
                    }
                }
            }
        }
    }

    private static void handleTrapLogic(World world, EntityLivingBase caster, NBTTagCompound data) {
        double tx = data.getDouble("TrapPosX"), ty = data.getDouble("TrapPosY"), tz = data.getDouble("TrapPosZ");

        if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer) world;
            List<EntityShadowKunai> shadowKunais = world.getEntitiesWithinAABB(EntityShadowKunai.class, new AxisAlignedBB(tx, ty, tz, tx, ty, tz).grow(0.5));

            if (shadowKunais.isEmpty() || shadowKunais.get(0).isDead) {
                data.removeTag("TrapPosX"); data.removeTag("TrapPosY"); data.removeTag("TrapPosZ");
                return;
            }

            if (world.getTotalWorldTime() % 2 == 0) {
                for (int i = 0; i < 16; i++) {
                    double a = i * (Math.PI * 2 / 16);
                    spawnBlackParticle(ws, tx + Math.cos(a) * 2.0, ty + 0.1, tz + Math.sin(a) * 2.0);
                }
            }

            for (EntityLivingBase target : world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(tx, ty, tz, tx, ty, tz).grow(2.0, 1.5, 2.0))) {
                if (target != caster && !target.isDead) {
                    NBTTagCompound tData = getSafeData(target);
                    tData.setDouble("FreezeX", target.posX);
                    tData.setDouble("FreezeY", target.posY);
                    tData.setDouble("FreezeZ", target.posZ);
                    tData.setFloat("FreezeYaw", target.rotationYaw);
                    tData.setFloat("FreezePitch", target.rotationPitch);
                    tData.setLong("ShadowFrozenUntil", world.getTotalWorldTime() + 200);
                    saveSafeData(target, tData);

                    shadowKunais.get(0).setDead();
                    data.removeTag("TrapPosX"); data.removeTag("TrapPosY"); data.removeTag("TrapPosZ");
                    break;
                }
            }
        }
    }

    private static void spawnBlackParticle(World world, double x, double y, double z) {
        if (world instanceof WorldServer) ((WorldServer) world).spawnParticle(EnumParticleTypes.REDSTONE, true, x, y, z, 0, R, G, B, 1.0D);
    }
}