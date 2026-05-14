package net.luck.narutoaddon.OtherCode.akatsuki.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiMember;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiSavedData;
import net.luck.narutoaddon.OtherCode.akatsuki.core.RingUpgradeConstants;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class AkatsukiRingUpgradeMessage implements IMessage {
   private int slotIndex;

   public AkatsukiRingUpgradeMessage() {
   }

   public AkatsukiRingUpgradeMessage(int slotIndex) {
      this.slotIndex = slotIndex;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.slotIndex);
   }

   public void fromBytes(ByteBuf buf) {
      this.slotIndex = buf.readInt();
   }

   public static class Handler implements IMessageHandler<AkatsukiRingUpgradeMessage, IMessage> {
      public IMessage onMessage(AkatsukiRingUpgradeMessage msg, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
            if (msg.slotIndex >= 0 && msg.slotIndex < 5) {
               AkatsukiMember member = AkatsukiManager.getInstance().getMember(player.getUniqueID());
               if (member != null) {
                  int currentLevel = member.getRingUpgradeLevel(msg.slotIndex);
                  if (currentLevel >= 5) {
                     player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "That upgrade is already at max level."));
                  } else {
                     int cost = RingUpgradeConstants.getUpgradeCost(msg.slotIndex, currentLevel);
                     if (member.getBountyTokens() < cost) {
                        player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "Not enough bounty tokens. Need " + cost + "."));
                     } else {
                        member.addBountyTokens(-cost);
                        member.upgradeRing(msg.slotIndex);
                        AkatsukiSavedData data = AkatsukiSavedData.get(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
                        if (data != null) {
                           data.markDirty();
                        }

                        AkatsukiManager.getInstance().syncToClient(player);
                        player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "Upgraded " + RingUpgradeConstants.UPGRADE_NAMES[msg.slotIndex] + " to level " + (currentLevel + 1) + "."));
                     }
                  }
               }
            }
         });
         return null;
      }
   }
}
