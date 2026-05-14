
package net.luck.narutoaddon.OtherCode.keybind;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.InfTsukAddon;
import net.luck.narutoaddon.OtherCode.SubstitutionCooldownOverlay;
import net.luck.narutoaddon.OtherCode.entity.EntitySubstitutionClone;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

@ElementsInfTsukAddon.ModElement.Tag
public class KeyBindingSubstitute extends ElementsInfTsukAddon.ModElement {
   @SideOnly(Side.CLIENT)
   private static KeyBinding keys;

   public KeyBindingSubstitute(ElementsInfTsukAddon instance) {
      super(instance, 2);
   }

   public void preInit(FMLPreInitializationEvent event) {
      this.elements.addNetworkMessage(KeyBindingPressedMessageHandler.class, KeyBindingPressedMessage.class, Side.SERVER);
   }

   @SideOnly(Side.CLIENT)
   public void init(FMLInitializationEvent event) {
      keys = new KeyBinding("key.mcreator.substitute", 45, "key.categories.misc");
      ClientRegistry.registerKeyBinding(keys);
      MinecraftForge.EVENT_BUS.register(new KeyEventHandler());
   }

   @SideOnly(Side.CLIENT)
   public static class KeyEventHandler {
      @SubscribeEvent
      public void onClientTick(TickEvent.ClientTickEvent event) {
         if (event.phase == Phase.END) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.player != null && mc.world != null && KeyBindingSubstitute.keys != null && KeyBindingSubstitute.keys.isPressed()) {
               InfTsukAddon.PACKET_HANDLER.sendToServer(new KeyBindingPressedMessage());
            }
         }

      }
   }

   public static class KeyBindingPressedMessageHandler implements IMessageHandler<KeyBindingPressedMessage, IMessage> {
      public IMessage onMessage(KeyBindingPressedMessage message, MessageContext context) {
         EntityPlayerMP player = context.getServerHandler().player;
         player.getServerWorld().addScheduledTask(() -> this.executeSubstitutionJutsu(player));
         return null;
      }

      private void executeSubstitutionJutsu(EntityPlayerMP player) {
         World world = player.world;
         long currentTime = world.getTotalWorldTime();
         long lastUse = player.getEntityData().getLong("SubstitutionCooldown");
         if (currentTime - lastUse >= 600L) {
            double origX = player.posX;
            double origY = player.posY;
            double origZ = player.posZ;
            Vec3d startVec = new Vec3d(player.posX, player.posY + (double)player.getEyeHeight(), player.posZ);
            Vec3d lookVec = player.getLookVec();
            Vec3d endVec = new Vec3d(startVec.x + lookVec.x * (double)30.0F, startVec.y + lookVec.y * (double)30.0F, startVec.z + lookVec.z * (double)30.0F);
            RayTraceResult rayTrace = world.rayTraceBlocks(startVec, endVec, false, true, false);
            double teleportX;
            double teleportY;
            double teleportZ;
            if (rayTrace != null && rayTrace.typeOfHit == Type.BLOCK) {
               Vec3d hitVec = rayTrace.hitVec;
               teleportX = hitVec.x - lookVec.x * (double)1.0F;
               teleportY = hitVec.y - lookVec.y * (double)1.0F;
               teleportZ = hitVec.z - lookVec.z * (double)1.0F;
               BlockPos teleportPos = new BlockPos(teleportX, teleportY, teleportZ);
               if (!this.isValidTeleportPosition(world, teleportPos)) {
                  teleportPos = this.findNearestValidPosition(world, teleportPos);
                  if (teleportPos == null) {
                     return;
                  }

                  teleportX = (double)teleportPos.getX() + (double)0.5F;
                  teleportY = (double)teleportPos.getY();
                  teleportZ = (double)teleportPos.getZ() + (double)0.5F;
               }
            } else {
               teleportX = startVec.x + lookVec.x * (double)30.0F;
               teleportY = startVec.y + lookVec.y * (double)30.0F;
               teleportZ = startVec.z + lookVec.z * (double)30.0F;
               BlockPos teleportPos = new BlockPos(teleportX, teleportY, teleportZ);
               if (!this.isValidTeleportPosition(world, teleportPos)) {
                  teleportPos = this.findNearestValidPosition(world, teleportPos);
                  if (teleportPos == null) {
                     return;
                  }

                  teleportX = (double)teleportPos.getX() + (double)0.5F;
                  teleportY = (double)teleportPos.getY();
                  teleportZ = (double)teleportPos.getZ() + (double)0.5F;
               }
            }

            if (!this.hasBarrierInPath(world, startVec, new Vec3d(teleportX, teleportY, teleportZ))) {
               player.getEntityData().setLong("SubstitutionCooldown", currentTime);
               long cooldownEndTime = currentTime + 600L;
               InfTsukAddon.PACKET_HANDLER.sendTo(new SubstitutionCooldownOverlay.CooldownSyncMessage(cooldownEndTime), player);

               for(Potion potion : new ArrayList(player.getActivePotionMap().keySet())) {
                  player.removePotionEffect(potion);
               }

               EntitySubstitutionClone.EntityCustom clone = new EntitySubstitutionClone.EntityCustom(world);
               clone.setPosition(origX, origY, origZ);
               clone.setOwnerUUID(player.getUniqueID().toString());
               clone.setOwnerName(player.getName());
               clone.copyTeamFromOwner(player);
               world.spawnEntity(clone);
               player.setPositionAndUpdate(teleportX, teleportY, teleportZ);
               this.spawnSmokeParticles((WorldServer)world, teleportX, teleportY, teleportZ);
            }
         }
      }

      private boolean isValidTeleportPosition(World world, BlockPos pos) {
         Block blockAtPos = world.getBlockState(pos).getBlock();
         Block blockAbove = world.getBlockState(pos.up()).getBlock();
         boolean positionClear = !world.getBlockState(pos).isFullBlock() && !world.getBlockState(pos.up()).isFullBlock();
         boolean noBarrier = blockAtPos != Blocks.BARRIER && blockAbove != Blocks.BARRIER;
         return positionClear && noBarrier;
      }

      private BlockPos findNearestValidPosition(World world, BlockPos targetPos) {
         for(int radius = 1; radius <= 5; ++radius) {
            for(int x = -radius; x <= radius; ++x) {
               for(int y = -radius; y <= radius; ++y) {
                  for(int z = -radius; z <= radius; ++z) {
                     BlockPos checkPos = targetPos.add(x, y, z);
                     if (this.isValidTeleportPosition(world, checkPos)) {
                        return checkPos;
                     }
                  }
               }
            }
         }

         return null;
      }

      private boolean hasBarrierInPath(World world, Vec3d start, Vec3d end) {
         Vec3d direction = end.subtract(start).normalize();
         double distance = start.distanceTo(end);

         for(double d = (double)0.0F; d < distance; d += (double)0.5F) {
            Vec3d checkPoint = new Vec3d(start.x + direction.x * d, start.y + direction.y * d, start.z + direction.z * d);
            BlockPos checkPos = new BlockPos(checkPoint);
            if (world.getBlockState(checkPos).getBlock() == Blocks.BARRIER) {
               return true;
            }
         }

         return false;
      }

      private void spawnSmokeParticles(WorldServer world, double x, double y, double z) {
         for(int i = 0; i < 20; ++i) {
            double offsetX = (world.rand.nextDouble() - (double)0.5F) * (double)2.0F;
            double offsetY = world.rand.nextDouble() * (double)2.0F;
            double offsetZ = (world.rand.nextDouble() - (double)0.5F) * (double)2.0F;
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, false, x + offsetX, y + offsetY, z + offsetZ, 5, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
            world.spawnParticle(EnumParticleTypes.CLOUD, false, x + offsetX, y + offsetY, z + offsetZ, 5, (double)0.5F, (double)0.5F, (double)0.5F, 0.02, new int[0]);
         }

         world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, false, x, y + (double)1.0F, z, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, new int[0]);
      }
   }

   public static class KeyBindingPressedMessage implements IMessage {
      public void toBytes(ByteBuf buf) {
      }

      public void fromBytes(ByteBuf buf) {
      }
   }
}
