
package net.luck.narutoaddon.OtherCode.quest.network;

import io.netty.buffer.ByteBuf;
import net.luck.narutoaddon.OtherCode.quest.core.QuestSavedData;
import net.luck.narutoaddon.OtherCode.shop.core.ShopSavedData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BeginnerActionMessage implements IMessage {
   private byte action;

   public BeginnerActionMessage() {
   }

   public BeginnerActionMessage(byte action) {
      this.action = action;
   }

   public void fromBytes(ByteBuf buf) {
      this.action = buf.readByte();
   }

   public void toBytes(ByteBuf buf) {
      buf.writeByte(this.action);
   }

   public static class Handler implements IMessageHandler<BeginnerActionMessage, IMessage> {
      private static final Set<UUID> processingPlayers = ConcurrentHashMap.newKeySet();

      public IMessage onMessage(BeginnerActionMessage message, MessageContext ctx) {
         EntityPlayerMP player = ctx.getServerHandler().player;
         player.getServerWorld().addScheduledTask(() -> {
            UUID playerId = player.getUniqueID();
            if (processingPlayers.add(playerId)) {
               try {
                  QuestSavedData savedData = QuestSavedData.get(player.world);
                  if (!savedData.hasFlag(playerId, "beginner_v2_complete")) {
                     ShopSavedData shopData = ShopSavedData.get(player.world);
                     boolean seasonReset = shopData != null && shopData.hasResetBeenApplied(playerId);
                     if (!seasonReset) {
                        Scoreboard scoreboard = player.getServerWorld().getScoreboard();
                        ScorePlayerTeam team = scoreboard.getPlayersTeam(player.getName());
                        if (team != null) {
                           String teamName = team.getName();
                           if ("Leaf".equals(teamName) || "Sand".equals(teamName) || "Mist".equals(teamName) || "Cloud".equals(teamName) || "Stone".equals(teamName) || "Rain".equals(teamName)) {
                              savedData.setFlag(playerId, "beginner_v2_complete");
                              savedData.setFlag(playerId, "beginner_v2_stage1_done");
                              return;
                           }
                        }
                     }

                     if (message.action >= 0 && message.action <= 6) {
                        if (message.action != 0) {
                           savedData.setFlag(playerId, "beginner_v2_complete");
                           this.giveStage2Rewards(player, message.action, savedData, playerId);
                           return;
                        }

                        if (!savedData.hasFlag(playerId, "beginner_v2_stage1_done")) {
                           savedData.setFlag(playerId, "beginner_v2_stage1_done");
                           this.giveStage1Rewards(player);
                           return;
                        }

                        return;
                     }

                     return;
                  }
               } finally {
                  processingPlayers.remove(playerId);
               }

            }
         });
         return null;
      }

      private void giveStage1Rewards(EntityPlayerMP player) {
         Item kunai = (Item)Item.REGISTRY.getObject(new ResourceLocation("narutomod", "kunai"));
         if (kunai != null) {
            ItemStack kunaiStack = new ItemStack(kunai, 3);
            NBTTagCompound kunaiNbt = kunaiStack.getTagCompound();
            if (kunaiNbt == null) {
               kunaiNbt = new NBTTagCompound();
            }

            kunaiNbt.setBoolean("Unbreakable", true);
            kunaiStack.setTagCompound(kunaiNbt);
            giveOrDrop(player, kunaiStack);
         } else {
            System.out.println("[BeginnerGui] WARNING: narutomod:kunai not found in registry!");
         }

         Item shuriken = (Item)Item.REGISTRY.getObject(new ResourceLocation("narutomod", "shuriken"));
         if (shuriken != null) {
            giveOrDrop(player, new ItemStack(shuriken, 3));
         } else {
            System.out.println("[BeginnerGui] WARNING: narutomod:shuriken not found in registry!");
         }

         player.addExperienceLevel(10);
      }

      private void giveStage2Rewards(EntityPlayerMP player, int villageIndex, QuestSavedData savedData, UUID playerId) {
         MinecraftServer server = player.getServer();
         String playerName = player.getName();
         String[] headbandIds = new String[]{"kabutoaddon:leaf_headband", "kabutoaddon:sand_headband", "narutomod:ninja_armor_kirihelmet", "kabutoaddon:cloud_headband", "kabutoaddon:stone_headband", "kabutoaddon:rain_headband"};
         String[] headbandNames = new String[]{"§2Official Leaf Headband", "§eOfficial Sand Headband", "§3Official Mist Headband", "§7Official Cloud Headband", "§6Official Stone Headband", "§1Official Rain Headband"};
         String[] lpParents = new String[]{"leaf", "sand", "mist", "cloud", "stone", "rain"};
         String[] teams = new String[]{"Leaf", "Sand", "Mist", "Cloud", "Stone", "Rain"};
         String[] warps = new String[]{"leaf", "sand", "mist", "cloud", "stone", "rain"};
         int vi = villageIndex - 1;
         if (server != null) {
            server.commandManager.executeCommand(server, "rank add " + playerName + " StoryShinobi");
         }

         if (server != null) {
            server.commandManager.executeCommand(server, "runasop " + playerName + " addninjaxp " + playerName + " 5000");
         }

         if (server != null) {
            server.commandManager.executeCommand(server, "lp user " + playerName + " parent set " + lpParents[vi]);
         }

         if (server != null) {
            server.commandManager.executeCommand(server, "scoreboard teams join " + teams[vi] + " " + playerName);
         }

         String[] parts = headbandIds[vi].split(":");
         Item headband = (Item)Item.REGISTRY.getObject(new ResourceLocation(parts[0], parts[1]));
         if (headband != null) {
            ItemStack headbandStack = new ItemStack(headband, 1);
            headbandStack.setStackDisplayName(headbandNames[vi]);
            giveOrDrop(player, headbandStack);
         } else {
            System.out.println("[BeginnerGui] WARNING: " + headbandIds[vi] + " not found in registry!");
         }

         this.giveNatureItems(player);
         Item steak = (Item)Item.REGISTRY.getObject(new ResourceLocation("minecraft", "cooked_beef"));
         if (steak != null) {
            giveOrDrop(player, new ItemStack(steak, 64));
         }

         QuestNetworkHelper.sendQuestSync(player);
         if (server != null) {
            server.commandManager.executeCommand(server, "warp " + warps[vi] + " " + playerName);
         }

      }

      private void giveNatureItems(EntityPlayerMP player) {
         UUID playerUuid = player.getUniqueID();
         Object[][] natures = new Object[][]{{"narutomod:katon", 3, 150, 6}, {"narutomod:suiton", 1, 150, 10}, {"narutomod:raiton", 3, 200, 7}, {"narutomod:futon", 2, 200, 6}, {"narutomod:doton", 6, 150, 7}};

         for(Object[] nature : natures) {
            String itemId = (String)nature[0];
            int targetIndex = (Integer)nature[1];
            int requiredXp = (Integer)nature[2];
            int jutsuCount = (Integer)nature[3];
            String[] parts = itemId.split(":");
            Item item = (Item)Item.REGISTRY.getObject(new ResourceLocation(parts[0], parts[1]));
            if (item == null) {
               System.out.println("[BeginnerGui] WARNING: " + itemId + " not found in registry!");
            } else {
               ItemStack stack = new ItemStack(item, 1);
               NBTTagCompound nbt = stack.getTagCompound();
               if (nbt == null) {
                  nbt = new NBTTagCompound();
               }

               nbt.setUniqueId("OwnerIdKey", playerUuid);
               int[] xpArray = new int[jutsuCount];
               xpArray[targetIndex] = requiredXp;
               nbt.setIntArray("JutsuExperienceMapKey", xpArray);
               nbt.setBoolean("IsNatureAffinityKey", true);
               nbt.setInteger("JutsuIndexKey", targetIndex);

               for(int i = 0; i < jutsuCount; ++i) {
                  nbt.setLong("JutsuCDMapKey" + i, i == targetIndex ? 0L : -1L);
               }

               stack.setTagCompound(nbt);
               giveOrDrop(player, stack);
            }
         }

      }

      private static void giveOrDrop(EntityPlayerMP player, ItemStack stack) {
         if (!player.inventory.addItemStackToInventory(stack)) {
            EntityItem drop = new EntityItem(player.world, player.posX, player.posY + (double)0.5F, player.posZ, stack);
            drop.setNoPickupDelay();
            player.world.spawnEntity(drop);
         }

      }
   }
}
