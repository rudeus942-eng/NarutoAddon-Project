
package net.luck.narutoaddon.OtherCode.jutsu.domain;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.item.ItemJutsu;

import java.util.Random;
import java.util.function.Supplier;

@ElementsInfTsukAddon.ModElement.Tag
public class BlockDomainSky extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:domain_sky")
   public static final Block block = null;

   public BlockDomainSky(ElementsInfTsukAddon instance) {
      super(instance, 955);
   }

   public void initElements() {
      this.elements.blocks.add((Supplier)() -> new BlockCustom());
   }

   @SideOnly(Side.CLIENT)
   public void registerModels(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation("inftsukaddon:domain_sky", "inventory"));
   }

   public static class BlockCustom extends Block {
      private static final String CONTACT_FLAG = "inftsuk_domain_hit";
      private static final int JUTSU_ENTITY_DAMAGE = 120;

      public BlockCustom() {
         super(Material.ROCK);
         this.setRegistryName("domain_sky");
         this.setTranslationKey("domain_sky");
         this.setSoundType(SoundType.GLASS);
         this.setHardness(-1.0F);
         this.setResistance(6000000.0F);
         this.setLightLevel(0.0F);
         this.setLightOpacity(15);
      }

      public EnumBlockRenderType getRenderType(IBlockState state) {
         return EnumBlockRenderType.INVISIBLE;
      }

      @SideOnly(Side.CLIENT)
      public BlockRenderLayer getRenderLayer() {
         return BlockRenderLayer.SOLID;
      }

      public boolean isOpaqueCube(IBlockState state) {
         return false;
      }

      public boolean isFullCube(IBlockState state) {
         return true;
      }

      @SideOnly(Side.CLIENT)
      public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
         return false;
      }

      public int quantityDropped(Random random) {
         return 0;
      }

      public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
         return ItemStack.EMPTY;
      }

      public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
         return false;
      }

      public boolean canDropFromExplosion(Explosion e) {
         return false;
      }

      public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
         if (!world.isRemote) {
            if (world instanceof WorldServer) {
               if (entity != null) {
                  if (!(entity instanceof EntityPlayer)) {
                     if (!(entity instanceof EntityItem)) {
                        if (!(entity instanceof EntityXPOrb)) {
                           if (entity instanceof ItemJutsu.IJutsu) {
                              if (!entity.getEntityData().getBoolean("inftsuk_domain_hit")) {
                                 entity.getEntityData().setBoolean("inftsuk_domain_hit", true);
                                 WorldServer server = (WorldServer)world;
                                 DomainInstance target = DomainEventHandler.findDomainForWall(server, pos);
                                 if (target != null) {
                                    DomainEventHandler.applyWallDamage(server, pos, target, entity.getPositionVector(), 120, (EntityPlayer)null);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
