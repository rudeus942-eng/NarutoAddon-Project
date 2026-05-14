
package net.luck.narutoaddon.OtherCode.raid.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class KnockbackHelper {
   private static final double STEP_SIZE = 0.4;
   private static final double WALL_GAP = 0.3;

   private KnockbackHelper() {
   }

   public static void applyWallSafeKnockback(Entity entity, double knockbackX, double knockbackY, double knockbackZ) {
      World world = entity.world;
      double horizDist = Math.sqrt(knockbackX * knockbackX + knockbackZ * knockbackZ);
      if (horizDist < 0.01) {
         entity.motionY += knockbackY;
         if (entity instanceof EntityPlayer) {
            ((EntityPlayer)entity).velocityChanged = true;
         }

      } else {
         double dirX = knockbackX / horizDist;
         double dirZ = knockbackZ / horizDist;
         double safeDistance = horizDist;
         double entityHeight = (double)entity.height;
         double startX = entity.posX;
         double startY = entity.posY;
         double startZ = entity.posZ;

         for(double d = 0.4; d <= horizDist + 0.01; d += 0.4) {
            double checkX = startX + dirX * d;
            double checkZ = startZ + dirZ * d;
            BlockPos feetPos = new BlockPos(checkX, startY, checkZ);
            BlockPos bodyPos = new BlockPos(checkX, startY + entityHeight * (double)0.5F, checkZ);
            BlockPos headPos = new BlockPos(checkX, startY + entityHeight - 0.1, checkZ);
            if (isSolidBlock(world, feetPos) || isSolidBlock(world, bodyPos) || isSolidBlock(world, headPos)) {
               safeDistance = Math.max((double)0.0F, d - 0.4 - 0.3);
               break;
            }
         }

         if (safeDistance > 0.01) {
            double scale = safeDistance / horizDist;
            entity.motionX += knockbackX * scale;
            entity.motionZ += knockbackZ * scale;
         }

         entity.motionY += Math.min(knockbackY, (double)1.0F);
         if (entity instanceof EntityPlayer) {
            ((EntityPlayer)entity).velocityChanged = true;
         }

      }
   }

   private static boolean isSolidBlock(World world, BlockPos pos) {
      IBlockState state = world.getBlockState(pos);
      return state.getMaterial().blocksMovement() && state.isFullBlock();
   }
}
