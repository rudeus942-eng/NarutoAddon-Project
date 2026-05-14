
package net.luck.narutoaddon.OtherCode.jutsu;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.narutomod.Chakra;
import net.narutomod.procedure.ProcedureSync.EntityNBTTag;
import net.narutomod.procedure.ProcedureSync.ResetBoundingBox;

public class ShrineHeianCommonHooks {
   @SubscribeEvent
   public void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END && !event.player.world.isRemote && event.player instanceof EntityPlayerMP) {
         EntityPlayerMP player = (EntityPlayerMP)event.player;
         if (!EntityShrineHeianEraTransformation.isActive(player)) {
            player.eyeHeight = player.getDefaultEyeHeight();
            if (player.getEntityData().getBoolean("ShrineHeianEraDoubleJumpUsed") && player.onGround) {
               player.getEntityData().setBoolean("ShrineHeianEraDoubleJumpUsed", false);
            }

         } else if (!player.isDead && player.isEntityAlive() && !EntityShrineHeianEraTransformation.findShrineStack(player).isEmpty()) {
            EntityShrineHeianEraTransformation.keepBodyHidden(player);
            EntityShrineHeianEraTransformation.deactivateConflictingModes(player);
            EntityShrineHeianEraTransformation.applyHeianEffects(player);
            player.eyeHeight = player.getDefaultEyeHeight() * 1.5F;
            if (player.onGround) {
               player.getEntityData().setBoolean("ShrineHeianEraDoubleJumpUsed", false);
            }

            this.notifyDoubleJumpReady(player);
            if (player.ticksExisted % 20 == 0) {
               Chakra.Pathway pathway = Chakra.pathway(player);
               if (pathway == null || pathway.getAmount() < (double)5.0F) {
                  player.sendStatusMessage(new TextComponentString("§cHeian Era Transformation ended."), true);
                  EntityShrineHeianEraTransformation.deactivate(player);
                  return;
               }

               pathway.consume((double)5.0F);
            }

         } else {
            EntityShrineHeianEraTransformation.deactivate(player);
         }
      }
   }

   @SubscribeEvent
   public void onLivingDeath(LivingDeathEvent event) {
      if (!event.getEntity().world.isRemote && event.getEntityLiving() instanceof EntityPlayerMP && EntityShrineHeianEraTransformation.isActive(event.getEntityLiving())) {
         EntityShrineHeianEraTransformation.deactivate((EntityPlayerMP)event.getEntityLiving());
      }

   }

   @SubscribeEvent
   public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
      if (!event.player.world.isRemote && event.player instanceof EntityPlayerMP) {
         EntityPlayerMP player = (EntityPlayerMP)event.player;
         if (EntityShrineHeianEraTransformation.isActive(player) && !EntityShrineHeianEraTransformation.findShrineStack(player).isEmpty()) {
            this.reapplyPersistentState(player);
         }
      }

   }

   @SubscribeEvent
   public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
      if (!event.player.world.isRemote && event.player instanceof EntityPlayerMP) {
         EntityPlayerMP player = (EntityPlayerMP)event.player;
         if (EntityShrineHeianEraTransformation.isActive(player) && !EntityShrineHeianEraTransformation.findShrineStack(player).isEmpty()) {
            this.reapplyPersistentState(player);
         }
      }

   }

   @SubscribeEvent
   public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
      if (!event.player.world.isRemote && event.player instanceof EntityPlayerMP && EntityShrineHeianEraTransformation.isActive(event.player)) {
         EntityShrineHeianEraTransformation.deactivate((EntityPlayerMP)event.player);
      }

   }

   @SubscribeEvent
   public void onStartTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) {
      if (event.getEntityPlayer() instanceof EntityPlayerMP && event.getTarget() instanceof EntityPlayerMP) {
         EntityPlayerMP viewer = (EntityPlayerMP)event.getEntityPlayer();
         EntityPlayerMP target = (EntityPlayerMP)event.getTarget();
         if (EntityShrineHeianEraTransformation.isActive(target)) {
            if (EntityShrineHeianEraTransformation.findShrineStack(target).isEmpty()) {
               EntityShrineHeianEraTransformation.deactivate(target);
            } else {
               this.syncActiveStateToViewer(target, viewer);
            }
         }
      }
   }

   private void reapplyPersistentState(EntityPlayerMP player) {
      player.getEntityData().setBoolean("ShrineHeianEraDoubleJumpUsed", false);
      if (!player.getEntityData().hasKey("ShrineHeianEraDoubleJumpCd")) {
         player.getEntityData().setInteger("ShrineHeianEraDoubleJumpCd", 0);
      }

      EntityNBTTag.sendToSelf(player, "ShrineHeianEraActive", true);
      EntityNBTTag.sendToTracking(player, "ShrineHeianEraActive", true);
      EntityShrineHeianEraTransformation.setPlayerScale(player, true);
      EntityShrineHeianEraTransformation.keepBodyHidden(player);
      EntityShrineHeianEraTransformation.applyHeianEffects(player);
   }

   private void notifyDoubleJumpReady(EntityPlayerMP player) {
      int readyTick = player.getEntityData().getInteger("ShrineHeianEraDoubleJumpCd");
      if (readyTick > 0) {
         if (player.ticksExisted < readyTick) {
            player.getEntityData().setBoolean("ShrineHeianEraDoubleJumpReadySound", false);
         } else if (!player.getEntityData().getBoolean("ShrineHeianEraDoubleJumpReadySound")) {
            player.getEntityData().setBoolean("ShrineHeianEraDoubleJumpReadySound", true);
            player.connection.sendPacket(new SPacketSoundEffect(SoundEvents.BLOCK_NOTE_PLING, SoundCategory.PLAYERS, player.posX, player.posY + (double)player.height * (double)0.5F, player.posZ, 0.45F, 1.75F));
         }
      }
   }

   private void syncActiveStateToViewer(EntityPlayerMP target, EntityPlayerMP viewer) {
      EntityShrineHeianEraTransformation.setPlayerScale(target, true, false);
      EntityNBTTag.sendTo(viewer, target, "ShrineHeianEraActive", true);
      ResetBoundingBox.sendToPlayer(target, viewer);
      EntityShrineHeianEraTransformation.keepBodyHidden(target);
   }
}
