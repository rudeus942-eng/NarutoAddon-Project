
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.narutomod.Particles;
import net.narutomod.Particles.Types;
import net.narutomod.item.ItemJutsu;

import java.util.Random;
import java.util.UUID;

@EventBusSubscriber(
   modid = "inftsukaddon"
)
public class TwinLionModeHandler {
   public static final String NBT_KEY = "inftsuk_blue_amp_palm_active";
   public static final String NBT_EXPIRY_KEY = "inftsuk_blue_amp_palm_expiry";
   public static final UUID ATTACK_MOD_UUID = UUID.fromString("7e12ab50-1234-4567-89ab-cdef01234567");
   public static final String ATTACK_MOD_NAME = "blue_amp_palm.damage";
   public static final double ATTACK_MOD_AMOUNT = (double)60.0F;
   public static final int DURATION_TICKS = 600;
   private static final int[] LION_PALETTE = new int[]{-2035457, -5583617, -10048769, -13399826, -15636788};

   public static boolean isActive(EntityPlayer player) {
      return player.getEntityData().getBoolean("inftsuk_blue_amp_palm_active");
   }

   public static void setActive(EntityPlayer player, boolean active) {
      NBTTagCompound data = player.getEntityData();
      if (active) {
         data.setBoolean("inftsuk_blue_amp_palm_active", true);
         data.setLong("inftsuk_blue_amp_palm_expiry", player.world.getTotalWorldTime() + 600L);
         applyModifier(player);
      } else {
         data.removeTag("inftsuk_blue_amp_palm_active");
         data.removeTag("inftsuk_blue_amp_palm_expiry");
         removeModifier(player);
      }

   }

   private static void applyModifier(EntityPlayer player) {
      IAttributeInstance atk = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
      if (atk.getModifier(ATTACK_MOD_UUID) != null) {
         atk.removeModifier(ATTACK_MOD_UUID);
      }

      atk.applyModifier(new AttributeModifier(ATTACK_MOD_UUID, "blue_amp_palm.damage", (double)60.0F, 0));
   }

   private static void removeModifier(EntityPlayer player) {
      IAttributeInstance atk = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
      if (atk.getModifier(ATTACK_MOD_UUID) != null) {
         atk.removeModifier(ATTACK_MOD_UUID);
      }

   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         EntityPlayer player = event.player;
         if (!isActive(player)) {
            if (!player.world.isRemote && player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getModifier(ATTACK_MOD_UUID) != null) {
               removeModifier(player);
            }

         } else {
            if (!player.world.isRemote) {
               NBTTagCompound data = player.getEntityData();
               long expiry = data.getLong("inftsuk_blue_amp_palm_expiry");
               if (expiry == 0L) {
                  expiry = player.world.getTotalWorldTime() + 600L;
                  data.setLong("inftsuk_blue_amp_palm_expiry", expiry);
               }

               if (player.world.getTotalWorldTime() >= expiry) {
                  setActive(player, false);
                  player.sendMessage(new TextComponentString("§8§lBlue Amplified Palm expired"));
                  player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5F, 0.9F);
                  return;
               }
            }

            if (!player.world.isRemote && player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getModifier(ATTACK_MOD_UUID) == null) {
               applyModifier(player);
            }

            if (!player.world.isRemote && player.world instanceof WorldServer) {
               WorldServer ws = (WorldServer)player.world;
               spawnHandAura(ws, player);
            }

         }
      }
   }

   private static void spawnHandAura(WorldServer world, EntityPlayer player) {
      Vec3d look = player.getLookVec();
      Vec3d right = (new Vec3d(-look.z, (double)0.0F, look.x)).normalize();
      double bodyY = player.posY + (double)player.getEyeHeight() - 0.9;
      double rx = player.posX + look.x * 0.35 + right.x * 0.42;
      double ry = bodyY + look.y * (double)0.25F;
      double rz = player.posZ + look.z * 0.35 + right.z * 0.42;
      emitAura(world, rx, ry, rz);
      double lx = player.posX + look.x * 0.35 - right.x * 0.42;
      double ly = bodyY + look.y * (double)0.25F;
      double lz = player.posZ + look.z * 0.35 - right.z * 0.42;
      emitAura(world, lx, ly, lz);
      if (player.ticksExisted % 5 == 0) {
         Particles.spawnParticle(world, Types.SMOKE, player.posX, player.posY + 0.9, player.posZ, 3, 0.35, 0.9, 0.35, (double)0.0F, 0.02, (double)0.0F, new int[]{-1067013377, 18, 20, 240});
      }

   }

   private static void emitAura(WorldServer world, double x, double y, double z) {
      Random rand = world.rand;
      int c1 = LION_PALETTE[rand.nextInt(LION_PALETTE.length)];
      Particles.spawnParticle(world, Types.SMOKE, x, y, z, 2, 0.08, 0.12, 0.08, (rand.nextDouble() - (double)0.5F) * 0.03, 0.03, (rand.nextDouble() - (double)0.5F) * 0.03, new int[]{c1, 14, 14, 240});
      if (rand.nextInt(3) == 0) {
         int c2 = LION_PALETTE[rand.nextInt(LION_PALETTE.length)];
         Particles.spawnParticle(world, Types.FALLING_DUST, x, y, z, 1, 0.1, 0.1, 0.1, (rand.nextDouble() - (double)0.5F) * 0.06, 0.1, (rand.nextDouble() - (double)0.5F) * 0.06, new int[]{c2, 240, 10});
      }

   }

   @SubscribeEvent
   public static void onPlayerDeath(LivingDeathEvent event) {
      if (event.getEntityLiving() instanceof EntityPlayer) {
         setActive((EntityPlayer)event.getEntityLiving(), false);
      }

   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (entity.world.isRemote) {
            return false;
         } else if (power < 0.3F) {
            return false;
         } else if (!(entity instanceof EntityPlayer)) {
            return false;
         } else {
            EntityPlayer player = (EntityPlayer)entity;
            boolean nowActive = !TwinLionModeHandler.isActive(player);
            TwinLionModeHandler.setActive(player, nowActive);
            if (nowActive) {
               player.sendMessage(new TextComponentString("§9§l★ Blue Amplified Palm §aACTIVATED §8(30s)"));
               player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.6F, 1.7F);
            } else {
               player.sendMessage(new TextComponentString("§8§lBlue Amplified Palm deactivated"));
               player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5F, 0.9F);
            }

            return true;
         }
      }

      public float getBasePower() {
         return 0.3F;
      }

      public float getPowerupDelay() {
         return 20.0F;
      }

      public float getMaxPower() {
         return 10.0F;
      }
   }
}
