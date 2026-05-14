package net.luck.narutoaddon.Items;

import net.luck.narutoaddon.LuckTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.narutomod.item.ItemJutsu;
import net.narutomod.procedure.ProcedureUtils;

import java.util.Random;

public class ItemPurpleLightning extends ItemJutsu.Base {

    // Definizioni dei Jutsu
    public static final ItemJutsu.JutsuEnum SHIDEN = new ItemJutsu.JutsuEnum(0, "purple_lightning_bolt", 'S', 400, 150d, new NetShiden());
    public static final ItemJutsu.JutsuEnum NAGASHI = new ItemJutsu.JutsuEnum(1, "purple_lightning_stream", 'A', 300, 100d, new NetNagashi());

    private static final String CHARGE_TAG = "PurpleLightningChargeValue";
    private static final float MAX_CHARGE = 250.0f;

    public ItemPurpleLightning() {
        super(ItemJutsu.JutsuEnum.Type.RAITON, SHIDEN, NAGASHI);
        this.setTranslationKey("purple_lightning");
        this.setRegistryName("purple_lightning");
        this.setCreativeTab(LuckTabs.LUCK_TAB);
    }

    // --- LOGICA DELL'ITEM ---

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        // Avviamo l'uso senza logiche pesanti per evitare desync immediati
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            int duration = this.getMaxItemUseDuration(stack) - count;

            // Iniziamo la carica dopo un brevissimo delay per stabilità
            if (duration > 5) {
                // Calcolo fluido della carica (2.0f per tick fino a 250)
                float charge = Math.min(duration * 2.0f, MAX_CHARGE);

                // Salvataggio NBT
                player.getEntityData().setFloat(CHARGE_TAG, charge);

                if (!player.world.isRemote) {
                    // - Utilizziamo la sincronizzazione della mod base per evitare il flickering
                    entity.getEntityData().setFloat(CHARGE_TAG, charge);
                } else {
                    // Particelle solo lato Client
                    spawnChargeParticles(player, charge, this.getCurrentJutsu(stack) == NAGASHI);
                }
            }
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft) {
        if (!world.isRemote && entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            float power = player.getEntityData().getFloat(CHARGE_TAG);

            // Controllo soglia minima per attivazione
            if (power >= 10.0f) {
                // Esecuzione del jutsu tramite la classe base
                this.executeJutsu(stack, player, power);
            }

            // Reset pulito e sincronizzazione finale
            player.getEntityData().setFloat(CHARGE_TAG, 0.0f);
            entity.getEntityData().setFloat(CHARGE_TAG, power);
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        // Logica di onUpdate rimossa per prevenire conflitti con la carica attiva
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        // Fondamentale: impedisce al braccio di resettarsi quando cambiano i dati NBT (carica)
        return slotChanged;
    }

    // --- EFFETTI VISIVI E UTILITY ---

    private void spawnChargeParticles(EntityLivingBase player, float charge, boolean isNagashi) {
        Random rand = player.world.rand;
        if (isNagashi) {
            float rad = 1.0f + (charge / MAX_CHARGE) * 3.0f;
            for (int i = 0; i < 2; i++) {
                double a = rand.nextDouble() * 2 * Math.PI;
                player.world.spawnParticle(EnumParticleTypes.REDSTONE,
                        player.posX + Math.cos(a) * rad, player.posY + 0.5, player.posZ + Math.sin(a) * rad,
                        0.5D, 0.0D, 0.9D); // Colore violaceo tramite parametri redstone
            }
        } else {
            Vec3d hand = getHandPos(player);
            player.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC, hand.x, hand.y, hand.z, 0, 0.05, 0);
        }
    }

    public static Vec3d getHandPos(EntityLivingBase entity) {
        Vec3d look = entity.getLookVec();
        Vec3d up = new Vec3d(0, 1, 0);
        Vec3d right = look.crossProduct(up).normalize();
        return new Vec3d(entity.posX + look.x * 0.6 - right.x * 0.4,
                entity.posY + entity.getEyeHeight() - 0.3 + look.y * 0.4,
                entity.posZ + look.z * 0.6 - right.z * 0.4);
    }

    public static void applyLightningShock(EntityLivingBase target, int duration) {
        target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, duration, 10));
        target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, duration, 2));
        if (target.world instanceof WorldServer) {
            ((WorldServer)target.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC,
                    target.posX, target.posY + 1, target.posZ, 20, 0.5, 0.5, 0.5, 0.1);
        }
    }

    // --- IMPLEMENTAZIONE JUTSU CALLBACKS ---

    public static class NetShiden implements ItemJutsu.IJutsuCallback {
        @Override
        public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
            // - Uso dei metodi Raytrace forniti da ProcedureUtils
            RayTraceResult res = ProcedureUtils.objectEntityLookingAt(entity, 20.0D);
            if (res != null && res.entityHit instanceof EntityLivingBase) {
                EntityLivingBase target = (EntityLivingBase) res.entityHit;
                target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)entity), 15.0f + (power / 10f));
                applyLightningShock(target, 60);
                return true;
            }
            return false;
        }
    }

    public static class NetNagashi implements ItemJutsu.IJutsuCallback {
        @Override
        public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
            float radius = 4.0f + (power / MAX_CHARGE) * 8.0f;
            AxisAlignedBB area = entity.getEntityBoundingBox().grow(radius);
            boolean hit = false;
            for (EntityLivingBase target : entity.world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
                if (target != entity) {
                    target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)entity), 10.0f);
                    applyLightningShock(target, 40);
                    hit = true;
                }
            }
            return hit;
        }
    }
}