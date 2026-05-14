
package net.luck.narutoaddon.OtherCode.jutsu;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Chakra;
import net.narutomod.PlayerRender;
import net.narutomod.PlayerRender.EnumPlayerBodyParts;
import net.narutomod.entity.EntityBijuManager;
import net.narutomod.item.*;
import net.narutomod.potion.PotionChakraEnhancedStrength;
import net.narutomod.potion.PotionReach;
import net.narutomod.procedure.ProcedureSync.EntityNBTTag;
import net.narutomod.procedure.ProcedureSync.ResetBoundingBox;

@ElementsInfTsukAddon.ModElement.Tag
public class EntityShrineHeianEraTransformation extends ElementsInfTsukAddon.ModElement {
   public static final String ACTIVE_KEY = "ShrineHeianEraActive";
   static final String DOUBLE_JUMP_CD_KEY = "ShrineHeianEraDoubleJumpCd";
   static final String DOUBLE_JUMP_USED_KEY = "ShrineHeianEraDoubleJumpUsed";
   static final String DOUBLE_JUMP_READY_SOUND_KEY = "ShrineHeianEraDoubleJumpReadySound";
   static final float BODY_SCALE = 1.5F;
   static final int DOUBLE_JUMP_COOLDOWN_TICKS = 140;
   static final double CHAKRA_DRAIN_PER_SECOND = (double)5.0F;
   static final ResourceLocation BODY_TEXTURE = new ResourceLocation("inftsukaddon:textures/entity/shrine_heianera_transformation.png");
   static final ResourceLocation FACE_TEXTURE = new ResourceLocation("inftsukaddon:textures/entity/shrine_sukunaface.png");
   static final PlayerRender.EnumPlayerBodyParts[] HIDDEN_PARTS;

   public EntityShrineHeianEraTransformation(ElementsInfTsukAddon instance) {
      super(instance, 947);
   }

   public void initElements() {
      this.elements.addNetworkMessage(DoubleJumpMessage.Handler.class, DoubleJumpMessage.class, Side.SERVER);
      this.elements.addNetworkMessage(DeactivateMessage.Handler.class, DeactivateMessage.class, Side.SERVER);
   }

   public void init(FMLInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new ShrineHeianCommonHooks());
   }

   @SideOnly(Side.CLIENT)
   public void preInit(FMLPreInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new ShrineHeianClientHooks());
   }

   public static boolean isActive(EntityLivingBase entity) {
      return entity != null && entity.getEntityData().getBoolean("ShrineHeianEraActive");
   }

   public static String getArmorTexture(EntityLivingBase living) {
      return isActive(living) ? BODY_TEXTURE.toString() : null;
   }

   static void deactivate(EntityPlayerMP player) {
      if (!isActive(player)) {
         clearLocalModeState(player);
      } else {
         EntityNBTTag.removeAndSync(player, "ShrineHeianEraActive");
         clearLocalModeState(player);
      }
   }

   private static void clearLocalModeState(EntityPlayerMP player) {
      player.getEntityData().removeTag("ShrineHeianEraDoubleJumpCd");
      player.getEntityData().removeTag("ShrineHeianEraDoubleJumpUsed");
      player.getEntityData().removeTag("ShrineHeianEraDoubleJumpReadySound");
      removeHeianEffects(player);
      player.eyeHeight = player.getDefaultEyeHeight();
      setPlayerScale(player, false);
   }

   private static boolean hasConflictingModes(EntityPlayer player) {
      if (EntityBijuManager.cloakLevel(player) > 0) {
         return true;
      } else if (ItemRaiton.CHAKRAMODE.jutsu.isActivated(player)) {
         return true;
      } else if (ItemYooton.CHAKRAMODE.jutsu.isActivated(player)) {
         return true;
      } else if (ItemSuiton.HYDRIFICATION.jutsu.isActivated(player)) {
         return true;
      } else if (ItemSenninka.STAGE2.jutsu.isActivated(player)) {
         return true;
      } else {
         return player.isPotionActive(PotionChakraEnhancedStrength.potion) ? true : player.getEntityData().hasKey("CrystalArmorEntityId");
      }
   }

   static void deactivateConflictingModes(EntityPlayer player) {
      if (EntityBijuManager.cloakLevel(player) > 0) {
         EntityBijuManager.toggleBijuCloak(player);
      }

      if (ItemSenjutsu.isSageModeActivated(player)) {
         ItemSenjutsu.deactivateSageMode(player);
      }

      if (ItemRaiton.CHAKRAMODE.jutsu.isActivated(player)) {
         ItemRaiton.CHAKRAMODE.jutsu.deactivate(player);
      }

      if (ItemYooton.CHAKRAMODE.jutsu.isActivated(player)) {
         ItemYooton.CHAKRAMODE.jutsu.deactivate(player);
      }

      if (ItemSuiton.HYDRIFICATION.jutsu.isActivated(player)) {
         ItemSuiton.HYDRIFICATION.jutsu.deactivate(player);
      }

      if (ItemSenninka.STAGE2.jutsu.isActivated(player)) {
         ItemSenninka.STAGE2.jutsu.deactivate(player);
      }

      if (ItemFutton.STRENGTH.jutsu.isActivated(player)) {
         ItemFutton.STRENGTH.jutsu.deactivate(player);
      }

      if (player.isPotionActive(PotionChakraEnhancedStrength.potion)) {
         player.removePotionEffect(PotionChakraEnhancedStrength.potion);
      }

      if (player.getEntityData().hasKey("CrystalArmorEntityId")) {
         Entity crystalArmor = player.world.getEntityByID(player.getEntityData().getInteger("CrystalArmorEntityId"));
         if (crystalArmor != null) {
            crystalArmor.setDead();
         }

         player.getEntityData().removeTag("CrystalArmorEntityId");
      }

   }

   static void setPlayerScale(EntityPlayerMP player, boolean active) {
      setPlayerScale(player, active, true);
   }

   static void setPlayerScale(EntityPlayerMP player, boolean active, boolean sync) {
      float width = 0.6F;
      float height = 1.8F;
      double halfWidth = (double)width * (double)0.5F;
      AxisAlignedBB bb = new AxisAlignedBB(player.posX - halfWidth, player.posY, player.posZ - halfWidth, player.posX + halfWidth, player.posY + (double)height, player.posZ + halfWidth);
      player.width = width;
      player.height = height;
      player.setEntityBoundingBox(bb);
      if (sync) {
         player.resetPositionToBB();
         ResetBoundingBox.sendToTracking(player);
         ResetBoundingBox.sendToPlayer(player, player);
      }

   }

   static void applyHeianEffects(EntityPlayer player) {
      player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 30, 2, false, false));
      player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30, 3, false, false));
      player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30, 19, false, false));
      player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 30, 11, false, false));
      player.addPotionEffect(new PotionEffect(PotionReach.potion, 30, 1, false, false));
   }

   private static void removeHeianEffects(EntityPlayer player) {
      player.removePotionEffect(MobEffects.HASTE);
      player.removePotionEffect(MobEffects.RESISTANCE);
      player.removePotionEffect(MobEffects.STRENGTH);
      player.removePotionEffect(MobEffects.SPEED);
      player.removePotionEffect(PotionReach.potion);
   }

   static ItemStack findShrineStack(EntityPlayer player) {
      for(ItemStack stack : player.inventory.mainInventory) {
         if (!stack.isEmpty() && stack.getItem() == ItemShrineRelease.block) {
            return stack;
         }
      }

      for(ItemStack stack : player.inventory.offHandInventory) {
         if (!stack.isEmpty() && stack.getItem() == ItemShrineRelease.block) {
            return stack;
         }
      }

      return ItemStack.EMPTY;
   }

   static void keepBodyHidden(EntityPlayerMP player) {
      PlayerRender.hideBodyPart(player, 2, HIDDEN_PARTS);
   }

   static {
      HIDDEN_PARTS = new PlayerRender.EnumPlayerBodyParts[]{EnumPlayerBodyParts.HEAD, EnumPlayerBodyParts.HEADWEAR, EnumPlayerBodyParts.BODY, EnumPlayerBodyParts.BODYWEAR, EnumPlayerBodyParts.LEFTARM, EnumPlayerBodyParts.LEFTARMWEAR, EnumPlayerBodyParts.RIGHTARM, EnumPlayerBodyParts.RIGHTARMWEAR, EnumPlayerBodyParts.LEFTLEG, EnumPlayerBodyParts.LEFTLEGWEAR, EnumPlayerBodyParts.RIGHTLEG, EnumPlayerBodyParts.RIGHTLEGWEAR};
   }

   public static class Jutsu implements ItemJutsu.IJutsuCallback {
      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (!(entity instanceof EntityPlayerMP)) {
            return false;
         } else {
            EntityPlayerMP player = (EntityPlayerMP)entity;
            if (EntityShrineHeianEraTransformation.isActive(player)) {
               EntityShrineHeianEraTransformation.deactivate(player);
               return true;
            } else {
               Chakra.Pathway pathway = Chakra.pathway(player);
               if (pathway != null && !(pathway.getAmount() < (double)5.0F)) {
                  EntityShrineHeianEraTransformation.deactivateConflictingModes(player);
                  if (EntityShrineHeianEraTransformation.hasConflictingModes(player)) {
                     player.sendStatusMessage(new TextComponentString("§cDisable your other active mode first."), true);
                     return false;
                  } else {
                     EntityNBTTag.setAndSync(player, "ShrineHeianEraActive", true);
                     player.getEntityData().setInteger("ShrineHeianEraDoubleJumpCd", 0);
                     player.getEntityData().setBoolean("ShrineHeianEraDoubleJumpUsed", false);
                     EntityShrineHeianEraTransformation.setPlayerScale(player, true);
                     return true;
                  }
               } else {
                  player.sendStatusMessage(new TextComponentString("§cNot enough chakra."), true);
                  return false;
               }
            }
         }
      }

      public boolean isActivated(EntityLivingBase entity) {
         return EntityShrineHeianEraTransformation.isActive(entity);
      }

      public boolean isActivated(ItemStack stack) {
         return false;
      }

      public void deactivate(EntityLivingBase entity) {
         if (entity instanceof EntityPlayerMP) {
            EntityShrineHeianEraTransformation.deactivate((EntityPlayerMP)entity);
         }

      }
   }

   public static class DoubleJumpMessage implements IMessage {
      public void toBytes(ByteBuf buf) {
      }

      public void fromBytes(ByteBuf buf) {
      }

      public static class Handler implements IMessageHandler<DoubleJumpMessage, IMessage> {
         public IMessage onMessage(DoubleJumpMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> this.tryDoubleJump(player));
            return null;
         }

         private void tryDoubleJump(EntityPlayerMP player) {
            if (EntityShrineHeianEraTransformation.isActive(player) && !player.onGround && !player.capabilities.isFlying && !player.isInWater() && !player.isOnLadder()) {
               if (!player.getEntityData().getBoolean("ShrineHeianEraDoubleJumpUsed")) {
                  int now = player.ticksExisted;
                  if (now >= player.getEntityData().getInteger("ShrineHeianEraDoubleJumpCd")) {
                     Vec3d look = player.getLookVec();
                     Vec3d horizontal = new Vec3d(look.x, (double)0.0F, look.z);
                     if (horizontal.lengthSquared() > 1.0E-4) {
                        horizontal = horizontal.normalize();
                     }

                     player.motionY = 1.24;
                     player.motionX += horizontal.x * 0.35;
                     player.motionZ += horizontal.z * 0.35;
                     player.fallDistance = 0.0F;
                     player.velocityChanged = true;
                     player.getEntityData().setBoolean("ShrineHeianEraDoubleJumpUsed", true);
                     player.getEntityData().setInteger("ShrineHeianEraDoubleJumpCd", now + 140);
                     player.getEntityData().setBoolean("ShrineHeianEraDoubleJumpReadySound", false);
                  }
               }
            }
         }
      }
   }

   public static class DeactivateMessage implements IMessage {
      public void toBytes(ByteBuf buf) {
      }

      public void fromBytes(ByteBuf buf) {
      }

      public static class Handler implements IMessageHandler<DeactivateMessage, IMessage> {
         public IMessage onMessage(DeactivateMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> this.tryDeactivate(player));
            return null;
         }

         private void tryDeactivate(EntityPlayerMP player) {
            if (EntityShrineHeianEraTransformation.isActive(player) && player.isSneaking()) {
               ItemStack stack = player.getHeldItemMainhand();
               if (stack.isEmpty() || !(stack.getItem() instanceof ItemShrineRelease.RangedItem)) {
                  stack = player.getHeldItemOffhand();
               }

               if (!stack.isEmpty() && stack.getItem() instanceof ItemShrineRelease.RangedItem) {
                  ItemShrineRelease.RangedItem item = (ItemShrineRelease.RangedItem)stack.getItem();
                  ItemJutsu.JutsuEnum current = item.getCurrentJutsu(stack);
                  if (current != null && current.index == ItemShrineRelease.HEIAN_ERA_TRANSFORMATION.index) {
                     EntityShrineHeianEraTransformation.deactivate(player);
                  }
               }
            }
         }
      }
   }
}
