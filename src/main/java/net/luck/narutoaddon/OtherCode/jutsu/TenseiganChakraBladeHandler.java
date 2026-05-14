
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
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

import java.util.UUID;

@EventBusSubscriber(
   modid = "inftsukaddon"
)
public class TenseiganChakraBladeHandler {
   public static final String NBT_KEY = "inftsuk_tenseigan_blade_active";
   public static final String NBT_EXPIRY_KEY = "inftsuk_tenseigan_blade_expiry";
   public static final UUID ATTACK_MOD_UUID = UUID.fromString("c9a75b42-7ae3-48f1-8d21-0fe4b31c5a77");
   public static final String ATTACK_MOD_NAME = "tenseigan_blade.damage";
   public static final double ATTACK_MOD_AMOUNT = (double)60.0F;
   public static final int DURATION_TICKS = 600;
   private static final int[] BLADE_PALETTE = new int[]{-1306, -7286, -14261, -5313537, -8399105};
   public static final double BLADE_LENGTH = 1.6;

   public static boolean isActive(EntityPlayer player) {
      return player.getEntityData().getBoolean("inftsuk_tenseigan_blade_active");
   }

   public static void setActive(EntityPlayer player, boolean active) {
      NBTTagCompound data = player.getEntityData();
      if (active) {
         data.setBoolean("inftsuk_tenseigan_blade_active", true);
         data.setLong("inftsuk_tenseigan_blade_expiry", player.world.getTotalWorldTime() + 600L);
         applyModifier(player);
      } else {
         data.removeTag("inftsuk_tenseigan_blade_active");
         data.removeTag("inftsuk_tenseigan_blade_expiry");
         removeModifier(player);
      }

   }

   private static void applyModifier(EntityPlayer player) {
      IAttributeInstance atk = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
      if (atk.getModifier(ATTACK_MOD_UUID) != null) {
         atk.removeModifier(ATTACK_MOD_UUID);
      }

      atk.applyModifier(new AttributeModifier(ATTACK_MOD_UUID, "tenseigan_blade.damage", (double)60.0F, 0));
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
               long expiry = data.getLong("inftsuk_tenseigan_blade_expiry");
               if (expiry == 0L) {
                  expiry = player.world.getTotalWorldTime() + 600L;
                  data.setLong("inftsuk_tenseigan_blade_expiry", expiry);
               }

               if (player.world.getTotalWorldTime() >= expiry) {
                  setActive(player, false);
                  player.sendMessage(new TextComponentString("§e§lTenseigan Chakra Blade expired"));
                  player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5F, 0.9F);
                  return;
               }
            }

            if (!player.world.isRemote && player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getModifier(ATTACK_MOD_UUID) == null) {
               applyModifier(player);
            }

            if (!player.world.isRemote && player.world instanceof WorldServer) {
               spawnBladeAura((WorldServer)player.world, player);
            }

         }
      }
   }

   private static void spawnBladeAura(WorldServer world, EntityPlayer player) {
      Vec3d look = player.getLookVec();
      Vec3d right = (new Vec3d(-look.z, (double)0.0F, look.x)).normalize();
      double handY = player.posY + (double)player.getEyeHeight() - 0.85;
      Vec3d bladeDir = (new Vec3d(look.x, look.y + 0.8, look.z)).normalize();
      double fwd = 0.4;
      double lat = (double)0.5F;
      double rsx = player.posX + look.x * fwd + right.x * lat;
      double rsz = player.posZ + look.z * fwd + right.z * lat;
      emitGoldChakraFlow(world, rsx, handY, rsz, rsx + bladeDir.x * 1.6, handY + bladeDir.y * 1.6, rsz + bladeDir.z * 1.6);
      double lsx = player.posX + look.x * fwd - right.x * lat;
      double lsz = player.posZ + look.z * fwd - right.z * lat;
      emitGoldChakraFlow(world, lsx, handY, lsz, lsx + bladeDir.x * 1.6, handY + bladeDir.y * 1.6, lsz + bladeDir.z * 1.6);
   }

   private static void emitGoldChakraFlow(WorldServer world, double sx, double sy, double sz, double ex, double ey, double ez) {
      double vx = (ex - sx) * 0.6;
      double vy = (ey - sy) * 0.6;
      double vz = (ez - sz) * 0.6;
      Particles.spawnParticle(world, Types.SMOKE, sx, sy, sz, 12, 0.06, 0.06, 0.06, vx * 0.35, vy * 0.35, vz * 0.35, new int[]{-2130713718, 16, 5, 240});
      Particles.spawnParticle(world, Types.SMOKE, sx, sy, sz, 10, 0.05, 0.05, 0.05, vx * 0.65, vy * 0.65, vz * 0.65, new int[]{-2130713718, 10, 5, 240});
      Particles.spawnParticle(world, Types.SMOKE, sx, sy, sz, 8, 0.04, 0.04, 0.04, vx * 0.95, vy * 0.95, vz * 0.95, new int[]{-2130713718, 7, 5, 240});
   }

   @SubscribeEvent
   public static void onPlayerDeath(LivingDeathEvent event) {
      if (event.getEntityLiving() instanceof EntityPlayer) {
         setActive((EntityPlayer)event.getEntityLiving(), false);
      }

   }

   public static void toggle(EntityPlayer player) {
      if (!player.world.isRemote) {
         boolean nowActive = !isActive(player);
         setActive(player, nowActive);
         if (nowActive) {
            player.sendMessage(new TextComponentString("§e§l★ Tenseigan Chakra Blade §aACTIVATED §8(30s)"));
            player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.6F, 1.9F);
         } else {
            player.sendMessage(new TextComponentString("§8§lTenseigan Chakra Blade deactivated"));
            player.world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5F, 0.9F);
         }

      }
   }
}
