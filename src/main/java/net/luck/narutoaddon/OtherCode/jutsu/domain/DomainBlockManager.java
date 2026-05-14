
package net.luck.narutoaddon.OtherCode.jutsu.domain;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DomainBlockManager {
   private static final IBlockState AIR_BLOCK;
   private static final IBlockState INVISIBLE_FLOOR;
   private static final int WALL_OFFSET = 0;
   private static final int OUTER_DOME_OFFSET = 1;
   private static final int GROUND_RADIUS_EXTENSION = 0;
   private static final int BLOCK_UPDATE_FLAGS = 2;
   private static final String BACKUP_FOLDER = "domain_backups";

   private static IBlockState wallState() {
      return BlockDomainSky.block != null ? BlockDomainSky.block.getDefaultState() : Blocks.CONCRETE.getStateFromMeta(15);
   }

   public static boolean buildDomain(WorldServer world, DomainInstance domain) {
      BlockPos center = domain.getCenter();
      int radius = domain.getRadius();
      UUID domainId = domain.getId();
      int groundY = center.getY() - 1;
      int groundRadius = radius + 0;
      int wallRadius = radius + 0;
      int outerDomeRadius = radius + 1;
      System.out.println("[InfTsukAddon/Domain] Building domain " + domainId + " at " + center + " radius=" + radius + " (" + domain.getType().getDisplayName() + ")");
      Map<BlockPos, IBlockState> blocksToBackup = new HashMap();

      for(int x = -outerDomeRadius; x <= outerDomeRadius; ++x) {
         for(int y = -outerDomeRadius; y <= outerDomeRadius; ++y) {
            for(int z = -outerDomeRadius; z <= outerDomeRadius; ++z) {
               BlockPos pos = center.add(x, y, z);
               double dist = Math.sqrt((double)(x * x + y * y + z * z));
               if (!(dist > (double)outerDomeRadius)) {
                  IBlockState currentState = world.getBlockState(pos);
                  if (!isContainerBlock(currentState, world, pos)) {
                     blocksToBackup.put(pos, currentState);
                  }
               }
            }
         }
      }

      if (!saveBlockBackup(world, domainId, blocksToBackup, center, radius, groundY)) {
         System.err.println("[InfTsukAddon/Domain] Failed to save block backup for domain " + domainId);
         return false;
      } else {
         System.out.println("[InfTsukAddon/Domain] Backed up " + blocksToBackup.size() + " blocks for domain " + domainId);
         IBlockState wall = wallState();
         int blocksPlaced = 0;

         for(int x = -outerDomeRadius; x <= outerDomeRadius; ++x) {
            for(int y = -outerDomeRadius; y <= outerDomeRadius; ++y) {
               for(int z = -outerDomeRadius; z <= outerDomeRadius; ++z) {
                  BlockPos pos = center.add(x, y, z);
                  double dist = Math.sqrt((double)(x * x + y * y + z * z));
                  IBlockState currentState = world.getBlockState(pos);
                  if (!isContainerBlock(currentState, world, pos)) {
                     if (dist > (double)wallRadius && dist <= (double)outerDomeRadius) {
                        world.setBlockState(pos, wall, 2);
                        ++blocksPlaced;
                     } else if (dist > (double)(wallRadius - 1) && dist <= (double)wallRadius) {
                        world.setBlockState(pos, wall, 2);
                        ++blocksPlaced;
                     } else if (dist <= (double)(wallRadius - 1)) {
                        double horizDist = Math.sqrt((double)(x * x + z * z));
                        if (horizDist <= (double)groundRadius && pos.getY() <= groundY) {
                           if (pos.getY() == groundY) {
                              world.setBlockState(pos, INVISIBLE_FLOOR, 2);
                           } else {
                              world.setBlockState(pos, wall, 2);
                           }

                           ++blocksPlaced;
                        } else if (currentState.getBlock() != Blocks.AIR) {
                           world.setBlockState(pos, AIR_BLOCK, 2);
                           ++blocksPlaced;
                        }
                     }
                  }
               }
            }
         }

         teleportEntitiesToGround(world, center, radius, groundY);
         System.out.println("[InfTsukAddon/Domain] Domain " + domainId + " built: " + blocksPlaced + " blocks placed");
         return true;
      }
   }

   public static boolean restoreDomain(WorldServer world, DomainInstance domain) {
      UUID domainId = domain.getId();
      Map<BlockPos, IBlockState> backup = loadBlockBackup(world, domainId);
      if (backup == null) {
         System.err.println("[InfTsukAddon/Domain] No backup found for domain " + domainId + " — cannot restore blocks");
         return false;
      } else {
         forceLoadDomainChunks(world, domain.getCenter(), domain.getRadius());
         clearDomainBlocks(world, domain.getCenter(), domain.getRadius());
         int restored = 0;

         for(Map.Entry<BlockPos, IBlockState> entry : backup.entrySet()) {
            world.setBlockState((BlockPos)entry.getKey(), (IBlockState)entry.getValue(), 2);
            ++restored;
         }

         System.out.println("[InfTsukAddon/Domain] Restored " + restored + " blocks for domain " + domainId);
         deleteBlockBackup(world, domainId);
         return true;
      }
   }

   private static void forceLoadDomainChunks(WorldServer world, BlockPos center, int radius) {
      int outerDomeRadius = radius + 1;
      int minChunkX = center.getX() - outerDomeRadius >> 4;
      int maxChunkX = center.getX() + outerDomeRadius >> 4;
      int minChunkZ = center.getZ() - outerDomeRadius >> 4;
      int maxChunkZ = center.getZ() + outerDomeRadius >> 4;

      for(int cx = minChunkX; cx <= maxChunkX; ++cx) {
         for(int cz = minChunkZ; cz <= maxChunkZ; ++cz) {
            world.getChunkProvider().provideChunk(cx, cz);
         }
      }

   }

   private static void clearDomainBlocks(WorldServer world, BlockPos center, int radius) {
      int outerDomeRadius = radius + 1;
      Block skyBlock = BlockDomainSky.block;

      for(int x = -outerDomeRadius; x <= outerDomeRadius; ++x) {
         for(int y = -outerDomeRadius; y <= outerDomeRadius; ++y) {
            for(int z = -outerDomeRadius; z <= outerDomeRadius; ++z) {
               BlockPos pos = center.add(x, y, z);
               IBlockState state = world.getBlockState(pos);
               Block block = state.getBlock();
               if (block == skyBlock || block == Blocks.CONCRETE || block == Blocks.BARRIER) {
                  world.setBlockState(pos, AIR_BLOCK, 2);
               }
            }
         }
      }

   }

   public static void performCrashRecovery(WorldServer world) {
      try {
         Path backupDir = getBackupPath(world);
         if (!Files.exists(backupDir, new LinkOption[0])) {
            return;
         }

         File[] backupFiles = backupDir.toFile().listFiles((dir, name) -> name.endsWith(".dat"));
         if (backupFiles == null || backupFiles.length == 0) {
            return;
         }

         System.out.println("[InfTsukAddon/Domain] Found " + backupFiles.length + " domain backup file(s) — performing crash recovery");

         for(File file : backupFiles) {
            UUID domainId;
            try {
               domainId = UUID.fromString(file.getName().replace(".dat", ""));
            } catch (IllegalArgumentException var30) {
               System.err.println("[InfTsukAddon/Domain] Skipping malformed backup file: " + file.getName());
               continue;
            }

            try {
               System.out.println("[InfTsukAddon/Domain] Recovering crashed domain: " + domainId);
               DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file.toPath())));
               Throwable var10 = null;

               NBTTagCompound root;
               try {
                  root = CompressedStreamTools.read(in);
               } catch (Throwable var28) {
                  var10 = var28;
                  throw var28;
               } finally {
                  if (in != null) {
                     if (var10 != null) {
                        try {
                           in.close();
                        } catch (Throwable var27) {
                           var10.addSuppressed(var27);
                        }
                     } else {
                        in.close();
                     }
                  }

               }

               if (root.hasKey("cx") && root.hasKey("radius")) {
                  BlockPos center = new BlockPos(root.getInteger("cx"), root.getInteger("cy"), root.getInteger("cz"));
                  int radius = root.getInteger("radius");
                  int outerDomeRadius = radius + 1;
                  int minChunkX = center.getX() - outerDomeRadius >> 4;
                  int maxChunkX = center.getX() + outerDomeRadius >> 4;
                  int minChunkZ = center.getZ() - outerDomeRadius >> 4;
                  int maxChunkZ = center.getZ() + outerDomeRadius >> 4;

                  for(int cx = minChunkX; cx <= maxChunkX; ++cx) {
                     for(int cz = minChunkZ; cz <= maxChunkZ; ++cz) {
                        world.getChunkProvider().provideChunk(cx, cz);
                     }
                  }

                  clearDomainBlocks(world, center, radius);
                  Map<BlockPos, IBlockState> backup = parseBackupBlocks(root);
                  if (backup == null) {
                     System.err.println("[InfTsukAddon/Domain] CRITICAL: could not parse backup for " + domainId + " — preserving file for manual recovery: " + file.getAbsolutePath());
                  } else {
                     int restored = 0;

                     for(Map.Entry<BlockPos, IBlockState> entry : backup.entrySet()) {
                        world.setBlockState((BlockPos)entry.getKey(), (IBlockState)entry.getValue(), 2);
                        ++restored;
                     }

                     System.out.println("[InfTsukAddon/Domain] Restored " + restored + " blocks from crashed domain " + domainId);
                     file.delete();
                  }
               } else {
                  System.err.println("[InfTsukAddon/Domain] Backup file " + file.getName() + " missing center/radius, deleting");
                  file.delete();
               }
            } catch (Exception e) {
               System.err.println("[InfTsukAddon/Domain] Failed to recover domain from " + file.getName());
               e.printStackTrace();
            }
         }
      } catch (Exception e) {
         System.err.println("[InfTsukAddon/Domain] Crash recovery scan failed");
         e.printStackTrace();
      }

   }

   private static Path getBackupPath(WorldServer world) {
      File worldDir = world.getSaveHandler().getWorldDirectory();
      return (new File(worldDir, "domain_backups")).toPath();
   }

   private static Path getBackupFile(WorldServer world, UUID domainId) {
      return getBackupPath(world).resolve(domainId.toString() + ".dat");
   }

   private static boolean saveBlockBackup(WorldServer world, UUID domainId, Map<BlockPos, IBlockState> blocks, BlockPos center, int radius, int groundY) {
      try {
         Path backupDir = getBackupPath(world);
         Files.createDirectories(backupDir);
         Path backupFile = getBackupFile(world, domainId);
         NBTTagCompound root = new NBTTagCompound();
         root.setString("domainId", domainId.toString());
         root.setLong("timestamp", System.currentTimeMillis());
         root.setInteger("cx", center.getX());
         root.setInteger("cy", center.getY());
         root.setInteger("cz", center.getZ());
         root.setInteger("radius", radius);
         root.setInteger("groundY", groundY);
         NBTTagList blockList = new NBTTagList();

         for(Map.Entry<BlockPos, IBlockState> entry : blocks.entrySet()) {
            NBTTagCompound blockNbt = new NBTTagCompound();
            BlockPos p = (BlockPos)entry.getKey();
            blockNbt.setInteger("x", p.getX());
            blockNbt.setInteger("y", p.getY());
            blockNbt.setInteger("z", p.getZ());
            NBTTagCompound stateNbt = new NBTTagCompound();
            NBTUtil.writeBlockState(stateNbt, (IBlockState)entry.getValue());
            blockNbt.setTag("state", stateNbt);
            blockList.appendTag(blockNbt);
         }

         root.setTag("blocks", blockList);
         DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(backupFile)));
         Throwable var27 = null;

         try {
            CompressedStreamTools.write(root, out);
         } catch (Throwable var23) {
            var27 = var23;
            throw var23;
         } finally {
            if (out != null) {
               if (var27 != null) {
                  try {
                     out.close();
                  } catch (Throwable var22) {
                     var27.addSuppressed(var22);
                  }
               } else {
                  out.close();
               }
            }

         }

         System.out.println("[InfTsukAddon/Domain] Saved domain backup to " + backupFile);
         return true;
      } catch (IOException e) {
         System.err.println("[InfTsukAddon/Domain] Failed to save domain backup");
         e.printStackTrace();
         return false;
      }
   }

   private static Map<BlockPos, IBlockState> loadBlockBackup(WorldServer world, UUID domainId) {
      try {
         Path backupFile = getBackupFile(world, domainId);
         if (!Files.exists(backupFile, new LinkOption[0])) {
            return null;
         } else {
            DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(backupFile)));
            Throwable var5 = null;

            NBTTagCompound root;
            try {
               root = CompressedStreamTools.read(in);
            } catch (Throwable var15) {
               var5 = var15;
               throw var15;
            } finally {
               if (in != null) {
                  if (var5 != null) {
                     try {
                        in.close();
                     } catch (Throwable var14) {
                        var5.addSuppressed(var14);
                     }
                  } else {
                     in.close();
                  }
               }

            }

            return parseBackupBlocks(root);
         }
      } catch (IOException e) {
         System.err.println("[InfTsukAddon/Domain] Failed to load domain backup");
         e.printStackTrace();
         return null;
      }
   }

   private static Map<BlockPos, IBlockState> parseBackupBlocks(NBTTagCompound root) {
      if (!root.hasKey("blocks")) {
         return null;
      } else {
         Map<BlockPos, IBlockState> blocks = new HashMap();
         NBTTagList blockList = root.getTagList("blocks", 10);

         for(int i = 0; i < blockList.tagCount(); ++i) {
            NBTTagCompound blockNbt = blockList.getCompoundTagAt(i);
            BlockPos pos = new BlockPos(blockNbt.getInteger("x"), blockNbt.getInteger("y"), blockNbt.getInteger("z"));
            IBlockState state = NBTUtil.readBlockState(blockNbt.getCompoundTag("state"));
            blocks.put(pos, state);
         }

         return blocks;
      }
   }

   private static void deleteBlockBackup(WorldServer world, UUID domainId) {
      try {
         Files.deleteIfExists(getBackupFile(world, domainId));
      } catch (IOException var3) {
         System.err.println("[InfTsukAddon/Domain] Failed to delete backup");
      }

   }

   private static boolean isContainerBlock(IBlockState state, WorldServer world, BlockPos pos) {
      TileEntity te = world.getTileEntity(pos);
      if (!(te instanceof TileEntityChest) && !(te instanceof TileEntityFurnace) && !(te instanceof TileEntityShulkerBox)) {
         ResourceLocation name = state.getBlock().getRegistryName();
         if (name != null) {
            String s = name.getPath();
            if (s.contains("chest") || s.contains("barrel") || s.contains("shulker")) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private static void teleportEntitiesToGround(WorldServer world, BlockPos center, int radius, int groundY) {
      double r = (double)(radius + 0);
      AxisAlignedBB box = new AxisAlignedBB((double)center.getX() - r, (double)center.getY() - r, (double)center.getZ() - r, (double)center.getX() + r, (double)center.getY() + r, (double)center.getZ() + r);

      for(EntityLivingBase e : world.getEntitiesWithinAABB(EntityLivingBase.class, box)) {
         double dx = e.posX - ((double)center.getX() + (double)0.5F);
         double dz = e.posZ - ((double)center.getZ() + (double)0.5F);
         if (Math.sqrt(dx * dx + dz * dz) <= (double)(radius + 0)) {
            e.setPositionAndUpdate(e.posX, (double)groundY + (double)1.0F, e.posZ);
            e.fallDistance = 0.0F;
         }
      }

   }

   static {
      AIR_BLOCK = Blocks.AIR.getDefaultState();
      INVISIBLE_FLOOR = Blocks.BARRIER.getDefaultState();
   }
}
