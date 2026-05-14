package net.luck.narutoaddon.Items.ShadowKg;

import net.luck.narutoaddon.LuckTabs;
import net.luck.narutoaddon.entity.EntityShadowKunai;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.narutomod.Chakra;
import net.narutomod.item.ItemJutsu;
import net.narutomod.procedure.ProcedureUtils;

public class ItemShadowRelease extends ItemJutsu.Base {

    // --- REGISTRAZIONE JUTSU CON I TUOI NUOVI VALORI ---
    // Ordine: Indice, Nome, Rango, Livello Sblocco, Costo Chakra, Callback
    // Esempio della riga corretta nel codice
    public static final ItemJutsu.JutsuEnum TRAP = new ItemJutsu.JutsuEnum(0, "shadow_trap", 'B', 100, 100d, new NetTrap());
    public static final ItemJutsu.JutsuEnum CLOAK = new ItemJutsu.JutsuEnum(1, "shadow_cloak", 'A', 150, 150d, new NetCloak());
    public static final ItemJutsu.JutsuEnum GATHERING = new ItemJutsu.JutsuEnum(2, "shadow_gathering", 'S', 200, 250d, new NetGathering());
    public static final ItemJutsu.JutsuEnum TENDRILS = new ItemJutsu.JutsuEnum(3, "shadow_tendrils", 'S', 200, 300d, new NetTendrils());
    public ItemShadowRelease() {
        super(ItemJutsu.JutsuEnum.Type.NINJUTSU, TRAP, CLOAK, GATHERING, TENDRILS);
        this.setTranslationKey("shadow_release");
        this.setRegistryName("shadow_release");
        this.setCreativeTab(LuckTabs.LUCK_TAB);
    }

    // --- LOGICA COSTI DINAMICI (+5% per ogni entità extra bloccata) ---

    public int getStunnedCount(EntityPlayer player) {
        int count = 0;
        String playerUuid = player.getUniqueID().toString();
        for (Entity entity : player.world.loadedEntityList) {
            if (entity instanceof EntityLivingBase) {
                NBTTagCompound nbt = entity.getEntityData();
                if (nbt.getBoolean("IsShadowStunned") && playerUuid.equals(nbt.getString("StunnedByUUID"))) {
                    count++;
                }
            }
        }
        return count;
    }

    private double getChakraMultiplier(EntityPlayer player) {
        int count = getStunnedCount(player);
        // Se count <= 1 moltiplicatore è 1.0. Se count è 2 -> 1.05, se count è 3 -> 1.10.
        return 1.0 + (Math.max(0, count - 1) * 0.05);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote) {
            double multiplier = getChakraMultiplier(playerIn);
            if (multiplier > 1.0) {
                ItemJutsu.JutsuEnum current = this.getCurrentJutsu(itemstack);
                double extraCost = (current.chakraUsage * multiplier) - current.chakraUsage;
                if (!Chakra.pathway(playerIn).consume(extraCost)) {
                    playerIn.sendStatusMessage(new TextComponentString("§cTo many entities blocked! Insufficient chakra due to Overload."), true);
                    return new ActionResult<>(EnumActionResult.FAIL, itemstack);
                }
            }
            this.executeJutsu(itemstack, playerIn, 1.0f);
        }
        playerIn.swingArm(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            NBTTagCompound nbt = player.getEntityData();

            if (!world.isRemote) {
                long currentTime = world.getTotalWorldTime();

                // 1. CLOAK: Mantenimento e Cooldown dinamico
                if (nbt.getBoolean("ShadowCloakActive")) {
                    nbt.setInteger("ShadowUsageTimer", nbt.getInteger("ShadowUsageTimer") + 1);
                    player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 25, 14, false, false));
                    player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 25, 2, false, false));

                    double costPerTick = 1.5d * getChakraMultiplier(player);
                    if (!Chakra.pathway(player).consume(costPerTick)) {
                        deactivateCloak(player, nbt);
                    }
                } else if (nbt.getInteger("ShadowUsageTimer") > 0) {
                    this.setJutsuCooldown(stack, CLOAK, nbt.getInteger("ShadowUsageTimer") * 2L);
                    nbt.setInteger("ShadowUsageTimer", 0);
                }

                // 2. GATHERING: Danni puri e Blindness al centro
                if (nbt.getBoolean("ShadowGatheringActive")) {
                    if (currentTime % 20 == 0) {
                        double gx = nbt.getDouble("GatherPosX"), gy = nbt.getDouble("GatherPosY"), gz = nbt.getDouble("GatherPosZ");
                        AxisAlignedBB center = new AxisAlignedBB(gx-1.5, gy-1.5, gz-1.5, gx+1.5, gy+1.5, gz+1.5);
                        for (EntityLivingBase target : world.getEntitiesWithinAABB(EntityLivingBase.class, center)) {
                            if (target != player) {
                                target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 60, 0));
                                target.attackEntityFrom(DamageSource.OUT_OF_WORLD, 5.0f);
                            }
                        }
                    }
                }

                // 3. STUN (Hard Freeze)
                if (nbt.getBoolean("IsShadowStunned")) {
                    if (currentTime >= nbt.getLong("ShadowFrozenUntil")) {
                        nbt.setBoolean("IsShadowStunned", false);
                        nbt.removeTag("StunnedByUUID");
                        player.removePotionEffect(MobEffects.SLOWNESS);
                    } else {
                        player.setPositionAndUpdate(nbt.getDouble("FreezeX"), nbt.getDouble("FreezeY"), nbt.getDouble("FreezeZ"));
                    }
                }
            }
        }
    }

    private void deactivateCloak(EntityPlayer player, NBTTagCompound nbt) {
        nbt.setBoolean("ShadowCloakActive", false);
        player.removePotionEffect(MobEffects.SPEED);
        player.removePotionEffect(MobEffects.RESISTANCE);
    }

    public static void applyHardFreeze(EntityPlayer caster, EntityLivingBase target, int duration) {
        NBTTagCompound data = target.getEntityData();
        data.setDouble("FreezeX", target.posX);
        data.setDouble("FreezeY", target.posY);
        data.setDouble("FreezeZ", target.posZ);
        data.setLong("ShadowFrozenUntil", target.world.getTotalWorldTime() + duration);
        data.setBoolean("IsShadowStunned", true);
        data.setString("StunnedByUUID", caster.getUniqueID().toString());
        target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, duration, 127, false, false));
    }

    // --- CALLBACKS ---

    public static class NetCloak implements ItemJutsu.IJutsuCallback {
        @Override
        public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
            NBTTagCompound nbt = entity.getEntityData();
            boolean active = !nbt.getBoolean("ShadowCloakActive");
            nbt.setBoolean("ShadowCloakActive", active);
            entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0f, active ? 0.5f : 0.8f);
            return true;
        }
    }

    public static class NetTendrils implements ItemJutsu.IJutsuCallback {
        @Override
        public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
            if (!(entity instanceof EntityPlayer)) return false;
            EntityPlayer player = (EntityPlayer) entity;

            if (((ItemShadowRelease)stack.getItem()).getStunnedCount(player) >= 3) {
                if (!player.world.isRemote) player.sendStatusMessage(new TextComponentString("§cTendrils Max Reached!"), true);
                return false;
            }

            RayTraceResult res = ProcedureUtils.objectEntityLookingAt(entity, 15.0D);
            if (res != null && res.entityHit instanceof EntityLivingBase) {
                if (!entity.world.isRemote) applyHardFreeze(player, (EntityLivingBase)res.entityHit, 200);
                return true;
            }
            return false;
        }
    }

    public static class NetGathering implements ItemJutsu.IJutsuCallback {
        @Override
        public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
            // Se non c'è il tag NBT, il cooldown è sicuramente 0
            if (stack.hasTagCompound()) {
                long currentTime = entity.world.getTotalWorldTime();
                // La chiave usata dalla mod per i cooldown è solitamente "JutsuCooldown" + indice
                String cooldownKey = "JutsuCooldown" + GATHERING.index;
                if (currentTime < stack.getTagCompound().getLong(cooldownKey)) {
                    return false; // È ancora in cooldown
                }
            }

            RayTraceResult look = ProcedureUtils.raytraceBlocks(entity, 20.0D);
            if (look != null) {
                NBTTagCompound data = entity.getEntityData();
                data.setBoolean("ShadowGatheringActive", !data.getBoolean("ShadowGatheringActive"));
                data.setDouble("GatherPosX", look.hitVec.x);
                data.setDouble("GatherPosY", look.hitVec.y + 1.1);
                data.setDouble("GatherPosZ", look.hitVec.z);

                // Usiamo il metodo setter che avevi già nel codice (che solitamente è pubblico)
                ((ItemShadowRelease)stack.getItem()).setJutsuCooldown(stack, GATHERING, 60L);
                return true;
            }
            return false;
        }
    }

    public static class NetTrap implements ItemJutsu.IJutsuCallback {
        @Override
        public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
            RayTraceResult look = ProcedureUtils.raytraceBlocks(entity, 15.0D);
            if (look != null && !entity.world.isRemote) {
                BlockPos p = look.getBlockPos();
                AxisAlignedBB checkArea = new AxisAlignedBB(p).grow(5);
                if (!entity.world.getEntitiesWithinAABB(EntityShadowKunai.class, checkArea).isEmpty()) {
                    if (entity instanceof EntityPlayer)
                        ((EntityPlayer)entity).sendStatusMessage(new TextComponentString("§cThere is another Trap near!"), true);
                    return false;
                }
                EntityShadowKunai k = new EntityShadowKunai(entity.world, p.getX()+0.5, p.getY()+1, p.getZ()+0.5);
                if (entity instanceof EntityPlayer) k.setOwner((EntityPlayer)entity);
                entity.world.spawnEntity(k);
                return true;
            }
            return false;
        }
    }
}