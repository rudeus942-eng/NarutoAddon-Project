package net.luck.narutoaddon.OtherCode.akatsuki.contract;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class ContractActionMessage implements IMessage {
   public static final int ACTION_POST = 0;
   public static final int ACTION_ACCEPT = 1;
   public static final int ACTION_CANCEL = 2;
   private int action;
   private int typeOrdinal;
   private String targetName = "";
   private int ryoAmount;
   private String targetZoneId = "";
   private String contractId = "";

   public static ContractActionMessage post(int typeOrdinal, String targetName, int ryoAmount, String targetZoneId) {
      ContractActionMessage msg = new ContractActionMessage();
      msg.action = 0;
      msg.typeOrdinal = typeOrdinal;
      msg.targetName = targetName != null ? targetName : "";
      msg.ryoAmount = ryoAmount;
      msg.targetZoneId = targetZoneId != null ? targetZoneId : "";
      return msg;
   }

   public static ContractActionMessage accept(String contractId) {
      ContractActionMessage msg = new ContractActionMessage();
      msg.action = 1;
      msg.contractId = contractId;
      return msg;
   }

   public static ContractActionMessage cancel(String contractId) {
      ContractActionMessage msg = new ContractActionMessage();
      msg.action = 2;
      msg.contractId = contractId;
      return msg;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.action);
      switch (this.action) {
         case 0:
            buf.writeInt(this.typeOrdinal);
            ByteBufUtils.writeUTF8String(buf, this.targetName);
            buf.writeInt(this.ryoAmount);
            ByteBufUtils.writeUTF8String(buf, this.targetZoneId);
            break;
         case 1:
         case 2:
            ByteBufUtils.writeUTF8String(buf, this.contractId);
      }

   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readInt();
      switch (this.action) {
         case 0:
            this.typeOrdinal = buf.readInt();
            this.targetName = ByteBufUtils.readUTF8String(buf);
            this.ryoAmount = buf.readInt();
            this.targetZoneId = ByteBufUtils.readUTF8String(buf);
            break;
         case 1:
         case 2:
            this.contractId = ByteBufUtils.readUTF8String(buf);
      }

   }

   public static class Handler implements IMessageHandler<ContractActionMessage, IMessage> {
      public IMessage onMessage(ContractActionMessage msg, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
            ContractManager cm = ContractManager.getInstance();
            switch (msg.action) {
               case 0:
                  Contract.ContractType[] types = Contract.ContractType.values();
                  if (msg.typeOrdinal < 0 || msg.typeOrdinal >= types.length) {
                     return;
                  }

                  cm.postContract(player, types[msg.typeOrdinal], msg.targetName, msg.ryoAmount, msg.targetZoneId);
                  break;
               case 1:
                  try {
                     UUID cId = UUID.fromString(msg.contractId);
                     cm.acceptContract(cId, player);
                  } catch (IllegalArgumentException var5) {
                  }
                  break;
               case 2:
                  try {
                     UUID cId = UUID.fromString(msg.contractId);
                     cm.cancelContract(cId, player);
                  } catch (IllegalArgumentException var4) {
                  }
            }

         });
         return null;
      }
   }
}
