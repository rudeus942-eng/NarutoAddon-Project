
package net.luck.narutoaddon.OtherCode.shop.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassDefinition;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;

public class BattlePassSyncMessage implements IMessage {
   private int seasonNumber;
   private String seasonName;
   private long seasonEndTime;
   private int playerTier;
   private int playerXP;
   private int purchasedTier;
   private Set<Integer> claimedTiers;
   private String activeKillEffect;
   private Set<String> ownedKillEffects;
   private int otsutsukiSkipsRemaining;

   public BattlePassSyncMessage() {
      this.seasonName = "";
      this.claimedTiers = new HashSet();
      this.activeKillEffect = "";
      this.ownedKillEffects = new HashSet();
   }

   public BattlePassSyncMessage(int seasonNumber, String seasonName, long seasonEndTime, int playerTier, int playerXP, int purchasedTier, Set<Integer> claimedTiers, String activeKillEffect, Set<String> ownedKillEffects, int otsutsukiSkipsRemaining) {
      this.seasonNumber = seasonNumber;
      this.seasonName = seasonName != null ? seasonName : "";
      this.seasonEndTime = seasonEndTime;
      this.playerTier = playerTier;
      this.playerXP = playerXP;
      this.purchasedTier = purchasedTier;
      this.claimedTiers = (Set<Integer>)(claimedTiers != null ? claimedTiers : new HashSet());
      this.activeKillEffect = activeKillEffect != null ? activeKillEffect : "";
      this.ownedKillEffects = (Set<String>)(ownedKillEffects != null ? ownedKillEffects : new HashSet());
      this.otsutsukiSkipsRemaining = otsutsukiSkipsRemaining;
   }

   public void fromBytes(ByteBuf buf) {
      this.seasonNumber = buf.readInt();
      this.seasonName = ByteBufUtils.readUTF8String(buf);
      this.seasonEndTime = buf.readLong();
      this.playerTier = buf.readInt();
      this.playerXP = buf.readInt();
      this.purchasedTier = buf.readInt();
      int claimedCount = buf.readInt();
      this.claimedTiers = new HashSet();

      for(int i = 0; i < claimedCount; ++i) {
         this.claimedTiers.add(buf.readInt());
      }

      this.activeKillEffect = ByteBufUtils.readUTF8String(buf);
      int ownedCount = buf.readInt();
      this.ownedKillEffects = new HashSet();

      for(int i = 0; i < ownedCount; ++i) {
         this.ownedKillEffects.add(ByteBufUtils.readUTF8String(buf));
      }

      this.otsutsukiSkipsRemaining = buf.readInt();
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.seasonNumber);
      ByteBufUtils.writeUTF8String(buf, this.seasonName);
      buf.writeLong(this.seasonEndTime);
      buf.writeInt(this.playerTier);
      buf.writeInt(this.playerXP);
      buf.writeInt(this.purchasedTier);
      buf.writeInt(this.claimedTiers.size());

      for(int tier : this.claimedTiers) {
         buf.writeInt(tier);
      }

      ByteBufUtils.writeUTF8String(buf, this.activeKillEffect);
      buf.writeInt(this.ownedKillEffects.size());

      for(String effectId : this.ownedKillEffects) {
         ByteBufUtils.writeUTF8String(buf, effectId);
      }

      buf.writeInt(this.otsutsukiSkipsRemaining);
   }

   public static class Handler implements IMessageHandler<BattlePassSyncMessage, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(BattlePassSyncMessage message, MessageContext ctx) {
         Minecraft.getMinecraft().addScheduledTask(() -> {
            BattlePassClientData.seasonNumber = message.seasonNumber;
            BattlePassClientData.seasonName = message.seasonName;
            BattlePassClientData.seasonEndTime = message.seasonEndTime;
            BattlePassClientData.playerTier = message.playerTier;
            BattlePassClientData.playerXP = message.playerXP;
            BattlePassClientData.purchasedTier = message.purchasedTier;
            BattlePassClientData.claimedTiers = new HashSet(message.claimedTiers);
            BattlePassClientData.activeKillEffect = message.activeKillEffect;
            BattlePassClientData.ownedKillEffects = new HashSet(message.ownedKillEffects);
            BattlePassClientData.otsutsukiSkipsRemaining = message.otsutsukiSkipsRemaining;
            ShopClientData.bpSeasonNumber = message.seasonNumber;
            ShopClientData.bpSeasonName = message.seasonName;
            ShopClientData.bpSeasonEndTime = message.seasonEndTime;
            ShopClientData.bpCurrentTier = message.playerTier;
            ShopClientData.bpCurrentXP = message.playerXP;
            ShopClientData.bpXPForNextLevel = BattlePassDefinition.getXPForLevel(message.playerTier + 1);
            ShopClientData.bpPurchasedTier = message.purchasedTier;
            ShopClientData.bpClaimedTiers = new HashSet(message.claimedTiers);
            ShopClientData.bpActiveKillEffect = message.activeKillEffect;
            ShopClientData.bpOwnedKillEffects = new HashSet(message.ownedKillEffects);
            ShopClientData.bpOtsutsukiSkipsRemaining = message.otsutsukiSkipsRemaining;
         });
         return null;
      }
   }
}
