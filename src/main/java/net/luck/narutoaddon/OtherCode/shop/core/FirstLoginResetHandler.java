
package net.luck.narutoaddon.OtherCode.shop.core;

import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiMember;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiSavedData;
import net.luck.narutoaddon.OtherCode.quest.core.QuestSavedData;
import net.luck.narutoaddon.OtherCode.quest.network.QuestNetworkHelper;
import net.luck.narutoaddon.OtherCode.stat.core.StatSavedData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.*;

public class FirstLoginResetHandler {
   private static final String SEASONAL_ITEM_ID = "inftsukaddon:seasonal_release";
   private static final Map<String, Integer> CRYSTAL_GRANTS = new HashMap();

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
      if (event.player != null && !event.player.world.isRemote) {
         if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            UUID playerId = player.getUniqueID();
            ShopSavedData shopData = ShopSavedData.get(player.world);
            if (shopData != null && !shopData.hasResetBeenApplied(playerId)) {
               System.out.println("[Reset] Starting first-login reset for " + player.getName());

               try {
                  int oldTokens = shopData.getTokens(playerId);
                  shopData.setTokens(playerId, 0);
                  System.out.println("[Reset] Cleared shop tokens for " + player.getName() + " (" + oldTokens + " -> 0)");
               } catch (Exception e) {
                  System.out.println("[Reset] WARNING: Failed to reset shop tokens for " + player.getName() + ": " + e.getMessage());
               }

               try {
                  shopData.clearOwnedItems(playerId);
                  System.out.println("[Reset] Cleared dupe tracking for " + player.getName());
               } catch (Exception e) {
                  System.out.println("[Reset] WARNING: Failed to clear dupe tracking for " + player.getName() + ": " + e.getMessage());
               }

               try {
                  StatSavedData statData = StatSavedData.get(player.world);
                  if (statData != null) {
                     statData.resetPlayerStats(playerId);
                  }
               } catch (Exception e) {
                  System.out.println("[Reset] WARNING: Failed to reset stats for " + player.getName() + ": " + e.getMessage());
               }

               try {
                  AkatsukiSavedData akatsukiData = AkatsukiSavedData.get(player.world);
                  if (akatsukiData != null) {
                     AkatsukiMember member = akatsukiData.getMember(playerId);
                     if (member != null) {
                        int[] levels = member.getRingUpgradeLevels();
                        if (levels != null) {
                           for(int i = 0; i < levels.length; ++i) {
                              levels[i] = 0;
                           }
                        }

                        member.setReputation(0);
                        member.addBountyTokens(-member.getBountyTokens());
                        akatsukiData.markDirty();
                     }
                  }
               } catch (Exception e) {
                  System.out.println("[Reset] WARNING: Failed to reset Akatsuki for " + player.getName() + ": " + e.getMessage());
               }

               try {
                  List<ItemStack> preserved = new ArrayList();

                  for(int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                     ItemStack stack = player.inventory.getStackInSlot(i);
                     if (!stack.isEmpty() && isSeasonalItem(stack)) {
                        preserved.add(stack.copy());
                     }
                  }

                  player.inventory.clear();
                  IInventory enderChest = player.getInventoryEnderChest();

                  for(int i = 0; i < enderChest.getSizeInventory(); ++i) {
                     ItemStack stack = enderChest.getStackInSlot(i);
                     if (!stack.isEmpty() && isSeasonalItem(stack)) {
                        preserved.add(stack.copy());
                     }

                     enderChest.setInventorySlotContents(i, ItemStack.EMPTY);
                  }

                  for(ItemStack item : preserved) {
                     if (!player.inventory.addItemStackToInventory(item)) {
                        player.dropItem(item, false);
                     }
                  }

                  MinecraftServer server = player.getServer();
                  if (server != null) {
                     try {
                        server.getCommandManager().executeCommand(server, "/bp clear " + player.getName());
                     } catch (Exception e) {
                        System.out.println("[Reset] Could not clear /bp for " + player.getName() + ": " + e.getMessage());
                     }

                     try {
                        server.getCommandManager().executeCommand(server, "/vaultadmin wipe " + player.getName());
                     } catch (Exception e) {
                        System.out.println("[Reset] Could not clear /vault for " + player.getName() + ": " + e.getMessage());
                     }
                  }

                  System.out.println("[Reset] Cleared inventory/EC for " + player.getName() + " (preserved " + preserved.size() + " seasonal items)");
               } catch (Exception e) {
                  System.out.println("[Reset] WARNING: Failed to clear inventory for " + player.getName() + ": " + e.getMessage());
               }

               try {
                  QuestSavedData questData = QuestSavedData.get(player.world);
                  if (questData != null) {
                     questData.removeFlag(playerId, "beginner_v2_complete");
                     questData.removeFlag(playerId, "beginner_v2_stage1_done");
                     questData.removeFlag(playerId, "kg_received");
                     System.out.println("[Reset] Cleared beginner + KG flags for " + player.getName());
                  }
               } catch (Exception e) {
                  System.out.println("[Reset] WARNING: Failed to clear beginner flags for " + player.getName() + ": " + e.getMessage());
               }

               try {
                  Scoreboard scoreboard = player.getServerWorld().getScoreboard();
                  ScorePlayerTeam team = scoreboard.getPlayersTeam(player.getName());
                  if (team != null) {
                     String teamName = team.getName();
                     if ("Leaf".equals(teamName) || "Sand".equals(teamName) || "Mist".equals(teamName) || "Cloud".equals(teamName) || "Stone".equals(teamName) || "Rain".equals(teamName)) {
                        scoreboard.removePlayerFromTeam(player.getName(), team);
                        System.out.println("[Reset] Removed " + player.getName() + " from village team " + teamName);
                     }
                  }
               } catch (Exception e) {
                  System.out.println("[Reset] WARNING: Failed to remove village team for " + player.getName() + ": " + e.getMessage());
               }

               try {
                  MinecraftServer server = player.getServer();
                  if (server != null) {
                     String playerName = player.getName();
                     String[] villages = new String[]{"leaf", "sand", "mist", "cloud", "stone", "rain"};

                     for(String village : villages) {
                        server.getCommandManager().executeCommand(server, "lp user " + playerName + " parent remove " + village);
                     }

                     server.getCommandManager().executeCommand(server, "lp user " + playerName + " parent remove StoryShinobi");
                     System.out.println("[Reset] Removed LP village parents + StoryShinobi for " + playerName);
                  }
               } catch (Exception e) {
                  System.out.println("[Reset] WARNING: Failed to remove LP groups for " + player.getName() + ": " + e.getMessage());
               }

               int crystalGrant = 0;

               try {
                  String playerNameLower = player.getName().toLowerCase();
                  if (CRYSTAL_GRANTS.containsKey(playerNameLower)) {
                     crystalGrant = (Integer)CRYSTAL_GRANTS.get(playerNameLower);
                     CrystalManager.getInstance().addCrystals(playerId, crystalGrant, player.world, "[SeasonReset]");
                     System.out.println("[Reset] Granted " + crystalGrant + " crystal refund to " + player.getName());
                  }
               } catch (Exception e) {
                  System.out.println("[Reset] WARNING: Failed to grant crystal refund for " + player.getName() + ": " + e.getMessage());
               }

               shopData.markResetApplied(playerId);

               try {
                  QuestNetworkHelper.sendQuestSync(player);
               } catch (Exception e) {
                  System.out.println("[Reset] WARNING: Failed to re-sync quest data for " + player.getName() + ": " + e.getMessage());
               }

               player.sendMessage(new TextComponentString("§6§l[Season Reset] §eYour inventory, tokens, stats, and Akatsuki progression have been reset for the new season."));
               player.sendMessage(new TextComponentString("§6§l[Season Reset] §eYour Ryo, Chakra Crystals, and Seasonal Release item have been preserved."));
               if (crystalGrant > 0) {
                  player.sendMessage(new TextComponentString("§b§l[Season Reset] §eYou have been refunded §b" + crystalGrant + " Chakra Crystals§e for your recent purchases."));
               }

               System.out.println("[Reset] Completed first-login reset for " + player.getName());
            }
         }
      }
   }

   private static boolean isSeasonalItem(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         ResourceLocation regName = stack.getItem().getRegistryName();
         return regName == null ? false : regName.toString().equals("inftsukaddon:seasonal_release");
      }
   }

   static {
      CRYSTAL_GRANTS.put("chronicwheeze", 9320);
      CRYSTAL_GRANTS.put("beansssssssre", 7300);
      CRYSTAL_GRANTS.put("mynamavet", 6790);
      CRYSTAL_GRANTS.put("svcaras", 3550);
      CRYSTAL_GRANTS.put("asilic", 6740);
      CRYSTAL_GRANTS.put("th4popler", 6800);
      CRYSTAL_GRANTS.put("sargentzero", 6275);
      CRYSTAL_GRANTS.put("d3athang3l69", 6300);
      CRYSTAL_GRANTS.put("juantonwoo", 6600);
      CRYSTAL_GRANTS.put("ifnjames2", 6800);
      CRYSTAL_GRANTS.put("alatino67", 6800);
      CRYSTAL_GRANTS.put("majed015", 6800);
      CRYSTAL_GRANTS.put("wolfzz_tv", 5272);
      CRYSTAL_GRANTS.put("ak_gaming0", 5390);
      CRYSTAL_GRANTS.put("untraceddeath", 4780);
      CRYSTAL_GRANTS.put("mrbedfords", 4610);
      CRYSTAL_GRANTS.put("rozkat6969", 4400);
      CRYSTAL_GRANTS.put("eljefe2534", 4400);
      CRYSTAL_GRANTS.put("kaioj3", 4400);
      CRYSTAL_GRANTS.put("watertitan", 4400);
      CRYSTAL_GRANTS.put("dj_nice7", 3120);
      CRYSTAL_GRANTS.put("thereb3l", 2720);
      CRYSTAL_GRANTS.put("robberto10", 2515);
      CRYSTAL_GRANTS.put("manjirookkotsu", 1300);
      CRYSTAL_GRANTS.put("bigglizzz", 2280);
      CRYSTAL_GRANTS.put("heclightning21", 2170);
      CRYSTAL_GRANTS.put("moe__", 2170);
      CRYSTAL_GRANTS.put("spoon1311", 2105);
      CRYSTAL_GRANTS.put("the_anbujamessd", 2075);
      CRYSTAL_GRANTS.put("uhlukk", 2160);
      CRYSTAL_GRANTS.put("xxtablespoonxx", 2160);
      CRYSTAL_GRANTS.put("ctmonster", 2050);
      CRYSTAL_GRANTS.put("nottusk", 2160);
      CRYSTAL_GRANTS.put("prof_rastaban", 1980);
      CRYSTAL_GRANTS.put("readthegod1", 2160);
      CRYSTAL_GRANTS.put("fsgeclipse", 2060);
      CRYSTAL_GRANTS.put("reble_6", 2045);
      CRYSTAL_GRANTS.put("munyunmaker", 1975);
      CRYSTAL_GRANTS.put("roguechaotic", 1440);
      CRYSTAL_GRANTS.put("reltiah", 1460);
      CRYSTAL_GRANTS.put("jjoker51", 1085);
      CRYSTAL_GRANTS.put("mrdinglelong", 1090);
      CRYSTAL_GRANTS.put("amrathelion", 1040);
      CRYSTAL_GRANTS.put("rantingz", 995);
      CRYSTAL_GRANTS.put("wal15ter", 965);
      CRYSTAL_GRANTS.put("wally124", 1000);
      CRYSTAL_GRANTS.put("p3pe9292", 420);
      CRYSTAL_GRANTS.put("noxey", 1060);
      CRYSTAL_GRANTS.put("kjdragon17", 1060);
      CRYSTAL_GRANTS.put("tvojemother", 1050);
      CRYSTAL_GRANTS.put("xxceroxx_93", 1005);
      CRYSTAL_GRANTS.put("osmodiumd", 710);
      CRYSTAL_GRANTS.put("smashingpenguins", 650);
      CRYSTAL_GRANTS.put("mizunauchiha", 735);
      CRYSTAL_GRANTS.put("twonights", 680);
      CRYSTAL_GRANTS.put("t012331", 98);
      CRYSTAL_GRANTS.put("sebastianph", 635);
      CRYSTAL_GRANTS.put("ohsomariii", 570);
      CRYSTAL_GRANTS.put("kairouchiha", 545);
      CRYSTAL_GRANTS.put("gynxll", 540);
      CRYSTAL_GRANTS.put("d4rkd3mon5152", 480);
      CRYSTAL_GRANTS.put("hemes3megistus33", 460);
      CRYSTAL_GRANTS.put("csutt2448", 140);
      CRYSTAL_GRANTS.put("owendragonfriend", 515);
      CRYSTAL_GRANTS.put("hanzossalamander", 385);
      CRYSTAL_GRANTS.put("dextersixx", 485);
      CRYSTAL_GRANTS.put("qnooooo", 480);
      CRYSTAL_GRANTS.put("eldritchentity7", 480);
      CRYSTAL_GRANTS.put("crystalcandy", 500);
      CRYSTAL_GRANTS.put("nightninjathegod", 380);
      CRYSTAL_GRANTS.put("darthrelyks", 400);
      CRYSTAL_GRANTS.put("ryfferx", 355);
      CRYSTAL_GRANTS.put("xrnerus", 350);
      CRYSTAL_GRANTS.put("zaestepin", 285);
      CRYSTAL_GRANTS.put("yanq", 300);
      CRYSTAL_GRANTS.put("twdseminole", 230);
      CRYSTAL_GRANTS.put("eps1lon1", 200);
      CRYSTAL_GRANTS.put("pipicalda", 200);
      CRYSTAL_GRANTS.put("snorlaxintub", 105);
      CRYSTAL_GRANTS.put("protocolypse", 100);
      CRYSTAL_GRANTS.put("jetty_5", 60);
      CRYSTAL_GRANTS.put("halocrusher9921", 15);
   }
}
