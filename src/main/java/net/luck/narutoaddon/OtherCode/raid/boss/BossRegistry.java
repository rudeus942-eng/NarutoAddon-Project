package net.luck.narutoaddon.OtherCode.raid.boss;

import net.luck.narutoaddon.OtherCode.entity.EntityHashirama;
import net.luck.narutoaddon.OtherCode.entity.EntityRaidBossItachi;
import net.luck.narutoaddon.OtherCode.entity.EntityRaidBossKimimaro;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Function;

public class BossRegistry {
   private static final Map<String, BossEntry> registry = new LinkedHashMap();

   public static void register(String bossId, String displayName, String description, Function<World, EntityLivingBase> factory) {
      registry.put(bossId.toLowerCase(), new BossEntry(bossId, displayName, description, factory));
   }

   public static void register(BossEntry entry) {
      registry.put(entry.bossId.toLowerCase(), entry);
   }

   public static void init() {
      registry.clear();
      register("hashirama", "Hashirama Senju", "The First Hokage, God of Shinobi. Master of Wood Release.", (world) -> new EntityHashirama.EntityCustom(world));
      register("itachi", "Itachi Uchiha", "The Uchiha prodigy. Master of Sharingan, Amaterasu, and Susanoo.", (world) -> new EntityRaidBossItachi.EntityCustom(world));
      register("kimimaro", "Kimimaro Kaguya", "Last of the Kaguya clan. Master of Shikotsumyaku, the Dead Bone Pulse.", (world) -> new EntityRaidBossKimimaro.EntityCustom(world));
   }

   public static boolean bossExists(String bossId) {
      return registry.containsKey(bossId.toLowerCase());
   }

   public static BossEntry getEntry(String bossId) {
      return (BossEntry)registry.get(bossId.toLowerCase());
   }

   public static String getBossDisplayName(String bossId) {
      BossEntry entry = (BossEntry)registry.get(bossId.toLowerCase());
      return entry != null ? entry.displayName : bossId;
   }

   public static String getBossDescription(String bossId) {
      BossEntry entry = (BossEntry)registry.get(bossId.toLowerCase());
      return entry != null ? entry.description : "";
   }

   public static EntityLivingBase createBoss(String bossId, World world) {
      BossEntry entry = (BossEntry)registry.get(bossId.toLowerCase());
      return entry != null && entry.factory != null ? (EntityLivingBase)entry.factory.apply(world) : null;
   }

   public static IRaidBoss createRaidBoss(String bossId, World world) {
      EntityLivingBase entity = createBoss(bossId, world);
      return entity instanceof IRaidBoss ? (IRaidBoss)entity : null;
   }

   public static Set<String> getAllBossIds() {
      return Collections.unmodifiableSet(registry.keySet());
   }

   public static Collection<BossEntry> getAllEntries() {
      return Collections.unmodifiableCollection(registry.values());
   }

   public static int getBossCount() {
      return registry.size();
   }

   public static class BossEntry {
      public final String bossId;
      public final String displayName;
      public final String description;
      public final Function<World, EntityLivingBase> factory;

      public BossEntry(String bossId, String displayName, String description, Function<World, EntityLivingBase> factory) {
         this.bossId = bossId;
         this.displayName = displayName;
         this.description = description;
         this.factory = factory;
      }
   }
}
