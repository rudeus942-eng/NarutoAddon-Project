package net.luck.narutoaddon.Items.ShadowKg;

import net.luck.narutoaddon.LuckTabs;
import net.luck.narutoaddon.entity.EntityShadowKunai;
import net.luck.narutoaddon.entity.EntityShadowSpike;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.narutomod.item.ItemJutsu;
import net.narutomod.Chakra;

import java.util.List;

public class ItemShadowRelease extends ItemJutsu.Base {

    public static final ItemJutsu.JutsuEnum CLOAK = new ItemJutsu.JutsuEnum(0, "shadow_cloak", 'B', 150d, new NetCloak());
    public static final ItemJutsu.JutsuEnum SPIKE = new ItemJutsu.JutsuEnum(1, "shadow_spike", 'A', 200d, new NetSpike());
    public static final ItemJutsu.JutsuEnum TENDRILS = new ItemJutsu.JutsuEnum(2, "shadow_tendrils", 'S', 300d, new NetTendrils());
    public static final ItemJutsu.JutsuEnum GATHERING = new ItemJutsu.JutsuEnum(3, "shadow_gathering", 'S', 250d, new NetGathering());
    public static final ItemJutsu.JutsuEnum TRAP = new ItemJutsu.JutsuEnum(4, "shadow_trap", 'B', 100d, new NetTrap());

    public ItemShadowRelease() {
        super(ItemJutsu.JutsuEnum.Type.RAITON, CLOAK, SPIKE, TENDRILS, GATHERING, TRAP);
        this.setTranslationKey("shadow_release");
        this.setRegistryName("shadow_release");
        this.setCreativeTab(LuckTabs.LUCK_TAB);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(net.minecraft.world.World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ItemJutsu.JutsuEnum selected = this.getCurrentJutsu(stack);
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void onUpdate(ItemStack stack, net.minecraft.world.World world, Entity entity, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);

        if (entity instanceof EntityPlayer && isSelected) {
            EntityPlayer player = (EntityPlayer) entity;

            if (world.isRemote) {
                ItemJutsu.JutsuEnum selected = this.getCurrentJutsu(stack);
                if (selected != null) {
                    if (!this.isJutsuEnabled(stack, selected)) {
                        this.enableJutsu(stack, selected, true);
                    }
                }
            }

            if (!world.isRemote) {
                NBTTagCompound nbt = player.getEntityData();
                long currentTime = world.getTotalWorldTime();

                if (nbt.getBoolean("IsShadowStunned")) {
                    if (currentTime < nbt.getLong("ShadowFrozenUntil")) {
                        Chakra.Pathway pathway = Chakra.pathway(player);
                        if (pathway != null) pathway.consume(0.0001d);
                    } else {
                        nbt.setBoolean("IsShadowStunned", false);
                    }
                }

                if (nbt.getBoolean("ShadowCloakActive")) {
                    player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 25, 14, false, false));
                    player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 25, 2, false, false));

                    if (currentTime - nbt.getLong("ShadowCloakStartTime") >= 18000 || !Chakra.pathway(player).consume(1.5d)) {
                        nbt.setBoolean("ShadowCloakActive", false);
                        applyShadowSpecificCooldown(player, nbt, currentTime);
                    }
                }
            }
        }
    }

    private static void applyShadowSpecificCooldown(EntityPlayer player, NBTTagCompound nbt, long currentTime) {
        long duration = currentTime - nbt.getLong("ShadowCloakStartTime");
        if (duration > 600) {
            int cd = (int) ((Math.min(duration, 18000) - 600) * 0.345f);
            nbt.setLong("ShadowCloakCDUntil", currentTime + Math.min(cd, 6000));
        }
        player.removePotionEffect(MobEffects.SPEED);
        player.removePotionEffect(MobEffects.RESISTANCE);
        nbt.setLong("ShadowCloakStartTime", 0);
    }

    // --- CALLBACK JUTSU (LOGICA ORIGINALE INVARIATA) ---

    public static class NetCloak implements ItemJutsu.IJutsuCallback {
        @Override public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
            if (!(entity instanceof EntityPlayer)) return false;
            EntityPlayer p = (EntityPlayer) entity;
            NBTTagCompound nbt = p.getEntityData();
            long time = p.world.getTotalWorldTime();
            if (nbt.getLong("ShadowCloakCDUntil") > time) return false;
            boolean active = !nbt.getBoolean("ShadowCloakActive");
            nbt.setBoolean("ShadowCloakActive", active);
            if (active) {
                nbt.setLong("ShadowCloakStartTime", time);
                p.addPotionEffect(new PotionEffect(MobEffects.SPEED, 40, 14, false, false));
                p.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 40, 2, false, false));
            } else {
                applyShadowSpecificCooldown(p, nbt, time);
            }
            p.world.playSound(null, p.posX, p.posY, p.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0f, active ? 0.5f : 0.8f);
            return true;
        }
    }

    public static class NetSpike implements ItemJutsu.IJutsuCallback {
        @Override public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
            RayTraceResult look = getSafeLook(entity, 20);
            if (look != null && look.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos pos = look.getBlockPos().up();
                if (!entity.world.isRemote) {
                    boolean isSneaking = entity.isSneaking();
                    int charge = 10;
                    for (int i = 0; i < (int)(charge * 1.3f); i++) {
                        EntityShadowSpike spike = new EntityShadowSpike(entity.world, pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);
                        if (isSneaking) spike.getEntityData().setBoolean("IsConjureSpike", true);
                        else spike.getEntityData().setBoolean("IsDamageSpike", true);
                        entity.world.spawnEntity(spike);
                    }
                }
                return true;
            }
            return false;
        }
    }

    public static class NetGathering implements ItemJutsu.IJutsuCallback {
        @Override public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
            if (entity.world.isRemote) return false;
            NBTTagCompound data = entity.getEntityData();
            if (data.getBoolean("ShadowGatheringActive")) {
                data.setBoolean("ShadowGatheringActive", false);
                data.removeTag("ShadowGatheringUntil");
                return true;
            }
            RayTraceResult look = getSafeLook(entity, 20);
            if (look != null && look.typeOfHit == RayTraceResult.Type.BLOCK) {
                data.setBoolean("ShadowGatheringActive", true);
                data.setDouble("GatherPosX", look.getBlockPos().getX() + 0.5);
                data.setDouble("GatherPosY", look.getBlockPos().getY() + 1.1);
                data.setDouble("GatherPosZ", look.getBlockPos().getZ() + 0.5);
                data.setLong("ShadowGatheringUntil", entity.world.getTotalWorldTime() + 1200);
                return true;
            }
            return false;
        }
    }

    public static class NetTrap implements ItemJutsu.IJutsuCallback {
        @Override public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
            RayTraceResult look = getSafeLook(entity, 15);
            if (look != null && look.typeOfHit == RayTraceResult.Type.BLOCK) {
                if (!entity.world.isRemote && entity instanceof EntityPlayer) {
                    BlockPos p = look.getBlockPos();
                    EntityShadowKunai k = new EntityShadowKunai(entity.world, p.getX()+0.5, p.getY()+1, p.getZ()+0.5);
                    k.setOwner((EntityPlayer) entity);
                    entity.world.spawnEntity(k);
                    NBTTagCompound data = entity.getEntityData();
                    data.setDouble("TrapPosX", p.getX()+0.5);
                    data.setDouble("TrapPosY", p.getY()+1.0);
                    data.setDouble("TrapPosZ", p.getZ()+0.5);
                }
                return true;
            }
            return false;
        }
    }

    public static class NetTendrils implements ItemJutsu.IJutsuCallback {
        @Override public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
            if (entity.world.isRemote) return false;
            EntityLivingBase target = getTarget(entity, 15.0D);
            if (target != null && entity instanceof EntityPlayer) {
                applyHardFreeze((EntityPlayer)entity, target, 200, true);
                return true;
            }
            return false;
        }
    }

    private static RayTraceResult getSafeLook(EntityLivingBase e, double r) {
        Vec3d eyes = e.getPositionEyes(1.0F);
        return e.world.rayTraceBlocks(eyes, eyes.add(e.getLookVec().scale(r)), false, true, false);
    }

    private static EntityLivingBase getTarget(EntityLivingBase caster, double range) {
        EntityLivingBase bestTarget = null;
        double closestDist = range;
        Vec3d lookVec = caster.getLookVec();
        Vec3d eyePos = caster.getPositionEyes(1.0F);
        List<EntityLivingBase> list = caster.world.getEntitiesWithinAABB(EntityLivingBase.class, caster.getEntityBoundingBox().grow(range));
        for (EntityLivingBase target : list) {
            if (target != caster && !target.isDead && caster.canEntityBeSeen(target)) {
                Vec3d toTarget = new Vec3d(target.posX - caster.posX, (target.getEntityBoundingBox().minY + target.getEyeHeight()) - eyePos.y, target.posZ - caster.posZ);
                double dist = toTarget.length();
                double dot = lookVec.dotProduct(toTarget.normalize());
                if (dot > 0.94D && dist < closestDist) {
                    closestDist = dist;
                    bestTarget = target;
                }
            }
        }
        return bestTarget;
    }

    public static void applyHardFreeze(EntityPlayer caster, EntityLivingBase target, int duration, boolean isTendrils) {
        NBTTagCompound data = target.getEntityData();
        long endTime = target.world.getTotalWorldTime() + duration;
        data.setDouble("FreezeX", target.posX);
        data.setDouble("FreezeY", target.posY);
        data.setDouble("FreezeZ", target.posZ);
        data.setFloat("FreezeYaw", target.rotationYaw);
        data.setFloat("FreezePitch", target.rotationPitch);
        data.setLong("ShadowFrozenUntil", endTime);
        data.setBoolean("IsShadowStunned", true);
        target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, duration, 127, false, false));
        target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, duration, 4, false, false));

        if (target instanceof EntityPlayer) {
            ((EntityPlayer)target).sendStatusMessage(new TextComponentString("§8The shadows are binding your every movement!"), true);
        }
    }
}