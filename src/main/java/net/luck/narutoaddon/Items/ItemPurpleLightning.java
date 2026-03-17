package net.luck.narutoaddon.Items;

import net.luck.narutoaddon.LuckTabs;
import net.minecraft.entity.Entity;
import net.narutomod.Chakra;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.narutomod.item.ItemJutsu;
import java.util.List;
import java.util.Random;

public class ItemPurpleLightning extends ItemJutsu.Base {

    public static final ItemJutsu.JutsuEnum SHIDEN = new ItemJutsu.JutsuEnum(0, "purple_lightning_bolt", 'S', 150d, new NetShiden());
    public static final ItemJutsu.JutsuEnum NAGASHI = new ItemJutsu.JutsuEnum(1, "purple_lightning_stream", 'A', 100d, new NetNagashi());
    private static final String CHARGE_TAG = "PurpleLightningChargeValue";

    public ItemPurpleLightning() {
        super(ItemJutsu.JutsuEnum.Type.RAITON, SHIDEN, NAGASHI);
        this.setTranslationKey("purple_lightning");
        this.setRegistryName("purple_lightning");
        this.setCreativeTab(LuckTabs.LUCK_TAB);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.BOW; }
    @Override
    public int getMaxItemUseDuration(ItemStack stack) { return 72000; }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        player.getEntityData().setFloat(CHARGE_TAG, 0.0f);
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        int duration = this.getMaxItemUseDuration(stack) - count;
        if (duration > 10) {
            float charge = Math.min((duration - 10) * 1.5f, 250f);
            player.getEntityData().setFloat(CHARGE_TAG, charge);
            if (player.world.isRemote) {
                Random rand = player.world.rand;
                if (this.getCurrentJutsu(stack) == NAGASHI) {
                    float rad = 5.0f + (charge / 250.0f) * 45.0f;
                    // Mapping: addVector
                    Vec3d center = player.getPositionVector().add(0, 0.5, 0);
                    for (int i = 0; i < 5; i++) {
                        double a = rand.nextDouble() * 2 * Math.PI;
                        player.world.spawnParticle(EnumParticleTypes.REDSTONE, player.posX + Math.cos(a) * rad, player.posY + 0.2, player.posZ + Math.sin(a) * rad, 0.6D, 0.0D, 0.9D);
                    }
                }
            }
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft) {
        if (!world.isRemote && entity != null) {
            float power = entity.getEntityData().getFloat(CHARGE_TAG);
            ItemJutsu.JutsuEnum current = this.getCurrentJutsu(stack);
            if (current == SHIDEN) new NetShiden().createJutsu(stack, entity, power);
            else if (current == NAGASHI) new NetNagashi().createJutsu(stack, entity, power);
        }
    }

    public static Vec3d getHandPos(EntityLivingBase entity) {
        Vec3d look = entity.getLookVec();
        if (look == null) return entity.getPositionVector().add(0, entity.getEyeHeight(), 0);
        Vec3d up = new Vec3d(0, 1, 0);
        // Mapping: lengthVector
        Vec3d right = look.crossProduct(up).length() < 0.01D ? look.crossProduct(new Vec3d(1, 0, 0)) : look.crossProduct(up);
        right = right.normalize();
        return new Vec3d(entity.posX + look.x * 0.6 - right.x * 0.4, entity.posY + entity.getEyeHeight() - 0.3 + look.y * 0.4, entity.posZ + look.z * 0.6 - right.z * 0.4);
    }

    public static class NetShiden implements ItemJutsu.IJutsuCallback {
        @Override
        public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
            Vec3d start = getHandPos(entity);
            Vec3d look = entity.getLookVec();
            double range = 25.0D;
            // Mapping: addVector
            Vec3d end = start.add(look.x * range, look.y * range, look.z * range);
            if (entity instanceof EntityPlayer) {
                Chakra.Pathway pathway = Chakra.pathway((EntityPlayer)entity);
                if (pathway == null || !pathway.consume(150d + (power * 1.5d))) return false;
            }
            return true;
        }
    }

    public static class NetNagashi implements ItemJutsu.IJutsuCallback {
        @Override
        public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
            float radius = 5.0f + (power / 250.0f) * 45.0f;
            // Mapping: addVector
            Vec3d center = entity.getPositionVector().add(0, 1, 0);
            return true;
        }
    }
}