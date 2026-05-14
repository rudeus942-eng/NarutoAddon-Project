
package net.luck.narutoaddon.OtherCode.quest.core;

import net.luck.narutoaddon.OtherCode.ElementsInfTsukAddon;
import net.luck.narutoaddon.OtherCode.akatsuki.contract.ContractManager;
import net.luck.narutoaddon.OtherCode.akatsuki.core.AkatsukiManager;
import net.luck.narutoaddon.OtherCode.akatsuki.mission.AkatsukiMissionManager;
import net.luck.narutoaddon.OtherCode.akatsuki.raid.VillageRaidManager;
import net.luck.narutoaddon.OtherCode.endgame.bingo.BingoManager;
import net.luck.narutoaddon.OtherCode.endgame.defense.DefenseManager;
import net.luck.narutoaddon.OtherCode.endgame.incursion.IncursionManager;
import net.luck.narutoaddon.OtherCode.endgame.outpost.OutpostManager;
import net.luck.narutoaddon.OtherCode.entity.base.QuestNpcBase;
import net.luck.narutoaddon.OtherCode.jutsu.CommandSeasonJutsu;
import net.luck.narutoaddon.OtherCode.quest.command.CommandQuest;
import net.luck.narutoaddon.OtherCode.quest.command.CommandQuestAdmin;
import net.luck.narutoaddon.OtherCode.quest.gui.QuestCombatOverlay;
import net.luck.narutoaddon.OtherCode.quest.gui.QuestTrackerOverlay;
import net.luck.narutoaddon.OtherCode.quest.network.*;
import net.luck.narutoaddon.OtherCode.quest.npc.NpcConfigRegistry;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.UUID;

@ElementsInfTsukAddon.ModElement.Tag
public class QuestModInit extends ElementsInfTsukAddon.ModElement {
   public static final String NETWORK_CHANNEL = "inftsuk_quest";
   public static SimpleNetworkWrapper NETWORK;
   private static int packetId = 0;

   public QuestModInit(ElementsInfTsukAddon instance) {
      super(instance, 9001);
   }

   public void preInit(FMLPreInitializationEvent event) {
      NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("inftsuk_quest");
      NETWORK.registerMessage(QuestSyncMessage.Handler.class, QuestSyncMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(QuestWaypointMessage.Handler.class, QuestWaypointMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(QuestDialogMessage.Handler.class, QuestDialogMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(QuestCombatHealthMessage.Handler.class, QuestCombatHealthMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(QuestActionMessage.Handler.class, QuestActionMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(KGRollResultMessage.Handler.class, KGRollResultMessage.class, packetId++, Side.CLIENT);
      NETWORK.registerMessage(KGRollClaimMessage.Handler.class, KGRollClaimMessage.class, packetId++, Side.SERVER);
      NETWORK.registerMessage(BeginnerActionMessage.Handler.class, BeginnerActionMessage.class, packetId++, Side.SERVER);
      MinecraftForge.EVENT_BUS.register(new QuestEventHandler());
      MinecraftForge.EVENT_BUS.register(new NpcBalanceHandler());
      QuestRegistry.init();
      NpcConfigRegistry.init();
      MissionTemplate.init();
   }

   public void init(FMLInitializationEvent event) {
      if (event.getSide() == Side.CLIENT) {
         this.initClient();
      }

   }

   @SideOnly(Side.CLIENT)
   private void initClient() {
      MinecraftForge.EVENT_BUS.register(new QuestTrackerOverlay());
      MinecraftForge.EVENT_BUS.register(new QuestCombatOverlay());
      MinecraftForge.EVENT_BUS.register(new WaypointRenderer());
      MinecraftForge.EVENT_BUS.register(new QuestClientEventHandler());
   }

   public void serverLoad(FMLServerStartingEvent event) {
      event.registerServerCommand(new CommandQuest());
      event.registerServerCommand(new CommandQuestAdmin());
      event.registerServerCommand(new CommandSeasonJutsu());
      QuestManager.reset();
      MinecraftServer server = event.getServer();
      if (server != null) {
         World world = server.getWorld(0);
         if (world != null) {
            QuestSavedData.get(world);
            System.out.println("[QuestSystem] Quest system initialized.");
         }
      }

   }

   public static class QuestEventHandler {
      private World cachedWorld = null;

      @SubscribeEvent
      public void onServerTick(TickEvent.ServerTickEvent event) {
         if (event.phase == Phase.END) {
            if (this.cachedWorld == null) {
               MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
               if (server != null) {
                  this.cachedWorld = server.getWorld(0);
               }
            }

            if (this.cachedWorld != null) {
               QuestManager.getInstance().onServerTick(this.cachedWorld);
            }

         }
      }

      @SubscribeEvent
      public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP)event.player;
            this.fixCorruptedPosition(mp);
            QuestManager.getInstance().onPlayerLogin(mp);
            if (!mp.getEntityData().getBoolean("sarutobiBladeFixed")) {
               this.fixSarutobiBlades(mp);
            }
         }

      }

      private void fixCorruptedPosition(EntityPlayerMP player) {
         double x = player.posX;
         double y = player.posY;
         double z = player.posZ;
         boolean corrupted = false;
         String reason = "";
         if (!Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(z) && !Double.isInfinite(x) && !Double.isInfinite(y) && !Double.isInfinite(z)) {
            if (!(Math.abs(x) > (double)20000.0F) && !(Math.abs(z) > (double)20000.0F)) {
               if (y < (double)-64.0F || y > (double)1000.0F) {
                  corrupted = true;
                  reason = "extreme vertical position (Y=" + (int)y + ")";
               }
            } else {
               corrupted = true;
               reason = "extreme horizontal position (" + (int)x + ", " + (int)z + ")";
            }
         } else {
            corrupted = true;
            reason = "NaN/Infinite coordinates";
         }

         if (corrupted) {
            BlockPos spawn = player.world.getSpawnPoint();
            player.connection.setPlayerLocation((double)spawn.getX() + (double)0.5F, (double)(spawn.getY() + 1), (double)spawn.getZ() + (double)0.5F, 0.0F, 0.0F);
            player.motionX = (double)0.0F;
            player.motionY = (double)0.0F;
            player.motionZ = (double)0.0F;
            player.velocityChanged = true;
            System.out.println("[QuestModInit] IR-82 FIX: Reset " + player.getName() + " to spawn — " + reason);
         }

      }

      private void fixSarutobiBlades(EntityPlayerMP player) {
         ResourceLocation chakraBladesId = new ResourceLocation("narutomod", "chakra_blades");
         Item chakraBladesItem = (Item)Item.REGISTRY.getObject(chakraBladesId);
         if (chakraBladesItem != null) {
            int replacedCount = 0;

            for(int i = 0; i < player.inventory.getSizeInventory() && replacedCount < 2; ++i) {
               ItemStack stack = player.inventory.getStackInSlot(i);
               if (!stack.isEmpty() && stack.getItem() == chakraBladesItem && stack.getCount() == 1) {
                  NBTTagCompound tag = stack.getTagCompound();
                  if (tag != null && !tag.getBoolean("Unbreakable") && !tag.getBoolean("asumaFixed") && tag.hasKey("ench")) {
                     if (tag.hasKey("display")) {
                        NBTTagCompound displayCheck = tag.getCompoundTag("display");
                        if (displayCheck.hasKey("Name")) {
                           continue;
                        }
                     }

                     NBTTagList enchList = tag.getTagList("ench", 10);
                     if (enchList.tagCount() == 1) {
                        NBTTagCompound ench = enchList.getCompoundTagAt(0);
                        if (ench.getShort("id") == 16 && ench.getShort("lvl") == 2) {
                           boolean isRight = replacedCount == 0;
                           NBTTagCompound newTag = new NBTTagCompound();
                           NBTTagList newEnchList = new NBTTagList();
                           NBTTagCompound newEnch = new NBTTagCompound();
                           newEnch.setShort("id", (short)16);
                           newEnch.setShort("lvl", (short)100);
                           newEnchList.appendTag(newEnch);
                           newTag.setTag("ench", newEnchList);
                           newTag.setBoolean("Unbreakable", true);
                           newTag.setBoolean("asumaFixed", true);
                           NBTTagCompound display = new NBTTagCompound();
                           display.setString("Name", "§lAsuma's Chakra Blades [" + (isRight ? "Right" : "Left") + "]");
                           newTag.setTag("display", display);
                           ItemStack newStack = new ItemStack(chakraBladesItem, 1, 0);
                           newStack.setTagCompound(newTag);
                           player.inventory.setInventorySlotContents(i, newStack);
                           ++replacedCount;
                        }
                     }
                  }
               }
            }

            if (replacedCount > 0) {
               player.sendMessage(new TextComponentString("§a§lYour Sarutobi Chakra Blades have been upgraded to Asuma's Chakra Blades!"));
               player.inventoryContainer.detectAndSendChanges();
               player.getEntityData().setBoolean("sarutobiBladeFixed", true);
               System.out.println("[InfTsuk] Fixed " + replacedCount + " Sarutobi blade(s) for " + player.getName());
            }

         }
      }

      @SubscribeEvent
      public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
         if (event.player instanceof EntityPlayerMP) {
            QuestManager.getInstance().onPlayerDisconnect((EntityPlayerMP)event.player);
         }

      }

      @SubscribeEvent
      public void onAdvancementGrant(AdvancementEvent event) {
         if (event.getEntityPlayer() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.getEntityPlayer();
            String advId = event.getAdvancement().getId().toString();
            if (advId.equals("inftsukaddon:becomegenin") || advId.equals("inftsukaddon:landofwaves") || advId.equals("inftsukaddon:chuninexams") || advId.equals("inftsukaddon:shinobi_way")) {
               QuestManager.getInstance().handleAdvancementSkips(player);
               QuestNetworkHelper.sendQuestSync(player);
            }
         }

      }

      @SubscribeEvent
      public void onLivingDamage(LivingDamageEvent event) {
         QuestManager.getInstance().onLivingDamage(event);
      }

      @SubscribeEvent
      public void onEntityDeath(LivingDeathEvent event) {
         Entity deadEntity = event.getEntity();
         if (deadEntity != null && !deadEntity.world.isRemote) {
            if (deadEntity instanceof EntityPlayerMP) {
               QuestManager.getInstance().onPlayerDeath((EntityPlayerMP)deadEntity);
            } else {
               EntityPlayerMP killer = null;
               if (event.getSource() != null) {
                  Entity trueSource = event.getSource().getTrueSource();
                  if (trueSource instanceof EntityPlayerMP) {
                     killer = (EntityPlayerMP)trueSource;
                  }

                  if (killer == null) {
                     Entity immediate = event.getSource().getImmediateSource();
                     if (immediate instanceof EntityPlayerMP) {
                        killer = (EntityPlayerMP)immediate;
                     }
                  }
               }

               NBTTagCompound entityData = deadEntity.getEntityData();
               if (entityData.getBoolean("questEntity") && !entityData.getBoolean("systemCleanup")) {
                  QuestManager.getInstance().onQuestEntityDeath(deadEntity, killer);
               }

               if (killer != null) {
                  Map<String, QuestInstance> activeSlots = QuestManager.getInstance().getActiveQuests(killer.getUniqueID());
                  boolean hasMatchingStep = false;

                  for(QuestInstance quest : activeSlots.values()) {
                     QuestStep step = quest.getCurrentStep();
                     if (step != null && step.type == QuestStep.StepType.COMBAT && step.targetEntityId != null && (step.isKillCount() || step.isDropChance())) {
                        hasMatchingStep = true;
                        break;
                     }
                  }

                  if (hasMatchingStep) {
                     QuestManager.getInstance().onWorldEntityDeath(deadEntity, killer);
                  }
               }

            }
         }
      }

      @SubscribeEvent
      public void onChunkLoad(ChunkEvent.Load event) {
         World world = event.getWorld();
         if (!world.isRemote) {
            QuestManager qm = QuestManager.getInstance();
            if (qm != null) {
               ClassInheritanceMultiMap<Entity>[] entityLists = event.getChunk().getEntityLists();

               for(ClassInheritanceMultiMap<Entity> section : entityLists) {
                  for(Entity entity : section) {
                     if (entity != null && !entity.isDead && entity instanceof QuestNpcBase) {
                        NBTTagCompound entityData = entity.getEntityData();
                        if (entityData.getBoolean("questEntity")) {
                           String ownerStr = entityData.getString("ownerUUID");
                           String questId = entityData.getString("sharedQuestId");
                           if (ownerStr != null && !ownerStr.isEmpty() && questId != null && !questId.isEmpty()) {
                              try {
                                 UUID ownerId = UUID.fromString(ownerStr);
                                 Map<String, QuestInstance> slots = qm.getActiveQuests(ownerId);
                                 boolean questStillActive = false;

                                 for(QuestInstance qi : slots.values()) {
                                    if (qi.getQuestId().equals(questId)) {
                                       questStillActive = true;
                                       break;
                                    }
                                 }

                                 if (!questStillActive) {
                                    entity.setDead();
                                    System.out.println("[QuestChunkCleanup] Removed orphaned quest NPC " + entity.getClass().getSimpleName() + " at " + String.format("%.0f, %.0f, %.0f", entity.posX, entity.posY, entity.posZ) + " (quest=" + questId + ")");
                                 }
                              } catch (IllegalArgumentException var19) {
                              }
                           }
                        }
                     }
                  }
               }

            }
         }
      }

      @SubscribeEvent
      public void onWorldUnload(WorldEvent.Unload event) {
         World world = event.getWorld();
         if (!world.isRemote) {
            if (world.provider.getDimension() == 0) {
               this.cachedWorld = null;
               System.out.println("[InfTsuk] Server stopping — clearing singleton managers");
               QuestManager.reset();

               try {
                  OutpostManager.reset();
               } catch (Exception e) {
                  System.err.println("[InfTsuk] OutpostManager reset failed: " + e.getMessage());
               }

               try {
                  BingoManager.reset();
               } catch (Exception e) {
                  System.err.println("[InfTsuk] BingoManager reset failed: " + e.getMessage());
               }

               try {
                  IncursionManager.reset();
               } catch (Exception e) {
                  System.err.println("[InfTsuk] IncursionManager reset failed: " + e.getMessage());
               }

               try {
                  DefenseManager.reset();
               } catch (Exception e) {
                  System.err.println("[InfTsuk] DefenseManager reset failed: " + e.getMessage());
               }

               try {
                  AkatsukiManager.reset();
               } catch (Exception e) {
                  System.err.println("[InfTsuk] AkatsukiManager reset failed: " + e.getMessage());
               }

               try {
                  ContractManager.reset();
               } catch (Exception e) {
                  System.err.println("[InfTsuk] ContractManager reset failed: " + e.getMessage());
               }

               try {
                  AkatsukiMissionManager.reset();
               } catch (Exception e) {
                  System.err.println("[InfTsuk] AkatsukiMissionManager reset failed: " + e.getMessage());
               }

               try {
                  VillageRaidManager.reset();
               } catch (Exception e) {
                  System.err.println("[InfTsuk] VillageRaidManager reset failed: " + e.getMessage());
               }

            }
         }
      }
   }
}
