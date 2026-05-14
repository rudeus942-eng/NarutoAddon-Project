
package net.luck.narutoaddon.OtherCode.quest.waypoint;

import net.luck.narutoaddon.OtherCode.entity.EntityItachi;
import net.luck.narutoaddon.OtherCode.entity.EntityKisame;
import net.luck.narutoaddon.OtherCode.quest.core.QuestStep;
import net.luck.narutoaddon.OtherCode.quest.npc.INpcConfigurable;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfig;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WaypointSpawnLogic {
   public static List<Entity> spawnQuestEnemies(World world, EntityPlayerMP player, QuestStep step, BlockPos pos, boolean isGenerated) {
      List<Entity> spawned = new ArrayList();
      pos = findLandPosition(world, pos);
      if (step.spawnEntityIds != null && step.spawnCounts != null) {
         for(int i = 0; i < step.spawnEntityIds.length; ++i) {
            String entityId = step.spawnEntityIds[i];
            int count = i < step.spawnCounts.length ? step.spawnCounts[i] : 1;

            for(int j = 0; j < count; ++j) {
               Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(entityId), world);
               if (entity == null) {
                  System.out.println("[QuestSpawn] Failed to create entity: " + entityId);
               } else {
                  double offsetX = (world.rand.nextDouble() - (double)0.5F) * (double)6.0F;
                  double offsetZ = (world.rand.nextDouble() - (double)0.5F) * (double)6.0F;
                  int spawnBlockX = pos.getX() + (int)offsetX;
                  int spawnBlockZ = pos.getZ() + (int)offsetZ;
                  double spawnX = (double)spawnBlockX + (double)0.5F + (offsetX - (double)((int)offsetX));
                  double spawnZ = (double)spawnBlockZ + (double)0.5F + (offsetZ - (double)((int)offsetZ));
                  double spawnY = isGenerated ? ensureSafeSpawnY(world, spawnBlockX, (double)pos.getY(), spawnBlockZ) : (double)pos.getY();
                  entity.setLocationAndAngles(spawnX, spawnY, spawnZ, world.rand.nextFloat() * 360.0F, 0.0F);
                  NBTTagCompound entityData = entity.getEntityData();
                  entityData.setBoolean("questEntity", true);
                  entityData.setString("ownerUUID", player.getUniqueID().toString());
                  if (entity instanceof EntityLiving) {
                     ((EntityLiving)entity).enablePersistence();
                  }

                  world.spawnEntity(entity);
                  spawned.add(entity);
               }
            }
         }

         return spawned;
      } else {
         return spawned;
      }
   }

   public static void cleanupQuestEntities(World world, Set<UUID> entityUUIDs) {
      if (entityUUIDs != null && !entityUUIDs.isEmpty()) {
         if (world instanceof WorldServer) {
            WorldServer ws = (WorldServer)world;

            for(UUID uuid : entityUUIDs) {
               Entity entity = ws.getEntityFromUuid(uuid);
               if (entity != null && !entity.isDead) {
                  entity.setDead();
               }
            }
         } else {
            for(Entity entity : new ArrayList(world.loadedEntityList)) {
               if (entity != null && !entity.isDead && entityUUIDs.contains(entity.getUniqueID())) {
                  entity.setDead();
               }
            }
         }

      }
   }

   public static boolean areAllEnemiesDead(World world, Set<UUID> entityUUIDs) {
      if (entityUUIDs != null && !entityUUIDs.isEmpty()) {
         for(Entity entity : new ArrayList(world.loadedEntityList)) {
            if (entity != null && !entity.isDead && entityUUIDs.contains(entity.getUniqueID())) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public static void spawnSmokeParticles(World world, double x, double y, double z) {
      if (world instanceof WorldServer) {
         WorldServer ws = (WorldServer)world;

         for(int i = 0; i < 30; ++i) {
            double ox = (world.rand.nextDouble() - (double)0.5F) * (double)2.0F;
            double oy = world.rand.nextDouble() * (double)2.0F;
            double oz = (world.rand.nextDouble() - (double)0.5F) * (double)2.0F;
            ws.spawnParticle(EnumParticleTypes.CLOUD, x + ox, y + oy, z + oz, 1, 0.1, 0.05, 0.1, 0.02, new int[0]);
         }
      }

   }

   public static Entity spawnQuestNpc(World world, EntityPlayerMP player, String npcConfigId, BlockPos pos) {
      return spawnQuestNpc(world, player, npcConfigId, pos, (String)null, false);
   }

   public static Entity spawnQuestNpc(World world, EntityPlayerMP player, String npcConfigId, BlockPos pos, String questId) {
      return spawnQuestNpc(world, player, npcConfigId, pos, questId, false);
   }

   public static Entity spawnQuestNpc(World world, EntityPlayerMP player, String npcConfigId, BlockPos pos, String questId, boolean isGenerated) {
      pos = findLandPosition(world, pos);
      NpcConfig config = NpcConfigRegistry.get(npcConfigId);
      if (config == null) {
         System.out.println("[QuestSpawn] Unknown NPC config: " + npcConfigId);
         return null;
      } else {
         Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(config.getEntityRegistryId()), world);
         if (entity == null) {
            System.out.println("[QuestSpawn] Failed to create NPC entity: " + config.getEntityRegistryId());
            return null;
         } else {
            double spawnX = (double)pos.getX() + (double)0.5F;
            double spawnZ = (double)pos.getZ() + (double)0.5F;
            double spawnY = isGenerated ? ensureSafeSpawnY(world, pos.getX(), (double)pos.getY(), pos.getZ()) : (double)pos.getY();
            float yaw;
            if (config.hasFixedSpawnYaw()) {
               yaw = config.getSpawnYaw();
            } else {
               double dx = player.posX - spawnX;
               double dz = player.posZ - spawnZ;
               yaw = (float)(MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
            }

            entity.setLocationAndAngles(spawnX, spawnY, spawnZ, yaw, 0.0F);
            if (entity instanceof EntityLivingBase) {
               ((EntityLivingBase)entity).rotationYawHead = yaw;
               ((EntityLivingBase)entity).renderYawOffset = yaw;
            }

            NBTTagCompound entityData = entity.getEntityData();
            entityData.setBoolean("questEntity", true);
            entityData.setString("ownerUUID", player.getUniqueID().toString());
            entityData.setString("npcConfigId", npcConfigId);
            if (questId != null) {
               entityData.setString("sharedQuestId", questId);
            }

            if (entity instanceof INpcConfigurable) {
               ((INpcConfigurable)entity).applyNpcConfig(config);
            }

            if (entity instanceof EntityLiving) {
               ((EntityLiving)entity).enablePersistence();
            }

            world.spawnEntity(entity);
            return entity;
         }
      }
   }

   public static int findGroundY(World world, int x, int z) {
      int topY = world.getHeight(x, z);
      if (topY <= 1) {
         topY = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
         if (topY <= 1) {
            return 64;
         }
      }

      for(int y = topY - 1; y > 0; --y) {
         IBlockState state = world.getBlockState(new BlockPos(x, y, z));
         Material mat = state.getMaterial();
         if (mat != Material.LEAVES && mat != Material.PLANTS && mat != Material.VINE && mat != Material.WOOD && mat != Material.WEB && mat != Material.SNOW && mat != Material.CARPET && mat != Material.AIR) {
            int spawnY = y + 1;
            Material feetMat = world.getBlockState(new BlockPos(x, spawnY, z)).getMaterial();
            Material headMat = world.getBlockState(new BlockPos(x, spawnY + 1, z)).getMaterial();
            if (isPassable(feetMat) && isPassable(headMat)) {
               return spawnY;
            }

            for(int uy = spawnY; uy < topY + 10 && uy < 256; ++uy) {
               Material ufeet = world.getBlockState(new BlockPos(x, uy, z)).getMaterial();
               Material uhead = world.getBlockState(new BlockPos(x, uy + 1, z)).getMaterial();
               if (isPassable(ufeet) && isPassable(uhead)) {
                  Material below = world.getBlockState(new BlockPos(x, uy - 1, z)).getMaterial();
                  if (below != Material.AIR && !isPassable(below)) {
                     return uy;
                  }
               }
            }
         }
      }

      return topY;
   }

   private static boolean isPassable(Material mat) {
      return mat == Material.AIR || mat == Material.PLANTS || mat == Material.VINE || mat == Material.SNOW || mat == Material.CARPET || mat == Material.WEB;
   }

   private static double ensureSafeSpawnY(World world, int x, double y, int z) {
      BlockPos feetPos = new BlockPos(x, (int)y, z);
      BlockPos headPos = new BlockPos(x, (int)y + 1, z);
      Material feetMat = world.getBlockState(feetPos).getMaterial();
      Material headMat = world.getBlockState(headPos).getMaterial();
      return !feetMat.blocksMovement() && !headMat.blocksMovement() ? y : (double)findGroundY(world, x, z);
   }

   private static boolean isSurfaceWater(World world, int x, int z) {
      int surfaceY = world.getHeight(x, z);
      if (surfaceY <= 0) {
         return false;
      } else {
         IBlockState state = world.getBlockState(new BlockPos(x, surfaceY - 1, z));
         return state.getMaterial() == Material.WATER;
      }
   }

   public static BlockPos findLandPosition(World world, BlockPos pos) {
      if (!isSurfaceWater(world, pos.getX(), pos.getZ())) {
         return pos;
      } else {
         for(int radius = 4; radius <= 32; radius += 4) {
            for(int dir = 0; dir < 8; ++dir) {
               double angle = (double)dir * (Math.PI / 4D);
               int checkX = pos.getX() + (int)(Math.cos(angle) * (double)radius);
               int checkZ = pos.getZ() + (int)(Math.sin(angle) * (double)radius);
               if (!isSurfaceWater(world, checkX, checkZ)) {
                  int newY = findGroundY(world, checkX, checkZ);
                  return new BlockPos(checkX, newY, checkZ);
               }
            }
         }

         return pos;
      }
   }

   public static List<Entity> spawnQuestNpcs(World world, EntityPlayerMP player, QuestStep step, BlockPos pos, String questId, boolean isGenerated) {
      List<Entity> spawned = new ArrayList();
      pos = findLandPosition(world, pos);
      if (step.spawnEntityIds != null && step.spawnCounts != null) {
         if (step.npcConfigIds == null) {
            return spawned;
         } else {
            for(int i = 0; i < step.spawnEntityIds.length; ++i) {
               String entityId = step.spawnEntityIds[i];
               int count = i < step.spawnCounts.length ? step.spawnCounts[i] : 1;
               String configId = i < step.npcConfigIds.length ? step.npcConfigIds[i] : null;
               NpcConfig config = configId != null ? NpcConfigRegistry.get(configId) : null;

               for(int j = 0; j < count; ++j) {
                  Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(entityId), world);
                  if (entity == null) {
                     System.out.println("[QuestSpawn] Failed to create entity: " + entityId);
                  } else {
                     int totalNpcs = 0;

                     for(int sc : step.spawnCounts) {
                        totalNpcs += sc;
                     }

                     boolean singleNpc = step.spawnEntityIds.length == 1 && totalNpcs == 1;
                     double offsetX = singleNpc ? (double)0.0F : (world.rand.nextDouble() - (double)0.5F) * (double)6.0F;
                     double offsetZ = singleNpc ? (double)0.0F : (world.rand.nextDouble() - (double)0.5F) * (double)6.0F;
                     int spawnBlockX = pos.getX() + (singleNpc ? 0 : (int)offsetX);
                     int spawnBlockZ = pos.getZ() + (singleNpc ? 0 : (int)offsetZ);
                     double spawnX = (double)spawnBlockX + (double)0.5F + (singleNpc ? (double)0.0F : offsetX - (double)((int)offsetX));
                     double spawnZ = (double)spawnBlockZ + (double)0.5F + (singleNpc ? (double)0.0F : offsetZ - (double)((int)offsetZ));
                     double spawnY = isGenerated ? ensureSafeSpawnY(world, spawnBlockX, (double)pos.getY(), spawnBlockZ) : (double)pos.getY();
                     float yaw;
                     if (config != null && config.hasFixedSpawnYaw()) {
                        yaw = config.getSpawnYaw();
                     } else {
                        double dx = player.posX - spawnX;
                        double dz = player.posZ - spawnZ;
                        yaw = (float)(MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
                     }

                     entity.setLocationAndAngles(spawnX, spawnY, spawnZ, yaw, 0.0F);
                     if (entity instanceof EntityLivingBase) {
                        ((EntityLivingBase)entity).rotationYawHead = yaw;
                        ((EntityLivingBase)entity).renderYawOffset = yaw;
                     }

                     NBTTagCompound entityData = entity.getEntityData();
                     entityData.setBoolean("questEntity", true);
                     entityData.setString("ownerUUID", player.getUniqueID().toString());
                     if (configId != null) {
                        entityData.setString("npcConfigId", configId);
                     }

                     if (questId != null) {
                        entityData.setString("sharedQuestId", questId);
                     }

                     if (config != null && entity instanceof INpcConfigurable) {
                        ((INpcConfigurable)entity).applyNpcConfig(config);
                     }

                     if (entity instanceof EntityLiving) {
                        ((EntityLiving)entity).enablePersistence();
                     }

                     world.spawnEntity(entity);
                     spawned.add(entity);
                  }
               }
            }

            EntityItachi.EntityCustom itachi = null;
            EntityKisame.EntityCustom kisame = null;

            for(Entity e : spawned) {
               if (e instanceof EntityItachi.EntityCustom) {
                  itachi = (EntityItachi.EntityCustom)e;
               }

               if (e instanceof EntityKisame.EntityCustom) {
                  kisame = (EntityKisame.EntityCustom)e;
               }
            }

            if (itachi != null && kisame != null) {
               itachi.setKisameUUID(kisame.getUniqueID());
               kisame.setItachiUUID(itachi.getUniqueID());
            }

            return spawned;
         }
      } else {
         return spawned;
      }
   }
}
