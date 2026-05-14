package net.luck.narutoaddon.OtherCode.stat.core;

import net.luck.narutoaddon.OtherCode.quest.core.RyoRewardHelper;
import net.luck.narutoaddon.OtherCode.quest.core.TerrainCache;
import net.luck.narutoaddon.OtherCode.quest.waypoint.WaypointSpawnLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketTitle.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChakraBossSpawner {
   private static final ChakraBossSpawner INSTANCE = new ChakraBossSpawner();
   private static final int SPAWN_INTERVAL_TICKS = 36000;
   private static final String MODID = "narutoaddon";

   // --- COSTANTI MANCANTI AGGIUNTE QUI ---
   private static final String[] TB_ENTITY_IDS = {
           MODID + ":worldbosskokuo",
           MODID + ":worldbosssaiken",
           MODID + ":worldbossgyuki"
   };
   private static final String[] TB_SHORT_NAMES = {"Kokuo", "Saiken", "Gyuki"};
   private static final String[] TB_DISPLAY_NAMES = {"§5§lKokuo - Cinque Code", "§2§lSaiken - Sei Code", "§8§lGyuki - Otto Code"};
   private static final String[][] DUO_CONFIGS = {
           {"world_boss_sasori", "world_boss_deidara"}, // Tipo 0
           null, null, null, // Riservati per TB
           {"wb_hidan", "wb_zabuza"} // Tipo 4
   };
   private static final int[] SPAWN_WEIGHTS = {3, 1, 1, 1, 3};
   // --------------------------------------

   private static final Map<Class<?>, Field> shootingEntityFieldCache = new ConcurrentHashMap<>();
   private static final Field NO_FIELD_MARKER;

   static {
      Field temp = null;
      try {
         temp = ChakraBossSpawner.class.getDeclaredField("tickCounter");
      } catch (Exception ignored) {}
      NO_FIELD_MARKER = temp;
   }

   private int tickCounter = 0;
   private World cachedWorld = null;
   private final List<BossGroup> activeGroups = new ArrayList<>();
   private BlockPos pendingLocation = null;
   private int pendingSpawnType = -1;
   private boolean hasPendingSpawn = false;
   private long pendingSpawnSetAt = 0L;

   private ChakraBossSpawner() {}

   public static ChakraBossSpawner getInstance() { return INSTANCE; }

   public void register() { MinecraftForge.EVENT_BUS.register(this); }

   @SubscribeEvent
   public void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase != Phase.END) return;

      if (this.cachedWorld == null) {
         MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
         if (server != null) this.cachedWorld = server.getWorld(0);
      }

      if (this.cachedWorld != null) {
         this.tickCounter++;

         if (!this.activeGroups.isEmpty() && this.tickCounter % 100 == 0) {
            this.activeGroups.removeIf(group -> {
               group.entityIds.removeIf(id -> {
                  Entity e = this.cachedWorld.getEntityByID(id);
                  return e == null || !e.isEntityAlive();
               });

               if (group.entityIds.isEmpty()) return true;

               long elapsed = this.cachedWorld.getTotalWorldTime() - group.spawnTimeTicks;
               if (elapsed >= 36000L && !this.isAnyPlayerNearGroup(group)) {
                  this.despawnGroup(group);
                  this.broadcastMessage("§6§l[World Boss] §7Il Boss si è ritirato...");
                  return true;
               }
               return false;
            });
         }

         if (this.tickCounter >= SPAWN_INTERVAL_TICKS) {
            this.tickCounter = 0;
            if (!this.hasPendingSpawn) this.pickBossLocation();
         }

         if (this.hasPendingSpawn && this.pendingLocation != null && this.tickCounter % 20 == 0) {
            for (EntityPlayer player : this.cachedWorld.playerEntities) {
               if (player.getDistanceSq(this.pendingLocation) <= 625.0) {
                  this.spawnWorldBosses();
                  break;
               }
            }
         }
      }
   }

   private boolean isAnyPlayerNearGroup(BossGroup group) {
      for (EntityPlayer player : this.cachedWorld.playerEntities) {
         if (player.getDistanceSq(group.location) <= 3600.0) return true;
      }
      return false;
   }

   private void despawnGroup(BossGroup group) {
      for (Integer id : group.entityIds) {
         Entity e = this.cachedWorld.getEntityByID(id);
         if (e != null) e.setDead();
      }
   }

   @SubscribeEvent
   public void onLivingDeath(LivingDeathEvent event) {
      EntityLivingBase entity = event.getEntityLiving();
      if (entity.world.isRemote) return;

      NBTTagCompound data = entity.getEntityData();
      if (data.getBoolean("chakraWorldBoss")) {
         BossGroup owningGroup = null;
         for (BossGroup group : this.activeGroups) {
            if (group.entityIds.contains(entity.getEntityId())) {
               owningGroup = group;
               break;
            }
         }

         int spawnType = data.getInteger("chakraWorldBossDuo");
         if (spawnType >= 1 && spawnType <= 3) {
            this.handleTailedBeastDeath(entity, spawnType, owningGroup);
         } else {
            this.handleDuoBossDeath(entity, spawnType, owningGroup);
         }

         if (owningGroup != null) owningGroup.entityIds.remove((Integer)entity.getEntityId());
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onLivingDamage(LivingDamageEvent event) {
      EntityLivingBase victim = event.getEntityLiving();
      if (victim == null || victim.world.isRemote) return;

      if (victim.getEntityData().getBoolean("chakraWorldBoss")) {
         for (BossGroup group : this.activeGroups) {
            if (group.entityIds.contains(victim.getEntityId())) {
               DamageSource src = event.getSource();
               UUID attackerUuid = this.resolveAttackerOwner(src.getTrueSource(), src.getImmediateSource());

               if (attackerUuid != null && event.getAmount() > 0) {
                  group.damageLedger.merge(attackerUuid, (double)event.getAmount(), Double::sum);
               }
               break;
            }
         }
      }
   }

   private UUID resolveAttackerOwner(Entity trueSource, Entity immediateSource) {
      if (trueSource instanceof EntityPlayerMP) return trueSource.getUniqueID();
      if (trueSource instanceof EntityTameable) {
         EntityLivingBase owner = ((EntityTameable)trueSource).getOwner();
         if (owner instanceof EntityPlayerMP) return owner.getUniqueID();
      }
      if (trueSource instanceof EntityArrow) {
         return resolveShootingEntity(trueSource);
      }
      if (trueSource instanceof EntityThrowable) {
         EntityLivingBase thrower = ((EntityThrowable)trueSource).getThrower();
         if (thrower instanceof EntityPlayerMP) return thrower.getUniqueID();
      }
      return null;
   }

   private UUID resolveShootingEntity(Entity entity) {
      Class<?> clazz = entity.getClass();
      Field cached = shootingEntityFieldCache.get(clazz);
      if (cached == NO_FIELD_MARKER) return null;

      try {
         if (cached == null) {
            for (Class<?> search = clazz; search != null && search != Entity.class; search = search.getSuperclass()) {
               try {
                  Field f = search.getDeclaredField("shootingEntity");
                  f.setAccessible(true);
                  shootingEntityFieldCache.put(clazz, f);
                  cached = f;
                  break;
               } catch (NoSuchFieldException ignored) {}
            }
         }
         if (cached != null) {
            Object shooter = cached.get(entity);
            if (shooter instanceof EntityPlayerMP) return ((EntityPlayerMP) shooter).getUniqueID();
         }
      } catch (Exception e) {
         shootingEntityFieldCache.put(clazz, NO_FIELD_MARKER);
      }
      return null;
   }

   private void handleDuoBossDeath(EntityLivingBase entity, int spawnType, BossGroup group) {
      String bossName = entity.hasCustomName() ? entity.getCustomNameTag() : "Akatsuki Boss";
      int ryoPool = bossName.toLowerCase().contains("zabuza") ? 50000 : 30000;

      this.distributeTieredRyo(entity, group, ryoPool);
      this.announceTopDamage(entity, group, bossName);

      if (entity.world.rand.nextDouble() < 0.04) {
         Item dropItem = Item.getByNameOrId(MODID + ":statbuff");
         if (dropItem != null) {
            this.spawnDropItem(entity, dropItem);
            this.spawnDropParticles(entity);
            this.broadcastMessage("§6§l[World Boss] §b§lSCROLL RARA RILASCIATA!");
         }
      }
   }

   private void handleTailedBeastDeath(EntityLivingBase entity, int spawnType, BossGroup group) {
      int tbIdx = spawnType - 1;
      this.distributeTieredRyo(entity, group, 25000);
      this.announceTopDamage(entity, group, TB_SHORT_NAMES[tbIdx]);

      if (entity.world.rand.nextDouble() < 0.02) {
         Item release = Item.getByNameOrId("clansaddon:imperfect_tb_release");
         if (release != null) this.spawnDropItem(entity, release);
      }
   }

   private void distributeTieredRyo(EntityLivingBase entity, BossGroup group, int pool) {
      if (group == null || group.maxHp <= 0) return;

      List<Map.Entry<UUID, Double>> ranked = new ArrayList<>(group.damageLedger.entrySet());
      ranked.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

      double totalQualifyingDamage = 0;
      for (Map.Entry<UUID, Double> e : ranked) {
         if (e.getValue() >= group.maxHp * 0.01) totalQualifyingDamage += e.getValue();
      }

      if (totalQualifyingDamage <= 0) return;

      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      for (Map.Entry<UUID, Double> entry : ranked) {
         if (entry.getValue() < group.maxHp * 0.01) continue;

         EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(entry.getKey());
         if (player != null) {
            int share = (int) Math.round((entry.getValue() / totalQualifyingDamage) * pool);
            RyoRewardHelper.grantRyo(player, share);
            player.sendMessage(new TextComponentString("§6§l[World Boss] §ePremio danno: §a+" + share + " Ryo"));
         }
      }
   }

   private void announceTopDamage(EntityLivingBase entity, BossGroup group, String bossName) {
      List<Map.Entry<UUID, Double>> ranked = new ArrayList<>(group.damageLedger.entrySet());
      ranked.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

      StringBuilder sb = new StringBuilder("§6§l" + bossName + " sconfitto! Top 3: ");
      for (int i = 0; i < Math.min(3, ranked.size()); i++) {
         Map.Entry<UUID, Double> e = ranked.get(i);
         EntityPlayer p = entity.world.getPlayerEntityByUUID(e.getKey());
         String name = (p != null) ? p.getName() : "Sconosciuto";
         int pct = (int) Math.round((e.getValue() / group.maxHp) * 100);
         sb.append("§e").append(name).append(" §7(").append(pct).append("%) ");
      }
      this.broadcastMessage(sb.toString());
   }

   private void pickBossLocation() {
      if (this.cachedWorld == null) return;
      Random rand = this.cachedWorld.rand;
      this.pendingSpawnType = rand.nextInt(5);

      int x = -5000 + rand.nextInt(10000);
      int z = -5000 + rand.nextInt(10000);
      this.pendingLocation = new BlockPos(x, 70, z);
      this.hasPendingSpawn = true;
      this.pendingSpawnSetAt = this.cachedWorld.getTotalWorldTime();

      this.broadcastMessage("§c§l[WORLD BOSS] §6Avvistamento vicino a §b" + x + ", " + z + "§6!");
   }

   private void spawnWorldBosses() {
      if (this.cachedWorld == null || this.pendingLocation == null) return;

      BossGroup group = new BossGroup(this.pendingSpawnType, this.cachedWorld.getTotalWorldTime(), this.pendingLocation);

      if (this.pendingSpawnType >= 1 && this.pendingSpawnType <= 3) {
         spawnEntity(TB_ENTITY_IDS[this.pendingSpawnType-1], group);
      } else {
         int idx = (this.pendingSpawnType == 4) ? 4 : 0;
         String[] duo = DUO_CONFIGS[idx];
         if (duo != null) {
            for (String s : duo) spawnEntity(MODID + ":" + s, group);
         }
      }

      this.activeGroups.add(group);
      this.hasPendingSpawn = false;
      this.broadcastMessage("§c§l[WORLD BOSS] §eIl Boss è apparso!");
   }

   private void spawnEntity(String id, BossGroup group) {
      Entity e = EntityList.createEntityByIDFromName(new ResourceLocation(id), this.cachedWorld);
      if (e instanceof EntityLivingBase) {
         EntityLivingBase el = (EntityLivingBase) e;
         el.setPosition(group.location.getX(), group.location.getY(), group.location.getZ());
         el.getEntityData().setBoolean("chakraWorldBoss", true);
         el.getEntityData().setInteger("chakraWorldBossDuo", group.spawnType);
         this.cachedWorld.spawnEntity(el);
         group.entityIds.add(el.getEntityId());
         group.maxHp += (double)el.getMaxHealth();
      }
   }

   private void spawnDropItem(EntityLivingBase entity, Item item) {
      EntityItem drop = new EntityItem(entity.world, entity.posX, entity.posY + 0.5, entity.posZ, new ItemStack(item, 1));
      drop.setPickupDelay(10);
      entity.world.spawnEntity(drop);
   }

   private void spawnDropParticles(EntityLivingBase entity) {
      if (entity.world instanceof WorldServer) {
         ((WorldServer)entity.world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, entity.posX, entity.posY + 1, entity.posZ, 50, 0.5, 0.5, 0.5, 0.05);
      }
   }

   private void broadcastMessage(String msg) {
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server != null) server.getPlayerList().sendMessage(new TextComponentString(msg));
   }

   public static class BossGroup {
      public final int spawnType;
      public final long spawnTimeTicks;
      public final BlockPos location;
      public final List<Integer> entityIds = new ArrayList<>();
      public final Map<UUID, Double> damageLedger = new HashMap<>();
      public double maxHp = 0;

      public BossGroup(int type, long time, BlockPos pos) {
         this.spawnType = type;
         this.spawnTimeTicks = time;
         this.location = pos;
      }
   }
}