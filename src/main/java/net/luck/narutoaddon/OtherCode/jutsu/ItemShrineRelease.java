
package net.luck.narutoaddon.OtherCode.jutsu;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.narutomod.Chakra;
import net.narutomod.PlayerTracker;
import net.narutomod.item.ItemJutsu;
import net.narutomod.item.ItemJutsu.JutsuEnum.Type;
import net.narutomod.item.ItemOnBody;
import net.narutomod.item.ItemOnBody.BodyPart;

import java.util.Locale;

@ElementsInfTsukAddon.ModElement.Tag
public class ItemShrineRelease extends ElementsInfTsukAddon.ModElement {
   @ObjectHolder("inftsukaddon:shrine_release")
   public static final Item block = null;
   private static final ItemJutsu.IJutsuCallback RAW_DISMANTLE_JUTSU = new EntityShrineDismantle.Jutsu();
   private static final ItemJutsu.IJutsuCallback RAW_CLEAVE_JUTSU = new EntityShrineCleave.Jutsu();
   private static final ItemJutsu.IJutsuCallback RAW_DIVINE_FLAMES_JUTSU = new EntityShrineDivineFlames.Jutsu();
   private static final ItemJutsu.IJutsuCallback RAW_MALEVOLENT_SHRINE_JUTSU = new EntityShrineMalevolentShrine.Jutsu();
   private static final ItemJutsu.IJutsuCallback RAW_HEIAN_ERA_TRANSFORMATION_JUTSU = new EntityShrineHeianEraTransformation.Jutsu();
   public static final ItemJutsu.JutsuEnum DISMANTLE;
   public static final ItemJutsu.JutsuEnum CLEAVE;
   public static final ItemJutsu.JutsuEnum DIVINE_FLAMES;
   public static final ItemJutsu.JutsuEnum MALEVOLENT_SHRINE;
   public static final ItemJutsu.JutsuEnum HEIAN_ERA_TRANSFORMATION;

   public ItemShrineRelease(ElementsInfTsukAddon instance) {
      super(instance, 943);
   }

   public void initElements() {
      this.elements.items.add(RangedItem::new);
   }

   @SideOnly(Side.CLIENT)
   public void registerModels(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(block, 0, new ModelResourceLocation("inftsukaddon:shrine_release", "inventory"));
   }

   public static boolean unlockJutsu(EntityPlayerMP player, int jutsuIndex) {
      Item shrineItem = (Item)Item.REGISTRY.getObject(new ResourceLocation("inftsukaddon", "shrine_release"));
      if (shrineItem == null) {
         return false;
      } else {
         boolean found = false;

         for(int i = 0; i < player.inventory.getSizeInventory(); ++i) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == shrineItem) {
               NBTTagCompound nbt = stack.getTagCompound();
               if (nbt == null) {
                  nbt = new NBTTagCompound();
                  stack.setTagCompound(nbt);
               }

               nbt.setLong("JutsuCDMapKey" + jutsuIndex, 0L);
               found = true;
            }
         }

         if (found) {
            player.inventoryContainer.detectAndSendChanges();
         }

         return found;
      }
   }

   public static boolean unlockAllJutsu(EntityPlayerMP player) {
      boolean unlockedAny = false;
      unlockedAny |= unlockJutsu(player, DISMANTLE.index);
      unlockedAny |= unlockJutsu(player, CLEAVE.index);
      unlockedAny |= unlockJutsu(player, DIVINE_FLAMES.index);
      unlockedAny |= unlockJutsu(player, MALEVOLENT_SHRINE.index);
      unlockedAny |= unlockJutsu(player, HEIAN_ERA_TRANSFORMATION.index);
      return unlockedAny;
   }

   static {
      DISMANTLE = new ItemJutsu.JutsuEnum(0, "entity.shrine_dismantle.name", 'B', (double)18.0F, RAW_DISMANTLE_JUTSU);
      CLEAVE = new ItemJutsu.JutsuEnum(1, "entity.shrine_cleave.name", 'A', (double)26.0F, RAW_CLEAVE_JUTSU);
      DIVINE_FLAMES = new ItemJutsu.JutsuEnum(2, "entity.shrine_divine_flames.name", 'S', (double)45.0F, RAW_DIVINE_FLAMES_JUTSU);
      MALEVOLENT_SHRINE = new ItemJutsu.JutsuEnum(3, "entity.shrine_malevolent_shrine.name", 'S', (double)90.0F, RAW_MALEVOLENT_SHRINE_JUTSU);
      HEIAN_ERA_TRANSFORMATION = new ItemJutsu.JutsuEnum(4, "entity.shrine_heian_era_transformation.name", 'S', (double)0.0F, RAW_HEIAN_ERA_TRANSFORMATION_JUTSU);
   }

   public static class RangedItem extends ItemJutsu.Base implements ItemOnBody.Interface {
      private static final String FRAMEWORK_READY_KEY = "ShrineFrameworkReady";
      private static final String DIVINE_FLAMES_AUTO_RELEASE_KEY = "ShrineDivineFlamesAutoRelease";
      private static final long DISMANTLE_COOLDOWN_TICKS = 25L;
      private static final long DIVINE_FLAMES_COOLDOWN_TICKS = 240L;
      private static final long MALEVOLENT_SHRINE_COOLDOWN_TICKS = 6000L;

      public RangedItem() {
         super(Type.OTHER, new ItemJutsu.JutsuEnum[]{ItemShrineRelease.DISMANTLE, ItemShrineRelease.CLEAVE, ItemShrineRelease.DIVINE_FLAMES, ItemShrineRelease.MALEVOLENT_SHRINE, ItemShrineRelease.HEIAN_ERA_TRANSFORMATION});
         this.setTranslationKey("shrine_release");
         this.setRegistryName("shrine_release");
         this.setCreativeTab(CreativeTabs.COMBAT);
         this.maxStackSize = 1;
         this.defaultCooldownMap[ItemShrineRelease.DISMANTLE.index] = 0L;
         this.defaultCooldownMap[ItemShrineRelease.CLEAVE.index] = 0L;
         this.defaultCooldownMap[ItemShrineRelease.DIVINE_FLAMES.index] = 0L;
         this.defaultCooldownMap[ItemShrineRelease.MALEVOLENT_SHRINE.index] = 0L;
         this.defaultCooldownMap[ItemShrineRelease.HEIAN_ERA_TRANSFORMATION.index] = 0L;
      }

      public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
         super.onUpdate(stack, world, entity, itemSlot, isSelected);
         if (entity instanceof EntityLivingBase) {
            EntityLivingBase owner = (EntityLivingBase)entity;
            if (this.isOwner(stack, owner)) {
               this.ensureFrameworkAccess(stack);
               this.normalizeDivineFlamesCooldown(stack, world);
               this.normalizeMalevolentShrineCooldown(stack, world);
            }
         }
      }

      public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
         ItemStack stack = player.getHeldItem(hand);
         this.isOwner(stack, player);
         this.ensureFrameworkAccess(stack);
         if (this.tryDeactivateHeian(stack, world, player)) {
            return new ActionResult(EnumActionResult.SUCCESS, stack);
         } else {
            ItemJutsu.JutsuEnum current = this.getCurrentJutsu(stack);
            long cooldown = this.getCurrentJutsuCooldown(stack);
            long now = world.getTotalWorldTime();
            if (cooldown < 0L) {
               return new ActionResult(EnumActionResult.FAIL, stack);
            } else if (cooldown > now) {
               if (!world.isRemote && current != null) {
                  if (current.index == ItemShrineRelease.MALEVOLENT_SHRINE.index && EntityShrineMalevolentShrine.WINDUP_PROTECTED.contains(player.getUniqueID())) {
                     player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_PURPLE + "" + TextFormatting.BOLD + "Domain Expanding..."), true);
                  } else {
                     long remainingTicks = cooldown - now;
                     double remainingSeconds = (double)remainingTicks / (double)20.0F;
                     String label = current.index == ItemShrineRelease.DISMANTLE.index ? "Dismantle" : (current.index == ItemShrineRelease.DIVINE_FLAMES.index ? "Divine Flames" : (current.index == ItemShrineRelease.MALEVOLENT_SHRINE.index ? "Malevolent Shrine" : null));
                     if (label != null) {
                        String text = String.format(Locale.ROOT, "%s CD: %.1fs", label, remainingSeconds);
                        player.sendStatusMessage(new TextComponentString(TextFormatting.GOLD + text), true);
                     }
                  }
               }

               return new ActionResult(EnumActionResult.PASS, stack);
            } else if (current != null && (current.index == ItemShrineRelease.MALEVOLENT_SHRINE.index || current.index == ItemShrineRelease.HEIAN_ERA_TRANSFORMATION.index)) {
               if (!world.isRemote && !PlayerTracker.isNinja(player)) {
                  player.sendStatusMessage(new TextComponentString("§cYou must be a ninja to use Shrine Release."), true);
                  return new ActionResult(EnumActionResult.FAIL, stack);
               } else {
                  ItemJutsu.IJutsuCallback rawCallback = this.getRawCallback(current);
                  return !world.isRemote && rawCallback != null ? new ActionResult(this.tryCastCurrentJutsu(stack, world, player, current, rawCallback, 1.0F) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, stack) : new ActionResult(EnumActionResult.SUCCESS, stack);
               }
            } else {
               player.setActiveHand(hand);
               return new ActionResult(EnumActionResult.SUCCESS, stack);
            }
         }
      }

      public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, EnumHand hand) {
         ItemStack stack = player.getHeldItem(hand);
         this.isOwner(stack, player);
         this.ensureFrameworkAccess(stack);
         return this.tryDeactivateHeian(stack, world, player) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
      }

      public int getMaxItemUseDuration(ItemStack stack) {
         return 72000;
      }

      public EnumAction getItemUseAction(ItemStack stack) {
         return EnumAction.BOW;
      }

      public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
         this.isOwner(stack, entity);
         this.ensureFrameworkAccess(stack);
         ItemJutsu.JutsuEnum current = this.getCurrentJutsu(stack);
         ItemJutsu.IJutsuCallback rawCallback = this.getRawCallback(current);
         float power = this.getManualChargePower(stack, entity, count, current);
         if (rawCallback != null) {
            rawCallback.onUsingTick(stack, entity, power);
         }

         if (!entity.world.isRemote && entity instanceof EntityPlayer && current != null && current.index == ItemShrineRelease.DIVINE_FLAMES.index && rawCallback != null && power >= rawCallback.getMaxPower(stack, entity)) {
            EntityPlayer player = (EntityPlayer)entity;
            if (this.tryCastCurrentJutsu(stack, entity.world, player, current, rawCallback, power)) {
               this.getOrCreateTag(stack).setBoolean("ShrineDivineFlamesAutoRelease", true);
               player.stopActiveHand();
            }
         }

      }

      public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft) {
         if (!world.isRemote && entity instanceof EntityPlayer) {
            this.isOwner(stack, entity);
            this.ensureFrameworkAccess(stack);
            EntityPlayer player = (EntityPlayer)entity;
            if (!PlayerTracker.isNinja(player)) {
               player.sendStatusMessage(new TextComponentString("§cYou must be a ninja to use Shrine Release."), true);
            } else {
               ItemJutsu.JutsuEnum current = this.getCurrentJutsu(stack);
               ItemJutsu.IJutsuCallback rawCallback = this.getRawCallback(current);
               if (rawCallback != null) {
                  NBTTagCompound nbt = this.getOrCreateTag(stack);
                  if (current.index == ItemShrineRelease.DIVINE_FLAMES.index && nbt.getBoolean("ShrineDivineFlamesAutoRelease")) {
                     nbt.setBoolean("ShrineDivineFlamesAutoRelease", false);
                  } else if (current.index != ItemShrineRelease.CLEAVE.index) {
                     float power = this.getManualChargePower(stack, entity, timeLeft, current);
                     if (!(power < rawCallback.getBasePower())) {
                        this.tryCastCurrentJutsu(stack, world, player, current, rawCallback, power);
                     }
                  }
               }
            }
         }
      }

      private boolean tryCastCurrentJutsu(ItemStack stack, World world, EntityPlayer player, ItemJutsu.JutsuEnum current, ItemJutsu.IJutsuCallback rawCallback, float power) {
         Chakra.Pathway pathway = Chakra.pathway(player);
         if (pathway == null) {
            player.sendStatusMessage(new TextComponentString("§cYour chakra pathway is not available."), true);
            return false;
         } else {
            double chakraCost = current.chakraUsage * (double)power;
            if (pathway.getAmount() < chakraCost) {
               player.sendStatusMessage(new TextComponentString("§cNot enough chakra."), true);
               return false;
            } else if (rawCallback.createJutsu(stack, player, power)) {
               pathway.consume(chakraCost);
               this.addCurrentJutsuXp(stack, 1);
               player.addExhaustion(0.4F);
               if (current.index == ItemShrineRelease.DISMANTLE.index) {
                  this.setJutsuCooldown(stack, current, 25L);
               } else if (current.index == ItemShrineRelease.DIVINE_FLAMES.index) {
                  this.setJutsuCooldown(stack, current, 240L);
               } else if (current.index == ItemShrineRelease.MALEVOLENT_SHRINE.index) {
                  this.setJutsuCooldown(stack, current, 6000L);
               }

               return true;
            } else {
               return false;
            }
         }
      }

      private float getManualChargePower(ItemStack stack, EntityLivingBase entity, int timeLeft, ItemJutsu.JutsuEnum current) {
         ItemJutsu.IJutsuCallback rawCallback = this.getRawCallback(current);
         if (rawCallback == null) {
            return 0.0F;
         } else {
            float basePower = rawCallback.getBasePower();
            float maxPower = rawCallback.getMaxPower(stack, entity);
            float powerupDelay = rawCallback.getPowerupDelay(stack, entity);
            if (powerupDelay <= 0.0F) {
               return basePower;
            } else {
               int useTicks = Math.max(0, this.getMaxItemUseDuration(stack) - timeLeft);
               float power = basePower + (float)useTicks / powerupDelay;
               return MathHelper.clamp(power, basePower, maxPower);
            }
         }
      }

      private boolean tryDeactivateHeian(ItemStack stack, World world, EntityPlayer player) {
         ItemJutsu.JutsuEnum current = this.getCurrentJutsu(stack);
         if (!stack.isEmpty() && stack.getItem() instanceof RangedItem && current != null && current.index == ItemShrineRelease.HEIAN_ERA_TRANSFORMATION.index && player.isSneaking() && EntityShrineHeianEraTransformation.isActive(player)) {
            if (!world.isRemote) {
               ItemShrineRelease.RAW_HEIAN_ERA_TRANSFORMATION_JUTSU.deactivate(player);
            }

            return true;
         } else {
            return false;
         }
      }

      private ItemJutsu.IJutsuCallback getRawCallback(ItemJutsu.JutsuEnum current) {
         if (current == null) {
            return null;
         } else if (current.index == ItemShrineRelease.DISMANTLE.index) {
            return ItemShrineRelease.RAW_DISMANTLE_JUTSU;
         } else if (current.index == ItemShrineRelease.CLEAVE.index) {
            return ItemShrineRelease.RAW_CLEAVE_JUTSU;
         } else if (current.index == ItemShrineRelease.DIVINE_FLAMES.index) {
            return ItemShrineRelease.RAW_DIVINE_FLAMES_JUTSU;
         } else if (current.index == ItemShrineRelease.MALEVOLENT_SHRINE.index) {
            return ItemShrineRelease.RAW_MALEVOLENT_SHRINE_JUTSU;
         } else {
            return current.index == ItemShrineRelease.HEIAN_ERA_TRANSFORMATION.index ? ItemShrineRelease.RAW_HEIAN_ERA_TRANSFORMATION_JUTSU : null;
         }
      }

      private void ensureFrameworkAccess(ItemStack stack) {
         NBTTagCompound nbt = this.getOrCreateTag(stack);
         this.seedJutsu(stack, ItemShrineRelease.DISMANTLE);
         this.seedJutsu(stack, ItemShrineRelease.CLEAVE);
         this.seedJutsu(stack, ItemShrineRelease.DIVINE_FLAMES);
         this.seedJutsu(stack, ItemShrineRelease.MALEVOLENT_SHRINE);
         this.seedJutsu(stack, ItemShrineRelease.HEIAN_ERA_TRANSFORMATION);
         nbt.setBoolean("ShrineFrameworkReady", true);
      }

      private void normalizeDivineFlamesCooldown(ItemStack stack, World world) {
         if (world != null) {
            NBTTagCompound nbt = this.getOrCreateTag(stack);
            String key = "JutsuCDMapKey" + ItemShrineRelease.DIVINE_FLAMES.index;
            long value = nbt.getLong(key);
            long now = world.getTotalWorldTime();
            long remaining = value - now;
            if (value < 0L || remaining > 260L) {
               nbt.setLong(key, 0L);
            }

         }
      }

      private void normalizeMalevolentShrineCooldown(ItemStack stack, World world) {
         if (world != null) {
            NBTTagCompound nbt = this.getOrCreateTag(stack);
            String key = "JutsuCDMapKey" + ItemShrineRelease.MALEVOLENT_SHRINE.index;
            long value = nbt.getLong(key);
            long now = world.getTotalWorldTime();
            long remaining = value - now;
            if (value < 0L || remaining > 6020L) {
               nbt.setLong(key, 0L);
            }

         }
      }

      private NBTTagCompound getOrCreateTag(ItemStack stack) {
         NBTTagCompound nbt = stack.getTagCompound();
         if (nbt == null) {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
         }

         return nbt;
      }

      private void seedJutsu(ItemStack stack, ItemJutsu.JutsuEnum jutsu) {
         if (!this.isJutsuEnabled(stack, jutsu)) {
            this.enableJutsu(stack, jutsu, true);
         }

         if (this.getJutsuCooldown(stack, jutsu) < 0L) {
            this.setJutsuCooldown(stack, jutsu, 0L);
         }

      }

      public boolean showSkinLayer() {
         return true;
      }

      public BodyPart showOnBody(ItemStack stack) {
         return BodyPart.NONE;
      }

      @SideOnly(Side.CLIENT)
      public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
         return EntityShrineHeianEraTransformation.isActive(living) ? ShrineHeianClientHooks.getBodyModel(defaultModel) : null;
      }

      public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
         return entity instanceof EntityLivingBase ? EntityShrineHeianEraTransformation.getArmorTexture((EntityLivingBase)entity) : null;
      }
   }

   private static class PlaceholderJutsu implements ItemJutsu.IJutsuCallback {
      private final String displayName;

      private PlaceholderJutsu(String displayName) {
         this.displayName = displayName;
      }

      public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
         if (!entity.world.isRemote && entity instanceof EntityPlayer) {
            entity.sendMessage(new TextComponentString("§4[Shrine Release] §7" + this.displayName + " is framework-only and not implemented yet."));
         }

         return false;
      }
   }
}
