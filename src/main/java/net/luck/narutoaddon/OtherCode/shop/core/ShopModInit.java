
package net.luck.narutoaddon.OtherCode.shop.core;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.shop.building.BuildingPurchaseMessage;
import net.luck.narutoaddon.OtherCode.shop.building.BuildingShopRegistry;
import net.luck.narutoaddon.OtherCode.shop.building.CraftingRestrictionHandler;
import net.luck.narutoaddon.OtherCode.shop.command.CommandCrystalAdmin;
import net.luck.narutoaddon.OtherCode.shop.command.CommandShopAdmin;
import net.luck.narutoaddon.OtherCode.shop.crate.CrateRegistry;
import net.luck.narutoaddon.OtherCode.shop.network.*;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassDefinition;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassManager;
import net.luck.narutoaddon.OtherCode.shop.pass.BattlePassSeason;
import net.luck.narutoaddon.OtherCode.shop.pass.KillEffectHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

@ElementsInfTsukAddon.ModElement.Tag
public class ShopModInit extends ElementsInfTsukAddon.ModElement {
   public static final String NETWORK_CHANNEL = "inftsuk_shop";
   public static SimpleNetworkWrapper NETWORK;
   private static int packetId = 0;

   public ShopModInit(ElementsInfTsukAddon instance) {
      super(instance, 9003);
   }

   public void preInit(FMLPreInitializationEvent event) {
      NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("inftsuk_shop");
      NETWORK.registerMessage(ShopSyncMessage.Handler.class, ShopSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(ShopCrateResultMessage.Handler.class, ShopCrateResultMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(ShopHistorySyncMessage.Handler.class, ShopHistorySyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(ShopAdminSyncMessage.Handler.class, ShopAdminSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(ShopActionMessage.Handler.class, ShopActionMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(ShopAdminRestoreMessage.Handler.class, ShopAdminRestoreMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(BattlePassSyncMessage.Handler.class, BattlePassSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(BattlePassActionMessage.Handler.class, BattlePassActionMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(BuildingPurchaseMessage.Handler.class, BuildingPurchaseMessage.class, packetId++, Side.SERVER);
      MinecraftForge.EVENT_BUS.register(new ShopEventHandler());
      MinecraftForge.EVENT_BUS.register(new DailyLoginHandler());
      MinecraftForge.EVENT_BUS.register(new CraftingRestrictionHandler());
      MinecraftForge.EVENT_BUS.register(new FirstLoginResetHandler());
   }

   public void serverLoad(FMLServerStartingEvent event) {
      MinecraftServer server = event.getServer();
      if (server != null) {
         World world = server.getWorld(0);
         if (world != null) {
            ShopSavedData.get(world);
            PlayerItemTracker.get(world);
            CrateRegistry.init();
            BuildingShopRegistry.init();
            System.out.println("[RyoShop] Shop system initialized.");
            System.out.println("[RyoShop] Building shop registry initialized.");
            System.out.println("[RyoShop] Item tracker initialized.");
            ShopSavedData bootData = ShopSavedData.get(world);
            if (bootData.getBpSeasonNumber() < 3 && !bootData.hasOwnedItem(new UUID(0L, 0L), "bp_s3_force_started")) {
               int oldSeason = bootData.getBpSeasonNumber();
               BattlePassManager mgr = BattlePassManager.getInstance();
               mgr.queueAutoClaimForReset(world);
               bootData.setBpSeasonNumber(3);
               bootData.setBpSeasonStartTime(System.currentTimeMillis());
               bootData.resetAllBattlePassData();
               bootData.addOwnedItem(new UUID(0L, 0L), "bp_s3_force_started");
               bootData.markDirty();
               System.out.println("[BattlePass] S3 auto-start: transitioned from season " + oldSeason + " to 3 (Veil of Shadow). Unclaimed rewards queued for auto-claim.");
            }

            if (bootData.getBpSeasonNumber() == 3 && !bootData.hasOwnedItem(new UUID(0L, 0L), "bp_s3_duration_fixed")) {
               long oldStart = bootData.getBpSeasonStartTime();
               long now = System.currentTimeMillis();
               bootData.setBpSeasonStartTime(now);
               bootData.addOwnedItem(new UUID(0L, 0L), "bp_s3_duration_fixed");
               bootData.markDirty();
               System.out.println("[BattlePass] S3 duration fix: start time moved from " + oldStart + " to " + now + " — 14 full days from now.");
            }
         }

         event.registerServerCommand(new CommandShopAdmin());
         event.registerServerCommand(new CommandCrystalAdmin());
         MinecraftForge.EVENT_BUS.register(new KillEffectHandler());
      }

   }

   public static class ShopEventHandler {
      private static int tickCounter = 0;

      @SubscribeEvent
      public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            ShopActionMessage.Handler.grantAllPending((EntityPlayerMP)event.player);
            EntityPlayerMP loginPlayer = (EntityPlayerMP)event.player;
            ShopSavedData loginShopData = ShopSavedData.get(loginPlayer.getServerWorld());
            if (loginShopData.hasBpPendingAutoClaim(loginPlayer.getUniqueID())) {
               BattlePassManager.getInstance().processPendingAutoClaim(loginPlayer.getUniqueID(), loginPlayer.getServerWorld());
            }

            if (loginShopData.getBpSeasonNumber() == 2 && loginShopData.hasBpClaimedTier(loginPlayer.getUniqueID(), 20) && !loginShopData.hasOwnedItem(loginPlayer.getUniqueID(), "bp_kirin_eligible")) {
               loginShopData.addOwnedItem(loginPlayer.getUniqueID(), "bp_kirin_eligible");
               System.out.println("[BattlePass] Retroactive Kirin eligibility for " + loginPlayer.getName() + " (claimed S2 tier 20 before marker existed)");
            }

            if (loginShopData.hasOwnedItem(loginPlayer.getUniqueID(), "bp_kirin_eligible")) {
               int kirinUnlocked = BattlePassManager.unlockKirinOnAllItems(loginPlayer);
               if (kirinUnlocked > 0 && !loginShopData.hasOwnedItem(loginPlayer.getUniqueID(), "bp_kirin_unlock_notified")) {
                  loginPlayer.sendMessage(new TextComponentString("§6[Battle Pass] §eLightning Release: Kirin has been unlocked on your Seasonal Release item!"));
                  loginShopData.addOwnedItem(loginPlayer.getUniqueID(), "bp_kirin_unlock_notified");
                  System.out.println("[BattlePass] Kirin unlocked on " + kirinUnlocked + " seasonal_release item(s) for " + loginPlayer.getName());
               }
            }

            if (loginShopData.getBpSeasonNumber() == 3 && loginShopData.hasBpClaimedTier(loginPlayer.getUniqueID(), 20) && !loginShopData.hasOwnedItem(loginPlayer.getUniqueID(), "bp_season_3_jutsu_eligible")) {
               loginShopData.addOwnedItem(loginPlayer.getUniqueID(), "bp_season_3_jutsu_eligible");
               System.out.println("[BattlePass] Retroactive Shadow eligibility for " + loginPlayer.getName() + " (claimed S3 tier 20 before marker existed)");
            }

            if (loginShopData.hasOwnedItem(loginPlayer.getUniqueID(), "bp_season_3_jutsu_eligible")) {
               int shadowUnlocked = BattlePassManager.unlockShadowRendOnAllItems(loginPlayer);
               if (shadowUnlocked > 0 && !loginShopData.hasOwnedItem(loginPlayer.getUniqueID(), "bp_shadow_rend_unlock_notified")) {
                  loginPlayer.sendMessage(new TextComponentString("§6[Battle Pass] §eShadow Release: Shadow Rend has been unlocked on your Seasonal Release item!"));
                  loginShopData.addOwnedItem(loginPlayer.getUniqueID(), "bp_shadow_rend_unlock_notified");
                  System.out.println("[BattlePass] Shadow Rend unlocked on " + shadowUnlocked + " seasonal_release item(s) for " + loginPlayer.getName());
               }
            }

            if (loginShopData.isWeekendPromoSO6PEnabled() && loginShopData.hasUsedFreeRoll(loginPlayer.getUniqueID()) && !loginShopData.hasOwnedItem(loginPlayer.getUniqueID(), "weekend_promo_so6p_claimed") && !loginShopData.hasOwnedItem(loginPlayer.getUniqueID(), "weekend_promo_so6p_available")) {
               loginShopData.addOwnedItem(loginPlayer.getUniqueID(), "weekend_promo_so6p_available");
               loginPlayer.sendMessage(new TextComponentString("§6§l[Weekend Promo] §eEnjoy a free Premium SO6P crate roll! Use §f/crystal §eto open."));
            }

            ShopNetworkHelper.sendShopSync((EntityPlayerMP)event.player);
            ShopNetworkHelper.sendBattlePassSync((EntityPlayerMP)event.player);
            EntityPlayerMP mp = (EntityPlayerMP)event.player;
            PlayerItemTracker tracker = PlayerItemTracker.get(mp.getServerWorld());
            NarutoItemScanner.scanAndUpdateTracker(mp, tracker);
            ShopSavedData shopData = ShopSavedData.get(mp.getServerWorld());
            String shiragoneKey = "narutomod:scroll_puppet:0";
            String migrationKey = "shiragone_puppet_migration_done";
            if (shopData.hasOwnedItem(mp.getUniqueID(), shiragoneKey) && !shopData.hasOwnedItem(mp.getUniqueID(), migrationKey)) {
               String[] newScrolls = new String[]{"clansaddon:scroll_puppet_technique", "clansaddon:scroll_karasu", "clansaddon:scroll_hiruko", "clansaddon:scroll_sanshouo", "clansaddon:scroll_3rd_kazekage", "clansaddon:scroll_hundred_puppets"};

               for(String scrollId : newScrolls) {
                  Item item = Item.getByNameOrId(scrollId);
                  if (item != null) {
                     ItemStack stack = new ItemStack(item, 1, 0);
                     if (!mp.inventory.addItemStackToInventory(stack)) {
                        NBTTagCompound overflowNbt = new NBTTagCompound();
                        overflowNbt.setString("itemId", scrollId);
                        overflowNbt.setInteger("meta", 0);
                        overflowNbt.setInteger("count", 1);
                        shopData.addOverflowItem(mp.getUniqueID(), overflowNbt);
                     }
                  }
               }

               shopData.addOwnedItem(mp.getUniqueID(), migrationKey);
               mp.sendMessage(new TextComponentString("§d[Shinobi Shop] §7Your Shiragone Clan has been upgraded! New puppet summon scrolls added to your inventory."));
            }

            String sarutobiMigrationKey = "sarutobi_upgrade_v2";
            boolean ownsSarutobi = shopData.hasOwnedItem(mp.getUniqueID(), "narutomod:chakra_blades:0");
            if (ownsSarutobi && !shopData.hasOwnedItem(mp.getUniqueID(), sarutobiMigrationKey)) {
               NBTTagCompound rightNbt = new NBTTagCompound();
               NBTTagList rEnch = new NBTTagList();
               NBTTagCompound rE = new NBTTagCompound();
               rE.setShort("id", (short)16);
               rE.setShort("lvl", (short)100);
               rEnch.appendTag(rE);
               rightNbt.setTag("ench", rEnch);
               rightNbt.setBoolean("Unbreakable", true);
               NBTTagCompound rDisp = new NBTTagCompound();
               rDisp.setString("Name", "§lAsuma's Chakra Blades [Right]");
               rightNbt.setTag("display", rDisp);
               NBTTagCompound leftNbt = new NBTTagCompound();
               NBTTagList lEnch = new NBTTagList();
               NBTTagCompound lE = new NBTTagCompound();
               lE.setShort("id", (short)16);
               lE.setShort("lvl", (short)100);
               lEnch.appendTag(lE);
               leftNbt.setTag("ench", lEnch);
               leftNbt.setBoolean("Unbreakable", true);
               NBTTagCompound lDisp = new NBTTagCompound();
               lDisp.setString("Name", "§lAsuma's Chakra Blades [Left]");
               leftNbt.setTag("display", lDisp);
               Item bladeItem = Item.getByNameOrId("narutomod:chakra_blades");
               if (bladeItem != null) {
                  ItemStack rightStack = new ItemStack(bladeItem, 1, 0);
                  rightStack.setTagCompound(rightNbt);
                  if (!mp.inventory.addItemStackToInventory(rightStack)) {
                     NBTTagCompound ov = new NBTTagCompound();
                     ov.setString("itemId", "narutomod:chakra_blades");
                     ov.setInteger("meta", 0);
                     ov.setInteger("count", 1);
                     ov.setTag("itemNbt", rightNbt);
                     shopData.addOverflowItem(mp.getUniqueID(), ov);
                  }

                  ItemStack leftStack = new ItemStack(bladeItem, 1, 0);
                  leftStack.setTagCompound(leftNbt);
                  if (!mp.inventory.addItemStackToInventory(leftStack)) {
                     NBTTagCompound ov = new NBTTagCompound();
                     ov.setString("itemId", "narutomod:chakra_blades");
                     ov.setInteger("meta", 0);
                     ov.setInteger("count", 1);
                     ov.setTag("itemNbt", leftNbt);
                     shopData.addOverflowItem(mp.getUniqueID(), ov);
                  }
               }

               Item flowScroll = Item.getByNameOrId("narutomod:scroll_futon_chakra_flow");
               if (flowScroll != null) {
                  ItemStack flowStack = new ItemStack(flowScroll, 1, 0);
                  if (!mp.inventory.addItemStackToInventory(flowStack)) {
                     NBTTagCompound ov = new NBTTagCompound();
                     ov.setString("itemId", "narutomod:scroll_futon_chakra_flow");
                     ov.setInteger("meta", 0);
                     ov.setInteger("count", 1);
                     shopData.addOverflowItem(mp.getUniqueID(), ov);
                  }
               }

               shopData.addOwnedItem(mp.getUniqueID(), sarutobiMigrationKey);
               mp.sendMessage(new TextComponentString("§d[Shinobi Shop] §7Your Sarutobi Clan has been upgraded! Enchanted Chakra Blades + Chakra Flow scroll added."));
               System.out.println("[SarutobiMigration] Granted blades + chakra flow to " + mp.getName());
            }

            int bpLevel = shopData.getBpCurrentLevel(mp.getUniqueID());
            int bpPurchased = shopData.getBpPurchasedTier(mp.getUniqueID());
            if (bpLevel > 0 && bpPurchased > 0) {
               UUID pid = mp.getUniqueID();
               boolean hadOldMigration = shopData.hasOwnedItem(pid, "bp_reward_fix_v1") || shopData.hasOwnedItem(pid, "bp_reward_fix_v2") || shopData.hasOwnedItem(pid, "bp_reward_fix_v3") || shopData.hasOwnedItem(pid, "bp_reward_fix_v4") || shopData.hasOwnedItem(pid, "bp_tier_1_granted");
               if (hadOldMigration) {
                  for(int t2 = 1; t2 <= bpLevel; ++t2) {
                     if (shopData.hasBpClaimedTier(pid, t2)) {
                        shopData.addOwnedItem(pid, "bp_tier_" + t2 + "_crate_granted");
                     }
                  }
               }

               BattlePassManager bpMgr = BattlePassManager.getInstance();
               int bpSeasonNum = shopData.getBpSeasonNumber();

               for(int t = 1; t <= bpLevel; ++t) {
                  if (shopData.hasBpClaimedTier(pid, t)) {
                     String crateKey = "bp_tier_" + t + "_crate_granted";
                     if (!shopData.hasOwnedItem(pid, crateKey)) {
                        BattlePassDefinition.TierReward freeR = BattlePassDefinition.getFreeTierReward(t);
                        if (freeR != null && freeR.hasCrate()) {
                           BattlePassDefinition.TierReward crateOnly = new BattlePassDefinition.TierReward(0, 0, 0, freeR.crateId, freeR.crateCount, freeR.crateId2, freeR.crateCount2, false, (String)null);
                           bpMgr.grantRewardPublic(pid, crateOnly, mp.getServerWorld());
                        }

                        BattlePassDefinition.TierReward premR = BattlePassDefinition.getPremiumTierReward(t, bpSeasonNum);
                        if (premR != null && (premR.hasCrate() || premR.hasCrate2())) {
                           BattlePassDefinition.TierReward premCrateOnly = new BattlePassDefinition.TierReward(0, 0, 0, premR.crateId, premR.crateCount, premR.crateId2, premR.crateCount2, false, (String)null);
                           bpMgr.grantRewardPublic(pid, premCrateOnly, mp.getServerWorld());
                        }

                        shopData.addOwnedItem(pid, crateKey);
                     }

                     String xpKey = "bp_tier_" + t + "_xp_granted";
                     if (!shopData.hasOwnedItem(pid, xpKey)) {
                        BattlePassDefinition.TierReward premR = BattlePassDefinition.getPremiumTierReward(t, bpSeasonNum);
                        if (premR != null && premR.jutsuXP > 0) {
                           MinecraftServer srv = mp.getServerWorld().getMinecraftServer();
                           if (srv != null) {
                              srv.getCommandManager().executeCommand(srv, "runasop " + mp.getName() + " addninjaxp " + mp.getName() + " " + premR.jutsuXP);
                           }
                        }

                        shopData.addOwnedItem(pid, xpKey);
                     }

                     String itemKey = "bp_tier_" + t + "_item_granted";
                     if (!shopData.hasOwnedItem(pid, itemKey)) {
                        BattlePassDefinition.TierReward freeR = BattlePassDefinition.getFreeTierReward(t);
                        if (freeR != null && freeR.hasItem()) {
                           Item pillItem = Item.getByNameOrId(freeR.itemId);
                           if (pillItem != null) {
                              ItemStack pillStack = new ItemStack(pillItem, freeR.itemCount, freeR.itemMeta);
                              if (!mp.inventory.addItemStackToInventory(pillStack)) {
                                 NBTTagCompound ov = new NBTTagCompound();
                                 ov.setString("itemId", freeR.itemId);
                                 ov.setInteger("meta", freeR.itemMeta);
                                 ov.setInteger("count", freeR.itemCount);
                                 shopData.addOverflowItem(pid, ov);
                              }

                              System.out.println("[BattlePass Migration] Granted " + freeR.itemCount + "x " + freeR.itemId + " to " + mp.getName() + " (tier " + t + ")");
                           }
                        }

                        shopData.addOwnedItem(pid, itemKey);
                     }

                     if (t == 15 && !shopData.hasOwnedItem(pid, "bp_tier_15_effect_granted")) {
                        BattlePassSeason season = bpMgr.getCurrentSeason(mp.getServerWorld());
                        shopData.addOwnedKillEffect(pid, season.getKillEffectId());
                        shopData.addOwnedItem(pid, "bp_tier_15_effect_granted");
                     }

                     if (t == 20 && !shopData.hasOwnedItem(pid, "bp_tier_20_jutsu_granted")) {
                        BattlePassSeason season = bpMgr.getCurrentSeason(mp.getServerWorld());
                        String jutsuId = season.getExclusiveJutsuItemId();
                        Item jutsuItem = Item.getByNameOrId(jutsuId);
                        if (jutsuItem != null) {
                           ItemStack stack = new ItemStack(jutsuItem, 1, 0);
                           if (!mp.inventory.addItemStackToInventory(stack)) {
                              NBTTagCompound ov = new NBTTagCompound();
                              ov.setString("itemId", jutsuId);
                              ov.setInteger("meta", 0);
                              ov.setInteger("count", 1);
                              shopData.addOverflowItem(pid, ov);
                           }
                        }

                        shopData.addOwnedItem(pid, "bp_tier_20_jutsu_granted");
                     }
                  }
               }
            }
         }

      }

      @SubscribeEvent
      public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            ShopActionMessage.Handler.grantAllPending((EntityPlayerMP)event.player);
            EntityPlayerMP mp = (EntityPlayerMP)event.player;
            PlayerItemTracker tracker = PlayerItemTracker.get(mp.getServerWorld());
            NarutoItemScanner.scanAndUpdateTracker(mp, tracker);
         }

      }

      @SubscribeEvent
      public void onPlayerDeath(LivingDeathEvent event) {
         if (event.getEntityLiving() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
            PlayerItemTracker tracker = PlayerItemTracker.get(player.getServerWorld());
            NarutoItemScanner.scanAndUpdateTracker(player, tracker);
         }

      }

      @SubscribeEvent
      public void onServerTick(TickEvent.ServerTickEvent event) {
         if (event.phase == Phase.END) {
            ++tickCounter;
            if (tickCounter >= 6000) {
               tickCounter = 0;
               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               if (server != null) {
                  PlayerItemTracker tracker = PlayerItemTracker.get(server.getWorld(0));

                  for(EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                     NarutoItemScanner.scanAndUpdateTracker(player, tracker);
                  }

                  BattlePassManager.getInstance().checkSeasonReset(server.getWorld(0));
               }
            }

         }
      }
   }
}
