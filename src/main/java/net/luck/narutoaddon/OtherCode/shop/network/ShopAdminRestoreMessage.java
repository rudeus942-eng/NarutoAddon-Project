
package net.luck.narutoaddon.OtherCode.shop.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.shop.core.PlayerItemTracker;
import net.luck.narutoaddon.OtherCode.shop.core.RestoreLogEntry;
import net.luck.narutoaddon.OtherCode.shop.core.TrackedItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class ShopAdminRestoreMessage implements IMessage {
   private String targetUUID;
   private String itemRegistryName;
   private int itemMeta;
   private String itemDisplayName;

   public ShopAdminRestoreMessage() {
      this.targetUUID = "";
      this.itemRegistryName = "";
      this.itemDisplayName = "";
   }

   public ShopAdminRestoreMessage(String targetUUID, String itemRegistryName, int itemMeta, String itemDisplayName) {
      this.targetUUID = targetUUID != null ? targetUUID : "";
      this.itemRegistryName = itemRegistryName != null ? itemRegistryName : "";
      this.itemMeta = itemMeta;
      this.itemDisplayName = itemDisplayName != null ? itemDisplayName : "";
   }

   public void fromBytes(ByteBuf buf) {
      this.targetUUID = ByteBufUtils.readUTF8String(buf);
      this.itemRegistryName = ByteBufUtils.readUTF8String(buf);
      this.itemMeta = buf.readInt();
      this.itemDisplayName = ByteBufUtils.readUTF8String(buf);
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.targetUUID);
      ByteBufUtils.writeUTF8String(buf, this.itemRegistryName);
      buf.writeInt(this.itemMeta);
      ByteBufUtils.writeUTF8String(buf, this.itemDisplayName);
   }

   public static class Handler implements IMessageHandler<ShopAdminRestoreMessage, IMessage> {
      public IMessage onMessage(ShopAdminRestoreMessage message, MessageContext ctx) {
         EntityPlayerMP admin = ctx.getServerHandler().player;
         MinecraftServer server = admin.getServer();
         if (server == null) {
            return null;
         } else {
            server.addScheduledTask(() -> {
               if (!admin.canUseCommand(2, "shopadmin")) {
                  admin.sendMessage(new TextComponentString("§cYou do not have permission to restore items."));
               } else {
                  PlayerItemTracker tracker = PlayerItemTracker.get(admin.getServerWorld());

                  UUID targetId;
                  try {
                     targetId = UUID.fromString(message.targetUUID);
                  } catch (IllegalArgumentException var10) {
                     admin.sendMessage(new TextComponentString("§cInvalid player UUID."));
                     return;
                  }

                  TrackedItem trackedItem = tracker.findTrackedItem(targetId, message.itemRegistryName, message.itemMeta, message.itemDisplayName);
                  if (trackedItem == null) {
                     trackedItem = tracker.findTrackedItem(targetId, message.itemRegistryName, message.itemMeta);
                  }

                  if (trackedItem == null) {
                     admin.sendMessage(new TextComponentString("§cTracked item not found for that player."));
                  } else if (trackedItem.nbtSnapshot == null) {
                     admin.sendMessage(new TextComponentString("§cNo NBT snapshot available for this item. Cannot restore."));
                  } else {
                     Item item = Item.getByNameOrId(trackedItem.itemRegistryName);
                     if (item == null) {
                        admin.sendMessage(new TextComponentString("§cItem registry name not found: " + trackedItem.itemRegistryName));
                     } else {
                        ItemStack restored = new ItemStack(item, 1, trackedItem.meta);
                        if (trackedItem.nbtSnapshot != null) {
                           restored.setTagCompound(trackedItem.nbtSnapshot.copy());
                        }

                        EntityPlayerMP target = server.getPlayerList().getPlayerByUUID(targetId);
                        if (target != null) {
                           String targetName = target.getName();
                           if (!target.addItemStackToInventory(restored)) {
                              target.entityDropItem(restored, 0.0F);
                              admin.sendMessage(new TextComponentString("§eInventory full. Item dropped at " + targetName + "'s feet."));
                           }

                           target.sendMessage(new TextComponentString("§a[ShopAdmin] " + trackedItem.displayName + " has been restored to your inventory by " + admin.getName() + "."));
                           trackedItem.currentlyOwned = true;
                           trackedItem.lastSeenTimestamp = System.currentTimeMillis();
                           tracker.addOrUpdateItem(targetId, trackedItem);
                           tracker.addRestoreLogEntry(targetId, new RestoreLogEntry(admin.getName(), admin.getUniqueID().toString(), trackedItem.itemRegistryName, trackedItem.displayName, targetName));
                           System.out.println("[ShopAdmin] " + admin.getName() + " restored " + trackedItem.displayName + " to " + targetName);
                           admin.sendMessage(new TextComponentString("§aSuccessfully restored " + trackedItem.displayName + " to " + targetName + "."));
                           ShopNetworkHelper.sendAdminSync(server, targetId, targetName, admin);
                        } else {
                           admin.sendMessage(new TextComponentString("§cTarget player is offline. Cannot restore item. They must be online to receive items."));
                        }
                     }
                  }
               }
            });
            return null;
         }
      }
   }
}
