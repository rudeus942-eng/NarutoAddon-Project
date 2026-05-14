
package net.luck.narutoaddon.OtherCode.raid.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.raid.core.RaidDifficulty;
import net.luck.narutoaddon.OtherCode.raid.core.RaidQueueManager;
import net.luck.narutoaddon.OtherCode.raid.rewards.RaidRewardDistributor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RaidQueueMessage implements IMessage {
   private String action;
   private String bossId;
   private int difficulty;

   public RaidQueueMessage() {
      this.action = "";
      this.bossId = "";
      this.difficulty = 0;
   }

   public RaidQueueMessage(String action, String bossId, int difficulty) {
      this.action = action;
      this.bossId = bossId;
      this.difficulty = difficulty;
   }

   public void fromBytes(ByteBuf buf) {
      this.action = ByteBufUtils.readUTF8String(buf);
      this.bossId = ByteBufUtils.readUTF8String(buf);
      this.difficulty = buf.readInt();
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.action);
      ByteBufUtils.writeUTF8String(buf, this.bossId);
      buf.writeInt(this.difficulty);
   }

   public static class Handler implements IMessageHandler<RaidQueueMessage, IMessage> {
      public IMessage onMessage(RaidQueueMessage message, MessageContext ctx) {
         if (ctx.side != Side.SERVER) {
            return null;
         } else {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (player == null) {
               return null;
            } else {
               player.getServerWorld().addScheduledTask(() -> this.handleMessage(message, player));
               return null;
            }
         }
      }

      private void handleMessage(RaidQueueMessage message, EntityPlayerMP player) {
         RaidQueueManager queueManager = RaidQueueManager.getInstance();
         switch (message.action) {
            case "queue":
               RaidDifficulty diff = RaidDifficulty.values()[Math.min(message.difficulty, RaidDifficulty.values().length - 1)];
               boolean joined = queueManager.joinQueue(player, message.bossId, diff);
               if (joined) {
                  player.sendMessage(new TextComponentString("§aYou have joined the queue for §6" + message.bossId + " §a(§e" + diff.getDisplayName() + "§a)"));
               } else {
                  player.sendMessage(new TextComponentString("§cFailed to join queue. You may already be in a queue or raid."));
               }
               break;
            case "leave":
               boolean left = queueManager.leaveQueue(player);
               if (left) {
                  player.sendMessage(new TextComponentString("§eYou have left the raid queue."));
               } else {
                  player.sendMessage(new TextComponentString("§cYou are not in a queue."));
               }
               break;
            case "status":
               queueManager.sendQueueStatus(player);
               break;
            case "claimrewards":
               this.claimPendingRewards(player);
         }

      }

      private void claimPendingRewards(EntityPlayerMP player) {
         RaidRewardDistributor distributor = RaidRewardDistributor.get(player.world);
         if (distributor == null) {
            player.sendMessage(new TextComponentString("§cError: Could not access reward data."));
         } else if (!distributor.hasPendingRewards(player.getUniqueID())) {
            player.sendMessage(new TextComponentString("§7You have no pending raid rewards."));
         } else {
            distributor.claimRewards(player);
         }
      }
   }
}
