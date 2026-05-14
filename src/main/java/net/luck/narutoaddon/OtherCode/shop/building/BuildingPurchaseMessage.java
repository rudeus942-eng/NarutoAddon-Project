
package net.luck.narutoaddon.OtherCode.shop.building;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.luck.narutoaddon.OtherCode.shop.network.ShopNetworkHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BuildingPurchaseMessage implements IMessage {
   private static final int MAX_CART_ITEMS = 50;
   private static final int MAX_QUANTITY_PER_ITEM = 576;
   private List<CartEntry> entries;

   public BuildingPurchaseMessage() {
      this.entries = new ArrayList();
   }

   public BuildingPurchaseMessage(List<CartEntry> entries) {
      this.entries = (List<CartEntry>)(entries != null ? entries : new ArrayList());
   }

   public void addItem(String registryName, int meta, int quantity) {
      this.entries.add(new CartEntry(registryName, meta, quantity, false));
   }

   public void addShulkerBox(String registryName, int meta, int quantity) {
      this.entries.add(new CartEntry(registryName, meta, quantity, true));
   }

   public void fromBytes(ByteBuf buf) {
      int count = buf.readInt();
      this.entries = new ArrayList(Math.min(count, 50));

      for(int i = 0; i < count; ++i) {
         String registryName = ByteBufUtils.readUTF8String(buf);
         int meta = buf.readInt();
         int quantity = buf.readInt();
         boolean isShulker = buf.readBoolean();
         this.entries.add(new CartEntry(registryName, meta, quantity, isShulker));
      }

   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.entries.size());

      for(CartEntry entry : this.entries) {
         ByteBufUtils.writeUTF8String(buf, entry.registryName);
         buf.writeInt(entry.meta);
         buf.writeInt(entry.quantity);
         buf.writeBoolean(entry.isShulkerBox);
      }

   }

   public static class CartEntry {
      public final String registryName;
      public final int meta;
      public final int quantity;
      public final boolean isShulkerBox;

      public CartEntry(String registryName, int meta, int quantity) {
         this(registryName, meta, quantity, false);
      }

      public CartEntry(String registryName, int meta, int quantity, boolean isShulkerBox) {
         this.registryName = registryName;
         this.meta = meta;
         this.quantity = quantity;
         this.isShulkerBox = isShulkerBox;
      }
   }

   public static class Handler implements IMessageHandler<BuildingPurchaseMessage, IMessage> {
      private static final ConcurrentHashMap<UUID, Long> lastPurchaseTime = new ConcurrentHashMap();
      private static final long PURCHASE_COOLDOWN_MS = 500L;

      public IMessage onMessage(BuildingPurchaseMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         WorldServer world = (WorldServer)player.world;
         world.addScheduledTask(() -> handlePurchase(player, message.entries));
         return null;
      }

      private static void handlePurchase(EntityPlayerMP player, List<CartEntry> entries) {
         if (player != null && !player.isDead) {
            UUID playerId = player.getUniqueID();
            long now = System.currentTimeMillis();
            Long lastTime = (Long)lastPurchaseTime.get(playerId);
            if (lastTime == null || now - lastTime >= 500L) {
               lastPurchaseTime.put(playerId, now);
               if (entries != null && !entries.isEmpty()) {
                  if (entries.size() > 50) {
                     player.sendMessage(new TextComponentString("§c[Building Shop] Cart exceeds maximum of 50 item types."));
                  } else {
                     ShopSavedData data = ShopSavedData.get(player.world);
                     long totalCost = 0L;
                     List<ValidatedEntry> validated = new ArrayList(entries.size());

                     for(CartEntry entry : entries) {
                        if (entry.quantity >= 1 && entry.quantity <= 576) {
                           if (entry.registryName != null && !entry.registryName.isEmpty() && entry.registryName.length() <= 128) {
                              BuildingShopRegistry.BuildingItem shopItem = BuildingShopRegistry.findItem(entry.registryName, entry.meta);
                              if (shopItem == null) {
                                 player.sendMessage(new TextComponentString("§c[Building Shop] Item not available: " + entry.registryName + ":" + entry.meta));
                                 return;
                              }

                              long itemCost;
                              if (BuildingShopRegistry.isToolkit(entry.registryName)) {
                                 itemCost = (long)shopItem.ryoPrice * (long)entry.quantity;
                              } else if (entry.isShulkerBox) {
                                 ItemStack probe = resolveItemStack(entry.registryName, entry.meta, 1);
                                 int maxStack = probe.isEmpty() ? 64 : probe.getMaxStackSize();
                                 itemCost = (long)shopItem.ryoPrice * 27L * (long)maxStack * (long)entry.quantity;
                              } else {
                                 itemCost = (long)shopItem.ryoPrice * (long)entry.quantity;
                              }

                              totalCost += itemCost;
                              if (totalCost < 0L) {
                                 player.sendMessage(new TextComponentString("§c[Building Shop] Cart total is too large."));
                                 return;
                              }

                              validated.add(new ValidatedEntry(entry.registryName, entry.meta, entry.quantity, (long)shopItem.ryoPrice, entry.isShulkerBox));
                              continue;
                           }

                           player.sendMessage(new TextComponentString("§c[Building Shop] Invalid item in cart."));
                           return;
                        }

                        player.sendMessage(new TextComponentString("§c[Building Shop] Invalid quantity for " + entry.registryName + ": " + entry.quantity + " (must be 1-" + 576 + ")."));
                        return;
                     }

                     long balance = data.getBalance(playerId);
                     if (balance < totalCost) {
                        player.sendMessage(new TextComponentString("§c[Building Shop] Not enough Ryo! Cost: ¥" + formatNumber(totalCost) + " | Balance: ¥" + formatNumber(balance)));
                     } else if (!data.removeBalance(playerId, totalCost)) {
                        player.sendMessage(new TextComponentString("§c[Building Shop] Transaction failed. Please try again."));
                     } else {
                        int totalItemsGranted = 0;
                        int overflowCount = 0;

                        for(ValidatedEntry ve : validated) {
                           if (BuildingShopRegistry.isToolkit(ve.registryName)) {
                              int enchLevel = BuildingShopRegistry.getToolkitEnchantLevel(ve.registryName);

                              for(int kit = 0; kit < ve.quantity; ++kit) {
                                 ItemStack toolkitBox = createToolkitShulkerBox(enchLevel);
                                 if (!toolkitBox.isEmpty()) {
                                    if (player.inventory.addItemStackToInventory(toolkitBox)) {
                                       ++totalItemsGranted;
                                    } else {
                                       NBTTagCompound itemNbt = new NBTTagCompound();
                                       toolkitBox.writeToNBT(itemNbt);
                                       data.addOverflowItem(playerId, itemNbt);
                                       ++overflowCount;
                                    }
                                 }
                              }
                           } else {
                              ItemStack testStack = resolveItemStack(ve.registryName, ve.meta, 1);
                              if (testStack.isEmpty()) {
                                 System.out.println("[BuildingShop] WARNING: Item '" + ve.registryName + "' resolved in registry but not found as item or block — skipping");
                              } else if (ve.isShulkerBox) {
                                 int maxStackSize = testStack.getMaxStackSize();

                                 for(int box = 0; box < ve.quantity; ++box) {
                                    ItemStack shulkerStack = createFilledShulkerBox(ve.registryName, ve.meta, maxStackSize);
                                    if (!shulkerStack.isEmpty()) {
                                       if (player.inventory.addItemStackToInventory(shulkerStack)) {
                                          ++totalItemsGranted;
                                       } else {
                                          NBTTagCompound itemNbt = new NBTTagCompound();
                                          shulkerStack.writeToNBT(itemNbt);
                                          data.addOverflowItem(playerId, itemNbt);
                                          ++overflowCount;
                                       }
                                    }
                                 }
                              } else {
                                 Item item = testStack.getItem();
                                 int remaining = ve.quantity;
                                 int maxStackSize = testStack.getMaxStackSize();
                                 if (maxStackSize <= 0) {
                                    maxStackSize = 64;
                                 }

                                 int stackSize;
                                 for(; remaining > 0; remaining -= stackSize) {
                                    stackSize = Math.min(remaining, maxStackSize);
                                    ItemStack stack = new ItemStack(item, stackSize, ve.meta);
                                    if (player.inventory.addItemStackToInventory(stack)) {
                                       totalItemsGranted += stackSize;
                                    } else {
                                       int leftover = stack.getCount();
                                       int added = stackSize - leftover;
                                       totalItemsGranted += added;
                                       if (leftover > 0) {
                                          NBTTagCompound itemNbt = new NBTTagCompound();
                                          itemNbt.setString("itemId", ve.registryName);
                                          itemNbt.setInteger("meta", ve.meta);
                                          itemNbt.setInteger("count", leftover);
                                          data.addOverflowItem(playerId, itemNbt);
                                          overflowCount += leftover;
                                       }
                                    }
                                 }
                              }
                           }
                        }

                        player.sendMessage(new TextComponentString("§a[Building Shop] Purchased " + totalItemsGranted + " items for ¥" + formatNumber(totalCost) + " Ryo!"));
                        if (overflowCount > 0) {
                           player.sendMessage(new TextComponentString("§e[Building Shop] " + overflowCount + " items placed in your bank (inventory full)"));
                        }

                        ShopNetworkHelper.sendShopSync(player);
                        System.out.println("[BuildingShop] " + player.getName() + " purchased " + validated.size() + " item types (" + (totalItemsGranted + overflowCount) + " total) for " + totalCost + " ryo" + (overflowCount > 0 ? " (" + overflowCount + " overflow)" : ""));
                     }
                  }
               }
            }
         }
      }

      private static String formatNumber(long number) {
         return String.format("%,d", number);
      }

      private static ItemStack createToolkitShulkerBox(int enchantLevel) {
         Item shulkerItem = Item.getByNameOrId("minecraft:white_shulker_box");
         if (shulkerItem == null) {
            Block shulkerBlock = Block.getBlockFromName("minecraft:white_shulker_box");
            if (shulkerBlock != null) {
               shulkerItem = Item.getItemFromBlock(shulkerBlock);
            }
         }

         if (shulkerItem == null) {
            return ItemStack.EMPTY;
         } else {
            ItemStack shulkerStack = new ItemStack(shulkerItem, 1, 0);
            NBTTagCompound blockEntityTag = new NBTTagCompound();
            NBTTagList itemsList = new NBTTagList();
            Item[] tools = new Item[]{Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE};

            for(int slot = 0; slot < tools.length; ++slot) {
               ItemStack tool = new ItemStack(tools[slot]);
               tool.addEnchantment(Enchantments.UNBREAKING, enchantLevel);
               tool.addEnchantment(Enchantments.EFFICIENCY, enchantLevel);
               NBTTagCompound slotTag = new NBTTagCompound();
               tool.writeToNBT(slotTag);
               slotTag.setByte("Slot", (byte)slot);
               itemsList.appendTag(slotTag);
            }

            blockEntityTag.setTag("Items", itemsList);
            shulkerStack.setTagInfo("BlockEntityTag", blockEntityTag);
            String romanLevel;
            switch (enchantLevel) {
               case 1:
                  romanLevel = "I";
                  break;
               case 2:
                  romanLevel = "II";
                  break;
               case 3:
                  romanLevel = "III";
                  break;
               case 4:
                  romanLevel = "IV";
                  break;
               case 5:
                  romanLevel = "V";
                  break;
               default:
                  romanLevel = String.valueOf(enchantLevel);
            }

            shulkerStack.setStackDisplayName("§fEnchant " + romanLevel + " Tool Kit");
            return shulkerStack;
         }
      }

      private static ItemStack createFilledShulkerBox(String registryName, int meta, int maxStackSize) {
         Item shulkerItem = Item.getByNameOrId("minecraft:white_shulker_box");
         if (shulkerItem == null) {
            Block shulkerBlock = Block.getBlockFromName("minecraft:white_shulker_box");
            if (shulkerBlock != null) {
               shulkerItem = Item.getItemFromBlock(shulkerBlock);
            }
         }

         if (shulkerItem == null) {
            return ItemStack.EMPTY;
         } else {
            ItemStack shulkerStack = new ItemStack(shulkerItem, 1, 0);
            NBTTagCompound blockEntityTag = new NBTTagCompound();
            NBTTagList itemsList = new NBTTagList();

            for(int slot = 0; slot < 27; ++slot) {
               ItemStack contentStack = resolveItemStack(registryName, meta, maxStackSize);
               if (!contentStack.isEmpty()) {
                  NBTTagCompound slotTag = new NBTTagCompound();
                  contentStack.writeToNBT(slotTag);
                  slotTag.setByte("Slot", (byte)slot);
                  itemsList.appendTag(slotTag);
               }
            }

            blockEntityTag.setTag("Items", itemsList);
            shulkerStack.setTagInfo("BlockEntityTag", blockEntityTag);
            ItemStack contentSample = resolveItemStack(registryName, meta, 1);
            String itemDisplayName = contentSample.isEmpty() ? registryName : contentSample.getDisplayName();
            shulkerStack.setStackDisplayName(itemDisplayName + " Box");
            return shulkerStack;
         }
      }

      private static ItemStack resolveItemStack(String registryName, int meta, int count) {
         ResourceLocation rl = new ResourceLocation(registryName);
         Item item = (Item)Item.REGISTRY.getObject(rl);
         if (item != null) {
            ItemStack stack = new ItemStack(item, count, meta);
            if (!stack.isEmpty()) {
               return stack;
            }
         }

         Block block = (Block)Block.REGISTRY.getObject(rl);
         if (block != null && block != Blocks.AIR) {
            ItemStack stack = new ItemStack(block, count, meta);
            if (!stack.isEmpty()) {
               return stack;
            }
         }

         item = Item.getByNameOrId(registryName);
         if (item != null) {
            ItemStack stack = new ItemStack(item, count, meta);
            if (!stack.isEmpty()) {
               return stack;
            }
         }

         return ItemStack.EMPTY;
      }

      private static class ValidatedEntry {
         final String registryName;
         final int meta;
         final int quantity;
         final long ryoPrice;
         final boolean isShulkerBox;

         ValidatedEntry(String registryName, int meta, int quantity, long ryoPrice, boolean isShulkerBox) {
            this.registryName = registryName;
            this.meta = meta;
            this.quantity = quantity;
            this.ryoPrice = ryoPrice;
            this.isShulkerBox = isShulkerBox;
         }
      }
   }
}
