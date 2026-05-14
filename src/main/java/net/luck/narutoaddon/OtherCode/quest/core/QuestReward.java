
package net.luck.narutoaddon.OtherCode.quest.core;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class QuestReward {
   public final RewardType type;
   public final String itemId;
   public final int itemCount;
   public final int itemMeta;
   public final String command;
   public final int xpAmount;
   private String displayMessage = null;

   private QuestReward(RewardType type, String itemId, int itemCount, int itemMeta, String command, int xpAmount) {
      this.type = type;
      this.itemId = itemId;
      this.itemCount = itemCount;
      this.itemMeta = itemMeta;
      this.command = command;
      this.xpAmount = xpAmount;
   }

   public QuestReward withMessage(String msg) {
      this.displayMessage = msg;
      return this;
   }

   public static QuestReward item(String id, int count, int meta) {
      return new QuestReward(RewardType.ITEM, id, count, meta, (String)null, 0);
   }

   public static QuestReward command(String cmd) {
      return new QuestReward(RewardType.COMMAND, (String)null, 0, 0, cmd, 0);
   }

   public static QuestReward xp(int amount) {
      return new QuestReward(RewardType.XP, (String)null, 0, 0, (String)null, amount);
   }

   public void grant(EntityPlayerMP player) {
      switch (this.type) {
         case ITEM:
            Item item = Item.getByNameOrId(this.itemId);
            if (item != null) {
               ItemStack stack = new ItemStack(item, this.itemCount, this.itemMeta);
               if (!player.inventory.addItemStackToInventory(stack)) {
                  player.dropItem(stack, false);
               }

               player.sendMessage(new TextComponentString("§aReceived: §f" + stack.getDisplayName() + " x" + this.itemCount));
            }
            break;
         case COMMAND:
            if (this.command != null) {
               String resolved = this.command.replace("{player}", player.getName());
               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               if (server != null) {
                  System.out.println("[QuestReward] Executing command: " + resolved);
                  int result = server.getCommandManager().executeCommand(server, resolved);
                  System.out.println("[QuestReward] Command result: " + result + " (0 = failed, 1+ = success)");
               }
            }
            break;
         case XP:
            MinecraftServer xpServer = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (xpServer != null) {
               String xpCmd = "runasop " + player.getName() + " addninjaxp " + player.getName() + " " + this.xpAmount;
               xpServer.getCommandManager().executeCommand(xpServer, xpCmd);
            }

            player.sendMessage(new TextComponentString("§aReceived: §e" + this.xpAmount + " Ninja XP"));
      }

      if (this.displayMessage != null && !this.displayMessage.isEmpty()) {
         player.sendMessage(new TextComponentString(this.displayMessage));
      }

   }

   public static enum RewardType {
      ITEM,
      COMMAND,
      XP;
   }
}
