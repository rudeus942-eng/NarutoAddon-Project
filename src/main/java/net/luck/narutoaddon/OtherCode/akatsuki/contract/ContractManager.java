package net.luck.narutoaddon.OtherCode.akatsuki.contract;

import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiModInit;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiSavedData;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class ContractManager {
   private static ContractManager INSTANCE = new ContractManager();
   private final Map<UUID, Contract> activeContracts = new HashMap();

   private ContractManager() {
   }

   public static ContractManager getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ContractManager();
      }

      return INSTANCE;
   }

   public static void reset() {
      INSTANCE = null;
   }

   private ShopSavedData getShopData() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      return server == null ? null : ShopSavedData.get(server.getWorld(0));
   }

   public UUID postContract(EntityPlayerMP poster, Contract.ContractType type, String target, int ryoAmount, String targetZoneId) {
      if (AkatsukiManager.getInstance().isAkatsuki(poster.getUniqueID())) {
         return null;
      } else {
         for(Contract c : this.activeContracts.values()) {
            if (c.getPosterId().equals(poster.getUniqueID()) && (c.getStatus() == Contract.ContractStatus.OPEN || c.getStatus() == Contract.ContractStatus.ACCEPTED)) {
               poster.sendMessage(new TextComponentString(TextFormatting.RED + "You already have an active contract."));
               return null;
            }
         }

         if (ryoAmount < 100) {
            poster.sendMessage(new TextComponentString(TextFormatting.RED + "Minimum contract value is 100 Ryo."));
            return null;
         } else {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (type == Contract.ContractType.ASSASSINATION) {
               if (target == null || target.isEmpty()) {
                  poster.sendMessage(new TextComponentString(TextFormatting.RED + "Assassination contract requires a target player name."));
                  return null;
               }

               if (server != null) {
                  EntityPlayerMP targetPlayer = server.getPlayerList().getPlayerByUsername(target);
                  if (targetPlayer == null) {
                     poster.sendMessage(new TextComponentString(TextFormatting.RED + "Target player '" + target + "' is not online."));
                     return null;
                  }

                  if (targetPlayer.getUniqueID().equals(poster.getUniqueID())) {
                     poster.sendMessage(new TextComponentString(TextFormatting.RED + "You cannot place a contract on yourself."));
                     return null;
                  }

                  if (AkatsukiManager.getInstance().isAkatsuki(targetPlayer.getUniqueID())) {
                     poster.sendMessage(new TextComponentString(TextFormatting.RED + "You cannot place a contract on an Akatsuki member."));
                     return null;
                  }
               }
            } else if (type == Contract.ContractType.TERRITORY) {
               if (targetZoneId == null || targetZoneId.isEmpty()) {
                  poster.sendMessage(new TextComponentString(TextFormatting.RED + "Territory contract requires a target zone."));
                  return null;
               }

               Team team = poster.getTeam();
               if (team == null || team.getName().isEmpty()) {
                  poster.sendMessage(new TextComponentString(TextFormatting.RED + "You must be in a village to hire territory help."));
                  return null;
               }
            }

            ShopSavedData shopData = this.getShopData();
            if (shopData != null && shopData.removeBalance(poster.getUniqueID(), (long)ryoAmount)) {
               Contract contract = new Contract(UUID.randomUUID(), poster.getUniqueID(), poster.getName(), type, type == Contract.ContractType.TERRITORY ? "" : target, ryoAmount);
               if (type == Contract.ContractType.TERRITORY) {
                  contract.setTargetZoneId(targetZoneId);
               }

               Team posterTeam = poster.getTeam();
               if (posterTeam != null && !posterTeam.getName().isEmpty()) {
                  contract.setPosterVillage(posterTeam.getName());
               }

               this.activeContracts.put(contract.getContractId(), contract);
               this.save();
               poster.sendMessage(new TextComponentString(TextFormatting.GOLD + "[Contract] " + TextFormatting.GRAY + "Posted " + type.displayName() + " contract for " + TextFormatting.GOLD + ryoAmount + " Ryo" + TextFormatting.GRAY + "."));
               this.syncToAllRelevant();
               return contract.getContractId();
            } else {
               poster.sendMessage(new TextComponentString(TextFormatting.RED + "Not enough Ryo."));
               return null;
            }
         }
      }
   }

   public boolean acceptContract(UUID contractId, EntityPlayerMP akatsukiPlayer) {
      Contract contract = (Contract)this.activeContracts.get(contractId);
      if (contract == null) {
         return false;
      } else if (contract.getStatus() != Contract.ContractStatus.OPEN && contract.getStatus() != Contract.ContractStatus.ACCEPTED) {
         return false;
      } else if (!AkatsukiManager.getInstance().isAkatsuki(akatsukiPlayer.getUniqueID())) {
         return false;
      } else if (contract.isAssignee(akatsukiPlayer.getUniqueID())) {
         return false;
      } else {
         int assignedCount = 0;

         for(Contract c : this.activeContracts.values()) {
            if (c.isAssignee(akatsukiPlayer.getUniqueID()) && (c.getStatus() == Contract.ContractStatus.OPEN || c.getStatus() == Contract.ContractStatus.ACCEPTED)) {
               ++assignedCount;
            }
         }

         if (assignedCount >= 2) {
            akatsukiPlayer.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "You already have 2 active contracts."));
            return false;
         } else if (!contract.addAssignee(akatsukiPlayer.getUniqueID(), akatsukiPlayer.getName())) {
            return false;
         } else {
            if (contract.getAssigneeCount() >= 2) {
               contract.setStatus(Contract.ContractStatus.ACCEPTED);
               contract.setAcceptedTimestamp(System.currentTimeMillis());
            }

            this.save();
            akatsukiPlayer.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "Accepted contract: " + contract.getType().displayName() + " (" + contract.getRyoAmount() + " Ryo)"));
            this.syncToAllRelevant();
            return true;
         }
      }
   }

   public void completeContract(UUID contractId) {
      Contract contract = (Contract)this.activeContracts.get(contractId);
      if (contract != null) {
         if (contract.getStatus() != Contract.ContractStatus.COMPLETED) {
            contract.setStatus(Contract.ContractStatus.COMPLETED);
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server == null) {
               this.save();
            } else {
               int splitAmount = contract.getRyoAmount() / 2;
               int tokensEach = Math.max(1, contract.getRyoAmount() / 20);
               ShopSavedData shopData = this.getShopData();

               for(int i = 0; i < 2; ++i) {
                  if (contract.getAssignees()[i] != null) {
                     UUID assigneeId = contract.getAssignees()[i];
                     if (shopData != null) {
                        shopData.addBalance(assigneeId, (long)splitAmount);
                     }

                     AkatsukiManager.getInstance().addBountyTokens(assigneeId, tokensEach);
                     AkatsukiManager.getInstance().addReputation(assigneeId, 25, "contract completed");
                     EntityPlayerMP assignee = server.getPlayerList().getPlayerByUUID(assigneeId);
                     if (assignee != null) {
                        assignee.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "[Akatsuki] " + TextFormatting.GRAY + "Contract completed! Earned " + TextFormatting.GOLD + splitAmount + " Ryo" + TextFormatting.GRAY + ", " + TextFormatting.YELLOW + tokensEach + " tokens" + TextFormatting.GRAY + "."));
                     }
                  }
               }

               EntityPlayerMP poster = server.getPlayerList().getPlayerByUUID(contract.getPosterId());
               if (poster != null) {
                  poster.sendMessage(new TextComponentString(TextFormatting.GOLD + "[Contract] " + TextFormatting.GRAY + "Your " + contract.getType().displayName() + " contract has been completed."));
               }

               this.save();
               this.syncToAllRelevant();
            }
         }
      }
   }

   public void onZoneCaptured(String zoneId, String capturingVillage) {
      for(Contract c : new ArrayList(this.activeContracts.values())) {
         if (c.getType() == Contract.ContractType.TERRITORY && (c.getStatus() == Contract.ContractStatus.OPEN || c.getStatus() == Contract.ContractStatus.ACCEPTED)) {
            String contractZone = c.getTargetZoneId();
            if (contractZone != null && !contractZone.isEmpty()) {
               String posterVillage = c.getPosterVillage();
               if (posterVillage != null && !posterVillage.isEmpty() && posterVillage.equalsIgnoreCase(capturingVillage)) {
                  this.completeContract(c.getContractId());
               }
            }
         }
      }

   }

   public void onPlayerKill(String victimName, UUID killerId) {
      for(Contract c : new ArrayList(this.activeContracts.values())) {
         if (c.getType() == Contract.ContractType.ASSASSINATION && (c.getStatus() == Contract.ContractStatus.ACCEPTED || c.getStatus() == Contract.ContractStatus.OPEN) && c.getAssigneeCount() != 0) {
            String target = c.getTargetName();
            if (target != null && !target.isEmpty() && target.equalsIgnoreCase(victimName)) {
               boolean isAssignee = false;

               for(UUID assignee : c.getAssignees()) {
                  if (assignee != null && assignee.equals(killerId)) {
                     isAssignee = true;
                     break;
                  }
               }

               if (isAssignee) {
                  this.completeContract(c.getContractId());
               }
            }
         }
      }

   }

   public void failContract(UUID contractId) {
      Contract contract = (Contract)this.activeContracts.get(contractId);
      if (contract != null) {
         contract.setStatus(Contract.ContractStatus.FAILED);
         this.refundPoster(contract);
         this.save();
         this.syncToAllRelevant();
      }
   }

   public void cancelContract(UUID contractId, EntityPlayerMP requester) {
      Contract contract = (Contract)this.activeContracts.get(contractId);
      if (contract != null) {
         if (contract.getPosterId().equals(requester.getUniqueID())) {
            if (contract.getStatus() != Contract.ContractStatus.OPEN) {
               requester.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot cancel a contract that has been accepted."));
            } else {
               contract.setStatus(Contract.ContractStatus.CANCELLED);
               this.refundPoster(contract);
               this.save();
               this.syncToAllRelevant();
               requester.sendMessage(new TextComponentString(TextFormatting.GOLD + "[Contract] " + TextFormatting.GRAY + "Contract cancelled. Ryo refunded."));
            }
         }
      }
   }

   private void refundPoster(Contract contract) {
      ShopSavedData shopData = this.getShopData();
      if (shopData != null) {
         shopData.addBalance(contract.getPosterId(), (long)contract.getRyoAmount());
      }

      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         EntityPlayerMP poster = server.getPlayerList().getPlayerByUUID(contract.getPosterId());
         if (poster != null) {
            poster.sendMessage(new TextComponentString(TextFormatting.GOLD + "[Contract] " + TextFormatting.GRAY + "Refunded " + TextFormatting.GOLD + contract.getRyoAmount() + " Ryo" + TextFormatting.GRAY + "."));
         }
      }

   }

   public void cleanExpired() {
      boolean changed = false;

      for(Contract c : new ArrayList(this.activeContracts.values())) {
         if (c.isExpired()) {
            c.setStatus(Contract.ContractStatus.EXPIRED);
            this.refundPoster(c);
            changed = true;
         }
      }

      if (changed) {
         this.save();
         this.syncToAllRelevant();
      }

   }

   public void onPlayerLogin(EntityPlayerMP player) {
   }

   public List<Contract> getOpenContracts() {
      List<Contract> result = new ArrayList();

      for(Contract c : this.activeContracts.values()) {
         if (c.getStatus() == Contract.ContractStatus.OPEN) {
            result.add(c);
         }
      }

      return result;
   }

   public Contract getActiveContractForPoster(UUID posterId) {
      for(Contract c : this.activeContracts.values()) {
         if (c.getPosterId().equals(posterId) && (c.getStatus() == Contract.ContractStatus.OPEN || c.getStatus() == Contract.ContractStatus.ACCEPTED)) {
            return c;
         }
      }

      return null;
   }

   public List<Contract> getContractsForAssignee(UUID playerId) {
      List<Contract> result = new ArrayList();

      for(Contract c : this.activeContracts.values()) {
         if (c.isAssignee(playerId) && (c.getStatus() == Contract.ContractStatus.OPEN || c.getStatus() == Contract.ContractStatus.ACCEPTED)) {
            result.add(c);
         }
      }

      return result;
   }

   public Contract getContract(UUID contractId) {
      return (Contract)this.activeContracts.get(contractId);
   }

   private void syncToAllRelevant() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            this.syncToClient(player);
         }

      }
   }

   public void syncToClient(EntityPlayerMP player) {
      UUID playerId = player.getUniqueID();
      boolean isAkatsuki = AkatsukiManager.getInstance().isAkatsuki(player.getUniqueID());
      ContractSyncMessage msg = new ContractSyncMessage();
      if (isAkatsuki) {
         for(Contract c : this.activeContracts.values()) {
            if (c.getStatus() == Contract.ContractStatus.OPEN || c.isAssignee(playerId) && c.getStatus() == Contract.ContractStatus.ACCEPTED) {
               msg.addEntry(this.contractToEntry(c));
            }
         }
      } else {
         for(Contract c : this.activeContracts.values()) {
            if (c.getPosterId().equals(playerId) && (c.getStatus() == Contract.ContractStatus.OPEN || c.getStatus() == Contract.ContractStatus.ACCEPTED)) {
               msg.addEntry(this.contractToEntry(c));
            }
         }
      }

      msg.setIsAkatsuki(isAkatsuki);
      AkatsukiModInit.NETWORK.sendTo(msg, player);
   }

   private ContractSyncMessage.ContractEntry contractToEntry(Contract c) {
      return new ContractSyncMessage.ContractEntry(c.getContractId().toString(), c.getPosterName(), c.getType().ordinal(), c.getTargetName(), c.getRyoAmount(), c.getStatus().ordinal(), c.getAssigneeNames()[0], c.getAssigneeNames()[1], c.getAssigneeCount(), c.getTargetZoneId(), c.getPosterVillage());
   }

   public void loadContracts(AkatsukiSavedData data) {
      this.activeContracts.clear();
      NBTTagCompound nbt = data.getContractsNBT();
      if (nbt != null) {
         NBTTagList list = nbt.getTagList("contracts", 10);

         for(int i = 0; i < list.tagCount(); ++i) {
            Contract c = new Contract();
            c.readFromNBT(list.getCompoundTagAt(i));
            this.activeContracts.put(c.getContractId(), c);
         }

      }
   }

   public void save() {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) {
         AkatsukiSavedData data = AkatsukiSavedData.get(server.getWorld(0));
         if (data != null) {
            NBTTagCompound nbt = new NBTTagCompound();
            NBTTagList list = new NBTTagList();

            for(Contract c : this.activeContracts.values()) {
               list.appendTag(c.writeToNBT());
            }

            nbt.setTag("contracts", list);
            data.setContractsNBT(nbt);
            data.markDirty();
         }
      }
   }
}
