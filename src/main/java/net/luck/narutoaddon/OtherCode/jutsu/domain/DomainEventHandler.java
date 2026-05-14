
package net.luck.narutoaddon.OtherCode.jutsu.domain;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(
   modid = "inftsukaddon"
)
public class DomainEventHandler {
   @SubscribeEvent
   public static void onWorldLoad(WorldEvent.Load event) {
      World world = event.getWorld();
      if (!world.isRemote) {
         if (world instanceof WorldServer) {
            WorldServer server = (WorldServer)world;

            try {
               DomainBlockManager.performCrashRecovery(server);
            } catch (Throwable t) {
               System.err.println("[InfTsukAddon/Domain] Crash recovery failed for dimension " + server.provider.getDimension());
               t.printStackTrace();
            }

            try {
               DomainSavedData data = DomainSavedData.get(server);
               DomainRegistry reg = DomainRegistry.forWorld(server);
               long now = server.getTotalWorldTime();

               for(NBTTagCompound nbt : data.takeOrphanedData()) {
                  try {
                     DomainInstance d = DomainInstance.deserializeNBT(nbt);
                     if (d.isExpired(now)) {
                        d.cleanup(server);
                     } else {
                        reg.add(d);
                        data.addDomain(d);
                     }
                  } catch (Throwable t) {
                     System.err.println("[InfTsukAddon/Domain] Failed to rehydrate a saved domain");
                     t.printStackTrace();
                  }
               }
            } catch (Throwable t) {
               System.err.println("[InfTsukAddon/Domain] Domain registry rehydrate failed");
               t.printStackTrace();
            }

         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (mcServer != null) {
            if (mcServer.getTickCounter() % 20 == 0) {
               for(WorldServer ws : mcServer.worlds) {
                  DomainRegistry reg = DomainRegistry.forWorld(ws);
                  long now = ws.getTotalWorldTime();
                  List<DomainInstance> expired = new ArrayList();

                  for(DomainInstance d : reg.all()) {
                     if (d.isExpired(now)) {
                        expired.add(d);
                     }
                  }

                  for(DomainInstance d : expired) {
                     try {
                        d.setPhase(DomainInstance.DomainPhase.ENDED);
                        d.cleanup(ws);
                        reg.remove(d.getId());
                        System.out.println("[InfTsukAddon/Domain] Force-expired domain " + d.getId() + " (anchor entity likely unloaded with absent caster)");
                     } catch (Throwable t) {
                        t.printStackTrace();
                     }
                  }
               }

            }
         }
      }
   }

   public static DomainInstance findDomainForWall(WorldServer server, BlockPos pos) {
      DomainRegistry reg = DomainRegistry.forWorld(server);
      DomainInstance target = null;
      double bestDist = Double.MAX_VALUE;

      for(DomainInstance d : reg.all()) {
         double dx = (double)pos.getX() + (double)0.5F - ((double)d.getCenter().getX() + (double)0.5F);
         double dy = (double)pos.getY() + (double)0.5F - (double)d.getCenter().getY();
         double dz = (double)pos.getZ() + (double)0.5F - ((double)d.getCenter().getZ() + (double)0.5F);
         double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (dist <= (double)(d.getRadius() + 3) && dist < bestDist) {
            bestDist = dist;
            target = d;
         }
      }

      return target;
   }

   public static void applyWallDamage(WorldServer server, BlockPos pos, DomainInstance target, Vec3d attackerPos, int damage, EntityPlayer feedbackPlayer) {
      if (target.isInsideDomain(attackerPos)) {
         if (feedbackPlayer != null) {
            feedbackPlayer.sendStatusMessage(new TextComponentString("§9The domain wall cannot be damaged from inside."), true);
         }

      } else {
         target.damageWall(damage);
         server.playSound((EntityPlayer)null, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, SoundEvents.BLOCK_GLASS_HIT, SoundCategory.BLOCKS, 0.9F, 0.6F + (float)Math.random() * 0.2F);
         server.spawnParticle(EnumParticleTypes.CRIT, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, 12, 0.35, 0.35, 0.35, 0.06, new int[0]);
         server.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, 6, 0.3, 0.3, 0.3, 0.02, new int[0]);
         int hp = target.getWallHP();
         int max = target.getMaxWallHP();
         if (feedbackPlayer != null) {
            int pct = (int)Math.round((double)100.0F * (double)hp / (double)max);
            feedbackPlayer.sendStatusMessage(new TextComponentString("§9Domain Wall: §b" + hp + "§7/§b" + max + " §8(" + pct + "%)"), true);
         }

         if (hp <= 0) {
            target.forceCollapse(server.getTotalWorldTime());
            server.playSound((EntityPlayer)null, target.getCenter(), SoundEvents.ENTITY_ENDERDRAGON_HURT, SoundCategory.PLAYERS, 1.4F, 0.6F);
            TextComponentString shatter = new TextComponentString("§9§l✦ The domain wall shatters!");

            for(EntityPlayer p : server.playerEntities) {
               p.sendMessage(shatter);
            }
         }

      }
   }

   @SubscribeEvent
   public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
      World world = event.getWorld();
      if (!world.isRemote) {
         if (world instanceof WorldServer) {
            WorldServer server = (WorldServer)world;
            BlockPos pos = event.getPos();
            Block b = world.getBlockState(pos).getBlock();
            if (b == BlockDomainSky.block) {
               EntityPlayer player = event.getEntityPlayer();
               if (player != null) {
                  DomainInstance target = findDomainForWall(server, pos);
                  if (target == null) {
                     event.setCanceled(true);
                  } else {
                     event.setCanceled(true);
                     if (player.capabilities.isCreativeMode) {
                        target.damageWall(target.getMaxWallHP());
                        target.forceCollapse(world.getTotalWorldTime());
                     } else {
                        double atk = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                        int damage = (int)Math.max((double)1.0F, Math.floor(atk));
                        Vec3d playerPos = new Vec3d(player.posX, player.posY + (double)player.getEyeHeight(), player.posZ);
                        applyWallDamage(server, pos, target, playerPos, damage, player);
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onProjectileImpact(ProjectileImpactEvent event) {
      Entity proj = event.getEntity();
      if (proj != null) {
         World world = proj.world;
         if (!world.isRemote) {
            if (world instanceof WorldServer) {
               WorldServer server = (WorldServer)world;
               RayTraceResult rtr = event.getRayTraceResult();
               if (rtr != null && rtr.typeOfHit == Type.BLOCK) {
                  BlockPos pos = rtr.getBlockPos();
                  if (world.getBlockState(pos).getBlock() == BlockDomainSky.block) {
                     DomainInstance target = findDomainForWall(server, pos);
                     if (target != null) {
                        Entity shooter = null;
                        if (proj instanceof EntityThrowable) {
                           shooter = ((EntityThrowable)proj).getThrower();
                        } else if (proj instanceof EntityArrow) {
                           shooter = ((EntityArrow)proj).shootingEntity;
                        }

                        Vec3d attackerPos = shooter != null ? shooter.getPositionVector() : proj.getPositionVector();
                        EntityPlayer feedback = shooter instanceof EntityPlayer ? (EntityPlayer)shooter : null;
                        int damage = 30;
                        if (proj instanceof EntityArrow) {
                           damage = 20;
                        } else if (proj instanceof EntityFireball) {
                           damage = 80;
                        }

                        applyWallDamage(server, pos, target, attackerPos, damage, feedback);
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerDeath(LivingDeathEvent event) {
      if (!event.getEntity().world.isRemote) {
         if (event.getEntity() instanceof EntityPlayerMP) {
            if (event.getEntity().world instanceof WorldServer) {
               cleanupDomainsForPlayer((WorldServer)event.getEntity().world, event.getEntity().getUniqueID(), "caster died");
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
      if (!event.player.world.isRemote) {
         if (event.player.world instanceof WorldServer) {
            cleanupDomainsForPlayer((WorldServer)event.player.world, event.player.getUniqueID(), "caster disconnected");
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
      if (!event.player.world.isRemote) {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) {
            WorldServer fromWorld = server.getWorld(event.fromDim);
            if (fromWorld != null) {
               cleanupDomainsForPlayer(fromWorld, event.player.getUniqueID(), "caster changed dimension");
            }

         }
      }
   }

   private static void cleanupDomainsForPlayer(WorldServer world, UUID playerId, String reason) {
      DomainRegistry reg = DomainRegistry.forWorld(world);
      List<DomainInstance> owned = new ArrayList();

      for(DomainInstance d : reg.all()) {
         if (playerId.equals(d.getOwnerUUID()) && d.getPhase() != DomainInstance.DomainPhase.ENDED) {
            owned.add(d);
         }
      }

      for(DomainInstance d : owned) {
         try {
            d.setPhase(DomainInstance.DomainPhase.ENDED);
            d.cleanup(world);
            reg.remove(d.getId());
            System.out.println("[InfTsukAddon/Domain] Force-collapsed domain " + d.getId() + " (" + reason + ")");
         } catch (Throwable t) {
            t.printStackTrace();
         }
      }

   }

   @SubscribeEvent
   public static void onWorldUnload(WorldEvent.Unload event) {
      World world = event.getWorld();
      if (!world.isRemote) {
         if (world instanceof WorldServer) {
            WorldServer server = (WorldServer)world;
            DomainSavedData data = DomainSavedData.get(server);

            for(DomainInstance d : new ArrayList(data.getTrackedDomains().values())) {
               try {
                  d.cleanup(server);
               } catch (Throwable t) {
                  t.printStackTrace();
               }
            }

         }
      }
   }
}
